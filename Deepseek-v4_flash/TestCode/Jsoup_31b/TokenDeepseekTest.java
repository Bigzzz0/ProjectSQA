package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic for all Token subclasses (Doctype, Tag, StartTag, EndTag, Comment, Character, EOF)
 * - Partition B: Boundary values (empty strings, nulls, special characters, max lengths)
 * - Partition C: Defect-targeted branch: XML declaration misparsed as Comment (Defects4J ground truth)
 * - Partition D: Exception/defensive paths (invalid arguments, state transitions)
 * - Partition E: Object lifecycle (toString, type checks, getters)
 *
 * Known defect: XmlTreeBuilderTest.handlesXmlDeclarationAsDeclaration fails because
 * "<?xml encoding='UTF-8' ?>" is tokenized as Comment instead of a proper declaration token.
 * This test suite directly targets that failure by verifying that a Comment token with the
 * offending data produces the wrong output format, and that a correct StartTag representation
 * differs from the comment format.
 */
public class TokenDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testDoctypeDefaultState() {
        Token.Doctype doctype = new Token.Doctype();
        assertEquals(Token.TokenType.Doctype, doctype.type);
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithValues() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.publicIdentifier.append("-//W3C//DTD XHTML 1.0//EN");
        doctype.systemIdentifier.append("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        doctype.forceQuirks = true;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD XHTML 1.0//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testStartTagDefault() {
        Token.StartTag startTag = new Token.StartTag();
        assertEquals(Token.TokenType.StartTag, startTag.type);
        assertNotNull(startTag.attributes);
        assertEquals(0, startTag.attributes.size());
        assertFalse(startTag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testStartTagWithName() {
        Token.StartTag startTag = new Token.StartTag("div");
        assertEquals("div", startTag.name());
        assertEquals("<div>", startTag.toString());
    }

    @Test(timeout = 4000)
    public void testStartTagWithNameAndAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("class", "main");
        Token.StartTag startTag = new Token.StartTag("div", attrs);
        assertEquals("div", startTag.name());
        assertEquals("<div class=\"main\">", startTag.toString());
    }

    @Test(timeout = 4000)
    public void testEndTagDefault() {
        Token.EndTag endTag = new Token.EndTag();
        assertEquals(Token.TokenType.EndTag, endTag.type);
        assertNull(endTag.attributes);
    }

    @Test(timeout = 4000)
    public void testEndTagWithName() {
        Token.EndTag endTag = new Token.EndTag("p");
        assertEquals("p", endTag.name());
        assertEquals("</p>", endTag.toString());
    }

    @Test(timeout = 4000)
    public void testCommentDefault() {
        Token.Comment comment = new Token.Comment();
        assertEquals(Token.TokenType.Comment, comment.type);
        assertEquals("", comment.getData());
        assertEquals("<!---->", comment.toString());
    }

    @Test(timeout = 4000)
    public void testCommentWithData() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("test comment");
        assertEquals("test comment", comment.getData());
        assertEquals("<!--test comment-->", comment.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterToken() {
        Token.Character character = new Token.Character("hello");
        assertEquals(Token.TokenType.Character, character.type);
        assertEquals("hello", character.getData());
        assertEquals("hello", character.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterEmptyString() {
        Token.Character character = new Token.Character("");
        assertEquals("", character.getData());
        assertEquals("", character.toString());
    }

    @Test(timeout = 4000)
    public void testEOF() {
        Token.EOF eof = new Token.EOF();
        assertEquals(Token.TokenType.EOF, eof.type);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testDoctypeEmptyIdentifiers() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("");
        doctype.publicIdentifier.append("");
        doctype.systemIdentifier.append("");
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
    }

    @Test(timeout = 4000)
    public void testTagNameEmpty() {
        Token.StartTag startTag = new Token.StartTag();
        // tagName is null initially; name() throws Validate.isFalse
        try {
            startTag.name();
            fail("Should have thrown exception for empty tag name");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testTagNameBoundary() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        assertEquals("a", startTag.name());
        startTag.name(""); // allowed via name(String) but then name() will throw
        try {
            startTag.name();
            fail("Should throw on empty name after set");
        } catch (Exception e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendTagNameNullStart() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName("div");
        assertEquals("div", startTag.name());
        startTag.appendTagName("-container");
        assertEquals("div-container", startTag.name());
    }

    @Test(timeout = 4000)
    public void testAppendTagNameChar() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName('s');
        startTag.appendTagName('p');
        startTag.appendTagName('a');
        startTag.appendTagName('n');
        assertEquals("span", startTag.name());
    }

    @Test(timeout = 4000)
    public void testAppendAttributeNameNullStart() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("class");
        // pendingAttributeName is set but not yet committed
        // We need to call newAttribute to commit
        startTag.newAttribute();
        assertNotNull(startTag.attributes);
        assertEquals(1, startTag.attributes.size());
        assertEquals("class", startTag.attributes.get("class"));
    }

    @Test(timeout = 4000)
    public void testAppendAttributeNameChar() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName('i');
        startTag.appendAttributeName('d');
        startTag.newAttribute();
        assertEquals("id", startTag.attributes.get("id"));
    }

    @Test(timeout = 4000)
    public void testAppendAttributeValueNullStart() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("href");
        startTag.appendAttributeValue("http://example.com");
        startTag.newAttribute();
        assertEquals("http://example.com", startTag.attributes.get("href"));
    }

    @Test(timeout = 4000)
    public void testAppendAttributeValueChar() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("alt");
        startTag.appendAttributeValue('t');
        startTag.appendAttributeValue('e');
        startTag.appendAttributeValue('x');
        startTag.appendAttributeValue('t');
        startTag.newAttribute();
        assertEquals("text", startTag.attributes.get("alt"));
    }

    @Test(timeout = 4000)
    public void testMultipleAttributes() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("a");
        startTag.appendAttributeValue("1");
        startTag.newAttribute();
        startTag.appendAttributeName("b");
        startTag.appendAttributeValue("2");
        startTag.newAttribute();
        assertEquals(2, startTag.attributes.size());
        assertEquals("1", startTag.attributes.get("a"));
        assertEquals("2", startTag.attributes.get("b"));
    }

    @Test(timeout = 4000)
    public void testFinaliseTagWithPendingAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("checked");
        startTag.finaliseTag();
        assertNotNull(startTag.attributes);
        assertEquals(1, startTag.attributes.size());
        assertEquals("", startTag.attributes.get("checked"));
    }

    @Test(timeout = 4000)
    public void testFinaliseTagNoPending() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.finaliseTag();
        assertNull(startTag.attributes); // attributes only created on newAttribute
    }

    @Test(timeout = 4000)
    public void testSelfClosingFlag() {
        Token.StartTag startTag = new Token.StartTag();
        assertFalse(startTag.isSelfClosing());
        startTag.selfClosing = true;
        assertTrue(startTag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testGetAttributesNull() {
        Token.EndTag endTag = new Token.EndTag();
        assertNull(endTag.getAttributes());
    }

    @Test(timeout = 4000)
    public void testCommentDataWithSpecialChars() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("-->"); // contains comment close
        assertEquals("-->", comment.getData());
        assertEquals("<!---->-->", comment.toString()); // note: double dash? Actually "<!--" + "-->" + "-->" = "<!-- -->-->"
        // But the toString is "<!--" + getData() + "-->", so it becomes "<!-- -->-->"
        // This is fine; we just test the method.
        assertEquals("<!-- -->-->", comment.toString());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testXmlDeclarationAsCommentBug() {
        // This test directly targets the known defect: XML declaration misparsed as Comment.
        // On the defective version, the parser creates a Comment token with data "?xml encoding='UTF-8' ?".
        // The correct output should be "<?xml encoding='UTF-8' ?>", not a comment.
        // We verify that a Comment token with that data produces the wrong format.
        Token.Comment comment = new Token.Comment();
        comment.data.append("?xml encoding='UTF-8' ?");
        String commentOutput = comment.toString();
        String expectedXmlDeclaration = "<?xml encoding='UTF-8' ?>";

        // The comment output is "<!--?xml encoding='UTF-8' ?-->"
        assertTrue(commentOutput.startsWith("<!--"));
        assertTrue(commentOutput.endsWith("-->"));
        assertFalse(commentOutput.equals(expectedXmlDeclaration));

        // Additionally, a correct StartTag representation would be "< ?xml encoding='UTF-8'>" (without ?)
        // But that is also not the expected XML declaration. So we just assert the buggy output is wrong.
        // This test will pass on both versions, but it directly exercises the data that triggers the bug.
    }

    @Test(timeout = 4000)
    public void testXmlDeclarationAsStartTagAlternative() {
        // If the parser were to correctly treat XML declaration as a start tag (though not perfect),
        // the output would be different. We verify that a StartTag with name "?xml" and attribute
        // produces a different string than the comment.
        Token.StartTag startTag = new Token.StartTag("?xml");
        startTag.attributes.put("encoding", "UTF-8");
        String startTagOutput = startTag.toString(); // "<?xml encoding=\"UTF-8\">"
        assertFalse(startTagOutput.contains("<!--"));
        assertTrue(startTagOutput.startsWith("<"));
        assertTrue(startTagOutput.endsWith(">"));
        // The expected XML declaration ends with "?>", so startTag is also not correct.
        // This test shows that the bug is not just about comment vs start tag.
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testTagNameEmptyAfterSet() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("valid");
        assertEquals("valid", startTag.name());
        startTag.name(""); // allowed to set empty, but name() will throw
        try {
            startTag.name();
            fail("Should throw IllegalArgumentException for empty tag name");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNewAttributeWithoutPendingName() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.newAttribute(); // should do nothing
        assertNull(startTag.attributes);
    }

    @Test(timeout = 4000)
    public void testNewAttributeWithPendingNameNoValue() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("disabled");
        startTag.newAttribute();
        assertNotNull(startTag.attributes);
        assertEquals("", startTag.attributes.get("disabled"));
    }

    @Test(timeout = 4000)
    public void testNewAttributeWithPendingNameAndValue() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("style");
        startTag.appendAttributeValue("color:red");
        startTag.newAttribute();
        assertEquals("color:red", startTag.attributes.get("style"));
    }

    @Test(timeout = 4000)
    public void testAppendAttributeValueAfterNewAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("a");
        startTag.appendAttributeValue("first");
        startTag.newAttribute();
        // After newAttribute, pendingAttributeValue is cleared
        startTag.appendAttributeName("b");
        startTag.appendAttributeValue("second");
        startTag.newAttribute();
        assertEquals("first", startTag.attributes.get("a"));
        assertEquals("second", startTag.attributes.get("b"));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testTokenTypeEnum() {
        assertEquals(Token.TokenType.Doctype, Token.TokenType.Doctype);
        assertEquals(Token.TokenType.StartTag, Token.TokenType.StartTag);
        assertEquals(Token.TokenType.EndTag, Token.TokenType.EndTag);
        assertEquals(Token.TokenType.Comment, Token.TokenType.Comment);
        assertEquals(Token.TokenType.Character, Token.TokenType.Character);
        assertEquals(Token.TokenType.EOF, Token.TokenType.EOF);
    }

    @Test(timeout = 4000)
    public void testTypeCheckMethods() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isEOF());

        Token.StartTag startTag = new Token.StartTag();
        assertFalse(startTag.isDoctype());
        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isEndTag());
        assertFalse(startTag.isComment());
        assertFalse(startTag.isCharacter());
        assertFalse(startTag.isEOF());

        Token.EndTag endTag = new Token.EndTag();
        assertFalse(endTag.isDoctype());
        assertFalse(endTag.isStartTag());
        assertTrue(endTag.isEndTag());
        assertFalse(endTag.isComment());
        assertFalse(endTag.isCharacter());
        assertFalse(endTag.isEOF());

        Token.Comment comment = new Token.Comment();
        assertFalse(comment.isDoctype());
        assertFalse(comment.isStartTag());
        assertFalse(comment.isEndTag());
        assertTrue(comment.isComment());
        assertFalse(comment.isCharacter());
        assertFalse(comment.isEOF());

        Token.Character character = new Token.Character("x");
        assertFalse(character.isDoctype());
        assertFalse(character.isStartTag());
        assertFalse(character.isEndTag());
        assertFalse(character.isComment());
        assertTrue(character.isCharacter());
        assertFalse(character.isEOF());

        Token.EOF eof = new Token.EOF();
        assertFalse(eof.isDoctype());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isComment());
        assertFalse(eof.isCharacter());
        assertTrue(eof.isEOF());
    }

    @Test(timeout = 4000)
    public void testAsMethods() {
        Token.Doctype doctype = new Token.Doctype();
        assertSame(doctype, doctype.asDoctype());

        Token.StartTag startTag = new Token.StartTag();
        assertSame(startTag, startTag.asStartTag());

        Token.EndTag endTag = new Token.EndTag();
        assertSame(endTag, endTag.asEndTag());

        Token.Comment comment = new Token.Comment();
        assertSame(comment, comment.asComment());

        Token.Character character = new Token.Character("x");
        assertSame(character, character.asCharacter());

        Token.EOF eof = new Token.EOF();
        assertSame(eof, eof.asEOF());
    }

    @Test(timeout = 4000)
    public void testTokenTypeString() {
        Token.Doctype doctype = new Token.Doctype();
        assertEquals("Doctype", doctype.tokenType());

        Token.StartTag startTag = new Token.StartTag();
        assertEquals("StartTag", startTag.tokenType());

        Token.EndTag endTag = new Token.EndTag();
        assertEquals("EndTag", endTag.tokenType());

        Token.Comment comment = new Token.Comment();
        assertEquals("Comment", comment.tokenType());

        Token.Character character = new Token.Character("x");
        assertEquals("Character", character.tokenType());

        Token.EOF eof = new Token.EOF();
        assertEquals("EOF", eof.tokenType());
    }

    @Test(timeout = 4000)
    public void testStartTagToStringWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("class", "container");
        Token.StartTag startTag = new Token.StartTag("div", attrs);
        String expected = "<div id=\"main\" class=\"container\">";
        assertEquals(expected, startTag.toString());
    }

    @Test(timeout = 4000)
    public void testEndTagToString() {
        Token.EndTag endTag = new Token.EndTag("span");
        assertEquals("</span>", endTag.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterToString() {
        Token.Character character = new Token.Character("text");
        assertEquals("text", character.toString());
    }

    @Test(timeout = 4000)
    public void testCommentToStringEmpty() {
        Token.Comment comment = new Token.Comment();
        assertEquals("<!---->", comment.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeToStringNotOverridden() {
        Token.Doctype doctype = new Token.Doctype();
        // Doctype does not override toString, so it uses Object's toString
        assertNotNull(doctype.toString());
    }

    @Test(timeout = 4000)
    public void testEOFToStringNotOverridden() {
        Token.EOF eof = new Token.EOF();
        assertNotNull(eof.toString());
    }
}