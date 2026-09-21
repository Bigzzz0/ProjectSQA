package org.jsoup.helper;

import org.jsoup.UncheckedIOException;
import org.jsoup.internal.ConstrainableInputStream;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import static org.junit.Assert.*;

/**
 * Test suite for DataUtil utility class.
 *
 * [Branch & Defect Analysis Matrix]
 * 
 * **Target Branches in parseInputStream:**
 * - input == null → early return with empty Document
 * - BOM detection (UTF-8, UTF-16, UTF-32, no BOM)
 * - charsetName == null → meta charset / XML declaration detection path
 *   - found charset != defaultCharset → re-decode with found charset
 *   - found charset == defaultCharset → keep UTF-8 if fully read, else null doc to re-parse
 *   - not fully read → doc = null to force re-parse with detected charset
 * - charsetName != null → skip detection, use provided charset
 * - doc == null → create reader with charset, parse, set output charset
 * - fallback to UTF-8 when specified charset cannot encode (defect area)
 * 
 * **Boundary Conditions:**
 * - maxSize in readToByteBuffer: 0, positive, negative
 * - getCharsetFromContentType: null input, empty, valid, invalid charset name
 * - validateCharset: null, empty, trimmed, quoted, unsupported names
 * - BOM detection with insufficient bytes (<4)
 * - mimeBoundary length
 * - crossStreams with empty/large streams
 * 
 * **Defect Target:**
 * - When charsetName is provided (e.g., ISO-2022-CN) but the charset cannot encode,
 *   the output charset should fallback to UTF-8. The bug keeps the original.
 */
public class DataUtilDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testParseInputStreamNullInputReturnsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertTrue(doc.children().isEmpty());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithExplicitCharset() throws IOException {
        String html = "<html><head></head><body>Hello</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.parseInputStream(in, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamDetectsBomUtf8() throws IOException {
        byte[] bomUtf8 = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H', 'e', 'l', 'l', 'o'};
        InputStream in = new ByteArrayInputStream(bomUtf8);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamDetectsBomUtf16() throws IOException {
        byte[] bomUtf16BE = new byte[]{(byte) 0xFE, (byte) 0xFF, 0, 'H', 0, 'i'};
        InputStream in = new ByteArrayInputStream(bomUtf16BE);
        // Should work with any content that is valid UTF-16BE, but we need minimal text
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        // BOM detection sets charset to UTF-16, then parsing should succeed
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testParseInputStreamDetectsMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>\u00E9</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("\u00E9", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamDetectsXmlDeclaration() throws IOException {
        String html = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><text>\u00E9</text></root>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("\u00E9", doc.text());
    }

    // ---------- Partition B: Boundary Value Analysis & Extremes ----------

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] data = "Test data".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals(data.length, buf.limit());
        byte[] result = new byte[buf.limit()];
        buf.get(result);
        assertArrayEquals(data, result);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[1]), -1);
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeEmpty() {
        assertNull(DataUtil.getCharsetFromContentType(""));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeValid() {
        String contentType = "text/html; charset=EUC-JP";
        assertEquals("EUC-JP", DataUtil.getCharsetFromContentType(contentType));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeInvalid() {
        // "not-supported" is not a valid charset; validateCharset returns null
        String contentType = "text/html; charset=not-supported";
        assertNull(DataUtil.getCharsetFromContentType(contentType));
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryLength() {
        String boundary = DataUtil.mimeBoundary();
        assertEquals(32, boundary.length());
        // should contain only allowed characters
        assertTrue(boundary.matches("[-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ]+"));
    }

    @Test(timeout = 4000)
    public void testCrossStreamsEmpty() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(new ByteArrayInputStream(new byte[0]), out);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testCrossStreamsData() throws IOException {
        byte[] data = "Hello, World!".getBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(new ByteArrayInputStream(data), out);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertEquals(0, buf.capacity());
        assertEquals(0, buf.limit());
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------

    /**
     * This test targets the known defect: when a charset that cannot encode (e.g., ISO-2022-CN) is specified,
     * the code should fallback to UTF-8 for output, but the bug leaves the original charset.
     */
    @Test(timeout = 4000)
    public void testFallbackToUtfIfCantEncode() throws IOException {
        // Create a simple HTML document with content that can be read with ISO-2022-CN but the charset cannot encode
        // We'll use bytes that are valid for ISO-2022-CN (ASCII range)
        String html = "<html><head><title>Test</title></head><body>Hello</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-2022-CN"));
        Document doc = DataUtil.parseInputStream(in, "ISO-2022-CN", "http://example.com", Parser.htmlParser());
        // Expected: the output charset should fallback to UTF-8 because ISO-2022-CN cannot encode all characters
        // (e.g., it cannot encode ASCII? Actually it can encode ASCII, but the defect indicates that it should fallback)
        // Per bug: expected UTF-8 but got ISO-2022-CN. So assert output charset is UTF-8.
        assertEquals("Output charset should fallback to UTF-8", "UTF-8", doc.outputSettings().charset().name());
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseInputStreamWithEmptyCharsetName() throws IOException {
        // charsetName = "" should be rejected by Validate.notEmpty
        DataUtil.parseInputStream(new ByteArrayInputStream(new byte[0]), "", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = UnsupportedCharsetException.class)
    public void testParseInputStreamWithUnsupportedCharset() throws IOException {
        // "unsupported" is not a valid charset, will throw when creating InputStreamReader
        DataUtil.parseInputStream(new ByteArrayInputStream("test".getBytes()), "unsupported", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeWithQuotes() {
        // content type may have quotes around charset
        String contentType = "text/html; charset=\"UTF-8\"";
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType(contentType));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeWithExtraSpaces() {
        String contentType = "text/html ; charset = UTF-8 ";
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType(contentType));
    }

    // ---------- Coverage for private helper via public methods ----------

    @Test(timeout = 4000)
    public void testLoadFile() throws IOException {
        // Create a temporary file with known content
        File tempFile = File.createTempFile("test", ".html");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("<html><body>OK</body></html>".getBytes("UTF-8"));
        }
        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("OK", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testLoadStreamWithParser() throws IOException {
        String html = "<root><item>value</item></root>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("value", doc.selectFirst("item").text());
    }

    @Test(timeout = 4000)
    public void testBomDetectionInsufficientBytes() throws IOException {
        // Only 2 bytes available – BOM detection should not match any pattern
        byte[] smallData = new byte[]{(byte) 0xFE, (byte) 0xBB};
        InputStream in = new ByteArrayInputStream(smallData);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // default UTF-8 should be used
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamNotFullyRead() throws IOException {
        // Create a stream that is larger than firstReadBufferSize (5120) to ensure not fully read
        byte[] data = new byte[10000];
        // Fill with some pattern
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        // Wrap with minimal HTML? Actually this will likely cause parse error, but we just want to hit the not-fully-read branch
        // Instead, use a valid HTML string that is larger than firstReadBufferSize
        StringBuilder sb = new StringBuilder("<html><head>");
        for (int i = 0; i < 2000; i++) {
            sb.append("a");
        }
        sb.append("</head><body>content</body></html>");
        byte[] largeHtml = sb.toString().getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(largeHtml);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testValidateCharsetWithQuotesAndSpaces() {
        // Indirect test via getCharsetFromContentType
        String contentType = "text/html; charset=\" UTF-8 \"";
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType(contentType));
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithNoMetaAndNotFullyReadTriggersReParse() throws IOException {
        // Provide a short HTML with no charset info, but not fully read? Actually firstReadBufferSize is 5120, so a small stream is fully read.
        // We need a stream larger than 5120 to be not fully read. Use 6000 bytes.
        StringBuilder sb = new StringBuilder("<html><head>");
        for (int i = 0; i < 5000; i++) {
            sb.append("x");
        }
        sb.append("</head><body>hello</body></html>");
        byte[] data = sb.toString().getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamDetectsXmlDeclarationFromComment() throws IOException {
        // XML declaration inside a comment
        String html = "<!--?xml version=\"1.0\" encoding=\"ISO-8859-1\"?--><root>\u00E9</root>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("\u00E9", doc.text());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithOnlyCommentAndXmlDeclaration() throws IOException {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><data></data>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithBomAndSpecifiedCharsetOverridesBom() throws IOException {
        // If charsetName is provided, BOM detection still runs but should not override the provided charset
        // Actually the code: if bomCharset != null, charsetName = bomCharset.charset; so BOM overrides.
        // But the spec says: "look for BOM - overrides any other header or input". So that's correct.
        // We test that BOM overrides even if charsetName is given.
        byte[] bomUtf8 = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H', 'e', 'l', 'l', 'o'};
        InputStream in = new ByteArrayInputStream(bomUtf8);
        Document doc = DataUtil.parseInputStream(in, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        // BOM should force UTF-8
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testCrossStreamsWithNoFlush() throws IOException {
        // Just verify that the method works without flushing streams
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = "test".getBytes();
        DataUtil.crossStreams(new ByteArrayInputStream(data), out);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferMaxSizeLessThanData() throws IOException {
        byte[] data = "1234567890".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, buf.limit());
        byte[] result = new byte[5];
        buf.get(result);
        assertArrayEquals(new byte[]{'1','2','3','4','5'}, result);
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeCaseInsensitive() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; CHARSET=UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeWithMultipleCharsetParams() {
        // Pattern only finds first match
        String ct = "text/html; charset=ISO-8859-1; charset=UTF-8";
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType(ct));
    }

    @Test(timeout = 4000)
    public void testLoadWithNullCharsetName() throws IOException {
        // When charsetName is null, detection should run
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>\u00E9</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithBomUtf32() throws IOException {
        // UTF-32 BE BOM: 00 00 FE FF
        byte[] bomUtf32BE = new byte[]{0x00, 0x00, (byte)0xFE, (byte)0xFF, 0x00, 0x00, 0x00, 'H'};
        InputStream in = new ByteArrayInputStream(bomUtf32BE);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        // Should detect UTF-32
        assertNotNull(doc);
        // Actually parsing might fail because content is minimal, but the detection should set charset to UTF-32
        // We just verify that no exception and document exists
        assertTrue(doc.outputSettings().charset().name().toUpperCase().contains("UTF-32"));
    }
}