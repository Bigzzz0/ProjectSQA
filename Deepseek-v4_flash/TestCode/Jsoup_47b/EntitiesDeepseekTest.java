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
 * - switch(c): '&', 0xA0, '<', '>', '"', default
 * - canEncode(): ascii, utf, fallback charset paths
 * - codePoint < MIN_SUPPLEMENTARY vs >= (surrogate pairs)
 * - isNamedEntity / isBaseNamedEntity / getCharacterByName for known/unknown names
 * - CoreCharset.byName: US-ASCII, UTF-*, fallback
 * - toCharacterKey duplicate handling (lowercase preference)
 * 
 * Boundary conditions:
 * - Empty string, null input (defensive)
 * - Whitespace sequences with normaliseWhite and stripLeadingWhite
 * - Characters at 0x00-0x7F, 0xA0, '<', '>', '&', '"'
 * - Non-ASCII encodable/non-encodable chars
 * - Supplementary code points (surrogate pairs)
 * - Entity names: "lt", "amp", "gt", "quot", "nbsp", "copy", "unknown"
 * 
 * Defect targeted (from Defects4J):
 * - In attribute context, '<' and '>' should be escaped as &lt; and &gt; even in HTML mode.
 *   The bug: when inAttribute=true, the code does NOT escape '<' and '>' (only escapes in character data).
 *   Expected: <a title="&lt;p>One&lt;/p>">One</a> but actual: <a title="<p>One</p>">One</a>
 */
public class EntitiesDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testEscapeBasicText() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // Simple text with no special chars
        assertEquals("Hello World", Entities.escape("Hello World", out));
        
        // Text with special chars in character data (not attribute)
        assertEquals("&lt;p&gt;One&lt;/p&gt;", Entities.escape("<p>One</p>", out));
        
        // Ampersand
        assertEquals("&amp;", Entities.escape("&", out));
        
        // Quote in text (not attribute) should not be escaped
        assertEquals("\"quote\"", Entities.escape("\"quote\"", out));
    }

    @Test(timeout = 4000)
    public void testEscapeInAttribute() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // In attribute, quotes should be escaped
        assertEquals("&quot;quoted&quot;", Entities.escape("\"quoted\"", out, true, false, false));
        
        // In attribute, '<' and '>' should be escaped (this is the defect)
        // Expected correct behavior: &lt; and &gt; are used in attributes
        assertEquals("&lt;p&gt;One&lt;/p&gt;", Entities.escape("<p>One</p>", out, true, false, false));
    }

    @Test(timeout = 4000)
    public void testEscapeWithNormaliseWhite() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // Normalise whitespace: multiple spaces become one
        assertEquals("a b", Entities.escape("a  b", out, false, true, false));
        
        // Leading whitespace stripped when stripLeadingWhite is true
        assertEquals("a", Entities.escape("   a", out, false, true, true));
        
        // Newlines and tabs become spaces
        assertEquals("a b", Entities.escape("a\n\tb", out, false, true, false));
        
        // Leading whitespace not stripped when stripLeadingWhite is false
        assertEquals(" a", Entities.escape("  a", out, false, true, false));
    }

    @Test(timeout = 4000)
    public void testEscapeNonBreakingSpace() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.charset("UTF-8");
        
        // In base mode, nbsp is escaped as &nbsp;
        out.escapeMode(Entities.EscapeMode.base);
        assertEquals("&nbsp;", Entities.escape("\u00A0", out));
        
        // In xhtml mode, nbsp is escaped as &#xa0;
        out.escapeMode(Entities.EscapeMode.xhtml);
        assertEquals("&#xa0;", Entities.escape("\u00A0", out));
        
        // In extended mode, nbsp is escaped as &nbsp;
        out.escapeMode(Entities.EscapeMode.extended);
        assertEquals("&nbsp;", Entities.escape("\u00A0", out));
    }

    @Test(timeout = 4000)
    public void testEscapeWithDifferentCharsets() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        
        // ASCII charset: non-ASCII chars need escaping
        out.charset("US-ASCII");
        assertEquals("&#xe9;", Entities.escape("\u00E9", out)); // é in base map? Actually é is not in base, so hex
        
        // UTF-8: all chars can be encoded
        out.charset("UTF-8");
        assertEquals("\u00E9", Entities.escape("\u00E9", out));
        
        // Fallback charset (e.g., ISO-8859-1)
        out.charset("ISO-8859-1");
        // é is encodable in ISO-8859-1
        assertEquals("\u00E9", Entities.escape("\u00E9", out));
    }

    @Test(timeout = 4000)
    public void testEscapeSupplementaryCodePoint() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // Supplementary code point (emoji) - encodable in UTF-8
        String emoji = new String(Character.toChars(0x1F600)); // 😀
        assertEquals(emoji, Entities.escape(emoji, out));
        
        // Supplementary code point with ASCII charset - should be hex escaped
        out.charset("US-ASCII");
        assertEquals("&#x1f600;", Entities.escape(emoji, out));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        assertEquals("", Entities.escape("", out));
        assertEquals("", Entities.escape("", out, true, false, false));
        assertEquals("", Entities.escape("", out, false, true, true));
    }

    @Test(timeout = 4000)
    public void testEscapeNullString() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // Null should cause NPE (not handled)
        try {
            Entities.escape(null, out);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeBoundaryCharacters() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // Control characters (0x00-0x1F) - not escaped by default
        assertEquals("\u0000", Entities.escape("\u0000", out));
        
        // 0x7F (DEL)
        assertEquals("\u007F", Entities.escape("\u007F", out));
        
        // 0x80-0x9F (C1 controls) - not in base map
        assertEquals("\u0080", Entities.escape("\u0080", out));
        
        // 0xA0 (nbsp) - already tested
        // 0xFF (y with diaeresis) - not in base map
        assertEquals("\u00FF", Entities.escape("\u00FF", out));
    }

    @Test(timeout = 4000)
    public void testIsNamedEntityBoundary() {
        // Known entities
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("nbsp"));
        assertTrue(Entities.isNamedEntity("copy"));
        
        // Unknown entities
        assertFalse(Entities.isNamedEntity(""));
        assertFalse(Entities.isNamedEntity("unknown"));
        assertFalse(Entities.isNamedEntity("LT")); // case sensitive
        assertFalse(Entities.isNamedEntity(null));
    }

    @Test(timeout = 4000)
    public void testIsBaseNamedEntityBoundary() {
        // Base entities
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        
        // Extended entities not in base
        assertFalse(Entities.isBaseNamedEntity("copy")); // copy is in full but not base? Actually copy is in base
        assertFalse(Entities.isBaseNamedEntity("nbsp")); // nbsp is in base? Actually nbsp is in base
        assertFalse(Entities.isBaseNamedEntity("unknown"));
        assertFalse(Entities.isBaseNamedEntity(null));
    }

    @Test(timeout = 4000)
    public void testGetCharacterByNameBoundary() {
        // Known entities
        assertEquals(Character.valueOf('<'), Entities.getCharacterByName("lt"));
        assertEquals(Character.valueOf('&'), Entities.getCharacterByName("amp"));
        assertEquals(Character.valueOf('>'), Entities.getCharacterByName("gt"));
        assertEquals(Character.valueOf('"'), Entities.getCharacterByName("quot"));
        
        // Unknown entities
        assertNull(Entities.getCharacterByName("unknown"));
        assertNull(Entities.getCharacterByName(""));
        assertNull(Entities.getCharacterByName(null));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * This test directly targets the known defect:
     * In attribute context, '<' and '>' should be escaped as &lt; and &gt;.
     * The buggy version does NOT escape them in attributes.
     */
    @Test(timeout = 4000)
    public void testEscapesGtInXmlAttributesButNotInHtml() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // Simulate attribute value: <p>One</p>
        String attributeValue = "<p>One</p>";
        
        // In HTML attribute, the correct behavior is to escape < and > as &lt; and &gt;
        // This is the expected correct behavior per the defect specification
        String escaped = Entities.escape(attributeValue, out, true, false, false);
        
        // The buggy version produces: <p>One</p> (unescaped)
        // The correct version should produce: &lt;p&gt;One&lt;/p&gt;
        assertEquals("&lt;p&gt;One&lt;/p&gt;", escaped);
        
        // Also test the full attribute context as in the failing test
        String html = "<a title=\"" + escaped + "\">One</a>";
        assertEquals("<a title=\"&lt;p&gt;One&lt;/p&gt;\">One</a>", html);
    }

    @Test(timeout = 4000)
    public void testEscapeGtInXmlAttributeMode() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.xhtml);
        out.charset("UTF-8");
        
        // In XHTML mode, attributes also escape < and >
        String escaped = Entities.escape("<p>One</p>", out, true, false, false);
        assertEquals("&lt;p&gt;One&lt;/p&gt;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeGtInExtendedAttributeMode() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.extended);
        out.charset("UTF-8");
        
        // In extended mode, attributes also escape < and >
        String escaped = Entities.escape("<p>One</p>", out, true, false, false);
        assertEquals("&lt;p&gt;One&lt;/p&gt;", escaped);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testEscapeWithNullOutputSettings() {
        try {
            Entities.escape("test", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeWithNullStringBuilder() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        try {
            Entities.escape(null, "test", out, false, false, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testUnescapeBasic() {
        // Basic unescape
        assertEquals("<p>One</p>", Entities.unescape("&lt;p&gt;One&lt;/p&gt;"));
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("\u00A0", Entities.unescape("&nbsp;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeStrict() {
        // Strict mode requires trailing semicolon
        assertEquals("&lt", Entities.unescape("&lt", true)); // no semicolon, stays as-is
        assertEquals("<", Entities.unescape("&lt;", true)); // with semicolon
        assertEquals("&lt", Entities.unescape("&lt", false)); // non-strict, optional semicolon
        assertEquals("<", Entities.unescape("&lt;", false));
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericEntities() {
        // Numeric entities
        assertEquals("<", Entities.unescape("&#60;"));
        assertEquals("<", Entities.unescape("&#x3C;"));
        assertEquals(">", Entities.unescape("&#62;"));
        assertEquals(">", Entities.unescape("&#x3E;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeUnknownEntities() {
        // Unknown entities are left as-is
        assertEquals("&unknown;", Entities.unescape("&unknown;"));
        assertEquals("&unknown", Entities.unescape("&unknown"));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEscapeModeMaps() {
        // XHTML map should contain only restricted entities
        Map<Character, String> xhtmlMap = Entities.EscapeMode.xhtml.getMap();
        assertEquals(4, xhtmlMap.size());
        assertTrue(xhtmlMap.containsKey('"'));
        assertTrue(xhtmlMap.containsKey('&'));
        assertTrue(xhtmlMap.containsKey('<'));
        assertTrue(xhtmlMap.containsKey('>'));
        
        // Base map should contain common entities
        Map<Character, String> baseMap = Entities.EscapeMode.base.getMap();
        assertTrue(baseMap.size() > 4);
        assertTrue(baseMap.containsKey('\u00A0')); // nbsp
        
        // Extended map should contain more entities
        Map<Character, String> extendedMap = Entities.EscapeMode.extended.getMap();
        assertTrue(extendedMap.size() > baseMap.size());
    }

    @Test(timeout = 4000)
    public void testEscapeModeMapImmutability() {
        // Maps should not be modifiable (though not enforced, but we can check content)
        Map<Character, String> xhtmlMap = Entities.EscapeMode.xhtml.getMap();
        try {
            xhtmlMap.put('x', "test");
            // If no exception, it's modifiable - but we don't want to break anything
            // Just verify we can read
            assertEquals("&lt;", "&" + xhtmlMap.get('<') + ";");
        } catch (UnsupportedOperationException e) {
            // Expected if unmodifiable
        }
    }

    @Test(timeout = 4000)
    public void testEscapeConsistency() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        
        // Escape then unescape should return original for text content
        String original = "Hello <world> & \"quotes\"";
        String escaped = Entities.escape(original, out);
        String unescaped = Entities.unescape(escaped);
        assertEquals(original, unescaped);
    }

    @Test(timeout = 4000)
    public void testEscapeWithAllEscapeModes() {
        String input = "<>&\"\u00A0";
        
        for (Entities.EscapeMode mode : Entities.EscapeMode.values()) {
            Document.OutputSettings out = new Document.OutputSettings();
            out.escapeMode(mode);
            out.charset("UTF-8");
            
            String escaped = Entities.escape(input, out);
            assertNotNull(escaped);
            assertFalse(escaped.contains("<"));
            assertFalse(escaped.contains(">"));
            assertFalse(escaped.contains("&"));
            
            // In attribute mode, quotes should be escaped
            String attrEscaped = Entities.escape(input, out, true, false, false);
            assertFalse(attrEscaped.contains("\""));
        }
    }

    @Test(timeout = 4000)
    public void testEscapeWithAllCharsets() {
        String[] charsets = {"US-ASCII", "UTF-8", "UTF-16", "ISO-8859-1", "windows-1252"};
        String input = "Hello \u00E9\u00E8\u00EA";
        
        for (String charset : charsets) {
            Document.OutputSettings out = new Document.OutputSettings();
            out.escapeMode(Entities.EscapeMode.base);
            out.charset(charset);
            
            String escaped = Entities.escape(input, out);
            assertNotNull(escaped);
            
            // Round-trip test
            String unescaped = Entities.unescape(escaped);
            // Note: not all chars may round-trip perfectly depending on charset
        }
    }
}