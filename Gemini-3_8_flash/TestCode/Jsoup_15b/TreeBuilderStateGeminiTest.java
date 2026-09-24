/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.parser.TreeBuilderState
 * Coverage Objectives:
 * - Line & Branch Coverage across all 23 states:
 *   Initial, BeforeHtml, BeforeHead, InHead, InHeadNoscript, AfterHead, InBody, Text, InTable, InTableText,
 *   InCaption, InColumnGroup, InTableBody, InRow, InCell, InSelect, InSelectInTable, AfterBody, InFrameset,
 *   AfterFrameset, AfterAfterBody, AfterAfterFrameset, ForeignContent.
 *
 * Specific Branch & Condition Targets:
 * - Initial: Whitespace handling, comments, valid doctype, force-quirks doctype, fallback to BeforeHtml.
 * - BeforeHtml: Doctype error, comments, whitespace, <html> start tag, foster/reprocess end tags ("head","body","html","br"),
 *   other end tags (error), default anythingElse fallback.
 * - BeforeHead: Whitespace, comments, doctype error, <html> tag, <head> tag insertion, implicit head generation.
 * - InHead: Whitespace, comments, doctype error, meta/base/link/title/style/noframes/noscript/script start tags,
 *   base tag href baseUri setting, end tag <head>, unexpected end tags.
 * - InHeadNoscript: Doctype error, <html> start tag, </noscript> end tag, whitespace, comments, head/noscript error, anythingElse.
 * - AfterHead: Whitespace, comments, doctype, <body> start tag (framesetOk=false), <frameset>, head elements pushed to head,
 *   anythingElse generating implicit body.
 * - InBody:
 *   * Character tokens: null character (\u0000) handling, whitespace vs non-whitespace (framesetOk reset).
 *   * StartTag: <html> attribute merging, <base>/<style>/etc. delegation to InHead, <body> attribute merging & duplicate checks,
 *     <frameset> handling, block tags closing <p>, headers (<h1>..<h6>) auto-closing current header, <pre>/<listing>,
 *     <form> duplicates, <li>/<dd>/<dt> scoping, <plaintext>, <button> in button scope, <a> active formatting element handling,
 *     formatting elements ("b","i", etc.), <nobr>, <applet>/<object>, <table> in quirks vs non-quirks, void tags ("img","br", etc.),
 *     <input> hidden vs non-hidden, <isindex> expansion, <textarea> Rcdata transition, <xmp>/<iframe>/<noembed> Rawtext transitions,
 *     <select> in table vs outside, <optgroup>/<option>, <rp>/<rt> with ruby scope, <math>/<svg>, misplaced table tags.
 *   * EndTag: </body>, </html>, block tags implied end tags & closing, <form> scope & removal, <p>, <li>, <dd>/<dt>,
 *     <h1>..<h6>, <sarcasm>, Adoption Agency Algorithm (furthestBlock reparenting, foster parenting, formatting replacement),
 *     <applet>/<marquee>/<object>, <br> error handling, anyOtherEndTag stack iteration & special element checks.
 * - Text: Character insertion, EOF handling, </script> / end tag transitions to originalState.
 * - InTable: Character pending buffer, comments, doctype, <caption>, <colgroup>, <col>, <tbody>/<tfoot>/<thead>,
 *   <td>/<th>/<tr> implicit <tbody>, <table> nesting, <style>/<script> in table, <input> hidden vs foster-parented,
 *   <form> in table, end tags </table>, foster parenting into InBody.
 * - InTableText: Buffer flushing on non-character token, whitespace vs non-whitespace foster inserts.
 * - InCaption: End tag </caption>, table structure start tags triggering implicit caption close, invalid end tags.
 * - InColumnGroup: Whitespace, comments, doctype, <col>, </colgroup>, anythingElse.
 * - InTableBody: <tr> start tag, <th>/<td> implicit <tr>, table/caption/col exit conditions, end tags </tbody> etc.
 * - InRow: <th>/<td> cell transition, missing <tr> triggers, </tr> end tag, table body transitions.
 * - InCell: End tags </td>/</th>, table structure tags triggering closeCell, start tags triggering closeCell.
 * - InSelect: Null character, start tags <option>/<optgroup>/<select>/<input>/<script>, end tags </optgroup>/</option>/</select>.
 * - InSelectInTable: Table tags start/end breaking out of select and re-processing.
 * - AfterBody: Whitespace, comments, doctype, </html> transition to AfterAfterBody, anythingElse.
 * - InFrameset / AfterFrameset / AfterAfterBody / AfterAfterFrameset: Frameset lifecycle, <frame>, <noframes>, EOF.
 * - ForeignContent: Fallback state verification.
 *
 * Defects4J Ground Truth Target:
 * - org.jsoup.parser.ParserTest::handlesDataOnlyTags (expected:<Hello []There> but was:<Hello ['); i++; ]There>)
 *   Defect: InBody state failed to treat <script> as a data tag / delegate to InHead, leading to script contents
 *   being treated as standard character text tokens in body rather than script data nodes.
 * ---------------------------------------------------------------------------------------------------------------------
 */
package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TreeBuilderStateGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardDocumentLifecycle() {
        String html = "<!DOCTYPE html><html lang='en'><head><title>Test Title</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Test Title", doc.title());
        assertEquals("Hello World", doc.select("p").first().text());
        assertEquals("en", doc.select("html").first().attr("lang"));
    }

    @Test(timeout = 4000)
    public void testInitialStateTransitions() {
        // Leading whitespace, comment before doctype, and standard doctype
        String html = "   \n<!-- comment before doctype -->\n<!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().attr("name"));
    }

    @Test(timeout = 4000)
    public void testInitialForceQuirksDoctype() {
        // Quirks mode doctype triggers Document.QuirksMode.quirks
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testInitialWithoutDoctype() {
        // Document without doctype transitions Initial -> BeforeHtml -> BeforeHead -> InHead
        String html = "<html><head><meta charset='utf-8'></head><body>Text</body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNull(doc.documentType());
        assertEquals("Text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlTransitions() {
        // Comment, whitespace, and start tag in BeforeHtml
        String html = "<!DOCTYPE html>\n<!-- comment before html -->\n<html id='root'><head></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("root", doc.select("html").first().id());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlUnexpectedTokens() {
        // Stray doctype after html start and unexpected end tags handled by anythingElse
        String html = "<!DOCTYPE html></head><!DOCTYPE html><body>Content</body>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadTransitions() {
        // Comments, whitespace before head, implicit head tag insertion
        String html = "<!DOCTYPE html><html><!-- comment before head --><body>No Head Explicit</body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc.head());
        assertEquals("No Head Explicit", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInHeadElements() {
        String html = "<!DOCTYPE html><html><head>" +
                "<base href='http://base.example.com/dir/'>" +
                "<link rel='stylesheet' href='style.css'>" +
                "<meta name='description' content='test'>" +
                "<style>body { color: blue; }</style>" +
                "<title>Page Title</title>" +
                "<noscript><link rel='stylesheet' href='fallback.css'></noscript>" +
                "</head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("http://base.example.com/dir/", doc.baseUri());
        assertEquals("Page Title", doc.title());
        assertEquals(1, doc.select("style").size());
        assertEquals(1, doc.select("meta").size());
    }

    @Test(timeout = 4000)
    public void testAfterHeadTransitions() {
        // Head closed, then comment, whitespace, and explicit body
        String html = "<html><head></head>\n<!-- after head comment -->\n<body class='main'><p>Content</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("main", doc.body().className());
    }

    @Test(timeout = 4000)
    public void testAfterHeadImplicitBodyDueToTag() {
        // Head closed, start tag other than body triggers implicit body creation
        String html = "<html><head></head><div>Div Starts Body</div></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(1, doc.select("body > div").size());
        assertEquals("Div Starts Body", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableStructure() {
        String html = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col width='10'></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Data</td></tr></tbody>" +
                "<tfoot><tr><td>Footer</td></tr></tfoot>" +
                "</table>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals("Header", doc.select("th").text());
        assertEquals("Data", doc.select("tbody td").text());
        assertEquals("Footer", doc.select("tfoot td").text());
    }

    @Test(timeout = 4000)
    public void testInSelectState() {
        String html = "<select id='s1'>" +
                "<optgroup label='group1'>" +
                "<option value='1'>One</option>" +
                "<option value='2'>Two</option>" +
                "</optgroup>" +
                "</select>";
        Document doc = Parser.parse(html, "http://example.com");

        Element select = doc.getElementById("s1");
        assertNotNull(select);
        assertEquals(2, select.select("option").size());
        assertEquals("One", select.select("option").first().text());
    }

    @Test(timeout = 4000)
    public void testInFramesetAndAfterFrameset() {
        String html = "<html><head><title>Frameset</title></head>" +
                "<frameset cols='50%,50%'>" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames support</p></noframes>" +
                "</frameset></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("frameset").size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullCharacterHandlingInBody() {
        // 0x0000 in InBody should be treated as parse error and ignored or sanitized
        String html = "<body>Hello\u0000World</body>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        // Either null char is dropped or handled gracefully without throwing unhandled exceptions
        assertTrue(doc.body().text().contains("Hello"));
    }

    @Test(timeout = 4000)
    public void testNullCharacterHandlingInTableText() {
        String html = "<table>Hello\u0000World<tr><td>Cell</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Cell", doc.select("td").text());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedFormattingElementsAdoptionAgency() {
        // Test Adoption Agency Algorithm boundary (exceeding 8 iterations or deeply nested formatting tags)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("<b>");
        }
        sb.append("<p>Nested Paragraph</p>");
        for (int i = 0; i < 15; i++) {
            sb.append("</b>");
        }
        Document doc = Parser.parse(sb.toString(), "http://example.com");

        assertNotNull(doc);
        assertEquals("Nested Paragraph", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testUnclosedFormattingAndBlockInterleaving() {
        // Misnested formatting tags: <b><i><p>Text</b></i></p>
        String html = "<b>Bold <i>Italic <p>Para</b> Normal</i> Rest</p>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Bold Italic", doc.body().childNodes().get(0).outerHtml().trim());
    }

    @Test(timeout = 4000)
    public void testEmptyDocumentAndWhitespaceOnly() {
        Document doc1 = Parser.parse("", "http://example.com");
        assertNotNull(doc1);
        assertEquals(0, doc1.body().children().size());

        Document doc2 = Parser.parse("   \n\t\r\f   ", "http://example.com");
        assertNotNull(doc2);
        assertEquals(0, doc2.body().children().size());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyAndComments() {
        String html = "<html><body>Main</body></html><!-- trailing comment 1 --><!-- trailing comment 2 -->   ";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Main", doc.body().text());
        assertTrue(doc.outerHtml().contains("trailing comment 1"));
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetComments() {
        String html = "<html><frameset><frame src='a.html'></frameset></html><!-- trailing comment -->";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(1, doc.select("frameset").size());
        assertTrue(doc.outerHtml().contains("trailing comment"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // Defects4J: ParserTest::handlesDataOnlyTags
    // Failure: expected:<Hello []There> but was:<Hello ['); i++; ]There>
    // Root cause: <script> inside <body> was not treated as data/text mode, leaking its script body into doc.text().
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlesDataOnlyTagsDefectInBody() {
        // Directly reproduces the failure condition in ParserTest::handlesDataOnlyTags
        String html = "<p>Hello <script>'); i++; </script>There</p>";
        Document doc = Parser.parse(html, "http://example.com");

        // The text of <script> MUST NOT appear in doc.text()
        assertEquals("Hello There", doc.text());
    }

    @Test(timeout = 4000)
    public void testHandlesDataOnlyTagsWithoutWrapper() {
        String html = "Hello <script>'); i++; </script>There";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Hello There", doc.text());
    }

    @Test(timeout = 4000)
    public void testDataOnlyTagsStyleInBody() {
        String html = "<p>Hello <style>p { font-weight: bold; }</style>There</p>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Hello There", doc.text());
        assertEquals(1, doc.select("style").size());
    }

    @Test(timeout = 4000)
    public void testScriptTagsInTable() {
        String html = "<table><tr><td>Hello</td><script>var x = 1;</script><td>There</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Hello There", doc.text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableFosterParentingForLooseText() {
        // Plain text inside <table> but outside <td> must be foster-parented before the table
        String html = "<table>Foster Text<tr><td>Cell</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");

        Element body = doc.body();
        assertTrue(body.text().startsWith("Foster Text"));
        assertEquals("Cell", doc.select("td").text());
    }

    @Test(timeout = 4000)
    public void testTableFosterParentingForElements() {
        String html = "<table><p>Foster Paragraph</p><tr><td>Cell</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");

        Element p = doc.select("body > p").first();
        assertNotNull("Paragraph should be foster parented outside table", p);
        assertEquals("Foster Paragraph", p.text());
    }

    @Test(timeout = 4000)
    public void testImplicitRowAndCellCreation() {
        // <td> directly in <table> without explicit <tbody> or <tr>
        String html = "<table><td>Direct Cell</td></table>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(1, doc.select("table > tbody > tr > td").size());
        assertEquals("Direct Cell", doc.select("td").text());
    }

    @Test(timeout = 4000)
    public void testNestedTables() {
        String html = "<table><tr><td>Outer<table><tr><td>Inner</td></tr></table></td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(2, doc.select("table").size());
        assertEquals("Outer Inner", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testSelectInTableTransitions() {
        // InSelectInTable state: unexpected table tags inside select close the select
        String html = "<table><tr><td><select><option>Opt 1</option></td><td>Next Cell</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(2, doc.select("td").size());
        assertEquals(1, doc.select("select").size());
        assertEquals("Next Cell", doc.select("td").get(1).text());
    }

    @Test(timeout = 4000)
    public void testMultipleBodyTagsMerged() {
        // Secondary body tag should merge attributes onto existing body
        String html = "<body class='first' data-a='1'><body class='ignored' data-b='2'><p>Text</p></body>";
        Document doc = Parser.parse(html, "http://example.com");

        Element body = doc.body();
        assertEquals("first", body.className());
        assertEquals("2", body.attr("data-b"));
    }

    @Test(timeout = 4000)
    public void testMultipleHtmlTagsMerged() {
        String html = "<html lang='en'><html data-version='2'><head></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        Element htmlEl = doc.select("html").first();
        assertEquals("en", htmlEl.attr("lang"));
        assertEquals("2", htmlEl.attr("data-version"));
    }

    @Test(timeout = 4000)
    public void testDuplicateFormElementsIgnored() {
        // Nested form tags are illegal; inner form is ignored
        String html = "<form id='f1'><input name='a'><form id='f2'><input name='b'></form></form>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(1, doc.select("form").size());
        assertEquals("f1", doc.select("form").first().id());
        assertEquals(2, doc.select("form input").size());
    }

    @Test(timeout = 4000)
    public void testIsIndexExpansion() {
        String html = "<form><isindex action='/search' prompt='Search Here:'></form>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testButtonScopeHandling() {
        // StartTag button inside another button closes the first button
        String html = "<button id='b1'>Btn 1 <button id='b2'>Btn 2</button></button>";
        Document doc = Parser.parse(html, "http://example.com");

        Elements buttons = doc.select("button");
        assertEquals(2, buttons.size());
        assertEquals("Btn 1", buttons.get(0).text());
        assertEquals("Btn 2", buttons.get(1).text());
    }

    @Test(timeout = 4000)
    public void testHeadersAutoClosePreviousHeader() {
        String html = "<h1>Heading 1 <h2>Heading 2</h2></h1>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
        assertEquals("Heading 1", doc.select("h1").text());
        assertEquals("Heading 2", doc.select("h2").text());
    }

    @Test(timeout = 4000)
    public void testListScoping() {
        String html = "<ul><li>Item 1<li>Item 2<ol><li>Sub 1<li>Sub 2</ol><li>Item 3</ul>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(5, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void testDefinitionListScoping() {
        String html = "<dl><dt>Term 1<dd>Def 1<dt>Term 2<dd>Def 2</dl>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());
    }

    @Test(timeout = 4000)
    public void testRawTextAndRcdataElements() {
        String html = "<div>" +
                "<textarea><p>Not A Tag</p></textarea>" +
                "<xmp><b>Not Bold</b></xmp>" +
                "<iframe><i>Not Italic</i></iframe>" +
                "<noembed>No Embed Content</noembed>" +
                "<plaintext>Plaintext content <p>never ends</p>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("<p>Not A Tag</p>", doc.select("textarea").first().text());
        assertEquals("<b>Not Bold</b>", doc.select("xmp").first().text());
    }

    @Test(timeout = 4000)
    public void testRubyElementsScoping() {
        String html = "<ruby>漢 <rp>(</rp><rt>Kan</rt><rp>)</rp> 字 <rp>(</rp><rt>ji</rt><rp>)</rp></ruby>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(2, doc.select("rt").size());
        assertEquals(4, doc.select("rp").size());
    }

    @Test(timeout = 4000)
    public void testForeignContentMathAndSvg() {
        String html = "<div><svg width='100'><circle cx='50' cy='50' r='40'/></svg><math><mi>x</mi></math></div>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals(1, doc.select("svg").size());
        assertEquals(1, doc.select("circle").size());
        assertEquals(1, doc.select("math").size());
    }

    @Test(timeout = 4000)
    public void testSarcasmAndUnknownEndTags() {
        String html = "<p>Some text</sarcasm></unknown> More text</p>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Some text More text", doc.select("p").text());
    }

    @Test(timeout = 4000)
    public void testStrayTableComponentsOutsideTable() {
        // Table tags directly in body are errors and ignored
        String html = "<body><caption>Cap</caption><col><colgroup><col></colgroup><thead><tr><th>H</th></tr></thead><tbody><tr><td>D</td></tr></tbody><tfoot></tfoot></table><p>Valid</p></body>";
        Document doc = Parser.parse(html, "http://example.com");

        assertEquals("Valid", doc.select("p").text());
        assertEquals(0, doc.select("table").size());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumCompletenessAndValues() {
        TreeBuilderState[] states = TreeBuilderState.values();
        assertNotNull(states);
        assertEquals(23, states.length);

        for (TreeBuilderState state : states) {
            assertNotNull(state);
            assertEquals(state, TreeBuilderState.valueOf(state.name()));
        }
    }

    @Test(timeout = 4000)
    public void testForeignContentDirectProcess() {
        // Direct test on ForeignContent state's process method
        boolean processed = TreeBuilderState.ForeignContent.process(null, null);
        assertTrue("ForeignContent should return true as default placeholder", processed);
    }

    @Test(timeout = 4000)
    public void testBodyFragmentParsing() {
        List<org.jsoup.nodes.Node> nodes = Parser.parseBodyFragment("<div>Fragment 1</div><div>Fragment 2</div>", "http://example.com").body().childNodes();
        assertFalse(nodes.isEmpty());
        assertEquals(2, Parser.parseBodyFragment("<div>Fragment 1</div><div>Fragment 2</div>", "http://example.com").select("div").size());
    }
}