package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.helper.Validate;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

/**
 * Comprehensive JUnit 4 test suite for Document class.
 * Targets line/branch coverage and the known Defects4J title defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Document constructor: baseUri validation (null handled by Tag.valueOf)
 * - createShell: null baseUri -> Validate.notNull throws
 * - head()/body(): recursive search, null if not found
 * - title() getter: null titleEl -> empty string, else text().trim()
 * - title() setter: null title -> Validate.notNull throws; if titleEl null -> appendElement("title").text(title); else titleEl.text(title)
 * - createElement: tagName validation (Tag.valueOf)
 * - normalise(): htmlEl null -> appendElement; head() null -> prependElement; body() null -> appendElement; normaliseTextNodes (blank/non-blank TextNode); normaliseStructure (duplicate tags, parent mismatch)
 * - outerHtml(): returns super.html()
 * - text(String): body().text(text)
 * - nodeName(): "#document"
 * - clone(): super.clone() + outputSettings.clone()
 * - OutputSettings: escapeMode, charset (String/Charset), prettyPrint, indentAmount (>=0), encoder, clone
 * - QuirksMode: enum get/set
 * - Known defect: title set/get may introduce extra whitespace/newline
 */
public class DocumentDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorAndShell() {
        Document doc = new Document("http://example.com");
        assertEquals("#root", doc.tagName());
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("#document", doc.nodeName());

        Document shell = Document.createShell("http://example.com");
        assertNotNull(shell.head());
        assertNotNull(shell.body());
        assertEquals("html", shell.child(0).tagName());
        assertEquals("head", shell.head().tagName());
        assertEquals("body", shell.body().tagName());
    }

    @Test(timeout = 4000)
    public void testHeadBodyOnEmptyDocument() {
        Document doc = new Document("http://example.com");
        assertNull(doc.head());
        assertNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testHeadBodyOnShell() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("head", doc.head().tagName());
        assertEquals("body", doc.body().tagName());
    }

    @Test(timeout = 4000)
    public void testTitleGetSet() {
        Document doc = Document.createShell("http://example.com");
        // Initially no title element -> empty string
        assertEquals("", doc.title());

        // Set title -> creates title element in head
        doc.title("Hello there now");
        assertEquals("Hello there now", doc.title());

        // Update existing title
        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());

        // Title with whitespace
        doc.title("  Trimmed  ");
        assertEquals("Trimmed", doc.title());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTitleNullThrows() {
        Document doc = Document.createShell("http://example.com");
        doc.title(null);
    }

    @Test(timeout = 4000)
    public void testCreateElement() {
        Document doc = new Document("http://example.com");
        Element div = doc.createElement("div");
        assertEquals("div", div.tagName());
        assertEquals("http://example.com", div.baseUri());
        assertFalse(doc.equals(div)); // not a child
    }

    @Test(timeout = 4000)
    public void testNormaliseAddsMissingHtmlHeadBody() {
        Document doc = new Document("http://example.com");
        assertNull(doc.head());
        assertNull(doc.body());
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertNotNull(doc.getElementsByTag("html").first());
    }

    @Test(timeout = 4000)
    public void testNormaliseMovesTextNodesToBody() {
        Document doc = Document.createShell("http://example.com");
        // Add a text node directly to document (outside body)
        doc.appendChild(new TextNode("Some text", ""));
        doc.normalise();
        // Text should now be in body
        assertTrue(doc.body().text().contains("Some text"));
    }

    @Test(timeout = 4000)
    public void testNormaliseRemovesDuplicateHeadBody() {
        Document doc = Document.createShell("http://example.com");
        // Add duplicate head and body
        Element html = doc.getElementsByTag("html").first();
        html.appendChild(new Element(Tag.valueOf("head"), ""));
        html.appendChild(new Element(Tag.valueOf("body"), ""));
        doc.normalise();
        Elements heads = doc.getElementsByTag("head");
        Elements bodies = doc.getElementsByTag("body");
        assertEquals(1, heads.size());
        assertEquals(1, bodies.size());
    }

    @Test(timeout = 4000)
    public void testNormaliseReparentsHeadBodyToHtml() {
        Document doc = new Document("http://example.com");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");
        // Move head and body outside html (simulate malformed)
        doc.removeChild(head);
        doc.removeChild(body);
        doc.appendChild(head);
        doc.appendChild(body);
        doc.normalise();
        assertEquals(html, head.parent());
        assertEquals(html, body.parent());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        Document doc = Document.createShell("http://example.com");
        String html = doc.outerHtml();
        assertTrue(html.startsWith("<html>"));
        assertTrue(html.endsWith("</html>"));
        assertFalse(html.contains("#document"));
    }

    @Test(timeout = 4000)
    public void testTextMethod() {
        Document doc = Document.createShell("http://example.com");
        doc.text("New body text");
        assertEquals("New body text", doc.body().text());
        // Ensure head and html structure intact
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testNodeName() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Document doc = Document.createShell("http://example.com");
        doc.title("Original");
        Document clone = doc.clone();
        assertEquals(doc.title(), clone.title());
        assertEquals(doc.baseUri(), clone.baseUri());
        assertNotSame(doc, clone);
        assertNotSame(doc.outputSettings(), clone.outputSettings());
        // Modify clone, original unchanged
        clone.title("Changed");
        assertEquals("Original", doc.title());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateShellNullBaseUri() {
        Document.createShell(null);
    }

    @Test(timeout = 4000)
    public void testTitleEmptyWhenNoTitleElement() {
        Document doc = new Document("http://example.com");
        assertEquals("", doc.title());
    }

    @Test(timeout = 4000)
    public void testTitleWithNewlinesAndSpaces() {
        Document doc = Document.createShell("http://example.com");
        // This test targets the known defect: setting title may introduce extra whitespace
        doc.title("Hello there now");
        assertEquals("Hello there now", doc.title());
        // Additional boundary: title with leading/trailing newlines
        doc.title("\n  Hello  \n");
        assertEquals("Hello", doc.title());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsDefaults() {
        Document doc = new Document("http://example.com");
        OutputSettings settings = doc.outputSettings();
        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        assertEquals(Charset.forName("UTF-8"), settings.charset());
        assertTrue(settings.prettyPrint());
        assertEquals(1, settings.indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsBoundaries() {
        Document doc = new Document("http://example.com");
        OutputSettings settings = doc.outputSettings();
        // indentAmount must be >= 0
        settings.indentAmount(0);
        assertEquals(0, settings.indentAmount());
        settings.indentAmount(10);
        assertEquals(10, settings.indentAmount());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIndentAmountNegativeThrows() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().indentAmount(-1);
    }

    @Test(timeout = 4000)
    public void testCharsetString() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().charset("ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsClone() {
        OutputSettings original = new Document("http://example.com").outputSettings();
        OutputSettings clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.escapeMode(), clone.escapeMode());
        assertEquals(original.charset(), clone.charset());
        assertEquals(original.prettyPrint(), clone.prettyPrint());
        assertEquals(original.indentAmount(), clone.indentAmount());
        // Modify clone, original unchanged
        clone.escapeMode(Entities.EscapeMode.extended);
        assertNotEquals(original.escapeMode(), clone.escapeMode());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testTitleDefectRevealing() {
        // This test directly targets the known Defects4J defect:
        // After setting title to "Hello there now", getter should return exactly that string.
        Document doc = Document.createShell("http://example.com");
        doc.title("Hello there now");
        String title = doc.title();
        assertEquals("Hello there now", title);
        // Also verify that no extra whitespace or newline is present
        assertFalse(title.contains("\n"));
        assertFalse(title.contains("\r"));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testOutputSettingsNullThrows() {
        Document doc = new Document("http://example.com");
        doc.outputSettings(null);
    }

    @Test(timeout = 4000)
    public void testQuirksModeDefault() {
        Document doc = new Document("http://example.com");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testQuirksModeSet() {
        Document doc = new Document("http://example.com");
        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testOutputSettingsChaining() {
        Document doc = new Document("http://example.com");
        OutputSettings settings = doc.outputSettings();
        assertSame(settings, settings.escapeMode(Entities.EscapeMode.extended));
        assertSame(settings, settings.charset("UTF-8"));
        assertSame(settings, settings.prettyPrint(false));
        assertSame(settings, settings.indentAmount(2));
    }

    @Test(timeout = 4000)
    public void testEncoderUpdatesOnCharsetChange() {
        Document doc = new Document("http://example.com");
        OutputSettings settings = doc.outputSettings();
        CharsetEncoder originalEncoder = settings.encoder();
        settings.charset("ISO-8859-1");
        assertNotSame(originalEncoder, settings.encoder());
    }

    @Test(timeout = 4000)
    public void testDocumentCloneDeepCopyOutputSettings() {
        Document doc = Document.createShell("http://example.com");
        doc.outputSettings().prettyPrint(false);
        Document clone = doc.clone();
        assertNotSame(doc.outputSettings(), clone.outputSettings());
        assertEquals(doc.outputSettings().prettyPrint(), clone.outputSettings().prettyPrint());
        // Modify clone's settings, original unchanged
        clone.outputSettings().prettyPrint(true);
        assertFalse(doc.outputSettings().prettyPrint());
    }

    @Test(timeout = 4000)
    public void testNormaliseTextNodesOnlyNonBlank() {
        Document doc = Document.createShell("http://example.com");
        // Add blank and non-blank text nodes to head
        Element head = doc.head();
        head.appendChild(new TextNode("   ", ""));
        head.appendChild(new TextNode("real", ""));
        doc.normalise();
        // Only non-blank text should be moved to body
        assertTrue(doc.body().text().contains("real"));
        assertFalse(doc.body().text().contains("   "));
    }

    @Test(timeout = 4000)
    public void testNormaliseStructureMultipleDuplicates() {
        Document doc = Document.createShell("http://example.com");
        Element html = doc.getElementsByTag("html").first();
        // Add two extra head elements with content
        Element extraHead1 = html.appendElement("head");
        extraHead1.appendElement("title").text("Extra1");
        Element extraHead2 = html.appendElement("head");
        extraHead2.appendElement("title").text("Extra2");
        doc.normalise();
        Elements heads = doc.getElementsByTag("head");
        assertEquals(1, heads.size());
        Element masterHead = heads.first();
        // Content from duplicates should be merged into master
        assertTrue(masterHead.html().contains("Extra1"));
        assertTrue(masterHead.html().contains("Extra2"));
    }

    @Test(timeout = 4000)
    public void testFindFirstElementByTagNameRecursive() {
        Document doc = Document.createShell("http://example.com");
        Element found = doc.findFirstElementByTagName("title", doc);
        assertNull(found); // no title element initially
        doc.head().appendElement("title").text("Test");
        found = doc.findFirstElementByTagName("title", doc);
        assertNotNull(found);
        assertEquals("title", found.tagName());
    }
}