package org.joda.time;

import org.joda.time.*;
import org.joda.time.chrono.*;
import org.joda.time.format.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

public class PartialClaudeTest {

    /**
     * @target Partial() default constructor
     * @scenario Construct with no arguments
     * @defectRisk Ensure default chronology is ISO UTC and size is zero
     */
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertNotNull(p.getChronology());
        assertEquals(DateTimeZone.UTC, p.getChronology().getZone());
    }

    /**
     * @target Partial(Chronology) constructor
     * @scenario Construct with null chronology, expect ISO default
     * @defectRisk Ensure null chronology defaults properly
     */
    @Test(timeout = 4000)
    public void testChronologyConstructor_Null() {
        Partial p = new Partial((Chronology) null);
        assertEquals(0, p.size());
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
    }

    /**
     * @target Partial(Chronology) constructor
     * @scenario Construct with a specific non-UTC chronology
     * @defectRisk Ensure chronology is converted to UTC
     */
    @Test(timeout = 4000)
    public void testChronologyConstructor_NonUTC() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.forID("Europe/Paris"));
        Partial p = new Partial(chrono);
        assertEquals(DateTimeZone.UTC, p.getChronology().getZone());
    }

    /**
     * @target Partial(DateTimeFieldType, int) constructor
     * @scenario Construct single field partial with valid type/value
     * @defectRisk Ensure single-field constructor sets values correctly
     */
    @Test(timeout = 4000)
    public void testSingleFieldConstructor() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(1, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(2005, p.getValue(0));
    }

    /**
     * @target Partial(DateTimeFieldType, int) constructor
     * @scenario Pass null type, expect exception
     * @defectRisk Ensure null type check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testSingleFieldConstructor_NullType() {
        try {
            new Partial((DateTimeFieldType) null, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("field type"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType, int, Chronology) constructor
     * @scenario Construct with a specific chronology
     * @defectRisk Ensure chronology parameter properly applied and validated
     */
    @Test(timeout = 4000)
    public void testSingleFieldConstructor_WithChronology() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6, ISOChronology.getInstance());
        assertEquals(1, p.size());
        assertEquals(6, p.getValue(0));
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Construct with multiple valid fields in correct order
     * @defectRisk Ensure multi-field constructor works correctly
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_Valid() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2005, 6, 9};
        Partial p = new Partial(types, values);
        assertEquals(3, p.size());
        assertEquals(2005, p.getValue(0));
        assertEquals(6, p.getValue(1));
        assertEquals(9, p.getValue(2));
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Pass null types array
     * @defectRisk Ensure null types check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_NullTypes() {
        int[] values = new int[] {2005};
        try {
            new Partial((DateTimeFieldType[]) null, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Types array"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Pass null values array
     * @defectRisk Ensure null values check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_NullValues() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year()};
        try {
            new Partial(types, (int[]) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Values array"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Mismatched array lengths
     * @defectRisk Ensure length mismatch check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_MismatchedLengths() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};
        int[] values = new int[] {2005};
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("same length"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Empty types and values arrays
     * @defectRisk Ensure zero-length arrays are accepted without error
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_EmptyArrays() {
        DateTimeFieldType[] types = new DateTimeFieldType[0];
        int[] values = new int[0];
        Partial p = new Partial(types, values);
        assertEquals(0, p.size());
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Types array contains a null element
     * @defectRisk Ensure null element check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_NullElementInTypes() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), null};
        int[] values = new int[] {2005, 1};
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("null"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Duplicate field types (same unit and range null)
     * @defectRisk Ensure duplicate detection throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_DuplicateTypes() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.year()};
        int[] values = new int[] {2005, 2006};
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("duplicate"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor
     * @scenario Fields out of order (smaller before larger)
     * @defectRisk Ensure out-of-order detection throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_OutOfOrder() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.monthOfYear(), DateTimeFieldType.year()};
        int[] values = new int[] {6, 2005};
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("order"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) constructor - CRITICAL DEFECT TEST
     * @scenario era() placed after year() and dayOfMonth() - invalid largest-to-smallest order
     * @defectRisk Known Defects4J bug: constructor fails to reject out-of-order era field
     */
    @Test(timeout = 4000)
    public void testConstructor_RejectOutOfOrderWithEra_Time1() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.era()
        };
        int[] values = new int[] { 2000, 15, 1 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException: types array must be in order largest-smallest");
        } catch (IllegalArgumentException expected) {
            assertTrue("Expected order error message", expected.getMessage().contains("order"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[], Chronology) constructor
     * @scenario Construct with explicit chronology and valid fields
     * @defectRisk Ensure chronology-aware constructor works correctly
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_WithChronology() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};
        int[] values = new int[] {2005, 6};
        Partial p = new Partial(types, values, ISOChronology.getInstance());
        assertEquals(2, p.size());
    }

    /**
     * @target Partial(ReadablePartial) copy constructor
     * @scenario Copy from an existing Partial
     * @defectRisk Ensure copy constructor duplicates types/values correctly
     */
    @Test(timeout = 4000)
    public void testCopyConstructor() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};
        int[] values = new int[] {2005, 6};
        Partial original = new Partial(types, values);
        Partial copy = new Partial(original);
        assertEquals(original.size(), copy.size());
        assertEquals(original.getValue(0), copy.getValue(0));
        assertEquals(original.getValue(1), copy.getValue(1));
    }

    /**
     * @target Partial(ReadablePartial) copy constructor
     * @scenario Pass null partial
     * @defectRisk Ensure null check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testCopyConstructor_Null() {
        try {
            new Partial((ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("partial"));
        }
    }

    /**
     * @target size(), getChronology(), getFieldType(int), getFieldTypes(), getValue(int), getValues()
     * @scenario Verify basic accessor methods work correctly
     * @defectRisk Ensure accessors return correct cloned arrays
     */
    @Test(timeout = 4000)
    public void testAccessors() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};
        int[] values = new int[] {2005, 6};
        Partial p = new Partial(types, values);
        assertEquals(2, p.size());
        assertNotNull(p.getChronology());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        DateTimeFieldType[] typesCopy = p.getFieldTypes();
        assertEquals(2, typesCopy.length);
        int[] valuesCopy = p.getValues();
        assertEquals(2005, valuesCopy[0]);
        assertEquals(6, valuesCopy[1]);
    }

    /**
     * @target withChronologyRetainFields(Chronology)
     * @scenario Change chronology while retaining field values
     * @defectRisk Ensure new chronology is validated and applied correctly
     */
    @Test(timeout = 4000)
    public void testWithChronologyRetainFields() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.withChronologyRetainFields(GregorianChronology.getInstance());
        assertEquals(2005, p2.getValue(0));
    }

    /**
     * @target withChronologyRetainFields(Chronology)
     * @scenario Set the same chronology as current - identity return path
     * @defectRisk Ensure same chronology returns same instance
     */
    @Test(timeout = 4000)
    public void testWithChronologyRetainFields_SameChronology() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005, ISOChronology.getInstanceUTC());
        Partial p2 = p.withChronologyRetainFields(ISOChronology.getInstanceUTC());
        assertSame(p, p2);
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Add a new field not previously present
     * @defectRisk Ensure insertion logic maintains correct ordering
     */
    @Test(timeout = 4000)
    public void testWith_NewField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(2, p2.size());
        assertEquals(DateTimeFieldType.year(), p2.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p2.getFieldType(1));
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Update an existing field to same value - identity return
     * @defectRisk Ensure same value shortcut returns same instance
     */
    @Test(timeout = 4000)
    public void testWith_SameValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.with(DateTimeFieldType.year(), 2005);
        assertSame(p, p2);
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Update an existing field to a different value
     * @defectRisk Ensure update path works correctly
     */
    @Test(timeout = 4000)
    public void testWith_DifferentValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.with(DateTimeFieldType.year(), 2010);
        assertEquals(2010, p2.getValue(0));
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Pass null field type
     * @defectRisk Ensure null check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testWith_NullFieldType() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        try {
            p.with(null, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("field type"));
        }
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Insert field into middle position (rangeDurationType comparison branch)
     * @defectRisk Ensure insertion point logic with same unit but different range works
     */
    @Test(timeout = 4000)
    public void testWith_InsertMiddle() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth()};
        int[] values = new int[] {2005, 15};
        Partial p = new Partial(types, values);
        Partial p2 = p.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(3, p2.size());
        assertEquals(DateTimeFieldType.year(), p2.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p2.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfMonth(), p2.getFieldType(2));
    }

    /**
     * @target without(DateTimeFieldType)
     * @scenario Remove existing field
     * @defectRisk Ensure removal logic works correctly
     */
    @Test(timeout = 4000)
    public void testWithout_ExistingField() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};
        int[] values = new int[] {2005, 6};
        Partial p = new Partial(types, values);
        Partial p2 = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(1, p2.size());
        assertEquals(DateTimeFieldType.year(), p2.getFieldType(0));
    }

    /**
     * @target without(DateTimeFieldType)
     * @scenario Remove non-existing field - identity return
     * @defectRisk Ensure non-existing field removal returns same instance
     */
    @Test(timeout = 4000)
    public void testWithout_NonExistingField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.without(DateTimeFieldType.monthOfYear());
        assertSame(p, p2);
    }

    /**
     * @target without(DateTimeFieldType)
     * @scenario Pass null field type - should not error, returns same instance
     * @defectRisk Ensure null-safe removal
     */
    @Test(timeout = 4000)
    public void testWithout_NullField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.without(null);
        assertSame(p, p2);
    }

    /**
     * @target withField(DateTimeFieldType, int)
     * @scenario Update existing supported field
     * @defectRisk Ensure withField updates value correctly
     */
    @Test(timeout = 4000)
    public void testWithField_Valid() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.withField(DateTimeFieldType.year(), 2010);
        assertEquals(2010, p2.getValue(0));
    }

    /**
     * @target withField(DateTimeFieldType, int)
     * @scenario Update existing supported field to same value - identity return
     * @defectRisk Ensure same value shortcut works
     */
    @Test(timeout = 4000)
    public void testWithField_SameValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.withField(DateTimeFieldType.year(), 2005);
        assertSame(p, p2);
    }

    /**
     * @target withField(DateTimeFieldType, int)
     * @scenario Field type not supported by partial
     * @defectRisk Ensure unsupported field throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testWithField_Unsupported() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        try {
            p.withField(DateTimeFieldType.monthOfYear(), 6);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * @target withFieldAdded(DurationFieldType, int)
     * @scenario Add nonzero amount to supported field
     * @defectRisk Ensure addition updates field value correctly
     */
    @Test(timeout = 4000)
    public void testWithFieldAdded_Valid() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.withFieldAdded(DurationFieldType.years(), 5);
        assertEquals(2010, p2.getValue(0));
    }

    /**
     * @target withFieldAdded(DurationFieldType, int)
     * @scenario Add zero amount - identity return
     * @defectRisk Ensure zero addition returns same instance
     */
    @Test(timeout = 4000)
    public void testWithFieldAdded_Zero() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(p, p2);
    }

    /**
     * @target withFieldAddWrapped(DurationFieldType, int)
     * @scenario Add amount that wraps within field
     * @defectRisk Ensure wrap-around addition works correctly
     */
    @Test(timeout = 4000)
    public void testWithFieldAddWrapped_Valid() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 11);
        Partial p2 = p.withFieldAddWrapped(DurationFieldType.months(), 3);
        assertEquals(2, p2.getValue(0));
    }

    /**
     * @target withFieldAddWrapped(DurationFieldType, int)
     * @scenario Add zero amount - identity return
     * @defectRisk Ensure zero addition returns same instance
     */
    @Test(timeout = 4000)
    public void testWithFieldAddWrapped_Zero() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial p2 = p.withFieldAddWrapped(DurationFieldType.months(), 0);
        assertSame(p, p2);
    }

    /**
     * @target withPeriodAdded(ReadablePeriod, int)
     * @scenario Add null period - identity return
     * @defectRisk Ensure null period short-circuits correctly
     */
    @Test(timeout = 4000)
    public void testWithPeriodAdded_NullPeriod() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.withPeriodAdded(null, 1);
        assertSame(p, p2);
    }

    /**
     * @target withPeriodAdded(ReadablePeriod, int)
     * @scenario Add period with scalar zero - identity return
     * @defectRisk Ensure zero scalar short-circuits correctly
     */
    @Test(timeout = 4000)
    public void testWithPeriodAdded_ZeroScalar() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Period period = Period.years(1);
        Partial p2 = p.withPeriodAdded(period, 0);
        assertSame(p, p2);
    }

    /**
     * @target withPeriodAdded(ReadablePeriod, int)
     * @scenario Add period with matching field type
     * @defectRisk Ensure period addition updates supported field
     */
    @Test(timeout = 4000)
    public void testWithPeriodAdded_MatchingField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Period period = Period.years(3);
        Partial p2 = p.withPeriodAdded(period, 1);
        assertEquals(2008, p2.getValue(0));
    }

    /**
     * @target withPeriodAdded(ReadablePeriod, int)
     * @scenario Period contains field types not present in partial - should be ignored
     * @defectRisk Ensure unsupported period fields are safely skipped
     */
    @Test(timeout = 4000)
    public void testWithPeriodAdded_UnsupportedFieldIgnored() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Period period = Period.months(3);
        Partial p2 = p.withPeriodAdded(period, 1);
        assertEquals(2005, p2.getValue(0));
    }

    /**
     * @target plus(ReadablePeriod)
     * @scenario Add a period to a partial
     * @defectRisk Ensure plus delegates correctly to withPeriodAdded with scalar 1
     */
    @Test(timeout = 4000)
    public void testPlus() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Period period = Period.years(5);
        Partial p2 = p.plus(period);
        assertEquals(2010, p2.getValue(0));
    }

    /**
     * @target minus(ReadablePeriod)
     * @scenario Subtract a period from a partial
     * @defectRisk Ensure minus delegates correctly to withPeriodAdded with scalar -1
     */
    @Test(timeout = 4000)
    public void testMinus() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Period period = Period.years(5);
        Partial p2 = p.minus(period);
        assertEquals(2000, p2.getValue(0));
    }

    /**
     * @target property(DateTimeFieldType)
     * @scenario Get property for a supported field type
     * @defectRisk Ensure property object binds correctly to partial field
     */
    @Test(timeout = 4000)
    public void testProperty_Valid() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        assertEquals(2005, prop.get());
        assertSame(p, prop.getPartial());
    }

    /**
     * @target property(DateTimeFieldType)
     * @scenario Get property for unsupported field type
     * @defectRisk Ensure unsupported field throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testProperty_Unsupported() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        try {
            p.property(DateTimeFieldType.monthOfYear());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * @target isMatch(ReadableInstant)
     * @scenario Partial matches an instant exactly
     * @defectRisk Ensure matching logic correctly compares field values
     */
    @Test(timeout = 4000)
    public void testIsMatch_Instant_True() {
        DateTime dt = new DateTime(2005, 6, 9, 0, 0, 0, 0, DateTimeZone.UTC);
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertTrue(p.isMatch(dt));
    }

    /**
     * @target isMatch(ReadableInstant)
     * @scenario Partial doesn't match instant
     * @defectRisk Ensure mismatch is correctly detected
     */
    @Test(timeout = 4000)
    public void testIsMatch_Instant_False() {
        DateTime dt = new DateTime(2006, 6, 9, 0, 0, 0, 0, DateTimeZone.UTC);
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        assertFalse(p.isMatch(dt));
    }

    /**
     * @target isMatch(ReadableInstant)
     * @scenario Pass null instant - defaults to now
     * @defectRisk Ensure null instant defaults correctly without exception
     */
    @Test(timeout = 4000)
    public void testIsMatch_Instant_Null() {
        Partial p = new Partial();
        assertTrue(p.isMatch((ReadableInstant) null));
    }

    /**
     * @target isMatch(ReadablePartial)
     * @scenario Partial matches another partial with same field values
     * @defectRisk Ensure partial-to-partial matching works correctly
     */
    @Test(timeout = 4000)
    public void testIsMatch_Partial_True() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};
        int[] values = new int[] {2005, 6};
        Partial p2 = new Partial(types, values);
        assertTrue(p1.isMatch(p2));
    }

    /**
     * @target isMatch(ReadablePartial)
     * @scenario Partial doesn't match another partial
     * @defectRisk Ensure mismatch is correctly detected
     */
    @Test(timeout = 4000)
    public void testIsMatch_Partial_False() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2006);
        assertFalse(p1.isMatch(p2));
    }

    /**
     * @target isMatch(ReadablePartial)
     * @scenario Pass null partial - expect exception
     * @defectRisk Ensure null check throws IllegalArgumentException
     */
    @Test(timeout = 4000)
    public void testIsMatch_Partial_Null() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        try {
            p.isMatch((ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("partial"));
        }
    }

    /**
     * @target toString()
     * @scenario Generate ISO string for year/month/day partial
     * @defectRisk Ensure ISO formatting works for standard field combination
     */
    @Test(timeout = 4000)
    public void testToString_ISOFormat() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2005, 6, 9};
        Partial p = new Partial(types, values);
        String str = p.toString();
        assertEquals("2005-06-09", str);
    }

    /**
     * @target toString()
     * @scenario Empty partial with no fields
     * @defectRisk Ensure empty partial toString returns "[]"
     */
    @Test(timeout = 4000)
    public void testToString_Empty() {
        Partial p = new Partial();
        assertEquals("[]", p.toString());
    }

    /**
     * @target toString()
     * @scenario Partial with fields that have no ISO format (e.g. dayOfWeek + hourOfDay)
     * @defectRisk Ensure fallback to toStringList works correctly
     */
    @Test(timeout = 4000)
    public void testToString_NoISOFormat() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfWeek(), DateTimeFieldType.hourOfDay()
        };
        int[] values = new int[] {3, 12};
        Partial p = new Partial(types, values);
        String str = p.toString();
        assertTrue(str.startsWith("["));
        assertTrue(str.contains("dayOfWeek"));
    }

    /**
     * @target toStringList()
     * @scenario List format for multiple fields
     * @defectRisk Ensure toStringList produces expected bracketed list
     */
    @Test(timeout = 4000)
    public void testToStringList() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()};
        int[] values = new int[] {2005, 6};
        Partial p = new Partial(types, values);
        String str = p.toStringList();
        assertEquals("[year=2005, monthOfYear=6]", str);
    }

    /**
     * @target toString(String)
     * @scenario Format with a specific pattern
     * @defectRisk Ensure custom pattern formatting works correctly
     */
    @Test(timeout = 4000)
    public void testToString_Pattern() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2005, 6, 9};
        Partial p = new Partial(types, values);
        String str = p.toString("yyyy/MM/dd");
        assertEquals("2005/06/09", str);
    }

    /**
     * @target toString(String)
     * @scenario Pass null pattern - defaults to toString()
     * @defectRisk Ensure null pattern uses default toString path
     */
    @Test(timeout = 4000)
    public void testToString_NullPattern() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2005, 6, 9};
        Partial p = new Partial(types, values);
        String str = p.toString((String) null);
        assertEquals("2005-06-09", str);
    }

    /**
     * @target toString(String, Locale)
     * @scenario Format with pattern and specific locale
     * @defectRisk Ensure locale-aware formatting works correctly
     */
    @Test(timeout = 4000)
    public void testToString_PatternLocale() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2005, 6, 9};
        Partial p = new Partial(types, values);
        String str = p.toString("yyyy/MM/dd", Locale.US);
        assertEquals("2005/06/09", str);
    }

    /**
     * @target toString(String, Locale)
     * @scenario Pass null pattern with locale - defaults to toString()
     * @defectRisk Ensure null pattern with locale falls back correctly
     */
    @Test(timeout = 4000)
    public void testToString_NullPatternLocale() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2005, 6, 9};
        Partial p = new Partial(types, values);
        String str = p.toString(null, Locale.US);
        assertEquals("2005-06-09", str);
    }

    /**
     * @target equals(Object)
     * @scenario Compare two equal partials
     * @defectRisk Ensure equals returns true for equivalent fields/chronology
     */
    @Test(timeout = 4000)
    public void testEquals_Equal() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2005);
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
    }

    /**
     * @target equals(Object)
     * @scenario Compare with different value
     * @defectRisk Ensure equals returns false for differing values
     */
    @Test(timeout = 4000)
    public void testEquals_DifferentValue() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2006);
        assertFalse(p1.equals(p2));
    }

    /**
     * @target equals(Object)
     * @scenario Compare with a non-Partial object
     * @defectRisk Ensure equals returns false for incompatible types
     */
    @Test(timeout = 4000)
    public void testEquals_DifferentType() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        assertFalse(p1.equals("not a partial"));
    }

    /**
     * @target equals(Object)
     * @scenario Compare partial to itself
     * @defectRisk Ensure reflexive equality holds
     */
    @Test(timeout = 4000)
    public void testEquals_Self() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        assertTrue(p1.equals(p1));
    }

    /**
     * @target hashCode()
     * @scenario Equal partials produce equal hash codes
     * @defectRisk Ensure hashCode consistency with equals contract
     */
    @Test(timeout = 4000)
    public void testHashCode_Consistent() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    /**
     * @target compareTo(ReadablePartial)
     * @scenario Compare partials with different values, returns negative
     * @defectRisk Ensure compareTo returns correct ordering sign
     */
    @Test(timeout = 4000)
    public void testCompareTo_Less() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2006);
        assertTrue(p1.compareTo(p2) < 0);
    }

    /**
     * @target compareTo(ReadablePartial)
     * @scenario Compare partials with different values, returns positive
     * @defectRisk Ensure compareTo returns correct ordering sign
     */
    @Test(timeout = 4000)
    public void testCompareTo_Greater() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2006);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2005);
        assertTrue(p1.compareTo(p2) > 0);
    }

    /**
     * @target compareTo(ReadablePartial)
     * @scenario Compare equal partials, returns zero
     * @defectRisk Ensure compareTo returns zero for equal values
     */
    @Test(timeout = 4000)
    public void testCompareTo_Equal() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2005);
        assertEquals(0, p1.compareTo(p2));
    }

    /**
     * @target getFormatter()
     * @scenario Get formatter for empty partial - should be null
     * @defectRisk Ensure empty partial returns null formatter
     */
    @Test(timeout = 4000)
    public void testGetFormatter_Empty() {
        Partial p = new Partial();
        assertNull(p.getFormatter());
    }

    /**
     * @target getFormatter()
     * @scenario Get formatter for valid year/month/day partial
     * @defectRisk Ensure formatter is properly cached and reused
     */
    @Test(timeout = 4000)
    public void testGetFormatter_Valid() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2005, 6, 9};
        Partial p = new Partial(types, values);
        DateTimeFormatter f = p.getFormatter();
        assertNotNull(f);
        // Call again to hit cache branch
        DateTimeFormatter f2 = p.getFormatter();
        assertSame(f, f2);
    }

    /**
     * @target Property.addToCopy(int)
     * @scenario Add value to a field property, creating new Partial copy
     * @defectRisk Ensure property addToCopy correctly updates field
     */
    @Test(timeout = 4000)
    public void testProperty_AddToCopy() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        Partial p2 = prop.addToCopy(5);
        assertEquals(2010, p2.getValue(0));
        assertEquals(2005, p.getValue(0));
    }

    /**
     * @target Property.addWrapFieldToCopy(int)
     * @scenario Add value that wraps within field bounds
     * @defectRisk Ensure wrap-around within field works via property
     */
    @Test(timeout = 4000)
    public void testProperty_AddWrapFieldToCopy() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 11);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial p2 = prop.addWrapFieldToCopy(3);
        assertEquals(2, p2.getValue(0));
    }

    /**
     * @target Property.setCopy(int)
     * @scenario Set new value via property
     * @defectRisk Ensure setCopy updates field correctly
     */
    @Test(timeout = 4000)
    public void testProperty_SetCopy() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        Partial p2 = prop.setCopy(2010);
        assertEquals(2010, p2.getValue(0));
    }

    /**
     * @target Property.setCopy(String)
     * @scenario Set new value via text with no locale
     * @defectRisk Ensure text-based setCopy works without locale
     */
    @Test(timeout = 4000)
    public void testProperty_SetCopyText() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial p2 = prop.setCopy("December");
        assertEquals(12, p2.getValue(0));
    }

    /**
     * @target Property.setCopy(String, Locale)
     * @scenario Set new value via text with explicit locale
     * @defectRisk Ensure text-based setCopy with locale parameter works
     */
    @Test(timeout = 4000)
    public void testProperty_SetCopyTextLocale() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial p2 = prop.setCopy("December", Locale.US);
        assertEquals(12, p2.getValue(0));
    }

    /**
     * @target Property.withMaximumValue()
     * @scenario Set field to its maximum value
     * @defectRisk Ensure withMaximumValue correctly sets max field value
     */
    @Test(timeout = 4000)
    public void testProperty_WithMaximumValue() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial p2 = prop.withMaximumValue();
        assertEquals(12, p2.getValue(0));
    }

    /**
     * @target Property.withMinimumValue()
     * @scenario Set field to its minimum value
     * @defectRisk Ensure withMinimumValue correctly sets min field value
     */
    @Test(timeout = 4000)
    public void testProperty_WithMinimumValue() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Partial p2 = prop.withMinimumValue();
        assertEquals(1, p2.getValue(0));
    }

    /**
     * @target Property.getField()
     * @scenario Verify field returned by property matches partial's field
     * @defectRisk Ensure getField() delegates to correct index
     */
    @Test(timeout = 4000)
    public void testProperty_GetField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        assertNotNull(prop.getField());
    }

    /**
     * @target Constructor duplicate detection with range field comparison
     * @scenario Two fields with same unit but different range causing duplicate branch (equal range)
     * @defectRisk Ensure duplicate detection catches equal-range duplicates correctly
     */
    @Test(timeout = 4000)
    public void testMultiFieldConstructor_DuplicateWithRange() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.hourOfDay(), DateTimeFieldType.clockhourOfDay()
        };
        int[] values = new int[] {10, 10};
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected - duplicate or order error
        }
    }

    /**
     * @target Constructor validation using chronology.validate
     * @scenario Provide invalid value out of range for field, expect exception
     * @defectRisk Ensure chronology validation of values is performed
     */
    @Test(timeout = 4000)
    public void testConstructor_InvalidValue() {
        try {
            new Partial(DateTimeFieldType.monthOfYear(), 13);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * @target with(DateTimeFieldType, int) insertion at end (largest existing already smaller unit)
     * @scenario Insert a field whose unit is smaller than all existing fields, goes to end
     * @defectRisk Ensure insertion at end-of-array branch works correctly
     */
    @Test(timeout = 4000)
    public void testWith_InsertAtEnd() {
        Partial p = new Partial(DateTimeFieldType.year(), 2005);
        Partial p2 = p.with(DateTimeFieldType.secondOfMinute(), 30);
        assertEquals(2, p2.size());
        assertEquals(DateTimeFieldType.year(), p2.getFieldType(0));
        assertEquals(DateTimeFieldType.secondOfMinute(), p2.getFieldType(1));
    }

    /**
     * @target with(DateTimeFieldType, int) insertion of unsupported duration type field
     * @scenario Insert field with unsupported duration into empty partial
     * @defectRisk Ensure unsupported unitField.isSupported() false branch works
     */
    @Test(timeout = 4000)
    public void testWith_UnsupportedDurationField() {
        Partial p = new Partial();
        Partial p2 = p.with(DateTimeFieldType.year(), 2005);
        assertEquals(1, p2.size());
        assertEquals(2005, p2.getValue(0));
    }
}