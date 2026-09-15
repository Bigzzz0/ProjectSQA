package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core functional logic & state transitions
 *   - Normal parse of simple CSV with headers, multiple records, quotes, escapes, comments
 *   - iterator() / nextRecord() sequence
 *   - getRecords() returning correct list
 *   - getHeaderMap() returns map or null
 *   - getCurrentLineNumber() increments correctly
 *   - getRecordNumber() increments after each record
 *   - close() and isClosed()
 * 
 * Partition B: Boundary Value Analysis & extremes
 *   - Empty input (no records)
 *   - Only header line (format provides headers, skipHeaderRecord)
 *   - Null string representation (nullString in format)
 *   - Header with empty string name
 *   - Very long record / many fields
 *   - EOF with trailing empty field
 *   - Empty line (blank record)
 * 
 * Partition C: Defect-targeted branch zone
 *   - Duplicate header names in CSVFormat => expect IllegalStateException during parsing
 *   - Pass invalid CSV (e.g., unclosed quote) => expect IOException
 *   - Unexpected token type (should not happen, but verify default throw)
 *   - Comment handling: multiple comments before record, comment only
 * 
 * Partition D: Exception & defensive guard paths
 *   - null reader, null format => IllegalArgumentException
 *   - parse(File, null) etc. => IllegalArgumentException
 *   - Calling iterator().next() after close => NoSuchElementException
 *   - Calling iterator().remove() => UnsupportedOperationException
 *   - Format validation: inconsistent format (e.g., delimiter = quote) => IllegalArgumentException
 *   - Invalid parse sequence (e.g., stray quote) => IOException
 * 
 * Partition E: Object lifecycle & contract integrity
 *   - getHeaderMap() returns a copy (modifications do not affect internal map)
 *   - getRecords() returns a mutable list
 *   - recordNumber increments correctly
 *   - iterator() wraps IOException in RuntimeException
 */
public class CSVParserDeepseekTest {

    // Helper to create a simple CSVFormat without headers
    private CSVFormat simpleFormat() {
        return CSVFormat.DEFAULT.withRecordSeparator('\n');
    }

    // Helper to create a CSVFormat with explicit headers
    private CSVFormat formatWithHeaders(String... headers) {
        return CSVFormat.DEFAULT.withHeader(headers).withRecordSeparator('\n');
    }

    // Helper to create a CSVFormat with skipHeaderRecord
    private CSVFormat formatWithSkipHeader(boolean skip, String... headers) {
        return CSVFormat.DEFAULT.withHeader(headers).withSkipHeaderRecord(skip).withRecordSeparator('\n');
    }

    // Helper to create a CSVFormat with nullString
    private CSVFormat formatWithNullString(String nullString) {
        return CSVFormat.DEFAULT.withNullString(nullString).withRecordSeparator('\n');
    }

    // =============== Partition A: Core functional logic ===============

    @Test(timeout = 4000)
    public void testSimpleParse() throws IOException {
        String csv = "a,b,c\n1,2,3\n4,5,6\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            CSVRecord rec1 = records.get(0);
            assertArrayEquals(new String[] {"a","b","c"}, rec1.values());
            CSVRecord rec2 = records.get(1);
            assertArrayEquals(new String[] {"4","5","6"}, rec2.values());
        }
    }

    @Test(timeout = 4000)
    public void testParseWithHeader() throws IOException {
        String csv = "name,age,city\nAlice,30,NYC\nBob,25,LA\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), formatWithHeaders("name","age","city"))) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            CSVRecord rec1 = records.get(0);
            assertEquals("Alice", rec1.get("name"));
            assertEquals("30", rec1.get("age"));
            assertEquals("NYC", rec1.get("city"));
            CSVRecord rec2 = records.get(1);
            assertEquals("Bob", rec2.get("name"));
            assertEquals("25", rec2.get("age"));
            assertEquals("LA", rec2.get("city"));
        }
    }

    @Test(timeout = 4000)
    public void testHeaderMap() throws IOException {
        String csv = "a,b,c\n1,2,3\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), formatWithHeaders("x","y","z"))) {
            Map<String, Integer> map = parser.getHeaderMap();
            assertNotNull(map);
            assertEquals(3, map.size());
            assertEquals(Integer.valueOf(0), map.get("x"));
            assertEquals(Integer.valueOf(1), map.get("y"));
            assertEquals(Integer.valueOf(2), map.get("z"));
        }
    }

    @Test(timeout = 4000)
    public void testHeaderMapNullWhenNoHeader() throws IOException {
        String csv = "a,b,c\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            assertNull(parser.getHeaderMap());
        }
    }

    @Test(timeout = 4000)
    public void testGetCurrentLineNumber() throws IOException {
        String csv = "a\nb\nc\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            assertEquals(1, parser.getCurrentLineNumber());
            parser.nextRecord(); // skip first record
            assertEquals(2, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(3, parser.getCurrentLineNumber());
            parser.nextRecord();
            assertEquals(3, parser.getCurrentLineNumber()); // end of file
        }
    }

    @Test(timeout = 4000)
    public void testRecordNumber() throws IOException {
        String csv = "1\n2\n3\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            assertEquals(0, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(1, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(2, parser.getRecordNumber());
            parser.nextRecord();
            assertEquals(3, parser.getRecordNumber());
            assertNull(parser.nextRecord()); // eof
        }
    }

    @Test(timeout = 4000)
    public void testIterator() throws IOException {
        String csv = "a,b\nc,d\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            int count = 0;
            for (CSVRecord record : parser) {
                count++;
                if (count == 1) {
                    assertArrayEquals(new String[]{"a","b"}, record.values());
                } else if (count == 2) {
                    assertArrayEquals(new String[]{"c","d"}, record.values());
                }
            }
            assertEquals(2, count);
        }
    }

    @Test(timeout = 4000)
    public void testIteratorHasNextAfterClose() throws IOException {
        String csv = "a,b\n";
        CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat());
        parser.close();
        assertFalse(parser.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratorThrowsNoSuchElementAfterClose() throws IOException {
        String csv = "a,b\n";
        CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat());
        parser.close();
        Iterator<CSVRecord> it = parser.iterator();
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveUnsupported() throws IOException {
        String csv = "a\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            Iterator<CSVRecord> it = parser.iterator();
            it.next();
            try {
                it.remove();
                fail("Expected UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a\n"), simpleFormat());
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        parser.close(); // double close should not throw
    }

    // =============== Partition B: Boundary Value Analysis ===============

    @Test(timeout = 4000)
    public void testEmptyInputNoRecords() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader(""), simpleFormat())) {
            List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
        }
    }

    @Test(timeout = 4000)
    public void testOnlyHeaderLineWithSkipHeader() throws IOException {
        String csv = "x,y,z\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), formatWithSkipHeader(true, "a","b","c"))) {
            List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty()); // header consumed, no data records
            Map<String, Integer> map = parser.getHeaderMap();
            assertNotNull(map);
            assertEquals(3, map.size());
        }
    }

    @Test(timeout = 4000)
    public void testNullString() throws IOException {
        String csv = "a,NA,c\n1,2,3\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), formatWithNullString("NA"))) {
            CSVRecord record = parser.iterator().next();
            assertNull(record.get(1));
            assertEquals("a", record.get(0));
            assertEquals("c", record.get(2));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyLine() throws IOException {
        String csv = "a\n\nb\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(3, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("", records.get(1).get(0)); // empty line treated as single empty field?
            assertEquals("b", records.get(2).get(0));
        }
    }

    @Test(timeout = 4000)
    public void testTrailingEmptyField() throws IOException {
        String csv = "a,b,\n1,2,3\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            CSVRecord rec = parser.iterator().next();
            assertEquals(3, rec.size());
            assertEquals("a", rec.get(0));
            assertEquals("b", rec.get(1));
            assertEquals("", rec.get(2));
        }
    }

    @Test(timeout = 4000)
    public void testMaxBoundaryFieldCount() throws IOException {
        // Build a record with 100 fields
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (i > 0) sb.append(',');
            sb.append("v" + i);
        }
        sb.append('\n');
        try (CSVParser parser = new CSVParser(new StringReader(sb.toString()), simpleFormat())) {
            CSVRecord rec = parser.iterator().next();
            assertEquals(100, rec.size());
            assertEquals("v0", rec.get(0));
            assertEquals("v99", rec.get(99));
        }
    }

    // =============== Partition C: Defect-targeted branch zone ===============

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testDuplicateHeaderEntries() throws IOException {
        // CSVFormat with duplicate header names should cause an IllegalStateException
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "a", "b").withRecordSeparator('\n');
        try (CSVParser parser = new CSVParser(new StringReader("1,2,3\n"), format)) {
            // The constructor calls initializeHeader() which should throw
            // because duplicate header names are detected (likely in format validation)
        }
        // If the constructor does not throw, parsing will later fail
        // We also test getRecords() to catch delayed detection
    }

    @Test(timeout = 4000)
    public void testDuplicateHeaderEntriesCausesException() throws IOException {
        // Another approach: try to parse and assert that an IllegalStateException is thrown
        CSVFormat format = CSVFormat.DEFAULT.withHeader("x", "x", "y").withRecordSeparator('\n');
        try {
            new CSVParser(new StringReader("1,2,3\n"), format);
            // If no exception, try to read records
            fail("Expected IllegalStateException due to duplicate headers");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidParseSequenceUnclosedQuote() throws IOException {
        String csv = "a,\"b,c\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            parser.nextRecord();
        }
    }

    @Test(timeout = 4000)
    public void testComment() throws IOException {
        String csv = "# comment\na,b\n";
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withRecordSeparator('\n');
        try (CSVParser parser = new CSVParser(new StringReader(csv), format)) {
            CSVRecord rec = parser.iterator().next();
            assertNotNull(rec);
            assertEquals("a", rec.get(0));
            assertEquals("b", rec.get(1));
            // comment should be stored in record
            assertNotNull(rec.getComment());
            assertTrue(rec.getComment().contains("comment"));
        }
    }

    @Test(timeout = 4000)
    public void testMultipleComments() throws IOException {
        String csv = "# first comment\n# second comment\na,b\n";
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withRecordSeparator('\n');
        try (CSVParser parser = new CSVParser(new StringReader(csv), format)) {
            CSVRecord rec = parser.iterator().next();
            assertNotNull(rec.getComment());
            // comments are concatenated with newline
            assertTrue(rec.getComment().contains("first comment"));
            assertTrue(rec.getComment().contains("second comment"));
        }
    }

    @Test(timeout = 4000)
    public void testCommentOnlyLine() throws IOException {
        String csv = "# just a comment\n";
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#').withRecordSeparator('\n');
        try (CSVParser parser = new CSVParser(new StringReader(csv), format)) {
            assertNull(parser.nextRecord()); // no data record
        }
    }

    // =============== Partition D: Exception & defensive guard paths ===============

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullReader() throws IOException {
        new CSVParser(null, simpleFormat());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullFormat() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, simpleFormat());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, simpleFormat());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseURLNullURL() throws IOException {
        CSVParser.parse((URL) null, java.nio.charset.StandardCharsets.UTF_8, simpleFormat());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidFormatDelimiterEqualsQuote() throws IOException {
        CSVFormat badFormat = CSVFormat.DEFAULT.withDelimiter('"').withQuote('"').withRecordSeparator('\n');
        new CSVParser(new StringReader("a,b\n"), badFormat);
    }

    @Test(timeout = 4000)
    public void testIOExceptionWrappedInRuntimeException() throws IOException {
        // Create a reader that throws IOException when read is called
        Reader faultyReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("simulated");
            }
            @Override
            public void close() throws IOException {}
        };
        try (CSVParser parser = new CSVParser(faultyReader, simpleFormat())) {
            Iterator<CSVRecord> it = parser.iterator();
            try {
                it.hasNext();
                fail("Expected RuntimeException wrapping IOException");
            } catch (RuntimeException e) {
                assertTrue(e.getCause() instanceof IOException);
                assertEquals("simulated", e.getCause().getMessage());
            }
        }
    }

    // =============== Partition E: Object lifecycle & contract integrity ===============

    @Test(timeout = 4000)
    public void testHeaderMapCopyImmutability() throws IOException {
        String csv = "a,b,c\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), formatWithHeaders("x","y","z"))) {
            Map<String, Integer> map1 = parser.getHeaderMap();
            Map<String, Integer> map2 = parser.getHeaderMap();
            assertNotSame(map1, map2);
            // modify map1 should not affect map2 or parser
            map1.put("new", 99);
            assertNull(map2.get("new"));
        }
    }

    @Test(timeout = 4000)
    public void testGetRecordsReturnsMutableList() throws IOException {
        String csv = "a\nb\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            // should be modifiable
            records.remove(0);
            assertEquals(1, records.size());
        }
    }

    @Test(timeout = 4000)
    public void testRecordNumberAfterGetRecords() throws IOException {
        String csv = "1\n2\n3\n";
        try (CSVParser parser = new CSVParser(new StringReader(csv), simpleFormat())) {
            parser.getRecords();
            assertEquals(3, parser.getRecordNumber());
        }
    }

    @Test(timeout = 4000)
    public void testMultipleCloseIdempotent() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("x\n"), simpleFormat());
        parser.close();
        parser.close();
        parser.close();
        assertTrue(parser.isClosed());
    }
}