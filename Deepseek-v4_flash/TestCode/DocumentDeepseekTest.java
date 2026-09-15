package org.jsoup.nodes;

import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.Tag;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for Document class targeting maximum coverage and known defect detection.
 */
public class DocumentDeepseekTest {

    /**
     * @target createShell(String baseUri)
     * @scenario Valid base URI provided
     * @defectRisk NullPointerException if baseUri is null
     */
    @Test(timeout = 4000)
    public void testCreateShell_ValidBaseUri() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.child(0).nodeName());
        assertEquals(2, doc.child(0).childNodes().size());
    }

    /**
     * @target createShell(String baseUri)
     * @scenario Null base URI provided
     * @defectRisk Missing null validation causing NullPointerException
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateShell_NullBaseUri() {
        Document.createShell(null);
    }

    /**
     * @target head()
     * @scenario Document with head element
     * @defectRisk Returns null when head exists
     */
    @Test(timeout = 4000)
    public void testHead_WithHeadElement() {
        Document doc = Document.createShell("http://example.com");
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.nodeName());
    }

    /**
     * @target head()
     * @scenario Document without head element
     * @defectRisk Returns non-null element when head doesn't exist
     */
    @Test(timeout = 4000)
    public void testHead_WithoutHeadElement() {
        Document doc = new Document("http://example.com");
        Element head = doc.head();
        assertNull(head);
    }

    /**
     * @target body()
     * @scenario Document with body element
     * @defectRisk Returns null when body exists
     */
    @Test(timeout = 4000)
    public void testBody_WithBodyElement() {
        Document doc = Document.createShell("http://example.com");
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.nodeName());
    }

    /**
     * @target body()
     * @scenario Document without body element
     * @defectRisk Returns non-null element when body doesn't exist
     */
    @Test(timeout = 4000)
    public void testBody_WithoutBodyElement() {
        Document doc = new Document("http://example.com");
        Element body = doc.body();
        assertNull(body);
    }

    /**
     * @target title()
     * @scenario Document with title element containing text
     * @defectRisk Returns incorrect text or includes whitespace
     */
    @Test(timeout = 4000)
    public void testTitle_WithTitleElement() {
        Document doc = Document.createShell("http://example.com");
        doc.head().appendElement("title").text("  My Title  ");
        assertEquals("My Title", doc.title());
    }

    /**
     * @target title()
     * @scenario Document without title element
     * @defectRisk Returns non-empty string when no title exists
     */
    @Test(timeout = 4000)
    public void testTitle_WithoutTitleElement() {
        Document doc = Document.createShell("http://example.com");
        assertEquals("", doc.title());
    }

    /**
     * @target title(String)
     * @scenario Update existing title element
     * @defectRisk Title not updated correctly
     */
    @Test(timeout = 4000)
    public void testTitle_UpdateExistingTitle() {
        Document doc = Document.createShell("http://example.com");
        doc.head().appendElement("title").text("Old Title");
        doc.title("New Title");
        assertEquals("New Title", doc.title());
    }

    /**
     * @target title(String)
     * @scenario Add title to head when missing
     * @defectRisk Title not added to head or NullPointerException
     */
    @Test(timeout = 4000)
    public void testTitle_AddTitleWhenMissing() {
        Document doc = Document.createShell("http://example.com");
        // Remove existing title if any
        Element titleEl = doc.head().select("title").first();
        if (titleEl != null) titleEl.remove();
        
        doc.title("Added Title");
        assertEquals("Added Title", doc.title());
        assertNotNull(doc.head().select("title").first());
    }

    /**
     * @target title(String)
     * @scenario Null title provided
     * @defectRisk Missing null validation causing NullPointerException
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTitle_NullTitle() {
        Document doc = Document.createShell("http://example.com");
        doc.title(null);
    }

    /**
     * @target createElement(String)
     * @scenario Valid tag name provided
     * @defectRisk Element not created with document's base URI
     */
    @Test(timeout = 4000)
    public void testCreateElement_ValidTagName() {
        Document doc = new Document("http://example.com");
        Element div = doc.createElement("div");
        assertNotNull(div);
        assertEquals("div", div.nodeName());
        assertEquals("http://example.com", div.baseUri());
    }

    /**
     * @target createElement(String)
     * @scenario Invalid tag name provided
     * @defectRisk Exception not thrown for invalid tag
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateElement_InvalidTagName() {
        Document doc = new Document("http://example.com");
        doc.createElement(null);
    }

    /**
     * @target normalise()
     * @scenario Document missing html element
     * @defectRisk html element not created
     */
    @Test(timeout = 4000)
    public void testNormalise_MissingHtml() {
        Document doc = new Document("http://example.com");
        doc.normalise();
        assertNotNull(doc.select("html").first());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    /**
     * @target normalise()
     * @scenario Document missing head element
     * @defectRisk head element not created
     */
    @Test(timeout = 4000)
    public void testNormalise_MissingHead() {
        Document doc = Document.createShell("http://example.com");
        doc.head().remove();
        doc.normalise();
        assertNotNull(doc.head());
    }

    /**
     * @target normalise()
     * @scenario Document missing body element
     * @defectRisk body element not created
     */
    @Test(timeout = 4000)
    public void testNormalise_MissingBody() {
        Document doc = Document.createShell("http://example.com");
        doc.body().remove();
        doc.normalise();
        assertNotNull(doc.body());
    }

    /**
     * @target normalise()
     * @scenario Text nodes outside body are moved into body
     * @defectRisk Text nodes not moved or order incorrect
     */
    @Test(timeout = 4000)
    public void testNormalise_TextNodesMovedToBody() {
        Document doc = new Document("http://example.com");
        Element html = doc.appendElement("html");
        html.appendChild(new TextNode("Outside text", ""));
        html.appendElement("head");
        html.appendElement("body");
        
        doc.normalise();
        Element body = doc.body();
        assertTrue(body.text().contains("Outside text"));
    }

    /**
     * @target normalise()
     * @scenario Multiple text nodes in root are moved preserving order
     * @defectRisk Text order not preserved
     */
    @Test(timeout = 4000)
    public void testNormalise_MultipleTextNodesOrder() {
        Document doc = new Document("http://example.com");
        doc.appendChild(new TextNode("First ", ""));
        doc.appendChild(new TextNode("Second ", ""));
        doc.appendChild(new TextNode("Third", ""));
        
        doc.normalise();
        assertEquals("First Second Third", doc.body().text().trim());
    }

    /**
     * @target outerHtml()
     * @scenario Normal document
     * @defectRisk Returns incorrect HTML structure
     */
    @Test(timeout = 4000)
    public void testOuterHtml() {
        Document doc = Document.createShell("http://example.com");
        String html = doc.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
    }

    /**
     * @target text(String)
     * @scenario Set body text
     * @defectRisk Body text not set or document structure destroyed
     */
    @Test(timeout = 4000)
    public void testText_SetBodyText() {
        Document doc = Document.createShell("http://example.com");
        doc.text("Hello World");
        assertEquals("Hello World", doc.body().text());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    /**
     * @target text(String)
     * @scenario Set body text multiple times
     * @defectRisk Previous text not cleared
     */
    @Test(timeout = 4000)
    public void testText_SetBodyTextMultipleTimes() {
        Document doc = Document.createShell("http://example.com");
        doc.text("First");
        doc.text("Second");
        assertEquals("Second", doc.body().text());
    }

    /**
     * @target nodeName()
     * @scenario Any document
     * @defectRisk Returns incorrect node name
     */
    @Test(timeout = 4000)
    public void testNodeName() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
    }

    /**
     * @target normalise() and text ordering
     * @scenario HTML snippet with text before block element
     * @defectRisk Text order not preserved (known defect: returns "bar baz foo" instead of "foo bar baz")
     */
    @Test(timeout = 4000)
    public void testCreatesStructureFromBodySnippet_TextOrder_Issue23() {
        String html = "foo <p>bar</p> baz";
        Document doc = Jsoup.parse(html);
        assertEquals("foo bar baz", doc.text());
    }

    /**
     * @target normalise() with direct text in root
     * @scenario Text added directly to document root before normalization
     * @defectRisk Leading text not preserved at beginning of body
     */
    @Test(timeout = 4000)
    public void testNormalise_LeadingTextPreserved() {
        Document doc = new Document("http://example.com");
        doc.appendChild(new TextNode("Leading text ", ""));
        Element html = doc.appendElement("html");
        html.appendElement("head");
        html.appendElement("body");
        
        doc.normalise();
        String bodyText = doc.body().text();
        assertTrue("Leading text should be at the beginning of body", 
                   bodyText.startsWith("Leading text"));
    }

    /**
     * @target normalise() with text in head
     * @scenario Text node present in head element
     * @defectRisk Text not moved from head to body
     */
    @Test(timeout = 4000)
    public void testNormalise_TextInHeadMovedToBody() {
        Document doc = Document.createShell("http://example.com");
        doc.head().appendChild(new TextNode("Head text", ""));
        
        doc.normalise();
        assertFalse("Head should not contain text after normalization", 
                    doc.head().text().contains("Head text"));
        assertTrue("Body should contain the text moved from head", 
                   doc.body().text().contains("Head text"));
    }

    /**
     * @target normalise() with blank text nodes
     * @scenario Blank text nodes in root/html/head
     * @defectRisk Blank text nodes incorrectly moved to body
     */
    @Test(timeout = 4000)
    public void testNormalise_BlankTextNodesNotMoved() {
        Document doc = new Document("http://example.com");
        doc.appendChild(new TextNode("   ", ""));
        Element html = doc.appendElement("html");
        html.appendElement("head");
        html.appendElement("body");
        
        doc.normalise();
        assertEquals("Body should be empty after normalization with only blank text", 
                     "", doc.body().text().trim());
    }

    /**
     * @target normalise() on already normalized document
     * @scenario Document already has html, head, body and no text outside body
     * @defectRisk Unnecessary modifications to document structure
     */
    @Test(timeout = 4000)
    public void testNormalise_AlreadyNormalized() {
        Document doc = Document.createShell("http://example.com");
        doc.body().text("Content");
        
        Document result = doc.normalise();
        assertSame("normalise should return this document", doc, result);
        assertEquals("Content", doc.body().text());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    /**
     * @target createShell with subsequent modifications
     * @scenario Create shell then add elements to body
     * @defectRisk Shell structure corrupted after modifications
     */
    @Test(timeout = 4000)
    public void testCreateShell_WithModifications() {
        Document doc = Document.createShell("http://example.com");
        doc.body().appendElement("div").text("Test");
        assertEquals("Test", doc.body().text());
        assertNotNull(doc.head());
        assertEquals(2, doc.child(0).childNodes().size()); // head and body
    }
}