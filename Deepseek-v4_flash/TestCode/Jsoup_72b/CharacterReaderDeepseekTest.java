package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;

public class CharacterReaderDeepseekTest {
    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: CharacterReader (org.jsoup.parser)
     * Known Defect: StringIndexOutOfBoundsException when consuming to a non‑existent character
     * after the reader has advanced past the end of the buffer.
     * 
     * Key branches and boundaries:
     * - bufferUp(): bufPos ≥ bufSplitPoint triggers buffer refill; edge at bufLength == readAheadLimit
     * - isEmpty(): bufPos >= bufLength (including when bufPos > bufLength due to unguarded advance())
     * - current() / consume(): handle EOF correctly when buffer exhausted
     * - advance(): no bounds check, can push bufPos beyond bufLength
     * - nextIndexOf(char/CharSequence): iterates from bufPos to bufLength
     * - consumeTo(char): calls consumeToEnd() when not found – bug triggers if bufPos > bufLength
     * - consumeToEnd(): uses bufLength - bufPos, which can be negative leading to StringIndexOutOfBoundsException
     * - cacheString(): count > maxStringCacheLen (12) bypasses cache; hash index uses & (length-1)
     * - matches* methods: rely on bufPos < bufLength; overshoot leads to false
     * 
     * Partition A: Core state transitions & sequence consumption
     * Partition B: Boundary values (empty input, max cache length, buffer refill thresholds)
     * Partition C: Defect‑specific scenario (advance past end then consumeTo non‑existent)
     * Partition D: Exception path (null reader, unsupported mark, illegal arguments)
     * Partition E: Contract methods (toString, rangeEquals)
     */

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void initialStateAndPos() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void consumeAdvancesPos() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
        assertEquals('b', r.consume());
        assertEquals('c', r.consume());
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
    }

    @Test(timeout = 4000)
    public void unconsumeReverses() {
        CharacterReader r = new CharacterReader("ab");
        r.consume();
        r.unconsume();
        assertEquals(0, r.pos());
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void advanceAndMarkRewind() {
        CharacterReader r = new CharacterReader("hello world");
        r.advance(); // h -> position 1
        r.mark();
        r.advance(); // e -> position 2
        r.rewindToMark();
        assertEquals(1, r.pos());
        assertEquals('e', r.current()); // because pos=1 is 'e'
    }

    @Test(timeout = 4000)
    public void nextIndexOfCharFound() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals(1, r.nextIndexOf('e'));
        assertEquals(0, r.nextIndexOf('h'));
        assertEquals(4, r.nextIndexOf('o'));
    }

    @Test(timeout = 4000)
    public void nextIndexOfCharNotFound() {
        CharacterReader r = new CharacterReader("hello");
        assertEquals(-1, r.nextIndexOf('x'));
        r.consumeToEnd(); // move to end
        assertEquals(-1, r.nextIndexOf('h'));
    }

    @Test(timeout = 4000)
    public void nextIndexOfSequence() {
        CharacterReader r = new CharacterReader("abcdef");
        assertEquals(2, r.nextIndexOf("cd"));
        assertEquals(0, r.nextIndexOf("abc"));
        assertEquals(-1, r.nextIndexOf("xyz"));
    }

    @Test(timeout = 4000)
    public void consumeToCharFound() {
        CharacterReader r = new CharacterReader("a<bc");
        assertEquals("a", r.consumeTo('<'));
        assertEquals(1, r.pos());
        assertEquals('<', r.current());
    }

    @Test(timeout = 4000)
    public void consumeToCharNotFound() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("abc", r.consumeTo('x'));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void consumeToString() {
        CharacterReader r = new CharacterReader("ab<cd>ef");
        assertEquals("ab", r.consumeTo("<"));
        assertEquals("<", r.consumeTo(">"));
        assertEquals("cd", r.consumeTo(">"));
    }

    @Test(timeout = 4000)
    public void consumeToAny() {
        CharacterReader r = new CharacterReader("a&b<c");
        assertEquals("a", r.consumeToAny('&', '<'));
        assertEquals('&', r.consume());
        assertEquals("b", r.consumeToAny('<', '&'));
        assertEquals('<', r.consume());
        assertEquals("c", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void consumeToAnySorted() {
        CharacterReader r = new CharacterReader("a< b>c");
        // sorted chars: '<', '>', ' '
        assertEquals("a", r.consumeToAnySorted('<', '>', ' '));
        assertEquals('<', r.current());
    }

    @Test(timeout = 4000)
    public void consumeData() {
        CharacterReader r = new CharacterReader("a&amp;b<");
        assertEquals("a", r.consumeData()); // stops at &
        assertEquals('&', r.consume());
    }

    @Test(timeout = 4000)
    public void consumeTagName() {
        CharacterReader r = new CharacterReader("div.class");
        assertEquals("div", r.consumeTagName()); // stops at '.'
        assertEquals('.', r.consume());
    }

    @Test(timeout = 4000)
    public void consumeLetterSequence() {
        CharacterReader r = new CharacterReader("abc123");
        assertEquals("abc", r.consumeLetterSequence());
        assertEquals('1', r.current());
    }

    @Test(timeout = 4000)
    public void consumeLetterThenDigitSequence() {
        CharacterReader r = new CharacterReader("ab12c");
        assertEquals("ab12", r.consumeLetterThenDigitSequence());
        assertEquals('c', r.current());
    }

    @Test(timeout = 4000)
    public void consumeHexSequence() {
        CharacterReader r = new CharacterReader("aBc123g");
        assertEquals("aBc123", r.consumeHexSequence());
        assertEquals('g', r.current());
    }

    @Test(timeout = 4000)
    public void consumeDigitSequence() {
        CharacterReader r = new CharacterReader("123abc");
        assertEquals("123", r.consumeDigitSequence());
        assertEquals('a', r.current());
    }

    @Test(timeout = 4000)
    public void matchesMethods() {
        CharacterReader r = new CharacterReader("Hello");
        assertTrue(r.matches('H'));
        assertTrue(r.matches("He"));
        assertFalse(r.matches("Hi"));
        assertTrue(r.matchesIgnoreCase("hello"));
        assertTrue(r.matchesAny('H', 'e'));
        assertFalse(r.matchesAny('x', 'y'));
        assertTrue(r.matchesLetter());
        assertFalse(r.matchesDigit());
    }

    @Test(timeout = 4000)
    public void matchConsume() {
        CharacterReader r = new CharacterReader("test");
        assertTrue(r.matchConsume("te"));
        assertEquals(2, r.pos());
        assertTrue(r.matchConsume("st"));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void matchConsumeIgnoreCase() {
        CharacterReader r = new CharacterReader("Hello");
        assertTrue(r.matchConsumeIgnoreCase("hello"));
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void containsIgnoreCase() {
        CharacterReader r = new CharacterReader("Hello World");
        assertTrue(r.containsIgnoreCase("world"));
        assertFalse(r.containsIgnoreCase("earth"));
    }

    @Test(timeout = 4000)
    public void consumeToEnd() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("abc", r.consumeToEnd());
        assertTrue(r.isEmpty());
        assertEquals("", r.consumeToEnd());
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void emptyInput() {
        CharacterReader r = new CharacterReader("");
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals("", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void cacheBoundaryUnder12() {
        CharacterReader r = new CharacterReader("12345678901"); // 11 chars
        String consumed = r.consumeToEnd();
        assertEquals("12345678901", consumed);
        // cached, so same reference? Not required, but value correct
    }

    @Test(timeout = 4000)
    public void cacheBoundaryExactly12() {
        CharacterReader r = new CharacterReader("123456789012"); // 12 chars
        String consumed = r.consumeToEnd();
        assertEquals("123456789012", consumed);
    }

    @Test(timeout = 4000)
    public void cacheBoundaryOver12() {
        CharacterReader r = new CharacterReader("1234567890123"); // 13 chars -> bypass cache? actually check: count=13 >12 so new string
        String consumed = r.consumeToEnd();
        assertEquals("1234567890123", consumed);
    }

    @Test(timeout = 4000)
    public void bufferRefillTrigger() {
        // Use a large input to force multiple buffer fills
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            sb.append('a');
        }
        String large = sb.toString();
        CharacterReader r = new CharacterReader(large);
        for (int i = 0; i < 50000; i++) {
            assertEquals('a', r.consume());
        }
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void posAfterBufferRefill() {
        // Verify that pos() is correctly maintained after bufferUp
        CharacterReader r = new CharacterReader("abcdefghijklmnopqrstuvwxyz");
        for (int i = 0; i < 10; i++) r.consume(); // now at position 10
        assertEquals(10, r.pos());
        // internal buffer will be refilled when bufPos >= bufSplitPoint (bufSplitPoint = min(bufLength, readAheadLimit))
        // readAheadLimit = 0.75*32768=24576, so no refill for this small input; still good coverage
        for (int i = 0; i < 10; i++) r.advance();
        assertEquals(20, r.pos());
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone – the known bug
    // ========================================================================

    @Test(timeout = 4000)
    public void consumeToNonexistentEndWhenAtAnd() {
        // This targets the defect that caused StringIndexOutOfBoundsException
        CharacterReader r = new CharacterReader("abc");
        // Advance past end (bufPos beyond bufLength)
        r.advance(); // to 1
        r.advance(); // to 2
        r.advance(); // to 3 (end)
        r.advance(); // to 4 (bufPos = 4, bufLength = 3, isEmpty() true)
        // Now call consumeTo with a char not present – it will go to consumeToEnd()
        // In the defective version this throws StringIndexOutOfBoundsException
        String result = r.consumeTo('x');
        // Correct behavior: returns empty string (no characters left)
        assertEquals("", result);
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void consumeToEndWhenOvershot() {
        // Similar scenario: after overshoot, consumeToEnd should handle gracefully
        CharacterReader r = new CharacterReader("abc");
        r.advance();
        r.advance();
        r.advance();
        r.advance();
        String result = r.consumeToEnd();
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void consumeWithOvershoot() {
        // consume() also increments bufPos, but it returns EOF when empty.
        CharacterReader r = new CharacterReader("ab");
        r.consume();
        r.consume();
        assertEquals(CharacterReader.EOF, r.consume()); // after end, returns EOF but increments bufPos
        // Now bufPos = 3, bufLength = 2
        // Calling consumeTo again should not throw
        assertEquals(CharacterReader.EOF, r.current());
        assertTrue(r.isEmpty());
    }

    @Test(timeout = 4000)
    public void matchesAfterOvershoot() {
        CharacterReader r = new CharacterReader("a");
        r.advance();
        r.advance();
        assertFalse(r.matches('a'));
        assertFalse(r.matches("any"));
        assertFalse(r.matchesAny('a'));
        assertFalse(r.matchesLetter());
        assertFalse(r.matchesDigit());
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void nullReaderConstructor() {
        new CharacterReader(null, 1024);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void readerWithoutMarkSupport() {
        // A reader that does not support mark should throw
        Reader noMarkReader = new StringReader("test") {
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new CharacterReader(noMarkReader, 1024);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void nullReaderSingleArgConstructor() {
        new CharacterReader((Reader) null);
    }

    @Test(timeout = 4000)
    public void constructorWithSmallBuffer() {
        // sz smaller than maxBufferLen, buffer should be sz
        CharacterReader r = new CharacterReader(new StringReader("hello"), 2);
        // should still work
        assertEquals("hello", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void constructorWithOversizedBuffer() {
        // sz > maxBufferLen, buffer truncated to maxBufferLen
        CharacterReader r = new CharacterReader(new StringReader("test"), 100000);
        assertEquals("test", r.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void unconsumeAtStart() {
        CharacterReader r = new CharacterReader("abc");
        r.unconsume(); // unconsume when bufPos = 0
        // Should not throw, maybe bufPos becomes -1
        // Actually, bufPos-- is decremented; but then pos() becomes readerPos + (-1) which is negative.
        // This is a potential bug, but we test behavior
        // The code does not guard against it.
        // We'll just ensure no exception during execution
        // After unconsume, bufPos is -1, so subsequent bufferUp may cause issues, but we don't rely on that.
        // This is an edge case but not part of the known defect.
    }

    @Test(timeout = 4000)
    public void rangeEqualsMethod() {
        // Test static rangeEquals
        char[] buf = "hello".toCharArray();
        assertTrue(CharacterReader.rangeEquals(buf, 0, 5, "hello"));
        assertFalse(CharacterReader.rangeEquals(buf, 0, 5, "world"));
        assertFalse(CharacterReader.rangeEquals(buf, 0, 4, "hallo")); // different
        assertTrue(CharacterReader.rangeEquals(buf, 1, 4, "ello"));
        // count different
        assertFalse(CharacterReader.rangeEquals(buf, 0, 4, "hello"));
    }

    @Test(timeout = 4000)
    public void instanceRangeEquals() {
        CharacterReader r = new CharacterReader("hello");
        assertTrue(r.rangeEquals(0, 5, "hello"));
        assertFalse(r.rangeEquals(0, 5, "world"));
        assertTrue(r.rangeEquals(1, 4, "ello"));
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================

    @Test(timeout = 4000)
    public void toStringMethod() {
        CharacterReader r = new CharacterReader("hello world");
        assertEquals("hello world", r.toString());
        r.consumeTo(' ');
        assertEquals(" world", r.toString());
    }

    @Test(timeout = 4000)
    public void toStringAfterEnd() {
        CharacterReader r = new CharacterReader("ab");
        r.consumeToEnd();
        assertEquals("", r.toString());
    }

    // Additional tests to cover edge in nextIndexOf(CharSequence) - scanning loop
    @Test(timeout = 4000)
    public void nextIndexOfSequenceNotFoundPartialMatch() {
        CharacterReader r = new CharacterReader("abacab");
        assertEquals(0, r.nextIndexOf("aba"));
        assertEquals(3, r.nextIndexOf("cab"));
        assertEquals(-1, r.nextIndexOf("abc"));
    }

    @Test(timeout = 4000)
    public void nextIndexOfSequenceAtEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consumeToEnd();
        assertEquals(-1, r.nextIndexOf("abc"));
    }

    @Test(timeout = 4000)
    public void consumeToSequenceAtEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consumeToEnd();
        assertEquals("", r.consumeTo("def"));
    }

    @Test(timeout = 4000)
    public void consumeToAnyAfterEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consumeToEnd();
        assertEquals("", r.consumeToAny('a', 'b'));
    }

    @Test(timeout = 4000)
    public void matchesAnySortedAfterEnd() {
        CharacterReader r = new CharacterReader("abc");
        r.consumeToEnd();
        assertFalse(r.matchesAnySorted(new char[]{'a'}));
    }

    @Test(timeout = 4000)
    public void matchConsumeWhenNotMatch() {
        CharacterReader r = new CharacterReader("abc");
        assertFalse(r.matchConsume("abd"));
        assertEquals(0, r.pos());
    }

    @Test(timeout = 4000)
    public void matchConsumeIgnoreCaseWhenNotMatch() {
        CharacterReader r = new CharacterReader("abc");
        assertFalse(r.matchConsumeIgnoreCase("ABd"));
        assertEquals(0, r.pos());
    }
}