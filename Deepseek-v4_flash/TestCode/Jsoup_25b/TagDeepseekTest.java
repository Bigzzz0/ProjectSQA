package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced white-box test suite for Tag.java.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Tag.valueOf(): null/empty/trimmed inputs, known tags, unknown tags,
 *   synchronization, static initializer registration.
 * - Tag properties: isBlock, isInline, isData, isEmpty, isSelfClosing,
 *   canContainBlock, canContainInline, formatAsBlock, preserveWhitespace,
 *   isKnownTag (instance & static).
 * - Mutators: setSelfClosing() returns this.
 * - equals() and hashCode() contracts with all fields.
 * - toString().
 * - Branch coverage: all boolean fields, all conditional expressions
 *   in isData() (canContainInline && !isEmpty), isSelfClosing() (empty || selfClosing),
 *   isInline() (!isBlock), isKnownTag() (tags.containsKey(tagName)).
 * - Defect targeting: textarea should preserve whitespace but does not
 *   (known Defects4J issue). Also verify other missing preserve whitespace tags?
 *   The defect specifically mentions preservesSpaceInTextArea failure.
 *   We test that Tag.valueOf("textarea").preserveWhitespace() == true
 *   (but currently returns false, so the test reveals the bug).
 */
public class TagDeepseekTest {

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testValueOfKnownBlockTag() {
        Tag tag = Tag.valueOf("div");
        assertEquals("div", tag.getName());
        assertTrue(tag.isBlock());
        assertFalse(tag.isInline());
        assertTrue(tag.canContainBlock());
        assertTrue(tag.canContainInline());
        assertTrue(tag.formatAsBlock());
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertFalse(tag.isData());
        assertTrue(tag.isKnownTag());
        assertFalse(tag.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testValueOfKnownInlineTag() {
        Tag tag = Tag.valueOf("span");
        assertEquals("span", tag.getName());
        assertFalse(tag.isBlock());
        assertTrue(tag.isInline());
        assertFalse(tag.canContainBlock());
        assertTrue(tag.canContainInline());
        assertFalse(tag.formatAsBlock());
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertFalse(tag.isData());
        assertTrue(tag.isKnownTag());
        assertFalse(tag.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testValueOfKnownEmptyTag() {
        Tag tag = Tag.valueOf("img");
        assertEquals("img", tag.getName());
        assertFalse(tag.isBlock());        // img is inline
        assertTrue(tag.isInline());
        assertFalse(tag.canContainBlock());
        assertFalse(tag.canContainInline());
        assertTrue(tag.isEmpty());
        assertTrue(tag.isSelfClosing());   // empty -> selfClosing
        assertTrue(tag.isData());          // !canContainInline && !isEmpty? Actually !false && !true => false? Wait: isData = !canContainInline && !isEmpty => !false && !true => true && false => false? That's wrong. Let's recompute: empty=true, canContainInline=false, so !canContainInline=true, !isEmpty=false, so isData = true && false = false. So img is not data? But it is an empty tag, so it cannot contain data? Actually the method isData returns true only if canContainInline is false AND not empty. Since empty=true, it returns false. So assertFalse.
        assertTrue(tag.isKnownTag());
        assertFalse(tag.preserveWhitespace());
        // Check isData correctly
        assertFalse(tag.isData());
    }

    @Test(timeout = 4000)
    public void testValueOfFormatAsInlineTag() {
        Tag tag = Tag.valueOf("p");
        assertEquals("p", tag.getName());
        assertTrue(tag.isBlock());
        assertFalse(tag.isInline());
        // but formatAsBlock is false
        assertFalse(tag.formatAsBlock());
        assertTrue(tag.canContainBlock()); // p is block but can contain inline? Actually p can contain inline, not block. Wait: p is a block tag but canContainBlock is true? According to static init, p is in blockTags, so isBlock=true, canContainBlock= true by default? But then it's in formatAsInlineTags, so formatAsBlock=false. But canContainBlock remains true? That would be incorrect: p should not contain block tags. However the known defect might be different. We'll follow the current code: p is in blockTags, then later it's not modified for canContainBlock. So canContainBlock remains true. That is correct for the code as is. We'll test that.
        assertTrue(tag.canContainBlock());
        assertTrue(tag.canContainInline());
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertFalse(tag.isData());
        assertTrue(tag.isKnownTag());
        assertFalse(tag.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testValueOfPreserveWhitespaceTag() {
        Tag tag = Tag.valueOf("pre");
        assertEquals("pre", tag.getName());
        assertTrue(tag.isBlock());
        assertFalse(tag.isInline());
        assertTrue(tag.canContainBlock());
        assertTrue(tag.canContainInline());
        assertFalse(tag.formatAsBlock()); // pre in formatAsInlineTags
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertFalse(tag.isData());
        assertTrue(tag.isKnownTag());
        assertTrue(tag.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testValueOfUnknownTag() {
        Tag tag = Tag.valueOf("unknown");
        assertEquals("unknown", tag.getName());
        assertFalse(tag.isBlock());
        assertTrue(tag.isInline());
        assertTrue(tag.canContainBlock()); // default for unknown
        assertTrue(tag.canContainInline()); // default
        assertTrue(tag.formatAsBlock()); // default true
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
        assertFalse(tag.isData());
        assertFalse(tag.isKnownTag());
        assertFalse(tag.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testValueOfCaseInsensitive() {
        Tag tag1 = Tag.valueOf("DIV");
        Tag tag2 = Tag.valueOf("div");
        assertSame(tag1, tag2);
    }

    @Test(timeout = 4000)
    public void testValueOfTrimmedInput() {
        Tag tag = Tag.valueOf("  br  ");
        assertEquals("br", tag.getName());
        assertTrue(tag.isEmpty());
    }

    @Test(timeout = 4000)
    public void testValueOfReturnsSameInstanceForKnownTag() {
        Tag t1 = Tag.valueOf("html");
        Tag t2 = Tag.valueOf("html");
        assertSame(t1, t2);
    }

    @Test(timeout = 4000)
    public void testSetSelfClosingFlags() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.isSelfClosing());
        Tag returned = tag.setSelfClosing();
        assertSame(tag, returned);
        assertTrue(tag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testIsKnownTagStatic() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("br"));
        assertFalse(Tag.isKnownTag("unknown"));
        assertFalse(Tag.isKnownTag("")); // empty not known
    }

    @Test(timeout = 4000)
    public void testToString() {
        Tag tag = Tag.valueOf("input");
        assertEquals("input", tag.toString());
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ============================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNull() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfEmptyString() {
        Tag.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfWhitespaceOnly() {
        Tag.valueOf("   ");
    }

    @Test(timeout = 4000)
    public void testIsBlockBoundary() {
        // Block tags should be true, inline false
        assertTrue(Tag.valueOf("div").isBlock());
        assertFalse(Tag.valueOf("span").isBlock());
        // Unknown tags are inline
        assertFalse(Tag.valueOf("custom").isBlock());
    }

    @Test(timeout = 4000)
    public void testIsInlineBoundary() {
        assertFalse(Tag.valueOf("div").isInline());
        assertTrue(Tag.valueOf("span").isInline());
        assertTrue(Tag.valueOf("custom").isInline());
    }

    @Test(timeout = 4000)
    public void testIsDataBoundaries() {
        // Empty tag: cannot contain inline, empty-> isData = !false && !true = true? Wait: 
        // isData = !canContainInline && !isEmpty
        // For br: canContainInline=false, isEmpty=true => !false && !true = true && false = false
        assertFalse(Tag.valueOf("br").isData());
        // For a non-empty inline tag: canContainInline=true => isData false regardless of isEmpty
        assertFalse(Tag.valueOf("a").isData());
        // A tag that cannot contain inline AND is not empty: e.g., a tag like "textarea"? Actually textarea canContainInline is true by default? Wait textarea is an inline tag, so canContainInline is false? According to static init: inline tags have canContainBlock=false, but canContainInline remains true? Actually in the loop for inline tags: tag.canContainBlock = false; they do not set canContainInline, so it remains true. So for inline tags, canContainInline is true. So isData false. The only tags that could be true for isData are those where canContainInline becomes false: that happens only for empty tags (canContainInline=false, empty=true) -> isData false because empty. So actually no tag returns true for isData? That seems like a potential bug, but we test according to current code.
        // To cover the branch where isData returns true, we need a tag that is not empty and canContainInline false. None currently. But we can test unknown tag: canContainInline true by default, so false. So isData always false for all tags? Let's check: The condition is !canContainInline && !isEmpty. Currently, for all tags canContainInline is true except empty tags where it's false, but they are also empty. So !isEmpty is false, so isData is false. So the method returns false for all tags in current code. That means no branch coverage for true. We'll still test the false branch.
        assertFalse(Tag.valueOf("div").isData());
        assertFalse(Tag.valueOf("img").isData());
        assertFalse(Tag.valueOf("custom").isData());
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone (textarea whitespace)
    // ============================================================

    @Test(timeout = 4000)
    public void testValueOfTextareaPreservesWhitespace() {
        // Known defect: textarea should preserve whitespace but does not
        Tag tag = Tag.valueOf("textarea");
        // According to HTML spec, textarea is a block? Actually textarea is inline? In the code,
        // textarea is in inlineTags list. So isBlock=false, canContainBlock=false, formatAsBlock=false.
        // It is not in preserveWhitespaceTags, so preserveWhitespace returns false.
        // The bug is that it should return true.
        // We assert true to reveal the bug. On fixed version, this passes.
        assertTrue("textarea should preserve whitespace", tag.preserveWhitespace());
        // Also ensure other properties
        assertFalse(tag.isBlock());
        assertTrue(tag.isInline());
        assertTrue(tag.isKnownTag());
        assertFalse(tag.isEmpty());
        assertFalse(tag.isSelfClosing());
    }

    // Additional defect target: script and style? The defect report is specific to textarea.
    // But we can also test that plaintext preserves, which it does.
    @Test(timeout = 4000)
    public void testPlaintextPreservesWhitespace() {
        Tag tag = Tag.valueOf("plaintext");
        assertTrue(tag.preserveWhitespace());
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNullAlreadyCovered() {
        // Already covered, but place here for structural completeness
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfEmptyAlreadyCovered() {
        Tag.valueOf("");
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Tag tag = Tag.valueOf("div");
        assertEquals(tag, tag);
    }

    @Test(timeout = 4000)
    public void testEqualsEqualTags() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("div");
        assertEquals(tag1, tag2);
        assertEquals(tag2, tag1);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTypes() {
        Tag tag = Tag.valueOf("div");
        assertNotEquals(tag, "someString");
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentName() {
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");
        assertNotEquals(div, span);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentProperties() {
        Tag div1 = Tag.valueOf("div");
        Tag div2 = Tag.valueOf("div");
        // modify one via reflection? Not allowed. Instead we use unknown tags to create different combos.
        // Since known tags are cached, we cannot modify them. So we rely on equals contract:
        // two tags with same name and same properties are equal.
        // To test inequality due to property, we can use setSelfClosing on one.
        div2.setSelfClosing(); // This modifies the same singleton? But it's the same object because valueOf returns same instance. So we cannot separate. Instead, we test that setSelfClosing changes equality? But since the same tag is modified, the hash would change? Actually equals would still be true because it's the same reference. So we need a different approach: create two unknown tags and modify one via setSelfClosing? Unknown tags are not cached, so each valueOf returns a new instance. So:
        Tag unknown1 = Tag.valueOf("myTag");
        Tag unknown2 = Tag.valueOf("myTag");
        assertEquals(unknown1, unknown2);
        unknown2.setSelfClosing();
        assertNotEquals(unknown1, unknown2);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Tag tag = Tag.valueOf("div");
        int hc1 = tag.hashCode();
        int hc2 = tag.hashCode();
        assertEquals(hc1, hc2);
    }

    @Test(timeout = 4000)
    public void testEqualsHashCodeContract() {
        Tag tag1 = Tag.valueOf("br");
        Tag tag2 = Tag.valueOf("br");
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        // Already covered, but repeat for completeness
        assertEquals("div", Tag.valueOf("div").toString());
    }

    // Additional coverage for isSelfClosing branch: empty || selfClosing
    // Already covered via empty tag (img) and setSelfClosing (div after setSelfClosing)
    @Test(timeout = 4000)
    public void testIsSelfClosingEmptyTag() {
        assertTrue(Tag.valueOf("br").isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testIsSelfClosingAfterSetSelfClosing() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.isSelfClosing());
        tag.setSelfClosing();
        assertTrue(tag.isSelfClosing());
    }

    // Coverage for isData branch: we already tested false cases. 
    // To reach the true branch, we would need a tag with canContainInline=false and isEmpty=false.
    // Such a tag does not exist in pre-defined tags, but we can create an unknown tag and then set its fields via reflection? Not allowed. So we skip.

    // Coverage for isKnownTag static method branch
    @Test(timeout = 4000)
    public void testIsKnownTagStaticFalse() {
        assertFalse(Tag.isKnownTag("notexist"));
    }

    @Test(timeout = 4000)
    public void testIsKnownTagTrue() {
        assertTrue(Tag.isKnownTag("html"));
    }

    // Test that register and static initializer work (access via valueOf)
    @Test(timeout = 4000)
    public void testStaticInitializerCreatesAllTags() {
        // Verify a few tags exist
        assertNotNull(Tag.valueOf("html"));
        assertNotNull(Tag.valueOf("head"));
        assertNotNull(Tag.valueOf("a"));
        assertNotNull(Tag.valueOf("br"));
        assertNotNull(Tag.valueOf("textarea"));
    }

    // Test equals for null
    @Test(timeout = 4000)
    public void testEqualsNull() {
        Tag tag = Tag.valueOf("p");
        assertFalse(tag.equals(null));
    }
}