package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix
 * ==============================
 * Partition A: Core Functional Logic & State Transitions
 *   - Initial: whitespace, comment, doctype, other
 *   - BeforeHtml: doctype, comment, whitespace, start html, end head/body/html/br, other
 *   - BeforeHead: whitespace, comment, doctype, start html, start head, end head/body/html/br, other
 *   - InHead: whitespace, comment, doctype, start (html, base/meta/link, title, style/script, noscript, head), end head/body/html/br, other
 *   - InHeadNoscript: doctype, start html, end noscript, whitespace/comment, start basefont/etc, end br, start head/noscript, other
 *   - AfterHead: whitespace, comment, doctype, start (html, body, frameset, base/..., head), end body/html, other
 *   - InBody: character, comment, doctype, start (html, head-related, body, frameset, block, h1-h6, pre, form, li, dd/dt, plaintext, button, a, formatting, nobr, applet, table, void, input, param, hr, image, isindex, textarea, xmp, iframe, noembed, select, optgroup/option, rp/rt, math, svg, caption/col/colgroup/...), end (body, html, block-level, form, p, li, dd/dt, h1-h6, sarcasm, formatting adoption agency, applet/marquee/object, br, anyOtherEndTag), EOF
 *   - Text: character, EOF, end tag
 *   - InTable: character, comment, doctype, start (caption, colgroup, col, tbody/tfoot/thead, td/th/tr, table, style/script, input hidden, form, other), end (table, body/caption/col/colgroup/html/tbody/td/tfoot/th/thead/tr, other), EOF
 *   - InTableText: character (null vs whitespace vs other), other
 *   - InCaption: end caption, start caption/col/colgroup/.../tr or end table (exit to InTable), end body/col/colgroup/.../tr, other -> InBody
 *   - InColumnGroup: whitespace, comment, doctype, start (html, col, other), end colgroup, EOF, other
 *   - InTableBody: start (tr, th/td, caption/col/colgroup/tbody/tfoot/thead), end (tbody/tfoot/thead, table, body/caption/col/colgroup/html/td/th/tr, other)
 *   - InRow: start (th/td, caption/col/colgroup/tbody/tfoot/thead/tr), end (tr, table, tbody/tfoot/thead, body/caption/col/colgroup/html/td/th, other)
 *   - InCell: end (td/th, body/caption/col/colgroup/html, table/tbody/tfoot/thead/tr), start (caption/col/colgroup/tbody/td/tfoot/th/thead/tr), other -> InBody
 *   - InSelect: character, comment, doctype, start (html, option, optgroup, select, input/keygen/textarea, script, other), end (optgroup, option, select, other), EOF
 *   - InSelectInTable: start caption/table/.../th, end same, other -> InSelect
 *   - AfterBody: whitespace, comment, doctype, start html, end html, EOF, other -> InBody
 *   - InFrameset: whitespace, comment, doctype, start (html, frameset, frame, noframes, other), end frameset, EOF, other
 *   - AfterFrameset: whitespace, comment, doctype, start html, end html, start noframes, EOF, other
 *   - AfterAfterBody: comment, doctype/whitespace/start html, EOF, other -> InBody
 *   - AfterAfterFrameset: comment, doctype/whitespace/start html, EOF, start noframes, other -> InBody
 *   - ForeignContent: (placeholder) returns true
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - Null character (0x0000) in text – known defect
 *   - Very long whitespace
 *   - Empty attributes
 *   - Stack depth extremes (deeply nested tags)
 *   - Fragment parsing vs full document
 *   - Force quirks doctype
 *   - Self-closing tags in different contexts
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - InBody.Character with nullString -> triggers tb.error(this) and return false (bug: null char should be inserted as text)
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - Token with null data
 *   - Unexpected end tag in various states
 *   - Unexpected start tag in restrictive contexts
 *   - EOF in middle of construction
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - State transitions correctness
 *   - Nested table handling (foster parenting)
 *   - Formatting element list management
 */
public class TreeBuilderStateDeepseekTest {

    // ========================= Partition A: Core Functional Logic =========================

    @Test(timeout = 4000)
    public void testInitialState_Whitespace() {
        Document doc = Jsoup.parse("   ");
        assertNotNull(doc);
        // Should stay at Initial and ignore whitespace -> no children
        assertEquals(0, doc.children().size());
    }

    @Test(timeout = 4000)
    public void testInitialState_Comment() {
        Document doc = Jsoup.parse("<!-- comment -->");
        assertEquals(0, doc.children().size()); // comment appended to document, not as child element
        assertEquals(1, doc.childNodes().size());
        assertEquals("#comment", doc.childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testInitialState_Doctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        assertEquals(1, doc.children().size()); // After doctype, BeforeHtml transition
        assertEquals("html", doc.children().get(0).tagName());
        assertNotNull(doc.documentType());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_StartTag() {
        Document doc = Jsoup.parse("<html><head></head><body></body></html>");
        assertEquals("html", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_EndTagBr() {
        // End tag "br" before html is allowed and triggers anythingElse
        Document doc = Jsoup.parse("</br>");
        // Should implicitly create html, head, body, and process </br> in body -> effectively ignore
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHead_StartHead() {
        Document doc = Jsoup.parse("<html><head>");
        Element head = doc.head();
        assertNotNull(head);
    }

    @Test(timeout = 4000)
    public void testBeforeHead_EndTagBody() {
        // </body> before head triggers anythingElse -> creates html, head, then process </body> in body?
        Document doc = Jsoup.parse("</body>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHead_Simple() {
        Document doc = Jsoup.parse("<head><title>Test</title></head>");
        assertEquals("Test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_Script() {
        Document doc = Jsoup.parse("<head><script>alert('hi');</script></head>");
        // Script content should be inside #text node inside script element
        Element script = doc.head().select("script").first();
        assertNotNull(script);
        assertTrue(script.childNode(0) instanceof TextNode);
    }

    @Test(timeout = 4000)
    public void testInHead_Noscript() {
        Document doc = Jsoup.parse("<head><noscript><meta charset=\"utf-8\"></noscript></head>");
        // Jsoup does not run scripts, so noscript content is treated as raw text? Actually current code handles noscript as InHeadNoscript state.
        Element noscript = doc.head().select("noscript").first();
        assertNotNull(noscript);
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartBody() {
        Document doc = Jsoup.parse("<html><head></head><body></body></html>");
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartFrameset() {
        Document doc = Jsoup.parse("<html><head></head><frameset><frame src='a.html'></frameset></html>");
        assertEquals("frameset", doc.body().tagName()); // body replaced by frameset? Actually body is not created
    }

    // ========================= Partition C: Defect-Targeted Branch =========================

    @Test(timeout = 4000)
    public void testInBody_NullCharacterAsText() {
        // Known defect: null character should be inserted as text, but current code errors and returns false.
        String html = "<div>\u0000</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        // Correct behavior: the null character (or replacement U+FFFD) should appear in the text.
        // Bug: it is dropped, so text is empty.
        assertNotNull(div);
        assertFalse("Null character should be handled as text", div.text().isEmpty());
        // According to HTML5 spec, null character should be replaced with U+FFFD.
        // We assert at least there is some text content.
        String text = div.text();
        assertTrue("Text should contain the null character or replacement",
                   text.contains("\u0000") || text.contains("\uFFFD"));
    }

    // ========================= More InBody Tests =========================

    @Test(timeout = 4000)
    public void testInBody_SimpleTags() {
        Document doc = Jsoup.parse("<div><p>Hello</p><h1>World</h1></div>");
        assertEquals("Hello", doc.select("p").text());
        assertEquals("World", doc.select("h1").text());
    }

    @Test(timeout = 4000)
    public void testInBody_FormattingTags() {
        Document doc = Jsoup.parse("<b><i>bold italic</i></b>");
        assertEquals("bold italic", doc.body().text());
        assertEquals("b", doc.body().child(0).tagName());
        assertEquals("i", doc.body().child(0).child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testInBody_AnchorTag() {
        Document doc = Jsoup.parse("<a href='http://example.com'>link</a>");
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("link", a.text());
    }

    @Test(timeout = 4000)
    public void testInBody_TableAndForm() {
        Document doc = Jsoup.parse("<form><table><tr><td>cell</td></tr></table></form>");
        // Form and table interaction: foster parenting may occur
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testInBody_ButtonInButtonScope() {
        Document doc = Jsoup.parse("<button>Click</button>");
        assertEquals("Click", doc.select("button").text());
    }

    @Test(timeout = 4000)
    public void testInBody_AdoptionAgency() {
        // Multiple nested <a> tags to trigger adoption agency algorithm
        Document doc = Jsoup.parse("<div><a><a>nested</a></a></div>");
        // Should close first <a> before second
        assertEquals("nested", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagP() {
        Document doc = Jsoup.parse("<p>paragraph</p>");
        assertEquals("paragraph", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testInBody_EmptyTag() {
        Document doc = Jsoup.parse("<br><hr>");
        assertNotNull(doc.select("br").first());
        assertNotNull(doc.select("hr").first());
    }

    // ========================= Text State =========================

    @Test(timeout = 4000)
    public void testTextState_ScriptContent() {
        Document doc = Jsoup.parse("<script>var x = 1;</script>");
        String scriptContent = doc.select("script").first().childNode(0).toString();
        assertTrue(scriptContent.contains("var x = 1;"));
    }

    // ========================= InTable =========================

    @Test(timeout = 4000)
    public void testInTable_SimpleTable() {
        Document doc = Jsoup.parse("<table><tr><td>data</td></tr></table>");
        assertEquals("data", doc.select("td").text());
    }

    @Test(timeout = 4000)
    public void testInTable_FosterParenting() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr>text after cell</table>");
        // Text should be foster-parented before table
        Element body = doc.body();
        // The text should be before the table or inside? Actually foster parenting places it before the table.
        assertTrue(doc.text().contains("text after cell"));
    }

    // ========================= InTableText =========================

    @Test(timeout = 4000)
    public void testInTableText_WhitespaceOnly() {
        Document doc = Jsoup.parse("<table>   <tr><td>cell</td></tr></table>");
        assertEquals("cell", doc.select("td").text());
    }

    // ========================= InCaption =========================

    @Test(timeout = 4000)
    public void testInCaption_Simple() {
        Document doc = Jsoup.parse("<table><caption>Caption</caption><tr><td>cell</td></tr></table>");
        assertEquals("Caption", doc.select("caption").text());
    }

    @Test(timeout = 4000)
    public void testInCaption_EndCaption() {
        Document doc = Jsoup.parse("<table><caption>Caption</caption></table>");
        assertNotNull(doc.select("caption").first());
    }

    // ========================= InColumnGroup =========================

    @Test(timeout = 4000)
    public void testInColumnGroup() {
        Document doc = Jsoup.parse("<table><colgroup><col style='width:50%'></colgroup><tr><td>cell</td></tr></table>");
        assertNotNull(doc.select("col").first());
    }

    // ========================= InTableBody =========================

    @Test(timeout = 4000)
    public void testInTableBody() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>cell</td></tr></tbody></table>");
        assertNotNull(doc.select("tbody").first());
    }

    @Test(timeout = 4000)
    public void testInTableBody_ExitOnTable() {
        // End tag "table" while in table body should close the body and table
        Document doc = Jsoup.parse("<table><tbody><tr><td>cell</td></tr></tbody></table>");
        assertEquals(1, doc.select("table").size());
    }

    // ========================= InRow =========================

    @Test(timeout = 4000)
    public void testInRow_ExplicitTr() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        assertEquals(1, doc.select("tr").size());
    }

    @Test(timeout = 4000)
    public void testInRow_HandleMissingTr() {
        // Starting td directly in table body should generate a tr
        Document doc = Jsoup.parse("<table><tbody><td>cell</td></tbody></table>");
        assertEquals(1, doc.select("tr").size());
    }

    // ========================= InCell =========================

    @Test(timeout = 4000)
    public void testInCell_EndTd() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        assertEquals(1, doc.select("td").size());
    }

    @Test(timeout = 4000)
    public void testInCell_CloseCellOnTableEnd() {
        // When </table> is seen while in a td, the cell should be closed first
        Document doc = Jsoup.parse("<table><tr><td>cell</table>");
        assertNotNull(doc.select("td").first());
    }

    // ========================= InSelect =========================

    @Test(timeout = 4000)
    public void testInSelect_Simple() {
        Document doc = Jsoup.parse("<select><option>Choice</option></select>");
        assertEquals("Choice", doc.select("option").text());
    }

    @Test(timeout = 4000)
    public void testInSelect_NestedSelect() {
        // Nested select triggers end tag for outer select
        Document doc = Jsoup.parse("<select><select><option>inner</option></select></select>");
        // Should close first select and then process inner select
        assertNotNull(doc.select("option").first());
    }

    // ========================= InSelectInTable =========================

    @Test(timeout = 4000)
    public void testInSelectInTable() {
        Document doc = Jsoup.parse("<table><select><option>opt</option></select></table>");
        // This should parse correctly
        assertNotNull(doc.select("select").first());
    }

    // ========================= AfterBody =========================

    @Test(timeout = 4000)
    public void testAfterBody_Whitespace() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><body></body>   </html>");
        // Whitspace after body should be ignored in AfterBody
        assertEquals(0, doc.body().childNodes().size());
    }

    // ========================= InFrameset =========================

    @Test(timeout = 4000)
    public void testInFrameset() {
        Document doc = Jsoup.parse("<html><frameset><frame src='a.html'></frameset></html>");
        assertEquals("frameset", doc.child(0).child(0).tagName());
    }

    // ========================= AfterFrameset =========================

    @Test(timeout = 4000)
    public void testAfterFrameset() {
        Document doc = Jsoup.parse("<html><frameset><frame src='a.html'></frameset></html>");
        // After frameset end, we are in AfterFrameset
        assertNotNull(doc);
    }

    // ========================= AfterAfterBody =========================

    @Test(timeout = 4000)
    public void testAfterAfterBody_Comment() {
        Document doc = Jsoup.parse("<html><body></body></html><!-- comment -->");
        assertEquals(1, doc.childNodes().size()); // comment is after html
    }

    // ========================= AfterAfterFrameset =========================

    @Test(timeout = 4000)
    public void testAfterAfterFrameset() {
        Document doc = Jsoup.parse("<html><frameset><frame></frameset></html>   ");
        // Whitespace after frameset in AfterAfterFrameset is ignored? Actually it's processed in InBody? But state is AfterAfterFrameset.
        assertNotNull(doc);
    }

    // ========================= ForeignContent (placeholder) =========================

    @Test(timeout = 4000)
    public void testForeignContent_Svg() {
        Document doc = Jsoup.parse("<div><svg><circle cx='50' cy='50' r='40'/></svg></div>");
        assertNotNull(doc.select("svg").first());
    }

    // ========================= Boundary Tests =========================

    @Test(timeout = 4000)
    public void testBoundary_VeryLongWhitespace() {
        StringBuilder sb = new StringBuilder("<div>");
        for (int i = 0; i < 10000; i++) sb.append(' ');
        sb.append("text</div>");
        Document doc = Jsoup.parse(sb.toString());
        assertEquals("text", doc.select("div").text().trim());
    }

    @Test(timeout = 4000)
    public void testBoundary_EmptyAttributes() {
        Document doc = Jsoup.parse("<div class='' id=''>content</div>");
        Element div = doc.select("div").first();
        assertTrue(div.hasAttr("class"));
        assertTrue(div.hasAttr("id"));
    }

    @Test(timeout = 4000)
    public void testBoundary_DeepNesting() {
        StringBuilder sb = new StringBuilder("<div>");
        for (int i = 0; i < 500; i++) sb.append("<div>");
        sb.append("deep");
        for (int i = 0; i < 500; i++) sb.append("</div>");
        sb.append("</div>");
        Document doc = Jsoup.parse(sb.toString());
        assertEquals("deep", doc.text());
    }

    @Test(timeout = 4000)
    public void testBoundary_FragmentParsing() {
        // Parse body fragment
        org.jsoup.parser.Parser parser = org.jsoup.parser.Parser.htmlParser();
        Document fragment = parser.parseBodyFragment("<p>fragment</p>", "http://base.com");
        assertEquals("fragment", fragment.body().text());
    }

    @Test(timeout = 4000)
    public void testBoundary_ForceQuirksDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
        // This doctype should not force quirks
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testBoundary_SelfClosingVoidTags() {
        Document doc = Jsoup.parse("<br/><hr/><img src='test.png'/>");
        assertNotNull(doc.select("br").first());
        assertNotNull(doc.select("hr").first());
        assertNotNull(doc.select("img").first());
    }
}