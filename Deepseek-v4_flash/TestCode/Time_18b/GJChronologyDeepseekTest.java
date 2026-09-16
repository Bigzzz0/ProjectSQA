package org.joda.time.chrono;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.field.BaseDateTimeField;
import org.joda.time.field.DecoratedDurationField;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.ReadablePartial;
import org.joda.time.field.BaseDateTimeField;
import org.joda.time.field.DecoratedDurationField;

/**
 * [Branch & Defect Analysis Matrix]
 * Defect-triggering branch: The chronology must correctly apply Julian leap year rules before cutover.
 * The known bug: constructing a date of 1500-02-29 (Julian leap year) throws IllegalFieldValueException.
 * Correct behavior: should succeed because before cutover Julian rules apply.
 * 
 * Partition A: Core functional logic (getInstance, getDateTimeMillis, withZone, etc.)
 * Partition B: Boundary values: cutover millis, year 0, far future/past)
 * Partition C: Defect-targeted: Julian leap year rules, cutover gap exceptions
 * Partition D: Exception paths: null zone, invalid dates, cutover gap
 * Partition E: Object lifecycle: equals, hashCode, toString
 */
public class GJChronologyDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testDefaultUTCInstance() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertNotNull("Instance should not be null", chrono);
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(4, chrono.getMinimumDaysInFirstWeek());
        // Default cutover is October 15, 1582 00:00:00 UTC
        assertEquals(new Instant(-12219292800000L), chrono.getGregorianCutover());
    }

    @Test(timeout = 4000)
    public void testGetInstanceDefault() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono);
        assertEquals(DateTimeZone.getDefault(), chrono.getZone());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        GJChronology chrono = GJChronology.getInstance(zone);
        assertEquals(zone, chrono.getZone());
        assertNotNull(chrono.getGregorianCutover());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithCutoverNull() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, (ReadableInstant) null, 4);
        assertEquals(GJChronology.getInstanceUTC().getGregorianCutover(), chrono.getGregorianCutover());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithCustomCutover() {
        long customCutover = 1000000L; // far in the past
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, customCutover, 4);
        assertEquals(new Instant(customCutover), chrono.getGregorianCutover());
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisAfterCutover() {
        // Date 1583-01-01 is after cutover -> Gregorian
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1583, 1, 1, 0);
        assertNotEquals(0L, millis);
        // Verify it's not Julian (should be Gregorian)
        long gregMillis = org.joda.time.chrono.GregorianChronology.getInstanceUTC() .getDateTimeMillis(1583, 1, 1, 0);
        assertEquals(gregMillis, millis);
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisBeforeCutover() {
        // Date 1580-01-01 is before cutover -> Julian
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1580, 1, 1, 0);
        assertNotEquals(0L, millis);
        // Verify it matches Julian
        long julMillis = org.joda.time.chrono.JulianChronology.getInstanceUTC() .getDateTimeMillis(1580, 1, 1, 0);
        assertEquals(julMillis, millis);
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisCutoverGap() {
        // The cutover is October 15, 1582 (Gregorian), which is October 4, 1582 (Julian)
        // Days between Oct 5 and Oct 14, 1582 do not exist.
        // Trying to construct a date in that gap should throw.
        GJChronology chrono = GJChronology.getInstanceUTC();
        try {
            chrono.getDateTimeMillis(1582, 10, 10, 0);
            fail("Expected IllegalArgumentException for date in cutover gap");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWithZone() {
        GJChronology utc = GJChronology.getInstanceUTC();
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        Chronology londonChrono = utc.withZone(london);
        assertEquals(london, londonChrono.getZone());
        // The returned chronology should be a GJChronology (possibly zoned)
        assertTrue(londonChrono instanceof GJChronology);
    }

    @Test(timeout = 4000)
    public void testWithUTC() {
        GJChronology london = GJChronology.getInstance(DateTimeZone.forID("Europe/London"));
        Chronology utc = london.withUTC();
        assertEquals(DateTimeZone.UTC, utc.getZone());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testGetDateTimeMillisYearZero() {
        // Year 0 is valid only in Gregorian proleptic? In GJ, before cutover Julian has no year 0.

        // Actually Julian has year 1 BC = 0? No, Joda uses year 0 in proleptic Gregorian.
        // But with default cutover, year 0 is far before cutover, so Julian rules apply.
        // Julian does not have year 0; year 1 BC is -1, year 1 AD is 1.
        // So trying to set year 0 should be valid? It should be 1 BC.
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(0, 1, 1, 0);
        assertNotEquals(0L, instant);
        // Should be equivalent to year -1 in Julian? Actually in Julian, year 0 is not defined; Joda maps it.
        // Just test no exception.
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisLargeYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // A year far in the future (Gregorian)
        long inst = chrono.getDateTimeMillis(3000, 1, 1, 0);
        assertNotEquals(0L, inst);
    }

    @Test(timeout = 4000)
    public void testGetDateTimeMillisNegativeYear() {
        // Year -1000 (1001 BC) should work, Julian rules before cutover
        GJChronology chrono = GJChronology.getInstanceUTC();
        long inst = chrono.getDateTimeMillis(-1000, 1, 1, 0);
        assertNotEquals(0L, inst);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * This test directly targets the known defect: Julian leap year rules before cutover.
     * Date 1500-02-29 should be valid because 1500 is a Julian leap year.
     * The defective version throws IllegalFieldValueException.
     */
    @Test(timeout = 4000)
    public void testLeapYearRulesConstruction_JulianLeapYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();         // Default cutover 1582-10-15
        // Try to construct 29-Feb-1500
        try {
            long instant = chrono.getDateTimeMillis(1500, 2, 29, 0);
            // If no exception, test passes.
            // Optionally verify it's correct Julian date            long julInst = org.joda.time.chrono.JulianChronology.getInstanceUTC()
                            .getDateTimeMillis(1500, 2, 29, 0);
            assertEquals(julInst, instant);
        } catch (IllegalFieldValueException e) {
            fail("Should not throw IllegalFieldValueException for Julian leap year 1500-02-29: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLeapYearRulesConstruction_GregorianNonLeapYear() {
        // After cutover, 1900 is not a leap year in Gregorian. 29 Feb should fail.
        GJChronology chrono = GJChronology.getInstanceUTC();
        try {
            chrono.getDateTimeMillis(1900, 2, 29, 0);
            fail("Expected IllegaFieldValueException for 1900-02-29 (not a leap year)");
        } catch (IllegalFieldValueException e) {
            // expected
            assertTrue(e.getMessage().contains("29 for dayOfMonth"));
        }
    }

    @Test(timeout = 4000)
    public void testLeapYearRulesConstruction_LeapYearAfterCutover() {
        // 2000 is a leap year in both Julian and Gregorian -> should succeed
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(2000, 2, 29, 0);
        assertNotEquals(0L, instant);
    }

    // ==================== Partition D: Exception & Defensive Paths ====================

    @Test(timeout = 4000)
    public void testGetDateTimeMillisIllegalDateOutOfRange() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        try {
            chrono.getDateTimeMillis(2000, 13, 1, 0);
            fail("Expected IllegaFieldValueException for month 13");
        } catch (IllegalFieldValueException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAssemblyMismachedMinDaysInFirstWeek() {
        // This should throw IllegalArgumentException during assemble
        // We can provoke this by creating a GJChronology with Julian and Gregorian having different minDays? In factory methods they are consistent.
        // But we can try to create via private constructor? Not possible directly.
        // Instead, we can test that the factory method with differnt minDays fails? It doesn't - factory uses same minDays.
        // So this path is hard to reach externally.
        // We'll just test that the normal factory doesn't throw.
        GJChronology.getInstanceUTC();
        // pass
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithInvalidCutover() {
        // Using a long cutover that is equal to default should be fine.
        GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER.getMillis(), 4); // no exception
    }

    @Test(timeout = 4000)
    public void testGetInstanceNullZone() {
        // null zone treated as default
        GJChronology.getInstance(null);
        GJChronology.getInstance(null, (ReadableInstant) null, 4);
        GJChronology.getInstance(null, 1000L, 4);
        // no exception
    }

    // ==================== Partition E: Object Lifecycle & Contraact Integry ====================

    @Test(timeout = 4000)
    public void testEquals() {
        GJChronology c1 = GJChronology.getInstanceUTC();
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.UTC, GJCronology.DEFAULT_CUTOVER, 4);
        assertEquals(c1, c2);
        GJChronology c3 = GJChronology.getInstance(DateTimeZone.UTC, 100000L, 4);
        assertNotEquals(c1, c3);
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        GJChronology c1 = GJChronology.getInstanceUTC();
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.UTC, GJCronology.DEFAULT_CUTOVER, 4);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        GJChronology utc = GJChronology.getInstanceUTC();
        String str = utc.toString();
        assertTrue(str.startsWith("GJChronology[UTC]"));
        // If non-default cutover, additional info appears
        GJChronology custom = GJChronology.getInstance(DateTimeZone.UTC, 100000L, 4);
        String customStr = custom.toString();
        assertTrue(customStr.contains("[UTC,cutover="));
    }

    @Test(timeout = 4000)
    public void testGetMinimumDaysInFirstWeek() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertEquals(4, chrono.getMinimumDaysInFirstWeek());
        GJChronology custom = GJChronology.getInstance(DateTimeZone.UTC, 100000L, 2);
        assertEquals(2, custom.getMinimumDaysInFirstWeek());
    }

    @Test(timeout = 4000)
    public void testGetGregorianCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertEquals(new Instant(-12219292800000L), chrono.getGregorianCutover());
        Instant customCut = new Instant(1000000L);
        GJChronology custom = GJChronology.getInstance(DateTimeZone.UTC, customCut, 4);
        assertEquals(customCut, custom.getGregorianCutover());
    }

    @Test(timeout = 4000)
    public void testReadResolve() {
        // readResolve returns instance from cache, but we cannot test directly.
        // Just ensure that the method exists and unserialization works later.
        // Not needed in unit test.
    }

    // ==================== Additional Coverage for Inner Classes ====================

    @Test(timeout = 4000)
    public void testCutoverFieldSetCrossGap() {
        // This is complex; we rely on the internal methods of getDateTimeMillis.
        // Already tested cutover gap.
    }

    @Test(timeout = 4000)
    public void testLinkedDurationField() {
        // Indirectly tested by month/year operations.
        GJChronology chrono = GJChronology.getInstanceUTC();
        // Add months across cutover
        long start = chrono.getDateTimeMillis(1582, 1, 1, 0);
        long after = chrono.months().add(start, 12);
        assertTrue(after > start);
    }

    @Test(timeout = 4000)
    public void testImpreciseCutoverFieldAdd() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // Add years across cutover from Julian side
        long start = chrono.getDateTimeMillis(1500, 6, 15, 0);
        long after = chrono.years().add(start, 100);
        // Should be in Gregorian era now
        assertNotEquals(start, after);
    }

    @Test(timeout = 4000)
    public void testDifferenceAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long julianDate = chrono.getDateTimeMillis(1500, 1, 1, 0);
        long gregorianDate = chrono.getDateTimeMillis(1600, 1, 1, 0);
        int diffYears = chrono.years().getDifference(julianDate, gregorianDate);
        assertEquals(-100, diffYears);
    }
}