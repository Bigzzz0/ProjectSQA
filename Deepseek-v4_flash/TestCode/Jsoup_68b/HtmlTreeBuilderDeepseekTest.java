package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
import org.jsoup.parser.HtmlTreeBuilderState;

import java.util.ArrayList;
import java.util.List;

/**
 * White-box JUnit 4 test suite for HtmlTreeBuilder.
 * Targets line/branch coverage and the known Defects4J StackOverflowError defect.
 *
 * [Branch & Defect Analysis Matrix]
 * Partition A: Core functional logic – state transitions, element insertion, stack ops, formatting elements.
 * Partition B: Boundary Value Analysis – null context, empty fragments, max depth, large nesting.
 * Partition C: Defect-targeted – deep nested spans triggering StackOverflowError (Defects4J #).
 * Partition D: Exception/defensive guard paths – invalid arguments, error conditions.
 * Partition E: Object lifecycle – toString, state queries.
 */
public class HtmlTreeBuilderDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testInitialState() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testSimpleTag() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testSelfClosingTag() {
        Document doc = Jsoup.parse("<br><hr>");
        assertEquals(2, doc.body().childrenSize());
    }

    @Test(timeout = 4000)
    public void testFormattingElements() {
        // <b><i></i></b> should be properly reconstructed
        Document doc = Jsoup.parse("<b><i>text</i></b>");
        assertEquals("text", doc.body().text());
        assertEquals("b", doc.body().child(0).tagName());
        assertEquals("i", doc.body().child(0).child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testFosterInserts() {
        // Misplaced inline inside table should foster-parent
        String html = "<table><b>text</b></table>";
         Document doc = Jsoup.parse(html);
        // <b> should be before <table> (foster)
        assertEquals("b", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testListImplicitClosure() {
        // <ul><li>one<li>two should close first <li> automatically
        Document doc = Jsoup.parse("<ul><li>one<li>two");
        assertEquals(2, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        // <p>text<p>more should close first <p>
        Document doc = Jsoup.parse("<p>one<p>two");
        assertEquals(2, doc.select("p").size());
    }

    @Test(timeout = 4000)
    public void testResetInsertionModeFromBody() {
        // Fragment with context "body" should reset to InBody
        Parser parser = Parser.htmlParser();
        List<Node> nodes = parser.parseFragment("<div>", new Element("body"), "");
        assertEquals(1, nodes.size());
    }

    @Test(timeout = 4000)
    public void testSpecialTag() {
        // <script> content should be DataNode, not TextNode
        Document doc = Jsoup.parse("<script>alert(1)</script>");
        assertEquals(1, doc.select("script").size());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullContext() {
        Parser parser = Parser.htmlParser();
        List<Node> nodes = parser.parseFragment("<span>", null, "");
        assertNotNull(nodes);
        assertTrue(nodes.size() > 0);
    }

    @Test(timeout = 4000)
    public void testEmptyFragment() {
        Parser parser = Parser.htmlParser();
        List<Node> nodes = parser.parseFragment("", new Element("div"), "");
        assertTrue(nodes.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMaxDepthSearchScope() {
        // Ensure that inSpecificScope does not cause infinite loop or overrun
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("<div>");
        }
        for (int i = 0; i < 200; i++) {
            sb.append("</div>");
        }
        Document doc = Jsoup.parse(sb.toString());
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testLargeAttributes() {
        StringBuilder attr = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            attr.append(" a").append(i).append("=\"").append(i).append("\"");
        }
        Document doc = Jsoup.parse("<p" + attr.toString() + ">text</p>");
        assertEquals("text", doc.body().text());
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Deep Nested Spans)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDeeplyNestedSpans() {
        // Known Defects4J defect: StackOverflowError on deeply nested spans.
        // This test must complete without error on a fixed version.
        int depth = 3000; // trigger stack overflow in buggy version
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            html.append("<span>");
        }
        for (int i = 0; i < depth; i++) {
            html.append("</span>");
        }
        try {
            Document doc = Jsoup.parse(html.toString());
            // If we reach here, no StackOverflowError (fixed version)
            assertNotNull(doc);
            // Optionally verify structure depth
            Element current = doc.body().child(0);
            int actualDepth = 0;
            while (current != null && current.tagName().equals("span")) {
                actualDepth++;
                current = current.children().size() > 0 ? current.child(0) : null;
            }
            assertTrue("Depth should be at least " + depth + ", got " + actualDepth, actualDepth >= depth);
        } catch (StackOverflowError e) {
            fail("StackOverflowError should not occur: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidBaseUri() {
        // baseUri may not be null in Jsoup, but blank could be handled
        Jsoup.parse("<p>test</p>", ""); // ok
    }

    @Test(timeout = 4000)
    public void testTokenErrorHandling() {
        // Broken HTML like <tag< should not throw uncaught exception
        Document doc = Jsoup.parse("<p<test");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testScriptDataHandling() {
        // Script tag with </script> inside should be parsed as data
        Document doc = Jsoup.parse("<script>document.write(\"</script>\");</script>");
        assertTrue(doc.select("script").first().data().contains("</script>"));
    }

    @Test(timeout = 4000)
    public void testInSelectScope() {
        // In <select>, only <option> and <optgroup> are in select scope
        Document doc = Jsoup.parse("<select><option>1<option>2");
        assertEquals(2, doc.select("option").size());
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testToString() {
        // Basic sanity: toString does not crash
        Document doc = Jsoup.parse("<p>text</p>");
        assertNotNull(doc.toString());
        assertTrue(doc.toString().length() > 0);
    }

    @Test(timeout = 4000)
    public void testGetHeadElement() {
        Document doc = Jsoup.parse("<head><title>test</title></head>");
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testGetFormElementAfterParse() {
        Document doc = Jsoup.parse("<form><input></form>");
        assertEquals("form", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testOnStackAfterParsing() {
        // Check that elements are on stack during parsing (internal, but we can verify structure)
        Document doc = Jsoup.parse("<html><body><div id='d'></div></body></html>");
        assertNotNull(doc.getElementById("d"));
    }

    @Test(timeout = 4000)
    public void testFramesetOk() {
        // Frameset detection: <frameset> should transition to InFrameset
        Document doc = Jsoup.parse("<frameset><frame></frameset>");
        assertNotNull(doc);
    }
}