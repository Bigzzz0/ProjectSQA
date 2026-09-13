package org.joda.time;

import org.joda.time.*;
import org.joda.time.chrono.*;
import org.joda.time.format.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Target Class: org.joda.time.Partial
 * Key Focus Areas:
 *  1. Defect Zone (Time-1 / Issue 93):
 *     - Out-of-order field specification where DateTimeFieldType.era() has an UnsupportedDurationField,
 *       causing compareTo() in standard DurationField to report supported > unsupported (e.g. dayOfMonth > era),
 *       bypassing largest-to-smallest ordering check in Partial(DateTimeFieldType[], int[], Chronology).
 *  2. Partition A: Constructors & Ordering Validation:
 *     - Empty constructor, null Chronology fallback to ISOChronology.getInstanceUTC().
 *     - Single field constructor: null type validation, value bounds validation.
 *     - Multi-field constructor: null types/values, mismatched array lengths, empty arrays,
 *       null elements within types array, duplicate types, out-of-order unit fields,
 *       equal unit fields with identical or inverted range duration fields.
 *     - Copy constructor: from ReadablePartial, null argument validation.
 *  3. Partition B: Introspection & Structural Access:
 *     - size(), getChronology(), getFieldType(int), getFieldTypes(), getValue(int), getValues().
 *     - Boundary indexing (valid indices, negative indices, indices >= size()).
 *  4. Partition C: Immutability, Mutation & Arithmetic:
 *     - withChronologyRetainFields(): identity check if Chronology matches, re-validation under new Chronology.
 *     - with(DateTimeFieldType, int): null type check, existing field (same vs different value),
 *       new field insertion (at beginning, middle, end based on duration/range duration comparisons).
 *     - without(DateTimeFieldType): existing field removal (first, middle, last), absent field no-op.
 *     - withField(DateTimeFieldType, int): unsupported field validation, identical vs new value.
 *     - withFieldAdded / withFieldAddWrapped: 0 amount short-circuit, overflow & wrap handling.
 *     - withPeriodAdded / plus / minus: null period / 0 scalar short-circuit, matching & ignored fields.
 *  5. Partition D: Matching, Formatting & Property Operations:
 *     - isMatch(ReadableInstant): null instant (system clock), match true, mismatch false.
 *     - isMatch(ReadablePartial): null partial validation, partial missing fields, match vs mismatch.
 *     - getFormatter(), toString(), toStringList(), toString(String), toString(String, Locale).
 *     - Property accessors: getField(), get(), addToCopy(), addWrapFieldToCopy(), setCopy(int),
 *       setCopy(String), setCopy(String, Locale), withMaximumValue(), withMinimumValue().
 * --------------------------------------------------------------------------------------------------
 */
public class PartialGeminiTest {

    // =========================================================================
    // Partition E: Defect Zone (Time-1 / Issue 93)
    // =========================================================================

    /**
     * Defects4J Time-1 regression test.
     * DateTimeFieldType.era() duration is unsupported. When placed after smaller fields
     * (e.g., year, dayOfMonth), the constructor must reject the ordering with IllegalArgumentException.
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

    @Test(timeout = 4000)
    public void testConstructor_RejectOutOfOrderEraFirstThenYearThenEra() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.era(),
            DateTimeFieldType.year(),
            DateTimeFieldType.era()
        };
        int[] values = new int[] { 1, 2010, 1 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException for duplicate era");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    // =========================================================================
    // Partition A: Constructors & Ordering Validation
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor_DefaultAndChronology() {
        Partial p1 = new Partial();
        assertEquals(0, p1.size());
        assertEquals(ISOChronology.getInstanceUTC(), p1.getChronology());

        Partial p2 = new Partial((Chronology) null);
        assertEquals(0, p2.size());
        assertEquals(ISOChronology.getInstanceUTC(), p2.getChronology());

        Partial p3 = new Partial(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstanceUTC(), p3.getChronology());
    }

    @Test(timeout = 4000)
    public void testConstructor_SingleField() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 14);
        assertEquals(1, p.size());
        assertEquals(DateTimeFieldType.hourOfDay(), p.getFieldType(0));
        assertEquals(14, p.getValue(0));
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());

        Partial pChrono = new Partial(DateTimeFieldType.dayOfMonth(), 10, CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), pChrono.getChronology());
        assertEquals(10, pChrono.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_SingleField_NullType() {
        new Partial((DateTimeFieldType) null, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_SingleField_InvalidValue() {
        new Partial(DateTimeFieldType.dayOfMonth(), 32);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_MultiField_NullTypes() {
        new Partial(null, new int[] { 1 });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_MultiField_NullValues() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year() }, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_MultiField_MismatchedLengths() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year() }, new int[] { 2000, 10 });
    }

    @Test(timeout = 4000)
    public void testConstructor_MultiField_EmptyArrays() {
        Partial p = new Partial(new DateTimeFieldType[0], new int[0]);
        assertEquals(0, p.size());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_MultiField_NullElementInTypes() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year(), null }, new int[] { 2000, 1 });
    }

    @Test(timeout = 4000)
    public void testConstructor_MultiField_DuplicateFieldsThrows() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.year()
        };
        int[] values = new int[] { 2000, 2001 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException for duplicate fields");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("duplicate"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructor_MultiField_DuplicateFieldsWithSameUnitAndRange() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfWeek(),
            DateTimeFieldType.dayOfWeek()
        };
        int[] values = new int[] { 1, 2 };
        try {
            new Partial(types, values);
            fail("Expected duplicate exception");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("duplicate"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructor_MultiField_EqualUnitFieldOutOfOrderRanges() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfWeek(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 1, 15 };
        try {
            new Partial(types, values);
            fail("Expected order error: week range is smaller than month range");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("order"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructor_MultiField_EqualUnitFieldCorrectRanges() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 100, 10 };
        Partial p = new Partial(types, values);
        assertEquals(2, p.size());
        assertEquals(100, p.getValue(0));
        assertEquals(10, p.getValue(1));
    }

    @Test(timeout = 4000)
    public void testConstructor_MultiField_OutOfOrderUnitsThrows() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.minuteOfHour(),
            DateTimeFieldType.hourOfDay()
        };
        int[] values = new int[] { 30, 12 };
        try {
            new Partial(types, values);
            fail("Expected IllegalArgumentException: minutes < hours");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("order"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructor_CopyReadablePartial() {
        LocalDate date = new LocalDate(2021, 5, 20);
        Partial p = new Partial(date);
        assertEquals(3, p.size());
        assertEquals(2021, p.getValue(0));
        assertEquals(5, p.getValue(1));
        assertEquals(20, p.getValue(2));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_CopyReadablePartial_Null() {
        new Partial((ReadablePartial) null);
    }

    // =========================================================================
    // Partition B: Field Access & Introspection
    // =========================================================================

    @Test(timeout = 4000)
    public void testFieldAccessAndIntrospection() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 2022, 11, 28 };
        Partial p = new Partial(types, values);

        assertEquals(3, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(2));

        assertEquals(2022, p.getValue(0));
        assertEquals(11, p.getValue(1));
        assertEquals(28, p.getValue(2));

        assertArrayEquals(types, p.getFieldTypes());
        assertArrayEquals(values, p.getValues());

        // Assert defensive copy
        int[] clonedValues = p.getValues();
        clonedValues[0] = 1999;
        assertEquals(2022, p.getValue(0));

        DateTimeFieldType[] clonedTypes = p.getFieldTypes();
        clonedTypes[0] = DateTimeFieldType.era();
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldType_IndexTooHigh() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        p.getFieldType(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldType_NegativeIndex() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        p.getFieldType(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValue_IndexTooHigh() {
        Partial p = new Partial(DateTimeFieldType.year(), 2000);
        p.getValue(1);
    }

    // =========================================================================
    // Partition C: Immutable Mutation & Arithmetic
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithChronologyRetainFields() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        assertSame(p, p.withChronologyRetainFields(null));
        assertSame(p, p.withChronologyRetainFields(ISOChronology.getInstanceUTC()));

        Partial coptic = p.withChronologyRetainFields(CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), coptic.getChronology());
        assertEquals(2020, coptic.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithChronologyRetainFields_InvalidForNewChronology() {
        Partial p = new Partial(DateTimeFieldType.dayOfMonth(), 31);
        // Coptic months have at most 30 days (or 5-6 in intercalary month)
        p.withChronologyRetainFields(CopticChronology.getInstanceUTC());
    }

    @Test(timeout = 4000)
    public void testWith_AddAndModifyFields() {
        Partial p = new Partial();
        // Insert first
        p = p.with(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(1, p.size());
        assertEquals(6, p.getValue(0));

        // Insert larger (year) -> becomes index 0
        p = p.with(DateTimeFieldType.year(), 2022);
        assertEquals(2, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(2022, p.getValue(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        assertEquals(6, p.getValue(1));

        // Insert smaller (dayOfMonth) -> becomes index 2
        p = p.with(DateTimeFieldType.dayOfMonth(), 15);
        assertEquals(3, p.size());
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(2));
        assertEquals(15, p.getValue(2));

        // Set same value on existing field returns `this`
        Partial same = p.with(DateTimeFieldType.dayOfMonth(), 15);
        assertSame(p, same);

        // Modify existing field value
        Partial modified = p.with(DateTimeFieldType.dayOfMonth(), 25);
        assertNotSame(p, modified);
        assertEquals(25, modified.getValue(2));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWith_NullType() {
        Partial p = new Partial();
        p.with(null, 1);
    }

    @Test(timeout = 4000)
    public void testWithout() {
        Partial p = new Partial()
            .with(DateTimeFieldType.year(), 2020)
            .with(DateTimeFieldType.monthOfYear(), 10)
            .with(DateTimeFieldType.dayOfMonth(), 12);

        // Remove middle
        Partial pMinusMonth = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(2, pMinusMonth.size());
        assertEquals(DateTimeFieldType.year(), pMinusMonth.getFieldType(0));
        assertEquals(DateTimeFieldType.dayOfMonth(), pMinusMonth.getFieldType(1));

        // Remove absent returns this
        assertSame(pMinusMonth, pMinusMonth.without(DateTimeFieldType.hourOfDay()));

        // Remove all
        Partial pEmpty = pMinusMonth.without(DateTimeFieldType.year()).without(DateTimeFieldType.dayOfMonth());
        assertEquals(0, pEmpty.size());
    }

    @Test(timeout = 4000)
    public void testWithField() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertSame(p, p.withField(DateTimeFieldType.hourOfDay(), 10));

        Partial updated = p.withField(DateTimeFieldType.hourOfDay(), 15);
        assertEquals(15, updated.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithField_Unsupported() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withField(DateTimeFieldType.minuteOfHour(), 30);
    }

    @Test(timeout = 4000)
    public void testWithFieldAddedAndWrapped() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 22);

        assertSame(p, p.withFieldAdded(DurationFieldType.hours(), 0));
        assertSame(p, p.withFieldAddWrapped(DurationFieldType.hours(), 0));

        Partial added = p.withFieldAdded(DurationFieldType.hours(), 1);
        assertEquals(23, added.getValue(0));

        Partial wrapped = p.withFieldAddWrapped(DurationFieldType.hours(), 3);
        assertEquals(1, wrapped.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAdded_Unsupported() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withFieldAdded(DurationFieldType.minutes(), 5);
    }

    @Test(timeout = 4000)
    public void testWithPeriodAdded_Plus_Minus() {
        Partial p = new Partial()
            .with(DateTimeFieldType.hourOfDay(), 10)
            .with(DateTimeFieldType.minuteOfHour(), 30);

        assertSame(p, p.withPeriodAdded(null, 1));
        assertSame(p, p.withPeriodAdded(Period.hours(1), 0));
        assertSame(p, p.plus(null));
        assertSame(p, p.minus(null));

        Period period = Period.hours(2).withMinutes(15).withDays(5); // days not in partial, ignored
        Partial pAdded = p.plus(period);
        assertEquals(12, pAdded.getValue(0));
        assertEquals(45, pAdded.getValue(1));

        Partial pSubtracted = p.minus(Period.hours(1));
        assertEquals(9, pSubtracted.getValue(0));
        assertEquals(30, pSubtracted.getValue(1));
    }

    // =========================================================================
    // Partition D: Matching, Comparisons, Formatting & Property
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsMatch_ReadableInstant() {
        DateTime dt = new DateTime(2023, 7, 14, 16, 45, 0, 0, DateTimeZone.UTC);

        Partial pMatch = new Partial()
            .with(DateTimeFieldType.year(), 2023)
            .with(DateTimeFieldType.monthOfYear(), 7);
        assertTrue(pMatch.isMatch(dt));

        Partial pMismatch = new Partial(DateTimeFieldType.year(), 2022);
        assertFalse(pMismatch.isMatch(dt));

        // Empty partial always matches any instant
        Partial pEmpty = new Partial();
        assertTrue(pEmpty.isMatch(dt));
        assertTrue(pEmpty.isMatch((ReadableInstant) null));
    }

    @Test(timeout = 4000)
    public void testIsMatch_ReadablePartial() {
        LocalDate localDate = new LocalDate(2023, 7, 14);

        Partial pMatch = new Partial()
            .with(DateTimeFieldType.year(), 2023)
            .with(DateTimeFieldType.dayOfMonth(), 14);
        assertTrue(pMatch.isMatch(localDate));

        Partial pMismatch = new Partial()
            .with(DateTimeFieldType.year(), 2023)
            .with(DateTimeFieldType.dayOfMonth(), 15);
        assertFalse(pMismatch.isMatch(localDate));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsMatch_ReadablePartial_Null() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.isMatch((ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsMatch_ReadablePartial_MissingFieldThrows() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        LocalDate localDate = new LocalDate(2023, 7, 14); // does not have hourOfDay
        p.isMatch(localDate);
    }

    @Test(timeout = 4000)
    public void testGetFormatterAndToString() {
        Partial pEmpty = new Partial();
        assertNull(pEmpty.getFormatter());
        assertEquals("[]", pEmpty.toString());

        Partial pDate = new Partial()
            .with(DateTimeFieldType.year(), 2023)
            .with(DateTimeFieldType.monthOfYear(), 7)
            .with(DateTimeFieldType.dayOfMonth(), 14);

        assertNotNull(pDate.getFormatter());
        assertEquals("2023-07-14", pDate.toString());

        // Custom toString patterns
        assertEquals("14/07/2023", pDate.toString("dd/MM/yyyy"));
        assertEquals("2023-07-14", pDate.toString(null));
        assertEquals("July", pDate.toString("MMMM", Locale.ENGLISH));
        assertEquals("2023-07-14", pDate.toString(null, Locale.ENGLISH));

        // Format where fields don't compose a standard ISO structure falls back to toStringList
        Partial nonStandard = new Partial()
            .with(DateTimeFieldType.year(), 2023)
            .with(DateTimeFieldType.minuteOfHour(), 15);
        assertEquals("[year=2023, minuteOfHour=15]", nonStandard.toString());
        assertEquals("[year=2023, minuteOfHour=15]", nonStandard.toStringList());
    }

    @Test(timeout = 4000)
    public void testPropertyMethods() {
        Partial p = new Partial()
            .with(DateTimeFieldType.year(), 2020)
            .with(DateTimeFieldType.monthOfYear(), 12)
            .with(DateTimeFieldType.dayOfMonth(), 15);

        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        assertNotNull(prop);
        assertSame(p, prop.getPartial());
        assertSame(p, prop.getReadablePartial());
        assertEquals(DateTimeFieldType.monthOfYear(), prop.getFieldType());
        assertEquals(12, prop.get());
        assertEquals(p.getField(1), prop.getField());

        // addToCopy
        Partial pAdded = prop.addToCopy(-2);
        assertEquals(10, pAdded.getValue(1));

        // addWrapFieldToCopy
        Partial pWrap = prop.addWrapFieldToCopy(1);
        assertEquals(1, pWrap.getValue(1)); // wraps 12 -> 1

        // setCopy int
        Partial pSetInt = prop.setCopy(5);
        assertEquals(5, pSetInt.getValue(1));

        // setCopy text
        Partial pSetText = prop.setCopy("6");
        assertEquals(6, pSetText.getValue(1));

        Partial pSetTextLocale = prop.setCopy("January", Locale.ENGLISH);
        assertEquals(1, pSetTextLocale.getValue(1));

        // withMaximumValue and withMinimumValue
        Partial pMax = prop.withMaximumValue();
        assertEquals(12, pMax.getValue(1));

        Partial pMin = prop.withMinimumValue();
        assertEquals(1, pMin.getValue(1));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testProperty_UnsupportedField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.property(DateTimeFieldType.secondOfDay());
    }
}