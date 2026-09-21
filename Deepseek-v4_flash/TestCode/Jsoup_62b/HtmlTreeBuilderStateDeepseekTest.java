package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Test suite for HtmlTreeBuilderState, targeting maximum line/branch coverage and the known
 * case-sensitivity defect (Defects4J). 
 * 
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional paths for each state (Initial, BeforeHtml, BeforeHead, InHead, 
 *   InHeadNoscript, AfterHead, InBody, Text, InTable, InTableText, InCaption, InColumnGroup, 
 *   InTableBody, InRow, InCell, InSelect, InSelectInTable, AfterBody, InFrameset, AfterFrameset, 
 *   AfterAfterBody, AfterAfterFrameset, ForeignContent).
 * - Partition B: Boundary values – whitespace, empty strings, null characters, EOF, 
 *   doctype quirks, fragment parsing.
 * - Partition C: Defect-targeted – case-sensitive tag handling (the known failure: 
 *   "<r><X>A<y>B</y></X></r>" incorrectly nested).
 * - Partition D: Exception/error paths – invalid end tags, duplicate form, missing scope, 
 *   foster parenting, etc.
 * - Partition E: Object lifecycle – state transitions, stack management, formatting elements.
 */
public class HtmlTreeBuilderStateDeepseekTest {

    // ==================== Partition A: Core Functional Paths ====================

    @Test(timeout = 4000)
    public void testInitialState_Whitespace() {
        // Initial state ignores whitespace
        Document doc = Jsoup.parse("   ");
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testInitialState_Comment() {
        Document doc = Jsoup.parse("<!-- comment -->");
        assertEquals("<!-- comment -->", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testInitialState_Doctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        assertEquals("", doc.body().html());
        assertEquals("html", doc.documentType().name());
    }

    @Test(timeout = 4000)
    public void testInitialState_DoctypeQuirks() {
        Document doc = Jsoup.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testInitialState_OtherToken() {
        // Token that is not whitespace/comment/doctype triggers BeforeHtml
        Document doc = Jsoup.parse("<p>text</p>");
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_Doctype() {
        // Doctype in BeforeHtml should error and return false
        Document doc = Jsoup.parse("<!DOCTYPE html><html><body></body></html>");
        // Should still parse, but doctype is ignored? Actually doctype before html is allowed? 
        // The spec says it's an error but still processed. We'll just check no exception.
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_Comment() {
        Document doc = Jsoup.parse("<!-- comment --><html><body></body></html>");
        assertEquals("<!-- comment -->", doc.head().html()); // comment inserted before html
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_Whitespace() {
        Document doc = Jsoup.parse("   <html><body></body></html>");
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_StartTagHtml() {
        Document doc = Jsoup.parse("<html><body><p>text</p></body></html>");
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_EndTagHeadBodyHtmlBr() {
        // End tags for head, body, html, br trigger anythingElse
        Document doc = Jsoup.parse("</head><html><body></body></html>");
        // Should still parse
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_OtherEndTag() {
        // Other end tag should error and return false
        Document doc = Jsoup.parse("</div><html><body></body></html>");
        // Should still parse, but error logged
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHtml_AnythingElse() {
        // Any other token (e.g., start tag not html) triggers anythingElse
        Document doc = Jsoup.parse("<div><html><body></body></html>");
        // Should insert html and reprocess
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHead_Whitespace() {
        Document doc = Jsoup.parse("<html>   <head></head><body></body></html>");
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_Comment() {
        Document doc = Jsoup.parse("<html><!-- comment --><head></head><body></body></html>");
        assertEquals("<!-- comment -->", doc.head().html());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_Doctype() {
        Document doc = Jsoup.parse("<html><!DOCTYPE html><head></head><body></body></html>");
        // Doctype in before head is error, but still processed? Actually it's ignored.
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHead_StartTagHtml() {
        Document doc = Jsoup.parse("<html><html><head></head><body></body></html>");
        // Second html start tag is processed in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHead_StartTagHead() {
        Document doc = Jsoup.parse("<html><head><title>test</title></head><body></body></html>");
        assertEquals("test", doc.title());
    }

    @Test(timeout = 4000)
    public void testBeforeHead_EndTagHeadBodyHtmlBr() {
        Document doc = Jsoup.parse("<html></head><head></head><body></body></html>");
        // Should process start tag head and then reprocess end tag
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHead_OtherEndTag() {
        Document doc = Jsoup.parse("<html></div><head></head><body></body></html>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHead_AnythingElse() {
        Document doc = Jsoup.parse("<html><div><head></head><body></body></html>");
        // Should process start tag head and then reprocess div
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHead_Whitespace() {
        Document doc = Jsoup.parse("<html><head>   <title>test</title></head><body></body></html>");
        assertEquals("test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_Comment() {
        Document doc = Jsoup.parse("<html><head><!-- comment --><title>test</title></head><body></body></html>");
        assertEquals("<!-- comment -->", doc.head().html().substring(0, 14));
    }

    @Test(timeout = 4000)
    public void testInHead_Doctype() {
        Document doc = Jsoup.parse("<html><head><!DOCTYPE html><title>test</title></head><body></body></html>");
        // Doctype in head is error, ignored
        assertEquals("test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagHtml() {
        Document doc = Jsoup.parse("<html><head><html><title>test</title></head><body></body></html>");
        // Second html start tag processed in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagBase() {
        Document doc = Jsoup.parse("<html><head><base href='http://example.com/'><title>test</title></head><body></body></html>");
        assertEquals("http://example.com/", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagMeta() {
        Document doc = Jsoup.parse("<html><head><meta charset='utf-8'><title>test</title></head><body></body></html>");
        // Meta is inserted
        assertNotNull(doc.select("meta").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagTitle() {
        Document doc = Jsoup.parse("<html><head><title>test</title></head><body></body></html>");
        assertEquals("test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagStyle() {
        Document doc = Jsoup.parse("<html><head><style>body { color: red; }</style></head><body></body></html>");
        assertNotNull(doc.select("style").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagNoscript() {
        Document doc = Jsoup.parse("<html><head><noscript><p>no script</p></noscript></head><body></body></html>");
        // Noscript is inserted, then transitions to InHeadNoscript
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagScript() {
        Document doc = Jsoup.parse("<html><head><script>alert('test');</script></head><body></body></html>");
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testInHead_StartTagHead() {
        Document doc = Jsoup.parse("<html><head><head><title>test</title></head><body></body></html>");
        // Second head is error, ignored
        assertEquals("test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_AnythingElse() {
        Document doc = Jsoup.parse("<html><head><div><title>test</title></head><body></body></html>");
        // Anything else triggers processEndTag("head") and reprocess
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHead_EndTagHead() {
        Document doc = Jsoup.parse("<html><head><title>test</title></head><body></body></html>");
        assertEquals("test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHead_EndTagBodyHtmlBr() {
        Document doc = Jsoup.parse("<html><head></head><body></body></html>");
        // End tag body/html/br triggers anythingElse
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHead_OtherEndTag() {
        Document doc = Jsoup.parse("<html><head></div><title>test</title></head><body></body></html>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_Doctype() {
        Document doc = Jsoup.parse("<html><head><noscript><!DOCTYPE html></noscript></head><body></body></html>");
        // Doctype in noscript is error
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_StartTagHtml() {
        Document doc = Jsoup.parse("<html><head><noscript><html><body></body></html></noscript></head><body></body></html>");
        // Process in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_EndTagNoscript() {
        Document doc = Jsoup.parse("<html><head><noscript></noscript></head><body></body></html>");
        // Pop and transition to InHead
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_WhitespaceCommentStartTagAllowed() {
        Document doc = Jsoup.parse("<html><head><noscript>   <!-- comment --><base href='http://example.com/'></noscript></head><body></body></html>");
        // Process in InHead
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_EndTagBr() {
        Document doc = Jsoup.parse("<html><head><noscript></br></noscript></head><body></body></html>");
        // anythingElse
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_StartTagHeadNoscript() {
        Document doc = Jsoup.parse("<html><head><noscript><head></head></noscript></head><body></body></html>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscript_AnythingElse() {
        Document doc = Jsoup.parse("<html><head><noscript><p>text</p></noscript></head><body></body></html>");
        // anythingElse inserts character data
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_Whitespace() {
        Document doc = Jsoup.parse("<html><head></head>   <body></body></html>");
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterHead_Comment() {
        Document doc = Jsoup.parse("<html><head></head><!-- comment --><body></body></html>");
        assertEquals("<!-- comment -->", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterHead_Doctype() {
        Document doc = Jsoup.parse("<html><head></head><!DOCTYPE html><body></body></html>");
        // Error, ignored
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartTagHtml() {
        Document doc = Jsoup.parse("<html><head></head><html><body></body></html>");
        // Process in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartTagBody() {
        Document doc = Jsoup.parse("<html><head></head><body><p>text</p></body></html>");
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartTagFrameset() {
        Document doc = Jsoup.parse("<html><head></head><frameset><frame src='a.html'></frameset></html>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartTagBaseEtc() {
        Document doc = Jsoup.parse("<html><head></head><base href='http://example.com/'><body></body></html>");
        // Error, push head element and process in InHead
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_StartTagHead() {
        Document doc = Jsoup.parse("<html><head></head><head><title>test</title></head><body></body></html>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_EndTagBodyHtml() {
        Document doc = Jsoup.parse("<html><head></head></body><body></body></html>");
        // anythingElse
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_OtherEndTag() {
        Document doc = Jsoup.parse("<html><head></head></div><body></body></html>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHead_AnythingElse() {
        Document doc = Jsoup.parse("<html><head></head><p>text</p></html>");
        // anythingElse processes start tag body and reprocesses
        assertEquals("<p>text</p>", doc.body().html());
    }

    // ==================== InBody (extensive coverage) ====================

    @Test(timeout = 4000)
    public void testInBody_Character() {
        Document doc = Jsoup.parse("<body>text</body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_Comment() {
        Document doc = Jsoup.parse("<body><!-- comment --></body>");
        assertEquals("<!-- comment -->", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testInBody_Doctype() {
        Document doc = Jsoup.parse("<body><!DOCTYPE html></body>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagA() {
        Document doc = Jsoup.parse("<body><a href='http://example.com'>link</a></body>");
        assertEquals("link", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagAWithExisting() {
        // If an 'a' element is already active formatting, it should be closed
        Document doc = Jsoup.parse("<body><a href='1'>first</a><a href='2'>second</a></body>");
        assertEquals("firstsecond", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagEmptyFormatters() {
        Document doc = Jsoup.parse("<body><br><img src='a.png'><embed src='b.swf'></body>");
        assertNotNull(doc.select("br").first());
        assertNotNull(doc.select("img").first());
        assertNotNull(doc.select("embed").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagPClosers() {
        Document doc = Jsoup.parse("<body><p>first</p><div>second</div></body>");
        assertEquals("firstsecond", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagSpan() {
        Document doc = Jsoup.parse("<body><span>text</span></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagLi() {
        Document doc = Jsoup.parse("<body><ul><li>item1</li><li>item2</li></ul></body>");
        assertEquals(2, doc.select("li").size());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagHtml() {
        Document doc = Jsoup.parse("<body><html><p>text</p></body>");
        // Merge attributes onto real html
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagToHead() {
        Document doc = Jsoup.parse("<body><base href='http://example.com/'><p>text</p></body>");
        // Process in InHead
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagBody() {
        Document doc = Jsoup.parse("<body><body><p>text</p></body>");
        // Error, merge attributes
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagFrameset() {
        Document doc = Jsoup.parse("<body><frameset><frame src='a.html'></frameset></body>");
        // Error, if framesetOk false, ignore
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagHeadings() {
        Document doc = Jsoup.parse("<body><h1>heading</h1><h2>sub</h2></body>");
        assertEquals("headingsub", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagPreListing() {
        Document doc = Jsoup.parse("<body><pre>code</pre></body>");
        assertEquals("code", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagForm() {
        Document doc = Jsoup.parse("<body><form action='/submit'><input name='q'></form></body>");
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagFormDuplicate() {
        Document doc = Jsoup.parse("<body><form></form><form></form></body>");
        // Second form is error, return false
        assertEquals(1, doc.select("form").size());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagDdDt() {
        Document doc = Jsoup.parse("<body><dl><dd>def</dd><dt>term</dt></dl></body>");
        assertEquals("defterm", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagPlaintext() {
        Document doc = Jsoup.parse("<body><plaintext>text</plaintext></body>");
        // Plaintext transitions to PLAINTEXT state, rest is treated as data
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagButton() {
        Document doc = Jsoup.parse("<body><button>click</button></body>");
        assertEquals("click", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagButtonInButton() {
        Document doc = Jsoup.parse("<body><button><button>nested</button></button></body>");
        // Inner button closes outer
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagFormatters() {
        Document doc = Jsoup.parse("<body><b>bold</b><i>italic</i></body>");
        assertEquals("bolditalic", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagNobr() {
        Document doc = Jsoup.parse("<body><nobr>text</nobr></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagApplets() {
        Document doc = Jsoup.parse("<body><applet code='a.class'></applet></body>");
        assertNotNull(doc.select("applet").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagTable() {
        Document doc = Jsoup.parse("<body><table><tr><td>cell</td></tr></table></body>");
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagInput() {
        Document doc = Jsoup.parse("<body><input type='text' name='q'></body>");
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagInputHidden() {
        Document doc = Jsoup.parse("<body><input type='hidden' name='id' value='1'></body>");
        // Hidden input does not set framesetOk false
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagMedia() {
        Document doc = Jsoup.parse("<body><param name='a' value='b'><source src='a.mp4'><track src='a.vtt'></body>");
        assertNotNull(doc.select("param").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagHr() {
        Document doc = Jsoup.parse("<body><hr></body>");
        assertNotNull(doc.select("hr").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagImage() {
        Document doc = Jsoup.parse("<body><image src='a.png'></body>");
        // <image> is changed to <img>
        assertNotNull(doc.select("img").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagIsindex() {
        Document doc = Jsoup.parse("<body><isindex action='/search' prompt='search:'></body>");
        // Isindex is processed as form, hr, label, input, etc.
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagTextarea() {
        Document doc = Jsoup.parse("<body><textarea>content</textarea></body>");
        assertEquals("content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagXmp() {
        Document doc = Jsoup.parse("<body><xmp>code</xmp></body>");
        assertEquals("code", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagIframe() {
        Document doc = Jsoup.parse("<body><iframe src='a.html'></iframe></body>");
        assertNotNull(doc.select("iframe").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagNoembed() {
        Document doc = Jsoup.parse("<body><noembed>text</noembed></body>");
        assertNotNull(doc.select("noembed").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagSelect() {
        Document doc = Jsoup.parse("<body><select><option>a</option></select></body>");
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagSelectInTable() {
        Document doc = Jsoup.parse("<body><table><tr><td><select><option>a</option></select></td></tr></table></body>");
        // Select inside table transitions to InSelectInTable
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagOptions() {
        Document doc = Jsoup.parse("<body><select><optgroup><option>a</option></optgroup></select></body>");
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagRuby() {
        Document doc = Jsoup.parse("<body><ruby><rp>(</rp><rt>annotation</rt><rp>)</rp></ruby></body>");
        assertEquals("(annotation)", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagMath() {
        Document doc = Jsoup.parse("<body><math><mi>x</mi></math></body>");
        assertNotNull(doc.select("math").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagSvg() {
        Document doc = Jsoup.parse("<body><svg><circle cx='50' cy='50' r='40'></circle></svg></body>");
        assertNotNull(doc.select("svg").first());
    }

    @Test(timeout = 4000)
    public void testInBody_StartTagDrop() {
        Document doc = Jsoup.parse("<body><caption>text</caption></body>");
        // caption in body is error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagAdoptionFormatters() {
        Document doc = Jsoup.parse("<body><b>bold</b></body>");
        assertEquals("bold", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagClosers() {
        Document doc = Jsoup.parse("<body><div>text</div></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagSpan() {
        Document doc = Jsoup.parse("<body><span>text</span></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagLi() {
        Document doc = Jsoup.parse("<body><ul><li>item</li></ul></body>");
        assertEquals("item", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagBody() {
        Document doc = Jsoup.parse("<body><p>text</p></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagHtml() {
        Document doc = Jsoup.parse("<body><p>text</p></html>");
        // Process end tag body first
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagForm() {
        Document doc = Jsoup.parse("<body><form><input></form></body>");
        // Form end tag removes form element
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagP() {
        Document doc = Jsoup.parse("<body><p>text</p></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagPWithoutOpen() {
        Document doc = Jsoup.parse("<body></p><p>text</p></body>");
        // If no p in scope, create empty p and reprocess
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagDdDt() {
        Document doc = Jsoup.parse("<body><dl><dd>def</dd></dl></body>");
        assertEquals("def", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagHeadings() {
        Document doc = Jsoup.parse("<body><h1>heading</h1></body>");
        assertEquals("heading", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagSarcasm() {
        Document doc = Jsoup.parse("<body><sarcasm>text</sarcasm></body>");
        // Falls through to anyOtherEndTag
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagApplets() {
        Document doc = Jsoup.parse("<body><applet code='a.class'>text</applet></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_EndTagBr() {
        Document doc = Jsoup.parse("<body></br><p>text</p></body>");
        // Error, process start tag br
        assertNotNull(doc.select("br").first());
    }

    @Test(timeout = 4000)
    public void testInBody_AnyOtherEndTag() {
        Document doc = Jsoup.parse("<body><custom>text</custom></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBody_AnyOtherEndTagSpecial() {
        // If a special element is encountered before matching, return false
        Document doc = Jsoup.parse("<body><div><custom>text</custom></div></body>");
        // custom end tag should close custom, not div
        assertEquals("text", doc.body().text());
    }

    // ==================== Text state ====================

    @Test(timeout = 4000)
    public void testText_Character() {
        Document doc = Jsoup.parse("<script>alert('test');</script>");
        assertEquals("alert('test');", doc.select("script").first().data());
    }

    @Test(timeout = 4000)
    public void testText_EOF() {
        Document doc = Jsoup.parse("<script>alert('test');");
        // EOF triggers error, pop, transition to original state
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testText_EndTag() {
        Document doc = Jsoup.parse("<style>body { color: red; }</style>");
        assertEquals("body { color: red; }", doc.select("style").first().data());
    }

    // ==================== InTable ====================

    @Test(timeout = 4000)
    public void testInTable_Character() {
        Document doc = Jsoup.parse("<table><tr><td>text</td></tr></table>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTable_Comment() {
        Document doc = Jsoup.parse("<table><!-- comment --><tr><td>text</td></tr></table>");
        assertEquals("<!-- comment -->", doc.body().html().substring(0, 14));
    }

    @Test(timeout = 4000)
    public void testInTable_Doctype() {
        Document doc = Jsoup.parse("<table><!DOCTYPE html><tr><td>text</td></tr></table>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagCaption() {
        Document doc = Jsoup.parse("<table><caption>caption</caption><tr><td>text</td></tr></table>");
        assertEquals("captiontext", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagColgroup() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup><tr><td>text</td></tr></table>");
        assertNotNull(doc.select("colgroup").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagCol() {
        Document doc = Jsoup.parse("<table><col><tr><td>text</td></tr></table>");
        // Process start tag colgroup first
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagTbodyTfootThead() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>text</td></tr></tbody></table>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagTdThTr() {
        Document doc = Jsoup.parse("<table><td>cell</td></table>");
        // Process start tag tbody first
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagTable() {
        Document doc = Jsoup.parse("<table><table><tr><td>cell</td></tr></table></table>");
        // Error, process end tag table first
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagStyleScript() {
        Document doc = Jsoup.parse("<table><style>td { color: red; }</style><tr><td>text</td></tr></table>");
        // Process in InHead
        assertNotNull(doc.select("style").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagInputHidden() {
        Document doc = Jsoup.parse("<table><input type='hidden' name='id' value='1'><tr><td>text</td></tr></table>");
        // Hidden input is inserted
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagInputNotHidden() {
        Document doc = Jsoup.parse("<table><input type='text' name='q'><tr><td>text</td></tr></table>");
        // Not hidden triggers anythingElse
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTable_StartTagForm() {
        Document doc = Jsoup.parse("<table><form><tr><td>text</td></tr></form></table>");
        // Form is inserted with foster parenting
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testInTable_AnythingElse() {
        Document doc = Jsoup.parse("<table><div>text</div><tr><td>cell</td></tr></table>");
        // AnythingElse processes in InBody with foster inserts
        assertEquals("textcell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTable_EndTagTable() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTable_EndTagInvalid() {
        Document doc = Jsoup.parse("<table></body><tr><td>cell</td></tr></table>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTable_EOF() {
        Document doc = Jsoup.parse("<table><tr><td>cell");
        // EOF stops parsing
        assertNotNull(doc);
    }

    // ==================== InTableText ====================

    @Test(timeout = 4000)
    public void testInTableText_Character() {
        Document doc = Jsoup.parse("<table><tr><td>text</td></tr></table>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableText_NonCharacter() {
        // When a non-character token arrives, pending characters are flushed
        Document doc = Jsoup.parse("<table>  <tr><td>text</td></tr></table>");
        assertEquals("text", doc.body().text());
    }

    // ==================== InCaption ====================

    @Test(timeout = 4000)
    public void testInCaption_EndTagCaption() {
        Document doc = Jsoup.parse("<table><caption>caption</caption><tr><td>text</td></tr></table>");
        assertEquals("captiontext", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInCaption_StartTagTableEtc() {
        Document doc = Jsoup.parse("<table><caption><table><tr><td>cell</td></tr></table></caption></table>");
        // Error, process end tag caption first
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInCaption_EndTagInvalid() {
        Document doc = Jsoup.parse("<table><caption></body></caption><tr><td>text</td></tr></table>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInCaption_AnythingElse() {
        Document doc = Jsoup.parse("<table><caption><p>text</p></caption><tr><td>cell</td></tr></table>");
        assertEquals("textcell", doc.body().text());
    }

    // ==================== InColumnGroup ====================

    @Test(timeout = 4000)
    public void testInColumnGroup_Whitespace() {
        Document doc = Jsoup.parse("<table><colgroup>   <col></colgroup><tr><td>text</td></tr></table>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_Comment() {
        Document doc = Jsoup.parse("<table><colgroup><!-- comment --><col></colgroup><tr><td>text</td></tr></table>");
        assertEquals("<!-- comment -->", doc.body().html().substring(0, 14));
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_Doctype() {
        Document doc = Jsoup.parse("<table><colgroup><!DOCTYPE html><col></colgroup><tr><td>text</td></tr></table>");
        // Error, ignored
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_StartTagHtml() {
        Document doc = Jsoup.parse("<table><colgroup><html><col></colgroup><tr><td>text</td></tr></table>");
        // Process in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_StartTagCol() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup><tr><td>text</td></tr></table>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_AnythingElse() {
        Document doc = Jsoup.parse("<table><colgroup><div>text</div></colgroup><tr><td>cell</td></tr></table>");
        // AnythingElse processes end tag colgroup and reprocesses
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_EndTagColgroup() {
        Document doc = Jsoup.parse("<table><colgroup><col></colgroup><tr><td>text</td></tr></table>");
        assertNotNull(doc.select("colgroup").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroup_EOF() {
        Document doc = Jsoup.parse("<table><colgroup><col>");
        // EOF stops parsing
        assertNotNull(doc);
    }

    // ==================== InTableBody ====================

    @Test(timeout = 4000)
    public void testInTableBody_StartTagTr() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>cell</td></tr></tbody></table>");
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_StartTagThTd() {
        Document doc = Jsoup.parse("<table><tbody><th>header</th></tbody></table>");
        // Process start tag tr first
        assertEquals("header", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_StartTagCaptionEtc() {
        Document doc = Jsoup.parse("<table><tbody><caption>cap</caption><tr><td>cell</td></tr></tbody></table>");
        // exitTableBody
        assertEquals("capcell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_EndTagTbodyTfootThead() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>cell</td></tr></tbody></table>");
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_EndTagTable() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>cell</td></tr></tbody></table>");
        // exitTableBody
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInTableBody_EndTagInvalid() {
        Document doc = Jsoup.parse("<table><tbody></body><tr><td>cell</td></tr></tbody></table>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInTableBody_AnythingElse() {
        Document doc = Jsoup.parse("<table><tbody><div>text</div><tr><td>cell</td></tr></tbody></table>");
        // Process in InTable
        assertEquals("textcell", doc.body().text());
    }

    // ==================== InRow ====================

    @Test(timeout = 4000)
    public void testInRow_StartTagThTd() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInRow_StartTagCaptionEtc() {
        Document doc = Jsoup.parse("<table><tr><caption>cap</caption><td>cell</td></tr></table>");
        // handleMissingTr
        assertEquals("capcell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInRow_EndTagTr() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInRow_EndTagTable() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        // handleMissingTr
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInRow_EndTagTbodyTfootThead() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        // Process end tag tr first
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInRow_EndTagInvalid() {
        Document doc = Jsoup.parse("<table><tr></body><td>cell</td></tr></table>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInRow_AnythingElse() {
        Document doc = Jsoup.parse("<table><tr><div>text</div><td>cell</td></tr></table>");
        // Process in InTable
        assertEquals("textcell", doc.body().text());
    }

    // ==================== InCell ====================

    @Test(timeout = 4000)
    public void testInCell_EndTagTdTh() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInCell_EndTagInvalid() {
        Document doc = Jsoup.parse("<table><tr><td></body></td></tr></table>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInCell_EndTagTableTbodyTfootTheadTr() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>");
        // closeCell and reprocess
        assertEquals("cell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInCell_StartTagCaptionEtc() {
        Document doc = Jsoup.parse("<table><tr><td><caption>cap</caption></td></tr></table>");
        // closeCell and reprocess
        assertEquals("cap", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInCell_AnythingElse() {
        Document doc = Jsoup.parse("<table><tr><td><p>text</p></td></tr></table>");
        // Process in InBody
        assertEquals("text", doc.body().text());
    }

    // ==================== InSelect ====================

    @Test(timeout = 4000)
    public void testInSelect_Character() {
        Document doc = Jsoup.parse("<select><option>a</option></select>");
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelect_Comment() {
        Document doc = Jsoup.parse("<select><!-- comment --><option>a</option></select>");
        assertEquals("<!-- comment -->", doc.body().html().substring(0, 14));
    }

    @Test(timeout = 4000)
    public void testInSelect_Doctype() {
        Document doc = Jsoup.parse("<select><!DOCTYPE html><option>a</option></select>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInSelect_StartTagHtml() {
        Document doc = Jsoup.parse("<select><html><option>a</option></select>");
        // Process in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInSelect_StartTagOption() {
        Document doc = Jsoup.parse("<select><option>a</option><option>b</option></select>");
        assertEquals("ab", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartTagOptgroup() {
        Document doc = Jsoup.parse("<select><optgroup><option>a</option></optgroup></select>");
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartTagSelect() {
        Document doc = Jsoup.parse("<select><select><option>a</option></select></select>");
        // Error, process end tag select
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelect_StartTagInputKeygenTextarea() {
        Document doc = Jsoup.parse("<select><input type='text' name='q'></select>");
        // Error, process end tag select and reprocess
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInSelect_StartTagScript() {
        Document doc = Jsoup.parse("<select><script>alert('test');</script></select>");
        // Process in InHead
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testInSelect_AnythingElse() {
        Document doc = Jsoup.parse("<select><div>text</div></select>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInSelect_EndTagOptgroup() {
        Document doc = Jsoup.parse("<select><optgroup><option>a</option></optgroup></select>");
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelect_EndTagOption() {
        Document doc = Jsoup.parse("<select><option>a</option></select>");
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelect_EndTagSelect() {
        Document doc = Jsoup.parse("<select><option>a</option></select>");
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelect_EOF() {
        Document doc = Jsoup.parse("<select><option>a");
        // EOF stops parsing
        assertNotNull(doc);
    }

    // ==================== InSelectInTable ====================

    @Test(timeout = 4000)
    public void testInSelectInTable_StartTagTableEtc() {
        Document doc = Jsoup.parse("<table><tr><td><select><option>a</option></select></td></tr></table>");
        // Start tag table etc. triggers process end tag select and reprocess
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelectInTable_EndTagTableEtc() {
        Document doc = Jsoup.parse("<table><tr><td><select><option>a</option></select></td></tr></table>");
        // End tag table etc. triggers process end tag select and reprocess
        assertEquals("a", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInSelectInTable_AnythingElse() {
        Document doc = Jsoup.parse("<table><tr><td><select><option>a</option></select></td></tr></table>");
        // Process in InSelect
        assertEquals("a", doc.body().text());
    }

    // ==================== AfterBody ====================

    @Test(timeout = 4000)
    public void testAfterBody_Whitespace() {
        Document doc = Jsoup.parse("<body><p>text</p></body>   ");
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterBody_Comment() {
        Document doc = Jsoup.parse("<body><p>text</p></body><!-- comment -->");
        assertEquals("<!-- comment -->", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterBody_Doctype() {
        Document doc = Jsoup.parse("<body><p>text</p></body><!DOCTYPE html>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterBody_StartTagHtml() {
        Document doc = Jsoup.parse("<body><p>text</p></body><html>");
        // Process in InBody
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterBody_EndTagHtml() {
        Document doc = Jsoup.parse("<body><p>text</p></body></html>");
        // Transition to AfterAfterBody
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterBody_EOF() {
        Document doc = Jsoup.parse("<body><p>text</p></body>");
        // EOF stops parsing
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterBody_OtherToken() {
        Document doc = Jsoup.parse("<body><p>text</p></body><div>extra</div>");
        // Error, transition to InBody and reprocess
        assertEquals("<p>text</p><div>extra</div>", doc.body().html());
    }

    // ==================== InFrameset ====================

    @Test(timeout = 4000)
    public void testInFrameset_Whitespace() {
        Document doc = Jsoup.parse("<frameset>   <frame src='a.html'></frameset>");
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_Comment() {
        Document doc = Jsoup.parse("<frameset><!-- comment --><frame src='a.html'></frameset>");
        assertEquals("<!-- comment -->", doc.body().html().substring(0, 14));
    }

    @Test(timeout = 4000)
    public void testInFrameset_Doctype() {
        Document doc = Jsoup.parse("<frameset><!DOCTYPE html><frame src='a.html'></frameset>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartTagHtml() {
        Document doc = Jsoup.parse("<frameset><html><frame src='a.html'></frameset>");
        // Process in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartTagFrameset() {
        Document doc = Jsoup.parse("<frameset><frameset><frame src='a.html'></frameset></frameset>");
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartTagFrame() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset>");
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_StartTagNoframes() {
        Document doc = Jsoup.parse("<frameset><noframes><body>text</body></noframes></frameset>");
        // Process in InHead
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_OtherStartTag() {
        Document doc = Jsoup.parse("<frameset><div>text</div></frameset>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInFrameset_EndTagFrameset() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset>");
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFrameset_EOF() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'>");
        // EOF stops parsing
        assertNotNull(doc);
    }

    // ==================== AfterFrameset ====================

    @Test(timeout = 4000)
    public void testAfterFrameset_Whitespace() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset>   ");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_Comment() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset><!-- comment -->");
        assertEquals("<!-- comment -->", doc.body().html().substring(0, 14));
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_Doctype() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset><!DOCTYPE html>");
        // Error, return false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_StartTagHtml() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset><html>");
        // Process in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_EndTagHtml() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset></html>");
        // Transition to AfterAfterFrameset
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_StartTagNoframes() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset><noframes><body>text</body></noframes>");
        // Process in InHead
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_EOF() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset>");
        // EOF stops parsing
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFrameset_OtherToken() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset><div>text</div>");
        // Error, return false
        assertNotNull(doc);
    }

    // ==================== AfterAfterBody ====================

    @Test(timeout = 4000)
    public void testAfterAfterBody_Comment() {
        Document doc = Jsoup.parse("<html><body><p>text</p></body></html><!-- comment -->");
        assertEquals("<!-- comment -->", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody_DoctypeWhitespaceStartTagHtml() {
        Document doc = Jsoup.parse("<html><body><p>text</p></body></html>   ");
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody_EOF() {
        Document doc = Jsoup.parse("<html><body><p>text</p></body></html>");
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBody_OtherToken() {
        Document doc = Jsoup.parse("<html><body><p>text</p></body></html><div>extra</div>");
        // Error, transition to InBody and reprocess
        assertEquals("<p>text</p><div>extra</div>", doc.body().html());
    }

    // ==================== AfterAfterFrameset ====================

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_Comment() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset></html><!-- comment -->");
        assertEquals("<!-- comment -->", doc.body().html().substring(0, 14));
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_DoctypeWhitespaceStartTagHtml() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset></html>   ");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_EOF() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset></html>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_StartTagNoframes() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset></html><noframes><body>text</body></noframes>");
        // Process in InHead
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFrameset_OtherToken() {
        Document doc = Jsoup.parse("<frameset><frame src='a.html'></frameset></html><div>text</div>");
        // Error, return false
        assertNotNull(doc);
    }

    // ==================== ForeignContent ====================

    @Test(timeout = 4000)
    public void testForeignContent() {
        // ForeignContent state is not fully implemented; just ensure no exception
        Document doc = Jsoup.parse("<svg><foreignObject><body><p>text</p></body></foreignObject></svg>");
        assertNotNull(doc);
    }

    // ==================== Partition C: Defect-Targeted Test ====================

    @Test(timeout = 4000)
    public void testCaseSensitiveParseTree() {
        // This test targets the known defect: case-sensitive tag handling causing incorrect nesting.
        // Expected: <r> <X> A </X> <y> B </y> </r>
        // Bug: <r> <X> A <y> B </y> </X> </r>
        String input = "<r><X>A<y>B</y></X></r>";
        Document doc = Jsoup.parse(input);
        String output = doc.body().html();
        // The correct output should have <X> and <y> as siblings under <r>
        // The buggy version nests <y> inside <X>
        // We assert that the output matches the expected structure.
        // Note: jsoup normalizes tag names to lowercase, so we compare lowercased.
        String expected = "<r><x>a</x><y>b</y></r>";
        assertEquals("Case-sensitive nesting defect", expected, output);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testNullCharacterInBody() {
        // Null character should cause error and return false
        Document doc = Jsoup.parse("<body>\u0000</body>");
        // Should not crash
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testFosterParenting() {
        // Foster parenting when inserting into table
        Document doc = Jsoup.parse("<table><div>text</div><tr><td>cell</td></tr></table>");
        // The div should be foster-parented before the table
        assertEquals("textcell", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testFragmentParsing() {
        // Fragment parsing (e.g., body fragment) should handle missing html/body
        Document doc = Jsoup.parseBodyFragment("<p>text</p>");
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testDeepStack() {
        // Deep nesting to exercise stack limits
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("<div>");
        }
        sb.append("text");
        for (int i = 0; i < 100; i++) {
            sb.append("</div>");
        }
        Document doc = Jsoup.parse(sb.toString());
        assertEquals("text", doc.body().text());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testStateTransitions() {
        // Ensure that after parsing, the tree builder state is correct (should be AfterAfterBody or similar)
        Document doc = Jsoup.parse("<html><body><p>text</p></body></html>");
        // No direct access to state, but we can check that the document is complete
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testFormattingElements() {
        // Test that active formatting elements are managed correctly
        Document doc = Jsoup.parse("<body><b><i>text</i></b></body>");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testStackManagement() {
        // Test that stack is properly managed for special elements
        Document doc = Jsoup.parse("<body><div><p>text</p></div></body>");
        assertEquals("text", doc.body().text());
    }
}