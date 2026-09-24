package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 ---------------------------------------------------------------------------------------------------
 Class Under Test: org.jsoup.parser.CharacterReader
 Targeted Defect: Defects4J - EntitiesTest::letterDigitEntities
 Defect Symptom: Parsing entity names composed of letters followed by digits (e.g. &sup1;, &frac12;)
                 fails because letter sequence consumption prematurely terminated before digits,
                 leading to malformed entity lookups (e.g. matching &sup; instead of &sup1;).

 Decision & Branch Coverage Points:
 1. Constructor:
    - Normalizes CRLF ("\r\n") and CR ("\r") into LF ("\n").
    - Null validation check.
 2. isEmpty() & pos() & current():
    - pos < length vs pos >= length.
    - current() returns EOF ((char) -1) when empty, character at pos otherwise.
 3. consume() & unconsume() & advance():
    - consume() returns EOF when empty, advances pos.
    - unconsume() decrements pos.
    - advance() increments pos.
 4. mark() & rewindToMark():
    - pos caching and restoration to cached mark.
 5. consumeTo(char) & consumeTo(String):
    - Target found: consumed substring before match, pos updated to target offset.
    - Target not found: consumes to end of buffer, pos set to length.
 6. consumeToAny(char...):
    - Match found: returns substring, pos placed at match.
    - Match not found: consumes to end of buffer.
    - Buffer empty or immediate match: returns empty string.
 7. Sequence Parsers (consumeLetterSequence, consumeHexSequence, consumeDigitSequence):
    - Letters: 'A'-'Z', 'a'-'z', boundary chars ('@', '[', '`', '{').
    - Hex: '0'-'9', 'A'-'F', 'a'-'f', non-hex ('G', 'g', '/', ':').
    - Digits: '0'-'9', non-digits ('/', ':').
 8. Predicate Matchers (matches, matchesIgnoreCase, matchesAny, matchesLetter, matchesDigit):
    - Exact match, case insensitive match, empty checks, character bounds.
 9. matchConsume & matchConsumeIgnoreCase:
    - Match success: advances pos by sequence length, returns true.
    - Match fail: leaves pos unchanged, returns false.
 10. containsIgnoreCase:
    - Case sensitivity check: looks for lowerCase and upperCase versions.
 ---------------------------------------------------------------------------------------------------
*/

public class CharacterReaderGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicConsumeAndAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());

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
    }

    @Test(timeout = 4000)
    public void testUnconsume() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume();
        assertEquals(1, reader.pos());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    @Test(timeout = 4000)
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // at 'b' (pos 1)
        reader.mark();

        reader.consume(); // at 'c' (pos 2)
        reader.consume(); // at 'd' (pos 3)
        assertEquals(3, reader.pos());

        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("foo#bar");
        String consumed = reader.consumeTo('#');
        assertEquals("foo", consumed);
        assertEquals(3, reader.pos());
        assertEquals('#', reader.current());

        // consumeTo when target not present -> consumes to end
        String rest = reader.consumeTo('z');
        assertEquals("#bar", rest);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToString() {
        CharacterReader reader = new CharacterReader("<div>content</div>");
        String consumed = reader.consumeTo("content");
        assertEquals("<div>", consumed);
        assertEquals(5, reader.pos());
        assertEquals('c', reader.current());

        String notFound = reader.consumeTo("missing");
        assertEquals("content</div>", notFound);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("one=two&three");
        String token1 = reader.consumeToAny('=', '&');
        assertEquals("one", token1);
        assertEquals('=', reader.current());
        reader.consume(); // skip '='

        String token2 = reader.consumeToAny('&');
        assertEquals("two", token2);
        assertEquals('&', reader.current());
        reader.consume(); // skip '&'

        String token3 = reader.consumeToAny(';', '!');
        assertEquals("three", token3);
        assertTrue(reader.isEmpty());

        // empty reader returns empty string
        assertEquals("", reader.consumeToAny('x'));
    }

    @Test(timeout = 4000)
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("start_middle_end");
        reader.consumeTo('_');
        String tail = reader.consumeToEnd();
        assertEquals("_middle_end", tail);
        assertTrue(reader.isEmpty());
        assertEquals("", reader.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testMatchConsume() {
        CharacterReader reader = new CharacterReader("http://example.com");
        assertFalse(reader.matchConsume("ftp://"));
        assertEquals(0, reader.pos());

        assertTrue(reader.matchConsume("http://"));
        assertEquals(7, reader.pos());
        assertEquals('e', reader.current());

        assertFalse(reader.matchConsumeIgnoreCase("EXAMPLE.ORG"));
        assertEquals(7, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase("EXAMPLE.COM"));
        assertEquals(18, reader.pos());
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("<html><title>Sample</TITLE></style>");
        assertTrue(reader.containsIgnoreCase("</title>"));
        assertTrue(reader.containsIgnoreCase("</TITLE>"));
        assertTrue(reader.containsIgnoreCase("</style>"));
        assertFalse(reader.containsIgnoreCase("</head>"));

        reader.consumeTo("Sample");
        // containsIgnoreCase matches only all-lowercase or all-uppercase forms per specification
        assertTrue(reader.containsIgnoreCase("</title>"));
        assertFalse(reader.containsIgnoreCase("<html>"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(1, reader.pos()); // pos increments even when empty
        assertTrue(reader.isEmpty());
        assertEquals("", reader.consumeToEnd());
        assertEquals("", reader.consumeLetterSequence());
        assertEquals("", reader.consumeDigitSequence());
        assertEquals("", reader.consumeHexSequence());
        assertFalse(reader.matches('a'));
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertFalse(reader.matchesAny('a', 'b'));
        assertEquals("", reader.toString());
    }

    @Test(timeout = 4000)
    public void testNewlinesNormalization() {
        // \r\n -> \n, standalone \r -> \n
        CharacterReader reader = new CharacterReader("Line1\r\nLine2\rLine3\nLine4");
        assertEquals("Line1\nLine2\nLine3\nLine4", reader.toString());
    }

    @Test(timeout = 4000)
    public void testLetterBoundaries() {
        // Boundary characters: '@' (64), 'A' (65), 'Z' (90), '[' (91), '`' (96), 'a' (97), 'z' (122), '{' (123)
        CharacterReader reader = new CharacterReader("@AZ[`az{");

        assertFalse(reader.matchesLetter());
        assertEquals("", reader.consumeLetterSequence());
        reader.consume(); // skip '@'

        assertTrue(reader.matchesLetter());
        assertEquals("AZ", reader.consumeLetterSequence());
        assertEquals('[', reader.current());

        assertFalse(reader.matchesLetter());
        reader.consume(); // skip '['
        assertFalse(reader.matchesLetter());
        reader.consume(); // skip '`'

        assertTrue(reader.matchesLetter());
        assertEquals("az", reader.consumeLetterSequence());
        assertEquals('{', reader.current());
        assertFalse(reader.matchesLetter());
    }

    @Test(timeout = 4000)
    public void testDigitBoundaries() {
        // Boundary characters: '/' (47), '0' (48), '9' (57), ':' (58)
        CharacterReader reader = new CharacterReader("/0123456789:");

        assertFalse(reader.matchesDigit());
        assertEquals("", reader.consumeDigitSequence());
        reader.consume(); // skip '/'

        assertTrue(reader.matchesDigit());
        assertEquals("0123456789", reader.consumeDigitSequence());
        assertEquals(':', reader.current());
        assertFalse(reader.matchesDigit());
    }

    @Test(timeout = 4000)
    public void testHexBoundaries() {
        // Boundaries: 0-9, A-F, a-f, invalid chars like '/', ':', '@', 'G', '`', 'g'
        CharacterReader reader = new CharacterReader("09AFaf/:@G`g");
        assertEquals("09AFaf", reader.consumeHexSequence());
        assertEquals('/', reader.current());
        assertEquals("", reader.consumeHexSequence());
    }

    @Test(timeout = 4000)
    public void testMatchesAndMatchesAny() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('h'));
        assertTrue(reader.matches("Hell"));
        assertFalse(reader.matches("hell"));
        assertTrue(reader.matchesIgnoreCase("hell"));
        assertTrue(reader.matchesIgnoreCase("HELL"));

        assertTrue(reader.matchesAny('x', 'H', 'z'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));
        assertFalse(reader.matchesAny()); // empty varargs
    }

    @Test(timeout = 4000)
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("Test");
        // CharacterReader.consumeAsString implementation returns substring(pos, pos++)
        // In Java evaluation order, pos is evaluated before pos++, returning "" and advancing pos
        assertEquals("", reader.consumeAsString());
        assertEquals(1, reader.pos());
        assertEquals('e', reader.current());
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertEquals("Hello World", reader.toString());
        reader.consumeTo(' ');
        reader.consume(); // skip ' '
        assertEquals("World", reader.toString());
        reader.consumeToEnd();
        assertEquals("", reader.toString());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets known Defect: org.jsoup.nodes.EntitiesTest::letterDigitEntities
     * Failure: expected:<[&sup1;&sup2;&sup3;&frac14;&frac12;&]frac34;> but was:<[⊃1;⊃2;⊃3;&amp;frac14;&amp;frac12;&amp;]frac34;>
     * Root Cause: Named entity tokens containing letters followed by digits (e.g., &sup1;, &frac14;)
     * were improperly consumed only up to letter boundaries, leaving trailing digits unconsumed and
     * erroneously matching prefixes (e.g., "sup" -> "⊃" + "1;").
     */
    @Test(timeout = 4000)
    public void testLetterDigitEntitiesDefect() {
        String html = "&sup1;&sup2;&sup3;&frac14;&frac12;&frac34;";
        assertEquals("¹²³¼½¾", org.jsoup.nodes.Entities.unescape(html));
    }

    @Test(timeout = 4000)
    public void testLetterSequenceWithDigitBoundary() {
        // CharacterReader's consumeLetterSequence halts at digits
        CharacterReader reader = new CharacterReader("sup1;");
        String letters = reader.consumeLetterSequence();
        assertEquals("sup", letters);
        assertEquals('1', reader.current());
        assertEquals("1;", reader.consumeToEnd());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullGuard() {
        new CharacterReader(null);
    }

    @Test(timeout = 4000)
    public void testConsumeWhenAlreadyEmptyReturnsEof() {
        CharacterReader reader = new CharacterReader("x");
        assertEquals('x', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & State Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testStatePreservationAcrossMultipleRewinds() {
        CharacterReader reader = new CharacterReader("0123456789");
        reader.advance(); // at 1
        reader.advance(); // at 2
        reader.mark();

        reader.consumeTo('7');
        assertEquals(7, reader.pos());
        reader.rewindToMark();
        assertEquals(2, reader.pos());
        assertEquals('2', reader.current());

        reader.advance(); // at 3
        reader.rewindToMark();
        assertEquals(2, reader.pos());
        assertEquals('2', reader.current());
    }
}