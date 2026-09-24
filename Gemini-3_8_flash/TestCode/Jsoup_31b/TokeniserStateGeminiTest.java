/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.TokeniserState
 *
 * 1. Defect-Targeted Branches (Defects4J: XmlTreeBuilderTest::handlesXmlDeclarationAsDeclaration):
 *    - BogusComment state transition from TagOpen ('?')
 *    - TokeniserState.TagOpen -> case '?': t.advanceTransition(BogusComment)
 *    - TokeniserState.BogusComment -> emits Token.Comment for XML declaration <?xml ... ?>
 *    - Verifies proper handling and preservation of XML declaration tokens vs standard comments.
 *
 * 2. State & Decision Branch Coverage:
 *    - Data, CharacterReferenceInData (ref null vs ref char, nullChar, eof, data)
 *    - Rcdata, CharacterReferenceInRcdata, RcdataLessthanSign, RCDATAEndTagOpen, RCDATAEndTagName
 *      (matches '/', matches letter, appropriate end tag match vs mismatch, whitespace, '/', '>', anythingElse)
 *    - Rawtext, RawtextLessthanSign, RawtextEndTagOpen, RawtextEndTagName
 *    - ScriptData, ScriptDataLessthanSign, ScriptDataEndTagOpen, ScriptDataEndTagName
 *    - ScriptDataEscapeStart, ScriptDataEscapeStartDash, ScriptDataEscaped, ScriptDataEscapedDash,
 *      ScriptDataEscapedDashDash, ScriptDataEscapedLessthanSign, ScriptDataEscapedEndTagOpen,
 *      ScriptDataEscapedEndTagName, ScriptDataDoubleEscapeStart, ScriptDataDoubleEscaped,
 *      ScriptDataDoubleEscapedDash, ScriptDataDoubleEscapedDashDash, ScriptDataDoubleEscapedLessthanSign,
 *      ScriptDataDoubleEscapeEnd ("script" name matching vs non-matching)
 *    - PLAINTEXT (nullChar, eof, data)
 *    - TagOpen ('!', '/', '?', matchesLetter, default invalid char error)
 *    - EndTagOpen (empty/eof, letter, '>', default bogus comment)
 *    - TagName (whitespace, '/', '>', nullChar replacement, eof error)
 *    - BeforeAttributeName, AttributeName, AfterAttributeName
 *    - BeforeAttributeValue, AttributeValue_doubleQuoted, AttributeValue_singleQuoted, AttributeValue_unquoted
 *    - AfterAttributeValue_quoted, SelfClosingStartTag
 *    - BogusComment, MarkupDeclarationOpen ("--", "DOCTYPE", "[CDATA[", bogus comment fallback)
 *    - CommentStart, CommentStartDash, Comment, CommentEndDash, CommentEnd, CommentEndBang
 *    - Doctype, BeforeDoctypeName, DoctypeName, AfterDoctypeName ("PUBLIC", "SYSTEM", '>', bogus)
 *    - AfterDoctypePublicKeyword, BeforeDoctypePublicIdentifier, DoctypePublicIdentifier_doubleQuoted,
 *      DoctypePublicIdentifier_singleQuoted, AfterDoctypePublicIdentifier, BetweenDoctypePublicAndSystemIdentifiers
 *    - AfterDoctypeSystemKeyword, BeforeDoctypeSystemIdentifier, DoctypeSystemIdentifier_doubleQuoted,
 *      DoctypeSystemIdentifier_singleQuoted, AfterDoctypeSystemIdentifier, BogusDoctype
 *    - CdataSection (data before "]]>", consumption of "]]>", transition to Data)
 *    - Enum integrity (values, valueOf)
 */
package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserStateGeminiTest {

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        return new Tokeniser(reader, ParseErrorList.tracking(50));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets XmlTreeBuilderTest::handlesXmlDeclarationAsDeclaration defect:
     * When parsing XML declarations starting with '<?', TagOpen branches to BogusComment.
     * BogusComment must emit a comment that the XML parser recognizes as an XML declaration
     * rather than formatting as a regular HTML/XML comment (<!--?xml ... ?-->).
     */
    @Test(timeout = 4000)
    public void testHandlesXmlDeclarationAsDeclarationDefect() {
        String xml = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<?xml encoding='UTF-8'?> <body> One </body> <!-- comment -->",
                org.jsoup.helper.StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

    @Test(timeout = 4000)
    public void testTagOpenToBogusCommentOnQuestionMark() {
        CharacterReader r = new CharacterReader("?xml version='1.0'?>rest");
        Tokeniser t = createTokeniser("");
        TokeniserState.TagOpen.read(t, r);

        assertEquals(TokeniserState.BogusComment, t.getState());
        Token token = t.read();
        assertTrue(token instanceof Token.Comment);
        Token.Comment comment = (Token.Comment) token;
        assertEquals("?xml version='1.0'?", comment.getData());
        assertEquals(TokeniserState.Data, t.getState());
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDataStateBranches() {
        // Switch branch: '&'
        CharacterReader rAmp = new CharacterReader("&amp;");
        Tokeniser tAmp = createTokeniser("");
        TokeniserState.Data.read(tAmp, rAmp);
        assertEquals(TokeniserState.CharacterReferenceInData, tAmp.getState());

        // Switch branch: '<'
        CharacterReader rLt = new CharacterReader("<div>");
        Tokeniser tLt = createTokeniser("");
        TokeniserState.Data.read(tLt, rLt);
        assertEquals(TokeniserState.TagOpen, tLt.getState());

        // Switch branch: nullChar
        CharacterReader rNull = new CharacterReader("\u0000text");
        Tokeniser tNull = createTokeniser("");
        TokeniserState.Data.read(tNull, rNull);
        assertEquals(1, tNull.getErrors().size());
        Token tokNull = tNull.read();
        assertTrue(tokNull instanceof Token.Character);
        assertEquals("\u0000", ((Token.Character) tokNull).getData());

        // Switch branch: EOF
        CharacterReader rEof = new CharacterReader("");
        Tokeniser tEof = createTokeniser("");
        TokeniserState.Data.read(tEof, rEof);
        Token tokEof = tEof.read();
        assertTrue(tokEof instanceof Token.EOF);

        // Switch branch: default text
        CharacterReader rText = new CharacterReader("Hello World<");
        Tokeniser tText = createTokeniser("");
        TokeniserState.Data.read(tText, rText);
        Token tokText = tText.read();
        assertTrue(tokText instanceof Token.Character);
        assertEquals("Hello World", ((Token.Character) tokText).getData());
    }

    @Test(timeout = 4000)
    public void testCharacterReferenceInData() {
        CharacterReader rValid = new CharacterReader("lt;rest");
        Tokeniser tValid = createTokeniser("");
        TokeniserState.CharacterReferenceInData.read(tValid, rValid);
        assertEquals(TokeniserState.Data, tValid.getState());
        Token tok = tValid.read();
        assertEquals("<", ((Token.Character) tok).getData());

        CharacterReader rInvalid = new CharacterReader("notaref;");
        Tokeniser tInvalid = createTokeniser("");
        TokeniserState.CharacterReferenceInData.read(tInvalid, rInvalid);
        assertEquals(TokeniserState.Data, tInvalid.getState());
        Token tokAmp = tInvalid.read();
        assertEquals("&", ((Token.Character) tokAmp).getData());
    }

    @Test(timeout = 4000)
    public void testRcdataStateBranches() {
        // '&' branch
        CharacterReader rAmp = new CharacterReader("&");
        Tokeniser tAmp = createTokeniser("");
        TokeniserState.Rcdata.read(tAmp, rAmp);
        assertEquals(TokeniserState.CharacterReferenceInRcdata, tAmp.getState());

        // '<' branch
        CharacterReader rLt = new CharacterReader("<");
        Tokeniser tLt = createTokeniser("");
        TokeniserState.Rcdata.read(tLt, rLt);
        assertEquals(TokeniserState.RcdataLessthanSign, tLt.getState());

        // nullChar branch
        CharacterReader rNull = new CharacterReader("\u0000");
        Tokeniser tNull = createTokeniser("");
        TokeniserState.Rcdata.read(tNull, rNull);
        assertEquals(1, tNull.getErrors().size());
        Token tok = tNull.read();
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) tok).getData());

        // EOF branch
        CharacterReader rEof = new CharacterReader("");
        Tokeniser tEof = createTokeniser("");
        TokeniserState.Rcdata.read(tEof, rEof);
        Token tokEof = tEof.read();
        assertTrue(tokEof instanceof Token.EOF);

        // default branch
        CharacterReader rDef = new CharacterReader("Text content<");
        Tokeniser tDef = createTokeniser("");
        TokeniserState.Rcdata.read(tDef, rDef);
        Token tokDef = tDef.read();
        assertEquals("Text content", ((Token.Character) tokDef).getData());
    }

    @Test(timeout = 4000)
    public void testCharacterReferenceInRcdata() {
        CharacterReader rValid = new CharacterReader("gt;rest");
        Tokeniser tValid = createTokeniser("");
        TokeniserState.CharacterReferenceInRcdata.read(tValid, rValid);
        assertEquals(TokeniserState.Rcdata, tValid.getState());
        Token tok = tValid.read();
        assertEquals(">", ((Token.Character) tok).getData());

        CharacterReader rInvalid = new CharacterReader("zzz;");
        Tokeniser tInvalid = createTokeniser("");
        TokeniserState.CharacterReferenceInRcdata.read(tInvalid, rInvalid);
        assertEquals(TokeniserState.Rcdata, tInvalid.getState());
        Token tokAmp = tInvalid.read();
        assertEquals("&", ((Token.Character) tokAmp).getData());
    }

    @Test(timeout = 4000)
    public void testRawtextStateBranches() {
        CharacterReader rLt = new CharacterReader("<");
        Tokeniser tLt = createTokeniser("");
        TokeniserState.Rawtext.read(tLt, rLt);
        assertEquals(TokeniserState.RawtextLessthanSign, tLt.getState());

        CharacterReader rNull = new CharacterReader("\u0000");
        Tokeniser tNull = createTokeniser("");
        TokeniserState.Rawtext.read(tNull, rNull);
        assertEquals(1, tNull.getErrors().size());
        Token tok = tNull.read();
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) tok).getData());

        CharacterReader rEof = new CharacterReader("");
        Tokeniser tEof = createTokeniser("");
        TokeniserState.Rawtext.read(tEof, rEof);
        assertTrue(tEof.read() instanceof Token.EOF);

        CharacterReader rDef = new CharacterReader("Raw Text<");
        Tokeniser tDef = createTokeniser("");
        TokeniserState.Rawtext.read(tDef, rDef);
        assertEquals("Raw Text", ((Token.Character) tDef.read()).getData());
    }

    @Test(timeout = 4000)
    public void testScriptDataStateBranches() {
        CharacterReader rLt = new CharacterReader("<");
        Tokeniser tLt = createTokeniser("");
        TokeniserState.ScriptData.read(tLt, rLt);
        assertEquals(TokeniserState.ScriptDataLessthanSign, tLt.getState());

        CharacterReader rNull = new CharacterReader("\u0000");
        Tokeniser tNull = createTokeniser("");
        TokeniserState.ScriptData.read(tNull, rNull);
        assertEquals(1, tNull.getErrors().size());
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) tNull.read()).getData());

        CharacterReader rEof = new CharacterReader("");
        Tokeniser tEof = createTokeniser("");
        TokeniserState.ScriptData.read(tEof, rEof);
        assertTrue(tEof.read() instanceof Token.EOF);

        CharacterReader rDef = new CharacterReader("var x = 1;<");
        Tokeniser tDef = createTokeniser("");
        TokeniserState.ScriptData.read(tDef, rDef);
        assertEquals("var x = 1;", ((Token.Character) tDef.read()).getData());
    }

    @Test(timeout = 4000)
    public void testPLAINTEXTStateBranches() {
        CharacterReader rNull = new CharacterReader("\u0000");
        Tokeniser tNull = createTokeniser("");
        TokeniserState.PLAINTEXT.read(tNull, rNull);
        assertEquals(1, tNull.getErrors().size());
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) tNull.read()).getData());

        CharacterReader rEof = new CharacterReader("");
        Tokeniser tEof = createTokeniser("");
        TokeniserState.PLAINTEXT.read(tEof, rEof);
        assertTrue(tEof.read() instanceof Token.EOF);

        CharacterReader rDef = new CharacterReader("Some plain text\u0000after");
        Tokeniser tDef = createTokeniser("");
        TokeniserState.PLAINTEXT.read(tDef, rDef);
        assertEquals("Some plain text", ((Token.Character) tDef.read()).getData());
    }

    // =========================================================================
    // PARTITION B: Tags, Attributes, ScriptData & RCDATA Branching
    // =========================================================================

    @Test(timeout = 4000)
    public void testTagOpenBranches() {
        Tokeniser t1 = createTokeniser("");
        TokeniserState.TagOpen.read(t1, new CharacterReader("!"));
        assertEquals(TokeniserState.MarkupDeclarationOpen, t1.getState());

        Tokeniser t2 = createTokeniser("");
        TokeniserState.TagOpen.read(t2, new CharacterReader("/"));
        assertEquals(TokeniserState.EndTagOpen, t2.getState());

        Tokeniser t3 = createTokeniser("");
        TokeniserState.TagOpen.read(t3, new CharacterReader("div"));
        assertEquals(TokeniserState.TagName, t3.getState());
        assertTrue(t3.tagPending instanceof Token.StartTag);

        Tokeniser t4 = createTokeniser("");
        TokeniserState.TagOpen.read(t4, new CharacterReader("123"));
        assertEquals(TokeniserState.Data, t4.getState());
        assertEquals(1, t4.getErrors().size());
        assertEquals("<", ((Token.Character) t4.read()).getData());
    }

    @Test(timeout = 4000)
    public void testEndTagOpenBranches() {
        Tokeniser tEmpty = createTokeniser("");
        TokeniserState.EndTagOpen.read(tEmpty, new CharacterReader(""));
        assertEquals(TokeniserState.Data, tEmpty.getState());
        assertEquals(1, tEmpty.getErrors().size());
        assertEquals("</", ((Token.Character) tEmpty.read()).getData());

        Tokeniser tLetter = createTokeniser("");
        TokeniserState.EndTagOpen.read(tLetter, new CharacterReader("span"));
        assertEquals(TokeniserState.TagName, tLetter.getState());
        assertTrue(tLetter.tagPending instanceof Token.EndTag);

        Tokeniser tGt = createTokeniser("");
        TokeniserState.EndTagOpen.read(tGt, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, tGt.getState());
        assertEquals(1, tGt.getErrors().size());

        Tokeniser tBogus = createTokeniser("");
        TokeniserState.EndTagOpen.read(tBogus, new CharacterReader("9"));
        assertEquals(TokeniserState.BogusComment, tBogus.getState());
        assertEquals(1, tBogus.getErrors().size());
    }

    @Test(timeout = 4000)
    public void testTagNameBranches() {
        // TagName whitespace branch
        Tokeniser tWs = createTokeniser("");
        tWs.createTagPending(true);
        TokeniserState.TagName.read(tWs, new CharacterReader("tag \t"));
        assertEquals(TokeniserState.BeforeAttributeName, tWs.getState());
        assertEquals("tag", tWs.tagPending.name());

        // TagName '/' branch
        Tokeniser tSlash = createTokeniser("");
        tSlash.createTagPending(true);
        TokeniserState.TagName.read(tSlash, new CharacterReader("tag/"));
        assertEquals(TokeniserState.SelfClosingStartTag, tSlash.getState());

        // TagName '>' branch
        Tokeniser tGt = createTokeniser("");
        tGt.createTagPending(true);
        TokeniserState.TagName.read(tGt, new CharacterReader("tag>"));
        assertEquals(TokeniserState.Data, tGt.getState());
        Token emitted = tGt.read();
        assertTrue(emitted instanceof Token.StartTag);
        assertEquals("tag", ((Token.StartTag) emitted).name());

        // TagName nullChar branch
        Tokeniser tNull = createTokeniser("");
        tNull.createTagPending(true);
        TokeniserState.TagName.read(tNull, new CharacterReader("tag\u0000>"));
        assertEquals(TokeniserState.TagName, tNull.getState());

        // TagName EOF branch
        Tokeniser tEof = createTokeniser("");
        tEof.createTagPending(true);
        TokeniserState.TagName.read(tEof, new CharacterReader("tag"));
        assertEquals(TokeniserState.Data, tEof.getState());
        assertEquals(1, tEof.getErrors().size());
    }

    @Test(timeout = 4000)
    public void testRcdataLessthanSignAndEndTag() {
        Tokeniser t = createTokeniser("");
        t.emit(new Token.StartTag("title")); // sets appropriateEndTagName
        t.read(); // consume start tag

        // matches '/'
        CharacterReader rSlash = new CharacterReader("/title>");
        TokeniserState.RcdataLessthanSign.read(t, rSlash);
        assertEquals(TokeniserState.RCDATAEndTagOpen, t.getState());

        // RCDATAEndTagOpen matches letter
        TokeniserState.RCDATAEndTagOpen.read(t, rSlash);
        assertEquals(TokeniserState.RCDATAEndTagName, t.getState());

        // RCDATAEndTagName consumes letter sequence
        TokeniserState.RCDATAEndTagName.read(t, rSlash);
        // Next character is '>' with appropriate end tag
        TokeniserState.RCDATAEndTagName.read(t, rSlash);
        assertEquals(TokeniserState.Data, t.getState());
        Token emitted = t.read();
        assertTrue(emitted instanceof Token.EndTag);
        assertEquals("title", ((Token.EndTag) emitted).name());

        // RcdataLessthanSign without appropriate end tag in remainder
        Tokeniser tDiverge = createTokeniser("");
        tDiverge.emit(new Token.StartTag("title"));
        tDiverge.read();
        CharacterReader rDiv = new CharacterReader("b>no end tag");
        TokeniserState.RcdataLessthanSign.read(tDiverge, rDiv);
        assertEquals(TokeniserState.Data, tDiverge.getState());
        Token divEnd = tDiverge.read();
        assertTrue(divEnd instanceof Token.EndTag);
        assertEquals("title", ((Token.EndTag) divEnd).name());

        // RcdataLessthanSign else branch
        Tokeniser tElse = createTokeniser("");
        CharacterReader rElse = new CharacterReader("123");
        TokeniserState.RcdataLessthanSign.read(tElse, rElse);
        assertEquals(TokeniserState.Rcdata, tElse.getState());
        assertEquals("<", ((Token.Character) tElse.read()).getData());

        // RCDATAEndTagOpen non-letter
        Tokeniser tNonLetter = createTokeniser("");
        CharacterReader rNonLetter = new CharacterReader("123");
        TokeniserState.RCDATAEndTagOpen.read(tNonLetter, rNonLetter);
        assertEquals(TokeniserState.Rcdata, tNonLetter.getState());
        assertEquals("</", ((Token.Character) tNonLetter.read()).getData());
    }

    @Test(timeout = 4000)
    public void testRCDATAEndTagNameBranches() {
        // whitespace with appropriate tag
        Tokeniser tWs = createTokeniser("");
        tWs.emit(new Token.StartTag("textarea"));
        tWs.read();
        tWs.createTagPending(false);
        tWs.tagPending.appendTagName("textarea");
        CharacterReader rWs = new CharacterReader(" attr");
        TokeniserState.RCDATAEndTagName.read(tWs, rWs);
        assertEquals(TokeniserState.BeforeAttributeName, tWs.getState());

        // '/' with appropriate tag
        Tokeniser tSlash = createTokeniser("");
        tSlash.emit(new Token.StartTag("textarea"));
        tSlash.read();
        tSlash.createTagPending(false);
        tSlash.tagPending.appendTagName("textarea");
        CharacterReader rSlash = new CharacterReader("/>");
        TokeniserState.RCDATAEndTagName.read(tSlash, rSlash);
        assertEquals(TokeniserState.SelfClosingStartTag, tSlash.getState());

        // anythingElse branch
        Tokeniser tOther = createTokeniser("");
        tOther.createTagPending(false);
        tOther.tagPending.appendTagName("wrong");
        tOther.createTempBuffer();
        tOther.dataBuffer.append("wrong");
        CharacterReader rOther = new CharacterReader(">");
        TokeniserState.RCDATAEndTagName.read(tOther, rOther);
        assertEquals(TokeniserState.Rcdata, tOther.getState());
        assertEquals("</wrong", ((Token.Character) tOther.read()).getData());
    }

    @Test(timeout = 4000)
    public void testRawtextEndTagTransitions() {
        Tokeniser t = createTokeniser("");
        t.emit(new Token.StartTag("style"));
        t.read();

        // RawtextLessthanSign '/' branch
        CharacterReader r1 = new CharacterReader("/style>");
        TokeniserState.RawtextLessthanSign.read(t, r1);
        assertEquals(TokeniserState.RawtextEndTagOpen, t.getState());

        // RawtextEndTagOpen matches letter
        TokeniserState.RawtextEndTagOpen.read(t, r1);
        assertEquals(TokeniserState.RawtextEndTagName, t.getState());

        // RawtextEndTagName letters
        TokeniserState.RawtextEndTagName.read(t, r1);
        // '>' branch
        TokeniserState.RawtextEndTagName.read(t, r1);
        assertEquals(TokeniserState.Data, t.getState());

        // RawtextLessthanSign else
        CharacterReader r2 = new CharacterReader("a");
        TokeniserState.RawtextLessthanSign.read(t, r2);
        assertEquals(TokeniserState.Rawtext, t.getState());
        assertEquals("<", ((Token.Character) t.read()).getData());

        // RawtextEndTagOpen non-letter
        CharacterReader r3 = new CharacterReader("1");
        TokeniserState.RawtextEndTagOpen.read(t, r3);
        assertEquals(TokeniserState.Rawtext, t.getState());
        assertEquals("</", ((Token.Character) t.read()).getData());

        // RawtextEndTagName whitespace and slash
        Tokeniser tWs = createTokeniser("");
        tWs.emit(new Token.StartTag("style"));
        tWs.read();
        tWs.createTagPending(false);
        tWs.tagPending.appendTagName("style");
        CharacterReader rWs = new CharacterReader(" ");
        TokeniserState.RawtextEndTagName.read(tWs, rWs);
        assertEquals(TokeniserState.BeforeAttributeName, tWs.getState());

        Tokeniser tSl = createTokeniser("");
        tSl.emit(new Token.StartTag("style"));
        tSl.read();
        tSl.createTagPending(false);
        tSl.tagPending.appendTagName("style");
        CharacterReader rSl = new CharacterReader("/");
        TokeniserState.RawtextEndTagName.read(tSl, rSl);
        assertEquals(TokeniserState.SelfClosingStartTag, tSl.getState());

        // RawtextEndTagName default / anythingElse
        Tokeniser tDef = createTokeniser("");
        tDef.emit(new Token.StartTag("style"));
        tDef.read();
        tDef.createTagPending(false);
        tDef.tagPending.appendTagName("style");
        tDef.createTempBuffer();
        CharacterReader rDef = new CharacterReader("z");
        TokeniserState.RawtextEndTagName.read(tDef, rDef);
        assertEquals(TokeniserState.Rawtext, tDef.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataLessthanSignAndEndTag() {
        Tokeniser t = createTokeniser("");
        t.emit(new Token.StartTag("script"));
        t.read();

        // '/' branch -> ScriptDataEndTagOpen
        CharacterReader r1 = new CharacterReader("/script>");
        TokeniserState.ScriptDataLessthanSign.read(t, r1);
        assertEquals(TokeniserState.ScriptDataEndTagOpen, t.getState());

        // '!' branch -> ScriptDataEscapeStart
        CharacterReader r2 = new CharacterReader("!--");
        TokeniserState.ScriptDataLessthanSign.read(t, r2);
        assertEquals(TokeniserState.ScriptDataEscapeStart, t.getState());
        assertEquals("<!", ((Token.Character) t.read()).getData());

        // default branch
        CharacterReader r3 = new CharacterReader("var x;");
        TokeniserState.ScriptDataLessthanSign.read(t, r3);
        assertEquals(TokeniserState.ScriptData, t.getState());
        assertEquals("<", ((Token.Character) t.read()).getData());

        // ScriptDataEndTagOpen letter vs non-letter
        CharacterReader rOpen = new CharacterReader("script>");
        TokeniserState.ScriptDataEndTagOpen.read(t, rOpen);
        assertEquals(TokeniserState.ScriptDataEndTagName, t.getState());

        CharacterReader rOpenNon = new CharacterReader("123");
        TokeniserState.ScriptDataEndTagOpen.read(t, rOpenNon);
        assertEquals(TokeniserState.ScriptData, t.getState());
        assertEquals("</", ((Token.Character) t.read()).getData());
    }

    @Test(timeout = 4000)
    public void testScriptDataEndTagNameBranches() {
        Tokeniser t = createTokeniser("");
        t.emit(new Token.StartTag("script"));
        t.read();
        t.createTagPending(false);
        t.tagPending.appendTagName("script");
        t.createTempBuffer();

        CharacterReader rWs = new CharacterReader(" ");
        TokeniserState.ScriptDataEndTagName.read(t, rWs);
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());

        CharacterReader rSl = new CharacterReader("/");
        TokeniserState.ScriptDataEndTagName.read(t, rSl);
        assertEquals(TokeniserState.SelfClosingStartTag, t.getState());

        CharacterReader rGt = new CharacterReader(">");
        TokeniserState.ScriptDataEndTagName.read(t, rGt);
        assertEquals(TokeniserState.Data, t.getState());

        CharacterReader rDef = new CharacterReader("z");
        TokeniserState.ScriptDataEndTagName.read(t, rDef);
        assertEquals(TokeniserState.ScriptData, t.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedBranches() {
        Tokeniser t = createTokeniser("");

        // Escape start '-' vs other
        CharacterReader rDash = new CharacterReader("-");
        TokeniserState.ScriptDataEscapeStart.read(t, rDash);
        assertEquals(TokeniserState.ScriptDataEscapeStartDash, t.getState());

        CharacterReader rNonDash = new CharacterReader("x");
        TokeniserState.ScriptDataEscapeStart.read(t, rNonDash);
        assertEquals(TokeniserState.ScriptData, t.getState());

        // Escape start dash '-' vs other
        TokeniserState.ScriptDataEscapeStartDash.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t.getState());

        TokeniserState.ScriptDataEscapeStartDash.read(t, new CharacterReader("x"));
        assertEquals(TokeniserState.ScriptData, t.getState());

        // ScriptDataEscaped: empty, '-', '<', nullChar, default
        TokeniserState.ScriptDataEscaped.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.ScriptDataEscaped.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.ScriptDataEscapedDash, t.getState());

        TokeniserState.ScriptDataEscaped.read(t, new CharacterReader("<"));
        assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataEscaped.read(t, new CharacterReader("\u0000"));
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) t.read()).getData());

        TokeniserState.ScriptDataEscaped.read(t, new CharacterReader("escaped text-"));
        assertEquals("escaped text", ((Token.Character) t.read()).getData());
    }

    @Test(timeout = 4000)
    public void testScriptDataEscapedDashAndDashDash() {
        Tokeniser t = createTokeniser("");

        // ScriptDataEscapedDash
        TokeniserState.ScriptDataEscapedDash.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.ScriptDataEscapedDash.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t.getState());

        TokeniserState.ScriptDataEscapedDash.read(t, new CharacterReader("<"));
        assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataEscapedDash.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.ScriptDataEscaped, t.getState());

        TokeniserState.ScriptDataEscapedDash.read(t, new CharacterReader("a"));
        assertEquals(TokeniserState.ScriptDataEscaped, t.getState());

        // ScriptDataEscapedDashDash
        TokeniserState.ScriptDataEscapedDashDash.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.ScriptDataEscapedDashDash.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.ScriptDataEscapedDashDash, t.getState());

        TokeniserState.ScriptDataEscapedDashDash.read(t, new CharacterReader("<"));
        assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataEscapedDashDash.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.ScriptData, t.getState());

        TokeniserState.ScriptDataEscapedDashDash.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.ScriptDataEscaped, t.getState());

        TokeniserState.ScriptDataEscapedDashDash.read(t, new CharacterReader("z"));
        assertEquals(TokeniserState.ScriptDataEscaped, t.getState());
    }

    @Test(timeout = 4000)
    public void testScriptDataDoubleEscapedSequence() {
        Tokeniser t = createTokeniser("");

        // ScriptDataEscapedLessthanSign letter vs '/' vs other
        TokeniserState.ScriptDataEscapedLessthanSign.read(t, new CharacterReader("s"));
        assertEquals(TokeniserState.ScriptDataDoubleEscapeStart, t.getState());

        TokeniserState.ScriptDataEscapedLessthanSign.read(t, new CharacterReader("/"));
        assertEquals(TokeniserState.ScriptDataEscapedEndTagOpen, t.getState());

        TokeniserState.ScriptDataEscapedLessthanSign.read(t, new CharacterReader("1"));
        assertEquals(TokeniserState.ScriptDataEscaped, t.getState());

        // ScriptDataDoubleEscapeStart matching "script"
        Tokeniser tDouble = createTokeniser("");
        tDouble.createTempBuffer();
        CharacterReader rScript = new CharacterReader("script>");
        TokeniserState.ScriptDataDoubleEscapeStart.read(tDouble, rScript);
        TokeniserState.ScriptDataDoubleEscapeStart.read(tDouble, rScript);
        assertEquals(TokeniserState.ScriptDataDoubleEscaped, tDouble.getState());

        // ScriptDataDoubleEscapeStart non-matching
        Tokeniser tOther = createTokeniser("");
        tOther.createTempBuffer();
        CharacterReader rOther = new CharacterReader("style>");
        TokeniserState.ScriptDataDoubleEscapeStart.read(tOther, rOther);
        TokeniserState.ScriptDataDoubleEscapeStart.read(tOther, rOther);
        assertEquals(TokeniserState.ScriptDataEscaped, tDouble.getState());

        // ScriptDataDoubleEscaped: '-', '<', null, EOF, default
        TokeniserState.ScriptDataDoubleEscaped.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.ScriptDataDoubleEscapedDash, t.getState());

        TokeniserState.ScriptDataDoubleEscaped.read(t, new CharacterReader("<"));
        assertEquals(TokeniserState.ScriptDataDoubleEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataDoubleEscaped.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.ScriptDataDoubleEscaped, t.getState());

        TokeniserState.ScriptDataDoubleEscaped.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.ScriptDataDoubleEscaped.read(t, new CharacterReader("abc<"));
        assertEquals("abc", ((Token.Character) t.read()).getData());

        // ScriptDataDoubleEscapedDash & DashDash
        TokeniserState.ScriptDataDoubleEscapedDash.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.ScriptDataDoubleEscapedDashDash, t.getState());

        TokeniserState.ScriptDataDoubleEscapedDash.read(t, new CharacterReader("<"));
        assertEquals(TokeniserState.ScriptDataDoubleEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataDoubleEscapedDash.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.ScriptDataDoubleEscapedDashDash.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.ScriptData, t.getState());

        TokeniserState.ScriptDataDoubleEscapedDashDash.read(t, new CharacterReader("<"));
        assertEquals(TokeniserState.ScriptDataDoubleEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataDoubleEscapedDashDash.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // ScriptDataDoubleEscapedLessthanSign '/' vs other
        TokeniserState.ScriptDataDoubleEscapedLessthanSign.read(t, new CharacterReader("/"));
        assertEquals(TokeniserState.ScriptDataDoubleEscapeEnd, t.getState());

        TokeniserState.ScriptDataDoubleEscapedLessthanSign.read(t, new CharacterReader("x"));
        assertEquals(TokeniserState.ScriptDataDoubleEscaped, t.getState());

        // ScriptDataDoubleEscapeEnd
        Tokeniser tEnd = createTokeniser("");
        tEnd.createTempBuffer();
        CharacterReader rEnd = new CharacterReader("script>");
        TokeniserState.ScriptDataDoubleEscapeEnd.read(tEnd, rEnd);
        TokeniserState.ScriptDataDoubleEscapeEnd.read(tEnd, rEnd);
        assertEquals(TokeniserState.ScriptDataEscaped, tEnd.getState());
    }

    @Test(timeout = 4000)
    public void testAttributeHandlingStates() {
        Tokeniser t = createTokeniser("");
        t.createTagPending(true);

        // BeforeAttributeName: whitespace, '/', '>', nullChar, EOF, quote/'<'/'=', default
        TokeniserState.BeforeAttributeName.read(t, new CharacterReader("\t"));
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());

        TokeniserState.BeforeAttributeName.read(t, new CharacterReader("/"));
        assertEquals(TokeniserState.SelfClosingStartTag, t.getState());

        t.transition(TokeniserState.BeforeAttributeName);
        TokeniserState.BeforeAttributeName.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        t.transition(TokeniserState.BeforeAttributeName);
        TokeniserState.BeforeAttributeName.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        t.transition(TokeniserState.BeforeAttributeName);
        TokeniserState.BeforeAttributeName.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.AttributeName, t.getState());

        t.transition(TokeniserState.BeforeAttributeName);
        TokeniserState.BeforeAttributeName.read(t, new CharacterReader("class"));
        assertEquals(TokeniserState.AttributeName, t.getState());

        // AttributeName: whitespace, '/', '=', '>', nullChar, EOF, quotes/'<'
        TokeniserState.AttributeName.read(t, new CharacterReader("name "));
        assertEquals(TokeniserState.AfterAttributeName, t.getState());

        TokeniserState.AttributeName.read(t, new CharacterReader("name/"));
        assertEquals(TokeniserState.SelfClosingStartTag, t.getState());

        TokeniserState.AttributeName.read(t, new CharacterReader("name="));
        assertEquals(TokeniserState.BeforeAttributeValue, t.getState());

        TokeniserState.AttributeName.read(t, new CharacterReader("name>"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AttributeName.read(t, new CharacterReader("name"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AttributeName.read(t, new CharacterReader("name\""));
        assertEquals(TokeniserState.AttributeName, t.getState());

        // AfterAttributeName: whitespace, '/', '=', '>', nullChar, EOF, quotes/'<', default
        TokeniserState.AfterAttributeName.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.AfterAttributeName, t.getState());

        TokeniserState.AfterAttributeName.read(t, new CharacterReader("/"));
        assertEquals(TokeniserState.SelfClosingStartTag, t.getState());

        TokeniserState.AfterAttributeName.read(t, new CharacterReader("="));
        assertEquals(TokeniserState.BeforeAttributeValue, t.getState());

        TokeniserState.AfterAttributeName.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterAttributeName.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterAttributeName.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.AttributeName, t.getState());

        TokeniserState.AfterAttributeName.read(t, new CharacterReader("newattr"));
        assertEquals(TokeniserState.AttributeName, t.getState());

        // BeforeAttributeValue: whitespace, '"', '&', '\'', nullChar, EOF, '>', chars('<','=','`'), default
        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BeforeAttributeValue, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.AttributeValue_doubleQuoted, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.AttributeValue_singleQuoted, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader("&"));
        assertEquals(TokeniserState.AttributeValue_unquoted, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.AttributeValue_unquoted, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader("<"));
        assertEquals(TokeniserState.AttributeValue_unquoted, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, new CharacterReader("unquoted"));
        assertEquals(TokeniserState.AttributeValue_unquoted, t.getState());
    }

    @Test(timeout = 4000)
    public void testAttributeValuesQuotedAndUnquoted() {
        Tokeniser t = createTokeniser("");
        t.createTagPending(true);
        t.tagPending.newAttribute();

        // AttributeValue_doubleQuoted
        TokeniserState.AttributeValue_doubleQuoted.read(t, new CharacterReader("val\""));
        assertEquals(TokeniserState.AfterAttributeValue_quoted, t.getState());

        TokeniserState.AttributeValue_doubleQuoted.read(t, new CharacterReader("&amp;\""));
        TokeniserState.AttributeValue_doubleQuoted.read(t, new CharacterReader("\u0000\""));
        TokeniserState.AttributeValue_doubleQuoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // AttributeValue_singleQuoted
        TokeniserState.AttributeValue_singleQuoted.read(t, new CharacterReader("val'"));
        assertEquals(TokeniserState.AfterAttributeValue_quoted, t.getState());

        TokeniserState.AttributeValue_singleQuoted.read(t, new CharacterReader("&amp;'"));
        TokeniserState.AttributeValue_singleQuoted.read(t, new CharacterReader("\u0000'"));
        TokeniserState.AttributeValue_singleQuoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // AttributeValue_unquoted
        TokeniserState.AttributeValue_unquoted.read(t, new CharacterReader("val "));
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());

        TokeniserState.AttributeValue_unquoted.read(t, new CharacterReader("val>"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AttributeValue_unquoted.read(t, new CharacterReader("val&lt;"));
        TokeniserState.AttributeValue_unquoted.read(t, new CharacterReader("val\u0000"));
        TokeniserState.AttributeValue_unquoted.read(t, new CharacterReader("val`"));
        TokeniserState.AttributeValue_unquoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // AfterAttributeValue_quoted
        TokeniserState.AfterAttributeValue_quoted.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());

        TokeniserState.AfterAttributeValue_quoted.read(t, new CharacterReader("/"));
        assertEquals(TokeniserState.SelfClosingStartTag, t.getState());

        TokeniserState.AfterAttributeValue_quoted.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterAttributeValue_quoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterAttributeValue_quoted.read(t, new CharacterReader("x"));
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());

        // SelfClosingStartTag
        t.createTagPending(true);
        TokeniserState.SelfClosingStartTag.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());
        assertTrue(t.tagPending.isSelfClosing());

        TokeniserState.SelfClosingStartTag.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.SelfClosingStartTag.read(t, new CharacterReader("x"));
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());
    }

    // =========================================================================
    // PARTITION D: Comments, Doctype, CDATA & Bogus Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMarkupDeclarationOpenBranches() {
        Tokeniser tComment = createTokeniser("");
        TokeniserState.MarkupDeclarationOpen.read(tComment, new CharacterReader("-- comment"));
        assertEquals(TokeniserState.CommentStart, tComment.getState());

        Tokeniser tDoctype = createTokeniser("");
        TokeniserState.MarkupDeclarationOpen.read(tDoctype, new CharacterReader("DOCTYPE html"));
        assertEquals(TokeniserState.Doctype, tDoctype.getState());

        Tokeniser tCdata = createTokeniser("");
        TokeniserState.MarkupDeclarationOpen.read(tCdata, new CharacterReader("[CDATA[data]]>"));
        assertEquals(TokeniserState.CdataSection, tCdata.getState());

        Tokeniser tBogus = createTokeniser("");
        TokeniserState.MarkupDeclarationOpen.read(tBogus, new CharacterReader("INVALID"));
        assertEquals(TokeniserState.BogusComment, tBogus.getState());
    }

    @Test(timeout = 4000)
    public void testCommentStates() {
        Tokeniser t = createTokeniser("");
        t.createCommentPending();

        // CommentStart
        TokeniserState.CommentStart.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.CommentStartDash, t.getState());

        TokeniserState.CommentStart.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.Comment, t.getState());

        TokeniserState.CommentStart.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentStart.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentStart.read(t, new CharacterReader("text"));
        assertEquals(TokeniserState.Comment, t.getState());

        // CommentStartDash
        TokeniserState.CommentStartDash.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.CommentStartDash, t.getState());

        TokeniserState.CommentStartDash.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.Comment, t.getState());

        TokeniserState.CommentStartDash.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentStartDash.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentStartDash.read(t, new CharacterReader("abc"));
        assertEquals(TokeniserState.Comment, t.getState());

        // Comment
        TokeniserState.Comment.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.CommentEndDash, t.getState());

        TokeniserState.Comment.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.Comment, t.getState());

        TokeniserState.Comment.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.Comment.read(t, new CharacterReader("body-"));
        assertEquals(TokeniserState.Comment, t.getState());

        // CommentEndDash
        TokeniserState.CommentEndDash.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.CommentEnd, t.getState());

        TokeniserState.CommentEndDash.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.Comment, t.getState());

        TokeniserState.CommentEndDash.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentEndDash.read(t, new CharacterReader("x"));
        assertEquals(TokeniserState.Comment, t.getState());

        // CommentEnd
        TokeniserState.CommentEnd.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentEnd.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.Comment, t.getState());

        TokeniserState.CommentEnd.read(t, new CharacterReader("!"));
        assertEquals(TokeniserState.CommentEndBang, t.getState());

        TokeniserState.CommentEnd.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.CommentEnd, t.getState());

        TokeniserState.CommentEnd.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentEnd.read(t, new CharacterReader("other"));
        assertEquals(TokeniserState.Comment, t.getState());

        // CommentEndBang
        TokeniserState.CommentEndBang.read(t, new CharacterReader("-"));
        assertEquals(TokeniserState.CommentEndDash, t.getState());

        TokeniserState.CommentEndBang.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentEndBang.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.Comment, t.getState());

        TokeniserState.CommentEndBang.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.CommentEndBang.read(t, new CharacterReader("other"));
        assertEquals(TokeniserState.Comment, t.getState());
    }

    @Test(timeout = 4000)
    public void testDoctypeStates() {
        Tokeniser t = createTokeniser("");

        // Doctype
        TokeniserState.Doctype.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BeforeDoctypeName, t.getState());

        TokeniserState.Doctype.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());
        assertTrue(t.doctypePending.isForceQuirks());

        TokeniserState.Doctype.read(t, new CharacterReader("x"));
        assertEquals(TokeniserState.BeforeDoctypeName, t.getState());

        // BeforeDoctypeName
        TokeniserState.BeforeDoctypeName.read(t, new CharacterReader("html"));
        assertEquals(TokeniserState.DoctypeName, t.getState());

        TokeniserState.BeforeDoctypeName.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.DoctypeName, t.getState());

        TokeniserState.BeforeDoctypeName.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.DoctypeName, t.getState());

        TokeniserState.BeforeDoctypeName.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // DoctypeName
        TokeniserState.DoctypeName.read(t, new CharacterReader("html>"));
        TokeniserState.DoctypeName.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.DoctypeName.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.AfterDoctypeName, t.getState());

        TokeniserState.DoctypeName.read(t, new CharacterReader("\u0000"));
        assertEquals(TokeniserState.DoctypeName, t.getState());

        TokeniserState.DoctypeName.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // AfterDoctypeName
        TokeniserState.AfterDoctypeName.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypeName.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.AfterDoctypeName, t.getState());

        TokeniserState.AfterDoctypeName.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        CharacterReader rPub = new CharacterReader("PUBLIC");
        TokeniserState.AfterDoctypeName.read(t, rPub);
        assertEquals(TokeniserState.AfterDoctypePublicKeyword, t.getState());

        CharacterReader rSys = new CharacterReader("SYSTEM");
        TokeniserState.AfterDoctypeName.read(t, rSys);
        assertEquals(TokeniserState.AfterDoctypeSystemKeyword, t.getState());

        TokeniserState.AfterDoctypeName.read(t, new CharacterReader("BOGUS"));
        assertEquals(TokeniserState.BogusDoctype, t.getState());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicAndSystemIdentifiers() {
        Tokeniser t = createTokeniser("");
        t.createDoctypePending();

        // AfterDoctypePublicKeyword
        TokeniserState.AfterDoctypePublicKeyword.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BeforeDoctypePublicIdentifier, t.getState());

        TokeniserState.AfterDoctypePublicKeyword.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.DoctypePublicIdentifier_doubleQuoted, t.getState());

        TokeniserState.AfterDoctypePublicKeyword.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.DoctypePublicIdentifier_singleQuoted, t.getState());

        TokeniserState.AfterDoctypePublicKeyword.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypePublicKeyword.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypePublicKeyword.read(t, new CharacterReader("bogus"));
        assertEquals(TokeniserState.BogusDoctype, t.getState());

        // BeforeDoctypePublicIdentifier
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BeforeDoctypePublicIdentifier, t.getState());

        TokeniserState.BeforeDoctypePublicIdentifier.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.DoctypePublicIdentifier_doubleQuoted, t.getState());

        TokeniserState.BeforeDoctypePublicIdentifier.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.DoctypePublicIdentifier_singleQuoted, t.getState());

        TokeniserState.BeforeDoctypePublicIdentifier.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BeforeDoctypePublicIdentifier.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BeforeDoctypePublicIdentifier.read(t, new CharacterReader("bogus"));
        assertEquals(TokeniserState.BogusDoctype, t.getState());

        // DoctypePublicIdentifier_doubleQuoted & singleQuoted
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.AfterDoctypePublicIdentifier, t.getState());

        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, new CharacterReader("\u0000"));
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.DoctypePublicIdentifier_singleQuoted.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.AfterDoctypePublicIdentifier, t.getState());

        TokeniserState.DoctypePublicIdentifier_singleQuoted.read(t, new CharacterReader("\u0000"));
        TokeniserState.DoctypePublicIdentifier_singleQuoted.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.DoctypePublicIdentifier_singleQuoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // AfterDoctypePublicIdentifier
        TokeniserState.AfterDoctypePublicIdentifier.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BetweenDoctypePublicAndSystemIdentifiers, t.getState());

        TokeniserState.AfterDoctypePublicIdentifier.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypePublicIdentifier.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t.getState());

        TokeniserState.AfterDoctypePublicIdentifier.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_singleQuoted, t.getState());

        TokeniserState.AfterDoctypePublicIdentifier.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypePublicIdentifier.read(t, new CharacterReader("bogus"));
        assertEquals(TokeniserState.BogusDoctype, t.getState());

        // BetweenDoctypePublicAndSystemIdentifiers
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BetweenDoctypePublicAndSystemIdentifiers, t.getState());

        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t.getState());

        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_singleQuoted, t.getState());

        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, new CharacterReader("bogus"));
        assertEquals(TokeniserState.BogusDoctype, t.getState());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierStates() {
        Tokeniser t = createTokeniser("");
        t.createDoctypePending();

        // AfterDoctypeSystemKeyword
        TokeniserState.AfterDoctypeSystemKeyword.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BeforeDoctypeSystemIdentifier, t.getState());

        TokeniserState.AfterDoctypeSystemKeyword.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypeSystemKeyword.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t.getState());

        TokeniserState.AfterDoctypeSystemKeyword.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_singleQuoted, t.getState());

        TokeniserState.AfterDoctypeSystemKeyword.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // BeforeDoctypeSystemIdentifier
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.BeforeDoctypeSystemIdentifier, t.getState());

        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t.getState());

        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.DoctypeSystemIdentifier_singleQuoted, t.getState());

        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, new CharacterReader("bogus"));
        assertEquals(TokeniserState.BogusDoctype, t.getState());

        // DoctypeSystemIdentifier_doubleQuoted & singleQuoted
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, new CharacterReader("\""));
        assertEquals(TokeniserState.AfterDoctypeSystemIdentifier, t.getState());

        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, new CharacterReader("\u0000"));
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.DoctypeSystemIdentifier_singleQuoted.read(t, new CharacterReader("'"));
        assertEquals(TokeniserState.AfterDoctypeSystemIdentifier, t.getState());

        TokeniserState.DoctypeSystemIdentifier_singleQuoted.read(t, new CharacterReader("\u0000"));
        TokeniserState.DoctypeSystemIdentifier_singleQuoted.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.DoctypeSystemIdentifier_singleQuoted.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        // AfterDoctypeSystemIdentifier
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, new CharacterReader(" "));
        assertEquals(TokeniserState.AfterDoctypeSystemIdentifier, t.getState());

        TokeniserState.AfterDoctypeSystemIdentifier.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypeSystemIdentifier.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.AfterDoctypeSystemIdentifier.read(t, new CharacterReader("x"));
        assertEquals(TokeniserState.BogusDoctype, t.getState());

        // BogusDoctype
        TokeniserState.BogusDoctype.read(t, new CharacterReader("ignoring chars>"));
        TokeniserState.BogusDoctype.read(t, new CharacterReader(">"));
        assertEquals(TokeniserState.Data, t.getState());

        TokeniserState.BogusDoctype.read(t, new CharacterReader(""));
        assertEquals(TokeniserState.Data, t.getState());
    }

    @Test(timeout = 4000)
    public void testCdataSection() {
        Tokeniser t = createTokeniser("");
        CharacterReader r = new CharacterReader("cdata content]]>rest");
        TokeniserState.CdataSection.read(t, r);
        assertEquals(TokeniserState.Data, t.getState());
        Token tok = t.read();
        assertTrue(tok instanceof Token.Character);
        assertEquals("cdata content", ((Token.Character) tok).getData());
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnumLifecycleAndIntegrity() {
        TokeniserState[] states = TokeniserState.values();
        assertTrue(states.length > 50);
        for (TokeniserState state : states) {
            assertNotNull(state);
            assertEquals(state, TokeniserState.valueOf(state.name()));
        }
        assertEquals(TokeniserState.Data, TokeniserState.valueOf("Data"));
        assertEquals(TokeniserState.TagOpen, TokeniserState.valueOf("TagOpen"));
        assertEquals(TokeniserState.BogusComment, TokeniserState.valueOf("BogusComment"));
    }
}