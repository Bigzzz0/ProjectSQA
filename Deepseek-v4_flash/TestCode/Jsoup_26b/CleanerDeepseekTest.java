package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * White-box test suite for Cleaner.java targeting:
 * - Core functional logic (clean, isValid)
 * - Boundary conditions (null arguments, empty documents, frameset documents)
 * - Defect-triggering scenario: NullPointerException when cleaning a frameset document
 * - Exception paths (null whitelist, null document)
 * - State transitions and recursion in copySafeNodes
 */
public class CleanerDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Decision branches in copySafeNodes:
     * - sourceChild instanceof Element (true/false)
     * - whitelist.isSafeTag(sourceEl.tagName()) (true/false)
     * - sourceChild instanceof TextNode (true/false)
     * - else (comments, etc.) – ignored
     * 
     * Decision branches in createSafeElement:
     * - whitelist.isSafeAttribute(sourceTag, sourceEl, sourceAttr) (true/false)
     * - enforced attributes added unconditionally
     * 
     * Boundary conditions:
     * - null whitelist in constructor → IllegalArgumentException
     * - null document in clean/isValid → IllegalArgumentException
     * - empty body (no children) → numDiscarded = 0
     * - frameset document (body() returns null) → NullPointerException (defect)
     * - unsafe tag with safe children → recursion discards parent but copies children
     * - safe tag with unsafe attributes → attributes discarded
     * - text nodes copied verbatim
     * - comments ignored
     * 
     * Defect targeted: handlesFramesets – NullPointerException when body is null.
     */

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void cleanSimpleSafeTags() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p>Hello</p><b>Bold</b>");
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body><p>Hello</p><b>Bold</b></body></html>",
                clean.html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void cleanUnsafeTagsRemoved() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<script>alert('xss')</script><p>safe</p>");
        Document clean = cleaner.clean(dirty);
        // Only text nodes survive (script is unsafe, p is unsafe too with none)
        assertEquals("<html><head></head><body>alert('xss')safe</body></html>",
                clean.html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void cleanWithSafeAttributes() {
        Whitelist whitelist = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href='http://example.com' title='test'>link</a>");
        Document clean = cleaner.clean(dirty);
        assertTrue(clean.html().contains("href"));
        assertTrue(clean.html().contains("title"));
    }

    @Test(timeout = 4000)
    public void cleanWithUnsafeAttributes() {
        Whitelist whitelist = Whitelist.basic(); // basic does not allow style
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p style='color:red'>text</p>");
        Document clean = cleaner.clean(dirty);
        assertFalse(clean.html().contains("style"));
    }

    @Test(timeout = 4000)
    public void cleanWithEnforcedAttributes() {
        Whitelist whitelist = Whitelist.basic();
        whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href='http://example.com'>link</a>");
        Document clean = cleaner.clean(dirty);
        assertTrue(clean.html().contains("rel=\"nofollow\""));
    }

    @Test(timeout = 4000)
    public void cleanNestedSafeUnsafe() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<div><p>safe</p><script>bad</script></div>");
        Document clean = cleaner.clean(dirty);
        // div is not in basic, so it's discarded, but p and text inside are copied
        String html = clean.html().replaceAll("\\s+", "");
        assertTrue(html.contains("<p>safe</p>"));
        assertTrue(html.contains("bad")); // text from script survives
        assertFalse(html.contains("<div>"));
    }

    @Test(timeout = 4000)
    public void cleanTextNodesOnly() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("Just text");
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body>Just text</body></html>",
                clean.html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void cleanCommentsIgnored() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p>text<!-- comment --></p>");
        Document clean = cleaner.clean(dirty);
        assertFalse(clean.html().contains("<!--"));
    }

    @Test(timeout = 4000)
    public void isValidReturnsTrueForCleanDoc() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p>Hello</p>");
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void isValidReturnsFalseForUnsafeTag() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p>Hello</p>");
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void isValidReturnsFalseForUnsafeAttribute() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p style='color:red'>text</p>");
        assertFalse(cleaner.isValid(dirty));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void cleanEmptyDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.createShell("http://example.com");
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body></body></html>",
                clean.html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void isValidEmptyDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.createShell("http://example.com");
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void cleanWithNullBaseUri() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p>text</p>", "");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known defect: NullPointerException when cleaning a frameset document.
     * In such documents, body() returns null, causing NPE in copySafeNodes.
     * This test should pass on fixed version and fail on defective version.
     */
    @Test(timeout = 4000)
    public void handlesFramesets() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        // Parse a frameset document (no body element)
        Document dirty = Jsoup.parse("<frameset><frame src='page.html'></frameset>");
        // The bug: body() returns null, leading to NPE
        try {
            Document clean = cleaner.clean(dirty);
            // If we reach here, bug is fixed; verify clean document has no frameset
            assertNotNull(clean);
            assertFalse(clean.html().contains("frameset"));
        } catch (NullPointerException e) {
            fail("NullPointerException thrown when cleaning frameset document: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void isValidFramesetDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<frameset><frame src='page.html'></frameset>");
        try {
            boolean valid = cleaner.isValid(dirty);
            // Should be false because frameset is not allowed
            assertFalse(valid);
        } catch (NullPointerException e) {
            fail("NullPointerException thrown when validating frameset document: " + e.getMessage());
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void constructorNullWhitelist() {
        new Cleaner(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void cleanNullDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void isValidNullDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.isValid(null);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void cleanMultipleCallsSameCleaner() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc1 = Jsoup.parse("<p>first</p>");
        Document doc2 = Jsoup.parse("<b>second</b>");
        Document clean1 = cleaner.clean(doc1);
        Document clean2 = cleaner.clean(doc2);
        assertTrue(clean1.html().contains("first"));
        assertTrue(clean2.html().contains("second"));
    }

    @Test(timeout = 4000)
    public void cleanDoesNotModifyOriginal() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<script>alert('xss')</script><p>safe</p>");
        String originalHtml = dirty.html();
        cleaner.clean(dirty);
        assertEquals(originalHtml, dirty.html());
    }

    @Test(timeout = 4000)
    public void isValidDoesNotModifyOriginal() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<script>alert('xss')</script><p>safe</p>");
        String originalHtml = dirty.html();
        cleaner.isValid(dirty);
        assertEquals(originalHtml, dirty.html());
    }
}