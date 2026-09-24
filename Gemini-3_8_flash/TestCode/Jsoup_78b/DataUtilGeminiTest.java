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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.helper.DataUtil
 * Core Responsibilities: Stream decoding, BOM detection, charset resolution, byte buffer mapping, mime generation.
 *
 * Branch & Defect Coverage:
 * 1. DEFECT REPRODUCTION (ConnectTest::handlesEmptyStreamDuringParseRead):
 *    - Empty input stream with explicit charset ("UTF-8") causes reader creation.
 *    - In defective versions, ConstrainableInputStream / StreamDecoder throws:
 *      "Underlying input stream returned zero bytes" (UncheckedIOException).
 *    - Must assert proper Document creation from empty stream without crashing.
 *
 * 2. BOM Detection (detectCharsetFromBom):
 *    - UTF-32 Big-Endian (00 00 FE FF) -> BomCharset("UTF-32", 0)
 *    - UTF-32 Little-Endian (FF FE 00 00) -> BomCharset("UTF-32", 0)
 *    - UTF-16 Big-Endian (FE FF) -> BomCharset("UTF-16", 0)
 *    - UTF-16 Little-Endian (FF FE) -> BomCharset("UTF-16", 0)
 *    - UTF-8 (EF BB BF) -> BomCharset("UTF-8", 3), offset skipped
 *    - Under-sized byte buffers (< 4 bytes) and non-matching buffers -> null
 *
 * 3. Charset Extraction & Validation:
 *    - Charset from <meta http-equiv="content-type" content="text/html; charset=gb2312">
 *    - Charset from HTML5 <meta charset="shift_jis">
 *    - Charset from XML declaration <?xml version="1.0" encoding="ISO-8859-1"?>
 *    - Charset re-decode branch when foundCharset != UTF-8 (doc = null, reread)
 *    - Charset validation with quotes, whitespace, unsupported names, IllegalCharsetNameException
 *
 * 4. Stream & File Loading:
 *    - load(File, ...), load(InputStream, ...), load(InputStream, ..., Parser)
 *    - readToByteBuffer(InputStream, maxSize) bounds: maxSize == 0 (unlimited), maxSize > 0, maxSize < 0 (exception)
 *    - readFileToByteBuffer(File), emptyByteBuffer()
 *    - crossStreams(InputStream, OutputStream) buffer looping
 *    - mimeBoundary() generation contract (length == 32, characters valid)
 * ---------------------------------------------------------------------------------------------------------
 */
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (ConnectTest::handlesEmptyStreamDuringParseRead)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlesEmptyStreamDuringParseReadWithExplicitCharset() throws IOException {
        // Targets defect where an empty stream with an explicit charset triggers
        // "java.io.IOException: Underlying input stream returned zero bytes"
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        Document doc = DataUtil.load(emptyStream, "UTF-8", "https://example.com");

        assertNotNull("Document should not be null for empty input stream", doc);
        assertEquals("https://example.com", doc.baseUri());
        assertEquals(0, doc.body().children().size());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testHandlesCustomEmptyStreamWithExplicitCharset() throws IOException {
        // Verifies defensive stream decoding when read() yields immediate EOF (-1)
        InputStream emptyCustomStream = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                return -1;
            }
        };

        Document doc = DataUtil.parseInputStream(emptyCustomStream, "ISO-8859-1", "https://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseInputStreamNullInputReturnsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "https://example.com/base", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("https://example.com/base", doc.baseUri());
        assertEquals(0, doc.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testLoadHtmlWithMetaCharsetRedecode() throws IOException {
        // Document has meta charset "ISO-8859-1" distinct from default UTF-8, triggering re-decode branch
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>H\u00e9llo</p></body></html>";
        byte[] encodedBytes = html.getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayInputStream in = new ByteArrayInputStream(encodedBytes);

        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("H\u00e9llo", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testLoadHtmlWithMetaHttpEquivRedecode() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testLoadXmlWithDeclarationEncodingRedecode() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><value>Data</value></root>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.load(in, null, "https://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Data", doc.select("value").text());
    }

    @Test(timeout = 4000)
    public void testLoadLargeDocumentExceedingFirstReadBuffer() throws IOException {
        // Exceeds firstReadBufferSize (5120 bytes) to force !fullyRead branch
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        for (int i = 0; i < 6000; i++) {
            sb.append("A");
        }
        sb.append("</body></html>");

        ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, null, "https://example.com");

        assertNotNull(doc);
        assertEquals(6000, doc.body().text().length());
    }

    @Test(timeout = 4000)
    public void testCrossStreamsPipesAllBytes() throws IOException {
        byte[] source = "Sample streaming payload for crossStreams".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(source);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertArrayEquals(source, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryContract() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(32, boundary.length());
        assertTrue("Boundary should match expected charset regex", boundary.matches("^[-_1-9a-zA-Z0-9]{32}$"));
    }

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertNotNull(buf);
        assertEquals(0, buf.capacity());
        assertEquals(0, buf.remaining());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & BOM Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testDetectBomUtf8() throws IOException {
        byte[] utf8Bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'p', '>', 'B', 'O', 'M', '<', '/', 'p', '>'};
        ByteArrayInputStream in = new ByteArrayInputStream(utf8Bom);

        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("BOM", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testDetectBomUtf16BE() throws IOException {
        byte[] utf16BeBom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] payload = "<p>BE</p>".getBytes(StandardCharsets.UTF_16BE);
        byte[] combined = new byte[utf16BeBom.length + payload.length];
        System.arraycopy(utf16BeBom, 0, combined, 0, utf16BeBom.length);
        System.arraycopy(payload, 0, combined, utf16BeBom.length, payload.length);

        ByteArrayInputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectBomUtf16LE() throws IOException {
        byte[] utf16LeBom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] payload = "<p>LE</p>".getBytes(StandardCharsets.UTF_16LE);
        byte[] combined = new byte[utf16LeBom.length + payload.length];
        System.arraycopy(utf16LeBom, 0, combined, 0, utf16LeBom.length);
        System.arraycopy(payload, 0, combined, utf16LeBom.length, payload.length);

        ByteArrayInputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectBomUtf32BE() throws IOException {
        byte[] bom = new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF};
        ByteArrayInputStream in = new ByteArrayInputStream(bom);

        // UTF-32 detection
        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectBomUtf32LE() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x00};
        ByteArrayInputStream in = new ByteArrayInputStream(bom);

        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testDetectBomWithInsufficientBytes() throws IOException {
        // Less than 4 bytes, no BOM match
        byte[] partial = new byte[]{0x00, 0x01};
        ByteArrayInputStream in = new ByteArrayInputStream(partial);

        Document doc = DataUtil.load(in, null, "https://example.com");
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeVariants() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=invalid_charset_$$$"));

        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='utf-8'"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1; boundary=something"));
        assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/plain; charset=us-ascii"));
    }

    // =========================================================================
    // Partition D: File Handling, Buffers, Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadFileToByteBufferAndLoadFile() throws IOException {
        File tempFile = File.createTempFile("jsoup_data_util_test", ".html");
        tempFile.deleteOnExit();

        String content = "<html><head><title>File Test</title></head><body><p>Hello File</p></body></html>";
        try (OutputStream os = new FileOutputStream(tempFile)) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }

        ByteBuffer buf = DataUtil.readFileToByteBuffer(tempFile);
        assertNotNull(buf);
        assertEquals(content.getBytes(StandardCharsets.UTF_8).length, buf.remaining());

        Document doc = DataUtil.load(tempFile, "UTF-8", "https://example.com");
        assertNotNull(doc);
        assertEquals("File Test", doc.title());
        assertEquals("Hello File", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] data = "Hello Buffer Unlimited".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertEquals(data.length, buffer.remaining());
        assertArrayEquals(data, buffer.array());
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferWithPositiveLimit() throws IOException {
        byte[] data = "1234567890".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, buffer.remaining());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadToByteBufferNegativeMaxSizeThrowsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInputStreamEmptyCharsetArgThrowsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("<html></html>".getBytes(StandardCharsets.UTF_8));
        DataUtil.load(in, "", "https://example.com");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Constructor Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorReflection() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }
}