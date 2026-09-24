package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Defect Targeting (Defects4J: discardsSpuriousByteOrderMarkWhenNoCharsetSet):
 *    - parseByteData(ByteBuffer, null, baseUri, parser): When charsetName is null and the document contains
 *      a leading Byte Order Mark (\uFEFF / 65279), the initial speculative UTF-8 parse creates a Document `doc`.
 *      When BOM is detected, docData is sliced (docData = docData.substring(1)), but in the defective code,
 *      `doc` was already initialized and not reset to null, causing `if (doc == null)` to skip re-parsing.
 *      As a result, the Document tree contains the misplaced BOM token which breaks HTML head/title parsing.
 *      Target Test: discardsSpuriousByteOrderMarkWhenNoCharsetSet
 *
 * 2. Core Functional Logic & State Transitions:
 *    - getCharsetFromContentType: match standard charset, match with single quotes, double quotes, spaces,
 *      case sensitivity handling, empty charset values, unsupported charsets, and IllegalCharsetNameException.
 *    - readToByteBuffer: unlimited size (maxSize = 0), capped size (maxSize > 0) with buffer overflow and
 *      partial read boundary, zero-byte input, input larger than bufferSize (0x20000 = 131072 bytes).
 *    - parseByteData: HTML5 meta charset re-decode, meta http-equiv content-type re-decode, fallback when
 *      meta charset matches default UTF-8, meta tag with http-equiv and separate charset attribute,
 *      meta tag with invalid charset attribute triggering IllegalCharsetNameException.
 *    - load(File, ...): file load, automatic stream closure, file not found.
 *    - load(InputStream, ...): stream load with default HTML parser and alternate XML parser.
 *
 * 3. Boundary Value Analysis (BVA) & Defensive Guard Paths:
 *    - readToByteBuffer with negative maxSize (< 0) -> IllegalArgumentException.
 *    - parseByteData with empty charset string ("") -> IllegalArgumentException (Validate.notEmpty).
 *    - getCharsetFromContentType with null contentType -> null.
 *    - Empty document buffer (0 bytes) with null and non-null charset.
 *    - Private constructor invocation via reflection for full coverage.
 */
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: Spurious Byte Order Mark (\uFEFF) must be discarded when no charset is explicitly set.
     * In the defective version, doc is parsed before stripping BOM, and doc is not re-parsed because
     * doc is not null, leaving the BOM in the document and corrupting the head element.
     */
    @Test(timeout = 4000)
    public void discardsSpuriousByteOrderMarkWhenNoCharsetSet() throws IOException {
        String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, null, "http://foo.com", Parser.htmlParser());
        assertEquals("One", doc.head().text());
        assertEquals("One", doc.title());
        assertEquals("Two", doc.body().text());
    }

    /**
     * Verifies that BOM is also properly stripped when UTF-8 charset is explicitly provided.
     */
    @Test(timeout = 4000)
    public void discardsSpuriousByteOrderMarkWhenCharsetExplicitlySet() throws IOException {
        String html = "\uFEFF<html><head><title>Explicit</title></head><body>Content</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://foo.com", Parser.htmlParser());
        assertEquals("Explicit", doc.head().text());
        assertEquals("Content", doc.body().text());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Charset Extraction
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeStandard() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/html; charset=\"US-ASCII\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=  UTF-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; CHARSET=UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeEdgeCases() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=\"\""));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=''"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
        // IllegalCharsetNameException path (e.g. invalid characters like [ or ?)
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=??invalid##charset"));
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaHtml5CharsetReDecode() {
        // Encoding in ISO-8859-1 with HTML5 <meta charset="...">
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Espa\u00f1ol</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Espa\u00f1ol", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaHttpEquivReDecode() {
        // Encoding in ISO-8859-1 with HTML4 <meta http-equiv="Content-Type" content="...">
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body>Fran\u00e7ais</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Fran\u00e7ais", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaHttpEquivWithSeparateCharsetAttr() {
        // Content-Type without charset in content attribute, but has charset attribute on the same tag
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html\" charset=\"ISO-8859-1\"></head><body>Test</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaHttpEquivWithIllegalCharsetAttr() {
        // IllegalCharsetNameException handling inside meta http-equiv charset branch
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html\" charset=\"??invalid??\"></head><body>Safe</body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Safe", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaMatchesDefaultCharsetNoReDecode() {
        // When meta charset is UTF-8 (same as defaultCharset), no re-decode is needed
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>No Re-decode</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No Re-decode", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaWithQuotesInCharset() {
        // Quotes inside charset attribute value: charset="'ISO-8859-1'"
        String html = "<html><head><meta charset=\"'ISO-8859-1'\"></head><body>Quoted Charset</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Quoted Charset", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Buffer Reading
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] testData = "Hello World, DataUtil test.".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(testData);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);
        assertEquals(testData.length, byteBuffer.remaining());
        assertArrayEquals(testData, byteBuffer.array());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 0);
        assertEquals(0, byteBuffer.remaining());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCappedPartial() throws IOException {
        byte[] testData = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(testData);

        // Cap at 5 bytes
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, byteBuffer.remaining());
        byte[] readBytes = new byte[5];
        byteBuffer.get(readBytes);
        assertArrayEquals("01234".getBytes(StandardCharsets.UTF_8), readBytes);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCappedExactAndOver() throws IOException {
        byte[] testData = "0123456789".getBytes(StandardCharsets.UTF_8);

        // Cap exactly equal to stream size
        InputStream in1 = new ByteArrayInputStream(testData);
        ByteBuffer byteBuffer1 = DataUtil.readToByteBuffer(in1, 10);
        assertEquals(10, byteBuffer1.remaining());

        // Cap larger than stream size
        InputStream in2 = new ByteArrayInputStream(testData);
        ByteBuffer byteBuffer2 = DataUtil.readToByteBuffer(in2, 50);
        assertEquals(10, byteBuffer2.remaining());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferMultiChunkLargerThanBuffer() throws IOException {
        // Buffer size in DataUtil is 0x20000 = 131072 bytes. Generate 140,000 bytes.
        int totalSize = 140000;
        byte[] bigData = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            bigData[i] = (byte) (i % 127);
        }
        InputStream in = new ByteArrayInputStream(bigData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 0);

        assertEquals(totalSize, byteBuffer.remaining());
        assertArrayEquals(bigData, byteBuffer.array());
    }

    @Test(timeout = 4000)
    public void testParseByteDataEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);
        Document doc = DataUtil.parseByteData(emptyBuffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSizeThrows() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseByteDataEmptyCharsetThrows() {
        ByteBuffer buffer = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testLoadNonExistentFileThrows() throws IOException {
        File missingFile = new File("non_existent_jsoup_test_file_xyz.html");
        assertFalse(missingFile.exists());
        DataUtil.load(missingFile, "UTF-8", "http://example.com");
    }

    // =========================================================================
    // Partition E: File & Stream Load API Integrations & Class Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testLoadFileSuccess() throws IOException {
        File tempFile = File.createTempFile("jsoup-datautil-test", ".html");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("<html><head><title>File Load</title></head><body>File Body</body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File Load", doc.title());
        assertEquals("File Body", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamHtml() throws IOException {
        String content = "<html><head><title>Stream Load</title></head><body>Stream Body</body></html>";
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Stream Load", doc.title());
        assertEquals("Stream Body", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamXmlParser() throws IOException {
        String xml = "<root><item id=\"1\">Value</item></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        Element item = doc.select("item").first();
        assertNotNull(item);
        assertEquals("1", item.attr("id"));
        assertEquals("Value", item.text());
    }

    @Test(timeout = 4000)
    public void testPrivateConstructorContract() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }
}