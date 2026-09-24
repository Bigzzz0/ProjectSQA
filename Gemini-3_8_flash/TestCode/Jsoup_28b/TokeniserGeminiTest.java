package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Character Reference Parsing (consumeCharacterReference):
 *    - Empty reader check -> returns null.
 *    - additionalAllowedCharacter match -> returns null.
 *    - reader.matchesAny whitespace/delimiters ('\t', '\n', '\r', '\f', ' ', '<', '&') -> returns null.
 *    - Numeric references ('#'):
 *        * Hexadecimal prefix ('x' / 'X') vs Decimal.
 *        * Missing digits (numRef.length() == 0) -> logs error, rewinds, returns null.
 *        * Missing terminating semicolon ';' -> logs missing semicolon error.
 *        * NumberFormatException handling on integer overflow (> 32-bit).
 *        * Out of range unicode points: -1, surrogates [0xD800, 0xDFFF], > 0x10FFFF -> returns replacementChar.
 *        * Valid unicode points -> returns (char) charval.
 *    - Named references:
 *        * Valid entity with terminating semicolon ';' -> returns mapped Character.
 *        * Valid base entity without terminating semicolon (legacy HTML entities) -> returns Character, logs error.
 *        * DEFECT-4J Ground Truth Target: Non-base/extended entities lacking ';' (e.g., &angst, &icy, &hopf).
 *          In HTML5, extended named entities require a terminating semicolon. Without ';', they must NOT be
 *          unescaped. The defect erroneously matched extended entities without checking for base status or ';'.
 *        * Attribute context guard (inAttribute == true): If followed by letter, digit, '=', '-', '_',
 *          rewinds and returns null to avoid invalid attribute unescapes.
 *        * Semicolon present but invalid entity name -> logs "invalid named referenece", rewinds, returns null.
 *        * Unknown name without semicolon -> rewinds, returns null without logging named entity error.
 *
 * 2. Tokeniser Lifecycle & State Machine:
 *    - read():
 *        * Flag selfClosingFlagAcknowledged == false -> generates error "Self closing flag not acknowledged".
 *        * isEmitPending loop -> state.read() transitions until token emitted.
 *        * charBuffer.length() > 0 -> returns buffered Token.Character, defers emitPending for next read().
 *        * charBuffer empty -> resets isEmitPending, returns emitPending.
 *    - emit(Token):
 *        * Precondition validation: isEmitPending must be false; throws IllegalArgumentException if violated.
 *        * StartTag handling: updates lastStartTag; if selfClosing == true, sets selfClosingFlagAcknowledged = false.
 *        * EndTag handling: if attributes != null, logs error "Attributes incorrectly present on end tag".
 *    - Tag/Comment/Doctype construction:
 *        * createTagPending (Start vs End), emitTagPending (finalises tag).
 *        * createCommentPending, emitCommentPending.
 *        * createDoctypePending, emitDoctypePending.
 *        * createTempBuffer (initializes dataBuffer).
 *    - End tag appropriateness:
 *        * isAppropriateEndTagToken() with lastStartTag null (false) vs matched (true) vs mismatched (false).
 *        * appropriateEndTagName().
 *    - State transitions:
 *        * transition(state) vs advanceTransition(state).
 *    - Error tracking:
 *        * ParseErrorList.tracking vs ParseErrorList.noTracking.
 *        * error(state), eofError(state).
 *    - Namespace check:
 *        * currentNodeInHtmlNS() returns true.
 */
public class TokeniserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStateTransitionsAndAdvance() {
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
    public void testTagPendingLifecycleStartAndEnd() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token.Tag startTag = tokeniser.createTagPending(true);
        assertTrue(startTag instanceof Token.StartTag);
        startTag.tagName = "div";
        tokeniser.emitTagPending();

        Token emittedStart = tokeniser.read();
        assertTrue(emittedStart instanceof Token.StartTag);
        assertEquals("div", ((Token.StartTag) emittedStart).name());

        Token.Tag endTag = tokeniser.createTagPending(false);
        assertTrue(endTag instanceof Token.EndTag);
        endTag.tagName = "div";
        tokeniser.emitTagPending();

        Token emittedEnd = tokeniser.read();
        assertTrue(emittedEnd instanceof Token.EndTag);
        assertEquals("div", ((Token.EndTag) emittedEnd).name());
    }

    @Test(timeout = 4000)
    public void testAppropriateEndTagTokenMatching() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        assertFalse(tokeniser.isAppropriateEndTagToken());

        tokeniser.createTagPending(true);
        tokeniser.tagPending.tagName = "title";
        tokeniser.emitTagPending();

        assertEquals("title", tokeniser.appropriateEndTagName());

        tokeniser.createTagPending(false);
        tokeniser.tagPending.tagName = "span";
        assertFalse(tokeniser.isAppropriateEndTagToken());

        tokeniser.tagPending.tagName = "title";
        assertTrue(tokeniser.isAppropriateEndTagToken());
    }

    @Test(timeout = 4000)
    public void testCommentPendingLifecycle() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        assertNotNull(tokeniser.commentPending);
        tokeniser.commentPending.data.append("sample comment content");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        assertTrue(token instanceof Token.Comment);
        assertEquals("sample comment content", ((Token.Comment) token).getData());
    }

    @Test(timeout = 4000)
    public void testDoctypePendingLifecycle() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createDoctypePending();
        assertNotNull(tokeniser.doctypePending);
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token token = tokeniser.read();
        assertTrue(token instanceof Token.Doctype);
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    @Test(timeout = 4000)
    public void testCreateTempBuffer() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        assertNull(tokeniser.dataBuffer);

        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
        assertEquals(0, tokeniser.dataBuffer.length());
    }

    @Test(timeout = 4000)
    public void testCurrentNodeInHtmlNS() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    @Test(timeout = 4000)
    public void testReadFullHtmlStreamToTokens() {
        CharacterReader reader = new CharacterReader("<p class=\"intro\">Hello</p>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = tokeniser.read();
            tokens.add(t);
        } while (t.type != Token.TokenType.EOF);

        assertEquals(4, tokens.size());
        assertEquals(Token.TokenType.StartTag, tokens.get(0).type);
        assertEquals("p", ((Token.StartTag) tokens.get(0)).name());
        assertEquals("intro", ((Token.StartTag) tokens.get(0)).attributes.get("class"));

        assertEquals(Token.TokenType.Character, tokens.get(1).type);
        assertEquals("Hello", ((Token.Character) tokens.get(1)).getData());

        assertEquals(Token.TokenType.EndTag, tokens.get(2).type);
        assertEquals("p", ((Token.EndTag) tokens.get(2)).name());

        assertEquals(Token.TokenType.EOF, tokens.get(3).type);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Character Reference Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        assertNull(tokeniser.consumeCharacterReference(null, false));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceAdditionalAllowedCharacter() {
        CharacterReader reader = new CharacterReader("\"test");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        assertNull(tokeniser.consumeCharacterReference('"', false));
        assertEquals('"', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceWhitespaceAndDelimiterBoundaries() {
        char[] delimiters = new char[]{'\t', '\n', '\r', '\f', ' ', '<', '&'};
        for (char delim : delimiters) {
            CharacterReader reader = new CharacterReader(delim + "rest");
            Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
            assertNull("Should return null for delimiter: " + (int) delim,
                    tokeniser.consumeCharacterReference(null, false));
            assertEquals(delim, reader.current());
        }
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericDecimalValid() {
        CharacterReader reader = new CharacterReader("#65;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), c);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericHexValid() {
        CharacterReader readerLower = new CharacterReader("#x41;");
        Tokeniser tokeniserLower = new Tokeniser(readerLower, ParseErrorList.noTracking());
        assertEquals(Character.valueOf('A'), tokeniserLower.consumeCharacterReference(null, false));
        assertTrue(readerLower.isEmpty());

        CharacterReader readerUpper = new CharacterReader("#X42;");
        Tokeniser tokeniserUpper = new Tokeniser(readerUpper, ParseErrorList.noTracking());
        assertEquals(Character.valueOf('B'), tokeniserUpper.consumeCharacterReference(null, false));
        assertTrue(readerUpper.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericMissingSemicolonLogsError() {
        CharacterReader reader = new CharacterReader("#65");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), c);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericNoDigitsLogsError() {
        CharacterReader reader = new CharacterReader("#;");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertNull(tokeniser.consumeCharacterReference(null, false));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
        assertEquals('#', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceHexNoDigitsLogsError() {
        CharacterReader reader = new CharacterReader("#x;");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertNull(tokeniser.consumeCharacterReference(null, false));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
        assertEquals('#', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNumericSurrogateAndOutOfRangeBoundaries() {
        // Surrogate range: 0xD800 (55296) to 0xDFFF (57343)
        CharacterReader readerSurrogateStart = new CharacterReader("#55296;");
        ParseErrorList errors1 = ParseErrorList.tracking(5);
        Tokeniser tokeniser1 = new Tokeniser(readerSurrogateStart, errors1);
        Character c1 = tokeniser1.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c1);
        assertEquals(1, errors1.size());
        assertTrue(errors1.get(0).getErrorMessage().contains("character outside of valid range"));

        CharacterReader readerSurrogateEnd = new CharacterReader("#57343;");
        ParseErrorList errors2 = ParseErrorList.tracking(5);
        Tokeniser tokeniser2 = new Tokeniser(readerSurrogateEnd, errors2);
        Character c2 = tokeniser2.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c2);
        assertEquals(1, errors2.size());

        // Above max Unicode codepoint 0x10FFFF (1114111)
        CharacterReader readerAboveMax = new CharacterReader("#1114112;");
        ParseErrorList errors3 = ParseErrorList.tracking(5);
        Tokeniser tokeniser3 = new Tokeniser(readerAboveMax, errors3);
        Character c3 = tokeniser3.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c3);
        assertEquals(1, errors3.size());
        assertTrue(errors3.get(0).getErrorMessage().contains("character outside of valid range"));

        // Integer overflow triggering NumberFormatException -> charval remains -1
        CharacterReader readerOverflow = new CharacterReader("#99999999999999999999;");
        ParseErrorList errors4 = ParseErrorList.tracking(5);
        Tokeniser tokeniser4 = new Tokeniser(readerOverflow, errors4);
        Character c4 = tokeniser4.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c4);
        assertEquals(1, errors4.size());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedValidWithSemicolon() {
        CharacterReader reader = new CharacterReader("amp;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        assertEquals(Character.valueOf('&'), tokeniser.consumeCharacterReference(null, false));
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedBaseWithoutSemicolon() {
        CharacterReader reader = new CharacterReader("amp");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertEquals(Character.valueOf('&'), tokeniser.consumeCharacterReference(null, false));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedUnrecognizedWithSemicolonLogsError() {
        CharacterReader reader = new CharacterReader("thisisnotanentity;");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertNull(tokeniser.consumeCharacterReference(null, false));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("invalid named referenece 'thisisnotanentity'"));
        assertEquals('t', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedUnrecognizedWithoutSemicolonSilentlyRewinds() {
        CharacterReader reader = new CharacterReader("thisisnotanentity");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertNull(tokeniser.consumeCharacterReference(null, false));
        assertEquals(0, errors.size());
        assertEquals('t', reader.current());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceInAttributeDisallowedFollowCharacters() {
        // If inAttribute is true, entity followed by letter, digit, '=', '-', '_' must not match
        char[] disallowedFollows = new char[]{'a', '1', '=', '-', '_'};
        for (char follow : disallowedFollows) {
            CharacterReader reader = new CharacterReader("lt" + follow);
            Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
            assertNull("Entity followed by '" + follow + "' in attribute must not be consumed",
                    tokeniser.consumeCharacterReference(null, true));
            assertEquals('l', reader.current());
        }

        // When NOT in attribute, 'lt=foo' should consume 'lt' as '<'
        CharacterReader readerNotInAttr = new CharacterReader("lt=foo");
        Tokeniser tokeniserNotInAttr = new Tokeniser(readerNotInAttr, ParseErrorList.noTracking());
        assertEquals(Character.valueOf('<'), tokeniserNotInAttr.consumeCharacterReference(null, false));
        assertEquals('=', readerNotInAttr.current());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETED DEFECT:
     * In HTML5, extended named entities require a terminating semicolon.
     * Legacy/base entities (e.g. amp, lt, gt, quot, reg, copy) may be unescaped without ';',
     * but extended entities like 'angst' (Å) or 'icy' (й) MUST NOT be consumed without ';'.
     *
     * In the defective version, Tokeniser matched any entity in Entities.isNamedEntity(nameRef)
     * regardless of whether it was a base entity or had a terminating ';'.
     * This test strictly asserts that extended entities without ';' return null.
     */
    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceRequiresSemicolonForExtendedEntities() {
        CharacterReader readerAngst = new CharacterReader("angst");
        Tokeniser tokeniser1 = new Tokeniser(readerAngst, ParseErrorList.tracking(5));
        Character c1 = tokeniser1.consumeCharacterReference(null, false);
        assertNull("Extended entity &angst without ';' must NOT be consumed", c1);

        CharacterReader readerIcy = new CharacterReader("icy");
        Tokeniser tokeniser2 = new Tokeniser(readerIcy, ParseErrorList.tracking(5));
        Character c2 = tokeniser2.consumeCharacterReference(null, false);
        assertNull("Extended entity &icy without ';' must NOT be consumed", c2);

        CharacterReader readerHopf = new CharacterReader("hopf");
        Tokeniser tokeniser3 = new Tokeniser(readerHopf, ParseErrorList.tracking(5));
        Character c3 = tokeniser3.consumeCharacterReference(null, false);
        assertNull("Extended entity &hopf without ';' must NOT be consumed", c3);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceExtendedEntitiesWithSemicolonSucceed() {
        CharacterReader readerAngst = new CharacterReader("angst;");
        Tokeniser tokeniser = new Tokeniser(readerAngst, ParseErrorList.noTracking());
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('\u00C5'), c);
        assertTrue(readerAngst.isEmpty());
    }

    // =========================================================================
    // Partition D: Exception, Defensive Guards & Protocol Errors
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmitWhenPendingTokenAlreadyExistsThrowsException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit(new Token.Character("token1"));
        try {
            tokeniser.emit(new Token.Character("token2"));
            fail("Expected IllegalArgumentException when emitting while isEmitPending is true");
        } catch (IllegalArgumentException e) {
            assertEquals("There is an unread token pending!", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCharBufferEmittedBeforeEmitPendingToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit("buffered chars");
        tokeniser.emit('!');
        Token.Comment comment = new Token.Comment();
        comment.data.append("deferred");
        tokeniser.emit(comment);

        Token firstToken = tokeniser.read();
        assertTrue(firstToken instanceof Token.Character);
        assertEquals("buffered chars!", ((Token.Character) firstToken).getData());

        Token secondToken = tokeniser.read();
        assertSame(comment, secondToken);
    }

    @Test(timeout = 4000)
    public void testSelfClosingFlagUnacknowledgedGeneratesError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;
        tokeniser.emit(startTag);

        Token readToken = tokeniser.read();
        assertSame(startTag, readToken);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Self closing flag not acknowledged"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingFlagAcknowledgedGeneratesNoError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;
        tokeniser.emit(startTag);
        tokeniser.acknowledgeSelfClosingFlag();

        Token readToken = tokeniser.read();
        assertSame(startTag, readToken);
        assertEquals(0, errors.size());
    }

    @Test(timeout = 4000)
    public void testEndTagWithAttributesGeneratesParseError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(5);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.EndTag endTag = new Token.EndTag();
        endTag.attributes = new org.jsoup.nodes.Attributes();
        endTag.attributes.put("key", "val");
        tokeniser.emit(endTag);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Attributes incorrectly present on end tag"));
    }

    @Test(timeout = 4000)
    public void testErrorAndEofErrorTrackingBranches() {
        CharacterReader reader = new CharacterReader("z");
        ParseErrorList trackingErrors = ParseErrorList.tracking(5);
        Tokeniser tokeniserTracking = new Tokeniser(reader, trackingErrors);

        tokeniserTracking.error(TokeniserState.Data);
        tokeniserTracking.eofError(TokeniserState.TagOpen);

        assertEquals(2, trackingErrors.size());
        assertTrue(trackingErrors.get(0).getErrorMessage().contains("Unexpected character 'z' in input state [Data]"));
        assertTrue(trackingErrors.get(1).getErrorMessage().contains("Unexpectedly reached end of file (EOF) in input state [TagOpen]"));

        ParseErrorList noTrackingErrors = ParseErrorList.noTracking();
        Tokeniser tokeniserNoTracking = new Tokeniser(reader, noTrackingErrors);

        tokeniserNoTracking.error(TokeniserState.Data);
        tokeniserNoTracking.eofError(TokeniserState.TagOpen);
        assertEquals(0, noTrackingErrors.size());
    }
}