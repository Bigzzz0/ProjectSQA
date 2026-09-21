package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Advanced white-box test suite for Tokeniser, targeting the known self-closing flag defect.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Self-closing flag acknowledgment: branch in read() when !selfClosingFlagAcknowledged
 * - Error message: "Self closing flag not acknowledged" vs expected "Tag cannot be self closing; not a void tag"
 * - Void vs non-void tag handling: acknowledgment should happen for void tags, error for non-void
 * - Boundary: empty input, null additionalAllowedCharacter, numeric reference overflow
 * - State transitions: Data, TagName, etc.
 * - Character reference consumption: named, numeric, hex, missing semicolon, invalid range
 * - Buffer management: charsString vs charsBuilder
 * - Token emission: start tag, end tag, character, comment, doctype
 * - Error accumulation: ParseErrorList capacity
 */
public class TokeniserDeepseekTest {

    // Helper to create a Tokeniser with a given input string and a fresh error list
    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        ParseErrorList errors = new ParseErrorList(16, 16); // max 16 errors
        return new Tokeniser(reader, errors);
    }

    // Helper to get the error list from a Tokeniser via reflection (since errors is private)
    private List<ParseError> getErrors(Tokeniser tokeniser) throws Exception {
        java.lang.reflect.Field field = Tokeniser.class.getDeclaredField("errors");
        field.setAccessible(true);
        return (List<ParseError>) field.get(tokeniser);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testReadNormalData() throws Exception {
        Tokeniser t = createTokeniser("Hello");
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("Hello", token.asCharacter().getData());
        assertTrue(t.read().isEOF()); // next read should be EOF
    }

    @Test(timeout = 4000)
    public void testEmitStringBuffering() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit("a");
        t.emit("b");
        // No read yet, charsString should be null, charsBuilder should have "ab"
        // We need to read to trigger emission
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("ab", token.asCharacter().getData());
    }

    @Test(timeout = 4000)
    public void testEmitCharArray() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit(new char[]{'x', 'y'});
        Token token = t.read();
        assertEquals("xy", token.asCharacter().getData());
    }

    @Test(timeout = 4000)
    public void testEmitCodepoints() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit(new int[]{0x1F600}); // grinning face emoji
        Token token = t.read();
        assertEquals("\uD83D\uDE00", token.asCharacter().getData());
    }

    @Test(timeout = 4000)
    public void testEmitChar() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit('Z');
        Token token = t.read();
        assertEquals("Z", token.asCharacter().getData());
    }

    @Test(timeout = 4000)
    public void testTransitionAndAdvanceTransition() throws Exception {
        Tokeniser t = createTokeniser("a");
        assertEquals(TokeniserState.Data, t.getState());
        t.transition(TokeniserState.Rawtext);
        assertEquals(TokeniserState.Rawtext, t.getState());
        t.advanceTransition(TokeniserState.RCDATA);
        assertEquals(TokeniserState.RCDATA, t.getState());
        // reader should have advanced past 'a'
        assertTrue(t.reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCreateTagPendingAndEmitTagPending() throws Exception {
        Tokeniser t = createTokeniser("");
        Token.Tag tag = t.createTagPending(true);
        assertNotNull(tag);
        assertTrue(tag instanceof Token.StartTag);
        tag.tagName = "div";
        t.emitTagPending();
        Token emitted = t.read();
        assertEquals(Token.TokenType.StartTag, emitted.type);
        assertEquals("div", emitted.asStartTag().tagName);
    }

    @Test(timeout = 4000)
    public void testCreateCommentPendingAndEmit() throws Exception {
        Tokeniser t = createTokeniser("");
        t.createCommentPending();
        t.commentPending.data("test comment");
        t.emitCommentPending();
        Token token = t.read();
        assertEquals(Token.TokenType.Comment, token.type);
        assertEquals("test comment", token.asComment().getData());
    }

    @Test(timeout = 4000)
    public void testCreateDoctypePendingAndEmit() throws Exception {
        Tokeniser t = createTokeniser("");
        t.createDoctypePending();
        t.doctypePending.name("html");
        t.emitDoctypePending();
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        assertEquals("html", token.asDoctype().getName());
    }

    @Test(timeout = 4000)
    public void testIsAppropriateEndTagToken() throws Exception {
        Tokeniser t = createTokeniser("");
        // Emit a start tag to set lastStartTag
        Token.StartTag start = new Token.StartTag();
        start.tagName = "p";
        t.emit(start);
        // Now create an end tag pending
        t.createTagPending(false);
        t.tagPending.name("P"); // case-insensitive
        assertTrue(t.isAppropriateEndTagToken());
        t.tagPending.name("div");
        assertFalse(t.isAppropriateEndTagToken());
    }

    @Test(timeout = 4000)
    public void testAppropriateEndTagName() throws Exception {
        Tokeniser t = createTokeniser("");
        assertNull(t.appropriateEndTagName());
        Token.StartTag start = new Token.StartTag();
        start.tagName = "span";
        t.emit(start);
        assertEquals("span", t.appropriateEndTagName());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testReadEmptyInput() throws Exception {
        Tokeniser t = createTokeniser("");
        Token token = t.read();
        assertTrue(token.isEOF());
    }

    @Test(timeout = 4000)
    public void testEmitNullString() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit((String) null);
        Token token = t.read();
        assertNull(token.asCharacter().getData()); // null string emitted
    }

    @Test(timeout = 4000)
    public void testEmitEmptyCharArray() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit(new char[0]);
        Token token = t.read();
        assertEquals("", token.asCharacter().getData());
    }

    @Test(timeout = 4000)
    public void testEmitEmptyCodepoints() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit(new int[0]);
        Token token = t.read();
        assertEquals("", token.asCharacter().getData());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNullAdditional() throws Exception {
        Tokeniser t = createTokeniser("&amp;");
        int[] result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals('&', result[0]); // &amp; -> &
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceWithAdditional() throws Exception {
        Tokeniser t = createTokeniser("&amp;");
        // additionalAllowedCharacter = '&' should cause null return
        int[] result = t.consumeCharacterReference('&', false);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumeric() throws Exception {
        Tokeniser t = createTokeniser("&#65;");
        int[] result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals('A', result[0]);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceHex() throws Exception {
        Tokeniser t = createTokeniser("&#x41;");
        int[] result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('A', result[0]);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceMissingSemicolon() throws Exception {
        Tokeniser t = createTokeniser("&#65");
        int[] result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('A', result[0]);
        // Should have added an error for missing semicolon
        List<ParseError> errors = getErrors(t);
        assertTrue(errors.size() > 0);
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceInvalidRange() throws Exception {
        Tokeniser t = createTokeniser("&#xD800;"); // surrogate
        int[] result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Tokeniser.replacementChar, result[0]);
        List<ParseError> errors = getErrors(t);
        assertTrue(errors.size() > 0);
        assertTrue(errors.get(0).getErrorMessage().contains("outside of valid range"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceOverflow() throws Exception {
        Tokeniser t = createTokeniser("&#x110000;"); // > 0x10FFFF
        int[] result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Tokeniser.replacementChar, result[0]);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedWithoutSemicolon() throws Exception {
        Tokeniser t = createTokeniser("&amp"); // no semicolon, but base named entity
        int[] result = t.consumeCharacterReference(null, false);
        assertNull(result); // should not match because no semicolon and not extended? Actually base named entity without ; is not found
        // The method returns null if not found
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedWithSemicolon() throws Exception {
        Tokeniser t = createTokeniser("&lt;");
        int[] result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('<', result[0]);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceInAttributeWithFollowingChars() throws Exception {
        Tokeniser t = createTokeniser("&amp=test"); // in attribute, after &amp there is '='
        int[] result = t.consumeCharacterReference(null, true);
        assertNull(result); // should not match because in attribute and next char is '='
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect: self-closing flag not acknowledged for non-void tags produces wrong error message.
     * Expected: "Tag cannot be self closing; not a void tag"
     * Actual (defective): "Self closing flag not acknowledged"
     */
    @Test(timeout = 4000)
    public void testSelfClosingNonVoidTagError() throws Exception {
        Tokeniser t = createTokeniser("<div />");
        // First read emits the start tag with selfClosing=true
        Token first = t.read();
        assertEquals(Token.TokenType.StartTag, first.type);
        assertTrue(first.asStartTag().selfClosing);
        // Second read should trigger the error check
        Token second = t.read(); // This will be EOF or something else
        List<ParseError> errors = getErrors(t);
        assertTrue("Expected at least one error", errors.size() > 0);
        String errorMsg = errors.get(0).getErrorMessage();
        // The correct behavior should be "Tag cannot be self closing; not a void tag"
        // The defective version gives "Self closing flag not acknowledged"
        assertEquals("Tag cannot be self closing; not a void tag", errorMsg);
    }

    /**
     * Defect: self-closing void tag should not produce an error.
     * Expected: 0 errors
     * Actual (defective): 2 errors (one for self-closing flag not acknowledged, maybe another)
     */
    @Test(timeout = 4000)
    public void testSelfClosingVoidTagNoError() throws Exception {
        Tokeniser t = createTokeniser("<br />");
        // First read emits the start tag
        Token first = t.read();
        assertEquals(Token.TokenType.StartTag, first.type);
        assertTrue(first.asStartTag().selfClosing);
        // For void tags, the tree builder should acknowledge the flag.
        // Simulate acknowledgment:
        t.acknowledgeSelfClosingFlag();
        // Second read should not produce an error
        Token second = t.read();
        List<ParseError> errors = getErrors(t);
        assertEquals("Expected no errors for void self-closing tag", 0, errors.size());
    }

    /**
     * Defect: tracksErrorsWhenRequested expects 50 errors but gets 50 with wrong message.
     * This test checks that the error count and message are correct for a sequence of self-closing non-void tags.
     */
    @Test(timeout = 4000)
    public void testMultipleSelfClosingNonVoidErrors() throws Exception {
        // Create a long input with many self-closing non-void tags
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            sb.append("<div />");
        }
        Tokeniser t = createTokeniser(sb.toString());
        // Read all tokens; after each start tag, the next read will produce an error
        int errorCount = 0;
        while (!t.reader.isEmpty()) {
            Token token = t.read();
            if (token.isEOF()) break;
            // After each start tag, the next read will trigger error
            // But we need to count errors after each read
        }
        List<ParseError> errors = getErrors(t);
        // Each self-closing non-void tag should produce one error (except maybe the last one if EOF)
        // Expected: 25 errors (one per tag)
        assertEquals("Expected 25 errors for 25 self-closing non-void tags", 25, errors.size());
        for (ParseError error : errors) {
            assertEquals("Tag cannot be self closing; not a void tag", error.getErrorMessage());
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEmitWhenPending() throws Exception {
        Tokeniser t = createTokeniser("");
        t.emit(new Token.StartTag());
        // Emitting again without reading should throw
        t.emit(new Token.EndTag());
    }

    @Test(timeout = 4000)
    public void testErrorMethod() throws Exception {
        Tokeniser t = createTokeniser("");
        t.error("custom error");
        List<ParseError> errors = getErrors(t);
        assertEquals(1, errors.size());
        assertEquals("custom error", errors.get(0).getErrorMessage());
    }

    @Test(timeout = 4000)
    public void testEofError() throws Exception {
        Tokeniser t = createTokeniser("");
        t.eofError(TokeniserState.Data);
        List<ParseError> errors = getErrors(t);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("EOF"));
    }

    @Test(timeout = 4000)
    public void testCharacterReferenceError() throws Exception {
        Tokeniser t = createTokeniser("");
        // Trigger a character reference error via consumeCharacterReference with invalid input
        t.consumeCharacterReference(null, false); // empty reader, should return null and no error? Actually if reader empty, returns null
        // Force error by calling private method via reflection? We can use the public method that calls it.
        // Instead, we can call the private method via reflection or use a scenario that triggers it.
        // For simplicity, we'll test via consumeCharacterReference with a numeric reference that has no numerals.
        Tokeniser t2 = createTokeniser("&#");
        int[] result = t2.consumeCharacterReference(null, false);
        assertNull(result);
        List<ParseError> errors = getErrors(t2);
        assertTrue(errors.size() > 0);
        assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntities() throws Exception {
        Tokeniser t = createTokeniser("a&amp;b");
        String result = t.unescapeEntities(false);
        assertEquals("a&b", result);
    }

    @Test(timeout = 4000)
    public void testUnescapeEntitiesInAttribute() throws Exception {
        Tokeniser t = createTokeniser("a&amp;b");
        String result = t.unescapeEntities(true);
        assertEquals("a&b", result);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testAcknowledgeSelfClosingFlag() throws Exception {
        Tokeniser t = createTokeniser("");
        // Initially true
        assertTrue(t.selfClosingFlagAcknowledged); // private field, but we can test indirectly
        // Emit a self-closing start tag sets it to false
        Token.StartTag start = new Token.StartTag();
        start.selfClosing = true;
        t.emit(start);
        // Now acknowledge
        t.acknowledgeSelfClosingFlag();
        // Next read should not produce error
        Token token = t.read(); // will be EOF
        List<ParseError> errors = getErrors(t);
        assertEquals(0, errors.size());
    }

    @Test(timeout = 4000)
    public void testCurrentNodeInHtmlNS() throws Exception {
        Tokeniser t = createTokeniser("");
        assertTrue(t.currentNodeInHtmlNS());
    }

    @Test(timeout = 4000)
    public void testCreateTempBuffer() throws Exception {
        Tokeniser t = createTokeniser("");
        t.createTempBuffer();
        // dataBuffer should be reset
        assertEquals(0, t.dataBuffer.length());
    }

    @Test(timeout = 4000)
    public void testTagPendingReset() throws Exception {
        Tokeniser t = createTokeniser("");
        Token.Tag tag = t.createTagPending(true);
        assertNotNull(tag);
        // After reset, tag should be clean
        assertEquals("", tag.name());
    }

    @Test(timeout = 4000)
    public void testDoctypePendingReset() throws Exception {
        Tokeniser t = createTokeniser("");
        t.createDoctypePending();
        t.doctypePending.name("html");
        t.createDoctypePending(); // reset
        assertNull(t.doctypePending.name());
    }

    @Test(timeout = 4000)
    public void testCommentPendingReset() throws Exception {
        Tokeniser t = createTokeniser("");
        t.createCommentPending();
        t.commentPending.data("test");
        t.createCommentPending(); // reset
        assertNull(t.commentPending.getData());
    }

    @Test(timeout = 4000)
    public void testEmitEndTagWithAttributesError() throws Exception {
        Tokeniser t = createTokeniser("");
        Token.EndTag end = new Token.EndTag();
        end.attributes = new org.jsoup.nodes.Attributes(); // simulate attributes
        t.emit(end);
        List<ParseError> errors = getErrors(t);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Attributes incorrectly present on end tag"));
    }
}