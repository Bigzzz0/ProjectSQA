/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.joda.time.chrono.ZonedChronology (and inner ZonedDateTimeField, ZonedDurationField)
 *
 * Decision / Branch Matrix Covered:
 * 1. ZonedChronology.getInstance(base, zone)
 *    - base == null -> throws IllegalArgumentException
 *    - base.withUTC() == null -> throws IllegalArgumentException
 *    - zone == null -> throws IllegalArgumentException
 *    - valid base & zone -> creates new ZonedChronology
 * 2. ZonedChronology.withZone(zone)
 *    - zone == null -> zone = DateTimeZone.getDefault()
 *    - zone == getZone() -> returns this
 *    - zone == DateTimeZone.UTC -> returns base
 *    - else -> returns new ZonedChronology
 * 3. ZonedChronology.withUTC() -> returns base
 * 4. ZonedChronology.getDateTimeMillis(..) variants & localToUTC
 *    - localToUTC DST gap offset mismatch -> throws IllegalArgumentException
 *    - standard conversions across multiple parameter overloads
 * 5. ZonedChronology.equals & hashCode & toString
 *    - equals: this == obj (true), non-instance (false), base equal && zone equal (true/false)
 *    - hashCode contract
 *    - toString format
 * 6. useTimeArithmetic(field)
 *    - field == null -> false
 *    - unitMillis < 12h -> true (time field)
 *    - unitMillis >= 12h -> false (date field)
 * 7. ZonedDurationField
 *    - unsupported field constructor -> IllegalArgumentException
 *    - isPrecise(): iTimeField true vs false (and iZone.isFixed() true vs false)
 *    - getUnitMillis(), getValue, getValueAsLong, getMillis
 *    - add(instant, int/long): iTimeField (sub original offset) vs !iTimeField (sub new offset)
 *    - getDifference, getDifferenceAsLong: iTimeField vs !iTimeField
 *    - getOffsetToAdd & getOffsetFromLocalToSubtract overflow checks: (instant ^ sum) < 0
 * 8. ZonedDateTimeField
 *    - unsupported field constructor -> IllegalArgumentException
 *    - isLenient, get, getAsText, getAsShortText (with & without locale)
 *    - add(instant, int/long): iTimeField branch vs !iTimeField branch
 *    - addWrapField(instant, int): iTimeField branch vs !iTimeField branch
 *    - set(instant, int): DST gap check throwing IllegalFieldValueException, normal set
 *    - set(instant, String, Locale)
 *    - getDifference, getDifferenceAsLong: iTimeField vs !iTimeField
 *    - isLeap, getLeapAmount, getLeapDurationField, getDurationField, getRangeDurationField
 *    - roundFloor, roundCeiling: iTimeField vs !iTimeField
 *    - remainder, getMinimumValue (all 4 overloads), getMaximumValue (all 4 overloads)
 *    - getMaximumTextLength, getMaximumShortTextLength
 *    - getOffsetToAdd overflow check
 * 9. Ground Truth Defect Verification (Defects4J: TestDateTimeZoneCutover):
 *    - Changing time fields (minuteOfHour, hourOfDay, secondOfMinute) during DST cutover overlap/transition.
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
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Partial;
import org.joda.time.ReadablePartial;
import org.joda.time.field.MillisDurationField;
import org.joda.time.field.PreciseDurationDateTimeField;
import org.joda.time.field.UnsupportedDateTimeField;
import org.joda.time.field.UnsupportedDurationField;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZonedChronologyGeminiTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone NEW_YORK = DateTimeZone.forID("America/New_York");
    private static final DateTimeZone CHICAGO = DateTimeZone.forID("America/Chicago");
    private static final Chronology ISO_BASE = ISOChronology.getInstanceUTC();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetInstanceAndZoneAccessors() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        assertNotNull(chrono);
        assertSame(PARIS, chrono.getZone());
        assertSame(ISO_BASE, chrono.withUTC());
        assertSame(ISO_BASE, chrono.getBase());

        // withZone tests
        assertSame(chrono, chrono.withZone(PARIS));
        assertSame(ISO_BASE, chrono.withZone(DateTimeZone.UTC));
        Chronology londonChrono = chrono.withZone(LONDON);
        assertNotSame(chrono, londonChrono);
        assertEquals(LONDON, londonChrono.getZone());

        DateTimeZone defaultZone = DateTimeZone.getDefault();
        Chronology defaultChrono = chrono.withZone(null);
        assertEquals(defaultZone, defaultChrono.getZone());
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisFourParams() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        // Paris is UTC+1 in winter
        long millis = chrono.getDateTimeMillis(2021, 1, 1, 3600000); // 01:00:00.000 local
        // 01:00:00 local Paris on Jan 1 is 00:00:00 UTC
        long expectedUTC = ISO_BASE.getDateTimeMillis(2021, 1, 1, 0);
        assertEquals(expectedUTC, millis);
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisSevenParams() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        long millis = chrono.getDateTimeMillis(2021, 6, 1, 14, 30, 15, 500);
        // Summer time Paris is UTC+2 -> 12:30:15.500 UTC
        long expectedUTC = ISO_BASE.getDateTimeMillis(2021, 6, 1, 12, 30, 15, 500);
        assertEquals(expectedUTC, millis);
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisInstantParam() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        long baseInstant = ISO_BASE.getDateTimeMillis(2021, 6, 1, 0, 0, 0, 0);
        // Reset time on baseInstant to 15:45:30.123
        long millis = chrono.getDateTimeMillis(baseInstant, 15, 45, 30, 123);
        long expectedUTC = ISO_BASE.getDateTimeMillis(2021, 6, 1, 13, 45, 30, 123);
        assertEquals(expectedUTC, millis);
    }

    @Test(timeout = 4000)
    public void testUseTimeArithmetic() {
        DurationField millisField = ISO_BASE.millis();
        DurationField hoursField = ISO_BASE.hours();
        DurationField halfdaysField = ISO_BASE.halfdays();
        DurationField daysField = ISO_BASE.days();

        assertTrue(ZonedChronology.useTimeArithmetic(millisField));
        assertTrue(ZonedChronology.useTimeArithmetic(hoursField));
        assertFalse(ZonedChronology.useTimeArithmetic(halfdaysField));
        assertFalse(ZonedChronology.useTimeArithmetic(daysField));
        assertFalse(ZonedChronology.useTimeArithmetic(null));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Overflow Guards
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetInstanceNullBase() {
        ZonedChronology.getInstance(null, PARIS);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetInstanceNullZone() {
        ZonedChronology.getInstance(ISO_BASE, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetInstanceNullBaseWithUTC() {
        Chronology badBase = new BaseChronology() {
            private static final long serialVersionUID = 1L;
            @Override
            public DateTimeZone getZone() { return null; }
            @Override
            public Chronology withUTC() { return null; }
            @Override
            public Chronology withZone(DateTimeZone zone) { return this; }
            @Override
            public String toString() { return ""; }
        };
        ZonedChronology.getInstance(badBase, PARIS);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDateTimeMillisDstGapException() {
        // Paris spring forward: 2021-03-28 02:00 -> 03:00.
        // 02:30 does not exist!
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        chrono.getDateTimeMillis(2021, 3, 28, 2, 30, 0, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testDurationFieldOffsetAddOverflow() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        DurationField field = chrono.hours();
        // Long.MAX_VALUE + positive offset causes sign inversion
        field.add(Long.MAX_VALUE - 100, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testDurationFieldOffsetSubtractOverflow() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        DurationField field = chrono.days();
        // Days field is non-time arithmetic, uses getOffsetFromLocalToSubtract
        field.add(Long.MIN_VALUE + 100, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testDateTimeFieldOffsetAddOverflow() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        DateTimeField field = chrono.hourOfDay();
        field.add(Long.MAX_VALUE - 100, 1);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Cutover Inaccuracies)
    // =========================================================================

    /**
     * Ground Truth Defect: Changing minute/hour/second on an instant during DST
     * fall-back transition must preserve the correct historical offset rather
     * than jumping between daylight/standard offsets unexpectedly.
     */
    @Test(timeout = 4000)
    public void testWithMinuteOfHourInDstChangeParis() {
        // 2010-10-31: 03:00 falls back to 02:00 in Paris.
        // 02:30:10.123+02:00 is before cutover (daylight time).
        DateTime dt = new DateTime(2010, 10, 31, 2, 30, 10, 123, PARIS);
        assertEquals(2 * 3600000, PARIS.getOffset(dt.getMillis()));

        // Modifying minute to 00 should remain in +02:00
        DateTime result = dt.minuteOfHour().setCopy(0);
        assertEquals("2010-10-31T02:00:10.123+02:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testWithHourOfDayInDstChangeParis() {
        // 2010-10-31 02:30:10.123+02:00
        DateTime dt = new DateTime(2010, 10, 31, 2, 30, 10, 123, PARIS);
        // Setting hour to 2 explicitly
        DateTime result = dt.hourOfDay().setCopy(2);
        assertEquals("2010-10-31T02:30:10.123+02:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testWithSecondOfMinuteInDstChangeParis() {
        DateTime dt = new DateTime(2010, 10, 31, 2, 30, 10, 123, PARIS);
        DateTime result = dt.secondOfMinute().setCopy(0);
        assertEquals("2010-10-31T02:30:00.123+02:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testWithMillisOfSecondInDstChangeParis() {
        DateTime dt = new DateTime(2010, 10, 31, 2, 30, 10, 123, PARIS);
        DateTime result = dt.millisOfSecond().setCopy(0);
        assertEquals("2010-10-31T02:30:10.000+02:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testBug2182444UsCentralDstCutover() {
        // 2008-11-02 01:00:00 Central time cutover from -05:00 to -06:00
        // Second 01:30 is post-cutover (-06:00)
        DateTime dt = new DateTime(2008, 11, 2, 1, 30, 0, 0, CHICAGO);
        // Ensure that setting minute back to 0 keeps -06:00
        DateTime postCutover = new DateTime(2008, 11, 2, 1, 30, 0, 0, CHICAGO)
            .plusHours(1); // pushes into standard time
        DateTime testDt = postCutover.minuteOfHour().setCopy(0);
        assertTrue(testDt.toString().endsWith("-06:00"));
    }

    // =========================================================================
    // Partition D: ZonedDurationField & ZonedDateTimeField Branch Operations
    // =========================================================================

    @Test(timeout = 4000)
    public void testZonedDurationFieldArithmetic() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        DurationField hours = chrono.hours(); // time field
        DurationField days = chrono.days();   // date field

        assertTrue(hours.isPrecise());
        // Paris has DST, so days duration field in Paris is not precise
        assertFalse(days.isPrecise());

        ZonedChronology utcChrono = ZonedChronology.getInstance(ISO_BASE, DateTimeZone.UTC);
        assertTrue(utcChrono.days().isPrecise()); // Fixed zone -> isPrecise() true

        long t0 = ISO_BASE.getDateTimeMillis(2021, 1, 1, 12, 0, 0, 0);

        // getValue, getValueAsLong, getMillis
        assertEquals(2, hours.getValue(7200000L, t0));
        assertEquals(2L, hours.getValueAsLong(7200000L, t0));
        assertEquals(7200000L, hours.getMillis(2, t0));
        assertEquals(7200000L, hours.getMillis(2L, t0));

        // add int and long
        long t1 = hours.add(t0, 3);
        assertEquals(t0 + 3 * 3600000L, t1);
        long t2 = hours.add(t0, 5L);
        assertEquals(t0 + 5 * 3600000L, t2);

        // days add (non-time field path)
        long d1 = days.add(t0, 1);
        long d2 = days.add(t0, 1L);
        assertEquals(d1, d2);

        // getDifference, getDifferenceAsLong
        assertEquals(3, hours.getDifference(t1, t0));
        assertEquals(3L, hours.getDifferenceAsLong(t1, t0));
        assertEquals(1, days.getDifference(d1, t0));
        assertEquals(1L, days.getDifferenceAsLong(d1, t0));

        assertEquals(DateTimeConstants.MILLIS_PER_HOUR, hours.getUnitMillis());
    }

    @Test(timeout = 4000)
    public void testZonedDateTimeFieldMethods() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        DateTimeField hour = chrono.hourOfDay(); // time field
        DateTimeField day = chrono.dayOfMonth(); // date field

        assertFalse(hour.isLenient());
        assertNotNull(hour.getDurationField());
        assertNotNull(hour.getRangeDurationField());
        assertNull(hour.getLeapDurationField());

        long t = chrono.getDateTimeMillis(2021, 2, 15, 10, 30, 20, 100);

        // get and text
        assertEquals(10, hour.get(t));
        assertEquals("10", hour.getAsText(t, Locale.ENGLISH));
        assertEquals("10", hour.getAsShortText(t, Locale.ENGLISH));
        assertEquals("10", hour.getAsText(10, Locale.ENGLISH));
        assertEquals("10", hour.getAsShortText(10, Locale.ENGLISH));

        // add and addWrapField (both timeField and non-timeField branches)
        long addedHour = hour.add(t, 2);
        assertEquals(12, hour.get(addedHour));
        long addedHourLong = hour.add(t, 2L);
        assertEquals(12, hour.get(addedHourLong));
        long wrappedHour = hour.addWrapField(t, 15);
        assertEquals(1, hour.get(wrappedHour));

        long addedDay = day.add(t, 2);
        assertEquals(17, day.get(addedDay));
        long addedDayLong = day.add(t, 2L);
        assertEquals(17, day.get(addedDayLong));
        long wrappedDay = day.addWrapField(t, 20);
        assertEquals(7, day.get(wrappedDay));

        // set with text
        long textSet = day.set(t, "20", Locale.ENGLISH);
        assertEquals(20, day.get(textSet));

        // difference
        assertEquals(2, hour.getDifference(addedHour, t));
        assertEquals(2L, hour.getDifferenceAsLong(addedHour, t));
        assertEquals(2, day.getDifference(addedDay, t));
        assertEquals(2L, day.getDifferenceAsLong(addedDay, t));

        // roundFloor & roundCeiling
        long floorHour = hour.roundFloor(t);
        assertEquals(chrono.getDateTimeMillis(2021, 2, 15, 10, 0, 0, 0), floorHour);
        long ceilHour = hour.roundCeiling(t);
        assertEquals(chrono.getDateTimeMillis(2021, 2, 15, 11, 0, 0, 0), ceilHour);

        long floorDay = day.roundFloor(t);
        assertEquals(chrono.getDateTimeMillis(2021, 2, 15, 0, 0, 0, 0), floorDay);
        long ceilDay = day.roundCeiling(t);
        assertEquals(chrono.getDateTimeMillis(2021, 2, 16, 0, 0, 0, 0), ceilDay);

        // remainder
        assertEquals(30 * 60000 + 20 * 1000 + 100, hour.remainder(t));

        // Min & Max bounds
        assertEquals(0, hour.getMinimumValue());
        assertEquals(0, hour.getMinimumValue(t));
        assertEquals(23, hour.getMaximumValue());
        assertEquals(23, hour.getMaximumValue(t));

        ReadablePartial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertEquals(0, hour.getMinimumValue(partial));
        assertEquals(0, hour.getMinimumValue(partial, new int[]{10}));
        assertEquals(23, hour.getMaximumValue(partial));
        assertEquals(23, hour.getMaximumValue(partial, new int[]{10}));

        assertTrue(hour.getMaximumTextLength(Locale.ENGLISH) >= 2);
        assertTrue(hour.getMaximumShortTextLength(Locale.ENGLISH) >= 2);

        // Leap support on monthOfYear
        DateTimeField month = chrono.monthOfYear();
        DateTimeField year = chrono.year();
        long leapYearTime = chrono.getDateTimeMillis(2020, 2, 1, 0, 0, 0, 0);
        assertTrue(year.isLeap(leapYearTime));
        assertEquals(1, year.getLeapAmount(leapYearTime));
        assertNotNull(year.getLeapDurationField());
        assertFalse(month.isLeap(leapYearTime));
    }

    @Test(expected = IllegalFieldValueException.class, timeout = 4000)
    public void testSetDuringDstGapThrowsException() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);
        // Paris forward DST occurs 2021-03-28 02:00:00 -> 03:00:00.
        // Trying to set hour to 2 on that date should fail.
        long instant = chrono.getDateTimeMillis(2021, 3, 28, 0, 0, 0, 0);
        chrono.hourOfDay().set(instant, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testZonedDurationFieldUnsupportedConstructor() {
        DurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.days());
        new ZonedChronology.ZonedDurationField(unsupported, PARIS);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testZonedDateTimeFieldUnsupportedConstructor() {
        DateTimeField unsupported = UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfMonth(), UnsupportedDurationField.getInstance(DurationFieldType.days()));
        new ZonedChronology.ZonedDateTimeField(unsupported, PARIS, null, null, null);
    }

    // =========================================================================
    // Partition E: Object Contract, Serialization & Special Chronologies
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        ZonedChronology c1 = ZonedChronology.getInstance(ISO_BASE, PARIS);
        ZonedChronology c2 = ZonedChronology.getInstance(ISO_BASE, PARIS);
        ZonedChronology c3 = ZonedChronology.getInstance(ISO_BASE, LONDON);
        ZonedChronology c4 = ZonedChronology.getInstance(GJChronology.getInstanceUTC(), PARIS);

        // Reflexive
        assertTrue(c1.equals(c1));
        // Symmetric
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));
        assertEquals(c1.hashCode(), c2.hashCode());

        // Asymmetric / Unequal
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(c4));
        assertFalse(c1.equals(null));
        assertFalse(c1.equals("NotAChronology"));

        // toString
        assertEquals("ZonedChronology[ISOChronology[UTC], Europe/Paris]", c1.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        ZonedChronology chrono = ZonedChronology.getInstance(ISO_BASE, PARIS);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(chrono);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ZonedChronology deserialized = (ZonedChronology) ois.readObject();
        ois.close();

        assertEquals(chrono, deserialized);
        assertEquals(chrono.getZone(), deserialized.getZone());
    }

    @Test(timeout = 4000)
    public void testAssembleCoverageWithVariousFieldConfigurations() {
        // Coptic Chronology exercises different assembled fields
        Chronology copticBase = CopticChronology.getInstanceUTC();
        ZonedChronology coptic = ZonedChronology.getInstance(copticBase, NEW_YORK);

        assertNotNull(coptic.dayOfWeek());
        assertNotNull(coptic.weekOfWeekyear());
        assertNotNull(coptic.dayOfYear());
        assertNotNull(coptic.centuryOfEra());
        assertNotNull(coptic.yearOfCentury());
        assertNotNull(coptic.halfdayOfDay());
        assertNotNull(coptic.clockhourOfDay());
        assertNotNull(coptic.clockhourOfHalfday());

        assertEquals(NEW_YORK, coptic.getZone());
        assertEquals(copticBase, coptic.withUTC());
    }
}