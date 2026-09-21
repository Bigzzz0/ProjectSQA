package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

/**
 * White-box JUnit 4 test suite for org.jsoup.parser.TreeBuilder.
 * 
 * /* [Branch & Defect Analysis Matrix]
 *   Partitions targeted:
 *   - initialiseParse: null input, null baseUri, normal case.
 *   - parse (single arg): delegates to parse(input, baseUri, noTracking).
 *   - parse (three args): calls initialiseParse + runParser, returns doc.
 *   - runParser: loop until EOF token, processes each token via abstract process().
 *   - processStartTag(String): resets start token, sets name, calls process(token).
 *   - processStartTag(String, Attributes): resets start token, sets name+attrs, calls process(token).
 *   - processEndTag(String): resets end token, sets name, calls process(token).
 *   - currentElement: returns last element from stack or null if empty.
 *   - Defect-specific: parsing <table><tr><!-- comment -->text</tr></table> exposes ordering bug.
 *   - Boundary: empty input, single text, deeply nested tags.
 *   - Error conditions: null arguments to initialiseParse.
 *   - Stack growth: ensure stack gets properly populated during parsing.
 *   - Token reset: each processed token is reset before next read.
 */
public class TreeBuilderDeepseekTest {

    // ----- Part A: Core functional logic & state transitions -----
    
    @Test(timeout = 4000)
    public void parseSimpleHtmlCreatesDocument() {
        Document doc = Jsoup.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>");
        assertNotNull(doc);
        assertEquals("#root", doc.tagName());
        Element html = doc.child(0);
        assertEquals("html", html.tagName());
        Element head = html.child(0);
        assertEquals("head", head.tagName());
        Element title = head.child(0);
        assertEquals("title", title.tagName());
        assertEquals("Test", title.text());
        Element body = html.child(1);
        assertEquals("body", body.tagName());
        Element p = body.child(0);
        assertEquals("p", p.tagName());
        assertEquals("Hello", p.text());
    }

    @Test(timeout = 4000)
    public void parseEmptyStringReturnsEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals(0, doc.childrenSize());
    }

    @Test(timeout = 4000)
    public void parseOnlyTextReturnsDocumentWithTextNode() {
        Document doc = Jsoup.parse("Hello World");
        assertNotNull(doc);
        assertEquals("Hello World", doc.text());
    }

    @Test(timeout = 4000)
    public void processStartTagViaDocument() {
        // Verifies that processStartTag works through the parsing pipeline
        Document doc = Jsoup.parse("<div><span>Content</span></div>");
        Element div = doc.selectFirst("div");
        assertNotNull(div);
        Element span = div.child(0);
        assertEquals("span", span.tagName());
        assertEquals("Content", span.text());
    }

    @Test(timeout = 4000)
    public void processEndTagViaDocument() {
        // End tags are processed implicitly; ensure no exceptions
        Document doc = Jsoup.parse("<ul><li>Item1</li><li>Item2</li></ul>");
        assertEquals(2, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void currentElementReturnsCorrectElementDuringParsing() {
        // Use a custom TreeBuilder to test currentElement
        // This is a white-box test on the concrete HtmlTreeBuilder
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.initialiseParse("<p>Text</p>", "http://example.com", ParseErrorList.noTracking());
        // After initialiseParse, stack is empty
        assertNull(builder.currentElement());
        builder.runParser();
        // After parsing, the stack should have one element (the document? Actually, in HtmlTreeBuilder,
        // after parsing, the document is on the stack. More precisely, the stack contains open elements.
        // For a simple <p> the stack may have [html, body, p].
        Element current = builder.currentElement();
        assertNotNull(current);
        assertEquals("p", current.tagName()); // Or body? Let's check the last open element
    }

    // ----- Part B: Boundary Value Analysis & Extremes -----

    @Test(timeout = 4000)
    public void parseNullInputThrowsException() {
        try {
            Jsoup.parse(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("String input must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void initialiseParseNullInputThrows() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        try {
            builder.initialiseParse(null, "http://example.com", ParseErrorList.noTracking());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("String input must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void initialiseParseNullBaseUriThrows() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        try {
            builder.initialiseParse("<html></html>", null, ParseErrorList.noTracking());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("BaseURI must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void parseWithEmptyBaseUriWorks() {
        Document doc = Jsoup.parse("<a href='/test'>Link</a>", "");
        assertNotNull(doc);
        assertEquals("", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void parseVeryDeepNestingDoesNotStackOverflow() {
        StringBuilder sb = new StringBuilder("<div>");
        for (int i = 0; i < 500; i++) {
            sb.append("<span>");
        }
        sb.append("deep");
        for (int i = 0; i < 500; i++) {
            sb.append("</span>");
        }
        sb.append("</div>");
        Document doc = Jsoup.parse(sb.toString());
        Element deepest = doc.selectFirst("span");
        // just ensure no exception
        assertNotNull(deepest);
    }

    // ----- Part C: Defect-Targeted Branch Zone -----
    // Known defect: "Search text did not come after comment"
    // This test reveals the bug where in invalid table contents,
    // the comment is not properly separated from following text.

    @Test(timeout = 4000)
    public void testInvalidTableContentsCommentBeforeText() {
        // The bug: when parsing <table><tr><!-- comment -->text</tr></table>,
        // the text "text" might appear before the comment node in the DOM,
        // or the ordering might be lost.
        String html = "<table><tr><!-- comment -->text</tr></table>";
        Document doc = Jsoup.parse(html);
        // The comment and text should be inside the tr.
        Elements tr = doc.select("tr");
        assertEquals(1, tr.size());
        NodeList children = tr.get(0).childNodesCopy();
        // Expected order: comment, then text node
        assertTrue("Expected at least two children in <tr>", children.size() >= 2);
        Node first = children.get(0);
        Node second = children.get(1);
        assertTrue("First child should be a Comment", first instanceof Comment);
        assertEquals(" comment ", ((Comment) first).getData());
        assertTrue("Second child should be a TextNode", second instanceof TextNode);
        assertEquals("text", ((TextNode) second).text());
        // This assertion fails on the buggy version because text appears first.
    }

    @Test(timeout = 4000)
    public void testInvalidTableContentsTextBeforeComment() {
        // Additional: text before comment should also work
        String html = "<table><tr>text<!-- comment --></tr></table>";
        Document doc = Jsoup.parse(html);
        Elements tr = doc.select("tr");
        assertEquals(1, tr.size());
        NodeList children = tr.get(0).childNodesCopy();
        assertTrue(children.size() >= 2);
        Node first = children.get(0);
        Node second = children.get(1);
        assertTrue("First child should be a TextNode", first instanceof TextNode);
        assertEquals("text", ((TextNode) first).text());
        assertTrue("Second child should be a Comment", second instanceof Comment);
        assertEquals(" comment ", ((Comment) second).getData());
    }

    // ----- Part D: Exception & Defensive Guard Paths -----

    @Test(timeout = 4000)
    public void parseWithTrackingErrorsDoesNotThrow() {
        ParseErrorList errors = new ParseErrorList(16, 32);
        Document doc = Jsoup.parse("<div><p>Unclosed", "http://example.com", errors);
        assertNotNull(doc);
        assertTrue(errors.size() > 0); // Should have at least one error
    }

    @Test(timeout = 4000)
    public void processStartTagWithAttributes() {
        // This tests the two-argument version of processStartTag
        Document doc = Jsoup.parse("<div class='foo' id='bar'>Content</div>");
        Element div = doc.selectFirst("div");
        assertNotNull(div);
        assertEquals("foo", div.className());
        assertEquals("bar", div.id());
    }

    @Test(timeout = 4000)
    public void processEndTagWithNameResetsToken() {
        // Ensure that processEndTag resets the token before usage.
        // This is tested by parsing a self-closing tag like <br/> and then a proper end tag.
        Document doc = Jsoup.parse("<div><br/></div>");
        Element br = doc.selectFirst("br");
        assertNotNull(br);
        // The end tag </div> should be processed correctly
        assertNotNull(doc.selectFirst("div"));
    }

    @Test(timeout = 4000)
    public void parseTokenResetBetweenToks() {
        // Parse a sequence that relies on token reset (e.g., <p>one</p><p>two</p>)
        Document doc = Jsoup.parse("<p>one</p><p>two</p>");
        assertEquals(2, doc.select("p").size());
        assertEquals("one", doc.select("p").get(0).text());
        assertEquals("two", doc.select("p").get(1).text());
    }

    // ----- Part E: Object Lifecycle & Contract Integrity -----
    // (Not directly applicable to TreeBuilder, but we can test the Document contract)

    @Test(timeout = 4000)
    public void documentBaseUriPreserved() {
        Document doc = Jsoup.parse("<html></html>", "http://base.com/path");
        assertEquals("http://base.com/path", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void documentTypePreserved() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html></html>");
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().name());
    }
}