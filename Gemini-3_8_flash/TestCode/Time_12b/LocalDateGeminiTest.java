/*
 *  Copyright 2001-2011 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Targeted Defects (Defects4J ground truth):
 *    - LocalDate.fromCalendarFields(Calendar) does not correctly handle BC era years (e.g. 1 BC -> year 0, 3 BC -> year -2).
 *    - LocalDate.fromDateFields(Date) does not correctly handle BC era years.
 *    Targeted Tests:
 *    - testFactory_fromCalendarFields_beforeYearZero1()
 *    - testFactory_fromCalendarFields_beforeYearZero3()
 *    - testFactory_fromDateFields_beforeYearZero1()
 *    - testFactory_fromDateFields_beforeYearZero3()
 *
 * 2. Coverage Dimensions:
 *    - Partition A: Core Functional Logic & State Transitions:
 *      * Field getters: getEra, getCenturyOfEra, getYearOfCentury, getYearOfEra, getYear, getWeekyear,
 *        getMonthOfYear, getWeekOfWeekyear, getDayOfYear, getDayOfMonth, getDayOfWeek.
 *      * Transformations: plus/minus Years, Months, Weeks, Days; withEra, withCenturyOfEra, withYear,
 *        withMonthOfYear, withDayOfMonth, withDayOfWeek, withDayOfYear, withWeekyear, withWeekOfWeekyear.
 *      * Property methods: addToCopy, addWrapFieldToCopy, setCopy(int), setCopy(String), setCopy(String, Locale),
 *        withMaximumValue, withMinimumValue, roundFloorCopy, roundCeilingCopy, roundHalfFloorCopy,
 *        roundHalfCeilingCopy, roundHalfEvenCopy.
 *      * Conversions: toDateTimeAtStartOfDay, toDateTimeAtMidnight, toDateTimeAtCurrentTime, toDateMidnight,
 *        toLocalDateTime, toDateTime(LocalTime, DateTimeZone), toInterval, toDate, toString formats.
 *    - Partition B: Boundary Value Analysis (BVA):
 *      * Zero additions/subtractions returning 'this' (plusDays(0), plusMonths(0), etc.).
 *      * Leap years, end-of-month clamping (e.g. Jan 31 + 1 month = Feb 28/29).
 *      * Millis floor rounding invariants in withLocalMillis.
 *    - Partition C: Null & Exception Guards:
 *      * Null calendar/date in factories, null types in get/isSupported/withField/property.
 *      * Unsupported DateTimeFieldType / DurationFieldType handling.
 *      * Size and IndexOutOfBoundsException in getField / getValue.
 *      * Mismatched chronologies in toLocalDateTime and toDateTime.
 *    - Partition D: Object Lifecycle & Contract Integrity:
 *      * equals (reflexive, symmetric, false against non-LocalDate, diff millis, diff chrono).
 *      * hashCode consistency and caching.
 *      * compareTo contract compliance (<, ==, >).
 *      * Java Serialization roundtrip for LocalDate and LocalDate.Property.
 *      * readResolve handling of non-UTC or null chronology deserialization.
 */
public class LocalDateGeminiTest {

    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED ZONE (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactory_fromCalendarFields_beforeYearZero1() {
        // Year 1 BC in GregorianCalendar corresponds to year 0 in ISO chronology
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);

        LocalDate expected = new LocalDate(0, 2, 3);
        LocalDate actual = LocalDate.fromCalendarFields(cal);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testFactory_fromCalendarFields_beforeYearZero3() {
        // Year 3 BC in GregorianCalendar corresponds to year -2 in ISO chronology
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 3);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);

        LocalDate expected = new LocalDate(-2, 2, 3);
        LocalDate actual = LocalDate.fromCalendarFields(cal);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testFactory_fromDateFields_beforeYearZero1() {
        // Year 1 BC in Date/Calendar corresponds to year 0 in ISO chronology
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);

        LocalDate expected = new LocalDate(0, 2, 3);
        LocalDate actual = LocalDate.fromDateFields(cal.getTime());
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testFactory_fromDateFields_beforeYearZero3() {
        // Year 3 BC in Date/Calendar corresponds to year -2 in ISO chronology
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 3);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);

        LocalDate expected = new LocalDate(-2, 2, 3);
        LocalDate actual = LocalDate.fromDateFields(cal.getTime());
        assertEquals(expected, actual);
    }

    // =========================================================================
    // PARTITION A: CONSTRUCTORS & FACTORIES
    // =========================================================================

    @Test(timeout = 4000)
    public void testNowFactories() {
        assertNotNull(LocalDate.now());
        assertNotNull(LocalDate.now(DateTimeZone.UTC));
        assertNotNull(LocalDate.now(ISOChronology.getInstance()));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNow_nullZoneThrows() {
        LocalDate.now((DateTimeZone) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNow_nullChronologyThrows() {
        LocalDate.now((Chronology) null);
    }

    @Test(timeout = 4000)
    public void testParseString() {
        LocalDate date = LocalDate.parse("2021-10-15");
        assertEquals(2021, date.getYear());
        assertEquals(10, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
        LocalDate date2 = LocalDate.parse("15/10/2021", dtf);
        assertEquals(date, date2);
    }

    @Test(timeout = 4000)
    public void testFromCalendarFields_AD() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.MARCH, 25);
        LocalDate date = LocalDate.fromCalendarFields(cal);
        assertEquals(new LocalDate(2023, 3, 25), date);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFromCalendarFields_nullThrows() {
        LocalDate.fromCalendarFields(null);
    }

    @Test(timeout = 4000)
    public void testFromDateFields_AD() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.MARCH, 25);
        LocalDate date = LocalDate.fromDateFields(cal.getTime());
        assertEquals(new LocalDate(2023, 3, 25), date);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFromDateFields_nullThrows() {
        LocalDate.fromDateFields(null);
    }

    @Test(timeout = 4000)
    public void testConstructors_NoArgAndZoneChrono() {
        LocalDate date1 = new LocalDate();
        assertNotNull(date1);

        LocalDate date2 = new LocalDate(PARIS);
        assertNotNull(date2);

        LocalDate date3 = new LocalDate((DateTimeZone) null);
        assertNotNull(date3);

        LocalDate date4 = new LocalDate(ISOChronology.getInstance());
        assertNotNull(date4);

        LocalDate date5 = new LocalDate((Chronology) null);
        assertNotNull(date5);
    }

    @Test(timeout = 4000)
    public void testConstructors_Instant() {
        long millis = 1500000000000L;
        LocalDate date1 = new LocalDate(millis);
        assertNotNull(date1);

        LocalDate date2 = new LocalDate(millis, LONDON);
        assertNotNull(date2);

        LocalDate date3 = new LocalDate(millis, (DateTimeZone) null);
        assertNotNull(date3);

        LocalDate date4 = new LocalDate(millis, CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), date4.getChronology());

        LocalDate date5 = new LocalDate(millis, (Chronology) null);
        assertEquals(ISOChronology.getInstanceUTC(), date5.getChronology());
    }

    @Test(timeout = 4000)
    public void testConstructors_Object() {
        LocalDate date1 = new LocalDate("2020-04-12");
        assertEquals(new LocalDate(2020, 4, 12), date1);

        LocalDate date2 = new LocalDate("2020-04-12", PARIS);
        assertEquals(new LocalDate(2020, 4, 12), date2);

        LocalDate date3 = new LocalDate("2020-04-12", (DateTimeZone) null);
        assertEquals(new LocalDate(2020, 4, 12), date3);

        LocalDate date4 = new LocalDate("2020-04-12", ISOChronology.getInstance());
        assertEquals(new LocalDate(2020, 4, 12), date4);

        LocalDate date5 = new LocalDate("2020-04-12", (Chronology) null);
        assertEquals(new LocalDate(2020, 4, 12), date5);
    }

    @Test(timeout = 4000)
    public void testConstructors_YMDAndChrono() {
        LocalDate date1 = new LocalDate(2015, 6, 20);
        assertEquals(2015, date1.getYear());
        assertEquals(6, date1.getMonthOfYear());
        assertEquals(20, date1.getDayOfMonth());

        LocalDate date2 = new LocalDate(2015, 6, 20, null);
        assertEquals(ISOChronology.getInstanceUTC(), date2.getChronology());

        LocalDate date3 = new LocalDate(2015, 6, 20, BuddhistChronology.getInstance());
        assertEquals(BuddhistChronology.getInstanceUTC(), date3.getChronology());
    }

    // =========================================================================
    // PARTITION B: READABLE PARTIAL IMPLEMENTATION & GETTERS
    // =========================================================================

    @Test(timeout = 4000)
    public void testSizeAndGetValues() {
        LocalDate date = new LocalDate(2022, 11, 28);
        assertEquals(3, date.size());
        assertEquals(2022, date.getValue(0));
        assertEquals(11, date.getValue(1));
        assertEquals(28, date.getValue(2));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValue_outOfBoundsLow() {
        new LocalDate(2022, 11, 28).getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValue_outOfBoundsHigh() {
        new LocalDate(2022, 11, 28).getValue(3);
    }

    @Test(timeout = 4000)
    public void testGetFieldProtected() {
        LocalDate date = new LocalDate(2022, 11, 28);
        Chronology chrono = ISOChronology.getInstanceUTC();
        assertEquals(chrono.year(), date.getField(0, chrono));
        assertEquals(chrono.monthOfYear(), date.getField(1, chrono));
        assertEquals(chrono.dayOfMonth(), date.getField(2, chrono));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldProtected_invalid() {
        LocalDate date = new LocalDate(2022, 11, 28);
        date.getField(3, ISOChronology.getInstanceUTC());
    }

    @Test(timeout = 4000)
    public void testFieldTypeQueries() {
        LocalDate date = new LocalDate(2020, 8, 15);
        assertEquals(2020, date.get(DateTimeFieldType.year()));
        assertEquals(8, date.get(DateTimeFieldType.monthOfYear()));
        assertEquals(15, date.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(6, date.get(DateTimeFieldType.dayOfWeek())); // Saturday
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetFieldType_nullThrows() {
        new LocalDate(2020, 8, 15).get(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetFieldType_unsupportedThrows() {
        new LocalDate(2020, 8, 15).get(DateTimeFieldType.hourOfDay());
    }

    @Test(timeout = 4000)
    public void testIsSupportedDateTimeFieldType() {
        LocalDate date = new LocalDate(2020, 8, 15);
        assertTrue(date.isSupported(DateTimeFieldType.year()));
        assertTrue(date.isSupported(DateTimeFieldType.monthOfYear()));
        assertTrue(date.isSupported(DateTimeFieldType.dayOfMonth()));
        assertTrue(date.isSupported(DateTimeFieldType.weekyear()));
        assertTrue(date.isSupported(DateTimeFieldType.dayOfWeek()));
        assertFalse(date.isSupported(DateTimeFieldType.minuteOfHour()));
        assertFalse(date.isSupported((DateTimeFieldType) null));
    }

    @Test(timeout = 4000)
    public void testIsSupportedDurationFieldType() {
        LocalDate date = new LocalDate(2020, 8, 15);
        assertTrue(date.isSupported(DurationFieldType.days()));
        assertTrue(date.isSupported(DurationFieldType.weeks()));
        assertTrue(date.isSupported(DurationFieldType.months()));
        assertTrue(date.isSupported(DurationFieldType.years()));
        assertTrue(date.isSupported(DurationFieldType.centuries()));
        assertTrue(date.isSupported(DurationFieldType.eras()));
        assertFalse(date.isSupported(DurationFieldType.hours()));
        assertFalse(date.isSupported(DurationFieldType.seconds()));
        assertFalse(date.isSupported((DurationFieldType) null));
    }

    @Test(timeout = 4000)
    public void testSpecificFieldGetters() {
        LocalDate date = new LocalDate(2023, 7, 19);
        assertEquals(1, date.getEra());
        assertEquals(20, date.getCenturyOfEra());
        assertEquals(23, date.getYearOfCentury());
        assertEquals(2023, date.getYearOfEra());
        assertEquals(2023, date.getYear());
        assertEquals(2023, date.getWeekyear());
        assertEquals(7, date.getMonthOfYear());
        assertEquals(29, date.getWeekOfWeekyear());
        assertEquals(200, date.getDayOfYear());
        assertEquals(19, date.getDayOfMonth());
        assertEquals(3, date.getDayOfWeek()); // Wednesday
        assertTrue(date.getLocalMillis() != 0);
    }

    // =========================================================================
    // PARTITION D: IMMUTABLE MODIFICATIONS (with*, plus*, minus*)
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithFields_ReadablePartial() {
        LocalDate base = new LocalDate(2021, 5, 20);
        YearMonth ym = new YearMonth(2025, 9);
        LocalDate updated = base.withFields(ym);
        assertEquals(new LocalDate(2025, 9, 20), updated);

        assertSame(base, base.withFields(null));
    }

    @Test(timeout = 4000)
    public void testWithField_and_withFieldAdded() {
        LocalDate date = new LocalDate(2021, 5, 20);

        LocalDate updated = date.withField(DateTimeFieldType.monthOfYear(), 12);
        assertEquals(new LocalDate(2021, 12, 20), updated);

        LocalDate added = date.withFieldAdded(DurationFieldType.months(), 2);
        assertEquals(new LocalDate(2021, 7, 20), added);

        assertSame(date, date.withFieldAdded(DurationFieldType.months(), 0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithField_nullFieldThrows() {
        new LocalDate(2021, 5, 20).withField(null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithField_unsupportedFieldThrows() {
        new LocalDate(2021, 5, 20).withField(DateTimeFieldType.hourOfDay(), 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAdded_nullFieldThrows() {
        new LocalDate(2021, 5, 20).withFieldAdded(null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAdded_unsupportedFieldThrows() {
        new LocalDate(2021, 5, 20).withFieldAdded(DurationFieldType.hours(), 1);
    }

    @Test(timeout = 4000)
    public void testWithPeriodAdded() {
        LocalDate date = new LocalDate(2021, 1, 31);
        Period p = Period.months(1);
        LocalDate res = date.withPeriodAdded(p, 1);
        assertEquals(new LocalDate(2021, 2, 28), res);

        assertSame(date, date.withPeriodAdded(null, 1));
        assertSame(date, date.withPeriodAdded(p, 0));
    }

    @Test(timeout = 4000)
    public void testPlusMinusPeriod() {
        LocalDate date = new LocalDate(2021, 1, 15);
        Period p = Period.days(10);
        assertEquals(new LocalDate(2021, 1, 25), date.plus(p));
        assertEquals(new LocalDate(2021, 1, 5), date.minus(p));

        assertSame(date, date.plus(null));
        assertSame(date, date.minus(null));
    }

    @Test(timeout = 4000)
    public void testPlusMinusUnits() {
        LocalDate date = new LocalDate(2020, 2, 29); // Leap year

        // Plus
        assertSame(date, date.plusYears(0));
        assertEquals(new LocalDate(2021, 2, 28), date.plusYears(1));

        assertSame(date, date.plusMonths(0));
        assertEquals(new LocalDate(2020, 3, 29), date.plusMonths(1));

        assertSame(date, date.plusWeeks(0));
        assertEquals(new LocalDate(2020, 3, 7), date.plusWeeks(1));

        assertSame(date, date.plusDays(0));
        assertEquals(new LocalDate(2020, 3, 1), date.plusDays(1));

        // Minus
        assertSame(date, date.minusYears(0));
        assertEquals(new LocalDate(2019, 2, 28), date.minusYears(1));

        assertSame(date, date.minusMonths(0));
        assertEquals(new LocalDate(2020, 1, 29), date.minusMonths(1));

        assertSame(date, date.minusWeeks(0));
        assertEquals(new LocalDate(2020, 2, 22), date.minusWeeks(1));

        assertSame(date, date.minusDays(0));
        assertEquals(new LocalDate(2020, 2, 28), date.minusDays(1));
    }

    @Test(timeout = 4000)
    public void testWithSpecificFieldMethods() {
        LocalDate date = new LocalDate(2021, 6, 15);

        assertEquals(new LocalDate(2021, 6, 15), date.withEra(1));
        assertEquals(new LocalDate(1921, 6, 15), date.withCenturyOfEra(19));
        assertEquals(new LocalDate(2050, 6, 15), date.withYearOfCentury(50));
        assertEquals(2050, date.withYearOfEra(2050).getYear());
        assertEquals(2030, date.withYear(2030).getYear());
        assertEquals(2022, date.withWeekyear(2022).getWeekyear());
        assertEquals(11, date.withMonthOfYear(11).getMonthOfYear());
        assertEquals(10, date.withWeekOfWeekyear(10).getWeekOfWeekyear());
        assertEquals(100, date.withDayOfYear(100).getDayOfYear());
        assertEquals(25, date.withDayOfMonth(25).getDayOfMonth());
        assertEquals(7, date.withDayOfWeek(7).getDayOfWeek());
    }

    // =========================================================================
    // PARTITION E: CONVERSIONS TO OTHER JODA TYPES & JDK TYPES
    // =========================================================================

    @Test(timeout = 4000)
    public void testToDateTimeAtStartOfDay() {
        LocalDate date = new LocalDate(2021, 5, 1);
        DateTime dtDefault = date.toDateTimeAtStartOfDay();
        assertEquals(2021, dtDefault.getYear());
        assertEquals(5, dtDefault.getMonthOfYear());
        assertEquals(1, dtDefault.getDayOfMonth());

        DateTime dtZone = date.toDateTimeAtStartOfDay(PARIS);
        assertEquals(PARIS, dtZone.getZone());
        assertEquals(2021, dtZone.getYear());
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testToDateTimeAtMidnight() {
        LocalDate date = new LocalDate(2021, 5, 1);
        DateTime dt = date.toDateTimeAtMidnight();
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());

        DateTime dtZone = date.toDateTimeAtMidnight(LONDON);
        assertEquals(LONDON, dtZone.getZone());
    }

    @Test(timeout = 4000)
    public void testToDateTimeAtCurrentTime() {
        LocalDate date = new LocalDate(2021, 5, 1);
        DateTime dt = date.toDateTimeAtCurrentTime();
        assertEquals(2021, dt.getYear());
        assertEquals(5, dt.getMonthOfYear());
        assertEquals(1, dt.getDayOfMonth());

        DateTime dtZone = date.toDateTimeAtCurrentTime(PARIS);
        assertEquals(PARIS, dtZone.getZone());
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testToDateMidnight() {
        LocalDate date = new LocalDate(2021, 5, 1);
        DateMidnight dm = date.toDateMidnight();
        assertEquals(2021, dm.getYear());
        assertEquals(5, dm.getMonthOfYear());
        assertEquals(1, dm.getDayOfMonth());

        DateMidnight dmZone = date.toDateMidnight(LONDON);
        assertEquals(LONDON, dmZone.getZone());
    }

    @Test(timeout = 4000)
    public void testToLocalDateTime() {
        LocalDate date = new LocalDate(2021, 5, 1);
        LocalTime time = new LocalTime(14, 30, 15);
        LocalDateTime ldt = date.toLocalDateTime(time);
        assertEquals(2021, ldt.getYear());
        assertEquals(5, ldt.getMonthOfYear());
        assertEquals(1, ldt.getDayOfMonth());
        assertEquals(14, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(15, ldt.getSecondOfMinute());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocalDateTime_nullThrows() {
        new LocalDate(2021, 5, 1).toLocalDateTime(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocalDateTime_mismatchedChronoThrows() {
        LocalDate date = new LocalDate(2021, 5, 1, ISOChronology.getInstanceUTC());
        LocalTime time = new LocalTime(14, 30, CopticChronology.getInstanceUTC());
        date.toLocalDateTime(time);
    }

    @Test(timeout = 4000)
    public void testToDateTime_LocalTime() {
        LocalDate date = new LocalDate(2021, 5, 1);
        LocalTime time = new LocalTime(10, 20);
        DateTime dt = date.toDateTime(time);
        assertEquals(2021, dt.getYear());
        assertEquals(10, dt.getHourOfDay());

        DateTime dtZone = date.toDateTime(time, PARIS);
        assertEquals(PARIS, dtZone.getZone());
        assertEquals(10, dtZone.getHourOfDay());

        DateTime dtNullTime = date.toDateTime((LocalTime) null, PARIS);
        assertEquals(2021, dtNullTime.getYear());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToDateTime_mismatchedChronoThrows() {
        LocalDate date = new LocalDate(2021, 5, 1, ISOChronology.getInstanceUTC());
        LocalTime time = new LocalTime(14, 30, CopticChronology.getInstanceUTC());
        date.toDateTime(time, PARIS);
    }

    @Test(timeout = 4000)
    public void testToInterval() {
        LocalDate date = new LocalDate(2021, 5, 1);
        Interval interval = date.toInterval();
        assertEquals(date.toDateTimeAtStartOfDay(), interval.getStart());
        assertEquals(date.plusDays(1).toDateTimeAtStartOfDay(), interval.getEnd());

        Interval intervalZone = date.toInterval(LONDON);
        assertEquals(date.toDateTimeAtStartOfDay(LONDON), intervalZone.getStart());
        assertEquals(date.plusDays(1).toDateTimeAtStartOfDay(LONDON), intervalZone.getEnd());
    }

    @Test(timeout = 4000)
    public void testToDate() {
        LocalDate date = new LocalDate(2021, 5, 1);
        Date d = date.toDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        assertEquals(2021, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        LocalDate date = new LocalDate(2021, 5, 1);
        assertEquals("2021-05-01", date.toString());
        assertEquals("01/05/2021", date.toString("dd/MM/yyyy"));
        assertEquals("2021-05-01", date.toString(null));
        assertEquals("2021-05-01", date.toString(null, Locale.FRANCE));
        assertTrue(date.toString("MMMM", Locale.ENGLISH).equalsIgnoreCase("May"));
    }

    // =========================================================================
    // PARTITION F: PROPERTY FUNCTIONALITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testPropertyGetters() {
        LocalDate date = new LocalDate(2021, 5, 15);
        assertNotNull(date.era());
        assertNotNull(date.centuryOfEra());
        assertNotNull(date.yearOfCentury());
        assertNotNull(date.yearOfEra());
        assertNotNull(date.year());
        assertNotNull(date.weekyear());
        assertNotNull(date.monthOfYear());
        assertNotNull(date.weekOfWeekyear());
        assertNotNull(date.dayOfYear());
        assertNotNull(date.dayOfMonth());
        assertNotNull(date.dayOfWeek());
    }

    @Test(timeout = 4000)
    public void testPropertyManipulations() {
        LocalDate date = new LocalDate(2021, 1, 31);
        LocalDate.Property prop = date.dayOfMonth();

        assertEquals(31, prop.get());
        assertEquals("31", prop.getAsText());
        assertEquals("31", prop.getAsShortText());
        assertEquals(date, prop.getLocalDate());
        assertEquals(date.getChronology(), prop.getChronology());
        assertNotNull(prop.getField());

        assertEquals(new LocalDate(2021, 1, 15), prop.setCopy(15));
        assertEquals(new LocalDate(2021, 1, 10), prop.setCopy("10"));
        assertEquals(new LocalDate(2021, 1, 10), prop.setCopy("10", Locale.ENGLISH));

        assertEquals(new LocalDate(2021, 2, 1), prop.addToCopy(1));
        assertEquals(new LocalDate(2021, 1, 1), prop.addWrapFieldToCopy(1));

        assertEquals(new LocalDate(2021, 1, 31), prop.withMaximumValue());
        assertEquals(new LocalDate(2021, 1, 1), prop.withMinimumValue());

        assertEquals(date, prop.roundFloorCopy());
        assertEquals(date, prop.roundCeilingCopy());
        assertEquals(date, prop.roundHalfFloorCopy());
        assertEquals(date, prop.roundHalfCeilingCopy());
        assertEquals(date, prop.roundHalfEvenCopy());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testProperty_nullFieldThrows() {
        new LocalDate(2021, 1, 1).property(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testProperty_unsupportedFieldThrows() {
        new LocalDate(2021, 1, 1).property(DateTimeFieldType.secondOfMinute());
    }

    // =========================================================================
    // PARTITION G: CONTRACT INTEGRITY (equals, hashCode, compareTo, serialization)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        LocalDate d1 = new LocalDate(2021, 5, 15);
        LocalDate d2 = new LocalDate(2021, 5, 15);
        LocalDate d3 = new LocalDate(2021, 5, 16);
        LocalDate d4 = new LocalDate(2021, 5, 15, BuddhistChronology.getInstanceUTC());

        // Reflexive
        assertTrue(d1.equals(d1));
        // Symmetric
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
        assertEquals(d1.hashCode(), d2.hashCode());

        // Different millis
        assertFalse(d1.equals(d3));
        // Different chronology
        assertFalse(d1.equals(d4));
        // Not a LocalDate
        assertFalse(d1.equals("2021-05-15"));
        assertFalse(d1.equals(null));

        // Super equals via partial
        Partial partial = new Partial()
            .with(DateTimeFieldType.year(), 2021)
            .with(DateTimeFieldType.monthOfYear(), 5)
            .with(DateTimeFieldType.dayOfMonth(), 15);
        assertTrue(d1.equals(partial));
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        LocalDate d1 = new LocalDate(2021, 5, 15);
        LocalDate d2 = new LocalDate(2021, 5, 15);
        LocalDate d3 = new LocalDate(2021, 5, 16);
        LocalDate d0 = new LocalDate(2021, 5, 14);

        assertEquals(0, d1.compareTo(d1));
        assertEquals(0, d1.compareTo(d2));
        assertTrue(d1.compareTo(d3) < 0);
        assertTrue(d1.compareTo(d0) > 0);

        Partial partial = new Partial()
            .with(DateTimeFieldType.year(), 2021)
            .with(DateTimeFieldType.monthOfYear(), 5)
            .with(DateTimeFieldType.dayOfMonth(), 15);
        assertEquals(0, d1.compareTo(partial));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testCompareTo_nullThrows() {
        new LocalDate(2021, 5, 15).compareTo(null);
    }

    @Test(timeout = 4000)
    public void testSerialization_LocalDate() throws Exception {
        LocalDate original = new LocalDate(2023, 11, 20);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDate deserialized = (LocalDate) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getChronology(), deserialized.getChronology());
    }

    @Test(timeout = 4000)
    public void testSerialization_Property() throws Exception {
        LocalDate date = new LocalDate(2023, 11, 20);
        LocalDate.Property prop = date.monthOfYear();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDate.Property deserialized = (LocalDate.Property) ois.readObject();
        ois.close();

        assertEquals(prop.get(), deserialized.get());
        assertEquals(prop.getLocalDate(), deserialized.getLocalDate());
        assertEquals(prop.getField().getType(), deserialized.getField().getType());
    }

    @Test(timeout = 4000)
    public void testReadResolve_ZoneConversion() {
        // Test internal readResolve by using non-UTC chronology
        LocalDate dateNonUtc = new LocalDate(2021, 5, 15, GJChronology.getInstance(LONDON));
        assertEquals(DateTimeZone.UTC, dateNonUtc.getChronology().getZone());
    }
}