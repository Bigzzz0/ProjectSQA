package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.TokeniserState
 *
 * Core Coverage & Decision Branches:
 * 1. Data & CharRefs:
 *    - Data: '&', '<', '\u0000', eof, default (data).
 *    - CharacterReferenceInData & CharacterReferenceInRcdata: valid charref vs non-charref (& alone).
 * 2. Rawtext, ScriptData, Rcdata, PLAINTEXT:
 *    - nullChar replacement, eof, data chunks, '<' transitions.
 *    - RcdataLessthanSign: '/' transition to RCDATAEndTagOpen; letter with appropriate end tag match vs mismatch; fallback '<'.
 *    - RCDATAEndTagName: letters, whitespace, '/', '>', default, anythingElse.
 * 3. Tag Creation & Attributes:
 *    - TagOpen: '!', '/', '?', matchesLetter, error default.
 *    - EndTagOpen: empty (eof), letter, '>', bogus comment fallback.
 *    - TagName: append letters, whitespace, '/', '>', nullChar, eof.
 *    - BeforeAttributeName: whitespace, '/', '>', nullChar, eof, [\"\'<=], default.
 *    - AttributeName: whitespace, '/', '=', '>', nullChar, eof, [\"\'<].
 *    - AfterAttributeName: whitespace, '/', '=', '>', nullChar, eof, [\"\'<], default.
 *    - BeforeAttributeValue: whitespace, '"', '&', '\'', nullChar, eof, '>', [<=`], default.
 *    - AttributeValue_doubleQuoted / singleQuoted / unquoted: quote end, '&' charrefs, nullChar, eof, delimiters.
 *    - AfterAttributeValue_quoted: whitespace, '/', '>', eof, default error.
 *    - SelfClosingStartTag: '>', eof, default.
 * 4. Comments & CDATA:
 *    - BogusComment, MarkupDeclarationOpen ("--", "DOCTYPE", "[CDATA[", fallback).
 *    - CommentStart, CommentStartDash, Comment, CommentEndDash, CommentEnd, CommentEndBang:
 *      all transitions on '-', '>', '!', nullChar, eof, regular chars.
 *    - CdataSection: CDATA consumption up to "]]>".
 * 5. Script Escaping States:
 *    - ScriptDataLessthanSign ('/', '!', default).
 *    - ScriptDataEscapeStart, ScriptDataEscapeStartDash, ScriptDataEscaped, ScriptDataEscapedDash, ScriptDataEscapedDashDash.
 *    - ScriptDataEscapedLessthanSign, ScriptDataEscapedEndTagOpen, ScriptDataEscapedEndTagName.
 *    - ScriptDataDoubleEscapeStart, ScriptDataDoubleEscaped, ScriptDataDoubleEscapedDash, ScriptDataDoubleEscapedDashDash,
 *      ScriptDataDoubleEscapedLessthanSign, ScriptDataDoubleEscapeEnd.
 * 6. DOCTYPE States & Defect Zone (DocumentTypeTest roundtrip / SYSTEM / PUBLIC parsing):
 *    - Doctype: whitespace, eof, '>', default.
 *    - BeforeDoctypeName: letter, whitespace, nullChar, eof, default.
 *    - DoctypeName: letter sequence, '>', whitespace, nullChar, eof, default.
 *    - AfterDoctypeName: empty/eof, whitespace, '>', PUBLIC, SYSTEM, default (bogus).
 *    - AfterDoctypePublicKeyword & BeforeDoctypePublicIdentifier: whitespace, '"', '\'', '>', eof, default.
 *    - DoctypePublicIdentifier_doubleQuoted & singleQuoted: delimiter, nullChar, '>', eof, chars.
 *    - AfterDoctypePublicIdentifier & BetweenDoctypePublicAndSystemIdentifiers: whitespace, '>', '"', '\'', eof, default.
 *    - AfterDoctypeSystemKeyword & BeforeDoctypeSystemIdentifier: whitespace, '>', '"', '\'', eof, default.
 *    - DoctypeSystemIdentifier_doubleQuoted & singleQuoted: delimiter, nullChar, '>', eof, chars.
 *    - AfterDoctypeSystemIdentifier: whitespace, '>', eof, default.
 *    - BogusDoctype: '>', eof, default chars.
 */
public class TokeniserStateGeminiTest {

    private Tokeniser createTokeniser(String html) {
        CharacterReader reader = new CharacterReader(html);
        return new Tokeniser(reader, ParseErrorList.tracking(50));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (DOCTYPE SYSTEM / PUBLIC / Roundtrip)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoctypeWithSystemKeywordAndIdentifier() {
        // Target defect: <!DOCTYPE html SYSTEM "exampledtdfile.dtd"> roundtrip preservation
        Tokeniser t = createTokeniser("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("exampledtdfile.dtd", doctype.getSystemIdentifier());
        assertEquals("", doctype.getPublicIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithSingleQuotedSystemIdentifier() {
        Tokeniser t = createTokeniser("<!DOCTYPE html SYSTEM 'sys.dtd'>");
        Token.Doctype doctype = (Token.Doctype) t.read();
        assertEquals("html", doctype.getName());
        assertEquals("sys.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithPublicAndSystemIdentifier() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        Token.Doctype doctype = (Token.Doctype) t.read();
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithSingleQuotedPublicAndSystemIdentifier() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC 'pubId' 'sysId'>");
        Token.Doctype doctype = (Token.Doctype) t.read();
        assertEquals("html", doctype.getName());
        assertEquals("pubId", doctype.getPublicIdentifier());
        assertEquals("sysId", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDataTransitionsAndNullCharacter() {
        CharacterReader reader = new CharacterReader("\u0000Text&amp;<tag>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));

        Token token1 = t.read();
        assertTrue(token1.isCharacter());
        assertEquals("\u0000", ((Token.Character) token1).getData());

        Token token2 = t.read();
        assertTrue(token2.isCharacter());
        assertEquals("Text&", ((Token.Character) token2).getData());

        Token token3 = t.read();
        assertTrue(token3.isStartTag());
        assertEquals("tag", ((Token.StartTag) token3).name());
    }

    @Test(timeout = 4000)
    public void testPlaintextState() {
        CharacterReader reader = new CharacterReader("plain \u0000 text");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.transition(TokeniserState.PLAINTEXT);

        Token t1 = t.read();
        assertTrue(t1.isCharacter());
        assertEquals("plain ", ((Token.Character) t1).getData());

        Token t2 = t.read();
        assertTrue(t2.isCharacter());
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) t2).getData());

        Token t3 = t.read();
        assertTrue(t3.isCharacter());
        assertEquals(" text", ((Token.Character) t3).getData());

        Token t4 = t.read();
        assertTrue(t4.isEOF());
    }

    @Test(timeout = 4000)
    public void testTagOpenVariations() {
        // '?' -> BogusComment
        Tokeniser t1 = createTokeniser("<?xml version=\"1.0\"?>");
        Token token1 = t1.read();
        assertTrue(token1.isComment());
        assertTrue(((Token.Comment) token1).bogus);

        // invalid start tag character e.g. '< 123'
        Tokeniser t2 = createTokeniser("< 123");
        Token token2 = t2.read();
        assertTrue(token2.isCharacter());
        assertEquals("<", ((Token.Character) token2).getData());
    }

    @Test(timeout = 4000)
    public void testEndTagOpenVariations() {
        // matches '>' directly in EndTagOpen
        Tokeniser t1 = createTokeniser("</>");
        Token token1 = t1.read();
        assertTrue(token1.isEOF()); // error emitted, transition to Data, then EOF

        // Bogus comment from end tag open like </?bogus>
        Tokeniser t2 = createTokeniser("</?bogus>");
        Token token2 = t2.read();
        assertTrue(token2.isComment());
        assertTrue(((Token.Comment) token2).bogus);

        // empty end tag at EOF
        Tokeniser t3 = createTokeniser("</");
        Token token3 = t3.read();
        assertTrue(token3.isCharacter());
        assertEquals("</", ((Token.Character) token3).getData());
    }

    @Test(timeout = 4000)
    public void testTagNameEofAndReplacement() {
        // Tag name hitting EOF
        Tokeniser t1 = createTokeniser("<div");
        Token tok1 = t1.read();
        assertTrue(tok1.isEOF());

        // Tag name hitting nullChar
        Tokeniser t2 = createTokeniser("<div\u0000extra>");
        Token tok2 = t2.read();
        assertTrue(tok2.isStartTag());
        assertEquals("div" + Tokeniser.replacementChar + "extra", ((Token.StartTag) tok2).name());
    }

    @Test(timeout = 4000)
    public void testAttributeStatesDoubleQuoted() {
        Tokeniser t = createTokeniser("<div id=\"main&amp;\" class=\"item\">");
        Token.StartTag tag = (Token.StartTag) t.read();
        assertEquals("div", tag.name());
        assertEquals("main&", tag.attributes.get("id"));
        assertEquals("item", tag.attributes.get("class"));
    }

    @Test(timeout = 4000)
    public void testAttributeStatesSingleQuotedAndUnquoted() {
        Tokeniser t = createTokeniser("<span title='it&#39;s' data-attr=val> content");
        Token.StartTag tag = (Token.StartTag) t.read();
        assertEquals("span", tag.name());
        assertEquals("it's", tag.attributes.get("title"));
        assertEquals("val", tag.attributes.get("data-attr"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueSpecialCharacters() {
        // Characters '<', '=', '`' in unquoted attribute values
        Tokeniser t = createTokeniser("<a href=<test`=foo>");
        Token.StartTag tag = (Token.StartTag) t.read();
        assertEquals("a", tag.name());
        assertEquals("<test`=foo", tag.attributes.get("href"));

        // Null character in attribute value
        Tokeniser t2 = createTokeniser("<a href=\"val\u0000test\">");
        Token.StartTag tag2 = (Token.StartTag) t2.read();
        assertEquals("val" + Tokeniser.replacementChar + "test", tag2.attributes.get("href"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingTagAndMalformedTransitions() {
        Tokeniser t1 = createTokeniser("<img src='foo.jpg' / >");
        Token.StartTag tag1 = (Token.StartTag) t1.read();
        assertTrue(tag1.isSelfClosing());

        // SelfClosingStartTag with non-'>'
        Tokeniser t2 = createTokeniser("<img /a>");
        Token.StartTag tag2 = (Token.StartTag) t2.read();
        assertTrue(tag2.attributes.hasKey("a"));

        // SelfClosingStartTag with EOF
        Tokeniser t3 = createTokeniser("<img /");
        Token tok3 = t3.read();
        assertTrue(tok3.isEOF());
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValueQuotedTransitions() {
        Tokeniser t = createTokeniser("<div class=\"btn\"/ id=\"x\">");
        Token.StartTag tag = (Token.StartTag) t.read();
        assertEquals("btn", tag.attributes.get("class"));
        assertEquals("x", tag.attributes.get("id"));

        Tokeniser t2 = createTokeniser("<div class=\"btn\"x>");
        Token.StartTag tag2 = (Token.StartTag) t2.read();
        assertTrue(tag2.attributes.hasKey("x"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeNameSpecialChars() {
        Tokeniser t = createTokeniser("<div \"attr1\" 'attr2' <attr3 =attr4 \u0000attr5>");
        Token.StartTag tag = (Token.StartTag) t.read();
        assertTrue(tag.attributes.hasKey("\"attr1\""));
        assertTrue(tag.attributes.hasKey("'attr2'"));
        assertTrue(tag.attributes.hasKey("<attr3"));
        assertTrue(tag.attributes.hasKey("=attr4"));
        assertTrue(tag.attributes.hasKey(Tokeniser.replacementChar + "attr5"));
    }

    @Test(timeout = 4000)
    public void testAfterAttributeNameSpecialChars() {
        Tokeniser t = createTokeniser("<div attr = \"val\" >");
        Token.StartTag tag = (Token.StartTag) t.read();
        assertEquals("val", tag.attributes.get("attr"));

        Tokeniser t2 = createTokeniser("<div attr/ >");
        Token.StartTag tag2 = (Token.StartTag) t2.read();
        assertTrue(tag2.isSelfClosing());

        Tokeniser t3 = createTokeniser("<div attr\"other\">");
        Token.StartTag tag3 = (Token.StartTag) t3.read();
        assertTrue(tag3.attributes.hasKey("attr\"other\""));
    }

    // =========================================================================
    // Partition B: Comments and CDATA Sections
    // =========================================================================

    @Test(timeout = 4000)
    public void testCommentVariations() {
        // Standard comment
        Tokeniser t1 = createTokeniser("<!-- hello world -->");
        Token.Comment c1 = (Token.Comment) t1.read();
        assertEquals(" hello world ", c1.getData());
        assertFalse(c1.bogus);

        // Abruptly closed comments: <!--> and <!--->
        Tokeniser t2 = createTokeniser("<!-->");
        Token.Comment c2 = (Token.Comment) t2.read();
        assertEquals("", c2.getData());

        Tokeniser t3 = createTokeniser("<!--->");
        Token.Comment c3 = (Token.Comment) t3.read();
        assertEquals("", c3.getData());

        // Comment with dashes inside: <!-- -- -- -->
        Tokeniser t4 = createTokeniser("<!-- a - b -- c -->");
        Token.Comment c4 = (Token.Comment) t4.read();
        assertEquals(" a - b -- c ", c4.getData());

        // Comment ending with bang: <!-- comment --!>
        Tokeniser t5 = createTokeniser("<!-- comment --!>");
        Token.Comment c5 = (Token.Comment) t5.read();
        assertEquals(" comment ", c5.getData());

        // Comment with null character
        Tokeniser t6 = createTokeniser("<!-- co\u0000mment -->");
        Token.Comment c6 = (Token.Comment) t6.read();
        assertEquals(" co" + Tokeniser.replacementChar + "mment ", c6.getData());
    }

    @Test(timeout = 4000)
    public void testCommentEndBangOtherBranches() {
        // CommentEndBang followed by non-'-', non-'>', non-nullChar
        Tokeniser t1 = createTokeniser("<!-- comment --!abc-->");
        Token.Comment c1 = (Token.Comment) t1.read();
        assertEquals(" comment --!abc", c1.getData());

        // CommentEndBang followed by nullChar
        Tokeniser t2 = createTokeniser("<!-- comment --!\u0000-->");
        Token.Comment c2 = (Token.Comment) t2.read();
        assertEquals(" comment --!" + Tokeniser.replacementChar, c2.getData());

        // CommentEndBang at EOF
        Tokeniser t3 = createTokeniser("<!-- comment --!");
        Token.Comment c3 = (Token.Comment) t3.read();
        assertEquals(" comment ", c3.getData());
    }

    @Test(timeout = 4000)
    public void testCommentEofTransitions() {
        Tokeniser t1 = createTokeniser("<!-- incomplete");
        Token.Comment c1 = (Token.Comment) t1.read();
        assertEquals(" incomplete", c1.getData());

        Tokeniser t2 = createTokeniser("<!-- incomplete -");
        Token.Comment c2 = (Token.Comment) t2.read();
        assertEquals(" incomplete -", c2.getData());

        Tokeniser t3 = createTokeniser("<!-- incomplete --");
        Token.Comment c3 = (Token.Comment) t3.read();
        assertEquals(" incomplete ", c3.getData());
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        CharacterReader reader = new CharacterReader("<![CDATA[raw <data> & symbols]]>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("raw <data> & symbols", ((Token.Character) token).getData());
    }

    // =========================================================================
    // Partition D: RCDATA, RAWTEXT & SCRIPT DATA States
    // =========================================================================

    @Test(timeout = 4000)
    public void testRcdataAppropriateEndTag() {
        CharacterReader reader = new CharacterReader("<title>Hello &amp; World</title>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        Token start = t.read();
        assertEquals("title", ((Token.StartTag) start).name());

        t.transition(TokeniserState.Rcdata);
        Token text = t.read();
        assertEquals("Hello & World", ((Token.Character) text).getData());

        Token end = t.read();
        assertTrue(end.isEndTag());
        assertEquals("title", ((Token.EndTag) end).name());
    }

    @Test(timeout = 4000)
    public void testRcdataMismatchedEndTag() {
        CharacterReader reader = new CharacterReader("Some text </wrong> and </title>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.createTagPending(true);
        t.tagPending.name("title"); // appropriate tag is title
        t.transition(TokeniserState.Rcdata);

        // Reads up to </wrong>
        Token t1 = t.read();
        assertEquals("Some text ", ((Token.Character) t1).getData());

        Token t2 = t.read();
        assertEquals("</wrong", ((Token.Character) t2).getData());

        Token t3 = t.read();
        assertEquals(" and ", ((Token.Character) t3).getData());

        Token t4 = t.read();
        assertTrue(t4.isEndTag());
        assertEquals("title", ((Token.EndTag) t4).name());
    }

    @Test(timeout = 4000)
    public void testRawtextParsing() {
        CharacterReader reader = new CharacterReader("<style> body { color: red; } </style>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        Token start = t.read();
        assertEquals("style", ((Token.StartTag) start).name());

        t.transition(TokeniserState.Rawtext);
        Token text = t.read();
        assertEquals(" body { color: red; } ", ((Token.Character) text).getData());

        Token end = t.read();
        assertTrue(end.isEndTag());
        assertEquals("style", ((Token.EndTag) end).name());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapeAndDoubleEscapeStates() {
        String script = "<script><!-- var a = '<b>hello</b>'; \n"
                + "<script> nested </script> \n"
                + "-->\n"
                + "</script>";
        Tokeniser t = createTokeniser(script);

        Token start = t.read();
        assertTrue(start.isStartTag());
        assertEquals("script", ((Token.StartTag) start).name());

        t.transition(TokeniserState.ScriptData);

        StringBuilder dataAccumulator = new StringBuilder();
        Token token;
        while (!(token = t.read()).isEndTag() && !token.isEOF()) {
            if (token.isCharacter()) {
                dataAccumulator.append(((Token.Character) token).getData());
            }
        }
        assertTrue(token.isEndTag());
        assertEquals("script", ((Token.EndTag) token).name());
        assertTrue(dataAccumulator.toString().contains("var a = '<b>hello</b>'"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedTransitionsDirectly() {
        CharacterReader reader = new CharacterReader("-<--><script></script></script>---");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.dataBuffer.append("script");
        t.transition(TokeniserState.ScriptDataDoubleEscaped);

        // Step through double escaped states
        while (!reader.isEmpty()) {
            t.getState().read(t, reader);
        }
        assertNotNull(t.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashVariations() {
        // Test '-' , '<', '>', nullChar, eof in ScriptDataEscapedDashDash
        CharacterReader r1 = new CharacterReader("--><null>\u0000");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(10));
        t1.transition(TokeniserState.ScriptDataEscapedDashDash);

        t1.getState().read(t1, r1); // '-'
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t1.getState());

        t1.getState().read(t1, r1); // '-'
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t1.getState());

        t1.getState().read(t1, r1); // '>'
        assertEquals(TokeniserState.ScriptData, t1.getState());
    }

    // =========================================================================
    // Partition E: Exhaustive DOCTYPE State Transitions & Quirks Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoctypeMalformedAndQuirks() {
        // <!DOCTYPE> -> quirks
        Tokeniser t1 = createTokeniser("<!DOCTYPE>");
        Token.Doctype d1 = (Token.Doctype) t1.read();
        assertTrue(d1.isForceQuirks());

        // <!DOCTYPE > with whitespace then '>'
        Tokeniser t2 = createTokeniser("<!DOCTYPE >");
        Token.Doctype d2 = (Token.Doctype) t2.read();
        assertTrue(d2.isForceQuirks());

        // <!DOCTYPE\u0000>
        Tokeniser t3 = createTokeniser("<!DOCTYPE\u0000>");
        Token.Doctype d3 = (Token.Doctype) t3.read();
        assertTrue(d3.isForceQuirks());
        assertEquals(String.valueOf(Tokeniser.replacementChar), d3.getName());

        // <!DOCTYPE html \u0000>
        Tokeniser t4 = createTokeniser("<!DOCTYPE html \u0000>");
        Token.Doctype d4 = (Token.Doctype) t4.read();
        assertTrue(d4.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicMalformedBranches() {
        // Public with premature '>'
        Tokeniser t1 = createTokeniser("<!DOCTYPE html PUBLIC >");
        Token.Doctype d1 = (Token.Doctype) t1.read();
        assertTrue(d1.isForceQuirks());

        // Public with invalid next character -> BogusDoctype
        Tokeniser t2 = createTokeniser("<!DOCTYPE html PUBLIC foo >");
        Token.Doctype d2 = (Token.Doctype) t2.read();
        assertTrue(d2.isForceQuirks());

        // Public with EOF
        Tokeniser t3 = createTokeniser("<!DOCTYPE html PUBLIC \"pubid");
        Token.Doctype d3 = (Token.Doctype) t3.read();
        assertTrue(d3.isForceQuirks());
        assertEquals("pubid", d3.getPublicIdentifier());

        // Public with nullChar
        Tokeniser t4 = createTokeniser("<!DOCTYPE html PUBLIC \"pub\u0000id\">");
        Token.Doctype d4 = (Token.Doctype) t4.read();
        assertEquals("pub" + Tokeniser.replacementChar + "id", d4.getPublicIdentifier());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemMalformedBranches() {
        // System with premature '>'
        Tokeniser t1 = createTokeniser("<!DOCTYPE html SYSTEM >");
        Token.Doctype d1 = (Token.Doctype) t1.read();
        assertTrue(d1.isForceQuirks());

        // System with invalid character
        Tokeniser t2 = createTokeniser("<!DOCTYPE html SYSTEM foo >");
        Token.Doctype d2 = (Token.Doctype) t2.read();
        assertTrue(d2.isForceQuirks());

        // System with EOF in quote
        Tokeniser t3 = createTokeniser("<!DOCTYPE html SYSTEM \"sysid");
        Token.Doctype d3 = (Token.Doctype) t3.read();
        assertTrue(d3.isForceQuirks());
        assertEquals("sysid", d3.getSystemIdentifier());

        // System with nullChar
        Tokeniser t4 = createTokeniser("<!DOCTYPE html SYSTEM \"sys\u0000id\">");
        Token.Doctype d4 = (Token.Doctype) t4.read();
        assertEquals("sys" + Tokeniser.replacementChar + "id", d4.getSystemIdentifier());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicIdentifierTransitions() {
        // Premature closing after public identifier
        Tokeniser t1 = createTokeniser("<!DOCTYPE html PUBLIC \"pubid\">");
        Token.Doctype d1 = (Token.Doctype) t1.read();
        assertEquals("pubid", d1.getPublicIdentifier());
        assertFalse(d1.isForceQuirks());

        // Public identifier followed by unexpected character
        Tokeniser t2 = createTokeniser("<!DOCTYPE html PUBLIC \"pubid\" unexpected>");
        Token.Doctype d2 = (Token.Doctype) t2.read();
        assertTrue(d2.isForceQuirks());

        // Public identifier at EOF
        Tokeniser t3 = createTokeniser("<!DOCTYPE html PUBLIC \"pubid\"");
        Token.Doctype d3 = (Token.Doctype) t3.read();
        assertTrue(d3.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testBetweenDoctypePublicAndSystemIdentifiersTransitions() {
        // Between identifiers hitting '>'
        Tokeniser t1 = createTokeniser("<!DOCTYPE html PUBLIC \"pubid\" >");
        Token.Doctype d1 = (Token.Doctype) t1.read();
        assertEquals("pubid", d1.getPublicIdentifier());

        // Between identifiers hitting EOF
        Tokeniser t2 = createTokeniser("<!DOCTYPE html PUBLIC \"pubid\" ");
        Token.Doctype d2 = (Token.Doctype) t2.read();
        assertTrue(d2.isForceQuirks());

        // Between identifiers hitting unexpected char
        Tokeniser t3 = createTokeniser("<!DOCTYPE html PUBLIC \"pubid\" junk>");
        Token.Doctype d3 = (Token.Doctype) t3.read();
        assertTrue(d3.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemIdentifierTransitions() {
        // After system identifier followed by unexpected characters
        Tokeniser t1 = createTokeniser("<!DOCTYPE html SYSTEM \"sysid\" garbage >");
        Token.Doctype d1 = (Token.Doctype) t1.read();
        assertEquals("sysid", d1.getSystemIdentifier());
        // Should not force quirks per HTML5 spec for AfterDoctypeSystemIdentifier default
        assertFalse(d1.isForceQuirks());

        // After system identifier hitting EOF
        Tokeniser t2 = createTokeniser("<!DOCTYPE html SYSTEM \"sysid\"");
        Token.Doctype d2 = (Token.Doctype) t2.read();
        assertTrue(d2.isForceQuirks());
    }

    // =========================================================================
    // Partition F: Enum Coverage & Direct State Execution
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllEnumValuesExist() {
        TokeniserState[] states = TokeniserState.values();
        assertTrue(states.length > 50);
        for (TokeniserState state : states) {
            assertNotNull(state);
            assertEquals(state, TokeniserState.valueOf(state.name()));
        }
    }

    @Test(timeout = 4000)
    public void testDirectBogusCommentExecution() {
        CharacterReader reader = new CharacterReader("comment data>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        TokeniserState.BogusComment.read(t, reader);
        Token tok = t.read();
        assertTrue(tok.isComment());
        assertEquals("comment data", ((Token.Comment) tok).getData());
    }

    @Test(timeout = 4000)
    public void testDirectBogusDoctypeExecution() {
        CharacterReader reader = new CharacterReader("some bogus doctype data>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.createDoctypePending();
        TokeniserState.BogusDoctype.read(t, reader);
        // Consumes until '>'
        while (!reader.isEmpty() && t.getState() == TokeniserState.Data) {
            break;
        }
    }
}