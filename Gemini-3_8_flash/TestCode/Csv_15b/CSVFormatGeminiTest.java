/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.csv.CSVFormat
 *
 * 1. DEFECT UNDER TEST (Defects4J - CSV-203 / testDontQuoteEuroFirstChar):
 *    - In printAndQuote (MINIMAL mode), condition `c > 0x7E` incorrectly treated
 *      characters above 0x7E (such as Euro symbol '\u20AC') as needing quotes.
 *    - Target Method: format(Object... values) / print(Object, Appendable, boolean)
 *    - Branch: MINIMAL quote mode -> c < 0x20 || ... || c > 0x7E
 *    - Revelation: assertEquals("€,Deux", CSVFormat.RFC4180.format("€", "Deux"));
 *
 * 2. DECISION / CONDITION COVERAGE MATRIX:
 *    - validate():
 *      - isLineBreak(delimiter) -> CR, LF
 *      - quoteChar == delimiter
 *      - escape == delimiter
 *      - commentMarker == delimiter
 *      - quoteChar == commentMarker
 *      - escape == commentMarker
 *      - escape == null && quoteMode == QuoteMode.NONE
 *      - header duplicate check
 *    - printAndQuote():
 *      - quoteMode: ALL, ALL_NON_NULL, NON_NUMERIC (Number vs non-Number), NONE, MINIMAL
 *      - MINIMAL: len == 0 (newRecord vs not newRecord), len > 0 (contains delim, quote, CR, LF, trailing space)
 *      - Quote character escaping (doubling up quote char)
 *    - printAndEscape():
 *      - Delimiter, escape, CR, LF escaping
 *    - print() with null value:
 *      - nullString == null -> EMPTY
 *      - QuoteMode.ALL with nullString
 *      - trim behavior on CharSequence / String
 *    - equals() & hashCode():
 *      - Reflexive, null, different class, each field diff (delimiter, quoteMode, quoteChar,
 *        commentMarker, escape, nullString, header, ignoreSurroundingSpaces, ignoreEmptyLines,
 *        skipHeaderRecord, recordSeparator)
 *    - Serialization & Deserialization:
 *      - Roundtrip verification maintaining state consistency
 */

package org.apache.commons.csv;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class CSVFormatGeminiTest {

    private enum TestEnumHeader {
        ID, NAME, EMAIL
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets defect in printAndQuote() where characters > 0x7E (e.g. Euro '€')
     * at the start of a record were erroneously quoted in MINIMAL mode.
     */
    @Test(timeout = 4000)
    public void testDontQuoteEuroFirstChar() {
        final CSVFormat format = CSVFormat.RFC4180;
        final String formatted = format.format("€", "Deux");
        assertEquals("€,Deux", formatted);
    }

    @Test(timeout = 4000)
    public void testDontQuoteUnicodeFirstCharInMinimal() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;
        final StringWriter out = new StringWriter();
        format.printRecord(out, "日本語", "Data");
        assertEquals("日本語,Data\r\n", out.toString());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPredefinedFormatsIntegrity() {
        for (final CSVFormat.Predefined predefined : CSVFormat.Predefined.values()) {
            assertNotNull(predefined.getFormat());
            assertSame(predefined.getFormat(), CSVFormat.valueOf(predefined.name()));
        }

        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());

        assertTrue(CSVFormat.EXCEL.getAllowMissingColumnNames());
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());

        assertEquals('|', CSVFormat.INFORMIX_UNLOAD.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.INFORMIX_UNLOAD.getEscapeCharacter());
        assertEquals("\n", CSVFormat.INFORMIX_UNLOAD.getRecordSeparator());

        assertEquals(',', CSVFormat.INFORMIX_UNLOAD_CSV.getDelimiter());
        assertNull(CSVFormat.INFORMIX_UNLOAD_CSV.getEscapeCharacter());

        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
        assertEquals(QuoteMode.ALL_NON_NULL, CSVFormat.MYSQL.getQuoteMode());

        assertEquals(',', CSVFormat.POSTGRESQL_CSV.getDelimiter());
        assertEquals("", CSVFormat.POSTGRESQL_CSV.getNullString());

        assertEquals('\t', CSVFormat.POSTGRESQL_TEXT.getDelimiter());
        assertEquals("\\N", CSVFormat.POSTGRESQL_TEXT.getNullString());

        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testWithMethodsFluentChain() {
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
                .withHeader("H1", "H2")
                .withHeaderComments("Comment1", "Comment2")
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true)
                .withIgnoreHeaderCase(true)
                .withTrim(true)
                .withTrailingDelimiter(true)
                .withAutoFlush(true);

        assertEquals(';', format.getDelimiter());
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("NULL", format.getNullString());
        assertArrayEquals(new String[]{"H1", "H2"}, format.getHeader());
        assertArrayEquals(new String[]{"Comment1", "Comment2"}, format.getHeaderComments());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getIgnoreHeaderCase());
        assertTrue(format.getTrim());
        assertTrue(format.getTrailingDelimiter());
        assertTrue(format.getAutoFlush());

        assertTrue(format.isCommentMarkerSet());
        assertTrue(format.isEscapeCharacterSet());
        assertTrue(format.isNullStringSet());
        assertTrue(format.isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testWithHeaderEnum() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(TestEnumHeader.class);
        assertArrayEquals(new String[]{"ID", "NAME", "EMAIL"}, format.getHeader());

        final CSVFormat formatNull = CSVFormat.DEFAULT.withHeader((Class<? extends Enum<?>>) null);
        assertNull(formatNull.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithFirstRecordAsHeader() {
        final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertNotNull(format.getHeader());
        assertEquals(0, format.getHeader().length);
        assertTrue(format.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithResultSetAndMetaData() throws SQLException {
        final ResultSetMetaData metaData = (ResultSetMetaData) Proxy.newProxyInstance(
                ResultSetMetaData.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) {
                        if ("getColumnCount".equals(method.getName())) {
                            return 2;
                        }
                        if ("getColumnLabel".equals(method.getName())) {
                            final int idx = (Integer) args[0];
                            return idx == 1 ? "ColA" : "ColB";
                        }
                        return null;
                    }
                }
        );

        final ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
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

        final CSVFormat formatFromMeta = CSVFormat.DEFAULT.withHeader(metaData);
        assertArrayEquals(new String[]{"ColA", "ColB"}, formatFromMeta.getHeader());

        final CSVFormat formatFromRS = CSVFormat.DEFAULT.withHeader(resultSet);
        assertArrayEquals(new String[]{"ColA", "ColB"}, formatFromRS.getHeader());

        final CSVFormat formatFromNullRS = CSVFormat.DEFAULT.withHeader((ResultSet) null);
        assertNull(formatFromNullRS.getHeader());

        final CSVFormat formatFromNullMeta = CSVFormat.DEFAULT.withHeader((ResultSetMetaData) null);
        assertNull(formatFromNullMeta.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparatorChar() {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testParameterlessWithMethods() {
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

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNewFormatMinimalSettings() {
        final CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertNull(format.getHeaderComments());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
        assertFalse(format.getTrim());
        assertFalse(format.getTrailingDelimiter());
        assertFalse(format.getAutoFlush());
        assertFalse(format.isCommentMarkerSet());
        assertFalse(format.isEscapeCharacterSet());
        assertFalse(format.isNullStringSet());
        assertFalse(format.isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testPrintNullValuesWithVariousModes() throws IOException {
        final CSVFormat formatAll = CSVFormat.DEFAULT
                .withQuoteMode(QuoteMode.ALL)
                .withNullString("NULL");
        assertEquals("\"NULL\",b", formatAll.format(null, "b"));

        final CSVFormat formatMinimal = CSVFormat.DEFAULT
                .withNullString("NULL");
        assertEquals("NULL,b", formatMinimal.format(null, "b"));

        final CSVFormat formatNoNullString = CSVFormat.DEFAULT;
        assertEquals(",b", formatNoNullString.format(null, "b"));
    }

    @Test(timeout = 4000)
    public void testQuoteModeAllNonNull() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withQuoteMode(QuoteMode.ALL_NON_NULL)
                .withNullString("NULL");
        assertEquals("NULL,\"value\"", format.format(null, "value"));
    }

    @Test(timeout = 4000)
    public void testQuoteModeNonNumeric() {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        assertEquals("\"text\",123,45.67", format.format("text", 123, 45.67));
    }

    @Test(timeout = 4000)
    public void testQuoteModeNoneWithEscape() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withQuoteMode(QuoteMode.NONE)
                .withQuote(null)
                .withEscape('\\');
        assertEquals("a\\,b,c", format.format("a,b", "c"));
    }

    @Test(timeout = 4000)
    public void testPrintAndEscapeSpecialChars() throws IOException {
        final CSVFormat format = CSVFormat.newFormat(',')
                .withEscape('\\');
        final StringWriter sw = new StringWriter();
        format.printRecord(sw, "hello,world", "line1\rline2\nline3", "with\\slash");
        assertEquals("hello\\,world,line1\\rline2\\nline3,with\\\\slash", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintEmptyRecordAndMinimalQuotes() {
        final CSVFormat format = CSVFormat.DEFAULT;
        assertEquals("\"\",b", format.format("", "b"));
        assertEquals("a,\"\"", format.format("a", ""));
    }

    @Test(timeout = 4000)
    public void testPrintWithEncapsulationAndQuoteDoubling() {
        final CSVFormat format = CSVFormat.DEFAULT;
        assertEquals("\"a\"\"b\"", format.format("a\"b"));
        assertEquals("\"a,b\"", format.format("a,b"));
        assertEquals("\"a\nb\"", format.format("a\nb"));
        assertEquals("\"a\rb\"", format.format("a\rb"));
        assertEquals("\"trailing \"", format.format("trailing "));
        assertEquals("\"#leadingHash\"", format.format("#leadingHash"));
    }

    @Test(timeout = 4000)
    public void testTrimBehaviorOnPrint() {
        final CSVFormat format = CSVFormat.DEFAULT.withTrim();
        assertEquals("trimmed,also", format.format("  trimmed  ", new StringBuilder(" also ")));
    }

    @Test(timeout = 4000)
    public void testTrailingDelimiterPrintln() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT
                .withTrailingDelimiter()
                .withRecordSeparator("\n");
        final StringWriter out = new StringWriter();
        format.printRecord(out, "a", "b");
        assertEquals("a,b,\n", out.toString());
    }

    @Test(timeout = 4000)
    public void testParseAndPrinterCreation() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT;
        final CSVParser parser = format.parse(new StringReader("a,b\nc,d"));
        assertNotNull(parser);
        parser.close();

        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = format.print(sw);
        assertNotNull(printer);
        printer.close();

        final CSVPrinter sysOutPrinter = format.printer();
        assertNotNull(sysOutPrinter);
    }

    @Test(timeout = 4000)
    public void testPrintToFileAndPath() throws IOException {
        final File tempFile = File.createTempFile("csv_test_", ".csv");
        tempFile.deleteOnExit();

        final CSVFormat format = CSVFormat.DEFAULT;
        try (final CSVPrinter printer = format.print(tempFile, StandardCharsets.UTF_8)) {
            printer.printRecord("fileCol1", "fileCol2");
        }
        assertTrue(tempFile.length() > 0);

        final Path tempPath = Files.createTempFile("csv_path_test_", ".csv");
        try (final CSVPrinter printer = format.print(tempPath, StandardCharsets.UTF_8)) {
            printer.printRecord("pathCol1", "pathCol2");
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDelimiterCannotBeLineBreakLF() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDelimiterCannotBeLineBreakCR() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNewFormatWithLineBreak() {
        CSVFormat.newFormat('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteCannotBeLineBreak() {
        CSVFormat.DEFAULT.withQuote('\r');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeCannotBeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCommentMarkerCannotBeLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteAndDelimiterCannotBeSame() {
        CSVFormat.DEFAULT.withQuote(';').withDelimiter(';');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeAndDelimiterCannotBeSame() {
        CSVFormat.DEFAULT.withEscape(';').withDelimiter(';');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCommentAndDelimiterCannotBeSame() {
        CSVFormat.DEFAULT.withCommentMarker(';').withDelimiter(';');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteAndCommentCannotBeSame() {
        CSVFormat.DEFAULT.withQuote('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeAndCommentCannotBeSame() {
        CSVFormat.DEFAULT.withEscape('!').withCommentMarker('!');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testQuoteModeNoneWithoutEscapeThrows() {
        CSVFormat.DEFAULT.withEscape(null).withQuoteMode(QuoteMode.NONE);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDuplicateHeaderEntriesThrow() {
        CSVFormat.DEFAULT.withHeader("ColA", "ColB", "ColA");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        final CSVFormat f1 = CSVFormat.DEFAULT;
        final CSVFormat f2 = CSVFormat.DEFAULT;
        final CSVFormat f3 = CSVFormat.DEFAULT.withDelimiter(';');

        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());

        assertNotEquals(f1, null);
        assertNotEquals(f1, "NotACSVFormat");
        assertNotEquals(f1, f3);

        assertNotEquals(f1, f1.withQuote('\''));
        assertNotEquals(f1, f1.withQuote(null));
        assertNotEquals(f1.withQuote(null), f1);
        assertNotEquals(f1, f1.withQuoteMode(QuoteMode.ALL));
        assertNotEquals(f1, f1.withCommentMarker('#'));
        assertNotEquals(f1.withCommentMarker('#'), f1);
        assertNotEquals(f1, f1.withEscape('\\'));
        assertNotEquals(f1.withEscape('\\'), f1);
        assertNotEquals(f1, f1.withNullString("NULL"));
        assertNotEquals(f1.withNullString("NULL"), f1);
        assertNotEquals(f1, f1.withHeader("A", "B"));
        assertNotEquals(f1, f1.withIgnoreSurroundingSpaces(!f1.getIgnoreSurroundingSpaces()));
        assertNotEquals(f1, f1.withIgnoreEmptyLines(!f1.getIgnoreEmptyLines()));
        assertNotEquals(f1, f1.withSkipHeaderRecord(!f1.getSkipHeaderRecord()));
        assertNotEquals(f1, f1.withRecordSeparator("\n"));
        assertNotEquals(f1, f1.withRecordSeparator((String) null));
        assertNotEquals(f1.withRecordSeparator((String) null), f1);
    }

    @Test(timeout = 4000)
    public void testToStringOutput() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("NULL")
                .withIgnoreHeaderCase(true)
                .withHeaderComments("TestComment")
                .withHeader("Col1", "Col2");

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
        assertTrue(str.contains("HeaderComments:[TestComment]"));
        assertTrue(str.contains("Header:[Col1, Col2]"));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
        final CSVFormat original = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuote('\'')
                .withEscape('\\')
                .withNullString("NULL")
                .withHeader("H1", "H2")
                .withHeaderComments("C1")
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withSkipHeaderRecord(true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final CSVFormat deserialized;
        try (final ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (CSVFormat) ois.readObject();
        }

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals(';', deserialized.getDelimiter());
        assertEquals(Character.valueOf('\''), deserialized.getQuoteCharacter());
        assertArrayEquals(new String[]{"H1", "H2"}, deserialized.getHeader());
        assertArrayEquals(new String[]{"C1"}, deserialized.getHeaderComments());
    }

    @Test(timeout = 4000)
    public void testHeaderImmutability() {
        final String[] header = new String[]{"A", "B", "C"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(header);
        header[0] = "MODIFIED";
        assertEquals("A", format.getHeader()[0]);

        final String[] returnedHeader = format.getHeader();
        returnedHeader[0] = "MODIFIED_AGAIN";
        assertEquals("A", format.getHeader()[0]);
    }

    @Test(timeout = 4000)
    public void testHeaderCommentsImmutability() {
        final Object[] comments = new Object[]{"C1", "C2"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeaderComments(comments);
        comments[0] = "MODIFIED";
        assertEquals("C1", format.getHeaderComments()[0]);

        final String[] returnedComments = format.getHeaderComments();
        returnedComments[0] = "MODIFIED_AGAIN";
        assertEquals("C1", format.getHeaderComments()[0]);
    }
}