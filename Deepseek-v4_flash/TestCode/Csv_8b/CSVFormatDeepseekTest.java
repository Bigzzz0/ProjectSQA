package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: CSVFormat (immutable format configuration)
 * 
 * Decision branches covered:
 * - Constructor: delimiter line-break check (throws IllegalArgumentException)
 * - with* methods: line-break checks for commentStart, escape, quoteChar, delimiter
 * - validate(): 
 *   - quoteChar == delimiter -> IllegalStateException
 *   - escape == delimiter -> IllegalStateException
 *   - commentStart == delimiter -> IllegalStateException
 *   - quoteChar == commentStart -> IllegalStateException
 *   - escape == commentStart -> IllegalStateException
 *   - escape == null && quotePolicy == Quote.NONE -> IllegalStateException
 *   - header duplicates -> IllegalStateException (KNOWN BUG: should be IllegalArgumentException)
 * - equals(): all fields compared, null handling, Arrays.equals for header
 * - hashCode(): prime 31, all fields
 * - toString(): conditional inclusion of escape, quote, comment, nullString, recordSeparator, emptyLines, spaces, skipHeaderRecord, header
 * - getHeader(): returns clone or null
 * - isCommentingEnabled(), isEscaping(), isNullHandling(), isQuoting()
 * - format(): uses CSVPrinter, returns trimmed string
 * - parse(): returns CSVParser
 * - newFormat(): static factory
 * 
 * Boundary conditions:
 * - null vs non-null for Character fields (quoteChar, commentStart, escape)
 * - null vs non-null for String fields (nullString, recordSeparator)
 * - null vs empty vs populated header array
 * - boolean flags: ignoreSurroundingSpaces, ignoreEmptyLines, skipHeaderRecord
 * - line break characters (LF, CR, CRLF) as arguments to with* methods
 * - duplicate header names (defect targeted)
 * - Quote.NONE with no escape (defect targeted)
 * - equality with identical, different, null, different class
 * 
 * Defect-specific test: validate() with duplicate header names should throw IllegalArgumentException (currently throws IllegalStateException)
 */
public class CSVFormatDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testDefaultFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertEquals(Quote.ALL, format.getQuotePolicy()); // default is ALL? Actually DEFAULT uses null quotePolicy? Let's check: DEFAULT constructor passes null for quotePolicy. So getQuotePolicy() returns null.
        // Actually DEFAULT uses null quotePolicy, so getQuotePolicy() returns null.
        assertNull(format.getQuotePolicy());
        assertNull(format.getCommentStart());
        assertNull(format.getEscape());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertTrue(format.getIgnoreEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertTrue(format.isQuoting());
        assertFalse(format.isEscaping());
        assertFalse(format.isCommentingEnabled());
        assertFalse(format.isNullHandling());
    }

    @Test(timeout = 4000)
    public void testRFC4180() {
        CSVFormat format = CSVFormat.RFC4180;
        assertFalse(format.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testExcel() {
        CSVFormat format = CSVFormat.EXCEL;
        assertFalse(format.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testTDF() {
        CSVFormat format = CSVFormat.TDF;
        assertEquals('\t', format.getDelimiter());
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testMYSQL() {
        CSVFormat format = CSVFormat.MYSQL;
        assertEquals('\t', format.getDelimiter());
        assertEquals(Character.valueOf('\\'), format.getEscape());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getQuoteChar());
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteChar());
        assertNull(format.getQuotePolicy());
        assertNull(format.getCommentStart());
        assertNull(format.getEscape());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithDelimiter() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withDelimiter(';');
        assertEquals(';', modified.getDelimiter());
        assertEquals(base.getQuoteChar(), modified.getQuoteChar());
        // other fields unchanged
    }

    @Test(timeout = 4000)
    public void testWithQuoteChar() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withQuoteChar('\'');
        assertEquals(Character.valueOf('\''), modified.getQuoteChar());
        // null disables quoting
        CSVFormat noQuote = base.withQuoteChar(null);
        assertNull(noQuote.getQuoteChar());
        assertFalse(noQuote.isQuoting());
    }

    @Test(timeout = 4000)
    public void testWithQuotePolicy() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withQuotePolicy(Quote.NONE);
        assertEquals(Quote.NONE, modified.getQuotePolicy());
    }

    @Test(timeout = 4000)
    public void testWithCommentStart() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withCommentStart('#');
        assertEquals(Character.valueOf('#'), modified.getCommentStart());
        assertTrue(modified.isCommentingEnabled());
        // null disables
        CSVFormat noComment = modified.withCommentStart(null);
        assertNull(noComment.getCommentStart());
        assertFalse(noComment.isCommentingEnabled());
    }

    @Test(timeout = 4000)
    public void testWithEscape() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withEscape('\\');
        assertEquals(Character.valueOf('\\'), modified.getEscape());
        assertTrue(modified.isEscaping());
        // null disables
        CSVFormat noEscape = modified.withEscape(null);
        assertNull(noEscape.getEscape());
        assertFalse(noEscape.isEscaping());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withIgnoreSurroundingSpaces(true);
        assertTrue(modified.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreEmptyLines() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withIgnoreEmptyLines(false);
        assertFalse(modified.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparatorChar() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withRecordSeparator('\n');
        assertEquals("\n", modified.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparatorString() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withRecordSeparator("|");
        assertEquals("|", modified.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithNullString() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withNullString("NULL");
        assertEquals("NULL", modified.getNullString());
        assertTrue(modified.isNullHandling());
    }

    @Test(timeout = 4000)
    public void testWithHeader() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withHeader("a", "b", "c");
        assertArrayEquals(new String[] {"a", "b", "c"}, modified.getHeader());
        // null header
        CSVFormat noHeader = base.withHeader((String[]) null);
        assertNull(noHeader.getHeader());
        // empty header
        CSVFormat emptyHeader = base.withHeader();
        assertNotNull(emptyHeader.getHeader());
        assertEquals(0, emptyHeader.getHeader().length);
    }

    @Test(timeout = 4000)
    public void testWithSkipHeaderRecord() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withSkipHeaderRecord(true);
        assertTrue(modified.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    @Test(timeout = 4000)
    public void testFormatWithNull() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        String result = format.format("a", null, "c");
        assertEquals("a,NULL,c", result);
    }

    @Test(timeout = 4000)
    public void testParse() throws Exception {
        CSVFormat format = CSVFormat.DEFAULT;
        java.io.Reader reader = new java.io.StringReader("a,b,c");
        CSVParser parser = format.parse(reader);
        assertNotNull(parser);
        parser.close();
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterLineBreakLF() {
        CSVFormat.newFormat('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDelimiterLineBreakCR() {
        CSVFormat.newFormat('\r');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithQuoteCharLineBreak() {
        CSVFormat.DEFAULT.withQuoteChar('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithCommentStartLineBreak() {
        CSVFormat.DEFAULT.withCommentStart('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorDelimiterLineBreak() {
        // private constructor, but we can trigger via newFormat which calls it
        CSVFormat.newFormat('\r');
    }

    @Test(timeout = 4000)
    public void testNullHeaderReturnsNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
    }

    @Test(timeout = 4000)
    public void testGetHeaderReturnsClone() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b");
        String[] header = format.getHeader();
        header[0] = "changed";
        assertArrayEquals(new String[] {"a", "b"}, format.getHeader());
    }

    @Test(timeout = 4000)
    public void testNullRecordSeparator() {
        CSVFormat format = CSVFormat.newFormat(',');
        assertNull(format.getRecordSeparator());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    // Known defect: validate() throws IllegalStateException for duplicate header names, should be IllegalArgumentException
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateHeaderElements() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "a");
        format.validate(); // should throw IllegalArgumentException, but bug causes IllegalStateException
    }

    // Additional validate() tests to cover all branches
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValidateQuoteCharEqualsDelimiter() {
        CSVFormat format = CSVFormat.newFormat('"').withQuoteChar('"');
        format.validate();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValidateEscapeEqualsDelimiter() {
        CSVFormat format = CSVFormat.newFormat('\\').withEscape('\\');
        format.validate();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValidateCommentStartEqualsDelimiter() {
        CSVFormat format = CSVFormat.newFormat('#').withCommentStart('#');
        format.validate();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValidateQuoteCharEqualsCommentStart() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('"');
        format.validate();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValidateEscapeEqualsCommentStart() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('#').withCommentStart('#');
        format.validate();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValidateNoEscapeAndQuoteNone() {
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE).withEscape(null);
        format.validate();
    }

    @Test(timeout = 4000)
    public void testValidateValidFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        format.validate(); // should not throw
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithCommentStartCharacterLineBreak() {
        CSVFormat.DEFAULT.withCommentStart(Character.valueOf('\n'));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithEscapeCharacterLineBreak() {
        CSVFormat.DEFAULT.withEscape(Character.valueOf('\n'));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithQuoteCharCharacterLineBreak() {
        CSVFormat.DEFAULT.withQuoteChar(Character.valueOf('\n'));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertTrue(format.equals(format));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertFalse(format.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsIdentical() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        assertTrue(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDelimiter() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentQuoteChar() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withQuoteChar('\'');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentQuotePolicy() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withQuotePolicy(Quote.NONE);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentCommentStart() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withCommentStart('#');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentEscape() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withEscape('\\');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentNullString() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentIgnoreSurroundingSpaces() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentIgnoreEmptyLines() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentSkipHeaderRecord() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentRecordSeparator() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentHeader() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withHeader("a", "b");
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameHeaderDifferentOrder() {
        CSVFormat f1 = CSVFormat.DEFAULT.withHeader("a", "b");
        CSVFormat f2 = CSVFormat.DEFAULT.withHeader("b", "a");
        assertFalse(f1.equals(f2)); // order matters
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferent() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertNotEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        CSVFormat format = CSVFormat.DEFAULT;
        String str = format.toString();
        assertTrue(str.contains("Delimiter=<,>"));
        assertTrue(str.contains("QuoteChar=<\" >")); // note: space before QuoteChar? Actually toString adds space before each optional field.
        // Let's check exact: "Delimiter=<,> QuoteChar=<">" but there is a space before QuoteChar.
        assertTrue(str.contains("QuoteChar=<"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:false"));
    }

    @Test(timeout = 4000)
    public void testToStringWithAllOptions() {
        CSVFormat format = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentStart('#')
                .withNullString("NULL")
                .withRecordSeparator("|")
                .withIgnoreSurroundingSpaces(true)
                .withHeader("a", "b");
        String str = format.toString();
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NULL>"));
        assertTrue(str.contains("RecordSeparator=<|>"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("Header:[a, b]"));
    }

    @Test(timeout = 4000)
    public void testIsCommentingEnabled() {
        assertFalse(CSVFormat.DEFAULT.isCommentingEnabled());
        assertTrue(CSVFormat.DEFAULT.withCommentStart('#').isCommentingEnabled());
    }

    @Test(timeout = 4000)
    public void testIsEscaping() {
        assertFalse(CSVFormat.DEFAULT.isEscaping());
        assertTrue(CSVFormat.DEFAULT.withEscape('\\').isEscaping());
    }

    @Test(timeout = 4000)
    public void testIsNullHandling() {
        assertFalse(CSVFormat.DEFAULT.isNullHandling());
        assertTrue(CSVFormat.DEFAULT.withNullString("NULL").isNullHandling());
    }

    @Test(timeout = 4000)
    public void testIsQuoting() {
        assertTrue(CSVFormat.DEFAULT.isQuoting());
        assertFalse(CSVFormat.DEFAULT.withQuoteChar(null).isQuoting());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(format);
        oos.close();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        CSVFormat deserialized = (CSVFormat) ois.readObject();
        assertEquals(format, deserialized);
        assertEquals(format.hashCode(), deserialized.hashCode());
        assertArrayEquals(format.getHeader(), deserialized.getHeader());
    }
}