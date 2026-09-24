package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.jsoup.safety.Cleaner
 *
 * Decision / Condition Matrix:
 * 1. Constructor:
 *    - whitelist == null -> IllegalArgumentException via Validate.notNull
 *    - whitelist != null -> success
 * 2. clean(dirtyDocument):
 *    - dirtyDocument == null -> IllegalArgumentException via Validate.notNull
 *    - dirtyDocument.body() != null -> copySafeNodes executed
 *    - dirtyDocument.body() == null (e.g., frameset or stripped doc) -> safe empty body returned
 * 3. isValid(dirtyDocument):
 *    - dirtyDocument == null -> IllegalArgumentException via Validate.notNull
 *    - dirtyDocument with unsafe nodes in body -> numDiscarded > 0 -> returns false
 *    - dirtyDocument with only safe nodes in body, empty head -> returns true
 *    - [DEFECT ZONE] dirtyDocument with safe body but non-empty head (<script>, <title>, etc.)
 *      -> must return false per specification/contract (Defects4J test failure: testIsValidDocument, testIsValidBodyHtml)
 * 4. CleaningVisitor.head(Node source, int depth):
 *    - source instanceof Element:
 *      * whitelist.isSafeTag(sourceEl.tagName()) == true -> createSafeElement, dest.appendChild, traverse
 *      * whitelist.isSafeTag(sourceEl.tagName()) == false && source != root -> numDiscarded++
 *      * whitelist.isSafeTag(sourceEl.tagName()) == false && source == root -> do not count against discarded
 *    - source instanceof TextNode -> creates new TextNode, appended to destination
 *    - source instanceof DataNode:
 *      * whitelist.isSafeTag(parent.nodeName()) == true -> creates new DataNode, appended to destination
 *      * whitelist.isSafeTag(parent.nodeName()) == false -> falls to else -> numDiscarded++
 *    - source instanceof other (e.g. Comment) -> falls to else -> numDiscarded++
 * 5. CleaningVisitor.tail(Node source, int depth):
 *    - source instanceof Element && whitelist.isSafeTag(source.nodeName()) == true -> destination = destination.parent()
 *    - otherwise -> no-op
 * 6. createSafeElement(Element sourceEl):
 *    - sourceAttr is safe -> destAttrs.put(sourceAttr)
 *    - sourceAttr is unsafe -> numDiscarded++
 *    - enforcedAttributes added to destination
 */
public class CleanerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanSimpleValidDocument() {
        String html = "<div><p>Hello <b>World</b></p></div>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());

        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertEquals("<p>Hello <b>World</b></p>", clean.body().html().trim());
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanDropsUnsafeElements() {
        String html = "<p>Safe</p><script>alert('xss');</script><object>data</object>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);
        assertEquals("<p>Safe</p>", clean.body().html().trim());
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanSanitizesAttributes() {
        String html = "<p><a href=\"http://example.com/\" onclick=\"steal()\" rel=\"nofollow\">Link</a></p>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);
        Element a = clean.body().select("a").first();
        assertNotNull(a);
        assertTrue(a.hasAttr("href"));
        assertFalse(a.hasAttr("onclick"));
        assertEquals("http://example.com/", a.attr("href"));
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanAppliesEnforcedAttributes() {
        String html = "<a href=\"http://example.com/\">Test</a>";
        Document dirty = Jsoup.parse(html);
        // Whitelist.basic enforces rel="nofollow" on <a> tags
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);
        Element link = clean.body().select("a").first();
        assertNotNull(link);
        assertEquals("nofollow", link.attr("rel"));
    }

    @Test(timeout = 4000)
    public void testPreservesTextNodesAcrossHierarchy() {
        String html = "<p>Line 1<br>Line 2</p>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);
        assertEquals("<p>Line 1<br>\nLine 2</p>", clean.body().html().trim());
        assertTrue(cleaner.isValid(dirty));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanEmptyBody() {
        Document dirty = Jsoup.parse("");
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);
        assertEquals("", clean.body().html().trim());
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCleanDocumentWithNullBody() {
        // Document without <body> (e.g., frameset or explicitly removed body)
        Document dirty = Document.createShell("http://example.com/");
        Element body = dirty.body();
        if (body != null) {
            body.remove();
        }
        assertNull(dirty.body());

        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertNotNull(clean.body());
        assertEquals("", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testPreservesBaseUri() {
        String baseUri = "http://my.domain.org/path/file.html";
        Document dirty = Document.createShell(baseUri);
        dirty.body().append("<p><a href=\"rel.html\">Rel</a></p>");

        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document clean = cleaner.clean(dirty);

        assertEquals(baseUri, clean.baseUri());
        Element a = clean.body().select("a").first();
        assertNotNull(a);
        assertEquals(baseUri, a.baseUri());
        assertEquals("http://my.domain.org/path/rel.html", a.attr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedSafeTagsStackUnwinding() {
        StringBuilder sb = new StringBuilder();
        int depth = 50;
        for (int i = 0; i < depth; i++) {
            sb.append("<b>");
        }
        sb.append("Deep");
        for (int i = 0; i < depth; i++) {
            sb.append("</b>");
        }

        Document dirty = Jsoup.parse(sb.toString());
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document clean = cleaner.clean(dirty);

        assertNotNull(clean);
        assertTrue(clean.body().text().contains("Deep"));
        assertTrue(cleaner.isValid(dirty));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsValidDocumentFailsWhenHeadHasScript() {
        // Defects4J CleanerTest::testIsValidDocument
        // Document has unsafe script in the head; isValid MUST return false
        String html = "<html><head><script>alert(1);</script></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());

        assertFalse("Document with script in head must be invalid", cleaner.isValid(doc));
    }

    @Test(timeout = 4000)
    public void testIsValidDocumentFailsWhenHeadHasTitle() {
        // Defects4J: document validator specification states there must be no content in head
        String html = "<html><head><title>Title</title></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());

        assertFalse("Document with content in head must be invalid", cleaner.isValid(doc));
    }

    @Test(timeout = 4000)
    public void testIsValidBodyHtmlWithHeadContaminant() {
        // Defects4J CleanerTest::testIsValidBodyHtml
        // When input HTML pushes nodes (like title or style) into the head during parsing
        String html = "<title>Invalid</title><p>Valid</p>";
        Document doc = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());

        assertFalse("Input with title tag should be invalid according to cleaner", cleaner.isValid(doc));
    }

    @Test(timeout = 4000)
    public void testIsValidDocumentPassesWhenHeadIsEmpty() {
        String html = "<html><head></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());

        assertTrue("Document with empty head and valid body must be valid", cleaner.isValid(doc));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullWhitelistThrows() {
        new Cleaner(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCleanNullDocumentThrows() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsValidNullDocumentThrows() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.isValid(null);
    }

    // =========================================================================
    // Partition E: Advanced Node Hierarchy, DataNodes & Comments
    // =========================================================================

    @Test(timeout = 4000)
    public void testCommentNodesAreDiscarded() {
        String html = "<div><!-- This is a secret comment --><p>Content</p></div>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());

        Document clean = cleaner.clean(dirty);
        assertFalse(clean.body().html().contains("secret comment"));
        assertFalse("Dirty document containing comment should count as discarded nodes", cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testDataNodeInSafeParent() {
        // When a parent element containing a DataNode (like style/script) is explicitly safe
        Whitelist whitelist = new Whitelist().addTags("script");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<script>var x = 123;</script>");
        Document clean = cleaner.clean(dirty);

        Element script = clean.body().select("script").first();
        assertNotNull(script);
        assertEquals("var x = 123;", script.data());
        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testDataNodeInUnsafeParentIsDiscarded() {
        // Whitelist allows 'p' but NOT 'style'. Style child is a DataNode.
        Whitelist whitelist = Whitelist.none().addTags("p");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Jsoup.parse("<p>Safe</p><style>body { color: red; }</style>");
        Document clean = cleaner.clean(dirty);

        assertEquals("<p>Safe</p>", clean.body().html().trim());
        assertFalse(clean.body().html().contains("color: red"));
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testUnsafeChildInsideSafeParentIsDiscarded() {
        String html = "<div><span>Safe</span><blink>Blink</blink></div>";
        Document dirty = Jsoup.parse(html);
        Whitelist whitelist = Whitelist.none().addTags("div", "span");
        Cleaner cleaner = new Cleaner(whitelist);

        Document clean = cleaner.clean(dirty);
        assertEquals("<div><span>Safe</span>Blink</div>", clean.body().html().replaceAll("\\s+", ""));
        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testCustomCommentNodeAppendedToBodyIsDiscarded() {
        Document dirty = Document.createShell("");
        dirty.body().appendChild(new Comment("Explicit Comment", ""));
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        assertEquals("", clean.body().html());
    }

    @Test(timeout = 4000)
    public void testUnsafeRootBodyTagNotCountedAgainstDiscarded() {
        // When traversing, the body root itself is passed as the traversal root.
        // Even if 'body' is not in the whitelist, it should not be counted as discarded.
        Document dirty = Document.createShell("");
        Element p = dirty.body().appendElement("p");
        p.text("Text");

        Whitelist whitelist = Whitelist.none().addTags("p");
        Cleaner cleaner = new Cleaner(whitelist);

        // Body tag itself isn't safe tag in whitelist, but only its content is checked
        assertTrue(cleaner.isValid(dirty));
    }
}