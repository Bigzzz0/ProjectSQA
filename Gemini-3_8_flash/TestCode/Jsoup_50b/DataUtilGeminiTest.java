package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: org.jsoup.helper.DataUtil
 * Defects4J Ground Truth Target: DataUtilTest::supportsBOMinFiles
 *
 * Branch & Decision Matrix:
 * 1. parseByteData(ByteBuffer, String, String, Parser):
 *    - Branch: charsetName == null vs charsetName != null
 *    - Branch: meta element detection (http-equiv=content-type vs charset vs null)
 *    - Branch: foundCharset != null && !foundCharset.equals(defaultCharset) -> midstream re-decode
 *    - Branch: UNICODE_BOM (0xFEFF) present at index 0 vs absent vs empty string
 *    - Branch: doc == null (re-parse path) vs doc != null (re-use initial parse)
 *    - DEFECT ZONE: When charsetName == null and file contains a UTF-8 BOM together with a meta
 *      charset tag declaring a non-UTF-8 charset (e.g. ISO-8859-1), the initial parse interprets
 *      the BOM as character tokens, forcing <head> closure into <body> mode, or re-decoding as
 *      ISO-8859-1 converts the BOM into non-BOM characters ("ï»¿"), failing BOM suppression and
 *      displacing <head> contents.
 * 2. readToByteBuffer(InputStream, int maxSize):
 *    - Branch: maxSize < 0 (Validate.isTrue guard)
 *    - Branch: maxSize == 0 (unlimited read, capped == false)
 *    - Branch: maxSize > 0 (capped == true, read > remaining vs read <= remaining)
 *    - Branch: buffer boundary (> bufferSize ~130KB)
 * 3. getCharsetFromContentType(String):
 *    - Branch: contentType == null
 *    - Branch: regex matches charset vs no match
 *    - Branch: charset empty string
 *    - Branch: Charset.isSupported(charset) directly vs upper-cased vs unsupported
 *    - Branch: IllegalCharsetNameException handling
 * 4. crossStreams(InputStream, OutputStream):
 *    - Branch: empty stream vs single buffer vs multiple buffers (> bufferSize)
 * 5. readFileToByteBuffer(File):
 *    - Branch: valid file vs non-existent file
 * 6. mimeBoundary():
 *    - Branch: 32 chars length, randomized valid characters from mimeBoundaryChars
 * =========================================================================================
 */
public class DataUtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCrossStreamsNormalAndExceedingBuffer() throws IOException {
        // Exceed internal bufferSize (0x20000 = 131,072 bytes) to force multiple read loops
        int dataSize = 140000;
        byte[] sourceBytes = new byte[dataSize];
        for (int i = 0; i < dataSize; i++) {
            sourceBytes[i] = (byte) (i % 127);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(sourceBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        byte[] resultBytes = out.toByteArray();

        assertEquals(dataSize, resultBytes.length);
        assertArrayEquals(sourceBytes, resultBytes);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] payload = "Testing unlimited byte buffer reading.".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(payload);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in);

        assertNotNull(buffer);
        assertEquals(payload.length, buffer.remaining());
        byte[] readBack = new byte[buffer.remaining()];
        buffer.get(readBack);
        assertArrayEquals(payload, readBack);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferCappedExactAndExceeded() throws IOException {
        byte[] payload = "1234567890ABCDEF".getBytes(StandardCharsets.UTF_8);

        // Case 1: MaxSize exactly payload size
        ByteBuffer b1 = DataUtil.readToByteBuffer(new ByteArrayInputStream(payload), payload.length);
        assertEquals(payload.length, b1.remaining());

        // Case 2: MaxSize less than payload size (triggers read > remaining)
        int cap = 5;
        ByteBuffer b2 = DataUtil.readToByteBuffer(new ByteArrayInputStream(payload), cap);
        assertEquals(cap, b2.remaining());
        byte[] cappedBytes = new byte[cap];
        b2.get(cappedBytes);
        assertArrayEquals("12345".getBytes(StandardCharsets.UTF_8), cappedBytes);
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamWithExplicitCharsetAndParser() throws IOException {
        String xml = "<root><child id='1'>Text</child></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com/", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("Text", doc.select("child").text());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(StandardCharsets.UTF_8, doc.outputSettings().charset());
    }

    @Test(timeout = 4000)
    public void testLoadInputStreamDefaultParser() throws IOException {
        String html = "<html><head><title>Jsoup Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com/");

        assertNotNull(doc);
        assertEquals("Jsoup Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testLoadFile() throws IOException {
        File tempFile = File.createTempFile("datautil_test", ".html");
        tempFile.deleteOnExit();

        try {
            String content = "<html><head><title>File Loading</title></head><body>Content</body></html>";
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }

            Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com/file");

            assertNotNull(doc);
            assertEquals("File Loading", doc.title());
            assertEquals("http://example.com/file", doc.baseUri());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testMimeBoundaryContract() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());

        // Characters must belong to mimeBoundaryChars
        String allowedChars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < boundary.length(); i++) {
            assertTrue(allowedChars.indexOf(boundary.charAt(i)) != -1);
        }

        // Must be non-static/randomized across distinct invocations
        String secondBoundary = DataUtil.mimeBoundary();
        assertNotEquals(boundary, secondBoundary);
    }

    @Test(timeout = 4000)
    public void testEmptyByteBuffer() {
        ByteBuffer empty = DataUtil.emptyByteBuffer();
        assertNotNull(empty);
        assertEquals(0, empty.capacity());
        assertEquals(0, empty.remaining());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStreamsAndFiles() throws IOException {
        // Empty InputStream crossStreams
        ByteArrayInputStream emptyIn = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(emptyIn, out);
        assertEquals(0, out.size());

        // Empty InputStream readToByteBuffer
        ByteBuffer buf = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, buf.remaining());

        // Empty File readFileToByteBuffer
        File emptyFile = File.createTempFile("empty", ".tmp");
        emptyFile.deleteOnExit();
        try {
            ByteBuffer fileBuf = DataUtil.readFileToByteBuffer(emptyFile);
            assertEquals(0, fileBuf.remaining());
        } finally {
            emptyFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testParseByteDataZeroLength() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        Document doc = DataUtil.parseByteData(emptyBuffer, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals(0, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeBoundaryVariants() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=   "));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz_1234"));

        // Case variations and quoting
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("iso-8859-1", DataUtil.getCharsetFromContentType("text/html; charset='iso-8859-1'"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=  UTF-8 ; other=val"));
    }

    @Test(timeout = 4000)
    public void testMetaCharsetDetectionVariants() {
        // HTML5 <meta charset="...">
        String html5 = "<html><head><meta charset=\"iso-8859-1\"><title>H5</title></head></html>";
        ByteBuffer buf5 = ByteBuffer.wrap(html5.getBytes(StandardCharsets.ISO_8859_1));
        Document doc5 = DataUtil.parseByteData(buf5, null, "http://example.com", Parser.htmlParser());
        assertEquals("iso-8859-1", doc5.outputSettings().charset().name().toLowerCase());

        // Malformed meta charset throwing IllegalCharsetNameException
        String malformedMeta = "<html><head><meta charset=\"?invalid?\"><title>Bad</title></head></html>";
        ByteBuffer bufBad = ByteBuffer.wrap(malformedMeta.getBytes(StandardCharsets.UTF_8));
        Document docBad = DataUtil.parseByteData(bufBad, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", docBad.outputSettings().charset().name());

        // http-equiv content type matches default UTF-8 (no re-decode path)
        String utf8Meta = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>U</title></head></html>";
        ByteBuffer bufUtf8 = ByteBuffer.wrap(utf8Meta.getBytes(StandardCharsets.UTF_8));
        Document docUtf8 = DataUtil.parseByteData(bufUtf8, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", docUtf8.outputSettings().charset().name());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J: supportsBOMinFiles)
    // =========================================================================

    /**
     * Target Defect: org.jsoup.helper.DataUtilTest::supportsBOMinFiles
     *
     * In defective versions of DataUtil:
     * When parsing byte data where charsetName is null, parseByteData() attempts an initial UTF-8
     * parse BEFORE stripping the Unicode BOM (0xFEFF). Because the HTML parser treats a leading
     * BOM character as an initial text token, the parser prematurely transitions out of Initial/BeforeHead
     * and forces the creation of <body>, ejecting all subsequent <head> elements (such as <meta> and <title>).
     *
     * Furthermore, if a meta charset (e.g. iso-8859-1) is present, re-decoding midstream without BOM
     * stripping corrupts the leading bytes into "ï»¿", causing the subsequent UNICODE_BOM check to fail
     * entirely and leaving the title outside <head>.
     *
     * This test explicitly asserts the ground truth: the <title> MUST be correctly found in doc.head().
     */
    @Test(timeout = 4000)
    public void testSupportsBOMinFilesDefectTargetClasspathResources() throws Exception {
        // If the Defects4J resource files exist on classpath, execute the exact ground truth assertion
        URL bomCharsetUrl = DataUtilGeminiTest.class.getResource("/bom-charset.html");
        if (bomCharsetUrl != null) {
            File in = new File(bomCharsetUrl.toURI());

            // 1. Explicit UTF-8
            Document doc1 = DataUtil.load(in, "UTF-8", "http://example.com");
            assertEquals("OK", doc1.head().select("title").text());

            // 2. Detected from content
            Document doc2 = DataUtil.load(in, null, "http://example.com");
            assertEquals("OK", doc2.head().select("title").text());
        }

        URL bomNoneUrl = DataUtilGeminiTest.class.getResource("/bom-none.html");
        if (bomNoneUrl != null) {
            File in = new File(bomNoneUrl.toURI());

            // 1. Explicit UTF-8
            Document doc1 = DataUtil.load(in, "UTF-8", "http://example.com");
            assertEquals("OK", doc1.head().select("title").text());

            // 2. Detected from content
            Document doc2 = DataUtil.load(in, null, "http://example.com");
            assertEquals("OK", doc2.head().select("title").text());
        }
    }

    @Test(timeout = 4000)
    public void testSupportsBOMInMemorySyntheticStream() throws IOException {
        // Synthetic recreation of bom-charset.html: UTF-8 BOM + meta http-equiv declaring iso-8859-1 + title in head
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String htmlContent = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" /><title>OK</title></head><body>One</body></html>";
        byte[] htmlBytes = htmlContent.getBytes(StandardCharsets.ISO_8859_1);

        byte[] combined = new byte[bom.length + htmlBytes.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(htmlBytes, 0, combined, bom.length, htmlBytes.length);

        // Targeted test: Passing charsetName = null triggers the defect in DataUtil.parseByteData
        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");

        Element head = doc.head();
        assertNotNull("Head must not be null even when BOM is present", head);
        assertEquals("Title must be parsed inside <head>, not ejected to <body> due to unstripped BOM",
                "OK", head.select("title").text());
    }

    @Test(timeout = 4000)
    public void testSupportsBOMWithoutMetaTag() throws IOException {
        // Synthetic recreation of bom-none.html: UTF-8 BOM + no meta tag + title in head
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String htmlContent = "<html><head><title>OK</title></head><body>One</body></html>";
        byte[] htmlBytes = htmlContent.getBytes(StandardCharsets.UTF_8);

        byte[] combined = new byte[bom.length + htmlBytes.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(htmlBytes, 0, combined, bom.length, htmlBytes.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");

        assertEquals("OK", doc.head().select("title").text());
        assertEquals("One", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testBOMWithExplicitSpecifiedCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String htmlContent = "<html><head><title>Explicit</title></head><body>Content</body></html>";
        byte[] htmlBytes = htmlContent.getBytes(StandardCharsets.UTF_8);

        byte[] combined = new byte[bom.length + htmlBytes.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(htmlBytes, 0, combined, bom.length, htmlBytes.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");

        assertEquals("Explicit", doc.head().select("title").text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadToByteBufferNegativeMaxSizeThrows() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseByteDataEmptyCharsetThrows() {
        ByteBuffer buf = ByteBuffer.wrap("<div>Test</div>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buf, "", "http://example.com", Parser.htmlParser());
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testReadFileToByteBufferNonExistentFileThrows() throws IOException {
        File missingFile = new File("/non_existent_path_jsoup_test_" + System.nanoTime() + ".html");
        DataUtil.readFileToByteBuffer(missingFile);
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeIllegalCharsetName() {
        // Ensure regex match with illegal charset token safely returns null via catch block
        String invalidHeader = "text/html; charset=invalid[charset]";
        String result = DataUtil.getCharsetFromContentType(invalidHeader);
        assertNull(result);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorViaReflection() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        assertTrue("Constructor must be private", java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            DataUtil instance = constructor.newInstance();
            assertNotNull(instance);
        } catch (InvocationTargetException e) {
            fail("Private constructor should instantiate without exception");
        }
    }
}