package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/*
 [Branch & Defect Analysis Matrix]
 ---------------------------------------------------------------------------------------------------------
 State / Branch Target                       | Input / Boundary Condition               | Expected Outcome
 ---------------------------------------------------------------------------------------------------------
 ScriptDataEscapedEndTagName (DEFECT)        | Script comment with quotes: </scr' + ... | Quote "'" preserved, NOT skipped by errant advance()
 ScriptDataEscapedEndTagName Direct (DEFECT) | Reader with "scr'rest"                   | Reader current char remains '\'', not consumed
 Data                                        | '&', '<', '\u0000', EOF, normal text     | Proper transitions and token emissions
 CharacterReferenceInData                    | Valid named ref & invalid ref            | Char or literal '&' emitted, transitions to Data
 Rcdata                                      | '&', '<', '\u0000', EOF, text            | Handles RCDATA transitions and replacements
 CharacterReferenceInRcdata                  | Valid & invalid entity                   | Entity replaced or '&' emitted, transitions to Rcdata
 Rawtext                                     | '<', '\u0000', EOF, text                 | Proper transitions & null replacement
 ScriptData & ScriptDataLessthanSign         | '<' then '/', '!', other                 | ScriptDataEndTagOpen, EscapeStart, unconsume
 ScriptDataEscapeStart & Dash                | '-' vs other chars                       | Advances to EscapedDash/DashDash or reverts
 ScriptDataEscaped & EscapedDash(Dash)       | '-', '<', '>', '\u0000', EOF, text       | Full transition path through escaped script data
 ScriptDataDoubleEscapeStart & End           | "script" vs non-"script" tag name        | Transitions to DoubleEscaped vs Escaped
 PLAINTEXT                                   | '\u0000', EOF, normal text               | Replacement char emitted, EOF handled
 TagOpen & EndTagOpen                        | '!', '/', '?', letter, '>', empty, EOF   | TagName, MarkupDeclaration, BogusComment
 TagName                                     | Whitespace, '/', '>', '\u0000', EOF      | BeforeAttributeName, SelfClosing, emit tag
 BeforeAttributeName & AttributeName         | Whitespace, '/', '=', '>', '"', '\'', '<'| Attribute creation, name accumulation
 AfterAttributeName                          | Whitespace, '/', '=', '>', quotes        | SelfClosing, BeforeAttributeValue, Bogus
 BeforeAttributeValue                        | Whitespace, '"', '\'', '&', '>', '`'     | Double/single/unquoted value states
 AttributeValue (Double, Single, Unquoted)   | Quotes, '&', '\u0000', EOF, delimiters   | Value appended, entity resolution, emit tag
 AfterAttributeValue_quoted                  | Whitespace, '/', '>', EOF, invalid char  | BeforeAttributeName, SelfClosing, Data
 SelfClosingStartTag                         | '>', EOF, invalid character              | selfClosing = true, Data, BeforeAttributeName
 BogusComment                                | Starts with '?' or error declaration     | Emits Comment token with comment data
 MarkupDeclarationOpen                       | "--", "DOCTYPE", "[CDATA[", invalid char | CommentStart, Doctype, CdataSection, Bogus
 CommentStart, Dash, End, Bang               | '-', '>', '!', '\u0000', EOF, text       | Full comment lifecycle & quirks handling
 Doctype & BeforeDoctypeName & DoctypeName   | Whitespace, letter, '>', '\u0000', EOF   | DoctypePending creation, quirks, name append
 AfterDoctypeName                            | "PUBLIC", "SYSTEM", '>', whitespace, EOF | Transitions to Public/System keywords or bogus
 Doctype Public & System Identifiers         | Quoted ids, nullChar, '>', EOF, between  | Identifiers collected, forceQuirks checks
 BogusDoctype                                | '>', EOF, ignored characters             | Emits doctype with forceQuirks
 CdataSection                                | "foo]]>"                                 | Consumes to "]]>", emits data, returns to Data
 Enum Contract & Lifecycle                   | values(), valueOf()                      | All 49 enum states present and non-null
 ---------------------------------------------------------------------------------------------------------
*/
public class TokeniserStateGeminiTest {

    private List<Token> tokenize(String input) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(100));
        List<Token> tokens = new ArrayList<Token>();
        Token token;
        while (!((token = tokeniser.read()) instanceof Token.EOF)) {
            tokens.add(token);
        }
        tokens.add(token);
        return tokens;
    }

    private String extractAllCharacters(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            if (t instanceof Token.Character) {
                sb.append(((Token.Character) t).getData());
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: HtmlParserTest::handlesQuotesInCommentsInScripts
     * In ScriptDataEscapedEndTagName, when r.matchesLetter() is true,
     * consumeLetterSequence() consumes the tag name letters.
     * The buggy code erroneously executed r.advance(), which skipped the very
     * next character (e.g., the quote `'` immediately following `</scr`).
     */
    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagNameHandlesQuotesInScriptCommentDefect() {
        String html = "<script><!-- document.write('</scr' + 'ipt>'); --></script>";
        List<Token> tokens = tokenize(html);
        String chars = extractAllCharacters(tokens);

        assertTrue("Emitted script data must preserve the single quote after '</scr': " + chars,
                chars.contains("document.write('</scr' + 'ipt>');"));
    }

    /**
     * Direct unit test targeting the errant r.advance() in ScriptDataEscapedEndTagName.
     */
    @Test(timeout = 4000)
    public void testScriptDataEscapedEndTagNameDirectReaderPositionDefect() {
        CharacterReader r = new CharacterReader("scr'tail");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        t.createTagPending(false);
        t.createTempBuffer();

        TokeniserState.ScriptDataEscapedEndTagName.read(t, r);

        // "scr" was consumed. The next character MUST be '\'' and NOT 't' (which would happen if r.advance() ran).
        assertEquals("Reader should be positioned at the quote character, not past it", '\'', r.current());
        assertEquals("scr", t.dataBuffer.toString());
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testDataStateBasicTransitions() {
        // Tag open
        CharacterReader r1 = new CharacterReader("<a");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(10));
        TokeniserState.Data.read(t1, r1);
        Token tagToken = t1.read();
        assertTrue(tagToken instanceof Token.StartTag);
        assertEquals("a", ((Token.StartTag) tagToken).name());

        // Char ref
        List<Token> tokens = tokenize("&lt;div&gt;");
        assertEquals("<div", extractAllCharacters(tokens));
    }

    @Test(timeout = 4000)
    public void testTagOpenAndEndTagOpenVariants() {
        // TagOpen '?' -> BogusComment
        List<Token> tokensBogus = tokenize("<?xml version='1.0'?>");
        assertTrue(tokensBogus.get(0) instanceof Token.Comment);

        // TagOpen '!' -> MarkupDeclarationOpen
        List<Token> tokensComment = tokenize("<!-- a comment -->");
        assertTrue(tokensComment.get(0) instanceof Token.Comment);
        assertEquals(" a comment ", ((Token.Comment) tokensComment.get(0)).getData());

        // TagOpen non-letter -> error and emit '<'
        CharacterReader rBad = new CharacterReader("3");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tBad = new Tokeniser(rBad, errors);
        TokeniserState.TagOpen.read(tBad, rBad);
        assertEquals(1, errors.size());

        // EndTagOpen '>' -> error and Data
        CharacterReader rEndAngle = new CharacterReader(">");
        ParseErrorList errEndAngle = ParseErrorList.tracking(10);
        Tokeniser tEndAngle = new Tokeniser(rEndAngle, errEndAngle);
        TokeniserState.EndTagOpen.read(tEndAngle, rEndAngle);
        assertEquals(1, errEndAngle.size());

        // EndTagOpen bogus character
        CharacterReader rEndBogus = new CharacterReader("@bogus>");
        ParseErrorList errEndBogus = ParseErrorList.tracking(10);
        Tokeniser tEndBogus = new Tokeniser(rEndBogus, errEndBogus);
        TokeniserState.EndTagOpen.read(tEndBogus, rEndBogus);
        assertEquals(1, errEndBogus.size());
    }

    @Test(timeout = 4000)
    public void testTagNameWhitespaceAndSelfClosingTransitions() {
        List<Token> tokens = tokenize("<img / src='foo.png' >");
        assertTrue(tokens.get(0) instanceof Token.StartTag);
        Token.StartTag img = (Token.StartTag) tokens.get(0);
        assertEquals("img", img.name());
        assertEquals("foo.png", img.getAttributes().get("src"));

        List<Token> tokensSelfClose = tokenize("<br/>");
        assertTrue(tokensSelfClose.get(0) instanceof Token.StartTag);
        assertTrue(((Token.StartTag) tokensSelfClose.get(0)).isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testAttributesVariants() {
        // Double quoted, single quoted, unquoted, empty attribute, and character references within
        String html = "<a b=\"val&amp;\" c='val&lt;' d=unquoted e>";
        List<Token> tokens = tokenize(html);
        assertTrue(tokens.get(0) instanceof Token.StartTag);
        Token.StartTag tag = (Token.StartTag) tokens.get(0);
        assertEquals("val&", tag.getAttributes().get("b"));
        assertEquals("val<", tag.getAttributes().get("c"));
        assertEquals("unquoted", tag.getAttributes().get("d"));
        assertTrue(tag.getAttributes().hasKey("e"));
    }

    @Test(timeout = 4000)
    public void testBeforeAttributeValueDelimiters() {
        // Characters '<', '=', '`' in BeforeAttributeValue produce syntax errors
        ParseErrorList errs = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("<a b=<val>");
        Tokeniser t = new Tokeniser(r, errs);
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertFalse(errs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValueQuotedStates() {
        // Quoted attribute value followed immediately by '/'
        List<Token> tokens = tokenize("<img src=\"test.jpg\"/>");
        assertTrue(tokens.get(0) instanceof Token.StartTag);
        assertTrue(((Token.StartTag) tokens.get(0)).isSelfClosing());

        // Quoted attribute value followed by invalid character (missing space)
        ParseErrorList errs = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("<a href=\"link\"class=\"btn\">");
        Tokeniser t = new Tokeniser(r, errs);
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertFalse(errs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCommentVariants() {
        // Comment with multiple dashes and bang
        String commentHtml = "<!-- -- comment --!>content";
        List<Token> tokens = tokenize(commentHtml);
        assertTrue(tokens.get(0) instanceof Token.Comment);
        assertEquals(" -- comment ", ((Token.Comment) tokens.get(0)).getData());
        assertTrue(tokens.get(1) instanceof Token.Character);
        assertEquals("content", ((Token.Character) tokens.get(1)).getData());

        // Comment ending immediately: <!--> and <!--->
        ParseErrorList errs = ParseErrorList.tracking(10);
        CharacterReader r1 = new CharacterReader("<!-->");
        Tokeniser t1 = new Tokeniser(r1, errs);
        Token tok1 = t1.read();
        assertTrue(tok1 instanceof Token.Comment);

        CharacterReader r2 = new CharacterReader("<!--->");
        Tokeniser t2 = new Tokeniser(r2, errs);
        Token tok2 = t2.read();
        assertTrue(tok2 instanceof Token.Comment);
    }

    @Test(timeout = 4000)
    public void testDoctypeTransitions() {
        // Standard HTML5 DOCTYPE
        List<Token> tokens = tokenize("<!DOCTYPE html>");
        assertTrue(tokens.get(0) instanceof Token.Doctype);
        Token.Doctype dt1 = (Token.Doctype) tokens.get(0);
        assertEquals("html", dt1.getName());
        assertFalse(dt1.isForceQuirks());

        // DOCTYPE with PUBLIC and SYSTEM identifiers
        String complexDoctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>";
        List<Token> tokens2 = tokenize(complexDoctype);
        assertTrue(tokens2.get(0) instanceof Token.Doctype);
        Token.Doctype dt2 = (Token.Doctype) tokens2.get(0);
        assertEquals("html", dt2.getName());
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", dt2.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", dt2.getSystemIdentifier());
        assertFalse(dt2.isForceQuirks());

        // Malformed DOCTYPE forcing quirks
        String badDoctype = "<!DOCTYPE>";
        List<Token> tokens3 = tokenize(badDoctype);
        assertTrue(tokens3.get(0) instanceof Token.Doctype);
        assertTrue(((Token.Doctype) tokens3.get(0)).isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeSystemKeywordAndBogus() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader(" SYSTEM 'sysid' >");
        Tokeniser t = new Tokeniser(r, errors);
        t.createDoctypePending();
        TokeniserState.AfterDoctypeName.read(t, r);
        Token tok = t.read();
        assertTrue(tok instanceof Token.Doctype);
        assertEquals("sysid", ((Token.Doctype) tok).getSystemIdentifier());
    }

    @Test(timeout = 4000)
    public void testRcdataAndAppropriateEndTag() {
        // Start tag <title>
        CharacterReader r = new CharacterReader("<title>Sample &amp; Test</title>After");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));

        Token startTag = t.read();
        assertTrue(startTag instanceof Token.StartTag);
        assertEquals("title", ((Token.StartTag) startTag).name());

        t.transition(TokeniserState.Rcdata);
        Token rcdataChars = t.read();
        assertTrue(rcdataChars instanceof Token.Character);
        assertEquals("Sample & Test", ((Token.Character) rcdataChars).getData());

        Token endTag = t.read();
        assertTrue(endTag instanceof Token.EndTag);
        assertEquals("title", ((Token.EndTag) endTag).name());
    }

    @Test(timeout = 4000)
    public void testRcdataWithInappropriateEndTag() {
        // In RCDATA, an unmatched end tag should be treated as text
        CharacterReader r = new CharacterReader("<title>Heading </textarea> still title</title>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token startTag = t.read();
        assertEquals("title", ((Token.StartTag) startTag).name());

        t.transition(TokeniserState.Rcdata);
        StringBuilder data = new StringBuilder();
        Token tok;
        while ((tok = t.read()) instanceof Token.Character) {
            data.append(((Token.Character) tok).getData());
        }
        assertTrue("RCDATA should include </textarea> in text: " + data.toString(),
                data.toString().contains("</textarea>"));
        assertTrue(tok instanceof Token.EndTag);
        assertEquals("title", ((Token.EndTag) tok).name());
    }

    @Test(timeout = 4000)
    public void testRawtextStateAndTransitions() {
        CharacterReader r = new CharacterReader("<style>div { color: red; }</style>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token start = t.read();
        assertEquals("style", ((Token.StartTag) start).name());

        t.transition(TokeniserState.Rawtext);
        Token body = t.read();
        assertTrue(body instanceof Token.Character);
        assertEquals("div { color: red; }", ((Token.Character) body).getData());

        Token end = t.read();
        assertTrue(end instanceof Token.EndTag);
        assertEquals("style", ((Token.EndTag) end).name());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedTransitions() {
        // Script data containing double escaped sequences like <script> inside <!--
        String script = "<script><!-- var s = '<script>inner</script>'; --></script>";
        List<Token> tokens = tokenize(script);
        String body = extractAllCharacters(tokens);
        assertTrue("Script double escaped body preserved: " + body,
                body.contains("var s = '<script>inner</script>';"));
    }

    @Test(timeout = 4000)
    public void testCdataSectionHandling() {
        CharacterReader r = new CharacterReader("CDATA Content]]>After");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.CdataSection.read(t, r);
        Token tok = t.read();
        assertTrue(tok instanceof Token.Character);
        assertEquals("CDATA Content", ((Token.Character) tok).getData());
    }

    @Test(timeout = 4000)
    public void testPLAINTEXTStateHandling() {
        CharacterReader r = new CharacterReader("Some plain text\u0000rest");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.PLAINTEXT);

        Token tok1 = t.read();
        assertTrue(tok1 instanceof Token.Character);
        assertEquals("Some plain text", ((Token.Character) tok1).getData());

        Token tok2 = t.read();
        assertTrue(tok2 instanceof Token.Character);
        assertEquals("\uFFFD", ((Token.Character) tok2).getData());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullCharacterHandlingAcrossStates() {
        ParseErrorList errors = ParseErrorList.tracking(50);
        CharacterReader r = new CharacterReader("a\u0000b");
        Tokeniser t = new Tokeniser(r, errors);

        // Data state with nullChar
        Token tok = t.read();
        assertTrue(tok instanceof Token.Character);
        // Data emits raw null character (or consumes it with an error)
        assertFalse(errors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullCharacterInAttributeNameAndValue() {
        ParseErrorList errors = ParseErrorList.tracking(50);
        CharacterReader r = new CharacterReader("<tag attr\u0000name=\"val\u0000ue\">");
        Tokeniser t = new Tokeniser(r, errors);
        Token tok = t.read();
        assertTrue(tok instanceof Token.StartTag);
        Token.StartTag tag = (Token.StartTag) tok;
        // Replacement character \uFFFD should be appended
        assertTrue(tag.getAttributes().hasKey("attr\uFFFDname"));
        assertEquals("val\uFFFDue", tag.getAttributes().get("attr\uFFFDname"));
    }

    @Test(timeout = 4000)
    public void testNullCharacterInComment() {
        ParseErrorList errors = ParseErrorList.tracking(50);
        CharacterReader r = new CharacterReader("<!-- com\u0000ment -->");
        Tokeniser t = new Tokeniser(r, errors);
        Token tok = t.read();
        assertTrue(tok instanceof Token.Comment);
        assertEquals(" com\uFFFDment ", ((Token.Comment) tok).getData());
    }

    @Test(timeout = 4000)
    public void testEmptyReaderEofHandling() {
        CharacterReader r = new CharacterReader("");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token tok = t.read();
        assertTrue(tok instanceof Token.EOF);
    }

    @Test(timeout = 4000)
    public void testUnexpectedEofInAllPendingStates() {
        // EOF in TagName
        List<Token> tokensTag = tokenize("<tag");
        assertTrue(tokensTag.get(tokensTag.size() - 1) instanceof Token.EOF);

        // EOF in AttributeName
        List<Token> tokensAttr = tokenize("<tag attr");
        assertTrue(tokensAttr.get(tokensAttr.size() - 1) instanceof Token.EOF);

        // EOF in AttributeValue
        List<Token> tokensVal = tokenize("<tag attr=\"val");
        assertTrue(tokensVal.get(tokensVal.size() - 1) instanceof Token.EOF);

        // EOF in Comment
        List<Token> tokensComm = tokenize("<!-- unfinished comment");
        assertTrue(tokensComm.get(0) instanceof Token.Comment);
        assertEquals(" unfinished comment", ((Token.Comment) tokensComm.get(0)).getData());

        // EOF in Doctype
        List<Token> tokensDoc = tokenize("<!DOCTYPE html PUBLIC \"pubid");
        assertTrue(tokensDoc.get(0) instanceof Token.Doctype);
        assertTrue(((Token.Doctype) tokensDoc.get(0)).isForceQuirks());
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testMarkupDeclarationOpenMalformed() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("<!XYZ>");
        Tokeniser t = new Tokeniser(r, errors);
        Token token = t.read();
        // Malformed declaration should produce a BogusComment token and an error
        assertTrue(token instanceof Token.Comment);
        assertFalse(errors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagInvalidCharacter() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("<img / src='foo'>");
        Tokeniser t = new Tokeniser(r, errors);
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertFalse(errors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypePublicKeywordErrors() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("<!DOCTYPE html PUBLIC >");
        Tokeniser t = new Tokeniser(r, errors);
        Token tok = t.read();
        assertTrue(tok instanceof Token.Doctype);
        assertTrue(((Token.Doctype) tok).isForceQuirks());
        assertFalse(errors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testBogusDoctypeRecovery() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("<!DOCTYPE html BOGUSIDENTIFIER 'something' >Text");
        Tokeniser t = new Tokeniser(r, errors);
        Token tok1 = t.read();
        assertTrue(tok1 instanceof Token.Doctype);
        assertTrue(((Token.Doctype) tok1).isForceQuirks());
        Token tok2 = t.read();
        assertTrue(tok2 instanceof Token.Character);
        assertEquals("Text", ((Token.Character) tok2).getData());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapeStartDashBranches() {
        // Test '-' followed by non-'-' in ScriptDataEscapeStart
        CharacterReader r = new CharacterReader("<!-X");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        t.transition(TokeniserState.ScriptDataLessthanSign);
        Token tok = t.read();
        assertTrue(tok instanceof Token.Character);
    }

    @Test(timeout = 4000)
    public void testCharacterReferenceWithoutSemicolonOrInvalid() {
        // Unterminated / unknown character reference in data
        List<Token> tokens = tokenize("&unknownEntity;");
        assertEquals("&unknownEntity;", extractAllCharacters(tokens));

        // In RCDATA
        CharacterReader r = new CharacterReader("<title>&notanentity; </title>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        t.read(); // <title>
        t.transition(TokeniserState.Rcdata);
        Token tok = t.read();
        assertTrue(tok instanceof Token.Character);
        assertEquals("&notanentity; ", ((Token.Character) tok).getData());
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokeniserStateEnumValuesAndCompleteness() {
        TokeniserState[] states = TokeniserState.values();
        assertNotNull(states);
        // TokeniserState has exactly 49 states per HTML5 tokeniser specification
        assertEquals(49, states.length);

        for (TokeniserState s : states) {
            assertNotNull(s);
            assertEquals(s, TokeniserState.valueOf(s.name()));
        }
    }

    @Test(timeout = 4000)
    public void testTokeniserStateSpecificConstants() {
        assertEquals(TokeniserState.Data, TokeniserState.valueOf("Data"));
        assertEquals(TokeniserState.TagOpen, TokeniserState.valueOf("TagOpen"));
        assertEquals(TokeniserState.EndTagOpen, TokeniserState.valueOf("EndTagOpen"));
        assertEquals(TokeniserState.TagName, TokeniserState.valueOf("TagName"));
        assertEquals(TokeniserState.ScriptData, TokeniserState.valueOf("ScriptData"));
        assertEquals(TokeniserState.ScriptDataEscaped, TokeniserState.valueOf("ScriptDataEscaped"));
        assertEquals(TokeniserState.ScriptDataEscapedEndTagName, TokeniserState.valueOf("ScriptDataEscapedEndTagName"));
        assertEquals(TokeniserState.Comment, TokeniserState.valueOf("Comment"));
        assertEquals(TokeniserState.Doctype, TokeniserState.valueOf("Doctype"));
    }
}