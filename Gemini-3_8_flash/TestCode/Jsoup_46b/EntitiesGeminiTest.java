/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.nodes.Entities
 *
 * Method / Decision Points Analyzed:
 * 1. isNamedEntity(String name):
 *    - Branch: full.containsKey(name) -> true / false.
 * 2. isBaseNamedEntity(String name):
 *    - Branch: base.containsKey(name) -> true / false.
 * 3. getCharacterByName(String name):
 *    - Branch: full.get(name) -> Character / null.
 * 4. escape(StringBuilder, String, OutputSettings, boolean, boolean, boolean):
 *    - Normalise whitespace:
 *      * StringUtil.isWhitespace(codePoint):
 *        - stripLeadingWhite && !reachedNonWhite -> skip leading whitespaces.
 *        - lastWasWhite -> collapse consecutive whitespaces into a single space.
 *        - first whitespace after non-white -> append ' ', set lastWasWhite = true.
 *      * non-whitespace: reset lastWasWhite = false, reachedNonWhite = true.
 *    - Single-char vs Supplementary code points (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT):
 *      * Single-char switch (c):
 *        - '&'  -> "&amp;"
 *        - 0xA0 (NBSP):
 *          - escapeMode != EscapeMode.xhtml -> "&nbsp;"
 *          - escapeMode == EscapeMode.xhtml -> [DEFECT ZONE: defective code directly appends 'c' (\u00a0)
 *            even when the charset (e.g. Shift_JIS, US-ASCII) cannot encode \u00a0, leading to '?' when encoded].
 *        - '<'  -> !inAttribute ? "&lt;" : '<'
 *        - '>'  -> !inAttribute ? "&gt;" : '>'
 *        - '"'  -> inAttribute ? "&quot;" : '"'
 *        - default:
 *          - canEncode(coreCharset, c, encoder) -> append c directly
 *          - !canEncode && map.containsKey(c)   -> append '&' + map.get(c) + ';'
 *          - !canEncode && !map.containsKey(c)  -> append "&#x" + hex + ';'
 *      * Supplementary code points (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT):
 *        - encoder.canEncode(c) -> append string
 *        - !encoder.canEncode(c) -> append "&#x" + hex + ';'
 * 5. canEncode(CoreCharset, char, CharsetEncoder):
 *    - CoreCharset.ascii: c < 0x80 -> true / false
 *    - CoreCharset.utf: always true
 *    - CoreCharset.fallback: fallback.canEncode(c) -> true / false (e.g., ISO-8859-1, Shift_JIS, Windows-1252)
 * 6. CoreCharset.byName(String):
 *    - name.equals("US-ASCII") -> ascii
 *    - name.startsWith("UTF-") -> utf
 *    - other -> fallback
 * 7. unescape(String) and unescape(String, boolean strict):
 *    - delegates to Parser.unescapeEntities(string, strict)
 * 8. EscapeMode enum & Map:
 *    - EscapeMode.xhtml, base, extended and their mappings
 *
 * Defects4J Bug Target:
 * - DocumentTest::testShiftJisRoundtrip:
 *   When charset is Shift_JIS or US-ASCII and escapeMode is xhtml, character 0xA0 (nbsp)
 *   cannot be encoded into the charset. Defective Entities.escape unconditionally appends
 *   0xA0 for xhtml, which encodes to '?' in Shift_JIS/US-ASCII instead of producing a numeric
 *   character reference (&#xa0; or &#160;).
 */

package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Map;

import static org.junit.Assert.*;

public class EntitiesGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNamedEntityPositiveAndNegative() {
        assertTrue("Expected 'lt' to be a recognized named entity", Entities.isNamedEntity("lt"));
        assertTrue("Expected 'amp' to be a recognized named entity", Entities.isNamedEntity("amp"));
        assertTrue("Expected 'gt' to be a recognized named entity", Entities.isNamedEntity("gt"));
        assertTrue("Expected 'quot' to be a recognized named entity", Entities.isNamedEntity("quot"));
        assertTrue("Expected 'copy' to be recognized in full entities", Entities.isNamedEntity("copy"));

        assertFalse("Expected non-existent entity name to return false", Entities.isNamedEntity("nonExistentEntityXYZ"));
        assertFalse("Expected empty entity name to return false", Entities.isNamedEntity(""));
    }

    @Test(timeout = 4000)
    public void testIsBaseNamedEntityPositiveAndNegative() {
        assertTrue("Expected 'lt' to be in base entity set", Entities.isBaseNamedEntity("lt"));
        assertTrue("Expected 'gt' to be in base entity set", Entities.isBaseNamedEntity("gt"));
        assertTrue("Expected 'amp' to be in base entity set", Entities.isBaseNamedEntity("amp"));
        assertTrue("Expected 'quot' to be in base entity set", Entities.isBaseNamedEntity("quot"));

        assertFalse("Expected 'notin' to not be in base entity set", Entities.isBaseNamedEntity("notin"));
        assertFalse("Expected unknown entity to return false", Entities.isBaseNamedEntity("randomBogus"));
    }

    @Test(timeout = 4000)
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertEquals(Character.valueOf('\u00A9'), Entities.getCharacterByName("copy"));
        assertNull(Entities.getCharacterByName("nonExistentEntityXYZ"));
    }

    @Test(timeout = 4000)
    public void testEscapeStandardCharactersDefaultSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "Hello & <world> \" ' \u00A0 \u00A9";
        String escaped = Entities.escape(input, settings);

        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&lt;"));
        assertTrue(escaped.contains("&gt;"));
        assertTrue(escaped.contains("&nbsp;"));
        // Under UTF-8, characters encodable by charset are preserved if not specifically escaped
        assertEquals("Hello &amp; &lt;world&gt; \" ' &nbsp; \u00A9", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeInAttributeMode() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder accum = new StringBuilder();

        // In attribute: '"' should be escaped as &quot;, while '<' and '>' should remain intact
        String input = "<tag attr=\"hello & goodbye\">";
        Entities.escape(accum, input, settings, true, false, false);

        String result = accum.toString();
        assertEquals("<tag attr=&quot;hello &amp; goodbye&quot;>", result);
    }

    @Test(timeout = 4000)
    public void testEscapeNotInAttributeMode() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder accum = new StringBuilder();

        // Not in attribute: '<' and '>' should be escaped, '"' remains intact
        String input = "<tag attr=\"hello & goodbye\">";
        Entities.escape(accum, input, settings, false, false, false);

        String result = accum.toString();
        assertEquals("&lt;tag attr=\"hello &amp; goodbye\"&gt;", result);
    }

    @Test(timeout = 4000)
    public void testUnescapeBasicAndStrict() {
        String escaped = "&lt;div&gt;&amp;&quot;&apos;&copy;&lt;/div&gt;";
        String unescaped = Entities.unescape(escaped);
        assertEquals("<div>&\"'©</div>", unescaped);

        // Strict unescape requires trailing ';'
        String partial = "&amp &amp; &lt &lt;";
        String unescapedStrict = Entities.unescape(partial, true);
        assertEquals("&amp & &lt <", unescapedStrict);

        String unescapedLenient = Entities.unescape(partial, false);
        assertEquals("& & < <", unescapedLenient);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        Document.OutputSettings settings = new Document.OutputSettings();
        assertEquals("", Entities.escape("", settings));

        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "", settings, false, false, false);
        assertEquals(0, accum.length());
    }

    @Test(timeout = 4000)
    public void testWhitespaceNormalizationCollapsingAndTrimming() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder accum = new StringBuilder();

        // Leading whitespace, consecutive whitespaces, and trailing whitespace
        String input = "   Hello   \t\n\r   World   ";
        Entities.escape(accum, input, settings, false, true, true);

        assertEquals("Hello World ", accum.toString());
    }

    @Test(timeout = 4000)
    public void testWhitespaceNormalizationWithoutStripLeadingWhite() {
        Document.OutputSettings settings = new Document.OutputSettings();
        StringBuilder accum = new StringBuilder();

        String input = "   Hello   World   ";
        Entities.escape(accum, input, settings, false, true, false);

        assertEquals(" Hello World ", accum.toString());
    }

    @Test(timeout = 4000)
    public void testWhitespaceNormalizationOnlyWhitespaces() {
        Document.OutputSettings settings = new Document.OutputSettings();

        // When stripping leading whitespace and string is only whitespace, result should be empty
        StringBuilder accum1 = new StringBuilder();
        Entities.escape(accum1, "   \t\n\r   ", settings, false, true, true);
        assertEquals("", accum1.toString());

        // When NOT stripping leading whitespace, string of spaces should collapse to a single space
        StringBuilder accum2 = new StringBuilder();
        Entities.escape(accum2, "   \t\n\r   ", settings, false, true, false);
        assertEquals(" ", accum2.toString());
    }

    @Test(timeout = 4000)
    public void testSupplementaryCodePointsInUtf8() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");

        // Musical symbol G Clef: U+1D11E (Surrogate pair: \uD834\uDD1E)
        String gClef = "\uD834\uDD1E";
        String escaped = Entities.escape(gClef, settings);
        assertEquals(gClef, escaped);
    }

    @Test(timeout = 4000)
    public void testSupplementaryCodePointsInAsciiFallback() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");

        // Musical symbol G Clef: U+1D11E cannot be encoded in ASCII -> hex representation
        String gClef = "\uD834\uDD1E";
        String escaped = Entities.escape(gClef, settings);
        assertEquals("&#x1d11e;", escaped);
    }

    @Test(timeout = 4000)
    public void testAsciiCharsetNonAsciiCharacters() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.base);

        // '©' (copyright) is in base entity set: &copy;
        String input = "Copyright \u00A9";
        String escaped = Entities.escape(input, settings);
        assertEquals("Copyright &copy;", escaped);

        // '¢' (cent, U+00A2) is in base entity set: &cent;
        String centInput = "Price: 50\u00A2";
        String centEscaped = Entities.escape(centInput, settings);
        assertEquals("Price: 50&cent;", centEscaped);

        // A character not in base entity map (e.g. Hebrew alef \u05D0) under base escapeMode
        String alef = "\u05D0";
        String alefEscaped = Entities.escape(alef, settings);
        assertEquals("&#x5d0;", alefEscaped);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Shift_JIS / NBSP Defect)
    // =========================================================================

    /**
     * Targets: Defects4J bug in DocumentTest::testShiftJisRoundtrip
     * Under Shift_JIS (or any charset unable to encode 0xA0) with EscapeMode.xhtml,
     * character 0xA0 (NBSP) must NOT be emitted as a raw character '\u00a0' because
     * encoding to Shift_JIS produces an unmappable character ('?').
     * Instead, it must be escaped as a numeric character reference ("&#xa0;").
     */
    @Test(timeout = 4000)
    public void testShiftJisRoundtripWithXhtmlNonBreakingSpaceDefect() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("Shift_JIS");
        settings.escapeMode(Entities.EscapeMode.xhtml);

        String input = "before\u00A0after";
        String escaped = Entities.escape(input, settings);

        // Under XHTML mode and Shift_JIS charset, \u00A0 cannot be encoded directly
        // in Shift_JIS without corruption ('?'). It should be escaped as numeric entity "&#xa0;".
        assertFalse("Escaped string must not retain raw unmappable character 0xA0 for Shift_JIS",
                escaped.contains("\u00A0"));
        assertTrue("Escaped string must contain numeric entity '&#xa0;' when unmappable in charset",
                escaped.contains("&#xa0;"));

        // Round-trip verification: encoding to Shift_JIS bytes and back must not contain '?'
        byte[] bytes = escaped.getBytes(Charset.forName("Shift_JIS"));
        String roundtripped = new String(bytes, Charset.forName("Shift_JIS"));
        assertFalse("Round-tripped Shift_JIS text should not contain '?'", roundtripped.contains("?"));
    }

    @Test(timeout = 4000)
    public void testAsciiRoundtripWithXhtmlNonBreakingSpace() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.xhtml);

        String input = "one\u00A0two";
        String escaped = Entities.escape(input, settings);

        // US-ASCII cannot encode 0xA0; XHTML mode cannot use &nbsp;; must use &#xa0;
        assertFalse("US-ASCII cannot contain raw 0xA0", escaped.contains("\u00A0"));
        assertTrue("US-ASCII with XHTML must encode non-breaking space as &#xa0;", escaped.contains("&#xa0;"));
    }

    @Test(timeout = 4000)
    public void testNonBreakingSpaceInBaseAndExtendedEscapeModes() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("UTF-8");

        settings.escapeMode(Entities.EscapeMode.base);
        assertEquals("&nbsp;", Entities.escape("\u00A0", settings));

        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals("&nbsp;", Entities.escape("\u00A0", settings));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testIsNamedEntityNull() {
        Entities.isNamedEntity(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testIsBaseNamedEntityNull() {
        Entities.isBaseNamedEntity(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetCharacterByNameNull() {
        Entities.getCharacterByName(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeNullString() {
        Document.OutputSettings settings = new Document.OutputSettings();
        Entities.escape(null, settings);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeNullOutputSettings() {
        Entities.escape("test", null);
    }

    @Test(timeout = 4000)
    public void testFallbackCharsetCanEncodeHandling() {
        // ISO-8859-1 (Latin-1) encodes up to 0xFF, triggers 'fallback' CoreCharset
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("ISO-8859-1");
        settings.escapeMode(Entities.EscapeMode.xhtml);

        // Latin-1 can encode \u00E9 ('é'), but cannot encode \u0416 ('Ж' Cyrillic)
        String input = "\u00E9 and \u0416";
        String escaped = Entities.escape(input, settings);

        assertTrue("ISO-8859-1 encodable character should be retained", escaped.contains("\u00E9"));
        assertTrue("ISO-8859-1 unencodable character should be hex-escaped", escaped.contains("&#x416;"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeModeEnumValuesAndMappings() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertEquals(3, modes.length);

        Entities.EscapeMode xhtml = Entities.EscapeMode.valueOf("xhtml");
        Entities.EscapeMode base = Entities.EscapeMode.valueOf("base");
        Entities.EscapeMode extended = Entities.EscapeMode.valueOf("extended");

        assertNotNull(xhtml.getMap());
        assertNotNull(base.getMap());
        assertNotNull(extended.getMap());

        Map<Character, String> xhtmlMap = xhtml.getMap();
        assertEquals("quot", xhtmlMap.get('"'));
        assertEquals("amp", xhtmlMap.get('&'));
        assertEquals("lt", xhtmlMap.get('<'));
        assertEquals("gt", xhtmlMap.get('>'));
        // XHTML restricted map must not contain other entities like 'copy' or 'nbsp'
        assertNull(xhtmlMap.get('\u00A9'));
        assertNull(xhtmlMap.get('\u00A0'));

        Map<Character, String> baseMap = base.getMap();
        assertTrue("Base map should contain 'lt'", baseMap.containsValue("lt"));
        assertTrue("Base map should contain 'quot'", baseMap.containsValue("quot"));

        Map<Character, String> extendedMap = extended.getMap();
        assertTrue("Extended map must have more or equal mappings than base",
                extendedMap.size() >= baseMap.size());
    }

    @Test(timeout = 4000)
    public void testExtendedModeEscapesUnencodableEntities() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.extended);

        // Greek small letter alpha U+03B1 has named entity &alpha; in extended set
        String input = "\u03B1";
        String escaped = Entities.escape(input, settings);
        assertEquals("&alpha;", escaped);
    }
}