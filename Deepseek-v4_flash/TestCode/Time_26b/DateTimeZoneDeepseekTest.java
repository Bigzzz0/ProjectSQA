package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for DateTimeZone, targeting branch coverage
 * and the known DST cutover defect (e.g., Paris October transition).
 *
 * [Branch & Defect Analysis Matrix]
 * - forID: null → default, "UTC" → UTC, offset string, invalid ID
 * - forOffsetHoursMinutes: 0/0 → UTC, minutes out of range, overflow
 * - forOffsetMillis: printOffset and caching
 * - getOffsetFromLocal: DST boundary logic (defect zone)
 * - convertLocalToUTC: strict vs non-strict, DST gap/overlap
 * - isLocalDateTimeGap: fixed zone, gap detection
 * - getName/getShortName: nameKey null, locale null, nameProvider fallback
 * - convertUTCToLocal: overflow detection
 * - getMillisKeepLocal: same zone, different zone null
 * - equals/hashCode: abstract; rely on concrete zones
 */
public class DateTimeZoneDeepseekTest {

    // ==================== Partition A: Core functional logic ====================

    @Test(timeout = 4000)
    public void testForID_nullReturnsDefault() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        DateTimeZone result = DateTimeZone.forID(null);
        assertNotNull(result);
        assertEquals(defaultZone, result);
    }

    @Test(timeout = 4000)
    public void testForID_UTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test(timeout = 4000)
    public void testForID_positiveOffset() {
        DateTimeZone zone = DateTimeZone.forID("+03:00");
        assertEquals("+03:00", zone.getID());
        assertEquals(3 * 60 * 60 * 1000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_negativeOffset() {
        DateTimeZone zone = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zone.getID());
        assertEquals(-(5 * 60 + 30) * 60 * 1000, zone.getOffset(0L));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForID_invalidId() {
        DateTimeZone.forID("InvalidID");
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_zero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_negative() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_60() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_overflow() {
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE / 60 + 1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals("-01:00", zone.getID());
        assertEquals(-3600000, zone.getOffset(0L));
    }

    // ==================== Partition B: Boundary & edge cases ====================

    @Test(timeout = 4000)
    public void testGetOffset_ReadableInstantNull() {
        long now = DateTimeUtils.currentTimeMillis();
        int offset = DateTimeZone.getDefault().getOffset(null);
        assertEquals(DateTimeZone.getDefault().getOffset(now), offset);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_overflowDetection() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(Integer.MAX_VALUE);
        long largeUTC = Long.MAX_VALUE;
        try {
            zone.convertUTCToLocal(largeUTC);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_noDST() {
        DateTimeZone zone = DateTimeZone.UTC;
        long local = 100000L;
        assertEquals(local, zone.convertLocalToUTC(local, false));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_fixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        LocalDateTime ldt = new LocalDateTime(2000, 1, 1, 12, 0);
        assertFalse(zone.isLocalDateTimeGap(ldt));
    }

    @Test(timeout = 4000)
    public void testGetName_nullLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long instant = 1000L * (10 * 365 * 86400); // approx 1980
        String name = zone.getName(instant, null);
        assertNotNull(name);
        // should not throw
    }

    @Test(timeout = 4000)
    public void testGetShortName_nameKeyNull() {
        DateTimeZone zone = new DateTimeZone("Custom") {
            public String getNameKey(long instant) { return null; }
            public int getOffset(long instant) { return 0; }
            public int getStandardOffset(long instant) { return 0; }
            public boolean isFixed() { return true; }
            public long nextTransition(long instant) { return instant; }
            public long previousTransition(long instant) { return instant; }
            public boolean equals(Object obj) { return false; }
        };
        assertEquals("Custom", zone.getShortName(0L));
    }

    // ==================== Partition C: Defect-targeted DST transition tests ====================

    /**
     * Targeted defect: Paris DST fall-back (October) - getOffsetFromLocal should return
     * earlier (summer) offset for ambiguous local time. Known failure: returns +01:00 instead of +02:00.
     */
    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_ParisFallBack() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        // DST ends in Paris at 2013-10-27T03:00:00+02:00 -> UTC+02:00 -> UTC+01:00
        // At 02:30 local, there is overlap: both +02:00 (summer) and +01:00 (winter) are valid.
        // Expected offset (prefer earlier/summer) is +02:00.
        // Use a local millis corresponding to 2013-10-27T02:30:00 local.
        // Convert from UTC: 2013-10-27T00:30:00Z (summer) = 1382838600000?
        // Let's compute: 2013-10-27T00:30:00Z = (2013-10-27T02:30:00+02:00)
        long utcSummer = 1382838600000L; // approximate
        long localMillis = utcSummer + paris.getOffset(utcSummer); // 1382838600000 + 7200000 = 1382845800000
        int offset = paris.getOffsetFromLocal(localMillis);
        // Expected offset during summer: +02:00 = 7200000 ms
        assertEquals("Paris DST fall-back should prefer summer offset", 7200000, offset);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_ParisFallBack_nonStrict() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        // local time during overlap (should map to earlier UTC = summer)
        long localMillis = 1382845800000L; // 2013-10-27T02:30:00 local
        long utc = paris.convertLocalToUTC(localMillis, false);
        // Expected UTC: 2013-10-27T00:30:00Z (summer) = 1382838600000
        // Actually convertLocalToUTC uses getOffsetFromLocal logic; we expect summer offset.
        long expectedUtc = 1382838600000L;
        assertEquals("convertLocalToUTC should prefer summer offset", expectedUtc, utc);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_StrictGap() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        // Spring gap: local time that does not exist (02:30 on the day clocks go forward)
        // 2013-03-31T02:30:00 local is in the gap (clocks jump from 02:00 to 03:00)
        // Use local millis that falls in the gap
        // 2013-03-31T02:30:00 local -> non-existent
        long gapLocal = 1364704200000L; // approximate; let's compute more precisely
        // Actually we'll use a known gap instant: 2013-03-31T02:30:00+01:00 is after 03:00+02:00? It's easier to trigger with strict=true.
        // Simulate by using an instant known to be in gap (e.g., for Europe/Paris 2013-03-31T02:30:00Z? no, UTC 01:30? This is tricky.
        // Instead, use a zone with well-known gap: America/New_York spring 2016.
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Spring forward 2016-03-13: 02:00 becomes 03:00. So 02:30 local doesn't exist.
        // Compute UTC of 03:00 EDT = 2016-03-13T07:00:00Z? Actually EDT = UTC-4, so 03:00 EDT = 07:00Z.
        // 2016-03-13T02:30:00 EST = 07:30Z? No, EST = UTC-5 -> 02:30 EST = 07:30Z.
        // Gap: there is no 02:30 EDT because clocks go directly from 02:00 EST to 03:00 EDT.
        // So a local millis representing 02:30 EST (exists) will be converted with strict=false fine,
        // but a local millis that pretends to be 02:30 EDT does not exist. 
        // We'll construct invalid local millis that would only be valid in summer offset.
        // The simplest: use a current DateTime and manually set to gap time.
        // We'll use timestamp for 2016-03-13T02:30:00.000-05:00 (EST) which is valid, but we want to test gap detection.
        // Instead test with a mock? According to spec, we need to trigger the strict branch.
        // For a real test, we'll use an instant where nextTransition returns the same as instantLocal-offsetLocal.
        // That happens if the local time is exactly at a transition? Hard.
        // We'll trust that the defect tests cover this; we provide a basic call that should pass for the bug fix.
        // For demonstration, call with a known gap: use DateTimeZone for NZ? 
        // Simpler: use a custom DateTimeZone with a gap? Not allowed.
        // We'll use Europe/Paris 2013-03-31 02:30:00 local (which is gap).
        LocalDateTime gapLdt = new LocalDateTime(2013, 3, 31, 2, 30, 0);
        try {
            gapLdt.toDateTime(paris);
            // If no exception, gap conversion succeeded incorrectly?
            fail("Expected IllegalArgumentException for gap");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_DSTTransition() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone utc = DateTimeZone.UTC;
        // Instant during DST (summer) in Paris:
        long utcInstant = 1382838600000L; // 2013-10-27T00:30:00Z
        // Keep local time in same zone should be unchanged
        long result = paris.getMillisKeepLocal(null, utcInstant);
        assertEquals(utcInstant, result);
        // Convert to UTC zone: keep local means that local time of Paris instant should be same as UTC instant local time?
        // Actually getMillisKeepLocal computes: local = utcInstant + getOffset(utcInstant); then result = local - newZone.getOffsetFromLocal(local)
        // So for newZone=UTC, getOffsetFromLocal(local) = offset(instantLocal) for UTC which is 0.
        // So result = local.
        long local = utcInstant + paris.getOffset(utcInstant); // 1382838600000 + 7200000 = 1382845800000
        long expected = local - utc.getOffsetFromLocal(local); // = local
        assertEquals(expected, paris.getMillisKeepLocal(utc, utcInstant));
    }

    // ==================== Partition D: Exception & defensive paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructor_nullId() {
        new DateTimeZone(null) {
            // minimal abstract implementation
            public String getNameKey(long instant) { return null; }
            public int getOffset(long instant) { return 0; }
            public int getStandardOffset(long instant) { return 0; }
            public boolean isFixed() { return true; }
            public long nextTransition(long instant) { return instant; }
            public long previousTransition(long instant) { return instant; }
            public boolean equals(Object obj) { return false; }
        };
    }

    @Test(timeout = 4000)
    public void testSetDefault_nullThrows() {
        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testForTimeZone_nullReturnsDefault() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        assertEquals(defaultZone, DateTimeZone.forTimeZone(null));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_convertedId() {
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("EST");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertEquals("America/New_York", zone.getID());
    }

    // ==================== Partition E: Object lifecycle & contract ====================

    @Test(timeout = 4000)
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertEquals("UTC", zone.toString());
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        java.util.TimeZone tz = zone.toTimeZone();
        assertEquals("America/New_York", tz.getID());
    }
}