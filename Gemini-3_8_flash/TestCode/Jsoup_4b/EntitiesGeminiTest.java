package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.nodes.Entities
 *
 * Decision / Condition Coverage Targets:
 * 1. escape(String, CharsetEncoder, EscapeMode):
 *    - escapeMode == EscapeMode.extended (true -> fullByVal, false -> baseByVal)
 *    - loop: pos = 0 to length() (empty string -> 0 iterations, multi-char string)
 *    - map.containsKey(c) == true (append '&' + name + ';')
 *    - map.containsKey(c) == false && encoder.canEncode(c) == true (append raw char)
 *    - map.containsKey(c) == false && encoder.canEncode(c) == false (append '&#x;' numeric entity)
 * 2. unescape(String):
 *    - !string.contains("&") (true -> early exit, false -> process matcher)
 *    - m.find() loop (no matches, single match, multiple matches, trailing text)
 *    - num != null (numeric entity) vs num == null (named entity)
 *    - m.group(2) != null (hex indicator 'x'/'X', base 16) vs null (base 10)
 *    - NumberFormatException handling (e.g. integer overflow in entity number)
 *    - full.containsKey(name) == true vs false
 *    - (charval != -1 || charval > 0xFFFF):
 *         * charval != -1 (valid character replaced)
 *         * charval == -1 (unknown entity or format error preserved)
 * 3. EscapeMode Enum:
 *    - values(), valueOf("base"), valueOf("extended")
 * 4. Document.OutputSettings delegation:
 *    - escape(String, Document.OutputSettings) -> delegates to encoder() and escapeMode()
 *
 * Known Defect (Defects4J Ground Truth):
 * - Case-sensitive entity escaping: Entities static initializer calls toLowerCase() on entity names,
 *   causing uppercase character entities like 0x00C5 ('\u00C5', &Aring;) and 0x00DC ('\u00DC', &Uuml;)
 *   to be stored as "aring" and "uuml". Escaping these characters incorrectly outputs lowercase entity names
 *   ("&aring;" instead of "&Aring;", "&uuml;" instead of "&Uuml;").
 */
public class EntitiesGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeBaseModeBasicAscii() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "Hello <world> & \"peace\"!";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("Hello &lt;world&gt; &amp; &quot;peace&quot;!", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeExtendedModeWithFullEntities() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        // 0x02135 is &aleph;, 0x000A9 is &copy;
        String input = "\u00A9 \u2135";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.extended);
        assertEquals("&copy; &aleph;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeUnencodableNonEntityCharacter() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        // A character not present in baseByVal (e.g., Thai char '\u0E01' = 3585)
        String input = "\u0E01";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&#3585;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeEncodableUtf8DoesNotEscapeNonReserved() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        // Cyrillic character '\u0410' is encodable in UTF-8 and not in baseByVal
        String input = "А < Б";
        String escaped = Entities.escape(input, utf8Encoder, Entities.EscapeMode.base);
        assertEquals("А &lt; Б", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeViaOutputSettings() {
        Document doc = new Document("");
        doc.outputSettings().charset("US-ASCII");
        doc.outputSettings().escapeMode(Entities.EscapeMode.base);

        String input = "one < two & three > four";
        String escaped = Entities.escape(input, doc.outputSettings());
        assertEquals("one &lt; two &amp; three &gt; four", escaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeNamedEntitiesWithSemicolon() {
        String input = "&lt;&gt;&amp;&quot;&copy;";
        String unescaped = Entities.unescape(input);
        assertEquals("<>&\"©", unescaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeNamedEntitiesWithoutSemicolon() {
        String input = "&amp &lt &gt";
        String unescaped = Entities.unescape(input);
        assertEquals("& < >", unescaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeDecimalEntities() {
        String input = "&#65; &#66; &#67; and without semi: &#68";
        String unescaped = Entities.unescape(input);
        assertEquals("A B C and without semi: D", unescaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeHexEntitiesLowerAndUpperX() {
        String input = "&#x41; &#X42; &#x61; &#X62;";
        String unescaped = Entities.unescape(input);
        assertEquals("A B a b", unescaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeInterspersedText() {
        String input = "Before &amp; middle &#x43; after &gt;";
        String unescaped = Entities.unescape(input);
        assertEquals("Before & middle C after >", unescaped);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        assertEquals("", Entities.escape("", asciiEncoder, Entities.EscapeMode.base));
        assertEquals("", Entities.escape("", asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeWithoutAmpersand() {
        String input = "Just plain text with no entities.";
        assertSame(input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeIsolatedOrInvalidAmpersand() {
        assertEquals("&", Entities.unescape("&"));
        assertEquals("&&", Entities.unescape("&&"));
        assertEquals("&;", Entities.unescape("&;"));
        assertEquals("&#;", Entities.unescape("&#;"));
        assertEquals("&#x;", Entities.unescape("&#x;"));
        assertEquals("& not an entity &;", Entities.unescape("& not an entity &;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeUnknownNamedEntityPreserved() {
        String input = "&unknownentity; &xyz;";
        assertEquals("&unknownentity; &xyz;", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericOverflowPreserved() {
        // Exceeds Integer.MAX_VALUE to trigger NumberFormatException in Integer.valueOf
        String input = "&#99999999999999999999999999999; &#x99999999999999999999999999999;";
        String unescaped = Entities.unescape(input);
        assertEquals("&#99999999999999999999999999999; &#x99999999999999999999999999999;", unescaped);
    }

    @Test(timeout = 4000)
    public void testEscapeAllBasicHtmlEntities() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<>&\"";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&lt;&gt;&amp;&quot;", escaped);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Regression Tests)
    // =========================================================================

    /**
     * Targets Defects4J defect: Entities.escape lowercase map conversion.
     * Escaping '\u00C5' (&Aring;) must output "&Aring;", NOT "&aring;".
     */
    @Test(timeout = 4000)
    public void testEscapeCaseSensitivityAring() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        // \u00C5 is &Aring; (uppercase) and \u00E5 is &aring; (lowercase)
        // \u03C0 is Greek small letter pi (&#960; in base mode since not in baseArray)
        String input = "Hello &<> \u00C5 \u00E5 \u03C0";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("Hello &amp;&lt;&gt; &Aring; &aring; &#960;", escaped);
    }

    /**
     * Targets Defects4J defect: Entities.escape case sensitivity on umlauts and standard entities.
     * Escaping '\u00DC' (&Uuml;) must produce "&Uuml;", NOT "&uuml;".
     */
    @Test(timeout = 4000)
    public void testEscapeCaseSensitivityUuml() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        // \u00DC is &Uuml; (uppercase), \u00FC is &uuml; (lowercase)
        String input = "\u00DC \u00FC & &";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&Uuml; &uuml; &amp; &amp;", escaped);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeWithNullStringThrowsNpe() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        Entities.escape(null, asciiEncoder, Entities.EscapeMode.base);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeWithNullEncoderThrowsNpe() {
        Entities.escape("test", (CharsetEncoder) null, Entities.EscapeMode.base);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeWithNullOutputSettingsThrowsNpe() {
        Entities.escape("test", (Document.OutputSettings) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testUnescapeWithNullStringThrowsNpe() {
        Entities.unescape(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEntitiesConstructorInstantiable() {
        Entities entitiesInstance = new Entities();
        assertNotNull(entitiesInstance);
    }

    @Test(timeout = 4000)
    public void testEscapeModeEnumValuesAndValueOf() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertEquals(2, modes.length);
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }
}