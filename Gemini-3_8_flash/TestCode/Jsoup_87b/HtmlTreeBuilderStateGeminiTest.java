package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Targeted Class: org.jsoup.parser.HtmlTreeBuilderState
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J ground truth: HtmlParserTest::preservedCaseLinksCantNest):
 *    - InBody tag "a" handling: when active formatting element exists for "a", case sensitivity with
 *      ParseSettings.preserveCase causes mismatch if nodeName() vs normalName() lookup fails.
 *      Target: <A> ONE <A> Two </A> under ParseSettings.preserveCase must correctly close the first <A>.
 *
 * 2. CORE FUNCTIONAL LOGIC & STATE TRANSITIONS (Coverage across all 23 enum states):
 *    - Initial: whitespace skip, comments, doctype (normal & quirks), anythingElse fallback.
 *    - BeforeHtml: doctype error, comments, whitespace, <html> tag, end tags (head, body, html, br),
 *      invalid end tags error, anythingElse.
 *    - BeforeHead: whitespace, comment, doctype error, <html> tag, <head> tag, end tags, fallback.
 *    - InHead: whitespace, comments, doctype error, <base> (href base URI update), <meta>, <title> (rcdata),
 *      <style>/<noframes> (rawtext), <noscript>, <script> (tokeniser transition), <head> error, </head>,
 *      end tags (body, html, br), invalid end tags.
 *    - InHeadNoscript: doctype error, <html> tag, </noscript>, whitespace/comments/<link>/<meta>,
 *      <br> end tag, <head>/<noscript> errors, fallback characters.
 *    - AfterHead: whitespace, comments, doctype, <html>, <body> (framesetOk false), <frameset>,
 *      tags to push head (<meta>, <link>, <style>, <script>), <head> error, end tags (body, html), fallback.
 *    - InBody:
 *      * Character tokens (null character error vs normal text, whitespace with framesetOk).
 *      * Start tags: <a> (nesting / Adoption Agency Algorithm), InBodyStartEmptyFormatters (img, br, wbr, etc.),
 *        InBodyStartPClosers (p, div, etc.), <li> with existing li/special breakers, <html> attribute merge,
 *        InBodyStartToHead, <body> attribute merge & fragment check, <frameset> (rejection vs body replacement),
 *        Headings (h1-h6 nesting & auto-closing), PreListing (<pre>/<listing> LF consumption), <form> nesting,
 *        DdDt (<dd>/<dt>), <plaintext>, <button> (inButtonScope auto-close), Formatters (b, i, etc.),
 *        <nobr> (inScope error & close), InBodyStartApplets (applet/marquee/object marker), <table> (quirks vs standards p closing),
 *        <input> (type="hidden" vs normal), InBodyStartMedia, <hr>, <image> (converted to <img> except in svg),
 *        <isindex> prompt/action/input simulation, <textarea> (Rcdata), <xmp>/<iframe>/<noembed> (Rawtext),
 *        <select> (InSelect vs InSelectInTable), InBodyStartOptions, InBodyStartRuby, <math>/<svg>, InBodyStartDrop.
 *      * End tags: InBodyEndAdoptionFormatters (AAA with furthestBlock, commonAncestor, foster parenting),
 *        InBodyEndClosers, <span>, <li>, <body>, <html>, <form>, <p>, DdDt, Headings, InBodyStartApplets,
 *        <br> (error + startTag inserted), fallback anyOtherEndTag.
 *    - Text: characters, EOF error + recovery, end tag popping.
 *    - InTable: character handling (transition to InTableText), doctype error, caption, colgroup, col,
 *      tbody/tfoot/thead, td/th/tr, nested <table> error/reprocess, script/style, <input hidden>, <form>,
 *      anythingElse foster parenting, end tag table / resetInsertionMode, EOF.
 *    - InTableText: null character error, pending characters flushing (whitespace vs foster-parented text).
 *    - InCaption: end tag caption, table/body start tags error, end tags error, InBody fallback.
 *    - InColumnGroup: whitespace, col start tag, colgroup end tag, fallback.
 *    - InTableBody: template, tr, th/td auto-tr, exitTableBody on caption/colgroup/etc., end tags.
 *    - InRow: template, th/td transition to InCell, missing tr handling, end tag tr, end tag tbody/thead/tfoot.
 *    - InCell: end tags td/th (InCellNames), InCellBody errors, InCellTable closeCell, start tags InCellCol.
 *    - InSelect: characters (null character check), comments, doctype error, html, option, optgroup,
 *      nested select error, input/keygen/textarea auto-close, script, end tags optgroup/option/select, EOF.
 *    - InSelectInTable: start/end table tags auto-close select and reprocess.
 *    - AfterBody: whitespace, comments, doctype error, html start/end (fragment check), EOF, fallback.
 *    - InFrameset: whitespace, comments, doctype error, html, frameset, frame, noframes, end tag frameset, EOF.
 *    - AfterFrameset: whitespace, comments, doctype, html, noframes, end tag html, EOF.
 *    - AfterAfterBody: comments, doctype/whitespace/html, EOF, fallback.
 *    - AfterAfterFrameset: comments, doctype/whitespace/html, noframes, EOF, fallback.
 *    - ForeignContent: process returns true.
 *
 * 3. BOUNDARY VALUE ANALYSIS & ARRAY INTEGRITY:
 *    - HtmlTreeBuilderState.Constants arrays must be sorted for StringUtil.inSorted binary search.
 */
public class HtmlTreeBuilderStateGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets: HtmlParserTest::preservedCaseLinksCantNest
     * InBody processing of <a> start tag when ParseSettings.preserveCase is active.
     * When an active formatting element for <a> exists, it must be properly closed
     * even if uppercase tags like <A> are used.
     */
    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNestDefect() {
        String html = "<A> ONE <A> Two </A>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<A> ONE </A> <A> Two </A>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

    /**
     * Additional check for links cannot nest with attributes under preserveCase.
     */
    @Test(timeout = 4000)
    public void testPreservedCaseLinksWithAttributesCantNest() {
        String html = "<A HREF='x'>1<A HREF='y'>2</A>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<A HREF=\"x\">1</A><A HREF=\"y\">2</A>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

    // =========================================================================
    // PARTITION A: CONSTANTS ARRAYS INTEGRITY (Binary Search Precondition)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllConstantsArraysAreProperlySorted() {
        assertArraySorted("InBodyStartToHead", HtmlTreeBuilderState.Constants.InBodyStartToHead);
        assertArraySorted("InBodyStartPClosers", HtmlTreeBuilderState.Constants.InBodyStartPClosers);
        assertArraySorted("Headings", HtmlTreeBuilderState.Constants.Headings);
        assertArraySorted("InBodyStartPreListing", HtmlTreeBuilderState.Constants.InBodyStartPreListing);
        assertArraySorted("InBodyStartLiBreakers", HtmlTreeBuilderState.Constants.InBodyStartLiBreakers);
        assertArraySorted("DdDt", HtmlTreeBuilderState.Constants.DdDt);
        assertArraySorted("Formatters", HtmlTreeBuilderState.Constants.Formatters);
        assertArraySorted("InBodyStartApplets", HtmlTreeBuilderState.Constants.InBodyStartApplets);
        assertArraySorted("InBodyStartEmptyFormatters", HtmlTreeBuilderState.Constants.InBodyStartEmptyFormatters);
        assertArraySorted("InBodyStartMedia", HtmlTreeBuilderState.Constants.InBodyStartMedia);
        assertArraySorted("InBodyStartInputAttribs", HtmlTreeBuilderState.Constants.InBodyStartInputAttribs);
        assertArraySorted("InBodyStartOptions", HtmlTreeBuilderState.Constants.InBodyStartOptions);
        assertArraySorted("InBodyStartRuby", HtmlTreeBuilderState.Constants.InBodyStartRuby);
        assertArraySorted("InBodyStartDrop", HtmlTreeBuilderState.Constants.InBodyStartDrop);
        assertArraySorted("InBodyEndClosers", HtmlTreeBuilderState.Constants.InBodyEndClosers);
        assertArraySorted("InBodyEndAdoptionFormatters", HtmlTreeBuilderState.Constants.InBodyEndAdoptionFormatters);
        assertArraySorted("InBodyEndTableFosters", HtmlTreeBuilderState.Constants.InBodyEndTableFosters);
        assertArraySorted("InCellNames", HtmlTreeBuilderState.Constants.InCellNames);
        assertArraySorted("InCellBody", HtmlTreeBuilderState.Constants.InCellBody);
        assertArraySorted("InCellTable", HtmlTreeBuilderState.Constants.InCellTable);
        assertArraySorted("InCellCol", HtmlTreeBuilderState.Constants.InCellCol);
    }

    private void assertArraySorted(String name, String[] arr) {
        String[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);
        assertArrayEquals("Array Constants." + name + " is not sorted!", copy, arr);
    }

    // =========================================================================
    // PARTITION B: INITIAL, BEFORE HTML, BEFORE HEAD, IN HEAD & IN HEAD NOSCRIPT
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialAndBeforeHtmlTransitions() {
        // Comments before doctype, whitespace, doctype quirks
        String html = "   <!-- comment 1 -->\n<!DOCTYPE html PUBLIC \"-//W3O//DTD W3 HTML Strict//EN\" \"\"><html><!-- c2 --><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().name());

        // Initial without doctype (falls straight to BeforeHtml -> BeforeHead)
        Document noDoctype = Jsoup.parse("<!-- c --><html><head></head><body>Hello</body></html>");
        assertEquals("Hello", noDoctype.body().text());

        // BeforeHtml invalid doctype error path
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<html><!DOCTYPE html></html>", "");
        assertTrue("Expected parse error for misplaced doctype", parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testBeforeHeadTransitions() {
        // Misplaced doctype in BeforeHead and premature end tags
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<html><!DOCTYPE html></br></foo><head><title>Test</title></head><body>Text</body></html>", "");
        assertEquals("Test", doc.title());
        assertEquals("Text", doc.body().text());
        assertTrue(parser.getErrors().size() > 0);

        // BeforeHead encountering start tag <html> directly
        Document doc2 = Jsoup.parse("<html><html lang='en'><head></head><body></body></html>");
        assertEquals("en", doc2.select("html").attr("lang"));
    }

    @Test(timeout = 4000)
    public void testInHeadElements() {
        // <base href="..."> updating baseUri
        String html = "<html><head>" +
                "<base href='http://example.com/sub/'>" +
                "<link rel='stylesheet' href='style.css'>" +
                "<meta charset='utf-8'>" +
                "<title>Head Elements</title>" +
                "<style>body { color: red; }</style>" +
                "<noframes>No frames</noframes>" +
                "<noscript><link rel='stylesheet' href='ns.css'></noscript>" +
                "<script>var a = 1;</script>" +
                "</head><body>Content</body></html>";

        Document doc = Jsoup.parse(html, "http://example.com/");
        assertEquals("http://example.com/sub/", doc.baseUri());
        assertEquals("Head Elements", doc.title());
        assertEquals("body { color: red; }", doc.select("style").first().data());
        assertEquals("var a = 1;", doc.select("script").first().data());
        assertEquals("Content", doc.body().text());

        // Duplicate <head> or misplaced doctype in head
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<html><head><head><!DOCTYPE html></head><body></body></html>", "");
        assertTrue(parser.getErrors().size() >= 2);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptState() {
        // noscript with whitespace, comments, link, meta, br end tag and invalid tags
        String html = "<html><head><noscript><!-- c -->  <link rel='stylesheet'>text in noscript<br></noscript></head><body></body></html>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        assertNotNull(doc.select("head noscript").first());

        // invalid doctype inside noscript
        parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<html><head><noscript><!DOCTYPE html><head></head></noscript></head><body></body></html>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    // =========================================================================
    // PARTITION D: AFTER HEAD, IN BODY & ADOPTION AGENCY ALGORITHM
    // =========================================================================

    @Test(timeout = 4000)
    public void testAfterHeadTransitions() {
        // Tags in AfterHead that belong to head (e.g. meta, link, script, style) pushed back to head
        String html = "<html><head><title>Title</title></head><!-- comment --><meta name='desc' content='d'><style>.x{}</style><body>Content</body></html>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        assertEquals("d", doc.select("head meta[name=desc]").attr("content"));
        assertEquals(".x{}", doc.select("head style").first().data());
        assertTrue(parser.getErrors().size() > 0); // triggers AfterHead error branch

        // Invalid end tag in AfterHead
        parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<html><head></head></span><body>Body</body></html>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testInBodyFormattingAndAdoptionAgencyAlgorithm() {
        // AAA: <b>1<p>2</b>3</p>
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>1</b><p><b>2</b>3</p>", StringUtil.normaliseWhitespace(doc.body().html()));

        // AAA with deep nesting & foster parent inside table context
        String tableAaa = "<table><b>1<td>2</b>3</td></table>";
        Document docTable = Jsoup.parse(tableAaa);
        assertTrue(docTable.body().html().contains("<b>1</b>"));

        // Nobr tag auto-closing
        String nobr = "<nobr>first<nobr>second</nobr>";
        Document docNobr = Jsoup.parse(nobr);
        assertEquals("<nobr>first</nobr><nobr>second</nobr>", StringUtil.normaliseWhitespace(docNobr.body().html()));
    }

    @Test(timeout = 4000)
    public void testInBodySpecialTagsAndButtons() {
        // Button in button scope
        String buttonHtml = "<button>First<button>Second</button>";
        Document docBtn = Jsoup.parse(buttonHtml);
        assertEquals("<button>First</button><button>Second</button>", StringUtil.normaliseWhitespace(docBtn.body().html()));

        // Headings closing headings and P
        String headingHtml = "<p>para<h1>Heading 1<h2>Heading 2</h2></h1>";
        Document docHeadings = Jsoup.parse(headingHtml);
        assertEquals("<p>para</p><h1>Heading 1</h1><h2>Heading 2</h2>", StringUtil.normaliseWhitespace(docHeadings.body().html()));

        // Pre / Listing newline skip
        Document docPre = Jsoup.parse("<pre>\nLine1\nLine2</pre>");
        assertEquals("Line1\nLine2", docPre.select("pre").first().text());

        // Plaintext tag
        Document docPlain = Jsoup.parse("<div><plaintext><b>not bold</b></plaintext></div>");
        assertEquals("<b>not bold</b></plaintext></div>", docPlain.select("plaintext").first().text());

        // Form in Form error
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document docForm = parser.parseInput("<form><form></form></form>", "");
        assertEquals(1, docForm.select("form").size());
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testInBodyListsAndRuby() {
        // Nested LI breakers
        String liHtml = "<ul><li>one<li>two<div>div</div><li>three</li></ul>";
        Document docLi = Jsoup.parse(liHtml);
        assertEquals(3, docLi.select("li").size());

        // Dd and Dt tags auto closing
        String dlHtml = "<dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document docDl = Jsoup.parse(dlHtml);
        assertEquals(2, docDl.select("dt").size());
        assertEquals(2, docDl.select("dd").size());

        // Ruby with rp/rt
        String rubyHtml = "<ruby>漢<rp>(</rp><rt>kan</rt><rp>)</rp>字<rt>ji</rt></ruby>";
        Document docRuby = Jsoup.parse(rubyHtml);
        assertEquals(2, docRuby.select("rt").size());
        assertEquals(2, docRuby.select("rp").size());
    }

    @Test(timeout = 4000)
    public void testInBodyIsindexAndApplets() {
        // Applet / marquee markers
        String appletHtml = "<applet><b>bold<p>para</b></applet>";
        Document docApplet = Jsoup.parse(appletHtml);
        assertNotNull(docApplet.select("applet").first());

        // Early 90s <isindex>
        String isindexHtml = "<isindex action='/search' prompt='Search Here: '>";
        Document docIsindex = Jsoup.parse(isindexHtml);
        assertEquals("/search", docIsindex.select("form").attr("action"));
        assertEquals("Search Here: ", docIsindex.select("label").text());
        assertEquals("isindex", docIsindex.select("input").attr("name"));
    }

    @Test(timeout = 4000)
    public void testInBodyRawtextAndForeign() {
        // Rawtext tags: xmp, iframe, noembed
        String rawHtml = "<xmp><b>xmp</b></xmp><iframe><span>frame</span></iframe><noembed><p>noembed</p></noembed>";
        Document docRaw = Jsoup.parse(rawHtml);
        assertEquals("<b>xmp</b>", docRaw.select("xmp").first().text());
        assertEquals("<span>frame</span>", docRaw.select("iframe").first().text());
        assertEquals("<p>noembed</p>", docRaw.select("noembed").first().text());

        // Image alias to img except in svg
        Document docImg = Jsoup.parse("<image src='foo.jpg'>");
        assertEquals("img", docImg.select("img").first().tagName());

        Document docSvgImg = Jsoup.parse("<svg><image href='bar.jpg'></svg>");
        assertNotNull(docSvgImg.select("svg image").first());

        // Sarcasm tag (anyOtherEndTag)
        Document docSarcasm = Jsoup.parse("<p>Hello <sarcasm>world</sarcasm></p>");
        assertEquals("Hello world", docSarcasm.body().text());

        // Null character in InBody character token
        Parser p = Parser.htmlParser().setTrackErrors(10);
        p.parseInput("<p>Null\u0000Char</p>", "");
        assertTrue(p.getErrors().size() > 0);
    }

    // =========================================================================
    // PARTITION E: TABLES, TABLE TEXT, COLGROUP, CAPTION & CELLS
    // =========================================================================

    @Test(timeout = 4000)
    public void testInTableTransitionsAndFosterParenting() {
        // Text inside table foster-parented
        String tableText = "<table>Foo<tr><td>Bar</td></tr>Baz</table>";
        Document doc = Jsoup.parse(tableText);
        // "Foo" and "Baz" are foster parented outside <table>
        assertTrue(doc.body().html().startsWith("FooBaz<table>"));

        // Table with whitespace only inside table stays or is handled properly
        String tableWs = "<table>   <tr><td>Cell</td></tr>   </table>";
        Document docWs = Jsoup.parse(tableWs);
        assertEquals("Cell", docWs.select("td").first().text());

        // Caption and ColumnGroup
        String tableCols = "<table><caption>Title</caption><colgroup><col span='2'></colgroup><tbody><tr><td>A</td></tr></tbody></table>";
        Document docCols = Jsoup.parse(tableCols);
        assertEquals("Title", docCols.select("caption").first().text());
        assertEquals("2", docCols.select("col").attr("span"));

        // Input hidden inside table vs non-hidden input
        String tableInput = "<table><input type='hidden' name='h' value='v'><input type='text' name='t'><tr><td>Cell</td></tr></table>";
        Document docInput = Jsoup.parse(tableInput);
        assertEquals("v", docInput.select("table > input[type=hidden]").val());
        assertEquals("t", docInput.select("body > input[type=text]").attr("name")); // foster parented
    }

    @Test(timeout = 4000)
    public void testInTableBodyAndInRowAndInCell() {
        // Missing TR / auto-creation of TR and TD/TH
        String html = "<table><tbody><td>One</td><th>Two</th></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("tr").size());
        assertEquals(1, doc.select("td").size());
        assertEquals(1, doc.select("th").size());

        // Misplaced start tags in InCell
        String misplacedHtml = "<table><tr><td>Cell 1<caption>Cap</caption><td>Cell 2</td></tr></table>";
        Document docMisplaced = Jsoup.parse(misplacedHtml);
        assertEquals(2, docMisplaced.select("td").size());

        // Nested table in cell
        String nestedTable = "<table><tr><td><table><tr><td>Nested</td></tr></table></td></tr></table>";
        Document docNested = Jsoup.parse(nestedTable);
        assertEquals("Nested", docNested.select("table table td").first().text());
    }

    // =========================================================================
    // PARTITION F: SELECT & SELECT IN TABLE
    // =========================================================================

    @Test(timeout = 4000)
    public void testSelectStates() {
        // Normal select with optgroup and options
        String selectHtml = "<select name='s'>" +
                "<optgroup label='G1'><option value='1'>One</option><option value='2'>Two</option></optgroup>" +
                "<option value='3'>Three</option>" +
                "</select>";
        Document doc = Jsoup.parse(selectHtml);
        assertEquals(3, doc.select("option").size());
        assertEquals(1, doc.select("optgroup").size());

        // Select inside table (InSelectInTable) encountering table tag auto-closes select
        String selectInTable = "<table><tr><td>" +
                "<select><option>A</option><tr><td>Next Cell</td></tr></select>" +
                "</td></tr></table>";
        Document docST = Jsoup.parse(selectInTable);
        assertNotNull(docST.select("select").first());
        assertEquals("Next Cell", docST.select("tr").get(1).text());

        // Select with unexpected tags (e.g. input / textarea inside select)
        Parser p = Parser.htmlParser().setTrackErrors(10);
        Document docErr = p.parseInput("<select><input type='text'><option>A</option></select>", "");
        assertTrue(p.getErrors().size() > 0);
        assertNotNull(docErr.select("input").first());
    }

    // =========================================================================
    // PARTITION G: FRAMESETS & AFTER BODY / AFTER AFTER BODY
    // =========================================================================

    @Test(timeout = 4000)
    public void testFramesetStates() {
        String framesetHtml = "<html><head><title>Frames</title></head>" +
                "<frameset rows='50%,50%'>" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames support</p></noframes>" +
                "</frameset></html>";
        Document doc = Jsoup.parse(framesetHtml);
        assertEquals(2, doc.select("frame").size());
        assertEquals("Frames", doc.title());
        assertEquals("No frames support", doc.select("noframes").first().text());

        // Body replaced by frameset if framesetOk is true
        Document docReplace = Jsoup.parse("<html><head></head><frameset cols='*'><frame></frameset></html>");
        assertNotNull(docReplace.select("frameset").first());
        assertNull(docReplace.select("body").first());
    }

    @Test(timeout = 4000)
    public void testAfterBodyAndAfterAfterBody() {
        String html = "<html><head></head><body>Hello</body><!-- comment after body --></html><!-- comment after html -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Hello", doc.body().text());
        assertEquals(2, doc.select("html").size() > 0 ? 1 : 0);

        // Content after body tag triggers error and fallback to InBody
        Parser p = Parser.htmlParser().setTrackErrors(10);
        Document docTrailing = p.parseInput("<html><body>Hello</body> trailing text</html>", "");
        assertTrue(p.getErrors().size() > 0);
        assertEquals("Hello trailing text", docTrailing.body().text());
    }

    // =========================================================================
    // PARTITION H: DIRECT STATE PROCESS COVERAGE (Edge & Defensive Branches)
    // =========================================================================

    @Test(timeout = 4000)
    public void testForeignContentDirectProcess() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div></div>", "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Token.Comment comment = new Token.Comment();
        comment.getData().append("test");
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(comment, tb));
    }

    @Test(timeout = 4000)
    public void testDirectProcessOnInitialDoctypeWithForceQuirks() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("", "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.forceQuirks = true;

        assertTrue(HtmlTreeBuilderState.Initial.process(doctype, tb));
        assertEquals(Document.QuirksMode.quirks, tb.getDocument().quirksMode());
    }

    @Test(timeout = 4000)
    public void testInTableDoctypeAndEofErrorPaths() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        tb.initialiseParse("<table>", "", errorList, ParseSettings.htmlDefault);
        tb.transition(HtmlTreeBuilderState.InTable);

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        assertFalse(HtmlTreeBuilderState.InTable.process(doctype, tb));
        assertTrue(errorList.size() > 0);

        Token.EOF eof = new Token.EOF();
        assertTrue(HtmlTreeBuilderState.InTable.process(eof, tb));
    }

    @Test(timeout = 4000)
    public void testInTableTextNullCharacterError() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        tb.initialiseParse("<table>", "", errorList, ParseSettings.htmlDefault);
        tb.transition(HtmlTreeBuilderState.InTableText);

        Token.Character nullChar = new Token.Character();
        nullChar.data("\u0000");

        assertFalse(HtmlTreeBuilderState.InTableText.process(nullChar, tb));
        assertTrue(errorList.size() > 0);
    }

    @Test(timeout = 4000)
    public void testInSelectNullCharacterAndEofError() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        tb.initialiseParse("<select>", "", errorList, ParseSettings.htmlDefault);
        tb.transition(HtmlTreeBuilderState.InSelect);

        Token.Character nullChar = new Token.Character();
        nullChar.data("\u0000");
        assertFalse(HtmlTreeBuilderState.InSelect.process(nullChar, tb));

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        assertFalse(HtmlTreeBuilderState.InSelect.process(doctype, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(HtmlTreeBuilderState.InSelect.process(eof, tb));
    }

    @Test(timeout = 4000)
    public void testAfterFramesetInvalidToken() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        tb.initialiseParse("<frameset></frameset>", "", errorList, ParseSettings.htmlDefault);
        tb.transition(HtmlTreeBuilderState.AfterFrameset);

        Token.StartTag invalidStart = new Token.StartTag();
        invalidStart.name("div");
        assertFalse(HtmlTreeBuilderState.AfterFrameset.process(invalidStart, tb));
        assertTrue(errorList.size() > 0);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetInvalidToken() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        tb.initialiseParse("<frameset></frameset>", "", errorList, ParseSettings.htmlDefault);
        tb.transition(HtmlTreeBuilderState.AfterAfterFrameset);

        Token.StartTag invalidStart = new Token.StartTag();
        invalidStart.name("span");
        assertFalse(HtmlTreeBuilderState.AfterAfterFrameset.process(invalidStart, tb));
        assertTrue(errorList.size() > 0);
    }
}