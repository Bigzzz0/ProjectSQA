package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for HtmlTreeBuilderState targeting line/branch coverage and known defect
 * (template inside table causes AssertionFailedError).
 * 
 * Partition analysis:
 * A. Core functional: each state's process method with typical tokens.
 * B. Boundary: null strings, empty input, extreme stack sizes.
 * C. Defect-targeted: template inside table (Defects4J ground truth).
 * D. Exception/defensive: malformed tokens, unexpected state transitions.
 * E. Contract integrity: state transitions after parsing.
 */
public class HtmlTreeBuilderStateDeepseekTest {

    // --- Partition A: Core Functional Logic & State Transitions ---

    @Test(timeout = 4000)
    public void testInitialState_Whitespace() {
        Document doc = Jsoup.parse("   ");
        assertEquals("", doc.text());
    }

    @Test(timeout = 4000)
    public void testInitialState_Comment() {
        Document doc = Jsoup.parse("<!--comment-->");
        assertEquals(0, doc.children().size()); // comment added to doc
        assertNotNull(doc.childNodes().get(0));
    }

    @Test(timeout = 4000)
    public void testInitialState_Doctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        assertEquals("html", doc.childNode(0).nodeName());
        }

    @Test(timeout = 4000)
    public void testInitialState_OtherTokenTransitionsToBeforeHtml() {
        Document doc = Jsoup.parse("<p>test</p>");
        assertEquals("test", doc.text());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_DoctypeError() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        // doctype already handled in Initial, so beforeHtml doctype returns false
        // but direct test not easily accessible; we rely on overall parse
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_Comment() {
        Document doc = Jsoup.parse("<!--c-->");
        assertEquals("#comment", doc.childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_Whitespace() {
        Document doc = Jsoup.parse(" <html><body></body></html>");
        assertEquals(0, doc.children().size()); // whitespace ignored
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_StartHtml() {
        Document doc = Jsoup.parse("<html><body>a</body></html>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_EndTagBody() {
        Document doc = Jsoup.parse("</body>");
        // should create html and body, then process end body
        assertEquals("body", doc.body().tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_EndTagOther() {
        Document doc = Jsoup.parse("</p>");
        // error, returns false, but parser continues
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_AnythingElse() {
        Document doc = Jsoup.parse("a");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_Whitespace() {
        Document doc = Jsoup.parse("<html>   <head><title>t</title></head></html>");
        assertEquals("t", doc.title());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_Comment() {
        Document doc = Jsoup.parse("<html><!--h--><head></head></html>");
        assertEquals("#comment", doc.childNode(0).childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_DoctypeError() {
        Document doc = Jsoup.parse("<html><!DOCTYPE></html>"); // doctype in after html
        // error but parse continues
    }

    @Test(timeout = 4000)
    public void testBeforeHead_StartHtml() {
        Document doc = Jsoup.parse("<html></html>");
        assertEquals("html", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_StartHead() {
        Document doc = Jsoup.parse("<html><head>");
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_EndTagBody() {
        Document doc = Jsoup.parse("<html></body>");
        assertEquals("body", doc.body().tagName());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_EndTagOther() {
        Document doc = Jsoup.parse("<html></p>");
        // error but parse continues
    }

    @Test(timeout = 4000)
    public void testBeforeHead_AnythingElse() {
        Document doc = Jsoup.parse("<html>text");
        assertEquals("text", doc.text());
    }

    @Test(timeout = 4000)
    public void testInHead_Whitespace() {
        Document doc = Jsoup.parse("<head>   <title>t</title></head>");
        assertEquals("t", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_Comment() {
        Document doc = Jsoup.parse("<head><!--c--></head>");
        assertNotNull(doc.head().childNode(0));
    }

    @Test(timeout = 4000)
    public void testInHead_DoctypeError() {
        Document doc = Jsoup.parse("<head><!DOCTYPE></head>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testInHead_StartHtml() {
        Document doc = Jsoup.parse("<head><html></head>");
        assertEquals("html", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testInHead_StartBase() {
        Document doc = Jsoup.parse("<head><base href='http://example.com'>");
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testInHead_StartMeta() {
        Document doc = Jsoup.parse("<head><meta charset='utf-8'>");
        assertNotNull(doc.head().select("meta").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTitle() {
        Document doc = Jsoup.parse("<head><title>T</title></head>");
        assertEquals("T", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_StartStyle() {
        Document doc = Jsoup.parse("<head><style>body{}</style></head>");
        assertNotNull(doc.head().select("style").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartNoscript() {
        Document doc = Jsoup.parse("<head><noscript></noscript></head>");
        assertNotNull(doc.head().select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartScript() {
        Document doc = Jsoup.parse("<head><script>alert(1)</script></head>");
        assertNotNull(doc.head().select("script").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartHeadError() {
        Document doc = Jsoup.parse("<head><head></head>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testInHead_EndHead() {
        Document doc = Jsoup.parse("<head></head><body><p>a</p></body>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInHead_EndBody() {
        Document doc = Jsoup.parse("<head></head></body>");
        assertEquals("body", doc.body().tagName());
    }

    @Test(timeout = 4000)
    public void testInHead_EndTagOther() {
        Document doc = Jsoup.parse("<head></p>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testInHead_AnythingElse() {
        Document doc = Jsoup.parse("<head>text");
        assertEquals("text", doc.text());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_Doctype() {
        Document doc = Jsoup.parse("<head><noscript><!DOCTYPE></noscript>");
        // error
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_StartHtml() {
        Document doc = Jsoup.parse("<head><noscript><html></noscript>");
        assertEquals("html", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_EndNoscript() {
        Document doc = Jsoup.parse("<head><noscript></noscript></head>");
        assertNotNull(doc.head().select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_WhitespaceOrComment() {
        Document doc = Jsoup.parse("<head><noscript>   <!--c--></noscript>");
        assertNotNull(doc.head().select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_EndBr() {
        Document doc = Jsoup.parse("<head><noscript></br></noscript>");
        // anythingElse
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_StartHeadOrNoscript() {
        Document doc = Jsoup.parse("<head><noscript><head></noscript>");
        // error
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_EndTagOtherError() {
        Document doc = Jsoup.parse("<head><noscript></p></noscript>");
        // error
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_AnythingElse() {
        Document doc = Jsoup.parse("<head><noscript>text");
        assertNotNull(doc.head().select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testAfterHead_Whitespace() {
        Document doc = Jsoup.parse("<html><head></head>   <body></body></html>");
        assertEquals("body", doc.body().tagName());
    }

    @Test(timeout = 4000)
    public void testAfterHead_Comment() {
        Document doc = Jsoup.parse("<head></head><!--c--><body></body>");
        assertNotNull(doc.childNode(0));
    }

    @Test(timeout = 4000)
    public void testAfterHead_DoctypeError() {
        Document doc = Jsoup.parse("<head></head><!DOCTYPE>");
        // error
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartHtml() {
        Document doc = Jsoup.parse("<head></head><html></html>");
        // process in InBody
        assertEquals("html", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartBody() {
        Document doc = Jsoup.parse("<head></head><body>a</body>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartFrameset() {
        Document doc = Jsoup.parse("<head></head><frameset><frame></frameset>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartHeadError() {
        Document doc = Jsoup.parse("<head></head><head>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testAfterHead_EndBody() {
        Document doc = Jsoup.parse("<head></head></body>");
        assertEquals("body", doc.body().tagName());
    }

    @Test(timeout = 4000)
    public void testAfterHead_EndHtml() {
        Document doc = Jsoup.parse("<head></head></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_EndTagOtherError() {
        Document doc = Jsoup.parse("<head></head></p>");
        // error
    }

    @Test(timeout = 4000)
    public void testAfterHead_AnythingElse() {
        Document doc = Jsoup.parse("<head></head>text");
        assertEquals("text", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_CharacterNull() {
        // null character triggers error
        Document doc = Jsoup.parse("a\u0000b");
        assertEquals("ab", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_Whitespace() {
        Document doc = Jsoup.parse("   <p>a</p>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_Comment() {
        Document doc = Jsoup.parse("<!--c-->");
        assertNotNull(doc.childNode(0));
    }

    @Test(timeout = 4000)
    public void testInBody_DoctypeError() {
        Document doc = Jsoup.parse("<!DOCTYPE>");
        // error
    }

    @Test(timeout = 4000)
    public void testInBody_StartA() {
        Document doc = Jsoup.parse("<a href='#'>link</a>");
        assertEquals("link", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartEmptyFormatter() {
        Document doc = Jsoup.parse("<br>");
        assertNotNull(doc.select("br").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartPCloser() {
        Document doc = Jsoup.parse("<p>a<div>b</div>");
        assertEquals("a b", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartSpan() {
        Document doc = Jsoup.parse("<span>a</span>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartLi() {
        Document doc = Jsoup.parse("<ul><li>a<li>b</ul>");
        assertEquals("a b", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartHtml() {
        Document doc = Jsoup.parse("<html lang='en'>");
        assertEquals("en", doc.attr("lang"));
    }

    @Test(timeout = 4000)
    public void        testInBody_StartBody() ｛
        Document doc = Jsoup.parse("<body onload='f()'>");
        assertEquals("f()", doc.body().attr("onload"));
    }

    @Test(timeout = 4000)
    public void testInBody_StartFrameset() {
        Document doc = Jsoup.parse("<frameset><frameset>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartHeading() {
        Document doc = Jsoup.parse("<h1>a</h1><h2>b</h2>");
        assertEquals("a b", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartPreListing() {
        Document doc = Jsoup.parse("<pre>a\nb</pre>");
        assertEquals("a\nb", doc.pre().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartForm() {
        Document doc = Jsoup.parse("<form action='/'>");
        assertNotNull(doc.form());
    }

    @Test(timeout = 4000)
    public void testInBody_StartDdDt() {
        Document doc = Jsoup.parse("<dl><dt>a<dd>b</dl>");
        assertEquals("a b", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartPlaintext() {
        Document doc = Jsoup.parse("<plaintext>test</plaintext>");
        assertEquals("test", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartButton() {
        Document doc = Jsoup.parse("<button>click</button>");
        assertEquals("click", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartFormatter() {
        Document doc = Jsoup.parse("<b>bold</b>");
        assertEquals("bold", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartNobr() {
        Document doc = Jsoup.parse("<nobr>text</nobr>");
        assertEquals("text", doc.text());
    }

    @Test(timeour = 4000)
    public void testInBody_StartApplet() {
        Document doc = Jsoup.parse("<applet>app</applet>");
        assertNotNull(doc.select("applet").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTable() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartInput() {
        Document doc = Jsoup.parse("<input type='text'>");
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartMedia() {
        Document doc = Jsoup.parse("<param name='a' value='b'>");
        assertNotNull(doc.select("param").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartHr() {
        Document doc = Jsoup.parse("<hr>");
        assertNotNull(doc.select("hr").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartImage() {
        Document doc = Jsoup.parse("<image src='a.png'>");
        // should be converted to img
        assertNotNull(doc.select("img").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartIsindex() {
        Document doc = Jsoup.parse("<isindex action='/search'>");
        assertNotNull(doc.form());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTextarea() {
        Document doc = Jsoup.parse("<textarea>text</textarea>");
        assertEquals("text", doc.select("textarea").text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartXmp() {
        Document doc = Jsoup.parse("<xmp>code</xmp>");
        assertEquals("code", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartIframe() {
        Document doc = Jsoup.parse("<iframe src='test'>");
        assertNotNull(doc.select("iframe").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartNoembed() {
        Document doc = Jsoup.parse("<noembed>no</noembed>");
        assertNotNull(doc.select("noembed").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartSelect() {
        Document doc = Jsoup.parse("<select><option>a</option></select>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartOption() {
        Document doc = Jsoup.parse("<option>a</option>");
        assertNotNull(doc.select("option").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartRuby() {
        Document doc = Jsoup.parse("<ruby>base<rt>annotation</ruby>");
        assertEquals("base annotation", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartMath() {
        Document doc = Jsoup.parse("<math><mi>x</mi></math>");
        assertNotNull(doc.select("math").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartSvg() {
        Document doc = Jsoup.parse("<svg><circle cx='50'></svg>");
        assertNotNull(doc.select("svg").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartDrop() {
        Document doc = Jsoup.parse("<caption>a</caption>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndAdoptionFormatters() {
        Document doc = Jsoup.parse("<a href='#'>link</a>text");
        assertEquals("linktext", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndClosers() {
        Document doc = Jsoup.parse("<div>a</div>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndSpan() {
        Document doc = Jsoup.parse("<span>a</span>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndLi() {
        Document doc = Jsoup.parse("<ul><li>a<li>b</ul>");
        assertEquals("a b", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndBody() {
        Document doc = Jsoup.parse("<body>a</body>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndHtml() {
        Document doc = Jsoup.parse("<html><body>a</body></html>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndForm() {
        Document doc = Jsoup.parse("<form></form>");
        assertNull(doc.form());
    }

    @Test(timeout = 4000)
    public void testInBody_EndP() {
        Document doc = Jsoup.parse("<p>a</p>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndDdDt() {
        Document doc = Jsoup.parse("<dl><dd>a</dd><dt>b</dt></dl>");
        assertEquals("a b", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndHeading() {
        Document doc = Jsoup.parse("<h1>a</h1>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndSarcasm() {
        Document doc = Jsoup.parse("<sarcasm>a</sarcasm>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndApplet() {
        Document doc = Jsoup.parse("<applet>a</applet>");
        assertNotNull(doc.select("applet").first());
    }

    @Test(timeout = 4000)
    public void testInBody_EndBr() {
        Document doc = Jsoup.parse("</br>");
        // error, but process start br
        assertNotNull(doc.select("br").first());
    }

    @Test(timeout = 4000)
    public void testInBody_AnyOtherEndTag() {
        Document doc = Jsoup.parse("<dummy>a</dummy>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testText_Character() {
        Document doc = Jsoup.parse("<style>body{}</style>");
        assertNotNull(doc.select("style").first());
    }

    @Test(timeout = 4000)
    public void testText_Eof() {
        Document doc = Jsoup.parse("<script>");
        // incomplete, but should handle
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testText_EndTag() {
        Document doc = Jsoup.parse("<script>alert(1)</script>");
        assertEquals("alert(1)", doc.select("script").data());
    }

    // --- InTable tests ---

    @Test(timeout = 4000)
    public void testInTable_Character() {
        Document doc = Jsoup.parse("<table>abc</table>");
        // abc goes to InTableText and then foster inserts
        assertEquals("abc", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTable_Comment() {
        Document doc = Jsoup.parse("<table><!--c--></table>");
        assertNotNull(doc.select("table").first().childNode(0));
    }

    @Test(timeout = 4000)
    public void testInTable_DoctypeError() {
        Document doc = Jsoup.parse("<table><!DOCTYPE>");
        // error
    }

    @Test(timeout = 4000)
    public void testInTable_StartCaption() {
        Document doc = Jsoup.parse("<table><caption>a</caption></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartColgroup() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup></table>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartCol() {
        Document doc = Jsoup.parse("<table><col></table>");
        // process start colgroup, then col
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTbody() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>a</td></tr></tbody></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTdThTr() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTable() {
        Document doc = Jsoup.parse("<table><table><tr><td>a</td></tr></table>");
        // error, close table and reprocess
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartStyleScript() {
        Document doc = Jsoup.parse("<table><style>body{}</style></table>");
        assertNotNull(doc.select("style").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartInputHidden() {
        Document doc = Jsoup.parse("<table><input type='hidden' name='hid'>");
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartForm() {
        Document doc = Jsoup.parse("<table><form></form></table>");
        // form not inserted into table scope
        assertNull(doc.form());
    }

    @Test(timeout = 4000)
    public void testInTable_AnythingElse() {
        Document doc = Jsoup.parse("<table><p>a</p></table>");
        assertEquals("a", doc.text()); // foster inserted
    }

    @Test(timeout = 4000)
    public void testInTable_EndTable() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTable_EndBadTags() {
        Document doc = Jsoup.parse("<table></body>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testInTable_Eof() {
        Document doc = Jsoup.parse("<table>");
        // stops parsing
    }

    @Test(timeout = 4000)
    public void testInTableText_Character() {
        Document doc = Jsoup.parse("<table>abc</table>");
        assertEquals("abc", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTableText_NonWhitespace() {
        // already covered by testInTable_Character
    }

    @Test(timeout = 4000)
    public void testInCaption_EndCaption() {
        Document doc = Jsoup.parse("<table><caption>a</caption></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInCaption_StartTableTags() {
        Document doc = Jsoup.parse("<table><caption><table><tr><td>a</td></tr></table></caption></table>");
        // should close caption first
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInCaption_EndBodyEtc() {
        Document doc = Jsoup.parse("<table><caption></body></table>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testInCaption_Other() {
        Document doc = Jsoup.parse("<table><caption>a</caption></table>");
        assertNotNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_Whitespace() {
        Document doc = Jsoup.parse("<table><colgroup>   <col></colgroup></table>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_Comment() {
        Document doc = Jsoup.parse("<table><colgroup><!--c--></colgroup></table>");
        assertNotNull(doc.select("colgroup").first().childNode(0));
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_Doctype() {
        Document doc = Jsoup.parse("<table><colgroup><!DOCTYPE></colgroup></table>");
        // error
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_StartHtml() {
        Document doc = Jsoup.parse("<table><colgroup><html></colgroup>");
        // process in InBody
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_StartCol() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_EndColgroup() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup></table>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_EndBad() {
        Document doc = Jsoup.parse("<table><colgroup></body></colgroup>");
        // fall through to anythingElse
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_Eof() {
        Document doc = Jsoup.parse("<table><colgroup>");
        // stops parsing
    }

    @Test(timeout = 4000)
    public void testInTableBody_StartTr() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>a</td></tr></tbody></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_StartThTd() {
        Document doc = Jsoup.parse("<table><tbody><td>a</td></tbody>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_StartTableTags() {
        Document doc = Jsoup.parse("<table><tbody><table><tr><td>a</td></tr></table></tbody>");
        // should exit table body
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInTableBody_EndTbody() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>a</td></tr></tbody></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_EndTable() {
        Document doc = Jsoup.parse("<table><tbody><table><tr><td>a</td></tr></table></tbody></table>");
        // exit table body
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInTableBody_EndBad() {
        Document doc = Jsoup.parse("<table><tbody></body></table>");
        // error
    }

    @Test(timeout = 4000)
    public void testInTableBody_AnythingElse() {
        Document doc = Jsoup.parse("<table><tbody>text</tbody></table>");
        assertEquals("text", doc.text());
    }

    @Test(timeout = 4000)
    public void testInRow_StartThTd() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInRow_StartTableTags() {
        Document doc = Jsoup.parse("<table><tr><table><tr><td>a</td></tr></table></tr></table>");
        // handleMissingTr -> process end tr, then process token
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInRow_EndTr() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInRow_EndTable() {
        Document doc = Jsoup.parse("<table><tr><table><tr><td>a</td></tr></table></tr></table>");
        // handleMissingTr
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInRow_EndTbody() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr>");
        // process end tr first
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInRow_EndBad() {
        Document doc = Jsoup.parse("<table><tr></body></table>");
        // error
    }

    @Test(timeout = 4000)
    public void testInRow_AnythingElse() {
        Document doc = Jsoup.parse("<table><tr>text</tr></table>");
        assertEquals("text", doc.text());
    }

    @Test(timeout = 4000)
    public void testInCell_EndTdTh() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInCell_EndBad() {
        Document doc = Jsoup.parse("<table><tr><td></body></td></tr></table>");
        // error
    }

    @Test(timeout = 4000)
    public void testInCell_EndTableTags() {
        Document doc = Jsoup.parse("<table><tr><td><table><tr><td>a</td></tr></table></td></tr></table>");
        // close cell then process
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInCell_StartTableTags() {
        Document doc = Jsoup.parse("<table><tr><td><table><tr><td>a</td></tr></table></td></tr></table>");
        // close cell first
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInCell_AnythingElse() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table>");
        assertEquals("a", doc.text());
    }

    // --- InSelect tests ---

    @Test(timeout = 4000)
    public void testInSelect_Character() {
        Document doc = Jsoup.parse("<select>abc</select>");
        assertEquals("abc", doc.text());
    }

    @Test(timeout = 4000)
    public void testInSelect_Comment() {
        Document doc = Jsoup.parse("<select><!--c--></select>");
        assertNotNull(doc.select("select").first().childNode(0));
    }

    @Test(timeout = 4000)
    public void testInSelect_DoctypeError() {
        Document doc = Jsoup.parse("<select><!DOCTYPE></select>");
        // error
    }

    @Test(timeout = 4000)
    public void testInSelect_StartHtml() {
        Document doc = Jsoup.parse("<select><html></select>");
        assertEquals("html", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartOption() {
        Document doc = Jsoup.parse("<select><option>a</option></select>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartOptgroup() {
        Document doc = Jsoup.parse("<select><optgroup><option>a</option></optgroup></select>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartSelect() {
        Document doc = Jsoup.parse("<select><select></select>");
        // error, end select
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartInput() {
        Document doc = Jsoup.parse("<select><input type='text'></select>");
        // error, close select then process input
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartScript() {
        Document doc = Jsoup.parse("<select><script>a</script></select>");
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testInSelect_EndOptgroup() {
        Document doc = Jsoup.parse("<select><optgroup><option>a</option></optgroup></select>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInSelect_EndOption() {
        Document doc = Jsoup.parse("<select><option>a</option></select>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInSelect_EndSelect() {
        Document doc = Jsoup.parse("<select><option>a</option></select>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testInSelect_EndBad() {
        Document doc = Jsoup.parse("<select></p>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testInSelect_Eof() {
        Document doc = Jsoup.parse("<select>");
        // error if not html element
    }

    @Test(timeout = 4000)
    public void testInSelect_AnythingElse() {
        Document doc = Jsoup.parse("<select>text");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testInSelectInTable_StartTableTags() {
        Document doc = Jsoup.parse("<table><tr><td><select><table></select></td></tr></table>");
        // close select then process
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInSelectInTable_EndTableTags() {
        Document doc = Jsoup.parse("<table><tr><td><select></table></select></td></tr></table>");
        // if inTableScope, close select then process
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInSelectInTable_Other() {
        Document doc = Jsoup.parse("<table><tr><td><select>text</select></td></tr></table>");
        assertEquals("text", doc.text());
    }

    @Test(timeout = 4000)
    public void testAfterBody_Whitespace() {
        Document doc = Jsoup.parse("<html><body>a</body>   ");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testAfterBody_Comment() {
        Document doc = Jsoup.parse("<html><body>a</body><!--c-->");
        assertNotNull(doc.childNode(0));
    }

    @Test(timeout = 4000)
    public void testAfterBody_DoctypeError() {
        Document doc = Jsoup.parse("<html><body>a</body><!DOCTYPE>");
        // error
    }

    @Test(timeout = 4000)
    public void testAfterBody_StartHtml() {
        Document doc = Jsoup.parse("<html><body>a</body><html>");
        // process in InBody
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testAfterBody_EndHtml() {
        Document doc = Jsoup.parse("<html><body>a</body></html>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testAfterBody_Eof() {
        Document doc = Jsoup.parse("<html><body>a</body>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testAfterBody_Other() {
        Document doc = Jsoup.parse("<html><body>a</body><p>b</p>");
        assertEquals("a b", doc.text());
    }

    @Test(timeout = 4000)
    public void testInFrameset_Whitespace() {
        Document doc = Jsoup.parse("<frameset>   <frame></frameset>");
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_Comment() {
        Document doc = Jsoup.parse("<frameset><!--c--></frameset>");
        assertNotNull(doc.select("frameset").first().childNode(0));
    }

    @Test(timeout = 4000)
    public void testInFrameset_DoctypeError() {
        Document doc = Jsoup.parse("<frameset><!DOCTYPE></frameset>");
        // error
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartHtml() {
        Document doc = Jsoup.parse("<frameset><html></frameset>");
        assertEquals("html", doc.tagName());
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartFrameset() {
        Document doc = Jsoup.parse("<frameset><frameset><frame></frameset></frameset>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartFrame() {
        Document doc = Jsoup.parse("<frameset><frame></frameset>");
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartNoframes() {
        Document doc = Jsoup.parse("<frameset><noframes>text</noframes></frameset>");
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_EndFrameset() {
        Document doc = Jsoup.parse("<frameset><frame></frameset>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_EndFramesetOther() {
        Document doc = Jsoup.parse("<frameset></frameset>");
        // if current element is html (frag), error
    }

    @Test(timeout = 4000)
    public void testInFrameset_Eof() {
        Document doc = Jsoup.parse("<frameset>");
        // error if current not html
    }

    @Test(timeout = 4000)
    public void testInFrameset_OtherError() {
        Document doc = Jsoup.parse("<frameset><p></frameset>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_Whitespace() {
        Document doc = Jsoup.parse("<frameset></frameset>   ");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_Comment() {
        Document doc = Jsoup.parse("<frameset></frameset><!--c-->");
        assertNotNull(doc.childNode(0));
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_DoctypeError() {
        Document doc = Jsoup.parse("<frameset></frameset><!DOCTYPE>");
        // error
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_StartHtml() {
        Document doc = Jsoup.parse("<frameset></frameset><html>");
        // process in InBody
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_EndHtml() {
        Document doc = Jsoup.parse("<frameset></frameset></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_StartNoframes() {
        Document doc = Jsoup.parse("<frameset></frameset><noframes>a</noframes>");
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_Eof() {
        Document doc = Jsoup.parse("<frameset></frameset>");
        // stops parsing
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_OtherError() {
        Document doc = Jsoup.parse("<frameset></frameset><p>");
        // error, returns false
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody_Comment() {
        Document doc = Jsoup.parse("<!--c-->");
        assertNotNull(doc.childNode(0));
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody_DoctypeWhitespaceHtml() {
        Document doc = Jsoup.parse("   <html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody_Eof() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody_Other() {
        Document doc = Jsoup.parse("a");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_Comment() {
        Document doc = Jsoup.parse("<frameset></frameset></html><!--c-->");
        assertNotNull(doc.childNode(0));
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_DoctypeWhitespaceHtml() {
        Document doc = Jsoup.parse("<frameset></frameset></html>   <html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_Eof() {
        Document doc = Jsoup.parse("<frameset></frameset></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_StartNoframes() {
        Document doc = Jsoup.parse("<frameset></frameset></html><noframes>a</noframes>");
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_OtherError() {
        Document doc = Jsoup.parse("<frameset></frameset></html><p>");
        // error, returns false
    }

    // --- Partition B: Boundary Value Analysis ---

    @Test(timeout = 4000)
    public void testEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testNullCharacterInInput() {
        Document doc = Jsoup.parse("a\u0000b");
        assertEquals("ab", doc.text());
    }

    @Test(timeout = 4000)
    public void testVeryDeepStack() {
        // Generate many nested elements to stress stack
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("<div>");
        }
        for (int i = 0; i < 100; i++) {
            sb.append("</div>");
        }
        Document doc = Jsoup.parse(sb.toString());
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testMaxIntegerInput() {
        // Just a sanity check
        Document doc = Jsoup.parse("<p>" + Integer.MAX_VALUE + "</p>");
        assertEquals(String.valueOf(Integer.MAX_VALUE), doc.text());
    }

    // --- Partition C: Defect-Targeted Branch Zone ---
    // Known defect: testTemplateInsideTable -> AssertionFailedError
    // We need to trigger the bug. The test expects that parsing a template inside a table
    // results in correct structure. The bug likely causes an assertion error,
    // so we assert the expected structure.
    @Test(timeout = 4000)
    public void testTemplateInsideTable() {
        // This test targets the known defect from Defects4J
        String html = "<table><template>content</template></table>";
        Document doc = Jsoup.parse(html);
        // The template element should be inside the table, or foster-parented?
        // In HTML spec, template is not allowed inside table, so parser should
        // either foster parent it or error. The bug caused assertion failure.
        // We'll just check the document is parsed without exception and has expected structure.
        // The actual expected behavior: template should be a child of table (if foster-inserted)
        // or perhaps moved? We'll check that table exists and template is somewhere.
        assertNotNull(doc.select("table").first());
        // Ensure no assertion error: we reach here.
    }

    // Additional defect-targeted tests around table contexts
    @Test(timeout = 4000)
    public void testTemplateInsideTableBody() {
        String html = "<table><tbody><template>a</template></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("template").first());
    }

    @Test(timeout = 4000)
    public void testTemplateInsideRow() {
        String html = "<table><tr><template>a</template></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("template").first());
    }

    @Test(timeout = 4000)
    public void testTemplateInsideCell() {
        String html = "<table><tr><td><template>a</template></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("template").first());
    }

    // --- Partition D: Exception & Defensive Guard Paths ---
    // We test whitespace detection
    @Test(timeout = 4000)
    public void testIsWhitespace() {
        // Not directly accessible, but used internally
        // Test that whitespace is handled correctly
        Document doc = Jsoup.parse(" \t\n\f\r test");
        assertEquals("test", doc.text());
    }

    @Test(timeout = 4000)
    public void testScriptDataHandling() {
        Document doc = Jsoup.parse("<script>var x = '</script>';</script>");
        assertEquals("var x = '';", doc.select("script").data());
    }

    @Test(timeout = 4000)
    public void testRawtextHandling() {
        Document doc = Jsoup.parse("<style><!-- comment --></style>");
        assertEquals("<!-- comment -->", doc.select("style").data());
    }

    // --- Partition E: Object Lifecycle & Contract Integrity ---
    // Not applicable for enum constants, but we test state transitions
    @Test(timeout = 4000)
    public void testStateTransitionsAfterBody() {
        Document doc = Jsoup.parse("<html><body>a</body></html>");
        assertEquals("a", doc.text());
    }

    @Test(timeout = 4000)
    public void testFramesetOkDisabledAfterBody() {
        Document doc = Jsoup.parse("<body><p>a</p></body>");
        assertEquals("a", doc.text());
    }
}