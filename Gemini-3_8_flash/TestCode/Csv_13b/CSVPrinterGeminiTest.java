package org.apache.commons.csv;

import org.junit.Test;

import java.io.CharArrayWriter;
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

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Constructor Header Handling:
 *    - format.getHeaderComments() != null vs null; with null & non-null lines.
 *    - format.getHeader() != null with format.getSkipHeaderRecord() true vs false.
 *    - Null check guards for 'out' and 'format'.
 * 2. Flushable & Closeable Life-cycle:
 *    - out instanceof Flushable / Closeable vs non-flushable/non-closeable Appendable.
 * 3. print(Object) & Null Handling:
 *    - value == null with format.getNullString() == null vs custom nullString.
 *    - Defect Target (Defects4J): QuoteMode.NON_NUMERIC quoting null values (null instanceof Number == false).
 *    - Defect Target (Defects4J): MySQL null string output formatting without incorrect quoting or escaping.
 * 4. Delimiter and newRecord State Transitions:
 *    - newRecord == true (start of line) -> no prepended delimiter.
 *    - newRecord == false (subsequent values) -> delimiter prepended.
 * 5. Quoting Policies (QuoteMode):
 *    - ALL: every field enclosed in quoteChar.
 *    - NON_NUMERIC: numbers unquoted, strings/nulls evaluated according to specification.
 *    - NONE: redirects to printAndEscape.
 *    - MINIMAL:
 *      * len <= 0: empty value at start of record (quoted) vs subsequent (unquoted).
 *      * len > 0, newRecord && char not in [0-9A-Za-z] -> quote.
 *      * len > 0, char <= '#' (COMMENT) -> quote.
 *      * len > 0, contains LF, CR, quoteChar, or delimChar -> quote; quoteChar doubled.
 *      * len > 0, trailing char <= ' ' (SP) -> quote.
 *      * else: unquoted output.
 * 6. printAndEscape Logic:
 *    - Escaping CR -> '\r', LF -> '\n', delimiter -> '\<delim>', escapeChar -> '\<escape>'.
 *    - Segments before/after escaped characters.
 * 7. printComment:
 *    - Comment marker not set -> early exit.
 *    - !newRecord -> inserts newline prior to comment.
 *    - Line endings: CRLF handled as single newline; CR alone; LF alone; standard text.
 * 8. printRecords Dispatching:
 *    - Nested arrays (Object[]), nested Iterables, flat Objects.
 *    - ResultSet pagination & multi-column mapping via dynamic Java reflection proxy.
 * 9. Appendable without Flushable/Closeable:
 *    - Custom StringBuilder-like Appendable to verify no ClassCastException on flush/close.
 */
public class CSVPrinterGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintBasicValuesAndGetOut() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);
        assertSame(sw, printer.getOut());

        printer.print("a");
        printer.print("b");
        printer.println();
        printer.print("c");

        assertEquals("a,b\r\nc", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordVarargsAndIterable() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.RFC4180);

        printer.printRecord("col1", "col2", "col3");
        printer.printRecord(Arrays.asList("v1", "v2", "v3"));

        assertEquals("col1,col2,col3\r\nv1,v2,v3\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFlushAndClose() throws IOException {
        final CustomAppendable customApp = new CustomAppendable();
        final CSVPrinter printer = new CSVPrinter(customApp, CSVFormat.DEFAULT);

        assertFalse(customApp.flushed);
        assertFalse(customApp.closed);

        printer.flush();
        assertTrue(customApp.flushed);

        printer.close();
        assertTrue(customApp.closed);
    }

    @Test(timeout = 4000)
    public void testNonFlushableNonCloseableAppendable() throws IOException {
        final NonFlushableAppendable app = new NonFlushableAppendable();
        final CSVPrinter printer = new CSVPrinter(app, CSVFormat.DEFAULT);

        printer.print("val");
        printer.flush();
        printer.close();
        assertEquals("val", app.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndNullValuesWithDefaultFormat() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        // Empty token at start of record must be quoted in MINIMAL mode
        printer.print("");
        // Empty token not at start of record is not quoted
        printer.print("");
        printer.print(null);
        printer.println();

        assertEquals("\"\",,\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMinimalQuoteBoundaryCharacters() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        // First char <= '#' (COMMENT) triggers quote
        printer.print("#hashtag");
        printer.println();

        // Char with space at the end triggers quote
        printer.print("ends-with-space ");
        printer.println();

        // Leading char not alphanumeric at start of record triggers quote
        printer.print("-startDash");
        printer.println();

        // Alphanumeric does not quote
        printer.print("Alpha123");
        // Space in the middle but not start/end or delim
        printer.print("mid space");
        printer.println();

        assertEquals("\"#hashtag\"\r\n\"ends-with-space \"\r\n\"-startDash\"\r\nAlpha123,mid space\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuoteEscapingInsideValue() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        printer.print("He said, \"Hello!\"");
        printer.println();

        // Enclosed in quotes, inner quote doubled: "He said, ""Hello!"""
        assertEquals("\"He said, \"\"Hello!\"\"\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuoteModeAll() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord("a", 123, "");
        assertEquals("\"a\",\"123\",\"\"\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testQuoteModeNoneUsesEscaping() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuoteMode(QuoteMode.NONE);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("a,b\rc\nd\\e");
        printer.println();

        assertEquals("a\\,b\\rc\\nd\\\\e\r\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPureEscapeFormatWithoutQuotes() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat(',')
                .withEscape('/')
                .withRecordSeparator("\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("a,b/c\r\n");
        printer.println();

        assertEquals("a/,b//c/r/n\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFormatWithoutQuotesAndWithoutEscaping() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.newFormat('|').withRecordSeparator("\n");
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("plain");
        printer.print("text|with|delim");
        printer.println();

        assertEquals("plain|text|with|delim\n", sw.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Targets known defect: testMySqlNullOutput
     * Under QuoteMode.NON_NUMERIC, null values should not be quoted as non-numeric strings
     * when a nullString is specified (or when null is printed).
     */
    @Test(timeout = 4000)
    public void testMySqlNullOutput() throws IOException {
        final Object[] s = new String[] { "NULL", null };
        final CSVFormat format = CSVFormat.MYSQL.withQuote('\"').withNullString("NULL").withQuoteMode(QuoteMode.NON_NUMERIC);
        final StringWriter writer = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(writer, format);
        printer.printRecord(s);
        assertEquals("\"NULL\"\tNULL\n", writer.toString());
    }

    /**
     * Targets known defect: testMySqlNullStringDefault
     * MySQL format uses '\\' escape, tab delimiter, '\n' record separator, and "\\N" default null string.
     */
    @Test(timeout = 4000)
    public void testMySqlNullStringDefault() throws IOException {
        final StringWriter writer = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(writer, CSVFormat.MYSQL);
        printer.print(null);
        assertEquals("\\N", writer.toString());
    }

    @Test(timeout = 4000)
    public void testNonNumericQuotingWithNumbersAndStrings() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.printRecord(100, 3.14f, "text");
        assertEquals("100,3.14,\"text\"\r\n", sw.toString());
    }

    // =========================================================================
    // Partition D: Comments & Header Processing
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintCommentDisabled() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT); // No comment marker
        printer.printComment("This should be ignored");
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentVariousLineEndings() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("data1");
        // !newRecord branch: must print newline before comment
        printer.printComment("Comment line 1\r\nComment line 2\rComment line 3\nComment line 4");
        printer.print("data2");
        printer.println();

        final String expected = "data1\r\n"
                + "# Comment line 1\r\n"
                + "# Comment line 2\r\n"
                + "# Comment line 3\r\n"
                + "# Comment line 4\r\n"
                + "data2\r\n";
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testConstructorHeaderAndHeaderComments() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withHeaderComments("Comment 1", null, "Comment 2")
                .withHeader("ColA", "ColB");

        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("ValA", "ValB");

        final String expected = "# Comment 1\r\n"
                + "# Comment 2\r\n"
                + "ColA,ColB\r\n"
                + "ValA,ValB\r\n";
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testConstructorHeaderSkipHeaderRecord() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT
                .withHeader("ColA", "ColB")
                .withSkipHeaderRecord(true);

        final CSVPrinter printer = new CSVPrinter(sw, format);
        printer.printRecord("ValA", "ValB");

        assertEquals("ValA,ValB\r\n", sw.toString());
    }

    // =========================================================================
    // Partition E: printRecords Hierarchical & ResultSet Dispatches
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintRecordsWithNestedArraysAndIterables() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final Object[] nestedArray = new Object[]{"a1", "a2"};
        final List<String> nestedList = Arrays.asList("b1", "b2");
        final String flatObject = "c1";

        // Test Object... variant
        printer.printRecords(nestedArray, nestedList, flatObject);
        // Test Iterable<?> variant
        printer.printRecords(Arrays.asList(nestedArray, nestedList, flatObject));

        final String expectedRecord = "a1,a2\r\nb1,b2\r\nc1\r\n";
        assertEquals(expectedRecord + expectedRecord, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsFromResultSet() throws SQLException, IOException {
        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT);

        final ResultSet mockRs = createMockResultSet(
                new String[][]{
                        {"row1_c1", "row1_c2"},
                        {"row2_c1", "row2_c2"}
                }
        );

        printer.printRecords(mockRs);
        assertEquals("row1_c1,row1_c2\r\nrow2_c1,row2_c2\r\n", sw.toString());
    }

    // =========================================================================
    // Partition F: Exception Handling & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullAppendable() throws IOException {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFormat() throws IOException {
        new CSVPrinter(new StringWriter(), null);
    }

    @Test(timeout = 4000)
    public void testPrintWithNullRecordSeparator() throws IOException {
        final StringWriter sw = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        final CSVPrinter printer = new CSVPrinter(sw, format);

        printer.print("val1");
        printer.println();
        printer.print("val2");

        // When record separator is null, println() should not append anything
        assertEquals("val1val2", sw.toString());
    }

    // =========================================================================
    // Helper Test Implementations (Clean Room, Zero External Mocks)
    // =========================================================================

    private static class CustomAppendable implements Appendable, Flushable, Closeable {
        final StringBuilder sb = new StringBuilder();
        boolean flushed = false;
        boolean closed = false;

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
        public void flush() {
            flushed = true;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private static class NonFlushableAppendable implements Appendable {
        final StringBuilder sb = new StringBuilder();

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
        public String toString() {
            return sb.toString();
        }
    }

    private static ResultSet createMockResultSet(final String[][] data) {
        final int colCount = (data.length > 0) ? data[0].length : 0;
        final int[] cursor = new int[]{-1};

        final InvocationHandler metaHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("getColumnCount".equals(method.getName())) {
                    return colCount;
                }
                return null;
            }
        };

        final ResultSetMetaData metaProxy = (ResultSetMetaData) Proxy.newProxyInstance(
                CSVPrinterGeminiTest.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                metaHandler
        );

        final InvocationHandler rsHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                final String name = method.getName();
                if ("getMetaData".equals(name)) {
                    return metaProxy;
                } else if ("next".equals(name)) {
                    cursor[0]++;
                    return cursor[0] < data.length;
                } else if ("getObject".equals(name)) {
                    final int colIndex = (Integer) args[0] - 1;
                    return data[cursor[0]][colIndex];
                }
                return null;
            }
        };

        return (ResultSet) Proxy.newProxyInstance(
                CSVPrinterGeminiTest.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                rsHandler
        );
    }
}