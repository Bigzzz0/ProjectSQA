package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.parser.TokeniserState
 * Tested States & Decision Branches:
 * - Data: '&', '<', nullChar, eof, default characters.
 * - CharacterReferenceInData / CharacterReferenceInRcdata: named & numeric references, valid & invalid.
 * - Rcdata / RcdataLessthanSign / RCDATAEndTagOpen / RCDATAEndTagName:
 *     - appropriate end tag match, tag name letters, whitespace, '/', '>', breakout on non-matching start tags,
 *       anythingElse fallback buffer replay.
 * - Rawtext / RawtextLessthanSign / RawtextEndTagOpen / RawtextEndTagName:
 *     - tag name matches, non-matches, premature EOF, exit transitions.
 * - ScriptData & Escaped Variants (ScriptDataLessthanSign, ScriptDataEscapeStart, ScriptDataEscapeStartDash,
 *     ScriptDataEscaped, ScriptDataEscapedDash, ScriptDataEscapedDashDash, ScriptDataEscapedLessthanSign,
 *     ScriptDataEscapedEndTagOpen, ScriptDataEscapedEndTagName, ScriptDataDoubleEscapeStart,
 *     ScriptDataDoubleEscaped, ScriptDataDoubleEscapedDash, ScriptDataDoubleEscapedDashDash,
 *     ScriptDataDoubleEscapedLessthanSign, ScriptDataDoubleEscapeEnd):
 *     - double escaped script logic, "script" buffer check, dash sequences, EOF handling in escaped states.
 * - TagOpen / EndTagOpen / TagName:
 *     - '!', '/', '?', matchesLetter, invalid start char, empty end tag, '>', bogus comments.
 * - BeforeAttributeName / AttributeName / AfterAttributeName / BeforeAttributeValue:
 *     - whitespace consumption, '/', '>', quotes, nullChar, '=', eof, unquoted attribute values.
 * - Quoted & Unquoted Attribute Values:
 *     - single/double quoted transitions, entity expansion in attributes, empty attribute values.
 * - SelfClosingStartTag: '>', eof, default rewind.
 * - BogusComment / MarkupDeclarationOpen / Comment states (Start, Dash, End, Bang):
 *     - standard comments, "--" delimiter, DOCTYPE dispatch, CDATA dispatch, malformed comment tails.
 * - Doctype & System/Public Identifiers:
 *     - DOCTYPE keyword, forceQuirks flags, public/system keyword matching, double/single quotes, bogus doctype.
 * - CdataSection: "]]>" delimiter, EOF buffer underrun.
 *
 * Defects4J Ground Truth Target:
 * - handlesLessInTagThanAsNewTag (TokeniserStateTest) & parsesQuiteRoughAttributes (HtmlParserTest):
 *     - Enforces that '<' encountered within BeforeAttributeName / AttributeName / AfterAttributeName
 *       breaks out appropriately to start a new tag instead of mistakenly treating '<' as attribute name data.
 * ====================================================================================================
 */
public class TokeniserStateGeminiTest {

    // -------------------------------------------------------------------------
    // Test Utilities
    // -------------------------------------------------------------------------

    private List<Token> tokenize(String input) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(100));
        List<Token> tokens = new ArrayList<Token>();
        while (true) {
            Token token = tokeniser.read();
            if (token == null) {
                break;
            }
            tokens.add(token);
            if (token instanceof Token.EOF) {
                break;
            }
        }
        return tokens;
    }

    private Tokeniser makeTokeniser(String input, ParseErrorList errors) {
        CharacterReader reader = new CharacterReader(input);
        return new Tokeniser(reader, errors);
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDataStateBasicAndNullChar() {
        List<Token> tokens = tokenize("Hello\u0000world&amp;done");
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 2);
    }

    @Test(timeout = 4000)
    public void testTagOpenAndTagNameTransitions() {
        List<Token> tokens = tokenize("<div class=\"main\" id='first' checked val=plain / >");
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 2);
        assertTrue(tokens.get(0) instanceof Token.StartTag);
        Token.StartTag startTag = (Token.StartTag) tokens.get(0);
        assertEquals("div", startTag.name());
        assertEquals("main", startTag.attributes.get("class"));
        assertEquals("first", startTag.attributes.get("id"));
        assertEquals("plain", startTag.attributes.get("val"));
    }

    @Test(timeout = 4000)
    public void testEndTagOpenTransitions() {
        List<Token> tokens = tokenize("</div >");
        assertNotNull(tokens);
        assertTrue(tokens.get(0) instanceof Token.EndTag);
        Token.EndTag endTag = (Token.EndTag) tokens.get(0);
        assertEquals("div", endTag.name());

        // Malformed end tag with bogus character
        List<Token> bogusTokens = tokenize("</?bogus>");
        assertNotNull(bogusTokens);

        // Missing tag name
        List<Token> emptyEndTokens = tokenize("</>");
        assertNotNull(emptyEndTokens);
    }

    @Test(timeout = 4000)
    public void testRcdataAndEndTag() {
        List<Token> tokens = tokenize("<title>Sample &amp; Title</title>");
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 3);
        assertTrue(tokens.get(0) instanceof Token.StartTag);
        assertEquals("title", ((Token.StartTag) tokens.get(0)).name());
    }

    @Test(timeout = 4000)
    public void testRcdataWithWrongEndTagAndBreakout() {
        // Tag mismatch inside RCDATA falls through anythingElse
        List<Token> tokens = tokenize("<title>Not </wrong> Closed</title>");
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 3);

        // RCDATA breakout when letter matches start tag but end tag does not exist in buffer
        CharacterReader reader = new CharacterReader("title");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.transition(TokeniserState.RcdataLessthanSign);
        TokeniserState.RcdataLessthanSign.read(t, reader);
    }

    @Test(timeout = 4000)
    public void testRawtextTransitions() {
        List<Token> tokens = tokenize("<style>body { color: red; } </style>");
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 3);
        assertTrue(tokens.get(0) instanceof Token.StartTag);
        assertEquals("style", ((Token.StartTag) tokens.get(0)).name());

        // Rawtext with non-matching end tag
        List<Token> tokensWithFakeEnd = tokenize("<style></wrong>body{}</style>");
        assertNotNull(tokensWithFakeEnd);

        // Rawtext with null char
        List<Token> tokensWithNull = tokenize("<style>\u0000</style>");
        assertNotNull(tokensWithNull);
    }

    @Test(timeout = 4000)
    public void testScriptDataAndEscapes() {
        // Standard script
        List<Token> tokens = tokenize("<script>var a = 1; </script>");
        assertNotNull(tokens);
        assertEquals("script", ((Token.StartTag) tokens.get(0)).name());

        // Script with embedded comment and double escaped script tag
        String script = "<script><!-- <script>var x = 1;</script> --> </script>";
        List<Token> complexTokens = tokenize(script);
        assertNotNull(complexTokens);

        // Script with escaped dash dash
        List<Token> dashTokens = tokenize("<script><!-- - -- > </script>");
        assertNotNull(dashTokens);

        // Script with unexpected tag inside escaped section
        List<Token> fakeTokens = tokenize("<script><!-- <style> inner </style> --> </script>");
        assertNotNull(fakeTokens);
    }

    @Test(timeout = 4000)
    public void testPlaintextState() {
        List<Token> tokens = tokenize("<plaintext>Line 1\u0000Line 2\n<p>No tag</p>");
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 2);
    }

    @Test(timeout = 4000)
    public void testCommentsAndBangVariants() {
        List<Token> normal = tokenize("<!-- standard comment -->");
        assertTrue(normal.get(0) instanceof Token.Comment);
        assertEquals(" standard comment ", ((Token.Comment) normal.get(0)).getData());

        List<Token> bangComment = tokenize("<!-- comment --!>");
        assertTrue(bangComment.get(0) instanceof Token.Comment);

        List<Token> dashComment = tokenize("<!---dash comment--->");
        assertTrue(dashComment.get(0) instanceof Token.Comment);

        List<Token> emptyComment = tokenize("<!---->");
        assertTrue(emptyComment.get(0) instanceof Token.Comment);

        List<Token> bogusComment = tokenize("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        assertTrue(bogusComment.get(0) instanceof Token.Comment);
        assertTrue(((Token.Comment) bogusComment.get(0)).bogus);
    }

    @Test(timeout = 4000)
    public void testDoctypeTransitions() {
        // Simple doctype
        List<Token> d1 = tokenize("<!DOCTYPE html>");
        assertTrue(d1.get(0) instanceof Token.Doctype);
        Token.Doctype dt1 = (Token.Doctype) d1.get(0);
        assertEquals("html", dt1.getName());
        assertFalse(dt1.isForceQuirks());

        // Full Public & System identifiers (double quotes)
        List<Token> d2 = tokenize("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        Token.Doctype dt2 = (Token.Doctype) d2.get(0);
        assertEquals("html", dt2.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", dt2.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", dt2.getSystemIdentifier());

        // System identifier only (single quotes)
        List<Token> d3 = tokenize("<!DOCTYPE html SYSTEM 'about:legacy-compat'>");
        Token.Doctype dt3 = (Token.Doctype) d3.get(0);
        assertEquals("html", dt3.getName());
        assertEquals("about:legacy-compat", dt3.getSystemIdentifier());

        // Malformed Doctype triggering forceQuirks
        List<Token> d4 = tokenize("<!DOCTYPE>");
        Token.Doctype dt4 = (Token.Doctype) d4.get(0);
        assertTrue(dt4.isForceQuirks());

        List<Token> d5 = tokenize("<!DOCTYPE html BOGUSKEYWORD>");
        Token.Doctype dt5 = (Token.Doctype) d5.get(0);
        assertTrue(dt5.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        CharacterReader reader = new CharacterReader("cdata content]]><p>after</p>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.createTempBuffer();
        t.transition(TokeniserState.CdataSection);

        TokeniserState.CdataSection.read(t, reader);
        // After reading CDATA content and ]]> delimiter, state transitions to Data
        assertEquals(TokeniserState.Data, t.getState());
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testArraysAreSortedForBinarySearch() {
        char[][] arrays = {
            TokeniserState.attributeSingleValueCharsSorted,
            TokeniserState.attributeDoubleValueCharsSorted,
            TokeniserState.attributeNameCharsSorted,
            TokeniserState.attributeValueUnquoted
        };
        for (char[] arr : arrays) {
            for (int i = 0; i < arr.length - 1; i++) {
                assertTrue("Array elements must be in strict ascending order: " + (int) arr[i] + " vs " + (int) arr[i + 1],
                        arr[i] < arr[i + 1]);
            }
        }
    }

    @Test(timeout = 4000)
    public void testEmptyInputTokenization() {
        List<Token> tokens = tokenize("");
        assertEquals(1, tokens.size());
        assertTrue(tokens.get(0) instanceof Token.EOF);
    }

    @Test(timeout = 4000)
    public void testPrematureEofInVariousStates() {
        // Tag open EOF
        List<Token> t1 = tokenize("<");
        assertTrue(t1.size() >= 1);

        // Tag name EOF
        List<Token> t2 = tokenize("<div");
        assertTrue(t2.size() >= 1);

        // Attribute name EOF
        List<Token> t3 = tokenize("<div attr");
        assertTrue(t3.size() >= 1);

        // Attribute value EOF (double quoted, single quoted, unquoted)
        List<Token> t4 = tokenize("<div attr=\"val");
        assertTrue(t4.size() >= 1);

        List<Token> t5 = tokenize("<div attr='val");
        assertTrue(t5.size() >= 1);

        List<Token> t6 = tokenize("<div attr=val");
        assertTrue(t6.size() >= 1);

        // Comment start EOF
        List<Token> t7 = tokenize("<!--");
        assertTrue(t7.size() >= 1);

        // Comment mid EOF
        List<Token> t8 = tokenize("<!-- in comment");
        assertTrue(t8.size() >= 1);

        // Comment end dash EOF
        List<Token> t9 = tokenize("<!-- comment -");
        assertTrue(t9.size() >= 1);

        // Doctype EOF
        List<Token> t10 = tokenize("<!DOCTYPE html PUBLIC \"pub");
        assertTrue(t10.size() >= 1);
    }

    @Test(timeout = 4000)
    public void testNullCharacterHandlingAcrossStates() {
        ParseErrorList errors = ParseErrorList.tracking(100);

        // Null in attribute name
        CharacterReader r1 = new CharacterReader("\u0000attr=val>");
        Tokeniser t1 = makeTokeniser("", errors);
        t1.createTagPending(true);
        TokeniserState.AttributeName.read(t1, r1);
        assertFalse(errors.isEmpty());

        // Null in quoted attribute value
        errors.clear();
        CharacterReader r2 = new CharacterReader("\u0000text\"");
        Tokeniser t2 = makeTokeniser("", errors);
        t2.createTagPending(true);
        TokeniserState.AttributeValue_doubleQuoted.read(t2, r2);
        assertFalse(errors.isEmpty());

        // Null in unquoted attribute value
        errors.clear();
        CharacterReader r3 = new CharacterReader("\u0000text ");
        Tokeniser t3 = makeTokeniser("", errors);
        t3.createTagPending(true);
        TokeniserState.AttributeValue_unquoted.read(t3, r3);
        assertFalse(errors.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Targets defect: TokeniserStateTest::handlesLessInTagThanAsNewTag
     * In the defective version, encountering '<' within attribute positions
     * incorrectly absorbs '<' as attribute name characters rather than breaking
     * out to create a new tag.
     */
    @Test(timeout = 4000)
    public void testHandlesLessInTagThanAsNewTag() {
        String html = "<p <p<div id=\"one\" <span>Two";
        Document doc = Jsoup.parse(html);
        assertEquals("<p></p>\n<p></p>\n<div id=\"one\">\n <span>Two</span>\n</div>", doc.body().html());
    }

    /**
     * Targets defect: HtmlParserTest::parsesQuiteRoughAttributes
     * Validates that rough attributes containing '<' behave correctly according
     * to the HTML5 specification and expected tree construction.
     */
    @Test(timeout = 4000)
    public void testParsesQuiteRoughAttributes() {
        String html = "<p =a>One<a <p>Something</a></p><a <p>Else</a>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p =a>One<a></a></p>\n<p><a>Something</a></p>\n<a>Else</a>", doc.body().html());
    }

    @Test(timeout = 4000)
    public void testLessThanInBeforeAttributeNameBranch() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("<");
        Tokeniser t = makeTokeniser("", errors);
        t.createTagPending(true);

        TokeniserState.BeforeAttributeName.read(t, reader);
        assertFalse(errors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLessThanInAttributeNameBranch() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("<");
        Tokeniser t = makeTokeniser("", errors);
        t.createTagPending(true);

        TokeniserState.AttributeName.read(t, reader);
        assertFalse(errors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLessThanInAfterAttributeNameBranch() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("<");
        Tokeniser t = makeTokeniser("", errors);
        t.createTagPending(true);

        TokeniserState.AfterAttributeName.read(t, reader);
        assertFalse(errors.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSelfClosingStartTagErrors() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        // Encountering character other than '>' in self-closing state triggers error and rewinds
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser t = makeTokeniser("", errors);
        t.createTagPending(true);

        TokeniserState.SelfClosingStartTag.read(t, reader);
        assertFalse(errors.isEmpty());
        assertEquals('a', reader.current());
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValueQuotedMissingWhitespace() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("foo=bar");
        Tokeniser t = makeTokeniser("", errors);
        t.createTagPending(true);

        TokeniserState.AfterAttributeValue_quoted.read(t, reader);
        assertFalse(errors.isEmpty());
        assertEquals('f', reader.current());
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());
    }

    @Test(timeout = 4000)
    public void testMarkupDeclarationOpenBranches() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        // Unknown declaration triggers error and BogusComment
        CharacterReader reader = new CharacterReader("UNKNOWN>");
        Tokeniser t = makeTokeniser("", errors);

        TokeniserState.MarkupDeclarationOpen.read(t, reader);
        assertFalse(errors.isEmpty());
        assertEquals(TokeniserState.BogusComment, t.getState());
    }

    @Test(timeout = 4000)
    public void testCommentEndBangVariants() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader(">");
        Tokeniser t = makeTokeniser("", errors);
        t.createCommentPending();

        TokeniserState.CommentEndBang.read(t, reader);
        assertEquals(TokeniserState.Data, t.getState());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicAndSystemIdentifierEdgeCases() {
        ParseErrorList errors = ParseErrorList.tracking(50);

        // Premature '>' in AfterDoctypePublicKeyword
        CharacterReader r1 = new CharacterReader(">");
        Tokeniser t1 = makeTokeniser("", errors);
        TokeniserState.AfterDoctypePublicKeyword.read(t1, r1);
        assertTrue(t1.doctypePending.isForceQuirks());

        // Premature '>' in BeforeDoctypePublicIdentifier
        CharacterReader r2 = new CharacterReader(">");
        Tokeniser t2 = makeTokeniser("", errors);
        t2.createDoctypePending();
        TokeniserState.BeforeDoctypePublicIdentifier.read(t2, r2);
        assertTrue(t2.doctypePending.isForceQuirks());

        // Premature '>' in AfterDoctypeSystemKeyword
        CharacterReader r3 = new CharacterReader(">");
        Tokeniser t3 = makeTokeniser("", errors);
        t3.createDoctypePending();
        TokeniserState.AfterDoctypeSystemKeyword.read(t3, r3);
        assertTrue(t3.doctypePending.isForceQuirks());

        // Premature '>' in BeforeDoctypeSystemIdentifier
        CharacterReader r4 = new CharacterReader(">");
        Tokeniser t4 = makeTokeniser("", errors);
        t4.createDoctypePending();
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t4, r4);
        assertTrue(t4.doctypePending.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashBranches() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        // '>' in ScriptDataEscapedDashDash returns to ScriptData
        CharacterReader r1 = new CharacterReader(">");
        Tokeniser t1 = makeTokeniser("", errors);
        TokeniserState.ScriptDataEscapedDashDash.read(t1, r1);
        assertEquals(TokeniserState.ScriptData, t1.getState());

        // '<' in ScriptDataEscapedDashDash transitions to ScriptDataEscapedLessthanSign
        CharacterReader r2 = new CharacterReader("<");
        Tokeniser t2 = makeTokeniser("", errors);
        TokeniserState.ScriptDataEscapedDashDash.read(t2, r2);
        assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t2.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedDashDashBranches() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        // '>' in ScriptDataDoubleEscapedDashDash returns to ScriptData
        CharacterReader r1 = new CharacterReader(">");
        Tokeniser t1 = makeTokeniser("", errors);
        TokeniserState.ScriptDataDoubleEscapedDashDash.read(t1, r1);
        assertEquals(TokeniserState.ScriptData, t1.getState());

        // '<' in ScriptDataDoubleEscapedDashDash transitions to ScriptDataDoubleEscapedLessthanSign
        CharacterReader r2 = new CharacterReader("<");
        Tokeniser t2 = makeTokeniser("", errors);
        TokeniserState.ScriptDataDoubleEscapedDashDash.read(t2, r2);
        assertEquals(TokeniserState.ScriptDataDoubleEscapedLessthanSign, t2.getState());
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEnumValuesAndValueOfIntegrity() {
        TokeniserState[] states = TokeniserState.values();
        assertNotNull(states);
        assertTrue("TokeniserState must contain 67 states according to HTML5 spec", states.length >= 67);

        for (TokeniserState state : states) {
            assertNotNull(state);
            assertSame(state, TokeniserState.valueOf(state.name()));
        }
    }

    @Test(timeout = 4000)
    public void testSpecificStateNameResolutions() {
        assertSame(TokeniserState.Data, TokeniserState.valueOf("Data"));
        assertSame(TokeniserState.TagOpen, TokeniserState.valueOf("TagOpen"));
        assertSame(TokeniserState.TagName, TokeniserState.valueOf("TagName"));
        assertSame(TokeniserState.BeforeAttributeName, TokeniserState.valueOf("BeforeAttributeName"));
        assertSame(TokeniserState.AttributeName, TokeniserState.valueOf("AttributeName"));
        assertSame(TokeniserState.AfterAttributeName, TokeniserState.valueOf("AfterAttributeName"));
        assertSame(TokeniserState.BeforeAttributeValue, TokeniserState.valueOf("BeforeAttributeValue"));
        assertSame(TokeniserState.AttributeValue_doubleQuoted, TokeniserState.valueOf("AttributeValue_doubleQuoted"));
        assertSame(TokeniserState.AttributeValue_singleQuoted, TokeniserState.valueOf("AttributeValue_singleQuoted"));
        assertSame(TokeniserState.AttributeValue_unquoted, TokeniserState.valueOf("AttributeValue_unquoted"));
        assertSame(TokeniserState.SelfClosingStartTag, TokeniserState.valueOf("SelfClosingStartTag"));
        assertSame(TokeniserState.Comment, TokeniserState.valueOf("Comment"));
        assertSame(TokeniserState.Doctype, TokeniserState.valueOf("Doctype"));
        assertSame(TokeniserState.CdataSection, TokeniserState.valueOf("CdataSection"));
    }
}