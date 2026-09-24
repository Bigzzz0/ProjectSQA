package org.jsoup.parser;

import org.jsoup.UncheckedIOException;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class: org.jsoup.parser.CharacterReader
 *
 * Branch & State Coverage Targets:
 * 1. Constructor Branching:
 *    - input == null (Validate.notNull throws IllegalArgumentException)
 *    - !input.markSupported() (Validate.isTrue throws IllegalArgumentException)
 *    - sz > maxBufferLen ? maxBufferLen : sz (both branches)
 *    - Reader vs String constructors
 * 2. Buffer Refill & Lifecycle (bufferUp):
 *    - pos < bufSplitPoint (early return without read)
 *    - pos >= bufSplitPoint (skips, marks, reads, resets)
 *    - read != -1 (updates readerPos, resets bufPos to 0, resets bufMark to -1)
 *    - read == -1 (EOF handling in buffer refill)
 *    - bufLength > readAheadLimit ? readAheadLimit : bufLength (both branches)
 *    - IOException wrapped into UncheckedIOException
 * 3. Position and Consumption State:
 *    - pos(): readerPos + bufPos
 *    - current() & consume(): EOF when empty, valid char when populated
 *    - unconsume(): bufPos < 1 exception vs decrement bufPos
 *    - advance(): increments bufPos
 *    - mark() & rewindToMark(): bufMark == -1 exception vs restoring bufPos
 * 4. Scan & Index Logic:
 *    - nextIndexOf(char): found vs not found (-1)
 *    - nextIndexOf(CharSequence):
 *      - startChar matched vs not found
 *      - while (++offset < bufLength && startChar != charBuf[offset])
 *      - boundary guard last <= bufLength
 *      - inner comparison loop seq.charAt(j) == charBuf[i]
 *      - full sequence match (i == last) vs partial mismatch
 * 5. Parsing & Extraction:
 *    - consumeTo(char) / consumeTo(String): delimiter found vs consumeToEnd()
 *    - consumeToAny(char...): matched any delimiter vs consumed to end, pos > start vs pos == start
 *    - consumeToAnySorted(char...): binary search match vs none
 *    - consumeData(): breaks on '&', '<', TokeniserState.nullChar ('\0')
 *    - consumeTagName(): breaks on '\t', '\n', '\r', '\f', ' ', '/', '>', '<', '\0'
 *    - consumeToEnd(): consumes remaining buffer
 *    - consumeLetterSequence(): 'A'-'Z', 'a'-'z', Character.isLetter(c) vs non-letters
 *    - consumeLetterThenDigitSequence(): letters followed by '0'-'9', digit-only, letter-only, none
 *    - consumeHexSequence(): '0'-'9', 'A'-'F', 'a'-'f'
 *    - consumeDigitSequence(): '0'-'9'
 * 6. Matching & Comparison:
 *    - matches(char): empty vs matching vs non-matching
 *    - matches(String) & matchesIgnoreCase(String): scanLength > remaining vs match vs mismatch
 *    - matchesAny(char...) & matchesAnySorted(char[]): empty vs in-set vs not-in-set
 *    - matchesLetter() & matchesDigit(): empty vs valid vs invalid
 *    - matchConsume(String) & matchConsumeIgnoreCase(String): match + advance vs false without advance
 *    - containsIgnoreCase(String): lowerScan match vs hiScan match vs not found
 * 7. String Cache & Flywheel Pattern (cacheString & rangeEquals):
 *    - count > maxStringCacheLen (12): creates new String without caching
 *    - count < 1: returns ""
 *    - cache miss (cached == null): puts into cache
 *    - cache hit: returns cached reference (assertSame)
 *    - hash collision / conflict: updates cache entry
 *    - rangeEquals: count != cached.length(), char mismatch, all characters equal
 * 8. Defect-Targeted Ground Truth:
 *    - Binary files/streams containing null bytes ('\0') and control characters
 *      (ConnectTest / ParseTest binary detection failures)
 */
public class CharacterReaderGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicConsumeAndAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.current());
        assertEquals(0, reader.pos());

        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());

        reader.advance();
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());

        assertEquals('c', reader.consume());
        assertEquals(3, reader.pos());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test(timeout = 4000)
    public void testUnconsumeNormal() {
        CharacterReader reader = new CharacterReader("test");
        assertEquals('t', reader.consume());
        assertEquals(1, reader.pos());

        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('t', reader.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewindNormal() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // at 'b', pos 1
        reader.mark();

        reader.consume(); // 'b'
        reader.consume(); // 'c'
        assertEquals(3, reader.pos());

        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testNextIndexOfChar() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(4, reader.nextIndexOf('o'));
        assertEquals(-1, reader.nextIndexOf('z'));

        reader.consume(); // 'h'
        assertEquals(3, reader.nextIndexOf('o'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharSequence() {
        CharacterReader reader = new CharacterReader("one two three two one");
        assertEquals(4, reader.nextIndexOf("two"));
        assertEquals(0, reader.nextIndexOf("one"));
        assertEquals(-1, reader.nextIndexOf("four"));

        reader.consumeTo("two");
        assertEquals(0, reader.nextIndexOf("two"));

        // seq where startChar matches but subsequent chars mismatch
        CharacterReader partialMatchReader = new CharacterReader("twx two");
        assertEquals(4, partialMatchReader.nextIndexOf("two"));

        // seq longer than remaining buffer after startChar match
        CharacterReader shortTail = new CharacterReader("prefix-tw");
        assertEquals(-1, shortTail.nextIndexOf("two"));

        // target not in buffer at all
        CharacterReader noMatch = new CharacterReader("abcdef");
        assertEquals(-1, noMatch.nextIndexOf("xyz"));
    }

    @Test(timeout = 4000)
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("foo/bar?baz");
        assertEquals("foo", reader.consumeTo('/'));
        assertEquals('/', reader.current());

        reader.consume(); // consume '/'
        assertEquals("bar?baz", reader.consumeTo('!')); // char not present -> consumeToEnd
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToString() {
        CharacterReader reader = new CharacterReader("foo<!--comment-->bar");
        assertEquals("foo", reader.consumeTo("<!--"));
        assertEquals('<', reader.current());

        reader.consumeTo("missing");
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("one;two,three");
        assertEquals("one", reader.consumeToAny(';', ','));
        assertEquals(';', reader.consume());

        assertEquals("two", reader.consumeToAny(';', ','));
        assertEquals(',', reader.consume());

        assertEquals("three", reader.consumeToAny(';', ','));
        assertTrue(reader.isEmpty());

        // When current pos already points to one of the characters
        CharacterReader immediateMatch = new CharacterReader(",rest");
        assertEquals("", immediateMatch.consumeToAny(',', ';'));
        assertEquals(',', immediateMatch.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnySorted() {
        CharacterReader reader = new Character