/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class : org.joda.time.Partial
 * Primary Defect: Defects4J bug in Partial where fields with no range duration type (e.g. era, year)
 *                 trigger a false-positive duplicate check in constructor/with() due to
 *                 UnsupportedDurationField.compareTo() returning 0.
 *
 * Branch & Decision Coverage Map:
 * - Constructors:
 *     * Partial() default ISO UTC empty.
 *     * Partial(Chronology) null fallback vs non-null (ISO, Buddhist, Coptic).
 *     * Partial(DateTimeFieldType, int) & with Chronology (null vs non-null, valid vs invalid values).
 *     * Partial(DateTimeFieldType[], int[]) validations:
 *         - types == null, values == null, lengths mismatch, length == 0.
 *         - types[i] == null.
 *         - compare < 0 (order violation).
 *         - compare != 0 && !loopUnitField.isSupported().
 *         - compare == 0:
 *             * lastRange == null && loopRange == null -> duplicate error (Defect zone).
 *             * lastRange == null && loopRange != null.
 *             * lastRange != null && loopRange == null -> order violation.
 *             * lastRange < loopRange -> order violation.
 *             * lastRange == loopRange -> duplicate error.
 *     * Partial(ReadablePartial) null vs non-null copy.
 * - with(DateTimeFieldType, int):
 *     * fieldType == null.
 *     * fieldType not present -> insertion at index 0, middle, or end.
 *     * fieldType not present -> insertion comparing unit fields and range fields.
 *     * fieldType present and value identical -> returns this.
 *     * fieldType present and value changed -> returns new updated Partial.
 * - without(DateTimeFieldType):
 *     * fieldType not present -> returns this.
 *     * fieldType present -> returns copy without field.
 * - withField / withFieldAdded / withFieldAddWrapped:
 *     * field not supported -> IllegalArgumentException.
 *     * amount/value unchanged / 0 -> returns this.
 *     * value changed / wrapped correctly.
 * - withPeriodAdded / plus / minus:
 *     * period == null / scalar == 0 -> returns this.
 *     * period with supported fields vs unsupported fields (ignored).
 * - withChronologyRetainFields:
 *     * same chrono -> returns this.
 *     * different chrono -> returns new Partial with validation.
 * - isMatch(ReadableInstant) & isMatch(ReadablePartial):
 *     * null instant (now), matching vs non-matching instant.
 *     * null partial (IAE), matching vs non-matching partial.
 * - getFormatter & toString variants:
 *     * size == 0 -> null formatter, toStringList().
 *     * ISO printable vs non-ISO dump (toStringList).
 *     * toString(pattern) / toString(pattern, locale) with null / non-null patterns.
 * - Property Inner Class:
 *     * get(), getField(), getPartial(), getReadablePartial().
 *     * addToCopy, addWrapFieldToCopy, setCopy(int), setCopy(String), setCopy(String, Locale).
 *     * withMaximumValue, withMinimumValue.
 * - Lifecycle & Contracts: equals, hashCode, serialization round-trip.
 * ====================================================================================================
 */
package org.joda.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

public class PartialGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndEmptyState() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
        assertEquals(0, p.getFieldTypes().length);
        assertEquals(0, p.getValues().length);
        assertNull(p.getFormatter());
        assertEquals("[]", p.toString());
        assertEquals("[]", p.toStringList());
    }

    @Test(timeout = 4000)
    public void testChronologyConstructor() {
        Partial pNull = new Partial((Chronology) null);
        assertEquals(ISOChronology.getInstanceUTC(), pNull.getChronology());

        Chronology coptic = CopticChronology.getInstance();
        Partial pCoptic = new Partial(coptic);
        assertEquals(CopticChronology.getInstanceUTC(), pCoptic.getChronology());
    }

    @Test(timeout = 4000)
    public void testSingleFieldConstructor() {
        Partial p = new Partial(DateTimeFieldType.year(), 2024);
        assertEquals(1, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(2024, p.getValue(0));
        assertEquals(2024, p.get(DateTimeFieldType.year()));
        assertTrue(p.isSupported(DateTimeFieldType.year()));
        assertFalse(p.isSupported(DateTimeFieldType.monthOfYear()));
    }

    @Test(timeout = 4000)
    public void testCopyConstructorFromReadablePartial() {
        LocalDate date = new LocalDate(2023, 5, 20);
        Partial p = new Partial(date);
        assertEquals(3, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(2));
        assertEquals(2023, p.getValue(0));
        assertEquals(5, p.getValue(1));
        assertEquals(20, p.getValue(2));
    }

    @Test(timeout = 4000)
    public void testWithChronologyRetainFields() {
        Partial p = new Partial(DateTimeFieldType.year(), 2024);
        assertSame(p, p.withChronologyRetainFields(null));
        assertSame(p, p.withChronologyRetainFields(ISOChronology.getInstanceUTC()));

        Partial copticP = p.withChronologyRetainFields(CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), copticP.getChronology());
        assertEquals(2024, copticP.getValue(0));
    }

    @Test(timeout = 4000)
    public void testWithFieldInsertionOrdering() {
        Partial p = new Partial();
        // Insert hour
        p = p.with(DateTimeFieldType.hourOfDay(), 14);
        // Insert minute after hour
        p = p.with(DateTimeFieldType.minuteOfHour(), 30);
        // Insert year before hour
        p = p.with(DateTimeFieldType.year(), 2025);
        // Insert month between year and hour
        p = p.with(DateTimeFieldType.monthOfYear(), 12);

        assertEquals(4, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        assertEquals(DateTimeFieldType.hourOfDay(), p.getFieldType(2));
        assertEquals(DateTimeFieldType.minuteOfHour(), p.getFieldType(3));
        assertEquals(2025, p.getValue(0));
        assertEquals(12, p.getValue(1));
        assertEquals(14, p.getValue(2));
        assertEquals(30, p.getValue(3));
    }

    @Test(timeout = 4000)
    public void testWithSameFieldExistingValueAndNewValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        assertSame(p, p.with(DateTimeFieldType.year(), 2020));

        Partial updated = p.with(DateTimeFieldType.year(), 2021);
        assertNotSame(p, updated);
        assertEquals(2021, updated.getValue(0));
    }

    @Test(timeout = 4000)
    public void testWithSameUnitDifferentRangeFields() {
        // dayOfYear > dayOfMonth > dayOfWeek
        Partial p = new Partial(DateTimeFieldType.dayOfMonth(), 15);
        Partial pWithDow = p.with(DateTimeFieldType.dayOfWeek(), 3);
        assertEquals(DateTimeFieldType.dayOfMonth(), pWithDow.getFieldType(0));
        assertEquals(DateTimeFieldType.dayOfWeek(), pWithDow.getFieldType(1));

        Partial p2 = new Partial(DateTimeFieldType.dayOfWeek(), 3);
        Partial p2WithDom = p2.with(DateTimeFieldType.dayOfMonth(), 15);
        assertEquals(DateTimeFieldType.dayOfMonth(), p2WithDom.getFieldType(0));
        assertEquals(DateTimeFieldType.dayOfWeek(), p2WithDom.getFieldType(1));
    }

    @Test(timeout = 4000)
    public void testWithout() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 10);
        assertSame(p, p.without(DateTimeFieldType.dayOfMonth()));

        Partial withoutMonth = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(1, withoutMonth.size());
        assertEquals(DateTimeFieldType.year(), withoutMonth.getFieldType(0));

        Partial empty = withoutMonth.without(DateTimeFieldType.year());
        assertEquals(0, empty.size());
    }

    @Test(timeout = 4000)
    public void testWithFieldExisting() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertSame(p, p.withField(DateTimeFieldType.hourOfDay(), 10));

        Partial pChanged = p.withField(DateTimeFieldType.hourOfDay(), 15);
        assertEquals(15, pChanged.get(DateTimeFieldType.hourOfDay()));
    }

    @Test(timeout = 4000)
    public void testWithFieldAddedAndWrapped() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 45);
        assertSame(p, p.withFieldAdded(DurationFieldType.minutes(), 0));
        assertSame(p, p.withFieldAddWrapped(DurationFieldType.minutes(), 0));

        Partial added = p.withFieldAdded(DurationFieldType.minutes(), 5);
        assertEquals(50, added.get(DateTimeFieldType.minuteOfHour()));

        Partial wrapped = p.withFieldAddWrapped(DurationFieldType.minutes(), 20);
        assertEquals(5, wrapped.get(DateTimeFieldType.minuteOfHour()));
    }

    @Test(timeout = 4000)
    public void testPeriodArithmetic() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 20);

        assertSame(p, p.withPeriodAdded(null, 1));
        assertSame(p, p.withPeriodAdded(Period.hours(1), 0));
        assertSame(p, p.plus(null));
        assertSame(p, p.minus(null));

        Period period = new Period(2, 15, 0, 0); // 2 hours, 15 minutes
        Partial plusResult = p.plus(period);
        assertEquals(12, plusResult.get(DateTimeFieldType.hourOfDay()));
        assertEquals(35, plusResult.get(DateTimeFieldType.minuteOfHour()));

        Partial minusResult = plusResult.minus(period);
        assertEquals(10, minusResult.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, minusResult.get(DateTimeFieldType.minuteOfHour()));

        // Period with fields not present in partial (days, seconds) should be ignored
        Period extraPeriod = new Period(0, 0, 0, 5, 1, 0, 30, 0);
        Partial mixedResult = p.plus(extraPeriod);
        assertEquals(11, mixedResult.get(DateTimeFieldType.hourOfDay()));
        assertEquals(20, mixedResult.get(DateTimeFieldType.minuteOfHour()));
    }

    @Test(timeout = 4000)
    public void testIsMatchInstant() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        DateTime dtMatch = new DateTime(2020, 6, 15, 12, 0, 0, 0, DateTimeZone.UTC);
        DateTime dtMismatch = new DateTime(2021, 6, 15, 12, 0, 0, 0, DateTimeZone.UTC);

        assertTrue(p.isMatch(dtMatch));
        assertFalse(p.isMatch(dtMismatch));
    }

    @Test(timeout = 4000)
    public void testIsMatchPartial() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        LocalDate matchDate = new LocalDate(2020, 6, 25);
        LocalDate mismatchDate = new LocalDate(2020, 7, 25);

        assertTrue(p1.isMatch(matchDate));
        assertFalse(p1.isMatch(mismatchDate));
    }

    @Test(timeout = 4000)
    public void testFormattersAndToStringVariants() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 6);
        assertNotNull(p.getFormatter());
        assertEquals("2020-06", p.toString());
        assertEquals("2020/06", p.toString("yyyy/MM"));
        assertEquals("2020-06", p.toString(null));
        assertEquals("2020-06", p.toString(null, Locale.ENGLISH));
        assertEquals("June 2020", p.toString("MMMM yyyy", Locale.ENGLISH));

        // Non-standard ISO combination falls back to toStringList
        Partial nonIso = new Partial(DateTimeFieldType.dayOfWeek(), 3)
                .with(DateTimeFieldType.hourOfDay(), 10);
        assertEquals("[dayOfWeek=3, hourOfDay=10]", nonIso.toString());
        assertEquals("[dayOfWeek=3, hourOfDay=10]", nonIso.toStringList());
    }

    @Test(timeout = 4000)
    public void testPropertyOperations() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());

        assertEquals(6, prop.get());
        assertEquals(DateTimeFieldType.monthOfYear(), prop.getFieldType());
        assertEquals(DateTimeFieldType.monthOfYear().getField(p.getChronology()), prop.getField());
        assertSame(p, prop.getPartial());
        assertSame(p, prop.getReadablePartial());
        assertEquals("6", prop.getAsString());
        assertEquals("June", prop.getAsText(Locale.ENGLISH));
        assertEquals("Jun", prop.getAsShortText(Locale.ENGLISH));

        Partial added = prop.addToCopy(3);
        assertEquals(9, added.get(DateTimeFieldType.monthOfYear()));

        Partial wrapped = prop.addWrapFieldToCopy(8);
        assertEquals(2, wrapped.get(DateTimeFieldType.monthOfYear()));

        Partial setNum = prop.setCopy(11);
        assertEquals(11, setNum.get(DateTimeFieldType.monthOfYear()));

        Partial setText = prop.setCopy("3");
        assertEquals(3, setText.get(DateTimeFieldType.monthOfYear()));

        Partial setTextLocale = prop.setCopy("March", Locale.ENGLISH);
        assertEquals(3, setTextLocale.get(DateTimeFieldType.monthOfYear()));

        Partial max = prop.withMaximumValue();
        assertEquals(12, max.get(DateTimeFieldType.monthOfYear()));

        Partial min = prop.withMinimumValue();
        assertEquals(1, min.get(DateTimeFieldType.monthOfYear()));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyArraysInConstructor() {
        Partial p = new Partial(new DateTimeFieldType[0], new int[0]);
        assertEquals(0, p.size());
        assertEquals(0, p.getFieldTypes().length);
        assertEquals(0, p.getValues().length);
    }

    @Test(timeout = 4000)
    public void testFieldTypesAndValuesCloningDefensiveCopy() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.year()};
        int[] values = new int[] {2020};
        Partial p = new Partial(types, values);

        // Modifying local arrays should not alter Partial state
        types[0] = DateTimeFieldType.monthOfYear();
        values[0] = 5;
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(2020, p.getValue(0));

        // Modifying returned cloned arrays should not alter Partial state
        DateTimeFieldType[] retrievedTypes = p.getFieldTypes();
        int[] retrievedValues = p.getValues();
        retrievedTypes[0] = DateTimeFieldType.hourOfDay();
        retrievedValues[0] = 12;
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(2020, p.getValue(0));
    }

    @Test(timeout = 4000)
    public void testMinMaxFieldValues() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 0)
                .with(DateTimeFieldType.minuteOfHour(), 0);
        assertEquals(0, p.get(DateTimeFieldType.hourOfDay()));
        assertEquals(0, p.get(DateTimeFieldType.minuteOfHour()));

        Partial pMax = p.with(DateTimeFieldType.hourOfDay(), 23)
                        .with(DateTimeFieldType.minuteOfHour(), 59);
        assertEquals(23, pMax.get(DateTimeFieldType.hourOfDay()));
        assertEquals(59, pMax.get(DateTimeFieldType.minuteOfHour()));
    }

    @Test(timeout = 4000)
    public void testHierarchicalRangeOrderingConstructor() {
        // Valid descending order: yearOfEra > yearOfCentury
        DateTimeFieldType[] validTypes = new DateTimeFieldType[] {
                DateTimeFieldType.yearOfEra(),
                DateTimeFieldType.yearOfCentury()
        };
        int[] validValues = new int[] {2025, 25};
        Partial p = new Partial(validTypes, validValues);
        assertEquals(2, p.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets the Defects4J defect in Partial where fields with no range duration
     * type (era and year) fail validation during with() or constructor with:
     * java.lang.IllegalArgumentException: Types array must not contain duplicate: era and year
     */
    @Test(timeout = 4000)
    public void testWith_baseAndArgHaveNoRange() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = test.with(DateTimeFieldType.era(), 1);
        assertEquals(2, result.size());
        assertEquals(DateTimeFieldType.era(), result.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), result.getFieldType(1));
        assertEquals(1, result.getValue(0));
        assertEquals(2005, result.getValue(1));
    }

    @Test(timeout = 4000)
    public void testConstructor_eraAndYearOrdering() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
                DateTimeFieldType.era(),
                DateTimeFieldType.year()
        };
        int[] values = new int[] {1, 2005};
        Partial test = new Partial(types, values);
        assertEquals(2, test.size());
        assertEquals(DateTimeFieldType.era(), test.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), test.getFieldType(1));
        assertEquals(1, test.getValue(0));
        assertEquals(2005, test.getValue(1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSingleFieldConstructorNullType() {
        new Partial((DateTimeFieldType) null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSingleFieldConstructorInvalidValue() {
        new Partial(DateTimeFieldType.monthOfYear(), 13);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorNullTypes() {
        new Partial(null, new int[] {1});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorNullValues() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.year()}, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorLengthMismatch() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.year()}, new int[] {2020, 2021});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorNullElementInTypes() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.year(), null}, new int[] {2020, 1});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorOutOfOrderTypes() {
        // monthOfYear < year
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.monthOfYear(), DateTimeFieldType.year()},
                new int[] {6, 2020});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorDuplicateTypesNoRange() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.year(), DateTimeFieldType.year()},
                new int[] {2020, 2021});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorDuplicateTypesWithRange() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.yearOfCentury(), DateTimeFieldType.yearOfCentury()},
                new int[] {20, 21});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorRangeDurationOutOfOrder() {
        // yearOfCentury (centuries) < yearOfEra (eras)
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.yearOfCentury(), DateTimeFieldType.yearOfEra()},
                new int[] {25, 2025});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayConstructorNullRangeFollowedByNonNullRangeOrderViolation() {
        // year (range=null) cannot be preceded by yearOfCentury if compare indicates inversion
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.yearOfCentury(), DateTimeFieldType.year()},
                new int[] {25, 2025});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCopyConstructorNullReadablePartial() {
        new Partial((ReadablePartial) null);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldTypeNegativeIndex() {
        new Partial(DateTimeFieldType.year(), 2020).getFieldType(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldTypeOverflowIndex() {
        new Partial(DateTimeFieldType.year(), 2020).getFieldType(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueNegativeIndex() {
        new Partial(DateTimeFieldType.year(), 2020).getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueOverflowIndex() {
        new Partial(DateTimeFieldType.year(), 2020).getValue(1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithNullFieldType() {
        new Partial().with(null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldUnsupported() {
        new Partial(DateTimeFieldType.year(), 2020).withField(DateTimeFieldType.monthOfYear(), 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAddedUnsupported() {
        new Partial(DateTimeFieldType.year(), 2020).withFieldAdded(DurationFieldType.months(), 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAddWrappedUnsupported() {
        new Partial(DateTimeFieldType.year(), 2020).withFieldAddWrapped(DurationFieldType.months(), 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyUnsupported() {
        new Partial(DateTimeFieldType.year(), 2020).property(DateTimeFieldType.monthOfYear());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsMatchNullPartial() {
        new Partial(DateTimeFieldType.year(), 2020).isMatch((ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsMatchPartialMissingField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial other = new Partial(DateTimeFieldType.monthOfYear(), 5);
        p.isMatch(other);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 5);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 5);
        Partial pDiffVal = new Partial(DateTimeFieldType.year(), 2021)
                .with(DateTimeFieldType.monthOfYear(), 5);
        Partial pDiffType = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.dayOfMonth(), 5);
        Partial pDiffSize = new Partial(DateTimeFieldType.year(), 2020);
        Partial pDiffChrono = p1.withChronologyRetainFields(BuddhistChronology.getInstanceUTC());

        // Reflexive
        assertTrue(p1.equals(p1));
        // Symmetric
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertEquals(p1.hashCode(), p2.hashCode());

        // Dissimilar
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("NotAPartial"));
        assertFalse(p1.equals(pDiffVal));
        assertFalse(p1.equals(pDiffType));
        assertFalse(p1.equals(pDiffSize));
        assertFalse(p1.equals(pDiffChrono));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        Partial original = new Partial(DateTimeFieldType.year(), 2025)
                .with(DateTimeFieldType.monthOfYear(), 12)
                .with(DateTimeFieldType.dayOfMonth(), 31);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        Partial deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (Partial) ois.readObject();
        }

        assertEquals(original, deserialized);
        assertEquals(original.getChronology(), deserialized.getChronology());
        assertEquals(original.size(), deserialized.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.getFieldType(i), deserialized.getFieldType(i));
            assertEquals(original.getValue(i), deserialized.getValue(i));
        }
    }
}