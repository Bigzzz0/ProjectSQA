/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Parser
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor:
 *    - isBodyFragment == true: Document.createShell, stack contains doc.body()
 *    - isBodyFragment == false: new Document, stack contains doc
 *    - Validate.notNull checks for html and baseUri
 * 2. parse():
 *    - Matches "<!--" -> parseComment()
 *      * Comment ends with "-" (was "-->") vs doesn't end with "-"
 *    - Matches "<![CDATA[" -> parseCdata()
 *    - Matches "<?" or "<!" -> parseXmlDecl()
 *      * firstChar == "!" (procInstr true) vs "?" (procInstr false)
 *    - Matches "</" -> parseEndTag()
 *      * tagName.length() != 0 vs == 0
 *      * popStackToClose: tag in stack vs not in stack; stops at body/html tag
 *    - Matches "<" -> parseStartTag()
 *      * tagName.length() == 0 (false alarm, re-inject &lt; as text)
 *      * Attributes parsing: SQ ('), DQ ("), unquoted, whitespace, empty/malformed
 *      * Self-closing / empty tags ("/>" vs ">", tag.isEmpty())
 *      * Data tags (tag.isData()): title/textarea (TextNode) vs script/style (DataNode)
 *      * Base href tag: absUrl href updates baseUri and doc.baseUri
 *    - Else -> parseTextNode()
 * 3. addChildToParent():
 *    - stackHasValidParent: root is html vs element has valid parent vs implicit parent required
 *    - child is bodyTag: implicit parent created, ensures head is created before body
 *    - popStackToSuitableContainer: finds element that canContain(tag) vs drops elements until match
 * 4. Ground Truth Defect (Defects4J - ParserTest::handlesTextAfterData):
 *    - For data tags (e.g. <script>, <title>, <textarea>), parseStartTag consumes the closing tag
 *      via tq.chompTo("</" + tagName) and tq.chompTo(">").
 *    - However, child was pushed onto the parsing stack in addChildToParent. Because the end tag
 *      was already consumed inside parseStartTag, parseEndTag is never invoked to pop the data element.
 *    - As a result, subsequent content/text (" aft") is erroneously attached as a child of <script>.
 *    - Targeted test: testHandlesTextAfterDataTag() asserts text after </script> is outside <script>.
 */

package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicDocumentParsing() {
        String html = "<html><head><title>Test Title</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull("Document should not be null", doc);
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("Test Title", doc.title());

        Element body = doc.body();
        assertNotNull("Body should exist", body);
        assertEquals(1, body.children().size());

        Element p = body.child(0);
        assertEquals("p", p.tagName());
        assertEquals("Hello World", p.text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragment() {
        String fragment = "<div><span>Fragment Content</span></div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com/");

        assertNotNull(doc);
        assertNotNull(doc.body());
        Element div = doc.body().child(0);
        assertEquals("div", div.tagName());
        assertEquals("span", div.child(0).tagName());
        assertEquals("Fragment Content", div.child(0).text());
    }

    @Test(timeout = 4000)
    public void testCommentsStandardAndDangling() {
        String html = "<div><!-- Standard Comment --><span>Middle</span><!-- Dangling Comment -></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        List<Node> nodes = div.childNodes();

        assertTrue(nodes.get(0) instanceof Comment);
        Comment comment1 = (Comment) nodes.get(0);
        assertEquals(" Standard Comment ", comment1.getData());

        assertEquals("span", ((Element) nodes.get(1)).tagName());

        assertTrue(nodes.get(2) instanceof Comment);
        Comment comment2 = (Comment) nodes.get(2);
        assertEquals(" Dangling Comment ", comment2.getData());
    }

    @Test(timeout = 4000)
    public void testXmlDeclarationAndProcessingInstruction() {
        String html = "<?xml version=\"1.0\" encoding=\"utf-8\"?><!DOCTYPE html><html><body>Content</body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        List<Node> rootNodes = doc.childNodes();
        boolean foundProcInstr = false;
        boolean foundDocType = false;

        for (Node node : rootNodes) {
            if (node instanceof XmlDeclaration) {
                XmlDeclaration decl = (XmlDeclaration) node;
                if (!decl.getWholeDeclaration().startsWith("DOCTYPE")) {
                    foundProcInstr = true;
                } else {
                    foundDocType = true;
                }
            }
        }
        assertTrue("XML Declaration should be parsed", foundProcInstr);
        assertTrue("DOCTYPE should be parsed as XmlDeclaration", foundDocType);
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        String html = "<div><![CDATA[<unescaped> & &amp; content]]></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        assertEquals(1, div.childNodes().size());
        Node child = div.childNode(0);
        assertTrue(child instanceof TextNode);
        assertEquals("<unescaped> & &amp; content", ((TextNode) child).getWholeText());
    }

    @Test(timeout = 4000)
    public void testAttributeParsingVariations() {
        String html = "<a href='single.html' title=\"double\" rel=unquoted disabled empty=\"\" weird%attr=val>Link</a>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element a = doc.body().child(0);
        Attributes attrs = a.attributes();

        assertEquals("single.html", attrs.get("href"));
        assertEquals("double", attrs.get("title"));
        assertEquals("unquoted", attrs.get("rel"));
        assertEquals("", attrs.get("disabled"));
        assertEquals("", attrs.get("empty"));
        assertEquals("val", attrs.get("weird%attr"));
    }

    @Test(timeout = 4000)
    public void testBaseHrefResolution() {
        String html = "<html><head><base href=\"http://cdn.example.com/sub/\"></head><body><a href=\"item.html\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("http://cdn.example.com/sub/", doc.baseUri());
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://cdn.example.com/sub/item.html", a.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingAndEmptyTags() {
        String html = "<div><img src=\"test.png\"/><br><input type=\"text\"/></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        assertEquals(3, div.children().size());
        assertEquals("img", div.child(0).tagName());
        assertEquals("br", div.child(1).tagName());
        assertEquals("input", div.child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testImplicitParentCreation() {
        String html = "<td>Lone Cell</td>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertNotNull(doc.select("table").first());
        assertNotNull(doc.select("tr").first());
        assertNotNull(doc.select("td").first());
        assertEquals("Lone Cell", doc.select("td").first().text());
    }

    @Test(timeout = 4000)
    public void testBodyImplicitHeadCreation() {
        String html = "<body>Body Content</body>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull("Head should be implicitly created before body", doc.head());
        assertNotNull("Body should exist", doc.body());
        assertEquals("Body Content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testDataTagsTextareaAndTitle() {
        String html = "<title>Page &amp; Title</title><textarea>Textarea &lt;b&gt;Raw&lt;/b&gt;</textarea>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element title = doc.head().select("title").first();
        assertNotNull(title);
        assertEquals("Page & Title", title.text());

        Element textarea = doc.body().select("textarea").first();
        assertNotNull(textarea);
        assertEquals("Textarea <b>Raw</b>", textarea.text());
        assertTrue(textarea.childNode(0) instanceof TextNode);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyHtml() {
        Document doc = Parser.parse("", "http://example.com/");
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(0, doc.body().childNodes().size());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyHtml() {
        Document doc = Parser.parse("   \n\t  ", "http://example.com/");
        assertNotNull(doc);
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testLessThanWithoutTagName() {
        String html = "< 5 and <";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertEquals("< 5 and <", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testMalformedEndTags() {
        String html = "<div><p>Text</></div></>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().select("div").first();
        assertNotNull(div);
        assertEquals("Text", div.text());
    }

    @Test(timeout = 4000)
    public void testUnclosedTagsAndStackRecovery() {
        String html = "<div><p>Paragraph <span>Span Text</div><div>Next Div</div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        assertEquals(2, doc.body().children().size());
        Element div1 = doc.body().child(0);
        Element div2 = doc.body().child(1);

        assertEquals("div", div1.tagName());
        assertEquals("div", div2.tagName());
        assertEquals("Next Div", div2.text());
    }

    @Test(timeout = 4000)
    public void testAttributeEdgeCases() {
        String html = "<div a= b= c   =   'val' d = \"val2\" e=></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element div = doc.body().child(0);
        assertEquals("", div.attr("a"));
        assertEquals("", div.attr("b"));
        assertEquals("val", div.attr("c"));
        assertEquals("val2", div.attr("d"));
        assertEquals("", div.attr("e"));
    }

    @Test(timeout = 4000)
    public void testBaseTagWithoutHref() {
        String html = "<html><head><base target=\"_blank\"></head><body>Text</body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals("http://example.com/", doc.baseUri());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: ParserTest::handlesTextAfterData
     * In the defective version, data tags like <script> chomp to </script> and >
     * inside parseStartTag, but the <script> Element is added to the parsing stack.
     * Because the closing tag was already consumed, parseEndTag is not called,
     * leaving <script> unclosed on the stack. Consequently, subsequent text
     * (" aft") is erroneously placed inside <script> instead of after it in <body>.
     */
    @Test(timeout = 4000)
    public void testHandlesTextAfterDataTag() {
        String html = "<html><body>pre <script>inner</script> aft</body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        Element body = doc.body();
        Element script = body.select("script").first();
        assertNotNull("Script tag must exist", script);

        assertEquals("inner", script.data());
        assertEquals(1, script.childNodes().size());
        assertTrue("Script child must be DataNode", script.childNode(0) instanceof DataNode);

        // Verify that " aft" is a child of body, NOT inside script
        boolean aftFoundInBody = false;
        for (Node node : body.childNodes()) {
            if (node instanceof TextNode && ((TextNode) node).getWholeText().contains("aft")) {
                aftFoundInBody = true;
                break;
            }
        }
        assertTrue("Text 'aft' must be after <script> in <body>, not inside <script>", aftFoundInBody);

        String normalizedBody = body.outerHtml().replaceAll("[\\r\\n]", "");
        assertTrue("Output HTML must have ' aft' outside of script tag",
                normalizedBody.contains("<script>inner</script> aft"));
    }

    @Test(timeout = 4000)
    public void testHandlesTextAfterTextareaDataTag() {
        String html = "<div>before<textarea>inner content</textarea>after</div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("inner content", textarea.text());

        Element div = doc.select("div").first();
        assertNotNull(div);

        boolean afterFoundInDiv = false;
        for (Node node : div.childNodes()) {
            if (node instanceof TextNode && ((TextNode) node).getWholeText().contains("after")) {
                afterFoundInDiv = true;
                break;
            }
        }
        assertTrue("Text 'after' must be a direct child of div, outside textarea", afterFoundInDiv);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullHtmlThrowsException() {
        Parser.parse(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullBaseUriThrowsException() {
        Parser.parse("<div>test</div>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentNullHtmlThrowsException() {
        Parser.parseBodyFragment(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentNullBaseUriThrowsException() {
        Parser.parseBodyFragment("<div>test</div>", null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Structural Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeeplyNestedStructureAndClosing() {
        String html = "<div id='1'><div id='2'><div id='3'><span id='s'>Deep</span></div></div></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");

        Element s = doc.getElementById("s");
        assertNotNull(s);
        assertEquals("Deep", s.text());
        assertEquals("3", s.parent().id());
        assertEquals("2", s.parent().parent().id());
        assertEquals("1", s.parent().parent().parent().id());
    }

    @Test(timeout = 4000)
    public void testClosingPastBodyIgnored() {
        String html = "<html><body><div>Content</div></body></html></div>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc.body());
        assertEquals("Content", doc.body().text());
    }
}