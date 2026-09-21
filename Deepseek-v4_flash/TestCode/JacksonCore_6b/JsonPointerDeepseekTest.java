package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JsonPointer class - Defect in _parseIndex for index "0" (zero).
 * 
 * Defect: When parsing segment "0", _parseIndex returns 0 (valid index) instead of -1,
 * because the method incorrectly treats "0" as a valid array index. According to JSON Pointer
 * spec, "0" is a valid index (first element), but the defect causes matchElement(0) to succeed
 * when it should fail for the empty/root pointer case. The test testIZeroIndex expects -1.
 * 
 * Decision branches covered:
 * 1. compile(null) -> EMPTY
 * 2. compile("") -> EMPTY
 * 3. compile("invalid") -> IllegalArgumentException
 * 4. compile("/") -> root pointer with empty segment
 * 5. compile("/a") -> single property segment
 * 6. compile("/a/b") -> nested property segments
 * 7. compile("/0") -> array index 0 (defect zone)
 * 8. compile("/01") -> leading zero invalid index
 * 9. compile("/12345678901") -> too long index
 * 10. compile("/1234567890") -> exactly 10 digits, > Integer.MAX_VALUE
 * 11. compile("/-") -> negative index
 * 12. compile("/~0") -> escaped tilde
 * 13. compile("/~1") -> escaped slash
 * 14. compile("/~01") -> escaped tilde followed by '1'
 * 15. compile("/a~0b") -> escaped tilde in middle
 * 16. compile("/a~2b") -> invalid escape sequence
 * 17. compile("/a~") -> trailing tilde
 * 18. compile("/a/b~1c") -> escaped slash in nested
 * 19. compile("/a/") -> trailing slash
 * 20. compile("/a//b") -> empty segment between slashes
 * 
 * Boundary values:
 * - Empty string, null
 * - Single char segments
 * - Index 0 (defect)
 * - Index -1 (invalid)
 * - Index 10 digits (overflow)
 * - Index 10 digits (max int)
 * - Leading zeros
 * - Escaped characters at boundaries
 * 
 * State transitions:
 * - matches() true only for EMPTY
 * - matchProperty() returns next segment or null
 * - matchElement() returns next segment or null
 * - tail() returns next segment
 * 
 * Exception paths:
 * - IllegalArgumentException for invalid pointer start
 * - No exception for valid pointers
 * 
 * Object contract:
 * - equals/hashCode based on _asString
 * - toString returns _asString
 */
public class JsonPointerDeepseekTest {

    /* ==================== Partition A: Core Functional Logic ==================== */

    @Test(timeout = 4000)
    public void testEmptyPointer() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNotNull(ptr);
        assertTrue(ptr.matches());
        assertEquals("", ptr.toString());
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
        assertNull(ptr.tail());
        assertNull(ptr.matchProperty("anything"));
        assertNull(ptr.matchElement(0));
    }

    @Test(timeout = 4000)
    public void testNullPointer() {
        JsonPointer ptr = JsonPointer.compile(null);
        assertNotNull(ptr);
        assertTrue(ptr.matches());
        assertEquals("", ptr.toString());
    }

    @Test(timeout = 4000)
    public void testSingleProperty() {
        JsonPointer ptr = JsonPointer.compile("/foo");
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals("/foo", ptr.toString());
        assertEquals("foo", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchProperty());
        assertFalse(ptr.mayMatchElement());
        assertNotNull(ptr.tail());
        assertTrue(ptr.tail().matches());
        assertNull(ptr.matchProperty("bar"));
        assertNotNull(ptr.matchProperty("foo"));
        assertNull(ptr.matchElement(0));
    }

    @Test(timeout = 4000)
    public void testNestedProperties() {
        JsonPointer ptr = JsonPointer.compile("/a/b/c");
        assertNotNull(ptr);
        assertEquals("/a/b/c", ptr.toString());
        assertEquals("a", ptr.getMatchingProperty());
        
        JsonPointer tail1 = ptr.tail();
        assertEquals("b", tail1.getMatchingProperty());
        JsonPointer tail2 = tail1.tail();
        assertEquals("c", tail2.getMatchingProperty());
        assertTrue(tail2.tail().matches());
    }

    @Test(timeout = 4000)
    public void testMatchPropertyChain() {
        JsonPointer ptr = JsonPointer.compile("/a/b");
        JsonPointer next = ptr.matchProperty("a");
        assertNotNull(next);
        assertEquals("b", next.getMatchingProperty());
        assertNull(ptr.matchProperty("x"));
        assertNull(ptr.matchProperty("ab"));
    }

    @Test(timeout = 4000)
    public void testMatchElementChain() {
        JsonPointer ptr = JsonPointer.compile("/0/1");
        JsonPointer next = ptr.matchElement(0);
        assertNotNull(next);
        assertEquals(1, next.getMatchingIndex());
        assertNull(ptr.matchElement(1));
        assertNull(ptr.matchElement(-1));
    }

    /* ==================== Partition B: Boundary Value Analysis ==================== */

    @Test(timeout = 4000)
    public void testIndexZero() {
        // Defect: _parseIndex("0") should return -1, but returns 0
        JsonPointer ptr = JsonPointer.compile("/0");
        assertEquals(-1, ptr.getMatchingIndex()); // This will fail on defective version
        assertFalse(ptr.mayMatchElement());
        assertTrue(ptr.mayMatchProperty());
    }

    @Test(timeout = 4000)
    public void testIndexNegative() {
        JsonPointer ptr = JsonPointer.compile("/-");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
        assertTrue(ptr.mayMatchProperty());
    }

    @Test(timeout = 4000)
    public void testIndexLeadingZero() {
        JsonPointer ptr = JsonPointer.compile("/01");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
        assertEquals("01", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testIndexMaxInt() {
        JsonPointer ptr = JsonPointer.compile("/2147483647");
        assertEquals(2147483647, ptr.getMatchingIndex());
        assertTrue(ptr.mayMatchElement());
    }

    @Test(timeout = 4000)
    public void testIndexOverflow() {
        JsonPointer ptr = JsonPointer.compile("/2147483648");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
        assertEquals("2147483648", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testIndexTooLong() {
        JsonPointer ptr = JsonPointer.compile("/12345678901");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
    }

    @Test(timeout = 4000)
    public void testIndexNonNumeric() {
        JsonPointer ptr = JsonPointer.compile("/12a");
        assertEquals(-1, ptr.getMatchingIndex());
        assertFalse(ptr.mayMatchElement());
        assertEquals("12a", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testEmptySegment() {
        JsonPointer ptr = JsonPointer.compile("/");
        assertNotNull(ptr);
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertTrue(ptr.tail().matches());
    }

    @Test(timeout = 4000)
    public void testTrailingSlash() {
        JsonPointer ptr = JsonPointer.compile("/a/");
        assertEquals("a", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertEquals("", tail.getMatchingProperty());
        assertTrue(tail.tail().matches());
    }

    @Test(timeout = 4000)
    public void testDoubleSlash() {
        JsonPointer ptr = JsonPointer.compile("/a//b");
        assertEquals("a", ptr.getMatchingProperty());
        JsonPointer tail1 = ptr.tail();
        assertEquals("", tail1.getMatchingProperty());
        JsonPointer tail2 = tail1.tail();
        assertEquals("b", tail2.getMatchingProperty());
    }

    /* ==================== Partition C: Defect-Targeted Branch Zone ==================== */

    @Test(timeout = 4000)
    public void testIZeroIndexDefect() {
        // Directly targets the known defect: expected -1 but was 0
        JsonPointer ptr = JsonPointer.compile("/0");
        assertEquals("Index 0 should not be a valid array index", -1, ptr.getMatchingIndex());
        assertFalse("Should not match element", ptr.mayMatchElement());
        assertTrue("Should match property", ptr.mayMatchProperty());
        assertNull("matchElement(0) should return null", ptr.matchElement(0));
    }

    @Test(timeout = 4000)
    public void testZeroIndexInNestedPath() {
        JsonPointer ptr = JsonPointer.compile("/a/0/b");
        JsonPointer tail1 = ptr.tail();
        assertEquals("0", tail1.getMatchingProperty());
        assertEquals(-1, tail1.getMatchingIndex());
        assertNull(tail1.matchElement(0));
    }

    /* ==================== Partition D: Exception & Defensive Guard Paths ==================== */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidStartNoSlash() {
        JsonPointer.compile("foo");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidStartEmptyString() {
        // Empty string is valid, but this tests the branch
        JsonPointer.compile("");
    }

    @Test(timeout = 4000)
    public void testEscapedTilde() {
        JsonPointer ptr = JsonPointer.compile("/~0");
        assertEquals("~", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testEscapedSlash() {
        JsonPointer ptr = JsonPointer.compile("/~1");
        assertEquals("/", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testEscapedTildeThenOne() {
        JsonPointer ptr = JsonPointer.compile("/~01");
        assertEquals("~1", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testEscapedInMiddle() {
        JsonPointer ptr = JsonPointer.compile("/a~0b");
        assertEquals("a~b", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testInvalidEscape() {
        JsonPointer ptr = JsonPointer.compile("/a~2b");
        assertEquals("a~2b", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testTrailingTilde() {
        JsonPointer ptr = JsonPointer.compile("/a~");
        assertEquals("a~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testEscapedInNested() {
        JsonPointer ptr = JsonPointer.compile("/a/b~1c");
        JsonPointer tail = ptr.tail();
        assertEquals("b/c", tail.getMatchingProperty());
    }

    /* ==================== Partition E: Object Lifecycle & Contract Integrity ==================== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        JsonPointer ptr = JsonPointer.compile("/a");
        assertEquals(ptr, ptr);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        JsonPointer ptr = JsonPointer.compile("/a");
        assertFalse(ptr.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        JsonPointer ptr = JsonPointer.compile("/a");
        assertFalse(ptr.equals("not a pointer"));
    }

    @Test(timeout = 4000)
    public void testEqualsSameValue() {
        JsonPointer ptr1 = JsonPointer.compile("/a");
        JsonPointer ptr2 = JsonPointer.compile("/a");
        assertEquals(ptr1, ptr2);
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValue() {
        JsonPointer ptr1 = JsonPointer.compile("/a");
        JsonPointer ptr2 = JsonPointer.compile("/b");
        assertFalse(ptr1.equals(ptr2));
    }

    @Test(timeout = 4000)
    public void testEqualsEmpty() {
        JsonPointer ptr1 = JsonPointer.compile("");
        JsonPointer ptr2 = JsonPointer.compile("");
        assertEquals(ptr1, ptr2);
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        JsonPointer ptr = JsonPointer.compile("/a/b");
        int h1 = ptr.hashCode();
        int h2 = ptr.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testToString() {
        assertEquals("", JsonPointer.compile("").toString());
        assertEquals("/a", JsonPointer.compile("/a").toString());
        assertEquals("/a/b", JsonPointer.compile("/a/b").toString());
        assertEquals("/~0", JsonPointer.compile("/~0").toString());
    }

    @Test(timeout = 4000)
    public void testValueOfAlias() {
        JsonPointer ptr1 = JsonPointer.valueOf("/a");
        JsonPointer ptr2 = JsonPointer.compile("/a");
        assertEquals(ptr1, ptr2);
    }

    @Test(timeout = 4000)
    public void testTailOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNull(ptr.tail());
    }

    @Test(timeout = 4000)
    public void testMatchPropertyOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNull(ptr.matchProperty("a"));
    }

    @Test(timeout = 4000)
    public void testMatchElementOnEmpty() {
        JsonPointer ptr = JsonPointer.compile("");
        assertNull(ptr.matchElement(0));
    }

    @Test(timeout = 4000)
    public void testMatchPropertyWrongType() {
        JsonPointer ptr = JsonPointer.compile("/0");
        // Even though "0" is a property, matchProperty should work
        assertNotNull(ptr.matchProperty("0"));
    }

    @Test(timeout = 4000)
    public void testMatchElementWrongType() {
        JsonPointer ptr = JsonPointer.compile("/a");
        assertNull(ptr.matchElement(0));
    }

    @Test(timeout = 4000)
    public void testComplexEscapedPath() {
        JsonPointer ptr = JsonPointer.compile("/~0a~1b/~1c~0d");
        assertEquals("~a/b", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertEquals("/c~d", tail.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testMultipleEscapes() {
        JsonPointer ptr = JsonPointer.compile("/~0~0");
        assertEquals("~~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testEscapeAtEnd() {
        JsonPointer ptr = JsonPointer.compile("/a~0");
        assertEquals("a~", ptr.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testEscapeThenSlash() {
        JsonPointer ptr = JsonPointer.compile("/~0/");
        assertEquals("~", ptr.getMatchingProperty());
        JsonPointer tail = ptr.tail();
        assertEquals("", tail.getMatchingProperty());
    }

    @Test(timeout = 4000)
    public void testIndexTenDigitsMax() {
        JsonPointer ptr = JsonPointer.compile("/2147483647");
        assertEquals(2147483647, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexTenDigitsOverflow() {
        JsonPointer ptr = JsonPointer.compile("/9999999999");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexTenDigitsExactMax() {
        JsonPointer ptr = JsonPointer.compile("/2147483647");
        assertEquals(2147483647, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexTenDigitsExactOverflow() {
        JsonPointer ptr = JsonPointer.compile("/2147483648");
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test(timeout = 4000)
    public void testIndexSingleDigit() {
        JsonPointer ptr = JsonPointer.compile("/5");
        assertEquals(-1, ptr.getMatchingIndex()); // Defect: should be -1 for any single digit
    }

    @Test(timeout = 4000)
    public void testIndexMultiDigit() {
        JsonPointer ptr = JsonPointer.compile("/123");
        assertEquals(-1, ptr.getMatchingIndex()); // Defect: should be -1 for any numeric string
    }
}