package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.DocumentType;

/**
 * [Branch & Defect Analysis Matrix]
 * Target: XmlTreeBuilder
 * Branches:
 * - process(): switch on token.type (StartTag, EndTag, Comment, Character, Doctype, EOF, default)
 * - insert(StartTag): if startTag.isSelfClosing() then if !tag.isKnownTag() setSelfClosing()
 * - popStackToClose(): find firstFound; if null return; else loop remove until firstFound
 * - insertNode(): calls currentElement().appendChild(node)
 * - initialiseParse(): adds doc to stack
 * Defect: XML declaration (<?xml ... ?>) is incorrectly parsed as comment (<!-- ... -->)
 * Tests target each branch and the defect.
 */
public class XmlTreeBuilderDeepseekTest {

    @Test(timeout = 4000)
    public void testSimpleXml() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child>text</child></root>", "http://example.com");
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("text", child.text());
    }

    @Test(timeout = 4000)
    public void testSelfClosingTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<br/><root/>", "http://example.com");
        Element br = doc.child(0);
        assertEquals("br", br.tagName());
        assertTrue(br.tag().isSelfClosing());
        Element root = doc.child(1);
        assertEquals("root", root.tagName());
        assertTrue(root.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testComment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<!-- comment --><root/>", "http://example.com");
        assertTrue(doc.childNode(0) instanceof Comment);
        Comment comment = (Comment) doc.childNode(0);
        assertEquals(" comment ", comment.getData());
    }

    @Test(timeout = 4000)
    public void testDoctype() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<!DOCTYPE root><root/>", "http://example.com");
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("root", doctype.name());
    }

    @Test(timeout = 4000)
    public void testCharacter() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("text", "http://example.com");
        assertTrue(doc.childNode(0) instanceof TextNode);
        TextNode text = (TextNode) doc.childNode(0);
        assertEquals("text", text.text());
    }

    @Test(timeout = 4000)
    public void testEndTagNotFound() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child></other></root>", "http://example.com");
        Element root = doc.child(0);
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals(0, child.childrenSize());
    }

    @Test(timeout = 4000)
    public void testEndTagFound() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child></child></root>", "http://example.com");
        Element root = doc.child(0);
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals(0, child.childrenSize());
    }

    @Test(timeout = 4000)
    public void testMultipleEndTags() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><a><b></b></a></root>", "http://example.com");
        Element root = doc.child(0);
        Element a = root.child(0);
        assertEquals("a", a.tagName());
        Element b = a.child(0);
        assertEquals("b", b.tagName());
    }

    @Test(timeout = 4000)
    public void testXmlDeclarationAsDeclaration() {
        // Targets the known defect: XML declaration should not be turned into a comment.
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<?xml version='1.0' encoding='UTF-8'?><body>One</body>", "http://example.com");
        String output = doc.toString().trim();
        assertTrue("Output should contain XML declaration", output.contains("<?xml"));
        assertFalse("Output should not contain comment delimiters", output.contains("<!--"));
        assertFalse(doc.childNode(0) instanceof Comment);
    }

    @Test(timeout = 4000)
    public void testEmptyInput() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("", "http://example.com");
        assertEquals(0, doc.childrenSize());
    }

    @Test(timeout = 4000)
    public void testUnknownTagSelfClosing() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<customtag/>", "http://example.com");
        Element el = doc.child(0);
        assertEquals("customtag", el.tagName());
        assertTrue(el.tag().isSelfClosing());
        assertFalse(el.tag().isKnownTag());
    }

    @Test(timeout = 4000)
    public void testNonSelfClosingUnknownTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<customtag></customtag>", "http://example.com");
        Element el = doc.child(0);
        assertEquals("customtag", el.tagName());
        assertFalse(el.tag().isSelfClosing());
        assertFalse(el.tag().isKnownTag());
    }

    @Test(timeout = 4000)
    public void testKnownSelfClosingTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<br/>", "http://example.com");
        Element br = doc.child(0);
        assertEquals("br", br.tagName());
        assertTrue(br.tag().isSelfClosing());
        assertTrue(br.tag().isKnownTag());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseMultipleSameName() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><a><a></a></a></root>", "http://example.com");
        Element root = doc.child(0);
        Element outerA = root.child(0);
        assertEquals("a", outerA.tagName());
        assertEquals(0, outerA.childrenSize());
    }
}