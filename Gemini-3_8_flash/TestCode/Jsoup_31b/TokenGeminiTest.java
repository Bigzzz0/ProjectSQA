package org.jsoup.parser;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.parser.Token and its inner static subclasses:
 *   - Token, Token.Doctype, Token.Tag, Token.StartTag, Token.EndTag, Token.Comment, Token.Character, Token.EOF, Token.TokenType
 *
 * Decision / Condition Branch Coverage:
 * 1. Token.tokenType(): Verifies reflective simple class name mapping for each subclass.
 * 2. Token Type Predicates & Casts:
 *    - isDoctype() / asDoctype(): true & cast success for Doctype; false & ClassCastException for non-Doctype.
 *    - isStartTag() / asStartTag(): true & cast success for StartTag; false & ClassCastException for non-StartTag.
 *    - isEndTag() / asEndTag(): true & cast success for EndTag; false & ClassCastException for non-EndTag.
 *    - isComment() / asComment(): true & cast success for Comment; false & ClassCastException for non-Comment.
 *    - isCharacter() / asCharacter(): true & cast success for Character; false & ClassCastException for non-Character.
 *    - isEOF(): true for EOF; false for other tokens.
 * 3. Token.Tag:
 *    - newAttribute():
 *      - Branch [attributes == null]: lazy instantiation (e.g. EndTag default).
 *      - Branch [pendingAttributeName != null]:
 *        - Sub-branch [pendingAttributeValue == null]: creates Attribute(name, "").
 *        - Sub-branch [pendingAttributeValue != null]: creates Attribute(name, value).
 *      - Branch [pendingAttributeValue != null]: resets via delete(0, length) and reuses buffer.
 *    - finaliseTag():
 *      - Branch [pendingAttributeName != null]: flushes pending attribute.
 *      - Branch [pendingAttributeName == null]: no-op.
 *    - name():
 *      - Branch [tagName.length() == 0]: Validate.isFalse throws IllegalArgumentException.
 *      - Branch [tagName == null]: NullPointerException.
 *      - Branch [valid tagName]: returns tagName.
 *    - name(String): fluent setter.
 *    - appendTagName(String / char): null vs non-null branch concatenation.
 *    - appendAttributeName(String / char): null vs non-null branch concatenation.
 *    - appendAttributeValue(String / char): null (new StringBuilder) vs non-null (append) branch.
 * 4. Token.StartTag.toString():
 *    - Branch [attributes != null && attributes.size() > 0]: returns "<name attrs>".
 *    - Branch [attributes == null]: returns "<name>".
 *    - Branch [attributes != null && attributes.size() == 0]: returns "<name>".
 * 5. Token.EndTag.toString(): returns "</name>".
 * 6. Defect-Targeted Condition:
 *    - Defect in handlesXmlDeclarationAsDeclaration: XML declarations (e.g. <?xml ...?>) erroneously
 *      parsed as Comment instead of XmlDeclaration node.
 * ---------------------------------------------------------------------------------------------------
 */
public class TokenGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoctypeDefaultStateAndGetters() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue("Token should identify as Doctype", doctype.isDoctype());
        assertEquals("Doctype", doctype.tokenType());
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse("forceQuirks should default to false", doctype.isForceQuirks());

        doctype.name.append("html");
        doctype.publicIdentifier.append("-//W3C//DTD HTML 4.01//EN");
        doctype.systemIdentifier.append("http://www.w3.org/TR/html4/strict.dtd");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testStartTagConstructorsAndAttributes() {
        Token.StartTag tag1 = new Token.StartTag();
        assertTrue(tag1.isStartTag());
        assertNotNull("StartTag default constructor must instantiate Attributes", tag1.getAttributes());
        assertEquals(0, tag1.getAttributes().size());

        Token.StartTag tag2 = new Token.StartTag("div");
        assertEquals("div", tag2.name());
        assertFalse(tag2.isSelfClosing());

        Attributes customAttrs = new Attributes();
        customAttrs.put("id", "main");
        Token.StartTag tag3 = new Token.StartTag("span", customAttrs);
        assertEquals("span", tag3.name());
        assertEquals("main", tag3.getAttributes().get("id"));
        assertEquals("<span id=\"main\">", tag3.toString());
    }

    @Test(timeout = 4000)
    public void testEndTagConstructorsAndToString() {
        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertNull("EndTag attributes should initially be null", endTag.getAttributes());

        Token.EndTag endTagNamed = new Token.EndTag("div");
        assertEquals("div", endTagNamed.name());
        assertEquals("</div>", endTagNamed.toString());
    }

    @Test(timeout = 4000)
    public void testCommentDataAndToString() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertEquals("Comment", comment.tokenType());
        assertEquals("", comment.getData());
        assertEquals("<!---->", comment.toString());

        comment.data.append("This is a test comment");
        assertEquals("This is a test comment", comment.getData());
        assertEquals("<!--This is a test comment-->", comment.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterDataAndToString() {
        Token.Character character = new Token.Character("sample text & entities");
        assertTrue(character.isCharacter());
        assertEquals("Character", character.tokenType());
        assertEquals("sample text & entities", character.getData());
        assertEquals("sample text & entities", character.toString());
    }

    @Test(timeout = 4000)
    public void testEOFState() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals("EOF", eof.tokenType());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isComment());
        assertFalse(eof.isCharacter());
        assertFalse(eof.isDoctype());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTagIncrementalAppendOperations() {
        Token.StartTag tag = new Token.StartTag();

        // 1. Tag name appending from null -> initial -> concatenated
        tag.appendTagName("d");
        tag.appendTagName('i');
        tag.appendTagName("v");
        assertEquals("div", tag.name());

        // 2. Attribute name appending from null -> initial -> concatenated
        tag.appendAttributeName("da");
        tag.appendAttributeName('t');
        tag.appendAttributeName("a-id");

        // 3. Attribute value appending from null -> initial -> concatenated
        tag.appendAttributeValue("val");
        tag.appendAttributeValue('-');
        tag.appendAttributeValue("1");

        tag.newAttribute();
        assertEquals("val-1", tag.getAttributes().get("data-id"));

        // 4. Consecutive newAttribute invocation with boolean/empty attribute
        tag.appendAttributeName("disabled");
        tag.newAttribute();
        assertEquals("", tag.getAttributes().get("disabled"));

        // 5. Reuse cleared pendingAttributeValue buffer
        tag.appendAttributeName("title");
        tag.appendAttributeValue("tooltip");
        tag.finaliseTag();
        assertEquals("tooltip", tag.getAttributes().get("title"));
    }

    @Test(timeout = 4000)
    public void testLazyAttributeInstantiationInTag() {
        Token.EndTag tag = new Token.EndTag("a");
        assertNull("Attributes should be null before any attribute operations", tag.attributes);

        // newAttribute with no pending attribute should still initialize attributes map
        tag.newAttribute();
        assertNotNull("Attributes should be lazily instantiated", tag.attributes);
        assertEquals(0, tag.attributes.size());
    }

    @Test(timeout = 4000)
    public void testFinaliseTagNoOpWhenNoPendingAttribute() {
        Token.StartTag tag = new Token.StartTag("meta");
        int countBefore = tag.getAttributes().size();
        tag.finaliseTag();
        assertEquals(countBefore, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testStartTagToStringBranches() {
        Token.StartTag tag = new Token.StartTag("br");

        // Branch 1: attributes != null && size == 0
        assertEquals("<br>", tag.toString());

        // Branch 2: attributes == null
        tag.attributes = null;
        assertEquals("<br>", tag.toString());

        // Branch 3: attributes != null && size > 0
        tag.attributes = new Attributes();
        tag.attributes.put(new Attribute("clear", "all"));
        assertEquals("<br clear=\"all\">", tag.toString());
    }

    @Test(timeout = 4000)
    public void testTagSelfClosingToggle() {
        Token.StartTag tag = new Token.StartTag("img");
        assertFalse(tag.isSelfClosing());
        tag.selfClosing = true;
        assertTrue(tag.isSelfClosing());
        tag.selfClosing = false;
        assertFalse(tag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testTagNameFluentSetter() {
        Token.StartTag tag = new Token.StartTag();
        Token.Tag returned = tag.name("section");
        assertSame("name(String) should return same instance for chaining", tag, returned);
        assertEquals("section", tag.name());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlesXmlDeclarationAsDeclarationDefect() {
        // Ground truth defect: handlesXmlDeclarationAsDeclaration
        // Expected: XML declaration should be parsed as declaration node (#declaration),
        // not erroneously misclassified as a bogus comment (<!--?xml ... ?-->).
        String xml = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");

        assertNotNull("Parsed document should not be null", doc);
        assertTrue("Document must contain child nodes", doc.childNodeSize() > 0);
        assertEquals("First node of an XML declaration must have nodeName '#declaration'",
                "#declaration", doc.childNode(0).nodeName());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagEmptyNameValidationThrowsException() {
        Token.StartTag tag = new Token.StartTag("");
        tag.name();
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testTagNullNameValidationThrowsException() {
        Token.StartTag tag = new Token.StartTag();
        tag.name();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testIllegalCastAsDoctype() {
        Token token = new Token.StartTag("p");
        assertFalse(token.isDoctype());
        token.asDoctype();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testIllegalCastAsStartTag() {
        Token token = new Token.EndTag("p");
        assertFalse(token.isStartTag());
        token.asStartTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testIllegalCastAsEndTag() {
        Token token = new Token.Comment();
        assertFalse(token.isEndTag());
        token.asEndTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testIllegalCastAsComment() {
        Token token = new Token.Character("text");
        assertFalse(token.isComment());
        token.asComment();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testIllegalCastAsCharacter() {
        Token token = new Token.EOF();
        assertFalse(token.isCharacter());
        token.asCharacter();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokenTypeEnumContract() {
        Token.TokenType[] expectedTypes = new Token.TokenType[]{
                Token.TokenType.Doctype,
                Token.TokenType.StartTag,
                Token.TokenType.EndTag,
                Token.TokenType.Comment,
                Token.TokenType.Character,
                Token.TokenType.EOF
        };
        assertArrayEquals(expectedTypes, Token.TokenType.values());

        for (Token.TokenType type : expectedTypes) {
            assertEquals(type, Token.TokenType.valueOf(type.name()));
        }
    }

    @Test(timeout = 4000)
    public void testValidCastsAndTypeChecks() {
        Token doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertSame(doctype, doctype.asDoctype());

        Token startTag = new Token.StartTag("a");
        assertTrue(startTag.isStartTag());
        assertSame(startTag, startTag.asStartTag());

        Token endTag = new Token.EndTag("a");
        assertTrue(endTag.isEndTag());
        assertSame(endTag, endTag.asEndTag());

        Token comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertSame(comment, comment.asComment());

        Token character = new Token.Character("data");
        assertTrue(character.isCharacter());
        assertSame(character, character.asCharacter());

        Token eof = new Token.EOF();
        assertTrue(eof.isEOF());
    }
}