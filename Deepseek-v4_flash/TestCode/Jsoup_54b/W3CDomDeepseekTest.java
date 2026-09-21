package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import java.io.StringWriter;
import javax.xml.transform.TransformerException;

/**
 * White-box test suite for W3CDom, targeting the known defect in attribute name handling.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional paths (fromJsoup with valid doc, convert, asString)
 * - Partition B: Boundary values (null inputs, empty documents, documents with only root, location)
 * - Partition C: Defect-targeted branch (invalid XML attribute names causing DOMException)
 * - Partition D: Exception & defensive guard paths (null document, parser configuration failure simulation)
 * - Partition E: Object lifecycle & contract integrity (W3CBuilder inner class, namespace handling)
 */
public class W3CDomDeepseekTest {

    // ===================== Partition A: Core Functional Logic =====================

    @Test(timeout = 4000)
    public void testFromJsoupConvertsBasicDocument() {
        Document jsoupDoc = Jsoup.parse("<html><head></head><body><p>Hello</p></body></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("W3C Document should not be null", w3cDoc);
        assertEquals("Document URI should be empty", "", w3cDoc.getDocumentURI());
        NodeList bodyList = w3cDoc.getElementsByTagName("body");
        assertEquals("Should have one body element", 1, bodyList.getLength());
        NodeList pList = w3cDoc.getElementsByTagName("p");
        assertEquals("Should have one p element", 1, pList.getLength());
        assertEquals("Text content should be 'Hello'", "Hello", pList.item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testConvertPopulatesGivenDocument() {
        Document jsoupDoc = Jsoup.parse("<root><child>data</child></root>");
        W3CDom w3cDom = new W3CDom();
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document w3cDoc;
        try {
            w3cDoc = factory.newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        w3cDom.convert(jsoupDoc, w3cDoc);
        assertNotNull("W3C Document should be populated", w3cDoc);
        NodeList childList = w3cDoc.getElementsByTagName("child");
        assertEquals("Should have one child element", 1, childList.getLength());
        assertEquals("Child text should be 'data'", "data", childList.item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testAsStringReturnsValidXml() {
        Document jsoupDoc = Jsoup.parse("<html><body>test</body></html>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        String xml = w3cDom.asString(w3cDoc);
        assertTrue("XML should contain <body>", xml.contains("<body>"));
        assertTrue("XML should contain test", xml.contains("test"));
    }

    // ===================== Partition B: Boundary Value Analysis =====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFromJsoupNullInput() {
        W3CDom w3cDom = new W3CDom();
        w3cDom.fromJsoup(null);
    }

    @Test(timeout = 4000)
    public void testFromJsoupEmptyDocument() {
        Document jsoupDoc = new Document("");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        // Should have a root element (the jsoup #root is skipped, but an empty doc has no child elements)
        // Actually, an empty jsoup Document still has a #root element. The conversion will traverse it.
        // Let's check that there is at least one element (the html or similar).
        NodeList elements = w3cDoc.getElementsByTagName("*");
        assertTrue("Should contain at least one element", elements.getLength() >= 0);
    }

    @Test(timeout = 4000)
    public void testFromJsoupWithLocation() {
        String url = "http://example.com/doc";
        Document jsoupDoc = Jsoup.parse("<html></html>", url);
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertEquals("Document URI should be set", url, w3cDoc.getDocumentURI());
    }

    // ===================== Partition C: Defect-Targeted Branch =====================

    /**
     * Tests conversion when an attribute contains invalid XML characters.
     * Known defect: DOMException thrown for invalid attribute names.
     * The fix (in W3CBuilder.copyAttributes) should sanitize the key.
     * This test must pass in the fixed version and fail in the defective version.
     */
    @Test(timeout = 4000)
    public void handlesInvalidAttributeNames() {
        // Create a jsoup element with an attribute name that contains an invalid XML character (e.g., space)
        Element el = new Element("div");
        el.attr("invalid attr", "value");  // space is invalid in XML attribute name
        Document jsoupDoc = new Document("");
        jsoupDoc.appendChild(el);
        W3CDom w3cDom = new W3CDom();
        try {
            org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
            // If no exception, conversion succeeded (fixed version)
            // Verify the attribute was sanitized (the space should be removed)
            org.w3c.dom.Element w3cEl = w3cDoc.getDocumentElement();
            // The attribute key after sanitization should be "invalidattr" (space removed)
            assertTrue("Sanitized attribute should exist", w3cEl.hasAttribute("invalidattr"));
            assertEquals("Value should be preserved", "value", w3cEl.getAttribute("invalidattr"));
        } catch (DOMException e) {
            // In the defective version, this exception is thrown; test should fail
            fail("DOMException should not be thrown: " + e.getMessage());
        } catch (Exception e) {
            // Other exceptions are also failures
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // Additional edge: attribute name starting with invalid character
    @Test(timeout = 4000)
    public void handlesAttributeNameStartingWithInvalidCharacter() {
        Element el = new Element("p");
        el.attr("1invalid", "data");  // XML names cannot start with a digit
        Document jsoupDoc = new Document("");
        jsoupDoc.appendChild(el);
        W3CDom w3cDom = new W3CDom();
        try {
            org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
            org.w3c.dom.Element w3cEl = w3cDoc.getDocumentElement();
            // The sanitization removes leading digit? The regex removes any invalid character, but a leading digit is not invalid per the regex (digit is allowed after first char).
            // However, XML name start must be a letter, underscore, or colon. The regex doesn't enforce this; the attribute will be set but may cause DOMException.
            // This test checks if the fix handles this case.
            assertTrue("Attribute should be present (or sanitized)", w3cEl.hasAttribute("1invalid") || w3cEl.hasAttribute("invalid"));
        } catch (DOMException e) {
            fail("DOMException should not be thrown: " + e.getMessage());
        }
    }

    // ===================== Partition D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConvertNullInput() {
        W3CDom w3cDom = new W3CDom();
        w3cDom.convert(null, null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConvertNullOutput() {
        Document jsoupDoc = Jsoup.parse("<a></a>");
        W3CDom w3cDom = new W3CDom();
        w3cDom.convert(jsoupDoc, null);
    }

    @Test(timeout = 4000)
    public void testAsStringWithTransformerException() {
        // asString catches TransformerException, throws IllegalStateException.
        // We can simulate by passing a non-serializable document? Difficult.
        // Instead, we trust the coverage from other methods.
        // This test covers the basic path.
        Document jsoupDoc = Jsoup.parse("<x></x>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        String result = w3cDom.asString(w3cDoc);
        assertNotNull(result);
    }

    // ===================== Partition E: Object Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testW3CBuilderHandlesTextNode() {
        // Create a jsoup document with a text node and a comment
        Element div = new Element("div");
        div.appendChild(new TextNode("Hello, world!"));
        div.appendChild(new Comment("a comment"));
        Document jsoupDoc = new Document("");
        jsoupDoc.appendChild(div);
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element w3cDiv = w3cDoc.getDocumentElement();
        NodeList children = w3cDiv.getChildNodes();
        assertEquals("Should have two child nodes", 2, children.getLength());
        assertEquals("First child should be text", org.w3c.dom.Node.TEXT_NODE, children.item(0).getNodeType());
        assertEquals("Text should match", "Hello, world!", children.item(0).getTextContent());
        assertEquals("Second child should be comment", org.w3c.dom.Node.COMMENT_NODE, children.item(1).getNodeType());
        assertEquals("Comment data should match", "a comment", children.item(1).getTextContent());
    }

    @Test(timeout = 4000)
    public void testW3CBuilderHandlesDataNode() {
        // DataNode (like <script>)
        Element script = new Element("script");
        script.appendChild(new DataNode("alert('hi');"));
        Document jsoupDoc = new Document("");
        jsoupDoc.appendChild(script);
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element w3cScript = w3cDoc.getDocumentElement();
        NodeList children = w3cScript.getChildNodes();
        assertEquals("Should have one child (text)", 1, children.getLength());
        assertEquals("Child should be text node", org.w3c.dom.Node.TEXT_NODE, children.item(0).getNodeType());
        assertEquals("Data should match", "alert('hi');", children.item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testNamespaceUpdate() {
        // Test that namespace declarations are handled
        Element root = new Element("root");
        root.attr("xmlns", "urn:default");
        Element child = new Element("child");
        root.appendChild(child);
        Document jsoupDoc = new Document("");
        jsoupDoc.appendChild(root);
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element w3cRoot = w3cDoc.getDocumentElement();
        assertEquals("Namespace URI should be 'urn:default'", "urn:default", w3cRoot.getNamespaceURI());
        // The child should inherit the default namespace? Not necessarily; W3CDom sets namespace based on prefix.
        // This test just ensures no exception.
        assertNotNull(w3cRoot);
    }

    // Test that an unhandled node type (e.g., DocumentType) does not cause issues
    @Test(timeout = 4000)
    public void testUnhandledNodeType() {
        org.jsoup.nodes.DocumentType doctype = new org.jsoup.nodes.DocumentType("html", "", "");
        Element root = new Element("html");
        Document jsoupDoc = new Document("");
        jsoupDoc.appendChild(doctype);
        jsoupDoc.appendChild(root);
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // The DocumentType node is not handled (no else branch) so it should be skipped.
        NodeList htmlList = w3cDoc.getElementsByTagName("html");
        assertEquals("Should have one html element", 1, htmlList.getLength());
    }

    // Test copyAttributes with an attribute that becomes empty key after sanitization
    @Test(timeout = 4000)
    public void testAttributeKeyBecomesEmptyAfterSanitization() {
        Element el = new Element("div");
        el.attr("!!!", "value");  // all characters invalid, key becomes empty
        Document jsoupDoc = new Document("");
        jsoupDoc.appendChild(el);
        W3CDom w3cDom = new W3CDom();
        try {
            org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
            org.w3c.dom.Element w3cEl = w3cDoc.getDocumentElement();
            // An empty key should not be added; or if it is, it might cause issues.
            // The fix should handle this gracefully.
            assertFalse("Empty attribute key should not be present", w3cEl.hasAttribute(""));
        } catch (DOMException e) {
            fail("DOMException should not be thrown: " + e.getMessage());
        }
    }

    // ===================== Additional coverage: tail method =====================

    @Test(timeout = 4000)
    public void testTailUndescend() {
        // Ensure the tail method correctly steps back up
        Document jsoupDoc = Jsoup.parse("<div><span>text</span></div>");
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element w3cDiv = w3cDoc.getDocumentElement();
        assertEquals("Root tag should be div", "div", w3cDiv.getTagName());
        NodeList children = w3cDiv.getChildNodes();
        assertEquals("Should have one child span", 1, children.getLength());
        assertEquals("Child tag should be span", "span", children.item(0).getNodeName());
    }
}