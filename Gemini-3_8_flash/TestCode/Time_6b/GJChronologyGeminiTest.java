/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.joda.time.chrono.GJChronology
 *
 * 1. Factory & Cache Branches:
 *    - getInstanceUTC(), getInstance(), getInstance(DateTimeZone), getInstance(zone, cutover, minDays)
 *    - Null zone fallback to default zone; null cutover fallback to DEFAULT_CUTOVER.
 *    - Cache hit & miss logic (matching and non-matching minDaysInFirstWeek and cutoverInstant).
 *    - Non-UTC zone instances wrapped in ZonedChronology.
 *    - Cutover specified as long (millis) matching DEFAULT_CUTOVER.getMillis() vs custom cutover.
 *
 * 2. Conversions & Chronology Assembly:
 *    - withUTC() and withZone() (with same zone, default zone, and different zone).
 *    - assemble(): condition where gregorian.millisOfDay().get(cutover) == 0 (midnight cutover) vs non-midnight cutover.
 *    - assemble(): validation of mismatched minDaysInFirstWeek between Julian and Gregorian chronologies.
 *    - getZone(), getGregorianCutover(), getMinimumDaysInFirstWeek().
 *    - Serialization singleton readResolve().
 *
 * 3. getDateTimeMillis Branches:
 *    - Delegation to base when getBase() != null.
 *    - 4-arg getDateTimeMillis: instant >= cutover, instant < cutover (Julian path), illegal cutover gap exception.
 *    - 7-arg getDateTimeMillis: February 29 leap day fallback handling in Julian leap vs Gregorian non-leap; cutover gap check.
 *
 * 4. CutoverField & ImpreciseCutoverField Inner Logic:
 *    - get(), getAsText(), getAsShortText(), roundFloor(), roundCeiling() crossing / not crossing cutover.
 *    - set(instant, int) and set(instant, String, Locale):
 *      * Gregorian to Julian gap transition with iGapDuration.
 *      * Julian to Gregorian gap transition.
 *      * Verification failure throwing IllegalFieldValueException when value fails to stick.
 *    - add(instant, int/long) in ImpreciseCutoverField:
 *      * instant >= cutover transitions to Julian (instant + iGapDuration < iCutover).
 *      * instant < cutover transitions to Gregorian (instant - iGapDuration >= iCutover).
 *    - add(partial, fieldIndex, values, valueToAdd) contiguous vs non-contiguous partials.
 *    - getDifference() and getDifferenceAsLong() when minuend/subtrahend cross cutovers in both directions.
 *    - getMinimumValue(), getMaximumValue() with instant, partial, and int array overloads.
 *
 * 5. Equality, HashCode, & String Representation:
 *    - equals() symmetry, identity, wrong class, field mismatch.
 *    - hashCode() consistency with equals.
 *    - toString() with default cutover vs custom cutover (at midnight vs midday), non-standard minDaysInFirstWeek.
 *
 * 6. Defect-Targeted Ground Truth (Defects4J Time-1 / Time-26):
 *    - test_plusYears_positiveToZero_crossCutover: Crossing cutover from CE to BCE where year 0 in Gregorian maps to Julian.
 *    - test_plusYears_positiveToNegative_crossCutover: Crossing cutover into negative years with correct astronomical alignment.
 *    - test_plusWeekyears_positiveToNegative_crossCutover & test_plusWeekyears_positiveToZero_crossCutover.
 *    - test_cutoverPreZero: GJChronology instances with cutover before year zero.
 */
package org.joda.time.chrono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.YearMonth;
import org.junit.Test;

import static org.junit.Assert.*;

public class GJChronologyGeminiTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetInstanceFactoriesAndCache() {
        GJChronology defaultUtc = GJChronology.getInstanceUTC();
        assertNotNull(defaultUtc);
        assertEquals(DateTimeZone.UTC, defaultUtc.getZone());
        assertEquals(4, defaultUtc.getMinimumDaysInFirstWeek());
        assertEquals(GJChronology.DEFAULT_CUTOVER, defaultUtc.getGregorianCutover());

        GJChronology cachedUtc = GJChronology.getInstance(DateTimeZone.UTC);
        assertSame(defaultUtc, cachedUtc);

        GJChronology defaultZone = GJChronology.getInstance();
        assertNotNull(defaultZone);
        assertEquals(DateTimeZone.getDefault(), defaultZone.getZone());

        GJChronology paris1 = GJChronology.getInstance(PARIS);
        GJChronology paris2 = GJChronology.getInstance(PARIS, GJChronology.DEFAULT_CUTOVER, 4);
        assertSame(paris1, paris2);

        // Different minDaysInFirstWeek cache miss then hit
        GJChronology parisMinDays5 = GJChronology.getInstance(PARIS, GJChronology.DEFAULT_CUTOVER, 5);
        assertNotSame(paris1, parisMinDays5);
        assertEquals(5, parisMinDays5.getMinimumDaysInFirstWeek());
        assertSame(parisMinDays5, GJChronology.getInstance(PARIS, GJChronology.DEFAULT_CUTOVER, 5));

        // Cutover specified as long millis
        GJChronology viaMillisDefault = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER.getMillis(), 4);
        assertSame(defaultUtc, viaMillisDefault);

        long customCutoverMillis = 0L; // 1970-01-01
        GJChronology customCutoverChrono = GJChronology.getInstance(DateTimeZone.UTC, customCutoverMillis, 4);
        assertEquals(new Instant(customCutoverMillis), customCutoverChrono.getGregorianCutover());
    }

    @Test(timeout = 4000)
    public void testZoneConversions() {
        GJChronology utc = GJChronology.getInstanceUTC();
        assertSame(utc, utc.withUTC());
        assertSame(utc, utc.withZone(DateTimeZone.UTC));

        Chronology parisChrono = utc.withZone(PARIS);
        assertEquals(PARIS, parisChrono.getZone());

        Chronology defaultChrono = utc.withZone(null);
        assertEquals(DateTimeZone.getDefault(), defaultChrono.getZone());
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisFourArgs() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // Gregorian era date (after 1582-10-15)
        long postCutover = chrono.getDateTimeMillis(2020, 1, 15, 500);
        assertEquals(2020, chrono.year().get(postCutover));
        assertEquals(1, chrono.monthOfYear().get(postCutover));
        assertEquals(15, chrono.dayOfMonth().get(postCutover));
        assertEquals(500, chrono.millisOfDay().get(postCutover));

        // Julian era date (before 1582-10-04)
        long preCutover = chrono.getDateTimeMillis(1500, 5, 10, 100);
        assertEquals(1500, chrono.year().get(preCutover));
        assertEquals(5, chrono.monthOfYear().get(preCutover));
        assertEquals(10, chrono.dayOfMonth().get(preCutover));
        assertEquals(100, chrono.millisOfDay().get(preCutover));
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisSevenArgsAndLeapYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        // Julian leap year (1500 was leap in Julian, but not in Gregorian)
        long julianLeap = chrono.getDateTimeMillis(1500, 2, 29, 12, 30, 45, 500);
        assertEquals(1500, chrono.year().get(julianLeap));
        assertEquals(2, chrono.monthOfYear().get(julianLeap));
        assertEquals(29, chrono.dayOfMonth().get(julianLeap));

        // Gregorian leap year (2000 was leap in Gregorian)
        long gregLeap = chrono.getDateTimeMillis(2000, 2, 29, 0, 0, 0, 0);
        assertEquals(2000, chrono.year().get(gregLeap));
        assertEquals(2, chrono.monthOfYear().get(gregLeap));
        assertEquals(29, chrono.dayOfMonth().get(gregLeap));
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisWithZonedBase() {
        GJChronology parisChrono = GJChronology.getInstance(PARIS);
        long millis = parisChrono.getDateTimeMillis(2022, 6, 1, 10, 15, 30, 250);
        assertEquals(2022, parisChrono.year().get(millis));
        assertEquals(6, parisChrono.monthOfYear().get(millis));
        assertEquals(1, parisChrono.dayOfMonth().get(millis));
    }

    @Test(timeout = 4000)
    public void testNonMidnightCutoverAssembly() {
        // Cutover with non-zero millisOfDay to execute cutover fields for time of day
        Instant nonMidnightCutover = new Instant(10000000L + 12345L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, nonMidnightCutover, 4);

        long instant = nonMidnightCutover.getMillis() - 1000L;
        assertNotNull(chrono.millisOfSecond().getAsText(instant));
        assertNotNull(chrono.secondOfDay().getAsShortText(instant));
        assertNotNull(chrono.hourOfDay().getAsText(instant));
        assertNotNull(chrono.halfdayOfDay().getAsText(instant));
        assertNotNull(chrono.clockhourOfDay().getAsText(instant));
        assertNotNull(chrono.clockhourOfHalfday().getAsText(instant));

        long after = nonMidnightCutover.getMillis() + 1000L;
        assertNotNull(chrono.minuteOfHour().getAsText(after));
        assertNotNull(chrono.hourOfHalfday().getAsText(after));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Edge Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCutoverFieldsMinMaxLimits() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        DateTimeField dayOfMonth = chrono.dayOfMonth();
        assertEquals(1, dayOfMonth.getMinimumValue());
        assertEquals(31, dayOfMonth.getMaximumValue());

        // Month of October 1582 has fewer days because of cutover (Oct 4 Julian -> Oct 15 Gregorian)
        long oct1582 = chrono.getDateTimeMillis(1582, 10, 18, 0);
        int maxOct1582 = dayOfMonth.getMaximumValue(oct1582);
        assertEquals(31, maxOct1582);

        long octJulian = chrono.getDateTimeMillis(1582, 10, 2, 0);
        int maxOctJulian = dayOfMonth.getMaximumValue(octJulian);
        assertTrue(maxOctJulian >= 4);

        // DayOfYear limits
        DateTimeField dayOfYear = chrono.dayOfYear();
        assertEquals(1, dayOfYear.getMinimumValue());
        assertTrue(dayOfYear.getMaximumValue() >= 365);
    }

    @Test(timeout = 4000)
    public void testRoundFloorAndCeilingCrossingCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField day = chrono.dayOfMonth();

        long cutover = chrono.getGregorianCutover().getMillis();
        long floored = day.roundFloor(cutover);
        assertTrue(floored <= cutover);

        long ceiling = day.roundCeiling(cutover - 1000L);
        assertTrue(ceiling >= cutover - 1000L);
    }

    @Test(timeout = 4000)
    public void testTextAndShortTextAcrossCutovers() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField era = chrono.era();

        long adInstant = chrono.getDateTimeMillis(2000, 1, 1, 0);
        assertEquals("AD", era.getAsText(adInstant, Locale.ENGLISH));
        assertEquals("AD", era.getAsShortText(adInstant, Locale.ENGLISH));

        long julianInstant = chrono.getDateTimeMillis(100, 1, 1, 0);
        assertEquals("AD", era.getAsText(julianInstant, Locale.ENGLISH));
        assertEquals("AD", era.getAsShortText(julianInstant, Locale.ENGLISH));

        assertEquals("AD", era.getAsText(1, Locale.ENGLISH));
        assertEquals("AD", era.getAsShortText(1, Locale.ENGLISH));

        assertTrue(era.getMaximumTextLength(Locale.ENGLISH) > 0);
        assertTrue(era.getMaximumShortTextLength(Locale.ENGLISH) > 0);
    }

    @Test(timeout = 4000)
    public void testAddAndDifferenceAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        // 1582-10-04 Julian
        long preCutover = chrono.getDateTimeMillis(1582, 10, 4, 0);
        // Add 1 day should leap over gap to 1582-10-15 Gregorian
        long nextDay = chrono.dayOfMonth().add(preCutover, 1);
        assertEquals(1582, chrono.year().get(nextDay));
        assertEquals(10, chrono.monthOfYear().get(nextDay));
        assertEquals(15, chrono.dayOfMonth().get(nextDay));

        // Subtract 1 day from 1582-10-15 should land on 1582-10-04
        long prevDay = chrono.dayOfMonth().add(nextDay, -1);
        assertEquals(preCutover, prevDay);

        // Difference in days across the cutover
        int diffDays = chrono.dayOfMonth().getDifference(nextDay, preCutover);
        assertEquals(1, diffDays);
        assertEquals(1L, chrono.dayOfMonth().getDifferenceAsLong(nextDay, preCutover));

        // Year difference across cutover
        long modern = chrono.getDateTimeMillis(1682, 10, 4, 0);
        int diffYears = chrono.year().getDifference(modern, preCutover);
        assertEquals(100, diffYears);
        assertEquals(100L, chrono.year().getDifferenceAsLong(modern, preCutover));
    }

    @Test(timeout = 4000)
    public void testAddPartialContiguous() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        YearMonth ym = new YearMonth(2004, 2, chrono);
        YearMonth plus4Years = ym.plusYears(4);
        assertEquals(2008, plus4Years.getYear());
        assertEquals(2, plus4Years.getMonthOfYear());

        MonthDay md = new MonthDay(2, 29, chrono);
        int[] vals = chrono.dayOfMonth().add(md, 1, new int[]{2, 29}, 0);
        assertArrayEquals(new int[]{2, 29}, vals);
    }

    @Test(timeout = 4000)
    public void testIsLeapAndLeapAmount() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        long leapJulian = chrono.getDateTimeMillis(1500, 2, 1, 0);
        assertTrue(chrono.year().isLeap(leapJulian));
        assertEquals(1, chrono.year().getLeapAmount(leapJulian));
        assertNotNull(chrono.year().getLeapDurationField());

        long nonLeapGregorian = chrono.getDateTimeMillis(1900, 2, 1, 0);
        assertFalse(chrono.year().isLeap(nonLeapGregorian));
        assertEquals(0, chrono.year().getLeapAmount(nonLeapGregorian));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: test_plusYears_positiveToZero_crossCutover
     * Crossing from CE back to BCE across the cutover where year 0 in Gregorian
     * must translate seamlessly to 1 BCE (-1) in Julian/Astronomical format without
     * throwing IllegalFieldValueException: Value 0 for year is not supported.
     */
    @Test(timeout = 4000)
    public void test_plusYears_positiveToZero_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJChronology.getInstanceUTC());
        LocalDate result = date.plusYears(-2003);
        assertEquals(new LocalDate(-1, 6, 30, GJChronology.getInstanceUTC()), result);
    }

    /**
     * Target Defect: test_plusYears_positiveToNegative_crossCutover
     * 2003 - 2005 years crossing cutover must land on year -2 (2 BCE), not off-by-one -1.
     */
    @Test(timeout = 4000)
    public void test_plusYears_positiveToNegative_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJChronology.getInstanceUTC());
        LocalDate result = date.plusYears(-2005);
        assertEquals(new LocalDate(-2, 6, 30, GJChronology.getInstanceUTC()), result);
    }

    /**
     * Target Defect: test_plusWeekyears_positiveToZero_crossCutover
     * Crossing cutover via weekyears to year 0 must not fail with Value 0 for year is not supported.
     */
    @Test(timeout = 4000)
    public void test_plusWeekyears_positiveToZero_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJChronology.getInstanceUTC());
        LocalDate result = date.plusWeekyears(-2003);
        assertEquals(new LocalDate(-1, 6, 30, GJChronology.getInstanceUTC()), result);
    }

    /**
     * Target Defect: test_plusWeekyears_positiveToNegative_crossCutover
     * Crossing cutover via weekyears from positive to negative should land on -0002-06-30.
     */
    @Test(timeout = 4000)
    public void test_plusWeekyears_positiveToNegative_crossCutover() {
        LocalDate date = new LocalDate(2003, 6, 30, GJChronology.getInstanceUTC());
        LocalDate result = date.plusWeekyears(-2005);
        assertEquals(new LocalDate(-2, 6, 30, GJChronology.getInstanceUTC()), result);
    }

    /**
     * Target Defect: test_cutoverPreZero
     * GJChronology instance configured with a cutover before year zero.
     */
    @Test(timeout = 4000)
    public void test_cutoverPreZero() {
        GJChronology chrono = GJChronology.getInstance(
                DateTimeZone.UTC,
                new LocalDate(-10, 1, 1, ISOChronology.getInstanceUTC()).toDateTimeAtStartOfDay(DateTimeZone.UTC)
        );
        LocalDate date = new LocalDate(-5, 1, 1, chrono);
        assertEquals(-5, date.getYear());
        LocalDate preCutoverDate = new LocalDate(-15, 1, 1, chrono);
        assertEquals(-15, preCutoverDate.getYear());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetDateTimeMillisInCutoverGapFourArgs() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // Dates between Oct 5, 1582 and Oct 14, 1582 do not exist
        chrono.getDateTimeMillis(1582, 10, 5, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetDateTimeMillisInCutoverGapSevenArgs() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // 1582-10-10 does not exist
        chrono.getDateTimeMillis(1582, 10, 10, 12, 0, 0, 0);
    }

    @Test(timeout = 4000, expected = IllegalFieldValueException.class)
    public void testIllegalFeb29InNonLeapGregorian() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // 1900 is not a leap year in Gregorian
        chrono.getDateTimeMillis(1900, 2, 29, 0, 0, 0, 0);
    }

    @Test(timeout = 4000)
    public void testCutoverFieldSetInvalidValueThrows() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long oct1582 = chrono.getDateTimeMillis(1582, 10, 20, 0);
        try {
            // Attempt to set day to 5 (which falls into the illegal cutover gap)
            chrono.dayOfMonth().set(oct1582, 5);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testCutoverFieldSetString() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long oct1582 = chrono.getDateTimeMillis(1582, 10, 20, 0);
        long result = chrono.dayOfMonth().set(oct1582, "25", Locale.ENGLISH);
        assertEquals(25, chrono.dayOfMonth().get(result));

        long preCutover = chrono.getDateTimeMillis(1582, 10, 1, 0);
        long resultJulian = chrono.dayOfMonth().set(preCutover, "2", Locale.ENGLISH);
        assertEquals(2, chrono.dayOfMonth().get(resultJulian));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        GJChronology utc1 = GJChronology.getInstanceUTC();
        GJChronology utc2 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 4);
        GJChronology london = GJChronology.getInstance(LONDON);
        GJChronology customCutover = GJChronology.getInstance(DateTimeZone.UTC, 1000L, 4);
        GJChronology customMdfw = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 5);

        // Reflexive & Symmetric
        assertEquals(utc1, utc1);
        assertEquals(utc1, utc2);
        assertEquals(utc2, utc1);
        assertEquals(utc1.hashCode(), utc2.hashCode());

        // Dissimilar instances
        assertNotEquals(utc1, london);
        assertNotEquals(utc1, customCutover);
        assertNotEquals(utc1, customMdfw);
        assertNotEquals(utc1, null);
        assertNotEquals(utc1, new Object());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        GJChronology utc = GJChronology.getInstanceUTC();
        assertEquals("GJChronology[UTC]", utc.toString());

        GJChronology londonMdfw5 = GJChronology.getInstance(LONDON, GJChronology.DEFAULT_CUTOVER, 5);
        assertEquals("GJChronology[Europe/London,mdfw=5]", londonMdfw5.toString());

        long customDate = utc.getDateTimeMillis(2000, 1, 1, 0);
        GJChronology customCut = GJChronology.getInstance(DateTimeZone.UTC, customDate, 4);
        assertEquals("GJChronology[UTC,cutover=2000-01-01]", customCut.toString());

        long customTime = utc.getDateTimeMillis(2000, 1, 1, 12, 0, 0, 0);
        GJChronology customCutTime = GJChronology.getInstance(DateTimeZone.UTC, customTime, 4);
        assertEquals("GJChronology[UTC,cutover=2000-01-01T12:00:00.000Z]", customCutTime.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        GJChronology original = GJChronology.getInstance(PARIS, 123456789L, 4);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        GJChronology deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (GJChronology) ois.readObject();
        }

        assertSame(original, deserialized);
    }
}