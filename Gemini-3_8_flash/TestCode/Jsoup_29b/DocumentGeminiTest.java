package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

/*
 [Branch & Defect Analysis Matrix]
 ---------------------------------------------------------------------------------------------------
 Target Class: org.jsoup.nodes.Document & Document.OutputSettings
 Defect ID: DocumentTest::testTitles (junit.framework.AssertionFailedError: expected:<Hello[ there] now> but was:<Hello[)

 Decision Branches & Boundary Conditions Targeted:
 1. title():
    - title element null vs non-null branch.
    - DEFECT ZONE: Whitespace normalization inside <title>. Tag "title" preserves whitespace in parsing,
      but Document#title() contract mandates whitespace normalization across newlines and spaces.
 2. title(String):
    - title == null (Validate.notNull defensive guard).
    - titleEl == null (create new <title> in head) vs titleEl != null (update existing <title> text).
 3. createShell(String):
    - baseUri == null (Validate.notNull defensive check).
    - Validation of created shell: <html>, <head>, and <body> hierarchy.
 4. head() & body():
    - findFirstElementByTagName recursive search: target tag exists vs target tag absent (null return).
 5. normalise():
    - htmlEl == null -> appendElement("html").
    - head() == null -> htmlEl.prependElement("head").
    - body() == null -> htmlEl.appendElement("body").
    - normaliseTextNodes(Element):
      * node instanceof TextNode == true vs false (e.g. Element, Comment).
      * tn.isBlank() == true (skipped) vs false (moved to body).
      * Reverse iteration order prepending to body with spacer.
    - normaliseStructure(String tag, Element htmlEl):
      * elements.size() > 1 (multiple <head> or <body> tags: dupes flattened into master).
      * !master.parent().equals(htmlEl) (master head/body reparented under <html>).
 6. createElement(String):
    - Element factory with document baseUri and detached parent state.
 7. outerHtml():
    - Stripped wrapper verification delegating to super.html().
 8. text(String):
    - Document body text replacement without nuking document shell structure.
 9. clone():
    - Deep copy of Document tree and deep copy of OutputSettings.
 10. OutputSettings:
    - escapeMode, charset, prettyPrint, indentAmount accessors and mutators.
    - indentAmount boundary: >= 0 (valid: 0, >0) vs < 0 (Validate.isTrue exception).
    - OutputSettings#clone() independent encoder, charset, and escapeMode.
 11. QuirksMode:
    - Default state (noQuirks), transitions to quirks and limitedQuirks.
*/
public class DocumentGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateShellStructure() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("#document", doc.nodeName());

        Element head = doc.head();
        assertNotNull("Head should not be null in shell", head);
        assertEquals("head", head.tagName());

        Element body = doc.body();
        assertNotNull("Body should not be null in shell", body);
        assertEquals("body", body.tagName());

        assertNotNull("Html element must be present", doc.select("html").first());
        assertEquals(doc.select("html").first(), head.parent());
        assertEquals(doc.select("html").first(), body.parent());
    }

    @Test(timeout = 4000)
    public void testHeadAndBodyLookupWhenMissing() {
        Document doc = new Document("http://example.com/");
        assertNull("Head should be null if not added", doc.head());
        assertNull("Body should be null if not added", doc.body());
    }

    @Test(timeout = 4000)
    public void testTitleGetAndSetWhenTitleInitiallyAbsent() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());

        doc.title("Page Title");
        assertEquals("Page Title", doc.title());

        Element titleEl = doc.head().getElementsByTag("title").first();
        assertNotNull(titleEl);
        assertEquals("Page Title", titleEl.text());
    }

    @Test(timeout = 4000)
    public void testTitleUpdateWhenTitleAlreadyPresent() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Initial Title");
        assertEquals("Initial Title", doc.title());

        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.getElementsByTag("title").size());
    }

    @Test(timeout = 4000)
    public void testCreateElementFactory() {
        Document doc = new Document("http://example.com/base/");
        Element div = doc.createElement("div");

        assertNotNull(div);
        assertEquals("div", div.tagName());
        assertEquals("http://example.com/base/", div.baseUri());
        assertNull("Element should not be attached to document tree", div.parent());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlDelegation() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Sample Text");
        String html = doc.outerHtml();

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<p>Sample Text</p>"));
        assertFalse(html.startsWith("<#document>"));
    }

    @Test(timeout = 4000)
    public void testDocumentTextMethodModifiesBodyOnly() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Retained Head Title");
        Element returned = doc.text("New Body Text");

        assertSame(doc, returned);
        assertEquals("New Body Text", doc.body().text());
        assertEquals("Retained Head Title", doc.title());
    }

    @Test(timeout = 4000)
    public void testQuirksModeTransitions() {
        Document doc = new Document("http://example.com/");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testOutputSettingsIndentAmountBoundaries() {
        Document.OutputSettings settings = new Document.OutputSettings();
        assertEquals(1, settings.indentAmount());

        settings.indentAmount(0);
        assertEquals(0, settings.indentAmount());

        settings.indentAmount(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, settings.indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsPropertyMutations() {
        Document.OutputSettings settings = new Document.OutputSettings();

        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());

        settings.prettyPrint(false);
        assertFalse(settings.prettyPrint());

        Charset iso = Charset.forName("ISO-8859-1");
        settings.charset(iso);
        assertEquals(iso, settings.charset());
        assertEquals("ISO-8859-1", settings.encoder().charset().name());

        settings.charset("US-ASCII");
        assertEquals(Charset.forName("US-ASCII"), settings.charset());
        assertEquals("US-ASCII", settings.encoder().charset().name());
    }

    @Test(timeout = 4000)
    public void testNormaliseMissingHtmlHeadBodyBranches() {
        Document doc = new Document("http://example.com/");
        doc.normalise();

        assertNotNull(doc.select("html").first());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testNormaliseMissingOnlyHead() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        html.appendElement("body");

        assertNull(doc.head());
        doc.normalise();
        assertNotNull(doc.head());
        assertEquals(doc.select("html").first(), doc.head().parent());
    }

    @Test(timeout = 4000)
    public void testNormaliseMissingOnlyBody() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        html.appendElement("head");

        assertNull(doc.body());
        doc.normalise();
        assertNotNull(doc.body());
        assertEquals(doc.select("html").first(), doc.body().parent());
    }

    @Test(timeout = 4000)
    public void testNormaliseWithTextNodesAndBlanksAndComments() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");

        // Add non-blank and blank text nodes, as well as non-text nodes
        doc.appendChild(new TextNode("doc-text", ""));
        doc.appendChild(new TextNode("   \n\t", ""));
        doc.appendChild(new Comment("a comment", ""));

        html.appendChild(new TextNode("html-text", ""));
        head.appendChild(new TextNode("head-text", ""));

        doc.normalise();

        String bodyText = body.text();
        assertTrue("Head text should be moved to body", bodyText.contains("head-text"));
        assertTrue("Html text should be moved to body", bodyText.contains("html-text"));
        assertTrue("Doc text should be moved to body", bodyText.contains("doc-text"));
    }

    @Test(timeout = 4000)
    public void testNormaliseMultipleHeadAndBodyStructure() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");

        Element head1 = html.appendElement("head");
        head1.appendElement("meta").attr("charset", "utf-8");

        Element head2 = html.appendElement("head");
        head2.appendElement("title").text("Dup Title");

        Element body1 = html.appendElement("body");
        body1.appendElement("p").text("Paragraph 1");

        Element body2 = html.appendElement("body");
        body2.appendElement("p").text("Paragraph 2");

        doc.normalise();

        assertEquals(1, doc.getElementsByTag("head").size());
        assertEquals(1, doc.getElementsByTag("body").size());
        assertEquals(1, doc.head().getElementsByTag("meta").size());
        assertEquals(1, doc.head().getElementsByTag("title").size());
        assertEquals(2, doc.body().getElementsByTag("p").size());
    }

    @Test(timeout = 4000)
    public void testNormaliseReparentingOrphanHeadAndBody() {
        Document doc = new Document("http://example.com/");
        Element div = doc.appendElement("div");
        Element misplacedHead = div.appendElement("head");
        misplacedHead.appendElement("title").text("Misplaced");
        doc.appendElement("html");

        doc.normalise();

        Element html = doc.select("html").first();
        assertEquals(html, doc.head().parent());
        assertEquals(html, doc.body().parent());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
    // =========================================================================

    /**
     * Targets the defect where Document#title() fails to normalize inner whitespace,
     * such as newlines and multiple spaces, leading to:
     * junit.framework.AssertionFailedError: expected:<Hello[ there] now> but was:<Hello[...
     */
    @Test(timeout = 4000)
    public void testTitlesDefectInnerWhitespaceNormalization() {
        Document doc = Document.createShell("http://example.com/");
        Element titleEl = doc.head().appendElement("title");
        // Appending text with newlines and uneven spaces
        titleEl.text("Hello\n there \nnow");

        // The expected value must have whitespace collapsed and normalized to single spaces
        assertEquals("Hello there now", doc.title());
    }

    @Test(timeout = 4000)
    public void testTitleEmptyAndTrimmedWhitespace() {
        Document doc = Document.createShell("http://example.com/");
        Element titleEl = doc.head().appendElement("title");
        titleEl.text("   ");

        assertEquals("", doc.title());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateShellNullUriThrowsException() {
        Document.createShell(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTitleSetNullThrowsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testOutputSettingsSetNullThrowsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.outputSettings(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testOutputSettingsNegativeIndentThrowsException() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(-1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDocumentCloneDeepCopy() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Original Title");
        doc.outputSettings().indentAmount(4);

        Document clone = doc.clone();

        assertNotSame("Clone must be a distinct object", doc, clone);
        assertNotSame("OutputSettings must be deep copied", doc.outputSettings(), clone.outputSettings());
        assertEquals("Original Title", clone.title());
        assertEquals(4, clone.outputSettings().indentAmount());

        // Modify clone, verify original remains unaffected
        clone.title("Modified Clone Title");
        clone.outputSettings().indentAmount(8);

        assertEquals("Original Title", doc.title());
        assertEquals("Modified Clone Title", clone.title());
        assertEquals(4, doc.outputSettings().indentAmount());
        assertEquals(8, clone.outputSettings().indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsCloneIntegrity() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(3);
        settings.prettyPrint(false);
        settings.escapeMode(Entities.EscapeMode.extended);
        settings.charset("ISO-8859-1");

        Document.OutputSettings cloned = settings.clone();

        assertNotSame(settings, cloned);
        assertNotSame(settings.encoder(), cloned.encoder());
        assertEquals(settings.indentAmount(), cloned.indentAmount());
        assertEquals(settings.prettyPrint(), cloned.prettyPrint());
        assertEquals(settings.escapeMode(), cloned.escapeMode());
        assertEquals(settings.charset(), cloned.charset());

        cloned.indentAmount(6);
        assertEquals(3, settings.indentAmount());
        assertEquals(6, cloned.indentAmount());
    }
}