package org.apache.commons.csv;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Targeted Defect (CSV-106):
 *    - CSVPrinter.println() appends format.getRecordSeparator() directly to Appendable.
 *      When recordSeparator is null (e.g. format.withRecordSeparator((String) null)),
 *      Appendable.append((CharSequence) null) outputs literal "null" instead of omitting separator.
 *    - Targeted by: testNullRecordSeparatorCsv106(), testPrintlnWithNullRecordSeparator()
 *
 * 2. Constructor & Lifecycle Branches:
 *    - out == null -> IllegalArgumentException
 *    - format == null -> IllegalArgumentException
 *    - getOut() returns exact Appendable instance
 *    - close(): out instanceof Closeable (true/false)
 *    - flush(): out instanceof Flushable (true/false)
 *
 * 3. print(Object) & Null Handling Branches:
 *    - value == null && format.getNullString() == null -> EMPTY string ("")
 *    - value == null && format.getNullString() != null -> nullString used
 *    - value != null -> value.toString()
 *
 * 4. Quoting Modes & Character Classification Branches:
 *    - Quote.ALL: all values encapsulated in quotes
 *    - Quote.NON_NUMERIC: Number instances unquoted, non-Number quoted
 *    - Quote.NONE: delegates to printAndEscape()
 *    - Quote.MINIMAL (default / null policy):
 *      * len <= 0 && newRecord == true  -> quoted ("")
 *      * len <= 0 && newRecord == false -> not quoted
 *      * len > 0 && newRecord && non-alphanumeric start -> quoted
 *      * len > 0 && !newRecord && c <= COMMENT ('#') -> quoted
 *      * inner char: LF, CR, quoteChar, delimChar -> quoted
 *      * inner quoteChar doubling -> pos, start tracking, escaped quotes
 *      * trailing char <= SP (' ') -> quoted
 *      * clean string -> unquoted fast path
 *    - isQuoting() == false && isEscaping() == false -> raw append path
 *
 * 5. Escaping Modes & Control Character Branches:
 *    - Escape handling for CR ('\r' -> escape + 'r')
 *    - Escape handling for LF ('\n' -> escape + 'n')
 *    - Escape handling for delimiter
 *    - Escape handling for escape character
 *    - Segment offset tracking: pos > start before control char and trailing pos > start
 *
 * 6. Commenting Branches (printComment):
 *    - !isCommentingEnabled() -> immediate no-op return
 *    - isCommentingEnabled() && !newRecord -> calls println() first
 *    - Line break variants in comment: CRLF ("\r\n"), standalone LF ("\n"), standalone CR ("\r")
 *    - Single-line comments without line breaks
 *
 * 7. Records Iteration & Polymorphism:
 *    - printRecord(Object...) & printRecord(Iterable)
 *    - printRecords(Iterable) and printRecords(Object[]):
 *      * element is Object[] -> delegates to printRecord(Object[])
 *      * element is Iterable -> delegates to printRecord(Iterable)
 *      * element is raw Object -> delegates to printRecord(Object...)
 *    - printRecords(ResultSet): multi-row, multi-column, dynamic proxy
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CSVPrinterGeminiTest {

    // =========================================================================
    // Helper Classes & Reflection Utilities
    // =========================================================================

    private static class CloseableFlushableAppendable implements Appendable, Closeable, Flushable {
        private final StringBuilder sb = new StringBuilder();
        boolean closed = false;
        boolean flushed = false;

        @Override
        public Appendable append(CharSequence csq) {
            sb.append(csq);
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            sb.append(csq, start, end);
            return this;
        }

        @Override
        public Appendable append(char c) {
            sb.append(c);
            return this;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public void flush() {
            flushed = true;
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private static CSVFormat withCommentMarkerCompat(final CSVFormat format, final char commentMarker) {
        try {
            Method m = CSVFormat.class.getMethod("withCommentStart", Character.class);
            return (CSVFormat) m.invoke(format, Character.valueOf(commentMarker));
        } catch (Exception e1) {
            try {
                Method m = CSVFormat.class.getMethod("withCommentStart", char.class);
                return (CSVFormat) m.invoke(format, commentMarker);
            } catch (Exception e2) {
                try {
                    Method m = CSVFormat.class.getMethod("withCommentMarker", Character.class);
                    return (CSVFormat) m.invoke(format, Character.valueOf(commentMarker));
                } catch (Exception e3) {
                    try {
                        Method m = CSVFormat.class.getMethod("withCommentMarker", char.class);
                        return (CSVFormat) m.invoke(format, commentMarker);
                    } catch (Exception e4) {
                        throw new RuntimeException("Could not configure comment marker", e4);
                    }
                }
            }
        }
    }

    private ResultSet createMockResultSet(final String[][] data) {
        final ResultSetMetaData meta = (ResultSetMetaData) Proxy.newProxyInstance(
                CSVPrinterGeminiTest.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("getColumnCount".equals(method.getName())) {
                            return data.length > 0 ? data[0].length : 0;
                        }
                        return null;
                    }
                }
        );

        return (ResultSet) Proxy.newProxyInstance(
                CSVPrinterGeminiTest.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                new InvocationHandler() {
                    private int currentRow = -1;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("getMetaData".equals(name)) {
                            return meta;
                        } else if ("next".equals(name)) {
                            currentRow++;
                            return currentRow < data.length;
                        } else if ("getString".equals(name)) {
                            int colIndex = ((Integer) args[0]).intValue() - 1;
                            return data[currentRow][colIndex];
                        }
                        return null;
                    }
                }
        );
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicRecordPrinting() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.printRecord("alpha", "beta", "gamma");
        assertEquals("alpha,beta,gamma\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSequentialPrintCallsProduceDelimiters() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.print("item1");
        printer.print("item2");
        printer.print("item3");
        printer.println();

        assertEquals("item1,item2,item3\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetOutReturnsExactAppendable() {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        assertSame(sb, printer.getOut());
    }

    @Test(timeout = 4000)
    public void testCloseDelegatesWhenCloseable() throws IOException {
        final CloseableFlushableAppendable out = new CloseableFlushableAppendable();
        final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);

        assertFalse(out.closed);
        printer.close();
        assertTrue(out.closed);
    }

    @Test(timeout = 4000)
    public void testCloseDoesNotThrowWhenNotCloseable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.close(); // No-op, should not throw
    }

    @Test(timeout = 4000)
    public void testFlushDelegatesWhenFlushable() throws IOException {
        final CloseableFlushableAppendable out = new CloseableFlushableAppendable();
        final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);

        assertFalse(out.flushed);
        printer.flush();
        assertTrue(out.flushed);
    }

    @Test(timeout = 4000)
    public void testFlushDoesNotThrowWhenNotFlushable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.flush(); // No-op, should not throw
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintNullValueWithDefaultFormat() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.print(null);
        printer.println();
        // Null with default format has null nullString, which defaults to empty string.
        // First token empty on newRecord gets quoted as ""
        assertEquals("\"\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintNullValueWithConfiguredNullString() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL_VAL");
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print(null);
        printer.print("data");
        printer.println();
        assertEquals("NULL_VAL,data\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyRecordPrinting() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.printRecord();
        assertEquals("\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteEmptyTokensBoundary() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        // First token empty is on newRecord -> quoted
        printer.print("");
        // Second token empty is NOT on newRecord -> unquoted
        printer.print("");
        printer.print("val");
        printer.println();

        assertEquals("\"\",,val\r\n", sw.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CSV-106)
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullRecordSeparatorCsv106() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("a", "b");
        // Defect: CSVPrinter appends format.getRecordSeparator() without checking for null.
        // On the defective version, sw.toString() becomes "a,bnull" instead of "a,b".
        assertEquals("a,b", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintlnWithNullRecordSeparator() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("val");
        printer.println();
        assertEquals("val", sw.toString());
    }

    // =========================================================================
    // Partition D: Quote Policy & Classification Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testQuotePolicyAll() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("text", Integer.valueOf(100));
        assertEquals("\"text\",\"100\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuotePolicyNonNumeric() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("text", Integer.valueOf(100), Double.valueOf(3.14), Boolean.TRUE);
        assertEquals("\"text\",100,3.14,\"true\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuotePolicyNoneDelegatesToEscape() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('\\');
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("a,b", "c\\d", "e\rf", "g\nh");
        assertEquals("a\\,b,c\\\\d,e\\rf,g\\nh\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteConditionBranches() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        // Branch 1: newRecord && starts with non-alphanumeric (e.g. '_' or '-')
        printer.print("_test");
        // Branch 2: !newRecord && starts with c <= COMMENT ('!' is ASCII 33, '#' is 35)
        printer.print("!notComment");
        // Branch 3: !newRecord && starts with c > COMMENT ('$' is ASCII 36) -> unquoted
        printer.print("$normal");
        // Branch 4: ends with space (c <= SP)
        printer.print("endSpace ");
        // Branch 5: ends with tab (c <= SP)
        printer.print("endTab\t");
        printer.println();

        assertEquals("\"_test\",\"!notComment\",$normal,\"endSpace \",\"endTab\t\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteInternalDelimQuoteAndLineBreaks() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        // Starts with alphanumeric so it passes the first check, but contains delim, quotes, LF, CR
        printer.print("alpha,beta");
        printer.print("quote\"inside");
        printer.print("line\nfeed");
        printer.print("carriage\rreturn");
        printer.println();

        assertEquals("\"alpha,beta\",\"quote\"\"inside\",\"line\nfeed\",\"carriage\rreturn\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testNoQuotingAndNoEscapingPath() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuote((Character) null).withEscape((Character) null);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("hello,world");
        printer.print("unquoted");
        printer.println();

        assertEquals("hello,world,unquoted\r\n", sw.toString());
    }

    // =========================================================================
    // Partition E: Escaping Logic Branches (printAndEscape)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintAndEscapeAllSpecialChars() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuote((Character) null).withEscape('\\');
        final CSVPrinter printer = new CSVPrinter(sw, format);

        // Tests CR at start, LF in middle, delim in middle, escape at end
        printer.print("\rstart");
        printer.print("mid\nline");
        printer.print("delim,here");
        printer.print("endsEscape\\");
        printer.println();

        assertEquals("\\rstart,mid\\nline,delim\\,here,endsEscape\\\\\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintAndEscapeConsecutiveSpecialChars() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuote((Character) null).withEscape('\\');
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print(",,");
        printer.println();

        assertEquals("\\,\\,\r\n", sw.toString());
    }

    // =========================================================================
    // Partition F: Comment Printing Branches (printComment)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintCommentWhenDisabledDoesNothing() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.printComment("This should not be printed");
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentOnNewRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = withCommentMarkerCompat(CSVFormat.DEFAULT, '#');
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printComment("Single line comment");
        assertEquals("# Single line comment\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentWhenNotNewRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = withCommentMarkerCompat(CSVFormat.DEFAULT, '#');
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("data");
        printer.printComment("Comment after data");

        assertEquals("data\r\n# Comment after data\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentWithVariousLineBreaks() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = withCommentMarkerCompat(CSVFormat.DEFAULT, '#');
        final CSVPrinter printer = new CSVPrinter(sw, format);

        // Contains CRLF ("\r\n"), standalone LF ("\n"), and standalone CR ("\r")
        printer.printComment("Line1\r\nLine2\nLine3\rLine4");

        assertEquals("# Line1\r\n# Line2\r\n# Line3\r\n# Line4\r\n", sw.toString());
    }

    // =========================================================================
    // Partition G: Batch Records & Polymorphism (printRecords)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintRecordIterable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final List<String> list = Arrays.asList("one", "two", "three");
        printer.printRecord(list);

        assertEquals("one,two,three\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsIterableWithHeterogeneousElements() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final List<Object> records = Arrays.asList(
                new Object[]{"r1c1", "r1c2"},
                Arrays.asList("r2c1", "r2c2"),
                "singleVal"
        );
        printer.printRecords(records);

        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\nsingleVal\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsArrayWithHeterogeneousElements() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final Object[] records = new Object[]{
                new Object[]{"r1c1", "r1c2"},
                Arrays.asList("r2c1", "r2c2"),
                "singleVal"
        };
        printer.printRecords(records);

        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\nsingleVal\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsFromResultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final String[][] data = new String[][]{
                {"Alice", "30"},
                {"Bob", "25"}
        };
        final ResultSet rs = createMockResultSet(data);

        printer.printRecords(rs);
        assertEquals("Alice,30\r\nBob,25\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsFromEmptyResultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final String[][] emptyData = new String[][]{};
        final ResultSet rs = createMockResultSet(emptyData);

        printer.printRecords(rs);
        assertEquals("", sw.toString());
    }

    // =========================================================================
    // Partition H: Defensive Guard Paths & Exceptions
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullAppendableThrows() {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFormatThrows() {
        new CSVPrinter(new StringBuilder(), null);
    }
}