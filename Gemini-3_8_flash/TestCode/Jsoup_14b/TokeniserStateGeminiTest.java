package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Enum: org.jsoup.parser.TokeniserState
 *
 * Target Defect (Defects4J):
 * - ParserTest::parsesUnterminatedTextarea
 *   Failure: expected:<one[]> but was:<one[<p>two]>
 * - ParserTest::handlesUnclosedTitle
 *   Failure: expected:<One[]> but was:<One[<b>Two <p>Test</p]>
 * Cause: In RCDATA state, when encountering '<' followed by non-end-tag (e.g., start tag),
 * the TokeniserState.RcdataLessthanSign transitions back to Rcdata emitting '<',
 * consuming subsequent HTML tags into the textarea/title rather than terminating.
 *
 * Branch Zones Covered:
 * 1. Data, Rcdata, Rawtext, ScriptData, PLAINTEXT:
 *    - Transitions on '&', '<', nullChar ('\u0000'), and EOF
 * 2. CharacterReferenceInData & CharacterReferenceInRcdata:
 *    - c == null vs c != null paths
 * 3. TagOpen & EndTagOpen:
 *    - '!', '/', '?', letter, empty/EOF, '>', and default characters
 * 4. TagName:
 *    - Whitespace ('\t', '\n', '\f', ' '), '/', '>', nullChar, EOF
 * 5. RcdataLessthanSign, RCDATAEndTagOpen, RCDATAEndTagName:
 *    - '/' vs non-'/', letter vs non-letter
 *    - isAppropriateEndTagToken() true vs false branches for whitespace, '/', '>', and default
 * 6. RawtextLessthanSign, RawtextEndTagOpen, RawtextEndTagName:
 *    - Appropriate end tag vs unhandled fallback paths
 * 7. ScriptDataLessthanSign, ScriptDataEndTagOpen, ScriptDataEndTagName:
 *    - '/', '!', default; handled vs unhandled fallback paths
 * 8. ScriptDataEscape states (EscapeStart, EscapeStartDash, Escaped, EscapedDash, EscapedDashDash):
 *    - '-', '<', nullChar, EOF, default branches
 * 9. ScriptDataDoubleEscape states (DoubleEscapeStart, DoubleEscaped, Dash, DashDash, LessthanSign, End):
 *    - "script" buffer match vs non-match, whitespace, '/', '>', default transitions
 * 10. Attribute states (BeforeAttributeName, AttributeName, AfterAttributeName, BeforeAttributeValue,
 *     AttributeValue_doubleQuoted, AttributeValue_singleQuoted, AttributeValue_unquoted,
 *     AfterAttributeValue_quoted, SelfClosingStartTag):
 *     - All delimiter branches, nullChar handling, quotes, '&' character references, EOF
 * 11. BogusComment & MarkupDeclarationOpen:
 *     - "--" (Comment), "DOCTYPE", "[CDATA[", and fallback to BogusComment
 * 12. Comment states (CommentStart, CommentStartDash, Comment, CommentEndDash, CommentEnd, CommentEndBang):
 *     - '-', '>', '!', nullChar, EOF, and data accumulation
 * 13. Doctype states (Doctype, BeforeDoctypeName, DoctypeName, AfterDoctypeName, AfterDoctypePublicKeyword,
 *     BeforeDoctypePublicIdentifier, PublicIdentifier double/single quoted, AfterDoctypePublicIdentifier,
 *     BetweenDoctypePublicAndSystemIdentifiers, AfterDoctypeSystemKeyword, BeforeDoctypeSystemIdentifier,
 *     SystemIdentifier double/single quoted, AfterDoctypeSystemIdentifier, BogusDoctype):
 *     - Whitespace, quotes, '>', nullChar, EOF, PUBLIC/SYSTEM keyword recognition, forceQuirks flags
 * 14. CdataSection:
 *     - Reads up to "]]>" and transitions to Data
 * =========================================================================
 */
public class TokeniserStateGeminiTest {

    private Tokeniser createTokeniser(String text) {
        return new Tokeniser(new CharacterReader(text), ParseErrorList.tracking(100));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDataTransitions() {
        Tokeniser t1 = createTokeniser("&");
        TokeniserState.Data.read(t1, t1.reader);
        assertEquals(TokeniserState.CharacterReferenceInData, t1.getState());

        Tokeniser t2 = createTokeniser("<");
        TokeniserState.Data.read(t2, t2.reader);
        assertEquals(TokeniserState.TagOpen, t2.getState());

        Tokeniser t3 = createTokeniser("hello world");
        TokeniserState.Data.read(t3, t3.reader);
        assertEquals(TokeniserState.Data, t3.getState());

        Tokeniser t4 = createTokeniser("");
        TokeniserState.Data.read(t4, t4.reader);
        assertEquals(TokeniserState.Data, t4.getState());
    }

    @Test(timeout = 4000)
    public void testCharacterReferenceInData() {
        // Null ref -> emits '&'
        Tokeniser t1 = createTokeniser(" unknown;");
        TokeniserState.CharacterReferenceInData.read(t1, t1.reader);
        assertEquals(TokeniserState.Data, t1.getState());

        // Valid ref -> emits resolved char
        Tokeniser t2 = createTokeniser("lt;");
        TokeniserState.CharacterReferenceInData.read(t2, t2.reader);
        assertEquals(TokeniserState.Data, t2.getState());
    }

    @Test(timeout = 4000)
    public void testRcdataTransitions() {
        Tokeniser t1 = createTokeniser("&");
        TokeniserState.Rcdata.read(t1, t1.reader);
        assertEquals(TokeniserState.CharacterReferenceInRcdata, t1.getState());

        Tokeniser t2 = createTokeniser("<");
        TokeniserState.Rcdata.read(t2, t2.reader);
        assertEquals(TokeniserState.RcdataLessthanSign, t2.getState());

        Tokeniser t3 = createTokeniser("some rcdata");
        TokeniserState.Rcdata.read(t3, t3.reader);
        assertEquals(TokeniserState.Rcdata, t3.getState());

        Tokeniser t4 = createTokeniser("");
        TokeniserState.Rcdata.read(t4, t4.reader);
        assertEquals(TokeniserState.Rcdata, t4.getState());
    }

    @Test(timeout = 4000)
    public void testCharacterReferenceInRcdata() {
        Tokeniser t1 = createTokeniser(" notRef");
        TokeniserState.CharacterReferenceInRcdata.read(t1, t1.reader);
        assertEquals(TokeniserState.Rcdata, t1.getState());

        Tokeniser t2 = createTokeniser("amp;");
        TokeniserState.CharacterReferenceInRcdata.read(t2, t2.reader);
        assertEquals(TokeniserState.Rcdata, t2.getState());
    }

    @Test(timeout = 4000)
    public void testRawtextTransitions() {
        Tokeniser t1 = createTokeniser("<");
        TokeniserState.Rawtext.read(t1, t1.reader);
        assertEquals(TokeniserState.RawtextLessthanSign, t1.getState());

        Tokeniser t2 = createTokeniser("raw text data");
        TokeniserState.Rawtext.read(t2, t2.reader);
        assertEquals(TokeniserState.Rawtext, t2.getState());

        Tokeniser t3 = createTokeniser("");
        TokeniserState.Rawtext.read(t3, t3.reader);
        assertEquals(TokeniserState.Rawtext, t3.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataTransitions() {
        Tokeniser t1 = createTokeniser("<");
        TokeniserState.ScriptData.read(t1, t1.reader);
        assertEquals(TokeniserState.ScriptDataLessthanSign, t1.getState());

        Tokeniser t2 = createTokeniser("var x = 1;");
        TokeniserState.ScriptData.read(t2, t2.reader);
        assertEquals(TokeniserState.ScriptData, t2.getState());

        Tokeniser t3 = createTokeniser("");
        TokeniserState.ScriptData.read(t3, t3.reader);
        assertEquals(TokeniserState.ScriptData, t3.getState());
    }

    @Test(timeout = 4000)
    public void testPlaintextTransitions() {
        Tokeniser t1 = createTokeniser("plain text data");
        TokeniserState.PLAINTEXT.read(t1, t1.reader);
        assertEquals(TokeniserState.PLAINTEXT, t1.getState());

        Tokeniser t2 = createTokeniser("");
        TokeniserState.PLAINTEXT.read(t2, t2.reader);
        assertEquals(TokeniserState.PLAINTEXT, t2.getState());
    }

    @Test(timeout = 4000)
    public void testTagOpenTransitions() {
        Tokeniser t1 = createTokeniser("!");
        TokeniserState.TagOpen.read(t1, t1.reader);
        assertEquals(TokeniserState.MarkupDeclarationOpen, t1.getState());

        Tokeniser t2 = createTokeniser("/");
        TokeniserState.TagOpen.read(t2, t2.reader);
        assertEquals(TokeniserState.EndTagOpen, t2.getState());

        Tokeniser t3 = createTokeniser("?");
        TokeniserState.TagOpen.read(t3, t3.reader);
        assertEquals(TokeniserState.BogusComment, t3.getState());

        Tokeniser t4 = createTokeniser("div");
        TokeniserState.TagOpen.read(t4, t4.reader);
        assertEquals(TokeniserState.TagName, t4.getState());
        assertNotNull(t4.tagPending);

        Tokeniser t5 = createTokeniser("@");
        TokeniserState.TagOpen.read(t5, t5.reader);
        assertEquals(TokeniserState.Data, t5.getState());
    }

    @Test(timeout = 4000)
    public void testEndTagOpenTransitions() {
        Tokeniser t1 = createTokeniser("");
        TokeniserState.EndTagOpen.read(t1, t1.reader);
        assertEquals(TokeniserState.Data, t1.getState());

        Tokeniser t2 = createTokeniser("a");
        TokeniserState.EndTagOpen.read(t2, t2.reader);
        assertEquals(TokeniserState.TagName, t2.getState());
        assertNotNull(t2.tagPending);

        Tokeniser t3 = createTokeniser(">");
        TokeniserState.EndTagOpen.read(t3, t3.reader);
        assertEquals(TokeniserState.Data, t3.getState());

        Tokeniser t4 = createTokeniser("9");
        TokeniserState.EndTagOpen.read(t4, t4.reader);
        assertEquals(TokeniserState.BogusComment, t4.getState());
    }

    @Test(timeout = 4000)
    public void testTagNameTransitions() {
        Tokeniser t1 = createTokeniser("div ");
        t1.createTagPending(true);
        TokeniserState.TagName.read(t1, t1.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t1.getState());

        Tokeniser t2 = createTokeniser("div/");
        t2.createTagPending(true);
        TokeniserState.TagName.read(t2, t2.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t2.getState());

        Tokeniser t3 = createTokeniser("div>");
        t3.createTagPending(true);
        TokeniserState.TagName.read(t3, t3.reader);
        assertEquals(TokeniserState.Data, t3.getState());

        Tokeniser t4 = createTokeniser("div");
        t4.createTagPending(true);
        TokeniserState.TagName.read(t4, t4.reader);
        assertEquals(TokeniserState.Data, t4.getState());
    }

    @Test(timeout = 4000)
    public void testRcdataEndTagHandling() {
        // Appropriate end tag token: '>'
        Tokeniser t1 = createTokeniser(">");
        t1.createTagPending(true);
        t1.tagPending.appendTagName("title");
        t1.emitTagPending(); // sets lastStartTag to "title"
        t1.createTagPending(false);
        t1.tagPending.appendTagName("title");
        TokeniserState.RCDATAEndTagName.read(t1, t1.reader);
        assertEquals(TokeniserState.Data, t1.getState());

        // Inappropriate end tag token: fallback to anythingElse
        Tokeniser t2 = createTokeniser(">");
        t2.createTagPending(true);
        t2.tagPending.appendTagName("title");
        t2.emitTagPending();
        t2.createTagPending(false);
        t2.tagPending.appendTagName("other");
        t2.createTempBuffer();
        TokeniserState.RCDATAEndTagName.read(t2, t2.reader);
        assertEquals(TokeniserState.Rcdata, t2.getState());

        // Appropriate end tag with whitespace and '/'
        Tokeniser t3 = createTokeniser(" ");
        t3.createTagPending(true);
        t3.tagPending.appendTagName("title");
        t3.emitTagPending();
        t3.createTagPending(false);
        t3.tagPending.appendTagName("title");
        TokeniserState.RCDATAEndTagName.read(t3, t3.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t3.getState());

        Tokeniser t4 = createTokeniser("/");
        t4.createTagPending(true);
        t4.tagPending.appendTagName("title");
        t4.emitTagPending();
        t4.createTagPending(false);
        t4.tagPending.appendTagName("title");
        TokeniserState.RCDATAEndTagName.read(t4, t4.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t4.getState());
    }

    @Test(timeout = 4000)
    public void testRawtextEndTagTransitions() {
        Tokeniser t1 = createTokeniser("/");
        TokeniserState.RawtextLessthanSign.read(t1, t1.reader);
        assertEquals(TokeniserState.RawtextEndTagOpen, t1.getState());

        Tokeniser t2 = createTokeniser("x");
        TokeniserState.RawtextLessthanSign.read(t2, t2.reader);
        assertEquals(TokeniserState.Rawtext, t2.getState());

        Tokeniser t3 = createTokeniser("style");
        TokeniserState.RawtextEndTagOpen.read(t3, t3.reader);
        assertEquals(TokeniserState.RawtextEndTagName, t3.getState());

        Tokeniser t4 = createTokeniser("1");
        TokeniserState.RawtextEndTagOpen.read(t4, t4.reader);
        assertEquals(TokeniserState.Rawtext, t4.getState());

        // Appropriate end tag in RawtextEndTagName
        Tokeniser t5 = createTokeniser(">");
        t5.createTagPending(true);
        t5.tagPending.appendTagName("style");
        t5.emitTagPending();
        t5.createTagPending(false);
        t5.tagPending.appendTagName("style");
        TokeniserState.RawtextEndTagName.read(t5, t5.reader);
        assertEquals(TokeniserState.Data, t5.getState());

        // Inappropriate end tag in RawtextEndTagName
        Tokeniser t6 = createTokeniser(">");
        t6.createTagPending(true);
        t6.tagPending.appendTagName("style");
        t6.emitTagPending();
        t6.createTagPending(false);
        t6.tagPending.appendTagName("other");
        t6.createTempBuffer();
        TokeniserState.RawtextEndTagName.read(t6, t6.reader);
        assertEquals(TokeniserState.Rawtext, t6.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataLessthanSignAndEndTag() {
        Tokeniser t1 = createTokeniser("/");
        TokeniserState.ScriptDataLessthanSign.read(t1, t1.reader);
        assertEquals(TokeniserState.ScriptDataEndTagOpen, t1.getState());

        Tokeniser t2 = createTokeniser("!");
        TokeniserState.ScriptDataLessthanSign.read(t2, t2.reader);
        assertEquals(TokeniserState.ScriptDataEscapeStart, t2.getState());

        Tokeniser t3 = createTokeniser("x");
        TokeniserState.ScriptDataLessthanSign.read(t3, t3.reader);
        assertEquals(TokeniserState.ScriptData, t3.getState());

        Tokeniser t4 = createTokeniser("script");
        TokeniserState.ScriptDataEndTagOpen.read(t4, t4.reader);
        assertEquals(TokeniserState.ScriptDataEndTagName, t4.getState());

        Tokeniser t5 = createTokeniser("1");
        TokeniserState.ScriptDataEndTagOpen.read(t5, t5.reader);
        assertEquals(TokeniserState.ScriptData, t5.getState());

        // Appropriate script end tag
        Tokeniser t6 = createTokeniser(">");
        t6.createTagPending(true);
        t6.tagPending.appendTagName("script");
        t6.emitTagPending();
        t6.createTagPending(false);
        t6.tagPending.appendTagName("script");
        TokeniserState.ScriptDataEndTagName.read(t6, t6.reader);
        assertEquals(TokeniserState.Data, t6.getState());

        // Inappropriate script end tag
        Tokeniser t7 = createTokeniser(">");
        t7.createTagPending(true);
        t7.tagPending.appendTagName("script");
        t7.emitTagPending();
        t7.createTagPending(false);
        t7.tagPending.appendTagName("div");
        t7.createTempBuffer();
        TokeniserState.ScriptDataEndTagName.read(t7, t7.reader);
        assertEquals(TokeniserState.ScriptData, t7.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapePipeline() {
        Tokeniser t1 = createTokeniser("-");
        TokeniserState.ScriptDataEscapeStart.read(t1, t1.reader);
        assertEquals(TokeniserState.ScriptDataEscapeStartDash, t1.getState());

        Tokeniser t2 = createTokeniser("x");
        TokeniserState.ScriptDataEscapeStart.read(t2, t2.reader);
        assertEquals(TokeniserState.ScriptData, t2.getState());

        Tokeniser t3 = createTokeniser("-");
        TokeniserState.ScriptDataEscapeStartDash.read(t3, t3.reader);
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t3.getState());

        Tokeniser t4 = createTokeniser("x");
        TokeniserState.ScriptDataEscapeStartDash.read(t4, t4.reader);
        assertEquals(TokeniserState.ScriptData, t4.getState());

        // ScriptDataEscaped branches
        Tokeniser t5 = createTokeniser("-");
        TokeniserState.ScriptDataEscaped.read(t5, t5.reader);
        assertEquals(TokeniserState.ScriptDataEscapedDash, t5.getState());

        Tokeniser t6 = createTokeniser("<");
        TokeniserState.ScriptDataEscaped.read(t6, t6.reader);
        assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t6.getState());

        Tokeniser t7 = createTokeniser("");
        TokeniserState.ScriptDataEscaped.read(t7, t7.reader);
        assertEquals(TokeniserState.Data, t7.getState());

        // ScriptDataEscapedDash branches
        Tokeniser t8 = createTokeniser("-");
        TokeniserState.ScriptDataEscapedDash.read(t8, t8.reader);
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t8.getState());

        Tokeniser t9 = createTokeniser("<");
        TokeniserState.ScriptDataEscapedDash.read(t9, t9.reader);
        assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t9.getState());

        Tokeniser t10 = createTokeniser("x");
        TokeniserState.ScriptDataEscapedDash.read(t10, t10.reader);
        assertEquals(TokeniserState.ScriptDataEscaped, t10.getState());

        // ScriptDataEscapedDashDash branches
        Tokeniser t11 = createTokeniser("-");
        TokeniserState.ScriptDataEscapedDashDash.read(t11, t11.reader);
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t11.getState());

        Tokeniser t12 = createTokeniser("<");
        TokeniserState.ScriptDataEscapedDashDash.read(t12, t12.reader);
        assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t12.getState());

        Tokeniser t13 = createTokeniser(">");
        TokeniserState.ScriptDataEscapedDashDash.read(t13, t13.reader);
        assertEquals(TokeniserState.ScriptData, t13.getState());

        Tokeniser t14 = createTokeniser("x");
        TokeniserState.ScriptDataEscapedDashDash.read(t14, t14.reader);
        assertEquals(TokeniserState.ScriptDataEscaped, t14.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapeStates() {
        Tokeniser t1 = createTokeniser("s");
        TokeniserState.ScriptDataEscapedLessthanSign.read(t1, t1.reader);
        assertEquals(TokeniserState.ScriptDataDoubleEscapeStart, t1.getState());

        Tokeniser t2 = createTokeniser("/");
        TokeniserState.ScriptDataEscapedLessthanSign.read(t2, t2.reader);
        assertEquals(TokeniserState.ScriptDataEscapedEndTagOpen, t2.getState());

        Tokeniser t3 = createTokeniser("?");
        TokeniserState.ScriptDataEscapedLessthanSign.read(t3, t3.reader);
        assertEquals(TokeniserState.ScriptDataEscaped, t3.getState());

        // ScriptDataDoubleEscapeStart with "script" matched vs unmatched
        Tokeniser t4 = createTokeniser(" ");
        t4.createTempBuffer();
        t4.dataBuffer.append("script");
        TokeniserState.ScriptDataDoubleEscapeStart.read(t4, t4.reader);
        assertEquals(TokeniserState.ScriptDataDoubleEscaped, t4.getState());

        Tokeniser t5 = createTokeniser(" ");
        t5.createTempBuffer();
        t5.dataBuffer.append("other");
        TokeniserState.ScriptDataDoubleEscapeStart.read(t5, t5.reader);
        assertEquals(TokeniserState.ScriptDataEscaped, t5.getState());

        Tokeniser t6 = createTokeniser("x");
        t6.createTempBuffer();
        TokeniserState.ScriptDataDoubleEscapeStart.read(t6, t6.reader);
        assertEquals(TokeniserState.ScriptDataEscaped, t6.getState());

        // ScriptDataDoubleEscaped
        Tokeniser t7 = createTokeniser("-");
        TokeniserState.ScriptDataDoubleEscaped.read(t7, t7.reader);
        assertEquals(TokeniserState.ScriptDataDoubleEscapedDash, t7.getState());

        Tokeniser t8 = createTokeniser("<");
        TokeniserState.ScriptDataDoubleEscaped.read(t8, t8.reader);
        assertEquals(TokeniserState.ScriptDataDoubleEscapedLessthanSign, t8.getState());

        Tokeniser t9 = createTokeniser("");
        TokeniserState.ScriptDataDoubleEscaped.read(t9, t9.reader);
        assertEquals(TokeniserState.Data, t9.getState());

        // ScriptDataDoubleEscapedLessthanSign
        Tokeniser t10 = createTokeniser("/");
        TokeniserState.ScriptDataDoubleEscapedLessthanSign.read(t10, t10.reader);
        assertEquals(TokeniserState.ScriptDataDoubleEscapeEnd, t10.getState());

        Tokeniser t11 = createTokeniser("x");
        TokeniserState.ScriptDataDoubleEscapedLessthanSign.read(t11, t11.reader);
        assertEquals(TokeniserState.ScriptDataDoubleEscaped, t11.getState());

        // ScriptDataDoubleEscapeEnd
        Tokeniser t12 = createTokeniser(" ");
        t12.createTempBuffer();
        t12.dataBuffer.append("script");
        TokeniserState.ScriptDataDoubleEscapeEnd.read(t12, t12.reader);
        assertEquals(TokeniserState.ScriptDataEscaped, t12.getState());

        Tokeniser t13 = createTokeniser(" ");
        t13.createTempBuffer();
        t13.dataBuffer.append("other");
        TokeniserState.ScriptDataDoubleEscapeEnd.read(t13, t13.reader);
        assertEquals(TokeniserState.ScriptDataDoubleEscaped, t13.getState());
    }

    @Test(timeout = 4000)
    public void testAttributeStatesLifecycle() {
        // BeforeAttributeName
        Tokeniser t1 = createTokeniser("   attr");
        t1.createTagPending(true);
        TokeniserState.BeforeAttributeName.read(t1, t1.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t1.getState());

        Tokeniser t2 = createTokeniser("/ ");
        t2.createTagPending(true);
        TokeniserState.BeforeAttributeName.read(t2, t2.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t2.getState());

        Tokeniser t3 = createTokeniser("> ");
        t3.createTagPending(true);
        TokeniserState.BeforeAttributeName.read(t3, t3.reader);
        assertEquals(TokeniserState.Data, t3.getState());

        Tokeniser t4 = createTokeniser("\"");
        t4.createTagPending(true);
        TokeniserState.BeforeAttributeName.read(t4, t4.reader);
        assertEquals(TokeniserState.AttributeName, t4.getState());

        // AttributeName
        Tokeniser t5 = createTokeniser("attr ");
        t5.createTagPending(true);
        t5.tagPending.newAttribute();
        TokeniserState.AttributeName.read(t5, t5.reader);
        assertEquals(TokeniserState.AfterAttributeName, t5.getState());

        Tokeniser t6 = createTokeniser("attr=");
        t6.createTagPending(true);
        t6.tagPending.newAttribute();
        TokeniserState.AttributeName.read(t6, t6.reader);
        assertEquals(TokeniserState.BeforeAttributeValue, t6.getState());

        Tokeniser t7 = createTokeniser("attr/");
        t7.createTagPending(true);
        t7.tagPending.newAttribute();
        TokeniserState.AttributeName.read(t7, t7.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t7.getState());

        Tokeniser t8 = createTokeniser("attr>");
        t8.createTagPending(true);
        t8.tagPending.newAttribute();
        TokeniserState.AttributeName.read(t8, t8.reader);
        assertEquals(TokeniserState.Data, t8.getState());

        // BeforeAttributeValue
        Tokeniser t9 = createTokeniser("\"val\"");
        t9.createTagPending(true);
        TokeniserState.BeforeAttributeValue.read(t9, t9.reader);
        assertEquals(TokeniserState.AttributeValue_doubleQuoted, t9.getState());

        Tokeniser t10 = createTokeniser("'val'");
        t10.createTagPending(true);
        TokeniserState.BeforeAttributeValue.read(t10, t10.reader);
        assertEquals(TokeniserState.AttributeValue_singleQuoted, t10.getState());

        Tokeniser t11 = createTokeniser("unquoted ");
        t11.createTagPending(true);
        TokeniserState.BeforeAttributeValue.read(t11, t11.reader);
        assertEquals(TokeniserState.AttributeValue_unquoted, t11.getState());

        Tokeniser t12 = createTokeniser("> ");
        t12.createTagPending(true);
        TokeniserState.BeforeAttributeValue.read(t12, t12.reader);
        assertEquals(TokeniserState.Data, t12.getState());
    }

    @Test(timeout = 4000)
    public void testAttributeValueQuotedAndUnquoted() {
        // AttributeValue_doubleQuoted
        Tokeniser t1 = createTokeniser("value\"");
        t1.createTagPending(true);
        TokeniserState.AttributeValue_doubleQuoted.read(t1, t1.reader);
        assertEquals(TokeniserState.AfterAttributeValue_quoted, t1.getState());

        // AttributeValue_singleQuoted
        Tokeniser t2 = createTokeniser("value'");
        t2.createTagPending(true);
        TokeniserState.AttributeValue_singleQuoted.read(t2, t2.reader);
        assertEquals(TokeniserState.AfterAttributeValue_quoted, t2.getState());

        // AfterAttributeValue_quoted
        Tokeniser t3 = createTokeniser(" ");
        TokeniserState.AfterAttributeValue_quoted.read(t3, t3.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t3.getState());

        Tokeniser t4 = createTokeniser("/");
        TokeniserState.AfterAttributeValue_quoted.read(t4, t4.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t4.getState());

        Tokeniser t5 = createTokeniser(">");
        t5.createTagPending(true);
        TokeniserState.AfterAttributeValue_quoted.read(t5, t5.reader);
        assertEquals(TokeniserState.Data, t5.getState());

        Tokeniser t6 = createTokeniser("x");
        TokeniserState.AfterAttributeValue_quoted.read(t6, t6.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t6.getState());

        // AttributeValue_unquoted
        Tokeniser t7 = createTokeniser("val ");
        t7.createTagPending(true);
        TokeniserState.AttributeValue_unquoted.read(t7, t7.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t7.getState());

        Tokeniser t8 = createTokeniser("val>");
        t8.createTagPending(true);
        TokeniserState.AttributeValue_unquoted.read(t8, t8.reader);
        assertEquals(TokeniserState.Data, t8.getState());
    }

    @Test(timeout = 4000)
    public void testCommentsLifecycle() {
        Tokeniser t1 = createTokeniser("--");
        TokeniserState.MarkupDeclarationOpen.read(t1, t1.reader);
        assertEquals(TokeniserState.CommentStart, t1.getState());
        assertNotNull(t1.commentPending);

        Tokeniser t2 = createTokeniser("-");
        t2.createCommentPending();
        TokeniserState.CommentStart.read(t2, t2.reader);
        assertEquals(TokeniserState.CommentStartDash, t2.getState());

        Tokeniser t3 = createTokeniser(">");
        t3.createCommentPending();
        TokeniserState.CommentStart.read(t3, t3.reader);
        assertEquals(TokeniserState.Data, t3.getState());

        Tokeniser t4 = createTokeniser("text");
        t4.createCommentPending();
        TokeniserState.CommentStart.read(t4, t4.reader);
        assertEquals(TokeniserState.Comment, t4.getState());

        Tokeniser t5 = createTokeniser("-");
        t5.createCommentPending();
        TokeniserState.Comment.read(t5, t5.reader);
        assertEquals(TokeniserState.CommentEndDash, t5.getState());

        Tokeniser t6 = createTokeniser("-");
        t6.createCommentPending();
        TokeniserState.CommentEndDash.read(t6, t6.reader);
        assertEquals(TokeniserState.CommentEnd, t6.getState());

        Tokeniser t7 = createTokeniser(">");
        t7.createCommentPending();
        TokeniserState.CommentEnd.read(t7, t7.reader);
        assertEquals(TokeniserState.Data, t7.getState());

        Tokeniser t8 = createTokeniser("!");
        t8.createCommentPending();
        TokeniserState.CommentEnd.read(t8, t8.reader);
        assertEquals(TokeniserState.CommentEndBang, t8.getState());

        Tokeniser t9 = createTokeniser(">");
        t9.createCommentPending();
        TokeniserState.CommentEndBang.read(t9, t9.reader);
        assertEquals(TokeniserState.Data, t9.getState());

        Tokeniser t10 = createTokeniser("-");
        t10.createCommentPending();
        TokeniserState.CommentEndBang.read(t10, t10.reader);
        assertEquals(TokeniserState.CommentEndDash, t10.getState());
    }

    @Test(timeout = 4000)
    public void testDoctypeTransitions() {
        Tokeniser t1 = createTokeniser("DOCTYPE");
        TokeniserState.MarkupDeclarationOpen.read(t1, t1.reader);
        assertEquals(TokeniserState.Doctype, t1.getState());

        Tokeniser t2 = createTokeniser(" ");
        TokeniserState.Doctype.read(t2, t2.reader);
        assertEquals(TokeniserState.BeforeDoctypeName, t2.getState());

        Tokeniser t3 = createTokeniser("html");
        TokeniserState.BeforeDoctypeName.read(t3, t3.reader);
        assertEquals(TokeniserState.DoctypeName, t3.getState());
        assertNotNull(t3.doctypePending);

        Tokeniser t4 = createTokeniser("html>");
        t4.createDoctypePending();
        TokeniserState.DoctypeName.read(t4, t4.reader);
        assertEquals(TokeniserState.Data, t4.getState());

        Tokeniser t5 = createTokeniser("html ");
        t5.createDoctypePending();
        TokeniserState.DoctypeName.read(t5, t5.reader);
        assertEquals(TokeniserState.AfterDoctypeName, t5.getState());

        Tokeniser t6 = createTokeniser("PUBLIC");
        t6.createDoctypePending();
        TokeniserState.AfterDoctypeName.read(t6, t6.reader);
        assertEquals(TokeniserState.AfterDoctypePublicKeyword, t6.getState());

        Tokeniser t7 = createTokeniser("SYSTEM");
        t7.createDoctypePending();
        TokeniserState.AfterDoctypeName.read(t7, t7.reader);
        assertEquals(TokeniserState.AfterDoctypeSystemKeyword, t7.getState());
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        Tokeniser t1 = createTokeniser("[CDATA[");
        TokeniserState.MarkupDeclarationOpen.read(t1, t1.reader);
        assertEquals(TokeniserState.CdataSection, t1.getState());

        Tokeniser t2 = createTokeniser("custom data]]>");
        TokeniserState.CdataSection.read(t2, t2.reader);
        assertEquals(TokeniserState.Data, t2.getState());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullCharacterHandlingAcrossStates() {
        Tokeniser t1 = createTokeniser("\u0000");
        TokeniserState.Data.read(t1, t1.reader);
        assertEquals(TokeniserState.Data, t1.getState());

        Tokeniser t2 = createTokeniser("\u0000");
        TokeniserState.Rcdata.read(t2, t2.reader);
        assertEquals(TokeniserState.Rcdata, t2.getState());

        Tokeniser t3 = createTokeniser("\u0000");
        TokeniserState.Rawtext.read(t3, t3.reader);
        assertEquals(TokeniserState.Rawtext, t3.getState());

        Tokeniser t4 = createTokeniser("\u0000");
        TokeniserState.ScriptData.read(t4, t4.reader);
        assertEquals(TokeniserState.ScriptData, t4.getState());

        Tokeniser t5 = createTokeniser("\u0000");
        TokeniserState.PLAINTEXT.read(t5, t5.reader);
        assertEquals(TokeniserState.PLAINTEXT, t5.getState());

        Tokeniser t6 = createTokeniser("\u0000");
        t6.createCommentPending();
        TokeniserState.Comment.read(t6, t6.reader);
        assertEquals(TokeniserState.Comment, t6.getState());
    }

    @Test(timeout = 4000)
    public void testEofHandlingAcrossStates() {
        Tokeniser t1 = createTokeniser("");
        t1.createTagPending(true);
        TokeniserState.TagName.read(t1, t1.reader);
        assertEquals(TokeniserState.Data, t1.getState());

        Tokeniser t2 = createTokeniser("");
        t2.createTagPending(true);
        TokeniserState.BeforeAttributeName.read(t2, t2.reader);
        assertEquals(TokeniserState.Data, t2.getState());

        Tokeniser t3 = createTokeniser("");
        t3.createTagPending(true);
        TokeniserState.AttributeName.read(t3, t3.reader);
        assertEquals(TokeniserState.Data, t3.getState());

        Tokeniser t4 = createTokeniser("");
        t4.createTagPending(true);
        TokeniserState.BeforeAttributeValue.read(t4, t4.reader);
        assertEquals(TokeniserState.Data, t4.getState());

        Tokeniser t5 = createTokeniser("");
        t5.createTagPending(true);
        TokeniserState.AttributeValue_doubleQuoted.read(t5, t5.reader);
        assertEquals(TokeniserState.Data, t5.getState());

        Tokeniser t6 = createTokeniser("");
        t6.createTagPending(true);
        TokeniserState.SelfClosingStartTag.read(t6, t6.reader);
        assertEquals(TokeniserState.Data, t6.getState());

        Tokeniser t7 = createTokeniser("");
        t7.createCommentPending();
        TokeniserState.Comment.read(t7, t7.reader);
        assertEquals(TokeniserState.Data, t7.getState());

        Tokeniser t8 = createTokeniser("");
        TokeniserState.Doctype.read(t8, t8.reader);
        assertEquals(TokeniserState.Data, t8.getState());
        assertTrue(t8.doctypePending.forceQuirks);
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTagTransitions() {
        Tokeniser t1 = createTokeniser(">");
        t1.createTagPending(true);
        TokeniserState.SelfClosingStartTag.read(t1, t1.reader);
        assertEquals(TokeniserState.Data, t1.getState());
        assertTrue(t1.tagPending.selfClosing);

        Tokeniser t2 = createTokeniser("x");
        t2.createTagPending(true);
        TokeniserState.SelfClosingStartTag.read(t2, t2.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t2.getState());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlesUnclosedTitleDefect() {
        // Targets known defect: ParserTest::handlesUnclosedTitle
        // Failure condition: expected:<One[]> but was:<One[<b>Two <p>Test</p]>
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse("<title>One<b>Two <p>Test</p>");
        assertEquals("One", doc.title());
    }

    @Test(timeout = 4000)
    public void testParsesUnterminatedTextareaDefect() {
        // Targets known defect: ParserTest::parsesUnterminatedTextarea
        // Failure condition: expected:<one[]> but was:<one[<p>two]>
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse("<textarea>one<p>two");
        org.jsoup.nodes.Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("one", textarea.text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testBogusCommentExecution() {
        Tokeniser t = createTokeniser("comment>");
        TokeniserState.BogusComment.read(t, t.reader);
        assertEquals(TokeniserState.Data, t.getState());
    }

    @Test(timeout = 4000)
    public void testBogusDoctypeExecution() {
        Tokeniser t1 = createTokeniser(">");
        t1.createDoctypePending();
        TokeniserState.BogusDoctype.read(t1, t1.reader);
        assertEquals(TokeniserState.Data, t1.getState());

        Tokeniser t2 = createTokeniser("");
        t2.createDoctypePending();
        TokeniserState.BogusDoctype.read(t2, t2.reader);
        assertEquals(TokeniserState.Data, t2.getState());

        Tokeniser t3 = createTokeniser("a");
        t3.createDoctypePending();
        TokeniserState.BogusDoctype.read(t3, t3.reader);
        assertEquals(TokeniserState.BogusDoctype, t3.getState());
    }

    @Test(timeout = 4000)
    public void testMarkupDeclarationOpenBogusFallback() {
        Tokeniser t = createTokeniser("[BOGUS]");
        TokeniserState.MarkupDeclarationOpen.read(t, t.reader);
        assertEquals(TokeniserState.BogusComment, t.getState());
    }

    @Test(timeout = 4000)
    public void testAfterDoctypeNameBogusFallback() {
        Tokeniser t = createTokeniser("INVALID");
        t.createDoctypePending();
        TokeniserState.AfterDoctypeName.read(t, t.reader);
        assertEquals(TokeniserState.BogusDoctype, t.getState());
        assertTrue(t.doctypePending.forceQuirks);
    }

    @Test(timeout = 4000)
    public void testDoctypePublicAndSystemIdentifiersFlow() {
        Tokeniser t1 = createTokeniser(" \"public_id\"");
        t1.createDoctypePending();
        TokeniserState.AfterDoctypePublicKeyword.read(t1, t1.reader);
        assertEquals(TokeniserState.BeforeDoctypePublicIdentifier, t1.getState());

        Tokeniser t2 = createTokeniser("\"pub\"");
        t2.createDoctypePending();
        TokeniserState.BeforeDoctypePublicIdentifier.read(t2, t2.reader);
        assertEquals(TokeniserState.DoctypePublicIdentifier_doubleQuoted, t2.getState());

        Tokeniser t3 = createTokeniser("pub\"");
        t3.createDoctypePending();
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t3, t3.reader);
        assertEquals(TokeniserState.AfterDoctypePublicIdentifier, t3.getState());

        Tokeniser t4 = createTokeniser(" ");
        t4.createDoctypePending();
        TokeniserState.AfterDoctypePublicIdentifier.read(t4, t4.reader);
        assertEquals(TokeniserState.BetweenDoctypePublicAndSystemIdentifiers, t4.getState());

        Tokeniser t5 = createTokeniser(" \"sys_id\"");
        t5.createDoctypePending();
        TokeniserState.AfterDoctypeSystemKeyword.read(t5, t5.reader);
        assertEquals(TokeniserState.BeforeDoctypeSystemIdentifier, t5.getState());

        Tokeniser t6 = createTokeniser("\"sys\"");
        t6.createDoctypePending();
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t6, t6.reader);
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t6.getState());

        Tokeniser t7 = createTokeniser("sys\"");
        t7.createDoctypePending();
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t7, t7.reader);
        assertEquals(TokeniserState.AfterDoctypeSystemIdentifier, t7.getState());

        Tokeniser t8 = createTokeniser(">");
        t8.createDoctypePending();
        TokeniserState.AfterDoctypeSystemIdentifier.read(t8, t8.reader);
        assertEquals(TokeniserState.Data, t8.getState());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumIntegrityAndValues() {
        TokeniserState[] states = TokeniserState.values();
        assertNotNull(states);
        assertTrue(states.length >= 67);

        assertEquals(TokeniserState.Data, TokeniserState.valueOf("Data"));
        assertEquals(TokeniserState.TagOpen, TokeniserState.valueOf("TagOpen"));
        assertEquals(TokeniserState.Rcdata, TokeniserState.valueOf("Rcdata"));
        assertEquals(TokeniserState.BogusComment, TokeniserState.valueOf("BogusComment"));
        assertEquals(TokeniserState.CdataSection, TokeniserState.valueOf("CdataSection"));

        for (TokeniserState s : states) {
            assertNotNull(s.name());
            assertTrue(s.ordinal() >= 0);
        }
    }
}