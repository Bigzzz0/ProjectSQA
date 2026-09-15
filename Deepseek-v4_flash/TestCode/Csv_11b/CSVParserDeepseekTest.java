package org.apache.commons.csv;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: CSVParser
 * 
 * Branches covered:
 * - initializeHeader():
 *   - formatHeader == null -> headerMap = null
 *   - formatHeader.length == 0 -> read header from first record
 *   - formatHeader.length > 0 -> use provided header
 *   - skipHeaderRecord true/false
 *   - headerRecord[i] null -> NPE (defect)
 *   - duplicate header detection (containsHeader && (!emptyHeader || (emptyHeader && !ignoreEmptyHeaders)))
 *   - empty header with ignoreEmptyHeaders true/false
 * - nextRecord():
 *   - Token types: TOKEN, EORECORD, EOF (isReady true/false), INVALID, COMMENT
 *   - record.clear() and reuse
 *   - comment accumulation (sb null vs non-null)
 * - addRecordValue():
 *   - nullString null vs non-null (equalsIgnoreCase)
 * - getHeaderMap(): null check
 * - iterator():
 *   - isClosed() true -> hasNext false, next throws NoSuchElementException
 *   - current null handling
 * - close(), isClosed()
 * 
 * Defect targeted: NullPointerException when header array contains null element
 * (initializeHeader() calls header.trim() without null check)
 */
public class CSVParserDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testParseSimpleString() throws IOException {
        final String csv = "a,b,c\n1,2,3\n4,5,6";
        final CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("1", records.get(0).get(0));
        assertEquals("6", records.get(1).get(2));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetHeaderMapWithHeader() throws IOException {
        final String csv = "col1,col2\nval1,val2";
        final CSVParser parser = CSVFormat.DEFAULT.withHeader("col1", "col2").parse(new StringReader(csv));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("col2"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetHeaderMapNull() throws IOException {
        final String csv = "a,b";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testRecordNumberAndLineNumber() throws IOException {
        final String csv = "a\nb\nc";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        assertEquals(1, parser.getCurrentLineNumber()); // before reading
        parser.nextRecord();
        assertEquals(1, parser.getRecordNumber());
        assertEquals(2, parser.getCurrentLineNumber());
        parser.nextRecord();
        assertEquals(2, parser.getRecordNumber());
        assertEquals(3, parser.getCurrentLineNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIterator() throws IOException {
        final String csv = "x,y\n1,2\n3,4";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        final Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next().get(0));
        assertTrue(it.hasNext());
        assertEquals("3", it.next().get(0));
        assertFalse(it.hasNext());
        parser.close();
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testIteratorAfterClose() throws IOException {
        final String csv = "a";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        parser.close();
        parser.iterator().next(); // should throw NoSuchElementException
    }

    @Test(timeout = 4000)
    public void testCloseAndIsClosed() throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(""));
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullFormat() throws IOException {
        new CSVParser(new StringReader(""), null);
    }

    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(""));
        assertFalse(parser.iterator().hasNext());
        assertTrue(parser.getRecords().isEmpty());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSingleRecord() throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader("only"));
        final CSVRecord rec = parser.nextRecord();
        assertNotNull(rec);
        assertEquals(1, rec.size());
        assertEquals("only", rec.get(0));
        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testQuotedFields() throws IOException {
        final String csv = "\"hello, world\",\"foo\"";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        final CSVRecord rec = parser.nextRecord();
        assertEquals("hello, world", rec.get(0));
        assertEquals("foo", rec.get(1));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNullStringHandling() throws IOException {
        final String csv = "a,\\N,c";
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("\\N");
        final CSVParser parser = format.parse(new StringReader(csv));
        final CSVRecord rec = parser.nextRecord();
        assertEquals("a", rec.get(0));
        assertNull(rec.get(1));
        assertEquals("c", rec.get(2));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTrailingEmptyField() throws IOException {
        final String csv = "a,b,";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        final CSVRecord rec = parser.nextRecord();
        assertEquals(3, rec.size());
        assertEquals("", rec.get(2));
        parser.close();
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHeaderWithNullElement() throws IOException {
        // This triggers the known defect: NPE when header array contains null
        // Expected correct behavior: throw IllegalArgumentException (or handle gracefully)
        // Buggy version throws NullPointerException -> test fails (reveals bug)
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("col1", null, "col3");
        final CSVParser parser = format.parse(new StringReader("a,b,c"));
        parser.close();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateHeader() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("dup", "dup");
        final CSVParser parser = format.parse(new StringReader("x,y"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyHeaderWithIgnoreEmptyHeaders() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("", "col").withIgnoreEmptyHeaders(true);
        final CSVParser parser = format.parse(new StringReader("a,b"));
        final Map<String, Integer> map = parser.getHeaderMap();
        assertNotNull(map);
        assertEquals(2, map.size()); // empty header is still added? Actually with ignoreEmptyHeaders, empty header is ignored? The code: if (containsHeader && (!emptyHeader || (emptyHeader && !ignoreEmptyHeaders))) -> if emptyHeader and ignoreEmptyHeaders, condition false, so no exception, but still added? Actually the condition only throws if duplicate and not ignored. So empty header is added. So map size 2.
        assertTrue(map.containsKey(""));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderReadFromFirstRecord() throws IOException {
        final String csv = "h1,h2\nv1,v2";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(); // header length 0 -> read from first line
        final CSVParser parser = format.parse(new StringReader(csv));
        final Map<String, Integer> map = parser.getHeaderMap();
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(0), map.get("h1"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSkipHeaderRecord() throws IOException {
        final String csv = "skip,this\nreal1,real2";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("a", "b").withSkipHeaderRecord(true);
        final CSVParser parser = format.parse(new StringReader(csv));
        final CSVRecord rec = parser.nextRecord();
        assertEquals("real1", rec.get(0));
        assertEquals("real2", rec.get(1));
        parser.close();
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidToken() throws IOException {
        // Simulate invalid parse sequence (e.g., unescaped quote)
        final String csv = "\"unclosed";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        parser.nextRecord(); // should throw IOException
    }

    @Test(timeout = 4000)
    public void testCommentIgnored() throws IOException {
        final String csv = "# comment\na,b";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = format.parse(new StringReader(csv));
        final CSVRecord rec = parser.nextRecord();
        assertNotNull(rec);
        assertEquals("a", rec.get(0));
        assertEquals("b", rec.get(1));
        // comment should be stored in record
        assertEquals("# comment", rec.getComment());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMultipleComments() throws IOException {
        final String csv = "# first\n# second\na,b";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = format.parse(new StringReader(csv));
        final CSVRecord rec = parser.nextRecord();
        assertEquals("# first\n# second", rec.getComment());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEOFWithReadyToken() throws IOException {
        // When last token is EOF but isReady true (e.g., trailing newline)
        final String csv = "a,b\n";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        final CSVRecord rec = parser.nextRecord();
        assertNotNull(rec);
        assertEquals(2, rec.size());
        assertNull(parser.nextRecord());
        parser.close();
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testGetRecordsEmptyCollection() throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader("a,b"));
        final List<CSVRecord> list = new java.util.ArrayList<>();
        final List<CSVRecord> result = parser.getRecords(list);
        assertSame(list, result);
        assertEquals(1, result.size());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveUnsupported() throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader("x"));
        final Iterator<CSVRecord> it = parser.iterator();
        it.next();
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMultipleCallsToGetRecords() throws IOException {
        final String csv = "a\nb\nc";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(csv));
        final List<CSVRecord> first = parser.getRecords();
        assertEquals(3, first.size());
        // Second call should return empty because stream exhausted
        final List<CSVRecord> second = parser.getRecords();
        assertTrue(second.isEmpty());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderMapCopyIndependence() throws IOException {
        final String csv = "x,y";
        final CSVParser parser = CSVFormat.DEFAULT.withHeader("a", "b").parse(new StringReader(csv));
        final Map<String, Integer> map1 = parser.getHeaderMap();
        map1.put("c", 2);
        final Map<String, Integer> map2 = parser.getHeaderMap();
        assertFalse(map2.containsKey("c"));
        parser.close();
    }
}