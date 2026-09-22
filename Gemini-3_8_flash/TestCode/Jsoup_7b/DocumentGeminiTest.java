package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.jsoup.nodes.Document
 * Known Defect (Defects4J): DocumentTest::testNormalisesStructure
 *   - Root cause: Document#normalise() fails to merge duplicate <body> elements and fail to enforce
 *     that <body> is parented directly by <html> when nested inside other tags (e.g. <noscript> in <head>).
 *
 * Target Branches & Decision Logic:
 * 1. Document#createShell(baseUri):
 *    - branch: baseUri == null (Validate.notNull) -> IllegalArgumentException
 *    - branch: valid baseUri -> creates document with <html>, <head>, and <body>
 * 2. Document#head() & Document#body():
 *    - branch: element present -> returns Element
 *    - branch: element absent -> returns null
 *    - branch: recursive search in findFirstElementByTagName
 * 3. Document#title():
 *    - branch: <title> tag exists -> returns trimmed text
 *    - branch: <title> tag absent -> returns ""
 * 4. Document#title(newTitle):
 *    - branch: newTitle == null -> IllegalArgumentException
 *    - branch: <title> tag absent -> appends <title> to <head>
 *    - branch: <title> tag exists -> updates existing <title> text
 * 5. Document#createElement(tagName):
 *    - creates unattached Element with document's baseUri
 * 6. Document#normalise():
 *    - branch: <html> element absent -> appends <html>
 *    - branch: <head> element absent -> prepends <head> to <html>
 *    - branch: <body> element absent -> appends <body> to <html>
 *    - branch: text nodes in <head>, <html>, and root -> moved to <body>
 *    - branch: blank text nodes -> skipped (not moved)
 *    - Defect branch: duplicate <body> elements or mis-parented <body> inside <head>/<noscript>
 * 7. Document#text(text):
 *    - sets <body> text without clearing doc root structure
 * 8. Document#nodeName():
 *    - returns "#document"
 * 9. Document.OutputSettings:
 *    - escapeMode: getter & setter
 *    - charset: getter & setter (by Charset and by String name), encoder()
 *    - prettyPrint: getter & setter
 *    - indentAmount: >= 0 valid, < 0 throws IllegalArgumentException
 * ====================================================================================================
 */
public class DocumentGeminiTest {

    private static String stripNewlines(String s) {
        return s.replaceAll("\\r?\\n\\s*", "");
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDocumentConstructorAndNodeName() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(0, doc.childNodes.size());
    }

    @Test(timeout = 4000)
    public void testCreateShellValid() {
        Document doc = Document.createShell("http://example.com/");
        assertNotNull(doc);
        assertEquals("http://example.com/", doc.baseUri());

        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());

        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());

        Element html = doc.child(0);
        assertEquals("html", html.tagName());
        assertSame(html, head.parent());
        assertSame(html, body.parent());
    }

    @Test(timeout = 4000)
    public void testTitleGetAndSetWhenTitleAbsent() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());

        doc.title("Initial Page Title");
        assertEquals("Initial Page Title", doc.title());

        Element titleEl = doc.head().getElementsByTag("title").first();
        assertNotNull(titleEl);
        assertEquals("Initial Page Title", titleEl.text());
    }

    @Test(timeout = 4000)
    public void testTitleUpdateWhenTitlePresent() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("First Title");
        assertEquals("First Title", doc.title());

        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.getElementsByTag("title").size());
    }

    @Test(timeout = 4000)
    public void testTitleTrimming() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendElement("title").text("   Spaced   Title   ");
        assertEquals("Spaced   Title", doc.title());
    }

    @Test(timeout = 4000)
    public void testCreateElement() {
        Document doc = new Document("http://example.com/base/");
        Element div = doc.createElement("div");
        assertNotNull(div);
        assertEquals("div", div.tagName());
        assertEquals("http://example.com/base/", div.baseUri());
        assertNull(div.parent());
        assertEquals(0, doc.childNodes.size());
    }

    @Test(timeout = 4000)
    public void testTextDelegatesToBody() {
        Document doc = Document.createShell("http://example.com/");
        Element returned = doc.text("Hello World Content");
        assertSame(doc, returned);
        assertEquals("Hello World Content", doc.body().text());
        assertNotNull(doc.head());
        assertEquals("head", doc.head().tagName());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWrapsChildrenOnly() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Testing OuterHtml");
        String outer = doc.outerHtml();
        assertFalse(outer.startsWith("#document"));
        assertTrue(outer.contains("<html>"));
        assertTrue(outer.contains("<p>Testing OuterHtml</p>"));
    }

    @Test(timeout = 4000)
    public void testNormaliseMissingHtmlHeadBody() {
        Document doc = new Document("http://example.com/");
        Document returned = doc.normalise();
        assertSame(doc, returned);

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.head().parent().tagName());
        assertEquals("html", doc.body().parent().tagName());
    }

    @Test(timeout = 4000)
    public void testNormaliseMissingHead() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        html.appendElement("body");

        assertNull(doc.head());
        doc.normalise();
        assertNotNull(doc.head());
        assertEquals(0, doc.head().siblingIndex());
        assertEquals(1, doc.body().siblingIndex());
    }

    @Test(timeout = 4000)
    public void testNormaliseMissingBody() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        html.appendElement("head");

        assertNull(doc.body());
        doc.normalise();
        assertNotNull(doc.body());
        assertEquals(0, doc.head().siblingIndex());
        assertEquals(1, doc.body().siblingIndex());
    }

    @Test(timeout = 4000)
    public void testNormaliseMovesNonBlankTextNodesToBody() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");

        // Append text nodes in root, html, and head
        doc.prependText("Root Text");
        html.prependText("Html Text");
        head.prependText("Head Text");
        // Add blank text node that must NOT be moved
        head.appendChild(new TextNode("   \n  ", ""));

        doc.normalise();

        String bodyText = body.text();
        assertTrue(bodyText.contains("Head Text"));
        assertTrue(bodyText.contains("Html Text"));
        assertTrue(bodyText.contains("Root Text"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateShellEmptyUri() {
        Document doc = Document.createShell("");
        assertEquals("", doc.baseUri());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testTitleWithEmptyString() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("");
        assertEquals("", doc.title());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsDefaults() {
        Document doc = new Document("http://example.com/");
        Document.OutputSettings settings = doc.outputSettings();

        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        assertEquals("UTF-8", settings.charset().name());
        assertNotNull(settings.encoder());
        assertTrue(settings.prettyPrint());
        assertEquals(1, settings.indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsChainingAndValues() {
        Document doc = new Document("http://example.com/");
        Document.OutputSettings settings = doc.outputSettings();

        settings.escapeMode(Entities.EscapeMode.extended)
                .charset(Charset.forName("US-ASCII"))
                .prettyPrint(false)
                .indentAmount(0);

        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());
        assertEquals("US-ASCII", settings.charset().name());
        assertEquals("US-ASCII", settings.encoder().charset().name());
        assertFalse(settings.prettyPrint());
        assertEquals(0, settings.indentAmount());

        settings.charset("ISO-8859-1");
        assertEquals("ISO-8859-1", settings.charset().name());
        assertEquals("ISO-8859-1", settings.encoder().charset().name());

        settings.indentAmount(8);
        assertEquals(8, settings.indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsPrettyPrintDisabledHtml() {
        Document doc = Document.createShell("http://example.com/");
        doc.outputSettings().prettyPrint(false);
        doc.body().appendElement("p").text("Content");

        String html = doc.html();
        assertEquals("<html><head></head><body><p>Content</p></body></html>", html);
    }

    @Test(timeout = 4000)
    public void testHeadAndBodyReturnNullWhenMissing() {
        Document doc = new Document("http://example.com/");
        assertNull(doc.head());
        assertNull(doc.body());

        Element div = doc.appendElement("div");
        div.appendElement("span");
        assertNull(doc.head());
        assertNull(doc.body());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug: DocumentTest::testNormalisesStructure
     * When HTML contains a <noscript> element in <head> containing block elements like <p>,
     * parser creates duplicate/misplaced <body> tags inside <noscript>.
     * Document#normalise() MUST ensure that duplicate <body> elements are merged into one
     * and that <body> is correctly parented directly under <html>, NOT trapped inside <noscript>.
     */
    @Test(timeout = 4000)
    public void testNormalisesStructure() {
        String html = "<html><head><script>foo</script><noscript><p>two</p><p>three</p>";
        Document doc = Jsoup.parse(html);
        doc.normalise();

        String expected = "<html><head><script>foo</script><noscript></noscript></head><body><p>two</p><p>three</p></body></html>";
        assertEquals(expected, stripNewlines(doc.html()));
    }

    /**
     * Direct structural validation of normalise() ensuring that nested duplicate <body>
     * elements under <head>/<noscript> are moved to become direct children of <html>.
     */
    @Test(timeout = 4000)
    public void testNormaliseDuplicateBodyAndMisplacedBody() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element noscript = head.appendElement("noscript");
        Element body1 = noscript.appendElement("body");
        body1.appendElement("p").text("two");
        Element body2 = noscript.appendElement("body");
        body2.appendElement("p").text("three");

        doc.normalise();

        assertEquals("html", doc.body().parent().tagName());
        assertSame(html, doc.body().parent());
        assertEquals(1, doc.getElementsByTag("body").size());
        assertEquals("two three", doc.body().text());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateShellNullUriThrowsException() {
        Document.createShell(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTitleNullThrowsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIndentAmountNegativeThrowsException() {
        Document doc = new Document("http://example.com/");
        doc.outputSettings().indentAmount(-1);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDocumentCloneIntegrity() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Original Title");
        doc.body().appendElement("p").text("Original Body");

        Document cloned = (Document) doc.clone();

        assertNotNull(cloned);
        assertNotSame(doc, cloned);
        assertEquals(doc.outerHtml(), cloned.outerHtml());
        assertEquals(doc.baseUri(), cloned.baseUri());
        assertEquals(doc.title(), cloned.title());

        cloned.title("Modified Cloned Title");
        assertEquals("Original Title", doc.title());
        assertEquals("Modified Cloned Title", cloned.title());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsEntityEscaping() {
        Document doc = Document.createShell("http://example.com/");
        doc.outputSettings().escapeMode(Entities.EscapeMode.base);
        doc.outputSettings().charset("US-ASCII");
        doc.body().appendElement("p").text("Price: 100 & 50 > 25, Copyright \u00a9");

        String html = doc.body().html();
        assertTrue(html.contains("&amp;"));
        assertTrue(html.contains("&gt;"));
        assertTrue(html.contains("&copy;"));
    }
}