/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.csv.CSVFormat
 *
 * 1. DEFECT-TARGETED BRANCHES (Defects4J Known Regressions):
 *    - CSVFormat.MYSQL constant: specification requires nullString to be "\\N", but in the defective
 *      version withNullString("\\N") was omitted during construction, leaving nullString as null.
 *      Targeted by: testMySqlNullStringDefault()
 *
 * 2. STRUCTURAL BRANCH COVERAGE (Validation & Consistency Guard):
 *    - validate():
 *      * isLineBreak(delimiter) -> CRLF/LF/CR (Throws IllegalArgumentException)
 *      * quoteCharacter != null && delimiter == quoteCharacter (Throws IllegalArgumentException)
 *      * escapeCharacter != null && delimiter == escapeCharacter (Throws IllegalArgumentException)
 *      * commentMarker != null && delimiter == commentMarker (Throws IllegalArgumentException)
 *      * quoteCharacter != null && quoteCharacter.equals(commentMarker) (Throws IllegalArgumentException)
 *      * escapeCharacter != null && escapeCharacter.equals(commentMarker) (Throws IllegalArgumentException)
 *      * escapeCharacter == null && quoteMode == QuoteMode.NONE (Throws IllegalArgumentException)
 *      * header duplicate detection (Throws IllegalArgumentException)
 *
 * 3. FACTORY & PREDEFINED ENUM BRANCHES:
 *    - Predefined enum: DEFAULT, EXCEL, MYSQL, RFC4180, TDF (values, valueOf, getFormat)
 *    - CSVFormat.valueOf(String): valid names, invalid names
 *    - CSVFormat.newFormat(char): minimal instance, verifies null/false defaults
 *
 * 4. EQUIVALENCE PARTITIONING & MUTATION IMMUTABILITY (with* Methods):
 *    - withDelimiter(char): valid char vs line-break
 *    - withQuote(char/Character): valid vs line-break vs null
 *    - withQuoteMode(QuoteMode): ALL, MINIMAL, NON_NUMERIC, NONE
 *    - withCommentMarker(char/Character): valid vs line-break vs null
 *    - withEscape(char/Character): valid vs line-break vs null
 *    - withIgnoreSurroundingSpaces(boolean) & parameterless
 *    - withIgnoreEmptyLines(boolean) & parameterless
 *    - withRecordSeparator(char/String)
 *    - withNullString(String)
 *    - withHeader(String...): null, empty, distinct headers
 *    - withHeader(ResultSet) & withHeader(ResultSetMetaData): null and non-null via java.lang.reflect.Proxy
 *    - withHeaderComments(Object...): null, mixed types, null-element
 *    - withSkipHeaderRecord(boolean) & parameterless
 *    - withAllowMissingColumnNames(boolean) & parameterless
 *    - withIgnoreHeaderCase(boolean) & parameterless
 *
 * 5. EQUALS, HASHCODE & TOSTRING:
 *    - equals: reflexivity, null, different class, each individual field variation
 *    - hashCode: consistency with equals
 *    - toString: all conditional string builders (escape, quote, comment, nullString, recordSeparator,
 *      emptyLines, surroundingSpaces, ignoreHeaderCase, skipHeaderRecord, headerComments, header)
 *
 * 6. FORMAT, PARSE & PRINT:
 *    - format(Object...): basic output check
 *    - parse(Reader) & print(Appendable): verifies proper instantiation of CSVParser and CSVPrinter
 * ====================================================================================================
 */

package org.apache.commons.csv;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CSVFormatGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Known Bug)
    // =========================================================================

    /**
     * Targets the defect where CSVFormat.MYSQL was initialized without withNullString("\\N").
     * The specification documents:
     * "The default NULL string is "\\N". Settings are: withNullString("\\N")"
     */
    @Test(timeout = 4000)
    public void testMySqlNullStringDefault() {
        assertNotNull("CSVFormat.MYSQL must not be null", CSVFormat.MYSQL);
        assertTrue("CSVFormat.MYSQL should have nullString set", CSVFormat.MYSQL.isNullStringSet());
        assertEquals("CSVFormat.MYSQL default nullString must be \"\\\\N\"", "\\N", CSVFormat.MYSQL.getNullString());
    }

    /**
     * Verifies that formatting null values using MYSQL format prints the defined null representation.
     */
    @Test(timeout = 4000)
    public void testMySqlNullFormatting() {
        String formatted = CSVFormat.MYSQL.format("a", null, "b");
        assertEquals("a\t\\N\tb", formatted);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & PREDEFINED FORMATS
    // =========================================================================

    @Test(timeout = 4000)
    public void testPredefinedFormats() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());

        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());
        assertEquals(',', CSVFormat.RFC4180.getDelimiter());

        assertTrue(CSVFormat.EXCEL.getAllowMissingColumnNames());
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testPredefinedEnumAndValueOf() {
        for (CSVFormat.Predefined p : CSVFormat.Predefined.values()) {
            assertNotNull(p.getFormat());
            assertSame(p.getFormat(), CSVFormat.valueOf(p.name()));
        }
    }

    @Test(timeout = 4000)
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getQuoteMode());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeaderComments());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
    }

    @Test(timeout = 4000)
    public void testFormatSimpleValues() {
        String formatted = CSVFormat.DEFAULT.format("foo", "bar");
        assertEquals("foo,bar", formatted);
    }

    @Test(timeout = 4000)
    public void testParseAndPrint() throws IOException {
        Reader in = new StringReader("A,B\n1,2");
        CSVParser parser = CSVFormat.DEFAULT.parse(in);
        assertNotNull(parser);
        assertFalse(parser.isClosed());
        parser.close();

        Appendable out = new StringWriter();
        CSVPrinter printer = CSVFormat.DEFAULT.print(out);
        assertNotNull(printer);
        printer.printRecord("val1", "val2");
        assertEquals("val1,val2\r\n", out.toString());
        printer.close();
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & WITH-* MUTATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
        assertNotEquals(format, CSVFormat.DEFAULT);
    }

    @Test(timeout = 4000)
    public void testWithQuote() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertTrue(format.isQuoteCharacterSet());

        CSVFormat noQuote = format.withQuote((Character) null);
        assertNull(noQuote.getQuoteCharacter());
        assertFalse(noQuote.isQuoteCharacterSet());

        CSVFormat charQuote = format.withQuote('~');
        assertEquals(Character.valueOf('~'), charQuote.getQuoteCharacter());
    }

    @Test(timeout = 4000)
    public void testWithQuoteMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
    }

    @Test(timeout = 4000)
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertTrue(format.isCommentMarkerSet());

        CSVFormat noComment = format.withCommentMarker((Character) null);
        assertNull(noComment.getCommentMarker());
        assertFalse(noComment.isCommentMarkerSet());

        CSVFormat charComment = format.withCommentMarker('/');
        assertEquals(Character.valueOf('/'), charComment.getCommentMarker());
    }

    @Test(timeout = 4000)
    public void testWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.isEscapeCharacterSet());

        CSVFormat noEscape = format.withEscape((Character) null);
        assertNull(noEscape.getEscapeCharacter());
        assertFalse(noEscape.isEscapeCharacterSet());

        CSVFormat charEscape = format.withEscape('^');
        assertEquals(Character.valueOf('^'), charEscape.getEscapeCharacter());
    }

    @Test(timeout = 4000)
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
        assertTrue(format.isNullStringSet());

        CSVFormat cleared = format.withNullString(null);
        assertNull(cleared.getNullString());
        assertFalse(cleared.isNullStringSet());
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparator() {
        CSVFormat formatChar = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", formatChar.getRecordSeparator());

        CSVFormat formatStr = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        assertEquals("\r\n", formatStr.getRecordSeparator());

        CSVFormat formatNull = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        assertNull(formatNull.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithBooleans() {
        CSVFormat f1 = CSVFormat.DEFAULT.withIgnoreEmptyLines().withIgnoreEmptyLines(false);
        assertFalse(f1.getIgnoreEmptyLines());
        f1 = f1.withIgnoreEmptyLines(true);
        assertTrue(f1.getIgnoreEmptyLines());

        CSVFormat f2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces().withIgnoreSurroundingSpaces(false);
        assertFalse(f2.getIgnoreSurroundingSpaces());
        f2 = f2.withIgnoreSurroundingSpaces(true);
        assertTrue(f2.getIgnoreSurroundingSpaces());

        CSVFormat f3 = CSVFormat.DEFAULT.withAllowMissingColumnNames().withAllowMissingColumnNames(false);
        assertFalse(f3.getAllowMissingColumnNames());
        f3 = f3.withAllowMissingColumnNames(true);
        assertTrue(f3.getAllowMissingColumnNames());

        CSVFormat f4 = CSVFormat.DEFAULT.withIgnoreHeaderCase().withIgnoreHeaderCase(false);
        assertFalse(f4.getIgnoreHeaderCase());
        f4 = f4.withIgnoreHeaderCase(true);
        assertTrue(f4.getIgnoreHeaderCase());

        CSVFormat f5 = CSVFormat.DEFAULT.withSkipHeaderRecord().withSkipHeaderRecord(false);
        assertFalse(f5.getSkipHeaderRecord());
        f5 = f5.withSkipHeaderRecord(true);
        assertTrue(f5.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithHeaderArray() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        assertArrayEquals(new String[]{"A", "B", "C"}, format.getHeader());

        // Ensure defensive copy
        String[] header = format.getHeader();
        header[0] = "MODIFIED";
        assertEquals("A", format.getHeader()[0]);

        CSVFormat emptyHeader = CSVFormat.DEFAULT.withHeader(new String[0]);
        assertArrayEquals(new String[0], emptyHeader.getHeader());

        CSVFormat nullHeader = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(nullHeader.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", 123, null);
        assertArrayEquals(new String[]{"Comment 1", "123", null}, format.getHeaderComments());

        String[] comments = format.getHeaderComments();
        comments[0] = "MODIFIED";
        assertEquals("Comment 1", format.getHeaderComments()[0]);

        CSVFormat nullComments = CSVFormat.DEFAULT.withHeaderComments((Object[]) null);
        assertNull(nullComments.getHeaderComments());
    }

    @Test(timeout = 4000)
    public void testWithHeaderResultSetMetaData() throws SQLException {
        InvocationHandler metaHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("getColumnCount".equals(method.getName())) {
                    return 2;
                }
                if ("getColumnLabel".equals(method.getName())) {
                    int index = (Integer) args[0];
                    return "Column" + index;
                }
                return null;
            }
        };

        ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                metaHandler
        );

        CSVFormat format = CSVFormat.DEFAULT.withHeader(metaData);
        assertArrayEquals(new String[]{"Column1", "Column2"}, format.getHeader());

        CSVFormat nullMetaFormat = CSVFormat.DEFAULT.withHeader((ResultSetMetaData) null);
        assertNull(nullMetaFormat.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithHeaderResultSet() throws SQLException {
        final InvocationHandler metaHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("getColumnCount".equals(method.getName())) {
                    return 1;
                }
                if ("getColumnLabel".equals(method.getName())) {
                    return "ColFromRS";
                }
                return null;
            }
        };

        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                metaHandler
        );

        InvocationHandler rsHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("getMetaData".equals(method.getName())) {
                    return metaData;
                }
                return null;
            }
        };

        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class},
                rsHandler
        );

        CSVFormat format = CSVFormat.DEFAULT.withHeader(rs);
        assertArrayEquals(new String[]{"ColFromRS"}, format.getHeader());

        CSVFormat nullRsFormat = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(nullRsFormat.getHeader());
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS (validate())
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDelimiterCannotBeLineFeed() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDelimiterCannotBeCarriageReturn() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNewFormatDelimiterCannotBeLineFeed() {
        CSVFormat.newFormat('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteCannotBeLineBreak() {
        CSVFormat.DEFAULT.withQuote('\r');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteCharacterObjectCannotBeLineBreak() {
        CSVFormat.DEFAULT.withQuote(Character.valueOf('\n'));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCommentMarkerCannotBeLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCommentMarkerObjectCannotBeLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker(Character.valueOf('\r'));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeCannotBeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeObjectCannotBeLineBreak() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf('\r'));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteCannotBeSameAsDelimiter() {
        CSVFormat.DEFAULT.withQuote(',');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeCannotBeSameAsDelimiter() {
        CSVFormat.DEFAULT.withEscape(',');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCommentMarkerCannotBeSameAsDelimiter() {
        CSVFormat.DEFAULT.withCommentMarker(',');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteCannotBeSameAsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('#').withQuote('#');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeCannotBeSameAsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('#').withEscape('#');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteModeNoneWithoutEscapeThrows() {
        CSVFormat.DEFAULT.withEscape((Character) null).withQuoteMode(QuoteMode.NONE);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDuplicateHeaderThrows() {
        CSVFormat.DEFAULT.withHeader("Header1", "Header2", "Header1");
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE, EQUALS, HASHCODE & TOSTRING
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;

        // Reflexivity & null/type checks
        assertTrue(f1.equals(f1));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Not a CSVFormat"));

        // Symmetry
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());

        // Field differences
        assertNotEquals(f1, f1.withDelimiter(';'));
        assertNotEquals(f1, f1.withQuote('\''));
        assertNotEquals(f1, f1.withQuoteMode(QuoteMode.ALL));
        assertNotEquals(f1, f1.withCommentMarker('#'));
        assertNotEquals(f1, f1.withEscape('\\'));
        assertNotEquals(f1, f1.withNullString("NULL"));
        assertNotEquals(f1, f1.withIgnoreSurroundingSpaces(!f1.getIgnoreSurroundingSpaces()));
        assertNotEquals(f1, f1.withIgnoreEmptyLines(!f1.getIgnoreEmptyLines()));
        assertNotEquals(f1, f1.withSkipHeaderRecord(!f1.getSkipHeaderRecord()));
        assertNotEquals(f1, f1.withRecordSeparator("\n"));
        assertNotEquals(f1, f1.withHeader("H1", "H2"));

        // Null vs Non-null checks in equals
        CSVFormat withQuote = f1.withQuote('"');
        CSVFormat withoutQuote = f1.withQuote((Character) null);
        assertNotEquals(withQuote, withoutQuote);
        assertNotEquals(withoutQuote, withQuote);

        CSVFormat withComment = f1.withCommentMarker('#');
        CSVFormat withoutComment = f1.withCommentMarker((Character) null);
        assertNotEquals(withComment, withoutComment);
        assertNotEquals(withoutComment, withComment);

        CSVFormat withEscape = f1.withEscape('\\');
        CSVFormat withoutEscape = f1.withEscape((Character) null);
        assertNotEquals(withEscape, withoutEscape);
        assertNotEquals(withoutEscape, withEscape);

        CSVFormat withNull = f1.withNullString("N");
        CSVFormat withoutNull = f1.withNullString(null);
        assertNotEquals(withNull, withoutNull);
        assertNotEquals(withoutNull, withNull);

        CSVFormat withRecSep = f1.withRecordSeparator("\r\n");
        CSVFormat withoutRecSep = f1.withRecordSeparator((String) null);
        assertNotEquals(withRecSep, withoutRecSep);
        assertNotEquals(withoutRecSep, withRecSep);
    }

    @Test(timeout = 4000)
    public void testToStringFullCoverage() {
        CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withQuote('"')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withRecordSeparator("\r\n")
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreHeaderCase(true)
                .withSkipHeaderRecord(true)
                .withHeaderComments("CommentLine")
                .withHeader("A", "B");

        String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("IgnoreHeaderCase:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:true"));
        assertTrue(str.contains("HeaderComments:[CommentLine]"));
        assertTrue(str.contains("Header:[A, B]"));

        // Minimal toString coverage
        CSVFormat minimal = CSVFormat.newFormat(';').withRecordSeparator((String) null);
        String minStr = minimal.toString();
        assertTrue(minStr.contains("Delimiter=<;>"));
        assertFalse(minStr.contains("Escape=<"));
        assertFalse(minStr.contains("QuoteChar=<"));
        assertFalse(minStr.contains("CommentStart=<"));
        assertFalse(minStr.contains("NullString=<"));
        assertFalse(minStr.contains("RecordSeparator=<"));
        assertFalse(minStr.contains("EmptyLines:ignored"));
        assertFalse(minStr.contains("SurroundingSpaces:ignored"));
        assertFalse(minStr.contains("IgnoreHeaderCase:ignored"));
        assertTrue(minStr.contains("SkipHeaderRecord:false"));
        assertFalse(minStr.contains("HeaderComments:"));
        assertFalse(minStr.contains("Header:"));
    }
}