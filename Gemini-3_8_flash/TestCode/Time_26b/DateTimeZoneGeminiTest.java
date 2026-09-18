package org.joda.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.chrono.GJChronology;
import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.joda.time.DateTimeZone
 * Defect Focus: Transition cutover calculations during DST change (e.g. America/Chicago, Europe/Paris,
 *               Australia/Sydney). Specifically, during overlap/gap transitions where withHourOfDay /
 *               withMinuteOfHour / withSecondOfMinute adjusts local instant and calls convertLocalToUTC,
 *               retaining incorrect offsets across cutovers (Defects4J ground truth: TestDateTimeZoneCutover).
 *
 * Branch & Coverage Matrix:
 * - getDefault() / setDefault(): cDefault null vs non-null; system property "user.timezone" valid/invalid/null;
 *   TimeZone.getDefault() fallback; null parameter check in setDefault.
 * - forID(String): null -> default; "UTC" -> UTC; provider.getZone(id); fixed offset strings "+/-HH:mm";
 *   zero offset "+00:00" -> UTC; unrecognised id -> IllegalArgumentException.
 * - forOffsetHours(int) & forOffsetHoursMinutes(int, int): zero/zero -> UTC; minute < 0 or > 59 -> IllegalArgumentException;
 *   hoursInMinutes arithmetic overflow -> IllegalArgumentException; forOffsetMillis conversions.
 * - forOffsetMillis(int): zero -> UTC; fixedOffsetZone caching & SoftReference retrieval; negative/positive millis.
 * - forTimeZone(TimeZone): null -> default; "UTC" -> UTC; getConvertedId mapping (e.g., "PST", "EST", "MIT", etc.);
 *   "GMT+..." and "GMT-..." parsing; offset == 0 -> UTC; unrecognised -> IllegalArgumentException.
 * - setProvider(Provider) / setNameProvider(NameProvider): null handling; empty available IDs; missing UTC;
 *   invalid UTC zone.
 * - getShortName / getName: locale null vs provided; nameKey null vs found; nameProvider null name fallback to printOffset.
 * - getOffset(ReadableInstant): null instant -> current time vs non-null instant.
 * - isStandardOffset(long): standard vs summer DST offset check.
 * - getOffsetFromLocal(long): normal non-DST; DST boundary with positive/negative offsets; nextLocal != nextAdjusted.
 * - convertUTCToLocal(long): normal; overflow check ((instantUTC ^ instantLocal) < 0 && (instantUTC ^ offset) >= 0).
 * - convertLocalToUTC(long, boolean strict): normal; DST gap strict=true (IllegalArgumentException);
 *   DST gap strict=false (western hemisphere adjustment); arithmetic overflow check.
 * - getMillisKeepLocal(DateTimeZone, long): newZone == null -> default; newZone == this; different zone conversion.
 * - isLocalDateTimeGap(LocalDateTime): fixed zone vs variable zone inside gap vs outside gap.
 * - Serialization: writeReplace -> Stub -> readResolve.
 * ---------------------------------------------------------------------------------------------------------
 */
public class DateTimeZoneGeminiTest {

    private DateTimeZone originalDefault;
    private Provider originalProvider;
    private NameProvider originalNameProvider;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        DateTimeZone.setProvider(originalProvider);
        DateTimeZone.setNameProvider(originalNameProvider);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetDefaultAndSetDefault() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        assertNotNull(defaultZone);

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        assertEquals(paris, DateTimeZone.getDefault());

        DateTimeZone.setDefault(defaultZone);
        assertEquals(defaultZone, DateTimeZone.getDefault());
    }

    @Test(timeout = 4000)
    public void testForID_PredefinedAndCommonZones() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, utc);
        assertEquals("UTC", utc.getID());

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", london.getID());
        assertFalse(london.isFixed());

        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        assertEquals("America/New_York", ny.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursAndMinutes_ValidValues() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone plusTwo = DateTimeZone.forOffsetHours(2);
        assertEquals("+02:00", plusTwo.getID());
        assertEquals(2 * 3600 * 1000, plusTwo.getOffset(0L));

        DateTimeZone minusFive = DateTimeZone.forOffsetHours(-5);
        assertEquals("-05:00", minusFive.getID());
        assertEquals(-5 * 3600 * 1000, minusFive.getOffset(0L));

        DateTimeZone plusFiveThirty = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", plusFiveThirty.getID());
        assertEquals((5 * 60 + 30) * 60 * 1000, plusFiveThirty.getOffset(0L));

        DateTimeZone minusTwoThirty = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals("-02:30", minusTwoThirty.getID());
        assertEquals(-(2 * 60 + 30) * 60 * 1000, minusTwoThirty.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_CachingAndFormatting() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(3600000);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(3600000);
        assertSame("Should return cached fixed offset zone instance", zone1, zone2);
        assertEquals("+01:00", zone1.getID());

        DateTimeZone subSeconds = DateTimeZone.forOffsetMillis(1234);
        assertEquals("+00:00:01.234", subSeconds.getID());
        assertEquals(1234, subSeconds.getOffset(0L));

        DateTimeZone negSeconds = DateTimeZone.forOffsetMillis(-61000);
        assertEquals("-00:01:01", negSeconds.getID());
        assertEquals(-61000, negSeconds.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_SupportedConversions() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        DateTimeZone convertedPST = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", convertedPST.getID());

        DateTimeZone convertedEST = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", convertedEST.getID());

        DateTimeZone gmtPlus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+04:00"));
        assertEquals("+04:00", gmtPlus.getID());

        DateTimeZone gmtZero = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, gmtZero);
    }

    @Test(timeout = 4000)
    public void testNamesAndLocales() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 (GMT)
        long summerInstant = 15778800000L; // 1970-07-02 (BST)

        assertNotNull(zone.getName(winterInstant, Locale.UK));
        assertNotNull(zone.getShortName(winterInstant, Locale.UK));
        assertNotNull(zone.getName(summerInstant));
        assertNotNull(zone.getShortName(summerInstant));

        // When locale is null, should fall back to default locale
        assertEquals(zone.getName(winterInstant, Locale.getDefault()), zone.getName(winterInstant, null));
        assertEquals(zone.getShortName(winterInstant, Locale.getDefault()), zone.getShortName(winterInstant, null));
    }

    @Test(timeout = 4000)
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        ReadableInstant instant = new Instant(0L);
        assertEquals(zone.getOffset(0L), zone.getOffset(instant));

        int nowOffset = zone.getOffset((ReadableInstant) null);
        assertTrue(nowOffset == 3600000 || nowOffset == 7200000);
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long winterInstant = 0L; // Jan 1970: +01:00 standard
        long summerInstant = 15778800000L; // Jul 1970: +02:00 DST

        assertTrue(zone.isStandardOffset(winterInstant));
        assertFalse(zone.isStandardOffset(summerInstant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");

        long instant = 0L;
        // Paris is +1 hour relative to London at instant 0
        long keepLocalParis = london.getMillisKeepLocal(paris, instant);
        assertEquals(-3600000L, keepLocalParis);

        // Same zone
        assertEquals(instant, london.getMillisKeepLocal(london, instant));

        // Null zone defaults to default
        DateTimeZone.setDefault(london);
        assertEquals(instant, london.getMillisKeepLocal(null, instant));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Spring forward gap on 2007-03-11: 02:00 -> 03:00 does not exist
        LocalDateTime gapTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapTime));

        LocalDateTime validTime = new LocalDateTime(2007, 3, 11, 1, 30, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(validTime));

        // Fixed zone never has a gap
        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapTime));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testForID_NullReturnsDefault() {
        DateTimeZone def = DateTimeZone.getDefault();
        assertSame(def, DateTimeZone.forID(null));
    }

    @Test(timeout = 4000)
    public void testForID_FixedOffsetBoundaries() {
        DateTimeZone maxPositive = DateTimeZone.forID("+23:59:59.999");
        assertEquals(23 * 3600000 + 59 * 60000 + 59999, maxPositive.getOffset(0L));

        DateTimeZone maxNegative = DateTimeZone.forID("-23:59:59.999");
        assertEquals(-(23 * 3600000 + 59 * 60000 + 59999), maxNegative.getOffset(0L));

        DateTimeZone zeroPlus = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zeroPlus);

        DateTimeZone zeroMinus = DateTimeZone.forID("-00:00");
        assertSame(DateTimeZone.UTC, zeroMinus);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_Overflow() {
        DateTimeZone plusOne = DateTimeZone.forOffsetHours(1);
        try {
            plusOne.convertUTCToLocal(Long.MAX_VALUE - 10);
            fail("Expected ArithmeticException on positive overflow");
        } catch (ArithmeticException expected) {
            // Success
        }

        DateTimeZone minusOne = DateTimeZone.forOffsetHours(-1);
        try {
            minusOne.convertUTCToLocal(Long.MIN_VALUE + 10);
            fail("Expected ArithmeticException on negative underflow");
        } catch (ArithmeticException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Overflow() {
        DateTimeZone minusOne = DateTimeZone.forOffsetHours(-1);
        try {
            minusOne.convertLocalToUTC(Long.MAX_VALUE - 10, false);
            fail("Expected ArithmeticException when subtracting negative offset causes overflow");
        } catch (ArithmeticException expected) {
            // Success
        }

        DateTimeZone plusOne = DateTimeZone.forOffsetHours(1);
        try {
            plusOne.convertLocalToUTC(Long.MIN_VALUE + 10, false);
            fail("Expected ArithmeticException when subtracting positive offset causes underflow");
        } catch (ArithmeticException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_GapHandling() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Gap instant: 2007-03-11 02:30:00 EST does not exist
        long gapLocalMillis = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();

        try {
            zone.convertLocalToUTC(gapLocalMillis, true);
            fail("Expected IllegalArgumentException in strict mode for DST gap");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Illegal instant due to time zone offset transition"));
        }

        // When strict is false, it should compute without throwing
        long utcResult = zone.convertLocalToUTC(gapLocalMillis, false);
        assertTrue(utcResult != 0L);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Cutover Transitions)
    // =========================================================================

    /**
     * Targets defects in DST cutover where adjusting fields like hourOfDay,
     * minuteOfHour, or secondOfMinute during an overlap/gap transition caused an
     * incorrect offset to be calculated.
     */
    @Test(timeout = 4000)
    public void testDefectCutover_EuropeParis_WithHourOfDay() {
        // Paris autumn cutover: 2010-10-31 from 03:00 (+02:00) back to 02:00 (+01:00)
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTime base = new DateTime(2010, 10, 31, 1, 30, 10, 123, zone);
        DateTime test = base.withHourOfDay(2);
        assertEquals("2010-10-31T02:30:10.123+02:00", test.toString());
    }

    @Test(timeout = 4000)
    public void testDefectCutover_EuropeParis_WithMinuteOfHour() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTime base = new DateTime(2010, 10, 31, 2, 30, 10, 123, zone);
        DateTime test = base.withMinuteOfHour(0);
        assertEquals("2010-10-31T02:00:10.123+02:00", test.toString());
    }

    @Test(timeout = 4000)
    public void testDefectCutover_EuropeParis_WithSecondOfMinute() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTime base = new DateTime(2010, 10, 31, 2, 30, 10, 123, zone);
        DateTime test = base.withSecondOfMinute(0);
        assertEquals("2010-10-31T02:30:00.123+02:00", test.toString());
    }

    @Test(timeout = 4000)
    public void testDefectCutover_USCentral_Bug2182444() {
        // Chicago autumn cutover: 2008-11-02 02:00 moves to 01:00
        Chronology chronUS = GJChronology.getInstance(DateTimeZone.forID("America/Chicago"));
        DateTime dt = new DateTime(2008, 11, 2, 2, 0, 0, 0, chronUS);
        DateTime dt2 = dt.withHourOfDay(1);
        assertEquals("2008-11-02T01:00:00.000-06:00", dt2.toString());
    }

    @Test(timeout = 4000)
    public void testDefectCutover_AusNSW_Bug2182444() {
        // Sydney autumn cutover: 2008-04-06 03:00 moves to 02:00
        Chronology chronNSW = GJChronology.getInstance(DateTimeZone.forID("Australia/Sydney"));
        DateTime dt = new DateTime(2008, 4, 6, 3, 0, 0, 0, chronNSW);
        DateTime dt2 = dt.withHourOfDay(2);
        assertEquals("2008-04-06T02:00:00.000+11:00", dt2.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_NullThrows() {
        DateTimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_UnrecognisedThrows() {
        DateTimeZone.forID("Invalid/NonExistentZone");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_NegativeMinuteThrows() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinuteOver59Throws() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_OverflowThrows() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 30);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_UnrecognisedThrows() {
        TimeZone tz = TimeZone.getTimeZone("CustomUnknownId");
        tz.setID("CustomUnknownId");
        DateTimeZone.forTimeZone(tz);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_EmptyIdsThrows() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() { return Collections.emptySet(); }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_NoUTCThrows() {
        final Set<String> ids = new HashSet<String>();
        ids.add("Europe/London");
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() { return ids; }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_InvalidUTCZoneThrows() {
        final Set<String> ids = new HashSet<String>();
        ids.add("UTC");
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return DateTimeZone.forOffsetHours(1); // Invalid UTC zone (not UTC)
            }
            public Set<String> getAvailableIDs() { return ids; }
        });
    }

    @Test(timeout = 4000)
    public void testSetProviderAndNameProvider_NullResetsToDefault() {
        DateTimeZone.setProvider(null);
        assertNotNull(DateTimeZone.getProvider());
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));

        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");

        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());

        assertFalse(zone1.equals(zoneParis));
        assertFalse(zone1.equals(null));
        assertFalse(zone1.equals("Europe/London"));
    }

    @Test(timeout = 4000)
    public void testToStringAndToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        assertEquals("America/Chicago", zone.toString());

        TimeZone tz = zone.toTimeZone();
        assertNotNull(tz);
        assertEquals("America/Chicago", tz.getID());
    }

    @Test(timeout = 4000)
    public void testTransitions() {
        DateTimeZone fixed = DateTimeZone.UTC;
        assertTrue(fixed.isFixed());
        assertEquals(1000L, fixed.nextTransition(1000L));
        assertEquals(1000L, fixed.previousTransition(1000L));

        DateTimeZone variable = DateTimeZone.forID("Europe/London");
        assertFalse(variable.isFixed());
        long next = variable.nextTransition(0L);
        assertTrue(next > 0L);
        long prev = variable.previousTransition(next);
        assertTrue(prev <= next);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame("Deserialization of DateTimeZone should resolve to canonical instance", zone, deserialized);
    }

    @Test(timeout = 4000)
    public void testProtectedConstructor_NullIdThrows() throws Exception {
        Constructor<DateTimeZone> constructor = DateTimeZone.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        try {
            // Instantiate anonymous subclass via reflection
            new DateTimeZone(null) {
                private static final long serialVersionUID = 1L;
                public String getNameKey(long instant) { return null; }
                public int getOffset(long instant) { return 0; }
                public int getStandardOffset(long instant) { return 0; }
                public boolean isFixed() { return true; }
                public long nextTransition(long instant) { return instant; }
                public long previousTransition(long instant) { return instant; }
                public boolean equals(Object object) { return object == this; }
            };
            fail("Expected IllegalArgumentException when constructor argument is null");
        } catch (IllegalArgumentException expected) {
            assertEquals("Id must not be null", expected.getMessage());
        }
    }
}