package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.*;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Parser.java
 * 
 * Partitions & Branches:
 * A. Core Parsing Logic:
 *    - parse(): full document, body fragment
 *    - parseComment(): standard comment, comment ending with - (e.g. <!-- -->)
 *    - parseXmlDecl(): <?xml...?>, <!DOCTYPE...>
 *    - parseEndTag(): valid end tag, empty tag name (ignored)
 *    - parseStartTag(): normal, self-closing via />, no tag name (treated as text)
 *    - parseAttribute(): with quotes (single/double), unquoted, missing key
 *    - parseTextNode(): consumeTo("<")
 *    - parseCdata(): CDATA sections
 *    - addChildToParent(): valid parent, implicit parent creation (e.g. <td> without <tr>)
 *    - stackHasValidParent(): depth check, html tag at root
 *    - popStackToSuitableContainer(): matching tag, removing elements that cannot contain
 *    - popStackToClose(): matching tag in stack, stop at body/html, no match
 *    - Data tags (script, textarea): chompTo end tag, create TextNode vs DataNode
 *    - Base tag: update baseUri
 * 
 * B. Boundary Values:
 *    - null/empty html or baseUri -> IllegalArgumentException
 *    - Empty html string -> empty document
 *    - HTML with only whitespace
 *    - Tags with no closing angle (>)
 *    - CDATA with no closing
 *    - Comments with no closing
 *    - Attributes with empty key
 *    - Stack size 1 (html tag)
 * 
 * C. Known Defect (Defects4J Parser handlesTextAfterData):
 *    - Text after script tag should appear after the script element,
 *      not inside it. Bug: aft text is consumed incorrectly.
 * 
 * D. Exception / Defensive Paths:
 *    - Illegal characters in attribute key
 *    - Invalid tag names
 *    - Mismatched quotes
 * 
 * E. Object Lifecycle:
 *    - ensure returned Document is non-null
 *    - document structure: html > head + body
 *    - normalise() is called
 */

public class ParserDeepseekTest {

    // ---------- SECTION A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testParseBasicDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragment() {
        String bodyHtml = "<p>Fragment</p><div>More</div>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        assertNotNull(doc);
        assertEquals(2, doc.body().children().size());
        assertEquals("p", doc.body().child(0).tagName());
        assertEquals("div", doc.body().child(1).tagName());
    }

    @Test(timeout = 4000)
    public void testParseComment() {
        String html = "<!-- comment --><p>text</p>";
        Document doc = Jsoup.parse(html);
        // comment should be child of html (or body? depends)
        // Jsoup normally places comment as child of body (due to tree building)
        // but we just check document is parsed without error
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseCommentEndingWithDash() {
        String html = "<!-- test- --><p>ok</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("ok", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseCdata() {
        String html = "<![CDATA[ raw text ]]><p>after</p>";
        Document doc = Jsoup.parse(html);
        // CDATA becomes text node?
        // Actually Jsoup puts CDATA as DataNode? But here it's parsed as child of body? We'll just check not crash.
        Node firstChild = doc.body().childNode(0);
        assertTrue(firstChild instanceof TextNode || firstChild instanceof DataNode);
    }

    @Test(timeout = 4000)
    public void testParseXmlDecl() {
        String html = "<?xml version=\"1.0\"?><root>content</root>";
        Document doc = Jsoup.parse(html);
        // XML declaration becomes child of document (parser treats as ?...? inside root)
        // but we just check no exception
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testParseEndTag() {
        String html = "<div><span>hello</span></div>";
        Document doc = Jsoup.parse(html);
        assertEquals("hello", doc.body().text());
        assertEquals(1, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testParseSelfClosingTag() {
        String html = "<br/><p>text</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.body().children().size());
        assertEquals("br", doc.body().child(0).tagName());
        assertTrue(doc.body().child(0).tag().isEmpty()); // self-closing
    }

    @Test(timeout = 4000)
    public void testParseUnclosedTagBecomesText() {
        String html = "< div><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        // "<" is not a valid start; treated as text
        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("< div>"));
        assertTrue(bodyText.contains("text"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeWithSingleQuotes() {
        String html = "<div class='myclass'>text</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("myclass", doc.body().child(0).attr("class"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeWithDoubleQuotes() {
        String html = "<div class=\"myclass\">text</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("myclass", doc.body().child(0).attr("class"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeUnquoted() {
        String html = "<div class=myclass>text</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("myclass", doc.body().child(0).attr("class"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeNoValue() {
        String html = "<div disabled>text</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.body().child(0).attr("disabled"));
    }

    @Test(timeout = 4000)
    public void testParseTextNode() {
        String html = "simple text only";
        Document doc = Jsoup.parse(html);
        assertEquals("simple text only", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseDataTagTextarea() {
        String html = "<textarea>content</textarea>";
        Document doc = Jsoup.parse(html);
        // textarea is data tag, content becomes text
        assertEquals("content", doc.body().child(0).text());
        // also check no other child
        assertEquals(1, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testParseDataTagScript() {
        String html = "<script>var x = 1;</script>";
        Document doc = Jsoup.parse(html);
        // script content should be DataNode (raw, not escaped)
        Element script = doc.body().child(0);
        assertEquals("script", script.tagName());
        Node dataNode = script.childNode(0);
        assertTrue(dataNode instanceof DataNode);
        assertEquals("var x = 1;", ((DataNode) dataNode).getWholeData());
    }

    @Test(timeout = 4000)
    public void testParseBaseTagUpdatesUri() {
        String html = "<base href='http://example.com/'><p>link</p>";
        Document doc = Jsoup.parse(html, "http://original.com/");
        assertEquals("http://example.com/", doc.baseUri());
        // Also check <p> link resolves relative to new base?
        // That would be later, but we check base uri is set
    }

    @Test(timeout = 4000)
    public void testImplicitParentCreation() {
        String html = "<td>cell</td>"; // td without tr, table should be implicit
        Document doc = Jsoup.parse(html);
        // body should contain table > tr > td
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element table = body.child(0);
        assertEquals("table", table.tagName());
        Element tr = table.child(0);
        assertEquals("tr", tr.tagName());
        Element td = tr.child(0);
        assertEquals("td", td.tagName());
    }

    @Test(timeout = 4000)
    public void testImplicitParentWithHead() {
        String html = "<body>content</body>"; // body without html will trigger implicit html, head
        Document doc = Jsoup.parse(html);
        // doc should have html, head, body
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testStackPopToClose() {
        String html = "<ul><li>item1</li><li>item2</li></ul>";
        Document doc = Jsoup.parse(html);
        Element ul = doc.body().child(0);
        assertEquals(2, ul.children().size());
    }

    // ---------- SECTION B: Boundary Value Analysis & Extremes ----------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullHtml() {
        Parser.parse(null, "http://base.com/");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullBaseUri() {
        Parser.parse("<p>test</p>", null);
    }

    @Test(timeout = 4000)
    public void testEmptyHtml() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertTrue(doc.body().children().isEmpty());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnly() {
        Document doc = Jsoup.parse("   \n  ");
        assertNotNull(doc);
        assertTrue(doc.body().text().isEmpty());
    }

    @Test(timeout = 4000)
    public void testCommentWithoutClosing() {
        String html = "<!-- not closed <p>text</p>";
        Document doc = Jsoup.parse(html);
        // parser will treat rest as comment until end of input
        assertTrue(doc.body().text().isEmpty());
    }

    @Test(timeout = 4000)
    public void testCdataWithoutClosing() {
        String html = "<![CDATA[ unclosed]] ><p>after</p>";
        Document doc = Jsoup.parse(html);
        // might consume to end, but just ensure no crash
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testMultipleAttributes() {
        String html = "<div id='main' class='content' style='color:red'>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.body().child(0);
        assertEquals("main", div.id());
        assertEquals("content", div.className());
        assertEquals("color:red", div.attr("style"));
    }

    @Test(timeout = 4000)
    public void testAttributeKeyEmptyReturnsNull() {
        // An edge case: if key is empty after whitespace, attribute parsing returns null
        // We need to create a scenario: e.g. <div ='value'> possibly triggers this.
        String html = "<div ='value'>test</div>";
        Document doc = Jsoup.parse(html);
        // The empty key attribute is ignored; parsing should not hang.
        Element div = doc.body().child(0);
        assertEquals("test", div.text());
    }

    // ---------- SECTION C: Defect-Targeted Branch Zone ----------

    @Test(timeout = 4000)
    public void testTextAfterDataTag() {
        // Known defect: handlesTextAfterData
        // HTML: <body>pre <script>inner</script> aft</body>
        // Expected: <body>pre <script>inner</script> aft</body>
        // Bug: aft gets consumed inside script or lost.
        String html = "<body>pre <script>inner</script> aft</body>";
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        // The body should contain three nodes: text "pre ", element <script>, text " aft"
        assertEquals(3, body.childNodesSize());
        assertEquals("pre ", ((TextNode) body.childNode(0)).text());
        assertEquals("script", body.child(0).tagName()); // first child element is script
        assertEquals("inner", body.child(0).text());
        assertEquals(" aft", ((TextNode) body.childNode(2)).text());
        // Also whole body text should be "pre inner aft"
        assertEquals("pre inner aft", body.text());
    }

    @Test(timeout = 4000)
    public void testTextAfterTextarea() {
        String html = "<textarea>content</textarea> after";
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        // Should have textarea and text node " after"
        assertEquals(2, body.childNodesSize());
        assertEquals("textarea", body.child(0).tagName());
        assertEquals(" after", ((TextNode) body.childNode(1)).text());
    }

    // ---------- SECTION D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000)
    public void testIllegalTagName() {
        // Tag name starting with digit? Though Tag.valueOf might handle, but ensure no crash
        String html = "<1div>test</1div>";
        Document doc = Jsoup.parse(html);
        // Should parse as text "test" or similar
        assertNotNull(doc);
    }

    @Test(timeout = 4000)
    public void testUnclosedStartTag() {
        String html = "<p>text";
        Document doc = Jsoup.parse(html);
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testExtraClosingTags() {
        String html = "<div><p>text</p></div></extra>";
        Document doc = Jsoup.parse(html);
        // extra closing ignored; no exception
        assertEquals("text", doc.body().text());
    }

    // ---------- SECTION E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testDocumentNormalised() {
        // Check that the returned document is normalised
        Document doc = Jsoup.parse("<html><head></head><body>text</body></html>");
        assertNotNull(doc);
        // normalised ensures head and body are direct children of html
        assertEquals("html", doc.children().get(0).tagName());
        assertEquals(2, doc.children().get(0).children().size()); // head and body
    }

    @Test(timeout = 4000)
    public void testMultipleParsesIndependent() {
        // Ensure reusing parser class doesn't share state
        Document doc1 = Jsoup.parse("<p>first</p>");
        Document doc2 = Jsoup.parse("<p>second</p>");
        assertEquals("first", doc1.body().text());
        assertEquals("second", doc2.body().text());
    }
}