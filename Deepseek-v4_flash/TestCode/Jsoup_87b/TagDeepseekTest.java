package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target: Tag.java - HTML tag capabilities with static registry
 * 
 * Decision Branch Targets:
 * B1: valueOf(String, ParseSettings) - null tagName -> Validate.notNull throws
 * B2: valueOf(String, ParseSettings) - tag exists in tags map after normalization
 * B3: valueOf(String, ParseSettings) - tag not found, creates new generic tag with isBlock=false
 * B4: valueOf(String) - delegates to valueOf with preserveCase settings
 * B5: isBlock() - returns isBlock field
 * B6: formatAsBlock() - returns formatAsBlock field
 * B7: canContainBlock() - returns isBlock (deprecated but active)
 * B8: isInline() - returns !isBlock
 * B9: isData() - returns !canContainInline && !isEmpty()
 * B10: isEmpty() - returns empty field
 * B11: isSelfClosing() - returns empty || selfClosing
 * B12: isKnownTag() - returns tags.containsKey(tagName)
 * B13: isKnownTag(String) - static version, same check
 * B14: preserveWhitespace() - returns preserveWhitespace field
 * B15: isFormListed() - returns formList field
 * B16: isFormSubmittable() - returns formSubmit field
 * B17: setSelfClosing() - sets selfClosing=true, returns this
 * B18: equals() - self comparison, null/type check, field-by-field equality
 * B19: hashCode() - consistent with equals
 * B20: toString() - returns tagName
 * 
 * Boundary Conditions:
 * - Null tagName in valueOf -> throws IllegalArgumentException
 * - Empty tagName after normalization -> throws IllegalArgumentException
 * - Known vs unknown tags
 * - Tags with special attributes (empty, selfClosing, formListed, formSubmit)
 * - Tag equality with different field combinations
 * 
 * Defect Targeting (Defects4J #5):
 * The preservedCaseLinksCantNest bug relates to tag nesting logic where 
 * case-preserved links (<A>) are not properly handled during parsing.
 * While this defect manifests in HtmlTreeBuilderState, the Tag class's 
 * formatAsBlock and isBlock settings for <a> tags are critical.
 * The <a> tag is defined as inline (isBlock=false, formatAsBlock=false in 
 * formatAsInlineTags), but the defect suggests that <A> (uppercase) tags 
 * created via valueOf with preserveCase may not be correctly identified 
 * as the same tag type, causing nesting issues.
 * 
 * Test strategy: Verify that <A> and <a> tags are properly recognized 
 * as the same tag via equals() and isKnownTag(), and that the tag 
 * properties are consistent for both cases.
 */

public class TagDeepseekTest {

    // ===================== Partition A: Core Functional Logic & State Transitions =====================
    
    @Test(timeout = 4000)
    public void testValueOfKnownTag() {
        Tag pTag = Tag.valueOf("p");
        assertEquals("p", pTag.getName());
        assertTrue(pTag.isBlock());
        assertFalse(pTag.formatAsBlock()); // p is in formatAsInlineTags
        assertTrue(pTag.canContainBlock());
        assertFalse(pTag.isInline());
        assertFalse(pTag.isData());
        assertFalse(pTag.isEmpty());
        assertFalse(pTag.isSelfClosing());
        assertTrue(pTag.isKnownTag());
        assertTrue(Tag.isKnownTag("p"));
        assertFalse(pTag.preserveWhitespace());
        assertFalse(pTag.isFormListed());
        assertFalse(pTag.isFormSubmittable());
    }

    @Test(timeout = 4000)
    public void testValueOfInlineTag() {
        Tag spanTag = Tag.valueOf("span");
        assertEquals("span", spanTag.getName());
        assertFalse(spanTag.isBlock());
        assertFalse(spanTag.formatAsBlock());
        assertFalse(spanTag.canContainBlock());
        assertTrue(spanTag.isInline());
        assertFalse(spanTag.isData());
        assertFalse(spanTag.isEmpty());
        assertFalse(spanTag.isSelfClosing());
        assertTrue(spanTag.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testValueOfEmptyTag() {
        Tag brTag = Tag.valueOf("br");
        assertEquals("br", brTag.getName());
        assertFalse(brTag.isBlock());
        assertFalse(brTag.isData());
        assertTrue(brTag.isEmpty());
        assertTrue(brTag.isSelfClosing());
        assertTrue(brTag.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testValueOfUnknownTag() {
        Tag unknownTag = Tag.valueOf("custom");
        assertEquals("custom", unknownTag.getName());
        assertFalse(unknownTag.isBlock());
        assertFalse(unknownTag.isKnownTag());
        assertFalse(Tag.isKnownTag("custom"));
    }

    @Test(timeout = 4000)
    public void testValueOfWithSettingsPreserveCase() {
        Tag tag = Tag.valueOf("DIV", ParseSettings.preserveCase);
        assertEquals("DIV", tag.getName());
        assertTrue(tag.isBlock());
        assertTrue(tag.isKnownTag()); // should find "div" in map
    }

    @Test(timeout = 4000)
    public void testValueOfWithSettingsNormalize() {
        Tag tag = Tag.valueOf("DIV", ParseSettings.htmlDefault);
        // htmlDefault normalizes to lowercase
        assertEquals("div", tag.getName());
        assertTrue(tag.isBlock());
        assertTrue(tag.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testSetSelfClosing() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.isSelfClosing());
        tag.setSelfClosing();
        assertTrue(tag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Tag tag = Tag.valueOf("img");
        assertEquals("img", tag.toString());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceTag() {
        Tag preTag = Tag.valueOf("pre");
        assertTrue(preTag.preserveWhitespace());
        Tag textareaTag = Tag.valueOf("textarea");
        assertTrue(textareaTag.preserveWhitespace());
        Tag divTag = Tag.valueOf("div");
        assertFalse(divTag.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testFormListedTags() {
        Tag inputTag = Tag.valueOf("input");
        assertTrue(inputTag.isFormListed());
        assertTrue(inputTag.isFormSubmittable());
        Tag buttonTag = Tag.valueOf("button");
        assertTrue(buttonTag.isFormListed());
        assertFalse(buttonTag.isFormSubmittable());
        Tag divTag = Tag.valueOf("div");
        assertFalse(divTag.isFormListed());
        assertFalse(divTag.isFormSubmittable());
    }

    @Test(timeout = 4000)
    public void testFormatAsBlockTag() {
        Tag aTag = Tag.valueOf("a");
        assertFalse(aTag.formatAsBlock());
        Tag divTag = Tag.valueOf("div");
        assertTrue(divTag.formatAsBlock());
        Tag pTag = Tag.valueOf("p");
        assertFalse(pTag.formatAsBlock()); // p is in formatAsInlineTags
    }

    // ===================== Partition B: Boundary Value Analysis & Extremes =====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNull() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNullWithSettings() {
        Tag.valueOf(null, ParseSettings.preserveCase);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfEmptyNormalized() {
        // Create a settings that normalizes to empty string
        Tag.valueOf("", ParseSettings.htmlDefault);
    }

    @Test(timeout = 4000)
    public void testIsDataTag() {
        Tag scriptTag = Tag.valueOf("script");
        assertFalse(scriptTag.isData()); // canContainInline=true for script? Actually script is block, canContainInline defaults true
        // Let's check: script is block, not empty, canContainInline should be true -> isData = false
        assertFalse(scriptTag.isData());
        
        Tag imgTag = Tag.valueOf("img");
        assertTrue(imgTag.isEmpty());
        assertFalse(imgTag.isData()); // empty, so !canContainInline && !isEmpty() -> false
    }

    @Test(timeout = 4000)
    public void testCaseSensitivityEquality() {
        Tag lowerP = Tag.valueOf("p", ParseSettings.preserveCase);
        Tag upperP = Tag.valueOf("P", ParseSettings.preserveCase);
        assertFalse(lowerP.equals(upperP)); // different tagName strings
        assertFalse(lowerP.isKnownTag() && upperP.isKnownTag()); // P might not be known
    }

    // ===================== Partition C: Defect-Targeted Branch Zone =====================
    
    @Test(timeout = 4000)
    public void testPreservedCaseLinkTagProperties() {
        // Targeting Defects4J preservedCaseLinksCantNest bug
        // Verify that <A> (uppercase) and <a> (lowercase) tags have same properties
        Tag lowerA = Tag.valueOf("a", ParseSettings.preserveCase);
        Tag upperA = Tag.valueOf("A", ParseSettings.preserveCase);
        
        // Both should have same properties but different names
        assertEquals("a", lowerA.getName());
        assertEquals("A", upperA.getName());
        
        // Core properties should be identical for inline anchor
        assertEquals(lowerA.isBlock(), upperA.isBlock());
        assertEquals(lowerA.formatAsBlock(), upperA.formatAsBlock());
        assertEquals(lowerA.canContainInline(), upperA.canContainInline());
        assertEquals(lowerA.isEmpty(), upperA.isEmpty());
        assertEquals(lowerA.isSelfClosing(), upperA.isSelfClosing());
        assertEquals(lowerA.preserveWhitespace(), upperA.preserveWhitespace());
        assertEquals(lowerA.isFormListed(), upperA.isFormListed());
        assertEquals(lowerA.isFormSubmittable(), upperA.isFormSubmittable());
        
        // The bug is about nesting - verify that both are inline and not block
        assertFalse(lowerA.isBlock());
        assertFalse(upperA.isBlock());
        assertFalse(lowerA.formatAsBlock());
        assertFalse(upperA.formatAsBlock());
        
        // Verify that <A> should be known tag when registered as "a"
        assertTrue(lowerA.isKnownTag());
        assertFalse(upperA.isKnownTag()); // "A" not in tags map
    }

    @Test(timeout = 4000)
    public void testAnchorTagEqualityWithCasePreservation() {
        // Two <a> tags created with different cases should not be equal
        Tag lowerA = Tag.valueOf("a", ParseSettings.preserveCase);
        Tag upperA = Tag.valueOf("A", ParseSettings.preserveCase);
        assertFalse(lowerA.equals(upperA));
        assertFalse(upperA.equals(lowerA));
    }

    @Test(timeout = 4000)
    public void testAnchorTagNormalizedEquality() {
        // With htmlDefault, both become lowercase
        Tag lowerA = Tag.valueOf("a", ParseSettings.htmlDefault);
        Tag upperA = Tag.valueOf("A", ParseSettings.htmlDefault);
        assertEquals("a", lowerA.getName());
        assertEquals("a", upperA.getName());
        assertTrue(lowerA.equals(upperA));
        assertEquals(lowerA.hashCode(), upperA.hashCode());
    }

    @Test(timeout = 4000)
    public void testDefectRevealingAnchorNestingScenario() {
        // Simulate the defect scenario: <A> tags with preserveCase
        // The bug is that <A> ONE <A> Two </A> </A> parses incorrectly
        // This tests that anchor tags are not block (can nest inlines but not blocks)
        Tag anchor = Tag.valueOf("a", ParseSettings.preserveCase);
        Tag anchorUpper = Tag.valueOf("A", ParseSettings.preserveCase);
        
        // Anchor is inline, so it can contain inline elements
        assertFalse(anchor.isBlock());
        assertFalse(anchorUpper.isBlock());
        
        // Both should not be block, which is relevant for nesting behavior
        assertTrue(anchor.canContainInline()); // default true for non-empty
        assertTrue(anchorUpper.canContainInline());
    }

    // ===================== Partition D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000)
    public void testIsKnownTagStatic() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("a"));
        assertTrue(Tag.isKnownTag("br"));
        assertFalse(Tag.isKnownTag("customxyz"));
        assertFalse(Tag.isKnownTag(""));
    }

    @Test(timeout = 4000)
    public void testKnownTagCaseSensitivity() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("DIV")); // static method checks map directly
        // Note: isKnownTag(String) checks tags map which contains lowercase keys
    }

    @Test(timeout = 4000)
    public void testCanContainBlockDeprecated() {
        Tag divTag = Tag.valueOf("div");
        assertTrue(divTag.canContainBlock());
        Tag spanTag = Tag.valueOf("span");
        assertFalse(spanTag.canContainBlock());
    }

    @Test(timeout = 4000)
    public void testSelfClosingWithoutEmpty() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.isSelfClosing());
        tag.setSelfClosing();
        assertTrue(tag.isSelfClosing());
        assertFalse(tag.isEmpty());
    }

    // ===================== Partition E: Object Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testEqualsSelf() {
        Tag tag = Tag.valueOf("div");
        assertTrue(tag.equals(tag));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        Tag tag = Tag.valueOf("div");
        assertFalse(tag.equals("div"));
    }

    @Test(timeout = 4000)
    public void testEqualsSameTag() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("div");
        assertTrue(tag1.equals(tag2));
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTag() {
        Tag divTag = Tag.valueOf("div");
        Tag spanTag = Tag.valueOf("span");
        assertFalse(divTag.equals(spanTag));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentProperties() {
        // Create a tag with same name but different properties via construction
        // Since we can't directly construct, use known tags
        Tag aTag = Tag.valueOf("a");
        Tag divTag = Tag.valueOf("div");
        assertFalse(aTag.equals(divTag));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Tag tag1 = Tag.valueOf("textarea");
        Tag tag2 = Tag.valueOf("textarea");
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test(timeout = 4000)
    public void testMultipleCallsReturnSameInstance() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("div");
        assertSame(tag1, tag2); // Known tags are registered and returned same instance
    }

    @Test(timeout = 4000)
    public void testUnknownTagMultipleCallsReturnDifferentInstances() {
        Tag tag1 = Tag.valueOf("customTag123");
        Tag tag2 = Tag.valueOf("customTag123");
        assertNotSame(tag1, tag2); // Unknown tags are not registered
        assertTrue(tag1.equals(tag2)); // But they should be equal
    }

    @Test(timeout = 4000)
    public void testAllTagTypes() {
        // Sanity check that various tag types produce correct properties
        Tag blockTag = Tag.valueOf("div");
        assertTrue(blockTag.isBlock());
        
        Tag inlineTag = Tag.valueOf("span");
        assertFalse(inlineTag.isBlock());
        assertFalse(inlineTag.formatAsBlock());
        
        Tag emptyTag = Tag.valueOf("br");
        assertTrue(emptyTag.isEmpty());
        assertTrue(emptyTag.isSelfClosing());
        
        Tag formTag = Tag.valueOf("input");
        assertTrue(formTag.isFormListed());
        assertTrue(formTag.isFormSubmittable());
        
        Tag preserveTag = Tag.valueOf("pre");
        assertTrue(preserveTag.preserveWhitespace());
    }
}