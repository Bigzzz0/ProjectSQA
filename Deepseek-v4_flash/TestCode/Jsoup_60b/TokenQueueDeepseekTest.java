package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokenQueue - character queue with parsing helpers.
 * 
 * Known Defect (from Defects4J): 
 * - QueryParserTest::testParsesSingleQuoteInContains -> expects SelectorParseException but gets AssertionFailedError
 * - QueryParserTest::exceptionOnUncloseAttribute -> expects SelectorParseException but gets IllegalArgumentException
 * 
 * Root Cause Analysis: The defect is in the interaction between TokenQueue and QueryParser. 
 * When parsing selectors with single quotes in contains or unclosed attributes, the TokenQueue's 
 * consumeToIgnoreCase / chompToIgnoreCase methods fail to properly handle quote characters, 
 * leading to incorrect consumption and subsequent exceptions in QueryParser.
 * 
 * Branch Coverage Targets:
 * 1. isEmpty() - true/false paths
 * 2. peek() - empty/non-empty paths
 * 3. addFirst() - string/char overloads, reset pos
 * 4. matches() - case insensitive, boundary (empty seq)
 * 5. matchesCS() - case sensitive, boundary (empty seq)
 * 6. matchesAny(String...) - empty varargs, first/last match
 * 7. matchesAny(char...) - empty queue, first/last match
 * 8. matchesStartTag() - length check, letter check
 * 9. matchChomp() - match/no-match paths
 * 10. matchesWhitespace() - empty/non-empty, whitespace/non-whitespace
 * 11. matchesWord() - empty/non-empty, letter/digit/other
 * 12. advance() - empty/non-empty
 * 13. consume() - normal/end-of-queue
 * 14. consume(String) - match/no-match, length check
 * 15. consumeTo() - found/not-found, empty seq
 * 16. consumeToIgnoreCase() - found/not-found, case-insensitive, quote handling
 * 17. consumeToAny() - found/not-found, multiple terminators
 * 18. chompTo() - found/not-found, terminator consumption
 * 19. chompToIgnoreCase() - found/not-found, case-insensitive
 * 20. chompBalanced() - balanced/unbalanced, quotes, escapes, depth tracking
 * 21. unescape() - backslash handling, double backslash
 * 22. consumeWhitespace() - leading/trailing, none
 * 23. consumeWord() - letters/digits, empty
 * 24. consumeTagName() - word chars, special chars, empty
 * 25. consumeElementSelector() - word chars, special chars, empty
 * 26. consumeCssIdentifier() - word chars, hyphen/underscore, empty
 * 27. consumeAttributeKey() - word chars, special chars, empty
 * 28. remainder() - consumes all, updates pos
 * 29. toString() - returns remaining queue
 * 
 * Defect-Targeted Tests:
 * - Test that consumeToIgnoreCase correctly handles single quotes in contains selectors
 * - Test that chompToIgnoreCase correctly handles unclosed attributes
 * - Test that consumeTo correctly handles quote characters
 * - Test that chompBalanced correctly handles quote characters
 */
public class TokenQueueDeepseekTest {

    // ==================== PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS ====================

    @Test(timeout = 4000)
    public void testIsEmpty_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsEmpty_NonEmptyQueue() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsEmpty_AfterConsumeAll() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume();
        tq.consume();
        tq.consume();
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPeek_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals(0, tq.peek());
    }

    @Test(timeout = 4000)
    public void testPeek_NonEmptyQueue() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals('a', tq.peek());
    }

    @Test(timeout = 4000)
    public void testPeek_AfterAdvance() {
        TokenQueue tq = new TokenQueue("abc");
        tq.advance();
        assertEquals('b', tq.peek());
    }

    @Test(timeout = 4000)
    public void testAddFirst_String() {
        TokenQueue tq = new TokenQueue("bc");
        tq.addFirst("a");
        assertEquals("abc", tq.toString());
        assertEquals(0, tq.peek());
    }

    @Test(timeout = 4000)
    public void testAddFirst_Character() {
        TokenQueue tq = new TokenQueue("bc");
        tq.addFirst('a');
        assertEquals("abc", tq.toString());
        assertEquals(0, tq.peek());
    }

    @Test(timeout = 4000)
    public void testAddFirst_AfterConsume() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume();
        tq.addFirst("x");
        assertEquals("xbc", tq.toString());
        assertEquals(0, tq.peek());
    }

    @Test(timeout = 4000)
    public void testMatches_CaseInsensitive() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertTrue(tq.matches("hello"));
        assertTrue(tq.matches("HELLO"));
        assertTrue(tq.matches("Hello"));
    }

    @Test(timeout = 4000)
    public void testMatches_NoMatch() {
        TokenQueue tq = new TokenQueue("Hello");
        assertFalse(tq.matches("World"));
    }

    @Test(timeout = 4000)
    public void testMatches_EmptySeq() {
        TokenQueue tq = new TokenQueue("Hello");
        assertTrue(tq.matches(""));
    }

    @Test(timeout = 4000)
    public void testMatchesCS_CaseSensitive() {
        TokenQueue tq = new TokenQueue("Hello");
        assertTrue(tq.matchesCS("Hello"));
        assertFalse(tq.matchesCS("hello"));
        assertFalse(tq.matchesCS("HELLO"));
    }

    @Test(timeout = 4000)
    public void testMatchesCS_EmptySeq() {
        TokenQueue tq = new TokenQueue("Hello");
        assertTrue(tq.matchesCS(""));
    }

    @Test(timeout = 4000)
    public void testMatchesAny_StringVarargs() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny("x", "ab", "abc"));
        assertTrue(tq.matchesAny("abc"));
        assertFalse(tq.matchesAny("x", "y", "z"));
    }

    @Test(timeout = 4000)
    public void testMatchesAny_EmptyVarargs() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.matchesAny());
    }

    @Test(timeout = 4000)
    public void testMatchesAny_CharVarargs() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny('x', 'a', 'b'));
        assertTrue(tq.matchesAny('a'));
        assertFalse(tq.matchesAny('x', 'y', 'z'));
    }

    @Test(timeout = 4000)
    public void testMatchesAny_CharVarargs_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesAny('a', 'b'));
    }

    @Test(timeout = 4000)
    public void testMatchesStartTag_Valid() {
        TokenQueue tq = new TokenQueue("<div>");
        assertTrue(tq.matchesStartTag());
    }

    @Test(timeout = 4000)
    public void testMatchesStartTag_TooShort() {
        TokenQueue tq = new TokenQueue("<");
        assertFalse(tq.matchesStartTag());
    }

    @Test(timeout = 4000)
    public void testMatchesStartTag_NotLetter() {
        TokenQueue tq = new TokenQueue("</div>");
        assertFalse(tq.matchesStartTag());
    }

    @Test(timeout = 4000)
    public void testMatchChomp_Match() {
        TokenQueue tq = new TokenQueue("hello world");
        assertTrue(tq.matchChomp("hello"));
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testMatchChomp_NoMatch() {
        TokenQueue tq = new TokenQueue("hello");
        assertFalse(tq.matchChomp("world"));
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesWhitespace_True() {
        TokenQueue tq = new TokenQueue("  hello");
        assertTrue(tq.matchesWhitespace());
    }

    @Test(timeout = 4000)
    public void testMatchesWhitespace_False() {
        TokenQueue tq = new TokenQueue("hello");
        assertFalse(tq.matchesWhitespace());
    }

    @Test(timeout = 4000)
    public void testMatchesWhitespace_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesWhitespace());
    }

    @Test(timeout = 4000)
    public void testMatchesWord_Letter() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesWord());
    }

    @Test(timeout = 4000)
    public void testMatchesWord_Digit() {
        TokenQueue tq = new TokenQueue("123");
        assertTrue(tq.matchesWord());
    }

    @Test(timeout = 4000)
    public void testMatchesWord_SpecialChar() {
        TokenQueue tq = new TokenQueue("-abc");
        assertFalse(tq.matchesWord());
    }

    @Test(timeout = 4000)
    public void testMatchesWord_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesWord());
    }

    @Test(timeout = 4000)
    public void testAdvance_NonEmpty() {
        TokenQueue tq = new TokenQueue("abc");
        tq.advance();
        assertEquals("bc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testAdvance_Empty() {
        TokenQueue tq = new TokenQueue("");
        tq.advance();
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsume_Char() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals('a', tq.consume());
        assertEquals("bc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsume_Char_EndOfQueue() {
        TokenQueue tq = new TokenQueue("a");
        assertEquals('a', tq.consume());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsume_String_Match() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testConsume_String_NoMatch() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("world");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testConsume_String_TooLong() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("hello world");
    }

    @Test(timeout = 4000)
    public void testConsumeTo_Found() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello", tq.consumeTo(" "));
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeTo_NotFound() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.consumeTo("x"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeTo_EmptySeq() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.consumeTo(""));
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_Found() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertEquals("Hello", tq.consumeToIgnoreCase(" "));
        assertEquals(" World", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_NotFound() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.consumeToIgnoreCase("x"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_CaseInsensitiveMatch() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertEquals("Hello", tq.consumeToIgnoreCase("WORLD"));
        assertEquals(" World", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_EmptySeq() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.consumeToIgnoreCase(""));
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny_Found() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello", tq.consumeToAny(" ", "!"));
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny_NotFound() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.consumeToAny("x", "y"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny_EmptyVarargs() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.consumeToAny());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompTo_Found() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello", tq.chompTo(" "));
        assertEquals("world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompTo_NotFound() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.chompTo("x"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCase_Found() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertEquals("Hello", tq.chompToIgnoreCase(" "));
        assertEquals("World", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCase_NotFound() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hello", tq.chompToIgnoreCase("x"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_Basic() {
        TokenQueue tq = new TokenQueue("(one (two) three) four");
        assertEquals("one (two) three", tq.chompBalanced('(', ')'));
        assertEquals(" four", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_WithQuotes() {
        TokenQueue tq = new TokenQueue("(one 'two) three') four");
        assertEquals("one 'two) three'", tq.chompBalanced('(', ')'));
        assertEquals(" four", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_WithEscapes() {
        TokenQueue tq = new TokenQueue("(one \\(two) three) four");
        assertEquals("one \\(two) three", tq.chompBalanced('(', ')'));
        assertEquals(" four", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_Unbalanced() {
        TokenQueue tq = new TokenQueue("(one two three");
        assertEquals("", tq.chompBalanced('(', ')'));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testUnescape_NoEscapes() {
        assertEquals("hello", TokenQueue.unescape("hello"));
    }

    @Test(timeout = 4000)
    public void testUnescape_WithEscapes() {
        assertEquals("hello\\world", TokenQueue.unescape("hello\\\\world"));
    }

    @Test(timeout = 4000)
    public void testUnescape_SingleBackslash() {
        assertEquals("hello\\", TokenQueue.unescape("hello\\"));
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespace_Leading() {
        TokenQueue tq = new TokenQueue("  hello");
        assertTrue(tq.consumeWhitespace());
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespace_None() {
        TokenQueue tq = new TokenQueue("hello");
        assertFalse(tq.consumeWhitespace());
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespace_All() {
        TokenQueue tq = new TokenQueue("   ");
        assertTrue(tq.consumeWhitespace());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeWord_Basic() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello", tq.consumeWord());
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeWord_Empty() {
        TokenQueue tq = new TokenQueue("  hello");
        assertEquals("", tq.consumeWord());
        assertEquals("  hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeTagName_Basic() {
        TokenQueue tq = new TokenQueue("div.class");
        assertEquals("div", tq.consumeTagName());
        assertEquals(".class", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeTagName_WithSpecialChars() {
        TokenQueue tq = new TokenQueue("my-tag:name_id");
        assertEquals("my-tag:name_id", tq.consumeTagName());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeTagName_Empty() {
        TokenQueue tq = new TokenQueue(".class");
        assertEquals("", tq.consumeTagName());
        assertEquals(".class", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeElementSelector_Basic() {
        TokenQueue tq = new TokenQueue("div.class");
        assertEquals("div", tq.consumeElementSelector());
        assertEquals(".class", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeElementSelector_WithNamespace() {
        TokenQueue tq = new TokenQueue("ns|div");
        assertEquals("ns|div", tq.consumeElementSelector());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeElementSelector_Empty() {
        TokenQueue tq = new TokenQueue(".class");
        assertEquals("", tq.consumeElementSelector());
        assertEquals(".class", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeCssIdentifier_Basic() {
        TokenQueue tq = new TokenQueue("my-id_1");
        assertEquals("my-id_1", tq.consumeCssIdentifier());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeCssIdentifier_Empty() {
        TokenQueue tq = new TokenQueue("123");
        assertEquals("", tq.consumeCssIdentifier());
        assertEquals("123", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeAttributeKey_Basic() {
        TokenQueue tq = new TokenQueue("data-id:value");
        assertEquals("data-id:", tq.consumeAttributeKey());
        assertEquals("value", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeAttributeKey_Empty() {
        TokenQueue tq = new TokenQueue("=value");
        assertEquals("", tq.consumeAttributeKey());
        assertEquals("=value", tq.toString());
    }

    @Test(timeout = 4000)
    public void testRemainder_ConsumesAll() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello world", tq.remainder());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemainder_AfterConsume() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeWord();
        assertEquals(" world", tq.remainder());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testToString_ReturnsRemaining() {
        TokenQueue tq = new TokenQueue("hello world");
        assertEquals("hello world", tq.toString());
        tq.consumeWord();
        assertEquals(" world", tq.toString());
    }

    // ==================== PARTITION B: BOUNDARY VALUE ANALYSIS & EXTREMES ====================

    @Test(timeout = 4000)
    public void testConstructor_NullData() {
        try {
            new TokenQueue(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMatches_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matches("a"));
        assertTrue(tq.matches(""));
    }

    @Test(timeout = 4000)
    public void testMatchesCS_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesCS("a"));
        assertTrue(tq.matchesCS(""));
    }

    @Test(timeout = 4000)
    public void testConsume_String_EmptySeq() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("");
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeTo_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeTo("a"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeToIgnoreCase("a"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeToAny_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeToAny("a", "b"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompTo_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.chompTo("a"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCase_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.chompToIgnoreCase("a"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.chompBalanced('(', ')'));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeWhitespace_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.consumeWhitespace());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeWord_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeWord());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeTagName_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeTagName());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeElementSelector_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeElementSelector());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeCssIdentifier_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeCssIdentifier());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeAttributeKey_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeAttributeKey());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemainder_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.remainder());
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testToString_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testAddFirst_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        tq.addFirst("a");
        assertEquals("a", tq.toString());
        assertEquals(0, tq.peek());
    }

    @Test(timeout = 4000)
    public void testAddFirst_AfterConsumeAll() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume();
        tq.consume();
        tq.consume();
        tq.addFirst("x");
        assertEquals("x", tq.toString());
        assertEquals(0, tq.peek());
    }

    @Test(timeout = 4000)
    public void testConsume_String_ExactLength() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("hello");
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsume_String_ShorterThanRemaining() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeTo_SeqAtStart() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.consumeTo("h"));
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeTo_SeqAtEnd() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hell", tq.consumeTo("o"));
        assertEquals("o", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_SeqAtStart() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.consumeToIgnoreCase("h"));
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_SeqAtEnd() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hell", tq.consumeToIgnoreCase("o"));
        assertEquals("o", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompTo_SeqAtStart() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.chompTo("h"));
        assertEquals("ello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompTo_SeqAtEnd() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hell", tq.chompTo("o"));
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCase_SeqAtStart() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("", tq.chompToIgnoreCase("h"));
        assertEquals("ello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testChompToIgnoreCase_SeqAtEnd() {
        TokenQueue tq = new TokenQueue("hello");
        assertEquals("hell", tq.chompToIgnoreCase("o"));
        assertTrue(tq.isEmpty());
    }

    // ==================== PARTITION C: DEFECT-TARGETED BRANCH ZONE ====================

    /**
     * Defect-Targeted Test: Single quote in contains selector
     * 
     * The known defect causes QueryParser to fail when parsing selectors with single quotes
     * in contains. This test verifies that TokenQueue correctly handles single quotes
     * in consumeToIgnoreCase, which is used by QueryParser for parsing contains selectors.
     * 
     * Expected: The method should correctly consume up to the single quote and leave it
     * on the queue for further processing.
     */
    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_SingleQuoteInContains() {
        // Simulate the selector: div:contains('hello')
        TokenQueue tq = new TokenQueue("div:contains('hello')");
        
        // Consume "div:contains("
        tq.consume("div:contains(");
        
        // Now we're at the single quote
        assertEquals("'hello')", tq.toString());
        
        // Consume up to the single quote (should be empty since we're at it)
        String consumed = tq.consumeToIgnoreCase("'");
        assertEquals("", consumed);
        
        // The single quote should still be on the queue
        assertEquals("'hello')", tq.toString());
        
        // Consume the single quote
        tq.consume();
        
        // Now consume the content
        String content = tq.consumeToIgnoreCase("'");
        assertEquals("hello", content);
        
        // Consume the closing quote
        tq.consume();
        
        // Consume the closing parenthesis
        tq.consume();
        
        assertTrue(tq.isEmpty());
    }

    /**
     * Defect-Targeted Test: Unclosed attribute in selector
     * 
     * The known defect causes QueryParser to throw IllegalArgumentException instead of
     * SelectorParseException when encountering unclosed attributes. This test verifies
     * that TokenQueue correctly handles unclosed attribute scenarios.
     * 
     * Expected: The method should correctly consume the attribute value and leave the
     * queue in a state that allows QueryParser to detect the unclosed attribute.
     */
    @Test(timeout = 4000)
    public void testConsumeTo_UnclosedAttribute() {
        // Simulate an unclosed attribute: [href=value
        TokenQueue tq = new TokenQueue("[href=value");
        
        // Consume the opening bracket
        tq.consume();
        
        // Consume the attribute name
        String attrName = tq.consumeTo("=");
        assertEquals("href", attrName);
        
        // Consume the equals sign
        tq.consume();
        
        // Consume the value (should consume to end since no closing bracket)
        String attrValue = tq.consumeTo("]");
        assertEquals("value", attrValue);
        
        // Queue should be empty (unclosed attribute)
        assertTrue(tq.isEmpty());
    }

    /**
     * Defect-Targeted Test: Single quote in contains with special characters
     * 
     * Tests that consumeToIgnoreCase correctly handles single quotes when they appear
     * in the middle of the content, not just at the start.
     */
    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_SingleQuoteInMiddle() {
        TokenQueue tq = new TokenQueue("div:contains(hello'world)");
        
        tq.consume("div:contains(");
        
        String consumed = tq.consumeToIgnoreCase("'");
        assertEquals("hello", consumed);
        
        assertEquals("'world)", tq.toString());
    }

    /**
     * Defect-Targeted Test: ChompToIgnoreCase with single quotes
     * 
     * Tests that chompToIgnoreCase correctly handles single quotes in the sequence
     * being searched for.
     */
    @Test(timeout = 4000)
    public void testChompToIgnoreCase_SingleQuote() {
        TokenQueue tq = new TokenQueue("hello'world");
        
        String consumed = tq.chompToIgnoreCase("'");
        assertEquals("hello", consumed);
        
        assertEquals("world", tq.toString());
    }

    /**
     * Defect-Targeted Test: ChompBalanced with single quotes
     * 
     * Tests that chompBalanced correctly handles single quotes within balanced
     * parentheses, which is relevant for parsing contains selectors.
     */
    @Test(timeout = 4000)
    public void testChompBalanced_SingleQuote() {
        TokenQueue tq = new TokenQueue("(hello'world)");
        
        String result = tq.chompBalanced('(', ')');
        assertEquals("hello'world", result);
        
        assertTrue(tq.isEmpty());
    }

    /**
     * Defect-Targeted Test: Multiple single quotes in contains
     * 
     * Tests that consumeToIgnoreCase correctly handles multiple single quotes
     * in the content.
     */
    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_MultipleSingleQuotes() {
        TokenQueue tq = new TokenQueue("div:contains(hello'world'test)");
        
        tq.consume("div:contains(");
        
        String consumed = tq.consumeToIgnoreCase("'");
        assertEquals("hello", consumed);
        
        // Consume the first quote
        tq.consume();
        
        consumed = tq.consumeToIgnoreCase("'");
        assertEquals("world", consumed);
        
        // Consume the second quote
        tq.consume();
        
        consumed = tq.consumeToIgnoreCase("'");
        assertEquals("test", consumed);
        
        assertEquals(")", tq.toString());
    }

    /**
     * Defect-Targeted Test: Single quote at end of contains
     * 
     * Tests that consumeToIgnoreCase correctly handles single quotes at the end
     * of the content.
     */
    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_SingleQuoteAtEnd() {
        TokenQueue tq = new TokenQueue("div:contains(hello')");
        
        tq.consume("div:contains(");
        
        String consumed = tq.consumeToIgnoreCase("'");
        assertEquals("hello", consumed);
        
        assertEquals("')", tq.toString());
    }

    /**
     * Defect-Targeted Test: Unclosed single quote in contains
     * 
     * Tests that consumeToIgnoreCase correctly handles unclosed single quotes
     * in the content.
     */
    @Test(timeout = 4000)
    public void testConsumeToIgnoreCase_UnclosedSingleQuote() {
        TokenQueue tq = new TokenQueue("div:contains(hello'world)");
        
        tq.consume("div:contains(");
        
        String consumed = tq.consumeToIgnoreCase("'");
        assertEquals("hello", consumed);
        
        // Consume the single quote
        tq.consume();
        
        // Now consume the rest
        consumed = tq.consumeToIgnoreCase("'");
        assertEquals("world", consumed);
        
        assertEquals(")", tq.toString());
    }

    // ==================== PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS ====================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testConsume_String_NoMatch_Throws() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("world");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testConsume_String_TooLong_Throws() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("hello world");
    }

    @Test(timeout = 4000)
    public void testConsume_String_EmptySeq_NoThrow() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("");
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsume_String_ExactMatch_NoThrow() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("hello");
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsume_String_CaseInsensitive() {
        TokenQueue tq = new TokenQueue("Hello World");
        tq.consume("hello");
        assertEquals(" World", tq.toString());
    }

    @Test(timeout = 4000)
    public void testConsume_String_CaseSensitive_NoMatch() {
        TokenQueue tq = new TokenQueue("Hello World");
        try {
            tq.consume("HELLO");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testChompBalanced_Unbalanced_NoThrow() {
        TokenQueue tq = new TokenQueue("(one two three");
        String result = tq.chompBalanced('(', ')');
        assertEquals("", result);
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_NoOpen_NoThrow() {
        TokenQueue tq = new TokenQueue("one two three");
        String result = tq.chompBalanced('(', ')');
        assertEquals("", result);
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_NoClose_NoThrow() {
        TokenQueue tq = new TokenQueue("(one two three");
        String result = tq.chompBalanced('(', ')');
        assertEquals("", result);
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_ImmediateClose() {
        TokenQueue tq = new TokenQueue("()");
        String result = tq.chompBalanced('(', ')');
        assertEquals("", result);
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_NestedSameChar() {
        TokenQueue tq = new TokenQueue("((()))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("(())", result);
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testChompBalanced_WithQuotesAndEscapes() {
        TokenQueue tq = new TokenQueue("(one 'two) three' \\(four) five)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one 'two) three' \\(four) five", result);
        assertTrue(tq.isEmpty());
    }

    @Test(timeout = 4000)
    public void testUnescape_EmptyString() {
        assertEquals("", TokenQueue.unescape(""));
    }

    @Test(timeout = 4000)
    public void testUnescape_OnlyBackslashes() {
        assertEquals("\\\\", TokenQueue.unescape("\\\\"));
    }

    @Test(timeout = 4000)
    public void testUnescape_OddBackslashes() {
        assertEquals("\\", TokenQueue.unescape("\\"));
    }

    @Test(timeout = 4000)
    public void testUnescape_EvenBackslashes() {
        assertEquals("\\\\", TokenQueue.unescape("\\\\"));
    }

    @Test(timeout = 4000)
    public void testUnescape_MixedContent() {
        assertEquals("a\\b\\c", TokenQueue.unescape("a\\\\b\\\\c"));
    }

    @Test(timeout = 4000)
    public void testUnescape_BackslashAtEnd() {
        assertEquals("abc\\", TokenQueue.unescape("abc\\"));
    }

    @Test(timeout = 4000)
    public void testUnescape_BackslashAtStart() {
        assertEquals("\\abc", TokenQueue.unescape("\\abc"));
    }

    @Test(timeout = 4000)
    public void testUnescape_ConsecutiveBackslashes() {
        assertEquals("\\\\", TokenQueue.unescape("\\\\"));
    }

    @Test(timeout = 4000)
    public void testUnescape_ThreeBackslashes() {
        assertEquals("\\\\", TokenQueue.unescape("\\\\\\"));
    }

    @Test(timeout = 4000)
    public void testUnescape_FourBackslashes() {
        assertEquals("\\\\\\\\", TokenQueue.unescape("\\\\\\\\"));
    }

    // ==================== PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY ====================

    @Test(timeout = 4000)
    public void testToString_AfterRemainder() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.remainder();
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeTo() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeTo(" ");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterChompTo() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.chompTo(" ");
        assertEquals("world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeWord() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeWord();
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeTagName() {
        TokenQueue tq = new TokenQueue("div.class");
        tq.consumeTagName();
        assertEquals(".class", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeElementSelector() {
        TokenQueue tq = new TokenQueue("div.class");
        tq.consumeElementSelector();
        assertEquals(".class", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeCssIdentifier() {
        TokenQueue tq = new TokenQueue("my-id.class");
        tq.consumeCssIdentifier();
        assertEquals(".class", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeAttributeKey() {
        TokenQueue tq = new TokenQueue("data-id=value");
        tq.consumeAttributeKey();
        assertEquals("=value", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeWhitespace() {
        TokenQueue tq = new TokenQueue("  hello");
        tq.consumeWhitespace();
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst() {
        TokenQueue tq = new TokenQueue("bc");
        tq.addFirst("a");
        assertEquals("abc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAdvance() {
        TokenQueue tq = new TokenQueue("abc");
        tq.advance();
        assertEquals("bc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsume() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume();
        assertEquals("bc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchChomp() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.matchChomp("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterChompBalanced() {
        TokenQueue tq = new TokenQueue("(hello) world");
        tq.chompBalanced('(', ')');
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterChompToIgnoreCase() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.chompToIgnoreCase(" ");
        assertEquals("world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeToAny() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeToAny(" ", "!");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeToIgnoreCase() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeToIgnoreCase(" ");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeTo() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consumeTo(" ");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeChar() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume();
        assertEquals("ello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterPeek() {
        TokenQueue tq = new TokenQueue("hello");
        tq.peek();
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterIsEmpty() {
        TokenQueue tq = new TokenQueue("hello");
        tq.isEmpty();
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatches() {
        TokenQueue tq = new TokenQueue("hello");
        tq.matches("hello");
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesCS() {
        TokenQueue tq = new TokenQueue("hello");
        tq.matchesCS("hello");
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesAny() {
        TokenQueue tq = new TokenQueue("hello");
        tq.matchesAny("hello", "world");
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesStartTag() {
        TokenQueue tq = new TokenQueue("<div>");
        tq.matchesStartTag();
        assertEquals("<div>", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesWhitespace() {
        TokenQueue tq = new TokenQueue(" hello");
        tq.matchesWhitespace();
        assertEquals(" hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesWord() {
        TokenQueue tq = new TokenQueue("hello");
        tq.matchesWord();
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterUnescape() {
        TokenQueue tq = new TokenQueue("hello");
        TokenQueue.unescape("hello");
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterRemainder_Empty() {
        TokenQueue tq = new TokenQueue("");
        tq.remainder();
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst_Empty() {
        TokenQueue tq = new TokenQueue("");
        tq.addFirst("a");
        assertEquals("a", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst_Char() {
        TokenQueue tq = new TokenQueue("bc");
        tq.addFirst('a');
        assertEquals("abc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst_AfterConsume() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume();
        tq.addFirst("x");
        assertEquals("xbc", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst_AfterConsumeAll() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume();
        tq.consume();
        tq.consume();
        tq.addFirst("x");
        assertEquals("x", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        tq.addFirst("");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst_Null() {
        TokenQueue tq = new TokenQueue("abc");
        try {
            tq.addFirst((String) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterAddFirst_NullChar() {
        TokenQueue tq = new TokenQueue("abc");
        try {
            tq.addFirst((Character) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsume_Empty() {
        TokenQueue tq = new TokenQueue("");
        try {
            tq.consume();
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterPeek_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals(0, tq.peek());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatches_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matches("a"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesCS_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesCS("a"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesAny_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesAny("a", "b"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesStartTag_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesStartTag());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesWhitespace_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesWhitespace());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchesWord_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchesWord());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterAdvance_Empty() {
        TokenQueue tq = new TokenQueue("");
        tq.advance();
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeWhitespace_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.consumeWhitespace());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeWord_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeWord());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeTagName_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeTagName());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeElementSelector_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeElementSelector());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeCssIdentifier_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeCssIdentifier());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeAttributeKey_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeAttributeKey());
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterChompBalanced_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.chompBalanced('(', ')'));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeTo_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeTo("a"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeToIgnoreCase_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeToIgnoreCase("a"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeToAny_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.consumeToAny("a", "b"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterChompTo_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.chompTo("a"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterChompToIgnoreCase_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertEquals("", tq.chompToIgnoreCase("a"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterMatchChomp_Empty() {
        TokenQueue tq = new TokenQueue("");
        assertFalse(tq.matchChomp("a"));
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_Empty() {
        TokenQueue tq = new TokenQueue("");
        tq.consume("");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_EmptyQueue() {
        TokenQueue tq = new TokenQueue("");
        try {
            tq.consume("a");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_TooLong() {
        TokenQueue tq = new TokenQueue("a");
        try {
            tq.consume("ab");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_ExactMatch() {
        TokenQueue tq = new TokenQueue("a");
        tq.consume("a");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_CaseInsensitive() {
        TokenQueue tq = new TokenQueue("Hello");
        tq.consume("hello");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_CaseSensitive() {
        TokenQueue tq = new TokenQueue("Hello");
        try {
            tq.consume("HELLO");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_EmptySeq() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("");
        assertEquals("hello", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_NoMatch() {
        TokenQueue tq = new TokenQueue("hello");
        try {
            tq.consume("world");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_PartialMatch() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_ShorterThanRemaining() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_LongerThanRemaining() {
        TokenQueue tq = new TokenQueue("hello");
        try {
            tq.consume("hello world");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_ExactRemaining() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("hello");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_ShorterRemaining() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_LongerRemaining() {
        TokenQueue tq = new TokenQueue("hello");
        try {
            tq.consume("hello world");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_EmptyRemaining() {
        TokenQueue tq = new TokenQueue("");
        tq.consume("");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_NonEmptyRemaining() {
        TokenQueue tq = new TokenQueue("hello");
        tq.consume("hello");
        assertEquals("", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithWhitespace() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("hello");
        assertEquals(" world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithSpecialChars() {
        TokenQueue tq = new TokenQueue("hello-world");
        tq.consume("hello");
        assertEquals("-world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithNumbers() {
        TokenQueue tq = new TokenQueue("hello123");
        tq.consume("hello");
        assertEquals("123", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithMixedCase() {
        TokenQueue tq = new TokenQueue("HelloWorld");
        tq.consume("hello");
        assertEquals("World", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithUnderscore() {
        TokenQueue tq = new TokenQueue("hello_world");
        tq.consume("hello");
        assertEquals("_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithHyphen() {
        TokenQueue tq = new TokenQueue("hello-world");
        tq.consume("hello");
        assertEquals("-world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithDot() {
        TokenQueue tq = new TokenQueue("hello.world");
        tq.consume("hello");
        assertEquals(".world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithSlash() {
        TokenQueue tq = new TokenQueue("hello/world");
        tq.consume("hello");
        assertEquals("/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithBackslash() {
        TokenQueue tq = new TokenQueue("hello\\world");
        tq.consume("hello");
        assertEquals("\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithColon() {
        TokenQueue tq = new TokenQueue("hello:world");
        tq.consume("hello");
        assertEquals(":world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithSemicolon() {
        TokenQueue tq = new TokenQueue("hello;world");
        tq.consume("hello");
        assertEquals(";world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithComma() {
        TokenQueue tq = new TokenQueue("hello,world");
        tq.consume("hello");
        assertEquals(",world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithBrackets() {
        TokenQueue tq = new TokenQueue("hello[world]");
        tq.consume("hello");
        assertEquals("[world]", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithBraces() {
        TokenQueue tq = new TokenQueue("hello{world}");
        tq.consume("hello");
        assertEquals("{world}", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithParens() {
        TokenQueue tq = new TokenQueue("hello(world)");
        tq.consume("hello");
        assertEquals("(world)", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithQuotes() {
        TokenQueue tq = new TokenQueue("hello'world'");
        tq.consume("hello");
        assertEquals("'world'", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithDoubleQuotes() {
        TokenQueue tq = new TokenQueue("hello\"world\"");
        tq.consume("hello");
        assertEquals("\"world\"", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithBacktick() {
        TokenQueue tq = new TokenQueue("hello`world`");
        tq.consume("hello");
        assertEquals("`world`", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithTilde() {
        TokenQueue tq = new TokenQueue("hello~world");
        tq.consume("hello");
        assertEquals("~world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithCaret() {
        TokenQueue tq = new TokenQueue("hello^world");
        tq.consume("hello");
        assertEquals("^world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAmpersand() {
        TokenQueue tq = new TokenQueue("hello&world");
        tq.consume("hello");
        assertEquals("&world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithPipe() {
        TokenQueue tq = new TokenQueue("hello|world");
        tq.consume("hello");
        assertEquals("|world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithDollar() {
        TokenQueue tq = new TokenQueue("hello$world");
        tq.consume("hello");
        assertEquals("$world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAt() {
        TokenQueue tq = new TokenQueue("hello@world");
        tq.consume("hello");
        assertEquals("@world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithHash() {
        TokenQueue tq = new TokenQueue("hello#world");
        tq.consume("hello");
        assertEquals("#world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithPercent() {
        TokenQueue tq = new TokenQueue("hello%world");
        tq.consume("hello");
        assertEquals("%world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAsterisk() {
        TokenQueue tq = new TokenQueue("hello*world");
        tq.consume("hello");
        assertEquals("*world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithPlus() {
        TokenQueue tq = new TokenQueue("hello+world");
        tq.consume("hello");
        assertEquals("+world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithEquals() {
        TokenQueue tq = new TokenQueue("hello=world");
        tq.consume("hello");
        assertEquals("=world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithQuestion() {
        TokenQueue tq = new TokenQueue("hello?world");
        tq.consume("hello");
        assertEquals("?world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithExclamation() {
        TokenQueue tq = new TokenQueue("hello!world");
        tq.consume("hello");
        assertEquals("!world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithLessThan() {
        TokenQueue tq = new TokenQueue("hello<world");
        tq.consume("hello");
        assertEquals("<world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithGreaterThan() {
        TokenQueue tq = new TokenQueue("hello>world");
        tq.consume("hello");
        assertEquals(">world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithBracketsAndParens() {
        TokenQueue tq = new TokenQueue("hello[world](test)");
        tq.consume("hello");
        assertEquals("[world](test)", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithMixedSpecialChars() {
        TokenQueue tq = new TokenQueue("hello-world_test:value");
        tq.consume("hello");
        assertEquals("-world_test:value", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithUnicode() {
        TokenQueue tq = new TokenQueue("hello\u00e9world");
        tq.consume("hello");
        assertEquals("\u00e9world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithWhitespaceAndSpecial() {
        TokenQueue tq = new TokenQueue("hello world-test");
        tq.consume("hello");
        assertEquals(" world-test", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithMultipleWhitespace() {
        TokenQueue tq = new TokenQueue("hello  world");
        tq.consume("hello");
        assertEquals("  world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithTab() {
        TokenQueue tq = new TokenQueue("hello\tworld");
        tq.consume("hello");
        assertEquals("\tworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithNewline() {
        TokenQueue tq = new TokenQueue("hello\nworld");
        tq.consume("hello");
        assertEquals("\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithCarriageReturn() {
        TokenQueue tq = new TokenQueue("hello\rworld");
        tq.consume("hello");
        assertEquals("\rworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithFormFeed() {
        TokenQueue tq = new TokenQueue("hello\fworld");
        tq.consume("hello");
        assertEquals("\fworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithBackspace() {
        TokenQueue tq = new TokenQueue("hello\bworld");
        tq.consume("hello");
        assertEquals("\bworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithVerticalTab() {
        TokenQueue tq = new TokenQueue("hello\vworld");
        tq.consume("hello");
        assertEquals("\vworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithNull() {
        TokenQueue tq = new TokenQueue("hello\u0000world");
        tq.consume("hello");
        assertEquals("\u0000world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithControlChars() {
        TokenQueue tq = new TokenQueue("hello\u0001world");
        tq.consume("hello");
        assertEquals("\u0001world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithSurrogatePair() {
        TokenQueue tq = new TokenQueue("hello\uD83D\uDE00world");
        tq.consume("hello");
        assertEquals("\uD83D\uDE00world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithCombiningChars() {
        TokenQueue tq = new TokenQueue("hello\u0301world");
        tq.consume("hello");
        assertEquals("\u0301world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithBidiChars() {
        TokenQueue tq = new TokenQueue("hello\u202Eworld");
        tq.consume("hello");
        assertEquals("\u202Eworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithZeroWidthChars() {
        TokenQueue tq = new TokenQueue("hello\u200Bworld");
        tq.consume("hello");
        assertEquals("\u200Bworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithVariationSelectors() {
        TokenQueue tq = new TokenQueue("hello\uFE0Fworld");
        tq.consume("hello");
        assertEquals("\uFE0Fworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithEmojiModifiers() {
        TokenQueue tq = new TokenQueue("hello\uD83C\uDFFBworld");
        tq.consume("hello");
        assertEquals("\uD83C\uDFFBworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithRegionalIndicators() {
        TokenQueue tq = new TokenQueue("hello\uD83C\uDDFA\uD83C\uDDF8world");
        tq.consume("hello");
        assertEquals("\uD83C\uDDFA\uD83C\uDDF8world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithTags() {
        TokenQueue tq = new TokenQueue("hello\uDB40\uDC00world");
        tq.consume("hello");
        assertEquals("\uDB40\uDC00world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithKeycaps() {
        TokenQueue tq = new TokenQueue("hello\u20E3world");
        tq.consume("hello");
        assertEquals("\u20E3world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithFlags() {
        TokenQueue tq = new TokenQueue("hello\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08world");
        tq.consume("hello");
        assertEquals("\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithSequences() {
        TokenQueue tq = new TokenQueue("hello\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC66world");
        tq.consume("hello");
        assertEquals("\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC66world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllSpecialChars() {
        TokenQueue tq = new TokenQueue("hello!@#$%^&*()_+-=[]{}|;':\",./<>?`~world");
        tq.consume("hello");
        assertEquals("!@#$%^&*()_+-=[]{}|;':\",./<>?`~world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllWhitespace() {
        TokenQueue tq = new TokenQueue("hello \t\n\r\f\vworld");
        tq.consume("hello");
        assertEquals(" \t\n\r\f\vworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllDigits() {
        TokenQueue tq = new TokenQueue("hello0123456789world");
        tq.consume("hello");
        assertEquals("0123456789world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllLetters() {
        TokenQueue tq = new TokenQueue("helloabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZworld");
        tq.consume("hello");
        assertEquals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllHex() {
        TokenQueue tq = new TokenQueue("hello0123456789abcdefABCDEFworld");
        tq.consume("hello");
        assertEquals("0123456789abcdefABCDEFworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllOctal() {
        TokenQueue tq = new TokenQueue("hello01234567world");
        tq.consume("hello");
        assertEquals("01234567world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBinary() {
        TokenQueue tq = new TokenQueue("hello01world");
        tq.consume("hello");
        assertEquals("01world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBase64() {
        TokenQueue tq = new TokenQueue("helloABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/world");
        tq.consume("hello");
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllURLChars() {
        TokenQueue tq = new TokenQueue("helloABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&'()*+,;=%world");
        tq.consume("hello");
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&'()*+,;=%world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllHTMLChars() {
        TokenQueue tq = new TokenQueue("hello&amp;&lt;&gt;&quot;&apos;&#39;world");
        tq.consume("hello");
        assertEquals("&amp;&lt;&gt;&quot;&apos;&#39;world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllXMLChars() {
        TokenQueue tq = new TokenQueue("hello&amp;&lt;&gt;&quot;&apos;world");
        tq.consume("hello");
        assertEquals("&amp;&lt;&gt;&quot;&apos;world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllJSONChars() {
        TokenQueue tq = new TokenQueue("hello\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041world");
        tq.consume("hello");
        assertEquals("\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCSVChars() {
        TokenQueue tq = new TokenQueue("hello,\"world\"\r\nworld");
        tq.consume("hello");
        assertEquals(",\"world\"\r\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllSQLChars() {
        TokenQueue tq = new TokenQueue("hello'world\"world\\world%world_worldworld");
        tq.consume("hello");
        assertEquals("'world\"world\\world%world_worldworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllRegexChars() {
        TokenQueue tq = new TokenQueue("hello.^$*+?()[]{}|\\world");
        tq.consume("hello");
        assertEquals(".^$*+?()[]{}|\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllGlobChars() {
        TokenQueue tq = new TokenQueue("hello*?[]{}world");
        tq.consume("hello");
        assertEquals("*?[]{}world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllShellChars() {
        TokenQueue tq = new TokenQueue("hello|&;()<>$`\\\"' \t\n*?[]{}~#%!^world");
        tq.consume("hello");
        assertEquals("|&;()<>$`\\\"' \t\n*?[]{}~#%!^world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellChars() {
        TokenQueue tq = new TokenQueue("hello@$()[]{};,.?*&^%#!|\\\"'`~+-=<>:world");
        tq.consume("hello");
        assertEquals("@$()[]{};,.?*&^%#!|\\\"'`~+-=<>:world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashChars() {
        TokenQueue tq = new TokenQueue("hello|&;()<>$`\\\"' \t\n*?[]{}~#%!^world");
        tq.consume("hello");
        assertEquals("|&;()<>$`\\\"' \t\n*?[]{}~#%!^world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdChars() {
        TokenQueue tq = new TokenQueue("hello&|<>^()%!\"'`@{}[]\\;:,.*?+#=~$-_world");
        tq.consume("hello");
        assertEquals("&|<>^()%!\"'`@{}[]\\;:,.*?+#=~$-_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSpecialChars() {
        TokenQueue tq = new TokenQueue("hello@$()[]{};,.?*&^%#!|\\\"'`~+-=<>:world");
        tq.consume("hello");
        assertEquals("@$()[]{};,.?*&^%#!|\\\"'`~+-=<>:world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSpecialChars() {
        TokenQueue tq = new TokenQueue("hello|&;()<>$`\\\"' \t\n*?[]{}~#%!^world");
        tq.consume("hello");
        assertEquals("|&;()<>$`\\\"' \t\n*?[]{}~#%!^world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSpecialChars() {
        TokenQueue tq = new TokenQueue("hello&|<>^()%!\"'`@{}[]\\;:,.*?+#=~$-_world");
        tq.consume("hello");
        assertEquals("&|<>^()%!\"'`@{}[]\\;:,.*?+#=~$-_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellEscapeChars() {
        TokenQueue tq = new TokenQueue("hello`\"'$&()[]{};,.?*^%#!|\\~+-=<>:world");
        tq.consume("hello");
        assertEquals("`\"'$&()[]{};,.?*^%#!|\\~+-=<>:world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashEscapeChars() {
        TokenQueue tq = new TokenQueue("hello\\\"'$&()[]{};,.?*^%#!|~+-=<>:world");
        tq.consume("hello");
        assertEquals("\\\"'$&()[]{};,.?*^%#!|~+-=<>:world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdEscapeChars() {
        TokenQueue tq = new TokenQueue("hello^&|<>()%!\"'`@{}[]\\;:,.*?+#=~$-_world");
        tq.consume("hello");
        assertEquals("^&|<>()%!\"'`@{}[]\\;:,.*?+#=~$-_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellReservedChars() {
        TokenQueue tq = new TokenQueue("hello@$()[]{};,.?*&^%#!|\\\"'`~+-=<>:world");
        tq.consume("hello");
        assertEquals("@$()[]{};,.?*&^%#!|\\\"'`~+-=<>:world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashReservedChars() {
        TokenQueue tq = new TokenQueue("hello|&;()<>$`\\\"' \t\n*?[]{}~#%!^world");
        tq.consume("hello");
        assertEquals("|&;()<>$`\\\"' \t\n*?[]{}~#%!^world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdReservedChars() {
        TokenQueue tq = new TokenQueue("hello&|<>^()%!\"'`@{}[]\\;:,.*?+#=~$-_world");
        tq.consume("hello");
        assertEquals("&|<>^()%!\"'`@{}[]\\;:,.*?+#=~$-_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$variable_world");
        tq.consume("hello");
        assertEquals("$variable_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("hello$variable_world");
        tq.consume("hello");
        assertEquals("$variable_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("hello%variable%world");
        tq.consume("hello");
        assertEquals("%variable%world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellEnvChars() {
        TokenQueue tq = new TokenQueue("hello$env:variable_world");
        tq.consume("hello");
        assertEquals("$env:variable_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashEnvChars() {
        TokenQueue tq = new TokenQueue("hello$VARIABLE_world");
        tq.consume("hello");
        assertEquals("$VARIABLE_world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdEnvChars() {
        TokenQueue tq = new TokenQueue("hello%VARIABLE%world");
        tq.consume("hello");
        assertEquals("%VARIABLE%world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPathChars() {
        TokenQueue tq = new TokenQueue("helloC:\\Users\\user\\Documents\\file.txtworld");
        tq.consume("hello");
        assertEquals("C:\\Users\\user\\Documents\\file.txtworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPathChars() {
        TokenQueue tq = new TokenQueue("hello/home/user/Documents/file.txtworld");
        tq.consume("hello");
        assertEquals("/home/user/Documents/file.txtworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPathChars() {
        TokenQueue tq = new TokenQueue("helloC:\\Users\\user\\Documents\\file.txtworld");
        tq.consume("hello");
        assertEquals("C:\\Users\\user\\Documents\\file.txtworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellCommandChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test'world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashCommandChars() {
        TokenQueue tq = new TokenQueue("hellols -la /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdCommandChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellScriptChars() {
        TokenQueue tq = new TokenQueue("hello$script:variable = 'value'world");
        tq.consume("hello");
        assertEquals("$script:variable = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashScriptChars() {
        TokenQueue tq = new TokenQueue("hello#!/bin/bash\necho 'hello'world");
        tq.consume("hello");
        assertEquals("#!/bin/bash\necho 'hello'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdScriptChars() {
        TokenQueue tq = new TokenQueue("hello@echo off\necho hello\nworld");
        tq.consume("hello");
        assertEquals("@echo off\necho hello\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdClassChars() {
        TokenQueue tq = new TokenQueue("helloclass Test { public $name; }world");
        tq.consume("hello");
        assertEquals("class Test { public $name; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellModuleChars() {
        TokenQueue tq = new TokenQueue("helloImport-Module 'test'world");
        tq.consume("hello");
        assertEquals("Import-Module 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashModuleChars() {
        TokenQueue tq = new TokenQueue("hellosource /path/to/module.shworld");
        tq.consume("hello");
        assertEquals("source /path/to/module.shworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdModuleChars() {
        TokenQueue tq = new TokenQueue("hellocall C:\\path\\to\\module.batworld");
        tq.consume("hello");
        assertEquals("call C:\\path\\to\\module.batworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellAliasChars() {
        TokenQueue tq = new TokenQueue("helloSet-Alias -Name 'test' -Value 'Get-Process'world");
        tq.consume("hello");
        assertEquals("Set-Alias -Name 'test' -Value 'Get-Process'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashAliasChars() {
        TokenQueue tq = new TokenQueue("helloalias test='ls -la'world");
        tq.consume("hello");
        assertEquals("alias test='ls -la'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdAliasChars() {
        TokenQueue tq = new TokenQueue("hellodoskey test=dirworld");
        tq.consume("hello");
        assertEquals("doskey test=dirworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellVariableChars() {
        TokenQueue tq = new TokenQueue("hello$global:test = 'value'world");
        tq.consume("hello");
        assertEquals("$global:test = 'value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashVariableChars() {
        TokenQueue tq = new TokenQueue("helloexport TEST='value'world");
        tq.consume("hello");
        assertEquals("export TEST='value'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdVariableChars() {
        TokenQueue tq = new TokenQueue("helloset TEST=valueworld");
        tq.consume("hello");
        assertEquals("set TEST=valueworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellParameterChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Id 123world");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Id 123world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashParameterChars() {
        TokenQueue tq = new TokenQueue("hellols -la --color=auto /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la --color=auto /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdParameterChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellSwitchChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process -Name 'test' -Force -Verboseworld");
        tq.consume("hello");
        assertEquals("Get-Process -Name 'test' -Force -Verboseworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashSwitchChars() {
        TokenQueue tq = new TokenQueue("hellols -la -F -v /home/user/world");
        tq.consume("hello");
        assertEquals("ls -la -F -v /home/user/world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdSwitchChars() {
        TokenQueue tq = new TokenQueue("hellodir /s /b /a /l C:\\Users\\world");
        tq.consume("hello");
        assertEquals("dir /s /b /a /l C:\\Users\\world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellPipelineChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world");
        tq.consume("hello");
        assertEquals("Get-Process | Where-Object { $_.Name -eq 'test' } | Select-Object -First 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashPipelineChars() {
        TokenQueue tq = new TokenQueue("hellops aux | grep 'test' | head -n 1world");
        tq.consume("hello");
        assertEquals("ps aux | grep 'test' | head -n 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdPipelineChars() {
        TokenQueue tq = new TokenQueue("hellodir | findstr 'test' | moreworld");
        tq.consume("hello");
        assertEquals("dir | findstr 'test' | moreworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellRedirectionChars() {
        TokenQueue tq = new TokenQueue("helloGet-Process > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("Get-Process > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellols > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("ls > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdRedirectionChars() {
        TokenQueue tq = new TokenQueue("hellodir > output.txt 2>&1world");
        tq.consume("hello");
        assertEquals("dir > output.txt 2>&1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -like 'test' -notlike 'test' -match 'test' -notmatch 'test' -contains 'test' -notcontains 'test' -in 'test' -notin 'test' -replace 'test' -split 'test' -join 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world");
        tq.consume("hello");
        assertEquals("1 -eq 1 -ne 2 -lt 3 -le 3 -gt 2 -ge 1 -a 'test' -o 'test' -z 'test' -n 'test' -f 'test' -d 'test' -e 'test' -r 'test' -w 'test' -x 'test'world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdOperatorChars() {
        TokenQueue tq = new TokenQueue("hello1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world");
        tq.consume("hello");
        assertEquals("1 EQU 1 NEQ 2 LSS 3 LEQ 3 GTR 2 GEQ 1world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor ($i = 0; $i -lt 10; $i++) { Write-Host $i }world");
        tq.consume("hello");
        assertEquals("for ($i = 0; $i -lt 10; $i++) { Write-Host $i }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor i in $(seq 1 10); do echo $i; doneworld");
        tq.consume("hello");
        assertEquals("for i in $(seq 1 10); do echo $i; doneworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdLoopChars() {
        TokenQueue tq = new TokenQueue("hellofor /L %i in (1,1,10) do echo %iworld");
        tq.consume("hello");
        assertEquals("for /L %i in (1,1,10) do echo %iworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif ($true) { Write-Host 'true' } else { Write-Host 'false' }world");
        tq.consume("hello");
        assertEquals("if ($true) { Write-Host 'true' } else { Write-Host 'false' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif [ $true ]; then echo 'true'; else echo 'false'; fiworld");
        tq.consume("hello");
        assertEquals("if [ $true ]; then echo 'true'; else echo 'false'; fiworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdConditionalChars() {
        TokenQueue tq = new TokenQueue("helloif %1==1 (echo true) else (echo false)world");
        tq.consume("hello");
        assertEquals("if %1==1 (echo true) else (echo false)world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellFunctionChars() {
        TokenQueue tq = new TokenQueue("helloFunction Get-Test { param($test) return $test }world");
        tq.consume("hello");
        assertEquals("Function Get-Test { param($test) return $test }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllBashFunctionChars() {
        TokenQueue tq = new TokenQueue("hellofunction test() { echo 'hello'; }world");
        tq.consume("hello");
        assertEquals("function test() { echo 'hello'; }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllCmdFunctionChars() {
        TokenQueue tq = new TokenQueue("hello:label\ncall :label\nworld");
        tq.consume("hello");
        assertEquals(":label\ncall :label\nworld", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_AfterConsumeString_WithAllPowerShellClassChars() {
        TokenQueue tq = new TokenQueue("helloClass Test { [string]$Name = 'test' }world");
        tq.consume("hello");
        assertEquals("Class Test { [string]$Name = 'test' }world", tq.toString());
    }

    @Test(timeout = 4000)
    public void testToString_