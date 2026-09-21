package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for Token class, targeting line/branch coverage and the known attribute duplication defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Branch coverage: all Token subclasses (Doctype, Tag, StartTag, EndTag, Comment, Character, CData, EOF)
 * - Decision branches: isDoctype, isStartTag, isEndTag, isComment, isCharacter, isCData, isEOF and casting
 * - Boundary conditions: null/empty tag name, null/empty attribute name/value, reset with null StringBuilder
 * - Defect target: newAttribute logic – first attribute of same name (case-sensitive) must be preserved.
 *   (Defects4J: attributes.put does not keep first duplicate; later ones overwrite.)
 * - State transitions: reset() clears fields; append operations accumulate.
 * - Exception paths: name() throws for null/empty tagName; finaliseTag handles pending attribute.
 */
public class TokenDeepseekTest {

    // ===================== Partition A: Core Functional Logic & State Transitions =====================

    @Test(timeout = 4000)
    public void testDoctypeCore() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertEquals("Doctype", doctype.tokenType());

        // initial state
        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());

        // set values via direct field access (package-private)
        doctype.name.append("html");
        doctype.pubSysKey = "PUBLIC";
        doctype.publicIdentifier.append("-//W3C//DTD XHTML 1.0//EN");
        doctype.systemIdentifier.append("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("PUBLIC", doctype.getPubSysKey());
        assertEquals("-//W3C//DTD XHTML 1.0//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());

        // reset
        Token resetResult = doctype.reset();
        assertSame(doctype, resetResult);
        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testStartTagCore() {
        Token.StartTag startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertEquals("StartTag", startTag.tokenType());

        // nameAttr sets tagName, normalName, attributes
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        startTag.nameAttr("div", attrs);
        assertEquals("div", startTag.name());
        assertEquals("div", startTag.normalName());
        assertNotNull(startTag.getAttributes());
        assertEquals("main", startTag.getAttributes().get("id"));

        // reset
        startTag.reset();
        assertNull(startTag.name()); // tagName is null after reset
        assertNull(startTag.normalName());
        // after reset, attributes is new empty, but we haven't set it? Actually StartTag.reset() sets attributes = new Attributes()
        // so getAttributes() returns non-null empty Attributes
        assertNotNull(startTag.getAttributes());
        assertEquals(0, startTag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testEndTagCore() {
        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertEquals("EndTag", endTag.tokenType());

        endTag.name("span");
        assertEquals("span", endTag.name());
        assertEquals("span", endTag.normalName());
        assertFalse(endTag.isSelfClosing());

        // EndTag reuses Tag methods
        endTag.selfClosing = true;
        assertTrue(endTag.isSelfClosing());

        // reset
        endTag.reset();
        assertNull(endTag.name());
    }

    @Test(timeout = 4000)
    public void testCommentCore() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertEquals("Comment", comment.tokenType());

        comment.data.append("hello world");
        comment.bogus = true;
        assertEquals("hello world", comment.getData());
        assertTrue(comment.bogus);

        // reset
        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    @Test(timeout = 4000)
    public void testCharacterCore() {
        Token.Character character = new Token.Character();
        assertTrue(character.isCharacter());
        assertFalse(character.isCData());
        assertEquals("Character", character.tokenType());

        character.data("some text");
        assertEquals("some text", character.getData());

        // reset
        character.reset();
        assertNull(character.getData());

        // data() returns self
        assertSame(character, character.data("new"));
    }

    @Test(timeout = 4000)
    public void testCDataCore() {
        Token.CData cdata = new Token.CData("<p>test</p>");
        assertTrue(cdata.isCData());
        assertTrue(cdata.isCharacter()); // CData extends Character
        assertEquals("Character", cdata.tokenType()); // tokenType returns class simple name, which is "CData"? Wait: tokenType() returns this.getClass().getSimpleName() -> "CData". Yes.
        assertEquals("CData", cdata.tokenType());
        assertEquals("<p>test</p>", cdata.getData());

        // override toString
        assertEquals("<![CDATA[<p>test</p>]]>", cdata.toString());

        // reset from parent
        cdata.reset();
        assertNull(cdata.getData());
    }

    @Test(timeout = 4000)
    public void testEOFCore() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals("EOF", eof.tokenType());

        Token resetResult = eof.reset();
        assertSame(eof, resetResult);
    }

    // ===================== Partition B: Boundary Value Analysis & Extremes =====================

    @Test(timeout = 4000)
    public void testDoctypeEmptyValues() {
        Token.Doctype doctype = new Token.Doctype();
        // all zero-length strings by default
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertNull(doctype.getPubSysKey());

        // reset with null StringBuilder (covered by static method)
        StringBuilder sb = null;
        Token.reset(sb); // should not throw
        // non-null StringBuilder
        StringBuilder sb2 = new StringBuilder("abc");
        Token.reset(sb2);
        assertEquals("", sb2.toString());
    }

    @Test(timeout = 4000)
    public void testTagNullEmptyNameEdge() {
        Token.StartTag startTag = new Token.StartTag();
        // name() throws when tagName is null or empty
        try {
            startTag.name();
            fail("Expected IllegalStateException for null tagName");
        } catch (IllegalStateException e) {
            // expected
        }

        // empty string via name()
        try {
            startTag.name("");
            fail("Expected exception for empty name");
        } catch (IllegalStateException e) {
            // expected
        }

        // valid name
        startTag.name("valid");
        assertEquals("valid", startTag.name());
    }

    @Test(timeout = 4000)
    public void testTagAttributeAppendBoundary() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("p");

        // Empty attribute name trimmed
        tag.appendAttributeName("  "); // whitespace
        tag.appendAttributeValue("value");
        tag.newAttribute(); // should not add because trimmed name is empty
        assertNull(tag.getAttributes().get("  "));
        assertEquals(0, tag.getAttributes().size());

        // Empty attribute value edge: setEmptyAttributeValue vs hasPendingAttributeValue false; both produce ""
        tag.appendAttributeName("empty");
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertEquals("", tag.getAttributes().get("empty"));

        // Null value (no value set)
        tag.appendAttributeName("nullval");
        tag.newAttribute(); // no value set => value = null
        assertNull(tag.getAttributes().get("nullval"));

        // Append attribute value from char array
        tag.appendAttributeName("arr");
        tag.appendAttributeValue(new char[]{'a','b','c'});
        tag.newAttribute();
        assertEquals("abc", tag.getAttributes().get("arr"));

        // Append attribute value from int[] codepoints
        tag.appendAttributeName("codepoint");
        tag.appendAttributeValue(new int[]{65, 8364}); // A and Euro sign
        tag.newAttribute();
        // The string would be "A€" - length 2, codepoint count 2.
        assertEquals("A€", tag.getAttributes().get("codepoint"));
    }

    @Test(timeout = 4000)
    public void testTagFinaliseEdge() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("img");
        tag.appendAttributeName("src");
        tag.appendAttributeValue("pic.jpg");
        // finaliseTag without calling newAttribute -> should call newAttribute internally
        tag.finaliseTag();
        assertNotNull(tag.getAttributes());
        assertEquals("pic.jpg", tag.getAttributes().get("src"));
    }

    // ===================== Partition C: Defect-Targeted Branch Zone =====================

    @Test(timeout = 4000)
    public void testRetainsAttributesOfDifferentCaseIfSensitive() {
        // This test targets the known defect: when duplicate attributes (same name but different case) are added,
        // the first value must be preserved for each distinct case.
        Token.StartTag tag = new Token.StartTag();
        tag.name("p");

        // Simulate building attributes in order: One="One", one="Two", two="Three", One="Four", two="Five", Two="Six"
        // According to expected behavior, the first value for each case-sensitive name should be kept.
        tag.appendAttributeName("One");
        tag.appendAttributeValue("One");
        tag.newAttribute();

        tag.appendAttributeName("one");
        tag.appendAttributeValue("Two");
        tag.newAttribute();

        tag.appendAttributeName("two");
        tag.appendAttributeValue("Three");
        tag.newAttribute();

        // duplicate "One" (same case) - should be ignored, keep "One"
        tag.appendAttributeName("One");
        tag.appendAttributeValue("Four");
        tag.newAttribute();

        // duplicate "two" (same case) - should be ignored, keep "Three"
        tag.appendAttributeName("two");
        tag.appendAttributeValue("Five");
        tag.newAttribute();

        // "Two" different case
        tag.appendAttributeName("Two");
        tag.appendAttributeValue("Six");
        tag.newAttribute();

        // Check attributes: order should be: One="One", one="Two", two="Three", Two="Six"
        // Note: duplicates are dropped, but order of first occurrence is kept.
        // The bug from Defects4J showed that later values overwrote earlier ones.
        assertEquals("One", tag.getAttributes().get("One"));
        assertEquals("Two", tag.getAttributes().get("one"));
        assertEquals("Three", tag.getAttributes().get("two"));
        assertEquals("Six", tag.getAttributes().get("Two"));

        // Also ensure that the size is correct (4 unique keys)
        assertEquals(4, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testDropsDuplicateAttributesSameCase() {
        // Test that duplicate attributes with exactly same name are dropped (first kept)
        Token.StartTag tag = new Token.StartTag();
        tag.name("p");

        tag.appendAttributeName("one");
        tag.appendAttributeValue("First");
        tag.newAttribute();

        tag.appendAttributeName("one");
        tag.appendAttributeValue("Second");
        tag.newAttribute();

        tag.appendAttributeName("two");
        tag.appendAttributeValue("Third");
        tag.newAttribute();

        // duplicate "two"
        tag.appendAttributeName("two");
        tag.appendAttributeValue("Fourth");
        tag.newAttribute();

        // Expected: one="First", two="Third" (first values preserved)
        assertEquals("First", tag.getAttributes().get("one"));
        assertEquals("Third", tag.getAttributes().get("two"));
        assertEquals(2, tag.getAttributes().size());
    }

    // ===================== Partition D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000)
    public void testNameThrowsOnNullEmpty() {
        Token.StartTag tag = new Token.StartTag();
        // null tagName
        try {
            tag.name();
            fail();
        } catch (IllegalStateException e) {
            // expected
        }

        // empty string via name()
        try {
            tag.name("");
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEnsureAttributeValueTransition() {
        // Test the transition from pendingAttributeValueS to StringBuilder
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("href");
        tag.appendAttributeValue("http://"); // sets pendingAttributeValueS
        tag.appendAttributeValue("example.com"); // triggers ensureAttributeValue, appends to StringBuilder
        tag.newAttribute();
        assertEquals("http://example.com", tag.getAttributes().get("href"));
    }

    // ===================== Partition E: Object Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testResetReturnsThis() {
        Token.Doctype doctype = new Token.Doctype();
        assertSame(doctype, doctype.reset());

        Token.StartTag startTag = new Token.StartTag();
        assertSame(startTag, startTag.reset());

        Token.EndTag endTag = new Token.EndTag();
        assertSame(endTag, endTag.reset());

        Token.Comment comment = new Token.Comment();
        assertSame(comment, comment.reset());

        Token.Character character = new Token.Character();
        assertSame(character, character.reset());

        Token.EOF eof = new Token.EOF();
        assertSame(eof, eof.reset());
    }

    @Test(timeout = 4000)
    public void testToStringOverrides() {
        // Doctype (no toString override, uses Object, so not tested here)
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        assertEquals("<div>", startTag.toString());

        Attributes attrs = new Attributes();
        attrs.put("class", "main");
        startTag.nameAttr("span", attrs);
        assertEquals("<span class=\"main\">", startTag.toString());

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("p");
        assertEquals("</p>", endTag.toString());

        Token.Comment comment = new Token.Comment();
        comment.data.append("note");
        assertEquals("<!--note-->", comment.toString());

        Token.Character character = new Token.Character();
        character.data("text");
        assertEquals("text", character.toString());

        Token.CData cdata = new Token.CData("data");
        assertEquals("<![CDATA[data]]>", cdata.toString());
    }

    @Test(timeout = 4000)
    public void testTokenTypeAndCasting() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isCData());
        assertFalse(doctype.isEOF());
        assertSame(doctype, doctype.asDoctype());

        Token.StartTag startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertSame(startTag, startTag.asStartTag());

        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertSame(endTag, endTag.asEndTag());

        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertSame(comment, comment.asComment());

        Token.Character character = new Token.Character();
        assertTrue(character.isCharacter());
        assertFalse(character.isCData());
        assertSame(character, character.asCharacter());

        Token.CData cdata = new Token.CData("");
        assertTrue(cdata.isCharacter());
        assertTrue(cdata.isCData());

        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
    }
}