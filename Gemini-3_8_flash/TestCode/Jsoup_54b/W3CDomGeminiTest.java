package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringWriter;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.helper.W3CDom & W3CDom.W3CBuilder
 *
 * 1. Branch Coverage Targets:
 *   - fromJsoup(Document in):
 *       * in == null -> Validate.notNull throws IllegalArgumentException
 *       * in != null -> initializes namespace-aware factory, builds Document, calls convert
 *   - convert(Document in, Document out):
 *       * !StringUtil.isBlank(in.location()) -> true: out.setDocumentURI(in.location())
 *       * !StringUtil.isBlank(in.location()) -> false: branch skipped
 *   - W3CBuilder.head(Node source, int depth):
 *       * source instanceof Element:
 *           - dest == null (Root element setup via doc.appendChild)
 *           - dest != null (Child element setup via dest.appendChild)
 *           - Tag prefix extraction: el.tagName().indexOf(":") > 0 vs <= 0
 *           - Namespaces map lookup for prefix (found vs null)
 *       * source instanceof TextNode:
 *           - dest.appendChild(doc.createTextNode(...))
 *       * source instanceof Comment:
 *           - dest.appendChild(doc.createComment(...))
 *       * source instanceof DataNode:
 *           - dest.appendChild(doc.createTextNode(...)) (e.g. inside <script> / <style>)
 *       * source instanceof other (e.g. DocumentType):
 *           - falls into unhandled else branch
 *   - W3CBuilder.tail(Node source, int depth):
 *       * source instanceof Element && dest.getParentNode() instanceof Element:
 *           - true -> dest = (Element) dest.getParentNode()
 *           - false -> no undescend (at root element boundary)
 *   - W3CBuilder.copyAttributes(Node source, Element el):
 *       * Attributes sanitize regex replacement: key.replaceAll("[^-a-zA-Z0-9_:.]", "")
 *       * el.setAttribute(key, value)
 *   - W3CBuilder.updateNamespaces(Element el):
 *       * key.equals("xmlns") -> prefix = ""
 *       * key.startsWith("xmlns:") -> prefix = key.substring(6)
 *       * other attribute key -> continue
 *   - asString(Document doc):
 *       * Transformer serialization to XML String
 *
 * 2. Defect Zone Targeting (Defects4J Known Fault):
 *   - Issue: Invalid character exception in el.setAttribute(key, val) when attribute key starts
 *     with characters forbidden as XML name starters (e.g., numbers, symbols like '2start', '-foo').
 *     The regex replaces chars but does not enforce XML 1.0 NameStartChar rules.
 *   - handlesInvalidAttributeNames targets this exact failure path.
 */
public class W3CDomGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSimpleHtmlConversion() {
        String html = "<html lang='en'><head><title>Test Title</title></head><body><p class='intro'>Hello World</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNotNull("W3C Document should not be null", w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getTagName());

        NodeList titles = w3cDoc.getElementsByTagName("title");
        assertEquals(1, titles.getLength());
        assertEquals("Test Title", titles.item(0).getTextContent());

        NodeList paragraphs = w3cDoc.getElementsByTagName("p");
        assertEquals(1, paragraphs.getLength());
        Element p = (Element) paragraphs.item(0);
        assertEquals("Hello World", p.getTextContent());
        assertEquals("intro", p.getAttribute("class"));
    }

    @Test(timeout = 4000)
    public void testHierarchyUndescendInTail() {
        // Deep hierarchy ensuring tail() undescends properly across multiple levels
        String html = "<html><body><div id='outer'><div id='inner'><p>Deep</p></div><span id='sibling'>Next</span></div></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList outerList = w3cDoc.getElementsByTagName("div");
        assertTrue(outerList.getLength() >= 2);

        Element outer = (Element) outerList.item(0);
        assertEquals("outer", outer.getAttribute("id"));

        NodeList innerP = outer.getElementsByTagName("p");
        assertEquals(1, innerP.getLength());
        assertEquals("Deep", innerP.item(0).getTextContent());

        NodeList siblingSpan = outer.getElementsByTagName("span");
        assertEquals(1, siblingSpan.getLength());
        assertEquals("sibling", ((Element) siblingSpan.item(0)).getAttribute("id"));
    }

    @Test(timeout = 4000)
    public void testDataNodeHandling() {
        // Scripts and styles generate DataNode children in Jsoup
        String html = "<html><head><script>var x = 10 < 20;</script><style>body { color: red; }</style></head><body></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList scriptList = w3cDoc.getElementsByTagName("script");
        assertEquals(1, scriptList.getLength());
        assertEquals("var x = 10 < 20;", scriptList.item(0).getTextContent());

        NodeList styleList = w3cDoc.getElementsByTagName("style");
        assertEquals(1, styleList.getLength());
        assertEquals("body { color: red; }", styleList.item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testCommentNodeHandling() {
        String html = "<html><body><!-- This is a test comment --><p>Content</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Element body = (Element) w3cDoc.getElementsByTagName("body").item(0);
        Node firstChild = body.getFirstChild();
        assertEquals(Node.COMMENT_NODE, firstChild.getNodeType());
        assertEquals(" This is a test comment ", firstChild.getNodeValue());
    }

    @Test(timeout = 4000)
    public void testNamespaceHandlingDefaultAndPrefixed() {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml' xmlns:epub='http://www.idpf.org/2007/ops'>"
                + "<body><epub:section id='sec1'>Chapter 1</epub:section></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Element root = w3cDoc.getDocumentElement();
        assertEquals("http://www.w3.org/1999/xhtml", root.getNamespaceURI());

        NodeList sections = w3cDoc.getElementsByTagNameNS("http://www.idpf.org/2007/ops", "section");
        assertEquals(1, sections.getLength());
        Element section = (Element) sections.item(0);
        assertEquals("http://www.idpf.org/2007/ops", section.getNamespaceURI());
        assertEquals("epub:section", section.getTagName());
        assertEquals("sec1", section.getAttribute("id"));
    }

    @Test(timeout = 4000)
    public void testPrefixWithoutDeclaredNamespace() {
        String html = "<html><body><custom:tag>Content</custom:tag></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList tags = w3cDoc.getElementsByTagName("custom:tag");
        assertEquals(1, tags.getLength());
        assertNull("Namespace URI should be null for undeclared prefix", tags.item(0).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testAsStringSerialization() {
        String html = "<html><head><title>Title</title></head><body><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);
        String xmlString = w3c.asString(w3cDoc);

        assertNotNull(xmlString);
        assertTrue("Output should contain XML header or elements", xmlString.contains("<title>Title</title>"));
        assertTrue("Output should contain p tag", xmlString.contains("<p>Text</p>"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Document URI Boundaries
    // =========================================================================

    @Test(timeout = 4000)
    public void testDocumentLocationSetWhenNotBlank() {
        String html = "<html><head></head><body></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "https://example.com/page.html");
        assertEquals("https://example.com/page.html", jsoupDoc.location());

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertEquals("https://example.com/page.html", w3cDoc.getDocumentURI());
    }

    @Test(timeout = 4000)
    public void testDocumentLocationBlankLeavesURINull() {
        String html = "<html><head></head><body></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html); // default empty location

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNull("URI should be null if jsoup document location is blank", w3cDoc.getDocumentURI());
    }

    @Test(timeout = 4000)
    public void testEmptyElementAndEmptyTextNodes() {
        String html = "<div></div><span>   </span>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList divs = w3cDoc.getElementsByTagName("div");
        assertEquals(1, divs.getLength());
        assertEquals("", divs.item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testUnhandledNodeTypesIgnored() {
        // DocumentType node is not Element, TextNode, Comment, or DataNode -> exercises unhandled path
        String html = "<!DOCTYPE html><html><body><p>Hello</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertEquals("html", w3cDoc.getDocumentElement().getTagName());
    }

    @Test(timeout = 4000)
    public void testCustomAttributeCharactersPreserved() {
        String html = "<html><body><div data-id_test:sub.part='value123'></div></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Element div = (Element) w3cDoc.getElementsByTagName("div").item(0);
        assertEquals("value123", div.getAttribute("data-id_test:sub.part"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlesInvalidAttributeNames() {
        // Defect Target: Attribute names starting with digits or invalid XML characters (e.g., '2start')
        // In the buggy implementation, regex '[^-a-zA-Z0-9_:.]' leaves '2start' unchanged,
        // which triggers org.w3c.dom.DOMException: INVALID_CHARACTER_ERR on el.setAttribute(key, ...).
        String html = "<html><head></head><body 2start='foo' -bad='bar' ?invalid='baz' foo='valid'><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNotNull("W3C Document conversion should succeed without throwing DOMException", w3cDoc);
        Element body = (Element) w3cDoc.getElementsByTagName("body").item(0);
        assertNotNull(body);
        assertEquals("valid", body.getAttribute("foo"));
    }

    @Test(timeout = 4000)
    public void testHandlesEmptyAttributeNameAfterSanitization() {
        // Attribute with only symbols (e.g. '?') that gets sanitized to empty string ""
        String html = "<div ?='question'>Content</div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        NodeList divs = w3cDoc.getElementsByTagName("div");
        assertEquals(1, divs.getLength());
    }

    // =========================================================================
    // Partition D: Defensive Guards & Exception Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFromJsoupNullDocThrowsException() {
        W3CDom w3c = new W3CDom();
        w3c.fromJsoup(null);
    }

    @Test(timeout = 4000)
    public void testConvertWithExplicitW3CDocument() throws Exception {
        String html = "<html><body><p>Direct Convert</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document customW3CDoc = dbf.newDocumentBuilder().newDocument();

        W3CDom w3c = new W3CDom();
        w3c.convert(jsoupDoc, customW3CDoc);

        assertNotNull(customW3CDoc.getDocumentElement());
        assertEquals("html", customW3CDoc.getDocumentElement().getTagName());
        assertEquals(1, customW3CDoc.getElementsByTagName("p").getLength());
        assertEquals("Direct Convert", customW3CDoc.getElementsByTagName("p").item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testDirectDataNodeInjection() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<div></div>");
        org.jsoup.nodes.Element div = jsoupDoc.select("div").first();
        DataNode dataNode = new DataNode("RAW_DATA_CONTENT", "");
        div.appendChild(dataNode);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Element w3cDiv = (Element) w3cDoc.getElementsByTagName("div").item(0);
        assertEquals("RAW_DATA_CONTENT", w3cDiv.getTextContent());
    }
}