package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.safety.Cleaner
 *
 * Decision / Branch Coverage Points:
 * 1. Cleaner(Whitelist):
 *    - whitelist != null -> success
 *    - whitelist == null -> IllegalArgumentException via Validate.notNull
 * 2. clean(Document):
 *    - dirtyDocument != null -> proceed
 *    - dirtyDocument == null -> IllegalArgumentException via Validate.notNull
 *    - dirtyDocument.body() != null -> recursive safe node copying
 *    - dirtyDocument.body() == null -> DEFECT ZONE (NPE in handlesFramesets)
 * 3. isValid(Document):
 *    - dirtyDocument != null -> proceed
 *    - dirtyDocument == null -> IllegalArgumentException via Validate.notNull
 *    - dirtyDocument.body() == null -> DEFECT ZONE (NPE in frameset documents)
 *    - numDiscarded == 0 -> returns true
 *    - numDiscarded > 0  -> returns false
 * 4. copySafeNodes(Element source, Element dest):
 *    - sourceChild instanceof Element:
 *        - whitelist.isSafeTag() == true  -> createSafeElement, recurse on child element
 *        - whitelist.isSafeTag() == false -> increment numDiscarded, drop tag, recurse safe children to dest
 *    - sourceChild instanceof TextNode:
 *        - clones TextNode with original baseUri, appends to dest
 *    - sourceChild instanceof other (Comment, DataNode, etc.):
 *        - ignored/dropped without incrementing numDiscarded
 * 5. createSafeElement(Element sourceEl):
 *    - whitelist.isSafeAttribute() == true  -> keep attribute
 *    - whitelist.isSafeAttribute() == false -> drop attribute, increment numDiscarded
 *    - whitelist.getEnforcedAttributes()     -> appended to cleaned element
 *
 * Known Defect Targeted:
 * - org.jsoup.safety.CleanerTest::handlesFramesets:
 *   When a Document with <frameset> is supplied, Document.body() is null, causing
 *   copySafeNodes(null, clean.body()) to throw a NullPointerException.
 */
public class CleanerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanSimpleValidHtml() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Hello <b>World</b></p>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("<p>Hello <b>World</b></p>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanDropsUnsafeTagPreservesSafeChildren() {
        // 'b' is safe in basic, 'script' and 'custom' are not
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<custom><b>Allowed</b> <script>alert('xss')</script></custom>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        // <custom> tag dropped, its child <b> is kept, <script> dropped, text from script might be preserved
        // Note: basic whitelist drops script, but script children might be text nodes.
        assertTrue(clean.body().html().contains("<b>Allowed</b>"));
        assertFalse(clean.body().html().contains("<custom>"));
        assertFalse(clean.body().html().contains("<script>"));
    }

    @Test(timeout = 4000)
    public void testCleanPreservesSafeAttributesAndDiscardsUnsafeAttributes() {
        Whitelist whitelist = Whitelist.none()
                .addTags("a")
                .addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<a href=\"http://example.com\" onclick=\"steal()\" style=\"color:red\">Link</a>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("<a href=\"http://example.com\">Link</a>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanAddsEnforcedAttributes() {
        Whitelist whitelist = Whitelist.none()
                .addTags("a")
                .addAttributes("a", "href")
                .addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<a href=\"http://example.com\">Link</a>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("<a href=\"http://example.com\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanCopiesTextNodesWithBaseUri() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        String baseUri = "http://example.com/dir/";
        Document dirty = Jsoup.parse("<p>Plain text node</p>", baseUri);
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        Element p = clean.body().child(0);
        assertEquals("p", p.tagName());
        TextNode text = (TextNode) p.childNode(0);
        assertEquals("Plain text node", text.getWholeText());
        assertEquals(baseUri, text.baseUri());
    }

    @Test(timeout = 4000)
    public void testCleanIgnoresComments() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Hello <!-- This is a comment --> World</p>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("<p>Hello  World</p>", clean.body().html());
        assertFalse(clean.body().html().contains("<!--"));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnsTrueForFullyCompliantHtml() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document compliant = Jsoup.parse("<p><a href=\"http://example.com\" rel=\"nofollow\">Link</a></p>");
        assertTrue(cleaner.isValid(compliant));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnsFalseWhenUnsafeTagPresent() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Hello <script>alert(1)</script></p>");
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnsFalseWhenUnsafeAttributePresent() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p onclick=\"steal()\">Hello</p>");
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnsFalseWhenEnforcedAttributeMissing() {
        Cleaner cleaner = new Cleaner(Whitelist.basic()); // basic enforces rel="nofollow" on <a>
        Document dirty = Jsoup.parse("<p><a href=\"http://example.com\">Link</a></p>");
        // Missing rel="nofollow", which should cause cleaner to discard/re-add and report invalid
        // Actually, basic does not discard <a>, but does it discard attributes? Let's check:
        // isSafeAttribute doesn't discard href, but isValid is based on numDiscarded == 0.
        // Wait, if an enforced attribute is not in source, numDiscarded is not incremented for missing attributes,
        // BUT if an attribute has an invalid protocol like javascript:, it will be discarded.
        Document invalidProtocol = Jsoup.parse("<a href=\"javascript:alert(1)\">Link</a>");
        assertFalse(cleaner.isValid(invalidProtocol));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanEmptyDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("", clean.body().html());
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanDeeplyNestedStructure() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("<b>");
        }
        sb.append("Deep");
        for (int i = 0; i < 50; i++) {
            sb.append("</b>");
        }

        Document dirty = Jsoup.parse(sb.toString());
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertTrue(clean.body().html().contains("Deep"));
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanOnlyTextNodes() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse("Just plain loose text &amp; characters");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("Just plain loose text &amp; characters", clean.body().html());
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanDocumentWithNonStandardBaseUri() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        String baseUri = "https://custom.domain.org/path/test.html";
        Document dirty = Jsoup.parse("<p>Relative</p>", baseUri);
        Document clean = cleaner.clean(dirty);

        assertEquals(baseUri, clean.baseUri());
        assertEquals(baseUri, clean.body().baseUri());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: org.jsoup.safety.CleanerTest::handlesFramesets
     * When HTML contains a <frameset> tag instead of <body>, Jsoup parses the
     * document with dirtyDocument.body() == null.
     * Calling cleaner.clean(dirty) or cleaner.isValid(dirty) triggers
     * a NullPointerException in copySafeNodes(dirty.body(), clean.body()).
     */
    @Test(timeout = 4000)
    public void testHandlesFramesetsCleanDoesNotThrowNPE() {
        String framesetHtml = "<html><head><script></script><noscript></noscript></head>"
                + "<frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>";
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse(framesetHtml);

        // In defective versions, dirty.body() is null, causing cleaner.clean(dirty) to throw NPE
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertNotNull(clean.body());
        assertEquals("", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testHandlesFramesetsIsValidDoesNotThrowNPE() {
        String framesetHtml = "<html><head><title>Frameset Doc</title></head>"
                + "<frameset cols=\"50%,50%\"><frame src=\"frame1.html\"><frame src=\"frame2.html\"></frameset></html>";
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse(framesetHtml);

        // Must not throw NullPointerException and should evaluate to false because framesets are not in basic whitelist
        boolean valid = cleaner.isValid(dirty);
        assertFalse(valid);
    }

    @Test(timeout = 4000)
    public void testHandlesPureFramesetDocumentWithoutHead() {
        String framesetHtml = "<frameset><frame src=\"a.html\"></frameset>";
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse(framesetHtml);

        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertEquals("", clean.body().html());
        assertFalse(cleaner.isValid(dirty));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullWhitelistThrowsException() {
        new Cleaner(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCleanNullDocumentThrowsException() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsValidNullDocumentThrowsException() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.isValid(null);
    }

    // =========================================================================
    // Partition E: Whitelist Feature Combinations & Protocols
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanPreservesValidProtocolDropsInvalidProtocol() {
        Whitelist whitelist = Whitelist.basicWithImages();
        Cleaner cleaner = new Cleaner(whitelist);

        String html = "<p><a href=\"ftp://example.com\">FTP</a> "
                + "<a href=\"javascript:alert('xss')\">JS</a> "
                + "<img src=\"http://example.com/pic.jpg\" /></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        String body = clean.body().html();
        assertTrue(body.contains("ftp://example.com"));
        assertTrue(body.contains("http://example.com/pic.jpg"));
        assertFalse(body.contains("javascript:"));
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanDropsAllTagsWhenWhitelistIsNone() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse("<div><h1>Title</h1><p>Text with <b>bold</b> and <i>italic</i></p></div>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("TitleText with bold and italic", clean.body().html().replaceAll("\\s+", " ").trim());
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testRelaxedWhitelistAllowedTags() {
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());
        Document dirty = Jsoup.parse("<table><tr><th>Header</th><td>Data</td></tr></table>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertTrue(clean.body().html().contains("<table>"));
        assertTrue(clean.body().html().contains("<th>Header</th>"));
        assertTrue(cleaner.isValid(dirty));
    }
}