/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.joda.time.format.PeriodFormatterBuilder
 *
 * Key Branches & Decision Coverage:
 * 1. Print Zero Settings:
 *    - PRINT_ZERO_RARELY_LAST (default): prints zero only for the last rarely field when all fields are zero.
 *    - PRINT_ZERO_RARELY_FIRST: prints zero only for the first rarely field when all fields are zero.
 *    - PRINT_ZERO_ALWAYS: forces zero printing even if field value is 0 or unsupported.
 *    - PRINT_ZERO_NEVER: never prints zero.
 *    - PRINT_ZERO_IF_SUPPORTED: prints zero only if supported by PeriodType.
 * 2. Field Types & Fractions:
 *    - YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLIS
 *    - SECONDS_MILLIS and SECONDS_OPTIONAL_MILLIS with fractional parsing and printing (decimal point, padded millis).
 *    - Boundary conditions: negative values, fractional seconds where seconds == 0 and millis < 0 (Defects4J defect!).
 * 3. Affixes:
 *    - SimpleAffix: prefix/suffix matching and scanning (including scanning past numeric characters).
 *    - PluralAffix: singular vs plural selection, length-based swap during parse and scan.
 *    - CompositeAffix: chained prefix/suffix delegates.
 * 4. Separators:
 *    - appendSeparator(text), appendSeparator(text, finalText), appendSeparator(text, finalText, variants)
 *    - useBefore and useAfter combinations (appendSeparatorIfFieldsBefore, appendSeparatorIfFieldsAfter).
 *    - Empty element pairs edge case (leading separator).
 *    - Adjacent separator exception validation.
 * 5. Parsing & Boundary Logic:
 *    - Signed parsing (+ and -) and rejectSignedValues(true/false).
 *    - Digit count limits (minimumPrintedDigits, maximumParsedDigits).
 *    - parseInt with lengths >= 10 (Integer.parseInt path) vs fast manual digit parsing loop.
 *    - Fallback and mismatch states (~position).
 *
 * Defects4J Ground Truth Target:
 * - TestISOPeriodFormat::testFormatStandard_negative:
 *   When a negative period has seconds = 0 and negative millis (e.g., -8ms), formatting with
 *   SECONDS_OPTIONAL_MILLIS or SECONDS_MILLIS fails to output the leading negative sign ("-0.008"),
 *   incorrectly yielding positive "0.008" because integer division (-8 / 1000) produces 0.
 */
package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.MutablePeriod;
import org.joda.time.DurationFieldType;

public class PeriodFormatterBuilderGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Negative Fraction Bug)
    // =========================================================================

    /**
     * Targets the defect where negative period of less than 1 second (e.g. -8 millis)
     * loses its negative sign because (-8 / 1000) == 0, causing FormatUtils to print "0" instead of "-0".
     */
    @Test(timeout = 4000)
    public void testDefectNegativeFractionalSeconds_SecondsMillis() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendLiteral("PT")
                .appendSecondsWithMillis()
                .appendLiteral("S")
                .toFormatter();

        Period period = new Period(0, 0, 0, -8); // 0 seconds, -8 millis
        String formatted = formatter.print(period);
        assertEquals("PT-0.008S", formatted);
    }

    /**
     * Targets the defect using appendSecondsWithOptionalMillis() which ISOPeriodFormat uses.
     */
    @Test(timeout = 4000)
    public void testDefectNegativeFractionalSeconds_OptionalMillis() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendLiteral("PT")
                .appendSecondsWithOptionalMillis()
                .appendLiteral("S")
                .toFormatter();

        Period period = new Period(0, 0, 0, -8);
        String formatted = formatter.print(period);
        assertEquals("PT-0.008S", formatted);
    }

    /**
     * Targets the defect via Writer printTo implementation.
     */
    @Test(timeout = 4000)
    public void testDefectNegativeFractionalSeconds_WriterOutput() throws IOException {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendSecondsWithMillis()
                .toFormatter();

        Period period = new Period(0, 0, 0, -500);
        StringWriter sw = new StringWriter();
        formatter.printTo(sw, period);
        assertEquals("-0.500", sw.toString());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicFieldsPrintingAndParsing() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("y")
                .appendMonths().appendSuffix("m")
                .appendWeeks().appendSuffix("w")
                .appendDays().appendSuffix("d")
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("min")
                .appendSeconds().appendSuffix("s")
                .appendMillis().appendSuffix("ms")
                .toFormatter();

        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        String printed = formatter.print(p);
        assertEquals("1y2m3w4d5h6min7s8ms", printed);

        Period parsed = formatter.parsePeriod("1y2m3w4d5h6min7s8ms");
        assertEquals(p, parsed);
    }

    @Test(timeout = 4000)
    public void testMillis3Digit() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendMillis3Digit()
                .toFormatter();

        Period p = Period.millis(5);
        assertEquals("005", formatter.print(p));

        Period parsed = formatter.parsePeriod("005");
        assertEquals(5, parsed.getMillis());
    }

    @Test(timeout = 4000)
    public void testPluralAffixHandling() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" hour", " hours")
                .toFormatter();

        assertEquals("1 hour", formatter.print(Period.hours(1)));
        assertEquals("2 hours", formatter.print(Period.hours(2)));
        assertEquals("0 hours", formatter.print(Period.hours(0)));

        assertEquals(1, formatter.parsePeriod("1 hour").getHours());
        assertEquals(2, formatter.parsePeriod("2 hours").getHours());
        assertEquals(1, formatter.parsePeriod("1 hours").getHours()); // Plural accepted for 1
    }

    @Test(timeout = 4000)
    public void testPrefixHandling() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendPrefix("Y:", "Years:")
                .appendYears()
                .toFormatter();

        assertEquals("Y:1", formatter.print(Period.years(1)));
        assertEquals("Years:2", formatter.print(Period.years(2)));

        assertEquals(1, formatter.parsePeriod("Y:1").getYears());
        assertEquals(2, formatter.parsePeriod("Years:2").getYears());
    }

    @Test(timeout = 4000)
    public void testCompositeAffixes() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendPrefix("[")
                .appendPrefix("Y=")
                .appendYears()
                .appendSuffix("]")
                .appendSuffix(";")
                .toFormatter();

        assertEquals("[Y=5];", formatter.print(Period.years(5)));
        assertEquals(5, formatter.parsePeriod("[Y=5];").getYears());
    }

    @Test(timeout = 4000)
    public void testPrintZeroSettings() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        Period zeroPeriod = Period.ZERO;

        // printZeroRarelyLast (default)
        PeriodFormatter fLast = builder.clear()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();
        assertEquals("0m", fLast.print(zeroPeriod));

        // printZeroRarelyFirst
        PeriodFormatter fFirst = builder.clear()
                .printZeroRarelyFirst()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();
        assertEquals("0h", fFirst.print(zeroPeriod));

        // printZeroNever
        PeriodFormatter fNever = builder.clear()
                .printZeroNever()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();
        assertEquals("", fNever.print(zeroPeriod));

        // printZeroAlways
        PeriodFormatter fAlways = builder.clear()
                .printZeroAlways()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();
        assertEquals("0h0m", fAlways.print(zeroPeriod));

        // printZeroIfSupported
        PeriodType typeHoursOnly = PeriodType.hours();
        PeriodFormatter fSupported = builder.clear()
                .printZeroIfSupported()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();
        Period pSupported = new Period(0, 0, 0, 0, 0, 0, 0, 0, typeHoursOnly);
        assertEquals("0h", fSupported.print(pSupported));
    }

    @Test(timeout = 4000)
    public void testSeparatorsBasicAndFinalText() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix("d")
                .appendSeparator(", ", " and ")
                .appendHours().appendSuffix("h")
                .appendSeparator(", ", " and ")
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        // 3 fields
        assertEquals("1d, 2h and 3m", formatter.print(new Period(0, 0, 0, 1, 2, 3, 0, 0)));
        // 2 fields
        assertEquals("1d and 2h", formatter.print(new Period(0, 0, 0, 1, 2, 0, 0, 0)));
        assertEquals("2h and 3m", formatter.print(new Period(0, 0, 0, 0, 2, 3, 0, 0)));
        // 1 field
        assertEquals("1d", formatter.print(new Period(0, 0, 0, 1, 0, 0, 0, 0)));

        // Parsing
        Period parsed = formatter.parsePeriod("1d, 2h and 3m");
        assertEquals(1, parsed.getDays());
        assertEquals(2, parsed.getHours());
        assertEquals(3, parsed.getMinutes());
    }

    @Test(timeout = 4000)
    public void testSeparatorVariants() {
        String[] variants = new String[] { "&", "/" };
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix("h")
                .appendSeparator(" and ", " and ", variants)
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        assertEquals(10, formatter.parsePeriod("1h and 10m").getMinutes());
        assertEquals(20, formatter.parsePeriod("1h&20m").getMinutes());
        assertEquals(30, formatter.parsePeriod("1h/30m").getMinutes());
    }

    @Test(timeout = 4000)
    public void testSeparatorIfFieldsBeforeAndAfter() {
        PeriodFormatter fBefore = new PeriodFormatterBuilder()
                .appendDays().appendSuffix("d")
                .appendSeparatorIfFieldsBefore(";")
                .appendHours().appendSuffix("h")
                .toFormatter();

        assertEquals("1d;2h", fBefore.print(new Period(0, 0, 0, 1, 2, 0, 0, 0)));
        assertEquals("1d;", fBefore.print(new Period(0, 0, 0, 1, 0, 0, 0, 0)));
        assertEquals("2h", fBefore.print(new Period(0, 0, 0, 0, 2, 0, 0, 0)));

        PeriodFormatter fAfter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix("d")
                .appendSeparatorIfFieldsAfter(";")
                .appendHours().appendSuffix("h")
                .toFormatter();

        assertEquals("1d;2h", fAfter.print(new Period(0, 0, 0, 1, 2, 0, 0, 0)));
        assertEquals("1d", fAfter.print(new Period(0, 0, 0, 1, 0, 0, 0, 0)));
        assertEquals(";2h", fAfter.print(new Period(0, 0, 0, 0, 2, 0, 0, 0)));
    }

    @Test(timeout = 4000)
    public void testLeadingSeparatorWithFinish() {
        // Appending separator before any fields produces a leading separator
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendSeparatorIfFieldsAfter("T")
                .appendHours().appendSuffix("H")
                .toFormatter();

        assertEquals("T5H", formatter.print(Period.hours(5)));
        assertEquals(5, formatter.parsePeriod("T5H").getHours());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Digit Bounds
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinimumPrintedDigitsPadding() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .minimumPrintedDigits(4)
                .appendYears()
                .toFormatter();

        assertEquals("0007", formatter.print(Period.years(7)));
        assertEquals("12345", formatter.print(Period.years(12345)));
    }

    @Test(timeout = 4000)
    public void testMaximumParsedDigitsLimit() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .maximumParsedDigits(2)
                .appendYears()
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        int endPos = formatter.getParser().parseInto(mp, "12345", 0, Locale.getDefault());
        assertEquals(2, endPos);
        assertEquals(12, mp.getYears());
    }

    @Test(timeout = 4000)
    public void testRejectSignedValues() {
        PeriodFormatter rejecting = new PeriodFormatterBuilder()
                .rejectSignedValues(true)
                .appendHours()
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        int resNeg = rejecting.getParser().parseInto(mp, "-5", 0, Locale.getDefault());
        assertTrue(resNeg < 0);

        int resPos = rejecting.getParser().parseInto(mp, "+5", 0, Locale.getDefault());
        assertTrue(resPos < 0);

        int resUnsigned = rejecting.getParser().parseInto(mp, "5", 0, Locale.getDefault());
        assertEquals(1, resUnsigned);
        assertEquals(5, mp.getHours());
    }

    @Test(timeout = 4000)
    public void testAcceptedSignedValues() {
        PeriodFormatter accepting = new PeriodFormatterBuilder()
                .rejectSignedValues(false)
                .appendHours()
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        int resNeg = accepting.getParser().parseInto(mp, "-5", 0, Locale.getDefault());
        assertEquals(2, resNeg);
        assertEquals(-5, mp.getHours());

        mp.clear();
        int resPos = accepting.getParser().parseInto(mp, "+12", 0, Locale.getDefault());
        assertEquals(3, resPos);
        assertEquals(12, mp.getHours());
    }

    @Test(timeout = 4000)
    public void testParseIntTenOrMoreDigits() {
        // >= 10 digits triggers Integer.parseInt fallback branch in parseInt()
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .maximumParsedDigits(10)
                .appendSeconds()
                .toFormatter();

        Period p = formatter.parsePeriod("1000000000"); // 10 digits (1 billion)
        assertEquals(1000000000, p.getSeconds());
    }

    @Test(timeout = 4000)
    public void testFractionalSecondsDecimalsParsing() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendSecondsWithOptionalMillis()
                .toFormatter();

        // 1 digit millis
        Period p1 = formatter.parsePeriod("12.3");
        assertEquals(12, p1.getSeconds());
        assertEquals(300, p1.getMillis());

        // 2 digits millis
        Period p2 = formatter.parsePeriod("12.34");
        assertEquals(12, p2.getSeconds());
        assertEquals(340, p2.getMillis());

        // 3 digits millis
        Period p3 = formatter.parsePeriod("12.345");
        assertEquals(12, p3.getSeconds());
        assertEquals(345, p3.getMillis());

        // 4 digits millis (truncated to 3 digits)
        Period p4 = formatter.parsePeriod("12.3456");
        assertEquals(12, p4.getSeconds());
        assertEquals(345, p4.getMillis());

        // comma separator for fraction
        Period p5 = formatter.parsePeriod("12,5");
        assertEquals(12, p5.getSeconds());
        assertEquals(500, p5.getMillis());

        // Negative whole value with fraction
        Period p6 = formatter.parsePeriod("-12.5");
        assertEquals(-12, p6.getSeconds());
        assertEquals(-500, p6.getMillis());
    }

    @Test(timeout = 4000)
    public void testAppendExistingFormatterAndPrinterParser() {
        PeriodFormatter part1 = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("y")
                .toFormatter();

        PeriodFormatter composed = new PeriodFormatterBuilder()
                .append(part1)
                .appendLiteral("-")
                .append(part1.getPrinter(), part1.getParser())
                .toFormatter();

        assertEquals("1y-2y", composed.print(new Period(1, 0, 0, 0, 0, 0, 0, 0))
                + "-" + part1.print(Period.years(2)));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullFormatterThrows() {
        new PeriodFormatterBuilder().append((PeriodFormatter) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullPrinterAndParserThrows() {
        new PeriodFormatterBuilder().append(null, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullLiteralThrows() {
        new PeriodFormatterBuilder().appendLiteral(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullPrefixThrows() {
        new PeriodFormatterBuilder().appendPrefix((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullPluralPrefixThrows() {
        new PeriodFormatterBuilder().appendPrefix(null, "plural");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullPluralPrefixThrows2() {
        new PeriodFormatterBuilder().appendPrefix("singular", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullSuffixThrows() {
        new PeriodFormatterBuilder().appendSuffix((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendNullPluralSuffixThrows() {
        new PeriodFormatterBuilder().appendSuffix("singular", null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSuffixWithoutPrecedingFieldThrows() {
        new PeriodFormatterBuilder().appendSuffix("s");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSuffixAfterSeparatorThrows() {
        new PeriodFormatterBuilder()
                .appendYears()
                .appendSeparator("-")
                .appendSuffix("s");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testPrefixNotFollowedByFieldThrows() {
        new PeriodFormatterBuilder()
                .appendPrefix("P")
                .toFormatter();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testTwoAdjacentSeparatorsThrow() {
        new PeriodFormatterBuilder()
                .appendYears()
                .appendSeparator("A")
                .appendSeparator("B");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullSeparatorTextThrows() {
        new PeriodFormatterBuilder().appendSeparator(null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNeitherPrinterNorParserThrows() {
        // Build formatter with printer-only and parser-only parts misaligned or empty
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.append(null, new PeriodParser() {
            public int parseInto(ReadWritablePeriod p, String t, int pos, Locale l) { return pos; }
        });
        builder.append(new PeriodPrinter() {
            public int calculatePrintedLength(ReadablePeriod p, Locale l) { return 0; }
            public int countFieldsToPrint(ReadablePeriod p, int stopAt, Locale l) { return 0; }
            public void printTo(StringBuffer b, ReadablePeriod p, Locale l) {}
            public void printTo(java.io.Writer w, ReadablePeriod p, Locale l) {}
        }, null);
        builder.toFormatter();
    }

    // =========================================================================
    // Partition E: Parser/Printer Partial States & Lifecycle Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrinterOnlyAndParserOnlyBuilds() {
        PeriodPrinter dummyPrinter = new PeriodPrinter() {
            public int calculatePrintedLength(ReadablePeriod p, Locale l) { return 1; }
            public int countFieldsToPrint(ReadablePeriod p, int stopAt, Locale l) { return 1; }
            public void printTo(StringBuffer b, ReadablePeriod p, Locale l) { b.append("X"); }
            public void printTo(java.io.Writer w, ReadablePeriod p, Locale l) throws IOException { w.write("X"); }
        };

        PeriodFormatterBuilder builder1 = new PeriodFormatterBuilder();
        builder1.append(dummyPrinter, null);
        assertNotNull(builder1.toPrinter());
        assertNull(builder1.toParser());
        assertTrue(builder1.toFormatter().isPrinter());
        assertFalse(builder1.toFormatter().isParser());

        PeriodParser dummyParser = new PeriodParser() {
            public int parseInto(ReadWritablePeriod p, String t, int pos, Locale l) { return pos; }
        };

        PeriodFormatterBuilder builder2 = new PeriodFormatterBuilder();
        builder2.append(null, dummyParser);
        assertNull(builder2.toPrinter());
        assertNotNull(builder2.toParser());
        assertFalse(builder2.toFormatter().isPrinter());
        assertTrue(builder2.toFormatter().isParser());
    }

    @Test(timeout = 4000)
    public void testParseMismatchAndErrors() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendLiteral("START")
                .appendHours().appendSuffix("h")
                .toFormatter();

        MutablePeriod mp = new MutablePeriod();
        int res = formatter.getParser().parseInto(mp, "WRONG", 0, Locale.getDefault());
        assertTrue("Expected negative position on mismatch", res < 0);

        int resMissingSuffix = formatter.getParser().parseInto(mp, "START5x", 0, Locale.getDefault());
        assertTrue(resMissingSuffix < 0);
    }

    @Test(timeout = 4000)
    public void testScanAffixWithInterveningNumericCharacters() {
        // Tests the scan logic of SimpleAffix and PluralAffix where digits are skipped
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix("d")
                .appendHours().appendSuffix("h")
                .toFormatter();

        Period p = formatter.parsePeriod("5d12h");
        assertEquals(5, p.getDays());
        assertEquals(12, p.getHours());
    }

    @Test(timeout = 4000)
    public void testCalculatePrintedLengthAndCountFields() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("y")
                .appendMonths().appendSuffix("m")
                .appendSeparator(" ")
                .appendSecondsWithMillis().appendSuffix("s")
                .toFormatter();

        Period p = new Period(2, 5, 0, 0, 0, 0, 10, 250);
        int expectedLen = formatter.print(p).length();
        assertEquals(expectedLen, formatter.getPrinter().calculatePrintedLength(p, Locale.getDefault()));
        assertEquals(3, formatter.getPrinter().countFieldsToPrint(p, Integer.MAX_VALUE, Locale.getDefault()));
        assertEquals(1, formatter.getPrinter().countFieldsToPrint(p, 1, Locale.getDefault()));
    }
}