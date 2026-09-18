/*
 *  Copyright 2001-2009 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time.format;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.DateTimeConstants;
import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------------------
 * Target Class: PeriodFormatterBuilder
 * Key Focus Areas:
 *  1. Defect Bug 2495455: Parsing large numeric field values (>= 10 digits) such as "PT1003199059S" where parseInt
 *     internal logic previously overflowed or threw format exceptions.
 *  2. Field Formatters: All 10 supported field types (YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLIS,
 *     SECONDS_MILLIS, SECONDS_OPTIONAL_MILLIS) and their calculate/print/parse/overflow logic.
 *  3. Print Zero Settings: PRINT_ZERO_ALWAYS, PRINT_ZERO_NEVER, PRINT_ZERO_RARELY_FIRST, PRINT_ZERO_RARELY_LAST,
 *     and PRINT_ZERO_IF_SUPPORTED under both zero and non-zero period conditions.
 *  4. Affixes: SimpleAffix, PluralAffix (swap logic when singular.length > plural.length and vice versa), and
 *     CompositeAffix (multiple chained prefixes/suffixes).
 *  5. Separators: standard, ifFieldsBefore, ifFieldsAfter, 2-way vs 3-way, variants sorting, adjacent separators check,
 *     and zero-formatter prefix/empty separator handling.
 *  6. Builder Guards: Null checks, illegal builder states (orphaned prefixes, suffix without field, adjacent separators,
 *     neither printer nor parser available).
 * -------------------------------------------------------------------------------------------------------------------
 */
public class PeriodFormatterBuilderGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Bug 2495455)
    // =========================================================================

    @Test(timeout = 4000)
    public void testBug2495455_LargeSecondsParsing() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendLiteral("P")
            .appendYears()
            .appendSuffix("Y")
            .appendMonths()
            .appendSuffix("M")
            .appendWeeks()
            .appendSuffix("W")
            .appendDays()
            .appendSuffix("D")
            .appendSeparatorIfFieldsAfter("T")
            .appendHours()
            .appendSuffix("H")
            .appendMinutes()
            .appendSuffix("M")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();

        Period period = formatter.parsePeriod("PT1003199059S");
        assertNotNull(period);
        assertEquals(1003199059, period.getSeconds());
        assertEquals(0, period.getMillis());
    }

    @Test(timeout = 4000)
    public void testBug2495455_ISOStandardCompatibility() {
        Period period = ISOPeriodFormat.standard().parsePeriod("PT1003199059S");
        assertNotNull(period);
        assertEquals(1003199059, period.getSeconds());
    }

    @Test(timeout = 4000)
    public void testBug2495455_TenDigitFieldDirect() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendSeconds()
            .appendSuffix("s")
            .toFormatter();

        Period parsed = formatter.parsePeriod("1003199059s");
        assertEquals(1003199059, parsed.getSeconds());

        Period parsedNegative = formatter.parsePeriod("-1003199059s");
        assertEquals(-1003199059, parsedNegative.getSeconds());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllFieldTypesPrintAndParse() {
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

        Period period = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        String printed = formatter.print(period);
        assertEquals("1y2m3w4d5h6min7s8ms", printed);

        Period parsed = formatter.parsePeriod("1y2m3w4d5h6min7s8ms");
        assertEquals(period, parsed);
    }

    @Test(timeout = 4000)
    public void testSecondsWithMillis() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendSecondsWithMillis()
            .appendSuffix("s")
            .toFormatter();

        Period p1 = new Period(0, 0, 0, 0, 0, 0, 5, 42);
        assertEquals("5.042s", formatter.print(p1));
        assertEquals(p1, formatter.parsePeriod("5.042s"));

        Period pZeroMillis = new Period(0, 0, 0, 0, 0, 0, 5, 0);
        assertEquals("5.000s", formatter.print(pZeroMillis));
        assertEquals(pZeroMillis, formatter.parsePeriod("5.000s"));

        Period parsedComma = formatter.parsePeriod("5,042s");
        assertEquals(p1, parsedComma);

        Period parsed1Digit = formatter.parsePeriod("5.4s");
        assertEquals(5, parsed1Digit.getSeconds());
        assertEquals(400, parsed1Digit.getMillis());

        Period parsed2Digits = formatter.parsePeriod("5.42s");
        assertEquals(5, parsed2Digits.getSeconds());
        assertEquals(420, parsed2Digits.getMillis());

        Period parsedNegative = formatter.parsePeriod("-5.420s");
        assertEquals(-5, parsedNegative.getSeconds());
        assertEquals(-420, parsedNegative.getMillis());
    }

    @Test(timeout = 4000)
    public void testSecondsWithOptionalMillis() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendSecondsWithOptionalMillis()
            .appendSuffix("s")
            .toFormatter();

        Period withMillis = new Period(0, 0, 0, 0, 0, 0, 7, 25);
        assertEquals("7.025s", formatter.print(withMillis));

        Period withoutMillis = new Period(0, 0, 0, 0, 0, 0, 7, 0);
        assertEquals("7s", formatter.print(withoutMillis));

        Period parsedNoFract = formatter.parsePeriod("7s");
        assertEquals(withoutMillis, parsedNoFract);

        Period parsedTrailingDot = formatter.parsePeriod("7.s");
        assertEquals(withoutMillis, parsedTrailingDot);
    }

    @Test(timeout = 4000)
    public void testAppendMillis3Digit() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendMillis3Digit()
            .toFormatter();

        assertEquals("005", formatter.print(new Period(0, 0, 0, 0, 0, 0, 0, 5)));
        assertEquals("050", formatter.print(new Period(0, 0, 0, 0, 0, 0, 0, 50)));
        assertEquals("500", formatter.print(new Period(0, 0, 0, 0, 0, 0, 0, 500)));
    }

    @Test(timeout = 4000)
    public void testPrintZeroRarelyFirst() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .toFormatter();

        assertEquals("0h", formatter.print(Period.ZERO));
        assertEquals("5m", formatter.print(new Period().withMinutes(5)));
    }

    @Test(timeout = 4000)
    public void testPrintZeroRarelyLast() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .printZeroRarelyLast()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .toFormatter();

        assertEquals("0s", formatter.print(Period.ZERO));
        assertEquals("5m", formatter.print(new Period().withMinutes(5)));
    }

    @Test(timeout = 4000)
    public void testPrintZeroAlways() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .printZeroAlways()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        assertEquals("0h0m", formatter.print(Period.ZERO));

        Period parsed = formatter.parsePeriod("0h0m");
        assertEquals(0, parsed.getHours());
        assertEquals(0, parsed.getMinutes());
    }

    @Test(timeout = 4000)
    public void testPrintZeroNever() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        assertEquals("", formatter.print(Period.ZERO));
        assertEquals("2h", formatter.print(new Period().withHours(2)));
    }

    @Test(timeout = 4000)
    public void testPrintZeroIfSupported() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .printZeroIfSupported()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        Period periodTime = new Period(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
        assertEquals("0h0m", formatter.print(periodTime));

        Period periodHoursOnly = new Period(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.hours());
        assertEquals("0h", formatter.print(periodHoursOnly));
    }

    @Test(timeout = 4000)
    public void testPluralAffixVariants() {
        PeriodFormatterBuilder builder1 = new PeriodFormatterBuilder()
            .appendDays()
            .appendSuffix(" day", " days");
        PeriodFormatter f1 = builder1.toFormatter();

        assertEquals("1 day", f1.print(new Period().withDays(1)));
        assertEquals("2 days", f1.print(new Period().withDays(2)));
        assertEquals(1, f1.parsePeriod("1 day").getDays());
        assertEquals(2, f1.parsePeriod("2 days").getDays());
        assertEquals(1, f1.parsePeriod("1 days").getDays());
        assertEquals(2, f1.parsePeriod("2 day").getDays());

        PeriodFormatterBuilder builder2 = new PeriodFormatterBuilder()
            .appendDays()
            .appendSuffix(" foot", " feet");
        PeriodFormatter f2 = builder2.toFormatter();

        assertEquals("1 foot", f2.print(new Period().withDays(1)));
        assertEquals("2 feet", f2.print(new Period().withDays(2)));
        assertEquals(1, f2.parsePeriod("1 foot").getDays());
        assertEquals(2, f2.parsePeriod("2 feet").getDays());
    }

    @Test(timeout = 4000)
    public void testPrefixAndCompositeAffixes() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendPrefix("Pre1-", "Pre1s-")
            .appendPrefix("Pre2:")
            .appendHours()
            .appendSuffix(" hr")
            .appendSuffix("!")
            .toFormatter();

        Period period1 = new Period().withHours(1);
        assertEquals("Pre1-Pre2:1 hr!", formatter.print(period1));

        Period period2 = new Period().withHours(2);
        assertEquals("Pre1s-Pre2:2 hr!", formatter.print(period2));

        Period parsed = formatter.parsePeriod("Pre1-Pre2:1 hr!");
        assertEquals(1, parsed.getHours());
    }

    @Test(timeout = 4000)
    public void testWriterPrintingSupport() throws IOException {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendPrefix("P:")
            .appendHours().appendSuffix("h")
            .appendSeparator(" ")
            .appendSecondsWithMillis().appendSuffix("s")
            .toFormatter();

        Period p = new Period().withHours(4).withSeconds(12).withMillis(345);
        StringWriter writer = new StringWriter();
        formatter.printTo(writer, p);
        assertEquals("P:4h 12.345s", writer.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDigitsBoundariesAndSignedRejection() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .minimumPrintedDigits(3)
            .maximumParsedDigits(4)
            .rejectSignedValues(true)
            .appendHours()
            .toFormatter();

        assertEquals("005", formatter.print(new Period().withHours(5)));
        assertEquals("1234", formatter.print(new Period().withHours(1234)));

        Period parsed = formatter.parsePeriod("12345");
        assertEquals(1234, parsed.getHours());

        MutablePeriod mp = new MutablePeriod();
        int parsePos = formatter.getParser().parseInto(mp, "-123", 0, Locale.getDefault());
        assertTrue(parsePos < 0);
    }

    @Test(timeout = 4000)
    public void testSignedValuesAcceptance() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .rejectSignedValues(false)
            .appendHours()
            .toFormatter();

        assertEquals(5, formatter.parsePeriod("+5").getHours());
        assertEquals(-5, formatter.parsePeriod("-5").getHours());

        MutablePeriod mp = new MutablePeriod();
        int parsePosSignOnly = formatter.getParser().parseInto(mp, "+", 0, Locale.getDefault());
        assertTrue(parsePosSignOnly < 0);
    }

    @Test(timeout = 4000)
    public void testSeparatorsTwoAndThreeWay() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendDays()
            .appendSeparator(", ", " and ")
            .appendHours()
            .appendSeparator(", ", " and ")
            .appendMinutes()
            .toFormatter();

        assertEquals("1 day", "1", formatter.print(new Period().withDays(1)));
        assertEquals("1 and 2", formatter.print(new Period().withDays(1).withHours(2)));
        assertEquals("1, 2 and 3", formatter.print(new Period().withDays(1).withHours(2).withMinutes(3)));
    }

    @Test(timeout = 4000)
    public void testSeparatorVariants() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendDays()
            .appendSeparator(", ", " and ", new String[] { " or ", "; " })
            .appendHours()
            .toFormatter();

        assertEquals(1, formatter.parsePeriod("1 or 2").getDays());
        assertEquals(2, formatter.parsePeriod("1 or 2").getHours());

        assertEquals(1, formatter.parsePeriod("1; 2").getDays());
        assertEquals(2, formatter.parsePeriod("1; 2").getHours());

        assertEquals(1, formatter.parsePeriod("1 and 2").getDays());
        assertEquals(2, formatter.parsePeriod("1 and 2").getHours());
    }

    @Test(timeout = 4000)
    public void testSeparatorIfFieldsBeforeAndAfter() {
        PeriodFormatter fBefore = new PeriodFormatterBuilder()
            .appendDays()
            .appendSeparatorIfFieldsBefore(";")
            .appendHours()
            .toFormatter();

        assertEquals("5;", fBefore.print(new Period().withDays(5)));
        assertEquals("3", fBefore.print(new Period().withHours(3)));
        assertEquals("5;3", fBefore.print(new Period().withDays(5).withHours(3)));

        PeriodFormatter fAfter = new PeriodFormatterBuilder()
            .appendDays()
            .appendSeparatorIfFieldsAfter(";")
            .appendHours()
            .toFormatter();

        assertEquals("5", fAfter.print(new Period().withDays(5)));
        assertEquals(";3", fAfter.print(new Period().withHours(3)));
        assertEquals("5;3", fAfter.print(new Period().withDays(5).withHours(3)));
    }

    @Test(timeout = 4000)
    public void testSeparatorLeadingWithoutFieldsBefore() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendSeparatorIfFieldsAfter("T")
            .appendHours().appendSuffix("H")
            .toFormatter();

        assertEquals("T5H", formatter.print(new Period().withHours(5)));
        assertEquals(5, formatter.parsePeriod("T5H").getHours());
    }

    @Test(timeout = 4000)
    public void testCountFieldsToPrint() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendYears()
            .appendMonths()
            .appendDays()
            .toFormatter();

        PeriodPrinter printer = formatter.getPrinter();
        Period p = new Period().withYears(1).withDays(2);
        assertEquals(0, printer.countFieldsToPrint(p, 0, Locale.getDefault()));
        assertEquals(1, printer.countFieldsToPrint(p, 1, Locale.getDefault()));
        assertEquals(2, printer.countFieldsToPrint(p, 5, Locale.getDefault()));
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
    public void testAppendLiteralNullThrows() {
        new PeriodFormatterBuilder().appendLiteral(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendPrefixNullThrows() {
        new PeriodFormatterBuilder().appendPrefix((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendPrefixSingularPluralNullThrows() {
        new PeriodFormatterBuilder().appendPrefix("a", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendSuffixNullThrows() {
        new PeriodFormatterBuilder().appendSuffix((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendSuffixSingularPluralNullThrows() {
        new PeriodFormatterBuilder().appendSuffix(null, "b");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendSeparatorNullThrows() {
        new PeriodFormatterBuilder().appendSeparator(null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testPrefixNotFollowedByFieldThrows() {
        new PeriodFormatterBuilder().appendPrefix("P:").appendLiteral("Lit");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSuffixWithoutFieldThrows() {
        new PeriodFormatterBuilder().appendSuffix("s");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAdjacentSeparatorsThrows() {
        new PeriodFormatterBuilder()
            .appendDays()
            .appendSeparator(",")
            .appendSeparator(";")
            .appendHours();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNeitherPrinterNorParserThrows() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodPrinter dummyPrinter = new PeriodFormatterBuilder().appendLiteral("a").toPrinter();
        PeriodParser dummyParser = new PeriodFormatterBuilder().appendLiteral("b").toParser();
        builder.append(null, dummyParser);
        builder.append(dummyPrinter, null);
        builder.toFormatter();
    }

    // =========================================================================
    // Partition E: Object Lifecycle, State & Composite Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testClearAndBuilderReuse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("Y");
        PeriodFormatter f1 = builder.toFormatter();

        builder.clear();
        builder.appendDays().appendSuffix("D");
        PeriodFormatter f2 = builder.toFormatter();

        assertEquals("5Y", f1.print(new Period().withYears(5)));
        assertEquals("5D", f2.print(new Period().withDays(5)));
    }

    @Test(timeout = 4000)
    public void testToPrinterAndToParserLifecycle() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodParser dummyParser = new PeriodFormatterBuilder().appendLiteral("x").toParser();
        builder.append(null, dummyParser);

        assertNull(builder.toPrinter());
        assertNotNull(builder.toParser());

        builder.clear();
        PeriodPrinter dummyPrinter = new PeriodFormatterBuilder().appendLiteral("x").toPrinter();
        builder.append(dummyPrinter, null);

        assertNotNull(builder.toPrinter());
        assertNull(builder.toParser());
    }

    @Test(timeout = 4000)
    public void testCompositeHierarchyDecomposition() {
        PeriodFormatter subFormatter = new PeriodFormatterBuilder()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        PeriodFormatter mainFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d ")
            .append(subFormatter)
            .appendSuffix("!")
            .toFormatter();

        Period period = new Period().withDays(1).withHours(2).withMinutes(3);
        assertEquals("1d 2h3m!", mainFormatter.print(period));

        Period parsed = mainFormatter.parsePeriod("1d 2h3m!");
        assertEquals(1, parsed.getDays());
        assertEquals(2, parsed.getHours());
        assertEquals(3, parsed.getMinutes());
    }

    @Test(timeout = 4000)
    public void testUnsupportedFieldTypeHandlingInParser() {
        PeriodType hoursOnly = PeriodType.hours();
        PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .toFormatter();

        MutablePeriod mp = new MutablePeriod(hoursOnly);
        int pos = formatter.getParser().parseInto(mp, "5h", 0, Locale.getDefault());
        assertEquals(2, pos);
        assertEquals(5, mp.getHours());
        assertEquals(0, mp.getDays());
    }

    @Test(timeout = 4000)
    public void testEmptyBuilderBehavior() {
        PeriodFormatter formatter = new PeriodFormatterBuilder().toFormatter();
        assertEquals("", formatter.print(new Period().withHours(5)));
        Period parsed = formatter.parsePeriod("");
        assertEquals(Period.ZERO, parsed);
    }
}