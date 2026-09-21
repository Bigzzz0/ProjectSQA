package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokeniserState enum - comprehensive state machine coverage
 * 
 * Defect: SelfClosingStartTag ignores last character - when a self-closing tag
 * is parsed like <div/ >, the '/' is consumed but the following character is
 * not properly handled, causing the last character before '>' to be dropped.
 * 
 * Branch coverage targets:
 * - Data: &, <, nullChar, eof, default
 * - TagOpen: !, /, ?, letter, default
 * - EndTagOpen: empty, letter, >, default
 * - TagName: whitespace, /, >, nullChar, eof
 * - BeforeAttributeName: whitespace, /, >, nullChar, eof, special chars, default
 * - AttributeName: whitespace, /, =, >, nullChar, eof, special chars
 * - AfterAttributeName: whitespace, /, =, >, nullChar, eof, special chars, default
 * - BeforeAttributeValue: whitespace, ", &, ', nullChar, eof, >, special, default
 * - AttributeValue_*: quotes, &, nullChar, eof, default
 * - AfterAttributeValue_quoted: whitespace, /, >, eof, default
 * - SelfClosingStartTag: >, eof, default (defect zone)
 * - Comment states: all transitions
 * - Doctype states: all transitions
 * - Script/Rawtext states: all transitions
 * - CdataSection: normal and eof
 * 
 * Boundary conditions:
 * - Empty input
 * - Null characters
 * - EOF handling
 * - Whitespace variations (\t, \n, \r, \f, space)
 * - Special characters in attributes
 * - Self-closing tag edge cases
 */
public class TokeniserStateDeepseekTest {

    // Helper to create a Tokeniser with given input
    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        return new Tokeniser(reader, ParseErrorList.noTracking());
    }

    // Helper to run tokeniser and collect output
    private String tokenise(String input) {
        Tokeniser t = createTokeniser(input);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        return sb.toString();
    }

    // ==================== PARTITION A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDataStateBasicText() {
        String result = tokenise("hello world");
        assertEquals("hello world", result);
    }

    @Test(timeout = 4000)
    public void testDataStateWithAmpersand() {
        String result = tokenise("a&b");
        assertEquals("a&b", result);
    }

    @Test(timeout = 4000)
    public void testDataStateWithTagOpen() {
        String result = tokenise("a<b");
        assertEquals("a<b", result);
    }

    @Test(timeout = 4000)
    public void testDataStateWithNullChar() {
        String result = tokenise("a\u0000b");
        assertEquals("a\uFFFDb", result);
    }

    @Test(timeout = 4000)
    public void testDataStateWithEOF() {
        String result = tokenise("");
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testRcdataStateBasic() {
        Tokeniser t = createTokeniser("text");
        t.transition(TokeniserState.Rcdata);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("text", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRcdataStateWithAmpersand() {
        Tokeniser t = createTokeniser("a&b");
        t.transition(TokeniserState.Rcdata);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a&b", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRcdataStateWithNull() {
        Tokeniser t = createTokeniser("a\u0000b");
        t.transition(TokeniserState.Rcdata);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a\uFFFDb", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRawtextState() {
        Tokeniser t = createTokeniser("raw<text");
        t.transition(TokeniserState.Rawtext);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("raw<text", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataState() {
        Tokeniser t = createTokeniser("var x = 1;");
        t.transition(TokeniserState.ScriptData);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("var x = 1;", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPlaintextState() {
        Tokeniser t = createTokeniser("plain text");
        t.transition(TokeniserState.PLAINTEXT);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("plain text", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPlaintextStateWithNull() {
        Tokeniser t = createTokeniser("a\u0000b");
        t.transition(TokeniserState.PLAINTEXT);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a\uFFFDb", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTagOpenWithBang() {
        String result = tokenise("<!DOCTYPE html>");
        assertTrue(result.contains("<!DOCTYPE html>"));
    }

    @Test(timeout = 4000)
    public void testTagOpenWithSlash() {
        String result = tokenise("</div>");
        assertTrue(result.contains("</div>"));
    }

    @Test(timeout = 4000)
    public void testTagOpenWithQuestion() {
        String result = tokenise("<?xml version=\"1.0\"?>");
        assertTrue(result.contains("<?xml version=\"1.0\"?>"));
    }

    @Test(timeout = 4000)
    public void testTagOpenWithLetter() {
        String result = tokenise("<div>");
        assertTrue(result.contains("<div>"));
    }

    @Test(timeout = 4000)
    public void testTagOpenWithInvalidChar() {
        String result = tokenise("<1");
        assertEquals("<1", result);
    }

    @Test(timeout = 4000)
    public void testEndTagOpenWithEOF() {
        String result = tokenise("</");
        assertEquals("</", result);
    }

    @Test(timeout = 4000)
    public void testEndTagOpenWithLetter() {
        String result = tokenise("</div>");
        assertTrue(result.contains("</div>"));
    }

    @Test(timeout = 4000)
    public void testEndTagOpenWithGreaterThan() {
        String result = tokenise("</>");
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testEndTagOpenWithInvalid() {
        String result = tokenise("</1>");
        assertTrue(result.contains("</1>"));
    }

    @Test(timeout = 4000)
    public void testTagNameWithWhitespace() {
        String result = tokenise("<div class=\"test\">");
        assertTrue(result.contains("<div"));
        assertTrue(result.contains("class=\"test\""));
    }

    @Test(timeout = 4000)
    public void testTagNameWithSlash() {
        String result = tokenise("<br/>");
        assertTrue(result.contains("<br"));
    }

    @Test(timeout = 4000)
    public void testTagNameWithGreaterThan() {
        String result = tokenise("<div>");
        assertTrue(result.contains("<div>"));
    }

    @Test(timeout = 4000)
    public void testTagNameWithNull() {
        String result = tokenise("<di\u0000v>");
        assertTrue(result.contains("<di\uFFFDev"));
    }

    @Test(timeout = 4000)
    public void testTagNameWithEOF() {
        String result = tokenise("<div");
        assertTrue(result.contains("<div"));
    }

    @Test(timeout = 4000)
    public void testRcdataLessthanSignWithSlash() {
        Tokeniser t = createTokeniser("</title>");
        t.transition(TokeniserState.Rcdata);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</title>"));
    }

    @Test(timeout = 4000)
    public void testRcdataLessthanSignWithLetterNoEndTag() {
        Tokeniser t = createTokeniser("<div>");
        t.transition(TokeniserState.Rcdata);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<div>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRcdataLessthanSignDefault() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.Rcdata);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagOpenWithLetter() {
        Tokeniser t = createTokeniser("</title>");
        t.transition(TokeniserState.RcdataLessthanSign);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</title>"));
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagOpenWithoutLetter() {
        Tokeniser t = createTokeniser("</1>");
        t.transition(TokeniserState.RcdataLessthanSign);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("</1>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagNameWithLetter() {
        Tokeniser t = createTokeniser("title>");
        t.transition(TokeniserState.RCDATAEndTagOpen);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</title>"));
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagNameWithWhitespace() {
        Tokeniser t = createTokeniser("title >");
        t.transition(TokeniserState.RCDATAEndTagOpen);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</title"));
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagNameWithSlash() {
        Tokeniser t = createTokeniser("title/>");
        t.transition(TokeniserState.RCDATAEndTagOpen);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</title"));
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagNameWithGreaterThan() {
        Tokeniser t = createTokeniser("title>");
        t.transition(TokeniserState.RCDATAEndTagOpen);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</title>"));
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagNameDefault() {
        Tokeniser t = createTokeniser("title1");
        t.transition(TokeniserState.RCDATAEndTagOpen);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</title"));
    }

    @Test(timeout = 4000)
    public void testRawtextLessthanSignWithSlash() {
        Tokeniser t = createTokeniser("</xmp>");
        t.transition(TokeniserState.Rawtext);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</xmp>"));
    }

    @Test(timeout = 4000)
    public void testRawtextLessthanSignDefault() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.Rawtext);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataLessthanSignWithSlash() {
        Tokeniser t = createTokeniser("</script>");
        t.transition(TokeniserState.ScriptData);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</script>"));
    }

    @Test(timeout = 4000)
    public void testScriptDataLessthanSignWithBang() {
        Tokeniser t = createTokeniser("<!--");
        t.transition(TokeniserState.ScriptData);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("<!--"));
    }

    @Test(timeout = 4000)
    public void testScriptDataLessthanSignDefault() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.ScriptData);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapeStartWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataEscapeStart);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapeStartWithoutDash() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataEscapeStart);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapeStartDashWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataEscapeStartDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapeStartDashWithoutDash() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataEscapeStartDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedWithEOF() {
        Tokeniser t = createTokeniser("");
        t.transition(TokeniserState.ScriptDataEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedWithLessThan() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.ScriptDataEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedWithNull() {
        Tokeniser t = createTokeniser("\u0000");
        t.transition(TokeniserState.ScriptDataEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("\uFFFD", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDefault() {
        Tokeniser t = createTokeniser("abc");
        t.transition(TokeniserState.ScriptDataEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashWithEOF() {
        Tokeniser t = createTokeniser("");
        t.transition(TokeniserState.ScriptDataEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashWithLessThan() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.ScriptDataEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashWithNull() {
        Tokeniser t = createTokeniser("\u0000");
        t.transition(TokeniserState.ScriptDataEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("\uFFFD", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashWithEOF() {
        Tokeniser t = createTokeniser("");
        t.transition(TokeniserState.ScriptDataEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashWithLessThan() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.ScriptDataEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashWithGreaterThan() {
        Tokeniser t = createTokeniser(">");
        t.transition(TokeniserState.ScriptDataEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals(">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashWithNull() {
        Tokeniser t = createTokeniser("\u0000");
        t.transition(TokeniserState.ScriptDataEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("\uFFFD", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedLessthanSignWithLetter() {
        Tokeniser t = createTokeniser("script");
        t.transition(TokeniserState.ScriptDataEscapedLessthanSign);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<script", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedLessthanSignWithSlash() {
        Tokeniser t = createTokeniser("/");
        t.transition(TokeniserState.ScriptDataEscapedLessthanSign);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("</", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedLessthanSignDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataEscapedLessthanSign);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagOpenWithLetter() {
        Tokeniser t = createTokeniser("script>");
        t.transition(TokeniserState.ScriptDataEscapedEndTagOpen);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertTrue(sb.toString().contains("</script>"));
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagOpenWithoutLetter() {
        Tokeniser t = createTokeniser("1>");
        t.transition(TokeniserState.ScriptDataEscapedEndTagOpen);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("</1>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStartWithLetter() {
        Tokeniser t = createTokeniser("script");
        t.transition(TokeniserState.ScriptDataDoubleEscapeStart);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("script", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStartWithWhitespace() {
        Tokeniser t = createTokeniser(" ");
        t.transition(TokeniserState.ScriptDataDoubleEscapeStart);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals(" ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStartDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataDoubleEscapeStart);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataDoubleEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedWithLessThan() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.ScriptDataDoubleEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedWithNull() {
        Tokeniser t = createTokeniser("\u0000");
        t.transition(TokeniserState.ScriptDataDoubleEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("\uFFFD", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedWithEOF() {
        Tokeniser t = createTokeniser("");
        t.transition(TokeniserState.ScriptDataDoubleEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDefault() {
        Tokeniser t = createTokeniser("abc");
        t.transition(TokeniserState.ScriptDataDoubleEscaped);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashWithLessThan() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashWithNull() {
        Tokeniser t = createTokeniser("\u0000");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("\uFFFD", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashWithEOF() {
        Tokeniser t = createTokeniser("");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithDash() {
        Tokeniser t = createTokeniser("-");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("-", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithLessThan() {
        Tokeniser t = createTokeniser("<");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("<", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithGreaterThan() {
        Tokeniser t = createTokeniser(">");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals(">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithNull() {
        Tokeniser t = createTokeniser("\u0000");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("\uFFFD", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashWithEOF() {
        Tokeniser t = createTokeniser("");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataDoubleEscapedDashDash);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignWithSlash() {
        Tokeniser t = createTokeniser("/");
        t.transition(TokeniserState.ScriptDataDoubleEscapedLessthanSign);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("/", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedLessthanSignDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataDoubleEscapedLessthanSign);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeEndWithLetter() {
        Tokeniser t = createTokeniser("script");
        t.transition(TokeniserState.ScriptDataDoubleEscapeEnd);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("script", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeEndWithWhitespace() {
        Tokeniser t = createTokeniser(" ");
        t.transition(TokeniserState.ScriptDataDoubleEscapeEnd);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals(" ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeEndDefault() {
        Tokeniser t = createTokeniser("a");
        t.transition(TokeniserState.ScriptDataDoubleEscapeEnd);
        StringBuilder sb = new StringBuilder();
        Token token;
        while ((token = t.read()) != null) {
            if (token instanceof Token.EOF) break;
            sb.append(token.toString());
        }
        assertEquals("a", sb.toString());
    }

    // ==================== PARTITION B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyInput() {
        String result = tokenise("");
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testSingleCharacter() {
        String result = tokenise("a");
        assertEquals("a", result);
    }

    @Test(timeout = 4000)
    public void testOnlyWhitespace() {
        String result = tokenise("   ");
        assertEquals("   ", result);
    }

    @Test(timeout = 4000)
    public void testOnlyNullChar() {
        String result = tokenise("\u0000");
        assertEquals("\uFFFD", result);
    }

    @Test(timeout = 4000)
    public void testOnlyLessThan() {
        String result = tokenise("<");
        assertEquals("<", result);
    }

    @Test(timeout = 4000)
    public void testOnlyAmpersand() {
        String result = tokenise("&");
        assertEquals("&", result);
    }

    @Test(timeout = 4000)
    public void testLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String result = tokenise(sb.toString());
        assertEquals(sb.toString(), result);
    }

    @Test(timeout = 4000)
    public void testAllWhitespaceChars() {
        String result = tokenise("\t\n\r\f ");
        assertEquals("\t\n\r\f ", result);
    }

    @Test(timeout = 4000)
    public void testSpecialCharsInData() {
        String result = tokenise("!@#$%^&*()");
        assertEquals("!@#$%^&*()", result);
    }

    @Test(timeout = 4000)
    public void testUnicodeChars() {
        String result = tokenise("héllo wörld");
        assertEquals("héllo wörld", result);
    }

    // ==================== PARTITION C: Defect-Targeted Branch Zone ====================

    /**
     * Defect: SelfClosingStartTag ignores last character
     * When parsing <div/ >, the '/' is consumed but the space after it is
     * incorrectly handled, causing the last character before '>' to be dropped.
     * 
     * The bug is in SelfClosingStartTag state - when it encounters a character
     * that is not '>' or EOF, it transitions to BeforeAttributeName but the
     * character is consumed and lost.
     */
    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithSpace() {
        // This should parse <div/ > as <div> with a space attribute
        // The bug causes the space to be dropped
        String result = tokenise("<div/ >");
        // Expected: <div> with attribute name " " (space)
        assertTrue("Self-closing tag with space should preserve the space", 
                   result.contains("<div") && result.contains(" "));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithAttribute() {
        // Test self-closing tag with attribute after slash
        String result = tokenise("<div/ class=\"test\">");
        assertTrue("Self-closing tag with attribute should preserve attribute",
                   result.contains("class=\"test\""));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithMultipleSpaces() {
        String result = tokenise("<div/  >");
        assertTrue("Multiple spaces after slash should be preserved",
                   result.contains("  "));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithTab() {
        String result = tokenise("<div/\t>");
        assertTrue("Tab after slash should be preserved",
                   result.contains("\t"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithNewline() {
        String result = tokenise("<div/\n>");
        assertTrue("Newline after slash should be preserved",
                   result.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithCarriageReturn() {
        String result = tokenise("<div/\r>");
        assertTrue("Carriage return after slash should be preserved",
                   result.contains("\r"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithFormFeed() {
        String result = tokenise("<div/\f>");
        assertTrue("Form feed after slash should be preserved",
                   result.contains("\f"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithEquals() {
        String result = tokenise("<div/=>");
        assertTrue("Equals after slash should be preserved",
                   result.contains("="));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithQuote() {
        String result = tokenise("<div/\">");
        assertTrue("Quote after slash should be preserved",
                   result.contains("\""));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithSingleQuote() {
        String result = tokenise("<div/'>");
        assertTrue("Single quote after slash should be preserved",
                   result.contains("'"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithLessThan() {
        String result = tokenise("<div/<>");
        assertTrue("Less than after slash should be preserved",
                   result.contains("<"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithNull() {
        String result = tokenise("<div/\u0000>");
        assertTrue("Null after slash should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithEOF() {
        String result = tokenise("<div/");
        assertTrue("Self-closing tag with EOF should still emit tag",
                   result.contains("<div"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagNormal() {
        String result = tokenise("<br/>");
        assertTrue("Normal self-closing tag should work",
                   result.contains("<br"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithLetter() {
        String result = tokenise("<div/a>");
        assertTrue("Letter after slash should be preserved as attribute",
                   result.contains("a"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithDigit() {
        String result = tokenise("<div/1>");
        assertTrue("Digit after slash should be preserved as attribute",
                   result.contains("1"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithUnderscore() {
        String result = tokenise("<div/_>");
        assertTrue("Underscore after slash should be preserved as attribute",
                   result.contains("_"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithDash() {
        String result = tokenise("<div/->");
        assertTrue("Dash after slash should be preserved as attribute",
                   result.contains("-"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagWithDot() {
        String result = tokenise("<div/.>");
        assertTrue("Dot after slash should be preserved as attribute",
                   result.contains("."));
    }

    // ==================== PARTITION D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testBeforeAttributeNameWithSpecialChars() {
        String result = tokenise("<div \"test\">");
        assertTrue("Quote in attribute name should be handled",
                   result.contains("\"test\""));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeNameWithEquals() {
        String result = tokenise("<div =test>");
        assertTrue("Equals in attribute name should be handled",
                   result.contains("=test"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeNameWithLessThan() {
        String result = tokenise("<div <test>");
        assertTrue("Less than in attribute name should be handled",
                   result.contains("<test"));
    }

    @Test(timeout = 4000)
    public void testAttributeNameWithSpecialChars() {
        String result = tokenise("<div a\"b>");
        assertTrue("Quote in attribute name should be handled",
                   result.contains("a\"b"));
    }

    @Test(timeout = 4000)
    public void testAttributeNameWithSingleQuote() {
        String result = tokenise("<div a'b>");
        assertTrue("Single quote in attribute name should be handled",
                   result.contains("a'b"));
    }

    @Test(timeout = 4000)
    public void testAttributeNameWithLessThan() {
        String result = tokenise("<div a<b>");
        assertTrue("Less than in attribute name should be handled",
                   result.contains("a<b"));
    }

    @Test(timeout = 4000)
    public void testAfterAttributeNameWithSpecialChars() {
        String result = tokenise("<div a \"b\">");
        assertTrue("Quote after attribute name should be handled",
                   result.contains("\"b\""));
    }

    @Test(timeout = 4000)
    public void testAfterAttributeNameWithEquals() {
        String result = tokenise("<div a =b>");
        assertTrue("Equals after attribute name should be handled",
                   result.contains("=b"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeValueWithSpecialChars() {
        String result = tokenise("<div a=<b>");
        assertTrue("Less than in attribute value should be handled",
                   result.contains("=<b"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeValueWithEquals() {
        String result = tokenise("<div a==b>");
        assertTrue("Equals in attribute value should be handled",
                   result.contains("==b"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeValueWithBacktick() {
        String result = tokenise("<div a=`b>");
        assertTrue("Backtick in attribute value should be handled",
                   result.contains("=`b"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueUnquotedWithSpecialChars() {
        String result = tokenise("<div a=b\"c>");
        assertTrue("Quote in unquoted attribute value should be handled",
                   result.contains("b\"c"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueUnquotedWithSingleQuote() {
        String result = tokenise("<div a=b'c>");
        assertTrue("Single quote in unquoted attribute value should be handled",
                   result.contains("b'c"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueUnquotedWithLessThan() {
        String result = tokenise("<div a=b<c>");
        assertTrue("Less than in unquoted attribute value should be handled",
                   result.contains("b<c"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueUnquotedWithEquals() {
        String result = tokenise("<div a=b=c>");
        assertTrue("Equals in unquoted attribute value should be handled",
                   result.contains("b=c"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueUnquotedWithBacktick() {
        String result = tokenise("<div a=b`c>");
        assertTrue("Backtick in unquoted attribute value should be handled",
                   result.contains("b`c"));
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValueQuotedWithInvalid() {
        String result = tokenise("<div a=\"b\"c>");
        assertTrue("Invalid char after quoted attribute should be handled",
                   result.contains("c"));
    }

    @Test(timeout = 4000)
    public void testBogusComment() {
        String result = tokenise("<?xml version=\"1.0\"?>");
        assertTrue("Bogus comment should be handled",
                   result.contains("<?xml version=\"1.0\"?>"));
    }

    @Test(timeout = 4000)
    public void testMarkupDeclarationOpenWithInvalid() {
        String result = tokenise("<!invalid>");
        assertTrue("Invalid markup declaration should be handled",
                   result.contains("<!invalid>"));
    }

    @Test(timeout = 4000)
    public void testCommentStartWithNull() {
        String result = tokenise("<!--\u0000-->");
        assertTrue("Null in comment start should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testCommentStartWithGreaterThan() {
        String result = tokenise("<!-->");
        assertTrue("Greater than in comment start should be handled",
                   result.contains("<!-->"));
    }

    @Test(timeout = 4000)
    public void testCommentStartWithEOF() {
        String result = tokenise("<!--");
        assertTrue("EOF in comment start should be handled",
                   result.contains("<!--"));
    }

    @Test(timeout = 4000)
    public void testCommentStartDashWithNull() {
        String result = tokenise("<!--\u0000-->");
        assertTrue("Null in comment start dash should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testCommentStartDashWithGreaterThan() {
        String result = tokenise("<!--->");
        assertTrue("Greater than in comment start dash should be handled",
                   result.contains("<!--->"));
    }

    @Test(timeout = 4000)
    public void testCommentStartDashWithEOF() {
        String result = tokenise("<!--");
        assertTrue("EOF in comment start dash should be handled",
                   result.contains("<!--"));
    }

    @Test(timeout = 4000)
    public void testCommentWithNull() {
        String result = tokenise("<!--a\u0000b-->");
        assertTrue("Null in comment should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testCommentWithEOF() {
        String result = tokenise("<!--abc");
        assertTrue("EOF in comment should be handled",
                   result.contains("<!--abc"));
    }

    @Test(timeout = 4000)
    public void testCommentEndDashWithNull() {
        String result = tokenise("<!--a-\u0000b-->");
        assertTrue("Null in comment end dash should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testCommentEndDashWithEOF() {
        String result = tokenise("<!--a-");
        assertTrue("EOF in comment end dash should be handled",
                   result.contains("<!--a-"));
    }

    @Test(timeout = 4000)
    public void testCommentEndWithNull() {
        String result = tokenise("<!--a--\u0000b-->");
        assertTrue("Null in comment end should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testCommentEndWithBang() {
        String result = tokenise("<!--a--!>");
        assertTrue("Bang in comment end should be handled",
                   result.contains("<!--a--!>"));
    }

    @Test(timeout = 4000)
    public void testCommentEndWithDash() {
        String result = tokenise("<!--a--->");
        assertTrue("Dash in comment end should be handled",
                   result.contains("<!--a--->"));
    }

    @Test(timeout = 4000)
    public void testCommentEndWithEOF() {
        String result = tokenise("<!--a--");
        assertTrue("EOF in comment end should be handled",
                   result.contains("<!--a--"));
    }

    @Test(timeout = 4000)
    public void testCommentEndBangWithDash() {
        String result = tokenise("<!--a--!->");
        assertTrue("Dash in comment end bang should be handled",
                   result.contains("<!--a--!->"));
    }

    @Test(timeout = 4000)
    public void testCommentEndBangWithNull() {
        String result = tokenise("<!--a--!\u0000b-->");
        assertTrue("Null in comment end bang should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testCommentEndBangWithEOF() {
        String result = tokenise("<!--a--!");
        assertTrue("EOF in comment end bang should be handled",
                   result.contains("<!--a--!"));
    }

    @Test(timeout = 4000)
    public void testDoctypeWithEOF() {
        String result = tokenise("<!DOCTYPE");
        assertTrue("EOF in doctype should be handled",
                   result.contains("<!DOCTYPE"));
    }

    @Test(timeout = 4000)
    public void testDoctypeWithGreaterThan() {
        String result = tokenise("<!DOCTYPE>");
        assertTrue("Greater than in doctype should be handled",
                   result.contains("<!DOCTYPE>"));
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypeNameWithNull() {
        String result = tokenise("<!DOCTYPE \u0000>");
        assertTrue("Null in before doctype name should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypeNameWithEOF() {
        String result = tokenise("<!DOCTYPE ");
        assertTrue("EOF in before doctype name should be handled",
                   result.contains("<!DOCTYPE"));
    }

    @Test(timeout = 4000)
    public void testDoctypeNameWithNull() {
        String result = tokenise("<!DOCTYPE h\u0000tml>");
        assertTrue("Null in doctype name should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testDoctypeNameWithEOF() {
        String result = tokenise("<!DOCTYPE html");
        assertTrue("EOF in doctype name should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeNameWithEOF() {
        String result = tokenise("<!DOCTYPE html ");
        assertTrue("EOF in after doctype name should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeNameWithInvalid() {
        String result = tokenise("<!DOCTYPE html invalid>");
        assertTrue("Invalid in after doctype name should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicKeywordWithEOF() {
        String result = tokenise("<!DOCTYPE html PUBLIC");
        assertTrue("EOF in after doctype public keyword should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicKeywordWithInvalid() {
        String result = tokenise("<!DOCTYPE html PUBLIC invalid>");
        assertTrue("Invalid in after doctype public keyword should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypePublicIdentifierWithEOF() {
        String result = tokenise("<!DOCTYPE html PUBLIC ");
        assertTrue("EOF in before doctype public identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypePublicIdentifierWithInvalid() {
        String result = tokenise("<!DOCTYPE html PUBLIC invalid>");
        assertTrue("Invalid in before doctype public identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testDoctypePublicIdentifierDoubleQuotedWithNull() {
        String result = tokenise("<!DOCTYPE html PUBLIC \"a\u0000b\">");
        assertTrue("Null in doctype public identifier should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testDoctypePublicIdentifierDoubleQuotedWithEOF() {
        String result = tokenise("<!DOCTYPE html PUBLIC \"abc");
        assertTrue("EOF in doctype public identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testDoctypePublicIdentifierSingleQuotedWithNull() {
        String result = tokenise("<!DOCTYPE html PUBLIC 'a\u0000b'>");
        assertTrue("Null in doctype public identifier should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testDoctypePublicIdentifierSingleQuotedWithEOF() {
        String result = tokenise("<!DOCTYPE html PUBLIC 'abc");
        assertTrue("EOF in doctype public identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicIdentifierWithEOF() {
        String result = tokenise("<!DOCTYPE html PUBLIC \"abc\"");
        assertTrue("EOF in after doctype public identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicIdentifierWithInvalid() {
        String result = tokenise("<!DOCTYPE html PUBLIC \"abc\" invalid>");
        assertTrue("Invalid in after doctype public identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testBetweenDoctypePublicAndSystemIdentifiersWithEOF() {
        String result = tokenise("<!DOCTYPE html PUBLIC \"abc\" ");
        assertTrue("EOF in between doctype identifiers should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testBetweenDoctypePublicAndSystemIdentifiersWithInvalid() {
        String result = tokenise("<!DOCTYPE html PUBLIC \"abc\" invalid>");
        assertTrue("Invalid in between doctype identifiers should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemKeywordWithEOF() {
        String result = tokenise("<!DOCTYPE html SYSTEM");
        assertTrue("EOF in after doctype system keyword should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemKeywordWithInvalid() {
        String result = tokenise("<!DOCTYPE html SYSTEM invalid>");
        assertTrue("Invalid in after doctype system keyword should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypeSystemIdentifierWithEOF() {
        String result = tokenise("<!DOCTYPE html SYSTEM ");
        assertTrue("EOF in before doctype system identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testBeforeDoctypeSystemIdentifierWithInvalid() {
        String result = tokenise("<!DOCTYPE html SYSTEM invalid>");
        assertTrue("Invalid in before doctype system identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierDoubleQuotedWithNull() {
        String result = tokenise("<!DOCTYPE html SYSTEM \"a\u0000b\">");
        assertTrue("Null in doctype system identifier should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierDoubleQuotedWithEOF() {
        String result = tokenise("<!DOCTYPE html SYSTEM \"abc");
        assertTrue("EOF in doctype system identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierSingleQuotedWithNull() {
        String result = tokenise("<!DOCTYPE html SYSTEM 'a\u0000b'>");
        assertTrue("Null in doctype system identifier should be replaced",
                   result.contains("\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierSingleQuotedWithEOF() {
        String result = tokenise("<!DOCTYPE html SYSTEM 'abc");
        assertTrue("EOF in doctype system identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemIdentifierWithEOF() {
        String result = tokenise("<!DOCTYPE html SYSTEM \"abc\"");
        assertTrue("EOF in after doctype system identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemIdentifierWithInvalid() {
        String result = tokenise("<!DOCTYPE html SYSTEM \"abc\" invalid>");
        assertTrue("Invalid in after doctype system identifier should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testBogusDoctypeWithEOF() {
        String result = tokenise("<!DOCTYPE html invalid");
        assertTrue("EOF in bogus doctype should be handled",
                   result.contains("<!DOCTYPE html"));
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        String result = tokenise("<![CDATA[some data]]>");
        assertTrue("CDATA section should be handled",
                   result.contains("some data"));
    }

    @Test(timeout = 4000)
    public void testCdataSectionWithoutClose() {
        String result = tokenise("<![CDATA[some data");
        assertTrue("CDATA without close should be handled",
                   result.contains("some data"));
    }

    // ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testTokeniserStateValues() {
        // Verify all enum values exist
        assertNotNull(TokeniserState.valueOf("Data"));
        assertNotNull(TokeniserState.valueOf("Rcdata"));
        assertNotNull(TokeniserState.valueOf("Rawtext"));
        assertNotNull(TokeniserState.valueOf("ScriptData"));
        assertNotNull(TokeniserState.valueOf("PLAINTEXT"));
        assertNotNull(TokeniserState.valueOf("TagOpen"));
        assertNotNull(TokeniserState.valueOf("EndTagOpen"));
        assertNotNull(TokeniserState.valueOf("TagName"));
        assertNotNull(TokeniserState.valueOf("BeforeAttributeName"));
        assertNotNull(TokeniserState.valueOf("AttributeName"));
        assertNotNull(TokeniserState.valueOf("AfterAttributeName"));
        assertNotNull(TokeniserState.valueOf("BeforeAttributeValue"));
        assertNotNull(TokeniserState.valueOf("AttributeValue_doubleQuoted"));
        assertNotNull(TokeniserState.valueOf("AttributeValue_singleQuoted"));
        assertNotNull(TokeniserState.valueOf("AttributeValue_unquoted"));
        assertNotNull(TokeniserState.valueOf("AfterAttributeValue_quoted"));
        assertNotNull(TokeniserState.valueOf("SelfClosingStartTag"));
        assertNotNull(TokeniserState.valueOf("BogusComment"));
        assertNotNull(TokeniserState.valueOf("MarkupDeclarationOpen"));
        assertNotNull(TokeniserState.valueOf("CommentStart"));
        assertNotNull(TokeniserState.valueOf("CommentStartDash"));
        assertNotNull(TokeniserState.valueOf("Comment"));
        assertNotNull(TokeniserState.valueOf("CommentEndDash"));
        assertNotNull(TokeniserState.valueOf("CommentEnd"));
        assertNotNull(TokeniserState.valueOf("CommentEndBang"));
        assertNotNull(TokeniserState.valueOf("Doctype"));
        assertNotNull(TokeniserState.valueOf("BeforeDoctypeName"));
        assertNotNull(TokeniserState.valueOf("DoctypeName"));
        assertNotNull(TokeniserState.valueOf("AfterDoctypeName"));
        assertNotNull(TokeniserState.valueOf("AfterDoctypePublicKeyword"));
        assertNotNull(TokeniserState.valueOf("BeforeDoctypePublicIdentifier"));
        assertNotNull(TokeniserState.valueOf("DoctypePublicIdentifier_doubleQuoted"));
        assertNotNull(TokeniserState.valueOf("DoctypePublicIdentifier_singleQuoted"));
        assertNotNull(TokeniserState.valueOf("AfterDoctypePublicIdentifier"));
        assertNotNull(TokeniserState.valueOf("BetweenDoctypePublicAndSystemIdentifiers"));
        assertNotNull(TokeniserState.valueOf("AfterDoctypeSystemKeyword"));
        assertNotNull(TokeniserState.valueOf("BeforeDoctypeSystemIdentifier"));
        assertNotNull(TokeniserState.valueOf("DoctypeSystemIdentifier_doubleQuoted"));
        assertNotNull(TokeniserState.valueOf("DoctypeSystemIdentifier_singleQuoted"));
        assertNotNull(TokeniserState.valueOf("AfterDoctypeSystemIdentifier"));
        assertNotNull(TokeniserState.valueOf("BogusDoctype"));
        assertNotNull(TokeniserState.valueOf("CdataSection"));
    }

    @Test(timeout = 4000)
    public void testTokeniserStateCount() {
        // Verify the expected number of states
        assertEquals(52, TokeniserState.values().length);
    }

    @Test(timeout = 4000)
    public void testTokeniserStateOrder() {
        // Verify first and last states
        assertEquals(TokeniserState.Data, TokeniserState.values()[0]);
        assertEquals(TokeniserState.CdataSection, TokeniserState.values()[TokeniserState.values().length - 1]);
    }

    @Test(timeout = 4000)
    public void testTokeniserStateName() {
        assertEquals("Data", TokeniserState.Data.name());
        assertEquals("CdataSection", TokeniserState.CdataSection.name());
    }

    @Test(timeout = 4000)
    public void testTokeniserStateOrdinal() {
        assertEquals(0, TokeniserState.Data.ordinal());
        assertEquals(51, TokeniserState.CdataSection.ordinal());
    }

    @Test(timeout = 4000)
    public void testTokeniserStateToString() {
        assertEquals("Data", TokeniserState.Data.toString());
        assertEquals("CdataSection", TokeniserState.CdataSection.toString());
    }

    @Test(timeout = 4000)
    public void testTokeniserStateEquals() {
        assertEquals(TokeniserState.Data, TokeniserState.Data);
        assertNotEquals(TokeniserState.Data, TokeniserState.Rcdata);
    }

    @Test(timeout = 4000)
    public void testTokeniserStateHashCode() {
        assertEquals(TokeniserState.Data.hashCode(), TokeniserState.Data.hashCode());
        assertNotEquals(TokeniserState.Data.hashCode(), TokeniserState.Rcdata.hashCode());
    }

    @Test(timeout = 4000)
    public void testTokeniserStateCompareTo() {
        assertTrue(TokeniserState.Data.compareTo(TokeniserState.Rcdata) < 0);
        assertTrue(TokeniserState.CdataSection.compareTo(TokeniserState.Data) > 0);
        assertEquals(0, TokeniserState.Data.compareTo(TokeniserState.Data));
    }

    @Test(timeout = 4000)
    public void testTokeniserStateGetDeclaringClass() {
        assertEquals(TokeniserState.class, TokeniserState.Data.getDeclaringClass());
    }

    @Test(timeout = 4000)
    public void testTokeniserStateIsEnum() {
        assertTrue(TokeniserState.Data instanceof Enum);
    }
}