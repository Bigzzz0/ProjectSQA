package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for W3CDom targeting line/branch coverage and the known defect:
 * "treatsUndeclaredNamespaceAsLocalName" (NAMESPACE_ERR when prefix used without declaration).
 *
 * [Branch & Defect Analysis Matrix]
 * - fromJsoup: null input → NPE; ParserConfigurationException → IllegalStateException
 * - convert: blank location → no setDocumentURI; non-blank → set
 * - W3CBuilder.head: Element branch (namespace update, createElementNS, copyAttributes, append)
 *                   TextNode branch (createTextNode, append)
 *                   Comment branch (createComment, append)
 *                   DataNode branch (createTextNode, append)
 *                   else branch (unhandled – no action)
 * - W3CBuilder.tail: Element and parent is Element → undescend; else no action; always pop stack
 * - updateNamespaces: xmlns="" → prefix=""; xmlns:prefix → prefix extracted; else skip
 * - copyAttributes: key sanitization regex; valid key pattern check; setAttribute
 * - asString: TransformerException → IllegalStateException
 *
 * Defect branch: When element tag has a colon (prefix) but no namespace declaration for that prefix,
 *                namespace lookup returns null, and createElementNS(null, "prefix:local") throws DOMException.
 *                Expected correct behavior: treat as local name (strip prefix) with null namespace.
 */
public class W3CDomDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testSimpleConversion() {
        Document jsoupDoc = Jsoup.parse("<html><body>Hello</body></html>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getTagName());
    }

    @Test(timeout = 4000)
    public void testWithTextNode() {
        Document jsoupDoc = Jsoup.parse("<p>text content</p>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        org.w3c.dom.Element p = w3cDoc.getDocumentElement();
        assertEquals("p", p.getTagName());
        assertEquals("text content", p.getTextContent());
    }

    @Test(timeout = 4000)
    public void testWithComment() {
        Document jsoupDoc = Jsoup.parse("<div><!-- comment --></div>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        org.w3c.dom.Element div = w3cDoc.getDocumentElement();
        assertEquals(1, div.getChildNodes().getLength());
        assertEquals(org.w3c.dom.Node.COMMENT_NODE, div.getChildNodes().item(0).getNodeType());
        assertEquals(" comment ", div.getChildNodes().item(0).getNodeValue());
    }

    @Test(timeout = 4000)
    public void testWithDataNode() {
        // DataNode appears inside <script> or <style>
        Document jsoupDoc = Jsoup.parse("<script>var x=1;</script>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        org.w3c.dom.Element script = w3cDoc.getDocumentElement();
        assertEquals("script", script.getTagName());
        assertEquals("var x=1;", script.getTextContent());
    }

    @Test(timeout = 4000)
    public void testWithNestedElements() {
        Document jsoupDoc = Jsoup.parse("<ul><li>a</li><li>b</li></ul>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        org.w3c.dom.Element ul = w3cDoc.getDocumentElement();
        assertEquals("ul", ul.getTagName());
        assertEquals(2, ul.getChildNodes().getLength());
        assertEquals("li", ul.getChildNodes().item(0).getNodeName());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFromJsoupNull() {
        new W3CDom().fromJsoup(null);
    }

    @Test(timeout = 4000)
    public void testEmptyDocument() {
        Document jsoupDoc = Jsoup.parse("");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        // root is #root, but convert skips it; no element added
        assertEquals(0, w3cDoc.getChildNodes().getLength());
    }

    @Test(timeout = 4000)
    public void testWithLocation() {
        Document jsoupDoc = Jsoup.parse("<a></a>");
        jsoupDoc.setLocation("http://example.com");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        assertEquals("http://example.com", w3cDoc.getDocumentURI());
    }

    @Test(timeout = 4000)
    public void testCopyAttributesSanitization() {
        Document jsoupDoc = Jsoup.parse("<div bad\"key=\"value\" good-key=\"ok\"></div>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        org.w3c.dom.Element div = w3cDoc.getDocumentElement();
        // bad"key is sanitized to badkey (removing "), but then fails regex? Actually regex allows letters, digits, colon, underscore, dot, hyphen.
        // The sanitization removes quotes, so "badkey" is valid. But the original key "bad\"key" becomes "badkey". Check if set.
        assertTrue(div.hasAttribute("badkey"));
        assertEquals("value", div.getAttribute("badkey"));
        assertTrue(div.hasAttribute("good-key"));
        assertEquals("ok", div.getAttribute("good-key"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testTreatsUndeclaredNamespaceAsLocalName() {
        // Create a jsoup document with an element that has a prefixed tag but no xmlns declaration.
        // The buggy version throws DOMException; correct version should succeed and treat as local name.
        Document jsoupDoc = Jsoup.parse("<html><foo:bar>content</foo:bar></html>");
        // Note: Jsoup will parse "foo:bar" as a tag name with colon, but no namespace declaration.
        // The W3C conversion should handle this gracefully.
        try {
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
            org.w3c.dom.Element root = w3cDoc.getDocumentElement(); // html
            org.w3c.dom.Element child = (org.w3c.dom.Element) root.getChildNodes().item(0);
            // Expected: local name is "bar", namespace is null (or empty)
            assertEquals("bar", child.getLocalName());
            assertNull(child.getNamespaceURI());
            // The qualified name might be "foo:bar" or "bar" depending on implementation.
            // The correct behavior is to treat as local name, so nodeName should be "bar".
            assertEquals("bar", child.getNodeName());
        } catch (Exception e) {
            fail("Conversion should not throw exception for undeclared namespace prefix: " + e.getMessage());
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testNamespaceDeclared() {
        Document jsoupDoc = Jsoup.parse("<html xmlns:pre=\"http://example.com\"><pre:el>text</pre:el></html>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        org.w3c.dom.Element child = (org.w3c.dom.Element) root.getChildNodes().item(0);
        assertEquals("el", child.getLocalName());
        assertEquals("http://example.com", child.getNamespaceURI());
        assertEquals("pre:el", child.getNodeName());
    }

    @Test(timeout = 4000)
    public void testMultipleNamespaces() {
        Document jsoupDoc = Jsoup.parse("<root xmlns:a=\"urn:a\" xmlns:b=\"urn:b\"><a:x/><b:y/></root>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        assertEquals(2, root.getChildNodes().getLength());
        org.w3c.dom.Element child1 = (org.w3c.dom.Element) root.getChildNodes().item(0);
        assertEquals("x", child1.getLocalName());
        assertEquals("urn:a", child1.getNamespaceURI());
        org.w3c.dom.Element child2 = (org.w3c.dom.Element) root.getChildNodes().item(1);
        assertEquals("y", child2.getLocalName());
        assertEquals("urn:b", child2.getNamespaceURI());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testAsString() {
        Document jsoupDoc = Jsoup.parse("<p>hello</p>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        String xml = new W3CDom().asString(w3cDoc);
        assertTrue(xml.contains("<p>hello</p>"));
    }

    @Test(timeout = 4000)
    public void testAsStringWithNamespace() {
        Document jsoupDoc = Jsoup.parse("<html xmlns=\"http://www.w3.org/1999/xhtml\"><body/></html>");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        String xml = new W3CDom().asString(w3cDoc);
        assertTrue(xml.contains("xmlns=\"http://www.w3.org/1999/xhtml\""));
    }
}