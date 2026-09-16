package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.ISOChronology;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;

public class DateTimeFormatterBuilderDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     *
     * Targeting branches in DateTimeFormatterBuilder:
     * - toFormatter(): printer/parser null checks, unsupported exception
     * - toPrinter(): isPrinter check
     * - toParser(): isParser check
     * - canBuildFormatter/Printer/Parser: isFormatter/isPrinter/isParser
     * - clear(): resets state
     * - append(DateTimeFormatter): null check, append0 delegation
     * - append(DateTimePrinter): null check
     * - append(DateTimeParser): null check
     * - append(DateTimePrinter, DateTimeParser): null checks
     * - append(DateTimePrinter, DateTimeParser[]): null checks, length==1, array validation
     * - appendOptional(DateTimeParser): MatchingParser creation
     * - appendLiteral(char): CharacterLiteral
     * - appendLiteral(String): empty string, length 1, multi-char, null
     * - appendDecimal/appendSignedDecimal: fieldType null, maxDigits < minDigits, minDigits <0 or maxDigits<=0, minDigits <=1 vs >1
     * - appendFixedDecimal/appendFixedSignedDecimal: null fieldType, numDigits<=0
     * - appendText/ShortText: null fieldType
     * - appendFraction: null fieldType, maxDigits<minDigits, minDigits<0 or maxDigits<=0
     * - appendFractionOfSecond/Minute/Hour/Day: delegates to appendFraction
     * - appendMillisOfSecond/Day, appendSecondOfMinute/Day, appendMinuteOfHour/Day, appendHourOfDay/ClockhourOfDay/HourOfHalfday/ClockhourOfHalfday, appendDayOfWeek/Month/Year, appendWeekOfWeekyear, appendWeekyear, appendMonthOfYear, appendYear, appendTwoDigitYear/Weekyear, appendYearOfEra/Century, appendCenturyOfEra, appendHalfdayOfDayText, appendDayOfWeekText/ShortText, appendMonthOfYearText/ShortText, appendEraText: delegation to appendDecimal/Text/SignedDecimal
     * - appendTimeZoneName()/ShortName(): printer-only, no parser
     * - appendTimeZoneName(Map)/ShortName(Map): printer & parser with lookup
     * - appendTimeZoneId(): TimeZoneId printer/parser
     * - appendTimeZoneOffset(4-arg): delegates to 5-arg
     * - appendTimeZoneOffset(5-arg): minFields<=0, maxFields<minFields, minFields>4 clamping, estimatePrintedLength
     * - appendPattern: delegates to DateTimeFormat
     * - getFormatter(): iElementPairs.size()==2 shortcut, Composite creation
     * - isPrinter/isParser/isFormatter: Composite delegation
     * - inner classes: CharacterLiteral, StringLiteral, NumberFormatter, UnpaddedNumber, PaddedNumber, FixedNumber, TwoDigitYear, TextField, Fraction, TimeZoneOffset, TimeZoneName, TimeZoneId, Composite, MatchingParser
     *
     * Defect targeting: TimeZoneId.parseInto() fails when timezone ID contains underscores
     * (e.g., "America/Dawson_Creek"). The bug is that the method iterates over ALL_IDS set
     * but the iteration order may cause partial matching on "America/Dawson" without the "_Creek"
     * suffix, leading to parsing failure. The test must verify that parsing a DateTime string
     * with such a zone ID works correctly.
     */

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testToFormatterCreatesFormatter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("T");
        DateTimeFormatter f = builder.toFormatter();
        assertNotNull(f);
        assertTrue(f.isPrinter());
        assertTrue(f.isParser());
    }

    @Test(timeout = 4000)
    public void testToFormatterWithOnlyPrinter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormat.forPattern("yyyy").getPrinter());
        DateTimeFormatter f = builder.toFormatter();
        assertTrue(f.isPrinter());
        assertFalse(f.isParser());
    }

    @Test(timeout = 4000)
    public void testToFormatterWithOnlyParser() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormat.forPattern("yyyy").getParser());
        DateTimeFormatter f = builder.toFormatter();
        assertFalse(f.isPrinter());
        assertTrue(f.isParser());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToFormatterThrowsWhenNeitherPrinterNorParser() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.toFormatter();
    }

    @Test(timeout = 4000)
    public void testToPrinterSuccess() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("A");
        DateTimePrinter printer = builder.toPrinter();
        assertNotNull(printer);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToPrinterThrowsWhenNoPrinter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormat.forPattern("yyyy").getParser());
        builder.toPrinter();
    }

    @Test(timeout = 4000)
    public void testToParserSuccess() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormat.forPattern("yyyy").getParser());
        DateTimeParser parser = builder.toParser();
        assertNotNull(parser);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testToParserThrowsWhenNoParser() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("A");
        builder.toParser();
    }

    @Test(timeout = 4000)
    public void testCanBuildMethods() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        assertFalse(builder.canBuildFormatter());
        assertFalse(builder.canBuildPrinter());
        assertFalse(builder.canBuildParser());

        builder.appendLiteral("X");
        assertTrue(builder.canBuildFormatter());
        assertTrue(builder.canBuildPrinter());
        assertTrue(builder.canBuildParser());

        builder.clear();
        assertFalse(builder.canBuildFormatter());

        builder.append(DateTimeFormat.forPattern("yyyy").getPrinter());
        assertTrue(builder.canBuildPrinter());
        assertFalse(builder.canBuildParser());
    }

    @Test(timeout = 4000)
    public void testClearResetsState() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("test");
        builder.clear();
        assertFalse(builder.canBuildFormatter());
        // verify no exception when building after clear (should throw)
        try {
            builder.toFormatter();
            fail("Should have thrown");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testAppendNullFormatterThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.append((DateTimeFormatter) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No formatter supplied"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendNullPrinterThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.append((DateTimePrinter) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No printer supplied"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendNullParserThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.append((DateTimeParser) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No parser supplied"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendNullPrinterParserPairThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.append((DateTimePrinter) null, DateTimeFormat.forPattern("yyyy").getParser());
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testAppendNullParserArrayThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.append(DateTimeFormat.forPattern("yyyy").getPrinter(), (DateTimeParser[]) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No parsers supplied"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendParserArrayWithNullMiddleElementThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeParser parser1 = DateTimeFormat.forPattern("yyyy").getParser();
        DateTimeParser parser2 = null;
        try {
            builder.append(null, new DateTimeParser[] {parser1, parser2});
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Incomplete parser array"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendParserArrayWithNullLastElement() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeParser parser1 = DateTimeFormat.forPattern("yyyy").getParser();
        DateTimeFormatter f = builder.append(null, new DateTimeParser[] {parser1, null}).toFormatter();
        assertFalse(f.isPrinter());
        assertTrue(f.isParser());
    }

    @Test(timeout = 4000)
    public void testAppendLiteralNullStringThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendLiteral((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Literal must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendLiteralEmptyString() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        // empty string should append nothing
        builder.appendLiteral("");
        assertFalse(builder.canBuildFormatter());
        // but after adding something else, should work
        builder.appendLiteral("A");
        assertTrue(builder.canBuildFormatter());
    }

    @Test(timeout = 4000)
    public void testAppendLiteralSingleChar() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral('X');
        DateTimeFormatter f = builder.toFormatter();
        assertEquals("X", f.print(0L));
    }

    @Test(timeout = 4000)
    public void testAppendLiteralMultiChar() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("Hello");
        DateTimeFormatter f = builder.toFormatter();
        assertEquals("Hello", f.print(0L));
    }

    @Test(timeout = 4000)
    public void testAppendDecimalFieldNullThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendDecimal(null, 1, 2);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Field type must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendDecimalInvalidMinMax() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendDecimal(DateTimeFieldType.year(), -1, 2);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testAppendDecimalMinDigitsOneUsesUnpadded() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDecimal(DateTimeFieldType.year(), 1, 4);
        DateTimeFormatter f = builder.toFormatter();
        // If year is 2019, it should print 2019 (unpadded because min 1)
        DateTime dt = new DateTime(2019, 6, 15, 0, 0, 0, 0);
        assertEquals("2019", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendDecimalMinDigitsGreaterThanOneUsesPadded() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDecimal(DateTimeFieldType.year(), 2, 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2019, 6, 15, 0, 0, 0, 0);
        // year 2019 already 4 digits, but min 2 so no extra padding
        assertEquals("2019", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendFixedDecimalNullThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendFixedDecimal(null, 2);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Field type must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendFixedDecimalInvalidDigits() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendFixedDecimal(DateTimeFieldType.year(), 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Illegal number of digits"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendFixedDecimalWorks() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendFixedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        assertEquals("2020", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendFractionNullFieldThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendFraction(null, 1, 3);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Field type must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendFractionInvalidParams() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendFraction(DateTimeFieldType.secondOfDay(), -1, 3);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testAppendFractionOfSecond() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendFractionOfSecond(3, 3);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 6, 15, 10, 30, 45, 123);
        // fraction of second for 123 ms = 123 (since secondOfDay remainder → fraction)
        String result = f.print(dt);
        assertEquals("123", result);
    }

    @Test(timeout = 4000)
    public void testAppendTextNullFieldThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendText(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Field type must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendShortTextNullFieldThrows() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendShortText(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Field type must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneNameWithoutLookup() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneName();
        DateTimeFormatter f = builder.toFormatter();
        assertTrue(f.isPrinter());
        assertFalse(f.isParser());
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneShortNameWithoutLookup() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneShortName();
        DateTimeFormatter f = builder.toFormatter();
        assertTrue(f.isPrinter());
        assertFalse(f.isParser());
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneNameWithLookup() {
        Map<String, DateTimeZone> lookup = new LinkedHashMap<String, DateTimeZone>();
        lookup.put("PST", DateTimeZone.forID("America/Los_Angeles"));
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneName(lookup);
        DateTimeFormatter f = builder.toFormatter();
        assertTrue(f.isPrinter());
        assertTrue(f.isParser());
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneShortNameWithLookup() {
        Map<String, DateTimeZone> lookup = new LinkedHashMap<String, DateTimeZone>();
        lookup.put("EST", DateTimeZone.forID("America/New_York"));
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneShortName(lookup);
        DateTimeFormatter f = builder.toFormatter();
        assertTrue(f.isPrinter());
        assertTrue(f.isParser());
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneId() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneId();
        DateTimeFormatter f = builder.toFormatter();
        assertTrue(f.isPrinter());
        assertTrue(f.isParser());
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime dt = new DateTime(2020, 6, 15, 12, 0, zone);
        assertEquals("Europe/London", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneOffsetFourArg() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset("Z", true, 2, 4);
        DateTimeFormatter f = builder.toFormatter();
        // For UTC, should print "Z"
        assertEquals("Z", f.print(0L));
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneOffsetFiveArg() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset("GMT", "Z", true, 2, 4);
        DateTimeFormatter f = builder.toFormatter();
        // For UTC, should print "Z" (parse text) or "GMT" (print text)?? Actually print text is "GMT"
        // Wait: first arg is zeroOffsetPrintText, second is zeroOffsetParseText
        // For UTC, it should print "GMT"
        assertEquals("GMT", f.print(0L));
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneOffsetInvalidMinFields() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            // minFields <= 0 should throw
            builder.appendTimeZoneOffset("Z", true, 0, 2);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testAppendTimeZoneOffsetMaxLessThanMin() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        try {
            builder.appendTimeZoneOffset("Z", true, 3, 2);
            fail();
        } catch (IllegalArgumentException e) { }
    }

    @Test(timeout = 4000)
    public void testAppendOptionalParser() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendOptional(DateTimeFormat.forPattern("yyyy").getParser());
        DateTimeFormatter f = builder.toFormatter();
        assertFalse(f.isPrinter());
        assertTrue(f.isParser());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // Targets the known defect with TimeZoneId parsing of "America/Dawson_Creek"

    @Test(timeout = 4000)
    public void testPrintParseZoneDawsonCreek() {
        // This test reproduces the exact scenario from the Defects4J test case
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendPattern("yyyy-MM-dd HH:mm ");
        builder.appendTimeZoneId();
        DateTimeFormatter f = builder.toFormatter();

        DateTimeZone zone = DateTimeZone.forID("America/Dawson_Creek");
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, 0, zone);
        String printed = f.print(dt);
        assertEquals("2007-03-04 12:30 America/Dawson_Creek", printed);

        // Now parse it back - this is where the bug manifests
        DateTime parsed = f.parseDateTime(printed);
        assertEquals("America/Dawson_Creek", parsed.getZone().getID());
        assertEquals(2007, parsed.getYear());
        assertEquals(3, parsed.getMonthOfYear());
        assertEquals(4, parsed.getDayOfMonth());
        assertEquals(12, parsed.getHourOfDay());
        assertEquals(30, parsed.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testParseTimeZoneIdWithUnderscore() {
        // Additional test to stress-test timezone IDs with underscores
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendPattern("yyyy-MM-dd HH:mm ");
        builder.appendTimeZoneId();
        DateTimeFormatter f = builder.toFormatter();

        String[] zoneIdsWithUnderscore = {
            "America/Port-au-Prince",
            "America/St_Johns",
            "America/Argentina/Buenos_Aires",
            "America/Dawson_Creek",
            "America/Miquelon",
            "Asia/Jayapura",
            "Pacific/Port_Moresby"
        };

        for (String zoneId : zoneIdsWithUnderscore) {
            DateTimeZone zone = DateTimeZone.forID(zoneId);
            DateTime dt = new DateTime(2015, 8, 10, 10, 0, 0, 0, zone);
            String printed = f.print(dt);
            // Check that the printed string contains the zone ID
            assertTrue("Printed string should contain zone ID: " + zoneId,
                       printed.endsWith(zoneId));
            try {
                DateTime parsed = f.parseDateTime(printed);
                assertEquals("Parsed zone should match for " + zoneId,
                             zoneId, parsed.getZone().getID());
            } catch (Exception e) {
                fail("Failed to parse " + zoneId + ": " + e.getMessage());
            }
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testAppendTwoDigitYear() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTwoDigitYear(2000);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2049, 6, 15, 0, 0, 0, 0);
        assertEquals("49", f.print(dt));
        // Parsing should work
        DateTime parsed = f.parseDateTime("99");
        assertEquals(1999, parsed.getYear());
    }

    @Test(timeout = 4000)
    public void testAppendTwoDigitYearWithLenient() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTwoDigitYear(2000, true);
        DateTimeFormatter f = builder.toFormatter();
        // Lenient with 4 digits should parse absolute year
        DateTime parsed = f.parseDateTime("2020");
        assertEquals(2020, parsed.getYear());
    }

    @Test(timeout = 4000)
    public void testPrintWithNullZone() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset("Z", true, 2, 2);
        DateTimeFormatter f = builder.toFormatter();
        // When zone is null, printTo should handle gracefully (returns without appending)
        assertEquals("", f.print(0L));
    }

    @Test(timeout = 4000)
    public void testCompositeWithEmptyPairList() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        // No elements added, but getFormatter should still work (returns Composite with empty)
        assertFalse(builder.canBuildFormatter());
    }

    @Test(timeout = 4000)
    public void testMatchingParserFallback() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeParser[] parsers = new DateTimeParser[] {
            DateTimeFormat.forPattern("yyyy/MM/dd").getParser(),
            DateTimeFormat.forPattern("yyyy-MM-dd").getParser()
        };
        builder.append(null, parsers);
        DateTimeFormatter f = builder.toFormatter();
        assertFalse(f.isPrinter());
        assertTrue(f.isParser());
        // Test parsing with first pattern
        DateTime dt1 = f.parseDateTime("2020/06/15");
        assertEquals(2020, dt1.getYear());
        // Test parsing with second pattern
        DateTime dt2 = f.parseDateTime("2020-06-15");
        assertEquals(2020, dt2.getYear());
    }

    @Test(timeout = 4000)
    public void testToStringFormats() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("Hello");
        builder.appendDayOfMonth(2);
        DateTimeFormatter f = builder.toFormatter();
        assertNotNull(f);
    }

    @Test(timeout = 4000)
    public void testAppendFractionOfMinute() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendFractionOfMinute(2, 4);
        DateTimeFormatter f = builder.toFormatter();
        // Fraction of minute for 30 seconds = 0.5 minute
        DateTime dt = new DateTime(2020, 1, 1, 10, 30, 0, 0);
        String result = f.print(dt);
        assertNotNull(result);
        assertTrue(result.length() >= 2);
    }

    @Test(timeout = 4000)
    public void testAppendFractionOfHour() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendFractionOfHour(1, 3);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 12, 30, 0, 0);
        String result = f.print(dt);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testAppendFractionOfDay() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendFractionOfDay(3, 6);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 6, 0, 0, 0);
        String result = f.print(dt);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testAppendMillisOfSecond() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendMillisOfSecond(3);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 123);
        assertEquals("123", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendMillisOfDay() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendMillisOfDay(5);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 123);
        // millis of day = 123, printed with min 5 digits = "00123"
        assertEquals("00123", f.print(dt));
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testBuilderReuseAfterToFormatter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("A");
        DateTimeFormatter f1 = builder.toFormatter();
        // Adding more after creating formatter should not affect f1
        builder.appendLiteral("B");
        DateTimeFormatter f2 = builder.toFormatter();
        assertEquals("A", f1.print(0L));
        assertEquals("AB", f2.print(0L));
    }

    @Test(timeout = 4000)
    public void testMultipleAppendsChain() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("Year: ")
               .appendYear(4, 4)
               .appendLiteral("-Month: ")
               .appendMonthOfYear(2);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 6, 15, 0, 0, 0, 0);
        assertEquals("Year: 2020-Month: 06", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendPattern() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendPattern("yyyy-MM-dd");
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 6, 15, 0, 0, 0, 0);
        assertEquals("2020-06-15", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendSignedDecimal() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendSignedDecimal(DateTimeFieldType.year(), 1, 6);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(-500, 1, 1, 0, 0, 0, 0);
        // Negative years should print with sign
        String printed = f.print(dt);
        assertTrue(printed.startsWith("-"));
    }

    @Test(timeout = 4000)
    public void testAppendFixedSignedDecimal() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendFixedSignedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        assertEquals("2020", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendTextAndShortText() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendMonthOfYearText();
        builder.appendLiteral(" ");
        builder.appendMonthOfYearShortText();
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 6, 15, 0, 0, 0, 0);
        String printed = f.print(dt);
        assertEquals("June Jun", printed);
    }

    @Test(timeout = 4000)
    public void testAppendEraText() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendEraText();
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        assertEquals("AD", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendHalfdayOfDayText() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendHalfdayOfDayText();
        DateTimeFormatter f = builder.toFormatter();
        DateTime dtMorning = new DateTime(2020, 1, 1, 8, 0, 0, 0);
        assertEquals("AM", f.print(dtMorning));
        DateTime dtEvening = new DateTime(2020, 1, 1, 20, 0, 0, 0);
        assertEquals("PM", f.print(dtEvening));
    }

    @Test(timeout = 4000)
    public void testAppendDayOfWeekText() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDayOfWeekText();
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 6, 15, 0, 0, 0, 0); // Monday
        assertEquals("Monday", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendDayOfWeekShortText() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDayOfWeekShortText();
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 6, 15, 0, 0, 0, 0); // Monday
        assertEquals("Mon", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendCenturyOfEra() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendCenturyOfEra(1, 2);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        assertEquals("20", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendYearOfEra() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendYearOfEra(4, 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        assertEquals("2020", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendYearOfCentury() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendYearOfCentury(2, 2);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        assertEquals("20", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendTwoDigitWeekyear() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTwoDigitWeekyear(2000);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        // Weekyear 2020 -> 20
        assertEquals("20", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testAppendTwoDigitWeekyearWithLenient() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTwoDigitWeekyear(2000, true);
        DateTimeFormatter f = builder.toFormatter();
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        assertEquals("20", f.print(dt));
    }

    @Test(timeout = 4000)
    public void testPrintWithReadablePartial() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDayOfMonth(2);
        builder.appendLiteral("-");
        builder.appendMonthOfYear(2);
        DateTimeFormatter f = builder.toFormatter();
        ReadablePartial partial = new LocalDateTime(2020, 6, 15, 0, 0, 0, 0);
        assertEquals("15-06", f.print(partial));
    }

    @Test(timeout = 4000)
    public void testPrinterOnlyFormatter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormat.forPattern("yyyy").getPrinter());
        DateTimeFormatter f = builder.toFormatter();
        // Should print but cannot parse
        assertEquals("2020", f.print(new DateTime(2020, 1, 1, 0, 0, 0, 0)));
        try {
            f.parseDateTime("2020");
            fail("Should not be able to parse with printer-only formatter");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParserOnlyFormatter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormat.forPattern("yyyy").getParser());
        DateTimeFormatter f = builder.toFormatter();
        try {
            f.print(0L);
            fail("Should not be able to print with parser-only formatter");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        DateTime dt = f.parseDateTime("2020");
        assertEquals(2020, dt.getYear());
    }

    @Test(timeout = 4000)
    public void testTimeZoneOffsetPrintWithPositiveOffset() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset("Z", true, 2, 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0, zone);
        String result = f.withZone(zone).print(dt);
        assertTrue(result.startsWith("+"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneOffsetPrintWithNegativeOffset() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset("Z", true, 2, 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTimeZone zone = DateTimeZone.forOffsetHours(-3);
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0, zone);
        String result = f.withZone(zone).print(dt);
        assertTrue(result.startsWith("-"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneOffsetMinFieldsOnly() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset(null, true, 1, 1);
        DateTimeFormatter f = builder.toFormatter();
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        DateTime dt = new DateTime(2020, 1, 1, 0, 0, 0, 0, zone);
        // Only hours (no minutes because minFields=1, maxFields=1)
        assertEquals("+05", f.withZone(zone).print(dt));
    }

    @Test(timeout = 4000)
    public void testParseTimeZoneOffsetWithZero() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset("Z", "Z", true, 2, 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTime parsed = f.parseDateTime("+00:00");
        assertEquals(DateTimeZone.UTC, parsed.getZone());
    }

    @Test(timeout = 4000)
    public void testParseTimeZoneOffsetWithZeroText() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset("Z", "Z", true, 2, 4);
        DateTimeFormatter f = builder.toFormatter();
        DateTime parsed = f.parseDateTime("Z");
        assertEquals(DateTimeZone.UTC, parsed.getZone());
    }

    @Test(timeout = 4000)
    public void testParseTimeZoneNameWithLookup() {
        Map<String, DateTimeZone> lookup = new LinkedHashMap<String, DateTimeZone>();
        lookup.put("EST", DateTimeZone.forID("America/New_York"));
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendPattern("yyyy-MM-dd HH:mm ");
        builder.appendTimeZoneShortName(lookup);
        DateTimeFormatter f = builder.toFormatter();
        DateTime parsed = f.parseDateTime("2020-06-15 12:00 EST");
        assertEquals("America/New_York", parsed.getZone().getID());
    }

    @Test(timeout = 4000)
    public void testMatchingParserOrdering() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        DateTimeParser[] parsers = new DateTimeParser[] {
            DateTimeFormat.forPattern("MM/dd").getParser(),
            DateTimeFormat.forPattern("dd/MM").getParser(),
            null  // optional
        };
        builder.append(null, parsers);
        DateTimeFormatter f = builder.toFormatter();
        // Should parse "01/02" as MM/dd -> month=1, day=2
        DateTime dt1 = f.parseDateTime("01/02");
        assertEquals(1, dt1.getMonthOfYear());
        assertEquals(2, dt1.getDayOfMonth());
        // Should parse "03/04" but since first fails, second succeeds: day=3, month=4
        // Actually "03/04" can be parsed by first as March 4th
        // Let's test a case that can only be parsed by second: "13/01" (day 13 month 1)
        DateTime dt2 = f.parseDateTime("13/01");
        assertEquals(1, dt2.getMonthOfYear());
        assertEquals(13, dt2.getDayOfMonth());
    }
}