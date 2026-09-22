/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Tokeniser
 *
 * Branch & State Analysis:
 * 1. read():
 *    - selfClosingFlagAcknowledged == false: logs error("Self closing flag not acknowledged"), resets flag to true.
 *    - while (!isEmitPending): loops reading state.read(this, reader).
 *    - charBuffer.length() > 0: returns Token.Character, leaves emitPending for subsequent read.
 *    - charBuffer.length() == 0: resets isEmitPending, returns emitPending.
 *
 * 2. emit(Token token):
 *    - Validate.isFalse(isEmitPending): throws IllegalArgumentException if previous token not consumed.
 *    - token.type == StartTag: updates lastStartTag; if startTag.selfClosing, sets selfClosingFlagAcknowledged = false.
 *    - token.type == EndTag: checks endTag.attributes.size() > 0 -> logs error("Attributes incorrectly present on end tag").
 *    - Other token types (Comment, Doctype, Character, EOF).
 *
 * 3. emit(String str) & emit(char c):
 *    - Appends to charBuffer.
 *
 * 4. consumeCharacterReference(Character, boolean):
 *    - reader.isEmpty() -> null.
 *    - additionalAllowedCharacter matches reader.current() -> null.
 *    - matchesAny('\t', '\n', '\f', '<', '&') -> null.
 *    - Numeric references: '#' prefix.
 *      * Hex mode: 'x' / 'X' followed by consumeHexSequence().
 *      * Decimal mode: consumeDigitSequence().
 *      * Empty numRef -> characterReferenceError(), rewindToMark(), return null.
 *      * Missing semicolon ';' -> characterReferenceError().
 *      * Invalid code points: charval == -1, surrogate range [0xD800, 0xDFFF], charval > 0x10FFFF -> replacementChar '\uFFFD'.
 *      * Valid code points -> (char) charval.
 *    - Named references:
 *      * Entities.isNamedEntity match and unconsume loop backwards.
 *      * Not found: if looksLegit (ends with ';') -> characterReferenceError(), rewind, return null.
 *      * inAttribute && (matchesLetter, matchesDigit, '=') -> rewind, return null.
 *      * Missing ';' -> characterReferenceError().
 *      * Entities.getCharacterByName(nameRef).
 *
 * 5. State transitions & Buffer management:
 *    - transition(state), advanceTransition(state), acknowledgeSelfClosingFlag().
 *    - createTagPending(start), emitTagPending().
 *    - createCommentPending(), emitCommentPending().
 *    - createDoctypePending(), emitDoctypePending().
 *    - createTempBuffer().
 *    - isAppropriateEndTagToken(): tagPending.tagName.equals(lastStartTag.tagName).
 *    - error reporting with trackErrors = true / false.
 *
 * 6. Defect-Targeted Zone (Defects4J ground truth: unterminated textarea / unclosed title):
 *    - Unterminated RCDATA / RAWTEXT states when EOF or unexpected tags are encountered.
 *    - Self-closing acknowledgment and appropriate end tag validation.
 */

package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadSimpleTagSequence() {
        CharacterReader reader = new CharacterReader("<p>Hello</p>");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token startToken = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, startToken.type);
        Token.StartTag startTag = (Token.StartTag) startToken;
        assertEquals("p", startTag.name());

        Token textToken = tokeniser.read();
        assertEquals(Token.TokenType.Character, textToken.type);
        assertEquals("Hello", ((Token.Character) textToken).getData());

        Token endToken = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, endToken.type);
        Token.EndTag endTag = (Token.EndTag) endToken;
        assertEquals("p", endTag.name());

        Token eofToken = tokeniser.read();
        assertEquals(Token.TokenType.EOF, eofToken.type);
    }

    @Test(timeout = 4000)
    public void testStateTransitionAndAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser tokeniser = new Tokeniser(reader);

        assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
        assertEquals('a', reader.current());

        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        assertEquals('b', reader.current());
    }

    @Test(timeout = 4000)
    public void testDoctypeAndCommentLifecycle() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.createDoctypePending();
        assertNotNull(tokeniser.doctypePending);
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token docToken = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, docToken.type);
        assertEquals("html", ((Token.Doctype) docToken).getName());

        tokeniser.createCommentPending();
        assertNotNull(tokeniser.commentPending);
        tokeniser.commentPending.data.append("sample comment");
        tokeniser.emitCommentPending();

        Token comToken = tokeniser.read();
        assertEquals(Token.TokenType.Comment, comToken.type);
        assertEquals("sample comment", ((Token.Comment) comToken).getData());
    }

    @Test(timeout = 4000)
    public void testCreateTempBufferAndHtmlNamespace() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        assertNull(tokeniser.dataBuffer);
        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
        assertEquals(0, tokeniser.dataBuffer.length());

        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    @Test(timeout = 4000)
    public void testCharBufferDrainingBeforeEmitPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.emit("BufferedText");
        Token.StartTag tag = new Token.StartTag();
        tag.name("span");
        tokeniser.emit(tag);

        Token firstToken = tokeniser.read();
        assertEquals(Token.TokenType.Character, firstToken.type);
        assertEquals("BufferedText", ((Token.Character) firstToken).getData());

        Token secondToken = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, secondToken.type);
        assertEquals("span", ((Token.StartTag) secondToken).name());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceEmptyAndEarlyExits() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character resEmpty = tokeniser.consumeCharacterReference(null, false);
        assertNull(resEmpty);

        reader = new CharacterReader("x");
        tokeniser = new Tokeniser(reader);
        Character resAllowed = tokeniser.consumeCharacterReference('x', false);
        assertNull(resAllowed);

        char[] excluded = new char[]{'\t', '\n', '\f', '<', '&'};
        for (char c : excluded) {
            reader = new CharacterReader(String.valueOf(c));
            tokeniser = new Tokeniser(reader);
            assertNull(tokeniser.consumeCharacterReference(null, false));
        }
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceDecimalNumeric() {
        CharacterReader reader = new CharacterReader("#65;rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character res = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(res);
        assertEquals(Character.valueOf('A'), res);
        assertEquals("rest", reader.consumeToEnd());
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceHexNumeric() {
        CharacterReader reader = new CharacterReader("#x41;rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character res = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(res);
        assertEquals(Character.valueOf('A'), res);
        assertEquals("rest", reader.consumeToEnd());

        reader = new CharacterReader("#X42;");
        tokeniser = new Tokeniser(reader);
        Character resUpper = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('B'), resUpper);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceMissingSemicolonNumeric() {
        CharacterReader reader = new CharacterReader("#65");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.setTrackErrors(true);

        Character res = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(res);
        assertEquals(Character.valueOf('A'), res);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceInvalidNumericPoints() {
        CharacterReader reader = new CharacterReader("#xD800;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character surrogate = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(surrogate);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), surrogate);

        reader = new CharacterReader("#x110000;");
        tokeniser = new Tokeniser(reader);
        Character outOfRange = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(outOfRange);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), outOfRange);

        reader = new CharacterReader("#;");
        tokeniser = new Tokeniser(reader);
        Character emptyNum = tokeniser.consumeCharacterReference(null, false);
        assertNull(emptyNum);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterReferenceNamedEntities() {
        CharacterReader reader = new CharacterReader("lt;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character res = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(res);
        assertEquals(Character.valueOf('<'), res);

        reader = new CharacterReader("notanentity;");
        tokeniser = new Tokeniser(reader);
        Character invalidNamed = tokeniser.consumeCharacterReference(null, false);
        assertNull(invalidNamed);

        reader = new CharacterReader("amp");
        tokeniser = new Tokeniser(reader);
        Character missingSemi = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(missingSemi);
        assertEquals(Character.valueOf('&'), missingSemi);
    }

    @Test(timeout = 4000)
    public void testConsumeCharacterRefInAttributeDisallowedChars() {
        CharacterReader reader = new CharacterReader("gt=value");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character resEq = tokeniser.consumeCharacterReference(null, true);
        assertNull(resEq);

        reader = new CharacterReader("gta");
        tokeniser = new Tokeniser(reader);
        Character resLetter = tokeniser.consumeCharacterReference(null, true);
        assertNull(resLetter);

        reader = new CharacterReader("gt1");
        tokeniser = new Tokeniser(reader);
        Character resDigit = tokeniser.consumeCharacterReference(null, true);
        assertNull(resDigit);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (RCDATA / RAWTEXT / Unterminated)
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnterminatedTextareaHandling() {
        CharacterReader reader = new CharacterReader("<textarea>one<p>two");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token startTag = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, startTag.type);
        assertEquals("textarea", ((Token.StartTag) startTag).name());

        StringBuilder textContent = new StringBuilder();
        Token t;
        while ((t = tokeniser.read()).type != Token.TokenType.EOF) {
            if (t.type == Token.TokenType.Character) {
                textContent.append(((Token.Character) t).getData());
            } else if (t.type == Token.TokenType.StartTag) {
                textContent.append("<").append(((Token.StartTag) t).name()).append(">");
            }
        }
        assertTrue("Textarea should consume text content correctly",
                textContent.toString().contains("one") && textContent.toString().contains("two"));
    }

    @Test(timeout = 4000)
    public void testHandlesUnclosedTitle() {
        CharacterReader reader = new CharacterReader("<title>One<b>Two <p>Test</p>");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token titleTag = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, titleTag.type);
        assertEquals("title", ((Token.StartTag) titleTag).name());

        StringBuilder titleText = new StringBuilder();
        Token t;
        while ((t = tokeniser.read()).type != Token.TokenType.EOF) {
            if (t.type == Token.TokenType.Character) {
                titleText.append(((Token.Character) t).getData());
            }
        }
        assertTrue(titleText.toString().contains("One"));
        assertTrue(titleText.toString().contains("Two"));
    }

    @Test(timeout = 4000)
    public void testAppropriateEndTagTokenMatching() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.StartTag startTag = (Token.StartTag) tokeniser.createTagPending(true);
        startTag.name("textarea");
        tokeniser.emitTagPending();
        tokeniser.read(); // consume start tag to set lastStartTag

        tokeniser.createTagPending(false);
        tokeniser.tagPending.name("p");
        assertFalse(tokeniser.isAppropriateEndTagToken());

        tokeniser.tagPending.name("textarea");
        assertTrue(tokeniser.isAppropriateEndTagToken());
    }

    @Test(timeout = 4000)
    public void testSelfClosingFlagAcknowledgmentAndErrors() {
        CharacterReader reader = new CharacterReader("<img />");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.setTrackErrors(true);

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type);
        Token.StartTag startTag = (Token.StartTag) token;
        assertTrue(startTag.isSelfClosing());

        Token next = tokeniser.read();
        assertEquals(Token.TokenType.EOF, next.type);

        tokeniser.acknowledgeSelfClosingFlag();
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoubleEmitWithoutReadThrowsException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.Tag tag1 = tokeniser.createTagPending(true);
        tag1.name("b");
        tokeniser.emit(tag1);

        Token.Tag tag2 = tokeniser.createTagPending(false);
        tag2.name("b");
        try {
            tokeniser.emit(tag2);
            fail("Expected IllegalArgumentException when emitting while unread token pending");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("unread token pending"));
        }
    }

    @Test(timeout = 4000)
    public void testEndTagWithAttributesTriggersError() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.setTrackErrors(true);

        Token.EndTag endTag = (Token.EndTag) tokeniser.createTagPending(false);
        endTag.name("div");
        endTag.attributes.put("class", "broken");
        tokeniser.emit(endTag);

        Token readToken = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, readToken.type);
    }

    @Test(timeout = 4000)
    public void testErrorTrackingSwitches() {
        CharacterReader reader = new CharacterReader("<!->");
        Tokeniser tokeniser = new Tokeniser(reader);

        assertTrue(tokeniser.isTrackErrors());
        tokeniser.setTrackErrors(false);
        assertFalse(tokeniser.isTrackErrors());

        tokeniser.error(TokeniserState.Comment);
        tokeniser.eofError(TokeniserState.Comment);

        tokeniser.setTrackErrors(true);
        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
    }

    @Test(timeout = 4000)
    public void testEmitCharacterVariations() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.emit('X');
        tokeniser.emit("YZ");
        tokeniser.emit(new Token.EOF());

        Token charToken = tokeniser.read();
        assertEquals(Token.TokenType.Character, charToken.type);
        assertEquals("XYZ", ((Token.Character) charToken).getData());

        Token eofToken = tokeniser.read();
        assertEquals(Token.TokenType.EOF, eofToken.type);
    }
}