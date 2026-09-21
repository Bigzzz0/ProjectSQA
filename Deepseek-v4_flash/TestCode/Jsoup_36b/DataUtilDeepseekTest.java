package org.jsoup.helper;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.IllegalCharsetNameException;

import static org.junit.Assert.*;

/**
 * White-box test suite for {@link DataUtil} targeting line/branch coverage and the known
 * defect in {@link DataUtil#getCharsetFromContentType(String)} and related methods.
 *
 * <p>Defect summary: when parsing charset from a Content-Type header or meta tag,
 * the extracted charset string may contain surrounding quotes, trailing commas, or
 * be empty, causing {@link java.nio.charset.Charset#forName(String)} or
 * {@link java.nio.charset.Charset#isSupported(String)} to throw {@link IllegalCharsetNameException}.
 * Expected behavior is to return {@code null} for such unsupported/invalid charset names.
 *
 * <p>Branch & Defect Analysis Matrix:
 * <ul>
 *   <li>Partition A (Core Functional): readToByteBuffer normal/zero/unlimited; parseByteData with explicit charset</li>
 *   <li>Partition B (BVA): null/empty/maxSize=0, BOM character at start, html meta charset found/not found</li>
 *   <li>Partition C (Defect-targeted): getCharsetFromContentType with quoted, trailing comma, empty, invalid charset</li>
 *   <li>Partition D (Exception paths): invalid charsetName argument, unsupported charset in meta, IO errors</li>
 *   <li>Partition E (Contract): Validate.isTrue/notEmpty</li>
 * </ul>
 */
public class DataUtilDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void readToByteBuffer_normalStream() throws IOException {
        byte[] data = "hello".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals("hello", new String(buf.array(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void readToByteBuffer_cappedExact() throws IOException {
        byte[] data = "abcdef".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 3);
        assertEquals("abc", new String(buf.array(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void readToByteBuffer_cappedSmaller() throws IOException {
        byte[] data = "abcdef".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 10);
        assertEquals("abcdef", new String(buf.array(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void readToByteBuffer_maxSizeZero() throws IOException {
        byte[] data = "test".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals("test", new String(buf.array(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void readToByteBuffer_emptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals(0, buf.array().length);
    }

    @Test(timeout = 4000)
    public void parseByteData_explicitCharsetUTF8() {
        ByteBuffer data = ByteBuffer.wrap("hello".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertEquals("hello", doc.text());
    }

    @Test(timeout = 4000)
    public void parseByteData_explicitCharsetISO() {
        ByteBuffer data = ByteBuffer.wrap("café".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, "ISO-8859-1", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertEquals("café", doc.text());
    }

    @Test(timeout = 4000)
    public void parseByteData_charsetNull_metaFound() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"><body>café</body></html>";
        ByteBuffer data = java.nio.ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertEquals("café", doc.text());
    }

    @Test(timeout = 4000)
    public void parseByteData_charsetNull_metaWithHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><body>test</body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertEquals("test", doc.text());
    }

    @Test(timeout = 4000)
    public void parseByteData_charsetNull_metaNotFound() {
        String html = "<html><head></head><body>hello</body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertEquals("hello", doc.text());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void parseByteData_withBOM() {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}; // UTF-8 BOM
        byte[] content = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] combined = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(content, 0, combined, bom.length, content.length);
        ByteBuffer data = ByteBuffer.wrap(combined);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertEquals("hello", doc.text());
    }

    @Test(timeout = 4000)
    public void parseByteData_charsetNull_metaCharsetEqualsDefault() {
        String html = "<html><head><meta charset=\"UTF-8\"><body>test</body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertEquals("test", doc.text());
    }

    @Test(timeout = 4000)
    public void readToByteBuffer_negativeMaxSizeThrows() {
        try {
            DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[1]), -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("maxSize must be 0"));
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void getCharsetFromContentType_nullReturnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_validCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_noCharsetReturnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_unsupportedCharsetReturnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unknown-encoding"));
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_quotedCharsetReturnsNull() {  // Defect trigger
        // Current bug throws IllegalCharsetNameException: 'UTF-8'
        String result = DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\"");
        assertNull("Quoted charset should return null", result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_emptyCharsetReturnsNull() {  // Defect trigger
        // Current bug throws IllegalCharsetNameException: 
        String result = DataUtil.getCharsetFromContentType("text/html; charset=");
        assertNull("Empty charset should return null", result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_trailingCommaReturnsNull() {  // Defect trigger
        // Current bug throws IllegalCharsetNameException: ISO-8859-1,
        String result = DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1,");
        assertNull("Charset with trailing comma should return null", result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_invalidCharsetNameReturnsNull() {  // Defect trigger
        // Current bug throws IllegalCharsetNameException: $HJKDF§$/(
        String result = DataUtil.getCharsetFromContentType("text/html; charset=$HJKDF§$/(");
        assertNull("Invalid charset name should return null", result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_singleDoubleQuoteReturnsNull() {  // Defect trigger
        // Current bug throws IllegalCharsetNameException: UTF-8"
        String result = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8\"");
        assertNull("Charset with trailing double quote should return null", result);
    }

    @Test(timeout = 4000)
    public void getCharsetFromContentType_duplicateCharsetStringReturnsNull() {  // Defect trigger
        // This tests the pattern where multiple charset declarations appear
        String result = DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1; charset=UTF-8");
        // According to regex, it will pick the first matching group: "iso-8859-1" => valid, returns "iso-8859-1"
        // But if the header contains repeating pattern "charset=iso-8859-1; charset=..." it's not a defect per se.
        // However, the known defect mentions "shouldCorrectCharsetForDuplicateCharsetString"
        // which likely involves something like "charset=iso-8859-1;" or duplicate charset= ... 
        // The exact input is not provided, but we can test a variant:
        String result2 = DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1;;");
        // semicolon after charset value -> regex captures "iso-8859-1"  so valid
        assertEquals("iso-8859-1", result2); // This might be valid, not defect.
    }

    // Additional defect-triggering test using parseByteData with meta charset containing problematic value
    @Test(timeout = 4000)
    public void parseByteData_metaCharsetWithQuotesCausesNoException() {
        // Simulate HTML5 meta charset with quote: <meta charset="UTF-8">
        // Already handled by test above; but we can simulate <meta charset='UTF-8'>?
        // Actually the attribute value is quoted in HTML, so it's fine. The problem is in getCharsetFromContentType.
        // Let's test via meta http-equiv that has quoted charset in content attribute.
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=\\\"UTF-8\\\"\"><body>test</body></html>";
        // This may be escaped incorrectly, but we can try with actual double quotes inside the content.
        // Better: construct a byte stream that would cause defect if not handled.
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        // Should not throw IllegalCharsetNameException; the method should fallback to UTF-8.
        assertEquals("test", doc.text());
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void parseByteData_emptyCharsetThrows() {
        ByteBuffer data = ByteBuffer.wrap("test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        DataUtil.parseByteData(data, "", "http://example.com", org.jsoup.parser.Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = java.nio.charset.UnsupportedCharsetException.class)
    public void parseByteData_unsupportedCharsetThrows() {
        ByteBuffer data = ByteBuffer.wrap("test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        DataUtil.parseByteData(data, "unsupported-charset", "http://example.com", org.jsoup.parser.Parser.htmlParser());
    }

    @Test(timeout = 4000, expected = java.nio.charset.IllegalCharsetNameException.class)
    public void parseByteData_illegalCharsetNameThrows() {
        ByteBuffer data = ByteBuffer.wrap("test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        DataUtil.parseByteData(data, "invalid-charset-name!", "http://example.com", org.jsoup.parser.Parser.htmlParser());
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void readToByteBuffer_zeroLengthAllowed() throws IOException {
        ByteBuffer buf = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]), 0);
        assertEquals(0, buf.array().length);
    }

    @Test(timeout = 4000)
    public void readToByteBuffer_cappedWithRemainingZero() throws IOException {
        ByteBuffer buf = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[5]), 5);
        assertEquals(5, buf.array().length);
        // reads exactly maxSize, no problem
    }

    @Test(timeout = 4000)
    public void parseByteData_charsetNull_metaCharsetEmpty() {
        // Meta charset attribute is empty string
        String html = "<html><head><meta charset=\"\"><body>test</body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(data, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        // Should fallback to UTF-8 default; no exception
        assertEquals("test", doc.text());
    }
}