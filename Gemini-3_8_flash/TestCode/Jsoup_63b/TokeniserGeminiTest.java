/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Tokeniser
 *
 * Decision / Condition Coverage Targets:
 * 1. Self-Closing Flag Tracking:
 *    - Emit start tag with selfClosing = true -> sets selfClosingFlagAcknowledged = false.
 *    - Next read() when selfClosingFlagAcknowledged == false: logs error "Self closing flag not acknowledged".
 *    - Next read() when acknowledgeSelfClosingFlag() called: selfClosingFlagAcknowledged == true, no error logged.
 *    - Emit start tag with selfClosing = false: selfClosingFlagAcknowledged remains true.
 * 2. Unread Token Pending Guard:
 *    - emit(token) called while isEmitPending == true -> Validate.isFalse throws IllegalArgumentException.
 * 3. EndTag Attribute Validation:
 *    - Emit EndTag with attributes != null -> logs error "Attributes incorrectly present on end tag".
 *    - Emit EndTag with attributes == null -> no error logged.
 * 4. Character Reference Consumption (consumeCharacterReference):
 *    - Empty reader -> returns null.
 *    - additionalAllowedCharacter match -> returns null.
 *    - notCharRefCharsSorted match ('\t', '\n', '\r', '\f', ' ', '<', '&') -> returns null.
 *    - Hex numeric entity (#x or #X): valid hex, missing semicolon, empty numerals, out of range (>0x10FFFF, surrogates), NumberFormatException.
 *    - Decimal numeric entity (#): valid decimal, missing semicolon, empty numerals, surrogate/out of range.
 *    - Named entity: valid base entity (with/without semi), extended entity with semi, unknown entity with semi (parse error),
 *      inAttribute with trailing letter/digit/('=','-','_') -> rewinds and returns null.
 *    - Multi-codepoint named entity handling (1 codepoint vs 2 codepoints).
 * 5. Character Buffering & Token Emission:
 *    - emit(String) with charsString == null -> buffers in charsString.
 *    - emit(String) with charsString != null -> transitions to charsBuilder.
 *    - emit(char[]), emit(int[]), emit(char).
 *    - read() draining charsBuilder first, charsString next, then emitPending.
 * 6. Tag Pending / Buffer Lifecycle:
 *    - createTagPending(true/false) returning startPending vs endPending.
 *    - isAppropriateEndTagToken() with null lastStartTag, matching (case-insensitive), non-matching.
 *    - appropriateEndTagName() returning null or lastStartTag.
 *    - createCommentPending(), emitCommentPending(), createDoctypePending(), emitDoctypePending().
 *    - createTempBuffer() resetting dataBuffer.
 * 7. State Transitions & Helpers:
 *    - transition(state), advanceTransition(state), getState().
 *    - error(state), eofError(state), currentNodeInHtmlNS().
 *    - unescapeEntities(inAttribute=true/false) with combinations of text, entities, and stray '&'.
 */

package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStateTransitionAndAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        assertEquals(TokeniserState.Data, tokeniser.getState());

        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
        assertEquals('a', reader.current());

        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testEmitPendingCharacterSequence() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        // Single string emit
        tokeniser.emit("Hello");
        Token.StartTag tag = new Token.StartTag();
        tag.name("p");
        tokeniser.emit(tag);

        Token token1 = tokeniser.read();
        assertTrue(token1.isCharacter());
        assertEquals("Hello", ((Token.Character) token1).getData());

        Token token2 = tokeniser.read();
        assertTrue(token2.isStartTag());
        assertEquals("p", ((Token.StartTag) token2).name());
    }

    @Test(timeout = 4000)
    public void testEmitMultipleCharacterFragmentsBuffersInBuilder() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit("Part1");
        tokeniser.emit("Part2");
        tokeniser.emit(new char[]{'P', '3'});
        tokeniser.emit(new int[]{0x50, 0x34}); // "P4"
        tokeniser.emit('5');

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        tokeniser.emit(endTag);

        Token charToken = tokeniser.read();
        assertTrue(charToken.isCharacter());
        assertEquals("Part1Part2P3P45", ((Token.Character) charToken).getData());

        Token tagToken = tokeniser.read();
        assertTrue(tagToken.isEndTag());
        assertEquals("div", ((Token.EndTag) tagToken).name());
    }

    @Test(timeout = 4000)
    public void testAppropriateEndTagToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        assertNull(tokeniser.appropriateEndTagName());
        assertFalse(tokeniser.isAppropriateEndTagToken());

        Token.StartTag startTag = (Token.StartTag) tokeniser.createTagPending(true);
        startTag.name("title");
        tokeniser.emitTagPending();
        tokeniser.read(); // Emits start tag, sets lastStartTag = "title"

        assertEquals("title", tokeniser.appropriateEndTagName());

        Token.EndTag endTag = (Token.EndTag) tokeniser.createTagPending(false);
        endTag.name("TITLE");
        assertTrue(tokeniser.isAppropriateEndTagToken());

        endTag.name("body");
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    @Test(timeout = 4000)
    public void testCommentAndDoctypePendingLifecycles() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("a comment");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals("a comment", ((Token.Comment) token).getData());

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token doctype = tokeniser.read();
        assertTrue(doctype.isDoctype());
        assertEquals("html", ((Token.Doctype) doctype).getName());
    }

    @Test(timeout = 4000)
    public void testDataBufferAndHtmlNamespace() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.dataBuffer.append("temp data");
        assertEquals("temp data", tokeniser.dataBuffer.toString());

        tokeniser.createTempBuffer();
        assertEquals(0, tokeniser.dataBuffer.length());
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Character References
    // =========================================================================

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(5));

        assertNull(tokeniser.consumeCharacterReference(null, false));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceAdditionalAllowedMatch() {
        CharacterReader reader = new CharacterReader("\"quoted\"");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(5));

        assertNull(tokeniser.consumeCharacterReference('\"', true));
        assertEquals('\"', reader.current()); // Reader should not advance
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNotCharRefChars() {
        char[] ignoredChars = new char[]{'\t', '\n', '\r', '\f', ' ', '<', '&'};
        for (char c : ignoredChars) {
            CharacterReader reader = new CharacterReader(String.valueOf(c));
            Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(5));
            assertNull("Character " + ((int) c) + " should not be consumed as ref",
                    tokeniser.consumeCharacterReference(null, false));
            assertEquals(c, reader.current());
        }
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceHexNumbers() {
        // Normal hex
        CharacterReader r1 = new CharacterReader("#x41;");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(5));
        int[] code1 = t1.consumeCharacterReference(null, false);
        assertNotNull(code1);
        assertEquals(0x41, code1[0]);

        // Upper case X, missing semi
        CharacterReader r2 = new CharacterReader("#X42");
        ParseErrorList errors2 = ParseErrorList.tracking(5);
        Tokeniser t2 = new Tokeniser(r2, errors2);
        int[] code2 = t2.consumeCharacterReference(null, false);
        assertNotNull(code2);
        assertEquals(0x42, code2[0]);
        assertEquals(1, errors2.size());
        assertTrue(errors2.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceDecimalNumbers() {
        // Normal decimal
        CharacterReader r1 = new CharacterReader("#65;");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(5));
        int[] code1 = t1.consumeCharacterReference(null, false);
        assertNotNull(code1);
        assertEquals(65, code1[0]);

        // Missing semicolon decimal
        CharacterReader r2 = new CharacterReader("#66");
        ParseErrorList errors2 = ParseErrorList.tracking(5);
        Tokeniser t2 = new Tokeniser(r2, errors2);
        int[] code2 = t2.consumeCharacterReference(null, false);
        assertNotNull(code2);
        assertEquals(66, code2[0]);
        assertEquals(1, errors2.size());
        assertTrue(errors2.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericErrorsAndBoundaries() {
        // No numerals after #
        CharacterReader r1 = new CharacterReader("#;");
        ParseErrorList errors1 = ParseErrorList.tracking(5);
        Tokeniser t1 = new Tokeniser(r1, errors1);
        assertNull(t1.consumeCharacterReference(null, false));
        assertEquals('#', r1.current()); // rewound
        assertEquals(1, errors1.size());
        assertTrue(errors1.get(0).getErrorMessage().contains("numeric reference with no numerals"));

        // Hex no numerals after #x
        CharacterReader r2 = new CharacterReader("#x;");
        ParseErrorList errors2 = ParseErrorList.tracking(5);
        Tokeniser t2 = new Tokeniser(r2, errors2);
        assertNull(t2.consumeCharacterReference(null, false));
        assertEquals('#', r2.current()); // rewound
        assertEquals(1, errors2.size());

        // Surrogate range (0xD800 - 0xDFFF)
        CharacterReader r3 = new CharacterReader("#xD800;");
        ParseErrorList errors3 = ParseErrorList.tracking(5);
        Tokeniser t3 = new Tokeniser(r3, errors3);
        int[] code3 = t3.consumeCharacterReference(null, false);
        assertNotNull(code3);
        assertEquals(Tokeniser.replacementChar, (char) code3[0]);
        assertEquals(1, errors3.size());
        assertTrue(errors3.get(0).getErrorMessage().contains("character outside of valid range"));

        // Out of max unicode range (> 0x10FFFF)
        CharacterReader r4 = new CharacterReader("#x110000;");
        ParseErrorList errors4 = ParseErrorList.tracking(5);
        Tokeniser t4 = new Tokeniser(r4, errors4);
        int[] code4 = t4.consumeCharacterReference(null, false);
        assertNotNull(code4);
        assertEquals(Tokeniser.replacementChar, (char) code4[0]);
        assertEquals(1, errors4.size());
        assertTrue(errors4.get(0).getErrorMessage().contains("character outside of valid range"));

        // Huge number causing NumberFormatException
        CharacterReader r5 = new CharacterReader("#99999999999999999999999999;");
        ParseErrorList errors5 = ParseErrorList.tracking(5);
        Tokeniser t5 = new Tokeniser(r5, errors5);
        int[] code5 = t5.consumeCharacterReference(null, false);
        assertNotNull(code5);
        assertEquals(Tokeniser.replacementChar, (char) code5[0]);
        assertEquals(1, errors5.size());
        assertTrue(errors5.get(0).getErrorMessage().contains("character outside of valid range"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedEntities() {
        // Base named entity with semicolon
        CharacterReader r1 = new CharacterReader("lt;");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(5));
        int[] code1 = t1.consumeCharacterReference(null, false);
        assertNotNull(code1);
        assertEquals('<', (char) code1[0]);

        // Base named entity without semicolon
        CharacterReader r2 = new CharacterReader("gt next");
        ParseErrorList errors2 = ParseErrorList.tracking(5);
        Tokeniser t2 = new Tokeniser(r2, errors2);
        int[] code2 = t2.consumeCharacterReference(null, false);
        assertNotNull(code2);
        assertEquals('>', (char) code2[0]);
        assertEquals(1, errors2.size());
        assertTrue(errors2.get(0).getErrorMessage().contains("missing semicolon"));

        // Unknown named entity followed by semicolon -> error recorded and rewound
        CharacterReader r3 = new CharacterReader("thisisnotanentity;");
        ParseErrorList errors3 = ParseErrorList.tracking(5);
        Tokeniser t3 = new Tokeniser(r3, errors3);
        assertNull(t3.consumeCharacterReference(null, false));
        assertEquals('t', r3.current()); // rewound
        assertEquals(1, errors3.size());
        assertTrue(errors3.get(0).getErrorMessage().contains("invalid named referenece"));

        // Unknown entity without semicolon -> no error, rewound
        CharacterReader r4 = new CharacterReader("notanentity next");
        ParseErrorList errors4 = ParseErrorList.tracking(5);
        Tokeniser t4 = new Tokeniser(r4, errors4);
        assertNull(t4.consumeCharacterReference(null, false));
        assertEquals('n', r4.current());
        assertEquals(0, errors4.size());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceInAttributeDisallowedFollowUp() {
        // In attribute, entity name followed by '=', should rewind and not consume
        CharacterReader r1 = new CharacterReader("notit=something");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(5));
        assertNull(t1.consumeCharacterReference(null, true));
        assertEquals('n', r1.current());

        // In attribute, entity followed by alphanumeric or '-', '_'
        CharacterReader r2 = new CharacterReader("lt123");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(5));
        assertNull(t2.consumeCharacterReference(null, true));
        assertEquals('l', r2.current());

        CharacterReader r3 = new CharacterReader("lt-more");
        Tokeniser t3 = new Tokeniser(r3, ParseErrorList.tracking(5));
        assertNull(t3.consumeCharacterReference(null, true));
        assertEquals('l', r3.current());

        CharacterReader r4 = new CharacterReader("lt_more");
        Tokeniser t4 = new Tokeniser(r4, ParseErrorList.tracking(5));
        assertNull(t4.consumeCharacterReference(null, true));
        assertEquals('l', r4.current());
    }

    @Test(timeout = 4000)
    public void testUnescapeEntities() {
        CharacterReader reader = new CharacterReader("foo &amp; bar &lt; baz &#x30; &#65; &notanentity; &");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        String unescaped = tokeniser.unescapeEntities(false);
        assertEquals("foo & bar < baz 0 A &notanentity; &", unescaped);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Self-Closing & EndTag Attributes)
    // =========================================================================

    /**
     * Targets Defects4J issue regarding Self-Closing Flag tracking:
     * When a StartTag is emitted with selfClosing = true, selfClosingFlagAcknowledged becomes false.
     * If acknowledgeSelfClosingFlag() is called, subsequent read() must NOT report an error.
     */
    @Test(timeout = 4000)
    public void testSelfClosingFlagAcknowledgedSuppressesError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag tag = (Token.StartTag) tokeniser.createTagPending(true);
        tag.name("img");
        tag.selfClosing = true;
        tokeniser.emitTagPending();

        Token emittedTag = tokeniser.read();
        assertTrue(emittedTag.isStartTag());
        assertTrue(((Token.StartTag) emittedTag).isSelfClosing());

        // Acknowledge the flag (as TreeBuilder would do for void tags like <img />)
        tokeniser.acknowledgeSelfClosingFlag();

        // Read next token (EOF)
        tokeniser.read();

        // Zero errors should have been recorded
        assertEquals(0, errors.size());
    }

    /**
     * Targets Defects4J issue: If a self-closing start tag is NOT acknowledged,
     * read() detects that selfClosingFlagAcknowledged is false and emits:
     * "Self closing flag not acknowledged".
     */
    @Test(timeout = 4000)
    public void testSelfClosingFlagNotAcknowledgedEmitsError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag tag = (Token.StartTag) tokeniser.createTagPending(true);
        tag.name("div");
        tag.selfClosing = true;
        tokeniser.emitTagPending();

        Token emittedTag = tokeniser.read();
        assertTrue(emittedTag.isStartTag());
        assertTrue(((Token.StartTag) emittedTag).isSelfClosing());

        // Do NOT acknowledge flag
        tokeniser.read();

        assertEquals(1, errors.size());
        assertEquals("Self closing flag not acknowledged", errors.get(0).getErrorMessage());
    }

    @Test(timeout = 4000)
    public void testEndTagWithAttributesEmitsError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.EndTag endTag = (Token.EndTag) tokeniser.createTagPending(false);
        endTag.name("div");
        endTag.attributes = new org.jsoup.nodes.Attributes();
        endTag.attributes.put("class", "error-test");
        tokeniser.emitTagPending();

        tokeniser.read();

        assertEquals(1, errors.size());
        assertEquals("Attributes incorrectly present on end tag", errors.get(0).getErrorMessage());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmitWhilePendingThrowsException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token.StartTag t1 = new Token.StartTag();
        t1.name("p");
        tokeniser.emit(t1);

        Token.StartTag t2 = new Token.StartTag();
        t2.name("span");
        // Should throw because isEmitPending is true
        tokeniser.emit(t2);
    }

    @Test(timeout = 4000)
    public void testErrorTrackingCapacities() {
        CharacterReader reader = new CharacterReader("input");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error("First error");
        assertEquals(1, errors.size());

        // Cannot add more errors once maxSize reached
        tokeniser.error(TokeniserState.Data);
        assertEquals(1, errors.size());

        tokeniser.eofError(TokeniserState.Data);
        assertEquals(1, errors.size());
    }

    @Test(timeout = 4000)
    public void testEofErrorAddsCorrectMessage() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.eofError(TokeniserState.TagOpen);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Unexpectedly reached end of file (EOF)"));
    }

    @Test(timeout = 4000)
    public void testStateErrorAddsCorrectMessage() {
        CharacterReader reader = new CharacterReader("?");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.TagOpen);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Unexpected character '?' in input state [TagOpen]"));
    }

    // =========================================================================
    // Partition E: Comprehensive Tokenizer Integration
    // =========================================================================

    @Test(timeout = 4000)
    public void testFullTokeniserLoop() {
        String html = "<div id=\"test\">Hello &amp; world!</div><!-- comment --><!DOCTYPE html>";
        CharacterReader reader = new CharacterReader(html);
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));

        Token t1 = tokeniser.read();
        assertTrue(t1.isStartTag());
        assertEquals("div", ((Token.StartTag) t1).name());
        assertEquals("test", ((Token.StartTag) t1).attributes.get("id"));

        Token t2 = tokeniser.read();
        assertTrue(t2.isCharacter());
        assertEquals("Hello & world!", ((Token.Character) t2).getData());

        Token t3 = tokeniser.read();
        assertTrue(t3.isEndTag());
        assertEquals("div", ((Token.EndTag) t3).name());

        Token t4 = tokeniser.read();
        assertTrue(t4.isComment());
        assertEquals(" comment ", ((Token.Comment) t4).getData());

        Token t5 = tokeniser.read();
        assertTrue(t5.isDoctype());
        assertEquals("html", ((Token.Doctype) t5).getName());

        Token t6 = tokeniser.read();
        assertTrue(t6.isEOF());
    }
}