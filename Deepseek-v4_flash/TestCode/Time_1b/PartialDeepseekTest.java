package org.joda.time;

import java.util.Locale;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for {@link Partial}.
 * Targets maximum line/branch coverage and the known defect
 * where out-of-order fields (era after year/day) are not rejected.
 */
public class PartialDeepseekTest {

    // -----------------------------------------------------------------------
    // Constructor tests
    // -----------------------------------------------------------------------

    /**
     * @target Partial()
     * @scenario Default constructor yields empty partial with ISO chronology.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_Empty() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
    }

    /**
     * @target Partial(Chronology)
     * @scenario Constructor with null chronology yields ISO UTC.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_Chronology_Null() {
        Partial p = new Partial((Chronology) null);
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
    }

    /**
     * @target Partial(DateTimeFieldType, int)
     * @scenario Single field partial created successfully.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_SingleField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        assertEquals(1, p.size());
        assertEquals(2000, p.getValue(0));
    }

    /**
     * @target Partial(DateTimeFieldType, int)
     * @scenario Null field type throws IllegalArgumentException.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_SingleField_NullType() {
        try {
            new Partial(null, 1);
            fail("Expected IllegalArgumentException for null field type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("field type must not be null"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType, int, Chronology)
     * @scenario Single field with explicit chronology.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_SingleFieldWithChrono() {
        Chronology buddhist = ISOChronology.getInstanceUTC();
        Partial p = new Partial(DateTimeFieldType.year(), 2543, buddhist);
        assertEquals(buddhist, p.getChronology());
        assertEquals(2543, p.getValue(0));
    }

    /**
     * @target Partial(DateTimeFieldType[], int[])
     * @scenario Two-array constructor with valid fields.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_TwoArrays_Valid() {
        DateTimeFieldType[] types = { DateTimeFieldType.year(), DateTimeFieldType.monthOfYear() };
        int[] values = { 2000, 6 };
        Partial p = new Partial(types, values);
        assertEquals(2, p.size());
        assertEquals(2000, p.getValue(0));
        assertEquals(6, p.getValue(1));
    }

    /**
     * @target Partial(DateTimeFieldType[], int[])
     * @scenario Null types array throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_TwoArrays_NullTypes() {
        try {
            new Partial(null, new int[0]);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Types array must not be null"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[])
     * @scenario Null values array throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_TwoArrays_NullValues() {
        try {
            new Partial(new DateTimeFieldType[0], null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Values array must not be null"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[])
     * @scenario Mismatched lengths throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_TwoArrays_MismatchedLengths() {
        DateTimeFieldType[] types = { DateTimeFieldType.year() };
        int[] values = { 2000, 6 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("same length"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[])
     * @scenario Null element in types array throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_TwoArrays_NullInTypes() {
        DateTimeFieldType[] types = { DateTimeFieldType.year(), null };
        int[] values = { 2000, 6 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not contain null"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[])
     * @scenario Duplicate fields thrown.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_TwoArrays_DuplicateField() {
        DateTimeFieldType[] types = { DateTimeFieldType.year(), DateTimeFieldType.year() };
        int[] values = { 2000, 2001 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not contain duplicate"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[])
     * @scenario Out-of-order fields thrown (smaller before larger).
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_TwoArrays_OutOfOrder() {
        DateTimeFieldType[] types = { DateTimeFieldType.dayOfMonth(), DateTimeFieldType.year() };
        int[] values = { 1, 2000 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("order"));
        }
    }

    /**
     * @target Partial(DateTimeFieldType[], int[]) – Defect-specific test
     * @scenario era() placed after year() and dayOfMonth().
     *   The field order is year, dayOfMonth, era.
     *   In ISO chronology, era has duration type "eras" which is larger than
     *   years and days, so this order violates largest-to-smallest.
     * @defectRisk Defects4J bug: out-of-order with era is not rejected.
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
     * @target Partial(ReadablePartial)
     * @scenario Copy constructor from another Partial.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_Copy() {
        Partial original = new Partial(DateTimeFieldType.year(), 2020);
        Partial copy = new Partial((ReadablePartial) original);
        assertEquals(original, copy);
        assertNotSame(original, copy);
    }

    /**
     * @target Partial(ReadablePartial)
     * @scenario Null partial throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testConstructor_Copy_Null() {
        try {
            new Partial((ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    // -----------------------------------------------------------------------
    // with() method tests
    // -----------------------------------------------------------------------

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Add a new field to empty partial.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWith_AddNewField_Empty() {
        Partial p = new Partial().with(DateTimeFieldType.year(), 2000);
        assertEquals(1, p.size());
        assertEquals(2000, p.getValue(0));
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Replace existing field value.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWith_ReplaceExistingField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        Partial q = p.with(DateTimeFieldType.year(), 2001);
        assertEquals(2001, q.getValue(0));
        assertNotSame(p, q);
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Same value returns this.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWith_SameValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        assertSame(p, p.with(DateTimeFieldType.year(), 2000));
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Null field type throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWith_NullFieldType() {
        Partial p = new Partial();
        try {
            p.with(null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("field type must not be null"));
        }
    }

    /**
     * @target with(DateTimeFieldType, int)
     * @scenario Insert field in correct order (year, month, day).
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWith_InsertOrder() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        Partial q = p.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(2, q.size());
        assertEquals(DateTimeFieldType.year(), q.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), q.getFieldType(1));
    }

    // -----------------------------------------------------------------------
    // without() method tests
    // -----------------------------------------------------------------------

    /**
     * @target without(DateTimeFieldType)
     * @scenario Remove existing field.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWithout_RemoveExisting() {
        Partial p = new Partial(new DateTimeFieldType[]{
                DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()},
                new int[]{2000, 6});
        Partial q = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(1, q.size());
        assertEquals(2000, q.getValue(0));
    }

    /**
     * @target without(DateTimeFieldType)
     * @scenario Remove non-existing field returns this.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWithout_RemoveNonExisting() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        assertSame(p, p.without(DateTimeFieldType.dayOfMonth()));
    }

    /**
     * @target without(DateTimeFieldType)
     * @scenario Remove all fields results in empty.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testWithout_RemoveAll() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        Partial q = p.without(DateTimeFieldType.year());
        assertEquals(0, q.size());
    }

    // -----------------------------------------------------------------------
    // plus / minus / withPeriodAdded tests
    // -----------------------------------------------------------------------

    /**
     * @target plus(ReadablePeriod)
     * @scenario Add period to partial.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testPlus_Period() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        Partial q = p.plus(Period.years(1));
        assertEquals(2001, q.getValue(0));
    }

    /**
     * @target plus(ReadablePeriod)
     * @scenario Null period returns this.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testPlus_NullPeriod() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        assertSame(p, p.plus(null));
    }

    /**
     * @target minus(ReadablePeriod)
     * @scenario Subtract period.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testMinus_Period() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        Partial q = p.minus(Period.years(1));
        assertEquals(1999, q.getValue(0));
    }

    // -----------------------------------------------------------------------
    // isMatch tests
    // -----------------------------------------------------------------------

    /**
     * @target isMatch(ReadableInstant)
     * @scenario Partial matches instant.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testIsMatch_Instant_True() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        DateTime instant = new DateTime(2000, 6, 15, 12, 0, 0, ISOChronology.getInstanceUTC());
        assertTrue(p.isMatch(instant));
    }

    /**
     * @target isMatch(ReadableInstant)
     * @scenario Partial does not match instant.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testIsMatch_Instant_False() {
        Partial p = new Partial(DateTimeFieldType.year(), 1999);
        DateTime instant = new DateTime(2000, 6, 15, 12, 0, 0, ISOChronology.getInstanceUTC());
        assertFalse(p.isMatch(instant));
    }

    /**
     * @target isMatch(ReadablePartial)
     * @scenario Partial matches another partial.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testIsMatch_Partial_True() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2000);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2000);
        assertTrue(p1.isMatch(p2));
    }

    /**
     * @target isMatch(ReadablePartial)
     * @scenario Partial does not match another partial.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testIsMatch_Partial_False() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2000);
        Partial p2 = new Partial(DateTimeFieldType.year(), 1999);
        assertFalse(p1.isMatch(p2));
    }

    /**
     * @target isMatch(ReadablePartial)
     * @scenario Null partial throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testIsMatch_Partial_Null() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        try {
            p.isMatch((ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    // -----------------------------------------------------------------------
    // toString tests
    // -----------------------------------------------------------------------

    /**
     * @target toString()
     * @scenario Empty partial returns list representation.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testToString_Empty() {
        Partial p = new Partial();
        assertEquals("[]", p.toString());
    }

    /**
     * @target toString()
     * @scenario Single field partial returns formatted string.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testToString_SingleField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        String result = p.toString();
        // The exact format may vary, but it should not be the list form
        assertNotNull(result);
        assertFalse(result.startsWith("["));
    }

    /**
     * @target toString(String)
     * @scenario Pattern formatting.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testToString_Pattern() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        String result = p.toString("yyyy");
        assertEquals("2000", result);
    }

    /**
     * @target toString(String)
     * @scenario Null pattern falls back to toString().
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testToString_PatternNull() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        assertEquals(p.toString(), p.toString((String) null));
    }

    /**
     * @target toString(String, Locale)
     * @scenario Pattern and locale.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testToString_PatternLocale() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        String result = p.toString("MMMM", Locale.ENGLISH);
        assertEquals("June", result);
    }

    // -----------------------------------------------------------------------
    // equals/hashCode/compareTo tests
    // -----------------------------------------------------------------------

    /**
     * @target equals(Object)
     * @scenario Equal partials.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testEquals_Equal() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2000);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2000);
        assertEquals(p1, p2);
    }

    /**
     * @target equals(Object)
     * @scenario Different values.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testEquals_NotEqual() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2000);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2001);
        assertNotEquals(p1, p2);
    }

    /**
     * @target hashCode()
     * @scenario Equal objects have same hash.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testHashCode_EqualObjects() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2000);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2000);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    /**
     * @target compareTo(ReadablePartial)
     * @scenario Compare two partials.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testCompareTo_Equal() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2000);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2000);
        assertEquals(0, p1.compareTo(p2));
    }

    /**
     * @target compareTo(ReadablePartial)
     * @scenario Compare with larger value.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testCompareTo_Greater() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2001);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2000);
        assertTrue(p1.compareTo(p2) > 0);
    }

    // -----------------------------------------------------------------------
    // Property tests
    // -----------------------------------------------------------------------

    /**
     * @target property(DateTimeFieldType)
     * @scenario Get property for existing field.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testProperty() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        Partial.Property prop = p.property(DateTimeFieldType.year());
        assertEquals(2000, prop.get());
    }

    /**
     * @target property(DateTimeFieldType)
     * @scenario Unsupported field throws.
     * @defectRisk None.
     */
    @Test(timeout = 4000)
    public void testProperty_Unsupported() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        try {
            p.property(DateTimeFieldType.dayOfMonth());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}