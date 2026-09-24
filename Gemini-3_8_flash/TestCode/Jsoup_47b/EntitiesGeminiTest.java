package org.jsoup.nodes;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Entities
 *
 * Decision / Condition Matrix:
 * 1. isNamedEntity(name): full.containsKey(name) -> [True: standard entities like 'lt', 'amp', False: unknown, empty, null]
 * 2. isBaseNamedEntity(name): base.containsKey(name) -> [True: base entities, False: extended-only or unknown]
 * 3. getCharacterByName(name): full.get(name) -> [Found: Character, Not found: null]
 * 4. normaliseWhite:
 *    - isWhitespace: (stripLeadingWhite && !reachedNonWhite) -> skip leading whitespace
 *    - isWhitespace: lastWasWhite -> collapse consecutive spaces
 *    - isWhitespace: normal space -> append ' '
 *    - non-whitespace: reset lastWasWhite, set reachedNonWhite
 * 5. codePoint < MIN_SUPPLEMENTARY_CODE_POINT (BMP vs Supplementary):
 *    - BMP char:
 *      - case '&': always '&amp;'
 *      - case 0xA0 (NBSP): escapeMode != xhtml -> '&nbsp;', escapeMode == xhtml -> '&#xa0;'
 *      - case '<': !inAttribute -> '&lt;', inAttribute -> '<' (Defect Target: should escape in XML attributes)
 *      - case '>': !inAttribute -> '&gt;', inAttribute -> '>'
 *      - case '"': inAttribute -> '&quot;', !inAttribute -> '"'
 *      - default:
 *        - canEncode (CoreCharset: ascii, utf, fallback) -> append char as-is
 *        - !canEncode && map.containsKey(c) -> append '&' + entity + ';'
 *        - !canEncode && !map.containsKey(c) -> append '&#x' + hex + ';'
 *    - Supplementary char (Surrogates / > 0xFFFF):
 *      - encoder.canEncode(c) -> append raw characters
 *      - !encoder.canEncode(c) -> append '&#x' + hex + ';'
 * 6. CoreCharset.byName:
 *    - US-ASCII -> ascii (c < 0x80)
 *    - UTF-* -> utf (always true)
 *    - Other (e.g. ISO-8859-1) -> fallback (encoder.canEncode(c))
 * 7. unescape(string, strict):
 *    - strict=true vs strict=false handling for unescaped entity strings
 * 8. Defect Targeted:
 *    - escapesGtInXmlAttributesButNotInHtml: XML attributes must properly escape markup tags.
 */
public class EntitiesGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNamedEntityStandardAndNonexistent() {
        assertTrue("Standard entity 'lt' must be recognized", Entities.isNamedEntity("lt"));
        assertTrue("Standard entity 'amp' must be recognized", Entities.isNamedEntity("amp"));
        assertTrue("Standard entity 'gt' must be recognized", Entities.isNamedEntity("gt"));
        assertTrue("Standard entity 'quot' must be recognized", Entities.isNamedEntity("quot"));
        assertTrue("Extended entity 'euro' must be recognized", Entities.isNamedEntity("euro"));
        assertFalse("Non-existent entity must return false", Entities.isNamedEntity("nonExistentEntityXYZ123"));
        assertFalse("Empty string should not be a named entity", Entities.isNamedEntity(""));
    }

    @Test(timeout = 4000)
    public void testIsBaseNamedEntity() {
        assertTrue("Base entity 'lt' must be in base set", Entities.isBaseNamedEntity("lt"));
        assertTrue("Base entity 'amp' must be in base set", Entities.isBaseNamedEntity("amp"));
        assertTrue("Base entity 'gt' must be in base set", Entities.isBaseNamedEntity("gt"));
        assertTrue("Base entity 'quot' must be in base set", Entities.isBaseNamedEntity("quot"));
        assertTrue("Base entity 'nbsp' must be in base set", Entities.isBaseNamedEntity("nbsp"));

        // 'notinbase' or extended-only entity like 'approx' or arbitrary names
        assertFalse("Extended-only entity must not be in base set", Entities.isBaseNamedEntity("approx"));
        assertFalse("Non-existent entity must not be in base set", Entities.isBaseNamedEntity("bogusEntity"));
    }

    @Test(timeout = 4000)
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertEquals(Character.valueOf((char) 160), Entities.getCharacterByName("nbsp"));
        assertNull("Non-existent entity lookup should return null", Entities.getCharacterByName("noSuchEntityExists"));
    }

    @Test(timeout = 4000)
    public void testEscapeModesAndMaps() {
        assertEquals(3, Entities.EscapeMode.values().length);
        assertSame(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertSame(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
        assertSame(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));

        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());

        assertEquals("quot", Entities.EscapeMode.xhtml.getMap().get('"'));
        assertEquals("amp", Entities.EscapeMode.xhtml.getMap().get('&'));
        assertEquals("lt", Entities.EscapeMode.xhtml.getMap().get('<'));
        assertEquals("gt", Entities.EscapeMode.xhtml.getMap().get('>'));
    }

    @Test(timeout = 4000)
    public void testBasicEscapingUtfOutput() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");
        settings.escapeMode(Entities.EscapeMode.base);

        String raw = "<div>Hello & \"World\" <123>!</div>";
        String escaped = Entities.escape(raw, settings);
        assertEquals("&lt;div&gt;Hello &amp; \"World\" &lt;123&gt;!&lt;/div&gt;", escaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeBasicAndStrict() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<tag> & \"hello\"", Entities.unescape("&lt;tag&gt; &amp; &quot;hello&quot;"));
        assertEquals(" ", Entities.unescape("&#x20;"));
        assertEquals("\u00A0", Entities.unescape("&nbsp;"));

        // strict vs non-strict unescaping
        assertEquals("&amp", Entities.unescape("&amp", true));
        assertEquals("&", Entities.unescape("&amp", false));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        Document.OutputSettings settings = new Document.OutputSettings();
        assertEquals("", Entities.escape("", settings));
    }

    @Test(timeout = 4000)
    public void testEscapeWhitespaceNormalisationVariations() {
        Document.OutputSettings settings = new Document.OutputSettings();

        // 1. stripLeadingWhite = true, normaliseWhite = true
        StringBuilder sb1 = new StringBuilder();
        Entities.escape(sb1, "   \t\n  hello   world  \n ", settings, false, true, true);
        assertEquals("hello world ", sb1.toString());

        // 2. stripLeadingWhite = false, normaliseWhite = true
        StringBuilder sb2 = new StringBuilder();
        Entities.escape(sb2, "   hello   world ", settings, false, true, false);
        assertEquals(" hello world ", sb2.toString());

        // 3. normaliseWhite = false
        StringBuilder sb3 = new StringBuilder();
        Entities.escape(sb3, "   hello   world \n ", settings, false, false, false);
        assertEquals("   hello   world \n ", sb3.toString());

        // 4. all whitespace with stripLeadingWhite = true
        StringBuilder sb4 = new StringBuilder();
        Entities.escape(sb4, "     \t \n  ", settings, false, true, true);
        assertEquals("", sb4.toString());

        // 5. all whitespace with stripLeadingWhite = false
        StringBuilder sb5 = new StringBuilder();
        Entities.escape(sb5, "     \t \n  ", settings, false, true, false);
        assertEquals(" ", sb5.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeQuotesInAndOutOfAttribute() {
        Document.OutputSettings settings = new Document.OutputSettings();

        StringBuilder inAttr = new StringBuilder();
        Entities.escape(inAttr, "\"Hello\"", settings, true, false, false);
        assertEquals("&quot;Hello&quot;", inAttr.toString());

        StringBuilder outAttr = new StringBuilder();
        Entities.escape(outAttr, "\"Hello\"", settings, false, false, false);
        assertEquals("\"Hello\"", outAttr.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeNbspAcrossModes() {
        Document.OutputSettings settings = new Document.OutputSettings();

        // Mode: base
        settings.escapeMode(Entities.EscapeMode.base);
        assertEquals("&nbsp;", Entities.escape("\u00A0", settings));

        // Mode: extended
        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals("&nbsp;", Entities.escape("\u00A0", settings));

        // Mode: xhtml
        settings.escapeMode(Entities.EscapeMode.xhtml);
        assertEquals("&#xa0;", Entities.escape("\u00A0", settings));
    }

    @Test(timeout = 4000)
    public void testEscapeCoreCharsetBranches() {
        // 1. US-ASCII: CoreCharset.ascii
        Document.OutputSettings asciiSettings = new Document.OutputSettings();
        asciiSettings.charset("US-ASCII");
        asciiSettings.escapeMode(Entities.EscapeMode.base);

        // 'a' is < 0x80 -> direct
        // '\u00e9' (é) -> mapped entity &eacute; in base
        // '\u03c0' (π) -> not in base map -> hex &#x3c0;
        String asciiResult = Entities.escape("a \u00e9 \u03c0", asciiSettings);
        assertEquals("a &eacute; &#x3c0;", asciiResult);

        // 2. Fallback charset: ISO-8859-1 (covers Latin-1 directly, but not higher Unicode)
        Document.OutputSettings isoSettings = new Document.OutputSettings();
        isoSettings.charset("ISO-8859-1");
        isoSettings.escapeMode(Entities.EscapeMode.base);

        // '\u00e9' can be encoded directly by ISO-8859-1
        // '\u2603' (snowman) cannot be encoded by ISO-8859-1, nor is it in base map -> hex &#x2603;
        String isoResult = Entities.escape("a \u00e9 \u2603", isoSettings);
        assertEquals("a \u00e9 &#x2603;", isoResult);

        // 3. Fallback charset with extended map match
        Document.OutputSettings isoExtended = new Document.OutputSettings();
        isoExtended.charset("ISO-8859-1");
        isoExtended.escapeMode(Entities.EscapeMode.extended);
        // '\u2200' (forall ∀) cannot be encoded in ISO-8859-1, but is in extended map
        String forAllResult = Entities.escape("\u2200", isoExtended);
        assertEquals("&forall;", forAllResult);
    }

    @Test(timeout = 4000)
    public void testEscapeSupplementaryCodePoints() {
        // Supplementary character: U+1F600 (GRINNING FACE 😀), codePoint = 128512
        String emoji = new String(Character.toChars(0x1F600));

        // 1. UTF-8 encoder can encode emoji directly
        Document.OutputSettings utfSettings = new Document.OutputSettings();
        utfSettings.charset("UTF-8");
        assertEquals(emoji, Entities.escape(emoji, utfSettings));

        // 2. US-ASCII encoder cannot encode emoji -> hex escape
        Document.OutputSettings asciiSettings = new Document.OutputSettings();
        asciiSettings.charset("US-ASCII");
        assertEquals("&#x1f600;", Entities.escape(emoji, asciiSettings));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets the known defect where '<' and '>' inside attribute values in XML syntax
     * must be escaped (at least '<' to '&lt;') to produce valid XML attributes.
     * Defects4J Ground Truth:
     * - org.jsoup.nodes.EntitiesTest::escapesGtInXmlAttributesButNotInHtml
     *   expected:<<a title="[&lt;p>One&lt;]/p>">One</a>> but was:<<a title="[<p>One<]/p>">One</a>>
     */
    @Test(timeout = 4000)
    public void escapesGtInXmlAttributesButNotInHtml() {
        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        doc.outputSettings().escapeMode(Entities.EscapeMode.base);

        Element a = doc.body().appendElement("a");
        a.attr("title", "<p>One</p>");
        a.text("One");

        assertEquals("<a title=\"&lt;p>One&lt;/p>\">One</a>", a.outerHtml());
    }

    @Test(timeout = 4000)
    public void testXmlAttributeEscapingDirect() {
        Document.OutputSettings xmlSettings = new Document.OutputSettings();
        xmlSettings.syntax(Document.OutputSettings.Syntax.xml);
        xmlSettings.escapeMode(Entities.EscapeMode.xhtml);

        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "<tag value=\"foo\">", xmlSettings, true, false, false);

        // In XML attributes, '<' must strictly be escaped to '&lt;'
        assertTrue("Opening angle brackets inside XML attribute must be escaped to &lt;",
                accum.toString().contains("&lt;tag"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullNamedEntityLookups() {
        assertFalse("Null entity name should return false", Entities.isNamedEntity(null));
        assertFalse("Null base entity name should return false", Entities.isBaseNamedEntity(null));
        assertNull("Null entity name should return null character", Entities.getCharacterByName(null));
    }

    @Test(timeout = 4000)
    public void testFallbackCharsetWithSpecialUnmappableChars() {
        // Test encoder fallback when a character is above ASCII, not in ISO-8859-1, and not in map
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("ISO-8859-1"));
        settings.escapeMode(Entities.EscapeMode.xhtml); // xhtml map only has lt, gt, amp, quot

        // '\u0100' is Latin Capital Letter A with Macron (not in ISO-8859-1, not in xhtml map)
        StringBuilder sb = new StringBuilder();
        Entities.escape(sb, "\u0100", settings, false, false, false);
        assertEquals("&#x100;", sb.toString());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorViaReflection() throws Exception {
        Constructor<Entities> constructor = Entities.class.getDeclaredConstructor();
        assertTrue("Entities constructor should be private",
                java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Entities instance = constructor.newInstance();
        assertNotNull("Reflective instantiation of utility class should succeed", instance);
    }

    @Test(timeout = 4000)
    public void testDuplicateCaseEntityResolution() {
        // Base and Full entities have casing variants like &AMP; and &amp;
        // The reverse map must prefer the lowercase entity name
        Character ampChar = '&';
        String resolvedBaseName = Entities.EscapeMode.base.getMap().get(ampChar);
        assertEquals("amp", resolvedBaseName);

        Character copyChar = (char) 169;
        String resolvedExtendedName = Entities.EscapeMode.extended.getMap().get(copyChar);
        assertEquals("copy", resolvedExtendedName);
    }
}