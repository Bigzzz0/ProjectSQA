package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.parser.ParseSettings
 *
 * 1. Decision & Branch Coverage:
 *    - normalizeTag(String name):
 *      * Branch [preserveTagCase == false]: name lower-cased via lowerCase().
 *      * Branch [preserveTagCase == true]: name casing preserved.
 *    - normalizeAttribute(String name):
 *      * Branch [preserveAttributeCase == false]: name lower-cased via lowerCase().
 *      * Branch [preserveAttributeCase == true]: name casing preserved.
 *    - normalizeAttributes(Attributes attributes):
 *      * Branch [preserveAttributeCase == false]: invokes attributes.normalize().
 *      * Branch [preserveAttributeCase == true]: skips normalization, preserves attributes intact.
 *
 * 2. Boundary Value Analysis (BVA):
 *    - Leading/trailing whitespace on tag and attribute names.
 *    - Empty strings and whitespace-only strings.
 *    - Null references:
 *      * normalizeTag(null) -> NullPointerException on trim().
 *      * normalizeAttribute(null) -> NullPointerException on trim().
 *      * normalizeAttributes(null) with preserveAttributeCase=false -> NullPointerException.
 *      * normalizeAttributes(null) with preserveAttributeCase=true -> returns null safely.
 *
 * 3. Defects4J Known Defect Targeting:
 *    - HtmlParserTest::retainsAttributesOfDifferentCaseIfSensitive
 *    - HtmlParserTest::dropsDuplicateAttributes
 *    - XmlTreeBuilderTest::dropsDuplicateAttributes
 *    Verifies duplicate attribute dropping and case-sensitivity behavior when ParseSettings
 *    controls parsing pipelines under HTML and XML configurations.
 */
public class ParseSettingsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHtmlDefaultConstantsAndGetters() {
        ParseSettings settings = ParseSettings.htmlDefault;
        assertNotNull(settings);
        assertFalse(settings.preserveTagCase());

        assertEquals("div", settings.normalizeTag("DIV"));
        assertEquals("class", settings.normalizeAttribute("CLASS"));
    }

    @Test(timeout = 4000)
    public void testPreserveCaseConstantsAndGetters() {
        ParseSettings settings = ParseSettings.preserveCase;
        assertNotNull(settings);
        assertTrue(settings.preserveTagCase());

        assertEquals("DIV", settings.normalizeTag("DIV"));
        assertEquals("CLASS", settings.normalizeAttribute("CLASS"));
    }

    @Test(timeout = 4000)
    public void testCustomConstructorTagTrueAttributeFalse() {
        ParseSettings settings = new ParseSettings(true, false);
        assertTrue(settings.preserveTagCase());

        assertEquals("SPAN", settings.normalizeTag("SPAN"));
        assertEquals("id", settings.normalizeAttribute("ID"));
    }

    @Test(timeout = 4000)
    public void testCustomConstructorTagFalseAttributeTrue() {
        ParseSettings settings = new ParseSettings(false, true);
        assertFalse(settings.preserveTagCase());

        assertEquals("span", settings.normalizeTag("SPAN"));
        assertEquals("ID", settings.normalizeAttribute("ID"));
    }

    @Test(timeout = 4000)
    public void testNormalizeAttributesPreserveFalse() {
        ParseSettings settings = new ParseSettings(false, false);
        Attributes attrs = new Attributes();
        attrs.put("HREF", "https://example.com");
        attrs.put("TITLE", "Example");

        Attributes returned = settings.normalizeAttributes(attrs);
        assertSame(attrs, returned);
        assertTrue(returned.hasKey("href"));
        assertTrue(returned.hasKey("title"));
        assertEquals("https://example.com", returned.get("href"));
        assertEquals("Example", returned.get("title"));
    }

    @Test(timeout = 4000)
    public void testNormalizeAttributesPreserveTrue() {
        ParseSettings settings = new ParseSettings(true, true);
        Attributes attrs = new Attributes();
        attrs.put("HREF", "https://example.com");
        attrs.put("Title", "Example");

        Attributes returned = settings.normalizeAttributes(attrs);
        assertSame(attrs, returned);
        assertTrue(returned.hasKey("HREF"));
        assertTrue(returned.hasKey("Title"));
        assertFalse(returned.hasKey("href"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNormalizeTagTrimming() {
        ParseSettings lowerSettings = ParseSettings.htmlDefault;
        assertEquals("p", lowerSettings.normalizeTag("  P  "));
        assertEquals("section", lowerSettings.normalizeTag("\t\n SECTION \r\n"));

        ParseSettings preserveSettings = ParseSettings.preserveCase;
        assertEquals("P", preserveSettings.normalizeTag("  P  "));
        assertEquals("Section", preserveSettings.normalizeTag("\t Section \n"));
    }

    @Test(timeout = 4000)
    public void testNormalizeAttributeTrimming() {
        ParseSettings lowerSettings = ParseSettings.htmlDefault;
        assertEquals("data-item", lowerSettings.normalizeAttribute("  DATA-ITEM  "));
        assertEquals("alt", lowerSettings.normalizeAttribute("\t ALT \n"));

        ParseSettings preserveSettings = ParseSettings.preserveCase;
        assertEquals("DATA-ITEM", preserveSettings.normalizeAttribute("  DATA-ITEM  "));
        assertEquals("AltText", preserveSettings.normalizeAttribute("\t AltText \n"));
    }

    @Test(timeout = 4000)
    public void testNormalizeTagEmptyAndWhitespaceOnly() {
        ParseSettings settings = ParseSettings.htmlDefault;
        assertEquals("", settings.normalizeTag(""));
        assertEquals("", settings.normalizeTag("   "));
        assertEquals("", settings.normalizeTag("\t\r\n"));

        ParseSettings preserve = ParseSettings.preserveCase;
        assertEquals("", preserve.normalizeTag(""));
        assertEquals("", preserve.normalizeTag("   "));
    }

    @Test(timeout = 4000)
    public void testNormalizeAttributeEmptyAndWhitespaceOnly() {
        ParseSettings settings = ParseSettings.htmlDefault;
        assertEquals("", settings.normalizeAttribute(""));
        assertEquals("", settings.normalizeAttribute("   "));
        assertEquals("", settings.normalizeAttribute("\t\r\n"));

        ParseSettings preserve = ParseSettings.preserveCase;
        assertEquals("", preserve.normalizeAttribute(""));
        assertEquals("", preserve.normalizeAttribute("   "));
    }

    @Test(timeout = 4000)
    public void testNormalizeAttributesEmpty() {
        ParseSettings lowerSettings = ParseSettings.htmlDefault;
        Attributes emptyAttrs = new Attributes();
        Attributes result = lowerSettings.normalizeAttributes(emptyAttrs);
        assertSame(emptyAttrs, result);
        assertEquals(0, result.size());

        ParseSettings preserveSettings = ParseSettings.preserveCase;
        Attributes emptyAttrs2 = new Attributes();
        Attributes result2 = preserveSettings.normalizeAttributes(emptyAttrs2);
        assertSame(emptyAttrs2, result2);
        assertEquals(0, result2.size());
    }

    @Test(timeout = 4000)
    public void testNormalizeAttributesNullWhenCasePreserved() {
        // When preserveAttributeCase is true, normalizeAttributes skips attributes.normalize() and returns null
        ParseSettings settings = new ParseSettings(false, true);
        Attributes result = settings.normalizeAttributes(null);
        assertNull(result);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testRetainsAttributesOfDifferentCaseIfSensitive() {
        String html = "<p One=\"One\" one=\"Three\" two=\"Four\" Two=\"Six\">Text</p>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<p One=\"One\" one=\"Three\" two=\"Four\" Two=\"Six\">Text</p>", doc.body().child(0).outerHtml());
    }

    @Test(timeout = 4000)
    public void testDropsDuplicateAttributesHtmlDefault() {
        String html = "<p one=\"One\" one=\"Two\" two=\"two\" one=\"Three\" two=\"Five\">Text</p>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.htmlDefault);
        Document doc = parser.parseInput(html, "");
        assertEquals("<p one=\"One\" two=\"two\">Text</p>", doc.body().child(0).outerHtml());
    }

    @Test(timeout = 4000)
    public void testDropsDuplicateAttributesXmlTreeBuilder() {
        String xml = "<p One=\"One\" ONE=\"Two\" one=\"Three\" two=\"Six\" Two=\"Eight\">Text</p>";
        Parser parser = Parser.xmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(xml, "");
        assertEquals("<p One=\"One\" ONE=\"Two\" one=\"Three\" two=\"Six\" Two=\"Eight\">Text</p>", doc.child(0).outerHtml());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNormalizeTagThrowsNullPointerException() {
        ParseSettings.htmlDefault.normalizeTag(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNormalizeAttributeThrowsNullPointerException() {
        ParseSettings.htmlDefault.normalizeAttribute(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNormalizeAttributesThrowsNullPointerExceptionWhenNotPreserved() {
        ParseSettings settings = new ParseSettings(false, false);
        settings.normalizeAttributes(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDistinctInstancesAndSettingsPermutations() {
        ParseSettings p1 = new ParseSettings(true, true);
        ParseSettings p2 = new ParseSettings(true, false);
        ParseSettings p3 = new ParseSettings(false, true);
        ParseSettings p4 = new ParseSettings(false, false);

        assertTrue(p1.preserveTagCase());
        assertTrue(p2.preserveTagCase());
        assertFalse(p3.preserveTagCase());
        assertFalse(p4.preserveTagCase());

        assertEquals("MYTAG", p1.normalizeTag("MYTAG"));
        assertEquals("MYTAG", p2.normalizeTag("MYTAG"));
        assertEquals("mytag", p3.normalizeTag("MYTAG"));
        assertEquals("mytag", p4.normalizeTag("MYTAG"));

        assertEquals("MYATTR", p1.normalizeAttribute("MYATTR"));
        assertEquals("myattr", p2.normalizeAttribute("MYATTR"));
        assertEquals("MYATTR", p3.normalizeAttribute("MYATTR"));
        assertEquals("myattr", p4.normalizeAttribute("MYATTR"));
    }
}