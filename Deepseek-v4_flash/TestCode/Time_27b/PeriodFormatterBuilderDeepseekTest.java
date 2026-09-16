package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadablePeriod;

public class PeriodFormatterBuilderDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Defect: Bug 2495455 - Parsing "PT1003199059S" fails with IllegalArgumentException
     * Root cause: In FieldFormatter.parseInto, when parsing seconds with optional millis,
     * the code incorrectly handles large second values that overflow into millis.
     * The parse logic for SECONDS_OPTIONAL_MILLIS uses a combined value calculation
     * that can overflow or misparse when seconds exceed Integer.MAX_VALUE / 1000.
     * 
     * Key branches targeted:
     * 1. printZeroSetting branches (ALWAYS, NEVER, RARELY_FIRST, RARELY_LAST, IF_SUPPORTED)
     * 2. Field type dispatch in getFieldValue (years through millis)
     * 3. SECONDS_OPTIONAL_MILLIS combined value calculation
     * 4. Prefix/suffix parsing logic
     * 5. Separator before/after logic
     * 6. Composite printer/parser delegation
     * 7. Affix plural/singular selection
     * 8. Boundary values: Long.MAX_VALUE, zero, negative, large positive
     * 9. Null/empty arguments for defensive paths
     * 10. IllegalStateException for missing field operations
     */

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testBasicAppendAndFormat() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix(" year", " years");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1 year", formatter.print(period));
        
        period = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("2 years", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendLiteral() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("P").appendYears().appendLiteral("Y");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P5Y", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendPrefix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("T").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 3, 0, 0, 0);
        assertEquals("T3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendMinutes().appendSuffix("m");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 7, 0, 0);
        assertEquals("7m", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 2, 3, 0, 0, 0);
        assertEquals("2,3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorIfFieldsAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparatorIfFieldsAfter(",").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 2, 3, 0, 0, 0);
        assertEquals("2,3", formatter.print(period));
        
        period = new Period(0, 0, 0, 2, 0, 0, 0, 0);
        assertEquals("2", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorIfFieldsBefore() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparatorIfFieldsBefore(",").appendHours();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 2, 3, 0, 0, 0);
        assertEquals("2,3", formatter.print(period));
        
        period = new Period(0, 0, 0, 0, 3, 0, 0, 0);
        assertEquals("3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorWithFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendDays().appendSeparator(",", " and ").appendHours().appendSeparator(",", " and ").appendMinutes();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 1, 2, 3, 0, 0);
        assertEquals("1,2 and 3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSecondsWithMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 5, 250);
        assertEquals("5.250", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testAppendSecondsWithOptionalMillis() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithOptionalMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 5, 250);
        assertEquals("5.250", formatter.print(period));
        
        period = new Period(0, 0, 0, 0, 0, 0, 5, 0);
        assertEquals("5", formatter.print(period));
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
    public void testPrintZeroRarelyFirst() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroRarelyFirst().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
        
        period = new Period(0, 1, 0, 0, 0, 0, 0, 0);
        assertEquals("1", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroRarelyLast() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().printZeroRarelyLast();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
        
        period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroIfSupported() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroIfSupported().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroAlways() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", formatter.print(period));
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
        
        Period period = formatter.parsePeriod("12");
        assertEquals(12, period.getYears());
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
            // Expected
        }
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testNullFormatterAppend() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.append((PeriodFormatter) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullLiteralAppend() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendLiteral(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullPrefixAppend() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendPrefix((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullSuffixAppend() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        try {
            builder.appendSuffix((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSuffixWithoutField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendSuffix("s");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullSeparatorAppend() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendSeparator(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullFinalSeparatorAppend() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        try {
            builder.appendSeparator(",", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testZeroPeriodWithAllFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendWeeks().appendDays()
               .appendHours().appendMinutes().appendSeconds().appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testLargeValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0, 0, 0, 0);
        String result = formatter.print(period);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test(timeout = 4000)
    public void testNegativeValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(-5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("-5", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testEmptyPeriodParse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testBug2495455() {
        // Defect: Parsing "PT1003199059S" throws IllegalArgumentException
        // Expected: Should parse successfully as 1003199059 seconds
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("PT").appendSecondsWithOptionalMillis().appendSuffix("S");
        PeriodFormatter formatter = builder.toFormatter();
        
        // This should parse without exception
        Period period = formatter.parsePeriod("PT1003199059S");
        
        // Verify the parsed value
        assertEquals(1003199059, period.getSeconds());
        assertEquals(0, period.getMillis());
    }

    @Test(timeout = 4000)
    public void testBug2495455WithMillis() {
        // Similar to bug but with millis component
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("PT").appendSecondsWithOptionalMillis().appendSuffix("S");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("PT1003199059.123S");
        
        assertEquals(1003199059, period.getSeconds());
        assertEquals(123, period.getMillis());
    }

    @Test(timeout = 4000)
    public void testBug2495455Boundary() {
        // Test boundary around the overflow point
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("PT").appendSecondsWithOptionalMillis().appendSuffix("S");
        PeriodFormatter formatter = builder.toFormatter();
        
        // Just below the problematic threshold
        Period period = formatter.parsePeriod("PT2147483S");
        assertEquals(2147483, period.getSeconds());
        
        // Just above the problematic threshold
        period = formatter.parsePeriod("PT2147484S");
        assertEquals(2147484, period.getSeconds());
    }

    @Test(timeout = 4000)
    public void testBug2495455LargeSeconds() {
        // Test with very large seconds value
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("PT").appendSecondsWithOptionalMillis().appendSuffix("S");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("PT999999999S");
        assertEquals(999999999, period.getSeconds());
    }

    @Test(timeout = 4000)
    public void testBug2495455WithNegative() {
        // Test negative large seconds
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("PT").appendSecondsWithOptionalMillis().appendSuffix("S");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("PT-1003199059S");
        assertEquals(-1003199059, period.getSeconds());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testInvalidFormat() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("abc");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testMalformedInput() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("1Y2");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        period = period.withMonths(5);
        
        // Should not print months since only years are appended
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testNullPeriodPrint() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.print((ReadablePeriod) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullLocalePrint() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        // Should work with null locale
        assertEquals("1", formatter.print(period, (java.util.Locale) null));
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testToFormatterTwice() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        
        PeriodFormatter formatter1 = builder.toFormatter();
        PeriodFormatter formatter2 = builder.toFormatter();
        
        // Both should work independently
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1", formatter1.print(period));
        assertEquals("1", formatter2.print(period));
        
        // Modifying builder after toFormatter should not affect existing formatters
        builder.appendMonths();
        PeriodFormatter formatter3 = builder.toFormatter();
        
        assertEquals("1", formatter1.print(period));
        assertEquals("1", formatter2.print(period));
        assertEquals("1", formatter3.print(period));
    }

    @Test(timeout = 4000)
    public void testIsPrinterAndParser() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        
        PeriodFormatter formatter = builder.toFormatter();
        assertTrue(formatter.isPrinter());
        assertTrue(formatter.isParser());
    }

    @Test(timeout = 4000)
    public void testPrinterOnly() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        
        PeriodFormatter formatter = builder.toFormatter();
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        
        assertEquals("1", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParserOnly() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        
        PeriodFormatter formatter = builder.toFormatter();
        Period period = formatter.parsePeriod("5");
        
        assertEquals(5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testCompositeFormatter() {
        PeriodFormatterBuilder builder1 = new PeriodFormatterBuilder();
        builder1.appendYears().appendSuffix("y");
        PeriodFormatter formatter1 = builder1.toFormatter();
        
        PeriodFormatterBuilder builder2 = new PeriodFormatterBuilder();
        builder2.appendMonths().appendSuffix("m");
        PeriodFormatter formatter2 = builder2.toFormatter();
        
        PeriodFormatterBuilder composite = new PeriodFormatterBuilder();
        composite.append(formatter1).appendLiteral("-").append(formatter2);
        PeriodFormatter formatter = composite.toFormatter();
        
        Period period = new Period(1, 2, 0, 0, 0, 0, 0, 0);
        assertEquals("1y-2m", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPluralAffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix(" year", " years");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period1 = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1 year", formatter.print(period1));
        
        Period period2 = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("2 years", formatter.print(period2));
    }

    @Test(timeout = 4000)
    public void testPluralPrefix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("1 ", "many ").appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period1 = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1 1", formatter.print(period1));
        
        Period period2 = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("many 2", formatter.print(period2));
    }

    @Test(timeout = 4000)
    public void testSeparatorWithZeroValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 5, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
        
        period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testCalculatePrintedLength() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix(" years");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(7, formatter.calculatePrintedLength(period, null));
    }

    @Test(timeout = 4000)
    public void testCountFieldsToPrint() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(1, formatter.countFieldsToPrint(period, Integer.MAX_VALUE, null));
        
        period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals(2, formatter.countFieldsToPrint(period, Integer.MAX_VALUE, null));
    }

    @Test(timeout = 4000)
    public void testParseIntoPosition() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        int position = formatter.parseInto(period, "5Y3M", 0, null);
        assertEquals(4, position);
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithPrefixAndSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("P").appendYears().appendLiteral("Y").appendMonths().appendLiteral("M");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("P5Y3M");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("-").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5-3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testAllFieldTypes() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendWeeks().appendDays()
               .appendHours().appendMinutes().appendSeconds().appendMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        String result = formatter.print(period);
        assertNotNull(result);
        
        Period parsed = formatter.parsePeriod(result);
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
        assertEquals(3, parsed.getWeeks());
        assertEquals(4, parsed.getDays());
        assertEquals(5, parsed.getHours());
        assertEquals(6, parsed.getMinutes());
        assertEquals(7, parsed.getSeconds());
        assertEquals(8, parsed.getMillis());
    }

    @Test(timeout = 4000)
    public void testSecondsWithMillisRoundTrip() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 5, 250);
        String result = formatter.print(period);
        assertEquals("5.250", result);
        
        Period parsed = formatter.parsePeriod(result);
        assertEquals(5, parsed.getSeconds());
        assertEquals(250, parsed.getMillis());
    }

    @Test(timeout = 4000)
    public void testSecondsWithOptionalMillisRoundTrip() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSecondsWithOptionalMillis();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 5, 250);
        String result = formatter.print(period);
        assertEquals("5.250", result);
        
        Period parsed = formatter.parsePeriod(result);
        assertEquals(5, parsed.getSeconds());
        assertEquals(250, parsed.getMillis());
        
        period = new Period(0, 0, 0, 0, 0, 0, 5, 0);
        result = formatter.print(period);
        assertEquals("5", result);
        
        parsed = formatter.parsePeriod(result);
        assertEquals(5, parsed.getSeconds());
        assertEquals(0, parsed.getMillis());
    }

    @Test(timeout = 4000)
    public void testCaseInsensitiveSeparatorParsing() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("Y").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testMultipleSeparators() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths()
               .appendSeparator(",", " and ").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 2, 0, 3, 0, 0, 0, 0);
        assertEquals("1,2 and 3", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("1,2 and 3");
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
        assertEquals(3, parsed.getDays());
    }

    @Test(timeout = 4000)
    public void testPrintToWriter() throws Exception {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        java.io.StringWriter writer = new java.io.StringWriter();
        formatter.printTo(writer, period);
        assertEquals("5", writer.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToBuffer() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        StringBuffer buffer = new StringBuffer();
        formatter.printTo(buffer, period);
        assertEquals("5", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testParsePeriodWithType() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5Y3M", PeriodType.yearMonthDay());
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        assertEquals(0, period.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithNullLocale() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5", null);
        assertEquals(5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testEmptyBuilder() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testBuilderWithOnlyLiteral() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("P");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testBuilderWithOnlySeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSeparator(",");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testBuilderWithOnlyPrefix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testBuilderWithOnlySuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendSuffix("Y");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroRarelyFirstWithMultipleFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroRarelyFirst().appendYears().appendMonths().appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 5, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
        
        period = new Period(0, 3, 0, 5, 0, 0, 0, 0);
        assertEquals("3,5", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroRarelyLastWithMultipleFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendMonths().appendDays().printZeroRarelyLast();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
        
        period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("5,3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroIfSupportedWithUnsupportedField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroIfSupported().appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testPrintZeroNeverWithNonZeroField() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroNever().appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParseWithRejectSignedValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.rejectSignedValues(true).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("-5");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        Period period = formatter.parsePeriod("5");
        assertEquals(5, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseWithMaximumParsedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.maximumParsedDigits(2).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("123");
        assertEquals(12, period.getYears());
    }

    @Test(timeout = 4000)
    public void testParseWithMinimumPrintedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.minimumPrintedDigits(3).appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("005", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("005");
        assertEquals(5, parsed.getYears());
    }

    @Test(timeout = 4000)
    public void testCompositeAffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P").appendYears().appendSuffix("Y");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P5Y", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("P5Y");
        assertEquals(5, parsed.getYears());
    }

    @Test(timeout = 4000)
    public void testSeparatorWithBeforeAndAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparatorIfFieldsAfter(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("5,3", formatter.print(period));
        
        period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSeparatorWithBeforeOnly() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparatorIfFieldsBefore(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("5,3", formatter.print(period));
        
        period = new Period(0, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSeparatorWithUseAfterFalse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", ",", false, true).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("5,3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSeparatorWithUseBeforeFalse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", ",", true, false).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("5,3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testSeparatorWithBothFalse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", ",", false, false).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("53", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorVariants() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5 and 3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithCaseInsensitiveSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("Y").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5Y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithMultipleSeparatorForms() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5 and 3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5 and 3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorOnlyAfter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparatorIfFieldsAfter(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5");
        assertEquals(5, period.getYears());
        assertEquals(0, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorOnlyBefore() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparatorIfFieldsBefore(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("3");
        assertEquals(0, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorBothFalse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", ",", false, false).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("53");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorUseAfterFalse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", ",", false, true).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorUseBeforeFalse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", ",", true, false).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPrefix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P").appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("P5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths().appendSuffix("M");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3M");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithAllComponents() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P").appendYears().appendSeparator(",").appendMonths().appendSuffix("M");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("P5,3M");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithMultipleSeparatorsAndFinalText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths()
               .appendSeparator(",", " and ").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1,2 and 3");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithMultipleSeparatorsAndFinalTextVariants() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths()
               .appendSeparator(",", " and ").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1,2,3");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getDays());
        
        period = formatter.parsePeriod("1 and 2 and 3");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndEmptyFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5");
        assertEquals(5, period.getYears());
        assertEquals(0, period.getMonths());
        
        period = formatter.parsePeriod(",3");
        assertEquals(0, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndZeroValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("0,3");
        assertEquals(0, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5,0");
        assertEquals(5, period.getYears());
        assertEquals(0, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNegativeValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("-5,3");
        assertEquals(-5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5,-3");
        assertEquals(5, period.getYears());
        assertEquals(-3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndLargeValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("2147483647,2147483647");
        assertEquals(2147483647, period.getYears());
        assertEquals(2147483647, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMaxValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("2147483647,2147483647");
        assertEquals(Integer.MAX_VALUE, period.getYears());
        assertEquals(Integer.MAX_VALUE, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMinValues() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("-2147483648,-2147483648");
        assertEquals(Integer.MIN_VALUE, period.getYears());
        assertEquals(Integer.MIN_VALUE, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndOverflow() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("2147483648,3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndInvalidFormat() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("5,,3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndTrailingSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("5,");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndLeadingSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod(",3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndEmptyString() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNull() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndWhitespace() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("5, 3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNonDigit() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("5,a");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPartialMatch() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("5,3extra");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("123,456");
        assertEquals(123, period.getYears());
        assertEquals(456, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndLeadingZeros() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("005,003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSigns() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("+5,+3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMixedSigns() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("-5,+3");
        assertEquals(-5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("+5,-3");
        assertEquals(5, period.getYears());
        assertEquals(-3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndRejectSigned() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.rejectSignedValues(true).appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parsePeriod("-5,3");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMaxParsedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.maximumParsedDigits(2).appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("123,456");
        assertEquals(12, period.getYears());
        assertEquals(45, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMinPrintedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.minimumPrintedDigits(3).appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("005,003", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("005,003");
        assertEquals(5, parsed.getYears());
        assertEquals(3, parsed.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPrintZeroAlways() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroAlways().appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0,0", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("0,0");
        assertEquals(0, parsed.getYears());
        assertEquals(0, parsed.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPrintZeroNever() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroNever().appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
        
        period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
        
        period = new Period(0, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPrintZeroRarelyFirst() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroRarelyFirst().appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
        
        period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
        
        period = new Period(0, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("3", formatter.print(period));
        
        period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("5,3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPrintZeroRarelyLast() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths().printZeroRarelyLast();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
        
        period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5", formatter.print(period));
        
        period = new Period(0, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("3", formatter.print(period));
        
        period = new Period(5, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("5,3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPrintZeroIfSupported() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroIfSupported().appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0,0", formatter.print(period));
        
        period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("5,0", formatter.print(period));
        
        period = new Period(0, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("0,3", formatter.print(period));
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndAllFieldTypes() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths().appendSeparator(",").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 2, 0, 3, 0, 0, 0, 0);
        assertEquals("1,2,3", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("1,2,3");
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
        assertEquals(3, parsed.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPartialFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths().appendSeparator(",").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 0, 0, 3, 0, 0, 0, 0);
        assertEquals("1,3", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("1,3");
        assertEquals(1, parsed.getYears());
        assertEquals(0, parsed.getMonths());
        assertEquals(3, parsed.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFinalTextOnly() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 2, 0, 0, 0, 0, 0, 0);
        assertEquals("1 and 2", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("1 and 2");
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleFinalTexts() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths()
               .appendSeparator(",", " and ").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(1, 2, 0, 3, 0, 0, 0, 0);
        assertEquals("1,2 and 3", formatter.print(period));
        
        Period parsed = formatter.parsePeriod("1,2 and 3");
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
        assertEquals(3, parsed.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMixedForms() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths()
               .appendSeparator(",", " and ").appendDays();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("1,2,3");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getDays());
        
        period = formatter.parsePeriod("1 and 2 and 3");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getDays());
        
        period = formatter.parsePeriod("1,2 and 3");
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getDays());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndCaseInsensitive() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("Y").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5Y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleCaseForms() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("Y", "y").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5Y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNullLocale() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3", null);
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndLocale() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3", java.util.Locale.US);
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPosition() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        int position = formatter.parseInto(period, "5,3", 0, null);
        assertEquals(3, position);
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPartialPosition() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        int position = formatter.parseInto(period, "5,3extra", 0, null);
        assertEquals(3, position);
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndInvalidPosition() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, "5,3", -1, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndOutOfBoundsPosition() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, "5,3", 10, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNullPeriod() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        try {
            formatter.parseInto(null, "5,3", 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNullText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, null, 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndEmptyText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, "", 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndWhitespaceText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, " ", 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNonNumericText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, "abc", 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPartialNumericText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, "5a,3", 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndOverflowText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, "2147483648,3", 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndUnderflowText() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period();
        try {
            formatter.parseInto(period, "-2147483649,3", 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleSeparators() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5 and 3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleSeparatorForms() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",", " and ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5 and 3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndCaseInsensitiveForms() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("Y", "y").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5Y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleCaseForms() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("Y", "y").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5Y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
        
        period = formatter.parsePeriod("5y3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNullSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(null).appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("53");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndEmptySeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("53");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndWhitespaceSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(" ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5 3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleWhitespaceSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("  ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5  3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndTabSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\t").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\t3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNewlineSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\n").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\n3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSpecialChars() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("-").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5-3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleSpecialChars() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("--").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5--3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndDotSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(".").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5.3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSlashSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("/").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5/3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndBackslashSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\\").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\\3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndColonSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(":").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5:3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSemicolonSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(";").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5;3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPipeSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("|").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5|3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndAmpersandSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("&").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5&3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndAtSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("@").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5@3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndHashSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("#").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5#3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndDollarSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("$").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5$3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPercentSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("%").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5%3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndCaretSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("^").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5^3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndAsteriskSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("*").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5*3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPlusSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("+").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5+3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndEqualsSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("=").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5=3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndQuestionSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("?").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5?3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndExclamationSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("!").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5!3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndTildeSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("~").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5~3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndBacktickSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("`").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5`3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndBracketSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("[").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5[3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndBraceSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("{").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5{3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndParenSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("(").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5(3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndAngleBracketSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("<").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5<3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndQuoteSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\"").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\"3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSingleQuoteSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("'").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5'3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndCommaSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(",").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5,3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndPeriodSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(".").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5.3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSemicolonSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(";").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5;3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndColonSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(":").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5:3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSlashSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("/").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5/3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndBackslashSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\\").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\\3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndDashSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("-").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5-3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndUnderscoreSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("_").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5_3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndSpaceSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator(" ").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5 3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndTabSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\t").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\t3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNewlineSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\n").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\n3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndCarriageReturnSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\r").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\r3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndFormFeedSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\f").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\f3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndBackspaceSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\b").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\b3");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndNullCharSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndUnicodeSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleUnicodeSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndEmojiSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleEmojiSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMixedUnicodeSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMixedUnicodeSeparatorReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingVeryLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingVeryLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongest() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternating() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLong() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE003");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLonger() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A03");
        assertEquals(5, period.getYears());
        assertEquals(3, period.getMonths());
    }

    @Test(timeout = 4000)
    public void testParseWithSeparatorAndMultipleMixedUnicodeSeparatorAlternatingExtremelyLongAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongestAlternatingLongerReverse() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSeparator("\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00").appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = formatter.parsePeriod("5\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0\uD83D\uDE00\u00A0