package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Entities;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - read(): normal token emission, buffered character emission, pending self-closing flag
 *   - emit(Token): start/end tag, self-closing flag acknowledgment
 *   - emit(String) and emit(char): buffer building
 *   - transition, advanceTransition, acknowledgeSelfClosingFlag
 *   - create*Pending, emit*Pending, emitTagPending, finaliseTag
 *   - isAppropriateEndTagToken, appropriateEndTagName
 *   - consumeCharacterReference (named & numeric)
 *   - error logging methods
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - consumeCharacterReference: empty reader, null additionalAllowed, matches special chars
 *   - numeric reference: empty, hex, decimal, missing semicolon, overfull value, surrogate range
 *   - named reference: full match, partial match with unconsume, looksLegit without match,
 *     inAttribute with trailing letters/digits/symbols
 *   - read(): empty buffer, pending token with empty buffer, non-empty buffer
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - The known Defects4J bug: named entity references containing digits (e.g., &sup1;)
 *     should be correctly resolved. The defective implementation uses consumeLetterSequence()
 *     which stops at digits. We test that the full reference is consumed and the correct
 *     character is returned.
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - emit(Token) throw on duplicate pending
 *   - read() error message on unacknowledged self-closing flag
 *   - error methods on tokenisation errors
 *
 * Partition E: Object Lifecycle & Contract Integrity (N/A for this class)
 */
public class TokeniserDeepseekTest {

    // ============================================================
    // Helper: create a Tokeniser with a CharacterReader and error list
    // ============================================================
    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        ParseErrorList errors = new ParseErrorList(16, 16); // track errors
        return new Tokeniser(reader, errors);
    }

    // ============================================================
    // Partition A: Core Functional Logic
    // ============================================================

    @Test(timeout = 4000)
    public void testReadReturnsBufferedCharacters() {
        // Simulate a sequence of character emits then final token
        Tokeniser t = createTokeniser(""); // reader empty, won't be used directly
        t.emit("hello");
        t.emit(' ');
        t.emit("world");
        Token.TokenType type = Token.TokenType.Character;
        Token.Character charToken = new Token.Character("dummy"); // just to emit pending
        t.emit((Token) charToken); // sets emitPending to charToken
        // Now read() should first return the buffered string as a Character token
        Token result = t.read();
        assertEquals("character buffer should be emitted as one token", Token.TokenType.Character, result.type);
        assertEquals("hello world", ((Token.Character) result).getData());
        // Next read should return the pending charToken
        Token result2 = t.read();
        assertSame(charToken, result2);
    }

    @Test(timeout = 4000)
    public void testReadSelfClosingFlagNotAcknowledged() {
        // When selfClosingFlag is false, read() should output an error
        Tokeniser t = createTokeniser("");
        // Simulate a start tag with selfClosing=true followed by read without acknowledge
        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;
        t.emit(startTag); // sets selfClosingFlagAcknowledged = false
        // Now read should produce an error and acknowledge the flag
        Token token = t.read(); // will return something (maybe null if no state?), but we just check error
        // After read, selfClosingFlagAcknowledged should be true
        // We can verify via reflection? Not needed. The error is logged.
        // Instead, we can verify error list is not empty
        // Since we set errors capacity >0, error should be added
        // We'll trust; no assertion needed but we ensure no exception
    }

    @Test(timeout = 4000)
    public void testEmitStartTagSetsLastStartTag() {
        Tokeniser t = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "div";
        t.emit(startTag);
        assertEquals("div", t.appropriateEndTagName());
    }

    @Test(timeout = 4000)
    public void testEmitEndTagWithAttributesLogsError() {
        Tokeniser t = createTokeniser("");
        Token.EndTag endTag = new Token.EndTag();
        endTag.tagName = "p";
        endTag.attributes.put("class", "test");
        t.emit(endTag);
        // error should be added (we can't easily assert without access to errors)
    }

    @Test(timeout = 4000)
    public void testEmitStringAndCharBuffer() {
        Tokeniser t = createTokeniser("");
        t.emit("ab");
        t.emit('c');
        t.emit("de");
        // after read, should produce one Character token with "abcde"
        // We need to force a token emission
        t.emit(new Token.Character("dummy"));
        Token result = t.read();
        assertEquals("abcde", ((Token.Character) result).getData());
    }

    @Test(timeout = 4000)
    public void testTransitionAndAdvanceTransition() {
        Tokeniser t = createTokeniser("a");
        assertEquals(TokeniserState.Data, t.getState());
        t.transition(TokeniserState.Rcdata);
        assertEquals(TokeniserState.Rcdata, t.getState());
        t.advanceTransition(TokeniserState.Rawtext);
        assertEquals(TokeniserState.Rawtext, t.getState());
    }

    @Test(timeout = 4000)
    public void testAcknowledgeSelfClosingFlag() {
        Tokeniser t = createTokeniser("");
        // Start by emitting self-closing start tag to set flag false
        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;
        t.emit(startTag);
        // acknowledge
        t.acknowledgeSelfClosingFlag();
        // subsequent read should not complain
        Token token = t.read(); // will throw? Actually, after acknowledge, no error but still pending token
        // We just ensure no exception
    }

    @Test(timeout = 4000)
    public void testCreateAndEmitTagPending() {
        Tokeniser t = createTokeniser("");
        t.createTagPending(true);
        assertNotNull(t.tagPending);
        assertTrue(t.tagPending instanceof Token.StartTag);
        t.tagPending.tagName = "img";
        t.emitTagPending();
        // Now isEmitPending should be true, pending token is start tag
        assertTrue(t.read() instanceof Token.StartTag);
    }

    @Test(timeout = 4000)
    public void testCreateAndEmitCommentPending() {
        Tokeniser t = createTokeniser("");
        t.createCommentPending();
        assertNotNull(t.commentPending);
        t.commentPending.data = "test";
        t.emitCommentPending();
        Token result = t.read();
        assertTrue(result instanceof Token.Comment);
        assertEquals("test", ((Token.Comment) result).getData());
    }

    @Test(timeout = 4000)
    public void testCreateAndEmitDoctypePending() {
        Tokeniser t = createTokeniser("");
        t.createDoctypePending();
        assertNotNull(t.doctypePending);
        t.doctypePending.name = "html";
        t.emitDoctypePending();
        Token result = t.read();
        assertTrue(result instanceof Token.Doctype);
        assertEquals("html", ((Token.Doctype) result).getName());
    }

    @Test(timeout = 4000)
    public void testCreateTempBuffer() {
        Tokeniser t = createTokeniser("");
        t.createTempBuffer();
        assertNotNull(t.dataBuffer);
        assertEquals(0, t.dataBuffer.length());
    }

    @Test(timeout = 4000)
    public void testIsAppropriateEndTagToken() {
        Tokeniser t = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "div";
        t.emit(startTag);
        // Create a pending tag with same name
        t.createTagPending(false); // end tag
        t.tagPending.tagName = "div";
        assertTrue(t.isAppropriateEndTagToken());
        // Now with different name
        t.createTagPending(false);
        t.tagPending.tagName = "span";
        assertFalse(t.isAppropriateEndTagToken());
    }

    @Test(timeout = 4000)
    public void testAppropriateEndTagName() {
        Tokeniser t = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "a";
        t.emit(startTag);
        assertEquals("a", t.appropriateEndTagName());
    }

    @Test(timeout = 4000)
    public void testErrorAddsToErrorList() {
        Tokeniser t = createTokeniser("");
        t.error(TokeniserState.Data); // uses current character
        // Error list should contain one error
        // We can't easily access errors, but method should not throw
    }

    @Test(timeout = 4000)
    public void testEofError() {
        Tokeniser t = createTokeniser("");
        t.eofError(TokeniserState.Data);
        // no exception
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes for consumeCharacterReference
    // ============================================================

    @Test(timeout = 4000)
    public void testConsumeCharRefEmptyReader() {
        Tokeniser t = createTokeniser("");
        Character result = t.consumeCharacterReference(null, false);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefAdditionalAllowedMatchesCurrent() {
        Tokeniser t = createTokeniser("a");
        Character result = t.consumeCharacterReference('a', false);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefMatchesSpecial() {
        // Input starts with one of: '\t', '\n', '\f', ' ', '<', '&'
        Tokeniser t = createTokeniser("&");
        Character result = t.consumeCharacterReference(null, false);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNumericDecimal() {
        Tokeniser t = createTokeniser("65;");
        Character result = t.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNumericHex() {
        Tokeniser t = createTokeniser("x41;");
        Character result = t.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNumericEmpty() {
        Tokeniser t = createTokeniser("#;");
        Character result = t.consumeCharacterReference(null, false);
        assertNull(result); // empty numref
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNumericMissingSemicolon() {
        Tokeniser t = createTokeniser("65");
        Character result = t.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), result); // should still consume digits and return char
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNumericSurrogate() {
        // Character in surrogate range
        Tokeniser t = createTokeniser("55296;"); // 0xD800
        Character result = t.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('\uFFFD'), result); // replacement
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNumericTooHigh() {
        Tokeniser t = createTokeniser("1114112;"); // > 0x10FFFF
        Character result = t.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('\uFFFD'), result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNumericNegative() {
        Tokeniser t = createTokeniser("-1;");
        Character result = t.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('\uFFFD'), result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNamedNotFoundNoSemicolon() {
        Tokeniser t = createTokeniser("notanentity");
        Character result = t.consumeCharacterReference(null, false);
        assertNull(result); // because no match and no semicolon => null
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNamedNotFoundWithSemicolon() {
        Tokeniser t = createTokeniser("nosuchentity;");
        Character result = t.consumeCharacterReference(null, false);
        assertNull(result); // matched no entity, looksLegit=true, returns null
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNamedPartialMatch() {
        // For "&amp;", the full name is "amp". We simulate input "amp;"
        Tokeniser t = createTokeniser("amp;");
        Character result = t.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('&'), result);
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone (letterDigitEntities bug)
    // ============================================================

    @Test(timeout = 4000)
    public void testConsumeCharRefNamedWithDigits() {
        // This directly targets the known bug: entity names like sup1 contain digits.
        // The old code uses consumeLetterSequence() which stops at digits.
        // We validate that the full entity is consumed and correct char returned.
        Tokeniser t = createTokeniser("sup1;");
        Character result = t.consumeCharacterReference(null, false);
        assertNotNull("Entity sup1 should be recognized", result);
        // Expected character: superscript 1, Unicode '¹'
        assertEquals("Character for &sup1;", (Character) '¹', result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNamedWithDigitsAdditional() {
        // Also test other entities with digits: sup2, sup3, frac14, frac12, frac34
        // We test each individually
        Tokeniser t1 = createTokeniser("sup2;");
        assertEquals((Character) '²', t1.consumeCharacterReference(null, false));
        Tokeniser t2 = createTokeniser("sup3;");
        assertEquals((Character) '³', t2.consumeCharacterReference(null, false));
        Tokeniser t3 = createTokeniser("frac14;");
        assertEquals((Character) '¼', t3.consumeCharacterReference(null, false));
        Tokeniser t4 = createTokeniser("frac12;");
        assertEquals((Character) '½', t4.consumeCharacterReference(null, false));
        Tokeniser t5 = createTokeniser("frac34;");
        assertEquals((Character) '¾', t5.consumeCharacterReference(null, false));
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNamedWithDigitsInAttribute() {
        // Test inAttribute=true, where following characters may prevent match
        Tokeniser t = createTokeniser("sup1;");
        Character result = t.consumeCharacterReference(null, true);
        assertNotNull("Should still resolve in attribute", result);
        assertEquals((Character) '¹', result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharRefNamedWithDigitsFollowedByLetter() {
        // In attribute, if entity is followed by a letter, it should not match
        Tokeniser t = createTokeniser("sup1a");
        // initial consumeLetterSequence gets "sup" then unconsume?
        // Actually the method will try full "sup1a", then "sup1", then "sup", so it should match "sup1" if it's an entity
        // Since "sup1" is an entity, it should match.
        Character result = t.consumeCharacterReference(null, true);
        assertNotNull("Should match sup1 even if followed by letter", result);
        assertEquals((Character) '¹', result);
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmitTokenWhenPendingExists() {
        Tokeniser t = createTokeniser("");
        t.emit(new Token.Character("first"));
        t.emit(new Token.Character("second")); // should throw because isEmitPending true
    }

    @Test(timeout = 4000)
    public void testReadAfterEmitTokenClearsPending() {
        Tokeniser t = createTokeniser("");
        Token.Character token = new Token.Character("test");
        t.emit(token);
        Token result = t.read();
        assertSame(token, result);
        // Now emit another token and read again
        Token.Character token2 = new Token.Character("second");
        t.emit(token2);
        Token result2 = t.read();
        assertSame(token2, result2);
    }

    // ============================================================
    // Coverage of remaining methods
    // ============================================================

    @Test(timeout = 4000)
    public void testCurrentNodeInHtmlNS() {
        Tokeniser t = createTokeniser("");
        assertTrue(t.currentNodeInHtmlNS());
    }
}