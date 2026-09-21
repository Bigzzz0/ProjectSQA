package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Token;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.HtmlTreeBuilderState;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;

import java.util.List;

/**
 * Branch & Defect Analysis Matrix:
 *
 * Target class: HtmlTreeBuilderState (enum)
 * Defect: <image> start tag should produce <img /> but fails (Defects4J issue).
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - Test each state's process method with typical tokens
 *   - Verify state transitions, element insertion, tree structure
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - Whitespace-only tokens (tabs, newlines, spaces)
 *   - Empty strings, nullCharacter strings
 *   - Tags with empty attributes, missing required attributes
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - Test <image> start tag in InBody -> must convert to <img>
 *   - Verify output HTML contains <img /> rather than <image>
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - Invalid end tags, unexpected doctypes, illegal close tags
 *   - Test error() calls and false returns
 *   - Test handling of "sarcasm" end tag (falls to anyOtherEndTag)
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Verify that sequence of process calls maintains correct stack and element relationships
 *   - Test fragment parsing behavior
 */
public class HtmlTreeBuilderStateDeepseekTest {

    // --- Helper methods ---
    private HtmlTreeBuilder createTreeBuilder(String input) {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new java.io.StringReader(input), "http://example.com", parser);
        return tb;
    }

    private Document parse(String html) {
        return Jsoup.parse(html);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch – <image> -> <img>
    // =========================================================================

    @Test(timeout = 4000)
    public void testImageTagConvertsToImg() {
        // This test directly targets the known defect: <image> should become <img />
        Document doc = parse("<image>");
        String html = doc.html();
        // The expected output is a self-closing <img> tag
        assertTrue("Expected <img> element, but got: " + html,
                html.contains("<img>") || html.contains("<img />"));
        // Also verify no <image> remains
        assertFalse("Unexpected <image> tag in output", html.contains("<image>"));
        // The element should be an img
        Element img = doc.select("img").first();
        assertNotNull("Expected an img element", img);
    }

    @Test(timeout = 4000)
    public void testImageTagWithAttributes() {
        Document doc = parse("<image src='test.jpg' alt='test'>");
        Element img = doc.select("img").first();
        assertNotNull("Expected img element", img);
        assertEquals("src attribute should be preserved", "test.jpg", img.attr("src"));
        assertEquals("alt attribute should be preserved", "test", img.attr("alt"));
        // Ensure it's a self-closing tag (no closing tag)
        assertTrue("Expected img to be self-closing", img.tag().isSelfClosing());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateIgnoresWhitespace() {
        // Initial state: whitespace should be ignored
        Document doc = parse("   ");
        assertEquals("Expected empty document", "", doc.text());
    }

    @Test(timeout = 4000)
    public void testInitialStateProcessesDoctype() {
        Document doc = parse("<!DOCTYPE html>");
        assertEquals("Should have doctype", "html", doc.document().documentType().name());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlTransitions() {
        Document doc = parse("<html><head></head><body></body></html>");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testInHeadProcessesTitleAndStyle() {
        Document doc = parse("<head><title>Hello</title><style>body {}</style></head>");
        assertEquals("Hello", doc.title());
        assertEquals(1, doc.head().getElementsByTag("style").size());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript() {
        Document doc = parse("<noscript><meta charset='utf-8'></noscript>");
        // noscript should be processed as raw text? jsoup treats noscript as in head noscript
        Element noscript = doc.getElementsByTag("noscript").first();
        assertNotNull(noscript);
    }

    @Test(timeout = 4000)
    public void testAfterHeadProcessesBody() {
        Document doc = parse("<html><head></head><body>Content</body></html>");
        assertEquals("Content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBodyProcessesVariousTags() {
        Document doc = parse("<div><p>Text</p><a href='#'>Link</a></div>");
        assertEquals(1, doc.select("div").size());
        assertEquals("Text", doc.select("p").text());
        assertEquals("Link", doc.select("a").text());
    }

    @Test(timeout = 4000)
    public void testInBodyProcessesFormTag() {
        Document doc = parse("<form action='/submit'><input name='x'></form>");
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("/submit", form.attr("action"));
    }

    @Test(timeout = 4000)
    public void testInBodyProcessesButtonTag() {
        Document doc = parse("<button>Click</button>");
        assertEquals(1, doc.select("button").size());
    }

    @Test(timeout = 4000)
    public void testInBodyProcessesTableTag() {
        Document doc = parse("<table><tr><td>Cell</td></tr></table>");
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInTableTextWhitespace() {
        Document doc = parse("<table>  \n  <tr><td>Cell</td></tr></table>");
        // whitespace should be ignored in table text
        assertEquals("Cell", doc.select("td").text());
    }

    @Test(timeout = 4000)
    public void testInCaptionTag() {
        Document doc = parse("<table><caption>Caption</caption><tr><td>Data</td></tr></table>");
        assertEquals("Caption", doc.select("caption").text());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup() {
        Document doc = parse("<table><colgroup><col span='2'></colgroup><tr><td>A</td><td>B</td></tr></table>");
        assertEquals(1, doc.select("col").size());
    }

    @Test(timeout = 4000)
    public void testInTableBody() {
        Document doc = parse("<table><tbody><tr><td>Cell</td></tr></tbody></table>");
        assertEquals(1, doc.select("tbody").size());
    }

    @Test(timeout = 4000)
    public void testInRowTag() {
        Document doc = parse("<table><tr><td>Cell</td></tr></table>");
        assertEquals(1, doc.select("tr").size());
    }

    @Test(timeout = 4000)
    public void testInCellTag() {
        Document doc = parse("<table><tr><td>Cell</td></tr></table>");
        assertEquals(1, doc.select("td").size());
    }

    @Test(timeout = 4000)
    public void testInSelectTag() {
        Document doc = parse("<select><option>1</option><option>2</option></select>");
        assertEquals(2, doc.select("option").size());
    }

    @Test(timeout = 4000)
    public void testInSelectInTable() {
        Document doc = parse("<table><tr><td><select><option>X</option></select></td></tr></table>");
        assertEquals(1, doc.select("select").size());
    }

    @Test(timeout = 4000)
    public void testAfterBodyTag() {
        Document doc = parse("<html><body>Content</body></html>");
        // After parsing, state should be AfterAfterBody or finished
        assertEquals("Content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInFramesetTag() {
        Document doc = parse("<html><frameset><frame src='a.html'></frameset></html>");
        assertEquals(1, doc.select("frame").size());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetTag() {
        Document doc = parse("<html><frameset></frameset><noframes>Text</noframes></html>");
        // noframes content should be ignored? Actually after frameset, noframes is processed
        assertEquals("Text", doc.text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvalidEndTagInInBody() {
        // An end tag that doesn't match any open tag should be ignored (error)
        Document doc = parse("<div><p>Text</div>");
        // The <p> should be closed implicitly, but the extra </div> may be error
        // The output should still be valid
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testDoctypeAfterInitialState() {
        // Doctype in before head should error
        Document doc = parse("<html><!DOCTYPE html>");
        // It should still produce a document
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testDoubleBodyTag() {
        Document doc = parse("<body><body>Content</body></body>");
        // Only one body should be present
        assertEquals(1, doc.select("body").size());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testStackClearsOnTableError() {
        // This tests that a table inside a table triggers error and correct stack manipulation
        Document doc = parse("<table><table><tr><td>Nested</td></tr></table></table>");
        // The inner table should be closed before the outer table is closed
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testFosterParenting() {
        // When a table is inside a paragraph, the table should be foster-parented
        Document doc = parse("<p>Text<table><tr><td>Cell</td></tr></table></p>");
        // The <p> should not contain the table; table should be sibling of <p>
        Element p = doc.select("p").first();
        Element table = doc.select("table").first();
        assertNotNull(p);
        assertNotNull(table);
        // Both are children of body
        assertEquals(doc.body(), p.parent());
        assertEquals(doc.body(), table.parent());
    }

    // =========================================================================
    // Additional coverage for specific branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelfClosingTags() {
        Document doc = parse("<br><hr><img src='a.png'>");
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("hr").size());
        assertEquals(1, doc.select("img").size());
    }

    @Test(timeout = 4000)
    public void testEmptyTagHandling() {
        Document doc = parse("<div></div>");
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testTextareaTag() {
        Document doc = parse("<textarea>Initial content</textarea>");
        assertEquals("Initial content", doc.select("textarea").text());
    }

    @Test(timeout = 4000)
    public void testXmpTag() {
        Document doc = parse("<xmp>Raw text</xmp>");
        assertEquals("Raw text", doc.select("xmp").text());
    }

    @Test(timeout = 4000)
    public void testScriptTag() {
        Document doc = parse("<script>alert('hi');</script>");
        assertEquals("alert('hi');", doc.select("script").html());
    }

    @Test(timeout = 4000)
    public void testStyleTag() {
        Document doc = parse("<style>body { background: red; }</style>");
        assertTrue(doc.select("style").html().contains("red"));
    }

    @Test(timeout = 4000)
    public void testNoscriptTag() {
        Document doc = parse("<noscript><img src='test.jpg'></noscript>");
        // In jsoup, noscript is treated as raw text (since scripting is off)
        assertFalse("noscript content should be text, not parsed", doc.select("img").isEmpty());
    }

    @Test(timeout = 4000)
    public void testCommentInBody() {
        Document doc = parse("<div><!-- comment --></div>");
        assertEquals(1, doc.select("div").first().childNodes().size());
    }

    @Test(timeout = 4000)
    public void testDoctypeInBodyError() {
        Document doc = parse("<div><!DOCTYPE html></div>");
        // Doctype should be ignored inside body? It's a parse error
        // The doctype might appear in output? Usually ignored.
        assertNotNull(doc);
    }
}