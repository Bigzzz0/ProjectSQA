/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.parser.Tag
 * Test Class:   org.jsoup.parser.TagGeminiTest
 *
 * Decision / Condition Coverage Targets:
 * - valueOf(String, ParseSettings):
 *   * Branch 1: tagName == null -> Validate.notNull fails (IllegalArgumentException)
 *   * Branch 2: tags.get(tagName) != null (Known tag lookup, case preserved or exact match)
 *   * Branch 3: tags.get(tagName) == null -> settings.normalizeTag(tagName) -> normalize trims / cases
 *     - Sub-branch 3a: normalized name is empty -> Validate.notEmpty fails (IllegalArgumentException)
 *     - Sub-branch 3b: tags.get(normalized) != null -> returns cached known Tag
 *     - Sub-branch 3c: tags.get(normalized) == null -> creates new generic Tag (isBlock = false)
 * - valueOf(String): delegates to valueOf(tagName, ParseSettings.preserveCase)
 * - isBlock(), isInline(), canContainBlock():
 *   * isBlock == true vs false; isInline == !isBlock; canContainBlock == isBlock
 * - formatAsBlock(): formatAsBlock == true (e.g., div, table) vs false (e.g., p, h1, span)
 * - isData(): !canContainInline && !isEmpty()
 *   * canContainInline true -> false (div, span)
 *   * canContainInline false && empty true -> false (img, br, input)
 * - isEmpty(): empty == true (img, br, input, hr) vs false (div, p, custom)
 * - isSelfClosing(): empty || selfClosing
 *   * empty == true (img) -> true
 *   * empty == false, selfClosing == false (div, custom) -> false
 *   * empty == false, selfClosing == true (custom after setSelfClosing) -> true
 * - isKnownTag() / isKnownTag(String): known tag in static map vs unknown tag
 * - preserveWhitespace(): pre, plaintext, title, textarea vs other tags
 * - isFormListed(): button, fieldset, input, keygen, object, output, select, textarea vs other tags
 * - isFormSubmittable(): input, keygen, object, select, textarea vs button/fieldset/output vs other tags
 * - equals(Object) & hashCode():
 *   * Identity equality (this == o)
 *   * Type check (!(o instanceof Tag))
 *   * Field differentiations: tagName, canContainInline, empty, formatAsBlock, isBlock,
 *     preserveWhitespace, selfClosing, formList, formSubmit
 * - toString(): returns exact tagName
 *
 * Known Defect Target:
 * - Defect: org.jsoup.parser.HtmlParserTest::preservedCaseLinksCantNest
 *   Failure: When parsing uppercase <A> with ParseSettings.preserveCase, the parser failed to enforce
 *            HTML rules prohibiting nesting of <a> active formatting elements because <A> was either
 *            unrecognized or improperly registered, yielding unclosed nested tags.
 * ====================================================================================================
 */
package org.jsoup.parser;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.*;

public class TagGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardBlockTagsProperties() {
        Tag div = Tag.valueOf("div");
        assertEquals("div", div.getName());
        assertEquals("div", div.toString());
        assertTrue(div.isBlock());
        assertFalse(div.isInline());
        assertTrue(div.canContainBlock());
        assertTrue(div.formatAsBlock());
        assertFalse(div.isEmpty());
        assertFalse(div.isSelfClosing());
        assertFalse(div.isData());
        assertTrue(div.isKnownTag());
        assertFalse(div.preserveWhitespace());
        assertFalse(div.isFormListed());
        assertFalse(div.isFormSubmittable());
    }

    @Test(timeout = 4000)
    public void testBlockTagFormattedAsInline() {
        // 'p', 'h1', 'title', etc. are block tags that format as inline
        Tag p = Tag.valueOf("p");
        assertEquals("p", p.getName());
        assertTrue(p.isBlock());
        assertFalse(p.isInline());
        assertFalse(p.formatAsBlock());
        assertFalse(p.isEmpty());
        assertFalse(p.isSelfClosing());
        assertTrue(p.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testStandardInlineTagsProperties() {
        Tag span = Tag.valueOf("span");
        assertEquals("span", span.getName());
        assertFalse(span.isBlock());
        assertTrue(span.isInline());
        assertFalse(span.canContainBlock());
        assertFalse(span.formatAsBlock());
        assertFalse(span.isEmpty());
        assertFalse(span.isSelfClosing());
        assertFalse(span.isData());
        assertTrue(span.isKnownTag());
    }

    @Test(timeout = 4000)
    public void testEmptyTagsProperties() {
        Tag img = Tag.valueOf("img");
        assertEquals("img", img.getName());
        assertFalse(img.isBlock());
        assertTrue(img.isInline());
        assertFalse(img.formatAsBlock());
        assertTrue(img.isEmpty());
        assertTrue(img.isSelfClosing());
        assertFalse(img.isData());
        assertTrue(img.isKnownTag());

        Tag hr = Tag.valueOf("hr");
        assertTrue(hr.isBlock());
        assertTrue(hr.isEmpty());
        assertTrue(hr.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceTags() {
        Tag pre = Tag.valueOf("pre");
        assertTrue(pre.preserveWhitespace());
        assertTrue(pre.isBlock());
        assertFalse(pre.formatAsBlock());

        Tag textarea = Tag.valueOf("textarea");
        assertTrue(textarea.preserveWhitespace());
        assertFalse(textarea.isBlock());

        Tag plaintext = Tag.valueOf("plaintext");
        assertTrue(plaintext.preserveWhitespace());

        Tag title = Tag.valueOf("title");
        assertTrue(title.preserveWhitespace());

        Tag div = Tag.valueOf("div");
        assertFalse(div.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testFormAssociations() {
        // Form listed AND form submittable
        Tag input = Tag.valueOf("input");
        assertTrue(input.isFormListed());
        assertTrue(input.isFormSubmittable());
        assertTrue(input.isEmpty());

        Tag select = Tag.valueOf("select");
        assertTrue(select.isFormListed());
        assertTrue(select.isFormSubmittable());
        assertFalse(select.isEmpty());

        // Form listed but NOT form submittable
        Tag button = Tag.valueOf("button");
        assertTrue(button.isFormListed());
        assertFalse(button.isFormSubmittable());

        Tag fieldset = Tag.valueOf("fieldset");
        assertTrue(fieldset.isFormListed());
        assertFalse(fieldset.isFormSubmittable());

        Tag output = Tag.valueOf("output");
        assertTrue(output.isFormListed());
        assertFalse(output.isFormSubmittable());

        // Neither
        Tag a = Tag.valueOf("a");
        assertFalse(a.isFormListed());
        assertFalse(a.isFormSubmittable());
    }

    @Test(timeout = 4000)
    public void testUnknownTagDefaultStateAndTransition() {
        Tag custom = Tag.valueOf("custom-tag");
        assertEquals("custom-tag", custom.getName());
        assertFalse(custom.isBlock());
        assertTrue(custom.isInline());
        assertFalse(custom.canContainBlock());
        assertTrue(custom.formatAsBlock());
        assertFalse(custom.isEmpty());
        assertFalse(custom.isSelfClosing());
        assertFalse(custom.isKnownTag());
        assertFalse(custom.preserveWhitespace());
        assertFalse(custom.isFormListed());
        assertFalse(custom.isFormSubmittable());

        // State transition via package-private setSelfClosing()
        Tag returned = custom.setSelfClosing();
        assertSame(custom, returned);
        assertTrue(custom.isSelfClosing());
        assertFalse(custom.isEmpty()); // selfClosing does not force empty
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Settings Normalization
    // =========================================================================

    @Test(timeout = 4000)
    public void testValueOfCaseInsensitiveHtmlDefault() {
        // ParseSettings.htmlDefault converts to lowercase
        Tag tagUpper = Tag.valueOf("DIV", ParseSettings.htmlDefault);
        Tag tagLower = Tag.valueOf("div", ParseSettings.htmlDefault);
        assertSame(tagLower, tagUpper);
        assertTrue(tagUpper.isKnownTag());
        assertTrue(tagUpper.isBlock());
    }

    @Test(timeout = 4000)
    public void testValueOfPreserveCaseKnownTags() {
        // Lowercase known tag matches directly
        Tag lower = Tag.valueOf("p", ParseSettings.preserveCase);
        assertTrue(lower.isKnownTag());
        assertTrue(lower.isBlock());

        // Static factory single argument preserves case
        Tag p = Tag.valueOf("p");
        assertSame(lower, p);
    }

    @Test(timeout = 4000)
    public void testIsKnownTagEvaluation() {
        assertTrue(Tag.isKnownTag("div"));
        assertTrue(Tag.isKnownTag("span"));
        assertTrue(Tag.isKnownTag("html"));
        assertTrue(Tag.isKnownTag("template"));
        assertTrue(Tag.isKnownTag("svg"));
        assertTrue(Tag.isKnownTag("math"));

        assertFalse(Tag.isKnownTag("unknown-tag-xyz"));
        assertFalse(Tag.isKnownTag(""));
    }

    @Test(timeout = 4000)
    public void testTrimmedTagNameNormalization() {
        Tag tag = Tag.valueOf("   span   ", ParseSettings.htmlDefault);
        assertEquals("span", tag.getName());
        assertTrue(tag.isKnownTag());
        assertSame(Tag.valueOf("span"), tag);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Preserved Case Tag Nesting)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPreservedCaseLinksCantNest() {
        // Targets Defects4J known defect: HtmlParserTest::preservedCaseLinksCantNest
        // When ParseSettings.preserveCase is active, parsing "<A> ONE <A> Two </A> </A>"
        // must properly enforce HTML adoption/formatting element rules preventing <a> tags from nesting.
        String html = "<A> ONE <A> Two </A> </A>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<A> ONE </A> <A> Two </A>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNullThrowsException() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNullWithSettingsThrowsException() {
        Tag.valueOf(null, ParseSettings.preserveCase);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfEmptyThrowsException() {
        Tag.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfWhitespaceOnlyThrowsException() {
        Tag.valueOf("   ");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfWhitespaceOnlyWithHtmlDefaultThrowsException() {
        Tag.valueOf("   ", ParseSettings.htmlDefault);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode, toString)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPredefinedTagsIdentityAndEquality() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertSame(p1, p2);
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test(timeout = 4000)
    public void testUnknownTagsEqualityContract() {
        Tag t1 = Tag.valueOf("custom");
        Tag t2 = Tag.valueOf("custom");

        assertNotSame(t1, t2); // Unknown tags return fresh instances
        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
        assertEquals(t1.hashCode(), t2.hashCode());

        assertEquals("custom", t1.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsExhaustiveBranches() {
        Tag t = Tag.valueOf("foo");

        // Identity branch
        assertTrue(t.equals(t));

        // Non-Tag / null branches
        assertFalse(t.equals(null));
        assertFalse(t.equals("foo"));
        assertFalse(t.equals(new Object()));

        // Different tagName
        Tag differentName = Tag.valueOf("bar");
        assertFalse(t.equals(differentName));

        // Differentiated by selfClosing
        Tag tNormal = Tag.valueOf("elem");
        Tag tSelfClosing = Tag.valueOf("elem").setSelfClosing();
        assertFalse(tNormal.equals(tSelfClosing));
        assertFalse(tSelfClosing.equals(tNormal));

        // Different predefined tags testing boolean field differences in equals
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");
        assertFalse(div.equals(span));

        Tag img = Tag.valueOf("img"); // empty tag
        assertFalse(span.equals(img));

        Tag pre = Tag.valueOf("pre"); // preserveWhitespace tag
        assertFalse(div.equals(pre));

        Tag button = Tag.valueOf("button"); // formList tag
        assertFalse(span.equals(button));

        Tag input = Tag.valueOf("input"); // formSubmit tag
        Tag textarea = Tag.valueOf("textarea");
        assertFalse(button.equals(input));
        assertFalse(input.equals(textarea));
    }

    @Test(timeout = 4000)
    public void testHashCodeStabilityAndDifferentiation() {
        Tag tag1 = Tag.valueOf("article");
        Tag tag2 = Tag.valueOf("article");
        assertEquals(tag1.hashCode(), tag2.hashCode());

        Tag section = Tag.valueOf("section");
        assertNotEquals(tag1.hashCode(), section.hashCode());

        Tag custom1 = Tag.valueOf("custom");
        int initialHashCode = custom1.hashCode();
        custom1.setSelfClosing();
        assertNotEquals(initialHashCode, custom1.hashCode());
    }
}