package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for HtmlTreeBuilderState.
 * Targets line/branch coverage and the known defect where <style> and <noframes>
 * incorrectly treat subsequent tags as raw text.
 *
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core state transitions (Initial, BeforeHtml, BeforeHead, InHead, InBody, Text, InTable, etc.)
 * - Partition B: Boundary values (nullString, empty strings, whitespace-only tokens, EOF)
 * - Partition C: Defect-targeted: <style> and <noframes> followed by <meta> should not escape the meta tag.
 * - Partition D: Error paths (invalid end tags, doctype in wrong states, etc.)
 * - Partition E: Not applicable (enum, no equals/hashCode)
 */
public class HtmlTreeBuilderStateDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testInitialStateWhitespace() {
        // Whitespace in Initial state should be ignored
        Document doc = Jsoup.parse("   <html></html>");
        assertEquals("<html></html>", doc.html().trim());
    }

    @Test(timeout = 4000)
    public void testInitialStateComment() {
        Document doc = Jsoup.parse("<!-- comment --><html></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testInitialStateDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html></html>");
        assertEquals("<!DOCTYPE html>", doc.childNode(0).toString());
    }

    @Test(timeout = 4000)
    public void testInitialStateOtherTokenTransitionsToBeforeHtml() {
        // Any non-whitespace, non-comment, non-doctype token triggers BeforeHtml
        Document doc = Jsoup.parse("<html></html>");
        assertEquals("<html></html>", doc.html().trim());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlDoctype() {
        // Doctype in BeforeHtml should error and return false
        Document doc = Jsoup.parse("<!DOCTYPE html><html></html>");
        // Should still parse, but doctype may be ignored? Actually doctype before html is allowed in initial, but here it's after initial? We'll just check no crash.
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlComment() {
        Document doc = Jsoup.parse("<!-- comment --><html></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlWhitespace() {
        Document doc = Jsoup.parse("   <html></html>");
        assertEquals("<html></html>", doc.html().trim());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlStartTagHtml() {
        Document doc = Jsoup.parse("<html></html>");
        assertEquals("<html></html>", doc.html().trim());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlEndTagHeadBodyHtmlBr() {
        // End tags like </head> before head should trigger anythingElse
        Document doc = Jsoup.parse("</head><html></html>");
        // Should create html element
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlEndTagOther() {
        // Other end tags should error and return false
        Document doc = Jsoup.parse("</div><html></html>");
        // Should still produce html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlAnythingElse() {
        // Anything else (e.g., start tag not html) triggers anythingElse
        Document doc = Jsoup.parse("<div></div>");
        // Should create html and then process div in body
        assertEquals("<html><head></head><body><div></div></body></html>", doc.html().trim());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadWhitespace() {
        Document doc = Jsoup.parse("<html>   <head></head></html>");
        assertNotNull(doc.select("head").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadComment() {
        Document doc = Jsoup.parse("<html><!-- comment --><head></head></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testBeforeHeadDoctype() {
        Document doc = Jsoup.parse("<html><!DOCTYPE html><head></head></html>");
        // Should error but continue
        assertNotNull(doc.select("head").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadStartTagHtml() {
        // Start tag html in BeforeHead should be processed by InBody
        Document doc = Jsoup.parse("<html><html></html></html>");
        // Should not create duplicate html
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadStartTagHead() {
        Document doc = Jsoup.parse("<html><head></head></html>");
        assertNotNull(doc.select("head").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadEndTagHeadBodyHtmlBr() {
        // End tags like </head> before head should process start tag head
        Document doc = Jsoup.parse("<html></head><body></body></html>");
        assertNotNull(doc.select("head").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadEndTagOther() {
        Document doc = Jsoup.parse("<html></div><body></body></html>");
        // Should error but continue
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadAnythingElse() {
        Document doc = Jsoup.parse("<html><div></div></html>");
        // Should create head and then process div in body
        assertNotNull(doc.select("head").first());
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInHeadWhitespace() {
        Document doc = Jsoup.parse("<html><head>   </head></html>");
        // Whitespace in head should be inserted as character
        assertTrue(doc.select("head").html().contains("   "));
    }

    @Test(timeout = 4000)
    public void testInHeadComment() {
        Document doc = Jsoup.parse("<html><head><!-- comment --></head></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testInHeadDoctype() {
        Document doc = Jsoup.parse("<html><head><!DOCTYPE html></head></html>");
        // Should error but continue
        assertNotNull(doc.select("head").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagHtml() {
        Document doc = Jsoup.parse("<html><head><html></html></head></html>");
        // Should be processed by InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagBase() {
        Document doc = Jsoup.parse("<html><head><base href='http://example.com'></head></html>");
        assertNotNull(doc.select("base").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagMeta() {
        Document doc = Jsoup.parse("<html><head><meta name='foo'></head></html>");
        assertNotNull(doc.select("meta").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagTitle() {
        Document doc = Jsoup.parse("<html><head><title>Test</title></head></html>");
        assertEquals("Test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagStyle() {
        Document doc = Jsoup.parse("<html><head><style></style></head></html>");
        assertNotNull(doc.select("style").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagScript() {
        Document doc = Jsoup.parse("<html><head><script>alert(1)</script></head></html>");
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagNoscript() {
        Document doc = Jsoup.parse("<html><head><noscript></noscript></head></html>");
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagHead() {
        // Duplicate head start tag should error and return false
        Document doc = Jsoup.parse("<html><head><head></head></html>");
        // Should still have one head
        assertEquals(1, doc.select("head").size());
    }

    @Test(timeout = 4000)
    public void testInHeadEndTagHead() {
        Document doc = Jsoup.parse("<html><head></head></html>");
        assertNotNull(doc.select("head").first());
    }

    @Test(timeout = 4000)
    public void testInHeadEndTagBodyHtmlBr() {
        // End tags like </body> in head should trigger anythingElse
        Document doc = Jsoup.parse("<html><head></body></head></html>");
        // Should close head and process body
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testInHeadEndTagOther() {
        Document doc = Jsoup.parse("<html><head></div></head></html>");
        // Should error but continue
        assertNotNull(doc.select("head").first());
    }

    @Test(timeout = 4000)
    public void testInHeadAnythingElse() {
        Document doc = Jsoup.parse("<html><head><div></div></head></html>");
        // Should close head and process div in body
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptDoctype() {
        Document doc = Jsoup.parse("<html><head><noscript><!DOCTYPE html></noscript></head></html>");
        // Should error but continue
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptStartTagHtml() {
        Document doc = Jsoup.parse("<html><head><noscript><html></html></noscript></head></html>");
        // Should process in InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptEndTagNoscript() {
        Document doc = Jsoup.parse("<html><head><noscript></noscript></head></html>");
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptWhitespaceCommentOrValidStartTags() {
        Document doc = Jsoup.parse("<html><head><noscript>   <!-- comment --><base></noscript></head></html>");
        // Should process in InHead
        assertNotNull(doc.select("base").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptEndTagBr() {
        Document doc = Jsoup.parse("<html><head><noscript></br></noscript></head></html>");
        // Should trigger anythingElse
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptInvalidStartOrEndTag() {
        Document doc = Jsoup.parse("<html><head><noscript><head></head></noscript></head></html>");
        // Should error and return false
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadWhitespace() {
        Document doc = Jsoup.parse("<html><head></head>   <body></body></html>");
        // Whitespace after head should be inserted as character
        assertTrue(doc.html().contains("   "));
    }

    @Test(timeout = 4000)
    public void testAfterHeadComment() {
        Document doc = Jsoup.parse("<html><head></head><!-- comment --><body></body></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testAfterHeadDoctype() {
        Document doc = Jsoup.parse("<html><head></head><!DOCTYPE html><body></body></html>");
        // Should error but continue
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagHtml() {
        Document doc = Jsoup.parse("<html><head></head><html></html></html>");
        // Should process in InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagBody() {
        Document doc = Jsoup.parse("<html><head></head><body></body></html>");
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagFrameset() {
        Document doc = Jsoup.parse("<html><head></head><frameset></frameset></html>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagBaseEtc() {
        Document doc = Jsoup.parse("<html><head></head><base href='http://example.com'><body></body></html>");
        // Should error, push head, process, then remove head
        assertNotNull(doc.select("base").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagHead() {
        Document doc = Jsoup.parse("<html><head></head><head></head><body></body></html>");
        // Should error and return false
        assertEquals(1, doc.select("head").size());
    }

    @Test(timeout = 4000)
    public void testAfterHeadEndTagBodyHtml() {
        Document doc = Jsoup.parse("<html><head></head></body></html>");
        // Should trigger anythingElse (creates body)
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadEndTagOther() {
        Document doc = Jsoup.parse("<html><head></head></div></html>");
        // Should error and return false
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadAnythingElse() {
        Document doc = Jsoup.parse("<html><head></head><div></div></html>");
        // Should create body and process div
        assertNotNull(doc.select("body").first());
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInBodyCharacter() {
        Document doc = Jsoup.parse("<html><body>Hello</body></html>");
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testInBodyNullString() {
        // Null character should error and return false
        Document doc = Jsoup.parse("<html><body>\u0000</body></html>");
        // Should not crash, null character may be ignored
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testInBodyComment() {
        Document doc = Jsoup.parse("<html><body><!-- comment --></body></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testInBodyDoctype() {
        Document doc = Jsoup.parse("<html><body><!DOCTYPE html></body></html>");
        // Should error and return false
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagA() {
        Document doc = Jsoup.parse("<html><body><a href='x'>link</a></body></html>");
        assertNotNull(doc.select("a").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagEmptyFormatters() {
        Document doc = Jsoup.parse("<html><body><br></body></html>");
        assertNotNull(doc.select("br").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagPClosers() {
        Document doc = Jsoup.parse("<html><body><p>para</p></body></html>");
        assertNotNull(doc.select("p").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagSpan() {
        Document doc = Jsoup.parse("<html><body><span>text</span></body></html>");
        assertNotNull(doc.select("span").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagLi() {
        Document doc = Jsoup.parse("<html><body><ul><li>item</li></ul></body></html>");
        assertNotNull(doc.select("li").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagHtml() {
        Document doc = Jsoup.parse("<html><body><html></html></body></html>");
        // Should merge attributes onto real html
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagBody() {
        Document doc = Jsoup.parse("<html><body><body></body></html>");
        // Should error and merge attributes
        assertEquals(1, doc.select("body").size());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagFrameset() {
        Document doc = Jsoup.parse("<html><body><frameset></frameset></body></html>");
        // Should error and possibly ignore
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagHeading() {
        Document doc = Jsoup.parse("<html><body><h1>heading</h1></body></html>");
        assertNotNull(doc.select("h1").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagPreListing() {
        Document doc = Jsoup.parse("<html><body><pre>text</pre></body></html>");
        assertNotNull(doc.select("pre").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagForm() {
        Document doc = Jsoup.parse("<html><body><form></form></body></html>");
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagDdDt() {
        Document doc = Jsoup.parse("<html><body><dl><dt>term</dt><dd>def</dd></dl></body></html>");
        assertNotNull(doc.select("dt").first());
        assertNotNull(doc.select("dd").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagPlaintext() {
        Document doc = Jsoup.parse("<html><body><plaintext>text</plaintext></body></html>");
        // Plaintext should switch tokeniser state
        assertNotNull(doc.select("plaintext").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagButton() {
        Document doc = Jsoup.parse("<html><body><button>click</button></body></html>");
        assertNotNull(doc.select("button").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagFormatter() {
        Document doc = Jsoup.parse("<html><body><b>bold</b></body></html>");
        assertNotNull(doc.select("b").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagNobr() {
        Document doc = Jsoup.parse("<html><body><nobr>text</nobr></body></html>");
        assertNotNull(doc.select("nobr").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagApplet() {
        Document doc = Jsoup.parse("<html><body><applet></applet></body></html>");
        assertNotNull(doc.select("applet").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagTable() {
        Document doc = Jsoup.parse("<html><body><table></table></body></html>");
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagInput() {
        Document doc = Jsoup.parse("<html><body><input type='text'></body></html>");
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagMedia() {
        Document doc = Jsoup.parse("<html><body><param name='foo'></body></html>");
        assertNotNull(doc.select("param").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagHr() {
        Document doc = Jsoup.parse("<html><body><hr></body></html>");
        assertNotNull(doc.select("hr").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagImage() {
        Document doc = Jsoup.parse("<html><body><image src='x'></body></html>");
        // Should be converted to img
        assertNotNull(doc.select("img").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagIsindex() {
        Document doc = Jsoup.parse("<html><body><isindex action='/search'></body></html>");
        // Should process form, hr, label, input, etc.
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagTextarea() {
        Document doc = Jsoup.parse("<html><body><textarea>text</textarea></body></html>");
        assertNotNull(doc.select("textarea").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagXmp() {
        Document doc = Jsoup.parse("<html><body><xmp>text</xmp></body></html>");
        assertNotNull(doc.select("xmp").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagIframe() {
        Document doc = Jsoup.parse("<html><body><iframe></iframe></body></html>");
        assertNotNull(doc.select("iframe").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagNoembed() {
        Document doc = Jsoup.parse("<html><body><noembed></noembed></body></html>");
        assertNotNull(doc.select("noembed").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagSelect() {
        Document doc = Jsoup.parse("<html><body><select><option>1</option></select></body></html>");
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagOptions() {
        Document doc = Jsoup.parse("<html><body><select><option>1</option></select></body></html>");
        assertNotNull(doc.select("option").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagRuby() {
        Document doc = Jsoup.parse("<html><body><ruby><rp>(</rp><rt>text</rt><rp>)</rp></ruby></body></html>");
        assertNotNull(doc.select("ruby").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagMath() {
        Document doc = Jsoup.parse("<html><body><math></math></body></html>");
        assertNotNull(doc.select("math").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagSvg() {
        Document doc = Jsoup.parse("<html><body><svg></svg></body></html>");
        assertNotNull(doc.select("svg").first());
    }

    @Test(timeout = 4000)
    public void testInBodyStartTagDrop() {
        // Tags like caption, col, etc. should error and return false
        Document doc = Jsoup.parse("<html><body><caption></caption></body></html>");
        // Should be ignored
        assertNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagAdoptionFormatters() {
        Document doc = Jsoup.parse("<html><body><b>bold</b></body></html>");
        assertNotNull(doc.select("b").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagClosers() {
        Document doc = Jsoup.parse("<html><body><div></div></body></html>");
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagSpan() {
        Document doc = Jsoup.parse("<html><body><span>text</span></body></html>");
        assertNotNull(doc.select("span").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagLi() {
        Document doc = Jsoup.parse("<html><body><ul><li>item</li></ul></body></html>");
        assertNotNull(doc.select("li").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagBody() {
        Document doc = Jsoup.parse("<html><body></body></html>");
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagHtml() {
        Document doc = Jsoup.parse("<html></html>");
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagForm() {
        Document doc = Jsoup.parse("<html><body><form></form></body></html>");
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagP() {
        Document doc = Jsoup.parse("<html><body><p>para</p></body></html>");
        assertNotNull(doc.select("p").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagDdDt() {
        Document doc = Jsoup.parse("<html><body><dl><dt>term</dt><dd>def</dd></dl></body></html>");
        assertNotNull(doc.select("dt").first());
        assertNotNull(doc.select("dd").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagHeadings() {
        Document doc = Jsoup.parse("<html><body><h1>heading</h1></body></html>");
        assertNotNull(doc.select("h1").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagSarcasm() {
        Document doc = Jsoup.parse("<html><body><sarcasm>text</sarcasm></body></html>");
        // Should fall through to anyOtherEndTag
        assertNotNull(doc.select("sarcasm").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagApplet() {
        Document doc = Jsoup.parse("<html><body><applet></applet></body></html>");
        assertNotNull(doc.select("applet").first());
    }

    @Test(timeout = 4000)
    public void testInBodyEndTagBr() {
        Document doc = Jsoup.parse("<html><body></br></body></html>");
        // Should error and process start tag br
        assertNotNull(doc.select("br").first());
    }

    @Test(timeout = 4000)
    public void testInBodyAnyOtherEndTag() {
        Document doc = Jsoup.parse("<html><body><custom></custom></body></html>");
        assertNotNull(doc.select("custom").first());
    }

    @Test(timeout = 4000)
    public void testTextCharacter() {
        Document doc = Jsoup.parse("<html><head><script>alert(1)</script></head></html>");
        assertTrue(doc.html().contains("alert(1)"));
    }

    @Test(timeout = 4000)
    public void testTextEOF() {
        // EOF in text state should pop and transition to original state
        Document doc = Jsoup.parse("<html><head><script>alert(1)");
        // Should still produce something
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testTextEndTag() {
        Document doc = Jsoup.parse("<html><head><script></script></head></html>");
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testInTableCharacter() {
        Document doc = Jsoup.parse("<html><body><table>text</table></body></html>");
        // Should go to InTableText
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInTableComment() {
        Document doc = Jsoup.parse("<html><body><table><!-- comment --></table></body></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testInTableDoctype() {
        Document doc = Jsoup.parse("<html><body><table><!DOCTYPE html></table></body></html>");
        // Should error and return false
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagCaption() {
        Document doc = Jsoup.parse("<html><body><table><caption></caption></table></body></html>");
        assertNotNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagColgroup() {
        Document doc = Jsoup.parse("<html><body><table><colgroup></colgroup></table></body></html>");
        assertNotNull(doc.select("colgroup").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagCol() {
        Document doc = Jsoup.parse("<html><body><table><col></table></body></html>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagTbodyTfootThead() {
        Document doc = Jsoup.parse("<html><body><table><tbody></tbody></table></body></html>");
        assertNotNull(doc.select("tbody").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagTdThTr() {
        Document doc = Jsoup.parse("<html><body><table><tr><td></td></tr></table></body></html>");
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagTable() {
        Document doc = Jsoup.parse("<html><body><table><table></table></table></body></html>");
        // Should error and process end tag table
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagStyleScript() {
        Document doc = Jsoup.parse("<html><body><table><style></style></table></body></html>");
        assertNotNull(doc.select("style").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagInputHidden() {
        Document doc = Jsoup.parse("<html><body><table><input type='hidden'></table></body></html>");
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagInputNotHidden() {
        Document doc = Jsoup.parse("<html><body><table><input type='text'></table></body></html>");
        // Should go to anythingElse
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInTableStartTagForm() {
        Document doc = Jsoup.parse("<html><body><table><form></form></table></body></html>");
        // Should error and insert form without fostering
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testInTableEndTagTable() {
        Document doc = Jsoup.parse("<html><body><table></table></body></html>");
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInTableEndTagInvalid() {
        Document doc = Jsoup.parse("<html><body><table></caption></table></body></html>");
        // Should error and return false
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInTableAnythingElse() {
        Document doc = Jsoup.parse("<html><body><table><div></div></table></body></html>");
        // Should foster parent the div
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInTableTextCharacter() {
        Document doc = Jsoup.parse("<html><body><table>text</table></body></html>");
        // Should collect characters and then process
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testInTableTextNonWhitespace() {
        Document doc = Jsoup.parse("<html><body><table>text</table></body></html>");
        // Non-whitespace should trigger error and foster insert
        assertTrue(doc.body().text().contains("text"));
    }

    @Test(timeout = 4000)
    public void testInCaptionEndTagCaption() {
        Document doc = Jsoup.parse("<html><body><table><caption></caption></table></body></html>");
        assertNotNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testInCaptionStartTagTableRelated() {
        Document doc = Jsoup.parse("<html><body><table><caption><table></table></caption></table></body></html>");
        // Should error, close caption, then process table
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInCaptionEndTagInvalid() {
        Document doc = Jsoup.parse("<html><body><table><caption></body></caption></table></body></html>");
        // Should error and return false
        assertNotNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroupWhitespace() {
        Document doc = Jsoup.parse("<html><body><table><colgroup>   </colgroup></table></body></html>");
        assertTrue(doc.html().contains("   "));
    }

    @Test(timeout = 4000)
    public void testInColumnGroupComment() {
        Document doc = Jsoup.parse("<html><body><table><colgroup><!-- comment --></colgroup></table></body></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testInColumnGroupDoctype() {
        Document doc = Jsoup.parse("<html><body><table><colgroup><!DOCTYPE html></colgroup></table></body></html>");
        // Should error but continue
        assertNotNull(doc.select("colgroup").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroupStartTagHtml() {
        Document doc = Jsoup.parse("<html><body><table><colgroup><html></html></colgroup></table></body></html>");
        // Should process in InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testInColumnGroupStartTagCol() {
        Document doc = Jsoup.parse("<html><body><table><colgroup><col></colgroup></table></body></html>");
        assertNotNull(doc.select("col").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroupEndTagColgroup() {
        Document doc = Jsoup.parse("<html><body><table><colgroup></colgroup></table></body></html>");
        assertNotNull(doc.select("colgroup").first());
    }

    @Test(timeout = 4000)
    public void testInColumnGroupEOF() {
        Document doc = Jsoup.parse("<html><body><table><colgroup>");
        // Should stop parsing
        assertNotNull(doc.select("colgroup").first());
    }

    @Test(timeout = 4000)
    public void testInTableBodyStartTagTr() {
        Document doc = Jsoup.parse("<html><body><table><tbody><tr></tr></tbody></table></body></html>");
        assertNotNull(doc.select("tr").first());
    }

    @Test(timeout = 4000)
    public void testInTableBodyStartTagThTd() {
        Document doc = Jsoup.parse("<html><body><table><tbody><td></td></tbody></table></body></html>");
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testInTableBodyStartTagTableRelated() {
        Document doc = Jsoup.parse("<html><body><table><tbody><caption></caption></tbody></table></body></html>");
        // Should exit table body
        assertNotNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testInTableBodyEndTagTbodyTfootThead() {
        Document doc = Jsoup.parse("<html><body><table><tbody></tbody></table></body></html>");
        assertNotNull(doc.select("tbody").first());
    }

    @Test(timeout = 4000)
    public void testInTableBodyEndTagTable() {
        Document doc = Jsoup.parse("<html><body><table><tbody></table></body></html>");
        // Should exit table body
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInTableBodyEndTagInvalid() {
        Document doc = Jsoup.parse("<html><body><table><tbody></body></tbody></table></body></html>");
        // Should error and return false
        assertNotNull(doc.select("tbody").first());
    }

    @Test(timeout = 4000)
    public void testInRowStartTagThTd() {
        Document doc = Jsoup.parse("<html><body><table><tr><td></td></tr></table></body></html>");
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testInRowStartTagTableRelated() {
        Document doc = Jsoup.parse("<html><body><table><tr><caption></caption></tr></table></body></html>");
        // Should handle missing tr
        assertNotNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testInRowEndTagTr() {
        Document doc = Jsoup.parse("<html><body><table><tr></tr></table></body></html>");
        assertNotNull(doc.select("tr").first());
    }

    @Test(timeout = 4000)
    public void testInRowEndTagTable() {
        Document doc = Jsoup.parse("<html><body><table><tr></table></body></html>");
        // Should handle missing tr
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInRowEndTagTbodyTfootThead() {
        Document doc = Jsoup.parse("<html><body><table><tr></tbody></table></body></html>");
        // Should process end tag tr then process
        assertNotNull(doc.select("tr").first());
    }

    @Test(timeout = 4000)
    public void testInRowEndTagInvalid() {
        Document doc = Jsoup.parse("<html><body><table><tr></body></tr></table></body></html>");
        // Should error and return false
        assertNotNull(doc.select("tr").first());
    }

    @Test(timeout = 4000)
    public void testInCellEndTagTdTh() {
        Document doc = Jsoup.parse("<html><body><table><tr><td></td></tr></table></body></html>");
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testInCellEndTagInvalid() {
        Document doc = Jsoup.parse("<html><body><table><tr><td></body></td></tr></table></body></html>");
        // Should error and return false
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testInCellEndTagTableRelated() {
        Document doc = Jsoup.parse("<html><body><table><tr><td></table></td></tr></table></body></html>");
        // Should close cell and process
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInCellStartTagTableRelated() {
        Document doc = Jsoup.parse("<html><body><table><tr><td><table></table></td></tr></table></body></html>");
        // Should close cell and process
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInSelectCharacter() {
        Document doc = Jsoup.parse("<html><body><select><option>1</option></select></body></html>");
        assertNotNull(doc.select("option").first());
    }

    @Test(timeout = 4000)
    public void testInSelectNullString() {
        Document doc = Jsoup.parse("<html><body><select>\u0000</select></body></html>");
        // Should error and return false
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInSelectComment() {
        Document doc = Jsoup.parse("<html><body><select><!-- comment --></select></body></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testInSelectDoctype() {
        Document doc = Jsoup.parse("<html><body><select><!DOCTYPE html></select></body></html>");
        // Should error and return false
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInSelectStartTagHtml() {
        Document doc = Jsoup.parse("<html><body><select><html></html></select></body></html>");
        // Should process in InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testInSelectStartTagOption() {
        Document doc = Jsoup.parse("<html><body><select><option>1</option></select></body></html>");
        assertNotNull(doc.select("option").first());
    }

    @Test(timeout = 4000)
    public void testInSelectStartTagOptgroup() {
        Document doc = Jsoup.parse("<html><body><select><optgroup><option>1</option></optgroup></select></body></html>");
        assertNotNull(doc.select("optgroup").first());
    }

    @Test(timeout = 4000)
    public void testInSelectStartTagSelect() {
        Document doc = Jsoup.parse("<html><body><select><select></select></select></body></html>");
        // Should error and process end tag select
        assertEquals(1, doc.select("select").size());
    }

    @Test(timeout = 4000)
    public void testInSelectStartTagInputKeygenTextarea() {
        Document doc = Jsoup.parse("<html><body><select><input type='text'></select></body></html>");
        // Should error, close select, then process input
        assertNotNull(doc.select("input").first());
    }

    @Test(timeout = 4000)
    public void testInSelectStartTagScript() {
        Document doc = Jsoup.parse("<html><body><select><script></script></select></body></html>");
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testInSelectEndTagOptgroup() {
        Document doc = Jsoup.parse("<html><body><select><optgroup></optgroup></select></body></html>");
        assertNotNull(doc.select("optgroup").first());
    }

    @Test(timeout = 4000)
    public void testInSelectEndTagOption() {
        Document doc = Jsoup.parse("<html><body><select><option></option></select></body></html>");
        assertNotNull(doc.select("option").first());
    }

    @Test(timeout = 4000)
    public void testInSelectEndTagSelect() {
        Document doc = Jsoup.parse("<html><body><select></select></body></html>");
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInSelectEOF() {
        Document doc = Jsoup.parse("<html><body><select>");
        // Should stop parsing
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testInSelectAnythingElse() {
        Document doc = Jsoup.parse("<html><body><select><div></div></select></body></html>");
        // Should error and return false
        assertNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInSelectInTableStartTagTableRelated() {
        Document doc = Jsoup.parse("<html><body><table><tr><td><select><table></table></select></td></tr></table></body></html>");
        // Should error, close select, then process table
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testInSelectInTableEndTagTableRelated() {
        Document doc = Jsoup.parse("<html><body><table><tr><td><select></table></select></td></tr></table></body></html>");
        // Should error, close select, then process
        assertEquals(1, doc.select("table").size());
    }

    @Test(timeout = 4000)
    public void testAfterBodyWhitespace() {
        Document doc = Jsoup.parse("<html><body></body>   </html>");
        // Should process in InBody
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testAfterBodyComment() {
        Document doc = Jsoup.parse("<html><body></body><!-- comment --></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testAfterBodyDoctype() {
        Document doc = Jsoup.parse("<html><body></body><!DOCTYPE html></html>");
        // Should error and return false
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterBodyStartTagHtml() {
        Document doc = Jsoup.parse("<html><body></body><html></html></html>");
        // Should process in InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testAfterBodyEndTagHtml() {
        Document doc = Jsoup.parse("<html><body></body></html>");
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterBodyEOF() {
        Document doc = Jsoup.parse("<html><body></body>");
        // Should stop
        assertNotNull(doc.select("body").first());
    }

    @Test(timeout = 4000)
    public void testAfterBodyOther() {
        Document doc = Jsoup.parse("<html><body></body><div></div></html>");
        // Should error, transition to InBody, and process
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetWhitespace() {
        Document doc = Jsoup.parse("<html><frameset>   </frameset></html>");
        assertTrue(doc.html().contains("   "));
    }

    @Test(timeout = 4000)
    public void testInFramesetComment() {
        Document doc = Jsoup.parse("<html><frameset><!-- comment --></frameset></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testInFramesetDoctype() {
        Document doc = Jsoup.parse("<html><frameset><!DOCTYPE html></frameset></html>");
        // Should error and return false
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagHtml() {
        Document doc = Jsoup.parse("<html><frameset><html></html></frameset></html>");
        // Should process in InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagFrameset() {
        Document doc = Jsoup.parse("<html><frameset><frameset></frameset></frameset></html>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagFrame() {
        Document doc = Jsoup.parse("<html><frameset><frame></frameset></html>");
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagNoframes() {
        Document doc = Jsoup.parse("<html><frameset><noframes></noframes></frameset></html>");
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagOther() {
        Document doc = Jsoup.parse("<html><frameset><div></div></frameset></html>");
        // Should error and return false
        assertNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetEndTagFrameset() {
        Document doc = Jsoup.parse("<html><frameset></frameset></html>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetEOF() {
        Document doc = Jsoup.parse("<html><frameset>");
        // Should error but stop
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetWhitespace() {
        Document doc = Jsoup.parse("<html><frameset></frameset>   </html>");
        assertTrue(doc.html().contains("   "));
    }

    @Test(timeout = 4000)
    public void testAfterFramesetComment() {
        Document doc = Jsoup.parse("<html><frameset></frameset><!-- comment --></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testAfterFramesetDoctype() {
        Document doc = Jsoup.parse("<html><frameset></frameset><!DOCTYPE html></html>");
        // Should error and return false
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetStartTagHtml() {
        Document doc = Jsoup.parse("<html><frameset></frameset><html></html></html>");
        // Should process in InBody
        assertEquals(1, doc.select("html").size());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetEndTagHtml() {
        Document doc = Jsoup.parse("<html><frameset></frameset></html>");
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetStartTagNoframes() {
        Document doc = Jsoup.parse("<html><frameset></frameset><noframes></noframes></html>");
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetEOF() {
        Document doc = Jsoup.parse("<html><frameset></frameset>");
        // Should stop
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetOther() {
        Document doc = Jsoup.parse("<html><frameset></frameset><div></div></html>");
        // Should error and return false
        assertNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyComment() {
        Document doc = Jsoup.parse("<!-- comment --><html></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyDoctypeOrWhitespaceOrHtml() {
        Document doc = Jsoup.parse("   <!DOCTYPE html><html></html>");
        // Should process in InBody
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyEOF() {
        Document doc = Jsoup.parse("");
        // Should stop
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyOther() {
        Document doc = Jsoup.parse("<div></div>");
        // Should error, transition to InBody, and process
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetComment() {
        Document doc = Jsoup.parse("<!-- comment --><html><frameset></frameset></html>");
        assertTrue(doc.html().contains("<!-- comment -->"));
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetDoctypeOrWhitespaceOrHtml() {
        Document doc = Jsoup.parse("   <!DOCTYPE html><html><frameset></frameset></html>");
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetEOF() {
        Document doc = Jsoup.parse("<html><frameset></frameset>");
        // Should stop
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetStartTagNoframes() {
        Document doc = Jsoup.parse("<html><frameset></frameset><noframes></noframes></html>");
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetOther() {
        Document doc = Jsoup.parse("<html><frameset></frameset><div></div></html>");
        // Should error and return false
        assertNull(doc.select("div").first());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyInput() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testOnlyWhitespace() {
        Document doc = Jsoup.parse("   ");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testNullCharacterInBody() {
        Document doc = Jsoup.parse("<html><body>\u0000</body></html>");
        // Should not crash
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testVeryLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        Document doc = Jsoup.parse("<html><body>" + sb.toString() + "</body></html>");
        assertEquals(10000, doc.body().text().length());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testHandlesKnownEmptyStyle() {
        // This test targets the known defect: <style> followed by <meta> should not escape the meta tag.
        String html = "<html><head><style></style><meta name=\"foo\"></head><body>One</body></html>";
        Document doc = Jsoup.parse(html);
        String expected = "<html><head><style></style><meta name=\"foo\"></head><body>One</body></html>";
        assertEquals(expected, doc.html().trim());
    }

    @Test(timeout = 4000)
    public void testHandlesKnownEmptyNoFrames() {
        // Similar defect with <noframes>
        String html = "<html><head><noframes></noframes><meta name=\"foo\"></head><body>One</body></html>";
        Document doc = Jsoup.parse(html);
        String expected = "<html><head><noframes></noframes><meta name=\"foo\"></head><body>One</body></html>";
        assertEquals(expected, doc.html().trim());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testMalformedHtml() {
        // Should not throw exceptions
        Document doc = Jsoup.parse("<html><head><body><div>");
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testUnclosedTags() {
        Document doc = Jsoup.parse("<html><body><p>text");
        assertNotNull(doc.select("p").first());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedTags() {
        StringBuilder sb = new StringBuilder("<html><body>");
        for (int i = 0; i < 100; i++) {
            sb.append("<div>");
        }
        sb.append("text");
        for (int i = 0; i < 100; i++) {
            sb.append("</div>");
        }
        sb.append("</body></html>");
        Document doc = Jsoup.parse(sb.toString());
        assertNotNull(doc.select("div").first());
    }
}