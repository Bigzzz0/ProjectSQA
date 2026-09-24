package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.parser.Token (and inner classes: Doctype, Tag, StartTag, EndTag, Comment, Character, EOF, TokenType)
 *
 * Decision / Branch Coverage Targets:
 * 1. Token.reset(StringBuilder):
 *    - sb == null (no-op guard)
 *    - sb != null (clears builder)
 * 2. Token Type Queries & Casts:
 *    - isDoctype / asDoctype, isStartTag / asStartTag, isEndTag / asEndTag,
 *      isComment / asComment, isCharacter / asCharacter, isEOF (true vs false paths, and invalid cast branches)
 * 3. Tag.name():
 *    - tagName == null -> IllegalArgumentException
 *    - tagName.length() == 0 -> IllegalArgumentException
 *    - valid tagName -> returns name preserving case; normalName() in lowercase
 * 4. Tag.newAttribute() Branches:
 *    - attributes == null (lazy instantiation in Tag vs pre-instantiated in StartTag)
 *    - pendingAttributeName == null (no-op)
 *    - pendingAttributeName != null:
 *      * hasPendingAttributeValue == true:
 *          - pendingAttributeValue.length() > 0 (accumulated builder)
 *          - pendingAttributeValue.length() == 0 (single shot pendingAttributeValueS)
 *      * hasEmptyAttributeValue == true -> Attribute with empty string value
 *      * hasPendingAttributeValue == false && hasEmptyAttributeValue == false -> BooleanAttribute
 * 5. Tag.ensureAttributeValue():
 *    - pendingAttributeValueS != null (flushes single-shot string into pendingAttributeValue builder)
 *    - pendingAttributeValueS == null
 * 6. Tag.appendAttributeValue variants:
 *    - append(String), append(char), append(char[]), append(int[] codepoints)
 * 7. Tag.finaliseTag():
 *    - pendingAttributeName != null -> calls newAttribute()
 *    - pendingAttributeName == null -> no-op
 * 8. StartTag.toString() & EndTag.toString():
 *    - attributes != null && attributes.size() > 0 -> "<tag attr>"
 *    - attributes == null || attributes.size() == 0 -> "<tag>"
 *    - EndTag -> "</tag>"
 * 9. Comment & Character & Doctype lifecycle, state accessors, and reset() execution
 * 10. KNOWN DEFECT SPECIFICATION:
 *     - Defects4J Bug: When pendingAttributeName collapses to empty after trim() (e.g. from whitespace or
 *       control characters), the defective code executes `new Attribute("")` or `new BooleanAttribute("")`
 *       which triggers `Validate.notEmpty(key)` -> `IllegalArgumentException: String must not be empty`.
 *       The expected correct behavior is to safely discard or handle empty attribute names without throwing.
 */
public class TokenGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoctypeCoreOperations() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertEquals("Doctype", doctype.tokenType());
        assertSame(doctype, doctype.asDoctype());

        doctype.name.append("html");
        doctype.pubSysKey = "SYSTEM";
        doctype.publicIdentifier.append("public_id");
        doctype.systemIdentifier.append("system_id");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("SYSTEM", doctype.getPubSysKey());
        assertEquals("public_id", doctype.getPublicIdentifier());
        assertEquals("system_id", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());

        doctype.reset();
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
        assertEquals("StartTag", startTag.tokenType());
        assertSame(startTag, startTag.asStartTag());

        startTag.name("DIV");
        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());
        assertFalse(startTag.isSelfClosing());
        assertEquals("<DIV>", startTag.toString());

        startTag.selfClosing = true;
        assertTrue(startTag.isSelfClosing());

        // Single shot attribute value
        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();

        assertNotNull(startTag.getAttributes());
        assertTrue(startTag.getAttributes().hasKey("id"));
        assertEquals("main", startTag.getAttributes().get("id"));
        assertEquals("<DIV id=\"main\">", startTag.toString());

        // Reset verification
        startTag.reset();
        assertEquals(0, startTag.getAttributes().size());
        assertFalse(startTag.isSelfClosing());
        assertNull(startTag.normalName());
    }

    @Test(timeout = 4000)
    public void testEndTagCoreOperationsAndToString() {
        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertEquals("EndTag", endTag.tokenType());
        assertSame(endTag, endTag.asEndTag());

        endTag.name("P");
        assertEquals("P", endTag.name());
        assertEquals("p", endTag.normalName());
        assertEquals("</P>", endTag.toString());

        endTag.reset();
        assertNull(endTag.normalName());
        assertNull(endTag.getAttributes());
    }

    @Test(timeout = 4000)
    public void testCommentCoreOperations() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertEquals("Comment", comment.tokenType());
        assertSame(comment, comment.asComment());

        comment.data.append("sample comment");
        comment.bogus = true;
        assertEquals("sample comment", comment.getData());
        assertEquals("<!--sample comment-->", comment.toString());
        assertTrue(comment.bogus);

        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    @Test(timeout = 4000)
    public void testCharacterCoreOperations() {
        Token.Character character = new Token.Character();
        assertTrue(character.isCharacter());
        assertEquals("Character", character.tokenType());
        assertSame(character, character.asCharacter());

        character.data("text data");
        assertEquals("text data", character.getData());
        assertEquals("text data", character.toString());

        character.reset();
        assertNull(character.getData());
    }

    @Test(timeout = 4000)
    public void testEOFCoreOperations() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals("EOF", eof.tokenType());
        assertSame(eof, eof.reset());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testResetStaticHelperWithNullAndNonNull() {
        Token.reset((StringBuilder) null); // Must not throw NullPointerException

        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTagAppendTagNameIncremental() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName("h");
        tag.appendTagName("1");
        tag.appendTagName('A');
        assertEquals("h1A", tag.name());
        assertEquals("h1a", tag.normalName());
    }

    @Test(timeout = 4000)
    public void testTagAppendAttributeNameIncremental() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");
        tag.appendAttributeName("req");
        tag.appendAttributeName("uired");
        tag.appendAttributeName('!');
        tag.newAttribute();

        assertTrue(tag.getAttributes().hasKey("required!"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueTransitionsSingleShotToBuilder() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("href");
        // 1st append -> stored in pendingAttributeValueS
        tag.appendAttributeValue("http://");
        // 2nd append -> forces ensureAttributeValue() to flush pendingAttributeValueS into builder
        tag.appendAttributeValue("example.com");
        // append char
        tag.appendAttributeValue('/');
        // append char array
        tag.appendAttributeValue(new char[]{'t', 'e', 's', 't'});
        // append codepoints (Unicode supplementary character U+1F600 😀)
        tag.appendAttributeValue(new int[]{0x1F600});

        tag.newAttribute();
        String expectedVal = "http://example.com/test\uD83D\uDE00";
        assertEquals(expectedVal, tag.getAttributes().get("href"));
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeAndEmptyStringAttributeCreation() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");

        // 1. Boolean attribute (no value appended, empty value flag not set)
        tag.appendAttributeName("disabled");
        tag.newAttribute();
        assertTrue(tag.getAttributes().hasKey("disabled"));

        // 2. Empty string attribute (setEmptyAttributeValue() explicitly called)
        tag.appendAttributeName("value");
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertTrue(tag.getAttributes().hasKey("value"));
        assertEquals("", tag.getAttributes().get("value"));
    }

    @Test(timeout = 4000)
    public void testFinaliseTagInvokesNewAttributeIfPending() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("meta");
        tag.appendAttributeName("charset");
        tag.appendAttributeValue("UTF-8");
        tag.finaliseTag();

        assertTrue(tag.getAttributes().hasKey("charset"));
        assertEquals("UTF-8", tag.getAttributes().get("charset"));

        // Calling finaliseTag again when pendingAttributeName is null should be a safe no-op
        tag.finaliseTag();
        assertEquals(1, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testNewAttributeWhenAttributesIsNullInitializesLazily() {
        // EndTag initializes attributes as null
        Token.EndTag endTag = new Token.EndTag();
        assertNull(endTag.getAttributes());

        endTag.appendAttributeName("class");
        endTag.appendAttributeValue("hide");
        endTag.newAttribute();

        assertNotNull(endTag.getAttributes());
        assertEquals(1, endTag.getAttributes().size());
        assertEquals("hide", endTag.getAttributes().get("class"));
    }

    @Test(timeout = 4000)
    public void testStartTagNameAttrMethod() {
        Token.StartTag startTag = new Token.StartTag();
        org.jsoup.nodes.Attributes attrs = new org.jsoup.nodes.Attributes();
        attrs.put("key", "val");

        Token.StartTag result = startTag.nameAttr("SPAN", attrs);
        assertSame(startTag, result);
        assertEquals("SPAN", startTag.name());
        assertEquals("span", startTag.normalName());
        assertSame(attrs, startTag.getAttributes());
        assertEquals("<SPAN key=\"val\">", startTag.toString());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT:
     * - HtmlParserTest::handlesControlCodeInAttributeName
     * - CleanerTest::handlesControlCharactersAfterTagName
     *
     * In the defective implementation:
     * pendingAttributeName is trimmed via `pendingAttributeName.trim();`.
     * When pendingAttributeName consists exclusively of whitespace or control characters,
     * trimming collapses it to an empty string ("").
     * The subsequent call `new Attribute(pendingAttributeName, ...)` or `new BooleanAttribute(pendingAttributeName)`
     * throws `IllegalArgumentException: String must not be empty` via Validate.notEmpty(key).
     *
     * Correct behavior:
     * Control characters/whitespace collapsing to an empty attribute name should be ignored or safely dropped
     * without throwing an IllegalArgumentException.
     */
    @Test(timeout = 4000)
    public void testNewAttributeControlCharsCollapsingToEmptyAttributeNameDoesNotThrow() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");
        // Pending attribute name containing only whitespace/control code that collapses to empty
        tag.appendAttributeName("   ");
        tag.newAttribute();

        assertEquals("Tag should not contain an attribute with an empty name", 0, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testFinaliseTagControlCharsCollapsingToEmptyAttributeNameDoesNotThrow() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("span");
        tag.appendAttributeName(" \t \r \n ");
        tag.appendAttributeValue("someVal");
        tag.finaliseTag();

        assertEquals("Collapsed whitespace attribute must be safely dropped", 0, tag.getAttributes().size());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagThrowsExceptionWhenNameCalledOnNullTagName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTagThrowsExceptionWhenNameCalledOnEmptyTagName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("");
        tag.name();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastingAsDoctypeThrowsClassCastException() {
        Token character = new Token.Character();
        character.asDoctype();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastingAsStartTagThrowsClassCastException() {
        Token comment = new Token.Comment();
        comment.asStartTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastingAsEndTagThrowsClassCastException() {
        Token doctype = new Token.Doctype();
        doctype.asEndTag();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastingAsCommentThrowsClassCastException() {
        Token startTag = new Token.StartTag();
        startTag.asComment();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testInvalidCastingAsCharacterThrowsClassCastException() {
        Token eof = new Token.EOF();
        eof.asCharacter();
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokenTypeEnumContract() {
        Token.TokenType[] expectedTypes = {
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
    public void testTypeCheckConsistencyAcrossAllImplementations() {
        Token doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isEOF());

        Token startTag = new Token.StartTag();
        assertFalse(startTag.isDoctype());
        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isEndTag());
        assertFalse(startTag.isComment());
        assertFalse(startTag.isCharacter());
        assertFalse(startTag.isEOF());

        Token endTag = new Token.EndTag();
        assertFalse(endTag.isDoctype());
        assertFalse(endTag.isStartTag());
        assertTrue(endTag.isEndTag());
        assertFalse(endTag.isComment());
        assertFalse(endTag.isCharacter());
        assertFalse(endTag.isEOF());

        Token comment = new Token.Comment();
        assertFalse(comment.isDoctype());
        assertFalse(comment.isStartTag());
        assertFalse(comment.isEndTag());
        assertTrue(comment.isComment());
        assertFalse(comment.isCharacter());
        assertFalse(comment.isEOF());

        Token character = new Token.Character();
        assertFalse(character.isDoctype());
        assertFalse(character.isStartTag());
        assertFalse(character.isEndTag());
        assertFalse(character.isComment());
        assertTrue(character.isCharacter());
        assertFalse(character.isEOF());

        Token eof = new Token.EOF();
        assertFalse(eof.isDoctype());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isComment());
        assertFalse(eof.isCharacter());
        assertTrue(eof.isEOF());
    }

    @Test(timeout = 4000)
    public void testTagToStringWithNullAttributes() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        assertEquals("</div>", endTag.toString());
        assertNull(endTag.getAttributes());

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("br");
        startTag.attributes = null;
        assertEquals("<br>", startTag.toString());
    }
}