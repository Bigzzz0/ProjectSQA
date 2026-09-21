package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokeniserState enum, specifically the state machine for HTML tokenization.
 * 
 * Defect: In the DoctypeSystemIdentifier_doubleQuoted state, when a '>' is encountered,
 * the code incorrectly emits the pending doctype with forceQuirks=true, but the bug
 * causes the XML declaration to be parsed as a comment instead of a proper doctype.
 * The ground truth failure shows: expected:<<[?xml encoding='UTF-8' ?]>...> but was:<<[!--?xml encoding='UTF-8' ?--]>...
 * This indicates the tokeniser is incorrectly entering BogusComment state instead of
 * handling the doctype system identifier correctly.
 * 
 * Branch coverage targets:
 * - DoctypeSystemIdentifier_doubleQuoted: all switch cases (", nullChar, >, eof, default)
 * - AfterDoctypePublicIdentifier: whitespace, >, ", ', eof, default
 * - BeforeDoctypeName: whitespace, letter, nullChar, eof, default
 * - DoctypeName: letter sequence, whitespace, >, nullChar, eof
 * - CommentStart: -, nullChar, >, eof, default
 * - CommentEnd: >, nullChar, !, -, eof, default
 * - CommentEndBang: -, >, nullChar, eof, default
 * - AttributeValue_unquoted: whitespace, &, >, nullChar, eof, special chars
 * - AfterAttributeValue_quoted: whitespace, /, >, eof, default
 * - SelfClosingStartTag: >, eof, default
 * - ScriptData states: various transitions
 * 
 * Boundary conditions:
 * - Empty input, single character, long sequences
 * - Whitespace variations (\t\n\r\f )
 * - Quoting styles (single/double)
 * - EOF handling
 * - Null character handling
 * - Case sensitivity (PUBLIC/SYSTEM keywords)
 */
public class TokeniserStateDeepseekTest {

    // Helper to create a tokeniser with given input
    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader, null);
        return tokeniser;
    }

    // ==================== PARTITION A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDataState_emitsText() {
        Tokeniser t = createTokeniser("hello world");
        t.read();
        assertEquals("hello world", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testDataState_emitsTagOpen() {
        Tokeniser t = createTokeniser("<div>");
        t.read();
        assertTrue(t.isTagPending());
    }

    @Test(timeout = 4000)
    public void testDataState_emitsEOF() {
        Tokeniser t = createTokeniser("");
        t.read();
        assertTrue(t.isEOF());
    }

    @Test(timeout = 4000)
    public void testTagName_emitsTag() {
        Tokeniser t = createTokeniser("<div>");
        t.read();
        assertEquals("div", t.tagPending.name.toString());
    }

    @Test(timeout = 4000)
    public void testTagName_handlesWhitespace() {
        Tokeniser t = createTokeniser("<div class=\"test\">");
        t.read();
        assertEquals("div", t.tagPending.name.toString());
        assertEquals("class", t.tagPending.attributes.get(0).getName());
        assertEquals("test", t.tagPending.attributes.get(0).getValue());
    }

    @Test(timeout = 4000)
    public void testDoctype_emitsDoctype() {
        Tokeniser t = createTokeniser("<!DOCTYPE html>");
        t.read();
        assertTrue(t.isDoctypePending());
        assertEquals("html", t.doctypePending.name.toString());
    }

    @Test(timeout = 4000)
    public void testDoctype_handlesPublicIdentifier() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">");
        t.read();
        assertTrue(t.isDoctypePending());
        assertEquals("html", t.doctypePending.name.toString());
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", t.doctypePending.publicIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctype_handlesSystemIdentifier() {
        Tokeniser t = createTokeniser("<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        t.read();
        assertTrue(t.isDoctypePending());
        assertEquals("html", t.doctypePending.name.toString());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testComment_emitsComment() {
        Tokeniser t = createTokeniser("<!-- comment -->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" comment ", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testComment_handlesEmpty() {
        Tokeniser t = createTokeniser("<!---->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals("", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testComment_handlesDash() {
        Tokeniser t = createTokeniser("<!--->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals("-", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testComment_handlesDoubleDash() {
        Tokeniser t = createTokeniser("<!-- -- -->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" -- ", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testScriptData_handlesEscaped() {
        Tokeniser t = createTokeniser("<script>var x = 1;</script>");
        t.read();
        assertTrue(t.isTagPending());
        assertEquals("script", t.tagPending.name.toString());
    }

    @Test(timeout = 4000)
    public void testScriptData_handlesDoubleEscaped() {
        Tokeniser t = createTokeniser("<script><!--<script>--></script>");
        t.read();
        assertTrue(t.isTagPending());
        assertEquals("script", t.tagPending.name.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeValue_unquoted() {
        Tokeniser t = createTokeniser("<div class=test>");
        t.read();
        assertEquals("test", t.tagPending.attributes.get(0).getValue());
    }

    @Test(timeout = 4000)
    public void testAttributeValue_singleQuoted() {
        Tokeniser t = createTokeniser("<div class='test'>");
        t.read();
        assertEquals("test", t.tagPending.attributes.get(0).getValue());
    }

    @Test(timeout = 4000)
    public void testAttributeValue_doubleQuoted() {
        Tokeniser t = createTokeniser("<div class=\"test\">");
        t.read();
        assertEquals("test", t.tagPending.attributes.get(0).getValue());
    }

    @Test(timeout = 4000)
    public void testAttributeValue_handlesEntities() {
        Tokeniser t = createTokeniser("<div class=\"a&amp;b\">");
        t.read();
        assertEquals("a&b", t.tagPending.attributes.get(0).getValue());
    }

    @Test(timeout = 4000)
    public void testAttributeValue_handlesNullChar() {
        Tokeniser t = createTokeniser("<div class=\"a\u0000b\">");
        t.read();
        assertEquals("a\uFFFDb", t.tagPending.attributes.get(0).getValue());
    }

    @Test(timeout = 4000)
    public void testAttributeValue_handlesEOF() {
        Tokeniser t = createTokeniser("<div class=\"test");
        t.read();
        assertTrue(t.isEOF());
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValue_quoted_handlesWhitespace() {
        Tokeniser t = createTokeniser("<div class=\"test\" >");
        t.read();
        assertEquals("test", t.tagPending.attributes.get(0).getValue());
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValue_quoted_handlesSlash() {
        Tokeniser t = createTokeniser("<div class=\"test\"/>");
        t.read();
        assertTrue(t.tagPending.selfClosing);
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValue_quoted_handlesGT() {
        Tokeniser t = createTokeniser("<div class=\"test\">");
        t.read();
        assertFalse(t.isTagPending());
    }

    @Test(timeout = 4000)
    public void testAfterAttributeValue_quoted_handlesEOF() {
        Tokeniser t = createTokeniser("<div class=\"test\"");
        t.read();
        assertTrue(t.isEOF());
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTag_handlesGT() {
        Tokeniser t = createTokeniser("<br/>");
        t.read();
        assertTrue(t.tagPending.selfClosing);
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTag_handlesEOF() {
        Tokeniser t = createTokeniser("<br/");
        t.read();
        assertTrue(t.isEOF());
    }

    @Test(timeout = 4000)
    public void testSelfClosingStartTag_handlesDefault() {
        Tokeniser t = createTokeniser("<br/ ");
        t.read();
        assertFalse(t.isEOF());
    }

    // ==================== PARTITION B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyInput() {
        Tokeniser t = createTokeniser("");
        t.read();
        assertTrue(t.isEOF());
    }

    @Test(timeout = 4000)
    public void testSingleCharInput() {
        Tokeniser t = createTokeniser("a");
        t.read();
        assertEquals("a", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyInput() {
        Tokeniser t = createTokeniser("   ");
        t.read();
        assertEquals("   ", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testNullCharInput() {
        Tokeniser t = createTokeniser("\u0000");
        t.read();
        assertEquals("\uFFFD", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testLongInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('a');
        }
        Tokeniser t = createTokeniser(sb.toString());
        t.read();
        assertEquals(sb.toString(), t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testMaxCharInput() {
        Tokeniser t = createTokeniser("\uFFFF");
        t.read();
        assertEquals("\uFFFF", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testMinCharInput() {
        Tokeniser t = createTokeniser("\u0000");
        t.read();
        assertEquals("\uFFFD", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testTabCharInput() {
        Tokeniser t = createTokeniser("\t");
        t.read();
        assertEquals("\t", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testNewlineCharInput() {
        Tokeniser t = createTokeniser("\n");
        t.read();
        assertEquals("\n", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testCarriageReturnInput() {
        Tokeniser t = createTokeniser("\r");
        t.read();
        assertEquals("\r", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testFormFeedInput() {
        Tokeniser t = createTokeniser("\f");
        t.read();
        assertEquals("\f", t.getPendingData());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithNoName() {
        Tokeniser t = createTokeniser("<!DOCTYPE>");
        t.read();
        assertTrue(t.isDoctypePending());
        assertEquals("", t.doctypePending.name.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithOnlyWhitespace() {
        Tokeniser t = createTokeniser("<!DOCTYPE   >");
        t.read();
        assertTrue(t.isDoctypePending());
        assertEquals("", t.doctypePending.name.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithForceQuirks() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC \"\">");
        t.read();
        assertTrue(t.doctypePending.forceQuirks);
    }

    @Test(timeout = 4000)
    public void testDoctypeWithSystemIdentifier() {
        Tokeniser t = createTokeniser("<!DOCTYPE html SYSTEM \"http://example.com\">");
        t.read();
        assertEquals("http://example.com", t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithPublicIdentifier() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">");
        t.read();
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", t.doctypePending.publicIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithNullChar() {
        Tokeniser t = createTokeniser("<!--\u0000-->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals("\uFFFD", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOF() {
        Tokeniser t = createTokeniser("<!--");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals("", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithOnlyDash() {
        Tokeniser t = createTokeniser("<!--->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals("-", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithDoubleDash() {
        Tokeniser t = createTokeniser("<!-- -- -->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" -- ", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithBang() {
        Tokeniser t = createTokeniser("<!--! -->");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals("! ", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDash() {
        Tokeniser t = createTokeniser("<!-- -");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" -", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDoubleDash() {
        Tokeniser t = createTokeniser("<!-- --");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterBang() {
        Tokeniser t = createTokeniser("<!--! ");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals("! ", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashBang() {
        Tokeniser t = createTokeniser("<!-- -!");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" -!", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDoubleDashBang() {
        Tokeniser t = createTokeniser("<!-- --!");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterTripleDash() {
        Tokeniser t = createTokeniser("<!-- ---");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" ---", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterQuadrupleDash() {
        Tokeniser t = createTokeniser("<!-- ----");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" ----", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBang() {
        Tokeniser t = createTokeniser("<!-- --! ");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --! ", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDash() {
        Tokeniser t = createTokeniser("<!-- --!-");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash() {
        Tokeniser t = createTokeniser("<!-- --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        t.read();
        assertTrue(t.isCommentPending());
        assertEquals(" --!-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", t.commentPending.data.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithEOFAfterDashDashBangDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDashDash