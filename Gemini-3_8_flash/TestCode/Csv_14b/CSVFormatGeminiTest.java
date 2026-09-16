package org.apache.commons.csv;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.csv.CSVFormat
 *
 * 1. DEFECT-TARGETED BRANCH ZONE:
 *    - Defects4J CSV Defect: Quoting/Escaping regression in printAndQuote() where non-alphanumeric
 *      leading characters (e.g. backslash '\\') or escaped values without quotes are erroneously
 *      encapsulated in quotes under QuoteMode.MINIMAL or QuoteMode.NONE / withEscape().
 *      Specifically, testEscapeBackslash1, testEscapeBackslash4, testEscapeNull1, etc.
 *      Trigger: format withQuote('\'').withEscape('\\') or withQuote(null).withEscape('\\') printing
 *      "\\" or containing backslashes. Expected unquoted or correctly escaped output without unwanted quotes.
 *
 * 2. CORE FUNCTIONAL LOGIC & STATE TRANSITIONS:
 *    - Predefined formats: DEFAULT, EXCEL, INFORMIX_UNLOAD, INFORMIX_UNLOAD_CSV, MYSQL, RFC4180, TDF.
 *    - Predefined enum: getFormat(), CSVFormat.valueOf(String).
 *    - Factory methods: CSVFormat.newFormat(char).
 *    - Immutability and Fluent API chaining: with* methods producing independent configurations.
 *    - Header configurations: withHeader(String...), withHeader(Class<? extends Enum<?>>),
 *      withHeader(ResultSet), withHeader(ResultSetMetaData), withFirstRecordAsHeader().
 *
 * 3. BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES:
 *    - Line breaks (CR, LF, CRLF).
 *    - Empty arrays, null arguments, single characters, trailing delimiters.
 *    - Trimming of String vs CharSequence implementations with spaces at boundaries.
 *    - Quote modes: ALL, MINIMAL, NON_NUMERIC, NONE.
 *    - Number vs non-Number under QuoteMode.NON_NUMERIC.
 *
 * 4. EXCEPTION & DEFENSIVE GUARD PATHS:
 *    - Delimiter cannot be CR or LF.
 *    - Delimiter equals quoteChar, escapeChar, or commentMarker.
 *    - QuoteChar equals commentMarker.
 *    - EscapeChar equals commentMarker.
 *    - QuoteMode.NONE without escape character.
 *    - Duplicate header names validation.
 *    - Line break characters passed to withCommentMarker, withEscape, withQuote, withDelimiter.
 *
 * 5. OBJECT LIFECYCLE & CONTRACT INTEGRITY:
 *    - equals() / hashCode() symmetry, reflexivity, and sensitivity across all 16 state fields.
 *    - toString() formatting and completeness across configurations.
 */
public class CSVFormatGeminiTest {

    private enum TestHeaderEnum {
        ID, NAME, EMAIL
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectEscapeBackslashSingleChar() throws IOException {
        // Targets: testEscapeBackslash1 (expected:<[\]> but was:<['\']>)
        final CSVFormat format = CSVFormat.DEFAULT.withQuote('\'').withEscape('\\');
        final StringWriter sw = new StringWriter();
        format.print("\\", sw, true);
        assertEquals("\\", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefectEscapeBackslashDoubleChar() throws IOException {
        // Targets: testEscapeBackslash4/5 (expected:<[\\]> but was:<['\\']>)
        final CSVFormat format = CSVFormat.DEFAULT.withQuote('\'').withEscape('\\');
        final StringWriter sw = new StringWriter();
        format.print("\\\\", sw, true);
        assertEquals("\\\\", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefectEscapeNullSingleBackslash() throws IOException {
        // Targets: testEscapeNull1 (expected:<[\]> but was:<["\"]>)
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        final StringWriter sw = new StringWriter();
        format.print("\\", sw, true);
        assertEquals("\\", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefectEscapeNullDoubleBackslash() throws IOException {
        // Targets: testEscapeNull4/5 (expected:<[\\]> but was:<["\\"]>)
        final CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        final StringWriter sw = new StringWriter();
        format.print("\\\\", sw, true);
        assertEquals("\\\\", sw.toString());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPredefinedFormats() {
        assertNotNull(CSVFormat.DEFAULT);
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());

        assertNotNull(CSVFormat.EXCEL);
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());
        assertTrue(CSVFormat.EXCEL.getAllowMissingColumnNames());

        assertNotNull(CSVFormat.INFORMIX_UNLOAD);
        assertEquals('|', CSVFormat.INFORMIX_UNLOAD.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.INFORMIX_UNLOAD.getEscapeCharacter());
        assertEquals("\n", CSVFormat.INFORMIX_UNLOAD.getRecordSeparator());

        assertNotNull(CSVFormat.INFORMIX_UNLOAD_CSV);
        assertEquals(',', CSVFormat.INFORMIX_UNLOAD_CSV.getDelimiter());
        assertEquals("\n", CSVFormat.INFORMIX_UNLOAD_CSV.getRecordSeparator());

        assertNotNull(CSVFormat.MYSQL);
        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());

        assertNotNull(CSVFormat.RFC4180);
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());

        assertNotNull(CSVFormat.TDF);
        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testPredefinedEnumAndValueOf() {
        for (final CSVFormat.Predefined predefined : CSVFormat.Predefined.values()) {
            assertNotNull(predefined.getFormat());
            final CSVFormat format = CSVFormat.valueOf(predefined.name());
            assertEquals(predefined.getFormat(), format);
        }
    }

    @Test(timeout = 4000)
    public void testNewFormatFactory() {
        final CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertFalse(format.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithMethodsFluentChaining() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuote('\'')
                .withQuoteMode(QuoteMode.ALL)
                .withCommentMarker('#')
                .withEscape('\\')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\n")
                .withNullString("NULL")
                .withHeader("A", "B")
                .withHeaderComments("Comment1", "Comment2")
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true)
                .withIgnoreHeaderCase(true)
                .withTrim(true)
                .withTrailingDelimiter(true);

        assertEquals(';', format.getDelimiter());
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("NULL", format.getNullString());
        assertArrayEquals(new String[]{"A", "B"}, format.getHeader());
        assertArrayEquals(new String[]{"Comment1", "Comment2"}, format.getHeaderComments());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getTrim());
        assertTrue(format.getTrailingDelimiter());

        assertTrue(format.isCommentMarkerSet());
        assertTrue(format.isEscapeCharacterSet());
        assertTrue(format.isNullStringSet());
        assertTrue(format.isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testWithMethodsNoArgVariants() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withAllowMissingColumnNames()
                .withIgnoreEmptyLines()
                .withIgnoreHeaderCase()
                .withIgnoreSurroundingSpaces()
                .withSkipHeaderRecord()
                .withTrailingDelimiter()
                .withTrim();

        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getIgnoreEmptyLines());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getTrailingDelimiter());
        assertTrue(format.getTrim());
    }

    @Test(timeout = 4000)
    public void testWithHeaderEnum() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(TestHeaderEnum.class);
        assertArrayEquals(new String[]{"ID", "NAME", "EMAIL"}, format.getHeader());

        final CSVFormat formatNullEnum = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(formatNullEnum.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithFirstRecordAsHeader() {
        final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertNotNull(format.getHeader());
        assertEquals(0, format.getHeader().length);
        assertTrue(format.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithHeaderResultSetMetaData() throws SQLException {
        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ResultSetMetaData.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) {
                        if ("getColumnCount".equals(method.getName())) {
                            return 2;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            final int idx = (Integer) args[0];
                            return "COL_" + idx;
                        }
                        return null;
                    }
                }
        );

        final CSVFormat format = CSVFormat.DEFAULT.withHeader(metaData);
        assertArrayEquals(new String[]{"COL_1", "COL_2"}, format.getHeader());

        final CSVFormat formatNullMeta = CSVFormat.DEFAULT.withHeader((ResultSetMetaData) null);
        assertNull(formatNullMeta.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithHeaderResultSet() throws SQLException {
        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ResultSetMetaData.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) {
                        if ("getColumnCount".equals(method.getName())) {
                            return 1;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            return "ID";
                        }
                        return null;
                    }
                }
        );

        final ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ResultSet.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) {
                        if ("getMetaData".equals(method.getName())) {
                            return metaData;
                        }
                        return null;
                    }
                }
        );

        final CSVFormat format = CSVFormat.DEFAULT.withHeader(rs);
        assertArrayEquals(new String[]{"ID"}, format.getHeader());

        final CSVFormat formatNullRs = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(formatNullRs.getHeader());
    }

    @Test(timeout = 4000)
    public void testFormatVarArgs() {
        final CSVFormat format = CSVFormat.DEFAULT;
        final String result = format.format("Foo", "Bar", 123);
        assertEquals("Foo,Bar,123", result);
    }

    @Test(timeout = 4000)
    public void testParseReader() throws IOException {
        final Reader reader = new StringReader("A,B,C\n1,2,3");
        final CSVParser parser = CSVFormat.DEFAULT.parse(reader);
        assertNotNull(parser);
        assertEquals(2, parser.getRecords().size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Quote/Escape Output Processing
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintNullValues() throws IOException {
        final StringWriter out = new StringWriter();
        CSVFormat.DEFAULT.print(null, out, true);
        assertEquals("", out.toString());

        final StringWriter outNullStr = new StringWriter();
        CSVFormat.DEFAULT.withNullString("NULL").print(null, outNullStr, true);
        assertEquals("NULL", outNullStr.toString());

        final StringWriter outNotNew = new StringWriter();
        CSVFormat.DEFAULT.withNullString("NULL").print(null, outNotNew, false);
        assertEquals(",NULL", outNotNew.toString());
    }

    @Test(timeout = 4000)
    public void testPrintTrimCharSequence() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withTrim();
        final StringWriter out = new StringWriter();
        final StringBuilder sb = new StringBuilder("  trimmed value  ");
        format.print(sb, out, true);
        assertEquals("trimmed value", out.toString());

        final StringWriter outString = new StringWriter();
        format.print("   string trimmed   ", outString, true);
        assertEquals("string trimmed", outString.toString());
    }

    @Test(timeout = 4000)
    public void testPrintQuoteModeAll() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        final StringWriter out = new StringWriter();
        format.print("hello", out, true);
        format.print(123, out, false);
        assertEquals("\"hello\",\"123\"", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintQuoteModeNonNumeric() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        final StringWriter out = new StringWriter();
        format.print("text", out, true);
        format.print(456, out, false);
        format.print(78.9, out, false);
        assertEquals("\"text\",456,78.9", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintQuoteModeNoneWithEscape() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape('\\');
        final StringWriter out = new StringWriter();
        format.print("hello,world\nnext\rline\\delim", out, true);
        assertEquals("hello\\,world\\nnext\\rline\\\\delim", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintMinimalQuoteTriggers() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;

        // Empty token on newRecord
        final StringWriter outEmptyFirst = new StringWriter();
        format.print("", outEmptyFirst, true);
        assertEquals("\"\"", outEmptyFirst.toString());

        // Empty token NOT on newRecord
        final StringWriter outEmptySecond = new StringWriter();
        format.print("", outEmptySecond, false);
        assertEquals(",", outEmptySecond.toString());

        // Contains delimiter
        final StringWriter outDelim = new StringWriter();
        format.print("a,b", outDelim, true);
        assertEquals("\"a,b\"", outDelim.toString());

        // Contains LF and CR
        final StringWriter outLf = new StringWriter();
        format.print("a\nb", outLf, true);
        assertEquals("\"a\nb\"", outLf.toString());

        final StringWriter outCr = new StringWriter();
        format.print("a\rb", outCr, true);
        assertEquals("\"a\rb\"", outCr.toString());

        // Contains embedded quoteChar (should be doubled)
        final StringWriter outQuote = new StringWriter();
        format.print("a\"b", outQuote, true);
        assertEquals("\"a\"\"b\"", outQuote.toString());

        // Token ending with space
        final StringWriter outEndSpace = new StringWriter();
        format.print("abc ", outEndSpace, true);
        assertEquals("\"abc \"", outEndSpace.toString());

        // Token starting with comment char (default '#')
        final CSVFormat formatComment = CSVFormat.DEFAULT.withCommentMarker('#');
        final StringWriter outComment = new StringWriter();
        formatComment.print("#comment", outComment, true);
        assertEquals("\"#comment\"", outComment.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithoutQuoteCharacterUsingEscape() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withQuote(null).withEscape('\\');
        final StringWriter out = new StringWriter();
        format.print("comma,cr\rlf\nescape\\ok", out, true);
        assertEquals("comma\\,cr\\rlf\\nescape\\\\ok", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWithoutQuoteAndWithoutEscape() throws IOException {
        final CSVFormat format = CSVFormat.newFormat('|');
        final StringWriter out = new StringWriter();
        format.print("plain", out, true);
        format.print("text", out, false);
        assertEquals("plain|text", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintlnAndTrailingDelimiter() throws IOException {
        final CSVFormat formatNoTrailing = CSVFormat.DEFAULT;
        final StringWriter out1 = new StringWriter();
        formatNoTrailing.println(out1);
        assertEquals("\r\n", out1.toString());

        final CSVFormat formatTrailing = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        final StringWriter out2 = new StringWriter();
        formatTrailing.println(out2);
        assertEquals(",\r\n", out2.toString());

        final CSVFormat formatNoSeparator = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        final StringWriter out3 = new StringWriter();
        formatNoSeparator.println(out3);
        assertEquals("", out3.toString());
    }

    @Test(timeout = 4000)
    public void testPrintRecord() throws IOException {
        final StringWriter out = new StringWriter();
        CSVFormat.DEFAULT.printRecord(out, "col1", "col2", "col3");
        assertEquals("col1,col2,col3\r\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testPrintToAppendableAndFileAndPath() throws IOException {
        final StringWriter sw = new StringWriter();
        try (final CSVPrinter printer = CSVFormat.DEFAULT.print(sw)) {
            printer.print("v1");
        }
        assertEquals("v1", sw.toString());

        final File tempFile = File.createTempFile("csv_test_", ".csv");
        try {
            try (final CSVPrinter printer = CSVFormat.DEFAULT.print(tempFile, StandardCharsets.UTF_8)) {
                printer.print("v2");
            }
            final String content = new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8);
            assertEquals("v2", content);

            final Path tempPath = tempFile.toPath();
            try (final CSVPrinter printer = CSVFormat.DEFAULT.print(tempPath, StandardCharsets.UTF_8)) {
                printer.print("v3");
            }
            final String contentPath = new String(Files.readAllBytes(tempPath), StandardCharsets.UTF_8);
            assertEquals("v3", contentPath);
        } finally {
            tempFile.delete();
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateDelimiterLineBreakCr() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateDelimiterLineBreakLf() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateNewFormatLineBreak() {
        CSVFormat.newFormat('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateDelimiterEqualsQuote() {
        CSVFormat.DEFAULT.withDelimiter('"');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateDelimiterEqualsEscape() {
        CSVFormat.DEFAULT.withEscape('!').withDelimiter('!');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateDelimiterEqualsComment() {
        CSVFormat.DEFAULT.withCommentMarker('#').withDelimiter('#');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateQuoteEqualsComment() {
        CSVFormat.DEFAULT.withCommentMarker('#').withQuote('#');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateEscapeEqualsComment() {
        CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateQuoteModeNoneWithoutEscape() {
        CSVFormat.newFormat(',').withQuoteMode(QuoteMode.NONE);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValidateDuplicateHeaderEntries() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithCommentMarkerCharLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithEscapeCharLineBreak() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithQuoteLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithQuoteCharLineBreak() {
        CSVFormat.DEFAULT.withQuote('\r');
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        final CSVFormat f1 = CSVFormat.DEFAULT
                .withDelimiter(',')
                .withQuote('"')
                .withRecordSeparator("\r\n")
                .withHeader("H1", "H2");

        final CSVFormat f2 = CSVFormat.DEFAULT
                .withDelimiter(',')
                .withQuote('"')
                .withRecordSeparator("\r\n")
                .withHeader("H1", "H2");

        // Reflexive
        assertEquals(f1, f1);
        // Symmetric
        assertEquals(f1, f2);
        assertEquals(f2, f1);
        assertEquals(f1.hashCode(), f2.hashCode());

        // Null and alien object checks
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("alien"));

        // Field differences
        assertNotEquals(f1, f1.withDelimiter(';'));
        assertNotEquals(f1, f1.withQuote('\''));
        assertNotEquals(f1, f1.withQuote(null));
        assertNotEquals(f1, f1.withQuoteMode(QuoteMode.ALL));
        assertNotEquals(f1, f1.withCommentMarker('#'));
        assertNotEquals(f1, f1.withEscape('\\'));
        assertNotEquals(f1, f1.withNullString("NULL"));
        assertNotEquals(f1, f1.withIgnoreSurroundingSpaces(true));
        assertNotEquals(f1, f1.withIgnoreEmptyLines(false));
        assertNotEquals(f1, f1.withSkipHeaderRecord(true));
        assertNotEquals(f1, f1.withRecordSeparator("\n"));
        assertNotEquals(f1, f1.withRecordSeparator((String) null));
        assertNotEquals(f1, f1.withHeader("Different"));
        assertNotEquals(f1, f1.withHeader((String[]) null));
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withIgnoreHeaderCase(true)
                .withHeaderComments("Comment")
                .withHeader("Col1");

        final String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("IgnoreHeaderCase:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:false"));
        assertTrue(str.contains("HeaderComments:[Comment]"));
        assertTrue(str.contains("Header:[Col1]"));
    }

    @Test(timeout = 4000)
    public void testHeaderCloningDefensiveCopy() {
        final String[] header = new String[]{"C1", "C2"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        header[0] = "MUTATED";
        assertEquals("C1", format.getHeader()[0]);

        final String[] retrievedHeader = format.getHeader();
        retrievedHeader[0] = "MUTATED_AGAIN";
        assertEquals("C1", format.getHeader()[0]);
    }

    @Test(timeout = 4000)
    public void testHeaderCommentsCloningDefensiveCopy() {
        final Object[] comments = new Object[]{"Comm1", "Comm2"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        comments[0] = "MUTATED";
        assertEquals("Comm1", format.getHeaderComments()[0]);

        final String[] retrievedComments = format.getHeaderComments();
        retrievedComments[0] = "MUTATED_AGAIN";
        assertEquals("Comm1", format.getHeaderComments()[0]);
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparatorChar() {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator());
    }
}