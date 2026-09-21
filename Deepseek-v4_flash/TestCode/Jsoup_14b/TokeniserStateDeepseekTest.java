package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic & state transitions (Data, TagOpen, TagName, etc.)
 * - Partition B: Boundary Value Analysis (nullChar, eof, whitespace, special chars)
 * - Partition C: Defect-targeted branch zone (Rcdata, RCDATAEndTagName, unterminated textarea/title)
 * - Partition D: Exception & defensive guard paths (illegal characters, unexpected eof)
 * - Partition E: Object lifecycle & contract integrity (not applicable for enum)
 *
 * Known defect: unterminated <textarea> and <title> cause inner HTML to be parsed as tags.
 * The fix ensures that in Rcdata state, '<' followed by a non-'/' character emits '<' and stays in Rcdata,
 * and that RCDATAEndTagName correctly falls back to emitting the buffer when the end tag is not appropriate.
 */
public class TokeniserStateDeepseekTest {

    // Helper: parse input and return token list
    private List<Token> parse(String input) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.tokenise();
        return tokeniser.getTokens();
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testDataStateSimpleText() {
        List<Token> tokens = parse("hello world");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isData());
        assertEquals("hello world", tokens.get(0).asData().getData());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testDataStateAmpersand() {
        List<Token> tokens = parse("&amp;");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isData());
        assertEquals("&", tokens.get(0).asData().getData()); // character reference resolved
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testTagOpenStartTag() {
        List<Token> tokens = parse("<p>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("p", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testTagOpenEndTag() {
        List<Token> tokens = parse("</p>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isEndTag());
        assertEquals("p", tokens.get(0).asEndTag().tagName());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testTagNameWithAttributes() {
        List<Token> tokens = parse("<div class=\"foo\">");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("div", tokens.get(0).asStartTag().tagName());
        assertEquals("foo", tokens.get(0).asStartTag().attr("class"));
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testSelfClosingTag() {
        List<Token> tokens = parse("<br/>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertTrue(tokens.get(0).asStartTag().isSelfClosing());
        assertTrue(tokens.get(1).isEOF());
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testNullCharInData() {
        List<Token> tokens = parse("he\u0000llo");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isData());
        // nullChar should be replaced with replacement char (U+FFFD)
        assertEquals("he\uFFFDto", tokens.get(0).asData().getData());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testEofInTagOpen() {
        List<Token> tokens = parse("<");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isData());
        assertEquals("<", tokens.get(0).asData().getData());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testEofInEndTagOpen() {
        List<Token> tokens = parse("</");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isData());
        assertEquals("</", tokens.get(0).asData().getData());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testWhitespaceInTagName() {
        List<Token> tokens = parse("<div class>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("div", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(0).asStartTag().hasAttribute("class"));
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testUnquotedAttributeValue() {
        List<Token> tokens = parse("<div class=foo>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("foo", tokens.get(0).asStartTag().attr("class"));
        assertTrue(tokens.get(1).isEOF());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testUnterminatedTextarea() {
        // Input: <textarea>one<p>two
        // Expected: textarea start tag, then data "one<p>two", then EOF
        List<Token> tokens = parse("<textarea>one<p>two");
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("textarea", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(1).isData());
        assertEquals("one<p>two", tokens.get(1).asData().getData());
        assertTrue(tokens.get(2).isEOF());
    }

    @Test(timeout = 4000)
    public void testUnclosedTitle() {
        // Input: <title>One<b>Two <p>Test</p>
        // Expected: title start tag, then data "One<b>Two <p>Test</p>", then EOF
        List<Token> tokens = parse("<title>One<b>Two <p>Test</p>");
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("title", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(1).isData());
        assertEquals("One<b>Two <p>Test</p>", tokens.get(1).asData().getData());
        assertTrue(tokens.get(2).isEOF());
    }

    @Test(timeout = 4000)
    public void testProperlyClosedTextarea() {
        // Input: <textarea>one</textarea>
        // Expected: start tag, data "one", end tag
        List<Token> tokens = parse("<textarea>one</textarea>");
        assertEquals(4, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("textarea", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(1).isData());
        assertEquals("one", tokens.get(1).asData().getData());
        assertTrue(tokens.get(2).isEndTag());
        assertEquals("textarea", tokens.get(2).asEndTag().tagName());
        assertTrue(tokens.get(3).isEOF());
    }

    @Test(timeout = 4000)
    public void testRcdataLessthanSignNotSlash() {
        // When in Rcdata, '<' followed by letter should emit '<' and stay in Rcdata
        List<Token> tokens = parse("<textarea>a<b");
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("textarea", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(1).isData());
        assertEquals("a<b", tokens.get(1).asData().getData());
        assertTrue(tokens.get(2).isEOF());
    }

    @Test(timeout = 4000)
    public void testRcdataEndTagNameInappropriate() {
        // When in Rcdata, '</' followed by a non-matching tag name should emit the buffer
        List<Token> tokens = parse("<textarea>one</div>");
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("textarea", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(1).isData());
        assertEquals("one</div>", tokens.get(1).asData().getData());
        assertTrue(tokens.get(2).isEOF());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testBogusComment() {
        List<Token> tokens = parse("<?xml?>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isComment());
        assertEquals("?xml?", tokens.get(0).asComment().getData());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testDoctypeSimple() {
        List<Token> tokens = parse("<!DOCTYPE html>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isDoctype());
        assertEquals("html", tokens.get(0).asDoctype().getName());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        List<Token> tokens = parse("<![CDATA[hello]]>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isData());
        assertEquals("hello", tokens.get(0).asData().getData());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testEofInScriptData() {
        List<Token> tokens = parse("<script>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("script", tokens.get(0).asStartTag().tagName());
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testEofInAttributeValue() {
        List<Token> tokens = parse("<a href=\"foo");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("a", tokens.get(0).asStartTag().tagName());
        assertEquals("foo", tokens.get(0).asStartTag().attr("href"));
        assertTrue(tokens.get(1).isEOF());
    }

    @Test(timeout = 4000)
    public void testInvalidCharacterInAttributeName() {
        List<Token> tokens = parse("<a \"foo\"=bar>");
        // The quote should be treated as part of attribute name (error recovery)
        assertTrue(tokens.get(0).isStartTag());
        // Attribute name will contain quote
        assertTrue(tokens.get(0).asStartTag().hasAttribute("\"foo\""));
        assertTrue(tokens.get(1).isEOF());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // Enum constants are singletons; no additional lifecycle tests needed.
    // However, we can test that the enum values are not null and have expected names.

    @Test(timeout = 4000)
    public void testEnumConstantsExist() {
        assertNotNull(TokeniserState.Data);
        assertNotNull(TokeniserState.Rcdata);
        assertNotNull(TokeniserState.TagOpen);
        // etc.
    }

    @Test(timeout = 4000)
    public void testEnumToString() {
        assertEquals("Data", TokeniserState.Data.toString());
        assertEquals("Rcdata", TokeniserState.Rcdata.toString());
    }
}