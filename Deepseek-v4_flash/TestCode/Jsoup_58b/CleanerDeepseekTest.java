package org.jsoup.safety;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.DataNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * Advanced White-Box test suite for Cleaner.java targeting known Defects4J defects.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core functional logic
 *   - clean() with valid/invalid body content
 *   - isValid() with valid/invalid body content
 *   - copySafeNodes() traversal and visitor logic
 *   - createSafeElement() attribute filtering and enforced attributes
 * 
 * Partition B: Boundary Value Analysis
 *   - null document argument (clean, isValid)
 *   - null whitelist argument (constructor)
 *   - empty body document
 *   - frameset document (body() == null)
 *   - document with only head content
 *   - document with only text nodes
 *   - document with data nodes (script/style)
 *   - whitelist with no tags allowed
 *   - whitelist with all tags allowed
 *   - edge cases: tag names with uppercase, special characters
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - isValid() on document with null body (frameset) – known NPE defect
 *   - isValid() on document where body tag itself is not in whitelist – should return false
 *   - isValid() on document with only head content – should return false
 *   - clean() on frameset document – should not throw NPE
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Constructor with null whitelist -> NullPointerException
 *   - clean(null) -> NullPointerException
 *   - isValid(null) -> NullPointerException
 *   - head() with non-Element, non-Text, non-DataNode (e.g., Comment) -> numDiscarded++
 *   - tail() with unsafe tag -> destination unchanged
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Not applicable (no equals/hashCode/clone)
 */
public class CleanerDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testCleanWithSafeTags() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<p>Hello <b>world</b></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body><p>Hello <b>world</b></p></body></html>",
                clean.html().replaceAll("\\s+", " ").trim());
    }

    @Test(timeout = 4000)
    public void testCleanWithUnsafeTags() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<p>Hello <script>alert(1)</script></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // Only text nodes should survive
        assertTrue(clean.body().html().contains("Hello"));
        assertFalse(clean.body().html().contains("<script>"));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnsTrueForValidDocument() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<b>bold</b><i>italic</i>";
        Document dirty = Jsoup.parse(html);
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnsFalseForInvalidDocument() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<p>paragraph</p>"; // <p> not in simpleText
        Document dirty = Jsoup.parse(html);
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanPreservesEnforcedAttributes() {
        Whitelist whitelist = Whitelist.relaxed();
        whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<a href='http://example.com'>link</a>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertTrue(clean.body().html().contains("rel=\"nofollow\""));
    }

    @Test(timeout = 4000)
    public void testCleanDiscardsUnsafeAttributes() {
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<b onclick='alert(1)'>bold</b>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertFalse(clean.body().html().contains("onclick"));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullWhitelist() {
        new Cleaner(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testCleanNullDocument() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.clean(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testIsValidNullDocument() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.isValid(null);
    }

    @Test(timeout = 4000)
    public void testCleanWithEmptyBody() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.createShell("");
        // body is empty
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body></body></html>",
                clean.html().replaceAll("\\s+", " ").trim());
    }

    @Test(timeout = 4000)
    public void testCleanWithFramesetDocument() {
        // Frameset documents have no body element
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<html><frameset><frame src='page.html'></frameset></html>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // Should not throw NPE; clean document should have empty body
        assertNotNull(clean.body());
        assertEquals("", clean.body().html().trim());
    }

    @Test(timeout = 4000)
    public void testIsValidWithFramesetDocument() {
        // Defect-targeted: isValid should handle null body gracefully
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<html><frameset><frame src='page.html'></frameset></html>";
        Document dirty = Jsoup.parse(html);
        // On buggy version, this throws NullPointerException; we expect false (no body content)
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidWithOnlyHeadContent() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<html><head><title>test</title></head><body></body></html>";
        Document dirty = Jsoup.parse(html);
        // Head content is not in body, so should be valid (no discarded nodes)
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanWithDataNodes() {
        Whitelist whitelist = Whitelist.relaxed();
        whitelist.addTags("script");
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<script>alert(1)</script>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertTrue(clean.body().html().contains("alert(1)"));
    }

    @Test(timeout = 4000)
    public void testCleanWithUnsafeDataNodeParent() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<script>alert(1)</script>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // DataNode should be discarded because parent (script) is not safe
        assertFalse(clean.body().html().contains("alert(1)"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testIsValidBodyTagNotInWhitelist() {
        // If the body tag itself is not allowed, isValid should return false
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<body><p>text</p></body>";
        Document dirty = Jsoup.parse(html);
        // The body element is the root of traversal; it is not safe, but because source == root,
        // numDiscarded is not incremented. This is a known defect: isValid returns true incorrectly.
        // On buggy version, this returns true; we assert false to reveal the bug.
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidDocumentWithUnsafeBodyTag() {
        // Similar to above but using a full document
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<html><body><p>text</p></body></html>";
        Document dirty = Jsoup.parse(html);
        // The body element is not safe, but root check prevents discarding. Should be invalid.
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidBodyHtmlWithUnsafeTags() {
        // Directly targets the failing test testIsValidBodyHtml
        Whitelist whitelist = Whitelist.simpleText();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<p>paragraph</p>"; // <p> not allowed
        Document dirty = Jsoup.parse(html);
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidDocumentWithMixedContent() {
        // Targets testIsValidDocument
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<html><head><title>test</title></head><body><p>ok</p><script>bad</script></body></html>";
        Document dirty = Jsoup.parse(html);
        // script is not in relaxed whitelist, so should be invalid
        assertFalse(cleaner.isValid(dirty));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testHeadWithCommentNode() {
        // Comment nodes are not Element, TextNode, or DataNode -> numDiscarded++
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<p>text<!-- comment --></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // Comment should be removed
        assertFalse(clean.body().html().contains("<!--"));
    }

    @Test(timeout = 4000)
    public void testTailWithUnsafeTag() {
        // When tail is called for an unsafe element, destination should not pop
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<div><p>text</p></div>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // Both div and p are unsafe, so only text should remain
        assertTrue(clean.body().html().contains("text"));
        assertFalse(clean.body().html().contains("<div>"));
        assertFalse(clean.body().html().contains("<p>"));
    }

    @Test(timeout = 4000)
    public void testCreateSafeElementWithNoAttributes() {
        // Directly test the private method via reflection? Not possible. Instead test via clean.
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<br>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertTrue(clean.body().html().contains("<br>"));
    }

    @Test(timeout = 4000)
    public void testCreateSafeElementWithUnsafeAttributes() {
        Whitelist whitelist = Whitelist.relaxed();
        // Remove all attributes from a tag
        whitelist.addAttributes("a", "href"); // only allow href
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<a href='ok' onclick='bad' style='color:red'>link</a>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertTrue(clean.body().html().contains("href=\"ok\""));
        assertFalse(clean.body().html().contains("onclick"));
        assertFalse(clean.body().html().contains("style"));
    }

    // ========== Additional coverage for inner classes ==========

    @Test(timeout = 4000)
    public void testCopySafeNodesWithNullSource() {
        // This path is not normally reachable, but we can test via isValid with null body
        // Already covered in testIsValidWithFramesetDocument
    }

    @Test(timeout = 4000)
    public void testCleaningVisitorNumDiscardedIncrementedForUnsafeElements() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<p>text</p><div>more</div>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // Only text nodes survive; both p and div are discarded
        assertTrue(clean.body().html().contains("text"));
        assertTrue(clean.body().html().contains("more"));
        // No tags
        assertEquals("textmore", clean.body().text().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testIsValidWithOnlyTextNode() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "just text";
        Document dirty = Jsoup.parse(html);
        // Text nodes are always allowed, so should be valid
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidWithUnsafeDataNode() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        String html = "<script>alert(1)</script>";
        Document dirty = Jsoup.parse(html);
        // script tag is unsafe, so data node inside it is also discarded? Actually, data node is only copied if parent is safe.
        // Since parent is unsafe, data node is not copied, but numDiscarded is incremented for the script element.
        // So isValid should return false.
        assertFalse(cleaner.isValid(dirty));
    }
}