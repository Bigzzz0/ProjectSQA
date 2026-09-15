package org.jsoup.nodes;

import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.Tag;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.nodes.Document
 *
 * 1. Construction & Structural Shell (Partition A):
 *    - Document(baseUri): Instantiation with "#root" tag, empty child list, valid baseUri.
 *    - createShell(baseUri): Validation with null argument (throws IllegalArgumentException).
 *    - createShell(baseUri): Normal path ensuring <html>, <head>, and <body> nodes are formed.
 *    - createElement(tagName): Creates isolated element retaining baseUri, unattached to root.
 *    - nodeName(): Exact return value "#document".
 *
 * 2. Document Title Access & Mutation (Partition B):
 *    - title(): titleEl is null -> returns empty string "".
 *    - title(): titleEl exists with untrimmed whitespace -> returns trimmed string.
 *    - title(null): Triggers Validate.notNull precondition check -> IllegalArgumentException.
 *    - title(newTitle): titleEl is null -> invokes head().appendElement("title").text(newTitle).
 *    - title(newTitle): titleEl exists -> invokes titleEl.text(newTitle) in-place.
 *
 * 3. Defect-Targeted Ordering Zone - Jsoup-1 / Issue 23 (Partition C):
 *    - HTML snippet: "foo <p>bar</p> baz". Parser / normaliser handles text before and after block.
 *    - Text nodes outside <body> must be prepended/ordered such that structural order
 *      "foo bar baz" is strictly preserved instead of buggy reordering ("bar baz foo").
 *    - Direct normalisation invocation moving root/html-level text nodes into body without order inversion.
 *
 * 4. Document Normalisation Transitions (Partition D):
 *    - normalise(): Missing <html> branch -> creates <html>.
 *    - normalise(): Missing <head> branch -> prepends <head> inside <html>.
 *    - normalise(): Missing <body> branch -> appends <body> inside <html>.
 *    - normalise(Element): Iterates over childNodes; filters non-TextNode, filters blank TextNodes,
 *      moves non-blank TextNodes from head/html/root into body with space separator.
 *
 * 5. Output & Mutation Semantics (Partition E):
 *    - outerHtml(): Overridden to invoke super.html() directly, omitting root wrapper tags.
 *    - text(String): Sets body text, preserves doc shell (head/body remain intact), returns this.
 * --------------------------------------------------------------------------------------------------
 */
public class DocumentGeminiTest {

    // =========================================================================
    // Partition A: Document Construction & Shell Structure
    // =========================================================================

    @Test(timeout = 4000)
    public void testDocumentConstructorAndNodeName() {
        String baseUri = "http://example.com/test/";
        Document doc = new Document(baseUri);

        assertEquals("#document", doc.nodeName());
        assertEquals(baseUri, doc.baseUri());
        assertEquals(0, doc.childNodes.size());
        assertNull(doc.head());
        assertNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testCreateShellStructure() {
        String baseUri = "http://example.com/";
        Document doc = Document.createShell(baseUri);

        assertNotNull(doc);
        assertEquals(baseUri, doc.baseUri());

        Element html = doc.childNode(0) instanceof Element ? (Element) doc.childNode(0) : null;
        assertNotNull(html);
        assertEquals("html", html.tagName());

        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
        assertSame(html, head.parent());

        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
        assertSame(html, body.parent());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateShellWithNullBaseUriThrowsException() {
        Document.createShell(null);
    }

    @Test(timeout = 4000)
    public void testCreateElementPreservesBaseUriAndDetachedState() {
        Document doc = new Document("http://example.com/dir/");
        Element span = doc.createElement("span");

        assertNotNull(span);
        assertEquals("span", span.tagName());
        assertEquals("http://example.com/dir/", span.baseUri());
        assertNull(span.parent());
        assertEquals(0, doc.childNodes.size());
    }

    // =========================================================================
    // Partition B: Document Title Management
    // =========================================================================

    @Test(timeout = 4000)
    public void testTitleWhenNoTitleElementExists() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());
    }

    @Test(timeout = 4000)
    public void testTitleTrimsWhitespace() {
        Document doc = Document.createShell("http://example.com/");
        doc.head().appendElement("title").text("   Document Title with Spaces   ");

        assertEquals("Document Title with Spaces", doc.title());
    }

    @Test(timeout = 4000)
    public void testSetTitleWhenTitleElementAlreadyExists() {
        Document doc = Document.createShell("http://example.com/");
        Element titleEl = doc.head().appendElement("title");
        titleEl.text("Original Title");

        doc.title("Updated Title");

        assertEquals("Updated Title", doc.title());
        assertEquals("Updated Title", titleEl.text());
        assertEquals(1, doc.getElementsByTag("title").size());
    }

    @Test(timeout = 4000)
    public void testSetTitleWhenNoTitleElementExistsAddsToHead() {
        Document doc = Document.createShell("http://example.com/");
        assertNull(doc.getElementsByTag("title").first());

        doc.title("Newly Created Title");

        Element titleEl = doc.head().getElementsByTag("title").first();
        assertNotNull(titleEl);
        assertEquals("Newly Created Title", titleEl.text());
        assertEquals("Newly Created Title", doc.title());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTitleWithNullThrowsException() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jsoup-1 / Issue 23)
    // =========================================================================

    /**
     * Exact defect reproduction for Jsoup-1:
     * When parsing body snippets where text precedes a block element (e.g. "foo <p>bar</p> baz"),
     * text outside the body must not be appended behind the body's existing contents ("bar baz foo"),
     * but must preserve the original sequential flow ("foo bar baz").
     */
    @Test(timeout = 4000)
    public void testCreatesStructureFromBodySnippet_TextOrder_Issue23() {
        String html = "foo <p>bar</p> baz";
        Document doc = Jsoup.parse(html);

        assertEquals("foo bar baz", doc.text());
    }

    @Test(timeout = 4000)
    public void testNormaliseTextOrderingWhenLeadingTextOutsideBody() {
        Document doc = new Document("http://example.com");
        // Manually assemble a document structure where text node is at html level before body
        Element html = doc.appendElement("html");
        html.appendElement("head");
        TextNode leadingText = new TextNode("leading", "");
        html.appendChild(leadingText);
        Element body = html.appendElement("body");
        body.appendElement("p").text("content");

        doc.normalise();

        // Leading text must precede the body's initial content in doc.text()
        assertEquals("leading content", doc.text().trim());
    }

    // =========================================================================
    // Partition D: Document Normalisation Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testNormaliseCreatesMissingHtmlHeadAndBody() {
        Document doc = new Document("http://example.com");
        assertEquals(0, doc.childNodes.size());

        Document returnedDoc = doc.normalise();
        assertSame(doc, returnedDoc);

        assertNotNull(doc.select("html").first());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertSame(doc.select("html").first(), doc.head().parent());
        assertSame(doc.select("html").first(), doc.body().parent());
    }

    @Test(timeout = 4000)
    public void testNormaliseWithHtmlPresentMissingHeadAndBody() {
        Document doc = new Document("http://example.com");
        Element html = doc.appendElement("html");

        doc.normalise();

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertSame(html, doc.head().parent());
        assertSame(html, doc.body().parent());
    }

    @Test(timeout = 4000)
    public void testNormaliseIgnoresBlankTextNodesInAncestors() {
        Document doc = Document.createShell("http://example.com");
        // Insert purely whitespace text nodes in root, html, and head
        doc.appendChild(new TextNode("   \n\t  ", ""));
        doc.select("html").first().appendChild(new TextNode("   ", ""));
        doc.head().appendChild(new TextNode(" \t ", ""));

        doc.body().appendElement("p").text("Hello");

        doc.normalise();

        // Blank text nodes should not be moved as text content into body
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testNormaliseMovesNonBlankTextNodesFromHeadAndRootToBody() {
        Document doc = new Document("http://example.com");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");

        // Non-blank text in root and head
        doc.appendChild(new TextNode("RootText", ""));
        head.appendChild(new TextNode("HeadText", ""));
        body.appendElement("span").text("BodyText");

        doc.normalise();

        // Both HeadText and RootText should be relocated into body
        String bodyText = doc.body().text();
        assertTrue("Body should contain HeadText", bodyText.contains("HeadText"));
        assertTrue("Body should contain RootText", bodyText.contains("RootText"));
        assertTrue("Body should contain BodyText", bodyText.contains("BodyText"));
    }

    // =========================================================================
    // Partition E: Output & Text Mutation
    // =========================================================================

    @Test(timeout = 4000)
    public void testOuterHtmlDoesNotContainDocumentWrapperTag() {
        Document doc = Document.createShell("http://example.com");
        doc.body().appendElement("p").text("Sample text");

        String outer = doc.outerHtml();

        assertFalse(outer.contains("#root"));
        assertFalse(outer.contains("#document"));
        assertTrue(outer.contains("<html>"));
        assertTrue(outer.contains("<head>"));
        assertTrue(outer.contains("<body>"));
        assertTrue(outer.contains("<p>Sample text</p>"));
    }

    @Test(timeout = 4000)
    public void testDocumentTextMutatesBodyAndReturnsThis() {
        Document doc = Document.createShell("http://example.com");
        doc.body().appendElement("div").appendElement("span").text("Initial content");
        doc.head().appendElement("title").text("Keep Head Intact");

        Element returned = doc.text("Replaced entire body content");

        assertSame(doc, returned);
        assertEquals("Replaced entire body content", doc.body().text());
        assertEquals("Keep Head Intact", doc.title());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }
}