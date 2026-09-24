package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.parser.HtmlTreeBuilderState
 *
 * 1. DEFECT TARGETING (Defects4J Ground Truth):
 *    - HtmlParserTest::preSkipsFirstNewline:
 *      HTML5 requires that the first newline (\n or \r\n) immediately following <pre>, <listing>, or <textarea>
 *      opening tags must be skipped during tree building / text node creation.
 *      Test targets: <pre>\n\nOne\nTwo\n</pre> => should preserve exactly "\nOne\nTwo\n" (first \n stripped).
 *                    <pre>\nOne\nTwo\n</pre> => should preserve exactly "One\nTwo\n" (first \n stripped).
 *
 * 2. PARTITION STRUCTURE & STATE COVERAGE:
 *    - Partition A: Core Functional Logic & State Transitions
 *      * Initial: Whitespace (ignore), Comment (insert), Doctype (quirks vs standards), Else (reprocess).
 *      * BeforeHtml: Doctype error, Comment, Whitespace, <html> tag, end tags (head/body/html/br vs others).
 *      * BeforeHead: Doctype error, <html>, <head>, end tags, fallback head insertion.
 *      * InHead: base (with/without href -> maybeSetBaseUri), link, meta, title (RcData), style/noframes (Rawtext),
 *                noscript, script, head error, unexpected start/end tags, head closing.
 *      * InHeadNoscript: doctype error, html, noscript end tag, whitespace/comment/head tags, br, fallback.
 *      * AfterHead: whitespace, comment, doctype error, html, body (framesetOk=false), frameset, misplaced head elements.
 *      * InBody:
 *        - Character: null character error (\u0000), whitespace with framesetOk true/false, non-whitespace.
 *        - Start tags: <a> (active formatting element duplication), empty formatters (area, br, img, wbr),
 *          p closers (div, p, etc.), span, li with stack-walking, html attribute merging, body attribute merging,
 *          frameset (allowed vs ignored), headings (p closing, nested heading pop), pre/listing, form (nested form error),
 *          dd/dt, plaintext, button (nested button closing), formatters (b, i, u, font), nobr (nested nobr scope check),
 *          applets (marker insertion), table (quirks vs standards p-closer), input (hidden vs visible framesetOk),
 *          hr, image (converted to img vs preserved in svg), isindex, textarea (Rcdata mode), xmp/iframe/noembed (Rawtext),
 *          select (InSelect vs InSelectInTable), ruby (rt/rp implied tags), math/svg, dropped tags.
 *        - End tags: Adoption Agency Algorithm (8 loops max, stack check, formatting element replacement, foster parenting),
 *          InBodyEndClosers, li (inListItemScope), body, html (delegation), form, p (inButtonScope vs empty p creation),
 *          dd/dt, headings, sarcasm, applets, br error, anyOtherEndTag fallback.
 *      * InTable: character -> InTableText, comment, doctype error, caption, colgroup, col, tbody/tfoot/thead,
 *                 td/th/tr, nested table, style/script, input (hidden vs other), form, end tags, foster parenting.
 *      * InTableText: nullString error, pendingTableCharacters whitespace vs foster-parented non-whitespace.
 *      * InCaption: caption end tag (inTableScope), table start/end tags, unexpected tags.
 *      * InColumnGroup: whitespace, comment, doctype, html, col, colgroup end tag, fallback.
 *      * InTableBody: template, tr, th/td, exitTableBody (caption, col, tbody, table), unexpected end tags.
 *      * InRow: template, th/td -> InCell, missing tr handling (caption, table, tbody), end tags (tr, table, tbody).
 *      * InCell: td/th end tags, closing cell on table/tr/caption/col, fallback to InBody.
 *      * InSelect & InSelectInTable: character (nullString check), option/optgroup start and end, select end tag,
 *                                    table tags closing select, input/textarea in select.
 *      * AfterBody, AfterAfterBody: comments, doctype error, html end tag, fragment parsing check, content after body.
 *      * InFrameset, AfterFrameset, AfterAfterFrameset: frame, frameset, noframes, comments, whitespace, doctype.
 *      * ForeignContent: fallback stub.
 *    - Partition B: Boundary Value Analysis (BVA) & Extremes
 *      * Empty input, whitespace-only, comment-only, null characters, deeply nested tags, fragment parsing contexts.
 *    - Partition C: Defect-Targeted Branch Zone (preSkipsFirstNewline).
 *    - Partition D: Defensive Guard Paths (direct invocation of error branches and invalid tokens).
 *    - Partition E: Contract Integrity & Constants Verification (Constants array sorting).
 * ---------------------------------------------------------------------------------------------------------------
 */
public class HtmlTreeBuilderStateGeminiTest {

    private HtmlTreeBuilder createBuilder(String html) {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(html), "http://example.com", ParseErrorList.tracking(100), ParseSettings.htmlDefault);
        return tb;
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE
    // =========================================================================

    /**
     * Targets known defect: org.jsoup.parser.HtmlParserTest::preSkipsFirstNewline
     * HTML5 parser specification requires skipping the leading newline character
     * immediately following opening pre, listing, or textarea elements.
     */
    @Test(timeout = 4000)
    public void testPreSkipsFirstNewlineDefect() {
        Document doc1 = Parser.htmlParser().parseInput("<pre>\n\nOne\nTwo\n</pre>", "");
        assertEquals("\nOne\nTwo\n", doc1.getElementsByTag("pre").get(0).text());

        Document doc2 = Parser.htmlParser().parseInput("<pre>\nOne\nTwo\n</pre>", "");
        assertEquals("One\nTwo\n", doc2.getElementsByTag("pre").get(0).text());
    }

    @Test(timeout = 4000)
    public void testListingAndCarriageReturnVariations() {
        Document docListing = Parser.htmlParser().parseInput("<listing>\nOne\nTwo</listing>", "");
        assertEquals("One\nTwo", docListing.getElementsByTag("listing").get(0).text());

        Document docCRLF = Parser.htmlParser().parseInput("<pre>\r\nOne\r\nTwo</pre>", "");
        assertEquals("One\r\nTwo", docCRLF.getElementsByTag("pre").get(0).text());
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialAndBeforeHtmlTransitions() {
        // Initial state with whitespace, comment, doctype
        String html = "   <!-- initial comment -->\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
                "<html lang=\"en\"><head><title>Test</title></head><body>Hello</body></html>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "http://example.com");

        assertNotNull(doc);
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        assertEquals("Test", doc.title());
        assertEquals(0, parser.getErrors().size());

        // Quirks mode trigger via doctype
        Document quirksDoc = Parser.htmlParser().parseInput("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//\"><html></html>", "");
        assertEquals(Document.QuirksMode.quirks, quirksDoc.quirksMode());

        // No doctype transitions directly to BeforeHtml then BeforeHead
        Document noDoctypeDoc = Parser.htmlParser().parseInput("<p>Direct text</p>", "");
        assertNotNull(noDoctypeDoc.body().select("p").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlAndBeforeHeadErrorsAndFallbacks() {
        // Redundant doctype in BeforeHtml triggers error
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<!DOCTYPE html><!DOCTYPE html><html><head></head><body></body></html>", "");
        assertFalse(parser.getErrors().isEmpty());

        // End tag </head> in BeforeHtml causes fallback to anythingElse
        parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("</head><html><head></head><body></body></html>", "");
        assertNotNull(doc.head());

        // Unexpected end tag in BeforeHtml
        parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("</div><html></html>", "");
        assertFalse(parser.getErrors().isEmpty());

        // End tag </p> in BeforeHead triggers error and auto-creates head
        parser = Parser.htmlParser().setTrackErrors(10);
        doc = parser.parseInput("<html></p><head></head><body></body></html>", "");
        assertFalse(parser.getErrors().isEmpty());
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testInHeadElements() {
        String html = "<html><head>" +
                "<base href=\"http://example.com/dir/\">" +
                "<link rel=\"stylesheet\" href=\"style.css\">" +
                "<meta charset=\"UTF-8\">" +
                "<title>Head Elements Test</title>" +
                "<style>body { color: red; }</style>" +
                "<noscript><link rel=\"stylesheet\" href=\"noscript.css\"></noscript>" +
                "<script>var a = 1;</script>" +
                "</head><body></body></html>";

        Document doc = Parser.htmlParser().parseInput(html, "http://example.com");
        assertEquals("http://example.com/dir/", doc.baseUri());
        assertEquals("Head Elements Test", doc.title());
        assertEquals(1, doc.getElementsByTag("style").size());
        assertEquals(1, doc.getElementsByTag("script").size());
        assertEquals(1, doc.getElementsByTag("noscript").size());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptState() {
        String html = "<html><head><noscript>" +
                "<!-- comment -->" +
                "<meta http-equiv=\"refresh\" content=\"30\">" +
                "<style>p { font-size: 10px; }</style>" +
                "Plain text in noscript" +
                "</noscript></head><body></body></html>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        assertNotNull(doc.select("noscript").first());
        assertFalse(parser.getErrors().isEmpty()); // text inside noscript triggers parse error in InHeadNoscript
    }

    @Test(timeout = 4000)
    public void testAfterHeadMisplacedElements() {
        // Head elements placed after <head> closed: should be reparented into head
        String html = "<html><head><title>Title</title></head>" +
                "<meta name=\"author\" content=\"Jsoup\">" +
                "<style>.cls { margin: 0; }</style>" +
                "<link rel=\"icon\" href=\"favicon.ico\">" +
                "<body>Body content</body></html>";

        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        assertFalse(parser.getErrors().isEmpty());
        assertEquals(1, doc.head().getElementsByTag("meta").size());
        assertEquals(1, doc.head().getElementsByTag("style").size());
        assertEquals(1, doc.head().getElementsByTag("link").size());
        assertEquals("Body content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBodyFormattingAndAdoptionAgency() {
        // Formatting elements adoption agency algorithm
        String html = "<b>1<i>2<p>3</b>4</p>5</i>";
        Document doc = Parser.htmlParser().parseInput(html, "");
        // <b> should wrap 1 and 2, and then cloned inside <p>
        assertEquals("12", doc.body().child(0).text());
        assertEquals("34", doc.body().select("p").first().text());

        // Nested <a> tags: outer <a> closed when inner <a> opened
        String aHtml = "<a>Link 1 <a>Link 2</a></a>";
        Document aDoc = Parser.htmlParser().parseInput(aHtml, "");
        assertEquals(2, aDoc.getElementsByTag("a").size());
        assertEquals("Link 1 ", aDoc.getElementsByTag("a").get(0).text());
        assertEquals("Link 2", aDoc.getElementsByTag("a").get(1).text());

        // Nobr nesting
        String nobrHtml = "<nobr>Nobr 1 <nobr>Nobr 2</nobr></nobr>";
        Document nobrDoc = Parser.htmlParser().parseInput(nobrHtml, "");
        assertEquals(2, nobrDoc.getElementsByTag("nobr").size());
    }

    @Test(timeout = 4000)
    public void testInBodyTagsHierarchyAndClosers() {
        // Paragraph closers: <div>, <p>, <h1>-<h6>, <hr> closes active <p>
        String html = "<p>Para 1<div>Div 1</div><p>Para 2<h1>Heading</h1><p>Para 3<hr>After Hr";
        Document doc = Parser.htmlParser().parseInput(html, "");
        assertEquals(3, doc.getElementsByTag("p").size());
        assertEquals("Para 1", doc.getElementsByTag("p").get(0).text());
        assertEquals("Para 2", doc.getElementsByTag("p").get(1).text());
        assertEquals("Para 3", doc.getElementsByTag("p").get(2).text());

        // Headings popping: <h1><h2></h2></h1>
        String headHtml = "<h1>H1 <h2>H2</h2></h1>";
        Document headDoc = Parser.htmlParser().parseInput(headHtml, "");
        assertEquals(1, headDoc.getElementsByTag("h1").size());
        assertEquals(1, headDoc.getElementsByTag("h2").size());

        // List items auto-closing
        String listHtml = "<ul><li>Item 1<li>Item 2<ol><li>Nested 1<li>Nested 2</ol><li>Item 3</ul>";
        Document listDoc = Parser.htmlParser().parseInput(listHtml, "");
        assertEquals(5, listDoc.getElementsByTag("li").size());

        // Definition lists auto-closing
        String dlHtml = "<dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document dlDoc = Parser.htmlParser().parseInput(dlHtml, "");
        assertEquals(2, dlDoc.getElementsByTag("dt").size());
        assertEquals(2, dlDoc.getElementsByTag("dd").size());
    }

    @Test(timeout = 4000)
    public void testInBodyFormsButtonsAndInputs() {
        // Nested form is ignored with error
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<form id=\"f1\"><input type=\"text\"><form id=\"f2\"><input type=\"hidden\"></form></form>", "");
        assertFalse(parser.getErrors().isEmpty());
        assertEquals(1, doc.getElementsByTag("form").size());
        assertEquals("f1", doc.getElementsByTag("form").first().id());

        // Nested button closes previous button
        Document btnDoc = Parser.htmlParser().parseInput("<button>Btn 1 <button>Btn 2</button></button>", "");
        assertEquals(2, btnDoc.getElementsByTag("button").size());

        // Image tag outside svg transformed to img
        Document imgDoc = Parser.htmlParser().parseInput("<image src=\"pic.png\">", "");
        assertEquals(1, imgDoc.getElementsByTag("img").size());

        // Image inside svg preserved
        Document svgDoc = Parser.htmlParser().parseInput("<svg><image href=\"pic.png\"></svg>", "");
        assertEquals(1, svgDoc.getElementsByTag("image").size());
    }

    @Test(timeout = 4000)
    public void testInBodySpecialBlocks() {
        // Plaintext tag
        Document plainDoc = Parser.htmlParser().parseInput("<plaintext>some <b>text</b></plaintext>", "");
        assertEquals("some <b>text</b></plaintext>", plainDoc.body().text());

        // Xmp, iframe, noembed
        Document rawDoc = Parser.htmlParser().parseInput("<xmp><b>xmp</b></xmp><iframe><p>iframe</p></iframe><noembed>raw</noembed>", "");
        assertEquals("<b>xmp</b>", rawDoc.getElementsByTag("xmp").first().text());
        assertEquals(1, rawDoc.getElementsByTag("iframe").size());
        assertEquals(1, rawDoc.getElementsByTag("noembed").size());

        // Ruby handling
        Document rubyDoc = Parser.htmlParser().parseInput("<ruby>base<rt>rt</rt><rp>rp</rp></ruby>", "");
        assertEquals(1, rubyDoc.getElementsByTag("ruby").size());

        // Applet / Marquee / Object markers
        Document appletDoc = Parser.htmlParser().parseInput("<object><marquee><applet code=\"Foo\"></applet></marquee></object>", "");
        assertEquals(1, appletDoc.getElementsByTag("object").size());
        assertEquals(1, appletDoc.getElementsByTag("marquee").size());
    }

    @Test(timeout = 4000)
    public void testInTableStructureAndFosterParenting() {
        // Non-whitespace character tokens inside table are foster parented outside table
        String html = "<table>foo<tr><td>bar</td></tr>baz</table>";
        Document doc = Parser.htmlParser().parseInput(html, "");
        // "foo" and "baz" should be foster-parented before the table in body
        assertTrue(doc.body().html().startsWith("foobaz<table>"));
        assertEquals("bar", doc.select("table td").first().text());

        // Whitespace inside table is preserved within table structure
        Document wsDoc = Parser.htmlParser().parseInput("<table>   <tr><td>cell</td></tr>\n</table>", "");
        assertEquals("cell", wsDoc.select("td").first().text());

        // Hidden input in table vs visible input
        Document inputDoc = Parser.htmlParser().parseInput("<table><input type=\"hidden\" name=\"token\" value=\"123\"><tr><td>c</td></tr></table>", "");
        assertEquals(1, inputDoc.select("table > input[type=hidden]").size());

        // Script and style in table
        Document scriptDoc = Parser.htmlParser().parseInput("<table><script>var x = 1;</script><style>td{}</style><tr><td>cell</td></tr></table>", "");
        assertEquals(1, scriptDoc.getElementsByTag("script").size());
        assertEquals(1, scriptDoc.getElementsByTag("style").size());
    }

    @Test(timeout = 4000)
    public void testInCaptionAndColumnGroup() {
        String html = "<table>" +
                "<caption>Table Caption <p>para in caption</p></caption>" +
                "<colgroup><col span=\"1\"><col span=\"2\"></colgroup>" +
                "<tbody><tr><td>cell</td></tr></tbody>" +
                "</table>";
        Document doc = Parser.htmlParser().parseInput(html, "");
        assertEquals("Table Caption para in caption", doc.select("caption").first().text());
        assertEquals(2, doc.select("colgroup > col").size());
        assertEquals("cell", doc.select("tbody td").first().text());
    }

    @Test(timeout = 4000)
    public void testInTableBodyRowAndCell() {
        // Missing <tr> and <tbody> auto-generation
        String html = "<table><td>Direct Cell 1</td><th>Header</th><td>Direct Cell 2</td></table>";
        Document doc = Parser.htmlParser().parseInput(html, "");
        assertEquals(1, doc.getElementsByTag("tbody").size());
        assertEquals(1, doc.getElementsByTag("tr").size());
        assertEquals(2, doc.getElementsByTag("td").size());
        assertEquals(1, doc.getElementsByTag("th").size());

        // Template in row and table body
        Document tmplDoc = Parser.htmlParser().parseInput("<table><tbody><template><tr><td>t</td></tr></template><tr><td>c</td></tr></tbody></table>", "");
        assertEquals(1, tmplDoc.getElementsByTag("template").size());
    }

    @Test(timeout = 4000)
    public void testInSelectAndInSelectInTable() {
        // Normal select with option and optgroup
        String selectHtml = "<select name=\"choice\">" +
                "<option value=\"1\">One</option>" +
                "<optgroup label=\"group\">" +
                "<option value=\"2\">Two</option>" +
                "</optgroup>" +
                "</select>";
        Document doc = Parser.htmlParser().parseInput(selectHtml, "");
        assertEquals(2, doc.getElementsByTag("option").size());
        assertEquals(1, doc.getElementsByTag("optgroup").size());

        // Select inside table closed by table elements
        String tableSelect = "<table><tr><td><select><option>1</option><td>other cell</td></tr></table>";
        Document tsDoc = Parser.htmlParser().parseInput(tableSelect, "");
        assertEquals(1, tsDoc.getElementsByTag("select").size());
        assertEquals(2, tsDoc.getElementsByTag("td").size());
    }

    @Test(timeout = 4000)
    public void testFramesetHierarchy() {
        String html = "<html><head><title>Frames</title></head>" +
                "<frameset rows=\"50%,50%\">" +
                "<frame src=\"top.html\">" +
                "<frame src=\"bottom.html\">" +
                "<noframes><p>No frames</p></noframes>" +
                "</frameset></html>";
        Document doc = Parser.htmlParser().parseInput(html, "");
        assertEquals(1, doc.getElementsByTag("frameset").size());
        assertEquals(2, doc.getElementsByTag("frame").size());
        assertEquals(1, doc.getElementsByTag("noframes").size());
        assertEquals(0, doc.getElementsByTag("body").size());
    }

    @Test(timeout = 4000)
    public void testAfterBodyAndAfterAfterBody() {
        String html = "<html><head></head><body>Main body</body></html>" +
                "<!-- trailing comment -->" +
                "<div>Trailing Content</div>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        assertFalse(parser.getErrors().isEmpty()); // trailing content triggers parse error
        assertEquals(1, doc.getElementsByTag("div").size());
        assertEquals("Trailing Content", doc.getElementsByTag("div").first().text());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testBvaEmptyAndWhitespaceOnly() {
        Document emptyDoc = Parser.htmlParser().parseInput("", "http://example.com");
        assertNotNull(emptyDoc);
        assertNotNull(emptyDoc.head());
        assertNotNull(emptyDoc.body());

        Document wsDoc = Parser.htmlParser().parseInput("     \n\t   \r\n   ", "http://example.com");
        assertNotNull(wsDoc);
        assertNotNull(wsDoc.body());
    }

    @Test(timeout = 4000)
    public void testBvaCommentsOnly() {
        Document doc = Parser.htmlParser().parseInput("<!-- c1 --><!-- c2 -->", "http://example.com");
        assertNotNull(doc);
        assertEquals(2, doc.childNodeSize() - 1); // comments attached
    }

    @Test(timeout = 4000)
    public void testBvaNullCharacterHandling() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<p>Hello\u0000World</p>", "");
        assertFalse(parser.getErrors().isEmpty());
        assertTrue(doc.text().contains("Hello") && doc.text().contains("World"));
    }

    @Test(timeout = 4000)
    public void testBvaFragmentParsingAcrossContexts() {
        Element contextBody = new Element(Tag.valueOf("body"), "");
        List<Node> nodesBody = Parser.parseFragment("<p>Fragment Paragraph</p>", contextBody, "");
        assertEquals(1, nodesBody.size());
        assertEquals("p", nodesBody.get(0).nodeName());

        Element contextTable = new Element(Tag.valueOf("table"), "");
        List<Node> nodesTable = Parser.parseFragment("<tr><td>Row</td></tr>", contextTable, "");
        assertFalse(nodesTable.isEmpty());

        Element contextSelect = new Element(Tag.valueOf("select"), "");
        List<Node> nodesSelect = Parser.parseFragment("<option>Opt 1</option><option>Opt 2</option>", contextSelect, "");
        assertEquals(2, nodesSelect.size());

        Element contextFrameset = new Element(Tag.valueOf("frameset"), "");
        List<Node> nodesFrameset = Parser.parseFragment("<frame src=\"a.html\">", contextFrameset, "");
        assertEquals(1, nodesFrameset.size());
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS (Direct State Processing)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectTokenProcessingGuards() {
        HtmlTreeBuilder tb = createBuilder("");

        // Doctype token in InHead returns false
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        assertFalse(HtmlTreeBuilderState.InHead.process(doctype, tb));

        // Doctype token in InTable returns false
        assertFalse(HtmlTreeBuilderState.InTable.process(doctype, tb));

        // Null character in InBody returns false
        Token.Character nullChar = new Token.Character().data("\u0000");
        assertFalse(HtmlTreeBuilderState.InBody.process(nullChar, tb));

        // Null character in InTableText returns false
        assertFalse(HtmlTreeBuilderState.InTableText.process(nullChar, tb));

        // Null character in InSelect returns false
        assertFalse(HtmlTreeBuilderState.InSelect.process(nullChar, tb));

        // Foreign content returns true
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(new Token.Character().data("txt"), tb));
    }

    @Test(timeout = 4000)
    public void testDirectUnexpectedEndTags() {
        HtmlTreeBuilder tb = createBuilder("");

        // EndTag 'div' in InSelect returns false
        Token.EndTag endDiv = new Token.EndTag();
        endDiv.name("div");
        assertFalse(HtmlTreeBuilderState.InSelect.process(endDiv, tb));

        // EndTag 'div' in AfterFrameset returns false
        assertFalse(HtmlTreeBuilderState.AfterFrameset.process(endDiv, tb));

        // EndTag 'div' in AfterAfterFrameset returns false
        assertFalse(HtmlTreeBuilderState.AfterAfterFrameset.process(endDiv, tb));

        // InCell closing td when not in scope returns false and transitions to InRow
        Token.EndTag endTd = new Token.EndTag();
        endTd.name("td");
        assertFalse(HtmlTreeBuilderState.InCell.process(endTd, tb));
        assertEquals(HtmlTreeBuilderState.InRow, tb.state());
    }

    @Test(timeout = 4000)
    public void testInTableEofWithHtmlElement() {
        HtmlTreeBuilder tb = createBuilder("");
        tb.getStack().add(new Element(Tag.valueOf("html"), ""));
        Token.EOF eof = new Token.EOF();
        boolean processed = HtmlTreeBuilderState.InTable.process(eof, tb);
        assertTrue(processed);
        assertFalse(tb.getErrors().isEmpty());
    }

    @Test(timeout = 4000)
    public void testInTableBodyExitTableBodyFragError() {
        HtmlTreeBuilder tb = createBuilder("");
        Token.StartTag caption = new Token.StartTag();
        caption.name("caption");
        boolean processed = HtmlTreeBuilderState.InTableBody.process(caption, tb);
        assertFalse(processed);
        assertFalse(tb.getErrors().isEmpty());
    }

    @Test(timeout = 4000)
    public void testInRowHandleMissingTrFragError() {
        HtmlTreeBuilder tb = createBuilder("");
        Token.StartTag caption = new Token.StartTag();
        caption.name("caption");
        boolean processed = HtmlTreeBuilderState.InRow.process(caption, tb);
        assertFalse(processed);
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumIntegrity() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertEquals(23, states.length);

        for (HtmlTreeBuilderState state : states) {
            assertNotNull(state);
            assertEquals(state, HtmlTreeBuilderState.valueOf(state.name()));
        }
    }

    @Test(timeout = 4000)
    public void testConstantsArraysSortedContract() {
        List<String[]> arrays = Arrays.asList(
                HtmlTreeBuilderState.Constants.InBodyStartToHead,
                HtmlTreeBuilderState.Constants.InBodyStartPClosers,
                HtmlTreeBuilderState.Constants.Headings,
                HtmlTreeBuilderState.Constants.InBodyStartPreListing,
                HtmlTreeBuilderState.Constants.InBodyStartLiBreakers,
                HtmlTreeBuilderState.Constants.DdDt,
                HtmlTreeBuilderState.Constants.Formatters,
                HtmlTreeBuilderState.Constants.InBodyStartApplets,
                HtmlTreeBuilderState.Constants.InBodyStartEmptyFormatters,
                HtmlTreeBuilderState.Constants.InBodyStartMedia,
                HtmlTreeBuilderState.Constants.InBodyStartInputAttribs,
                HtmlTreeBuilderState.Constants.InBodyStartOptions,
                HtmlTreeBuilderState.Constants.InBodyStartRuby,
                HtmlTreeBuilderState.Constants.InBodyStartDrop,
                HtmlTreeBuilderState.Constants.InBodyEndClosers,
                HtmlTreeBuilderState.Constants.InBodyEndAdoptionFormatters,
                HtmlTreeBuilderState.Constants.InBodyEndTableFosters
        );

        for (String[] arr : arrays) {
            assertNotNull(arr);
            assertTrue(arr.length > 0);
            for (int i = 0; i < arr.length - 1; i++) {
                assertTrue("Array elements must be sorted: " + arr[i] + " <= " + arr[i + 1],
                        arr[i].compareTo(arr[i + 1]) < 0);
            }
        }
    }
}