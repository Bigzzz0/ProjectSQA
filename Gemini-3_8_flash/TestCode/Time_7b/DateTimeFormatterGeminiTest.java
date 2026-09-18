package org.joda.time.format;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.joda.time.format.DateTimeFormatter
 *
 * 1. Defects4J Ground Truth Defect:
 *    - parseInto calculates defaultYear using chrono.year().get(instantLocal). When instant
 *      has a zone with a negative/positive offset relative to UTC at year boundaries (e.g. America/New_York
 *      start-of-year or Asia/Tokyo end-of-year), instantLocal causes an unintended year shift when queried
 *      against a zoned chronology, forcing defaultYear to a non-leap year (e.g. 2003 or 2005 instead of 2004),
 *      causing Feb 29 parsing to fail with IllegalFieldValueException.
 *
 * 2. Structural Branch & Condition Coverage:
 *    - Printer/Parser capabilities: isPrinter(), isParser(), requirePrinter(), requireParser() (null guards).
 *    - Decorator immutability & identity checks:
 *      * withLocale: identical reference, equal object, new object, null.
 *      * withOffsetParsed: true -> true identity check, transition from false -> true (sets zone to null).
 *      * withChronology: identical reference, changed reference, null override.
 *      * withZone & withZoneUTC: identical reference, null zone, override zone takes precedence.
 *      * withPivotYear: Integer vs int overload, identical value check, override parsing logic.
 *      * withDefaultYear: custom year applied to parsing dates without year.
 *    - Output Overflows in printTo(StringBuffer/Writer, long, Chronology):
 *      * (instant ^ adjustedInstant) < 0 && (instant ^ offset) >= 0 (e.g. Long.MAX_VALUE with positive offset).
 *    - Output targets: StringBuffer, Writer (IOException handling), Appendable (StringBuilder).
 *    - Parsing variants:
 *      * parseInto: success, failure (~position returned), null instant, zone/offset mutations.
 *      * parseMillis, parseLocalDate, parseLocalTime, parseLocalDateTime, parseDateTime, parseMutableDateTime.
 *      * Incomplete parse or trailing characters (throwing IllegalArgumentException).
 */
public class DateTimeFormatterGeminiTest {

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParseInto_monthDay_feb29_newYork_startOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        MutableDateTime result = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, zone);
        int pos = f.parseInto(result, "2 29", 0);
        assertEquals(4, pos);
        assertEquals(new MutableDateTime(2004, 2, 29, 0, 0, 0, 0, zone), result);
    }

    @Test(timeout = 4000)
    public void testParseInto_monthDay_feb29_tokyo_endOfYear() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
        DateTimeZone zone = DateTimeZone.forID("Asia/Tokyo");
        MutableDateTime result = new MutableDateTime(2004, 12, 31, 23, 59, 59, 999, zone);
        int pos = f.parseInto(result, "2 29", 0);
        assertEquals(4, pos);
        assertEquals(new MutableDateTime(2004, 2, 29, 23, 59, 59, 999, zone), result);
    }

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrinterAndParserCapabilityInspection() {
        DateTimeFormatter fBoth = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertTrue(fBoth.isPrinter());
        assertNotNull(fBoth.getPrinter());
        assertTrue(fBoth.isParser());
        assertNotNull(fBoth.getParser());

        DateTimeFormatter fPrintOnly = new DateTimeFormatter(fBoth.getPrinter(), null);
        assertTrue(fPrintOnly.isPrinter());
        assertNotNull(fPrintOnly.getPrinter());
        assertFalse(fPrintOnly.isParser());
        assertNull(fPrintOnly.getParser());

        DateTimeFormatter fParseOnly = new DateTimeFormatter(null, fBoth.getParser());
        assertFalse(fParseOnly.isPrinter());
        assertNull(fParseOnly.getPrinter());
        assertTrue(fParseOnly.isParser());
        assertNotNull(fParseOnly.getParser());
    }

    @Test(timeout = 4000)
    public void testWithLocaleTransitions() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MMM-dd");
        assertNull(f.getLocale());

        DateTimeFormatter fUs = f.withLocale(Locale.US);
        assertEquals(Locale.US, fUs.getLocale());
        assertSame(fUs, fUs.withLocale(Locale.US));
        assertSame(fUs, fUs.withLocale(new Locale("en", "US")));

        DateTimeFormatter fFr = fUs.withLocale(Locale.FRANCE);
        assertEquals(Locale.FRANCE, fFr.getLocale());
        assertNotSame(fUs, fFr);

        DateTimeFormatter fBackToNull = fFr.withLocale(null);
        assertNull(fBackToNull.getLocale());
        assertSame(fBackToNull, fBackToNull.withLocale(null));
    }

    @Test(timeout = 4000)
    public void testWithOffsetParsedTransitions() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ");
        assertFalse(f.isOffsetParsed());
        assertNull(f.getZone());

        DateTimeFormatter fOffset = f.withOffsetParsed();
        assertTrue(fOffset.isOffsetParsed());
        assertNull(fOffset.getZone());
        assertSame(fOffset, fOffset.withOffsetParsed());

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeFormatter fWithZone = fOffset.withZone(london);
        assertFalse(fWithZone.isOffsetParsed());
        assertEquals(london, fWithZone.getZone());

        DateTimeFormatter fBackToOffset = fWithZone.withOffsetParsed();
        assertTrue(fBackToOffset.isOffsetParsed());
        assertNull(fBackToOffset.getZone());
    }

    @Test(timeout = 4000)
    public void testWithChronologyTransitions() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNull(f.getChronology());
        assertNull(f.getChronolgy());

        Chronology coptic = CopticChronology.getInstanceUTC();
        DateTimeFormatter fCoptic = f.withChronology(coptic);
        assertSame(coptic, fCoptic.getChronology());
        assertSame(coptic, fCoptic.getChronolgy());
        assertSame(fCoptic, fCoptic.withChronology(coptic));

        DateTimeFormatter fISO = fCoptic.withChronology(ISOChronology.getInstanceUTC());
        assertEquals(ISOChronology.getInstanceUTC(), fISO.getChronology());

        DateTimeFormatter fReset = fISO.withChronology(null);
        assertNull(fReset.getChronology());
        assertSame(fReset, fReset.withChronology(null));
    }

    @Test(timeout = 4000)
    public void testWithZoneAndWithZoneUTC() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNull(f.getZone());

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeFormatter fParis = f.withZone(paris);
        assertSame(paris, fParis.getZone());
        assertSame(fParis, fParis.withZone(paris));

        DateTimeFormatter fUtc = fParis.withZoneUTC();
        assertEquals(DateTimeZone.UTC, fUtc.getZone());
        assertSame(fUtc, fUtc.withZone(DateTimeZone.UTC));
        assertSame(fUtc, fUtc.withZoneUTC());

        DateTimeFormatter fNullZone = fUtc.withZone(null);
        assertNull(fNullZone.getZone());
        assertSame(fNullZone, fNullZone.withZone(null));
    }

    @Test(timeout = 4000)
    public void testWithPivotYearTransitions() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yy-MM-dd");
        assertNull(f.getPivotYear());

        DateTimeFormatter f2020Obj = f.withPivotYear(Integer.valueOf(2020));
        assertEquals(Integer.valueOf(2020), f2020Obj.getPivotYear());
        assertSame(f2020Obj, f2020Obj.withPivotYear(Integer.valueOf(2020)));
        assertSame(f2020Obj, f2020Obj.withPivotYear(2020));

        DateTimeFormatter f1950Prim = f2020Obj.withPivotYear(1950);
        assertEquals(Integer.valueOf(1950), f1950Prim.getPivotYear());

        DateTimeFormatter fNullPivot = f1950Prim.withPivotYear(null);
        assertNull(fNullPivot.getPivotYear());
        assertSame(fNullPivot, fNullPivot.withPivotYear(null));
    }

    @Test(timeout = 4000)
    public void testWithDefaultYearTransitions() {
        DateTimeFormatter f = DateTimeFormat.forPattern("MM-dd");
        assertEquals(2000, f.getDefaultYear());

        DateTimeFormatter f2016 = f.withDefaultYear(2016);
        assertEquals(2016, f2016.getDefaultYear());

        DateTime parsed = f2016.withZoneUTC().parseDateTime("02-29");
        assertEquals(2016, parsed.getYear());
        assertEquals(2, parsed.getMonthOfYear());
        assertEquals(29, parsed.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPrintingReadableInstantAllTargets() throws IOException {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
        Instant instant = new Instant(0L);

        // String print(ReadableInstant)
        assertEquals("1970-01-01 00:00:00", f.print(instant));

        // printTo(StringBuffer, ReadableInstant)
        StringBuffer sb = new StringBuffer("Prefix: ");
        f.printTo(sb, instant);
        assertEquals("Prefix: 1970-01-01 00:00:00", sb.toString());

        // printTo(Writer, ReadableInstant)
        StringWriter sw = new StringWriter();
        f.printTo(sw, instant);
        assertEquals("1970-01-01 00:00:00", sw.toString());

        // printTo(Appendable, ReadableInstant)
        StringBuilder appendable = new StringBuilder("Log: ");
        f.printTo((Appendable) appendable, instant);
        assertEquals("Log: 1970-01-01 00:00:00", appendable.toString());

        // Null instant tests DateTimeUtils.getInstantMillis(null) (current time)
        String nowStr = f.print((ReadableInstant) null);
        assertNotNull(nowStr);
        assertTrue(nowStr.length() > 0);

        StringBuffer sbNull = new StringBuffer();
        f.printTo(sbNull, (ReadableInstant) null);
        assertEquals(nowStr, sbNull.toString());

        StringWriter swNull = new StringWriter();
        f.printTo(swNull, (ReadableInstant) null);
        assertEquals(nowStr, swNull.toString());

        StringBuilder abNull = new StringBuilder();
        f.printTo((Appendable) abNull, (ReadableInstant) null);
        assertEquals(nowStr, abNull.toString());
    }

    @Test(timeout = 4000)
    public void testPrintingMillisAllTargets() throws IOException {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
        long millis = 1000000000000L; // 2001-09-09T01:46:40.000Z

        assertEquals("2001-09-09 01:46:40", f.print(millis));

        StringBuffer sb = new StringBuffer();
        f.printTo(sb, millis);
        assertEquals("2001-09-09 01:46:40", sb.toString());

        StringWriter sw = new StringWriter();
        f.printTo(sw, millis);
        assertEquals("2001-09-09 01:46:40", sw.toString());

        StringBuilder ab = new StringBuilder();
        f.printTo((Appendable) ab, millis);
        assertEquals("2001-09-09 01:46:40", ab.toString());
    }

    @Test(timeout = 4000)
    public void testPrintingReadablePartialAllTargets() throws IOException {
        DateTimeFormatter f = DateTimeFormat.forPattern("MM/dd");
        LocalDate date = new LocalDate(2020, 12, 25);

        assertEquals("12/25", f.print(date));

        StringBuffer sb = new StringBuffer();
        f.printTo(sb, date);
        assertEquals("12/25", sb.toString());

        StringWriter sw = new StringWriter();
        f.printTo(sw, date);
        assertEquals("12/25", sw.toString());

        StringBuilder ab = new StringBuilder();
        f.printTo((Appendable) ab, date);
        assertEquals("12/25", ab.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalTypes() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        LocalDate date = f.parseLocalDate("2021-07-15 14:30:00");
        assertEquals(new LocalDate(2021, 7, 15), date);

        LocalTime time = f.parseLocalTime("2021-07-15 14:30:00");
        assertEquals(new LocalTime(14, 30, 0), time);

        LocalDateTime dateTime = f.parseLocalDateTime("2021-07-15 14:30:00");
        assertEquals(new LocalDateTime(2021, 7, 15, 14, 30, 0), dateTime);
    }

    @Test(timeout = 4000)
    public void testParseDateTimeAndMutableDateTime() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(zone);

        DateTime dt = f.parseDateTime("2022-03-10 12:00:00");
        assertEquals(2022, dt.getYear());
        assertEquals(3, dt.getMonthOfYear());
        assertEquals(10, dt.getDayOfMonth());
        assertEquals(12, dt.getHourOfDay());
        assertEquals(zone, dt.getZone());

        MutableDateTime mdt = f.parseMutableDateTime("2022-03-10 12:00:00");
        assertEquals(dt.getMillis(), mdt.getMillis());
        assertEquals(zone, mdt.getZone());

        long millis = f.parseMillis("2022-03-10 12:00:00");
        assertEquals(dt.getMillis(), millis);
    }

    @Test(timeout = 4000)
    public void testParseWithZoneAndOffsetParsedCombinations() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");

        // Parse with default behavior (parsed zone retained)
        DateTime dt1 = f.parseDateTime("2020-01-01 12:00:00 +0200");
        assertEquals(DateTimeZone.forOffsetHours(2), dt1.getZone());

        // Parse with offset parsed enabled
        DateTime dt2 = f.withOffsetParsed().parseDateTime("2020-01-01 12:00:00 -0500");
        assertEquals(DateTimeZone.forOffsetHours(-5), dt2.getZone());

        // Parse with override zone
        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");
        DateTime dt3 = f.withZone(tokyo).parseDateTime("2020-01-01 12:00:00 +0000");
        assertEquals(tokyo, dt3.getZone());
        assertEquals(21, dt3.getHourOfDay()); // 12:00 UTC -> 21:00 Tokyo

        // Parse local date time with parsed offset
        LocalDateTime ldt = f.parseLocalDateTime("2020-01-01 12:00:00 +0500");
        assertEquals(new LocalDateTime(2020, 1, 1, 12, 0, 0), ldt);
    }

    @Test(timeout = 4000)
    public void testParseIntoModificationsAndOverrides() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
        MutableDateTime mdt = new MutableDateTime(0L, DateTimeZone.forOffsetHours(5));

        int nextPos = f.parseInto(mdt, "2015-06-20", 0);
        assertEquals(10, nextPos);
        assertEquals(DateTimeZone.UTC, mdt.getZone());
        assertEquals(2015, mdt.getYear());
        assertEquals(6, mdt.getMonthOfYear());
        assertEquals(20, mdt.getDayOfMonth());

        // Partial match failure
        int failPos = f.parseInto(mdt, "invalid-date", 0);
        assertTrue(failPos < 0);
        assertEquals(0, ~failPos);
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintToOverflowAdjustedInstant() throws IOException {
        // Target branch: (instant ^ adjustedInstant) < 0 && (instant ^ offset) >= 0
        DateTimeZone plusTwo = DateTimeZone.forOffsetHours(2);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy").withZone(plusTwo);

        long nearMax = Long.MAX_VALUE - 1000L;
        String resultStr = f.print(nearMax);
        assertNotNull(resultStr);

        StringBuffer sb = new StringBuffer();
        f.printTo(sb, nearMax);
        assertEquals(resultStr, sb.toString());

        StringWriter sw = new StringWriter();
        f.printTo(sw, nearMax);
        assertEquals(resultStr, sw.toString());
    }

    @Test(timeout = 4000)
    public void testTwoDigitYearParsingWithPivotYearBoundaries() {
        DateTimeFormatter fBase = DateTimeFormat.forPattern("yy-MM-dd").withZoneUTC();

        // Range with pivot 2000 is 1950..2049
        DateTimeFormatter f2000 = fBase.withPivotYear(2000);
        assertEquals(2049, f2000.parseDateTime("49-01-01").getYear());
        assertEquals(1950, f2000.parseDateTime("50-01-01").getYear());

        // Range with pivot 1950 is 1900..1999
        DateTimeFormatter f1950 = fBase.withPivotYear(1950);
        assertEquals(1900, f1950.parseDateTime("00-01-01").getYear());
        assertEquals(1999, f1950.parseDateTime("99-01-01").getYear());
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequirePrinterThrowsWhenNullPrinterOnPrintInstant() {
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, DateTimeFormat.forPattern("yyyy").getParser());
        parserOnly.print(new Instant());
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequirePrinterThrowsWhenNullPrinterOnPrintMillis() {
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, DateTimeFormat.forPattern("yyyy").getParser());
        parserOnly.print(123456789L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequirePrinterThrowsWhenNullPrinterOnPrintPartial() {
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, DateTimeFormat.forPattern("yyyy").getParser());
        parserOnly.print(new LocalDate());
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequirePrinterThrowsWhenNullPrinterOnPrintToBuffer() {
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, DateTimeFormat.forPattern("yyyy").getParser());
        parserOnly.printTo(new StringBuffer(), 0L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequirePrinterThrowsWhenNullPrinterOnPrintToWriter() throws IOException {
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, DateTimeFormat.forPattern("yyyy").getParser());
        parserOnly.printTo(new StringWriter(), 0L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequireParserThrowsWhenNullParserOnParseMillis() {
        DateTimeFormatter printOnly = new DateTimeFormatter(DateTimeFormat.forPattern("yyyy").getPrinter(), null);
        printOnly.parseMillis("2020");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequireParserThrowsWhenNullParserOnParseDateTime() {
        DateTimeFormatter printOnly = new DateTimeFormatter(DateTimeFormat.forPattern("yyyy").getPrinter(), null);
        printOnly.parseDateTime("2020");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequireParserThrowsWhenNullParserOnParseMutableDateTime() {
        DateTimeFormatter printOnly = new DateTimeFormatter(DateTimeFormat.forPattern("yyyy").getPrinter(), null);
        printOnly.parseMutableDateTime("2020");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequireParserThrowsWhenNullParserOnParseLocalDateTime() {
        DateTimeFormatter printOnly = new DateTimeFormatter(DateTimeFormat.forPattern("yyyy").getPrinter(), null);
        printOnly.parseLocalDateTime("2020");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequireParserThrowsWhenNullParserOnParseInto() {
        DateTimeFormatter printOnly = new DateTimeFormatter(DateTimeFormat.forPattern("yyyy").getPrinter(), null);
        printOnly.parseInto(new MutableDateTime(), "2020", 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintToNullPartialThrowsIllegalArgument() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.printTo(new StringBuffer(), (ReadablePartial) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintToWriterNullPartialThrowsIllegalArgument() throws IOException {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.printTo(new StringWriter(), (ReadablePartial) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseIntoNullInstantThrowsIllegalArgument() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseInto(null, "2020-01-01", 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseMillisInvalidTextThrows() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseMillis("not-a-date");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseMillisIncompleteParseThrows() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseMillis("2020-01-01 trailing characters");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseDateTimeInvalidTextThrows() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseDateTime("2020/01/01");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseLocalDateTimeInvalidTextThrows() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseLocalDateTime("2020-99-99");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseMutableDateTimeIncompleteParseThrows() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        f.parseMutableDateTime("2020-01-01 extra text");
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Format Precedence
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testZonePrecedenceOverChronologyZone() {
        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        Chronology copticInLondon = CopticChronology.getInstance(london);

        // Setting chronology with London zone, but override zone with Tokyo
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .withChronology(copticInLondon)
                .withZone(tokyo);

        DateTime parsed = f.parseDateTime("1737-01-01 10:00:00");
        assertEquals(tokyo, parsed.getZone());
        assertTrue(parsed.getChronology() instanceof CopticChronology);
    }

    @Test(timeout = 4000)
    public void testPrintUsesInstantChronologyWhenNotOverridden() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        DateTime dt = new DateTime(2000, 1, 1, 0, 0, 0, buddhist);

        // Gregorian year 2000 is Buddhist year 2543
        String printed = f.print(dt);
        assertEquals("2543-01-01", printed);

        // If override chronology is set, override takes precedence
        DateTimeFormatter fIso = f.withChronology(ISOChronology.getInstanceUTC());
        assertEquals("2000-01-01", fIso.print(dt));
    }
}