package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.jsoup.UncheckedIOException;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------
 * Target Class: org.jsoup.parser.CharacterReader
 * Defects4J Bug Identification:
 *   - CharacterReaderTest::consumeToNonexistentEndWhenAtAnd
 *   - HtmlParserTest::commentAtEnd
 *   --> Root Cause: When reader hits EOF, bufLength is set to -1 by reader.read(charBuf).
 *       Subsequent calls to consumeTo('c') or consumeTo(seq) fall back to consumeToEnd().
 *       consumeToEnd() passes (bufLength - bufPos) = (-1 - 0) = -1 as count to cacheString(),
 *       triggering java.lang.StringIndexOutOfBoundsException: String index out of range: -1.
 *
 * Decision / Condition Matrix Covered:
 *   1. Constructors:
 *      - null Reader -> Validate.notNull fails (IllegalArgumentException)
 *      - Reader with markSupported() == false -> Validate.isTrue fails (IllegalArgumentException)
 *      - sz > maxBufferLen vs sz <= maxBufferLen branch
 *   2. bufferUp():
 *      - bufPos < bufSplitPoint (fast path return)
 *      - bufPos >= bufSplitPoint (refill buffer, IOException wrap in UncheckedIOException)
 *      - bufLength > readAheadLimit vs bufLength <= readAheadLimit
 *   3. pos() & isEmpty():
 *      - bufPos >= bufLength boundary (empty state), readerPos calculation across buffer reloads
 *   4. current() & consume() & unconsume() & advance():
 *      - EOF char (-1) return on empty reader
 *      - Buffer repositioning, single step advance and unconsume
 *   5. mark() & rewindToMark():
 *      - State preservation and restoration to marked position
 *   6. nextIndexOf(char) & nextIndexOf(CharSequence):
 *      - Start char immediate match vs scan loop
 *      - Sequence length exceeding remaining buffer (last > bufLength)
 *      - Partial sequence match and mismatch during inner comparison loop
 *      - Target character not found (-1 branch)
 *   7. consumeTo(char) & consumeTo(String):
 *      - Target found (offset != -1, substring cached)
 *      - Target not found (offset == -1, triggers consumeToEnd())
 *   8. consumeToAny(char...) & consumeToAnySorted(char...):
 *      - Delimiter match breaking outer loop
 *      - No delimiter match consuming to end
 *      - binarySearch positive vs negative results
 *   9. consumeData() & consumeTagName():
 *      - Delimiter stops (&, <, \0, \t, \n, \r, \f, ' ', /, >)
 *      - Non-delimiter pass-through
 *  10. consumeLetterSequence(), consumeLetterThenDigitSequence(), consumeHexSequence(), consumeDigitSequence():
 *      - ASCII range boundaries ('A'-'Z', 'a'-'z', '0'-'9', 'A'-'F', 'a'-'f')
 *      - Character.isLetter unicode branch
 *      - Zero-match empty string return
 *  11. matches(char), matches(String), matchesIgnoreCase(String), matchesAny(), matchesAnySorted():
 *      - Length comparison (scanLength > bufLength - bufPos)
 *      - Case conversion uppercase comparison
 *      - Empty buffer checks
 *  12. matchConsume() & matchConsumeIgnoreCase():
 *      - Match success (advance bufPos) vs mismatch (keep bufPos)
 *  13. containsIgnoreCase():
 *      - Match via lowerCase scan vs upperCase scan vs neither
 *  14. cacheString flywheel cache:
 *      - count > maxStringCacheLen (cache bypass)
 *      - cache miss (null slot insert)
 *      - cache hit (rangeEquals == true)
 *      - cache collision (rangeEquals == false, overwrite slot)
 *  15. rangeEquals():
 *      - count != cached.length()
 *      - Character by character match and mismatch
 */
public class CharacterReaderGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicConsumeAndPositionTransitions() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());

        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());

        reader.advance();
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());

        reader.unconsume();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());

        assertEquals('b', reader.consume());
        assertEquals('c', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // at 'b', pos 1
        reader.mark();

        assertEquals('b', reader.consume());
        assertEquals('c', reader.consume());
        assertEquals(3, reader.pos());

        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testBufferRefillAcrossSmallBufferSize() {
        // String length 20 with buffer size 8 to force multiple bufferUp() cycles
        String content = "0123456789ABCDEFGHIJ";
        CharacterReader reader = new CharacterReader(new StringReader(content), 8);

        for (int i = 0; i < content.length(); i++) {
            assertEquals(i, reader.pos());
            assertEquals(content.charAt(i), reader.consume());
        }
        assertTrue(reader.isEmpty());
        assertEquals(20, reader.pos());
    }

    @Test(timeout = 4000)
    public void testNextIndexOfChar() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(0, reader.nextIndexOf('h'));
        assertEquals(4, reader.nextIndexOf('o'));
        assertEquals(-1, reader.nextIndexOf('z'));

        reader.consumeTo(' ');
        reader.advance(); // skip space
        assertEquals(4, reader.nextIndexOf('d'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfSequence() {
        CharacterReader reader = new CharacterReader("one two three two one");
        assertEquals(4, reader.nextIndexOf("two"));
        assertEquals(0, reader.nextIndexOf("one"));
        assertEquals(-1, reader.nextIndexOf("four"));

        // Scan sequence where first char matches multiple times before full match
        CharacterReader repetition = new CharacterReader("nananana_batman");
        assertEquals(9, repetition.nextIndexOf("batman"));

        // Sequence extending past buffer end
        CharacterReader shortReader = new CharacterReader("short");
        assertEquals(-1, shortReader.nextIndexOf("shorterThanInput"));
    }

    @Test(timeout = 4000)
    public void testConsumeToCharAndString() {
        CharacterReader reader = new CharacterReader("target:value;next:done");
        String prefix = reader.consumeTo(':');
        assertEquals("target", prefix);
        assertEquals(':', reader.consume()); // consume ':'

        String val = reader.consumeTo(';');
        assertEquals("value", val);
        reader.advance(); // skip ';'

        String nextVal = reader.consumeTo("done");
        assertEquals("next:", nextVal);
        assertEquals("done", reader.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("foo&bar<baz");
        assertEquals("foo", reader.consumeToAny('&', '<'));
        assertEquals('&', reader.consume());
        assertEquals("bar", reader.consumeToAny('&', '<'));
        assertEquals('<', reader.consume());
        assertEquals("baz", reader.consumeToAny('&', '<'));
        assertEquals("", reader.consumeToAny('&', '<'));
    }

    @Test(timeout = 4000)
    public void testConsumeToAnySorted() {
        CharacterReader reader = new CharacterReader("apple;banana,orange");
        char[] sortedDelims = new char[]{',', ';'};
        assertEquals("apple", reader.consumeToAnySorted(sortedDelims));
        assertEquals(';', reader.consume());
        assertEquals("banana", reader.consumeToAnySorted(sortedDelims));
        assertEquals(',', reader.consume());
        assertEquals("orange", reader.consumeToAnySorted(sortedDelims));
    }

    @Test(timeout = 4000)
    public void testConsumeData() {
        CharacterReader reader1 = new CharacterReader("plainText&more");
        assertEquals("plainText", reader1.consumeData());

        CharacterReader reader2 = new CharacterReader("plainText<tag");
        assertEquals("plainText", reader2.consumeData());

        CharacterReader reader3 = new CharacterReader("plainText\u0000null");
        assertEquals("plainText", reader3.consumeData());

        CharacterReader reader4 = new CharacterReader("plainTextOnly");
        assertEquals("plainTextOnly", reader4.consumeData());
    }

    @Test(timeout = 4000)
    public void testConsumeTagName() {
        char[] delimiters = new char[]{'\t', '\n', '\r', '\f', ' ', '/', '>', '\u0000'};
        for (char delim : delimiters) {
            CharacterReader reader = new CharacterReader("tag" + delim + "rest");
            assertEquals("tag", reader.consumeTagName());
        }
        CharacterReader readerNoDelim = new CharacterReader("tagWithoutDelim");
        assertEquals("tagWithoutDelim", readerNoDelim.consumeTagName());
    }

    @Test(timeout = 4000)
    public void testConsumeSequences() {
        // consumeLetterSequence
        CharacterReader letters = new CharacterReader("HelloWorld123");
        assertEquals("HelloWorld", letters.consumeLetterSequence());
        assertEquals("123", letters.consumeToEnd());

        CharacterReader unicodeLetters = new CharacterReader("Émile123");
        assertEquals("Émile", unicodeLetters.consumeLetterSequence());

        CharacterReader nonLetters = new CharacterReader("123Hello");
        assertEquals("", nonLetters.consumeLetterSequence());

        // consumeLetterThenDigitSequence
        CharacterReader letDig = new CharacterReader("var123 = true;");
        assertEquals("var123", letDig.consumeLetterThenDigitSequence());

        CharacterReader onlyLetters = new CharacterReader("onlyLetters ");
        assertEquals("onlyLetters", onlyLetters.consumeLetterThenDigitSequence());

        CharacterReader onlyDigits = new CharacterReader("12345letters");
        assertEquals("12345", onlyDigits.consumeLetterThenDigitSequence());

        CharacterReader neither = new CharacterReader("!@#123");
        assertEquals("", neither.consumeLetterThenDigitSequence());

        // consumeHexSequence
        CharacterReader hex = new CharacterReader("0123456789ABCDEFabcdefGHIJ");
        assertEquals("0123456789ABCDEFabcdef", hex.consumeHexSequence());

        CharacterReader nonHex = new CharacterReader("xyz");
        assertEquals("", nonHex.consumeHexSequence());

        // consumeDigitSequence
        CharacterReader digits = new CharacterReader("9876543210abc");
        assertEquals("9876543210", digits.consumeDigitSequence());

        CharacterReader nonDigits = new CharacterReader("abc123");
        assertEquals("", nonDigits.consumeDigitSequence());
    }

    @Test(timeout = 4000)
    public void testMatchesAndMatchConsume() {
        CharacterReader reader = new CharacterReader("HelloWorld");

        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('e'));

        assertTrue(reader.matches("Hello"));
        assertFalse(reader.matches("hello"));
        assertFalse(reader.matches("HelloWorldLongerThanBuffer"));

        assertTrue(reader.matchesIgnoreCase("hello"));
        assertTrue(reader.matchesIgnoreCase("HELLO"));
        assertFalse(reader.matchesIgnoreCase("world"));
        assertFalse(reader.matchesIgnoreCase("HelloWorldLonger"));

        assertTrue(reader.matchConsume("Hello"));
        assertEquals("World", reader.toString());

        assertFalse(reader.matchConsume("Earth"));
        assertEquals("World", reader.toString());

        assertTrue(reader.matchConsumeIgnoreCase("world"));
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatchesAnyAndSorted() {
        CharacterReader reader = new CharacterReader("quick");

        assertTrue(reader.matchesAny('a', 'e', 'i', 'o', 'u', 'q'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));

        char[] sorted = new char[]{'b', 'k', 'q', 'z'};
        assertTrue(reader.matchesAnySorted(sorted));
        assertFalse(reader.matchesAnySorted(new char[]{'a', 'c'}));

        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        CharacterReader digitReader = new CharacterReader("9lives");
        assertFalse(digitReader.matchesLetter());
        assertTrue(digitReader.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("prefix </TITLE> suffix");
        assertTrue(reader.containsIgnoreCase("</title>"));
        assertTrue(reader.containsIgnoreCase("</TITLE>"));
        assertFalse(reader.containsIgnoreCase("</style>"));

        CharacterReader readerLower = new CharacterReader("prefix </style> suffix");
        assertTrue(readerLower.containsIgnoreCase("</STYLE>"));
        assertTrue(readerLower.containsIgnoreCase("</style>"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyReaderBehavior() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals("", reader.consumeToEnd());
        assertEquals("", reader.consumeLetterSequence());
        assertEquals("", reader.consumeLetterThenDigitSequence());
        assertEquals("", reader.consumeHexSequence());
        assertEquals("", reader.consumeDigitSequence());
        assertFalse(reader.matches('a'));
        assertFalse(reader.matches("a"));
        assertFalse(reader.matchesIgnoreCase("a"));
        assertFalse(reader.matchesAny('a', 'b'));
        assertFalse(reader.matchesAnySorted(new char[]{'a', 'b'}));
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertFalse(reader.matchConsume("a"));
        assertFalse(reader.matchConsumeIgnoreCase("a"));
    }

    @Test(timeout = 4000)
    public void testStringCacheFlywheelBoundaryAndCollisions() {
        // String longer than maxStringCacheLen (12 chars) -> bypasses cache
        String longStr = "1234567890123";
        CharacterReader r1 = new CharacterReader(longStr);
        String consumedLong = r1.consumeToEnd();
        assertEquals(longStr, consumedLong);

        // Strings <= 12 chars -> utilizes stringCache
        CharacterReader r2 = new CharacterReader("div span div");
        String tag1 = r2.consumeTo(' ');
        assertEquals("div", tag1);
        r2.advance();

        String tag2 = r2.consumeTo(' ');
        assertEquals("span", tag2);
        r2.advance();

        String tag3 = r2.consumeToEnd();
        assertEquals("div", tag3);
        assertSame("Flywheel cache should return exact same interned instance", tag1, tag3);

        // Direct test of package-private rangeEquals methods
        CharacterReader r3 = new CharacterReader("sampleText");
        assertTrue(r3.rangeEquals(0, 6, "sample"));
        assertFalse(r3.rangeEquals(0, 5, "sample")); // length mismatch
        assertFalse(r3.rangeEquals(0, 6, "simple")); // character mismatch
    }

    @Test(timeout = 4000)
    public void testConstructorWithLargeBufferSize() {
        // sz > maxBufferLen branch
        int hugeSize = CharacterReader.maxBufferLen + 1000;
        CharacterReader reader = new CharacterReader(new StringReader("test"), hugeSize);
        assertEquals("test", reader.consumeToEnd());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect:
     * - CharacterReaderTest::consumeToNonexistentEndWhenAtAnd
     * - HtmlParserTest::commentAtEnd
     * When cursor reaches end of reader, bufLength is set to -1, which without defensive
     * normalization causes consumeTo to call consumeToEnd with negative length, throwing
     * java.lang.StringIndexOutOfBoundsException: String index out of range: -1.
     */
    @Test(timeout = 4000)
    public void testConsumeToNonexistentEndWhenAtEnd() {
        CharacterReader r = new CharacterReader("<!");
        assertTrue(r.matchConsume("<!"));
        assertTrue(r.isEmpty());

        // Under defective state: bufLength is -1, nextIndexOf returns -1,
        // and consumeTo('>') invokes consumeToEnd() -> new String(charBuf, 0, -1)
        String s = r.consumeTo('>');
        assertEquals("", s);
    }

    @Test(timeout = 4000)
    public void testConsumeToNonexistentSequenceWhenAtEnd() {
        CharacterReader r = new CharacterReader("<!--");
        assertTrue(r.matchConsume("<!--"));
        assertTrue(r.isEmpty());

        String s = r.consumeTo("-->");
        assertEquals("", s);
    }

    @Test(timeout = 4000)
    public void testEmptyReaderConsumeToDelimiters() {
        CharacterReader r = new CharacterReader(new StringReader(""));
        assertEquals("", r.consumeTo('>'));
        assertEquals("", r.consumeTo("any"));
        assertEquals("", r.consumeToEnd());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullReaderThrowsException() {
        new CharacterReader((Reader) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnmarkableReaderThrowsException() {
        Reader unmarkable = new Reader() {
            @Override
            public boolean markSupported() {
                return false;
            }

            @Override
            public int read(char[] cbuf, int off, int len) {
                return -1;
            }

            @Override
            public void close() {}
        };
        new CharacterReader(unmarkable);
    }

    @Test(expected = UncheckedIOException.class, timeout = 4000)
    public void testReaderThrowingIOExceptionWrapsInUncheckedIOException() {
        Reader faultyReader = new Reader() {
            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public void mark(int readAheadLimit) throws IOException {}

            @Override
            public void reset() throws IOException {}

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated disk failure");
            }

            @Override
            public void close() throws IOException {}
        };
        new CharacterReader(faultyReader);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringIntegrity() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.toString());

        reader.consume();
        reader.consume();
        assertEquals("cdef", reader.toString());

        reader.consumeToEnd();
        assertEquals("", reader.toString());
    }
}