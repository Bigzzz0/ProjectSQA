/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects & Branches:
 * 1. DEFECT CSV-122 / testHeaderMissingWithNull:
 *    - In initializeHeader(), headerRecord values can be null (e.g. when withNullString("") is set
 *      and an empty column exists on the header line). Calling header.trim() triggers a NullPointerException.
 *    - Targeted by: testHeaderMissingWithNull, testHeaderMissingWithNullRecordValues, testHeaderWithExplicitNullName.
 * 2. initializeHeader() decision branches:
 *    - formatHeader == null: returns null header map.
 *    - formatHeader.length == 0:
 *        * nextRecord() returns null (empty input) -> empty headerMap.
 *        * nextRecord() returns values -> maps header values to 0-based indices.
 *    - formatHeader.length > 0:
 *        * skipHeaderRecord == true -> calls nextRecord() to skip line.
 *        * skipHeaderRecord == false -> does not skip first line.
 *    - Header validation:
 *        * duplicate header name -> throws IllegalArgumentException.
 *        * duplicate empty headers with ignoreEmptyHeaders=true -> allowed.
 *        * duplicate empty headers with ignoreEmptyHeaders=false -> throws IllegalArgumentException.
 * 3. nextRecord() token processing:
 *    - TOKEN: calls addRecordValue(), loop continues.
 *    - EORECORD: calls addRecordValue(), terminates loop.
 *    - EOF with isReady == true: calls addRecordValue(), terminates loop.
 *    - EOF with isReady == false: terminates loop without adding value.
 *    - INVALID: throws IOException("(line X) invalid parse sequence").
 *    - COMMENT: accumulates comments across multiple comment lines, sets sb.
 * 4. addRecordValue() nullString matching:
 *    - nullString == null: adds literal value.
 *    - nullString != null: case-insensitive match replaced by null, non-match preserved.
 * 5. Iterator implementation:
 *    - hasNext() / next() lifecycle, prefetching, and state transitions.
 *    - hasNext() idempotency.
 *    - next() when closed -> NoSuchElementException("CSVParser has been closed").
 *    - next() when exhausted -> NoSuchElementException("No more CSV records available").
 *    - remove() -> UnsupportedOperationException.
 *    - IOException inside iterator -> wraps in RuntimeException.
 * 6. Static factories and defensive parameter guards:
 *    - parse(File, Charset, CSVFormat), parse(String, CSVFormat), parse(URL, Charset, CSVFormat).
 *    - null guards throw IllegalArgumentException.
 * 7. Encapsulation & Resource management:
 *    - close() idempotent, isClosed() reflect lexer status.
 *    - getHeaderMap() defensive copy verification.
 */
public class CSVParserGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: org.apache.commons.csv.CSVParserTest::testHeaderMissingWithNull
     * When nullString is defined and matches an empty field on the header row,
     * headerRecord[i] becomes null. A defective implementation performs header.trim(),
     * throwing NullPointerException instead of properly handling or assigning the column.
     */
    @Test(timeout = 4000)
    public void testHeaderMissingWithNull() throws Exception {
        final Reader in = new StringReader("a,,c");
        final CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withHeader().withNullString(""));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull("Header map should be created", headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("a"));
        assertEquals(Integer.valueOf(2), headerMap.get("c"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderMissingWithNullRecordValues() throws Exception {
        final Reader in = new StringReader("a,,c\n1,2,3");
        final CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withHeader().withNullString(""));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("1", records.get(0).get("a"));
        assertEquals("3", records.get(0).get("c"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderWithExplicitNullName() throws Exception {
        final Reader in = new StringReader("v1,v2,v3");
        final CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withHeader("h1", null, "h3"));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("h1"));
        assertEquals(Integer.valueOf(2), headerMap.get("h3"));
        parser.close();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseStringBasic() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());

        final CSVRecord rec1 = records.get(0);
        assertEquals(1L, rec1.getRecordNumber());
        assertEquals("a", rec1.get(0));
        assertEquals("b", rec1.get(1));
        assertEquals("c", rec1.get(2));

        final CSVRecord rec2 = records.get(1);
        assertEquals(2L, rec2.getRecordNumber());
        assertEquals("1", rec2.get(0));
        assertEquals("2", rec2.get(1));
        assertEquals("3", rec2.get(2));
        assertEquals(2L, parser.getRecordNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderAutoDetection() throws Exception {
        final CSVParser parser = CSVParser.parse("Name,Score,Grade\nAlice,95,A\nBob,82,B", CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("Name"));
        assertEquals(Integer.valueOf(1), headerMap.get("Score"));
        assertEquals(Integer.valueOf(2), headerMap.get("Grade"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("Alice", records.get(0).get("Name"));
        assertEquals("95", records.get(0).get("Score"));
        assertEquals("Bob", records.get(1).get("Name"));
        assertEquals("82", records.get(1).get("Score"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderSkipHeaderRecordTrue() throws Exception {
        final CSVParser parser = CSVParser.parse("SkipMe1,SkipMe2\n10,20\n30,40",
                CSVFormat.DEFAULT.withHeader("ColA", "ColB").withSkipHeaderRecord(true));
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("ColA"));
        assertEquals(Integer.valueOf(1), headerMap.get("ColB"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("10", records.get(0).get("ColA"));
        assertEquals("20", records.get(0).get("ColB"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderSkipHeaderRecordFalse() throws Exception {
        final CSVParser parser = CSVParser.parse("FirstRowValA,FirstRowValB\n10,20",
                CSVFormat.DEFAULT.withHeader("ColA", "ColB").withSkipHeaderRecord(false));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("FirstRowValA", records.get(0).get("ColA"));
        assertEquals("FirstRowValB", records.get(0).get("ColB"));
        assertEquals("10", records.get(1).get("ColA"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNullStringReplacement() throws Exception {
        final CSVParser parser = CSVParser.parse("a,NULL,c\nnull,b,NULL", CSVFormat.DEFAULT.withNullString("null"));
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());

        assertNull(records.get(0).get(1));
        assertEquals("a", records.get(0).get(0));
        assertEquals("c", records.get(0).get(2));

        assertNull(records.get(1).get(0));
        assertEquals("b", records.get(1).get(1));
        assertNull(records.get(1).get(2));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMultiLineRecordAndLineNumber() throws Exception {
        final String csv = "\"line1\nline2\",val2\r\nval3,val4";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        final CSVRecord rec1 = parser.nextRecord();
        assertNotNull(rec1);
        assertEquals("line1\nline2", rec1.get(0));
        assertEquals("val2", rec1.get(1));
        assertEquals(2L, parser.getCurrentLineNumber());

        final CSVRecord rec2 = parser.nextRecord();
        assertNotNull(rec2);
        assertEquals("val3", rec2.get(0));
        assertEquals("val4", rec2.get(1));
        assertEquals(3L, parser.getCurrentLineNumber());

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCommentsAccumulation() throws Exception {
        final String csv = "# First comment\n# Second comment\na,b\n# Third comment\nc,d";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withCommentMarker('#'));

        final CSVRecord rec1 = parser.nextRecord();
        assertNotNull(rec1);
        assertEquals("First comment\nSecond comment", rec1.getComment());
        assertEquals("a", rec1.get(0));
        assertEquals("b", rec1.get(1));

        final CSVRecord rec2 = parser.nextRecord();
        assertNotNull(rec2);
        assertEquals("Third comment", rec2.getComment());
        assertEquals("c", rec2.get(0));
        assertEquals("d", rec2.get(1));

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetRecordsWithCustomCollection() throws Exception {
        final CSVParser parser = CSVParser.parse("x,y\n1,2\n3,4", CSVFormat.DEFAULT);
        final LinkedList<CSVRecord> customList = new LinkedList<CSVRecord>();
        final LinkedList<CSVRecord> returnedList = parser.getRecords(customList);

        assertSame(customList, returnedList);
        assertEquals(2, returnedList.size());
        assertEquals("x", returnedList.get(0).get(0));
        assertEquals("3", returnedList.get(1).get(0));
        parser.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyInput() throws Exception {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
        assertNull(parser.nextRecord());
        assertEquals(0, parser.getRecords().size());
        assertEquals(0L, parser.getRecordNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyInputWithHeader() throws Exception {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertTrue(headerMap.isEmpty());
        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTrailingDelimiter() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b,\n1,2,", CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals(3, records.get(0).size());
        assertEquals("", records.get(0).get(2));
        assertEquals("", records.get(1).get(2));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderMapDefensiveCopy() throws Exception {
        final CSVParser parser = CSVParser.parse("A,B\n1,2", CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> map1 = parser.getHeaderMap();
        assertNotNull(map1);
        map1.put("MUTATION", 999);

        final Map<String, Integer> map2 = parser.getHeaderMap();
        assertFalse("Internal headerMap must be protected from external modification",
                map2.containsKey("MUTATION"));
        parser.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullReader() throws Exception {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFormat() throws Exception {
        new CSVParser(new StringReader(""), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullString() throws Exception {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullFormat() throws Exception {
        CSVParser.parse("a,b,c", (CSVFormat) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFile() throws Exception {
        CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFormat() throws Exception {
        final File temp = File.createTempFile("test", ".csv");
        temp.deleteOnExit();
        try {
            CSVParser.parse(temp, Charset.defaultCharset(), null);
        } finally {
            temp.delete();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullUrl() throws Exception {
        CSVParser.parse((URL) null, Charset.defaultCharset(), CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullCharset() throws Exception {
        final File temp = File.createTempFile("test", ".csv");
        temp.deleteOnExit();
        try {
            CSVParser.parse(temp.toURI().toURL(), null, CSVFormat.DEFAULT);
        } finally {
            temp.delete();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullFormat() throws Exception {
        final File temp = File.createTempFile("test", ".csv");
        temp.deleteOnExit();
        try {
            CSVParser.parse(temp.toURI().toURL(), Charset.defaultCharset(), null);
        } finally {
            temp.delete();
        }
    }

    @Test(timeout = 4000)
    public void testDuplicateHeaderThrowsException() throws Exception {
        try {
            CSVParser.parse("colA,colA\n1,2", CSVFormat.DEFAULT.withHeader());
            fail("Expected IllegalArgumentException for duplicate non-empty header");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }

    @Test(timeout = 4000)
    public void testDuplicateEmptyHeadersWithoutIgnoreThrows() throws Exception {
        try {
            CSVParser.parse(",\n1,2", CSVFormat.DEFAULT.withHeader().withIgnoreEmptyHeaders(false));
            fail("Expected IllegalArgumentException for duplicate empty headers without ignore flag");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }

    @Test(timeout = 4000)
    public void testDuplicateEmptyHeadersWithIgnoreAllowed() throws Exception {
        final CSVParser parser = CSVParser.parse(",\n1,2", CSVFormat.DEFAULT.withHeader().withIgnoreEmptyHeaders(true));
        final Map<String, Integer> map = parser.getHeaderMap();
        assertNotNull(map);
        assertTrue(map.containsKey(""));
        parser.close();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testInvalidTokenSequenceThrowsIOException() throws Exception {
        // Unterminated quote triggers Token.Type.INVALID in Lexer, causing IOException in CSVParser
        final CSVParser parser = CSVParser.parse("\"unclosed quote", CSVFormat.DEFAULT);
        parser.nextRecord();
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Iterator, & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFileLifecycle() throws Exception {
        final File tempFile = File.createTempFile("csv_test_", ".csv");
        tempFile.deleteOnExit();
        try {
            final PrintWriter pw = new PrintWriter(tempFile, "UTF-8");
            pw.println("H1,H2");
            pw.println("V1,V2");
            pw.close();

            final CSVParser parser = CSVParser.parse(tempFile, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withHeader());
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("V1", records.get(0).get("H1"));
            assertEquals("V2", records.get(0).get("H2"));

            assertFalse(parser.isClosed());
            parser.close();
            assertTrue(parser.isClosed());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testParseUrlLifecycle() throws Exception {
        final File tempFile = File.createTempFile("csv_url_test_", ".csv");
        tempFile.deleteOnExit();
        try {
            final PrintWriter pw = new PrintWriter(tempFile, "UTF-8");
            pw.println("id,val");
            pw.println("100,abc");
            pw.close();

            final URL url = tempFile.toURI().toURL();
            final CSVParser parser = CSVParser.parse(url, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withHeader());
            final Iterator<CSVRecord> it = parser.iterator();
            assertTrue(it.hasNext());
            final CSVRecord record = it.next();
            assertEquals("100", record.get("id"));
            assertEquals("abc", record.get("val"));
            assertFalse(it.hasNext());
            parser.close();
            assertTrue(parser.isClosed());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testIteratorContract() throws Exception {
        final CSVParser parser = CSVParser.parse("r1c1,r1c2\nr2c1,r2c2", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        assertTrue(it.hasNext());
        assertTrue("hasNext must be idempotent", it.hasNext());
        final CSVRecord rec1 = it.next();
        assertEquals("r1c1", rec1.get(0));

        // Directly call next without calling hasNext
        final CSVRecord rec2 = it.next();
        assertEquals("r2c1", rec2.get(0));

        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException when iterator is exhausted");
        } catch (final NoSuchElementException expected) {
            // Success
        }

        parser.close();
        assertTrue(parser.isClosed());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException on closed parser iterator");
        } catch (final NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testIteratorRemoveThrowsUnsupportedOperation() throws Exception {
        final CSVParser parser = CSVParser.parse("1,2\n3,4", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        it.next();
        it.remove();
    }

    @Test(timeout = 4000)
    public void testIteratorWrapsIOExceptionInRuntimeException() {
        final Reader throwingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated I/O failure");
            }

            @Override
            public void close() throws IOException {
            }
        };

        try {
            final CSVParser parser = new CSVParser(throwingReader, CSVFormat.DEFAULT);
            final Iterator<CSVRecord> it = parser.iterator();
            it.hasNext();
            fail("Expected RuntimeException wrapping IOException");
        } catch (final RuntimeException expected) {
            assertTrue(expected.getCause() instanceof IOException);
            assertEquals("Simulated I/O failure", expected.getCause().getMessage());
        } catch (final IOException e) {
            fail("IOException should have been wrapped in RuntimeException by iterator");
        }
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b,c", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // Second close should be safe
        parser.close();
        assertTrue(parser.isClosed());
    }
}