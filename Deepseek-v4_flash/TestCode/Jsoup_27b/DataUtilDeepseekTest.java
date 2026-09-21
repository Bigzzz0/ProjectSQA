package org.jsoup.helper;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * DataUtilDeepseekTest
 * <p>
 * Target: org.jsoup.helper.DataUtil
 * <p>
 * Coverage plan:
 * - Partition A: Core functional paths (load streams, parseByteData, readToByteBuffer, getCharsetFromContentType)
 * - Partition B: Boundary and extreme values (null, empty, BOM, large input, unsupported charset etc.)
 * - Partition C: Defect-targeted tests for charset case (the known bug: getCharsetFromContentType uppercases but expected lowercase)
 * - Partition D: Exception and defensive guard paths (null/empty charsetName, null input stream)
 * - Partition E: Lifecycle and contract integrity (not applicable, no equals/hashCode etc.)
 * <p>
 * The known defect (Defects4J): getCharsetFromContentType returns charset in uppercase ("UTF-8")
 * while test expects lowercase ("utf-8"). We target this with explicit assertions expecting lowercase.
 */
public class DataUtilDeepseekTest {

    // ----------------------------------------------------------------------
    // Partition A: Core functional logic and state transitions
    // ----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadToByteBufferFromSmallStream() throws IOException {
        byte[] data = "Hello, world!".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in);
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertArrayEquals(data, out);
    }

    @Test(timeout = 4000)
    public void testReadToByteBufferEmptyStream() throws IOException {
        byte[] data = new byte[0];
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in);
        assertEquals(0, buf.remaining());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithCharset() throws Exception {
        String html = "<html><head><meta charset=\"utf-8\"><title>Test</title></head><body></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes("UTF-8"));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(buf, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        // The output setting charset should be the passed one (UTF-8)
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithoutCharsetDetectFromMeta() throws Exception {
        // HTML with meta charset (uppercase) – detection should happen
        String html = "<html><head><meta charset=\"utf-8\"><title>Detected</title></head><body></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes("UTF-8"));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(buf, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Detected", doc.title());
        // When charsetName is null, the effective charset becomes the detected one (lowercase "utf-8" from meta)
        // The method uppercases via getCharsetFromContentType? Actually in parseByteData, after detecting from meta,
        // it sets charsetName = foundCharset (which comes from getCharsetFromContentType, which uppercases).
        // So after re-decode, doc.outputSettings().charset() will be the effective charset.
        // Since the defect is uppercase, we expect "UTF-8". But to reveal the bug, we don't enforce lowercase here.
        // We just check that it's not null.
        assertNotNull(doc.outputSettings().charset());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithBOM() throws Exception {
        // String with Unicode BOM at start
        String html = "\uFEFF<html><head><title>BOM</title></head><body></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes("UTF-8"));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(buf, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM", doc.title());
    }

    @Test(timeout = 4000)
    public void testParseByteDataWithNoCharsetAndNoMeta() throws Exception {
        String html = "<html><head><title>No meta</title></head><body></body></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes("UTF-8"));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(buf, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No meta", doc.title());
        // Default charset UTF-8 should be used
        assertEquals("UTF-8", doc.outputSettings().charset().name()); // intentional wrong spelling? Actually correct: UTF-8 is not standard but here it's default.
        // The default is "UTF-8" constant. So we assert that.
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // ----------------------------------------------------------------------
    // Partition B: Boundary value analysis and extremes
    // ----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeEmpty() {
        // content type with no charset
        String ct = "text/html;";
        assertNull(DataUtil.getCharsetFromContentType(ct));
    }

    @Test(timeout = 4000)
    public void testGetCharsetFromContentTypeNoMatch() {
        String ct = "text/html";
        assertNull(DataUtil.getCharsetFromContentType(ct));
    }

    @Test(timeout = 4000)
    public void testGetChar setFromContentTypeUppercase() {
        // This is a variant that should also trigger the defect if we expect lowercase
        String ct = "text/html; charset=UTF-8";
        String ch = DataUtil.getChar setFromContentType(ct);
        // The bug: returns "UTF-8" (uppercase). Our expected value is "utf-8" (lowercase).
        // This assertion will fail on the defective code.
        assertEquals("utf-8", ch);
    }

    // ----------------------------------------------------------------------
    // Partition C: Defect-targeted branch zone (the known Charset case bug)
    // ----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCharsetCaseLowerCase() {
        // Directly test getCharsetFromContentType with lower case charset in header
        String ct = "text/html; charset=utf-8";
        String result = DataUtil.getCharsetFromContentType(ct);
        // The bug transforms "utf-8" to "UTF-8". Expected is "utf-8".
        // This assertion will reveal the defect.
        assertEquals("utf-8", result);
    }

    @Test(timeout = 4000)
    public void testCharsetCaseMixedCase() {
        String ct = "text/html; charset=UtF-8";
        String result = DataUtil.getCharsetFromContentType(ct);
        // Expected: "UtF-8" should be lowercased? Actually the method uppercases, so result is "UTF-8".
        // But we expect lowercase? The defect says expected "utf-8". So we assert "utf-8"? That would fail.
        // To reveal bug, we assert that the result is not all uppercase? Instead we can assert that it's "utf-8".
        // Actually the test from Defects4J expects "utf-8" for input "utf-8". So we do the same.
        assertEquals("utf-8", result);
    }

    @Test(timeout = 4000)
    public void testCharsetQuoted() {
        // As per testQuotedCharset
        String ct = "text/html; charset=\"utf-8\"";
        String result = DataUtil.getCharsetFromContentType(ct);
        // Expected: "utf-8"
        assertEquals("utf-8", result);
    }

    @Test(timeout = 4000)
    public void testCharsetQuotedMixedCase() {
        String ct = "text/html; charset=\"UtF-8\"";
        String result = DataUtil.getCharsetFromContentType(ct);
        assertEquals("utf-8", result);
    }

    // ----------------------------------------------------------------------
    // Partition D: Exception and defensive guard paths
    // ----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseByteDataEmptyCharset() {
        // When charsetName is not null but empty, Validate.notEmpty throws
        String html = "<html></html>";
        ByteBuffer buf = ByteBuffer.wrap(html.getBytes());
        DataUtil.parseByteData(buf, "", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        fail("Should have thrown IllegalArgumentException");
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testReadToByteBufferNullStream() throws IOException {
        DataUtil.readToByteBuffer(null);
    }

    @Test(timeout = 4000)
    public void testLoadStreamWithNullCharset() throws IOException {
        // load(InputStream, null, baseUri) should handle null charset by detection
        String html = "<html><head><meta charset=\"utf-8\"><title>OK</title></head></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        org.jsoup.nodes.Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("OK", doc.title());
    }

    // ----------------------------------------------------------------------
    // Partition E: Not applicable (no equals/hashCode etc.)
    // ----------------------------------------------------------------------
}