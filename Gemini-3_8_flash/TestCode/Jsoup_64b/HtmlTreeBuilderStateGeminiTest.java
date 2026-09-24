package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.parser.HtmlTreeBuilderState
 *
 * 1. DEFECT-TARGETED BRANCHES (Defects4J Ground Truth):
 *    - InHead state: Handling of self-closing / empty <style /> and <noframes /> tags.
 *      Bug: Unconditionally executing handleRawtext transitions the tokenizer to Rawtext and tree builder
 *      to Text mode without checking if the tag was self-closing/empty, swallowing subsequent head/body
 *      elements (e.g., <meta>, <body>) into raw text until EOF.
 *      Target Tests: testHandlesKnownEmptyStyleDefect, testHandlesKnownEmptyNoFramesDefect.
 *
 * 2. PARTITION A: Core Functional Logic & State Transitions
 *    - Initial: Whitespace ignore, comment insertion, doctype quirks vs no-quirks, transition to BeforeHtml.
 *    - BeforeHtml: Doctype error, comments, whitespace, <html> start tag transition to BeforeHead,
 *      end tags ("head", "body", "html", "br") invoking anythingElse, unexpected end tag error.
 *    - BeforeHead: Whitespace, comment, doctype error, <html> to InBody, <head> to InHead,
 *      anythingElse auto-inserting <head>.
 *    - InHead: Whitespace as characters, comments, doctype error, <base> setting URI, <meta>, <title> (RcData),
 *      <style>/<noframes> (Rawtext), <noscript> to InHeadNoscript, <script> to ScriptData,
 *      unexpected <head> error, </head> transition to AfterHead, anythingElse.
 *    - InHeadNoscript: Whitespace, comments, head tags redirected to InHead, </noscript> back to InHead,
 *      </br> error handling, unexpected doctype/head/noscript.
 *    - AfterHead: Whitespace, comments, <body> to InBody (framesetOk=false), <frameset> to InFrameset,
 *      tags pushed back to InHead (<meta>, <link>, etc.), anythingElse inserting <body> (framesetOk=true).
 *    - InBody:
 *      * Character tokens: null character error, whitespace with framesetOk vs without, text insertion.
 *      * Start tags: <a> active formatting reset, formatting elements, <p> closers, <li> breakers,
 *        <html>/<body attributes merging, <frameset> handling, headings closing headings, <pre>/<listing>,
 *        <form> duplicates guard, <dd>/<dt>, <plaintext>, <button> nesting, <nobr> duplicate scope,
 *        applets, <table> quirks check and InTable transition, <input> hidden vs visible, media tags,
 *        <hr>, <image> converted to <img> (unless in SVG), <isindex> prompt/form generation,
 *        <textarea> to Rcdata, <xmp>/<iframe>/<noembed> Rawtext, <select> transition to InSelect/InSelectInTable,
 *        ruby elements, math/svg foreign elements, InBodyStartDrop errors.
 *      * End tags: Adoption Agency Algorithm (AAA) branches (formatting element null, not on stack,
 *        not in scope, furthestBlock scan, bookmark shifting, foster parenting), closers, <li> scope,
 *        <body> transition to AfterBody, <html> re-processing, <form> stack removal, <p> button scope,
 *        <dd>/<dt>, headings, applets marker clearing, </br> error auto-open.
 *      * anyOtherEndTag fallback loop and isSpecial guard.
 *    - Text: Character insertion, EOF error with pop and originalState transition, end tag pop.
 *    - InTable: Characters to InTableText with pending characters, comments, caption/colgroup/col/tbody/
 *      tfoot/thead/tr/td/th transitions, nested table error, hidden input vs visible foster insertion,
 *      table end tag reset, EOF error handling, anythingElse foster parenting.
 *    - InTableText: null character error, pending whitespace insertion vs non-whitespace foster parenting.
 *    - InCaption, InColumnGroup, InTableBody, InRow, InCell: Structural table transitions, missing <tr>/<td>
 *      recovery, clearStack contexts, scope checks.
 *    - InSelect & InSelectInTable: Option/optgroup nesting and auto-closing, nested select/input/keygen/
 *      textarea errors, table tags triggering select closure.
 *    - AfterBody, InFrameset, AfterFrameset, AfterAfterBody, AfterAfterFrameset: Trailing comments,
 *      whitespace, doctype recovery, fragment parsing checks.
 *    - ForeignContent: Dummy pass-through branch.
 *
 * 3. PARTITION B: Boundary Value Analysis (BVA) & Extremes
 *    - Empty strings, whitespace-only documents, null characters (\u0000).
 *    - Deeply nested formatting tags (>8 iterations, >64 stack elements in Adoption Agency Algorithm).
 *    - Multiple frameset and fragment parsing permutations.
 *
 * 4. PARTITION C: Defect-Targeted Regression Assertions
 *    - Verifying known Defects4J issue where self-closing style/noframes swallowed elements.
 *
 * 5. PARTITION D & E: Defensive Guard Paths & Enum Contract Integrity
 *    - Direct invocation of state.process(Token, HtmlTreeBuilder) with unexpected tokens.
 *    - Enum valueOf, values(), and ordinal contracts.
 * ====================================================================================================
 */
public class HtmlTreeBuilderStateGeminiTest {

    private HtmlTreeBuilder createBuilder(String baseUri) {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), baseUri, ParseErrorList.tracking(100), ParseSettings.htmlDefault);
        return tb;
    }

    private Document parseWithErrors(String html, List<ParseError> errorList) {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(500);
        Document doc = parser.parseInput(html, "http://example.com/");
        if (errorList != null) {
            errorList.addAll(parser.getErrors());
        }
        return doc;
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlesKnownEmptyStyleDefect() {
        String h = "<html><head><style /><meta name=foo></head><body>One</body></html>";
        Document doc = Parser.parse(h, "http://example.com/");
        Element meta = doc.select("meta").first();
        assertNotNull("Meta tag should exist in head and not be consumed by style", meta);
        assertEquals("foo", meta.attr("name"));
        assertEquals("One", doc.body().text());
        Element style = doc.select("style").first();
        assertNotNull("Style element should exist", style);
        assertEquals("", style.data());
    }

    @Test(timeout = 4000)
    public void testHandlesKnownEmptyNoFramesDefect() {
        String h = "<html><head><noframes /><meta name=foo></head><body>One</body></html>";
        Document doc = Parser.parse(h, "http://example.com/");
        Element meta = doc.select("meta").first();
        assertNotNull("Meta tag should exist in head and not be consumed by noframes", meta);
        assertEquals("foo", meta.attr("name"));
        assertEquals("One", doc.body().text());
        Element noframes = doc.select("noframes").first();
        assertNotNull("Noframes element should exist", noframes);
        assertEquals("", noframes.data());
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateTransitionsAndQuirks() {
        String htmlQuirks = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><body>Quirks</body></html>";
        Document docQuirks = Parser.parse(htmlQuirks, "");
        assertNotNull(docQuirks.documentType());
        assertEquals("html", docQuirks.documentType().name());

        // Comment and whitespace before doctype
        String htmlWithComment = "   <!-- leading comment -->\n<!DOCTYPE html><html><body>Test</body></html>";
        Document docComment = Parser.parse(htmlWithComment, "");
        assertEquals("leading comment", ((Comment) docComment.childNode(0)).getData().trim());

        // Direct token testing on Initial state
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        assertTrue(HtmlTreeBuilderState.Initial.process(new Token.Character().data("   \t\n"), tb));

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.forceQuirks = true;
        assertTrue(HtmlTreeBuilderState.Initial.process(doctype, tb));
        assertEquals(Document.QuirksMode.quirks, tb.getDocument().quirksMode());
        assertEquals(HtmlTreeBuilderState.BeforeHtml, tb.state());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlTransitionsAndErrors() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.BeforeHtml);

        // Doctype in BeforeHtml should trigger error
        assertFalse(HtmlTreeBuilderState.BeforeHtml.process(new Token.Doctype(), tb));

        // Comment in BeforeHtml
        Token.Comment comment = new Token.Comment();
        comment.data.append("before-html-comment");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(comment, tb));

        // Whitespace in BeforeHtml
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(new Token.Character().data("  "), tb));

        // Unexpected end tag in BeforeHtml
        Token.EndTag badEnd = new Token.EndTag();
        badEnd.name("div");
        assertFalse(HtmlTreeBuilderState.BeforeHtml.process(badEnd, tb));

        // Expected end tag (e.g. head, body, br) auto-creates html and transitions to BeforeHead
        Token.EndTag brEnd = new Token.EndTag();
        brEnd.name("br");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(brEnd, tb));
        assertEquals("html", tb.getDocument().childNode(tb.getDocument().childNodeSize() - 1).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadTransitionsAndAutoInsertion() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.BeforeHead);

        // Doctype error
        assertFalse(HtmlTreeBuilderState.BeforeHead.process(new Token.Doctype(), tb));

        // Whitespace & comment
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(new Token.Character().data("  "), tb));
        Token.Comment c = new Token.Comment();
        c.data.append("head-comment");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(c, tb));

        // Unexpected end tag
        Token.EndTag badEnd = new Token.EndTag();
        badEnd.name("span");
        assertFalse(HtmlTreeBuilderState.BeforeHead.process(badEnd, tb));

        // Start tag <head> transitions to InHead
        Token.StartTag headTag = new Token.StartTag();
        headTag.name("head");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(headTag, tb));
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
        assertNotNull(tb.getHeadElement());
    }

    @Test(timeout = 4000)
    public void testInHeadElementsAndTransitions() {
        String html = "<html><head>" +
                "<base href=\"http://jsoup.org/base/\">" +
                "<meta name=\"test\" content=\"val\">" +
                "<title>Page Title</title>" +
                "<style>body { color: red; }</style>" +
                "<noscript><link rel=\"stylesheet\" href=\"style.css\"></noscript>" +
                "<script>var x = 1;</script>" +
                "</head><body></body></html>";

        Document doc = Parser.parse(html, "http://jsoup.org/");
        assertEquals("http://jsoup.org/base/", doc.baseUri());
        assertEquals("Page Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("style").size());
        assertEquals(1, doc.head().getElementsByTag("meta").size());

        // InHead direct processing
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InHead);

        // Whitespace inserted as character
        assertTrue(HtmlTreeBuilderState.InHead.process(new Token.Character().data(" "), tb));

        // Doctype error
        assertFalse(HtmlTreeBuilderState.InHead.process(new Token.Doctype(), tb));

        // Start tag <head> error in InHead
        Token.StartTag dupHead = new Token.StartTag();
        dupHead.name("head");
        assertFalse(HtmlTreeBuilderState.InHead.process(dupHead, tb));

        // End tag </head> transitions to AfterHead
        Token.EndTag endHead = new Token.EndTag();
        endHead.name("head");
        tb.insert(new Token.StartTag().name("head"));
        assertTrue(HtmlTreeBuilderState.InHead.process(endHead, tb));
        assertEquals(HtmlTreeBuilderState.AfterHead, tb.state());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptBranches() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InHeadNoscript);

        // Doctype error
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(new Token.Doctype(), tb));

        // Whitespace and comments handled
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(new Token.Character().data("  "), tb));
        Token.Comment comm = new Token.Comment();
        comm.data.append("ns-comment");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(comm, tb));

        // Allowed head tags in noscript
        Token.StartTag meta = new Token.StartTag();
        meta.name("meta");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(meta, tb));

        // End tag </br> in noscript triggers anythingElse
        Token.EndTag br = new Token.EndTag();
        br.name("br");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(br, tb));

        // Illegal start tag in noscript
        Token.StartTag badHead = new Token.StartTag();
        badHead.name("head");
        assertFalse(HtmlTreeBuilderState.InHeadNoscript.process(badHead, tb));

        // End tag </noscript> transitions to InHead
        tb.insert(new Token.StartTag().name("noscript"));
        Token.EndTag endNs = new Token.EndTag();
        endNs.name("noscript");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(endNs, tb));
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
    }

    @Test(timeout = 4000)
    public void testAfterHeadBranches() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.AfterHead);

        // Comment and whitespace
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.Character().data("\n "), tb));
        Token.Comment comm = new Token.Comment();
        comm.data.append("after-head-comment");
        assertTrue(HtmlTreeBuilderState.AfterHead.process(comm, tb));

        // Doctype error
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.Doctype(), tb));

        // Duplicate <head> tag returns false
        Token.StartTag headTag = new Token.StartTag();
        headTag.name("head");
        assertFalse(HtmlTreeBuilderState.AfterHead.process(headTag, tb));

        // Bad end tag returns false
        Token.EndTag pEnd = new Token.EndTag();
        pEnd.name("p");
        assertFalse(HtmlTreeBuilderState.AfterHead.process(pEnd, tb));

        // Start tag <body> transitions to InBody and sets framesetOk=false
        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.name("body");
        assertTrue(HtmlTreeBuilderState.AfterHead.process(bodyTag, tb));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
        assertFalse(tb.framesetOk());
    }

    @Test(timeout = 4000)
    public void testInBodyNullCharactersAndFormatting() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InBody);
        tb.insert(new Token.StartTag().name("body"));

        // Null character in InBody returns false
        Token.Character nullChar = new Token.Character().data(String.valueOf('\u0000'));
        assertFalse(HtmlTreeBuilderState.InBody.process(nullChar, tb));

        // Normal character sets framesetOk to false
        tb.framesetOk(true);
        Token.Character textChar = new Token.Character().data("Hello world");
        assertTrue(HtmlTreeBuilderState.InBody.process(textChar, tb));
        assertFalse(tb.framesetOk());

        // Doctype in body returns false
        assertFalse(HtmlTreeBuilderState.InBody.process(new Token.Doctype(), tb));
    }

    @Test(timeout = 4000)
    public void testInBodyAdoptionAgencyAlgorithm() {
        // AAA handles mis-nested active formatting tags
        String html = "<p><b>1<i>2</b>3</i></p>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.body().select("b").first());
        assertNotNull(doc.body().select("i").first());
        assertEquals("12", doc.body().select("b").text());
        assertEquals("23", doc.body().select("i").text());

        // Nested <a> tag triggers auto-closing of the earlier <a> tag
        String aHtml = "<a>First<a>Second</a></a>";
        Document aDoc = Parser.parse(aHtml, "");
        assertEquals(2, aDoc.body().select("a").size());
        assertEquals("First", aDoc.body().select("a").get(0).text());
        assertEquals("Second", aDoc.body().select("a").get(1).text());
    }

    @Test(timeout = 4000)
    public void testInBodyBlockElementsAndClosers() {
        // Headings close headings
        String headingsHtml = "<h1>Heading 1<h2>Heading 2</h3>";
        Document hDoc = Parser.parse(headingsHtml, "");
        assertEquals(1, hDoc.select("h1").size());
        assertEquals(1, hDoc.select("h2").size());

        // <p> auto-closed by block elements
        String pBlocksHtml = "<p>Paragraph<div>Division</div><p>Para<pre>Code</pre>";
        Document pDoc = Parser.parse(pBlocksHtml, "");
        assertEquals(2, pDoc.select("p").size());
        assertEquals(1, pDoc.select("div").size());
        assertEquals(1, pDoc.select("pre").size());

        // Buttons nesting
        String btnHtml = "<button>Btn1<button>Btn2</button></button>";
        Document btnDoc = Parser.parse(btnHtml, "");
        assertEquals(2, btnDoc.select("button").size());

        // Lists: li closes li
        String listHtml = "<ul><li>One<li>Two<li>Three</ul>";
        Document listDoc = Parser.parse(listHtml, "");
        assertEquals(3, listDoc.select("li").size());

        // Definition list: dt/dd auto-close
        String dlHtml = "<dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document dlDoc = Parser.parse(dlHtml, "");
        assertEquals(2, dlDoc.select("dt").size());
        assertEquals(2, dlDoc.select("dd").size());
    }

    @Test(timeout = 4000)
    public void testInBodySpecialElementsAndFormHandling() {
        // Forms: duplicate nested form ignored
        String formHtml = "<form id=f1><input name=a><form id=f2><input name=b></form></form>";
        Document fDoc = Parser.parse(formHtml, "");
        assertEquals(1, fDoc.select("form").size());
        assertEquals("f1", fDoc.select("form").first().id());

        // Nobr duplicate in scope
        String nobrHtml = "<nobr>One<nobr>Two</nobr></nobr>";
        Document nobrDoc = Parser.parse(nobrHtml, "");
        assertEquals(2, nobrDoc.select("nobr").size());

        // Textarea & Plaintext
        String textPlain = "<textarea>Line1\nLine2</textarea><plaintext>Not <b>bold</b>";
        Document tpDoc = Parser.parse(textPlain, "");
        assertEquals("Line1\nLine2", tpDoc.select("textarea").first().text());
        assertTrue(tpDoc.select("plaintext").text().contains("Not <b>bold</b>"));

        // Image tag outside svg converted to img, but kept inside svg
        String imgHtml = "<image src=\"foo.jpg\"><svg><image href=\"bar.jpg\"></svg>";
        Document imgDoc = Parser.parse(imgHtml, "");
        assertEquals(1, imgDoc.select("img").size());
        assertEquals(1, imgDoc.select("svg image").size());

        // Isindex early 90s tag emulation
        String isindexHtml = "<isindex prompt=\"Search here:\" action=\"/search\">";
        Document isDoc = Parser.parse(isindexHtml, "");
        assertNotNull(isDoc.select("form").first());
        assertNotNull(isDoc.select("input[name=isindex]").first());
        assertTrue(isDoc.text().contains("Search here:"));
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagFallbacks() {
        // </br> end tag should be treated as <br>
        String brHtml = "Line 1</br>Line 2";
        Document brDoc = Parser.parse(brHtml, "");
        assertEquals(1, brDoc.select("br").size());

        // Sarcasm tag fallback
        String sarcasmHtml = "<div><sarcasm>Clever</sarcasm></div>";
        Document sDoc = Parser.parse(sarcasmHtml, "");
        assertEquals(1, sDoc.select("sarcasm").size());

        // Unexpected end tag for unmatched opener
        List<ParseError> errors = new ArrayList<>();
        parseWithErrors("<div></span></div>", errors);
        assertFalse("Should report parse error for unexpected span end tag", errors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testInTableParsingAndFosterParenting() {
        // Foster parenting: non-table tags inside table pushed before table
        String fosterHtml = "<table>A<b>B</b><tr><td>C</td></tr>D</table>";
        Document doc = Parser.parse(fosterHtml, "");
        assertEquals("AB", doc.body().childNodes().get(0).outerHtml() + doc.body().child(0).outerHtml());
        assertEquals("C", doc.select("td").text());

        // Hidden vs visible input in table
        String inputTableHtml = "<table><input type=\"hidden\" name=\"h\" value=\"1\"><input type=\"text\" name=\"t\"><tr><td>Cell</td></tr></table>";
        Document inDoc = Parser.parse(inputTableHtml, "");
        // Hidden input stays in table, text input gets foster-parented
        assertEquals("table", inDoc.select("input[type=hidden]").first().parent().nodeName());
        assertEquals("body", inDoc.select("input[type=text]").first().parent().nodeName());

        // Table null characters
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InTable);
        tb.insert(new Token.StartTag().name("table"));
        Token.Character tableNull = new Token.Character().data(String.valueOf('\u0000'));
        // InTable delegates characters to InTableText
        HtmlTreeBuilderState.InTable.process(tableNull, tb);
        assertEquals(HtmlTreeBuilderState.InTableText, tb.state());
        assertFalse(HtmlTreeBuilderState.InTableText.process(tableNull, tb));
    }

    @Test(timeout = 4000)
    public void testTableStructuralTransitions() {
        String fullTable = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col span=\"1\"></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Body Cell</td></tr></tbody>" +
                "<tfoot><tr><td>Footer Cell</td></tr></tfoot>" +
                "</table>";

        Document doc = Parser.parse(fullTable, "");
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(1, doc.select("col").size());
        assertEquals("Header", doc.select("th").text());
        assertEquals("Body Cell", doc.select("tbody td").text());
        assertEquals("Footer Cell", doc.select("tfoot td").text());

        // Unclosed cells and rows auto-closing
        String looseTable = "<table><tr><td>1<td>2<tr><th>3";
        Document looseDoc = Parser.parse(looseTable, "");
        assertEquals(2, looseDoc.select("tr").size());
        assertEquals(2, looseDoc.select("td").size());
        assertEquals(1, looseDoc.select("th").size());
    }

    @Test(timeout = 4000)
    public void testInSelectAndInSelectInTable() {
        String selectHtml = "<select><option>Opt 1<option selected>Opt 2<optgroup label=\"G\"><option>Opt 3</select>";
        Document doc = Parser.parse(selectHtml, "");
        assertEquals(3, doc.select("option").size());
        assertEquals(1, doc.select("optgroup").size());

        // InSelect closed by disallowed tag
        String selectClosed = "<select><option>1</option><input type=\"text\"><option>2</option></select>";
        Document cDoc = Parser.parse(selectClosed, "");
        assertEquals(1, cDoc.select("select option").size());

        // Select inside table: table start tag forces closure
        String selTableHtml = "<table><tr><td><select><option>A<tr><td>Next</td></tr></select></table>";
        Document stDoc = Parser.parse(selTableHtml, "");
        assertEquals(2, stDoc.select("tr").size());
    }

    @Test(timeout = 4000)
    public void testAfterBodyAndAfterAfterBody() {
        String html = "<html><head></head><body>Content</body></html><!-- Trailing Comment -->   ";
        Document doc = Parser.parse(html, "");
        assertEquals("Content", doc.body().text());
        assertEquals("Trailing Comment", ((Comment) doc.childNode(doc.childNodeSize() - 1)).getData().trim());

        // Direct testing AfterBody and AfterAfterBody
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.AfterBody);
        Token.EndTag endHtml = new Token.EndTag();
        endHtml.name("html");
        assertTrue(HtmlTreeBuilderState.AfterBody.process(endHtml, tb));
        assertEquals(HtmlTreeBuilderState.AfterAfterBody, tb.state());

        // Trailing text in AfterAfterBody transitions back to InBody
        assertTrue(HtmlTreeBuilderState.AfterAfterBody.process(new Token.Character().data("tail"), tb));
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    @Test(timeout = 4000)
    public void testFramesetStates() {
        String framesetHtml = "<html><head><title>Frames</title></head>" +
                "<frameset rows=\"50%,50%\">" +
                "<frame src=\"frame1.html\">" +
                "<frame src=\"frame2.html\">" +
                "<noframes><p>No frames supported</p></noframes>" +
                "</frameset></html><!-- trailing comment -->";

        Document doc = Parser.parse(framesetHtml, "");
        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("noframes").size());
        assertNotNull(doc.select("frameset").first());

        // Direct token testing on InFrameset and AfterFrameset
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InFrameset);
        tb.insert(new Token.StartTag().name("frameset"));

        // Whitespace and comments in InFrameset
        assertTrue(HtmlTreeBuilderState.InFrameset.process(new Token.Character().data("  "), tb));
        Token.Comment comm = new Token.Comment();
        comm.data.append("frame-comment");
        assertTrue(HtmlTreeBuilderState.InFrameset.process(comm, tb));

        // End tag </frameset> transitions to AfterFrameset
        Token.EndTag endFrameset = new Token.EndTag();
        endFrameset.name("frameset");
        assertTrue(HtmlTreeBuilderState.InFrameset.process(endFrameset, tb));
        assertEquals(HtmlTreeBuilderState.AfterFrameset, tb.state());
    }

    @Test(timeout = 4000)
    public void testTextStateHandling() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.Text);

        // Character token inserts
        assertTrue(HtmlTreeBuilderState.Text.process(new Token.Character().data("Raw text data"), tb));

        // EOF in text state pops and restores originalState
        tb.insert(new Token.StartTag().name("script"));
        assertTrue(HtmlTreeBuilderState.Text.process(new Token.EOF(), tb));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBoundaryEmptyAndWhitespaceOnly() {
        Document emptyDoc = Parser.parse("", "");
        assertNotNull(emptyDoc);
        assertNotNull(emptyDoc.body());
        assertEquals("", emptyDoc.body().text());

        Document wsDoc = Parser.parse("     \n\t   ", "");
        assertNotNull(wsDoc);
        assertEquals("", wsDoc.body().text());
    }

    @Test(timeout = 4000)
    public void testBoundaryAttributeMergingOnHtmlAndBody() {
        String mergeHtml = "<html id=\"h1\" lang=\"en\"><html id=\"h2\" class=\"main\">" +
                "<body class=\"b1\" style=\"color:red;\"><body class=\"b2\" id=\"real-body\">" +
                "Text</body></html>";

        Document doc = Parser.parse(mergeHtml, "");
        Element html = doc.select("html").first();
        Element body = doc.body();

        assertEquals("h1", html.id());
        assertEquals("main", html.className());
        assertEquals("b1", body.className());
        assertEquals("real-body", body.id());
    }

    @Test(timeout = 4000)
    public void testBoundaryDeeplyNestedAdoptionAgencyAlgorithm() {
        // Extremely deep nesting of formatting elements testing 8 AAA iterations limit
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("<b>");
        }
        sb.append("Deep Content");
        for (int i = 0; i < 20; i++) {
            sb.append("</b>");
        }
        Document doc = Parser.parse(sb.toString(), "");
        assertTrue(doc.body().text().contains("Deep Content"));
    }

    @Test(timeout = 4000)
    public void testBoundaryFragmentParsingContexts() {
        Element contextTable = new Element(Tag.valueOf("table"), "");
        List<Node> tableNodes = Parser.parseFragment("<tr><td>Cell</td></tr>", contextTable, "http://example.com/");
        assertFalse(tableNodes.isEmpty());
        assertEquals("tbody", tableNodes.get(0).nodeName());

        Element contextSelect = new Element(Tag.valueOf("select"), "");
        List<Node> selectNodes = Parser.parseFragment("<option>Item 1</option><option>Item 2</option>", contextSelect, "http://example.com/");
        assertEquals(2, selectNodes.size());

        Element contextDiv = new Element(Tag.valueOf("div"), "");
        List<Node> divNodes = Parser.parseFragment("<span>Span Text</span>", contextDiv, "http://example.com/");
        assertEquals(1, divNodes.size());
        assertEquals("span", divNodes.get(0).nodeName());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefensiveDroppedTagsInBody() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InBody);
        tb.insert(new Token.StartTag().name("body"));

        String[] droppedTags = {"caption", "col", "colgroup", "frame", "head", "tbody", "td", "tfoot", "th", "thead", "tr"};
        for (String tag : droppedTags) {
            Token.StartTag st = new Token.StartTag();
            st.name(tag);
            assertFalse("Start tag <" + tag + "> in InBody should return false", HtmlTreeBuilderState.InBody.process(st, tb));
        }
    }

    @Test(timeout = 4000)
    public void testDefensiveForeignContentPassthrough() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(new Token.Character().data("foreign"), tb));
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(new Token.EOF(), tb));
    }

    @Test(timeout = 4000)
    public void testDefensiveInTableDoctypeAndUnexpectedTags() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InTable);
        tb.insert(new Token.StartTag().name("table"));

        // Doctype in table returns false
        assertFalse(HtmlTreeBuilderState.InTable.process(new Token.Doctype(), tb));

        // Unmatched end tags in table return false
        String[] illegalTableEndTags = {"body", "caption", "col", "colgroup", "html", "tbody", "td", "tfoot", "th", "thead", "tr"};
        for (String tag : illegalTableEndTags) {
            Token.EndTag et = new Token.EndTag();
            et.name(tag);
            assertFalse("End tag </" + tag + "> in InTable should return false", HtmlTreeBuilderState.InTable.process(et, tb));
        }
    }

    @Test(timeout = 4000)
    public void testDefensiveInCellAndInRowScopeGuards() {
        HtmlTreeBuilder tb = createBuilder("http://example.com/");
        tb.transition(HtmlTreeBuilderState.InCell);
        // End tag for <td> when none is in scope returns false
        Token.EndTag tdEnd = new Token.EndTag();
        tdEnd.name("td");
        assertFalse(HtmlTreeBuilderState.InCell.process(tdEnd, tb));

        tb.transition(HtmlTreeBuilderState.InRow);
        // End tag for <tr> when none is in scope returns false
        Token.EndTag trEnd = new Token.EndTag();
        trEnd.name("tr");
        assertFalse(HtmlTreeBuilderState.InRow.process(trEnd, tb));
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumValuesAndValueOfIntegrity() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertEquals("There should be exactly 23 states in HtmlTreeBuilderState", 23, states.length);

        for (HtmlTreeBuilderState state : states) {
            assertNotNull(state);
            assertEquals(state, HtmlTreeBuilderState.valueOf(state.name()));
            assertTrue(state.ordinal() >= 0);
        }

        // Verify specific known states
        assertSame(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
        assertSame(HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.valueOf("InBody"));
        assertSame(HtmlTreeBuilderState.InTable, HtmlTreeBuilderState.valueOf("InTable"));
        assertSame(HtmlTreeBuilderState.ForeignContent, HtmlTreeBuilderState.valueOf("ForeignContent"));
    }
}