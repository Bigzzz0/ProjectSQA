package org.joda.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Target Class: org.joda.time.LocalDateTime
 *
 * Targeted Defects & Decision Branches:
 * 1. BC / Before Year Zero handling in static factories:
 *    - fromCalendarFields(Calendar) for Era BC (GregorianCalendar.BC, year 1 -> year 0 ISO; year 3 -> year -2 ISO).
 *    - fromDateFields(Date) for Era BC (Date representing 1 BC -> year 0 ISO; 3 BC -> year -2 ISO).
 * 2. Constructors & Chronologies:
 *    - Default now(), now(zone), now(chronology), null guards.
 *    - parse(str), parse(str, formatter).
 *    - Object instant constructor via ConverterManager (ReadableInstant, String, Long, Calendar, Date).
 *    - Field constructors: (y, m, d, h, m), (y, m, d, h, m, s), (y, m, d, h, m, s, S), with/without Chronology.
 * 3. Inspection & Index-based access:
 *    - size() == 4; getField(0..3, chrono) & getValue(0..3).
 *    - IndexOutOfBoundsException for invalid index (< 0 or > 3).
 *    - get(DateTimeFieldType), isSupported(DateTimeFieldType), isSupported(DurationFieldType).
 * 4. Comparisons & Equality:
 *    - equals() (same ref, not instance, different chronology, diff millis, equal instance).
 *    - compareTo() (same ref, instance with same chronology, comparison with general ReadablePartial).
 * 5. Conversions:
 *    - toDateTime(), toDateTime(DateTimeZone), toLocalDate(), toLocalTime(), toDate() (gap, overlap, standard).
 * 6. Immutability & Mutations (with* & plus/minus):
 *    - withDate, withTime, withFields (null and non-null), withField, withFieldAdded (amount == 0 branch).
 *    - withDurationAdded, withPeriodAdded (duration/period == null, scalar == 0).
 *    - plus/minus: Years, Months, Weeks, Days, Hours, Minutes, Seconds, Millis (0 delta shortcut check).
 * 7. Property inner class:
 *    - All field properties: era, centuryOfEra, yearOfCentury, yearOfEra, year, weekyear, monthOfYear,
 *      weekOfWeekyear, dayOfYear, dayOfMonth, dayOfWeek, hourOfDay, minuteOfHour, secondOfMinute,
 *      millisOfSecond, millisOfDay.
 *    - Property methods: addToCopy, addWrapFieldToCopy, setCopy(int), setCopy(String), setCopy(String, Locale),
 *      withMaximumValue, withMinimumValue, roundFloorCopy, roundCeilingCopy, roundHalfFloorCopy,
 *      roundHalfCeilingCopy, roundHalfEvenCopy.
 * 8. Serialization:
 *    - LocalDateTime serialization roundtrip and readResolve validation.
 *    - LocalDateTime.Property serialization roundtrip.
 */
public class LocalDateTimeGeminiTest {

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFactory_fromCalendarFields_beforeYearZero1() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);

        LocalDateTime expected = new LocalDateTime(0, 2, 3, 4, 5, 6, 7);
        LocalDateTime actual = LocalDateTime.fromCalendarFields(cal);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testFactory_fromCalendarFields_beforeYearZero3() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 3);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);

        LocalDateTime expected = new LocalDateTime(-2, 2, 3, 4, 5, 6, 7);
        LocalDateTime actual = LocalDateTime.fromCalendarFields(cal);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testFactory_fromDateFields_beforeYearZero1() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);

        Date date = cal.getTime();
        LocalDateTime expected = new LocalDateTime(0, 2, 3, 4, 5, 6, 7);
        LocalDateTime actual = LocalDateTime.fromDateFields(date);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testFactory_fromDateFields_beforeYearZero3() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 3);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);

        Date date = cal.getTime();
        LocalDateTime expected = new LocalDateTime(-2, 2, 3, 4, 5, 6, 7);
        LocalDateTime actual = LocalDateTime.fromDateFields(date);
        assertEquals(expected, actual);
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorsAndNow() {
        LocalDateTime dtNow = LocalDateTime.now();
        assertNotNull(dtNow);
        assertEquals(ISOChronology.getInstanceUTC(), dtNow.getChronology());

        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");
        LocalDateTime dtZone = LocalDateTime.now(zoneParis);
        assertNotNull(dtZone);
        assertEquals(ISOChronology.getInstanceUTC(), dtZone.getChronology());

        Chronology coptic = CopticChronology.getInstance();
        LocalDateTime dtChrono = LocalDateTime.now(coptic);
        assertNotNull(dtChrono);
        assertEquals(CopticChronology.getInstanceUTC(), dtChrono.getChronology());

        LocalDateTime dtMillis = new LocalDateTime(10000L);
        assertEquals(ISOChronology.getInstanceUTC(), dtMillis.getChronology());

        LocalDateTime dtMillisZone = new LocalDateTime(10000L, zoneParis);
        assertEquals(ISOChronology.getInstanceUTC(), dtMillisZone.getChronology());

        LocalDateTime dtMillisChrono = new LocalDateTime(10000L, coptic);
        assertEquals(CopticChronology.getInstanceUTC(), dtMillisChrono.getChronology());

        LocalDateTime dtMillisNullZone = new LocalDateTime(10000L, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstanceUTC(), dtMillisNullZone.getChronology());

        LocalDateTime dtMillisNullChrono = new LocalDateTime(10000L, (Chronology) null);
        assertEquals(ISOChronology.getInstanceUTC(), dtMillisNullChrono.getChronology());
    }

    @Test(timeout = 4000)
    public void testFieldConstructors() {
        LocalDateTime dt5 = new LocalDateTime(2021, 5, 12, 14, 30);
        assertEquals(2021, dt5.getYear());
        assertEquals(5, dt5.getMonthOfYear());
        assertEquals(12, dt5.getDayOfMonth());
        assertEquals(14, dt5.getHourOfDay());
        assertEquals(30, dt5.getMinuteOfHour());
        assertEquals(0, dt5.getSecondOfMinute());
        assertEquals(0, dt5.getMillisOfSecond());

        LocalDateTime dt6 = new LocalDateTime(2021, 5, 12, 14, 30, 45);
        assertEquals(45, dt6.getSecondOfMinute());
        assertEquals(0, dt6.getMillisOfSecond());

        LocalDateTime dt7 = new LocalDateTime(2021, 5, 12, 14, 30, 45, 123);
        assertEquals(123, dt7.getMillisOfSecond());

        LocalDateTime dtChrono = new LocalDateTime(2021, 5, 12, 14, 30, 45, 123, BuddhistChronology.getInstanceUTC());
        assertEquals(BuddhistChronology.getInstanceUTC(), dtChrono.getChronology());
        assertEquals(2021, dtChrono.getYear());
    }

    @Test(timeout = 4000)
    public void testObjectConstructors() {
        LocalDateTime source = new LocalDateTime(2020, 1, 15, 10, 20, 30, 400);
        LocalDateTime copy1 = new LocalDateTime(source);
        assertEquals(source, copy1);

        LocalDateTime copy2 = new LocalDateTime(source, DateTimeZone.UTC);
        assertEquals(source, copy2);

        LocalDateTime copy3 = new LocalDateTime("2020-01-15T10:20:30.400");
        assertEquals(source, copy3);

        LocalDateTime copy4 = new LocalDateTime("2020-01-15T10:20:30.400", BuddhistChronology.getInstanceUTC());
        assertEquals(BuddhistChronology.getInstanceUTC(), copy4.getChronology());
    }

    @Test(timeout = 4000)
    public void testParse() {
        LocalDateTime dt = LocalDateTime.parse("2023-11-20T18:45:12.345");
        assertEquals(2023, dt.getYear());
        assertEquals(11, dt.getMonthOfYear());
        assertEquals(20, dt.getDayOfMonth());
        assertEquals(18, dt.getHourOfDay());
        assertEquals(45, dt.getMinuteOfHour());
        assertEquals(12, dt.getSecondOfMinute());
        assertEquals(345, dt.getMillisOfSecond());

        LocalDateTime dtFmt = LocalDateTime.parse("2023/11/20 18-45", org.joda.time.format.DateTimeFormat.forPattern("yyyy/MM/dd HH-mm"));
        assertEquals(2023, dtFmt.getYear());
        assertEquals(11, dtFmt.getMonthOfYear());
        assertEquals(20, dtFmt.getDayOfMonth());
        assertEquals(18, dtFmt.getHourOfDay());
        assertEquals(45, dtFmt.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testGettersAndSize() {
        LocalDateTime dt = new LocalDateTime(2022, 6, 18, 9, 15, 25, 500);
        assertEquals(4, dt.size());

        assertEquals(2022, dt.getValue(0));
        assertEquals(6, dt.getValue(1));
        assertEquals(18, dt.getValue(2));
        assertEquals(dt.getMillisOfDay(), dt.getValue(3));

        assertEquals(ISOChronology.getInstanceUTC().year(), dt.getField(0, ISOChronology.getInstanceUTC()));
        assertEquals(ISOChronology.getInstanceUTC().monthOfYear(), dt.getField(1, ISOChronology.getInstanceUTC()));
        assertEquals(ISOChronology.getInstanceUTC().dayOfMonth(), dt.getField(2, ISOChronology.getInstanceUTC()));
        assertEquals(ISOChronology.getInstanceUTC().millisOfDay(), dt.getField(3, ISOChronology.getInstanceUTC()));

        assertEquals(1, dt.getEra());
        assertEquals(21, dt.getCenturyOfEra());
        assertEquals(2022, dt.getYearOfEra());
        assertEquals(22, dt.getYearOfCentury());
        assertEquals(2022, dt.getWeekyear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(24, dt.getWeekOfWeekyear());
        assertEquals(169, dt.getDayOfYear());
        assertEquals(18, dt.getDayOfMonth());
        assertEquals(6, dt.getDayOfWeek()); // Saturday
        assertEquals(9, dt.getHourOfDay());
        assertEquals(15, dt.getMinuteOfHour());
        assertEquals(25, dt.getSecondOfMinute());
        assertEquals(500, dt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testGetAndIsSupported() {
        LocalDateTime dt = new LocalDateTime(2021, 4, 10, 8, 30, 0, 0);
        assertEquals(2021, dt.get(DateTimeFieldType.year()));
        assertEquals(4, dt.get(DateTimeFieldType.monthOfYear()));
        assertEquals(10, dt.get(DateTimeFieldType.dayOfMonth()));

        assertTrue(dt.isSupported(DateTimeFieldType.dayOfYear()));
        assertTrue(dt.isSupported(DateTimeFieldType.minuteOfHour()));
        assertFalse(dt.isSupported((DateTimeFieldType) null));

        assertTrue(dt.isSupported(DurationFieldType.days()));
        assertTrue(dt.isSupported(DurationFieldType.hours()));
        assertFalse(dt.isSupported((DurationFieldType) null));
    }

    @Test(timeout = 4000)
    public void testWithDateAndWithTime() {
        LocalDateTime dt = new LocalDateTime(2021, 4, 10, 8, 30, 15, 100);

        LocalDateTime sameDate = dt.withDate(2021, 4, 10);
        assertSame(dt, sameDate);

        LocalDateTime newDate = dt.withDate(2025, 12, 25);
        assertEquals(2025, newDate.getYear());
        assertEquals(12, newDate.getMonthOfYear());
        assertEquals(25, newDate.getDayOfMonth());
        assertEquals(8, newDate.getHourOfDay());

        LocalDateTime sameTime = dt.withTime(8, 30, 15, 100);
        assertSame(dt, sameTime);

        LocalDateTime newTime = dt.withTime(22, 11, 5, 999);
        assertEquals(2021, newTime.getYear());
        assertEquals(22, newTime.getHourOfDay());
        assertEquals(11, newTime.getMinuteOfHour());
        assertEquals(5, newTime.getSecondOfMinute());
        assertEquals(999, newTime.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testWithFieldAndWithFieldAdded() {
        LocalDateTime dt = new LocalDateTime(2021, 4, 10, 8, 30, 15, 100);

        LocalDateTime dtYear = dt.withField(DateTimeFieldType.year(), 2010);
        assertEquals(2010, dtYear.getYear());

        LocalDateTime dtSameYear = dt.withField(DateTimeFieldType.year(), 2021);
        assertSame(dt, dtSameYear);

        LocalDateTime dtAdded = dt.withFieldAdded(DurationFieldType.years(), 5);
        assertEquals(2026, dtAdded.getYear());

        LocalDateTime dtAddedZero = dt.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(dt, dtAddedZero);
    }

    @Test(timeout = 4000)
    public void testWithFields() {
        LocalDateTime dt = new LocalDateTime(2021, 4, 10, 8, 30, 15, 100);
        assertSame(dt, dt.withFields(null));

        LocalDate date = new LocalDate(2023, 7, 19);
        LocalDateTime changed = dt.withFields(date);
        assertEquals(2023, changed.getYear());
        assertEquals(7, changed.getMonthOfYear());
        assertEquals(19, changed.getDayOfMonth());
        assertEquals(8, changed.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testPlusMinusPeriodAndDuration() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 1, 12, 0, 0, 0);

        assertSame(dt, dt.plus((ReadableDuration) null));
        assertSame(dt, dt.minus((ReadableDuration) null));
        assertSame(dt, dt.withDurationAdded(new Duration(1000L), 0));

        Duration dur1Hour = new Duration(3600000L);
        LocalDateTime plusDur = dt.plus(dur1Hour);
        assertEquals(13, plusDur.getHourOfDay());
        LocalDateTime minusDur = dt.minus(dur1Hour);
        assertEquals(11, minusDur.getHourOfDay());

        assertSame(dt, dt.plus((ReadablePeriod) null));
        assertSame(dt, dt.minus((ReadablePeriod) null));
        assertSame(dt, dt.withPeriodAdded(Period.days(1), 0));

        Period period2Days = Period.days(2);
        LocalDateTime plusPeriod = dt.plus(period2Days);
        assertEquals(3, plusPeriod.getDayOfMonth());
        LocalDateTime minusPeriod = dt.minus(period2Days);
        assertEquals(30, minusPeriod.getDayOfMonth());
        assertEquals(2019, minusPeriod.getYear());
    }

    @Test(timeout = 4000)
    public void testPlusMinusIndividualUnits() {
        LocalDateTime dt = new LocalDateTime(2020, 2, 28, 10, 20, 30, 400);

        assertSame(dt, dt.plusYears(0));
        assertEquals(2021, dt.plusYears(1).getYear());
        assertSame(dt, dt.minusYears(0));
        assertEquals(2019, dt.minusYears(1).getYear());

        assertSame(dt, dt.plusMonths(0));
        assertEquals(3, dt.plusMonths(1).getMonthOfYear());
        assertSame(dt, dt.minusMonths(0));
        assertEquals(1, dt.minusMonths(1).getMonthOfYear());

        assertSame(dt, dt.plusWeeks(0));
        assertEquals(6, dt.plusWeeks(1).getDayOfMonth());
        assertSame(dt, dt.minusWeeks(0));
        assertEquals(21, dt.minusWeeks(1).getDayOfMonth());

        assertSame(dt, dt.plusDays(0));
        assertEquals(29, dt.plusDays(1).getDayOfMonth()); // Leap year 2020
        assertSame(dt, dt.minusDays(0));
        assertEquals(27, dt.minusDays(1).getDayOfMonth());

        assertSame(dt, dt.plusHours(0));
        assertEquals(12, dt.plusHours(2).getHourOfDay());
        assertSame(dt, dt.minusHours(0));
        assertEquals(8, dt.minusHours(2).getHourOfDay());

        assertSame(dt, dt.plusMinutes(0));
        assertEquals(35, dt.plusMinutes(15).getMinuteOfHour());
        assertSame(dt, dt.minusMinutes(0));
        assertEquals(5, dt.minusMinutes(15).getMinuteOfHour());

        assertSame(dt, dt.plusSeconds(0));
        assertEquals(40, dt.plusSeconds(10).getSecondOfMinute());
        assertSame(dt, dt.minusSeconds(0));
        assertEquals(20, dt.minusSeconds(10).getSecondOfMinute());

        assertSame(dt, dt.plusMillis(0));
        assertEquals(500, dt.plusMillis(100).getMillisOfSecond());
        assertSame(dt, dt.minusMillis(0));
        assertEquals(300, dt.minusMillis(100).getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testWithIndividualUnits() {
        LocalDateTime dt = new LocalDateTime(2020, 5, 10, 15, 30, 45, 500);

        assertEquals(1, dt.withEra(1).getEra());
        assertEquals(21, dt.withCenturyOfEra(21).getCenturyOfEra());
        assertEquals(2025, dt.withYearOfEra(2025).getYearOfEra());
        assertEquals(35, dt.withYearOfCentury(35).getYearOfCentury());
        assertEquals(2019, dt.withYear(2019).getYear());
        assertEquals(2022, dt.withWeekyear(2022).getWeekyear());
        assertEquals(11, dt.withMonthOfYear(11).getMonthOfYear());
        assertEquals(10, dt.withWeekOfWeekyear(10).getWeekOfWeekyear());
        assertEquals(200, dt.withDayOfYear(200).getDayOfYear());
        assertEquals(25, dt.withDayOfMonth(25).getDayOfMonth());
        assertEquals(3, dt.withDayOfWeek(3).getDayOfWeek());
        assertEquals(8, dt.withHourOfDay(8).getHourOfDay());
        assertEquals(50, dt.withMinuteOfHour(50).getMinuteOfHour());
        assertEquals(12, dt.withSecondOfMinute(12).getSecondOfMinute());
        assertEquals(888, dt.withMillisOfSecond(888).getMillisOfSecond());
        assertEquals(1000, dt.withMillisOfDay(1000).getMillisOfDay());
    }

    @Test(timeout = 4000)
    public void testConversions() {
        LocalDateTime dt = new LocalDateTime(2021, 6, 15, 14, 30, 45, 123);

        LocalDate ld = dt.toLocalDate();
        assertEquals(new LocalDate(2021, 6, 15), ld);

        LocalTime lt = dt.toLocalTime();
        assertEquals(new LocalTime(14, 30, 45, 123), lt);

        DateTime dateTimeUtc = dt.toDateTime(DateTimeZone.UTC);
        assertEquals(dt.getYear(), dateTimeUtc.getYear());
        assertEquals(dt.getMonthOfYear(), dateTimeUtc.getMonthOfYear());
        assertEquals(dt.getDayOfMonth(), dateTimeUtc.getDayOfMonth());
        assertEquals(dt.getHourOfDay(), dateTimeUtc.getHourOfDay());
        assertEquals(DateTimeZone.UTC, dateTimeUtc.getZone());

        DateTime dateTimeDefault = dt.toDateTime();
        assertEquals(dt.getYear(), dateTimeDefault.getYear());

        Date javaDate = dt.toDate();
        assertEquals(dt.getYear(), javaDate.getYear() + 1900);
        assertEquals(dt.getMonthOfYear(), javaDate.getMonth() + 1);
        assertEquals(dt.getDayOfMonth(), javaDate.getDate());
        assertEquals(dt.getHourOfDay(), javaDate.getHours());
        assertEquals(dt.getMinuteOfHour(), javaDate.getMinutes());
        assertEquals(dt.getSecondOfMinute(), javaDate.getSeconds());
    }

    @Test(timeout = 4000)
    public void testToStringAndFormatting() {
        LocalDateTime dt = new LocalDateTime(2022, 10, 5, 8, 9, 7, 6);
        assertEquals("2022-10-05T08:09:07.006", dt.toString());
        assertEquals("2022-10-05T08:09:07.006", dt.toString((String) null));
        assertEquals("2022-10-05T08:09:07.006", dt.toString((String) null, Locale.ENGLISH));

        assertEquals("2022/10/05", dt.toString("yyyy/MM/dd"));
        assertEquals("05-Oct-2022", dt.toString("dd-MMM-yyyy", Locale.ENGLISH));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPropertyMethods() {
        LocalDateTime dt = new LocalDateTime(2021, 5, 15, 12, 30, 45, 500);

        LocalDateTime.Property propDay = dt.dayOfMonth();
        assertNotNull(propDay);
        assertEquals(15, propDay.get());
        assertEquals("15", propDay.getAsText());
        assertEquals("15", propDay.getAsShortText());
        assertEquals(dt, propDay.getLocalDateTime());
        assertEquals(dt.getLocalMillis(), propDay.getMillis());
        assertEquals(dt.getChronology(), propDay.getChronology());
        assertEquals(DateTimeFieldType.dayOfMonth(), propDay.getField().getType());

        assertEquals(16, propDay.addToCopy(1).getDayOfMonth());
        assertEquals(17, propDay.addToCopy(2L).getDayOfMonth());
        assertEquals(1, dt.withDayOfMonth(31).dayOfMonth().addWrapFieldToCopy(1).getDayOfMonth());

        assertEquals(20, propDay.setCopy(20).getDayOfMonth());
        assertEquals(22, propDay.setCopy("22").getDayOfMonth());
        assertEquals(25, propDay.setCopy("25", Locale.US).getDayOfMonth());

        assertEquals(31, propDay.withMaximumValue().getDayOfMonth());
        assertEquals(1, propDay.withMinimumValue().getDayOfMonth());

        LocalDateTime.Property propHour = dt.hourOfDay();
        assertEquals(12, propHour.roundFloorCopy().getHourOfDay());
        assertEquals(0, propHour.roundFloorCopy().getMinuteOfHour());
        assertEquals(13, propHour.roundCeilingCopy().getHourOfDay());
        assertEquals(0, propHour.roundCeilingCopy().getMinuteOfHour());

        assertEquals(13, propHour.roundHalfFloorCopy().getHourOfDay());
        assertEquals(13, propHour.roundHalfCeilingCopy().getHourOfDay());
        assertEquals(12, propHour.roundHalfEvenCopy().getHourOfDay());

        assertNotNull(dt.era());
        assertNotNull(dt.centuryOfEra());
        assertNotNull(dt.yearOfCentury());
        assertNotNull(dt.yearOfEra());
        assertNotNull(dt.year());
        assertNotNull(dt.weekyear());
        assertNotNull(dt.monthOfYear());
        assertNotNull(dt.weekOfWeekyear());
        assertNotNull(dt.dayOfYear());
        assertNotNull(dt.dayOfWeek());
        assertNotNull(dt.minuteOfHour());
        assertNotNull(dt.secondOfMinute());
        assertNotNull(dt.millisOfSecond());
        assertNotNull(dt.millisOfDay());
    }

    @Test(timeout = 4000)
    public void testToDateDSTGapAndOverlap() {
        // DST Gap test using America/New_York (Spring forward: 2021-03-14 02:30:00 does not exist)
        TimeZone origTz = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            LocalDateTime gapLdt = new LocalDateTime(2021, 3, 14, 2, 30, 0, 0);
            Date gapDate = gapLdt.toDate();
            assertNotNull(gapDate);

            // DST Overlap test (Fall back: 2021-11-07 01:30:00 occurs twice)
            LocalDateTime overlapLdt = new LocalDateTime(2021, 11, 7, 1, 30, 0, 0);
            Date overlapDate = overlapLdt.toDate();
            assertNotNull(overlapDate);
        } finally {
            TimeZone.setDefault(origTz);
        }
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNowNullZone() {
        LocalDateTime.now((DateTimeZone) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNowNullChronology() {
        LocalDateTime.now((Chronology) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFromCalendarFieldsNull() {
        LocalDateTime.fromCalendarFields(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFromDateFieldsNull() {
        LocalDateTime.fromDateFields(null);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueNegativeIndex() {
        LocalDateTime dt = new LocalDateTime();
        dt.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueTooLargeIndex() {
        LocalDateTime dt = new LocalDateTime();
        dt.getValue(4);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldInvalidIndex() {
        LocalDateTime dt = new LocalDateTime();
        dt.getField(4, ISOChronology.getInstanceUTC());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetNullFieldType() {
        LocalDateTime dt = new LocalDateTime();
        dt.get(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldNullType() {
        LocalDateTime dt = new LocalDateTime();
        dt.withField(null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAddedNullType() {
        LocalDateTime dt = new LocalDateTime();
        dt.withFieldAdded(null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyNullFieldType() {
        LocalDateTime dt = new LocalDateTime();
        dt.property(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyUnsupportedFieldType() {
        LocalDateTime dt = new LocalDateTime();
        DateTimeFieldType unsupported = new DateTimeFieldType("dummy") {
            private static final long serialVersionUID = 1L;
            public DurationFieldType getDurationType() { return DurationFieldType.days(); }
            public DurationFieldType getRangeDurationType() { return null; }
            public DateTimeField getField(Chronology chronology) {
                return org.joda.time.field.UnsupportedDateTimeField.getInstance(this, UnsupportedDurationField.getInstance(getDurationType()));
            }
        };
        dt.property(unsupported);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        LocalDateTime dt1 = new LocalDateTime(2021, 5, 10, 12, 0, 0, 0);
        LocalDateTime dt2 = new LocalDateTime(2021, 5, 10, 12, 0, 0, 0);
        LocalDateTime dtDiffMillis = new LocalDateTime(2021, 5, 10, 12, 0, 0, 1);
        LocalDateTime dtDiffChrono = new LocalDateTime(2021, 5, 10, 12, 0, 0, 0, GregorianChronology.getInstanceUTC());

        assertEquals(dt1, dt1);
        assertEquals(dt1, dt2);
        assertEquals(dt1.hashCode(), dt2.hashCode());

        assertFalse(dt1.equals(null));
        assertFalse(dt1.equals("StringInstant"));
        assertFalse(dt1.equals(dtDiffMillis));
        assertFalse(dt1.equals(dtDiffChrono));
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        LocalDateTime dt1 = new LocalDateTime(2021, 5, 10, 12, 0, 0, 0);
        LocalDateTime dt2 = new LocalDateTime(2021, 5, 10, 12, 0, 0, 0);
        LocalDateTime dtEarlier = new LocalDateTime(2021, 5, 10, 11, 59, 59, 999);
        LocalDateTime dtLater = new LocalDateTime(2021, 5, 10, 12, 0, 0, 1);

        assertEquals(0, dt1.compareTo(dt1));
        assertEquals(0, dt1.compareTo(dt2));
        assertTrue(dt1.compareTo(dtEarlier) > 0);
        assertTrue(dt1.compareTo(dtLater) < 0);

        // Different chronology comparison invokes super.compareTo
        LocalDateTime dtChrono = new LocalDateTime(2021, 5, 10, 12, 0, 0, 0, GregorianChronology.getInstanceUTC());
        assertEquals(0, dt1.compareTo(dtChrono));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        LocalDateTime original = new LocalDateTime(2023, 8, 22, 16, 45, 10, 250);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDateTime deserialized = (LocalDateTime) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getChronology(), deserialized.getChronology());
    }

    @Test(timeout = 4000)
    public void testPropertySerialization() throws Exception {
        LocalDateTime dt = new LocalDateTime(2023, 8, 22, 16, 45, 10, 250);
        LocalDateTime.Property originalProp = dt.dayOfMonth();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalProp);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDateTime.Property deserializedProp = (LocalDateTime.Property) ois.readObject();
        ois.close();

        assertEquals(originalProp.get(), deserializedProp.get());
        assertEquals(originalProp.getField().getType(), deserializedProp.getField().getType());
        assertEquals(originalProp.getLocalDateTime(), deserializedProp.getLocalDateTime());
    }

    @Test(timeout = 4000)
    public void testReadResolveNonUTCZoneChronology() throws Exception {
        // Construct LocalDateTime with a non-UTC chronology to exercise readResolve branch
        Chronology nonUtcChrono = GregorianChronology.getInstance(DateTimeZone.forOffsetHours(2));
        LocalDateTime dt = new LocalDateTime(100000L, nonUtcChrono);
        assertEquals(DateTimeZone.UTC, dt.getChronology().getZone());
    }
}