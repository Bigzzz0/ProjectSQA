package org.jsoup.helper;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * White-box test suite for DataUtil.
 * Targets maximum line/branch coverage and the known Defects4J defect supportsBOMinFiles.
 *
 * [Branch & Defect Analysis Matrix]
 * Branch targets:
 *  - parseByteData: charsetName==null vs not null
 *  - parseByteData: meta http-equiv vs meta charset, both found/not found
 *  - parseByteData: foundCharset valid/invalid/empty/null
 *  - parseByteData: BOM detection (first char UNICODE_BOM)
 *  - parseByteData: doc==null after meta processing or after BOM
 *  - getCharsetFromContentType: null input, no match, match with charset=, unsupported charset, uppercase fallback
 *  - readToByteBuffer: capped mode, maxSize boundary, read==-1, read>remaining
 *  - mimeBoundary: length and character validity
 *  - load: delegation to parseByteData
 *
 * Defect target: BOM handling in parseByteData.
 * Known failure: supportsBOMinFiles assertion fails when BOM is present in a file.
 * Additional test: BOM with and without charset specification, ensuring BOM stripped and charset set to UTF-8.
 */
public class DataUtilDeepseekTest {

    // Helper: create ByteBuffer from string with given charset
    private ByteBuffer bufferFromString(String s, Charset charset) {
        return ByteBuffer.wrap(s.getBytes(charset));
    }

    // Helper: create ByteBuffer from string with UTF-8
    private ByteBuffer utf8Buffer(String s) {
        return bufferFromString(s, StandardCharsets.UTF_8);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndNoMeta() {
        String html = "<html><head></head><body>Hello</body></html>";
        ByteBuffer data = utf8Buffer(html);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
        // default charset should be UTF-8
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndMetaHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body>Hello</body></html>";
        ByteBuffer data = bufferFromString(html, StandardCharsets.ISO_8859_1);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name().toUpperCase());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndMetaCharset() {
        String html = "<html><head><meta charset=\"UTF-16\"></head><body>Hello</body></html>";
        ByteBuffer data = bufferFromString(html, StandardCharsets.UTF_16);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithSpecifiedCharset() {
        String html = "<html><head></head><body>Héllo</body></html>";
        ByteBuffer data = bufferFromString(html, StandardCharsets.ISO_8859_1);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, "ISO-8859-1", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Héllo", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name().toUpperCase());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithSpecifiedCharsetAndBOM() {
        // Input with UTF-8 BOM and specified charset (should ignore BOM)
        String html = "\uFEFF<html><head><meta charset=\"UTF-8\"></head><body>Hello</body></html>";
        ByteBuffer data = utf8Buffer(html);
        // Even though charset is specified, BOM should be stripped and doc redecoded as UTF-8
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
        // Check that BOM not present in output
        assertFalse(doc.toString().contains("\uFEFF"));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void parseByteDataWithEmptyByteBuffer() {
        ByteBuffer data = ByteBuffer.allocate(0);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        // empty input yields empty document
        assertTrue(doc.body().text().isEmpty());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithNullCharsetAndInvalidMetaCharset() {
        String html = "<html><head><meta charset=\"Unsupported-Charset\"></head><body>Hello</body></html>";
        ByteBuffer data = utf8Buffer(html);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        // invalid charset should fall back to UTF-8
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void readToByteBufferWithNegativeMaxSize() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(timeout = 4000)
    public void readToByteBufferMaxSizeZero() throws IOException {
        byte[] data = new byte[10];
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals(10, buf.remaining());
    }

    @Test(timeout = 4000)
    public void readToByteBufferCappedExact() throws IOException {
        byte[] data = new byte[100];
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 100);
        assertEquals(100, buf.remaining());
    }

    @Test(timeout = 4000)
    public void readToByteBufferCappedSmaller() throws IOException {
        byte[] data = new byte[100];
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 50);
        assertEquals(50, buf.remaining());
    }

    @Test(timeout = 4000)
    public void readToByteBufferCappedLarger() throws IOException {
        byte[] data = new byte[50];
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 100);
        assertEquals(50, buf.remaining());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (BOM Handling) ====================

    /**
     * Regression test for Defects4J bug: supportsBOMinFiles fails.
     * This test reproduces the scenario: file with UTF-8 BOM and no charset specified.
     * Expected: BOM stripped, document parsed as UTF-8.
     */
    @Test(timeout = 4000)
    public void parseByteDataWithBOMAndNoCharsetSpecified() {
        String html = "\uFEFF<html><head><title>BOM Test</title></head><body>Hello</body></html>";
        ByteBuffer data = utf8Buffer(html);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull("Document should not be null", doc);
        assertEquals("Title should be parsed", "BOM Test", doc.title());
        assertEquals("Body text should be Hello", "Hello", doc.body().text());
        // BOM character should not appear in the output
        String outerHtml = doc.outerHtml();
        assertFalse("Output should not contain BOM character", outerHtml.contains("\uFEFF"));
        // charset should be UTF-8
        assertEquals("Charset should be UTF-8", "UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithBOMAndCharsetFromMeta() {
        // File has BOM and meta charset=UTF-8 (same)
        String html = "\uFEFF<html><head><meta charset=\"UTF-8\"><title>BOM Meta</title></head><body>Hello</body></html>";
        ByteBuffer data = utf8Buffer(html);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM Meta", doc.title());
        assertEquals("Hello", doc.body().text());
        // BOM should be stripped, charset UTF-8
        assertFalse(doc.outerHtml().contains("\uFEFF"));
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void parseByteDataWithBOMAndDifferentMetaCharset() {
        // BOM (UTF-8) but meta charset says ISO-8859-1 -> should follow meta and re-decode
        // BOM will be lost during re-decode? Actually BOM is a UTF-8 sequence, if re-decoded as ISO-8859-1 it becomes garbage.
        // The method handles BOM after meta processing: if BOM found, rewind and decode as UTF-8, strip BOM.
        // That overrides meta charset. So expected: document is UTF-8, meta charset ignored.
        String html = "\uFEFF<html><head><meta charset=\"ISO-8859-1\"><title>BOM vs Meta</title></head><body>Hello</body></html>";
        ByteBuffer data = utf8Buffer(html);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        // BOM handling will re-decode as UTF-8, so title and body come through correctly
        assertEquals("BOM vs Meta", doc.title());
        assertFalse(doc.outerHtml().contains("\uFEFF"));
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeNoMatch() {
        assertNull(DataUtil.getCharsetFromContentType("text/plain"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeValid() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeUnsupported() {
        // Should return null if charset is unsupported
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeUppercaseFallback() {
        // If given lowercase not supported but uppercase is (rare), should try uppercase
        // Use a real charset but force lowercase variant (e.g., "utf-8" is supported, but make it "utf-8" anyway)
        // Actually all charsets support lowercase. Use a made-up one.
        // The method does uppercase if first attempt fails. For unsupported, still returns null.
        // We can't easily test fallback without a charset that fails lowercase but works uppercase.
        // Instead test that it tries uppercase: e.g., "x-unknown" fails both.
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=x-unknown"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void parseByteDataEmptyCharsetName() {
        // Specifying empty charset should throw (due to Validate.notEmpty)
        ByteBuffer data = utf8Buffer("<html></html>");
        DataUtil.parseByteData(data, "", "http://example.com", org.jsoup.parser.Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = IllegalCharsetNameException.class)
    public void parseByteDataInvalidCharsetName() {
        ByteBuffer data = utf8Buffer("<html></html>");
        DataUtil.parseByteData(data, "invalid-charset", "http://example.com", org.jsoup.parser.Parser.htmlParser());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void mimeBoundaryLength() {
        String boundary = DataUtil.mimeBoundary();
        assertEquals("Boundary must be 32 characters", 32, boundary.length());
    }

    @Test(timeout = 4000)
    public void mimeBoundaryCharacters() {
        String boundary = DataUtil.mimeBoundary();
        for (char c : boundary.toCharArray()) {
            assertTrue("Character " + c + " not allowed in boundary",
                    (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                    c == '-' || c == '_');
        }
    }

    @Test(timeout = 4000)
    public void mimeBoundaryRandomness() {
        // Ensure two calls produce different boundaries (probabilistic)
        String b1 = DataUtil.mimeBoundary();
        String b2 = DataUtil.mimeBoundary();
        assertNotEquals("Two boundaries should not be identical", b1, b2);
    }

    @Test(timeout = 4000)
    public void emptyByteBufferReturnsEmpty() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertNotNull(buf);
        assertEquals(0, buf.capacity());
    }

    @Test(timeout = 4000)
    public void readFileToByteBuffer() throws IOException {
        // Use a temporary file
        java.io.File temp = java.io.File.createTempFile("test", ".txt");
        temp.deleteOnExit();
        java.io.FileOutputStream out = new java.io.FileOutputStream(temp);
        out.write("Hello".getBytes(StandardCharsets.UTF_8));
        out.close();

        ByteBuffer buf = DataUtil.readFileToByteBuffer(temp);
        assertEquals(5, buf.remaining());
        byte[] bytes = new byte[5];
        buf.get(bytes);
        assertEquals("Hello", new String(bytes, StandardCharsets.UTF_8));
    }

    @Test(timeout = 4000)
    public void crossStreamsCopies() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("Test data".getBytes(StandardCharsets.UTF_8));
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        assertEquals("Test data", out.toString("UTF-8"));
    }
}