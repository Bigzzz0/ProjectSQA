package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * === PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS ===
 * - Constructor variants (empty, int, long, ReadableInstant, etc.)
 * - Static factory methods: years(), months(), weeks(), days(), hours(), minutes(), seconds(), millis()
 * - Getter methods: getYears(), getMonths(), getWeeks(), getDays(), getHours(), getMinutes(), getSeconds(), getMillis()
 * - withXxx() methods for all field types
 * - plusXxx() / minusXxx() methods for all field types
 * - plus(ReadablePeriod) / minus(ReadablePeriod)
 * - withPeriodType(), withFields(), withField(), withFieldAdded()
 * - toStandardWeeks(), toStandardDays(), toStandardHours(), toStandardMinutes(), toStandardSeconds()
 * - toStandardDuration()
 * - fieldDifference()
 * - normalizedStandard() and normalizedStandard(PeriodType)
 * 
 * === PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES ===
 * - Zero values for all fields
 * - Integer.MAX_VALUE, Integer.MIN_VALUE for field values
 * - Negative values for fields
 * - Large millis values (near Long.MAX_VALUE boundaries)
 * - Empty period (ZERO constant)
 * - Null arguments for methods accepting ReadablePeriod, PeriodType, Chronology, ReadablePartial
 * - ISOChronology.getInstanceUTC() boundaries
 * 
 * === PARTITION C: DEFECT-TARGETED BRANCH ZONE ===
 * - KNOWN DEFECT: normalizedStandard(PeriodType) throws UnsupportedOperationException 
 *   when period contains months and the target PeriodType does not support months.
 *   The defect lies in the logic where months are normalized and then set via result.withMonths(months)
 *   without checking if the target PeriodType supports months.
 *   Root cause: The method constructs the base period using new Period(millis, type, ISOChronology.getInstanceUTC())
 *   which uses the provided type (potentially omitting months). Then it tries to set months on a period
 *   that doesn't support the months field, causing UnsupportedOperationException.
 *   Fixed behavior should either not attempt to set unsupported fields or use a compatible type.
 * 
 * === PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS ===
 * - IllegalArgumentException for null DurationFieldType in withField()/withFieldAdded()
 * - IllegalArgumentException for null ReadablePartial in fieldDifference()
 * - IllegalArgumentException for mismatched field sizes in fieldDifference()
 * - IllegalArgumentException for overlapping fields in fieldDifference()
 * - IllegalArgumentException for different field types in fieldDifference()
 * - UnsupportedOperationException from checkYearsAndMonths() when converting to standard duration/weeks/days etc.
 * - UnsupportedOperationException from withXxx() when field not supported by PeriodType
 * 
 * === PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY ===
 * - Immutability: verify that withXxx() and plusXxx() return new instances
 * - Period.ZERO is a singleton with zero values
 * - toString() and parse() round-trip
 */
public class PeriodDeepseekTest {

    // ===== PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS =====

    @Test(timeout = 4000)
    public void testEmptyPeriod() {
        Period p = new Period();
        assertEquals(0, p.getYears());
        assertEquals(0, p.getMonths());
        assertEquals(0, p.getWeeks());
        assertEquals(0, p.getDays());
        assertEquals(0, p.getHours());
        assertEquals(0, p.getMinutes());
        assertEquals(0, p.getSeconds());
        assertEquals(0, p.getMillis());
        assertSame(PeriodType.standard(), p.getPeriodType());
    }

    @Test(timeout = 4000)
    public void testPeriodIntIntIntInt() {
        Period p = new Period(1, 2, 3, 4);
        assertEquals(0, p.getYears());
        assertEquals(0, p.getMonths());
        assertEquals(0, p.getWeeks());
        assertEquals(0, p.getDays());
        assertEquals(1, p.getHours());
        assertEquals(2, p.getMinutes());
        assertEquals(3, p.getSeconds());
        assertEquals(4, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testPeriodIntIntIntIntIntIntIntInt() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(3, p.getWeeks());
        assertEquals(4, p.getDays());
        assertEquals(5, p.getHours());
        assertEquals(6, p.getMinutes());
        assertEquals(7, p.getSeconds());
        assertEquals(8, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testPeriodLong() {
        Period p = new Period(100000L);
        assertEquals(0, p.getYears());
        assertEquals(0, p.getMonths());
        assertEquals(0, p.getWeeks());
        assertEquals(0, p.getDays());
        assertEquals(0, p.getHours());
        assertEquals(1, p.getMinutes());
        assertEquals(40, p.getSeconds());
        assertEquals(0, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testStaticFactoryYears() {
        Period p = Period.years(5);
        assertEquals(5, p.getYears());
        assertEquals(0, p.getMonths());
    }

    @Test(timeout = 4000)
    public void testStaticFactoryMonths() {
        Period p = Period.months(3);
        assertEquals(3, p.getMonths());
        assertEquals(0, p.getYears());
    }

    @Test(timeout = 4000)
    public void testStaticFactoryWeeks() {
        Period p = Period.weeks(2);
        assertEquals(2, p.getWeeks());
    }

    @Test(timeout = 4000)
    public void testStaticFactoryDays() {
        Period p = Period.days(7);
        assertEquals(7, p.getDays());
    }

    @Test(timeout = 4000)
    public void testStaticFactoryHours() {
        Period p = Period.hours(10);
        assertEquals(10, p.getHours());
    }

    @Test(timeout = 4000)
    public void testStaticFactoryMinutes() {
        Period p = Period.minutes(30);
        assertEquals(30, p.getMinutes());
    }

    @Test(timeout = 4000)
    public void testStaticFactorySeconds() {
        Period p = Period.seconds(45);
        assertEquals(45, p.getSeconds());
    }

    @Test(timeout = 4000)
    public void testStaticFactoryMillis() {
        Period p = Period.millis(500);
        assertEquals(500, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testWithYears() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withYears(10);
        assertEquals(10, result.getYears());
        assertEquals(2, result.getMonths());
        assertNotSame(base, result);
    }

    @Test(timeout = 4000)
    public void testWithMonths() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withMonths(10);
        assertEquals(10, result.getMonths());
        assertEquals(1, result.getYears());
    }

    @Test(timeout = 4000)
    public void testWithWeeks() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withWeeks(10);
        assertEquals(10, result.getWeeks());
        assertEquals(4, result.getDays());
    }

    @Test(timeout = 4000)
    public void testWithDays() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withDays(10);
        assertEquals(10, result.getDays());
        assertEquals(3, result.getWeeks());
    }

    @Test(timeout = 4000)
    public void testWithHours() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withHours(10);
        assertEquals(10, result.getHours());
    }

    @Test(timeout = 4000)
    public void testWithMinutes() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withMinutes(10);
        assertEquals(10, result.getMinutes());
    }

    @Test(timeout = 4000)
    public void testWithSeconds() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withSeconds(10);
        assertEquals(10, result.getSeconds());
    }

    @Test(timeout = 4000)
    public void testWithMillis() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.withMillis(10);
        assertEquals(10, result.getMillis());
    }

    @Test(timeout = 4000)
    public void testPlusYears() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusYears(3);
        assertEquals(4, result.getYears());
        assertEquals(2, result.getMonths());
    }

    @Test(timeout = 4000)
    public void testPlusMonths() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusMonths(3);
        assertEquals(1, result.getYears());
        assertEquals(5, result.getMonths());
    }

    @Test(timeout = 4000)
    public void testPlusWeeks() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusWeeks(2);
        assertEquals(5, result.getWeeks());
    }

    @Test(timeout = 4000)
    public void testPlusDays() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusDays(2);
        assertEquals(6, result.getDays());
    }

    @Test(timeout = 4000)
    public void testPlusHours() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusHours(2);
        assertEquals(7, result.getHours());
    }

    @Test(timeout = 4000)
    public void testPlusMinutes() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusMinutes(2);
        assertEquals(8, result.getMinutes());
    }

    @Test(timeout = 4000)
    public void testPlusSeconds() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusSeconds(2);
        assertEquals(9, result.getSeconds());
    }

    @Test(timeout = 4000)
    public void testPlusMillis() {
        Period base = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.plusMillis(2);
        assertEquals(10, result.getMillis());
    }

    @Test(timeout = 4000)
    public void testMinusYears() {
        Period base = new Period(5, 2, 3, 4, 5, 6, 7, 8);
        Period result = base.minusYears(2);
        assertEquals(3, result.getYears());
    }

    @Test(timeout = 4000)
    public void testMinusMonths() {
        Period base = new Period(1, 5, 3, 4, 5, 6, 7, 8);
        Period result = base.minusMonths(2);
        assertEquals(3, result.getMonths());
    }

    @Test(timeout = 4000)
    public void testPlusReadablePeriod() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        Period result = p1.plus(p2);
        assertEquals(2, result.getYears());
        assertEquals(3, result.getMonths());
        assertEquals(4, result.getWeeks());
        assertEquals(5, result.getDays());
        assertEquals(6, result.getHours());
        assertEquals(7, result.getMinutes());
        assertEquals(8, result.getSeconds());
        assertEquals(9, result.getMillis());
    }

    @Test(timeout = 4000)
    public void testMinusReadablePeriod() {
        Period p1 = new Period(2, 3, 4, 5, 6, 7, 8, 9);
        Period p2 = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        Period result = p1.minus(p2);
        assertEquals(1, result.getYears());
        assertEquals(2, result.getMonths());
        assertEquals(3, result.getWeeks());
        assertEquals(4, result.getDays());
        assertEquals(5, result.getHours());
        assertEquals(6, result.getMinutes());
        assertEquals(7, result.getSeconds());
        assertEquals(8, result.getMillis());
    }

    @Test(timeout = 4000)
    public void testPlusNullPeriod() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.plus(null));
    }

    @Test(timeout = 4000)
    public void testMinusNullPeriod() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.minus(null));
    }

    @Test(timeout = 4000)
    public void testPlusZeroYears() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.plusYears(0));
    }

    @Test(timeout = 4000)
    public void testPlusZeroMonths() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.plusMonths(0));
    }

    @Test(timeout = 4000)
    public void testWithFieldNullField() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            p.withField(null, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWithFieldAddedNullField() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        try {
            p.withFieldAdded(null, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWithFieldAddedZeroValue() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.withFieldAdded(DurationFieldType.years(), 0));
    }

    @Test(timeout = 4000)
    public void testWithFieldsNull() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.withFields(null));
    }

    @Test(timeout = 4000)
    public void testWithPeriodTypeNull() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.withPeriodType(null);
        assertEquals(PeriodType.standard(), result.getPeriodType());
    }

    @Test(timeout = 4000)
    public void testWithPeriodTypeSame() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.withPeriodType(PeriodType.standard()));
    }

    @Test(timeout = 4000)
    public void testToStandardWeeks() {
        Period p = new Period(0, 0, 1, 7, 0, 0, 0, 0);
        Weeks w = p.toStandardWeeks();
        assertEquals(2, w.getWeeks());
    }

    @Test(timeout = 4000)
    public void testToStandardDays() {
        Period p = new Period(0, 0, 0, 3, 12, 0, 0, 0);
        Days d = p.toStandardDays();
        assertEquals(3, d.getDays());
    }

    @Test(timeout = 4000)
    public void testToStandardHours() {
        Period p = new Period(0, 0, 0, 0, 2, 120, 0, 0);
        Hours h = p.toStandardHours();
        assertEquals(4, h.getHours());
    }

    @Test(timeout = 4000)
    public void testToStandardMinutes() {
        Period p = new Period(0, 0, 0, 0, 0, 2, 180, 0);
        Minutes m = p.toStandardMinutes();
        assertEquals(5, m.getMinutes());
    }

    @Test(timeout = 4000)
    public void testToStandardSeconds() {
        Period p = new Period(0, 0, 0, 0, 0, 0, 2, 3000);
        Seconds s = p.toStandardSeconds();
        assertEquals(5, s.getSeconds());
    }

    @Test(timeout = 4000)
    public void testToStandardDuration() {
        Period p = new Period(0, 0, 0, 0, 0, 1, 0, 0);
        Duration d = p.toStandardDuration();
        assertEquals(60000L, d.getMillis());
    }

    @Test(timeout = 4000)
    public void testFieldDifference() {
        LocalDate start = new LocalDate(2020, 1, 15);
        LocalDate end = new LocalDate(2021, 3, 20);
        Period p = Period.fieldDifference(start, end);
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(0, p.getWeeks());
        assertEquals(5, p.getDays());
    }

    @Test(timeout = 4000)
    public void testMultipliedBy() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.multipliedBy(3);
        assertEquals(3, result.getYears());
        assertEquals(6, result.getMonths());
        assertEquals(9, result.getWeeks());
        assertEquals(12, result.getDays());
        assertEquals(15, result.getHours());
        assertEquals(18, result.getMinutes());
        assertEquals(21, result.getSeconds());
        assertEquals(24, result.getMillis());
    }

    @Test(timeout = 4000)
    public void testMultipliedByOne() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.multipliedBy(1));
    }

    @Test(timeout = 4000)
    public void testMultipliedByZero() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.multipliedBy(0);
        assertEquals(0, result.getYears());
        assertEquals(0, result.getMonths());
        assertEquals(0, result.getWeeks());
        assertEquals(0, result.getDays());
        assertEquals(0, result.getHours());
        assertEquals(0, result.getMinutes());
        assertEquals(0, result.getSeconds());
        assertEquals(0, result.getMillis());
    }

    @Test(timeout = 4000)
    public void testNegated() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.negated();
        assertEquals(-1, result.getYears());
        assertEquals(-2, result.getMonths());
        assertEquals(-3, result.getWeeks());
        assertEquals(-4, result.getDays());
        assertEquals(-5, result.getHours());
        assertEquals(-6, result.getMinutes());
        assertEquals(-7, result.getSeconds());
        assertEquals(-8, result.getMillis());
    }

    // ===== PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES =====

    @Test(timeout = 4000)
    public void testZeroPeriod() {
        assertSame(Period.ZERO, Period.ZERO);
        Period p = new Period();
        assertEquals(0, p.getYears());
        assertEquals(0, p.getMonths());
    }

    @Test(timeout = 4000)
    public void testNegativeValues() {
        Period p = new Period(-1, -2, -3, -4, -5, -6, -7, -8);
        assertEquals(-1, p.getYears());
        assertEquals(-2, p.getMonths());
        assertEquals(-3, p.getWeeks());
        assertEquals(-4, p.getDays());
        assertEquals(-5, p.getHours());
        assertEquals(-6, p.getMinutes());
        assertEquals(-7, p.getSeconds());
        assertEquals(-8, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testIntegerMaxValues() {
        Period p = new Period(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                              Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, p.getYears());
        assertEquals(Integer.MAX_VALUE, p.getMonths());
        assertEquals(Integer.MAX_VALUE, p.getWeeks());
        assertEquals(Integer.MAX_VALUE, p.getDays());
        assertEquals(Integer.MAX_VALUE, p.getHours());
        assertEquals(Integer.MAX_VALUE, p.getMinutes());
        assertEquals(Integer.MAX_VALUE, p.getSeconds());
        assertEquals(Integer.MAX_VALUE, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testIntegerMinValues() {
        Period p = new Period(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE,
                              Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, p.getYears());
        assertEquals(Integer.MIN_VALUE, p.getMonths());
        assertEquals(Integer.MIN_VALUE, p.getWeeks());
        assertEquals(Integer.MIN_VALUE, p.getDays());
        assertEquals(Integer.MIN_VALUE, p.getHours());
        assertEquals(Integer.MIN_VALUE, p.getMinutes());
        assertEquals(Integer.MIN_VALUE, p.getSeconds());
        assertEquals(Integer.MIN_VALUE, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testLargeDurationConstructor() {
        Period p = new Period(Long.MAX_VALUE / 2);
        assertNotNull(p);
    }

    @Test(timeout = 4000)
    public void testZeroDurationConstructor() {
        Period p = new Period(0L);
        assertEquals(0, p.getMillis());
        assertEquals(0, p.getSeconds());
        assertEquals(0, p.getMinutes());
    }

    @Test(timeout = 4000)
    public void testPeriodWithNullChronology() {
        Period p = new Period(1000L, (Chronology) null);
        assertNotNull(p);
    }

    @Test(timeout = 4000)
    public void testPeriodWithNullPeriodType() {
        Period p = new Period(1000L, (PeriodType) null);
        assertNotNull(p);
        assertEquals(PeriodType.standard(), p.getPeriodType());
    }

    @Test(timeout = 4000)
    public void testWithFieldsNullField() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.withFields(null));
    }

    // ===== PARTITION C: DEFECT-TARGETED BRANCH ZONE =====

    /**
     * CRITICAL DEFECT TEST: normalizedStandard(PeriodType) with months and a type that doesn't support months.
     * The defect causes UnsupportedOperationException when trying to call withMonths() on a period
     * that was created with a PeriodType that doesn't include months.
     */
    @Test(timeout = 4000)
    public void testNormalizedStandard_periodType_months1() {
        // Period with months only - target type is yearWeekDayTime (no months!)
        Period p = new Period(0, 10, 0, 0, 0, 0, 0, 0);
        PeriodType type = PeriodType.yearWeekDayTime();
        try {
            Period result = p.normalizedStandard(type);
            // If we get here, the bug is fixed - verify the result
            // Expected: months should be converted to years or handled gracefully
            assertNotNull(result);
            // Since type doesn't support months, resulting period should have 0 months
            // and 0 years if months < 12, or appropriate years if months >= 12
            assertEquals(0, result.getMonths());
            assertEquals(0, result.getYears()); // 10 months < 12, so no years
        } catch (UnsupportedOperationException e) {
            // This is the bug - should not throw!
            fail("normalizedStandard should not throw UnsupportedOperationException for months with yearWeekDayTime type: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNormalizedStandard_periodType_months2() {
        // Period with years and months - target type is weekDayTime (no years, no months!)
        Period p = new Period(1, 3, 0, 0, 0, 0, 0, 0);
        PeriodType type = PeriodType.weekDayTime();
        try {
            Period result = p.normalizedStandard(type);
            assertNotNull(result);
            // Should not have years or months in the result
            assertEquals(0, result.getYears());
            assertEquals(0, result.getMonths());
        } catch (UnsupportedOperationException e) {
            fail("normalizedStandard should not throw UnsupportedOperationException for years/months with weekDayTime type: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNormalizedStandard_periodType_monthsWeeks() {
        // Period with months and weeks - target type is yearWeekDay (no months!)
        Period p = new Period(0, 15, 2, 0, 0, 0, 0, 0);
        PeriodType type = PeriodType.yearWeekDay();
        try {
            Period result = p.normalizedStandard(type);
            assertNotNull(result);
            // 15 months should become 1 year 3 months, but type doesn't support months
            // So result should have 1 year, 0 months, and weeks/days should be preserved
            assertEquals(1, result.getYears());
            assertEquals(0, result.getMonths());
            assertEquals(2, result.getWeeks());
            assertEquals(0, result.getDays());
        } catch (UnsupportedOperationException e) {
            fail("normalizedStandard should not throw UnsupportedOperationException for months+weeks with yearWeekDay type: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNormalizedStandardWithYearsAndMonths() {
        // Normal case: standard type supports everything
        Period p = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period result = p.normalizedStandard();
        assertEquals(2, result.getYears());
        assertEquals(3, result.getMonths());
    }

    @Test(timeout = 4000)
    public void testNormalizedStandardWithTimeOnly() {
        Period p = new Period(0, 0, 0, 0, 25, 90, 0, 0);
        Period result = p.normalizedStandard();
        assertEquals(1, result.getDays());
        assertEquals(1, result.getHours());
        assertEquals(30, result.getMinutes());
    }

    @Test(timeout = 4000)
    public void testNormalizedStandardWithNullType() {
        Period p = new Period(0, 5, 0, 0, 0, 0, 0, 0);
        Period result = p.normalizedStandard(null);
        assertEquals(PeriodType.standard(), result.getPeriodType());
    }

    // ===== PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFieldDifferenceNullStart() {
        Period.fieldDifference(null, new LocalDate(2020, 1, 1));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFieldDifferenceNullEnd() {
        Period.fieldDifference(new LocalDate(2020, 1, 1), null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFieldDifferenceDifferentSize() {
        Period.fieldDifference(new LocalDate(2020, 1, 1), new LocalTime(10, 0));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFieldDifferenceDifferentFields() {
        // Create a partial with different field types
        ReadablePartial start = new LocalDate(2020, 1, 1);
        ReadablePartial end = new MonthDay(2, 15); // MonthDay has monthOfYear and dayOfMonth vs LocalDate's year+month+day
        Period.fieldDifference(start, end);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardWeeksWithMonths() {
        Period p = new Period(0, 1, 0, 0, 0, 0, 0, 0);
        p.toStandardWeeks();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardWeeksWithYears() {
        Period p = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        p.toStandardWeeks();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardDaysWithMonths() {
        Period p = new Period(0, 1, 0, 0, 0, 0, 0, 0);
        p.toStandardDays();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardHoursWithYears() {
        Period p = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        p.toStandardHours();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardMinutesWithMonths() {
        Period p = new Period(0, 1, 0, 0, 0, 0, 0, 0);
        p.toStandardMinutes();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardSecondsWithYears() {
        Period p = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        p.toStandardSeconds();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardDurationWithMonths() {
        Period p = new Period(0, 1, 0, 0, 0, 0, 0, 0);
        p.toStandardDuration();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testToStandardDurationWithYears() {
        Period p = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        p.toStandardDuration();
    }

    @Test(timeout = 4000)
    public void testNormalizedStandardWithMonthsOnly() {
        Period p = new Period(0, 24, 0, 0, 0, 0, 0, 0);
        Period result = p.normalizedStandard();
        assertEquals(2, result.getYears());
        assertEquals(0, result.getMonths());
    }

    // ===== PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY =====

    @Test(timeout = 4000)
    public void testImmutabilityWithYears() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.withYears(10);
        assertEquals(1, p.getYears());
        assertEquals(10, result.getYears());
    }

    @Test(timeout = 4000)
    public void testImmutabilityPlusYears() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.plusYears(5);
        assertEquals(1, p.getYears());
        assertEquals(6, result.getYears());
    }

    @Test(timeout = 4000)
    public void testPeriodToString() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        String str = p.toString();
        assertTrue(str.contains("P"));
        assertTrue(str.contains("Y") || str.contains("M") || str.contains("W") || 
                   str.contains("D") || str.contains("T"));
    }

    @Test(timeout = 4000)
    public void testPeriodParse() {
        Period p = Period.parse("P1Y2M3W4DT5H6M7.008S");
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(3, p.getWeeks());
        assertEquals(4, p.getDays());
        assertEquals(5, p.getHours());
        assertEquals(6, p.getMinutes());
        assertEquals(7, p.getSeconds());
        assertEquals(8, p.getMillis());
    }

    @Test(timeout = 4000)
    public void testZEROSingleton() {
        assertSame(Period.ZERO, Period.ZERO);
        Period p = new Period();
        assertEquals(p, Period.ZERO);
    }

    @Test(timeout = 4000)
    public void testToPeriodReturnsSelf() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(p, p.toPeriod());
    }

    @Test(timeout = 4000)
    public void testConstructorCopyFromPeriod() {
        Period original = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period copy = new Period((Object) original);
        assertEquals(original, copy);
    }

    @Test(timeout = 4000)
    public void testConstructorWithReadableInstant() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        DateTime end = new DateTime(2021, 2, 3, 4, 5, 6, 7);
        Period p = new Period(start, end);
        assertTrue(p.getYears() > 0 || p.getMonths() > 0 || p.getDays() > 0);
    }

    @Test(timeout = 4000)
    public void testWithFieldNonNull() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.withField(DurationFieldType.years(), 10);
        assertEquals(10, result.getYears());
        assertEquals(2, result.getMonths());
    }

    @Test(timeout = 4000)
    public void testWithFieldAddedNonNull() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period result = p.withFieldAdded(DurationFieldType.years(), 5);
        assertEquals(6, result.getYears());
    }

    @Test(timeout = 4000)
    public void testNormalizedStandardWithOnlyTimeFields() {
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 100);
        Period result = p.normalizedStandard();
        assertEquals(0, result.getYears());
        assertEquals(0, result.getMonths());
        assertEquals(0, result.getWeeks());
        assertEquals(0, result.getDays());
        assertEquals(0, result.getHours());
        assertEquals(0, result.getMinutes());
        assertEquals(0, result.getSeconds());
        assertEquals(100, result.getMillis());
    }

    @Test(timeout = 4000)
    public void testNormalizedStandardWithLargeHours() {
        Period p = new Period(0, 0, 0, 0, 100, 0, 0, 0);
        Period result = p.normalizedStandard();
        assertEquals(4, result.getDays());
        assertEquals(4, result.getHours());
    }
}