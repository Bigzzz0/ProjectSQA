package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Method Under Test: Entities.unescape(String)
 * Branch 1: string.contains("&") == false -> Fast return of original string reference.
 * Branch 2: string.contains("&") == true -> Matcher loop processing entity tokens.
 *   - Sub-branch 2a: Numeric entity (group(3) != null)
 *       * Hex indicator (group(2) != null) -> base 16 parsing ('x' or 'X').
 *       * Decimal indicator (group(2) == null) -> base 10 parsing.
 *       * Parsing success -> valid charval computed.
 *       * NumberFormatException -> caught, charval remains -1.
 *   - Sub-branch 2b: Named entity (group(3) == null)
 *       * full.containsKey(name) == true -> charval retrieved from map.
 *       * full.containsKey(name) == false -> charval remains -1.
 *   - Sub-branch 2c: Replacement decision
 *       * charval != -1 -> append replacement character.
 *       * charval == -1 -> append original token unchanged.
 * DEFECT TARGET (Defects4J):
 *   - unescapePattern "&(#(x|X)?([0-9a-fA-F]+)|[a-zA-Z]+);?" uses "[a-zA-Z]+" which fails
 *     to match entity names containing numerical digits (e.g., &frac34;, &frac12;, &sup2;).
 *     Ground truth defect: "&frac34;" fails to unescape to "¾" (\u00be).
 * =========================================================================================
 * Method Under Test: Entities.escape(String, CharsetEncoder, EscapeMode)
 * Branch 1: Character found in EscapeMode map -> emit "&" + entity + ";".
 * Branch 2: Character not in map, but encoder.canEncode(c) == true -> emit raw char.
 * Branch 3: Character not in map and encoder.canEncode(c) == false -> emit "&#" + (int) c + ";".
 * EscapeMode Enums: xhtml, base, extended with respective coverage maps.
 * =========================================================================================
 */
public class EntitiesGeminiTest {

    // =====================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =====================================================================================

    @Test(timeout = 4000)
    public void testUnescapeStandardNamedEntities() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("'", Entities.unescape("&apos;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeWithoutTrailingSemicolon() {
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("<", Entities.unescape("&lt"));
        assertEquals(">", Entities.unescape("&gt"));
        assertEquals("\"", Entities.unescape("&quot"));
        assertEquals("'", Entities.unescape("&apos"));
    }

    @Test(timeout = 4000)
    public void testUnescapeDecimalEntities() {
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("a", Entities.unescape("&#97;"));
        assertEquals("A", Entities.unescape("&#65")); // Without semicolon
        assertEquals("&", Entities.unescape("&#38;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexEntities() {
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("A", Entities.unescape("&#X41;")); // Uppercase X indicator
        assertEquals("a", Entities.unescape("&#x61;"));
        assertEquals("a", Entities.unescape("&#X61")); // Uppercase X without semicolon
        assertEquals(" ", Entities.unescape("&#x20;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeMixedTextAndEntities() {
        String input = "One &lt; Two &amp;&amp; Three &gt; Two; &quot;Quote&quot; and &apos;Apos&apos;";
        String expected = "One < Two && Three > Two; \"Quote\" and 'Apos'";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testEscapeWithXhtmlModeAscii() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<tag id=\"val\" attr='test'>&foo</tag>";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.xhtml);
        assertEquals("&lt;tag id=&quot;val&quot; attr=&apos;test&apos;&gt;&amp;foo&lt;/tag&gt;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeWithBaseModeAscii() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "© 2024 & <test>";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals("&copy; 2024 &amp; &lt;test&gt;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeWithExtendedModeAscii() {
        CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
        String input = "α + β = γ; ©";
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.extended);
        assertEquals("&alpha; + &beta; = &gamma;; &copy;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeWithUtf8EncoderLeavesEncodableChars() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        // Extended non-HTML characters that are valid in UTF-8
        String input = "Hello 日本語 © & < >";
        // In UTF-8, Japanese characters can be encoded and are not in base map
        String escaped = Entities.escape(input, encoder, Entities.EscapeMode.base);
        assertEquals("Hello 日本語 &copy; &amp; &lt; &gt;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeViaDocumentOutputSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset("US-ASCII");
        settings.escapeMode(Entities.EscapeMode.base);
        String input = "Price: 100€ & 50¢ <cheap>";
        String escaped = Entities.escape(input, settings);
        assertEquals("Price: 100&euro; &amp; 50&cent; &lt;cheap&gt;", escaped);
    }

    // =====================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =====================================================================================

    @Test(timeout = 4000)
    public void testUnescapeWithoutAmpersandFastPath() {
        String input = "Plain ASCII text with no entities at all.";
        assertSame("Should return the identical string instance when no ampersand is present",
                input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.xhtml));
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.base));
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.extended));
    }

    @Test(timeout = 4000)
    public void testUnescapeLoneAmpersands() {
        assertEquals("&", Entities.unescape("&"));
        assertEquals("&&&", Entities.unescape("&&&"));
        assertEquals("A & B & C", Entities.unescape("A & B & C"));
        assertEquals("A &; B", Entities.unescape("A &; B"));
    }

    @Test(timeout = 4000)
    public void testUnescapeRegexSpecialCharactersHandling() {
        // Replacement tokens containing $ and \ must not crash Matcher.appendReplacement
        assertEquals("$", Entities.unescape("&#36;"));
        assertEquals("$", Entities.unescape("&dollar;"));
        assertEquals("\\", Entities.unescape("&#92;"));
        assertEquals("\\", Entities.unescape("&bsol;"));
        assertEquals("$100 and \\path\\to\\file",
                Entities.unescape("&dollar;100 and &bsol;path&bsol;to&bsol;file"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNullCharacterEntity() {
        assertEquals("\0", Entities.unescape("&#0;"));
        assertEquals("\0", Entities.unescape("&#x0;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeLargeNumberOverflowHandledSafely() {
        // String matches numeric regex, but exceeds Integer.MAX_VALUE causing NumberFormatException
        String overflow = "&#999999999999999999999999999999;";
        assertEquals("Should leave invalid/overflow numeric entity untouched",
                overflow, Entities.unescape(overflow));

        String hexOverflow = "&#xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF;";
        assertEquals("Should leave hex overflow numeric entity untouched",
                hexOverflow, Entities.unescape(hexOverflow));
    }

    @Test(timeout = 4000)
    public void testUnescapeUnknownNamedEntityUntouched() {
        String unknown = "&notarealentityname;";
        assertEquals(unknown, Entities.unescape(unknown));

        String unknownWithoutSemi = "&notarealentityname";
        assertEquals(unknownWithoutSemi, Entities.unescape(unknownWithoutSemi));
    }

    @Test(timeout = 4000)
    public void testEscapeNonEncodableCharacterWithoutEntityMapping() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        // The Japanese character '新' (0x65B0) is not in xhtmlByVal map and cannot be encoded in US-ASCII
        String input = "新";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&#26032;", escaped);
    }

    // =====================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =====================================================================================

    /**
     * Target Defect:
     * EntitiesTest::unescape failure:
     *   AssertionFailedError: expected:<...<> Å π π 新 there &! [¾]> but was:<...<> Å π π 新 there &! [&frac34;]>
     * Root Cause:
     *   The regex pattern was compiled with "[a-zA-Z]+" which excludes digits in named entities.
     *   Entities such as "frac34", "frac12", "frac14", "sup1", "sup2", "sup3" must be resolved.
     */
    @Test(timeout = 4000)
    public void testUnescapeKnownDefectFrac34() {
        assertEquals("¾", Entities.unescape("&frac34;"));
        assertEquals("¾", Entities.unescape("&frac34")); // Without trailing semicolon
    }

    @Test(timeout = 4000)
    public void testUnescapeKnownDefectOtherNumberedEntities() {
        assertEquals("½", Entities.unescape("&frac12;"));
        assertEquals("¼", Entities.unescape("&frac14;"));
        assertEquals("¹", Entities.unescape("&sup1;"));
        assertEquals("²", Entities.unescape("&sup2;"));
        assertEquals("³", Entities.unescape("&sup3;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeKnownDefectFullSentenceDefects4jReplication() {
        String input = "Hello &amp;&lt;&gt; &Aring; &pi; &#960; &#x65B0; there &! &frac34;";
        String expected = "Hello &<> Å π π 新 there &! ¾";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeCaseSensitivityMatchesTable() {
        // "AMP" and "amp" are both mapped
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("&", Entities.unescape("&AMP;"));
        // "LT" and "lt" are both mapped
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals("<", Entities.unescape("&LT;"));
        // "GT" and "gt" are both mapped
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals(">", Entities.unescape("&GT;"));
        // "COPY" and "copy" are both mapped
        assertEquals("©", Entities.unescape("&copy;"));
        assertEquals("©", Entities.unescape("&COPY;"));
    }

    // =====================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =====================================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testUnescapeNullStringThrowsNpe() {
        Entities.unescape(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeNullStringWithSettingsThrowsNpe() {
        Document.OutputSettings settings = new Document.OutputSettings();
        Entities.escape(null, settings);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeNullSettingsThrowsNpe() {
        Entities.escape("test", (Document.OutputSettings) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeNullEncoderThrowsNpe() {
        Entities.escape("test", (CharsetEncoder) null, Entities.EscapeMode.base);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEscapeNullEscapeModeThrowsNpe() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        Entities.escape("test", encoder, null);
    }

    // =====================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =====================================================================================

    @Test(timeout = 4000)
    public void testEntitiesClassInstantiation() {
        Entities entities = new Entities();
        assertNotNull(entities);
    }

    @Test(timeout = 4000)
    public void testEscapeModeEnumValues() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertEquals(3, modes.length);
        assertEquals(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    @Test(timeout = 4000)
    public void testEscapeModeMapsIntegrity() {
        Map<Character, String> xhtmlMap = Entities.EscapeMode.xhtml.getMap();
        assertNotNull(xhtmlMap);
        assertEquals("lt", xhtmlMap.get('<'));
        assertEquals("gt", xhtmlMap.get('>'));
        assertEquals("amp", xhtmlMap.get('&'));
        assertEquals("quot", xhtmlMap.get('"'));
        assertEquals("apos", xhtmlMap.get('\''));
        assertEquals(5, xhtmlMap.size());

        Map<Character, String> baseMap = Entities.EscapeMode.base.getMap();
        assertNotNull(baseMap);
        assertTrue(baseMap.containsKey('<'));
        assertTrue(baseMap.containsKey('>'));
        assertTrue(baseMap.containsKey('&'));
        assertTrue(baseMap.containsKey('"'));
        assertTrue(baseMap.containsKey('©'));

        Map<Character, String> fullMap = Entities.EscapeMode.extended.getMap();
        assertNotNull(fullMap);
        assertTrue(fullMap.containsKey('<'));
        assertTrue(fullMap.containsKey('>'));
        assertTrue(fullMap.containsKey('&'));
        assertTrue(fullMap.containsKey('π'));
        assertTrue(fullMap.size() > baseMap.size());
    }
}