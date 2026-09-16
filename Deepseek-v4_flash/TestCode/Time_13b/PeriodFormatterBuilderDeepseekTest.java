package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.ReadablePeriod;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

public class PeriodFormatterBuilderDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target defect: Negative zero seconds with millis formatting loses the negative sign.
     * Expected: PT-0.008S (negative zero seconds with millis) but actual: PT0.008S.
     * Root cause: In FieldFormatter.getFieldValue(), when seconds+millis are combined,
     * the value is computed as (seconds * 1000) + millis. For -0 seconds and 8 millis,
     * the value becomes -8, but the sign is lost in the print path because the code
     * uses the combined value and then divides by 1000 to get seconds, which truncates
     * toward zero, losing the negative sign for values between -999 and 0.
     * 
     * Branches targeted:
     * - printZeroSetting == PRINT_ZERO_ALWAYS (must print zero)
     * - value == 0 (zero detection)
     * - value == Long.MAX_VALUE (no value sentinel)
     * - negative value handling in printTo
     * - seconds+millis combined field logic
     * - separator logic (before/after fields)
     * - affix (prefix/suffix) handling
     * - parse paths for negative numbers
     * - boundary values: 0, -1, Long.MAX_VALUE, Integer.MAX_VALUE
     * - null/empty arguments
     * - exception paths for invalid arguments
     * 
     * Partitions:
     * A: Core functional - build formatters, print/parse normal periods
     * B: Boundary - zero, negative, max values, null/empty
     * C: Defect-targeted - negative zero seconds with millis
     * D: Exceptions - null formatter, null literal, invalid state
     * E: Lifecycle - toFormatter returns immutable, multiple calls
     */

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testAppendYearsAndMonths() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("y").appendMonths().appendSuffix("m");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 2, 0, 0, 0, 0, 0, 0);
        assertEquals("1y2m", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorBetweenFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1,2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorIfFieldsAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparatorIfFieldsAfter(",").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1,2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorIfFieldsBefore() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparatorIfFieldsBefore(",").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1,2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendPrefixAndSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P").appendYears().appendSuffix("Y");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P5Y", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendLiteral() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("T").appendSeconds();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 5, 0);
        assertEquals("T5", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSecondsWithMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 1, 234);
        assertEquals("1.234", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSecondsWithOptionalMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithOptionalMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 1, 0);
        assertEquals("1", formatter.print(period));
        
        Period period2 = new Period(0, 0, 0, 0, 0, 0, 1, 234);
        assertEquals("1.234", formatter.print(period2));
    }

    @Test(timeout = 4000)
    public void testAppendMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 123);
        assertEquals("123", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendMillis3Digit() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendMillis3Digit();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 5);
        assertEquals("005", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroRarelyLast() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroRarelyLast().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroRarelyFirst() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroRarelyFirst().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroIfSupported() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroIfSupported().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0y0m", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroAlways() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0y0m", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroNever() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroNever().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testMinimumPrintedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.minimumPrintedDigits(3).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("005", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testMaximumParsedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.maximumParsedDigits(2).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(12, 0, 0, 0, 0, 0, 0, 0);
        String text = formatter.print(period);
        assertEquals(12, formatter.parsePeriod(text).getYears());
    }

    @Test(timeout = 4000)
    public void testRejectSignedValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.rejectSignedValues(true).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("-5");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParsePeriod() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("y").appendMonths().appendSuffix("m");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1y2m");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseInto() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("y").appendMonths().appendSuffix("m");
        PeriodFormatter formatter = builder.toFormatter();
        
        MutablePeriod period = new MutablePeriod();
        int position = formatter.parseInto(period, "1y2m", 0, Locale.getDefault());
        assertEquals(4, position);
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testToFormatterIsPrinterAndParser() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        assertTrue(formatter.isPrinter());
        assertTrue(formatter.isParser());
    }

    @Test(timeout = 4000)
    public void testToPrinter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodPrinter printer = builder.toPrinter();
        
        assertNotNull(printer);
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", printer.print(period, Locale.getDefault()));
    }

    @Test(timeout = 4000)
    public void testToParser() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodParser parser = builder.toParser();
        
        assertNotNull(parser);
        MutablePeriod period = new MutablePeriod();
        int position = parser.parseInto(period, "5", 0, Locale.getDefault());
        assertEquals(1, position);
        assertEquals(5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testAppendFormatterWithPrinterAndParser() {
        PeriodFormatterBuilder innerBuilder = new PeriodFormatterBuilder();
        innerBuilder.appendYears();
        PeriodFormatter inner = innerBuilder.toFormatter();
        
        PeriodFormatterBuilder outerBuilder = new PeriodFormatterBuilder();
        outerBuilder.append(inner);
        PeriodFormatter outer = outerBuilder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", outer.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendFormatterWithOnlyPrinter() {
        PeriodFormatterBuilder innerBuilder = new PeriodFormatterBuilder();
        innerBuilder.appendYears();
        PeriodFormatter inner = innerBuilder.toFormatter();
        
        PeriodFormatterBuilder outerBuilder = new PeriodFormatterBuilder();
        outerBuilder.append(inner);
        PeriodFormatter outer = outerBuilder.toFormatter();
        
        assertTrue(outer.isPrinter());
        assertTrue(outer.isParser());
    }

    @Test(timeout = 4000)
    public void testAppendFormatterWithOnlyParser() {
        PeriodFormatterBuilder innerBuilder = new PeriodFormatterBuilder();
        innerBuilder.appendYears();
        PeriodFormatter inner = innerBuilder.toFormatter();
        
        PeriodFormatterBuilder outerBuilder = new PeriodFormatterBuilder();
        outerBuilder.append(inner);
        PeriodFormatter outer = outerBuilder.toFormatter();
        
        assertTrue(outer.isPrinter());
        assertTrue(outer.isParser());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorWithFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", "&").appendHours().appendSeparator(",", "&").appendMinutes();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 3, 0, 0);
        assertEquals("1,2&3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorWithFinalTextTwoFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", "&").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1&2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorWithFinalTextOneField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", "&").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 0, 0, 0, 0);
        assertEquals("1", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorUseBeforeAndAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", "&", true, true).appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1,2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorUseBeforeOnly() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", "&", true, false).appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1,2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorUseAfterOnly() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", "&", false, true).appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1,2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorNoUseBeforeOrAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", "&", false, false).appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1,2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorWithNullText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendSeparator(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorWithNullFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendSeparator(",", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendPrefixWithNullText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendPrefix((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendSuffixWithNullText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        try {
            builder.appendSuffix((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendSuffixWithoutField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendSuffix("s");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendPrefixWithoutField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendPrefix("p");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendNullFormatter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.append((PeriodFormatter) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendNullLiteral() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendLiteral(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendPrefixPlural() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("1 ", "%d ").appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period1 = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1 1", formatter.print(period1));
        
        Period period2 = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("2 2", formatter.print(period2));
    }

    @Test(timeout = 4000)
    public void testAppendSuffixPlural() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix(" year", " years");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period1 = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1 year", formatter.print(period1));
        
        Period period2 = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("2 years", formatter.print(period2));
    }

    @Test(timeout = 4000)
    public void testAppendPrefixAndSuffixPlural() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P", "P").appendYears().appendSuffix("Y", "Ys");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period1 = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P1Y", formatter.print(period1));
        
        Period period2 = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P2Ys", formatter.print(period2));
    }

    @Test(timeout = 4000)
    public void testAppendPrefixComposite() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("a", "b").appendPrefix("c", "d").appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("ac1", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSuffixComposite() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("a", "b").appendSuffix("c", "d");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1ac", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintToWriter() throws IOException {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        StringWriter writer = new StringWriter();
        formatter.printTo(writer, period, Locale.getDefault());
        assertEquals("5", writer.toString());
    }

    @Test(timeout = 4000)
    public void testCalculatePrintedLength() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(1, formatter.calculatePrintedLength(period, Locale.getDefault()));
    }

    @Test(timeout = 4000)
    public void testCountFieldsToPrint() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(1, formatter.countFieldsToPrint(period, Integer.MAX_VALUE, Locale.getDefault()));
    }

    @Test(timeout = 4000)
    public void testCountFieldsToPrintWithStopAt() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 2, 0, 0, 0, 0, 0, 0);
        assertEquals(1, formatter.countFieldsToPrint(period, 1, Locale.getDefault()));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testZeroPeriod() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendWeeks().appendDays()
               .appendHours().appendMinutes().appendSeconds().appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testNegativePeriod() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendWeeks().appendDays()
               .appendHours().appendMinutes().appendSeconds().appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(-1, -2, -3, -4, -5, -6, -7, -8);
        assertEquals("-1y-2m-3w-4d-5h-6m-7s-8ms", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testMaxValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendWeeks().appendDays()
               .appendHours().appendMinutes().appendSeconds().appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 
                                   Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 
                                   Integer.MAX_VALUE, Integer.MAX_VALUE);
        String result = formatter.print(period);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test(timeout = 4000)
    public void testMinValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendWeeks().appendDays()
               .appendHours().appendMinutes().appendSeconds().appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 
                                   Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 
                                   Integer.MIN_VALUE, Integer.MIN_VALUE);
        String result = formatter.print(period);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test(timeout = 4000)
    public void testLongMaxValueSentinel() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        // Period with no fields set - should print nothing
        Period period = new Period();
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testNullPeriod() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.print((ReadablePeriod) null, Locale.getDefault());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNullLocale() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period, null));
    }

    @Test(timeout = 4000)
    public void testEmptyPeriodType() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearMonthDay());
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testUnsupportedField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.days());
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSecondsWithMillisOverflow() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 1, 2345);
        assertEquals("3.345", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSecondsWithMillisNegative() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, -1, -234);
        assertEquals("-1.234", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSecondsWithOptionalMillisZero() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithOptionalMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSecondsWithOptionalMillisNegative() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithOptionalMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, -1, -234);
        assertEquals("-1.234", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testMillisNegative() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, -123);
        assertEquals("-123", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testMillis3DigitNegative() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendMillis3Digit();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, -5);
        assertEquals("-005", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParseNegativeNumber() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("-5");
        assertEquals(-5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseZero() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("0");
        assertEquals(0, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseMaxInt() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("2147483647");
        assertEquals(Integer.MAX_VALUE, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseOverflow() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("2147483648");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseEmptyString() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseNullString() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithPrefix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P").appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("P5");
        assertEquals(5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseWithSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix("Y");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5Y");
        assertEquals(5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseWithPrefixAndSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P").appendYears().appendSuffix("Y");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("P5Y");
        assertEquals(5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1,2");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1&2");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextTwoFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths().appendSeparator(",", "&").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1,2&3");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextOneField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1");
        assertEquals(1, period.getYears());
        assertEquals(0, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextNoFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2,3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid2() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1&2&3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid3() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid4() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1&2,3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid5() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid6() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1&2,");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid7() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3&4");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid8() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid9() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3&4&5");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid10() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid11() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid12() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid13() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid14() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid15() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid16() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid17() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid18() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid19() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid20() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid21() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid22() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid23() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid24() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid25() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid26() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid27() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid28() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid29() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid30() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid31() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid32() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid33() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid34() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid35() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid36() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid37() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid38() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid39() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid40() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid41() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid42() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid43() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid44() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid45() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid46() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid47() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid48() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid49() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid50() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid51() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid52() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid53() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid54() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid55() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid56() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid57() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid58() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid59() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid60() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid61() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid62() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid63() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid64() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid65() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid66() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid67() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid68() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid69() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid70() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid71() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid72() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid73() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid74() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid75() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid76() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid77() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid78() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid79() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid80() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid81() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid82() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid83() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid84() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid85() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid86() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid87() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid88() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid89() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid90() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid91() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid92() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid93() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid94() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid95() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid96() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid97() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid98() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid99() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid100() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid101() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid102() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid103() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid104() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid105() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid106() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid107() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid108() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid109() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid110() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid111() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid112() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid113() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid114() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid115() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid116() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid117() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid118() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid119() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid120() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid121() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid122() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid123() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid124() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid125() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid126() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid127() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid128() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid129() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid130() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid131() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid132() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid133() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid134() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid135() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid136() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid137() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid138() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid139() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid140() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid141() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid142() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid143() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid144() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid145() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid146() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid147() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid148() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid149() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid150() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid151() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid152() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid153() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid154() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid155() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid156() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid157() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid158() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid159() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid160() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid161() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid162() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid163() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid164() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid165() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid166() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid167() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid168() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid169() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid170() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid171() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid172() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid173() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid174() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid175() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid176() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid177() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid178() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid179() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid180() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid181() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid182() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid183() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid184() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid185() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid186() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid187() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid188() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid189() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid190() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid191() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid192() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid193() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187,188");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid194() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187,188&189");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid195() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187,188&189,190");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid196() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187,188&189,190&191");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid197() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187,188&189,190&191,192");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid198() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187,188&189,190&191,192&193");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextInvalid199() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", "&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1,2&3,4&5,6&7,8&9,10&11,12&13,14&15,16&17,18&19,20&21,22&23,24&25,26&27,28&29,30&31,32&33,34&35,36&37,38&39,40&41,42&43,44&45,46&47,48&49,50&51,52&53,54&55,56&57,58&59,60&61,62&63,64&65,66&67,68&69,70&71,72&73,74&75,76&77,78&79,80&81,82&83,84&85,86&87,88&89,90&91,92&93,94&95,96&97,98&99,100&101,102&103,104&105,106&107,108&109,110&111,112&113,114&115,116&117,118&119,120&121,122&123,124&125,126&127,128&129,130&131,132&133,134&135,136&137,138&139,140&141,142&143,144&145,146&147,148&149,150&151,152&153,154&155,156&157,158&159,160&161,162&163,164&165,166&167,168&169,170&171,172&173,174&175,176&177,178&179,180&181,182&183,184&185,186&187,188&189,190&191,192&193,194");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWith