package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.jsoup.parser.CharacterReader
 *
 * Defect Identification (from Defects4J Ground Truth):
 * - consumeToEnd() defective implementation: uses input.substring(pos, input.length() - 1),
 *   which prematurely cuts off the last character of the input string.
 * - Calling consumeToEnd() when pos==0 on "one two three" yields "one two thre" instead of "one two three".
 * - Calling consumeTo(char) or consumeTo(String) when the delimiter is not found falls through to
 *   consumeToEnd(), thereby also dropping the final character.
 *
 * Branch Coverage Targets:
 * - Constructor: null validation (Validate.notNull)
 * - isEmpty(): pos >= length (true, false)
 * - current(): isEmpty() ? EOF : charAt(pos)
 * - consume(): isEmpty() ? EOF : charAt(pos), increments pos
 * - unconsume() / advance(): pos decrements/increments
 * - mark() / rewindToMark(): save pos, restore pos
 * - consumeAsString(): single character extraction
 * - consumeTo(char): found (offset != -1) vs not found (offset == -1 -> consumeToEnd)
 * - consumeTo(String): found (offset != -1) vs not found (offset == -1 -> consumeToEnd)
 * - consumeToAny(char...): match found at start, match found in middle, no match found till EOF
 * - consumeToEnd(): remaining string consumption from pos to length
 * - consumeLetterSequence(): [A-Z], [a-z], non-letter, empty at start
 * - consumeHexSequence(): [0-9], [A-F], [a-f], non-hex, empty at start
 * - consumeDigitSequence(): [0-9], non-digit, empty at start
 * - matches(char): isEmpty(), match, non-match
 * - matches(String): match, non-match
 * - matchesIgnoreCase(String): regionMatches case-insensitive true/false
 * - matchesAny(char...): isEmpty(), char matched in seq, no char matched
 * - matchesLetter(): isEmpty(), [A-Z], [a-z], non-letter
 * - matchesDigit(): isEmpty(), [0-9], non-digit
 * - matchConsume(String): matches -> pos += length & true, non-match -> false
 * - matchConsumeIgnoreCase(String): matches -> pos += length & true, non-match -> false
 * - containsIgnoreCase(String): loScan hit, hiScan hit, neither hit
 * - toString(): substring from pos to end
 */
public class CharacterReaderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndPositionTracking() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());

        reader.advance();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());

        char consumed = reader.consume();
        assertEquals('b', consumed);
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());

        reader.unconsume();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("testing");
        reader.advance();
        reader.advance(); // pos = 2 ('s')
        reader.mark();

        reader.consume(); // pos = 3
        reader.consume(); // pos = 4
        assertEquals(4, reader.pos());

        reader.rewindToMark();
        assertEquals(2, reader.pos());
        assertEquals('s', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("foo#bar");
        String consumed = reader.consumeTo('#');
        assertEquals("foo", consumed);
        assertEquals(3, reader.pos());
        assertEquals('#', reader.current());

        reader.consume(); // skip '#'
        String remaining = reader.consumeTo('z'); // 'z' not found
        // When not found, delegates to consumeToEnd()
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToStringSequence() {
        CharacterReader reader = new CharacterReader("foo<!--comment-->bar");
        String consumed = reader.consumeTo("<!--");
        assertEquals("foo", consumed);
        assertEquals(3, reader.pos());
        assertTrue(reader.matches("<!--"));

        reader.consumeTo("missing");
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("abc&def?ghi");
        String part1 = reader.consumeToAny('&', '?');
        assertEquals("abc", part1);
        assertEquals('&', reader.current());

        reader.consume(); // consume '&'
        String part2 = reader.consumeToAny('&', '?');
        assertEquals("def", part2);
        assertEquals('?', reader.current());

        // Immediately matching first character should return empty string
        String emptyPart = reader.consumeToAny('?');
        assertEquals("", emptyPart);

        reader.consume(); // consume '?'
        String part3 = reader.consumeToAny('x', 'y', 'z');
        assertEquals("ghi", part3);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLetterHexAndDigitSequences() {
        CharacterReader reader = new CharacterReader("Hello1234AFG");
        String letters = reader.consumeLetterSequence();
        assertEquals("Hello", letters);
        assertEquals('1', reader.current());

        String digits = reader.consumeDigitSequence();
        assertEquals("1234", digits);
        assertEquals('A', reader.current());

        // "AFG" -> AF are hex digits, G is not
        String hex = reader.consumeHexSequence();
        assertEquals("AF", hex);
        assertEquals('G', reader.current());

        // Hex sequence on lower-case hex
        CharacterReader hexReader = new CharacterReader("1a2fG");
        assertEquals("1a2f", hexReader.consumeHexSequence());
        assertEquals('G', hexReader.current());
    }

    @Test(timeout = 4000)
    public void testMatchesAndMatchConsume() {
        CharacterReader reader = new CharacterReader("Titlecase String");

        assertTrue(reader.matches('T'));
        assertFalse(reader.matches('t'));
        assertTrue(reader.matches("Title"));
        assertFalse(reader.matches("title"));

        assertTrue(reader.matchesIgnoreCase("titlecase"));
        assertFalse(reader.matchesIgnoreCase("nonmatching"));

        assertTrue(reader.matchesAny('x', 'y', 'T'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));

        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        assertFalse(reader.matchConsume("nonmatch"));
        assertEquals(0, reader.pos());

        assertTrue(reader.matchConsume("Title"));
        assertEquals(5, reader.pos());
        assertEquals("case String", reader.toString());

        assertTrue(reader.matchConsumeIgnoreCase("CASE"));
        assertEquals(9, reader.pos());
        assertEquals(" String", reader.toString());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("abcdef</STYLE>xyz");
        assertTrue(reader.containsIgnoreCase("</style>"));
        assertTrue(reader.containsIgnoreCase("</STYLE>"));
        assertFalse(reader.containsIgnoreCase("</title>"));

        reader.consumeTo('<');
        assertTrue(reader.containsIgnoreCase("</STYLE>"));
        reader.advance();
        assertFalse(reader.containsIgnoreCase("abcdef"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyReaderBoundaries() {
        CharacterReader emptyReader = new CharacterReader("");

        assertTrue(emptyReader.isEmpty());
        assertEquals(0, emptyReader.pos());
        assertEquals(CharacterReader.EOF, emptyReader.current());
        assertEquals(CharacterReader.EOF, emptyReader.consume());
        assertEquals(1, emptyReader.pos());
        assertTrue(emptyReader.isEmpty());

        emptyReader.unconsume();
        assertEquals(0, emptyReader.pos());

        assertFalse(emptyReader.matches('a'));
        assertFalse(emptyReader.matches("a"));
        assertFalse(emptyReader.matchesIgnoreCase("a"));
        assertFalse(emptyReader.matchesAny('a', 'b'));
        assertFalse(emptyReader.matchesLetter());
        assertFalse(emptyReader.matchesDigit());

        assertEquals("", emptyReader.consumeLetterSequence());
        assertEquals("", emptyReader.consumeHexSequence());
        assertEquals("", emptyReader.consumeDigitSequence());
        assertEquals("", emptyReader.consumeToAny('a', 'b'));
        assertEquals("", emptyReader.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterBoundaryChecks() {
        // Test boundary values for letters: @ (64), A (65), Z (90), [ (91), ` (96), a (97), z (122), { (123)
        CharacterReader r1 = new CharacterReader("@A[");
        assertFalse(r1.matchesLetter());
        r1.advance();
        assertTrue(r1.matchesLetter());
        r1.advance();
        assertFalse(r1.matchesLetter());

        CharacterReader r2 = new CharacterReader("`a{");
        assertFalse(r2.matchesLetter());
        r2.advance();
        assertTrue(r2.matchesLetter());
        r2.advance();
        assertFalse(r2.matchesLetter());

        // Test boundary values for digits: / (47), 0 (48), 9 (57), : (58)
        CharacterReader r3 = new CharacterReader("/09:");
        assertFalse(r3.matchesDigit());
        r3.advance();
        assertTrue(r3.matchesDigit());
        r3.advance();
        assertTrue(r3.matchesDigit());
        r3.advance();
        assertFalse(r3.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testHexSequenceBoundaries() {
        // Hex allows 0-9, A-F, a-f
        CharacterReader r = new CharacterReader("09AFafGg");
        assertEquals("09AFaf", r.consumeHexSequence());
        assertEquals('G', r.current());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETED DEFECT:
     * In defective CharacterReader:
     * String consumeToEnd() {
     *     String data = input.substring(pos, input.length() - 1);
     *     pos = input.length();
     *     return data;
     * }
     *
     * It erroneously drops the final character!
     * Expected: "one two three"
     * Actual in buggy code: "one two thre"
     */
    @Test(timeout = 4000)
    public void testConsumeToEndDoesNotDropLastChar() {
        CharacterReader reader = new CharacterReader("one two three");
        String consumed = reader.consumeToEnd();
        assertEquals("one two three", consumed);
        assertEquals(13, reader.pos());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToEndFromOffsetDoesNotDropLastChar() {
        CharacterReader reader = new CharacterReader("prefix: data to the end!");
        reader.consumeTo(' ');
        reader.advance(); // skip ' '
        String consumed = reader.consumeToEnd();
        assertEquals("data to the end!", consumed);
    }

    @Test(timeout = 4000)
    public void testConsumeToCharNotFoundDoesNotDropLastChar() {
        CharacterReader reader = new CharacterReader("hello world");
        String consumed = reader.consumeTo('z'); // 'z' not in input
        assertEquals("hello world", consumed);
    }

    @Test(timeout = 4000)
    public void testConsumeToStringNotFoundDoesNotDropLastChar() {
        CharacterReader reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo("xyz"); // "xyz" not in input
        assertEquals("abcdef", consumed);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullInputThrowsException() {
        new CharacterReader(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & String Representation
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringContract() {
        CharacterReader reader = new CharacterReader("remaining content");
        assertEquals("remaining content", reader.toString());

        reader.consumeTo(' ');
        assertEquals(" remaining content".substring(1), reader.toString());

        reader.advance();
        assertEquals("content", reader.toString());

        reader.consumeToEnd();
        assertEquals("", reader.toString());
    }
}