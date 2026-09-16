package org.joda.time.format;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for DateTimeFormatter with focus on the known defect
 * where parseInto ignores the formatter's default year when parsing month/day without year.
 * 
 * Branch & Defect Analysis Matrix:
 * - A: Core state transitions (withLocale, withZone, withChronology, withPivotYear, withDefaultYear)
 * - B: Boundary/edge cases (null parameters, empty strings, out-of-range values)
 * - C: Defect-targeted: parseInto with default year override vs instant's year (fails for Feb 29)
 * - D: Exception paths (null inputs, unsupported operations, invalid text)
 * - E: Object immutability and getter correctness
 */
public class DateTimeFormatterDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core functional logic and state transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorDefaults() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        assertNull(f.getPrinter());
        assertNull(f.getParser());
        assertFalse(f.isPrinter());
        assertFalse(f.isParser());
        assertNull(f.getLocale());
        assertFalse(f.isOffsetParsed());
        assertNull(f.getChronology());
        assertNull(f.getZone());
        assertNull(f.getPivotYear());
        assertEquals(2000, f.getDefaultYear());
    }

    @Test(timeout = 4000)
    public void testWithLocaleSame() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        DateTimeFormatter f2 = f.withLocale(null);
        assertSame(f, f2);
        f2 = f.withLocale(Locale.US);
        assertNotSame(f, f2);
        assertEquals(Locale.US, f2.getLocale());
        assertNull(f.getLocale());
    }

    @Test(timeout = 4000)
    public void testWithZoneReplace() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        DateTimeFormatter f2 = f.withZone(DateTimeZone.UTC);
        assertNotSame(f, f2);
        assertEquals(DateTimeZone.UTC, f2.getZone());
        assertNull(f.getZone());
        // withZoneUTC delegate
        DateTimeFormatter f3 = f.withZoneUTC();
        assertEquals(DateTimeZone.UTC, f3.getZone());
    }

    @Test(timeout = 4000)
    public void testWithOffsetParsed() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        DateTimeFormatter f2 = f.withOffsetParsed();
        assertNotSame(f, f2);
        assertTrue(f2.isOffsetParsed());
        assertFalse(f.isOffsetParsed());
        // idempotent
        assertSame(f2, f2.withOffsetParsed());
    }

    @Test(timeout = 4000)
    public void testWithChronologyOverride() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        Chronology iso = ISOChronology.getInstanceUTC();
        DateTimeFormatter f2 = f.withChronology(iso);
        assertNotSame(f, f2);
        assertSame(iso, f2.getChronology());
        assertNull(f.getChronology());
        // null clears it
        DateTimeFormatter f3 = f2.withChronology(null);
        assertNull(f3.getChronology());
    }

    @Test(timeout = 4000)
    public void testWithPivotYear() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        DateTimeFormatter f2 = f.withPivotYear(2000);
        assertNotSame(f, f2);
        assertEquals(Integer.valueOf(2000), f2.getPivotYear());
        DateTimeFormatter f3 = f.withPivotYear((Integer) null);
        assertNull(f3.getPivotYear());
        // int version
        DateTimeFormatter f4 = f.withPivotYear(2010);
        assertEquals(Integer.valueOf(2010), f4.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testWithDefaultYear() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        assertEquals(2000, f.getDefaultYear());
        DateTimeFormatter f2 = f.withDefaultYear(2004);
        assertNotSame(f, f2);
        assertEquals(2004, f2.getDefaultYear());
        // idempotent? Actually it always creates new object (no check)
        DateTimeFormatter f3 = f2.withDefaultYear(2004);
        assertNotSame(f2, f3);
        assertEquals(2004, f3.getDefaultYear());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary value analysis (BVA) and extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullLocaleAndZone() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        assertNotNull(f.withLocale(null));
        assertNotNull(f.withZone(null));
        assertNotNull(f.withChronology(null));
    }

    @Test(timeout = 4000)
    public void testGetChronolgyDeprecated() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        assertNull(f.getChronolgy()); // deprecated but still present
    }

    @Test(timeout = 4000)
    public void testIsPrinterAndParser() {
        DateTimePrinter printer = DateTimeFormat.forPattern("yyyy").getPrinter();
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy").getParser();
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        assertTrue(f.isPrinter());
        assertTrue(f.isParser());
        f = new DateTimeFormatter(null, parser);
        assertFalse(f.isPrinter());
        assertTrue(f.isParser());
        f = new DateTimeFormatter(printer, null);
        assertTrue(f.isPrinter());
        assertFalse(f.isParser());
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-targeted branch – parseInto with default year
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParseInto_monthDay_feb29_withDefaultYear() {
        // This test reveals the defect: parseInto ignores formatter's default year.
        // Create formatter that parses month and day only.
        DateTimeParser parser = DateTimeFormat.forPattern("M d").getParser();
        DateTimePrinter printer = DateTimeFormat.forPattern("M d").getPrinter();
        DateTimeFormatter f = new DateTimeFormatter(printer, parser)
                .withDefaultYear(2000); // 2000 is a leap year

        // Instant is in a non-leap year (2001)
        MutableDateTime dt = new MutableDateTime(2001, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        // Parse "2 29" – month 2, day 29
        int result = f.parseInto(dt, "2 29", 0);
        assertTrue("Parse should succeed", result >= 0);
        // After successful parse, year should be 2000 (default year from formatter)
        assertEquals(2000, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(29, dt.getDayOfMonth());
        // If the defect is present, an IllegalFieldValueException is thrown here.
    }

    @Test(timeout = 4000)
    public void testParseInto_monthDay_feb29_newYork_startOfYear() {
        // Additional defect test with timezone and chronology variations
        DateTimeParser parser = DateTimeFormat.forPattern("M d").getParser();
        DateTimePrinter printer = DateTimeFormat.forPattern("M d").getPrinter();
        DateTimeFormatter f = new DateTimeFormatter(printer, parser)
                .withDefaultYear(2000)
                .withZone(DateTimeZone.forID("America/New_York"));

        MutableDateTime dt = new MutableDateTime(2001, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int result = f.parseInto(dt, "2 29", 0);
        assertTrue("Parse should succeed", result >= 0);
        assertEquals(2000, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(29, dt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseInto_monthDay_feb29_tokyo_endOfYear() {
        DateTimeParser parser = DateTimeFormat.forPattern("M d").getParser();
        DateTimePrinter printer = DateTimeFormat.forPattern("M d").getPrinter();
        DateTimeFormatter f = new DateTimeFormatter(printer, parser)
                .withDefaultYear(2000)
                .withZone(DateTimeZone.forID("Asia/Tokyo"));

        // Use an instant near end of year to test default year selection
        MutableDateTime dt = new MutableDateTime(2001, 12, 31, 23, 59, 59, 0, DateTimeZone.UTC);
        int result = f.parseInto(dt, "2 29", 0);
        assertTrue("Parse should succeed", result >= 0);
        assertEquals(2000, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(29, dt.getDayOfMonth());
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception and defensive guard paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testPrintWithoutPrinter() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        f.print(new DateTime());
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testParseWithoutParser() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        f.parseMillis("2020-01-01");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintToNullPartial() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        f.printTo(new StringBuffer(), (ReadablePartial) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseIntoNullInstant() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        f.parseInto(null, "text", 0);
    }

    @Test(timeout = 4000)
    public void testParseMillisInvalid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        try {
            f.parseMillis("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("invalid"));
        }
    }

    @Test(timeout = 4000)
    public void testParseDateTimeInvalid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        try {
            f.parseDateTime("2020-13-01");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test(timeout = 4000)
    public void testParseLocalDateTime_complete() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = f.parseLocalDateTime("2020-02-29T12:00:00");
        assertEquals(2020, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(29, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(0, ldt.getMinuteOfHour());
        assertEquals(0, ldt.getSecondOfMinute());
    }

    // -----------------------------------------------------------------------
    // Partition E: Object lifecycle and contract integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testImmutabilityViaWithMethods() {
        DateTimeFormatter base = new DateTimeFormatter(null, null);
        DateTimeFormatter changed = base.withLocale(Locale.GERMAN)
                                        .withZone(DateTimeZone.forID("Europe/Berlin"))
                                        .withChronology(ISOChronology.getInstanceUTC())
                                        .withOffsetParsed()
                                        .withPivotYear(2050)
                                        .withDefaultYear(1996);
        // Original unchanged
        assertNull(base.getLocale());
        assertNull(base.getZone());
        assertNull(base.getChronology());
        assertFalse(base.isOffsetParsed());
        assertNull(base.getPivotYear());
        assertEquals(2000, base.getDefaultYear());
        // Changed has new values
        assertEquals(Locale.GERMAN, changed.getLocale());
        assertEquals(DateTimeZone.forID("Europe/Berlin"), changed.getZone());
        assertEquals(ISOChronology.getInstanceUTC(), changed.getChronology());
        assertTrue(changed.isOffsetParsed());
        assertEquals(Integer.valueOf(2050), changed.getPivotYear());
        assertEquals(1996, changed.getDefaultYear());
    }

    // -----------------------------------------------------------------------
    // Additional coverage for print/parse methods with real formatter
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintTo_ReadableInstant() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = new DateTime(2020, 2, 29, 0, 0, DateTimeZone.UTC);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, dt);
        assertEquals("2020-02-29", buf.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTo_long() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, 0L); // 1970-01-01
        assertEquals("1970-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTo_ReadablePartial() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate ld = new LocalDate(2020, 2, 29);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, ld);
        assertEquals("2020-02-29", buf.toString());
    }

    @Test(timeout = 4000)
    public void testPrint_ReadableInstant_nullMeansNow() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        String result = f.print((ReadableInstant) null);
        assertNotNull(result);
        assertEquals(4, result.length()); // year only
    }

    @Test(timeout = 4000)
    public void testParseMillis_valid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        long millis = f.parseMillis("2020-02-29");
        assertEquals(new DateTime(2020, 2, 29, 0, 0, DateTimeZone.UTC).getMillis(), millis);
    }

    @Test(timeout = 4000)
    public void testParseDateTime_valid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = f.parseDateTime("2020-02-29");
        assertEquals(2020, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(29, dt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseMutableDateTime_valid() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        MutableDateTime mdt = f.parseMutableDateTime("2020-02-29");
        assertEquals(2020, mdt.getYear());
        assertEquals(2, mdt.getMonthOfYear());
        assertEquals(29, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseLocalDate() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate ld = f.parseLocalDate("2020-02-29");
        assertEquals(2020, ld.getYear());
        assertEquals(2, ld.getMonthOfYear());
        assertEquals(29, ld.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseLocalTime() {
        DateTimeFormatter f = DateTimeFormat.forPattern("HH:mm:ss");
        LocalTime lt = f.parseLocalTime("12:30:45");
        assertEquals(12, lt.getHourOfDay());
        assertEquals(30, lt.getMinuteOfHour());
        assertEquals(45, lt.getSecondOfMinute());
    }

    @Test(timeout = 4000)
    public void testPrintTo_Writer() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        java.io.Writer w = new java.io.StringWriter();
        f.printTo(w, new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC));
        assertEquals("2020", w.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTo_Appendable() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        StringBuilder sb = new StringBuilder();
        f.printTo(sb, new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC));
        assertEquals("2020", sb.toString());
    }

    @Test(timeout = 4000)
    public void testParseInto_basic() {
        // Basic parseInto test not related to defect
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int pos = f.parseInto(mdt, "2020-02-29", 0);
        assertTrue(pos > 0);
        assertEquals(2020, mdt.getYear());
        assertEquals(2, mdt.getMonthOfYear());
        assertEquals(29, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseInto_negativePosition() {
        // Actually position can be negative? The method doesn't specify; but it's used internally.
        // We can test out-of-range positions.
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        MutableDateTime mdt = new MutableDateTime();
        // position = -1 is invalid; the underlying parser may throw.
        f.parseInto(mdt, "text", -1);
    }
}