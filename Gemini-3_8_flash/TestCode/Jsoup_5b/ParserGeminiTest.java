/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Parser
 * Known Defect: ParserTest::parsesQuiteRoughAttributes -> java.lang.StringIndexOutOfBoundsException
 * Branches Targeted:
 * - parse(): matchesStartTag, matchesCS("</"), matchesCS("<!--"), matches("<![CDATA["), matchesCS("<?") || matchesCS("<!"), parseTextNode()
 * - parseComment(): data.endsWith("-") true/false
 * - parseXmlDecl(): firstChar is "!" (DOCTYPE) vs "?" (XML Decl / PI)
 * - parseEndTag(): empty tag name vs valid tag name; stack pop to close
 * - parseStartTag(): empty element vs self-closing ("/>") on known and unknown tags; data tags (script, style) vs title/textarea text nodes; base tag href handling
 * - parseAttribute(): single quote, double quote, unquoted value, empty attribute key (Defect-targeted branch where key.length() == 0)
 * - parseTextNode(): peek() == '<' special case vs consumeTo("<")
 * - addChildToParent(): root element, validAncestor check, implicit parent creation (!relaxed), relaxed mode bypass, bodyTag implicit head creation
 * - stackHasValidParent(): stack.size() == 1 with htmlTag; requiresSpecificParent() check; ancestor loop resolution
 * - popStackToSuitableContainer(): stack pop until canContain() matches or stack emptied
 * - popStackToClose(): elTag equals bodyTag/htmlTag boundary guard; matching tag; tag not found
 * - Boundary Values: null inputs to constructors/factory methods, empty html strings, malformed html syntax, rough attributes
 */

package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardDocumentParse() {
        String html = "<html><head><title>Sample Page</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        assertEquals("Sample Page", doc.title());
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Hello World", p.text());
        assertEquals("http://example.com/", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragment() {
        String html = "<div id='content'><span>Fragment Text</span></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertNotNull(doc);
        assertNotNull(doc.body());
        Element div = doc.getElementById("content");
        assertNotNull(div);
        assertEquals("div", div.tagName());
        Element span = div.select("span").first();
        assertNotNull(span);
        assertEquals("Fragment Text", span.text());
    }

    @Test(timeout = 4000)
    public void testParseComments() {
        // Test standard comment with --> and non-standard with ->
        String html = "<div><!-- Regular Comment --></div><span><!-- Short Comment -></span>";
        Document doc = Parser.parse(html, "http://example.com/");

        List<Node> divNodes = doc.select("div").first().childNodes();
        assertTrue(divNodes.get(0) instanceof Comment);
        Comment c1 = (Comment) divNodes.get(0);
        assertEquals(" Regular Comment ", c1.getData());

        List<Node> spanNodes = doc.select("span").first().childNodes();
        assertTrue(spanNodes.get(0) instanceof Comment);
        Comment c2 = (Comment) spanNodes.get(0);
        assertEquals(" Short Comment ", c2.getData());
    }

    @Test(timeout = 4000)
    public void testParseXmlDeclarationAndDocType() {
        String html = "<?xml version=\"1.0\" encoding=\"utf-8\"?><!DOCTYPE html><html><body>Content</body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        boolean foundXmlDecl = false;
        boolean foundDocType = false;

        for (Node child : doc.childNodes()) {
            if (child instanceof XmlDeclaration) {
                XmlDeclaration decl = (XmlDeclaration) child;
                if (!decl.name().startsWith("!")) {
                    foundXmlDecl = true;
                    assertFalse(decl.toString().contains("<!"));
                } else {
                    foundDocType = true;
                }
            }
        }
        assertTrue(foundXmlDecl || foundDocType);
    }

    @Test(timeout = 4000)
    public void testParseCdata() {
        String html = "<div><![CDATA[Some <raw> & unescaped data]]></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals(1, div.childNodes().size());
        Node child = div.childNode(0);
        assertTrue(child instanceof TextNode);
        assertEquals("Some <raw> & unescaped data", ((TextNode) child).getWholeText());
    }

    @Test(timeout = 4000)
    public void testDataTagsScriptAndStyle() {
        String html = "<script>var x = 1 < 2 ? 'a' : 'b';</script><style>body > p { color: red; }</style>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals(1, script.childNodes().size());
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = 1 < 2 ? 'a' : 'b';", ((DataNode) script.childNode(0)).getWholeData());

        Element style = doc.select("style").first();
        assertNotNull(style);
        assertTrue(style.childNode(0) instanceof DataNode);
        assertEquals("body > p { color: red; }", ((DataNode) style.childNode(0)).getWholeData());
    }

    @Test(timeout = 4000)
    public void testDataTagsTitleAndTextarea() {
        String html = "<title>Escaped &amp; Title</title><textarea>Hello &amp; &lt;World&gt;</textarea>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element title = doc.select("title").first();
        assertNotNull(title);
        assertTrue(title.childNode(0) instanceof TextNode);
        assertEquals("Escaped & Title", title.text());

        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertTrue(textarea.childNode(0) instanceof TextNode);
        assertEquals("Hello & <World>", textarea.text());
    }

    @Test(timeout = 4000)
    public void testBaseTagHrefUpdate() {
        String html = "<html><head><base href=\"http://other.org/path/\"></head><body><a href=\"sub\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://initial.com/");

        assertEquals("http://other.org/path/", doc.baseUri());
        Element link = doc.select("a").first();
        assertNotNull(link);
        assertEquals("http://other.org/path/sub", link.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testBaseTagWithoutHref() {
        String html = "<html><head><base target=\"_blank\"></head><body><a href=\"sub\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://initial.com/");

        assertEquals("http://initial.com/", doc.baseUri());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceHtml() {
        Document docEmpty = Parser.parse("", "http://example.com/");
        assertNotNull(docEmpty);
        assertEquals("", docEmpty.body().html());

        Document docWhitespace = Parser.parse("   \n\t  ", "http://example.com/");
        assertNotNull(docWhitespace);
        assertEquals("", docWhitespace.body().html());
    }

    @Test(timeout = 4000)
    public void testAttributeQuotesAndUnquoted() {
        String html = "<p single='one' double=\"two\" unquoted=three boolean></p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("one", p.attr("single"));
        assertEquals("two", p.attr("double"));
        assertEquals("three", p.attr("unquoted"));
        assertTrue(p.hasAttr("boolean"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingTagsKnownAndUnknown() {
        String html = "<div><img src='foo.jpg' /><custom-tag id='custom' /></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element img = doc.select("img").first();
        assertNotNull(img);
        assertEquals("foo.jpg", img.attr("src"));

        Element custom = doc.select("custom-tag").first();
        assertNotNull(custom);
        assertEquals("custom", custom.attr("id"));
        assertTrue(custom.tag().isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testUnclosedTagsAndNesting() {
        String html = "<p>Paragraph 1 <div>Container <p>Paragraph 2";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc);
        Elements divs = doc.select("div");
        assertEquals(1, divs.size());
    }

    @Test(timeout = 4000)
    public void testEmptyEndTag() {
        String html = "<p>Text</></p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertTrue(p.text().contains("Text"));
    }

    @Test(timeout = 4000)
    public void testClosingTagsNotInStackOrPastBody() {
        // Closing div that never opened, and attempt to close body/html early
        String html = "</div></body></html><p>Outside</p>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc.body());
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Outside", p.text());
    }

    @Test(timeout = 4000)
    public void testTextNodesWithLessThanChar() {
        String html = "<p>5 < 10 and 10 > 5</p><p>< notatag <p>";
        Document doc = Parser.parse(html, "http://example.com/");

        Elements ps = doc.select("p");
        assertTrue(ps.get(0).text().contains("5 < 10"));
        assertTrue(ps.get(1).text().contains("< notatag"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * TARGETS KNOWN DEFECT:
     * org.jsoup.parser.ParserTest::parsesQuiteRoughAttributes
     * --> java.lang.StringIndexOutOfBoundsException: String index out of range: 14
     * This defect occurs when parsing ill-formed attribute sequences where an
     * attribute key is missing or begins with illegal characters (e.g. '=a', '<', etc.).
     */
    @Test(timeout = 4000)
    public void testParsesQuiteRoughAttributesDefect() {
        String html = "<p =a>one<a <p> something</p> else";
        Document doc = Parser.parse(html, "http://foo.com");

        assertNotNull(doc);
        assertNotNull(doc.body());
        assertTrue(doc.text().contains("one"));
        assertTrue(doc.text().contains("something"));
        assertTrue(doc.text().contains("else"));
    }

    @Test(timeout = 4000)
    public void testRoughAttributesAdditionalVariations() {
        String html1 = "<a id=1 href='/foo' noval=foo =empty val=foo = <p>test";
        Document doc1 = Parser.parse(html1, "http://foo.com");
        assertNotNull(doc1);
        assertEquals(1, doc1.select("a").size());

        String html2 = "<div =foo =><span noval= =bar>text</span></div>";
        Document doc2 = Parser.parse(html2, "http://foo.com");
        assertNotNull(doc2);
        assertEquals("text", doc2.select("span").first().text());

        String html3 = "<a id=1 href='/foo' =bar class = foo />";
        Document doc3 = Parser.parse(html3, "http://foo.com");
        assertNotNull(doc3);
        Element a = doc3.select("a").first();
        assertNotNull(a);
        assertEquals("/foo", a.attr("href"));
        assertEquals("foo", a.attr("class"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullHtml() {
        Parser.parse(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullBaseUri() {
        Parser.parse("<div></div>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentNullHtml() {
        Parser.parseBodyFragment(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentNullBaseUri() {
        Parser.parseBodyFragment("<div></div>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentRelaxedNullHtml() {
        Parser.parseBodyFragmentRelaxed(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentRelaxedNullBaseUri() {
        Parser.parseBodyFragmentRelaxed("<div></div>", null);
    }

    // =========================================================================
    // Partition E: Relaxed Parsing vs Strict Implicit Parents
    // =========================================================================

    @Test(timeout = 4000)
    public void testImplicitParentCreationStandardParse() {
        // Table components without <table> parent should trigger implicit parent creation
        String html = "<tr><td>Data</td></tr>";
        Document doc = Parser.parse(html, "http://example.com");

        Element table = doc.select("table").first();
        assertNotNull(table);
        Element td = doc.select("td").first();
        assertNotNull(td);
        assertEquals("Data", td.text());
    }

    @Test(timeout = 4000)
    public void testImplicitHeadCreationForBodyTag() {
        String html = "<body>Hello World</body>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Hello World", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentRelaxedBehavior() {
        // In relaxed mode, missing implicit parents are not automatically wrapped
        String fragment = "<tr><td>Cell</td></tr>";
        Document docStrict = Parser.parseBodyFragment(fragment, "http://example.com");
        Document docRelaxed = Parser.parseBodyFragmentRelaxed(fragment, "http://example.com");

        assertNotNull(docStrict);
        assertNotNull(docRelaxed);
        assertEquals("Cell", docStrict.select("td").first().text());
        assertEquals("Cell", docRelaxed.select("td").first().text());
    }

    @Test(timeout = 4000)
    public void testListItemImplicitParent() {
        String html = "<li>First</li><li>Second</li>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        Elements items = doc.select("li");
        assertEquals(2, items.size());
        assertEquals("First", items.get(0).text());
        assertEquals("Second", items.get(1).text());
    }
}