package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SQA Engineer: Advanced White-Box Testing for org.joda.time.Partial
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Partial.with(DateTimeFieldType, int) method
 * 
 * Defect from Defects4J (TestPartial_Basics::testWith3):
 * The bug reveals itself when calling `with()` to add a new field that is 
 * chronologically smaller than existing fields. The insertion logic in `with()`
 * incorrectly positions the new field relative to fields with unsupported 
 * duration types (e.g., dayOfWeek vs dayOfMonth). When comparing range duration 
 * fields, a NullPointerException or incorrect ordering can occur if 
 * `fieldType.getRangeDurationType()` returns null while another field's 
 * range duration type is non-null, breaking the assumption in the comparison 
 * logic at line 333-336 (raw source).
 * 
 * Key decision branches in `with()`:
 * - indexOf(fieldType) == -1 (new field insertion)
 * - DurationField.isSupported() checks for both new and existing fields
 * - compareTo() for unitDuration and rangeDuration ordering
 * - Null checks for rangeDurationType on either field
 * - Correct array copy for insertion at position i
 * - Value validation via iChronology.validate() and getField().set()
 * 
 * Partitions:
 * A: Core functional: empty partial, single-field, multiple-field with/without()
 * B: Boundaries: null fieldType, extreme values, add/remove fields
 * C: Defect-targeted: Insertion of field with null rangeDurationType 
 *    among fields with non-null rangeDurationType (e.g., adding dayOfWeek 
 *    to partial containing dayOfMonth)
 * D: Exception handling: null args, invalid values, unsupported fields
 * E: Contract: toString, getFormatter, isMatch, equality/hash behavior
 */
public class PartialDeepseekTest {

    // ====================================================
    // Partition A: Core Functional Logic & State Transitions
    // ====================================================

    @Test(timeout = 4000)
    public void testEmptyPartial() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(0, p.getValues().length);
        assertEquals(0, p.getFieldTypes().length);
        assertNotNull(p.getChronology());
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
    }

    @Test(timeout = 4000)
    public void testSingleFieldConstructor() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        assertEquals(1, p.size());
        assertEquals(2000, p.getValue(0));
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
    }

    @Test(timeout = 4000)
    public void testWithAddYearToEmpty() {
        Partial p = new Partial();
        Partial result = p.with(DateTimeFieldType.year(), 2020);
        assertEquals(1, result.size());
        assertEquals(2020, result.getValue(0));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(0));
    }

    @Test(timeout = 4000)
    public void testWithReplaceExistingField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial result = p.with(DateTimeFieldType.year(), 2021);
        assertEquals(1, result.size());
        assertEquals(2021, result.getValue(0));
        // Should return same instance if value unchanged
        Partial same = p.with(DateTimeFieldType.year(), 2020);
        assertSame(p, same);
    }

    @Test(timeout = 4000)
    public void testWithMultipleFieldsInOrder() {
        Partial p = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        assertEquals(3, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(2));
        assertEquals(2020, p.getValue(0));
        assertEquals(6, p.getValue(1));
        assertEquals(15, p.getValue(2));
    }

    @Test(timeout = 4000)
    public void testWithoutField() {
        Partial p = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        Partial result = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(1, result.size());
        assertEquals(DateTimeFieldType.year(), result.getFieldType(0));
        assertEquals(2020, result.getValue(0));
    }

    @Test(timeout = 4000)
    public void testWithoutNonExistentFieldReturnsSelf() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial result = p.without(DateTimeFieldType.monthOfYear());
        assertSame(p, result);
    }

    @Test(timeout = 4000)
    public void testWithChronologyRetainFields() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        Partial result = p.withChronologyRetainFields(buddhist);
        assertNotSame(p, result);
        assertEquals(buddhist.withUTC(), result.getChronology());
        assertEquals(2020, result.getValue(0));
    }

    // ====================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ====================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullFieldTypeWith() {
        Partial p = new Partial();
        p.with(null, 1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullFieldTypeConstructor() {
        new Partial(null, 1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullTypesArray() {
        new Partial((DateTimeFieldType[]) null, new int[]{1});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullValuesArray() {
        new Partial(new DateTimeFieldType[]{DateTimeFieldType.year()}, null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMismatchedArrays() {
        new Partial(
            new DateTimeFieldType[]{DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()},
            new int[]{2020}
        );
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTypesArrayWithNull() {
        new Partial(
            new DateTimeFieldType[]{DateTimeFieldType.year(), null},
            new int[]{2020, 6}
        );
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithInvalidValue() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        p.with(DateTimeFieldType.monthOfYear(), 13);
    }

    @Test(timeout = 4000)
    public void testBoundaryValues() {
        // Minimum and maximum valid values for various fields
        Partial p = new Partial()
                .with(DateTimeFieldType.year(), 1)
                .with(DateTimeFieldType.monthOfYear(), 1)
                .with(DateTimeFieldType.dayOfMonth(), 1)
                .with(DateTimeFieldType.hourOfDay(), 0)
                .with(DateTimeFieldType.minuteOfHour(), 0)
                .with(DateTimeFieldType.secondOfMinute(), 0);
        assertEquals(1, p.getValue(0));
        assertEquals(1, p.getValue(1));
        assertEquals(1, p.getValue(2));
    }

    // ====================================================
    // Partition C: Defect-Targeted Branch Zone
    // (Targeting Defects4J testWith3 failure)
    // ====================================================

    @Test(timeout = 4000)
    public void testWith_DayOfWeekToMonthDayPartial() {
        // This targets the known defect:
        // When adding dayOfWeek (which has null rangeDurationType) to a Partial
        // that already contains fields like monthOfYear and dayOfMonth 
        // (which have non-null rangeDurationType), the insertion ordering logic
        // in `with()` can fail with NullPointerException or incorrect ordering.
        
        Partial base = new Partial()
                .with(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        
        // dayOfWeek has rangeDurationType = null (week is not a proper duration range)
        // monthOfYear has rangeDurationType = year()
        // dayOfMonth has rangeDurationType = month()
        // This should be inserted between monthOfYear and dayOfMonth
        Partial result = base.with(DateTimeFieldType.dayOfWeek(), 3);
        
        assertEquals(3, result.size());
        assertEquals(DateTimeFieldType.monthOfYear(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.dayOfWeek(), result.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfMonth(), result.getFieldType(2));
        assertEquals(6, result.getValue(0));
        assertEquals(3, result.getValue(1));
        assertEquals(15, result.getValue(2));
    }

    @Test(timeout = 4000)
    public void testWith_DayOfWeekToYearMonthDayPartial() {
        // Additional edge case: year, month, day with dayOfWeek insertion
        Partial base = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        
        Partial result = base.with(DateTimeFieldType.dayOfWeek(), 1);
        
        assertEquals(4, result.size());
        assertEquals(DateTimeFieldType.year(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), result.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfWeek(), result.getFieldType(2));
        assertEquals(DateTimeFieldType.dayOfMonth(), result.getFieldType(3));
    }

    @Test(timeout = 4000)
    public void testWith_WeekyearToYearPartial() {
        // weekyear has different duration type than year but both are supported
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial result = p.with(DateTimeFieldType.weekyear(), 2020);
        // weekyear is larger than year in duration, so should be inserted before
        assertEquals(2, result.size());
        assertEquals(DateTimeFieldType.weekyear(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(1));
    }

    // ====================================================
    // Partition D: Exception & Defensive Guard Paths
    // ====================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithDuplicateTypes() {
        new Partial(
            new DateTimeFieldType[]{DateTimeFieldType.monthOfYear(), DateTimeFieldType.monthOfYear()},
            new int[]{6, 6}
        );
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithOutOfOrderTypes() {
        new Partial(
            new DateTimeFieldType[]{DateTimeFieldType.dayOfMonth(), DateTimeFieldType.monthOfYear()},
            new int[]{15, 6}
        );
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithNullPartial() {
        new Partial((ReadablePartial) null);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetFieldTypeInvalidIndex() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.getFieldType(5);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetValueInvalidIndex() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.getValue(-1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithFieldUnsupportedType() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.withField(DateTimeFieldType.monthOfYear(), 6);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithFieldAddedUnsupportedType() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.withFieldAdded(DateTimeFieldType.monthOfYear(), 1);
    }

    // ====================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ====================================================

    @Test(timeout = 4000)
    public void testIsMatchWithReadableInstant() {
        Partial p = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        
        DateTime dt = new DateTime(2020, 6, 15, 0, 0, 0, 0);
        assertTrue(p.isMatch((ReadableInstant) dt));
        
        DateTime dt2 = new DateTime(2020, 6, 16, 0, 0, 0, 0);
        assertFalse(p.isMatch((ReadableInstant) dt2));
    }

    @Test(timeout = 4000)
    public void testIsMatchWithReadablePartial() {
        Partial p1 = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        
        Partial p2 = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        
        assertTrue(p1.isMatch((ReadablePartial) p2));
        
        Partial p3 = new Partial()
                .with(DateTimeFieldType.year(), 2021)
                .with(DateTimeFieldType.monthOfYear(), 6);
        assertFalse(p1.isMatch((ReadablePartial) p3));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsMatchNullPartial() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.isMatch((ReadablePartial) null);
    }

    @Test(timeout = 4000)
    public void testToStringForEmpty() {
        Partial p = new Partial();
        String str = p.toString();
        // Empty partial should return something, likely "[]"
        assertNotNull(str);
        assertTrue(str.startsWith("[") && str.endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testToStringList() {
        Partial p = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        String str = p.toStringList();
        assertTrue(str.contains("year=2020"));
        assertTrue(str.contains("monthOfYear=6"));
    }

    @Test(timeout = 4000)
    public void testGetFormatter() {
        Partial p = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        DateTimeFormatter fmt = p.getFormatter();
        assertNotNull(fmt);
        String result = fmt.print(p);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testPropertyOperations() {
        Partial p = new Partial()
                .with(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        
        Partial.Property prop = p.property(DateTimeFieldType.dayOfMonth());
        assertEquals(15, prop.get());
        
        Partial added = prop.addToCopy(1);
        assertEquals(16, added.getValue(1));
        
        Partial setCopy = prop.setCopy(20);
        assertEquals(20, setCopy.getValue(1));
        
        Partial max = prop.withMaximumValue();
        assertTrue(max.getValue(1) > 15);
        
        Partial min = prop.withMinimumValue();
        assertEquals(1, min.getValue(1));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPropertyWithUnsupportedType() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.property(DateTimeFieldType.monthOfYear()); // monthOfYear not present
    }

    @Test(timeout = 4000)
    public void testWithFieldAddWrapped() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 12);
        Partial result = p.withFieldAddWrapped(DurationFieldType.months(), 1);
        // December + 1 month wrapped = January (month 1)
        assertEquals(1, result.getValue(0));
    }

    @Test(timeout = 4000)
    public void testPlusMinusPeriod() {
        Partial p = new Partial()
                .with(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        
        Partial plus = p.plus(Period.months(1));
        assertEquals(7, plus.getValue(1));
        
        Partial minus = p.minus(Period.months(1));
        assertEquals(5, minus.getValue(1));
        
        // Null period returns self
        assertSame(p, p.plus((ReadablePeriod) null));
        assertSame(p, p.minus((ReadablePeriod) null));
    }

    @Test(timeout = 4000)
    public void testWithPeriodAddedZeroScalar() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial result = p.withPeriodAdded(Period.months(1), 0);
        assertSame(p, result);
    }
}