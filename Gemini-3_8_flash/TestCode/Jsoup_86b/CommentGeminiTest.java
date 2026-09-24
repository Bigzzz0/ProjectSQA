package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Comment
 *
 * Decision / Condition Coverage Matrix:
 * 1. isXmlDeclaration():
 *    - data.length() <= 1 (e.g., "", "!", "?", "a") -> false [False branch of length > 1]
 *    - data.length() > 1 && data.startsWith("!") -> true [True branch 1]
 *    - data.length() > 1 && data.startsWith("?") -> true [True branch 2]
 *    - data.length() > 1 && !data.startsWith("!") && !data.startsWith("?") -> false [False branch of prefix check]
 *
 * 2. asXmlDeclaration():
 *    - Valid processing instruction: data starting with '?' and ending with '?' (e.g. "?xml version=\"1.0\"?")
 *      -> doc.childNodeSize() > 0, child element present, data.startsWith("!") == false.
 *    - Valid declaration: data starting with '!' (e.g. "!DOCTYPE html>")
 *      -> doc.childNodeSize() > 0, child element present, data.startsWith("!") == true.
 *    - DEFECT ZONE: handlesLTinScript / malformed bogus comment (e.g. "!?", "! ", "??", "? ", "!--"):
 *      -> Parsed XML doc has doc.childNodeSize() > 0 (comment or text node), but doc.children().size() == 0 (no Element).
 *      -> Defective version: doc.child(0) throws java.lang.IndexOutOfBoundsException: Index: 0, Size: 0.
 *      -> Expected correct behavior: returns null gracefully.
 *
 * 3. outerHtmlHead() / outerHtmlTail():
 *    - out.prettyPrint() == true -> invokes indent()
 *    - out.prettyPrint() == false -> skips indent()
 *    - outerHtmlTail() -> no-op branch
 *
 * 4. Constructors & Node Contracts:
 *    - Comment(String data)
 *    - Comment(String data, String baseUri) [deprecated constructor]
 *    - nodeName() returns "#comment"
 *    - toString() delegates to outerHtml()
 *    - clone() creates an independent Comment copy
 */
public class CommentGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardCommentCreationAndGetters() {
        String content = "This is a standard HTML comment.";
        Comment comment = new Comment(content);

        assertEquals("#comment", comment.nodeName());
        assertEquals(content, comment.getData());
        assertEquals("<!--" + content + "-->", comment.outerHtml());
        assertEquals("<!--" + content + "-->", comment.toString());
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructorWithBaseUri() {
        String content = "BaseUri test comment";
        Comment comment = new Comment(content, "https://example.com/base");

        assertEquals("#comment", comment.nodeName());
        assertEquals(content, comment.getData());
        assertEquals("<!--" + content + "-->", comment.outerHtml());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithPrettyPrintDisabled() throws IOException {
        Comment comment = new Comment("no-pretty-print");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);

        comment.outerHtmlHead(accum, 2, settings);
        comment.outerHtmlTail(accum, 2, settings);

        assertEquals("<!--no-pretty-print-->", accum.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithPrettyPrintEnabled() throws IOException {
        Comment comment = new Comment("pretty-print");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(true).indentAmount(4);

        comment.outerHtmlHead(accum, 1, settings);
        comment.outerHtmlTail(accum, 1, settings);

        assertEquals("\n    <!--pretty-print-->", accum.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlTailDoesNotModifyAccumulator() throws IOException {
        Comment comment = new Comment("tail-test");
        StringBuilder accum = new StringBuilder("pre-existing");
        Document.OutputSettings settings = new Document.OutputSettings();

        comment.outerHtmlTail(accum, 0, settings);
        assertEquals("pre-existing", accum.toString());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationValidXmlProcessingInstruction() {
        Comment comment = new Comment("?xml version=\"1.0\" encoding=\"UTF-8\"?");
        assertTrue(comment.isXmlDeclaration());

        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertFalse(decl.isProcessingInstruction());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", decl.outerHtml());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationValidDoctypeDeclaration() {
        Comment comment = new Comment("!DOCTYPE html>");
        assertTrue(comment.isXmlDeclaration());

        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("DOCTYPE", decl.name());
        assertTrue(decl.hasAttr("html"));
        assertTrue(decl.isProcessingInstruction());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationSingleTagNameNoAttributes() {
        Comment comment = new Comment("?custom-tag?");
        assertTrue(comment.isXmlDeclaration());

        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("custom-tag", decl.name());
        assertFalse(decl.isProcessingInstruction());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsXmlDeclarationEmptyString() {
        Comment comment = new Comment("");
        assertFalse(comment.isXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationSingleCharBoundaries() {
        assertFalse(new Comment("!").isXmlDeclaration());
        assertFalse(new Comment("?").isXmlDeclaration());
        assertFalse(new Comment("a").isXmlDeclaration());
        assertFalse(new Comment("<").isXmlDeclaration());
        assertFalse(new Comment(" ").isXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationLengthGreaterThanOneWithoutXmlPrefix() {
        assertFalse(new Comment("normal comment").isXmlDeclaration());
        assertFalse(new Comment("xml version=\"1.0\"").isXmlDeclaration());
        assertFalse(new Comment(" -- comment").isXmlDeclaration());
        assertFalse(new Comment("<xml>").isXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationLengthGreaterThanOneWithXmlPrefix() {
        assertTrue(new Comment("! ").isXmlDeclaration());
        assertTrue(new Comment("? ").isXmlDeclaration());
        assertTrue(new Comment("!a").isXmlDeclaration());
        assertTrue(new Comment("?a").isXmlDeclaration());
        assertTrue(new Comment("!?").isXmlDeclaration());
        assertTrue(new Comment("?!").isXmlDeclaration());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J handlesLTinScript)
    // =========================================================================

    /**
     * Targets Defects4J Ground Truth:
     * org.jsoup.parser.XmlTreeBuilderTest::handlesLTinScript
     * -> java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
     *
     * Cause:
     * When parsed XML produces child nodes but no child elements (e.g. comment node from "<>"),
     * doc.childNodeSize() > 0 evaluates to true, but doc.child(0) calls doc.children().get(0)
     * which fails with IndexOutOfBoundsException.
     * Expected behavior: asXmlDeclaration() must return null without throwing an exception.
     */
    @Test(timeout = 4000)
    public void testAsXmlDeclarationWithNonElementNodeDefectLTinScript() {
        Comment comment = new Comment("!?");
        assertTrue(comment.isXmlDeclaration());

        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull("Malformed declaration producing non-element child must return null", decl);
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationWithEmptyTagNameDefect() {
        Comment comment = new Comment("! ");
        assertTrue(comment.isXmlDeclaration());

        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull("Whitespace payload producing empty tag must return null", decl);
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationWithDoubleQuestionMarkDefect() {
        Comment comment = new Comment("??");
        assertTrue(comment.isXmlDeclaration());

        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull("Double question mark comment must return null without exception", decl);
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationWithInvalidXmlTagDefect() {
        Comment comment = new Comment("!--");
        assertTrue(comment.isXmlDeclaration());

        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNull("Malformed XML tag '<->' must return null without exception", decl);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyCommentOuterHtml() {
        Comment emptyComment = new Comment("");
        assertEquals("", emptyComment.getData());
        assertEquals("<!---->", emptyComment.outerHtml());
    }

    @Test(timeout = 4000)
    public void testCommentContainingSpecialCharacters() {
        String special = "<script>alert('xss');</script>&amp;\"'";
        Comment comment = new Comment(special);
        assertEquals(special, comment.getData());
        assertEquals("<!--" + special + "-->", comment.outerHtml());
    }

    // =========================================================================
    // Partition E: Object Lifecycle, DOM Integration & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        Comment original = new Comment("clone-content");
        Comment clone = (Comment) original.clone();

        assertNotSame(original, clone);
        assertEquals(original.getData(), clone.getData());
        assertEquals(original.nodeName(), clone.nodeName());
        assertEquals(original.outerHtml(), clone.outerHtml());

        clone.attr(clone.nodeName(), "mutated-clone");
        assertEquals("clone-content", original.getData());
        assertEquals("mutated-clone", clone.getData());
    }

    @Test(timeout = 4000)
    public void testCommentInDocumentTree() {
        Document doc = Jsoup.parse("<div><!--initial comment--></div>");
        Element div = doc.selectFirst("div");
        assertNotNull(div);

        assertEquals(1, div.childNodeSize());
        assertTrue(div.childNode(0) instanceof Comment);

        Comment parsedComment = (Comment) div.childNode(0);
        assertEquals("initial comment", parsedComment.getData());
        assertSame(div, parsedComment.parent());

        Comment newComment = new Comment("appended comment");
        div.appendChild(newComment);
        assertEquals(2, div.childNodeSize());
        assertSame(div, newComment.parent());
    }
}