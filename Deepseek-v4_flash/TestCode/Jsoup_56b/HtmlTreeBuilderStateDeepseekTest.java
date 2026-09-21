package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for HtmlTreeBuilderState.
 * Targets line/branch coverage and the known defect in DocumentType output
 * (missing "SYSTEM" keyword when system identifier present without public identifier).
 */
public class HtmlTreeBuilderStateDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Defect: When parsing a DOCTYPE with system identifier but no public identifier,
     * the output should include "SYSTEM" before the system identifier.
     * The bug is in DocumentType generation (Initial state) or its toString().
     * 
     * Coverage targets:
     * - All states: Initial, BeforeHtml, BeforeHead, InHead, InHeadNoscript, AfterHead,
     *   InBody, Text, InTable, InTableText, InCaption, InColumnGroup, InTableBody,
     *   InRow, InCell, InSelect, InSelectInTable, AfterBody, InFrameset, AfterFrameset,
     *   AfterAfterBody, AfterAfterFrameset, ForeignContent
     * - Each state's process() method: all branches (token types, tag names, conditions)
     * - Helper methods: isWhitespace, handleRcData, handleRawtext, anythingElse variants
     * - Constants arrays: ensure all tag name lookups are exercised
     * - Edge cases: nullString, EOF, fragment parsing, foster parenting, formatting elements
     */

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testInitialStateDoctypeWithSystemId() {
        // Defect-targeted: DOCTYPE with system identifier only
        String html = "<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        // Expected: "<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">"
        assertEquals("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">", doctype.outerHtml());
    }

    @Test(timeout = 4000)
    public void testInitialStateDoctypeWithPublicAndSystem() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertTrue(doctype.outerHtml().contains("PUBLIC"));
        assertTrue(doctype.outerHtml().contains("SYSTEM"));
    }

    @Test(timeout = 4000)
    public void testInitialStateDoctypeWithPublicOnly() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertTrue(doctype.outerHtml().contains("PUBLIC"));
        assertFalse(doctype.outerHtml().contains("SYSTEM"));
    }

    @Test(timeout = 4000)
    public void testInitialStateDoctypeWithForceQuirks() {
        String html = "<!DOCTYPE html SYSTEM \"http://example.com/dtd\" [<!ATTLIST>]>";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertTrue(doctype.outerHtml().contains("SYSTEM"));
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    @Test(timeout = 4000)
    public void testInitialStateWhitespaceIgnored() {
        String html = "   <html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("html", doc.childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testInitialStateComment() {
        String html = "<!-- comment --><html></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.childNodes().size()); // comment is inserted before html
        // Actually comment becomes child of document
        assertEquals("#comment", doc.childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlDoctype() {
        String html = "<!DOCTYPE html><html></html>";
        Document doc = Jsoup.parse(html);
        // Should not error, but doctype is processed in Initial, then BeforeHtml
        assertNotNull(doc.childNode(0));
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlComment() {
        String html = "<!-- comment --><html></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlWhitespace() {
        String html = "   <html></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("html", doc.childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlStartTagHtml() {
        String html = "<html></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("html", doc.childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlEndTagBody() {
        String html = "</body><html></html>";
        Document doc = Jsoup.parse(html);
        // should trigger anythingElse which inserts html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHtmlEndTagOther() {
        String html = "</p><html></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false, but parsing continues? Actually returns false, but parser may continue
        // We just check no exception
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHeadWhitespace() {
        String html = "<html>   <head></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadComment() {
        String html = "<html><!-- comment --><head></head></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.head().previousSibling().nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadDoctype() {
        String html = "<html><!DOCTYPE html><head></head></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHeadStartTagHtml() {
        String html = "<html><html><head></head></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadStartTagHead() {
        String html = "<html><head></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadEndTagBody() {
        String html = "<html></body></html>";
        Document doc = Jsoup.parse(html);
        // triggers processStartTag("head") then process(t)
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testBeforeHeadEndTagOther() {
        String html = "<html></p></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testBeforeHeadOther() {
        String html = "<html><div></div></html>";
        Document doc = Jsoup.parse(html);
        // triggers processStartTag("head") then process(t)
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testInHeadWhitespace() {
        String html = "<html><head>   </head></html>";
        Document doc = Jsoup.parse(html);
        // whitespace inserted as character
        assertNotNull(doc.head().textNodes().get(0));
    }

    @Test(timeout = 4000)
    public void testInHeadComment() {
        String html = "<html><head><!-- comment --></head></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.head().childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testInHeadDoctype() {
        String html = "<html><head><!DOCTYPE html></head></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagHtml() {
        String html = "<html><head><html></head></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagBase() {
        String html = "<html><head><base href='http://example.com'></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("base").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagMeta() {
        String html = "<html><head><meta charset='utf-8'></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("meta").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagTitle() {
        String html = "<html><head><title>Test</title></head></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test", doc.title());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagStyle() {
        String html = "<html><head><style>body {}</style></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("style").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagNoscript() {
        String html = "<html><head><noscript></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagScript() {
        String html = "<html><head><script>alert(1)</script></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("script").first());
    }

    @Test(timeout = 4000)
    public void testInHeadStartTagHeadAgain() {
        String html = "<html><head><head></head></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadEndTagHead() {
        String html = "<html><head></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testInHeadEndTagBody() {
        String html = "<html><head></body></html>";
        Document doc = Jsoup.parse(html);
        // triggers anythingElse -> processEndTag("head") then process(t)
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testInHeadEndTagOther() {
        String html = "<html><head></p></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadOther() {
        String html = "<html><head><div></div></head></html>";
        Document doc = Jsoup.parse(html);
        // anythingElse -> processEndTag("head") then process(t)
        assertNotNull(doc.head());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptDoctype() {
        String html = "<html><head><noscript><!DOCTYPE html></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        // error
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptStartTagHtml() {
        String html = "<html><head><noscript><html></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        // processed in InBody
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptEndTagNoscript() {
        String html = "<html><head><noscript></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptWhitespace() {
        String html = "<html><head><noscript>   </noscript></head></html>";
        Document doc = Jsoup.parse(html);
        // whitespace processed in InHead
        assertNotNull(doc.select("noscript").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptComment() {
        String html = "<html><head><noscript><!-- comment --></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.select("noscript").first().childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptStartTagBasefont() {
        String html = "<html><head><noscript><basefont></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("basefont").first());
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptEndTagBr() {
        String html = "<html><head><noscript></br></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        // anythingElse -> error + insert character
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptStartTagHead() {
        String html = "<html><head><noscript><head></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptEndTagOther() {
        String html = "<html><head><noscript></p></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInHeadNoscriptOther() {
        String html = "<html><head><noscript><div></div></noscript></head></html>";
        Document doc = Jsoup.parse(html);
        // anythingElse -> error + insert character
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHeadWhitespace() {
        String html = "<html><head></head>   <body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testAfterHeadComment() {
        String html = "<html><head></head><!-- comment --><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.childNode(1).nodeName());
    }

    @Test(timeout = 4000)
    public void testAfterHeadDoctype() {
        String html = "<html><head></head><!DOCTYPE html><body></body></html>";
        Document doc = Jsoup.parse(html);
        // error
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagHtml() {
        String html = "<html><head></head><html><body></body></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagBody() {
        String html = "<html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagFrameset() {
        String html = "<html><head></head><frameset></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagBase() {
        String html = "<html><head></head><base href='http://example.com'></html>";
        Document doc = Jsoup.parse(html);
        // error, then push head, process in InHead, pop head
        assertNotNull(doc.select("base").first());
    }

    @Test(timeout = 4000)
    public void testAfterHeadStartTagHead() {
        String html = "<html><head></head><head></head></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHeadEndTagBody() {
        String html = "<html><head></head></body></html>";
        Document doc = Jsoup.parse(html);
        // anythingElse -> processStartTag("body") then process(t)
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testAfterHeadEndTagOther() {
        String html = "<html><head></head></p></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterHeadOther() {
        String html = "<html><head></head><div></div></html>";
        Document doc = Jsoup.parse(html);
        // anythingElse -> processStartTag("body") then process(t)
        assertNotNull(doc.body());
        assertNotNull(doc.select("div").first());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testNullStringCharacter() {
        // Character with null character (U+0000) should be error
        String html = "<html><body>\u0000</body></html>";
        Document doc = Jsoup.parse(html);
        // Should not crash, may be ignored or replaced
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    @Test(timeout = 4000)
    public void testOnlyWhitespace() {
        Document doc = Jsoup.parse("   \n\t");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    @Test(timeout = 4000)
    public void testVeryLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append("a");
        String html = "<html><body>" + sb.toString() + "</body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(sb.toString(), doc.body().text());
    }

    @Test(timeout = 4000)
    public void testDeepNesting() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) sb.append("<div>");
        sb.append("text");
        for (int i = 0; i < 500; i++) sb.append("</div>");
        Document doc = Jsoup.parse(sb.toString());
        assertNotNull(doc);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testDoctypeWithSystemIdOnly() {
        // Directly targets the known defect
        String html = "<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        // The bug: missing "SYSTEM" keyword
        assertEquals("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">", doctype.outerHtml());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithSystemIdAndInternalSubset() {
        String html = "<!DOCTYPE html SYSTEM \"http://example.com/dtd\" [<!ENTITY foo \"bar\">]>";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertTrue(doctype.outerHtml().contains("SYSTEM"));
        assertTrue(doctype.outerHtml().contains("[<!ENTITY foo \"bar\">]"));
    }

    @Test(timeout = 4000)
    public void testDoctypeWithPublicIdAndSystemId() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertTrue(doctype.outerHtml().contains("PUBLIC"));
        assertTrue(doctype.outerHtml().contains("SYSTEM"));
    }

    @Test(timeout = 4000)
    public void testDoctypeWithPublicIdOnly() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertTrue(doctype.outerHtml().contains("PUBLIC"));
        assertFalse(doctype.outerHtml().contains("SYSTEM"));
    }

    @Test(timeout = 4000)
    public void testDoctypeWithForceQuirksFlag() {
        String html = "<!DOCTYPE html SYSTEM \"http://example.com/dtd\" [<!ATTLIST>]>";
        Document doc = Jsoup.parse(html);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testMalformedDoctype() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/loose.dtd\" extra>";
        Document doc = Jsoup.parse(html);
        // Should not throw, may parse as best effort
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testUnclosedTag() {
        String html = "<html><body><p>text";
        Document doc = Jsoup.parse(html);
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testSelfClosingTagInBody() {
        String html = "<html><body><br/></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("br").first());
    }

    @Test(timeout = 4000)
    public void testFosterParenting() {
        String html = "<table><tr><td>text</td></tr></table>";
        Document doc = Jsoup.parse(html);
        // text should be foster-parented before table
        assertNotNull(doc.select("table").first());
    }

    @Test(timeout = 4000)
    public void testScriptTagContent() {
        String html = "<html><head><script>var x = 1;</script></head></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("var x = 1;", doc.select("script").first().data());
    }

    @Test(timeout = 4000)
    public void testStyleTagContent() {
        String html = "<html><head><style>body { color: red; }</style></head></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("body { color: red; }", doc.select("style").first().data());
    }

    @Test(timeout = 4000)
    public void testTextareaContent() {
        String html = "<html><body><textarea>initial text</textarea></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("initial text", doc.select("textarea").first().text());
    }

    @Test(timeout = 4000)
    public void testXmpTag() {
        String html = "<html><body><xmp>raw text</xmp></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("raw text", doc.select("xmp").first().text());
    }

    @Test(timeout = 4000)
    public void testIframeTag() {
        String html = "<html><body><iframe src='test.html'></iframe></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("iframe").first());
    }

    @Test(timeout = 4000)
    public void testNoembedTag() {
        String html = "<html><body><noembed>fallback</noembed></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noembed").first());
    }

    @Test(timeout = 4000)
    public void testSelectInTable() {
        String html = "<table><tr><td><select><option>1</option></select></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("select").first());
    }

    @Test(timeout = 4000)
    public void testFormInTable() {
        String html = "<table><form><tr><td></td></tr></form></table>";
        Document doc = Jsoup.parse(html);
        // form is foster-parented
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testImageTagConvertedToImg() {
        String html = "<html><body><image src='test.png'></image></body></html>";
        Document doc = Jsoup.parse(html);
        // image is converted to img unless in svg
        assertNotNull(doc.select("img").first());
    }

    @Test(timeout = 4000)
    public void testIsindexTag() {
        String html = "<html><body><isindex action='/search' prompt='Search:'></body></html>";
        Document doc = Jsoup.parse(html);
        // isindex is processed as form, hr, label, input, etc.
        assertNotNull(doc.select("form").first());
    }

    @Test(timeout = 4000)
    public void testButtonInScope() {
        String html = "<html><body><button>Click</button></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("button").first());
    }

    @Test(timeout = 4000)
    public void testNobrInScope() {
        String html = "<html><body><nobr>text</nobr></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("nobr").first());
    }

    @Test(timeout = 4000)
    public void testAppletTag() {
        String html = "<html><body><applet code='Test.class'></applet></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("applet").first());
    }

    @Test(timeout = 4000)
    public void testMarqueeTag() {
        String html = "<html><body><marquee>scroll</marquee></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("marquee").first());
    }

    @Test(timeout = 4000)
    public void testObjectTag() {
        String html = "<html><body><object data='test.swf'></object></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("object").first());
    }

    @Test(timeout = 4000)
    public void testTableCaption() {
        String html = "<table><caption>Title</caption><tr><td></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("caption").first());
    }

    @Test(timeout = 4000)
    public void testTableColgroup() {
        String html = "<table><colgroup><col></colgroup><tr><td></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("colgroup").first());
    }

    @Test(timeout = 4000)
    public void testTableTbody() {
        String html = "<table><tbody><tr><td></td></tr></tbody></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("tbody").first());
    }

    @Test(timeout = 4000)
    public void testTableThead() {
        String html = "<table><thead><tr><th></th></tr></thead></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("thead").first());
    }

    @Test(timeout = 4000)
    public void testTableTfoot() {
        String html = "<table><tfoot><tr><td></td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("tfoot").first());
    }

    @Test(timeout = 4000)
    public void testTableRow() {
        String html = "<table><tr><td></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("tr").first());
    }

    @Test(timeout = 4000)
    public void testTableCell() {
        String html = "<table><tr><td>cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("td").first());
    }

    @Test(timeout = 4000)
    public void testTableTh() {
        String html = "<table><tr><th>header</th></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("th").first());
    }

    @Test(timeout = 4000)
    public void testFrameset() {
        String html = "<html><frameset><frame src='a.html'></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testNoframes() {
        String html = "<html><frameset><noframes>fallback</noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterBodyWhitespace() {
        String html = "<html><body></body>   </html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterBodyComment() {
        String html = "<html><body></body><!-- comment --></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.childNode(1).nodeName());
    }

    @Test(timeout = 4000)
    public void testAfterBodyDoctype() {
        String html = "<html><body></body><!DOCTYPE html></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterBodyStartTagHtml() {
        String html = "<html><body></body><html></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterBodyEndTagHtml() {
        String html = "<html><body></body></html>";
        Document doc = Jsoup.parse(html);
        // transitions to AfterAfterBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterBodyEOF() {
        String html = "<html><body></body>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterBodyOther() {
        String html = "<html><body></body><div></div></html>";
        Document doc = Jsoup.parse(html);
        // error, transitions to InBody, processes div
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetWhitespace() {
        String html = "<html><frameset>   <frame src='a.html'></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetComment() {
        String html = "<html><frameset><!-- comment --><frame src='a.html'></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.select("frameset").first().childNode(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testInFramesetDoctype() {
        String html = "<html><frameset><!DOCTYPE html></frameset></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagHtml() {
        String html = "<html><frameset><html></frameset></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagFrameset() {
        String html = "<html><frameset><frameset></frameset></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagFrame() {
        String html = "<html><frameset><frame src='a.html'></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frame").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagNoframes() {
        String html = "<html><frameset><noframes>fallback</noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testInFramesetStartTagOther() {
        String html = "<html><frameset><div></div></frameset></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInFramesetEndTagFrameset() {
        String html = "<html><frameset></frameset></html>";
        Document doc = Jsoup.parse(html);
        // transitions to AfterFrameset
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testInFramesetEOF() {
        String html = "<html><frameset>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFramesetWhitespace() {
        String html = "<html><frameset></frameset>   </html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFramesetComment() {
        String html = "<html><frameset></frameset><!-- comment --></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.childNode(1).nodeName());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetDoctype() {
        String html = "<html><frameset></frameset><!DOCTYPE html></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFramesetStartTagHtml() {
        String html = "<html><frameset></frameset><html></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetEndTagHtml() {
        String html = "<html><frameset></frameset></html>";
        Document doc = Jsoup.parse(html);
        // transitions to AfterAfterFrameset
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFramesetStartTagNoframes() {
        String html = "<html><frameset></frameset><noframes>fallback</noframes></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterFramesetEOF() {
        String html = "<html><frameset></frameset>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterFramesetOther() {
        String html = "<html><frameset></frameset><div></div></html>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyComment() {
        String html = "<html><body></body></html><!-- comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.childNode(1).nodeName());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyDoctype() {
        String html = "<html><body></body></html><!DOCTYPE html>";
        Document doc = Jsoup.parse(html);
        // processed in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyWhitespace() {
        String html = "<html><body></body></html>   ";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyStartTagHtml() {
        String html = "<html><body></body></html><html></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyEOF() {
        String html = "<html><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterBodyOther() {
        String html = "<html><body></body></html><div></div>";
        Document doc = Jsoup.parse(html);
        // error, transitions to InBody, processes div
        assertNotNull(doc.select("div").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetComment() {
        String html = "<html><frameset></frameset></html><!-- comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("#comment", doc.childNode(1).nodeName());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetDoctype() {
        String html = "<html><frameset></frameset></html><!DOCTYPE html>";
        Document doc = Jsoup.parse(html);
        // processed in InBody
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetWhitespace() {
        String html = "<html><frameset></frameset></html>   ";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetStartTagHtml() {
        String html = "<html><frameset></frameset></html><html></html>";
        Document doc = Jsoup.parse(html);
        // InBody processes second html
        assertNotNull(doc.select("html").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetEOF() {
        String html = "<html><frameset></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetStartTagNoframes() {
        String html = "<html><frameset></frameset></html><noframes>fallback</noframes>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noframes").first());
    }

    @Test(timeout = 4000)
    public void testAfterAfterFramesetOther() {
        String html = "<html><frameset></frameset></html><div></div>";
        Document doc = Jsoup.parse(html);
        // error, returns false
        assertNotNull(doc);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testDocumentTypeToString() {
        String html = "<!DOCTYPE html>";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertNotNull(doctype.toString());
    }

    @Test(timeout = 4000)
    public void testDocumentTypeEquality() {
        String html1 = "<!DOCTYPE html>";
        String html2 = "<!DOCTYPE html>";
        Document doc1 = Jsoup.parse(html1);
        Document doc2 = Jsoup.parse(html2);
        DocumentType dt1 = (DocumentType) doc1.childNode(0);
        DocumentType dt2 = (DocumentType) doc2.childNode(0);
        // Not necessarily equal due to different base URIs, but should not throw
        assertNotNull(dt1);
        assertNotNull(dt2);
    }

    @Test(timeout = 4000)
    public void testDocumentTypeHashCode() {
        String html = "<!DOCTYPE html>";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        int hash = doctype.hashCode();
        assertTrue(hash != 0);
    }

    @Test(timeout = 4000)
    public void testDocumentTypeClone() {
        String html = "<!DOCTYPE html>";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        DocumentType clone = doctype.clone();
        assertEquals(doctype.outerHtml(), clone.outerHtml());
    }

    @Test(timeout = 4000)
    public void testDocumentTypeOuterHtml() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
        Document doc = Jsoup.parse(html);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        String outer = doctype.outerHtml();
        assertTrue(outer.startsWith("<!DOCTYPE"));
        assertTrue(outer.endsWith(">"));
    }
}