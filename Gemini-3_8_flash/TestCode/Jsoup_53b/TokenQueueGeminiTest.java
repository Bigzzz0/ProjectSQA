/* [Branch & Defect Analysis Matrix]
 * Targets: org.jsoup.parser.TokenQueue
 *
 * 1. Constructor & Preconditions:
 *    - TokenQueue(String data): null check (Validate.notNull throws IllegalArgumentException), empty string, normal text.
 *
 * 2. State & Peek / Read Operations:
 *    - isEmpty(): remainingLength() == 0 vs remainingLength() > 0.
 *    - peek(): empty (returns 0) vs non-empty (returns char at pos).
 *    - toString(): returns remaining string from current pos.
 *    - remainder(): returns remaining string and advances pos to length.
 *
 * 3. Mutation & Prepends:
 *    - addFirst(Character): boxing helper forwards to addFirst(String).
 *    - addFirst(String): prepends to remaining queue, resets pos to 0.
 *    - advance(): empty queue (no-op) vs non-empty (pos++).
 *    - consume(): advances pos, returns char; boundary at end of queue.
 *    - consume(String): matches prefix (advances pos by len) vs mismatch (IllegalStateException).
 *
 * 4. Matching Operations:
 *    - matches(String): case-insensitive region match true/false.
 *    - matchesCS(String): case-sensitive startsWith match true/false.
 *    - matchesAny(String...): iterates strings; returns true on first match, false otherwise.
 *    - matchesAny(char...): empty queue check; iterates chars; true if match at pos, false if none.
 *    - matchesStartTag(): checks remainingLength >= 2, '<' char, Character.isLetter(pos+1); covers boundary < 2, non-tag '<1', '</', etc.
 *    - matchChomp(String): matches -> consumes string and returns true; mismatch -> returns false without advancing.
 *    - matchesWhitespace(): empty vs whitespace vs non-whitespace.
 *    - matchesWord(): empty vs letter/digit vs symbol/punctuation.
 *
 * 5. Consuming & Chomping Substrings:
 *    - consumeTo(String): substring found (consumes up to offset) vs not found (returns remainder).
 *    - consumeToIgnoreCase(String):
 *        * canScan path: non-cased first char (e.g., symbols, digits) exercising skip == 0, skip < 0, skip > 0.
 *        * !canScan path: cased letter first char, step-by-step advance.
 *    - consumeToAny(String...): iterates chars until matchesAny(seq) is true or queue empty.
 *    - chompTo(String): consumeTo + matchChomp.
 *    - chompToIgnoreCase(String): consumeToIgnoreCase + matchChomp.
 *    - consumeWhitespace(): runs through all contiguous whitespace, returns true if seen, false if none.
 *    - consumeWord(): letters and digits run.
 *    - consumeTagName(): word or ':', '_', '-'.
 *    - consumeElementSelector(): word or '|', '_', '-'.
 *    - consumeCssIdentifier(): word or '-', '_'.
 *    - consumeAttributeKey(): word or '-', '_', ':'.
 *
 * 6. Balanced Parentheses / Quotes Chomping & Defect Targeting:
 *    - chompBalanced(char open, char close): nested depths, escaped characters (ESC = '\\').
 *    - KNOWN DEFECT (Defects4J): chompBalanced handling of quotes containing closing brackets.
 *      E.g., `[data='End]']` - closing bracket inside quotes should not terminate balanced scan prematurely.
 *
 * 7. Unescape Utility:
 *    - unescape(String): single backslash, consecutive backslashes ("\\\\"), regular chars.
 */

package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenQueueGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicQueueLifecycleAndState() {
        TokenQueue queue = new TokenQueue("HelloWorld");
        assertFalse(queue.isEmpty());
        assertEquals('H', queue.peek());
        assertEquals("HelloWorld", queue.toString());

        char c = queue.consume();
        assertEquals('H', c);
        assertEquals('e', queue.peek());
        assertEquals("elloWorld", queue.toString());

        queue.advance();
        assertEquals('l', queue.peek());

        String remainder = queue.remainder();
        assertEquals("loWorld", remainder);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.peek());
        assertEquals("", queue.toString());
        assertEquals("", queue.remainder());
    }

    @Test(timeout = 4000)
    public void testAddFirstCharacterAndString() {
        TokenQueue queue = new TokenQueue("World");
        queue.addFirst(Character.valueOf(' '));
        assertEquals(" World", queue.toString());

        queue.addFirst("Hello");
        assertEquals("Hello World", queue.toString());
        assertEquals('H', queue.peek());

        queue.consume(); // 'H'
        queue.consume(); // 'e'
        assertEquals("llo World", queue.toString());

        // Prepending resets pos to 0
        queue.addFirst("Re");
        assertEquals("Rello World", queue.toString());
    }

    @Test(timeout = 4000)
    public void testMatchingOperations() {
        TokenQueue queue = new TokenQueue("One Two Three");

        assertTrue(queue.matches("one"));
        assertTrue(queue.matches("ONE"));
        assertFalse(queue.matches("Two"));

        assertTrue(queue.matchesCS("One"));
        assertFalse(queue.matchesCS("one"));

        assertTrue(queue.matchesAny("foo", "one", "bar"));
        assertFalse(queue.matchesAny("foo", "bar"));

        assertTrue(queue.matchesAny('X', 'o', 'O'));
        assertFalse(queue.matchesAny('X', 'Y', 'Z'));

        assertTrue(queue.matchesWord());
        assertFalse(queue.matchesWhitespace());
    }

    @Test(timeout = 4000)
    public void testMatchChomp() {
        TokenQueue queue = new TokenQueue("abcdef");
        assertFalse(queue.matchChomp("xyz"));
        assertEquals("abcdef", queue.toString());

        assertTrue(queue.matchChomp("ABC"));
        assertEquals("def", queue.toString());

        assertTrue(queue.matchChomp("def"));
        assertTrue(queue.isEmpty());
        assertFalse(queue.matchChomp("def"));
    }

    @Test(timeout = 4000)
    public void testMatchesStartTag() {
        assertTrue(new TokenQueue("<div>").matchesStartTag());
        assertTrue(new TokenQueue("<a href=''>").matchesStartTag());
        assertTrue(new TokenQueue("<Z>").matchesStartTag());

        assertFalse(new TokenQueue("</div>").matchesStartTag());
        assertFalse(new TokenQueue("<!-- comment -->").matchesStartTag());
        assertFalse(new TokenQueue("<?xml>").matchesStartTag());
        assertFalse(new TokenQueue("<1tag>").matchesStartTag());
        assertFalse(new TokenQueue("<").matchesStartTag());
        assertFalse(new TokenQueue("").matchesStartTag());
        assertFalse(new TokenQueue("div").matchesStartTag());
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespaceAndWords() {
        TokenQueue queue = new TokenQueue("   \t\n  hello123_world  ");
        assertTrue(queue.consumeWhitespace());
        assertFalse(queue.consumeWhitespace());

        String word = queue.consumeWord();
        assertEquals("hello123", word);

        assertEquals('_', queue.peek());
        queue.advance();

        assertEquals("world", queue.consumeWord());
        assertTrue(queue.consumeWhitespace());
        assertTrue(queue.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeTagNameAndVariants() {
        TokenQueue queue = new TokenQueue("ns:tag_name-123 remaining");
        assertEquals("ns:tag_name-123", queue.consumeTagName());
        assertEquals(" remaining", queue.toString());

        TokenQueue cssIdQueue = new TokenQueue("my-id_123:pseudo");
        assertEquals("my-id_123", cssIdQueue.consumeCssIdentifier());
        assertEquals(":pseudo", cssIdQueue.toString());

        TokenQueue elQueue = new TokenQueue("ns|tag_name-456");
        assertEquals("ns|tag_name-456", elQueue.consumeElementSelector());

        TokenQueue attrQueue = new TokenQueue("xml:id_attr-1 remainder");
        assertEquals("xml:id_attr-1", attrQueue.consumeAttributeKey());
    }

    @Test(timeout = 4000)
    public void testConsumeToAndChompTo() {
        TokenQueue q1 = new TokenQueue("foo:bar;baz");
        assertEquals("foo", q1.consumeTo(":"));
        assertEquals(":bar;baz", q1.toString());

        TokenQueue q2 = new TokenQueue("foo:bar;baz");
        assertEquals("foo", q2.chompTo(":"));
        assertEquals("bar;baz", q2.toString());

        // Target not found -> consumes to end
        TokenQueue q3 = new TokenQueue("alpha beta");
        assertEquals("alpha beta", q3.consumeTo("gamma"));
        assertTrue(q3.isEmpty());

        TokenQueue q4 = new TokenQueue("alpha beta");
        assertEquals("alpha beta", q4.chompTo("gamma"));
        assertTrue(q4.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase() {
        // canScan = true path (non-cased first character e.g. '=')
        TokenQueue q1 = new TokenQueue("data=test");
        assertEquals("data", q1.consumeToIgnoreCase("="));
        assertEquals("=test", q1.toString());

        // canScan = true with skip > 0
        TokenQueue q2 = new TokenQueue("abc123def");
        assertEquals("abc", q2.consumeToIgnoreCase("123"));

        // canScan = true with skip < 0 (not found)
        TokenQueue q3 = new TokenQueue("abcdef");
        assertEquals("abcdef", q3.consumeToIgnoreCase("123"));
        assertTrue(q3.isEmpty());

        // canScan = true with skip == 0 but string mismatch (forces pos++)
        TokenQueue q4 = new TokenQueue("12a123");
        assertEquals("12a", q4.consumeToIgnoreCase("123"));
        assertEquals("123", q4.toString());

        // !canScan path (cased first character e.g. 'f')
        TokenQueue q5 = new TokenQueue("targetFOObar");
        assertEquals("target", q5.consumeToIgnoreCase("foo"));
        assertEquals("FOObar", q5.toString());

        TokenQueue q6 = new TokenQueue("targetFOObar");
        assertEquals("target", q6.chompToIgnoreCase("foo"));
        assertEquals("bar", q6.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny() {
        TokenQueue queue = new TokenQueue("hello;world,test");
        assertEquals("hello", queue.consumeToAny(";", ","));
        assertEquals(";world,test", queue.toString());
        queue.advance();
        assertEquals("world", queue.consumeToAny(";", ","));
        assertEquals(",test", queue.toString());
    }

    @Test(timeout = 4000)
    public void testBalancedParenthesesStandard() {
        TokenQueue queue = new TokenQueue("(one (two) three) four");
        String balanced = queue.chompBalanced('(', ')');
        assertEquals("one (two) three", balanced);
        assertEquals(" four", queue.toString());

        TokenQueue escapedQueue = new TokenQueue("(one \\(two\\) three) four");
        assertEquals("one \\(two\\) three", escapedQueue.chompBalanced('(', ')'));
        assertEquals(" four", escapedQueue.toString());
    }

    @Test(timeout = 4000)
    public void testUnescape() {
        assertEquals("hello world", TokenQueue.unescape("hello world"));
        assertEquals("foo\\bar", TokenQueue.unescape("foo\\\\bar"));
        assertEquals("quote'mark", TokenQueue.unescape("quote\\'mark"));
        assertEquals("", TokenQueue.unescape(""));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyQueueBehaviors() {
        TokenQueue empty = new TokenQueue("");
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.peek());
        assertEquals("", empty.toString());
        assertEquals("", empty.remainder());
        assertEquals("", empty.consumeWord());
        assertEquals("", empty.consumeTagName());
        assertEquals("", empty.consumeElementSelector());
        assertEquals("", empty.consumeCssIdentifier());
        assertEquals("", empty.consumeAttributeKey());
        assertFalse(empty.consumeWhitespace());
        assertFalse(empty.matchesWhitespace());
        assertFalse(empty.matchesWord());
        assertFalse(empty.matchesStartTag());
        assertFalse(empty.matchesAny('a', 'b'));
        assertFalse(empty.matchesAny("a", "b"));
        assertEquals("", empty.consumeTo("any"));
        assertEquals("", empty.consumeToIgnoreCase("any"));
        assertEquals("", empty.consumeToAny("any"));
        assertEquals("", empty.chompBalanced('(', ')'));

        // Advance on empty queue is safe no-op
        empty.advance();
        assertTrue(empty.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalancedUnclosedOrEmpty() {
        TokenQueue q1 = new TokenQueue("no open paren here");
        assertEquals("", q1.chompBalanced('(', ')'));
        assertTrue(q1.isEmpty());

        TokenQueue q2 = new TokenQueue("(unclosed paren");
        assertEquals("", q2.chompBalanced('(', ')'));
        assertTrue(q2.isEmpty());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J issue where chompBalanced terminates early when an unbalanced
     * close character is contained inside quotes within the balanced construct.
     * E.g. [data='End]'] -> expected contents inside brackets: "data='End]'"
     */
    @Test(timeout = 4000)
    public void testChompBalancedWithQuotesContainingCloseBracket() {
        TokenQueue queue = new TokenQueue("[data='End]']");
        String balanced = queue.chompBalanced('[', ']');
        assertEquals("data='End]'", balanced);
        assertTrue(queue.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalancedWithDoubleQuotesContainingCloseBracket() {
        TokenQueue queue = new TokenQueue("[data=\"End]\"]");
        String balanced = queue.chompBalanced('[', ']');
        assertEquals("data=\"End]\"", balanced);
        assertTrue(queue.isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullThrowsException() {
        new TokenQueue(null);
    }

    @Test(timeout = 4000)
    public void testConsumeStringExactMatch() {
        TokenQueue queue = new TokenQueue("Testing");
        queue.consume("Test");
        assertEquals("ing", queue.toString());
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testConsumeStringMismatchThrowsException() {
        TokenQueue queue = new TokenQueue("Testing");
        queue.consume("Failed");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testConsumeStringLongerThanRemainingThrowsException() {
        TokenQueue queue = new TokenQueue("Short");
        queue.consume("ShorterLonger");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testConsumeOnEmptyQueueThrowsException() {
        TokenQueue queue = new TokenQueue("");
        queue.consume();
    }
}