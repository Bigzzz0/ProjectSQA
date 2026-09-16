package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParserBucket;
import org.joda.time.format.DateTimeParserBucket.SavedField;
import java.lang.reflect.Field;
import java.util.Locale;

public class DateTimeParserBucketDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Target: DateTimeParserBucket.computeMillis(boolean, String) and saveField logic
     * Defect: When parsing weekyear-month-week patterns (e.g., "xxxx-'W'ww-e"), the
     *         bucket fails to correctly adjust the base year when the first saved field
     *         is a week-based field (weekyear, week, dayOfWeek). The defect causes
     *         incorrect year computation, e.g., expected 2010-01-04 but got 2008-12-29.
     *
     * Branches targeted:
     * 1. computeMillis: count > 0, first field duration between months and days
     *    (i.e., week-based fields) -> should save default year.
     * 2. saveField: array expansion when count == length or shared.
     * 3. sort: insertion sort for high <= 10, Arrays.sort for high > 10.
     * 4. restoreState: valid/invalid saved state, nested saves.
     * 5. setZone/setOffset: zone vs offset handling.
     * 6. Boundary: null text, empty text, resetFields true/false.
     * 7. Exception paths: IllegalFieldValueException with/without text.
     * 8. compareTo/compareReverse: null/unsupported duration fields.
     */

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testComputeMillis_basicFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        long millis = bucket.computeMillis();
        assertEquals("2010-01-04T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_resetFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        long millis = bucket.computeMillis(true);
        assertEquals("2010-01-04T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_withText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        long millis = bucket.computeMillis(false, "2010-01-04");
        assertEquals("2010-01-04T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testSaveField_chronologyMismatch() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        // Should not throw; field is obtained from bucket's chronology
        bucket.saveField(DateTimeFieldType.year(), 2010);
        assertEquals(2010, bucket.computeMillis() / 1000 / 60 / 60 / 24 / 365 + 1970); // rough check
    }

    @Test(timeout = 4000)
    public void testSaveField_textValue() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.monthOfYear(), "1", Locale.US);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        long millis = bucket.computeMillis();
        assertEquals("2010-01-04T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testSaveState_restoreState() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        Object state = bucket.saveState();
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        assertTrue(bucket.restoreState(state));
        assertEquals(2010, bucket.computeMillis() / 1000 / 60 / 60 / 24 / 365 + 1970);
    }

    @Test(timeout = 4000)
    public void testRestoreState_invalid() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        assertFalse(bucket.restoreState(new Object()));
        assertFalse(bucket.restoreState(null));
    }

    @Test(timeout = 4000)
    public void testSetZone_getZone() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        assertNull(bucket.getZone()); // UTC is represented as null
        bucket.setZone(DateTimeZone.forID("Europe/London"));
        assertEquals(DateTimeZone.forID("Europe/London"), bucket.getZone());
        bucket.setZone(DateTimeZone.UTC);
        assertNull(bucket.getZone());
    }

    @Test(timeout = 4000)
    public void testSetOffset_getOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        assertEquals(0, bucket.getOffset());
        bucket.setOffset(3600000);
        assertEquals(3600000, bucket.getOffset());
        bucket.setOffset(0);
        assertEquals(0, bucket.getOffset());
    }

    @Test(timeout = 4000)
    public void testGetPivotYear_setPivotYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        assertNull(bucket.getPivotYear());
        bucket.setPivotYear(2050);
        assertEquals(Integer.valueOf(2050), bucket.getPivotYear());
        bucket.setPivotYear(null);
        assertNull(bucket.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testGetChronology_getLocale() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.FRANCE);
        assertEquals(ISOChronology.getInstanceUTC(), bucket.getChronology());
        assertEquals(Locale.FRANCE, bucket.getLocale());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testComputeMillis_noFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        assertEquals(0L, bucket.computeMillis());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_emptyText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        long millis = bucket.computeMillis(false, "");
        assertEquals("2010-01-01T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testSaveField_manyFields_expandArray() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        for (int i = 0; i < 20; i++) {
            bucket.saveField(DateTimeFieldType.year(), 2000 + i);
        }
        // Should not throw; array expansion works
        assertTrue(bucket.computeMillis() > 0);
    }

    @Test(timeout = 4000)
    public void testSaveField_sharedArray_expand() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        Object state = bucket.saveState();
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        // After restore, array is shared; next save should expand
        bucket.restoreState(state);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        assertEquals(2010, bucket.computeMillis() / 1000 / 60 / 60 / 24 / 365 + 1970);
    }

    @Test(timeout = 4000)
    public void testComputeMillis_zoneOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.setOffset(3600000);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        long millis = bucket.computeMillis();
        assertEquals("2010-01-01T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_zoneTransition() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.setZone(DateTimeZone.forID("America/New_York"));
        bucket.saveField(DateTimeFieldType.year(), 2010);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 14);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 2);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);
        try {
            bucket.computeMillis();
            fail("Expected IllegalArgumentException for zone transition");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Cannot parse"));
        }
    }

    @Test(timeout = 4000)
    public void testComputeMillis_invalidFieldValue() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);
        try {
            bucket.computeMillis();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testComputeMillis_invalidFieldValueWithText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);
        try {
            bucket.computeMillis(false, "invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Cannot parse \"invalid\""));
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Directly targets the known defect: parsing weekyear-month-week patterns.
     * The bug causes incorrect year when the first saved field is week-based.
     * Expected: 2010-01-04 (Monday of week 1, 2010) but defective version returns 2008-12-29.
     */
    @Test(timeout = 4000)
    public void testParseLocalDate_weekyear_month_week_2010() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendWeekyear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2010-01-01-1");
        // The correct date for weekyear 2010, week 1, day 1 (Monday) is 2010-01-04
        assertEquals("2010-01-04", result.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_weekyear_month_week_2011() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendWeekyear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2011-01-01-1");
        // 2011-01-03 is Monday of week 1, 2011
        assertEquals("2011-01-03", result.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_weekyear_month_week_2012() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendWeekyear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2012-01-01-1");
        // 2012-01-02 is Monday of week 1, 2012
        assertEquals("2012-01-02", result.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_weekyear_month_week_2016() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendWeekyear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2016-01-01-1");
        // 2016-01-04 is Monday of week 1, 2016
        assertEquals("2016-01-04", result.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_year_month_week_2010() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2010-01-01-1");
        // For year-month-week, the week is relative to the year, so 2010-01-04
        assertEquals("2010-01-04", result.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_year_month_week_2011() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2011-01-01-1");
        assertEquals("2011-01-03", result.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_year_month_week_2012() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2012-01-01-1");
        assertEquals("2012-01-02", result.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_year_month_week_2016() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendWeekOfWeekyear(2)
                .appendLiteral('-')
                .appendDayOfWeek(1)
                .toFormatter()
                .withZoneUTC();
        LocalDate result = formatter.parseLocalDate("2016-01-01-1");
        assertEquals("2016-01-04", result.toString());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testComputeMillis_illegalZoneTransition() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.setZone(DateTimeZone.forID("America/New_York"));
        bucket.saveField(DateTimeFieldType.year(), 2010);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 14);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 2);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);
        bucket.computeMillis();
    }

    @Test(timeout = 4000)
    public void testSaveField_nullFieldType() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        try {
            bucket.saveField((DateTimeFieldType) null, 1);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSaveField_nullField() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        try {
            bucket.saveField((DateTimeField) null, 1);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRestoreState_wrongEnclosing() throws Exception {
        DateTimeParserBucket bucket1 = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        DateTimeParserBucket bucket2 = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        Object state = bucket1.saveState();
        assertFalse(bucket2.restoreState(state));
    }

    @Test(timeout = 4000)
    public void testRestoreState_nested() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.year(), 2010);
        Object state1 = bucket.saveState();
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        Object state2 = bucket.saveState();
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        assertTrue(bucket.restoreState(state2));
        assertTrue(bucket.restoreState(state1));
        assertEquals(2010, bucket.computeMillis() / 1000 / 60 / 60 / 24 / 365 + 1970);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testSavedField_compareTo() throws Exception {
        DateTimeField yearField = DateTimeFieldType.year().getField(ISOChronology.getInstanceUTC());
        DateTimeField monthField = DateTimeFieldType.monthOfYear().getField(ISOChronology.getInstanceUTC());
        DateTimeField dayField = DateTimeFieldType.dayOfMonth().getField(ISOChronology.getInstanceUTC());

        SavedField year = new SavedField(yearField, 2010);
        SavedField month = new SavedField(monthField, 1);
        SavedField day = new SavedField(dayField, 4);

        assertTrue(year.compareTo(month) > 0); // year > month
        assertTrue(month.compareTo(day) > 0); // month > day
        assertEquals(0, year.compareTo(new SavedField(yearField, 2011)));
    }

    @Test(timeout = 4000)
    public void testSavedField_compareReverse() throws Exception {
        DateTimeField yearField = DateTimeFieldType.year().getField(ISOChronology.getInstanceUTC());
        DateTimeField monthField = DateTimeFieldType.monthOfYear().getField(ISOChronology.getInstanceUTC());
        DateTimeField dayField = DateTimeFieldType.dayOfMonth().getField(ISOChronology.getInstanceUTC());

        SavedField year = new SavedField(yearField, 2010);
        SavedField month = new SavedField(monthField, 1);
        SavedField day = new SavedField(dayField, 4);

        assertTrue(SavedField.compareReverse(year.iField.getDurationField(), month.iField.getDurationField()) < 0);
        assertTrue(SavedField.compareReverse(month.iField.getDurationField(), day.iField.getDurationField()) < 0);
        assertEquals(0, SavedField.compareReverse(year.iField.getDurationField(), year.iField.getDurationField()));
    }

    @Test(timeout = 4000)
    public void testSort_smallArray() throws Exception {
        DateTimeField yearField = DateTimeFieldType.year().getField(ISOChronology.getInstanceUTC());
        DateTimeField monthField = DateTimeFieldType.monthOfYear().getField(ISOChronology.getInstanceUTC());
        DateTimeField dayField = DateTimeFieldType.dayOfMonth().getField(ISOChronology.getInstanceUTC());

        SavedField[] fields = new SavedField[] {
                new SavedField(dayField, 4),
                new SavedField(yearField, 2010),
                new SavedField(monthField, 1)
        };
        // Use reflection to call private sort method
        java.lang.reflect.Method sortMethod = DateTimeParserBucket.class.getDeclaredMethod("sort", SavedField[].class, int.class);
        sortMethod.setAccessible(true);
        sortMethod.invoke(null, fields, 3);
        assertEquals(yearField, fields[0].iField);
        assertEquals(monthField, fields[1].iField);
        assertEquals(dayField, fields[2].iField);
    }

    @Test(timeout = 4000)
    public void testSort_largeArray() throws Exception {
        DateTimeField yearField = DateTimeFieldType.year().getField(ISOChronology.getInstanceUTC());
        DateTimeField monthField = DateTimeFieldType.monthOfYear().getField(ISOChronology.getInstanceUTC());
        DateTimeField dayField = DateTimeFieldType.dayOfMonth().getField(ISOChronology.getInstanceUTC());

        SavedField[] fields = new SavedField[11];
        for (int i = 0; i < 11; i++) {
            fields[i] = new SavedField(dayField, 4);
        }
        fields[5] = new SavedField(yearField, 2010);
        fields[10] = new SavedField(monthField, 1);
        java.lang.reflect.Method sortMethod = DateTimeParserBucket.class.getDeclaredMethod("sort", SavedField[].class, int.class);
        sortMethod.setAccessible(true);
        sortMethod.invoke(null, fields, 11);
        assertEquals(yearField, fields[0].iField);
        assertEquals(monthField, fields[1].iField);
        assertEquals(dayField, fields[2].iField);
    }

    @Test(timeout = 4000)
    public void testConstructor_deprecated() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(1000L, ISOChronology.getInstanceUTC(), Locale.US);
        assertEquals(1000L, bucket.computeMillis());
    }

    @Test(timeout = 4000)
    public void testConstructor_pivotYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US, 2050);
        assertEquals(Integer.valueOf(2050), bucket.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testConstructor_defaultYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US, null, 1999);
        // Test that default year is used when only month/day saved
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        long millis = bucket.computeMillis();
        assertEquals("1999-01-04T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_weekBasedFirstField() {
        // Directly test the bucket logic: first field is week-based (weekyear)
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.weekyear(), 2010);
        bucket.saveField(DateTimeFieldType.weekOfWeekyear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 1);
        long millis = bucket.computeMillis();
        assertEquals("2010-01-04T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_weekBasedFirstField_2011() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.weekyear(), 2011);
        bucket.saveField(DateTimeFieldType.weekOfWeekyear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 1);
        long millis = bucket.computeMillis();
        assertEquals("2011-01-03T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_weekBasedFirstField_2012() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.weekyear(), 2012);
        bucket.saveField(DateTimeFieldType.weekOfWeekyear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 1);
        long millis = bucket.computeMillis();
        assertEquals("2012-01-02T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }

    @Test(timeout = 4000)
    public void testComputeMillis_weekBasedFirstField_2016() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.US);
        bucket.saveField(DateTimeFieldType.weekyear(), 2016);
        bucket.saveField(DateTimeFieldType.weekOfWeekyear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 1);
        long millis = bucket.computeMillis();
        assertEquals("2016-01-04T00:00:00.000Z", new DateTime(millis, DateTimeZone.UTC).toString());
    }
}