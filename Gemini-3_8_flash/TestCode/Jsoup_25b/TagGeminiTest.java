package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Tag
 *
 * 1. Defects4J Known Defect:
 *    - HtmlParserTest::preservesSpaceInTextArea reveals that <textarea> must preserve whitespace.
 *    - In Tag.java, preserveWhitespaceTags = {"pre", "plaintext", "title"}.
 *    - "textarea" is missing from preserveWhitespaceTags despite field documentation:
 *      "private boolean preserveWhitespace = false; // for pre, textarea, script etc".
 *    - Tag.valueOf("textarea").preserveWhitespace() must evaluate to true.
 *
 * 2. Decision Branches Covered:
 *    - valueOf(String):
 *      * null check -> Validate.notNull
 *      * empty check after trim -> Validate.notEmpty
 *      * known tag lookup (cache hit) vs unknown tag (cache miss, instantiated with inline=true, canContainBlock=true)
 *    - isBlock() / isInline():
 *      * true/false for block tags (e.g. div, p)
 *      * false/true for inline tags (e.g. span, a)
 *      * false/true for unknown tags
 *    - formatAsBlock():
 *      * true for block tags not in formatAsInlineTags (e.g. div)
 *      * false for formatAsInlineTags (e.g. p, title, a) and inline tags
 *    - canContainBlock():
 *      * true for block tags and unknown tags
 *      * false for inline tags
 *    - isData():
 *      * evaluates (!canContainInline && !isEmpty())
 *      * short-circuit branch on canContainInline == true vs false
 *      * second condition branch on isEmpty() == true vs false
 *    - isEmpty():
 *      * true for empty tags (e.g. img, br, meta)
 *      * false for normal tags
 *    - isSelfClosing():
 *      * true if empty is true
 *      * true if empty is false but selfClosing is true (via setSelfClosing())
 *      * false if both empty and selfClosing are false
 *    - isKnownTag() / isKnownTag(String):
 *      * instance and static variants with registered vs unregistered tag names
 *      * case-sensitivity check on static isKnownTag() vs valueOf()
 *    - preserveWhitespace():
 *      * true for pre, plaintext, title, and textarea (defect trigger)
 *      * false for normal tags
 *    - equals(Object) / hashCode():
 *      * this == o
 *      * !(o instanceof Tag)
 *      * canContainBlock divergence
 *      * canContainInline divergence
 *      * empty divergence
 *      * formatAsBlock divergence
 *      * isBlock divergence
 *      * preserveWhitespace divergence
 *      * selfClosing divergence
 *      * tagName divergence
 *      * all fields equal (true)
 */
public class TagGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardBlockTagProperties() {
        Tag div = Tag.valueOf("div");
        assertEquals("div", div.getName());
        assertEquals("div", div.toString());
        assertTrue(div.isBlock());
        assertFalse(div.isInline());
        assertTrue(div.canContainBlock());
        assertTrue(div.formatAsBlock());
        assertFalse(div.isEmpty());
        assertFalse(div.isSelfClosing());
        assertFalse(div.preserveWhitespace());
        assertFalse(div.isData());
        assertTrue(div.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testStandardInlineTagProperties() {
        Tag span = Tag.valueOf("span");
        assertEquals("span", span.getName());
        assertFalse(span.isBlock());
        assertTrue(span.isInline());
        assertFalse(span.canContainBlock());
        assertFalse(span.formatAsBlock());
        assertFalse(span.isEmpty());
        assertFalse(span.isSelfClosing());
        assertFalse(span.preserveWhitespace());
        assertFalse(span.isData());
        assertTrue(span.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testEmptyTagProperties() {
        Tag img = Tag.valueOf("img");
        assertEquals("img", img.getName());
        assertFalse(img.isBlock());
        assertTrue(img.isInline());
        assertFalse(img.canContainBlock());
        assertFalse(img.formatAsBlock());
        assertTrue(img.isEmpty());
        assertTrue(img.isSelfClosing());
        assertFalse(img.preserveWhitespace());
        assertFalse(img.isData());
        assertTrue(img.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testFormatAsInlineBlockTag() {
        Tag p = Tag.valueOf("p");
        assertTrue(p.isBlock());
        assertFalse(p.isInline());
        assertTrue(p.canContainBlock());
        assertFalse("p tag should format as inline", p.formatAsBlock());
        assertFalse(p.isEmpty());
        assertFalse(p.isSelfClosing());
        assertTrue(p.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceKnownTags() {
        Tag pre = Tag.valueOf("pre");
        assertTrue(pre.preserveWhitespace());
        assertFalse(pre.formatAsBlock());

        Tag plaintext = Tag.valueOf("plaintext");
        assertTrue(plaintext.preserveWhitespace());

        Tag title = Tag.valueOf("title");
        assertTrue(title.preserveWhitespace());
        assertFalse(title.formatAsBlock());
    }

    @Test(timeout = 4000)
    public void testUnknownTagCreationAndDefaultState() {
        Tag custom = Tag.valueOf("custom-tag");
        assertEquals("custom-tag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.isInline());
        assertTrue(custom.canContainBlock());
        assertTrue(custom.formatAsBlock());
        assertFalse(custom.isEmpty());
        assertFalse(custom.isSelfClosing());
        assertFalse(custom.preserveWhitespace());
        assertFalse(custom.isData());
        assertFalse(custom.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testSetSelfClosingOnUnknownTag() {
        Tag custom = Tag.valueOf("custom-self-closing");
        assertFalse(custom.isSelfClosing());
        Tag returned = custom.setSelfClosing();
        assertSame(custom, returned);
        assertTrue(custom.isSelfClosing());
        assertFalse(custom.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsKnownTagStaticAndInstance() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("p"));
        assertTrue(Tag.isKnownTag("img"));
        assertFalse(Tag.isKnownTag("unknown-element"));

        // Static isKnownTag does not lowercase its input
        assertFalse("Static isKnownTag is exact on raw key", Tag.isKnownTag("DIV"));

        // ValueOf converts case before resolution
        Tag divUpper = Tag.valueOf("DIV");
        assertTrue(divUpper.isKnownTag());
        assertEquals("div", divUpper.getName());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testValueOfTrimmingAndNormalization() {
        Tag divSpaced = Tag.valueOf("  \t div \n ");
        assertSame(Tag.valueOf("div"), divSpaced);
        assertEquals("div", divSpaced.getName());

        Tag customMixed = Tag.valueOf("   cUsToM-TaG   ");
        assertEquals("custom-tag", customMixed.getName());
    }

    @Test(timeout = 4000)
    public void testKnownTagsIdentityEquality() {
        Tag div1 = Tag.valueOf("div");
        Tag div2 = Tag.valueOf("DIV");
        assertSame("Predefined tags must return singleton instance", div1, div2);
    }

    @Test(timeout = 4000)
    public void testUnknownTagsFreshInstances() {
        Tag custom1 = Tag.valueOf("custom-element");
        Tag custom2 = Tag.valueOf("custom-element");
        assertNotSame("Unknown tags must generate distinct instances", custom1, custom2);
        assertEquals("Unknown tags with same state must be equal", custom1, custom2);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J bug: HtmlParserTest::preservesSpaceInTextArea.
     * <textarea> is an HTML tag that must have preserveWhitespace set to true.
     */
    @Test(timeout = 4000)
    public void testPreservesWhitespaceInTextarea() {
        Tag textarea = Tag.valueOf("textarea");
        assertTrue("Tag 'textarea' must preserve whitespace", textarea.preserveWhitespace());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNullThrowsException() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfEmptyStringThrowsException() {
        Tag.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfWhitespaceOnlyThrowsException() {
        Tag.valueOf("    \t  \n ");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals & hashCode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        Tag tag = Tag.valueOf("div");
        assertTrue(tag.equals(tag));
    }

    @Test(timeout = 4000)
    public void testEqualsNullOrOtherType() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("div"));
        assertFalse(tag.equals(new Object()));
    }

    @Test(timeout = 4000)
    public void testEqualsCanContainBlockDivergence() {
        // Unknown tag: canContainBlock = true, isBlock = false
        // span: canContainBlock = false, isBlock = false
        Tag unknown = Tag.valueOf("custom-block-test");
        Tag span = Tag.valueOf("span");
        assertFalse(unknown.equals(span));
    }

    @Test(timeout = 4000)
    public void testEqualsCanContainInlineDivergence() {
        // span: canContainBlock = false, canContainInline = true
        // img: canContainBlock = false, canContainInline = false
        Tag span = Tag.valueOf("span");
        Tag img = Tag.valueOf("img");
        assertFalse(span.equals(img));
    }

    @Test(timeout = 4000)
    public void testEqualsFormatAsBlockDivergence() {
        // div: formatAsBlock = true, canContainBlock = true, canContainInline = true, isBlock = true
        // p: formatAsBlock = false, canContainBlock = true, canContainInline = true, isBlock = true
        Tag div = Tag.valueOf("div");
        Tag p = Tag.valueOf("p");
        assertFalse(div.equals(p));
    }

    @Test(timeout = 4000)
    public void testEqualsIsBlockDivergence() {
        // Unknown tag: canContainBlock = true, canContainInline = true, empty = false, formatAsBlock = true, isBlock = false
        // div: canContainBlock = true, canContainInline = true, empty = false, formatAsBlock = true, isBlock = true
        Tag unknown = Tag.valueOf("custom-div-like");
        Tag div = Tag.valueOf("div");
        assertFalse(unknown.equals(div));
    }

    @Test(timeout = 4000)
    public void testEqualsPreserveWhitespaceDivergence() {
        // p: formatAsBlock = false, isBlock = true, preserveWhitespace = false
        // pre: formatAsBlock = false, isBlock = true, preserveWhitespace = true
        Tag p = Tag.valueOf("p");
        Tag pre = Tag.valueOf("pre");
        assertFalse(p.equals(pre));
    }

    @Test(timeout = 4000)
    public void testEqualsSelfClosingDivergence() {
        Tag tag1 = Tag.valueOf("custom-self-flag");
        Tag tag2 = Tag.valueOf("custom-self-flag").setSelfClosing();
        assertFalse(tag1.equals(tag2));
        assertFalse(tag2.equals(tag1));
    }

    @Test(timeout = 4000)
    public void testEqualsTagNameDivergence() {
        Tag tag1 = Tag.valueOf("custom-a");
        Tag tag2 = Tag.valueOf("custom-b");
        assertFalse(tag1.equals(tag2));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeConsistency() {
        Tag tag1 = Tag.valueOf("custom-equal");
        Tag tag2 = Tag.valueOf("custom-equal");

        assertTrue(tag1.equals(tag2));
        assertTrue(tag2.equals(tag1));
        assertEquals("Equal objects must have identical hashCodes", tag1.hashCode(), tag2.hashCode());

        Tag tag3 = Tag.valueOf("custom-equal").setSelfClosing();
        assertFalse(tag1.equals(tag3));
        assertNotEquals(tag1.hashCode(), tag3.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeKnownTagsDivergence() {
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");
        assertNotEquals(div.hashCode(), span.hashCode());
    }
}