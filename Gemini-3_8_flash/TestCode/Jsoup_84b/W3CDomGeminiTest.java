package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.helper.W3CDom
 *
 * 1. Branch Coverage Targets:
 *    - fromJsoup(Document in): Validate.notNull(in) [null vs non-null input]
 *    - convert(in, out): StringUtil.isBlank(in.location()) [blank vs non-blank location]
 *    - W3CBuilder.head:
 *        - Node type: Element, TextNode, Comment, DataNode, and Unhandled (e.g., XmlDeclaration/DocumentType)
 *        - dest == null (root setup) vs dest != null (child appending)
 *    - W3CBuilder.tail:
 *        - source instanceof Element && dest.getParentNode() instanceof Element [true for nested elements vs false for root element]
 *        - namespacesStack.pop() invocation
 *    - W3CBuilder.copyAttributes:
 *        - key.matches(...) [valid XML attribute name vs invalid/stripped attribute name]
 *    - W3CBuilder.updateNamespaces:
 *        - attr key.equals("xmlns") [default namespace mapping]
 *        - attr key.startsWith("xmlns:") [prefixed namespace mapping]
 *        - neither xmlns key [continue loop branch]
 *        - el.tagName().indexOf(":") > 0 [element with prefix vs element without prefix]
 *    - asString(Document doc): DOMSource serialization to string
 *
 * 2. Defect-Targeted Ground Truth:
 *    - Defect: treatsUndeclaredNamespaceAsLocalName
 *    - Trigger: Element tag containing a colon prefix (e.g., <fb:like>) with no xmlns definition
 *    - Cause: Calling doc.createElementNS(null, "fb:like") throws org.w3c.dom.DOMException: NAMESPACE_ERR
 *    - Expected Behavior: Conversion succeeds and serialized output retains the tag name
 */
public class W3CDomGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConvertBasicHtmlStructure() {
        String html = "<!DOCTYPE html><html><head><title>Test Title</title></head><body><p id=\"p1\">Hello W3C</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        assertNotNull("W3C Document should not be null", w3cDoc);
        Element htmlEl = w3cDoc.getDocumentElement();
        assertEquals("Root element should be html", "html", htmlEl.getTagName());

        NodeList pElements = w3cDoc.getElementsByTagName("p");
        assertEquals("Should contain 1 paragraph element", 1, pElements.getLength());

        Element p = (Element) pElements.item(0);
        assertEquals("p1", p.getAttribute("id"));
        assertEquals("Hello W3C", p.getTextContent());
    }

    @Test(timeout = 4000)
    public void testConvertNodeTypesTextCommentAndData() {
        String html = "<div>" +
                "<!-- Top Comment -->" +
                "Plain Text" +
                "<script>var secret = 42;</script>" +
                "<style>body { background: #fff; }</style>" +
                "</div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);

        NodeList scripts = w3cDoc.getElementsByTagName("script");
        assertEquals(1, scripts.getLength());
        assertEquals("var secret = 42;", scripts.item(0).getTextContent());

        NodeList styles = w3cDoc.getElementsByTagName("style");
        assertEquals(1, styles.getLength());
        assertEquals("body { background: #fff; }", styles.item(0).getTextContent());

        String serialized = w3c.asString(w3cDoc);
        assertTrue("Output should serialize comment", serialized.contains("<!-- Top Comment -->"));
        assertTrue("Output should serialize text", serialized.contains("Plain Text"));
    }

    @Test(timeout = 4000)
    public void testNamespaceHandlingDefaultAndPrefix() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:custom=\"http://example.com/custom\">" +
                "<custom:section id=\"sec1\"><p>In namespace</p></custom:section>" +
                "</html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "", Parser.xmlParser());

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);

        Element root = w3cDoc.getDocumentElement();
        assertEquals("http://www.w3.org/1999/xhtml", root.getNamespaceURI());

        NodeList customSections = w3cDoc.getElementsByTagName("custom:section");
        assertEquals(1, customSections.getLength());
        Element section = (Element) customSections.item(0);
        assertEquals("http://example.com/custom", section.getNamespaceURI());
        assertEquals("sec1", section.getAttribute("id"));
    }

    @Test(timeout = 4000)
    public void testNestedNamespaceInheritance() {
        String html = "<root xmlns:outer=\"urn:outer\">" +
                "<middle>" +
                "<outer:inner>Content</outer:inner>" +
                "</middle>" +
                "</root>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "", Parser.xmlParser());

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);

        NodeList innerNodes = w3cDoc.getElementsByTagName("outer:inner");
        assertEquals(1, innerNodes.getLength());
        assertEquals("urn:outer", innerNodes.item(0).getNamespaceURI());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDocumentLocationHandling() {
        // Test non-blank location
        org.jsoup.nodes.Document docWithLocation = new org.jsoup.nodes.Document("http://example.com/test.html");
        docWithLocation.appendElement("html").appendElement("body").appendElement("p").text("Location Test");

        W3CDom w3c = new W3CDom();
        Document w3cDocWithLoc = w3c.fromJsoup(docWithLocation);
        assertEquals("http://example.com/test.html", w3cDocWithLoc.getDocumentURI());

        // Test blank location
        org.jsoup.nodes.Document docWithoutLocation = Jsoup.parse("<p>No Location</p>");
        Document w3cDocWithoutLoc = w3c.fromJsoup(docWithoutLocation);
        assertNull("Blank location should leave documentURI null", w3cDocWithoutLoc.getDocumentURI());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedDomTraversorTail() {
        StringBuilder sb = new StringBuilder();
        int depth = 15;
        for (int i = 0; i < depth; i++) {
            sb.append("<div id=\"d").append(i).append("\">");
        }
        sb.append("Deep Text");
        for (int i = 0; i < depth; i++) {
            sb.append("</div>");
        }

        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(sb.toString());
        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);

        Element innermost = (Element) w3cDoc.getElementsByTagName("div").item(depth - 1);
        assertEquals("d" + (depth - 1), innermost.getAttribute("id"));
        assertEquals("Deep Text", innermost.getTextContent());
    }

    @Test(timeout = 4000)
    public void testAttributeFilteringAndSanitization() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<p>Attr Test</p>");
        org.jsoup.nodes.Element p = jsoupDoc.select("p").first();
        p.attr("valid_name-1.0:test", "value1");
        p.attr("@invalid@name", "value2"); // '@' stripped -> "invalidname", valid
        p.attr("123starts_with_num", "value3"); // starts with digit -> invalid xml attr
        p.attr("$$$", "value4"); // all stripped -> empty string -> invalid xml attr

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Element w3cP = (Element) w3cDoc.getElementsByTagName("p").item(0);
        assertEquals("value1", w3cP.getAttribute("valid_name-1.0:test"));
        assertEquals("value2", w3cP.getAttribute("invalidname"));
        assertFalse("Numeric-starting attr should be rejected", w3cP.hasAttribute("123starts_with_num"));
        assertFalse("Empty stripped attr should be rejected", w3cP.hasAttribute(""));
    }

    @Test(timeout = 4000)
    public void testAsStringOutputIntegrity() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<html><head><title>Serialize</title></head><body><br/></body></html>");
        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        String serialized = w3c.asString(w3cDoc);
        assertNotNull(serialized);
        assertTrue("Serialized output should contain title", serialized.contains("<title>Serialize</title>"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known fault:
     * treatsUndeclaredNamespaceAsLocalName
     * An undeclared namespace prefix (such as <fb:like>) causes doc.createElementNS(null, "fb:like")
     * to throw DOMException: NAMESPACE_ERR on defective versions.
     */
    @Test(timeout = 4000)
    public void testTreatsUndeclaredNamespaceAsLocalName() {
        String html = "<fb:like>One</fb:like>";
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3Doc = w3c.fromJsoup(doc);
        assertNotNull("W3C Document should be created successfully without DOMException", w3Doc);

        String out = w3c.asString(w3Doc);
        assertTrue("Output should preserve the fb:like tag", out.contains("fb:like"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFromJsoupNullDocThrowsException() {
        new W3CDom().fromJsoup(null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAsStringTransformerExceptionHandled() {
        // Passing null Document to DOMSource causes TransformerException wrapped into IllegalStateException
        new W3CDom().asString(null);
    }

    // =========================================================================
    // Partition E: Direct Builder Invocations & Unhandled Node Types
    // =========================================================================

    @Test(timeout = 4000)
    public void testW3CBuilderUnhandledNodesAndRootTail() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document w3cDoc = db.newDocument();

        W3CDom.W3CBuilder builder = new W3CDom.W3CBuilder(w3cDoc);

        // Parse XML containing an XmlDeclaration node (which is unhandled in W3CBuilder.head)
        org.jsoup.nodes.Document xmlDoc = Jsoup.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>", "", Parser.xmlParser());
        org.jsoup.nodes.Node xmlDeclaration = xmlDoc.childNode(0);

        // head and tail on unhandled node should fall into "// unhandled" branch without errors
        builder.head(xmlDeclaration, 0);
        builder.tail(xmlDeclaration, 0);

        // Traverse root element to exercise tail when dest.getParentNode() is Document (not Element)
        org.jsoup.nodes.Element rootEl = xmlDoc.child(0);
        builder.head(rootEl, 0);
        builder.tail(rootEl, 0);

        assertEquals("Root element should be attached to doc", "root", w3cDoc.getDocumentElement().getTagName());
    }

    @Test(timeout = 4000)
    public void testConvertViaExistingDocumentInstance() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<div><span>Direct Convert</span></div>");
        W3CDom w3c = new W3CDom();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document customDoc = dbf.newDocumentBuilder().newDocument();

            w3c.convert(jsoupDoc, customDoc);

            NodeList spanList = customDoc.getElementsByTagName("span");
            assertEquals(1, spanList.getLength());
            assertEquals("Direct Convert", spanList.item(0).getTextContent());
        } catch (Exception e) {
            fail("Direct convert invocation should not throw: " + e.getMessage());
        }
    }
}