package org.jsoup.parser;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Enum: org.jsoup.parser.HtmlTreeBuilderState
 *
 * 1. DEFECT TARGET (Ground Truth: HtmlParserTest::convertsImageToImg):
 *    - State InBody: Handles <image> start tag by rewriting tag name to "img" and reprocessing.
 *    - In defective versions, <image> wasn't converted or caused incorrect element emission.
 *    - Dedicated test: testDefectConvertsImageToImg & testDefectConvertsImageWithAttributes.
 *
 * 2. CORE DECISION & BRANCH COVERAGE MATRIX:
 *    - Initial:
 *        * Whitespace (ignore)
 *        * Comment (insert comment)
 *        * Doctype (normal mode & force-quirks mode)
 *        * Fallback / Anything else (transition to BeforeHtml, re-process token)
 *    - BeforeHtml:
 *        * Doctype (error, return false)
 *        * Comment (insert comment)
 *        * Whitespace (ignore)
 *        * StartTag "html" (insert, transition to BeforeHead)
 *        * EndTag "head", "body", "html", "br" (anythingElse: insert "html", transition BeforeHead, re-process)
 *        * Invalid EndTag (error, return false)
 *        * Anything else (insert "html", transition BeforeHead, re-process)
 *    - BeforeHead:
 *        * Whitespace (ignore)
 *        * Comment (insert comment)
 *        * Doctype (error, return false)
 *        * StartTag "html" (delegate to InBody)
 *        * StartTag "head" (insert, setHeadElement, transition InHead)
 *        * EndTag "head", "body", "html", "br" (process start tag head, re-process)
 *        * Invalid EndTag (error, return false)
 *        * Anything else (fallback start tag head, re-process)
 *    - InHead:
 *        * Whitespace (insert character)
 *        * Comment (insert comment)
 *        * Doctype (error, return false)
 *        * StartTag "html" (delegate InBody)
 *        * StartTag "base", "basefont", "bgsound", "command", "link" (insertEmpty; update base URI if base+href)
 *        * StartTag "meta" (insertEmpty)
 *        * StartTag "title" (handleRcData)
 *        * StartTag "noframes", "style" (handleRawtext)
 *        * StartTag "noscript" (insert, transition InHeadNoscript)
 *        * StartTag "script" (script data tokeniser transition, markInsertionMode, transition Text, insert)
 *        * StartTag "head" (error, return false)
 *        * EndTag "head" (pop, transition AfterHead)
 *        * EndTag "body", "html", "br" (anythingElse)
 *        * Other EndTag (error, return false)
 *        * Default anythingElse (process end tag head, re-process)
 *    - InHeadNoscript:
 *        * Doctype (error)
 *        * StartTag "html" (process InBody)
 *        * EndTag "noscript" (pop, transition InHead)
 *        * Whitespace, Comment, head tags ("basefont", "link", "meta", etc.) -> delegate InHead
 *        * EndTag "br" (anythingElse)
 *        * StartTag "head", "noscript", or any other EndTag (error, return false)
 *        * Fallback anythingElse (error, end tag noscript, re-process)
 *    - AfterHead:
 *        * Whitespace, Comment, Doctype (error)
 *        * StartTag "html" (delegate InBody)
 *        * StartTag "body" (insert, framesetOk=false, transition InBody)
 *        * StartTag "frameset" (insert, transition InFrameset)
 *        * StartTag head tags ("base", "link", "meta", etc.) -> repush head, delegate InHead, remove head
 *        * StartTag "head" (error, return false)
 *        * EndTag "body", "html" (anythingElse)
 *        * Invalid EndTag (error, return false)
 *        * Fallback anythingElse (start tag body, framesetOk=true, re-process)
 *    - InBody:
 *        * Character: null character (error, return false), whitespace, non-whitespace (framesetOk=false)
 *        * Comment, Doctype (error)
 *        * StartTag: "html" (attribute merging), InBodyStartToHead, "body" (attribute merging, fragment check),
 *          "frameset" (framesetOk condition, fragment check), InBodyStartPClosers (p close if inButtonScope),
 *          Headings (p close, nested heading close), InBodyStartPreListing, "form" (nested form error),
 *          "li" (search stack for li breakers), "dd"/"dt", "plaintext", "button" (scope check and re-process),
 *          "a" (active formatting duplicate handle), Formatters, "nobr", Applets ("applet","marquee","object"),
 *          "table" (quirks mode vs standard mode p closer), Empty formatters, "input" (hidden vs non-hidden),
 *          Media ("param", "source", "track"), "hr", "image" (rewrites to "img"), "isindex", "textarea",
 *          "xmp", "iframe", "noembed", "select" (InSelect vs InSelectInTable), "optgroup"/"option",
 *          "rp"/"rt" (ruby scope check), "math", "svg", InBodyStartDrop (error), unknown tags.
 *        * EndTag: "body" (scope check, transition AfterBody), "html", InBodyEndClosers, "form",
 *          "p" (inButtonScope check, fake start tag p if not in scope), "li", "dd"/"dt", Headings,
 *          "sarcasm", Adoption Agency Algorithm (AAA), Applets, "br" (error, fake start tag br),
 *          anyOtherEndTag (matching node search, special tag barrier).
 *        * EOF
 *    - Text:
 *        * Character insert, EOF (pop, transition originalState, re-process), EndTag (pop, transition originalState)
 *    - InTable:
 *        * Character (pending table characters, transition InTableText)
 *        * Comment, Doctype
 *        * StartTag: "caption", "colgroup", "col", "tbody"/"tfoot"/"thead", "td"/"th"/"tr",
 *          "table" (nested table), "style"/"script", "input" (hidden vs non-hidden), "form", fallback (foster parenting)
 *        * EndTag: "table", table context tags (error), fallback
 *        * EOF
 *    - InTableText:
 *        * Null char (error), characters added to pending, flush pending on other tokens (foster insert if non-whitespace)
 *    - InCaption:
 *        * EndTag "caption", StartTag/EndTag closing caption, invalid EndTags, fallback InBody
 *    - InColumnGroup:
 *        * Whitespace, Comment, Doctype, StartTag "col", EndTag "colgroup", EOF, fallback
 *    - InTableBody:
 *        * StartTag "tr", "th"/"td", exitTableBody tags, fallback InTable
 *        * EndTag "tbody"/"tfoot"/"thead", "table", invalid EndTags, fallback InTable
 *    - InRow:
 *        * StartTag "th"/"td", handleMissingTr tags, EndTag "tr", "table", table bodies, fallback InTable
 *    - InCell:
 *        * EndTag "th"/"td", closeCell triggers, fallback InBody
 *    - InSelect & InSelectInTable:
 *        * Characters (null check), Comment, Doctype, StartTag "option"/"optgroup"/"select"/"input"/"textarea"/"script",
 *          EndTag "optgroup"/"option"/"select", InSelectInTable table tags handling
 *    - AfterBody, InFrameset, AfterFrameset, AfterAfterBody, AfterAfterFrameset, ForeignContent
 */
public class HtmlTreeBuilderStateGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectConvertsImageToImg() {
        String html = "<body><image /></body>";
        Document doc = Parser.parse(html, "http://example.com/");
        List<Element> imgs = doc.body().getElementsByTag("img");
        assertEquals("Expected exactly 1 <img> element converted from <image>", 1, imgs.size());
        assertEquals("Tag name should be 'img'", "img", imgs.get(0).tagName());
        assertEquals("Parent should be 'body'", "body", imgs.get(0).parent().tagName());
        assertEquals("<image> elements should not exist in DOM", 0, doc.body().getElementsByTag("image").size());
    }

    @Test(timeout = 4000)
    public void testDefectConvertsImageWithAttributes() {
        String html = "<image src='avatar.png' alt='user profile' id='profile-img' />";
        Document doc = Parser.parse(html, "http://example.com/");
        List<Element> imgs = doc.body().getElementsByTag("img");
        assertEquals(1, imgs.size());
        Element img = imgs.get(0);
        assertEquals("img", img.tagName());
        assertEquals("avatar.png", img.attr("src"));
        assertEquals("user profile", img.attr("alt"));
        assertEquals("profile-img", img.id());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumValuesAndValueOf() {
        for (HtmlTreeBuilderState state : HtmlTreeBuilderState.values()) {
            assertNotNull(state);
            assertSame(state, HtmlTreeBuilderState.valueOf(state.name()));
        }
    }

    @Test(timeout = 4000)
    public void testInitialStateVariants() {
        // Comments, whitespace, doctype
        String html = "  <!-- header comment -->  <!DOCTYPE html><html><head></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        assertNotNull(doc.body());

        // Quirks mode via force quirks / legacy doctype
        String quirksHtml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><body></body></html>";
        Document quirksDoc = Parser.parse(quirksHtml, "http://example.com/");
        assertNotNull(quirksDoc);

        // Immediate text without doctype or html tag
        String directText = "Direct Text Content";
        Document textDoc = Parser.parse(directText, "http://example.com/");
        assertTrue(textDoc.body().text().contains("Direct Text Content"));
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlTransitions() {
        // Comment before html
        Document doc = Parser.parse("<!-- pre-html --><html><body></body></html>", "");
        assertNotNull(doc.childNode(0));

        // Invalid doctype in BeforeHtml triggers error and returns false
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<html>", "");
        tb.transition(HtmlTreeBuilderState.BeforeHtml);
        Token.Doctype doctype = new Token.Doctype();
        boolean processed = HtmlTreeBuilderState.BeforeHtml.process(doctype, tb);
        assertFalse("Doctype in BeforeHtml should fail and log error", processed);

        // Invalid end tag in BeforeHtml
        boolean endTagProcessed = HtmlTreeBuilderState.BeforeHtml.process(new Token.EndTag("div"), tb);
        assertFalse("EndTag div in BeforeHtml should return false", endTagProcessed);

        // EndTag head in BeforeHtml triggers anythingElse
        boolean headEndProcessed = HtmlTreeBuilderState.BeforeHtml.process(new Token.EndTag("head"), tb);
        assertTrue("EndTag head in BeforeHtml should trigger anythingElse", headEndProcessed);
    }

    @Test(timeout = 4000)
    public void testBeforeHeadTransitions() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<html>", "");
        tb.transition(HtmlTreeBuilderState.BeforeHead);

        // Whitespace ignored
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(new Token.Character("   \n\t"), tb));

        // Doctype in BeforeHead should fail
        assertFalse(HtmlTreeBuilderState.BeforeHead.process(new Token.Doctype(), tb));

        // StartTag html delegates to InBody
        Token.StartTag htmlTag = new Token.StartTag("html");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(htmlTag, tb));

        // EndTag head triggers implicit head
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(new Token.EndTag("head"), tb));

        // Invalid EndTag triggers error
        tb.transition(HtmlTreeBuilderState.BeforeHead);
        assertFalse(HtmlTreeBuilderState.BeforeHead.process(new Token.EndTag("div"), tb));

        // Fallback start tag head on other tokens
        tb.transition(HtmlTreeBuilderState.BeforeHead);
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(new Token.StartTag("div"), tb));
    }

    @Test(timeout = 4000)
    public void testInHeadElements() {
        String html = "<head>" +
                "<base href='http://example.com/base/'>" +
                "<basefont>" +
                "<bgsound>" +
                "<link rel='stylesheet' href='test.css'>" +
                "<meta charset='UTF-8'>" +
                "<title>Page Title</title>" +
                "<style>body { color: black; }</style>" +
                "<noscript><link rel='stylesheet' href='noscript.css'></noscript>" +
                "<script>var a = 1;</script>" +
                "</head>";
        Document doc = Parser.parse(html, "http://example.com/");
        assertEquals("http://example.com/base/", doc.baseUri());
        assertEquals("Page Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("style").size());
        assertEquals(1, doc.head().getElementsByTag("script").size());
        assertEquals(1, doc.head().getElementsByTag("meta").size());

        // InHead error cases: duplicate <head> tag and doctype
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<head>", "");
        tb.transition(HtmlTreeBuilderState.InHead);
        assertFalse(HtmlTreeBuilderState.InHead.process(new Token.StartTag("head"), tb));
        assertFalse(HtmlTreeBuilderState.InHead.process(new Token.Doctype(), tb));
        assertFalse(HtmlTreeBuilderState.InHead.process(new Token.EndTag("span"), tb));

        // End tag body/html/br triggers anythingElse
        assertTrue(HtmlTreeBuilderState.InHead.process(new Token.EndTag("body"), tb));
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript() {
        String html = "<head><noscript><meta name='foo' content='bar'></noscript></head>";
        Document doc = Parser.parse(html, "http://example.com/");
        assertEquals(1, doc.head().getElementsByTag("noscript").size());

        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<head><noscript>", "");
        tb.transition(HtmlTreeBuilderState.InHeadNoscript);

        // Doctype logs error in InHeadNoscript
        tb.errorList.clear();
        HtmlTreeBuilderState.InHeadNoscript.process(new Token.Doctype(), tb);
        assertFalse(tb.errorList.isEmpty());

        // Start tag html delegates to InBody
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(new Token.StartTag("html"), tb));

        // Start tag head or noscript inside noscript is error
        tb.transition(HtmlTreeBuilderState.InHeadNoscript);
        assertFalse(HtmlTreeBuilderState.InHeadNoscript.process(new Token.StartTag("head"), tb));
        assertFalse(HtmlTreeBuilderState.InHeadNoscript.process(new Token.StartTag("noscript"), tb));

        // EndTag br triggers anythingElse
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(new Token.EndTag("br"), tb));

        // General end tag is error
        assertFalse(HtmlTreeBuilderState.InHeadNoscript.process(new Token.EndTag("div"), tb));
    }

    @Test(timeout = 4000)
    public void testAfterHeadTransitions() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<head></head>", "");
        tb.transition(HtmlTreeBuilderState.AfterHead);

        // Whitespace, comment, doctype
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.Character("   "), tb));
        tb.errorList.clear();
        HtmlTreeBuilderState.AfterHead.process(new Token.Doctype(), tb);
        assertFalse(tb.errorList.isEmpty());

        // StartTag html
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.StartTag("html"), tb));

        // StartTag body
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.StartTag("body"), tb));
        assertSame(HtmlTreeBuilderState.InBody, tb.state());

        // Reset to AfterHead, test frameset
        tb.transition(HtmlTreeBuilderState.AfterHead);
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.StartTag("frameset"), tb));
        assertSame(HtmlTreeBuilderState.InFrameset, tb.state());

        // Reset to AfterHead, test repushing head tags
        tb.transition(HtmlTreeBuilderState.AfterHead);
        tb.errorList.clear();
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.StartTag("meta"), tb));
        assertFalse(tb.errorList.isEmpty());

        // Duplicate <head> tag is error
        assertFalse(HtmlTreeBuilderState.AfterHead.process(new Token.StartTag("head"), tb));

        // Invalid EndTag
        assertFalse(HtmlTreeBuilderState.AfterHead.process(new Token.EndTag("div"), tb));

        // EndTag body/html triggers anythingElse
        assertTrue(HtmlTreeBuilderState.AfterHead.process(new Token.EndTag("body"), tb));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Comprehensive InBody Tags
    // =========================================================================

    @Test(timeout = 4000)
    public void testInBodyAllStartTagBranches() {
        String html = "<body>" +
                "<p>Para 1</p>" +
                "<h1>Header 1</h1>" +
                "<h2>Header 2</h2>" +
                "<pre>Code listing</pre>" +
                "<listing>Listing content</listing>" +
                "<form id='form1'><input type='text' name='q'><input type='hidden' name='h'></form>" +
                "<ul><li>Item 1<li>Item 2</ul>" +
                "<dl><dt>Term<dd>Definition</dl>" +
                "<button>Button 1</button>" +
                "<a href='url1'>Link 1</a>" +
                "<b>Bold</b><i>Italic</i><strike>Strike</strike><small>Small</small>" +
                "<nobr>No break</nobr>" +
                "<applet code='App.class'><param name='x' value='y'></applet>" +
                "<marquee>Marquee</marquee>" +
                "<object data='data.obj'></object>" +
                "<table id='t1'><tr><td>Cell 1</td></tr></table>" +
                "<area><br><wbr><embed src='file.swf'>" +
                "<source src='audio.mp3'><track src='sub.vtt'>" +
                "<hr>" +
                "<textarea>Text content</textarea>" +
                "<xmp><tag-inside-xmp></xmp>" +
                "<iframe></iframe>" +
                "<noembed>Fallback</noembed>" +
                "<select id='s1'><option>Opt 1</option><optgroup label='g'><option>Opt 2</option></optgroup></select>" +
                "<ruby>漢 <rp>(</rp><rt>kan</rt><rp>)</rp></ruby>" +
                "<math><mi>x</mi></math>" +
                "<svg><circle cx='5' cy='5' r='5'/></svg>" +
                "</body>";

        Document doc = Parser.parse(html, "http://example.com/");
        assertNotNull(doc.getElementById("t1"));
        assertNotNull(doc.getElementById("form1"));
        assertNotNull(doc.getElementById("s1"));
        assertEquals(2, doc.getElementsByTag("li").size());
        assertEquals(1, doc.getElementsByTag("textarea").size());
        assertEquals(1, doc.getElementsByTag("math").size());
        assertEquals(1, doc.getElementsByTag("svg").size());
    }

    @Test(timeout = 4000)
    public void testInBodySpecialEdgeCases() {
        // Plaintext tag transitions tokenizer permanently
        Document docPlaintext = Parser.parse("<body><plaintext>Some <b>HTML</b> not parsed", "");
        assertEquals("Some <b>HTML</b> not parsed", docPlaintext.body().getElementsByTag("plaintext").get(0).text());

        // Nested form error
        Document docNestedForm = Parser.parse("<body><form id='outer'><form id='inner'></form></form></body>", "");
        assertEquals("Nested form should be dropped", 1, docNestedForm.body().getElementsByTag("form").size());

        // Nested button closes previous button
        Document docButton = Parser.parse("<body><button id='b1'><button id='b2'>Click</button></button></body>", "");
        assertEquals(2, docButton.body().getElementsByTag("button").size());
        assertNotSame(docButton.getElementById("b1"), docButton.getElementById("b2").parent());

        // Nested headings close previous heading
        Document docHeadings = Parser.parse("<body><h1>Title 1<h2>Title 2</h2></h1></body>", "");
        assertEquals(1, docHeadings.body().getElementsByTag("h1").size());
        assertEquals(1, docHeadings.body().getElementsByTag("h2").size());

        // Dropped tags inside body (caption, col, tbody, etc.)
        Document docDrop = Parser.parse("<body><caption>Invalid</caption><colgroup><col></colgroup></body>", "");
        assertEquals(0, docDrop.body().getElementsByTag("caption").size());

        // Isindex creation
        Document docIsindex = Parser.parse("<body><isindex prompt='Filter: ' action='/search'></body>", "");
        assertEquals(1, docIsindex.body().getElementsByTag("form").size());
        assertEquals(1, docIsindex.body().getElementsByTag("input").size());
        assertEquals("isindex", docIsindex.body().getElementsByTag("input").get(0).attr("name"));
    }

    @Test(timeout = 4000)
    public void testInBodyAdoptionAgencyAlgorithm() {
        // Misnested formatting tags exercising AAA
        String html = "<p><b>Bold <i>Italic</b> Still Italic?</i> Normal</p>";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.body().getElementsByTag("b").first());
        assertNotNull(doc.body().getElementsByTag("i").first());

        // Nested anchor tag resets previous active formatting element
        String aHtml = "<a>Link 1 <a>Link 2</a></a>";
        Document aDoc = Parser.parse(aHtml, "");
        assertEquals(2, aDoc.body().getElementsByTag("a").size());

        // Nobr in scope error and re-creation
        String nobrHtml = "<nobr>One <nobr>Two</nobr></nobr>";
        Document nobrDoc = Parser.parse(nobrHtml, "");
        assertNotNull(nobrDoc.body().getElementsByTag("nobr").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagVariants() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<body><p>Text</p></body>", "");
        tb.transition(HtmlTreeBuilderState.InBody);

        // End tag br logs error and creates start tag br
        assertFalse(HtmlTreeBuilderState.InBody.process(new Token.EndTag("br"), tb));
        assertEquals(1, tb.getDocument().body().getElementsByTag("br").size());

        // End tag p when not in button scope creates an empty p element
        assertTrue(HtmlTreeBuilderState.InBody.process(new Token.EndTag("p"), tb));

        // End tag form when no form is in scope
        assertFalse(HtmlTreeBuilderState.InBody.process(new Token.EndTag("form"), tb));

        // End tag for sarcasm tag
        assertTrue(HtmlTreeBuilderState.InBody.process(new Token.EndTag("sarcasm"), tb));

        // End tag html delegates to body
        assertTrue(HtmlTreeBuilderState.InBody.process(new Token.EndTag("html"), tb));

        // End tag body transitions to AfterBody
        tb.parse("<body><div></div></body>", "");
        tb.transition(HtmlTreeBuilderState.InBody);
        assertTrue(HtmlTreeBuilderState.InBody.process(new Token.EndTag("body"), tb));
        assertSame(HtmlTreeBuilderState.AfterBody, tb.state());
    }

    // =========================================================================
    // Partition D: Table Parsing States (InTable, InTableText, InCaption, InRow, InCell, etc.)
    // =========================================================================

    @Test(timeout = 4000)
    public void testTableStatesFlow() {
        String tableHtml = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col width='100'></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Data 1</td><td>Data 2</td></tr></tbody>" +
                "<tfoot><tr><td>Footer</td></tr></tfoot>" +
                "</table>";
        Document doc = Parser.parse(tableHtml, "");
        assertNotNull(doc.getElementsByTag("caption").first());
        assertNotNull(doc.getElementsByTag("colgroup").first());
        assertNotNull(doc.getElementsByTag("thead").first());
        assertNotNull(doc.getElementsByTag("tbody").first());
        assertNotNull(doc.getElementsByTag("tfoot").first());
        assertEquals(2, doc.getElementsByTag("td").size());
        assertEquals(1, doc.getElementsByTag("th").size());
    }

    @Test(timeout = 4000)
    public void testInTableFosterParenting() {
        // Non-whitespace character in table triggers foster parenting into InBody before table
        String fosterHtml = "<table>Foster Text<tr><td>Cell</td></tr></table>";
        Document doc = Parser.parse(fosterHtml, "");
        assertTrue("Foster Text should appear before or outside the table",
                doc.body().text().startsWith("Foster Text"));
    }

    @Test(timeout = 4000)
    public void testInTableInputsAndForms() {
        // Hidden input allowed inside table directly
        String hiddenInput = "<table><input type='hidden' name='token' value='xyz'><tr><td>Cell</td></tr></table>";
        Document docHidden = Parser.parse(hiddenInput, "");
        assertEquals(1, docHidden.getElementsByTag("input").size());

        // Table with embedded script & style
        String scriptTable = "<table><script>var x=0;</script><style>td { border: 1px; }</style><tr><td>A</td></tr></table>";
        Document docScript = Parser.parse(scriptTable, "");
        assertEquals(1, docScript.getElementsByTag("script").size());
        assertEquals(1, docScript.getElementsByTag("style").size());

        // Nested table closes outer or foster parents
        String nestedTable = "<table><table><tr><td>Inner</td></tr></table></table>";
        Document docNested = Parser.parse(nestedTable, "");
        assertEquals(2, docNested.getElementsByTag("table").size());
    }

    @Test(timeout = 4000)
    public void testInTableTextNullCharacter() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<table>", "");
        tb.transition(HtmlTreeBuilderState.InTableText);

        Token.Character nullChar = new Token.Character(String.valueOf('\u0000'));
        boolean result = HtmlTreeBuilderState.InTableText.process(nullChar, tb);
        assertFalse("Null character in InTableText should return false and log error", result);
    }

    @Test(timeout = 4000)
    public void testInCaptionAndInColumnGroup() {
        // Caption closed by table tag
        Document capDoc = Parser.parse("<table><caption>Caption Text<tr><td>Cell</td></tr></table>", "");
        assertEquals("Caption Text", capDoc.getElementsByTag("caption").first().text());

        // Column group with col elements
        Document colDoc = Parser.parse("<table><colgroup><col id='c1'><col id='c2'></colgroup></table>", "");
        assertNotNull(colDoc.getElementById("c1"));
        assertNotNull(colDoc.getElementById("c2"));
    }

    @Test(timeout = 4000)
    public void testInRowAndInCellTransitions() {
        // Cells missing closing tags are auto-closed by next cell
        Document docCells = Parser.parse("<table><tr><td>1<td>2<td>3</tr></table>", "");
        assertEquals(3, docCells.getElementsByTag("td").size());

        // Row missing closing tr is auto-closed by next tr
        Document docRows = Parser.parse("<table><tr><td>R1<tr><td>R2</table>", "");
        assertEquals(2, docRows.getElementsByTag("tr").size());

        // Missing tr auto-inserted if td seen in table body
        Document docImplicitTr = Parser.parse("<table><tbody><td>Implicit</td></tbody></table>", "");
        assertEquals(1, docImplicitTr.getElementsByTag("tr").size());
    }

    // =========================================================================
    // Partition E: Select, Frameset, Text, and After* States
    // =========================================================================

    @Test(timeout = 4000)
    public void testInSelectAndInSelectInTable() {
        String selectInTable = "<table><tr><td>" +
                "<select id='s1'>" +
                "<option value='1'>One</option>" +
                "<option value='2'>Two</option>" +
                "</select>" +
                "</td></tr></table>";
        Document doc = Parser.parse(selectInTable, "");
        assertEquals(2, doc.getElementsByTag("option").size());

        // InSelect table tag interrupts select
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<table><select><option>Opt</option>", "");
        tb.transition(HtmlTreeBuilderState.InSelectInTable);
        Token.StartTag tableTag = new Token.StartTag("table");
        boolean processed = HtmlTreeBuilderState.InSelectInTable.process(tableTag, tb);
        assertTrue(processed);
    }

    @Test(timeout = 4000)
    public void testTextStateHandling() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<script>", "");
        assertSame(HtmlTreeBuilderState.Text, tb.state());

        // Character token inserted
        assertTrue(tb.process(new Token.Character("var a = 1;")));

        // EndTag script transitions back to original state
        assertTrue(tb.process(new Token.EndTag("script")));
        assertNotSame(HtmlTreeBuilderState.Text, tb.state());
    }

    @Test(timeout = 4000)
    public void testFramesetStatesFlow() {
        String framesetHtml = "<html>" +
                "<head><title>Frameset</title></head>" +
                "<frameset rows='50%,50%'>" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames supported</p></noframes>" +
                "</frameset>" +
                "</html>";
        Document doc = Parser.parse(framesetHtml, "");
        assertEquals(1, doc.getElementsByTag("frameset").size());
        assertEquals(2, doc.getElementsByTag("frame").size());
        assertEquals(1, doc.getElementsByTag("noframes").size());
    }

    @Test(timeout = 4000)
    public void testAfterBodyAndAfterAfterBody() {
        String html = "<html><head></head><body>Hello</body></html><!-- final comment -->";
        Document doc = Parser.parse(html, "");
        assertEquals("Hello", doc.body().text());

        // Trailing content after body
        String trailingHtml = "<html><body>Body</body></html><div>Trailing</div>";
        Document trailingDoc = Parser.parse(trailingHtml, "");
        assertEquals("Body Trailing", trailingDoc.body().text());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetAndAfterAfterFrameset() {
        String html = "<html><frameset><frame></frameset></html><!-- trailing comment -->";
        Document doc = Parser.parse(html, "");
        assertNotNull(doc.getElementsByTag("frameset").first());

        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<frameset></frameset>", "");
        tb.transition(HtmlTreeBuilderState.AfterAfterFrameset);

        // Noframes in AfterAfterFrameset delegates to InHead
        assertTrue(HtmlTreeBuilderState.AfterAfterFrameset.process(new Token.StartTag("noframes"), tb));
        // EOF in AfterAfterFrameset completes cleanly
        assertTrue(HtmlTreeBuilderState.AfterAfterFrameset.process(new Token.EOF(), tb));
    }

    @Test(timeout = 4000)
    public void testForeignContentDefault() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<div>", "");
        assertTrue("ForeignContent process should return true",
                HtmlTreeBuilderState.ForeignContent.process(new Token.StartTag("div"), tb));
    }

    @Test(timeout = 4000)
    public void testFragmentParsingBranches() {
        Element context = new Element(Tag.valueOf("body"), "");
        List<org.jsoup.nodes.Node> nodes = Parser.parseFragment("<div>Fragment Content</div>", context, "");
        assertFalse(nodes.isEmpty());
        assertEquals("div", nodes.get(0).nodeName());

        // Fragment parsing in select context
        Element selectContext = new Element(Tag.valueOf("select"), "");
        List<org.jsoup.nodes.Node> options = Parser.parseFragment("<option>Item 1</option><option>Item 2</option>", selectContext, "");
        assertEquals(2, options.size());

        // Fragment parsing in table context
        Element tableContext = new Element(Tag.valueOf("table"), "");
        List<org.jsoup.nodes.Node> rows = Parser.parseFragment("<tr><td>Cell</td></tr>", tableContext, "");
        assertFalse(rows.isEmpty());
    }
}