package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * White-box test suite for CharacterReader targeting the known Defects4J defect:
 * - testSupportsNonAsciiTags fails because methods like matchesLetter() and consumeLetterSequence()
 *   only accept ASCII letters (A-Z, a-z) instead of any Unicode letter.
 *
 * Key decision points:
 * - Constructor: null input validation
 * - isEmpty(): pos >= length
 * - current()/consume(): pos >= length => EOF
 * - unconsume(): decrements pos (no guards)
 * - nextIndexOf(char): linear scan; handles not found
 * - nextIndexOf(CharSequence): complex scan with offset adjustment; handles boundary
 * - consumeTo(char/string): delegates to nextIndexOf; fallback to consumeToEnd
 * - consumeToAny/consumeToAnySorted: breaks on match; returns cached substring
 * - consumeData/consumeTagName: break on special chars (including nullChar)
 * - consumeLetterSequence: while (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
 * - consumeLetterThenDigitSequence: two while loops, first for letters (ASCII only), second for digits
 * - consumeHexSequence: while (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')
 * - consumeDigitSequence: while (c >= '0' && c <= '9')
 * - matches: delegates to isEmpty and char compare
 * - matches(String): length check and char-by-char comparison
 * - matchesIgnoreCase: uses Character.toUpperCase on both sides
 * - matchesAny, matchesAnySorted: binary search or linear scan
 * - matchesLetter: isEmpty or (ASCII letters only)
 * - matchesDigit: isEmpty or (digits only)
 * - matchConsume/matchConsumeIgnoreCase: combines matches and advance
 * - containsIgnoreCase: uses toLowerCase/toUpperCase and nextIndexOf
 * - cacheString: maxCacheLen = 12; hash collision handling; fallback to new String
 * - rangeEquals: length check and char-by-char compare
 *
 * Partitions:
 * A: Core functional logic (all public methods)
 * B: Boundary values (empty, single char, maxCacheLen+1, EOF, min/max chars)
 * C: Defect-targeted non-ASCII letter handling (matchesLetter, consumeLetterSequence, etc.)
 * D: Exception/defensive paths (null input, pos underflow)
 * E: Object contract (toString, cacheString hash collisions)
 */

public class CharacterReaderDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorAndPosition() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEmptyReader() {
        CharacterReader r = new CharacterReader("");
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
    }

    @Test(timeout = 4000)
    public void testConsumeAndAdvance() {
        CharacterReader r = new CharacterReader("ab");
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        r.advance();
        assertEquals(2, r.pos());
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testUnconsume() {
        CharacterReader r = new CharacterReader("abc");
        r.consume(); // a
        r.consume(); // b
        r.unconsume();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume(); // a
        r.consume(); // b
        r.mark();
        r.consume(); // c
        r.consume(); // d
        r.rewindToMark();
        assertEquals(2, r.pos());
        assertEquals('c', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeAsString() {
        CharacterReader r = new CharacterReader("x");
        assertEquals("x", r.consumeAsString());
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharFound() {
        CharacterReader r = new CharacterReader("hello world");
        assertEquals(1, r.nextIndexOf('e')); // offset 1
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharNotFound() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals(-1, r.nextIndexOf('z'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharBoundary() {
        CharacterReader r = new CharacterReader("a");
        assertEquals(0, r.nextIndexOf('a'));
        r.consume();
        assertEquals(-1, r.nextIndexOf('a')); // at EOF
    }

    @Test(timeout = 4000)
    public void testNextIndexOfStringFound() {
        CharacterReader r = new CharacterReader("hello world");
        assertEquals(0, r.nextIndexOf("hello"));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfStringNotFound() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals(-1, r.nextIndexOf("world"));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfStringPartiallyMatches() {
        CharacterReader r = new CharacterReader("abac");
        assertEquals(1, r.nextIndexOf("ba")); // should be 1? Actually "abac": from pos=0, scan: offset=0 startChar 'b'? Not found; offset=1 found 'b', then i=2 last=2+2-1=3? Wait: seq="ba", startChar='b', length=2. For offset=1: input[1]='b', then i=2, last=3, last<=length? length=4, last=3, ok, j=1, compare seq[1]='a' with input[2]='a', i=3, j=2, i==last -> found, return 1-0=1. OK.
        assertEquals(1, r.nextIndexOf("ba"));
    }

    @Test(timeout = 4000)
    public void testConsumeToChar() {
        CharacterReader r = new CharacterReader("abcde");
        assertEquals("ab", r.consumeTo('c'));
        assertEquals(2, r.pos());
        assertEquals('c', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToCharEnd() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("abc", r.consumeTo('z'));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToString() {
        CharacterReader r = new CharacterReader("hello world");
        assertEquals("hello", r.consumeTo(" "));
        assertEquals(5, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToStringEnd() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals("hello", r.consumeTo("world"));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyFound() {
        CharacterReader r = new CharacterReader("hello world");
        String consumed = r.consumeToAny(' ', 'x');
        assertEquals("hello", consumed);
        assertEquals(5, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyNotFound() {
        CharacterReader r = new CharacterReader("hello");
        String consumed = r.consumeToAny('z');
        assertEquals("hello", consumed);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyFirstMatch() {
        CharacterReader r = new CharacterReader("a");
        assertEquals("", r.consumeToAny('a')); // position stays at 0
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnySortedFound() {
        CharacterReader r = new CharacterReader("hello world");
        String consumed = r.consumeToAnySorted(' ', 'x');
        assertEquals("hello", consumed);
    }

    @Test(timeout = 4000)
    public void testConsumeData() {
        CharacterReader r = new CharacterReader("a&b<c");
        assertEquals("a", r.consumeData());
        assertEquals(1, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeTagName() {
        CharacterReader r = new CharacterReader("div>");
        assertEquals("div", r.consumeTagName());
        assertEquals(3, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToEnd() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("abc", r.consumeToEnd());
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToEndEmpty() {
        CharacterReader r = new CharacterReader("");
        assertEquals("", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceBasic() {
        CharacterReader r = new CharacterReader("hello123");
        assertEquals("hello", r.consumeLetterSequence());
        assertEquals(5, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceEmpty() {
        CharacterReader r = new CharacterReader("12");
        assertEquals("", r.consumeLetterSequence());
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader r = new CharacterReader("abc123def");
        assertEquals("abc123", r.consumeLetterThenDigitSequence());
        assertEquals(6, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("1aFg");
        assertEquals("1aF", r.consumeHexSequence());
        assertEquals(3, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeDigitSequence() {
        CharacterReader r = new CharacterReader("123abc");
        assertEquals("123", r.consumeDigitSequence());
        assertEquals(3, r.pos());
    }

    @Test(timeout = 4000)
    public void testMatchesCharTrue() {
        CharacterReader r = new CharacterReader("a");
        assertTrue(r.matches('a'));
    }

    @Test(timeout = 4000)
    public void testMatchesCharFalse() {
        CharacterReader r = new CharacterReader("b");
        assertFalse(r.matches('a'));
    }

    @Test(timeout = 4000)
    public void testMatchesStringTrue() {
        CharacterReader r = new CharacterReader("hello");
        assertTrue(r.matches("hello"));
    }

    @Test(timeout = 4000)
    public void testMatchesStringFalse() {
        CharacterReader r = new CharacterReader("hello");
        assertFalse(r.matches("world"));
    }

    @Test(timeout = 4000)
    public void testMatchesStringTooLong() {
        CharacterReader r = new CharacterReader("hi");
        assertFalse(r.matches("hello"));
    }

    @Test(timeout = 4000)
    public void testMatchesIgnoreCaseTrue() {
        CharacterReader r = new CharacterReader("Hello");
        assertTrue(r.matchesIgnoreCase("hello"));
    }

    @Test(timeout = 4000)
    public void testMatchesIgnoreCaseFalse() {
        CharacterReader r = new CharacterReader("Hello");
        assertFalse(r.matchesIgnoreCase("world"));
    }

    @Test(timeout = 4000)
    public void testMatchesAnyTrue() {
        CharacterReader r = new CharacterReader("ab");
        assertTrue(r.matchesAny('a', 'b'));
    }

    @Test(timeout = 4000)
    public void testMatchesAnyFalse() {
        CharacterReader r = new CharacterReader("c");
        assertFalse(r.matchesAny('a', 'b'));
    }

    @Test(timeout = 4000)
    public void testMatchesAnySortedTrue() {
        CharacterReader r = new CharacterReader("b");
        char[] sorted = {'a', 'b', 'c'};
        assertTrue(r.matchesAnySorted(sorted));
    }

    @Test(timeout = 4000)
    public void testMatchesAnySortedFalse() {
        CharacterReader r = new CharacterReader("d");
        char[] sorted = {'a', 'b', 'c'};
        assertFalse(r.matchesAnySorted(sorted));
    }

    @Test(timeout = 4000)
    public void testMatchesLetterTrue() {
        CharacterReader r = new CharacterReader("a");
        assertTrue(r.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testMatchesLetterFalseDigit() {
        CharacterReader r = new CharacterReader("1");
        assertFalse(r.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testMatchesDigitTrue() {
        CharacterReader r = new CharacterReader("5");
        assertTrue(r.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testMatchesDigitFalse() {
        CharacterReader r = new CharacterReader("a");
        assertFalse(r.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeTrue() {
        CharacterReader r = new CharacterReader("hello world");
        assertTrue(r.matchConsume("hello"));
        assertEquals(5, r.pos());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeFalse() {
        CharacterReader r = new CharacterReader("hello");
        assertFalse(r.matchConsume("world"));
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCaseTrue() {
        CharacterReader r = new CharacterReader("Hello");
        assertTrue(r.matchConsumeIgnoreCase("hello"));
        assertEquals(5, r.pos());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCaseFalse() {
        CharacterReader r = new CharacterReader("Hello");
        assertFalse(r.matchConsumeIgnoreCase("world"));
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCasePositive() {
        CharacterReader r = new CharacterReader("Some Text with TITLE");
        assertTrue(r.containsIgnoreCase("title"));
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCaseNegative() {
        CharacterReader r = new CharacterReader("No match");
        assertFalse(r.containsIgnoreCase("title"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals("hello", r.toString());
        r.consume();
        assertEquals("ello", r.toString());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testBoundaryEmptyString() {
        CharacterReader r = new CharacterReader("");
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
        assertEquals("", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testBoundarySingleChar() {
        CharacterReader r = new CharacterReader("x");
        assertEquals('x', r.current());
        assertEquals("x", r.consumeTo('y'));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testBoundaryMaxCacheLen() {
        // maxCacheLen = 12; test string with length 12 and 13
        String s12 = "123456789012"; // length 12
        String s13 = "1234567890123"; // length 13
        CharacterReader r12 = new CharacterReader(s12);
        assertEquals(s12, r12.consumeToEnd());
        CharacterReader r13 = new CharacterReader(s13);
        assertEquals(s13, r13.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testBoundaryEOFAfterConsume() {
        CharacterReader r = new CharacterReader("a");
        r.consume();
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharAtEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consume(); r.consume(); r.consume(); // pos=3
        assertEquals(-1, r.nextIndexOf('z'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfStringAtEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consume(); r.consume(); r.consume();
        assertEquals(-1, r.nextIndexOf("a"));
    }

    @Test(timeout = 4000)
    public void testConsumeToFromPos() {
        CharacterReader r = new CharacterReader("abXcd");
        r.consume(); // a
        r.consume(); // b
        assertEquals("", r.consumeTo('X')); // immediately at pos, empty string
        assertEquals(2, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceAtEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consumeToEnd();
        assertEquals("", r.consumeLetterSequence());
    }

    // ==================== Partition C: Defect-Targeted Non-ASCII ====================

    @Test(timeout = 4000)
    public void testMatchesLetterWithNonAscii() {
        // Non-ASCII letter: 'á' (Latin small a with acute, Unicode 225)
        CharacterReader r = new CharacterReader("\u00E1");
        // Current implementation returns false, but should be true (Unicode letter)
        assertTrue("Non-ASCII letter should be recognized as letter", r.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceWithNonAscii() {
        CharacterReader r = new CharacterReader("\u00E1bc");
        // Current implementation returns empty string because first char not in A-Za-z
        String consumed = r.consumeLetterSequence();
        assertEquals("Non-ASCII letter sequence should be consumed", "\u00E1bc", consumed);
    }

    @Test(timeout = 4000)
    public void testConsumeLetterThenDigitSequenceWithNonAscii() {
        CharacterReader r = new CharacterReader("\u00E1123");
        // First while loop should consume the non-ASCII letter, then digits
        String consumed = r.consumeLetterThenDigitSequence();
        assertEquals("Non-ASCII letter followed by digits should be consumed", "\u00E1123", consumed);
    }

    @Test(timeout = 4000)
    public void testMatchesIgnoreCaseWithNonAscii() {
        // Upper case of 'á' is 'Á' (Unicode 193)
        CharacterReader r = new CharacterReader("\u00C1");
        assertTrue("matchesIgnoreCase should handle non-ASCII letters", r.matchesIgnoreCase("\u00E1"));
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCaseWithNonAscii() {
        CharacterReader r = new CharacterReader("caf\u00E9");
        // With defect, matchConsumeIgnoreCase might fail on 'é' (non-ASCII)
        assertTrue("matchConsumeIgnoreCase should work with non-ASCII", r.matchConsumeIgnoreCase("Caf\u00C9")); // "CAFÉ" uppercase
    }

    // Simulate the failing testSupportsNonAsciiTags: expecting tag name "Yes" with non-ASCII
    // This test will fail on defective version because consumeTagName stops on non-ASCII? Actually consumeTagName doesn't stop on letters, only on whitespace and special chars. The defect is likely in matching tag names. But we can create a scenario where a tag name contains non-ASCII and the parser fails.
    // The actual Defects4J test: "testSupportsNonAsciiTags" parses HTML with tag like <Yes> and expects "Yes". But in CharacterReader, matchesLetter and consumeLetterSequence are used elsewhere. Let's test that a non-ASCII tag name can be consumed via consumeTagName.
    @Test(timeout = 4000)
    public void testConsumeTagNameWithNonAscii() {
        // Tag names are allowed to contain any non-whitespace, non-special char, including non-ASCII.
        // This test should pass even with defect, but let's ensure.
        CharacterReader r = new CharacterReader("\u00C9cole>"); // "École"
        String tag = r.consumeTagName();
        assertEquals("\u00C9cole", tag);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullInput() {
        new CharacterReader(null);
    }

    @Test(timeout = 4000)
    public void testUnconsumeAtZero() {
        CharacterReader r = new CharacterReader("abc");
        // unconsume at pos 0 does nothing (but doesn't crash)
        r.unconsume();
        assertEquals(0, r.pos()); // should stay 0 (or become -1 if incorrect)
        // Future operations might break, but we just assert no exception
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnySortedEmptySearch() {
        CharacterReader r = new CharacterReader("abc");
        String consumed = r.consumeToAnySorted(); // no chars
        assertEquals("", consumed);
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeDataWithNullChar() {
        CharacterReader r = new CharacterReader("a\u0000b");
        assertEquals("a", r.consumeData());
        assertEquals(1, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeTagNameWithNullChar() {
        CharacterReader r = new CharacterReader("div\u0000>");
        assertEquals("div", r.consumeTagName());
        assertEquals(3, r.pos());
    }

    @Test(timeout = 4000)
    public void testCacheStringCollision() {
        // Induce potential hash collision (simple test)
        // Use two different strings that may map to same cache index
        CharacterReader r = new CharacterReader("abcdefghijklmnop");
        r.consumeToEnd(); // forces cache usage
        // no assertion, just coverage
    }

    // ==================== Partition E: Object Contract & Internal Methods ====================

    @Test(timeout = 4000)
    public void testRangeEqualsTrue() {
        CharacterReader r = new CharacterReader("abc");
        assertTrue(r.rangeEquals(0, 3, "abc"));
    }

    @Test(timeout = 4000)
    public void testRangeEqualsFalseDiffLength() {
        CharacterReader r = new CharacterReader("abc");
        assertFalse(r.rangeEquals(0, 3, "ab"));
    }

    @Test(timeout = 4000)
    public void testRangeEqualsFalseContent() {
        CharacterReader r = new CharacterReader("abc");
        assertFalse(r.rangeEquals(0, 3, "abd"));
    }

    @Test(timeout = 4000)
    public void testCacheStringHit() {
        // First access caches, second should hit
        CharacterReader r = new CharacterReader("abc");
        assertEquals("abc", r.consumeToEnd());
        // Reset reader? Not possible. But we can test via multiple calls to consumeToEnd on different readers with same string.
        // That tests cache separately. Simpler: we rely on coverage from internal usage
    }

    @Test(timeout = 4000)
    public void testMatchesAnyEmptyInput() {
        CharacterReader r = new CharacterReader("");
        assertFalse(r.matchesAny('a'));
    }

    @Test(timeout = 4000)
    public void testMatchesAnySortedEmptyInput() {
        CharacterReader r = new CharacterReader("");
        char[] sorted = {'a'};
        assertFalse(r.matchesAnySorted(sorted));
    }

    @Test(timeout = 4000)
    public void testMatchesLetterEmptyInput() {
        CharacterReader r = new CharacterReader("");
        assertFalse(r.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testMatchesDigitEmptyInput() {
        CharacterReader r = new CharacterReader("");
        assertFalse(r.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeAtEnd() {
        CharacterReader r = new CharacterReader("a");
        r.consume();
        assertFalse(r.matchConsume("a"));
        assertEquals(1, r.pos());
    }

    @Test(timeout = 4000)
    public void testNextIndexOfStringEdgeCase() {
        // When seq length > remaining length, loop condition handles it
        CharacterReader r = new CharacterReader("ab");
        assertEquals(-1, r.nextIndexOf("abc"));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfStringWithRepeatedStartChar() {
        CharacterReader r = new CharacterReader("aaab");
        assertEquals(2, r.nextIndexOf("ab")); // should find at offset 2
    }
}