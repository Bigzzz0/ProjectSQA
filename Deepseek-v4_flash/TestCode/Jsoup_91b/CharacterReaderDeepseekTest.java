package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Comprehensive white-box test suite for CharacterReader.
 * Targets all methods, branches, and the known Defects4J defect related to binary content handling.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (consume, current, isEmpty, pos, advance, unconsume, mark/rewind)
 * - Partition B: Boundary Value Analysis (empty input, single char, max buffer, null char, EOF)
 * - Partition C: Defect-targeted zone (binary content with null chars, large binary-like sequences)
 * - Partition D: Exception paths (unconsume at start, mark invalid, reader skip failure)
 * - Partition E: String caching (cacheString, rangeEquals, hash collisions)
 *
 * Known defect: Parser fails to throw exception on binary content. This test ensures that
 * CharacterReader correctly identifies null characters and handles binary data without premature EOF.
 */
public class CharacterReaderDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testPosInitiallyZero() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals(0, reader.pos());
    }

    @Test(timeout = 4000)
    public void testPosAfterConsume() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume();
        assertEquals(1, reader.pos());
    }

    @Test(timeout = 4000)
    public void testIsEmptyOnEmptyString() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsEmptyOnNonEmpty() {
        CharacterReader reader = new CharacterReader("x");
        assertFalse(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsEmptyAfterConsumeAll() {
        CharacterReader reader = new CharacterReader("ab");
        reader.consume();
        reader.consume();
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCurrentReturnsEOFWhenEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test(timeout = 4000)
    public void testCurrentReturnsFirstChar() {
        CharacterReader reader = new CharacterReader("a");
        assertEquals('a', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeReturnsCharAndAdvances() {
        CharacterReader reader = new CharacterReader("ab");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test(timeout = 4000)
    public void testUnconsume() {
        CharacterReader reader = new CharacterReader("x");
        reader.consume();
        reader.unconsume();
        assertEquals('x', reader.current());
        assertEquals(0, reader.pos());
    }

    @Test(timeout = 4000)
    public void testAdvance() {
        CharacterReader reader = new CharacterReader("yz");
        reader.advance();
        assertEquals('z', reader.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewindToMark() {
        CharacterReader reader = new CharacterReader("hello world");
        reader.consume(); // h
        reader.consume(); // e
        reader.mark();
        reader.consume(); // l
        reader.consume(); // l
        reader.rewindToMark();
        assertEquals('l', reader.current()); // back to first l
    }

    @Test(timeout = 4000)
    public void testMarkAndRewindAfterBufferUp() {
        // Force bufferUp by reading a lot
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append('a');
        sb.append("target");
        CharacterReader reader = new CharacterReader(sb.toString());
        // consume many chars to trigger bufferUp
        for (int i = 0; i < 500; i++) reader.consume();
        reader.mark();
        reader.consume(); // consume one more
        reader.rewindToMark();
        assertEquals('a', reader.current());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyInputAllMethods() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals("", reader.consumeTo('x'));
        assertEquals("", reader.consumeTo("xyz"));
        assertEquals("", reader.consumeToAny('a', 'b'));
        assertEquals("", reader.consumeToEnd());
        assertEquals("", reader.consumeData());
        assertEquals("", reader.consumeTagName());
        assertEquals("", reader.consumeLetterSequence());
        assertEquals("", reader.consumeLetterThenDigitSequence());
        assertEquals("", reader.consumeHexSequence());
        assertEquals("", reader.consumeDigitSequence());
        assertFalse(reader.matches('a'));
        assertFalse(reader.matches("abc"));
        assertFalse(reader.matchesIgnoreCase("ABC"));
        assertFalse(reader.matchesAny('x', 'y'));
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertFalse(reader.matchConsume("test"));
        assertFalse(reader.matchConsumeIgnoreCase("TEST"));
        assertEquals("", reader.toString());
    }

    @Test(timeout = 4000)
    public void testSingleCharInput() {
        CharacterReader reader = new CharacterReader("Z");
        assertEquals('Z', reader.current());
        assertEquals("", reader.consumeTo('Z')); // consumeTo stops before delimiter
        assertEquals('Z', reader.consume());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMaxBufferLengthConstructor() {
        // Use a reader with size > maxBufferLen
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50000; i++) sb.append('x');
        CharacterReader reader = new CharacterReader(new StringReader(sb.toString()), 100000);
        assertFalse(reader.isEmpty());
        // consume a bit
        for (int i = 0; i < 1000; i++) reader.consume();
        assertFalse(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullCharHandling() {
        // Binary content often contains null chars
        String input = "abc\u0000def";
        CharacterReader reader = new CharacterReader(input);
        assertEquals('a', reader.current());
        String data = reader.consumeData(); // should stop at null
        assertEquals("abc", data);
        assertEquals('\u0000', reader.current());
        reader.consume(); // consume null
        assertEquals('d', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToEndWithNullChars() {
        String input = "a\u0000b\u0000c";
        CharacterReader reader = new CharacterReader(input);
        String rest = reader.consumeToEnd();
        assertEquals(input, rest);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNextIndexOfWithNullChar() {
        CharacterReader reader = new CharacterReader("ab\u0000cd");
        assertEquals(2, reader.nextIndexOf('\u0000'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfSequenceWithNull() {
        CharacterReader reader = new CharacterReader("ab\u0000cd");
        assertEquals(2, reader.nextIndexOf("\u0000c"));
    }

    @Test(timeout = 4000)
    public void testMatchesNullChar() {
        CharacterReader reader = new CharacterReader("\u0000");
        assertTrue(reader.matches('\u0000'));
    }

    @Test(timeout = 4000)
    public void testMatchesAnyWithNull() {
        CharacterReader reader = new CharacterReader("\u0000");
        assertTrue(reader.matchesAny('\u0000', 'x'));
    }

    // ==================== Partition C: Defect-Targeted Zone (Binary Content) ====================

    /**
     * This test targets the known defect where binary content (many null chars) is not properly handled.
     * The parser should throw an exception, but CharacterReader must correctly report non-empty state
     * and allow consumption of all characters including nulls.
     */
    @Test(timeout = 4000)
    public void testBinaryContentWithManyNulls() {
        // Build a string with many null characters (simulating binary)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('\u0000');
        }
        sb.append("end");
        CharacterReader reader = new CharacterReader(sb.toString());
        assertFalse(reader.isEmpty());
        // Consume all nulls
        for (int i = 0; i < 100; i++) {
            assertEquals('\u0000', reader.consume());
        }
        // Now should see 'e'
        assertEquals('e', reader.current());
        // Consume rest
        assertEquals("end", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeDataStopsAtNull() {
        // Binary content: null char should stop consumeData
        String input = "hello\u0000world";
        CharacterReader reader = new CharacterReader(input);
        String part = reader.consumeData();
        assertEquals("hello", part);
        assertEquals('\u0000', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeTagNameStopsAtNull() {
        String input = "div\u0000class";
        CharacterReader reader = new CharacterReader(input);
        String tag = reader.consumeTagName();
        assertEquals("div", tag);
        assertEquals('\u0000', reader.current());
    }

    @Test(timeout = 4000)
    public void testMatchesIgnoreCaseWithNull() {
        CharacterReader reader = new CharacterReader("\u0000abc");
        assertFalse(reader.matchesIgnoreCase("ABC"));
        reader.consume(); // skip null
        assertTrue(reader.matchesIgnoreCase("abc"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = UncheckedIOException.class, timeout = 4000)
    public void testUnconsumeAtStartThrows() {
        CharacterReader reader = new CharacterReader("a");
        reader.unconsume(); // bufPos is 0, should throw
    }

    @Test(expected = UncheckedIOException.class, timeout = 4000)
    public void testRewindToMarkWithoutMarkThrows() {
        CharacterReader reader = new CharacterReader("test");
        reader.rewindToMark(); // bufMark == -1
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullReaderThrows() {
        try {
            new CharacterReader(null, 100);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithUnsupportedMarkReader() {
        // Reader that does not support mark() should be rejected
        Reader noMark = new Reader() {
            @Override public int read(char[] cbuf, int off, int len) throws IOException { return 0; }
            @Override public void close() throws IOException {}
            @Override public boolean markSupported() { return false; }
        };
        try {
            new CharacterReader(noMark, 100);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Partition E: String Caching & RangeEquals ====================

    @Test(timeout = 4000)
    public void testCacheStringShortString() {
        // Short strings (<=12) are cached
        CharacterReader reader = new CharacterReader("hello");
        String s1 = reader.consumeToEnd();
        // Create another reader with same content to test cache
        CharacterReader reader2 = new CharacterReader("hello");
        String s2 = reader2.consumeToEnd();
        // Should be same object due to caching
        assertSame(s1, s2);
    }

    @Test(timeout = 4000)
    public void testCacheStringLongString() {
        // Long strings (>12) are not cached
        String longStr = "abcdefghijklmnop";
        CharacterReader reader = new CharacterReader(longStr);
        String s1 = reader.consumeToEnd();
        CharacterReader reader2 = new CharacterReader(longStr);
        String s2 = reader2.consumeToEnd();
        assertNotSame(s1, s2);
        assertEquals(s1, s2);
    }

    @Test(timeout = 4000)
    public void testCacheStringHashCollision() {
        // Force hash collision by using strings with same hash (unlikely but we can test logic)
        // The cache uses a simple hash; we can't easily force collision, but we can verify that
        // rangeEquals is called correctly.
        CharacterReader reader = new CharacterReader("ab");
        String s1 = reader.consumeToEnd();
        // Consume again from another reader with same content
        CharacterReader reader2 = new CharacterReader("ab");
        String s2 = reader2.consumeToEnd();
        assertSame(s1, s2);
    }

    @Test(timeout = 4000)
    public void testRangeEqualsExactMatch() {
        char[] buf = "hello".toCharArray();
        assertTrue(CharacterReader.rangeEquals(buf, 0, 5, "hello"));
    }

    @Test(timeout = 4000)
    public void testRangeEqualsMismatch() {
        char[] buf = "hello".toCharArray();
        assertFalse(CharacterReader.rangeEquals(buf, 0, 5, "world"));
    }

    @Test(timeout = 4000)
    public void testRangeEqualsDifferentLength() {
        char[] buf = "hello".toCharArray();
        assertFalse(CharacterReader.rangeEquals(buf, 0, 4, "hello"));
    }

    @Test(timeout = 4000)
    public void testRangeEqualsWithOffset() {
        char[] buf = "abcdef".toCharArray();
        assertTrue(CharacterReader.rangeEquals(buf, 2, 3, "cde"));
    }

    // ==================== Additional Coverage for consumeToAnySorted, matchesAnySorted, etc. ====================

    @Test(timeout = 4000)
    public void testConsumeToAnySorted() {
        CharacterReader reader = new CharacterReader("abc;def");
        String part = reader.consumeToAnySorted(';', '!');
        assertEquals("abc", part);
        assertEquals(';', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnySortedNoMatch() {
        CharacterReader reader = new CharacterReader("abcdef");
        String part = reader.consumeToAnySorted('x', 'y');
        assertEquals("abcdef", part);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatchesAnySorted() {
        CharacterReader reader = new CharacterReader("z");
        assertTrue(reader.matchesAnySorted(new char[]{'a', 'z', 'b'}));
    }

    @Test(timeout = 4000)
    public void testMatchesAnySortedNoMatch() {
        CharacterReader reader = new CharacterReader("z");
        assertFalse(reader.matchesAnySorted(new char[]{'a', 'b'}));
    }

    @Test(timeout = 4000)
    public void testMatchesAnySortedEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesAnySorted(new char[]{'a'}));
    }

    @Test(timeout = 4000)
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abc123");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals('1', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("abc123def");
        assertEquals("abc123", reader.consumeLetterThenDigitSequence());
        assertEquals('d', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequence() {
        CharacterReader reader = new CharacterReader("1a2b3c");
        assertEquals("1a2b3c", reader.consumeHexSequence());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("42abc");
        assertEquals("42", reader.consumeDigitSequence());
        assertEquals('a', reader.current());
    }

    @Test(timeout = 4000)
    public void testMatchConsume() {
        CharacterReader reader = new CharacterReader("hello world");
        assertTrue(reader.matchConsume("hello"));
        assertEquals(' ', reader.current());
        assertFalse(reader.matchConsume("world"));
    }

    @Test(timeout = 4000)
    public void testMatchConsumeIgnoreCase() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matchConsumeIgnoreCase("hello"));
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.containsIgnoreCase("world"));
        assertFalse(reader.containsIgnoreCase("xyz"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("abc", reader.toString());
        reader.consume();
        assertEquals("bc", reader.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesLetter() {
        CharacterReader reader = new CharacterReader("A1");
        assertTrue(reader.matchesLetter());
        reader.consume();
        assertFalse(reader.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testMatchesDigit() {
        CharacterReader reader = new CharacterReader("5a");
        assertTrue(reader.matchesDigit());
        reader.consume();
        assertFalse(reader.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testConsumeToWithString() {
        CharacterReader reader = new CharacterReader("beforeXYZafter");
        String part = reader.consumeTo("XYZ");
        assertEquals("before", part);
        assertEquals('X', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToWithCharNotFound() {
        CharacterReader reader = new CharacterReader("abcdef");
        String part = reader.consumeTo('z');
        assertEquals("abcdef", part);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyWithMultipleDelimiters() {
        CharacterReader reader = new CharacterReader("a;b,c");
        String part = reader.consumeToAny(';', ',');
        assertEquals("a", part);
        assertEquals(';', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyNoDelimiter() {
        CharacterReader reader = new CharacterReader("abc");
        String part = reader.consumeToAny('x', 'y');
        assertEquals("abc", part);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testBufferUpWithLargeInput() {
        // Ensure bufferUp works correctly when bufPos exceeds split point
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append('x');
        CharacterReader reader = new CharacterReader(sb.toString());
        // Consume many chars to force bufferUp
        for (int i = 0; i < 5000; i++) reader.consume();
        assertFalse(reader.isEmpty());
        assertEquals('x', reader.current());
    }

    @Test(timeout = 4000)
    public void testMarkResetsSplitPoint() {
        CharacterReader reader = new CharacterReader("a");
        reader.mark(); // sets bufSplitPoint = 0, forces bufferUp
        // After mark, bufSplitPoint should be 0
        // This is internal, but we can verify behavior by rewinding
        reader.consume();
        reader.rewindToMark();
        assertEquals('a', reader.current());
    }
}