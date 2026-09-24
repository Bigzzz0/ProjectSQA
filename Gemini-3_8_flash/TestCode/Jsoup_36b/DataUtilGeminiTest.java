/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.helper.DataUtil
 * Java Target: 8 / JUnit 4
 *
 * Decision / Condition Coverage Targets:
 * 1. DataUtil.getCharsetFromContentType(String):
 *    - null contentType -> returns null.
 *    - contentType without charset pattern match -> returns null.
 *    - charsetPattern match with valid supported charset -> returns charset.
 *    - charsetPattern match requiring uppercase conversion (e.g., lower-case standard name) -> returns matched charset.
 *    - charsetPattern match with illegal / malformed charset names (e.g. empty, quotes, commas, invalid chars)
 *      -> DEFECT TARGET: Charset.isSupported(illegalName) throws IllegalCharsetNameException on defective versions.
 *         Expected: return null safely without throwing an uncaught exception.
 *
 * 2. DataUtil.readToByteBuffer(InputStream, int maxSize):
 *    - maxSize < 0 -> throws IllegalArgumentException via Validate.isTrue.
 *    - maxSize == 0 (unlimited) -> reads all data into ByteBuffer.
 *    - maxSize > 0 (capped):
 *        * Stream smaller than maxSize -> reads complete stream.
 *        * Stream larger than maxSize (partial read where read > remaining) -> truncates to maxSize exactly.
 *    - Multi-chunk reads exceeding bufferSize (0x20000 = 131072 bytes).
 *
 * 3. DataUtil.parseByteData(ByteBuffer, String, String, Parser):
 *    - charsetName != null:
 *        * charsetName is empty string -> throws IllegalArgumentException via Validate.notEmpty.
 *        * valid charsetName -> decodes using specified charset.
 *        * checks BOM stripping (leading \uFEFF / 65279).
 *        * sets doc.outputSettings().charset(charsetName).
 *    - charsetName == null (detect from meta):
 *        * No meta tag present -> retains UTF-8 default, leaves doc as-is (doc != null, outputSettings unchanged).
 *        * Meta tag with http-equiv="content-type" and content="...":
 *            - charset same as default (UTF-8) -> no re-decode.
 *            - charset different (e.g., ISO-8859-1, GB2312) -> re-decodes, rewinds ByteBuffer, strips BOM if present.
 *            - charset empty/null -> keeps UTF-8.
 *        * Meta tag with charset="...":
 *            - charset same as default -> no re-decode.
 *            - charset different -> re-decodes with detected charset.
 *
 * 4. DataUtil.load(File, String, String) & DataUtil.load(InputStream, ...):
 *    - File load with valid file and charset.
 *    - File load with FileNotFoundException / IOException handling (ensuring finally stream close).
 *    - InputStream load variants with htmlParser and custom Parser.xmlParser().
 *
 * 5. DataUtil private constructor reflection invocation for 100% constructor coverage.
 */

package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DataUtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeStandard() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/plain; charset=\"US-ASCII\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset = UTF-8;"));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeNullOrNoMatch() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; no-charset-here"));
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] expected = "Testing unlimited byte buffer reading functionality.".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(expected);

        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertNotNull(buf);
        assertArrayEquals(expected, buf.array());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferDefaultOverload() throws IOException {
        byte[] expected = "Default overload test data.".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(expected);

        ByteBuffer buf = DataUtil.readToByteBuffer(in);
        assertNotNull(buf);
        assertArrayEquals(expected, buf.array());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCappedSmallerThanTotal() throws IOException {
        byte[] original = "0123456789ABCDEF".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(original);

        ByteBuffer buf = DataUtil.readToByteBuffer(in, 10);
        assertNotNull(buf);
        assertEquals(10, buf.remaining());
        byte[] truncated = new byte[10];
        buf.get(truncated);
        assertArrayEquals("0123456789".getBytes("UTF-8"), truncated);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCappedLargerThanTotal() throws IOException {
        byte[] original = "Short string".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(original);

        ByteBuffer buf = DataUtil.readToByteBuffer(in, 100);
        assertNotNull(buf);
        assertEquals(original.length, buf.remaining());
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals(original, actual);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferLargeDataSpanningChunks() throws IOException {
        // bufferSize is 0x20000 = 131072. We allocate ~150KB to test multi-loop reading.
        int totalSize = 150000;
        byte[] largeData = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            largeData[i] = (byte) (i % 127);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(largeData);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);

        assertEquals(totalSize, buf.remaining());
        assertArrayEquals(largeData, buf.array());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferLargeDataSpanningChunksCapped() throws IOException {
        int totalSize = 150000;
        int capSize = 135000;
        byte[] largeData = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            largeData[i] = (byte) (i % 127);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(largeData);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, capSize);

        assertEquals(capSize, buf.remaining());
        byte[] expected = new byte[capSize];
        System.arraycopy(largeData, 0, expected, 0, capSize);
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testParseByteDataExplicitCharset() {
        String html = "<html><head><title>Explicit</title></head><body><p>Hello World</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Explicit", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithBomAndExplicitCharset() {
        // \uFEFF is 65279, Byte Order Mark
        String htmlWithBom = "\uFEFF<html><head><title>BOM Test</title></head><body><p>BOM Body</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(htmlWithBom.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM Test", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataAutoDetectNoMetaKeepsUtf8() {
        String html = "<html><head><title>No Meta</title></head><body><p>Content</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No Meta", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataAutoDetectMetaHttpEquivUtf8MatchesDefault() {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>Meta UTF8</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Meta UTF8", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataAutoDetectMetaHttpEquivReDecode() {
        String nonUtf8 = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><title>ISO Document</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(nonUtf8.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO Document", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataAutoDetectMetaCharsetHtml5ReDecode() {
        String html5 = "<html><head><meta charset=\"ISO-8859-1\"><title>HTML5 Meta</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html5.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("HTML5 Meta", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataReDecodeWithBom() {
        String html = "\uFEFF<html><head><meta charset=\"ISO-8859-1\"><title>BOM ReDecode</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM ReDecode", doc.title());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamDefaultParser() throws IOException {
        String html = "<html><head><title>Stream Load</title></head><body></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com/");
        assertNotNull(doc);
        assertEquals("Stream Load", doc.title());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamXmlParser() throws IOException {
        String xml = "<xml><element>test</element></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com/", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("test", doc.select("element").text());
    }

    @Test(timeout = 4000)
    public void testLoadFile() throws IOException {
        File tempFile = File.createTempFile("datautil_test", ".html");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write("<html><head><title>File Test</title></head><body></body></html>".getBytes("UTF-8"));
        fos.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com/");
        assertNotNull(doc);
        assertEquals("File Test", doc.title());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadToByteBufferEmptyStream() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertNotNull(buf);
        assertEquals(0, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferExactCapacity() throws IOException {
        byte[] data = new byte[100];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 100);
        assertNotNull(buf);
        assertEquals(100, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testParseByteDataEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);
        Document doc = DataUtil.parseByteData(emptyBuffer, "UTF-8", "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeUnsupportedCharsetReturnsNull() {
        // Supported syntax regex-wise, but unsupported charset name
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset-name-12345"));
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaHttpEquivEmptyContent() {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"\"><title>Empty Content</title></head><body></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com/", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Empty Content", doc.title());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectShouldReturnNullForIllegalCharsetNames() {
        // Ground truth: Charset.isSupported(charset) throws IllegalCharsetNameException
        // when passed illegal charset syntax. It must return null rather than crashing with an exception.
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=$HJKDF§$/("));
    }

    @Test(timeout = 4000)
    public void testDefectShouldNotThrowExceptionOnEmptyCharset() {
        // Ground truth: charset="" or charset=
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=\"\""));
    }

    @Test(timeout = 4000)
    public void testDefectQuotedCharset() {
        // Ground truth: single quotes around charset name: 'UTF-8'
        // Defect caused IllegalCharsetNameException: 'UTF-8'
        // Expected behavior: extract UTF-8 or return null without throwing IllegalCharsetNameException
        String charset = DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'");
        assertTrue(charset == null || "UTF-8".equalsIgnoreCase(charset));
    }

    @Test(timeout = 4000)
    public void testDefectShouldCorrectCharsetForDuplicateCharsetString() {
        // Ground truth: duplicate charset declaration "charset=iso-8859-1"
        // Defect caused IllegalCharsetNameException: charset=iso-8859-1
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=charset=iso-8859-1");
        assertTrue(charset == null || "ISO-8859-1".equalsIgnoreCase(charset));
    }

    @Test(timeout = 4000)
    public void testDefectShouldSelectFirstCharsetOnWeirdMultipleCharsetsInMetaTags() {
        // Ground truth: multiple charsets separated by comma: "ISO-8859-1,"
        // Defect caused IllegalCharsetNameException: ISO-8859-1,
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1, text/html");
        assertTrue(charset == null || "ISO-8859-1".equalsIgnoreCase(charset));
    }

    @Test(timeout = 4000)
    public void testDefectBrokenHtml5CharsetWithASingleDoubleQuote() {
        // Ground truth: broken HTML5 charset ending with quote: UTF-8"
        // Defect caused IllegalCharsetNameException: UTF-8"
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8\"");
        assertTrue(charset == null || "UTF-8".equalsIgnoreCase(charset));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadToByteBufferNegativeMaxSizeThrows() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("test".getBytes("UTF-8"));
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseByteDataEmptyExplicitCharsetThrows() {
        ByteBuffer buffer = ByteBuffer.wrap("test".getBytes(Charset.forName("UTF-8")));
        DataUtil.parseByteData(buffer, "", "http://example.com/", Parser.htmlParser());
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testLoadNonExistentFileThrows() throws IOException {
        File nonExistent = new File("non_existent_file_for_jsoup_datautil_test_12345.html");
        DataUtil.load(nonExistent, "UTF-8", "http://example.com/");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorContract() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        assertNotNull("Instance should be creatable via reflection", instance);
    }
}