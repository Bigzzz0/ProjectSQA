package org.jsoup;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: Jsoup (public static methods)
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - parse(String, String) -> delegates to Parser.parse
 *   - parse(String, String, Parser) -> delegates to parser.parseInput
 *   - parse(String) -> delegates to Parser.parse with empty baseUri
 *   - connect(String) -> delegates to HttpConnection.connect
 *   - parse(File, String, String) -> delegates to DataUtil.load
 *   - parse(File, String) -> delegates to DataUtil.load with file path as baseUri
 *   - parse(InputStream, String, String) -> delegates to DataUtil.load
 *   - parse(InputStream, String, String, Parser) -> delegates to DataUtil.load with parser
 *   - parseBodyFragment(String, String) -> delegates to Parser.parseBodyFragment
 *   - parseBodyFragment(String) -> delegates to Parser.parseBodyFragment with empty baseUri
 *   - parse(URL, int) -> creates Connection, sets timeout, calls get()
 *   - clean(String, String, Whitelist) -> parseBodyFragment, Cleaner.clean, return body html
 *   - clean(String, Whitelist) -> calls clean with empty baseUri
 *   - clean(String, String, Whitelist, Document.OutputSettings) -> parseBodyFragment, Cleaner.clean, set output settings, return body html
 *   - isValid(String, Whitelist) -> parseBodyFragment, Cleaner.isValid
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments for html, baseUri, parser, whitelist, file, inputStream, url, timeout
 *   - empty strings for html, baseUri, charsetName
 *   - negative/zero timeout values
 *   - invalid URLs for connect and parse(URL)
 *   - non-HTTP/HTTPS URLs
 *   - null whitelist in clean/isValid
 *   - null output settings in clean
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: CleanerTest.testIsValidBodyHtml and testIsValidDocument fail.
 *     Likely issue in isValid() or clean() when whitelist is used with body fragments.
 *     We target:
 *       * isValid() with a whitelist that should allow certain tags but bug causes false negative/positive.
 *       * clean() with output settings that might affect escaping or structure.
 *       * parseBodyFragment with malformed HTML.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - IOException from file/stream parsing (simulate with non-existent file)
 *   - MalformedURLException from parse(URL) with invalid URL
 *   - NullPointerException from null arguments (some methods may throw, some may not)
 *   - IllegalArgumentException from invalid charsetName? (DataUtil.load may throw)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Not applicable (Jsoup is static utility class, no instance state)
 * 
 * Coverage goals: 100% line and branch coverage of Jsoup class.
 * All public methods must be invoked with representative inputs.
 * 
 * Defect-specific test: testIsValidWithWhitelist() and testCleanWithOutputSettings()
 * to expose the known failure in CleanerTest.
 */
public class JsoupDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testParseStringBaseUri() {
        Document doc = Jsoup.parse("<p>Hello</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseStringBaseUriParser() {
        Parser xmlParser = Parser.xmlParser();
        Document doc = Jsoup.parse("<root><item/></root>", "http://example.com", xmlParser);
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        // XML parser preserves structure
        assertEquals("<root> <item /> </root>", doc.html().replaceAll("\\s+", " ").trim());
    }

    @Test(timeout = 4000)
    public void testParseStringOnly() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        assertNotNull(doc);
        assertEquals("", doc.baseUri());
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testConnect() {
        // Cannot actually connect, but we can verify the returned object is a Connection
        Connection conn = Jsoup.connect("http://example.com");
        assertNotNull(conn);
        assertTrue(conn instanceof HttpConnection);
    }

    @Test(timeout = 4000)
    public void testParseFileCharsetBaseUri() throws IOException {
        // Use a temporary file with known content
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        java.io.FileWriter fw = new java.io.FileWriter(temp);
        fw.write("<html><body><p>Test</p></body></html>");
        fw.close();
        Document doc = Jsoup.parse(temp, "UTF-8", "http://base.com");
        assertNotNull(doc);
        assertEquals("http://base.com", doc.baseUri());
        assertEquals("<p>Test</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseFileCharset() throws IOException {
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        java.io.FileWriter fw = new java.io.FileWriter(temp);
        fw.write("<html><body><p>Test</p></body></html>");
        fw.close();
        Document doc = Jsoup.parse(temp, "UTF-8");
        assertNotNull(doc);
        // baseUri should be file's absolute path
        assertEquals(temp.getAbsolutePath(), doc.baseUri());
        assertEquals("<p>Test</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamCharsetBaseUri() throws IOException {
        String html = "<html><body><p>Hello</p></body></html>";
        InputStream in = new java.io.ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", "http://base.com");
        assertNotNull(doc);
        assertEquals("http://base.com", doc.baseUri());
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamCharsetBaseUriParser() throws IOException {
        String html = "<root><item/></root>";
        InputStream in = new java.io.ByteArrayInputStream(html.getBytes("UTF-8"));
        Parser xmlParser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, "UTF-8", "http://base.com", xmlParser);
        assertNotNull(doc);
        assertEquals("http://base.com", doc.baseUri());
        assertEquals("<root> <item /> </root>", doc.html().replaceAll("\\s+", " ").trim());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentStringBaseUri() {
        Document doc = Jsoup.parseBodyFragment("<p>Hello</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        // body fragment should be wrapped in a document
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentString() {
        Document doc = Jsoup.parseBodyFragment("<p>Hello</p>");
        assertNotNull(doc);
        assertEquals("", doc.baseUri());
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseURLTimeout() throws IOException {
        // This test will actually try to connect; we use a non-routable address to force timeout/exception
        // But we can't guarantee no network; we'll use a known invalid URL that throws MalformedURLException
        // Actually parse(URL, int) expects a valid URL; we'll test with a valid but non-http URL? It will throw MalformedURLException.
        // To avoid network, we can test with a URL that is valid but will cause an IOException (e.g., file URL)
        // But file URL may not be supported. We'll just test that the method returns a Document when connection succeeds? Not possible.
        // Instead, we test that it throws an exception for a malformed URL (non-http/https)
        try {
            Jsoup.parse(new URL("ftp://example.com"), 1000);
            fail("Expected MalformedURLException or IOException");
        } catch (IOException e) {
            // expected
        }
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testParseNullHtml() {
        // parse(String) with null should throw NullPointerException (or maybe not, but we test)
        try {
            Jsoup.parse(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseEmptyHtml() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    @Test(timeout = 4000)
    public void testParseNullBaseUri() {
        // parse(String, String) with null baseUri - Parser.parse may handle null
        Document doc = Jsoup.parse("<p>Hi</p>", null);
        assertNotNull(doc);
        // baseUri might be null or empty string depending on implementation
        // We just check no exception
    }

    @Test(timeout = 4000)
    public void testParseNullParser() {
        // parse(String, String, Parser) with null parser - should throw NullPointerException
        try {
            Jsoup.parse("<p>Hi</p>", "http://example.com", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConnectNullUrl() {
        try {
            Jsoup.connect(null);
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (Exception e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConnectInvalidUrl() {
        try {
            Jsoup.connect("not a url");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseFileNullCharset() throws IOException {
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        java.io.FileWriter fw = new java.io.FileWriter(temp);
        fw.write("<html><body><p>Test</p></body></html>");
        fw.close();
        // charsetName null should be handled (determine from meta or fallback to UTF-8)
        Document doc = Jsoup.parse(temp, null, "http://base.com");
        assertNotNull(doc);
        assertEquals("<p>Test</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseFileNullBaseUri() throws IOException {
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        java.io.FileWriter fw = new java.io.FileWriter(temp);
        fw.write("<html><body><p>Test</p></body></html>");
        fw.close();
        // baseUri null - DataUtil.load may handle null
        Document doc = Jsoup.parse(temp, "UTF-8", null);
        assertNotNull(doc);
        // baseUri might be empty string or null
    }

    @Test(timeout = 4000)
    public void testParseInputStreamNullCharset() throws IOException {
        String html = "<html><body><p>Hello</p></body></html>";
        InputStream in = new java.io.ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, null, "http://base.com");
        assertNotNull(doc);
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamNullBaseUri() throws IOException {
        String html = "<html><body><p>Hello</p></body></html>";
        InputStream in = new java.io.ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", null);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testParseInputStreamNullParser() throws IOException {
        String html = "<root><item/></root>";
        InputStream in = new java.io.ByteArrayInputStream(html.getBytes("UTF-8"));
        try {
            Jsoup.parse(in, "UTF-8", "http://base.com", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentNullHtml() {
        try {
            Jsoup.parseBodyFragment(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentEmptyHtml() {
        Document doc = Jsoup.parseBodyFragment("");
        assertNotNull(doc);
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseURLNegativeTimeout() {
        // parse(URL, int) with negative timeout - should throw IllegalArgumentException
        try {
            Jsoup.parse(new URL("http://example.com"), -1);
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            // expected (IllegalArgumentException or IOException)
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testIsValidWithWhitelist() {
        // This test targets the known defect in CleanerTest.testIsValidBodyHtml
        // The bug may cause isValid to return false when it should be true, or vice versa.
        // We test a simple case: a body fragment with only allowed tags.
        Whitelist whitelist = Whitelist.relaxed(); // allows most tags
        String validHtml = "<p>Hello</p>";
        assertTrue("isValid should return true for allowed HTML", Jsoup.isValid(validHtml, whitelist));

        // Test with a tag not in whitelist
        String invalidHtml = "<script>alert('xss')</script>";
        assertFalse("isValid should return false for disallowed HTML", Jsoup.isValid(invalidHtml, whitelist));

        // Test with null whitelist - should throw NullPointerException
        try {
            Jsoup.isValid("<p>Hello</p>", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCleanWithOutputSettings() {
        // This test targets the known defect in CleanerTest.testIsValidDocument (maybe related to clean)
        Whitelist whitelist = Whitelist.basic();
        String dirtyHtml = "<p>Hello <b>World</b></p>";
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        String cleaned = Jsoup.clean(dirtyHtml, "http://example.com", whitelist, settings);
        // basic whitelist allows p and b
        assertEquals("<p>Hello <b>World</b></p>", cleaned);

        // Test with null output settings - should throw NullPointerException
        try {
            Jsoup.clean(dirtyHtml, "http://example.com", whitelist, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCleanWithNullWhitelist() {
        try {
            Jsoup.clean("<p>Hello</p>", "http://example.com", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCleanWithNullBaseUri() {
        Whitelist whitelist = Whitelist.none();
        String cleaned = Jsoup.clean("<p>Hello</p>", null, whitelist);
        // none whitelist removes all tags
        assertEquals("Hello", cleaned);
    }

    @Test(timeout = 4000)
    public void testCleanWithEmptyBodyHtml() {
        Whitelist whitelist = Whitelist.basic();
        String cleaned = Jsoup.clean("", whitelist);
        assertEquals("", cleaned);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IOException.class)
    public void testParseFileNotFound() throws IOException {
        File nonExistent = new File("/nonexistent/file.html");
        Jsoup.parse(nonExistent, "UTF-8", "http://base.com");
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testParseInputStreamInvalidCharset() throws IOException {
        // DataUtil.load may throw IOException for invalid charset
        String html = "<html><body><p>Hello</p></body></html>";
        InputStream in = new java.io.ByteArrayInputStream(html.getBytes("UTF-8"));
        Jsoup.parse(in, "invalid-charset", "http://base.com");
    }

    @Test(timeout = 4000)
    public void testParseURLMalformed() {
        // parse(URL, int) with non-http/https URL should throw MalformedURLException
        try {
            Jsoup.parse(new URL("ftp://example.com"), 1000);
            fail("Expected MalformedURLException");
        } catch (IOException e) {
            // expected (MalformedURLException is subclass of IOException)
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    // Not applicable for static utility class.

    // Additional coverage: parse(File, String) with null charset
    @Test(timeout = 4000)
    public void testParseFileNullCharsetNoBaseUri() throws IOException {
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        java.io.FileWriter fw = new java.io.FileWriter(temp);
        fw.write("<html><body><p>Test</p></body></html>");
        fw.close();
        Document doc = Jsoup.parse(temp, null);
        assertNotNull(doc);
        assertEquals("<p>Test</p>", doc.body().html());
    }

    // Test parse(InputStream, String, String, Parser) with null parser (already covered)
    // Test parse(InputStream, String, String) with null charset (covered)
    // Test parse(InputStream, String, String) with null baseUri (covered)
    // Test parseBodyFragment(String, String) with null baseUri
    @Test(timeout = 4000)
    public void testParseBodyFragmentNullBaseUri() {
        Document doc = Jsoup.parseBodyFragment("<p>Hi</p>", null);
        assertNotNull(doc);
        // baseUri may be null or empty
    }

    // Test clean(String, Whitelist) with null whitelist
    @Test(timeout = 4000)
    public void testCleanSingleArgNullWhitelist() {
        try {
            Jsoup.clean("<p>Hello</p>", (Whitelist) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // Test clean(String, String, Whitelist) with null bodyHtml
    @Test(timeout = 4000)
    public void testCleanNullBodyHtml() {
        Whitelist whitelist = Whitelist.basic();
        try {
            Jsoup.clean(null, "http://example.com", whitelist);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // Test clean(String, String, Whitelist, OutputSettings) with null bodyHtml
    @Test(timeout = 4000)
    public void testCleanWithSettingsNullBodyHtml() {
        Whitelist whitelist = Whitelist.basic();
        Document.OutputSettings settings = new Document.OutputSettings();
        try {
            Jsoup.clean(null, "http://example.com", whitelist, settings);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // Test isValid with null bodyHtml
    @Test(timeout = 4000)
    public void testIsValidNullBodyHtml() {
        Whitelist whitelist = Whitelist.basic();
        try {
            Jsoup.isValid(null, whitelist);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // Test connect with empty string
    @Test(timeout = 4000)
    public void testConnectEmptyUrl() {
        try {
            Jsoup.connect("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Test parse(URL, int) with zero timeout
    @Test(timeout = 4000)
    public void testParseURLZeroTimeout() throws IOException {
        // Zero timeout may cause immediate timeout or be accepted
        try {
            Jsoup.parse(new URL("http://example.com"), 0);
            // If no exception, we just check document is not null? But it will likely throw.
        } catch (IOException e) {
            // expected
        }
    }
}