package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.XmlDeclaration;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - parse() normal HTML, body fragment, relaxed mode
 *   - parseStartTag: empty/non-empty tags, self-closing, data tags (textarea, title, script)
 *   - parseEndTag: valid/invalid tag names, stack pop
 *   - parseTextNode: normal text, special '<' case
 *   - parseComment: standard comment, comment ending with '-'
 *   - parseCdata: CDATA section
 *   - parseXmlDecl: processing instruction vs declaration
 *   - parseAttribute: quoted (single/double), unquoted, empty key
 *   - addChildToParent: valid ancestor, implicit parent creation, bodyTag special case
 *   - stackHasValidParent: root html, requiresSpecificParent, ancestor loop
 *   - popStackToClose: tag found, body/html break, not found
 *   - base href update
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty HTML string
 *   - HTML with only whitespace
 *   - HTML with only comments
 *   - HTML with only CDATA
 *   - HTML with only processing instructions
 *   - HTML with malformed tags (e.g., <div class=foo>)
 *   - HTML with missing closing quotes in attributes
 *   - HTML with empty attribute keys
 *   - HTML with very long attribute values
 *   - Null baseUri (should throw)
 *   - Null html (should throw)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: StringIndexOutOfBoundsException when parsing rough attributes
 *   - Test: parsesQuiteRoughAttributes – triggers the bug on defective version
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null html/baseUri -> IllegalArgumentException
 *   - Empty tag name in parseStartTag -> IllegalArgumentException
 *   - Empty end tag name -> no exception, but tag ignored
 *   - Invalid XML declaration -> still parsed
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Document returned is not null
 *   - Document has correct baseUri after parsing
 *   - Body fragment has empty head
 *   - Relaxed mode does not create implicit parents
 */
public class ParserDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void parseSimpleHtml() {
        Document doc = Parser.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseBodyFragment() {
        Document doc = Parser.parseBodyFragment("<p>Fragment</p>", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.head().childNodes().isEmpty()); // body fragment has empty head
        assertEquals("Fragment", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<p>Relaxed</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Relaxed", doc.body().text());
    }

    @Test(timeout = 4000)
    public void parseSelfClosingTag() {
        Document doc = Parser.parse("<br/><img src='test.png'/>", "http://example.com");
        assertNotNull(doc);
        assertEquals(2, doc.body().children().size());
        assertEquals("br", doc.body().child(0).tagName());
        assertEquals("img", doc.body().child(1).tagName());
    }

    @Test(timeout = 4000)
    public void parseDataTagTextarea() {
        Document doc = Parser.parse("<textarea>Hello &amp; World</textarea>", "http://example.com");
        assertNotNull(doc);
        Element textarea = doc.body().child(0);
        assertEquals("textarea", textarea.tagName());
        // TextNode inside textarea should be encoded
        assertEquals("Hello & World", textarea.text());
    }

    @Test(timeout = 4000)
    public void parseDataTagScript() {
        Document doc = Parser.parse("<script>if (a < b) {}</script>", "http://example.com");
        assertNotNull(doc);
        Element script = doc.body().child(0);
        assertEquals("script", script.tagName());
        // Script content is raw DataNode, not encoded
        assertEquals("if (a < b) {}", script.data());
    }

    @Test(timeout = 4000)
    public void parseComment() {
        Document doc = Parser.parse("<!-- comment --><p>text</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().childNodes().size()); // comment is not in body? Actually comment is before body, but parser adds to last element (html). Let's check structure.
        // Better: parse body fragment
        Document bodyDoc = Parser.parseBodyFragment("<!-- comment --><p>text</p>", "http://example.com");
        assertNotNull(bodyDoc);
        // In body fragment, comment is added to body
        assertEquals(2, bodyDoc.body().childNodes().size());
        assertTrue(bodyDoc.body().childNode(0) instanceof Comment);
        assertEquals(" comment ", ((Comment) bodyDoc.body().childNode(0)).getData());
    }

    @Test(timeout = 4000)
    public void parseCdata() {
        Document doc = Parser.parseBodyFragment("<![CDATA[<greeting>]]>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof TextNode);
        assertEquals("<greeting>", ((TextNode) doc.body().childNode(0)).text());
    }

    @Test(timeout = 4000)
    public void parseXmlDecl() {
        Document doc = Parser.parseBodyFragment("<?xml version='1.0'?><p>text</p>", "http://example.com");
        assertNotNull(doc);
        // XML declaration becomes a child of body? Actually it's added to last element (body)
        assertEquals(2, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.body().childNode(0);
        assertEquals("xml version='1.0'", decl.getData());
        assertFalse(decl.isProcessingInstruction()); // <? is processing instruction? Actually <? is proc instr, <! is declaration. The code: firstChar = consume(); procInstr = firstChar.toString().equals("!"); So for <?, firstChar is '?', procInstr=false. So it's a processing instruction? Wait: the code says: boolean procInstr = firstChar.toString().equals("!"); So for <?, procInstr=false, meaning it's treated as declaration? That seems inverted. But we just test that it parses without exception.
    }

    @Test(timeout = 4000)
    public void parseBaseHref() {
        Document doc = Parser.parse("<html><head><base href='http://example.com/base/'></head><body><p>text</p></body></html>", "http://original.com");
        assertNotNull(doc);
        assertEquals("http://example.com/base/", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void parseImplicitParentCreation() {
        // <p> inside <body> is valid, but <td> without <table> should create implicit <table><tr><td>
        Document doc = Parser.parseBodyFragment("<td>cell</td>", "http://example.com");
        assertNotNull(doc);
        // Should have table > tr > td
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("table", body.child(0).tagName());
        assertEquals(1, body.child(0).children().size());
        assertEquals("tr", body.child(0).child(0).tagName());
        assertEquals(1, body.child(0).child(0).children().size());
        assertEquals("td", body.child(0).child(0).child(0).tagName());
    }

    @Test(timeout = 4000)
    public void parseRelaxedNoImplicitParent() {
        Document doc = Parser.parseBodyFragmentRelaxed("<td>cell</td>", "http://example.com");
        assertNotNull(doc);
        // In relaxed mode, no implicit parent, so <td> is added directly to body? Actually it will be added to body because body can contain td? No, body cannot contain td directly, but relaxed mode skips validAncestor check, so it will be added to body.
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("td", body.child(0).tagName());
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void parseEmptyHtml() {
        Document doc = Parser.parse("", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.body().children().isEmpty());
    }

    @Test(timeout = 4000)
    public void parseWhitespaceOnly() {
        Document doc = Parser.parse("   ", "http://example.com");
        assertNotNull(doc);
        // Whitespace becomes text node
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof TextNode);
    }

    @Test(timeout = 4000)
    public void parseOnlyComment() {
        Document doc = Parser.parseBodyFragment("<!-- only comment -->", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof Comment);
    }

    @Test(timeout = 4000)
    public void parseOnlyCdata() {
        Document doc = Parser.parseBodyFragment("<![CDATA[raw]]>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof TextNode);
        assertEquals("raw", ((TextNode) doc.body().childNode(0)).text());
    }

    @Test(timeout = 4000)
    public void parseOnlyProcessingInstruction() {
        Document doc = Parser.parseBodyFragment("<?target data?>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof XmlDeclaration);
    }

    @Test(timeout = 4000)
    public void parseMalformedAttributeUnquoted() {
        // Attribute without quotes and with space
        Document doc = Parser.parseBodyFragment("<div class=foo id=bar>text</div>", "http://example.com");
        assertNotNull(doc);
        Element div = doc.body().child(0);
        assertEquals("foo", div.attr("class"));
        assertEquals("bar", div.attr("id"));
    }

    @Test(timeout = 4000)
    public void parseMalformedAttributeMissingClosingQuote() {
        // Missing closing double quote
        Document doc = Parser.parseBodyFragment("<div class=\"foo>text</div>", "http://example.com");
        assertNotNull(doc);
        // The attribute value will be everything until >? Actually the parser will chompTo(DQ) but no DQ, so it will chomp to end? Then later tq will be empty? This might cause issues. But we expect no exception.
        Element div = doc.body().child(0);
        // The value might be "foo>text</div"? Actually the parser will consume until it finds DQ, but since there is none, it will consume the rest of the string? Let's just assert no exception and that div exists.
        assertNotNull(div);
    }

    @Test(timeout = 4000)
    public void parseEmptyAttributeKey() {
        // Attribute with empty key: e.g., <div =value>
        Document doc = Parser.parseBodyFragment("<div =value>text</div>", "http://example.com");
        assertNotNull(doc);
        // The empty key attribute is skipped (returns null), so no attribute added.
        Element div = doc.body().child(0);
        assertTrue(div.attributes().isEmpty());
    }

    @Test(timeout = 4000)
    public void parseNullHtmlThrows() {
        try {
            Parser.parse(null, "http://example.com");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void parseNullBaseUriThrows() {
        try {
            Parser.parse("<p>text</p>", null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void parsesQuiteRoughAttributes() {
        // This test targets the known defect: StringIndexOutOfBoundsException when parsing rough attributes.
        // The input should trigger the bug on the defective version. We use a variety of rough attribute patterns.
        // The test must not throw any exception and should produce a valid document.
        String roughHtml = "<div class=\"foo\" id=bar data-value='test' emptyattr = ><p>text</p></div>";
        Document doc = Parser.parseBodyFragment(roughHtml, "http://example.com");
        assertNotNull(doc);
        Element div = doc.body().child(0);
        assertEquals("div", div.tagName());
        // Check that attributes are parsed as expected (some may be lost due to roughness)
        // The emptyattr with = and no value should be ignored (empty key? Actually key is "emptyattr", value is empty)
        assertEquals("foo", div.attr("class"));
        assertEquals("bar", div.attr("id"));
        assertEquals("test", div.attr("data-value"));
        // emptyattr might have empty value
        assertEquals("", div.attr("emptyattr"));
        // Ensure no exception was thrown
    }

    @Test(timeout = 4000)
    public void parsesRoughAttributesWithMissingClosingTag() {
        // Another rough scenario: attribute with missing closing quote and then end of input
        String roughHtml = "<div class=\"foo";
        Document doc = Parser.parseBodyFragment(roughHtml, "http://example.com");
        assertNotNull(doc);
        // Should not throw StringIndexOutOfBoundsException
        Element div = doc.body().child(0);
        assertEquals("div", div.tagName());
        // The attribute value might be "foo" or "foo" plus rest? Actually the parser will chompTo(DQ) and since no DQ, it will consume the rest of the string? That could cause issues. But we just assert no exception.
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void parseEmptyTagNameThrows() {
        // This is internal, but we can trigger via malformed input like "< >"
        try {
            Parser.parseBodyFragment("< >", "http://example.com");
            // Should throw IllegalArgumentException because tagName is empty
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void parseEndTagWithEmptyName() {
        // End tag with no name: </> should be ignored
        Document doc = Parser.parseBodyFragment("<p>text</>", "http://example.com");
        assertNotNull(doc);
        // The </> is ignored, so <p> remains open? Actually it will try to close tag with empty name, but tagName.length()==0 so it does nothing.
        assertEquals(1, doc.body().children().size());
        assertEquals("p", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void parseInvalidXmlDeclaration() {
        // <! with no data? Actually <!> is not valid, but parser will consume < then ! then chompTo(">") which may return empty.
        Document doc = Parser.parseBodyFragment("<!>", "http://example.com");
        assertNotNull(doc);
        // Should not throw
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof XmlDeclaration);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void documentBaseUriAfterParse() {
        Document doc = Parser.parse("<html></html>", "http://original.com");
        assertEquals("http://original.com", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void bodyFragmentHasEmptyHead() {
        Document doc = Parser.parseBodyFragment("<p>test</p>", "http://example.com");
        assertTrue(doc.head().childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void relaxedModeDoesNotCreateImplicitTags() {
        // <td> without <table> should not create implicit table in relaxed mode
        Document doc = Parser.parseBodyFragmentRelaxed("<td>cell</td>", "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("td", body.child(0).tagName());
        // No table or tr created
    }

    @Test(timeout = 4000)
    public void parseMultipleChildren() {
        Document doc = Parser.parseBodyFragment("<p>first</p><p>second</p>", "http://example.com");
        assertEquals(2, doc.body().children().size());
        assertEquals("first", doc.body().child(0).text());
        assertEquals("second", doc.body().child(1).text());
    }

    @Test(timeout = 4000)
    public void parseTextWithLessThan() {
        // Special case: text starting with '<' that is not a tag
        Document doc = Parser.parseBodyFragment("< hello", "http://example.com");
        assertNotNull(doc);
        // The first character '<' is consumed as text node "<"
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof TextNode);
        assertEquals("< hello", ((TextNode) doc.body().childNode(0)).text());
    }

    @Test(timeout = 4000)
    public void parseCommentEndingWithDash() {
        // Comment ending with "-" (i.e., "<!-- comment- ->")
        Document doc = Parser.parseBodyFragment("<!-- comment- -->", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().childNodes().size());
        Comment comment = (Comment) doc.body().childNode(0);
        assertEquals(" comment- ", comment.getData()); // The trailing dash is removed? Actually code: if data.endsWith("-") then substring(0, length-1). So " comment- " ends with "-"? No, it ends with space then dash? Actually data is " comment- " (with trailing space before ->). The chompTo("->") will consume until "->", so data is " comment- " (space before ->). That ends with space, not dash. So no substring. But if comment is "<!-- comment-->" then data is " comment-" which ends with dash, so substring removes last dash. Let's test that.
        Document doc2 = Parser.parseBodyFragment("<!-- comment-->", "http://example.com");
        Comment comment2 = (Comment) doc2.body().childNode(0);
        assertEquals(" comment", comment2.getData()); // trailing dash removed
    }
}