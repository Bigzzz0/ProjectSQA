package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box JUnit 4 test suite for Comment.java targeting the known Defects4J defect:
 * IndexOutOfBoundsException in asXmlDeclaration() when the parsed XML document has no children.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (nodeName, getData, outerHtmlHead, outerHtmlTail, toString)
 * - Partition B: Boundary Value Analysis (null data, empty data, single char, two char, long data)
 * - Partition C: Defect-targeted branch (asXmlDeclaration with data causing empty parsed doc)
 * - Partition D: Exception & defensive paths (null data, invalid XML declaration)
 * - Partition E: Object lifecycle (equals, hashCode, clone not applicable; contract via toString)
 * 
 * Known defect: asXmlDeclaration() throws IndexOutOfBoundsException when data length is 2 and
 * starts with '!' or '?' (e.g., "!?"), because substring(1,1) yields "" and parsing "<>" produces
 * a document with no children, but doc.childNodeSize() > 0 returns true erroneously.
 */
public class CommentDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testNodeName() {
        Comment comment = new Comment("test data");
        assertEquals("#comment", comment.nodeName());
    }

    @Test(timeout = 4000)
    public void testGetData() {
        Comment comment = new Comment("hello world");
        assertEquals("hello world", comment.getData());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadPrettyPrint() throws Exception {
        Comment comment = new Comment("comment");
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(true);
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 0, settings);
        assertEquals("<!--comment-->", accum.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadNoPrettyPrint() throws Exception {
        Comment comment = new Comment("comment");
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 0, settings);
        assertEquals("<!--comment-->", accum.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlTail() throws Exception {
        Comment comment = new Comment("data");
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlTail(accum, 0, new Document.OutputSettings());
        assertEquals("", accum.toString());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Comment comment = new Comment("test");
        assertEquals("<!--test-->", comment.toString());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testGetDataEmpty() {
        Comment comment = new Comment("");
        assertEquals("", comment.getData());
    }

    @Test(timeout = 4000)
    public void testGetDataSingleChar() {
        Comment comment = new Comment("a");
        assertEquals("a", comment.getData());
    }

    @Test(timeout = 4000)
    public void testGetDataLongString() {
        String longData = "x".repeat(1000);
        Comment comment = new Comment(longData);
        assertEquals(longData, comment.getData());
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationNullData() {
        // Note: null data is allowed by constructor but will cause NPE in isXmlDeclaration
        Comment comment = new Comment((String) null);
        try {
            comment.isXmlDeclaration();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationEmpty() {
        Comment comment = new Comment("");
        assertFalse(comment.isXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationSingleChar() {
        Comment comment = new Comment("!");
        assertFalse(comment.isXmlDeclaration()); // length > 1 is false
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationStartsWithExclamation() {
        Comment comment = new Comment("!DOCTYPE");
        assertTrue(comment.isXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationStartsWithQuestion() {
        Comment comment = new Comment("?xml version='1.0'?");
        assertTrue(comment.isXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testIsXmlDeclarationNotDeclaration() {
        Comment comment = new Comment("regular comment");
        assertFalse(comment.isXmlDeclaration());
    }

    // ==================== Partition C: Defect-Targeted Branch ====================

    /**
     * Directly targets the known defect: asXmlDeclaration() throws IndexOutOfBoundsException
     * when data length is 2 and starts with '!' or '?' (e.g., "!?").
     * On the fixed version, this should return null without exception.
     */
    @Test(timeout = 4000)
    public void testAsXmlDeclarationWithShortData() {
        Comment comment = new Comment("!?");
        // This should not throw; on defective version it throws IndexOutOfBoundsException
        assertNull(comment.asXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationWithQuestionMarkOnly() {
        Comment comment = new Comment("?");
        // isXmlDeclaration returns false, so asXmlDeclaration should return null
        assertNull(comment.asXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationWithExclamationOnly() {
        Comment comment = new Comment("!");
        assertNull(comment.asXmlDeclaration());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationValidDeclaration() {
        Comment comment = new Comment("?xml version='1.0'?");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("xml", decl.tagName());
        assertFalse(decl.isProcessingInstruction()); // starts with '?' -> processing instruction
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationValidDoctype() {
        Comment comment = new Comment("!DOCTYPE html");
        XmlDeclaration decl = comment.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("DOCTYPE", decl.tagName());
        assertTrue(decl.isProcessingInstruction()); // starts with '!' -> not processing instruction? Actually isProcessingInstruction returns true if starts with '!'
        // According to XmlDeclaration constructor: isProcessingInstruction = data.startsWith("!")
        // So for "!DOCTYPE", isProcessingInstruction should be true.
        assertTrue(decl.isProcessingInstruction());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationInvalidXml() {
        // Data that is not a valid XML declaration but passes isXmlDeclaration
        Comment comment = new Comment("!invalid<");
        // Should return null because parsed document has no children
        assertNull(comment.asXmlDeclaration());
    }

    // ==================== Partition D: Exception & Defensive Paths ====================

    @Test(timeout = 4000)
    public void testConstructorWithBaseUri() {
        Comment comment = new Comment("data", "http://example.com");
        assertEquals("data", comment.getData());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithDepth() throws Exception {
        Comment comment = new Comment("test");
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(true);
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 2, settings);
        // Indentation: 2 levels -> 4 spaces (assuming indent amount = 2)
        assertEquals("    <!--test-->", accum.toString());
    }

    @Test(timeout = 4000)
    public void testAsXmlDeclarationNullData() {
        Comment comment = new Comment((String) null);
        try {
            comment.asXmlDeclaration();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testToStringMatchesOuterHtml() {
        Comment comment = new Comment("data");
        assertEquals(comment.outerHtml(), comment.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleComments() {
        Comment c1 = new Comment("a");
        Comment c2 = new Comment("b");
        assertNotEquals(c1.getData(), c2.getData());
    }
}