/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.joda.time.Partial
 * 
 * 1. Constructor Branching & Validation:
 *   - Partial(): Default constructor -> ISO UTC Chronology, empty arrays.
 *   - Partial(Chronology): Null chronology fallback vs specified chronology.
 *   - Partial(DateTimeFieldType, int, Chronology):
 *       * null type check (throws IllegalArgumentException).
 *       * validation through chronology.validate().
 *   - Partial(DateTimeFieldType[], int[], Chronology):
 *       * null types array, null values array.
 *       * mismatched array lengths.
 *       * empty array short-circuit.
 *       * null elements in types array.
 *       * field ordering checks (largest to smallest) using DurationField.compareTo():
 *           - compare < 0 (order violation).
 *           - compare != 0 and unsupported duration field.
 *           - compare == 0:
 *               * both null range duration -> duplicate exception.
 *               * first null range, second non-null -> order violation.
 *               * both non-null range: compare range fields (< 0 order violation, == 0 duplicate).
 *   - Partial(ReadablePartial):
 *       * null partial argument (throws IllegalArgumentException).
 *       * copy constructor populating types and values.
 * 
 * 2. Field Accessors & Immutability:
 *   - size(), getChronology(), getField(int, Chronology), getFieldType(int), getFieldTypes(),
 *     getValue(int), getValues().
 *   - Cloning checks on getFieldTypes() and getValues() to prevent representation exposure.
 * 
 * 3. Mutation Operations (Immutability contract & identity preservation):
 *   - withChronologyRetainFields(Chronology):
 *       * same chronology (returns this).
 *       * different chronology (returns validated new Partial).
 *   - with(DateTimeFieldType, int):
 *       * null field type check.
 *       * existing field:
 *           - same value (returns this) [CRITICAL Defects4J ground truth target: testWith3].
 *           - different value (returns new Partial with updated value).
 *           - invalid value (throws IllegalArgumentException).
 *       * non-existing field:
 *           - insert at index 0 (larger unit).
 *           - insert in middle or end.
 *           - same unit field, secondary sorting by range duration field.
 *   - without(DateTimeFieldType):
 *       * non-existing field (returns this).
 *       * existing field (returns new Partial with field removed, size - 1).
 *   - withField(DateTimeFieldType, int):
 *       * unsupported field (throws IllegalArgumentException via indexOfSupported).
 *       * supported field with same value (returns this).
 *       * supported field with new value (returns new Partial).
 *   - withFieldAdded(DurationFieldType, int):
 *       * unsupported field.
 *       * amount == 0 (returns this).
 *       * amount != 0 (returns new Partial with added amount).
 *   - withFieldAddWrapped(DurationFieldType, int):
 *       * unsupported field.
 *       * amount == 0 (returns this).
 *       * amount != 0 (wraps within field range).
 *   - withPeriodAdded(ReadablePeriod, int):
 *       * null period or scalar == 0 (returns this).
 *       * positive / negative scalar.
 *       * period fields not in partial (ignored).
 *   - plus(ReadablePeriod), minus(ReadablePeriod).
 * 
 * 4. Comparison & Matching:
 *   - isMatch(ReadableInstant): null instant (now), matching instant, non-matching instant.
 *   - isMatch(ReadablePartial): null partial (IAE), matching partial, non-matching partial.
 * 
 * 5. String Formatting & Printing:
 *   - getFormatter(): empty partial (returns null), ISO compatible, non-ISO fallback.
 *   - toString(): formatted ISO, fallback to toStringList().
 *   - toStringList(): formatted list [field=value, ...].
 *   - toString(String): pattern based, null pattern.
 *   - toString(String, Locale): pattern with locale, null pattern.
 * 
 * 6. Property Inner Class:
 *   - getField(), getReadablePartial(), getPartial(), get().
 *   - addToCopy(int), addWrapFieldToCopy(int).
 *   - setCopy(int), setCopy(String), setCopy(String, Locale).
 *   - withMaximumValue(), withMinimumValue().
 */

package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;

public class PartialGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J TestPartial_Basics::testWith3.
     * Verifies that calling with() with an existing field and its current value
     * returns the exact same instance ('this') without unnecessary reallocation.
     */
    @Test(timeout = 4000)
    public void testWith3_DefectTarget_SameFieldAndValueReturnsSameInstance() {
        Partial test = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour()},
            new int[] {10, 20}
        );
        Partial result = test.with(DateTimeFieldType.hourOfDay(), 10);
        assertSame("with() should return 'this' when setting an existing field to its current value", test, result);

        Partial result2 = test.with(DateTimeFieldType.minuteOfHour(), 20);
        assertSame("with() should return 'this' when setting an existing field to its current value", test, result2);
    }

    /**
     * Further targets with() when adding a field before, between, and after existing fields.
     */
    @Test(timeout = 4000)
    public void testWith_InsertionsAtDifferentPositions() {
        Partial test = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour()},
            new int[] {10, 20}
        );

        // Insert larger unit (before existing fields)
        Partial withYear = test.with(DateTimeFieldType.year(), 2024);
        assertEquals(3, withYear.size());
        assertEquals(DateTimeFieldType.year(), withYear.getFieldType(0));
        assertEquals(DateTimeFieldType.hourOfDay(), withYear.getFieldType(1));
        assertEquals(DateTimeFieldType.minuteOfHour(), withYear.getFieldType(2));
        assertEquals(2024, withYear.getValue(0));
        assertEquals(10, withYear.getValue(1));
        assertEquals(20, withYear.getValue(2));

        // Insert smaller unit (after existing fields)
        Partial withSec = test.with(DateTimeFieldType.secondOfMinute(), 35);
        assertEquals(3, withSec.size());
        assertEquals(DateTimeFieldType.hourOfDay(), withSec.getFieldType(0));
        assertEquals(DateTimeFieldType.minuteOfHour(), withSec.getFieldType(1));
        assertEquals(DateTimeFieldType.secondOfMinute(), withSec.getFieldType(2));
        assertEquals(35, withSec.getValue(2));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & CONSTRUCTORS
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
        assertEquals(0, p.getFieldTypes().length);
        assertEquals(0, p.getValues().length);
    }

    @Test(timeout = 4000)
    public void testChronologyConstructor() {
        Chronology coptic = CopticChronology.getInstance();
        Partial p = new Partial(coptic);
        assertEquals(0, p.size());
        assertEquals(CopticChronology.getInstanceUTC(), p.getChronology());

        Partial pNull = new Partial((Chronology) null);
        assertEquals(ISOChronology.getInstanceUTC(), pNull.getChronology());
    }

    @Test(timeout = 4000)
    public void testSingleFieldConstructor() {
        Partial p = new Partial(DateTimeFieldType.dayOfMonth(), 15);
        assertEquals(1, p.size());
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(0));
        assertEquals(15, p.getValue(0));
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());

        Partial pChrono = new Partial(DateTimeFieldType.monthOfYear(), 6, BuddhistChronology.getInstance());
        assertEquals(1, pChrono.size());
        assertEquals(6, pChrono.getValue(0));
        assertEquals(BuddhistChronology.getInstanceUTC(), pChrono.getChronology());
    }

    @Test(timeout = 4000)
    public void testArraysConstructor_ValidOrdering() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2023, 11, 28};
        Partial p = new Partial(types, values, null);

        assertEquals(3, p.size());
        assertArrayEquals(types, p.getFieldTypes());
        assertArrayEquals(values, p.getValues());
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
    }

    @Test(timeout = 4000)
    public void testArraysConstructor_SameUnitDifferentRange() {
        // dayOfYear (range: years) vs dayOfMonth (range: months) vs dayOfWeek (range: weeks)
        // Same duration unit (days), ordered by descending range unit: years > months > weeks
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfYear(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfWeek()
        };
        int[] values = new int[] {150, 15, 3};
        Partial p = new Partial(types, values);
        assertEquals(3, p.size());
        assertEquals(150, p.getValue(0));
        assertEquals(15, p.getValue(1));
        assertEquals(3, p.getValue(2));
    }

    @Test(timeout = 4000)
    public void testCopyConstructor_FromReadablePartial() {
        Partial src = new Partial(DateTimeFieldType.hourOfDay(), 14);
        Partial copy = new Partial(src);
        assertEquals(1, copy.size());
        assertEquals(DateTimeFieldType.hourOfDay(), copy.getFieldType(0));
        assertEquals(14, copy.getValue(0));
        assertEquals(src.getChronology(), copy.getChronology());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & DEFENSIVE CHECKS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSingleFieldConstructor_NullTypeThrows() {
        new Partial((DateTimeFieldType) null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_NullTypesThrows() {
        new Partial(null, new int[] {1});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_NullValuesThrows() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.dayOfMonth()}, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_MismatchedLengthsThrows() {
        new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.dayOfMonth()},
            new int[] {1, 2}
        );
    }

    @Test(timeout = 4000)
    public void testArraysConstructor_EmptyArraysAllowed() {
        Partial p = new Partial(new DateTimeFieldType[0], new int[0]);
        assertEquals(0, p.size());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_NullElementInTypesThrows() {
        new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.year(), null},
            new int[] {2020, 1}
        );
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_InvalidOrder_SmallerToLargerThrows() {
        new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.minuteOfHour(), DateTimeFieldType.hourOfDay()},
            new int[] {15, 10}
        );
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_DuplicateFieldsThrows() {
        new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.hourOfDay()},
            new int[] {10, 10}
        );
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_SameUnitAscendingRangeOrderThrows() {
        // dayOfWeek (weeks) before dayOfMonth (months) violates descending order
        new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.dayOfWeek(), DateTimeFieldType.dayOfMonth()},
            new int[] {1, 15}
        );
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArraysConstructor_DuplicateRangeThrows() {
        new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.dayOfMonth(), DateTimeFieldType.dayOfMonth()},
            new int[] {10, 10}
        );
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCopyConstructor_NullReadablePartialThrows() {
        new Partial((ReadablePartial) null);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldType_OutOfBoundsNegativeThrows() {
        Partial p = new Partial(DateTimeFieldType.secondOfMinute(), 10);
        p.getFieldType(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldType_OutOfBoundsHighThrows() {
        Partial p = new Partial(DateTimeFieldType.secondOfMinute(), 10);
        p.getFieldType(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValue_OutOfBoundsThrows() {
        Partial p = new Partial(DateTimeFieldType.secondOfMinute(), 10);
        p.getValue(1);
    }

    // =========================================================================
    // PARTITION D: MUTATION OPERATIONS & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithChronologyRetainFields() {
        Partial p1 = new Partial(DateTimeFieldType.monthOfYear(), 5);
        Partial p2 = p1.withChronologyRetainFields(ISOChronology.getInstance());
        assertSame(p1, p2); // Same chronology UTC instance

        Partial p3 = p1.withChronologyRetainFields(GJChronology.getInstanceUTC());
        assertNotSame(p1, p3);
        assertEquals(GJChronology.getInstanceUTC(), p3.getChronology());
        assertEquals(5, p3.getValue(0));

        // Fallback null to ISO
        Partial p4 = p3.withChronologyRetainFields(null);
        assertEquals(ISOChronology.getInstanceUTC(), p4.getChronology());
    }

    @Test(timeout = 4000)
    public void testWith_ExistingField_DifferentValue() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 15);
        Partial updated = p.with(DateTimeFieldType.minuteOfHour(), 45);
        assertNotSame(p, updated);
        assertEquals(15, p.getValue(0));
        assertEquals(45, updated.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWith_NullFieldTypeThrows() {
        Partial p = new Partial();
        p.with(null, 10);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWith_ExistingField_InvalidValueThrows() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 15);
        p.with(DateTimeFieldType.minuteOfHour(), 99); // max is 59
    }

    @Test(timeout = 4000)
    public void testWithout() {
        Partial p = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour()},
            new int[] {10, 20}
        );
        // Remove non-existing field returns this
        Partial same = p.without(DateTimeFieldType.year());
        assertSame(p, same);

        // Remove existing field
        Partial removed = p.without(DateTimeFieldType.hourOfDay());
        assertEquals(1, removed.size());
        assertEquals(DateTimeFieldType.minuteOfHour(), removed.getFieldType(0));
        assertEquals(20, removed.getValue(0));

        // Remove remaining field results in empty
        Partial empty = removed.without(DateTimeFieldType.minuteOfHour());
        assertEquals(0, empty.size());
    }

    @Test(timeout = 4000)
    public void testWithField() {
        Partial p = new Partial(DateTimeFieldType.dayOfMonth(), 10);
        assertSame(p, p.withField(DateTimeFieldType.dayOfMonth(), 10));

        Partial changed = p.withField(DateTimeFieldType.dayOfMonth(), 20);
        assertNotSame(p, changed);
        assertEquals(20, changed.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithField_UnsupportedThrows() {
        Partial p = new Partial(DateTimeFieldType.dayOfMonth(), 10);
        p.withField(DateTimeFieldType.year(), 2024);
    }

    @Test(timeout = 4000)
    public void testWithFieldAdded() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 30);
        assertSame(p, p.withFieldAdded(DurationFieldType.minutes(), 0));

        Partial added = p.withFieldAdded(DurationFieldType.minutes(), 15);
        assertEquals(45, added.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAdded_UnsupportedThrows() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 30);
        p.withFieldAdded(DurationFieldType.hours(), 1);
    }

    @Test(timeout = 4000)
    public void testWithFieldAddWrapped() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 50);
        assertSame(p, p.withFieldAddWrapped(DurationFieldType.minutes(), 0));

        Partial wrapped = p.withFieldAddWrapped(DurationFieldType.minutes(), 15);
        assertEquals(5, wrapped.getValue(0));
    }

    @Test(timeout = 4000)
    public void testWithPeriodAdded_Plus_Minus() {
        Partial p = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour()},
            new int[] {10, 20}
        );

        assertSame(p, p.withPeriodAdded(null, 1));
        assertSame(p, p.withPeriodAdded(Period.hours(2), 0));
        assertSame(p, p.plus(null));
        assertSame(p, p.minus(null));

        Period period = Period.hours(2).withMinutes(15);
        Partial plusResult = p.plus(period);
        assertEquals(12, plusResult.getValue(0));
        assertEquals(35, plusResult.getValue(1));

        Partial minusResult = p.minus(period);
        assertEquals(8, minusResult.getValue(0));
        assertEquals(5, minusResult.getValue(1));

        // Period with fields not in Partial (e.g. days)
        Period daysAndMinutes = Period.days(3).withMinutes(5);
        Partial addedDaysMinutes = p.plus(daysAndMinutes);
        assertEquals(10, addedDaysMinutes.getValue(0));
        assertEquals(25, addedDaysMinutes.getValue(1));
    }

    // =========================================================================
    // PARTITION E: MATCHING & FORMATTING LOGIC
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsMatch_Instant() {
        // 2024-01-15T10:30:00Z -> millis: 1705314600000L
        DateTime instant = new DateTime(2024, 1, 15, 10, 30, 0, 0, DateTimeZone.UTC);

        Partial matching = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()},
            new int[] {2024, 1}
        );
        assertTrue(matching.isMatch(instant));

        Partial nonMatching = new Partial(DateTimeFieldType.year(), 2023);
        assertFalse(nonMatching.isMatch(instant));

        // Empty partial always matches
        assertTrue(new Partial().isMatch(instant));
        // Match with null instant (uses DateTimeUtils.currentTimeMillis)
        Partial empty = new Partial();
        assertTrue(empty.isMatch((ReadableInstant) null));
    }

    @Test(timeout = 4000)
    public void testIsMatch_Partial() {
        Partial full = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()},
            new int[] {2024, 1}
        );
        Partial subsetMatching = new Partial(DateTimeFieldType.year(), 2024);
        Partial subsetNonMatching = new Partial(DateTimeFieldType.year(), 2023);

        assertTrue(subsetMatching.isMatch(full));
        assertFalse(subsetNonMatching.isMatch(full));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsMatch_Partial_NullThrows() {
        Partial p = new Partial(DateTimeFieldType.year(), 2024);
        p.isMatch((ReadablePartial) null);
    }

    @Test(timeout = 4000)
    public void testGetFormatter_And_ToString() {
        // Empty partial
        Partial empty = new Partial();
        assertNull(empty.getFormatter());
        assertEquals("[]", empty.toString());

        // Valid ISO format (Year and Month)
        Partial ym = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.monthOfYear()},
            new int[] {2024, 5}
        );
        DateTimeFormatter fmt = ym.getFormatter();
        assertNotNull(fmt);
        assertEquals("2024-05", ym.toString());

        // Partial with non-standard ISO combination -> falls back to toStringList
        Partial nonIso = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.dayOfWeek(), DateTimeFieldType.minuteOfHour()},
            new int[] {3, 45}
        );
        assertEquals("[dayOfWeek=3, minuteOfHour=45]", nonIso.toString());

        // toStringList verification
        assertEquals("[year=2024, monthOfYear=5]", ym.toStringList());

        // toString(String) and toString(String, Locale)
        assertEquals("05/2024", ym.toString("MM/yyyy"));
        assertEquals("May", ym.toString("MMM", Locale.ENGLISH));
        assertEquals("2024-05", ym.toString(null));
        assertEquals("2024-05", ym.toString(null, Locale.ENGLISH));
    }

    // =========================================================================
    // PARTITION F: PROPERTY INNER CLASS
    // =========================================================================

    @Test(timeout = 4000)
    public void testProperty_Accessors() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 14);
        Partial.Property prop = p.property(DateTimeFieldType.hourOfDay());

        assertEquals(p, prop.getPartial());
        assertEquals(p, prop.getReadablePartial());
        assertEquals(14, prop.get());
        assertEquals(DateTimeFieldType.hourOfDay().getField(ISOChronology.getInstanceUTC()), prop.getField());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testProperty_UnsupportedFieldThrows() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 14);
        p.property(DateTimeFieldType.minuteOfHour());
    }

    @Test(timeout = 4000)
    public void testProperty_AddToCopy_And_AddWrapFieldToCopy() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 45);
        Partial.Property prop = p.property(DateTimeFieldType.minuteOfHour());

        Partial added = prop.addToCopy(10);
        assertEquals(55, added.getValue(0));

        Partial wrapped = prop.addWrapFieldToCopy(20);
        assertEquals(5, wrapped.getValue(0));
    }

    @Test(timeout = 4000)
    public void testProperty_SetCopy_Int_String_Locale() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 2);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());

        Partial setInt = prop.setCopy(8);
        assertEquals(8, setInt.getValue(0));

        Partial setStr = prop.setCopy("11");
        assertEquals(11, setStr.getValue(0));

        Partial setStrLocale = prop.setCopy("December", Locale.ENGLISH);
        assertEquals(12, setStrLocale.getValue(0));

        Partial min = prop.withMinimumValue();
        assertEquals(1, min.getValue(0));

        Partial max = prop.withMaximumValue();
        assertEquals(12, max.getValue(0));
    }

    // =========================================================================
    // PARTITION G: CONTRACT INTEGRITY & SERIALIZATION
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        Partial p = new Partial(
            new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth()},
            new int[] {2024, 25}
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(p);
        }

        Partial deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (Partial) ois.readObject();
        }

        assertEquals(p.size(), deserialized.size());
        assertEquals(p.getChronology(), deserialized.getChronology());
        assertArrayEquals(p.getFieldTypes(), deserialized.getFieldTypes());
        assertArrayEquals(p.getValues(), deserialized.getValues());
        assertEquals(p, deserialized);
    }

    @Test(timeout = 4000)
    public void testArrayCloningIntegrity() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.dayOfMonth()};
        int[] values = new int[] {10};
        Partial p = new Partial(types, values);

        // Mutating external arrays should not affect internal state
        types[0] = DateTimeFieldType.hourOfDay();
        values[0] = 99;
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(0));
        assertEquals(10, p.getValue(0));

        // Mutating returned arrays should not affect internal state
        DateTimeFieldType[] returnedTypes = p.getFieldTypes();
        int[] returnedValues = p.getValues();
        returnedTypes[0] = DateTimeFieldType.year();
        returnedValues[0] = 999;
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(0));
        assertEquals(10, p.getValue(0));
    }
}