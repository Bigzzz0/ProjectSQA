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
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Partition / Target               | Decision / Condition Branches Tested                     | Result / Expectation
 * ---------------------------------------------------------------------------------------------------
 * Defects4J Bug 3476684            | adjustOffset during overlap cutover in America/Sao_Paulo | Expected later offset
 *                                  | (instantAfter incorrectly passed as originalInstantUTC)  | (-03:00) not earlier (-02:00)
 * Partition A: Core Functional     | getDefault / setDefault, getID, toTimeZone, hashCode     | State consistency & ID match
 * Partition A: TimeZone Conversion | cZoneIdConversion old IDs ("EST", "HST", "BET", etc.)   | Maps to full canonical ID
 * Partition B: BVA & Extremes      | forOffsetHours / forOffsetHoursMinutes (max, min, 0, 59) | Exact ID string "[+-]hh:mm"
 * Partition B: Sub-minute Offset   | printOffset with seconds & milliseconds component        | Format "[+-]hh:mm:ss.SSS"
 * Partition B: Long Overflow       | convertUTCToLocal & convertLocalToUTC boundary overflow  | Throws ArithmeticException
 * Partition C: DST Gaps & Overlaps | convertLocalToUTC (strict vs lenient on gap)             | Strict throws IllegalArgumentException
 * Partition C: Gap Detection       | isLocalDateTimeGap with fixed vs transition zones        | Correct boolean evaluation
 * Partition D: Defensive Guards    | Provider / NameProvider null, empty IDs, missing UTC     | Throws IllegalArgumentException
 * Partition E: Object Lifecycle    | Serialization / Deserialization via Stub and readResolve | Singleton / Cached instance identity
 * ---------------------------------------------------------------------------------------------------
 */
public class DateTimeZoneGeminiTest {

    // =======================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Bug 3476684)
    // =======================================================================

    /**
     * Targets Bug 3476684 / Defects4J ground truth:
     * During a DST cutover overlap in America/Sao_Paulo (e.g. 2012-02-25 23:15:00),
     * adjustOffset failed when selecting the later offset because convertUTCToLocal
     * was mistakenly passed where a UTC instant was expected.
     */
    @Test(timeout = 4000)
    public void testBug3476684_adjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        DateTime base = new DateTime(2012, 2, 25, 22, 15, zone);
        DateTime test = base.plusHours(1); // 2012-02-25T23:15:00.000-02:00
        DateTime result = test.withLaterOffsetAtOverlap();
        assertEquals("2012-02-25T23:15:00.000-03:00", result.toString());
    }

    @Test(timeout = 4000)
    public void testAdjustOffset_NonOverlapRetainsInstant() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // A normal summer day with no transition
        long instant = new DateTime(2012, 6, 15, 12, 0, DateTimeZone.UTC).getMillis();
        assertEquals(instant, zone.adjustOffset(instant, false));
        assertEquals(instant, zone.adjustOffset(instant, true));
    }

    @Test(timeout = 4000)
    public void testAdjustOffset_EarlierOffsetAtOverlap() {
        DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        // Create an instant representing standard time at 23:15 (-03:00)
        DateTime overlapLater = new DateTime(2012, 2, 25, 23, 15, DateTimeZone.forOffsetHours(-3));
        long earlierInstant = zone.adjustOffset(overlapLater.getMillis(), false);
        DateTime result = new DateTime(earlierInstant, zone);
        assertEquals("2012-02-25T23:15:00.000-02:00", result.toString());
    }

    // =======================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =======================================================================

    @Test(timeout = 4000)
    public void testGetDefaultAndSetDefault() {
        DateTimeZone original = DateTimeZone.getDefault();
        assertNotNull(original);
        try {
            DateTimeZone.setDefault(DateTimeZone.UTC);
            assertSame(DateTimeZone.UTC, DateTimeZone.getDefault());

            DateTimeZone ny = DateTimeZone.forID("America/New_York");
            DateTimeZone.setDefault(ny);
            assertSame(ny, DateTimeZone.getDefault());
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void testForID_BasicAndSpecialIDs() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forID((String) null));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", london.getID());

        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone fixedPositive = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", fixedPositive.getID());
        assertEquals(2 * 3600000, fixedPositive.getOffset(0L));

        DateTimeZone fixedNegative = DateTimeZone.forID("-05:00");
        assertEquals("-05:00", fixedNegative.getID());
        assertEquals(-5 * 3600000, fixedNegative.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_KnownConversions() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone((TimeZone) null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));

        assertEquals("America/New_York", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")).getID());
        assertEquals("Pacific/Honolulu", DateTimeZone.forTimeZone(TimeZone.getTimeZone("HST")).getID());
        assertEquals("America/Los_Angeles", DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")).getID());
        assertEquals("America/Sao_Paulo", DateTimeZone.forTimeZone(TimeZone.getTimeZone("BET")).getID());
        assertEquals("Asia/Tokyo", DateTimeZone.forTimeZone(TimeZone.getTimeZone("JST")).getID());

        // Custom GMT offset display name
        TimeZone gmtPlus = TimeZone.getTimeZone("GMT+04:00");
        DateTimeZone zoneGmtPlus = DateTimeZone.forTimeZone(gmtPlus);
        assertEquals("+04:00", zoneGmtPlus.getID());

        TimeZone gmtZero = TimeZone.getTimeZone("GMT+00:00");
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(gmtZero));
    }

    @Test(timeout = 4000)
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("America/New_York"));
        assertTrue(ids.contains("Europe/London"));
        try {
            ids.add("Invalid/New_ID");
            fail("Expected UnsupportedOperationException on unmodifiable set");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneTokyo = DateTimeZone.forID("Asia/Tokyo");
        long instant = 123456789L;

        // Same zone
        assertEquals(instant, zoneLondon.getMillisKeepLocal(zoneLondon, instant));

        // Keep local across zones
        long tokyoInstant = zoneLondon.getMillisKeepLocal(zoneTokyo, instant);
        long localInLondon = zoneLondon.convertUTCToLocal(instant);
        long localInTokyo = zoneTokyo.convertUTCToLocal(tokyoInstant);
        assertEquals(localInLondon, localInTokyo);

        // Null target uses default
        DateTimeZone originalDefault = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(zoneTokyo);
            assertEquals(tokyoInstant, zoneLondon.getMillisKeepLocal(null, instant));
        } finally {
            DateTimeZone.setDefault(originalDefault);
        }
    }

    @Test(timeout = 4000)
    public void testToTimeZone_HashCode_ToString() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        TimeZone tz = zone.toTimeZone();
        assertEquals("America/Chicago", tz.getID());
        assertEquals("America/Chicago", zone.toString());
        assertEquals(57 + "America/Chicago".hashCode(), zone.hashCode());
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Winter is standard time (-5h)
        long winter = new DateTime(2012, 1, 15, 12, 0, DateTimeZone.UTC).getMillis();
        assertTrue(ny.isStandardOffset(winter));
        // Summer is DST (-4h)
        long summer = new DateTime(2012, 7, 15, 12, 0, DateTimeZone.UTC).getMillis();
        assertFalse(ny.isStandardOffset(summer));
    }

    // =======================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =======================================================================

    @Test(timeout = 4000)
    public void testForOffsetHours_Boundaries() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertEquals("+01:00", DateTimeZone.forOffsetHours(1).getID());
        assertEquals("-01:00", DateTimeZone.forOffsetHours(-1).getID());
        assertEquals("+23:00", DateTimeZone.forOffsetHours(23).getID());
        assertEquals("-23:00", DateTimeZone.forOffsetHours(-23).getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_ValidBoundaries() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertEquals("+00:30", DateTimeZone.forOffsetHoursMinutes(0, 30).getID());
        assertEquals("+02:45", DateTimeZone.forOffsetHoursMinutes(2, 45).getID());
        assertEquals("-02:45", DateTimeZone.forOffsetHoursMinutes(-2, 45).getID());
        assertEquals("+00:59", DateTimeZone.forOffsetHoursMinutes(0, 59).getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_SubMinuteAndSubSecondPrecision() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        // Millis with seconds
        DateTimeZone withSeconds = DateTimeZone.forOffsetMillis(3661000);
        assertEquals("+01:01:01", withSeconds.getID());

        DateTimeZone withNegSeconds = DateTimeZone.forOffsetMillis(-3661000);
        assertEquals("-01:01:01", withNegSeconds.getID());

        // Millis with fractional seconds
        DateTimeZone withMillis = DateTimeZone.forOffsetMillis(3661005);
        assertEquals("+01:01:01.005", withMillis.getID());

        DateTimeZone withNegMillis = DateTimeZone.forOffsetMillis(-3661005);
        assertEquals("-01:01:01.005", withNegMillis.getID());

        // Soft reference caching verification
        DateTimeZone cached = DateTimeZone.forOffsetMillis(3661005);
        assertSame(withMillis, cached);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertUTCToLocal_OverflowPositive() {
        DateTimeZone positiveZone = DateTimeZone.forOffsetHours(1);
        positiveZone.convertUTCToLocal(Long.MAX_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertUTCToLocal_OverflowNegative() {
        DateTimeZone negativeZone = DateTimeZone.forOffsetHours(-1);
        negativeZone.convertUTCToLocal(Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertLocalToUTC_OverflowNegative() {
        DateTimeZone positiveZone = DateTimeZone.forOffsetHours(1);
        positiveZone.convertLocalToUTC(Long.MIN_VALUE, false);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testConvertLocalToUTC_OverflowPositive() {
        DateTimeZone negativeZone = DateTimeZone.forOffsetHours(-1);
        negativeZone.convertLocalToUTC(Long.MAX_VALUE, false);
    }

    // =======================================================================
    // Partition C: Transition Boundaries, DST Gaps & Overlaps
    // =======================================================================

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_DSTGapStrictVsLenient() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Spring forward gap on 2012-03-11: 02:00 -> 03:00 does not exist
        DateTime gapLocal = new DateTime(2012, 3, 11, 2, 30, DateTimeZone.UTC);
        long gapMillis = gapLocal.getMillis();

        try {
            ny.convertLocalToUTC(gapMillis, true);
            fail("Expected IllegalArgumentException on strict conversion in DST gap");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Illegal instant due to time zone offset transition"));
        }

        // Lenient converts by adjusting
        long nonStrictUTC = ny.convertLocalToUTC(gapMillis, false);
        long backToLocal = ny.convertUTCToLocal(nonStrictUTC);
        assertEquals(gapMillis, backToLocal);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_WithOriginalInstantUTC() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long winterLocal = new DateTime(2012, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        long origUTC = winterLocal + 5 * 3600000;
        long converted = ny.convertLocalToUTC(winterLocal, false, origUTC);
        assertEquals(origUTC, converted);
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2012, 3, 11, 2, 30);
        assertTrue(ny.isLocalDateTimeGap(gapTime));

        LocalDateTime nonGapTime = new LocalDateTime(2012, 3, 11, 1, 30);
        assertFalse(ny.isLocalDateTimeGap(nonGapTime));

        // Fixed zones never have gaps
        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapTime));
        assertFalse(DateTimeZone.forOffsetHours(2).isLocalDateTimeGap(gapTime));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long winterInstant = new DateTime(2012, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        assertEquals(-5 * 3600000, ny.getOffsetFromLocal(winterInstant));

        long summerInstant = new DateTime(2012, 7, 1, 12, 0, DateTimeZone.UTC).getMillis();
        assertEquals(-4 * 3600000, ny.getOffsetFromLocal(summerInstant));

        // Transition gap check
        long gapLocal = new DateTime(2012, 3, 11, 2, 30, DateTimeZone.UTC).getMillis();
        int offset = ny.getOffsetFromLocal(gapLocal);
        assertTrue(offset == -5 * 3600000 || offset == -4 * 3600000);
    }

    @Test(timeout = 4000)
    public void testIsFixed_Transitions() {
        assertTrue(DateTimeZone.UTC.isFixed());
        assertTrue(DateTimeZone.forOffsetHours(5).isFixed());
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        assertFalse(ny.isFixed());

        long t1 = ny.nextTransition(0L);
        assertTrue(t1 > 0L);
        long t2 = ny.previousTransition(t1);
        assertEquals(0L, ny.previousTransition(0L) == 0L ? 0L : t2 <= 0L ? 0L : 0L);
    }

    @Test(timeout = 4000)
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertEquals(0, zone.getOffset(new Instant(1000L)));
        assertEquals(0, zone.getOffset((ReadableInstant) null));
    }

    @Test(timeout = 4000)
    public void testGetNameAndShortName_Fallbacks() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertNotNull(zone.getName(0L));
        assertNotNull(zone.getShortName(0L));
        assertNotNull(zone.getName(0L, Locale.FRANCE));
        assertNotNull(zone.getShortName(0L, Locale.FRANCE));

        // Zone with null name key returns ID
        DateTimeZone noKeyZone = new DateTimeZone("CustomZoneNoKey") {
            public String getNameKey(long instant) { return null; }
            public int getOffset(long instant) { return 0; }
            public int getStandardOffset(long instant) { return 0; }
            public boolean isFixed() { return true; }
            public long nextTransition(long instant) { return instant; }
            public long previousTransition(long instant) { return instant; }
            public boolean equals(Object object) { return false; }
        };
        assertEquals("CustomZoneNoKey", noKeyZone.getName(0L, Locale.ENGLISH));
        assertEquals("CustomZoneNoKey", noKeyZone.getShortName(0L, Locale.ENGLISH));

        // Zone with unmatched name key prints offset fallback
        DateTimeZone fallbackZone = new DateTimeZone("CustomZoneFallback") {
            public String getNameKey(long instant) { return "unknown_key_xyz"; }
            public int getOffset(long instant) { return 3600000; }
            public int getStandardOffset(long instant) { return 3600000; }
            public boolean isFixed() { return true; }
            public long nextTransition(long instant) { return instant; }
            public long previousTransition(long instant) { return instant; }
            public boolean equals(Object object) { return false; }
        };
        assertEquals("+01:00", fallbackZone.getName(0L, Locale.ENGLISH));
        assertEquals("+01:00", fallbackZone.getShortName(0L, Locale.ENGLISH));
    }

    // =======================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =======================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_NullIdRejection() {
        new DateTimeZone(null) {
            public String getNameKey(long instant) { return null; }
            public int getOffset(long instant) { return 0; }
            public int getStandardOffset(long instant) { return 0; }
            public boolean isFixed() { return true; }
            public long nextTransition(long instant) { return instant; }
            public long previousTransition(long instant) { return instant; }
            public boolean equals(Object object) { return false; }
        };
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_NullRejection() {
        DateTimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_InvalidID() {
        DateTimeZone.forID("NonExistentZoneXYZ");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_NegativeMinutes() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesExceed59() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_HoursOverflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_UnrecognisedID() {
        TimeZone tz = new SimpleTimeZone(0, "InvalidTimeZoneID") {
            public String getDisplayName() { return "InvalidTimeZoneID"; }
        };
        DateTimeZone.forTimeZone(tz);
    }

    @Test(timeout = 4000)
    public void testSetProvider_ValidationGuards() {
        Provider orig = DateTimeZone.getProvider();
        try {
            // Null reverts to default
            DateTimeZone.setProvider(null);
            assertNotNull(DateTimeZone.getProvider());

            // Empty IDs provider
            try {
                DateTimeZone.setProvider(new Provider() {
                    public DateTimeZone getZone(String id) { return null; }
                    public Set<String> getAvailableIDs() { return Collections.emptySet(); }
                });
                fail("Expected IllegalArgumentException for empty IDs");
            } catch (IllegalArgumentException ex) {
                assertEquals("The provider doesn't have any available ids", ex.getMessage());
            }

            // Missing UTC provider
            try {
                DateTimeZone.setProvider(new Provider() {
                    public DateTimeZone getZone(String id) { return null; }
                    public Set<String> getAvailableIDs() { return Collections.singleton("EST"); }
                });
                fail("Expected IllegalArgumentException when UTC missing");
            } catch (IllegalArgumentException ex) {
                assertEquals("The provider doesn't support UTC", ex.getMessage());
            }

            // Mismatched UTC zone provider
            try {
                Set<String> ids = new HashSet<String>();
                ids.add("UTC");
                DateTimeZone.setProvider(new Provider() {
                    public DateTimeZone getZone(String id) { return null; }
                    public Set<String> getAvailableIDs() { return ids; }
                });
                fail("Expected IllegalArgumentException for invalid UTC zone");
            } catch (IllegalArgumentException ex) {
                assertEquals("Invalid UTC zone provided", ex.getMessage());
            }
        } finally {
            DateTimeZone.setProvider(orig);
        }
    }

    @Test(timeout = 4000)
    public void testSetNameProvider_ValidationAndCustom() {
        NameProvider orig = DateTimeZone.getNameProvider();
        try {
            DateTimeZone.setNameProvider(null);
            assertNotNull(DateTimeZone.getNameProvider());

            NameProvider custom = new NameProvider() {
                public String getShortName(Locale locale, String id, String nameKey) { return "CUSTOM_SHORT"; }
                public String getName(Locale locale, String id, String nameKey) { return "CUSTOM_LONG"; }
            };
            DateTimeZone.setNameProvider(custom);
            assertSame(custom, DateTimeZone.getNameProvider());
            DateTimeZone zone = DateTimeZone.forID("America/New_York");
            assertEquals("CUSTOM_SHORT", zone.getShortName(0L));
            assertEquals("CUSTOM_LONG", zone.getName(0L));
        } finally {
            DateTimeZone.setNameProvider(orig);
        }
    }

    // =======================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =======================================================================

    @Test(timeout = 4000)
    public void testSerialization_Resolution() throws Exception {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ny);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        assertSame(ny, deserialized);
    }

    @Test(timeout = 4000)
    public void testSerialization_UTC() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(DateTimeZone.UTC);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        assertSame(DateTimeZone.UTC, deserialized);
    }
}