package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive white-box test suite for CharacterReader, targeting all core logic,
 * boundary conditions, and the known Defects4J defect related to entity parsing.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Branch coverage: All internal branches (isEmpty, current, consume, unconsume, 
 *   advance, mark, rewind, consumeTo(char/String/any/end), matches, matchesIgnoreCase,
 *   matchesAny, matchesLetter, matchesDigit, matchConsume, matchConsumeIgnoreCase,
 *   containsIgnoreCase, consumeLetterSequence, consumeDigitSequence, consumeHexSequence)
 * - Boundary conditions: empty input, single char, full consumption, over-consumption,
 *   null input, negative pos, very long strings, all sequences at end.
 * - Defect-targeted: Entity sequence "&sup1;&sup2;&sup3;" to verify that the reader
 *   correctly handles mixed letter-digit entity names. The bug causes mismatch in
 *   entity parsing; we test that matchesIgnoreCase and matchConsumeIgnoreCase work
 *   for such sequences, and that consumeLetterSequence does not consume digits
 *   (which may be the root cause if the tokeniser expects a combined alphanumeric method).
 */
public class CharacterReaderDeepseekTest {

    // ===================== Partition A: Core Functional Logic & State Transitions =====================

    @Test(timeout = 4000)
    public void testPosAndIsEmptyInitial() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPosAndIsEmptyAfterConsume() {
        CharacterReader r = new CharacterReader("abc");
        r.consume();
        assertEquals(1, r.pos());
        assertFalse(r.isEmpty());
        r.consume();
        r.consume();
        assertTrue(r.isEmpty());
        assertEquals(3, r.pos());
    }

    @Test(timeout = 4000)
    public void testCurrent() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals('h', r.current());
        r.consume();
        assertEquals('e', r.current());
    }

    @Test(timeout = 4000)
    public void testCurrentAtEnd() {
        CharacterReader r = new CharacterReader("x");
        r.consume();
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
    }

    @Test(timeout = 4000)
    public void testConsume() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals('a', r.consume());
        assertEquals('b', r.consume());
        assertEquals('c', r.consume());
        assertEquals(CharacterReader.EOF, r.consume());
    }

    @Test(timeout = 4000)
    public void testUnconsume() {
        CharacterReader r = new CharacterReader("abc");
        r.consume();
        assertEquals('b', r.consume());
        r.unconsume();
        assertEquals('b', r.current());
        r.consume();
        assertEquals('c', r.current());
    }

    @Test(timeout = 4000)
    public void testAdvance() {
        CharacterReader r = new CharacterReader("xyz");
        r.advance();
        assertEquals(1, r.pos());
        assertEquals('y', r.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume(); // a
        r.consume(); // b
        r.mark();
        r.consume(); // c
        r.consume(); // d
        assertEquals('e', r.current());
        r.rewindToMark();
        assertEquals('c', r.current());
        assertEquals(2, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeAsString() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("a", r.consumeAsString());
        assertEquals(1, r.pos());
        assertEquals("b", r.consumeAsString());
        assertEquals(2, r.pos());
        assertEquals("c", r.consumeAsString());
        assertEquals(3, r.pos());
        assertEquals("", r.consumeAsString()); // after end returns empty? Actually substring(pos, pos) with pos=length is fine but throws? substring(length, length) returns "".
    }

    // ===================== Partition B: Boundary Value Analysis & Extremes =====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullInput() {
        new CharacterReader(null);
    }

    @Test(timeout = 4000)
    public void testEmptyInput() {
        CharacterReader r = new CharacterReader("");
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
        assertEquals("", r.consumeTo('x'));
        assertEquals("", r.consumeToEnd());
        assertFalse(r.matches('a'));
        assertFalse(r.matches("abc"));
        assertFalse(r.matchesIgnoreCase("ABC"));
        assertFalse(r.matchesAny('a', 'b'));
        assertFalse(r.matchesLetter());
        assertFalse(r.matchesDigit());
        assertEquals("", r.consumeLetterSequence());
        assertEquals("", r.consumeDigitSequence());
        assertEquals("", r.consumeHexSequence());
        assertEquals("", r.toString());
    }

    @Test(timeout = 4000)
    public void testSingleCharacterInput() {
        CharacterReader r = new CharacterReader("Z");
        assertFalse(r.isEmpty());
        assertEquals('Z', r.current());
        assertEquals('Z', r.consume());
        assertTrue(r.isEmpty());
        assertEquals("", r.consumeTo('x'));
    }

    @Test(timeout = 4000)
    public void testFullConsumption() {
        CharacterReader r = new CharacterReader("longstring");
        while (!r.isEmpty()) {
            r.consume();
        }
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToEnd() {
        CharacterReader r = new CharacterReader("to the end");
        String all = r.consumeToEnd();
        assertEquals("to the end", all);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToCharNotFound() {
        CharacterReader r = new CharacterReader("abc");
        String consumed = r.consumeTo('z');
        assertEquals("abc", consumed);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToCharAtStart() {
        CharacterReader r = new CharacterReader("ab");
        // current = 'a', looking for 'a' at pos 0
        String consumed = r.consumeTo('a');
        assertEquals("", consumed);
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToCharAtEnd() {
        CharacterReader r = new CharacterReader("abx");
        r.consume(); r.consume(); // pos=2, char is 'x'
        String consumed = r.consumeTo('x');
        assertEquals("", consumed); // no chars between pos and x (at pos)
        assertEquals(2, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeStringNotFound() {
        CharacterReader r = new CharacterReader("abcdef");
        String consumed = r.consumeTo("notfound");
        assertEquals("abcdef", consumed);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeStringAtStart() {
        CharacterReader r = new CharacterReader("abcdef");
        String consumed = r.consumeTo("abc");
        assertEquals("", consumed);
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyEmptySeq() {
        CharacterReader r = new CharacterReader("hello");
        String consumed = r.consumeToAny(); // no seek characters
        assertEquals("hello", consumed);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyAtCurrent() {
        CharacterReader r = new CharacterReader("abc");
        String consumed = r.consumeToAny('a');
        assertEquals("", consumed);
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyMatchInMiddle() {
        CharacterReader r = new CharacterReader("abcdef");
        String consumed = r.consumeToAny('c', 'd');
        assertEquals("ab", consumed);
        assertEquals(2, r.pos());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyNoMatch() {
        CharacterReader r = new CharacterReader("xyz");
        String consumed = r.consumeToAny('a', 'b');
        assertEquals("xyz", consumed);
        assertTrue(r.isEmpty());
    }

    // ===================== Partition C: Defect-Targeted Branch Zone =====================

    @Test(timeout = 4000)
    public void testEntitySequenceSup() {
        // This test directly targets the known Defects4J bug where entity names containing
        // digits (e.g., &sup1;) are not handled correctly. The bug likely lies in the tokeniser,
        // but we verify the reader's basic behavior for such sequences.
        // Input: "sup1" (a typical entity name suffix with digits)
        CharacterReader r = new CharacterReader("sup1;");
        // Test matchesIgnoreCase on the full name
        assertTrue("matchesIgnoreCase should recognize 'sup1'", r.matchesIgnoreCase("sup1"));
        assertTrue("matchConsumeIgnoreCase should consume 'sup1'", r.matchConsumeIgnoreCase("sup1"));
        assertEquals(';', r.current());
        assertEquals(4, r.pos());

        // Also test that consumeLetterSequence stops at the digit '1'
        CharacterReader r2 = new CharacterReader("sup1");
        String letters = r2.consumeLetterSequence();
        assertEquals("sup", letters);
        assertEquals('1', r2.current());
        // Then consume digit sequence
        String digits = r2.consumeDigitSequence();
        assertEquals("1", digits);
        assertTrue(r2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEntitySequenceDegenerate() {
        // Test a degenerate case: "frac14" - letters then digits
        CharacterReader r = new CharacterReader("frac14;");
        assertTrue(r.matchesIgnoreCase("frac14"));
        assertTrue(r.matchConsumeIgnoreCase("frac14"));
        assertEquals(';', r.current());
        assertEquals(6, r.pos());

        // Using letterSequence + digitSequence
        CharacterReader r2 = new CharacterReader("frac14");
        assertEquals("frac", r2.consumeLetterSequence());
        assertEquals("14", r2.consumeDigitSequence());
        assertTrue(r2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatchesIgnoreCaseMixedCaseAndDigits() {
        CharacterReader r = new CharacterReader("Sup1");
        assertTrue(r.matchesIgnoreCase("sup1"));
        assertTrue(r.matchConsumeIgnoreCase("sup1"));
        assertTrue(r.isEmpty());

        CharacterReader r2 = new CharacterReader("SUP1");
        assertTrue(r2.matchesIgnoreCase("sup1"));
        assertTrue(r2.matchConsumeIgnoreCase("sup1"));
        assertTrue(r2.isEmpty());

        CharacterReader r3 = new CharacterReader("sup1");
        assertTrue(r3.matchesIgnoreCase("SUp1"));
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCasePartial() {
        CharacterReader r = new CharacterReader("sup1extra");
        assertTrue(r.matchConsumeIgnoreCase("sup1"));
        assertEquals('e', r.current());
        assertEquals("extra", r.consumeToEnd());
    }

    // ===================== Partition D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000)
    public void testUnconsumePastZero() {
        CharacterReader r = new CharacterReader("abc");
        // Attempting to unconsume when pos=0 should not throw (but may produce negative pos)
        // This is a potential defect: unconsume can cause pos < 0, leading to index out of bounds.
        // We test that it doesn't throw and that subsequent operations are handled.
        r.unconsume();
        // pos is now -1; calling current() or isEmpty() will use pos as index
        // isEmpty(): pos >= length? -1 >= 0? false -> not empty. But then current() would call input.charAt(-1) -> StringIndexOutOfBoundsException.
        // This is a known bug; we can catch the exception.
        try {
            r.current();
            fail("Should have thrown StringIndexOutOfBoundsException due to negative pos");
        } catch (StringIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testConsumeAfterNegativeUnconsume() {
        CharacterReader r = new CharacterReader("abc");
        r.unconsume();
        r.consume(); // will try input.charAt(-1)
    }

    @Test(timeout = 4000)
    public void testMatchesWhenEmpty() {
        CharacterReader r = new CharacterReader("");
        assertFalse(r.matches('a'));
        assertFalse(r.matches("a"));
        assertFalse(r.matchesIgnoreCase("A"));
        assertFalse(r.matchesAny('x', 'y'));
        assertFalse(r.matchesLetter());
        assertFalse(r.matchesDigit());
        assertFalse(r.matchConsume("a"));
        assertFalse(r.matchConsumeIgnoreCase("a"));
    }

    @Test(timeout = 4000)
    public void testMatchesWithSeqLongerThanRemaining() {
        CharacterReader r = new CharacterReader("ab");
        assertFalse(r.matches("abc"));
        assertFalse(r.matchesIgnoreCase("ABC"));
        assertFalse(r.matches("abx"));
    }

    // ===================== Partition E: Object Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testToString() {
        CharacterReader r = new CharacterReader("hello world");
        assertEquals("hello world", r.toString());
        r.consume(); r.consume(); // skip "he"
        assertEquals("llo world", r.toString());
        r.consumeToEnd();
        assertEquals("", r.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesAnyWithEmptyArray() {
        CharacterReader r = new CharacterReader("a");
        assertEquals(false, r.matchesAny());
    }

    @Test(timeout = 4000)
    public void testMatchesAnyWithCurrentChar() {
        CharacterReader r = new CharacterReader("abc");
        assertTrue(r.matchesAny('a', 'b'));
        r.consume();
        assertTrue(r.matchesAny('b', 'c'));
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequenceWithDigits() {
        CharacterReader r = new CharacterReader("hello123");
        assertEquals("hello", r.consumeLetterSequence());
        assertEquals("123", r.consumeDigitSequence());
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("1a2b3c");
        assertEquals("1a2b3c", r.consumeHexSequence());
        // after that, pos = 6, empty
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequenceNonHexFollows() {
        CharacterReader r = new CharacterReader("1a2g");
        assertEquals("1a2", r.consumeHexSequence());
        assertEquals('g', r.current());
    }

    @Test(timeout = 4000)
    public void testConsumeDigitSequenceEmpty() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("", r.consumeDigitSequence());
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void testMatchesLetter() {
        CharacterReader r = new CharacterReader("A");
        assertTrue(r.matchesLetter());
        r.consume();
        assertFalse(r.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testMatchesDigit() {
        CharacterReader r = new CharacterReader("9");
        assertTrue(r.matchesDigit());
        r.consume();
        assertFalse(r.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testMatchConsumeSuccess() {
        CharacterReader r = new CharacterReader("the quick");
        assertTrue(r.matchConsume("the "));
        assertEquals(4, r.pos());
        assertFalse(r.matchConsume("not"));
        assertEquals(4, r.pos());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCaseLowerUpper() {
        CharacterReader r = new CharacterReader("HelloWorld");
        assertTrue(r.containsIgnoreCase("hello"));
        assertTrue(r.containsIgnoreCase("WORLD"));
        assertTrue(r.containsIgnoreCase("ell"));
        CharacterReader r2 = new CharacterReader("abc");
        assertFalse(r2.containsIgnoreCase("x"));
    }

    @Test(timeout = 4000)
    public void testConsumeToWithNewlines() {
        // Verify carriage return normalization
        CharacterReader r = new CharacterReader("a\r\nb");
        assertEquals("a", r.consumeTo('\n'));
        assertEquals('\n', r.current());
        r.consume();
        assertEquals('b', r.current());
    }

    @Test(timeout = 4000)
    public void testComplexSequence() {
        // Simulates parsing of nested entities
        CharacterReader r = new CharacterReader("&amp;lt;");
        assertEquals('&', r.consume());
        assertTrue(r.matchesIgnoreCase("amp"));
        r.consumeLetterSequence(); // consumes "amp"
        r.consume(); // consumes ';'
        assertEquals('l', r.consume());
        assertEquals('t', r.consume());
        assertEquals(';', r.consume());
        assertTrue(r.isEmpty());
    }
}