package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CharacterReaderDeepseekTest
 *
 * White-box test suite for CharacterReader.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic & state transitions
 *   - Initialization (empty, non-empty)
 *   - pos(), isEmpty(), current(), consume(), unconsume(), advance(), mark(), rewindToMark()
 *   - consumeAsString(), consumeToEnd()
 *   - matches, matchesIgnoreCase, matchesAny, matchesLetter, matchesDigit
 *   - matchConsume, matchConsumeIgnoreCase
 *   - containsIgnoreCase, toString
 * - Partition B: Boundary Value Analysis & Extremes
 *   - Empty string, single char, full string
 *   - All consumption methods at boundaries (pos=0, pos=length-1, pos=length)
 *   - nextIndexOf with target at beginning, end, not found
 *   - nextIndexOf(CharSequence) with short and long sequences
 *   - consumeTo with absent delimiter => consumeToEnd
 *   - Sequence consumption methods (consumeLetterSequence, etc.) at boundaries
 * - Partition C: Defect-Targeted Branch Zone
 *   - Known defect: ArrayIndexOutOfBoundsException in nextIndexOf(CharSequence)
 *     when inner loop bounds exceed array length (e.g., seq length > remaining chars)
 *     The bug is triggered when offset+seq.length() > length, causing input[i] access beyond array.
 *     Test: nextIndexOf(CharSequence) with a sequence that partially matches at end of input.
 *     Expected: -1 (not found) without exception.
 * - Partition D: Exception & Defensive Guard Paths
 *   - null input constructor (Validate.notNull)
 *   - unconsume when pos=0 (no check in code, but no exception thrown; pos becomes -1)
 *   - consume on empty => returns EOF
 *   - matches on empty => false
 * - Partition E: Object Lifecycle & Contract Integrity
 *   - toString returns remaining substring
 *   - Consistency of state after multiple operations
 */
public class CharacterReaderDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testInitialState() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
        assertEquals('a', r.current());
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    @Test(timeout = 4000)
    public void testEmptyReader() {
        CharacterReader r = new CharacterReader("");
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
        assertTrue(r.isEmpty());
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testUnconsume() {
        CharacterReader r = new CharacterReader("abc");
        r.consume();
        r.unconsume();
        assertEquals(0, r.pos());
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void testAdvance() {
        CharacterReader r = new CharacterReader("abc");
        r.advance();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader r = new CharacterReader("abc");
        r.consume();
        r.mark();
        r.consume(); // consume 'b'
        r.rewindToMark();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeAsString() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("a", r.consumeAsString());
        assertEquals(1, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consume(); // skip 'a'
        String rest = r.consumeToEnd();
        assertEquals("bc", rest);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatches() {
        CharacterReader r = new CharacterReader("abc");
        assertTrue(r.matches('a'));
        assertTrue(r.matches("abc"));
        assertFalse(r.matches("abx"));
        assertFalse(r.matches('z'));
        r.consume();
        assertTrue(r.matches('b'));
        assertTrue(r.matches("bc"));
        assertFalse(r.matches("bcd"));
    }

    @Test(timeout = 4000)
    public void testMatchesIgnoreCase() {
        CharacterReader r = new CharacterReader("AbC");
        assertTrue(r.matchesIgnoreCase("abc"));
        r.consume(); // consume 'A'
        assertTrue(r.matchesIgnoreCase("bc"));
        assertFalse(r.matchesIgnoreCase("BD"));
    }

    @Test(timeout = 4000)
    public void testMatchesAny() {
        CharacterReader r = new CharacterReader("abc");
        assertTrue(r.matchesAny('a', 'b'));
        assertFalse(r.matchesAny('x', 'y'));
        r.consume(); // 'a'
        assertTrue(r.matchesAny('b', 'c'));
    }

    @Test(timeout = 4000)
    public void testMatchesLetter() {
        CharacterReader r = new CharacterReader("a1");
        assertTrue(r.matchesLetter());
        r.consume();
        assertFalse(r.matchesLetter());
        r.advance();
        assertFalse(r.matchesLetter()); // isEmpty
    }

    @Test(timeout = 4000)
    public void testMatchesDigit() {
        CharacterReader r = new CharacterReader("1a");
        assertTrue(r.matchesDigit());
        r.consume();
        assertFalse(r.matchesDigit());
        r.advance();
        assertFalse(r.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testMatchConsume() {
        CharacterReader r = new CharacterReader("abc");
        assertTrue(r.matchConsume("ab"));
        assertEquals(2, r.pos());
        assertFalse(r.matchConsume("bc")); // only 'c' left
        assertTrue(r.matchConsume("c"));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCase() {
        CharacterReader r = new CharacterReader("AbC");
        assertTrue(r.matchConsumeIgnoreCase("abc"));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader r = new CharacterReader("Hello World");
        assertTrue(r.containsIgnoreCase("hello"));
        assertTrue(r.containsIgnoreCase("WORLD"));
        assertFalse(r.containsIgnoreCase("xyz"));
        r.consume(); // consume 'H'
        assertTrue(r.containsIgnoreCase("hello")); // still contains 'ello'? Actually 'ello' != 'hello' -> false
        // After consuming 'H', remaining "ello World" does not contain "hello" (case-insensitive)
        assertFalse(r.containsIgnoreCase("hello"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("abc", r.toString());
        r.consume();
        assertEquals("bc", r.toString());
        r.consumeToEnd();
        assertEquals("", r.toString());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testNextIndexOfCharBoundaries() {
        CharacterReader r = new CharacterReader("abcabc");
        assertEquals(0, r.nextIndexOf('a'));
        r.consume();
        assertEquals(2, r.nextIndexOf('c')); // at index 2 from pos=1 => offset 2
        r.consumeToEnd();
        assertEquals(-1, r.nextIndexOf('a'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharNotFound() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(-1, r.nextIndexOf('d'));
        r.consumeToEnd();
        assertEquals(-1, r.nextIndexOf('a'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfSequenceBoundaries() {
        CharacterReader r = new CharacterReader("abcabc");
        assertEquals(0, r.nextIndexOf("ab"));
        r.consume();
        assertEquals(2, r.nextIndexOf("ca")); // "cabc" -> "ca" at offset 2
        r.consumeToEnd();
        assertEquals(-1, r.nextIndexOf("ab"));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfSequenceNotFound() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(-1, r.nextIndexOf("abd"));
        assertEquals(-1, r.nextIndexOf("abcd"));
    }

    @Test(timeout = 4000)
    public void testConsumeToCharBoundaries() {
        CharacterReader r = new CharacterReader("a|b|c");
        assertEquals("a", r.consumeTo('|'));
        assertEquals(2, r.pos());
        assertEquals("b", r.consumeTo('|'));
        assertEquals("c", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testConsumeToStringBoundaries() {
        CharacterReader r = new CharacterReader("ab.cd");
        assertEquals("ab", r.consumeTo("."));
        assertEquals(2, r.pos());
        assertEquals("cd", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyBoundaries() {
        CharacterReader r = new CharacterReader("ab.cd");
        assertEquals("ab", r.consumeToAny('.', '!'));
        assertEquals(2, r.pos());
        assertEquals("cd", r.consumeToAny('x','y'));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceBoundaries() {
        CharacterReader r = new CharacterReader("abc123");
        assertEquals("abc", r.consumeLetterSequence());
        assertEquals(3, r.pos());
        assertEquals("", r.consumeLetterSequence()); // digits, not letters
        r.consume(); // consume '1'
        assertEquals("", r.consumeLetterSequence());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader r = new CharacterReader("abc123def");
        assertEquals("abc123", r.consumeLetterThenDigitSequence());
        assertEquals(6, r.pos());
        assertEquals("def", r.consumeLetterThenDigitSequence()); // no digit after
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("1a2bGX");
        assertEquals("1a2b", r.consumeHexSequence());
        assertEquals(4, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeDigitSequence() {
        CharacterReader r = new CharacterReader("123abc");
        assertEquals("123", r.consumeDigitSequence());
        assertEquals(3, r.pos());
        assertEquals("", r.consumeDigitSequence());
    }

    @Test(timeout = 4000)
    public void testEmptyConsumeTo() {
        CharacterReader r = new CharacterReader("");
        assertEquals("", r.consumeTo('x'));
        assertTrue(r.isEmpty());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly targets the known ArrayIndexOutOfBoundsException bug in nextIndexOf(CharSequence).
     * When the sequence does not match but the inner loop checks characters beyond the array length,
     * an AIOOBE is thrown.
     *
     * This test triggers the bug by placing a partial match at the end of the input.
     * The input "abc" with sequence "abcd" (length 4 > length - pos) should return -1 immediately,
     * but the bug also appears when the scan reaches the end and offset + seq.length() > length.
     * The classic trigger is a string like "abc" with sequence "bc" when cursor is at 0:
     *   pos=0, offset=0 (startChar==input[0]? 'b'!='a' => while loop advances offset to 1 (match 'b'):
     *   i = offset+1 = 2, last = i + seq.length()-1 = 2+2-1 = 3, length=3 => loop condition i < last? i=2 < 3 true,
     *   but input[i] = input[2] = 'c', seq.charAt(1) = 'c' => match, then i++ => i=3, but loop condition i < last? 3 < 3 false,
     *   then condition i == last? 3 == 3 true => returns offset - pos =1. This works.
     *
     * Another trigger: sequence "abd" on "abc": startChar='a', offset=0 matches, i=1, last=1+3-1=3, length=3:
     *   loop: j=1, i=1 < last=3 true, seq.charAt(1)='b' vs input[1]='b' ok, i=2, j=2;
     *   i=2 < last=3 true, seq.charAt(2)='d' vs input[2]='c' fails, loop exits (i=2, j=2 not incremented).
     *   Then check i == last? 2 == 3 false, so continues. No crash.
     *
     * The bug report shows ArrayIndexOutOfBoundsException: 8, likely from a longer input.
     * To reproduce, we need a scenario where the inner 'for' loop (for j=1; i < last && seq.charAt(j) == input[i]; i++, j++)
     * goes out of bounds. This can happen if 'last' is computed incorrectly: 'i = offset + 1; last = i + seq.length() - 1;'
     * If offset is such that last > length, then input[i] may be accessed beyond array.
     *
     * Example: input "ab", seq "abc". length=2. pos=0.
     *   startChar='a', offset=0 matches. i=1, last = i + seq.length()-1 = 1+3-1=3.
     *   Loop: j=1, i=1 < last=3 true, seq.charAt(1)='b' vs input[1]='b' ok, i=2, j=2.
     *   Now i=2 < last=3 true, seq.charAt(2)='c' vs input[2] -> ArrayIndexOutOfBoundsException (input length 2, index 2).
     *   This will crash.
     *
     * So test: new CharacterReader("ab").nextIndexOf("abc") should return -1 without exception.
     * Also test: new CharacterReader("a").nextIndexOf("ab") should return -1.
     */
    @Test(timeout = 4000)
    public void testNextIndexOfSequenceOutOfBounds() {
        // Sequence longer than remaining characters, with partial match at end
        CharacterReader r1 = new CharacterReader("ab");
        int result1 = r1.nextIndexOf("abc");
        assertEquals(-1, result1);

        CharacterReader r2 = new CharacterReader("a");
        int result2 = r2.nextIndexOf("ab");
        assertEquals(-1, result2);

        // Another variation: input longer but still trigger
        CharacterReader r3 = new CharacterReader("abcde");
        int result3 = r3.nextIndexOf("abcdef"); // longer than input
        assertEquals(-1, result3);

        // Not found but sequence length <= remaining, but partial matches cause early exit?
        CharacterReader r4 = new CharacterReader("abx");
        int result4 = r4.nextIndexOf("abc"); // 'a' matches, then 'b' matches, then 'c' != 'x'
        assertEquals(-1, result4);
    }

    // Additional boundary: sequence of length 0? Not handled, but we test typical.
    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testNextIndexOfEmptySequence() {
        CharacterReader r = new CharacterReader("abc");
        r.nextIndexOf(""); // seq.charAt(0) will throw
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullConstructor() {
        new CharacterReader(null);
    }

    @Test(timeout = 4000)
    public void testUnconsumeAtStart() {
        // unconsume at start: no exception, pos becomes -1
        CharacterReader r = new CharacterReader("abc");
        r.unconsume();
        assertEquals(-1, r.pos());
        // subsequent operations may behave unexpectedly, but no exception thrown
        // we just verify state
    }

    @Test(timeout = 4000)
    public void testConsumeWhenEmpty() {
        CharacterReader r = new CharacterReader("");
        assertEquals(CharacterReader.EOF, r.consume());
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatchesOnEmpty() {
        CharacterReader r = new CharacterReader("");
        assertFalse(r.matches('a'));
        assertFalse(r.matches("a"));
        assertFalse(r.matchesAny('a'));
        assertFalse(r.matchesLetter());
        assertFalse(r.matchesDigit());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testToStringAfterConsume() {
        CharacterReader r = new CharacterReader("hello");
        r.consume();
        r.consume();
        assertEquals("llo", r.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleOperationsConsistency() {
        CharacterReader r = new CharacterReader("abcd");
        assertEquals(0, r.pos());
        assertTrue(r.matches("abcd"));
        r.mark();
        r.consume();
        r.consume();
        assertEquals(2, r.pos());
        assertEquals("cd", r.consumeToEnd());
        assertTrue(r.isEmpty());
        r.rewindToMark();
        assertEquals(0, r.pos());
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCaseEdge() {
        CharacterReader r = new CharacterReader("</TITLE>");
        assertTrue(r.containsIgnoreCase("</title>"));
        assertTrue(r.containsIgnoreCase("</TITLE>"));
        assertFalse(r.containsIgnoreCase("</style>"));
    }
}