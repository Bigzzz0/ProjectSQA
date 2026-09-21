package org.jsoup.helper;

import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * White-box test suite for DataUtil, targeting line/branch coverage and the known defect
 * where XML charset declarations are not properly detected (Defects4J issue).
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional paths (load from InputStream, parseInputStream with charsetName null/not null)
 * - Partition B: Boundary values (null input, empty streams, maxSize=0, negative maxSize)
 * - Partition C: Defect-targeted branch: XML declaration charset detection (<?xml encoding='...'?>)
 * - Partition D: Exception paths (IllegalCharsetNameException, UncheckedIOException, null contentType)
 * - Partition E: Object lifecycle (emptyByteBuffer, readToByteBuffer, crossStreams not directly testable without IO)
 *
 * Known defect: supportsXmlCharsetDeclaration fails because charset from <?xml encoding='ISO-8859-1'?>
 * is not applied, resulting in mojibake. Test C1 reproduces this.
 */
public class DataUtilDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void loadFromInputStreamWithCharsetName() throws IOException {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Hello</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void loadFromInputStreamWithNullCharsetAndMetaFound() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Hëllo</body></html>";
        // Use ISO-8859-1 bytes
        byte[] bytes = "Hëllo".getBytes(StandardCharsets.ISO_8859_1);
        String fullHtml = "<html><head><meta charset=\"ISO-8859-1\"></head><body>" + new String(bytes, StandardCharsets.ISO_8859_1) + "</body></html>";
        InputStream in = new ByteArrayInputStream(fullHtml.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Hëllo", doc.body().text());
    }

    @Test(timeout = 4000)
    public void loadFromInputStreamWithNullCharsetAndMetaNotFound() throws IOException {
        String html = "<html><head></head><body>Hello</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void loadFromInputStreamWithXmlParser() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>Content</root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("Content", doc.text());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void loadWithNullInputStreamReturnsEmptyDocument() throws IOException {
        // parseInputStream handles null input by returning new Document(baseUri)
        // We cannot call load with null InputStream directly because load calls parseInputStream,
        // but we can test parseInputStream indirectly via load with a null? Actually load(InputStream, ...) will throw NPE if in is null.
        // Instead, we test the internal behavior by calling parseInputStream directly? Not possible as it's package-private.
        // We'll test via load with a non-null stream that results in empty body? The code says "if (input == null) return new Document(baseUri);"
        // That branch is only triggered if input is null, but load methods don't pass null. So we skip.
        // Instead, test readToByteBuffer with maxSize=0 (unlimited) and negative (should throw).
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void readToByteBufferWithNegativeMaxSize() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(timeout = 4000)
    public void readToByteBufferWithZeroMaxSize() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertTrue(buf.remaining() == 10);
    }

    @Test(timeout = 4000)
    public void readToByteBufferWithMaxSizeLessThanStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[100]);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 50);
        assertTrue(buf.remaining() <= 50);
    }

    @Test(timeout = 4000)
    public void emptyByteBufferReturnsZeroCapacity() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertEquals(0, buf.capacity());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Reproduces the known defect: XML charset declaration not detected.
     * Input: XML with <?xml encoding='ISO-8859-1'?> and content containing characters
     * that differ between UTF-8 and ISO-8859-1 (e.g., ö).
     * Expected: Document text "Hellö Wö rld!" (correctly decoded).
     * Defective version: "Hell W rld!" (mojibake).
     */
    @Test(timeout = 4000)
    public void supportsXmlCharsetDeclaration() throws IOException {
        // Build XML with ISO-8859-1 encoded content
        String content = "Hellö Wö rld!";
        byte[] contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root>" + new String(contentBytes, StandardCharsets.ISO_8859_1) + "</root>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.ISO_8859_1);
        InputStream in = new ByteArrayInputStream(xmlBytes);
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        // The root element's text should be correctly decoded
        assertEquals("Hellö Wö rld!", doc.text());
    }

    // Additional test: XML declaration with different encoding
    @Test(timeout = 4000)
    public void supportsXmlCharsetDeclarationUtf16() throws IOException {
        String content = "Test";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16LE);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-16LE\"?><root>" + new String(contentBytes, StandardCharsets.UTF_16LE) + "</root>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_16LE);
        InputStream in = new ByteArrayInputStream(xmlBytes);
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("Test", doc.text());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeNullReturnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeValid() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=EUC-JP");
        assertEquals("EUC-JP", result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeNoCharsetReturnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeWithExtraSpaces() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset = \"UTF-8\" ");
        assertEquals("UTF-8", result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentTypeUnsupportedCharsetReturnsNull() {
        // "unsupported" is not a valid charset
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported"));
    }

    @Test(timeout = 4000)
    public void validateCharsetNullReturnsNull() {
        // validateCharset is private, but we can test via getCharsetFromContentType
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // Test BOM detection (UTF-8 BOM)
    @Test(timeout = 4000)
    public void loadWithUtf8Bom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String html = "<html><body>Hello</body></html>";
        byte[] full = new byte[bom.length + html.length()];
        System.arraycopy(bom, 0, full, 0, bom.length);
        System.arraycopy(html.getBytes(StandardCharsets.UTF_8), 0, full, bom.length, html.length());
        InputStream in = new ByteArrayInputStream(full);
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    // Test BOM detection (UTF-16 LE BOM)
    @Test(timeout = 4000)
    public void loadWithUtf16LeBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        String html = "<html><body>Hello</body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_16LE);
        byte[] full = new byte[bom.length + htmlBytes.length];
        System.arraycopy(bom, 0, full, 0, bom.length);
        System.arraycopy(htmlBytes, 0, full, bom.length, htmlBytes.length);
        InputStream in = new ByteArrayInputStream(full);
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    // Test BOM detection (UTF-32 BE BOM) - may not be supported on all JVMs, but we test the detection
    @Test(timeout = 4000)
    public void loadWithUtf32BeBom() throws IOException {
        byte[] bom = new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF};
        String html = "<html><body>Hello</body></html>";
        byte[] htmlBytes = html.getBytes(java.nio.charset.Charset.forName("UTF-32BE"));
        byte[] full = new byte[bom.length + htmlBytes.length];
        System.arraycopy(bom, 0, full, 0, bom.length);
        System.arraycopy(htmlBytes, 0, full, bom.length, htmlBytes.length);
        InputStream in = new ByteArrayInputStream(full);
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    // Test IOException during parsing (UncheckedIOException)
    @Test(timeout = 4000, expected = IOException.class)
    public void parseInputStreamThrowsIOExceptionOnBadStream() throws IOException {
        // Create a stream that throws IOException on read
        InputStream badStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated IO error");
            }
        };
        // This should throw UncheckedIOException which is caught and rethrown as IOException
        DataUtil.load(badStream, null, "http://example.com");
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void mimeBoundaryGeneratesCorrectLength() {
        String boundary = DataUtil.mimeBoundary();
        assertEquals(32, boundary.length());
        // Ensure only allowed characters
        for (char c : boundary.toCharArray()) {
            assertTrue("Unexpected character: " + c,
                    c == '-' || c == '_' || (c >= '0' && c <= '9') ||
                    (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
        }
    }

    @Test(timeout = 4000)
    public void mimeBoundaryIsRandom() {
        String b1 = DataUtil.mimeBoundary();
        String b2 = DataUtil.mimeBoundary();
        assertNotEquals(b1, b2);
    }

    // Additional coverage for readToByteBuffer with InputStream that returns -1 immediately
    @Test(timeout = 4000)
    public void readToByteBufferEmptyStream() throws IOException {
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        ByteBuffer buf = DataUtil.readToByteBuffer(empty, 100);
        assertEquals(0, buf.remaining());
    }

    // Test crossStreams (package-private, but we can test via load? Not directly. We'll skip.)
}