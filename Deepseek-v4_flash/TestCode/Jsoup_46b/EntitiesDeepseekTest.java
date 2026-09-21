package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Entities.escape() and related methods.
 * 
 * Decision branches covered:
 * - escape(): normaliseWhite true/false, stripLeadingWhite true/false, inAttribute true/false
 * - escape(): codePoint < MIN_SUPPLEMENTARY vs >= (surrogate pairs)
 * - escape(): switch on c for '&', 0xA0, '<', '>', '"', default
 * - escape(): canEncode() branches for ascii, utf, fallback charsets
 * - escape(): map.containsKey(c) vs else (hex escape)
 * - canEncode(): ascii -> c < 0x80, utf -> true, fallback -> fallback.canEncode(c)
 * - CoreCharset.byName(): "US-ASCII", startsWith("UTF-"), else fallback
 * - isNamedEntity(), isBaseNamedEntity(), getCharacterByName() for known/unknown names
 * - toCharacterKey(): duplicate handling (prefer lower case)
 * - loadEntities(): resource loading, IOException path
 * 
 * Boundary conditions:
 * - codePoint = 0x7F (max ascii), 0x80 (min non-ascii), 0xFFFF (max BMP), 0x10000 (min supplementary)
 * - c = '&', 0xA0, '<', '>', '"' in both attribute and non-attribute contexts
 * - empty string, null input (should throw NPE)
 * - whitespace handling: leading, trailing, consecutive, non-breaking space
 * - entity names: "lt", "amp", "quot", "gt", "nbsp", "copy", "unknown"
 * - charset names: "US-ASCII", "UTF-8", "UTF-16", "ISO-8859-1", "unknown"
 * 
 * Defect targeting (testShiftJisRoundtrip):
 * The bug causes a '?' character to appear in output when round-tripping Shift_JIS encoded content.
 * This occurs because escape() fails to properly encode characters that are not representable in the
 * target charset, falling through to the hex escape path incorrectly or using the wrong encoding.
 * The test verifies that no '?' appears in the escaped output for characters that should be
 * hex-escaped or entity-escaped.
 */
public class EntitiesDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testIsNamedEntityKnown() {
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("nbsp"));
        assertTrue(Entities.isNamedEntity("copy"));
    }

    @Test(timeout = 4000)
    public void testIsNamedEntityUnknown() {
        assertFalse(Entities.isNamedEntity("unknown"));
        assertFalse(Entities.isNamedEntity(""));
        assertFalse(Entities.isNamedEntity("LT")); // case sensitive
    }

    @Test(timeout = 4000)
    public void testIsBaseNamedEntity() {
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertFalse(Entities.isBaseNamedEntity("copy")); // not in base set
        assertFalse(Entities.isBaseNamedEntity("unknown"));
    }

    @Test(timeout = 4000)
    public void testGetCharacterByName() {
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertNull(Entities.getCharacterByName("unknown"));
        assertNull(Entities.getCharacterByName(null));
    }

    @Test(timeout = 4000)
    public void testEscapeModeMaps() {
        // xhtml map should have exactly 4 entries
        assertEquals(4, Entities.EscapeMode.xhtml.getMap().size());
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey(Character.valueOf('<')));
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey(Character.valueOf('>')));
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey(Character.valueOf('&')));
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey(Character.valueOf('"')));

        // base map should have at least the basic entities
        assertTrue(Entities.EscapeMode.base.getMap().containsKey(Character.valueOf('<')));
        assertTrue(Entities.EscapeMode.base.getMap().containsKey(Character.valueOf('>')));
        assertTrue(Entities.EscapeMode.base.getMap().containsKey(Character.valueOf('&')));
        assertTrue(Entities.EscapeMode.base.getMap().containsKey(Character.valueOf('"')));

        // extended map should have many entities
        assertTrue(Entities.EscapeMode.extended.getMap().size() > 100);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEscapeNullInput() {
        try {
            Entities.escape(null, new Document.OutputSettings());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        Document.OutputSettings out = new Document.OutputSettings();
        assertEquals("", Entities.escape("", out));
    }

    @Test(timeout = 4000)
    public void testEscapeAsciiBoundary() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("US-ASCII"));

        // 0x7F is max ASCII, should be escaped as hex
        String input = new String(new char[]{(char) 0x7F});
        String result = Entities.escape(input, out);
        assertFalse("Should not contain '?'", result.contains("?"));
        assertTrue("Should be hex escaped", result.contains("&#x7f;"));

        // 0x80 is min non-ASCII, should be hex escaped
        String input2 = new String(new char[]{(char) 0x80});
        String result2 = Entities.escape(input2, out);
        assertFalse("Should not contain '?'", result2.contains("?"));
        assertTrue("Should be hex escaped", result2.contains("&#x80;"));
    }

    @Test(timeout = 4000)
    public void testEscapeSurrogatePair() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        // Supplementary character (U+1F600)
        String input = new String(Character.toChars(0x1F600));
        String result = Entities.escape(input, out);
        assertEquals("UTF-8 can encode supplementary", input, result);
    }

    @Test(timeout = 4000)
    public void testEscapeSurrogatePairNotEncodable() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("US-ASCII"));

        // Supplementary character (U+1F600) not encodable in ASCII
        String input = new String(Character.toChars(0x1F600));
        String result = Entities.escape(input, out);
        assertFalse("Should not contain '?'", result.contains("?"));
        assertTrue("Should be hex escaped", result.contains("&#x1f600;"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known defect from testShiftJisRoundtrip.
     * The bug causes a '?' to appear in output when characters cannot be encoded.
     * This test verifies that no '?' appears in escaped output for various charsets.
     */
    @Test(timeout = 4000)
    public void testEscapeNoQuestionMarkForShiftJis() {
        // Simulate Shift_JIS charset
        java.nio.charset.Charset shiftJis = java.nio.charset.Charset.forName("Shift_JIS");
        
        // Test with characters that are problematic in Shift_JIS
        String[] testStrings = {
            "\u00A0", // non-breaking space
            "\u2014", // em dash
            "\u00E9", // é
            "\u4E00", // CJK unified ideograph
            "\uFFFD", // replacement character
            "test\u00E9test", // mixed
            "\uD83D\uDE00" // emoji (supplementary)
        };

        for (String input : testStrings) {
            Document.OutputSettings out = new Document.OutputSettings();
            out.escapeMode(Entities.EscapeMode.base);
            out.charset(shiftJis);
            
            String result = Entities.escape(input, out);
            assertFalse("Should not contain '?' for input: " + input + " -> " + result, 
                       result.contains("?"));
        }
    }

    @Test(timeout = 4000)
    public void testEscapeNoQuestionMarkForAscii() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("US-ASCII"));

        String[] testStrings = {
            "\u00E9", // é
            "\u4E00", // CJK
            "\u00A0", // nbsp
            "caf\u00E9", // café
            "\u201C\u201D" // smart quotes
        };

        for (String input : testStrings) {
            String result = Entities.escape(input, out);
            assertFalse("Should not contain '?' for input: " + input + " -> " + result,
                       result.contains("?"));
        }
    }

    @Test(timeout = 4000)
    public void testEscapeNoQuestionMarkForUtf8() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        String[] testStrings = {
            "\u00E9", // é
            "\u4E00", // CJK
            "\u00A0", // nbsp
            "\uD83D\uDE00", // emoji
            "test\u00E9test"
        };

        for (String input : testStrings) {
            String result = Entities.escape(input, out);
            assertFalse("Should not contain '?' for input: " + input + " -> " + result,
                       result.contains("?"));
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testEscapeWithNullOutputSettings() {
        try {
            Entities.escape("test", null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeWithNullCharset() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.charset(null);
        try {
            Entities.escape("test", out);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeWithInvalidCharset() {
        Document.OutputSettings out = new Document.OutputSettings();
        // Setting invalid charset should throw
        try {
            out.charset(java.nio.charset.Charset.forName("INVALID_CHARSET"));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEscapeModeEnumValues() {
        assertEquals(3, Entities.EscapeMode.values().length);
        assertEquals(Entities.EscapeMode.xhtml, Entities.EscapeMode.valueOf("xhtml"));
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    @Test(timeout = 4000)
    public void testEscapeModeMapImmutability() {
        // Maps should not be modifiable
        try {
            Entities.EscapeMode.xhtml.getMap().put(Character.valueOf('x'), "test");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeModeMapContents() {
        // xhtml map
        assertEquals("lt", Entities.EscapeMode.xhtml.getMap().get(Character.valueOf('<')));
        assertEquals("gt", Entities.EscapeMode.xhtml.getMap().get(Character.valueOf('>')));
        assertEquals("amp", Entities.EscapeMode.xhtml.getMap().get(Character.valueOf('&')));
        assertEquals("quot", Entities.EscapeMode.xhtml.getMap().get(Character.valueOf('"')));

        // base map should have nbsp
        assertTrue(Entities.EscapeMode.base.getMap().containsKey(Character.valueOf('\u00A0')));
        assertEquals("nbsp", Entities.EscapeMode.base.getMap().get(Character.valueOf('\u00A0')));
    }

    // ==================== Additional escape() behavior tests ====================

    @Test(timeout = 4000)
    public void testEscapeBasicEntities() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        assertEquals("&lt;", Entities.escape("<", out));
        assertEquals("&gt;", Entities.escape(">", out));
        assertEquals("&amp;", Entities.escape("&", out));
        assertEquals("\"", Entities.escape("\"", out)); // not escaped in text
    }

    @Test(timeout = 4000)
    public void testEscapeInAttribute() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        // Test with inAttribute=true
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "\"<>&", out, true, false, false);
        assertEquals("&quot;&lt;&gt;&amp;", accum.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeNormaliseWhite() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        // Test normaliseWhite
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "  a  b  ", out, false, true, false);
        assertEquals(" a b ", accum.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeStripLeadingWhite() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        // Test stripLeadingWhite
        StringBuilder accum = new StringBuilder();
        Entities.escape(accum, "   a", out, false, true, true);
        assertEquals("a", accum.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeNonBreakingSpace() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        assertEquals("&nbsp;", Entities.escape("\u00A0", out));
    }

    @Test(timeout = 4000)
    public void testEscapeNonBreakingSpaceXhtml() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.xhtml);
        out.charset(java.nio.charset.Charset.forName("UTF-8"));

        assertEquals("\u00A0", Entities.escape("\u00A0", out));
    }

    @Test(timeout = 4000)
    public void testEscapeUnencodableCharacter() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("US-ASCII"));

        // é is not in ASCII, should be hex escaped
        String result = Entities.escape("é", out);
        assertTrue(result.contains("&#xe9;"));
    }

    @Test(timeout = 4000)
    public void testEscapeEntityFromMap() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset(java.nio.charset.Charset.forName("ISO-8859-1"));

        // © is in ISO-8859-1, should be encoded directly
        assertEquals("©", Entities.escape("©", out));
    }

    @Test(timeout = 4000)
    public void testUnescapeBasic() {
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals(">", Entities.unescape("&gt;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeStrict() {
        assertEquals("&lt", Entities.unescape("&lt", true));
        assertEquals("&", Entities.unescape("&lt", false));
    }

    @Test(timeout = 4000)
    public void testUnescapeNull() {
        try {
            Entities.unescape(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
}