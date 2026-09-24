package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 -----------------------------------------------------------------------------------------------------------------------
 Target Class: org.jsoup.helper.DataUtil
 Core Decision Points & Branches Tested:
 1. BOM Detection (detectCharsetFromBom):
    - UTF-32 Big Endian: 0x00 0x00 0xFE 0xFF
    - UTF-32 Little Endian: 0xFF 0xFE 0x00 0x00
    - UTF-16 Big Endian: 0xFE 0xFF
    - UTF-16 Little Endian: 0xFF 0xFE
    - UTF-8 BOM: 0xEF 0xBB 0xBF (verifying position shifted by 3)
    - Under-sized buffers (< 4 bytes) and empty buffers
    - Non-BOM content preserving original charsetName
 2. Stream Buffer Reading (readToByteBuffer):
    - Negative maxSize -> IllegalArgumentException via Validate.isTrue
    - maxSize == 0 (unlimited reading)
    - maxSize > 0 where stream length < maxSize
    - maxSize > 0 where stream length > maxSize (capping behavior)
    - Large stream traversing multiple buffer chunks (> 0x20000 = 131,072 bytes)
 3. Stream Transfer (crossStreams):
    - Multi-chunk copying from InputStream to OutputStream
 4. Content-Type Charset Parsing (getCharsetFromContentType / validateCharset):
    - Null / empty contentType
    - Content-Type without charset parameter
    - Charset enclosed in double quotes: charset="UTF-8"
    - Charset enclosed in single quotes: charset='ISO-8859-1'
    - Bare charset: charset=gb2312
    - Trailing parameters: charset=UTF-8; format=flowed
    - Unsupported / illegal charset names triggering IllegalCharsetNameException -> returns null
 5. Document Byte Parsing (parseByteData):
    - Explicit charsetName passed vs. null (auto-detect)
    - Empty charset string -> IllegalArgumentException via Validate.notEmpty
    - HTML <meta http-equiv="Content-Type" content="..."> charset detection
    - HTML5 <meta charset="..."> charset detection
    - XML declaration prolog: <?xml version="1.0" encoding="ISO-8859-1"?> with Parser.xmlParser()
    - Re-decoding cycle when detected charset != defaultCharset ("UTF-8")
    - OutputSettings charset synchronization
 6. Known Defect Targeting (Defects4J):
    - Target: XML declaration encoding detection (Parser.xmlParser() and XmlDeclaration prolog)
      triggering re-decode to ISO-8859-1 and setting doc.outputSettings().charset().
 7. File Loading and Utilities:
    - readFileToByteBuffer and load(File, ...)
    - emptyByteBuffer() capacity and state
    - mimeBoundary() length (32) and character validity
 -----------------------------------------------------------------------------------------------------------------------
*/
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] payload = "Jsoup DataUtil Read Test String".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(payload);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);

        assertEquals(payload.length, buf.remaining());
        assertArrayEquals(payload, buf.array());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferDefaultUnlimited() throws IOException {
        byte[] payload = "Default unlimited read test".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(payload);
        ByteBuffer buf = DataUtil.readToByteBuffer(in);

        assertEquals(payload.length, buf.remaining());
        assertArrayEquals(payload, buf.array());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCapped() throws IOException {
        byte[] payload = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(payload);
        int capSize = 5;
        ByteBuffer buf = DataUtil.readToByteBuffer(in, capSize);

        assertEquals(capSize, buf.remaining());
        byte[] expected = "01234".getBytes(StandardCharsets.UTF_8);
        byte[] actual = new byte[capSize];
        buf.get(actual);
        assertArrayEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testCrossStreamsLargePayload() throws IOException {
        int targetSize = 0x20000 + 512; // Exceeds internal bufferSize
        byte[] payload = new byte[targetSize];
        for (int i = 0; i < targetSize; i++) {
            payload[i] = (byte) (i % 127);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(payload);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);

        byte[] result = out.toByteArray();
        assertEquals(targetSize, result.length);
        assertArrayEquals(payload, result);
    }

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertNotNull(buf);
        assertEquals(0, buf.capacity());
        assertEquals(0, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryContract() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());

        String validChars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < boundary.length(); i++) {
            char c = boundary.charAt(i);
            assertTrue("Char '" + c + "' not allowed in MIME boundary", validChars.indexOf(c) >= 0);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Content-Type Charset
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeBoundaryValues() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; nocharset=utf-8"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=   "));

        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset='ISO-8859-1'"));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset=gb2312"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8; boundary=something"));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeIllegalCharset() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=ILLEGAL$$$CHARSET"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=NON_EXISTENT_CHARSET_XYZ"));
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferStreamShorterThanCap() throws IOException {
        byte[] payload = "short".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(payload);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 100);

        assertEquals(payload.length, buf.remaining());
        assertArrayEquals(payload, buf.array());
    }

    // =========================================================================
    // Partition C: BOM Detection Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testDetectCharsetFromBomUtf32BE() {
        byte[] data = new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF, 'A'};
        ByteBuffer buf = ByteBuffer.wrap(data);
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectCharsetFromBomUtf32LE() {
        byte[] data = new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x00, 'A'};
        ByteBuffer buf = ByteBuffer.wrap(data);
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectCharsetFromBomUtf16BE() {
        byte[] data = new byte[]{(byte) 0xFE, (byte) 0xFF, 0x00, 'A'};
        ByteBuffer buf = ByteBuffer.wrap(data);
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectCharsetFromBomUtf16LE() {
        byte[] data = new byte[]{(byte) 0xFF, (byte) 0xFE, 'A', 0x00};
        ByteBuffer buf = ByteBuffer.wrap(data);
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectCharsetFromBomUtf8() {
        byte[] data = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'p', '>', 'h', 'i', '<', '/', 'p', '>'};
        ByteBuffer buf = ByteBuffer.wrap(data);
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("hi", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testDetectCharsetFromBomUnder4BytesBuffer() {
        byte[] data = new byte[]{'<', 'p'};
        ByteBuffer buf = ByteBuffer.wrap(data);
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // =========================================================================
    // Partition D: HTML Meta & Re-decode Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testMetaHttpEquivCharsetReDecode() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head>"
                + "<body><p>Test</p></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());

        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testMetaHtml5CharsetReDecode() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Hello HTML5</p></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());

        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Hello HTML5", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testMetaCharsetIllegalNameFallback() {
        String html = "<html><head><meta charset=\"ILLEGAL$$CHARSET\"></head><body><p>Fallback</p></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());

        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Fallback", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testMetaCharsetUtf8MatchesDefaultNoReDecode() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body><p>Same Charset</p></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());

        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Same Charset", doc.select("p").text());
    }

    // =========================================================================
    // Partition E: DEFECT-TARGETED ZONE (Defects4J XML Declaration Charset Detection)
    // =========================================================================

    /**
     * Targets Defects4J issue where XML declaration prolog encoding
     * (e.g. <?xml version="1.0" encoding="ISO-8859-1"?>) must be detected and re-decoded
     * using the XML parser.
     */
    @Test(timeout = 4000)
    public void testDetectCharsetEncodingDeclarationXml() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<data>äöüé</data>";
        byte[] xmlBytes = xml.getBytes(Charset.forName("ISO-8859-1"));

        InputStream in = new ByteArrayInputStream(xmlBytes);
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("äöüé", doc.select("data").text());
    }

    @Test(timeout = 4000)
    public void testXmlDeclarationWithHtmlParserDoesNotCrash() {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<data>test</data>";
        byte[] xmlBytes = xml.getBytes(Charset.forName("ISO-8859-1"));

        ByteBuffer buf = ByteBuffer.wrap(xmlBytes);
        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());

        assertNotNull(doc);
    }

    // =========================================================================
    // Partition F: Defensive Checks, File IO, & Overload Integrity
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseByteDataEmptyCharsetThrows() {
        ByteBuffer buf = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buf, "", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000)
    public void testParseByteDataExplicitCharset() {
        String content = "Hello ISO-8859-1";
        ByteBuffer buf = ByteBuffer.wrap(content.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buf, "ISO-8859-1", "http://example.com", Parser.htmlParser());

        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Hello ISO-8859-1", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamWithoutParser() throws IOException {
        String html = "<p>Default Parser Stream Test</p>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");

        assertNotNull(doc);
        assertEquals("Default Parser Stream Test", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testLoadFromFile() throws IOException {
        File tempFile = File.createTempFile("datautil_test", ".html");
        tempFile.deleteOnExit();

        String html = "<html><body><h1>File Test</h1></body></html>";
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(html.getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File Test", doc.select("h1").text());
    }

    @Test(timeout = 4000)
    public void testReadFileToByteBufferDirectly() throws IOException {
        File tempFile = File.createTempFile("datautil_raw", ".bin");
        tempFile.deleteOnExit();

        byte[] payload = new byte[]{10, 20, 30, 40, 50};
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(payload);
        }

        ByteBuffer buf = DataUtil.readFileToByteBuffer(tempFile);
        assertNotNull(buf);
        assertEquals(payload.length, buf.remaining());
        byte[] readBack = new byte[buf.remaining()];
        buf.get(readBack);
        assertArrayEquals(payload, readBack);
    }
}