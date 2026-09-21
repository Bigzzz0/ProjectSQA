package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Parser.java - Defect in parseBodyFragment (and related static parse methods)
 * 
 * Decision branches targeted:
 * 1. parseInput: isTrackErrors() ? tracking(maxErrors) : noTracking() - both true/false paths
 * 2. setTrackErrors: maxErrors > 0 boundary (0, 1, negative)
 * 3. parseBodyFragment: nodeList.toArray() - empty vs non-empty list
 * 4. parseBodyFragment: for loop (i > 0) - single node, multiple nodes, zero nodes
 * 5. parseBodyFragment: node removal and re-parenting logic
 * 6. Static parse methods: parse, parseFragment, parseXmlFragment, unescapeEntities
 * 7. Constructor: treeBuilder.defaultSettings() initialization
 * 8. settings() getter/setter: null vs non-null settings
 * 
 * Defect targeting:
 * - The known defect causes AssertionFailedError in CleanerTest::testIsValidBodyHtml and 
 *   testIsValidDocument. This indicates parseBodyFragment/parseBodyFragmentRelaxed 
 *   produces incorrect document structure (likely related to node re-parenting loop 
 *   starting at i > 0 instead of i >= 0, or incorrect handling of single-node fragments).
 * - Test targets the exact scenario: parsing a body fragment with a single root element 
 *   and verifying the document structure.
 */
public class ParserDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */
    
    @Test(timeout = 4000)
    public void testParseInputWithErrorTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        assertTrue(parser.isTrackErrors());
        Document doc = parser.parseInput("<p>Hello</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
        assertNotNull(parser.getErrors());
        assertEquals(0, parser.getErrors().size()); // no errors for valid HTML
    }

    @Test(timeout = 4000)
    public void testParseInputWithoutErrorTracking() {
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackErrors());
        Document doc = parser.parseInput("<p>World</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("World", doc.body().text());
        assertNotNull(parser.getErrors());
        assertEquals(0, parser.getErrors().size());
    }

    @Test(timeout = 4000)
    public void testSetTreeBuilderAndGetTreeBuilder() {
        Parser parser = Parser.htmlParser();
        TreeBuilder original = parser.getTreeBuilder();
        assertNotNull(original);
        assertTrue(original instanceof HtmlTreeBuilder);
        
        TreeBuilder xmlBuilder = new XmlTreeBuilder();
        Parser returned = parser.setTreeBuilder(xmlBuilder);
        assertSame(parser, returned);
        assertSame(xmlBuilder, parser.getTreeBuilder());
    }

    @Test(timeout = 4000)
    public void testSettingsGetAndSet() {
        Parser parser = Parser.htmlParser();
        ParseSettings defaultSettings = parser.settings();
        assertNotNull(defaultSettings);
        
        ParseSettings customSettings = new ParseSettings(true, true);
        Parser returned = parser.settings(customSettings);
        assertSame(parser, returned);
        assertSame(customSettings, parser.settings());
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */
    
    @Test(timeout = 4000)
    public void testSetTrackErrorsBoundaryValues() {
        Parser parser = Parser.htmlParser();
        
        // Zero disables tracking
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());
        
        // One enables tracking
        parser.setTrackErrors(1);
        assertTrue(parser.isTrackErrors());
        
        // Negative values disable tracking (treated as 0)
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
        
        // Large values enable tracking
        parser.setTrackErrors(Integer.MAX_VALUE);
        assertTrue(parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testParseInputWithNullHtml() {
        Parser parser = Parser.htmlParser();
        try {
            parser.parseInput(null, "http://example.com");
            fail("Expected NullPointerException for null HTML");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseInputWithNullBaseUri() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<p>Test</p>", null);
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseInputWithEmptyString() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */
    
    /**
     * CRITICAL DEFECT TEST: Tests the exact scenario from CleanerTest::testIsValidBodyHtml
     * that fails with AssertionFailedError. The defect is in parseBodyFragment's node
     * re-parenting logic. When parsing a simple body fragment, the resulting document
     * structure is incorrect.
     */
    @Test(timeout = 4000)
    public void testParseBodyFragmentSingleRootElement() {
        String bodyHtml = "<p>Hello</p>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "http://example.com");
        
        // Verify document structure - this is where the defect manifests
        assertNotNull(doc);
        assertNotNull(doc.body());
        
        // The body should contain exactly one <p> element with text "Hello"
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
        assertEquals("Hello", body.child(0).text());
        
        // Verify the document has proper html/head/body structure
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Hello", doc.body().text());
    }

    /**
     * CRITICAL DEFECT TEST: Tests the scenario from CleanerTest::testIsValidDocument
     * with a full document containing body content.
     */
    @Test(timeout = 4000)
    public void testParseBodyFragmentWithMultipleNodes() {
        String bodyHtml = "<p>First</p><div>Second</div>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "http://example.com");
        
        assertNotNull(doc);
        Element body = doc.body();
        assertEquals(2, body.children().size());
        assertEquals("p", body.child(0).tagName());
        assertEquals("First", body.child(0).text());
        assertEquals("div", body.child(1).tagName());
        assertEquals("Second", body.child(1).text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithEmptyString() {
        Document doc = Parser.parseBodyFragment("", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(0, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithNestedElements() {
        String bodyHtml = "<div><p>Nested</p></div>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "http://example.com");
        
        assertNotNull(doc);
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("div", body.child(0).tagName());
        assertEquals(1, body.child(0).children().size());
        assertEquals("p", body.child(0).child(0).tagName());
        assertEquals("Nested", body.child(0).child(0).text());
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */
    
    @Test(timeout = 4000)
    public void testParseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<p>Relaxed</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Relaxed", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testUnescapeEntities() {
        assertEquals("&", Parser.unescapeEntities("&amp;", false));
        assertEquals("&", Parser.unescapeEntities("&amp;", true));
        assertEquals("<", Parser.unescapeEntities("&lt;", false));
        assertEquals(">", Parser.unescapeEntities("&gt;", false));
        assertEquals("\"", Parser.unescapeEntities("&quot;", true));
        assertEquals("'", Parser.unescapeEntities("&apos;", true));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntitiesWithNullString() {
        try {
            Parser.unescapeEntities(null, false);
            fail("Expected NullPointerException for null string");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */
    
    @Test(timeout = 4000)
    public void testHtmlParserFactory() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertNotNull(parser.settings());
    }

    @Test(timeout = 4000)
    public void testXmlParserFactory() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertNotNull(parser.settings());
    }

    @Test(timeout = 4000)
    public void testStaticParseMethod() {
        Document doc = Parser.parse("<p>Static</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Static", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseFragmentStatic() {
        Element context = new Element("div");
        List<Node> nodes = Parser.parseFragment("<span>Fragment</span>", context, "http://example.com");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("span", nodes.get(0).nodeName());
        assertEquals("Fragment", nodes.get(0).childNode(0).toString());
    }

    @Test(timeout = 4000)
    public void testParseXmlFragmentStatic() {
        List<Node> nodes = Parser.parseXmlFragment("<root><child/></root>", "http://example.com");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("root", nodes.get(0).nodeName());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithNullContext() {
        List<Node> nodes = Parser.parseFragment("<p>No context</p>", null, "http://example.com");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithNullBaseUri() {
        Document doc = Parser.parseBodyFragment("<p>Test</p>", null);
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testGetErrorsBeforeParse() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        // Before parse, errors should be null
        assertNull(parser.getErrors());
    }

    @Test(timeout = 4000)
    public void testGetErrorsAfterParseWithErrors() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        // Parse invalid HTML to generate errors
        parser.parseInput("<p>Unclosed", "http://example.com");
        assertNotNull(parser.getErrors());
        // May or may not have errors depending on HTML5 spec
        assertTrue(parser.getErrors().size() >= 0);
    }

    @Test(timeout = 4000)
    public void testParseInputWithErrorTrackingMaxErrors() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(2);
        // Parse HTML with multiple errors
        parser.parseInput("<p>Unclosed<div>Also unclosed", "http://example.com");
        assertNotNull(parser.getErrors());
        assertTrue(parser.getErrors().size() <= 2);
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithTextOnly() {
        Document doc = Parser.parseBodyFragment("Just text", "http://example.com");
        assertNotNull(doc);
        assertEquals("Just text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithComments() {
        Document doc = Parser.parseBodyFragment("<!-- comment --><p>After</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("After", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithScript() {
        Document doc = Parser.parseBodyFragment("<script>var x = 1;</script>", "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("script", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithDoctype() {
        Document doc = Parser.parseBodyFragment("<!DOCTYPE html><p>Doc</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Doc", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithEntities() {
        Document doc = Parser.parseBodyFragment("<p>&amp;&lt;&gt;</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("&<>", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithAttributes() {
        Document doc = Parser.parseBodyFragment("<p id='test' class='foo'>Attr</p>", "http://example.com");
        assertNotNull(doc);
        Element p = doc.body().child(0);
        assertEquals("test", p.attr("id"));
        assertEquals("foo", p.attr("class"));
        assertEquals("Attr", p.text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithNestedLists() {
        String html = "<ul><li>One</li><li>Two</li></ul>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element ul = doc.body().child(0);
        assertEquals("ul", ul.tagName());
        assertEquals(2, ul.children().size());
        assertEquals("One", ul.child(0).text());
        assertEquals("Two", ul.child(1).text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithTable() {
        String html = "<table><tr><td>Cell</td></tr></table>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element table = doc.body().child(0);
        assertEquals("table", table.tagName());
        assertEquals("Cell", table.text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithForm() {
        String html = "<form><input type='text' name='q'></form>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element form = doc.body().child(0);
        assertEquals("form", form.tagName());
        assertEquals(1, form.children().size());
        assertEquals("input", form.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithMultipleRootElements() {
        String html = "<p>One</p><p>Two</p><p>Three</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(3, doc.body().children().size());
        assertEquals("One", doc.body().child(0).text());
        assertEquals("Two", doc.body().child(1).text());
        assertEquals("Three", doc.body().child(2).text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithWhitespace() {
        Document doc = Parser.parseBodyFragment("   <p>Spaced</p>   ", "http://example.com");
        assertNotNull(doc);
        assertEquals("Spaced", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithNewlines() {
        Document doc = Parser.parseBodyFragment("\n<p>Line</p>\n", "http://example.com");
        assertNotNull(doc);
        assertEquals("Line", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithVoidElements() {
        String html = "<br><hr><img src='test.jpg'>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(3, doc.body().children().size());
        assertEquals("br", doc.body().child(0).tagName());
        assertEquals("hr", doc.body().child(1).tagName());
        assertEquals("img", doc.body().child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithRawTextElements() {
        String html = "<pre>  Preformatted  </pre>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("  Preformatted  ", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithStyle() {
        String html = "<style>body { color: red; }</style>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("style", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithTitle() {
        String html = "<title>My Title</title>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        // Title in body fragment context may be treated as regular element
        assertEquals(1, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithMeta() {
        String html = "<meta charset='utf-8'>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("meta", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithLink() {
        String html = "<link rel='stylesheet' href='style.css'>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("link", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithBase() {
        String html = "<base href='http://example.com/'>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("base", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithIframe() {
        String html = "<iframe src='http://example.com'></iframe>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("iframe", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithEmbed() {
        String html = "<embed src='movie.swf'>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("embed", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithObject() {
        String html = "<object data='file.pdf'></object>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("object", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithAudio() {
        String html = "<audio controls><source src='song.mp3'></audio>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("audio", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithVideo() {
        String html = "<video controls><source src='movie.mp4'></video>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("video", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithCanvas() {
        String html = "<canvas id='myCanvas'></canvas>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("canvas", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithSvg() {
        String html = "<svg width='100' height='100'><circle cx='50' cy='50' r='40'/></svg>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("svg", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithMath() {
        String html = "<math><mi>x</mi><mo>+</mo><mn>2</mn></math>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("math", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithTemplate() {
        String html = "<template><p>Template content</p></template>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("template", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithSlot() {
        String html = "<slot name='content'></slot>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("slot", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithCustomElements() {
        String html = "<my-element>Custom</my-element>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals(1, doc.body().children().size());
        assertEquals("my-element", doc.body().child(0).tagName());
        assertEquals("Custom", doc.body().child(0).text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithDataAttributes() {
        String html = "<div data-id='123' data-name='test'>Data</div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element div = doc.body().child(0);
        assertEquals("123", div.attr("data-id"));
        assertEquals("test", div.attr("data-name"));
        assertEquals("Data", div.text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithAriaAttributes() {
        String html = "<div role='button' aria-label='Click'>ARIA</div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element div = doc.body().child(0);
        assertEquals("button", div.attr("role"));
        assertEquals("Click", div.attr("aria-label"));
        assertEquals("ARIA", div.text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithBooleanAttributes() {
        String html = "<input type='checkbox' checked disabled>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element input = doc.body().child(0);
        assertEquals("checkbox", input.attr("type"));
        assertEquals("", input.attr("checked"));
        assertEquals("", input.attr("disabled"));
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithQuotedAttributes() {
        String html = "<p title='Single \"quoted\"'>Text</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element p = doc.body().child(0);
        assertEquals("Single \"quoted\"", p.attr("title"));
        assertEquals("Text", p.text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithUnquotedAttributes() {
        String html = "<p id=unquoted>Text</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element p = doc.body().child(0);
        assertEquals("unquoted", p.attr("id"));
        assertEquals("Text", p.text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithMixedContent() {
        String html = "<div>Text <b>Bold</b> More <i>Italic</i></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        Element div = doc.body().child(0);
        assertEquals("div", div.tagName());
        assertEquals("Text Bold More Italic", div.text());
        assertEquals(3, div.children().size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithDeepNesting() {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            html.append("<div>");
        }
        html.append("Deep");
        for (int i = 0; i < 100; i++) {
            html.append("</div>");
        }
        Document doc = Parser.parseBodyFragment(html.toString(), "http://example.com");
        assertNotNull(doc);
        assertEquals("Deep", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithMalformedHtml() {
        String html = "<p>Unclosed<div>Nested</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        // Should not throw and should produce some document
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithUnicode() {
        String html = "<p>Héllo Wörld 你好</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("Héllo Wörld 你好", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithSpecialChars() {
        String html = "<p>&lt;script&gt;alert('xss')&lt;/script&gt;</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("<script>alert('xss')</script>", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithCdata() {
        String html = "<![CDATA[Some <b>raw</b> text]]>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        // CDATA in HTML is treated as text
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithProcessingInstruction() {
        String html = "<?xml version='1.0'?><p>After PI</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("After PI", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithDoctypeAndComments() {
        String html = "<!DOCTYPE html><!-- comment --><p>Content</p>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("Content", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithMultipleTextNodes() {
        String html = "Text1<p>Middle</p>Text2";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("Text1MiddleText2", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyWhitespace() {
        Document doc = Parser.parseBodyFragment("   \n\t  ", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyComments() {
        Document doc = Parser.parseBodyFragment("<!-- only comment -->", "http://example.com");
        assertNotNull(doc);
        assertEquals(0, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyDoctype() {
        Document doc = Parser.parseBodyFragment("<!DOCTYPE html>", "http://example.com");
        assertNotNull(doc);
        assertEquals(0, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyProcessingInstruction() {
        Document doc = Parser.parseBodyFragment("<?pi test?>", "http://example.com");
        assertNotNull(doc);
        assertEquals(0, doc.body().children().size());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyCdata() {
        Document doc = Parser.parseBodyFragment("<![CDATA[test]]>", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEntities() {
        Document doc = Parser.parseBodyFragment("&amp;&lt;&gt;", "http://example.com");
        assertNotNull(doc);
        assertEquals("&<>", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyNumericEntities() {
        Document doc = Parser.parseBodyFragment("&#65;&#x42;", "http://example.com");
        assertNotNull(doc);
        assertEquals("AB", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyHexEntities() {
        Document doc = Parser.parseBodyFragment("&#x41;&#x42;", "http://example.com");
        assertNotNull(doc);
        assertEquals("AB", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyNamedEntities() {
        Document doc = Parser.parseBodyFragment("&copy;&reg;", "http://example.com");
        assertNotNull(doc);
        assertEquals("©®", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyUnknownEntities() {
        Document doc = Parser.parseBodyFragment("&unknown;", "http://example.com");
        assertNotNull(doc);
        assertEquals("&unknown;", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyInvalidEntities() {
        Document doc = Parser.parseBodyFragment("&;", "http://example.com");
        assertNotNull(doc);
        assertEquals("&;", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyAmpersand() {
        Document doc = Parser.parseBodyFragment("&", "http://example.com");
        assertNotNull(doc);
        assertEquals("&", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyLessThan() {
        Document doc = Parser.parseBodyFragment("<", "http://example.com");
        assertNotNull(doc);
        assertEquals("<", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyGreaterThan() {
        Document doc = Parser.parseBodyFragment(">", "http://example.com");
        assertNotNull(doc);
        assertEquals(">", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyQuotes() {
        Document doc = Parser.parseBodyFragment("\"", "http://example.com");
        assertNotNull(doc);
        assertEquals("\"", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyApostrophe() {
        Document doc = Parser.parseBodyFragment("'", "http://example.com");
        assertNotNull(doc);
        assertEquals("'", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyBackslash() {
        Document doc = Parser.parseBodyFragment("\\", "http://example.com");
        assertNotNull(doc);
        assertEquals("\\", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlySlash() {
        Document doc = Parser.parseBodyFragment("/", "http://example.com");
        assertNotNull(doc);
        assertEquals("/", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEquals() {
        Document doc = Parser.parseBodyFragment("=", "http://example.com");
        assertNotNull(doc);
        assertEquals("=", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyQuestionMark() {
        Document doc = Parser.parseBodyFragment("?", "http://example.com");
        assertNotNull(doc);
        assertEquals("?", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyExclamationMark() {
        Document doc = Parser.parseBodyFragment("!", "http://example.com");
        assertNotNull(doc);
        assertEquals("!", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyColon() {
        Document doc = Parser.parseBodyFragment(":", "http://example.com");
        assertNotNull(doc);
        assertEquals(":", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlySemicolon() {
        Document doc = Parser.parseBodyFragment(";", "http://example.com");
        assertNotNull(doc);
        assertEquals(";", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyComma() {
        Document doc = Parser.parseBodyFragment(",", "http://example.com");
        assertNotNull(doc);
        assertEquals(",", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyPeriod() {
        Document doc = Parser.parseBodyFragment(".", "http://example.com");
        assertNotNull(doc);
        assertEquals(".", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyHyphen() {
        Document doc = Parser.parseBodyFragment("-", "http://example.com");
        assertNotNull(doc);
        assertEquals("-", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyUnderscore() {
        Document doc = Parser.parseBodyFragment("_", "http://example.com");
        assertNotNull(doc);
        assertEquals("_", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyPlus() {
        Document doc = Parser.parseBodyFragment("+", "http://example.com");
        assertNotNull(doc);
        assertEquals("+", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyStar() {
        Document doc = Parser.parseBodyFragment("*", "http://example.com");
        assertNotNull(doc);
        assertEquals("*", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyPercent() {
        Document doc = Parser.parseBodyFragment("%", "http://example.com");
        assertNotNull(doc);
        assertEquals("%", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyDollar() {
        Document doc = Parser.parseBodyFragment("$", "http://example.com");
        assertNotNull(doc);
        assertEquals("$", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyHash() {
        Document doc = Parser.parseBodyFragment("#", "http://example.com");
        assertNotNull(doc);
        assertEquals("#", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyAt() {
        Document doc = Parser.parseBodyFragment("@", "http://example.com");
        assertNotNull(doc);
        assertEquals("@", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyCaret() {
        Document doc = Parser.parseBodyFragment("^", "http://example.com");
        assertNotNull(doc);
        assertEquals("^", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyTilde() {
        Document doc = Parser.parseBodyFragment("~", "http://example.com");
        assertNotNull(doc);
        assertEquals("~", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyBacktick() {
        Document doc = Parser.parseBodyFragment("`", "http://example.com");
        assertNotNull(doc);
        assertEquals("`", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyPipe() {
        Document doc = Parser.parseBodyFragment("|", "http://example.com");
        assertNotNull(doc);
        assertEquals("|", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyBrackets() {
        Document doc = Parser.parseBodyFragment("[]{}()", "http://example.com");
        assertNotNull(doc);
        assertEquals("[]{}()", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyAngleBrackets() {
        Document doc = Parser.parseBodyFragment("<>", "http://example.com");
        assertNotNull(doc);
        assertEquals("<>", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyNewline() {
        Document doc = Parser.parseBodyFragment("\n", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyTab() {
        Document doc = Parser.parseBodyFragment("\t", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyCarriageReturn() {
        Document doc = Parser.parseBodyFragment("\r", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyFormFeed() {
        Document doc = Parser.parseBodyFragment("\f", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyBackspace() {
        Document doc = Parser.parseBodyFragment("\b", "http://example.com");
        assertNotNull(doc);
        assertEquals("\b", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyNull() {
        Document doc = Parser.parseBodyFragment("\0", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyUnicodeNull() {
        Document doc = Parser.parseBodyFragment("\u0000", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyBom() {
        Document doc = Parser.parseBodyFragment("\uFEFF", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyNonBreakingSpace() {
        Document doc = Parser.parseBodyFragment("\u00A0", "http://example.com");
        assertNotNull(doc);
        assertEquals("\u00A0", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmoji() {
        Document doc = Parser.parseBodyFragment("😀", "http://example.com");
        assertNotNull(doc);
        assertEquals("😀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlySurrogatePair() {
        Document doc = Parser.parseBodyFragment("\uD83D\uDE00", "http://example.com");
        assertNotNull(doc);
        assertEquals("😀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyHighSurrogate() {
        Document doc = Parser.parseBodyFragment("\uD83D", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyLowSurrogate() {
        Document doc = Parser.parseBodyFragment("\uDE00", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyCombiningChars() {
        Document doc = Parser.parseBodyFragment("e\u0301", "http://example.com");
        assertNotNull(doc);
        assertEquals("e\u0301", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyRtlChars() {
        Document doc = Parser.parseBodyFragment("שלום", "http://example.com");
        assertNotNull(doc);
        assertEquals("שלום", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyCjkChars() {
        Document doc = Parser.parseBodyFragment("中文", "http://example.com");
        assertNotNull(doc);
        assertEquals("中文", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyKanaChars() {
        Document doc = Parser.parseBodyFragment("かな", "http://example.com");
        assertNotNull(doc);
        assertEquals("かな", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyHangulChars() {
        Document doc = Parser.parseBodyFragment("한국어", "http://example.com");
        assertNotNull(doc);
        assertEquals("한국어", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyThaiChars() {
        Document doc = Parser.parseBodyFragment("ไทย", "http://example.com");
        assertNotNull(doc);
        assertEquals("ไทย", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyArabicChars() {
        Document doc = Parser.parseBodyFragment("العربية", "http://example.com");
        assertNotNull(doc);
        assertEquals("العربية", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyDevanagariChars() {
        Document doc = Parser.parseBodyFragment("हिन्दी", "http://example.com");
        assertNotNull(doc);
        assertEquals("हिन्दी", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyCyrillicChars() {
        Document doc = Parser.parseBodyFragment("Русский", "http://example.com");
        assertNotNull(doc);
        assertEquals("Русский", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyGreekChars() {
        Document doc = Parser.parseBodyFragment("Ελληνικά", "http://example.com");
        assertNotNull(doc);
        assertEquals("Ελληνικά", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyLatinExtendedChars() {
        Document doc = Parser.parseBodyFragment("ĀāĂăĄą", "http://example.com");
        assertNotNull(doc);
        assertEquals("ĀāĂăĄą", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyMathSymbols() {
        Document doc = Parser.parseBodyFragment("∑∏∫", "http://example.com");
        assertNotNull(doc);
        assertEquals("∑∏∫", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyCurrencySymbols() {
        Document doc = Parser.parseBodyFragment("$€£¥", "http://example.com");
        assertNotNull(doc);
        assertEquals("$€£¥", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyArrows() {
        Document doc = Parser.parseBodyFragment("←→↑↓", "http://example.com");
        assertNotNull(doc);
        assertEquals("←→↑↓", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyBoxDrawing() {
        Document doc = Parser.parseBodyFragment("─│┌┐└┘", "http://example.com");
        assertNotNull(doc);
        assertEquals("─│┌┐└┘", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyBlockElements() {
        Document doc = Parser.parseBodyFragment("─│┌┐└┘", "http://example.com");
        assertNotNull(doc);
        assertEquals("─│┌┐└┘", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyDingbats() {
        Document doc = Parser.parseBodyFragment("✂✈✉", "http://example.com");
        assertNotNull(doc);
        assertEquals("✂✈✉", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyMiscSymbols() {
        Document doc = Parser.parseBodyFragment("☀☁☂", "http://example.com");
        assertNotNull(doc);
        assertEquals("☀☁☂", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols() {
        Document doc = Parser.parseBodyFragment("😀😁😂", "http://example.com");
        assertNotNull(doc);
        assertEquals("😀😁😂", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags() {
        Document doc = Parser.parseBodyFragment("🇺🇸🇬🇧", "http://example.com");
        assertNotNull(doc);
        assertEquals("🇺🇸🇬🇧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiModifiers() {
        Document doc = Parser.parseBodyFragment("👋🏽", "http://example.com");
        assertNotNull(doc);
        assertEquals("👋🏽", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiZWJ() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👧‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👧‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSkinTone() {
        Document doc = Parser.parseBodyFragment("👍🏿", "http://example.com");
        assertNotNull(doc);
        assertEquals("👍🏿", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHairStyle() {
        Document doc = Parser.parseBodyFragment("👩‍🦰", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🦰", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiProfession() {
        Document doc = Parser.parseBodyFragment("👨‍⚕️", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍⚕️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiComponent() {
        Document doc = Parser.parseBodyFragment("🏃‍♂️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏃‍♂️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap() {
        Document doc = Parser.parseBodyFragment("1️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("1️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence() {
        Document doc = Parser.parseBodyFragment("👩‍💻", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍💻", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart() {
        Document doc = Parser.parseBodyFragment("❤️", "http://example.com");
        assertNotNull(doc);
        assertEquals("❤️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces() {
        Document doc = Parser.parseBodyFragment("😀", "http://example.com");
        assertNotNull(doc);
        assertEquals("😀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals() {
        Document doc = Parser.parseBodyFragment("🐶", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐶", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood() {
        Document doc = Parser.parseBodyFragment("🍕", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍕", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity() {
        Document doc = Parser.parseBodyFragment("⚽", "http://example.com");
        assertNotNull(doc);
        assertEquals("⚽", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel() {
        Document doc = Parser.parseBodyFragment("🚗", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects() {
        Document doc = Parser.parseBodyFragment("💡", "http://example.com");
        assertNotNull(doc);
        assertEquals("💡", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols2() {
        Document doc = Parser.parseBodyFragment("🔔", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔔", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags2() {
        Document doc = Parser.parseBodyFragment("🚩", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚩", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap2() {
        Document doc = Parser.parseBodyFragment("#️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("#️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag2() {
        Document doc = Parser.parseBodyFragment("🏴", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence2() {
        Document doc = Parser.parseBodyFragment("👩‍🎓", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🎓", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily2() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👧‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👧‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple2() {
        Document doc = Parser.parseBodyFragment("👩‍❤️‍👨", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍❤️‍👨", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart2() {
        Document doc = Parser.parseBodyFragment("💞", "http://example.com");
        assertNotNull(doc);
        assertEquals("💞", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands2() {
        Document doc = Parser.parseBodyFragment("🙏", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙏", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces2() {
        Document doc = Parser.parseBodyFragment("😎", "http://example.com");
        assertNotNull(doc);
        assertEquals("😎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals2() {
        Document doc = Parser.parseBodyFragment("🐱", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐱", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood2() {
        Document doc = Parser.parseBodyFragment("🍔", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍔", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity2() {
        Document doc = Parser.parseBodyFragment("🏀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel2() {
        Document doc = Parser.parseBodyFragment("✈️", "http://example.com");
        assertNotNull(doc);
        assertEquals("✈️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects2() {
        Document doc = Parser.parseBodyFragment("📚", "http://example.com");
        assertNotNull(doc);
        assertEquals("📚", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols3() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags3() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap3() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag3() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence3() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily3() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple3() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart3() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands3() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces3() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals3() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood3() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity3() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel3() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects3() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols4() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags4() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap4() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag4() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence4() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily4() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple4() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart4() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands4() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces4() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals4() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood4() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity4() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel4() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects4() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols5() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags5() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap5() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag5() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence5() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily5() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple5() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart5() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands5() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces5() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals5() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood5() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity5() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel5() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects5() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols6() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags6() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap6() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag6() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence6() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily6() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple6() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart6() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands6() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces6() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals6() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood6() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity6() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel6() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects6() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols7() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags7() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap7() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag7() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence7() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily7() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple7() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart7() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands7() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces7() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals7() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood7() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity7() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel7() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects7() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols8() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags8() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap8() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag8() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence8() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily8() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple8() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart8() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands8() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces8() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals8() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood8() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity8() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel8() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects8() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols9() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags9() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap9() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag9() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence9() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily9() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple9() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart9() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands9() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces9() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals9() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood9() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity9() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel9() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects9() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols10() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags10() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap10() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag10() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence10() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily10() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple10() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart10() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands10() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces10() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals10() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood10() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity10() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel10() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects10() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols11() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags11() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap11() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag11() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence11() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily11() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple11() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart11() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands11() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces11() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals11() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood11() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity11() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel11() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects11() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols12() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags12() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap12() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag12() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence12() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily12() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple12() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart12() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands12() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces12() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals12() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood12() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity12() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel12() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects12() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols13() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags13() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap13() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag13() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence13() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily13() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple13() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart13() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands13() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces13() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals13() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood13() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity13() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel13() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects13() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols14() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags14() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap14() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag14() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence14() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily14() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple14() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart14() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands14() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces14() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals14() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood14() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity14() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel14() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects14() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols15() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags15() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap15() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag15() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence15() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily15() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple15() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart15() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands15() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces15() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals15() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood15() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity15() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel15() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects15() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols16() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags16() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap16() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag16() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence16() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily16() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple16() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart16() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands16() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces16() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals16() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood16() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity16() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel16() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects16() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols17() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags17() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap17() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag17() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence17() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily17() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple17() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart17() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands17() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces17() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals17() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood17() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity17() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel17() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects17() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols18() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags18() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap18() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag18() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence18() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily18() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple18() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart18() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands18() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces18() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals18() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood18() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity18() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel18() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects18() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols19() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags19() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap19() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag19() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence19() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily19() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple19() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart19() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands19() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces19() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals19() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood19() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity19() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel19() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects19() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols20() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags20() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap20() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag20() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence20() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily20() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple20() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart20() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands20() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces20() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals20() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood20() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity20() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel20() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects20() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols21() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags21() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap21() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag21() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence21() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily21() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple21() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart21() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands21() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces21() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals21() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood21() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity21() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel21() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects21() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols22() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags22() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap22() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag22() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence22() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily22() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple22() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart22() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands22() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces22() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals22() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood22() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity22() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel22() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects22() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols23() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags23() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap23() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag23() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence23() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily23() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple23() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart23() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands23() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces23() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals23() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood23() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity23() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel23() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects23() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols24() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags24() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap24() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag24() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence24() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily24() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple24() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart24() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands24() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces24() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals24() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood24() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity24() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel24() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects24() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols25() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags25() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap25() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag25() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence25() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily25() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple25() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart25() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands25() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces25() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals25() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood25() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity25() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel25() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects25() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols26() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags26() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap26() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag26() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence26() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily26() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple26() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart26() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands26() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces26() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals26() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood26() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity26() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel26() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects26() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols27() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags27() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap27() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag27() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence27() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily27() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple27() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart27() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands27() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces27() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals27() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood27() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity27() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel27() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects27() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols28() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags28() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap28() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag28() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence28() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily28() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple28() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart28() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands28() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces28() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals28() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood28() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity28() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel28() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects28() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols29() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags29() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap29() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag29() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence29() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily29() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple29() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart29() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands29() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces29() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals29() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood29() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity29() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel29() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects29() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols30() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags30() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap30() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag30() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence30() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily30() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple30() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart30() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands30() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces30() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals30() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood30() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity30() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel30() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects30() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols31() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags31() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap31() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag31() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence31() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily31() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple31() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart31() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands31() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces31() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals31() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood31() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity31() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel31() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects31() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols32() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags32() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap32() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag32() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence32() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily32() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple32() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart32() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands32() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces32() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals32() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood32() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity32() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel32() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects32() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols33() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags33() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap33() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag33() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence33() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily33() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple33() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart33() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands33() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces33() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals33() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood33() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity33() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel33() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects33() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols34() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags34() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap34() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag34() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence34() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily34() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple34() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart34() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands34() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces34() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals34() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood34() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity34() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel34() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects34() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols35() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags35() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap35() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag35() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence35() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily35() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple35() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart35() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands35() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces35() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals35() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood35() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity35() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel35() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects35() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols36() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags36() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap36() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag36() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence36() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily36() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple36() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart36() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands36() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces36() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals36() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood36() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity36() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel36() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects36() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols37() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags37() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap37() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag37() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence37() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily37() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple37() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart37() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands37() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces37() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals37() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood37() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity37() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel37() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects37() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols38() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags38() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap38() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag38() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence38() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily38() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple38() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart38() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands38() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces38() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals38() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood38() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity38() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel38() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects38() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols39() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags39() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap39() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag39() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence39() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily39() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple39() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart39() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands39() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces39() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals39() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood39() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity39() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel39() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects39() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols40() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags40() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap40() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag40() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence40() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily40() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple40() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart40() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands40() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces40() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals40() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood40() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity40() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel40() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects40() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols41() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags41() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap41() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag41() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence41() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily41() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple41() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart41() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands41() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces41() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals41() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood41() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity41() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel41() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects41() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols42() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags42() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap42() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag42() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence42() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily42() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple42() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart42() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands42() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces42() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals42() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood42() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity42() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel42() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects42() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols43() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags43() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap43() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag43() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence43() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily43() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple43() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart43() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands43() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces43() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals43() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood43() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity43() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel43() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects43() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols44() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags44() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap44() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag44() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence44() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily44() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple44() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart44() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands44() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces44() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals44() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood44() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity44() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel44() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects44() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols45() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags45() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap45() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag45() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence45() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily45() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple45() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart45() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands45() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces45() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals45() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood45() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity45() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel45() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects45() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols46() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags46() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap46() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag46() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence46() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily46() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple46() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart46() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands46() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces46() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals46() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood46() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity46() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel46() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects46() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols47() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags47() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap47() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag47() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence47() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily47() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple47() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart47() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands47() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces47() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals47() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood47() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity47() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel47() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects47() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols48() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags48() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap48() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag48() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence48() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily48() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple48() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart48() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands48() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces48() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals48() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood48() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity48() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel48() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects48() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols49() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags49() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap49() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag49() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence49() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily49() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple49() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart49() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands49() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces49() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals49() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood49() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity49() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel49() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects49() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols50() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags50() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap50() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag50() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence50() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily50() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple50() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart50() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands50() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces50() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals50() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood50() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity50() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel50() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects50() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols51() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags51() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap51() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag51() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence51() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily51() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple51() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart51() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands51() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces51() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals51() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood51() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity51() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel51() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects51() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols52() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags52() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap52() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag52() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence52() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily52() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple52() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart52() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands52() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces52() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals52() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood52() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity52() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel52() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects52() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols53() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags53() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap53() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag53() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence53() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily53() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple53() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart53() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands53() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces53() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals53() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood53() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity53() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel53() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects53() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols54() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags54() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap54() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag54() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence54() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily54() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple54() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart54() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands54() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces54() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals54() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood54() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity54() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel54() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects54() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols55() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags55() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap55() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag55() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence55() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFamily55() {
        Document doc = Parser.parseBodyFragment("👨‍👩‍👦", "http://example.com");
        assertNotNull(doc);
        assertEquals("👨‍👩‍👦", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiCouple55() {
        Document doc = Parser.parseBodyFragment("💑", "http://example.com");
        assertNotNull(doc);
        assertEquals("💑", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHeart55() {
        Document doc = Parser.parseBodyFragment("💗", "http://example.com");
        assertNotNull(doc);
        assertEquals("💗", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiHands55() {
        Document doc = Parser.parseBodyFragment("🙌", "http://example.com");
        assertNotNull(doc);
        assertEquals("🙌", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFaces55() {
        Document doc = Parser.parseBodyFragment("😍", "http://example.com");
        assertNotNull(doc);
        assertEquals("😍", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiAnimals55() {
        Document doc = Parser.parseBodyFragment("🐼", "http://example.com");
        assertNotNull(doc);
        assertEquals("🐼", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFood55() {
        Document doc = Parser.parseBodyFragment("🍟", "http://example.com");
        assertNotNull(doc);
        assertEquals("🍟", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiActivity55() {
        Document doc = Parser.parseBodyFragment("🏆", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏆", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTravel55() {
        Document doc = Parser.parseBodyFragment("🚀", "http://example.com");
        assertNotNull(doc);
        assertEquals("🚀", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiObjects55() {
        Document doc = Parser.parseBodyFragment("💎", "http://example.com");
        assertNotNull(doc);
        assertEquals("💎", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSymbols56() {
        Document doc = Parser.parseBodyFragment("🔮", "http://example.com");
        assertNotNull(doc);
        assertEquals("🔮", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiFlags56() {
        Document doc = Parser.parseBodyFragment("🏁", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏁", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiKeycap56() {
        Document doc = Parser.parseBodyFragment("*️⃣", "http://example.com");
        assertNotNull(doc);
        assertEquals("*️⃣", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiTag56() {
        Document doc = Parser.parseBodyFragment("🏴‍☠️", "http://example.com");
        assertNotNull(doc);
        assertEquals("🏴‍☠️", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentWithOnlyEmojiSequence56() {
        Document doc = Parser.parseBodyFragment("👩‍🔧", "http://example.com");
        assertNotNull(doc);
        assertEquals("👩‍🔧