package org.joda.time;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
 * -------------------------------------------------------------------------------------------------------
 * Target Method                   | Branch / Condition Analyzed                 | Target Assertions
 * -------------------------------------------------------------------------------------------------------
 * forID(String)                   | id == null                                  | returns getDefault()
 *                                 | id.equals("UTC")                            | returns DateTimeZone.UTC
 *                                 | cProvider.getZone(id) != null               | returns zone (e.g., "WET", "America/New_York")
 *                                 | startsWith("+") or startsWith("-")          | parses fixed offset, handles zero / non-zero
 *                                 | invalid/unknown ID                          | throws IllegalArgumentException
 *                                 | DEFECT TARGET: forID("WET")                 | asserts getID() equals "WET"
 * -------------------------------------------------------------------------------------------------------
 * forOffsetHours(int)             | hoursOffset == 0                            | returns DateTimeZone.UTC
 *                                 | out of range / safeMultiply overflow        | throws IllegalArgumentException
 * -------------------------------------------------------------------------------------------------------
 * forOffsetHoursMinutes(int, int) | (0, 0) boundary                             | returns DateTimeZone.UTC
 *                                 | minutesOffset < 0 || minutesOffset > 59     | throws IllegalArgumentException
 *                                 | negative hours with positive minutes        | calculates negative combined offset
 *                                 | safeMultiply / safeAdd overflow             | throws IllegalArgumentException
 * -------------------------------------------------------------------------------------------------------
 * forOffsetMillis(int)            | offset == 0                                 | returns DateTimeZone.UTC
 *                                 | offset != 0, cache hit / miss               | fixedOffsetZone caching & formatting
 * -------------------------------------------------------------------------------------------------------
 * forTimeZone(TimeZone)           | zone == null                                | returns getDefault()
 *                                 | zone.getID().equals("UTC")                  | returns DateTimeZone.UTC
 *                                 | getConvertedId(id) != null                  | converts old JDK aliases (e.g., "PST")
 *                                 | custom GMT+/-hh:mm displayName              | parses offset or returns UTC for GMT0
 *                                 | unknown / unsupported TimeZone              | throws IllegalArgumentException
 * -------------------------------------------------------------------------------------------------------
 * getDefault() / setDefault(Zone) | cDefault == null initialization             | returns non-null default
 *                                 | setDefault(null)                            | throws IllegalArgumentException
 *                                 | setDefault(validZone)                       | updates cDefault
 * -------------------------------------------------------------------------------------------------------
 * setProvider / setNameProvider   | provider == null -> default fallback        | resets provider correctly
 *                                 | invalid provider missing UTC or empty IDs   | throws IllegalArgumentException
 * -------------------------------------------------------------------------------------------------------
 * convertUTCToLocal(long)         | arithmetic overflow checks                  | throws ArithmeticException on overflow
 * convertLocalToUTC(long, bool)   | strict gap detection                        | throws IllegalArgumentException in gap
 *                                 | non-strict western hemisphere adjust        | corrects offset to offsetLocal
 *                                 | arithmetic underflow/overflow               | throws ArithmeticException on overflow
 * convertLocalToUTC(long, b, long)| originalInstantUTC offset match / mismatch  | preserves original or delegates
 * -------------------------------------------------------------------------------------------------------
 * adjustOffset(long, boolean)     | no overlap                                  | returns original instant
 *                                 | overlap earlier (false) vs later (true)     | adjusts by DST offset gap
 * -------------------------------------------------------------------------------------------------------
 * Serialization (writeReplace)    | Stub round-trip                             | serializes & deserializes same zone
 * =======================================================================================================
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

    // =======================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =======================================================================

    /**
     * Targets Defect: testForID_String_old
     * Ground Truth: Calling DateTimeZone.forID("WET") must return a DateTimeZone
     * whose ID is exactly "WET" without incorrectly aliasing or remapping it.
     */
    @Test(timeout = 4000)
    public void testDefect_forID_String_old_WET() {
        DateTimeZone zone = DateTimeZone.forID("WET");
        assertNotNull("DateTimeZone.forID(\"WET\") must not be null", zone);
        assertEquals("DateTimeZone ID should be exactly WET", "WET", zone.getID());
    }

    // =======================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =======================================================================

    @Test(timeout = 4000)
    public void testGetDefaultAndSetDefault() {
        DateTimeZone currentDefault = DateTimeZone.getDefault();
        assertNotNull(currentDefault);

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        assertSame(paris, DateTimeZone.getDefault());

        DateTimeZone.setDefault(DateTimeZone.UTC);
        assertSame(DateTimeZone.UTC, DateTimeZone.getDefault());
    }

    @Test(timeout = 4000)
    public void testForID_NullAndUTC() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forID(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test(timeout = 4000)
    public void testForID_FixedOffsets() {
        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);

        DateTimeZone zoneMinusZero = DateTimeZone.forID("-00:00");
        assertSame(DateTimeZone.UTC, zoneMinusZero);

        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * 3600000, zonePlus.getOffset(0L));
        assertTrue(zonePlus.isFixed());

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zoneMinus.getID());
        assertEquals(-(5 * 3600000 + 30 * 60000), zoneMinus.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_Valid() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));

        DateTimeZone zone5 = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone5.getID());
        assertEquals(5 * 3600000, zone5.getOffset(0L));

        DateTimeZone zoneMinus8 = DateTimeZone.forOffsetHours(-8);
        assertEquals("-08:00", zoneMinus8.getID());
        assertEquals(-8 * 3600000, zoneMinus8.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Valid() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zonePositive = DateTimeZone.forOffsetHoursMinutes(5, 45);
        assertEquals("+05:45", zonePositive.getID());
        assertEquals(5 * 3600000 + 45 * 60000, zonePositive.getOffset(0L));

        DateTimeZone zoneNegative = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals("-02:30", zoneNegative.getID());
        assertEquals(-(2 * 3600000 + 30 * 60000), zoneNegative.getOffset(0L));

        DateTimeZone zoneZeroHoursNegativeMin = DateTimeZone.forOffsetHoursMinutes(0, 15);
        assertEquals("+00:15", zoneZeroHoursNegativeMin.getID());
        assertEquals(15 * 60000, zoneZeroHoursNegativeMin.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_CachingAndExtremes() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(3600000);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(3600000);
        assertSame("Fixed offset zones should be cached", zone1, zone2);
        assertEquals("+01:00", zone1.getID());

        // Offset with milliseconds precision
        DateTimeZone zoneMillis = DateTimeZone.forOffsetMillis(1234);
        assertEquals("+00:00:01.234", zoneMillis.getID());
        assertEquals(1234, zoneMillis.getOffset(0L));

        DateTimeZone zoneNegMillis = DateTimeZone.forOffsetMillis(-3661001);
        assertEquals("-01:01:01.001", zoneNegMillis.getID());
        assertEquals(-3661001, zoneNegMillis.getOffset(0L));

        // Offset with seconds only
        DateTimeZone zoneSeconds = DateTimeZone.forOffsetMillis(61000);
        assertEquals("+00:01:01", zoneSeconds.getID());
    }

    @Test(timeout = 4000)
    public void testForTimeZone_AllBranches() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        // Alias conversion: PST -> America/Los_Angeles
        DateTimeZone pst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", pst.getID());

        // Standard GMT format: GMT+01:00
        DateTimeZone gmtPlus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+01:00"));
        assertEquals("+01:00", gmtPlus.getID());

        DateTimeZone gmtZero = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-00:00"));
        assertSame(DateTimeZone.UTC, gmtZero);
    }

    @Test(timeout = 4000)
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("Europe/London"));
        assertTrue(ids.contains("America/New_York"));
    }

    @Test(timeout = 4000)
    public void testNameProviderAndLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01, standard time GMT
        long summerInstant = 15000000000L; // Summer time BST

        assertNotNull(zone.getName(winterInstant));
        assertNotNull(zone.getName(winterInstant, Locale.UK));
        assertNotNull(zone.getShortName(winterInstant));
        assertNotNull(zone.getShortName(winterInstant, Locale.UK));

        assertEquals("Europe/London", zone.getID());
        assertEquals("Europe/London", zone.toString());

        // Test with custom NameProvider
        NameProvider originalNP = DateTimeZone.getNameProvider();
        try {
            DateTimeZone.setNameProvider(new NameProvider() {
                public String getShortName(Locale locale, String id, String nameKey) {
                    return "SHORT_TEST";
                }
                public String getName(Locale locale, String id, String nameKey) {
                    return "LONG_TEST";
                }
            });
            assertEquals("SHORT_TEST", zone.getShortName(winterInstant, Locale.ENGLISH));
            assertEquals("LONG_TEST", zone.getName(winterInstant, Locale.ENGLISH));
        } finally {
            DateTimeZone.setNameProvider(originalNP);
        }

        // Test setting null NameProvider reverts to default
        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
        assertTrue(DateTimeZone.getNameProvider() instanceof DefaultNameProvider);
    }

    @Test(timeout = 4000)
    public void testGetOffsetWithReadableInstant() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        assertEquals(3 * 3600000, zone.getOffset(new Instant(100000L)));
        assertEquals(3 * 3600000, zone.getOffset((ReadableInstant) null));
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winter = 0L; // 1970-01-01
        assertTrue(zone.isStandardOffset(winter));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");

        long instant = 10000000L;
        // Same zone
        assertEquals(instant, london.getMillisKeepLocal(london, instant));

        // Null zone defaults to default DateTimeZone
        long expectedWithDefault = london.getMillisKeepLocal(DateTimeZone.getDefault(), instant);
        assertEquals(expectedWithDefault, london.getMillisKeepLocal(null, instant));

        // Different zone: Paris is UTC+1 in winter, London is UTC+0 -> Paris UTC time is 1 hour earlier
        long converted = london.getMillisKeepLocal(paris, instant);
        assertEquals(instant - 3600000L, converted);
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertFalse(fixed.isLocalDateTimeGap(new LocalDateTime(2020, 3, 29, 1, 30)));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Spring forward in London: 2020-03-29 01:00 -> 02:00
        LocalDateTime gapTime = new LocalDateTime(2020, 3, 29, 1, 30);
        assertTrue(london.isLocalDateTimeGap(gapTime));

        LocalDateTime normalTime = new LocalDateTime(2020, 3, 29, 3, 30);
        assertFalse(london.isLocalDateTimeGap(normalTime));
    }

    @Test(timeout = 4000)
    public void testAdjustOffset() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(1);
        assertEquals(5000L, fixed.adjustOffset(5000L, true));
        assertEquals(5000L, fixed.adjustOffset(5000L, false));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Fall back transition: 2020-10-25 02:00 -> 01:00 (overlap between 01:00 and 02:00)
        // 2020-10-25 01:30 BST = 2020-10-25 00:30 UTC
        DateTime overlapBst = new DateTime(2020, 10, 25, 1, 30, DateTimeZone.forID("+01:00"));
        long overlapInstant = overlapBst.getMillis();

        long earlier = london.adjustOffset(overlapInstant, false);
        long later = london.adjustOffset(overlapInstant, true);
        assertEquals(3600000L, later - earlier);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_Branches() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");

        // Normal instant
        assertEquals(0, london.getOffsetFromLocal(0L));

        // Spring forward gap: 2020-03-29 01:30 local time
        // Local representation in millis
        DateTime gapLocal = new DateTime(2020, 3, 29, 1, 30, DateTimeZone.UTC);
        int offsetGap = london.getOffsetFromLocal(gapLocal.getMillis());
        // In gap, offsetLocal != offsetAdjusted
        assertTrue(offsetGap == 0 || offsetGap == 3600000);

        // Fall back overlap: 2020-10-25 01:30 local time
        DateTime overlapLocal = new DateTime(2020, 10, 25, 1, 30, DateTimeZone.UTC);
        int offsetOverlap = london.getOffsetFromLocal(overlapLocal.getMillis());
        assertEquals(3600000, offsetOverlap); // returns summer offset (earlier instant)
    }

    // =======================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =======================================================================

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_Overflow() {
        DateTimeZone zonePlus = DateTimeZone.forOffsetHours(1);
        try {
            zonePlus.convertUTCToLocal(Long.MAX_VALUE);
            fail("Expected ArithmeticException on positive overflow");
        } catch (ArithmeticException expected) {
            // expected
        }

        DateTimeZone zoneMinus = DateTimeZone.forOffsetHours(-1);
        try {
            zoneMinus.convertUTCToLocal(Long.MIN_VALUE);
            fail("Expected ArithmeticException on negative overflow");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Overflow() {
        DateTimeZone zoneMinus = DateTimeZone.forOffsetHours(-1);
        try {
            zoneMinus.convertLocalToUTC(Long.MAX_VALUE, false);
            fail("Expected ArithmeticException on positive overflow");
        } catch (ArithmeticException expected) {
            // expected
        }

        DateTimeZone zonePlus = DateTimeZone.forOffsetHours(1);
        try {
            zonePlus.convertLocalToUTC(Long.MIN_VALUE, false);
            fail("Expected ArithmeticException on negative overflow");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_StrictGap() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Spring forward gap: 2020-03-29 01:30
        DateTime gapLocal = new DateTime(2020, 3, 29, 1, 30, DateTimeZone.UTC);
        try {
            london.convertLocalToUTC(gapLocal.getMillis(), true);
            fail("Expected IllegalArgumentException in strict mode for DST gap");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Illegal instant due to time zone offset transition"));
        }

        // Non-strict should adjust gracefully
        long nonStrictUtc = london.convertLocalToUTC(gapLocal.getMillis(), false);
        assertTrue(nonStrictUtc > 0);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_WithOriginalInstant() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long instantLocal = 100000000L;
        long originalInstantUTC = 100000000L;

        long result = london.convertLocalToUTC(instantLocal, false, originalInstantUTC);
        assertEquals(instantLocal - london.getOffset(originalInstantUTC), result);
    }

    // =======================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =======================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_Null() {
        DateTimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_InvalidID() {
        DateTimeZone.forID("NonExistentTimeZone123");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_InvalidOffsetFormat() {
        DateTimeZone.forID("+Invalid");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_HoursOverflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_UnknownZone() {
        TimeZone tz = new TimeZone() {
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
                return 0;
            }
            public void setRawOffset(int offsetMillis) {}
            public int getRawOffset() {
                return 0;
            }
            public boolean useDaylightTime() {
                return false;
            }
            public boolean inDaylightTime(java.util.Date date) {
                return false;
            }
            public String getID() {
                return "Unknown_Zone_XYZ";
            }
        };
        DateTimeZone.forTimeZone(tz);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_EmptyProvider() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                return Collections.emptySet();
            }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_NoUTC() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("Europe/London");
                return set;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_InvalidUTCZone() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return DateTimeZone.forOffsetHours(1); // non-zero offset for UTC
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("UTC");
                return set;
            }
        });
    }

    @Test(timeout = 4000)
    public void testSetProvider_NullResetsToDefault() {
        DateTimeZone.setProvider(new UTCProvider());
        assertEquals(1, DateTimeZone.getAvailableIDs().size());

        DateTimeZone.setProvider(null);
        assertTrue(DateTimeZone.getAvailableIDs().size() > 1);
    }

    // =======================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =======================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone3 = DateTimeZone.forID("Europe/London");

        assertEquals(zone1, zone2);
        assertNotEquals(zone1, zone3);
        assertNotEquals(zone1, null);
        assertNotEquals(zone1, "Europe/Paris");

        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals(57 + "Europe/Paris".hashCode(), zone1.hashCode());
    }

    @Test(timeout = 4000)
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        TimeZone tz = zone.toTimeZone();
        assertNotNull(tz);
        assertEquals("Europe/London", tz.getID());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        DateTimeZone original = DateTimeZone.forID("America/New_York");

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
    public void testFixedDateTimeZoneTransitions() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertTrue(fixed.isFixed());
        assertEquals(12345L, fixed.nextTransition(12345L));
        assertEquals(12345L, fixed.previousTransition(12345L));
        assertEquals(2 * 3600000, fixed.getStandardOffset(0L));
    }
}