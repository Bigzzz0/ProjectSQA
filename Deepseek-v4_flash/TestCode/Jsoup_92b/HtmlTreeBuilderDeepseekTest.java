package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;

/**
 * White-box test suite for HtmlTreeBuilder.
 * Targets line/branch coverage and the known defect regarding duplicate attributes & case sensitivity.
 *
 * [Branch & Defect Analysis Matrix]
 * - Defect: duplicate attributes not dropped (case-insensitive) / case-sensitive attributes not preserved.
 * - Branches: token processing, stack manipulation, formatting elements, foster insertion, scope checks, resetInsertionMode, reconstructFormattingElements, etc.
 * - Partitions: A: Core parsing & state transitions; B: Boundary (empty/null, extreme depths); C: Defect-specific; D: Exception/defensive; E: Lifecycle/contract.
 */
public class HtmlTreeBuilderDeepseekTest {

    /* ========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ======================================================================== */

    @Test(timeout = 4000)
    public void testBasicParsingNormalFlow() {
        String html = "<html><head><title>Hello</title></head><body>World</body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Hello", doc.title());
        assertEquals("World", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testSimpleTextInsertion() {
        String html = "<p>Text</p>";
        Document doc = Jsoup.parse(html);
        Element p = doc.selectFirst("p");
        assertNotNull(p);
        assertEquals("Text", p.text());
    }

    @Test(timeout = 4000)
    public void testSelfClosingTag() {
        String html = "<br /><hr />";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.body().childNodeSize());
    }

    @Test(timeout = 4000)
    public void testCommentInsertion() {
        String html = "<!-- comment --><p>text</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.body().childNodeSize()); // comment + p
    }

    @Test(timeout = 4000)
    public void testCDataInScript() {
        String html = "<script><![CDATA[data]]></script>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.selectFirst("script"));
    }

    @Test(timeout = 4000)
    public void testDoctypePushedToDoc() {
        String html = "<!DOCTYPE html><html></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.documentType());
    }

    /* ========================================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ======================================================================== */

    @Test(timeout = 4000)
    public void testEmptyInput() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertTrue(doc.body().childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullInput() {
        // parse(String) throws IllegalArgumentException on null
        try {
            Jsoup.parse((String) null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVeryDeepNesting() {
        StringBuilder sb = new StringBuilder("<div id='0'>");
        for (int i = 1; i < 200; i++) {
            sb.append("<div id='").append(i).append("'>");
        }
        sb.append("content");
        for (int i = 0; i < 200; i++) sb.append("</div>");
        Document doc = Jsoup.parse(sb.toString());
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testMaxScopeSearchDepth() {
        // Exceeds MaxScopeSearchDepth (100), verify scope search still terminates
        StringBuilder sb = new StringBuilder("<div id='0'>");
        for (int i = 1; i <= 150; i++) {
            sb.append("<span id='").append(i).append("'>");
        }
        sb.append("<a id='target'>link</a>");
        for (int i = 0; i < 150; i++) sb.append("</span>");
        Document doc = Jsoup.parse(sb.toString());
        Element a = doc.selectFirst("a");
        assertNotNull(a);
    }

    /* ========================================================================
     * Partition C: Defect-Targeted Branch Zone
     * ======================================================================== */

    /**
     * Defect: retainsAttributesOfDifferentCaseIfSensitive
     * When ParseSettings.preserveCase is used, attribute cases should be preserved;
     * duplicate attributes (same name, case-sensitive) should be dropped (first wins).
     */
    @Test(timeout = 4000)
    public void testRetainsAttributesOfDifferentCaseIfSensitive() {
        String html = "<p One=\"One\" one=\"Three\" two=\"Four\">Text</p>";
        // Use case-sensitive parsing
        Parser parser = Parser.htmlParser();
        parser.settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "http://example.com");
        Element p = doc.selectFirst("p");
        assertNotNull(p);
        // With preserveCase, attribute "One" and "one" are different; both kept.
        // Duplicate "one" (lowercase) only keeps first value "Three"? Actually there is only one "one" (lowercase).
        // Ensure all attributes present.
        assertEquals("One", p.attr("One"));
        assertEquals("Three", p.attr("one"));
        assertEquals("Four", p.attr("two"));
        // Also ensure no extra attributes (e.g., from defect, extra "Two" appears)
        assertEquals(3, p.attributes().size());
    }

    /**
     * Defect: dropsDuplicateAttributes (HTML mode)
     * In HTML (case-insensitive), duplicate attributes should be dropped, first one wins.
     */
    @Test(timeout = 4000)
    public void testDropsDuplicateAttributes() {
        String html = "<p one=\"One\" one=\"Two\" one=\"Three\" two=\"four\">Text</p>";
        Document doc = Jsoup.parse(html);
        Element p = doc.selectFirst("p");
        assertNotNull(p);
        // Default HTML is case-insensitive, so all "one" are same; first value "One" kept.
        assertEquals("One", p.attr("one"));
        assertEquals("four", p.attr("two"));
        // Only two attributes expected
        assertEquals(2, p.attributes().size());
    }

    /**
     * Defect: duplicate attributes with mixed case – HTML case-insensitive should keep first.
     */
    @Test(timeout = 4000)
    public void testDropsDuplicateAttributesMixedCase() {
        String html = "<p ONE=\"One\" one=\"Two\" One=\"Three\">Text</p>";
        Document doc = Jsoup.parse(html);
        Element p = doc.selectFirst("p");
        assertNotNull(p);
        // In HTML, attribute names are lowercased, so all same; first value "One" wins.
        assertEquals("One", p.attr("one"));
        assertEquals(1, p.attributes().size());
    }

    /* ========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ======================================================================== */

    @Test(timeout = 4000)
    public void testInvalidHtmlDoesNotCrash() {
        String html = "<div><span><p></div></span>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testUnclosedTags() {
        String html = "<p>Para1<p>Para2";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("p").size());
    }

    @Test(timeout = 4000)
    public void testFosterInserts() {
        // Table with text inside should foster-insert
        String html = "<table>text<tr><td>cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        // The "text" should be fostered before the table
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testFosterInsertsWithNoTableParent() {
        // Fragment context without table
        String html = "<div>text<span></span></div>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.selectFirst("div"));
    }

    @Test(timeout = 4000)
    public void testFormElementAssociation() {
        String html = "<form id='f'><input name='x'></form>";
        Document doc = Jsoup.parse(html);
        Element input = doc.selectFirst("input");
        assertNotNull(input);
        // Input should be associated with form
        FormElement form = (FormElement) doc.selectFirst("#f");
        assertNotNull(form);
        assertTrue(form.elements().contains(input));
    }

    /* ========================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ======================================================================== */

    @Test(timeout = 4000)
    public void testBaseUriSetFromDoc() {
        String base = "http://example.com";
        Document doc = Jsoup.parse("<html><head><base href='/subdir/'></head><body></body></html>", base);
        // The base href should be resolved
        assertTrue(doc.baseUri().contains("/subdir/"));
    }

    @Test(timeout = 4000)
    public void testFragmentParsing() {
        String fragment = "<p>fragment</p>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        Element body = doc.body();
        assertEquals(1, body.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testFormattingElementsReconstruction() {
        String html = "<b><i>bold italic</i></b>";
        Document doc = Jsoup.parse(html);
        // Ensure formatting elements are properly reconstructed after closing
        assertEquals("bold italic", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testResetInsertionMode() {
        // Trigger resetInsertionMode by using a table and resetting
        String html = "<table><tr><td>cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.selectFirst("td"));
    }

    @Test(timeout = 4000)
    public void testInScopeMethods() {
        // Simple test to exercise inScope via parsing
        String html = "<div><p><a href='x'>link</a></p></div>";
        Document doc = Jsoup.parse(html);
        Element a = doc.selectFirst("a");
        assertNotNull(a);
        // This only checks that parsing succeeded; scope is tested indirectly.
    }

    @Test(timeout = 4000)
    public void testGenerateImpliedEndTags() {
        String html = "<ul><li>item1<li>item2</ul>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void testClearStackToTableContext() {
        String html = "<table><thead><tr><th>header</th></tr></thead><tbody><tr><td>data</td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.selectFirst("table"));
    }

    @Test(timeout = 4000)
    public void testEmptyEndTagReuse() {
        // Self-closing <script /> triggers emission of empty end tag for script
        String html = "<script />";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.selectFirst("script"));
    }

    @Test(timeout = 4000)
    public void testFramesetOkFlag() {
        // In body state, if we see <img>, framesetOk becomes false; later <frameset> should be ignored.
        String html = "<html><body><img src='x'><frameset></frameset></body></html>";
        Document doc = Jsoup.parse(html);
        // <frameset> should not be in output
        assertNull(doc.selectFirst("frameset"));
    }

    @Test(timeout = 4000)
    public void testMaybeSetBaseUriMultiple() {
        // Only first <base> element sets baseUri, subsequent ignored.
        String html = "<head><base href='/first'><base href='/second'></head>";
        Document doc = Jsoup.parse(html, "http://example.com");
        assertTrue(doc.baseUri().contains("/first"));
    }

    @Test(timeout = 4000)
    public void testTransitionToDifferentStates() {
        // Parse HTML that triggers multiple state transitions
        String html = "<!DOCTYPE html><html><head><title>t</title></head>" +
                "<body><p>text</p><table><tr><td>cell</td></tr></table>" +
                "<form><input></form></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testPopStackToClose() {
        // Trigger popStackToClose by mismatched closing tags
        String html = "<div><p>text</div>";
        Document doc = Jsoup.parse(html);
        // The parser should close <p> when </div> is seen
        assertNotNull(doc.selectFirst("div"));
    }

    @Test(timeout = 4000)
    public void testPopStackToBefore() {
        // popStackToBefore used when closing certain tags
        String html = "<b><i><u>nested</u></i></b>";
        Document doc = Jsoup.parse(html);
        assertEquals("nested", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testGetActiveFormattingElement() {
        // Active formatting element retrieval used in adoption agency algorithm
        String html = "<a href='1'>link1<a href='2'>link2</a></a>";
        Document doc = Jsoup.parse(html);
        // Should be two links, but second is mis-nested
        assertEquals(2, doc.select("a").size());
    }

    @Test(timeout = 4000)
    public void testInsertMarkerToFormattingElements() {
        // Marker inserted by e.g. <table> inside formatting elements
        String html = "<b><table><tr><td>cell</td></tr></table></b>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.selectFirst("td"));
    }

    @Test(timeout = 4000)
    public void testPendingTableCharacters() {
        // Text inside table before any cell
        String html = "<table>text</table>";
        Document doc = Jsoup.parse(html);
        // "text" should be fostered before table
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testIsSpecial() {
        // Ensure special elements like <script> are handled correctly
        String html = "<script>data</script>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.selectFirst("script"));
    }
}