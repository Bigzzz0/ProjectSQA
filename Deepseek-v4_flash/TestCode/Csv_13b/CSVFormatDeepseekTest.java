package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * This test suite targets the CSVFormat class with the following partitions:
 *
 * Partition A – Core Functional Logic & State Transitions:
 *   - DEFAULT, RFC4180, EXCEL, TDF, MYSQL predefined formats.
 *   - Getters for all fields.
 *   - withX methods returning new immutable instances.
 *   - format() method for basic value formatting.
 *
 * Partition B – Boundary Value Analysis & Extremes:
 *   - Null/empty arguments: header=null, headerComments=null.
 *   - Characters: line break characters (LF, CR) passed to with methods and constructor.
 *   - Empty header array (valid) vs null header.
 *   - Boundary: delimiter, quote, escape, comment marker conflicts.
 *
 * Partition C – Defect-Targeted Branch Zone (known Defects4J bug):
 *   - MYSQL nullString = "\\N". Test that format(null) and getNullString() return "\\N".
 *   - Verifies output of null values for MYSQL format (defect: was printing "NULL" or null).
 *
 * Partition D – Exception & Defensive Guard Paths:
 *   - validate() throws IllegalArgumentException for:
 *     - Delimiter is line break
 *     - Delimiter equals quoteChar, escape, or commentMarker
 *     - quoteChar equals commentMarker
 *     - escape equals commentMarker
 *     - No escape with NONE quote mode
 *     - Duplicate header entries
 *   - withX methods throw for line break characters.
 *
 * Partition E – Object Lifecycle & Contract Integrity:
 *   - equals() consistency (null, same, different, symmetric)
 *   - hashCode() contract
 *   - toString() coverage (all components present/absent)
 */
public class CSVFormatDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testDefaultFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals("\r\n", format.getRecordSeparator());
        assertTrue(format.getIgnoreEmptyLines());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreHeaderCase());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertNull(format.getHeaderComments());
        assertNull(format.getQuoteMode());
    }

    @Test(timeout = 4000)
    public void testRfc4180Format() {
        CSVFormat format = CSVFormat.RFC4180;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertFalse(format.getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testExcelFormat() {
        CSVFormat format = CSVFormat.EXCEL;
        assertTrue(format.getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testTdfFormat() {
        CSVFormat format = CSVFormat.TDF;
        assertEquals('\t', format.getDelimiter());
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testMySqlFormat() {
        CSVFormat format = CSVFormat.MYSQL;
        assertEquals('\t', format.getDelimiter());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertEquals('\n', format.getRecordSeparator().charAt(0));
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getQuoteCharacter());
        assertEquals("\\N", format.getNullString());
    }

    @Test(timeout = 4000)
    public void testWithDelimiter() {
        CSVFormat base = CSVFormat.DEFAULT;
        CSVFormat modified = base.withDelimiter(';');
        assertEquals(';', modified.getDelimiter());
        // original unchanged
        assertEquals(',', base.getDelimiter());
    }

    @Test(timeout = 4000)
    public void testWithQuote() {
        CSVFormat modified = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), modified.getQuoteCharacter());
        // disabling quote
        CSVFormat noQuote = modified.withQuote(null);
        assertNull(noQuote.getQuoteCharacter());
    }

    @Test(timeout = 4000)
    public void testWithEscape() {
        CSVFormat modified = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), modified.getEscapeCharacter());
    }

    @Test(timeout = 4000)
    public void testWithCommentMarker() {
        CSVFormat modified = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), modified.getCommentMarker());
        assertTrue(modified.isCommentMarkerSet());
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparator() {
        CSVFormat modified = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", modified.getRecordSeparator());
        CSVFormat stringSep = modified.withRecordSeparator("CRLF");
        assertEquals("CRLF", stringSep.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithNullString() {
        CSVFormat modified = CSVFormat.DEFAULT.withNullString("null");
        assertEquals("null", modified.getNullString());
        assertTrue(modified.isNullStringSet());
    }

    @Test(timeout = 4000)
    public void testWithHeader() {
        CSVFormat modified = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        assertArrayEquals(new String[]{"A", "B", "C"}, modified.getHeader());
        // empty header (auto)
        CSVFormat emptyHeader = CSVFormat.DEFAULT.withHeader();
        assertNotNull(emptyHeader.getHeader());
        assertEquals(0, emptyHeader.getHeader().length);
    }

    @Test(timeout = 4000)
    public void testWithHeaderComments() {
        CSVFormat modified = CSVFormat.DEFAULT.withHeaderComments("comment1", "comment2");
        assertArrayEquals(new String[]{"comment1", "comment2"}, modified.getHeaderComments());
        // Object array with null element
        CSVFormat withNull = modified.withHeaderComments((Object) null);
        assertArrayEquals(new String[]{null}, withNull.getHeaderComments());
    }

    @Test(timeout = 4000)
    public void testWithSkipHeaderRecord() {
        assertTrue(CSVFormat.DEFAULT.withSkipHeaderRecord().getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.withSkipHeaderRecord(false).getSkipHeaderRecord());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreEmptyLines() {
        assertTrue(CSVFormat.DEFAULT.withIgnoreEmptyLines().getIgnoreEmptyLines());
        assertFalse(CSVFormat.DEFAULT.withIgnoreEmptyLines(false).getIgnoreEmptyLines());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreSurroundingSpaces() {
        assertTrue(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces().getIgnoreSurroundingSpaces());
        assertFalse(CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(false).getIgnoreSurroundingSpaces());
    }

    @Test(timeout = 4000)
    public void testWithAllowMissingColumnNames() {
        assertTrue(CSVFormat.DEFAULT.withAllowMissingColumnNames().getAllowMissingColumnNames());
        assertFalse(CSVFormat.DEFAULT.withAllowMissingColumnNames(false).getAllowMissingColumnNames());
    }

    @Test(timeout = 4000)
    public void testWithIgnoreHeaderCase() {
        assertTrue(CSVFormat.DEFAULT.withIgnoreHeaderCase().getIgnoreHeaderCase());
        assertFalse(CSVFormat.DEFAULT.withIgnoreHeaderCase(false).getIgnoreHeaderCase());
    }

    @Test(timeout = 4000)
    public void testWithQuoteMode() {
        CSVFormat modified = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, modified.getQuoteMode());
    }

    @Test(timeout = 4000)
    public void testIsMethods() {
        CSVFormat noExtra = CSVFormat.newFormat(',');
        assertFalse(noExtra.isCommentMarkerSet());
        assertFalse(noExtra.isEscapeCharacterSet());
        assertFalse(noExtra.isNullStringSet());
        assertFalse(noExtra.isQuoteCharacterSet());
    }

    @Test(timeout = 4000)
    public void testFormatBasic() {
        String result = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", result);
    }

    // ========== Partition B: Boundary & Edge Cases ==========

    @Test(timeout = 4000)
    public void testNewFormatWithLineBreakDelimiter() {
        try {
            CSVFormat.newFormat('\n');
            fail("Expected IllegalArgumentException for LF delimiter");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            CSVFormat.newFormat('\r');
            fail("Expected IllegalArgumentException for CR delimiter");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithQuoteLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(timeout = 4000)
    public void testNullHeader() {
        assertNull(CSVFormat.DEFAULT.withHeader((String[]) null).getHeader());
    }

    @Test(timeout = 4000)
    public void testEmptyHeader() {
        assertNotNull(CSVFormat.DEFAULT.withHeader().getHeader());
        assertEquals(0, CSVFormat.DEFAULT.withHeader().getHeader().length);
    }

    @Test(timeout = 4000)
    public void testNullHeaderComments() {
        assertNull(CSVFormat.DEFAULT.withHeaderComments((Object[]) null).getHeaderComments());
    }

    // ========== Partition C: Defect-Targeted Tests (Known Bug) ==========

    @Test(timeout = 4000)
    public void testMySqlNullStringValue() {
        // The known defect: MYSQL nullString should be "\\N", but bug causes null output
        assertEquals("\\N", CSVFormat.MYSQL.getNullString());
    }

    @Test(timeout = 4000)
    public void testMySqlNullOutput() {
        // Defect: format() with null argument should produce the nullString ("\\N"), but bug produces "NULL" or null
        // With MYSQL format, record separator is '\n'. format() trims to nullString.
        String output = CSVFormat.MYSQL.format((Object) null);
        assertEquals("\\N", output);
    }

    @Test(timeout = 4000)
    public void testMySqlNullInMiddle() {
        // Multiple values: first and last non-null, middle null
        String output = CSVFormat.MYSQL.format("a", null, "b");
        // Expected: "a\t\\N\tb" after trim (tab is delimiter, record separator \n)
        assertEquals("a\t\\N\tb", output);
    }

    @Test(timeout = 4000)
    public void testMySqlNullStringDefault() {
        // Another test from the defect: using CSVPrinter directly? We'll test via format to ensure nullString is used.
        CSVFormat format = CSVFormat.MYSQL;
        String nullStr = format.getNullString();
        assertNotNull(nullStr);
        // Ensure that when formatting a null, it uses the nullString
        assertEquals(nullStr, format.format((Object) null));
    }

    // ========== Partition D: Exception & Defensive Guards ==========

    @Test(timeout = 4000)
    public void testValidateDelimiterEqualsQuote() {
        try {
            CSVFormat.newFormat('"').withQuote('"');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateDelimiterEqualsEscape() {
        try {
            CSVFormat.newFormat('\\').withEscape('\\');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateDelimiterEqualsComment() {
        try {
            CSVFormat.newFormat('#').withCommentMarker('#');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateQuoteEqualsComment() {
        try {
            CSVFormat.newFormat(',').withQuote('*').withCommentMarker('*');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateEscapeEqualsComment() {
        try {
            CSVFormat.newFormat(',').withEscape('@').withCommentMarker('@');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cannot be the same"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateNoEscapeWithQuoteModeNone() {
        // QuoteMode.NONE reqires an escape character
        try {
            CSVFormat.newFormat(',').withQuoteMode(QuoteMode.NONE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No quotes mode set but no escape character is set"));
        }
    }

    @Test(timeout = 4000)
    public void testValidateDuplicateHeader() {
        try {
            CSVFormat.DEFAULT.withHeader("A", "B", "A");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("duplicate entry"));
        }
    }

    // ========== Partition E: Equals / HashCode / ToString ==========

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        CSVFormat f = CSVFormat.DEFAULT;
        assertTrue(f.equals(f));
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
    public void testEqualsSymmetric() {
        CSVFormat f1 = CSVFormat.DEFAULT.withDelimiter(';');
        CSVFormat f2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(f1, f2);
        assertEquals(f2, f1);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentFields() {
        CSVFormat base = CSVFormat.DEFAULT;
        assertNotEquals(base, base.withDelimiter(';'));
        assertNotEquals(base, base.withQuote('\''));
        assertNotEquals(base, base.withEscape('\\'));
        assertNotEquals(base, base.withCommentMarker('#'));
        assertNotEquals(base, base.withNullString("NULL"));
        assertNotEquals(base, base.withIgnoreEmptyLines(false));
        assertNotEquals(base, base.withIgnoreSurroundingSpaces(true));
        assertNotEquals(base, base.withSkipHeaderRecord(true));
        assertNotEquals(base, base.withHeader("A"));
        assertNotEquals(base, base.withRecordSeparator("\n"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        CSVFormat f = CSVFormat.DEFAULT.withHeader("X");
        assertEquals(f.hashCode(), f.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeEqualsContract() {
        CSVFormat f1 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        CSVFormat f2 = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        String s = CSVFormat.DEFAULT.toString();
        assertTrue(s.contains("Delimiter=<,>"));
        assertTrue(s.contains("QuoteChar=<\">"));
        assertTrue(s.contains("RecordSeparator=<"));
        assertTrue(s.contains("EmptyLines:ignored"));
        assertTrue(s.contains("SkipHeaderRecord:false"));

        // MYSQL format
        String mysqlStr = CSVFormat.MYSQL.toString();
        assertTrue(mysqlStr.contains("NullString=<\\N>"));
        assertTrue(mysqlStr.contains("Escape=<\\>"));
    }

    @Test(timeout = 4000)
    public void testPredefinedValues() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("Default"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("Excel"));
        assertEquals(CSVFormat.MYSQL, CSVFormat.valueOf("MySQL"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
        assertEquals(CSVFormat.TDF, CSVFormat.valueOf("TDF"));
    }

    // Edge: valueOf with non-existent name throws IllegalArgumentException
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        CSVFormat.valueOf("InvalidFormat");
    }
}