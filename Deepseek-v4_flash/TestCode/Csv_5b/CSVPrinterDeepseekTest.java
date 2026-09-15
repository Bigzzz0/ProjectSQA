package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Comprehensive JUnit 4 test suite for CSVPrinter with White-Box coverage goals:
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - print() with various value types (String, null, Number, empty string)
 *   - printRecord() with Iterable and Object... variants
 *   - printRecords() with Iterable, Object[], JDBC ResultSet
 *   - println() and newRecord state management
 *   - flush() and close() operations
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Null values with nullString configured
 *   - Empty collections/arrays
 *   - Strings containing special chars (delimiter, quote, CR, LF, escape)
 *   - Values starting/ending with space or special chars
 *   - Single value vs multiple values
 *   - Empty string values
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Targeting Defects4J defect: printAndQuote with quotePolicy=MINIMAL
 *     and empty first token on new line -> must quote empty token
 *   - Targeting CSVs null record separator (CSV-106): ensuring printComment
 *     and println handle null record separator properly; also edge cases
 *     where format.getRecordSeparator() may be null
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Constructor null checks (out, format)
 *   - Invalid format validation
 *   - printAndEscape called when escaping disabled (should not happen but 
 *     we make sure code paths are covered)
 *   - printAndQuote with quoting NONE -> printAndEscape
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - getOut() returns original Appendable
 *   - Close/Flush through delegation
 */
public class CSVPrinterDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testPrintSimpleString() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("hello");
        printer.println();
        printer.close();
        assertEquals("hello\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintNullValueWithoutNullString() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null); // default
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print((Object) null);
        printer.println();
        printer.close();
        assertEquals("\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintNullValueWithNullString() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print((Object) null);
        printer.println();
        printer.close();
        assertEquals("\\N\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintEmptyString() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("");
        printer.println();
        printer.close();
        assertEquals("\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintMultipleValues() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a");
        printer.print("b");
        printer.print((Object) null);
        printer.println();
        printer.close();
        assertEquals("a,b,NULL\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordIterable() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord(Arrays.asList("x", "y", "z"));
        printer.close();
        assertEquals("x,y,z\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordVarargs() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("1", "2", "3");
        printer.close();
        assertEquals("1,2,3\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsIterableOfObjects() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecords(Arrays.asList("single", "record"));
        printer.close();
        assertEquals("single\r\nrecord\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsIterableOfIterable() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecords(Arrays.asList(
            Arrays.asList("a1", "a2"),
            Arrays.asList("b1", "b2")
        ));
        printer.close();
        assertEquals("a1,a2\r\nb1,b2\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsArray() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecords(new Object[] {
            new Object[] {"x", "y"},
            "single"
        });
        printer.close();
        // Note: first is Object[] -> printRecord(Object[]), second is String -> printRecord(value)
        assertEquals("x,y\r\nsingle\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintlnNewRecordFlag() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.println(); // starts new line
        printer.print("a");
        printer.println();
        printer.println(); // empty line
        printer.print("b");
        printer.close();
        // After first println we have record separator, then new line with 'a',
        // then another record separator, then another (empty line), then print b (no trailing newline)
        assertEquals("\r\na\r\n\r\nb", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFlushAndClose() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("test");
        printer.flush();
        assertFalse(sw.toString().isEmpty());
        printer.close();
        // close flushes if Closeable
    }

    @Test(timeout = 4000)
    public void testGetOut() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        assertSame(sw, printer.getOut());
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testPrintValueWithDelimiter() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.NONE);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b");
        printer.println();
        printer.close();
        assertEquals("a\\,b\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintValueWithQuoteChar() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a\"b");
        printer.println();
        printer.close();
        assertEquals("\"a\"\"b\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintValueWithNewline() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a\nb");
        printer.println();
        printer.close();
        assertEquals("\"a\nb\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintValueWithCR() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a\rb");
        printer.println();
        printer.close();
        assertEquals("\"a\rb\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintEmptyRecordFirstOnNewLine() throws IOException {
        StringWriter sw = new StringWriter();
        // Defect sensitivity: With MINIMAL quoting, an empty first token on a new line should be quoted.
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.MINIMAL);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.println(); // start new record (newRecord = true)
        printer.print("");
        printer.println();
        printer.close();
        // The empty string is the first token on the line, must be quoted
        assertEquals("\r\n\"\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintNonEmptyFirstTokenStartsWithSpecialChar() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.MINIMAL);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("#startsWithCommentChar");
        printer.println();
        printer.close();
        // Starting with '#' (<= COMMENT char '#') should trigger quoting
        assertEquals("\"#startsWithCommentChar\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTokenEndsWithSpace() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.MINIMAL);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("trailingSpace ");
        printer.println();
        printer.close();
        // trailing space <= SP (0x20) should trigger quoting
        assertEquals("\"trailingSpace \"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTokenWithOnlySpaces() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.MINIMAL);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("   ");
        printer.println();
        printer.close();
        assertEquals("\"   \"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testNullStringBoundary() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("");
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print((Object) null);
        printer.println();
        printer.close();
        assertEquals("\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyIterablePrintRecords() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecords(Collections.emptyList());
        printer.close();
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyArrayPrintRecords() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecords(new Object[0]);
        printer.close();
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithNumbers() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.NON_NUMERIC);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print(123);
        printer.print(45.67);
        printer.println();
        printer.close();
        assertEquals("123,45.67\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithNonNumericQuoting() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.NON_NUMERIC);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("text");
        printer.println();
        printer.close();
        assertEquals("\"text\"\r\n", sw.toString());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testNullRecordSeparatorCsv106() throws IOException {
        // Defect: In CSVPrinter, if record separator is null, println() will
        // try to append null to Appendable; should produce empty line or
        // handle gracefully. The known defect from CSV-106 reveals that
        // using null record separator causes NPE or assertion failure.
        // We verify that the output is correct (empty record separator = nothing appended)
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(null);
        try {
            CSVPrinter printer = new CSVPrinter(sw, format);
            printer.print("a");
            printer.println();
            printer.close();
            // If null record separator is handled as empty string, output should be "a"
            assertEquals("a", sw.toString());
        } catch (NullPointerException e) {
            // Accept NPE as bug indicator, but test should fail if bug exists
            // On fixed version, we expect no exception; on buggy version, NPE causes test failure
            fail("Null record separator should not cause NullPointerException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testPrintCommentWithNullRecordSeparator() throws IOException {
        // Additional coverage for CSV-106: printComment should handle null record separator
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT
            .withCommentStart('#')
            .withRecordSeparator(null);
        try {
            CSVPrinter printer = new CSVPrinter(sw, format);
            printer.printComment("test comment");
            printer.close();
            // If null record separator is handled, comment should be written with newlines separated by empty string
            // Expected: "# test comment" (newRecord flag reset)
            assertTrue(sw.toString().contains("# test comment"));
        } catch (NullPointerException e) {
            fail("Null record separator should not cause NullPointerException in printComment: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testPrintEmptyTokenFirstOnNewLineWithNullRecordSeparator() throws IOException {
        // Combined defect sensitivity: empty token on new line with null record separator
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT
            .withQuoteMode(Quote.MINIMAL)
            .withRecordSeparator(null);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.println(); // sets newRecord = true, but appends null to out (may NPE)
        printer.print("");
        printer.println();
        printer.close();
        // On fixed version: first println does nothing (null sep), then empty token quoted, then nothing
        assertEquals("\"\"", sw.toString());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullOut() {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFormat() {
        new CSVPrinter(new StringWriter(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidFormat() {
        // Create format that will fail validation (e.g., both quote and escape null)
        CSVFormat invalid = CSVFormat.newFormat(',').withQuote(null).withEscape(null);
        new CSVPrinter(new StringWriter(), invalid);
    }

    @Test(timeout = 4000)
    public void testPrintWithEscapingOnly() throws IOException {
        // When quoting disabled but escaping enabled, printAndEscape is used
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null).withEscape('\\');
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b\nc");
        printer.println();
        printer.close();
        assertEquals("a\\,b\\nc\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuotingAll() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.ALL);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("simple");
        printer.println();
        printer.close();
        assertEquals("\"simple\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuotingNone() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.NONE).withEscape('\\');
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("a,b");
        printer.println();
        printer.close();
        assertEquals("a\\,b\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentDisabled() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart(null); // commenting disabled
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("should not appear");
        printer.close();
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentEnabled() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#').withRecordSeparator("\n");
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("value1");
        printer.printComment("my comment");
        printer.close();
        // First line: value1, then newline, then comment line
        assertTrue(sw.toString().contains("# my comment\n"));
    }

    @Test(timeout = 4000)
    public void testPrintCommentWithCRLFInComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#').withRecordSeparator("\r\n");
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("line1\r\nline2");
        printer.close();
        // Should split comment across lines
        String output = sw.toString();
        assertTrue(output.contains("# line1\r\n"));
        assertTrue(output.contains("# line2\r\n"));
    }

    @Test(timeout = 4000)
    public void testPrintRecordsWithNestedArrays() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecords(new Object[] {
            new String[] {"a", "b"},
            new Integer[] {1, 2}
        });
        printer.close();
        assertEquals("a,b\r\n1,2\r\n", sw.toString());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testCloseOnNonCloseableAppendable() throws IOException {
        // StringBuilder is not Closeable, should not fail
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.close(); // no-op, should not throw
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void testFlushOnNonFlushableAppendable() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.flush(); // no-op
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void testMultiplePrintRecords() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a", "b");
        printer.printRecord("c", "d");
        printer.close();
        assertEquals("a,b\r\nc,d\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsMixedTypes() throws IOException {
        StringWriter sw = new StringWriter();
        CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecords(Arrays.asList(
            Arrays.asList("1", "2"),
            "single",
            new Object[] {"x", "y"}
        ));
        printer.close();
        assertEquals("1,2\r\nsingle\r\nx,y\r\n", sw.toString());
    }

    // Additional boundary: value that starts with tab (char 9, which is < COMMENT '#')
    @Test(timeout = 4000)
    public void testPrintValueStartingWithTab() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.MINIMAL);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("\tstart");
        printer.println();
        printer.close();
        // Tab char (0x09) is < '#' (0x23), should trigger quoting
        assertEquals("\"\tstart\"\r\n", sw.toString());
    }

    // Edge: value ending with form feed (char 12, which is <= SP)
    @Test(timeout = 4000)
    public void testPrintValueEndingWithFormFeed() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(Quote.MINIMAL);
        CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("text\f");
        printer.println();
        printer.close();
        assertEquals("\"text\f\"\r\n", sw.toString());
    }
}