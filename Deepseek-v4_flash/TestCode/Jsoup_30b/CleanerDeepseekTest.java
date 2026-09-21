package org.jsoup.safety;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box JUnit 4 test suite for {@link Cleaner} targeting 100% line/branch coverage
 * and the known Defects4J bug where {@link Cleaner#isValid(Document)} throws
 * NullPointerException when the document has an absent body (e.g., frameset).
 *
 * <h3>Branch & Defect Analysis Matrix</h3>
 * <ul>
 *   <li><b>Coverage targets in {@link Cleaner#copySafeNodes(Element, Element)}:</b>
 *     <ul>
 *       <li>source instanceof Element (Element) / not instanceof (TextNode)</li>
 *       <li>whitelist.isSafeTag(tagName) == true / false</li>
 *       <li>For a safe tag: recursive call and attribute discarding</li>
 *       <li>For an unsafe tag: increment discard count and recursive call (flatten children)</li>
 *       <li>TextNode: copy as new TextNode</li>
 *       <li>Empty children (no loop) – covered by leaf elements</li>
 *     </ul>
 *   </li>
 *   <li><b>Coverage targets in {@link Cleaner#createSafeElement(Element)}:</b>
 *     <ul>
 *       <li>isSafeAttribute == true / false → attribute kept / discarded</li>
 *       <li>Enforced attributes added from whitelist</li>
 *     </ul>
 *   </li>
 *   <li><b>Coverage targets in {@link Cleaner#clean(Document)} and {@link Cleaner#isValid(Document)}:</b>
 *     <ul>
 *       <li>dirtyDocument.body() != null / null</li>
 *       <li>clean() always handles null body; isValid() does NOT (bug)</li>
 *     </ul>
 *   </li>
 *   <li><b>Defect-specific test:</b> {@link #testIsValidFramesetDocument()} triggers the NPE in
 *       the defective version by passing a document without body to isValid(). The test expects
 *       no exception and returns false; on the buggy version it fails with NullPointerException.</li>
 * </ul>
 *
 * <b>Environment:</b> Java 8, JUnit 4
 */
public class CleanerDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testCleanEmptyDocument() {
        // Document with empty body
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.createShell("http://example.com");
        // no children in body
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertEquals("http://example.com", clean.baseUri());
        assertNotNull(clean.body());
        assertTrue(clean.body().childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testCleanWithSafeTagAndText() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        // Build a simple document with <b>hello</b> (b is in basic whitelist)
        Document dirty = Document.createShell("http://example.com");
        Element body = dirty.body();
        Element b = new Element(Tag.valueOf("b"), "");
        b.appendChild(new TextNode("hello", ""));
        body.appendChild(b);

        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().childNodes().size());
        Element cloneB = (Element) clean.body().childNodes().get(0);
        assertEquals("b", cloneB.tagName());
        assertEquals(1, cloneB.childNodes().size());
        assertEquals("hello", ((TextNode) cloneB.childNodes().get(0)).getWholeText());
    }

    @Test(timeout = 4000)
    public void testCleanWithUnsafeTag() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        // <script> is not allowed
        Document dirty = Document.reateShell("http://example.com");
        Element body = dirty.body();
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new TextNode("alert('xss')", ""));
        body.appendChild(script);

        Document clean = cleaner.clean(dirty);
        // script tag is stripped, but its text is not (since script is not safe, its children are processed recursively)
        assertEquals(1, clean.body().childNodes().size()); // text node remains
        assertEquals("alert('xss')", ((TextNode) clean.body().childNodes().get0)).getWholeText());
    }

    @Test(timeout = 4000)
    public void testCleanWithFramesetDocument() {
        // Document without body (frameset)
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = new Document("http://example.com");
        // no body added

        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertNotNull(clean.body()); // shell always has body
        assertTrue(clean.body().childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsValidValidDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.reateShell("http://example.com");
        Element body = dirty.body();
        body.appendChild(new TextNode("just text", ""));

        assertTrue(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidUnsafeTag() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.reateShell("http://example.com");
        Element body = dirty.body();
        Element script = new Element(Tag.valueOf("script"), "");
        body.appendChild(script);

        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidUnsafeAttribute() {
        // Create a whitelist that allows <a> but only certain attributes
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags("a");
        whitelist.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Document.createShell("http://example.com");
        Element body = dirty.body();
        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "http://safe.com");
        a.attr("onclick", "evil();"); // not allowed
        body.appendChild(a);

        assertFalse(cleaner.isValid(dirty));
    }

    @Test(timeout = 4000)
    public void testIsValidWithEnforcedAttributes() {
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags("img");
        whitelist.addEnforcedAttribute("img", "alt", "placeholder");
        Cleaner cleaner = new Cleaner(whitelist);

        Document dirty = Document.createShell("http://example.com");
        Element body = dirty.body();
        Element img = new Element(Tag.valueOf("img"), "");
        // No alt attribute originally
        body.appendChild(img);

        // isValid should return false because enforced attribute is missing? No, getEnforcedAttributes just adds them; the clean document will have them, but isValid looks at original dirty.
        // The dirty document lacks the enforced attribute, so isValid should return true? Actually enforced attributes are added by clean, but isValid counts discarded nodes based on whitelist checks.
        // The attribute is not discarded because it's not present; it's just not in the dirty. So numDiscarded=0, isValid true.
        assertTrue(cleaner.isValid(dirty));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullWhitelist() {
        new Cleaner(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCleanNullDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.clean(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsValidNullDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        cleaner.isValid(null);
    }

    @Test(timeout = 4000)
    public void testCleanWithEmptyStringDocument() {
        // baseUri empty
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.createShell("");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly targets the known Defects4J bug: Calling {@link #isValid(Document)} on a document
     * without a body (frameset style) throws NullPointerException in the defective version.
     * The fixed version should return false (no nodes to clean).
     *
     * This test passes with a fixed version (assertFalse) and fails with the buggy version (throws NPE).
     */
    @Test(timeout = 4000)
    public void testIsValidFramesetDocument() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        // Create a document with no body (like a frameset)
        Document dirty = new Document("http://example.com");
        // Ensure no body element is present
        assertNull("Document should have no body for this test", dirty.body());
        assertFalse("isValid on document without body should be false", cleaner.isValid(dirty));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========
    // (Already covered: constructor null, clean null, isValid null)

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // Cleaner has no equals/hashCode/clone; only state is whitelist reference.
    // We verify that whitelsit is used correctly through behavior.

    @Test(timeout = 4000)
    public void testMultipleCallsWithSameCleaner() {
        Whitelist whitelist = Whitelist.basic();
        Cleaner cleaner = new Cleaner(whitelist);
        Document d1 = Document.createShell("http://a.com");
        Document d2 = Document.reateShell("http://b.com");
        assertTrue(cleaner.isValid(d1));
        assertTrue(cleaner.isValid(d2));
    }
}