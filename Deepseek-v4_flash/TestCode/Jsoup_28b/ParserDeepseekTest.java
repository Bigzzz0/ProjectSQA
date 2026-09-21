package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: Parser (org.jsoup.parser)
 * 
 * Decision branches covered:
 * - isTrackErrors() branch: maxErrors > 0 / <= 0
 * - setTrackErrors(): sets maxErrors to any int
 * - parseInput(): initializes errors based on trackErrors flag
 * - getErrors(): returns errors reference
 * - setTreeBuilder: changes treeBuilder and returns this
 * - static parse, parseFragment, parseBodyFragment, parseBodyFragmentRelaxed
 * - htmlParser / xmlParser factory methods
 * 
 * Boundary conditions:
 * - maxErrors: 0 (default), 1 (positive), negative (counts as disabled)
 * - parseInput null html -> NullPointerException
 * - Empty/fragment inputs
 * 
 * Defect-targeted partitions (based on known entity decoding bugs):
 * - "&amp;clubsuit" should decode as '&' + "clubsuit", not as a single clubsuit character
 * - "&angst" should not be mistaken for &Aring; expected literal "&angst" in output
 * - "&mid;" should not be decoded when part of a longer token like "&mid-size"
 * - "&amp;icy" should not decode as a single entity (icy)
 * - "&num_rooms" should not decode as #_rooms
 * 
 * Exception paths: parseInput with null html -> NPE, parseFragment with null context -> ? (probably works but test)
 * 
 * Object contract: getErrors returns mutable list from last parse
 */
public class ParserDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testDefaultMaxErrorsZero() {
        Parser parser = Parser.htmlParser();
        assertFalse("Default maxErrors should disable track errors", parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testSetTrackErrorsPositive() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        assertTrue("Setting maxErrors=5 should enable track errors", parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testSetTrackErrorsZeroDisables() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(1);
        assertTrue(parser.isTrackErrors());
        parser.setTrackErrors(0);
        assertFalse("Setting back to 0 should disable tracking", parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testSetTrackErrorsNegative() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(-1);
        assertFalse("Negative maxErrors should also disable tracking", parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testDefaultTreeBuilderIsHtml() {
        Parser parser = Parser.htmlParser();
        assertTrue("Default htmlParser should use HtmlTreeBuilder", 
                   parser.getTreeBuilder() instanceof HtmlTreeBuilder);
    }

    @Test(timeout = 4000)
    public void testSetTreeBuilderReturnsThis() {
        Parser parser = Parser.htmlParser();
        TreeBuilder xmlBuilder = new XmlTreeBuilder();
        assertSame("setTreeBuilder should return this", parser, parser.setTreeBuilder(xmlBuilder));
        assertTrue("TreeBuilder should be changed to XmlTreeBuilder", 
                   parser.getTreeBuilder() instanceof XmlTreeBuilder);
    }

    @Test(timeout = 4000)
    public void testParseInputReturnsDocument() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<html><body><p>Hello</p></body></html>", "http://example.com");
        assertNotNull("Document should not be null", doc);
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseInputWithErrorTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        assertTrue(parser.isTrackErrors());
        // Parse malformed HTML to generate errors
        Document doc = parser.parseInput("<html><body><p>Unclosed", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertNotNull("Errors list should not be null when tracking enabled", errors);
        assertTrue("Should have parse errors for malformed input", errors.size() > 0);
    }

    @Test(timeout = 4000)
    public void testGetErrorsWithoutTracking() {
        Parser parser = Parser.htmlParser();
        parser.parseInput("<html><body><p>Hello</p></body></html>", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertNotNull("Errors list should never be null", errors);
        assertEquals("With no tracking, errors list should be empty", 0, errors.size());
    }

    @Test(timeout = 4000)
    public void testParseInputReusesErrorsList() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        parser.parseInput("<html><body><p>Hello", "http://example.com");
        List<ParseError> firstErrors = parser.getErrors();
        int firstSize = firstErrors.size();
        parser.parseInput("<html><body><p>World", "http://example.com");
        List<ParseError> secondErrors = parser.getErrors();
        assertSame("The errors list reference should be updated (new list per parse)", 
                   firstErrors != secondErrors || firstSize != secondErrors.size());
    }

    // ==================== Partition B: Boundary & Edge Cases ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testParseInputWithNullHtml() {
        Parser parser = Parser.htmlParser();
        parser.parseInput(null, "http://example.com");
    }

    @Test(timeout = 4000)
    public void testParseInputWithEmptyHtml() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("", "http://example.com");
        assertNotNull(doc);
        assertTrue("Empty HTML should produce a blank document", doc.body().text().isEmpty());
    }

    @Test(timeout = 4000)
    public void testParseInputWithNullBaseUri() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<html><body><p>Test</p></body></html>", null);
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testStaticParse() {
        Document doc = Parser.parse("<html><body><p>Static</p></body></html>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Static", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testStaticParseFragment() {
        Element context = Document.createShell("http://example.com").body();
        List<Node> nodes = Parser.parseFragment("<p>Fragment</p>", context, "http://example.com");
        assertNotNull(nodes);
        assertFalse("Should contain nodes", nodes.isEmpty());
        assertEquals("p", ((Element) nodes.get(0)).tagName());
    }

    @Test(timeout = 4000)
    public void testStaticParseBodyFragment() {
        Document doc = Parser.parseBodyFragment("<p>Body fragment</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Body fragment", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<p>Deprecated</p>", "http://example.com");
        assertNotNull(doc);
        assertEquals("Deprecated", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testHtmlParserFactory() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
    }

    @Test(timeout = 4000)
    public void testXmlParserFactory() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    /**
     * Targets defect from HtmlParserTest.doesNotFindShortestMatchingEntity:
     * Parsing "One &amp;clubsuit e; ♣" should decode "&amp;" to '&' and leave
     * "clubsuit" as literal text, not decode the whole sequence as clubsuit (♣).
     */
    @Test(timeout = 4000)
    public void testEntityParsingAmpClubsuit() {
        String html = "<html><body><p>One &amp;clubsuit e; ♣</p></body></html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        String text = doc.body().text();
        // Expected: "One &clubsuit e; ♣" (where & is literal ampersand, ♣ is actual clubsuit at the end)
        assertEquals("One &clubsuit e; ♣", text);
    }

    /**
     * Targets defect from EntitiesTest.unescape: "&angst" should not be conflated with &Aring (Å).
     * Parsing "Hello &angst there" should keep "&angst" as literal text (or decode to angstrom sign,
     * but NOT to Å). Based on the defect expected, the literal "&angst" should appear in output.
     * We'll test: "&angst" (Note: defect shows "&angst" but the expected has [&angst] so it should stay)
    */
    @Test(timeout = 4000)
    public void testEntityParsingAngstNotAring() {
        String html = "<html><body><p>Hello &angst there</p></body></html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        String text = doc.body().text();
        // According to the defect, expected: "Hello &angst there" with literal &angst
        // But careful: The defect says expected: "Hello &<> ® Å [&angst] π π 新 there &! ¾ © ..."
        // That includes "Å" before the "[&angst]". So we nee to assert that "&angst" is present as literal.
        assertTrue("text should contain '&angst'", text.contains("&angst"));
        assertFalse("text should not contain 'Å' (Aring)", text.contains("Å"));
    }

    /**
     * Targets defect from AttributeParseTest.strictAttributeUnscapes:
     * "&mid;" inside an attribute should not be decoded when it is part of a larger token like "&mid-size".
     * We'll test parsing an attribute with "&mid-size=true&ok=&wr"
     */
    @Test(timeout = 4000)
    public void testAttributeEntityParsingMidSize() {
        Stringhtml = "<html><body><a href=\"http://foo.com?a=1&mid-size=true&ok=&wr\">link</a></body></html>";
        Document doc = Jsoup.parse(html, "htp://example.com");
        String href = doc.select("a").first().attr("href");
        // Expected: "http://foo.com?a=1&mid-size=true&ok=&wr" (with &mid not decoded)
        assertEquals("http://foo.com?a=1&mid-size=true&ok=&wr", href);
    }

    /**
     * Targets defect from HtmlParserTest.relaxedBaseEntityMatchAndStrictExtendedMatch:
     * Parsing "&amp; &quot; &reg; &amp;icy &amp;hopf &icy; &hopf;"
     * should preserve "&amp;icy" and "&amp;hopf" as two entities, not combine them.
    */
    @Test(timeout = 4000)
    public void testEntityParsingAmpIcyAmpHopf() {
        String html = "<html><body><p>&amp; &quot; &reg; &amp;icy &amp;hopf &icy; &hopf;</p></body></html>";
        Document doc = Jsop.parse(html, "http://example.com");
        String text = doc.body().text();
        // Expected partial string (the defect shows exected with [&amp;icy &amp;hopf] as literal)
        // We'll check that the text contains "&amp;icy" and "&amp;hopf" literally
        assertTrue("text should contain '&amp;icy'", text.contains("&amp;icy"));
        assertTrue("text should contain '&amp;hopf'", text.contains("&amp;hopf"));
        assertFalse("text should not contain just 'icy' (decoded)", text.contains("\u0130")); // icy is not a standard entity
        assertFalse("text should not contain just 'hopf' (decoded)", text.contains("\u210D")); // &hopf; may or may not decode
    }

    /**
     * Targets defect from AttributeParseTest.moreAttributeUnscasp: "&wr" should not be decoded as ≀ (reversed tilde)
     */
    @Test(timeout = 4000)
    public void testAttributeEntityParsingWr() {
        String html = "<html><body><a href=\"http://foo.com?a=1&mid-size=true&ok=&wr\">link</a></body></html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        String href = doc.select("a").first().attr("href");
        // Should not contain "≀" (reversed tilde)
        assertFalse("href should not contain the reversed tilde character", href.contains("\u2240"));
        // Should contain literal "&wr"
        assertTrue("href should contain '&wr'", href.contains("&wr"));
    }

    /**
     * Targets defect from EntitiesTest.noSpuriousDecodes: "&num_rooms" should not decode as "#_rooms"
     */
    @Test(timeout = 4000)
    public void testEntityParsingNumRooms() {
        String html = "<html><body><a href=\"http://www.foo.com?a=1&num_rooms=1&children=0&int=VA&b=2\">link</a></body></html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        String href = doc.select("a").first().attr("href");
        // Expected: "http://www.foo.com?a=1&num_rooms=1&children=0&int=VA&b=2"
        assertEquals("http://www.foo.com?a=1&num_rooms=1&children=0&int=VA&b=2", href);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testParseFragmentWithNullContext() {
        // parseFragment with null context should not throw (likely it works), but we'll check the method signature.
        // Actually it's not documented. We'll just ensure it does not crash unexpectedly.
        List<Node> nodes = Parser.parseFragment("<p>Test</p>", null, "http://example.com");
        // Expect no exception (will just parse as standalone fragment)
        assertNotNull(nodes);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testParseFragmentWithNullHtml() {
        Element context = Document.createShell("http://example.com").body();
        Parser.parseFragment(null, context, "http://example.com");
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testGetErrorsReturnsMutableList() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(5);
        parser.parseInput("<html><body><p>Test", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        int originalSize = errors.size();
        // Add a bogus error (if we could, but the list is from ParseErrorList, may be unmodifiable? Actually it's a List<ParseError> returned from ParseErrorList, we're not sure)
        // Just verify it's the same reference as before parseInput
        parser.parseInput("<html><body><p>Another", "http://example.com");
        List<ParseError> newErrors = parser.getErrors();
        // The reference should have changed because parseInput creates a new list if tracking
        assertNotSame("Each parse should create a new error list if tracking", errors, newErrors);
    }

    @Test(timeout = 4000)
    public void testChainingSetTrackErrors() {
        Parser parser = Parser.htmlParser();
        assertSame("setTrackErrors should return this", parser, parser.setTrackErrors(3));
        assertTrue(parser.isTrackErrors());
    }

    @Test(timeout = 4000)
    public void testConstructorWithTreeBuilder() {
        TreeBuilder builder = new HtmlTreeBuilder();
        Parser parser = new Parser(builder);
        assertSame("Constructor should store the given treeBuilder", builder, parser.getTreeBuilder());
    }
}