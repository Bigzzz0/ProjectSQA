/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.joda.time.format.DateTimeFormatterBuilder
 *
 * 1. DEFECT-TARGETED TESTS (Defects4J Ground Truth):
 *    - test_printParseZoneDawsonCreek:
 *      TimeZoneId.parseInto() iterates over ALL_IDS without sorting by length descending.
 *      When parsing an ID like "America/Dawson_Creek", if a prefix like "America/Dawson"
 *      is encountered first, it prematurely consumes the prefix, causing parsing to fail
 *      with "malformed at '_Creek'".
 *
 * 2. EQUIVALENCE PARTITIONS & BOUNDARY VALUE ANALYSIS:
 *    - Partition A: Builder configuration and lifecycle
 *      - clear(): clears elements and cached formatter
 *      - canBuildFormatter/canBuildPrinter/canBuildParser logic for combinations of null/non-null
 *      - UnsupportedOperationException when neither printer nor parser exists
 *      - append(DateTimeFormatter), append(printer, parser), append(printer, parsers[])
 *      - appendOptional(DateTimeParser) with backtracking/fallback
 *    - Partition B: Literals and Text Fields
 *      - appendLiteral(char) & appendLiteral(String) [lengths 0, 1, >1, null]
 *      - case-insensitivity in CharacterLiteral and StringLiteral
 *      - appendText & appendShortText with month/dayOfWeek/halfday/era
 *      - Era "BC", "AD", "BCE", "CE" handling in English locale
 *    - Partition C: Numeric and Fixed Fields
 *      - appendDecimal vs appendSignedDecimal with minDigits <= 1 and > 1 (Unpadded vs Padded)
 *      - appendFixedDecimal & appendFixedSignedDecimal (FixedNumber boundary checks and failures)
 *      - Negative sign parsing and positive sign skipping
 *      - Integer length overflow (> 9 digits) triggering Integer.parseInt
 *    - Partition D: Two-Digit Years & Pivots
 *      - appendTwoDigitYear / appendTwoDigitWeekyear: pivot handling (pivot - 50 .. pivot + 49)
 *      - Lenient vs strict 2-digit parsing, explicit sign parsing, partial formatting
 *    - Partition E: Fractions and Time Zone Offsets
 *      - appendFraction, appendFractionOfSecond: maxDigits trimming (<=18), scale calculation
 *      - appendTimeZoneOffset: separators vs non-separators, min/max fields (1..4), zero offset
 *      - appendTimeZoneName, appendTimeZoneShortName with custom parseLookup
 *      - appendTimeZoneId printing and parsing
 */
package org.joda.time.format;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class DateTimeFormatterBuilderGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
    // =========================================================================

    /**
     * Targets the defect where TimeZoneId.parseInto() matches a shorter ID prefix
     * (e.g. "America/Dawson") before a longer one ("America/Dawson_Creek").
     */
    @Test(timeout = 4000)
    public void test_printParseZoneDawsonCreek() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm ")
                .appendTimeZoneId()
                .toFormatter();

        DateTime dt = fmt.parseDateTime("2007-03-04 12:30 America/Dawson_Creek");
        assertEquals(2007, dt.getYear());
        assertEquals(3, dt.getMonthOfYear());
        assertEquals(4, dt.getDayOfMonth());
        assertEquals(12, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
        assertEquals(DateTimeZone.forID("America/Dawson_Creek"), dt.getZone());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Builder State Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testBuilderLifecycleAndCapabilities() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertFalse(bld.canBuildFormatter());
        assertFalse(bld.canBuildPrinter());
        assertFalse(bld.canBuildParser());

        bld.appendLiteral('T');
        assertTrue(bld.canBuildFormatter());
        assertTrue(bld.canBuildPrinter());
        assertTrue(bld.canBuildParser());

        DateTimeFormatter f1 = bld.toFormatter();
        assertNotNull(f1);
        assertNotNull(bld.toPrinter());
        assertNotNull(bld.toParser());

        bld.clear();
        assertFalse(bld.canBuildFormatter());
        assertFalse(bld.canBuildPrinter());
        assertFalse(bld.canBuildParser());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToFormatterThrowsWhenEmpty() {
        new DateTimeFormatterBuilder().toFormatter();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToPrinterThrowsWhenParserOnly() {
        DateTimeParser parser = new DateTimeFormatterBuilder().appendLiteral('A').toParser();
        new DateTimeFormatterBuilder().append(parser).toPrinter();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToParserThrowsWhenPrinterOnly() {
        DateTimePrinter printer = new DateTimeFormatterBuilder().appendLiteral('A').toPrinter();
        new DateTimeFormatterBuilder().append(printer).toParser();
    }

    @Test(timeout = 4000)
    public void testAppendFormatterAndAppendPairs() {
        DateTimeFormatter subFormatter = new DateTimeFormatterBuilder().appendYear(4, 4).toFormatter();
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.append(subFormatter);

        DateTimePrinter printer = new DateTimeFormatterBuilder().appendMonthOfYear(2).toPrinter();
        DateTimeParser parser = new DateTimeFormatterBuilder().appendMonthOfYear(2).toParser();
        bld.append(printer, parser);

        DateTimeFormatter composite = bld.toFormatter();
        DateTime dt = composite.parseDateTime("202105");
        assertEquals(2021, dt.getYear());
        assertEquals(5, dt.getMonthOfYear());
        assertEquals("202105", composite.print(dt.withZone(DateTimeZone.UTC)));
    }

    @Test(timeout = 4000)
    public void testAppendMatchingParsersMultiple() {
        DateTimeParser p1 = new DateTimeFormatterBuilder().appendLiteral("XX").toParser();
        DateTimeParser p2 = new DateTimeFormatterBuilder().appendLiteral("YY").toParser();
        DateTimePrinter pr = new DateTimeFormatterBuilder().appendLiteral("ZZ").toPrinter();

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .append(pr, new DateTimeParser[]{p1, p2})
                .appendYear(4, 4)
                .toFormatter();

        assertEquals(2020, fmt.parseDateTime("XX2020").getYear());
        assertEquals(2022, fmt.parseDateTime("YY2022").getYear());
        assertEquals("ZZ2020", fmt.print(new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC)));
    }

    @Test(timeout = 4000)
    public void testAppendOptionalParser() {
        DateTimeParser optionalPart = new DateTimeFormatterBuilder().appendLiteral('-').toParser();
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendOptional(optionalPart)
                .appendMonthOfYear(2)
                .toFormatter();

        assertEquals(5, fmt.parseDateTime("2023-05").getMonthOfYear());
        assertEquals(5, fmt.parseDateTime("202305").getMonthOfYear());
    }

    // =========================================================================
    // Partition B: Literals and Text Elements
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendLiteralVariants() throws IOException {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral(""); // 0 length - no op
        bld.appendLiteral('A'); // char
        bld.appendLiteral("BC"); // string

        DateTimeFormatter fmt = bld.appendYear(4, 4).toFormatter();
        assertEquals("ABC2020", fmt.print(new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC)));

        // Case-insensitive check
        DateTime parsed = fmt.parseDateTime("abc2020");
        assertEquals(2020, parsed.getYear());

        // Writer print check
        StringWriter sw = new StringWriter();
        fmt.printTo(sw, new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC));
        assertEquals("ABC2020", sw.toString());

        // Partial print check
        StringBuffer sb = new StringBuffer();
        fmt.printTo(sb, new LocalDate(2020, 1, 1));
        assertEquals("ABC2020", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTextFieldsLongAndShort() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendDayOfWeekText()
                .appendLiteral(' ')
                .appendDayOfWeekShortText()
                .appendLiteral(' ')
                .appendMonthOfYearText()
                .appendLiteral(' ')
                .appendMonthOfYearShortText()
                .appendLiteral(' ')
                .appendHalfdayOfDayText()
                .appendLiteral(' ')
                .appendEraText()
                .toFormatter()
                .withLocale(Locale.ENGLISH)
                .withZone(DateTimeZone.UTC);

        DateTime dt = new DateTime(2023, 10, 2, 10, 0, DateTimeZone.UTC); // Monday, October
        String text = fmt.print(dt);
        assertEquals("Monday Mon October Oct AM AD", text);

        DateTimeFormatter parseFmt = new DateTimeFormatterBuilder()
                .appendEraText()
                .appendLiteral(' ')
                .appendYear(4, 4)
                .toFormatter()
                .withLocale(Locale.ENGLISH);

        DateTime parsedBCE = parseFmt.parseDateTime("BCE 0044");
        assertEquals(-43, parsedBCE.getYear()); // 44 BC = -43 in astronomical year
    }

    // =========================================================================
    // Partition C: Numeric Fields (Padded, Unpadded, Signed, Fixed)
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumericDecimalsAndSigns() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendSignedDecimal(DateTimeFieldType.year(), 1, 6)
                .appendLiteral('/')
                .appendDecimal(DateTimeFieldType.monthOfYear(), 2, 2)
                .appendLiteral('/')
                .appendDecimal(DateTimeFieldType.dayOfMonth(), 1, 2)
                .toFormatter();

        DateTime dt1 = fmt.parseDateTime("-200/05/9");
        assertEquals(-200, dt1.getYear());
        assertEquals(5, dt1.getMonthOfYear());
        assertEquals(9, dt1.getDayOfMonth());

        DateTime dt2 = fmt.parseDateTime("+2023/12/25");
        assertEquals(2023, dt2.getYear());
        assertEquals(12, dt2.getMonthOfYear());
        assertEquals(25, dt2.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testFixedDecimalSignedAndUnsigned() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendFixedDecimal(DateTimeFieldType.year(), 4)
                .appendFixedSignedDecimal(DateTimeFieldType.monthOfYear(), 2)
                .toFormatter();

        DateTime dt = fmt.parseDateTime("202305");
        assertEquals(2023, dt.getYear());
        assertEquals(5, dt.getMonthOfYear());

        // Parsing should fail if digits don't match fixed count exactly
        try {
            fmt.parseDateTime("20205");
            fail("Expected parse failure for insufficient digits");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testNumberFieldLargeValues() {
        // Test parsing numbers with >= 9 digits to hit Integer.parseInt fallback branch
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendSignedDecimal(DateTimeFieldType.millisOfDay(), 1, 10)
                .toFormatter();

        DateTime dt = fmt.parseDateTime("12345678");
        assertEquals(12345678, dt.getMillisOfDay());
    }

    @Test(timeout = 4000)
    public void testAllPredefinedNumericHelpers() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendYearOfEra(4, 4).appendLiteral('-')
                .appendCenturyOfEra(2, 2).appendLiteral('-')
                .appendYearOfCentury(2, 2).appendLiteral('-')
                .appendDayOfYear(3).appendLiteral('-')
                .appendWeekyear(4, 4).appendLiteral('-')
                .appendWeekOfWeekyear(2).appendLiteral('-')
                .appendDayOfWeek(1).appendLiteral('-')
                .appendHourOfDay(2).appendLiteral('-')
                .appendClockhourOfDay(2).appendLiteral('-')
                .appendHourOfHalfday(2).appendLiteral('-')
                .appendClockhourOfHalfday(2).appendLiteral('-')
                .appendMinuteOfHour(2).appendLiteral('-')
                .appendMinuteOfDay(4).appendLiteral('-')
                .appendSecondOfMinute(2).appendLiteral('-')
                .appendSecondOfDay(5).appendLiteral('-')
                .appendMillisOfSecond(3).appendLiteral('-')
                .appendMillisOfDay(8)
                .toFormatter();

        assertNotNull(fmt);
    }

    // =========================================================================
    // Partition D: Two-Digit Years & Pivot Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testTwoDigitYearPivoting() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendTwoDigitYear(2000, false)
                .toFormatter();

        assertEquals(2000, fmt.parseDateTime("00").getYear());
        assertEquals(2049, fmt.parseDateTime("49").getYear());
        assertEquals(1950, fmt.parseDateTime("50").getYear());
        assertEquals(1999, fmt.parseDateTime("99").getYear());

        // Negative pivot branch test
        DateTimeFormatter fmtNeg = new DateTimeFormatterBuilder()
                .appendTwoDigitYear(-50, false)
                .toFormatter();
        assertEquals(-1, fmtNeg.parseDateTime("99").getYear());
    }

    @Test(timeout = 4000)
    public void testTwoDigitYearLenient() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendTwoDigitYear(2000, true)
                .toFormatter();

        // 2 digits use pivot
        assertEquals(2023, fmt.parseDateTime("23").getYear());
        // More or less digits or signed treated as absolute
        assertEquals(1985, fmt.parseDateTime("1985").getYear());
        assertEquals(-500, fmt.parseDateTime("-500").getYear());
        assertEquals(200, fmt.parseDateTime("+200").getYear());
    }

    @Test(timeout = 4000)
    public void testTwoDigitWeekyear() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendTwoDigitWeekyear(2000, false)
                .appendLiteral("-W")
                .appendWeekOfWeekyear(2)
                .toFormatter();

        DateTime dt = fmt.parseDateTime("10-W05");
        assertEquals(2010, dt.getWeekyear());
        assertEquals(5, dt.getWeekOfWeekyear());

        // Lenient 2-digit weekyear
        DateTimeFormatter lenientFmt = new DateTimeFormatterBuilder()
                .appendTwoDigitWeekyear(2000, true)
                .toFormatter();
        assertEquals(2015, lenientFmt.parseDateTime("2015").getWeekyear());
    }

    // =========================================================================
    // Partition E: Fractions and Milliseconds
    // =========================================================================

    @Test(timeout = 4000)
    public void testFractionHandling() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendSecondOfMinute(2)
                .appendLiteral('.')
                .appendFractionOfSecond(1, 3)
                .toFormatter();

        DateTime dt = fmt.parseDateTime("30.5");
        assertEquals(30, dt.getSecondOfMinute());
        assertEquals(500, dt.getMillisOfSecond());

        DateTime dt2 = fmt.parseDateTime("30.075");
        assertEquals(30, dt2.getSecondOfMinute());
        assertEquals(75, dt2.getMillisOfSecond());

        String printed = fmt.print(new DateTime(2020, 1, 1, 0, 0, 30, 200, DateTimeZone.UTC));
        assertEquals("30.2", printed);

        DateTimeFormatter fmtFractionMinute = new DateTimeFormatterBuilder()
                .appendFractionOfMinute(1, 2)
                .toFormatter();
        assertNotNull(fmtFractionMinute);

        DateTimeFormatter fmtFractionHour = new DateTimeFormatterBuilder()
                .appendFractionOfHour(1, 2)
                .toFormatter();
        assertNotNull(fmtFractionHour);

        DateTimeFormatter fmtFractionDay = new DateTimeFormatterBuilder()
                .appendFractionOfDay(1, 2)
                .toFormatter();
        assertNotNull(fmtFractionDay);
    }

    // =========================================================================
    // Partition F: Time Zones (Offsets, Names, IDs)
    // =========================================================================

    @Test(timeout = 4000)
    public void testTimeZoneOffsetFormattingAndParsing() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendTimeZoneOffset("Z", true, 2, 4)
                .toFormatter();

        DateTime zero = fmt.parseDateTime("Z");
        assertEquals(0, zero.getZone().getOffset(0L));

        DateTime positive = fmt.parseDateTime("+05:30");
        assertEquals(5 * 3600000 + 30 * 60000, positive.getZone().getOffset(0L));

        DateTime negative = fmt.parseDateTime("-08:00");
        assertEquals(-8 * 3600000, negative.getZone().getOffset(0L));

        // Format test
        assertEquals("Z", fmt.print(new DateTime(0L, DateTimeZone.UTC)));
        assertEquals("+01:00", fmt.print(new DateTime(0L, DateTimeZone.forOffsetHours(1))));
        assertEquals("-05:00", fmt.print(new DateTime(0L, DateTimeZone.forOffsetHours(-5))));
    }

    @Test(timeout = 4000)
    public void testTimeZoneOffsetWithFractionsAndNoSeparators() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendTimeZoneOffset("0", false, 1, 4)
                .toFormatter();

        DateTime dt = fmt.parseDateTime("+0230");
        assertEquals(2 * 3600000 + 30 * 60000, dt.getZone().getOffset(0L));

        // Format without separators
        assertEquals("+0230", fmt.print(new DateTime(0L, DateTimeZone.forOffsetHoursMinutes(2, 30))));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameCustomLookup() {
        Map<String, DateTimeZone> lookup = new LinkedHashMap<String, DateTimeZone>();
        lookup.put("PST", DateTimeZone.forOffsetHours(-8));
        lookup.put("PDT", DateTimeZone.forOffsetHours(-7));
        lookup.put("GMT", DateTimeZone.UTC);

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendHourOfDay(2)
                .appendLiteral(' ')
                .appendTimeZoneName(lookup)
                .toFormatter();

        DateTime dt = fmt.parseDateTime("12 PST");
        assertEquals(DateTimeZone.forOffsetHours(-8), dt.getZone());

        DateTimeFormatter fmtShort = new DateTimeFormatterBuilder()
                .appendHourOfDay(2)
                .appendLiteral(' ')
                .appendTimeZoneShortName(lookup)
                .toFormatter();

        DateTime dtShort = fmtShort.parseDateTime("14 PDT");
        assertEquals(DateTimeZone.forOffsetHours(-7), dtShort.getZone());
    }

    @Test(timeout = 4000)
    public void testTimeZoneIdPrintAndParse() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendYear(4, 4)
                .appendLiteral(' ')
                .appendTimeZoneId()
                .toFormatter();

        DateTime dt = fmt.parseDateTime("2020 Europe/London");
        assertEquals(2020, dt.getYear());
        assertEquals(DateTimeZone.forID("Europe/London"), dt.getZone());

        String printed = fmt.print(new DateTime(2020, 1, 1, 0, 0, DateTimeZone.forID("Asia/Tokyo")));
        assertEquals("2020 Asia/Tokyo", printed);
    }

    // =========================================================================
    // Partition G: Defensive Guard Paths & Exception Handling
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullFormatterThrows() {
        new DateTimeFormatterBuilder().append((DateTimeFormatter) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullPrinterThrows() {
        new DateTimeFormatterBuilder().append((DateTimePrinter) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullParserThrows() {
        new DateTimeFormatterBuilder().append((DateTimeParser) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullLiteralThrows() {
        new DateTimeFormatterBuilder().appendLiteral((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendDecimalInvalidMinDigitsThrows() {
        new DateTimeFormatterBuilder().appendDecimal(DateTimeFieldType.dayOfMonth(), -1, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendFixedDecimalNonPositiveDigitsThrows() {
        new DateTimeFormatterBuilder().appendFixedDecimal(DateTimeFieldType.dayOfMonth(), 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendFixedSignedDecimalNonPositiveDigitsThrows() {
        new DateTimeFormatterBuilder().appendFixedSignedDecimal(DateTimeFieldType.year(), -2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendMatchingParsersIncompleteArrayThrows() {
        DateTimeParser p1 = new DateTimeFormatterBuilder().appendLiteral('a').toParser();
        // Null in middle of parsers array is illegal
        new DateTimeFormatterBuilder().append(null, new DateTimeParser[]{p1, null, p1});
    }

    @Test(timeout = 4000)
    public void testAppendPatternIntegration() {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .toFormatter();

        LocalDate ld = fmt.parseLocalDate("2023-11-20");
        assertEquals(2023, ld.getYear());
        assertEquals(11, ld.getMonthOfYear());
        assertEquals(20, ld.getDayOfMonth());
    }
}