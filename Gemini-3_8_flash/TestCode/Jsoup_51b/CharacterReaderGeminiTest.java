package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.jsoup.parser.CharacterReader
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor:
 *    - null input -> IllegalArgumentException (via Validate.notNull)
 *    - empty string -> length = 0, isEmpty() = true
 * 2. Positional & Cursor State:
 *    - pos(), isEmpty(), current(), consume(), unconsume(), advance()
 *    - Boundary: pos >= length -> EOF returned by current() & consume()
 *    - mark() and rewindToMark() -> restore previous position
 * 3. Token Search & Offsets:
 *    - nextIndexOf(char): match found, match at start, match at end, not found (-1)
 *    - nextIndexOf(CharSequence):
 *      * length 1, substring inside, not found, sequence longer than remaining chars
 *      * loop branches: inner character mismatch, outer scan loop completion
 * 4. Consuming Substrings:
 *    - consumeTo(char) & consumeTo(String): offset != -1 vs offset == -1 (consumeToEnd)
 *    - consumeToAny(char...): match found early, multiple matches, no match
 *    - consumeToAnySorted(char...): binary search hit vs miss, empty chars
 *    - consumeData(): terminates on '&', '<', TokeniserState.nullChar, or end of string
 *    - consumeTagName(): terminates on '\t', '\n', '\r', '\f', ' ', '/', '>', nullChar, or end
 *    - consumeToEnd(): from start, from middle, when already at EOF
 * 5. Character Sequence Categories:
 *    - consumeLetterSequence(): ASCII upper/lower vs non-letter
 *    - consumeLetterThenDigitSequence(): letters only, letters + digits, digits only, neither
 *    - consumeHexSequence(): 0-9, A-F, a-f, non-hex
 *    - consumeDigitSequence(): digits only, non-digits, mixed
 * 6. Matching & Predicates:
 *    - matches(char): empty check, match, non-match
 *    - matches(String): scanLength > remaining, exact match, prefix mismatch
 *    - matchesIgnoreCase(String): scanLength > remaining, case variations, mismatch
 *    - matchesAny(char...): empty reader, match, no match
 *    - matchesAnySorted(char[]): empty reader, binary search match, miss
 *    - matchesLetter(): upper, lower, non-letter, empty
 *    - matchesDigit(): 0-9, non-digit, empty
 *    - matchConsume(String) & matchConsumeIgnoreCase(String): true and advances pos vs false
 *    - containsIgnoreCase(String): lower found, upper found, neither found
 * 7. String Cache & Flywheel Mechanism:
 *    - count > maxCacheLen (12): bypasses cache
 *    - cache miss: adds new string to stringCache
 *    - cache hit: rangeEquals returns true, returns cached instance
 *    - cache collision: same hash bucket, rangeEquals returns false -> replaces cached instance
 *    - rangeEquals(): different length, identical contents, different character within range
 * 8. Defect Targeted:
 *    - Known issue: HtmlParserTest::testSupportsNonAsciiTags.
 *    - Non-ASCII unicode letters (e.g., 'ä', 'ö', 'ü', Cyrillic 'п') should be recognized
 *      by matchesLetter(), consumeLetterSequence(), and consumeTagName() to support non-ASCII tags.
 */
public class CharacterReaderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndBasicNavigation() {
        CharacterReader reader = new CharacterReader("Hello");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('H', reader.current());

        char consumed = reader.consume();
        assertEquals('H', consumed);
        assertEquals(1, reader.pos());
        assertEquals('e', reader.current());

        reader.advance();
        assertEquals(2, reader.pos());
        assertEquals('l', reader.current());

        reader.unconsume();
        assertEquals(1, reader.pos());
        assertEquals('e', reader.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("Testing");
        reader.consume(); // pos = 1
        reader.mark();

        reader.consume();
        reader.consume();
        assertEquals(3, reader.pos());

        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('e', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("abc");
        String s1 = reader.consumeAsString();
        assertEquals("a", s1);
        assertEquals(1, reader.pos());
        String s2 = reader.consumeAsString();
        assertEquals("b", s2);
        assertEquals(2, reader.pos());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        CharacterReader reader = new CharacterReader("HelloWorld");
        assertEquals("HelloWorld", reader.toString());
        reader.consume();
        reader.consume();
        assertEquals("lloWorld", reader.toString());
        reader.consumeToEnd();
        assertEquals("", reader.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyReaderBehavior() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(-1, reader.nextIndexOf('a'));
        assertEquals(-1, reader.nextIndexOf("a"));
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
        assertFalse(reader.containsIgnoreCase("a"));
    }

    @Test(timeout = 4000)
    public void testExhaustionBoundaries() {
        CharacterReader reader = new CharacterReader("x");
        assertEquals('x', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(1, reader.pos());
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharBoundaries() {
        CharacterReader reader = new CharacterReader("banana");
        assertEquals(0, reader.nextIndexOf('b'));
        assertEquals(1, reader.nextIndexOf('a'));
        assertEquals(5, reader.nextIndexOf('a') + 4);
        assertEquals(-1, reader.nextIndexOf('z'));

        reader.advance(); // pos = 1 ('a')
        assertEquals(0, reader.nextIndexOf('a'));
        assertEquals(1, reader.nextIndexOf('n'));
        assertEquals(-1, reader.nextIndexOf('b'));
    }

    @Test(timeout = 4000)
    public void testNextIndexOfCharSequenceBoundaries() {
        CharacterReader reader = new CharacterReader("mississippi");
        assertEquals(0, reader.nextIndexOf("miss"));
        assertEquals(1, reader.nextIndexOf("is"));
        assertEquals(4, reader.nextIndexOf("issip"));
        assertEquals(-1, reader.nextIndexOf("zebra"));
        assertEquals(-1, reader.nextIndexOf("mississippis")); // longer than source

        // Scan when first char matches partially but second does not
        CharacterReader reader2 = new CharacterReader("ababac");
        assertEquals(4, reader2.nextIndexOf("ac"));
        assertEquals(-1, reader2.nextIndexOf("ad"));
    }

    @Test(timeout = 4000)
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("one;two;three");
        String part1 = reader.consumeTo(';');
        assertEquals("one", part1);
        assertEquals(';', reader.current());

        reader.consume(); // skip ';'
        String part2 = reader.consumeTo(';');
        assertEquals("two", part2);

        reader.consume(); // skip ';'
        String part3 = reader.consumeTo(';'); // not found, consumes to end
        assertEquals("three", part3);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToString() {
        CharacterReader reader = new CharacterReader("start<!--comment-->end");
        String part1 = reader.consumeTo("<!--");
        assertEquals("start", part1);

        reader.matchConsume("<!--");
        String part2 = reader.consumeTo("-->");
        assertEquals("comment", part2);

        reader.matchConsume("-->");
        String part3 = reader.consumeTo("missing");
        assertEquals("end", part3);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("foo&bar<baz");
        String text1 = reader.consumeToAny('&', '<');
        assertEquals("foo", text1);
        assertEquals('&', reader.current());

        reader.consume(); // consume '&'
        String text2 = reader.consumeToAny('&', '<');
        assertEquals("bar", text2);
        assertEquals('<', reader.current());

        reader.consume(); // consume '<'
        String text3 = reader.consumeToAny('&', '<'); // neither exists
        assertEquals("baz", text3);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnySorted() {
        char[] delimiters = new char[]{'&', '<'};
        Arrays.sort(delimiters);

        CharacterReader reader = new CharacterReader("alpha&beta<gamma");
        String s1 = reader.consumeToAnySorted(delimiters);
        assertEquals("alpha", s1);
        assertEquals('&', reader.current());

        reader.consume();
        String s2 = reader.consumeToAnySorted(delimiters);
        assertEquals("beta", s2);
        assertEquals('<', reader.current());

        reader.consume();
        String s3 = reader.consumeToAnySorted(delimiters);
        assertEquals("gamma", s3);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeData() {
        CharacterReader reader = new CharacterReader("some text&more<other" + TokeniserState.nullChar + "final");
        assertEquals("some text", reader.consumeData());
        assertEquals('&', reader.consume());
        assertEquals("more", reader.consumeData());
        assertEquals('<', reader.consume());
        assertEquals("other", reader.consumeData());
        assertEquals(TokeniserState.nullChar, reader.consume());
        assertEquals("final", reader.consumeData());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeTagName() {
        String input = "div\tspan\np\ra\fb img/button>input" + TokeniserState.nullChar + "end";
        CharacterReader reader = new CharacterReader(input);
        assertEquals("div", reader.consumeTagName());
        assertEquals('\t', reader.consume());
        assertEquals("span", reader.consumeTagName());
        assertEquals('\n', reader.consume());
        assertEquals("p", reader.consumeTagName());
        assertEquals('\r', reader.consume());
        assertEquals("a", reader.consumeTagName());
        assertEquals('\f', reader.consume());
        assertEquals("b", reader.consumeTagName());
        assertEquals(' ', reader.consume());
        assertEquals("img", reader.consumeTagName());
        assertEquals('/', reader.consume());
        assertEquals("button", reader.consumeTagName());
        assertEquals('>', reader.consume());
        assertEquals("input", reader.consumeTagName());
        assertEquals(TokeniserState.nullChar, reader.consume());
        assertEquals("end", reader.consumeTagName());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSequencesConsumption() {
        // consumeLetterSequence
        CharacterReader r1 = new CharacterReader("AbCdEf123");
        assertEquals("AbCdEf", r1.consumeLetterSequence());
        assertEquals('1', r1.current());

        // consumeLetterThenDigitSequence
        CharacterReader r2 = new CharacterReader("var123_rest");
        assertEquals("var123", r2.consumeLetterThenDigitSequence());
        assertEquals('_', r2.current());

        CharacterReader r2b = new CharacterReader("123onlydigits");
        assertEquals("123", r2b.consumeLetterThenDigitSequence());
        assertEquals("onlydigits", r2b.consumeLetterSequence());

        // consumeHexSequence
        CharacterReader r3 = new CharacterReader("0123456789ABCDEFabcdefGHI");
        assertEquals("0123456789ABCDEFabcdef", r3.consumeHexSequence());
        assertEquals('G', r3.current());

        // consumeDigitSequence
        CharacterReader r4 = new CharacterReader("98765abc");
        assertEquals("98765", r4.consumeDigitSequence());
        assertEquals('a', r4.current());
    }

    @Test(timeout = 4000)
    public void testMatchesAndVariants() {
        CharacterReader reader = new CharacterReader("HelloWorld123");
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('e'));

        assertTrue(reader.matches("Hello"));
        assertFalse(reader.matches("Hellp"));
        assertFalse(reader.matches("HelloWorld123ExtraLong"));

        assertTrue(reader.matchesIgnoreCase("hello"));
        assertTrue(reader.matchesIgnoreCase("HELLo"));
        assertFalse(reader.matchesIgnoreCase("hellp"));
        assertFalse(reader.matchesIgnoreCase("HelloWorld123ExtraLong"));

        assertTrue(reader.matchesAny('a', 'e', 'H'));
        assertFalse(reader.matchesAny('a', 'b', 'c'));

        char[] sorted = new char[]{'H', 'X', 'Z'};
        assertTrue(reader.matchesAnySorted(sorted));
        assertFalse(reader.matchesAnySorted(new char[]{'A', 'B'}));

        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        assertTrue(reader.matchConsume("Hello"));
        assertEquals(5, reader.pos());
        assertFalse(reader.matchConsume("WorldMismatch"));
        assertEquals(5, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase("world"));
        assertEquals(10, reader.pos());
        assertTrue(reader.matchesDigit());
        assertFalse(reader.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("The Quick Brown FOX Jumps Over The Lazy Dog");
        assertTrue(reader.containsIgnoreCase("quick"));
        assertTrue(reader.containsIgnoreCase("QUICK"));
        assertTrue(reader.containsIgnoreCase("fox"));
        assertTrue(reader.containsIgnoreCase("FOX"));
        assertFalse(reader.containsIgnoreCase("cat"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (HtmlParserTest::testSupportsNonAsciiTags)
    // =========================================================================

    /**
     * Targets Defects4J issue regarding support for non-ASCII tags (e.g., <äöü>Yes</äöü>, <первый>).
     * CharacterReader's matchesLetter() must recognize non-ASCII unicode letters
     * so that the parser can enter and consume non-ASCII tag names instead of dropping them.
     */
    @Test(timeout = 4000)
    public void testSupportsNonAsciiLettersInTagNamesAndSequences() {
        String nonAsciiTag = "äöü";
        CharacterReader reader = new CharacterReader(nonAsciiTag);

        // Fault-revealing assertion: On defective implementation, matchesLetter() returns false
        // for non-ASCII letters like 'ä', causing HtmlParser to fail to parse non-ASCII tags.
        assertTrue("Non-ASCII letters must be matched as letters", reader.matchesLetter());

        String consumedLetterSequence = reader.consumeLetterSequence();
        assertEquals("äöü", consumedLetterSequence);

        CharacterReader cyrillicReader = new CharacterReader("первый");
        assertTrue("Cyrillic letters must be recognized as letters", cyrillicReader.matchesLetter());
        assertEquals("первый", cyrillicReader.consumeLetterSequence());
    }

    @Test(timeout = 4000)
    public void testNonAsciiLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("тест123");
        assertTrue("Cyrillic characters should match letter predicate", reader.matchesLetter());
        assertEquals("тест123", reader.consumeLetterThenDigitSequence());
        assertTrue(reader.isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullInputThrowsException() {
        new CharacterReader(null);
    }

    // =========================================================================
    // Partition E: Flywheel Cache & Internal Range Matching
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringCacheFlywheelHitAndBypass() {
        // String longer than maxCacheLen (12 chars) bypasses cache
        String longStr = "1234567890123";
        CharacterReader reader = new CharacterReader(longStr + ";" + longStr + ";short;short;");

        String long1 = reader.consumeTo(';');
        reader.consume(); // skip ';'
        String long2 = reader.consumeTo(';');
        reader.consume();

        assertEquals(longStr, long1);
        assertEquals(longStr, long2);

        // Cache hit test for short strings (<= 12 chars)
        String short1 = reader.consumeTo(';');
        reader.consume();
        String short2 = reader.consumeTo(';');
        reader.consume();

        assertEquals("short", short1);
        assertEquals("short", short2);
        // Flywheel must return the exact same instance from cache
        assertSame(short1, short2);
    }

    @Test(timeout = 4000)
    public void testRangeEqualsDirect() {
        CharacterReader reader = new CharacterReader("abcdef");
        // rangeEquals(start, count, cached)
        assertTrue(reader.rangeEquals(0, 3, "abc"));
        assertFalse(reader.rangeEquals(0, 3, "abcd")); // different length
        assertFalse(reader.rangeEquals(0, 3, "abd"));  // different char
        assertTrue(reader.rangeEquals(3, 3, "def"));
    }

    @Test(timeout = 4000)
    public void testStringCacheCollisionReplacement() {
        // Construct two different short strings that hash to the same bucket
        // stringCache array length is 512, index = hash & 511.
        CharacterReader reader = new CharacterReader("tag1;tag2;tag1;");
        String first = reader.consumeTo(';');
        reader.consume();
        String second = reader.consumeTo(';');
        reader.consume();
        String third = reader.consumeTo(';');

        assertEquals("tag1", first);
        assertEquals("tag2", second);
        assertEquals("tag1", third);
    }
}