package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for XmlTreeBuilder targeting maximum line/branch coverage and the known defect
 * regarding duplicate attributes and case-sensitive retention.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic and state transitions
 *   - process(StartTag), process(EndTag), process(Comment), process(Character), process(Doctype), process(EOF)
 *   - insert(StartTag) with self-closing vs normal, unknown tags
 *   - insert(Comment) with bogus xml declaration, normal comment
 *   - insert(Character) with CDATA and text
 *   - insert(Doctype)
 *   - popStackToClose: found, not found
 *   - insertNode, currentElement(), initialiseParse, parseFragment
 * - Partition B: Boundary value analysis
 *   - Empty input, single tag, nested tags, whitespace, null/empty baseUri
 *   - Multiple attributes with same/different case
 * - Partition C: Defect-targeted branch zone
 *   - Duplicate attributes (same key, same case): should drop later ones
 *   - Case-sensitive attributes: should preserve different-case keys
 * - Partition D: Exception & defensive paths
 *   - Default case in process (unreachable with normal tokens, but covered conceptually)
 * - Partition E: Object lifecycle – not directly applicable to TreeBuilder; document integrity via select
 */
public class XmlTreeBuilderDeepseekTest {

    // Helper to create a fresh XmlTreeBuilder and parse a string
    private Document parseXml(String input) {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        return builder.parse(input, "");
    }

    // --------------------- Partition A: Core Functional Logic ---------------------

    @Test(timeout = 4000)
    public void testSimpleStartEndTag() {
        Document doc = parseXml("<root><child></child></root>");
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.childrenSize());
        assertEquals("child", root.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testSelfClosingTag() {
        Document doc = parseXml("<br/>");
        Element br = doc.child(0);
        assertEquals("br", br.tagName());
        assertTrue(br.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testUnknownSelfClosingTag() {
        Document doc = parseXml("<myTag/>");
        Element el = doc.child(0);
        assertEquals("myTag", el.tagName());
        assertTrue(el.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testNormalStartTagWithAttributes() {
        Document doc = parseXml("<div id='x' class='y'>");
        Element div = doc.child(0);
        assertEquals("div", div.tagName());
        assertEquals("x", div.attr("id"));
        assertEquals("y", div.attr("class"));
    }

    @Test(timeout = 4000)
    public void testEndTagUnmatched() {
        // popStackToClose not found – should skip gracefully
        Document doc = parseXml("<a><b></c></a>");
        // The output is likely <a><b></b></a> because </c> is ignored
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertEquals("b", a.child(0).tagName());
        // No child for c
        assertEquals(1, a.childrenSize());
    }

    @Test(timeout = 4000)
    public void testComment() {
        Document doc = parseXml("<!-- test comment -->");
        List<Node> nodes = doc.childNodes();
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Comment);
        Comment comment = (Comment) nodes.get(0);
        assertEquals(" test comment ", comment.getData());
    }

    @Test(timeout = 4000)
    public void testBogusCommentAsXmlDeclaration() {
        Document doc = parseXml("<?xml version='1.0'?>");
        List<Node> nodes = doc.childNodes();
        assertEquals(1, nodes.size());
        // Should be an XmlDeclaration, not a Comment
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("version", decl.attr("version"));
        assertEquals("1.0", decl.attr("version"));
    }

    @Test(timeout = 4000)
    public void testCharacterText() {
        Document doc = parseXml("<p>Hello World</p>");
        Element p = doc.child(0);
        assertEquals("Hello World", p.text());
        assertEquals(1, p.childNodes().size());
        assertTrue(p.childNode(0) instanceof TextNode);
    }

    @Test(timeout = 4000)
    public void testCharacterCData() {
        Document doc = parseXml("<root><![CDATA[<greeting>]]></root>");
        Element root = doc.child(0);
        assertEquals(1, root.childNodes().size());
        assertTrue(root.childNode(0) instanceof CDataNode);
        CDataNode cdata = (CDataNode) root.childNode(0);
        assertEquals("<greeting>", cdata.getWholeText());
    }

    @Test(timeout = 4000)
    public void testDoctype() {
        Document doc = parseXml("<!DOCTYPE root PUBLIC 'pubid' 'sysid'>");
        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("root", doctype.name());
        assertEquals("pubid", doctype.publicId());
        assertEquals("sysid", doctype.systemId());
    }

    @Test(timeout = 4000)
    public void testEOF() {
        // Parsing empty input should still produce a document with no children
        Document doc = parseXml("");
        assertEquals(0, doc.childrenSize());
    }

    // --------------------- Partition B: Boundary Value Analysis ---------------------

    @Test(timeout = 4000)
    public void testEmptyInput() {
        Document doc = parseXml("");
        assertEquals(0, doc.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnly() {
        Document doc = parseXml("   ");
        assertEquals(1, doc.childNodes().size());
        assertTrue(doc.childNode(0) instanceof TextNode);
        assertEquals("   ", ((TextNode)doc.childNode(0)).getWholeText());
    }

    @Test(timeout = 4000)
    public void testNestedTags() {
        Document doc = parseXml("<a><b><c>text</c></b></a>");
        Element a = doc.child(0);
        Element b = a.child(0);
        Element c = b.child(0);
        assertEquals("a", a.tagName());
        assertEquals("b", b.tagName());
        assertEquals("c", c.tagName());
        assertEquals("text", c.text());
    }

    @Test(timeout = 4000)
    public void testMultipleAttributesSameElement() {
        Document doc = parseXml("<p one='1' two='2' three='3'>");
        Element p = doc.child(0);
        assertEquals("1", p.attr("one"));
        assertEquals("2", p.attr("two"));
        assertEquals("3", p.attr("three"));
        assertEquals(3, p.attributes().size());
    }

    @Test(timeout = 4000)
    public void testNullBaseUri() {
        // null baseUri should not cause NPE; default to empty string
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root/>", null);
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
    }

    // --------------------- Partition C: Defect-Targeted Branch Zone ---------------------
    // The known defect: duplicate attributes (same case) are not dropped; different case keys preserved.
    @Test(timeout = 4000)
    public void testDropsDuplicateAttributes() {
        // same key repeated with same case -> should keep only first
        String xml = "<p one='One' one='Two' one='Three'>Text</p>";
        Document doc = parseXml(xml);
        Element p = doc.child(0);
        // After deduplication, only first 'one' should remain
        assertEquals("One", p.attr("one"));
        // Ensure only one attribute with key 'one'
        assertEquals(1, p.attributes().get("one").size());
        // Also check total attribute count (should be exactly 1)
        assertEquals(1, p.attributes().size());
        // Text content preserved
        assertEquals("Text", p.text());
    }

    @Test(timeout = 4000)
    public void testRetainsAttributesOfDifferentCaseIfSensitive() {
        // Keys differ only in case: all should be preserved
        String xml = "<p One='One' ONE='Two' one='Three' Two='Four'>Text</p>";
        Document doc = parseXml(xml);
        Element p = doc.child(0);
        // All three case-variants of 'one' should exist with their first value
        assertEquals("One", p.attr("One"));
        assertEquals("Two", p.attr("ONE"));
        assertEquals("Three", p.attr("one"));
        assertEquals("Four", p.attr("Two"));
        // Total unique keys: 4 (One, ONE, one, Two)
        assertEquals(4, p.attributes().size());
        // Ensure no duplication per key (each key appears once)
        assertEquals(1, p.attributes().get("One").size());
        assertEquals(1, p.attributes().get("ONE").size());
        assertEquals(1, p.attributes().get("one").size());
        assertEquals(1, p.attributes().get("Two").size());
    }

    // Additional combination: case-sensitive plus actual duplicates
    @Test(timeout = 4000)
    public void testDuplicateAndCaseSensitiveMixed() {
        String xml = "<p a='1' A='2' a='3' b='4' b='5'>Text</p>";
        Document doc = parseXml(xml);
        Element p = doc.child(0);
        // 'a' (lowercase) first value: '1' (should drop later '3')
        assertEquals("1", p.attr("a"));
        // 'A' (uppercase) should be preserved with '2'
        assertEquals("2", p.attr("A"));
        // 'b' should drop duplicate '5' and keep '4'
        assertEquals("4", p.attr("b"));
        // Total unique keys: a, A, b => 3
        assertEquals(3, p.attributes().size());
    }

    // --------------------- Partition D: Exception & Defensive Paths ---------------------
    // Default case in process (unreachable via normal parsing; we can't easily trigger it,
    // but we can test that other tokens don't throw)

    @Test(timeout = 4000)
    public void testProcessAllTokenTypes() {
        // This test indirectly covers all token types by parsing a varied XML
        Document doc = parseXml("<?xml version='1.0'?><!-- comment --><root><![CDATA[<]]></root>");
        // just ensure no exception
        assertNotNull(doc);
    }

    // No null token tests because the code assumes non-null tokens from the parser.

    // --------------------- Partition E: Object Lifecycle & Contract Integrity ---------------------
    @Test(timeout = 4000)
    public void testParseFragment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Parser parser = new Parser(builder);
        List<Node> nodes = builder.parseFragment("<item>value</item>", "", parser);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("item", ((Element)nodes.get(0)).tagName());
        assertEquals("value", ((Element)nodes.get(0)).text());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithContext() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Parser parser = new Parser(builder);
        Element context = new Element(Tag.valueOf("div"), "");
        List<Node> nodes = builder.parseFragment("<span>text</span>", context, "", parser);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("span", ((Element)nodes.get(0)).tagName());
    }

    @Test(timeout = 4000)
    public void testOutputSettingsSyntax() {
        Document doc = parseXml("<test/>");
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test(timeout = 4000)
    public void testDefaultSettingsPreserveCase() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        assertSame(ParseSettings.preserveCase, builder.defaultSettings());
    }
}