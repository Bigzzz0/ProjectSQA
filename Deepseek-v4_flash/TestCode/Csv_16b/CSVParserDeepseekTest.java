package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Target Defect: testIteratorSequenceBreaking - expected:<[3]> but was:<[4]>
     * This defect occurs when the iterator's hasNext()/next() sequence is broken.
     * Specifically, when next() is called without calling hasNext() first, or when
     * the iterator state is not properly maintained across multiple iterations.
     * 
     * Key branches to cover:
     * 1. nextRecord() - TOKEN, COMMENT, INVALID, EOF branches
     * 2. addRecordValue() - lastRecord, trailingDelimiter, nullString branches
     * 3. initializeHeader() - null header, empty header, duplicate header branches
     * 4. iterator() - hasNext()/next() state transitions
     * 5. getRecords() - empty list, single record, multiple records
     * 6. getHeaderMap() - null header, populated header
     * 7. getCurrentLineNumber() - various line positions
     * 8. getFirstEndOfLine() - CRLF, LF, CR
     * 9. close() - open/closed states
     * 10. recordNumber - initial value, increments
     * 
     * Boundary conditions:
     * - Empty input string
     * - Single record without newline
     * - Multiple records with various line endings
     * - Header with/without case sensitivity
     * - Null string values
     * - Trailing delimiters
     * - Empty fields
     * - Quoted fields with embedded delimiters/newlines
     * - Comments
     * - Invalid token sequences
     * - Closed parser state
     * - Iterator misuse (next() without hasNext())
     */

    // Partition A: Core Functional Logic & State Transitions

    @Test(timeout = 4000)
    public void testBasicParsing() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b,c\n1,2,3\n4,5,6"), CSVFormat.DEFAULT);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        
        CSVRecord first = records.get(0);
        assertEquals(3, first.size());
        assertEquals("1", first.get(0));
        assertEquals("2", first.get(1));
        assertEquals("3", first.get(2));
        
        CSVRecord second = records.get(1);
        assertEquals("4", second.get(0));
        assertEquals("5", second.get(1));
        assertEquals("6", second.get(2));
        
        assertEquals(2, parser.getRecordNumber());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
        assertEquals(0, parser.getRecordNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSingleRecordNoNewline() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b,c"), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("c", records.get(0).get(2));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderParsing() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("col1", "col2", "col3");
        CSVParser parser = new CSVParser(new StringReader("1,2,3\n4,5,6"), format);
        
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("col2"));
        assertEquals(Integer.valueOf(2), headerMap.get("col3"));
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get("col1"));
        assertEquals("5", records.get(1).get("col2"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderFromFirstRecord() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("h1,h2,h3\n1,2,3"), 
                CSVFormat.DEFAULT.withHeader());
        
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("h1"));
        assertEquals(Integer.valueOf(1), headerMap.get("h2"));
        assertEquals(Integer.valueOf(2), headerMap.get("h3"));
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("1", records.get(0).get("h1"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCaseInsensitiveHeader() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Name", "Age")
                .withIgnoreHeaderCase(true);
        CSVParser parser = new CSVParser(new StringReader("John,30\nJane,25"), format);
        
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("name"));
        assertEquals(Integer.valueOf(1), headerMap.get("age"));
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals("John", records.get(0).get("NAME"));
        assertEquals("25", records.get(1).get("AGE"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNullStringHandling() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        CSVParser parser = new CSVParser(new StringReader("a,NULL,c\nNULL,2,3"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertNull(records.get(0).get(1));
        assertNull(records.get(1).get(0));
        assertEquals("a", records.get(0).get(0));
        assertEquals("c", records.get(0).get(2));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTrimValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        CSVParser parser = new CSVParser(new StringReader("  a  ,  b  ,  c  \n  1  ,  2  ,  3  "), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("b", records.get(0).get(1));
        assertEquals("1", records.get(1).get(0));
        assertEquals("3", records.get(1).get(2));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testQuotedFields() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("\"a,b\",\"c\nd\",e\n1,2,3"), CSVFormat.DEFAULT);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a,b", records.get(0).get(0));
        assertEquals("c\nd", records.get(0).get(1));
        assertEquals("e", records.get(0).get(2));
        assertEquals(2, parser.getCurrentLineNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testComments() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = new CSVParser(new StringReader("# comment\n1,2,3\n# another\n4,5,6"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("4", records.get(1).get(0));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTrailingDelimiter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter();
        CSVParser parser = new CSVParser(new StringReader("1,2,3,\n4,5,6,"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals(3, records.get(1).size());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyFields() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,,c\n,2,\n,,3"), CSVFormat.DEFAULT);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        assertEquals("", records.get(0).get(1));
        assertEquals("", records.get(1).get(0));
        assertEquals("", records.get(1).get(2));
        assertEquals("", records.get(2).get(0));
        assertEquals("", records.get(2).get(1));
        parser.close();
    }

    // Partition B: Boundary Value Analysis & Extremes

    @Test(timeout = 4000)
    public void testNullReader() {
        try {
            new CSVParser(null, CSVFormat.DEFAULT);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test(timeout = 4000)
    public void testNullFormat() {
        try {
            new CSVParser(new StringReader("a,b,c"), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test(timeout = 4000)
    public void testNullStringParse() {
        try {
            CSVParser.parse((String) null, CSVFormat.DEFAULT);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test(timeout = 4000)
    public void testVeryLongRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(',');
            sb.append("value").append(i);
        }
        sb.append('\n');
        
        CSVParser parser = new CSVParser(new StringReader(sb.toString()), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals(1000, records.get(0).size());
        assertEquals("value0", records.get(0).get(0));
        assertEquals("value999", records.get(0).get(999));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testManyRecords() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(i).append(',').append(i * 2).append('\n');
        }
        
        CSVParser parser = new CSVParser(new StringReader(sb.toString()), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(100, records.size());
        assertEquals(99, parser.getRecordNumber());
        assertEquals("99", records.get(99).get(0));
        assertEquals("198", records.get(99).get(1));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLineEndings() throws IOException {
        // CRLF
        CSVParser parser1 = new CSVParser(new StringReader("a,b\r\n1,2\r\n"), CSVFormat.DEFAULT);
        assertEquals("\r\n", parser1.getFirstEndOfLine());
        assertEquals(2, parser1.getRecords().size());
        parser1.close();
        
        // LF
        CSVParser parser2 = new CSVParser(new StringReader("a,b\n1,2\n"), CSVFormat.DEFAULT);
        assertEquals("\n", parser2.getFirstEndOfLine());
        assertEquals(2, parser2.getRecords().size());
        parser2.close();
        
        // CR
        CSVParser parser3 = new CSVParser(new StringReader("a,b\r1,2\r"), CSVFormat.DEFAULT);
        assertEquals("\r", parser3.getFirstEndOfLine());
        assertEquals(2, parser3.getRecords().size());
        parser3.close();
    }

    @Test(timeout = 4000)
    public void testCharacterOffset() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("abc\ndef\nghi"), CSVFormat.DEFAULT, 10, 5);
        assertEquals(10, parser.getCurrentLineNumber());
        assertEquals(4, parser.getRecordNumber());
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        assertEquals(7, parser.getRecordNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDuplicateHeader() {
        try {
            CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "a");
            new CSVParser(new StringReader("1,2"), format);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test(timeout = 4000)
    public void testEmptyHeaderWithAllowMissing() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames(true);
        CSVParser parser = new CSVParser(new StringReader("1,2,3"), format);
        
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        parser.close();
    }

    // Partition C: Defect-Targeted Branch Zone

    @Test(timeout = 4000)
    public void testIteratorSequenceBreaking() throws IOException {
        // This test targets the specific defect: testIteratorSequenceBreaking
        // expected:<[3]> but was:<[4]>
        // The bug occurs when the iterator's next() is called without hasNext()
        // or when the iterator state is not properly maintained.
        
        CSVParser parser = new CSVParser(new StringReader("1,2,3\n4,5,6\n7,8,9"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        
        // Call next() without calling hasNext() first
        CSVRecord first = iterator.next();
        assertEquals("1", first.get(0));
        
        // Call hasNext() then next()
        assertTrue(iterator.hasNext());
        CSVRecord second = iterator.next();
        assertEquals("4", second.get(0));
        
        // Call next() again without hasNext()
        CSVRecord third = iterator.next();
        assertEquals("7", third.get(0));
        
        // Verify no more records
        assertFalse(iterator.hasNext());
        
        // Verify record count
        assertEquals(3, parser.getRecordNumber());
        
        // Try to call next() when no more records
        try {
            iterator.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIteratorHasNextAfterClose() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("1,2,3\n4,5,6"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        
        assertTrue(iterator.hasNext());
        iterator.next();
        parser.close();
        
        // After close, hasNext() should return false
        assertFalse(iterator.hasNext());
        
        // next() should throw NoSuchElementException
        try {
            iterator.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveUnsupported() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("1,2,3"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        
        assertTrue(iterator.hasNext());
        iterator.next();
        
        try {
            iterator.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIteratorNextWithoutHasNextMultiple() throws IOException {
        // This test specifically targets the sequence breaking defect
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d\ne,f\ng,h"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        
        // Skip hasNext() entirely and call next() repeatedly
        CSVRecord rec1 = iterator.next();
        assertEquals("a", rec1.get(0));
        
        CSVRecord rec2 = iterator.next();
        assertEquals("c", rec2.get(0));
        
        CSVRecord rec3 = iterator.next();
        assertEquals("e", rec3.get(0));
        
        CSVRecord rec4 = iterator.next();
        assertEquals("g", rec4.get(0));
        
        // Verify exactly 4 records were read
        assertEquals(4, parser.getRecordNumber());
        
        // No more records
        try {
            iterator.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIteratorMixedSequence() throws IOException {
        // Mix of hasNext() and next() calls to test state transitions
        CSVParser parser = new CSVParser(new StringReader("1\n2\n3\n4\n5"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        
        assertTrue(iterator.hasNext());
        assertEquals("1", iterator.next().get(0));
        
        assertEquals("2", iterator.next().get(0));
        
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("3", iterator.next().get(0));
        
        assertEquals("4", iterator.next().get(0));
        
        assertTrue(iterator.hasNext());
        assertEquals("5", iterator.next().get(0));
        
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        
        assertEquals(5, parser.getRecordNumber());
        parser.close();
    }

    // Partition D: Exception & Defensive Guard Paths

    @Test(timeout = 4000)
    public void testInvalidTokenSequence() {
        // Create a format that would cause invalid token sequences
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        
        try {
            CSVParser parser = new CSVParser(new StringReader("\"unterminated"), format);
            parser.getRecords();
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected - unterminated quote
        }
    }

    @Test(timeout = 4000)
    public void testClosedParserNextRecord() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("1,2,3"), CSVFormat.DEFAULT);
        parser.close();
        
        Iterator<CSVRecord> iterator = parser.iterator();
        assertFalse(iterator.hasNext());
        
        try {
            iterator.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRecordsAfterClose() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("1,2,3"), CSVFormat.DEFAULT);
        parser.close();
        
        try {
            parser.getRecords();
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected - parser is closed
        }
    }

    @Test(timeout = 4000)
    public void testNullHeaderMap() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("1,2,3"), CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetFirstEndOfLineEmpty() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        assertNull(parser.getFirstEndOfLine());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testRecordNumberInitialValue() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT);
        assertEquals(0, parser.getRecordNumber());
        
        parser.nextRecord();
        assertEquals(1, parser.getRecordNumber());
        
        parser.nextRecord();
        assertEquals(2, parser.getRecordNumber());
        
        parser.close();
    }

    // Partition E: Object Lifecycle & Contract Integrity

    @Test(timeout = 4000)
    public void testCloseIdempotent() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("1,2,3"), CSVFormat.DEFAULT);
        parser.close();
        parser.close(); // Should not throw
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testGetRecordsReturnsEmptyList() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertNotNull(records);
        assertTrue(records.isEmpty());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderMapCopy() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b");
        CSVParser parser = new CSVParser(new StringReader("1,2"), format);
        
        Map<String, Integer> headerMap1 = parser.getHeaderMap();
        Map<String, Integer> headerMap2 = parser.getHeaderMap();
        
        assertNotSame(headerMap1, headerMap2);
        assertEquals(headerMap1, headerMap2);
        
        // Modify the returned map - should not affect internal state
        headerMap1.put("c", 2);
        assertEquals(2, parser.getHeaderMap().size());
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMultipleIterators() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("1,2\n3,4\n5,6"), CSVFormat.DEFAULT);
        
        Iterator<CSVRecord> it1 = parser.iterator();
        Iterator<CSVRecord> it2 = parser.iterator();
        
        // Both iterators should start from the beginning
        assertTrue(it1.hasNext());
        assertTrue(it2.hasNext());
        
        CSVRecord rec1 = it1.next();
        assertEquals("1", rec1.get(0));
        
        CSVRecord rec2 = it2.next();
        assertEquals("1", rec2.get(0));
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testParseStaticMethods() throws IOException {
        // Test parse(String, CSVFormat)
        CSVParser parser1 = CSVParser.parse("a,b\n1,2", CSVFormat.DEFAULT);
        assertEquals(1, parser1.getRecords().size());
        parser1.close();
        
        // Test parse(Reader, CSVFormat)
        CSVParser parser2 = CSVParser.parse(new StringReader("a,b\n1,2"), CSVFormat.DEFAULT);
        assertEquals(1, parser2.getRecords().size());
        parser2.close();
    }

    @Test(timeout = 4000)
    public void testSkipHeaderRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("h1", "h2").withSkipHeaderRecord(true);
        CSVParser parser = new CSVParser(new StringReader("h1,h2\n1,2\n3,4"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get("h1"));
        assertEquals("4", records.get(1).get("h2"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIgnoreEmptyLines() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVParser parser = new CSVParser(new StringReader("1,2\n\n3,4\n\n5,6"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("3", records.get(1).get(0));
        assertEquals("5", records.get(2).get(0));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testRecordWithComment() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        CSVParser parser = new CSVParser(new StringReader("1,2\n#comment\n3,4"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("3", records.get(1).get(0));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEscapeCharacter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        CSVParser parser = new CSVParser(new StringReader("a\\,b,c\nd,e,f"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a,b", records.get(0).get(0));
        assertEquals("c", records.get(0).get(1));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDelimiterVariations() throws IOException {
        // Semicolon delimiter
        CSVFormat semicolonFormat = CSVFormat.DEFAULT.withDelimiter(';');
        CSVParser parser1 = new CSVParser(new StringReader("a;b;c\n1;2;3"), semicolonFormat);
        assertEquals(1, parser1.getRecords().size());
        assertEquals("a", parser1.getRecords().get(0).get(0));
        parser1.close();
        
        // Tab delimiter
        CSVFormat tabFormat = CSVFormat.DEFAULT.withDelimiter('\t');
        CSVParser parser2 = new CSVParser(new StringReader("a\tb\tc\n1\t2\t3"), tabFormat);
        assertEquals(1, parser2.getRecords().size());
        assertEquals("b", parser2.getRecords().get(0).get(1));
        parser2.close();
    }

    @Test(timeout = 4000)
    public void testRecordNumberWithOffset() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d"), CSVFormat.DEFAULT, 0, 10);
        assertEquals(9, parser.getRecordNumber());
        
        parser.nextRecord();
        assertEquals(10, parser.getRecordNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetCurrentLineNumberWithMultiline() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,\"b\nc\",d\ne,f,g"), CSVFormat.DEFAULT);
        
        parser.nextRecord();
        assertEquals(2, parser.getCurrentLineNumber());
        
        parser.nextRecord();
        assertEquals(3, parser.getCurrentLineNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderWithNullValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b", "c").withNullString("NULL");
        CSVParser parser = new CSVParser(new StringReader("1,NULL,3\nNULL,5,NULL"), format);
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertNull(records.get(0).get("b"));
        assertNull(records.get(1).get("a"));
        assertNull(records.get(1).get("c"));
        assertEquals("1", records.get(0).get("a"));
        parser.close();
    }
}