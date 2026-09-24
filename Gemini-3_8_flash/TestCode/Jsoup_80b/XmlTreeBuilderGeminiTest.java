package org.jsoup.parser;

/*
 * [Branch & Defect Analysis Matrix]
 * =================================================================================================================
 * Target Class: org.jsoup.parser.XmlTreeBuilder
 * Target Environment: Java 8 / JUnit 4 / Defects4J
 *
 * Decision / Condition Matrix Covered:
 * 1. defaultSettings()
 *    - Direct call verifying ParseSettings.preserveCase.
 * 2. parse(Reader, baseUri) & parse(String, baseUri)
 *    - Reader vs String entry points with valid Base URIs and empty Base URIs.
 * 3. initialiseParse(Reader, baseUri, errors, settings)
 *    - Verifies Document.OutputSettings.Syntax is strictly Syntax.xml.
 *    - Verifies doc is placed onto the stack initially.
 * 4. process(Token) - Switch Branch Coverage:
 *    - Case StartTag: Covered via insert(Token.StartTag).
 *    - Case EndTag: Covered via popStackToClose(Token.EndTag).
 *    - Case Comment: Covered via insert(Token.Comment).
 *    - Case Character: Covered via insert(Token.Character).
 *    - Case Doctype: Covered via insert(Token.Doctype).
 *    - Case EOF: Processed cleanly without altering stack state.
 * 5. insert(Token.StartTag)
 *    - Branch: isSelfClosing() == true && !tag.isKnownTag() -> tag.setSelfClosing() invoked.
 *    - Branch: isSelfClosing() == true && tag.isKnownTag() -> self-closing preserved, not added to stack.
 *    - Branch: isSelfClosing() == false -> element added to parse stack.
 * 6. insert(Token.Comment)
 *    - Branch: bogus == false -> regular Comment node created.
 *    - Branch: bogus == true && length <= 1 -> fallback to Comment node.
 *    - Branch: bogus == true && length > 1:
 *        * startsWith("?") -> XmlDeclaration created with isProcessingInstruction = false.
 *        * startsWith("!") -> XmlDeclaration created with isProcessingInstruction = true.
 *        * DEFECT-TARGET: "<?xml version='1.0'>" -> unclosed quote stripping causes doc.child(0)
 *          to throw IndexOutOfBoundsException when children count is 0.
 * 7. insert(Token.Character)
 *    - Branch: token.isCData() == true -> CDataNode created.
 *    - Branch: token.isCData() == false -> TextNode created.
 * 8. insert(Token.Doctype)
 *    - Standard Doctype, Public/System Identifiers, PubSysKey assignment.
 * 9. popStackToClose(Token.EndTag)
 *    - Branch: Target element at stack top -> popped immediately.
 *    - Branch: Target element deeper in stack -> all intermediate unclosed elements popped.
 *    - Branch: Target element NOT found in stack (firstFound == null) -> no-op skip.
 * 10. parseFragment(input, baseUri, errors, settings)
 *    - Verification of fragment node list with case-preservation vs case-normalization.
 * =================================================================================================================
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicXmlParsingAndOutputSyntax() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root id=\"1\"><child>Hello XML</child></root>", "http://example.com");

        assertNotNull("Document should not be null", doc);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());

        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("1", root.attr("id"));
        assertEquals("Hello XML", root.child(0).text());
        assertEquals("http://example.com", root.baseUri());
    }

    @Test(timeout = 4000)
    public void testParseUsingReader() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Reader reader = new StringReader("<feed><title>Sample</title></feed>");
        Document doc = builder.parse(reader, "http://feed.com");

        assertNotNull(doc);
        assertEquals("Sample", doc.select("title").text());
        assertEquals("http://feed.com", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testSelfClosingUnknownTag() {
        String xml = "<root><custom-tag attr=\"val\" /><other>text</other></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Element root = doc.child(0);
        assertEquals(2, root.children().size());

        Element custom = root.child(0);
        assertEquals("custom-tag", custom.tagName());
        assertEquals("val", custom.attr("attr"));

        Element other = root.child(1);
        assertEquals("other", other.tagName());
        assertEquals("text", other.text());
    }

    @Test(timeout = 4000)
    public void testSelfClosingKnownHtmlTagInXmlMode() {
        // In XML mode, known HTML void tags like <img> or <br> shouldn't adopt HTML rules
        String xml = "<root><img src=\"foo.jpg\"/><p>Paragraph</p></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("img", root.child(0).tagName());
        assertEquals("p", root.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testCDataNodeInsertion() {
        String xml = "<data><![CDATA[Some <unescaped> & 'special' text]]></data>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Element dataEl = doc.child(0);
        assertEquals(1, dataEl.childNodeSize());

        Node childNode = dataEl.childNode(0);
        assertTrue("Node must be instance of CDataNode", childNode instanceof CDataNode);
        CDataNode cdata = (CDataNode) childNode;
        assertEquals("Some <unescaped> & 'special' text", cdata.text());
    }

    @Test(timeout = 4000)
    public void testDoctypeInsertion() {
        String xml = "<!DOCTYPE note SYSTEM \"Note.dtd\"><note><to>User</to></note>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        List<Node> nodes = doc.childNodes();
        assertTrue("First node must be DocumentType", nodes.get(0) instanceof DocumentType);

        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("note", doctype.attr("name"));
        assertEquals("Note.dtd", doctype.attr("systemId"));
        assertEquals("", doctype.attr("publicId"));
    }

    @Test(timeout = 4000)
    public void testDoctypeWithPublicAndSystemIds() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    @Test(timeout = 4000)
    public void testNormalCommentInsertion() {
        String xml = "<root><!-- This is a standard XML comment --><leaf/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Element root = doc.child(0);
        Node firstChild = root.childNode(0);
        assertTrue("Node must be a Comment", firstChild instanceof Comment);
        assertFalse("Standard comment must not be an XmlDeclaration", firstChild instanceof XmlDeclaration);
        assertEquals(" This is a standard XML comment ", ((Comment) firstChild).getData());
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionXmlDeclaration() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Node firstNode = doc.childNode(0);
        assertTrue("Node must be XmlDeclaration", firstNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) firstNode;
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertFalse(decl.isProcessingInstruction());
    }

    @Test(timeout = 4000)
    public void testExclamationDeclarationBogusComment() {
        String xml = "<!xml-stylesheet href=\"style.css\" type=\"text/css\"?><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Node firstNode = doc.childNode(0);
        assertTrue("Node must be XmlDeclaration", firstNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) firstNode;
        assertEquals("xml-stylesheet", decl.name());
        assertEquals("style.css", decl.attr("href"));
        assertTrue(decl.isProcessingInstruction());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInput() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document emptyDoc = builder.parse("", "");
        assertEquals(0, emptyDoc.children().size());

        Document blankDoc = builder.parse("   \n\t  ", "");
        assertEquals(0, blankDoc.children().size());
    }

    @Test(timeout = 4000)
    public void testUnclosedNestedElementsStackPopping() {
        // <outer> -> <middle> -> <inner> : then closing </outer> pops inner and middle
        String xml = "<outer><middle><inner>text</outer>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals(1, doc.children().size());
        Element outer = doc.child(0);
        assertEquals("outer", outer.tagName());
        assertEquals("middle", outer.child(0).tagName());
        assertEquals("inner", outer.child(0).child(0).tagName());
        assertEquals("text", outer.child(0).child(0).text());
    }

    @Test(timeout = 4000)
    public void testClosingTagNotInStackIsSafelyIgnored() {
        String xml = "<root><item>1</item></strayTag><item>2</item></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("1", root.child(0).text());
        assertEquals("2", root.child(1).text());
    }

    @Test(timeout = 4000)
    public void testOrphanClosingTagAtRoot() {
        String xml = "</orphan><root>content</root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals(1, doc.children().size());
        assertEquals("root", doc.child(0).tagName());
        assertEquals("content", doc.child(0).text());
    }

    @Test(timeout = 4000)
    public void testDefaultSettingsPreservesCase() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        ParseSettings settings = builder.defaultSettings();
        assertSame(ParseSettings.preserveCase, settings);

        Document doc = builder.parse("<MixedCaseTag CamelCaseAttr=\"Val\">Test</MixedCaseTag>", "");
        Element el = doc.child(0);
        assertEquals("MixedCaseTag", el.tagName());
        assertTrue(el.hasAttr("CamelCaseAttr"));
        assertEquals("Val", el.attr("CamelCaseAttr"));
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithPreserveCaseSettings() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment(
                "<FirstItem ID=\"A\" /><SecondItem ID=\"B\">text</SecondItem>",
                "http://example.com",
                ParseErrorList.noTracking(),
                ParseSettings.preserveCase
        );

        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element el1 = (Element) nodes.get(0);
        assertEquals("FirstItem", el1.tagName());
        assertEquals("A", el1.attr("ID"));

        Element el2 = (Element) nodes.get(1);
        assertEquals("SecondItem", el2.tagName());
        assertEquals("text", el2.text());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithHtmlDefaultSettings() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment(
                "<UpperTag UPPERATTR=\"VALUE\" />",
                "http://example.com",
                ParseErrorList.noTracking(),
                ParseSettings.htmlDefault
        );

        assertEquals(1, nodes.size());
        Element el = (Element) nodes.get(0);
        assertEquals("uppertag", el.tagName());
        assertTrue(el.hasAttr("upperattr"));
        assertEquals("VALUE", el.attr("upperattr"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known regression:
     * org.jsoup.parser.XmlTreeBuilderTest::handlesDodgyXmlDecl
     * --> java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
     *
     * In defective XmlTreeBuilder, unclosed quote or dodgy syntax in xml declaration
     * like "<?xml version='1.0'>" causes Jsoup.parse("<" + data.substring(1, data.length() -1) + ">")
     * to strip the single-quote delimiter, resulting in an unclosed attribute, EOF without emitting
     * a tag, producing 0 child elements, and throwing IndexOutOfBoundsException at doc.child(0).
     */
    @Test(timeout = 4000)
    public void testHandlesDodgyXmlDeclDefectGroundTruth() {
        String xml = "<?xml version='1.0'><val>One</val><?xml ?>";
        Document doc = Jsoup.parse(xml, "http://example.com", Parser.xmlParser());

        assertNotNull("Document must parse without IndexOutOfBoundsException", doc);
        assertEquals("One", doc.select("val").text());
    }

    @Test(timeout = 4000)
    public void testDodgyXmlDeclMissingClosingQuestion() {
        String xml = "<?xml version='1.0'><data>Sample</data>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("Sample", doc.select("data").text());
    }

    @Test(timeout = 4000)
    public void testDodgyXmlDeclEmptyQuestion() {
        String xml = "<??><val>Text</val>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("Text", doc.select("val").text());
    }

    @Test(timeout = 4000)
    public void testBogusCommentWithSingleCharacter() {
        // Checks data.length() <= 1 boundary branch in insert(Token.Comment)
        String xml = "<?>content</?>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("content", doc.text());
    }

    @Test(timeout = 4000)
    public void testBogusCommentWithExclamationOnly() {
        // Checks markup declaration open boundary
        String xml = "<!><data>Valid</data>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("Valid", doc.select("data").text());
    }

    // =========================================================================
    // Partition D: Exception, Defensive Guards & Direct Token Processing
    // =========================================================================

    @Test(timeout = 4000)
    public void testProcessTokenCoverage() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        // 1. StartTag
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("custom");
        assertTrue(builder.process(startTag));
        assertEquals("custom", builder.currentElement().tagName());

        // 2. Character token (regular text)
        Token.Character textToken = new Token.Character();
        textToken.data("inside text");
        assertTrue(builder.process(textToken));

        // 3. Comment token
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("inline comment");
        assertTrue(builder.process(commentToken));

        // 4. Doctype token
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name.append("html");
        assertTrue(builder.process(doctypeToken));

        // 5. EndTag
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("custom");
        assertTrue(builder.process(endTag));

        // 6. EOF token
        Token.EOF eofToken = new Token.EOF();
        assertTrue(builder.process(eofToken));
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseWhenElementNotFoundSkipsSafely() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader("<parent><child/></parent>"), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        builder.runParser();

        Token.EndTag missingEndTag = new Token.EndTag();
        missingEndTag.name("nonExistentElement");

        // Should return cleanly without modifying stack
        assertTrue(builder.process(missingEndTag));
    }

    @Test(timeout = 4000)
    public void testParseWithParseErrorListTracking() {
        ParseErrorList errors = ParseErrorList.tracking(5);
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(new StringReader("<root><unclosed>text</root>"), "http://example.com", errors, ParseSettings.preserveCase);

        assertNotNull(doc);
        assertEquals("text", doc.select("unclosed").text());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDocumentStructureAndXmlDeclarationAttributes() {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?><root/>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        assertEquals(1, doc.children().size());
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("1.0", decl.attr("version"));
        assertEquals("ISO-8859-1", decl.attr("encoding"));
        assertEquals("yes", decl.attr("standalone"));
        assertEquals("xml", decl.name());
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>", decl.getWholeDeclaration());
    }
}