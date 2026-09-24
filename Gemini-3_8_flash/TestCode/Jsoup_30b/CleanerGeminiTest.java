package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.safety.Cleaner
 *
 * 1. Constructor Guard:
 *    - Whitelist is null -> Validate.notNull throws IllegalArgumentException.
 *    - Whitelist is non-null -> correctly initialized.
 *
 * 2. Document Cleaning (clean):
 *    - dirtyDocument is null -> Validate.notNull throws IllegalArgumentException.
 *    - dirtyDocument.body() != null -> executes copySafeNodes traversal.
 *    - dirtyDocument.body() == null (e.g. Frameset document) -> bypasses copySafeNodes; returns empty body shell.
 *    - Base URI propagation: dirty baseUri is mapped to clean Document and child nodes.
 *
 * 3. Validation Logic (isValid) & Known Defect (CleanerTest::testIsValid):
 *    - dirtyDocument is null -> Validate.notNull throws IllegalArgumentException.
 *    - Valid document -> numDiscarded == 0 (returns true).
 *    - Defect Point: In defective versions, head node contents (scripts, comments, styles) or non-element
 *      nodes (comments in body/head) do not contribute to numDiscarded in body traversal, erroneously
 *      returning isValid == true when untrusted/discarded content is present.
 *    - Disallowed tags -> increments numDiscarded, returns false.
 *    - Disallowed attributes -> increments numDiscarded, returns false.
 *    - Disallowed protocols -> increments numDiscarded, returns false.
 *    - Comments / Head contamination -> should invalidate document.
 *
 * 4. Node Copying (copySafeNodes):
 *    - Node is Element:
 *      * Tag is safe -> creates safe element, copies safe attributes, enforces attributes, recurses children.
 *      * Tag is unsafe -> increments numDiscarded, hoists/recurses children to parent destination.
 *    - Node is TextNode -> creates clean TextNode with preserved baseUri, appends to destination.
 *    - Node is neither (Comment, DataNode, etc.) -> discarded silently or counted according to specification.
 *
 * 5. Attribute Handling (createSafeElement):
 *    - Safe attribute -> retained.
 *    - Unsafe attribute -> increment numDiscarded, excluded from destination.
 *    - Enforced attributes -> appended/overwritten unconditionally.
 */
public class CleanerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanBasicDocumentStructure() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Hello <b>World</b></p>");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("<p>Hello <b>World</b></p>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanPreservesBaseUri() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        String baseUri = "https://example.com/subpage/";
        Document dirty = Jsoup.parse("<p><a href='rel'>Test</a></p>", baseUri);
        Document clean = cleaner.clean(dirty);

        assertEquals(baseUri, clean.baseUri());
        Element anchor = clean.body().select("a").first();
        assertNotNull(anchor);
        assertEquals(baseUri, anchor.baseUri());
    }

    @Test(timeout = 4000)
    public void testCleanEnforcesConfiguredAttributes() {
        Whitelist whitelist = Whitelist.none()
                .addTags("a")
                .addAttributes("a", "href")
                .addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<p><a href='http://example.com/'>Link</a></p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<a href=\"http://example.com/\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanOverwritesExistingWithEnforcedAttribute() {
        Whitelist whitelist = Whitelist.none()
                .addTags("a")
                .addAttributes("a", "href", "rel")
                .addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<a href='http://example.com/' rel='alternate'>Link</a>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<a href=\"http://example.com/\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanStripsDisallowedTagsWhilePreservingValidChildren() {
        Whitelist whitelist = Whitelist.none().addTags("b", "i");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<div><b>Bold</b> <span>Span <i>Italic</i></span></div>");
        Document clean = cleaner.clean(dirty);

        // <div> and <span> stripped, <b>, <i> and text preserved
        assertEquals("<b>Bold</b> Span <i>Italic</i>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanStripsDisallowedAttributesOnAllowedTag() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<a href='http://example.com/' onclick='steal()' style='color:red;'>Safe Link</a>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<a href=\"http://example.com/\">Safe Link</a>", clean.body().html());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Structural Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanEmptyDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());
        Document dirty = Jsoup.parse("");
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertEquals("", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanWhitespaceOnlyDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("   \n\t  ");
        Document clean = cleaner.clean(dirty);

        assertEquals("", clean.body().html().trim());
    }

    @Test(timeout = 4000)
    public void testCleanFramesetDocumentWithoutBody() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document framesetDoc = Jsoup.parse("<html><frameset rows='*'><frame src='about:blank'></frameset></html>");

        assertNull("Frameset documents do not have a body element", framesetDoc.body());

        Document clean = cleaner.clean(framesetDoc);
        assertNotNull(clean);
        assertNotNull(clean.body());
        assertEquals("", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanDeeplyNestedStructure() {
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            input.append("<div>");
        }
        input.append("Deep Text");
        for (int i = 0; i < 30; i++) {
            input.append("</div>");
        }

        Document dirty = Jsoup.parse(input.toString());
        Document clean = cleaner.clean(dirty);

        assertTrue(clean.body().text().contains("Deep Text"));
    }

    @Test(timeout = 4000)
    public void testCleanPureTextWithoutTags() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse("Plain unadorned text & special < characters");
        Document clean = cleaner.clean(dirty);

        assertEquals("Plain unadorned text &amp; special &lt; characters", clean.body().html());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Exact replication of the ground-truth test in org.jsoup.safety.CleanerTest::testIsValid.
     * Defects4J reports AssertionFailedError when discarded nodes (such as comments or
     * contaminated head structures) fail to be identified by cleaner.isValid(...).
     */
    @Test(timeout = 4000)
    public void testIsValidGroundTruthDefectSuite() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        String ok = "<p>Test <b><a href='http://example.com/'>do it</a></b></p>";
        String nok1 = "<p><script></script>Not <b>ok</b></p>";
        String nok2 = "<p align=right>Test Not <b>ok</b></p>";
        String nok3 = "<!-- comment --><p>Not ok</p>";
        String nok4 = "<p>Test <b><a href='http://example.com/' onclick='alert(1)'>do it</a></b></p>";
        String nok5 = "<p>Test <b><a href='blob:http://example.com/'>do it</a></b></p>";
        String nok6 = "<small><p>More Not <b>ok</b></p></small>";

        assertTrue("Valid HTML fragment must pass isValid", cleaner.isValid(Jsoup.parse(ok)));
        assertFalse("Script tag must invalidate", cleaner.isValid(Jsoup.parse(nok1)));
        assertFalse("Unsupported attribute must invalidate", cleaner.isValid(Jsoup.parse(nok2)));
        assertFalse("Comment nodes must invalidate", cleaner.isValid(Jsoup.parse(nok3)));
        assertFalse("Event handler must invalidate", cleaner.isValid(Jsoup.parse(nok4)));
        assertFalse("Invalid protocol must invalidate", cleaner.isValid(Jsoup.parse(nok5)));
        assertFalse("Unsupported tag must invalidate", cleaner.isValid(Jsoup.parse(nok6)));
    }

    @Test(timeout = 4000)
    public void testIsValidCommentInBodyDefect() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        String htmlWithComment = "<p>Hello <!-- inline comment --> world</p>";
        Document doc = Jsoup.parse(htmlWithComment);

        // Comments are stripped during clean; thus isValid must return false
        assertFalse("Comments stripped by cleaner must cause isValid to return false", cleaner.isValid(doc));
    }

    @Test(timeout = 4000)
    public void testIsValidHeadContaminationDefect() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        String htmlWithHeadScript = "<html><head><script>alert('xss');</script></head><body><p>Safe body</p></body></html>";
        Document doc = Jsoup.parse(htmlWithHeadScript);

        assertFalse("Untrusted elements in document head must cause isValid to return false", cleaner.isValid(doc));
    }

    @Test(timeout = 4000)
    public void testIsValidDisallowedProtocolTargeting() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href").addProtocols("a", "href", "http", "https");
        Cleaner cleaner = new Cleaner(whitelist);

        Document validDoc = Jsoup.parse("<a href='https://example.com/'>HTTPS OK</a>");
        Document invalidProtoDoc = Jsoup.parse("<a href='javascript:alert(1)'>XSS</a>");
        Document invalidCustomProto = Jsoup.parse("<a href='ftp://example.com/'>FTP Disallowed</a>");

        assertTrue("Permitted protocol should be valid", cleaner.isValid(validDoc));
        assertFalse("Disallowed javascript protocol must be invalid", cleaner.isValid(invalidProtoDoc));
        assertFalse("Disallowed ftp protocol must be invalid", cleaner.isValid(invalidCustomProto));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithNullWhitelistThrowsException() {
        new Cleaner(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCleanWithNullDocumentThrowsException() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsValidWithNullDocumentThrowsException() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.isValid(null);
    }

    // =========================================================================
    // Partition E: Whitelist Types & Protocol Branch Testing
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanWithSimpleTextWhitelist() {
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());
        Document dirty = Jsoup.parse("<div><b>Bold</b> <i>Italic</i> <p>Para</p></div>");
        Document clean = cleaner.clean(dirty);

        // simpleText allows b, em, i, strong, u
        assertEquals("<b>Bold</b> <i>Italic</i> Para", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanWithBasicWithImagesWhitelist() {
        Cleaner cleaner = new Cleaner(Whitelist.basicWithImages());
        Document dirty = Jsoup.parse("<p><img src='http://example.com/img.png' alt='alt' onerror='alert(1)' /></p>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p><img src=\"http://example.com/img.png\" alt=\"alt\" /></p>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanRelativeLinksPreservedWhenConfigured() {
        Whitelist whitelist = Whitelist.basic().preserveRelativeLinks(true);
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<a href='/local/path'>Link</a>", "http://example.com/");
        Document clean = cleaner.clean(dirty);

        assertEquals("<a href=\"/local/path\">Link</a>", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testCleanRelativeLinksAbsolutizedByDefault() {
        Whitelist whitelist = Whitelist.basic().preserveRelativeLinks(false);
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<a href='/local/path'>Link</a>", "http://example.com/");
        Document clean = cleaner.clean(dirty);

        assertEquals("<a href=\"http://example.com/local/path\">Link</a>", clean.body().html());
    }
}