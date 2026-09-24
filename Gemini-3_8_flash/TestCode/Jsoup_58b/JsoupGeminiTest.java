package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.Jsoup
 * Core Functionality: Facade entry point for HTML parsing, fragment parsing, cleaning, validation, and HTTP.
 *
 * Decision / Branch Matrix:
 * 1. Constructor Jsoup():
 *    - Private default constructor; tested via reflection for lifecycle/instantiation integrity.
 * 2. parse(String html, [String baseUri], [Parser parser]):
 *    - HTML string with standard baseUri, empty baseUri, custom XML/HTML parsers.
 * 3. parse(File in, String charsetName, [String baseUri]):
 *    - File with explicit baseUri, file with default derived path, charset UTF-8 and null charset fallback.
 * 4. parse(InputStream in, String charsetName, String baseUri, [Parser parser]):
 *    - Standard stream parsing, null charset detection, alternate XML parser routing.
 * 5. parseBodyFragment(String bodyHtml, [String baseUri]):
 *    - Verifies fragment encapsulated properly within Document body container.
 * 6. clean(String bodyHtml, [String baseUri], Whitelist whitelist, [OutputSettings settings]):
 *    - Normalization, removal of malicious tags/attributes, relative link resolution, custom OutputSettings.
 * 7. isValid(String bodyHtml, Whitelist whitelist):
 *    - Targeted Defect (CleanerTest::testIsValidBodyHtml & testIsValidDocument):
 *      Detects invalid content outside or inside body (head scripts, comments, metadata, disallowed attributes).
 * 8. connect(String url) & parse(URL url, int timeoutMillis):
 *    - Connection creation, protocol boundary validation (http/https vs unsupported protocols like ftp/file).
 * ---------------------------------------------------------------------------------------------------------
 */
public class JsoupGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseStringDefaultBaseUri() {
        String html = "<html><head><title>Jsoup Facade Test</title></head><body><p>Hello Jsoup</p></body></html>";
        Document doc = Jsoup.parse(html);

        assertNotNull("Document should not be null", doc);
        assertEquals("Jsoup Facade Test", doc.title());
        assertEquals("Hello Jsoup", doc.select("p").first().text());
        assertEquals("", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testParseStringWithBaseUri() {
        String html = "<p><a href='/relative/path'>Link</a></p>";
        Document doc = Jsoup.parse(html, "http://example.com/base/");

        assertNotNull("Document should not be null", doc);
        Element link = doc.select("a").first();
        assertNotNull(link);
        assertEquals("http://example.com/relative/path", link.attr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testParseStringWithCustomXmlParser() {
        String xml = "<xml><item id='1'>TextContent</item></xml>";
        Document doc = Jsoup.parse(xml, "http://example.com", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("TextContent", doc.select("item").first().text());
        assertEquals("1", doc.select("item").attr("id"));
    }

    @Test(timeout = 4000)
    public void testParseBodyFragment() {
        String fragment = "<div><span>Fragment Content</span></div>";
        Document doc = Jsoup.parseBodyFragment(fragment);

        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(1, doc.body().children().size());
        assertEquals("Fragment Content", doc.body().select("span").first().text());
        assertEquals("", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithBaseUri() {
        String fragment = "<a href='subpage'>Fragment Link</a>";
        Document doc = Jsoup.parseBodyFragment(fragment, "http://example.com/app/");

        assertNotNull(doc);
        Element a = doc.body().select("a").first();
        assertNotNull(a);
        assertEquals("http://example.com/app/subpage", a.attr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testParseFileWithExplicitBaseUri() throws IOException {
        File temp = File.createTempFile("jsoup_test_explicit", ".html");
        temp.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write("<title>File Test</title><p>File Content</p>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = Jsoup.parse(temp, "UTF-8", "http://example.org/dir/");
        assertNotNull(doc);
        assertEquals("File Test", doc.title());
        assertEquals("File Content", doc.select("p").text());
        assertEquals("http://example.org/dir/", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testParseFileWithDefaultBaseUri() throws IOException {
        File temp = File.createTempFile("jsoup_test_default", ".html");
        temp.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write("<h1>Heading</h1>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = Jsoup.parse(temp, "UTF-8");
        assertNotNull(doc);
        assertEquals("Heading", doc.select("h1").text());
        assertEquals(temp.getAbsolutePath(), doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testParseInputStream() throws IOException {
        byte[] bytes = "<title>Stream Title</title><p>Stream Paragraph</p>".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            Document doc = Jsoup.parse(in, "UTF-8", "http://stream.example.com");
            assertNotNull(doc);
            assertEquals("Stream Title", doc.title());
            assertEquals("Stream Paragraph", doc.select("p").text());
            assertEquals("http://stream.example.com", doc.baseUri());
        }
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithXmlParser() throws IOException {
        byte[] bytes = "<feed><entry><title>Entry Title</title></entry></feed>".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            Document doc = Jsoup.parse(in, "UTF-8", "http://feed.example.com", Parser.xmlParser());
            assertNotNull(doc);
            assertEquals("Entry Title", doc.select("title").text());
        }
    }

    @Test(timeout = 4000)
    public void testCleanBodyHtmlBasic() {
        String dirty = "<p>Clean <a href='http://example.com/'>link</a><script>alert('xss')</script></p>";
        String cleaned = Jsoup.clean(dirty, Whitelist.basic());

        assertNotNull(cleaned);
        assertFalse(cleaned.contains("script"));
        assertTrue(cleaned.contains("<a href=\"http://example.com/\" rel=\"nofollow\">link</a>"));
        assertTrue(cleaned.contains("<p>Clean"));
    }

    @Test(timeout = 4000)
    public void testCleanBodyHtmlWithBaseUri() {
        String dirty = "<a href='/path'>Relative</a>";
        String cleaned = Jsoup.clean(dirty, "http://example.com/", Whitelist.basic());

        assertTrue(cleaned.contains("http://example.com/path"));
        assertTrue(cleaned.contains("rel=\"nofollow\""));
    }

    @Test(timeout = 4000)
    public void testCleanBodyHtmlWithCustomOutputSettings() {
        String dirty = "<p>Line 1<br>Line 2</p>";
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);

        String cleaned = Jsoup.clean(dirty, "http://example.com", Whitelist.basic(), settings);
        assertNotNull(cleaned);
        assertEquals("<p>Line 1<br />Line 2</p>", cleaned);
    }

    @Test(timeout = 4000)
    public void testConnectReturnsFunctionalConnection() {
        Connection con = Jsoup.connect("http://example.com/index.html");
        assertNotNull(con);
        assertEquals("http://example.com/index.html", con.request().url().toString());

        con.userAgent("JsoupGeminiAgent");
        assertEquals("JsoupGeminiAgent", con.request().userAgent());
        con.timeout(5000);
        assertEquals(5000, con.request().timeout());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseEmptyString() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseEmptyBodyFragment() {
        Document doc = Jsoup.parseBodyFragment("");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanEmptyString() {
        String cleaned = Jsoup.clean("", Whitelist.basic());
        assertEquals("", cleaned);
    }

    @Test(timeout = 4000)
    public void testParseFileWithNullCharsetFallsBackSafely() throws IOException {
        File temp = File.createTempFile("jsoup_null_charset", ".html");
        temp.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write("<div><span>Fallback Text</span></div>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = Jsoup.parse(temp, null);
        assertNotNull(doc);
        assertEquals("Fallback Text", doc.select("span").text());
    }

    @Test(timeout = 4000)
    public void testParseInputStreamWithNullCharsetFallsBackSafely() throws IOException {
        byte[] bytes = "<p>Null Charset Stream</p>".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            Document doc = Jsoup.parse(in, null, "");
            assertNotNull(doc);
            assertEquals("Null Charset Stream", doc.select("p").text());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Cleaner isValid Defect)
    // =========================================================================

    /**
     * Targets defects documented in CleanerTest::testIsValidBodyHtml and CleanerTest::testIsValidDocument.
     * Ensures isValid accurately returns false when HTML contains discarded nodes in the head,
     * discarded top-level comments, scripts, or disallowed attributes, even when body appears balanced.
     */
    @Test(timeout = 4000)
    public void testIsValidBodyHtmlDefectConditions() {
        String ok = "<p>Test <b><a href='http://example.com/'>do ok</a></b></p>";
        String ok1 = "<p>Test <b><a href='http://example.com/'>do ok</a></b>"; // unbalanced tags are tidied
        String nok1 = "<p><script></script>Not <b>ok</b></p>"; // script must invalidate
        String nok2 = "<p align=right>Test Not <b>ok</b></p>"; // disallowed attribute
        String nok3 = "<!-- comment --><p>Not ok</p>"; // comment discarded -> invalid
        String nok4 = "<html><head>Foo</head><body><b>OK</b></body></html>"; // content in head -> invalid
        String nok5 = "<p>Test <b><a href='http://example.com/' rel='nofollow'>do ok</a></b></p>"; // manual enforced rel

        assertTrue("Valid HTML fragment should pass isValid", Jsoup.isValid(ok, Whitelist.basic()));
        assertTrue("Tidied valid HTML fragment should pass isValid", Jsoup.isValid(ok1, Whitelist.basic()));
        assertFalse("HTML with <script> must NOT be valid", Jsoup.isValid(nok1, Whitelist.basic()));
        assertFalse("HTML with disallowed attribute must NOT be valid", Jsoup.isValid(nok2, Whitelist.basic()));
        assertFalse("HTML with top-level comment must NOT be valid", Jsoup.isValid(nok3, Whitelist.basic()));
        assertFalse("Full HTML with head content must NOT be valid for body fragment", Jsoup.isValid(nok4, Whitelist.basic()));
        assertFalse("HTML with explicit enforced attribute must be invalidated prior to clean", Jsoup.isValid(nok5, Whitelist.basic()));
    }

    @Test(timeout = 4000)
    public void testIsValidDocumentDefectCondition() {
        String okDoc = "<html><head></head><body><p>Hello World</p></body></html>";
        String nokDocScriptInHead = "<html><head><script>alert('bad');</script></head><body><p>Hello</p></body></html>";
        String nokDocTitle = "<html><head><title>Title Not Allowed</title></head><body><p>Hello</p></body></html>";

        // Whitelist.basic() is intended for body fragments and disallows head contents (scripts, titles, meta)
        assertTrue("Clean body content should be valid", Jsoup.isValid(okDoc, Whitelist.basic()));
        assertFalse("Head scripts must trigger validation failure", Jsoup.isValid(nokDocScriptInHead, Whitelist.basic()));
        assertFalse("Head title must trigger validation failure under fragment whitelist", Jsoup.isValid(nokDocTitle, Whitelist.basic()));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void testParseNonExistentFileThrowsException() throws IOException {
        File nonExistent = new File("/non/existent/path/for/jsoup_test_12345.html");
        Jsoup.parse(nonExistent, "UTF-8");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectNullUrlThrowsException() {
        Jsoup.connect(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectUnsupportedProtocolThrowsException() {
        Jsoup.connect("ftp://ftp.example.com/file.txt");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseUrlUnsupportedProtocolThrowsException() throws IOException {
        URL url = new URL("ftp://example.com/test.html");
        Jsoup.parse(url, 2000);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCleanNullHtmlThrowsException() {
        Jsoup.clean(null, Whitelist.basic());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCleanNullWhitelistThrowsException() {
        Jsoup.clean("<p>Test</p>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsValidNullHtmlThrowsException() {
        Jsoup.isValid(null, Whitelist.basic());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsValidNullWhitelistThrowsException() {
        Jsoup.isValid("<p>Test</p>", null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorIsPrivateAndInstantiableViaReflection() throws Exception {
        Constructor<Jsoup> constructor = Jsoup.class.getDeclaredConstructor();
        assertTrue("Constructor must be declared private", java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Jsoup instance = constructor.newInstance();
        assertNotNull("Jsoup instance created via reflection should not be null", instance);
    }
}