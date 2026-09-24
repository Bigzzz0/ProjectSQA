package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target: org.jsoup.parser.TokeniserState
 *
 * Defects4J Target Defect:
 * - AttributeParseTest::dropsSlashFromAttributeName:
 *   In TokeniserState.SelfClosingStartTag, when encountering a character other than '>' or EOF,
 *   the character was consumed by `r.consume()` but never unconsumed before transitioning to
 *   BeforeAttributeName. This caused the first character of the next attribute name to be lost
 *   (e.g., `<img /foo=bar>` parsed the attribute name as `oo` instead of `foo`).
 *
 * Decision / Branch Matrix:
 * 1. Data:
 *    - '&' -> advance to CharacterReferenceInData
 *    - '<' -> advance to TagOpen
 *    - nullChar ('\u0000') -> error & emit consumed null char
 *    - eof -> emit EOF token
 *    - default -> emit consumed data
 * 2. CharacterReferenceInData:
 *    - readCharRef(t, Data) -> consumed character reference resolution or '&'
 * 3. Rcdata:
 *    - '&', '<', nullChar (error, advance, emit replacementChar), eof, default
 * 4. CharacterReferenceInRcdata:
 *    - readCharRef(t, Rcdata)
 * 5. Rawtext:
 *    - '<' -> RawtextLessthanSign, nullChar, eof, default
 * 6. ScriptData:
 *    - '<' -> ScriptDataLessthanSign, nullChar, eof, default
 * 7. PLAINTEXT:
 *    - nullChar (error, replacementChar), eof, default consumeTo nullChar
 * 8. TagOpen:
 *    - '!' -> MarkupDeclarationOpen, '/' -> EndTagOpen, '?' -> BogusComment, letter -> TagName, default -> error, emit '<'
 * 9. EndTagOpen:
 *    - empty -> eofError, emit "</", Data
 *    - letter -> TagName
 *    - '>' -> error, Data
 *    - default -> error, BogusComment
 * 10. TagName:
 *    - whitespace -> BeforeAttributeName
 *    - '/' -> SelfClosingStartTag
 *    - '>' -> emitTagPending, Data
 *    - nullChar -> append replacementStr
 *    - eof -> eofError, Data
 * 11. RcdataLessthanSign & RCDATAEndTagOpen & RCDATAEndTagName:
 *    - '/' -> RCDATAEndTagOpen
 *    - letter with non-matching end tag / matching end tag / anythingElse
 * 12. RawtextLessthanSign & RawtextEndTagOpen & RawtextEndTagName:
 *    - readEndTag, handleDataEndTag transitions
 * 13. ScriptDataLessthanSign, ScriptDataEscapeStart, ScriptDataEscapeStartDash, ScriptDataEscaped,
 *     ScriptDataEscapedDash, ScriptDataEscapedDashDash, ScriptDataEscapedLessthanSign,
 *     ScriptDataEscapedEndTagOpen, ScriptDataEscapedEndTagName, ScriptDataDoubleEscapeStart,
 *     ScriptDataDoubleEscaped, ScriptDataDoubleEscapedDash, ScriptDataDoubleEscapedDashDash,
 *     ScriptDataDoubleEscapedLessthanSign, ScriptDataDoubleEscapeEnd:
 *    - Full script data escaping state machine coverage
 * 14. BeforeAttributeName, AttributeName, AfterAttributeName:
 *    - whitespace, '/', '>', nullChar, eof, quote / equals / bogus chars, default
 * 15. BeforeAttributeValue, AttributeValue_doubleQuoted, AttributeValue_singleQuoted,
 *     AttributeValue_unquoted, AfterAttributeValue_quoted:
 *    - quoting variants, character reference handling inside attributes, eof, nullChar
 * 16. SelfClosingStartTag:
 *    - '>' -> selfClosing = true, emit, Data
 *    - eof -> eofError, Data
 *    - default -> error, unconsume (defect location!), transition BeforeAttributeName
 * 17. BogusComment, MarkupDeclarationOpen (DOCTYPE, CDATA, comment --, bogus default):
 * 18. CommentStart, CommentStartDash, Comment, CommentEndDash, CommentEnd, CommentEndBang:
 *    - Dash transitions, nullChar replacement, bang handling, eof
 * 19. Doctype & DOCTYPE public/system identifier variants:
 *    - BeforeDoctypeName, DoctypeName, AfterDoctypeName, PUBLIC/SYSTEM keyword branches,
 *      quoted identifiers, Between identifiers, BogusDoctype, quirks mode triggering
 * 20. CdataSection:
 *    - consumeTo("]]>"), emit, matchConsume("]]>"), Data
 */
public class TokeniserStateGeminiTest {

    private Tokeniser createTokeniser(String html) {
        CharacterReader reader = new CharacterReader(html);
        ParseErrorList errors = ParseErrorList.tracking(100);
        return new Tokeniser(reader, errors);
    }

    private Token readNextToken(Tokeniser t) {
        return t.read();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Target Defect: SelfClosingStartTag drops the first character of attribute name
     * when encountering non-'>' and non-EOF.
     * E.g. `<img /foo="bar">` or `<input /value='foo'/>`
     * In the buggy code, `r.consume()` ate 'f' without `r.unconsume()`, so attribute name became 'oo'.
     */
    @Test(timeout = 4000)
    public void testSelfClosingStartTagPreservesAttributeNameCharacter() {
        Tokeniser t = createTokeniser("<img /foo='bar'>");
        Token token = readNextToken(t);
        assertTrue("Token must be a StartTag", token instanceof Token.StartTag);
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals("img", tag.name());
        assertTrue("SelfClosingStartTag must not drop the 'f' from 'foo'", tag.attributes.hasKey("foo"));
        assertEquals("bar", tag.attributes.get("foo"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagMultipleSlashesAndAttributes() {
        Tokeniser t = createTokeniser("<a /href='url' /id='test'/>");
        Token token = readNextToken(t);
        assertTrue(token instanceof Token.StartTag);
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals("a", tag.name());
        assertTrue("href attribute preserved", tag.attributes.hasKey("href"));
        assertEquals("url", tag.attributes.get("href"));
        assertTrue("id attribute preserved", tag.attributes.hasKey("id"));
        assertEquals("test", tag.attributes.get("id"));
        assertTrue("Should be self closing", tag.isSelfClosing());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDataStateBasicAndNullChar() {
        Tokeniser t = createTokeniser("Hello\u0000World&amp;");
        Token.Character c1 = (Token.Character) readNextToken(t);
        assertEquals("Hello", c1.getData());

        Token.Character c2 = (Token.Character) readNextToken(t);
        assertEquals("\u0000", c2.getData()); // In Data, nullChar emits literal nullChar

        Token.Character c3 = (Token.Character) readNextToken(t);
        assertEquals("World", c3.getData());

        Token.Character c4 = (Token.Character) readNextToken(t);
        assertEquals("&", c4.getData());
    }

    @Test(timeout = 4000)
    public void testDataStateEof() {
        Tokeniser t = createTokeniser("");
        Token token = readNextToken(t);
        assertTrue(token instanceof Token.EOF);
    }

    @Test(timeout = 4000)
    public void testTagOpenStates() {
        // Tag open with letter
        Tokeniser t1 = createTokeniser("<div >");
        Token.StartTag tag1 = (Token.StartTag) readNextToken(t1);
        assertEquals("div", tag1.name());

        // Tag open with '?' -> BogusComment
        Tokeniser t2 = createTokeniser("<?bogus comment>text");
        Token.Comment comment = (Token.Comment) readNextToken(t2);
        assertTrue(comment.bogus);
        assertEquals("?bogus comment", comment.getData());

        // Tag open with invalid char -> error and emit '<'
        Tokeniser t3 = createTokeniser("< 123");
        Token.Character charTok = (Token.Character) readNextToken(t3);
        assertEquals("<", charTok.getData());
    }

    @Test(timeout = 4000)
    public void testEndTagOpenVariants() {
        // Normal end tag
        Tokeniser t1 = createTokeniser("</div >");
        Token.EndTag endTag = (Token.EndTag) readNextToken(t1);
        assertEquals("div", endTag.name());

        // Empty end tag: </ at EOF
        Tokeniser t2 = createTokeniser("</");
        Token.Character c2 = (Token.Character) readNextToken(t2);
        assertEquals("</", c2.getData());

        // End tag with immediately '>' -> error
        Tokeniser t3 = createTokeniser("</>");
        Token token3 = readNextToken(t3);
        assertTrue(token3 instanceof Token.EOF);

        // End tag with non-letter non-bracket -> BogusComment
        Tokeniser t4 = createTokeniser("</%bogus>");
        Token.Comment c4 = (Token.Comment) readNextToken(t4);
        assertTrue(c4.bogus);
        assertEquals("/%bogus", c4.getData());
    }

    @Test(timeout = 4000)
    public void testTagNameVariants() {
        // Null char inside tag name
        Tokeniser t = createTokeniser("<di\u0000v>");
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertEquals("di\uFFFDv", tag.name());

        // EOF inside tag name
        Tokeniser t2 = createTokeniser("<div");
        Token token2 = readNextToken(t2);
        assertTrue(token2 instanceof Token.EOF);
    }

    @Test(timeout = 4000)
    public void testAttributeStatesAndQuotes() {
        // Double quotes, single quotes, unquoted, empty value
        String html = "<a b=\"val1\" c='val2' d=val3 e empty=\"\" f= >";
        Tokeniser t = createTokeniser(html);
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertEquals("a", tag.name());
        assertEquals("val1", tag.attributes.get("b"));
        assertEquals("val2", tag.attributes.get("c"));
        assertEquals("val3", tag.attributes.get("d"));
        assertEquals("", tag.attributes.get("e"));
        assertEquals("", tag.attributes.get("empty"));
        assertTrue(tag.attributes.hasKey("f"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueEntityReferences() {
        String html = "<a href=\"&lt;&gt;&amp;&quot;&apos;\" title='&copy;'>";
        Tokeniser t = createTokeniser(html);
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertEquals("<>&\"'", tag.attributes.get("href"));
        assertEquals("\u00a9", tag.attributes.get("title"));
    }

    @Test(timeout = 4000)
    public void testAttributeNullChars() {
        String html = "<a foo\u0000bar=\"val\u0000ue\" single='val\u0000ue' unq=val\u0000ue>";
        Tokeniser t = createTokeniser(html);
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertTrue(tag.attributes.hasKey("foo\uFFFDbar"));
        assertEquals("val\uFFFDue", tag.attributes.get("foo\uFFFDbar"));
        assertEquals("val\uFFFDue", tag.attributes.get("single"));
        assertEquals("val\uFFFDue", tag.attributes.get("unq"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeNameSpecialChars() {
        // Characters: '"', '\'', '<', '=' in BeforeAttributeName
        Tokeniser t = createTokeniser("<a \"val\" 'foo' <tag> =bar>");
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertEquals("a", tag.name());
        assertTrue(tag.attributes.hasKey("\"val\""));
        assertTrue(tag.attributes.hasKey("'foo'"));
        assertTrue(tag.attributes.hasKey("<tag"));
        assertTrue(tag.attributes.hasKey("=bar"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeValueSpecialChars() {
        Tokeniser t = createTokeniser("<a a=< b=> c=` d=>");
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertEquals("a", tag.name());
        assertEquals("<", tag.attributes.get("a"));
        assertEquals("", tag.attributes.get("b"));
        assertEquals("`", tag.attributes.get("c"));
    }

    @Test(timeout = 4000)
    public void testAfterAttributeNameBranches() {
        Tokeniser t = createTokeniser("<a foo / >");
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertEquals("a", tag.name());
        assertTrue(tag.isSelfClosing());

        Tokeniser t2 = createTokeniser("<a foo\u0000 >");
        Token.StartTag tag2 = (Token.StartTag) readNextToken(t2);
        assertTrue(tag2.attributes.hasKey("foo\uFFFD"));
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValueQuotedBranches() {
        // AfterAttributeValue_quoted followed by slash
        Tokeniser t1 = createTokeniser("<a href=\"link\"/>");
        Token.StartTag tag1 = (Token.StartTag) readNextToken(t1);
        assertTrue(tag1.isSelfClosing());

        // AfterAttributeValue_quoted followed by no space and another attribute
        Tokeniser t2 = createTokeniser("<a href=\"link\"id=\"main\">");
        Token.StartTag tag2 = (Token.StartTag) readNextToken(t2);
        assertEquals("link", tag2.attributes.get("href"));
        assertEquals("main", tag2.attributes.get("id"));

        // AfterAttributeValue_quoted at EOF
        Tokeniser t3 = createTokeniser("<a href=\"link\"");
        Token.EOF eof = (Token.EOF) readNextToken(t3);
        assertNotNull(eof);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Complex States (Rcdata, Rawtext, ScriptData)
    // =========================================================================

    @Test(timeout = 4000)
    public void testRcdataStateAndTransitions() {
        Tokeniser t = createTokeniser("<title>Hello &amp; <world> \u0000 </title>");
        // transition tokeniser manually or read tokens
        Token.StartTag titleTag = (Token.StartTag) readNextToken(t);
        assertEquals("title", titleTag.name());
        t.transition(TokeniserState.Rcdata);

        StringBuilder sb = new StringBuilder();
        while (true) {
            Token tok = readNextToken(t);
            if (tok instanceof Token.Character) {
                sb.append(((Token.Character) tok).getData());
            } else if (tok instanceof Token.EndTag) {
                assertEquals("title", ((Token.EndTag) tok).name());
                break;
            } else if (tok instanceof Token.EOF) {
                fail("Unexpected EOF");
            }
        }
        assertEquals("Hello & <world> \uFFFD ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRcdataAppropriateEndTagNotFound() {
        // Rcdata with start tag and no appropriate end tag
        Tokeniser t = createTokeniser("<textarea><foo>");
        Token.StartTag start = (Token.StartTag) readNextToken(t);
        assertEquals("textarea", start.name());
        t.transition(TokeniserState.Rcdata);

        Token tok1 = readNextToken(t);
        assertTrue(tok1 instanceof Token.EndTag);
        assertEquals("textarea", ((Token.EndTag) tok1).name());
    }

    @Test(timeout = 4000)
    public void testRawtextState() {
        Tokeniser t = createTokeniser("style data <notEnd </style-not> \u0000 </style>");
        t.transition(TokeniserState.Rawtext);
        t.createTagPending(false).name("style"); // set appropriate end tag

        StringBuilder sb = new StringBuilder();
        while (true) {
            Token tok = readNextToken(t);
            if (tok instanceof Token.Character) {
                sb.append(((Token.Character) tok).getData());
            } else if (tok instanceof Token.EndTag) {
                assertEquals("style", ((Token.EndTag) tok).name());
                break;
            }
        }
        assertEquals("style data <notEnd </style-not> \uFFFD ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testPLAINTEXTState() {
        Tokeniser t = createTokeniser("plain\u0000text data");
        t.transition(TokeniserState.PLAINTEXT);

        Token.Character c1 = (Token.Character) readNextToken(t);
        assertEquals("plain", c1.getData());
        Token.Character c2 = (Token.Character) readNextToken(t);
        assertEquals("\uFFFD", c2.getData());
        Token.Character c3 = (Token.Character) readNextToken(t);
        assertEquals("text data", c3.getData());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedStates() {
        String script = "<script><!-- var a = '<script>alert(1)<\\/script>'; --> \u0000 </script>";
        Tokeniser t = createTokeniser(script);
        Token.StartTag start = (Token.StartTag) readNextToken(t);
        assertEquals("script", start.name());
        t.transition(TokeniserState.ScriptData);

        StringBuilder sb = new StringBuilder();
        while (true) {
            Token tok = readNextToken(t);
            if (tok instanceof Token.Character) {
                sb.append(((Token.Character) tok).getData());
            } else if (tok instanceof Token.EndTag) {
                assertEquals("script", ((Token.EndTag) tok).name());
                break;
            } else if (tok instanceof Token.EOF) {
                break;
            }
        }
        assertTrue(sb.toString().contains("<!-- var a = '<script>alert(1)<\\/script>'; -->"));
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedStates() {
        String script = "<!--<script>var x = 1; --></script>";
        Tokeniser t = createTokeniser(script);
        t.transition(TokeniserState.ScriptData);

        StringBuilder sb = new StringBuilder();
        while (true) {
            Token tok = readNextToken(t);
            if (tok instanceof Token.Character) {
                sb.append(((Token.Character) tok).getData());
            } else if (tok instanceof Token.EndTag) {
                assertEquals("script", ((Token.EndTag) tok).name());
                break;
            } else if (tok instanceof Token.EOF) {
                break;
            }
        }
        assertTrue(sb.length() > 0);
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapeStartDashBranches() {
        Tokeniser t = createTokeniser("<!-other");
        t.transition(TokeniserState.ScriptDataLessthanSign);
        Token.Character c1 = (Token.Character) readNextToken(t);
        assertEquals("<!", c1.getData());
        Token.Character c2 = (Token.Character) readNextToken(t);
        assertEquals("-other", c2.getData());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashDashBranches() {
        // Test script data escaped with dashes and closing bracket
        Tokeniser t = createTokeniser("--->");
        t.transition(TokeniserState.ScriptDataEscapedDashDash);
        Token.Character c1 = (Token.Character) readNextToken(t);
        assertEquals("-", c1.getData());
        Token.Character c2 = (Token.Character) readNextToken(t);
        assertEquals(">", c2.getData());
    }

    // =========================================================================
    // Partition D: Comments, DOCTYPE & CDATA Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testCommentNormalAndDashes() {
        Tokeniser t = createTokeniser("<!-- hello -- world -->");
        Token.Comment comment = (Token.Comment) readNextToken(t);
        assertEquals(" hello -- world ", comment.getData());
        assertFalse(comment.bogus);
    }

    @Test(timeout = 4000)
    public void testCommentNullCharsAndBang() {
        Tokeniser t1 = createTokeniser("<!-- hello \u0000 --!>");
        Token.Comment comment1 = (Token.Comment) readNextToken(t1);
        assertEquals(" hello \uFFFD ", comment1.getData());

        Tokeniser t2 = createTokeniser("<!-- hello --!->");
        Token.Comment comment2 = (Token.Comment) readNextToken(t2);
        assertTrue(comment2.getData().contains("--!"));
    }

    @Test(timeout = 4000)
    public void testCommentPrematureEofAndStartDash() {
        Tokeniser t1 = createTokeniser("<!--");
        Token.Comment comment1 = (Token.Comment) readNextToken(t1);
        assertEquals("", comment1.getData());

        Tokeniser t2 = createTokeniser("<!--->");
        Token.Comment comment2 = (Token.Comment) readNextToken(t2);
        assertEquals("", comment2.getData());

        Tokeniser t3 = createTokeniser("<!-->");
        Token.Comment comment3 = (Token.Comment) readNextToken(t3);
        assertEquals("", comment3.getData());
    }

    @Test(timeout = 4000)
    public void testDoctypeStandardHtml5() {
        Tokeniser t = createTokeniser("<!DOCTYPE html>");
        Token.Doctype doctype = (Token.Doctype) readNextToken(t);
        assertEquals("html", doctype.getName());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicAndSystemIdentifiers() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" 'http://www.w3.org/TR/html4/strict.dtd'>");
        Token.Doctype doctype = (Token.Doctype) readNextToken(t);
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemOnly() {
        Tokeniser t = createTokeniser("<!DOCTYPE html SYSTEM \"about:legacy-compat\">");
        Token.Doctype doctype = (Token.Doctype) readNextToken(t);
        assertEquals("html", doctype.getName());
        assertEquals("about:legacy-compat", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypeInvalidAndQuirks() {
        // Missing name
        Tokeniser t1 = createTokeniser("<!DOCTYPE>");
        Token.Doctype d1 = (Token.Doctype) readNextToken(t1);
        assertTrue(d1.isForceQuirks());

        // Premature EOF in doctype
        Tokeniser t2 = createTokeniser("<!DOCTYPE html");
        Token.Doctype d2 = (Token.Doctype) readNextToken(t2);
        assertEquals("html", d2.getName());
        assertTrue(d2.isForceQuirks());

        // Null character in Doctype name
        Tokeniser t3 = createTokeniser("<!DOCTYPE ht\u0000ml>");
        Token.Doctype d3 = (Token.Doctype) readNextToken(t3);
        assertEquals("ht\uFFFDml", d3.getName());

        // Bogus doctype branch
        Tokeniser t4 = createTokeniser("<!DOCTYPE html BOGUS 'bad'>");
        Token.Doctype d4 = (Token.Doctype) readNextToken(t4);
        assertTrue(d4.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        Tokeniser t = createTokeniser("<![CDATA[some <cdata> & data]]>after");
        Token.Character cdata = (Token.Character) readNextToken(t);
        assertEquals("some <cdata> & data", cdata.getData());

        Token.Character after = (Token.Character) readNextToken(t);
        assertEquals("after", after.getData());
    }

    @Test(timeout = 4000)
    public void testBogusCommentFromMarkupDeclaration() {
        Tokeniser t = createTokeniser("<!UNKNOWN comment>");
        Token.Comment comment = (Token.Comment) readNextToken(t);
        assertTrue(comment.bogus);
        assertEquals("UNKNOWN comment", comment.getData());
    }

    // =========================================================================
    // Partition E: Enum Integrity & Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokeniserStateEnumValues() {
        TokeniserState[] states = TokeniserState.values();
        assertNotNull(states);
        assertTrue(states.length > 50);

        TokeniserState state = TokeniserState.valueOf("Data");
        assertEquals(TokeniserState.Data, state);
        assertEquals("Data", state.name());
    }

    @Test(timeout = 4000)
    public void testUnquotedAttributeSpecialEndings() {
        // Unquoted attribute followed by '>', '&', or whitespace
        Tokeniser t = createTokeniser("<a b=foo&amp;bar c=baz>");
        Token.StartTag tag = (Token.StartTag) readNextToken(t);
        assertEquals("foo&bar", tag.attributes.get("b"));
        assertEquals("baz", tag.attributes.get("c"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueQuotedEof() {
        // EOF inside double-quoted attribute value
        Tokeniser t1 = createTokeniser("<a b=\"val");
        Token token1 = readNextToken(t1);
        assertTrue(token1 instanceof Token.EOF);

        // EOF inside single-quoted attribute value
        Tokeniser t2 = createTokeniser("<a b='val");
        Token token2 = readNextToken(t2);
        assertTrue(token2 instanceof Token.EOF);
    }
}