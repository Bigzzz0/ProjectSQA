package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for Token.java targeting maximum line/branch coverage and the known defect
 * where control characters in attribute names cause IllegalArgumentException.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic and state transitions for all token types (Doctype, Tag, StartTag, EndTag, Comment, Character, EOF)
 * - Partition B: Boundary Value Analysis – null/empty strings, control characters, extreme lengths, zero/negative values
 * - Partition C: Defect-targeted branch – empty attribute name after trim (control characters), empty tag name, empty public/system identifiers
 * - Partition D: Exception and defensive guard paths – invalid arguments, out-of-range parameters, null checks
 * - Partition E: Object lifecycle and contract integrity – reset(), toString(), type checks, casting
 *
 * Known defect: org.jsoup.parser.HtmlParserTest::handlesControlCodeInAttributeName and
 * org.jsoup.safety.CleanerTest::handlesControlCharactersAfterTagName throw
 * IllegalArgumentException: String must not be empty when attribute name becomes empty after trimming.
 * This test suite includes dedicated tests that trigger this path and assert correct behavior (no exception).
 */
public class TokenDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDoctypeResetAndGetters() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertEquals("Doctype", doctype.tokenType());

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

        // Reset and verify
        doctype.reset();
        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testStartTagConstructionAndToString() {
        Token.StartTag startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertEquals("StartTag", startTag.tokenType());
        assertNotNull(startTag.getAttributes());
        assertEquals(0, startTag.getAttributes().size());

        startTag.name("div");
        assertEquals("div", startTag.name());
        assertEquals("div", startTag.normalName());
        assertFalse(startTag.isSelfClosing());

        // toString with attributes
        startTag.getAttributes().put("class", "container");
        assertEquals("<div class=\"container\">", startTag.toString());

        // toString without attributes
        Token.StartTag emptyTag = new Token.StartTag();
        emptyTag.name("br");
        assertEquals("<br>", emptyTag.toString());
    }

    @Test(timeout = 4000)
    public void testEndTagConstructionAndToString() {
        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertEquals("EndTag", endTag.tokenType());

        endTag.name("p");
        assertEquals("</p>", endTag.toString());
        assertEquals("p", endTag.name());
    }

    @Test(timeout = 4000)
    public void testCommentConstructionAndToString() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertEquals("Comment", comment.tokenType());

        comment.data.append("test comment");
        assertEquals("test comment", comment.getData());
        assertEquals("<!--test comment-->", comment.toString());

        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    @Test(timeout = 4000)
    public void testCharacterToken() {
        Token.Character character = new Token.Character();
        assertTrue(character.isCharacter());
        assertEquals("Character", character.tokenType());

        character.data("hello");
        assertEquals("hello", character.getData());
        assertEquals("hello", character.toString());

        character.reset();
        assertNull(character.getData());
    }

    @Test(timeout = 4000)
    public void testEOFToken() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals("EOF", eof.tokenType());

        // reset returns same instance
        assertSame(eof, eof.reset());
    }

    @Test(timeout = 4000)
    public void testTagNameAndNormalName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("MyTag");
        assertEquals("MyTag", tag.name());
        assertEquals("mytag", tag.normalName());

        // appendTagName
        tag.appendTagName("Extra");
        assertEquals("MyTagExtra", tag.name());
        assertEquals("mytagextra", tag.normalName());

        // appendTagName char
        tag.appendTagName('!');
        assertEquals("MyTagExtra!", tag.name());
    }

    @Test(timeout = 4000)
    public void testTagSelfClosingFlag() {
        Token.StartTag tag = new Token.StartTag();
        assertFalse(tag.isSelfClosing());
        tag.selfClosing = true;
        assertTrue(tag.isSelfClosing());
    }

    @Test(timeout = 4000)
    public void testTagAttributesBasic() {
        Token.StartTag tag = new Token.StartTag();
        assertNotNull(tag.getAttributes());
        assertEquals(0, tag.getAttributes().size());

        // Simulate attribute accumulation via newAttribute
        tag.appendAttributeName("id");
        tag.appendAttributeValue("main");
        tag.newAttribute();
        assertEquals(1, tag.getAttributes().size());
        assertEquals("main", tag.getAttributes().get("id"));

        // Boolean attribute
        tag.appendAttributeName("disabled");
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertEquals(2, tag.getAttributes().size());
        assertTrue(tag.getAttributes().hasKey("disabled"));
        assertEquals("", tag.getAttributes().get("disabled"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendMultiple() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("class");
        tag.appendAttributeValue("btn");
        tag.appendAttributeValue(" btn-primary");
        tag.newAttribute();
        assertEquals("btn btn-primary", tag.getAttributes().get("class"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendChar() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("data-val");
        tag.appendAttributeValue('X');
        tag.newAttribute();
        assertEquals("X", tag.getAttributes().get("data-val"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendCharArray() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("name");
        tag.appendAttributeValue(new char[]{'a', 'b', 'c'});
        tag.newAttribute();
        assertEquals("abc", tag.getAttributes().get("name"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendCodepoints() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("unicode");
        tag.appendAttributeValue(new int[]{0x1F600, 0x1F601});
        tag.newAttribute();
        assertEquals("\uD83D\uDE00\uD83D\uDE01", tag.getAttributes().get("unicode"));
    }

    @Test(timeout = 4000)
    public void testTagFinaliseWithPendingAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("checked");
        tag.setEmptyAttributeValue();
        tag.finaliseTag();
        assertEquals(1, tag.getAttributes().size());
        assertTrue(tag.getAttributes().hasKey("checked"));
    }

    @Test(timeout = 4000)
    public void testTagFinaliseWithoutPendingAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.finaliseTag(); // should do nothing
        assertEquals(0, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testStartTagNameAttr() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/");
        Token.StartTag tag = new Token.StartTag();
        tag.nameAttr("a", attrs);
        assertEquals("a", tag.name());
        assertEquals("a", tag.normalName());
        assertSame(attrs, tag.getAttributes());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testDoctypeEmptyIdentifiers() {
        Token.Doctype doctype = new Token.Doctype();
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertNull(doctype.getPubSysKey());
        assertFalse(doctype.isForceQuirks());
    }

    @Test(timeout = 4000)
    public void testTagNameEmptyThrows() {
        Token.StartTag tag = new Token.StartTag();
        try {
            tag.name(); // should throw because tagName is null
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTagNameEmptyAfterReset() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");
        tag.reset();
        try {
            tag.name(); // tagName is null after reset
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAppendTagNameFromNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName("span");
        assertEquals("span", tag.name());
    }

    @Test(timeout = 4000)
    public void testAppendAttributeNameFromNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("class");
        tag.newAttribute();
        assertEquals(1, tag.getAttributes().size());
    }

    @Test(timeout = 4000)
    public void testAttributeValueWithEmptyString() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("data-empty");
        tag.appendAttributeValue("");
        tag.newAttribute();
        assertEquals("", tag.getAttributes().get("data-empty"));
    }

    @Test(timeout = 4000)
    public void testAttributeValueWithNullString() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("test");
        // pendingAttributeValueS remains null, hasPendingAttributeValue false
        // but we set hasEmptyAttributeValue to true
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertEquals("", tag.getAttributes().get("test"));
    }

    @Test(timeout = 4000)
    public void testMultipleResets() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.reset();
        doctype.reset(); // second reset should be safe
        assertEquals("", doctype.getName());
    }

    @Test(timeout = 4000)
    public void testCommentBogusFlag() {
        Token.Comment comment = new Token.Comment();
        assertFalse(comment.bogus);
        comment.bogus = true;
        assertTrue(comment.bogus);
        comment.reset();
        assertFalse(comment.bogus);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testAttributeNameWithControlCharacters() {
        // This test targets the known defect: control characters in attribute name
        // that become empty after trim() cause IllegalArgumentException.
        Token.StartTag tag = new Token.StartTag();
        // Use a control character that is trimmed (e.g., \u0000)
        tag.appendAttributeName("\u0000");
        // The pendingAttributeName after trim becomes empty.
        // On defective version, newAttribute() throws IllegalArgumentException.
        // On fixed version, it should handle gracefully (e.g., skip or create attribute with empty name?).
        // We assert that no exception is thrown.
        try {
            tag.newAttribute();
            // If we reach here, the bug is fixed or the behavior is acceptable.
            // The test passes on fixed version, fails on defective.
        } catch (IllegalArgumentException e) {
            fail("Control character in attribute name should not cause IllegalArgumentException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAttributeNameWithOnlyWhitespace() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("   "); // spaces only, trim to empty
        try {
            tag.newAttribute();
        } catch (IllegalArgumentException e) {
            fail("Whitespace-only attribute name should not cause IllegalArgumentException");
        }
    }

    @Test(timeout = 4000)
    public void testAttributeNameWithMixedControlAndValid() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("\u0000data\u0000"); // control chars around valid name
        tag.newAttribute();
        // After trim, should become "data" (since \u0000 is trimmed)
        assertTrue(tag.getAttributes().hasKey("data"));
    }

    @Test(timeout = 4000)
    public void testAttributeNameWithOnlyControlCharacters() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("\u0001\u0002\u0003");
        try {
            tag.newAttribute();
        } catch (IllegalArgumentException e) {
            fail("Only control characters in attribute name should not cause IllegalArgumentException");
        }
    }

    @Test(timeout = 4000)
    public void testFinaliseTagWithControlAttributeName() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("\u0000");
        try {
            tag.finaliseTag();
        } catch (IllegalArgumentException e) {
            fail("finaliseTag with control attribute name should not throw");
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNameWithNullTagName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name(); // tagName is null
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNameWithEmptyTagName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name(""); // This sets tagName to "" and normalName to ""; then name() will throw
        tag.name(); // throws because length == 0
    }

    @Test(timeout = 4000)
    public void testAppendTagNameFromEmpty() {
        Token.StartTag tag = new Token.StartTag();
        tag.name(""); // sets tagName to ""
        // appending to empty string
        tag.appendTagName("x");
        assertEquals("x", tag.name());
    }

    @Test(timeout = 4000)
    public void testAppendAttributeNameFromEmpty() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName(""); // pendingAttributeName becomes ""
        tag.newAttribute(); // should create attribute with empty name? Actually trim() makes it empty, then BooleanAttribute("") throws.
        // We expect no exception (defect test already covers), but here we just verify it doesn't throw.
        // This is a boundary case.
        try {
            tag.newAttribute();
        } catch (IllegalArgumentException e) {
            fail("Empty attribute name should not cause exception");
        }
    }

    @Test(timeout = 4000)
    public void testNullPubSysKey() {
        Token.Doctype doctype = new Token.Doctype();
        assertNull(doctype.getPubSysKey());
        doctype.pubSysKey = "SYSTEM";
        assertEquals("SYSTEM", doctype.getPubSysKey());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testTokenTypeMethod() {
        assertEquals("Doctype", new Token.Doctype().tokenType());
        assertEquals("StartTag", new Token.StartTag().tokenType());
        assertEquals("EndTag", new Token.EndTag().tokenType());
        assertEquals("Comment", new Token.Comment().tokenType());
        assertEquals("Character", new Token.Character().tokenType());
        assertEquals("EOF", new Token.EOF().tokenType());
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

        Token.Character character = new Token.Character();
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
    public void testAsTypeMethods() {
        Token.Doctype doctype = new Token.Doctype();
        assertSame(doctype, doctype.asDoctype());

        Token.StartTag startTag = new Token.StartTag();
        assertSame(startTag, startTag.asStartTag());

        Token.EndTag endTag = new Token.EndTag();
        assertSame(endTag, endTag.asEndTag());

        Token.Comment comment = new Token.Comment();
        assertSame(comment, comment.asComment());

        Token.Character character = new Token.Character();
        assertSame(character, character.asCharacter());
    }

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
    public void testStartTagResetClearsAttributes() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");
        tag.getAttributes().put("class", "test");
        tag.reset();
        assertEquals(0, tag.getAttributes().size());
        try {
            tag.name(); // should throw because tagName is null
            fail("Expected IllegalArgumentException after reset");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEndTagReset() {
        Token.EndTag tag = new Token.EndTag();
        tag.name("span");
        tag.reset();
        try {
            tag.name();
            fail("Expected IllegalArgumentException after reset");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testStaticResetStringBuilder() {
        StringBuilder sb = new StringBuilder("hello");
        Token.reset(sb);
        assertEquals(0, sb.length());

        // null should not throw
        Token.reset(null);
    }

    @Test(timeout = 4000)
    public void testDoctypeToString() {
        // Doctype does not override toString, but we can test getters
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        assertEquals("html", doctype.getName());
    }

    @Test(timeout = 4000)
    public void testCharacterDataMethod() {
        Token.Character character = new Token.Character();
        character.data("text");
        assertEquals("text", character.getData());
        // data() returns this for chaining
        assertSame(character, character.data("new"));
        assertEquals("new", character.getData());
    }

    @Test(timeout = 4000)
    public void testCommentData() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("data");
        assertEquals("data", comment.getData());
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendAfterEmptyValueS() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("class");
        tag.appendAttributeValue("first"); // sets pendingAttributeValueS
        tag.appendAttributeValue(" second"); // triggers ensureAttributeValue, moves to builder
        tag.newAttribute();
        assertEquals("first second", tag.getAttributes().get("class"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendCharAfterString() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("name");
        tag.appendAttributeValue("init");
        tag.appendAttributeValue('!');
        tag.newAttribute();
        assertEquals("init!", tag.getAttributes().get("name"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendCharArrayAfterString() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("data");
        tag.appendAttributeValue("base");
        tag.appendAttributeValue(new char[]{'-', 's', 'uffix'});
        tag.newAttribute();
        assertEquals("base-suffix", tag.getAttributes().get("data"));
    }

    @Test(timeout = 4000)
    public void testTagAttributeValueAppendCodepointsAfterString() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("emoji");
        tag.appendAttributeValue("smile");
        tag.appendAttributeValue(new int[]{0x1F600});
        tag.newAttribute();
        assertEquals("smile\uD83D\uDE00", tag.getAttributes().get("emoji"));
    }

    @Test(timeout = 4000)
    public void testMultipleAttributesWithMixedValues() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("id");
        tag.appendAttributeValue("main");
        tag.newAttribute();

        tag.appendAttributeName("class");
        tag.setEmptyAttributeValue();
        tag.newAttribute();

        tag.appendAttributeName("style");
        tag.appendAttributeValue("color:red");
        tag.newAttribute();

        assertEquals(3, tag.getAttributes().size());
        assertEquals("main", tag.getAttributes().get("id"));
        assertEquals("", tag.getAttributes().get("class"));
        assertEquals("color:red", tag.getAttributes().get("style"));
    }

    @Test(timeout = 4000)
    public void testAttributeNameTrimmedToEmptyWithHasEmptyValue() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("  ");
        tag.setEmptyAttributeValue();
        try {
            tag.newAttribute();
        } catch (IllegalArgumentException e) {
            fail("Whitespace attribute name with empty value should not throw");
        }
    }

    @Test(timeout = 4000)
    public void testAttributeNameTrimmedToEmptyWithPendingValue() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("\u0000");
        tag.appendAttributeValue("value");
        try {
            tag.newAttribute();
        } catch (IllegalArgumentException e) {
            fail("Control attribute name with value should not throw");
        }
    }
}