package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.XmlTreeBuilder
 *
 * 1. Defect-Targeted Zone:
 *    - Duplicate Attribute Overwrite: Defects4J issue where duplicate XML/HTML attributes
 *      were erroneously overwritten by subsequent occurrences instead of preserving the
 *      first occurrence (e.g., <p One="One" ... One="Four">).
 *    - Case Preservation: Verification that XML attribute preservation keeps differing-case
 *      attributes distinct while discarding identical-case duplicates.
 *
 * 2. Decision & Branch Coverage Points:
 *    - initialiseParse: Verifies doc placed on stack and syntax set to Document.OutputSettings.Syntax.xml.
 *    - defaultSettings: Returns ParseSettings.preserveCase.
 *    - process(Token):
 *        * StartTag -> insert(StartTag)
 *        * EndTag -> popStackToClose(EndTag)
 *        * Comment -> insert(Comment) [normal comment vs. bogus xml declaration]
 *        * Character -> insert(Character) [standard text vs. CDataNode]
 *        * Doctype -> insert(Doctype) [system/public ID, pubSysKey]
 *        * EOF -> no-op normalisation
 *    - insert(StartTag):
 *        * startTag.isSelfClosing() & !tag.isKnownTag() -> tag.setSelfClosing()
 *        * startTag.isSelfClosing() & tag.isKnownTag() -> remains known
 *        * !startTag.isSelfClosing() -> stack.add(el)
 *    - insert(Comment):
 *        * normal comment: bogus == false
 *        * bogus == true & isXmlDeclaration() == true & asXmlDeclaration() != null
 *        * bogus == true & isXmlDeclaration() == false or asXmlDeclaration() == null
 *    - insert(Character):
 *        * isCData() == true -> CDataNode
 *        * isCData() == false -> TextNode
 *    - popStackToClose(EndTag):
 *        * Tag not found in stack -> skips (firstFound == null)
 *        * Tag found at top of stack -> pops 1 element
 *        * Tag found deeper in stack -> pops multiple unclosed child elements
 *    - parseFragment:
 *        * 3-arg: (inputFragment, baseUri, parser)
 *        * 4-arg: (inputFragment, context, baseUri, parser) -> delegates to 3-arg
 */
public class XmlTreeBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultSettingsPreservesCase() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        ParseSettings settings = treeBuilder.defaultSettings();
        assertNotNull(settings);
        assertEquals(ParseSettings.preserveCase, settings);
        assertTrue(settings.preserveTagCase());
        assertTrue(settings.preserveAttributeCase());
    }

    @Test(timeout = 4000)
    public void testParseReaderAndOutputSettingsXml() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        String xml = "<root><child id=\"1\">Content</child></root>";
        Document doc = treeBuilder.parse(new StringReader(xml), "https://example.com/");

        assertNotNull(doc);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        assertEquals("https://example.com/", doc.baseUri());
        assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("1", child.attr("id"));
        assertEquals("Content", child.text());
    }

    @Test(timeout = 4000)
    public void testParseStringMethod() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<msg status=\"ok\">Hello</msg>", "https://example.com");
        assertEquals("msg", doc.child(0).tagName());
        assertEquals("ok", doc.child(0).attr("status"));
        assertEquals("Hello", doc.child(0).text());
    }

    @Test(timeout = 4000)
    public void testStandardCommentInsertion() {
        String xml = "<root><!-- This is a comment --></root>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        Node childNode = root.childNode(0);
        assertTrue(childNode instanceof Comment);
        Comment comment = (Comment) childNode;
        assertEquals(" This is a comment ", comment.getData());
        assertFalse(comment.isXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testXmlDeclarationInsertion() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        List<Node> childNodes = doc.childNodes();
        assertTrue(childNodes.size() >= 2);

        Node firstNode = childNodes.get(0);
        assertTrue(firstNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) firstNode;
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
    }

    @Test(timeout = 4000)
    public void testCDataSectionInsertion() {
        String xml = "<root><![CDATA[<unescaped & raw data>]]></root>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        Node childNode = root.childNode(0);
        assertTrue(childNode instanceof CDataNode);
        CDataNode cdata = (CDataNode) childNode;
        assertEquals("<unescaped & raw data>", cdata.text());
    }

    @Test(timeout = 4000)
    public void testDocumentTypeInsertion() {
        String xml = "<!DOCTYPE note SYSTEM \"Note.dtd\"><note><to>User</to></note>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        List<Node> childNodes = doc.childNodes();
        assertTrue(childNodes.size() >= 2);

        Node doctypeNode = childNodes.get(0);
        assertTrue(doctypeNode instanceof DocumentType);
        DocumentType doctype = (DocumentType) doctypeNode;
        assertEquals("note", doctype.name());
        assertEquals("Note.dtd", doctype.attr("systemId"));
        assertEquals("SYSTEM", doctype.getPubSysKey());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInput() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document docEmpty = treeBuilder.parse("", "");
        assertNotNull(docEmpty);
        assertEquals(0, docEmpty.childNodeSize());

        Document docBlank = treeBuilder.parse("   \n\t  ", "");
        assertNotNull(docBlank);
        assertTrue(docBlank.childNodeSize() > 0);
        assertTrue(docBlank.childNode(0) instanceof TextNode);
    }

    @Test(timeout = 4000)
    public void testSelfClosingUnknownTag() {
        String xml = "<root><custom-tag attr=\"val\"/></root>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        Element root = doc.child(0);
        assertEquals(1, root.children().size());
        Element custom = root.child(0);
        assertEquals("custom-tag", custom.tagName());
        assertEquals("val", custom.attr("attr"));
        assertTrue(custom.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testSelfClosingKnownHtmlTagInXml() {
        // Tag "img" is known in HTML tag table. In XML mode, if self closing, verify behavior.
        String xml = "<root><img src=\"test.png\"/><br/></root>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        Element root = doc.child(0);
        assertEquals(2, root.children().size());
        assertEquals("img", root.child(0).tagName());
        assertEquals("br", root.child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testClosingNonExistentTagSkipped() {
        String xml = "<root><child>text</unopened></child></root>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("child", root.child(0).tagName());
        assertEquals("text", root.child(0).text());
    }

    @Test(timeout = 4000)
    public void testUnclosedChildElementsPoppedByParentClose() {
        // Closing <parent> should pop <nested2> and <nested1> off stack
        String xml = "<parent><nested1><nested2>Deep content</parent>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        assertEquals(1, doc.children().size());
        Element parent = doc.child(0);
        assertEquals("parent", parent.tagName());
        Element nested1 = parent.child(0);
        assertEquals("nested1", nested1.tagName());
        Element nested2 = nested1.child(0);
        assertEquals("nested2", nested2.tagName());
        assertEquals("Deep content", nested2.text());
    }

    @Test(timeout = 4000)
    public void testParseFragmentThreeArgs() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Parser parser = new Parser(treeBuilder);
        List<Node> nodes = treeBuilder.parseFragment("<one>1</one><two>2</two>", "http://example.com", parser);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("one", ((Element) nodes.get(0)).tagName());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("two", ((Element) nodes.get(1)).tagName());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithContextDelegation() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Parser parser = new Parser(treeBuilder);
        Element context = new Element(Tag.valueOf("dummy"), "");
        List<Node> nodes = treeBuilder.parseFragment("<item>test</item>", context, "http://example.com", parser);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("item", ((Element) nodes.get(0)).tagName());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J regression where duplicate attributes were erroneously overwritten
     * by later occurrences rather than preserving the first occurrence.
     * Also tests case-sensitive attribute preservation in XML mode.
     */
    @Test(timeout = 4000)
    public void testDropsDuplicateAttributesTargetingDefect() {
        String xml = "<p One=\"One\" ONE=\"Two\" one=\"Three\" One=\"Four\" ONE=\"Five\" two=\"Six\" two=\"Seven\" Two=\"Eight\">Text</p>";
        Parser parser = Parser.xmlParser();
        Document doc = parser.parseInput(xml, "");
        Element p = doc.select("p").first();
        assertNotNull(p);

        // Expected according to XML / HTML specifications:
        // First occurrence must be retained; subsequent duplicate occurrences of identical case must be dropped.
        String expectedOuterHtml = "<p One=\"One\" ONE=\"Two\" one=\"Three\" two=\"Six\" Two=\"Eight\">Text</p>";
        assertEquals(expectedOuterHtml, p.outerHtml());

        // Also assert individual attribute values
        assertEquals("One", p.attr("One"));
        assertEquals("Two", p.attr("ONE"));
        assertEquals("Three", p.attr("one"));
        assertEquals("Six", p.attr("two"));
        assertEquals("Eight", p.attr("Two"));
    }

    @Test(timeout = 4000)
    public void testRetainsAttributesOfDifferentCaseIfSensitive() {
        String xml = "<item case=\"lower\" CASE=\"upper\" Case=\"camel\">data</item>";
        Parser parser = Parser.xmlParser();
        Document doc = parser.parseInput(xml, "");
        Element item = doc.select("item").first();
        assertNotNull(item);

        assertEquals("lower", item.attr("case"));
        assertEquals("upper", item.attr("CASE"));
        assertEquals("camel", item.attr("Case"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testBogusCommentNotXmlDeclaration() {
        // '<?!' starts a bogus comment in Tokeniser, but is not a valid XmlDeclaration
        String xml = "<?!invalid xml decl>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        assertEquals(1, doc.childNodeSize());
        Node node = doc.childNode(0);
        assertTrue(node instanceof Comment);
        assertFalse(node instanceof XmlDeclaration);
    }

    @Test(timeout = 4000)
    public void testMalformedProcessingInstructionBecomesComment() {
        String xml = "<?invalid?><root/>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.size() >= 2);
        assertTrue(nodes.get(0) instanceof Comment || nodes.get(0) instanceof XmlDeclaration);
    }

    @Test(timeout = 4000)
    public void testInterleavedTagsAutoCorrectNesting() {
        String xml = "<x><a><b></x></b></a>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        // In XML tree builder, </x> pops <b>, <a>, and <x>
        Element x = doc.child(0);
        assertEquals("x", x.tagName());
        Element a = x.child(0);
        assertEquals("a", a.tagName());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithPublicAndSystemIds() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html/>";
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse(xml, "");
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.name());
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
        assertEquals("PUBLIC", doctype.getPubSysKey());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & State Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testReusabilityOfXmlTreeBuilderInstance() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc1 = treeBuilder.parse("<first>1</first>", "");
        assertEquals("first", doc1.child(0).tagName());
        assertEquals("1", doc1.child(0).text());

        Document doc2 = treeBuilder.parse("<second>2</second>", "");
        assertEquals("second", doc2.child(0).tagName());
        assertEquals("2", doc2.child(0).text());
        // Verify doc2 does not retain remnants from doc1
        assertEquals(1, doc2.children().size());
    }
}