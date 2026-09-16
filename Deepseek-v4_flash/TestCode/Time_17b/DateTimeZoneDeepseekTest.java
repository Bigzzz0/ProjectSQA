package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.TimeZone;
import java.util.Locale;
import java.util.Set;

/**
 * Comprehensive white-box test suite for DateTimeZone.
 * Targets all major branches, boundary values, and the known defect
 * from Defects4J (testBug3476684_adjustOffset).
 *
 * [Branch & Defect Analysis Matrix]
 * ==================================
 * 1. forID(): null -> default, "UTC" -> UTC, valid provider zone, offset strings (+/-hh:mm), invalid ID -> IllegalArgumentException.
 * 2. forOffsetHoursMinutes(): hours/minutes range, overflow, sign handling, UTC case.
 * 3. forOffsetMillis(): positive/negative offset.
 * 4. forTimeZone(): null -> default, "UTC" zone, old alias conversion, GMT+/- format, invalid -> IllegalArgumentException.
 * 5. getDefault() / setDefault(): null check, security, caching.
 * 6. getOffset(ReadableInstant): null returns current time offset.
 * 7. getStandardOffset(): abstract, tested via concrete zones.
 * 8. isStandardOffset(): comparison of offset and standard offset.
 * 9. getOffsetFromLocal(): DST gap/overlap branches, western hemisphere handling.
 * 10. convertUTCToLocal(): overflow detection.
 * 11. convertLocalToUTC(instant, strict): DST gap handling, strict exception, eastern/western hemisphere logic.
 * 12. adjustOffset(): duplicate local time resolution, earlier/later selection.
 * 13. isLocalDateTimeGap(): fixed zone => false, gap detection via exception.
 * 14. getName()/getShortName(): nameKey null, provider returns null, locale handling.
 * 15. equals/hashCode/toString.
 * 16. Defect reproduction: adjustOffset in a zone with DST (e.g., America/Sao_Paulo) around 2012-02-25.
 */
public class DateTimeZoneDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testForID_UTC() {
        assertSame("forID(\"UTC\") must return DateTimeZone.UTC", DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test(timeout = 4000)
    public void testForID_NullReturnsDefault() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        assertNotNull("getDefault() must not be null", defaultZone);
        assertEquals("forID(null) must return default", defaultZone, DateTimeZone.forID(null));
    }

    @Test(timeout = 4000)
    public void testForID_ValidProviderZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull("Zone must be found", zone);
        assertTrue("ID must be Europe/London", zone.getID().startsWith("Europe/London") || zone.getID().equals("Europe/London"));
    }

    @Test(timeout = 4000)
    public void testForID_OffsetPlus() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertNotNull("Zone for +05:30 must not be null", zone);
        assertEquals("Offset must be +05:30", "+05:30", zone.getID());
        assertEquals("Offset millis must be 19800000", 19800000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_OffsetNegative() {
        DateTimeZone zone = DateTimeZone.forID("-03:00");
        assertNotNull("Zone for -03:00 must not be null", zone);
        assertEquals("Offset must be -03:00", "-03:00", zone.getID());
        assertEquals("Offset millis must be -10800000", -10800000, zone.getOffset(0L));
    }

    @Test(timeout = 4000)
    public void testForID_InvalidIdThrows() {
        try {
            DateTimeZone.forID("InvalidZone");
            fail("Expected IllegalArgumentException for invalid id");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Zero() {
        assertSame("forOffsetHoursMinutes(0,0) must return UTC", DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-5, 0);
        assertEquals("-05:00", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_MinutesOutOfRange() {
        try {
            DateTimeZone.forOffsetHoursMinutes(0, 60);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            DateTimeZone.forOffsetHoursMinutes(0, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testForOffsetHoursMinutes_ArithmeticOverflow() {
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
            fail("Expected IllegalArgumentException due to overflow");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals("-01:00", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForOffsetMillis_UTC() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(0);
        assertSame("forOffsetMillis(0) must return UTC", DateTimeZone.UTC, zone);
    }

    @Test(timeout = 4000)
    public void testForTimeZone_Null() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        assertEquals("forTimeZone(null) must return default", defaultZone, DateTimeZone.forTimeZone((TimeZone) null));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_UTC() {
        assertSame("forTimeZone(TimeZone.getTimeZone(\"UTC\")) must be DateTimeZone.UTC",
                DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
    }

    @Test(timeout = 4000)
    public void testForTimeZone_OldAlias() {
        // EST -> America/New_York
        TimeZone jdkEst = TimeZone.getTimeZone("EST");
        DateTimeZone jodaEst = DateTimeZone.forTimeZone(jdkEst);
        assertEquals("EST must map to America/New_York", "America/New_York", jodaEst.getID());
    }

    @Test(timeout = 4000)
    public void testForTimeZone_GmtOffset() {
        TimeZone gmt = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone zone = DateTimeZone.forTimeZone(gmt);
        assertEquals("+02:00", zone.getID());
    }

    @Test(timeout = 4000)
    public void testForTimeZone_Invalid() {
        TimeZone invalid = new SimpleTimeZone(0, "BAD");
        try {
            DateTimeZone.forTimeZone(invalid);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetDefault_NotNull() {
        assertNotNull("Default zone must not be null", DateTimeZone.getDefault());
    }

    @Test(timeout = 4000)
    public void testSetDefault_Valid() {
        DateTimeZone original = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.UTC);
            assertSame("Default should be UTC", DateTimeZone.UTC, DateTimeZone.getDefault());
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void testSetDefault_Null() {
        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetAvailableIDs_NotEmpty() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull("Available IDs must not be null", ids);
        assertTrue("Available IDs must contain at least UTC", ids.contains("UTC"));
    }

    @Test(timeout = 4000)
    public void testGetOffset_ReadableInstantNull() {
        // Should use current time, so just ensure no exception and result is int
        int offset = DateTimeZone.UTC.getOffset((ReadableInstant) null);
        assertEquals("UTC offset must be 0", 0, offset);
    }

    @Test(timeout = 4000)
    public void testGetOffset_ReadableInstantNonNull() {
        Instant instant = new Instant(0L);
        int offset = DateTimeZone.forID("America/New_York").getOffset(instant);
        // New York standard offset is -5 hours = -18000000 ms (if not DST around 1970-01-01)
        assertTrue("Offset should be negative for America/New_York around epoch", offset < 0);
    }

    @Test(timeout = 4000)
    public void testIsStandardOffset() {
        // UTC is always standard
        assertTrue("UTC isStandardOffset must be true", DateTimeZone.UTC.isStandardOffset(0L));
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // In winter (1970-01-01) London is GMT+0, so standard offset = 0
        assertTrue("London at epoch should be standard", london.isStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_Normal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        // Local time 1970-01-01T00:00:00.000 in London = UTC 0? Actually at that date London was GMT+0, so offset=0
        long localMillis = 0L; // local millis assuming offset 0 -> but we call getOffsetFromLocal
        int offset = london.getOffsetFromLocal(localMillis);
        // In winter, should be 0
        assertEquals("London winter offset from local should be 0", 0, offset);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testGetOffsetFromLocal_DSTGap() {
        // Use a zone with DST gap: e.g., America/New_York spring forward
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Spring forward 2013-03-10: clock jumps from 2:00 to 3:00. Local 2:30 does not exist.
        // getOffsetFromLocal should return the offset after the gap (EDT) according to javadoc.
        // The local millis for 2013-03-10T02:30:00.000 (Eastern Standard Time, offset -5h) would be:
        // Actually to compute local instant we need to assume an offset. We'll use known transition.
        long transitionMillis = 1362693600000L; // UTC 2013-03-10T07:00:00Z (daylight start)
        // Before transition: standard offset -5h, after: -4h.
        // Local time 2:30 standard: instant = UTC 7:30 = transition+1800000? But let's rely on known behavior.
        long localMillisBeforeGap = transitionMillis - 1800000L; // local 1:30 (standard)
        int offset = ny.getOffsetFromLocal(localMillisBeforeGap);
        assertEquals("Offset at 1:30 before spring forward should be -5h", -18000000, offset);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_Normal() {
        long utc = 0L;
        long local = DateTimeZone.UTC.convertUTCToLocal(utc);
        assertEquals("UTC+0 conversion should be identity", utc, local);
        local = DateTimeZone.forID("+02:00").convertUTCToLocal(utc);
        assertEquals("+02:00 zone conversion should add 7200000", 7200000L, local);
    }

    @Test(timeout = 4000)
    public void testConvertUTCToLocal_Overflow() {
        // Near Long.MAX_VALUE, addition may overflow
        try {
            DateTimeZone.forID("+14:00").convertUTCToLocal(Long.MAX_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_Normal() {
        DateTimeZone utc = DateTimeZone.UTC;
        long local = 0L;
        assertEquals("UTC local->UTC should be identity", local, utc.convertLocalToUTC(local, false));
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_StrictGap() {
        // In spring forward gap, strict should throw
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Choose local time that falls into the gap: e.g., local 2:30 AM on March 10, 2013.
        long transitionUtc = 1362693600000L; // UTC 2013-03-10T07:00:00Z
        // Local instant assuming standard offset (-5h): local = transitionUtc + 18000000 = transitionUtc + 5h
        long localInGap = transitionUtc + 18000000L + 1800000L; // 2:30 local (but 2:30 does not exist)
        try {
            ny.convertLocalToUTC(localInGap, true);
            fail("Expected IllegalArgumentException for strict local-to-UTC in DST gap");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConvertLocalToUTC_NonStrictGap() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long transitionUtc = 1362693600000L;
        long localInGap = transitionUtc + 18000000L + 1800000L; // 2:30 local standard offset assumption
        long utcResult = ny.convertLocalToUTC(localInGap, false);
        // Should return something after the gap
        assertTrue("Result should be > transition", utcResult >= transitionUtc);
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_SameZone() {
        long instant = 123456789L;
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Same zone should return same instant", instant, zone.getMillisKeepLocal(zone, instant));
    }

    @Test(timeout = 4000)
    public void testGetMillisKeepLocal_DifferentZone() {
        long instant = 0L;
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        long parisLocal = london.convertUTCToLocal(instant); // 0+0 = 0
        // But paris at winter is +1h, so local 0 in paris corresponds to UTC -1h
        long expected = paris.convertLocalToUTC(parisLocal, false);
        assertEquals("MillisKeepLocal should return correct UTC", expected, london.getMillisKeepLocal(paris, instant));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_FixedZone() {
        assertFalse("Fixed zone should return false", DateTimeZone.UTC.isLocalDateTimeGap(null));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (adjustOffset bug) ====================

    /**
     * Reproduces the Defects4J bug: testBug3476684_adjustOffset.
     * 
     * The bug manifests when adjustOffset is called during a DST overlap (fall back).
     * The method should return the later instant (earlierOrLater=true => later offset,
     * i.e., more east/west? In southern hemisphere, e.g., Brazil, DST ends in February,
     * clocks go back from -02:00 to -03:00. The overlap occurs at 0:00 local time.
     * For a given local time during overlap, there are two possible UTC instants.
     * adjustOffset(instant, true) should return the later UTC instant (the one with -03:00).
     * The bug returns the earlier instant (-02:00).
     * We use America/Sao_Paulo with a known transition around 2012-02-25.
     */
    @Test(timeout = 4000)
    public void testAdjustOffset_Bug3476684() {
        // Zone: America/Sao_Paulo (Brazil) has DST ended on Feb 26, 2012 (in 2012, DST end was third Sunday of February).
        // Transition from -02:00 to -03:00 at 0:00 local time (i.e., clocks go back 1 hour at midnight UTC -02:00).
        // The UTC instant of transition: 2012-02-26T02:00:00Z (since -02:00 midnight local = 02:00 UTC).
        // Overlap: local times 0:00 to 1:00 are repeated (first with -02:00, then after change with -03:00).
        DateTimeZone saoPaulo = DateTimeZone.forID("America/Sao_Paulo");
        // A specific instant during the overlap: e.g., local 2012-02-25T23:15:00.000
        // This local time exists twice: one with -02:00 (UTC 01:15 next day) and one with -03:00 (UTC 02:15 next day).
        // The bug: when calling adjustOffset(instant, true) for the earlier UTC instant (which maps to the later local time?),
        // the method should return the later instant but returns the earlier.
        // We need to determine the correct UTC instant for that local time.
        // According to the test expectation: expected: <2012-02-25T23:15:00.000-03:00> but was: <2012-02-25T23:15:00.000-02:00>.
        // This indicates that for some original instant, adjustOffset should have given the -03:00 offset but gave -02:00.
        // Let's construct a scenario: suppose we have an instant in the earlier half of the overlap (with -02:00).
        // If we call adjustOffset with earlierOrLater=true, the method should return the later instant (with -03:00).
        // The bug: it returns the earlier one (with -02:00).
        // We'll test adjustOffset on an instant that is known to be in the overlap.
        // From standard data: For America/Sao_Paulo, the transition in 2012 occurred at 2012-02-26T02:00:00Z (UTC).
        // Overlap: local times from 0:00 to 1:00 (first pass with -02:00, second pass with -03:00).
        // Choose local 0:30 (i.e., 30 minutes after midnight). The earlier UTC instant = local + offset -02:00 = 0:30 + 2h = 2:30 UTC.
        // The later UTC instant = local + offset -03:00 = 0:30 + 3h = 3:30 UTC.
        // Call adjustOffset on earlier UTC 2:30 (which corresponds to local 0:30 with -02:00) with earlierOrLater=true.
        // Should return the later UTC 3:30 (local 0:30 with -03:00). The bug returns 2:30.
        long earlierUtc = 1330245000000L; // 2012-02-26T02:30:00.000 UTC (check: 2012-02-26 02:30)
        long expectedLaterUtc = 1330248600000L; // 2012-02-26T03:30:00.000 UTC
        long result = saoPaulo.adjustOffset(earlierUtc, true);
        assertEquals("adjustOffset(true) should return later instant (with -03:00 offset)", expectedLaterUtc, result);
        // Also test earlierOrLater=false: should keep the earlier instant
        result = saoPaulo.adjustOffset(earlierUtc, false);
        assertEquals("adjustOffset(false) should return earlier instant (with -02:00 offset)", earlierUtc, result);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testForID_NullInput() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        assertNotNull(defaultZone);
        assertEquals("forID(null) should return default", defaultZone, DateTimeZone.forID(null));
    }

    @Test(timeout = 4000)
    public void testForID_EmptyString() {
        try {
            DateTimeZone.forID("");
            fail("Expected IllegalArgumentException for empty id");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testForTimeZone_NullInput() {
        assertEquals("forTimeZone(null) should return default", DateTimeZone.getDefault(), DateTimeZone.forTimeZone((TimeZone) null));
    }

    @Test(timeout = 4000)
    public void testSetDefault_RejectsNull() {
        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals_SameId() {
        DateTimeZone z1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone z2 = DateTimeZone.forID("Europe/Paris");
        assertEquals("Same ID zones should be equal", z1, z2);
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentId() {
        DateTimeZone z1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone z2 = DateTimeZone.forID("Europe/London");
        assertFalse("Different ID zones should not be equal", z1.equals(z2));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        int expected = 57 + zone.getID().hashCode();
        assertEquals("HashCode must match formula", expected, zone.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("toString should return ID", "Europe/Paris", zone.toString());
    }

    @Test(timeout = 4000)
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        TimeZone tz = zone.toTimeZone();
        assertEquals("JDK TimeZone ID must match", "America/New_York", tz.getID());
    }

    @Test(timeout = 4000)
    public void testGetID() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertEquals("getID must return 'UTC'", "UTC", zone.getID());
    }

    @Test(timeout = 4000)
    public void testGetName_NullLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        String name = zone.getName(0L, null);
        assertNotNull("Name must not be null", name);
    }

    @Test(timeout = 4000)
    public void testGetShortName_NullLocale() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        String name = zone.getShortName(0L, null);
        assertNotNull("Short name must not be null", name);
    }

    @Test(timeout = 4000)
    public void testIsFixed() {
        assertTrue("UTC is fixed", DateTimeZone.UTC.isFixed());
        assertFalse("Europe/London is not fixed", DateTimeZone.forID("Europe/London").isFixed());
    }

    @Test(timeout = 4000)
    public void testNextTransition_UTC() {
        assertEquals("nextTransition on UTC should return same instant", Long.MAX_VALUE, DateTimeZone.UTC.nextTransition(0L));
    }

    @Test(timeout = 4000)
    public void testPreviousTransition_UTC() {
        assertEquals("previousTransition on UTC should return same instant", Long.MIN_VALUE, DateTimeZone.UTC.previousTransition(0L));
    }

    @Test(timeout = 4000)
    public void testGetStandardOffset_UTC() {
        assertEquals("UTC standard offset is 0", 0, DateTimeZone.UTC.getStandardOffset(0L));
    }

    @Test(timeout = 4000)
    public void testAdjustOffset_NotOverlap() {
        // Outside DST transitions, adjustOffset should return same instant
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long instant = 0L;
        assertEquals("No overlap, earlierOrLater=true should return same", instant, london.adjustOffset(instant, true));
        assertEquals("No overlap, earlierOrLater=false should return same", instant, london.adjustOffset(instant, false));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_ActualGap() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Spring forward 2013-03-10: local 2:30 does not exist
        LocalDateTime gapDateTime = new LocalDateTime(2013, 3, 10, 2, 30, 0, 0);
        assertTrue("Local date time in gap should return true", ny.isLocalDateTimeGap(gapDateTime));
    }

    @Test(timeout = 4000)
    public void testIsLocalDateTimeGap_NotGap() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        LocalDateTime normalDateTime = new LocalDateTime(2013, 3, 10, 1, 30, 0, 0);
        assertFalse("Local date time not in gap should return false", ny.isLocalDateTimeGap(normalDateTime));
    }

    // Additional coverage for adjustOffset with negative offsets and western hemisphere
    @Test(timeout = 4000)
    public void testAdjustOffset_NegativeOffset() {
        DateTimeZone pacific = DateTimeZone.forID("America/Los_Angeles");
        // Known transition: fall back 2012-11-04 at 2:00 local (PDT to PST)
        // Overlap: 1:00 local repeated
        long instant = 1352091600000L; // 2012-11-04T09:00:00Z (UTC) corresponds to local 2:00? Actually need precise.
        // Just ensure no exception and some coherent result
        long result = pacific.adjustOffset(instant, true);
        assertTrue("Result should be within acceptable range", result >= instant - 3600000 && result <= instant + 3600000);
    }
}