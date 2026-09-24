/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Parser
 *
 * Branches & Methods Covered:
 * 1. Parser(TreeBuilder treeBuilder) constructor
 * 2. parseInput(String html, String baseUri)
 *    - Branch: isTrackErrors() == true -> ParseErrorList.tracking(maxErrors)
 *    - Branch: isTrackErrors() == false -> ParseErrorList.noTracking()
 * 3. getTreeBuilder() / setTreeBuilder(TreeBuilder treeBuilder)
 * 4. isTrackErrors()
 *    - Branch: maxErrors > 0 (true)
 *    - Branch: maxErrors <= 0 (false) (zero, negative boundaries)
 * 5. setTrackErrors(int maxErrors) fluent chaining
 * 6. getErrors() -> retrieved list inspection
 * 7. parse(String html, String baseUri)
 * 8. parseFragment(String fragmentHtml, Element context, String baseUri)
 *    - With explicit context element (e.g. table, div)
 *    - With null context element
 * 9. parseBodyFragment(String bodyHtml, String baseUri)
 *    - Reparenting nodes into shell body
 * 10. parseBodyFragmentRelaxed(String bodyHtml, String baseUri)
 * 11. htmlParser() / xmlParser() static factory methods
 *
 * Defects4J Targeted Defect Zones:
 * - Entity unescaping / tokenisation regressions:
 *   - "doesNotFindShortestMatchingEntity": Invalid extended prefix matching where an invalid
 *     extended entity (e.g., &clubsuite;) must not decode prefix (&clubsuit;) leaving orphaned text.
 *   - "relaxedBaseEntityMatchAndStrictExtendedMatch": Un-semicoloned entities in body text
 *     where legacy entities may match without ';' but extended named entities require ';'.
 *   - "strictAttributeUnescapes": Attribute values containing '&' parameters (like '&mid')
 *     that must not decode without terminal semicolon.
 */

package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHtmlParserFactoryInitialization() {
        Parser parser = Parser.htmlParser();
        assertNotNull("Html parser instance should not be null", parser);
        assertNotNull("TreeBuilder should be set", parser.getTreeBuilder());
        assertTrue("TreeBuilder should be HtmlTreeBuilder", parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertFalse("Tracking should be off by default", parser.isTrackErrors());
        assertNull("Errors list should be null before any parse execution", parser.getErrors());
    }

    @Test(timeout = 4000)
    public void testXmlParserFactoryInitialization() {
        Parser parser = Parser.xmlParser();
        assertNotNull("Xml parser instance should not be null", parser);
        assertNotNull("TreeBuilder should be set", parser.getTreeBuilder());
        assertTrue("TreeBuilder should be XmlTreeBuilder", parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertFalse("Tracking should be off by default", parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testSetAndGetTreeBuilder() {
        Parser parser = Parser.htmlParser();
        TreeBuilder xmlTb = new XmlTreeBuilder();
        Parser returned = parser.setTreeBuilder(xmlTb);

        assertSame("setTreeBuilder should return this parser for chaining", parser, returned);
        assertSame("TreeBuilder should match the newly configured one", xmlTb, parser.getTreeBuilder());
    }

    @Test(timeout = 4000)
    public void testParseInputWithoutErrorTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());

        Document doc = parser.parseInput("<div><p>Hello</div>", "http://example.com/");
        assertNotNull("Document should not be null", doc);
        assertEquals("Hello", doc.select("div > p").text());
        assertEquals("http://example.com/", doc.baseUri());
        assertNotNull("Errors list should not be null after parse", parser.getErrors());
        assertEquals("Errors list should be empty when tracking is disabled", 0, parser.getErrors().size());
    }

    @Test(timeout = 4000)
    public void testParseInputWithErrorTrackingEnabled() {
        Parser parser = Parser.htmlParser();
        Parser chained = parser.setTrackErrors(10);
        assertSame("setTrackErrors should return this for fluent chaining", parser, chained);
        assertTrue(parser.isTrackErrors());

        // Malformed HTML to trigger parse error collection
        Document doc = parser.parseInput("<div><span>Test</div>", "http://example.com/");
        assertNotNull(doc);
        assertNotNull("Errors collection should not be null", parser.getErrors());
        assertFalse("Parse errors should have been recorded", parser.getErrors().isEmpty());
        assertTrue("Errors recorded should not exceed maxErrors limit", parser.getErrors().size() <= 10);
    }

    @Test(timeout = 4000)
    public void testStaticParse() {
        Document doc = Parser.parse("<title>Jsoup Spec</title><p>Paragraph</p>", "http://example.com/spec");
        assertNotNull(doc);
        assertEquals("Jsoup Spec", doc.title());
        assertEquals("Paragraph", doc.select("p").first().text());
        assertEquals("http://example.com/spec", doc.baseUri());
    }

    @Test(timeout = 4000)
    public void testStaticParseFragmentWithContext() {
        Document doc = Document.createShell("http://example.com/");
        Element table = doc.body().appendElement("table");

        List<Node> nodes = Parser.parseFragment("<tr><td>Row 1</td></tr>", table, "http://example.com/");
        assertNotNull(nodes);
        assertFalse("Fragment nodes list should not be empty", nodes.isEmpty());
        Element tr = (Element) nodes.get(0);
        assertEquals("tr", tr.tagName());
        assertEquals("Row 1", tr.select("td").text());
    }

    @Test(timeout = 4000)
    public void testStaticParseFragmentWithNullContext() {
        List<Node> nodes = Parser.parseFragment("<div>Block</div><span>Inline</span>", null, "http://example.com/");
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals("div", ((Element) nodes.get(0)).tagName());
        assertEquals("span", ((Element) nodes.get(1)).tagName());
    }

    @Test(timeout = 4000)
    public void testStaticParseBodyFragment() {
        Document doc = Parser.parseBodyFragment("<p>Sample Text</p><span>Extra</span>", "http://example.com/");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(2, doc.body().children().size());
        assertEquals("Sample Text", doc.body().select("p").text());
        assertEquals("Extra", doc.body().select("span").text());
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testStaticParseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<div>Relaxed Node</div>", "http://example.com/");
        assertNotNull(doc);
        assertEquals("Relaxed Node", doc.select("div").text());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTrackErrorsBoundaryValues() {
        Parser parser = Parser.htmlParser();

        parser.setTrackErrors(0);
        assertFalse("0 should mean tracking is off", parser.isTrackErrors());

        parser.setTrackErrors(-1);
        assertFalse("Negative value should mean tracking is off", parser.isTrackErrors());

        parser.setTrackErrors(Integer.MIN_VALUE);
        assertFalse("Integer.MIN_VALUE should mean tracking is off", parser.isTrackErrors());

        parser.setTrackErrors(1);
        assertTrue("1 should enable error tracking", parser.isTrackErrors());

        parser.setTrackErrors(Integer.MAX_VALUE);
        assertTrue("Integer.MAX_VALUE should enable error tracking", parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testParseEmptyHtml() {
        Document doc = Parser.parse("", "http://example.com/");
        assertNotNull(doc);
        assertEquals("", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithEmptyString() {
        Document doc = Parser.parseBodyFragment("", "http://example.com/");
        assertNotNull(doc);
        assertEquals(0, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testXmlParserHandlingXmlDeclaration() {
        Parser parser = Parser.xmlParser();
        Document doc = parser.parseInput("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child id=\"1\">Data</child></root>", "http://example.com/");
        assertNotNull(doc);
        assertEquals("root", doc.childNode(1).nodeName());
        assertEquals("Data", doc.select("child").text());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoesNotFindShortestMatchingEntity() {
        // Known bug: &clubsuite; was improperly decoded by matching prefix entity &clubsuit; into ♣
        // leaving trailing 'e;'. The full entity &clubsuite; does not exist, so it must not be decoded.
        String html = "One &clubsuite; &clubsuit;";
        Document doc = Parser.parse(html, "");
        assertEquals("One &amp;clubsuite; ♣", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testRelaxedBaseEntityMatchAndStrictExtendedMatch() {
        // Extended named entities without a semicolon must not be decoded in body text
        String html = "& &\" &reg &icy &hopf &icy; &hopf;";
        Document doc = Parser.parse(html, "");
        assertEquals("&amp; &quot; &reg; &amp;icy &amp;hopf &icy; &hopf;", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testStrictAttributeUnescapes() {
        // In attribute context, &mid without semicolon should not decode into U+2223 (∣)
        String html = "<a href=\"?foo=bar&mid&lt=true\">link</a>";
        Document doc = Parser.parse(html, "");
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("?foo=bar&mid&lt=true", a.attr("href"));
    }

    @Test(timeout = 4000)
    public void testMoreAttributeUnescapes() {
        // &wr without semicolon should not decode into ≀
        String html = "<a href=\"?id=123&mid-size=true&ok=&wr\">link</a>";
        Document doc = Parser.parse(html, "");
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("?id=123&mid-size=true&ok=&wr", a.attr("href"));
    }

    @Test(timeout = 4000)
    public void testNoSpuriousDecodesInUrlAttributes() {
        // URL with multiple query params must not decode &num_rooms or &int
        String html = "<a href=\"http://www.foo.com?a=1&num_rooms=1&children=0&int=VA&b=2\">link</a>";
        Document doc = Parser.parse(html, "");
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://www.foo.com?a=1&num_rooms=1&children=0&int=VA&b=2", a.attr("href"));
    }

    // =========================================================================
    // Partition D: TreeBuilder and Parser Edge Integration
    // =========================================================================

    @Test(timeout = 4000)
    public void testErrorTrackingCap() {
        Parser parser = Parser.htmlParser();
        int maxErrors = 2;
        parser.setTrackErrors(maxErrors);

        // Many syntax errors: unclosed tags, invalid nesting
        String html = "<p><div><span><b></i></em></span></div></p>";
        parser.parseInput(html, "");

        assertNotNull(parser.getErrors());
        assertEquals("Errors should be capped at maxErrors limit", maxErrors, parser.getErrors().size());
    }

    @Test(timeout = 4000)
    public void testCustomConstructorAssignment() {
        TreeBuilder customTb = new HtmlTreeBuilder();
        Parser parser = new Parser(customTb);
        assertSame("TreeBuilder passed to constructor must be retained", customTb, parser.getTreeBuilder());
    }
}