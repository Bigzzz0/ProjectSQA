package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for Entities class.
 * Targets line/branch coverage and the known defect where unescape fails for entity names containing digits.
 *
 * [Branch & Defect Analysis Matrix]
 * - escape(): branches: map.containsKey(c) -> true/false; encoder.canEncode(c) -> true/false.
 * - unescape(): branches: string.contains("&") -> true/false; regex match -> found/not found;
 *   group(3) != null (numeric) vs null (named); group(2) != null (hex) vs null (decimal);
 *   NumberFormatException catch; full.containsKey(name) -> true/false;
 *   charval != -1 || charval > 0xFFFF (defect: should be &&) -> replacement vs keep original.
 * - Defect: regex pattern [a-zA-Z]+ does not match entity names with digits (e.g., &frac34;).
 *   This causes the named entity branch to be skipped, leaving the entity unescaped.
 */
public class EntitiesDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testEscapeXhtml() {
        // XHTML mode: only lt, gt, amp, apos, quot
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.xhtml);
        // Use a charset encoder that can encode all characters (UTF-8)
        out.charset("UTF-8");
        String input = "<>&\"'";
        String expected = "&lt;&gt;&amp;&quot;&apos;";
        assertEquals(expected, Entities.escape(input, out));
    }

    @Test(timeout = 4000)
    public void testEscapeBase() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        String input = "©®";
        String expected = "&copy;&reg;";
        assertEquals(expected, Entities.escape(input, out));
    }

    @Test(timeout = 4000)
    public void testEscapeExtended() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.extended);
        out.charset("UTF-8");
        String input = "αβ";
        String expected = "&alpha;&beta;";
        assertEquals(expected, Entities.escape(input, out));
    }

    @Test(timeout = 4000)
    public void testEscapeUnencodableCharacter() {
        // Character not encodable in ISO-8859-1 -> numeric escape
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("ISO-8859-1"); // cannot encode Chinese
        String input = "新";
        String expected = "&#26032;";
        assertEquals(expected, Entities.escape(input, out));
    }

    @Test(timeout = 4000)
    public void testEscapeNoSpecialChars() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.xhtml);
        out.charset("UTF-8");
        String input = "hello world";
        assertEquals(input, Entities.escape(input, out));
    }

    @Test(timeout = 4000)
    public void testUnescapeNoAmpersand() {
        String input = "plain text";
        assertEquals(input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeNamedEntityWithoutDigits() {
        String input = "&amp;";
        assertEquals("&", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeDecimalNumeric() {
        String input = "&#60;";
        assertEquals("<", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexNumeric() {
        String input = "&#x3C;";
        assertEquals("<", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexLowercaseX() {
        String input = "&#x3c;";
        assertEquals("<", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeMultipleEntities() {
        String input = "&lt;&gt;&amp;";
        assertEquals("<>&", Entities.unescape(input));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testUnescapeEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeStringWithOnlyAmpersand() {
        assertEquals("&", Entities.unescape("&"));
    }

    @Test(timeout = 4000)
    public void testUnescapeStringWithIncompleteEntity() {
        // No semicolon, but pattern allows optional semicolon for named entities
        // However, the regex requires at least one letter; "&" alone doesn't match.
        assertEquals("&", Entities.unescape("&"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericOutOfRange() {
        // charval > 0xFFFF should not be replaced (defect: condition uses ||, but for numeric it may still replace incorrectly)
        // Actually, the bug is that for numeric entities with charval > 0xFFFF, the condition (charval != -1 || charval > 0xFFFF) is true,
        // so it replaces with truncated char. We test that the behavior is as per the buggy version? No, we test expected correct behavior.
        // The correct behavior: if charval > 0xFFFF, keep original. So we assert that the entity is kept.
        String input = "&#x110000;"; // > 0xFFFF
        // In the buggy version, this would be replaced with a truncated character (e.g., '\u0000').
        // We want to reveal the bug, so we assert the correct behavior (keep original).
        // But the test must fail on the buggy version. So we assert that the output is the original string.
        assertEquals(input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericInvalidFormat() {
        // NumberFormatException should be caught, charval remains -1, so original kept
        String input = "&#xZZZ;";
        assertEquals(input, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeNamedEntityNotFound() {
        // Not in map, should keep original
        String input = "&unknown;";
        assertEquals(input, Entities.unescape(input));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testUnescapeEntityWithDigits() {
        // This test directly targets the known defect: regex [a-zA-Z]+ fails to match entity names with digits.
        // The correct behavior is to replace &frac34; with ¾.
        String input = "&frac34;";
        String expected = "\u00BE"; // ¾
        assertEquals(expected, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeMultipleEntitiesWithDigits() {
        String input = "&frac12; &frac14; &sup1;";
        String expected = "\u00BD \u00BC \u00B9";
        assertEquals(expected, Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntityWithDigitsAndLetters() {
        // e.g., &frac34; already tested, also &sup2; etc.
        String input = "&sup2;";
        assertEquals("\u00B2", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntityWithDigitsNoSemicolon() {
        // The pattern allows optional semicolon for named entities, but digits may still cause failure.
        // However, the regex requires at least one letter; digits alone won't match.
        // But entities like &frac34 without semicolon should still match if the regex allowed digits.
        // Since the bug is that digits are not allowed, this will also fail.
        String input = "&frac34";
        // Expected: should be replaced with ¾ (since semicolon is optional for base entities)
        // But the buggy version will not match, so it will keep "&frac34".
        // We assert the correct behavior.
        assertEquals("\u00BE", Entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeMixedEntitiesWithAndWithoutDigits() {
        String input = "&lt;&frac34;&amp;";
        String expected = "<\u00BE&";
        assertEquals(expected, Entities.unescape(input));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testUnescapeNullInput() {
        // The method does not handle null; it will throw NullPointerException.
        // We test that it throws NPE as expected.
        try {
            Entities.unescape(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeNullInput() {
        // escape also does not handle null; will throw NPE.
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.base);
        out.charset("UTF-8");
        try {
            Entities.escape(null, out);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeNullEncoder() {
        // The escape method with explicit encoder and escapeMode
        // If encoder is null, canEncode will throw NPE.
        try {
            Entities.escape("test", null, Entities.EscapeMode.base);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    // Entities is a utility class with no instance state; no equals/hashCode/clone.
    // But we can test the enum EscapeMode getMap() method.

    @Test(timeout = 4000)
    public void testEscapeModeGetMap() {
        assertNotNull(Entities.EscapeMode.xhtml.getMap());
        assertNotNull(Entities.EscapeMode.base.getMap());
        assertNotNull(Entities.EscapeMode.extended.getMap());
        // Check that xhtml map contains only the five restricted entities
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('\u0022')); // quot
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('\u0026')); // amp
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('\u0027')); // apos
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('\u003C')); // lt
        assertTrue(Entities.EscapeMode.xhtml.getMap().containsKey('\u003E')); // gt
        assertEquals(5, Entities.EscapeMode.xhtml.getMap().size());
    }

    @Test(timeout = 4000)
    public void testBaseMapContainsDigitEntities() {
        // Ensure base map includes entities with digits (e.g., frac12)
        assertTrue(Entities.EscapeMode.base.getMap().containsKey('\u00BD')); // frac12
        assertTrue(Entities.EscapeMode.base.getMap().containsKey('\u00BE')); // frac34
    }

    @Test(timeout = 4000)
    public void testFullMapContainsDigitEntities() {
        assertTrue(Entities.EscapeMode.extended.getMap().containsKey('\u00BD'));
        assertTrue(Entities.EscapeMode.extended.getMap().containsKey('\u00BE'));
    }
}