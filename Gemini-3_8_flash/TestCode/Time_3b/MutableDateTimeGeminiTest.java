package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.field.UnsupportedDateTimeField;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Defects4J Target Defect:
 * - Issue: Calling addYears(0), addMonths(0), addDays(0), addWeeks(0), or add(DurationFieldType, 0)
 *   on a MutableDateTime situated in the winter daylight saving time overlap (fall-back)
 *   inadvertently flips the zone offset from winter (+01:00) back to summer (+02:00).
 * - Targeted Test Methods:
 *   - testAdd_dstOverlapWinter_addZero_Defect()
 *   - testAddYears_dstOverlapWinter_addZero_Defect()
 *   - testAddMonths_dstOverlapWinter_addZero_Defect()
 *   - testAddWeeks_dstOverlapWinter_addZero_Defect()
 *   - testAddDays_dstOverlapWinter_addZero_Defect()
 *
 * Branch Coverage Targets:
 * - Constructors: null vs non-null zone/chronology/object, all field arities.
 * - Static factories: now(), now(zone), now(chronology), parse(str), parse(str, formatter).
 * - Guard conditions: NullPointerExceptions for null zones/chronologies in now().
 * - setRounding(field, mode):
 *   - field != null && (mode < 0 || mode > 5) -> IllegalArgumentException.
 *   - field == null -> mode resets to ROUND_NONE.
 *   - mode == ROUND_NONE -> field resets to null.
 *   - switch(iRoundingMode): cases ROUND_NONE, ROUND_FLOOR, ROUND_CEILING,
 *     ROUND_HALF_FLOOR, ROUND_HALF_CEILING, ROUND_HALF_EVEN.
 * - add(ReadableDuration, scalar): duration == null vs != null.
 * - add(ReadablePeriod, scalar): period == null vs != null.
 * - setZone(newZone): chrono.getZone() == newZone vs != newZone.
 * - setZoneRetainFields(newZone): newZone == originalZone vs != originalZone.
 * - set(DateTimeFieldType, int) / add(DurationFieldType, int): null type checks.
 * - setDate(ReadableInstant): instant instanceof ReadableDateTime vs general ReadableInstant;
 *   zone != null vs null.
 * - setTime(ReadableInstant): zone != null vs null.
 * - property(DateTimeFieldType): type == null, unsupported field.
 * - Inner Property class: field navigation, rounding, add/set methods, serialization round-trip.
 * - copy(), clone(), toString(), equals(), hashCode().
 */
public class MutableDateTimeGeminiTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone UTC = DateTimeZone.UTC;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddYears_dstOverlapWinter_addZero_Defect() {
        // 2011-10-30 01:30:00 UTC corresponds to 02:30:00+01:00 (the 2nd occurrence in DST overlap)
        DateTime dt = new DateTime(2011, 10, 30, 1, 30, 0, 0, UTC).withZone(PARIS);
        assertEquals("2011-10-30T02:30:00.000+01:00", dt.toString());

        MutableDateTime test = dt.toMutableDateTime();
        test.addYears(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

    @Test(timeout = 4000)
    public void testAddMonths_dstOverlapWinter_addZero_Defect() {
        DateTime dt = new DateTime(2011, 10, 30, 1, 30, 0, 0, UTC).withZone(PARIS);
        assertEquals("2011-10-30T02:30:00.000+01:00", dt.toString());

        MutableDateTime test = dt.toMutableDateTime();
        test.addMonths(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

    @Test(timeout = 4000)
    public void testAddWeeks_dstOverlapWinter_addZero_Defect() {
        DateTime dt = new DateTime(2011, 10, 30, 1, 30, 0, 0, UTC).withZone(PARIS);
        assertEquals("2011-10-30T02:30:00.000+01:00", dt.toString());

        MutableDateTime test = dt.toMutableDateTime();
        test.addWeeks(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

    @Test(timeout = 4000)
    public void testAddDays_dstOverlapWinter_addZero_Defect() {
        DateTime dt = new DateTime(2011, 10, 30, 1, 30, 0, 0, UTC).withZone(PARIS);
        assertEquals("2011-10-30T02:30:00.000+01:00", dt.toString());

        MutableDateTime test = dt.toMutableDateTime();
        test.addDays(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

    @Test(timeout = 4000)
    public void testAdd_DurationFieldType_dstOverlapWinter_addZero_Defect() {
        DateTime dt = new DateTime(2011, 10, 30, 1, 30, 0, 0, UTC).withZone(PARIS);
        assertEquals("2011-10-30T02:30:00.000+01:00", dt.toString());

        MutableDateTime test = dt.toMutableDateTime();
        test.add(DurationFieldType.days(), 0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndFactories() {
        MutableDateTime mdt1 = MutableDateTime.now();
        assertNotNull(mdt1);

        MutableDateTime mdt2 = MutableDateTime.now(PARIS);
        assertEquals(PARIS, mdt2.getZone());

        MutableDateTime mdt3 = MutableDateTime.now(ISOChronology.getInstanceUTC());
        assertEquals(ISOChronology.getInstanceUTC(), mdt3.getChronology());

        MutableDateTime mdt4 = new MutableDateTime(PARIS);
        assertEquals(PARIS, mdt4.getZone());

        MutableDateTime mdt5 = new MutableDateTime((DateTimeZone) null);
        assertEquals(DateTimeZone.getDefault(), mdt5.getZone());

        MutableDateTime mdt6 = new MutableDateTime(ISOChronology.getInstanceUTC());
        assertEquals(ISOChronology.getInstanceUTC(), mdt6.getChronology());

        MutableDateTime mdt7 = new MutableDateTime((Chronology) null);
        assertEquals(ISOChronology.getInstance(), mdt7.getChronology());

        MutableDateTime mdt8 = new MutableDateTime(123456789L);
        assertEquals(123456789L, mdt8.getMillis());

        MutableDateTime mdt9 = new MutableDateTime(123456789L, PARIS);
        assertEquals(123456789L, mdt9.getMillis());
        assertEquals(PARIS, mdt9.getZone());

        MutableDateTime mdt10 = new MutableDateTime(123456789L, (DateTimeZone) null);
        assertEquals(DateTimeZone.getDefault(), mdt10.getZone());

        MutableDateTime mdt11 = new MutableDateTime(123456789L, ISOChronology.getInstanceUTC());
        assertEquals(ISOChronology.getInstanceUTC(), mdt11.getChronology());

        MutableDateTime mdt12 = new MutableDateTime(123456789L, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), mdt12.getChronology());

        MutableDateTime mdt13 = new MutableDateTime("2020-05-10T12:30:45.123Z");
        assertEquals(2020, mdt13.getYear());
        assertEquals(5, mdt13.getMonthOfYear());

        MutableDateTime mdt14 = new MutableDateTime("2020-05-10T12:30:45.123Z", PARIS);
        assertEquals(PARIS, mdt14.getZone());

        MutableDateTime mdt15 = new MutableDateTime("2020-05-10T12:30:45.123Z", (DateTimeZone) null);
        assertEquals(DateTimeZone.getDefault(), mdt15.getZone());

        MutableDateTime mdt16 = new MutableDateTime("2020-05-10T12:30:45.123Z", GregorianChronology.getInstance(UTC));
        assertEquals(GregorianChronology.getInstance(UTC), mdt16.getChronology());

        MutableDateTime mdt17 = new MutableDateTime("2020-05-10T12:30:45.123Z", (Chronology) null);
        assertEquals(ISOChronology.getInstance(), mdt17.getChronology());

        MutableDateTime mdt18 = new MutableDateTime(2021, 6, 15, 10, 20, 30, 400);
        assertEquals(2021, mdt18.getYear());
        assertEquals(6, mdt18.getMonthOfYear());
        assertEquals(15, mdt18.getDayOfMonth());
        assertEquals(10, mdt18.getHourOfDay());
        assertEquals(20, mdt18.getMinuteOfHour());
        assertEquals(30, mdt18.getSecondOfMinute());
        assertEquals(400, mdt18.getMillisOfSecond());

        MutableDateTime mdt19 = new MutableDateTime(2021, 6, 15, 10, 20, 30, 400, UTC);
        assertEquals(UTC, mdt19.getZone());

        MutableDateTime mdt20 = new MutableDateTime(2021, 6, 15, 10, 20, 30, 400, (DateTimeZone) null);
        assertEquals(DateTimeZone.getDefault(), mdt20.getZone());

        MutableDateTime mdt21 = new MutableDateTime(2021, 6, 15, 10, 20, 30, 400, GregorianChronology.getInstanceUTC());
        assertEquals(GregorianChronology.getInstanceUTC(), mdt21.getChronology());

        MutableDateTime mdt22 = new MutableDateTime(2021, 6, 15, 10, 20, 30, 400, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), mdt22.getChronology());
    }

    @Test(timeout = 4000)
    public void testParseMethods() {
        MutableDateTime mdt = MutableDateTime.parse("2023-01-15T08:30:00.000Z");
        assertEquals(2023, mdt.getYear());
        assertEquals(1, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());

        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm").withZone(UTC);
        MutableDateTime mdtFormatted = MutableDateTime.parse("2023/02/20 14:45", dtf);
        assertEquals(2023, mdtFormatted.getYear());
        assertEquals(2, mdtFormatted.getMonthOfYear());
        assertEquals(20, mdtFormatted.getDayOfMonth());
        assertEquals(14, mdtFormatted.getHourOfDay());
        assertEquals(45, mdtFormatted.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testSetAndGetChronologyAndZone() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(UTC, mdt.getZone());

        mdt.setZone(PARIS);
        assertEquals(PARIS, mdt.getZone());
        // setZone with identical zone takes no-op branch
        mdt.setZone(PARIS);
        assertEquals(PARIS, mdt.getZone());

        // setZone with null uses default zone
        mdt.setZone(null);
        assertEquals(DateTimeZone.getDefault(), mdt.getZone());

        // setChronology
        Chronology gj = GJChronology.getInstanceUTC();
        mdt.setChronology(gj);
        assertEquals(gj, mdt.getChronology());
        mdt.setChronology(null);
        assertEquals(ISOChronology.getInstance(), mdt.getChronology());
    }

    @Test(timeout = 4000)
    public void testSetZoneRetainFields() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 10, 0, 0, 0, UTC);
        mdt.setZoneRetainFields(PARIS);
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(PARIS, mdt.getZone());

        // No-op branch when target zone equals original zone
        mdt.setZoneRetainFields(PARIS);
        assertEquals(PARIS, mdt.getZone());

        // null defaults to default zone
        DateTimeZone def = DateTimeZone.getDefault();
        mdt.setZoneRetainFields(null);
        assertEquals(def, mdt.getZone());
        assertEquals(10, mdt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testSetDateAndTimeVariants() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 12, 30, 40, 500, UTC);

        // setDate(long)
        DateTime targetDate = new DateTime(2025, 11, 23, 5, 0, 0, 0, UTC);
        mdt.setDate(targetDate.getMillis());
        assertEquals(2025, mdt.getYear());
        assertEquals(11, mdt.getMonthOfYear());
        assertEquals(23, mdt.getDayOfMonth());
        assertEquals(12, mdt.getHourOfDay()); // time part unaffected

        // setDate(ReadableInstant) with ReadableDateTime
        DateTime targetRdt = new DateTime(2018, 7, 4, 1, 0, 0, 0, LONDON);
        mdt.setDate(targetRdt);
        assertEquals(2018, mdt.getYear());
        assertEquals(7, mdt.getMonthOfYear());
        assertEquals(4, mdt.getDayOfMonth());
        assertEquals(12, mdt.getHourOfDay());

        // setDate(ReadableInstant) with general Instant (not ReadableDateTime)
        Instant instant = new DateTime(2019, 8, 12, 0, 0, UTC).toInstant();
        mdt.setDate(instant);
        assertEquals(2019, mdt.getYear());
        assertEquals(8, mdt.getMonthOfYear());
        assertEquals(12, mdt.getDayOfMonth());

        // setDate(year, month, day)
        mdt.setDate(2030, 3, 19);
        assertEquals(2030, mdt.getYear());
        assertEquals(3, mdt.getMonthOfYear());
        assertEquals(19, mdt.getDayOfMonth());
        assertEquals(12, mdt.getHourOfDay());

        // setTime(long)
        DateTime targetTime = new DateTime(1970, 1, 1, 16, 45, 12, 321, UTC);
        mdt.setTime(targetTime.getMillis());
        assertEquals(16, mdt.getHourOfDay());
        assertEquals(45, mdt.getMinuteOfHour());
        assertEquals(12, mdt.getSecondOfMinute());
        assertEquals(321, mdt.getMillisOfSecond());
        assertEquals(2030, mdt.getYear()); // date part unaffected

        // setTime(ReadableInstant)
        DateTime rdtTime = new DateTime(2000, 5, 5, 21, 15, 30, 100, UTC);
        mdt.setTime(rdtTime);
        assertEquals(21, mdt.getHourOfDay());
        assertEquals(15, mdt.getMinuteOfHour());
        assertEquals(30, mdt.getSecondOfMinute());
        assertEquals(100, mdt.getMillisOfSecond());

        // setTime(h, m, s, ms)
        mdt.setTime(6, 7, 8, 9);
        assertEquals(6, mdt.getHourOfDay());
        assertEquals(7, mdt.getMinuteOfHour());
        assertEquals(8, mdt.getSecondOfMinute());
        assertEquals(9, mdt.getMillisOfSecond());

        // setDateTime(y, m, d, h, m, s, ms)
        mdt.setDateTime(2015, 10, 20, 8, 9, 10, 11);
        assertEquals(2015, mdt.getYear());
        assertEquals(10, mdt.getMonthOfYear());
        assertEquals(20, mdt.getDayOfMonth());
        assertEquals(8, mdt.getHourOfDay());
        assertEquals(9, mdt.getMinuteOfHour());
        assertEquals(10, mdt.getSecondOfMinute());
        assertEquals(11, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testFieldGettersAndSetters() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 0, 0, 0, 0, UTC);

        mdt.setYear(2022);
        assertEquals(2022, mdt.getYear());
        mdt.addYears(3);
        assertEquals(2025, mdt.getYear());

        mdt.setWeekyear(2024);
        assertEquals(2024, mdt.getWeekyear());
        mdt.addWeekyears(-1);
        assertEquals(2023, mdt.getWeekyear());

        mdt.setMonthOfYear(6);
        assertEquals(6, mdt.getMonthOfYear());
        mdt.addMonths(2);
        assertEquals(8, mdt.getMonthOfYear());

        mdt.setWeekOfWeekyear(15);
        assertEquals(15, mdt.getWeekOfWeekyear());
        mdt.addWeeks(2);
        assertEquals(17, mdt.getWeekOfWeekyear());

        mdt.setDayOfYear(100);
        assertEquals(100, mdt.getDayOfYear());

        mdt.setDayOfMonth(12);
        assertEquals(12, mdt.getDayOfMonth());
        mdt.addDays(5);
        assertEquals(17, mdt.getDayOfMonth());

        mdt.setDayOfWeek(DateTimeConstants.WEDNESDAY);
        assertEquals(DateTimeConstants.WEDNESDAY, mdt.getDayOfWeek());

        mdt.setHourOfDay(14);
        assertEquals(14, mdt.getHourOfDay());
        mdt.addHours(3);
        assertEquals(17, mdt.getHourOfDay());

        mdt.setMinuteOfDay(500);
        assertEquals(500, mdt.getMinuteOfDay());

        mdt.setMinuteOfHour(45);
        assertEquals(45, mdt.getMinuteOfHour());
        mdt.addMinutes(10);
        assertEquals(55, mdt.getMinuteOfHour());

        mdt.setSecondOfDay(1000);
        assertEquals(1000, mdt.getSecondOfDay());

        mdt.setSecondOfMinute(35);
        assertEquals(35, mdt.getSecondOfMinute());
        mdt.addSeconds(10);
        assertEquals(45, mdt.getSecondOfMinute());

        mdt.setMillisOfDay(1234567);
        assertEquals(1234567, mdt.getMillisOfDay());

        mdt.setMillisOfSecond(888);
        assertEquals(888, mdt.getMillisOfSecond());
        mdt.addMillis(100);
        assertEquals(988, mdt.getMillisOfSecond());

        // Field set/add with DateTimeFieldType and DurationFieldType
        mdt.set(DateTimeFieldType.monthOfYear(), 11);
        assertEquals(11, mdt.getMonthOfYear());
        mdt.add(DurationFieldType.months(), 1);
        assertEquals(12, mdt.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testDurationAndPeriodAdditions() {
        MutableDateTime mdt = new MutableDateTime(10000L, UTC);

        mdt.add(5000L);
        assertEquals(15000L, mdt.getMillis());

        Duration dur = new Duration(2000L);
        mdt.add(dur);
        assertEquals(17000L, mdt.getMillis());

        mdt.add(dur, 2);
        assertEquals(21000L, mdt.getMillis());

        // Null duration adds zero
        mdt.add((ReadableDuration) null);
        mdt.add((ReadableDuration) null, 5);
        assertEquals(21000L, mdt.getMillis());

        Period period = Period.seconds(5);
        mdt.add(period);
        assertEquals(26000L, mdt.getMillis());

        mdt.add(period, -2);
        assertEquals(16000L, mdt.getMillis());

        // Null period adds zero
        mdt.add((ReadablePeriod) null);
        mdt.add((ReadablePeriod) null, 10);
        assertEquals(16000L, mdt.getMillis());

        // setMillis(ReadableInstant)
        mdt.setMillis(new Instant(99999L));
        assertEquals(99999L, mdt.getMillis());

        mdt.setMillis((ReadableInstant) null);
        assertTrue(mdt.getMillis() > 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRoundingModes() {
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 10, 30, 45, 750, UTC);
        DateTimeField secondField = mdt.getChronology().secondOfMinute();

        // setRounding(field) defaults to ROUND_FLOOR
        mdt.setRounding(secondField);
        assertEquals(secondField, mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_FLOOR, mdt.getRoundingMode());
        assertEquals(0, mdt.getMillisOfSecond());

        // ROUND_CEILING
        mdt.setMillis(10500L);
        mdt.setRounding(secondField, MutableDateTime.ROUND_CEILING);
        assertEquals(11000L, mdt.getMillis());

        // ROUND_HALF_FLOOR
        mdt.setRounding(secondField, MutableDateTime.ROUND_HALF_FLOOR);
        mdt.setMillis(10500L);
        assertEquals(10000L, mdt.getMillis());
        mdt.setMillis(10501L);
        assertEquals(11000L, mdt.getMillis());

        // ROUND_HALF_CEILING
        mdt.setRounding(secondField, MutableDateTime.ROUND_HALF_CEILING);
        mdt.setMillis(10500L);
        assertEquals(11000L, mdt.getMillis());

        // ROUND_HALF_EVEN
        mdt.setRounding(secondField, MutableDateTime.ROUND_HALF_EVEN);
        mdt.setMillis(10500L); // 10 is even -> rounds to 10000
        assertEquals(10000L, mdt.getMillis());
        mdt.setMillis(11500L); // 11 is odd -> rounds to 12000
        assertEquals(12000L, mdt.getMillis());

        // ROUND_NONE disables rounding
        mdt.setRounding(secondField, MutableDateTime.ROUND_NONE);
        assertNull(mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());
        mdt.setMillis(12345L);
        assertEquals(12345L, mdt.getMillis());

        // null field disables rounding
        mdt.setRounding(null, MutableDateTime.ROUND_FLOOR);
        assertNull(mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());
    }

    @Test(timeout = 4000)
    public void testPropertyMethods() {
        MutableDateTime mdt = new MutableDateTime(2021, 5, 20, 13, 30, 45, 500, UTC);

        // All property accessors
        assertNotNull(mdt.era());
        assertNotNull(mdt.centuryOfEra());
        assertNotNull(mdt.yearOfCentury());
        assertNotNull(mdt.yearOfEra());
        assertNotNull(mdt.year());
        assertNotNull(mdt.weekyear());
        assertNotNull(mdt.monthOfYear());
        assertNotNull(mdt.weekOfWeekyear());
        assertNotNull(mdt.dayOfYear());
        assertNotNull(mdt.dayOfMonth());
        assertNotNull(mdt.dayOfWeek());
        assertNotNull(mdt.hourOfDay());
        assertNotNull(mdt.minuteOfDay());
        assertNotNull(mdt.minuteOfHour());
        assertNotNull(mdt.secondOfDay());
        assertNotNull(mdt.secondOfMinute());
        assertNotNull(mdt.millisOfDay());
        assertNotNull(mdt.millisOfSecond());

        // Property operations
        MutableDateTime.Property prop = mdt.minuteOfHour();
        assertEquals(30, prop.get());
        assertSame(mdt, prop.getMutableDateTime());
        assertEquals(mdt.getMillis(), prop.getMillis());
        assertEquals(mdt.getChronology(), prop.getChronology());
        assertEquals(DateTimeConstants.MINUTES_PER_HOUR - 1, prop.getMaximumValue());

        prop.add(5);
        assertEquals(35, mdt.getMinuteOfHour());

        prop.add(10L);
        assertEquals(45, mdt.getMinuteOfHour());

        prop.addWrapField(20);
        assertEquals(5, mdt.getMinuteOfHour());

        prop.set(15);
        assertEquals(15, mdt.getMinuteOfHour());

        prop.set("25", Locale.ENGLISH);
        assertEquals(25, mdt.getMinuteOfHour());

        prop.set("40");
        assertEquals(40, mdt.getMinuteOfHour());

        // Property rounding
        mdt.secondOfMinute().set(30);
        mdt.millisOfSecond().set(500);

        prop.roundFloor();
        assertEquals(0, mdt.getSecondOfMinute());
        assertEquals(0, mdt.getMillisOfSecond());

        mdt.minuteOfHour().set(10);
        mdt.secondOfMinute().set(30);
        prop.roundCeiling();
        assertEquals(11, mdt.getMinuteOfHour());

        mdt.minuteOfHour().set(10);
        mdt.secondOfMinute().set(30);
        mdt.millisOfSecond().set(0);
        prop.roundHalfFloor();
        assertEquals(10, mdt.getMinuteOfHour());

        mdt.minuteOfHour().set(10);
        mdt.secondOfMinute().set(30);
        mdt.millisOfSecond().set(0);
        prop.roundHalfCeiling();
        assertEquals(11, mdt.getMinuteOfHour());

        mdt.minuteOfHour().set(10);
        mdt.secondOfMinute().set(30);
        mdt.millisOfSecond().set(0);
        prop.roundHalfEven();
        assertEquals(10, mdt.getMinuteOfHour());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNowZoneNull() {
        MutableDateTime.now((DateTimeZone) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNowChronologyNull() {
        MutableDateTime.now((Chronology) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRoundingInvalidModeNegative() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.setRounding(mdt.getChronology().hourOfDay(), -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetRoundingInvalidModeExcess() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.setRounding(mdt.getChronology().hourOfDay(), 6);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNullDateTimeFieldType() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.set((DateTimeFieldType) null, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNullDurationFieldType() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.add((DurationFieldType) null, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyNullFieldType() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.property(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyUnsupportedField() {
        MutableDateTime mdt = new MutableDateTime();
        DateTimeFieldType unsupportedType = new DateTimeFieldType("unsupported") {
            private static final long serialVersionUID = 1L;
            public DurationFieldType getDurationType() {
                return DurationFieldType.days();
            }
            public DurationFieldType getRangeDurationType() {
                return null;
            }
            public DateTimeField getField(Chronology chronology) {
                return UnsupportedDateTimeField.getInstance(this, UnsupportedDurationField.getInstance(getDurationType()));
            }
        };
        mdt.property(unsupportedType);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneAndCopy() {
        MutableDateTime original = new MutableDateTime(2020, 5, 10, 15, 20, 30, 400, UTC);
        MutableDateTime clone = (MutableDateTime) original.clone();
        MutableDateTime copy = original.copy();

        assertEquals(original, clone);
        assertEquals(original, copy);
        assertNotSame(original, clone);
        assertNotSame(original, copy);

        clone.addDays(1);
        assertFalse(original.equals(clone));
    }

    @Test(timeout = 4000)
    public void testToString() {
        MutableDateTime mdt = new MutableDateTime(2021, 12, 25, 0, 0, 0, 0, UTC);
        assertEquals("2021-12-25T00:00:00.000Z", mdt.toString());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        MutableDateTime mdt = new MutableDateTime(2022, 7, 14, 18, 45, 30, 123, PARIS);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mdt);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutableDateTime deserialized = (MutableDateTime) ois.readObject();
        ois.close();

        assertEquals(mdt, deserialized);
        assertEquals(mdt.getMillis(), deserialized.getMillis());
        assertEquals(mdt.getChronology(), deserialized.getChronology());
    }

    @Test(timeout = 4000)
    public void testPropertySerialization() throws Exception {
        MutableDateTime mdt = new MutableDateTime(2022, 7, 14, 18, 45, 30, 123, UTC);
        MutableDateTime.Property prop = mdt.dayOfMonth();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MutableDateTime.Property deserializedProp = (MutableDateTime.Property) ois.readObject();
        ois.close();

        assertEquals(prop.get(), deserializedProp.get());
        assertEquals(prop.getMillis(), deserializedProp.getMillis());
        assertEquals(prop.getField().getType(), deserializedProp.getField().getType());
    }
}