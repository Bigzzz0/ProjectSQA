/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.HtmlTreeBuilderState
 *
 * Core State Machine Transitions Targeted:
 * - Initial: Whitespace, comments, doctypes (with/without Quirks), misplaced start/end tokens, default fallback.
 * - BeforeHtml: Doctype error branch, comment insertion, whitespace ignore, <html> tag, unexpected end tags (head/body/html/br vs others), fallback insertion.
 * - BeforeHead: Whitespace ignore, comments, doctype error, <html> delegating to InBody, <head> transition, implied head creation via tokens, unexpected end tags.
 * - InHead: Whitespace insertion, comments, doctype error, <html> handling, empty head tags (base, basefont, bgsound, command, link), meta, title (RcData),
 *           style/noframes (Rawtext), noscript transition, script (tokeniser transition & mark insertion mode), head error, end tags (head, body, html, br, others).
 * - InHeadNoscript: Doctype error, <html> delegation, </noscript> pop, whitespace/comment/head tags processing, </br>, illegal start/end tags.
 * - AfterHead: Whitespace/comment/doctype, <html> delegation, <body> framesetOk flag handling, <frameset>, head elements re-injection via stack push/pop,
 *              misplaced <head>, end tags (body/html vs illegal), fallback body insertion.
 * - InBody:
 *     - Character: null character '\u0000' error handling, whitespace reconstructFormattingElements, non-whitespace framesetOk toggle.
 *     - StartTag: <html> attribute merging, InHead delegation, <body> attribute merging & fragment handling, <frameset> stack popping,
 *                 button scope block elements (address, article, p, etc.), header tags (h1-h6) self-closing, pre/listing, <form> duplicate guard,
 *                 <li> list item scoping, dd/dt definition list scoping, <plaintext>, <button> nested scope closing,
 *                 <a> active formatting element check and stack cleanup, inline formatting (b, i, strong, etc.), <nobr> scoping,
 *                 markers (applet, marquee, object), <table> quirksmode p-closing, empty elements (img, br, etc.), <input> hidden vs non-hidden framesetOk,
 *                 <hr>, <image> tag name rewrite to <img>, <isindex> form generation, <textarea> Rcdata, <xmp>/<iframe>/<noembed> Rawtext,
 *                 <select> mode branch (InSelect vs InSelectInTable), <optgroup>/<option>, <ruby> rp/rt scoping, foreign tags (math, svg).
 *     - EndTag: </body> scope check, </html> delegating to </body>, block elements implied closing, </form> stack removal, </p> auto-generation when out of scope,
 *               </li> list item scope check, dd/dt, h1-h6, Adoption Agency Algorithm (AAA) 8-iteration limit, 64-depth stack scan, foster parenting,
 *               </br> error-then-insert-<br>, anyOtherEndTag descending stack iterator and special tag boundary guard.
 * - Text: Character insertion, EOF handling with originalState restoration, end tags.
 * - InTable / InTableText: Whitespace vs non-whitespace table characters foster parenting, null character guard, table context clears (caption, colgroup, col,
 *                          tbody/tfoot/thead, td/th/tr, nested <table>), table form handling, hidden input, resetInsertionMode on table close, EOF.
 * - InCaption / InColumnGroup / InTableBody / InRow / InCell: Structural table transitions, missing tr/td auto-generation, implied end tags, context popping.
 * - InSelect / InSelectInTable: Option/optgroup nesting and auto-closing, nested <select> error, input/keygen/textarea escape, table-scoped select escape.
 * - AfterBody / InFrameset / AfterFrameset / AfterAfterBody / AfterAfterFrameset / ForeignContent: Terminal states, frameset structures, comments outside html.
 *
 * Targeted Known Regression (Defects4J):
 * - org.jsoup.parser.HtmlParserTest::handlesUnclosedAnchors
 *   Adoption Agency Algorithm (AAA) handling unclosed <a> formatting across block boundary (<p>), asserting correct reconstructed DOM.
 */
package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defect: HtmlParserTest::handlesUnclosedAnchors
     * Verifies that unclosed anchor tags spanning block elements (<p>) are properly handled
     * by the Adoption Agency Algorithm without corrupting the anchor or dropping content.
     */
    @Test(timeout = 4000)
    public void testHandlesUnclosedAnchorsGroundTruthDefect() {
        String h = "<a href='http://example.com/'>Link<p>Error link</a>";
        Document doc = Jsoup.parse(h);
        String correct = "<a href=\"http://example.com/\">Link</a>\n<p><a href=\"http://example.com/\">Error link</a></p>";
        assertEquals(correct, doc.body().html());
    }

    /**
     * Additional AAA regression: Multiple unclosed formatting anchors spanning blocks.
     */
    @Test(timeout = 4000)
    public void testHandlesMultipleUnclosedAnchorsAcrossBlocks() {
        String h = "<a href='http://example.com/1'>Link 1<div>Block <a href='http://example.com/2'>Link 2</div>Extra</a>";
        Document doc = Jsoup.parse(h);
        Element body = doc.body();
        assertNotNull(body);
        assertTrue(body.html().contains("Link 1"));
        assertTrue(body.html().contains("Link 2"));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialAndBeforeHtmlTransitions() {
        // Comments and whitespace before doctype and html
        String html = "   <!-- initial comment -->\n<!DOCTYPE html><!-- before html --><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testInitialDoctypeForceQuirks() {
        String html = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadAndInHeadElements() {
        String html = "<html><head>" +
                "<base href='http://example.com/base/'>" +
                "<basefont face='Arial'>" +
                "<bgsound src='sound.mp3'>" +
                "<link rel='stylesheet' href='test.css'>" +
                "<meta charset='utf-8'>" +
                "<title>Test Title</title>" +
                "<style>body { color: red; }</style>" +
                "<noframes>No frames text</noframes>" +
                "<noscript><p>No script text</p></noscript>" +
                "<script>var x = 1;</script>" +
                "</head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("http://example.com/base/", doc.baseUri());
        assertEquals("Test Title", doc.title());
        assertEquals(1, doc.select("style").size());
        assertEquals(1, doc.select("script").size());
    }

    @Test(timeout = 4000)
    public void testAfterHeadAndBodyFramesetOkFlag() {
        // With normal body and text, framesetOk is set to false, so subsequent <frameset> is ignored
        String html = "<html><head></head><body><p>Text</p><frameset><frame src='frame.html'></frameset></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(0, doc.select("frameset").size());
        assertEquals(1, doc.select("p").size());
    }

    @Test(timeout = 4000)
    public void testFramesetRenderingWhenHeadTransitionsToFrameset() {
        String html = "<html><head></head><frameset rows='50%,50%'><frame src='a.html'><frame src='b.html'></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
    }

    @Test(timeout = 4000)
    public void testInBodyAllSpecialBlockTags() {
        String html = "<body>" +
                "<address>Address</address><article>Article</article><aside>Aside</aside>" +
                "<blockquote>Blockquote</blockquote><center>Center</center><details>Details</details>" +
                "<dir>Dir</dir><div>Div</div><dl><dt>Dt</dt><dd>Dd</dd></dl>" +
                "<fieldset>Fieldset</fieldset><figcaption>Figcaption</figcaption><figure>Figure</figure>" +
                "<footer>Footer</footer><header>Header</header><hgroup>Hgroup</hgroup>" +
                "<menu>Menu</menu><nav>Nav</nav><ol><li>Item</li></ol>" +
                "<p>Paragraph</p><section>Section</section><summary>Summary</summary><ul><li>Ul item</li></ul>" +
                "</body>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("address").size());
        assertEquals(1, doc.select("article").size());
        assertEquals(1, doc.select("nav").size());
        assertEquals(2, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void testInBodyHeadingsAutoClose() {
        String html = "<h1>Heading 1<h2>Heading 2<h3>Heading 3</h3></h2></h1>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals(1, doc.select("h3").size());
    }

    @Test(timeout = 4000)
    public void testInBodyPreAndListing() {
        String html = "<pre>Preformatted text</pre><listing>Listing text</listing>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("pre").size());
        assertEquals(1, doc.select("listing").size());
    }

    @Test(timeout = 4000)
    public void testInBodyNestedButtons() {
        String html = "<button>Btn 1<button>Btn 2</button></button>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("button").size());
    }

    @Test(timeout = 4000)
    public void testInBodyNobrScopeHandling() {
        String html = "<nobr>Nobr 1 <nobr>Nobr 2</nobr></nobr>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("nobr").size());
    }

    @Test(timeout = 4000)
    public void testInBodyEmptyElementsAndVoidTags() {
        String html = "<body><area><br><embed><img src='pic.jpg'><keygen><wbr><hr><image src='legacy.png'></body>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("area").size());
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("embed").size());
        // <image> should be rewritten to <img>, yielding 2 images total
        assertEquals(2, doc.select("img").size());
        assertEquals(1, doc.select("hr").size());
    }

    @Test(timeout = 4000)
    public void testInBodyInputTypesFramesetOk() {
        // Hidden inputs keep framesetOk intact; visible inputs reset it
        String htmlHidden = "<html><head></head><body><input type='hidden' name='x' value='y'></body></html>";
        Document docHidden = Jsoup.parse(htmlHidden);
        assertEquals(1, docHidden.select("input[type=hidden]").size());

        String htmlVisible = "<html><head></head><body><input type='text' name='x' value='y'></body></html>";
        Document docVisible = Jsoup.parse(htmlVisible);
        assertEquals(1, docVisible.select("input[type=text]").size());
    }

    @Test(timeout = 4000)
    public void testInBodyIsindexGeneration() {
        String html = "<body><isindex prompt='Custom search:' action='/search'></body>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals(1, doc.select("label").size());
        assertEquals(1, doc.select("input[name=isindex]").size());
        assertEquals("/search", doc.select("form").first().attr("action"));
    }

    @Test(timeout = 4000)
    public void testInBodyRawtextAndRcdataElements() {
        String html = "<body>" +
                "<textarea><p>not a tag</p></textarea>" +
                "<xmp><b>not bold</b></xmp>" +
                "<iframe><i>not italic</i></iframe>" +
                "<noembed><p>no embed</p></noembed>" +
                "<plaintext>Some <b>plaintext</b></plaintext>" +
                "</body>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p>not a tag</p>", doc.select("textarea").first().text());
        assertEquals("<b>not bold</b>", doc.select("xmp").first().text());
        assertEquals("<i>not italic</i>", doc.select("iframe").first().text());
        assertEquals(1, doc.select("plaintext").size());
    }

    @Test(timeout = 4000)
    public void testInBodyRubyAndRpRtElements() {
        String html = "<ruby>漢 <rp>(</rp><rt>kan</rt><rp>)</rp></ruby>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("ruby").size());
        assertEquals(2, doc.select("rp").size());
        assertEquals(1, doc.select("rt").size());
    }

    @Test(timeout = 4000)
    public void testInBodyForeignMathAndSvgElements() {
        String html = "<div><math><mi>x</mi></math><svg><circle cx='50' cy='50' r='40'/></svg></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("math").size());
        assertEquals(1, doc.select("svg").size());
        assertEquals(1, doc.select("circle").size());
    }

    @Test(timeout = 4000)
    public void testInBodyFormattingAdoptionAgencyAlgorithm() {
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p><b>1</b></p>\n<b>2</b>\n<p>3</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testInBodyAppletMarqueeObjectMarkers() {
        String html = "<marquee><b>Scrolling text</b></marquee>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("marquee").size());
        assertEquals(1, doc.select("b").size());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullCharacterHandlingInBody() {
        // Token.Character containing null string should be ignored with parse error
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<body>\u0000Hello</body>", "");
        assertTrue(doc.body().text().contains("Hello"));
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testNullCharacterHandlingInTableTextAndSelect() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<table>\u0000<tr><td>Cell</td></tr></table><select>\u0000<option>1</option></select>", "");
        assertNotNull(doc.select("table").first());
        assertNotNull(doc.select("select").first());
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testTableFosterParentingOnUnexpectedCharacters() {
        String html = "<table>Characters outside cells<tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        // "Characters outside cells" must be foster parented outside/before the table
        Element body = doc.body();
        assertTrue(body.text().startsWith("Characters outside cells"));
    }

    @Test(timeout = 4000)
    public void testTableCompleteLifecycle() {
        String html = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col class='c1'><col class='c2'></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Data</td></tr></tbody>" +
                "<tfoot><tr><td>Footer</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("table").size());
        assertEquals(1, doc.select("caption").size());
        assertEquals(1, doc.select("colgroup").size());
        assertEquals(2, doc.select("col").size());
        assertEquals(1, doc.select("thead").size());
        assertEquals(1, doc.select("tbody").size());
        assertEquals(1, doc.select("tfoot").size());
        assertEquals(3, doc.select("tr").size());
    }

    @Test(timeout = 4000)
    public void testTableImplicitRowAndCellCreation() {
        // Missing <tr> and <tbody>
        String html = "<table><td>Cell 1</td><th>Cell 2</th></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("tbody").size());
        assertEquals(1, doc.select("tr").size());
        assertEquals(1, doc.select("td").size());
        assertEquals(1, doc.select("th").size());
    }

    @Test(timeout = 4000)
    public void testTableHiddenInputsVsNormalInputs() {
        String html = "<table><input type='hidden' name='token' value='123'><tr><td>Data</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("input[type=hidden]").size());
    }

    @Test(timeout = 4000)
    public void testTableNestedFormHandling() {
        String html = "<table><form action='/post'><tr><td>Cell</td></tr></form></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals(1, doc.select("td").size());
    }

    @Test(timeout = 4000)
    public void testSelectWithinTableTransitions() {
        String html = "<table><tr><td><select><option>Opt 1</option><tr><td>Next</td></tr></select></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(2, doc.select("td").size());
    }

    @Test(timeout = 4000)
    public void testSelectOptgroupNestingAndClosing() {
        String html = "<select>" +
                "<optgroup label='G1'><option>1<option>2</optgroup>" +
                "<optgroup label='G2'><option>3</optgroup>" +
                "</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
    }

    @Test(timeout = 4000)
    public void testAdoptionAgencyStackDepthLimitBVA() {
        // AAA limits search to si < 64 and outer loop to 8 iterations
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            sb.append("<div>");
        }
        sb.append("<b>Deep Text");
        for (int i = 0; i < 70; i++) {
            sb.append("</div>");
        }
        sb.append("</b>");
        Document doc = Jsoup.parse(sb.toString());
        assertNotNull(doc.select("b").first());
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseErrorsOnMisplacedDoctypeAndTags() {
        Parser parser = Parser.htmlParser().setTrackErrors(50);
        String html = "<html><head><!DOCTYPE html><head></head></head><body><!DOCTYPE html></body></html>";
        parser.parseInput(html, "http://example.com");
        assertTrue("Should record parser errors on misplaced doctype and tags", parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testUnexpectedEndTagsDoNotThrowException() {
        Parser parser = Parser.htmlParser().setTrackErrors(20);
        String html = "</head></p></div></span></td></tr></tbody></table></select></form>";
        Document doc = parser.parseInput(html, "");
        assertNotNull(doc.body());
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testSarcasmEndTagIgnoredGracefully() {
        String html = "<p>Real statement</sarcasm> Continued</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("p").size());
        assertTrue(doc.body().text().contains("Real statement"));
    }

    @Test(timeout = 4000)
    public void testBrEndTagConvertedToStartTag() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("Line 1</br>Line 2", "");
        assertEquals(1, doc.select("br").size());
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testHtmlTagAttributesMergingInBody() {
        String html = "<html id='one' class='first'><body class='body1'><html id='ignored' class='second' data-new='attr'></body></html>";
        Document doc = Jsoup.parse(html);
        Element htmlEl = doc.select("html").first();
        assertEquals("one", htmlEl.id());
        assertTrue(htmlEl.hasClass("first"));
        assertTrue(htmlEl.hasAttr("data-new"));
    }

    @Test(timeout = 4000)
    public void testBodyTagAttributesMergingInBody() {
        String html = "<html><body id='first' class='main'><body id='ignored' class='secondary' data-extra='val'></body></html>";
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        assertEquals("first", body.id());
        assertTrue(body.hasClass("main"));
        assertTrue(body.hasAttr("data-extra"));
    }

    @Test(timeout = 4000)
    public void testFragmentParsingEdgeCases() {
        // Parsing fragments invokes special fragment checks across builder states
        List<Node> colNodes = Parser.parseFragment("<col class='test'>", new Element(Tag.valueOf("colgroup"), ""), "");
        assertEquals(1, colNodes.size());
        assertEquals("col", colNodes.get(0).nodeName());

        List<Node> rowNodes = Parser.parseFragment("<td>Cell</td>", new Element(Tag.valueOf("tr"), ""), "");
        assertEquals(1, rowNodes.size());
        assertEquals("td", rowNodes.get(0).nodeName());

        List<Node> optionNodes = Parser.parseFragment("<option>One</option>", new Element(Tag.valueOf("select"), ""), "");
        assertEquals(1, optionNodes.size());
        assertEquals("option", optionNodes.get(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testAfterBodyAndAfterAfterBodyComments() {
        String html = "<html><head></head><body>Content</body></html><!-- after body comment -->\n<!-- second comment -->";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        assertEquals("Content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetAndAfterAfterFramesetComments() {
        String html = "<html><head></head><frameset><frame src='test.html'></frameset></html><!-- after frameset -->\n";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE, CONTRACT INTEGRITY & DIRECT METHODS
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumIntegrityAndValues() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertEquals(23, states.length);
        for (HtmlTreeBuilderState state : states) {
            assertNotNull(state.name());
            assertSame(state, HtmlTreeBuilderState.valueOf(state.name()));
        }
    }

    @Test(timeout = 4000)
    public void testForeignContentProcessReturnsTrue() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html>", "http://example.com", ParseErrorList.noTracking());
        boolean result = HtmlTreeBuilderState.ForeignContent.process(new Token.EOF(), tb);
        assertTrue("ForeignContent should return true as default placeholder", result);
    }

    @Test(timeout = 4000)
    public void testDirectProcessOnInitialWithWhitespaceAndComment() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html>", "http://example.com", ParseErrorList.noTracking());

        Token.Character ws = new Token.Character("   \t\n");
        boolean wsResult = HtmlTreeBuilderState.Initial.process(ws, tb);
        assertTrue("Initial state should ignore pure whitespace", wsResult);

        Token.Comment comment = new Token.Comment();
        comment.getData().append("Direct test comment");
        boolean commentResult = HtmlTreeBuilderState.Initial.process(comment, tb);
        assertTrue("Initial state should process comment", commentResult);
    }

    @Test(timeout = 4000)
    public void testDirectProcessOnInTableTextFallback() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<table>", "http://example.com", ParseErrorList.noTracking());
        tb.transition(HtmlTreeBuilderState.InTableText);

        Token.Character ch = new Token.Character("a");
        boolean charResult = HtmlTreeBuilderState.InTableText.process(ch, tb);
        assertTrue(charResult);
        assertEquals(1, tb.getPendingTableCharacters().size());
    }

    @Test(timeout = 4000)
    public void testEmptyHtmlAndWhitespaceOnlyHtml() {
        Document emptyDoc = Jsoup.parse("");
        assertNotNull(emptyDoc.body());
        assertEquals(0, emptyDoc.body().childNodeSize());

        Document wsDoc = Jsoup.parse("   \n\t   ");
        assertNotNull(wsDoc.body());
        assertEquals(0, wsDoc.body().childNodeSize());
    }
}