package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

/**
 * White-box test suite for Entities class.
 * Targets line/branch coverage and the known defect where escape() outputs lowercase entity names
 * instead of preserving the original case from the entity arrays.
 *
 * [Branch & Defect Analysis Matrix]
 * - escape(): branches: escapeMode == extended vs base; map.containsKey(c); encoder.canEncode(c)
 * - unescape(): branches: string.contains("&"); m.find(); num != null; m.group(2) != null (hex);
 *               NumberFormatException; full.containsKey(name); charval != -1 || charval > 0xFFFF (bug);
 *               else branch (keep original)
 * - Static initializer: loops over baseArray and fullArray, populating maps with toLowerCase values.
 *   Defect: map values are always lowercase, causing incorrect case for entities like &Aring; -> &aring;
 */
public class EntitiesDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testEscapeBaseMode() {
        // Use base escape mode (default)
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        // Characters with base entities: &, <, >, Å, Ü, é, etc.
        // Expected: case should match the original entry in baseArray (uppercase for Aring, Uuml, etc.)
        String input = "&<>ÅÜé";
        String expected = "&amp;&lt;&gt;&Aring;&Uuml;&eacute;";
        String result = Entities.escape(input, settings);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testEscapeExtendedMode() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.extended);
        // Characters with extended entities: α (lowercase alpha), Α (uppercase Alpha)
        String input = "αΑ";
        // Expected: α -> &alpha; (lowercase), Α -> &Alpha; (uppercase)
        String expected = "&alpha;&Alpha;";
        String result = Entities.escape(input, settings);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testEscapeWithEncoderCannotEncode() {
        // Use an encoder that only supports ASCII (e.g., ISO-8859-1 encoder for ASCII subset)
        CharsetEncoder asciiEncoder = StandardCharsets.US_ASCII.newEncoder();
        // Characters like é (0xE9) cannot be encoded in ASCII
        String input = "é";
        String expected = "&#233;";
        String result = Entities.escape(input, asciiEncoder, Entities.EscapeMode.base);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testEscapeNoEscapeNeeded() {
        // Characters not in map and encoder can encode -> output as is
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        String input = "Hello World!";
        String result = Entities.escape(input, settings);
        assertEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testEscapeMixed() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        String input = "a&b<c>dÅe";
        String expected = "a&amp;b&lt;c&gt;d&Aring;e";
        String result = Entities.escape(input, settings);
        assertEquals(expected, result);
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        Document.OutputSettings settings = new Document.OutputSettings();
        String input = "";
        String result = Entities.escape(input, settings);
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testUnescapeNoAmpersand() {
        String input = "Hello World";
        String result = Entities.unescape(input);
        assertEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyString() {
        String input = "";
        String result = Entities.unescape(input);
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testUnescapeWithOptionalSemicolon() {
        // Base entities without trailing semicolon should still be unescaped
        String input = "&amp &lt &gt";
        String expected = "&<>";
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericDecimal() {
        String input = "&#60;";
        String expected = "<";
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericHex() {
        String input = "&#x3C;";
        String expected = "<";
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericHexLowercase() {
        String input = "&#x3c;";
        String expected = "<";
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeInvalidNumeric() {
        // Invalid numeric entity (non-numeric) should be left as-is
        String input = "&#xyz;";
        String result = Entities.unescape(input);
        assertEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeOutOfRangeNumeric() {
        // Numeric entity > 0xFFFF should NOT be replaced (bug: condition uses OR)
        String input = "&#x10000;";
        // Correct behavior: keep original because charval > 0xFFFF
        String result = Entities.unescape(input);
        assertEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeNamedEntityNotInMap() {
        String input = "&unknown;";
        String result = Entities.unescape(input);
        assertEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeCaseInsensitive() {
        // Named entities are case-insensitive due to toLowerCase lookup
        String input = "&AMP; &amp;";
        String expected = "&&";
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testEscapeDefectCaseSensitive() {
        // Known defect: escape outputs lowercase entity names for characters that have uppercase entries
        // e.g., 'Å' should become &Aring; but bug gives &aring;
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        String input = "Å";
        String expected = "&Aring;";
        String result = Entities.escape(input, settings);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testEscapeDefectUuml() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        String input = "Ü";
        String expected = "&Uuml;";
        String result = Entities.escape(input, settings);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testEscapeDefectExtendedCase() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.extended);
        // Uppercase Greek Alpha should become &Alpha; not &alpha;
        String input = "Α";
        String expected = "&Alpha;";
        String result = Entities.escape(input, settings);
        assertEquals(expected, result);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testUnescapeNumberFormatException() {
        // Numeric entity with invalid base (e.g., &#G;) should be caught and left as-is
        String input = "&#G;";
        String result = Entities.unescape(input);
        assertEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testUnescapeMixedEntities() {
        String input = "&amp;&lt;&gt;&#60;&#x3C;&unknown;";
        String expected = "<><<<&unknown;";
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeRoundTrip() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        String original = "Hello & < > \" ' Å Ü é";
        String escaped = Entities.escape(original, settings);
        String unescaped = Entities.unescape(escaped);
        assertEquals(original, unescaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeWithMultipleMatches() {
        String input = "&amp;&lt;&gt;&amp;";
        String expected = "<>&";
        String result = Entities.unescape(input);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testEscapeWithExtendedOnlyEntity() {
        // Character that exists only in fullArray, not in baseArray
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.extended);
        String input = "α"; // alpha (lowercase)
        String expected = "&alpha;";
        String result = Entities.escape(input, settings);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testEscapeBaseModeWithEncoder() {
        // Use base mode but with encoder that can encode everything (UTF-8)
        CharsetEncoder utf8Encoder = StandardCharsets.UTF_8.newEncoder();
        String input = "&<>";
        String expected = "&amp;&lt;&gt;";
        String result = Entities.escape(input, utf8Encoder, Entities.EscapeMode.base);
        assertEquals(expected, result);
    }
}