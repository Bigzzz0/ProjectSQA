/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.parser.HtmlTreeBuilderState
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J: HtmlParserTest::caseSensitiveParseTree):
 *    - In `InBody.anyOtherEndTag(Token, HtmlTreeBuilder)`:
 *      The method compares `node.nodeName().equals(name)`, where `name = t.asEndTag().normalName()` (lowercased).
 *      When HTML parsing is configured with `ParseSettings.preserveCase`, `node.nodeName()` retains case (e.g., "X"),
 *      while `normalName()` is lower-case ("x"). Consequently, `node.nodeName().equals(name)` evaluates to false,
 *      preventing proper closure of case-preserved custom tags and incorrectly nesting subsequent siblings.
 *      Test: `testCaseSensitiveParseTreeDefect()` reproduces this condition.
 *
 * 2. EQUIVALENCE PARTITIONING & BRANCH COVERAGE:
 *    - Partition A: Initial & BeforeHtml Transitions
 *      * Initial: Whitespace (ignored), Comment (inserted), Doctype (parsed, quirks checked, transitions to BeforeHtml),
 *                 anything else (transitions to BeforeHtml and reprocessed).
 *      * BeforeHtml: Doctype error, Comment, Whitespace, StartTag "html" (transitions to BeforeHead),
 *                    EndTag ("head", "body", "html", "br") fallback, other EndTag errors, anythingElse fallback.
 *    - Partition B: BeforeHead, InHead, InHeadNoscript, AfterHead Transitions
 *      * BeforeHead: Whitespace, Comment, Doctype error, StartTag "html", StartTag "head", EndTag triggers.
 *      * InHead: Whitespace char, Comment, Doctype error, StartTag ("html", "base", "meta", "title", "style",
 *                "noscript", "script", "head", anythingElse), EndTag ("head", "body", "html", "br", anythingElse).
 *      * InHeadNoscript: Doctype error, StartTag "html", EndTag "noscript", whitespace/comment/head tags, anythingElse.
 *      * AfterHead: Whitespace char, Comment, Doctype error, StartTag ("html", "body", "frameset", head elements,
 *                   "head" error, anythingElse), EndTag ("body", "html", anythingElse).
 *    - Partition C: InBody State Processing
 *      * Character tokens: null character error, whitespace framesetOk handling, non-whitespace text.
 *      * Start tags: "a" (formatting adoption / already in active formatting), empty formatters, p-closers,
 *                    "li", "html", "body", "frameset", headings (nested heading error), "pre", "form",
 *                    "button", "nobr", "table", "input", "hr", "image" -> "img", "textarea", "select",
 *                    "svg", "math", unknown tags.
 *      * End tags: Adoption agency algorithm (8-step loop, formatting elements on/off stack, table foster checks),
 *                  closers, "li", "body", "html", "form", "p", headings, "br", anyOtherEndTag stack scan.
 *      * EOF token handling.
 *    - Partition D: Table Modes
 *      * InTable: Character handling (transitions to InTableText), Comment, Doctype error, StartTag ("caption",
 *                 "colgroup", "col", "tbody"/"tfoot"/"thead", "td"/"th"/"tr", "table", "input", "form"), EndTags.
 *      * InTableText: Character collection, null char error, flush pending characters to InBody foster parents.
 *      * InCaption, InColumnGroup, InTableBody, InRow, InCell: transitions and cell closures.
 *    - Partition E: InSelect, InSelectInTable, InFrameset, AfterBody, AfterFrameset, AfterAfterBody, AfterAfterFrameset
 *      * Specific switch cases, state resets, frameset handling, and end-of-document cleanup.
 */

package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the bug reported in HtmlParserTest::caseSensitiveParseTree:
     * When parse settings preserve case, closing tags like </X> should correctly match
     * <X> even when case is preserved. If `anyOtherEndTag` checks `node.nodeName().equals(name)`
     * against normalized lower-case name, the tag <X> will not be closed and <y> becomes a child.
     */
    @Test(timeout = 4000)
    public void testCaseSensitiveParseTreeDefect() {
        Parser parser = Parser.htmlParser();
        parser.settings(ParseSettings.preserveCase);
        String html = "<r><X>A</X><y>B</y></r>";
        Document doc = parser.parseInput(html, "");

        Element r = doc.select("r").first();
        assertNotNull("Element <r> should exist", r);

        Element x = r.select("X").first();
        assertNotNull("Element <X> should exist", x);

        Element y = r.select("y").first();
        assertNotNull("Element <y> should exist", y);

        // Under the bug, <y> was incorrectly placed inside <X> instead of being its sibling
        assertEquals("<y> should be a child of <r>, not <X>", r, y.parent());
        assertEquals("<X> should have text 'A'", "A", x.ownText().trim());
        assertEquals("<y> should have text 'B'", "B", y.ownText().trim());
    }

    // =========================================================================
    // PARTITION A: CORE INITIAL & BEFORE-HTML & BEFORE-HEAD LOGIC
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateVariants() {
        // DocType with quirks
        String quirksHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><body></body></html>";
        Document doc = Jsoup.parse(quirksHtml);
        assertNotNull(doc.documentType());

        // Leading comment and whitespace before Doctype
        String leadingComment = "   <!-- comment before doc -->\n<!DOCTYPE html><html><body>Test</body></html>";
        Document doc2 = Jsoup.parse(leadingComment);
        assertEquals("Test", doc2.body().text());

        // Reprocess token directly into BeforeHtml when no doctype is present
        String noDoctype = "<html><head></head><body>No Doctype</body></html>";
        Document doc3 = Jsoup.parse(noDoctype);
        assertEquals("No Doctype", doc3.body().text());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlTransitions() {
        // Unexpected doctype in BeforeHtml triggers error
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        Document doc = parser.parseInput("<html><!DOCTYPE html></html>", "");
        assertTrue(parser.getErrors().size() > 0);

        // Comment before html
        Document docComment = Jsoup.parse("<!-- c1 --><html><!-- c2 --><body></body></html>");
        assertNotNull(docComment.child(0));

        // EndTag triggering anythingElse in BeforeHtml
        Document docEndTag = Jsoup.parse("</head><html><body>Text</body></html>");
        assertEquals("Text", docEndTag.body().text());

        // Stray end tag in BeforeHtml
        Parser parserStray = Parser.htmlParser().setTrackErrors(5);
        parserStray.parseInput("</div><html><body></body></html>", "");
        assertTrue(parserStray.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testBeforeHeadTransitions() {
        // StartTag "head"
        Document docHead = Jsoup.parse("<html><head><title>Sample</title></head></html>");
        assertEquals("Sample", docHead.title());

        // StartTag "html" processed inside BeforeHead (delegates to InBody)
        Document docDoubleHtml = Jsoup.parse("<html><html lang='en'><head></head><body></body></html>");
        assertEquals("en", docDoubleHtml.select("html").attr("lang"));

        // Premature end tags in BeforeHead
        Document docEnd = Jsoup.parse("<html></head><head><title>T</title></head><body></body></html>");
        assertEquals("T", docEnd.title());

        // Unexpected Doctype in BeforeHead
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<html><!DOCTYPE html><head></head><body></body></html>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    // =========================================================================
    // PARTITION B: IN-HEAD, IN-HEAD-NOSCRIPT, AFTER-HEAD
    // =========================================================================

    @Test(timeout = 4000)
    public void testInHeadElements() {
        String headContent = "<html><head>"
                + "<base href='http://example.com/'>"
                + "<basefont face='sans'>"
                + "<bgsound src='sound.mp3'>"
                + "<command label='cmd'>"
                + "<link rel='stylesheet' href='style.css'>"
                + "<meta charset='UTF-8'>"
                + "<title>Head Test</title>"
                + "<style>body { color: red; }</style>"
                + "<noframes>No Frames Content</noframes>"
                + "<noscript><p>No Script</p></noscript>"
                + "<script>var x = 1;</script>"
                + "</head><body>Content</body></html>";

        Document doc = Jsoup.parse(headContent);
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("Head Test", doc.title());
        assertEquals(1, doc.head().getElementsByTag("style").size());
        assertEquals(1, doc.head().getElementsByTag("script").size());

        // Duplicate <head> tag generates error
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<html><head><head></head><body></body></html>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptState() {
        String noscriptHtml = "<html><head><noscript>"
                + "<!-- comment in noscript -->"
                + "<link rel='stylesheet' href='a.css'>"
                + "<meta name='keywords' content='test'>"
                + "<style>p { color: blue; }</style>"
                + "</noscript></head><body></body></html>";
        Document doc = Jsoup.parse(noscriptHtml);
        assertNotNull(doc.select("head noscript link"));

        // Error path: nested noscript or doctype inside noscript
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<html><head><noscript><!DOCTYPE html><noscript></noscript></noscript></head></html>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testAfterHeadTransitions() {
        // In head elements encountered in AfterHead
        String htmlAfterHead = "<html><head><title>T</title></head><meta name='after' content='val'><body>Hello</body></html>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        Document doc = parser.parseInput(htmlAfterHead, "");
        assertEquals("val", doc.head().select("meta[name=after]").attr("content"));
        assertTrue(parser.getErrors().size() > 0); // triggers error in AfterHead

        // Body tag encountered
        Document docBody = Jsoup.parse("<html><head></head><body class='main'>Content</body></html>");
        assertEquals("main", docBody.body().className());

        // Frameset tag in AfterHead
        String framesetHtml = "<html><head></head><frameset cols='25%,*'><frame src='frame_1.html'></frameset></html>";
        Document docFrame = Jsoup.parse(framesetHtml);
        assertNotNull(docFrame.select("frameset").first());

        // Head tag re-opened in AfterHead -> error
        parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<html><head></head><head><body></body></html>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    // =========================================================================
    // PARTITION C: IN-BODY ELEMENT & ADOPTION AGENCY ALGORITHM
    // =========================================================================

    @Test(timeout = 4000)
    public void testInBodyActiveFormattingAndAdoptionAgency() {
        // Tag 'a' reconstructed & adoption agency
        String nestedA = "<a>1<a>2</a>3</a>";
        Document docA = Jsoup.parse(nestedA);
        assertEquals(2, docA.select("a").size());

        // Deep adoption agency algorithm with formatting elements and blocks
        String adoption = "<b>1<p>2</b>3</p>";
        Document docAdoption = Jsoup.parse(adoption);
        assertEquals("<b>1</b><p><b>2</b>3</p>", docAdoption.body().html());

        // Formatting element closed when not on stack
        String unclosedFormatting = "<span></b></span>";
        Parser p = Parser.htmlParser().setTrackErrors(5);
        p.parseInput(unclosedFormatting, "");
        assertTrue(p.getErrors().size() > 0);

        // Multiple formatters
        String multiFormat = "<b><i><u>text</b></i></u>";
        Document docMulti = Jsoup.parse(multiFormat);
        assertEquals("text", docMulti.select("u").text());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTags() {
        // p-closers, headings, pre, li, dl, dd, dt
        String html = "<div>"
                + "<p>Paragraph 1"
                + "<h1>Heading 1"
                + "<h2>Heading 2</h2>"
                + "<pre>Preformatted</pre>"
                + "<ul><li>Item 1<li>Item 2</ul>"
                + "<dl><dt>Term<dd>Definition</dl>"
                + "</div>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("li").size());
        assertEquals("Term", doc.select("dt").text());
        assertEquals("Definition", doc.select("dd").text());

        // Button in button scope
        String buttons = "<button>Btn 1<button>Btn 2</button></button>";
        Document docButtons = Jsoup.parse(buttons);
        assertEquals(2, docButtons.select("button").size());

        // Nobr tag handling
        String nobr = "<nobr>Nobr 1<nobr>Nobr 2</nobr></nobr>";
        Document docNobr = Jsoup.parse(nobr);
        assertEquals(2, docNobr.select("nobr").size());

        // Form tag inside another form (nested form error)
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        Document docForm = parser.parseInput("<form><form></form></form>", "");
        assertTrue(parser.getErrors().size() > 0);
        assertEquals(1, docForm.select("form").size());

        // Applet / object / marquee formatting marker
        String applet = "<applet><b>Applet Text</b></applet>";
        Document docApplet = Jsoup.parse(applet);
        assertEquals("Applet Text", docApplet.select("applet b").text());

        // Image tag outside svg mapped to img
        Document docImg = Jsoup.parse("<image src='test.jpg'>");
        assertEquals("img", docImg.body().child(0).tagName());

        // Svg retaining image
        Document docSvg = Jsoup.parse("<svg><image></image></svg>");
        assertEquals("image", docSvg.select("svg image").first().tagName());

        // Textarea & xmp & iframe
        Document docTextarea = Jsoup.parse("<textarea><tag>test</textarea><xmp>raw <b>html</b></xmp><iframe><frame></iframe>");
        assertEquals("<tag>test", docTextarea.select("textarea").val());
        assertEquals("raw <b>html</b>", docTextarea.select("xmp").text());

        // MathML
        Document docMath = Jsoup.parse("<math><mrow></mrow></math>");
        assertEquals(1, docMath.select("math").size());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTags() {
        // Stray end tags: br, p without open p, body, html
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("</br></p></body></html><div>Valid</div>", "");
        assertTrue(parser.getErrors().size() > 0);
        assertEquals("Valid", doc.select("div").text());

        // Form element end tag
        Document docFormEnd = Jsoup.parse("<form id=1><div><input></form></div>");
        assertNotNull(docFormEnd.select("form").first());

        // Sarcasm tag (should hit anyOtherEndTag)
        Document docSarcasm = Jsoup.parse("<sarcasm>Truth</sarcasm>");
        assertEquals("Truth", docSarcasm.select("sarcasm").text());

        // InBodyStartDrop tags (e.g. <caption>, <tr> in body context are dropped with error)
        parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<body><tr><td>Cell</td></tr></body>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testInBodyNullCharactersAndWhitespace() {
        // Null character token error
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<body>Hello\u0000World</body>", "");
        assertTrue(parser.getErrors().size() > 0);

        // Whitespace handling before and after framesetOk modified
        Document doc = Jsoup.parse("<body>   <span>Text</span>   </body>");
        assertEquals("Text", doc.body().text());
    }

    // =========================================================================
    // PARTITION D: TABLES & FOSTER PARENTING & TABLE TEXT
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableProcessingModes() {
        // Standard table structure
        String tableHtml = "<table>"
                + "<caption>Table Caption</caption>"
                + "<colgroup><col span='1'></colgroup>"
                + "<thead><tr><th>Header</th></tr></thead>"
                + "<tbody><tr><td>Data</td></tr></tbody>"
                + "<tfoot><tr><td>Footer</td></tr></tfoot>"
                + "</table>";
        Document doc = Jsoup.parse(tableHtml);
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(1, doc.select("col").size());
        assertEquals("Header", doc.select("th").text());
        assertEquals("Data", doc.select("tbody td").text());
        assertEquals("Footer", doc.select("tfoot td").text());

        // Foster parenting: non-table tags and characters inside table
        String fosterHtml = "<table>A<b>B</b><tr><td>C</td></tr></table>";
        Document docFoster = Jsoup.parse(fosterHtml);
        // Foster-parented text "AB" moves before table
        assertTrue(docFoster.body().html().startsWith("A<b>B</b><table>"));

        // Table with hidden input vs visible input
        String inputInTable = "<table><input type='hidden' name='h' value='1'><input type='text' name='t'><tr><td>Cell</td></tr></table>";
        Document docInput = Jsoup.parse(inputInTable);
        assertEquals("1", docInput.select("table > input[type=hidden]").val());
        // Visible input is foster-parented out of table
        assertFalse(docInput.select("table > input[type=text]").size() > 0);

        // Table in caption / nested table
        String nestedTable = "<table><caption><table><tr><td>Inside</td></tr></table></caption><tr><td>Outside</td></tr></table>";
        Document docNested = Jsoup.parse(nestedTable);
        assertEquals(2, docNested.select("table").size());

        // Premature table closing
        String unclosedTable = "<table><tr><td>Cell";
        Document docUnclosed = Jsoup.parse(unclosedTable);
        assertEquals("Cell", docUnclosed.select("td").text());
    }

    @Test(timeout = 4000)
    public void testTableColumnGroupAndCellAnomalies() {
        // Colgroup with unexpected elements triggers colgroup close
        String colgroupHtml = "<table><colgroup><col><div>Invalid</div></colgroup></table>";
        Document doc = Jsoup.parse(colgroupHtml);
        assertNotNull(doc.select("colgroup").first());

        // Misplaced end tags in table/row/cell
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<table><tr></th></td></tr></table>", "");
        assertTrue(parser.getErrors().size() > 0);

        // Table body without tr directly followed by td/th generates implicit tr
        Document docAutoRow = Jsoup.parse("<table><tbody><td>Direct Cell</td></tbody></table>");
        assertEquals("Direct Cell", docAutoRow.select("tbody tr td").text());
    }

    // =========================================================================
    // PARTITION E: SELECT, FRAMESET & AFTER MODES
    // =========================================================================

    @Test(timeout = 4000)
    public void testInSelectAndInSelectInTable() {
        // Normal select with optgroup and options
        String selectHtml = "<select name='s'>"
                + "<optgroup label='g1'><option value='1'>One<option value='2'>Two</optgroup>"
                + "<optgroup label='g2'><option value='3'>Three</optgroup>"
                + "</select>";
        Document doc = Jsoup.parse(selectHtml);
        assertEquals(3, doc.select("option").size());
        assertEquals(2, doc.select("optgroup").size());

        // Select inside table (InSelectInTable mode)
        String selectInTable = "<table><tr><td>"
                + "<select><option>Option 1</option><td>Adjacent Cell</td></select>"
                + "</td></tr></table>";
        Document docSelectTable = Jsoup.parse(selectInTable);
        assertEquals(2, docSelectTable.select("td").size());

        // Stray input inside select forces select closure
        String selectWithInput = "<select><option>1</option><input type='text'></select>";
        Document docSelectInput = Jsoup.parse(selectWithInput);
        assertEquals(1, docSelectInput.select("input").size());

        // Script inside select
        String selectScript = "<select><script>var a = 0;</script><option>1</option></select>";
        Document docSelectScript = Jsoup.parse(selectScript);
        assertEquals(1, docSelectScript.select("select script").size());
    }

    @Test(timeout = 4000)
    public void testFramesetStates() {
        // InFrameset, AfterFrameset, AfterAfterFrameset
        String framesetDoc = "<!DOCTYPE html>"
                + "<html>"
                + "<frameset rows='50%,50%'>"
                + "<!-- frameset comment -->"
                + "<frame src='top.html'>"
                + "<frame src='bottom.html'>"
                + "<noframes><p>No Frames</p></noframes>"
                + "</frameset>"
                + "<!-- comment after frameset -->"
                + "</html>"
                + "<!-- comment after after frameset -->";

        Document doc = Jsoup.parse(framesetDoc);
        assertEquals(2, doc.select("frame").size());
        assertNotNull(doc.select("frameset").first());

        // Error path: stray tags inside frameset
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput("<html><frameset><p>Illegal In Frameset</p></frameset></html>", "");
        assertTrue(parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testAfterBodyAndAfterAfterBody() {
        // Content after body tag
        String afterBodyHtml = "<html><body>Main</body><!-- c1 --></html><!-- c2 -->";
        Document doc = Jsoup.parse(afterBodyHtml);
        assertEquals("Main", doc.body().text());

        // Stray tags after body re-open body
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        Document docReprocess = parser.parseInput("<html><body>Main</body><div>After</div></html>", "");
        assertTrue(parser.getErrors().size() > 0);
        assertTrue(docReprocess.body().text().contains("After"));
    }

    @Test(timeout = 4000)
    public void testForeignContentAndEnumIntegrity() {
        // Verify all HtmlTreeBuilderState enums are present and non-null
        for (HtmlTreeBuilderState state : HtmlTreeBuilderState.values()) {
            assertNotNull(state);
            assertNotNull(state.name());
        }

        // Test ForeignContent state process stub returns true
        Token.Comment dummyComment = new Token.Comment();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(dummyComment, tb));
    }
}