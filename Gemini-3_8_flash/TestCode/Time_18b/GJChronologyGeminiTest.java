/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decisions & Predicates:
 * 1. Factory & Caching (getInstance, getInstanceUTC, withZone, withUTC):
 *    - null zone -> default zone branch
 *    - null gregorianCutover -> DEFAULT_CUTOVER branch
 *    - Cache hits: zone exists + matching minDaysInFirstWeek + matching cutoverInstant
 *    - Non-UTC base creation -> ZonedChronology wrapping
 *    - Long cutover matching DEFAULT_CUTOVER.getMillis() -> null cutoverInstant
 * 2. Cutover Gap & Validity (getDateTimeMillis):
 *    - Valid Gregorian instant >= iCutoverMillis
 *    - Instant < iCutoverMillis falling back to Julian
 *    - Cutover gap condition (Julian instant >= iCutoverMillis) -> throws IllegalArgumentException
 * 3. Cutover Construction & Leap Rules (DEFECT-TARGET: testLeapYearRulesConstruction):
 *    - Julian leap day cutovers (e.g. Feb 29 on year 1500 Julian where 1500 is NOT Gregorian leap year)
 *    - assemble() -> julianToGregorianByYear() calculation of iGapDuration
 * 4. CutoverField & ImpreciseCutoverField transitions:
 *    - Boundary evaluation: instant < iCutover vs instant >= iCutover
 *    - set() crossing forward: Julian -> Gregorian (instant >= iCutover, verify gap adjustment & stuck check)
 *    - set() crossing backward: Gregorian -> Julian (instant < iCutover, verify gap adjustment & stuck check)
 *    - add() crossing forward and backward across gap duration
 *    - getDifference() & getDifferenceAsLong(): minuend and subtrahend on same side vs opposing sides
 *    - getMinimumValue(instant) / getMaximumValue(instant) near cutover threshold
 *    - add(ReadablePartial, int, int[], int): valueToAdd == 0, contiguous vs non-contiguous partial
 * 5. Formatting & Contract Integrity:
 *    - toString() with default cutover vs non-default cutover (with and without time of day)
 *    - toString() with minDaysInFirstWeek != 4
 *    - equals() & hashCode() consistency
 *    - Serialization (readResolve singleton resolution)
 */
package org.joda.time.chrono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.YearMonth;
import org.junit.Test;

import static org.junit.Assert.*;

public class GJChronologyGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryAndGettersUTC() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(4, chrono.getMinimumDaysInFirstWeek());
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono.getGregorianCutover());
        assertSame(chrono, chrono.withUTC());
    }

    @Test(timeout = 4000)
    public void testFactoryAndGettersDefaultZone() {
        GJChronology chrono = GJChronology.getInstance();
        assertEquals(DateTimeZone.getDefault(), chrono.getZone());
        assertEquals(4, chrono.getMinimumDaysInFirstWeek());
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono.getGregorianCutover());
    }

    @Test(timeout = 4000)
    public void testWithZoneTransitions() {
        GJChronology utc = GJChronology.getInstanceUTC();
        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");
        Chronology zoned = utc.withZone(tokyo);

        assertEquals(tokyo, zoned.getZone());
        assertSame(zoned, zoned.withZone(tokyo));
        assertSame(utc, zoned.withZone(DateTimeZone.UTC));

        Chronology defaultZoned = zoned.withZone(null);
        assertEquals(DateTimeZone.getDefault(), defaultZoned.getZone());
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillis4FieldsGregorianAndJulian() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // Gregorian era date (after 1582-10-15)
        long millis2000 = chrono.getDateTimeMillis(2000, 1, 1, 123);
        assertEquals(2000, chrono.year().get(millis2000));
        assertEquals(1, chrono.monthOfYear().get(millis2000));
        assertEquals(1, chrono.dayOfMonth().get(millis2000));
        assertEquals(123, chrono.millisOfDay().get(millis2000));

        // Julian era date (before 1582-10-04)
        long millis1500 = chrono.getDateTimeMillis(1500, 5, 10, 500);
        assertEquals(1500, chrono.year().get(millis1500));
        assertEquals(5, chrono.monthOfYear().get(millis1500));
        assertEquals(10, chrono.dayOfMonth().get(millis1500));
        assertEquals(500, chrono.millisOfDay().get(millis1500));
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillis7FieldsGregorianAndJulian() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1995, 6, 15, 14, 30, 45, 250);
        assertEquals(1995, chrono.year().get(millis));
        assertEquals(6, chrono.monthOfYear().get(millis));
        assertEquals(15, chrono.dayOfMonth().get(millis));
        assertEquals(14, chrono.hourOfDay().get(millis));
        assertEquals(30, chrono.minuteOfHour().get(millis));
        assertEquals(45, chrono.secondOfMinute().get(millis));
        assertEquals(250, chrono.millisOfSecond().get(millis));

        long julianMillis = chrono.getDateTimeMillis(1000, 2, 10, 10, 20, 30, 40);
        assertEquals(1000, chrono.year().get(julianMillis));
        assertEquals(2, chrono.monthOfYear().get(julianMillis));
        assertEquals(10, chrono.dayOfMonth().get(julianMillis));
    }

    @Test(timeout = 4000)
    public void testZonedChronologyGetDateTimeMillis() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        GJChronology chrono = GJChronology.getInstance(paris);
        long millis4 = chrono.getDateTimeMillis(2010, 5, 1, 100);
        assertTrue(millis4 > 0);

        long millis7 = chrono.getDateTimeMillis(2010, 5, 1, 12, 0, 0, 0);
        assertTrue(millis7 > 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryBoundaryCaches() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        Instant cutover = new Instant(0L);

        GJChronology c1 = GJChronology.getInstance(zone, cutover, 5);
        GJChronology c2 = GJChronology.getInstance(zone, cutover, 5);
        assertSame(c1, c2);

        GJChronology c3 = GJChronology.getInstance(zone, cutover, 6);
        assertNotSame(c1, c3);

        GJChronology c4 = GJChronology.getInstance(zone, (ReadableInstant) null);
        assertEquals(GJChronology.DEFAULT_CUTOVER, c4.getGregorianCutover());

        GJChronology c5 = GJChronology.getInstance(zone, GJChronology.DEFAULT_CUTOVER.getMillis(), 4);
        assertSame(c4, c5);

        GJChronology c6 = GJChronology.getInstance(zone, 123456789L, 4);
        assertEquals(123456789L, c6.getGregorianCutover().getMillis());
    }

    @Test(timeout = 4000)
    public void testCutoverFieldAddAndDifferencesAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayField = chrono.dayOfMonth();

        // 1582-10-04 is Julian, 1582-10-15 is Gregorian
        DateTime dtJulian = new DateTime(1582, 10, 4, 0, 0, 0, 0, chrono);
        DateTime dtGregorian = dtJulian.plusDays(1);
        assertEquals(1582, dtGregorian.getYear());
        assertEquals(10, dtGregorian.getMonthOfYear());
        assertEquals(15, dtGregorian.getDayOfMonth());

        assertEquals(1, dayField.getDifference(dtGregorian.getMillis(), dtJulian.getMillis()));
        assertEquals(1L, dayField.getDifferenceAsLong(dtGregorian.getMillis(), dtJulian.getMillis()));

        // Subtract across cutover
        DateTime dtBack = dtGregorian.minusDays(1);
        assertEquals(dtJulian, dtBack);

        assertEquals(-1, dayField.getDifference(dtJulian.getMillis(), dtGregorian.getMillis()));
        assertEquals(-1L, dayField.getDifferenceAsLong(dtJulian.getMillis(), dtGregorian.getMillis()));
    }

    @Test(timeout = 4000)
    public void testCutoverFieldTextAndShortText() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField era = chrono.era();

        long postCutover = chrono.getDateTimeMillis(2020, 1, 1, 0);
        long preCutover = chrono.getDateTimeMillis(1000, 1, 1, 0);

        assertEquals("AD", era.getAsText(postCutover, Locale.ENGLISH));
        assertEquals("AD", era.getAsText(preCutover, Locale.ENGLISH));
        assertEquals("AD", era.getAsShortText(postCutover, Locale.ENGLISH));
        assertEquals("AD", era.getAsShortText(preCutover, Locale.ENGLISH));
        assertEquals("AD", era.getAsText(1, Locale.ENGLISH));
        assertEquals("AD", era.getAsShortText(1, Locale.ENGLISH));
        assertFalse(era.isLenient());
        assertTrue(era.getMaximumTextLength(Locale.ENGLISH) > 0);
        assertTrue(era.getMaximumShortTextLength(Locale.ENGLISH) > 0);
    }

    @Test(timeout = 4000)
    public void testCutoverFieldLeapMethods() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField monthField = chrono.monthOfYear();

        long leapYear = chrono.getDateTimeMillis(2004, 2, 1, 0);
        long nonLeapYear = chrono.getDateTimeMillis(2003, 2, 1, 0);
        long julianLeapYear = chrono.getDateTimeMillis(1500, 2, 1, 0); // 1500 is leap in Julian

        assertTrue(monthField.isLeap(leapYear));
        assertFalse(monthField.isLeap(nonLeapYear));
        assertTrue(monthField.isLeap(julianLeapYear));

        assertEquals(1, monthField.getLeapAmount(leapYear));
        assertEquals(0, monthField.getLeapAmount(nonLeapYear));
        assertEquals(1, monthField.getLeapAmount(julianLeapYear));
        assertNotNull(monthField.getLeapDurationField());
    }

    @Test(timeout = 4000)
    public void testCutoverFieldMinMaxValues() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayOfMonth = chrono.dayOfMonth();

        assertEquals(1, dayOfMonth.getMinimumValue());
        assertEquals(31, dayOfMonth.getMaximumValue());

        long instantCutover = chrono.getDateTimeMillis(1582, 10, 15, 0);
        assertEquals(1, dayOfMonth.getMinimumValue(instantCutover));
        assertEquals(31, dayOfMonth.getMaximumValue(instantCutover));

        long julianInstant = chrono.getDateTimeMillis(1582, 10, 4, 0);
        assertEquals(1, dayOfMonth.getMinimumValue(julianInstant));
        assertEquals(31, dayOfMonth.getMaximumValue(julianInstant));

        Partial partial = new Partial(DateTimeFieldType.dayOfMonth(), 15);
        assertEquals(1, dayOfMonth.getMinimumValue(partial));
        assertEquals(1, dayOfMonth.getMinimumValue(partial, new int[]{15}));
        assertEquals(31, dayOfMonth.getMaximumValue(partial));
        assertEquals(31, dayOfMonth.getMaximumValue(partial, new int[]{15}));
    }

    @Test(timeout = 4000)
    public void testCutoverFieldRoundFloorAndCeiling() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayField = chrono.dayOfMonth();

        long postCutover = chrono.getDateTimeMillis(1582, 10, 16, 12, 30, 0, 0);
        long roundedFloorPost = dayField.roundFloor(postCutover);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 16, 0, 0, 0, 0), roundedFloorPost);

        long preCutover = chrono.getDateTimeMillis(1582, 10, 3, 12, 30, 0, 0);
        long roundedFloorPre = dayField.roundFloor(preCutover);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 3, 0, 0, 0, 0), roundedFloorPre);

        long roundedCeilPost = dayField.roundCeiling(postCutover);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 17, 0, 0, 0, 0), roundedCeilPost);

        long roundedCeilPre = dayField.roundCeiling(preCutover);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 4, 0, 0, 0, 0), roundedCeilPre);
    }

    @Test(timeout = 4000)
    public void testCutoverFieldAddPartial() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        YearMonth ym = new YearMonth(2004, 2, chrono);

        // Add 0 months
        YearMonth ymZero = ym.plusMonths(0);
        assertSame(ym, ymZero);

        // Add 48 months across leap cycle
        YearMonth ymPlus48 = ym.plusMonths(48);
        assertEquals(2008, ymPlus48.getYear());
        assertEquals(2, ymPlus48.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testImpreciseCutoverFieldDifferenceBranches() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField yearField = chrono.year();

        long post1 = chrono.getDateTimeMillis(1600, 1, 1, 0);
        long post2 = chrono.getDateTimeMillis(1610, 1, 1, 0);
        long pre1 = chrono.getDateTimeMillis(1500, 1, 1, 0);
        long pre2 = chrono.getDateTimeMillis(1510, 1, 1, 0);

        // post - post
        assertEquals(10, yearField.getDifference(post2, post1));
        assertEquals(10L, yearField.getDifferenceAsLong(post2, post1));

        // pre - pre
        assertEquals(10, yearField.getDifference(pre2, pre1));
        assertEquals(10L, yearField.getDifferenceAsLong(pre2, pre1));

        // post - pre
        assertEquals(100, yearField.getDifference(post1, pre1));
        assertEquals(100L, yearField.getDifferenceAsLong(post1, pre1));

        // pre - post
        assertEquals(-100, yearField.getDifference(pre1, post1));
        assertEquals(-100L, yearField.getDifferenceAsLong(pre1, post1));

        assertEquals(1, yearField.getMinimumValue(post1));
        assertEquals(1, yearField.getMinimumValue(pre1));
        assertEquals(yearField.getMaximumValue(), yearField.getMaximumValue(post1));
        assertEquals(yearField.getMaximumValue(), yearField.getMaximumValue(pre1));
    }

    @Test(timeout = 4000)
    public void testImpreciseCutoverFieldAddAndDurationField() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField yearField = chrono.year();

        long dt = chrono.getDateTimeMillis(1580, 1, 1, 0);
        long added5 = yearField.add(dt, 5); // 1585 crosses cutover
        assertEquals(1585, yearField.get(added5));

        long added5Long = yearField.add(dt, 5L);
        assertEquals(1585, yearField.get(added5Long));

        long dt2 = chrono.getDateTimeMillis(1585, 1, 1, 0);
        long sub5 = yearField.add(dt2, -5); // 1580 crosses back
        assertEquals(1580, yearField.get(sub5));

        long sub5Long = yearField.add(dt2, -5L);
        assertEquals(1580, yearField.get(sub5Long));

        assertEquals(5, yearField.getDurationField().getDifference(added5, dt));
        assertEquals(5L, yearField.getDurationField().getDifferenceAsLong(added5, dt));
        assertEquals(added5, yearField.getDurationField().add(dt, 5));
        assertEquals(added5, yearField.getDurationField().add(dt, 5L));
    }

    @Test(timeout = 4000)
    public void testCutoverFieldSetString() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField monthField = chrono.monthOfYear();

        long post = chrono.getDateTimeMillis(2000, 1, 1, 0);
        long postSet = monthField.set(post, "June", Locale.ENGLISH);
        assertEquals(6, monthField.get(postSet));

        long pre = chrono.getDateTimeMillis(1500, 1, 1, 0);
        long preSet = monthField.set(pre, "June", Locale.ENGLISH);
        assertEquals(6, monthField.get(preSet));
    }

    @Test(timeout = 4000)
    public void testCutoverAtNonMidnight() {
        // Cutover with time of day != 0 (12:34:56.789)
        DateTime nonMidnightCutover = new DateTime(1582, 10, 15, 12, 34, 56, 789, DateTimeZone.UTC);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, nonMidnightCutover);

        assertEquals(nonMidnightCutover.toInstant(), chrono.getGregorianCutover());
        long dt = chrono.getDateTimeMillis(1582, 10, 15, 15, 0, 0, 0);
        assertEquals(1582, chrono.year().get(dt));
        assertEquals(10, chrono.monthOfYear().get(dt));
        assertEquals(15, chrono.dayOfMonth().get(dt));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect)
    // =========================================================================

    /**
     * Targets known Defects4J defect: TestGJChronology::testLeapYearRulesConstruction
     * In Julian chronology, year 1500 IS a leap year (Feb 29 exists).
     * In Gregorian chronology, year 1500 IS NOT a leap year (divisible by 100, not 400).
     * Constructing a GJChronology with a cutover on a Julian leap day (1500-02-29) causes
     * assemble() -> julianToGregorianByYear() -> GregorianChronology.getDateTimeMillis(1500, 2, 29, ...)
     * which fails with: IllegalFieldValueException: Value 29 for dayOfMonth must be in the range [1,28].
     */
    @Test(timeout = 4000)
    public void testLeapYearRulesConstruction() {
        DateTime cutover = new DateTime(1500, 2, 29, 0, 0, 0, 0, JulianChronology.getInstanceUTC());
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutover);
        assertNotNull("GJChronology must successfully construct with cutover on Julian leap day", chrono);
        assertEquals(cutover.toInstant(), chrono.getGregorianCutover());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDateTimeMillisCutoverGap4Fields() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // 1582-10-05 was removed by the Gregorian reform (illegal gap)
        chrono.getDateTimeMillis(1582, 10, 5, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDateTimeMillisCutoverGap7Fields() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // 1582-10-10 does not exist
        chrono.getDateTimeMillis(1582, 10, 10, 12, 0, 0, 0);
    }

    @Test(expected = IllegalFieldValueException.class, timeout = 4000)
    public void testCutoverFieldSetValueNotStuckThrows() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayField = chrono.dayOfMonth();
        // Trying to set day to 31 in a 30-day month
        long dt = chrono.getDateTimeMillis(1999, 4, 15, 0);
        dayField.set(dt, 31);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        GJChronology c1 = GJChronology.getInstanceUTC();
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 4);
        GJChronology c3 = GJChronology.getInstance(DateTimeZone.forOffsetHours(1));

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
        assertNotEquals(c1, null);
        assertNotEquals(c1, new Object());
    }

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        GJChronology cDefault = GJChronology.getInstanceUTC();
        assertEquals("GJChronology[UTC]", cDefault.toString());

        GJChronology cWithMdfw = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 3);
        assertEquals("GJChronology[UTC,mdfw=3]", cWithMdfw.toString());

        Instant customCutoverDateOnly = new DateTime(1970, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).toInstant();
        GJChronology cCustomDate = GJChronology.getInstance(DateTimeZone.UTC, customCutoverDateOnly, 4);
        assertTrue(cCustomDate.toString().contains("cutover=1970-01-01"));

        Instant customCutoverTime = new DateTime(1970, 1, 1, 12, 30, 0, 0, DateTimeZone.UTC).toInstant();
        GJChronology cCustomTime = GJChronology.getInstance(DateTimeZone.UTC, customCutoverTime, 4);
        assertTrue(cCustomTime.toString().contains("cutover=1970-01-01T12:30:00.000Z"));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.forOffsetHours(2), new Instant(1000000L), 5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(chrono);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        GJChronology deserialized = (GJChronology) ois.readObject();
        ois.close();

        assertSame(chrono, deserialized);
    }
}