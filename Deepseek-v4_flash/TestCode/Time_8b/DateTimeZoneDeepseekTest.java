package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Target Class: DateTimeZone (abstract, tested via static methods and fixed offset zones)
 * 
 * Partition A - Core Functional Logic & State Transitions:
 *   - forID("UTC") returns UTC constant
 *   - forID("+HH:MM") valid offsets
 *   - forID(null) returns default
 *   - getDefault() / setDefault() interactions (basic)
 * 
 * Partition B - Boundary Value Analysis (BVA) & Extremes:
 *   - forOffsetHoursMinutes: hours -23, 0, 23; minutes 0, 59, -1, 60
 *   - forOffsetMillis: millis 0, -MAX_MILLIS, MAX_MILLIS, out-of-range
 *   - parseOffset: "+00:00", "-23:59:59.999", "+23:59:59.999"
 *   - null arguments to forID, forTimeZone, getOffset(ReadableInstant)
 * 
 * Partition C - Defect-Targeted Branch Zone:
 *   - forOffsetHoursMinutes(0, -15) SHOULD throw IAE (bug: currently throws "Minutes out of range: -15" but spec says it should be allowed? 
 *     Actually defect shows minutes negative only when hours negative, but for (0,-15) minutes<0 should be invalid, which is correct behavior. 
 *     Wait: the bug report says testForOffsetHoursMinutes_int_int fails with "Minutes out of range: -15" - need to check original logic.
 *     According to source: if (minutesOffset < 0 || minutesOffset > 59) throws. So (-15) correctly throws. 
 *     The defect must be that for (2, -15) it does NOT throw but should? Looking at comments: "Hour +ve, minute -ve => IllegalArgumentException". 
 *     But source code: if (minutesOffset < 0) throws regardless of hours sign. So for (2, -15) it would throw. 
 *     Actually the bug might be that for negative hours with negative minutes, the calculation is wrong. 
 *     Let's re-read: "minutesOffset = hoursInMinutes - minutesOffset" when hoursInMinutes < 0. If hours=-2, minutes=-15, then hoursInMinutes=-120, 
 *     minutesOffset = -120 - (-15) = -105, then safeMultiply(-105, 60000) = -6300000, which is -1:45 offset, valid. But the minutes validation check 
 *     would catch minutesOffset < 0 first for -15 and throw. So callers can't pass negative minutes at all. 
 *     But the doc says for (-2,-15) it should work. So the bug is that negative minutes are rejected even when hours are negative.
 *     Defect: the validation `if (minutesOffset < 0)` should be conditional on hoursOffset >= 0, or more permissive.
 *     We'll test: forOffsetHoursMinutes(-2, -15) should succeed, forOffsetHoursMinutes(2, -15) should throw. 
 *     Also test (0, -15) should throw per doc? Doc says: zero hour, negative minute => "-00:15" valid. So (0, -15) should succeed too. 
 *     Actually the doc table: zero minute negative gives -00:15. So minutes negative is allowed when hours <= 0? Yes.
 *     Current code: minutesOffset < 0 always throws. So bug: it should allow negative minutes when hoursOffset <= 0.
 *     We'll target this.
 * 
 * Partition D - Exception & Defensive Guard Paths:
 *   - forOffsetHoursMinutes with hours out of range
 *   - forOffsetMillis with millis out of range
 *   - forTimeZone with null
 *   - DateTimeZone constructor with null id
 * 
 * Partition E - Object Lifecycle & Contract Integrity:
 *   - hashCode consistency (abstract, but test fixed zone)
 *   - toString returns ID
 */
public class DateTimeZoneDeepseekTest {

    // ======= Partition A: Core Functional Logic =======
    @Test(timeout = 4000)
    public void testForID_UTC() {
        assertSame("forID(UTC) should return UTC constant", DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test(timeout = 4000)
    public void testForID_OffsetString_Positive() {
        DateTimeZone zone = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zone.getID());
        assertEquals(2 * 3600 * 1000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_OffsetString_Negative() {
        DateTimeZone zone = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zone.getID());
        assertEquals(-(5 * 3600 + 30 * 60) * 1000L, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_OffsetString_Zero() {
        assertSame("+00:00 should map to UTC", DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertSame("-00:00 should map to UTC", DateTimeZone.UTC, DateTimeZone.forID("-00:00"));
    }

    @Test(timeout = 4000)
    public void testForID_NullReturnsDefault() {
        DateTimeZone saved = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.UTC);
            assertSame(DateTimeZone.UTC, DateTimeZone.forID(null));
        } finally {
            DateTimeZone.setDefault(saved);
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForID_InvalidString() {
        DateTimeZone.forID("BogusZone");
    }

    // ======= Partition B: BVA & Extremes =======
    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_ZeroZero() {
        assertSame("0h0m should be UTC", DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_PositivePositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 45);
        assertEquals("+05:45", zone.getID());
        assertEquals((5 * 60 + 45) * 60 * 1000, zone.getOffset(0L));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooLow() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(0, 60);
    }

    // Note: (0, -15) should succeed per documentation (produces -00:15).
    // This test will fail on current buggy code, revealing the defect.
    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_ZeroHourNegativeMinute() {
        // Expected: -00:15 is a valid offset
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, -15);
        assertEquals("-00:15", zone.getID());
        assertEquals(-15 * 60 * 1000, zone.getOffset(0L));
    }

    // Also ( -2, -15 ) should succeed
    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_NegativeHourNegativeMinute() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, -15);
        assertEquals("-02:15", zone.getID());
        assertEquals(-(2 * 60 + 15) * 60 * 1000, zone.getOffset(0L));
    }

    // (2, -15) should throw - hour positive, minute negative
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_PositiveHourNegativeMinute_ShouldThrow() {
        DateTimeZone.forOffsetHoursMinutes(2, -15);
    }

    // Maximum offset +23:59
    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_MaxPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(23, 59);
        assertEquals("+23:59", zone.getID());
        assertEquals((23 * 60 + 59) * 60 * 1000, zone.getOffset(0L));
    }

    // Minimum offset -23:59
    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_MaxNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-23, -59);
        assertEquals("-23:59", zone.getID());
        assertEquals(-(23 * 60 + 59) * 60 * 1000, zone.getOffset(0L));
    }

    // Boundary: offset just beyond max (23:60) caught by minutes check
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesBoundary() {
        DateTimeZone.forOffsetHoursMinutes(23, 60);
    }

    // forOffsetMillis boundaries
    @Test(timeout = 4000)
    public void testForOffsetMillis_Zero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_MaxPositive() {
        int max = 86400 * 1000 - 1;
        DateTimeZone zone = DateTimeZone.forOffsetMillis(max);
        assertEquals("+23:59:59.999", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_MaxNegative() {
        int min = -(86400 * 1000 - 1);
        DateTimeZone zone = DateTimeZone.forOffsetMillis(min);
        assertEquals("-23:59:59.999", zone.getID());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetMillis_TooHigh() {
        DateTimeZone.forOffsetMillis(86400 * 1000);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetMillis_TooLow() {
        DateTimeZone.forOffsetMillis(-86400 * 1000);
    }

    // ======= Partition C: Defect-Targeted Branch Zone =======
    // This directly targets the known defect: negative minute validation is too strict.
    // The method should allow negative minutes when hour is zero or negative.
    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_DefectTarget_NegativeMinuteWithNonPositiveHour() {
        // Zero hour, negative minute should work -> -00:15
        DateTimeZone zone1 = DateTimeZone.forOffsetHoursMinutes(0, -15);
        assertEquals("-00:15", zone1.getID());

        // Negative hour, negative minute should work -> -02:15
        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-2, -15);
        assertEquals("-02:15", zone2.getID());

        // Negative hour, positive minute should work -> -01:45
        DateTimeZone zone3 = DateTimeZone.forOffsetHoursMinutes(-1, 45);
        assertEquals("-01:45", zone3.getID());
    }

    // Also verify positive hour with negative minute still throws (defect not about this case)
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_DefectTarget_PositiveHourNegativeMinute() {
        DateTimeZone.forOffsetHoursMinutes(1, -30);
    }

    // ======= Partition D: Exception & Defensive Guard Paths =======
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForTimeZone_Null() {
        DateTimeZone.forTimeZone(null);
    }

    @Test(timeout = 4000)
    public void testForTimeZone_UTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_ValidId() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        assertEquals("America/New_York", dtz.getID());
    }

    @Test(timeout = 4000)
    public void testForTimeZone_GMTPlusOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        assertEquals("+05:30", dtz.getID());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testForTimeZone_InvalidId() {
        TimeZone tz = TimeZone.getTimeZone("Bogus");
        DateTimeZone.forTimeZone(tz);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullId() {
        // Anonymous subclass to test constructor validation
        DateTimeZone zone = new DateTimeZone(null) {
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
    public void testGetOffsetReadableInstant_Null() {
        // When instant is null, uses current time millis - just ensure no exception
        DateTimeZone zone = DateTimeZone.UTC;
        int offset = zone.getOffset((ReadableInstant) null);
        assertTrue("Offset should be reasonable", offset >= -86400000 && offset <= 86400000);
    }

    @Test(timeout = 4000)
    public void testGetOffsetReadableInstant_NotNull() {
        DateTimeZone zone = DateTimeZone.forID("+03:00");
        long millis = 0L;
        assertEquals(3 * 3600 * 1000, zone.getOffset(new Instant(millis)));
    }

    // ======= Partition E: Object Lifecycle & Contract Integrity =======
    @Test(timeout = 4000)
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertEquals("+05:30", zone.toString());
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("+02:00");
        DateTimeZone zone2 = DateTimeZone.forID("+02:00");
        assertEquals("Hash codes should match for equal zones", zone1.hashCode(), zone2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertTrue("Zone should equal itself", zone.equals(zone));
        assertFalse("Zone should not equal null", zone.equals(null));
        assertFalse("Zone should not equal string", zone.equals("UTC"));
    }

    @Test(timeout = 4000)
    public void testGetID() {
        assertEquals("UTC", DateTimeZone.UTC.getID());
        assertEquals("+02:00", DateTimeZone.forID("+02:00").getID());
    }

    @Test(timeout = 4000)
    public void testIsFixed_UTC() {
        assertTrue("UTC is fixed", DateTimeZone.UTC.isFixed());
    }

    @Test(timeout = 4000)
    public void testIsFixed_Offset() {
        assertTrue("Fixed offset zone should be fixed", DateTimeZone.forID("+05:30").isFixed());
    }

    // Test convertUTCToLocal basic
    @Test(timeout = 4000)
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utc = 100000L;
        long local = zone.convertUTCToLocal(utc);
        assertEquals(utc + 2 * 3600 * 1000, local);
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_basic() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long local = 100000L;
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(local - 2 * 3600 * 1000, utc);
    }
}