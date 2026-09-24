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
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Methods in DataUtil:
 *  - load(File, String, String)
 *  - load(InputStream, String, String)
 *  - load(InputStream, String, String, Parser)
 *  - parseByteData(ByteBuffer, String, String, Parser)
 *  - readToByteBuffer(InputStream)
 *  - getCharsetFromContentType(String)
 *
 * Decision / Condition Coverage Targets:
 *  1. getCharsetFromContentType:
 *     - contentType == null -> returns null
 *     - charsetPattern match found vs not found
 *     - charset value case normalization (Defect jsoup: testCharset & testQuotedCharset expected lowercase)
 *     - quoted charset, unquoted charset, empty/spaces
 *  2. parseByteData:
 *     - charsetName == null (auto-detect from meta)
 *       * meta element found vs null
 *       * meta.hasAttr("http-equiv") vs meta[charset]
 *       * foundCharset: null, empty string, equals defaultCharset ("UTF-8"), or distinct charset (e.g. ISO-8859-1)
 *       * re-decode triggered (doc == null branch)
 *     - charsetName != null
 *       * Validate.notEmpty check (empty string triggers IllegalArgumentException)
 *       * Valid charset decode (e.g., UTF-8, ISO-8859-1)
 *     - doc == null post-processing:
 *       * BOM check: docData.charAt(0) == 65279 (\uFEFF) -> stripped
 *       * BOM check: docData.charAt(0) != 65279 -> kept intact
 *       * doc.outputSettings().charset(charsetName) verification
 *  3. readToByteBuffer:
 *     - Streams smaller than bufferSize (130K)
 *     - Streams larger than bufferSize (multi-pass buffer loop)
 *     - Empty stream (0 bytes)
 *  4. load(File, ...):
 *     - Valid file parsing
 *     - Missing file / IOException handling in try-finally stream closing
 * ---------------------------------------------------------------------------------------------------------
 */
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets defect where getCharsetFromContentType unexpectedly upper-cases or
     * mishandles the extracted charset format.
     */
    @Test(timeout = 4000)
    public void testCharset() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html;charset=utf-8"));
    }

    /**
     * Targets defect with quoted charset attribute in Content-Type header.
     */
    @Test(timeout = 4000)
    public void testQuotedCharset() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeVariants() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; no-charset=foo"));
        
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/html; charset=US-ASCII; charset=somethingelse"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset = \"UTF-8\" ; other=param"));
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamWithExplicitCharset() throws IOException {
        String html = "<html><head><title>Test Page</title></head><body><p>Hello World</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com/");
        assertNotNull(doc);
        assertEquals("Test Page", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("UTF-8", doc.outputSettings().charset().displayName());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamWithCustomParser() throws IOException {
        String xml = "<rss><channel><title>RSS Title</title></channel></rss>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com/rss", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("RSS Title", doc.select("title").text());
    }

    @Test(timeout = 4000)
    public void testLoadFile() throws IOException {
        File tempFile = File.createTempFile("datautil_test", ".html");
        tempFile.deleteOnExit();
        
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write("<html><head><title>File Load</title></head><body>Content</body></html>".getBytes("UTF-8"));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://file.example.com/");
        assertNotNull(doc);
        assertEquals("File Load", doc.title());
        assertEquals("http://file.example.com/", doc.baseUri());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadToByteBufferLargeStreamMultipleBuffers() throws IOException {
        // Buffer size is 0x20000 (131072 bytes). Create stream of ~150K bytes
        int totalBytes = 150000;
        byte[] payload = new byte[totalBytes];
        for (int i = 0; i < totalBytes; i++) {
            payload[i] = (byte) ('a' + (i % 26));
        }

        InputStream in = new ByteArrayInputStream(payload);
        ByteBuffer bb = DataUtil.readToByteBuffer(in);

        assertNotNull(bb);
        assertEquals(totalBytes, bb.remaining());
        assertEquals('a', bb.get(0));
        assertEquals('b', bb.get(1));
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer bb = DataUtil.readToByteBuffer(in);

        assertNotNull(bb);
        assertEquals(0, bb.remaining());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithBomMarker() throws IOException {
        // BOM \uFEFF followed by HTML content
        String htmlWithBOM = "\uFEFF<html><head><title>BOM Test</title></head><body>BOM Body</body></html>";
        byte[] bytes = htmlWithBOM.getBytes("UTF-8");
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(bb, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM Test", doc.title());
        assertFalse("BOM marker must be stripped", doc.html().startsWith("\uFEFF"));
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithoutBomMarker() {
        String html = "<html><head><title>No BOM</title></head><body>No BOM Body</body></html>";
        byte[] bytes = html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(bb, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No BOM", doc.title());
    }

    // =========================================================================
    // Partition D: Meta Tag Re-Decoding & Branch Exploration
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseByteDataMetaHttpEquivRedecode() {
        // Charset is initially null -> parsed as UTF-8 -> detects ISO-8859-1 -> re-decodes
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><title>Re-Decode</title></head><body>Hello</body></html>";
        ByteBuffer bb = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(bb, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Re-Decode", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaCharsetHtml5Redecode() {
        // HTML5 meta charset tag
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>HTML5 Re-Decode</title></head><body>Content</body></html>";
        ByteBuffer bb = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(bb, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("HTML5 Re-Decode", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaCharsetEqualsDefaultCharset() {
        // Meta charset matches defaultCharset "UTF-8", no re-decode needed
        String html = "<html><head><meta charset=\"UTF-8\"><title>Default Charset</title></head><body>Content</body></html>";
        ByteBuffer bb = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(bb, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Default Charset", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaCharsetEmpty() {
        // Meta charset attribute is empty
        String html = "<html><head><meta charset=\"\"><title>Empty Charset</title></head><body>Content</body></html>";
        ByteBuffer bb = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(bb, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Empty Charset", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataNoMetaTag() {
        // No meta tag at all, charsetName is null -> stays UTF-8 without re-decode
        String html = "<html><head><title>No Meta</title></head><body>Content</body></html>";
        ByteBuffer bb = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(bb, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No Meta", doc.title());
    }

    // =========================================================================
    // Partition E: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseByteDataEmptyCharsetThrowsException() {
        ByteBuffer bb = ByteBuffer.wrap("<html><body>Test</body></html>".getBytes());
        DataUtil.parseByteData(bb, "", "http://example.com", Parser.htmlParser());
    }

    @Test(expected = UnsupportedCharsetException.class, timeout = 4000)
    public void testParseByteDataUnsupportedCharsetThrowsException() {
        ByteBuffer bb = ByteBuffer.wrap("<html><body>Test</body></html>".getBytes());
        DataUtil.parseByteData(bb, "UNSUPPORTED_CHARSET_XYZ", "http://example.com", Parser.htmlParser());
    }

    @Test(expected = IllegalCharsetNameException.class, timeout = 4000)
    public void testParseByteDataIllegalCharsetThrowsException() {
        ByteBuffer bb = ByteBuffer.wrap("<html><body>Test</body></html>".getBytes());
        DataUtil.parseByteData(bb, "illegal charset? name", "http://example.com", Parser.htmlParser());
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testLoadNonExistentFileThrowsException() throws IOException {
        File missingFile = new File("non_existent_file_jsoup_test_" + System.currentTimeMillis() + ".html");
        DataUtil.load(missingFile, "UTF-8", "http://example.com");
    }

    @Test(timeout = 4000)
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InstantiationException {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }
}