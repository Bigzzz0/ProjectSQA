package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * White-box JUnit 4 test suite for CSVFormat.
 * Targets the known defect where the Euro character (€) is unnecessarily quoted
 * when it appears as the first character of a value.
 * Also achieves maximum line and branch coverage.
 */
public class CSVFormatDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partitions:
     * A: Core functional logic & state transitions (predefined formats, with methods, getters)
     * B: Boundary value analysis (nulls, empty strings, zero/negative/MAX boundaries, line break characters)
     * C: Defect-targeted branch zone (Euro first char quoting – MINIMAL mode)
     * D: Exception & defensive guard paths (validate() – illegal delimiter, duplicate header, conflicting chars)
     * E: Object lifecycle & contract (equals, hashCode, toString)
     * 
     * Key branches in printAndQuote (QuoteMode.MINIMAL):
     *   - newRecord && (c < 0x20 || c > 0x21 && c < 0x23 || c > 0x2B && c < 0x2D || c > 0x7E) => quote = true
     *   - c <= COMMENT (i.e., '#') => quote = true
     *   - while loop: LF, CR, quoteChar, delimChar => quote = true
     *   - end-of-value check: c <= SP => quote = true
     * 
     * Defect: Euro (0x20AC > 0x7E) causes quote to be set when it is not needed.
     * Fix should exclude characters like Euro (non-ASCII but not requiring quoting).
     */

    // ============================
    // Partition A: Core Functional Logic
    // ============================

    @Test(timeout = 4000)
    public void testDefaultFormatProperties() {
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteCharacter());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertFalse(CSVFormat.DEFAULT.getAllowMissingColumnNames());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertNull(CSVFormat.DEFAULT.getQuoteMode());
        assertFalse(CSVFormat.DEFAULT.getTrailingDelimiter());
        assertFalse(CSVFormat.DEFAULT.getTrim());
        assertFalse(CSVFormat.DEFAULT.getAutoFlush());
    }

    @Test(timeout = 4000) // typo: should be 4000? I will keep 4000.
    public void testExcelFormatProperties() {
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());
        assertTrue(CSVFormat.EXCEL.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testRfc4180Properties() {
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testTdfProperties() {
        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testWithMethodsReturnNewInstance() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withDelimiter(';');
        assertNotSame(base, modified);
        assertEquals(';', modified.getDelimiter());
        // Ensure base unchanged
        assertEquals(',', base.getDelimiter());
    }

    @Test(timeout = 4000)
    public void testWithHeaderVariants() {
        CSVFormat fmt = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        assertArrayEquals(new String[]{"A", "B", "C"}, fmt.getHeader());
        fmt = CSVFormat.DEFAULT.withHeader(); // empty header for automatic parsing
        assertArrayEquals(new String[0], fmt.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithCommentMarkerChar() {
        CSFormat fmt = CSVFormat.DEFAULT.withCommentarker('#');        assertEquals(Character.valueof('#'), fmt.getCommentMarker());
    }

    @Test(timeout=4000)
    public void testWithEscapeChar() {
        CSVFormat fmt = CSformat.DEFAUT.withEscape('\\');
        assertEquals(Character.valuef('\\'), fmt.getEscapeCharacter());
    }

    @Test(timeout 4000)
    public void tetIgnoreEmptyLinesTrue() {        CSFormat fmt = CSVFormat.DFULT.withgnoreEmptyLines(true);
        asserTrue(fmt.gtIgnoreEmptyLines());
    }

    // ============================
    // Partition B: Boundary Value Analysis
    // ============================

    @Test(timeout 4000)
    public void testNullQutCharacter() {
        CSFormat fmt = CSVFormat.DEFAULT.withQuote(null);
        assertFalse(fmt.isQuotCharacterSet());
        assertNull(fmt.getQuoteharacter());
    }

    @Test(timeout 4000)    public void testNulEscapeCharactr() {
        CSFormat fmt = CSFormat.DEFAUT.wihEscape(null);        assertFalse(fm.tEscapeCharacterSet());
    }

    @Test(timeout 4000)
    public void testNllString() {
        CSFormat fmt = CSVFormat.DEFAULT.wihNullString("\\N");    
        assertEquals("\\N", fmt.getNullString());
        assertTrue(fmt.isNullStringSet());    
    }

    @Test(timeout 4000)    public void trokeLineBreakChar() {
        // via withDelimiter, withCommentMarker, withEscape, withQuote should throw
        try {            CSFormat.DFAULT.withDelimiter('\n');
            fail();
        } catch (IllegalArgumentException expected)            // ok        }

        try {
            CSVFormat.DEFAULT.withComentMarker('\r');            fail();        } catch (IllegalArgumentException e) {}
        try {
            CSVFormat.DEFAUT.withEscape('\n');
            fail();
        } catch (IllegalArgumentExcption e) {}
        try {            CSVFormat.DEFULT.withQuote('\r');
            fail();        } catch (IllegalArgumentException e) {}
    }

    @Test(timeout=4000)
    public void testEmtyStringNulString() {
        CSFormat fmt = CSVFormat.DEFAULT.withNllString("");
        assertEquals( "", fmt.getNullString());
    }

    // BVA on trim
    @Test(timeout 4000)    public void testTrimValues() {
        CSVFormat fmt = CSVFormat.DEFAULT.withTrim(true);        String result = fmt.format("  hello  ");
        assertEquals("hello", result); // trim removes leading/trailing spaces
    }

    @Test(timeout = 4000)
    public void testIgnoreSurroundingSpaces() {
        CSVFormat fmt = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        // This does not trim spaces inside values; parsing is not tested here.
        // For format, it applies only if trim is also true. IgnoreSurroundingSpaces does not affect format.
        assertTrue(fmt.getIgnoreSurroundingSpaces());
    }

    // ============================
    // Partition C: Defect-Targeted Branch Zone (Euro first char)
    // ============================

    /**
     * Reproduces the known defect: Euro character (€) should not be quoted when
     * it is the first character of a value in MINIMAL quote mode.
     * The bug causes quoting because of condition (c > 0x7E) in printAndQuote.
     * Expected output: ",Deux" (no quotes)
     */
    @Test(timeout = 4000)
    public void testDontQuoteEuroFirstChar() {
        CSVFormat format = CSVFormat.DEFAULT; // QuoteMode.MINIMAL (null defaults to MINIMAL)
        String result = format.format("€", "Deux");
        assertEquals("€,Deux", result);
    }

    @Test(timeout = 4000)
    public void testDontQuoteNonAsciiFirstChar() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("ü", "test");
        assertEquals("ü,test", result);
    }

    @Test(timeout = 4000)
    public void testQuoteEuroWhenOtherCharsForceQuote() {
        // If value contains delimiter or quote, Euro should still be quoted because of that char
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("€,test");
        // Contains comma -> must be quoted
        assertEquals("\"€,test\"", result);
    }

    // ============================
    // Partition D: Exception & Defensive Guard Paths (validate)
    // ============================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterAndQuoteSame() {
        CSVFormat.DEFAULT.withQuote(',').withDelimiter(','); // order doesn't matter: constructor validates
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterAndEscapeSame() {
        CSVFormat.DEFAULT.withEscape(',').withDelimiter(',');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterAndCommentSame() {
        CSVFormat.DEFAULT.withCommentMarker(',').withDelimiter(',');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testQuoteAndCommentSame() {
        CSVFormat.DEFAULT.withQuote('#').withCommentMarker('#');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeAndCommentSame() {
        CSVFormat.DEFAULT.withEscape('#').withCommentMarker('#');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeNullWithQuoteModeNone() {
        CSVFormat.DEFAULT.withEscape(null).withQuoteMode(QuoteMode.NONE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateHeader() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNewFormatLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }

    // Additional validation: withRecordSeparator not checking line breaks? It allows any string.
    @Test(timeout = 4000)
    public void testRecordSeparatorCustom() {
        CSVFormat fmt = CSVFormat.DEFAULT.withRecordSeparator("RS");
        assertEquals("RS", fmt.getRecordSeparator());
    }

    // ============================
    // Partition E: Object Lifecycle & Contract (equals, hashCode, toString)
    // ============================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        assertTrue(fmt.equals(fmt));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        assertFalse(CSVFormat.DEFAULT.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        assertFalse(CSVFormat.DEFAULT.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsIdentical() {
        CSVFormat fmt1 = CSVFormat.DEFAULT;
        CSVFormat fmt2 = CSVFormat.DEFAULT;
        assertEquals(fmt1, fmt2);
        assertEquals(fmt1.hashCode(), fmt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDelimiter() {
        CSVFormat fmt1 = CSVFormat.DEFAULT;
        CSVFormat fmt2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertNotEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentQuoteMode() {
        CSVFormat fmt1 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        CSVFormat fmt2 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        assertNotEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        CSVFormat fmt = CSVFormat.DEFAULT.withHeader("X");
        int hash1 = fmt.hashCode();
        int hash2 = fmt.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testToStringContainsKeyElements() {
        String str = CSVFormat.DEFAULT.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("QuoteChar=<\""));
        assertTrue(str.contains("RecordSeparator=<")); // depends on format
    }

    // ============================
    // Additional Coverage for print paths via format()
    // ============================

    @Test(timeout = 4000)
    public void testPrintNullValue() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        // null value -> nullString not set, so treated as empty string
        String result = fmt.format((Object) null);
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testPrintNullValueWithNullString() {
        CSVFormat fmt = CSVFormat.DEFAULT.withNullString("NULL");
        String result = fmt.format((Object) null);
        assertEquals("NULL", result);
    }

    @Test(timeout = 4000)
    public void testPrintNullValueWithNullStringAndQuoteAll() {
        CSVFormat fmt = CSVFormat.DEFAULT.withNullString("NULL").withQuoteMode(QuoteMode.ALL);
        String result = fmt.format((Object) null);
        assertEquals("\"NULL\"", result);
    }

    @Test(timeout = 4000)
    public void testPrintRecordMultipleValues() {
        String result = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithEscape() {
        CSVFormat fmt = CSVFormat.DEFAULT.withEscape('\\').withQuote(null);
        String result = fmt.format("a,b");
        assertEquals("a\\,b", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteModeNoneAndEscape() {
        // QuoteMode.NONE requires escape
        CSVFormat fmt = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE).withEscape('\\');
        String result = fmt.format("a\"b");
        assertEquals("a\\\"b", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteModeAll() {
        CSVFormat fmt = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals("\"hello\"", fmt.format("hello"));
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteModeAllNonNullAndNull() {
        CSVFormat fmt = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL_NON_NULL);
        assertEquals("\"hello\"", fmt.format("hello"));
        // null is printed without quotes (nullString not set, so empty)
        assertEquals("", fmt.format((Object) null));
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteModeNonNumericNumber() {
        CSVFormat fmt = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC);
        assertEquals("123", fmt.format(123)); // number not quoted
        assertEquals("\"abc\"", fmt.format("abc")); // string quoted
    }

    @Test(timeout = 4000)
    public void testPrintWithTrim() {
        CSVFormat fmt = CSVFormat.DEFAULT.withTrim(true);
        assertEquals("hello", fmt.format("  hello  "));
    }

    @Test(timeout = 4000)
    public void testPrintWithTrailingDelimiter() throws IOException {
        CSVFormat fmt = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        StringBuilder sb = new StringBuilder();
        fmt.printRecord(sb, "a", "b");
        // Expected: "a,b,\r\n"
        String expected = "a,b," + fmt.getRecordSeparator();
        assertEquals(expected, sb.toString());
    }

    // ============================
    // Test CLI interface: valueOf and Predefined
    // ============================
    @Test(timeout = 4000)
    public void testValueOfPredefined() {
        assertSame(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertSame(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
    }

    // ============================
    // Test withFirstRecordAsHeader
    // ============================
    @Test(timeout = 4000)
    public void testWithFirstRecordAsHeader() {
        CSVFormat fmt = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertTrue(fmt.getSkipHeaderRecord());
        assertNotNull(fmt.getHeader());
        assertEquals(0, fmt.getHeader().length); // empty header array
    }

    // ============================
    // Test withIgnoreHeaderCase
    // ============================
    @Test(timeout = 4000)
    public void testWithIgnoreHeaderCaseTrue() {
        CSVFormat fmt = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(fmt.getIgnoreHeaderCase());
    }

    // ============================
    // Test withAutoFlush
    // ============================
    @Test(timeout = 4000)
    public void testWithAutoFlush() {
        CSVFormat fmt = CSVFormat.DEFAULT.withAutoFlush(true);
        assertTrue(fmt.getAutoFlush());
    }

}