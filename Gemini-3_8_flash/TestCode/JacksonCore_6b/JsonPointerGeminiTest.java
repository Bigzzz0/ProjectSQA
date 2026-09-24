package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.core.JsonPointer
 *
 * 1. Defect-Targeted Zone (core#176 / testIZeroIndex):
 *    - Branch: _parseIndex(String) leading zero check.
 *    - RFC 6901 specifies that array indices MUST NOT have leading zeroes (e.g., "00", "01").
 *    - Ground Truth Defect: JsonPointer.compile("/00") mistakenly parsed index as 0 instead of -1.
 *
 * 2. Equivalence Partitions & Decision Branches:
 *    - compile(String) / valueOf(String):
 *      * input == null -> EMPTY
 *      * input.length() == 0 -> EMPTY
 *      * input.charAt(0) != '/' -> IllegalArgumentException
 *      * Valid expressions starting with '/' -> _parseTail(input)
 *    - _parseTail(String) & _parseQuotedTail(String, int):
 *      * Traversal with no escaped characters (e.g., "/a/b/c")
 *      * Tilde escapes: "~0" -> '~', "~1" -> '/', other "~x" -> "~x"
 *      * Quoted segment at start of tail (i == 2 vs i > 2)
 *      * Consecutive escapes (e.g., "/~0~1", "/~00")
 *      * Empty segments ("//", "/")
 *    - _parseIndex(String):
 *      * len == 0 ("/") -> -1
 *      * len > 10 -> -1 (overflow boundary)
 *      * non-digit characters ("a", "-1", "+1", "1a") -> -1
 *      * len == 10, value <= Integer.MAX_VALUE (2147483647) -> parsed correctly
 *      * len == 10, value > Integer.MAX_VALUE (e.g., 2147483648L, 9999999999L) -> -1
 *      * Single zero ("0") -> 0
 *      * Positive integers ("1", "42") -> 1, 42
 *    - Navigation & Matching API:
 *      * matches(): true if _nextSegment == null, else false
 *      * matchProperty(String): match vs mismatch vs terminal segment
 *      * matchElement(int): match (>=0), mismatch, negative input (-1)
 *      * tail(): returns next segment or null
 *    - Object Lifecycle Contracts:
 *      * equals(): self (reflexive), null, non-JsonPointer, identical path, divergent path
 *      * hashCode(): contract consistency with equals
 *      * toString(): returns exact original representation
 */
public class JsonPointerGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: testIZeroIndex
     * RFC 6901 rules that an array index cannot have leading zeroes (except single "0").
     * Bug: NumberInput.parseInt("00") returned 0 instead of treating it as non-index property (-1).
     */
    @Test(timeout = 4000)
    public void testIZeroIndex() {
        JsonPointer ptr = JsonPointer.compile("/00");
        assertEquals("Property name should match full string segment", "00", ptr.getMatchingProperty());
        assertEquals("Index with leading zero must not be treated as array element index", -1, ptr.getMatchingIndex());
        assertFalse("Leading zero segment must not match array element", ptr.mayMatchElement());
    }

    @Test(timeout = 4000)
    public void testLeadingZeroVariants() {
        JsonPointer p1 = JsonPointer.compile("/01");
        assertEquals(-1, p1.getMatchingIndex());

        JsonPointer p2 = JsonPointer.compile("/007");
        assertEquals(-1, p2.getMatchingIndex());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSimpleNavigation() {
        JsonPointer ptr = JsonPointer.compile("/users/12/name");

        assertFalse(ptr.matches());
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
        assertEquals("users", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());

        JsonPointer mid = ptr.matchProperty("users");
        assertNotNull(mid);
        assertEquals("12", mid.getMatchingProperty());
        assertEquals(12, mid.getMatchingIndex());
        assertTrue(mid.mayMatchProperty());
        assertTrue(mid.mayMatchElement());

        JsonPointer leaf = mid.matchElement(12);
        assertNotNull(leaf);
        assertEquals("name", leaf.getMatchingProperty());
        assertEquals(-1, leaf.getMatchingIndex());
        assertFalse(leaf.matches());

        JsonPointer end = leaf.matchProperty("name");
        assertNotNull(end);
        assertTrue(end.matches());
        assertEquals("", end.getMatchingProperty());
        assertEquals(-1, end.getMatchingIndex());
        assertNull(end.tail());
    }

    @Test(timeout = 4000)
    public void testTailMethod() {
        JsonPointer p = JsonPointer.compile("/a/b/c");
        JsonPointer t1 = p.tail();
        assertEquals("/b/c", t1.toString());
        assertEquals("b", t1.getMatchingProperty());

        JsonPointer t2 = t1.tail();
        assertEquals("/c", t2.toString());
        assertEquals("c", t2.getMatchingProperty());

        JsonPointer t3 = t2.tail();
        assertEquals("", t3.toString());
        assertTrue(t3.matches());
        assertNull(t3.tail());
    }

    @Test(timeout = 4000)
    public void testValueOfAlias() {
        JsonPointer p1 = JsonPointer.compile("/foo/bar");
        JsonPointer p2 = JsonPointer.valueOf("/foo/bar");
        assertEquals(p1, p2);
    }

    @Test(timeout = 4000)
    public void testEscapedTildeAndSlash() {
        // ~0 resolves to '~', ~1 resolves to '/'
        JsonPointer ptr = JsonPointer.compile("/~0root/~1path/sub~0~1test");

        assertEquals("~root", ptr.getMatchingProperty());
        JsonPointer p2 = ptr.tail();
        assertNotNull(p2);
        assertEquals("/path", p2.getMatchingProperty());
        JsonPointer p3 = p2.tail();
        assertNotNull(p3);
        assertEquals("sub~/test", p3.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testEscapeSpecialEdgeCases() {
        // Escape sequence where escape char is not 0 or 1 (e.g. ~2)
        JsonPointer p1 = JsonPointer.compile("/~2custom");
        assertEquals("~2custom", p1.getMatchingProperty());

        // Escape at index 2 boundary (i <= 2)
        JsonPointer p2 = JsonPointer.compile("/~0");
        assertEquals("~", p2.getMatchingProperty());

        // Consecutive escapes
        JsonPointer p3 = JsonPointer.compile("/~0~0/~1~1");
        assertEquals("~~", p3.getMatchingProperty());
        assertEquals("//", p3.tail().getMatchingProperty());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndRootPointers() {
        JsonPointer empty1 = JsonPointer.compile(null);
        assertTrue(empty1.matches());
        assertEquals("", empty1.getMatchingProperty());
        assertEquals(-1, empty1.getMatchingIndex());
        assertEquals("", empty1.toString());

        JsonPointer empty2 = JsonPointer.compile("");
        assertSame(empty1, empty2);
        assertSame(JsonPointer.EMPTY, empty2);

        // Single slash: segment is empty string ""
        JsonPointer root = JsonPointer.compile("/");
        assertFalse(root.matches());
        assertEquals("", root.getMatchingProperty());
        assertEquals(-1, root.getMatchingIndex());
        assertTrue(root.tail().matches());
    }

    @Test(timeout = 4000)
    public void testEmptySegmentsInPath() {
        JsonPointer p = JsonPointer.compile("//");
        assertEquals("", p.getMatchingProperty());
        JsonPointer p2 = p.tail();
        assertNotNull(p2);
        assertEquals("", p2.getMatchingProperty());
        assertTrue(p2.tail().matches());
    }

    @Test(timeout = 4000)
    public void testIndexParsingBoundaries() {
        // Valid zero
        JsonPointer pZero = JsonPointer.compile("/0");
        assertEquals(0, pZero.getMatchingIndex());
        assertTrue(pZero.mayMatchElement());

        // Valid max 32-bit positive int: 2147483647 (10 digits)
        JsonPointer pMaxInt = JsonPointer.compile("/2147483647");
        assertEquals(Integer.MAX_VALUE, pMaxInt.getMatchingIndex());

        // Overflow 32-bit positive int: 2147483648 (10 digits)
        JsonPointer pOverInt = JsonPointer.compile("/2147483648");
        assertEquals(-1, pOverInt.getMatchingIndex());

        // 10 digits large number: 9999999999
        JsonPointer pLarge10 = JsonPointer.compile("/9999999999");
        assertEquals(-1, pLarge10.getMatchingIndex());

        // More than 10 digits (len > 10)
        JsonPointer p11Digits = JsonPointer.compile("/10000000000");
        assertEquals(-1, p11Digits.getMatchingIndex());

        // Negative numbers are not valid index tokens per RFC
        JsonPointer pNegative = JsonPointer.compile("/-1");
        assertEquals(-1, pNegative.getMatchingIndex());

        // Non-digits interspersed
        JsonPointer pAlpha = JsonPointer.compile("/123a");
        assertEquals(-1, pAlpha.getMatchingIndex());
        JsonPointer pPreAlpha = JsonPointer.compile("/a123");
        assertEquals(-1, pPreAlpha.getMatchingIndex());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidInputMissingLeadingSlash() {
        JsonPointer.compile("invalid/path");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidInputSingleNonSlashChar() {
        JsonPointer.compile("a");
    }

    @Test(timeout = 4000)
    public void testMatchPropertyGuards() {
        JsonPointer ptr = JsonPointer.compile("/prop");

        // Non-matching property
        assertNull(ptr.matchProperty("other"));

        // Null match
        assertNull(ptr.matchProperty(null));

        // Matching on empty/terminal pointer returns null
        JsonPointer empty = JsonPointer.compile("");
        assertNull(empty.matchProperty(""));
        assertNull(empty.matchProperty("prop"));
    }

    @Test(timeout = 4000)
    public void testMatchElementGuards() {
        JsonPointer ptr = JsonPointer.compile("/5");

        // Negative index never matches
        assertNull(ptr.matchElement(-1));
        assertNull(ptr.matchElement(-5));

        // Mismatched index
        assertNull(ptr.matchElement(4));

        // Matched index returns tail
        JsonPointer tail = ptr.matchElement(5);
        assertNotNull(tail);
        assertTrue(tail.matches());

        // Terminal pointer cannot match element
        assertNull(tail.matchElement(0));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        JsonPointer p1 = JsonPointer.compile("/a/b/1");
        JsonPointer p2 = JsonPointer.compile("/a/b/1");
        JsonPointer p3 = JsonPointer.compile("/a/b/2");

        // Reflexivity
        assertEquals(p1, p1);

        // Symmetry
        assertEquals(p1, p2);
        assertEquals(p2, p1);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Distinctness
        assertNotEquals(p1, p3);
        assertNotEquals(p1.hashCode(), p3.hashCode());

        // Null and type divergence
        assertNotNull(p1);
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("/a/b/1")); // String is not JsonPointer
    }

    @Test(timeout = 4000)
    public void testToStringPreserved() {
        String expr = "/foo/~0bar/~1baz/42";
        JsonPointer ptr = JsonPointer.compile(expr);
        assertEquals(expr, ptr.toString());
    }
}