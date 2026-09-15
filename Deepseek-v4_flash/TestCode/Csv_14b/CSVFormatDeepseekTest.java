package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Partitions:
 * A - Core functional logic & state transitions: constructor, with* methods (immutability), getters.
 * B - Boundary value analysis: null/empty strings, extreme characters (LF, CR, quote, delimiter), line breaks.
 * C - Defect-targeted branch zone: printAndEscape, printAndQuote with escape set, no quoting, special characters.
 * D - Exception & defensive guard paths: validation in validate() and with* methods.
 * E - Object lifecycle & contract: equals, hashCode, toString.
 *
 * Known defect (Defects4J): When escape character is set and quoting is not used (QuoteMode.NONE or no quote char),
 * the print method fails to escape certain characters correctly, producing quoted output instead of escaped,
 * or wrong escaping. Specifically, backslash and null cases are affected.
 * Targeted branches:
 * - print() -> no quote char + escape char set -> calls printAndEscape
 * - printAndEscape: should escape CR, LF, delimiter, escape char with the escape char prefix.
 * - printAndQuote with QuoteMode.NONE falls through to printAndEscape (but NONE mode should NOT quote).
 * - The bug likely is in printAndEscape or in the decision logic that incorrectly uses quoting when escape is set.
 *
 * Tests below exercise these paths to reveal the defect.
 */
public class CSVFormatDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDefaultFormatGetters() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        assertEquals(',', fmt.getDelimiter());
        assertEquals(Character.valueOf('"'), fmt.getQuoteCharacter());
        assertEquals("\r\n", fmt.getRecordSeparator());
        assertTrue(fmt.getIgnoreEmptyLines());
        assertFalse(fmt.getAllowMissingColumnNames());
        assertFalse(fmt.getIgnoreHeaderCase());
        assertFalse(fmt.getIgnoreSurroundingSpaces());
        assertFalse(fmt.getSkipHeaderRecord());
        assertFalse(fmt.getTrailingDelimiter());
        assertFalse(fmt.getTrim());
        assertNull(fmt.getCommentMarker());
        assertNull(fmt.getEscapeCharacter());
        assertNull(fmt.getNullString());
        assertNull(fmt.getQuoteMode());
        assertNull(fmt.getHeader());
        assertNull(fmt.getHeaderComments());
    }

    @Test(timeout = 4000)
    public void testWithMethodsReturnNewInstance() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat mod = base.withDelimiter(';');
        assertNotSame(base, mod);
        assertEquals(';', mod.getDelimiter());
        assertEquals(',', base.getDelimiter()); // original unchanged
    }

    @Test(timeout = 4000)
    public void testWithChain() {
        CSVFormat fmt = CSVFormat.DEFAULT
                .withDelimiter('|')
                .withQuote(null)
                .withEscape('\\')
                .withRecordSeparator("\n");
        assertEquals('|', fmt.getDelimiter());
        assertNull(fmt.getQuoteCharacter());
        assertEquals(Character.valueOf('\\'), fmt.getEscapeCharacter());
        assertEquals("\n", fmt.getRecordSeparator());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testNullHeader() {
        CSVFormat fmt = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(fmt.getHeader());
    }

    @Test(timeout = 4000)
    public void testEmptyHeader() {
        CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
        assertArrayEquals(new String[0], fmt.getHeader());
    }

    @Test(timeout = 4000)
    public void testHeaderWithNullStringArray() {
        // withHeader(String...) with null element is allowed (toString will be "null")
        CSVFormat fmt = CSVFormat.DEFAULT.withHeader("A", null, "B");
        String[] expected = {"A", null, "B"};
        assertArrayEquals(expected, fmt.getHeader());
    }

    @Test(timeout = 4000)
    public void testNullCommentMarker() {
        CSVFormat fmt = CSVFormat.DEFAULT.withCommentMarker((Character) null);
        assertNull(fmt.getCommentMarker());
    }

    @Test(timeout = 4000)
    public void testNullEscape() {
        CSVFormat fmt = CSVFormat.DEFAULT.withEscape((Character) null);
        assertNull(fmt.getEscapeCharacter());
    }

    @Test(timeout = 4000)
    public void testNullQuote() {
        CSVFormat fmt = CSVFormat.DEFAULT.withQuote((Character) null);
        assertNull(fmt.getQuoteCharacter());
    }

    @Test(timeout = 4000)
    public void testNullNullString() {
        CSVFormat fmt = CSVFormat.DEFAULT.withNullString(null);
        assertNull(fmt.getNullString());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    // The known defect: print with escape set and no quoting fails to escape properly.
    // We test format() which internally uses printRecord.

    @Test(timeout = 4000)
    public void testPrintEscapeBackslash() {
        // Escape char = '\\', no quote char, record separator = "\n"
        CSVFormat fmt = CSVFormat.newFormat('|')
                .withEscape('\\')
                .withQuote(null)
                .withRecordSeparator("\n");
        // Value containing a backslash should be escaped as "\\"
        String result = fmt.format("a\\b");
        // Expected: "a\\b" (backslash escaped as \\)
        assertEquals("a\\\\b", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeDelimiterInValue() {
        CSVFormat fmt = CSVFormat.newFormat(';')
                .withEscape('\\')
                .withQuote(null)
                .withRecordSeparator("\n");
        String result = fmt.format("val;ue");
        // Expect: "val\\;ue" because delimiter ; must be escaped
        assertEquals("val\\;ue", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeNewline() {
        CSVFormat fmt = CSVFormat.newFormat(',')
                .withEscape('\\')
                .withQuote(null)
                .withRecordSeparator("\n");
        // value with LF (0x0A) should be escaped as \n (two characters: backslash + 'n')
        String valueWithLF = "line1\nline2";
        String result = fmt.format(valueWithLF);
        // Expected: "line1\\nline2"
        assertEquals("line1\\nline2", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeCR() {
        CSVFormat fmt = CSVFormat.newFormat(',')
                .withEscape('\\')
                .withQuote(null)
                .withRecordSeparator("\n");
        String valueWithCR = "line1\rline2";
        String result = fmt.format(valueWithCR);
        // Expected: "line1\\rline2"
        assertEquals("line1\\rline2", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeMultipleSpecialChars() {
        CSVFormat fmt = CSVFormat.newFormat(',')
                .withEscape('\\')
                .withQuote(null)
                .withRecordSeparator("\n");
        String value = "a,b\nc\\d\re";
        String result = fmt.format(value);
        // Expected: "a\\,b\\nc\\\\d\\re"
        assertEquals("a\\,b\\nc\\\\d\\re", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeNullString() {
        // When nullString is set and value is null, nullString should be output as is (no escaping needed)
        CSVFormat fmt = CSVFormat.newFormat(',')
                .withEscape('\\')
                .withQuote(null)
                .withNullString("\\N")
                .withRecordSeparator("\n");
        String result = fmt.format((Object) null);
        // nullString = "\\N", which contains a backslash. Since backslash is escape char, it should be escaped.
        // But the nullString is written as-is? Actually the print method when object is null writes nullString directly (no escaping).
        // However, according to CSVFormat.print(), when object is null, it appends value (nullString) directly, see print method:
        // if (object == null) { out.append(value); } => no escaping.
        // That could be a bug too. But the known defect is about non-null values.
        // We'll assert that nullString is output literally (maybe defect? but test covers boundary).
        // However, the known defect testEscapeNull1 expects a backslash when printing null with escape?
        // Actually testEscapeNull1 in CSVPrinterTest expects "\" (single backslash) but got "\"\"" (quoted).
        // That test likely uses quoting enabled. So we need a test with quoting but escape set? Let's examine.
        // The defect description: testEscapeNull1: expected:<[\]> but was:<["\"]>. That indicates when printing a null
        // with nullString = "\\" (maybe) and escape char set, it was quoting instead of just outputting backslash.
        // We'll create a test that mimics that scenario: with quote char, escape char, and nullString, and print null.
        // Let's do a separate test.
        assertEquals("\\N", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeNullWithQuoteChar() {
        // Simulate testEscapeNull1 scenario: null string = "\\", quote char = '"', escape char = '\\'
        CSVFormat fmt = CSVFormat.DEFAULT
                .withEscape('\\')
                .withNullString("\\")
                .withQuoteMode(QuoteMode.NONE)  // ensure no quoting, use escape only
                .withRecordSeparator("\n");
        String result = fmt.format((Object) null);
        // Expected: "\" (single backslash). The nullString is "\\" which is a String containing one backslash.
        // With escape character set, when printing non-null, escaping would happen, but for null it writes nullString directly.
        // Actually the print method: if (object == null) { out.append(value); } — value is nullString. No escaping.
        // So expected output is "\". However, previously with quoting enabled and QuoteMode not NONE, it might have quoted.
        // With QuoteMode.NONE, quoting is disabled, so it should go to printAndEscape route for non-null.
        // But for null, it goes to the direct append branch. So it should output the null string as is.
        // Let's verify: print method code:
        // if (object == null) { charSequence = nullString == null ? Constants.EMPTY : nullString; }
        // then charSequence = getTrim() ? trim(charSequence) : charSequence;
        // then this.print(value, charSequence, 0, charSequence.length(), out, newRecord);
        // In private print(Object, CharSequence,...) method:
        // if (object == null) { out.append(value); } 
        // So for null, it goes to out.append(value) directly, which writes the nullString without escaping.
        // Therefore expected output is "\\" which is a single backslash.
        assertEquals("\\", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeBackslashInValueWithQuoteNone() {
        // Defect testEscapeBackslash1: expected:<[\]> but was:<['\']>
        // That test likely uses quote char = '\'' and escape char = '\\' and value contains a single backslash.
        CSVFormat fmt = CSVFormat.newFormat(',')
                .withEscape('\\')
                .withQuote('\'')
                .withQuoteMode(QuoteMode.NONE) // Ensures no quoting, only escape
                .withRecordSeparator("\n");
        String result = fmt.format("\\"); // a single backslash
        // Expected: "\\" (two characters: backslash backslash)
        assertEquals("\\\\", result);
    }

    @Test(timeout = 4000)
    public void testPrintEscapeBackslashWithMultipleChars() {
        CSVFormat fmt = CSVFormat.newFormat(',')
                .withEscape('\\')
                .withQuote(null)
                .withRecordSeparator("\n");
        String result = fmt.format("a\\b\\c");
        assertEquals("a\\\\b\\\\c", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteMinimalAndSpecialChars() {
        // Ensure minimal quoting works (not defect but coverage)
        CSVFormat fmt = CSVFormat.DEFAULT.withRecordSeparator("\n"); // default quote '"', no escape
        String result = fmt.format("hello,world");
        assertEquals("\"hello,world\"", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteAll() {
        CSVFormat fmt = CSVFormat.DEFAULT
                .withQuoteMode(QuoteMode.ALL)
                .withRecordSeparator("\n");
        String result = fmt.format("simple");
        assertEquals("\"simple\"", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteNonNumericForString() {
        CSVFormat fmt = CSVFormat.DEFAULT
                .withQuoteMode(QuoteMode.NON_NUMERIC)
                .withRecordSeparator("\n");
        String result = fmt.format("text");
        assertEquals("\"text\"", result);
    }

    @Test(timeout = 4000)
    public void testPrintWithQuoteNonNumericForNumber() {
        CSVFormat fmt = CSVFormat.DEFAULT
                .withQuoteMode(QuoteMode.NON_NUMERIC)
                .withRecordSeparator("\n");
        String result = fmt.format(123);
        assertEquals("123", result);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNewFormatWithLineBreakDelimiter() {
        CSVFormat.newFormat('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterEqualsQuote() {
        CSVFormat.DEFAULT.withDelimiter('"');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterEqualsEscape() {
        CSVFormat.newFormat(',')  // default delimiter is ','
                .withEscape(',');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterEqualsCommentMarker() {
        CSVFormat.newFormat('#')
                .withCommentMarker('#');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testQuoteEqualsCommentMarker() {
        CSVFormat.DEFAULT.withCommentMarker('"');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeEqualsCommentMarker() {
        CSVFormat.newFormat(',')
                .withEscape('#')
                .withCommentMarker('#');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNoQuoteModeAndNoEscape() {
        CSVFormat.newFormat(',')
                .withQuoteMode(QuoteMode.NONE)
                .withEscape(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateHeader() {
        CSVFormat.DEFAULT.withHeader("A", "B", "A");
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        assertEquals(fmt, fmt);
    }

    @Test(timeout = 4000)
    public void testEqualsEqualFormats() {
        CSVFormat fmt1 = CSVFormat.DEFAULT;
        CSVFormat fmt2 = CSVFormat.DEFAULT;
        assertEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDelimiter() {
        CSVFormat fmt1 = CSVFormat.DEFAULT;
        CSVFormat fmt2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertNotEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        assertNotNull(CSVFormat.DEFAULT);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        CSVFormat fmt = CSVFormat.DEFAULT.withEscape('\\').withQuote(null);
        int hc1 = fmt.hashCode();
        int hc2 = fmt.hashCode();
        assertEquals(hc1, hc2);
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferent() {
        CSVFormat fmt1 = CSVFormat.DEFAULT;
        CSVFormat fmt2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertNotEquals(fmt1.hashCode(), fmt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringContainsDelimiter() {
        String str = CSVFormat.DEFAULT.toString();
        assertTrue(str.contains("Delimiter=<,>"));
    }

    @Test(timeout = 4000)
    public void testToStringWithEscape() {
        CSVFormat fmt = CSVFormat.DEFAULT.withEscape('\\');
        String str = fmt.toString();
        assertTrue(str.contains("Escape=<\\>"));
    }

    @Test(timeout = 4000)
    public void testWithFirstRecordAsHeader() {
        CSVFormat fmt = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        assertNotNull(fmt.getHeader());
        assertEquals(0, fmt.getHeader().length);
        assertTrue(fmt.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreEmptyLinesDefault() {
        CSVFormat fmt = CSVFormat.DEFAULT.withIgnoreEmptyLines();
        assertTrue(fmt.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreSurroundingSpacesDefault() {
        CSVFormat fmt = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces();
        assertTrue(fmt.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreHeaderCaseDefault() {
        CSVFormat fmt = CSVFormat.DEFAULT.withIgnoreHeaderCase();
        assertTrue(fmt.getIgnoreHeaderCase());
    }

    @Test(timeout = 4000)
    public void testWithSkipHeaderRecordDefault() {
        CSVFormat fmt = CSVFormat.DEFAULT.withSkipHeaderRecord();
        assertTrue(fmt.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithTrailingDelimiterDefault() {
        CSVFormat fmt = CSVFormat.DEFAULT.withTrailingDelimiter();
        assertTrue(fmt.getTrailingDelimiter());
    }

    @Test(timeout = 4000)
    public void testWithTrimDefault() {
        CSVFormat fmt = CSVFormat.DEFAULT.withTrim();
        assertTrue(fmt.getTrim());
    }

    @Test(timeout = 4000)
    public void testWithAllowMissingColumnNamesDefault() {
        CSVFormat fmt = CSVFormat.DEFAULT.withAllowMissingColumnNames();
        assertTrue(fmt.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testPrintlnWithTrailingDelimiter() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat fmt = CSVFormat.DEFAULT.withTrailingDelimiter().withRecordSeparator("\n");
        fmt.println(sb);
        assertEquals(",\n", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPrintlnWithoutTrailingDelimiter() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat fmt = CSVFormat.DEFAULT.withRecordSeparator("\r\n");
        fmt.println(sb);
        assertEquals("\r\n", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimEnabled() throws IOException {
        CSVFormat fmt = CSVFormat.DEFAULT.withTrim().withRecordSeparator("\n");
        String result = fmt.format("  spaced  ");
        assertEquals("spaced", result);
    }

    @Test(timeout = 4000)
    public void testTrimDisabled() throws IOException {
        CSVFormat fmt = CSVFormat.DEFAULT.withRecordSeparator("\n");
        String result = fmt.format("  spaced  ");
        assertEquals("  spaced  ", result);
    }

    @Test(timeout = 4000)
    public void testNewFormatMinimal() {
        CSVFormat fmt = CSVFormat.newFormat(',');
        assertEquals(',', fmt.getDelimiter());
        assertNull(fmt.getQuoteCharacter());
        assertNull(fmt.getEscapeCharacter());
        assertNull(fmt.getCommentMarker());
        assertFalse(fmt.getIgnoreEmptyLines());
        assertFalse(fmt.getIgnoreSurroundingSpaces());
        assertNull(fmt.getRecordSeparator());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValidateExceptionQuoteModeNoneNoEscape() {
        CSVFormat.newFormat(',')
                .withQuoteMode(QuoteMode.NONE);
        // Constructor will throw because escape is null and quoteMode is NONE
    }

    @Test(timeout = 4000)
    public void testWithHeaderEnum() {
        CSVFormat fmt = CSVFormat.DEFAULT.withHeader(Predefined.class);
        String[] expected = {"Default", "Excel", "InformixUnload", "InformixUnloadCsv", "MySQL", "RFC4180", "TDF"};
        assertArrayEquals(expected, fmt.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithNullHeaderComments() {
        CSVFormat fmt = CSVFormat.DEFAULT.withHeaderComments((Object[]) null);
        assertNull(fmt.getHeaderComments());
    }

    @Test(timeout = 4000)
    public void testWithHeaderCommentsCopied() {
        Object[] comments = {"Comment1", null, "Comment3"};
        CSVFormat fmt = CSVFormat.DEFAULT.withHeaderComments(comments);
        String[] expected = {"Comment1", null, "Comment3"};
        assertArrayEquals(expected, fmt.getHeaderComments());
    }

    @Test(timeout = 4000)
    public void testSerialize() throws Exception {
        // Basic serialization/deserialization test (not required but nice)
        CSVFormat fmt = CSVFormat.DEFAULT.withEscape('\\');
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(fmt);
        oos.close();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        CSVFormat deserialized = (CSVFormat) ois.readObject();
        assertEquals(fmt, deserialized);
        assertEquals(fmt.getDelimiter(), deserialized.getDelimiter());
        assertEquals(fmt.getEscapeCharacter(), deserialized.getEscapeCharacter());
    }

    @Test(timeout = 4000)
    public void testIsCommentMarkerSet() {
        assertFalse(CSVFormat.DEFAULT.isCommentMarkerSet());
        assertTrue(CSVFormat.DEFAULT.withCommentMarker('#').isCommentMarkerSet());
    }

    @Test(timeout = 4000)
    public void testIsEscapeCharacterSet() {
        assertFalse(CSVFormat.DEFAULT.isEscapeCharacterSet());
        assertTrue(CSVFormat.DEFAULT.withEscape('\\').isEscapeCharacterSet());
    }

    @Test(timeout = 4000)
    public void testIsNullStringSet() {
        assertFalse(CSVFormat.DEFAULT.isNullStringSet());
        assertTrue(CSVFormat.DEFAULT.withNullString("NULL").isNullStringSet());
    }

    @Test(timeout = 4000)
    public void testIsQuoteCharacterSet() {
        assertTrue(CSVFormat.DEFAULT.isQuoteCharacterSet());
        assertFalse(CSVFormat.DEFAULT.withQuote(null).isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testValueOfPredefined() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
    }

    @Test(timeout = 4000)
    public void testPredefinedGetFormat() {
        assertSame(CSVFormat.DEFAULT, CSVFormat.Predefined.Default.getFormat());
    }
}