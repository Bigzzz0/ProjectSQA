/*
 *  Copyright 2001-2011 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time;

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

import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGETED METHODS & BRANCHES:
 * 1. getDefault() / setDefault(DateTimeZone):
 *    - null zone handling (IllegalArgumentException).
 *    - fallback hierarchy: user.timezone -> TimeZone.getDefault() -> UTC.
 * 2. forID(String):
 *    - null -> default zone.
 *    - "UTC" -> DateTimeZone.UTC.
 *    - provider lookup hit/miss.
 *    - prefix '+' / '-' offset parsing; 0 offset returns UTC; non-zero creates fixedOffsetZone.
 *    - invalid id format -> IllegalArgumentException.
 * 3. forOffsetHours(int), forOffsetHoursMinutes(int, int), forOffsetMillis(int):
 *    - (0, 0) -> UTC.
 *    - minutes < 0 or minutes > 59 -> IllegalArgumentException.
 *    - negative hours with minutes: correctly subtracts minutes.
 *    - positive hours with minutes: adds minutes.
 *    - arithmetic overflow (safeMultiply, extreme values) -> IllegalArgumentException.
 * 4. forTimeZone(TimeZone):
 *    - null -> default zone.
 *    - ID "UTC" -> UTC.
 *    - legacy conversions (e.g., "PST", "EST", "WET", "CET", "MET", "ECT", "EET", "HST").
 *    - custom/unrecognized TimeZone IDs with "GMT+hh:mm" or "GMT-hh:mm" display names.
 *    - completely unrecognized TimeZone -> IllegalArgumentException.
 * 5. getOffsetFromLocal(long):
 *    - DST gap resolution (Western vs Eastern hemisphere cutovers).
 *    - DST overlap resolution: KNOWN DEFECT in London cutover (2011-10-30T01:15:00.000) where the
 *      earlier (daylight savings, BST +01:00) offset must be selected over standard winter time (+00:00).
 * 6. convertUTCToLocal(long) / convertLocalToUTC(long, boolean, long) / convertLocalToUTC(long, boolean):
 *    - arithmetic overflow guards (Long.MAX_VALUE / Long.MIN_VALUE).
 *    - DST gap with strict=true (throws IllegalArgumentException).
 *    - DST gap with strict=false (returns offsetLocal).
 *    - originalInstantUTC offset reuse in 3-arg convertLocalToUTC.
 * 7. printOffset(int) / parseOffset(String):
 *    - hours, minutes, seconds, milliseconds formatting boundaries.
 * 8. isLocalDateTimeGap(LocalDateTime) / adjustOffset(long, boolean):
 *    - gap detection on fixed vs transition zones.
 *    - overlap adjustment (earlier vs later).
 * 9. Serialization & Contract Integrity:
 *    - writeReplace() -> Stub round-trip via serialization stream.
 *    - hashCode, equals, toString, toTimeZone.
 * ----------------------------------------------------------------------------------------------------
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
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: testDateTimeCreation_london
     * In Europe/London, on 2011-10-30 at 01:15 local time, an overlap occurs (BST -> GMT).
     * The specification requires returning the earlier instant (+01:00) during an overlap.
     */
    @Test(timeout = 4000)
    public void testDefect_DateTimeCreation_london_overlap() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTime dt = new DateTime(2011, 10, 30, 1, 15, 0, 0, zoneLondon);
        assertEquals("2011-10-30T01:15:00.000+01:00", dt.toString());
        assertEquals(3600000, zoneLondon.getOffset(dt.getMillis()));
    }

    /**
     * Target Defect: testOffsetFromLocal_london_overlap
     * Direct test on getOffsetFromLocal for the Europe/London overlap instant.
     */
    @Test(timeout = 4000)
    public void testDefect_getOffsetFromLocal_overlapLondon() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        // Local 1:15 AM on Oct 30, 2011 is inside the DST fall-back overlap
        DateTime localDt = new DateTime(2011, 10, 30, 1, 15, 0, 0, DateTimeZone.UTC);
        long localMillis = localDt.getMillis();
        int offset = zoneLondon.getOffsetFromLocal(localMillis);
        assertEquals("During overlap, getOffsetFromLocal must prefer daylight savings (+01:00)", 3600000, offset);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetDefault_and_SetDefault() {
        assertNotNull(DateTimeZone.getDefault());
        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");
        DateTimeZone.setDefault(tokyo);
        assertEquals(tokyo, DateTimeZone.getDefault());

        DateTimeZone.setDefault(originalDefault);
        assertEquals(originalDefault, DateTimeZone.getDefault());
    }

    @Test(timeout = 4000)
    public void testForID_BasicAndSpecialIDs() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forID(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        assertNotNull(paris);
        assertEquals("Europe/Paris", paris.getID());

        DateTimeZone plusTwo = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", plusTwo.getID());
        assertEquals(7200000, plusTwo.getOffset(0L));

        DateTimeZone minusFive = DateTimeZone.forID("-05:00");
        assertEquals("-05:00", minusFive.getID());
        assertEquals(-18000000, minusFive.getOffset(0L));

        DateTimeZone zeroOffset = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zeroOffset);

        DateTimeZone zeroMinus = DateTimeZone.forID("-00:00");
        assertSame(DateTimeZone.UTC, zeroMinus);
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_and_HoursMinutes() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zonePlus5 = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zonePlus5.getID());
        assertEquals(5 * 3600000, zonePlus5.getOffset(0L));

        DateTimeZone zoneMinus530 = DateTimeZone.forOffsetHoursMinutes(-5, 30);
        assertEquals("-05:30", zoneMinus530.getID());
        assertEquals(-(5 * 3600000 + 30 * 60000), zoneMinus530.getOffset(0L));

        DateTimeZone zonePlus530 = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zonePlus530.getID());
        assertEquals(5 * 3600000 + 30 * 60000, zonePlus530.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_FullResolution() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        // Offset with seconds and milliseconds
        int millis = 3600000 + 120000 + 4000 + 500; // 01:02:04.500
        DateTimeZone customZone = DateTimeZone.forOffsetMillis(millis);
        assertEquals("+01:02:04.500", customZone.getID());
        assertEquals(millis, customZone.getOffset(0L));

        int negMillis = -(3600000 + 120000 + 4000 + 500);
        DateTimeZone customNegZone = DateTimeZone.forOffsetMillis(negMillis);
        assertEquals("-01:02:04.500", customNegZone.getID());
        assertEquals(negMillis, customNegZone.getOffset(0L));

        // Offset with seconds only
        int millisWithSec = 3600000 + 120000 + 45000; // 01:02:45
        DateTimeZone secZone = DateTimeZone.forOffsetMillis(millisWithSec);
        assertEquals("+01:02:45", secZone.getID());

        // Cache verification: Same millisecond offset should return cached instance
        DateTimeZone cached = DateTimeZone.forOffsetMillis(millisWithSec);
        assertSame(secZone, cached);
    }

    @Test(timeout = 4000)
    public void testForTimeZone_Conversions() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        // Test converted ID alias mappings
        assertEquals("America/Los_Angeles", DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")).getID());
        assertEquals("America/New_York", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")).getID());
        assertEquals("America/Chicago", DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")).getID());
        assertEquals("America/Denver", DateTimeZone.forTimeZone(TimeZone.getTimeZone("MST")).getID());
        assertEquals("Europe/London", DateTimeZone.forTimeZone(TimeZone.getTimeZone("WET")).getID());
        assertEquals("Europe/Paris", DateTimeZone.forTimeZone(TimeZone.getTimeZone("CET")).getID());
        assertEquals("Europe/Bucharest", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EET")).getID());
        assertEquals("Pacific/Honolulu", DateTimeZone.forTimeZone(TimeZone.getTimeZone("HST")).getID());

        // Test custom TimeZone with GMT+hh:mm style display name
        TimeZone customGmt = new SimpleTimeZone(7200000, "GMT-02:00");
        DateTimeZone fromCustom = DateTimeZone.forTimeZone(customGmt);
        assertEquals("-02:00", fromCustom.getID());

        TimeZone zeroGmt = new SimpleTimeZone(0, "GMT+00:00");
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(zeroGmt));
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
    public void testNames_and_Localization() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2010, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        assertNotNull(zone.getNameKey(winterInstant));
        assertNotNull(zone.getNameKey(summerInstant));

        String shortWinter = zone.getShortName(winterInstant, Locale.UK);
        String shortSummer = zone.getShortName(summerInstant, Locale.UK);
        assertNotNull(shortWinter);
        assertNotNull(shortSummer);

        String longWinter = zone.getName(winterInstant, Locale.UK);
        String longSummer = zone.getName(summerInstant, Locale.UK);
        assertNotNull(longWinter);
        assertNotNull(longSummer);

        // Fallback checks with null locale
        assertEquals(zone.getShortName(winterInstant, Locale.getDefault()), zone.getShortName(winterInstant, null));
        assertEquals(zone.getName(winterInstant, Locale.getDefault()), zone.getName(winterInstant, null));
        assertEquals(zone.getShortName(winterInstant), zone.getShortName(winterInstant, null));
        assertEquals(zone.getName(winterInstant), zone.getName(winterInstant, null));
    }

    @Test(timeout = 4000)
    public void testOffsetMethods_and_IsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2010, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        assertEquals(0, zone.getOffset(winterInstant));
        assertEquals(3600000, zone.getOffset(summerInstant));

        ReadableInstant nullInstant = null;
        assertTrue(zone.getOffset(nullInstant) == 0 || zone.getOffset(nullInstant) == 3600000);

        ReadableInstant instantObj = new Instant(summerInstant);
        assertEquals(3600000, zone.getOffset(instantObj));

        assertEquals(0, zone.getStandardOffset(winterInstant));
        assertEquals(0, zone.getStandardOffset(summerInstant));

        assertTrue(zone.isStandardOffset(winterInstant));
        assertFalse(zone.isStandardOffset(summerInstant));
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_and_ConvertLocalToUTC() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris"); // UTC+1 in winter, UTC+2 in summer
        long winterUTC = new DateTime(2010, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long local = zone.convertUTCToLocal(winterUTC);
        assertEquals(winterUTC + 3600000, local);

        long backToUTC = zone.convertLocalToUTC(local, false);
        assertEquals(winterUTC, backToUTC);

        long backToUTCStrict = zone.convertLocalToUTC(local, true);
        assertEquals(winterUTC, backToUTCStrict);

        long backToUTC3Arg = zone.convertLocalToUTC(local, false, winterUTC);
        assertEquals(winterUTC, backToUTC3Arg);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");

        long instant = new DateTime(2010, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        // Same zone
        assertEquals(instant, zoneLondon.getMillisKeepLocal(zoneLondon, instant));

        // Different zone
        long londonLocal = zoneLondon.convertUTCToLocal(instant);
        long inParis = zoneLondon.getMillisKeepLocal(zoneParis, instant);
        long parisLocal = zoneParis.convertUTCToLocal(inParis);
        assertEquals(londonLocal, parisLocal);

        // Null target zone defaults to default zone
        long withNull = zoneLondon.getMillisKeepLocal(null, instant);
        long withDefault = zoneLondon.getMillisKeepLocal(DateTimeZone.getDefault(), instant);
        assertEquals(withDefault, withNull);
    }

    @Test(timeout = 4000)
    public void testTransitions_and_IsFixed() {
        assertTrue(DateTimeZone.UTC.isFixed());
        assertEquals(0L, DateTimeZone.UTC.nextTransition(0L));
        assertEquals(0L, DateTimeZone.UTC.previousTransition(0L));

        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertFalse(zone.isFixed());
        long instant = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long next = zone.nextTransition(instant);
        assertTrue(next > instant);
        long prev = zone.previousTransition(next);
        assertTrue(prev <= next);
    }

    @Test(timeout = 4000)
    public void testAdjustOffset() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        // Overlap on 2011-10-30: 01:15 BST (+01:00) vs 01:15 GMT (+00:00)
        long instantBST = new DateTime(2011, 10, 30, 1, 15, 0, 0, DateTimeZone.forOffsetHours(1)).getMillis();
        long adjustedEarlier = zoneLondon.adjustOffset(instantBST, false);
        long adjustedLater = zoneLondon.adjustOffset(instantBST, true);
        assertTrue(adjustedEarlier <= adjustedLater);
        assertEquals(3600000, adjustedLater - adjustedEarlier);

        // Fixed zone adjustOffset returns same instant
        assertEquals(1000L, DateTimeZone.UTC.adjustOffset(1000L, true));
        assertEquals(1000L, DateTimeZone.UTC.adjustOffset(1000L, false));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertFalse(fixed.isLocalDateTimeGap(new LocalDateTime(2011, 3, 27, 2, 30)));

        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        // In London, 2011-03-27 01:00 to 01:59 was skipped (gap)
        LocalDateTime gapTime = new LocalDateTime(2011, 3, 27, 1, 30);
        assertTrue(zoneLondon.isLocalDateTimeGap(gapTime));

        LocalDateTime regularTime = new LocalDateTime(2011, 3, 27, 3, 30);
        assertFalse(zoneLondon.isLocalDateTimeGap(regularTime));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Boundaries() {
        DateTimeZone maxPositive = DateTimeZone.forOffsetHoursMinutes(23, 59);
        assertEquals("+23:59", maxPositive.getID());

        DateTimeZone maxNegative = DateTimeZone.forOffsetHoursMinutes(-23, 59);
        assertEquals("-23:59", maxNegative.getID());

        DateTimeZone zeroHours59Mins = DateTimeZone.forOffsetHoursMinutes(0, 59);
        assertEquals("+00:59", zeroHours59Mins.getID());

        DateTimeZone zeroHours0Mins = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, zeroHours0Mins);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_Overflow() {
        DateTimeZone zonePlus = DateTimeZone.forOffsetHours(2);
        try {
            zonePlus.convertUTCToLocal(Long.MAX_VALUE - 100);
            fail("Expected ArithmeticException on overflow");
        } catch (ArithmeticException expected) {
            // Success
        }

        DateTimeZone zoneMinus = DateTimeZone.forOffsetHours(-2);
        try {
            zoneMinus.convertUTCToLocal(Long.MIN_VALUE + 100);
            fail("Expected ArithmeticException on underflow");
        } catch (ArithmeticException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Overflow() {
        DateTimeZone zoneMinus = DateTimeZone.forOffsetHours(-2);
        try {
            zoneMinus.convertLocalToUTC(Long.MAX_VALUE - 100, false);
            fail("Expected ArithmeticException on overflow");
        } catch (ArithmeticException expected) {
            // Success
        }

        DateTimeZone zonePlus = DateTimeZone.forOffsetHours(2);
        try {
            zonePlus.convertLocalToUTC(Long.MIN_VALUE + 100, false);
            fail("Expected ArithmeticException on underflow");
        } catch (ArithmeticException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_DSTGap_StrictVsNonStrict() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        // Gap on 2011-03-27: 01:30 did not exist
        long gapLocalMillis = new DateTime(2011, 3, 27, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();

        try {
            zoneLondon.convertLocalToUTC(gapLocalMillis, true);
            fail("Strict conversion should throw IllegalArgumentException for DST gap");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Illegal instant due to time zone offset transition"));
        }

        // Non-strict should resolve without throwing
        long resolvedUTC = zoneLondon.convertLocalToUTC(gapLocalMillis, false);
        assertTrue(resolvedUTC > 0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_NullZone() {
        DateTimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_InvalidID() {
        DateTimeZone.forID("Invalid/Non_Existent_Timezone_ID");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_InvalidSignOnly() {
        DateTimeZone.forID("+");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(5, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(5, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_OverflowHours() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_UnderflowHours() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MIN_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_Unrecognized() {
        DateTimeZone.forTimeZone(new SimpleTimeZone(0, "UNKNOWN_TZ_ID"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_NullIDs() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_EmptyIDs() {
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
    public void testSetProvider_MissingUTC() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("America/New_York");
                return set;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_InvalidUTCZone() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return DateTimeZone.forOffsetHours(1);
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("UTC");
                return set;
            }
        });
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEquals_HashCode_ToString() {
        DateTimeZone zone1 = DateTimeZone.forID("+02:00");
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(2);
        DateTimeZone zone3 = DateTimeZone.forID("+03:00");

        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertNotEquals(zone1, zone3);
        assertNotEquals(zone1, null);
        assertNotEquals(zone1, "Different Type");

        assertEquals("+02:00", zone1.toString());
        assertEquals("UTC", DateTimeZone.UTC.toString());
        assertEquals("Europe/London", DateTimeZone.forID("Europe/London").toString());
    }

    @Test(timeout = 4000)
    public void testToTimeZone() {
        DateTimeZone dtz = DateTimeZone.forID("Europe/Paris");
        TimeZone tz = dtz.toTimeZone();
        assertNotNull(tz);
        assertEquals("Europe/Paris", tz.getID());
    }

    @Test(timeout = 4000)
    public void testSerialization_Stub() throws Exception {
        DateTimeZone london = DateTimeZone.forID("Europe/London");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(london);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame("Deserialized zone should resolve to cached instance", london, deserialized);
    }

    @Test(timeout = 4000)
    public void testCustomProvider_and_NameProvider_Pluggability() {
        Provider defaultProv = DateTimeZone.getProvider();
        assertNotNull(defaultProv);

        DateTimeZone.setProvider(new UTCProvider());
        assertEquals(1, DateTimeZone.getAvailableIDs().size());
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));

        // Reset provider
        DateTimeZone.setProvider(null);
        assertTrue(DateTimeZone.getAvailableIDs().size() > 1);

        NameProvider defaultNameProv = DateTimeZone.getNameProvider();
        assertNotNull(defaultNameProv);

        NameProvider customNameProv = new DefaultNameProvider();
        DateTimeZone.setNameProvider(customNameProv);
        assertSame(customNameProv, DateTimeZone.getNameProvider());

        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
    }
}