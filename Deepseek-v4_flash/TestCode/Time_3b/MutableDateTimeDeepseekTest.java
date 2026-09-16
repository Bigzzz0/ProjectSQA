package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: MutableDateTime
 * 
 * Key Decision Branches & Boundary Conditions:
 * 1. setRounding(DateTimeField, int) - mode validation (ROUND_NONE to ROUND_HALF_EVEN)
 * 2. setMillis(long) - switch on iRoundingMode (6 cases + default)
 * 3. setMillis(ReadableInstant) - null handling via DateTimeUtils
 * 4. add(long) - safeAdd overflow check
 * 5. add(ReadableDuration, int) - null duration handling
 * 6. add(ReadablePeriod, int) - null period handling
 * 7. setZone(DateTimeZone) - zone change detection
 * 8. setZoneRetainFields(DateTimeZone) - zone equality check, millis adjustment
 * 9. set(DateTimeFieldType, int) - null type check
 * 10. add(DurationFieldType, int) - null type check
 * 11. setDate(ReadableInstant) - ReadableDateTime cast and zone conversion
 * 12. setTime(ReadableInstant) - zone conversion to UTC
 * 13. property(DateTimeFieldType) - null check and unsupported field check
 * 14. DST Overlap Bug: addYears/addDays/addWeeks/addMonths/add(DurationFieldType) 
 *     during DST overlap with zero amount - offset should remain unchanged
 * 
 * Defect-Targeted: DST overlap winter time, adding zero to various fields
 * should preserve the original offset (+01:00), but bug causes offset change to +02:00
 */
public class MutableDateTimeDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorDefault() {
        MutableDateTime mdt = new MutableDateTime();
        assertNotNull(mdt);
        assertNotNull(mdt.getChronology());
        assertNotNull(mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testConstructorWithZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        MutableDateTime mdt = new MutableDateTime(zone);
        assertEquals(zone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullZone() {
        MutableDateTime mdt = new MutableDateTime((DateTimeZone) null);
        assertNotNull(mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testConstructorWithChronology() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        MutableDateTime mdt = new MutableDateTime(chrono);
        assertEquals(chrono, mdt.getChronology());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullChronology() {
        MutableDateTime mdt = new MutableDateTime((Chronology) null);
        assertNotNull(mdt.getChronology());
    }

    @Test(timeout = 4000)
    public void testConstructorWithLong() {
        long millis = 1000000L;
        MutableDateTime mdt = new MutableDateTime(millis);
        assertEquals(millis, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongAndZone() {
        long millis = 1000000L;
        DateTimeZone zone = DateTimeZone.UTC;
        MutableDateTime mdt = new MutableDateTime(millis, zone);
        assertEquals(millis, mdt.getMillis());
        assertEquals(zone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongAndChronology() {
        long millis = 1000000L;
        Chronology chrono = ISOChronology.getInstanceUTC();
        MutableDateTime mdt = new MutableDateTime(millis, chrono);
        assertEquals(millis, mdt.getMillis());
        assertEquals(chrono, mdt.getChronology());
    }

    @Test(timeout = 4000)
    public void testConstructorWithObject() {
        MutableDateTime mdt = new MutableDateTime((Object) null);
        assertNotNull(mdt);
    }

    @Test(timeout = 4000)
    public void testConstructorWithFields() {
        MutableDateTime mdt = new MutableDateTime(2020, 6, 15, 10, 30, 45, 500);
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(45, mdt.getSecondOfMinute());
        assertEquals(500, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testSetRoundingWithField() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field);
        assertEquals(field, mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_FLOOR, mdt.getRoundingMode());
    }

    @Test(timeout = 4000)
    public void testSetRoundingWithNullField() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.setRounding(null);
        assertNull(mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());
    }

    @Test(timeout = 4000)
    public void testSetRoundingWithMode() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        DateTimeField field = ISOChronology.getInstanceUTC().secondOfMinute();
        mdt.setRounding(field, MutableDateTime.ROUND_CEILING);
        assertEquals(field, mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_CEILING, mdt.getRoundingMode());
    }

    @Test(timeout = 4000)
    public void testSetRoundingWithInvalidMode() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        DateTimeField field = ISOChronology.getInstanceUTC().secondOfMinute();
        try {
            mdt.setRounding(field, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetRoundingWithModeRoundNone() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        DateTimeField field = ISOChronology.getInstanceUTC().secondOfMinute();
        mdt.setRounding(field, MutableDateTime.ROUND_NONE);
        assertNull(mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingFloor() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field, MutableDateTime.ROUND_FLOOR);
        mdt.setMillis(1500L);
        assertEquals(1000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingCeiling() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field, MutableDateTime.ROUND_CEILING);
        mdt.setMillis(1500L);
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingHalfFloor() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_FLOOR);
        mdt.setMillis(1500L);
        assertEquals(1000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingHalfCeiling() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_CEILING);
        mdt.setMillis(1500L);
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingHalfEven() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_EVEN);
        mdt.setMillis(1500L);
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithReadableInstant() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        DateTime dt = new DateTime(2000L);
        mdt.setMillis((ReadableInstant) dt);
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithNullReadableInstant() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.setMillis((ReadableInstant) null);
        // Should set to current time, just verify no exception
        assertTrue(mdt.getMillis() > 0);
    }

    @Test(timeout = 4000)
    public void testAddDuration() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.add(500L);
        assertEquals(1500L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddReadableDuration() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.add(new Duration(500L));
        assertEquals(1500L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddNullReadableDuration() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.add((ReadableDuration) null);
        assertEquals(1000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddReadableDurationWithScalar() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.add(new Duration(500L), 3);
        assertEquals(2500L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddNullReadableDurationWithScalar() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.add((ReadableDuration) null, 3);
        assertEquals(1000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddReadablePeriod() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.add(Period.days(1));
        assertEquals(86400000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddNullReadablePeriod() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.add((ReadablePeriod) null);
        assertEquals(1000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddReadablePeriodWithScalar() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.add(Period.days(1), 2);
        assertEquals(172800000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetChronology() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        Chronology chrono = ISOChronology.getInstanceUTC();
        mdt.setChronology(chrono);
        assertEquals(chrono, mdt.getChronology());
    }

    @Test(timeout = 4000)
    public void testSetZone() {
        MutableDateTime mdt = new MutableDateTime(1000L, DateTimeZone.UTC);
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        mdt.setZone(zone);
        assertEquals(zone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testSetZoneSameZone() {
        DateTimeZone zone = DateTimeZone.UTC;
        MutableDateTime mdt = new MutableDateTime(1000L, zone);
        mdt.setZone(zone);
        assertEquals(zone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testSetZoneRetainFields() {
        MutableDateTime mdt = new MutableDateTime(1000L, DateTimeZone.UTC);
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        mdt.setZoneRetainFields(zone);
        assertEquals(zone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testSetZoneRetainFieldsSameZone() {
        DateTimeZone zone = DateTimeZone.UTC;
        MutableDateTime mdt = new MutableDateTime(1000L, zone);
        mdt.setZoneRetainFields(zone);
        assertEquals(zone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testSetWithDateTimeFieldType() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.set(DateTimeFieldType.year(), 2020);
        assertEquals(2020, mdt.getYear());
    }

    @Test(timeout = 4000)
    public void testSetWithNullDateTimeFieldType() {
        MutableDateTime mdt = new MutableDateTime(0L);
        try {
            mdt.set(null, 2020);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddDurationFieldType() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.add(DurationFieldType.days(), 1);
        assertEquals(86400000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddNullDurationFieldType() {
        MutableDateTime mdt = new MutableDateTime(0L);
        try {
            mdt.add((DurationFieldType) null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetYear() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setYear(2020);
        assertEquals(2020, mdt.getYear());
    }

    @Test(timeout = 4000)
    public void testAddYears() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addYears(1);
        assertEquals(1, mdt.getYear());
    }

    @Test(timeout = 4000)
    public void testSetWeekyear() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setWeekyear(2020);
        assertEquals(2020, mdt.getWeekyear());
    }

    @Test(timeout = 4000)
    public void testAddWeekyears() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addWeekyears(1);
        assertEquals(1, mdt.getWeekyear());
    }

    @Test(timeout = 4000)
    public void testSetMonthOfYear() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setMonthOfYear(6);
        assertEquals(6, mdt.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testAddMonths() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addMonths(1);
        assertEquals(1, mdt.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testSetWeekOfWeekyear() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setWeekOfWeekyear(10);
        assertEquals(10, mdt.getWeekOfWeekyear());
    }

    @Test(timeout = 4000)
    public void testAddWeeks() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addWeeks(1);
        assertEquals(1, mdt.getWeekOfWeekyear());
    }

    @Test(timeout = 4000)
    public void testSetDayOfYear() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setDayOfYear(100);
        assertEquals(100, mdt.getDayOfYear());
    }

    @Test(timeout = 4000)
    public void testSetDayOfMonth() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setDayOfMonth(15);
        assertEquals(15, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testSetDayOfWeek() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setDayOfWeek(3);
        assertEquals(3, mdt.getDayOfWeek());
    }

    @Test(timeout = 4000)
    public void testAddDays() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addDays(1);
        assertEquals(1, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testSetHourOfDay() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setHourOfDay(10);
        assertEquals(10, mdt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testAddHours() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addHours(1);
        assertEquals(1, mdt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testSetMinuteOfDay() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setMinuteOfDay(30);
        assertEquals(30, mdt.getMinuteOfDay());
    }

    @Test(timeout = 4000)
    public void testSetMinuteOfHour() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setMinuteOfHour(30);
        assertEquals(30, mdt.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testAddMinutes() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addMinutes(1);
        assertEquals(1, mdt.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testSetSecondOfDay() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setSecondOfDay(45);
        assertEquals(45, mdt.getSecondOfDay());
    }

    @Test(timeout = 4000)
    public void testSetSecondOfMinute() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setSecondOfMinute(45);
        assertEquals(45, mdt.getSecondOfMinute());
    }

    @Test(timeout = 4000)
    public void testAddSeconds() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addSeconds(1);
        assertEquals(1, mdt.getSecondOfMinute());
    }

    @Test(timeout = 4000)
    public void testSetMillisOfDay() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setMillisOfDay(500);
        assertEquals(500, mdt.getMillisOfDay());
    }

    @Test(timeout = 4000)
    public void testSetMillisOfSecond() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setMillisOfSecond(500);
        assertEquals(500, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testAddMillis() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.addMillis(1);
        assertEquals(1, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testSetDateWithLong() {
        MutableDateTime mdt = new MutableDateTime(0L);
        long dateMillis = new DateTime(2020, 6, 15, 0, 0, 0, 0).getMillis();
        mdt.setDate(dateMillis);
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testSetDateWithReadableInstant() {
        MutableDateTime mdt = new MutableDateTime(0L);
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, 0);
        mdt.setDate((ReadableInstant) dt);
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testSetDateWithFields() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setDate(2020, 6, 15);
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testSetTimeWithLong() {
        MutableDateTime mdt = new MutableDateTime(0L);
        long timeMillis = new DateTime(0L).withTime(10, 30, 45, 500).getMillis();
        mdt.setTime(timeMillis);
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(45, mdt.getSecondOfMinute());
        assertEquals(500, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testSetTimeWithReadableInstant() {
        MutableDateTime mdt = new MutableDateTime(0L);
        DateTime dt = new DateTime(0L).withTime(10, 30, 45, 500);
        mdt.setTime((ReadableInstant) dt);
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(45, mdt.getSecondOfMinute());
        assertEquals(500, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testSetTimeWithFields() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setTime(10, 30, 45, 500);
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(45, mdt.getSecondOfMinute());
        assertEquals(500, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testSetDateTime() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.setDateTime(2020, 6, 15, 10, 30, 45, 500);
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(45, mdt.getSecondOfMinute());
        assertEquals(500, mdt.getMillisOfSecond());
    }

    @Test(timeout = 4000)
    public void testProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.property(DateTimeFieldType.year());
        assertNotNull(prop);
        assertEquals(mdt, prop.getMutableDateTime());
    }

    @Test(timeout = 4000)
    public void testPropertyWithNullType() {
        MutableDateTime mdt = new MutableDateTime(0L);
        try {
            mdt.property(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPropertyWithUnsupportedField() {
        MutableDateTime mdt = new MutableDateTime(0L);
        // Using a field that might not be supported in all chronologies
        try {
            mdt.property(DateTimeFieldType.centuryOfEra());
            // May or may not throw depending on chronology
        } catch (IllegalArgumentException e) {
            // expected if unsupported
        }
    }

    @Test(timeout = 4000)
    public void testEraProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.era();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testCenturyOfEraProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.centuryOfEra();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testYearOfCenturyProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.yearOfCentury();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testYearOfEraProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.yearOfEra();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testYearProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.year();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testWeekyearProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.weekyear();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testMonthOfYearProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.monthOfYear();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testWeekOfWeekyearProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.weekOfWeekyear();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testDayOfYearProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.dayOfYear();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testDayOfMonthProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.dayOfMonth();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testDayOfWeekProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.dayOfWeek();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testHourOfDayProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.hourOfDay();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testMinuteOfDayProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.minuteOfDay();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testMinuteOfHourProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.minuteOfHour();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testSecondOfDayProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.secondOfDay();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testSecondOfMinuteProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.secondOfMinute();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testMillisOfDayProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.millisOfDay();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testMillisOfSecondProperty() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.millisOfSecond();
        assertNotNull(prop);
    }

    @Test(timeout = 4000)
    public void testCopy() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        MutableDateTime copy = mdt.copy();
        assertEquals(mdt.getMillis(), copy.getMillis());
        assertNotSame(mdt, copy);
    }

    @Test(timeout = 4000)
    public void testClone() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        MutableDateTime clone = (MutableDateTime) mdt.clone();
        assertEquals(mdt.getMillis(), clone.getMillis());
        assertNotSame(mdt, clone);
    }

    @Test(timeout = 4000)
    public void testToString() {
        MutableDateTime mdt = new MutableDateTime(0L, DateTimeZone.UTC);
        String str = mdt.toString();
        assertNotNull(str);
        assertTrue(str.contains("1970-01-01T00:00:00.000Z"));
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testAddLongMaxValue() {
        MutableDateTime mdt = new MutableDateTime(0L);
        try {
            mdt.add(Long.MAX_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddLongMinValue() {
        MutableDateTime mdt = new MutableDateTime(0L);
        try {
            mdt.add(Long.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingAllModes() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        
        mdt.setRounding(field, MutableDateTime.ROUND_FLOOR);
        mdt.setMillis(1500L);
        assertEquals(1000L, mdt.getMillis());
        
        mdt.setRounding(field, MutableDateTime.ROUND_CEILING);
        mdt.setMillis(1500L);
        assertEquals(2000L, mdt.getMillis());
        
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_FLOOR);
        mdt.setMillis(1500L);
        assertEquals(1000L, mdt.getMillis());
        
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_CEILING);
        mdt.setMillis(1500L);
        assertEquals(2000L, mdt.getMillis());
        
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_EVEN);
        mdt.setMillis(1500L);
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingHalfEvenEvenValue() {
        MutableDateTime mdt = new MutableDateTime(2500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_EVEN);
        mdt.setMillis(2500L);
        assertEquals(2000L, mdt.getMillis()); // 2 is even, so floor
    }

    @Test(timeout = 4000)
    public void testSetMillisWithRoundingHalfEvenOddValue() {
        MutableDateTime mdt = new MutableDateTime(3500L);
        DateTimeField field = ISOChronology.getInstanceUTC().millisOfSecond();
        mdt.setRounding(field, MutableDateTime.ROUND_HALF_EVEN);
        mdt.setMillis(3500L);
        assertEquals(4000L, mdt.getMillis()); // 4 is even, so ceiling
    }

    @Test(timeout = 4000)
    public void testAddReadableDurationWithNegativeScalar() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        mdt.add(new Duration(500L), -1);
        assertEquals(500L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testAddReadablePeriodWithNegativeScalar() {
        MutableDateTime mdt = new MutableDateTime(86400000L);
        mdt.add(Period.days(1), -1);
        assertEquals(0L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testSetDateWithReadableDateTimeDifferentZone() {
        MutableDateTime mdt = new MutableDateTime(0L, DateTimeZone.UTC);
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, 0, paris);
        mdt.setDate((ReadableInstant) dt);
        // Date should be set, time should be preserved (0)
        assertEquals(2020, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(15, mdt.getDayOfMonth());
        assertEquals(0, mdt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testSetTimeWithReadableInstantDifferentZone() {
        MutableDateTime mdt = new MutableDateTime(0L, DateTimeZone.UTC);
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 0, 0, paris);
        mdt.setTime((ReadableInstant) dt);
        // Time should be set in UTC, date should be preserved (1970-01-01)
        assertEquals(10, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(1970, mdt.getYear());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // DST Overlap Bug: Adding zero to various fields during DST overlap should preserve offset

    @Test(timeout = 4000)
    public void testAddYears_int_dstOverlapWinter_addZero() {
        // Europe/Paris DST overlap: October 30, 2022 at 3:00 AM clocks go back to 2:00 AM
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        // Create a time during the overlap in winter time (+01:00)
        MutableDateTime mdt = new MutableDateTime(2022, 10, 30, 2, 30, 0, 0, zone);
        // Verify we're in winter time (+01:00)
        String before = mdt.toString();
        assertTrue("Expected +01:00 offset but got: " + before, before.contains("+01:00"));
        
        mdt.addYears(0);
        
        String after = mdt.toString();
        assertEquals("Offset should remain +01:00 after adding zero years", before, after);
    }

    @Test(timeout = 4000)
    public void testAddDays_int_dstOverlapWinter_addZero() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        MutableDateTime mdt = new MutableDateTime(2022, 10, 30, 2, 30, 0, 0, zone);
        String before = mdt.toString();
        assertTrue("Expected +01:00 offset but got: " + before, before.contains("+01:00"));
        
        mdt.addDays(0);
        
        String after = mdt.toString();
        assertEquals("Offset should remain +01:00 after adding zero days", before, after);
    }

    @Test(timeout = 4000)
    public void testAddWeeks_int_dstOverlapWinter_addZero() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        MutableDateTime mdt = new MutableDateTime(2022, 10, 30, 2, 30, 0, 0, zone);
        String before = mdt.toString();
        assertTrue("Expected +01:00 offset but got: " + before, before.contains("+01:00"));
        
        mdt.addWeeks(0);
        
        String after = mdt.toString();
        assertEquals("Offset should remain +01:00 after adding zero weeks", before, after);
    }

    @Test(timeout = 4000)
    public void testAdd_DurationFieldType_int_dstOverlapWinter_addZero() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        MutableDateTime mdt = new MutableDateTime(2022, 10, 30, 2, 30, 0, 0, zone);
        String before = mdt.toString();
        assertTrue("Expected +01:00 offset but got: " + before, before.contains("+01:00"));
        
        mdt.add(DurationFieldType.years(), 0);
        
        String after = mdt.toString();
        assertEquals("Offset should remain +01:00 after adding zero years via DurationFieldType", before, after);
    }

    @Test(timeout = 4000)
    public void testAddMonths_int_dstOverlapWinter_addZero() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        MutableDateTime mdt = new MutableDateTime(2022, 10, 30, 2, 30, 0, 0, zone);
        String before = mdt.toString();
        assertTrue("Expected +01:00 offset but got: " + before, before.contains("+01:00"));
        
        mdt.addMonths(0);
        
        String after = mdt.toString();
        assertEquals("Offset should remain +01:00 after adding zero months", before, after);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testNowWithNullZone() {
        try {
            MutableDateTime.now((DateTimeZone) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNowWithNullChronology() {
        try {
            MutableDateTime.now((Chronology) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithNullString() {
        try {
            MutableDateTime.parse((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithNullFormatter() {
        try {
            MutableDateTime.parse("2020-06-15", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetRoundingWithNullFieldAndInvalidMode() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        // When field is null, mode validation is skipped
        mdt.setRounding(null, MutableDateTime.ROUND_FLOOR);
        assertNull(mdt.getRoundingField());
        assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());
    }

    @Test(timeout = 4000)
    public void testAddDurationOverflow() {
        MutableDateTime mdt = new MutableDateTime(Long.MAX_VALUE);
        try {
            mdt.add(1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddDurationUnderflow() {
        MutableDateTime mdt = new MutableDateTime(Long.MIN_VALUE);
        try {
            mdt.add(-1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testPropertyAdd() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.year();
        prop.add(1);
        assertEquals(1, mdt.getYear());
    }

    @Test(timeout = 4000)
    public void testPropertyAddLong() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.year();
        prop.add(1L);
        assertEquals(1, mdt.getYear());
    }

    @Test(timeout = 4000)
    public void testPropertyAddWrapField() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.monthOfYear();
        prop.addWrapField(13);
        assertEquals(2, mdt.getMonthOfYear()); // wraps around
    }

    @Test(timeout = 4000)
    public void testPropertySet() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.year();
        prop.set(2020);
        assertEquals(2020, mdt.getYear());
    }

    @Test(timeout = 4000)
    public void testPropertySetText() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.monthOfYear();
        prop.set("June", Locale.ENGLISH);
        assertEquals(6, mdt.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testPropertySetTextDefaultLocale() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.monthOfYear();
        prop.set("June");
        assertEquals(6, mdt.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testPropertyRoundFloor() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        MutableDateTime.Property prop = mdt.millisOfSecond();
        prop.roundFloor();
        assertEquals(1000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testPropertyRoundCeiling() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        MutableDateTime.Property prop = mdt.millisOfSecond();
        prop.roundCeiling();
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testPropertyRoundHalfFloor() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        MutableDateTime.Property prop = mdt.millisOfSecond();
        prop.roundHalfFloor();
        assertEquals(1000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testPropertyRoundHalfCeiling() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        MutableDateTime.Property prop = mdt.millisOfSecond();
        prop.roundHalfCeiling();
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testPropertyRoundHalfEven() {
        MutableDateTime mdt = new MutableDateTime(1500L);
        MutableDateTime.Property prop = mdt.millisOfSecond();
        prop.roundHalfEven();
        assertEquals(2000L, mdt.getMillis());
    }

    @Test(timeout = 4000)
    public void testPropertyGetField() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.year();
        assertNotNull(prop.getField());
    }

    @Test(timeout = 4000)
    public void testPropertyGetMillis() {
        MutableDateTime mdt = new MutableDateTime(1000L);
        MutableDateTime.Property prop = mdt.year();
        assertEquals(1000L, prop.getMillis());
    }

    @Test(timeout = 4000)
    public void testPropertyGetChronology() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.year();
        assertNotNull(prop.getChronology());
    }

    @Test(timeout = 4000)
    public void testPropertyGetMutableDateTime() {
        MutableDateTime mdt = new MutableDateTime(0L);
        MutableDateTime.Property prop = mdt.year();
        assertSame(mdt, prop.getMutableDateTime());
    }

    @Test(timeout = 4000)
    public void testPropertyChaining() {
        MutableDateTime mdt = new MutableDateTime(0L);
        mdt.year().add(1).monthOfYear().set(6);
        assertEquals(1, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
    }
}