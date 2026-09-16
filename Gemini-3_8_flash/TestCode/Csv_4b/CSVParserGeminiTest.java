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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------------
 * Target Method / Branch Area       | Condition / Boundary Under Test                          | Expected Outcome
 * ---------------------------------------------------------------------------------------------------------------------
 * CSVParser#getHeaderMap            | headerMap == null (no header defined)                    | Must return null (Ground Truth Defect: throws NPE)
 * CSVParser#initializeHeader        | formatHeader != null && formatHeader.length == 0         | Reads header from first record
 * CSVParser#initializeHeader        | formatHeader != null && formatHeader.length > 0          | Uses provided array as header
 * CSVParser#initializeHeader        | format.getSkipHeaderRecord() == true                     | Discards first record of stream
 * CSVParser#initializeHeader        | empty input with formatHeader.length == 0                | record == null -> header == null
 * CSVParser#addRecordValue          | format.getNullString() == null                           | Values added verbatim
 * CSVParser#addRecordValue          | input.equalsIgnoreCase(nullString) == true               | Value mapped to null
 * CSVParser#addRecordValue          | input.equalsIgnoreCase(nullString) == false              | Value remains string
 * CSVParser#nextRecord              | Token.Type.TOKEN / EORECORD / EOF / INVALID / COMMENT    | Full coverage of tokenizer states
 * CSVParser#nextRecord              | Multiple consecutive comments (sb != null)               | Joins comments with LF ('\n')
 * CSVParser#iterator                | hasNext() / next() without prior hasNext()               | Accurate traversal, NoSuchElementException on end
 * CSVParser#iterator                | isClosed() == true                                       | hasNext() false, next() throws NoSuchElementException
 * CSVParser#iterator#remove         | iterator.remove()                                        | UnsupportedOperationException
 * CSVParser#parse(File, Format)     | Normal file parsing + null argument assertions           | Creates valid parser; throws IAE on null
 * CSVParser#parse(String, Format)   | Normal string parsing + null argument assertions         | Creates valid parser; throws IAE on null
 * CSVParser#parse(URL, Charset, Fmt)| URL protocol parsing + null argument assertions          | Parses stream; throws IAE on null
 * CSVParser#close / isClosed        | close() resource lifecycle                               | lexer.isClosed() == true
 * CSVParser#getCurrentLineNumber    | Multiline records                                        | Line count diverges from record count
 * ---------------------------------------------------------------------------------------------------------------------
 */
public class CSVParserGeminiTest {

    // ====================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // ====================================================================================

    /**
     * Target: Defect testNoHeaderMap
     * When CSVFormat has no header specified, CSVParser.getHeaderMap() should return null.
     * In defective versions, new LinkedHashMap<String, Integer>(this.headerMap) throws NullPointerException.
     */
    @Test(timeout = 4000)
    public void testNoHeaderMapDefectTarget() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3", CSVFormat.DEFAULT);
        try {
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNull("Header map must be null when format specifies no header", headerMap);
        } finally {
            parser.close();
        }
    }

    // ====================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ====================================================================================

    @Test(timeout = 4000)
    public void testParseStringSimpleRecords() throws IOException {
        final String input = "A,B,C\n1,2,3\n4,5,6";
        final CSVParser parser = CSVParser.parse(input, CSVFormat.DEFAULT);

        final List<CSVRecord> records = parser.getRecords();
        assertNotNull(records);
        assertEquals(3, records.size());

        assertEquals("A", records.get(0).get(0));
        assertEquals("B", records.get(0).get(1));
        assertEquals("C", records.get(0).get(2));
        assertEquals(1L, records.get(0).getRecordNumber());

        assertEquals("1", records.get(1).get(0));
        assertEquals(2L, records.get(1).getRecordNumber());

        assertEquals("4", records.get(2).get(0));
        assertEquals(3L, records.get(2).getRecordNumber());

        assertEquals(3L, parser.getRecordNumber());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testExplicitHeaderMap() throws IOException {
        final String input = "1,2,3\n4,5,6";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2", "Col3");
        final CSVParser parser = CSVParser.parse(input, format);

        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("Col1"));
        assertEquals(Integer.valueOf(1), headerMap.get("Col2"));
        assertEquals(Integer.valueOf(2), headerMap.get("Col3"));

        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals("1", record.get("Col1"));
        assertEquals("2", record.get("Col2"));
        assertEquals("3", record.get("Col3"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHeaderFromFirstRecord() throws IOException {
        final String input = "Header1,Header2\nVal1,Val2";
        // withHeader() with no args sets header to empty array -> dynamic first record header
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        final CSVParser parser = CSVParser.parse(input, format);

        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("Header1"));
        assertEquals(Integer.valueOf(1), headerMap.get("Header2"));

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("Val1", records.get(0).get("Header1"));
        assertEquals("Val2", records.get(0).get("Header2"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSkipHeaderRecordWithExplicitHeader() throws IOException {
        final String input = "H1,H2\nV1,V2\nV3,V4";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader("ColA", "ColB").withSkipHeaderRecord(true);
        final CSVParser parser = CSVParser.parse(input, format);

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("V1", records.get(0).get(0));
        assertEquals("V3", records.get(1).get(0));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNullStringTransformation() throws IOException {
        final String input = "alpha,NULL,beta,null,gamma";
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        final CSVParser parser = CSVParser.parse(input, format);

        final CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(5, record.size());
        assertEquals("alpha", record.get(0));
        assertNull(record.get(1)); // "NULL" matches nullString
        assertEquals("beta", record.get(2));
        assertNull(record.get(3)); // "null" matches nullString (case-insensitive)
        assertEquals("gamma", record.get(4));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSingleAndMultiLineComments() throws IOException {
        final String input = "# First Comment\n# Second Comment\na,b\n# Third Comment\nc,d";
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        final CSVParser parser = CSVParser.parse(input, format);

        final CSVRecord record1 = parser.nextRecord();
        assertNotNull(record1);
        assertEquals("a", record1.get(0));
        assertEquals("b", record1.get(1));
        assertEquals("First Comment\nSecond Comment", record1.getComment());

        final CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals("c", record2.get(0));
        assertEquals("d", record2.get(1));
        assertEquals("Third Comment", record2.getComment());

        assertNull(parser.nextRecord());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLineNumberVsRecordNumberWithMultilineValues() throws IOException {
        final String input = "\"line1\nline2\nline3\",val2\nline4,val4";
        final CSVParser parser = CSVParser.parse(input, CSVFormat.DEFAULT);

        final CSVRecord record1 = parser.nextRecord();
        assertNotNull(record1);
        assertEquals(1L, parser.getRecordNumber());
        assertEquals(3L, parser.getCurrentLineNumber());

        final CSVRecord record2 = parser.nextRecord();
        assertNotNull(record2);
        assertEquals(2L, parser.getRecordNumber());
        assertEquals(4L, parser.getCurrentLineNumber());

        assertNull(parser.nextRecord());
        parser.close();
    }

    // ====================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ====================================================================================

    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        final CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        assertNull(parser.nextRecord());
        assertTrue(parser.getRecords().isEmpty());
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
    public void testTrailingEmptyValuesAndDelimiters() throws IOException {
        final String input = "a,b,\n,c,";
        final CSVParser parser = CSVParser.parse(input, CSVFormat.DEFAULT);

        final CSVRecord r1 = parser.nextRecord();
        assertNotNull(r1);
        assertEquals(3, r1.size());
        assertEquals("a", r1.get(0));
        assertEquals("b", r1.get(1));
        assertEquals("", r1.get(2));

        final CSVRecord r2 = parser.nextRecord();
        assertNotNull(r2);
        assertEquals(3, r2.size());
        assertEquals("", r2.get(0));
        assertEquals("c", r2.get(1));
        assertEquals("", r2.get(2));

        parser.close();
    }

    @Test(timeout = 4000)
    public void testParseFromFile() throws IOException {
        final File tempFile = File.createTempFile("csv_test_", ".csv");
        tempFile.deleteOnExit();

        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write("x,y,z\n7,8,9\n");
        }

        final CSVParser parser = CSVParser.parse(tempFile, CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("x", records.get(0).get(0));
        assertEquals("7", records.get(1).get(0));

        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testParseFromURL() throws IOException {
        final File tempFile = File.createTempFile("csv_url_test_", ".csv");
        tempFile.deleteOnExit();

        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write("u1,u2\nv1,v2\n");
        }

        final URL fileUrl = tempFile.toURI().toURL();
        final CSVParser parser = CSVParser.parse(fileUrl, StandardCharsets.UTF_8, CSVFormat.DEFAULT);

        final List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("u1", records.get(0).get(0));
        assertEquals("v1", records.get(1).get(0));

        parser.close();
    }

    // ====================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ====================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader("a,b"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFile() throws IOException {
        CSVParser.parse((File) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseFileNullFormat() throws IOException {
        final File tempFile = File.createTempFile("csv_tmp_", ".csv");
        tempFile.deleteOnExit();
        CSVParser.parse(tempFile, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullString() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseStringNullFormat() throws IOException {
        CSVParser.parse("foo", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseURLNullURL() throws IOException {
        CSVParser.parse((URL) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseURLNullCharset() throws IOException {
        final URL dummyUrl = new URL("http://localhost");
        CSVParser.parse(dummyUrl, (Charset) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseURLNullFormat() throws IOException {
        final URL dummyUrl = new URL("http://localhost");
        CSVParser.parse(dummyUrl, StandardCharsets.UTF_8, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInconsistentFormatValidation() throws IOException {
        // Delimiter and QuoteChar cannot be identical
        final CSVFormat invalidFormat = CSVFormat.DEFAULT.withDelimiter('!').withQuoteChar('!');
        new CSVParser(new StringReader("a!b"), invalidFormat);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testInvalidTokenThrowsIOException() throws IOException {
        // Unclosed quoted token at EOF with strict quoting or invalid escape
        final String badCsv = "\"unclosed quote string";
        final CSVParser parser = CSVParser.parse(badCsv, CSVFormat.DEFAULT);
        try {
            parser.getRecords();
        } finally {
            parser.close();
        }
    }

    // ====================================================================================
    // Partition E: Iterator Lifecycle & Contract Integrity
    // ====================================================================================

    @Test(timeout = 4000)
    public void testIteratorStandardTraversal() throws IOException {
        final CSVParser parser = CSVParser.parse("r1c1,r1c2\nr2c1,r2c2", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        assertTrue(it.hasNext());
        assertTrue(it.hasNext()); // Idempotency of hasNext()

        final CSVRecord rec1 = it.next();
        assertEquals("r1c1", rec1.get(0));

        final CSVRecord rec2 = it.next(); // Direct next without hasNext
        assertEquals("r2c1", rec2.get(0));

        assertFalse(it.hasNext());
        parser.close();
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testIteratorExhaustionThrowsNoSuchElementException() throws IOException {
        final CSVParser parser = CSVParser.parse("v1", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        assertNotNull(it.next());
        it.next(); // Exhausted
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testIteratorRemoveThrowsUnsupportedOperationException() throws IOException {
        final CSVParser parser = CSVParser.parse("v1,v2", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();
        it.remove();
    }

    @Test(timeout = 4000)
    public void testIteratorWhenParserClosed() throws IOException {
        final CSVParser parser = CSVParser.parse("1,2\n3,4", CSVFormat.DEFAULT);
        final Iterator<CSVRecord> it = parser.iterator();

        parser.close();
        assertTrue(parser.isClosed());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException when next() is called on closed parser");
        } catch (final NoSuchElementException expected) {
            assertEquals("CSVParser has been closed", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testIteratorWrapsIOExceptionInRuntimeException() {
        final Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated I/O failure");
            }

            @Override
            public void close() {
            }
        };

        try {
            final CSVParser parser = new CSVParser(failingReader, CSVFormat.DEFAULT);
            final Iterator<CSVRecord> it = parser.iterator();
            it.hasNext();
            fail("Expected RuntimeException wrapping IOException");
        } catch (final RuntimeException re) {
            assertTrue(re.getCause() instanceof IOException);
            assertEquals("Simulated I/O failure", re.getCause().getMessage());
        } catch (final IOException ioe) {
            fail("Should have been caught in iterator and wrapped in RuntimeException");
        }
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.DEFAULT);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        parser.close(); // Second close must not throw
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testHeaderMapIterationOrderPreserved() throws IOException {
        final String input = "Z,A,M\n1,2,3";
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        final CSVParser parser = CSVParser.parse(input, format);

        final Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);

        final String[] keys = headerMap.keySet().toArray(new String[0]);
        assertEquals(3, keys.length);
        assertEquals("Z", keys[0]);
        assertEquals("A", keys[1]);
        assertEquals("M", keys[2]);

        // Verify that mutating the returned copy does not affect internal parser state
        headerMap.remove("Z");
        assertEquals(2, headerMap.size());
        assertEquals(3, parser.getHeaderMap().size());

        parser.close();
    }
}