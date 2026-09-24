package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT UNDER TEST (Known Defects4J issue in Tokeniser/Entities):
 *    - Named character references containing numbers (e.g., &frac14;, &sup1;, &sup2;, &frac34;).
 *    - In defective Tokeniser.consumeCharacterReference():
 *      String nameRef = reader.consumeLetterSequence();
 *      Because it consumes ONLY letters, "sup1" is split into "sup" (which is an entity: ⊃) and "1",
 *      and "frac14" is split into "frac" (not an entity) and "14", causing complete parse failure
 *      or incorrect entity resolution.
 *    - Branch Targeted: consumeCharacterReference() -> named entity resolution with alphanumeric entity names.
 *
 * 2. BRANCH & CONDITION COVERAGE MATRIX:
 *    - Token.read():
 *        Branch: !selfClosingFlagAcknowledged -> logs error, resets flag.
 *        Branch: charBuffer.length() > 0 -> returns Character token, keeps emitPending queued.
 *        Branch: charBuffer.length() == 0 -> returns emitPending directly.
 *    - Token.emit(Token):
 *        Branch: isEmitPending == true -> IllegalArgumentException (Validate.isFalse).
 *        Branch: StartTag with selfClosing = true -> sets selfClosingFlagAcknowledged = false.
 *        Branch: EndTag with attributes.size() > 0 -> logs parse error.
 *    - Token.consumeCharacterReference(additionalAllowedChar, inAttribute):
 *        Branch: reader.isEmpty() -> null.
 *        Branch: additionalAllowedCharacter == reader.current() -> null.
 *        Branch: reader.matchesAny('\t', '\n', '\f', ' ', '<', '&') -> null.
 *        Branch: Numeric reference (#) ->
 *            - hex mode ("x" / "X") vs decimal mode.
 *            - numRef.length() == 0 (no digits) -> error, rewind, return null.
 *            - missing semicolon -> error logged, still parses.
 *            - NumberFormatException / out of range (surrogate 0xD800-0xDFFF or > 0x10FFFF) -> replacementChar.
 *            - valid charval -> cast to char.
 *        Branch: Named reference ->
 *            - valid entity with semicolon (e.g., "lt;").
 *            - valid entity without semicolon (e.g., "lt ").
 *            - invalid entity with semicolon (looksLegit=true) -> error logged, returns null.
 *            - invalid entity without semicolon (looksLegit=false) -> returns null.
 *            - inAttribute check with letter, digit, '=', '-', '_' -> rewind, returns null.
 *    - Tag / Comment / Doctype lifecycle:
 *        - createTagPending(true) / createTagPending(false)
 *        - emitTagPending(), isAppropriateEndTagToken(), appropriateEndTagName()
 *        - createCommentPending(), emitCommentPending()
 *        - createDoctypePending(), emitDoctypePending()
 *        - createTempBuffer(), currentNodeInHtmlNS()
 *        - error(), eofError(), advanceTransition(), transition()
 */
public class TokeniserGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where reader.consumeLetterSequence() stops at digits,
     * corrupting entities with digits like &frac14; (¼), &sup1; (¹), &sup2; (²), &frac12; (½), &frac34; (¾).
     * On the defective code, "frac14;" is unconsumed / fails to resolve because only letters ("frac")
     * are matched, returning null or improper characters.
     */
    @Test(timeout = 4000)
    public void testDefectNamedEntitiesWithDigitsFrac14() {
        CharacterReader reader = new CharacterReader("frac14;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        Character result = tokeniser.consumeCharacterReference(null, false);

        assertNotNull("Character reference 'frac14;' should be recognized", result);
        assertEquals("frac14; must resolve to ¼ (U+00BC)", (char) 0xBC, result.charValue());
    }

    /**
     * Targets the defect where "sup1;" is parsed as "sup" (⊃, U+2283) instead of "sup1" (¹, U+00B9).
     */
    @Test(timeout = 4000)
    public void testDefectNamedEntitiesWithDigitsSup1() {
        CharacterReader reader = new CharacterReader("sup1;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        Character result = tokeniser.consumeCharacterReference(null, false);

        assertNotNull("Character reference 'sup1;' should be recognized", result);
        assertEquals("sup1; must resolve to ¹ (U+00B9), not 'sup' (⊃)", (char) 0xB9, result.charValue());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadLifecycleAndStateTransitions() {
        CharacterReader reader = new CharacterReader("<p>Hello</p>");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertEquals(TokeniserState.Data, tokeniser.getState());

        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());

        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        assertEquals('p', reader.current());
    }

    @Test(timeout = 4000)
    public void testReadPendingCharBufferBeforeEmitPending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        // Pre-populate character buffer and an emitPending token
        tokeniser.emit("text");
        Token.StartTag tag = new Token.StartTag();
        tag.name("b");
        tokeniser.emit(tag);

        // First read() must return the buffered characters as a Character token
        Token first = tokeniser.read();
        assertTrue(first.isCharacter());
        assertEquals("text", ((Token.Character) first).getData());

        // Second read() returns the pending tag
        Token second = tokeniser.read();
        assertTrue(second.isStartTag());
        assertEquals("b", ((Token.StartTag) second).name());
    }

    @Test(timeout = 4000)
    public void testCreateAndEmitTagPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token.Tag startTag = tokeniser.createTagPending(true);
        assertTrue(startTag.isStartTag());
        startTag.name("div");
        tokeniser.emitTagPending();

        Token readToken = tokeniser.read();
        assertTrue(readToken.isStartTag());
        assertEquals("div", ((Token.StartTag) readToken).name());

        Token.Tag endTag = tokeniser.createTagPending(false);
        assertTrue(endTag.isEndTag());
        endTag.name("div");

        assertTrue("End tag 'div' should match last start tag 'div'", tokeniser.isAppropriateEndTagToken());
        assertEquals("div", tokeniser.appropriateEndTagName());
    }

    @Test(timeout = 4000)
    public void testCreateAndEmitCommentPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("a comment");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals("a comment", ((Token.Comment) token).getData());
    }

    @Test(timeout = 4000)
    public void testCreateAndEmitDoctypePending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    @Test(timeout = 4000)
    public void testEmitCharAndDataBuffer() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
        tokeniser.dataBuffer.append("temp_data");
        assertEquals("temp_data", tokeniser.dataBuffer.toString());

        tokeniser.emit('X');
        tokeniser.emit(new Token.EOF());

        Token charToken = tokeniser.read();
        assertTrue(charToken.isCharacter());
        assertEquals("X", ((Token.Character) charToken).getData());
    }

    @Test(timeout = 4000)
    public void testCurrentNodeInHtmlNS() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Numeric/Named References
    // =========================================================================

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceEmptyAndImmediateBreakChars() {
        // Empty reader
        Tokeniser tEmpty = new Tokeniser(new CharacterReader(""), ParseErrorList.noTracking());
        assertNull(tEmpty.consumeCharacterReference(null, false));

        // Matches additionalAllowedCharacter
        Tokeniser tAllowed = new Tokeniser(new CharacterReader("\"value"), ParseErrorList.noTracking());
        assertNull(tAllowed.consumeCharacterReference('\"', false));

        // Matches whitespace, '<', or '&'
        char[] breakChars = new char[]{'\t', '\n', '\f', ' ', '<', '&'};
        for (char c : breakChars) {
            Tokeniser tBreak = new Tokeniser(new CharacterReader(String.valueOf(c)), ParseErrorList.noTracking());
            assertNull("Character '" + c + "' should immediately stop character reference consumption",
                    tBreak.consumeCharacterReference(null, false));
        }
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceDecimalValid() {
        CharacterReader reader = new CharacterReader("#65;"); // 'A'
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        Character c = tokeniser.consumeCharacterReference(null, false);

        assertNotNull(c);
        assertEquals('A', c.charValue());
        assertTrue(tokeniser.getState() == TokeniserState.Data);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceHexValid() {
        // Lowercase x
        CharacterReader r1 = new CharacterReader("#x42;"); // 'B'
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(10));
        assertEquals(Character.valueOf('B'), t1.consumeCharacterReference(null, false));

        // Uppercase X
        CharacterReader r2 = new CharacterReader("#X43;"); // 'C'
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(10));
        assertEquals(Character.valueOf('C'), t2.consumeCharacterReference(null, false));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericNoDigits() {
        CharacterReader reader = new CharacterReader("#;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
        // Confirm reader was rewound to before '#'
        assertEquals('#', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericMissingSemicolon() {
        CharacterReader reader = new CharacterReader("#65next");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals('A', c.charValue());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericSurrogatesAndOutOfRange() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        // Surrogate boundary: 0xD800
        Tokeniser t1 = new Tokeniser(new CharacterReader("#xDBFF;"), errors);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), t1.consumeCharacterReference(null, false));

        // Above max unicode code point > 0x10FFFF
        Tokeniser t2 = new Tokeniser(new CharacterReader("#x110000;"), errors);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), t2.consumeCharacterReference(null, false));

        // Huge number causing NumberFormatException (charval == -1)
        Tokeniser t3 = new Tokeniser(new CharacterReader("#99999999999999999999;"), errors);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), t3.consumeCharacterReference(null, false));

        assertTrue(errors.size() >= 3);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedValidAndMissingSemi() {
        // Valid entity with semicolon
        CharacterReader r1 = new CharacterReader("amp;");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.noTracking());
        assertEquals(Character.valueOf('&'), t1.consumeCharacterReference(null, false));

        // Valid entity without semicolon
        CharacterReader r2 = new CharacterReader("lt ");
        ParseErrorList errors2 = ParseErrorList.tracking(10);
        Tokeniser t2 = new Tokeniser(r2, errors2);
        assertEquals(Character.valueOf('<'), t2.consumeCharacterReference(null, false));
        assertEquals(1, errors2.size());
        assertTrue(errors2.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedInvalidLooksLegitOrNot() {
        // Invalid entity with semicolon (looks legit -> generates error and rewinds)
        CharacterReader r1 = new CharacterReader("fakeentity;");
        ParseErrorList errors1 = ParseErrorList.tracking(10);
        Tokeniser t1 = new Tokeniser(r1, errors1);
        assertNull(t1.consumeCharacterReference(null, false));
        assertEquals(1, errors1.size());
        assertTrue(errors1.get(0).getErrorMessage().contains("invalid named referenece"));
        assertEquals('f', r1.current()); // rewound

        // Invalid entity without semicolon (doesn't look legit -> no error, rewinds)
        CharacterReader r2 = new CharacterReader("fakeentity");
        ParseErrorList errors2 = ParseErrorList.tracking(10);
        Tokeniser t2 = new Tokeniser(r2, errors2);
        assertNull(t2.consumeCharacterReference(null, false));
        assertEquals(0, errors2.size());
        assertEquals('f', r2.current()); // rewound
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceInAttributeDisallowedFollowChars() {
        // When in attribute, if followed by [a-zA-Z0-9=_-], should not match
        char[] invalidAttributeFollows = new char[]{'a', '1', '=', '-', '_'};
        for (char follow : invalidAttributeFollows) {
            CharacterReader r = new CharacterReader("lt" + follow);
            Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
            assertNull("Entity followed by '" + follow + "' in attribute must rewind and return null",
                    t.consumeCharacterReference(null, true));
            assertEquals('l', r.current()); // must be fully rewound
        }
    }

    // =========================================================================
    // Partition D: Exception, Error Reporting & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmitWhenTokenAlreadyPendingThrowsException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit(new Token.StartTag().name("div"));
        // Second emit without reading first must violate Validate.isFalse(isEmitPending)
        tokeniser.emit(new Token.EndTag().name("div"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingFlagAcknowledgmentError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag selfClosing = new Token.StartTag();
        selfClosing.name("img");
        selfClosing.selfClosing = true;

        tokeniser.emit(selfClosing);
        assertEquals(0, errors.size());

        // First read returns the tag and sets selfClosingFlagAcknowledged = false
        Token t = tokeniser.read();
        assertTrue(t.isStartTag());

        // Second read without calling acknowledgeSelfClosingFlag() must log parse error
        tokeniser.emit(new Token.EOF());
        tokeniser.read();

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Self closing flag not acknowledged"));
    }

    @Test(timeout = 4000)
    public void testAcknowledgeSelfClosingFlagPreventsError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag selfClosing = new Token.StartTag();
        selfClosing.name("img");
        selfClosing.selfClosing = true;

        tokeniser.emit(selfClosing);
        tokeniser.read();

        // Acknowledge explicitly
        tokeniser.acknowledgeSelfClosingFlag();

        tokeniser.emit(new Token.EOF());
        tokeniser.read();
        assertEquals(0, errors.size());
    }

    @Test(timeout = 4000)
    public void testEndTagWithAttributesGeneratesError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("span");
        endTag.attributes.put("class", "errorClass");

        tokeniser.emit(endTag);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Attributes incorrectly present on end tag"));
    }

    @Test(timeout = 4000)
    public void testErrorAndEofErrorLogging() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Unexpected character 'a' in input state [Data]"));

        tokeniser.eofError(TokeniserState.TagName);
        assertEquals(2, errors.size());
        assertTrue(errors.get(1).getErrorMessage().contains("Unexpectedly reached end of file (EOF) in input state [TagName]"));
    }

    @Test(timeout = 4000)
    public void testParseErrorListCapacityCheck() {
        CharacterReader reader = new CharacterReader("x");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        assertEquals(1, errors.size());

        // Exceeding capacity should not throw and not add beyond max size
        tokeniser.error(TokeniserState.TagOpen);
        assertEquals(1, errors.size());
    }
}