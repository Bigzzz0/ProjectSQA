package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.TreeBuilderState
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J: ParserTest::handles0CharacterAsText):
 *    - Defect: nullString initialized as String.valueOf(0x0000) which evaluates to "0" instead of "\u0000".
 *    - Failure: Any character token containing "0" in states InBody, InSelect, and InTableText matches
 *      nullString and is wrongfully discarded/errors instead of being treated as plain text.
 *    - Targeted Tests:
 *      * testHandles0CharacterAsText: Verifies "0" parsed in standard body content is preserved.
 *      * testHandles0CharacterInTable: Verifies "0" in table cell is preserved.
 *      * testHandles0CharacterInSelect: Verifies "0" in select option is preserved.
 *      * testHandles0CharacterInTableFosterParenting: Verifies pending characters "0" in table text mode.
 *
 * 2. CORE FUNCTIONAL LOGIC & EQUIVALENCE PARTITIONS:
 *    - Partition Initial: Comments, Doctypes (standard, quirks), Whitespace, Root html tags, AnythingElse.
 *    - Partition BeforeHtml & BeforeHead: Doctypes, Start tags, End tags, Token reprocessing, Implicit head.
 *    - Partition InHead & InHeadNoscript: Scripts, Styles, Meta, Base, Title, Noscript transitions, End tags.
 *    - Partition AfterHead: Body insertion, Frameset detection, Head tag reprocessing.
 *    - Partition InBody:
 *      * Formatting elements: Adoption Agency Algorithm (furthestBlock null/non-null, table foster parenting,
 *        replacement on stack and active formatting).
 *      * Special tags: Form scopes, lists (li, dt, dd), heading tags (h1-h6 nesting), button scope, nobr.
 *      * Self-closing / Void / Special: hr, image (remap to img), isindex (macro transformation), plaintext,
 *        textarea, xmp, iframe, noembed, ruby (rp, rt), math, svg.
 *    - Partition InTable, InTableText, InCaption, InColumnGroup, InTableBody, InRow, InCell:
 *      * Nested table elements, stray text foster-parenting, column groups, cell closing, missing tr insertion.
 *    - Partition InSelect & InSelectInTable: Option/optgroup nesting, select scope closure, table interception.
 *    - Partition AfterBody, InFrameset, AfterFrameset, AfterAfterBody, AfterAfterFrameset:
 *      * Document lifecycle completion, trailing comments, whitespace, fragment parse boundaries.
 *    - Partition ForeignContent: Terminal state fallback.
 */
public class TreeBuilderStateGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J: handles0CharacterAsText)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandles0CharacterAsText() {
        // Direct trigger for the '0' character being misidentified as nullString (0x0000 evaluated to "0")
        Document doc = Parser.parse("0", "");
        assertEquals("0", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testHandles0CharacterInTable() {
        Document doc = Parser.parse("<table><tr><td>0</td></tr></table>", "");
        Element td = doc.select("td").first();
        assertNotNull(td);
        assertEquals("0", td.text());
    }

    @Test(timeout = 4000)
    public void testHandles0CharacterInSelect() {
        Document doc = Parser.parse("<select><option>0</option></select>", "");
        Element option = doc.select("option").first();
        assertNotNull(option);
        assertEquals("0", option.text());
    }

    @Test(timeout = 4000)
    public void testHandles0CharacterInTableFosterParenting() {
        // Stray '0' in table text triggers InTableText character accumulation
        Document doc = Parser.parse("<table>0<tr><td>data</td></tr></table>", "");
        assertTrue("Output should retain '0' character via foster parenting", doc.text().contains("0"));
    }

    @Test(timeout = 4000)
    public void testHandles0CharacterSurroundedByText() {
        Document doc = Parser.parse("<p>Page 0 of 10</p>", "");
        assertEquals("Page 0 of 10", doc.select("p").first().text());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndDoctypeTransitions() {
        // Initial -> BeforeHtml -> BeforeHead -> InHead
        String html = "   <!-- Comment before doctype -->\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n" +
                "<html><head><title>Title</title></head><body>Hello</body></html>";
        Document doc = Parser.parse(html, "http://example.com/");
        assertEquals("Title", doc.title());
        assertEquals("Hello", doc.body().text());
        assertEquals("html", doc.childNode(1).nodeName());
    }

    @Test(timeout = 4000)
    public void testInitialStateDoctypeQuirks() {
        String html = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><body>Quirks</body></html>";
        Document doc = Parser.parse(html, "");
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlAnythingElseAndStrayTags() {
        // Trigger BeforeHtml anythingElse with stray tags and text
        String html = "<div>Direct content without html or head tags</div>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Direct content without html or head tags", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInHeadElementsHandling() {
        String html = "<html><head>" +
                "<base href='http://example.com/base/'>" +
                "<link rel='stylesheet' href='styles.css'>" +
                "<meta name='description' content='Test'>" +
                "<style>body { color: red; }</style>" +
                "<noframes>No frames content</noframes>" +
                "<noscript><p>No script</p></noscript>" +
                "<script>var a = 1;</script>" +
                "</head><body>Text</body></html>";
        Document doc = Parser.parse(html, "http://example.com/");
        assertEquals("http://example.com/base/", doc.baseUri());
        assertEquals(1, doc.head().getElementsByTag("link").size());
        assertEquals(1, doc.head().getElementsByTag("meta").size());
        assertEquals(1, doc.head().getElementsByTag("style").size());
        assertEquals(1, doc.head().getElementsByTag("script").size());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptState() {
        String html = "<head><noscript><!-- comment --><link rel='stylesheet' href='a.css'><meta charset='utf-8'></noscript></head>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.head().select("link").first());
        assertNotNull(doc.head().select("meta").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadAndBodyAttributesMerge() {
        String html = "<html><head></head><body class='b1' id='main'><body class='b2' title='merged'>Content</body></html>";
        Document doc = Parser.parse(html, "");
        assertEquals("b1", doc.body().attr("class"));
        assertEquals("merged", doc.body().attr("title"));
    }

    @Test(timeout = 4000)
    public void testAdoptionAgencyAlgorithmNestedFormatting() {
        // Triggers AAA with formatting elements across block tags
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Parser.parse(html, "");
        Elements bTags = doc.select("b");
        assertEquals(2, bTags.size());
        assertEquals("1", bTags.get(0).text());
        assertEquals("2", bTags.get(1).text());
    }

    @Test(timeout = 4000)
    public void testAdoptionAgencyAlgorithmAnchorTags() {
        String html = "<a>1<p>2</p>3</a>";
        Document doc = Parser.parse(html, "");
        Elements aTags = doc.select("a");
        assertEquals(2, aTags.size());
    }

    @Test(timeout = 4000)
    public void testAdoptionAgencyAlgorithmInTableFosterParenting() {
        String html = "<table><b><tr><td>Cell</b></td></tr></table>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("table").first());
        assertNotNull(doc.select("b").first());
    }

    @Test(timeout = 4000)
    public void testSpecialBlockScopingHeadingsAndLists() {
        String html = "<h1>Heading 1 <h2>Heading 2</h2></h1>" +
                "<ul><li>Item 1<li>Item 2</li></ul>" +
                "<dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document doc = Parser.parse(html, "");
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals(2, doc.select("li").size());
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
    }

    @Test(timeout = 4000)
    public void testButtonScopeAndNestedButtons() {
        String html = "<button>Button 1 <button>Button 2</button></button>";
        Document doc = Parser.parse(html, "");
        assertEquals(2, doc.select("button").size());
    }

    @Test(timeout = 4000)
    public void testNobrScopeHandling() {
        String html = "<nobr>First <nobr>Second</nobr> Third</nobr>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("nobr").first());
    }

    @Test(timeout = 4000)
    public void testAppletMarqueeObjectFormattingMarkers() {
        String html = "<applet code='test'>Content</applet><marquee>Scroll</marquee><object data='obj'>Fall</object>";
        Document doc = Parser.parse(html, "");
        assertEquals(1, doc.select("applet").size());
        assertEquals(1, doc.select("marquee").size());
        assertEquals(1, doc.select("object").size());
    }

    @Test(timeout = 4000)
    public void testEmptyAndVoidTagsInBody() {
        String html = "<div><area><br><embed><img src='foo.png'><keygen><wbr><hr><input type='text'><input type='hidden'></div>";
        Document doc = Parser.parse(html, "");
        assertEquals(1, doc.select("area").size());
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("embed").size());
        assertEquals(1, doc.select("img").size());
        assertEquals(1, doc.select("keygen").size());
        assertEquals(1, doc.select("wbr").size());
        assertEquals(1, doc.select("hr").size());
        assertEquals(2, doc.select("input").size());
    }

    @Test(timeout = 4000)
    public void testImageAliasToImg() {
        String html = "<image src='test.jpg'>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("img").first());
        assertEquals("test.jpg", doc.select("img").first().attr("src"));
    }

    @Test(timeout = 4000)
    public void testIsindexTagExpansion() {
        String html = "<isindex action='search.cgi' prompt='Enter keywords: '>";
        Document doc = Parser.parse(html, "");
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("search.cgi", form.attr("action"));
        Element input = doc.select("input[name=isindex]").first();
        assertNotNull(input);
        assertTrue(doc.text().contains("Enter keywords: "));
    }

    @Test(timeout = 4000)
    public void testTextareaXmpIframeNoembedRawTextTags() {
        String html = "<textarea>Line 1\n<tag>Not parsed</tag></textarea>" +
                "<xmp><b>Raw content</b></xmp>" +
                "<iframe><span>Frame content</span></iframe>" +
                "<noembed><i>No embed fallback</i></noembed>";
        Document doc = Parser.parse(html, "");
        assertEquals("Line 1\n<tag>Not parsed</tag>", doc.select("textarea").val());
        assertEquals("<b>Raw content</b>", doc.select("xmp").text());
        assertEquals("<span>Frame content</span>", doc.select("iframe").text());
    }

    @Test(timeout = 4000)
    public void testPlaintextTransition() {
        String html = "<div><plaintext>This is <b>not</b> parsed <p>tags are ignored";
        Document doc = Parser.parse(html, "");
        Element pt = doc.select("plaintext").first();
        assertNotNull(pt);
        assertTrue(pt.text().contains("This is <b>not</b> parsed"));
    }

    @Test(timeout = 4000)
    public void testRubyAndAnnotations() {
        String html = "<ruby>Base<rp>(</rp><rt>Annotation</rt><rp>)</rp></ruby>";
        Document doc = Parser.parse(html, "");
        assertEquals("Base(Annotation)", doc.select("ruby").text());
    }

    @Test(timeout = 4000)
    public void testMathAndSvgForeignNamespaces() {
        String html = "<math><mi>x</mi></math><svg><circle cx='50' cy='50' r='40'/></svg>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("math").first());
        assertNotNull(doc.select("svg").first());
    }

    @Test(timeout = 4000)
    public void testFormElementHandlingAndNestingError() {
        String html = "<form id='f1'><input name='a'><form id='f2'><input name='b'></form></form>";
        Document doc = Parser.parse(html, "");
        assertEquals(1, doc.select("form").size());
        assertEquals("f1", doc.select("form").first().id());
        assertEquals(2, doc.select("input").size());
    }

    // =========================================================================
    // Partition B: Table, Caption, ColumnGroup, Row, Cell, and Select States
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompleteTableStructureTransitions() {
        String html = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col width='20'><col width='80'></colgroup>" +
                "<thead><tr><th>H1</th><th>H2</th></tr></thead>" +
                "<tbody><tr><td>C1</td><td>C2</td></tr></tbody>" +
                "<tfoot><tr><td>F1</td><td>F2</td></tr></tfoot>" +
                "</table>";
        Document doc = Parser.parse(html, "");
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(2, doc.select("col").size());
        assertEquals(1, doc.select("thead").size());
        assertEquals(1, doc.select("tbody").size());
        assertEquals(1, doc.select("tfoot").size());
        assertEquals(3, doc.select("tr").size());
        assertEquals(2, doc.select("th").size());
        assertEquals(4, doc.select("td").size());
    }

    @Test(timeout = 4000)
    public void testStrayTableContentFosterParenting() {
        String html = "<table>Stray Plaintext<div>Stray Div</div><tr><td>Cell</td></tr></table>";
        Document doc = Parser.parse(html, "");
        assertTrue(doc.text().contains("Stray Plaintext"));
        assertNotNull(doc.select("div").first());
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testTableMissingTrAndTbodyImplicitCreation() {
        String html = "<table><td>Cell without row or body</td></table>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("tbody").first());
        assertNotNull(doc.select("tr").first());
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testTableNestedFormsAndInputs() {
        String html = "<table><form action='x'><input type='hidden' name='h' value='v'><tr><td>Cell</td></tr></form></table>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("form").first());
        assertNotNull(doc.select("input[type=hidden]").first());
    }

    @Test(timeout = 4000)
    public void testSelectOptionsAndOptgroups() {
        String html = "<select>" +
                "<optgroup label='Group 1'>" +
                "<option value='1'>One</option>" +
                "<option value='2'>Two" +
                "</optgroup>" +
                "<optgroup label='Group 2'>" +
                "<option value='3'>Three</option>" +
                "</optgroup>" +
                "</select>";
        Document doc = Parser.parse(html, "");
        assertEquals(2, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());
    }

    @Test(timeout = 4000)
    public void testInSelectInTableState() {
        String html = "<table><tr><td><select><option>Opt 1<tr><td>Stray cell in select</td></tr></select></td></tr></table>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("select").first());
        assertEquals(2, doc.select("td").size());
    }

    // =========================================================================
    // Partition D: Frameset, Trailing Content, and Fragment Parsing
    // =========================================================================

    @Test(timeout = 4000)
    public void testFramesetFullLifecycle() {
        String html = "<html><head><title>Frame</title></head>" +
                "<frameset rows='50%,50%'>" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames</p></noframes>" +
                "</frameset></html>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.select("frameset").first());
        assertEquals(2, doc.select("frame").size());
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testTrailingCommentsAndContentAfterBody() {
        String html = "<html><body>Main</body></html><!-- trailing comment --><div>After After Body Content</div>";
        Document doc = Parser.parse(html, "");
        assertTrue(doc.text().contains("Main"));
        assertTrue(doc.text().contains("After After Body Content"));
    }

    @Test(timeout = 4000)
    public void testFragmentParsingTdOrphan() {
        Document doc = Parser.parseBodyFragment("<td>Orphan TD</td>", "");
        assertNotNull(doc.body().select("td").first());
        assertEquals("Orphan TD", doc.body().select("td").text());
    }

    @Test(timeout = 4000)
    public void testFragmentParsingOptions() {
        Document doc = Parser.parseBodyFragment("<option>Item 1</option><option>Item 2</option>", "");
        assertEquals(2, doc.body().select("option").size());
    }

    @Test(timeout = 4000)
    public void testFragmentParsingDiv() {
        Document doc = Parser.parseBodyFragment("<div><p>Fragment text</p></div>", "");
        assertEquals(1, doc.body().select("div").size());
        assertEquals("Fragment text", doc.body().select("p").text());
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Contract Integrity & Boundary Values
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumCoverageAndForeignContent() {
        // Guarantee all enum constants are loaded and accessible
        for (TreeBuilderState state : TreeBuilderState.values()) {
            assertNotNull(state);
            assertEquals(state, TreeBuilderState.valueOf(state.name()));
        }

        // ForeignContent always returns true
        assertTrue(TreeBuilderState.ForeignContent.process(null, null));
    }

    @Test(timeout = 4000)
    public void testSarcasmEndTagHandling() {
        String html = "<p>Literal sarcasm end tag</sarcasm></p>";
        Document doc = Parser.parse(html, "");
        assertEquals("Literal sarcasm end tag", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testStrayBrEndTagCreatesElement() {
        String html = "<div>Line 1</br>Line 2</div>";
        Document doc = Parser.parse(html, "");
        assertEquals(1, doc.select("br").size());
        assertTrue(doc.text().contains("Line 1 Line 2") || doc.text().contains("Line 1Line 2"));
    }

    @Test(timeout = 4000)
    public void testUnexpectedEndTagsIgnoredGracefully() {
        String html = "</head></body></html></p></div></span></td></tr>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testEmptyInputParsing() {
        Document doc = Parser.parse("", "");
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyParsing() {
        Document doc = Parser.parse("   \t\n\r   ", "");
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("", doc.body().text());
    }
}