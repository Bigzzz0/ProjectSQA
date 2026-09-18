package org.joda.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.joda.time.DateTimeZone
 *
 * 1. DEFECT UNDER TEST (Defects4J Time-18):
 *    - forOffsetHoursMinutes(int, int) and forOffsetHours(int) fail to validate the
 *      documented range of hoursOffset: from -23 to +23.
 *    - Boundary conditions: hoursOffset = 24, hoursOffset = -24, hoursOffset = 100,
 *      hoursOffset = -100 must throw IllegalArgumentException.
 *
 * 2. FACTORY METHODS & PARSING BRANCHES:
 *    - forID(null) -> returns default zone.
 *    - forID("UTC") -> returns DateTimeZone.UTC.
 *    - forID("+00:00") and forID("-00:00") -> offset == 0 -> returns DateTimeZone.UTC.
 *    - forID("+01:00"), forID("-05:00"), forID("+02:30:15.500") -> fixed offset zone formatting.
 *    - forID("Invalid_Zone_ID_XYZ") -> throws IllegalArgumentException.
 *    - forTimeZone(null) -> returns default zone.
 *    - forTimeZone("UTC"), forTimeZone("GMT") -> aliases and conversion map.
 *    - forTimeZone with "GMT+02:00", "GMT-08:00", "GMT+00:00".
 *    - forTimeZone with unrecognized ID -> throws IllegalArgumentException.
 *
 * 3. LOCAL / UTC CONVERSIONS & DST HANDLING:
 *    - convertUTCToLocal() / convertLocalToUTC():
 *      - Arithmetic overflow on Long.MAX_VALUE / Long.MIN_VALUE boundaries.
 *      - DST gap handling (strict = true throws IllegalInstantException; strict = false adjusts).
 *      - DST overlap handling and adjustOffset() earlier/later paths.
 *    - isLocalDateTimeGap() for fixed zone (always false) and gap instant.
 *    - getMillisKeepLocal() when newZone == this, newZone == null, and between different offsets.
 *
 * 4. PROVIDER & SYSTEM STATE INTEGRITY:
 *    - setProvider() validation (null defaults, empty IDs, missing UTC, invalid UTC zone).
 *    - setNameProvider() validation and getShortName / getName fallback branches.
 *    - Serialization round-trip using writeReplace / Stub.
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
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_HoursTooLargePositive() {
        // Range must be -23 to +23 according to javadoc specification
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_HoursTooLargeNegative() {
        // Range must be -23 to +23 according to javadoc specification
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHours_HoursTooLargePositive() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHours_HoursTooLargeNegative() {
        DateTimeZone.forOffsetHours(-24);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & FACTORY PATHWAYS
    // =========================================================================

    @Test(timeout = 4000)
    public void testForID_BasicAndDefault() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forID(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * 3600 * 1000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:00");
        assertEquals("-05:00", zoneMinus.getID());
        assertEquals(-5 * 3600 * 1000, zoneMinus.getOffset(0L));

        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", zoneLondon.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_ValidBoundaries() {
        DateTimeZone zoneMax = DateTimeZone.forOffsetHours(23);
        assertEquals("+23:00", zoneMax.getID());
        assertEquals(23 * 3600 * 1000, zoneMax.getOffset(0L));

        DateTimeZone zoneMin = DateTimeZone.forOffsetHours(-23);
        assertEquals("-23:00", zoneMin.getID());
        assertEquals(-23 * 3600 * 1000, zoneMin.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forOffsetHours(0);
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_ValidCombinations() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone1 = DateTimeZone.forOffsetHoursMinutes(2, 30);
        assertEquals("+02:30", zone1.getID());
        assertEquals((2 * 60 + 30) * 60 * 1000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals("-02:30", zone2.getID());
        assertEquals(-(2 * 60 + 30) * 60 * 1000, zone2.getOffset(0L));

        DateTimeZone zoneMaxMin = DateTimeZone.forOffsetHoursMinutes(23, 59);
        assertEquals("+23:59", zoneMaxMin.getID());

        DateTimeZone zoneMinMin = DateTimeZone.forOffsetHoursMinutes(-23, 59);
        assertEquals("-23:59", zoneMinMin.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_FormattingSubUnits() {
        DateTimeZone zoneZero = DateTimeZone.forOffsetMillis(0);
        assertSame(DateTimeZone.UTC, zoneZero);

        // Millis including seconds and fractional seconds
        int millis = (1 * 3600 + 23 * 60 + 45) * 1000 + 678;
        DateTimeZone zoneWithFraction = DateTimeZone.forOffsetMillis(millis);
        assertEquals("+01:23:45.678", zoneWithFraction.getID());
        assertEquals(millis, zoneWithFraction.getOffset(0L));

        DateTimeZone zoneNegWithFraction = DateTimeZone.forOffsetMillis(-millis);
        assertEquals("-01:23:45.678", zoneNegWithFraction.getID());
        assertEquals(-millis, zoneNegWithFraction.getOffset(0L));

        // Millis with whole seconds but no millis
        int millisSeconds = (1 * 3600 + 20 * 60 + 30) * 1000;
        DateTimeZone zoneSeconds = DateTimeZone.forOffsetMillis(millisSeconds);
        assertEquals("+01:20:30", zoneSeconds.getID());

        DateTimeZone cached = DateTimeZone.forOffsetMillis(millis);
        assertSame(zoneWithFraction, cached);
    }

    @Test(timeout = 4000)
    public void testForTimeZone_AliasesAndFormats() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));

        // Old 3-letter alias conversions
        assertEquals("America/New_York", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")).getID());
        assertEquals("America/Los_Angeles", DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")).getID());
        assertEquals("Europe/Paris", DateTimeZone.forTimeZone(TimeZone.getTimeZone("CET")).getID());
        assertEquals("Asia/Tokyo", DateTimeZone.forTimeZone(TimeZone.getTimeZone("JST")).getID());

        // Custom JDK GMT offsets
        DateTimeZone zoneGmtPlus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+04:00"));
        assertEquals("+04:00", zoneGmtPlus.getID());

        DateTimeZone zoneGmtMinus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-08:00"));
        assertEquals("-08:00", zoneGmtMinus.getID());

        DateTimeZone zoneGmtZero = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, zoneGmtZero);
    }

    @Test(timeout = 4000)
    public void testSetAndGetDefault() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        assertSame(paris, DateTimeZone.getDefault());

        DateTimeZone.setDefault(DateTimeZone.UTC);
        assertSame(DateTimeZone.UTC, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_NullThrows() {
        DateTimeZone.setDefault(null);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & EXCEPTION GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_InvalidIDThrows() {
        DateTimeZone.forID("NonExistent_Zone_123");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_UnrecognizedThrows() {
        DateTimeZone.forTimeZone(new TimeZone() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getID() { return "UNKNOWN_TZ_ID"; }
            @Override
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) { return 0; }
            @Override
            public void setRawOffset(int offsetMillis) {}
            @Override
            public int getRawOffset() { return 0; }
            @Override
            public boolean useDaylightTime() { return false; }
            @Override
            public boolean inDaylightTime(java.util.Date date) { return false; }
        });
    }

    // =========================================================================
    // PARTITION D: LOCAL TIME / UTC CONVERSION & DST SEMANTICS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_BasicAndOverflow() {
        DateTimeZone zonePlus2 = DateTimeZone.forOffsetHours(2);
        long utc = 10000L;
        long expectedLocal = 10000L + (2 * 3600 * 1000);
        assertEquals(expectedLocal, zonePlus2.convertUTCToLocal(utc));

        // Positive overflow check
        try {
            zonePlus2.convertUTCToLocal(Long.MAX_VALUE - 100);
            fail("Expected ArithmeticException on positive overflow");
        } catch (ArithmeticException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }

        // Negative overflow check
        DateTimeZone zoneMinus2 = DateTimeZone.forOffsetHours(-2);
        try {
            zoneMinus2.convertUTCToLocal(Long.MIN_VALUE + 100);
            fail("Expected ArithmeticException on negative overflow");
        } catch (ArithmeticException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_BasicAndOverflow() {
        DateTimeZone zonePlus2 = DateTimeZone.forOffsetHours(2);
        long local = 20000000L;
        long expectedUTC = 20000000L - (2 * 3600 * 1000);
        assertEquals(expectedUTC, zonePlus2.convertLocalToUTC(local, false));

        // Subtraction overflow check: Long.MIN_VALUE - (+offset) -> underflows
        try {
            zonePlus2.convertLocalToUTC(Long.MIN_VALUE + 1000, false);
            fail("Expected ArithmeticException on underflow");
        } catch (ArithmeticException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_DstGapStrictVsLenient() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Spring forward gap in New York: 2007-03-11 02:00:00 -> 03:00:00 EST to EDT
        // 2007-03-11 02:30:00 does not exist locally.
        DateTime gapLocalTime = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC);
        long gapLocalMillis = gapLocalTime.getMillis();

        // Strict mode must reject non-existent gap time
        try {
            ny.convertLocalToUTC(gapLocalMillis, true);
            fail("Strict conversion should throw IllegalInstantException for gap");
        } catch (IllegalInstantException expected) {
            assertEquals("America/New_York", expected.getZoneId());
        }

        // Lenient mode converts without exception
        long convertedLenient = ny.convertLocalToUTC(gapLocalMillis, false);
        assertTrue(convertedLenient > 0);

        // With original instant parameter
        long convertedWithOriginal = ny.convertLocalToUTC(gapLocalMillis, false, gapLocalMillis);
        assertTrue(convertedWithOriginal > 0);
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        LocalDateTime gapDateTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        assertTrue(ny.isLocalDateTimeGap(gapDateTime));

        LocalDateTime validDateTime = new LocalDateTime(2007, 3, 11, 1, 30, 0, 0);
        assertFalse(ny.isLocalDateTimeGap(validDateTime));

        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapDateTime));
    }

    @Test(timeout = 4000)
    public void testAdjustOffset_Overlap() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Autumn overlap transition in New York: 2007-11-04 01:00 to 02:00 repeats
        // 01:30 is ambiguous.
        DateTime overlapDateTime = new DateTime(2007, 11, 4, 1, 30, 0, 0, DateTimeZone.forOffsetHours(-4));
        long overlapInstant = overlapDateTime.getMillis();

        long adjustedEarlier = ny.adjustOffset(overlapInstant, false);
        long adjustedLater = ny.adjustOffset(overlapInstant, true);

        assertTrue("Earlier offset should be <= later offset instant", adjustedEarlier <= adjustedLater);

        // Non-overlap instant returned unchanged
        long normalInstant = 0L;
        assertEquals(normalInstant, ny.adjustOffset(normalInstant, false));
        assertEquals(normalInstant, ny.adjustOffset(normalInstant, true));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long standardWinter = new DateTime(2012, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        assertEquals(0, london.getOffsetFromLocal(standardWinter));

        long summer = new DateTime(2012, 7, 1, 12, 0, DateTimeZone.UTC).getMillis();
        assertEquals(3600000, london.getOffsetFromLocal(summer));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");

        long instant = 10000000L;
        // Keep local in same zone
        assertEquals(instant, zoneLondon.getMillisKeepLocal(zoneLondon, instant));
        // Keep local with null target defaults to default zone
        long keptWithDefault = zoneLondon.getMillisKeepLocal(null, instant);
        assertEquals(zoneLondon.getMillisKeepLocal(DateTimeZone.getDefault(), instant), keptWithDefault);

        // London (UTC+0 in winter) to Paris (UTC+1 in winter)
        long winterInstant = new DateTime(2012, 1, 1, 12, 0, 0, DateTimeZone.UTC).getMillis();
        long inParis = zoneLondon.getMillisKeepLocal(zoneParis, winterInstant);
        assertEquals(winterInstant - 3600000, inParis);
    }

    // =========================================================================
    // PARTITION E: NAMES, PROVIDERS & LIFECYCLE
    // =========================================================================

    @Test(timeout = 4000)
    public void testNamesAndShortNames() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long summerInstant = new DateTime(2012, 7, 1, 12, 0, DateTimeZone.UTC).getMillis();

        assertNotNull(zone.getName(summerInstant));
        assertNotNull(zone.getName(summerInstant, Locale.UK));
        assertNotNull(zone.getShortName(summerInstant));
        assertNotNull(zone.getShortName(summerInstant, Locale.UK));

        // Fixed offset zone name fallback to offset string
        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        assertEquals("+03:00", fixed.getName(0L));
        assertEquals("+03:00", fixed.getShortName(0L));
    }

    @Test(timeout = 4000)
    public void testOffsetByReadableInstant() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals(5 * 3600 * 1000, zone.getOffset((ReadableInstant) null));

        Instant inst = new Instant(50000L);
        assertEquals(5 * 3600 * 1000, zone.getOffset(inst));
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winter = new DateTime(2012, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summer = new DateTime(2012, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        assertTrue(london.isStandardOffset(winter));
        assertFalse(london.isStandardOffset(summer));
    }

    @Test(timeout = 4000)
    public void testProviderValidation() {
        assertNotNull(DateTimeZone.getAvailableIDs());
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));

        // Valid provider reset
        DateTimeZone.setProvider(null); // Defaults to default provider
        assertNotNull(DateTimeZone.getProvider());

        // Provider returning empty IDs must fail
        try {
            DateTimeZone.setProvider(new Provider() {
                public DateTimeZone getZone(String id) { return null; }
                public Set<String> getAvailableIDs() { return Collections.emptySet(); }
            });
            fail("Expected IllegalArgumentException for empty IDs");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("available ids"));
        }

        // Provider missing UTC must fail
        try {
            DateTimeZone.setProvider(new Provider() {
                public DateTimeZone getZone(String id) { return null; }
                public Set<String> getAvailableIDs() {
                    Set<String> set = new HashSet<String>();
                    set.add("America/New_York");
                    return set;
                }
            });
            fail("Expected IllegalArgumentException when UTC not in available IDs");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("UTC"));
        }

        // Provider returning invalid UTC zone must fail
        try {
            DateTimeZone.setProvider(new Provider() {
                public DateTimeZone getZone(String id) {
                    if ("UTC".equals(id)) {
                        return DateTimeZone.forOffsetHours(1); // Mismatched UTC
                    }
                    return null;
                }
                public Set<String> getAvailableIDs() {
                    Set<String> set = new HashSet<String>();
                    set.add("UTC");
                    return set;
                }
            });
            fail("Expected IllegalArgumentException for invalid UTC zone");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Invalid UTC"));
        }
    }

    @Test(timeout = 4000)
    public void testNameProviderValidation() {
        assertNotNull(DateTimeZone.getNameProvider());
        DateTimeZone.setNameProvider(null);
        assertTrue(DateTimeZone.getNameProvider() instanceof DefaultNameProvider);

        NameProvider customProvider = new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) { return "customShort"; }
            public String getName(Locale locale, String id, String nameKey) { return "customLong"; }
        };
        DateTimeZone.setNameProvider(customProvider);
        assertSame(customProvider, DateTimeZone.getNameProvider());
        assertEquals("customShort", DateTimeZone.forID("Europe/London").getShortName(0L));
        assertEquals("customLong", DateTimeZone.forID("Europe/London").getName(0L));
    }

    @Test(timeout = 4000)
    public void testObjectContractAndSerialization() throws Exception {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone3 = DateTimeZone.forID("America/New_York");

        assertEquals(zone1, zone2);
        assertNotEquals(zone1, zone3);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals("Europe/London", zone1.toString());

        // toTimeZone conversion
        TimeZone tz = zone1.toTimeZone();
        assertEquals("Europe/London", tz.getID());

        // Serialization round trip via Stub
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone1);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame(zone1, deserialized);
    }

    @Test(timeout = 4000)
    public void testProtectedConstructorAndInheritance() {
        class MockDateTimeZone extends DateTimeZone {
            private static final long serialVersionUID = 1L;
            MockDateTimeZone(String id) { super(id); }
            public String getNameKey(long instant) { return "MOCK"; }
            public int getOffset(long instant) { return 0; }
            public int getStandardOffset(long instant) { return 0; }
            public boolean isFixed() { return true; }
            public long nextTransition(long instant) { return instant; }
            public long previousTransition(long instant) { return instant; }
            public boolean equals(Object object) { return object instanceof MockDateTimeZone; }
        }

        MockDateTimeZone mockZone = new MockDateTimeZone("Custom/Mock");
        assertEquals("Custom/Mock", mockZone.getID());

        try {
            new MockDateTimeZone(null);
            fail("Expected IllegalArgumentException for null ID");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("must not be null"));
        }
    }
}