package org.jsoup.parser;

import org.jsoup.UncheckedIOException;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 -----------------------------------------------------------------------------------------------------------------------
 Target Class: org.jsoup.parser.CharacterReader
 Defect ID   : HTML Tag Name parsing defect (Defects4J jsoup issue where '<' is not treated as a delimiter in consumeTagName)

 Branches & Decision Points Targeted:
 1. Constructor Validation:
    - Reader is null -> IllegalArgumentException via Validate.notNull.
    - Reader does not support mark -> IllegalArgumentException via Validate.isTrue.
    - sz > maxBufferLen vs sz <= maxBufferLen sizing branch.
 2. bufferUp() logic & Buffer Transitions:
    - bufPos < bufSplitPoint (early return).
    - bufPos >= bufSplitPoint (trigger read and refill).
    - read == -1 (EOF reached, no update) vs read != -1.
    - IOException during reader operations wrapped into UncheckedIOException.
    - Multi-chunk read across readAheadLimit boundary.
 3. Position and Navigation:
    - pos(), isEmpty(), isEmptyNoBufferUp(), current(), consume(), unconsume(), advance(), mark(), rewindToMark().
    - current() & consume() at EOF returning CharacterReader.EOF ((char)-1).
 4. Searching & NextIndexOf:
    - nextIndexOf(char): found at start, middle, end, not found.
    - nextIndexOf(CharSequence): empty seq, sequence found, sequence not found,
      partial matches, sequence longer than buffer, offset + seq.length() > bufLength.
 5. Consumption Logic:
    - consumeTo(char): found vs not found (consumeToEnd).
    - consumeTo(String): found vs not found (consumeToEnd).
    - consumeToAny(char...): matched at 0, matched at middle, never matched.
    - consumeToAnySorted(char...): binarySearch match vs no match.
    - consumeData(): delimiters '&', '<', TokeniserState.nullChar ('\0').
    - consumeTagName(): delimiters '\t', '\n', '\r', '\f', ' ', '/', '>', '\0', and critical defect delimiter '<'.
    - consumeLetterSequence(), consumeLetterThenDigitSequence(), consumeHexSequence(), consumeDigitSequence().
 6. Matching Routines:
    - matches(char): empty buffer vs matching char vs non-matching char.
    - matches(String): empty, scanLength > bufLength - bufPos, matches, mismatch.
    - matchesIgnoreCase(String): case difference, mismatch, buffer length boundary.
    - matchesAny(char...): empty buffer, match, no match.
    - matchesAnySorted(char[]): empty, match, no match.
    - matchesLetter(): uppercase, lowercase, Unicode letter, non-letter.
    - matchesDigit(): '0'-'9', non-digit.
    - matchConsume(String) & matchConsumeIgnoreCase(String): match true (advances bufPos), false.
    - containsIgnoreCase(String): lower case match, upper case match, neither.
 7. String Cache & Flywheel Behavior:
    - count > maxStringCacheLen (12) -> bypass cache.
    - count < 1 -> returns "".
    - Cache miss (first time string seen).
    - Cache hit (exact string equality).
    - Cache collision (same hash bucket, different string -> overwrites cache).
    - rangeEquals utility checks: length mismatch, character mismatch, exact match.
 -----------------------------------------------------------------------------------------------------------------------
 */
public class CharacterReaderGeminiTest {

    // =================================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =================================================================================================================

    @Test(timeout = 4000)
    public void testBasicNavigationAndConsumption() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.isEmpty());
        assertEquals(0, reader.pos());

        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());

        assertEquals('b', reader.current());
        reader.advance();
        assertEquals(2, reader.pos());

        reader.unconsume();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.consume());

        assertEquals('c', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // 'a'
        reader.consume(); // 'b'
        reader.mark();
        assertEquals(2, reader.pos());

        reader.consume(); // 'c'
        reader.consume(); // 'd'
        assertEquals(4, reader.pos());

        reader.rewindToMark();
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());
    }

    @Test(timeout = 4000)
    public void testToStringReturnsRemainingCharacters() {
        CharacterReader reader = new CharacterReader("hello world");
        reader.consume(); // h
        reader.consume(); // e
        assertEquals("llo world", reader.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("foo=bar");
        String consumed = reader.consumeTo('=');
        assertEquals("foo", consumed);
        assertEquals('=', reader.current());

        String rest = reader.consumeTo('z');
        assertEquals("=bar", rest);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToStringSequence() {
        CharacterReader reader = new CharacterReader("one-->two-->three");
        String part1 = reader.consumeTo("-->");
        assertEquals("one", part1);
        assertTrue(reader.matches("-->"));
        reader.matchConsume("-->");

        String part2 = reader.consumeTo("missing");
        assertEquals("two-->three", part2);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("name: value;");
        String part = reader.consumeToAny(':', ';', ' ');
        assertEquals("name", part);
        assertEquals(':', reader.current());

        reader.advance(); // skip ':'
        reader.advance(); // skip ' '
        String part2 = reader.consumeToAny(';');
        assertEquals("value", part2);
        assertEquals(';', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnySorted() {
        CharacterReader reader = new CharacterReader("foo,bar;baz");
        char[] sortedDelims = new char[]{',', ';'};
        Arrays.sort(sortedDelims);

        String first = reader.consumeToAnySorted(sortedDelims);
        assertEquals("foo", first);
        assertEquals(',', reader.current());
        reader.advance();

        String second = reader.consumeToAnySorted(sortedDelims);
        assertEquals("bar", second);
        assertEquals(';', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeData() {
        CharacterReader reader = new CharacterReader("Normal text &amp; <tag>\0remainder");
        String data1 = reader.consumeData();
        assertEquals("Normal text ", data1);
        assertEquals('&', reader.current());
        reader.advance();

        String data2 = reader.consumeData();
        assertEquals("amp; ", data2);
        assertEquals('<', reader.current());
        reader.advance();

        String data3 = reader.consumeData();
        assertEquals("tag>", data3);
        assertEquals('\0', reader.current());
        reader.advance();

        String data4 = reader.consumeData();
        assertEquals("remainder", data4);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeLetterAndDigitSequences() {
        CharacterReader reader = new CharacterReader("alpha123 456 deadBEEF789xyz");
        assertEquals("alpha", reader.consumeLetterSequence());
        assertEquals("123", reader.consumeDigitSequence());
        reader.advance(); // space

        assertEquals("456", reader.consumeDigitSequence());
        reader.advance(); // space

        // Letters then digits: "deadBEEF789"
        assertEquals("deadBEEF789", reader.consumeLetterThenDigitSequence());

        // Hex sequence on "xyz" -> empty string
        assertEquals("", reader.consumeHexSequence());
        assertEquals("xyz", reader.consumeLetterSequence());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeHexSequence() {
        CharacterReader reader = new CharacterReader("0123456789ABCDEFabcdefGHI");
        assertEquals("0123456789ABCDEFabcdef", reader.consumeHexSequence());
        assertEquals('G', reader.current());
    }

    @Test(timeout = 4000)
    public void testMatchingPredicates() {
        CharacterReader reader = new CharacterReader("Hello 123");
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('e'));

        assertTrue(reader.matches("Hello"));
        assertFalse(reader.matches("Helo"));
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertFalse(reader.matchesIgnoreCase("hella"));

        assertTrue(reader.matchesAny('a', 'H', 'z'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));

        char[] sorted = new char[]{'A', 'H', 'Z'};
        assertTrue(reader.matchesAnySorted(sorted));
        assertFalse(reader.matchesAnySorted(new char[]{'B', 'C'}));

        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        assertTrue(reader.matchConsume("Hello "));
        assertEquals("123", reader.toString());

        assertFalse(reader.matchesLetter());
        assertTrue(reader.matchesDigit());
        assertTrue(reader.matchConsumeIgnoreCase("123"));
        assertTrue(reader.isEmpty());

        assertFalse(reader.matches('a'));
        assertFalse(reader.matches("a"));
        assertFalse(reader.matchesIgnoreCase("a"));
        assertFalse(reader.matchesAny('a'));
        assertFalse(reader.matchesAnySorted(new char[]{'a'}));
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertFalse(reader.matchConsume("a"));
        assertFalse(reader.matchConsumeIgnoreCase("a"));
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("<html><TITLE>Test Page</title></html>");
        assertTrue(reader.containsIgnoreCase("title"));
        assertTrue(reader.containsIgnoreCase("TITLE"));
        assertTrue(reader.containsIgnoreCase("<html"));
        assertFalse(reader.containsIgnoreCase("body"));
    }

    // =================================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =================================================================================================================

    @Test(timeout = 4000)
    public void testEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals("", reader.consumeTo('a'));
        assertEquals("", reader.consumeTo("seq"));
        assertEquals("", reader.consumeToAny('a', 'b'));
        assertEquals("", reader.consumeToAnySorted(new char[]{'a', 'b'}));
        assertEquals("", reader.consumeData());
        assertEquals("", reader.consumeTagName());
        assertEquals("", reader.consumeLetterSequence());
        assertEquals("", reader.consumeLetterThenDigitSequence());
        assertEquals("", reader.consumeHexSequence());
        assertEquals("", reader.consumeDigitSequence());
        assertEquals("", reader.consumeToEnd());
        assertEquals("", reader.toString());
        assertEquals(-1, reader.nextIndexOf('x'));
        assertEquals(-1, reader.nextIndexOf("xyz"));
        assertFalse(reader.containsIgnoreCase("anything"));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfSequenceBoundaries() {
        CharacterReader reader = new CharacterReader("abababc");
        assertEquals(0, reader.nextIndexOf("ab"));
        assertEquals(2, reader.nextIndexOf("aba"));
        assertEquals(4, reader.nextIndexOf("abc"));
        assertEquals(-1, reader.nextIndexOf("abcd")); // longer than remaining
        assertEquals(-1, reader.nextIndexOf("z"));
    }

    @Test(timeout = 4000)
    public void testMatchesLengthGreaterThanBuffer() {
        CharacterReader reader = new CharacterReader("short");
        assertFalse(reader.matches("longer sequence"));
        assertFalse(reader.matchesIgnoreCase("longer sequence"));
    }

    @Test(timeout = 4000)
    public void testLargeBufferBufferingAcrossSplitPoint() {
        // Construct string larger than CharacterReader.maxBufferLen
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String content = sb.toString();
        CharacterReader reader = new CharacterReader(new StringReader(content));

        int count = 0;
        while (!reader.isEmpty()) {
            char expected = content.charAt(count);
            char actual = reader.consume();
            assertEquals(expected, actual);
            count++;
        }
        assertEquals(content.length(), count);
        assertEquals(content.length(), reader.pos());
    }

    @Test(timeout = 4000)
    public void testCustomBufferCapacityConstructor() {
        // sz > maxBufferLen branch
        CharacterReader reader1 = new CharacterReader(new StringReader("test"), CharacterReader.maxBufferLen + 100);
        assertEquals("test", reader1.consumeToEnd());

        // sz <= maxBufferLen branch
        CharacterReader reader2 = new CharacterReader(new StringReader("test"), 16);
        assertEquals("test", reader2.consumeToEnd());
    }

    // =================================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect in consumeTagName)
    // =================================================================================================================

    /**
     * TARGETED DEFECT:
     * In defective jsoup versions, consumeTagName() omits checking for '<' as a delimiter,
     * despite comments indicating '<' was added to handle author bugs like <p<div.
     * When hitting '<' inside a tag name, consumeTagName() MUST stop consumption before '<'.
     */
    @Test(timeout = 4000)
    public void testConsumeTagNameStopsOnLessThanDelimiter() {
        CharacterReader reader = new CharacterReader("p<div id=\"one\">");
        String tagName = reader.consumeTagName();
        assertEquals("consumeTagName must terminate before '<'", "p", tagName);
        assertEquals('<', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeTagNameStandardDelimiters() {
        // Delimiters: '\t', '\n', '\r', '\f', ' ', '/', '>', '\0'
        String[] cases = {"tag\t", "tag\n", "tag\r", "tag\f", "tag ", "tag/", "tag>", "tag\0"};
        for (String input : cases) {
            CharacterReader reader = new CharacterReader(input);
            assertEquals("Failed delimiter for input: " + input, "tag", reader.consumeTagName());
        }
    }

    // =================================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =================================================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullReaderThrowsException() {
        new CharacterReader((Reader) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullStringThrowsException() {
        new CharacterReader((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReaderWithoutMarkSupportThrowsException() {
        Reader unmarkableReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                return -1;
            }

            @Override
            public void close() {
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new CharacterReader(unmarkableReader);
    }

    @Test(expected = UncheckedIOException.class, timeout = 4000)
    public void testReaderThrowsIOExceptionWrappedInUnchecked() {
        Reader faultReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated disk error");
            }

            @Override
            public void close() {
            }

            @Override
            public boolean markSupported() {
                return true;
            }
        };
        new CharacterReader(faultReader);
    }

    // =================================================================================================================
    // Partition E: String Flywheel Cache & Range Equality Mechanics
    // =================================================================================================================

    @Test(timeout = 4000)
    public void testStringCacheHitsAndMisses() {
        // maxStringCacheLen is 12; test cache hit for strings <= 12 chars
        CharacterReader reader = new CharacterReader("span-span-span-superlongstringexceedingcache");
        String first = reader.consumeTo('-');
        reader.advance(); // skip '-'
        String second = reader.consumeTo('-');
        reader.advance(); // skip '-'
        String third = reader.consumeTo('-');
        reader.advance(); // skip '-'

        assertEquals("span", first);
        assertEquals("span", second);
        assertEquals("span", third);
        assertSame("String cache should return identical flyweight instance", first, second);
        assertSame("String cache should return identical flyweight instance", second, third);

        // String > 12 characters bypasses cache
        String longStr = reader.consumeToEnd();
        assertEquals("superlongstringexceedingcache", longStr);
    }

    @Test(timeout = 4000)
    public void testRangeEqualsHelper() {
        CharacterReader reader = new CharacterReader("TestingRangeEquals");
        // rangeEquals(int start, int count, String cached)
        assertTrue(reader.rangeEquals(0, 7, "Testing"));
        assertFalse(reader.rangeEquals(0, 7, "testing")); // case mismatch
        assertFalse(reader.rangeEquals(0, 6, "Testing")); // count mismatch
        assertFalse(reader.rangeEquals(7, 5, "Equals"));  // Range is "Range", not "Equals"
        assertTrue(reader.rangeEquals(12, 6, "Equals"));
    }

    @Test(timeout = 4000)
    public void testStaticRangeEqualsDirect() {
        char[] buf = "abcdef".toCharArray();
        assertTrue(CharacterReader.rangeEquals(buf, 0, 3, "abc"));
        assertFalse(CharacterReader.rangeEquals(buf, 0, 3, "abd"));
        assertFalse(CharacterReader.rangeEquals(buf, 0, 2, "abc"));
    }
}