package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.TimeZone;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: DateTimeZone (abstract, with static factories and instance methods)
 * 
 * Defect Focus (from Defects4J ground truth):
 * - Moscow Autumn DST cutover: When converting local time to UTC during the autumn overlap
 *   (e.g., 2013-10-27T02:30 local, when clocks go back from +04:00 to +03:00),
 *   the offset selection is incorrect. Expected: +04:00 (earlier offset) for the overlap,
 *   but defective code returns +03:00 (later offset).
 *   Affected methods: convertLocalToUTC, getOffsetFromLocal.
 * 
 * Branches/conditions targeted:
 * 1. convertLocalToUTC(long, boolean) - strict vs non-strict, DST gap/overlap handling
 * 2. getOffsetFromLocal(long, int, int, int) - offset selection during overlap
 * 3. forID / forOffsetMillis / fixedOffsetZone - ID parsing, cache behavior
 * 4. getAvailableIDs - set immutability and content
 * 5. equals/hashCode - contract integrity
 * 6. getMillisKeepLocal - cross-zone conversion
 * 7. Boundary values: Long.MIN/MAX, zero offsets, negative offsets
 * 8. Exception paths: null IDs, invalid offsets, security manager checks
 * 
 * Test partitions:
 * A. Core functional: forID, forOffsetMillis, getOffset, getName, getShortName
 * B. Boundary: Long.MIN/MAX, zero/negative offsets, null locale
 * C. Defect-targeted: Moscow autumn overlap (both strict and non-strict)
 * D. Exception: invalid IDs, null arguments, overflow in conversions
 * E. Contract: equals/hashCode, immutability of available IDs set
 */

public class DateTimeZoneDeepseekTest {

    // ========== PARTITION A: CORE FUNCTIONAL LOGIC ==========

    @Test(timeout = 4000)
    public void testForID_UTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertNotNull(zone);
        assertEquals("UTC", zone.getID());
        assertEquals(0, zone.getOffset(0L));
        assertSame(DateTimeZone.UTC, zone);
    }

    @Test(timeout = 4000)
    public void testForID_FixedOffset() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertNotNull(zone);
        assertEquals("+05:30", zone.getID());
        assertEquals(5 * 3600000L + 30 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_NegativeFixedOffset() {
        DateTimeZone zone = DateTimeZone.forID("-08:00");
        assertNotNull(zone);
        assertEquals(-8 * 3600000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(0);
        assertNotNull(zone);
        assertEquals(0, zone.getOffset(0L));
        assertEquals("UTC", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertNotNull(zone);
        assertEquals(3600000L, zone.getOffset(0L));
        assertEquals("+01:00", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-7200000L);
        assertNotNull(zone);
        assertEquals(-7200000L, zone.getOffset(0L));
        assertEquals("-02:00", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Valid() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertNotNull(zone);
        assertEquals(5 * 3600000L + 30 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_NegativeHours() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-5, 30);
        assertNotNull(zone);
        assertEquals(-5 * 3600000L + 30 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffset_UTC() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertEquals(0, zone.getOffset(0L));
        assertEquals(0, zone.getOffset(Long.MAX_VALUE));
        assertEquals(0, zone.getOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetOffset_FixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertEquals(3600000L, zone.getOffset(0L));
        assertEquals(3600000L, zone.getOffset(123456789L));
    }

    @Test(timeout = 4000)
    public void testGetName_DefaultLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetName_NullLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getName(0L, null);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetShortName_DefaultLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getShortName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetShortName_NullLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getShortName(0L, null);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        TimeZone tz = zone.toTimeZone();
        assertNotNull(tz);
        assertEquals("Europe/London", tz.getID());
    }

    @Test(timeout = 4000)
    public void testGetAvailableIDs_NotEmpty() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertFalse(ids.isEmpty());
        assertTrue(ids.contains("UTC"));
    }

    @Test(timeout = 4000)
    public void testGetAvailableIDs_Unmodifiable() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        try {
            ids.add("INVALID");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ========== PARTITION B: BOUNDARY VALUE ANALYSIS ==========

    @Test(timeout = 4000)
    public void testForOffsetMillis_MaxValue() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone);
        assertEquals(Integer.MAX_VALUE, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_MinValue() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone);
        assertEquals(Integer.MIN_VALUE, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_MaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(23, 59);
        assertNotNull(zone);
        assertEquals(23 * 3600000L + 59 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_MinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-23, -59);
        assertNotNull(zone);
        assertEquals(-23 * 3600000L - 59 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffset_LongMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertEquals(3600000L, zone.getOffset(Long.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetOffset_LongMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertEquals(3600000L, zone.getOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_Zero() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertEquals(0L, zone.convertUTCToLocal(0L));
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Zero() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertEquals(0L, zone.convertLocalToUTC(0L, false));
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_Zero() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertEquals(0L, zone.convertLocalToUTC(0L, true));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_SameZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long instant = 123456789L;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_DifferentZone() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        long instant = 123456789L;
        long result = zone1.getMillisKeepLocal(zone2, instant);
        assertNotEquals(instant, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NullZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long instant = 123456789L;
        long result = zone.getMillisKeepLocal(null, instant);
        assertEquals(instant, result);
    }

    // ========== PARTITION C: DEFECT-TARGETED BRANCH ZONE ==========

    /**
     * CRITICAL DEFECT TEST: Moscow Autumn DST overlap.
     * 
     * On 2013-10-27, Moscow moved from +04:00 to +03:00 (permanent winter time).
     * At 02:30 local time, the clock goes back from 02:00 to 01:00, so 02:30 occurs twice.
     * The correct behavior for convertLocalToUTC during overlap is to use the EARLIER offset (+04:00).
     * The defective version incorrectly uses the LATER offset (+03:00).
     * 
     * This test directly targets the known failure from Defects4J.
     */
    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Moscow_Autumn_Overlap() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Moscow");
        // 2013-10-27T02:30:00 local time
        long localMillis = 1382841000000L; // 2013-10-27T00:30:00Z (UTC)
        // Expected: +04:00 offset (earlier offset during overlap)
        long expectedUTC = localMillis - 4 * 3600000L;
        
        long actualUTC = zone.convertLocalToUTC(localMillis, false);
        assertEquals("Moscow autumn overlap should use earlier offset (+04:00)", 
                expectedUTC, actualUTC);
    }

    /**
     * CRITICAL DEFECT TEST: Moscow Autumn overlap with strict mode.
     * Strict mode should also use the earlier offset during overlap.
     */
    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Moscow_Autumn_Overlap_Strict() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Moscow");
        long localMillis = 1382841000000L; // 2013-10-27T02:30:00 local
        long expectedUTC = localMillis - 4 * 3600000L;
        
        long actualUTC = zone.convertLocalToUTC(localMillis, true);
        assertEquals("Moscow autumn overlap (strict) should use earlier offset (+04:00)", 
                expectedUTC, actualUTC);
    }

    /**
     * CRITICAL DEFECT TEST: getOffsetFromLocal for Moscow Autumn overlap.
     * The offset at 02:30 local during the overlap should be +04:00 (earlier offset).
     */
    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_Moscow_Autumn_Overlap() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Moscow");
        long localMillis = 1382841000000L; // 2013-10-27T02:30:00 local
        
        int offset = zone.getOffsetFromLocal(localMillis);
        assertEquals("Moscow autumn overlap offset should be +04:00", 
                4 * 3600000, offset);
    }

    /**
     * CRITICAL DEFECT TEST: Direct offset check at the overlap boundary.
     * At 2013-10-27T02:30:00 local, the offset should be +04:00.
     */
    @Test(timeout = 4000)
    public void testGetOffset_Moscow_Autumn_Overlap() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Moscow");
        // 2013-10-27T02:30:00 local = 2013-10-26T22:30:00Z (with +04:00)
        long instantUTC = 1382826600000L; // 2013-10-26T22:30:00Z
        int offset = zone.getOffset(instantUTC);
        assertEquals("Offset at Moscow autumn overlap should be +04:00", 
                4 * 3600000, offset);
    }

    /**
     * CRITICAL DEFECT TEST: Verify the local time mapping.
     * The local time 02:30 on 2013-10-27 should map to 22:30 UTC on 2013-10-26.
     */
    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Moscow_Autumn_ExactInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Moscow");
        long localMillis = 1382841000000L; // 2013-10-27T02:30:00 local
        long expectedUTC = 1382826600000L; // 2013-10-26T22:30:00Z
        
        long actualUTC = zone.convertLocalToUTC(localMillis, false);
        assertEquals("Moscow autumn local time should map to 22:30 UTC previous day", 
                expectedUTC, actualUTC);
    }

    // ========== PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_Null() {
        DateTimeZone.forID(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForID_Invalid() {
        DateTimeZone.forID("Invalid/Zone");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesOutOfRange() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_NegativeMinutesOutOfRange() {
        DateTimeZone.forOffsetHoursMinutes(1, -60);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForOffsetHoursMinutes_Overflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testForTimeZone_Null() {
        DateTimeZone.forTimeZone(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefault_Null() {
        DateTimeZone.setDefault(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetProvider_Null() {
        DateTimeZone.setProvider(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNameProvider_Null() {
        DateTimeZone.setNameProvider(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructor_NullId() {
        new DateTimeZone(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getNameKey(long instant) {
                return null;
            }

            @Override
            public int getOffset(long instant) {
                return 0;
            }

            @Override
            public int getStandardOffset(long instant) {
                return 0;
            }

            @Override
            public boolean isFixed() {
                return true;
            }

            @Override
            public long nextTransition(long instant) {
                return instant;
            }

            @Override
            public long previousTransition(long instant) {
                return instant;
            }

            @Override
            public boolean equals(Object object) {
                return false;
            }
        };
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        try {
            zone.convertLocalToUTC(Long.MAX_VALUE, false);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_Overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        try {
            zone.convertLocalToUTC(Long.MAX_VALUE, true);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ========== PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY ==========

    @Test(timeout = 4000)
    public void testEquals_SameObject() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals(zone, zone);
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentZones() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        assertNotEquals(zone1, zone2);
    }

    @Test(timeout = 4000)
    public void testEquals_Null() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotEquals(null, zone);
    }

    @Test(timeout = 4000)
    public void testHashCode_Consistent() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        int hash1 = zone.hashCode();
        int hash2 = zone.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCode_EqualsContract() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone.toString());
        assertFalse(zone.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetID_NotNull() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone.getID());
        assertEquals("Europe/London", zone.getID());
    }

    @Test(timeout = 4000)
    public void testIsFixed_UTC() {
        assertTrue(DateTimeZone.UTC.isFixed());
    }

    @Test(timeout = 4000)
    public void testIsFixed_FixedOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertTrue(zone.isFixed());
    }

    @Test(timeout = 4000)
    public void testIsFixed_NonFixed() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertFalse(zone.isFixed());
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_UTC() {
        assertEquals(0, DateTimeZone.UTC.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_Fixed() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertEquals(3600000L, zone.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testNextTransition_UTC() {
        long instant = 123456789L;
        assertEquals(instant, DateTimeZone.UTC.nextTransition(instant));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_UTC() {
        long instant = 123456789L;
        assertEquals(instant, DateTimeZone.UTC.previousTransition(instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_UTCToLondon() {
        DateTimeZone utc = DateTimeZone.UTC;
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long instant = 123456789L;
        long result = utc.getMillisKeepLocal(london, instant);
        assertNotEquals(instant, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_LondonToUTC() {
        DateTimeZone utc = DateTimeZone.UTC;
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long instant = 123456789L;
        long result = london.getMillisKeepLocal(utc, instant);
        assertNotEquals(instant, result);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_UTC() {
        assertEquals(0, DateTimeZone.UTC.getOffsetFromLocal(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_Fixed() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertEquals(3600000L, zone.getOffsetFromLocal(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_NonFixed() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long local = 123456789L;
        int offset = zone.getOffsetFromLocal(local);
        assertTrue(offset >= -12 * 3600000 && offset <= 14 * 3600000);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonZero() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        long instant = 123456789L;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(instant + 3600000L, local);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonZero() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        long local = 123456789L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local - 3600000L, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_NonZero() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        long local = 123456789L;
        long utc = zone.convertLocalToUTC(local, true);
        assertEquals(local - 3600000L, utc);
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonFixed() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        int offset = zone.getOffset(0L);
        assertTrue(offset >= -12 * 3600000 && offset <= 14 * 3600000);
    }

    @Test(timeout = 4000)
    public void testGetNameKey_NotNull() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testGetNameKey_UTC() {
        assertEquals("UTC", DateTimeZone.UTC.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testGetNameKey_Fixed() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000L);
        assertNotNull(zone.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testGetShortName_Locale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getShortName(0L, Locale.UK);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetName_Locale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getName(0L, Locale.UK);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testForTimeZone_UTC() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertNotNull(zone);
        assertEquals("UTC", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForTimeZone_Default() {
        TimeZone tz = TimeZone.getDefault();
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertNotNull(zone);
    }

    @Test(timeout = 4000)
    public void testGetDefault_NotNull() {
        assertNotNull(DateTimeZone.getDefault());
    }

    @Test(timeout = 4000)
    public void testSetDefault_Valid() {
        DateTimeZone original = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.UTC);
            assertEquals(DateTimeZone.UTC, DateTimeZone.getDefault());
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void testGetProvider_NotNull() {
        assertNotNull(DateTimeZone.getProvider());
    }

    @Test(timeout = 4000)
    public void testGetNameProvider_NotNull() {
        assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test(timeout = 4000)
    public void testGetAvailableIDs_ContainsKnown() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertTrue(ids.contains("Europe/London"));
        assertTrue(ids.contains("Europe/Moscow"));
        assertTrue(ids.contains("America/New_York"));
    }

    @Test(timeout = 4000)
    public void testForID_Moscow() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Moscow");
        assertNotNull(zone);
        assertEquals("Europe/Moscow", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForID_London() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForID_NewYork() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertNotNull(zone);
        assertEquals("America/New_York", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(0);
        assertNotNull(zone);
        assertEquals(0, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertNotNull(zone);
        assertEquals(2 * 3600000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHours_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-3);
        assertNotNull(zone);
        assertEquals(-3 * 3600000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertNotNull(zone);
        assertEquals(0, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(1, 30);
        assertNotNull(zone);
        assertEquals(1 * 3600000L + 30 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-1, -30);
        assertNotNull(zone);
        assertEquals(-1 * 3600000L - 30 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertNotNull(zone);
        assertEquals(90 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_NegativeNonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-90 * 60000L);
        assertNotNull(zone);
        assertEquals(-90 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(90 * 60000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(90 * 60000L, zone.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testIsFixed_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertTrue(zone.isFixed());
    }

    @Test(timeout = 4000)
    public void testGetNameKey_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertNotNull(zone.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testGetShortName_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        String name = zone.getShortName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetName_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        String name = zone.getName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = 123456789L;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(instant + 90 * 60000L, local);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long local = 123456789L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local - 90 * 60000L, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long local = 123456789L;
        long utc = zone.convertLocalToUTC(local, true);
        assertEquals(local - 90 * 60000L, utc);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(90 * 60000L, zone.getOffsetFromLocal(0L));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = 123456789L;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourToUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = 123456789L;
        long result = zone.getMillisKeepLocal(DateTimeZone.UTC, instant);
        assertEquals(instant - 90 * 60000L, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_UTCToNonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = 123456789L;
        long result = DateTimeZone.UTC.getMillisKeepLocal(zone, instant);
        assertEquals(instant + 90 * 60000L, result);
    }

    @Test(timeout = 4000)
    public void testNextTransition_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = 123456789L;
        assertEquals(instant, zone.nextTransition(instant));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = 123456789L;
        assertEquals(instant, zone.previousTransition(instant));
    }

    @Test(timeout = 4000)
    public void testEquals_NonHour() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(90 * 60000L);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(zone1, zone2);
    }

    @Test(timeout = 4000)
    public void testHashCode_NonHour() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(90 * 60000L);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertNotNull(zone.toString());
        assertFalse(zone.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetID_NonHour() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertNotNull(zone.getID());
        assertFalse(zone.getID().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(90 * 60000L, zone.getOffset(Long.MAX_VALUE));
        assertEquals(90 * 60000L, zone.getOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(90 * 60000L, zone.getStandardOffset(Long.MAX_VALUE));
        assertEquals(90 * 60000L, zone.getStandardOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = Long.MAX_VALUE - 90 * 60000L;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(instant + 90 * 60000L, local);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long local = Long.MIN_VALUE + 90 * 60000L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local - 90 * 60000L, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long local = Long.MIN_VALUE + 90 * 60000L;
        long utc = zone.convertLocalToUTC(local, true);
        assertEquals(local - 90 * 60000L, utc);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(90 * 60000L, zone.getOffsetFromLocal(Long.MAX_VALUE));
        assertEquals(90 * 60000L, zone.getOffsetFromLocal(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = Long.MAX_VALUE - 90 * 60000L;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourToUTCBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = Long.MAX_VALUE - 90 * 60000L;
        long result = zone.getMillisKeepLocal(DateTimeZone.UTC, instant);
        assertEquals(instant - 90 * 60000L, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_UTCToNonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        long instant = Long.MIN_VALUE + 90 * 60000L;
        long result = DateTimeZone.UTC.getMillisKeepLocal(zone, instant);
        assertEquals(instant + 90 * 60000L, result);
    }

    @Test(timeout = 4000)
    public void testNextTransition_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(Long.MAX_VALUE, zone.nextTransition(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, zone.nextTransition(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_NonHourBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(90 * 60000L);
        assertEquals(Long.MAX_VALUE, zone.previousTransition(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, zone.previousTransition(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = 0L;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(Integer.MAX_VALUE, local);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = 0L;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(Integer.MIN_VALUE, local);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long local = 0L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(-Integer.MAX_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long local = 0L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(-Integer.MIN_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long local = 0L;
        long utc = zone.convertLocalToUTC(local, true);
        assertEquals(-Integer.MAX_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long local = 0L;
        long utc = zone.convertLocalToUTC(local, true);
        assertEquals(-Integer.MIN_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getOffsetFromLocal(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getOffsetFromLocal(0L));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = 0L;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = 0L;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourToUTCMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = 0L;
        long result = zone.getMillisKeepLocal(DateTimeZone.UTC, instant);
        assertEquals(-Integer.MAX_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourToUTCMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = 0L;
        long result = zone.getMillisKeepLocal(DateTimeZone.UTC, instant);
        assertEquals(-Integer.MIN_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_UTCToNonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = 0L;
        long result = DateTimeZone.UTC.getMillisKeepLocal(zone, instant);
        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_UTCToNonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = 0L;
        long result = DateTimeZone.UTC.getMillisKeepLocal(zone, instant);
        assertEquals(Integer.MIN_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testNextTransition_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(0L, zone.nextTransition(0L));
    }

    @Test(timeout = 4000)
    public void testNextTransition_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(0L, zone.nextTransition(0L));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(0L, zone.previousTransition(0L));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(0L, zone.previousTransition(0L));
    }

    @Test(timeout = 4000)
    public void testEquals_NonHourMax() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(zone1, zone2);
    }

    @Test(timeout = 4000)
    public void testEquals_NonHourMin() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(zone1, zone2);
    }

    @Test(timeout = 4000)
    public void testHashCode_NonHourMax() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCode_NonHourMin() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone.toString());
        assertFalse(zone.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testToString_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone.toString());
        assertFalse(zone.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetID_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone.getID());
        assertFalse(zone.getID().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetID_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone.getID());
        assertFalse(zone.getID().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetName_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        String name = zone.getName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetName_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        String name = zone.getName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetShortName_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        String name = zone.getShortName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetShortName_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        String name = zone.getShortName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetNameKey_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testGetNameKey_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testIsFixed_NonHourMax() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertTrue(zone.isFixed());
    }

    @Test(timeout = 4000)
    public void testIsFixed_NonHourMin() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertTrue(zone.isFixed());
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getOffset(Long.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, zone.getOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getOffset(Long.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, zone.getOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getStandardOffset(Long.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, zone.getStandardOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getStandardOffset(Long.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, zone.getStandardOffset(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = Long.MAX_VALUE - Integer.MAX_VALUE;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(instant + Integer.MAX_VALUE, local);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = Long.MIN_VALUE - Integer.MIN_VALUE;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(instant + Integer.MIN_VALUE, local);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long local = Long.MIN_VALUE + Integer.MAX_VALUE;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local - Integer.MAX_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long local = Long.MAX_VALUE + Integer.MIN_VALUE;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local - Integer.MIN_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long local = Long.MIN_VALUE + Integer.MAX_VALUE;
        long utc = zone.convertLocalToUTC(local, true);
        assertEquals(local - Integer.MAX_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Strict_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long local = Long.MAX_VALUE + Integer.MIN_VALUE;
        long utc = zone.convertLocalToUTC(local, true);
        assertEquals(local - Integer.MIN_VALUE, utc);
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getOffsetFromLocal(Long.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, zone.getOffsetFromLocal(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getOffsetFromLocal(Long.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, zone.getOffsetFromLocal(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = Long.MAX_VALUE - Integer.MAX_VALUE;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = Long.MIN_VALUE - Integer.MIN_VALUE;
        assertEquals(instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourToUTCMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = Long.MAX_VALUE - Integer.MAX_VALUE;
        long result = zone.getMillisKeepLocal(DateTimeZone.UTC, instant);
        assertEquals(instant - Integer.MAX_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_NonHourToUTCMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = Long.MIN_VALUE - Integer.MIN_VALUE;
        long result = zone.getMillisKeepLocal(DateTimeZone.UTC, instant);
        assertEquals(instant - Integer.MIN_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_UTCToNonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = Long.MIN_VALUE + Integer.MAX_VALUE;
        long result = DateTimeZone.UTC.getMillisKeepLocal(zone, instant);
        assertEquals(instant + Integer.MAX_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_UTCToNonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = Long.MAX_VALUE + Integer.MIN_VALUE;
        long result = DateTimeZone.UTC.getMillisKeepLocal(zone, instant);
        assertEquals(instant + Integer.MIN_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testNextTransition_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, zone.nextTransition(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, zone.nextTransition(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testNextTransition_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Long.MAX_VALUE, zone.nextTransition(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, zone.nextTransition(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, zone.previousTransition(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, zone.previousTransition(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Long.MAX_VALUE, zone.previousTransition(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, zone.previousTransition(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testEquals_NonHourMaxBoundary() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(zone1, zone2);
    }

    @Test(timeout = 4000)
    public void testEquals_NonHourMinBoundary() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(zone1, zone2);
    }

    @Test(timeout = 4000)
    public void testHashCode_NonHourMaxBoundary() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCode_NonHourMinBoundary() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone.toString());
        assertFalse(zone.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testToString_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone.toString());
        assertFalse(zone.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetID_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone.getID());
        assertFalse(zone.getID().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetID_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone.getID());
        assertFalse(zone.getID().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetName_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        String name = zone.getName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetName_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        String name = zone.getName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetShortName_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        String name = zone.getShortName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetShortName_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        String name = zone.getShortName(0L);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetNameKey_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertNotNull(zone.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testGetNameKey_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertNotNull(zone.getNameKey(0L));
    }

    @Test(timeout = 4000)
    public void testIsFixed_NonHourMaxBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertTrue(zone.isFixed());
    }

    @Test(timeout = 4000)
    public void testIsFixed_NonHourMinBoundary() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertTrue(zone.isFixed());
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHourMaxBoundary2() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffset_NonHourMinBoundary2() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHourMaxBoundary2() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, zone.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_NonHourMinBoundary2() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, zone.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHourMaxBoundary2() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long instant = 0L;
        long local = zone.convertUTCToLocal(instant);
        assertEquals(Integer.MAX_VALUE, local);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_NonHourMinBoundary2() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MIN_VALUE);
        long instant = 0L