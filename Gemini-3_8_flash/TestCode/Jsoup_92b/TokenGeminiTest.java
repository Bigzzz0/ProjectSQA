package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Token and inner subclasses (Doctype, Tag, StartTag, EndTag, Comment, Character, CData, EOF)
 * ----------------------------------------------------------------------------------------------------------------------
 * Branch / Condition Coverage:
 * 1. Token.reset(StringBuilder):
 *    - sb == null (defensive guard, no-op)
 *    - sb != null (clears content)
 * 2. Token Type Discrimination & Casting:
 *    - isDoctype / asDoctype on Doctype vs non-Doctype (ClassCastException)
 *    - isStartTag / asStartTag on StartTag vs non-StartTag
 *    - isEndTag / asEndTag on EndTag vs non-EndTag
 *    - isComment / asComment on Comment vs non-Comment
 *    - isCharacter / asCharacter on Character and CData vs non-Character
 *    - isCData on CData (true) vs Character (false) vs other Tokens (false)
 *    - isEOF on EOF vs other Tokens
 * 3. Token.Tag State Machine & Attributes:
 *    - appendTagName(String/char) when tagName == null vs tagName != null
 *    - normalName() case normalization (lowerCase transform)
 *    - appendAttributeName(String/char) when pendingAttributeName == null vs pendingAttributeName != null
 *    - appendAttributeValue(String) when pendingAttributeValue.length() == 0 (stores in pendingAttributeValueS)
 *      vs multiple calls (transitions pendingAttributeValueS into pendingAttributeValue StringBuilder)
 *    - appendAttributeValue(char), appendAttributeValue(char[]), appendAttributeValue(int[] codePoints)
 *    - setEmptyAttributeValue: hasEmptyAttributeValue = true -> value = ""
 *    - Boolean attribute: neither pending value nor empty value -> value = null
 *    - pendingAttributeName whitespace trimming: collapsed to empty (length == 0) -> dropped
 *    - finaliseTag with pendingAttributeName != null vs == null
 *    - selfClosing toggle
 *    - name() validation: throws IllegalArgumentException if tagName is null or empty
 * 4. Subclass String Representations:
 *    - StartTag.toString(): attributes != null && attributes.size() > 0 vs empty attributes
 *    - EndTag.toString()
 *    - Comment.toString()
 *    - Character.toString()
 *    - CData.toString()
 * 5. Lifecycle & Reuse (reset):
 *    - Doctype.reset(): name, pubSysKey, publicIdentifier, systemIdentifier, forceQuirks
 *    - Tag.reset() & StartTag.reset(): attributes refreshed, all state zeroed
 *    - Comment.reset(): data cleared, bogus reset
 *    - Character.reset(): data cleared
 *    - EOF.reset(): no-op self return
 * 6. Defects4J Known Defect Targeted (Partition C):
 *    - Defects: dropsDuplicateAttributes, retainsAttributesOfDifferentCaseIfSensitive
 *    - Defect Mechanism: In Token.Tag.newAttribute(), attributes.put(...) was invoked instead of
 *      preserving the FIRST attribute seen. HTML5 specification dictates that subsequent duplicate
 *      attributes on a start tag must be ignored, retaining the initial value.
 */
public class TokenGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoctypeCompleteLifecycle() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertEquals(Token.TokenType.Doctype, doctype.type);
        assertEquals("Doctype", doctype.tokenType());

        doctype.name.append("html");
        doctype.pubSysKey = "PUBLIC";
        doctype.publicIdentifier.append("-//W3C//DTD HTML 4.01//EN");
        doctype.systemIdentifier.append("http://www.w3.org/TR/html4/strict.dtd");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("PUBLIC", doctype.getPubSysKey());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());

        assertSame(doctype, doctype.reset());
        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testStartTagCoreOperationsAndToString() {
        Token.StartTag startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertEquals(Token.TokenType.StartTag, startTag.type);
        assertEquals("StartTag", startTag.tokenType());

        startTag.name("DIV");
        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());
        assertEquals("<DIV>", startTag.toString());

        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();
        startTag.selfClosing = true;

        assertTrue(startTag.isSelfClosing());
        assertEquals("<DIV id=\"main\">", startTag.toString());

        Attributes customAttrs = new Attributes();
        customAttrs.put("class", "container");
        startTag.nameAttr("SECTION", customAttrs);

        assertEquals("SECTION", startTag.name());
        assertEquals("section", startTag.normalName());
        assertEquals("<SECTION class=\"container\">", startTag.toString());
    }

    @Test(timeout = 4000)
    public void testEndTagOperationsAndToString() {
        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertEquals(Token.TokenType.EndTag, endTag.type);
        assertEquals("EndTag", endTag.tokenType());

        endTag.name("SPAN");
        assertEquals("SPAN", endTag.name());
        assertEquals("span", endTag.normalName());
        assertEquals("</SPAN>", endTag.toString());

        // End tags start with null attributes until newAttribute is called
        assertNull(endTag.getAttributes());
        endTag.appendAttributeName("ignored");
        endTag.appendAttributeValue("val");
        endTag.newAttribute();
        assertNotNull(endTag.getAttributes());
        assertTrue(endTag.getAttributes().hasKey("ignored"));
    }

    @Test(timeout = 4000)
    public void testCommentOperationsAndToString() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertEquals(Token.TokenType.Comment, comment.type);
        assertEquals("Comment", comment.tokenType());

        comment.data.append("This is a comment");
        comment.bogus = true;
        assertEquals("This is a comment", comment.getData());
        assertTrue(comment.bogus);
        assertEquals("<!--This is a comment-->", comment.toString());

        assertSame(comment, comment.reset());
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
        assertEquals("<!---->", comment.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterAndCDataOperations() {
        Token.Character character = new Token.Character();
        assertTrue(character.isCharacter());
        assertFalse(character.isCData());
        assertEquals(Token.TokenType.Character, character.type);
        assertEquals("Character", character.tokenType());

        character.data("Sample Text");
        assertEquals("Sample Text", character.getData());
        assertEquals("Sample Text", character.toString());

        assertSame(character, character.reset());
        assertNull(character.getData());

        Token.CData cdata = new Token.CData("raw <xml> & data");
        assertTrue(cdata.isCharacter());
        assertTrue(cdata.isCData());
        assertEquals("raw <xml> & data", cdata.getData());
        assertEquals("<![CDATA[raw <xml> & data]]>", cdata.toString());
        assertEquals("CData", cdata.tokenType());
    }

    @Test(timeout = 4000)
    public void testEOFTokenOperations() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals(Token.TokenType.EOF, eof.type);
        assertEquals("EOF", eof.tokenType());
        assertSame(eof, eof.reset());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStaticResetStringBuilderNullAndNonNull() {
        // Defensive branch: sb == null must not throw NullPointerException
        Token.reset(null);

        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendTagNameIncrementalTransitions() {
        Token.StartTag tag = new Token.StartTag();
        // First append from null
        tag.appendTagName("d");
        assertEquals("d", tag.name());
        assertEquals("d", tag.normalName());

        // Second append with String concatenation
        tag.appendTagName("iv");
        assertEquals("div", tag.name());
        assertEquals("div", tag.normalName());

        // Char append
        tag.appendTagName('1');
        assertEquals("div1", tag.name());
        assertEquals("div1", tag.normalName());
    }

    @Test(timeout = 4000)
    public void testAppendAttributeNameIncrementalTransitions() {
        Token.StartTag tag = new Token.StartTag();
        // First append from null
        tag.appendAttributeName("data-");
        // Append char
        tag.appendAttributeName('v');
        // Append String
        tag.appendAttributeName("al");
        tag.setEmptyAttributeValue();
        tag.newAttribute();

        assertTrue(tag.getAttributes().hasKey("data-val"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueAccumulationVarieties() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("test");

        // 1. Single string append (uses pendingAttributeValueS branch)
        tag.appendAttributeName("attr1");
        tag.appendAttributeValue("simple");
        tag.newAttribute();
        assertEquals("simple", tag.getAttributes().get("attr1"));

        // 2. Multi-string append (transitions pendingAttributeValueS into pendingAttributeValue)
        tag.appendAttributeName("attr2");
        tag.appendAttributeValue("part1-");
        tag.appendAttributeValue("part2");
        tag.newAttribute();
        assertEquals("part1-part2", tag.getAttributes().get("attr2"));

        // 3. Char append
        tag.appendAttributeName("attr3");
        tag.appendAttributeValue('A');
        tag.appendAttributeValue('B');
        tag.newAttribute();
        assertEquals("AB", tag.getAttributes().get("attr3"));

        // 4. Char array append
        tag.appendAttributeName("attr4");
        tag.appendAttributeValue(new char[]{'x', 'y', 'z'});
        tag.newAttribute();
        assertEquals("xyz", tag.getAttributes().get("attr4"));

        // 5. CodePoints append (including Supplementary Character Emoji U+1F600)
        tag.appendAttributeName("attr5");
        tag.appendAttributeValue(new int[]{0x41, 0x1F600, 0x42});
        tag.newAttribute();
        String expectedEmoji = "A\uD83D\uDE00B";
        assertEquals(expectedEmoji, tag.getAttributes().get("attr5"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueTypesBooleanAndEmpty() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");

        // Boolean attribute: hasEmptyAttributeValue=false, hasPendingAttributeValue=false -> null value
        tag.appendAttributeName("disabled");
        tag.newAttribute();
        assertTrue(tag.getAttributes().hasKey("disabled"));
        assertEquals("", tag.getAttributes().get("disabled"));

        // Explicit empty value: hasEmptyAttributeValue=true -> "" value
        tag.appendAttributeName("value");
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertTrue(tag.getAttributes().hasKey("value"));
        assertEquals("", tag.getAttributes().get("value"));
    }

    @Test(timeout = 4000)
    public void testAttributeNameWhitespaceTrimmingToEmptyIsIgnored() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");

        // Attribute name with only whitespace trims to length 0 -> should be dropped
        tag.appendAttributeName("   ");
        tag.appendAttributeValue("val");
        tag.newAttribute();

        assertEquals(0, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testFinaliseTagBranches() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");

        // When pendingAttributeName is null, finaliseTag is a no-op
        tag.finaliseTag();
        assertEquals(0, tag.getAttributes().size());

        // When pendingAttributeName is present, finaliseTag calls newAttribute
        tag.appendAttributeName("autofocus");
        tag.finaliseTag();
        assertTrue(tag.getAttributes().hasKey("autofocus"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (HTML5 Duplicate Attributes)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDuplicateAttributesRetainFirstValueDefect() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("p");

        // First occurrence of attribute "One" with value "One"
        startTag.appendAttributeName("One");
        startTag.appendAttributeValue("One");
        startTag.newAttribute();

        // Second duplicate occurrence of attribute "One" with value "Two"
        startTag.appendAttributeName("One");
        startTag.appendAttributeValue("Two");
        startTag.newAttribute();

        startTag.finaliseTag();

        // HTML5 Specification: duplicate attributes on a start tag must keep the first value.
        // On defective versions using attributes.put(...), the second value overwrites the first.
        assertEquals("One", startTag.getAttributes().get("One"));
    }

    @Test(timeout = 4000)
    public void testMultipleDuplicateAttributesDropSubsequentDefect() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("p");

        // Primary attribute "one" -> "One"
        startTag.appendAttributeName("one");
        startTag.appendAttributeValue("One");
        startTag.newAttribute();

        // Attribute "two" -> "two"
        startTag.appendAttributeName("two");
        startTag.appendAttributeValue("two");
        startTag.newAttribute();

        // Duplicate "one" -> "Two"
        startTag.appendAttributeName("one");
        startTag.appendAttributeValue("Two");
        startTag.newAttribute();

        // Duplicate "one" -> "Three"
        startTag.appendAttributeName("one");
        startTag.appendAttributeValue("Three");
        startTag.newAttribute();

        startTag.finaliseTag();

        assertEquals("One", startTag.getAttributes().get("one"));
        assertEquals("two", startTag.getAttributes().get("two"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameNullThrowsException() {
        Token.StartTag tag = new Token.StartTag();
        tag.name();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagNameEmptyThrowsException() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("");
        tag.name();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastAsDoctypeThrowsClassCastException() {
        Token token = new Token.Comment();
        token.asDoctype();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastAsStartTagThrowsClassCastException() {
        Token token = new Token.EndTag();
        token.asStartTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastAsEndTagThrowsClassCastException() {
        Token token = new Token.StartTag();
        token.asEndTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastAsCommentThrowsClassCastException() {
        Token token = new Token.Character();
        token.asComment();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastAsCharacterThrowsClassCastException() {
        Token token = new Token.EOF();
        token.asCharacter();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testTagResetSanitizesAllState() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("custom");
        tag.appendAttributeName("key");
        tag.appendAttributeValue("value");
        tag.selfClosing = true;
        tag.newAttribute();

        assertEquals(1, tag.getAttributes().size());
        assertTrue(tag.isSelfClosing());

        tag.reset();

        assertNull(tag.normalName());
        assertFalse(tag.isSelfClosing());
        assertNotNull(tag.getAttributes());
        assertEquals(0, tag.getAttributes().size());

        try {
            tag.name();
            fail("Expected IllegalArgumentException after reset wiped tagName");
        } catch (IllegalArgumentException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testTokenCastMethodsCorrectDispatch() {
        Token doctype = new Token.Doctype();
        Token start = new Token.StartTag();
        Token end = new Token.EndTag();
        Token comment = new Token.Comment();
        Token character = new Token.Character();
        Token cdata = new Token.CData("data");
        Token eof = new Token.EOF();

        assertSame(doctype, doctype.asDoctype());
        assertSame(start, start.asStartTag());
        assertSame(end, end.asEndTag());
        assertSame(comment, comment.asComment());
        assertSame(character, character.asCharacter());
        assertSame(cdata, cdata.asCharacter());

        assertFalse(start.isDoctype());
        assertFalse(end.isStartTag());
        assertFalse(comment.isEndTag());
        assertFalse(character.isComment());
        assertFalse(eof.isCharacter());
        assertFalse(doctype.isEOF());
    }

    @Test(timeout = 4000)
    public void testTokenTypeEnumContract() {
        Token.TokenType[] types = Token.TokenType.values();
        assertEquals(6, types.length);
        assertEquals(Token.TokenType.Doctype, Token.TokenType.valueOf("Doctype"));
        assertEquals(Token.TokenType.StartTag, Token.TokenType.valueOf("StartTag"));
        assertEquals(Token.TokenType.EndTag, Token.TokenType.valueOf("EndTag"));
        assertEquals(Token.TokenType.Comment, Token.TokenType.valueOf("Comment"));
        assertEquals(Token.TokenType.Character, Token.TokenType.valueOf("Character"));
        assertEquals(Token.TokenType.EOF, Token.TokenType.valueOf("EOF"));
    }
}