package org.joda.time.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.Months;
import org.joda.time.MutablePeriod;
import org.joda.time.Partial;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.YearMonth;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Target Defect (Defects4J):
 *    - BaseSingleFieldPeriod.between(ReadablePartial, ReadablePartial, ReadablePeriod)
 *      When computing between two MonthDay instances where one or both fall on Feb 29 (leap day),
 *      the method historically set the partial fields onto reference instant 0L (Jan 1, 1970 UTC).
 *      Because 1970 is not a leap year, setting Feb 29 throws an IllegalFieldValueException.
 *      Target Test: testBetween_ReadablePartial_MonthDay_LeapYear_Defect()
 *
 * 2. Method Coverage Matrix:
 *    - between(ReadableInstant, ReadableInstant, DurationFieldType):
 *      * start == null (throws IAE)
 *      * end == null (throws IAE)
 *      * start > end (negative difference)
 *      * start < end (positive difference)
 *      * start == end (zero difference)
 *      * cross-chronology start/end
 *    - between(ReadablePartial, ReadablePartial, ReadablePeriod):
 *      * start == null (throws IAE)
 *      * end == null (throws IAE)
 *      * start.size() != end.size() (throws IAE)
 *      * mismatched field types at index i (throws IAE)
 *      * non-contiguous partial (e.g., Partial with Year and DayOfMonth, throws IAE)
 *      * normal contiguous partials (LocalDate, YearMonth, MonthDay, LocalTime)
 *    - standardPeriodIn(ReadablePeriod, long):
 *      * period == null (returns 0)
 *      * period with 0 values (skips precise check / addition)
 *      * period with non-precise field (e.g. Months/Years in period -> throws IAE)
 *      * precise fields (Weeks, Days, Hours, Minutes, Seconds, Millis)
 *      * safe calculation boundaries (overflows safely checked)
 *    - Instance Methods & Contracts:
 *      * constructor & getValue() & setValue()
 *      * size() -> returns 1
 *      * getFieldType(int) -> index 0 returns type, index != 0 throws IOOBE
 *      * getValue(int) -> index 0 returns value, index != 0 throws IOOBE
 *      * get(DurationFieldType) -> matching vs non-matching vs null
 *      * isSupported(DurationFieldType) -> matching vs non-matching vs null
 *      * toPeriod() & toMutablePeriod()
 *      * equals(Object) -> identity, non-ReadablePeriod, different PeriodType, different value, equal
 *      * hashCode() -> deterministic, matches contract
 *      * compareTo(BaseSingleFieldPeriod) -> same value, lesser, greater, different class (CCE), null (NPE)
 *      * Serialization round-trip
 */
public class BaseSingleFieldPeriodGeminiTest {

    // Concrete test stub class to exercise protected BaseSingleFieldPeriod methods
    static class SingleTestPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        SingleTestPeriod(int period) {
            super(period);
        }

        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.days();
        }

        @Override
        public PeriodType getPeriodType() {
            return PeriodType.days();
        }

        public void setTestValue(int value) {
            super.setValue(value);
        }
    }

    // Secondary stub with a different DurationFieldType/PeriodType for cross-class testing
    static class AnotherTestPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        AnotherTestPeriod(int period) {
            super(period);
        }

        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.hours();
        }

        @Override
        public PeriodType getPeriodType() {
            return PeriodType.hours();
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where computing period between MonthDay partials involving Feb 29
     * fails with IllegalFieldValueException because 0L (1970, non-leap year) is used as base.
     */
    @Test(timeout = 4000)
    public void testBetween_ReadablePartial_MonthDay_LeapYear_Defect() {
        MonthDay start = new MonthDay(2, 29);
        MonthDay end = new MonthDay(3, 1);

        // This triggers the defect in BaseSingleFieldPeriod.between(...)
        int days = BaseSingleFieldPeriod.between(start, end, Days.ZERO);
        assertEquals("Difference between Feb 29 and Mar 1 should be 1 day", 1, days);

        int months = BaseSingleFieldPeriod.between(start, end, Months.ZERO);
        assertEquals("Difference between Feb 29 and Mar 1 should be 0 months", 0, months);
    }

    @Test(timeout = 4000)
    public void testBetween_ReadablePartial_MonthDay_LeapYearToLeapYear() {
        MonthDay start = new MonthDay(2, 28);
        MonthDay end = new MonthDay(2, 29);

        int days = BaseSingleFieldPeriod.between(start, end, Days.ZERO);
        assertEquals(1, days);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicGettersAndSetters() {
        SingleTestPeriod period = new SingleTestPeriod(42);
        assertEquals(42, period.getValue());
        assertEquals(1, period.size());
        assertEquals(DurationFieldType.days(), period.getFieldType(0));
        assertEquals(42, period.getValue(0));
        assertEquals(42, period.get(DurationFieldType.days()));
        assertTrue(period.isSupported(DurationFieldType.days()));

        period.setTestValue(100);
        assertEquals(100, period.getValue());
        assertEquals(100, period.getValue(0));
    }

    @Test(timeout = 4000)
    public void testGetAndIsSupported_NonMatchingOrNull() {
        SingleTestPeriod period = new SingleTestPeriod(15);
        assertFalse(period.isSupported(DurationFieldType.hours()));
        assertFalse(period.isSupported(null));
        assertEquals(0, period.get(DurationFieldType.hours()));
        assertEquals(0, period.get(null));
    }

    @Test(timeout = 4000)
    public void testToPeriodAndToMutablePeriod() {
        SingleTestPeriod period = new SingleTestPeriod(5);
        Period p = period.toPeriod();
        assertNotNull(p);
        assertEquals(5, p.getDays());
        assertEquals(PeriodType.standard(), p.getPeriodType());

        MutablePeriod mp = period.toMutablePeriod();
        assertNotNull(mp);
        assertEquals(5, mp.getDays());
        assertEquals(PeriodType.standard(), mp.getPeriodType());

        mp.setDays(20);
        assertEquals(20, mp.getDays());
        assertEquals(5, period.getValue());
    }

    @Test(timeout = 4000)
    public void testBetween_ReadableInstant_Normal() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, DateTimeZone.UTC);
        DateTime end = new DateTime(2020, 1, 11, 0, 0, 0, DateTimeZone.UTC);

        int daysBetween = BaseSingleFieldPeriod.between(start, end, DurationFieldType.days());
        assertEquals(10, daysBetween);

        int negDays = BaseSingleFieldPeriod.between(end, start, DurationFieldType.days());
        assertEquals(-10, negDays);

        int zeroDays = BaseSingleFieldPeriod.between(start, start, DurationFieldType.days());
        assertEquals(0, zeroDays);
    }

    @Test(timeout = 4000)
    public void testBetween_ReadableInstant_DifferentChronology() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, BuddhistChronology.getInstanceUTC());
        DateTime end = new DateTime(2020, 1, 5, 0, 0, 0, ISOChronology.getInstanceUTC());

        int days = BaseSingleFieldPeriod.between(start, end, DurationFieldType.days());
        // Buddhist year 2020 is ISO year 1477; start is far before end
        assertTrue(days > 0);
    }

    @Test(timeout = 4000)
    public void testBetween_ReadablePartial_ContiguousTypes() {
        LocalDate startD = new LocalDate(2023, 5, 10);
        LocalDate endD = new LocalDate(2023, 5, 25);
        assertEquals(15, BaseSingleFieldPeriod.between(startD, endD, Days.ZERO));

        YearMonth startYM = new YearMonth(2023, 1);
        YearMonth endYM = new YearMonth(2023, 10);
        assertEquals(9, BaseSingleFieldPeriod.between(startYM, endYM, Months.ZERO));

        LocalTime startT = new LocalTime(10, 0, 0);
        LocalTime endT = new LocalTime(14, 0, 0);
        assertEquals(4, BaseSingleFieldPeriod.between(startT, endT, Hours.ZERO));
    }

    @Test(timeout = 4000)
    public void testStandardPeriodIn_PreciseFields() {
        Period p = new Period(0, 0, 2, 3, 4, 5, 6, 0); // 2 weeks, 3 days, 4 hours, 5 mins, 6 secs
        long expectedMillis = (2L * 7 * 24 * 3600 * 1000)
                + (3L * 24 * 3600 * 1000)
                + (4L * 3600 * 1000)
                + (5L * 60 * 1000)
                + (6L * 1000);

        int seconds = BaseSingleFieldPeriod.standardPeriodIn(p, 1000L);
        assertEquals((int) (expectedMillis / 1000L), seconds);

        int hours = BaseSingleFieldPeriod.standardPeriodIn(p, 3600 * 1000L);
        assertEquals((int) (expectedMillis / (3600 * 1000L)), hours);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardPeriodIn_NullPeriod() {
        int result = BaseSingleFieldPeriod.standardPeriodIn(null, 1000L);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodIn_ZeroPeriod() {
        Period zero = Period.ZERO;
        int result = BaseSingleFieldPeriod.standardPeriodIn(zero, 1000L);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodIn_OnlyZeroValues() {
        MutablePeriod mp = new MutablePeriod();
        mp.setDays(0);
        mp.setHours(0);
        int result = BaseSingleFieldPeriod.standardPeriodIn(mp, 1000L);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodIn_NegativeValues() {
        Period p = Period.hours(-5);
        int hours = BaseSingleFieldPeriod.standardPeriodIn(p, 3600 * 1000L);
        assertEquals(-5, hours);
    }

    @Test(timeout = 4000)
    public void testGetFieldType_InvalidIndex() {
        SingleTestPeriod period = new SingleTestPeriod(1);
        try {
            period.getFieldType(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("1", ex.getMessage());
        }

        try {
            period.getFieldType(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("-1", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testGetValue_InvalidIndex() {
        SingleTestPeriod period = new SingleTestPeriod(1);
        try {
            period.getValue(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("1", ex.getMessage());
        }

        try {
            period.getValue(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("-1", ex.getMessage());
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBetween_ReadableInstant_NullStart() {
        BaseSingleFieldPeriod.between((ReadableInstant) null, new Instant(), DurationFieldType.days());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBetween_ReadableInstant_NullEnd() {
        BaseSingleFieldPeriod.between(new Instant(), (ReadableInstant) null, DurationFieldType.days());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBetween_ReadablePartial_NullStart() {
        BaseSingleFieldPeriod.between((ReadablePartial) null, new LocalDate(), Days.ZERO);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBetween_ReadablePartial_NullEnd() {
        BaseSingleFieldPeriod.between(new LocalDate(), (ReadablePartial) null, Days.ZERO);
    }

    @Test(timeout = 4000)
    public void testBetween_ReadablePartial_MismatchedSize() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2020);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 1);
        try {
            BaseSingleFieldPeriod.between(p1, p2, Days.ZERO);
            fail("Expected IllegalArgumentException for mismatched size");
        } catch (IllegalArgumentException ex) {
            assertEquals("ReadablePartial objects must have the same set of fields", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBetween_ReadablePartial_MismatchedFieldTypes() {
        Partial p1 = new Partial(DateTimeFieldType.dayOfMonth(), 10);
        Partial p2 = new Partial(DateTimeFieldType.monthOfYear(), 10);
        try {
            BaseSingleFieldPeriod.between(p1, p2, Days.ZERO);
            fail("Expected IllegalArgumentException for mismatched field types");
        } catch (IllegalArgumentException ex) {
            assertEquals("ReadablePartial objects must have the same set of fields", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBetween_ReadablePartial_NonContiguous() {
        DateTimeFieldType[] types = new DateTimeFieldType[]{
                DateTimeFieldType.year(),
                DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[]{2020, 15};
        Partial nonContiguous = new Partial(types, values);
        try {
            BaseSingleFieldPeriod.between(nonContiguous, nonContiguous, Days.ZERO);
            fail("Expected IllegalArgumentException for non-contiguous partial");
        } catch (IllegalArgumentException ex) {
            assertEquals("ReadablePartial objects must be contiguous", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testStandardPeriodIn_ImpreciseField_ThrowsException() {
        Period periodWithMonths = Period.months(2);
        try {
            BaseSingleFieldPeriod.standardPeriodIn(periodWithMonths, 1000L);
            fail("Expected IllegalArgumentException for imprecise field");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Cannot convert period to duration as months is not precise"));
        }

        Period periodWithYears = Period.years(1);
        try {
            BaseSingleFieldPeriod.standardPeriodIn(periodWithYears, 1000L);
            fail("Expected IllegalArgumentException for imprecise field");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Cannot convert period to duration as years is not precise"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        SingleTestPeriod p1 = new SingleTestPeriod(10);
        SingleTestPeriod p2 = new SingleTestPeriod(10);
        SingleTestPeriod p3 = new SingleTestPeriod(20);
        AnotherTestPeriod diffType = new AnotherTestPeriod(10);

        // Reflexive
        assertTrue(p1.equals(p1));
        // Symmetric
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertEquals(p1.hashCode(), p2.hashCode());

        // Value inequality
        assertFalse(p1.equals(p3));
        assertFalse(p3.equals(p1));

        // Different PeriodType inequality
        assertFalse(p1.equals(diffType));
        assertFalse(diffType.equals(p1));

        // Non-ReadablePeriod and null checks
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("Not a period"));
    }

    @Test(timeout = 4000)
    public void testCompareTo_Contract() {
        SingleTestPeriod small = new SingleTestPeriod(5);
        SingleTestPeriod equalSmall = new SingleTestPeriod(5);
        SingleTestPeriod large = new SingleTestPeriod(15);

        assertEquals(0, small.compareTo(equalSmall));
        assertEquals(0, equalSmall.compareTo(small));

        assertTrue(small.compareTo(large) < 0);
        assertTrue(large.compareTo(small) > 0);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testCompareTo_Null_ThrowsNPE() {
        SingleTestPeriod period = new SingleTestPeriod(5);
        period.compareTo(null);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testCompareTo_DifferentSubclass_ThrowsCCE() {
        SingleTestPeriod daysPeriod = new SingleTestPeriod(5);
        AnotherTestPeriod hoursPeriod = new AnotherTestPeriod(5);
        daysPeriod.compareTo(hoursPeriod);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        SingleTestPeriod original = new SingleTestPeriod(12345);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SingleTestPeriod deserialized = (SingleTestPeriod) ois.readObject();
        ois.close();

        assertEquals(original.getValue(), deserialized.getValue());
        assertEquals(original, deserialized);
    }
}