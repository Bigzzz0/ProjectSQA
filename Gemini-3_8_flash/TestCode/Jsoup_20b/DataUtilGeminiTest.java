package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.helper.DataUtil
 *
 * Decision / Condition Matrix:
 * 1. parseByteData(ByteBuffer, String, String, Parser):
 *    - charsetName == null:
 *        - no meta charset tag -> fallback defaultCharset (UTF-8), doc != null
 *        - meta tag with http-equiv -> getCharsetFromContentType()
 *        - meta tag with charset attribute -> HTML5 style meta charset
 *        - foundCharset != null && foundCharset.length() != 0 && !foundCharset.equals(defaultCharset)
 *            -> re-decode with foundCharset, rewind buffer, set doc = null
 *        - foundCharset equals defaultCharset -> keep initial doc
 *        - foundCharset empty or null -> keep initial doc
 *    - charsetName != null:
 *        - Validate.notEmpty(charsetName) -> throws IllegalArgumentException if empty string
 *        - Valid charsetName decoded -> doc == null path triggered
 *    - doc == null:
 *        - BOM handling: UTF-8 BOM (\uFEFF) at start of text [DEFECT ZONE]
 *        - doc.outputSettings().charset(charsetName)
 *
 * 2. readToByteBuffer(InputStream):
 *    - Stream size < bufferSize (single read loop termination)
 *    - Stream size > bufferSize (multiple read loop iterations across boundary 0x20000 / 130KB)
 *    - Stream size == 0 (empty stream)
 *
 * 3. getCharsetFromContentType(String):
 *    - contentType == null -> return null
 *    - contentType without charset parameter -> return null
 *    - contentType with unquoted charset -> trimmed and uppercase returned
 *    - contentType with quoted charset (e.g., charset="UTF-8") -> matched and stripped
 *    - contentType with empty charset (e.g., charset=) -> return ""
 *
 * 4. load(File / InputStream, String, String, Parser):
 *    - Normal file loading and stream closure verification
 *    - Non-existent file -> FileNotFoundException
 *    - HTML vs XML parser selection
 *
 * Defects4J Targeted Defect:
 * - org.jsoup.helper.DataUtilTest::discardsSpuriousByteOrderMark:
 *   When a file or byte stream contains a leading BOM (\uFEFF), HTML parser fails to discard it,
 *   shifting the parser state into body/character mode prematurely and losing the head title.
 * ----------------------------------------------------------------------------------------------------
 */
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: Spurious leading Byte Order Mark (\uFEFF) causes premature parser state
     * transition, resulting in empty head/title.
     */
    @Test(timeout = 4000)
    public void discardsSpuriousByteOrderMark() {
        String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(html);
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://foo.com", Parser.htmlParser());
        assertEquals("One", doc.head().text());
        assertEquals("Two", doc.body().text());
    }

    @Test(timeout = 4000)
    public void discardsSpuriousByteOrderMarkWhenCharsetNull() {
        String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(html);
        Document doc = DataUtil.parseByteData(buffer, null, "http://foo.com", Parser.htmlParser());
        assertEquals("One", doc.head().text());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Meta Charset Switching
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseByteDataWithExplicitCharset() {
        String html = "<html><head><title>Test</title></head><body>Hello World</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());

        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello World", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaHttpEquivRedecode() {
        // Japanese text in Shift_JIS with meta http-equiv
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"><title>\u65e5\u672c</title></head><body>\u6771\u4eac</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("Shift_JIS")));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("\u65e5\u672c", doc.title());
        assertEquals("\u6771\u4eac", doc.body().text());
        assertEquals("Shift_JIS", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaHtml5CharsetRedecode() {
        // ISO-8859-1 with HTML5 <meta charset="...">
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>Caf\u00e9</title></head><body>Cr\u00e8me</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Caf\u00e9", doc.title());
        assertEquals("Cr\u00e8me", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaCharsetMatchesDefaultUtf8() {
        // Meta charset explicitly set to UTF-8: should not trigger re-decoding
        String html = "<html><head><meta charset=\"UTF-8\"><title>Default</title></head><body>No Redecode</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Default", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaWithoutCharset() {
        // Meta tag present, but without charset specification
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\"><title>No Charset</title></head><body>Fallback</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No Charset", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataMetaEmptyCharset() {
        // Meta tag with empty charset parameter
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"><title>Empty</title></head><body>Content</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Empty", doc.title());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamThreeParams() throws IOException {
        String html = "<html><head><title>Stream 3</title></head><body>Data</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Stream 3", doc.title());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamFourParamsWithXmlParser() throws IOException {
        String xml = "<response status=\"ok\"><message>Processed</message></response>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("Processed", doc.select("message").text());
        assertEquals("ok", doc.select("response").attr("status"));
    }

    @Test(timeout = 4000)
    public void testLoadFileSuccess() throws IOException {
        File temp = File.createTempFile("jsoup-datautil-", ".html");
        temp.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write("<html><head><title>From File</title></head><body>File Data</body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(temp, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("From File", doc.title());
        assertEquals("File Data", doc.body().text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Buffer Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadToByteBufferEmptyStream() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(emptyStream);

        assertNotNull(byteBuffer);
        assertEquals(0, byteBuffer.limit());
        assertEquals(0, byteBuffer.remaining());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferExceedingInternalBuffer() throws IOException {
        // Internal bufferSize is 0x20000 (131072 bytes). Read 200,000 bytes to force multiple loop passes.
        int payloadSize = 200000;
        byte[] payload = new byte[payloadSize];
        for (int i = 0; i < payloadSize; i++) {
            payload[i] = (byte) (i % 127);
        }

        InputStream stream = new ByteArrayInputStream(payload);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(stream);

        assertNotNull(byteBuffer);
        assertEquals(payloadSize, byteBuffer.limit());
        assertArrayEquals(payload, byteBuffer.array());
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeVariations() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; boundary=something"));

        // Case insensitivity, spacing, and quotes
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; CHARSET=utf-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("EUC-JP", DataUtil.getCharsetFromContentType("text/html; charset=  \"euc-jp\"  ; other=param"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1; boundary=xyz"));
        assertEquals("", DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseByteDataThrowsOnEmptyCharsetName() {
        ByteBuffer buffer = ByteBuffer.wrap("<html><body>Test</body></html>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = FileNotFoundException.class)
    public void testLoadNonExistentFileThrowsException() throws IOException {
        File nonExistentFile = new File("non_existent_file_" + System.nanoTime() + ".html");
        DataUtil.load(nonExistentFile, "UTF-8", "http://example.com");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Private Constructor Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorViaReflection() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", (constructor.getModifiers() & java.lang.reflect.Modifier.PRIVATE) != 0);
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        assertNotNull("Instance should be creatable via reflection", instance);
    }
}