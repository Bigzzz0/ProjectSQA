package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.core.JsonPointer
 * Defect Target: Issue #173 (Double-increment in _parseIndex skips odd-indexed characters)
 *
 * Decision / Branch Matrix:
 * 1. compile(String input) / valueOf(String input):
 *    - input == null -> returns EMPTY
 *    - input.length() == 0 -> returns EMPTY
 *    - input.charAt(0) != '/' -> throws IllegalArgumentException
 *    - input.charAt(0) == '/' -> proceeds to _parseTail(input)
 *
 * 2. _parseTail(String input) & _parseQuotedTail(String input, int i):
 *    - single unquoted segment (e.g. "/foo") -> loop finishes, points to EMPTY
 *    - multiple segments separated by '/' (e.g. "/foo/bar") -> recursive _parseTail calls
 *    - quoted tilde escape '~0' -> unescapes to '~'
 *    - quoted tilde escape '~1' -> unescapes to '/'
 *    - unrecognized escape '~x' -> appends '~' and 'x'
 *    - tilde at string boundary (e.g. "/foo~") -> boundary condition
 *    - tilde at start of segment (e.g. "/~0") vs offset > 2 (e.g. "/prefix~0")
 *
 * 3. _parseIndex(String str):
 *    - len == 0 -> returns -1
 *    - len > 10 -> returns -1 (overflow boundary)
 *    - all digit chars -> parses via NumberInput.parseInt(str)
 *    - non-digit char at even index (e.g., "-1", "a") -> returns -1
 *    - [DEFECT TARGET] non-digit char at odd index (e.g., "1e0", "1a", "0b") ->
 *      In buggy code, `i++` inside the loop combined with `++i` in the loop header skips odd indices,
 *      resulting in NumberFormatException on NumberInput.parseInt("1e0").
 *    - len == 10:
 *      * parsed long > Integer.MAX_VALUE ("2147483648", "9999999999") -> returns -1
 *      * parsed long <= Integer.MAX_VALUE ("2147483647") -> returns 2147483647
 *
 * 4. matchProperty(String) / matchElement(int):
 *    - _nextSegment == null -> returns null
 *    - matching property name equals vs not equals
 *    - matching element index equals vs not equals vs negative index (< 0)
 *
 * 5. Object Integrity:
 *    - equals (same ref, null, foreign class, equal content, unequal content)
 *    - hashCode consistency
 *    - toString() representation
 * ---------------------------------------------------------------------------------------------------
 */
public class JsonPointerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyPointerLifecycle() {
        JsonPointer empty = JsonPointer.compile("");
        assertSame(JsonPointer.EMPTY, empty);
        assertTrue(empty.matches());
        assertEquals("", empty.getMatchingProperty());
        assertEquals(-1, empty.getMatchingIndex());
        assertTrue(empty.mayMatchProperty());
        assertFalse(empty.mayMatchElement());
        assertNull(empty.tail());
        assertNull(empty.matchProperty("anything"));
        assertNull(empty.matchElement(0));
        assertEquals("", empty.toString());
    }

    @Test(timeout = 4000)
    public void testSinglePropertySegmentTraversal() {
        JsonPointer ptr = JsonPointer.compile("/users");
        assertFalse(ptr.matches());
        assertEquals("users", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
        assertEquals("/users", ptr.toString());

        JsonPointer tail = ptr.tail();
        assertNotNull(tail);
        assertTrue(tail.matches());
        assertSame(JsonPointer.EMPTY, tail);

        // Matching checks
        assertNull(ptr.matchProperty("other"));
        JsonPointer matched = ptr.matchProperty("users");
        assertNotNull(matched);
        assertSame(tail, matched);
        assertNull(ptr.matchElement(0));
    }

    @Test(timeout = 4000)
    public void testMultiSegmentHierarchy() {
        JsonPointer ptr = JsonPointer.compile("/a/b/c");
        assertEquals("a", ptr.getMatchingProperty());
        assertEquals("/a/b/c", ptr.toString());

        JsonPointer p1 = ptr.matchProperty("a");
        assertNotNull(p1);
        assertEquals("b", p1.getMatchingProperty());
        assertEquals("/b/c", p1.toString());

        JsonPointer p2 = p1.matchProperty("b");
        assertNotNull(p2);
        assertEquals("c", p2.getMatchingProperty());
        assertEquals("/c", p2.toString());

        JsonPointer p3 = p2.matchProperty("c");
        assertNotNull(p3);
        assertTrue(p3.matches());
        assertSame(JsonPointer.EMPTY, p3);
    }

    @Test(timeout = 4000)
    public void testArrayElementIndexMatching() {
        JsonPointer ptr = JsonPointer.compile("/0/15");
        assertEquals("0", ptr.getMatchingProperty());
        assertEquals(0, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchElement());

        assertNull(ptr.matchElement(1));
        assertNull(ptr.matchElement(-1));

        JsonPointer next = ptr.matchElement(0);
        assertNotNull(next);
        assertEquals("15", next.getMatchingProperty());
        assertEquals(15, next.getMatchingIndex());
        assertTrue(next.mayMatchElement());

        JsonPointer leaf = next.matchElement(15);
        assertNotNull(leaf);
        assertTrue(leaf.matches());
    }

    @Test(timeout = 4000)
    public void testEscapedCharactersTildeAndSlash() {
        // ~0 unescapes to ~ ; ~1 unescapes to /
        JsonPointer ptr = JsonPointer.compile("/~0/~1/a~0b/c~1d");
        assertEquals("~", ptr.getMatchingProperty());

        ptr = ptr.tail();
        assertEquals("/", ptr.getMatchingProperty());

        ptr = ptr.tail();
        assertEquals("a~b", ptr.getMatchingProperty());

        ptr = ptr.tail();
        assertEquals("c/d", ptr.getMatchingProperty());

        ptr = ptr.tail();
        assertTrue(ptr.matches());
    }

    @Test(timeout = 4000)
    public void testValueOfAlias() {
        JsonPointer ptr1 = JsonPointer.valueOf("/test/path");
        JsonPointer ptr2 = JsonPointer.compile("/test/path");
        assertEquals