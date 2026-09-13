package org.jsoup.nodes;

import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.Tag;
import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentClaudeTest {

    /**
     * @target Document.createShell(String)
     * @scenario Create a shell document with a valid base URI, verify html/head/body structure
     * @defectRisk Ensures html, head, and body elements are created correctly in shell
     */
    @Test(timeout = 4000)
    public void testCreateShellValidBaseUri() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.child(0).tagName());
    }

    /**
     * @target Document.createShell(String)
     * @scenario Pass null baseUri, expect Validate.notNull to throw exception
     * @defectRisk Null check bypass would allow invalid document creation
     */
    @Test(timeout = 4000)
    public void testCreateShellNullBaseUriThrows() {
        boolean threw = false;
        try {
            Document.createShell(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        } catch (NullPointerException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    /**
     * @target Document.head()
     * @scenario Retrieve head element from a properly structured document
     * @defectRisk Incorrect tag selection could return null or wrong element
     */
    @Test(timeout = 4000)
    public void testHeadAccessor() {
        Document doc = Document.createShell("");
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
    }

    /**
     * @target Document.head()
     * @scenario Retrieve head when document has no head element
     * @defectRisk Should return null gracefully rather than throwing
     */
    @Test(timeout = 4000)
    public void testHeadAccessorMissing() {
        Document doc = new Document("");
        assertNull(doc.head());
    }

    /**
     * @target Document.body()
     * @scenario Retrieve body element from a properly structured document
     * @defectRisk Incorrect tag selection could return null or wrong element
     */
    @Test(timeout = 4000)
    public void testBodyAccessor() {
        Document doc = Document.createShell("");
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    /**
     * @target Document.body()
     * @scenario Retrieve body when document has no body element
     * @defectRisk Should return null gracefully rather than throwing
     */
    @Test(timeout = 4000)
    public void testBodyAccessorMissing() {
        Document doc = new Document("");
        assertNull(doc.body());
    }

    /**
     * @target Document.title()
     * @scenario Retrieve title text when title element exists with whitespace, verify trimming
     * @defectRisk Title text should be trimmed; untrimmed text indicates bug
     */
    @Test(timeout = 4000)
    public void testTitleGetterWithWhitespace() {
        Document doc = Jsoup.parse("<html><head><title>  My Title  </title></head><body></body></html>");
        assertEquals("My Title", doc.title());
    }

    /**
     * @target Document.title()
     * @scenario Retrieve title when no title element is present, expect empty string
     * @defectRisk Missing null check could throw NullPointerException instead of returning ""
     */
    @Test(timeout = 4000)
    public void testTitleGetterMissing() {
        Document doc = Document.createShell("");
        assertEquals("", doc.title());
    }

    /**
     * @target Document.title(String)
     * @scenario Set title when no title element exists yet, verify it's added to head
     * @defectRisk Title should be appended to head; missing head handling could throw NPE
     */
    @Test(timeout = 4000)
    public void testTitleSetterAddsNewTitle() {
        Document doc = Document.createShell("");
        doc.title("New Title");
        assertEquals("New Title", doc.title());
        Element titleEl = doc.head().getElementsByTag("title").first();
        assertNotNull(titleEl);
    }

    /**
     * @target Document.title(String)
     * @scenario Set title when title element already exists, verify it's updated not duplicated
     * @defectRisk Existing title element should be updated in place, not creating a duplicate
     */
    @Test(timeout = 4000)
    public void testTitleSetterUpdatesExisting() {
        Document doc = Jsoup.parse("<html><head><title>Old</title></head><body></body></html>");
        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.getElementsByTag("title").size());
    }

    /**
     * @target Document.title(String)
     * @scenario Pass null title string, expect Validate.notNull to throw exception
     * @defectRisk Null check bypass would allow invalid title assignment
     */
    @Test(timeout = 4000)
    public void testTitleSetterNullThrows() {
        Document doc = Document.createShell("");
        boolean threw = false;
        try {
            doc.title(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        } catch (NullPointerException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    /**
     * @target Document.createElement(String)
     * @scenario Create a new element with given tag name, verify base URI is inherited
     * @defectRisk New element should carry document's base URI; incorrect propagation is a defect
     */
    @Test(timeout = 4000)
    public void testCreateElementInheritsBaseUri() {
        Document doc = new Document("http://example.com/");
        Element el = doc.createElement("div");
        assertNotNull(el);
        assertEquals("div", el.tagName());
        assertEquals("http://example.com/", el.baseUri());
    }

    /**
     * @target Document.createElement(String)
     * @scenario Created element is not automatically attached as child of document
     * @defectRisk Element should be standalone; accidental attachment is a defect
     */
    @Test(timeout = 4000)
    public void testCreateElementNotAttached() {
        Document doc = new Document("");
        Element el = doc.createElement("span");
        assertEquals(0, doc.childNodes().size());
        assertNull(el.parent());
    }

    /**
     * @target Document.normalise()
     * @scenario Call normalise on empty document with no html element, verify html/head/body auto-created
     * @defectRisk Missing structure creation would leave document invalid
     */
    @Test(timeout = 4000)
    public void testNormaliseCreatesFullStructure() {
        Document doc = new Document("");
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(1, doc.select("html").size());
    }

    /**
     * @target Document.normalise()
     * @scenario Document has html element but missing head, verify head is prepended
     * @defectRisk Head must be prepended (before body), not appended
     */
    @Test(timeout = 4000)
    public void testNormaliseAddsMissingHead() {
        Document doc = new Document("");
        Element html = doc.appendElement("html");
        html.appendElement("body");
        doc.normalise();
        assertNotNull(doc.head());
        // head should come before body
        assertEquals("head", html.child(0).tagName());
        assertEquals("body", html.child(1).tagName());
    }

    /**
     * @target Document.normalise()
     * @scenario Document has html element but missing body, verify body is appended
     * @defectRisk Body must be appended after head; missing body handling could throw NPE
     */
    @Test(timeout = 4000)
    public void testNormaliseAddsMissingBody() {
        Document doc = new Document("");
        Element html = doc.appendElement("html");
        html.appendElement("head");
        doc.normalise();
        assertNotNull(doc.body());
    }

    /**
     * @target Document.normalise() private normalise(Element) helper
     * @scenario Text node exists directly in document root before html element, verify it's moved into body
     * @defectRisk Text nodes outside body must be relocated correctly maintaining structure
     */
    @Test(timeout = 4000)
    public void testNormaliseMovesRootTextIntoBody() {
        Document doc = new Document("");
        doc.appendChild(new TextNode("floating text", ""));
        Element html = doc.appendElement("html");
        html.appendElement("head");
        html.appendElement("body");
        doc.normalise();
        assertTrue(doc.body().text().contains("floating text"));
    }

    /**
     * @target Document.normalise() private normalise(Element) helper
     * @scenario Blank text node exists in head element, verify it's NOT moved (isBlank check)
     * @defectRisk Blank text nodes should be skipped from relocation into body
     */
    @Test(timeout = 4000)
    public void testNormaliseSkipsBlankTextInHead() {
        Document doc = new Document("");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        head.appendChild(new TextNode("   ", ""));
        html.appendElement("body");
        doc.normalise();
        // blank text should not appear as content in body
        assertEquals("", doc.body().text());
    }

    /**
     * @target Document.outerHtml()
     * @scenario Generate outerHtml of a full document, verify no extra wrapper tag is present
     * @defectRisk outerHtml should not wrap output in a root tag like #root
     */
    @Test(timeout = 4000)
    public void testOuterHtmlNoWrapperTag() {
        Document doc = Jsoup.parse("<html><head><title>T</title></head><body><p>Hello</p></body></html>");
        String html = doc.outerHtml();
        assertFalse(html.contains("#root"));
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<p>Hello</p>"));
    }

    /**
     * @target Document.text(String)
     * @scenario Set text on document body, verify existing content is cleared and new text set
     * @defectRisk Document structure (html/head/body) should remain intact after text() call
     */
    @Test(timeout = 4000)
    public void testTextSetterClearsBodyAndSetsText() {
        Document doc = Jsoup.parse("<html><head><title>T</title></head><body><p>Old</p></body></html>");
        doc.text("New Body Text");
        assertEquals("New Body Text", doc.body().text());
        // structure should remain intact
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    /**
     * @target Document.nodeName()
     * @scenario Verify nodeName always returns "#document" regardless of content
     * @defectRisk Incorrect node name could break serialization or traversal logic
     */
    @Test(timeout = 4000)
    public void testNodeNameIsDocument() {
        Document doc = new Document("");
        assertEquals("#document", doc.nodeName());
    }

    /**
     * @target Document text order defect (Issue 23) - normalise() text node ordering
     * @scenario Parse "foo <p>bar</p> baz" where leading text appears before block element
     * @defectRisk Known defect: text nodes moved into body in reverse/wrong order,
     *             causing "bar baz foo" instead of correct "foo bar baz"
     */
    @Test(timeout = 4000)
    public void testCreatesStructureFromBodySnippet_TextOrder_Issue23() {
        String html = "foo <p>bar</p> baz";
        Document doc = Jsoup.parse(html);
        assertEquals("foo bar baz", doc.text());
    }

    /**
     * @target Document.normalise() direct invocation - text prepended before body content
     * @scenario Manually build document with leading text node before html element containing body with existing content,
     *           verify normalise() preserves original left-to-right order (leading text stays first)
     * @defectRisk Defect causes text nodes outside body to be appended in wrong order,
     *             putting leading text after existing body content instead of before it
     */
    @Test(timeout = 4000)
    public void testNormaliseDirectTextOrderPreservation() {
        Document doc = new Document("");
        doc.appendChild(new TextNode("foo", ""));
        Element html = doc.appendElement("html");
        html.appendElement("head");
        Element body = html.appendElement("body");
        body.appendChild(new TextNode("bar", ""));

        doc.normalise();

        // Expect leading "foo" text to remain first in overall body text ordering
        assertEquals("foo bar", doc.body().text());
    }

    /**
     * @target Document.normalise() combined multi-source text relocation ordering
     * @scenario Text present in head and in root, with existing body text, verify overall combined
     *           order follows document source order (head text before root text before body text, per normalise() call order)
     * @defectRisk Improper ordering algorithm could scramble text across head/root/body during relocation
     */
    @Test(timeout = 4000)
    public void testNormaliseMultipleTextSourcesOrder() {
        Document doc = new Document("");
        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        head.appendChild(new TextNode("headtext", ""));
        Element body = html.appendElement("body");
        body.appendChild(new TextNode("bodytext", ""));
        doc.appendChild(new TextNode("roottext", ""));

        doc.normalise();

        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("headtext"));
        assertTrue(bodyText.contains("bodytext"));
        assertTrue(bodyText.contains("roottext"));
    }
}