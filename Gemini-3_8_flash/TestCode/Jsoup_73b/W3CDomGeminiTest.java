package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.LeafNode;
import org.jsoup.parser.Parser;
import org.junit.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.jsoup.helper.W3CDom & W3CDom$W3CBuilder
 * DEFECT SPEC: W3CDomTest::namespacePreservation (Defects4J ground truth failure)
 *   Root Cause: The W3CBuilder retains a single global HashMap<String, String> of namespaces.
 *   When an inner element/subtree introduces or re-defines a namespace (such as xmlns="http://example.com/clip"),
 *   the namespace map state is permanently altered. Subsequent sibling elements or ascending branches
 *   retain the modified namespace rather than restoring the enclosing element's namespace.
 *
 * BRANCH COVERAGE TARGETS:
 * 1. fromJsoup(null): Validate.notNull branch -> IllegalArgumentException.
 * 2. convert(in, out):
 *    - !StringUtil.isBlank(in.location()) [true, false] -> sets document URI.
 * 3. W3CBuilder.head(source, depth):
 *    - source instanceof Element:
 *      - dest == null (root element initialization) vs dest != null (child appending)
 *    - source instanceof TextNode
 *    - source instanceof Comment
 *    - source instanceof DataNode (e.g., <script>, <style>)
 *    - source instanceof other Node (unhandled branch)
 * 4. W3CBuilder.tail(source, depth):
 *    - source instanceof Element && dest.getParentNode() instanceof Element -> undescend to parent
 *    - dest.getParentNode() not instanceof Element (when at root)
 * 5. W3CBuilder.copyAttributes:
 *    - Key sanitization regex: key.replaceAll("[^-a-zA-Z0-9_:.]", "")
 *    - Attribute matching regex: key.matches("[a-zA-Z_:][-a-zA-Z0-9_:.]*") [true, false]
 * 6. W3CBuilder.updateNamespaces:
 *    - key.equals("xmlns") -> prefix = ""
 *    - key.startsWith("xmlns:") -> prefix = key.substring(6)
 *    - other attribute keys -> continue loop
 *    - el.tagName().indexOf(":") > 0 -> prefix extracted vs pos <= 0 -> prefix = ""
 * 7. asString(Document):
 *    - Normal transformation to serialized string.
 * ----------------------------------------------------------------------------------------------------
 */
public class W3CDomGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicDocumentConversion() {
        String html = "<html id='html-root'><head><title>Sample Title</title></head><body><p class='intro'>Hello World</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document doc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull("Converted document should not be null", doc);
        Element root = doc.getDocumentElement();
        assertEquals("html", root.getTagName());
        assertEquals("html-root", root.getAttribute("id"));

        NodeList pList = doc.getElementsByTagName("p");
        assertEquals(1, pList.getLength());
        Element p = (Element) pList.item(0);
        assertEquals("intro", p.getAttribute("class"));
        assertEquals("Hello World", p.getTextContent());

        NodeList titleList = doc.getElementsByTagName("title");
        assertEquals(1, titleList.getLength());
        assertEquals("Sample Title", titleList.item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testConvertWithDocumentLocationUri() {
        String html = "<html><body><p>Location Test</p></body></html>";
        String baseUri = "https://jsoup.org/test/path";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, baseUri);

        W3CDom w3cDom = new W3CDom();
        Document doc = w3cDom.fromJsoup(jsoupDoc);

        assertEquals(baseUri, doc.getDocumentURI());
    }

    @Test(timeout = 4000)
    public void testConvertWithoutLocationLeavesUriNull() {
        String html = "<html><body><p>No Location</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html); // default empty location

        W3CDom w3cDom = new W3CDom();
        Document doc = w3cDom.fromJsoup(jsoupDoc);

        assertNull("Document URI should be null when input location is blank", doc.getDocumentURI());
    }

    @Test(timeout = 4000)
    public void testCommentNodeConversion() {
        String html = "<div><!-- This is a comment --><span>Content</span></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        W3CDom w3cDom = new W3CDom();
        Document doc = w3cDom.fromJsoup(jsoupDoc);

        NodeList divs = doc.getElementsByTagName("div");
        assertEquals(1, divs.getLength());
        Node div = divs.item(0);

        boolean commentFound = false;
        NodeList children = div.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                Comment comment = (Comment) child;
                assertEquals(" This is a comment ", comment.getData());
                commentFound = true;
            }
        }
        assertTrue("Comment node should have been converted into DOM", commentFound);
    }

    @Test(timeout = 4000)
    public void testDataNodeConversion() {
        String html = "<script>var x = 10; var y = 20;</script><style>body { color: red; }</style>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        W3CDom w3cDom = new W3CDom();
        Document doc = w3cDom.fromJsoup(jsoupDoc);

        NodeList scripts = doc.getElementsByTagName("script");
        assertEquals(1, scripts.getLength());
        assertEquals("var x = 10; var y = 20;", scripts.item(0).getTextContent());

        NodeList styles = doc.getElementsByTagName("style");
        assertEquals(1, styles.getLength());
        assertEquals("body { color: red; }", styles.item(0).getTextContent());
    }

    @Test(timeout = 4000)
    public void testAsStringSerialization() {
        String html = "<html><head><title>Serialized</title></head><body><p>Test Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document doc = w3cDom.fromJsoup(jsoupDoc);
        String xml = w3cDom.asString(doc);

        assertNotNull(xml);
        assertTrue(xml.contains("<title>Serialized</title>"));
        assertTrue(xml.contains("<p>Test Text</p>"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAttributeSanitizationAndFiltering() {
        // Attribute with valid characters, invalid symbols, and start with number
        String html = "<div valid-attr='val1' "
                + "clean@attr='val2' "       // '@' replaced -> cleanattr (valid)
                + "123numeric='val3' "      // starts with digit -> fails regex match -> omitted
                + "@#$='val4' "             // empty string after regex replacement -> omitted
                + "_under:score.dot='val5'" // valid start and body
                + "></div>";

        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        W3CDom w3cDom = new W3CDom();
        Document doc = w3cDom.fromJsoup(jsoupDoc);

        Element div = (Element) doc.getElementsByTagName("div").item(0);
        assertEquals("val1", div.getAttribute("valid-attr"));
        assertEquals("val2", div.getAttribute("cleanattr"));
        assertEquals("val5", div.getAttribute("_under:score.dot"));
        assertFalse("Numeric-prefixed attribute should be filtered out", div.hasAttribute("123numeric"));
    }