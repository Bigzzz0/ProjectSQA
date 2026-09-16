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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target: org.apache.commons.csv.CSVParser
 * Defect Reference: Defects4J / testIteratorSequenceBreaking (expected:<[3]> but was:<[4]>)
 * 
 * Branch Coverage Areas:
 * 1. Static factory methods: parse(File, Charset, CSVFormat), parse(InputStream, Charset, CSVFormat),
 *    parse(Path, Charset, CSVFormat), parse(Reader, CSVFormat), parse(String, CSVFormat), parse(URL, Charset, CSVFormat).
 * 2. Header parsing & validation:
 *    - formatHeader == null vs formatHeader != null
 *    - formatHeader.length == 0 (read from stream) vs explicit headers
 *    - format.getSkipHeaderRecord() true/false
 *    - format.getIgnoreHeaderCase() true/false (TreeMap vs LinkedHashMap)
 *    - duplicate headers validation with format.getAllowMissingColumnNames() true/false
 * 3. Token switch processing: TOKEN, EORECORD, EOF (isReady true/false), INVALID (IOException), COMMENT (single & multi-line).
 * 4. Value processing: format.getTrim() true/false, format.getTrailingDelimiter() true/false, format.getNullString().
 * 5. Iterator contract:
 *    - hasNext() / next() standard traversal
 *    - sequence breaking: next() called without hasNext(), mixed hasNext() and next()
 *    - closed parser interaction (hasNext() -> false, next() -> NoSuchElementException)
 *    - iteration exhaustion (next() -> NoSuchElementException)
 *    - IOException wrapped in IllegalStateException
 *    - remove() -> UnsupportedOperationException
 * 6. Offset & Record tracking: characterOffset, recordNumber initialization & incrementing.
 */
public class CSVParserGeminiTest {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseSimpleRecords() throws IOException {
        final String csv = "a,b,c\n1,2,3";
        try (final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());

            final CSVRecord rec1 = records.get(0);
            assertEquals("a", rec1.get(0));
            assertEquals("b", rec1.get(1));
            assertEquals("c", rec1.get(2));
            assertEquals(1L, rec1.getRecordNumber());

            final CSVRecord rec2 = records.get(1);
            assertEquals("1", rec2.get(0));
            assertEquals("2", rec2.get(1));
            assertEquals("3", rec2.get(2));
            assertEquals(2L, rec2.getRecordNumber());
            assertEquals(2L, parser.getRecordNumber());
        }
    }

    @Test(timeout = 4000)
    public void testFirstEndOfLineDetection() throws IOException {
        try (final CSVParser crlfParser = CSVParser.parse("a,b\r\nc,d", CSVFormat.DEFAULT)) {
            crlfParser.getRecords();
            assertEquals("\r\n", crlfParser.getFirstEndOfLine());
        }

        try (final CSVParser lfParser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT)) {
            lfParser.getRecords();
            assertEquals("\n", lfParser.getFirstEndOfLine());
        }

        try (final CSVParser crParser = CSVParser.parse("a,b\rc,d", CSVFormat.DEFAULT)) {
            crParser.getRecords();
            assertEquals("\r", crParser.getFirstEndOfLine());
        }

        try (final CSVParser noEolParser = CSVParser.parse("a,b", CSVFormat.DEFAULT)) {
            noEolParser.getRecords();
            assertNull(noEolParser.getFirstEndOfLine());
        }
    }

    @Test(timeout = 4000)
    public void testCurrentLineNumberMultiLine() throws IOException {
        final String csv = "\"line1\nline2\",b\r\nc,d";
        try (final CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals(3L, parser.getCurrentLineNumber());
        }
    }

    @Test(timeout = 4000)
    public void testHeaderMapAutoDetection() throws IOException {
        final String csv = "Col1,Col2,Col3\nv1,v2,v3";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        try (final CSVParser parser = CSVParser.parse(csv, format)) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(3, headerMap.size());
            assertEquals(Integer.valueOf(0), headerMap.get("Col1"));
            assertEquals(Integer.valueOf(1), headerMap.get("Col2"));
            assertEquals(Integer.valueOf(2), headerMap.get("Col3"));

            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            final CSVRecord record = records.get(0);
            assertEquals("v1", record.get("Col1"));
            assertEquals("v2", record.get("Col2"));
            assertEquals("v3", record.get("Col3"));
        }
    }

    @Test(timeout = 4000)
    public void testHeaderMapExplicitWithSkipHeader() throws IOException {
        final String csv = "Header1,Header2\nval1,val2";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("H1", "H2").withSkipHeaderRecord(true);
        try (final CSVParser parser = CSVParser.parse(csv, format)) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(Integer.valueOf(0), headerMap.get("H1"));
            assertEquals(Integer.valueOf(1), headerMap.get("H2"));

            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("val1", records.get(0).get("H1"));
            assertEquals("val2", records.get(0).get("H2"));
        }
    }

    @Test(timeout = 4000)
    public void testHeaderCaseInsensitive() throws IOException {
        final String csv = "FirstName,LastName\nJohn,Doe";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader().withIgnoreHeaderCase(true);
        try (final CSVParser parser = CSVParser.parse(csv, format)) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(Integer.valueOf(0), headerMap.get("firstname"));
            assertEquals(Integer.valueOf(0), headerMap.get("FIRSTNAME"));
            assertEquals(Integer.valueOf(1), headerMap.get("lastname"));

            final CSVRecord record = parser.iterator().next();
            assertEquals("John", record.get("FIRSTNAME"));
            assertEquals("Doe", record.get("lastname"));
        }
    }

    @Test(timeout = 4000)
    public void testHeaderMapEncapsulation() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B");
        try (final CSVParser parser = CSVParser.parse("1,2", format)) {
            final Map<String, Integer> map1 = parser.getHeaderMap();
            map1.put("C", 2);
            final Map<String, Integer> map2 = parser.getHeaderMap();
            assertFalse(map2.containsKey("C"));
        }
    }

    @Test(timeout = 4000)
    public void testCommentsMultiLine() throws IOException {
        final String csv = "# First comment\n# Second comment\na,b,c\n# Another comment\nx,y,z";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (final CSVParser parser = CSVParser.parse(csv, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("First comment\nSecond comment", records.get(0).getComment());
            assertEquals("Another comment", records.get(1).getComment());
        }
    }

    @Test(timeout = 4000)
    public void testTrimAndNullStringCombination() throws IOException {
        final String csv = "  hello  , NULL ,  world  ";
        final CSVFormat format = CSVFormat.DEFAULT.withTrim(true).withNullString("NULL");
        try (final CSVParser parser = CSVParser.parse(csv, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            final CSVRecord record = records.get(0);
            assertEquals("hello", record.get(0));
            assertNull(record.get(1));
            assertEquals("world", record.get(2));
        }
    }

    @Test(timeout = 4000)
    public void testTrailingDelimiter() throws IOException {
        final String csv = "a,b,c,\n1,2,3,";
        final CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        try (final CSVParser parser = CSVParser.parse(csv, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals(3, records.get(0).size());
            assertEquals("c", records.get(0).get(2));
            assertEquals(3, records.get(1).size());
            assertEquals("3", records.get(1).get(2));
        }
    }

    @Test(timeout = 4000)
    public void testCustomOffsetAndRecordNumber() throws IOException {
        final String csv = "line1\nline2";
        final long initialOffset = 100L;
        final long initialRecordNumber = 10L;
        try (final CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT, initialOffset, initialRecordNumber)) {
            final CSVRecord rec1 = parser.nextRecord();
            assertNotNull(rec1);
            assertEquals(10L, rec1.getRecordNumber());
            assertEquals(initialOffset, rec1.getCharacterPosition());

            final CSVRecord rec2 = parser.nextRecord();
            assertNotNull(rec2);
            assertEquals(11L, rec2.getRecordNumber());
            assertTrue(rec2.getCharacterPosition() > initialOffset);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        try (final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
            assertEquals(0L, parser.getRecordNumber());
            assertNull(parser.getHeaderMap());
        }
    }

    @Test(timeout = 4000)
    public void testEmptyInputWithHeader() throws IOException {
        try (final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT.withHeader())) {
            final List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
            assertNotNull(parser.getHeaderMap());
            assertTrue(parser.getHeaderMap().isEmpty());
        }
    }

    @Test(timeout = 4000)
    public void testNoHeaderSpecified() throws IOException {
        try (final CSVParser parser = CSVParser.parse("a,b,c", CSVFormat.DEFAULT)) {
            assertNull(parser.getHeaderMap());
        }
    }

    @Test(timeout = 4000)
    public void testParseFromFile() throws IOException {
        final File tempFile = File.createTempFile("csv_test_", ".csv");
        tempFile.deleteOnExit();
        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), UTF_8)) {
            writer.write("x,y\n8,9");
        }

        try (final CSVParser parser = CSVParser.parse(tempFile, UTF_8, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("x", records.get(0).get(0));
            assertEquals("8", records.get(1).get(0));
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testParseFromPath() throws IOException {
        final File tempFile = File.createTempFile("csv_path_test_", ".csv");
        tempFile.deleteOnExit();
        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), UTF_8)) {
            writer.write("k,v\nkey1,val1");
        }

        final Path path = tempFile.toPath();
        try (final CSVParser parser = CSVParser.parse(path, UTF_8, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("k", records.get(0).get(0));
            assertEquals("key1", records.get(1).get(0));
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testParseFromInputStream() throws IOException {
        final byte[] bytes = "h1,h2\nv1,v2".getBytes(UTF_8);
        try (final InputStream inputStream = new ByteArrayInputStream(bytes);
             final CSVParser parser = CSVParser.parse(inputStream, UTF_8, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("h1", records.get(0).get(0));
        }
    }

    @Test(timeout = 4000)
    public void testParseFromURL() throws IOException {
        final File tempFile = File.createTempFile("csv_url_test_", ".csv");
        tempFile.deleteOnExit();
        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), UTF_8)) {
            writer.write("u1,u2\nu3,u4");
        }

        final URL url = tempFile.toURI().toURL();
        try (final CSVParser parser = CSVParser.parse(url, UTF_8, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("u1", records.get(0).get(0));
            assertEquals("u3", records.get(1).get(0));
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testParseFromReaderDirect() throws IOException {
        try (final Reader reader = new StringReader("r1,r2\nr3,r4");
             final CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("r1", records.get(0).get(0));
        }
    }

    @Test(timeout = 4000)
    public void testGetRecordsCalledRepeatedly() throws IOException {
        try (final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT)) {
            final List<CSVRecord> firstCall = parser.getRecords();
            assertEquals(2, firstCall.size());
            final List<CSVRecord> secondCall = parser.getRecords();
            assertTrue(secondCall.isEmpty());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets exact defect documented in Defects4J:
     * CSVParserTest::testIteratorSequenceBreaking (expected:<[3]> but was:<[4]>)
     */
    @Test(timeout = 4000)
    public void testIteratorSequenceBreaking() throws IOException {
        final String csvData = "a,b,c\n1,2,3\nx,y,z";
        final CSVParser parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        final Iterator<CSVRecord> itr = parser.iterator();

        CSVRecord record = itr.next();
        assertEquals("a", record.get(0));
        assertEquals(1L, record.getRecordNumber());

        record = itr.next();
        assertEquals("1", record.get(0));
        assertEquals(2L, record.getRecordNumber());

        record = itr.next();
        assertEquals("x", record.get(0));
        assertEquals(3L, record.getRecordNumber());

        assertFalse(itr.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratorSequenceBreakingSingleColumn() throws IOException {
        final CSVParser parser = CSVParser.parse("1\n2\n3\n4", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> itr = parser.iterator();

        assertEquals("1", itr.next().get(0));
        assertTrue(itr.hasNext());
        assertEquals("2", itr.next().get(0));
        assertEquals("3", itr.next().get(0));
        assertEquals("4", itr.next().get(0));
        assertFalse(itr.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratorSequenceBreakingMixedHasNextAndNext() throws IOException {
        final CSVParser parser = CSVParser.parse("1\n2\n3\n4\n5", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> itr = parser.iterator();

        assertEquals("1", itr.next().get(0));
        assertEquals("2", itr.next().get(0));
        assertTrue(itr.hasNext());
        assertTrue(itr.hasNext()); // Consecutive hasNext() calls must be idempotent
        assertEquals("3", itr.next().get(0));
        assertEquals("4", itr.next().get(0));
        assertTrue(itr.hasNext());
        assertEquals("5", itr.next().get(0));
        assertFalse(itr.hasNext());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFormat() throws IOException {
        final File tempFile = File.createTempFile("test_", ".csv");
        tempFile.deleteOnExit();
        try {
            CSVParser.parse(tempFile, UTF_8, null);
        } finally {
            tempFile.delete();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInputStreamNullStream() throws IOException {
        CSVParser.parse((InputStream) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInputStreamNullFormat() throws IOException {
        CSVParser.parse(new ByteArrayInputStream(new byte[0]), UTF_8, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParsePathNullPath() throws IOException {
        CSVParser.parse((Path) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParsePathNullFormat() throws IOException {
        final File tempFile = File.createTempFile("test_", ".csv");
        tempFile.deleteOnExit();
        try {
            CSVParser.parse(tempFile.toPath(), UTF_8, null);
        } finally {
            tempFile.delete();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("test", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullUrl() throws IOException {
        CSVParser.parse((URL) null, UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullCharset() throws IOException {
        final URL url = new URL("file://dummy");
        CSVParser.parse(url, null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlNullFormat() throws IOException {
        final URL url = new URL("file://dummy");
        CSVParser.parse(url, UTF_8, null);
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
    public void testDuplicateHeaderNamesThrowsException() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col1");
        CSVParser.parse("1,2", format);
    }

    @Test(timeout = 4000)
    public void testDuplicateEmptyHeaderNamesAllowedWhenConfigured() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("", "").withAllowMissingColumnNames(true);
        try (final CSVParser parser = CSVParser.parse("1,2", format)) {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertTrue(headerMap.containsKey(""));
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDuplicateEmptyHeaderNamesDisallowed() throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("", "").withAllowMissingColumnNames(false);
        CSVParser.parse("1,2", format);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testInvalidParseSequenceThrowsException() throws IOException {
        // Unterminated quoted string without closing quote
        final CSVFormat format = CSVFormat.DEFAULT.withQuote('\"');
        try (final CSVParser parser = CSVParser.parse("\"unterminated_quote", format)) {
            parser.getRecords();
        }
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testIteratorNextOnEmptyThrowsException() throws IOException {
        try (final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT)) {
            final Iterator<CSVRecord> iterator = parser.iterator();
            assertFalse(iterator.hasNext());
            iterator.next();
        }
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testIteratorRemoveThrowsException() throws IOException {
        try (final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT)) {
            final Iterator<CSVRecord> iterator = parser.iterator();
            iterator.remove();
        }
    }

    @Test(timeout = 4000)
    public void testIteratorIOExceptionWrappedInIllegalStateException() {
        final Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated disk error");
            }

            @Override
            public void close() throws IOException {
            }
        };

        try {
            final CSVParser parser = new CSVParser(failingReader, CSVFormat.DEFAULT);
            final Iterator<CSVRecord> iterator = parser.iterator();
            iterator.hasNext();
            fail("Expected IllegalStateException due to IOException in Reader");
        } catch (final IllegalStateException e) {
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("Simulated disk error", e.getCause().getMessage());
        } catch (final IOException e) {
            fail("IOException should have been caught and wrapped by iterator: " + e);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        parser.close(); // Calling close again must not throw an exception
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testIteratorAfterCloseBehavior() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> iterator = parser.iterator();
        parser.close();

        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("Expected NoSuchElementException when calling next() on closed parser");
        } catch (final NoSuchElementException expected) {
            assertEquals("CSVParser has been closed", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testForEachIterationSupport() throws IOException {
        final CSVParser parser = CSVParser.parse("a\nb\nc", CSVFormat.DEFAULT);
        int count = 0;
        for (final CSVRecord record : parser) {
            assertNotNull(record);
            count++;
        }
        assertEquals(3, count);
        assertEquals(3L, parser.getRecordNumber());
    }
}