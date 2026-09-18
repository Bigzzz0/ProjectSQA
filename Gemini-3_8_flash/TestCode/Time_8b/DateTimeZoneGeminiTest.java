/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.joda.time.DateTimeZone
 * Target Defect: testForOffsetHoursMinutes_int_int fails when negative minutes are supplied
 *                (e.g., forOffsetHoursMinutes(0, -15) or (-2, -15)) due to guard check
 *                `if (minutesOffset < 0 || minutesOffset > 59)`. The specification supports
 *                negative minutes in the range -59 to 59.
 *
 * Decision / Branch Coverage Targets:
 * - getDefault() / setDefault():
 *     - null check, security check, synchronized lazy-initialization, fallbacks (user.timezone, JDK TimeZone, UTC).
 * - forID(String id):
 *     - null id (delegates to getDefault()).
 *     - "UTC" literal check.
 *     - provider.getZone(id) hit/miss.
 *     - startsWith("+") / startsWith("-") offset parsing (offset == 0 -> UTC, offset != 0 -> fixedOffsetZone).
 *     - unrecognised ID throws IllegalArgumentException.
 * - forOffsetHours(int hoursOffset) / forOffsetHoursMinutes(int hoursOffset, int minutesOffset):
 *     - (0, 0) -> UTC.
 *     - hours out of range (< -23 or > 23).
 *     - minutes out of range (< -59 or > 59) and positive/negative combinations (+/+, +/0, 0/+, 0/-, -/+, -/0, -/-).
 *     - arithmetic overflow checks on safe multiplication.
 * - forOffsetMillis(int millisOffset):
 *     - boundary limits +/- 86399999 (MAX_MILLIS), out-of-range bounds throwing IllegalArgumentException.
 *     - fixedOffsetZone caching (soft reference retrieval and eviction handling).
 * - forTimeZone(TimeZone zone):
 *     - null zone -> getDefault().
 *     - "UTC" ID -> UTC.
 *     - old conversion IDs (e.g., "GMT", "PST", "EST", "ECT", etc.).
 *     - "GMT+" / "GMT-" prefix stripping and parsing (offset == 0 vs offset != 0).
 *     - unrecognised ID handling.
 * - Providers and NameProviders:
 *     - setProvider / setNameProvider with null (resets to default) and valid/invalid custom instances.
 *     - Provider without UTC or invalid UTC zone throwing IllegalArgumentException.
 * - getOffset / getOffsetFromLocal / isStandardOffset / getStandardOffset:
 *     - DST gap and overlap conditions.
 *     - Western vs Eastern hemisphere offset differences.
 * - convertUTCToLocal / convertLocalToUTC:
 *     - strict vs non-strict conversions.
 *     - gap detection (IllegalInstantException in strict mode).
 *     - arithmetic overflow checks on long subtraction/addition.
 *     - 3-argument convertLocalToUTC(instantLocal, strict, originalInstantUTC).
 * - adjustOffset:
 *     - before/after DST transitions, earlierOrLater flags.
 * - Object lifecycle & contracts:
 *     - equals, hashCode, toString, writeReplace/readResolve serialization via Stub.
 */

package org.joda.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Targets the documented defect where negative minutes are rejected in forOffsetHoursMinutes
     * despite the specification and Javadoc allowing minute offsets in the range -59 to 59.
     */
    @Test(timeout = 4000)
    public void testDefect_forOffsetHoursMinutes_negativeMinutesWithZeroHours() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, -15);
        assertEquals("-00:15", zone.getID());
        assertEquals(-15 * 60 * 1000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testDefect_forOffsetHoursMinutes_negativeMinutesWithNegativeHours() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, -15);
        assertEquals("-02:15", zone.getID());
        assertEquals(-(2 * 60 + 15) * 60 * 1000, zone.getOffset(0L));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetDefault_and_SetDefault() {
        assertNotNull(DateTimeZone.getDefault());

        DateTimeZone utc = DateTimeZone.UTC;
        DateTimeZone.setDefault(utc);
        assertSame(DateTimeZone.UTC, DateTimeZone.getDefault());

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        assertEquals(paris, DateTimeZone.getDefault());
    }

    @Test(timeout = 4000)
    public void testForID_validIDs() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forID(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        assertNotNull(london);
        assertEquals("Europe/London", london.getID());

        DateTimeZone offsetPlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", offsetPlus.getID());
        assertEquals(2 * 3600 * 1000, offsetPlus.getOffset(0L));

        DateTimeZone offsetMinus = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", offsetMinus.getID());
        assertEquals(-(5 * 3600 + 30 * 60) * 1000, offsetMinus.getOffset(0L));

        DateTimeZone offsetZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, offsetZero);

        DateTimeZone offsetZeroMinus = DateTimeZone.forID("-00:00");
        assertSame(DateTimeZone.UTC, offsetZeroMinus);
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_validCombinations() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone1 = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone1.getID());
        assertEquals(5 * 3600 * 1000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHours(-8);
        assertEquals("-08:00", zone2.getID());
        assertEquals(-8 * 3600 * 1000, zone2.getOffset(0L));

        DateTimeZone zone3 = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone3.getID());
        assertEquals((5 * 60 + 30) * 60 * 1000, zone3.getOffset(0L));

        DateTimeZone zone4 = DateTimeZone.forOffsetHoursMinutes(-5, 30);
        assertEquals("-05:30", zone4.getID());
        assertEquals(-(5 * 60 + 30) * 60 * 1000, zone4.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_andCaching() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zoneA = DateTimeZone.forOffsetMillis(12345);
        DateTimeZone zoneB = DateTimeZone.forOffsetMillis(12345);
        assertSame(zoneA, zoneB);
        assertEquals(12345, zoneA.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals("-01:00", zoneNeg.getID());
        assertEquals(-3600000, zoneNeg.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_validMappings() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        DateTimeZone gmt = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT"));
        assertSame(DateTimeZone.UTC, gmt);

        DateTimeZone pst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", pst.getID());

        DateTimeZone est = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", est.getID());

        DateTimeZone customGmtPlus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+04:00"));
        assertEquals("+04:00", customGmtPlus.getID());
        assertEquals(4 * 3600 * 1000, customGmtPlus.getOffset(0L));

        DateTimeZone customGmtZero = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        assertSame(DateTimeZone.UTC, customGmtZero);
    }

    @Test(timeout = 4000)
    public void testAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("America/New_York"));
        assertTrue(ids.contains("Europe/London"));
    }

    @Test(timeout = 4000)
    public void testNamesAndShortNames() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 UTC

        String shortName = zone.getShortName(winterInstant);
        assertNotNull(shortName);

        String shortNameLocale = zone.getShortName(winterInstant, Locale.UK);
        assertNotNull(shortNameLocale);

        String longName = zone.getName(winterInstant);
        assertNotNull(longName);

        String longNameLocale = zone.getName(winterInstant, Locale.UK);
        assertNotNull(longNameLocale);

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        assertEquals("+03:00", fixed.getName(winterInstant));
        assertEquals("+03:00", fixed.getShortName(winterInstant));
    }

    @Test(timeout = 4000)
    public void testReadableInstantOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Instant winterInstant = new Instant(0L);
        assertEquals(zone.getOffset(0L), zone.getOffset(winterInstant));
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset((ReadableInstant) null));
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 -> Standard (GMT)
        long summerInstant = 10000000000L; // Summer DST in 1970
        assertTrue(zone.isStandardOffset(winterInstant));
        assertFalse(zone.isStandardOffset(summerInstant));
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_andLocalToUTC() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long utc = 0L;
        long local = zone.convertUTCToLocal(utc);
        assertEquals(3600000L, local);

        long backToUTC = zone.convertLocalToUTC(local, false);
        assertEquals(utc, backToUTC);

        long backToUTCStrict = zone.convertLocalToUTC(local, true);
        assertEquals(utc, backToUTCStrict);

        long backToUTCTriple = zone.convertLocalToUTC(local, false, utc);
        assertEquals(utc, backToUTCTriple);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal() {
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris"); // +1
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London"); // +0

        long instant = 0L;
        long converted = zoneParis.getMillisKeepLocal(zoneLondon, instant);
        assertEquals(3600000L, converted);

        long sameZone = zoneParis.getMillisKeepLocal(zoneParis, instant);
        assertEquals(instant, sameZone);

        DateTimeZone.setDefault(zoneLondon);
        long toNullDefault = zoneParis.getMillisKeepLocal(null, instant);
        assertEquals(converted, toNullDefault);
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-03-11 02:30:00 gap in New York
        LocalDateTime gapDateTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapDateTime));

        LocalDateTime normalDateTime = new LocalDateTime(2007, 3, 11, 4, 30, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(normalDateTime));

        DateTimeZone fixed = DateTimeZone.UTC;
        assertFalse(fixed.isLocalDateTimeGap(gapDateTime));
    }

    @Test(timeout = 4000)
    public void testAdjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long standardTime = 0L;
        assertEquals(standardTime, zone.adjustOffset(standardTime, true));
        assertEquals(standardTime, zone.adjustOffset(standardTime, false));

        // Fixed zones do not adjust
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertEquals(12345678L, fixed.adjustOffset(12345678L, true));
        assertEquals(12345678L, fixed.adjustOffset(12345678L, false));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBoundaryOffsets() {
        DateTimeZone maxPositive = DateTimeZone.forOffsetMillis((86400 * 1000) - 1);
        assertEquals("+23:59:59.999", maxPositive.getID());

        DateTimeZone maxNegative = DateTimeZone.forOffsetMillis(-((86400 * 1000) - 1));
        assertEquals("-23:59:59.999", maxNegative.getID());

        DateTimeZone maxHours = DateTimeZone.forOffsetHours(23);
        assertEquals("+23:00", maxHours.getID());

        DateTimeZone minHours = DateTimeZone.forOffsetHours(-23);
        assertEquals("-23:00", minHours.getID());

        DateTimeZone maxMinutes = DateTimeZone.forOffsetHoursMinutes(0, 59);
        assertEquals("+00:59", maxMinutes.getID());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMillisOffset_ExceedsPositiveMax() {
        DateTimeZone.forOffsetMillis(86400 * 1000);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMillisOffset_ExceedsNegativeMax() {
        DateTimeZone.forOffsetMillis(-86400 * 1000);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testHoursOffset_TooPositive() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testHoursOffset_TooNegative() {
        DateTimeZone.forOffsetHours(-24);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinutesOffset_TooPositive() {
        DateTimeZone.forOffsetHoursMinutes(0, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinutesOffset_TooNegative() {
        DateTimeZone.forOffsetHoursMinutes(0, -60);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_null() {
        DateTimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_invalidString() {
        DateTimeZone.forID("Invalid/NonExistent/Zone");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_emptyString() {
        DateTimeZone.forID("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_unknownID() {
        TimeZone tz = new TimeZone() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getID() { return "UnknownTimeZoneID"; }
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
        };
        DateTimeZone.forTimeZone(tz);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertUTCToLocal_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        zone.convertUTCToLocal(Long.MAX_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertUTCToLocal_underflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-1);
        zone.convertUTCToLocal(Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertLocalToUTC_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-1);
        zone.convertLocalToUTC(Long.MAX_VALUE, false);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertLocalToUTC_underflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        zone.convertLocalToUTC(Long.MIN_VALUE, false);
    }

    @Test(expected = IllegalInstantException.class, timeout = 4000)
    public void testConvertLocalToUTC_strictGapThrows() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // In New York, 2007-03-11 02:30:00 does not exist
        long gapLocalMillis = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        zone.convertLocalToUTC(gapLocalMillis, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_emptyProviderThrows() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() { return Collections.emptySet(); }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_noUtcThrows() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() {
                Set<String> s = new HashSet<String>();
                s.add("America/New_York");
                return s;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_invalidUtcInstanceThrows() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                if ("UTC".equals(id)) {
                    return DateTimeZone.forOffsetHours(1); // Mismatched UTC
                }
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> s = new HashSet<String>();
                s.add("UTC");
                return s;
            }
        });
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testProviderResetToDefault() {
        DateTimeZone.setProvider(null);
        assertNotNull(DateTimeZone.getProvider());
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }

    @Test(timeout = 4000)
    public void testNameProviderResetToDefault() {
        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
        assertTrue(DateTimeZone.getNameProvider() instanceof DefaultNameProvider);
    }

    @Test(timeout = 4000)
    public void testCustomValidProvider() {
        Provider customProvider = new UTCProvider();
        DateTimeZone.setProvider(customProvider);
        assertSame(customProvider, DateTimeZone.getProvider());
        assertEquals(1, DateTimeZone.getAvailableIDs().size());
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }

    @Test(timeout = 4000)
    public void testCustomValidNameProvider() {
        NameProvider customNameProvider = new DefaultNameProvider();
        DateTimeZone.setNameProvider(customNameProvider);
        assertSame(customNameProvider, DateTimeZone.getNameProvider());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone3 = DateTimeZone.forID("America/New_York");

        assertEquals(zone1, zone2);
        assertNotEquals(zone1, zone3);
        assertNotEquals(zone1, null);
        assertNotEquals(zone1, "Europe/London");

        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals("Europe/London", zone1.toString());
        assertEquals(zone1.getID(), zone1.toString());
    }

    @Test(timeout = 4000)
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        TimeZone tz = zone.toTimeZone();
        assertNotNull(tz);
        assertEquals("America/Chicago", tz.getID());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        DateTimeZone original = DateTimeZone.forID("America/Los_Angeles");

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
    public void testFixedOffsetZoneSerializationRoundTrip() throws Exception {
        DateTimeZone original = DateTimeZone.forOffsetHoursMinutes(3, 45);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
    }
}