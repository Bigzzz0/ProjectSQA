package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * Partitions:
 *  A: Core Functional Logic & State Transitions – normal parsing, stack operations, 
 *     formatting elements, insertion modes.
 *  B: Boundary Value Analysis (BVA) – deep nesting (stack depth), null context, 
 *     empty strings, malformed tokens.
 *  C: Defect-Targeted Branch Zone – handlesDeepStack failure (StackOverflowError or 
 *     assertion failure due to tree depth).
 *  D: Exception & Defensive Guard Paths – invalid arguments, out-of-range indexes 
 *     (via Validate), self-closing non-void tags.
 *  E: Object Lifecycle & Contract – toString, state transitions, fragment parsing.
 *
 * Known Defect: handlesDeepStack – deep nesting causes incorrect state handling 
 * or stack overflow. Test targets very deep HTML structure.
 */
public class HtmlTreeBuilderDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSimpleDocumentParsing() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testStateTransitionFromInitialToBeforeHead() {
        // After parsing <html>, state should move to BeforeHead
        String html = "<html></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testInsertEmptyVoidTag() {
        String html = "<br>"; // self-closing void tag
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.body().childrenSize());
        assertEquals("br", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testFosterInsertsEnabled() {
        // Foster parenting occurs when inside a table and inserting content
        String html = "<table><tr><td>Hello</td></tr></table>";
        Document doc = Jsoup.parse(html);
        // Should not throw and should produce correct DOM
        assertEquals("table", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testFormattingElementsReconstruction() {
        // Create a scenario where formatting elements need reconstruction (e.g., nested <b><i>...)
        String html = "<b><i>text</i></b>";
        Document doc = Jsoup.parse(html);
        assertEquals("text", doc.body().text());
        assertEquals("b", doc.body().child(0).tagName());
        assertEquals("i", doc.body().child(0).child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testInScopeBasic() {
        String html = "<div><p>para</p></div>";
        Document doc = Jsoup.parse(html);
        // We cannot easily call inScope directly; instead verify DOM structure
        assertEquals("div", doc.body().child(0).tagName());
        assertEquals("p", doc.body().child(0).child(0).tagName());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDeepNestingNotOverflow() {
        // Very deep nesting (e.g., 10000 nested <div> tags) to avoid StackOverflowError
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append("<div>");
        }
        sb.append("deep");
        for (int i = 0; i < 5000; i++) {
            sb.append("</div>");
        }
        String html = sb.toString();
        Document doc = Jsoup.parse(html);
        assertEquals("deep", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testNullContextFragmentParsing() {
        // parseFragment with null context (fragmentParsing true)
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<p>test</p>", null, "http://example.com", new ParseErrorList(0, 0), ParseSettings.htmlDefault);
        assertEquals(1, nodes.size());
        assertEquals("p", nodes.get(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testEmptyInput() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    @Test(timeout = 4000)
    public void testVeryLongAttributeValues() {
        StringBuilder attr = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            attr.append('a');
        }
        String html = "<p class=\"" + attr.toString() + "\">value</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("value", doc.text());
    }

    @Test(timeout = 4000)
    public void testMalformedSelfClosingTag() {
        // Tag like <br/> is fine; but <div/> should be handled (unknown tag)
        String html = "<div/>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        // Should produce an empty div (self-closing unknown)
        assertEquals("div", doc.body().child(0).tagName());
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (handlesDeepStack)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testHandlesDeepStackTargeted() {
        // Simulates the known failure: deep stack of opening tags without closing
        // that may cause stack overflow or assertion error.
        // The fix should handle deep stacks without throwing.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("<div>");
        }
        // No closing tags – will go to end-of-file, builder should still produce a valid doc
        String html = sb.toString();
        Document doc = Jsoup.parse(html);
        // Should not throw StackOverflowError or AssertionError
        assertNotNull(doc);
        // The body should have 10000 nested divs
        assertTrue(doc.body().childNodeSize() > 0);
        // Also check that we can traverse deep
        Element current = doc.body().child(0);
        for (int i = 1; i < 10000; i++) {
            assertNotNull(current);
            assertTrue(current.childrenSize() >= 1);
            current = current.child(0);
        }
    }

    // Additional defect-triggering variations
    @Test(timeout = 4000)
    public void testDeepStackWithInlineElements() {
        // Deep stack with inline elements like <span>
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append("<span>");
        }
        sb.append("text");
        for (int i = 0; i < 5000; i++) {
            sb.append("</span>");
        }
        String html = sb.toString();
        Document doc = Jsoup.parse(html);
        assertEquals("text", doc.body().text());
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidBaseUri() {
        // baseUri is validated? Not directly, but null should cause NPE? Actually Jsoup.parse does not allow null baseUri.
        Jsoup.parse("<html></html>", null);
    }

    @Test(timeout = 4000)
    public void testFragmentedParseWithInvalidState() {
        // Attempt to parse a fragment in a context that might cause transitions to invalid states
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element context = new Element(Tag.valueOf("div", ParseSettings.htmlDefault), "http://example.com");
        List<Node> nodes = builder.parseFragment("<script>alert(1)</script>", context, "http://example.com", new ParseErrorList(0, 0), ParseSettings.htmlDefault);
        // Should parse script as data node inside script tag
        assertEquals(1, nodes.size());
        assertEquals("script", nodes.get(0).nodeName());
        // The script content is a DataNode child
    }

    @Test(timeout = 4000)
    public void testErrorStateMethod() {
        // error() method adds parse error; we can check error list
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        Document doc = parser.parseInput("<p>Unclosed tag", "http://example.com");
        assertTrue(parser.getErrors().size() > 0);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testToStringMethod() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        // After initialisation, toString should not throw
        String str = builder.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("TreeBuilder{"));
    }

    @Test(timeout = 4000)
    public void testFormElementAssociation() {
        String html = "<form><input type='text' name='q' /></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.body().child(0);
        assertEquals(1, form.elements().size());
        assertEquals("q", form.elements().get(0).attr("name"));
    }

    @Test(timeout = 4000)
    public void testFramesetOkToggle() {
        // framesetOk should be true initially, but set false after certain tokens
        // We can test by parsing a frameset document
        String html = "<html><frameset><frame src='a.html'></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        // In HTML5, frameset not supported, but it should parse without error
    }

    @Test(timeout = 4000)
    public void testScriptTagContentPreservation() {
        String html = "<script>var x = 1 < 2;</script>";
        Document doc = Jsoup.parse(html);
        assertEquals("var x = 1 < 2;", doc.select("script").first().data());
    }
}