package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.parser.CharacterReader
 *
 * Branch & Boundary Coverage Matrix:
 * 1. Constructor:
 *    - null input: Validate.notNull throws IllegalArgumentException
 *    - empty input (""): length = 0, pos = 0, isEmpty = true
 * 2. Position & State:
 *    - pos(), isEmpty(), current(), consume(), unconsume(), advance(), mark(), rewindToMark()
 *    - current() and consume() when pos >= length (returns CharacterReader.EOF)
 *    - unconsume() moving pos backward; mark() storing current pos and rewindToMark() resetting pos
 *    - consumeAsString() returning single-character string and advancing pos
 * 3. nextIndexOf(char c):
 *    - Char found at current pos (offset = 0)
 *    - Char found further in input (offset > 0)
 *    - Char not found (returns -1)
 *    - Reader already empty (returns -1)
 * 4. nextIndexOf(CharSequence seq) [KNOWN DEFECT ZONE]:
 *    - Subsequence match at current pos, intermediate pos, end of input
 *    - First char of seq matches near end of input, but remaining seq length exceeds remaining buffer
 *      (Defects4J Bug: triggers java.lang.ArrayIndexOutOfBoundsException due to missing i < length check)
 *    - Sequence not matched at all; startChar matched but inner mismatch
 * 5. consumeTo(char c) & consumeTo(String seq):
 *    - Target found: returns string from pos to target, pos advanced
 *    - Target not found: delegates to consumeToEnd(), returns full remainder, pos = length
 * 6. consumeToAny(char... chars):
 *    - Target found matching one of several chars
 *    - Target not found; empty search chars
 * 7. consumeLetterSequence(), consumeLetterThenDigitSequence(), consumeHexSequence(), consumeDigitSequence():
 *    - Full matches, partial matches, leading mismatches, transitions between letters and digits
 * 8. matches(char c), matches(String seq), matchesIgnoreCase(String seq), matchesAny(char...):
 *    - Empty reader checks; length overflow checks (scanLength > length - pos)
 *    - Case variations (upper/lower mismatch and match)
 * 9. matchesLetter(), matchesDigit():
 *    - Boundaries: 'A'-'Z', 'a'-'z', '0'-'9', non-alphanumeric, EOF
 * 10. matchConsume(String seq), matchConsumeIgnoreCase(String seq):
 *     - True path: matches and advances pos by seq.length()
 *     - False path: does not match, leaves pos untouched
 * 11. containsIgnoreCase(String seq):
 *     - Found via lowercase branch, uppercase branch, or neither (-1)
 * 12. toString():
 *     - Remainder of buffer as string from pos to length
 * ====================================================================================================
 */
public class CharacterReaderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndBasicConsumption() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());

        char consumed = reader.consume();
        assertEquals('a', consumed);
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());

        reader.advance();
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());

        reader.unconsume();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // at 'b', pos = 1
        reader.mark();

        reader.consume(); // 'b', pos = 2
        reader.consume(); // 'c', pos = 3
        assertEquals(3, reader.pos());

        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("xy");
        String s1 = reader.consumeAsString();
        assertEquals("x", s1);
        assertEquals(1, reader.pos());

        String s2 = reader.consumeAsString();
        assertEquals("y", s2);
        assertEquals(2, reader.pos());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("hello world");
        reader.consume(); // consume 'h'
        String rest = reader.consumeToEnd();
        assertEquals("ello world", rest);
        assertTrue(reader.isEmpty());
        assertEquals(11, reader.pos());

        // consumeToEnd when already empty
        String emptyRest = reader.consumeToEnd();
        assertEquals("", emptyRest);
    }

    @Test(timeout = 4000)
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("foo/bar/baz");
        String part1 = reader.consumeTo('/');
        assertEquals("foo", part1);
        assertEquals('/', reader.current());

        reader.consume(); // skip '/'
        String part2 = reader.consumeTo('?'); // not found
        assertEquals("bar/baz", part2);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToString() {
        CharacterReader reader = new CharacterReader("start<!--comment-->end");
        String beforeComment = reader.consumeTo("<!--");
        assertEquals("start", beforeComment);
        assertEquals('<', reader.current());

        String comment = reader.consumeTo("-->");
        assertEquals("<!--comment", comment);

        String remainder = reader.consumeTo("nonexistent");
        assertEquals("-->end", remainder);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("abcdef");
        String part = reader.consumeToAny('x', 'c', 'z');
        assertEquals("ab", part);
        assertEquals('c', reader.current());

        // consumeToAny when none match
        String rest = reader.consumeToAny('1', '2');
        assertEquals("cdef", rest);
        assertTrue(reader.isEmpty());

        // consumeToAny when already at end
        String empty = reader.consumeToAny('a');
        assertEquals("", empty);
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("HelloWorld123");
        String letters = reader.consumeLetterSequence();
        assertEquals("HelloWorld", letters);
        assertEquals('1', reader.current());

        // Starting with non-letter
        String none = reader.consumeLetterSequence();
        assertEquals("", none);
        assertEquals('1', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("item42-tail");
        String seq = reader.consumeLetterThenDigitSequence();
        assertEquals("item42", seq);
        assertEquals('-', reader.current());

        // Only digits
        CharacterReader digitsOnly = new CharacterReader("123abc");
        String emptyLetters = digitsOnly.consumeLetterThenDigitSequence();
        assertEquals("123", emptyLetters);
        assertEquals('a', digitsOnly.current());

        // Non-letter non-digit start
        CharacterReader nonAlpha = new CharacterReader("!item42");
        assertEquals("", nonAlpha.consumeLetterThenDigitSequence());
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequence() {
        CharacterReader reader = new CharacterReader("0123456789ABCDEFabcdefGHI");
        String hex = reader.consumeHexSequence();
        assertEquals("0123456789ABCDEFabcdef", hex);
        assertEquals('G', reader.current());

        // No hex at start
        CharacterReader nonHex = new CharacterReader("XYZ");
        assertEquals("", nonHex.consumeHexSequence());
    }

    @Test(timeout = 4000)
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("12345abc");
        String digits = reader.consumeDigitSequence();
        assertEquals("12345", digits);
        assertEquals('a', reader.current());

        // No digits at start
        CharacterReader nonDigit = new CharacterReader("abc123");
        assertEquals("", nonDigit.consumeDigitSequence());
    }

    @Test(timeout = 4000)
    public void testMatchesAndMatchConsume() {
        CharacterReader reader = new CharacterReader("<html>");
        assertTrue(reader.matches('<'));
        assertFalse(reader.matches('>'));

        assertTrue(reader.matches("<html"));
        assertFalse(reader.matches("<xml"));

        assertTrue(reader.matchesIgnoreCase("<HTML>"));
        assertFalse(reader.matchesIgnoreCase("<HEAD>"));

        assertTrue(reader.matchConsume("<html"));
        assertEquals('>', reader.current());
        assertEquals(5, reader.pos());

        assertFalse(reader.matchConsume("missing"));
        assertEquals(5, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase(">"));
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatchesAny() {
        CharacterReader reader = new CharacterReader("test");
        assertTrue(reader.matchesAny('a', 'b', 't'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matchesAny('a', 'b'));
    }

    @Test(timeout = 4000)
    public void testMatchesLetterAndDigit() {
        CharacterReader letterUpper = new CharacterReader("A1");
        assertTrue(letterUpper.matchesLetter());
        assertFalse(letterUpper.matchesDigit());

        CharacterReader letterLower = new CharacterReader("z9");
        assertTrue(letterLower.matchesLetter());

        CharacterReader digit = new CharacterReader("5X");
        assertFalse(digit.matchesLetter());
        assertTrue(digit.matchesDigit());

        CharacterReader symbol = new CharacterReader("#");
        assertFalse(symbol.matchesLetter());
        assertFalse(symbol.matchesDigit());

        CharacterReader empty = new CharacterReader("");
        assertFalse(empty.matchesLetter());
        assertFalse(empty.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader reader1 = new CharacterReader("prefix</TITLE>suffix");
        assertTrue(reader1.containsIgnoreCase("</title>"));

        CharacterReader reader2 = new CharacterReader("prefix</style>suffix");
        assertTrue(reader2.containsIgnoreCase("</style>"));

        CharacterReader reader3 = new CharacterReader("no match here");
        assertFalse(reader3.containsIgnoreCase("</title>"));
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.toString());

        reader.consume();
        reader.consume();
        assertEquals("cdef", reader.toString());

        reader.consumeToEnd();
        assertEquals("", reader.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyReaderBoundaries() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(1, reader.pos());
        assertTrue(reader.isEmpty());

        assertEquals(-1, reader.nextIndexOf('a'));
        assertEquals(-1, reader.nextIndexOf("a"));
        assertFalse(reader.matches('a'));
        assertFalse(reader.matches("a"));
        assertFalse(reader.matchesIgnoreCase("a"));
        assertFalse(reader.matchConsume("a"));
        assertFalse(reader.matchConsumeIgnoreCase("a"));
        assertEquals("", reader.consumeTo('a'));
        assertEquals("", reader.consumeTo("a"));
        assertEquals("", reader.consumeToAny('a', 'b'));
        assertEquals("", reader.consumeLetterSequence());
        assertEquals("", reader.consumeLetterThenDigitSequence());
        assertEquals("", reader.consumeHexSequence());
        assertEquals("", reader.consumeDigitSequence());
        assertEquals("", reader.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesLengthExceeded() {
        CharacterReader reader = new CharacterReader("short");
        assertFalse(reader.matches("short_and_more"));
        assertFalse(reader.matchesIgnoreCase("short_and_more"));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharBoundaries() {
        CharacterReader reader = new CharacterReader("abcda");
        assertEquals(0, reader.nextIndexOf('a'));
        assertEquals(1, reader.nextIndexOf('b'));
        assertEquals(3, reader.nextIndexOf('d'));
        assertEquals(-1, reader.nextIndexOf('z'));

        reader.consume(); // pos = 1 ('b')
        assertEquals(3, reader.nextIndexOf('a')); // second 'a' is at index 4, pos is 1, offset = 3
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * TARGETED DEFECT:
     * When nextIndexOf(CharSequence seq) scans for a multi-char sequence and matches the first
     * character near the end of the input, the subsequent inner loop checks:
     * `for (int j = 1; i < last && seq.charAt(j) == input[i]; i++, j++);`
     * Defect: `i < length` boundary check is omitted, causing `input[i]` to throw
     * `java.lang.ArrayIndexOutOfBoundsException` when `last > length`.
     * This test strictly asserts correct handling (returns -1) without exception.
     */
    @Test(timeout = 4000)
    public void testNextIndexOfUnmatchedSequenceNearEOFRevealingDefect() {
        // 'c' matches at index 2 (pos=2), but seq length 4 ("cdef") extends beyond buffer length 3 ("abc")
        CharacterReader reader = new CharacterReader("abc");
        int offset = reader.nextIndexOf("cdef");
        assertEquals(-1, offset);
    }

    @Test(timeout = 4000)
    public void testHandlesUnclosedCdataAtEOFRevealingDefect() {
        // Similar to HtmlParserTest::handlesUnclosedCdataAtEOF where '<' matches at end of input
        CharacterReader reader = new CharacterReader("some text <![CDATA[");
        int offset = reader.nextIndexOf("<![CDATA[unclosed");
        assertEquals(-1, offset);
    }

    @Test(timeout = 4000)
    public void testConsumeToWithUnmatchedSequenceNearEOF() {
        CharacterReader reader = new CharacterReader("prefix<![CDATA[");
        // Should consume to end without throwing ArrayIndexOutOfBoundsException
        String consumed = reader.consumeTo("<![CDATA[missing");
        assertEquals("prefix<![CDATA[", consumed);
        assertTrue(reader.isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullGuard() {
        new CharacterReader(null);
    }

    @Test(timeout = 4000)
    public void testUnconsumeBeyondZero() {
        CharacterReader reader = new CharacterReader("a");
        reader.unconsume();
        assertEquals(-1, reader.pos());
    }

    // =========================================================================
    // Partition E: Substring and Complex Scan Path Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testNextIndexOfCharSequenceVariations() {
        CharacterReader reader = new CharacterReader("aaxaaay");

        // Start char matches but sequence fails on second char
        int idx = reader.nextIndexOf("aaz");
        assertEquals(-1, idx);

        // Sequence matches exactly further into string
        int matchIdx = reader.nextIndexOf("aaay");
        assertEquals(3, matchIdx);

        // Sequence matches immediately at pos 0
        CharacterReader reader2 = new CharacterReader("exactMatch");
        assertEquals(0, reader2.nextIndexOf("exact"));
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCaseBoundaryCases() {
        CharacterReader reader = new CharacterReader("MixedCaseTarget");

        assertFalse(reader.matchConsumeIgnoreCase("WRONG"));
        assertEquals(0, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase("mixedcase"));
        assertEquals(9, reader.pos());
        assertEquals("Target", reader.toString());

        // Target longer than remaining
        assertFalse(reader.matchConsumeIgnoreCase("TargetAndMore"));
        assertEquals(9, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase("TARGET"));
        assertTrue(reader.isEmpty());
    }
}