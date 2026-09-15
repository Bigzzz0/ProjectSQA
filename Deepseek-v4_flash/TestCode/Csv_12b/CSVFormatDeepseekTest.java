package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Arrays;

/**
 * Advanced white-box test suite for CSVFormat targeting maximum line/branch coverage
 * and specifically the known defect regarding duplicate empty header names.
 *
 * <h3>Branch & Defect Analysis Matrix</h3>
 * <ul>
 *   <li><b>Constructor validation (validate):</b>
 *     <ul>
 *       <li>delimiter == quote character</li>
 *       <li>delimiter == escape character</li>
 *       <li>delimiter == comment marker</li>
 *       <li>quote character == comment marker</li>
 *       <li>escape character == comment marker</li>
 *       <li>escapeCharacter == null && quoteMode == QuoteMode.NONE</li>
 *     </ul>
 *   </li>
 *   <li><b>Header duplicate check:</b> duplicates in header array (including empty strings) → IllegalArgumentException</li>
 *   <li><b>withX methods:</b> all return new format, check line break exceptions, preserve other fields</li>
 *   <li><b>Getters:</b> every field accessible, cloning of header, null handling</li>
 *   <li><b>equals/hashCode:</b> all fields compared, null safety, array equality</li>
 *   <li><b>isCommentMarkerSet / isEscapeCharacterSet / isNullStringSet / isQuoteCharacterSet:</b> true only when not null</li>
 *   <li><b>toString:</b> all present fields appear</li>
 *   <li><b>format:</b> basic formatting works</li>
 *   <li><b>Known defect (CSV-1):</b> Using allowMissingColumnNames=true with header containing duplicate empty strings
 *       should not throw IllegalArgumentException. The buggy version throws.</li>
 * </ul>
 */
public class CSVFormatDeepseekTest {

    // --------------------------------------------
    // Partition A: Core Functional Logic & State
    // --------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultFormat() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        assertEquals(',', fmt.getDelimiter());
        assertEquals(Character.valueOf('"'), fmt.getQuoteCharacter());
        assertEquals("\r\n", fmt.getRecordSeparator());
        assertTrue(fmt.getIgnoreEmptyLines());
        assertFalse(fmt.getIgnoreSurroundingSpaces());
        assertFalse(fmt.getSkipHeaderRecord());
        assertNull(fmt.getCommentMarker());
        assertNull(fmt.getEscapeCharacter());
        assertNull(fmt.getNullString());
        assertNull(fmt.getHeader());
        assertNull(fmt.getQuoteMode());
        assertFalse(fmt.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testRfc4180() {
        CSVFormat fmt = CSVFormat.RFC4180;
        assertFalse(fmt.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testExcelFormat() {
        CSVFormat fmt = CSVFormat.EXCEL;
        assertFalse(fmt.getIgnoreEmptyLines());
        assertEquals(Character.valueOf('"'), fmt.getQuoteCharacter());
    }

    @Test(timeout = 4000)
    public void testTdfFormat() {
        CSVFormat fmt = CSVFormat.TDF;
        assertEquals('\t', fmt.getDelimiter());
        assertTrue(fmt.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testMysqlFormat() {
        CSVFormat fmt = CSVFormat.MYSQL;
        assertEquals('\t', fmt.getDelimiter());
        assertEquals(Character.valueOf('\\'), fmt.getEscapeCharacter());
        assertFalse(fmt.getIgnoreEmptyLines());
        assertNull(fmt.getQuoteCharacter());
        assertEquals("\n", fmt.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testNewFormat() {
        CSVFormat fmt = CSVFormat.newFormat(';');
        assertEquals(';', fmt.getDelimiter());
        assertNull(fmt.getQuoteCharacter());
        assertNull(fmt.getCommentMarker());
        assertNull(fmt.getEscapeCharacter());
        assertNull(fmt.getNullString());
        assertNull(fmt.getHeader());
        assertNull(fmt.getRecordSeparator());
        assertFalse(fmt.getIgnoreSurroundingSpaces());
        assertFalse(fmt.getIgnoreEmptyLines());
        assertFalse(fmt.getSkipHeaderRecord());
        assertFalse(fmt.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testWithDelimiter() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withDelimiter(';');
        assertEquals(';', modified.getDelimiter());
        assertEquals(base.getQuoteCharacter(), modified.getQuoteCharacter());
        assertEquals(base.getRecordSeparator(), modified.getRecordSeparator());
        assertFalse(modified.equals(base));
    }

    @Test(timeout = 4000)
    public void testWithQuote() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withQuote('\'');
        assertEquals(Character.valueOf('\''), modified.getQuoteCharacter());
    }

    @Test(timeout = 4000)
    public void testWithQuoteNull() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withQuote(null);
        assertNull(modified.getQuoteCharacter());
    }

    @Test(timeout = 4000)
    public void testWithEscape() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withEscape('\\');
        assertEquals(Character.valueOf('\\'), modified.getEscapeCharacter());
    }

    @Test(timeout = 4000)
    public void testWithEscapeNull() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withEscape(null);
        assertNull(modified.getEscapeCharacter());
    }

    @Test(timeout = 4000)
    public void testWithCommentMarker() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), modified.getCommentMarker());
    }

    @Test(timeout = 4000)
    public void testWithCommentMarkerNull() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withCommentMarker(null);
        assertNull(modified.getCommentMarker());
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
        CSVFormat modified = base.withRecordSeparator("\r\n");
        assertEquals("\r\n", modified.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithNullString() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withNullString("NULL");
        assertEquals("NULL", modified.getNullString());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreEmptyLines() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withIgnoreEmptyLines(false);
        assertFalse(modified.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withIgnoreSurroundingSpaces(true);
        assertTrue(modified.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testWithSkipHeaderRecord() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withSkipHeaderRecord(true);
        assertTrue(modified.getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithAllowMissingColumnNames() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withAllowMissingColumnNames(true);
        assertTrue(modified.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testWithQuoteMode() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withQuoteMode(QuoteMode.MINIMAL);
        assertEquals(QuoteMode.MINIMAL, modified.getQuoteMode());
    }

    @Test(timeout = 4000)
    public void testWithHeaderVarargs() {
        CSVFormat base = CSVFormat.DEFAULT;
        String[] headers = {"A", "B", "C"};
        CSVFormat modified = base.withHeader(headers);
        assertArrayEquals(headers, modified.getHeader());
        // ensure independence
        headers[0] = "X";
        assertNotEquals("X", modified.getHeader()[0]);
    }

    @Test(timeout = 4000)
    public void testWithHeaderEmpty() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withHeader(); // empty array
        assertNotNull(modified.getHeader());
        assertEquals(0, modified.getHeader().length);
    }

    @Test(timeout = 4000)
    public void testWithHeaderNull() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withHeader((String[]) null);
        assertNull(modified.getHeader());
    }

    @Test(timeout = 4000)
    public void testIsCommentMarkerSet() {
        CSVFormat without = CSVFormat.DEFAULT;
        assertFalse(without.isCommentMarkerSet());
        CSVFormat with = without.withCommentMarker('#');
        assertTrue(with.isCommentMarkerSet());
    }

    @Test(timeout = 4000)
    public void testIsEscapeCharacterSet() {
        CSVFormat without = CSVFormat.DEFAULT;
        assertFalse(without.isEscapeCharacterSet());
        CSVFormat with = without.withEscape('\\');
        assertTrue(with.isEscapeCharacterSet());
    }

    @Test(timeout = 4000)
    public void testIsNullStringSet() {
        CSVFormat without = CSVFormat.DEFAULT;
        assertFalse(without.isNullStringSet());
        CSVFormat with = without.withNullString("NULL");
        assertTrue(with.isNullStringSet());
    }

    @Test(timeout = 4000)
    public void testIsQuoteCharacterSet() {
        CSVFormat without = CSVFormat.DEFAULT;
        assertTrue(without.isQuoteCharacterSet());
        CSVFormat noQuote = without.withQuote(null);
        assertFalse(noQuote.isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testGetHeaderReturnsClone() {
        CSVFormat fmt = CSVFormat.DEFAULT.withHeader("A", "B");
        String[] hdr = fmt.getHeader();
        hdr[0] = "Changed";
        assertArrayEquals(new String[]{"A", "B"}, fmt.getHeader());
    }

    @Test(timeout = 4000)
    public void testGetHeaderNull() {
        assertNull(CSVFormat.DEFAULT.getHeader());
    }

    @Test(timeout = 4000)
    public void testFormat() {
        String result = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    @Test(timeout = 4000)
    public void testFormatWithQuoting() {
        String result = CSVFormat.DEFAULT.format("a,b", "c");
        assertEquals("\"a,b\",c", result);
    }

    @Test(timeout = 4000)
    public void testToStringContainsDelimiter() {
        String str = CSVFormat.DEFAULT.toString();
        assertTrue(str.contains("Delimiter=<,>"));
    }

    @Test(timeout = 4000)
    public void testToStringContainsAllSetFlags() {
        CSVFormat fmt = CSVFormat.DEFAULT
                .withEscape('\\')
                .withCommentMarker('#')
                .withNullString("NIL")
                .withRecordSeparator("|")
                .withIgnoreEmptyLines(true)
                .withIgnoreSurroundingSpaces(true)
                .withSkipHeaderRecord(true)
                .withHeader("H1", "H2");
        String str = fmt.toString();
        assertTrue(str.contains("Escape=<\\>"));
        assertTrue(str.contains("CommentStart=<#>"));
        assertTrue(str.contains("NullString=<NIL>"));
        assertTrue(str.contains("RecordSeparator=<|>"));
        assertTrue(str.contains("EmptyLines:ignored"));
        assertTrue(str.contains("SurroundingSpaces:ignored"));
        assertTrue(str.contains("SkipHeaderRecord:true"));
        assertTrue(str.contains("Header:[H1, H2]"));
    }

    @Test(timeout = 4000)
    public void testToStringWithoutUnsetFlags() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        String str = fmt.toString();
        assertFalse(str.contains("Escape=<");
        assertFalse(str.contains("CommentStart=<");
        assertFalse(str.contains("NullString=<");
        assertFalse(str.contains("RecordSeparator=<"));
        // but default has record separator  assertTrue(str.contains("RecordSeparator=<"));
    }

    // --------------------------------------------
    // Partition B: Boundaries & Extreme Values
    // --------------------------------------------

    @Test(timeout = 4000)
    public void testWithDelimiterLineBreakThrows() {
        try {
            CSVFormat.DEFAULT.withDelimiter('\n');
            fail("Expected IllegalArgumentException for line break delimiter");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWithQuoteCharLineBreakThrows() {
        try {
            CSVFormat.DEFAULT.withQuote('\r');
            fail("Expected IllegalArgumentException for line break quote");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWithEscapeLineBreakThrows() {
        try {
            CSVFormat.DEFAULT.withEscape('\n');
            fail("Expected IllegalArgumentException for line break escape");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWithCommentMarkerLineBreakThrows() {
        try {
            CSVFormat.DEFAULT.withCommentMarker('\r');
            fail("Expected IllegalArgumentException for line break comment");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNewFormatLineBreakThrows() {
        try {
            CSVFormat.newFormat('\n');
            fail("Expected IllegalArgumentException for line break delimiter");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testHeaderWithDuplicateNamesThrows() {
        try {
            CSVFormat.DEFAULT.withHeader("A", "B", "A");
            fail("Expected IllegalArgumentException for duplicate header name");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("duplicate entry: 'A'"));
        }
    }

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
    public void testEqualsDifferentDelimiter() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withDelimiter(';');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentQuoteMode() {
        CSVFormat f1 = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NONE);
        CSVFormat f2 = f1.withQuoteMode(QuoteMode.MINIMAL);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentQuoteChar() {
        CSVFormat f1 = CSVFormat.DEFAULT.withQuote('\'');
        CSVFormat f2 = f1.withQuote('"');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentCommentMarker() {
        CSVFormat f1 = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVFormat f2 = f1.withCommentMarker('!');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentEscape() {
        CSVFormat f1 = CSVFormat.DEFAULT.withEscape('\\');
        CSVFormat f2 = f1.withEscape('!');
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentNullString() {
        CSVFormat f1 = CSVFormat.DEFAULT.withNullString("NIL");
        CSVFormat f2 = f1.withNullString("NULL");
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentHeader() {
        CSVFormat f1 = CSVFormat.DEFAULT.withHeader("A");
        CSVFormat f2 = f1.withHeader("B");
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentIgnoreSurroundingSpaces() {
        CSVFormat f1 = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVFormat f2 = f1.withIgnoreSurroundingSpaces(false);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentIgnoreEmptyLines() {
        CSVFormat f1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        CSVFormat f2 = f1.withIgnoreEmptyLines(true);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentSkipHeaderRecord() {
        CSVFormat f1 = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVFormat f2 = f1.withSkipHeaderRecord(false);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentRecordSeparator() {
        CSVFormat f1 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        CSVFormat f2 = f1.withRecordSeparator("\r");
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsAllFieldsSame() {
        CSVFormat f1 = CSVFormat.DEFAULT.withHeader("A").withEscape('\\').withIgnoreSurroundingSpaces(true);
        CSVFormat f2 = CSVFormat.DEFAULT.withHeader("A").withEscape('\\').withIgnoreSurroundingSpaces(true);
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistent() {
        CSVFormat fmt = CSVFormat.DEFAULT;
        int hc1 = fmt.hashCode();
        int hc2 = fmt.hashCode();
        assertEquals(hc1, hc2);
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferent() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = f1.withDelimiter(';');
        assertNotEquals(f1.hashCode(), f2.hashCode());
    }

    // --------------------------------------------
    // Partition C: Defect-Targeted Tests (CSV-1)
    // --------------------------------------------

    /**
     * This test directly targets the known defect: using allowMissingColumnNames=true
     * with a header that contains duplicate empty strings (e.g., when the CSV header line
     * has fewer columns than the data). The buggy version throws an IllegalArgumentException
     * because the empty strings are duplicates. The correct behavior (fixed) should allow
     * duplicate empty strings when allowMissingColumnNames is true.
     */
    @Test(timeout = 4000)
    public void testExcelHeaderCountLessThanData() {
        // Simulate the scenario from the defect: parsing a CSV with header "A,B,C" and data with 5 columns.
        // The header array would be ["A","B","C","",""] (empty strings for missing columns).
        // The EXCEL format has allowMissingColumnNames=true.
        try {
            CSVFormat fmt = CSVFormat.EXCEL.withHeader("A", "B", "C", "", "");
            // If we reach here, the bug is fixed (no exception).
            // On the buggy version, this line throws IllegalArgumentException.
        } catch (IllegalArgumentException e) {
            // If the exception is thrown, the test fails (bug present).
            fail("Unexpected IllegalArgumentException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testHeaderWithDuplicateEmptyStringsFailsWithoutAllowMissing() {
        // Even with allowMissingColumnNames=false, duplicate empty strings should throw? Actually,
        // the default (allowMissingColumnNames=false) should still throw because the validation
        // does not distinguish empty strings. This tests the existing behavior.
        try {
            CSVFormat.DEFAULT.withHeader("", "", "A");
            fail("Expected IllegalArgumentException for duplicate empty header names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("duplicate entry: ''"));
        }
    }

    // --------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // --------------------------------------------

    @Test(timeout = 4000)
    public void testValidateDelimiterEqualsQuote() {
        try {
            CSVFormat.DEFAULT.withQuote(',').withDelimiter(','); // order doesn't matter, constructor validates
            // Actually withDelimiter creates a new instance, so we need to create directly
            // Use private constructor indirectly via withQuote and withDelimiter: if both set same,
            // the constructor called by withDelimiter after withQuote will validate.
            // Better: create using newFormat and then withQuote.
            CSVFormat fmt = CSVFormat.newFormat(',').withQuote(',');
            fail("Expected IllegalArgumentException for delimiter == quote");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateDelimiterEqualsEscape() {
        try {
            CSVFormat.newFormat(',').withEscape(',');
            fail("Expected IllegalArgumentException for delimiter == escape");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateDelimiterEqualsCommentMarker() {
        try {
            CSVFormat.newFormat('#').withCommentMarker('#');
            fail("Expected IllegalArgumentException for delimiter == comment");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateQuoteEqualsCommentMarker() {
        try {
            CSVFormat.newFormat(',').withQuote('!').withCommentMarker('!');
            fail("Expected IllegalArgumentException for quote == comment");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateEscapeEqualsCommentMarker() {
        try {
            CSVFormat.newFormat(',').withEscape('#').withCommentMarker('#');
            fail("Expected IllegalArgumentException for escape == comment");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateNoEscapeAndQuoteModeNone() {
        try {
            CSVFormat.newFormat(',').withQuoteMode(QuoteMode.NONE);
            fail("Expected IllegalArgumentException for no escape and quoteMode NONE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No quotes mode set but no escape character is set"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateNoEscapeAndQuoteModeNoneWithEscapeSet() {
        // This should NOT throw because escape is set
        CSVFormat fmt = CSVFormat.newFormat(',').withEscape('\\').withQuoteMode(QuoteMode.NONE);
        assertNotNull(fmt);
    }

    @Test(timeout = 4000)
    public void testValidateNoEscapeAndQuoteModeNoneWithQuoteNull() {
        // If quote is null, quoteMode is irrelevant? Actually validate checks escape==null && quoteMode==NONE.
        // Even if quote is null, the condition triggers. In this case, it should still throw.
        try {
            CSVFormat.newFormat(',').withQuote(null).withQuoteMode(QuoteMode.NONE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --------------------------------------------
    // Partition E: Object Lifecycle & Contract
    // --------------------------------------------

    @Test(timeout = 4000)
    public void testSerializationWillNotBeTestedDirectlyButEqualsContract() {
        // We test equals reflexivity, symmetry, transitivity, consistency)
        CSVFormat f1 = CSVFormat.DEFAULT.withHeader("A").withEscape('\\').withIgnoreSurroundingSpaces(true);
        CSVFormat f2 = CSVFormat.DEFAULT.withHeader("A").withEscape('\\').withIgnoreSurroundingSpaces(true);
        CSVFormat f3 = CSVFormat.DEFAULT.withHeader("A").withEscape('\\').withIgnoreSurroundingSpaces(true);
        // reflexivity
        assertTrue(f1.equals(f1));
        // symmetry
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f1));
        // transitivity
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f3));
        assertTrue(f1.equals(f3));
        // consistency
        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testImmutability() {
        CSVFormat original = CSVFormat.DEFAULT;
        CSVFormat modified = original.withDelimiter(';');
        assertNotEquals(original.getDelimiter(), modified.getDelimiter());
        assertEquals(',', original.getDelimiter());
    }

    @Test(timeout = 4000)
    public void testWithHeaderPreservesOtherFields() {
        CSVFormat base = CSVFormat.DEFAULT.withDelimiter(';').withEscape('\\').withIgnoreSurroundingSpaces(true);
        CSVFormat withHeader = base.withHeader("X");
        assertEquals(';', withHeader.getDelimiter());
        assertEquals(Character.valueOf('\\'), withHeader.getEscapeCharacter());
        assertTrue(withHeader.getIgnoreSurroundingSpaces());
        assertArrayEquals(new String[]{"X"}, withHeader.getHeader());
    }
}