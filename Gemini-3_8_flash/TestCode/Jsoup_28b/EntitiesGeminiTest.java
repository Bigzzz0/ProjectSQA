package org.jsoup.nodes;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.nodes.Entities
 *
 * Decision / Condition Coverage Targets:
 * 1. isNamedEntity(name):
 *    - name present in full map (true)
 *    - name absent in full map (false)
 * 2. getCharacterByName(name):
 *    - name found -> returns Character
 *    - name not found -> returns null
 * 3. escape(string, encoder, escapeMode):
 *    - char in map -> append '&' + name + ';'
 *    - char not in map, encoder.canEncode(c) == true -> append raw character
 *    - char not in map, encoder.canEncode(c) == false -> append "&#" + (int) c + ";"
 *    - escape(string, Document.OutputSettings) delegation
 * 4. unescape(string, strict):
 *    - !string.contains("&") early return branch
 *    - strict regex (strict=true) vs relaxed regex (strict=false)
 *    - numeric entity hex (x|X) -> group(2) != null (base 16)
 *    - numeric entity dec -> group(2) == null (base 10)
 *    - NumberFormatException handling -> charval remains -1 -> else branch (raw match retained)
 *    - named entity match: full.containsKey(name) true vs false
 *    - charval != -1 branch: character decoded and replaced
 *    - charval == -1 branch: raw match preserved
 * 5. EscapeMode Enum:
 *    - values: xhtml, base, extended
 *    - getMap() non-null and correctly populated
 *
 * Defect-Targeted Branches (Ground Truth: Defects4J jsoup issue):
 * - Strict vs. relaxed named entity matching without semicolon:
 *   Relaxed unescape regex `&(#(x|X)?([0-9a-fA-F]+)|[a-zA-Z]+\\d*);?` greedily matched prefixes of words
 *   (e.g., &num_rooms -> &#_rooms, &int=VA -> &int decoded to integral symbol '∫').
 *   Extended entities like &angst without trailing ';' were spuriously decoded instead of being preserved.
 * ---------------------------------------------------------------------------------------------------------
 */
public class EntitiesGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNamedEntityPositiveAndNegative() {
        assertTrue("Expected 'lt' to be recognized as named entity", Entities.isNamedEntity("lt"));
        assertTrue("Expected 'gt' to be recognized as named entity", Entities.isNamedEntity("gt"));
        assertTrue("Expected 'amp' to be recognized as named entity", Entities.isNamedEntity("amp"));
        assertTrue("Expected 'quot' to be recognized as named entity", Entities.isNamedEntity("quot"));
        assertTrue("Expected 'aring' to be recognized as named entity", Entities.isNamedEntity("aring"));
        assertTrue("Expected 'copy' to be recognized as named entity", Entities.isNamedEntity("copy"));

        assertFalse("Expected non-existent entity to return false", Entities.isNamedEntity("notAnEntityName"));
        assertFalse("Expected empty entity name to return false", Entities.isNamedEntity(""));
    }

    @Test(timeout = 4000)
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        assertEquals(Character.valueOf('\''), Entities.getCharacterByName("apos"));
        assertEquals(Character.valueOf((char) 169), Entities.getCharacterByName("copy"));
        assertNull(Entities.getCharacterByName("nonExistentEntityXYZ"));
    }

    @Test(timeout = 4000)
    public void testEscapeWithAsciiEncoderUnencodableChars() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

        // In xhtml mode, euro symbol € (U+20AC, 8364) is not in xhtml map and not encodable in ASCII
        String input = "\u20AC";
        String escaped = Entities.escape(input, asciiEncoder, Entities.EscapeMode.xhtml);
        assertEquals("&#8364;", escaped);

        // In base mode, &copy; is in map -> &copy;
        String copyInput = "\u00A9";
        String escapedCopy = Entities.escape(copyInput, asciiEncoder, Entities.EscapeMode.base);
        assertEquals("&copy;", escapedCopy);
    }

    @Test(timeout = 4000)
    public void testEscapeWithUtf8Encoder() {
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();

        // Characters in XHTML map should be escaped
        String input = "<hello & 'world' \"quote\">";
        String escaped = Entities.escape(input, utf8Encoder, Entities.EscapeMode.xhtml);
        assertEquals("&lt;hello &amp; &apos;world&apos; &quot;quote&quot;&gt;", escaped);

        // Unmapped characters that are encodable in UTF-8 should remain raw characters
        String nonAscii = "\u65B0"; // Chinese char
        String escapedNonAscii = Entities.escape(nonAscii, utf8Encoder, Entities.EscapeMode.xhtml);
        assertEquals(nonAscii, escapedNonAscii);
    }

    @Test(timeout = 4000)
    public void testEscapeWithDocumentOutputSettings() {
        Document doc = new Document("");
        doc.outputSettings().charset("US-ASCII");
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        String result = Entities.escape("<b>&\"'\u65B0</b>", doc.outputSettings());
        assertEquals("&lt;b&gt;&amp;&quot;&apos;&#26032;&lt;/b&gt;", result);
    }

    @Test(timeout = 4000)
    public void testUnescapeBasicAndNumeric() {
        assertEquals("<>&\"'", Entities.unescape("&lt;&gt;&amp;&quot;&apos;"));
        assertEquals("<>&", Entities.unescape("&#60;&#62;&#38;"));
        assertEquals("<>&", Entities.unescape("&#x3c;&#x3e;&#x26;"));
        assertEquals("<>&", Entities.unescape("&#X3C;&#X3E;&#X26;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeStrictRequirement() {
        // Strict mode requires trailing semicolon
        assertEquals("<", Entities.unescape("&lt;", true));
        assertEquals("&lt", Entities.unescape("&lt", true));

        assertEquals("<", Entities.unescape("&#60;", true));
        assertEquals("&#6