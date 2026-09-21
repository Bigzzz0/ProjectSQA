package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.parser.Tag;
import java.util.List;

/**
 * Comprehensive test suite for Node class targeting line/branch coverage and the known Defects4J defect
 * in absUrl handling of relative query strings.
 *
 * [Branch & Defect Analysis Matrix]
 * - Core methods: attr, hasAttr, removeAttr, baseUri, setBaseUri, parent, childNodes, siblingNodes,
 *   nextSibling, previousSibling, siblingIndex, setSiblingIndex, remove, replaceWith,
 *   childNode, ownerDocument, outerHtml, toString, equals, hashCode, clone.
 * - Boundary/Defect branches:
 *   - absUrl: missing attribute => return ""
 *   - absUrl: base MalformedURLException => try relative as absolute
 *   - absUrl: base and relative form valid URL (normal)
 *   - absUrl: relative starting with '?' (defect: URL resolves incorrectly)
 *   - absUrl: relative empty string (via attr returns "")
 *   - attr: key null => Validate.notNull throw
 *   - attr: attribute missing => return ""
 *   - attr: "abs:" prefix => delegates to absUrl
 *   - hasAttr: null key => throw
 *   - removeAttr: null key => throw
 *   - setBaseUri: null => throw
 *   - remove: parent null => throw
 *   - replaceWith: null => throw; parent null => throw
 *   - childNode: index out of bounds => IndexOutOfBoundsException
 *   - siblingNodes: parent null => NPE? (line parent().childNodes())
 *   - nextSibling/previousSibling: parent null => null
 *   - equals: always false (except identity)
 *   - hashCode: combines parent+attributes
 *   - clone: deep copy, orphan
 *   - Default constructor: childNodes empty unmodifiable, attributes null
 *   - Null/empty boundary for attr, hasAttr, absUrl
 *   - ownerDocument: root Document, null parent, parent chain
 */
public class NodeDeepseekTest {

    // ---------- Helper to create a simple Document/Element hierarchy ----------
    private Document doc;
    private Element root;
    private Element child1;
    private Element child2;
    private TextNode text1;

    @Test(timeout = 4000)
    public void setupNodes() {
        doc = new Document("http://example.com/base/");
        root = new Element(Tag.valueOf("div"), doc.baseUri());
        doc.appendChild(root);
        child1 = new Element(Tag.valueOf("p"), doc.baseUri());
        child1.attr("id", "first");
        child2 = new Element(Tag.valueOf("span"), doc.baseUri());
        text1 = new TextNode("hello", doc.baseUri());
        root.addChildren(child1, child2, text1);
        // child1 = index 0, child2 = index 1, text1 = index 2
    }

    // ======================== Partition A: Core Functional Logic ========================

    @Test(timeout = 4000)
    public void testAttrGet() {
        setupNodes();
        assertEquals("first", child1.attr("id"));
        assertEquals("", child1.attr("nonexistent"));
        // abs: prefix
        child2.attr("href", "page.html");
        String abs = child2.attr("abs:href");
        assertTrue(abs.startsWith("http://example.com/base/"));
    }

    @Test(timeout = 4000)
    public void testAttrSet() {
        setupNodes();
        Node n = child1.attr("newAttr", "value");
        assertSame(child1, n);
        assertEquals("value", child1.attr("newAttr"));
    }

    @Test(timeout = 4000)
    public void testHasAttr() {
        setupNodes();
        assertTrue(child1.hasAttr("id"));
        assertFalse(child1.hasAttr("class"));
    }

    @Test(timeout = 4000)
    public void testRemoveAttr() {
        setupNodes();
        child1.removeAttr("id");
        assertFalse(child1.hasAttr("id"));
    }

    @Test(timeout = 4000)
    public void testBaseUri() {
        setupNodes();
        assertEquals("http://example.com/base/", doc.baseUri());
        child1.setBaseUri("http://other.com/");
        assertEquals("http://other.com/", child1.baseUri());
    }

    @Test(timeout = 4000)
    public void testParent() {
        setupNodes();
        assertSame(root, child1.parent());
        assertSame(doc, root.parent());
        assertNull(doc.parent());
    }

    @Test(timeout = 4000)
    public void testChildNodes() {
        setupNodes();
        List<Node> children = root.childNodes();
        assertEquals(3, children.size());
        assertEquals(child1, children.get(0));
    }

    @Test(timeout = 4000)
    public void testChildNode() {
        setupNodes();
        assertSame(child1, root.childNode(0));
        assertSame(text1, root.childNode(2));
    }

    @Test(timeout = 4000)
    public void testSiblingNodes() {
        setupNodes();
        List<Node> siblings = child1.siblingNodes();
        assertEquals(3, siblings.size());
        assertTrue(siblings.contains(child1));
        assertTrue(siblings.contains(child2));
    }

    @Test(timeout = 4000)
    public void testNextSibling() {
        setupNodes();
        assertSame(child2, child1.nextSibling());
        assertSame(text1, child2.nextSibling());
        assertNull(text1.nextSibling());
        // root (no parent)
        assertNull(doc.nextSibling());
    }

    @Test(timeout = 4000)
    public void testPreviousSibling() {
        setupNodes();
        assertNull(child1.previousSibling());
        assertSame(child1, child2.previousSibling());
        assertSame(child2, text1.previousSibling());
    }

    @Test(timeout = 4000)
    public void testSiblingIndex() {
        setupNodes();
        assertEquals(0, (int) child1.siblingIndex());
        assertEquals(1, (int) child2.siblingIndex());
        assertEquals(2, (int) text1.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testRemove() {
        setupNodes();
        child1.remove();
        assertNull(child1.parent());
        List<Node> children = root.childNodes();
        assertEquals(2, children.size());
        assertEquals(child2, children.get(0));
    }

    @Test(timeout = 4000)
    public void testReplaceWith() {
        setupNodes();
        Element replacement = new Element(Tag.valueOf("b"), doc.baseUri());
        child1.replaceWith(replacement);
        assertSame(root, replacement.parent());
        assertNull(child1.parent());
        assertSame(replacement, root.childNode(0));
    }

    @Test(timeout = 4000)
    public void testOwnerDocument() {
        setupNodes();
        assertSame(doc, child1.ownerDocument());
        assertSame(doc, doc.ownerDocument());
        // orphan node
        Node orphan = new TextNode("orphan", "");
        assertNull(orphan.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        setupNodes();
        String html = child1.outerHtml();
        assertTrue(html.contains("id=\"first\""));
        assertTrue(html.startsWith("<p"));
        // text node
        assertEquals("hello", text1.outerHtml());
    }

    @Test(timeout = 4000)
    public void testToString() {
        setupNodes();
        assertEquals(child1.outerHtml(), child1.toString());
    }

    // ======================== Partition B: Boundary Value Analysis ========================

    @Test(timeout = 4000)
    public void testAttrNullKey() {
        try {
            child1.attr((String) null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testHasAttrNullKey() {
        try {
            child1.hasAttr(null);
            fail();
        } catch (IllegalArgumentException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testRemoveAttrNullKey() {
        try {
            child1.removeAttr(null);
            fail();
        } catch (IllegalArgumentException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testSetBaseUriNull() {
        try {
            child1.setBaseUri(null);
            fail();
        } catch (IllegalArgumentException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testRemoveParentNull() {
        // orphan node cannot be removed
        Node orphan = new TextNode("orphan", "");
        try {
            orphan.remove();
            fail();
        } catch (IllegalArgumentException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testReplaceWithNull() {
        setupNodes();
        try {
            child1.replaceWith(null);
            fail();
        } catch (IllegalArgumentException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testReplaceWithOrphanParentNull() {
        Node orphan = new TextNode("orphan", "");
        try {
            orphan.replaceWith(new TextNode("x", ""));
            fail();
        } catch (IllegalArgumentException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testChildNodeOutOfBounds() {
        setupNodes();
        try {
            root.childNode(10);
            fail();
        } catch (IndexOutOfBoundsException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testAbsUrlEmptyAttribute() {
        // missing attribute => ""
        Node n = new TextNode("test", "http://base.com/");
        // TextNode has no attributes, but attr returns "" always (since hasAttr false)
        assertEquals("", n.absUrl("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlMalformedBaseTryRelative() {
        // baseUri is not a valid URL, but relative URL is absolute
        Node n = new Element(Tag.valueOf("a"), "not-a-url");
        n.attr("href", "http://valid.com/page");
        assertEquals("http://valid.com/page", n.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlNormal() {
        Node n = new Element(Tag.valueOf("a"), "http://example.com/dir/");
        n.attr("href", "page.html");
        assertEquals("http://example.com/dir/page.html", n.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlAlreadyAbsolute() {
        Node n = new Element(Tag.valueOf("a"), "http://example.com/");
        n.attr("href", "http://other.com/path");
        assertEquals("http://other.com/path", n.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlBaseNoPath() {
        Node n = new Element(Tag.valueOf("a"), "http://example.com");
        n.attr("href", "?query");
        // base has no path -> result should be http://example.com/?query
        assertEquals("http://example.com/?query", n.absUrl("href"));
    }

    // ======================== Partition C: Defect-Targeted (absHandlesRelativeQuery) ========================

    @Test(timeout = 4000)
    public void testAbsHandlesRelativeQuery() {
        // Known defect: URL.resolve loses file component when relative starts with '?'
        // e.g. base: http://jsoup.org/path/file, relative: ?foo => expected: http://jsoup.org/path/file?foo
        // Bug: returns http://jsoup.org/path/?foo (loses "file")
        Node n = new Element(Tag.valueOf("a"), "http://jsoup.org/path/file");
        n.attr("href", "?foo");
        String result = n.absUrl("href");
        assertEquals("http://jsoup.org/path/file?foo", result);
    }

    // Additional variant: relative with fragment
    @Test(timeout = 4000)
    public void testAbsHandlesRelativeFragment() {
        Node n = new Element(Tag.valueOf("a"), "http://jsoup.org/path/file");
        n.attr("href", "#section");
        assertEquals("http://jsoup.org/path/file#section", n.absUrl("href"));
    }

    // ======================== Partition D: Exception & Defensive Guard Paths ========================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbsUrlEmptyKey() {
        Node n = new TextNode("", "");
        n.absUrl("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbsUrlNullKey() {
        Node n = new TextNode("", "");
        n.absUrl(null);
    }

    @Test(timeout = 4000)
    public void testAttrAbsPrefixNoAttribute() {
        // abs: prefix but attribute missing -> returns "" (via attr logic)
        Node n = new TextNode("", "http://base.com/");
        assertEquals("", n.attr("abs:missing"));
    }

    @Test(timeout = 4000)
    public void testDefaultConstructorBehavior() {
        // Node() default constructor: children empty unmodifiable, attributes null
        // Use a concrete node that uses that constructor? TextNode does not use it.
        // We'll create a custom anonymous subclass? But we can test via Document's default? Document uses other constructor.
        // Instead, test through existing class: org.jsoup.nodes.Node has default, but subclasses may override.
        // We'll skip direct test of default constructor because it's protected and not used by public classes.
        // Nevertheless, we can indirectly test: a node created with default will have null attributes and empty childNodes.
        // We'll create a simple anonymous class for testing (since we're in same package).
        Node n = new Node() {
            @Override
            public String nodeName() { return "test"; }
            @Override
            void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
                accum.append("head");
            }
            @Override
            void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
                accum.append("tail");
            }
        };
        assertNull(n.attributes);
        assertTrue(n.childNodes.isEmpty());
        // Operations that require attributes will NPE
        try {
            n.hasAttr("x");
            fail("Expected NPE");
        } catch (NullPointerException e) { /* ok, null attributes */ }
    }

    // ======================== Partition E: Object Lifecycle & Contract Integrity ========================

    @Test(timeout = 4000)
    public void testEquals() {
        setupNodes();
        assertFalse(child1.equals(child2));
        assertFalse(child1.equals(null));
        assertTrue(child1.equals(child1));
        // Node.equals always returns false for different objects (only identity)
        Element sameContent = new Element(Tag.valueOf("p"), "");
        sameContent.attr("id", "first");
        assertFalse(child1.equals(sameContent));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        setupNodes();
        int hc = child1.hashCode();
        // should be consistent
        assertEquals(hc, child1.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() {
        setupNodes();
        Node cloned = child1.clone();
        assertEquals(child1.attributes(), cloned.attributes());
        assertNull(cloned.parent());
        assertEquals(0, (int) cloned.siblingIndex());
        // children should be deep cloned
        assertEquals(child1.childNodes().size(), cloned.childNodes().size());
        for (int i = 0; i < child1.childNodes().size(); i++) {
            assertNotSame(child1.childNode(i), cloned.childNode(i));
        }
        // clone of leaf node
        Node textClone = text1.clone();
        assertNull(textClone.parent());
        assertEquals(0, (int) textClone.siblingIndex());
    }

    // Additional coverage: siblingNodes on root (no parent) - might throw NPE
    @Test(timeout = 4000)
    public void testSiblingNodesNoParent() {
        Node orphan = new TextNode("alone", "");
        try {
            orphan.siblingNodes();
            fail("Should throw NullPointerException because parent is null");
        } catch (NullPointerException e) { /* expected */ }
    }

    // cover addChildren variant with index
    @Test(timeout = 4000)
    public void testAddChildrenWithIndex() {
        setupNodes();
        Element newKid = new Element(Tag.valueOf("br"), doc.baseUri());
        root.addChildren(1, newKid); // insert at index 1
        assertEquals(4, root.childNodes().size());
        assertEquals(child1, root.childNode(0));
        assertEquals(newKid, root.childNode(1));
        assertEquals(child2, root.childNode(2));
        assertEquals(text1, root.childNode(3));
    }

    // cover addChildren multiple at once
    @Test(timeout = 4000)
    public void testAddChildrenMultiple() {
        setupNodes();
        Element a = new Element(Tag.valueOf("a"), doc.baseUri());
        Element b = new Element(Tag.valueOf("b"), doc.baseUri());
        root.addChildren(a, b);
        assertEquals(5, root.childNodes().size());
        assertEquals(a, root.childNode(3));
        assertEquals(b, root.childNode(4));
    }

    // cover reparenting (remove from old parent when adding)
    @Test(timeout = 4000)
    public void testAddChildrenReparent() {
        setupNodes();
        // add child2 to another parent; should be removed from root
        Element newParent = new Element(Tag.valueOf("div"), doc.baseUri());
        doc.appendChild(newParent);
        newParent.addChildren(child2);
        assertEquals(2, root.childNodes().size()); // child2 removed from root
        assertSame(newParent, child2.parent());
        assertFalse(root.childNodes().contains(child2));
    }

    // cover removeChild and reindex
    @Test(timeout = 4000)
    public void testRemoveChildThenIndex() {
        setupNodes();
        root.removeChild(child2);
        assertEquals(2, root.childNodes().size());
        assertEquals(child1, root.childNode(0));
        assertEquals(text1, root.childNode(1));
        assertEquals(0, (int) child1.siblingIndex());
        assertEquals(1, (int) text1.siblingIndex());
    }
}