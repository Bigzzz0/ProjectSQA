package org.joda.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundaries:
 * 1. normalizedStandard(PeriodType):
 *    - Branch: years != 0 || months != 0 vs both == 0
 *    - Branch: years != 0 when result PeriodType does or does not support years (Defect Joda-Time / Defects4J)
 *    - Branch: months != 0 when result PeriodType supports or omits months
 *    - Arithmetic bounds and day/time field rollups into millis
 * 2. parse(String) & parse(String, PeriodFormatter):
 *    - Standard ISO-8601 strings and custom formatters
 * 3. Static Field Difference & Factories:
 *    - fieldDifference(ReadablePartial, ReadablePartial): null validation, unequal size, mismatched field types,
 *      overlapping adjacent types, negative and positive delta calculation
 *    - Individual field factories (years, months, weeks, days, hours, minutes, seconds, millis)
 * 4. Constructors Coverage:
 *    - Default, 4-int, 8-int, 8-int with PeriodType
 *    - millisecond duration: (long), (long, PeriodType), (long, Chronology), (long, PeriodType, Chronology)
 *    - millisecond intervals: (long, long), (long, long, PeriodType), (long, long, Chronology), (long, long, PeriodType, Chronology)
 *    - Instant intervals: (ReadableInstant, ReadableInstant), (..., PeriodType)
 *    - Partial intervals: (ReadablePartial, ReadablePartial), (..., PeriodType)
 *    - Instant + Duration & Duration + Instant: (ReadableInstant, ReadableDuration, [PeriodType])
 *    - Object converter constructors: (Object), (Object, PeriodType), (Object, Chronology), (Object, PeriodType, Chronology)
 * 5. Withers and In-place Mutations:
 *    - withPeriodType (same type returns this, different type creates new, unsupported throws)
 *    - withFields (null returns this, non-null merges fields)
 *    - withField & withFieldAdded (null check, value == 0 optimization)
 *    - withXxx individual field modifiers
 * 6. Plus and Minus operations:
 *    - plus(ReadablePeriod) & minus(ReadablePeriod) (null checks return this, additions across all 8 fields)
 *    - plusXxx & minusXxx for all 8 index types with 0 short-circuit check
 * 7. Scalar Multiplications and Conversions:
 *    - multipliedBy: this == ZERO short-circuit, scalar == 1 short-circuit, negative scalar, overflow check
 *    - negated()
 *    - toStandardWeeks, toStandardDays, toStandardHours, toStandardMinutes, toStandardSeconds, toStandardDuration
 *    - checkYearsAndMonths exception branches when years != 0 or months != 0
 * 8. Defect-Targeted Branch Zone (Defects4J):
 *    - testNormalizedStandard_periodType_months1: Normalizing a Period with months into PeriodType.months()
 *    - testNormalizedStandard_periodType_months2: Period with >= 12 months normalized into PeriodType.months()
 *    - testNormalizedStandard_periodType_monthsWeeks: Period normalized into PeriodType.monthsWeeks()
 */
public class PeriodGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectNormalizedStandard_periodType_months1() {
        // Exposes defect where normalizedStandard attempts to set years on a PeriodType
        // that does not support years (e.g., PeriodType.months()).
        Period test = new Period(0, 6, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.months());
        assertEquals(6, result.getMonths());
        assertEquals(0, result.getYears());
    }

    @Test(timeout = 4000)
    public void testDefectNormalizedStandard_periodType_months2() {
        // When months >= 12 and target PeriodType is months-only, years cannot be added
        Period test = new Period(0, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.months());
        assertEquals(15, result.getMonths());
        assertEquals(0, result.getYears());
    }

    @Test(timeout = 4000)
    public void testDefectNormalizedStandard_periodType_monthsWeeks() {
        // Target PeriodType supporting months and weeks, but not years
        PeriodType type = PeriodType.forFields(new DurationFieldType[]{
            DurationFieldType.months(),
            DurationFieldType.weeks(),
            DurationFieldType.days()
        });
        Period test = new Period(0, 25, 2, 3, 0, 0, 0, 0);
        Period result = test.normalizedStandard(type);
        assertEquals(25, result.getMonths());
        assertEquals(2, result.getWeeks());
        assertEquals(3, result.getDays());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStaticFactoriesAndGetters() {
        Period pYears = Period.years(5);
        assertEquals(5, pYears.getYears());
        assertEquals(0, pYears.getMonths());

        Period pMonths = Period.months(4);
        assertEquals(4, pMonths.getMonths());

        Period pWeeks = Period.weeks(3);
        assertEquals(3, pWeeks.getWeeks());

        Period pDays = Period.days(2);
        assertEquals(2, pDays.getDays());

        Period pHours = Period.hours(10);
        assertEquals(10, pHours.getHours());

        Period pMinutes = Period.minutes(30);
        assertEquals(30, pMinutes.getMinutes());

        Period pSeconds = Period.seconds(45);
        assertEquals(45, pSeconds.getSeconds());

        Period pMillis = Period.millis(500);
        assertEquals(500, pMillis.getMillis());

        assertSame(pYears, pYears.toPeriod());
    }

    @Test(timeout = 4000)
    public void testParseMethods() {
        Period parsed = Period.parse("P1Y2M3W4DT5H6M7.008S");
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
        assertEquals(3, parsed.getWeeks());
        assertEquals(4, parsed.getDays());
        assertEquals(5, parsed.getHours());
        assertEquals(6, parsed.getMinutes());
        assertEquals(7, parsed.getSeconds());
        assertEquals(8, parsed.getMillis());

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();
        Period customParsed = Period.parse("12h34m", formatter);
        assertEquals(12, customParsed.getHours());
        assertEquals(34, customParsed.getMinutes());
    }

    @Test(timeout = 4000)
    public void testFieldDifferenceCalculation() {
        LocalDate start = new LocalDate(2012, 3, 10);
        LocalDate end = new LocalDate(2015, 6, 25);
        Period diff = Period.fieldDifference(start, end);

        assertEquals(3, diff.getYears());
        assertEquals(3, diff.getMonths());
        assertEquals(15, diff.getDays());
        assertEquals(PeriodType.forFields(new DurationFieldType[]{
            DurationFieldType.years(),
            DurationFieldType.months(),
            DurationFieldType.days()
        }), diff.getPeriodType());
    }

    @Test(timeout = 4000)
    public void testWithers() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(10, base.withYears(10).getYears());
        assertEquals(10, base.withMonths(10).getMonths());
        assertEquals(10, base.withWeeks(10).getWeeks());
        assertEquals(10, base.withDays(10).getDays());
        assertEquals(10, base.withHours(10).getHours());
        assertEquals(10, base.withMinutes(10).getMinutes());
        assertEquals(10, base.withSeconds(10).getSeconds());
        assertEquals(10, base.withMillis(10).getMillis());

        // withField and withFieldAdded
        Period modified = base.withField(DurationFieldType.hours(), 20);
        assertEquals(20, modified.getHours());
        Period added = base.withFieldAdded(DurationFieldType.hours(), 5);
        assertEquals(10, added.getHours());

        assertSame(base, base.withFieldAdded(DurationFieldType.hours(), 0));

        // withPeriodType
        Period sameType = base.withPeriodType(PeriodType.standard());
        assertSame(base, sameType);

        Period timeOnly = new Period(0, 0, 0, 0, 1, 2, 3, 4).withPeriodType(PeriodType.time());
        assertEquals(PeriodType.time(), timeOnly.getPeriodType());

        // withFields
        Period override = new Period(0, 0, 0, 0, 99, 99, 0, 0);
        Period merged = base.withFields(override);
        assertEquals(99, merged.getHours());
        assertEquals(99, merged.getMinutes());
        assertEquals( base.getYears(), merged.getYears() );
        assertSame(base, base.withFields(null));
    }

    @Test(timeout = 4000)
    public void testPlusAndMinusReadablePeriod() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(2, 3, 4, 5, 6, 7, 8, 9);

        Period sum = p1.plus(p2);
        assertEquals(3, sum.getYears());
        assertEquals(5, sum.getMonths());
        assertEquals(7, sum.getWeeks());
        assertEquals(9, sum.getDays());
        assertEquals(11, sum.getHours());
        assertEquals(13, sum.getMinutes());
        assertEquals(15, sum.getSeconds());
        assertEquals(17, sum.getMillis());

        assertSame(p1, p1.plus(null));

        Period diff = sum.minus(p2);
        assertEquals(p1, diff);
        assertSame(p1, p1.minus(null));
    }

    @Test(timeout = 4000)
    public void testPlusMinusIndividualFields() {
        Period p = new Period();
        // check zero short-circuits
        assertSame(p, p.plusYears(0));
        assertSame(p, p.plusMonths(0));
        assertSame(p, p.plusWeeks(0));
        assertSame(p, p.plusDays(0));
        assertSame(p, p.plusHours(0));
        assertSame(p, p.plusMinutes(0));
        assertSame(p, p.plusSeconds(0));
        assertSame(p, p.plusMillis(0));

        assertSame(p, p.minusYears(0));
        assertSame(p, p.minusMonths(0));
        assertSame(p, p.minusWeeks(0));
        assertSame(p, p.minusDays(0));
        assertSame(p, p.minusHours(0));
        assertSame(p, p.minusMinutes(0));
        assertSame(p, p.minusSeconds(0));
        assertSame(p, p.minusMillis(0));

        // plus non-zero
        assertEquals(5, p.plusYears(5).getYears());
        assertEquals(5, p.plusMonths(5).getMonths());
        assertEquals(5, p.plusWeeks(5).getWeeks());
        assertEquals(5, p.plusDays(5).getDays());
        assertEquals(5, p.plusHours(5).getHours());
        assertEquals(5, p.plusMinutes(5).getMinutes());
        assertEquals(5, p.plusSeconds(5).getSeconds());
        assertEquals(5, p.plusMillis(5).getMillis());

        // minus non-zero
        assertEquals(-5, p.minusYears(5).getYears());
        assertEquals(-5, p.minusMonths(5).getMonths());
        assertEquals(-5, p.minusWeeks(5).getWeeks());
        assertEquals(-5, p.minusDays(5).getDays());
        assertEquals(-5, p.minusHours(5).getHours());
        assertEquals(-5, p.minusMinutes(5).getMinutes());
        assertEquals(-5, p.minusSeconds(5).getSeconds());
        assertEquals(-5, p.minusMillis(5).getMillis());
    }

    @Test(timeout = 4000)
    public void testMultipliedByAndNegated() {
        assertSame(Period.ZERO, Period.ZERO.multipliedBy(5));

        Period p = new Period(1, -2, 3, -4, 5, -6, 7, -8);
        assertSame(p, p.multipliedBy(1));

        Period mult = p.multipliedBy(2);
        assertEquals(2, mult.getYears());
        assertEquals(-4, mult.getMonths());
        assertEquals(6, mult.getWeeks());
        assertEquals(-8, mult.getDays());
        assertEquals(10, mult.getHours());
        assertEquals(-12, mult.getMinutes());
        assertEquals(14, mult.getSeconds());
        assertEquals(-16, mult.getMillis());

        Period neg = p.negated();
        assertEquals(-1, neg.getYears());
        assertEquals(2, neg.getMonths());
        assertEquals(-3, neg.getWeeks());
        assertEquals(4, neg.getDays());
        assertEquals(-5, neg.getHours());
        assertEquals(6, neg.getMinutes());
        assertEquals(-7, neg.getSeconds());
        assertEquals(8, neg.getMillis());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Standard Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStandardConversions() {
        Period p = new Period(0, 0, 2, 3, 4, 5, 6, 700);

        // Standard Weeks: 2 weeks + (3d*86400s + 4h*3600s + 5m*60s + 6s + 0.7s) / 604800s
        Weeks weeks = p.toStandardWeeks();
        assertEquals(2, weeks.getWeeks());

        // Standard Days: 2*7 + 3 = 17 days
        Days days = p.toStandardDays();
        assertEquals(17, days.getDays());

        // Standard Hours: 17*24 + 4 = 412 hours
        Hours hours = p.toStandardHours();
        assertEquals(412, hours.getHours());

        // Standard Minutes: 412*60 + 5 = 24725 minutes
        Minutes minutes = p.toStandardMinutes();
        assertEquals(24725, minutes.getMinutes());

        // Standard Seconds: 24725*60 + 6 = 1483506 seconds
        Seconds seconds = p.toStandardSeconds();
        assertEquals(1483506, seconds.getSeconds());

        // Standard Duration
        Duration duration = p.toStandardDuration();
        long expectedMillis = (1483506L * 1000L) + 700L;
        assertEquals(expectedMillis, duration.getMillis());
    }

    @Test(timeout = 4000)
    public void testNormalizedStandard() {
        // Standard normalisation rolls days up into weeks, minutes into hours, etc.
        Period unnorm = new Period(1, 15, 0, 8, 25, 70, 65, 1200);
        Period norm = unnorm.normalizedStandard();

        assertEquals(2, norm.getYears());
        assertEquals(3, norm.getMonths());
        // (8d*86400 + 25h*3600 + 70m*60 + 65s + 1.2s) => total days/weeks/hours/mins/secs/millis
        assertTrue(norm.getWeeks() >= 1);
        assertEquals(PeriodType.standard(), norm.getPeriodType());

        // Test normalisation with null type defaults to standard
        Period normDefault = unnorm.normalizedStandard(null);
        assertEquals(norm, normDefault);

        // Period with 0 years and 0 months
        Period timePeriod = new Period(0, 0, 0, 0, 0, 125, 0, 0);
        Period normTime = timePeriod.normalizedStandard();
        assertEquals(2, normTime.getHours());
        assertEquals(5, normTime.getMinutes());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFieldDifferenceNullStart() {
        Period.fieldDifference(null, new LocalTime());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFieldDifferenceNullEnd() {
        Period.fieldDifference(new LocalTime(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFieldDifferenceMismatchedSizes() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalTime end = new LocalTime(12, 0);
        Period.fieldDifference(start, end);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFieldDifferenceMismatchedFieldTypes() {
        Partial p1 = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial p2 = new Partial(DateTimeFieldType.minuteOfHour(), 10);
        Period.fieldDifference(p1, p2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFieldDifferenceOverlappingFields() {
        DateTimeFieldType[] types = new DateTimeFieldType[]{
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfMonth()
        };
        Partial p1 = new Partial(types, new int[]{1, 1});
        Partial p2 = new Partial(types, new int[]{2, 2});
        Period.fieldDifference(p1, p2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldNull() {
        Period.ZERO.withField(null, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAddedNull() {
        Period.ZERO.withFieldAdded(null, 5);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToStandardWeeksThrowsWhenYearsPresent() {
        Period.years(1).toStandardWeeks();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToStandardDaysThrowsWhenMonthsPresent() {
        Period.months(1).toStandardDays();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToStandardHoursThrowsWhenYearsPresent() {
        Period.years(1).toStandardHours();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToStandardMinutesThrowsWhenMonthsPresent() {
        Period.months(1).toStandardMinutes();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToStandardSecondsThrowsWhenYearsPresent() {
        Period.years(1).toStandardSeconds();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToStandardDurationThrowsWhenMonthsPresent() {
        Period.months(1).toStandardDuration();
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMultipliedByOverflow() {
        Period p = Period.years(Integer.MAX_VALUE);
        p.multipliedBy(2);
    }

    // =========================================================================
    // Partition E: Constructors Lifecycle & Comprehensive Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructors() {
        Period pDefault = new Period();
        assertEquals(0, pDefault.getYears());

        Period p4Int = new Period(1, 2, 3, 4);
        assertEquals(1, p4Int.getHours());
        assertEquals(2, p4Int.getMinutes());
        assertEquals(3, p4Int.getSeconds());
        assertEquals(4, p4Int.getMillis());

        Period p8Int = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, p8Int.getYears());
        assertEquals(8, p8Int.getMillis());

        Period p8IntType = new Period(0, 0, 0, 0, 5, 6, 7, 8, PeriodType.time());
        assertEquals(PeriodType.time(), p8IntType.getPeriodType());

        // duration constructors
        Period pDur1 = new Period(5000L);
        assertEquals(5, pDur1.getSeconds());

        Period pDur2 = new Period(5000L, PeriodType.standard());
        assertEquals(5, pDur2.getSeconds());

        Period pDur3 = new Period(5000L, ISOChronology.getInstanceUTC());
        assertEquals(5, pDur3.getSeconds());

        Period pDur4 = new Period(5000L, PeriodType.seconds(), ISOChronology.getInstanceUTC());
        assertEquals(5, pDur4.getSeconds());
        assertEquals(PeriodType.seconds(), pDur4.getPeriodType());

        // interval long millisecond constructors
        Period pInt1 = new Period(1000L, 5000L);
        assertEquals(4, pInt1.getSeconds());

        Period pInt2 = new Period(1000L, 5000L, PeriodType.standard());
        assertEquals(4, pInt2.getSeconds());

        Period pInt3 = new Period(1000L, 5000L, CopticChronology.getInstanceUTC());
        assertEquals(4, pInt3.getSeconds());

        Period pInt4 = new Period(1000L, 5000L, PeriodType.seconds(), BuddhistChronology.getInstanceUTC());
        assertEquals(4, pInt4.getSeconds());

        // ReadableInstant constructors
        Instant inst1 = new Instant(10000L);
        Instant inst2 = new Instant(25000L);
        Period pInst1 = new Period(inst1, inst2);
        assertEquals(15, pInst1.getSeconds());

        Period pInst2 = new Period(inst1, inst2, PeriodType.seconds());
        assertEquals(15, pInst2.getSeconds());

        // ReadablePartial constructors
        LocalDate ld1 = new LocalDate(2020, 1, 1);
        LocalDate ld2 = new LocalDate(2020, 1, 15);
        Period pPart1 = new Period(ld1, ld2);
        assertEquals(2, pPart1.getWeeks());

        Period pPart2 = new Period(ld1, ld2, PeriodType.days());
        assertEquals(14, pPart2.getDays());

        // Instant and Duration constructors
        Duration dur = new Duration(60000L);
        Period pInstDur1 = new Period(inst1, dur);
        assertEquals(1, pInstDur1.getMinutes());

        Period pInstDur2 = new Period(inst1, dur, PeriodType.minutes());
        assertEquals(1, pInstDur2.getMinutes());

        Period pDurInst1 = new Period(dur, inst2);
        assertEquals(1, pDurInst1.getMinutes());

        Period pDurInst2 = new Period(dur, inst2, PeriodType.minutes());
        assertEquals(1, pDurInst2.getMinutes());

        // Object converters
        Period pObj1 = new Period("PT15M");
        assertEquals(15, pObj1.getMinutes());

        Period pObj2 = new Period("PT15M", PeriodType.time());
        assertEquals(15, pObj2.getMinutes());

        Period pObj3 = new Period("PT15M", ISOChronology.getInstanceUTC());
        assertEquals(15, pObj3.getMinutes());

        Period pObj4 = new Period("PT15M", PeriodType.time(), ISOChronology.getInstanceUTC());
        assertEquals(15, pObj4.getMinutes());
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        Period original = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Period deserialized = (Period) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualityAndImmutability() {
        Period p1 = new Period(1, 2, 0, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 2, 0, 4, 5, 6, 7, 8);
        Period pDiff = new Period(1, 2, 0, 4, 5, 6, 7, 9);

        assertEquals(p1, p2);
        assertNotEquals(p1, pDiff);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "NotAPeriod");
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}