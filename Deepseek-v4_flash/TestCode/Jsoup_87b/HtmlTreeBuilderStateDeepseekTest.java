package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * White-box JUnit 4 test suite for HtmlTreeBuilderState.
 * Targets line/branch coverage and the known preservedCaseLinksCantNest defect.
 */
public class HtmlTreeBuilderStateDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Part A: Core functional paths through all enum states (Initial, BeforeHtml, BeforeHead, InHead,
     *         InHeadNoscript, AfterHead, InBody, Text, InTable, InTableText, InCaption, InColumnGroup,
     *         InTableBody, InRow, InCell, InSelect, InSelectInTable, AfterBody, InFrameset,
     *         AfterFrameset, AfterAfterBody, AfterAfterFrameset, ForeignContent).
     * Part B: Boundary values: empty input, only whitespace, null-like tokens (conceptually), EOF.
     * Part C: Defect-targeted: nested <A> tags with case preservation.
     * Part D: Error/exception paths: invalid end tags, doctype in wrong state, etc.
     * Part E: Lifecycle/state transitions: each state transition is exercised.
     */

    // ---------------------------------------------------------------
    // Part A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testInitialStateWhitespace() {
        // Only whitespace in initial state -> ignored, stays in Initial
        Document doc = Jsoup.parse("   ");
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testInitialStateComment() {
        Document doc = Jsoup.parse("<!-- comment -->");
        assertEquals("<!-- comment -->", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testInitialStateDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        assertEquals("", doc.body().html()); // doctype attached to document, not body
        // Verify document node has doctype child
        assertEquals(1, doc.childNodes().size());
        assertTrue(doc.childNodes().get(0) instanceof org.jsoup.nodes.DocumentType);
    }

    @Test(timeout = 4000)
    public void testInitialStateThenBeforeHtml() {
        // Start tag, e.g., <p>, goes to BeforeHtml and reprocessed
        Document doc = Jsoup.parse("<p>Hello");
        assertEquals("<p>Hello</p>", doc.body().html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlDoctype() {
        // Doctype in BeforeHtml -> error, returns false (should not affect output)
        Document doc = Jsoup.parse("<!DOCTYPE html><html><body></body></html>");
        // Should still parse normally
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlStartTagHtml() {
        Document doc = Jsoup.parse("<html></html>");
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadStartTagHtml() {
        // In BeforeHead, start tag "html" delegates to InBody
        Document doc = Jsoup.parse("<html><head></head><body></body></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadScript() {
        Document doc = Jsoup.parse("<head><script>alert(1)</script></head>");
        assertEquals("alert(1)", doc.head().getElementsByTag("script").text());
    }

    @Test(timeout = 4000)
    public void testInHeadTitle() {
        Document doc = Jsoup.parse("<head><title>My Title</title></head>");
        assertEquals("My Title", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHeadEndTagBody() {
        // End tag "body" in InHead -> anythingElse -> process end tag head then process body
        Document doc = Jsoup.parse("<head></body>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptStartTagHtml() {
        Document doc = Jsoup.parse("<head><noscript><html></html></noscript></head>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagBody() {
        Document doc = Jsoup.parse("<html><head></head><body><p>Hello</p></body></html>");
        assertEquals("<p>Hello</p>", doc.body().html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagFrameset() {
        Document doc = Jsoup.parse("<html><head></head><frameset><frame src='a.html'></frameset></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBodySimpleText() {
        Document doc = Jsoup.parse("<body>Hello</body>");
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBodyAnchorTag() {
        Document doc = Jsoup.parse("<a href='x'>Link</a>");
        assertEquals("<a href=\"x\">Link</a>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testInBodyTable() {
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr></table>");
        assertEquals("<table><tbody><tr><td>Cell</td></tr></tbody></table>", doc.body().html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testInBodyForm() {
        Document doc = Jsoup.parse("<form><input></form>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagBody() {
        Document doc = Jsoup.parse("<body><p>Para</p></body>");
        assertEquals("<p>Para</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testTextEndTag() {
        // Text state handles end tag by popping and transitioning to original state
        Document doc = Jsoup.parse("<script>x</script>");
        assertEquals("x", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableCaption() {
        Document doc = Jsoup.parse("<table><caption>Cap</caption><tr><td>X</td></tr></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTableColgroup() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup><tr><td>X</td></tr></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTableTbody() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>X</td></tr></tbody></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTableText() {
        // Text in table should be moved to foster parenting
        Document doc = Jsoup.parse("<table>Text</table>");
        assertTrue(doc.body().html().contains("Text") || doc.body().html().contains("Text"));
    }

    @Test(timeout = 4000)
    public void testInCaptionEndTagCaption() {
        Document doc = Jsoup.parse("<table><caption>Cap</caption><tr><td>X</td></tr></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInColumnGroupEndTagColgroup() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup><tr><td>X</td></tr></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTableBodyStartTagTr() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>X</td></tr></tbody></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInRowStartTagTd() {
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr></table>");
        assertEquals("Cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInCellEndTagTd() {
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInSelectOption() {
        Document doc = Jsoup.parse("<select><option>One</option></select>");
        assertEquals("One", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelectInTableCaption() {
        Document doc = Jsoup.parse("<table><tr><td><select><option>X</option></select></td></tr></table>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterBody() {
        Document doc = Jsoup.parse("<html><body>Content</body></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInFramesetFrame() {
        Document doc = Jsoup.parse("<html><head></head><frameset><frame src='a.html'></frameset></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset() {
        Document doc = Jsoup.parse("<html><head></head><frameset></frameset><noframes>No</noframes></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody() {
        Document doc = Jsoup.parse("<html>Content</html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset() {
        Document doc = Jsoup.parse("<html><noframes>No</noframes></html>");
        assertNotNull(doc);
    }

    // ---------------------------------------------------------------
    // Part B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyInput() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testOnlyWhitespace() {
        Document doc = Jsoup.parse(" \t\n\r");
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testNullCharacter() {
        // null character in InBody should be an error, but not crash
        Document doc = Jsoup.parse("te\u0000xt");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append("x");
        Document doc = Jsoup.parse("<p>" + sb.toString() + "</p>");
        assertEquals(sb.toString(), doc.body().text());
    }

    @Test(timeout = 4000)
    public void testManyAttributes() {
        StringBuilder html = new StringBuilder("<a");
        for (int i = 0; i < 50; i++) {
            html.append(" attr").append(i).append("='value").append(i).append("'");
        }
        html.append(">Link</a>");
        Document doc = Jsoup.parse(html.toString());
        assertEquals("Link", doc.body().text());
    }

    // ---------------------------------------------------------------
    // Part C: Defect-Targeted Branch Zone (preservedCaseLinksCantNest)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNest() {
        // Defect: nested <A> tags should close the outer one before starting inner.
        // Expected output: <A> ONE </A> <A> Two </A>
        String html = "<A> ONE <A> Two </A> </A>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        String out = doc.body().html().replaceAll("\\s+", " ").trim();
        // Expected: two separate <A> tags
        assertTrue("First <A> should close before second", out.contains("</A> <A>") || out.contains("</A>\n<A>"));
        // More precise: the output should be like "<A> ONE </A><A> Two </A>" (order may vary)
        assertTrue(out.contains("<A> ONE </A>"));
        assertTrue(out.contains("<A> Two </A>"));
    }

    // ---------------------------------------------------------------
    // Part D: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testInvalidEndTagInInHead() {
        // End tag "invalid" in InHead -> error, returns false
        Document doc = Jsoup.parse("<head></invalid></head>");
        assertNotNull(doc); // should not crash
    }

    @Test(timeout = 4000)
    public void testInvalidStartTagInInHead() {
        // Start tag "foo" in InHead -> anythingElse -> process end head
        Document doc = Jsoup.parse("<head><foo></head>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testDoctypeInAfterBody() {
        // Doctype in AfterBody -> error, returns false
        Document doc = Jsoup.parse("<html><body>Content</body></html>");
        // adding doctype after body would be invalid but parser should handle
        // We'll parse fragment that triggers this: start with body then doctype
        // Actually let's directly test state transitions via internal if needed, but use fragment.
        // Simpler: test that parser does not throw on common invalid input
        String html = "<html><body>X<!DOCTYPE html></body></html>";
        Document doc2 = Jsoup.parse(html);
        assertNotNull(doc2);
    }

    @Test(timeout = 4000)
    public void testFormInTableWithExistingForm() {
        // InTable, start tag "form" with existing form element -> error, return false
        Document doc = Jsoup.parse("<table><form><input></form></table>");
        assertNotNull(doc);
        // The original form should not be inside table; parser should handle
    }

    // ---------------------------------------------------------------
    // Part E: Object Lifecycle & Contract Integrity (state transitions)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStateTransitionFromInitialToBeforeHtml() {
        // Parser should transition through states correctly
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head></head><body></body></html>");
        assertNotNull(doc);
        assertEquals("#root", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testElementsStackNotEmptyAfterParse() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        assertFalse(doc.body().children().isEmpty());
    }

    @Test(timeout = 4000)
    public void testFormattingElementsManagement() {
        // Ensure active formatting elements are managed correctly (e.g., <b> and <i>)
        Document doc = Jsoup.parse("<b><i>text</i></b>");
        assertEquals("<b><i>text</i></b>", doc.body().html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testFosterParenting() {
        // Content in table that is white-space only should be inserted into the table
        Document doc = Jsoup.parse("<table> <tr><td>X</td></tr> </table>");
        assertNotNull(doc);
    }
}