package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ParseSettingsDeepseekTest
 *
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: ParseSettings
 * Branches:
 *   - normalizeTag: trim() always; lowerCase() if !preserveTagCase
 *   - normalizeAttribute: trim() always; lowerCase() if !preserveAttributeCase
 *   - normalizeAttributes: calls attributes.normalize() only if !preserveAttributeCase
 *   - preserveTagCase() / preserveAttributeCase() getters
 *
 * Boundary conditions:
 *   - null input for normalizeTag / normalizeAttribute (NPE expected)
 *   - empty string, whitespace-only string, mixed case strings
 *   - duplicate attribute keys (same case) with preserveAttributeCase = true (defect area)
 *   - duplicate attribute keys with preserveAttributeCase = false (should be deduplicated)
 *
 * Known defect (Defects4J ground truth):
 *   - When preserveAttributeCase is true, normalizeAttributes does NOT call attributes.normalize(),
 *     so duplicate attribute keys are not removed. This causes failures in parser tests.
 *   - The test `normalizeAttributes_duplicates_preserveCase` directly targets this defect.
 */
public class ParseSettingsDeepseekTest {

    // ========== Partition A: Core Functional Logic & State ==========

    @Test(timeout = 4000)
    public void htmlDefault_preserveTagCase_false() {
        ParseSettings settings = ParseSettings.htmlDefault;
        assertFalse(settings.preserveTagCase());
    }

    @Test(timeout = 4000)
    public void htmlDefault_preserveAttributeCase_false() {
        ParseSettings settings = ParseSettings.htmlDefault;
        assertFalse(settings.preserveAttributeCase());
    }

    @Test(timeout = 4000)
    public void preserveCase_preserveTagCase_true() {
        ParseSettings settings = ParseSettings.preserveCase;
        assertTrue(settings.preserveTagCase());
    }

    @Test(timeout = 4000)
    public void preserveCase_preserveAttributeCase_true() {
        ParseSettings settings = ParseSettings.preserveCase;
        assertTrue(settings.preserveAttributeCase());
    }

    @Test(timeout = 4000)
    public void constructor_setsFlags() {
        ParseSettings settings = new ParseSettings(true, false);
        assertTrue(settings.preserveTagCase());
        assertFalse(settings.preserveAttributeCase());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void normalizeTag_emptyString() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("", settings.normalizeTag(""));
    }

    @Test(timeout = 4000)
    public void normalizeTag_whitespaceOnly() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("", settings.normalizeTag("   "));
    }

    @Test(timeout = 4000)
    public void normalizeTag_trimmed() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("DIV", settings.normalizeTag("  DIV  "));
    }

    @Test(timeout = 4000)
    public void normalizeTag_lowerCaseWhenNotPreserved() {
        ParseSettings settings = new ParseSettings(false, true);
        assertEquals("div", settings.normalizeTag("DIV"));
    }

    @Test(timeout = 4000)
    public void normalizeTag_preserveCase() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("DIV", settings.normalizeTag("DIV"));
    }

    @Test(timeout = 4000)
    public void normalizeAttribute_emptyString() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("", settings.normalizeAttribute(""));
    }

    @Test(timeout = 4000)
    public void normalizeAttribute_whitespaceOnly() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("", settings.normalizeAttribute("   "));
    }

    @Test(timeout = 4000)
    public void normalizeAttribute_trimmed() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("CLASS", settings.normalizeAttribute("  CLASS  "));
    }

    @Test(timeout = 4000)
    public void normalizeAttribute_lowerCaseWhenNotPreserved() {
        ParseSettings settings = new ParseSettings(true, false);
        assertEquals("class", settings.normalizeAttribute("CLASS"));
    }

    @Test(timeout = 4000)
    public void normalizeAttribute_preserveCase() {
        ParseSettings settings = new ParseSettings(true, true);
        assertEquals("CLASS", settings.normalizeAttribute("CLASS"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void normalizeAttributes_duplicates_preserveCase() {
        // This test directly targets the known defect:
        // When preserveAttributeCase is true, normalizeAttributes does NOT call
        // attributes.normalize(), so duplicate keys are not removed.
        ParseSettings settings = new ParseSettings(true, true); // preserve attribute case
        Attributes attrs = new Attributes();
        attrs.add("One", "First");
        attrs.add("One", "Second"); // duplicate key (same case)
        Attributes result = settings.normalizeAttributes(attrs);
        // Expected: only the first occurrence should remain
        assertEquals(1, result.size());
        assertEquals("First", result.get("One"));
    }

    @Test(timeout = 4000)
    public void normalizeAttributes_duplicates_lowerCase() {
        // When not preserving case, normalize() is called and should remove duplicates.
        ParseSettings settings = new ParseSettings(true, false); // do not preserve attribute case
        Attributes attrs = new Attributes();
        attrs.add("One", "First");
        attrs.add("One", "Second"); // duplicate key
        Attributes result = settings.normalizeAttributes(attrs);
        assertEquals(1, result.size());
        assertEquals("First", result.get("one")); // key is lowercased
    }

    @Test(timeout = 4000)
    public void normalizeAttributes_noDuplicates_preserveCase() {
        ParseSettings settings = new ParseSettings(true, true);
        Attributes attrs = new Attributes();
        attrs.add("One", "First");
        attrs.add("Two", "Second");
        Attributes result = settings.normalizeAttributes(attrs);
        assertEquals(2, result.size());
        assertEquals("First", result.get("One"));
        assertEquals("Second", result.get("Two"));
    }

    @Test(timeout = 4000)
    public void normalizeAttributes_noDuplicates_lowerCase() {
        ParseSettings settings = new ParseSettings(true, false);
        Attributes attrs = new Attributes();
        attrs.add("One", "First");
        attrs.add("Two", "Second");
        Attributes result = settings.normalizeAttributes(attrs);
        assertEquals(2, result.size());
        assertEquals("First", result.get("one"));
        assertEquals("Second", result.get("two"));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void normalizeTag_null_throwsNPE() {
        ParseSettings settings = new ParseSettings(true, true);
        settings.normalizeTag(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void normalizeAttribute_null_throwsNPE() {
        ParseSettings settings = new ParseSettings(true, true);
        settings.normalizeAttribute(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void normalizeAttributes_null_throwsNPE() {
        ParseSettings settings = new ParseSettings(true, true);
        settings.normalizeAttributes(null);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void htmlDefault_isSingleton() {
        assertSame(ParseSettings.htmlDefault, ParseSettings.htmlDefault);
    }

    @Test(timeout = 4000)
    public void preserveCase_isSingleton() {
        assertSame(ParseSettings.preserveCase, ParseSettings.preserveCase);
    }

    @Test(timeout = 4000)
    public void htmlDefault_notSameAsPreserveCase() {
        assertNotSame(ParseSettings.htmlDefault, ParseSettings.preserveCase);
    }

    @Test(timeout = 4000)
    public void normalizeAttributes_returnsSameInstance() {
        ParseSettings settings = new ParseSettings(true, true);
        Attributes attrs = new Attributes();
        attrs.add("key", "value");
        assertSame(attrs, settings.normalizeAttributes(attrs));
    }
}