package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

/* [Branch & Defect Analysis Matrix]
 * Targeted branches and defect conditions:
 * 
 * 1. Null value handling with custom null string: CSVPrinter.print(Object) -> when value==null and format.getNullString()!=null
 *    - Branch: if (value == null) { final String nullString = format.getNullString(); strValue = nullString == null ? Constants.EMPTY : nullString; }
 *    - Defect: MySQL NULL string output may be inconsistent when nullString is set
 * 
 * 2. print() private method branches:
 *    - newRecord check: delimiter insertion skipped on first column
 *    - Quote character set check: if (format.isQuoteCharacterSet()) -> printAndQuote else if (format.isEscapeCharacterSet()) -> printAndEscape else -> out.append
 * 
 * 3. printAndQuote() method branches:
 *    - QuoteMode handling: ALL, NON_NUMERIC, NONE, MINIMAL (default), and default exception
 *    - MINIMAL mode sub-branches:
 *      a) len <= 0 -> always quote if newRecord
 *      b) newRecord char check: (c < '0' || (c > '9' && c < 'A') || (c > 'Z' && c < 'a') || (c > 'z'))
 *      c) c <= COMMENT check
 *      d) While loop: c == LF || c == CR || c == quoteChar || c == delimChar
 *      e) End char check: c <= SP
 *    - Actual quoting loop: while (pos < end) { if (c == quoteChar) { ... } }
 * 
 * 4. printAndEscape() method branches:
 *    - While loop: c == CR || c == LF || c == delim || c == escape
 *    - Character replacement: LF -> 'n', CR -> 'r'
 * 
 * 5. printComment() method branches:
 *    - if (!format.isCommentMarkerSet()) early return
 *    - CR and LF handling with fall-through
 * 
 * 6. println() branch: if (recordSeparator != null)
 * 
 * 7. Constructor branches:
 *    - Header comments iteration with null check
 *    - Header printing: if (format.getHeader() != null && !format.getSkipHeaderRecord())
 * 
 * 8. printRecords() branches:
 *    - instanceof Object[] -> printRecord((Object[]) value)
 *    - instanceof Iterable -> printRecord((Iterable<?>) value)
 *    - else -> printRecord(value)
 * 
 * 9. close() and flush() polymorphic checks:
 *    - if (out instanceof Closeable)
 *    - if (out instanceof Flushable)
 * 
 * Defect target: TestMySqlNullOutput and testMySqlNullStringDefault
 * - When nullString is set (e.g., "NULL"), null values should be printed as nullString, not empty
 * - When nullString is set to "\N", null values should print "\N"
 */
public class CSVPrinterDeepseekTest {

    @Test(timeout = 4000)
    public void testNullValueWithCustomNullString() throws IOException {
        // Defect-targeted: TestMySqlNullOutput - null should be printed as "NULL" when nullString="NULL"
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print(null);
            printer.println();
        }
        assertEquals("NULL\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueWithBackslashNNullString() throws IOException {
        // Defect-targeted: TestMySqlNullStringDefault - null should be printed as "\N"
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print(null);
            printer.println();
        }
        assertEquals("\\N\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueDefaultNullString() throws IOException {
        // When nullString is null, null should print as empty string
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print(null);
            printer.println();
        }
        assertEquals("\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintMultipleValuesWithNulls() throws IOException {
        // Mix of null and non-null values with custom null string
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.printRecord("a", null, "b");
        }
        assertEquals("a,N/A,b\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordWithIterable() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecord(Arrays.asList("x", "y", "z"));
        }
        assertEquals("x,y,z\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordVarargs() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecord("1", "2", "3");
        }
        assertEquals("1,2,3\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithEscapeCharacter() throws IOException {
        // Test printAndEscape: delimiter and newline should be escaped
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("a,b");
            printer.print("c\nd");
            printer.println();
        }
        assertTrue(out.toString().contains("a\\,b"));
        assertTrue(out.toString().contains("c\\nd"));
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteAll() throws IOException {
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("hello");
            printer.print("world");
            printer.println();
        }
        assertEquals("\"hello\",\"world\"\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteNonNumericNumber() throws IOException {
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print(123);
            printer.println();
        }
        assertEquals("123\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteNonNumericString() throws IOException {
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("abc");
            printer.println();
        }
        assertEquals("\"abc\"\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteModeNone() throws IOException {
        // QuoteMode.NONE should delegate to printAndEscape
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape('\\');
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("a,b");
            printer.println();
        }
        assertEquals("a\\,b\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalLeadingNonAlphaNum() throws IOException {
        // MINIMAL: first char on new record is not alphanumeric -> should quote
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("!hello");
            printer.println();
        }
        assertEquals("\"!hello\"\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalLeadingCommentChar() throws IOException {
        // MINIMAL: first char <= COMMENT ('#') -> should quote (c <= COMMENT)
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("#header");
            printer.println();
        }
        assertEquals("\"#header\"\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalLeadingSpace() throws IOException {
        // MINIMAL: first char is space which is > COMMENT but should not trigger newRecord alphanum rule normally
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print(" hello");
            printer.println();
        }
        // Space is between ' ' and '0'? Actually space is 0x20, COMMENT is 0x23 so c <= COMMENT is false for space
        // Check newRecord char rule: space is not < '0', so it goes to else if (c <= COMMENT) which is false
        // Then while loop checks for LF, CR, quote, delim - none present, then end char check: c <= SP -> space is equal to SP so quote!
        assertEquals("\" hello\"\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalContainsDelimiter() throws IOException {
        // MINIMAL: value contains delimiter
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("a,b");
            printer.println();
        }
        assertEquals("\"a,b\"\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalContainsQuote() throws IOException {
        // MINIMAL: value contains quote char
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("a\"b");
            printer.println();
        }
        assertEquals("\"a\"\"b\"\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalEmptyFirstValue() throws IOException {
        // MINIMAL: empty token on newRecord
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("");
            printer.print("next");
            printer.println();
        }
        assertEquals("\"\",next\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalNonEmptyNoSpecialChars() throws IOException {
        // MINIMAL: normal alphanumeric string without special chars
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("hello");
            printer.println();
        }
        assertEquals("hello\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintComment() throws IOException {
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.printComment("note");
        }
        assertEquals("# note\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentWithLineBreak() throws IOException {
        // Comment with embedded newlines
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.printComment("line1\nline2");
        }
        assertEquals("# line1\n# line2\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentWithCRLF() throws IOException {
        // Comment with CRLF
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.printComment("line1\r\nline2");
        }
        assertEquals("# line1\n# line2\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintCommentDisabled() throws IOException {
        // Comment marker not set -> nothing printed
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.printComment("should not appear");
        }
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintln() throws IOException {
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.println();
        }
        assertEquals("\r\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintlnNullRecordSeparator() throws IOException {
        // When record separator is null, println should set newRecord but append nothing
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.newFormat(',').withRecordSeparator(null);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.println();
        }
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testConstructorWithHeaderComments() throws IOException {
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("comment1", "comment2").withHeader("a", "b");
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            // Constructor prints headers
        }
        assertTrue(out.toString().startsWith("# comment1\n# comment2\n"));
        assertTrue(out.toString().contains("a,b\n"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithSkipHeaderRecord() throws IOException {
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b").withSkipHeaderRecord(true);
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            // Header should not be printed
        }
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testConstructorNullHeaderComments() throws IOException {
        // Null header comments should be skipped
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            // No exception expected
        }
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsObjectArray() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecords((Object[]) new String[] { "a", "b" });
        }
        assertEquals("a\nb\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsIterableOfObjectArrays() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecords(Arrays.asList(new Object[] { "x", "y" }, new Object[] { "1", "2" }));
        }
        assertEquals("x,y\n1,2\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsIterableOfIterables() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecords(Arrays.asList(Arrays.asList("p", "q"), Arrays.asList("r", "s")));
        }
        assertEquals("p,q\nr,s\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsIterableOfSimpleObjects() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecords(Arrays.asList("single1", "single2"));
        }
        assertEquals("single1\nsingle2\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testGetOut() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            assertSame(out, printer.getOut());
        }
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.flush(); // Should not throw
        }
    }

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        StringWriter out = new StringWriter();
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);
        printer.close();
        // After close, writing should fail
        try {
            printer.print("test");
            fail("Expected IOException after close");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullOut() throws IOException {
        new CSVPrinter(null, CSVFormat.DEFAULT);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVPrinter(new StringWriter(), null);
    }

    @Test(timeout = 4000)
    public void testPrintWithEscapeCarriageReturn() throws IOException {
        // Ensure CR is escaped to 'r' in printAndEscape
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("a\rb");
            printer.println();
        }
        assertEquals("a\\rb\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithEscapeDelimiterAndNewline() throws IOException {
        // Multiple escape characters in sequence
        StringWriter out = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        try (CSVPrinter printer = new CSVPrinter(out, format)) {
            printer.print("a,b\nc");
            printer.println();
        }
        assertEquals("a\\,b\\nc\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecordsResultSet() throws Exception {
        // Use a simple in-memory database
        java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        try {
            conn.createStatement().execute("CREATE TABLE test (id INT, name VARCHAR)");
            conn.createStatement().execute("INSERT INTO test VALUES (1, 'Alice')");
            conn.createStatement().execute("INSERT INTO test VALUES (2, 'Bob')");
            java.sql.ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM test ORDER BY id");
            StringWriter out = new StringWriter();
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
                printer.printRecords(rs);
            }
            assertEquals("1,Alice\n2,Bob\n", out.toString());
        } finally {
            conn.close();
        }
    }

    @Test(timeout = 4000)
    public void testPrintNestedObjectArray() throws IOException {
        // printRecords with nested arrays
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            printer.printRecords((Object) new Object[] { new Object[] { "a", "b" }, new Object[] { "c", "d" } });
        }
        assertEquals("a,b\nc,d\n", out.toString());
    }
}