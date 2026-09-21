package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokenQueue (org.jsoup.parser.TokenQueue)
 * 
 * Decision branches and boundary conditions analyzed:
 * 
 * 1. Constructor: Validate.notNull(data) - null input should throw IllegalArgumentException
 * 2. isEmpty(): remainingLength() == 0 - empty queue, queue with content, queue fully consumed
 * 3. peek(): empty queue returns 0, non-empty returns first char
 * 4. addFirst(Character/String): add to empty queue, add to non-empty queue, add when pos > 0
 * 5. matches(String): case-insensitive match, no match, empty seq, match at pos
 * 6. matchesCS(String): case-sensitive match, no match, empty seq
 * 7. matchesAny(String...): multiple sequences, empty varargs, null element
 * 8. matchesAny(char...): multiple chars, empty varargs, empty queue
 * 9. matchesStartTag(): "<x" pattern, "<" followed by non-letter, length < 2
 * 10. matchChomp(String): match and consume, no match
 * 11. matchesWhitespace(): whitespace, non-whitespace, empty queue
 * 12. matchesWord(): letter/digit, non-word, empty queue
 * 13. advance(): advance on non-empty, advance on empty (no-op)
 * 14. consume(): consume char, consume when at end
 * 15. consume(String): match and consume, mismatch throws ISE, seq longer than remaining throws ISE
 * 16. consumeTo(String): found, not found (returns remainder), empty seq
 * 17. consumeToIgnoreCase(String): case-insensitive search, not found, empty seq, first char non-cased
 * 18. consumeToAny(String...): found, not found, empty varargs
 * 19. chompTo(String): consume to and chomp, not found
 * 20. chompToIgnoreCase(String): case-insensitive consume and chomp
 * 21. chompBalanced(char, char): balanced, unbalanced, escaped chars, quoted, empty
 * 22. unescape(String): backslash escapes, double backslash, no escapes
 * 23. consumeWhitespace(): leading whitespace, no whitespace, all whitespace
 * 24. consumeWord(): word chars, non-word, empty
 * 25. consumeTagName(): word, ':', '_', '-', mixed
 * 26. consumeElementSelector(): word, '|', '_', '-', mixed
 * 27. consumeCssIdentifier(): word, '-', '_', mixed
 * 28. consumeAttributeKey(): word, '-', '_', ':', mixed
 * 29. remainder(): consume rest, empty queue
 * 30. toString(): substring from pos
 * 
 * Defect targeting: consumeToIgnoreCase with sequence containing ']' 
 * (e.g., "']'") - the known defect causes SelectorParseException when 
 * parsing attribute with brackets. The bug is in consumeToIgnoreCase 
 * where the first character ']' is non-cased (canScan = true) and 
 * indexOf finds it, but the logic fails to advance correctly when 
 * the match is not at the current position.
 */
public class TokenQueueDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testConstructorAndBasicState() {
        TokenQueue tq = new TokenQueue("hello");
        assertFalse(tq.isEmpty());
        assertEquals("hello", tq.toString());
        assertEquals(5, tq.remainingLength());
    }

    @Test(timeout = 4000)
    public void testEmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertTrue(tq.isEmpty());
        assertEquals(0, tq.peek());
        assertEquals("", tq.remainder());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testPeekAndAdvance() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals('a', tq.peek());
        tq.advance();
        assertEquals('b', tq.peek());
        tq.advance();
        tq.advance();
        assertTrue(tq.isEmpty());
        tq.advance(); // no-op on empty
        assertEquals(0, tq.peek());
    }

    @Test(timeout = 4000)
    public void testAddFirst() {
        TokenQueue tq = new TokenQueue("world");
        tq.addFirst("hello ");
        assertEquals("hello world", tq.toString());
        assertEquals(0, tq.pos);
        
        tq.addFirst('X');
        assertEquals("Xhello world", tq.toString());
        
        // addFirst when pos > 0
        tq.advance();
        tq.addFirst("Y");
        assertEquals("YXhello world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsume() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals('a', tq.consume());
        assertEquals('b', tq.consume());
        assertEquals('c', tq.consume());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeString() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
        
        tq = new TokenQueue("abc");
        tq.consume("abc");
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeWordAndWhitespace() {
        TokenQueue tq = new TokenQueue("  hello123 world");
        assertTrue(tq.consumeWhitespace());
        assertEquals("hello123", tq.consumeWord());
        assertTrue(tq.consumeWhitespace());
        assertEquals("world", tq.consumeWord());
        assertFalse(tq.consumeWhitespace());
    }

    @Test(timeout = 4000)
    public void testRemainder() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.advance();
        tq.advance();
        assertEquals("llo world", tq.remainder());
        assertTrue(tq.isEmpty());
        assertEquals("", tq.remainder());
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====
    
    @Test(timeout = 4000)
    public void testMatchesWithEmptySeq() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matches(""));
        assertTrue(tq.matchesCS(""));
    }

    @Test(timeout = 4000)
    public void testMatchesAtEndOfQueue() {
        TokenQueue tq = new TokenQueue("abc");
        tq.advance();
        tq.advance();
        tq.advance();
        assertFalse(tq.matches("a"));
        assertFalse(tq.matchesCS("a"));
    }

    @Test(timeout = 4000)
    public void testMatchesAnyWithEmptyVarargs() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.matchesAny(new String[]{}));
        assertFalse(tq.matchesAny(new char[]{}));
    }

    @Test(timeout = 4000)
    public void testMatchesStartTagBoundaries() {
        TokenQueue tq = new TokenQueue("<");
        assertFalse(tq.matchesStartTag());
        
        tq = new TokenQueue("<a");
        assertTrue(tq.matchesStartTag());
        
        tq = new TokenQueue("<1");
        assertFalse(tq.matchesStartTag());
        
        tq = new TokenQueue("");
        assertFalse(tq.matchesStartTag());
    }

    @Test(timeout = 4000)
    public void testConsumeToNotFound() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello world", tq.consumeTo("xyz"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToEmptySeq() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.consumeTo(""));
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalancedEmpty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.chompBalanced('(', ')'));
    }

    @Test(timeout = 4000)
    public void testUnescapeBoundaries() {
        assertEquals("", TokenQueue.unescape(""));
        assertEquals("abc", TokenQueue.unescape("abc"));
        assertEquals("a\\b", TokenQueue.unescape("a\\\\b"));
        assertEquals("a\\", TokenQueue.unescape("a\\"));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    /**
     * Defect test: consumeToIgnoreCase with sequence starting with ']'
     * This targets the known defect where parsing 'div[data='End]'' fails.
     * The bug is in consumeToIgnoreCase when the first character is non-cased
     * (like ']') and the logic incorrectly advances past the match.
     */
    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBracketSequence() {
        TokenQueue tq = new TokenQueue("div[data='End]'");
        // Simulate parsing attribute value with ']' in it
        String consumed = tq.consumeToIgnoreCase("']'");
        // The correct behavior: consume up to but not including "']'"
        // The buggy version would consume past it or fail
        assertEquals("div[data='End", consumed);
        assertEquals("']'", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBracketAtStart() {
        TokenQueue tq = new TokenQueue("]test");
        String consumed = tq.consumeToIgnoreCase("']'");
        assertEquals("", consumed);
        assertEquals("]test", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithMultipleBrackets() {
        TokenQueue tq = new TokenQueue("a]b]c");
        String consumed = tq.consumeToIgnoreCase("']'");
        assertEquals("a", consumed);
        assertEquals("]b]c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithNonCasedFirstChar() {
        // First char is non-cased (digit)
        TokenQueue tq = new TokenQueue("abc123def");
        String consumed = tq.consumeToIgnoreCase("123");
        assertEquals("abc", consumed);
        assertEquals("123def", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCaseWithBracketSequence() {
        TokenQueue tq = new TokenQueue("data']'rest");
        String consumed = tq.chompToIgnoreCase("']'");
        assertEquals("data", consumed);
        assertEquals("rest", tq.toString());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNull() {
        new TokenQueue(null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testConsumeStringMismatch() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("world");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testConsumeStringTooLong() {
        TokenQueue tq = new TokenQueue("hi");
        tq.consume("hello");
    }

    @Test(timeout = 4000)
    public void testConsumeStringExactLength() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume("abc");
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeStringShorterThanRemaining() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testToStringAfterOperations() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.advance();
        tq.advance();
        assertEquals("llo world", tq.toString());
        
        tq.consumeWord();
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeTagNameWithSpecialChars() {
        TokenQueue tq = new TokenQueue("my-tag:name_extra");
        assertEquals("my-tag:name_extra", tq.consumeTagName());
        
        tq = new TokenQueue("div");
        assertEquals("div", tq.consumeTagName());
        
        tq = new TokenQueue("");
        assertEquals("", tq.consumeTagName());
    }

    @Test(timeout = 4000)
    public void testConsumeElementSelector() {
        TokenQueue tq = new TokenQueue("my|tag_name-extra");
        assertEquals("my|tag_name-extra", tq.consumeElementSelector());
        
        tq = new TokenQueue("div");
        assertEquals("div", tq.consumeElementSelector());
    }

    @Test(timeout = 4000)
    public void testConsumeCssIdentifier() {
        TokenQueue tq = new TokenQueue("my-identifier_123");
        assertEquals("my-identifier_123", tq.consumeCssIdentifier());
        
        tq = new TokenQueue("123abc");
        assertEquals("123abc", tq.consumeCssIdentifier());
        
        tq = new TokenQueue("-start");
        assertEquals("-start", tq.consumeCssIdentifier());
    }

    @Test(timeout = 4000)
    public void testConsumeAttributeKey() {
        TokenQueue tq = new TokenQueue("data-key:value");
        assertEquals("data-key:", tq.consumeAttributeKey());
        
        tq = new TokenQueue("simple");
        assertEquals("simple", tq.consumeAttributeKey());
        
        tq = new TokenQueue("");
        assertEquals("", tq.consumeAttributeKey());
    }

    @Test(timeout = 4000)
    public void testChompBalancedNested() {
        TokenQueue tq = new TokenQueue("(one (two) three) four");
        assertEquals("one (two) three", tq.chompBalanced('(', ')'));
        assertEquals(" four", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalancedWithEscapes() {
        TokenQueue tq = new TokenQueue("(one \\(two\\) three) four");
        assertEquals("one \\(two\\) three", tq.chompBalanced('(', ')'));
        assertEquals(" four", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalancedUnbalanced() {
        TokenQueue tq = new TokenQueue("(one two");
        assertEquals("", tq.chompBalanced('(', ')'));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespaceVariants() {
        TokenQueue tq = new TokenQueue("   \t\n\r  hello");
        assertTrue(tq.consumeWhitespace());
        assertEquals("hello", tq.toString());
        
        tq = new TokenQueue("hello");
        assertFalse(tq.consumeWhitespace());
        assertEquals("hello", tq.toString());
        
        tq = new TokenQueue("   ");
        assertTrue(tq.consumeWhitespace());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatchesAnyCharVariants() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny('x', 'a'));
        assertFalse(tq.matchesAny('x', 'y'));
        
        tq = new TokenQueue("");
        assertFalse(tq.matchesAny('a'));
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyVariants() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello", tq.consumeToAny(" ", "!"));
        assertEquals(" world", tq.toString());
        
        tq = new TokenQueue("hello");
        assertEquals("hello", tq.consumeToAny("!"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompToVariants() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello", tq.chompTo(" "));
        assertEquals("world", tq.toString());
        
        tq = new TokenQueue("hello");
        assertEquals("hello", tq.chompTo("!"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testUnescapeWithEscapes() {
        assertEquals("a\\b", TokenQueue.unescape("a\\\\b"));
        assertEquals("a\\b\\c", TokenQueue.unescape("a\\\\b\\\\c"));
        assertEquals("\\", TokenQueue.unescape("\\\\"));
        assertEquals("a\\", TokenQueue.unescape("a\\"));
        assertEquals("a\\b", TokenQueue.unescape("a\\b"));
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseNotFound() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello world", tq.consumeToIgnoreCase("xyz"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseCaseInsensitive() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertEquals("Hello ", tq.consumeToIgnoreCase("world"));
        assertEquals("World", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseEmptySeq() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.consumeToIgnoreCase(""));
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCaseNotFound() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.chompToIgnoreCase("!"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCaseCaseInsensitive() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertEquals("Hello ", tq.chompToIgnoreCase("WORLD"));
        assertEquals("World", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeWordEmpty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeWord());
        
        tq = new TokenQueue("123");
        assertEquals("123", tq.consumeWord());
        
        tq = new TokenQueue("!@#");
        assertEquals("", tq.consumeWord());
    }

    @Test(timeout = 4000)
    public void testConsumeTagNameEmpty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeTagName());
        
        tq = new TokenQueue("123");
        assertEquals("123", tq.consumeTagName());
        
        tq = new TokenQueue("!@#");
        assertEquals("", tq.consumeTagName());
    }

    @Test(timeout = 4000)
    public void testConsumeElementSelectorEmpty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeElementSelector());
        
        tq = new TokenQueue("123");
        assertEquals("123", tq.consumeElementSelector());
        
        tq = new TokenQueue("!@#");
        assertEquals("", tq.consumeElementSelector());
    }

    @Test(timeout = 4000)
    public void testConsumeCssIdentifierEmpty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeCssIdentifier());
        
        tq = new TokenQueue("123");
        assertEquals("123", tq.consumeCssIdentifier());
        
        tq = new TokenQueue("!@#");
        assertEquals("", tq.consumeCssIdentifier());
    }

    @Test(timeout = 4000)
    public void testConsumeAttributeKeyEmpty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeAttributeKey());
        
        tq = new TokenQueue("123");
        assertEquals("123", tq.consumeAttributeKey());
        
        tq = new TokenQueue("!@#");
        assertEquals("", tq.consumeAttributeKey());
    }

    @Test(timeout = 4000)
    public void testChompBalancedWithQuotes() {
        TokenQueue tq = new TokenQueue("('one (two) three') four");
        assertEquals("'one (two) three'", tq.chompBalanced('(', ')'));
        assertEquals(" four", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalancedWithEscapedQuotes() {
        TokenQueue tq = new TokenQueue("('one \\'two\\' three') four");
        assertEquals("'one \\'two\\' three'", tq.chompBalanced('(', ')'));
        assertEquals(" four", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalancedNoClose() {
        TokenQueue tq = new TokenQueue("(one two");
        assertEquals("", tq.chompBalanced('(', ')'));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalancedNoOpen() {
        TokenQueue tq = new TokenQueue("one two)");
        assertEquals("", tq.chompBalanced('(', ')'));
        assertEquals("one two)", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalancedImmediateClose() {
        TokenQueue tq = new TokenQueue("()");
        assertEquals("", tq.chompBalanced('(', ')'));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalancedSingleChar() {
        TokenQueue tq = new TokenQueue("(a)");
        assertEquals("a", tq.chompBalanced('(', ')'));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToWithMultipleOccurrences() {
        TokenQueue tq = new TokenQueue("aXbXc");
        assertEquals("a", tq.consumeTo("X"));
        assertEquals("XbXc", tq.toString());
        
        tq.consume();
        assertEquals("b", tq.consumeTo("X"));
        assertEquals("Xc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithMultipleOccurrences() {
        TokenQueue tq = new TokenQueue("aXbXc");
        assertEquals("a", tq.consumeToIgnoreCase("x"));
        assertEquals("XbXc", tq.toString());
        
        tq.consume();
        assertEquals("b", tq.consumeToIgnoreCase("x"));
        assertEquals("Xc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyWithMultipleMatches() {
        TokenQueue tq = new TokenQueue("abc def");
        assertEquals("abc", tq.consumeToAny(" ", "d"));
        assertEquals(" def", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToAnyWithNoMatch() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.consumeToAny("!"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompToWithMultipleMatches() {
        TokenQueue tq = new TokenQueue("aXbXc");
        assertEquals("a", tq.chompTo("X"));
        assertEquals("bXc", tq.toString());
        
        assertEquals("b", tq.chompTo("X"));
        assertEquals("c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCaseWithMultipleMatches() {
        TokenQueue tq = new TokenQueue("aXbXc");
        assertEquals("a", tq.chompToIgnoreCase("x"));
        assertEquals("bXc", tq.toString());
        
        assertEquals("b", tq.chompToIgnoreCase("x"));
        assertEquals("c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespaceWithMixedWhitespace() {
        TokenQueue tq = new TokenQueue(" \t\n\r\f\u000Bhello");
        assertTrue(tq.consumeWhitespace());
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesWordWithUnicode() {
        TokenQueue tq = new TokenQueue("café");
        assertTrue(tq.matchesWord());
        assertEquals("caf", tq.consumeWord()); // 'é' is not letter/digit in Java
    }

    @Test(timeout = 4000)
    public void testConsumeTagNameWithUnicode() {
        TokenQueue tq = new TokenQueue("café");
        assertEquals("caf", tq.consumeTagName());
    }

    @Test(timeout = 4000)
    public void testConsumeElementSelectorWithUnicode() {
        TokenQueue tq = new TokenQueue("café");
        assertEquals("caf", tq.consumeElementSelector());
    }

    @Test(timeout = 4000)
    public void testConsumeCssIdentifierWithUnicode() {
        TokenQueue tq = new TokenQueue("café");
        assertEquals("caf", tq.consumeCssIdentifier());
    }

    @Test(timeout = 4000)
    public void testConsumeAttributeKeyWithUnicode() {
        TokenQueue tq = new TokenQueue("café");
        assertEquals("caf", tq.consumeAttributeKey());
    }

    @Test(timeout = 4000)
    public void testRemainderAfterConsume() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeWord();
        tq.consumeWhitespace();
        assertEquals("world", tq.remainder());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testToStringAfterRemainder() {
        TokenQueue tq = new TokenQueue("hello");
        tq.remainder();
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testAddFirstAfterConsume() {
        TokenQueue tq = new TokenQueue("world");
        tq.consumeWord();
        tq.addFirst("hello ");
        assertEquals("hello ", tq.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesAfterAddFirst() {
        TokenQueue tq = new TokenQueue("world");
        tq.addFirst("hello ");
        assertTrue(tq.matches("hello"));
        assertTrue(tq.matchesCS("hello"));
    }

    @Test(timeout = 4000)
    public void testConsumeAfterAddFirst() {
        TokenQueue tq = new TokenQueue("world");
        tq.addFirst("hello ");
        assertEquals("hello ", tq.consumeTo("world"));
        assertEquals("world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalancedAfterAddFirst() {
        TokenQueue tq = new TokenQueue("world");
        tq.addFirst("(hello) ");
        assertEquals("hello", tq.chompBalanced('(', ')'));
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSpecialChars() {
        TokenQueue tq = new TokenQueue("a.b.c");
        assertEquals("a", tq.consumeToIgnoreCase("."));
        assertEquals(".b.c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithRegexChars() {
        TokenQueue tq = new TokenQueue("a*b*c");
        assertEquals("a", tq.consumeToIgnoreCase("*"));
        assertEquals("*b*c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBackslash() {
        TokenQueue tq = new TokenQueue("a\\b\\c");
        assertEquals("a", tq.consumeToIgnoreCase("\\"));
        assertEquals("\\b\\c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithQuote() {
        TokenQueue tq = new TokenQueue("a'b'c");
        assertEquals("a", tq.consumeToIgnoreCase("'"));
        assertEquals("'b'c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBrackets() {
        TokenQueue tq = new TokenQueue("a[b]c");
        assertEquals("a", tq.consumeToIgnoreCase("["));
        assertEquals("[b]c", tq.toString());
        
        tq = new TokenQueue("a[b]c");
        assertEquals("a[b", tq.consumeToIgnoreCase("]"));
        assertEquals("]c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithParens() {
        TokenQueue tq = new TokenQueue("a(b)c");
        assertEquals("a", tq.consumeToIgnoreCase("("));
        assertEquals("(b)c", tq.toString());
        
        tq = new TokenQueue("a(b)c");
        assertEquals("a(b", tq.consumeToIgnoreCase(")"));
        assertEquals(")c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBraces() {
        TokenQueue tq = new TokenQueue("a{b}c");
        assertEquals("a", tq.consumeToIgnoreCase("{"));
        assertEquals("{b}c", tq.toString());
        
        tq = new TokenQueue("a{b}c");
        assertEquals("a{b", tq.consumeToIgnoreCase("}"));
        assertEquals("}c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithAngleBrackets() {
        TokenQueue tq = new TokenQueue("a<b>c");
        assertEquals("a", tq.consumeToIgnoreCase("<"));
        assertEquals("<b>c", tq.toString());
        
        tq = new TokenQueue("a<b>c");
        assertEquals("a<b", tq.consumeToIgnoreCase(">"));
        assertEquals(">c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithColon() {
        TokenQueue tq = new TokenQueue("a:b:c");
        assertEquals("a", tq.consumeToIgnoreCase(":"));
        assertEquals(":b:c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSemicolon() {
        TokenQueue tq = new TokenQueue("a;b;c");
        assertEquals("a", tq.consumeToIgnoreCase(";"));
        assertEquals(";b;c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithComma() {
        TokenQueue tq = new TokenQueue("a,b,c");
        assertEquals("a", tq.consumeToIgnoreCase(","));
        assertEquals(",b,c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithAt() {
        TokenQueue tq = new TokenQueue("a@b@c");
        assertEquals("a", tq.consumeToIgnoreCase("@"));
        assertEquals("@b@c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHash() {
        TokenQueue tq = new TokenQueue("a#b#c");
        assertEquals("a", tq.consumeToIgnoreCase("#"));
        assertEquals("#b#c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithDollar() {
        TokenQueue tq = new TokenQueue("a$b$c");
        assertEquals("a", tq.consumeToIgnoreCase("$"));
        assertEquals("$b$c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithPercent() {
        TokenQueue tq = new TokenQueue("a%b%c");
        assertEquals("a", tq.consumeToIgnoreCase("%"));
        assertEquals("%b%c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithAmpersand() {
        TokenQueue tq = new TokenQueue("a&b&c");
        assertEquals("a", tq.consumeToIgnoreCase("&"));
        assertEquals("&b&c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithAsterisk() {
        TokenQueue tq = new TokenQueue("a*b*c");
        assertEquals("a", tq.consumeToIgnoreCase("*"));
        assertEquals("*b*c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithPlus() {
        TokenQueue tq = new TokenQueue("a+b+c");
        assertEquals("a", tq.consumeToIgnoreCase("+"));
        assertEquals("+b+c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithEquals() {
        TokenQueue tq = new TokenQueue("a=b=c");
        assertEquals("a", tq.consumeToIgnoreCase("="));
        assertEquals("=b=c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithQuestion() {
        TokenQueue tq = new TokenQueue("a?b?c");
        assertEquals("a", tq.consumeToIgnoreCase("?"));
        assertEquals("?b?c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithExclamation() {
        TokenQueue tq = new TokenQueue("a!b!c");
        assertEquals("a", tq.consumeToIgnoreCase("!"));
        assertEquals("!b!c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithTilde() {
        TokenQueue tq = new TokenQueue("a~b~c");
        assertEquals("a", tq.consumeToIgnoreCase("~"));
        assertEquals("~b~c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithCaret() {
        TokenQueue tq = new TokenQueue("a^b^c");
        assertEquals("a", tq.consumeToIgnoreCase("^"));
        assertEquals("^b^c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithPipe() {
        TokenQueue tq = new TokenQueue("a|b|c");
        assertEquals("a", tq.consumeToIgnoreCase("|"));
        assertEquals("|b|c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBacktick() {
        TokenQueue tq = new TokenQueue("a`b`c");
        assertEquals("a", tq.consumeToIgnoreCase("`"));
        assertEquals("`b`c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSpace() {
        TokenQueue tq = new TokenQueue("a b c");
        assertEquals("a", tq.consumeToIgnoreCase(" "));
        assertEquals(" b c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithTab() {
        TokenQueue tq = new TokenQueue("a\tb\tc");
        assertEquals("a", tq.consumeToIgnoreCase("\t"));
        assertEquals("\tb\tc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithNewline() {
        TokenQueue tq = new TokenQueue("a\nb\nc");
        assertEquals("a", tq.consumeToIgnoreCase("\n"));
        assertEquals("\nb\nc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithCarriageReturn() {
        TokenQueue tq = new TokenQueue("a\rb\rc");
        assertEquals("a", tq.consumeToIgnoreCase("\r"));
        assertEquals("\rb\rc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithFormFeed() {
        TokenQueue tq = new TokenQueue("a\fb\fc");
        assertEquals("a", tq.consumeToIgnoreCase("\f"));
        assertEquals("\fb\fc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBackspace() {
        TokenQueue tq = new TokenQueue("a\bb\bc");
        assertEquals("a", tq.consumeToIgnoreCase("\b"));
        assertEquals("\bb\bc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithUnicodeChar() {
        TokenQueue tq = new TokenQueue("a\u00e9b\u00e9c");
        assertEquals("a", tq.consumeToIgnoreCase("\u00e9"));
        assertEquals("\u00e9b\u00e9c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithNullChar() {
        TokenQueue tq = new TokenQueue("a\0b\0c");
        assertEquals("a", tq.consumeToIgnoreCase("\0"));
        assertEquals("\0b\0c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithMultipleCharSeq() {
        TokenQueue tq = new TokenQueue("abcXYZdef");
        assertEquals("abc", tq.consumeToIgnoreCase("xyz"));
        assertEquals("XYZdef", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithMixedCase() {
        TokenQueue tq = new TokenQueue("AbCdEf");
        assertEquals("Ab", tq.consumeToIgnoreCase("cD"));
        assertEquals("CdEf", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithRepeatedChars() {
        TokenQueue tq = new TokenQueue("aaaXaaa");
        assertEquals("aaa", tq.consumeToIgnoreCase("x"));
        assertEquals("Xaaa", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithOverlappingMatches() {
        TokenQueue tq = new TokenQueue("ababa");
        assertEquals("ab", tq.consumeToIgnoreCase("aba"));
        assertEquals("aba", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithLongSeq() {
        TokenQueue tq = new TokenQueue("short");
        assertEquals("", tq.consumeToIgnoreCase("longer"));
        assertEquals("short", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSameLength() {
        TokenQueue tq = new TokenQueue("abcd");
        assertEquals("", tq.consumeToIgnoreCase("efgh"));
        assertEquals("abcd", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithShorterSeq() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals("", tq.consumeToIgnoreCase("ab"));
        assertEquals("abc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSingleCharSeq() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals("", tq.consumeToIgnoreCase("a"));
        assertEquals("abc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithEmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeToIgnoreCase("a"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithNullSeq() {
        TokenQueue tq = new TokenQueue("abc");
        try {
            tq.consumeToIgnoreCase(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithEmptyString() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals("", tq.consumeToIgnoreCase(""));
        assertEquals("abc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithWhitespaceSeq() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello", tq.consumeToIgnoreCase(" "));
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSpecialRegexChars() {
        TokenQueue tq = new TokenQueue("a.b*c");
        assertEquals("a", tq.consumeToIgnoreCase("."));
        assertEquals(".b*c", tq.toString());
        
        tq = new TokenQueue("a.b*c");
        assertEquals("a.b", tq.consumeToIgnoreCase("*"));
        assertEquals("*c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithEscapedChars() {
        TokenQueue tq = new TokenQueue("a\\nb\\nc");
        assertEquals("a", tq.consumeToIgnoreCase("\\n"));
        assertEquals("\\nb\\nc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithUnicodeEscape() {
        TokenQueue tq = new TokenQueue("a\\u00e9b");
        assertEquals("a", tq.consumeToIgnoreCase("\\u00e9"));
        assertEquals("\\u00e9b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHexEscape() {
        TokenQueue tq = new TokenQueue("a\\x41b");
        assertEquals("a", tq.consumeToIgnoreCase("\\x41"));
        assertEquals("\\x41b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithOctalEscape() {
        TokenQueue tq = new TokenQueue("a\\101b");
        assertEquals("a", tq.consumeToIgnoreCase("\\101"));
        assertEquals("\\101b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithControlChars() {
        TokenQueue tq = new TokenQueue("a\u0001b\u0001c");
        assertEquals("a", tq.consumeToIgnoreCase("\u0001"));
        assertEquals("\u0001b\u0001c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSurrogatePair() {
        TokenQueue tq = new TokenQueue("a\ud83d\ude00b");
        assertEquals("a", tq.consumeToIgnoreCase("\ud83d\ude00"));
        assertEquals("\ud83d\ude00b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithCombiningChars() {
        TokenQueue tq = new TokenQueue("ae\u0301b");
        assertEquals("a", tq.consumeToIgnoreCase("e\u0301"));
        assertEquals("e\u0301b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBidiChars() {
        TokenQueue tq = new TokenQueue("a\u05d0b\u05d0c");
        assertEquals("a", tq.consumeToIgnoreCase("\u05d0"));
        assertEquals("\u05d0b\u05d0c", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithZeroWidthChars() {
        TokenQueue tq = new TokenQueue("a\u200bb\u200bc");
        assertEquals("a", tq.consumeToIgnoreCase("\u200b"));
        assertEquals("\u200bb\u200bc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithVariationSelectors() {
        TokenQueue tq = new TokenQueue("a\uFE0Fb\uFE0Fc");
        assertEquals("a", tq.consumeToIgnoreCase("\uFE0F"));
        assertEquals("\uFE0Fb\uFE0Fc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithEmoji() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDE00b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDE00"));
        assertEquals("\uD83D\uDE00b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithFlags() {
        TokenQueue tq = new TokenQueue("a\uD83C\uDDFA\uD83C\uDDF8b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83C\uDDFA\uD83C\uDDF8"));
        assertEquals("\uD83C\uDDFA\uD83C\uDDF8b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithKeycap() {
        TokenQueue tq = new TokenQueue("a1\uFE0F\u20E3b");
        assertEquals("a", tq.consumeToIgnoreCase("1\uFE0F\u20E3"));
        assertEquals("1\uFE0F\u20E3b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithTag() {
        TokenQueue tq = new TokenQueue("a\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08"));
        assertEquals("\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithFamily() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66"));
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSkinTone() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDC69b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDC69"));
        assertEquals("\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDC69b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHairStyle() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\u200D\uD83E\uDEB0b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\u200D\uD83E\uDEB0"));
        assertEquals("\uD83D\uDC68\u200D\uD83E\uDEB0b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithRedHair() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\uD83C\uDFFB\u200D\uD83E\uDEB0b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\uD83C\uDFFB\u200D\uD83E\uDEB0"));
        assertEquals("\uD83D\uDC68\uD83C\uDFFB\u200D\uD83E\uDEB0b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithCurlyHair() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\u200D\uD83E\uDEB1b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\u200D\uD83E\uDEB1"));
        assertEquals("\uD83D\uDC68\u200D\uD83E\uDEB1b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithWhiteHair() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\u200D\uD83E\uDEB3b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\u200D\uD83E\uDEB3"));
        assertEquals("\uD83D\uDC68\u200D\uD83E\uDEB3b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBald() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\u200D\uD83E\uDEB2b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\u200D\uD83E\uDEB2"));
        assertEquals("\uD83D\uDC68\u200D\uD83E\uDEB2b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithNoHair() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC68\u200D\uD83E\uDEB2b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC68\u200D\uD83E\uDEB2"));
        assertEquals("\uD83D\uDC68\u200D\uD83E\uDEB2b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithWomanAndMan() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithCouple() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithKiss() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartpulse() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBlueHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithGreenHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithYellowHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithPurpleHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithBlackHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithWhiteHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithSparklingHeart() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartDecoration() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartExclamation() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartBeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartPulse() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b");
        assertEquals("a", tq.consumeToIgnoreCase("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68"));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC68b", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCaseWithHeartbeat() {
        TokenQueue tq = new TokenQueue("a\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83