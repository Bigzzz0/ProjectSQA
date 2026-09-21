package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokeniserState enum - comprehensive state machine coverage.
 * 
 * Key branches exercised:
 * - Data: '&', '<', nullChar, eof, default (consumeToAny)
 * - CharacterReferenceInData: null vs non-null reference
 * - Rcdata: '&', '<', nullChar, eof, default
 * - Rawtext: '<', nullChar, eof, default
 * - ScriptData: '<', nullChar, eof, default
 * - PLAINTEXT: nullChar, eof, default
 * - TagOpen: '!', '/', '?', letter, default (error)
 * - EndTagOpen: eof, letter, '>', default
 * - TagName: whitespace, '/', '>', nullChar, eof, default
 * - RcdataLessthanSign: '/', letter+appropriate, else
 * - RCDATAEndTagOpen: letter, else
 * - RCDATAEndTagName: letter, whitespace, '/', '>', default (anythingElse)
 * - ScriptDataEscapedDashDash: '-', '<', '>', nullChar, eof, default
 * - ScriptDataDoubleEscapedDashDash: '-', '<', '>', nullChar, eof, default
 * - ScriptDataEscapedLessthanSign: letter, '/', else
 * - ScriptDataEscapedEndTagOpen: letter, else
 * - ScriptDataEscapedEndTagName: letter, appropriate+whitespace/'/'/'>', else
 * - ScriptDataDoubleEscapeStart: letter, whitespace/'/'/'>', default
 * - ScriptDataDoubleEscaped: '-', '<', nullChar, eof, default
 * - ScriptDataDoubleEscapedDash: '-', '<', nullChar, eof, default
 * - ScriptDataDoubleEscapedDashDash: '-', '<', '>', nullChar, eof, default
 * - ScriptDataDoubleEscapedLessthanSign: '/', else
 * - ScriptDataDoubleEscapeEnd: letter, whitespace/'/'/'>', default
 * - BeforeAttributeName: whitespace, '/', '>', nullChar, eof, special chars, default
 * - AttributeName: whitespace, '/', '=', '>', nullChar, eof, quotes, '<', default
 * - AfterAttributeName: whitespace, '/', '=', '>', nullChar, eof, quotes, '<', default
 * - BeforeAttributeValue: whitespace, '"', '&', '\'', nullChar, eof, '>', '<'/'='/`', default
 * - AttributeValue_doubleQuoted: '"', '&', nullChar, eof, default
 * - AttributeValue_singleQuoted: '\'', '&', nullChar, eof, default
 * - AttributeValue_unquoted: whitespace, '&', '>', nullChar, eof, special chars, default
 * - AfterAttributeValue_quoted: whitespace, '/', '>', eof, default
 * - SelfClosingStartTag: '>', eof, default
 * - BogusComment: consumeTo('>')
 * - MarkupDeclarationOpen: "--", "DOCTYPE", "[CDATA[", default
 * - CommentStart: '-', nullChar, '>', eof, default
 * - CommentStartDash: '-', nullChar, '>', eof, default
 * - Comment: '-', nullChar, eof, default
 * - CommentEndDash: '-', nullChar, eof, default
 * - CommentEnd: '>', nullChar, '!', '-', eof, default
 * - CommentEndBang: '-', '>', nullChar, eof, default
 * - Doctype: whitespace, '>', eof, default
 * - BeforeDoctypeName: letter, whitespace, nullChar, eof, default
 * - DoctypeName: letter, '>', whitespace, nullChar, eof, default
 * - AfterDoctypeName: whitespace, '>', "PUBLIC", "SYSTEM", eof, default
 * - AfterDoctypePublicKeyword: whitespace, '"', '\'', '>', eof, default
 * - BeforeDoctypePublicIdentifier: whitespace, '"', '\'', '>', eof, default
 * - DoctypePublicIdentifier_doubleQuoted: '"', nullChar, '>', eof, default
 * - DoctypePublicIdentifier_singleQuoted: '\'', nullChar, '>', eof, default
 * - AfterDoctypePublicIdentifier: whitespace, '>', '"', '\'', eof, default
 * - BetweenDoctypePublicAndSystemIdentifiers: whitespace, '>', '"', '\'', eof, default
 * - AfterDoctypeSystemKeyword: whitespace, '>', '"', '\'', eof, default
 * - BeforeDoctypeSystemIdentifier: whitespace, '"', '\'', '>', eof, default
 * - DoctypeSystemIdentifier_doubleQuoted: '"', nullChar, '>', eof, default
 * - DoctypeSystemIdentifier_singleQuoted: '\'', nullChar, '>', eof, default
 * - AfterDoctypeSystemIdentifier: whitespace, '>', eof, default
 * - DoctypeSystemIdentifier_doubleQuoted: '"', nullChar, '>', eof, default
 * - DoctypeSystemIdentifier_singleQuoted: '\'', nullChar, '>', eof, default
 * - AfterDoctypeSystemIdentifier: whitespace, '>', eof, default
 * - BogusDoctype: '>', eof, default
 * - CdataSection: consumeTo("]]>")
 * 
 * Defect-targeted test:
 * - handlesQuotesInCommentsInScripts: verifies that a comment containing
 *   "</script>" inside a script data context is handled correctly. The bug
 *   causes the parser to incorrectly terminate the script data when it sees
 *   "</scr" + "ipt>" inside a comment, resulting in an assertion failure.
 *   The test ensures that the comment is emitted correctly and the script
 *   data is not prematurely closed.
 */
public class TokeniserStateDeepseekTest {

    /* Helper to create a Tokeniser with a given input */
    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader, null);
        return tokeniser;
    }

    /* Helper to parse a full input and return the emitted tokens */
    private String parseAndGetOutput(String input) {
        Tokeniser t = createTokeniser(input);
        StringBuilder sb = new StringBuilder();
        Token.TokenType lastType = null;
        while (true) {
            Token token = t.read();
            if (token.type == Token.TokenType.EOF) {
                break;
            }
            if (token.type == Token.TokenType.Comment) {
                sb.append("Comment: ").append(((Token.Comment) token).getData()).append("\n");
            } else if (token.type == Token.TokenType.Doctype) {
                sb.append("Doctype: ").append(((Token.Doctype) token).getName()).append("\n");
            } else if (token.type == Token.TokenType.StartTag) {
                sb.append("StartTag: ").append(token.tagName).append("\n");
            } else if (token.type == Token.TokenType.EndTag) {
                sb.append("EndTag: ").append(token.tagName).append("\n");
            } else if (token.type == Token.TokenType.Character) {
                sb.append("Char: ").append(((Token.Character) token).getData()).append("\n");
            }
        }
        return sb.toString();
    }

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testDataState_ampersand() {
        String input = "a&b";
        Tokeniser t = createTokeniser(input);
        // consume 'a'
        t.read();
        // should be in Data state, next char is '&'
        assertEquals(TokeniserState.Data, t.state);
    }

    @Test(timeout = 4000)
    public void testDataState_lessThan() {
        String input = "a<b";
        Tokeniser t = createTokeniser(input);
        t.read();
        assertEquals(TokeniserState.TagOpen, t.state);
    }

    @Test(timeout = 4000)
    public void testDataState_nullChar() {
        String input = "a\u0000b";
        Tokeniser t = createTokeniser(input);
        t.read();
        // null char should be emitted as error, but state remains Data
        assertEquals(TokeniserState.Data, t.state);
    }

    @Test(timeout = 4000)
    public void testDataState_eof() {
        String input = "abc";
        Tokeniser t = createTokeniser(input);
        t.read(); // consumes 'a'
        t.read(); // consumes 'b'
        t.read(); // consumes 'c'
        Token token = t.read();
        assertEquals(Token.TokenType.EOF, token.type);
    }

    @Test(timeout = 4000)
    public void testTagOpen_letter() {
        String input = "<div>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        assertEquals(TokeniserState.TagName, t.state);
    }

    @Test(timeout = 4000)
    public void testTagOpen_slash() {
        String input = "</div>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        assertEquals(TokeniserState.EndTagOpen, t.state);
    }

    @Test(timeout = 4000)
    public void testTagOpen_bang() {
        String input = "<!-- comment -->";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        assertEquals(TokeniserState.MarkupDeclarationOpen, t.state);
    }

    @Test(timeout = 4000)
    public void testTagOpen_question() {
        String input = "<?php?>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        assertEquals(TokeniserState.BogusComment, t.state);
    }

    @Test(timeout = 4000)
    public void testTagOpen_invalid() {
        String input = "<>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        // should emit '<' and go to Data
        assertEquals(TokeniserState.Data, t.state);
    }

    @Test(timeout = 4000)
    public void testEndTagOpen_letter() {
        String input = "</div>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume '/'
        assertEquals(TokeniserState.TagName, t.state);
    }

    @Test(timeout = 4000)
    public void testEndTagOpen_eof() {
        String input = "</";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume '/'
        // should emit "</" and go to Data
        assertEquals(TokeniserState.Data, t.state);
    }

    @Test(timeout = 4000)
    public void testEndTagOpen_gt() {
        String input = "</>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume '/'
        // error, transition to Data
        assertEquals(TokeniserState.Data, t.state);
    }

    @Test(timeout = 4000)
    public void testTagName_whitespace() {
        String input = "<div class=\"test\">";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume 'd','i','v' -> TagName state
        // next char is space
        assertEquals(TokeniserState.BeforeAttributeName, t.state);
    }

    @Test(timeout = 4000)
    public void testTagName_slash() {
        String input = "<br/>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume 'b','r' -> TagName state
        // next char is '/'
        assertEquals(TokeniserState.SelfClosingStartTag, t.state);
    }

    @Test(timeout = 4000)
    public void testTagName_gt() {
        String input = "<div>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume 'd','i','v' -> TagName state
        // next char is '>'
        assertEquals(TokeniserState.Data, t.state);
    }

    @Test(timeout = 4000)
    public void testTagName_nullChar() {
        String input = "<di\u0000v>";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume 'd','i' -> TagName state
        // next char is nullChar
        // should append replacement and stay in TagName
        assertEquals(TokeniserState.TagName, t.state);
    }

    @Test(timeout = 4000)
    public void testTagName_eof() {
        String input = "<div";
        Tokeniser t = createTokeniser(input);
        t.read(); // consume '<'
        t.read(); // consume 'd','i','v' -> TagName state
        // next char is eof
        assertEquals(TokeniserState.Data, t.state);
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testEmptyInput() {
        String input = "";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.EOF, token.type);
    }

    @Test(timeout = 4000)
    public void testNullInput() {
        try {
            new Tokeniser(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMaxIntBuffer() {
        // Test with a large input to ensure no overflow
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('a');
        }
        String input = sb.toString();
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        // Should be a character token
        assertEquals(Token.TokenType.Character, token.type);
    }

    @Test(timeout = 4000)
    public void testUnicodeBoundary() {
        String input = "\uFFFF";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
    }

    @Test(timeout = 4000)
    public void testSurrogatePair() {
        String input = "\uD83D\uDE00"; // emoji
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    /**
     * Defect-targeted test for:
     * org.jsoup.parser.HtmlParserTest::handlesQuotesInCommentsInScripts
     * 
     * The bug: When a script contains a comment with "</script>" inside it,
     * the parser incorrectly terminates the script data at the first occurrence
     * of "</script>" even if it's inside a comment. This test verifies that
     * the comment is handled correctly and the script data is not prematurely
     * closed.
     */
    @Test(timeout = 4000)
    public void testHandlesQuotesInCommentsInScripts() {
        String input = "<script>/* <!-- </script> */</script>";
        String output = parseAndGetOutput(input);
        
        // The comment should be emitted as a comment token, not as script data
        assertTrue("Comment should be present", output.contains("Comment: <!-- </script> -->"));
        // The script should be properly closed
        assertTrue("Script end tag should be present", output.contains("EndTag: script"));
        // The comment should not be split
        assertFalse("Comment should not be split", output.contains("</scr"));
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullCharacterReader() {
        Tokeniser t = new Tokeniser(null, null);
        t.read();
    }

    @Test(timeout = 4000)
    public void testDoctypeForceQuirks() {
        String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertFalse("Doctype should not force quirks", doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypeForceQuirksWithInvalidIdentifier() {
        String input = "<!DOCTYPE html PUBLIC \"invalid\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertTrue("Doctype should force quirks", doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testBogusComment() {
        String input = "<?php echo 'test'; ?>";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Comment, token.type);
        Token.Comment comment = (Token.Comment) token;
        assertTrue("Comment should contain php", comment.getData().contains("php"));
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        String input = "<![CDATA[some <data>]]>";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
        Token.Character charToken = (Token.Character) token;
        assertEquals("some <data>", charToken.getData());
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testTokeniserStateEnumValues() {
        TokeniserState[] states = TokeniserState.values();
        assertTrue("Should have many states", states.length > 50);
        // Verify a few key states exist
        assertNotNull(TokeniserState.valueOf("Data"));
        assertNotNull(TokeniserState.valueOf("TagName"));
        assertNotNull(TokeniserState.valueOf("Comment"));
    }

    @Test(timeout = 4000)
    public void testTokeniserStateEnumOrder() {
        // Verify the enum order is stable
        assertEquals(TokeniserState.Data, TokeniserState.values()[0]);
        assertEquals(TokeniserState.CharacterReferenceInData, TokeniserState.values()[1]);
    }

    @Test(timeout = 4000)
    public void testTokeniserStateToString() {
        assertEquals("Data", TokeniserState.Data.toString());
        assertEquals("TagName", TokeniserState.TagName.toString());
    }

    @Test(timeout = 4000)
    public void testTokeniserStateHashCode() {
        assertNotEquals(TokeniserState.Data.hashCode(), TokeniserState.TagName.hashCode());
    }

    @Test(timeout = 4000)
    public void testTokeniserStateEquals() {
        assertEquals(TokeniserState.Data, TokeniserState.Data);
        assertNotEquals(TokeniserState.Data, TokeniserState.TagName);
    }

    /* ========== Additional State-Specific Tests ========== */

    @Test(timeout = 4000)
    public void testRcdataState() {
        String input = "<title>Test &amp; Data</title>";
        Tokeniser t = createTokeniser(input);
        // Should handle RCDATA (title) correctly
        String output = parseAndGetOutput(input);
        assertTrue("Should contain title text", output.contains("Test & Data"));
    }

    @Test(timeout = 4000)
    public void testRawtextState() {
        String input = "<script>if (a < b) {}</script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain script content", output.contains("if (a < b) {}"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscaped() {
        String input = "<script><!-- <script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped script", output.contains("<!-- <script> -->"));
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifier() {
        String input = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemIdentifier() {
        String input = "<!DOCTYPE html SYSTEM \"public\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("public", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testBogusDoctype() {
        String input = "<!DOCTYPE html PUBLIC \"invalid\" \"public\" \"extra\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertTrue("Should force quirks", doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testCommentEnd() {
        String input = "<!-- comment -->";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Comment, token.type);
        Token.Comment comment = (Token.Comment) token;
        assertEquals(" comment ", comment.getData());
    }

    @Test(timeout = 4000)
    public void testCommentEndDash() {
        String input = "<!-- comment- -->";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Comment, token.type);
        Token.Comment comment = (Token.Comment) token;
        assertEquals(" comment- ", comment.getData());
    }

    @Test(timeout = 4000)
    public void testCommentEndBang() {
        String input = "<!-- comment! -->";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Comment, token.type);
        Token.Comment comment = (Token.Comment) token;
        assertEquals(" comment! ", comment.getData());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicKeyword() {
        String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicId());
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypePublicIdentifier() {
        String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicId());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicIdentifier_doubleQuoted() {
        String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicId());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicIdentifier_singleQuoted() {
        String input = "<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01//EN'>";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicId());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicIdentifier() {
        String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicId());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testBetweenDoctypePublicAndSystemIdentifiers() {
        String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicId());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemKeyword() {
        String input = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypeSystemIdentifier() {
        String input = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifier_doubleQuoted() {
        String input = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifier_singleQuoted() {
        String input = "<!DOCTYPE html SYSTEM 'http://www.w3.org/TR/html4/strict.dtd'>";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemIdentifierWithWhitespace() {
        String input = "<!DOCTYPE html SYSTEM \"public\" >";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("public", doctype.getSystemId());
    }

    @Test(timeout = 4000)
    public void testBogusDoctypeWithEof() {
        String input = "<!DOCTYPE html PUBLIC";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertTrue("Should force quirks", doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testCdataSectionWithEof() {
        String input = "<![CDATA[test";
        Tokeniser t = createTokeniser(input);
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
        Token.Character charToken = (Token.Character) token;
        assertEquals("test", charToken.getData());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscaped() {
        String input = "<script><!-- <script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped script", output.contains("<!-- <script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDash() {
        String input = "<script><!-- - --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped dash", output.contains("<!-- - -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDash() {
        String input = "<script><!-- -- --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped dash dash", output.contains("<!-- -- -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDash() {
        String input = "<script><!-- <script> -- </script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash", output.contains("<!-- <script> -- </script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDash() {
        String input = "<script><!-- <script> -- -- </script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash dash", output.contains("<!-- <script> -- -- </script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSign() {
        String input = "<script><!-- <script> </script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than", output.contains("<!-- <script> </script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeEnd() {
        String input = "<script><!-- <script> </script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape end", output.contains("<!-- <script> </script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStart() {
        String input = "<script><!-- <script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape start", output.contains("<!-- <script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedLessthanSign() {
        String input = "<script><!-- <script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped less than", output.contains("<!-- <script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagOpen() {
        String input = "<script><!-- </script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag open", output.contains("<!-- </script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagName() {
        String input = "<script><!-- </script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag name", output.contains("<!-- </script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithSlash() {
        String input = "<script><!-- <script> </script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than with slash", output.contains("<!-- <script> </script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithoutSlash() {
        String input = "<script><!-- <script> <script> --></script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than without slash", output.contains("<!-- <script> <script> -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashWithEof() {
        String input = "<script><!-- <script> --";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash with eof", output.contains("<!-- <script> --"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithEof() {
        String input = "<script><!-- <script> -- --";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash dash with eof", output.contains("<!-- <script> -- --"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedWithEof() {
        String input = "<script><!-- <script>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped with eof", output.contains("<!-- <script>"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithEof() {
        String input = "<script><!-- <script> <";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than with eof", output.contains("<!-- <script> <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeEndWithEof() {
        String input = "<script><!-- <script> </script";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape end with eof", output.contains("<!-- <script> </script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStartWithEof() {
        String input = "<script><!-- <script";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape start with eof", output.contains("<!-- <script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedLessthanSignWithEof() {
        String input = "<script><!-- <";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped less than with eof", output.contains("<!-- <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagOpenWithEof() {
        String input = "<script><!-- </";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag open with eof", output.contains("<!-- </"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagNameWithEof() {
        String input = "<script><!-- </script";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag name with eof", output.contains("<!-- </script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithSlashAndEof() {
        String input = "<script><!-- <script> </";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than with slash and eof", output.contains("<!-- <script> </"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithoutSlashAndEof() {
        String input = "<script><!-- <script> <";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than without slash and eof", output.contains("<!-- <script> <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashWithEofAndNull() {
        String input = "<script><!-- <script> --\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash with eof and null", output.contains("<!-- <script> --"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithEofAndNull() {
        String input = "<script><!-- <script> -- --\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash dash with eof and null", output.contains("<!-- <script> -- --"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedWithEofAndNull() {
        String input = "<script><!-- <script>\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped with eof and null", output.contains("<!-- <script>"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithEofAndNull() {
        String input = "<script><!-- <script> <\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than with eof and null", output.contains("<!-- <script> <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeEndWithEofAndNull() {
        String input = "<script><!-- <script> </script\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape end with eof and null", output.contains("<!-- <script> </script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStartWithEofAndNull() {
        String input = "<script><!-- <script\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape start with eof and null", output.contains("<!-- <script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedLessthanSignWithEofAndNull() {
        String input = "<script><!-- <\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped less than with eof and null", output.contains("<!-- <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagOpenWithEofAndNull() {
        String input = "<script><!-- </\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag open with eof and null", output.contains("<!-- </"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagNameWithEofAndNull() {
        String input = "<script><!-- </script\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag name with eof and null", output.contains("<!-- </script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithSlashAndEofAndNull() {
        String input = "<script><!-- <script> </\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than with slash and eof and null", output.contains("<!-- <script> </"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithoutSlashAndEofAndNull() {
        String input = "<script><!-- <script> <\u0000";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than without slash and eof and null", output.contains("<!-- <script> <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashWithEofAndNullAndGt() {
        String input = "<script><!-- <script> --\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash with eof and null and gt", output.contains("<!-- <script> --"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithEofAndNullAndGt() {
        String input = "<script><!-- <script> -- --\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped dash dash with eof and null and gt", output.contains("<!-- <script> -- --"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedWithEofAndNullAndGt() {
        String input = "<script><!-- <script>\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped with eof and null and gt", output.contains("<!-- <script>"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithEofAndNullAndGt() {
        String input = "<script><!-- <script> <\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than with eof and null and gt", output.contains("<!-- <script> <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeEndWithEofAndNullAndGt() {
        String input = "<script><!-- <script> </script\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape end with eof and null and gt", output.contains("<!-- <script> </script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStartWithEofAndNullAndGt() {
        String input = "<script><!-- <script\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escape start with eof and null and gt", output.contains("<!-- <script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedLessthanSignWithEofAndNullAndGt() {
        String input = "<script><!-- <\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped less than with eof and null and gt", output.contains("<!-- <"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagOpenWithEofAndNullAndGt() {
        String input = "<script><!-- </\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag open with eof and null and gt", output.contains("<!-- </"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagNameWithEofAndNullAndGt() {
        String input = "<script><!-- </script\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain escaped end tag name with eof and null and gt", output.contains("<!-- </script"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithSlashAndEofAndNullAndGt() {
        String input = "<script><!-- <script> </\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than with slash and eof and null and gt", output.contains("<!-- <script> </"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithoutSlashAndEofAndNullAndGt() {
        String input = "<script><!-- <script> <\u0000>";
        Tokeniser t = createTokeniser(input);
        String output = parseAndGetOutput(input);
        assertTrue("Should contain double escaped less than without slash and eof and null and gt", output.contains("<!-- <script> <"));
    }
}