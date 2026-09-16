package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.TimeZone;
import java.util.Set;
import java.util.Locale;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partitions and branches targeted:
 * 
 * A. Core Functional Logic & State Transitions:
 *    - forID(): null, "UTC", valid IDs, offset strings (+/-hh:mm), invalid IDs
 *    - forOffsetHours(): zero, positive, negative, boundary values
 *    - forOffsetHoursMinutes(): zero/zero, hours/minutes boundaries, overflow
 *    - forOffsetMillis(): various millis values, zero
 *    - forTimeZone(): null, UTC, old short IDs, GMT+/- offsets, invalid
 *    - getDefault(): normal operation, fallback logic
 *    - setDefault(): normal, null (throws), security check
 *    - fixedOffsetZone(): id mapping, cache hit/miss, offset==0 returns UTC
 *    - getConvertedId(): known mappings, GMT->UTC, null for unknown
 *    - parseOffset(): positive, negative, zero offsets
 *    - printOffset(): various positive/negative offsets with seconds/millis
 * 
 * B. Boundary Value Analysis (BVA) & Extremes:
 *    - forOffsetHoursMinutes: hoursOffset = Integer.MAX_VALUE, MIN_VALUE
 *    - minutesOffset = 0, 59, -1, 60
 *    - forOffsetMillis: Integer.MAX_VALUE, MIN_VALUE
 *    - getOffsetFromLocal: around DST transitions (spring forward, fall back)
 *    - convertUTCToLocal: overflow detection (sign change logic)
 *    - convertLocalToUTC: strict mode, DST gap/overlap handling
 *    - adjustOffset: before/after DST transitions
 *    - isLocalDateTimeGap: fixed zone (false), gap zone (true), non-gap (false)
 *    - getShortName/getName: null locale, nameKey null, name from provider, fallback
 * 
 * C. Defect-Targeted Branch Zone (Targeting known DST cutover bug):
 *    The known defect occurs during Europe/London autumn DST transition:
 *    - At 2011-10-30T01:15:00, the offset should be +01:00 (BST),
 *      but the defective version returns Z (UTC/GMT).
 *    - This involves getOffsetFromLocal and convertLocalToUTC logic
 *      where negative offset differences are mishandled.
 *    - Branches: (offsetLocal - offsetAdjusted) < 0, nextTransition comparison
 *    - The bug manifests when getOffsetFromLocal is called with instantLocal
 *      inside the overlapping period, incorrectly returning the winter offset.
 * 
 * D. Exception & Defensive Guard Paths:
 *    - forID: invalid id -> IllegalArgumentException
 *    - forOffsetHoursMinutes: minutes out of range -> IllegalArgumentException
 *    - forOffsetHoursMinutes: arithmetic overflow -> IllegalArgumentException
 *    - setDefault: null -> IllegalArgumentException
 *    - convertUTCToLocal: overflow -> ArithmeticException
 *    - convertLocalToUTC: strict DST gap -> IllegalArgumentException
 *    - constructor: null id -> IllegalArgumentException
 *    - forTimeZone: invalid zone -> IllegalArgumentException
 * 
 * E. Object Lifecycle & Contract Integrity:
 *    - equals: not tested directly (abstract), but hashCode consistency
 *    - toString returns getID()
 *    - toTimeZone returns TimeZone with matching ID
 *    - Stub serialization round-trip (via writeReplace/readResolve)
 */
public class DateTimeZoneDeepseekTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testForID_nullReturnsDefault() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        DateTimeZone result = DateTimeZone.forID(null);
        assertNotNull(result);
        assertEquals(defaultZone, result);
    }

    @Test(timeout = 4000)
    public void testForID_utc() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertNotNull(zone);
        assertSame(DateTimeZone.UTC, zone);
        assertEquals("UTC", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForID_validZone() {
        // Use a well-known zone that should exist
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForID_positiveOffset() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertNotNull(zone);
        // The offset +05:30 = 19800000 millis
        assertEquals(19800000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_negativeOffset() {
        DateTimeZone zone = DateTimeZone.forID("-08:00");
        assertNotNull(zone);
        assertEquals(-28800000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_zeroOffsetString() {
        DateTimeZone zone = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zone);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForID_invalidId() {
        DateTimeZone.forID("InvalidZoneID");
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_zero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(7200000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        assertEquals(-18000000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_zeroZero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_positivePositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 30);
        assertEquals(9000000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_negativePositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals(-9000000, zone.getOffset(0L));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_hoursOverflow() {
        // This should cause arithmetic overflow when multiplying hours by 60
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_zero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_positive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals(3600000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-7200000);
        assertEquals(-7200000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_null() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        DateTimeZone result = DateTimeZone.forTimeZone(null);
        assertNotNull(result);
        assertEquals(defaultZone, result);
    }

    @Test(timeout = 4000)
    public void testForTimeZone_utc() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(tz));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_gmtOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertNotNull(zone);
        assertEquals(19800000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_oldId() {
        // EST should be converted to America/New_York
        TimeZone tz = TimeZone.getTimeZone("EST");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertNotNull(zone);
        // America/New_York is not fixed, but offset at epoch should be -18000000 (EST)
        assertEquals(-18000000, zone.getOffset(0L));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForTimeZone_invalid() {
        TimeZone tz = new java.util.SimpleTimeZone(0, "BOGUS");
        DateTimeZone.forTimeZone(tz);
    }

    @Test(timeout = 4000)
    public void testGetDefault_notNull() {
        assertNotNull(DateTimeZone.getDefault());
    }

    @Test(timeout = 4000)
    public void testSetDefault_normal() {
        DateTimeZone original = DateTimeZone.getDefault();
        try {
            DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
            DateTimeZone.setDefault(zone);
            assertSame(zone, DateTimeZone.getDefault());
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetDefault_null() {
        DateTimeZone.setDefault(null);
    }

    @Test(timeout = 4000)
    public void testGetAvailableIDs_notEmpty() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.size() > 0);
        assertTrue(ids.contains("UTC"));
    }

    @Test(timeout = 4000)
    public void testGetProvider_notNull() {
        assertNotNull(DateTimeZone.getProvider());
    }

    @Test(timeout = 4000)
    public void testGetNameProvider_notNull() {
        assertNotNull(DateTimeZone.getNameProvider());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_boundaryMinute59() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 59);
        assertEquals(3540000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_maxValue() {
        // Large positive offset should still produce a valid string
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone);
        // Offset string should be something like +596:31:23.647
        assertTrue(zone.getID().startsWith("+"));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_minValue() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone);
        assertTrue(zone.getID().startsWith("-"));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_positiveOffsetZone() {
        // Using a fixed positive offset zone for simple behavior
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        // instantLocal is local time, offset should be 7200000
        int offset = zone.getOffsetFromLocal(1000000L);
        assertEquals(7200000, offset);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_negativeOffsetZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        int offset = zone.getOffsetFromLocal(1000000L);
        assertEquals(-18000000, offset);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_dstSpringForward() {
        // Europe/London spring forward: 2011-03-27T01:00:00Z -> 02:00 BST
        // At local time 01:30 (which doesn't exist in wall clock),
        // getOffsetFromLocal should give BST offset +0100 (+3600000)
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // 2011-03-27T00:30:00Z = 1301185800000L (actually let's compute)
        // The gap starts at 2011-03-27T01:00:00Z UTC
        long gapStartUtc = 1301187600000L; // 2011-03-27T01:00:00Z
        // Local time 01:30:00 during gap => instantLocal = gapStartUtc + offset + 30*60000
        int offsetBefore = london.getOffset(gapStartUtc - 60000); // should be 0
        long instantLocal = gapStartUtc + offsetBefore + 1800000L; // 01:30 local
        int offset = london.getOffsetFromLocal(instantLocal);
        // Should return BST offset (+3600000) not GMT (0)
        assertEquals(3600000, offset);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_dstFallBack() {
        // Europe/London fall back: 2011-10-30T02:00:00 BST -> 01:00 GMT
        // At local time 01:15 (which occurs twice), getOffsetFromLocal should
        // return the earlier (BST) offset according to the spec.
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // 2011-10-30T01:00:00Z UTC is the transition from BST to GMT
        // Actually, BST ends at 02:00 BST which is 01:00 UTC
        long transitionUtc = 1319936400000L; // 2011-10-30T01:00:00Z
        // Local time 01:15 during overlap
        long instantLocal = transitionUtc + 3600000 + 900000L; // 01:15 local (using BST offset for calculation)
        int offset = london.getOffsetFromLocal(instantLocal);
        // Should return BST offset (+3600000) according to spec
        assertEquals(3600000, offset);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_normal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        long utc = 1000000L;
        long local = zone.convertUTCToLocal(utc);
        assertEquals(utc + 3600000, local);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        // When adding offset causes sign change with same sign original
        long max = Long.MAX_VALUE;
        zone.convertUTCToLocal(max);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_normal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        long local = 1000000L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local - 3600000, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_strictGap() {
        // During a DST gap, strict mode should throw
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // 2011-03-27T01:30:00 local (during gap) should throw
        long gapLocal = 1301187600000L + 1800000L; // local time 01:30 (gap)
        try {
            london.convertLocalToUTC(gapLocal, true);
            fail("Expected IllegalArgumentException for DST gap in strict mode");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_nonStrictGap() {
        // Non-strict mode should handle the gap gracefully
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Use a time during spring forward gap
        long gapLocal = 1301187600000L + 1800000L; // 01:30 local
        long utc = london.convertLocalToUTC(gapLocal, false);
        // Should not throw, should return a reasonable value
        assertTrue(utc > 0);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_withOriginalInstant() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Use an instant where offset is known
        long originalUtc = 1301184000000L; // Some time in winter (GMT)
        long local = originalUtc + london.getOffset(originalUtc);
        long result = london.convertLocalToUTC(local, false, originalUtc);
        assertEquals(originalUtc, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_sameZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long instant = 1000000L;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_differentZone() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(0);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(1);
        long instant = 1000000L;
        long result = zone1.getMillisKeepLocal(zone2, instant);
        // Local time in zone2 should be same as zone1
        // zone1 local = instant + 0 = 1000000
        // zone2 local should also be 1000000 => result = 1000000 - 3600000 = -2600000
        assertEquals(instant - 3600000, result);
    }

    @Test(timeout = 4000)
    public void testAdjustOffset_noTransition() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        long instant = 1000000L;
        assertEquals(instant, zone.adjustOffset(instant, true));
        assertEquals(instant, zone.adjustOffset(instant, false));
    }

    @Test(timeout = 4000)
    public void testAdjustOffset_dstOverlap() {
        // This is a rough test - adjustOffset should handle overlaps
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Around autumn transition
        long transition = 1319936400000L; // 2011-10-30T01:00:00Z
        long result = london.adjustOffset(transition, true);
        // Should not throw, result should be reasonable
        assertTrue(result >= 0);
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_fixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        assertFalse(zone.isLocalDateTimeGap(new LocalDateTime(2011, 3, 27, 1, 30)));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_dstGap() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // 2011-03-27T01:30:00 is in the gap
        assertTrue(london.isLocalDateTimeGap(new LocalDateTime(2011, 3, 27, 1, 30)));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_noGap() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // 2011-03-27T03:30:00 is after gap
        assertFalse(london.isLocalDateTimeGap(new LocalDateTime(2011, 3, 27, 3, 30)));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets the known defect: Europe/London autumn DST transition.
     * The bug causes getOffsetFromLocal to return 0 (GMT) instead of 3600000 (BST)
     * for local times during the overlap period.
     * 
     * The specific failing test case from Defects4J:
     * expected:<...1-10-30T01:15:00.000[+01:00]> but was:<...1-10-30T01:15:00.000[Z]>
     * 
     * This test directly replicates the scenario: create a DateTime for
     * 2011-10-30T01:15:00 in London and verify the offset is +01:00 (BST).
     */
    @Test(timeout = 4000)
    public void testDateTimeCreation_london_cutover() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Create DateTime for 2011-10-30T01:15:00.000 in London
        DateTime dt = new DateTime(2011, 10, 30, 1, 15, 0, 0, london);
        // The offset should be +01:00 (BST = 3600000 ms) during the overlap
        assertEquals("Offset should be BST (+01:00)", 3600000, dt.getZone().getOffset(dt.getMillis()));
        // Also verify the string representation matches expected
        // The expected output is "2011-10-30T01:15:00.000+01:00"
        String expected = "2011-10-30T01:15:00.000+01:00";
        String actual = dt.toString();
        assertEquals("DateTime string representation mismatch", expected, actual);
    }

    /**
     * Additional targeted test: directly test getOffsetFromLocal for the
     * failing scenario to isolate the bug in that method.
     */
    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_londonCutover() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Transition happens at 2011-10-30T02:00:00 BST which is 2011-10-30T01:00:00 UTC
        // During overlap (01:00-02:00 BST), local time 01:15 exists twice
        // Get the UTC instant for 01:00 UTC on that day
        long transitionUtc = 1319936400000L; // 2011-10-30T01:00:00Z
        // Local time 01:15 using BST offset first (for calculation)
        long localInstant = transitionUtc + 3600000 + 900000L; // 01:15 BST local
        int offset = london.getOffsetFromLocal(localInstant);
        // The correct behavior is to return the earlier (summer/BST) offset = 3600000
        assertEquals("getOffsetFromLocal should return BST offset during overlap", 
                     3600000, offset);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetShortName_nullLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getShortName(0L, null);
        assertNotNull(name);
        // Should use default locale
    }

    @Test(timeout = 4000)
    public void testGetShortName_withLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getShortName(0L, Locale.UK);
        // At epoch, London was in BST (GMT+1), short name may be "BST"
        assertNotNull(name);
    }

    @Test(timeout = 4000)
    public void testGetName_nullLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getName(0L, null);
        assertNotNull(name);
    }

    @Test(timeout = 4000)
    public void testGetName_withLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getName(0L, Locale.UK);
        assertNotNull(name);
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        assertTrue(zone.isStandardOffset(0L));
        // For a fixed zone, all offsets are standard
    }

    @Test(timeout = 4000)
    public void testToString_returnsId() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", zone.toString());
    }

    @Test(timeout = 4000)
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        TimeZone tz = zone.toTimeZone();
        assertEquals("Europe/London", tz.getID());
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        DateTimeZone original = DateTimeZone.forID("Europe/London");
        // Write replacement should produce a Stub with the ID
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(original.getID(), deserialized.getID());
        assertEquals(original.getOffset(0L), deserialized.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testSerializationUsesForID() throws Exception {
        // The serialization mechanism should use forID, which handles short IDs
        // This tests the Stub class behavior
        DateTimeZone original = DateTimeZone.forID("Europe/London");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        // Should be the same instance as forID would return (or another instance
        // with the same behavior)
        assertEquals(original.getID(), deserialized.getID());
    }
}