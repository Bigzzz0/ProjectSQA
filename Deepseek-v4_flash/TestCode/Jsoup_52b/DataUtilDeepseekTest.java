package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import static org.junit.Assert.*;

/**
 * White-box test suite for DataUtil.
 * Targets line/branch coverage and the known defect related to XML charset declaration handling.
 *
 * Branch & Defect Analysis Matrix:
 * - parseByteData: BOM detection, charset from meta, charset from XML declaration, fallback to default.
 * - getCharsetFromContentType: null, pattern match, charset validation, quotes.
 * - validateCharset: null, empty, valid, invalid, uppercase, quotes removal.
 * - detectCharsetFromBom: UTF-32 BE/LE, UTF-16 BE/LE, UTF-8, no BOM.
 * - readToByteBuffer: maxSize=0 (unlimited), maxSize>0, maxSize<0 (exception), exact read, partial read.
 * - crossStreams: normal copy, empty input.
 * - mimeBoundary: length, character set.
 * - Known defect: XML declaration encoding attribute may be incorrectly formatted or charset not applied.
 */
public class DataUtilDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testParseByteDataWithNullCharsetAndMetaCharset() {
        // Simulate HTML with meta charset
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body></body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        // Should detect ISO-8859-1 from meta and re-decode
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithNullCharsetAndXmlDeclaration() {
        // XML with encoding declaration
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root></root>";
        ByteBuffer data = ByteBuffer.wrap(xml.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.xmlParser());
        // Should detect ISO-8859-1 from XML declaration
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        // Also verify the XML declaration node is preserved correctly
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertTrue(decl.name().equals("xml"));
        assertEquals("ISO-8859-1", decl.attr("encoding"));
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithExplicitCharset() {
        String html = "<html><body>Hello</body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithBomUtf8() {
        // UTF-8 BOM: EF BB BF
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String content = "<html><body>Hello</body></html>";
        ByteBuffer data = ByteBuffer.allocate(bom.length + content.length());
        data.put(bom);
        data.put(content.getBytes(Charset.forName("UTF-8")));
        data.flip();
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithBomUtf16BE() {
        // UTF-16 BE BOM: FE FF
        byte[] bom = {(byte)0xFE, (byte)0xFF};
        String content = "<html><body>Hello</body></html>";
        ByteBuffer data = ByteBuffer.allocate(bom.length + content.length() * 2);
        data.put(bom);
        // Encode content as UTF-16BE
        byte[] encoded = content.getBytes(Charset.forName("UTF-16BE"));
        data.put(encoded);
        data.flip();
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithBomUtf32LE() {
        // UTF-32 LE BOM: FF FE 00 00
        byte[] bom = {(byte)0xFF, (byte)0xFE, 0x00, 0x00};
        String content = "<html><body>Hello</body></html>";
        ByteBuffer data = ByteBuffer.allocate(bom.length + content.length() * 4);
        data.put(bom);
        byte[] encoded = content.getBytes(Charset.forName("UTF-32LE"));
        data.put(encoded);
        data.flip();
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeNoMatch() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeValid() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeWithQuotes() {
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeInvalidCharset() {
        // Invalid charset should return null
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=invalid-charset"));
    }

    @Test(timeout = 4000)
    public void testValidateCharsetNull() {
        assertNull(DataUtil.validateCharset(null));
    }

    @Test(timeout = 4000)
    public void testValidateCharsetEmpty() {
        assertNull(DataUtil.validateCharset(""));
    }

    @Test(timeout = 4000)
    public void testValidateCharsetValid() {
        assertEquals("UTF-8", DataUtil.validateCharset("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testValidateCharsetValidUppercase() {
        assertEquals("UTF-8", DataUtil.validateCharset("utf-8"));
    }

    @Test(timeout = 4000)
    public void testValidateCharsetWithQuotes() {
        assertEquals("UTF-8", DataUtil.validateCharset("\"UTF-8\""));
    }

    @Test(timeout = 4000)
    public void testValidateCharsetInvalid() {
        assertNull(DataUtil.validateCharset("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] data = "Hello World".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in);
        assertEquals(data.length, buf.remaining());
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertArrayEquals(data, out);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCappedExact() throws IOException {
        byte[] data = "Hello World".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, data.length);
        assertEquals(data.length, buf.remaining());
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertArrayEquals(data, out);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCappedPartial() throws IOException {
        byte[] data = "Hello World".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, buf.remaining());
        byte[] out = new byte[5];
        buf.get(out);
        assertArrayEquals("Hello".getBytes(), out);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]), -1);
    }

    @Test(timeout = 4000)
    public void testCrossStreams() throws IOException {
        byte[] data = "Test data".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testCrossStreamsEmpty() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        assertEquals(0, out.size());
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryLength() {
        String boundary = DataUtil.mimeBoundary();
        assertEquals(DataUtil.boundaryLength, boundary.length());
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryCharacters() {
        String boundary = DataUtil.mimeBoundary();
        for (char c : boundary.toCharArray()) {
            assertTrue("Character not in allowed set: " + c,
                    "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) >= 0);
        }
    }

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertEquals(0, buf.remaining());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // The known defect involves incorrect handling of XML declaration encoding attribute.
    // The following test directly targets the scenario where an XML document with a charset declaration
    // is parsed and the resulting document's charset should be set correctly.

    @Test(timeout = 4000)
    public void testParseByteDataWithXmlDeclarationCharsetAndVerifyOutput() {
        // This test reproduces the defect: when parsing an XML document with encoding="ISO-8859-1",
        // the output settings charset should be ISO-8859-1, and the XML declaration should be preserved.
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root>Test</root>";
        ByteBuffer data = ByteBuffer.wrap(xml.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.xmlParser());
        // The defect caused the charset to remain UTF-8 or the encoding attribute to be mangled.
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        // Also verify the XML declaration node's encoding attribute is exactly as input
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("xml", decl.name());
        assertEquals("ISO-8859-1", decl.attr("encoding"));
        // Ensure no extra quotes or spaces
        String declOut = decl.outerHtml();
        assertTrue("Declaration should contain encoding=\"ISO-8859-1\"", declOut.contains("encoding=\"ISO-8859-1\""));
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithXmlDeclarationUtf8() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>";
        ByteBuffer data = ByteBuffer.wrap(xml.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.xmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("UTF-8", decl.attr("encoding"));
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithXmlDeclarationNoCharset() {
        // XML without encoding declaration should fall back to UTF-8
        String xml = "<?xml version=\"1.0\"?><root></root>";
        ByteBuffer data = ByteBuffer.wrap(xml.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.xmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseByteDataWithEmptyCharsetName() {
        // When charsetName is not null but empty, should throw
        ByteBuffer data = ByteBuffer.wrap("test".getBytes());
        DataUtil.parseByteData(data, "", "http://example.com", Parser.htmlParser());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithIllegalCharsetNameInMeta() {
        // If meta charset is illegal, should fall back to default
        String html = "<html><head><meta charset=\"illegal-name\"></head><body></body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeWithIllegalCharsetName() {
        // Should return null for unsupported charset
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported"));
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    // DataUtil is a utility class with private constructor; no instances.
    // Test that constructor is private (via reflection) – optional but good for coverage.
    @Test(timeout = 4000)
    public void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<DataUtil> c = DataUtil.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(c.getModifiers()));
        c.setAccessible(true);
        c.newInstance(); // should succeed
    }

    // Additional coverage for readFileToByteBuffer (requires a temp file)
    @Test(timeout = 4000)
    public void testReadFileToByteBuffer() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("File content".getBytes());
        }
        ByteBuffer buf = DataUtil.readFileToByteBuffer(tempFile);
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        assertEquals("File content", new String(bytes));
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadFileToByteBufferNonExistent() throws IOException {
        DataUtil.readFileToByteBuffer(new File("/nonexistent/file"));
    }
}