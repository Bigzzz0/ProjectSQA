package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * TARGET CLASS: org.jsoup.parser.Tag
 *
 * 1. DEFECT-TARGETED ZONE (Defects4J Table Row Ancestry Regression):
 *    - TR Tag Ancestors: In the defective model, TR only registers TABLE as its valid ancestor.
 *      Consequently, TBODY, THEAD, and TFOOT fail the `isValidParent(Tag child)` condition,
 *      causing parsers/DOM builders to incorrectly split or wrap rows inside synthetic tables.
 *    - Target Assertions: `tbody.isValidParent(tr)`, `thead.isValidParent(tr)`, `tfoot.isValidParent(tr)`.
 *
 * 2. BRANCH & CONDITION COVERAGE MATRIX:
 *    - `valueOf(String)`:
 *        * null tagName -> Validate.notNull -> IllegalArgumentException
 *        * empty / whitespace tagName -> Validate.notEmpty -> IllegalArgumentException
 *        * pre-defined tag cache hit (case-insensitive & trimmed)
 *        * undefined/custom tag cache miss -> dynamic creation (isBlock=false, canContainBlock=true)
 *    - `canContain(Tag child)`:
 *        * child == null -> Validate.notNull -> IllegalArgumentException
 *        * child.isBlock && !this.canContainBlock -> false (e.g., P containing DIV)
 *        * !child.isBlock && !this.canContainInline -> false (e.g., SCRIPT containing SPAN)
 *        * this.optionalClosing && this.equals(child) -> false (e.g., LI in LI, A in A, TR in TR)
 *        * this.empty || this.isData() -> false (e.g., IMG, HR, SCRIPT)
 *        * HEAD specific whitelisted children (base, script, noscript, link, meta, title, style, object) -> true
 *        * HEAD non-whitelisted children (p, div, b) -> false
 *        * DT containing DD -> false; DD containing DT -> false
 *        * default containment -> true
 *    - `isValidParent(Tag child)`:
 *        * child.ancestors.isEmpty() -> true (e.g., HTML tag has no ancestors)
 *        * child.ancestors contains this -> true (e.g., BODY for P, TABLE for TR)
 *        * child.ancestors does not contain this -> false
 *    - `getImplicitParent()`:
 *        * ancestors non-empty -> ancestors.get(0)
 *        * ancestors empty -> null (for HTML)
 *    - `equals(Object)` & `hashCode()`:
 *        * identity (this == o)
 *        * null and class mismatch
 *        * field-by-field permutations: canContainBlock, canContainInline, empty, isBlock,
 *          optionalClosing, tagName equality
 * ====================================================================================================
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
        assertTrue("DIV should be block", div.isBlock());
        assertFalse("DIV should not be inline", div.isInline());
        assertTrue("DIV can contain blocks", div.canContainBlock());
        assertFalse("DIV is not empty", div.isEmpty());
        assertFalse("DIV is not data", div.isData());
        assertFalse("DIV does not preserve whitespace", div.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testStandardInlineTagProperties() {
        Tag span = Tag.valueOf("span");
        assertEquals("span", span.getName());
        assertFalse("SPAN is not block", span.isBlock());
        assertTrue("SPAN is inline", span.isInline());
        assertFalse("SPAN cannot contain blocks", span.canContainBlock());
        assertFalse("SPAN is not empty", span.isEmpty());
        assertFalse("SPAN is not data", span.isData());
        assertFalse("SPAN does not preserve whitespace", span.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testEmptyTagProperties() {
        Tag img = Tag.valueOf("img");
        assertTrue("IMG is empty", img.isEmpty());
        assertTrue("IMG is inline", img.isInline());
        assertFalse("IMG cannot contain blocks", img.canContainBlock());
        assertFalse("IMG is not data-only", img.isData());

        Tag hr = Tag.valueOf("hr");
        assertTrue("HR is empty", hr.isEmpty());
        assertTrue("HR is block", hr.isBlock());
        assertFalse("HR cannot contain blocks", hr.canContainBlock());
    }

    @Test(timeout = 4000)
    public void testDataAndPreserveWhitespaceTagProperties() {
        Tag script = Tag.valueOf("script");
        assertTrue("SCRIPT is data-only", script.isData());
        assertTrue("SCRIPT preserves whitespace", script.preserveWhitespace());
        assertTrue("SCRIPT is block", script.isBlock());
        assertFalse("SCRIPT cannot contain blocks", script.canContainBlock());

        Tag textarea = Tag.valueOf("textarea");
        assertTrue("TEXTAREA is data-only", textarea.isData());
        assertTrue("TEXTAREA preserves whitespace", textarea.preserveWhitespace());
        assertTrue("TEXTAREA is inline", textarea.isInline());

        Tag pre = Tag.valueOf("pre");
        assertFalse("PRE is not data-only", pre.isData());
        assertTrue("PRE preserves whitespace", pre.preserveWhitespace());
        assertTrue("PRE is block", pre.isBlock());
        assertFalse("PRE cannot contain blocks", pre.canContainBlock());
    }

    @Test(timeout = 4000)
    public void testCanContainBlockAndInlineRules() {
        Tag div = Tag.valueOf("div");
        Tag p = Tag.valueOf("p");
        Tag span = Tag.valueOf("span");

        assertTrue("DIV can contain block P", div.canContain(p));
        assertTrue("DIV can contain inline SPAN", div.canContain(span));
        assertFalse("P cannot contain block DIV", p.canContain(div));
        assertTrue("P can contain inline SPAN", p.canContain(span));
        assertFalse("SPAN cannot contain block DIV", span.canContain(div));
        assertTrue("SPAN can contain inline SPAN", span.canContain(span));
    }

    @Test(timeout = 4000)
    public void testCanContainOptionalClosingElements() {
        Tag li = Tag.valueOf("li");
        assertFalse("LI cannot contain another LI due to optional closing", li.canContain(li));

        Tag a = Tag.valueOf("a");
        assertFalse("A cannot contain another A due to optional closing", a.canContain(a));

        Tag form = Tag.valueOf("form");
        assertFalse("FORM cannot contain another FORM", form.canContain(form));

        Tag tr = Tag.valueOf("tr");
        assertFalse("TR cannot contain another TR", tr.canContain(tr));

        Tag td = Tag.valueOf("td");
        assertFalse("TD cannot contain another TD", td.canContain(td));

        Tag th = Tag.valueOf("th");
        assertFalse("TH cannot contain another TH", th.canContain(th));
    }

    @Test(timeout = 4000)
    public void testHeadElementContainmentWhitelist() {
        Tag head = Tag.valueOf("head");
        String[] allowedInHead = new String[]{"base", "script", "noscript", "link", "meta", "title", "style", "object"};
        for (String tagName : allowedInHead) {
            Tag child = Tag.valueOf(tagName);
            assertTrue("HEAD must contain " + tagName, head.canContain(child));
        }

        assertFalse("HEAD cannot contain P", head.canContain(Tag.valueOf("p")));
        assertFalse("HEAD cannot contain DIV", head.canContain(Tag.valueOf("div")));
        assertFalse("HEAD cannot contain SPAN", head.canContain(Tag.valueOf("span")));
    }

    @Test(timeout = 4000)
    public void testDtAndDdMutualExclusion() {
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");
        Tag span = Tag.valueOf("span");

        assertFalse("DT cannot contain DD", dt.canContain(dd));
        assertFalse("DD cannot contain DT", dd.canContain(dt));
        assertTrue("DT can contain SPAN", dt.canContain(span));
        assertTrue("DD can contain SPAN", dd.canContain(span));
    }

    @Test(timeout = 4000)
    public void testImplicitParentResolution() {
        Tag html = Tag.valueOf("html");
        assertNull("HTML has no implicit parent", html.getImplicitParent());

        Tag body = Tag.valueOf("body");
        assertEquals("HTML is implicit parent of BODY", html, body.getImplicitParent());

        Tag p = Tag.valueOf("p");
        assertEquals("BODY is implicit parent of P", body, p.getImplicitParent());

        Tag custom = Tag.valueOf("custom-tag");
        assertEquals("BODY is default ancestor of custom tags", body, custom.getImplicitParent());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTagValueOfNormalization() {
        Tag p1 = Tag.valueOf("P");
        Tag p2 = Tag.valueOf("p");
        Tag p3 = Tag.valueOf("  p  ");

        assertSame("Known tags must resolve to identical singleton instances", p1, p2);
        assertSame("Whitespace trimmed tags must resolve to identical singleton instances", p1, p3);
        assertEquals("Tag name must be normalized to lowercase", "p", p1.getName());
    }

    @Test(timeout = 4000)
    public void testCustomUnknownTagSemantics() {
        Tag custom1 = Tag.valueOf("custom_tag");
        Tag custom2 = Tag.valueOf("CUSTOM_TAG");

        assertNotSame("Unknown tags are not registered in the singleton map", custom1, custom2);
        assertEquals("Unknown tags with same name must be equal", custom1, custom2);
        assertEquals("custom_tag", custom1.getName());
        assertFalse("Unknown tags are not block", custom1.isBlock());
        assertTrue("Unknown tags are inline", custom1.isInline());
        assertTrue("Unknown tags can contain blocks", custom1.canContainBlock());
        assertFalse("Unknown tags are not empty", custom1.isEmpty());
        assertFalse("Unknown tags are not data", custom1.isData());
    }

    @Test(timeout = 4000)
    public void testHtmlTagIsValidParentForAny() {
        Tag html = Tag.valueOf("html");
        Tag p = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        Tag custom = Tag.valueOf("unknown");

        assertTrue("Any parent is technically valid for child with empty ancestors (HTML)", p.isValidParent(html));
        assertTrue("DIV is valid parent for HTML (root boundary)", div.isValidParent(html));
        assertTrue("Custom tag is valid parent for HTML", custom.isValidParent(html));
    }

    @Test(timeout = 4000)
    public void testMultiAncestorResolution() {
        Tag script = Tag.valueOf("script");
        Tag head = Tag.valueOf("head");
        Tag body = Tag.valueOf("body");
        Tag div = Tag.valueOf("div");

        assertTrue("HEAD is a valid parent for SCRIPT", head.isValidParent(script));
        assertTrue("BODY is a valid parent for SCRIPT", body.isValidParent(script));
        assertFalse("DIV is not a valid parent for SCRIPT", div.isValidParent(script));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (TR Ancestry in Tables)
    // =========================================================================

    /**
     * TARGET DEFECT: TR must recognize TBODY, THEAD, and TFOOT as valid parents.
     * Ground truth from Defects4J (ElementTest::testAppendRowToTable / ParserTest::handlesNestedImplicitTable):
     * When appending rows to tables or implicit table structures, TR was previously only registered
     * with TABLE as its ancestor. Thus tbody.isValidParent(tr) evaluated to false, leading to broken
     * table DOM parsing.
     */
    @Test(timeout = 4000)
    public void testTableRowAncestrySupportsTbodyTheadTfoot() {
        Tag tr = Tag.valueOf("tr");
        Tag table = Tag.valueOf("table");
        Tag tbody = Tag.valueOf("tbody");
        Tag thead = Tag.valueOf("thead");
        Tag tfoot = Tag.valueOf("tfoot");

        assertTrue("TABLE must be a valid parent for TR", table.isValidParent(tr));
        assertTrue("TBODY must be a valid parent for TR to prevent nested table defect", tbody.isValidParent(tr));
        assertTrue("THEAD must be a valid parent for TR", thead.isValidParent(tr));
        assertTrue("TFOOT must be a valid parent for TR", tfoot.isValidParent(tr));
    }

    @Test(timeout = 4000)
    public void testTableCellAncestry() {
        Tag tr = Tag.valueOf("tr");
        Tag td = Tag.valueOf("td");
        Tag th = Tag.valueOf("th");
        Tag table = Tag.valueOf("table");

        assertTrue("TR is valid parent for TD", tr.isValidParent(td));
        assertTrue("TR is valid parent for TH", tr.isValidParent(th));
        assertFalse("TABLE is not a direct valid parent for TD", table.isValidParent(td));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testValueOfNullThrowsException() {
        Tag.valueOf(null);
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
    public void testCanContainNullThrowsException() {
        Tag.valueOf("div").canContain(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals & hashCode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Tag customA = Tag.valueOf("my-element");
        Tag customB = Tag.valueOf("MY-ELEMENT");
        Tag customC = Tag.valueOf("my-element");

        // Reflexive
        assertTrue(customA.equals(customA));

        // Symmetric
        assertTrue(customA.equals(customB));
        assertTrue(customB.equals(customA));

        // Transitive
        assertTrue(customB.equals(customC));
        assertTrue(customA.equals(customC));

        // Non-nullity
        assertFalse(customA.equals(null));
        assertFalse(customA.equals("my-element"));

        // HashCode consistency
        assertEquals(customA.hashCode(), customB.hashCode());
        assertEquals(customA.hashCode(), customC.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentiatingAllBranches() {
        Tag p = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");
        Tag img = Tag.valueOf("img");
        Tag li = Tag.valueOf("li");

        // Different canContainBlock: DIV can contain block, P cannot
        assertFalse("P and DIV differ in canContainBlock", p.equals(div));

        // Different isBlock: DIV is block, SPAN is inline
        assertFalse("DIV and SPAN differ in isBlock", div.equals(span));

        // Different empty flag: SPAN is not empty, IMG is empty
        assertFalse("SPAN and IMG differ in empty", span.equals(img));

        // Different optionalClosing: LI has optionalClosing, DIV does not
        assertFalse("LI and DIV differ in optionalClosing", li.equals(div));

        // Different tag names
        Tag a1 = Tag.valueOf("b");
        Tag a2 = Tag.valueOf("i");
        assertFalse("Different tag names with identical boolean traits are not equal", a1.equals(a2));
    }
}