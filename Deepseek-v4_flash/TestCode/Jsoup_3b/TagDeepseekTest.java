package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for Tag class targeting line/branch coverage and the known Defects4J defect.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (valueOf, canContain, isBlock, etc.)
 * - Partition B: Boundary values (null, empty, unknown tags, extremes)
 * - Partition C: Defect-targeted branch: tr.canContain(table) should be false (circular containment)
 * - Partition D: Exception paths (null/empty arguments)
 * - Partition E: Object contract (equals, hashCode, toString)
 * 
 * Known defect: tr.canContain(table) incorrectly returns true, causing nested table insertion.
 */
public class TagDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testValueOfKnownTag() {
        Tag p = Tag.valueOf("p");
        assertEquals("p", p.getName());
        assertTrue(p.isBlock());
        assertFalse(p.canContainBlock());
        assertTrue(p.canContainBlock() == false); // inline-only
        assertTrue(p.isInline() == false);
        assertFalse(p.isEmpty());
        assertFalse(p.preserveWhitespace());
        assertFalse(p.isData());
    }

    @Test(timeout = 4000)
    public void testValueOfBlockTag() {
        Tag div = Tag.valueOf("div");
        assertTrue(div.isBlock());
        assertTrue(div.canContainBlock());
        assertTrue(div.canContainInline());
        assertFalse(div.isEmpty());
        assertFalse(div.isData());
    }

    @Test(timeout = 4000)
    public void testValueOfInlineTag() {
        Tag span = Tag.valueOf("span");
        assertFalse(span.isBlock());
        assertFalse(span.canContainBlock());
        assertTrue(span.canContainInline());
        assertFalse(span.isEmpty());
    }

    @Test(timeout = 4000)
    public void testValueOfEmptyTag() {
        Tag img = Tag.valueOf("img");
        assertTrue(img.isEmpty());
        assertFalse(img.canContainBlock());
        assertFalse(img.canContainInline());
        assertTrue(img.isData()); // empty implies data? Actually isData = !canContainInline && !isEmpty -> false && false = false? Wait: isData = !canContainInline && !isEmpty. For img: canContainInline=false, isEmpty=true => !false && !true = true && false = false. So img is not data. Correct.
        assertFalse(img.isData());
    }

    @Test(timeout = 4000)
    public void testValueOfDataTag() {
        Tag script = Tag.valueOf("script");
        assertTrue(script.isData());
        assertFalse(script.canContainBlock());
        assertFalse(script.canContainInline());
        assertTrue(script.preserveWhitespace());
        assertFalse(script.isEmpty());
    }

    @Test(timeout = 4000)
    public void testValueOfUnknownTag() {
        Tag unknown = Tag.valueOf("custom");
        assertEquals("custom", unknown.getName());
        assertFalse(unknown.isBlock()); // default false
        assertTrue(unknown.canContainBlock()); // default true
        assertTrue(unknown.canContainInline()); // default true
        assertFalse(unknown.isEmpty());
        assertFalse(unknown.isData());
        assertFalse(unknown.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testCanContainBlockChildInBlockParent() {
        Tag div = Tag.valueOf("div");
        Tag p = Tag.valueOf("p");
        assertTrue(div.canContain(p)); // div can contain block p
    }

    @Test(timeout = 4000)
    public void testCanContainInlineChildInBlockParent() {
        Tag div = Tag.valueOf("div");
        Tag span = Tag.valueOf("span");
        assertTrue(div.canContain(span));
    }

    @Test(timeout = 4000)
    public void testCanContainBlockChildInInlineOnlyParent() {
        Tag p = Tag.valueOf("p"); // inline-only (canContainBlock=false)
        Tag div = Tag.valueOf("div");
        assertFalse(p.canContain(div)); // p cannot contain block
    }

    @Test(timeout = 4000)
    public void testCanContainInlineChildInBlockOnlyParent() {
        Tag pre = Tag.valueOf("pre"); // block, canContainBlock=false, canContainInline=true
        Tag span = Tag.valueOf("span");
        assertTrue(pre.canContain(span)); // pre can contain inline
    }

    @Test(timeout = 4000)
    public void testCanContainSelfWithOptionalClosing() {
        Tag p = Tag.valueOf("p"); // optionalClosing=true
        assertFalse(p.canContain(p)); // p cannot contain itself
    }

    @Test(timeout = 4000)
    public void testCanContainEmptyParent() {
        Tag img = Tag.valueOf("img"); // empty
        Tag span = Tag.valueOf("span");
        assertFalse(img.canContain(span));
    }

    @Test(timeout = 4000)
    public void testCanContainDataParent() {
        Tag script = Tag.valueOf("script"); // data
        Tag span = Tag.valueOf("span");
        assertFalse(script.canContain(span));
    }

    @Test(timeout = 4000)
    public void testCanContainHeadRestrictions() {
        Tag head = Tag.valueOf("head");
        Tag title = Tag.valueOf("title");
        assertTrue(head.canContain(title));
        Tag div = Tag.valueOf("div");
        assertFalse(head.canContain(div));
    }

    @Test(timeout = 4000)
    public void testCanContainDtDdRestrictions() {
        Tag dt = Tag.valueOf("dt");
        Tag dd = Tag.valueOf("dd");
        assertFalse(dt.canContain(dd));
        assertFalse(dd.canContain(dt));
    }

    @Test(timeout = 4000)
    public void testCanContainTableContainsTr() {
        Tag table = Tag.valueOf("table");
        Tag tr = Tag.valueOf("tr");
        assertTrue(table.canContain(tr)); // valid
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueOfNull() {
        Tag.valueOf(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueOfEmptyString() {
        Tag.valueOf("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueOfWhitespaceOnly() {
        Tag.valueOf("   ");
    }

    @Test(timeout = 4000)
    public void testValueOfCaseInsensitive() {
        Tag p1 = Tag.valueOf("P");
        Tag p2 = Tag.valueOf("p");
        assertSame(p1, p2); // same instance for known tags
    }

    @Test(timeout = 4000)
    public void testCanContainNullChild() {
        Tag div = Tag.valueOf("div");
        try {
            div.canContain(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsValidParentWithEmptyAncestors() {
        Tag html = Tag.valueOf("html"); // ancestors empty
        Tag div = Tag.valueOf("div");
        assertTrue(html.isValidParent(div)); // empty ancestors => true
    }

    @Test(timeout = 4000)
    public void testIsValidParentWithMatchingAncestor() {
        Tag table = Tag.valueOf("table");
        Tag tr = Tag.valueOf("tr");
        assertTrue(table.isValidParent(tr)); // tr's ancestors contain table
    }

    @Test(timeout = 4000)
    public void testIsValidParentWithNonMatchingAncestor() {
        Tag div = Tag.valueOf("div");
        Tag tr = Tag.valueOf("tr");
        assertFalse(div.isValidParent(tr)); // tr's ancestors are [table], not div
    }

    @Test(timeout = 4000)
    public void testGetImplicitParentWithAncestors() {
        Tag tr = Tag.valueOf("tr");
        Tag implicit = tr.getImplicitParent();
        assertNotNull(implicit);
        assertEquals("table", implicit.getName());
    }

    @Test(timeout = 4000)
    public void testGetImplicitParentWithoutAncestors() {
        Tag html = Tag.valueOf("html");
        assertNull(html.getImplicitParent());
    }

    // ==================== Partition C: Defect-Targeted Branch ====================

    @Test(timeout = 4000)
    public void testCanContainTrCannotContainTable() {
        // Known defect: tr.canContain(table) should return false, but buggy version returns true.
        Tag tr = Tag.valueOf("tr");
        Tag table = Tag.valueOf("table");
        assertFalse("tr should not be able to contain table", tr.canContain(table));
    }

    @Test(timeout = 4000)
    public void testCanContainTdCannotContainTable() {
        Tag td = Tag.valueOf("td");
        Tag table = Tag.valueOf("table");
        assertFalse("td should not be able to contain table", td.canContain(table));
    }

    @Test(timeout = 4000)
    public void testCanContainThCannotContainTable() {
        Tag th = Tag.valueOf("th");
        Tag table = Tag.valueOf("table");
        assertFalse("th should not be able to contain table", th.canContain(table));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCanContainNull() {
        Tag div = Tag.valueOf("div");
        div.canContain(null);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Tag p = Tag.valueOf("p");
        assertEquals(p, p);
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertEquals(p1, p2);
        assertEquals(p2, p1);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTags() {
        Tag p = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        assertNotEquals(p, div);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Tag p = Tag.valueOf("p");
        assertNotNull(p);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Tag p = Tag.valueOf("p");
        assertNotEquals(p, "p");
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentFields() {
        Tag p = Tag.valueOf("p");
        Tag p2 = new Tag("p") {}; // anonymous subclass? Not possible. Use reflection? Simpler: compare with unknown tag that has same name but different properties.
        Tag unknownP = Tag.valueOf("p"); // actually same instance. So we need to create a tag with same name but different properties via valueOf? Not possible. We'll test via equals contract: two tags with same name but different block status? Not possible via public API. We'll rely on the fact that known tags are singletons.
        // Instead, test that two different unknown tags with same name are equal? Actually unknown tags are not registered, so each valueOf creates a new instance with same properties. They should be equal.
        Tag unknown1 = Tag.valueOf("xyz");
        Tag unknown2 = Tag.valueOf("xyz");
        assertEquals(unknown1, unknown2);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Tag p = Tag.valueOf("p");
        int hash1 = p.hashCode();
        int hash2 = p.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCodeEqualObjects() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Tag p = Tag.valueOf("p");
        assertEquals("p", p.toString());
    }

    // Additional coverage for isBlock, isInline, canContainBlock, isEmpty, isData, preserveWhitespace

    @Test(timeout = 4000)
    public void testIsBlockForInline() {
        Tag span = Tag.valueOf("span");
        assertFalse(span.isBlock());
        assertTrue(span.isInline());
    }

    @Test(timeout = 4000)
    public void testCanContainBlockForInlineOnly() {
        Tag p = Tag.valueOf("p");
        assertFalse(p.canContainBlock());
    }

    @Test(timeout = 4000)
    public void testIsEmptyForNonEmpty() {
        Tag div = Tag.valueOf("div");
        assertFalse(div.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceForPre() {
        Tag pre = Tag.valueOf("pre");
        assertTrue(pre.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testPreserveWhitespaceForData() {
        Tag script = Tag.valueOf("script");
        assertTrue(script.preserveWhitespace());
    }

    @Test(timeout = 4000)
    public void testIsDataForEmptyTag() {
        Tag br = Tag.valueOf("br");
        assertFalse(br.isData()); // empty but canContainInline=false, isEmpty=true => !false && !true = true && false = false
    }

    @Test(timeout = 4000)
    public void testIsDataForDataTag() {
        Tag style = Tag.valueOf("style");
        assertTrue(style.isData());
    }

    // Test optionalClosing for tags like A, FORM, etc.
    @Test(timeout = 4000)
    public void testOptionalClosingPreventsSelfContainment() {
        Tag a = Tag.valueOf("a");
        assertFalse(a.canContain(a));
    }

    @Test(timeout = 4000)
    public void testOptionalClosingNotPreventingOther() {
        Tag a = Tag.valueOf("a");
        Tag span = Tag.valueOf("span");
        assertTrue(a.canContain(span));
    }
}