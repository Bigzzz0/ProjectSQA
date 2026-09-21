package org.jsoup.helper;

import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.IllegalCharsetNameException;

import static org.junit.Assert.*;

/**
 * White-box JUnit 4 test suite for DataUtil.
 *
 * <h3>Branch & Defect Analysis Matrix</h3>
 * <ul>
 *   <li><b>Core Logic Branches:</b> parseInputStream handling of null input, BOM detection, charset from meta/xml, 
 *       fullyRead flag, charset validation, stream re-reading.</li>
 *   <li><b>Boundary Conditions:</b> null/empty/max-size InputStream, negative maxSize, buffer sizes (firstReadBufferSize-1,
 *       bufferSize), empty charset string, unsupported charset, BOM offset values (0,3).</li>
 *   <li><b>Defect Zone:</b> InputStream returning 0 bytes on read (infinite loop / UncheckedIOException).</li>
 *   <li><b>Exception Paths:</b> NullPointerException, IllegalArgumentException, UncheckedIOException, IOException.</li>
 *   <li><b>Helper Methods:</b> readToByteBuffer (0/unlimited), readFileToByteBuffer, emptyByteBuffer, getCharsetFromContentType,
 *       mimeBoundary (random but deterministic seed not required).</li>
 * </ul>
 */
public class DataUtilDeepseekTest {

    // --- Helper: create InputStream from string ---
    private static InputStream streamFromString(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    // --- Helper: InputStream that always returns 0 bytes (simulates empty stream defect) ---
    private static InputStream zeroByteStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0; // not -1, not blocking – triggers issue
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return 0;
            }
        };
    }

    // --- Helper: InputStream that throws IOException on any read ---
    private static InputStream failingStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated stream failure");
            }
        };
    }

    // ========================= PART A: Core Functional =========================

    @Test(timeout = 4000)
    public void testLoadFromFileBase() throws IOException {
        File f = File.createTempFile("test", ".html");
        f.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write("<html><head><meta charset=\"utf-8\"></head><body>Hello</body></html>".getBytes());
        }
        Document doc = DataUtil.load(f, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testLoadFromInputStream() throws IOException {
        String html = "<p>test</p>";
        Document doc = DataUtil.load(streamFromString(html), "UTF-8", "http://base");
        assertNotNull(doc);
        assertEquals("test", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testLoadWithXmlParser() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root>data</root>";
        Document doc = DataUtil.load(streamFromString(xml), null, "http://base", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("data", doc.body().text());
    }

    // ========================= PART B: Boundary & Extreme =========================

    @Test(timeout = 4000)
    public void testNullInput() throws IOException {
        Document doc = DataUtil.parseInputStream(null, null, "http://base", Parser.htmlParser());
        assertNotNull(doc);
        // Empty document with base URI
        assertEquals("http://base", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {        InputStream in = streamFromString("hello");
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0); // unlimited
        assertEquals(5, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferMaxed() throws IOException {
        InputStream in = streamFromString("abcdefgh");
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 3);
        assertEquals(3, buf.remaining());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMax() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[1]), -1);
    }

    @Test(timeout = 4000)
    public void testReadFileToByteBuffer() throws IOException {
        File f = File.createTempFile("test", ".txt");
        f.deleteOnExit();        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write("data".getBytes());
        }
        ByteBuffer buf = DataUtil.readFileToByteBuffer(f);
        assertEquals(4, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertNotNull(buf);
        assertEquals(0, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void testCharsetFromContentTypeNoMatch() {        assertEquals(null, DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test(timeout = 4000)
    public void testCharsetFromContentTypeValid() {        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
    }

    @Test(timeout = 4000)
    public void testCharsetFromContentTypeWithQuotesAndSpaces() {
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset = \"iso-8859-1\""));
    }

    @Test(timeout = 4000)
    public void testCharsetFromContentTypeInvalid() {
        // validate returns null for unsupported charset        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unknown"));
    }

    // ========================= PART C: Defect-Targeted Zone =========================

    /**
     * Triggers the known defect: "Underlying input stream returned zero bytes".
     * A stream that returns 0 on read() (not -1) can cause infinite loop / UncheckedIOException.
     */
    @Test(timeout = 4000, expected = UncheckedIOException.class)
 public void testZeroByteStreamDuringParse() throws IOException {
        // This should throw UncheckedIOException because parseInputStream cannot handle zero-byte reads
        DataUtil.parseInputStream(zeroByteStream(), null, "http://base", Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = UncheckedIOException.class)
    public void testFailingInputStream() throws IOException {
        DataUtil.parseInputStream(failingStream(), null, "http://base", Parser.htmlParser());
    }

    // Additional defect: empty charset name should be rejected
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmptyCharsetName() throws IOException {
        DataUtil.load(new ByteArrayInputStream(new byte[0]), "", "http://base");
    }

    // ========================= PART D: Exception & Defensive Guard =========================

    @Test(timeout = 4000)
    public void testCharsetValidationWithIlegalName() {
        // validate will catch IllegalCharsetNameException and return null
        // We can only test via getCharsetFromContentType which calls validate internally
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=\"\\u0000\""));
    }

    @Test(timeout = 4000)
    public void testLoadWithUnsupportedCharset() throws IOException {
        String html = "<html><head><meta charset=\"xyz\"></head></html>";
        // Should still parse with UTF-8 default
        Document doc = DataUtil.load(streamFromString(html), null, "http://base");
        assertNotNull(doc);
    }

    // ========================= PART E: Object Lifecycle & Contract =========================

    // No equals/hashCode/clone/serialization on this utility class.
    // But we test mimeBoundary for basic contract (non-null, correct length)
    @Test(timeout = 4000)
    public void testMimeBoundary() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(32, boundary.length());
        // Ensure only allowed characters
        for (char c : boundary.toCharArray()) {
            assertTrue("Illegal character in boundary", "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) >= 0);
        }
    }

    // ========================= BOM detection tests =========================

    @Test(timeout = 4000)
    public void testUtf8Bom() throws IOException {
        // Stream with BOM + content
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] content = "<html><body>BOM</body></html>".getBytes();
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);
        Document doc = DataUtil.load(new ByteArrayInputStream(all), null, "http://base");
        assertEquals("BOM", doc.body().text());
        // charset should be UTF-8
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testUtf16Bom() throws IOException {
        // UTF-16LE BOM: 0xFF, 0xFE
        byte[] bom = {(byte)0xFF, (byte)0xFE};
        // Encode content in UTF-16LE
        String content = "<html><body>UTF16</body></html>";
        byte[] encoded = content.getBytes("UTF-16LE");
        byte[] all = new byte[bom.length + encoded.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(encoded, 0, all, bom.length, encoded.length);
        Document doc = DataUtil.load(new ByteArrayInputStream(all), null, "http://base");
        assertEquals("UTF16", doc.body().text());
        assertEquals("UTF-16LE", doc.outputSettings().charset().name().toUpperCase());
    }

    @Test(timeout = 4000)
    public void testUtf32Bom() throws IOException {
        // UTF-32BE BOM: 0x00 0x00 0xFE 0xFF
        byte[] bom = {0x00, 0x00, (byte)0xFE, (byte)0xFF};
        String content = "<html><body>UTF32</body></html>";
        byte[] encoded = content.getBytes("UTF-32BE");
        byte[] all = new byte[bom.length + encoded.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(encoded, 0, all, bom.length, encoded.length);
        Document doc = DataUtil.load(new ByteArrayInputStream(all), null, "http://base");
        assertEquals("UTF32", doc.body().text());
        assertEquals("UTF-32BE", doc.outputSettings().charset().name().toUpperCase());
    }

    // ========================= Additional branch coverage =========================

    // When charsetName is provided, Validate.notEmpty is called; test with non-empty valid.
    @Test(timeout = 4000)
    public void testParseWithExplicitCharset() throws IOException {
        String html = "<html><body>Hi</body></html>";
        Document doc = DataUtil.load(streamFromString(html), "ISO-8859-1", "http://base");
        assertEquals("Hi", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // When charsetName is provided but empty triggers exception (already tested)

    // Test the fullyRead == true branch (when stream ends exactly after first read block)
    @Test(timeout = 4000)
    public void testStreamFullyReadAfterFirstChunk() throws IOException {
        // Provide a stream exactly firstReadBufferSize-1 bytes (5KB-1) with meta charset
        int size = 1024 * 5 - 1; // firstReadBufferSize - 1
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"utf-8\"></head><body>");
        for (int i = 0; i < size - sb.length(); i++) {
            sb.append('a');
        }
        sb.append("</body></html>");
        Document doc = DataUtil.load(streamFromString(sb.toString()), null, "http://base");
        assertNotNull(doc);
        // The body should contain the 'a's
        assertTrue(doc.body().text().contains("aaa"));
    }

    // When charset detection finds a different charset and doc is set to null, then re-parsed
    @Test(timeout = 4000)
    public void testMetaCharsetDifferentFromDefault() throws IOException {
        String html = "<html><head><meta charset=\"windows-1252\"></head><body>€</body></html>";
        // Without BOM, should detect windows-1252 and re-parse
        Document doc = DataUtil.load(streamFromString(html), null, "http://base");
        assertNotNull(doc);
        // The euro sign should be correctly parsed
        assertEquals("\u20AC", doc.body().text());
    }

    // When meta provides charset equal to default but not fullyRead, doc set to null then re-parsed with same charset (UTF-8)
    @Test(timeout = 4000)
    public void testMetaCharsetSameAsDefaultButStreamNotFinished() throws IOException {
        // Provide data that is longer than firstReadBufferSize to make sure fullyRead is false
        StringBuilder sb = new StringBuilder("<html><head><meta charset=\"utf-8\"></head><body>");
        for (int i = 0; i < 6000; i++) {
            sb.append('x');
        }
        sb.append("</body></html>");
        Document doc = DataUtil.load(streamFromString(sb.toString()), null, "http://base");
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertTrue(doc.body().text().contains("xxx"));
    }

    // When charsetName is null, no BOM, meta charset found equals default (e.g., utf-8) but stream is fully read -> doc stays non-null
    @Test(timeout = 4000)
    public void testMetaCharsetDefaultAndFullyRead() throws IOException {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>done</body></html>";
        // Small size, stream ends exactly
        Document doc = DataUtil.load(streamFromString(html), null, "http://base");
        assertNotNull(doc);
        assertEquals("done", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Xml declaration encoding detection
    @Test(timeout = 4000)
    public void testXmlDeclarationEncoding() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root>€</root>";
        Document doc = DataUtil.load(streamFromString(xml), null, "http://base", Parser.xmlParser());
        assertEquals("\u20AC", doc.body().text());
        assertEquals("windows-1252", doc.outputSettings().charset().name().toLowerCase());
    }

    // Cross streams test
    @Test(timeout = 4000)
    public void testCrossStreams() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(streamFromString("hello"), out);
        assertEquals("hello", new String(out.toByteArray()));
    }

    // Charset validation with uppercase
    @Test(timeout = 4000)
    public void testValidateCharsetUppercase() {
        // Directly call private method via reflection? Not needed; tested via getCharsetFromContentType
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
    }

    // Charset validation with illegal name returns null
    @Test(timeout = 4000)
    public void testValidateCharsetIllegalName() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=\u0000"));
    }

    // Edge: readToByteBuffer with maxSize=0 (unlimited) and large stream
    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimitedLarge() throws IOException {
        byte[] big = new byte[100000];
        for (int i = 0; i < big.length; i++) big[i] = (byte) i;
        ByteBuffer buf = DataUtil.readToByteBuffer(new ByteArrayInputStream(big), 0);
        assertEquals(big.length, buf.remaining());
    }

    // File read with invalid file (UncheckedIOException will be thrown inside? Actually readFileToByteBuffer throws IOException)
    @Test(timeout = 4000, expected = IOException.class)
    public void testReadFileToByteBufferNonExistent() throws IOException {
        DataUtil.readFileToByteBuffer(new File("/nonexistent"));
    }
}