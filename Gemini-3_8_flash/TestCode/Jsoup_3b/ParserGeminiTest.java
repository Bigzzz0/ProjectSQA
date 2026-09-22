package org.jsoup.parser;

import org.jsoup.nodes.Attribute;
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

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.parser.Parser
 * Target Defect (Defects4J): ParserTest::handlesNestedImplicitTable & ElementTest::testAppendRowToTable
 * ---------------------------------------------------------------------------------------------------------
 * Root Cause in Parser.java:
 * In stackHasValidParent(Tag childTag), the stack is traversed from top to bottom (stack.size()-1 down to 0).
 * When childTag is 'td' or 'tr' inside a nested table, stackHasValidParent looks too far up the stack and
 * matches an ancestor <tr> or <table> belonging to an OUTER table. Consequently, stackHasValidParent returns
 * true, failing to realize that the immediate nested table context lacks a valid <tr>, thereby skipping the
 * implicit <tr> parent creation around <td>.
 *
 * Decision / Branch Points Covered:
 * 1. Parser construction: isBodyFragment (true: Document.createShell / stack.add(doc.body()) vs false: Document / stack.add(doc))
 * 2. Token consumption loop:
 *    - <!-- (parseComment) -> endsWith("-") vs normal "->"
 *    - <![CDATA[ (parseCdata)
 *    - <? or <! (parseXmlDecl) -> '!' (DOCTYPE/procInstr=true) vs '?' (procInstr=false)
 *    - </ (parseEndTag) -> tagName.length() == 0 (e.g. </>) vs non-empty; elToClose found vs not found; body/html guard
 *    - < (parseStartTag) -> tagName.length() == 0 (e.g. < 3, <>) vs valid; attributes loop; isEmptyElement (self-closing '/>' vs '>');
 *      isData tags (title/textarea -> TextNode vs script/style -> DataNode); <base href> URI update
 *    - text (parseTextNode)
 * 3. parseAttribute: single quote (''), double quote (""), unquoted, empty key handling (key.length() == 0)
 * 4. addChildToParent & stackHasValidParent:
 *    - validAncestor = true vs false
 *    - child.tag().equals(bodyTag) -> implicit head insertion
 *    - popStackToSuitableContainer: stack.isEmpty() vs last().tag().canContain
 * 5. Defensive guards: null html, null baseUri for parse() and parseBodyFragment()
 * ---------------------------------------------------------------------------------------------------------
 */
public class ParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseStandardDocument() {
        String html = "<html><head><title>Test Page</title></head><body><p id=\"p1\">Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull("Document should not be null", doc);
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("Test Page", doc.title());

        Element p = doc.getElementById("p1");
        assertNotNull("Element #p1 must exist", p);
        assertEquals("p", p.tagName());
        assertEquals("Hello World", p.text());
        assertEquals("http://example.com/", p.baseUri());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragment() {
        String fragment = "<div class=\"content\"><p>Fragment Paragraph</p></div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com/base/");

        assertNotNull(doc);
        assertNotNull(doc.body());
        Element div = doc.body().select("div.content").first();
        assertNotNull("Div must be present in body", div);
        assertEquals("Fragment Paragraph", div.text());
        assertEquals(0, doc.head().children().size());
    }

    @Test(timeout = 4000)
    public void testParseCommentStandardAndNonStandard() {
        // Standard comment ending with -->
        String html1 = "<div><!-- Standard Comment --></div>";
        Document doc1 = Parser.parse(html1, "http://example.com/");
        Element div1 = doc1.select("div").first();
        assertNotNull(div1);
        assertEquals(1, div1.childNodes().size());
        assertTrue(div1.childNode(0) instanceof Comment);
        Comment comment1 = (Comment) div1.childNode(0);
        assertEquals(" Standard Comment ", comment1.getData());

        // Non-standard comment ending with -> (without the second hyphen)
        String html2 = "<div><!-- Nonstandard Comment -></div>";
        Document doc2 = Parser.parse(html2, "http://example.com/");
        Element div2 = doc2.select("div").first();
        assertNotNull(div2);
        assertTrue(div2.childNode(0) instanceof Comment);
        Comment comment2 = (Comment) div2.childNode(0);
        assertEquals(" Nonstandard Comment ", comment2.getData());
    }

    @Test(timeout = 4000)
    public void testParseXmlDeclarationAndDoctype() {
        // <?xml ... ?> -> procInstr is false
        String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><html><body></body></html>";
        Document docXml = Parser.parse(xmlDecl, "http://example.com/");
        List<Node> nodes = docXml.childNodes();
        XmlDeclaration decl = null;
        for (Node node : nodes) {
            if (node instanceof XmlDeclaration) {
                decl = (XmlDeclaration) node;
                break;
            }
        }
        assertNotNull("XmlDeclaration node should exist", decl);
        assertTrue(decl.getWholeDeclaration().contains("version=\"1.0\""));

        // <!DOCTYPE html> -> starts with '!', procInstr is true
        String doctypeHtml = "<!DOCTYPE html><html><head></head><body></body></html>";
        Document docDoctype = Parser.parse(doctypeHtml, "http://example.com/");
        XmlDeclaration doctypeNode = null;
        for (Node node : docDoctype.childNodes()) {
            if (node instanceof XmlDeclaration) {
                doctypeNode = (XmlDeclaration) node;
                break;
            }
        }
        assertNotNull("DOCTYPE node should exist as XmlDeclaration", doctypeNode);
        assertTrue(doctypeNode.getWholeDeclaration().contains("DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testParseCdata() {
        String html = "<p><![CDATA[Some <unescaped> & un-parsed text]]></p>";
        Document doc = Parser.parse(html, "http://example.com/");
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals(1, p.childNodes().size());
        assertTrue(p.childNode(0) instanceof TextNode);
        TextNode textNode = (TextNode) p.childNode(0);
        assertEquals("Some <unescaped> & un-parsed text", textNode.getWholeText());
    }

    @Test(timeout = 4000)
    public void testParseDataTagsScriptStyleTitleAndTextarea() {
        String html = "<title>Page &amp; Title</title>" +
                      "<textarea>Line 1 &amp; Line 2</textarea>" +
                      "<script>var x = 1 < 2 && \"yes\";</script>" +
                      "<style>body > p { color: red; }</style>";
        Document doc = Parser.parse(html, "http://example.com/");

        // Title creates TextNode from encoded
        Element titleEl = doc.select("title").first();
        assertNotNull(titleEl);
        assertEquals(1, titleEl.childNodes().size());
        assertTrue(titleEl.childNode(0) instanceof TextNode);
        assertEquals("Page & Title", ((TextNode) titleEl.childNode(0)).getWholeText());

        // Textarea creates TextNode from encoded
        Element textareaEl = doc.select("textarea").first();
        assertNotNull(textareaEl);
        assertEquals(1, textareaEl.childNodes().size());
        assertTrue(textareaEl.childNode(0) instanceof TextNode);
        assertEquals("Line 1 & Line 2", ((TextNode) textareaEl.childNode(0)).getWholeText());

        // Script creates raw DataNode
        Element scriptEl = doc.select("script").first();
        assertNotNull(scriptEl);
        assertEquals(1, scriptEl.childNodes().size());
        assertTrue(scriptEl.childNode(0) instanceof DataNode);
        assertEquals("var x = 1 < 2 && \"yes\";", ((DataNode) scriptEl.childNode(0)).getWholeData());

        // Style creates raw DataNode
        Element styleEl = doc.select("style").first();
        assertNotNull(styleEl);
        assertEquals(1, styleEl.childNodes().size());
        assertTrue(styleEl.childNode(0) instanceof DataNode);
        assertEquals("body > p { color: red; }", ((DataNode) styleEl.childNode(0)).getWholeData());
    }

    @Test(timeout = 4000)
    public void testBaseTagHrefUpdatesBaseUri() {
        String html = "<html><head><base href=\"http://cdn.example.com/assets/\"></head><body><a href=\"sub/page.html\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://original.example.com/");

        assertEquals("http://cdn.example.com/assets/", doc.baseUri());
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://cdn.example.com/assets/sub/page.html", a.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testBaseTagWithoutHrefDoesNotUpdateBaseUri() {
        String html = "<html><head><base target=\"_blank\"></head><body></body></html>";
        Document doc = Parser.parse(html, "http://original.example.com/");

        assertEquals("http://original.example.com/", doc.baseUri());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseEmptyAndWhitespaceStrings() {
        Document docEmpty = Parser.parse("", "http://example.com/");
        assertNotNull(docEmpty);
        assertEquals(0, docEmpty.body().childNodes().size());

        Document docWhitespace = Parser.parse("   \n\t   ", "http://example.com/");
        assertNotNull(docWhitespace);
        assertEquals(0, docWhitespace.body().children().size());
    }

    @Test(timeout = 4000)
    public void testMalformedStartTagsAndAngleBrackets() {
        // '<' followed by space or number is treated as text
        String html = "< 5 and > 3 and <";
        Document doc = Parser.parse(html, "http://example.com/");
        assertEquals("< 5 and > 3 and <", doc.body().text());

        // Bare angle brackets '<>'
        String html2 = "<>Some text</>";
        Document doc2 = Parser.parse(html2, "http://example.com/");
        assertTrue(doc2.body().text().contains("Some text"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingAndEmptyTags() {
        // Tag defined as empty in HTML (img, hr, br)
        String html = "<img src=\"foo.jpg\"><hr><br>";
        Document doc = Parser.parse(html, "http://example.com/");
        assertEquals(1, doc.select("img").size());
        assertEquals(1, doc.select("hr").size());
        assertEquals(1, doc.select("br").size());

        // Self-closing slash on both normal and empty elements
        String html2 = "<div id=\"d1\"/><img src=\"bar.jpg\"/><p/>";
        Document doc2 = Parser.parse(html2, "http://example.com/");
        assertNotNull(doc2.getElementById("d1"));
        assertEquals(1, doc2.select("img").size());
        assertEquals(1, doc2.select("p").size());
    }

    @Test(timeout = 4000)
    public void testAttributesVariants() {
        // Single quotes, double quotes, unquoted, boolean attributes, extra spaces
        String html = "<input id='a' class=\"b\" name=c value=hello world disabled checked='checked' />";
        Document doc = Parser.parse(html, "http://example.com/");
        Element input = doc.select("input").first();
        assertNotNull(input);

        assertEquals("a", input.attr("id"));
        assertEquals("b", input.attr("class"));
        assertEquals("c", input.attr("name"));
        assertEquals("hello", input.attr("value"));
        assertTrue(input.hasAttr("disabled"));
        assertEquals("checked", input.attr("checked"));
    }

    @Test(timeout = 4000)
    public void testAttributeWithCorruptChar() {
        // '=' without a valid preceding key triggers the key.length() == 0 branch in parseAttribute
        String html = "<div =foo id=\"valid\">text</div>";
        Document doc = Parser.parse(html, "http://example.com/");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("valid", div.attr("id"));
    }

    @Test(timeout = 4000)
    public void testUnclosedAndMismatchedEndTags() {
        // Mismatched closing tags should pop appropriately without breaking DOM
        String html = "<div><p><span>Hello</div>";
        Document doc = Parser.parse(html, "http://example.com/");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("Hello", div.text());

        // Closing an unopened tag should be ignored
        String html2 = "<p>Text</foo></p>";
        Document doc2 = Parser.parse(html2, "http://example.com/");
        assertEquals("Text", doc2.select("p").first().text());

        // Closing body/html tags within content should not close past body
        String html3 = "<p>Before</body>After</html>End</p>";
        Document doc3 = Parser.parse(html3, "http://example.com/");
        assertEquals("Before After End", doc3.body().text());
    }

    @Test(timeout = 4000)
    public void testImplicitHeadCreationWhenBodyIsParsed() {
        // Parsing body directly: tests child.tag().equals(bodyTag) branch in addChildToParent
        String html = "<body><p>Direct body content</p></body>";
        Document doc = Parser.parse(html, "http://example.com/");

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Direct body content", doc.body().text());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Target Defect Ground Truth:
     * - org.jsoup.parser.ParserTest::handlesNestedImplicitTable
     *   Failure: expected:<...> <tr><td> <table><t[r><td>3</td> <td>4</td></tr></table> </td></tr><tr><td>5]</td></tr></table>>
     *            but was:<...>  <tr><td> <table><t[d>3</td> <td>4</td></table> <tr><td>5</td></tr>]</td></tr></table>>
     *
     * In defective jsoup, parsing <td> without an explicit <tr> inside a nested table fails to create the implicit
     * <tr> parent because stackHasValidParent inspects the outer table's <tr> higher in the stack.
     */
    @Test(timeout = 4000)
    public void testHandlesNestedImplicitTableDefect() {
        String html = "<table><tr><td>1</td></tr> <tr><td><table>3<td>4</td></table> <tr><td>5</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com/");

        // Target check 1: The nested table MUST contain an implicit <tr> wrapping its cells
        Element innerTable = doc.select("table table").first();
        assertNotNull("Inner table must exist", innerTable);

        Element innerTr = innerTable.select("tr").first();
        assertNotNull("Inner table cells must be wrapped in an implicit <tr>", innerTr);
        assertEquals("Inner table's first child element must be 'tr'", "tr", innerTable.child(0).tagName());

        // Target check 2: The inner table's <tr> must contain <td>3</td> and <td>4</td>
        List<Element> innerTds = innerTable.select("td");
        assertEquals("Inner table should have 2 <td> elements", 2, innerTds.size());
        assertEquals("3", innerTds.get(0).text());
        assertEquals("4", innerTds.get(1).text());

        // Target check 3: Row 5 must not leak into the inner table or outer td incorrectly
        List<Element> outerTableRows = doc.select("table > tr, table > tbody > tr");
        assertTrue("Outer table must contain multiple rows including row 5", outerTableRows.size() >= 3);
    }

    /**
     * Companion defect check: Direct nested table with implicit row creation
     */
    @Test(timeout = 4000)
    public void testNestedTableImplicitRowDirect() {
        String html = "<table><tr><td><table><td>Cell Inside</td></table></td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element nestedTable = doc.select("table table").first();
        assertNotNull(nestedTable);
        assertEquals("tr", nestedTable.child(0).tagName());
        assertEquals("Cell Inside", nestedTable.select("tr > td").first().text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullHtmlThrows() {
        Parser.parse(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseNullBaseUriThrows() {
        Parser.parse("<div></div>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentNullHtmlThrows() {
        Parser.parseBodyFragment(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBodyFragmentNullBaseUriThrows() {
        Parser.parseBodyFragment("<div></div>", null);
    }

    // =========================================================================
    // Partition E: Deep Tree & Implicit Parent Scaffolding Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testImplicitTableStructureScaffolding() {
        // Bare <tr><td> content must trigger implicit <table> parent creation
        String html = "<tr><td>Cell 1</td><td>Cell 2</td></tr>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element table = doc.select("table").first();
        assertNotNull("Implicit table should be generated around orphan <tr>", table);
        assertEquals(2, table.select("td").size());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedAndUnclosedTagsHierarchy() {
        String html = "<div><ul><li>Item 1<li>Item 2<ol><li>Sub 1<li>Sub 2";
        Document doc = Parser.parse(html, "http://example.com/");

        assertEquals(2, doc.select("ul > li").size());
        assertEquals(2, doc.select("ol > li").size());
        assertEquals("Sub 1", doc.select("ol > li").first().text());
    }

    @Test(timeout = 4000)
    public void testEntitiesInAttributesAndText() {
        String html = "<a href=\"http://example.com/?a=1&amp;b=2\" title=\"&quot;Quote&quot;\">Click &amp; See</a>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://example.com/?a=1&b=2", a.attr("href"));
        assertEquals("\"Quote\"", a.attr("title"));
        assertEquals("Click & See", a.text());
    }
}