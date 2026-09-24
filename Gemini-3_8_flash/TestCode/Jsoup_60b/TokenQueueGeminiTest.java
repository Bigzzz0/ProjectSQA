package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.parser.TokenQueue
 * Target Environment: Java 8 / JUnit 4
 *
 * Decision / Branch Matrix:
 * - TokenQueue(String):
 *     - data != null -> init queue, pos = 0
 *     - data == null -> Validate.notNull throws IllegalArgumentException
 * - isEmpty():
 *     - remainingLength() == 0 (true)
 *     - remainingLength() > 0 (false)
 * - peek():
 *     - isEmpty() == true -> returns 0
 *     - isEmpty() == false -> returns queue.charAt(pos)
 * - addFirst(Character) / addFirst(String):
 *     - prepends string to unconsumed queue, resets pos to 0
 * - matches(String):
 *     - regionMatches case-insensitive: true / false
 * - matchesCS(String):
 *     - startsWith at pos case-sensitive: true / false
 * - matchesAny(String...):
 *     - matches any element in array -> true
 *     - matches none / empty array -> false
 * - matchesAny(char...):
 *     - isEmpty() == true -> false
 *     - char matches any element -> true
 *     - char matches none -> false
 * - matchesStartTag():
 *     - remainingLength < 2 -> false
 *     - remainingLength >= 2 && charAt(pos) != '<' -> false
 *     - remainingLength >= 2 && charAt(pos) == '<' && !isLetter(charAt(pos+1)) -> false
 *     - remainingLength >= 2 && charAt(pos) == '<' && isLetter(charAt(pos+1)) -> true
 * - matchChomp(String):
 *     - matches(seq) == true -> pos += seq.length(), returns true
 *     - matches(seq) == false -> pos unchanged, returns false
 * - matchesWhitespace() / matchesWord():
 *     - isEmpty() -> false
 *     - char evaluation -> true / false
 * - advance():
 *     - isEmpty() -> no-op
 *     - !isEmpty() -> pos++
 * - consume():
 *     - returns queue.charAt(pos++)
 * - consume(String):
 *     - !matches(seq) -> throws IllegalStateException
 *     - len > remainingLength() -> throws IllegalStateException
 *     - matches(seq) -> pos += len
 * - consumeTo(String):
 *     - offset != -1 -> substrings and advances pos
 *     - offset == -1 -> returns remainder(), queue emptied
 * - consumeToIgnoreCase(String):
 *     - canScan (non-cased first char):
 *         - skip == 0 -> pos++
 *         - skip < 0 -> pos = queue.length()
 *         - skip > 0 -> pos += skip
 *     - !canScan (cased letter first char): pos++
 *     - matches(seq) -> break
 *     - isEmpty() -> break
 * - consumeToAny(String...):
 *     - loop until isEmpty() or matchesAny(seq)
 * - chompTo(String) / chompToIgnoreCase(String):
 *     - consumeTo + matchChomp
 * - chompBalanced(char open, char close):
 *     - balanced outer pair with clean nested elements
 *     - quoting logic (' or "):
 *         - inside quotes, close/open brackets should not alter depth
 *         - defect condition: apostrophe inside text (e.g. "foo'bar") toggles inQuote
 *           causing subsequent closing brackets to be skipped and premature/truncated consumption
 *         - defect condition: escaped quote inside quotes (\') failing because 'last' is not
 *           updated due to premature continue statement
 * - unescape(String):
 *     - single backslash vs escaped backslash (\\\\)
 * - CSS token consumers:
 *     - consumeWhitespace(), consumeWord(), consumeTagName(), consumeElementSelector(),
 *       consumeCssIdentifier(), consumeAttributeKey()
 */
public class TokenQueueGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPeekAndConsumeFlow() {
        TokenQueue queue = new TokenQueue("abc");
        assertFalse(queue.isEmpty());
        assertEquals('a', queue.peek());
        assertEquals('a', queue.consume());
        assertEquals('b', queue.peek());
        assertEquals('b', queue.consume());
        assertEquals('c', queue.peek());
        assertEquals('c', queue.consume());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.peek());
    }

    @Test(timeout = 4000)
    public void testAddFirstCharacterAndString() {
        TokenQueue queue = new TokenQueue("world");
        queue.addFirst(' ');
        queue.addFirst("hello");
        assertEquals("hello world", queue.remainder());
        assertTrue(queue.isEmpty());

        // Test addFirst after partial consumption
        TokenQueue queue2 = new TokenQueue("12345");
        assertEquals('1', queue2.consume());
        assertEquals('2', queue2.consume());
        queue2.addFirst("xy");
        assertEquals("xy345", queue2.remainder());
    }

    @Test(timeout = 4000)
    public void testCaseInsensitiveAndSensitiveMatches() {
        TokenQueue queue = new TokenQueue("HelloWorld");
        assertTrue(queue.matches("hello"));
        assertTrue(queue.matches("HELLO"));
        assertTrue(queue.matchesCS("Hello"));
        assertFalse(queue.matchesCS("hello"));

        assertTrue(queue.matchesAny("none", "HELLO", "other"));
        assertFalse(queue.matchesAny("foo", "bar"));
        assertFalse(queue.matchesAny());

        assertTrue(queue.matchesAny('H', 'x', 'y'));
        assertFalse(queue.matchesAny('h', 'x', 'y'));
    }

    @Test(timeout = 4000)
    public void testMatchChomp() {
        TokenQueue queue = new TokenQueue("abcdef");
        assertTrue(queue.matchChomp("AB"));
        assertEquals("cdef", queue.remainder());

        TokenQueue queue2 = new TokenQueue("abcdef");
        assertFalse(queue2.matchChomp("xyz"));
        assertEquals("abcdef", queue2.remainder());
    }

    @Test(timeout = 4000)
    public void testMatchesStartTag() {
        assertTrue(new TokenQueue("<div>").matchesStartTag());
        assertTrue(new TokenQueue("<H1>").matchesStartTag());
        assertTrue(new TokenQueue("<p").matchesStartTag());

        assertFalse(new TokenQueue("< 123").matchesStartTag());
        assertFalse(new TokenQueue("</p>").matchesStartTag());
        assertFalse(new TokenQueue("<?xml").matchesStartTag());
        assertFalse(new TokenQueue("<!DOCTYPE").matchesStartTag());
        assertFalse(new TokenQueue("<1tag").matchesStartTag());
        assertFalse(new TokenQueue("<").matchesStartTag());
        assertFalse(new TokenQueue("").matchesStartTag());
        assertFalse(new TokenQueue("div").matchesStartTag());
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespaceAndAdvance() {
        TokenQueue queue = new TokenQueue("   \t\r\n\fhello");
        assertTrue(queue.matchesWhitespace());
        assertTrue(queue.consumeWhitespace());
        assertFalse(queue.matchesWhitespace());
        assertFalse(queue.consumeWhitespace());
        assertEquals("hello", queue.remainder());

        TokenQueue queue2 = new TokenQueue("abc");
        queue2.advance();
        assertEquals("bc", queue2.remainder());
        queue2.advance(); // on empty queue
        queue2.advance();
        queue2.advance();
        assertTrue(queue2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeExpectedSequence() {
        TokenQueue queue = new TokenQueue("HelloWorld");
        queue.consume("hello"); // case-insensitive
        assertEquals("World", queue.remainder());
    }

    @Test(timeout = 4000)
    public void testConsumeToAndChompTo() {
        TokenQueue queue = new TokenQueue("key:value;other");
        String consumed = queue.consumeTo(":");
        assertEquals("key", consumed);
        assertEquals(':', queue.peek());

        String chomp = queue.chompTo(";");
        assertEquals(":value", chomp);
        assertEquals("other", queue.remainder());

        // Target not found consumes to end
        TokenQueue queue2 = new TokenQueue("no-delimiters");
        assertEquals("no-delimiters", queue2.consumeTo(":"));
        assertTrue(queue2.isEmpty());

        TokenQueue queue3 = new TokenQueue("no-delimiters");
        assertEquals("no-delimiters", queue3.chompTo(":"));
        assertTrue(queue3.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseAndChompToIgnoreCase() {
        // canScan == false (letter initial in seq)
        TokenQueue queue1 = new TokenQueue("oneTWOthree");
        String res1 = queue1.consumeToIgnoreCase("two");
        assertEquals("one", res1);
        assertEquals("TWOthree", queue1.toString());

        // canScan == true (digit or symbol initial), skip > 0
        TokenQueue queue2 = new TokenQueue("abc:def");
        String res2 = queue2.chompToIgnoreCase(":def");
        assertEquals("abc", res2);
        assertTrue(queue2.isEmpty());

        // canScan == true, skip == 0 (matches initial char but not full sequence)
        TokenQueue queue3 = new TokenQueue("100234");
        String res3 = queue3.consumeToIgnoreCase("123");
        assertEquals("100234", res3);
        assertTrue(queue3.isEmpty());

        // canScan == true, skip < 0 (initial char not found at all)
        TokenQueue queue4 = new TokenQueue("abcdef");
        String res4 = queue4.consumeToIgnoreCase("999");
        assertEquals("abcdef", res4);
        assertTrue(queue4.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        TokenQueue queue = new TokenQueue("foo[bar]baz");
        assertEquals("foo", queue.consumeToAny("[", "]", "{", "}"));
        assertEquals('[', queue.peek());

        TokenQueue queue2 = new TokenQueue("plainText");
        assertEquals("plainText", queue2.consumeToAny("!", "@"));
        assertTrue(queue2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCssAndHtmlTokenConsumers() {
        TokenQueue wordQueue = new TokenQueue("Word123_456!rest");
        assertEquals("Word123", wordQueue.consumeWord());
        assertEquals("_456!rest", wordQueue.remainder());

        TokenQueue tagQueue = new TokenQueue("ns:tag_name-one attr='x'");
        assertEquals("ns:tag_name-one", tagQueue.consumeTagName());
        assertEquals(" attr='x'", tagQueue.remainder());

        TokenQueue elemQueue = new TokenQueue("*|elem_one fb|name");
        assertEquals("*|elem_one", elemQueue.consumeElementSelector());
        elemQueue.consumeWhitespace();
        assertEquals("fb|name", elemQueue.consumeElementSelector());

        TokenQueue idQueue = new TokenQueue("my-id_123.class");
        assertEquals("my-id_123", idQueue.consumeCssIdentifier());
        assertEquals(".class", idQueue.remainder());

        TokenQueue attrQueue = new TokenQueue("xml:attr-name_1='val'");
        assertEquals("xml:attr-name_1", attrQueue.consumeAttributeKey());
        assertEquals("='val'", attrQueue.remainder());
    }

    @Test(timeout = 4000)
    public void testUnescape() {
        assertEquals("hello world", TokenQueue.unescape("hello\\ world"));
        assertEquals("slash\\here", TokenQueue.unescape("slash\\\\here"));
        assertEquals("multiple\\\\slashes", TokenQueue.unescape("multiple\\\\\\\\slashes"));
        assertEquals("plain text", TokenQueue.unescape("plain text"));
        assertEquals("", TokenQueue.unescape(""));
    }

    @Test(timeout = 4000)
    public void testStandardChompBalanced() {
        TokenQueue queue = new TokenQueue("(nested (inner) value) outside");
        String balanced = queue.chompBalanced('(', ')');
        assertEquals("nested (inner) value", balanced);
        assertEquals(" outside", queue.remainder());

        TokenQueue queueQuotes = new TokenQueue("(one 'two)' three) outside");
        assertEquals("one 'two)' three", queueQuotes.chompBalanced('(', ')'));
        assertEquals(" outside", queueQuotes.remainder());

        TokenQueue queueDoubleQuotes = new TokenQueue("(one \"two)\" three) outside");
        assertEquals("one \"two)\" three", queueDoubleQuotes.chompBalanced('(', ')'));
        assertEquals(" outside", queueDoubleQuotes.remainder());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyQueueOperations() {
        TokenQueue queue = new TokenQueue("");
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.peek());
        assertEquals("", queue.remainder());
        assertEquals("", queue.toString());
        assertFalse(queue.matches("a"));
        assertFalse(queue.matchesCS("a"));
        assertFalse(queue.matchesAny("a", "b"));
        assertFalse(queue.matchesAny('a', 'b'));
        assertFalse(queue.matchesWhitespace());
        assertFalse(queue.matchesWord());
        assertFalse(queue.matchesStartTag());
        assertFalse(queue.consumeWhitespace());
        assertEquals("", queue.consumeWord());
        assertEquals("", queue.consumeTagName());
        assertEquals("", queue.consumeElementSelector());
        assertEquals("", queue.consumeCssIdentifier());
        assertEquals("", queue.consumeAttributeKey());
        assertEquals("", queue.consumeTo("any"));
        assertEquals("", queue.consumeToIgnoreCase("any"));
        assertEquals("", queue.consumeToAny("any"));
        assertEquals("", queue.chompTo("any"));
        assertEquals("", queue.chompToIgnoreCase("any"));
        assertEquals("", queue.chompBalanced('(', ')'));
    }

    @Test(timeout = 4000)
    public void testChompBalancedUnmatched() {
        TokenQueue queue = new TokenQueue("no opening or closing");
        assertEquals("", queue.chompBalanced('(', ')'));
        assertTrue(queue.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalancedImmediateClose() {
        TokenQueue queue = new TokenQueue("()after");
        assertEquals("", queue.chompBalanced('(', ')'));
        assertEquals("after", queue.remainder());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: QueryParserTest#testParsesSingleQuoteInContains
     *
     * In defective TokenQueue, an unquoted apostrophe/single-quote inside a balanced pair
     * (e.g. "(foo'bar)") prematurely sets inQuote = true and skips the closing delimiter,
     * consuming to EOF or returning truncated tokens ("foo" instead of "foo'bar").
     * This test asserts the expected correct behavior on balanced parsing containing apostrophes.
     */
    @Test(timeout = 4000)
    public void testChompBalancedWithSingleQuoteInContains() {
        TokenQueue queue = new TokenQueue("(foo'bar)");
        String result = queue.chompBalanced('(', ')');
        assertEquals("foo'bar", result);
        assertTrue("Queue should have fully consumed the balanced block", queue.isEmpty());
    }

    /**
     * Target Defect: Escaped quotes inside quoted strings within chompBalanced.
     *
     * In the defective version, when inQuote is true, "continue;" executes before "last = c",
     * leaving "last" as the character before the quote started. Consequently, escaped quotes
     * like \' inside quotes are not recognized as escaped, prematurely terminating inQuote.
     */
    @Test(timeout = 4000)
    public void testChompBalancedWithEscapedQuoteInsideQuotes() {
        TokenQueue queue = new TokenQueue("(one 'two\\'three' four) five");
        String result = queue.chompBalanced('(', ')');
        assertEquals("one 'two\\'three' four", result);
        assertEquals(" five", queue.remainder());
    }

    /**
     * Target Defect: QueryParserTest#exceptionOnUncloseAttribute
     *
     * Evaluates handling of an unclosed delimiter