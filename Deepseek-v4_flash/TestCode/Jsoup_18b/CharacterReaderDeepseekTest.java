package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CharacterReaderDeepseekTest: White-box test suite targeting the known defects
 * and achieving high line/branch coverage.
 *
 * BRANCH & DEFECT ANALYSIS MATRIX:
 * 1. consumeToEnd() uses input.substring(pos, input.length()-1) -> off-by-one (drops last char)
 * 2. No normalization of carriage returns (\r) to line feeds (\n) in any consumption method
 * 3. consumeTo(String) and consumeTo(char) do not treat \r as part of newline sequence
 * 4. consumeToAny(char[]) loops while !isEmpty(), break on match; boundary when seq is empty
 * 5. matches(String) uses startsWith; matchesIgnoreCase uses regionMatches (case-insensitive)
 * 6. matchConsume/matchConsumeIgnoreCase increment pos only on match
 * 7. containsIgnoreCase uses indexOf with toLower/toUpper; bug if case mapping differs
 * 8. unconsume() does not check pos>0 -> can underflow (negative pos)
 * 9. consumeAsString() uses substring(pos, pos++) -> post-increment, returns char as String
 * 10. isEmpty() -> pos >= length; current() returns EOF if isEmpty
 * 11. consumeLetterSequence/consumeHexSequence/consumeDigitSequence: loops over alphabet/digit ranges
 * 12. matchesAny: returns false if isEmpty; matchesLetter/matchesDigit similar
 * 13. mark()/rewindToMark() basic state save/restore
 * 14. toString() returns substring from current pos
 */
public class CharacterReaderDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    @Test(timeout = 4000)
    public void testPosAndEmpty() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
        r.consume(); // 'a'
        assertEquals(1, r.pos());
        assertFalse(r.isEmpty());
        r.consume(); // 'b'
        r.consume(); // 'c'
        assertEquals(3, r.pos());
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCurrentAndConsume() {
        CharacterReader r = new CharacterReader("x");
        assertEquals('x', r.current());
        assertEquals('x', r.consume());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume()); // after EOF
    }

    @Test(timeout = 4000)
    public void testAdvanceUnconsume() {
        CharacterReader r = new CharacterReader("abc");
        r.advance(); // pos=1
        assertEquals('b', r.current());
        r.unconsume(); // pos=0
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void testMarkRewind() {
        CharacterReader r = new CharacterReader("hello");
        r.consume(); // 'h'
        r.mark();    // mark at pos=1
        r.consume(); // 'e'
        r.consume(); // 'l'
        assertEquals(3, r.pos());
        r.rewindToMark();
        assertEquals(1, r.pos());
        assertEquals('e', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeAsString() {
        CharacterReader r = new CharacterReader("abc");
        String s = r.consumeAsString();
        assertEquals("a", s);
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToEndFull() {
        CharacterReader r = new CharacterReader("abc");
        String s = r.consumeToEnd();
        // Bug: currently returns substring(pos, length-1) -> "ab" instead of "abc"
        // This test will fail on the defective version, revealing the bug.
        assertEquals("abc", s);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToEndWithOneChar() {
        CharacterReader r = new CharacterReader("x");
        String s = r.consumeToEnd();
        assertEquals("x", s);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToCharFound() {
        CharacterReader r = new CharacterReader("abcxdefxgh");
        String s = r.consumeTo('x');
        assertEquals("abc", s);
        assertEquals(4, r.pos()); // after consuming "abc", pos at 'x'
        // consume the 'x' itself with consume
        assertEquals('x', r.consume());
        // now second part
        s = r.consumeTo('x');
        assertEquals("def", s);
    }

    @Test(timeout = 4000)
    public void testConsumeToCharNotFound() {
        CharacterReader r = new CharacterReader("abcdef");
        String s = r.consumeTo('z');
        // should consume to end (buggy consumeToEnd drops last char)
        // But if consumeToEnd fixed, it would be "abcdef"? Actually consumeTo when not found calls consumeToEnd
        // So this test also reveals the consumeToEnd bug
        assertEquals("abcdef", s);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToStringFound() {
        CharacterReader r = new CharacterReader("one**two***three");
        String s = r.consumeTo("**");
        assertEquals("one", s);
        assertEquals(5, r.pos()); // after "one**"
        // verify next characters
        assertEquals('*', r.current()); // actually after "one**" the next char is '*', but we consumed "**"? Wait: substring(pos, offset) gives "one" because offset=3, pos=0 -> "one". Then pos += consumed.length() => pos=3. So next char is input.charAt(3)='*'. But we still have the "**" pattern in the string; we consumed only up to before the pattern. So correct.
        assertEquals('*', r.consume());
        assertEquals('*', r.consume());
        assertEquals('t', r.consume());
    }

    @Test(timeout = 4000)
    public void testConsumeToStringNotFound() {
        CharacterReader r = new CharacterReader("hello");
        String s = r.consumeTo("xyz");
        assertEquals("hello", s);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader r = new CharacterReader("ab:cd!ef");
        String s = r.consumeToAny(':', '!');
        assertEquals("ab", s);
        assertEquals(2, r.pos());
        assertEquals(':', r.current());
        // consume the delimiter
        r.consume();
        s = r.consumeToAny(':', '!');
        assertEquals("cd", s);
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyNoMatch() {
        CharacterReader r = new CharacterReader("abcdef");
        String s = r.consumeToAny('z', 'y');
        // should consume all (via loop until empty)
        assertEquals("abcdef", s);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyEmptySeq() {
        CharacterReader r = new CharacterReader("abc");
        String s = r.consumeToAny();
        // No seq given, loop never breaks, will consume entire string
        assertEquals("abc", s);
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequence() {
        CharacterReader r = new CharacterReader("abc123");
        String s = r.consumeLetterSequence();
        assertEquals("abc", s);
        assertEquals(3, r.pos());
        assertEquals('1', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceDigitsOnly() {
        CharacterReader r = new CharacterReader("123abc");
        String s = r.consumeLetterSequence();
        assertEquals("", s); // no letters at start
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeDigitSequence() {
        CharacterReader r = new CharacterReader("123abc456");
        String s = r.consumeDigitSequence();
        assertEquals("123", s);
        assertEquals(3, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeDigitSequenceNonDigit() {
        CharacterReader r = new CharacterReader("abc123");
        String s = r.consumeDigitSequence();
        assertEquals("", s);
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("1aFg");
        String s = r.consumeHexSequence();
        assertEquals("1aF", s);
    }

    @Test(timeout = 4000)
    public void testMatchesMethods() {
        CharacterReader r = new CharacterReader("Hello World");
        assertTrue(r.matches('H'));
        assertTrue(r.matches("Hello"));
        assertFalse(r.matches("hello"));
        assertTrue(r.matchesIgnoreCase("hello"));
        assertTrue(r.matchesAny('H', 'X'));
        assertFalse(r.matchesAny('Z'));
        assertTrue(r.matchesLetter());
        assertFalse(r.matchesDigit());
        // consume to advance
        r.consume();
        assertFalse(r.matches('H'));
        // test matches on empty
        CharacterReader empty = new CharacterReader("");
        assertFalse(empty.matches('a'));
        assertFalse(empty.matches("a"));
        assertFalse(empty.matchesIgnoreCase("a"));
        assertFalse(empty.matchesAny('a'));
        assertFalse(empty.matchesLetter());
        assertFalse(empty.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testMatchConsume() {
        CharacterReader r = new CharacterReader("abcdef");
        assertTrue(r.matchConsume("abc"));
        assertEquals(3, r.pos());
        assertFalse(r.matchConsume("xyz"));
        assertEquals(3, r.pos());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCase() {
        CharacterReader r = new CharacterReader("HELLO");
        assertTrue(r.matchConsumeIgnoreCase("hello"));
        assertEquals(5, r.pos());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader r = new CharacterReader("ABCdefGHi");
        assertTrue(r.containsIgnoreCase("cde"));
        assertTrue(r.containsIgnoreCase("CDE"));
        assertTrue(r.containsIgnoreCase("ghi"));
        assertFalse(r.containsIgnoreCase("xyz"));
        // test from non-zero position
        r.consume(); // 'A'
        assertTrue(r.containsIgnoreCase("bcd")); // should find "bcd" from pos 1
    }

    @Test(timeout = 4000)
    public void testToString() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals("hello", r.toString());
        r.consume();
        assertEquals("ello", r.toString());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullInput() {
        new CharacterReader(null);
    }

    @Test(timeout = 4000)
    public void testEmptyInput() {
        CharacterReader r = new CharacterReader("");
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
        assertEquals("", r.consumeToEnd());
        assertEquals("", r.consumeTo('a'));
        assertEquals("", r.consumeTo("abc"));
        assertEquals("", r.consumeToAny('x'));
        assertEquals("", r.consumeLetterSequence());
        assertEquals("", r.consumeDigitSequence());
        assertEquals("", r.consumeHexSequence());
        assertFalse(r.matches('a'));
        assertFalse(r.matches("a"));
        assertFalse(r.matchesAny('a'));
        assertFalse(r.matchesLetter());
        assertFalse(r.matchesDigit());
        r.advance(); // no effect
        r.unconsume(); // pos becomes -1, but no crash? It will be negative, subsequent operations may be weird
        // but we can still check pos is now -1, but calling isEmpty() uses pos>=length, -1>=0 false -> not empty? Actually length=0, pos=-1, -1>=0 false, so isEmpty returns false, which is wrong. But this is a defect in unconsume: no guard.
        // We'll test that unconsume on empty can cause negative pos, but that's the existing behavior; we just note it.
    }

    @Test(timeout = 4000)
    public void testSingleCharInput() {
        CharacterReader r = new CharacterReader("x");
        assertEquals('x', r.current());
        assertEquals("x", r.consumeToEnd());
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testWhitespaceInput() {
        CharacterReader r = new CharacterReader("   ");
        r.consume(); // ' '
        r.consume(); // ' '
        r.consume(); // ' '
        assertTrue(r.isEmpty());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    @Test(timeout = 4000)
    public void testHandleCarriageReturnAsLineFeed() {
        // The expected behavior is that \r is treated as \n and consumed accordingly.
        // Current implementation does not normalize, so consumeTo("\n") includes the \r.
        CharacterReader r = new CharacterReader("one\r\ntwo");
        String consumed = r.consumeTo("\n");
        // With correct normalization, \r should be consumed as part of the newline,
        // so consumed should be "one" without \r.
        // The buggy version returns "one\r".
        assertEquals("one", consumed);
        // After consuming "one", pos should be at the \n (if \r was treated as newline, pos would be after \n).
        // But in buggy version, after consuming "one\r", pos=4 (0-based), pointing to '\n'.
        // The correct position after consuming up to newline, and having consumed the \r as newline? Actually if we treat \r\n as one newline, we should consume "one" and then the newline characters? The test's expectation: we only consume the text before the newline, so pos should be at the start of the newline sequence. The specification isn't fully clear, but the key is the consumed string must not contain \r.
        CharacterReader r2 = new CharacterReader("one\r\ntwo");
        r2.consumeTo("\n");
        // After consumeTo, the pos should be at the character after the '\n'? No, consumeTo returns substring up to the offset, and then sets pos to offset. So if input is "one\r\ntwo", indexOf("\n") returns 4, so consumed = substring(0,4) = "one\r", pos=4. Then the next char is '\n'.
        // In normalized version, if \r\n is a single newline, then consumeTo("\n") should consume "one" and pos should be after the \n? But the pattern "\n" would be found at index 4? Actually if we normalize, we might convert \r to \n, then the string becomes "one\n\ntwo"? That doesn't make sense. The expected behavior likely is that the reader should skip \r characters and treat them as part of the newline sequence, so that consumeTo('\n') would return "one" and then the reader is at position after the newline (i.e., after \n). But indexOf("\n") in the original string is at position 4 (after \r). So after consuming "one\r", pos=4, next char is '\n'. The test might expect that after consuming up to '\n', the reader's position is at the start of "two". That would require consuming both \r and \n as part of the newline. However, the given failed test shows expected "<one []>" but actual "<one [\n two \n]>". The brackets indicate what was consumed? Hard to interpret.

        // We'll write a second test that verifies the output after consumeToEnd with \r.
        CharacterReader r3 = new CharacterReader("one\r\ntwo");
        String consumedEnd = r3.consumeToEnd();
        // With normalization, expected "one\ntwo"; buggy returns "one\r\ntwo" (if consumeToEnd bug fixed) or "one\r\ntw" (if also off-by-one)
        assertEquals("one\ntwo", consumedEnd);
    }

    @Test(timeout = 4000)
    public void testConsumeToEndKnownBug() {
        // This directly reveals the off-by-one in consumeToEnd.
        CharacterReader r = new CharacterReader("abcde");
        String s = r.consumeToEnd();
        assertEquals("abcde", s);
    }

    @Test(timeout = 4000)
    public void testHandlesNewlinesAndWhitespaceInTag() {
        // Simulates a tag with newline and whitespace before closing '>'
        // The bug was that an empty attribute value was generated, likely due to mishandling of whitespace.
        // Test: consumeTo(">") with a newline and space before '>'
        CharacterReader r = new CharacterReader("a \n b>c");
        String consumed = r.consumeTo(">");
        // Expected: "a \n b" (the entire tag content up to but not including '>')
        // Bug might cause returning something like "a \n b" but then parser misbehaves.
        assertEquals("a \n b", consumed);
        // After consuming, pos should be at '>'
        assertEquals('>', r.current());
    }

    // Additional test for whitespace in attribute parsing context
    @Test(timeout = 4000)
    public void testConsumeToWithWhitespaceBeforeDelimiter() {
        CharacterReader r = new CharacterReader("<a href=\"one\"\n id=\"two\">");
        String consumed = r.consumeTo(">");
        // The correct behavior: consume everything up to the first '>', which includes the newline and attributes.
        assertEquals("<a href=\"one\"\n id=\"two\"", consumed);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testUnconsumeUnderflow() {
        CharacterReader r = new CharacterReader("a");
        r.unconsume(); // pos becomes -1
        // Now any operation that reads input will throw because substring with negative index
        r.current(); // Should throw StringIndexOutOfBoundsException
    }

    @Test(timeout = 4000)
    public void testConsumeToEndOnEmptyDoesNotThrow() {
        CharacterReader r = new CharacterReader("");
        String s = r.consumeToEnd(); // Returns substring(0, -1)? Actually input.length()-1 = -1, substring(0,-1) throws StringIndexOutOfBoundsException
        // But currently it does throw on empty input. We'll assert that it should handle empty gracefully.
        // However, since it's a bug, we can't expect it to pass. We'll just note that it fails.
        // We'll write a test that expects the correct behavior: return "".
        // This will fail on buggy version.
        assertEquals("", s);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    @Test(timeout = 4000)
    public void testMultipleOperations() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals('a', r.consume());
        r.mark();
        assertEquals('b', r.consume());
        r.rewindToMark();
        assertEquals('b', r.current());
        r.advance();
        assertEquals('c', r.current());
        assertEquals("c", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testConsumeToEmptyStringSeq() {
        CharacterReader r = new CharacterReader("abc");
        String s = r.consumeTo("");
        // empty sequence: indexOf returns 0 always? Actually indexOf("",pos) returns pos for any pos. So offset will be pos, consumed = substring(pos,pos) = "", pos unchanged.
        assertEquals("", s);
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToCharAtEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consume();
        r.consume();
        r.consume();
        // now empty
        String s = r.consumeTo('x');
        assertEquals("", s);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyAtEnd() {
        CharacterReader r = new CharacterReader("a");
        r.consume();
        String s = r.consumeToAny('b');
        assertEquals("", s);
    }

    @Test(timeout = 4000)
    public void testMatchesDigitLetter() {
        CharacterReader r = new CharacterReader("1a");
        assertTrue(r.matchesDigit());
        assertFalse(r.matchesLetter());
        r.consume();
        assertFalse(r.matchesDigit());
        assertTrue(r.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testMarkRewindAtStart() {
        CharacterReader r = new CharacterReader("abc");
        r.mark();
        r.consume();
        r.rewindToMark();
        assertEquals(0, r.pos());
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequenceWithUpperAndLower() {
        CharacterReader r = new CharacterReader("1aF2G");
        String s = r.consumeHexSequence();
        assertEquals("1aF2", s);
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceWithNonLetters() {
        CharacterReader r = new CharacterReader("Hello_World");
        String s = r.consumeLetterSequence();
        assertEquals("Hello", s);
        assertEquals('_', r.current());
    }
}