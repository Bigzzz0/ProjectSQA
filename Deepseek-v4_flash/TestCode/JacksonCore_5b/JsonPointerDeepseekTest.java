package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: com.fasterxml.jackson.core.JsonPointer
 * 
 * Known Defect (Defects4J): 
 *   - testWonkyNumber173: NumberFormatException for input "1e0"
 *   - Root cause: _parseIndex() does not validate that the string is purely numeric
 *     before calling NumberInput.parseInt(). For "1e0", len==3 (not 0 or >10), all chars
 *     are digits? No: '1','e','0' - 'e' is not a digit, so the loop should catch it.
 *     However, the loop has a bug: `char c = str.charAt(i++);` increments i twice per
 *     iteration (once in charAt(i++) and once in the for-loop increment). This skips
 *     every other character, so for "1e0": i=0 -> c='1', i becomes 1, then for-loop
 *     increments to 2 -> c='0', i becomes 3, loop ends. 'e' is never checked!
 *     Then NumberInput.parseInt("1e0") throws NumberFormatException.
 * 
 * Branch/Decision coverage targets:
 * 1. compile() null/empty -> EMPTY
 * 2. compile() non-slash start -> IllegalArgumentException
 * 3. _parseTail() with no '/' and no '~' -> single segment
 * 4. _parseTail() with '/' segments
 * 5. _parseTail() with '~' escapes (0,1, other)
 * 6. _parseQuotedTail() with escapes at start, middle, end
 * 7. _parseIndex() boundary: len==0, len>10, len==10 with overflow, non-digit chars
 * 8. matchProperty()/matchElement() null/negative/valid
 * 9. equals/hashCode/toString
 * 10. mayMatchProperty/mayMatchElement
 * 
 * Partitions:
 * A: Core functional (compile, match, tail, getters)
 * B: Boundary (null, empty, len 10, max int, negative)
 * C: Defect-targeted (wonky numbers like "1e0", "12e3", "1.0")
 * D: Exception paths (invalid compile, invalid index)
 * E: Object contract (equals, hashCode, toString)
 */
public class JsonPointerDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testCompileEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNotNull(ptr);
        assertTrue(ptr.matches());
        assertEquals("", ptr.toString());
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
        assertNull(ptr.tail());
    }

    @Test(timeout = 4000)
    public void testCompileNull() {
        JsonPointer ptr = JsonPointer.compile(null);
        assertNotNull(ptr);
        assertTrue(ptr.matches());
        assertEquals("", ptr.toString());
    }

    @Test(timeout = 4000)
    public void testCompileSingleSegment() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertFalse(ptr.matches());
        assertEquals("foo", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
        assertNotNull(ptr.tail());
        assertTrue(ptr.tail().matches());
        assertEquals("", ptr.tail().getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileMultipleSegments() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar/baz");
        assertFalse(ptr.matches());
        assertEquals("foo", ptr.getMatchingProperty());
        JsonPointer second = ptr.tail();
        assertEquals("bar", second.getMatchingProperty());
        JsonPointer third = second.tail();
        assertEquals("baz", third.getMatchingProperty());
        assertTrue(third.tail().matches());
    }

    @Test(timeout = 4000)
    public void testCompileNumericSegment() {
        JsonPointer ptr = JsonPointer.compile("/123");
        assertFalse(ptr.matches());
        assertEquals(123, ptr.getMatchingIndex());
        assertEquals("123", ptr.getMatchingProperty());
        assertTrue(ptr.mayMatchElement());
        assertTrue(ptr.mayMatchProperty());
    }

    @Test(timeout = 4000)
    public void testMatchProperty() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");
        assertNotNull(ptr.matchProperty("foo"));
        assertNull(ptr.matchProperty("baz"));
        assertNull(ptr.matchProperty(null));
        JsonPointer tail = ptr.matchProperty("foo");
        assertNotNull(tail);
        assertEquals("bar", tail.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testMatchElement() {
        JsonPointer ptr = JsonPointer.compile("/0/1");
        assertNotNull(ptr.matchElement(0));
        assertNull(ptr.matchElement(1));
        assertNull(ptr.matchElement(-1));
        JsonPointer tail = ptr.matchElement(0);
        assertNotNull(tail);
        assertEquals(1, tail.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testTailOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNull(ptr.tail());
    }

    @Test(timeout = 4000)
    public void testValueOfAlias() {
        JsonPointer ptr = JsonPointer.valueOf("/a");
        assertEquals("/a", ptr.toString());
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testCompileSlashOnly() {
        JsonPointer ptr = JsonPointer.compile("/");
        assertFalse(ptr.matches());
        assertEquals("", ptr.getMatchingProperty());
        assertTrue(ptr.tail().matches());
    }

    @Test(timeout = 4000)
    public void testCompileDoubleSlash() {
        JsonPointer ptr = JsonPointer.compile("//");
        assertFalse(ptr.matches());
        assertEquals("", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertFalse(tail.matches());
        assertEquals("", tail.getMatchingProperty());
        assertTrue(tail.tail().matches());
    }

    @Test(timeout = 4000)
    public void testIndexBoundaryMaxInt() {
        JsonPointer ptr = JsonPointer.compile("/2147483647");
        assertEquals(2147483647, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexBoundaryOverflow() {
        JsonPointer ptr = JsonPointer.compile("/2147483648");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("2147483648", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testIndexBoundaryTenDigits() {
        JsonPointer ptr = JsonPointer.compile("/9999999999");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexBoundaryElevenDigits() {
        JsonPointer ptr = JsonPointer.compile("/12345678901");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexNegative() {
        JsonPointer ptr = JsonPointer.compile("/-1");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("-1", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testIndexLeadingZeros() {
        JsonPointer ptr = JsonPointer.compile("/00123");
        assertEquals(123, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexEmptyString() {
        JsonPointer ptr = JsonPointer.compile("/");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    /**
     * Targets the known defect: NumberFormatException on "1e0"
     * The bug is in _parseIndex(): the loop `char c = str.charAt(i++);` 
     * increments i twice per iteration, skipping the 'e' character.
     * Expected: "1e0" is not a valid index, so getMatchingIndex() should return -1
     * and getMatchingProperty() should return "1e0".
     */
    @Test(timeout = 4000)
    public void testWonkyNumberScientificNotation() {
        JsonPointer ptr = JsonPointer.compile("/1e0");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("1e0", ptr.getMatchingProperty());
        assertFalse(ptr.mayMatchElement());
    }

    @Test(timeout = 4000)
    public void testWonkyNumberDecimal() {
        JsonPointer ptr = JsonPointer.compile("/1.5");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("1.5", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testWonkyNumberHex() {
        JsonPointer ptr = JsonPointer.compile("/0x10");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("0x10", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testWonkyNumberWithPlus() {
        JsonPointer ptr = JsonPointer.compile("/+123");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("+123", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testWonkyNumberWithSpace() {
        JsonPointer ptr = JsonPointer.compile("/12 3");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("12 3", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testWonkyNumberWithLetters() {
        JsonPointer ptr = JsonPointer.compile("/12a34");
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("12a34", ptr.getMatchingProperty());
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCompileInvalidNoSlash() {
        JsonPointer.compile("foo");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCompileInvalidEmptyWithSpace() {
        JsonPointer.compile(" ");
    }

    @Test(timeout = 4000)
    public void testCompileEscapedTilde() {
        JsonPointer ptr = JsonPointer.compile("/~0");
        assertEquals("~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileEscapedSlash() {
        JsonPointer ptr = JsonPointer.compile("/~1");
        assertEquals("/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileEscapedOther() {
        JsonPointer ptr = JsonPointer.compile("/~2");
        assertEquals("~2", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileEscapedAtEnd() {
        JsonPointer ptr = JsonPointer.compile("/foo~0");
        assertEquals("foo~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileEscapedInMiddle() {
        JsonPointer ptr = JsonPointer.compile("/foo~1bar");
        assertEquals("foo/bar", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileMultipleEscapes() {
        JsonPointer ptr = JsonPointer.compile("/~0~1~0");
        assertEquals("~/~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileEscapedSegmentWithSlash() {
        JsonPointer ptr = JsonPointer.compile("/a~1b/c");
        assertEquals("a/b", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertEquals("c", tail.getMatchingProperty());
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertTrue(ptr.equals(ptr));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertFalse(ptr.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertFalse(ptr.equals("foo"));
    }

    @Test(timeout = 4000)
    public void testEqualsSameContent() {
        JsonPointer ptr1 = JsonPointer.compile("/foo/bar");
        JsonPointer ptr2 = JsonPointer.compile("/foo/bar");
        assertTrue(ptr1.equals(ptr2));
        assertTrue(ptr2.equals(ptr1));
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentContent() {
        JsonPointer ptr1 = JsonPointer.compile("/foo");
        JsonPointer ptr2 = JsonPointer.compile("/bar");
        assertFalse(ptr1.equals(ptr2));
    }

    @Test(timeout = 4000)
    public void testEqualsEmpty() {
        JsonPointer ptr1 = JsonPointer.compile("");
        JsonPointer ptr2 = JsonPointer.compile("");
        assertTrue(ptr1.equals(ptr2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");
        int h1 = ptr.hashCode();
        int h2 = ptr.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testToString() {
        assertEquals("/foo/bar", JsonPointer.compile("/foo/bar").toString());
        assertEquals("", JsonPointer.compile("").toString());
        assertEquals("/", JsonPointer.compile("/").toString());
    }

    @Test(timeout = 4000)
    public void testMatchPropertyOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNull(ptr.matchProperty("foo"));
    }

    @Test(timeout = 4000)
    public void testMatchElementOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNull(ptr.matchElement(0));
    }

    @Test(timeout = 4000)
    public void testMayMatchPropertyOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertFalse(ptr.mayMatchProperty());
    }

    @Test(timeout = 4000)
    public void testMayMatchElementOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertFalse(ptr.mayMatchElement());
    }

    @Test(timeout = 4000)
    public void testMayMatchPropertyOnNumeric() {
        JsonPointer ptr = JsonPointer.compile("/123");
        assertTrue(ptr.mayMatchProperty());
        assertTrue(ptr.mayMatchElement());
    }

    @Test(timeout = 4000)
    public void testMayMatchPropertyOnNonNumeric() {
        JsonPointer ptr = JsonPointer.compile("/abc");
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
    }

    @Test(timeout = 4000)
    public void testNestedTail() {
        JsonPointer ptr = JsonPointer.compile("/a/b/c");
        JsonPointer tail1 = ptr.tail();
        JsonPointer tail2 = tail1.tail();
        JsonPointer tail3 = tail2.tail();
        assertTrue(tail3.matches());
        assertNull(tail3.tail());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedSlashAndTail() {
        JsonPointer ptr = JsonPointer.compile("/a~1b/c~1d");
        assertEquals("a/b", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertEquals("c/d", tail.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithTildeAtStart() {
        JsonPointer ptr = JsonPointer.compile("/~0foo");
        assertEquals("~foo", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithTildeAtEnd() {
        JsonPointer ptr = JsonPointer.compile("/foo~0");
        assertEquals("foo~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithDoubleTilde() {
        JsonPointer ptr = JsonPointer.compile("/~~0");
        assertEquals("~~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlash() {
        JsonPointer ptr = JsonPointer.compile("/~0~1");
        assertEquals("~/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAtEndOfSegment() {
        JsonPointer ptr = JsonPointer.compile("/a~0/b");
        assertEquals("a~", ptr.getMatchingProperty());
        assertEquals("b", ptr.tail().getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedSlashAtEndOfSegment() {
        JsonPointer ptr = JsonPointer.compile("/a~1/b");
        assertEquals("a/", ptr.getMatchingProperty());
        assertEquals("b", ptr.tail().getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithMultipleEscapesAndSegments() {
        JsonPointer ptr = JsonPointer.compile("/~0~1~0/~1~0~1");
        assertEquals("~//", ptr.getMatchingProperty());
        assertEquals("/~", ptr.tail().getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeInMiddleOfLongSegment() {
        JsonPointer ptr = JsonPointer.compile("/abc~0def");
        assertEquals("abc~def", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedSlashInMiddleOfLongSegment() {
        JsonPointer ptr = JsonPointer.compile("/abc~1def");
        assertEquals("abc/def", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedOtherInMiddle() {
        JsonPointer ptr = JsonPointer.compile("/abc~2def");
        assertEquals("abc~2def", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAtVeryEnd() {
        JsonPointer ptr = JsonPointer.compile("/abc~0");
        assertEquals("abc~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedSlashAtVeryEnd() {
        JsonPointer ptr = JsonPointer.compile("/abc~1");
        assertEquals("abc/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithOnlyEscapedTilde() {
        JsonPointer ptr = JsonPointer.compile("/~0");
        assertEquals("~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithOnlyEscapedSlash() {
        JsonPointer ptr = JsonPointer.compile("/~1");
        assertEquals("/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithOnlyEscapedOther() {
        JsonPointer ptr = JsonPointer.compile("/~2");
        assertEquals("~2", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeThenSlash() {
        JsonPointer ptr = JsonPointer.compile("/~0~1");
        assertEquals("~/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedSlashThenTilde() {
        JsonPointer ptr = JsonPointer.compile("/~1~0");
        assertEquals("/~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeThenOther() {
        JsonPointer ptr = JsonPointer.compile("/~0~2");
        assertEquals("~~2", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedSlashThenOther() {
        JsonPointer ptr = JsonPointer.compile("/~1~2");
        assertEquals("/~2", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedOtherThenTilde() {
        JsonPointer ptr = JsonPointer.compile("/~2~0");
        assertEquals("~2~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedOtherThenSlash() {
        JsonPointer ptr = JsonPointer.compile("/~2~1");
        assertEquals("~2/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedOtherThenOther() {
        JsonPointer ptr = JsonPointer.compile("/~2~3");
        assertEquals("~2~3", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAtStartAndEnd() {
        JsonPointer ptr = JsonPointer.compile("/~0abc~0");
        assertEquals("~abc~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedSlashAtStartAndEnd() {
        JsonPointer ptr = JsonPointer.compile("/~1abc~1");
        assertEquals("/abc/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed() {
        JsonPointer ptr = JsonPointer.compile("/~0~1~0~1");
        assertEquals("~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed2() {
        JsonPointer ptr = JsonPointer.compile("/~1~0~1~0");
        assertEquals("/~~/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed3() {
        JsonPointer ptr = JsonPointer.compile("/~0~0~1~1");
        assertEquals("~~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed4() {
        JsonPointer ptr = JsonPointer.compile("/~1~1~0~0");
        assertEquals("//~~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed5() {
        JsonPointer ptr = JsonPointer.compile("/~0~1~0~1~0");
        assertEquals("~//~/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed6() {
        JsonPointer ptr = JsonPointer.compile("/~1~0~1~0~1");
        assertEquals("/~~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed7() {
        JsonPointer ptr = JsonPointer.compile("/~0~0~1~1~0~0");
        assertEquals("~~//~~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed8() {
        JsonPointer ptr = JsonPointer.compile("/~1~1~0~0~1~1");
        assertEquals("//~~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed9() {
        JsonPointer ptr = JsonPointer.compile("/~0~1~0~1~0~1~0");
        assertEquals("~//~/~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed10() {
        JsonPointer ptr = JsonPointer.compile("/~1~0~1~0~1~0~1");
        assertEquals("/~~//~/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed11() {
        JsonPointer ptr = JsonPointer.compile("/~0~0~1~1~0~0~1~1");
        assertEquals("~~//~~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed12() {
        JsonPointer ptr = JsonPointer.compile("/~1~1~0~0~1~1~0~0");
        assertEquals("//~~//~~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed13() {
        JsonPointer ptr = JsonPointer.compile("/~0~1~0~1~0~1~0~1~0");
        assertEquals("~//~/~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed14() {
        JsonPointer ptr = JsonPointer.compile("/~1~0~1~0~1~0~1~0~1");
        assertEquals("/~~//~~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed15() {
        JsonPointer ptr = JsonPointer.compile("/~0~0~1~1~0~0~1~1~0~0");
        assertEquals("~~//~~//~~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed16() {
        JsonPointer ptr = JsonPointer.compile("/~1~1~0~0~1~1~0~0~1~1");
        assertEquals("//~~//~~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed17() {
        JsonPointer ptr = JsonPointer.compile("/~0~1~0~1~0~1~0~1~0~1~0");
        assertEquals("~//~/~//~/", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed18() {
        JsonPointer ptr = JsonPointer.compile("/~1~0~1~0~1~0~1~0~1~0~1");
        assertEquals("/~~//~~//~~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed19() {
        JsonPointer ptr = JsonPointer.compile("/~0~0~1~1~0~0~1~1~0~0~1~1");
        assertEquals("~~//~~//~~//", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEscapedTildeAndSlashMixed20() {
        JsonPointer ptr = JsonPointer.compile("/~1~1~0~0~1~1~0~0~1~1~0~0");
        assertEquals("//~~//~~//~~", ptr.getMatchingProperty());
    }
}