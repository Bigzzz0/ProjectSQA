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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.csv.CSVParser
 *
 * Branch Coverage & Decision Points:
 * 1. Factory Methods:
 *    - parse(File, CSVFormat): null checks on file, format.
 *    - parse(String, CSVFormat): null checks on string, format.
 *    - parse(URL, Charset, CSVFormat): null checks on url, charset, format.
 *    - CSVParser(Reader, CSVFormat): null checks, format.validate().
 * 2. initializeHeader():
 *    - format.getHeader() == null -> hdrMap is null.
 *    - format.getHeader().length == 0 -> read header from first record.
 *    - format.getHeader().length == 0 on empty input -> nextRecord is null, hdrMap empty.
 *    - format.getHeader().length > 0 -> explicit header map built.
 *    - format.getSkipHeaderRecord() == true -> skips first line when explicit header is used.
 *    - format.getSkipHeaderRecord() == false -> does not skip first line.
 *    - Duplicate header detection (Defects4J defect):
 *        * Duplicate names in header line when withHeader() is called.
 *        * Duplicate names in explicit header array when withHeader(names...) is called.
 * 3. addRecordValue():
 *    - nullString == null -> stores string literal.
 *    - nullString != null -> case-insensitive match stores null; non-match stores string.
 * 4. nextRecord():
 *    - TOKEN -> addRecordValue().
 *    - EORECORD -> addRecordValue() and terminates record line.
 *    - EOF -> addRecordValue() if reusableToken.isReady; breaks loop.
 *    - COMMENT -> first comment line initializes sb; subsequent comment appends LF + text.
 *    - Empty input -> returns null CSVRecord.
 *    - Record number tracking -> increments recordNumber per non-empty record.
 * 5. Iterator implementation:
 *    - hasNext(): before/after parsing, consecutive calls, when parser isClosed() returns false.
 *    - next(): normal retrieval, called without hasNext(), throws NoSuchElementException when closed,
 *              throws NoSuchElementException when exhausted.
 *    - remove(): throws UnsupportedOperationException.
 * 6. Parsing into memory:
 *    - getRecords(): returns List<CSVRecord>.
 *    - getRecords(T): adds to custom Collection<CSVRecord>.
 *    - Parsing mid-stream: starts at current position.
 * 7. State & Lifecycle:
 *    - close(): closes underlying lexer; idempotent.
 *    - isClosed(): reflects underlying lexer state.
 *    - getHeaderMap(): returns copy or null; mutations on returned map do not alter parser state.
 *    - getCurrentLineNumber() vs getRecordNumber() on multiline input.
 */
public class CSVParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseBasicRecords() throws IOException {
        final String csv = "A,B,C\n1,2,3\n4,5,6";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        assertFalse(parser.isClosed());
        assertEquals(0L, parser.getRecordNumber());

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        assertEquals(3L, parser.getRecordNumber());

        assertEquals("A", records.get(0).get(0));
        assertEquals("B", records.get(0).get(1));
        assertEquals("C", records.get(0).get(2));

        assertEquals("1", records.get(1).get(0));
        assertEquals("2", records.get(1).get(1));
        assertEquals("3", records.get(1).get(2));

        assertEquals("4", records.get(2).get(0));
        assertEquals("5", records.get(2).get(1));
        assertEquals("6", records.get(2).get(2));

        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testGetRecordsWithCustomCollection() throws IOException {
        final String csv = "r1c1,r1c2\nr2c1,r2c2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        final Set<CSVRecord> recordSet = new LinkedHashSet<CSVRecord>();
        final Set<CSVRecord> result = parser.getRecords(recordSet);

        assertSame(recordSet, result);
        assertEquals(2, result.size());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMidStreamGetRecords() throws IOException {
        final String csv = "row1\nrow2\nrow3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        final CSVRecord firstRecord = parser.nextRecord();
        assertNotNull(firstRecord);
        assertEquals("row1", firstRecord.get(0));
        assertEquals(1L, parser.getRecordNumber());

        final List<CSVRecord> remaining = parser.getRecords();
        assertEquals(2, remaining.size());
        assertEquals("row2", remaining.get(0).get(0));
        assertEquals("row3", remaining.get(1).get(0));
        assertEquals(3L, parser.getRecordNumber());

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLineAndRecordNumberTrackingWithMultiLine() throws IOException {
        final String csv = "\"line1\nline2\",valA\r\nvalB,valC";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        final CSVRecord record1 = parser.nextRecord();
        assertNotNull(record1);
        assertEquals("line1\nline2", record1.get(0));
        assertEquals("valA", record1.get(1));
        assertEquals(1L, record1.getRecordNumber());
        assertEquals(1L, parser.getRecordNumber());

        final CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals("valB", record2.get(0));
        assertEquals("valC", record2.get(1));
        assertEquals(2L, record2.getRecordNumber());
        assertEquals(2L, parser.getRecordNumber());

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCommentHandlingMultipleConsecutive() throws IOException {
        final String csv = "# Header Comment 1\n# Header Comment 2\ncol1,col2\n# Row Comment\nval1,val2";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = CSVParser.parse(csv, format);

        final CSVRecord record1 = parser.nextRecord();
        assertNotNull(record1);
        assertEquals("col1", record1.get(0));
        assertEquals("col2", record1.get(1));
        assertEquals("Header Comment 1\nHeader Comment 2", record1.getComment());

        final CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals("val1", record2.get(0));
        assertEquals("val2", record2.get(1));
        assertEquals("Row Comment", record2.getComment());

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNullStringReplacement() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVParser parser = CSVParser.parse("NULL,null,foo,Null", format);

        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertNull(record.get(0));
        assertNull(record.get(1));
        assertEquals("foo", record.get(2));
        assertNull(record.get(3));

        parser.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Header Configuration
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyInputNoHeader() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
        final List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
        assertEquals(0L, parser.getRecordNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyInputWithHeader() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertTrue(headerMap.isEmpty());
        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderFromFirstRecord() throws IOException {
        final String csv = "h1,h2,h3\nv1,v2,v3";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader());

        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("h1"));
        assertEquals(Integer.valueOf(1), headerMap.get("h2"));
        assertEquals(Integer.valueOf(2), headerMap.get("h3"));

        // Header map returned must be a copy
        headerMap.put("extra", 99);
        assertEquals(3, parser.getHeaderMap().size());

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("v1", records.get(0).get("h1"));
        assertEquals("v2", records.get(0).get("h2"));
        assertEquals("v3", records.get(0).get("h3"));

        parser.close();
    }

    @Test(timeout = 4000)
    public void testExplicitHeaderSkipHeaderRecordFalse() throws IOException {
        final String csv = "c1,c2\nd1,d2";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("COL_A", "COL_B").withSkipHeaderRecord(false);
        final CSVParser parser = CSVParser.parse(csv, format);

        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(Integer.valueOf(0), headerMap.get("COL_A"));
        assertEquals(Integer.valueOf(1), headerMap.get("COL_B"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("c1", records.get(0).get("COL_A"));
        assertEquals("d1", records.get(1).get("COL_A"));

        parser.close();
    }

    @Test(timeout = 4000)
    public void testExplicitHeaderSkipHeaderRecordTrue() throws IOException {
        final String csv = "header_ignored1,header_ignored2\nv1,v2";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("COL_X", "COL_Y").withSkipHeaderRecord(true);
        final CSVParser parser = CSVParser.parse(csv, format);

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("v1", records.get(0).get("COL_X"));
        assertEquals("v2", records.get(0).get("COL_Y"));

        parser.close();
    }

    @Test(timeout = 4000)
    public void testTrailingDelimiterPreserved() throws IOException {
        final String csv = "a,b,\n1,2,";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);

        final CSVRecord record1 = parser.nextRecord();
        assertNotNull(record1);
        assertEquals(3, record1.size());
        assertEquals("", record1.get(2));

        final CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals(3, record2.size());
        assertEquals("", record2.get(2));

        parser.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Target: CSVParserTest::testDuplicateHeaderEntries
     * Expected exception: java.lang.IllegalStateException
     * When duplicate column headers are provided or read from the stream,
     * initializeHeader() must detect duplicates and throw IllegalStateException.
     */
    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testDuplicateHeaderEntriesFromFirstRecord() throws Exception {
        CSVParser.parse("a,b,a\n1,2,3\nx,y,z", CSVFormat.DEFAULT.withHeader());
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testDuplicateHeaderEntriesExplicit() throws Exception {
        CSVParser.parse("1,2,3", CSVFormat.DEFAULT.withHeader("colA", "colB", "colA"));
    }

    // =========================================================================
    // Partition D: Iterator Lifecycle & Exception Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testIteratorStandardTraversal() throws IOException {
        final String csv = "item1\nitem2";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        assertTrue(it.hasNext());
        assertTrue(it.hasNext()); // Idempotent hasNext() check
        final CSVRecord r1 = it.next();
        assertEquals("item1", r1.get(0));

        assertTrue(it.hasNext());
        final CSVRecord r2 = it.next();
        assertEquals("item2", r2.get(0));

        assertFalse(it.hasNext());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testIteratorNextWithoutHasNext() throws IOException {
        final String csv = "singleItem";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        final CSVRecord record = it.next();
        assertNotNull(record);
        assertEquals("singleItem", record.get(0));

        try {
            it.next();
            fail("Expected NoSuchElementException when records are exhausted");
        } catch (final NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("No more CSV records"));
        }

        parser.close();
    }

    @Test(timeout = 4000)
    public void testIteratorOnClosedParser() throws IOException {
        final String csv = "a,b\nc,d";
        final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        parser.close();
        assertTrue(parser.isClosed());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException after parser is closed");
        } catch (final NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("CSVParser has been closed"));
        }
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testIteratorRemoveUnsupported() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        try {
            it.remove();
        } finally {
            parser.close();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader("test"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidFormatValidation() throws IOException {
        // Delimiter and quote char identical -> format.validate() throws IllegalArgumentException
        final CSVFormat invalidFormat = CSVFormat.DEFAULT.withDelimiter('!').withQuote('!');
        new CSVParser(new StringReader("data"), invalidFormat);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFormat() throws IOException {
        final File dummyFile = File.createTempFile("csv_dummy", ".csv");
        dummyFile.deleteOnExit();
        try {
            CSVParser.parse(dummyFile, null);
        } finally {
            dummyFile.delete();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("text", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullUrl() throws IOException {
        CSVParser.parse((URL) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullCharset() throws IOException {
        final URL url = new URL("http://localhost");
        CSVParser.parse(url, null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullFormat() throws IOException {
        final URL url = new URL("http://localhost");
        CSVParser.parse(url, StandardCharsets.UTF_8, null);
    }

    // =========================================================================
    // Partition E: Resource Handling & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseRealFile() throws IOException {
        final File tempFile = File.createTempFile("csv_test_file", ".csv");
        tempFile.deleteOnExit();
        try {
            final FileWriter writer = new FileWriter(tempFile);
            writer.write("name,age\nAlice,30\nBob,25\n");
            writer.close();

            final CSVParser parser = CSVParser.parse(tempFile, CSVFormat.DEFAULT.withHeader());
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("Alice", records.get(0).get("name"));
            assertEquals("30", records.get(0).get("age"));
            assertEquals("Bob", records.get(1).get("name"));
            assertEquals("25", records.get(1).get("age"));

            parser.close();
            assertTrue(parser.isClosed());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testParseRealUrl() throws IOException {
        final File tempFile = File.createTempFile("csv_test_url", ".csv");
        tempFile.deleteOnExit();
        try {
            final FileWriter writer = new FileWriter(tempFile);
            writer.write("key,value\nk1,v1\n");
            writer.close();

            final URL fileUrl = tempFile.toURI().toURL();
            final CSVParser parser = CSVParser.parse(fileUrl, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader());

            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("k1", records.get(0).get("key"));
            assertEquals("v1", records.get(0).get("value"));

            parser.close();
            assertTrue(parser.isClosed());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        final CSVParser parser = CSVParser.parse("foo,bar", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        // Second close must not throw exception
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testIterableForEachLoop() throws IOException {
        final CSVParser parser = CSVParser.parse("1\n2\n3", CSVFormat.DEFAULT);
        int count = 0;
        for (final CSVRecord record : parser) {
            count++;
            assertEquals(String.valueOf(count), record.get(0));
        }
        assertEquals(3, count);
        parser.close();
    }
}