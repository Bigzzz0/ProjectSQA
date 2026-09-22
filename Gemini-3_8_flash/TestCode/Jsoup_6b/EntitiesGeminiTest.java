package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/* [Branch & Defect Analysis Matrix]
 * Class Under Test: org.jsoup.nodes.Entities
 *
 * 1. escape(String, Document.OutputSettings)
 *    - Validates delegation to escape(string, encoder, escapeMode).
 *
 * 2. escape(String, CharsetEncoder, EscapeMode)
 *    - Branch: escapeMode == EscapeMode.extended (fullByVal map) vs base (baseByVal map).
 *    - Branch: map.containsKey(c) -> append '&' + map.get(c) + ';'.
 *    - Branch: !map.containsKey(c) && encoder.canEncode(c) -> raw character pass-through.
 *    - Branch: !map.containsKey(c) && !encoder.canEncode(c) -> numeric entity fallback '&#(int);'.
 *
 * 3. unescape(String)
 *    - Branch: !string.contains("&") -> early exit returning string unmodified.
 *    - Loop: Matcher.find() covering single, multiple, and no regex matches.
 *    - Branch: num != null (numeric character entity) vs num == null (named entity).
 *      - Sub-branch: group(2) != null (hex 'x' or 'X', base 16) vs group(2) == null (decimal, base 10).
 *      - Sub-branch: NumberFormatException on numeric overflow -> charval remains -1.
 *    - Branch: num == null -> full.containsKey(name) true vs false.
 *    - Branch: charval != -1 || charval > 0xFFFF true -> append replacement vs false -> retain original token.
 *    - Tail: Matcher.appendTail correctly preserves unmatched trailing text.
 *
 * 4. DEFECT-TARGETED ZONE (Defects4J Known Bug):
 *    - Issue: Matcher.appendReplacement(accum, c) treats '$' (group ref) and '\' (escape) as special
 *      regex syntax. When resolving entities like '&dollar;', '&#36;', '&bsol;', or '&#92;', it throws
 *      IllegalArgumentException ("Illegal group reference" or "character to be escaped is missing").
 *    - Test: Directly targets unescaping '$' and '\' entities to assert compliant replacement behavior.
 */
public class EntitiesGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeBaseModeWithAsciiEncoder() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "<foo & bar \" ' > \u00a9";
        // In base mode, < -> &lt;, & -> &amp;, " -> &quot;, > -> &gt;, © -> &copy;
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&lt;foo &amp; bar &quot; ' &gt; &copy;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeExtendedModeWithUtf8Encoder() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        // &Abreve; is in extended/full map (0x0102) but not in base map
        String input = "\u0102 & <";
        String escaped = Entities.escape(input, utf8Encoder, Entities.EscapeMode.extended);
        assertEquals("&Abreve; &amp; &lt;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeViaDocumentOutputSettings() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        settings.charset("US-ASCII");

        String escaped = Entities.escape("Hello & World >", settings);
        assertEquals("Hello &amp; World &gt;", escaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeStandardNamedEntities() {
        String input = "&amp; &lt; &gt; &quot; &copy; &reg; &AElig;";
        String expected = "& < > \" \u00a9 \u00ae \u00c6";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeDecimalEntities() {
        String input = "&#65; &#66; &#67; &#38;";
        String expected = "A B C &";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexEntitiesBothCases() {
        // Lowercase 'x' and uppercase 'X'
        String input = "&#x41; &#X42; &#x0043; &#X26;";
        String expected = "A B C &";
        assertEquals(expected, Entities.unescape(input));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnescapeStringWithoutAmpersand() {
        String plain = "This string has no ampersands at all 12345.";
        assertSame(plain, Entities.unescape(plain));
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.base));
        assertEquals("", Entities.escape("", encoder, Entities.EscapeMode.extended));
    }

    @Test(timeout = 4000)
    public void testUnescapeWithoutTrailingSemicolon() {
        // HTML allows base entities and numeric entities without trailing semicolon
        assertEquals("&", Entities.unescape("&amp"));
        assertEquals("<", Entities.unescape("&lt"));
        assertEquals(">", Entities.unescape("&gt"));
        assertEquals("A", Entities.unescape("&#65"));
        assertEquals("B", Entities.unescape("&#x42"));
    }

    @Test(timeout = 4000)
    public void testUnescapeConsecutiveEntities() {
        String input = "&amp;&lt;&gt;&quot;";
        assertEquals("&<>\"", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeLeadingAndTrailingPlainText() {
        String input = "start &amp; middle &#65; end";
        assertEquals("start & middle A end", Entities.unescape(input));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testQuoteReplacementsNumericDollarAndBackslash() {
        // Target Defect: Matcher.appendReplacement throws IllegalArgumentException when
        // replacement string is '$' (group index missing) or '\' (character missing).
        String escaped = "&#92; &#36;";
        String expected = "\\ $";
        assertEquals(expected, Entities.unescape(escaped));
    }

    @Test(timeout = 4000)
    public void testQuoteReplacementsNamedDollarAndBackslash() {
        // &dollar; maps to '$' (0x24) and &bsol; maps to '\' (0x5C)
        String escaped = "&bsol; &dollar;";
        String expected = "\\ $";
        assertEquals(expected, Entities.unescape(escaped));
    }

    @Test(timeout = 4000)
    public void testQuoteReplacementsInComplexSentence() {
        // Typical integration failure pattern seen in ParseTest::testYahooArticle
        String escaped = "Price is &dollar;100 &amp; file path is C:&bsol;data&bsol;test.txt";
        String expected = "Price is $100 & file path is C:\\data\\test.txt";
        assertEquals(expected, Entities.unescape(escaped));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnescapeMalformedNumericEntityNumberFormatException() {
        // Very large number causing NumberFormatException during Integer.valueOf(num, base)
        String input = "Value &#99999999999999999999999999999999; remains intact";
        assertEquals(input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeUnknownNamedEntityPreserved() {
        // Named entities not in 'full' map must be preserved as original text
        String input = "This &notARealEntity; should stay.";
        assertEquals(input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeIsolatedAmpersandPreserved() {
        String input = "AT&T & Sons & 123";
        assertEquals(input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testEscapeNonEncodableCharacterFallbackToNumeric() {
        // When character cannot be encoded by charset and is not in entity map:
        // CJK character \u4e2d (20013) is not in base or full entity maps.
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "Chinese: \u4e2d";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("Chinese: &#20013;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeBaseModeLeavesExtendedEntityNumericWhenUnencodable() {
        // \u0102 (&Abreve;, code 258) is only in extended/full map, NOT in base map.
        // Under base mode with ASCII, it cannot encode and is not in baseByVal -> &#258;
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "\u0102";
        String escapedBase = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&#258;", escapedBase);

        // Under extended mode, it resolves to named entity &Abreve;
        String escapedExt = Entities.escape(input, asciiEncoder, Entities.EscapeMode.extended);
        assertEquals("&Abreve;", escapedExt);

        // Under base mode with UTF-8, it preserves raw character since encoder.canEncode() is true
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        String escapedUtf8 = Entities.escape(input, utf8Encoder, Entities.EscapeMode.base);
        assertEquals("\u0102", escapedUtf8);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEntitiesInstantiation() {
        Entities entities = new Entities();
        assertNotNull(entities);
    }

    @Test(timeout = 4000)
    public void testEscapeModeEnumValues() {
        Entities.EscapeMode[] modes = Entities.EscapeMode.values();
        assertEquals(2, modes.length);
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }
}