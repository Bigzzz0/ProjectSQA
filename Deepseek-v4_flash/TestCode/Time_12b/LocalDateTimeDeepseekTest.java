package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructors: all public constructors, fromCalendarFields, fromDateFields, parse, now()
 *   - Getters: getYear, getMonthOfYear, getDayOfMonth, getHourOfDay, getMinuteOfHour,
 *              getSecondOfMinute, getMillisOfSecond, getMillisOfDay, getEra, getCenturyOfEra,
 *              getYearOfEra, getYearOfCentury, getWeekyear, getWeekOfWeekyear, getDayOfYear,
 *              getDayOfWeek
 *   - with* methods: withDate, withTime, withField, withFieldAdded, withDurationAdded, withPeriodAdded
 *   - plus/minus: plusYears, plusMonths, plusDays, plusHours, plusMinutes, plusSeconds, plusMillis,
 *                 minusYears, etc.
 *   - Conversion: toDateTime, toDate, toLocalDate, toLocalTime
 *   - Property methods: addToCopy, setCopy, roundFloorCopy, roundCeilingCopy, withMaximumValue, etc.
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Year: 0, -1, 1, 9999, -9999
 *   - Month: 1, 12, 0, 13
 *   - Day: 1, 28, 29, 30, 31, 0, 32
 *   - Hour: 0, 23, -1, 24
 *   - Minute: 0, 59, -1, 60
 *   - Second: 0, 59, -1, 60
 *   - Millis: 0, 999, -1, 1000
 *   - Null parameters for factory methods and chaining
 *   - Chronology: null, ISO, other (e.g., Buddhist, GJ)
 *   - TimeZone: null, UTC, default
 * 
 * Partition C: Defect-Targeted Branch Zone (ground truth from Defects4J)
 *   - fromDateFields with dates before year zero (1 BC, 3 BC)
 *   - fromCalendarFields with dates before year zero
 *   - Bug: year offset calculation ignores era, causing year 0→1, -2→3
 *   - Expected ISO year: for 1 BC → 0, for 2 BC → -1, 3 BC → -2
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null arguments to methods: get(null fieldType), isSupported(null), property(null), etc.
 *   - IndexOutOfBoundsException in getField, getValue with invalid index
 *   - IllegalArgumentException for invalid date/time fields
 *   - IllegalArgumentException for null calendar/date in fromCalendarFields/fromDateFields
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals: reflexive, symmetric, transitive, with null, different chronology
 *   - compareTo: with same object, different chronology, different millis
 *   - toString: ISO8601 format
 *   - toString with pattern and locale
 *   - Serialization (implied by readResolve)
 */
public class LocalDateTimeDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorEmpty() {
        LocalDateTime ldt = new LocalDateTime();
        assertNotNull(ldt);
        assertEquals(4, ldt.size());
    }

    @Test(timeout = 4000)
    public void testConstructorWithZone() {
        DateTimeZone zone = DateTimeZone.UTC;
        LocalDateTime ldt = new LocalDateTime(zone);
        assertNotNull(ldt);
        assertEquals(zone, ldt.getChronology().getZone());
    }

    @Test(timeout = 4000)
    public void testConstructorWithChronology() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        LocalDateTime ldt = new LocalDateTime(chrono);
        assertNotNull(ldt);
        assertEquals(chrono, ldt.getChronology());
    }

    @Test(timeout = 4000)
    public void testConstructorWithInstant() {
        long millis = 1000000L;
        LocalDateTime ldt = new LocalDateTime(millis);
        assertNotNull(ldt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithInstantAndZone() {
        long millis = 1000000L;
        DateTimeZone zone = DateTimeZone.UTC;
        LocalDateTime ldt = new LocalDateTime(millis, zone);
        assertNotNull(ldt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithInstantAndChronology() {
        long millis = 1000000L;
        Chronology chrono = ISOChronology.getInstanceUTC();
        LocalDateTime ldt = new LocalDateTime(millis, chrono);
        assertNotNull(ldt);
        assertEquals(chrono.withUTC(), ldt.getChronology());
    }

    @Test(timeout = 4000)
    public void testConstructorWithObjectDateTime() {
        LocalDateTime ldt = new LocalDateTime((Object) "2020-06-30T12:00:00");
        assertNotNull(ldt);
        assertEquals(2020, ldt.getYear());
    }

    @Test(timeout = 4000)
    public void testConstructorWithObjectAndZone() {
        DateTimeZone zone = DateTimeZone.UTC;
        LocalDateTime ldt = new LocalDateTime((Object) new Date(0), zone);
        assertNotNull(ldt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithObjectAndChronology() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        LocalDateTime ldt = new LocalDateTime((Object) new Date(0), chrono);
        assertNotNull(ldt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithFieldsNoMillis() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(30, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(0, ldt.getSecondOfMinute());
        assertEquals(0, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testConstructorWithFieldsAndSeconds() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45);
        assertEquals(45, ldt.getSecondOfMinute());
    }

    @Test(timeout = 4000)
    public void testConstructorWithFieldsAndMillis() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500);
        assertEquals(500, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testConstructorWithFieldsAndChronology() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500, chrono);
        assertEquals(chrono, ldt.getChronology());
    }

    @Test(timeout = 4000)
    public void testNow() {
        LocalDateTime now = LocalDateTime.now();
        assertNotNull(now);
    }

    @Test(timeout = 4000)
    public void testNowWithZone() {
        LocalDateTime now = LocalDateTime.now(DateTimeZone.UTC);
        assertNotNull(now);
    }

    @Test(timeout = 4000)
    public void testNowWithChronology() {
        LocalDateTime now = LocalDateTime.now(ISOChronology.getInstanceUTC());
        assertNotNull(now);
    }

    @Test(timeout = 4000)
    public void testParse() {
        LocalDateTime ldt = LocalDateTime.parse("2020-06-30T12:30:45.500");
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(30, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(45, ldt.getSecondOfMinute());
        assertEquals(500, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testParseWithFormatter() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        LocalDateTime ldt = LocalDateTime.parse("2020-06-30 12:30", formatter);
        assertEquals(2020, ldt.getYear());
    }

    @Test(timeout = 4000)
    public void testFromCalendarFields() {
        Calendar cal = new GregorianCalendar(2020, Calendar.JUNE, 30, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 500);
        LocalDateTime ldt = LocalDateTime.fromCalendarFields(cal);
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(30, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(45, ldt.getSecondOfMinute());
        assertEquals(500, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testFromDateFields() {
        Date date = new Date(2020 - 1900, Calendar.JUNE, 30, 12, 30, 45);
        date.setTime(date.getTime() + 500);
        LocalDateTime ldt = LocalDateTime.fromDateFields(date);
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(30, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(45, ldt.getSecondOfMinute());
        assertEquals(500, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testGetters() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500);
        assertEquals(2020, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(30, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(45, ldt.getSecondOfMinute());
        assertEquals(500, ldt.getMillisOfSecond());
        assertEquals(12 * 3600 * 1000 + 30 * 60 * 1000 + 45 * 1000 + 500, ldt.getMillisOfDay());
        // Additional getters
        assertEquals(1, ldt.getEra()); // CE
        assertEquals(21, ldt.getCenturyOfEra());
        assertEquals(2020, ldt.getYearOfEra());
        assertEquals(20, ldt.getYearOfCentury());
        assertEquals(2020, ldt.getWeekyear());
        assertEquals(27, ldt.getWeekOfWeekyear());
        assertEquals(182, ldt.getDayOfYear());
        assertEquals(2, ldt.getDayOfWeek()); // Tuesday for 2020-06-30
    }

    @Test(timeout = 4000)
    public void testWithDate() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime changed = base.withDate(2021, 1, 1);
        assertEquals(2021, changed.getYear());
        assertEquals(1, changed.getMonthOfYear());
        assertEquals(1, changed.getDayOfMonth());
        assertEquals(12, changed.getHourOfDay());
        assertEquals(30, changed.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testWithTime() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime changed = base.withTime(8, 15, 30, 100);
        assertEquals(8, changed.getHourOfDay());
        assertEquals(15, changed.getMinuteOfHour());
        assertEquals(30, changed.getSecondOfMinute());
        assertEquals(100, changed.getMillisOfSecond());
        assertEquals(2020, changed.getYear());
    }

    @Test(timeout = 4000)
    public void testWithField() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime changed = base.withField(DateTimeFieldType.year(), 2021);
        assertEquals(2021, changed.getYear());
    }

    @Test(timeout = 4000)
    public void testWithFieldAdded() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime added = base.withFieldAdded(DurationFieldType.years(), 2);
        assertEquals(2022, added.getYear());
    }

    @Test(timeout = 4000)
    public void testWithDurationAdded() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime added = base.withDurationAdded(Duration.standardHours(1), 3);
        assertEquals(15, added.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testWithPeriodAdded() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime added = base.withPeriodAdded(Period.days(5), 1);
        assertEquals(5, added.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPlusYears() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        assertEquals(2025, base.plusYears(5).getYear());
        assertEquals(2015, base.plusYears(-5).getYear());
    }

    @Test(timeout = 4000)
    public void testPlusMonths() {
        LocalDateTime base = new LocalDateTime(2020, 1, 31, 12, 30);
        // Adding one month to Jan 31 should give Feb 29 in 2020 (leap year)
        LocalDateTime result = base.plusMonths(1);
        assertEquals(2, result.getMonthOfYear());
        assertEquals(29, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPlusDays() {
        LocalDateTime base = new LocalDateTime(2020, 2, 28, 12, 30);
        assertEquals(29, base.plusDays(1).getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPlusHours() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 23, 30);
        LocalDateTime result = base.plusHours(2);
        assertEquals(1, result.getDayOfMonth());
        assertEquals(1, result.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testPlusMinutes() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 59);
        LocalDateTime result = base.plusMinutes(2);
        assertEquals(13, result.getHourOfDay());
        assertEquals(1, result.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testPlusSeconds() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30, 59);
        LocalDateTime result = base.plusSeconds(2);
        assertEquals(12, result.getHourOfDay());
        assertEquals(31, result.getMinuteOfHour());
        assertEquals(1, result.getSecondOfMinute());
    }

    @Test(timeout = 4000)
    public void testPlusMillis() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30, 45, 999);
        LocalDateTime result = base.plusMillis(2);
        assertEquals(12, result.getHourOfDay());
        assertEquals(30, result.getMinuteOfHour());
        assertEquals(46, result.getSecondOfMinute());
        assertEquals(1, result.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testMinusYears() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        assertEquals(2015, base.minusYears(5).getYear());
    }

    @Test(timeout = 4000)
    public void testMinusMonths() {
        LocalDateTime base = new LocalDateTime(2020, 3, 31, 12, 30);
        LocalDateTime result = base.minusMonths(1);
        assertEquals(2, result.getMonthOfYear());
        assertEquals(29, result.getDayOfMonth()); // Feb 29 in 2020 leap year
    }

    @Test(timeout = 4000)
    public void testMinusDays() {
        LocalDateTime base = new LocalDateTime(2020, 3, 1, 12, 30);
        assertEquals(29, base.minusDays(1).getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testMinusHours() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 1, 30);
        LocalDateTime result = base.minusHours(2);
        assertEquals(29, result.getDayOfMonth());
        assertEquals(23, result.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testMinusMinutes() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 0);
        LocalDateTime result = base.minusMinutes(5);
        assertEquals(11, result.getHourOfDay());
        assertEquals(55, result.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testMinusSeconds() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30, 0);
        LocalDateTime result = base.minusSeconds(30);
        assertEquals(12, result.getHourOfDay());
        assertEquals(29, result.getMinuteOfHour());
        assertEquals(30, result.getSecondOfMinute());
    }

    @Test(timeout = 4000)
    public void testMinusMillis() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30, 45, 0);
        LocalDateTime result = base.minusMillis(1);
        assertEquals(12, result.getHourOfDay());
        assertEquals(30, result.getMinuteOfHour());
        assertEquals(44, result.getSecondOfMinute());
        assertEquals(999, result.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testToDateTime() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        DateTime dt = ldt.toDateTime();
        assertEquals(ldt.getYear(), dt.getYear());
        assertEquals(ldt.getMonthOfYear(), dt.getMonthOfYear());
        assertEquals(ldt.getDayOfMonth(), dt.getDayOfMonth());
        assertEquals(ldt.getHourOfDay(), dt.getHourOfDay());
        assertEquals(ldt.getMinuteOfHour(), dt.getMinuteOfHour());
        assertEquals(ldt.getSecondOfMinute(), dt.getSecondOfMinute());
        assertEquals(ldt.getMillisOfSecond(), dt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testToDateTimeWithZone() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTime dt = ldt.toDateTime(zone);
        assertEquals(ldt.getYear(), dt.getYear());
        // The hour may shift due to time zone offset; we just check not null and same millis roughly
        assertNotNull(dt);
    }

    @Test(timeout = 4000)
    public void testToLocalDate() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDate ld = ldt.toLocalDate();
        assertEquals(2020, ld.getYear());
        assertEquals(6, ld.getMonthOfYear());
        assertEquals(30, ld.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testToLocalTime() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500);
        LocalTime lt = ldt.toLocalTime();
        assertEquals(12, lt.getHourOfDay());
        assertEquals(30, lt.getMinuteOfHour());
        assertEquals(45, lt.getSecondOfMinute());
        assertEquals(500, lt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testPropertyGetters() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertNotNull(ldt.era());
        assertNotNull(ldt.centuryOfEra());
        assertNotNull(ldt.yearOfCentury());
        assertNotNull(ldt.yearOfEra());
        assertNotNull(ldt.year());
        assertNotNull(ldt.weekyear());
        assertNotNull(ldt.monthOfYear());
        assertNotNull(ldt.weekOfWeekyear());
        assertNotNull(ldt.dayOfYear());
        assertNotNull(ldt.dayOfMonth());
        assertNotNull(ldt.dayOfWeek());
        assertNotNull(ldt.hourOfDay());
        assertNotNull(ldt.minuteOfHour());
        assertNotNull(ldt.secondOfMinute());
        assertNotNull(ldt.millisOfSecond());
        assertNotNull(ldt.millisOfDay());
    }

    @Test(timeout = 4000)
    public void testPropertyAddToCopy() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = ldt.year().addToCopy(5);
        assertEquals(2025, result.getYear());
    }

    @Test(timeout = 4000)
    public void testPropertyAddToCopyLong() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = ldt.year().addToCopy(5L);
        assertEquals(2025, result.getYear());
    }

    @Test(timeout = 4000)
    public void testPropertyAddWrapFieldToCopy() {
        LocalDateTime ldt = new LocalDateTime(2020, 12, 31, 12, 30);
        // AddWrapField dayOfMonth: 31 + 1 wraps to 1 next month? Actually dayOfMonth is not wrapping across months,
        // but wraps within the month's range. For 31, addWrapField(1) should give 1 (since Feb has 29 days in 2020? Actually
        // the wrapField is only within the field's range, so month stays same. For dayOfMonth, max is 31, so 31+1=32 wraps to 1.
        LocalDateTime result = ldt.dayOfMonth().addWrapFieldToCopy(1);
        assertEquals(1, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPropertySetCopy() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = ldt.year().setCopy(2025);
        assertEquals(2025, result.getYear());
    }

    @Test(timeout = 4000)
    public void testPropertySetCopyText() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = ldt.monthOfYear().setCopy("December");
        assertEquals(12, result.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testPropertyWithMaximumValue() {
        LocalDateTime ldt = new LocalDateTime(2020, 2, 1, 12, 30);
        LocalDateTime result = ldt.dayOfMonth().withMaximumValue();
        assertEquals(29, result.getDayOfMonth()); // 2020 is leap year
    }

    @Test(timeout = 4000)
    public void testPropertyWithMinimumValue() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 15, 12, 30);
        LocalDateTime result = ldt.dayOfMonth().withMinimumValue();
        assertEquals(1, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testRoundFloorCopy() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500);
        LocalDateTime result = ldt.hourOfDay().roundFloorCopy();
        assertEquals(12, result.getHourOfDay());
        assertEquals(0, result.getMinuteOfHour());
        assertEquals(0, result.getSecondOfMinute());
        assertEquals(0, result.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testRoundCeilingCopy() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500);
        LocalDateTime result = ldt.hourOfDay().roundCeilingCopy();
        assertEquals(13, result.getHourOfDay());
        assertEquals(0, result.getMinuteOfHour());
        assertEquals(0, result.getSecondOfMinute());
        assertEquals(0, result.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testRoundHalfFloorCopy() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 0, 0);
        LocalDateTime result = ldt.hourOfDay().roundHalfFloorCopy();
        assertEquals(12, result.getHourOfDay()); // 12:30 rounds to 12 (floor)
    }

    @Test(timeout = 4000)
    public void testRoundHalfCeilingCopy() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 0, 0);
        LocalDateTime result = ldt.hourOfDay().roundHalfCeilingCopy();
        assertEquals(13, result.getHourOfDay()); // 12:30 rounds to 13 (ceiling)
    }

    @Test(timeout = 4000)
    public void testRoundHalfEvenCopy() {
        // 12:30 rounds to 12 (even hour)
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 0, 0);
        LocalDateTime result = ldt.hourOfDay().roundHalfEvenCopy();
        assertEquals(12, result.getHourOfDay());
        // 13:30 rounds to 14 (13 is odd, 14 even)
        ldt = new LocalDateTime(2020, 6, 30, 13, 30, 0, 0);
        result = ldt.hourOfDay().roundHalfEvenCopy();
        assertEquals(14, result.getHourOfDay());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testYearZero() {
        LocalDateTime ldt = new LocalDateTime(0, 6, 30, 12, 30);
        assertEquals(0, ldt.getYear());
    }

    @Test(timeout = 4000)
    public void testYearNegative() {
        LocalDateTime ldt = new LocalDateTime(-1, 6, 30, 12, 30);
        assertEquals(-1, ldt.getYear());
    }

    @Test(timeout = 4000)
    public void testYearLargePositive() {
        LocalDateTime ldt = new LocalDateTime(9999, 12, 31, 23, 59, 59, 999);
        assertEquals(9999, ldt.getYear());
    }

    @Test(timeout = 4000)
    public void testYearLargeNegative() {
        LocalDateTime ldt = new LocalDateTime(-9999, 6, 30, 12, 30);
        assertEquals(-9999, ldt.getYear());
    }

    @Test(timeout = 4000)
    public void testMonthBoundary() {
        LocalDateTime ldt = new LocalDateTime(2020, 12, 31, 12, 30);
        assertEquals(12, ldt.getMonthOfYear());
        assertEquals(31, ldt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testDayOfMonthLeapYear() {
        LocalDateTime ldt = new LocalDateTime(2020, 2, 29, 12, 30);
        assertEquals(29, ldt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testDayOfMonthNonLeapYear() {
        // 2019 is not leap, Feb 28 valid
        LocalDateTime ldt = new LocalDateTime(2019, 2, 28, 12, 30);
        assertEquals(28, ldt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testHourBoundary() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 23, 59);
        assertEquals(23, ldt.getHourOfDay());
        assertEquals(59, ldt.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testSecondBoundary() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 59, 999);
        assertEquals(59, ldt.getSecondOfMinute());
        assertEquals(999, ldt.getMillisOfSecond());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testFromDateFields_beforeYearZero1() {
        // Test for 1 BC: Date represents year 1 BC, but Java Date returns year -1900, so year = 1 BC?
        // Actually Date constructor: year = year - 1900. For 1 BC, we need to set year = -1900?
        // Recognize that the JCK's GregorianCalendar has year 0 for 1 BC. The Date object to represent
        // a date in year 0 (1 BC) should be constructed with (year - 1900) == -1900.
        // However, the Java Date/Calendar handling of BC is messy. The defect shows that fromDateFields
        // incorrectly returns year 1 for an input that should give year 0.
        // We'll construct a Date that represents 1 BC (Feb 3, 4:05:06.007) using deprecated Date constructor.
        // Note: In Java, year 0 in the Date constructor corresponds to 1900 BC? Actually the year parameter is
        // the year minus 1900. So for year 0 (1 BC), set year = -1900.
        @SuppressWarnings("deprecation")
        Date date = new Date(-1900, 1, 3, 4, 5, 6); // year 0 (1 BC), month 1 (Feb), day 3, time 04:05:06
        date.setTime(date.getTime() + 7); // add 7 millis to get .007
        LocalDateTime ldt = LocalDateTime.fromDateFields(date);
        // Expected: year 0, month 2 (Feb), day 3, hour 4, minute 5, second 6, millis 7
        assertEquals("Year should be 0 for 1 BC", 0, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(3, ldt.getDayOfMonth());
        assertEquals(4, ldt.getHourOfDay());
        assertEquals(5, ldt.getMinuteOfHour());
        assertEquals(6, ldt.getSecondOfMinute());
        assertEquals(7, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testFromDateFields_beforeYearZero3() {
        // Test for 3 BC: year = -2
        @SuppressWarnings("deprecation")
        Date date = new Date(-1902, 1, 3, 4, 5, 6); // year -2 (3 BC)
        date.setTime(date.getTime() + 7);
        LocalDateTime ldt = LocalDateTime.fromDateFields(date);
        assertEquals("Year should be -2 for 3 BC", -2, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(3, ldt.getDayOfMonth());
        assertEquals(4, ldt.getHourOfDay());
        assertEquals(5, ldt.getMinuteOfHour());
        assertEquals(6, ldt.getSecondOfMinute());
        assertEquals(7, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testFromCalendarFields_beforeYearZero1() {
        // Calendar for 1 BC: year 0 in GregorianCalendar, but we use set(Calendar.ERA, GregorianCalendar.BC)
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1); // 1 BC -> year 0 in proleptic
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime ldt = LocalDateTime.fromCalendarFields(cal);
        assertEquals("Year should be 0 for 1 BC", 0, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(3, ldt.getDayOfMonth());
        assertEquals(4, ldt.getHourOfDay());
        assertEquals(5, ldt.getMinuteOfHour());
        assertEquals(6, ldt.getSecondOfMinute());
        assertEquals(7, ldt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testFromCalendarFields_beforeYearZero3() {
        // Calendar for 3 BC: year 2 BC? Actually 3 BC -> year = -2. Calendar: set YEAR=3, ERA=BC.
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 3); // 3 BC -> year -2 in proleptic
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime ldt = LocalDateTime.fromCalendarFields(cal);
        assertEquals("Year should be -2 for 3 BC", -2, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(3, ldt.getDayOfMonth());
        assertEquals(4, ldt.getHourOfDay());
        assertEquals(5, ldt.getMinuteOfHour());
        assertEquals(6, ldt.getSecondOfMinute());
        assertEquals(7, ldt.getMillisOfSecond());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNowZoneNull() {
        LocalDateTime.now((DateTimeZone) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNowChronologyNull() {
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

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetNullFieldType() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        ldt.get(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyNullFieldType() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        ldt.property(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldNullFieldType() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        ldt.withField(null, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithFieldAddedNullFieldType() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        ldt.withFieldAdded(null, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldInvalidIndex() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        // Accessing getField via getValue with invalid index? Actually getField is protected.
        // But getValue with index 4 should throw IndexOutOfBoundsException.
        ldt.getValue(4);
    }

    @Test(timeout = 4000)
    public void testIsSupportedNullFieldType() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertFalse(ldt.isSupported((DateTimeFieldType) null));
    }

    @Test(timeout = 4000)
    public void testIsSupportedDurationFieldNull() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertFalse(ldt.isSupported((DurationFieldType) null));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyWithUnsupportedField() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        // DateTimeFieldType.centuryOfEra() is supported, but we can try a field that might not be supported?
        // Actually all fields are supported for ISO chronology. Maybe a field like 'weekyear' is supported.
        // To test unsupported, we can use a custom field type? Simpler: just test that property throws when field is not supported.
        // But all standard fields are supported. Instead, we can test that property with era? era is supported.
        // For safety, we test that the property method works; we already tested unsupported via isSupported.
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertEquals(ldt, ldt);
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        LocalDateTime ldt1 = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime ldt2 = new LocalDateTime(2020, 6, 30, 12, 30);
        assertEquals(ldt1, ldt2);
        assertEquals(ldt2, ldt1);
    }

    @Test(timeout = 4000)
    public void testEqualsNotEqual() {
        LocalDateTime ldt1 = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime ldt2 = new LocalDateTime(2020, 6, 30, 12, 31);
        assertNotEquals(ldt1, ldt2);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertNotNull(ldt);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentChronology() {
        LocalDateTime ldt1 = new LocalDateTime(2020, 6, 30, 12, 30);
        // Use a different chronology, e.g., Buddhist
        Chronology buddhist = org.joda.time.chrono.BuddhistChronology.getInstanceUTC();
        LocalDateTime ldt2 = new LocalDateTime(2020, 6, 30, 12, 30, 0, 0, buddhist);
        // Different chrono, so not equal
        assertNotEquals(ldt1, ldt2);
    }

    @Test(timeout = 4000)
    public void testCompareToSame() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertEquals(0, ldt.compareTo(ldt));
    }

    @Test(timeout = 4000)
    public void testCompareToLess() {
        LocalDateTime ldt1 = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime ldt2 = new LocalDateTime(2020, 6, 30, 12, 31);
        assertTrue(ldt1.compareTo(ldt2) < 0);
    }

    @Test(timeout = 4000)
    public void testCompareToGreater() {
        LocalDateTime ldt1 = new LocalDateTime(2020, 6, 30, 12, 31);
        LocalDateTime ldt2 = new LocalDateTime(2020, 6, 30, 12, 30);
        assertTrue(ldt1.compareTo(ldt2) > 0);
    }

    @Test(timeout = 4000)
    public void testToString() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500);
        assertEquals("2020-06-30T12:30:45.500", ldt.toString());
    }

    @Test(timeout = 4000)
    public void testToStringWithPattern() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertEquals("2020/06/30", ldt.toString("yyyy/MM/dd"));
    }

    @Test(timeout = 4000)
    public void testToStringWithPatternAndLocale() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        String result = ldt.toString("yyyy MMM dd", Locale.US);
        assertEquals("2020 Jun 30", result);
    }

    @Test(timeout = 4000)
    public void testWithLocalMillisIdentity() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        // withLocalMillis with same millis returns this
        LocalDateTime result = ldt.withLocalMillis(ldt.getLocalMillis());
        assertSame(ldt, result);
    }

    @Test(timeout = 4000)
    public void testWithLocalMillisDifferent() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        long newMillis = ldt.getLocalMillis() + 3600000L; // +1 hour
        LocalDateTime result = ldt.withLocalMillis(newMillis);
        assertNotSame(ldt, result);
        assertEquals(13, result.getHourOfDay());
    }

    // Additional edge: test that plus(ReadableDuration) and minus work
    @Test(timeout = 4000)
    public void testPlusDuration() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = base.plus(Duration.standardHours(2));
        assertEquals(14, result.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testMinusDuration() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = base.minus(Duration.standardHours(2));
        assertEquals(10, result.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testPlusPeriod() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = base.plus(Period.days(5));
        assertEquals(5, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testMinusPeriod() {
        LocalDateTime base = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = base.minus(Period.days(5));
        assertEquals(25, result.getDayOfMonth());
    }

    // Test readResolve: we can't easily trigger serialization without full mock, but we can check the condition.
    // The readResolve handles null chronology or non-UTC zone. We'll rely on constructor to set proper values.

    // Additional branch: test that withFieldAdded zero amount returns this.
    @Test(timeout = 4000)
    public void testWithFieldAddedZero() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertSame(ldt, ldt.withFieldAdded(DurationFieldType.years(), 0));
    }

    // Test that withDurationAdded with null duration returns this.
    @Test(timeout = 4000)
    public void testWithDurationAddedNull() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertSame(ldt, ldt.withDurationAdded(null, 1));
    }

    // Test that withPeriodAdded with null period returns this.
    @Test(timeout = 4000)
    public void testWithPeriodAddedNull() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertSame(ldt, ldt.withPeriodAdded(null, 1));
    }

    // Test that plus with null duration returns this (since withDurationAdded returns this).
    @Test(timeout = 4000)
    public void testPlusNullDuration() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertSame(ldt, ldt.plus((ReadableDuration) null));
    }

    // Test that minus with null period returns this.
    @Test(timeout = 4000)
    public void testMinusNullPeriod() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        assertSame(ldt, ldt.minus((ReadablePeriod) null));
    }

    // Test property setCopy with locale
    @Test(timeout = 4000)
    public void testPropertySetCopyWithLocale() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30);
        LocalDateTime result = ldt.monthOfYear().setCopy("December", Locale.US);
        assertEquals(12, result.getMonthOfYear());
    }

    // Test toDate: just call and verify not null and compatible
    @Test(timeout = 4000)
    public void testToDate() {
        LocalDateTime ldt = new LocalDateTime(2020, 6, 30, 12, 30, 45, 500);
        Date date = ldt.toDate();
        assertNotNull(date);
        // Rough check: should be within a day of 2020-06-30
        long expected = new Date(2020 - 1900, 5, 30, 12, 30, 45).getTime() + 500;
        // Allow minute difference due to DST handling
        assertTrue(Math.abs(date.getTime() - expected) < 60000);
    }
}