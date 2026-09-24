/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.jsoup.parser.HtmlTreeBuilderState (and associated state machine transitions)
 *
 * Decision / Condition Matrix Targeted:
 * 1. Initial State:
 *    - Whitespace token (ignore, return true)
 *    - Comment token (insert comment)
 *    - Doctype token (quirks / force-quirks, DocumentType generation with system/public IDs, transition to BeforeHtml)
 *    - Fallback: anything else transitions to BeforeHtml and re-processes
 * 2. BeforeHtml & BeforeHead States:
 *    - Doctype in BeforeHtml/BeforeHead (triggers tb.error, returns false)
 *    - Comment, whitespace tokens
 *    - html StartTag vs non-html StartTag
 *    - End tags ("head", "body", "html", "br" vs invalid end tags)
 * 3. InHead & InHeadNoscript States:
 *    - Whitespace, comment, Doctype error
 *    - Start tags: html, base (with href updating base URI), meta, title (RcData), noframes/style (Rawtext),
 *      noscript, script (ScriptData & Text mode), head (error)
 *    - End tags: head, body/html/br fallback, unexpected end tags
 * 4. AfterHead & InBody States:
 *    - Start tags: body (framesetOk false), frameset (transition), head (error), formatters, headings (p closing),
 *      pre/listing, form (nested form error), dd/dt, plaintext, button, nobr (nested nobr check), table, input,
 *      hr, textarea, xmp, iframe, noembed, select (InSelect vs InSelectInTable), isindex early-90s element handling,
 *      math, svg, dropping tags.
 *    - Adoption Agency Algorithm (8 loops, active formatting elements, furthestBlock, foster parenting).
 *    - End tags: body, html, p (with/without button scope), li, form, dd/dt, headings, br (fake start tag).
 * 5. Table Subsystem States:
 *    - InTable: character pending / foster parenting, caption, colgroup, col, tbody/tfoot/thead, td/th/tr, nested table, hidden vs text input.
 *    - InTableText: null characters, whitespace characters vs non-whitespace characters foster-inserted.
 *    - InCaption, InColumnGroup, InTableBody, InRow, InCell: transitions, table scoping, cell closing, missing <tr> generation.
 * 6. InSelect & InSelectInTable States:
 *    - Options, optgroups, select nesting, inputs/keygens, table tags inside select causing select end tag.
 * 7. Frameset States:
 *    - InFrameset, AfterFrameset, AfterAfterFrameset transitions and character/tag handling.
 * 8. AfterBody & AfterAfterBody States:
 *    - Whitespace, comment, doctype, html tag, EOF.
 * 9. Known Defect Ground Truth:
 *    - DocumentType round trip with SYSTEM ID (SYSTEM identifier missing in legacy doctype rendering when public ID is blank).
 */

package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierRoundTrip() {
        // Direct trigger for the defect where <!DOCTYPE html SYSTEM "exampledtdfile.dtd">
        // lost the SYSTEM keyword when publicId is empty.
        String html = "<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\"><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        List<Node> nodes = doc.childNodes();
        assertTrue("Document must have at least one child node for doctype", nodes.size() > 0);
        assertTrue("First child should be DocumentType", nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);

        assertEquals("html", doctype.attr("name"));
        assertEquals("exampledtdfile.dtd", doctype.attr("systemId"));
        assertEquals("", doctype.attr("publicId"));

        String renderedDocType = doctype.outerHtml();
        assertTrue("Rendered DOCTYPE must contain SYSTEM keyword: " + renderedDocType,
                renderedDocType.contains("SYSTEM"));
        assertEquals("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">", renderedDocType.trim());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicAndSystemIdentifiers() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        List<Node> nodes = doc.childNodes();
        assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.attr("systemId"));
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">",
                doctype.outerHtml().trim());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateTransitions() {
        // Whitespace and comments before doctype
        String html = "   <!-- comment -->\n<!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("html", doc.select("html").first().tagName());
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testInitialStateWithoutDoctype() {
        // Missing DOCTYPE should trigger quirks mode and transition to BeforeHtml
        String html = "<div>Content without doctype</div>";
        Document doc = Jsoup.parse(html);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
        assertNotNull(doc.body());
        assertEquals("Content without doctype", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlTransitions() {
        // Comments and doctype handling in BeforeHtml
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<!DOCTYPE html><!-- comment in before html --><html><head></head><body></body></html>", "");
        assertEquals(0, parser.getErrors().size());
        assertEquals(1, doc.head().children().size() + doc.body().children().size());

        // Anything else in BeforeHtml creates html
        Document doc2 = Jsoup.parse("<head></head>");
        assertNotNull(doc2.select("html").first());
        assertNotNull(doc2.head());
    }

    @Test(timeout = 4000)
    public void testInHeadElements() {
        String html = "<!DOCTYPE html><html><head>" +
                "<base href=\"http://example.com/sub/\">" +
                "<meta charset=\"UTF-8\">" +
                "<link rel=\"stylesheet\" href=\"style.css\">" +
                "<title>Sample Title</title>" +
                "<style>body { color: red; }</style>" +
                "<script>var a = 1;</script>" +
                "<noscript><link rel=\"stylesheet\" href=\"noscript.css\"></noscript>" +
                "</head><body></body></html>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        assertEquals("Sample Title", doc.title());
        assertEquals("http://example.com/sub/", doc.baseUri());
        assertEquals(1, doc.head().getElementsByTag("base").size());
        assertEquals(1, doc.head().getElementsByTag("meta").size());
        assertEquals(1, doc.head().getElementsByTag("style").size());
        assertEquals(1, doc.head().getElementsByTag("script").size());
    }

    @Test(timeout = 4000)
    public void testAfterHeadTransitions() {
        // Body tag with attributes
        String html = "<!DOCTYPE html><html><head></head><body id=\"main\" class=\"test\"><p>Text</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("main", doc.body().id());
        assertEquals("test", doc.body().className());

        // Implicit body created by text or paragraph
        String implicit = "<!DOCTYPE html><html><head></head>Text without body</html>";
        Document docImp = Jsoup.parse(implicit);
        assertNotNull(docImp.body());
        assertTrue(docImp.body().text().contains("Text without body"));
    }

    @Test(timeout = 4000)
    public void testInBodyFormattingAndAdoptionAgency() {
        // Adoption Agency Algorithm trigger: misnested inline formatting across blocks
        String html = "<p><b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        // Expect <p><b>1</b></p><p><b>2</b>3</p>
        Elements pTags = doc.select("p");
        assertEquals(2, pTags.size());
        assertEquals("1", pTags.get(0).select("b").text());
        assertEquals("2", pTags.get(1).select("b").text());
    }

    @Test(timeout = 4000)
    public void testInBodyActiveFormattingAElement() {
        // Nested <a> tag should close previous <a> tag
        String html = "<a>First <a>Second</a></a>";
        Document doc = Jsoup.parse(html);
        Elements aTags = doc.select("a");
        assertEquals(2, aTags.size());
        assertEquals("First ", aTags.get(0).text());
        assertEquals("Second", aTags.get(1).text());
    }

    @Test(timeout = 4000)
    public void testInBodyListsAndHeadings() {
        // Nested li items without closing tag, and headings closing p tags
        String html = "<p>Intro" +
                "<h1>Header</h1>" +
                "<ul>" +
                "<li>Item 1" +
                "<li>Item 2" +
                "</ul>";
        Document doc = Jsoup.parse(html);
        assertEquals("Intro", doc.select("p").first().text());
        assertEquals("Header", doc.select("h1").first().text());
        assertEquals(2, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void testInBodyFormsAndButtons() {
        // Nested forms are forbidden; buttons inside buttons close earlier button
        String html = "<form id=\"f1\"><p>Form1</p><form id=\"f2\"><input type=\"text\"></form></form>" +
                "<button id=\"b1\">Btn1<button id=\"b2\">Btn2</button></button>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        assertEquals(1, doc.select("form").size());
        assertEquals("f1", doc.select("form").first().id());
        assertEquals(2, doc.select("button").size());
    }

    @Test(timeout = 4000)
    public void testInBodyMediaAndInputs() {
        String html = "<input type=\"hidden\" value=\"v1\"><input type=\"text\" value=\"v2\"><hr><br>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("input").size());
        assertEquals(1, doc.select("hr").size());
        assertEquals(1, doc.select("br").size());
    }

    @Test(timeout = 4000)
    public void testInBodyRawTextTags() {
        String html = "<textarea>Sample <tag> inside</textarea>" +
                "<xmp><tag>xmp content</tag></xmp>" +
                "<iframe><p>iframe content</p></iframe>" +
                "<noembed><p>noembed content</p></noembed>";
        Document doc = Jsoup.parse(html);
        assertEquals("Sample <tag> inside", doc.select("textarea").first().val());
        assertEquals("<tag>xmp content</tag>", doc.select("xmp").first().text());
        assertEquals("<p>iframe content</p>", doc.select("iframe").first().text());
        assertEquals("<p>noembed content</p>", doc.select("noembed").first().text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Edge Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableHandlingAndFosterParenting() {
        // Text inside table outside td/th should be foster parented
        String html = "<table>" +
                "FosterText" +
                "<tr>" +
                "<td>Cell 1</td>" +
                "</tr>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        // Foster-parented text is moved before table
        assertEquals("FosterText Cell 1", doc.body().text());
        assertTrue(doc.body().childNode(0).outerHtml().contains("FosterText"));
    }

    @Test(timeout = 4000)
    public void testTableWithCaptionColgroupAndHeadings() {
        String html = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col class=\"col1\"></colgroup>" +
                "<thead><tr><th>Header 1</th></tr></thead>" +
                "<tbody><tr><td>Data 1</td></tr></tbody>" +
                "<tfoot><tr><td>Foot 1</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(1, doc.select("colgroup col").size());
        assertEquals(1, doc.select("thead th").size());
        assertEquals(1, doc.select("tbody td").size());
        assertEquals(1, doc.select("tfoot td").size());
    }

    @Test(timeout = 4000)
    public void testInTableHiddenInputAllowed() {
        String html = "<table><input type=\"hidden\" name=\"csrf\" value=\"token\"><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Element input = doc.select("input").first();
        assertNotNull(input);
        assertEquals("csrf", input.attr("name"));
        // hidden input is placed inside table
        assertEquals("table", input.parent().tagName());
    }

    @Test(timeout = 4000)
    public void testSelectInsideAndOutsideTable() {
        // InSelect
        String html = "<select><optgroup label=\"group\"><option value=\"1\">1</option></optgroup><option value=\"2\">2</option></select>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("option").size());
        assertEquals(1, doc.select("optgroup").size());

        // InSelectInTable: table tag breaks out of select
        String tableSelect = "<table><tr><td><select><option>Opt1<tr><td>Cell2</td></tr></select></td></tr></table>";
        Document docTable = Jsoup.parse(tableSelect);
        assertNotNull(docTable.select("select").first());
        assertEquals(2, docTable.select("td").size());
    }

    @Test(timeout = 4000)
    public void testFramesetStateTransitions() {
        String html = "<html><head><title>Frameset</title></head>" +
                "<frameset rows=\"50%,50%\">" +
                "<frame src=\"frame1.html\">" +
                "<frame src=\"frame2.html\">" +
                "<noframes><p>No frames support</p></noframes>" +
                "</frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals(0, doc.select("body").size());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyAndComments() {
        String html = "<!DOCTYPE html><html><head></head><body>Content</body></html><!-- trailing comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.body().text());
        assertTrue(doc.outerHtml().contains("<!-- trailing comment -->"));
    }

    // =========================================================================
    // Partition D: Exception, Error Paths & Defensive Guards
    // =========================================================================

    @Test(timeout = 4000)
    public void testHtmlTreeBuilderErrorTracking() {
        // Various invalid HTML tokens causing errors in states
        Parser parser = Parser.htmlParser().setTrackErrors(50);
        String invalidHtml = "<!DOCTYPE html><html><head><head></head><body>" +
                "<p>Broken paragraph</b>" +
                "</body></head></html>";
        parser.parseInput(invalidHtml, "");
        assertTrue("Parser should have logged syntax errors", parser.getErrors().size() > 0);
    }

    @Test(timeout = 4000)
    public void testPrematureEOFInVariousStates() {
        // InHead unclosed script/style, InTable unclosed
        Document doc1 = Jsoup.parse("<script>var x = 1;");
        assertNotNull(doc1.body());

        Document doc2 = Jsoup.parse("<table><tr><td>Cell");
        assertNotNull(doc2.select("table").first());
        assertEquals("Cell", doc2.select("td").text());

        Document doc3 = Jsoup.parse("<select><option>Option");
        assertEquals("Option", doc3.select("option").text());
    }

    @Test(timeout = 4000)
    public void testIsIndexHistoricalElement() {
        // Early 90s isindex element handling in InBody
        String html = "<isindex prompt=\"Search here:\" action=\"/search\">";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");
        assertEquals(1, doc.select("form").size());
        assertEquals("/search", doc.select("form").attr("action"));
        assertEquals(1, doc.select("input[name=isindex]").size());
    }

    @Test(timeout = 4000)
    public void testDirectEnumProcessMethods() {
        // Verify state enum transitions using tokens directly with HtmlTreeBuilder
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div>test</div>", "http://example.com", new ParseErrorList(10, 10), ParseSettings.htmlDefault);
        
        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        // Process whitespace in Initial
        Token.Character ws = new Token.Character().data("   ");
        boolean resWs = HtmlTreeBuilderState.Initial.process(ws, tb);
        assertTrue(resWs);

        // Process comment in Initial
        Token.Comment comment = new Token.Comment();
        comment.getData().append("test comment");
        boolean resComment = HtmlTreeBuilderState.Initial.process(comment, tb);
        assertTrue(resComment);

        // Foreign content fallback
        Token.Character any = new Token.Character().data("content");
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(any, tb));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumCompletenessAndValues() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertNotNull(states);
        assertTrue(states.length >= 23);

        // Verify key enum values existence and valueOf
        assertEquals(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
        assertEquals(HtmlTreeBuilderState.BeforeHtml, HtmlTreeBuilderState.valueOf("BeforeHtml"));
        assertEquals(HtmlTreeBuilderState.BeforeHead, HtmlTreeBuilderState.valueOf("BeforeHead"));
        assertEquals(HtmlTreeBuilderState.InHead, HtmlTreeBuilderState.valueOf("InHead"));
        assertEquals(HtmlTreeBuilderState.InHeadNoscript, HtmlTreeBuilderState.valueOf("InHeadNoscript"));
        assertEquals(HtmlTreeBuilderState.AfterHead, HtmlTreeBuilderState.valueOf("AfterHead"));
        assertEquals(HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.valueOf("InBody"));
        assertEquals(HtmlTreeBuilderState.Text, HtmlTreeBuilderState.valueOf("Text"));
        assertEquals(HtmlTreeBuilderState.InTable, HtmlTreeBuilderState.valueOf("InTable"));
        assertEquals(HtmlTreeBuilderState.InTableText, HtmlTreeBuilderState.valueOf("InTableText"));
        assertEquals(HtmlTreeBuilderState.InCaption, HtmlTreeBuilderState.valueOf("InCaption"));
        assertEquals(HtmlTreeBuilderState.InColumnGroup, HtmlTreeBuilderState.valueOf("InColumnGroup"));
        assertEquals(HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.valueOf("InTableBody"));
        assertEquals(HtmlTreeBuilderState.InRow, HtmlTreeBuilderState.valueOf("InRow"));
        assertEquals(HtmlTreeBuilderState.InCell, HtmlTreeBuilderState.valueOf("InCell"));
        assertEquals(HtmlTreeBuilderState.InSelect, HtmlTreeBuilderState.valueOf("InSelect"));
        assertEquals(HtmlTreeBuilderState.InSelectInTable, HtmlTreeBuilderState.valueOf("InSelectInTable"));
        assertEquals(HtmlTreeBuilderState.AfterBody, HtmlTreeBuilderState.valueOf("AfterBody"));
        assertEquals(HtmlTreeBuilderState.InFrameset, HtmlTreeBuilderState.valueOf("InFrameset"));
        assertEquals(HtmlTreeBuilderState.AfterFrameset, HtmlTreeBuilderState.valueOf("AfterFrameset"));
        assertEquals(HtmlTreeBuilderState.AfterAfterBody, HtmlTreeBuilderState.valueOf("AfterAfterBody"));
        assertEquals(HtmlTreeBuilderState.AfterAfterFrameset, HtmlTreeBuilderState.valueOf("AfterAfterFrameset"));
        assertEquals(HtmlTreeBuilderState.ForeignContent, HtmlTreeBuilderState.valueOf("ForeignContent"));
    }
}