package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

/**
 * Partition analysis for Partial class testing:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Default empty constructor
 *   - Constructor with Chronology (null, ISO)
 *   - Constructor with single field/value
 *   - Constructor with field/value/chronology
 *   - Constructor with types array and values array
 *   - Constructor with ReadablePartial
 *   - size(), getChronology(), getField(), getFieldType(), getFieldTypes()
 *   - getValue(), getValues()
 *   - with() method for adding/changing fields
 *   - without() method for removing fields
 *   - withField(), withFieldAdded(), withFieldAddWrapped()
 *   - withPeriodAdded(), plus(), minus()
 *   - property() method
 *   - isMatch(ReadableInstant) and isMatch(ReadablePartial)
 *   - getFormatter(), toString(), toStringList(), toString(String), toString(String, Locale)
 *   - withChronologyRetainFields()
 *   - Property methods: addToCopy(), addWrapFieldToCopy(), setCopy(), withMaximumValue(), withMinimumValue()
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty Partial (zero fields)
 *   - Single field Partial
 *   - Multiple fields in order largest to smallest
 *   - Values at minimum, maximum, and boundary ranges
 *   - Null arguments for chronology, types, values
 *   - Empty arrays
 * 
 * Partition C: Defect-Targeted Branch Zone (Defects4J defect)
 *   - Bug: TestPartial_Basics::testWith_baseAndArgHaveNoRange
 *     Expected: Partial with year and era (fields without range duration types) should 
 *              work correctly when using with() method
 *     Actual: IllegalArgumentException: "Types array must not contain duplicate: era and year"
 *   - This is triggered when adding a field that has no range duration type alongside
 *     an existing field that also has no range duration type
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null field type in constructor
 *   - Null types array
 *   - Null values array
 *   - Mismatched types/values lengths
 *   - Null field type in types array
 *   - Out-of-order fields
 *   - Duplicate field types
 *   - Null field type in with() method
 *   - Null ReadablePartial in isMatch()
 *   - Invalid field values
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple operations on same instance (immutability verification)
 *   - Chained method calls (with().with().without())
 *   - Constructor validation with validate()
 */

public class PartialDeepseekTest {
    
    /* ================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ================================================================ */
    
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(DateTimeZone.UTC, p.getChronology().getZone());
        assertNotNull(p.getChronology());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithNullChronology() {
        Partial p = new Partial((Chronology) null);
        assertEquals(0, p.size());
        assertEquals(DateTimeZone.UTC, p.getChronology().getZone());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithISOChronology() {
        Chronology iso = ISOChronology.getInstanceUTC();
        Partial p = new Partial(iso);
        assertEquals(0, p.size());
        assertEquals(iso, p.getChronology());
    }
    
    @Test(timeout = 4000)
    public void testConstructorSingleFieldValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(1, p.size());
        assertEquals(2005, p.getValue(0));
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
    }
    
    @Test(timeout = 4000)
    public void testConstructorSingleFieldValueWithChronology() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6, null);
        assertEquals(1, p.size());
        assertEquals(6, p.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testConstructorTypesAndValuesArrays() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 2005, 6, 15 };
        Partial p = new Partial(types, values);
        assertEquals(3, p.size());
        assertEquals(2005, p.getValue(0));
        assertEquals(6, p.getValue(1));
        assertEquals(15, p.getValue(2));
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(2));
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithReadablePartial() {
        Partial base = new Partial(DateTimeFieldType.year(), 2005);
        Partial copy = new Partial((ReadablePartial) base);
        assertEquals(1, copy.size());
        assertEquals(2005, copy.getValue(0));
        assertEquals(DateTimeFieldType.year(), copy.getFieldType(0));
    }
    
    @Test(timeout = 4000)
    public void testSize() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        p = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(1, p.size());
        p = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()
        }, new int[] { 2005, 6 });
        assertEquals(2, p.size());
    }
    
    @Test(timeout = 4000)
    public void testGetChronology() {
        Chronology iso = ISOChronology.getInstanceUTC();
        Partial p = new Partial(iso);
        assertEquals(iso, p.getChronology());
    }
    
    @Test(timeout = 4000)
    public void testGetField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        DateTimeField field = p.getField(0, p.getChronology());
        assertNotNull(field);
        assertEquals("year", field.toString());
    }
    
    @Test(timeout = 4000)
    public void testGetFieldType() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
    }
    
    @Test(timeout = 4000)
    public void testGetFieldTypes() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()
        };
        Partial p = new Partial(types, new int[] { 2005, 6 });
        DateTimeFieldType[] retrieved = p.getFieldTypes();
        assertEquals(2, retrieved.length);
        assertEquals(DateTimeFieldType.year(), retrieved[0]);
        assertEquals(DateTimeFieldType.monthOfYear(), retrieved[1]);
    }
    
    @Test(timeout = 4000)
    public void testGetValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(2005, p.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testGetValues() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()
        };
        Partial p = new Partial(types, new int[] { 2005, 6 });
        int[] values = p.getValues();
        assertEquals(2, values.length);
        assertEquals(2005, values[0]);
        assertEquals(6, values[1]);
    }
    
    @Test(timeout = 4000)
    public void testWithFieldTypeValue_AddNewField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(2, result.size());
        assertEquals(2005, result.getValue(0));
        assertEquals(6, result.getValue(1));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), result.getFieldType(1));
    }
    
    @Test(timeout = 4000)
    public void testWithFieldTypeValue_ChangeExistingField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.with(DateTimeFieldType.year(), 2006);
        assertEquals(1, result.size());
        assertEquals(2006, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testWithFieldTypeValue_SameValueReturnsSame() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.with(DateTimeFieldType.year(), 2005);
        assertSame(p, result);
    }
    
    @Test(timeout = 4000)
    public void testWithoutFieldType_RemoveExisting() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()
        };
        Partial p = new Partial(types, new int[] { 2005, 6 });
        Partial result = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(1, result.size());
        assertEquals(2005, result.getValue(0));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(0));
    }
    
    @Test(timeout = 4000)
    public void testWithoutFieldType_NotPresent() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.without(DateTimeFieldType.monthOfYear());
        assertSame(p, result);
    }
    
    @Test(timeout = 4000)
    public void testWithField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.withField(DateTimeFieldType.year(), 2006);
        assertEquals(2006, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testWithField_SameValueReturnsSame() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertSame(p, p.withField(DateTimeFieldType.year(), 2005));
    }
    
    @Test(timeout = 4000)
    public void testWithFieldAdded() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.withFieldAdded(DurationFieldType.years(), 2);
        assertEquals(2007, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testWithFieldAdded_ZeroAmount() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertSame(p, p.withFieldAdded(DurationFieldType.years(), 0));
    }
    
    @Test(timeout = 4000)
    public void testWithFieldAddWrapped() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 12);
        Partial result = p.withFieldAddWrapped(DurationFieldType.months(), 2);
        assertEquals(2, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testWithFieldAddWrapped_ZeroAmount() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        assertSame(p, p.withFieldAddWrapped(DurationFieldType.months(), 0));
    }
    
    @Test(timeout = 4000)
    public void testPlus() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.plus(Period.years(2));
        assertEquals(2007, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPlus_NullPeriod() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertSame(p, p.plus(null));
    }
    
    @Test(timeout = 4000)
    public void testMinus() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.minus(Period.years(1));
        assertEquals(2004, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testMinus_NullPeriod() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertSame(p, p.minus(null));
    }
    
    @Test(timeout = 4000)
    public void testProperty() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        assertNotNull(prop);
        assertEquals(2005, prop.get());
    }
    
    @Test(timeout = 4000)
    public void testIsMatch_ReadableInstant() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        DateTime dt = new DateTime(2005, 6, 15, 12, 30, 0, 0);
        assertTrue(p.isMatch((ReadableInstant) dt));
    }
    
    @Test(timeout = 4000)
    public void testIsMatch_ReadableInstantNoMatch() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        DateTime dt = new DateTime(2006, 6, 15, 12, 30, 0, 0);
        assertFalse(p.isMatch((ReadableInstant) dt));
    }
    
    @Test(timeout = 4000)
    public void testIsMatch_ReadableInstantNull() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        // null means "now", should return some value (not throw)
        boolean result = p.isMatch((ReadableInstant) null);
        // Just verify it doesn't throw and returns boolean
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testIsMatch_ReadablePartial() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2005);
        assertTrue(p1.isMatch((ReadablePartial) p2));
    }
    
    @Test(timeout = 4000)
    public void testIsMatch_ReadablePartialNoMatch() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2006);
        assertFalse(p1.isMatch((ReadablePartial) p2));
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        // Since there is no formatter for year alone, returns toStringList
        String result = p.toString();
        assertTrue(result.contains("year=2005"));
    }
    
    @Test(timeout = 4000)
    public void testToStringList() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        String result = p.toStringList();
        assertEquals("[year=2005]", result);
    }
    
    @Test(timeout = 4000)
    public void testToStringWithPattern() {
        Partial p = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        }, new int[] { 2005, 6, 15 });
        String result = p.toString("yyyy-MM-dd");
        assertEquals("2005-06-15", result);
    }
    
    @Test(timeout = 4000)
    public void testToStringWithPatternNull() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(p.toString(), p.toString((String) null));
    }
    
    @Test(timeout = 4000)
    public void testToStringWithPatternAndLocale() {
        Partial p = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        }, new int[] { 2005, 6, 15 });
        String result = p.toString("yyyy-MM-dd", Locale.US);
        assertEquals("2005-06-15", result);
    }
    
    @Test(timeout = 4000)
    public void testWithChronologyRetainFields() {
        Chronology iso = ISOChronology.getInstanceUTC();
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        Partial p = new Partial(iso, new DateTimeFieldType[] { DateTimeFieldType.year() }, new int[] { 2005 });
        Partial result = p.withChronologyRetainFields(buddhist);
        assertEquals(buddhist, result.getChronology());
        assertEquals(2005, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testWithChronologyRetainFields_SameChronology() {
        Chronology iso = ISOChronology.getInstanceUTC();
        Partial p = new Partial(iso, new DateTimeFieldType[] { DateTimeFieldType.year() }, new int[] { 2005 });
        assertSame(p, p.withChronologyRetainFields(iso));
    }
    
    @Test(timeout = 4000)
    public void testPropertyAddToCopy() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        Partial result = prop.addToCopy(2);
        assertEquals(2007, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPropertyAddWrapFieldToCopy() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 12);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial result = prop.addWrapFieldToCopy(2);
        assertEquals(2, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPropertySetCopy() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        Partial result = prop.setCopy(2006);
        assertEquals(2006, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPropertySetCopyWithText() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial result = prop.setCopy("12");
        assertEquals(12, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPropertySetCopyWithTextAndLocale() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial result = prop.setCopy("12", Locale.US);
        assertEquals(12, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPropertyWithMaximumValue() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 1);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial result = prop.withMaximumValue();
        assertEquals(12, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPropertyWithMinimumValue() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial result = prop.withMinimumValue();
        assertEquals(1, result.getValue(0));
    }
    
    /* ================================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ================================================================ */
    
    @Test(timeout = 4000)
    public void testEmptyPartial() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals("[]", p.toString());
        assertTrue(p.getFormatter() == null);
    }
    
    @Test(timeout = 4000)
    public void testSingleFieldPartial() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(1, p.size());
        assertEquals(2005, p.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testMultipleFieldsOrderValidation() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour(),
            DateTimeFieldType.secondOfMinute()
        };
        int[] values = new int[] { 2005, 6, 15, 10, 30, 45 };
        Partial p = new Partial(types, values);
        assertEquals(6, p.size());
        assertEquals(2005, p.getValue(0));
        assertEquals(45, p.getValue(5));
    }
    
    @Test(timeout = 4000)
    public void testBoundaryValues() {
        // Minimum and maximum valid values for month
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 1);
        assertEquals(1, p.getValue(0));
        p = new Partial(DateTimeFieldType.monthOfYear(), 12);
        assertEquals(12, p.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testZeroLengthArrays() {
        DateTimeFieldType[] types = new DateTimeFieldType[0];
        int[] values = new int[0];
        Partial p = new Partial(types, values);
        assertEquals(0, p.size());
    }
    
    /* ================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J defect)
     * ================================================================ */
    
    /**
     * This test targets the defect from Defects4J:
     * "testWith_baseAndArgHaveNoRange"
     * 
     * The bug occurs when using with() to add a field where both the base partial's
     * field and the new field have no range duration type (e.g., era and year).
     * The method incorrectly throws IllegalArgumentException claiming duplicate fields.
     * 
     * Expected behavior: The with() method should successfully add the new field
     * when they are different types, even if both have no range duration type.
     */
    @Test(timeout = 4000)
    public void testWith_BaseAndArgHaveNoRange() {
        // Create partial with year (which has no range duration type)
        Partial base = new Partial(DateTimeFieldType.year(), 2005);
        
        // Add era (which also has no range duration type) using with()
        // This should work correctly - year and era are different fields
        Partial result = base.with(DateTimeFieldType.era(), 1);
        
        // Verify the result has both fields
        assertEquals(2, result.size());
        assertEquals(1, result.getValue(0)); // era is larger, should come first
        assertEquals(2005, result.getValue(1)); // year is smaller, comes second
        assertEquals(DateTimeFieldType.era(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(1));
        
        // Verify the Partial is valid
        result.getChronology().validate(result, result.getValues());
    }
    
    /**
     * Additional test for the same defect scenario with different field order.
     * Start with era and add year.
     */
    @Test(timeout = 4000)
    public void testWith_BaseAndArgHaveNoRange_ReverseOrder() {
        Partial base = new Partial(DateTimeFieldType.era(), 1);
        Partial result = base.with(DateTimeFieldType.year(), 2005);
        
        assertEquals(2, result.size());
        assertEquals(1, result.getValue(0));
        assertEquals(2005, result.getValue(1));
        assertEquals(DateTimeFieldType.era(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(1));
    }
    
    /**
     * Test with centuryOfEra (also has no range) alongside year.
     */
    @Test(timeout = 4000)
    public void testWith_CenturyAndYearHaveNoRange() {
        Partial base = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = base.with(DateTimeFieldType.centuryOfEra(), 20);
        
        assertEquals(2, result.size());
        assertEquals(20, result.getValue(0));
        assertEquals(2005, result.getValue(1));
    }
    
    /* ================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ================================================================ */
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFieldType() {
        new Partial((DateTimeFieldType) null, 5);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullTypesArray() {
        new Partial((DateTimeFieldType[]) null, new int[0]);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullValuesArray() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year() }, null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorMismatchedArrays() {
        new Partial(
            new DateTimeFieldType[] { DateTimeFieldType.year(), DateTimeFieldType.monthOfYear() },
            new int[] { 2005 }
        );
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFieldInTypesArray() {
        new Partial(
            new DateTimeFieldType[] { DateTimeFieldType.year(), null },
            new int[] { 2005, 6 }
        );
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorOutOfOrderFields() {
        new Partial(
            new DateTimeFieldType[] { DateTimeFieldType.monthOfYear(), DateTimeFieldType.year() },
            new int[] { 6, 2005 }
        );
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorDuplicateFields() {
        new Partial(
            new DateTimeFieldType[] { DateTimeFieldType.year(), DateTimeFieldType.year() },
            new int[] { 2005, 2006 }
        );
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithNullReadablePartial() {
        new Partial((ReadablePartial) null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithNullFieldType() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        p.with(null, 5);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsMatchNullReadablePartial() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        p.isMatch((ReadablePartial) null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidFieldValue() {
        new Partial(DateTimeFieldType.monthOfYear(), 13);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidFieldValueZero() {
        new Partial(DateTimeFieldType.monthOfYear(), 0);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldTypeInvalidIndex() {
        Partial p = new Partial();
        p.getFieldType(0);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueInvalidIndex() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        p.getValue(1);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldUnsupportedField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        p.withField(DateTimeFieldType.monthOfYear(), 6);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAddedUnsupportedField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        p.withFieldAdded(DurationFieldType.months(), 1);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyUnsupportedField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        p.property(DateTimeFieldType.monthOfYear());
    }
    
    /* ================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ================================================================ */
    
    @Test(timeout = 4000)
    public void testImmutability() {
        Partial original = new Partial(DateTimeFieldType.year(), 2005);
        
        // with() should return new instance (if value changes)
        Partial modified = original.with(DateTimeFieldType.year(), 2006);
        assertNotSame(original, modified);
        assertEquals(2005, original.getValue(0));
        assertEquals(2006, modified.getValue(0));
        
        // Original should be unchanged
        assertEquals(1, original.size());
    }
    
    @Test(timeout = 4000)
    public void testChainedWithCalls() {
        Partial p = new Partial();
        Partial result = p
            .with(DateTimeFieldType.year(), 2005)
            .with(DateTimeFieldType.monthOfYear(), 6)
            .with(DateTimeFieldType.dayOfMonth(), 15);
        
        assertEquals(3, result.size());
        assertEquals(2005, result.getValue(0));
        assertEquals(6, result.getValue(1));
        assertEquals(15, result.getValue(2));
    }
    
    @Test(timeout = 4000)
    public void testChainedWithAndWithout() {
        Partial p = new Partial();
        Partial result = p
            .with(DateTimeFieldType.year(), 2005)
            .with(DateTimeFieldType.monthOfYear(), 6)
            .with(DateTimeFieldType.dayOfMonth(), 15)
            .without(DateTimeFieldType.dayOfMonth());
        
        assertEquals(2, result.size());
        assertEquals(2005, result.getValue(0));
        assertEquals(6, result.getValue(1));
    }
    
    @Test(timeout = 4000)
    public void testPartialWithPeriodAdded() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.withPeriodAdded(Period.years(2), 1);
        assertEquals(2007, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testPartialWithPeriodAdded_NullPeriod() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertSame(p, p.withPeriodAdded(null, 1));
    }
    
    @Test(timeout = 4000)
    public void testPartialWithPeriodAdded_ZeroScalar() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertSame(p, p.withPeriodAdded(Period.years(2), 0));
    }
    
    @Test(timeout = 4000)
    public void testPartialWithPeriodAdded_NegativeScalar() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = p.withPeriodAdded(Period.years(2), -1);
        assertEquals(2003, result.getValue(0));
    }
    
    @Test(timeout = 4000)
    public void testGetFormatter() {
        Partial p = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        }, new int[] { 2005, 6, 15 });
        DateTimeFormatter formatter = p.getFormatter();
        assertNotNull(formatter);
    }
    
    @Test(timeout = 4000)
    public void testGetFormatter_EmptyPartial() {
        Partial p = new Partial();
        assertNull(p.getFormatter());
    }
    
    @Test(timeout = 4000)
    public void testPartialWithYearMonthDayToString() {
        Partial p = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        }, new int[] { 2005, 6, 15 });
        String result = p.toString();
        // Should be ISO format: 2005-06-15
        assertEquals("2005-06-15", result);
    }
}