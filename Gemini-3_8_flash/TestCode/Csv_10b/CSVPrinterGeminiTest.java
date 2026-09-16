package org.apache.commons.csv;

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

import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Branches & Scenarios:
 * 1. Constructor:
 *    - null 'out' argument -> IllegalArgumentException.
 *    - null 'format' argument -> IllegalArgumentException.
 *    - format.validate() invalid parameters -> IllegalArgumentException.
 *    - format with header (DEFECT ZONE CSV-1 / testHeader): format.getHeader() != null
 *      must be output upon printer initialization.
 * 2. Lifecycle & Stream Delegation:
 *    - close() when Appendable is Closeable vs not Closeable.
 *    - close() propagating IOException from underlying Appendable.
 *    - flush() when Appendable is Flushable vs not Flushable.
 *    - flush() propagating IOException from underlying Appendable.
 *    - getOut() returns the exact target Appendable.
 * 3. Print Values & Formatting:
 *    - null value with null format.getNullString() vs configured nullString.
 *    - newRecord state transition (printing delimiter when !newRecord).
 * 4. Quoting Modes & Policies:
 *    - isQuoting() enabled vs isEscaping() vs plain unquoted/unescaped.
 *    - Quote.ALL: all tokens quoted unconditionally.
 *    - Quote.NON_NUMERIC: Number instances not quoted, non-numbers quoted.
 *    - Quote.NONE: delegates to printAndEscape.
 *    - Quote.MINIMAL:
 *      * len <= 0 & newRecord -> quoted.
 *      * len <= 0 & !newRecord -> empty unquoted.
 *      * start char < '0', > '9' && < 'A', > 'Z' && < 'a', > 'z' on newRecord -> quoted.
 *      * start char <= COMMENT ('#') -> quoted.
 *      * internal quoteChar, delimChar, CR, LF -> quoted and quoteChar doubled ("").
 *      * end char <= SP (' ') -> quoted.
 *      * unquoted alphanumeric on !newRecord.
 * 5. Escaping Mode (printAndEscape):
 *    - CR mapped to 'r', LF mapped to 'n', escape char escaped, delim char escaped.
 *    - Unescaped segments preserved correctly.
 * 6. Comments (printComment):
 *    - isCommentingEnabled() false -> no-op.
 *    - isCommentingEnabled() true on newRecord vs mid-record (!newRecord invokes println()).
 *    - CR, LF, CRLF multi-line parsing with comment prefix insertion.
 * 7. Records & Batch Printing:
 *    - println() with null record separator vs valid separator.
 *    - printRecord(Iterable) and printRecord(Object...).
 *    - printRecords(Iterable) with nested arrays, Iterables, and flat objects.
 *    - printRecords(Object[]) with nested arrays, Iterables, and flat objects.
 *    - printRecords(ResultSet) iterating over metadata columnCount and rows.
 */
public class CSVPrinterGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where CSVPrinter constructor does not print the header
     * specified in CSVFormat.
     */
    @Test(timeout = 4000)
    public void testHeader() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("C1", "C2", "C3");
        new CSVPrinter(sw, format);
        assertEquals("C1,C2,C3\r\n", sw.toString());
    }

    /**
     * Targets header printing combined with subsequent record printing.
     */
    @Test(timeout = 4000)
    public void testHeaderFollowedByRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("A", "B");
        assertEquals("Col1,Col2\r\nA,B\r\n", sw.toString());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintBasicTokens() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("a");
        printer.print("b");
        printer.println();
        assertEquals("a,b\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordObjectArray() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("val1", "val2", "val3");
        assertEquals("val1,val2,val3\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordIterable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final List<String> list = Arrays.asList("x", "y", "z");
        printer.printRecord(list);
        assertEquals("x,y,z\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsNestedInObjectArray() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final Object[] records = new Object[] {
            new Object[] { "r1c1", "r1c2" },
            Arrays.asList("r2c1", "r2c2"),
            "singleVal"
        };
        printer.printRecords(records);
        assertEquals("r1c1,r1c2\r\nr2c1,r2c2\r\nsingleVal\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsNestedInIterable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        final List<Object> records = Arrays.asList(
            new Object[] { "a1", "a2" },
            Arrays.asList("b1", "b2"),
            "c1"
        );
        printer.printRecords(records);
        assertEquals("a1,a2\r\nb1,b2\r\nc1\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetOutReturnsExactAppendable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        assertSame(sb, printer.getOut());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Quoting / Escaping Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintNullValueWithoutNullString() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print(null);
        printer.print("next");
        printer.println();
        // First token is empty string, which gets quoted under MINIMAL because newRecord && len <= 0
        assertEquals("\"\",next\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintNullValueWithNullString() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL_VAL");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print(null);
        printer.print("data");
        printer.println();
        assertEquals("NULL_VAL,data\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyTokenQuotingMinimal() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.print("a");
        // Second empty token on the line: newRecord is false, so it shouldn't be quoted
        printer.print("");
        printer.println();
        assertEquals("a,\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuotePolicyAll() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("hello", 123);
        assertEquals("\"hello\",\"123\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuotePolicyNonNumeric() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord(100, "text", 200.5, true);
        assertEquals("100,\"text\",200.5,\"true\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuotePolicyNoneDelegatesToEscape() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape('\\');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("a,b", "c\nd", "e\rf", "g\\h");
        assertEquals("a\\,b,c\\nd,e\\rf,g\\\\h\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuoteCharDoubling() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("He said \"Hello\"");
        assertEquals("\"He said \"\"Hello\"\"\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteBoundaryCharacters() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        // Special start characters on newRecord:
        // '< 0': '-' or '+' or '/'
        // '> 9 && < A': ':'
        // '> Z && < a': '['
        // '> z': '{'
        // '<= COMMENT': '#'
        printer.printRecord("-dash", ":colon", "[bracket", "{brace", "#commentChar");
        assertEquals("\"-dash\",\":colon\",\"[bracket\",\"{brace\",\"#commentChar\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteEndingWithWhitespace() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        // 'word ' ends in space <= SP
        printer.printRecord("first", "word ");
        assertEquals("first,\"word \"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteAlphanumericNotFirstDoesNotQuote() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        // Neither first token nor second contains special chars; second starts with '$' (> '#')
        printer.print("word");
        printer.print("$money");
        printer.println();
        assertEquals("word,$money\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteWithDelimAndNewlines() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        printer.printRecord("a,b", "line\nbreak", "cr\rbreak");
        assertEquals("\"a,b\",\"line\nbreak\",\"cr\rbreak\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPureEscapingModeNoQuote() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat(',')
                .withEscape('\\')
                .withRecordSeparator("\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("hello,world", "line\r\nbreak", "esc\\char");
        assertEquals("hello\\,world,line\\r\\nbreak,esc\\\\char\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testRawModeNeitherQuotingNorEscaping() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator("\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("raw1", "raw2");
        assertEquals("raw1,raw2\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintlnWithNullRecordSeparator() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("first");
        printer.println();
        printer.print("second");
        // With null record separator, no newline appended, but newRecord reset to true
        // so second is the start of a new record (no delimiter between first and second)
        assertEquals("first\"second\"", sw.toString());
    }

    // =========================================================================
    // Partition D: Comments Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintCommentDisabledDoesNothing() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT); // commenting disabled by default
        printer.printComment("This comment should be ignored");
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentSingleLine() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("Simple comment");
        assertEquals("# Simple comment\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentMultiLineCRLF() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printComment("Line1\r\nLine2\nLine3\rLine4");
        assertEquals("# Line1\r\n# Line2\r\n# Line3\r\n# Line4\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentMidRecordStartsOnNewLine() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.print("col1");
        printer.printComment("Comment after col1");
        assertEquals("col1\r\n# Comment after col1\r\n", sw.toString());
    }

    // =========================================================================
    // Partition E: Lifecycle & Defensive Guards
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullOut() throws IOException {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVPrinter(new StringWriter(), null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorInconsistentFormatThrows() throws IOException {
        // Delimiter and QuoteChar cannot be identical
        final CSVFormat invalidFormat = CSVFormat.DEFAULT.withDelimiter('!').withQuote('!');
        new CSVPrinter(new StringWriter(), invalidFormat);
    }

    @Test(timeout = 4000)
    public void testCloseOnCloseable() throws IOException {
        final boolean[] closed = new boolean[] { false };
        final CloseableAppendable ca = new CloseableAppendable(closed, null);
        final CSVPrinter printer = new CSVPrinter(ca, CSVFormat.DEFAULT);
        printer.close();
        assertTrue("Underlying stream should have been closed", closed[0]);
    }

    @Test(timeout = 4000)
    public void testCloseOnNonCloseable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.close(); // Should safely do nothing without exception
    }

    @Test(timeout = 4000)
    public void testFlushOnFlushable() throws IOException {
        final boolean[] flushed = new boolean[] { false };
        final FlushableAppendable fa = new FlushableAppendable(flushed);
        final CSVPrinter printer = new CSVPrinter(fa, CSVFormat.DEFAULT);
        printer.flush();
        assertTrue("Underlying stream should have been flushed", flushed[0]);
    }

    @Test(timeout = 4000)
    public void testFlushOnNonFlushable() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
        printer.flush(); // Should safely do nothing without exception
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testPropagatesCloseException() throws IOException {
        final Appendable throwingApp = (Appendable & Closeable) Proxy.newProxyInstance(
            CSVPrinterGeminiTest.class.getClassLoader(),
            new Class<?>[] { Appendable.class, Closeable.class },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("close".equals(method.getName())) {
                        throw new IOException("Close failed");
                    }
                    return proxy;
                }
            }
        );
        final CSVPrinter printer = new CSVPrinter(throwingApp, CSVFormat.DEFAULT);
        printer.close();
    }

    // =========================================================================
    // Partition F: JDBC ResultSet Integration
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintRecordsResultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final String[] columns = new String[] { "ID", "Name" };
        final Object[][] data = new Object[][] {
            { "1", "Alice" },
            { "2", "Bob" }
        };
        final ResultSet mockRs = createMockResultSet(columns, data);

        printer.printRecords(mockRs);
        assertEquals("1,Alice\r\n2,Bob\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsEmptyResultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final String[] columns = new String[] { "Col1" };
        final Object[][] data = new Object[0][];
        final ResultSet mockRs = createMockResultSet(columns, data);

        printer.printRecords(mockRs);
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000, expected = SQLException.class)
    public void testPrintRecordsResultSetPropagatesSQLException() throws SQLException, IOException {
        final ResultSet throwingRs = (ResultSet) Proxy.newProxyInstance(
            ResultSet.class.getClassLoader(),
            new Class<?>[] { ResultSet.class },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("getMetaData".equals(method.getName())) {
                        throw new SQLException("Database error");
                    }
                    return null;
                }
            }
        );

        final CSVPrinter printer = new CSVPrinter(new StringWriter(), CSVFormat.DEFAULT);
        printer.printRecords(throwingRs);
    }

    // =========================================================================
    // Test Helpers & Dynamic Mocks
    // =========================================================================

    private static class CloseableAppendable implements Appendable, Closeable {
        private final boolean[] closed;
        private final StringBuilder delegate = new StringBuilder();

        CloseableAppendable(final boolean[] closed, final String initial) {
            this.closed = closed;
            if (initial != null) {
                this.delegate.append(initial);
            }
        }

        @Override
        public Appendable append(CharSequence csq) {
            delegate.append(csq);
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            delegate.append(csq, start, end);
            return this;
        }

        @Override
        public Appendable append(char c) {
            delegate.append(c);
            return this;
        }

        @Override
        public void close() {
            closed[0] = true;
        }
    }

    private static class FlushableAppendable implements Appendable, Flushable {
        private final boolean[] flushed;

        FlushableAppendable(final boolean[] flushed) {
            this.flushed = flushed;
        }

        @Override
        public Appendable append(CharSequence csq) {
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            return this;
        }

        @Override
        public Appendable append(char c) {
            return this;
        }

        @Override
        public void flush() {
            flushed[0] = true;
        }
    }

    private ResultSet createMockResultSet(final String[] headers, final Object[][] data) {
        final InvocationHandler handler = new InvocationHandler() {
            private int rowIndex = -1;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final String name = method.getName();
                if ("getMetaData".equals(name)) {
                    return Proxy.newProxyInstance(
                        ResultSetMetaData.class.getClassLoader(),
                        new Class<?>[] { ResultSetMetaData.class },
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object p, Method m, Object[] a) {
                                if ("getColumnCount".equals(m.getName())) {
                                    return headers.length;
                                }
                                return null;
                            }
                        }
                    );
                } else if ("next".equals(name)) {
                    rowIndex++;
                    return rowIndex < data.length;
                } else if ("getString".equals(name)) {
                    final int colIndex = ((Integer) args[0]) - 1;
                    final Object val = data[rowIndex][colIndex];
                    return val == null ? null : String.valueOf(val);
                }
                return null;
            }
        };

        return (ResultSet) Proxy.newProxyInstance(
            ResultSet.class.getClassLoader(),
            new Class<?>[] { ResultSet.class },
            handler
        );
    }
}