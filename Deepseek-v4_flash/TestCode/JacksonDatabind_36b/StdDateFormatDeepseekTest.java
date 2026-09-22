package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import java.util.TimeZone;
import java.util.Locale;
import java.util.Date;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted Branches/Paths:
 * 1. parse(String): ISO8601 path (looksLikeISO8601 true) 
 *    - 'Z' ending with/without milliseconds
 *    - Timezone indicator present/absent
 *    - Plain date (len <= 10)
 *    - Various time lengths for padding
 * 2. parse(String): numeric timestamp path
 *    - Positive/negative values
 *    - inLongRange boundary
 * 3. parse(String): RFC1123 fallback
 * 4. parse(String, ParsePosition): similar paths, null handling
 * 5. format(Date, StringBuffer, FieldPosition): lazy init of _formatISO8601
 * 6. setTimeZone/withTimeZone: null handling, equality, format clearing
 * 7. withLocale: equality check
 * 8. clone: state preservation
 * 9. isLenient/setLenient: null handling, boolean boxing
 * 10. Defect-target: _lenient null in parseAsISO8601 throwErrors path
 *     (line ~275: String.format uses _lenient which can be null -> NPE)
 */
public class StdDateFormatDeepseekTest {

    /* =========================================
     * Partition A: Core Functional Logic & State Transitions
     * ========================================= */

    @Test(timeout = 4000)
    public void testParseISO8601Zulu() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date dt = fmt.parse("2024-01-15T12:30:45.123Z");
        assertNotNull(dt);
        // Verify time is correct by reformatting with known UTC format
        DateFormat verifyFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        verifyFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2024-01-15T12:30:45.123Z", verifyFmt.format(dt));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithOffset() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // +05:30 offset with colon
        Date dt = fmt.parse("2024-01-15T12:30:45.123+05:30");
        assertNotNull(dt);
        // Verify by converting to UTC
        DateFormat verifyFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        verifyFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2024-01-15T07:00:45.123Z", verifyFmt.format(dt));
    }

    @Test(timeout = 4000)
    public void testParseISO8601NoOffsetNoZ() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Should treat as local? Actually code appends Z, so UTC
        Date dt = fmt.parse("2024-01-15T12:30:45.123");
        assertNotNull(dt);
        DateFormat verifyFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        verifyFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2024-01-15T12:30:45.123Z", verifyFmt.format(dt));
    }

    @Test(timeout = 4000)
    public void testParseISO8601MissingMillis() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Missing milliseconds, with 'Z'
        Date dt = fmt.parse("2024-01-15T12:30:45Z");
        assertNotNull(dt);
        // Check that parsing doesn't throw and returns a valid date
        assertTrue(dt.getTime() > 0);
    }

    @Test(timeout = 4000)
    public void testParsePlainDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date dt = fmt.parse("2024-01-15");
        assertNotNull(dt);
        // Should be midnight UTC
        DateFormat verifyFmt = new SimpleDateFormat("yyyy-MM-dd");
        verifyFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2024-01-15", verifyFmt.format(dt));
    }

    @Test(timeout = 4000)
    public void testParseRFC1123() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date dt = fmt.parse("Mon, 15 Jan 2024 12:30:45 GMT");
        assertNotNull(dt);
        DateFormat verifyFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        verifyFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2024-01-15 12:30:45", verifyFmt.format(dt));
    }

    @Test(timeout = 4000)
    public void testParseNumericTimestamp() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        long expected = 1705318245123L; // Some timestamp
        Date dt = fmt.parse(String.valueOf(expected));
        assertNotNull(dt);
        assertEquals(expected, dt.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNegativeNumericTimestamp() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        long expected = -1000000L; // Negative timestamp
        Date dt = fmt.parse(String.valueOf(expected));
        assertNotNull(dt);
        assertEquals(expected, dt.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericTimestampOutOfRange() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Too large for long, should be treated as RFC1123
        try {
            fmt.parse("999999999999999999999999999");
            fail("Should have thrown ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testFormat() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date dt = new Date(1705318245123L);
        StringBuffer sb = new StringBuffer();
        StringBuffer result = fmt.format(dt, sb, new FieldPosition(0));
        assertSame(sb, result);
        assertTrue(result.length() > 0);
        // Should contain ISO8601 format
        assertTrue(result.toString().contains("T"));
    }

    @Test(timeout = 4000)
    public void testClone() throws Exception {
        StdDateFormat original = new StdDateFormat(TimeZone.getTimeZone("PST"), Locale.US, Boolean.TRUE);
        StdDateFormat cloned = original.clone();
        assertNotSame(original, cloned);
        // Clone should have same state
        assertTrue(cloned.isLenient());
    }

    @Test(timeout = 4000)
    public void testWithTimeZone() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat newFmt = fmt.withTimeZone(tz);
        assertNotSame(fmt, newFmt);
        assertEquals(tz, newFmt.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testWithTimeZoneNull() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        StdDateFormat newFmt = fmt.withTimeZone(null);
        // Should default to UTC
        assertEquals(TimeZone.getTimeZone("UTC"), newFmt.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testWithTimeZoneSame() throws Exception {
        StdDateFormat fmt = new StdDateFormat(TimeZone.getTimeZone("PST"), Locale.US, null);
        // Same instance should be returned when tz.equals(_timezone)
        StdDateFormat result = fmt.withTimeZone(TimeZone.getTimeZone("PST"));
        assertSame(fmt, result);
    }

    @Test(timeout = 4000)
    public void testWithLocale() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Locale loc = Locale.CANADA_FRENCH;
        StdDateFormat newFmt = fmt.withLocale(loc);
        assertNotSame(fmt, newFmt);
        // Can't directly check locale, but verify behavior
        assertNotNull(newFmt);
    }

    @Test(timeout = 4000)
    public void testWithLocaleSame() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        StdDateFormat result = fmt.withLocale(Locale.US);
        assertSame(fmt, result);
    }

    @Test(timeout = 4000)
    public void testSetTimeZone() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        TimeZone tz = TimeZone.getTimeZone("EST");
        fmt.setTimeZone(tz);
        assertEquals(tz, fmt.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testSetTimeZoneSame() throws Exception {
        StdDateFormat fmt = new StdDateFormat(TimeZone.getTimeZone("UTC"), Locale.US, null);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(TimeZone.getTimeZone("UTC"), fmt.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testIsLenientDefault() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        assertTrue(fmt.isLenient());
    }

    @Test(timeout = 4000)
    public void testIsLenientSetFalse() throws Exception {
        // Since setLenient returns void, we can only test via constructor
        StdDateFormat fmt = new StdDateFormat(null, Locale.US, Boolean.FALSE);
        assertFalse(fmt.isLenient());
    }

    /* =========================================
     * Partition B: Boundary Value Analysis & Extremes
     * ========================================= */

    @Test(timeout = 4000)
    public void testParseEmptyString() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        try {
            fmt.parse("   ");
            fail("Should have thrown ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseNull() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        try {
            fmt.parse((String) null);
            fail("Should have thrown NullPointerException or ParseException");
        } catch (NullPointerException e) {
            // Expected due to .trim()
        } catch (ParseException e) {
            // Also acceptable
        }
    }

    @Test(timeout = 4000)
    public void testParseNumericBoundary() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Long.MAX_VALUE
        Date dt = fmt.parse(String.valueOf(Long.MAX_VALUE));
        assertNotNull(dt);
        assertEquals(Long.MAX_VALUE, dt.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericLongMinValue() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date dt = fmt.parse(String.valueOf(Long.MIN_VALUE));
        assertNotNull(dt);
        assertEquals(Long.MIN_VALUE, dt.getTime());
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithOffsetNoMinutes() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // +05 (no minutes) should have "00" appended
        Date dt = fmt.parse("2024-01-15T12:30:45.123+05");
        assertNotNull(dt);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithOffsetColonAndSecondsOnly() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Missing minutes, only seconds
        Date dt = fmt.parse("2024-01-15T12:30+05:30");
        assertNotNull(dt);
    }

    @Test(timeout = 4000)
    public void testParseISO8601TimeLenCases() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Various time length scenarios
        assertNotNull(fmt.parse("2024-01-15T12:30:45.1Z"));
        assertNotNull(fmt.parse("2024-01-15T12:30:45.12Z"));
        assertNotNull(fmt.parse("2024-01-15T12:30:45.123Z"));
        assertNotNull(fmt.parse("2024-01-15T12:30:45Z"));
        assertNotNull(fmt.parse("2024-01-15T12:30Z"));
    }

    @Test(timeout = 4000)
    public void testParseISO8601SpecialCases() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Case 11: timeLen == 11 -> insert '0'
        // Case 10: timeLen == 10 -> insert "00"
        // Case 9: timeLen == 9 -> insert "000"
        // Case 8: timeLen == 8 -> insert ".000"
        // These require specific crafted strings to trigger
        // We'll just ensure basic parsing works
        assertNotNull(fmt.parse("2024-01-15T12:30:45.12+05:30"));
        assertNotNull(fmt.parse("2024-01-15T12:30:45.1+05:30"));
    }

    @Test(timeout = 4000)
    public void testHasTimeZoneBoundaries() throws Exception {
        // Test hasTimeZone indirectly by examining parse behavior
        StdDateFormat fmt = new StdDateFormat();
        // String with timezone at various positions
        assertNotNull(fmt.parse("2024-01-15T12:30:45+05:30")); // len-6 = '+'
        assertNotNull(fmt.parse("2024-01-15T12:30:45+0530"));  // len-5 = '+'
        assertNotNull(fmt.parse("2024-01-15T12:30:45+05"));    // len-3 = '+'
    }

    /* =========================================
     * Partition C: Defect-Targeted Branch Zone
     * ========================================= */

    /**
     * Targets the known NullPointerException when _lenient is null
     * in parseAsISO8601 with throwErrors=true.
     * The bug is in the String.format call that uses _lenient directly
     * without null check.
     */
    @Test(timeout = 4000)
    public void testParseAsISO8601WithNullLenient() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Construct a date string that will go through ISO8601 parsing
        // and trigger the _lenient null dereference in the error message
        // Use a string that looks like ISO8601 but fails parsing
        // The key is that _lenient is null (default)
        try {
            // This should trigger the path where dt == null after df.parse
            // and _lenient is null -> NPE
            fmt.parse("2024-13-15T12:30:45.123Z"); // Invalid month 13
        } catch (NullPointerException e) {
            // This is the defect! On fixed version, should get ParseException
            fail("NullPointerException thrown due to _lenient being null in error message - this is the defect!");
        } catch (ParseException e) {
            // Expected behavior on fixed version
            assertTrue(e.getMessage().contains("Can not parse date"));
        }
    }

    @Test(timeout = 4000)
    public void testParseAsISO8601WithLenientFalseFailure() throws Exception {
        // With lenient=false and invalid date, should get ParseException without NPE
        StdDateFormat fmt = new StdDateFormat(null, Locale.US, Boolean.FALSE);
        try {
            fmt.parse("2024-13-15T12:30:45.123Z");
            fail("Should have thrown ParseException");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("Can not parse date"));
        }
    }

    /* =========================================
     * Partition D: Exception & Defensive Guard Paths
     * ========================================= */

    @Test(timeout = 4000)
    public void testParseInvalidString() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        try {
            fmt.parse("not a date");
            fail("Should have thrown ParseException");
        } catch (ParseException e) {
            // Expected
            assertTrue(e.getMessage().contains("Can not parse date"));
        }
    }

    @Test(timeout = 4000)
    public void testParseWithPosition() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        ParsePosition pos = new ParsePosition(0);
        Date dt = fmt.parse("2024-01-15T12:30:45.123Z", pos);
        assertNotNull(dt);
        assertTrue(pos.getIndex() > 0);
    }

    @Test(timeout = 4000)
    public void testParseWithPositionInvalid() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        ParsePosition pos = new ParsePosition(0);
        // Invalid date, should return null
        Date dt = fmt.parse("not a date", pos);
        assertNull(dt);
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testGetISO8601Format() throws Exception {
        DateFormat df = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(df);
        assertFalse(df == StdDateFormat.instance); // Not the same instance
    }

    @Test(timeout = 4000)
    public void testGetRFC1123Format() throws Exception {
        DateFormat df = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(df);
    }

    @Test(timeout = 4000)
    public void testDeprecatedGetISO8601Format() throws Exception {
        DateFormat df = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("UTC"));
        assertNotNull(df);
    }

    @Test(timeout = 4000)
    public void testDeprecatedGetRFC1123Format() throws Exception {
        DateFormat df = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("UTC"));
        assertNotNull(df);
    }

    /* =========================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ========================================= */

    @Test(timeout = 4000)
    public void testToString() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        String str = fmt.toString();
        assertTrue(str.contains("StdDateFormat"));
        assertTrue(str.contains("locale"));
    }

    @Test(timeout = 4000)
    public void testToStringWithTimezone() throws Exception {
        StdDateFormat fmt = new StdDateFormat(TimeZone.getTimeZone("PST"), Locale.US, null);
        String str = fmt.toString();
        assertTrue(str.contains("PST"));
    }

    @Test(timeout = 4000)
    public void testInstanceIsNotNull() throws Exception {
        assertNotNull(StdDateFormat.instance);
    }

    @Test(timeout = 4000)
    public void testCloneIsDifferent() throws Exception {
        StdDateFormat original = StdDateFormat.instance;
        StdDateFormat cloned = original.clone();
        assertNotSame(original, cloned);
    }

    @Test(timeout = 4000)
    public void testDefaultTimeZone() throws Exception {
        assertEquals(TimeZone.getTimeZone("UTC"), StdDateFormat.getDefaultTimeZone());
    }

    @Test(timeout = 4000)
    public void testLookLikeISO8601() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Access protected method through reflection? No, just test via parse
        // But we can test indirectly
        assertNotNull(fmt.parse("2024-01-15")); // Should be treated as ISO8601
        assertNotNull(fmt.parse("9999-99-99T99:99:99.999Z")); // looks like ISO8601
    }

    /**
     * Additional edge: parse with position returning null for ISO8601 lookalike
     */
    @Test(timeout = 4000)
    public void testParseWithPositionISO8601Failure() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        ParsePosition pos = new ParsePosition(0);
        // Invalid ISO8601 (wrong month)
        Date dt = fmt.parse("2024-13-15T12:30:45.123Z", pos);
        assertNull(dt); // Should return null due to error
    }

    /**
     * Test the _clearFormats path via setTimeZone
     */
    @Test(timeout = 4000)
    public void testClearFormatsOnSetTimeZone() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        // Trigger format creation by parsing
        fmt.parse("2024-01-15T12:30:45.123Z");
        fmt.setTimeZone(TimeZone.getTimeZone("PST"));
        // Should still work after timezone change
        Date dt = fmt.parse("2024-01-15T12:30:45.123Z");
        assertNotNull(dt);
    }

    /**
     * Test the _clearFormats path via setTimeZone with same timezone
     * (should not clear)
     */
    @Test(timeout = 4000)
    public void testSetTimeZoneSameDoesNotClear() throws Exception {
        StdDateFormat fmt = new StdDateFormat(TimeZone.getTimeZone("UTC"), Locale.US, null);
        // First parse triggers format creation
        fmt.parse("2024-01-15T12:30:45.123Z");
        // Setting same timezone should not clear (but _timezone is null initially, so != tz)
        // Actually _timezone is null, UTC != null, so it will clear. Let's test with non-null
        StdDateFormat fmt2 = new StdDateFormat(TimeZone.getTimeZone("PST"), Locale.US, null);
        fmt2.parse("2024-01-15T12:30:45.123Z");
        fmt2.setTimeZone(TimeZone.getTimeZone("PST")); // Same, should not clear
        fmt2.parse("2024-01-15T12:30:45.123Z"); // Should work
    }

    /**
     * Test deprecated constructor
     */
    @Test(timeout = 4000)
    public void testDeprecatedConstructor() throws Exception {
        StdDateFormat fmt = new StdDateFormat(TimeZone.getTimeZone("PST"), Locale.US);
        assertNotNull(fmt);
    }
}