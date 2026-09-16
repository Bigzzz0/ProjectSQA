package org.apache.commons.csv;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import static org.apache.commons.csv.Constants.BACKSLASH;
import static org.apache.commons.csv.Constants.COMMA;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.DOUBLE_QUOTE_CHAR;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.TAB;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.apache.commons.csv.CSVFormat
 *
 * Target Branches & Condition Coverage:
 * 1. Predefined Formats:
 *    - DEFAULT: (',', '"', null, null, null, false, true, "\r\n", null, null, false, false)
 *    - RFC4180: DEFAULT with ignoreEmptyLines = false
 *    - EXCEL: RFC4180 with allowMissingColumnNames = true (Known Defect: defective version missing allowMissingColumnNames=true)
 *    - TDF: TAB delimiter, ignoreSurroundingSpaces = true, quote = '"'
 *    - MYSQL: TAB delimiter, quote = null, escape = '\\', recordSeparator = "\n", ignoreEmptyLines = false
 * 2. Static Factories & Helpers:
 *    - isLineBreak(char) / isLineBreak(Character): LF ('\n'), CR ('\r'), null, and non-break chars
 *    - newFormat(char delimiter): creates pristine minimal format, checks line break rejection
 * 3. Constructor & Validation Branches:
 *    - Delimiter == CR / LF (throws IllegalArgumentException)
 *    - Delimiter == quoteCharacter (throws IllegalArgumentException)
 *    - Delimiter == escapeCharacter (throws IllegalArgumentException)
 *    - Delimiter == commentMarker (throws IllegalArgumentException)
 *    - QuoteCharacter == commentMarker (throws IllegalArgumentException)
 *    - EscapeCharacter == commentMarker (throws IllegalArgumentException)
 *    - QuoteMode.NONE when escapeCharacter == null (throws IllegalArgumentException)
 *    - Duplicate headers (throws IllegalArgumentException)
 * 4. With-Builders & Immutability:
 *    - withDelimiter, withQuote, withQuoteMode, withCommentMarker, withEscape
 *    - withIgnoreEmptyLines, withIgnoreSurroundingSpaces, withRecordSeparator, withNullString
 *    - withHeader, withSkipHeaderRecord, withAllowMissingColumnNames
 * 5. Formatting & Parsing:
 *    - format(Object... values) produces valid String output via CSVPrinter
 *    - parse(Reader) returns functional CSVParser
 *    - print(Appendable) returns functional CSVPrinter
 * 6. Object Contracts & Edge Cases:
 *    - equals (reflexive, symmetric, null, class-mismatch, and all 12 distinct field discrepancies)
 *    - hashCode (consistency with equals and null handling across fields)
 *    - toString (verifies inclusion/omission of optional parameters)
 *    - Serialization / Deserialization fidelity
 *
 * Ground Truth Defect Targeted:
 * - CSVParserTest::testExcelHeaderCountLessThanData ->
 *   CSVFormat.EXCEL was defined as DEFAULT.withIgnoreEmptyLines(false) rather than
 *   DEFAULT.withIgnoreEmptyLines(false).withAllowMissingColumnNames(true), failing to support
 *   missing column headers in Excel sheets.
 */
public class CSVFormatGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic, State Verification & Builder Flow
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultFormatConfiguration() {
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertNull(CSVFormat.DEFAULT.getQuoteMode());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertNull(CSVFormat.DEFAULT.getHeader());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.getAllowMissingColumnNames());

        assertTrue(CSVFormat.DEFAULT.isQuoteCharacterSet());
        assertFalse(CSVFormat.DEFAULT.isCommentMarkerSet());
        assertFalse(CSVFormat.DEFAULT.isEscapeCharacterSet());
        assertFalse(CSVFormat.DEFAULT.isNullStringSet());
    }

    @Test(timeout = 4000)
    public void testRfc4180FormatConfiguration() {
        assertEquals(',', CSVFormat.RFC4180.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.RFC4180.getQuoteCharacter());
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.RFC4180.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testTdfFormatConfiguration() {
        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.TDF.getQuoteCharacter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.TDF.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testMysqlFormatConfiguration() {
        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertNull(CSVFormat.MYSQL.getQuoteCharacter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscapeCharacter());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
        assertFalse(CSVFormat.MYSQL.getIgnoreEmptyLines());
        assertTrue(CSVFormat.MYSQL.isEscapeCharacterSet());
        assertFalse(CSVFormat.MYSQL.isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testNewFormatFactory() {
        final CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteCharacter());
        assertNull(format.getQuoteMode());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testWithMethodsImmutabilityAndChaining() {
        final CSVFormat base = CSVFormat.DEFAULT;
        final CSVFormat customized = base
                .withDelimiter(';')
                .withQuote('\'')
                .withQuoteMode(QuoteMode.ALL)
                .withCommentMarker('#')
                .withEscape('/')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\n")
                .withNullString("NULL")
                .withHeader("C1", "C2")
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true);

        // Ensure base is unaffected
        assertEquals(',', base.getDelimiter());
        assertFalse(base.getIgnoreSurroundingSpaces());

        // Validate customized
        assertEquals(';', customized.getDelimiter());
        assertEquals(Character.valueOf('\''), customized.getQuoteCharacter());
        assertEquals(QuoteMode.ALL, customized.getQuoteMode());
        assertEquals(Character.valueOf('#'), customized.getCommentMarker());
        assertEquals(Character.valueOf('/'), customized.getEscapeCharacter());
        assertTrue(customized.getIgnoreSurroundingSpaces());
        assertFalse(customized.getIgnoreEmptyLines());
        assertEquals("\n", customized.getRecordSeparator());
        assertEquals("NULL", customized.getNullString());
        assertArrayEquals(new String[]{"C1", "C2"}, customized.getHeader());
        assertTrue(customized.getSkipHeaderRecord());
        assertTrue(customized.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testFormatAndParseAndPrintOperations() throws Exception {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        final String formatted = format.format("foo", "bar,baz", 123);
        assertEquals("\"foo\",\"bar,baz\",123", formatted.trim().replaceAll("\r\n", ""));

        final CSVParser parser = format.parse(new StringReader("a,b\r\nc,d"));
        assertNotNull(parser);
        assertEquals(2, parser.getRecords().size());

        final StringWriter sw = new StringWriter();
        final CSVPrinter printer = format.print(sw);
        assertNotNull(printer);
        printer.printRecord("x", "y");
        assertEquals("x,y\r\n", sw.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testHeaderImmutabilityDefensiveCopy() {
        final String[] original = new String[]{"First", "Second"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(original);

        original[0] = "Mutated";
        assertArrayEquals(new String[]{"First", "Second"}, format.getHeader());

        final String[] retrieved = format.getHeader();
        retrieved[1] = "MutatedAgain";
        assertArrayEquals(new String[]{"First", "Second"}, format.getHeader());
    }

    @Test(timeout = 4000)
    public void testEmptyHeaderSpecification() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        assertNotNull(format.getHeader());
        assertEquals(0, format.getHeader().length);
    }

    @Test(timeout = 4000)
    public void testNullHeaderSpecification() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparatorChar() {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithCommentMarkerCharAndCharacter() {
        final CSVFormat fmt1 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), fmt1.getCommentMarker());
        assertTrue(fmt1.isCommentMarkerSet());

        final CSVFormat fmt2 = fmt1.withCommentMarker((Character) null);
        assertNull(fmt2.getCommentMarker());
        assertFalse(fmt2.isCommentMarkerSet());
    }

    @Test(timeout = 4000)
    public void testWithEscapeCharAndCharacter() {
        final CSVFormat fmt1 = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), fmt1.getEscapeCharacter());
        assertTrue(fmt1.isEscapeCharacterSet());

        final CSVFormat fmt2 = fmt1.withEscape((Character) null);
        assertNull(fmt2.getEscapeCharacter());
        assertFalse(fmt2.isEscapeCharacterSet());
    }

    @Test(timeout = 4000)
    public void testWithQuoteCharAndCharacter() {
        final CSVFormat fmt1 = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), fmt1.getQuoteCharacter());
        assertTrue(fmt1.isQuoteCharacterSet());

        final CSVFormat fmt2 = fmt1.withQuote((Character) null);
        assertNull(fmt2.getQuoteCharacter());
        assertFalse(fmt2.isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testWithNullStringState() {
        final CSVFormat fmt1 = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", fmt1.getNullString());
        assertTrue(fmt1.isNullStringSet());

        final CSVFormat fmt2 = fmt1.withNullString(null);
        assertNull(fmt2.getNullString());
        assertFalse(fmt2.isNullStringSet());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
    // =========================================================================

    /**
     * Defects4J Target Defect:
     * CSVFormat.EXCEL specification states:
     * withAllowMissingColumnNames(true)
     * In the buggy version, EXCEL lacks allowMissingColumnNames=true,
     * triggering: java.lang.IllegalArgumentException: The header contains a duplicate name: "" in [A, B, C, , ]
     */
    @Test(timeout = 4000)
    public void testExcelFormatAllowMissingColumnNamesDefect() {
        assertTrue("CSVFormat.EXCEL must have allowMissingColumnNames set to true per specification",
                CSVFormat.EXCEL.getAllowMissingColumnNames());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNewFormatThrowsOnLineBreakLF() {
        CSVFormat.newFormat(LF);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNewFormatThrowsOnLineBreakCR() {
        CSVFormat.newFormat(CR);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithDelimiterThrowsOnLineBreak() {
        CSVFormat.DEFAULT.withDelimiter(LF);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithQuoteThrowsOnLineBreakChar() {
        CSVFormat.DEFAULT.withQuote(CR);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithQuoteThrowsOnLineBreakCharacter() {
        CSVFormat.DEFAULT.withQuote(Character.valueOf(LF));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithCommentMarkerThrowsOnLineBreakChar() {
        CSVFormat.DEFAULT.withCommentMarker(LF);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithCommentMarkerThrowsOnLineBreakCharacter() {
        CSVFormat.DEFAULT.withCommentMarker(Character.valueOf(CR));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithEscapeThrowsOnLineBreakChar() {
        CSVFormat.DEFAULT.withEscape(LF);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithEscapeThrowsOnLineBreakCharacter() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf(CR));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThrowsWhenQuoteEqualsDelimiter() {
        CSVFormat.DEFAULT.withQuote(',').withDelimiter(',');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThrowsWhenEscapeEqualsDelimiter() {
        CSVFormat.DEFAULT.withEscape(';').withDelimiter(';');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThrowsWhenCommentMarkerEqualsDelimiter() {
        CSVFormat.DEFAULT.withCommentMarker('#').withDelimiter('#');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThrowsWhenQuoteEqualsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('!').withQuote('!');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThrowsWhenEscapeEqualsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('?').withEscape('?');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThrowsWhenQuoteModeNoneWithoutEscape() {
        CSVFormat.DEFAULT.withQuote(null).withQuoteMode(QuoteMode.NONE);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThrowsOnDuplicateHeaderNames() {
        CSVFormat.DEFAULT.withHeader("ColumnA", "ColumnB", "ColumnA");
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Contract Integrity & Output Formatting
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringFullRepresentation() {
        final CSVFormat fmt = CSVFormat.DEFAULT
                .withEscape('\\')
                .withQuote('\"')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withRecordSeparator("\r\n")
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withHeader("H1", "H2");

        final String str = fmt.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("QuoteChar=<\">"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("RecordSeparator=<\r\n>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:false"));
        assertTrue(str.contains("Header:[H1, H2]"));
    }

    @Test(timeout = 4000)
    public void testToStringMinimalRepresentation() {
        final CSVFormat fmt = CSVFormat.newFormat('|');
        final String str = fmt.toString();
        assertEquals("Delimiter=<|> SkipHeaderRecord:false", str);
    }

    @Test(timeout = 4000)
    public void testEqualsContractComprehensive() {
        final CSVFormat fmt1 = CSVFormat.DEFAULT;
        assertEquals(fmt1, fmt1);
        assertNotEquals(fmt1, null);
        assertNotEquals(fmt1, "NotACSVFormatObject");

        final CSVFormat fmt2 = CSVFormat.DEFAULT;
        assertEquals(fmt1, fmt2);
        assertEquals(fmt1.hashCode(), fmt2.hashCode());

        // Field-by-field divergence testing
        assertNotEquals(fmt1, fmt1.withDelimiter(';'));
        assertNotEquals(fmt1, fmt1.withQuoteMode(QuoteMode.ALL));
        assertNotEquals(fmt1, fmt1.withQuote('\''));
        assertNotEquals(fmt1, fmt1.withQuote(null));
        assertNotEquals(fmt1.withQuote(null), fmt1);

        assertNotEquals(fmt1, fmt1.withCommentMarker('#'));
        assertNotEquals(fmt1.withCommentMarker('#'), fmt1);
        assertNotEquals(fmt1.withCommentMarker('#'), fmt1.withCommentMarker('!'));

        assertNotEquals(fmt1, fmt1.withEscape('\\'));
        assertNotEquals(fmt1.withEscape('\\'), fmt1);
        assertNotEquals(fmt1.withEscape('\\'), fmt1.withEscape('/'));

        assertNotEquals(fmt1, fmt1.withNullString("N/A"));
        assertNotEquals(fmt1.withNullString("N/A"), fmt1);
        assertNotEquals(fmt1.withNullString("N/A"), fmt1.withNullString("NULL"));

        assertNotEquals(fmt1, fmt1.withHeader("A", "B"));
        assertNotEquals(fmt1.withHeader("A"), fmt1.withHeader("B"));

        assertNotEquals(fmt1, fmt1.withIgnoreSurroundingSpaces(true));
        assertNotEquals(fmt1, fmt1.withIgnoreEmptyLines(false));
        assertNotEquals(fmt1, fmt1.withSkipHeaderRecord(true));

        assertNotEquals(fmt1, fmt1.withRecordSeparator("\n"));
        assertNotEquals(fmt1, CSVFormat.newFormat(',').withQuote('"').withRecordSeparator((String) null));
        assertNotEquals(CSVFormat.newFormat(',').withQuote('"'), fmt1);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistencyWithNullFields() {
        final CSVFormat minFmt = CSVFormat.newFormat(';');
        final int hash1 = minFmt.hashCode();
        final int hash2 = minFmt.hashCode();
        assertEquals(hash1, hash2);

        final CSVFormat populated = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("NULL")
                .withQuoteMode(QuoteMode.MINIMAL)
                .withHeader("A", "B");
        assertTrue(populated.hashCode() != 0);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        final CSVFormat original = CSVFormat.DEFAULT
                .withCommentMarker('#')
                .withEscape('\\')
                .withNullString("NULL")
                .withHeader("C1", "C2")
                .withIgnoreSurroundingSpaces(true)
                .withSkipHeaderRecord(true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        final CSVFormat deserialized = (CSVFormat) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertArrayEquals(original.getHeader(), deserialized.getHeader());
    }
}