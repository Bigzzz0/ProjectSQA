package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * White-box test suite for DataUtil.
 *
 * [Branch & Defect Analysis Matrix]
 * - parseByteData: charsetName null vs non-null, meta charset found vs not found, re-decode path, doc==null path, BOM stripping (missing)
 * - readToByteBuffer: normal read, empty stream, large stream, IOException
 * - getCharsetFromContentType: null, empty, valid charset, charset with quotes, no charset, multiple matches
 * - load(File): file not found, normal file
 * - load(InputStream): normal, null stream (will throw NPE)
 * - load(InputStream, Parser): same with custom parser
 * - Boundary: charsetName empty (throws IllegalArgumentException), charsetName invalid (throws IllegalArgumentException from Charset.forName)
 * - Defect target: BOM not stripped causing empty body text
 */
public class DataUtilDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndNoMetaCharset() throws IOException {
        // charsetName=null, no meta charset -> decode as UTF-8, no re-decode, doc not null
        String html = "<html><head></head><body><p>Hello</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndMetaCharsetSameAsDefault() throws IOException {
        // meta charset = UTF-8, same as default, no re-decode
        String html = "<html><head><meta charset=\"UTF-8\"></head><body><p>World</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("World", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndMetaCharsetDifferent() throws IOException {
        // meta charset = ISO-8859-1, different from default, triggers re-decode
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Bonjour</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Bonjour", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithSpecifiedCharset() throws IOException {
        String html = "<html><body><p>Test</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
        // output settings charset should be set
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithSpecifiedCharsetAndBOM() throws IOException {
        // BOM should be stripped, but bug exists: BOM not stripped
        // This test targets the known defect: discardsSpuriousByteOrderMark
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String html = "<html><body><p>One</p></body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteData = ByteBuffer.allocate(bom.length + htmlBytes.length);
        byteData.put(bom);
        byteData.put(htmlBytes);
        byteData.flip();
        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // Expected: BOM stripped, body text "One". Bug: BOM not stripped -> body text empty or different
        assertEquals("One", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndBOM() throws IOException {
        // Same BOM test but with null charset (auto-detect)
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String html = "<html><body><p>One</p></body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteData = ByteBuffer.allocate(bom.length + htmlBytes.length);
        byteData.put(bom);
        byteData.put(htmlBytes);
        byteData.flip();
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("One", doc.body().text());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void parseByteDataWithEmptyCharsetName() throws IOException {
        ByteBuffer byteData = ByteBuffer.wrap("dummy".getBytes());
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void parseByteDataWithInvalidCharsetName() throws IOException {
        ByteBuffer byteData = ByteBuffer.wrap("dummy".getBytes());
        DataUtil.parseByteData(byteData, "invalid-charset", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithEmptyByteBuffer() throws IOException {
        ByteBuffer byteData = ByteBuffer.allocate(0);
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // empty document should have empty body
        assertTrue(doc.body().text().isEmpty());
    }

    @Test(timeout = 4000)
    public void readToByteBufferWithEmptyStream() throws IOException {
        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
        ByteBuffer buf = DataUtil.readToByteBuffer(empty);
        assertEquals(0, buf.remaining());
    }

    @Test(timeout = 4000)
    public void readToByteBufferWithLargeStream() throws IOException {
        // create a stream of size > bufferSize (0x20000 = 131072)
        int size = 200000;
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 256);
        }
        ByteArrayInputStream large = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(large);
        assertEquals(size, buf.remaining());
        // verify content
        byte[] result = new byte[size];
        buf.get(result);
        assertArrayEquals(data, result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeNoCharset() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeWithCharset() {
        assertEquals("EUC-JP", DataUtil.getCharsetFromContentType("text/html; charset=EUC-JP"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeWithQuotedCharset() {
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeWithSpaces() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset = UTF-8"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeMultipleMatches() {
        // regex finds first
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8; charset=ISO-8859-1"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    // Already covered by parseByteDataWithSpecifiedCharsetAndBOM and parseByteDataWithNullCharsetAndBOM
    // Additional test: load from InputStream with BOM
    @Test(timeout = 4000)
    public void loadInputStreamWithBOM() throws IOException {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String html = "<html><body><p>One</p></body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(concat(bom, htmlBytes));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("One", doc.body().text());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void loadWithEmptyCharsetName() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("test".getBytes());
        DataUtil.load(in, "", "http://example.com");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void loadWithInvalidCharsetName() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("test".getBytes());
        DataUtil.load(in, "invalid", "http://example.com");
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void loadWithNullInputStream() throws IOException {
        DataUtil.load((InputStream) null, "UTF-8", "http://example.com");
    }

    @Test(timeout = 4000, expected = FileNotFoundException.class)
    public void loadFileNotFound() throws IOException {
        File nonExistent = new File("nonexistent_file_12345");
        DataUtil.load(nonExistent, "UTF-8", "http://example.com");
    }

    @Test(timeout = 4000)
    public void loadFileSuccess() throws IOException {
        File temp = File.createTempFile("test", ".html");
        temp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write("<html><body><p>FileTest</p></body></html>".getBytes(StandardCharsets.UTF_8));
        }
        Document doc = DataUtil.load(temp, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("FileTest", doc.body().text());
    }

    @Test(timeout = 4000)
    public void loadWithCustomParser() throws IOException {
        String xml = "<?xml version=\"1.0\"?><root><item>XML</item></root>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("XML", doc.text());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    // DataUtil is a utility class with private constructor, no instance methods.
    // No equals/hashCode/clone/serialization to test.

    // Helper method to concatenate byte arrays
    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}