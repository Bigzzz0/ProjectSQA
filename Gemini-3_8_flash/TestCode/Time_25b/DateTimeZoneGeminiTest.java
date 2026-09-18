package org.joda.time;

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
import org.joda.time.tz.FixedDateTimeZone;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.joda.time.DateTimeZone
 * TARGET DEFECT: Defects4J DateTimeZone cutover / getOffsetFromLocal DST overlap issue (e.g. Moscow Autumn).
 * During daylight saving transitions (DST overlap), ambiguous local times must favor daylight time over
 * standard time. In certain transitions (e.g. Europe/Moscow October cutover), getOffsetFromLocal failed
 * to choose the earlier daylight savings offset.
 *
 * TEST PARTITIONS:
 * - Partition A: Core Functional Logic & State Transitions
 *   * getDefault, setDefault with security and state restoration
 *   * forID, forTimeZone, forOffsetMillis, forOffsetHours, forOffsetHoursMinutes
 *   * Conversion helpers: convertUTCToLocal, convertLocalToUTC (strict & non-strict)
 *   * getMillisKeepLocal with identity, null, and across zones
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   * Arithmetic overflow checks on convertUTCToLocal / convertLocalToUTC (Long.MAX_VALUE / MIN_VALUE)
 *   * Millisecond offset formatting with hours, minutes, seconds, and milliseconds (+/-)
 *   * Upper and lower bounds for hours and minutes offsets (0 to 59 minutes, hour boundaries)
 * - Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *   * test_DateTime_constructor_Moscow_Autumn (Defects4J regression trigger)
 *   * test_getOffsetFromLocal_Moscow_Autumn_overlap (Explicit check of DST overlap favor daylight)
 *   * Additional overlap/gap tests (e.g., America/New_York, Europe/London, America/Los_Angeles)
 * - Partition D: Exception & Defensive Guard Paths
 *   * Invalid provider registration: empty IDs, missing UTC, invalid UTC mapping
 *   * Illegal IDs, unparseable zone strings, null values
 *   * Illegal local instants in strict convertLocalToUTC mode
 * - Partition E: Object Lifecycle & Contract Integrity
 *   * Serialization roundtrip with writeReplace / Stub mechanism
 *   * equals, hashCode, toString contracts
 *   * getAvailableIDs immutable set inspection
 * ----------------------------------------------------------------------------------------------------
 */
public class DateTimeZoneGeminiTest {

    private DateTimeZone originalDefaultZone;
    private Provider originalProvider;
    private NameProvider originalNameProvider;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalDefaultZone = DateTimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefaultZone);
        DateTimeZone.setProvider(originalProvider);
        DateTimeZone.setNameProvider(originalNameProvider);
        Locale.setDefault(originalLocale);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the known defect in getOffsetFromLocal / DateTime constructor during DST overlap.
     * In Europe/Moscow on 2007-10-28 at 02:30:00 (autumn DST overlap), the earlier offset (+04:00)
     * must be chosen over winter offset (+03:00).
     */
    @Test(timeout = 4000)
    public void testDefect_DateTime_constructor_Moscow_Autumn() {
        DateTimeZone zoneMoscow = DateTimeZone.forID("Europe/Moscow");
        DateTime dt = new DateTime(2007, 10, 28, 2, 30, 0, 0, zoneMoscow);
        assertEquals("2007-10-28T02:30:00.000+04:00", dt.toString());
        assertEquals(4 * 3600000, zoneMoscow.getOffset(dt.getMillis()));
    }

    /**
     * Targets getOffsetFromLocal directly on Europe/Moscow at 02:00:00 DST autumn overlap boundary.
     */
    @Test(timeout = 4000)
    public void testDefect_getOffsetFromLocal_Moscow_Autumn_overlap() {
        DateTimeZone zoneMoscow = DateTimeZone.forID("Europe/Moscow");
        DateTime dt = new DateTime(2007, 10, 28, 2, 0, 0, 0, zoneMoscow);
        assertEquals("2007-10-28T02:00:00.000+04:00", dt.toString());
        assertEquals(4 * 3600000, zoneMoscow.getOffset(dt.getMillis()));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetAndSetDefault() {
        DateTimeZone initial = DateTimeZone.getDefault();
        assertNotNull(initial);

        DateTimeZone newYork = DateTimeZone.forID("America/New_York");
        DateTimeZone.setDefault(newYork);
        assertEquals(newYork, DateTimeZone.getDefault());

        DateTimeZone.setDefault(DateTimeZone.UTC);
        assertEquals(DateTimeZone.UTC, DateTimeZone.getDefault());

        DateTimeZone.setDefault(initial);
        assertEquals(initial, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_null() {
        DateTimeZone.setDefault(null);
    }

    @Test(timeout = 4000)
    public void testForID_nullReturnsDefault() {
        DateTimeZone currentDefault = DateTimeZone.getDefault();
        assertSame(currentDefault, DateTimeZone.forID(null));
    }

    @Test(timeout = 4000)
    public void testForID_utcAndPrefixes() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone plusTwo = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", plusTwo.getID());
        assertEquals(2 * 3600000, plusTwo.getOffset(0L));

        DateTimeZone minusFiveThirty = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", minusFiveThirty.getID());
        assertEquals(-(5 * 3600000 + 30 * 60000), minusFiveThirty.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_normal() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));

        DateTimeZone zonePlus1 = DateTimeZone.forOffsetHours(1);
        assertEquals("+01:00", zonePlus1.getID());
        assertEquals(3600000, zonePlus1.getOffset(0L));

        DateTimeZone zoneMinus8 = DateTimeZone.forOffsetHours(-8);
        assertEquals("-08:00", zoneMinus8.getID());
        assertEquals(-8 * 3600000, zoneMinus8.getOffset(0L));

        DateTimeZone zoneMinus2_30 = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals("-02:30", zoneMinus2_30.getID());
        assertEquals(-(2 * 3600000 + 30 * 60000), zoneMinus2_30.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_formattingComponents() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        // Offset with hours, minutes, seconds and milliseconds
        int millis = (1 * 3600000) + (15 * 60000) + (30 * 1000) + 250;
        DateTimeZone zonePos = DateTimeZone.forOffsetMillis(millis);
        assertEquals("+01:15:30.250", zonePos.getID());
        assertEquals(millis, zonePos.getOffset(0L));
        assertTrue(zonePos.isFixed());

        // Negative with seconds and milliseconds
        int negMillis = -((2 * 3600000) + (45 * 60000) + (10 * 1000));
        DateTimeZone zoneNeg = DateTimeZone.forOffsetMillis(negMillis);
        assertEquals("-02:45:10", zoneNeg.getID());
        assertEquals(negMillis, zoneNeg.getOffset(0L));

        // Soft reference caching verification
        DateTimeZone zonePos2 = DateTimeZone.forOffsetMillis(millis);
        assertSame(zonePos, zonePos2);
    }

    @Test(timeout = 4000)
    public void testForTimeZone_variousInputs() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        // Old 3-letter converted IDs
        DateTimeZone pst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", pst.getID());

        DateTimeZone est = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", est.getID());

        DateTimeZone gmt = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT"));
        assertSame(DateTimeZone.UTC, gmt);

        // Custom GMT offset
        TimeZone customGmt = TimeZone.getTimeZone("GMT+05:30");
        DateTimeZone customZone = DateTimeZone.forTimeZone(customGmt);
        assertEquals("+05:30", customZone.getID());

        TimeZone customGmtZero = TimeZone.getTimeZone("GMT+00:00");
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(customGmtZero));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");

        // Identity
        long instant = 123456789L;
        assertEquals(instant, london.getMillisKeepLocal(london, instant));

        // Null target zone delegates to default
        DateTimeZone originalDefault = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(tokyo);
            assertEquals(london.getMillisKeepLocal(tokyo, instant), london.getMillisKeepLocal(null, instant));
        } finally {
            DateTimeZone.setDefault(originalDefault);
        }

        // Keep local time: London local (instant + londonOffset) -> Tokyo UTC (local - tokyoOffset)
        long tokyoKeepLocal = london.getMillisKeepLocal(tokyo, instant);
        long londonLocal = london.convertUTCToLocal(instant);
        long tokyoLocal = tokyo.convertUTCToLocal(tokyoKeepLocal);
        assertEquals(londonLocal, tokyoLocal);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utc = 100000000000L;
        long local = zone.convertUTCToLocal(utc);
        assertEquals(utc + 2 * 3600000, local);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_nonStrictAndOriginal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        long local = 50000000000L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local + 5 * 3600000, utc);

        // With original instant UTC matching offset
        long originalUTC = utc;
        long convertedWithOriginal = zone.convertLocalToUTC(local, false, originalUTC);
        assertEquals(utc, convertedWithOriginal);

        // With non-matching original instant
        long convertedNonMatch = zone.convertLocalToUTC(local, false, 0L);
        assertEquals(utc, convertedNonMatch);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_gap() {
        // America/New_York 2007 spring transition gap: 2007-03-11 02:00 -> 03:00
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        long gapLocalMillis = gapTime.toDateTime(DateTimeZone.UTC).getMillis();

        assertTrue(ny.isLocalDateTimeGap(gapTime));

        // Strict mode throws exception on gap
        try {
            ny.convertLocalToUTC(gapLocalMillis, true);
            fail("Expected IllegalArgumentException on strict conversion in DST gap");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Illegal instant due to time zone offset transition"));
        }

        // Non-strict mode adjusts offset
        long nonStrictUtc = ny.convertLocalToUTC(gapLocalMillis, false);
        assertTrue(nonStrictUtc > 0);
    }

    @Test(timeout = 4000)
    public void testNamesAndNameKeys() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 (GMT)
        long summerInstant = 15778463000L; // Summer 1970 (BST)

        assertEquals("GMT", london.getShortName(winterInstant));
        assertEquals("BST", london.getShortName(summerInstant));
        assertNotNull(london.getName(winterInstant, Locale.ENGLISH));
        assertNotNull(london.getNameKey(winterInstant));

        // Null locale falls back to default locale
        assertEquals(london.getName(winterInstant, Locale.getDefault()), london.getName(winterInstant, null));
        assertEquals(london.getShortName(winterInstant, Locale.getDefault()), london.getShortName(winterInstant, null));

        // Fixed zone naming fallback
        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        assertEquals("+03:00", fixed.getName(0L));
        assertEquals("+03:00", fixed.getShortName(0L));
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // GMT
        long summerInstant = 15778463000L; // BST (+1)
        assertTrue(london.isStandardOffset(winterInstant));
        assertFalse(london.isStandardOffset(summerInstant));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        assertTrue(fixed.isStandardOffset(0L));
        assertTrue(fixed.isStandardOffset(100000000000L));
    }

    @Test(timeout = 4000)
    public void testTransitions() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long instant = 0L;
        long next = london.nextTransition(instant);
        assertTrue(next > instant);
        long prev = london.previousTransition(next);
        assertEquals(instant, prev);

        DateTimeZone fixed = DateTimeZone.UTC;
        assertEquals(1000L, fixed.nextTransition(1000L));
        assertEquals(1000L, fixed.previousTransition(1000L));
    }

    @Test(timeout = 4000)
    public void testReadableInstantOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals(5 * 3600000, zone.getOffset((ReadableInstant) null));

        Instant inst = new Instant(1000L);
        assertEquals(5 * 3600000, zone.getOffset(inst));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertUTCToLocal_overflowPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        zone.convertUTCToLocal(Long.MAX_VALUE - 100);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertUTCToLocal_overflowNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-1);
        zone.convertUTCToLocal(Long.MIN_VALUE + 100);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertLocalToUTC_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-1);
        zone.convertLocalToUTC(Long.MAX_VALUE - 100, false);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertLocalToUTC_overflowUnderflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        zone.convertLocalToUTC(Long.MIN_VALUE + 100, false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_minutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_minutesExceed59() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_hoursOutOfRange() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test(timeout = 4000)
    public void testPrintOffsetBoundaries() {
        // Fixed zone with exactly millisecond precision
        DateTimeZone z1 = DateTimeZone.forOffsetMillis(1);
        assertEquals("+00:00:00.001", z1.getID());

        DateTimeZone z2 = DateTimeZone.forOffsetMillis(-1);
        assertEquals("-00:00:00.001", z2.getID());

        DateTimeZone z3 = DateTimeZone.forOffsetMillis(59999);
        assertEquals("+00:00:59.999", z3.getID());

        DateTimeZone z4 = DateTimeZone.forOffsetMillis(60000);
        assertEquals("+00:01", z4.getID());
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_fixedZone() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(4);
        LocalDateTime ldt = new LocalDateTime(2020, 1, 1, 12, 0);
        assertFalse(fixed.isLocalDateTimeGap(ldt));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_invalidString() {
        DateTimeZone.forID("NonExistentZoneId12345");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_emptyString() {
        DateTimeZone.forID("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_unrecognized() {
        TimeZone tz = new TimeZone() {
            private static final long serialVersionUID = 1L;
            @Override
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
                return 0;
            }
            @Override
            public void setRawOffset(int offsetMillis) {}
            @Override
            public int getRawOffset() {
                return 0;
            }
            @Override
            public boolean useDaylightTime() {
                return false;
            }
            @Override
            public boolean inDaylightTime(java.util.Date date) {
                return false;
            }
            @Override
            public String getID() {
                return "UnknownTZ";
            }
            @Override
            public String getDisplayName() {
                return "UnknownTZ";
            }
        };
        DateTimeZone.forTimeZone(tz);
    }

    @Test(timeout = 4000)
    public void testSetProvider_nullRestoresDefault() {
        DateTimeZone.setProvider(null);
        assertNotNull(DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_emptyIds() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() { return Collections.emptySet(); }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_noUTC() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("America/New_York");
                return set;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_invalidUTC() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                if ("UTC".equals(id)) {
                    return DateTimeZone.forOffsetHours(1);
                }
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("UTC");
                return set;
            }
        });
    }

    @Test(timeout = 4000)
    public void testSetNameProvider_validAndNull() {
        NameProvider defaultNP = DateTimeZone.getNameProvider();
        assertNotNull(defaultNP);

        DateTimeZone.setNameProvider(new DefaultNameProvider());
        assertNotNull(DateTimeZone.getNameProvider());

        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("America/New_York"));
        assertTrue(ids.contains("Europe/London"));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        DateTimeZone z1 = DateTimeZone.forOffsetHours(5);
        DateTimeZone z2 = DateTimeZone.forOffsetHours(5);
        DateTimeZone z3 = DateTimeZone.forOffsetHours(6);

        assertEquals(z1, z2);
        assertNotEquals(z1, z3);
        assertEquals(z1.hashCode(), z2.hashCode());

        assertFalse(z1.equals(null));
        assertFalse(z1.equals("NotADateTimeZone"));

        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test(timeout = 4000)
    public void testToStringAndToTimeZone() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", london.toString());

        TimeZone tz = london.toTimeZone();
        assertEquals("Europe/London", tz.getID());

        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertEquals("+02:00", fixed.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        DateTimeZone original = DateTimeZone.forID("Europe/Paris");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame(original, deserialized);
    }

    @Test(timeout = 4000)
    public void testCustomSubclassConstructorProtection() {
        DateTimeZone custom = new DateTimeZone("Custom/Zone") {
            private static final long serialVersionUID = 1L;
            @Override
            public String getNameKey(long instant) { return "CZ"; }
            @Override
            public int getOffset(long instant) { return 0; }
            @Override
            public int getStandardOffset(long instant) { return 0; }
            @Override
            public boolean isFixed() { return true; }
            @Override
            public long nextTransition(long instant) { return instant; }
            @Override
            public long previousTransition(long instant) { return instant; }
            @Override
            public boolean equals(Object object) { return object instanceof DateTimeZone && getID().equals(((DateTimeZone) object).getID()); }
        };

        assertEquals("Custom/Zone", custom.getID());
        assertEquals("Custom/Zone", custom.getName(0L));
        assertEquals("Custom/Zone", custom.getShortName(0L));
    }
}