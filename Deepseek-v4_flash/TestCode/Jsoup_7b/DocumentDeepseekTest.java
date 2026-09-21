package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Deep structural and boundary test suite for Document.
 * Targets core functionality, edge cases, and the known defect regarding
 * normalisation of misplaced &lt;body&gt; elements inside &lt;noscript&gt;.
 *
 * Partition summary:
 * A: Normal document creation, head/body access, title handling
 * B: Boundary values (null title, empty baseUri, negative indent)
 * C: Defect-targeted branch: multiple body inside noscript → normalise
 * D: Exception/guard paths (null title, illegal indent)
 * E: Object lifecycle: outerHtml, nodeName, text() override
 * F: OutputSettings all methods and chaining
 */
public class DocumentDeepseekTest {

    @Test(timeout = 4000)
    public void testCreateShell_createsHtmlHeadBody() {
        Document doc = Document.createShell("http://example.com");
        assertEquals("html", doc.child(0).tagName());
        assertEquals("head", doc.child(0).child(0).tagName());
        assertEquals("body", doc.child(0).child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testHeadAndBody_returnCorrectElements() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc.head());
        assertEquals("head", doc.head().tagName());
        assertNotNull(doc.body());
        assertEquals("body", doc.body().tagName());
    }

    @Test(timeout = 4000)
    public void testTitle_getEmptyWhenNoTitle() {
        Document doc = new Document("http://example.com");
        assertEquals("", doc.title());
    }

    @Test(timeout = 4000)
    public void testTitle_setAddsToHead() {
        Document doc = Document.createShell("http://example.com");
        doc.title("My Title");
        assertEquals("My Title", doc.title());
        // verify element exists in head
        Element titleEl = doc.head().getElementsByTag("title").first();
        assertNotNull(titleEl);
        assertEquals("My Title", titleEl.text());
    }

    @Test(timeout = 4000)
    public void testTitle_setThrowsOnNull() {
        Document doc = new Document("http://example.com");
        try {
            doc.title((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateElement_inheritsBaseUri() {
        Document doc = new Document("http://example.com");
        Element newEl = doc.createElement("div");
        assertEquals("div", newEl.tagName());
        assertEquals("http://example.com", newEl.baseUri());
    }

    @Test(timeout = 4000)
    public void testNormalise_addsMissingHtmlHeadBody() {
        // start with completely empty document
        Document doc = new Document("http://example.com");
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        // there should be an html element (root child)
        assertEquals("html", doc.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testNormalise_movesTextNodesFromHeadToBody() {
        Document doc = Document.createShell("http://example.com");
        // insert a non-blank text node directly into head (simulating leftover)
        Node textNode = new TextNode("some text", "");
        doc.head().appendChild(textNode);
        doc.normalise();
        // text should now be in body prepended with space
        assertTrue(doc.body().text().contains("some text"));
        // ensure head no longer contains the text
        assertFalse(doc.head().text().contains("some text"));
    }

    @Test(timeout = 4000)
    public void testNormalise_movesTextNodesFromRootToBody() {
        Document doc = new Document("http://example.com");
        Element root = doc;  // the document itself is the root
        Node textNode = new TextNode("root text", "");
        root.appendChild(textNode);
        // add html element to have a body later
        root.appendChild(new Element(Tag.valueOf("html"), ""));
        Element html = (Element) doc.childNodes().get(1); // index 0 is the textNode we just added?
        // Actually we appended textNode first, then html. So order: textNode, html
        html.appendChild(new Element(Tag.valueOf("head"), ""));
        html.appendChild(new Element(Tag.valueOf("body"), ""));

        doc.normalise();
        // text should move to body
        assertFalse(doc.text().contains("root text")); // root now contains no text directly
        assertTrue(doc.body().text().contains("root text"));
    }

    @Test(timeout = 4000)
    public void testNormalise_misplacedBodyInNoscript() {
        // Replicate the known defect scenario:
        // document with <html><head><script></script><noscript><body><p>two</p><body><p>three</p></body></body></noscript></head></html>
        // After normalise, should become: <html><head><script></script><noscript></noscript></head><body><p>two</p><p>three</p></body></html>

        // Build the malformed document programmatically
        Document doc = new Document("http://example.com");
        Element html = new Element(Tag.valueOf("html"), "");
        doc.appendChild(html);
        Element head = new Element(Tag.valueOf("head"), "");
        html.appendChild(head);
        Element script = new Element(Tag.valueOf("script"), "");
        head.appendChild(script);
        Element noscript = new Element(Tag.valueOf("noscript"), "");
        head.appendChild(noscript);

        // Two body elements inside noscript, each containing a paragraph
        Element body1 = new Element(Tag.valueOf("body"), "");
        Element p1 = new Element(Tag.valueOf("p"), "");
        p1.text("two");
        body1.appendChild(p1);
        Element body2 = new Element(Tag.valueOf("body"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        p2.text("three");
        body2.appendChild(p2);

        noscript.appendChild(body1);
        noscript.appendChild(body2);

        // Ensure default output settings (pretty print) don't interfere
        doc.outputSettings().prettyPrint(false);

        doc.normalise();

        String expected = "<html><head><script></script><noscript></noscript></head><body><p>two</p><p>three</p></body></html>";
        assertEquals("Normalisation should merge misplaced body contents into the one body element",
                expected, doc.outerHtml());
    }

    @Test(timeout = 4000)
    public void testOuterHtml_returnsInnerHtml() {
        Document doc = Document.createShell("http://example.com");
        // outerHtml for Document returns super.html() – which is innerHTML of the root (#document)
        // Since root has one child <html>, the innerHTML of root is the markup of <html> and its children.
        String outer = doc.outerHtml();
        assertTrue(outer.startsWith("<html>"));
        assertTrue(outer.endsWith("</html>"));
    }

    @Test(timeout = 4000)
    public void testText_overridesBodyAndClears() {
        Document doc = Document.createShell("http://example.com");
        // add some content to body
        doc.body().appendElement("p").text("old");
        // call text("new") – should clear body and set its text
        doc.text("new");
        assertEquals("new", doc.body().text());
        // ensure the paragraph is gone
        assertEquals(0, doc.body().childrenSize());
        // ensure document structure intact (head still exists)
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testNodeName_isDocument() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
    }

    // OutputSettings tests

    @Test(timeout = 4000)
    public void testOutputSettings_defaults() {
        Document doc = new Document("http://example.com");
        assertEquals(Entities.EscapeMode.base, doc.outputSettings().escapeMode());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertTrue(doc.outputSettings().prettyPrint());
        assertEquals(1, doc.outputSettings().indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettings_escapeMode_chaining() {
        Document doc = new Document("http://example.com");
        OutputSettings settings = doc.outputSettings().escapeMode(Entities.EscapeMode.extended);
        assertSame(settings, doc.outputSettings()); // chaining
        assertEquals(Entities.EscapeMode.extended, doc.outputSettings().escapeMode());
    }

    @Test(timeout = 4000)
    public void testOutputSettings_charset() {
        Document doc = new Document("http://example.com");
        assertSame(doc.outputSettings().charset(doc.outputSettings().charset()), doc.outputSettings()); // chaining
    }

    @Test(timeout = 4000)
    public void testOutputSettings_charsetByName() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().charset("ISO-8859-1");
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(timeout = 4000)
    public void testOutputSettings_encoder() {
        Document doc = new Document("http://example.com");
        assertNotNull(doc.outputSettings().encoder());
        // encoder should match charset
        assertEquals(doc.outputSettings().charset().newEncoder().charset(), doc.outputSettings().encoder().charset());
    }

    @Test(timeout = 4000)
    public void testOutputSettings_prettyPrint_chaining() {
        Document doc = new Document("http://example.com");
        assertSame(doc.outputSettings(), doc.outputSettings().prettyPrint(false));
        assertFalse(doc.outputSettings().prettyPrint());
    }

    @Test(timeout = 4000)
    public void testOutputSettings_indentAmount_valid() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().indentAmount(4);
        assertEquals(4, doc.outputSettings().indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettings_indentAmount_boundaryZero() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().indentAmount(0);
        assertEquals(0, doc.outputSettings().indentAmount());
    }

    @Test(timeout = 4000)
    public void testOutputSettings_indentAmount_negativeThrows() {
        Document doc = new Document("http://example.com");
        try {
            doc.outputSettings().indentAmount(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testOutputSettings_chainingMultiple() {
        Document doc = new Document("http://example.com");
        doc.outputSettings()
            .escapeMode(Entities.EscapeMode.extended)
            .charset("ISO-8859-1")
            .prettyPrint(false)
            .indentAmount(2);
        assertEquals(Entities.EscapeMode.extended, doc.outputSettings().escapeMode());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertFalse(doc.outputSettings().prettyPrint());
        assertEquals(2, doc.outputSettings().indentAmount());
    }

    // Also test that normalise with existing body works and doesn't duplicate
    @Test(timeout = 4000)
    public void testNormalise_alreadyNormalDocument() {
        Document doc = Document.createShell("http://example.com");
        String before = doc.html();
        doc.normalise();
        assertEquals("Normalising a well-formed document should not change it", before, doc.html());
    }
}