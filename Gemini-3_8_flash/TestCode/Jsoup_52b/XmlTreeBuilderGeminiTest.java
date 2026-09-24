package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;

import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.XmlTreeBuilder
 *
 * Decision / Branch Matrix:
 * 1. initialiseParse(input, baseUri, errors)
 *    - stack contains doc: [True]
 *    - doc outputSettings syntax: [Document.OutputSettings.Syntax.xml]
 * 2. process(Token token)
 *    - Token.StartTag -> calls insert(StartTag)
 *    - Token.EndTag -> calls popStackToClose(EndTag)
 *    - Token.Comment -> calls insert(Comment)
 *    - Token.Character -> calls insert(Character)
 *    - Token.Doctype -> calls insert(Doctype)
 *    - Token.EOF -> no-op break
 * 3. insert(Token.StartTag startTag)
 *    - isSelfClosing():
 *      - True & tag.isKnownTag() == false -> tag.setSelfClosing()
 *      - True & tag.isKnownTag() == true  -> self-closing acknowledged, no setSelfClosing()
 *      - False                            -> element added to stack
 * 4. insert(Token.Comment commentToken)
 *    - commentToken.bogus:
 *      - False -> insert standard Comment node
 *      - True:
 *        - data.length() <= 1 -> standard Comment node inserted
 *        - data.length() > 1:
 *          - data starts with "!" -> XmlDeclaration created with isProcessingInstruction = true
 *          - data starts with "?" -> XmlDeclaration created with isProcessingInstruction = false
 *          - data starts with neither -> standard Comment node inserted
 * 5. popStackToClose(Token.EndTag endTag)
 *    - Element matching endTag found in stack:
 *      - Found at top -> pops 1 element
 *      - Found deeper -> pops multiple intermediate unclosed elements
 *    - Element matching endTag NOT found in stack -> skips without modifying stack
 * 6. parseFragment(inputFragment, baseUri, errors)
 *    - Returns childNodes of the synthesized document
 *
 * Target Defect (Defects4J):
 * - Bogus comments representing XML declarations (`<?xml ...?>`) must parse declaration attributes.
 *   In the defective version, attributes are omitted/empty on the resulting XmlDeclaration node.
 */
public class XmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardXmlDocumentTreeConstruction() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<root><child attr=\"val\">Content</child></root>", "http://example.com/");

        assertNotNull("Document must not be null", doc);
        assertEquals("Syntax must be set to XML", Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());

        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("val", child.attr("attr"));
        assertEquals("Content", child.text());
    }

    @Test(timeout = 4000)
    public void testDoctypeNodeInsertion() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        assertEquals(2, doc.childNodeSize());
        assertTrue("First child node should be DocumentType", doc.childNode(0) instanceof DocumentType);

        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
        assertEquals("", doctype.attr("publicId"));
    }

    @Test(timeout = 4000)
    public void testStandardNonBogusCommentInsertion() {
        String xml = "<root><!-- This is a standard comment --></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue("Child should be Comment", root.childNode(0) instanceof Comment);

        Comment comment = (Comment) root.childNode(0);
        assertEquals(" This is a standard comment ", comment.getData());
    }

    @Test(timeout = 4000)
    public void testCharacterNodeInsertionAndCData() {
        String xml = "<root><![CDATA[cdata section <unescaped> & data]]></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue("CDATA should be inserted as text", root.childNode(0) instanceof TextNode);

        TextNode textNode = (TextNode) root.childNode(0);
        assertEquals("cdata section <unescaped> & data", textNode.getWholeText());
    }

    @Test(timeout = 4000)
    public void testKnownAndUnknownSelfClosingTags() {
        // 'img' is a known HTML void tag, 'custom-self' is unknown
        String xml = "<root><img id=\"1\"/><custom-self id=\"2\"/></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals(2, root.children().size());

        Element img = root.child(0);
        assertEquals("img", img.tagName());
        assertEquals("1", img.attr("id"));

        Element custom = root.child(1);
        assertEquals("custom-self", custom.tagName());
        assertEquals("2", custom.attr("id"));
        assertTrue("Unknown tag marked self-closing should retain self-closing flag", custom.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testParseFragmentBasic() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<item id=\"1\">A</item><item id=\"2\">B</item>", "http://example.com/", ParseErrorList.noTracking());

        assertNotNull("Nodes list should not be null", nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertTrue(nodes.get(1) instanceof Element);

        assertEquals("A", ((Element) nodes.get(0)).text());
        assertEquals("B", ((Element) nodes.get(1)).text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyInputParsing() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("", "http://example.com/");

        assertNotNull(doc);
        assertEquals(0, doc.children().size());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyInput() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("   \n\t   ", "http://example.com/");

        assertNotNull(doc);
        assertEquals(1, doc.childNodeSize());
        assertTrue(doc.childNode(0) instanceof TextNode);
    }

    @Test(timeout = 4000)
    public void testPopStackWhenEndTagNotInStack() {
        // Tag </orphan> has no open start tag; popStackToClose should cleanly skip
        String xml = "<root><child>value</child></orphan></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testPopStackDeepClosing() {
        // Closing an outer element prematurely should pop all descendants
        String xml = "<a><b><c><d>nested</d></a>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
        Element c = b.child(0);
        assertEquals("c", c.tagName());
        Element d = c.child(0);
        assertEquals("d", d.tagName());
        assertEquals("nested", d.text());
    }

    @Test(timeout = 4000)
    public void testBogusCommentLengthBoundaries() {
        // Bogus comment with data length <= 1, e.g., <?> or <!>
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse("<?>", "http://example.com/");

        assertEquals(1, doc.childNodeSize());
        assertTrue("Single-char question mark bogus comment should remain a Comment node",
                doc.childNode(0) instanceof Comment);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDetectAndParseDeclarationAttributesXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" something=\"else\"?><val>One</val>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com");

        assertTrue("First child must be XmlDeclaration", doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);

        // Targeted Defect Check: Attributes must be parsed and accessible
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("else", decl.attr("something"));
    }

    @Test(timeout = 4000)
    public void testHandlesXmlDeclarationAsDeclarationNode() {
        String xml = "<?xml encoding='UTF-8' ?><body>One</body><!-- a comment -->";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com");

        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("UTF-8", decl.attr("encoding"));

        // Validate outerHtml format
        String outerHtml = doc.outerHtml().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        assertTrue("Outer HTML should contain properly quoted declaration",
                outerHtml.startsWith("<?xml encoding=\"UTF-8\"?>") ||
                outerHtml.startsWith("<?xml encoding='UTF-8'?>"));
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionBangDeclaration() {
        // When bogus comment starts with '!', isProcessingInstruction is true
        String xml = "<!foo bar=\"baz\"><root/>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com");

        assertTrue("Node should be XmlDeclaration", doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertTrue("Should be treated as processing instruction / bang declaration",
                decl.getWholeDeclaration().startsWith("!") || decl.outerHtml().startsWith("<!"));
    }

    // =========================================================================
    // Partition D: State Transitions & Error Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseWithParseErrorListTracking() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        Document doc = tb.parse("<root><unclosed>text</root>", "http://example.com/", errorList);

        assertNotNull(doc);
        assertNotNull(errorList);
        assertEquals("root", doc.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithMultipleMixedNodes() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<!-- comment -->Text<tag/>", "http://example.com/", ParseErrorList.noTracking());

        assertEquals(3, nodes.size());
        assertTrue(nodes.get(0) instanceof Comment);
        assertTrue(nodes.get(1) instanceof TextNode);
        assertTrue(nodes.get(2) instanceof Element);
    }

    @Test(timeout = 4000)
    public void testDuplicateNestedSameTagNames() {
        String xml = "<folder><folder><folder>deep</folder></folder></folder>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element outer = doc.child(0);
        assertEquals("folder", outer.tagName());
        Element middle = outer.child(0);
        assertEquals("folder", middle.tagName());
        Element inner = middle.child(0);
        assertEquals("folder", inner.tagName());
        assertEquals("deep", inner.text());
    }

    // =========================================================================
    // Partition E: Parser Delegation and Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testParserViaJsoupFactory() {
        String xml = "<xml><data/></xml>";
        Document doc = Jsoup.parse(xml, "http://example.com/", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        assertEquals("xml", doc.child(0).tagName());
        assertEquals("data", doc.child(0).child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testSelfClosingXmlRoundTrip() {
        String xml = "<test id=\"1\"/><test id=\"2\"/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals(2, doc.children().size());
        assertEquals("1", doc.child(0).attr("id"));
        assertEquals("2", doc.child(1).attr("id"));
    }
}