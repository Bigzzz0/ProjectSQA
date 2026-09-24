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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Component: org.jsoup.helper.DataUtil
 *
 * 1. Defect-Targeted Branch Zone (Defects4J):
 *    - supportsXmlCharsetDeclaration: HTML parser parsing "<?xml version='1.0' encoding='ISO-8859-1'?>".
 *      When charset is null, htmlParser parses the XML prologue as a Comment node instead of an
 *      XmlDeclaration directly, causing foundCharset to remain null and defaulting to UTF-8. Non-ASCII
 *      characters (e.g., ISO-8859-1 "Hellö Wörld!") are corrupted into "Hell Wrld!".
 *
 * 2. BOM Detection Branches (detectCharsetFromBom):
 *    - UTF-32 Big Endian: [0x00, 0x00, 0xFE, 0xFF] -> UTF-32 (no offset)
 *    - UTF-32 Little Endian: [0xFF, 0xFE, 0x00, 0x00] -> UTF-32 (no offset)
 *    - UTF-16 Big Endian: [0xFE, 0xFF] -> UTF-16 (no offset)
 *    - UTF-16 Little Endian: [0xFF, 0xFE] -> UTF-16 (no offset)
 *    - UTF-8 with BOM: [0xEF, 0xBB, 0xBF] -> UTF-8 (offset = true, reader.skip(1))
 *    - No BOM / stream shorter than 4 bytes
 *
 * 3. parseInputStream & Encoding Resolution:
 *    - input == null -> returns new Document(baseUri)
 *    - charsetName is null:
 *        * Meta http-equiv="Content-Type" content="text/html; charset=..."
 *        * Meta charset="..." (HTML5)
 *        * XML Declaration encoding attribute
 *        * Content matches defaultCharset (UTF-8) -> no re-parse needed if fullyRead
 *        * Content not fully read in first read buffer (stream > firstReadBufferSize) -> re-reads remaining
 *        * foundCharset != UTF-8 -> re-decode with specified charset
 *    - charsetName specified:
 *        * Empty string "" -> IllegalArgumentException
 *        * Explicit charset (e.g. "UTF-8", "ISO-8859-1")
 *    - Stream throws IOException during parse -> UncheckedIOException unwrapped and thrown
 *
 * 4. Helper Methods:
 *    - crossStreams: stream copy verification
 *    - readToByteBuffer: maxSize < 0 validation, maxSize == 0 unlimited, maxSize > 0 limitation
 *    - readFileToByteBuffer: file reading to buffer and resource closure
 *    - emptyByteBuffer: buffer allocation check (capacity 0)
 *    - getCharsetFromContentType: regex matching, quoting variants, unsupported/illegal charsets
 *    - mimeBoundary: length 32 and character set verification
 * ---------------------------------------------------------------------------------------------------------
 */
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCrossStreamsCopiesContentCorrectly() throws IOException {
        byte[] sourceBytes = "Quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(sourceBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);

        assertArrayEquals(sourceBytes, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] expected = "Arbitrary test payload content for reading".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);

        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);

        assertEquals(expected.length, buf.remaining());
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferDefaultHelper() throws IOException {
        byte[] expected = "Single parameter readToByteBuffer test".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);

        ByteBuffer buf = DataUtil.readToByteBuffer(in);

        assertEquals(expected.length, buf.remaining());
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferWithMaxSizeLimit() throws IOException {
        byte[] inputData = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);

        ByteBuffer buf = DataUtil.readToByteBuffer(in, 5);

        assertEquals(5, buf.remaining());
        byte[] actual = new byte[5];
        buf.get(actual);
        assertArrayEquals("01234".getBytes(StandardCharsets.UTF_8), actual);
    }

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertNotNull(buf);
        assertEquals(0, buf.capacity());
        assertEquals(0, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryFormat() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
        for (char c : boundary.toCharArray()) {
            boolean valid = (c == '-') || (c == '_') ||
                    (c >= '0' && c <= '9') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z');
            assertTrue("Boundary char '" + c + "' is invalid", valid);
        }
    }

    @Test(timeout = 4000)
    public void testLoadFromFile() throws IOException {
        File tempFile = File.createTempFile("datautil_test", ".html");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("<html><body><p id='sample'>LoadedFromFile</p></body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "https://example.com");
        assertEquals("LoadedFromFile", doc.select("#sample").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testReadFileToByteBuffer() throws IOException {
        File tempFile = File.createTempFile("datautil_buf_test", ".bin");
        tempFile.deleteOnExit();
        byte[] fileData = new byte[]{1, 2, 3, 4, 5, 99};
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(fileData);
        }

        ByteBuffer buf = DataUtil.readFileToByteBuffer(tempFile);
        assertNotNull(buf);
        assertEquals(fileData.length, buf.remaining());
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals(fileData, actual);
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithExplicitParser() throws IOException {
        String xml = "<root><child key=\"val\">Text</child></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());

        assertEquals("Text", doc.select("child").text());
        assertEquals("val", doc.select("child").attr("key"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Encoding Metadata Detection
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseInputStreamWithNullInputYieldsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals(0, doc.children().size());
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeVariants() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='utf-8'"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html;charset=utf-8;param=value"));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset=gb2312"));

        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; boundary=something"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=UNSUPPORTED-UNKNOWN-CHARSET-12345"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=invalid/illegal*charset"));
    }

    @Test(timeout = 4000)
    public void testMetaHtml5CharsetDetection() throws IOException {
        String html = "<html><head><meta charset=\"windows-1252\"></head><body>£100</body></html>";
        byte[] bytes = html.getBytes("windows-1252");
        InputStream in = new ByteArrayInputStream(bytes);

        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("£100", doc.body().text());
        assertEquals("windows-1252", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testMetaHttpEquivContentTypeCharsetDetection() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\"></head><body>€50</body></html>";
        byte[] bytes = html.getBytes("windows-1252");
        InputStream in = new ByteArrayInputStream(bytes);

        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("€50", doc.body().text());
        assertEquals("windows-1252", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testStreamLargerThanFirstReadBufferSizeWithUtf8() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p>");
        for (int i = 0; i < 3000; i++) {
            sb.append("Large content line number ").append(i).append(";\n");
        }
        sb.append("</p></body></html>");
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        assertTrue("Input must be larger than firstReadBufferSize (5120 bytes)", bytes.length > 5120);

        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = DataUtil.load(in, null, "http://example.com");

        assertTrue(doc.body().text().contains("Large content line number 2999"));
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf8() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = "<html><body><p>UTF-8 with BOM</p></body></html>".getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(content, 0, combined, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("UTF-8 with BOM", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf16Be() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] content = "<html><body><p>UTF-16BE with BOM</p></body></html>".getBytes(StandardCharsets.UTF_16BE);
        byte[] combined = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(content, 0, combined, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("UTF-16BE with BOM", doc.select("p").text());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf16Le() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] content = "<html><body><p>UTF-16LE with BOM</p></body></html>".getBytes(StandardCharsets.UTF_16LE);
        byte[] combined = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(content, 0, combined, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("UTF-16LE with BOM", doc.select("p").text());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf32Be() throws IOException {
        byte[] bom = new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF};
        byte[] content = "<html><body><p>UTF-32BE</p></body></html>".getBytes("UTF-32BE");
        byte[] combined = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(content, 0, combined, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("UTF-32BE", doc.select("p").text());
        assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testBomDetectionUtf32Le() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x00};
        byte[] content = "<html><body><p>UTF-32LE</p></body></html>".getBytes("UTF-32LE");
        byte[] combined = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(content, 0, combined, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("UTF-32LE", doc.select("p").text());
        assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: org.jsoup.helper.DataUtilTest::supportsXmlCharsetDeclaration
     *
     * In the defective version, when parsing an XML document via the default htmlParser
     * without providing a charset parameter, the XML declaration <?xml version="1.0" encoding="ISO-8859-1"?>
     * is parsed as a Comment node (not an XmlDeclaration instance).
     * Consequently, foundCharset remains null and the document is incorrectly decoded as UTF-8,
     * corrupting "Hellö Wörld!" into "Hell Wrld!".
     */
    @Test(timeout = 4000)
    public void testSupportsXmlCharsetDeclaration() throws IOException {
        String encoding = "iso-8859-1";
        InputStream in = new ByteArrayInputStream((
                "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">Hellö Wörld!</html>"
        ).getBytes(encoding));

        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Hellö Wörld!", doc.text());
    }

    @Test(timeout = 4000)
    public void testSupportsXmlCharsetDeclarationWithXmlParser() throws IOException {
        String encoding = "ISO-8859-1";
        InputStream in = new ByteArrayInputStream((
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<root>Hellö Wörld!</root>"
        ).getBytes(encoding));

        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("Hellö Wörld!", doc.text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadToByteBufferNegativeMaxSizeThrowsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInputStreamWithEmptyCharsetThrowsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("<p>content</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.load(in, "", "http://example.com");
    }

    @Test(timeout = 4000)
    public void testParseInputStreamPropagatesIOException() {
        InputStream faultyStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated IO failure during read");
            }
        };

        try {
            DataUtil.load(faultyStream, "UTF-8", "http://example.com");
            fail("Expected IOException was not thrown");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Simulated IO failure"));
        }
    }

    // =========================================================================
    // Partition E: Boundary & Extreme Document Structures
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseEmptyStreamYieldsValidEmptyDocument() throws IOException {
        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        Document doc = DataUtil.load(emptyStream, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testShortStreamLessThanBomLength() throws IOException {
        ByteArrayInputStream shortStream = new ByteArrayInputStream(new byte[]{0x41, 0x42});
        Document doc = DataUtil.load(shortStream, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("AB", doc.text());
    }

    @Test(timeout = 4000)
    public void testDocumentWithNoCharsetMetaRemainsDefaultCharset() throws IOException {
        String html = "<html><head><title>No charset specified</title></head><body>Plain text</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("Plain text", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }
}