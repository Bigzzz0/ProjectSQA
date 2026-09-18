package org.joda.time.format;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.UnsupportedDurationField;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------
 * Target Class: org.joda.time.format.DateTimeParserBucket
 * Target Flaws: Defects4J Time-7 (Inconsistent sorting and calculation between weekyear/year,
 *               month, and week fields leading to wrong parsed calendar dates e.g. 2008-12-29
 *               instead of 2010-01-04).
 *
 * Decision / Branch Coverage:
 * 1. Constructors:
 *    - null Chronology -> fallback to ISOChronology.getInstance()
 *    - null Locale -> fallback to Locale.getDefault()
 *    - Pivot year and default year propagation
 * 2. Zone & Offset State Mutations:
 *    - setZone(UTC) -> iZone set to null
 *    - setZone(non-UTC) -> iZone set to target, offset reset to 0
 *    - setOffset(int) -> iOffset set, zone reset to null
 * 3. Array Expansion & State Restoration:
 *    - saveField() with count == length (expanding array > 8 elements)
 *    - saveField() when iSavedFieldsShared is true (cloning array after restoreState)
 *    - restoreState() with invalid object, cross-bucket instance, and lower count
 * 4. computeMillis() Execution Paths:
 *    - iSavedFieldsShared clone path
 *    - Insertion sort (high <= 10) vs Arrays.sort (high > 10)
 *    - Altering base year when first field is month/day (compareReverse logic)
 *    - Field setting: text value vs integer value, resetFields true vs false
 *    - IllegalFieldValueException with text prepending vs null text
 *    - Zone offset evaluation: iZone == null (using offset) vs iZone != null
 *    - DST gap offset inconsistency (throwing IllegalArgumentException for non-existent local time)
 * 5. Duration Field Ordering:
 *    - compareReverse() covering null, unsupported, and supported DurationFields
 * ---------------------------------------------------------------------------------------------
 */
public class DateTimeParserBucketGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInitializationAndGetters() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeParserBucket bucket = new DateTimeParserBucket(1000L, chrono, Locale.GERMANY, 2050, 1995);

        assertEquals(chrono, bucket.getChronology());
        assertEquals(Locale.GERMANY, bucket.getLocale());
        assertNull(bucket.getZone()); // UTC sets zone to null internally
        assertEquals(0, bucket.getOffset());
        assertEquals(Integer.valueOf(2050), bucket.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testZoneAndOffsetSwitching() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        DateTimeZone nyZone = DateTimeZone.forID("America/New_York");

        bucket.setZone(nyZone);
        assertEquals(nyZone, bucket.getZone());
        assertEquals(0, bucket.getOffset());

        bucket.setOffset(3600000);
        assertNull(bucket.getZone());
        assertEquals(3600000, bucket.getOffset());

        bucket.setZone(DateTimeZone.UTC);
        assertNull(bucket.getZone());
        assertEquals(0, bucket.getOffset());

        bucket.setPivotYear(1985);
        assertEquals(Integer.valueOf(1985), bucket.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testSaveFieldWithTextAndResetFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2021);
        bucket.saveField(DateTimeFieldType.monthOfYear(), "March", Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);

        long computed = bucket.computeMillis(true);
        LocalDate date = new LocalDate(computed, DateTimeZone.UTC);
        assertEquals(new LocalDate(2021, 3, 15), date);
    }

    @Test(timeout = 4000)
    public void testComputeMillisWithExplicitOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);
        bucket.setOffset(3600000); // +1 hour

        long millis = bucket.computeMillis(false);
        // UTC 2020-01-01T00:00:00Z is 1577836800000L.
        // With +1h offset, UTC instant is 1577836800000 - 3600000
        assertEquals(1577836800000L - 3600000L, millis);
    }

    @Test(timeout = 4000)
    public void testComputeMillisWithDefaultYearFallbackForMonthDay() {
        // When first field is month or day, iDefaultYear is injected
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US, null, 2004);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 2);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 29); // Leap day valid in 2004

        long millis = bucket.computeMillis(true);
        LocalDate date = new LocalDate(millis, DateTimeZone.UTC);
        assertEquals(new LocalDate(2004, 2, 29), date);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsWithNullArguments() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, null, null);
        assertNotNull(bucket.getChronology());
        assertEquals(ISOChronology.getInstanceUTC(), bucket.getChronology());
        assertEquals(Locale.getDefault(), bucket.getLocale());
        assertNull(bucket.getPivotYear());

        DateTimeParserBucket bucket2 = new DateTimeParserBucket(0L, null, null, null);
        assertNull(bucket2.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testArrayExpansionPastEightFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        // Add 9 fields to force dynamic resizing of iSavedFields array (initial cap = 8)
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 6);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 10);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 20);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 30);
        bucket.saveField(DateTimeFieldType.millisOfSecond(), 400);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 1);
        bucket.saveField(DateTimeFieldType.dayOfYear(), 167); // 9th field

        long millis = bucket.computeMillis(false);
        assertTrue(millis > 0L);
    }

    @Test(timeout = 4000)
    public void testLargeFieldCountTriggersArraysSort() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        // Exceed 10 fields to trigger Arrays.sort(array, 0, high) instead of insertion sort
        bucket.saveField(DateTimeFieldType.era(), 1);
        bucket.saveField(DateTimeFieldType.centuryOfEra(), 20);
        bucket.saveField(DateTimeFieldType.yearOfCentury(), 20);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.weekOfWeek(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 12);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 0);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 0); // 11th field

        long millis = bucket.computeMillis(false);
        assertTrue(millis > 0L);
    }

    @Test(timeout = 4000)
    public void testCompareReverseBoundaries() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DurationField years = chrono.years();
        DurationField months = chrono.months();
        DurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.years());

        // Null and unsupported duration permutations
        assertEquals(0, DateTimeParserBucket.compareReverse(null, null));
        assertEquals(-1, DateTimeParserBucket.compareReverse(null, years));
        assertEquals(1, DateTimeParserBucket.compareReverse(years, null));
        assertEquals(0, DateTimeParserBucket.compareReverse(unsupported, unsupported));
        assertEquals(-1, DateTimeParserBucket.compareReverse(unsupported, years));
        assertEquals(1, DateTimeParserBucket.compareReverse(years, unsupported));

        // Supported comparison: years duration > months duration -> compareReverse returns -1
        assertTrue(DateTimeParserBucket.compareReverse(years, months) < 0);
        assertTrue(DateTimeParserBucket.compareReverse(months, years) > 0);
        assertEquals(0, DateTimeParserBucket.compareReverse(years, years));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Time-7)
    // =========================================================================

    /**
     * Targets Defects4J Time-7: parsing weekyear, month, and week of week for 2010.
     * In the defective version, week 1 of 2010 is computed as 2008-12-29 instead of 2010-01-04.
     */
    @Test(timeout = 4000)
    public void testDefectTargetParseLocalDateWeekyearMonthWeek2010() {
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withLocale(Locale.UK);
        LocalDate result = f.parseLocalDate("2010-01-01");
        assertEquals("Defect Time-7 reproduced: weekyear 2010 month 01 week 01 resolved incorrectly",
                new LocalDate(2010, 1, 4), result);
    }

    @Test(timeout = 4000)
    public void testDefectTargetParseLocalDateYearMonthWeek2010() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withLocale(Locale.UK);
        LocalDate result = f.parseLocalDate("2010-01-01");
        assertEquals("Defect Time-7 reproduced: year 2010 month 01 week 01 resolved incorrectly",
                new LocalDate(2010, 1, 4), result);
    }

    @Test(timeout = 4000)
    public void testDefectTargetParseLocalDateWeekyearMonthWeek2011() {
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withLocale(Locale.UK);
        LocalDate result = f.parseLocalDate("2011-01-01");
        assertEquals(new LocalDate(2011, 1, 3), result);
    }

    @Test(timeout = 4000)
    public void testDefectTargetParseLocalDateYearMonthWeek2012() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withLocale(Locale.UK);
        LocalDate result = f.parseLocalDate("2012-01-01");
        assertEquals(new LocalDate(2012, 1, 2), result);
    }

    @Test(timeout = 4000)
    public void testDefectTargetBucketDirectWeekyearMonthWeek2010() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.UK, null, 2000);
        bucket.saveField(DateTimeFieldType.weekyear(), 2010);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.weekOfWeek(), 1);

        long computed = bucket.computeMillis(true, "2010-01-01");
        long expected = new LocalDate(2010, 1, 4).toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis();
        assertEquals("Direct bucket calculation failed for weekyear/month/week ordering", expected, computed);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testIllegalFieldValueExceptionWithTextContext() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);
        try {
            bucket.computeMillis(false, "2020-13-01");
            fail("Expected IllegalFieldValueException for month 13");
        } catch (IllegalFieldValueException e) {
            assertTrue(e.getMessage().contains("Cannot parse \"2020-13-01\""));
        }
    }

    @Test(timeout = 4000)
    public void testIllegalFieldValueExceptionWithoutTextContext() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 32);
        try {
            bucket.computeMillis(false, null);
            fail("Expected IllegalFieldValueException for day 32");
        } catch (IllegalFieldValueException e) {
            assertFalse(e.getMessage().contains("Cannot parse"));
        }
    }

    @Test(timeout = 4000)
    public void testDstGapTransitionThrowsIllegalArgumentException() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstance(ny), Locale.US);
        bucket.setZone(ny);

        // 2007-03-11 02:30:00 does not exist in America/New_York (Spring forward gap: 2:00 -> 3:00)
        bucket.saveField(DateTimeFieldType.year(), 2007);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 11);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 2);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);

        try {
            bucket.computeMillis(false, "2007-03-11 02:30");
            fail("Expected IllegalArgumentException for DST gap instant");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Illegal instant due to time zone offset transition"));
            assertTrue(e.getMessage().contains("Cannot parse \"2007-03-11 02:30\""));
        }
    }

    @Test(timeout = 4000)
    public void testDstGapTransitionWithoutText() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstance(ny), Locale.US);
        bucket.setZone(ny);

        bucket.saveField(DateTimeFieldType.year(), 2007);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 11);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 2);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);

        try {
            bucket.computeMillis(false, null);
            fail("Expected IllegalArgumentException for DST gap instant");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Illegal instant due to time zone offset transition"));
            assertFalse(e.getMessage().contains("Cannot parse"));
        }
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle, State Rollback & Isolation
    // =========================================================================

    @Test(timeout = 4000)
    public void testSaveAndRestoreStateSuccess() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2015);
        bucket.setOffset(1000);

        Object state1 = bucket.saveState();
        assertSame(state1, bucket.saveState()); // Idempotency of saveState when unchanged

        bucket.saveField(DateTimeFieldType.monthOfYear(), 8);
        bucket.setOffset(2000);

        boolean restored = bucket.restoreState(state1);
        assertTrue(restored);
        assertEquals(1000, bucket.getOffset());

        // Computing millis should only reflect year 2015 (month 8 was rolled back)
        long millis = bucket.computeMillis(true);
        LocalDate date = new LocalDate(millis + 1000, DateTimeZone.UTC);
        assertEquals(1, date.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testRestoreStateForeignObjectAndForeignBucket() {
        DateTimeParserBucket bucket1 = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        DateTimeParserBucket bucket2 = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);

        Object state1 = bucket1.saveState();

        assertFalse(bucket1.restoreState("foreign_string_object"));
        assertFalse(bucket1.restoreState(null));
        assertFalse("Cannot restore state belonging to another bucket instance", bucket2.restoreState(state1));
    }

    @Test(timeout = 4000)
    public void testSharedSavedFieldsArrayCloningAfterRestore() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        Object state1 = bucket.saveState();

        // Add fields to increase count
        bucket.saveField(DateTimeFieldType.monthOfYear(), 5);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 12);

        // Restoring state to a lower count marks iSavedFieldsShared = true
        assertTrue(bucket.restoreState(state1));

        // Compute millis must clone array if iSavedFieldsShared is true
        long millis1 = bucket.computeMillis(true);
        LocalDate date1 = new LocalDate(millis1, DateTimeZone.UTC);
        assertEquals(2010, date1.getYear());

        // Saving another field after restore must trigger defensive array recreation
        bucket.saveField(DateTimeFieldType.monthOfYear(), 11);
        long millis2 = bucket.computeMillis(true);
        LocalDate date2 = new LocalDate(millis2, DateTimeZone.UTC);
        assertEquals(11, date2.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testDifferentChronologySupport() {
        Chronology bChrono = BuddhistChronology.getInstanceUTC();
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, bChrono, Locale.US);
        DateTimeField yearField = DateTimeFieldType.year().getField(bChrono);

        bucket.saveField(yearField, 2553); // Buddhist year 2553 = CE 2010
        long millis = bucket.computeMillis(true);

        LocalDate date = new LocalDate(millis, DateTimeZone.UTC);
        assertEquals(2010, date.getYear());
    }
}