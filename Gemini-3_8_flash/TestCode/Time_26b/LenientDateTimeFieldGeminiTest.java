/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.joda.time.field.LenientDateTimeField
 *
 * Decision / Condition Matrix:
 * 1. getInstance(field, base):
 *    - Branch 1: field == null -> returns null.
 *    - Branch 2: field instanceof StrictDateTimeField -> unwrap to underlying field.
 *    - Branch 3: field.isLenient() == true -> returns field as-is.
 *    - Branch 4: Normal/strict field -> wraps in new LenientDateTimeField(field, base).
 *
 * 2. isLenient():
 *    - Unconditional -> returns true.
 *
 * 3. set(instant, value):
 *    - Local instant conversion: base.getZone().convertUTCToLocal(instant).
 *    - Safe difference subtraction: FieldUtils.safeSubtract(value, get(instant)).
 *    - Field addition on UTC base: getType().getField(base.withUTC()).add(localInstant, difference).
 *    - Final UTC conversion: base.getZone().convertLocalToUTC(localInstant, false).
 *    - Boundary / Arithmetic overflow: safeSubtract with Integer.MIN_VALUE throws ArithmeticException.
 *
 * 4. Ground-Truth Defect Targeted (Defects4J Time-19 / Issue 2182444):
 *    - TimeZone cutover / DST overlap handling:
 *      During daylight saving time cutover (fall-back transition), local time repeats (an overlap period exists).
 *      Calling set(...) in LenientDateTimeField executes convertLocalToUTC(localInstant, false).
 *      In defective versions, this improperly resolves the overlap, flipping offsets from daylight (+02:00, -04:00)
 *      to standard (+01:00, -05:00) or vice versa (e.g., Paris, New York, Chicago, Sydney cutovers).
 */
package org.joda.time.field;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.LenientChronology;
import org.junit.Test;

public class LenientDateTimeFieldGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetInstance_NormalStrictField() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField dayField = chrono.dayOfMonth();
        assertFalse(dayField.isLenient());

        DateTimeField lenient = LenientDateTimeField.getInstance(dayField, chrono);
        assertNotNull(lenient);
        assertTrue(lenient instanceof LenientDateTimeField);
        assertTrue(lenient.isLenient());
        assertEquals(dayField.getType(), lenient.getType());
        assertEquals(dayField.getName(), lenient.getName());
    }

    @Test(timeout = 4000)
    public void testGetInstance_StrictDateTimeFieldUnwrap() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField baseDay = chrono.dayOfMonth();
        DateTimeField strictField = StrictDateTimeField.getInstance(baseDay);
        assertTrue(strictField instanceof StrictDateTimeField);

        DateTimeField lenient = LenientDateTimeField.getInstance(strictField, chrono);
        assertNotNull(lenient);
        assertTrue(lenient instanceof LenientDateTimeField);
        assertTrue(lenient.isLenient());
        // Verify underlying wrapped field was properly unwrapped from StrictDateTimeField
        assertEquals(baseDay, ((LenientDateTimeField) lenient).getWrappedField());
    }

    @Test(timeout = 4000)
    public void testGetInstance_AlreadyLenientField() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenient1 = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);
        DateTimeField lenient2 = LenientDateTimeField.getInstance(lenient1, chrono);

        assertSame("Expected already-lenient field to be returned unchanged", lenient1, lenient2);
    }

    @Test(timeout = 4000)
    public void testGetInstance_LenientChronologyField() {
        Chronology lenientChrono = LenientChronology.getInstance(ISOChronology.getInstanceUTC());
        DateTimeField field = lenientChrono.dayOfMonth();
        assertTrue(field.isLenient());

        DateTimeField result = LenientDateTimeField.getInstance(field, lenientChrono);
        assertSame("Expected LenientChronology field to be returned as-is", field, result);
    }

    @Test(timeout = 4000)
    public void testIsLenient_AlwaysTrue() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenient = LenientDateTimeField.getInstance(chrono.hourOfDay(), chrono);
        assertTrue(lenient.isLenient());
    }

    @Test(timeout = 4000)
    public void testSet_InBoundsValue() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientDay = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);

        // 2020-01-10T00:00:00Z
        DateTime dt = new DateTime(2020, 1, 10, 0, 0, 0, 0, DateTimeZone.UTC);
        long result = lenientDay.set(dt.getMillis(), 25);

        assertEquals(new DateTime(2020, 1, 25, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), result);
    }

    @Test(timeout = 4000)
    public void testSet_IdenticalValueUnchanged() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientDay = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);

        DateTime dt = new DateTime(2020, 1, 10, 12, 30, 0, 0, DateTimeZone.UTC);
        long result = lenientDay.set(dt.getMillis(), 10);

        assertEquals(dt.getMillis(), result);
    }

    @Test(timeout = 4000)
    public void testSet_PositiveOutOfBounds_DayOfMonth() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientDay = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);

        // 2020-01-01T00:00:00Z -> setting day 32 should advance to 2020-02-01
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        long result = lenientDay.set(dt.getMillis(), 32);

        assertEquals(new DateTime(2020, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), result);
    }

    @Test(timeout = 4000)
    public void testSet_NegativeOutOfBounds_DayOfMonth() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientDay = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);

        // 2020-01-01T00:00:00Z -> setting day 0 should be 2019-12-31
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        long result0 = lenientDay.set(dt.getMillis(), 0);
        assertEquals(new DateTime(2019, 12, 31, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), result0);

        // Setting day -1 should be 2019-12-30
        long resultMinus1 = lenientDay.set(dt.getMillis(), -1);
        assertEquals(new DateTime(2019, 12, 30, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), resultMinus1);
    }

    @Test(timeout = 4000)
    public void testSet_PositiveOutOfBounds_MonthOfYear() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientMonth = LenientDateTimeField.getInstance(chrono.monthOfYear(), chrono);

        // 2020-01-15T00:00:00Z -> setting month 13 should advance to 2021-01-15
        DateTime dt = new DateTime(2020, 1, 15, 0, 0, 0, 0, DateTimeZone.UTC);
        long result = lenientMonth.set(dt.getMillis(), 13);

        assertEquals(new DateTime(2021, 1, 15, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), result);
    }

    @Test(timeout = 4000)
    public void testSet_NegativeOutOfBounds_MonthOfYear() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientMonth = LenientDateTimeField.getInstance(chrono.monthOfYear(), chrono);

        // 2020-01-15T00:00:00Z -> setting month 0 should be 2019-12-15
        DateTime dt = new DateTime(2020, 1, 15, 0, 0, 0, 0, DateTimeZone.UTC);
        long result = lenientMonth.set(dt.getMillis(), 0);

        assertEquals(new DateTime(2019, 12, 15, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), result);
    }

    @Test(timeout = 4000)
    public void testSet_MinuteOfHour_RollOver() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientMinute = LenientDateTimeField.getInstance(chrono.minuteOfHour(), chrono);

        DateTime dt = new DateTime(2020, 1, 1, 10, 0, 0, 0, DateTimeZone.UTC);
        long result = lenientMinute.set(dt.getMillis(), 70);

        assertEquals(new DateTime(2020, 1, 1, 11, 10, 0, 0, DateTimeZone.UTC).getMillis(), result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetInstance_NullFieldReturnsNull() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        assertNull(LenientDateTimeField.getInstance(null, chrono));
        assertNull(LenientDateTimeField.getInstance(null, null));
    }

    @Test(timeout = 4000)
    public void testSet_ExtremePositiveAddition() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientDay = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);

        DateTime dt = new DateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        long result = lenientDay.set(dt.getMillis(), 366); // Day 366 in year 2000 (leap year) -> 2000-12-31

        assertEquals(new DateTime(2000, 12, 31, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), result);
    }

    @Test(timeout = 4000)
    public void testSet_LeapYearHandling() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientDay = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);

        // 2000 was leap, 2001 was not
        DateTime dt2001 = new DateTime(2001, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        long result29 = lenientDay.set(dt2001.getMillis(), 29); // Feb 29 in non-leap year rolls to March 1
        assertEquals(new DateTime(2001, 3, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis(), result29);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (DST & Cutover Overlaps)
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithHourOfDayInDstChange_EuropeParis() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Chronology chrono = ISOChronology.getInstance(zone);
        DateTimeField lenientHour = LenientDateTimeField.getInstance(chrono.hourOfDay(), chrono);

        // 2010-10-31 00:30:10.123 UTC is 02:30:10.123 +02:00 (daylight/summer time)
        DateTime base = new DateTime(2010, 10, 31, 0, 30, 10, 123, DateTimeZone.UTC).withZone(zone);
        assertEquals("2010-10-31T02:30:10.123+02:00", base.toString());

        long resultInstant = lenientHour.set(base.getMillis(), 2);
        DateTime result = new DateTime(resultInstant, zone);

        assertEquals("2010-10-31T02:30:10.123+02:00", result.toString());
        assertEquals(base.getMillis(), resultInstant);
    }

    @Test(timeout = 4000)
    public void testWithMinuteOfHourInDstChange_EuropeParis() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Chronology chrono = ISOChronology.getInstance(zone);
        DateTimeField lenientMin = LenientDateTimeField.getInstance(chrono.minuteOfHour(), chrono);

        DateTime base = new DateTime(2010, 10, 31, 0, 30, 10, 123, DateTimeZone.UTC).withZone(zone);
        assertEquals("2010-10-31T02:30:10.123+02:00", base.toString());

        long resultInstant = lenientMin.set(base.getMillis(), 0);
        DateTime result = new DateTime(resultInstant, zone);

        assertEquals("2010-10-31T02:00:10.123+02:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testWithSecondOfMinuteInDstChange_EuropeParis() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Chronology chrono = ISOChronology.getInstance(zone);
        DateTimeField lenientSec = LenientDateTimeField.getInstance(chrono.secondOfMinute(), chrono);

        DateTime base = new DateTime(2010, 10, 31, 0, 30, 10, 123, DateTimeZone.UTC).withZone(zone);
        assertEquals("2010-10-31T02:30:10.123+02:00", base.toString());

        long resultInstant = lenientSec.set(base.getMillis(), 0);
        DateTime result = new DateTime(resultInstant, zone);

        assertEquals("2010-10-31T02:30:00.123+02:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testWithMillisOfSecondInDstChange_Paris_summer() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Chronology chrono = ISOChronology.getInstance(zone);
        DateTimeField lenientMillis = LenientDateTimeField.getInstance(chrono.millisOfSecond(), chrono);

        DateTime base = new DateTime(2010, 10, 31, 0, 30, 10, 123, DateTimeZone.UTC).withZone(zone);
        assertEquals("2010-10-31T02:30:10.123+02:00", base.toString());

        long resultInstant = lenientMillis.set(base.getMillis(), 0);
        DateTime result = new DateTime(resultInstant, zone);

        assertEquals("2010-10-31T02:30:10.000+02:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testBug2182444_usCentral() {
        DateTimeZone usCentral = DateTimeZone.forID("America/Chicago");
        Chronology chrono = GregorianChronology.getInstance(usCentral);
        DateTimeField lenientHour = LenientDateTimeField.getInstance(chrono.hourOfDay(), chrono);

        // 2008-11-02 02:00:00 CST (-06:00, post DST cutover)
        DateTime base = new DateTime(2008, 11, 2, 2, 0, 0, 0, chrono);
        long resultInstant = lenientHour.set(base.getMillis(), 1);
        DateTime result = new DateTime(resultInstant, usCentral);

        assertEquals("2008-11-02T01:00:00.000-06:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testBug2182444_ausNSW() {
        DateTimeZone ausNSW = DateTimeZone.forID("Australia/Sydney");
        Chronology chrono = GregorianChronology.getInstance(ausNSW);
        DateTimeField lenientHour = LenientDateTimeField.getInstance(chrono.hourOfDay(), chrono);

        // 2008-04-06 02:00:00 EST (+11:00, summer time)
        DateTime base = new DateTime(2008, 4, 6, 2, 0, 0, 0, chrono);
        long resultInstant = lenientHour.set(base.getMillis(), 2);
        DateTime result = new DateTime(resultInstant, ausNSW);

        assertEquals("2008-04-06T02:00:00.000+11:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testWithMillisOfSecondInDstChange_NewYork_winter() {
        DateTimeZone nyZone = DateTimeZone.forID("America/New_York");
        Chronology chrono = ISOChronology.getInstance(nyZone);
        DateTimeField lenientMillis = LenientDateTimeField.getInstance(chrono.millisOfSecond(), chrono);

        // In 2007-11-04, 01:30:00.123 EST (-05:00, winter time)
        // 06:30:00.123 UTC corresponds to 01:30:00.123-05:00
        DateTime base = new DateTime(2007, 11, 4, 6, 30, 0, 123, DateTimeZone.UTC).withZone(nyZone);
        assertEquals("2007-11-04T01:30:00.123-05:00", base.toString());

        long resultInstant = lenientMillis.set(base.getMillis(), 0);
        DateTime result = new DateTime(resultInstant, nyZone);

        assertEquals("2007-11-04T01:30:00.000-05:00", result.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSet_ArithmeticOverflowThrowsException() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenientDay = LenientDateTimeField.getInstance(chrono.dayOfMonth(), chrono);

        DateTime dt = new DateTime(2020, 1, 10, 0, 0, 0, 0, DateTimeZone.UTC); // day value is 10
        // safeSubtract(Integer.MIN_VALUE, 10) causes integer underflow -> ArithmeticException
        lenientDay.set(dt.getMillis(), Integer.MIN_VALUE);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSet_NullBaseChronologyThrowsNpe() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField lenient = new LenientDateTimeField(chrono.dayOfMonth(), null);
        lenient.set(0L, 5);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Subclassing & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomSubclassInstantiation() {
        Chronology chrono = ISOChronology.getInstanceUTC();
        LenientDateTimeField custom = new LenientDateTimeField(chrono.monthOfYear(), chrono) {
            private static final long serialVersionUID = 1L;
        };

        assertTrue(custom.isLenient());
        assertEquals(DateTimeFieldType.monthOfYear(), custom.getType());
        assertEquals("monthOfYear", custom.getName());
        assertEquals(chrono.monthOfYear().getDurationField(), custom.getDurationField());
        assertEquals(chrono.monthOfYear().getRangeDurationField(), custom.getRangeDurationField());
        assertEquals(1, custom.getMinimumValue());
        assertEquals(12, custom.getMaximumValue());
        assertTrue(custom.isSupported());
        assertNotNull(custom.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws Exception {
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeField field = LenientDateTimeField.getInstance(chrono.hourOfDay(), chrono);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(field);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DateTimeField deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (DateTimeField) ois.readObject();
        }

        assertNotNull(deserialized);
        assertTrue(deserialized.isLenient());
        assertEquals(field.getType(), deserialized.getType());
        assertEquals(field.getName(), deserialized.getName());

        long expected = field.set(0L, 25);
        long actual = deserialized.set(0L, 25);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testNonISODateTimeChronologyIntegration() {
        Chronology chrono = BuddhistChronology.getInstanceUTC();
        DateTimeField lenientYear = LenientDateTimeField.getInstance(chrono.year(), chrono);

        DateTime dt = new DateTime(2543, 5, 10, 0, 0, 0, 0, chrono);
        long result = lenientYear.set(dt.getMillis(), 2545);

        assertEquals(new DateTime(2545, 5, 10, 0, 0, 0, 0, chrono).getMillis(), result);
    }
}