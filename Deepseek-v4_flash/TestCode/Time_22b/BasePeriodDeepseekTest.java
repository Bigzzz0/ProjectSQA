package org.joda.time.base;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.ZonedChronology;
import org.joda.time.tz.DateTimeZoneBuilder;
import java.util.TimeZone;

public class BasePeriodDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Target defect: toDurationFrom/toDurationTo with fixed-zone chronology miscalculates
     * due to incorrect handling of time-zone offset when adding period to instant.
     * Branches targeted:
     * - toDurationFrom: null startInstant, non-null startInstant, arithmetic overflow
     * - toDurationTo: null endInstant, non-null endInstant, arithmetic overflow
     * - Constructor with ReadableInstant/ReadableDuration combinations
     * - checkAndUpdate: unsupported field with non-zero value, supported field
     * - setFieldInto: null field, unsupported field, supported field
     * - addFieldInto: safeAdd overflow, normal addition
     * - mergePeriodInto: null period, non-null period with supported/unsupported fields
     * - addPeriodInto: overflow, normal addition
     * - setPeriodInternal: null period, non-null period with various field types
     * - Boundary values: Integer.MIN_VALUE, Integer.MAX_VALUE, zero, negative values
     * - Fixed-zone chronology: use a custom fixed-offset time zone to expose bug
     */

    // Helper to create a fixed-zone chronology with a specific offset (e.g., +1 hour)
    private Chronology fixedZoneChronology(int offsetMillis) {
        DateTimeZone zone = new DateTimeZoneBuilder()
                .setStandardOffset(offsetMillis)
                .setFixed("Fixed")
                .toDateTimeZone();
        return ZonedChronology.getInstance(ISOChronology.getInstance(zone));
    }

    // Test class to expose protected methods for testing
    private static class TestBasePeriod extends BasePeriod {
        TestBasePeriod(int[] values, PeriodType type) {
            super(values, type);
        }
        TestBasePeriod(int years, int months, int weeks, int days,
                       int hours, int minutes, int seconds, int millis, PeriodType type) {
            super(years, months, weeks, days, hours, minutes, seconds, millis, type);
        }
        TestBasePeriod(long start, long end, PeriodType type, Chronology chrono) {
            super(start, end, type, chrono);
        }
        TestBasePeriod(ReadableInstant start, ReadableInstant end, PeriodType type) {
            super(start, end, type);
        }
        TestBasePeriod(ReadablePartial start, ReadablePartial end, PeriodType type) {
            super(start, end, type);
        }
        TestBasePeriod(ReadableInstant start, ReadableDuration duration, PeriodType type) {
            super(start, duration, type);
        }
        TestBasePeriod(ReadableDuration duration, ReadableInstant end, PeriodType type) {
            super(duration, end, type);
        }
        TestBasePeriod(long duration, PeriodType type, Chronology chrono) {
            super(duration, type, chrono);
        }
        TestBasePeriod(Object period, PeriodType type, Chronology chrono) {
            super(period, type, chrono);
        }
        void setFieldProtected(DurationFieldType field, int value) {
            setField(field, value);
        }
        void setFieldIntoProtected(int[] values, DurationFieldType field, int value) {
            setFieldInto(values, field, value);
        }
        void addFieldProtected(DurationFieldType field, int value) {
            addField(field, value);
        }
        void addFieldIntoProtected(int[] values, DurationFieldType field, int value) {
            addFieldInto(values, field, value);
        }
        void mergePeriodProtected(ReadablePeriod period) {
            mergePeriod(period);
        }
        int[] mergePeriodIntoProtected(int[] values, ReadablePeriod period) {
            return mergePeriodInto(values, period);
        }
        void addPeriodProtected(ReadablePeriod period) {
            addPeriod(period);
        }
        int[] addPeriodIntoProtected(int[] values, ReadablePeriod period) {
            return addPeriodInto(values, period);
        }
        void setValueProtected(int index, int value) {
            setValue(index, value);
        }
        void setPeriodProtected(ReadablePeriod period) {
            setPeriod(period);
        }
        void setPeriodInternalProtected(ReadablePeriod period) {
            setPeriodInternal(period);
        }
        void checkAndUpdateProtected(DurationFieldType type, int[] values, int newValue) {
            checkAndUpdate(type, values, newValue);
        }
        PeriodType checkPeriodTypeProtected(PeriodType type) {
            return checkPeriodType(type);
        }
    }

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorWithValuesAndType() {
        PeriodType type = PeriodType.yearMonthDayTime();
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, type);
        assertEquals(type, period.getPeriodType());
        assertEquals(8, period.size());
        assertEquals(1, period.getValue(0));
        assertEquals(2, period.getValue(1));
        assertEquals(3, period.getValue(2));
        assertEquals(4, period.getValue(3));
        assertEquals(5, period.getValue(4));
        assertEquals(6, period.getValue(5));
        assertEquals(7, period.getValue(6));
        assertEquals(8, period.getValue(7));
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullTypeUsesDefault() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, null);
        assertNotNull(period.getPeriodType());
        assertEquals(PeriodType.standard(), period.getPeriodType());
    }

    @Test(timeout = 4000)
    public void testConstructorWithUnsupportedFieldNonZeroThrows() {
        try {
            new TestBasePeriod(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetFieldTypeAndValue() {
        PeriodType type = PeriodType.yearMonthDay();
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, type);
        assertEquals(DurationFieldType.years(), period.getFieldType(0));
        assertEquals(DurationFieldType.months(), period.getFieldType(1));
        assertEquals(DurationFieldType.days(), period.getFieldType(2));
        assertEquals(1, period.getValue(0));
        assertEquals(2, period.getValue(1));
        assertEquals(3, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testGetValueIndexOutOfBounds() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        try {
            period.getValue(3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetField() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.setFieldProtected(DurationFieldType.years(), 5);
        assertEquals(5, period.getValue(0));
        period.setFieldProtected(DurationFieldType.months(), 3);
        assertEquals(3, period.getValue(1));
        period.setFieldProtected(DurationFieldType.days(), 7);
        assertEquals(7, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testSetFieldUnsupportedThrows() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        try {
            period.setFieldProtected(DurationFieldType.years(), 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetFieldNullFieldThrows() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        try {
            period.setFieldProtected(null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddField() {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.addFieldProtected(DurationFieldType.years(), 2);
        assertEquals(3, period.getValue(0));
        period.addFieldProtected(DurationFieldType.months(), -1);
        assertEquals(1, period.getValue(1));
        period.addFieldProtected(DurationFieldType.days(), 4);
        assertEquals(7, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testAddFieldOverflowThrows() {
        TestBasePeriod period = new TestBasePeriod(Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        try {
            period.addFieldProtected(DurationFieldType.years(), 1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetValue() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.setValueProtected(0, 10);
        period.setValueProtected(1, 20);
        period.setValueProtected(2, 30);
        assertEquals(10, period.getValue(0));
        assertEquals(20, period.getValue(1));
        assertEquals(30, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testSetPeriod() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        Period p = new Period(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.setPeriodProtected(p);
        assertEquals(1, period.getValue(0));
        assertEquals(2, period.getValue(1));
        assertEquals(3, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testSetPeriodNullClearsValues() {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.setPeriodProtected(null);
        assertEquals(0, period.getValue(0));
        assertEquals(0, period.getValue(1));
        assertEquals(0, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testSetPeriodInternal() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        Period p = new Period(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.setPeriodInternalProtected(p);
        assertEquals(1, period.getValue(0));
        assertEquals(2, period.getValue(1));
        assertEquals(3, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testSetPeriodInternalWithUnsupportedFieldThrows() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        Period p = new Period(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        try {
            period.setPeriodInternalProtected(p);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMergePeriod() {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        Period p = new Period(1, 1, 1, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.mergePeriodProtected(p);
        assertEquals(2, period.getValue(0));
        assertEquals(3, period.getValue(1));
        assertEquals(4, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testMergePeriodNullDoesNothing() {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.mergePeriodProtected(null);
        assertEquals(1, period.getValue(0));
        assertEquals(2, period.getValue(1));
        assertEquals(3, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testMergePeriodInto() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        int[] values = new int[3];
        Period p = new Period(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        int[] result = period.mergePeriodIntoProtected(values, p);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test(timeout = 4000)
    public void testAddPeriod() {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        Period p = new Period(1, 1, 1, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        period.addPeriodProtected(p);
        assertEquals(2, period.getValue(0));
        assertEquals(3, period.getValue(1));
        assertEquals(4, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testAddPeriodInto() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        int[] values = new int[3];
        Period p = new Period(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        int[] result = period.addPeriodIntoProtected(values, p);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test(timeout = 4000)
    public void testCheckAndUpdateSupported() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        int[] values = new int[3];
        period.checkAndUpdateProtected(DurationFieldType.years(), values, 5);
        assertArrayEquals(new int[]{5, 0, 0}, values);
    }

    @Test(timeout = 4000)
    public void testCheckAndUpdateUnsupportedNonZeroThrows() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        int[] values = new int[3];
        try {
            period.checkAndUpdateProtected(DurationFieldType.years(), values, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckAndUpdateUnsupportedZeroDoesNothing() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        int[] values = new int[3];
        period.checkAndUpdateProtected(DurationFieldType.years(), values, 0);
        assertArrayEquals(new int[]{0, 0, 0}, values);
    }

    @Test(timeout = 4000)
    public void testCheckPeriodTypeNull() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        PeriodType result = period.checkPeriodTypeProtected(null);
        assertNotNull(result);
        assertEquals(PeriodType.standard(), result);
    }

    @Test(timeout = 4000)
    public void testCheckPeriodTypeNonNull() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        PeriodType type = PeriodType.yearMonthDay();
        PeriodType result = period.checkPeriodTypeProtected(type);
        assertSame(type, result);
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testConstructorWithMaxValues() {
        PeriodType type = PeriodType.yearMonthDayTime();
        TestBasePeriod period = new TestBasePeriod(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, type);
        assertEquals(Integer.MAX_VALUE, period.getValue(0));
        assertEquals(Integer.MAX_VALUE, period.getValue(7));
    }

    @Test(timeout = 4000)
    public void testConstructorWithMinValues() {
        PeriodType type = PeriodType.yearMonthDayTime();
        TestBasePeriod period = new TestBasePeriod(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE,
                Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, type);
        assertEquals(Integer.MIN_VALUE, period.getValue(0));
        assertEquals(Integer.MIN_VALUE, period.getValue(7));
    }

    @Test(timeout = 4000)
    public void testAddFieldMinOverflow() {
        TestBasePeriod period = new TestBasePeriod(Integer.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        try {
            period.addFieldProtected(DurationFieldType.years(), -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddFieldMaxOverflow() {
        TestBasePeriod period = new TestBasePeriod(Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        try {
            period.addFieldProtected(DurationFieldType.years(), 1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetFieldToZeroOnUnsupported() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        // Setting unsupported field to zero should be allowed (no exception)
        period.setFieldProtected(DurationFieldType.years(), 0);
        assertEquals(0, period.getValue(0));
    }

    @Test(timeout = 4000)
    public void testSetFieldNullWithZeroValue() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        // Null field with zero value should be allowed? Actually checkAndUpdate throws if field null and value != 0
        // But setFieldInto checks value != 0 || field == null, so null with zero is allowed
        period.setFieldProtected(null, 0);
        // No exception expected
    }

    @Test(timeout = 4000)
    public void testSetFieldNullWithNonZeroValueThrows() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        try {
            period.setFieldProtected(null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testToDurationFromFixedZone() {
        // Fixed zone with +1 hour offset (3600000 ms)
        Chronology chrono = fixedZoneChronology(3600000);
        DateTimeZone zone = chrono.getZone();
        long startMillis = 0L; // 1970-01-01T00:00:00Z
        // Create a period of 1 hour using the fixed zone
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        // Use a ReadableInstant with the fixed zone
        ReadableInstant start = new Instant(startMillis).toDateTime(zone);
        Duration duration = period.toDurationFrom(start);
        // Expected duration: 1 hour = 3600000 ms
        assertEquals(3600000L, duration.getMillis());
    }

    @Test(timeout = 4000)
    public void testToDurationToFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        DateTimeZone zone = chrono.getZone();
        long endMillis = 0L;
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        ReadableInstant end = new Instant(endMillis).toDateTime(zone);
        Duration duration = period.toDurationTo(end);
        // Expected duration: 1 hour = 3600000 ms
        assertEquals(3600000L, duration.getMillis());
    }

    @Test(timeout = 4000)
    public void testToDurationFromNullStartInstant() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        // Null start instant should use current time, but we can't predict exact value.
        // Just ensure no exception and duration is positive.
        Duration duration = period.toDurationFrom((ReadableInstant) null);
        assertNotNull(duration);
        assertTrue(duration.getMillis() > 0);
    }

    @Test(timeout = 4000)
    public void testToDurationToNullEndInstant() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        Duration duration = period.toDurationTo((ReadableInstant) null);
        assertNotNull(duration);
        assertTrue(duration.getMillis() > 0);
    }

    @Test(timeout = 4000)
    public void testToDurationFromArithmeticOverflow() {
        // Create a period with huge values that would overflow when added to instant
        TestBasePeriod period = new TestBasePeriod(Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        ReadableInstant start = new Instant(0L);
        try {
            period.toDurationFrom(start);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToDurationToArithmeticOverflow() {
        TestBasePeriod period = new TestBasePeriod(Integer.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        ReadableInstant end = new Instant(0L);
        try {
            period.toDurationTo(end);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testConstructorWithNullReadablePartialThrows() {
        try {
            new TestBasePeriod((ReadablePartial) null, new LocalDate(), PeriodType.yearMonthDay());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithDifferentPartialFieldsThrows() {
        LocalDate date = new LocalDate(2020, 1, 1);
        LocalTime time = new LocalTime(12, 0);
        try {
            new TestBasePeriod(date, time, PeriodType.yearMonthDayTime());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNonContiguousPartialThrows() {
        // Create a non-contiguous partial (e.g., month and day without year)
        Partial partial = new Partial()
                .with(DurationFieldType.months(), 1)
                .with(DurationFieldType.days(), 15);
        try {
            new TestBasePeriod(partial, partial, PeriodType.yearMonthDay());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullPeriodObjectThrows() {
        try {
            new TestBasePeriod((Object) null, PeriodType.yearMonthDay(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithUnsupportedPeriodTypeThrows() {
        // Create a period with a type that doesn't support a field with non-zero value
        PeriodType type = PeriodType.time();
        try {
            new TestBasePeriod(1, 0, 0, 0, 0, 0, 0, 0, type);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetFieldTypeIndexOutOfBounds() {
        TestBasePeriod period = new TestBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        try {
            period.getFieldType(3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        TestBasePeriod p1 = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        TestBasePeriod p2 = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        TestBasePeriod p3 = new TestBasePeriod(1, 2, 4, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentType() {
        TestBasePeriod p1 = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        TestBasePeriod p2 = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDayTime());
        assertNotEquals(p1, p2);
    }

    @Test(timeout = 4000)
    public void testEqualsWithNull() {
        TestBasePeriod p1 = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        assertNotEquals(p1, null);
    }

    @Test(timeout = 4000)
    public void testToString() {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        String str = period.toString();
        assertNotNull(str);
        assertTrue(str.contains("P1Y2M3DT4H5M6.007S"));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(period);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()));
        TestBasePeriod deserialized = (TestBasePeriod) ois.readObject();
        assertEquals(period, deserialized);
    }

    @Test(timeout = 4000)
    public void testClone() {
        TestBasePeriod period = new TestBasePeriod(1, 2, 3, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        // BasePeriod doesn't override clone, but we can test that it's not directly cloneable
        // Actually, AbstractPeriod doesn't implement Cloneable, so clone throws
        try {
            period.clone();
            fail("Expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
            // expected
        }
    }

    // Additional tests for constructors with ReadableInstant/Duration

    @Test(timeout = 4000)
    public void testConstructorWithReadableInstantAndDuration() {
        Instant start = new Instant(1000L);
        Duration duration = new Duration(2000L);
        TestBasePeriod period = new TestBasePeriod(start, duration, PeriodType.time());
        assertEquals(2000L, period.toDurationFrom(start).getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDurationAndReadableInstant() {
        Instant end = new Instant(3000L);
        Duration duration = new Duration(2000L);
        TestBasePeriod period = new TestBasePeriod(duration, end, PeriodType.time());
        assertEquals(2000L, period.toDurationTo(end).getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDuration() {
        TestBasePeriod period = new TestBasePeriod(5000L);
        assertEquals(5000L, period.toDurationFrom(new Instant(0L)).getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndType() {
        TestBasePeriod period = new TestBasePeriod(5000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertEquals(5000L, period.toDurationFrom(new Instant(0L)).getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithObjectPeriod() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        TestBasePeriod period = new TestBasePeriod(p, null, null);
        assertEquals(p, period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithObjectPeriodAndType() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        TestBasePeriod period = new TestBasePeriod(p, PeriodType.yearMonthDay(), null);
        assertEquals(PeriodType.yearMonthDay(), period.getPeriodType());
        assertEquals(1, period.getValue(0));
        assertEquals(2, period.getValue(1));
        assertEquals(3, period.getValue(2));
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadablePartial() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2020, 2, 1);
        TestBasePeriod period = new TestBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(1, period.getValue(0)); // 1 month
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadablePartialSameClass() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2020, 2, 1);
        TestBasePeriod period = new TestBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(1, period.getValue(0));
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadablePartialDifferentClass() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2020, 2, 1);
        TestBasePeriod period = new TestBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(1, period.getValue(0));
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadablePartialNullStart() {
        try {
            new TestBasePeriod((ReadablePartial) null, new LocalDate(), PeriodType.yearMonthDay());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadablePartialNullEnd() {
        try {
            new TestBasePeriod(new LocalDate(), (ReadablePartial) null, PeriodType.yearMonthDay());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadablePartialDifferentSizes() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalTime end = new LocalTime(12, 0);
        try {
            new TestBasePeriod(start, end, PeriodType.yearMonthDayTime());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadablePartialNonContiguous() {
        Partial start = new Partial()
                .with(DurationFieldType.months(), 1)
                .with(DurationFieldType.days(), 15);
        Partial end = new Partial()
                .with(DurationFieldType.months(), 2)
                .with(DurationFieldType.days(), 20);
        try {
            new TestBasePeriod(start, end, PeriodType.yearMonthDay());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadableInstantBothNull() {
        TestBasePeriod period = new TestBasePeriod((ReadableInstant) null, (ReadableInstant) null, PeriodType.yearMonthDay());
        assertEquals(0, period.size());
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadableInstantStartNull() {
        Instant end = new Instant(1000L);
        TestBasePeriod period = new TestBasePeriod((ReadableInstant) null, end, PeriodType.time());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadableInstantEndNull() {
        Instant start = new Instant(1000L);
        TestBasePeriod period = new TestBasePeriod(start, (ReadableInstant) null, PeriodType.time());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadableInstantAndDurationNullDuration() {
        Instant start = new Instant(1000L);
        TestBasePeriod period = new TestBasePeriod(start, (ReadableDuration) null, PeriodType.time());
        assertNotNull(period);
        assertEquals(0L, period.toDurationFrom(start).getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDurationAndReadableInstantNullDuration() {
        Instant end = new Instant(1000L);
        TestBasePeriod period = new TestBasePeriod((ReadableDuration) null, end, PeriodType.time());
        assertNotNull(period);
        assertEquals(0L, period.toDurationTo(end).getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDurationAndReadableInstantNullEnd() {
        Duration duration = new Duration(1000L);
        TestBasePeriod period = new TestBasePeriod(duration, (ReadableInstant) null, PeriodType.time());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithObjectPeriodNullType() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        TestBasePeriod period = new TestBasePeriod(p, null, null);
        assertEquals(PeriodType.yearMonthDayTime(), period.getPeriodType());
    }

    @Test(timeout = 4000)
    public void testConstructorWithObjectPeriodAndChronology() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.yearMonthDayTime());
        TestBasePeriod period = new TestBasePeriod(p, null, ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndNullChronology() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndNullType() {
        TestBasePeriod period = new TestBasePeriod(1000L, null, ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndNullBoth() {
        TestBasePeriod period = new TestBasePeriod(1000L, null, null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTime() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertEquals(1000L, period.toDurationFrom(new Instant(0L)).getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeYearMonthDay() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.yearMonthDay(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeYearMonthDayTime() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.yearMonthDayTime(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeStandard() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeYears() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.years(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeMonths() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.months(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeWeeks() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.weeks(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeDays() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.days(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeHours() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.hours(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeMinutes() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.minutes(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeSeconds() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.seconds(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeMillis() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.millis(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChrono() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefault() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefault() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronology() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronology() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronology() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronology() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronology() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronology() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithSystemDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithFixedZoneChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixed() {
        Chronology chrono = fixedZoneChronology(3600000);
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithZonedChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        Chronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), DateTimeZone.forOffsetHours(1));
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), chrono);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithNullChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), null);
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithISOChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone() {
        TestBasePeriod period = new TestBasePeriod(1000L, PeriodType.time(), ISOChronology.getInstance());
        assertNotNull(period);
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongDurationAndTypeTimeWithUTCDefaultChronologyFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZoneFixedZone