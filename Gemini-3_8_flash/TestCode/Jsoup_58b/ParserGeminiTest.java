package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class under Test: org.jsoup.parser.Parser
 *
 * Decision / Condition Coverage Targets:
 * - Parser(TreeBuilder): TreeBuilder initialization, defaultSettings propagation.
 * - parseInput(String, String):
 *     * isTrackErrors() == true  -> ParseErrorList.tracking(maxErrors)
 *     * isTrackErrors() == false -> ParseErrorList.noTracking()
 * - isTrackErrors():
 *     * maxErrors > 0  (true)
 *     * maxErrors == 0 (false - default)
 *     * maxErrors < 0  (false - negative boundary)
 * - setTrackErrors(int): Fluent API chaining, boundary values (0, -1, 1, Integer.MAX_VALUE).
 * - getErrors():
 *     * Pre-parse invocation -> returns null
 *     * Post-parse invocation with tracking -> populated List<ParseError>
 *     * Post-parse invocation without tracking -> empty List<ParseError>
 * - settings() & settings(ParseSettings):
 *     * default settings preservation
 *     * custom settings propagation (preserveCase vs htmlDefault)
 * - TreeBuilder getters & setters: fluent chaining, dynamic TreeBuilder swapping.
 * - static parse(String, String): HTML parsing shell, normalization check.
 * - static parseFragment(String, Element, String):
 *     * context element provided (context stack setup, e.g., table, select, body)
 *     * null context element fallback
 *     * context element immutability verification (context is not modified)
 * - static parseXmlFragment(String, String): XML parsing, multi-node root handling.
 * - static parseBodyFragment(String, String):
 *     * empty bodyHtml (0 nodes)
 *     * single node (i = 0 loop boundary)
 *     * multiple nodes (i > 0 backward loop removal, node array re-parenting)
 *     * text-only and comment nodes
 * - static parseBodyFragmentRelaxed(String, String): deprecated delegation to parse.
 * - static unescapeEntities(String, boolean):
 *     * inAttribute == true vs false
 *     * standard entities (&amp;, &lt;, &gt;, &quot;, &apos;)
 *     * strings with no entities, empty strings
 * - static htmlParser() & xmlParser(): factory creation checks.
 *
 * Defects4J Ground Truth Target:
 * - CleanerTest::testIsValidBodyHtml & CleanerTest::testIsValidDocument failures:
 *   Body fragments containing full document structural tags (<html>, <head>, <script>)
 *   or unclosed tags must be correctly parsed or error-tracked.
 *   Testing that Parser.parseBodyFragment handles full HTML tags by embedding them in body
 *   while leaving head empty, and verifying that Parser instance error-tracking accurately
 *   captures syntax and structural errors like unclosed tags and illegal head content.
 */
public class ParserGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHtmlParserFactoryDefaults() {
        Parser parser = Parser.htmlParser();
        assertNotNull("Parser instance should not be null", parser);
        assertNotNull("TreeBuilder should be initialized", parser.getTreeBuilder());
        assertTrue("TreeBuilder should be HtmlTreeBuilder", parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertFalse("Tracking errors should be disabled by default", parser.isTrackErrors());
        assertNull("Errors list should be null before any parse operation", parser.getErrors());

        ParseSettings settings = parser.settings();
        assertNotNull("Settings should not be null", settings);
    }

    @Test(timeout = 4000)
    public void testXmlParserFactoryDefaults() {
        Parser parser = Parser.xmlParser();
        assertNotNull("Parser instance should not be null", parser);
        assertNotNull("TreeBuilder should be initialized", parser.getTreeBuilder());
        assertTrue("TreeBuilder should be XmlTreeBuilder", parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertFalse("Tracking errors should be disabled by default", parser.isTrackErrors());

        Document doc = parser.parseInput("<root><child attr='val'>Content</child></root>", "http://example.com/");
        assertNotNull("Document should not be null", doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("val", doc.child(0).child(0).attr("attr"));
    }

    @Test(timeout = 4000)
    public void testSetAndGetTreeBuilder() {
        Parser parser = Parser.htmlParser();
        XmlTreeBuilder xmlTreeBuilder = new XmlTreeBuilder();

        Parser chained = parser.setTreeBuilder(xmlTreeBuilder);
        assertSame("setTreeBuilder should return this for chaining", parser, chained);
        assertSame("TreeBuilder should be updated", xmlTreeBuilder, parser.getTreeBuilder());

        Document doc = parser.parseInput("<xml Tag='Upper'>test</xml>", "");
        assertEquals("xml", doc.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testSettingsGetAndSetChaining() {
        Parser parser = Parser.htmlParser();
        ParseSettings customSettings = new ParseSettings(true, true);

        Parser chained = parser.settings(customSettings);
        assertSame("settings(ParseSettings) should return this for chaining", parser, chained);
        assertSame("settings() should return the updated settings instance", customSettings, parser.settings());

        Document doc = parser.parseInput("<DIV ID='UPPER'>Test</DIV>", "");
        Element div = doc.select("DIV").first();
        assertNotNull("Element DIV with preserved case should be found", div);
        assertEquals("DIV", div.tagName());
        assertEquals("UPPER", div.attr("ID"));
    }

    @Test(timeout = 4000)
    public void testStaticParseHtml() {
        Document doc = Parser.parse("<div id='content'><p>Hello World</p></div>", "http://example.com/");
        assertNotNull(doc);
        assertEquals("http://example.com/", doc.baseUri());
        Element div = doc.getElementById("content");
        assertNotNull(div);
        assertEquals("Hello World", div.select("p").first().text());
    }

    @Test(timeout = 4000)
    public void testStaticParseXmlFragment() {
        String xml = "<item id='1'>Text 1</item><item id='2'>Text 2</item>";
        List<Node> nodes = Parser.parseXmlFragment(xml, "http://example.com/");
        assertNotNull(nodes);
        assertEquals(2, nodes.size());

        assertTrue(nodes.get(0) instanceof Element);
        Element item1 = (Element) nodes.get(0);
        assertEquals("item", item1.tagName());
        assertEquals("1", item1.attr("id"));
        assertEquals("Text 1", item1.text());

        assertTrue(nodes.get(1) instanceof Element);
        Element item2 = (Element) nodes.get(1);
        assertEquals("item", item2.tagName());
        assertEquals("2", item2.attr("id"));
        assertEquals("Text 2", item2.text());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testStaticParseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<p>Relaxed <b>HTML</b></p>", "http://example.com/");
        assertNotNull(doc);
        Element p = doc.body().select("p").first();
        assertNotNull(p);
        assertEquals("Relaxed HTML", p.text());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTrackErrorsBoundaryStates() {
        Parser parser = Parser.htmlParser();

        // Default: 0 errors
        assertFalse(parser.isTrackErrors());

        // Boundary: 1 error enabled
        parser.setTrackErrors(1);
        assertTrue(parser.isTrackErrors());

        // Boundary: 0 disabled
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());

        // Boundary: Negative values treated as disabled
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
        parser.setTrackErrors(Integer.MIN_VALUE);
        assertFalse(parser.isTrackErrors());

        // Boundary: Upper extreme
        parser.setTrackErrors(Integer.MAX_VALUE);
        assertTrue(parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testParseInputWithNoErrorsTracked() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(0);

        Document doc = parser.parseInput("<html><p>Unclosed paragraph", "");
        assertNotNull(doc);
        List<ParseError> errors = parser.getErrors();
        assertNotNull("Errors list should be non-null after parsing", errors);
        assertEquals("Tracking was disabled (maxErrors=0), so errors must be empty", 0, errors.size());
    }

    @Test(timeout = 4000)
    public void testParseInputWithMaxErrorsLimit() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(1);

        // Input with multiple syntax and structural flaws
        String malformedHtml = "<p>Unclosed <b id=1 id=2>Duplicate and Unclosed";
        Document doc = parser.parseInput(malformedHtml, "");
        assertNotNull(doc);

        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertEquals("Errors should be capped exactly at maxErrors limit of 1", 1, errors.size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentZeroNodes() {
        Document doc = Parser.parseBodyFragment("", "http://example.com/");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals("Body should have zero child nodes for empty string", 0, doc.body().childNodes().size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentSingleNode() {
        // Exercise the nodes.length == 1 branch in parseBodyFragment where loop (i = length - 1; i > 0; i--) does not enter
        Document doc = Parser.parseBodyFragment("<div>Single Element</div>", "http://example.com/");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("Single Element", doc.body().child(0).text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentMultipleNodes() {
        // Exercise the backward removal loop (i = length - 1; i > 0; i--) with multiple nodes
        String html = "<p>First</p><span>Second</span><div>Third</div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");
        assertNotNull(doc);
        assertEquals("Should contain 3 elements in exact document order", 3, doc.body().children().size());
        assertEquals("p", doc.body().child(0).tagName());
        assertEquals("First", doc.body().child(0).text());
        assertEquals("span", doc.body().child(1).tagName());
        assertEquals("Second", doc.body().child(1).text());
        assertEquals("div", doc.body().child(2).tagName());
        assertEquals("Third", doc.body().child(2).text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithTextAndComments() {
        String html = "<!-- Initial Comment -->Raw Text<p>Paragraph</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com/");
        assertNotNull(doc);

        List<Node> childNodes = doc.body().childNodes();
        assertTrue("Body should contain at least comment, text, and element", childNodes.size() >= 3);
        assertEquals("Paragraph", doc.body().select("p").first().text());
    }

    @Test(timeout = 4000)
    public void testUnescapeEntitiesBoundaries() {
        // Empty string
        assertEquals("", Parser.unescapeEntities("", false));
        assertEquals("", Parser.unescapeEntities("", true));

        // String with no entities
        assertEquals("Plain text without entities", Parser.unescapeEntities("Plain text without entities", false));
        assertEquals("Plain text without entities", Parser.unescapeEntities("Plain text without entities", true));

        // Standard entities in body mode
        assertEquals("& < > \" '", Parser.unescapeEntities("&amp; &lt; &gt; &quot; &apos;", false));

        // Strict mode (inAttribute = true)
        assertEquals("& < > \" '", Parser.unescapeEntities("&amp; &lt; &gt; &quot; &apos;", true));

        // Malformed entity preservation
        assertEquals("&notAnEntity;", Parser.unescapeEntities("&notAnEntity;", false));
    }

    @Test(timeout = 4000)
    public void testParseFragmentContextIntegrity() {
        // Test with table context (stack context determines implicit element creation)
        Element table = new Element("table");
        List<Node> nodes = Parser.parseFragment("<tr><td>Cell 1</td><td>Cell 2</td></tr>", table, "http://example.com/");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        Element tr = (Element) nodes.get(0);
        assertEquals("tr", tr.tagName());
        assertEquals(2, tr.children().size());
        assertEquals("Cell 1", tr.child(0).text());

        // Crucial check: Context element itself MUST NOT be modified
        assertEquals("Context element child nodes must remain untouched", 0, table.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithNullContext() {
        List<Node> nodes = Parser.parseFragment("<div>Item 1</div><p>Item 2</p>", null, "http://example.com/");
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals("div", ((Element) nodes.get(0)).tagName());
        assertEquals("p", ((Element) nodes.get(1)).tagName());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (CleanerTest Defect Alignment)
    // =========================================================================

    /**
     * Targets defects related to CleanerTest::testIsValidBodyHtml and CleanerTest::testIsValidDocument.
     * When a full HTML document (containing <html>, <head>, <script>) is supplied as body fragment:
     * - Parser.parseBodyFragment must normalize and relocate body content into the body shell.
     * - The shell's <head> element must remain empty, and structural content must reside in body.
     */
    @Test(timeout = 4000)
    public void testParseBodyFragmentWithFullHtmlDocumentDefectCondition() {
        String fullHtml = "<html><head><title>Title</title><script>alert(1);</script></head><body><p><b>OK</b></p></body></html>";
        Document doc = Parser.parseBodyFragment(fullHtml, "http://example.com/");

        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());

        // In parseBodyFragment, Document.createShell creates an empty head, and fragment nodes are appended to body
        assertEquals("Head element should not receive fragment content during body fragment parsing",
                0, doc.head().childNodes().size());

        Element paragraph = doc.body().select("p").first();
        assertNotNull("Body must contain the parsed paragraph", paragraph);
        assertEquals("OK", paragraph.text());
    }

    /**
     * Targets error-tracking behavior on unclosed tags and document-level structural tags.
     * Cleaner.isValidBodyHtml relies on parse errors being accurately recorded.
     */
    @Test(timeout = 4000)
    public void testParseInputErrorTrackingOnMalformedAndUnclosedHtml() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(50);

        // String with unclosed tags (ok1 in CleanerTest)
        String unclosedHtml = "<p>Test <b><a href='http://example.com/'>do ok</a></b>";
        Document doc = parser.parseInput(unclosedHtml, "http://example.com/");
        assertNotNull(doc);

        List<ParseError> errors = parser.getErrors();
        assertNotNull("Errors list must not be null when tracking is enabled", errors);
        assertFalse("Unclosed tags must generate parse errors", errors.isEmpty());
    }

    /**
     * Targets error tracking when full HTML structure (<html><head>...</head>) is provided in unexpected context.
     */
    @Test(timeout = 4000)
    public void testParseInputErrorTrackingOnIllegalStructure() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(50);

        // Nested html tags and misplaced elements
        String invalidHtml = "<div><html><head>Foo</head><body>Bar</body></html></div>";
        Document doc = parser.parseInput(invalidHtml, "http://example.com/");
        assertNotNull(doc);

        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertFalse("Misplaced <html>/<head>/<body> tags must produce parse errors", errors.isEmpty());
    }

    /**
     * Verifies clean, valid HTML produces zero errors when error tracking is active.
     */
    @Test(timeout = 4000)
    public void testParseInputZeroErrorsOnCleanDocument() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(50);

        String cleanHtml = "<!DOCTYPE html><html><head><title>Clean</title></head><body><p>Clean Paragraph</p></body></html>";
        Document doc = parser.parseInput(cleanHtml, "http://example.com/");
        assertNotNull(doc);

        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertEquals("Clean and valid document must produce 0 parse errors", 0, errors.size());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInputNullHtmlThrowsException() {
        Parser parser = Parser.htmlParser();
        parser.parseInput(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseInputNullBaseUriThrowsException() {
        Parser parser = Parser.htmlParser();
        parser.parseInput("<p>Test</p>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseNullHtmlThrowsException() {
        Parser.parse(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseNullBaseUriThrowsException() {
        Parser.parse("<div>Text</div>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseBodyFragmentNullHtmlThrowsException() {
        Parser.parseBodyFragment(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseBodyFragmentNullBaseUriThrowsException() {
        Parser.parseBodyFragment("<p>Text</p>", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseFragmentNullHtmlThrowsException() {
        Parser.parseFragment(null, new Element("body"), "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseFragmentNullBaseUriThrowsException() {
        Parser.parseFragment("<p>Text</p>", new Element("body"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseXmlFragmentNullXmlThrowsException() {
        Parser.parseXmlFragment(null, "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStaticParseXmlFragmentNullBaseUriThrowsException() {
        Parser.parseXmlFragment("<item>Test</item>", null);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Parser Pipeline Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParserSequentialReusability() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);

        // First parse: clean document
        Document doc1 = parser.parseInput("<p>Valid document</p>", "http://example.com/");
        assertNotNull(doc1);
        int errorsDoc1 = parser.getErrors().size();
        assertEquals(0, errorsDoc1);

        // Second parse: document with parse errors
        Document doc2 = parser.parseInput("<p>Missing closing tag <div>Invalid nesting", "http://example.com/");
        assertNotNull(doc2);
        int errorsDoc2 = parser.getErrors().size();
        assertTrue("Second parse should register parse errors", errorsDoc2 > 0);

        // Third parse: clean document again -> errors list should be reset
        Document doc3 = parser.parseInput("<div><p>Clean again</p></div>", "http://example.com/");
        assertNotNull(doc3);
        int errorsDoc3 = parser.getErrors().size();
        assertEquals("Errors list must be refreshed for each parseInput invocation", 0, errorsDoc3);
    }
}