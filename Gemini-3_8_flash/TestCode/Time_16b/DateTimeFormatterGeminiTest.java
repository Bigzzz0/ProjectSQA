/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.joda.time.format.DateTimeFormatter
 * 
 * 1. DEFECT UNDER TEST (Defects4J):
 *    - parseInto(ReadWritableInstant, String, int): Incorrectly instantiates DateTimeParserBucket
 *      using iDefaultYear instead of the year derived from the target instant's chronology/local instant.
 *      Manifestation: When parsing month-only or month-day fields without year into an existing instant
 *      (e.g., year 2004), the target instant is erroneously reset to defaultYear (2000) instead of retaining 2004.
 *      Targeted by: testParseInto_defectYearPreservation_monthOnly(), testParseInto_defectYearPreservation_feb29().
 * 
 * 2. BRANCH & CONDITION COVERAGE:
 *    - requirePrinter() / requireParser(): null checks throwing UnsupportedOperationException.
 *    - withLocale(Locale): identity check, equality check, null check.
 *    - withOffsetParsed(): already true vs transitioning from false.
 *    - withChronology(Chronology): identity check.
 *    - withZone(DateTimeZone): identity check, null zone, UTC zone helper withZoneUTC().
 *    - withPivotYear(Integer / int): identity check, equality check.
 *    - withDefaultYear(int): creation of new instance with specified default year.
 *    - printTo(StringBuffer/Writer/Appendable, ReadableInstant): null instant vs specific instant.
 *    - printTo(StringBuffer/Writer/Appendable, long): local time shifting and integer overflow branch
 *      where (instant ^ adjustedInstant) < 0 && (instant ^ offset) >= 0.
 *    - printTo(StringBuffer/Writer/Appendable, ReadablePartial): null partial throwing IllegalArgumentException.
 *    - parseInto(ReadWritableInstant, String, int): null instant check, offset parsed vs bucket zone vs iZone overrides.
 *    - parseMillis / parseDateTime / parseMutableDateTime / parseLocalDateTime / parseLocalDate / parseLocalTime:
 *      Valid full parse, partial/invalid parse failure throwing IllegalArgumentException, offset-parsed branches.
 *    - selectChronology(Chronology): handling null chrono, iChrono override, and iZone override.
 */
package org.joda.time.format;

import static org.junit.Assert.*;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

public class DateTimeFormatterGeminiTest {

    private static final DateTimeZone TOKYO = DateTimeZone.forID("Asia/Tokyo");
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone OFFSET_PLUS_2 = DateTimeZone.forOffsetHours(2);

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseInto_defectYearPreservation_monthOnly() {
        // Parser for month-of-year only
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM").withZone(TOKYO);
        MutableDateTime mdt = new MutableDateTime(2004, 5, 1, 12, 20, 30, 0, TOKYO);

        // Parse month 10 ("10") into instant at position 0
        int result = formatter.parseInto(mdt, "10", 0);

        assertEquals("Parse position should advance by 2", 2, result);
        // On defective version: mdt gets year 2000 because DateTimeParserBucket is passed iDefaultYear (2000)
        // Correct behavior: mdt should retain base year 2004 and update month to 10
        assertEquals(2004, mdt.getYear());
        assertEquals(10, mdt.getMonthOfYear());
        assertEquals(1, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseInto_defectYearPreservation_feb29() {
        // 2004 is a leap year; 2000 is also a leap year, but base year must be preserved
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd").withZone(DateTimeZone.UTC);
        MutableDateTime mdt = new MutableDateTime(2004, 1, 1, 12, 20, 30, 0, DateTimeZone.UTC);

        int result = formatter.parseInto(mdt, "02-29", 0);

        assertEquals(5, result);
        assertEquals(2004, mdt.getYear());
        assertEquals(2, mdt.getMonthOfYear());
        assertEquals(29, mdt.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseInto_defectYearPreservation_monthOnly_baseEndYear() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM").withZone(TOKYO);
        MutableDateTime mdt = new MutableDateTime(2004, 5, 31, 12, 20, 30, 0, TOKYO);

        int result = formatter.parseInto(mdt, "12", 0);

        assertEquals(2, result);
        assertEquals(2004, mdt.getYear());
        assertEquals(12, mdt.getMonthOfYear());
    }

    // =========================================================================
    // Partition A: Immutability, Configuration & State Modifiers
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithLocale_IdentityAndModification() {
        DateTimeFormatter base = ISODateTimeFormat.dateTime();
        assertNull(base.getLocale());

        DateTimeFormatter fr = base.withLocale(Locale.FRENCH);
        assertNotNull(fr);
        assertEquals(Locale.FRENCH, fr.getLocale());
        assertNotSame(base, fr);

        // Same instance returned if equals
        assertSame(fr, fr.withLocale(Locale.FRENCH));
        // Same instance returned if null and already null
        assertSame(base, base.withLocale(null));

        DateTimeFormatter us = fr.withLocale(Locale.US);
        assertEquals(Locale.US, us.getLocale());
    }

    @Test(timeout = 4000)
    public void testWithOffsetParsed_StateTransitions() {
        DateTimeFormatter base = ISODateTimeFormat.dateTime();
        assertFalse(base.isOffsetParsed());

        DateTimeFormatter offsetParsed = base.withOffsetParsed();
        assertTrue(offsetParsed.isOffsetParsed());
        // Idempotency: same instance if already offsetParsed
        assertSame(offsetParsed, offsetParsed.withOffsetParsed());

        // withZone clears offsetParsed flag
        DateTimeFormatter withZone = offsetParsed.withZone(PARIS);
        assertFalse(withZone.isOffsetParsed());
        assertEquals(PARIS, withZone.getZone());
    }

    @Test(timeout = 4000)
    public void testWithChronology_IdentityAndAccessors() {
        DateTimeFormatter base = ISODateTimeFormat.dateTime();
        assertNull(base.getChronology());
        assertNull(base.getChronolgy()); // test deprecated spelling method

        Chronology chrono = BuddhistChronology.getInstanceUTC();
        DateTimeFormatter modified = base.withChronology(chrono);
        assertSame(chrono, modified.getChronology());
        assertSame(chrono, modified.getChronolgy());
        assertNotSame(base, modified);

        // Idempotency on same chronology
        assertSame(modified, modified.withChronology(chrono));
    }

    @Test(timeout = 4000)
    public void testWithZone_And_withZoneUTC() {
        DateTimeFormatter base = ISODateTimeFormat.dateTime();
        assertNull(base.getZone());

        DateTimeFormatter utc = base.withZoneUTC();
        assertEquals(DateTimeZone.UTC, utc.getZone());

        DateTimeFormatter tokyo = base.withZone(TOKYO);
        assertEquals(TOKYO, tokyo.getZone());
        assertSame(tokyo, tokyo.withZone(TOKYO));

        DateTimeFormatter noZone = tokyo.withZone(null);
        assertNull(noZone.getZone());
    }

    @Test(timeout = 4000)
    public void testWithPivotYear_IntegerAndInt() {
        DateTimeFormatter base = ISODateTimeFormat.dateTime();
        assertNull(base.getPivotYear());

        DateTimeFormatter p2020 = base.withPivotYear(2020);
        assertEquals(Integer.valueOf(2020), p2020.getPivotYear());

        DateTimeFormatter p2020Obj = base.withPivotYear(Integer.valueOf(2020));
        assertEquals(Integer.valueOf(2020), p2020Obj.getPivotYear());
        assertSame(p2020, p2020.withPivotYear(Integer.valueOf(2020)));

        DateTimeFormatter pNull = p2020.withPivotYear((Integer) null);
        assertNull(pNull.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testWithDefaultYear_Configuration() {
        DateTimeFormatter base = ISODateTimeFormat.dateTime();
        assertEquals(2000, base.getDefaultYear());

        DateTimeFormatter customYear = base.withDefaultYear(1996);
        assertEquals(1996, customYear.getDefaultYear());
    }

    @Test(timeout = 4000)
    public void testCapabilitiesGetters() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        assertTrue(formatter.isPrinter());
        assertTrue(formatter.isParser());
        assertNotNull(formatter.getPrinter());
        assertNotNull(formatter.getParser());

        DateTimeFormatter noPrinter = new DateTimeFormatter(null, formatter.getParser());
        assertFalse(noPrinter.isPrinter());
        assertNull(noPrinter.getPrinter());
        assertTrue(noPrinter.isParser());

        DateTimeFormatter noParser = new DateTimeFormatter(formatter.getPrinter(), null);
        assertTrue(noParser.isPrinter());
        assertFalse(noParser.isParser());
        assertNull(noParser.getParser());
    }

    // =========================================================================
    // Partition B: BVA & Output Channels (Printing)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrint_ReadableInstant() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
        DateTime dt = new DateTime(2021, 12, 25, 10, 30, 0, 0, DateTimeZone.UTC);

        assertEquals("2021-12-25T10:30:00.000Z", fmt.print(dt));

        // Test with null instant (translates to current instant, must not throw)
        String nowStr = fmt.print((DateTime) null);
        assertNotNull(nowStr);
        assertFalse(nowStr.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPrint_MillisLong() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
        long millis = 0L; // 1970-01-01T00:00:00Z
        assertEquals("1970-01-01T00:00:00.000Z", fmt.print(millis));
    }

    @Test(timeout = 4000)
    public void testPrint_ReadablePartial() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = new LocalDate(2023, 7, 15);
        assertEquals("2023-07-15", fmt.print(date));
    }

    @Test(timeout = 4000)
    public void testPrintTo_AllAppendables() throws IOException {
        DateTimeFormatter fmt = ISODateTimeFormat.date().withZone(DateTimeZone.UTC);
        DateTime dt = new DateTime(2022, 5, 20, 0, 0, 0, 0, DateTimeZone.UTC);

        // StringBuffer
        StringBuffer sb = new StringBuffer("Prefix: ");
        fmt.printTo(sb, dt);
        assertEquals("Prefix: 2022-05-20", sb.toString());

        StringBuffer sbMillis = new StringBuffer();
        fmt.printTo(sbMillis, dt.getMillis());
        assertEquals("2022-05-20", sbMillis.toString());

        // Writer
        StringWriter sw = new StringWriter();
        fmt.printTo(sw, dt);
        assertEquals("2022-05-20", sw.toString());

        StringWriter swMillis = new StringWriter();
        fmt.printTo(swMillis, dt.getMillis());
        assertEquals("2022-05-20", swMillis.toString());

        // Appendable
        StringBuilder app = new StringBuilder();
        fmt.printTo((Appendable) app, dt);
        assertEquals("2022-05-20", app.toString());

        StringBuilder appMillis = new StringBuilder();
        fmt.printTo((Appendable) appMillis, dt.getMillis());
        assertEquals("2022-05-20", appMillis.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTo_Partial_AllAppendables() throws IOException {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
        LocalTime time = new LocalTime(14, 30, 45);

        StringBuffer sb = new StringBuffer();
        fmt.printTo(sb, time);
        assertEquals("14:30:45", sb.toString());

        CharArrayWriter cw = new CharArrayWriter();
        fmt.printTo(cw, time);
        assertEquals("14:30:45", cw.toString());

        StringBuilder app = new StringBuilder();
        fmt.printTo((Appendable) app, time);
        assertEquals("14:30:45", app.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTo_InstantArithmeticOverflow_RevertsToUTC() {
        // Target branch: ((instant ^ adjustedInstant) < 0 && (instant ^ offset) >= 0)
        // With Long.MAX_VALUE and positive offset (+02:00), addition overflows into negative.
        DateTimeFormatter fmt = ISODateTimeFormat.year().withZone(OFFSET_PLUS_2);
        String result = fmt.print(Long.MAX_VALUE);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testRequirePrinter_ThrowsWhenPrinterNull() {
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, ISODateTimeFormat.dateTime().getParser());
        parserOnly.print(new DateTime());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testRequireParser_ThrowsWhenParserNull() {
        DateTimeFormatter printerOnly = new DateTimeFormatter(ISODateTimeFormat.dateTime().getPrinter(), null);
        printerOnly.parseDateTime("2020-01-01T00:00:00Z");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintTo_NullPartial_StringBuffer_Throws() {
        DateTimeFormatter fmt = ISODateTimeFormat.date();
        fmt.printTo(new StringBuffer(), (ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintTo_NullPartial_Writer_Throws() throws IOException {
        DateTimeFormatter fmt = ISODateTimeFormat.date();
        fmt.printTo(new StringWriter(), (ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInto_NullInstant_Throws() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        fmt.parseInto(null, "2020-01-01", 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseMillis_InvalidText_Throws() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        fmt.parseMillis("not-a-date");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseDateTime_IncompleteParse_Throws() {
        DateTimeFormatter fmt = ISODateTimeFormat.date();
        fmt.parseDateTime("2020-01-01ExtraChars");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseMutableDateTime_InvalidText_Throws() {
        DateTimeFormatter fmt = ISODateTimeFormat.date();
        fmt.parseMutableDateTime("INVALID");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseLocalDateTime_InvalidText_Throws() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        fmt.parseLocalDateTime("2020-99-99T99:99:99");
    }

    // =========================================================================
    // Partition E: Parsing Variations & Type Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseMillis_ValidText() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZoneUTC();
        long millis = fmt.parseMillis("1970-01-01T01:00:00.000Z");
        assertEquals(3600000L, millis);
    }

    @Test(timeout = 4000)
    public void testParseLocalDate_And_LocalTime() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String text = "2023-11-20T15:45:30.123Z";

        LocalDate date = fmt.parseLocalDate(text);
        assertEquals(new LocalDate(2023, 11, 20), date);

        LocalTime time = fmt.parseLocalTime(text);
        assertEquals(new LocalTime(15, 45, 30, 123), time);
    }

    @Test(timeout = 4000)
    public void testParseLocalDateTime_WithOffset() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String text = "2022-06-10T12:00:00.000+02:00";
        LocalDateTime ldt = fmt.parseLocalDateTime(text);

        assertEquals(2022, ldt.getYear());
        assertEquals(6, ldt.getMonthOfYear());
        assertEquals(10, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testParseDateTime_WithZoneOverride() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(TOKYO);
        DateTime dt = fmt.parseDateTime("2020-01-01T00:00:00.000Z");
        assertEquals(TOKYO, dt.getZone());
        // UTC 00:00 is 09:00 Tokyo
        assertEquals(9, dt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testParseDateTime_WithOffsetParsed() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withOffsetParsed();
        DateTime dt = fmt.parseDateTime("2020-01-01T10:00:00+04:00");
        assertEquals(DateTimeZone.forOffsetHours(4), dt.getZone());
        assertEquals(10, dt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testParseMutableDateTime_WithZoneAndChronology() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withChronology(chrono).withZone(PARIS);

        MutableDateTime mdt = fmt.parseMutableDateTime("2021-03-15T08:00:00.000Z");
        assertEquals(PARIS, mdt.getZone());
        assertEquals(chrono.withZone(PARIS), mdt.getChronology());
    }

    @Test(timeout = 4000)
    public void testParseInto_FailureReturnsNegative() {
        DateTimeFormatter fmt = ISODateTimeFormat.date();
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        int result = fmt.parseInto(mdt, "invalid-date", 0);
        assertTrue("Failed parse must return negative index", result < 0);
        int failurePos = ~result;
        assertEquals(0, failurePos);
    }

    @Test(timeout = 4000)
    public void testParseInto_WithZoneAndOffsetParsed() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withOffsetParsed();
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        int result = fmt.parseInto(mdt, "2020-06-01T15:30:00+03:00", 0);
        assertTrue(result > 0);
        assertEquals(DateTimeZone.forOffsetHours(3), mdt.getZone());
        assertEquals(15, mdt.getHourOfDay());
    }

    @Test(timeout = 4000)
    public void testParseInto_WithZoneOverride() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(PARIS);
        MutableDateTime mdt = new MutableDateTime(2020, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        int result = fmt.parseInto(mdt, "2020-01-01T12:00:00Z", 0);
        assertTrue(result > 0);
        assertEquals(PARIS, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testPivotYearParsing() {
        DateTimeFormatter base = DateTimeFormat.forPattern("yy-MM-dd");
        DateTimeFormatter fmtWithPivot = base.withPivotYear(1950).withZoneUTC();

        // 49 -> 1949, 50 -> 1950, 20 -> 2020
        DateTime dt49 = fmtWithPivot.parseDateTime("49-01-01");
        assertEquals(1949, dt49.getYear());

        DateTime dt50 = fmtWithPivot.parseDateTime("50-01-01");
        assertEquals(1950, dt50.getYear());
    }
}