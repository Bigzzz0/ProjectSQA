package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.BooleanAttribute;
import org.jsoup.nodes.Document;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Token and nested subclasses
 *
 * 1. Defect Targeting (Ground Truth):
 *    - DocumentType DOCTYPE token handling with SYSTEM identifiers:
 *      Round-trip parsing of '<!DOCTYPE html SYSTEM "exampledtdfile.dtd">' was failing
 *      to emit the 'SYSTEM ' keyword because publicIdentifier was empty rather than populated.
 *    - Validates Doctype token state when parsing and tokenizing SYSTEM identifiers.
 *
 * 2. Token Class Hierarchy & Reset Branches:
 *    - Token.reset(StringBuilder): branch sb == null vs sb != null.
 *    - Doctype.reset(): resets name, publicIdentifier, systemIdentifier, forceQuirks.
 *    - Tag.reset(): resets tagName, normalName, pending attributes, selfClosing, attributes.
 *    - StartTag.reset(): super.reset() + attributes re-instantiated.
 *    - Comment.reset(): resets data and bogus.
 *    - Character.reset(): resets data to null.
 *    - EOF.reset(): no-op return this.
 *
 * 3. Tag State & Attribute Accumulator Branches:
 *    - Tag.name(): Validate.isFalse(tagName == null || tagName.length() == 0) -> exception branches.
 *    - Tag.appendTagName(String/char): tagName == null (initial) vs tagName != null (concatenation).
 *    - Tag.appendAttributeName(String/char): pendingAttributeName == null vs != null.
 *    - Tag.appendAttributeValue branches:
 *      * String (first chunk stored in pendingAttributeValueS).
 *      * Multiple Strings / Chars / Char[] / CodePoints transitioning to pendingAttributeValue builder.
 *      * hasPendingAttributeValue: pendingAttributeValue.length() > 0 vs pendingAttributeValueS fallback.
 *      * hasEmptyAttributeValue: Attribute(pendingAttributeName, "").
 *      * BooleanAttribute fallback: BooleanAttribute(pendingAttributeName).
 *    - Tag.finaliseTag(): pendingAttributeName != null triggers newAttribute() vs null no-op.
 *    - StartTag.toString(): attributes != null && attributes.size() > 0 vs empty attributes.
 *    - EndTag.toString(): "</" + name() + ">".
 *
 * 4. Token Type Checks & Casts:
 *    - isDoctype(), asDoctype(), isStartTag(), asStartTag(), isEndTag(), asEndTag(),
 *      isComment(), asComment(), isCharacter(), asCharacter(), isEOF().
 */
public class TokenGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokenBaseTypeNames() {
        assertEquals("Doctype", new Token.Doctype().tokenType());
        assertEquals("StartTag", new Token.StartTag().tokenType());
        assertEquals("EndTag", new Token.EndTag().tokenType());
        assertEquals("Comment", new Token.Comment().tokenType());
        assertEquals("Character", new Token.Character().tokenType());
        assertEquals("EOF", new Token.EOF().tokenType());
    }

    @Test(timeout = 4000)
    public void testDoctypeFieldAccessAndReset() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertEquals(Token.TokenType.Doctype, doctype.type);

        doctype.name.append("html");
        doctype.publicIdentifier.append("-//W3C//DTD HTML 4.01//EN");
        doctype.systemIdentifier.append("http://www.w3.org/TR/html4/strict.dtd");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());

        assertSame(doctype, doctype.reset());
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testStartTagAttributesAndReset() {
        Token.StartTag startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertEquals(Token.TokenType.StartTag, startTag.type);
        assertNotNull(startTag.getAttributes());

        startTag.name("DIV");
        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());

        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();

        assertEquals(1, startTag.getAttributes().size());
        assertEquals("main", startTag.getAttributes().get("id"));
        assertEquals("<DIV id=\"main\">", startTag.toString());

        assertSame(startTag, startTag.reset());
        assertNull(startTag.normalName());
        assertNotNull(startTag.getAttributes());
        assertEquals(0, startTag.getAttributes().size());
        assertFalse(startTag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testEndTagFormattingAndReset() {
        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertEquals(Token.TokenType.EndTag, endTag.type);

        endTag.name("SPAN");
        assertEquals("SPAN", endTag.name());
        assertEquals("span", endTag.normalName());
        assertEquals("</SPAN>", endTag.toString());

        assertSame(endTag, endTag.reset());
        assertNull(endTag.normalName());
        assertNull(endTag.getAttributes());
    }

    @Test(timeout = 4000)
    public void testCommentTokenAndReset() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertEquals(Token.TokenType.Comment, comment.type);

        comment.data.append("Hello World");
        comment.bogus = true;
        assertEquals("Hello World", comment.getData());
        assertEquals("<!--Hello World-->", comment.toString());
        assertTrue(comment.bogus);

        assertSame(comment, comment.reset());
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    @Test(timeout = 4000)
    public void testCharacterTokenAndReset() {
        Token.Character character = new Token.Character();
        assertTrue(character.isCharacter());
        assertEquals(Token.TokenType.Character, character.type);

        character.data("text-data");
        assertEquals("text-data", character.getData());
        assertEquals("text-data", character.toString());

        assertSame(character, character.reset());
        assertNull(character.getData());
    }

    @Test(timeout = 4000)
    public void testEOFTokenAndReset() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals(Token.TokenType.EOF, eof.type);
        assertSame(eof, eof.reset());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testResetStringBuilderNullBoundary() {
        // Must handle null safely without NullPointerException
        Token.reset(null);

        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testTagAppendNameFromNullAndConcat() {
        Token.StartTag tag = new Token.StartTag();

        tag.appendTagName("div");
        assertEquals("div", tag.name());
        assertEquals("div", tag.normalName());

        tag.appendTagName("-custom");
        assertEquals("div-custom", tag.name());
        assertEquals("div-custom", tag.normalName());

        tag.appendTagName('1');
        assertEquals("div-custom1", tag.name());
        assertEquals("div-custom1", tag.normalName());
    }

    @Test(timeout = 4000)
    public void testTagAppendAttributeNameFromNullAndConcat() {
        Token.StartTag tag = new Token.StartTag();

        tag.appendAttributeName("data");
        tag.appendAttributeName("-");
        tag.appendAttributeName('v');
        tag.setEmptyAttributeValue();
        tag.newAttribute();

        assertTrue(tag.getAttributes().hasKey("data-v"));
        assertEquals("", tag.getAttributes().get("data-v"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeSingleStringOptimizationPath() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("href");
        tag.appendAttributeValue("https://example.com"); // Only single string hit
        tag.newAttribute();

        assertEquals("https://example.com", tag.getAttributes().get("href"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeMultiValueAppendPaths() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");
        tag.appendAttributeName("value");

        // 1. Initial string (buffered into pendingAttributeValueS)
        tag.appendAttributeValue("part1-");
        // 2. Second string: forces pendingAttributeValueS to move to pendingAttributeValue
        tag.appendAttributeValue("part2-");
        // 3. Char append
        tag.appendAttributeValue('A');
        // 4. Char array append
        tag.appendAttributeValue(new char[]{'-', 'B'});
        // 5. Code points append (Unicode codepoint 65 = 'A')
        tag.appendAttributeValue(new int[]{45, 65});

        tag.newAttribute();

        assertEquals("part1-part2-A-B-A", tag.getAttributes().get("value"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeDirectCharAppendWithoutPriorString() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("p");
        tag.appendAttributeName("class");
        tag.appendAttributeValue('x');
        tag.newAttribute();

        assertEquals("x", tag.getAttributes().get("class"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeDirectCharArrayAppend() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("p");
        tag.appendAttributeName("class");
        tag.appendAttributeValue(new char[]{'a', 'b'});
        tag.newAttribute();

        assertEquals("ab", tag.getAttributes().get("class"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeDirectCodePointsAppend() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("p");
        tag.appendAttributeName("class");
        tag.appendAttributeValue(new int[]{97, 98});
        tag.newAttribute();

        assertEquals("ab", tag.getAttributes().get("class"));
    }

    @Test(timeout = 4000)
    public void testTagBooleanAttributeCreation() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");
        tag.appendAttributeName("disabled");
        tag.newAttribute(); // Neither pendingAttributeValue nor emptyAttributeValue set

        assertTrue(tag.getAttributes().hasKey("disabled"));
        Attribute attr = tag.getAttributes().asList().get(0);
        assertTrue(attr instanceof BooleanAttribute);
        assertEquals("", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testTagNewAttributeWithNullPendingNameNoOp() {
        Token.EndTag tag = new Token.EndTag();
        assertNull(tag.getAttributes());
        tag.newAttribute(); // pendingAttributeName is null
        assertNotNull(tag.getAttributes());
        assertEquals(0, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testTagFinaliseTagBranches() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("img");

        // When pendingAttributeName is null: no-op
        tag.finaliseTag();
        assertEquals(0, tag.getAttributes().size());

        // When pendingAttributeName is not null: creates the attribute
        tag.appendAttributeName("src");
        tag.appendAttributeValue("logo.png");
        tag.finaliseTag();

        assertEquals(1, tag.getAttributes().size());
        assertEquals("logo.png", tag.getAttributes().get("src"));
    }

    @Test(timeout = 4000)
    public void testStartTagNameAttrAndToStringVariants() {
        Token.StartTag tag = new Token.StartTag();
        Attributes attrs = new Attributes();
        attrs.put("k", "v");
        tag.nameAttr("SPAN", attrs);

        assertEquals("SPAN", tag.name());
        assertEquals("span", tag.normalName());
        assertEquals("<SPAN k=\"v\">", tag.toString());

        Token.StartTag emptyAttrTag = new Token.StartTag();
        emptyAttrTag.name("br");
        assertEquals("<br>", emptyAttrTag.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: org.jsoup.nodes.DocumentTypeTest::testRoundTrip
     * DOCTYPE with SYSTEM identifier must preserve the SYSTEM identifier tokenization
     * and round-trip properly: expected '<!DOCTYPE html SYSTEM "exampledtdfile.dtd">'
     * but defectively emitted '<!DOCTYPE html "exampledtdfile.dtd">'.
     */
    @Test(timeout = 4000)
    public void testDoctypeRoundTripSystemDefect() {
        String systemDoc = "<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">";
        Document doc = Jsoup.parse(systemDoc);
        assertEquals(systemDoc, doc.childNode(0).outerHtml());
    }

    @Test(timeout = 4000)
    public void testDoctypeDirectTokenPublicAndSystemIdentifiers() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.systemIdentifier.append("exampledtdfile.dtd");

        assertEquals("html", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("exampledtdfile.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
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
    public void testAsDoctypeCastException() {
        Token token = new Token.Comment();
        token.asDoctype();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testAsStartTagCastException() {
        Token token = new Token.EndTag();
        token.asStartTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testAsEndTagCastException() {
        Token token = new Token.StartTag();
        token.asEndTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testAsCommentCastException() {
        Token token = new Token.Character();
        token.asComment();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testAsCharacterCastException() {
        Token token = new Token.EOF();
        token.asCharacter();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCastingAndTypePredicatesConsistency() {
        Token doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isEOF());
        assertSame(doctype, doctype.asDoctype());

        Token startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isDoctype());
        assertSame(startTag, startTag.asStartTag());

        Token endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertSame(endTag, endTag.asEndTag());

        Token comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertSame(comment, comment.asComment());

        Token character = new Token.Character();
        assertTrue(character.isCharacter());
        assertSame(character, character.asCharacter());

        Token eof = new Token.EOF();
        assertTrue(eof.isEOF());
    }

    @Test(timeout = 4000)
    public void testSelfClosingProperty() {
        Token.StartTag tag = new Token.StartTag();
        assertFalse(tag.isSelfClosing());
        tag.selfClosing = true;
        assertTrue(tag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testTokenTypeEnumValues() {
        Token.TokenType[] expected = new Token.TokenType[]{
            Token.TokenType.Doctype,
            Token.TokenType.StartTag,
            Token.TokenType.EndTag,
            Token.TokenType.Comment,
            Token.TokenType.Character,
            Token.TokenType.EOF
        };
        assertArrayEquals(expected, Token.TokenType.values());
        assertEquals(Token.TokenType.Doctype, Token.TokenType.valueOf("Doctype"));
    }
}