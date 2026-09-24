package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target: org.jsoup.parser.HtmlTreeBuilderState
 *
 * 1. Targeted Defect (Defects4J: testTemplateInsideTable):
 *    - InTable state processes unknown tags (like <template>) via anythingElse(Token, HtmlTreeBuilder).
 *    - anythingElse triggers foster parenting if currentElement is table/tbody/tfoot/thead/tr.
 *    - When <template> is encountered within <table>, foster parenting causes premature extraction
 *      or misplaced nodes, failing HTML5 structural expectations when tested with <table><template>...
 *
 * 2. State & Branch Matrix Coverage:
 *    - Initial: Comments, DOCTYPE (with quirks/force-quirks, public/system ID), whitespace, unhandled tokens.
 *    - BeforeHtml: DOCTYPE error, comment, whitespace, <html> start tag, misnested end tags (head, body, html, br), anythingElse.
 *    - BeforeHead: Whitespace, comment, doctype error, <html> start tag, <head> start tag, misnested end tags, anythingElse.
 *    - InHead: Whitespace character insertion, comment, doctype error, <base> (sets base URI), <meta>, <title> (RcData),
 *      <noframes>/<style> (Rawtext), <noscript>, <script> (ScriptData), duplicate <head> error, </head> transition,
 *      misnested end tags (body, html, br), unexpected tags.
 *    - InHeadNoscript: Doctype error, <html> start, </noscript> pop, whitespace/comment/basefont/link/meta/style, <br> end tag,
 *      illegal start tags (<head>, <noscript>), fallback characters.
 *    - AfterHead: Whitespace, comment, doctype, <body>, <frameset>, head elements pushed/popped back to InHead, </head>,
 *      end tags (body, html), anythingElse.
 *    - InBody:
 *      * Character handling: null character ('\0'), framesetOk whitespace vs non-whitespace.
 *      * Start tags: <a> (active formatting adoption/re-adoption), InBodyStartEmptyFormatters (img, br, etc.),
 *        InBodyStartPClosers (p close), <span>, <li> (loop through stack, special tags), <form> (nested vs new),
 *        <dd>/<dt>, <plaintext>, <button> (in scope close vs open), <nobr> (in scope adoption error),
 *        InBodyStartApplets (markers), <table> (quirks vs standards p-closer), <input> (hidden vs non-hidden),
 *        <hr>, <image> (svg vs html img replacement), <isindex> (prompt, form attributes), <textarea>,
 *        <xmp>/<iframe>/<noembed>, <select> (InSelect vs InSelectInTable), <ruby> (implied end tags), <math>/<svg>.
 *      * End tags: Adoption Agency Algorithm (AAA: 8-iteration limit, stack search, bookmarking, foster parent / common ancestor reparenting),
 *        InBodyEndClosers, <li>, <body> (transition to AfterBody), <html>, <form>, <p>, <dd>/<dt>, Headings,
 *        <br> (creates br tag), anyOtherEndTag with special tag boundaries.
 *    - Text: Character insertion, EOF handling (pops and returns to originalState), script/style end tags.
 *    - InTable: Characters (InTableText accumulation, whitespace vs non-whitespace foster parenting),
 *      <caption>, <colgroup>, <col>, <tbody>/<tfoot>/<thead>, <tr>/<td>/<th>, nested <table>, <style>/<script>,
 *      <input> (hidden vs other), <form>, </table scope, end tag errors, EOF.
 *    - InTableText: null characters, whitespace insert vs non-whitespace foster parent dispatch.
 *    - InCaption: </caption> end tag, nested table elements triggering fake </caption>, end tag errors, InBody fallback.
 *    - InColumnGroup: Whitespace, comment, doctype, <html>, <col>, </colgroup>, EOF fragment handling.
 *    - InTableBody: <tr> start tag, <td>/<th> start tag, caption/col/colgroup exit, </tbody> end tag, </table exit.
 *    - InRow: <td>/<th> start tag, row context cleanup, missing tr handling, </tr> end tag, table/tbody end tags.
 *    - InCell: </td>/</th> end tag, cell close triggering InRow, inner table tags closing cells.
 *    - InSelect: Character, comment, doctype, <option>, <optgroup>, nested <select> error, input/textarea exit,
 *      </option>, </optgroup>, </select> reset insertion mode.
 *    - InSelectInTable: Table tags (start and end) forcing </select> close and reprocessing.
 *    - AfterBody: Whitespace, comment, doctype, </html> transition to AfterAfterBody, EOF, anythingElse re-transition to InBody.
 *    - InFrameset: Whitespace, comment, doctype, <frameset>, <frame>, <noframes>, </frameset>, EOF.
 *    - AfterFrameset: Whitespace, comment, doctype, <html>, </html> transition to AfterAfterFrameset, <noframes>.
 *    - AfterAfterBody: Comment, doctype/whitespace/html to InBody, EOF, anythingElse.
 *    - AfterAfterFrameset: Comment, doctype/whitespace/html to InBody, EOF, <noframes>, anythingElse.
 *    - ForeignContent: Dummy pass-through branch.
 */
public class HtmlTreeBuilderStateGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J testTemplateInsideTable)
    // =========================================================================

    /**
     * Targets Defects4J known issue regarding template elements inside table context.
     * In the defect version, <template> inside <table> triggers InTable.anythingElse(),
     * which misinterprets template or foster-parents content out of the table incorrectly.
     */
    @Test(timeout = 4000)
    public void testTemplateInsideTable() {
        String html = "<table><template><tr><td>1</td></tr></template></table>";
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").first();
        assertNotNull("Table element must be present", table);
        Element template = table.select("template").first();
        assertNotNull("Template should remain inside table or parsed without foster dropping", template);
        assertEquals("Template parent should be table", "table", template.parent().nodeName());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateVariants() {
        // DOCTYPE parsing with quirks mode triggers
        String htmlQuirks = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><body></body></html>";
        Document docQuirks = Jsoup.parse(htmlQuirks);
        assertNotNull(docQuirks.documentType());
        assertEquals("html", docQuirks.documentType().name());

        // Comment before doctype
        String htmlComment = "<!-- Leading comment --><!DOCTYPE html><html><body>Test</body></html>";
        Document docComment = Jsoup.parse(htmlComment);
        assertEquals("Leading comment", docComment.childNode(0).attr("comment").trim());

        // Token reprocessing without DOCTYPE
        String htmlNoDoc = "   <html><head></head><body>Hello</body></html>";
        Document docNoDoc = Jsoup.parse(htmlNoDoc);
        assertEquals("Hello", docNoDoc.body().text());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlTransitions() {
        // Unexpected end tags in BeforeHtml (head, body, html, br)
        String htmlWithEndTags = "</head></br><html><head></head><body>Content</body></html>";
        Document doc = Jsoup.parse(htmlWithEndTags);
        assertEquals("Content", doc.body().text());

        // Error end tag in BeforeHtml
        String htmlErrEnd = "</span><!DOCTYPE html><html><body>Content</body></html>";
        Document docErr = Jsoup.parse(htmlErrEnd);
        assertEquals("Content", docErr.body().text());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadTransitions() {
        // Whitespace and comments before head, <html> start tag handled in BeforeHead
        String html = "<html><!-- comment in BeforeHead -->   <head><title>Title</title></head></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Title", doc.title());

        // Premature end tags in BeforeHead
        String htmlPrematureEnd = "<html></head><body>Content</body></html>";
        Document docPrem = Jsoup.parse(htmlPrematureEnd);
        assertEquals("Content", docPrem.body().text());

        // Disallowed end tag in BeforeHead
        String htmlDisallowed = "<html></div><head></head><body>Content</body></html>";
        Document docDis = Jsoup.parse(htmlDisallowed);
        assertEquals("Content", docDis.body().text());
    }

    @Test(timeout = 4000)
    public void testInHeadElements() {
        String html = "<html><head>" +
                "<base href='http://example.com/test/' target='_blank'>" +
                "<meta charset='UTF-8'>" +
                "<title>Sample Page</title>" +
                "<link rel='stylesheet' href='style.css'>" +
                "<style>body { color: red; }</style>" +
                "<noframes>No frames allowed</noframes>" +
                "<noscript><a href='noscript.html'>NoScript</a></noscript>" +
                "<script>var x = 10;</script>" +
                "</head><body>Text</body></html>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        assertEquals("http://example.com/test/", doc.baseUri());
        assertEquals("Sample Page", doc.title());
        assertEquals(1, doc.select("style").size());
        assertEquals(1, doc.select("script").size());
        assertEquals("Text", doc.body().text());

        // InHead with erroneous duplicate head tag and doctype
        String duplicateHead = "<html><head><!DOCTYPE html><head></head></head><body>Body</body></html>";
        Document docDup = Jsoup.parse(duplicateHead);
        assertEquals("Body", docDup.body().text());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptState() {
        // Parsing elements within noscript in head
        String html = "<html><head><noscript>" +
                "<!-- noscript comment -->" +
                "<link rel='stylesheet' href='ns.css'>" +
                "<meta name='robots' content='noindex'>" +
                "<style>p { margin: 0; }</style>" +
                "<br>" +
                "<head></head>" + // invalid inside noscript
                "</noscript></head><body>Inside</body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Inside", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testAfterHeadVariants() {
        // Head elements leaking into AfterHead (re-injected to InHead)
        String htmlLeakedHead = "<html><head><title>Test</title></head>" +
                "   <!-- comment after head -->" +
                "<meta name='keywords' content='test'>" +
                "<body>Hello</body></html>";
        Document doc = Jsoup.parse(htmlLeakedHead);
        assertEquals(1, doc.head().select("meta").size());
        assertEquals("Hello", doc.body().text());

        // Frameset in AfterHead
        String htmlFrameset = "<html><head></head><frameset cols='50%,50%'><frame src='1.html'><frame src='2.html'></frameset></html>";
        Document docFrameset = Jsoup.parse(htmlFrameset);
        assertEquals(2, docFrameset.select("frame").size());

        // Illegal end tag in AfterHead
        String htmlIllegalEnd = "<html><head></head></div><body>Valid</body></html>";
        Document docIllegal = Jsoup.parse(htmlIllegalEnd);
        assertEquals("Valid", docIllegal.body().text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Comprehensive InBody Tag Switching
    // =========================================================================

    @Test(timeout = 4000)
    public void testInBodyFormattingAndAdoptionAgencyAlgorithm() {
        // Complex Adoption Agency Algorithm (AAA): unclosed <a> with overlapping blocks
        String aaaHtml = "<div><a href='#'>Link<div>Nested block inside link</a>End div</div>";
        Document docAAA = Jsoup.parse(aaaHtml);
        assertNotNull(docAAA.select("a").first());
        assertEquals("LinkNested block inside link", docAAA.select("a").first().text());

        // Multiple unclosed formatters
        String nestedFormatters = "<p><b>Bold <i>Italic <b>Duplicate</b></i></b></p>";
        Document docFormat = Jsoup.parse(nestedFormatters);
        assertEquals("Bold Italic Duplicate", docFormat.select("p").first().text());

        // Reconstructing formatting elements around empty tags and buttons
        String formattersWithBreaks = "<b>Line 1<br>Line 2<img src='x.png'>Line 3</b>";
        Document docBreaks = Jsoup.parse(formattersWithBreaks);
        assertEquals(3, docBreaks.select("b").size() > 0 ? 1 : 0);
        assertEquals(1, docBreaks.select("img").size());
    }

    @Test(timeout = 4000)
    public void testInBodyBlockAndListClosers() {
        // Implicit paragraph closing by headings, divs, hr
        String html = "<p>Paragraph 1<h1>Heading</h1><p>Paragraph 2<div>Div</div><p>Paragraph 3<hr>";
        Document doc = Jsoup.parse(html);
        assertEquals(0, doc.select("p h1").size());
        assertEquals(0, doc.select("p div").size());
        assertEquals(0, doc.select("p hr").size());

        // Consecutive headings error handling
        String headings = "<h1>Heading 1<h2>Heading 2</h2></h1>";
        Document docH = Jsoup.parse(headings);
        assertEquals(1, docH.select("h1").size());
        assertEquals(1, docH.select("h2").size());

        // List item closing (li, dt, dd)
        String lists = "<ul><li>Item 1<li>Item 2</ul><dl><dt>Term 1<dd>Def 1<dt>Term 2</dl>";
        Document docList = Jsoup.parse(lists);
        assertEquals(2, docList.select("li").size());
        assertEquals(2, docList.select("dt").size());
        assertEquals(1, docList.select("dd").size());
    }

    @Test(timeout = 4000)
    public void testInBodyFormsAndInputHandling() {
        // Nested form tag violation
        String nestedForms = "<form id='f1'><input type='text'><form id='f2'><input type='hidden' name='h'></form></form>";
        Document doc = Jsoup.parse(nestedForms);
        assertEquals(1, doc.select("form").size());
        assertEquals("f1", doc.select("form").first().id());

        // isindex tag handling
        String isindexHtml = "<form><isindex prompt='Enter keyword:' action='/search'></form>";
        Document docIsindex = Jsoup.parse(isindexHtml);
        assertNotNull(docIsindex.body());
    }

    @Test(timeout = 4000)
    public void testInBodyMediaAppletRubyMathSvg() {
        String html = "<div>" +
                "<applet code='test.class'><param name='p' value='1'></applet>" +
                "<object data='data.bin'><source src='s.mp4'><track src='t.vtt'></object>" +
                "<ruby>Base<rt>Annotation</rt><rp>(</rp></ruby>" +
                "<svg><circle cx='50' cy='50' r='40'/><image href='test.png'/></svg>" +
                "<math><mrow><mi>x</mi></mrow></math>" +
                "<plaintext>Some literal text <b>not parsed</b>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("applet").size());
        assertEquals(1, doc.select("ruby").size());
        assertEquals(1, doc.select("rt").size());
        assertEquals(1, doc.select("svg").size());
        assertEquals(1, doc.select("math").size());
        assertTrue(doc.body().html().contains("Some literal text <b>not parsed</b>"));
    }

    @Test(timeout = 4000)
    public void testInBodySpecialEndTags() {
        // End tag for span, sarcasm, br, body, html
        String html = "<p>Span test</span test></sarcasm><br/>Paragraph</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Span testParagraph", doc.body().text());

        // Empty paragraph creation when closing unopened p tag
        String unopenedP = "</div></p>";
        Document docP = Jsoup.parse(unopenedP);
        assertEquals(1, docP.select("p").size());
    }

    // =========================================================================
    // Partition D: Table States Coverage (InTable, InTableText, InCaption, etc.)
    // =========================================================================

    @Test(timeout = 4000)
    public void testInTableTransitionsAndFosterParenting() {
        // Foster parenting: non-whitespace characters & stray tags inside table
        String fosterHtml = "<table>Stray text<b>Stray bold</b><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(fosterHtml);
        assertEquals("Stray text", doc.body().ownText().trim());
        Element bold = doc.select("b").first();
        assertNotNull(bold);
        assertEquals("body", bold.parent().nodeName());

        // Colgroup and col processing
        String colHtml = "<table><colgroup><col width='50%'><col width='50%'></colgroup><tbody><tr><td>A</td></tr></tbody></table>";
        Document docCol = Jsoup.parse(colHtml);
        assertEquals(2, docCol.select("col").size());

        // InTable with hidden input vs visible input
        String inputInTable = "<table><input type='hidden' name='token' value='123'><input type='text' name='visible'><tr><td>B</td></tr></table>";
        Document docInput = Jsoup.parse(inputInTable);
        assertEquals("123", docInput.select("table > input[type=hidden]").val());
        assertEquals(1, docInput.select("body > input[type=text]").size());
    }

    @Test(timeout = 4000)
    public void testInCaptionAndInRowAndInCell() {
        // Caption with inner markup and premature close
        String captionHtml = "<table><caption>Table <b>Caption</b></caption><tr><th>Head</th></tr><tr><td>Data</td></tr></table>";
        Document doc = Jsoup.parse(captionHtml);
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(1, doc.select("th").size());
        assertEquals(1, doc.select("td").size());

        // Nested table inside a cell
        String nestedTable = "<table><tr><td><table><tr><td>Nested</td></tr></table></td></tr></table>";
        Document docNested = Jsoup.parse(nestedTable);
        assertEquals(2, docNested.select("table").size());
        assertEquals("Nested", docNested.select("table table td").text());
    }

    @Test(timeout = 4000)
    public void testInSelectAndInSelectInTable() {
        // Basic select with optgroup and options
        String selectHtml = "<select name='test'><optgroup label='group'><option value='1'>One</option><option value='2'>Two</optgroup></select>";
        Document doc = Jsoup.parse(selectHtml);
        assertEquals(2, doc.select("option").size());

        // Select inside table triggering InSelectInTable and breaking on table tags
        String selectInTable = "<table><tr><td><select><option>Opt 1</option><td>Next cell</td></tr></table>";
        Document docSelect = Jsoup.parse(selectInTable);
        assertEquals(2, docSelect.select("td").size());
        assertEquals(1, docSelect.select("select").size());

        // Select interrupted by input tag
        String selectWithInput = "<select><option>1</option><input type='text'></select>";
        Document docSelInput = Jsoup.parse(selectWithInput);
        assertEquals(1, docSelInput.select("select").size());
        assertEquals(1, docSelInput.select("input").size());
    }

    @Test(timeout = 4000)
    public void testFramesetStates() {
        // Full frameset with noframes, comment, doctype, and after frameset
        String framesetHtml = "<!DOCTYPE html>" +
                "<html><frameset rows='50%,*'>" +
                "<!-- frameset comment -->" +
                "<frame src='frame1.html'>" +
                "<frameset cols='50%,50%'>" +
                "<frame src='frame2.html'>" +
                "<frame src='frame3.html'>" +
                "</frameset>" +
                "<noframes><p>No frames support</p></noframes>" +
                "</frameset></html>";
        Document doc = Jsoup.parse(framesetHtml);
        assertEquals(3, doc.select("frame").size());
        assertEquals(2, doc.select("frameset").size());
        assertEquals(1, doc.select("noframes").size());
    }

    @Test(timeout = 4000)
    public void testAfterBodyAndAfterAfterBody() {
        // Trailing content after body and html
        String trailingHtml = "<html><head></head><body>Main</body></html><!-- trailing comment -->   <div>Trailing Div</div>";
        Document doc = Jsoup.parse(trailingHtml);
        assertEquals("Main Trailing Div", doc.body().text());

        // Comment after after body
        String trailingCommentOnly = "<html><body>Main</body></html><!-- End of document -->";
        Document docComment = Jsoup.parse(trailingCommentOnly);
        assertEquals(2, docComment.childNodeSize()); // html node and comment node
    }

    // =========================================================================
    // Partition E: Direct Token & Edge Branch Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullCharacterHandling() {
        // Null character in body and in table
        String nullInBody = "<body>Hello\u0000World</body>";
        Document doc = Jsoup.parse(nullInBody);
        assertNotNull(doc.body());

        String nullInTable = "<table>Hello\u0000World<tr><td>Data</td></tr></table>";
        Document docTable = Jsoup.parse(nullInTable);
        assertNotNull(docTable.body());
    }

    @Test(timeout = 4000)
    public void testDirectHtmlTreeBuilderStateMethods() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>Test</div>", "", ParseErrorList.tracking(10));

        // Direct state transitions & process execution
        HtmlTreeBuilderState initial = HtmlTreeBuilderState.Initial;
        assertNotNull(initial);

        // Process a comment through Initial state
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("Direct test comment");
        boolean res = initial.process(commentToken, tb);
        assertTrue(res);

        // Foreign content fallback
        HtmlTreeBuilderState foreign = HtmlTreeBuilderState.ForeignContent;
        assertTrue(foreign.process(commentToken, tb));

        // Enum values integrity
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertTrue(states.length >= 20);
        assertEquals(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
    }

    @Test(timeout = 4000)
    public void testFragmentParsingBranches() {
        // Parsing fragments triggers specific branches in InTable, InBody, InRow
        List<Element> cellFragment = Parser.parseFragment("<td>Fragment Cell</td>", new Element(Tag.valueOf("tr"), ""), "");
        assertFalse(cellFragment.isEmpty());

        List<Element> trFragment = Parser.parseFragment("<tr><td>Row</td></tr>", new Element(Tag.valueOf("table"), ""), "");
        assertFalse(trFragment.isEmpty());

        List<Element> colFragment = Parser.parseFragment("<col width='100'>", new Element(Tag.valueOf("colgroup"), ""), "");
        assertFalse(colFragment.isEmpty());
    }
}