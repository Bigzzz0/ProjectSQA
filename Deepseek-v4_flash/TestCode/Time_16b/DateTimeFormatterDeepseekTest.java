package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadWritableInstant;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.ISOChronology;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches targeted (from source code):
 *  1. Constructor invocation: public (2 params) vs private (8 params)
 *  2. isPrinter(): return (iPrinter != null)
 *  3. isParser(): return (iParser != null)
 *  4. withLocale(): null/equal/not-equal branches
 *  5. getLocale(): return iLocale (null/default)
 *  6. withOffsetParsed(): iOffsetParsed flag true/false
 *  7. isOffsetParsed(): return iOffsetParsed
 *  8. withChronology(): null/equal/not-equal branches
 *  9. getChronology(): return iChrono
 * 10. getChronolgy(): deprecated method returning iChrono
 * 11. withZoneUTC(): delegates to withZone(DateTimeZone.UTC)
 * 12. withZone(): iZone == zone vs not
 * 13. getZone(): return iZone
 * 14. withPivotYear(Integer): null/equals/not-equals branches
 * 15. withPivotYear(int): box and call Integer version
 * 16. getPivotYear(): return iPivotYear
 * 17. withDefaultYear(): always create new formatter
 * 18. getDefaultYear(): return iDefaultYear
 * 19. printTo(StringBuffer, ReadableInstant): delegates to printTo(buf, millis, chrono)
 * 20. printTo(Writer, ReadableInstant): delegates to printTo(out, millis, chrono)
 * 21. printTo(Appendable, ReadableInstant): delegates to print(instant).append
 * 22. printTo(StringBuffer, long): delegates to printTo(buf, instant, null)
 * 23. printTo(Writer, long): delegates to printTo(out, instant, null)
 * 24. printTo(Appendable, long): delegates to print(instant).append
 * 25. printTo(StringBuffer, ReadablePartial): checks null partial; delegates to printer
 * 26. printTo(Writer, ReadablePartial): checks null partial; delegates to printer
 * 27. printTo(Appendable, ReadablePartial): delegates to print(partial).append
 * 28. print(ReadableInstant): uses StringBuffer and printTo
 * 29. print(long): uses StringBuffer and printTo
 * 30. print(ReadablePartial): uses StringBuffer and printTo(buf, partial)
 * 31. printTo(StringBuffer, long, Chronology) private: offset overflow detection branch
 * 32. printTo(Writer, long, Chronology) private: offset overflow detection branch
 * 33. requirePrinter(): throws UnsupportedOperationException if null
 * 34. parseInto(ReadWritableInstant, String, int): null instant check; offset/zone resolution; iZone override
 * 35. parseMillis(String): newPos >=0 && newPos >= length vs error
 * 36. parseLocalDate(String): delegates to parseLocalDateTime
 * 37. parseLocalTime(String): delegates to parseLocalDateTime
 * 38. parseLocalDateTime(String): offset/zone resolution from bucket
 * 39. parseDateTime(String): offset/zone resolution; iZone override
 * 40. parseMutableDateTime(String): offset/zone resolution; iZone override
 * 41. requireParser(): throws UnsupportedOperationException if null
 * 42. selectChronology(Chronology): iChrono override and iZone override
 * 
 * Boundary conditions:
 *  - Default year = 2000 for parsing without year
 *  - Pivot year range [pivot-50, pivot+49]
 *  - Offset overflow detection in printTo private methods
 *  - null chronology/zone arguments
 *  - February 29th parsing with leap year default (2000 vs 2004)
 *  - Month/day parsing without year (default year used)
 *  - Time zone offset handling in parseInto
 *  
 * Defect targeting:
 *  - The known defect involves parseInto using iDefaultYear when computing millis,
 *    but not properly using the base instant's year. The fix likely involves
 *    using the instant's year as default when parsing month/day only.
 *  - Tests: testParseInto_monthOnly*, testParseInto_monthDay_*feb29
 *    All show expected year 2000 vs 2004/2012 mismatch.
 *  - The defect is in parseInto's DateTimeParserBucket construction: 
 *    final argument is iDefaultYear (2000) instead of using the instant's year.
 *    The fix should use the year from the instant's local time.
 */

public class DateTimeFormatterDeepseekTest {

    // ===== Test helpers =====
    
    private static class MockDateTimePrinter implements DateTimePrinter {
        private final int estimatedLength;
        private final String output;
        
        MockDateTimePrinter(String output, int estimatedLength) {
            this.output = output;
            this.estimatedLength = estimatedLength;
        }
        
        @Override
        public int estimatePrintedLength() {
            return estimatedLength;
        }

        @Override
        public void printTo(StringBuffer buf, long instant, Chronology chrono, int displayOffset,
                DateTimeZone displayZone, Locale locale) {
            if (output == null) {
                throw new UnsupportedOperationException("Print not supported");
            }
            buf.append(output);
        }

        @Override
        public void printTo(Writer out, long instant, Chronology chrono, int displayOffset,
                DateTimeZone displayZone, Locale locale) throws IOException {
            if (output == null) {
                throw new UnsupportedOperationException("Print not supported");
            }
            out.write(output);
        }

        @Override
        public void printTo(StringBuffer buf, ReadablePartial partial, Locale locale) {
            if (output == null) {
                throw new UnsupportedOperationException("Print not supported");
            }
            buf.append(output);
        }

        @Override
        public void printTo(Writer out, ReadablePartial partial, Locale locale) throws IOException {
            if (output == null) {
                throw new UnsupportedOperationException("Print not supported");
            }
            out.write(output);
        }
    }
    
    private static class MockDateTimeParser implements DateTimeParser {
        private final int parseResult;
        private final boolean supportsParsing;
        
        MockDateTimeParser(int parseResult, boolean supportsParsing) {
            this.parseResult = parseResult;
            this.supportsParsing = supportsParsing;
        }
        
        @Override
        public int estimateParsedLength() {
            return 10;
        }

        @Override
        public int parseInto(DateTimeParserBucket bucket, String text, int position) {
            if (!supportsParsing) {
                throw new UnsupportedOperationException("Parsing not supported");
            }
            return parseResult;
        }
    }
    
    private static class MockReadablePartial implements ReadablePartial {
        @Override
        public int size() { return 0; }
        
        @Override
        public DateTimeFieldType getFieldType(int index) { return null; }
        
        @Override
        public DateTimeField getField(int index) { return null; }
        
        @Override
        public int get(DateTimeFieldType field) { return 0; }
        
        @Override
        public boolean isSupported(DateTimeFieldType type) { return false; }
        
        @Override
        public DateTime toDateTime(ReadableInstant baseInstant) { return null; }
        
        @Override
        public Chronology getChronology() { return null; }
        
        @Override
        public boolean equals(Object partial) { return false; }
        
        @Override
        public int hashCode() { return 0; }
        
        @Override
        public String toString() { return ""; }
        
        @Override
        public int compareTo(ReadablePartial other) { return 0; }
    }
    
    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorDefault() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        assertSame(printer, f.getPrinter());
        assertSame(parser, f.getParser());
        assertNull(f.getLocale());
        assertFalse(f.isOffsetParsed());
        assertNull(f.getChronology());
        assertNull(f.getZone());
        assertNull(f.getPivotYear());
        assertEquals(2000, f.getDefaultYear());
        assertTrue(f.isPrinter());
        assertTrue(f.isParser());
    }

    @Test(timeout = 4000)
    public void testConstructorNullPrinterAndParser() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        assertNull(f.getPrinter());
        assertNull(f.getParser());
        assertFalse(f.isPrinter());
        assertFalse(f.isParser());
    }

    @Test(timeout = 4000)
    public void testWithLocale() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        // Set locale
        Locale locale = Locale.FRENCH;
        DateTimeFormatter f2 = f.withLocale(locale);
        assertNotSame(f, f2);
        assertEquals(locale, f2.getLocale());
        assertNull(f.getLocale()); // original unchanged
        
        // Same locale - should return same instance
        DateTimeFormatter f3 = f2.withLocale(locale);
        assertSame(f2, f3);
        
        // Null locale
        DateTimeFormatter f4 = f2.withLocale(null);
        assertNotSame(f2, f4);
        assertNull(f4.getLocale());
    }

    @Test(timeout = 4000)
    public void testWithOffsetParsed() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        assertFalse(f.isOffsetParsed());
        
        DateTimeFormatter f2 = f.withOffsetParsed();
        assertTrue(f2.isOffsetParsed());
        assertNull(f2.getZone()); // zone set to null
        assertFalse(f.isOffsetParsed()); // original unchanged
        
        // Already offset parsed - return same
        DateTimeFormatter f3 = f2.withOffsetParsed();
        assertSame(f2, f3);
    }

    @Test(timeout = 4000)
    public void testWithChronology() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeFormatter f2 = f.withChronology(chrono);
        assertNotSame(f, f2);
        assertSame(chrono, f2.getChronology());
        assertNull(f.getChronology());
        
        // Same chronology
        DateTimeFormatter f3 = f2.withChronology(chrono);
        assertSame(f2, f3);
        
        // Null chronology
        DateTimeFormatter f4 = f2.withChronology(null);
        assertNotSame(f2, f4);
        assertNull(f4.getChronology());
    }

    @Test(timeout = 4000)
    public void testGetChronolgyDeprecated() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        assertNull(f.getChronolgy());
        
        Chronology chrono = ISOChronology.getInstanceUTC();
        DateTimeFormatter f2 = f.withChronology(chrono);
        assertSame(chrono, f2.getChronolgy());
    }

    @Test(timeout = 4000)
    public void testWithZoneUTC() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTimeFormatter f2 = f.withZoneUTC();
        assertSame(DateTimeZone.UTC, f2.getZone());
        assertNull(f.getZone());
    }

    @Test(timeout = 4000)
    public void testWithZone() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTimeFormatter f2 = f.withZone(zone);
        assertNotSame(f, f2);
        assertSame(zone, f2.getZone());
        assertFalse(f2.isOffsetParsed()); // zone overrides offsetParsed
        assertNull(f.getZone());
        
        // Same zone
        DateTimeFormatter f3 = f2.withZone(zone);
        assertSame(f2, f3);
        
        // Null zone
        DateTimeFormatter f4 = f2.withZone(null);
        assertNotSame(f2, f4);
        assertNull(f4.getZone());
    }

    @Test(timeout = 4000)
    public void testWithPivotYearInteger() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTimeFormatter f2 = f.withPivotYear(Integer.valueOf(2005));
        assertNotSame(f, f2);
        assertEquals(Integer.valueOf(2005), f2.getPivotYear());
        assertNull(f.getPivotYear());
        
        // Same pivot year
        DateTimeFormatter f3 = f2.withPivotYear(Integer.valueOf(2005));
        assertSame(f2, f3);
        
        // Null pivot year
        DateTimeFormatter f4 = f2.withPivotYear((Integer) null);
        assertNotSame(f2, f4);
        assertNull(f4.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testWithPivotYearInt() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTimeFormatter f2 = f.withPivotYear(2010);
        assertEquals(Integer.valueOf(2010), f2.getPivotYear());
        assertNull(f.getPivotYear());
    }

    @Test(timeout = 4000)
    public void testWithDefaultYear() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        assertEquals(2000, f.getDefaultYear());
        
        DateTimeFormatter f2 = f.withDefaultYear(2012);
        assertNotSame(f, f2);
        assertEquals(2012, f2.getDefaultYear());
        assertEquals(2000, f.getDefaultYear()); // original unchanged
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testPrintToAppendableReadableInstant() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("printed", 7);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTime dt = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        StringBuilder sb = new StringBuilder();
        f.printTo(sb, (ReadableInstant) dt);
        assertEquals("printed", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToWriterReadableInstant() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("writer", 6);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTime dt = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        StringWriter sw = new StringWriter();
        f.printTo(sw, (ReadableInstant) dt);
        assertEquals("writer", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToAppendableLong() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("longprint", 9);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        StringBuilder sb = new StringBuilder();
        f.printTo(sb, 1000000L);
        assertEquals("longprint", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToWriterLong() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("writerlong", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        StringWriter sw = new StringWriter();
        f.printTo(sw, 2000000L);
        assertEquals("writerlong", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToAppendableReadablePartial() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("partial", 7);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        ReadablePartial partial = new MockReadablePartial();
        StringBuilder sb = new StringBuilder();
        f.printTo(sb, partial);
        assertEquals("partial", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPrintReadableInstant() {
        MockDateTimePrinter printer = new MockDateTimePrinter("printed", 7);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTime dt = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        String result = f.print((ReadableInstant) dt);
        assertEquals("printed", result);
    }

    @Test(timeout = 4000)
    public void testPrintLong() {
        MockDateTimePrinter printer = new MockDateTimePrinter("longresult", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        String result = f.print(3000000L);
        assertEquals("longresult", result);
    }

    @Test(timeout = 4000)
    public void testPrintReadablePartial() {
        MockDateTimePrinter printer = new MockDateTimePrinter("partresult", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        ReadablePartial partial = new MockReadablePartial();
        String result = f.print(partial);
        assertEquals("partresult", result);
    }

    @Test(timeout = 4000)
    public void testPrintToWithNullInstant() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        // Null instant means "now" for ReadableInstant - will use current time
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, (ReadableInstant) null);
        assertTrue(buf.length() > 0);
        assertEquals("test", buf.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToPartialWithNullPartial() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        try {
            f.printTo(new StringBuffer(), (ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPrintToWriterPartialWithNullPartial() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        try {
            f.printTo(new StringWriter(), (ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testParseInto_monthOnly_baseStartYear() {
        // Reproduce defect: parseInto should use the instant's year, not iDefaultYear
        // This test targets the bug: iDefaultYear (2000) is used instead of instant's year (2004)
        
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendMonthOfYear(2)
                .toFormatter();
        
        // Create base instant with year 2004
        DateTimeZone zone = DateTimeZone.forID("Asia/Tokyo");
        MutableDateTime base = new MutableDateTime(2004, 5, 1, 12, 20, 30, 0, zone);
        
        // The bug: when parsing "-MM" pattern, the default year (2000) overrides the instant's year
        // Expected behavior: instant's year (2004) should be preserved
        String text = "-05";
        int result = f.parseInto(base, text, 0);
        
        // Result should preserve year 2004, not use 2000
        assertEquals(new MutableDateTime(2004, 5, 1, 12, 20, 30, 0, zone), base);
        assertTrue(result >= 0);
    }

    @Test(timeout = 4000)
    public void testParseInto_monthOnly_parseStartYear() {
        // Similar defect: year should come from parsed year or base instant
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendMonthOfYear(2)
                .toFormatter();
        
        DateTimeZone zone = DateTimeZone.forID("Asia/Tokyo");
        MutableDateTime base = new MutableDateTime(2004, 1, 1, 12, 20, 30, 0, zone);
        
        // Parse month only, year should be preserved from base (2004)
        String text = "-01";
        f.parseInto(base, text, 0);
        
        assertEquals(2004, base.getYear());
        assertEquals(1, base.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testParseInto_monthOnly_baseEndYear() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendMonthOfYear(2)
                .toFormatter();
        
        DateTimeZone zone = DateTimeZone.forID("Asia/Tokyo");
        MutableDateTime base = new MutableDateTime(2004, 5, 31, 12, 20, 30, 0, zone);
        
        String text = "-05";
        f.parseInto(base, text, 0);
        
        // Month should be updated but year should remain 2004
        assertEquals(2004, base.getYear());
        assertEquals(5, base.getMonthOfYear());
        assertEquals(31, base.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseInto_monthOnly() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendMonthOfYear(2)
                .toFormatter();
        
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        MutableDateTime base = new MutableDateTime(2004, 5, 9, 12, 20, 30, 0, zone);
        
        String text = "-05";
        f.parseInto(base, text, 0);
        
        // Year should be preserved from base instant (2004), not default (2000)
        assertEquals(2004, base.getYear());
        assertEquals(5, base.getMonthOfYear());
    }

    @Test(timeout = 4000)
    public void testParseInto_monthDay_withDefaultYear_feb29() {
        // Critical: February 29th parsing with default year
        // Base instant year 2004 is leap year, default year 2000 is also leap year
        // But the bug causes 2000 to be used when it should be 2004
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendMonthOfYear(2)
                .appendLiteral('-')
                .appendDayOfMonth(2)
                .toFormatter()
                .withDefaultYear(2012); // Override default year
        
        DateTimeZone zone = DateTimeZone.UTC;
        MutableDateTime base = new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, zone);
        
        String text = "02-29";
        f.parseInto(base, text, 0);
        
        // With the bug, this would use default year (different from base year)
        // The expected correct behavior is to use base instant's year
        assertEquals(2004, base.getYear());
        assertEquals(2, base.getMonthOfYear());
        assertEquals(29, base.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseInto_monthDay_feb29() {
        // February 29th with default year 2000
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendMonthOfYear(2)
                .appendLiteral('-')
                .appendDayOfMonth(2)
                .toFormatter();
        
        DateTimeZone zone = DateTimeZone.UTC;
        MutableDateTime base = new MutableDateTime(2004, 2, 29, 12, 20, 30, 0, zone);
        
        String text = "02-29";
        f.parseInto(base, text, 0);
        
        // The bug: uses iDefaultYear (2000) instead of base instant's year (2004)
        assertEquals(2004, base.getYear());
        assertEquals(2, base.getMonthOfYear());
        assertEquals(29, base.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testParseInto_monthOnly_parseEndYear() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendMonthOfYear(2)
                .toFormatter();
        
        DateTimeZone zone = DateTimeZone.forID("Asia/Tokyo");
        MutableDateTime base = new MutableDateTime(2004, 12, 31, 12, 20, 30, 0, zone);
        
        String text = "-12";
        f.parseInto(base, text, 0);
        
        // Year should be 2004 from base instant
        assertEquals(2004, base.getYear());
        assertEquals(12, base.getMonthOfYear());
        assertEquals(31, base.getDayOfMonth());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequirePrinterThrowsWhenNull() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        f.print(new DateTime()); // triggers requirePrinter
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRequireParserThrowsWhenNull() {
        DateTimeFormatter f = new DateTimeFormatter(null, null);
        f.parseMillis("test"); // triggers requireParser
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseIntoNullInstant() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        f.parseInto(null, "test", 0);
    }

    @Test(timeout = 4000)
    public void testParseMillisWithInvalidText() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(-1, true); // return negative position
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        try {
            f.parseMillis("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseMillisWithIncompleteParse() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(3, true); // parse only first 3 chars
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        try {
            f.parseMillis("toolongstring");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseLocalDateTimeWithInvalidText() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(-1, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        try {
            f.parseLocalDateTime("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseDateTimeWithInvalidText() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(-1, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        try {
            f.parseDateTime("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseMutableDateTimeWithInvalidText() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(-1, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        try {
            f.parseMutableDateTime("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testPrintThrowsUnsupported() {
        // No printer set
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(null, parser);
        
        f.print(new DateTime()); // should throw
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testParseMillisThrowsUnsupported() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        DateTimeFormatter f = new DateTimeFormatter(printer, null);
        
        f.parseMillis("test"); // should throw
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testSelectChronologyWithOverride() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        
        // With chronology override and zone override
        Chronology isoUTC = ISOChronology.getInstanceUTC();
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTimeFormatter f = new DateTimeFormatter(printer, parser, null, false, isoUTC, zone, null, 2000);
        
        // Test the selectChronology path with chronology that has a different zone
        Chronology input = ISOChronology.getInstance(DateTimeZone.forID("Europe/London"));
        Chronology result = f.selectChronology(input); // actually private, but we test via behavior
        
        // This tests the logic indirectly via parse methods
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testPrintToAppendableInstantDelegation() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("appendable", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTime dt = new DateTime(2023, 1, 1, 0, 0, 0, DateTimeZone.UTC);
        StringBuilder sb = new StringBuilder();
        f.printTo(sb, (ReadableInstant) dt);
        assertEquals("appendable", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToAppendableLongInstant() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("longmsg", 7);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        StringBuilder sb = new StringBuilder();
        f.printTo(sb, 500000L);
        assertEquals("longmsg", sb.toString());
    }

    @Test(timeout = 4000)
    public void testParseLocalDateDelegation() {
        // Test that parseLocalDate delegates to parseLocalDateTime and extracts date
        // Using a simple ISO date formatter
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate result = f.parseLocalDate("2024-01-15");
        assertEquals(new LocalDate(2024, 1, 15), result);
    }

    @Test(timeout = 4000)
    public void testParseLocalTimeDelegation() {
        DateTimeFormatter f = DateTimeFormat.forPattern("HH:mm:ss");
        LocalTime result = f.parseLocalTime("14:30:45");
        assertEquals(new LocalTime(14, 30, 45), result);
    }

    @Test(timeout = 4000)
    public void testParseIntoWithOffsetParsed() {
        // Test parseInto with offset parsing enabled
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendYear(4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendDayOfMonth(2)
                .appendLiteral('T')
                .appendHourOfDay(2)
                .appendLiteral(':')
                .appendMinuteOfHour(2)
                .toFormatter()
                .withOffsetParsed();
        
        MutableDateTime mdt = new MutableDateTime(0, DateTimeZone.UTC);
        f.parseInto(mdt, "2024-06-15T10:30", 0);
        
        // Should use offset from parsed zone (none provided, so default)
        assertNotNull(mdt);
    }

    @Test(timeout = 4000)
    public void testParseIntoWithZoneOverride() {
        // Test parseInto with zone override on formatter
        DateTimeZone parisZone = DateTimeZone.forID("Europe/Paris");
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeFormatter f = builder.appendYear(4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendDayOfMonth(2)
                .toFormatter()
                .withZone(parisZone);
        
        MutableDateTime mdt = new MutableDateTime(0, DateTimeZone.UTC);
        f.parseInto(mdt, "2024-06-15", 0);
        
        // Zone should be overridden to Paris
        assertEquals(parisZone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testParseIntoWithOffsetFromBucket() {
        // Test parseInto when bucket has offset (from timezone in text)
        // Using a formatter that can parse timezone offset
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                .withOffsetParsed();
        
        MutableDateTime mdt = new MutableDateTime(0, DateTimeZone.UTC);
        f.parseInto(mdt, "2024-06-15T10:30:00+0200", 0);
        
        // The offset should be parsed and used
        assertEquals(DateTimeZone.forOffsetHours(2), mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testPrintToBufferReadablePartial() {
        // Test printing a partial to buffer with null partial check
        MockDateTimePrinter printer = new MockDateTimePrinter("partial", 7);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        ReadablePartial partial = new MockReadablePartial();
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, partial);
        assertEquals("partial", buf.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToWriterReadablePartial() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("writerpart", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        ReadablePartial partial = new MockReadablePartial();
        StringWriter sw = new StringWriter();
        f.printTo(sw, partial);
        assertEquals("writerpart", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToAppendableReadablePartialDelegate() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("appendpart", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        ReadablePartial partial = new MockReadablePartial();
        StringBuilder sb = new StringBuilder();
        f.printTo(sb, partial);
        assertEquals("appendpart", sb.toString());
    }

    @Test(timeout = 4000)
    public void testWithOffsetParsedSetsZoneNull() {
        // When withOffsetParsed is called, zone is set to null
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser, null, false, null, london, null, 2000);
        
        DateTimeFormatter f2 = f.withOffsetParsed();
        assertTrue(f2.isOffsetParsed());
        assertNull(f2.getZone()); // zone should be null
    }

    @Test(timeout = 4000)
    public void testWithZoneSetsOffsetParsedFalse() {
        // When withZone is called, offsetParsed is set to false
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser, null, true, null, null, null, 2000);
        
        DateTimeFormatter f2 = f.withZone(DateTimeZone.UTC);
        assertFalse(f2.isOffsetParsed()); // offsetParsed should be false
        assertSame(DateTimeZone.UTC, f2.getZone());
    }

    @Test(timeout = 4000)
    public void testPrintToBufferReadableInstant() {
        MockDateTimePrinter printer = new MockDateTimePrinter("buffertest", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTime dt = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, (ReadableInstant) dt);
        assertEquals("buffertest", buf.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToWriterReadableInstantDelegation() throws IOException {
        MockDateTimePrinter printer = new MockDateTimePrinter("writertest", 10);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        DateTime dt = new DateTime(2023, 6, 15, 10, 30, 0, DateTimeZone.UTC);
        StringWriter sw = new StringWriter();
        f.printTo(sw, (ReadableInstant) dt);
        assertEquals("writertest", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetPrinterReturnsInternal() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        assertSame(printer, f.getPrinter());
    }

    @Test(timeout = 4000)
    public void testGetParserReturnsInternal() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        assertSame(parser, f.getParser());
    }

    @Test(timeout = 4000)
    public void testParseIntoReturnsPosition() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(7, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        MutableDateTime mdt = new MutableDateTime(0, DateTimeZone.UTC);
        int pos = f.parseInto(mdt, "some text", 2);
        assertEquals(7, pos);
    }

    @Test(timeout = 4000)
    public void testParseIntoWithNegativeReturn() {
        // When parser returns negative, method still processes partially
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(-3, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        MutableDateTime mdt = new MutableDateTime(0, DateTimeZone.UTC);
        int pos = f.parseInto(mdt, "text", 0);
        assertTrue(pos < 0);
    }

    @Test(timeout = 4000)
    public void testParseMillisWithCompleteParse() {
        // Test parseMillis with parser that completes successfully
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        long millis = f.parseMillis("2024-01-15");
        assertEquals(new DateTime(2024, 1, 15, 0, 0, 0, DateTimeZone.UTC).getMillis(), millis);
    }

    @Test(timeout = 4000)
    public void testParseDateTimeWithZone() {
        // Full parse with zone override
        DateTimeZone zone = DateTimeZone.forID("America/Los_Angeles");
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd")
                .withZone(zone);
        
        DateTime dt = f.parseDateTime("2024-01-15");
        assertEquals(zone, dt.getZone());
    }

    @Test(timeout = 4000)
    public void testParseMutableDateTimeWithZone() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Shanghai");
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd")
                .withZone(zone);
        
        MutableDateTime mdt = f.parseMutableDateTime("2024-01-15");
        assertEquals(zone, mdt.getZone());
    }

    @Test(timeout = 4000)
    public void testParseLocalDateTimeWithOffset() {
        // parseLocalDateTime should still handle offset
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        LocalDateTime ldt = f.parseLocalDateTime("2024-01-15T10:30:00+0200");
        // Should return local time without zone/offset
        assertEquals(new LocalDateTime(2024, 1, 15, 10, 30, 0), ldt);
    }

    @Test(timeout = 4000)
    public void testParseLocalDateTimeWithZone() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        LocalDateTime ldt = f.parseLocalDateTime("2024-01-15T10:30:00Z");
        assertEquals(new LocalDateTime(2024, 1, 15, 10, 30, 0), ldt);
    }

    @Test(timeout = 4000)
    public void testParseLocalDateTimeWithCompleteParse() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDateTime ldt = f.parseLocalDateTime("2024-01-15");
        assertEquals(new LocalDateTime(2024, 1, 15, 0, 0, 0), ldt);
    }

    @Test(timeout = 4000)
    public void testParseLocalDateTimeWithIncompleteParse() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        try {
            f.parseLocalDateTime("2024-01");
            fail("Expected IllegalArgumentException for incomplete text");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseDateTimeWithIncompleteParse() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        try {
            f.parseDateTime("2024-01");
            fail("Expected IllegalArgumentException for incomplete text");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseMutableDateTimeWithIncompleteParse() {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        try {
            f.parseMutableDateTime("2024-01");
            fail("Expected IllegalArgumentException for incomplete text");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPrintWithOverrideChronology() {
        // Test print with override chronology
        Chronology julian = ISOChronology.getInstanceUTC(); // using ISO as substitute
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd")
                .withChronology(julian);
        
        DateTime dt = new DateTime(2024, 1, 15, 0, 0, 0, julian);
        String result = f.print(dt);
        assertEquals("2024-01-15", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithOverrideZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
                .withZone(zone);
        
        DateTime dt = new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC);
        String result = f.print(dt);
        // Should print in New York time (UTC-5)
        assertNotNull(result);
        assertTrue(result.contains("2024-01-15"));
    }

    @Test(timeout = 4000)
    public void testWithPivotYearNullNoOp() {
        MockDateTimePrinter printer = new MockDateTimePrinter("test", 4);
        MockDateTimeParser parser = new MockDateTimeParser(5, true);
        DateTimeFormatter f = new DateTimeFormatter(printer, parser);
        
        // Calling withPivotYear(null) on a formatter with no pivot year should return same
        DateTimeFormatter f2 = f.withPivotYear((Integer) null);
        assertSame(f, f2);
    }
}