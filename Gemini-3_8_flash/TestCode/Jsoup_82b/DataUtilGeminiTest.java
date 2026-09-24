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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Methods Analyzed:
 * 1. parseInputStream(InputStream, String, String, Parser):
 *    - null input -> empty Document(baseUri)
 *    - BOM handling: UTF-32 (BE/LE), UTF-16 (BE/LE), UTF-8 (with BOM offset skip), No BOM
 *    - charsetName == null (sniffing path):
 *      - meta http-equiv="Content-Type"
 *      - meta charset="..."
 *      - XML declaration <?xml encoding="..."?> as XmlDeclaration or Comment
 *      - re-decode triggered if foundCharset != UTF-8
 *      - fullyRead handling (doc reset if !fullyRead)
 *    - charsetName != null: Validate.notEmpty() branch
 *    - Exception wrapping: UncheckedIOException rethrown as IOException
 *    - Target Defect: non-encodable charsets (e.g. ISO-2022-CN) must fall back to UTF-8
 * 2. readToByteBuffer(InputStream, int maxSize):
 *    - maxSize < 0 (IllegalArgumentException validation)
 *    - maxSize == 0 (unlimited)
 *    - maxSize > 0 (bounded read)
 * 3. emptyByteBuffer():
 *    - returns 0-capacity buffer
 * 4. getCharsetFromContentType(String):
 *    - null input
 *    - valid/invalid charset strings, quoted, whitespace, unsupported names
 * 5. validateCharset(String):
 *    - null, empty, invalid format, lowercase/uppercase support, illegal names
 * 6. mimeBoundary():
 *    - length is 32, characters belong to mimeBoundaryChars table
 * 7. crossStreams(InputStream, OutputStream):
 *    - stream copying across multiple buffer chunks
 * 8. load(File, ...), load(InputStream, ...):
 *    - file and stream loader overloads
 * ----------------------------------------------------------------------------------------------------
 */
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull("Buffer should not be null", buffer);
        assertEquals("Buffer should have 0 capacity", 0, buffer.capacity());
        assertEquals("Buffer should have 0 remaining", 0, buffer.remaining());
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryGeneration() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull("Boundary must not be null", boundary);
        assertEquals("Boundary length must be 32", 32, boundary.length());
        assertTrue("Boundary should match alphanumeric and '-' / '_'",
                boundary.matches("^[a-zA-Z0-9-_]{32}$"));
    }

    @Test(timeout = 4000)
    public void testCrossStreamsNormal() throws IOException {
        byte[] expected = "Testing crossStreams transfer functionality".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertArrayEquals("Output stream bytes must match input stream bytes", expected, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testCrossStreamsLargePayload() throws IOException {
        byte[] largeData = new byte[DataUtil.bufferSize * 2 + 512];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 127);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(largeData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertArrayEquals("Large payload must be fully and accurately copied", largeData, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferBounded() throws IOException {
        byte[] inputData = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);

        ByteBuffer buf = DataUtil.readToByteBuffer(in, 5);
        assertEquals("Read buffer limit must match requested maxSize", 5, buf.remaining());
        byte[] readBytes = new byte[5];
        buf.get(readBytes);
        assertEquals("01234", new String(readBytes, StandardCharsets.UTF_8));
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] inputData = "Unlimited reading stream data".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);

        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals("Read buffer should contain entire stream when maxSize=0", inputData.length, buf.remaining());
        byte[] readBytes = new byte[buf.remaining()];
        buf.get(readBytes);
        assertArrayEquals(inputData, readBytes);
    }

    @Test(timeout = 4000)
    public void testLoadFromFile() throws IOException {
        File temp = File.createTempFile("datautil_test", ".html");
        temp.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write("<p>File load test</p>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(temp, "UTF-8", "https://example.com/");
        assertEquals("File load test", doc.select("p").text());
        assertEquals("https://example.com/", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testLoadFromStreamWithParser() throws IOException {
        String html = "<div><span class='target'>Hello Parser</span></div>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "https://example.com/", Parser.htmlParser());
        assertEquals("Hello Parser", doc.select("span.target").text());
    }

    @Test(timeout = 4000)
    public void testLoadFromStreamConvenience() throws IOException {
        String html = "<title>Stream Load</title>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "https://example.com/");
        assertEquals("Stream Load", doc.title());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseInputStreamNullStream() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "https://example.com/", Parser.htmlParser());
        assertNotNull("Parsing null stream should return a non-null document", doc);
        assertEquals("Document baseUri should match argument", "https://example.com/", doc.baseUri());
        assertEquals("Document should be empty", 0, doc.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeNull() {
        assertNull("Null content-type should yield null charset", DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeEmptyAndNoMatch() {
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("   "));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; boundary=something"));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeVariousFormats() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='utf-8'"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1; other=value"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=  UTF-8 "));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeUnsupportedAndMalformed() {
        assertNull("Unsupported charset name should return null",
                DataUtil.getCharsetFromContentType("text/html; charset=NOT_A_REAL_CHARSET_NAME"));
        assertNull("Invalid syntax characters should return null",
                DataUtil.getCharsetFromContentType("text/html; charset=<>invalid*char"));
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf8() throws IOException {
        byte[] dataWithBom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'h', '1', '>', 'B', 'O', 'M', '<', '/', 'h', '1', '>'};
        ByteArrayInputStream in = new ByteArrayInputStream(dataWithBom);

        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertEquals("BOM", doc.select("h1").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf16BE() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] text = "<h1>UTF16BE</h1>".getBytes(StandardCharsets.UTF_16BE);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        ByteArrayInputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertEquals("UTF16BE", doc.select("h1").text());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf16LE() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] text = "<h1>UTF16LE</h1>".getBytes(StandardCharsets.UTF_16LE);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        ByteArrayInputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertEquals("UTF16LE", doc.select("h1").text());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testSniffMetaHttpEquivReDecode() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head>" +
                "<body><p>Sch\u00f6n</p></body></html>";
        byte[] isoBytes = html.getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayInputStream in = new ByteArrayInputStream(isoBytes);

        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertEquals("Sch\u00f6n", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testSniffMetaCharsetReDecode() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Sch\u00f6n</p></body></html>";
        byte[] isoBytes = html.getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayInputStream in = new ByteArrayInputStream(isoBytes);

        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertEquals("Sch\u00f6n", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testSniffXmlDeclarationEncoding() throws IOException {
        String xml = "<?xml encoding='ISO-8859-1'?><feed><entry>Sm\u00f6rg\u00e5sbord</entry></feed>";
        byte[] isoBytes = xml.getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayInputStream in = new ByteArrayInputStream(isoBytes);

        Document doc = DataUtil.parseInputStream(in, null, "", Parser.xmlParser());
        assertEquals("Sm\u00f6rg\u00e5sbord", doc.select("entry").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testSniffXmlDeclarationInsideComment() throws IOException {
        String htmlWithXmlComment = "<?xml encoding=\"ISO-8859-1\"?><html><body><p>Data</p></body></html>";
        byte[] bytes = htmlWithXmlComment.getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        // HtmlParser parses initial <?xml ...?> as a Comment with declaration attributes
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testSniffStreamLargerThanFirstReadBufferKeepsDocIfFullyRead() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        for (int i = 0; i < 6000; i++) {
            sb.append("A");
        }
        sb.append("</body></html>");
        byte[] utfBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(utfBytes);

        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertTrue(doc.text().startsWith("AAAA"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known failure:
     * org.jsoup.parser.HtmlParserTest::fallbackToUtfIfCantEncode
     * junit.framework.AssertionFailedError: expected:<[UTF-8]> but was:<[ISO-2022-CN]>
     *
     * When a document specifies a charset that Java can read/decode, but CANNOT encode
     * (such as ISO-2022-CN), DataUtil must fall back to UTF-8 for doc.outputSettings().charset().
     */
    @Test(timeout = 4000)
    public void testFallbackToUtfIfCantEncode() throws IOException {
        String in = "<meta charset=\"ISO-2022-CN\" />";
        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(in.getBytes(StandardCharsets.US_ASCII)),
                null,
                "",
                Parser.htmlParser()
        );
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadToByteBufferNegativeMaxSizeThrows() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[10]), -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInputStreamEmptyCharsetNameThrows() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("<p>Hello</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseInputStream(in, "", "", Parser.htmlParser());
    }

    @Test(timeout = 4000)
    public void testCrossStreamsEmptyInput() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testSniffUnknownCharsetFallsBackToDefaultUtf8() throws IOException {
        String html = "<meta charset=\"UNKNOWN-INVALID-CHARSET-12345\" /><p>Testing</p>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        assertEquals("Testing", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }
}