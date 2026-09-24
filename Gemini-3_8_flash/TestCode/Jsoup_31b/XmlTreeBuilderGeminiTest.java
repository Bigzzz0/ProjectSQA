package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Class Under Test: org.jsoup.parser.XmlTreeBuilder
 * Primary Responsibilities: Parse XML into Document DOM structure using an element stack and token processing.
 *
 * Targeted Decision Branches & Conditions:
 * 1. initialiseParse(input, baseUri, errors):
 *    - Stack initialization: doc must be pushed onto stack (stack.add(doc))
 * 2. process(Token):
 *    - StartTag branch -> insert(Token.StartTag)
 *    - EndTag branch -> popStackToClose(Token.EndTag)
 *    - Comment branch -> insert(Token.Comment)
 *    - Character branch -> insert(Token.Character)
 *    - Doctype branch -> insert(Token.Doctype)
 *    - EOF branch -> break, no-op
 *    - default branch -> Validate.fail
 * 3. insert(Token.StartTag):
 *    - isSelfClosing = true vs false
 *    - When self-closing:
 *      * !tag.isKnownTag() (unknown custom XML tag) -> tag.setSelfClosing()
 *      * tag.isKnownTag() (HTML known tag like <img> or <div>) -> does not setSelfClosing on Tag definition
 *      * Tokeniser self-closing acknowledged
 *      * Not added to stack
 *    - When not self-closing:
 *      * Added to stack
 * 4. popStackToClose(Token.EndTag):
 *    - Match exists on stack vs Match does not exist (skip / early return)
 *    - Stack element == firstFound (exact match popped, stop)
 *    - Stack element != firstFound (nested unclosed elements forcibly popped until firstFound)
 * 5. insert(Token.Comment):
 *    - Standard comment <!-- ... --> -> Comment node inserted
 *    - Defect-Targeted Condition: XML declarations parsed as comments (bogus comment <?xml ... ?>)
 *      Fixed versions convert bogus comments starting with '?' or '!' to XmlDeclaration nodes (#declaration).
 *      Defective version leaves them as Comment nodes (#comment).
 * 6. insert(Token.Character):
 *    - Standard text data, whitespace, special entities, CDATA sections.
 * 7. insert(Token.Doctype):
 *    - DOCTYPE token populated with name, publicId, and systemId.
 * -------------------------------------------------------------------------------------------------------
 */
public class XmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseStandardNestedHierarchy() {
        String xml = "<root><parent id=\"1\"><child>Inner Text</child></parent></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        assertNotNull(doc);
        Element root = doc.select("root").first();
        assertNotNull("Root element should exist", root);

        Element parent = root.select("parent").first();
        assertNotNull(parent);
        assertEquals("1", parent.attr("id"));

        Element child = parent.select("child").first();
        assertNotNull(child);
        assertEquals("Inner Text", child.text());
    }

    @Test(timeout = 4000)
    public void testParseDocumentType() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><root/>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        List<Node> childNodes = doc.childNodes();
        assertTrue("Document must have child nodes", childNodes.size() >= 2);
        assertTrue("First node must be DocumentType", childNodes.get(0) instanceof DocumentType);

        DocumentType doctype = (DocumentType) childNodes.get(0);
        assertEquals("#doctype", doctype.nodeName());
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    @Test(timeout = 4000)
    public void testParseStandardComment() {
        String xml = "<root><!-- This is a valid comment --><child/></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals(2, root.childNodes().size());

        Node commentNode = root.childNode(0);
        assertTrue("Expected Comment node", commentNode instanceof Comment);
        assertEquals(" This is a valid comment ", ((Comment) commentNode).getData());
    }

    @Test(timeout = 4000)
    public void testParseTextAndCData() {
        String xml = "<root><data><![CDATA[<unescaped> & 'content'</data>]]></data></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element dataEl = doc.select("data").first();
        assertNotNull(dataEl);
        assertEquals("<unescaped> & 'content'</data>", dataEl.text());
    }

    @Test(timeout = 4000)
    public void testMultipleSiblingElements() {
        String xml = "<root><item id=\"1\"/><item id=\"2\"/><item id=\"3\"/></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals(3, root.children().size());
        assertEquals("1", root.child(0).attr("id"));
        assertEquals("2", root.child(1).attr("id"));
        assertEquals("3", root.child(2).attr("id"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInput() {
        XmlTreeBuilder tb = new XmlTreeBuilder();

        Document emptyDoc = tb.parse("", "http://example.com/");
        assertNotNull(emptyDoc);
        assertEquals(0, emptyDoc.children().size());

        Document whitespaceDoc = tb.parse("   \n\t   ", "http://example.com/");
        assertNotNull(whitespaceDoc);
        assertEquals(0, whitespaceDoc.children().size());
    }

    @Test(timeout = 4000)
    public void testPopStackWithUnclosedInnerElements() {
        // When </root> is parsed, unclosed inner tags <a>, <b>, <c> must be popped off
        String xml = "<root><a><b><c></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.select("root").first();
        assertNotNull(root);
        Element a = root.select("a").first();
        assertNotNull(a);
        Element b = a.select("b").first();
        assertNotNull(b);
        Element c = b.select("c").first();
        assertNotNull(c);
    }

    @Test(timeout = 4000)
    public void testPopStackWithUnmatchedEndTagSkipsGracefully() {
        // Tag </unmatched> does not exist on stack; should be skipped without error
        String xml = "<root><child>value</child></unmatched><other>value2</other></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals(2, root.children().size());
        assertEquals("child", root.child(0).tagName());
        assertEquals("other", root.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testSelfClosingKnownHtmlTagVsUnknownXmlTag() {
        // <img> is known tag in HTML; <custom-tag> is unknown
        String xml = "<root><img src=\"test.jpg\"/><custom-tag attr=\"val\"/></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals(2, root.children().size());

        Element img = root.child(0);
        assertEquals("img", img.tagName());
        assertTrue("img is a known tag", img.tag().isKnownTag());

        Element custom = root.child(1);
        assertEquals("custom-tag", custom.tagName());
        assertFalse("custom-tag is an unknown tag", custom.tag().isKnownTag());
        assertTrue("Unknown self closing tag must be marked self closing", custom.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testCaseSensitivityPreserved() {
        String xml = "<CaseSensitiveTag AttrName=\"AttrVal\">text</CaseSensitiveTag>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        Element el = doc.child(0);
        assertEquals("CaseSensitiveTag", el.tagName());
        assertEquals("AttrVal", el.attr("AttrName"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Target Defect: handlesXmlDeclarationAsDeclaration
     * Ground Truth: In defective versions, XML declarations like '<?xml encoding='UTF-8' ?>'
     * are incorrectly inserted as Comment nodes instead of XmlDeclaration nodes.
     * Expectation: Node name must be "#declaration" and type must be XmlDeclaration.
     */
    @Test(timeout = 4000)
    public void testXmlDeclarationParsedAsXmlDeclarationDefect() {
        String xml = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/*");

        assertTrue("Root must have children", doc.childNodes().size() >= 3);
        Node firstChild = doc.childNode(0);

        // Defect assertion: defective code produces "#comment", fixed code produces "#declaration"
        assertEquals("#declaration", firstChild.nodeName());
        assertTrue("First child must be instance of XmlDeclaration", firstChild instanceof XmlDeclaration);

        XmlDeclaration decl = (XmlDeclaration) firstChild;
        assertEquals("xml", decl.name());
    }

    @Test(timeout = 4000)
    public void testXmlProcessingInstructionAsDeclarationDefect() {
        String xml = "<?php echo 'hello'; ?><data>test</data>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");

        Node firstChild = doc.childNode(0);
        assertEquals("#declaration", firstChild.nodeName());
        assertTrue("Processing instruction should be parsed as XmlDeclaration", firstChild instanceof XmlDeclaration);
    }

    // =========================================================================
    // Partition D: Direct Token & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialiseParsePushesDocumentOntoStack() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<dummy/>", "http://example.com/", ParseErrorList.noTracking());

        assertNotNull(tb.doc);
        assertNotNull(tb.stack);
        assertEquals(1, tb.stack.size());
        assertSame("Document must be the first item on the stack", tb.doc, tb.stack.peekLast());
    }

    @Test(timeout = 4000)
    public void testProcessEofToken() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<dummy/>", "http://example.com/", ParseErrorList.noTracking());

        boolean result = tb.process(new Token.EOF());
        assertTrue("Processing EOF token should return true", result);
    }

    @Test(timeout = 4000)
    public void testDirectTokenCharacterAndCommentInsertion() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<dummy/>", "http://example.com/", ParseErrorList.noTracking());

        // Process Character Token
        Token.Character charToken = new Token.Character();
        charToken.data("Manual Character Data");
        assertTrue(tb.process(charToken));

        // Process Comment Token
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("Manual Comment Data");
        assertTrue(tb.process(commentToken));

        // Validate state on document
        List<Node> childNodes = tb.doc.childNodes();
        boolean foundChar = false;
        boolean foundComment = false;

        for (Node node : childNodes) {
            if (node instanceof TextNode && ((TextNode) node).text().equals("Manual Character Data")) {
                foundChar = true;
            } else if (node instanceof Comment && ((Comment) node).getData().equals("Manual Comment Data")) {
                foundComment = true;
            }
        }

        assertTrue("Direct character token must be inserted", foundChar);
        assertTrue("Direct comment token must be inserted", foundComment);
    }

    // =========================================================================
    // Partition E: Base URI Propagation & Root Multiplicity
    // =========================================================================

    @Test(timeout = 4000)
    public void testBaseUriPropagation() {
        String baseUri = "http://example.org/path/file.xml";
        String xml = "<root><child attr=\"val\">text</child></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, baseUri);

        assertEquals(baseUri, doc.baseUri());
        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals(baseUri, root.baseUri());

        Element child = root.select("child").first();
        assertNotNull(child);
        assertEquals(baseUri, child.baseUri());
    }

    @Test(timeout = 4000)
    public void testMultipleRootElementsInXml() {
        String xml = "<doc1>First</doc1><doc2>Second</doc2><doc3>Third</doc3>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");

        assertEquals(3, doc.children().size());
        assertEquals("doc1", doc.child(0).tagName());
        assertEquals("doc2", doc.child(1).tagName());
        assertEquals("doc3", doc.child(2).tagName());
    }
}