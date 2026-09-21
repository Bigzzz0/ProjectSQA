package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Advanced white-box test suite for Node.java targeting the known Defects4J defects.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - attr(String), attr(String, String), hasAttr, removeAttr, baseUri, setBaseUri
 *   - absUrl (relative/absolute/base malformed)
 *   - childNode, childNodes, parent, ownerDocument
 *   - remove, before/after (HTML and Node), wrap, unwrap, replaceWith
 *   - siblingNodes, nextSibling, previousSibling, siblingIndex
 *   - traverse, outerHtml, equals, hashCode, clone
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments (Validate.notNull)
 *   - empty strings (Validate.notEmpty)
 *   - missing attributes
 *   - empty child list
 *   - null parent (orphan node)
 *   - index out of bounds
 *   - baseUri with malformed URL
 *   - attribute key with "abs:" prefix
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - siblingNodes() includes self (bug: returns parent.childNodes() which includes self)
 *   - orphan node siblingNodes() throws NullPointerException (bug: parent() is null)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Validate.notNull on attributeKey, html, node, baseUri
 *   - Validate.notEmpty on attributeKey in absUrl
 *   - Validate.isTrue in replaceChild, removeChild
 *   - Validate.noNullElements in addChildren(int, Node...)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals (always returns false except same reference)
 *   - hashCode (consistent with equals)
 *   - clone (deep copy, orphan)
 *   - doClone with parent
 */
public class NodeDeepseekTest {

    // --- Concrete test node implementation ---
    private static class TestNode extends Node {
        private final String name;

        TestNode(String baseUri, Attributes attributes, String name) {
            super(baseUri, attributes);
            this.name = name;
        }

        TestNode(String baseUri, String name) {
            super(baseUri);
            this.name = name;
        }

        TestNode() {
            super();
            this.name = "test";
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<").append(name).append(">");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</").append(name).append(">");
        }
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testAttrGetSet() {
        TestNode node = new TestNode("http://example.com", "div");
        node.attr("id", "main");
        assertEquals("main", node.attr("id"));
        assertEquals("", node.attr("nonexistent"));
        assertTrue(node.hasAttr("id"));
        assertFalse(node.hasAttr("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAttrAbsPrefix() {
        TestNode node = new TestNode("http://example.com", "a");
        node.attr("href", "/path");
        // absUrl is called via attr("abs:href")
        assertEquals("http://example.com/path", node.attr("abs:href"));
        // when attribute missing, absUrl returns ""
        assertEquals("", node.attr("abs:missing"));
    }

    @Test(timeout = 4000)
    public void testHasAttrAbsPrefix() {
        TestNode node = new TestNode("http://example.com", "a");
        node.attr("href", "/path");
        assertTrue(node.hasAttr("abs:href"));
        // if attribute exists but absUrl fails (e.g., baseUri malformed), hasAttr returns false
        TestNode badBase = new TestNode("invalid://", "a");
        badBase.attr("href", "/path");
        assertFalse(badBase.hasAttr("abs:href")); // absUrl returns "" due to MalformedURLException
    }

    @Test(timeout = 4000)
    public void testRemoveAttr() {
        TestNode node = new TestNode("http://example.com", "div");
        node.attr("class", "foo");
        assertTrue(node.hasAttr("class"));
        node.removeAttr("class");
        assertFalse(node.hasAttr("class"));
    }

    @Test(timeout = 4000)
    public void testBaseUri() {
        TestNode node = new TestNode("http://example.com/", "div");
        assertEquals("http://example.com/", node.baseUri());
        node.setBaseUri("https://new.org/");
        assertEquals("https://new.org/", node.baseUri());
    }

    @Test(timeout = 4000)
    public void testAbsUrl() {
        TestNode node = new TestNode("http://example.com/base/", "a");
        node.attr("href", "page.html");
        assertEquals("http://example.com/base/page.html", node.absUrl("href"));

        // absolute URL already
        node.attr("href", "http://other.com/");
        assertEquals("http://other.com/", node.absUrl("href"));

        // missing attribute
        assertEquals("", node.absUrl("missing"));

        // baseUri malformed, but attribute is absolute
        TestNode badBase = new TestNode("not-a-url", "a");
        badBase.attr("href", "http://valid.com/");
        assertEquals("http://valid.com/", badBase.absUrl("href"));

        // both malformed -> empty string
        badBase.attr("href", "relative");
        assertEquals("", badBase.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testChildNodeAccess() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child1 = new TestNode("http://example.com", "child1");
        TestNode child2 = new TestNode("http://example.com", "child2");
        parent.addChildren(child1, child2);

        assertEquals(child1, parent.childNode(0));
        assertEquals(child2, parent.childNode(1));
        assertEquals(2, parent.childNodes().size());
        assertTrue(parent.childNodes().contains(child1));
        assertTrue(parent.childNodes().contains(child2));
    }

    @Test(timeout = 4000)
    public void testParentAndOwnerDocument() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);
        assertEquals(parent, child.parent());
        assertNull(child.ownerDocument()); // no Document ancestor

        // if child is a Document itself
        Document doc = new Document("http://example.com");
        assertEquals(doc, doc.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testRemove() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);
        assertEquals(1, parent.childNodes().size());
        child.remove();
        assertEquals(0, parent.childNodes().size());
        assertNull(child.parent());
    }

    @Test(timeout = 4000)
    public void testBeforeAfterHtml() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);

        // before HTML
        child.before("<span>before</span>");
        assertEquals(2, parent.childNodes().size());
        assertEquals("span", parent.childNode(0).nodeName());

        // after HTML
        child.after("<span>after</span>");
        assertEquals(3, parent.childNodes().size());
        assertEquals("span", parent.childNode(2).nodeName());
    }

    @Test(timeout = 4000)
    public void testBeforeAfterNode() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);

        TestNode beforeNode = new TestNode("http://example.com", "before");
        child.before(beforeNode);
        assertEquals(beforeNode, parent.childNode(0));

        TestNode afterNode = new TestNode("http://example.com", "after");
        child.after(afterNode);
        assertEquals(afterNode, parent.childNode(2));
    }

    @Test(timeout = 4000)
    public void testWrap() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);

        child.wrap("<div class='wrapper'></div>");
        // after wrap, child should be inside the wrapper div
        assertEquals("div", parent.childNode(0).nodeName());
        Node wrapper = parent.childNode(0);
        assertEquals(1, wrapper.childNodes().size());
        assertEquals(child, wrapper.childNode(0));
    }

    @Test(timeout = 4000)
    public void testUnwrap() {
        TestNode grandparent = new TestNode("http://example.com", "gp");
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        grandparent.addChildren(parent);
        parent.addChildren(child);

        Node firstChild = parent.unwrap();
        assertEquals(child, firstChild);
        // parent removed, child now direct child of grandparent
        assertEquals(1, grandparent.childNodes().size());
        assertEquals(child, grandparent.childNode(0));
    }

    @Test(timeout = 4000)
    public void testReplaceWith() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode old = new TestNode("http://example.com", "old");
        TestNode replacement = new TestNode("http://example.com", "new");
        parent.addChildren(old);

        old.replaceWith(replacement);
        assertEquals(replacement, parent.childNode(0));
        assertNull(old.parent());
    }

    @Test(timeout = 4000)
    public void testSiblingIndex() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child0 = new TestNode("http://example.com", "c0");
        TestNode child1 = new TestNode("http://example.com", "c1");
        TestNode child2 = new TestNode("http://example.com", "c2");
        parent.addChildren(child0, child1, child2);

        assertEquals(0, child0.siblingIndex());
        assertEquals(1, child1.siblingIndex());
        assertEquals(2, child2.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testNextPreviousSibling() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child0 = new TestNode("http://example.com", "c0");
        TestNode child1 = new TestNode("http://example.com", "c1");
        TestNode child2 = new TestNode("http://example.com", "c2");
        parent.addChildren(child0, child1, child2);

        assertEquals(child1, child0.nextSibling());
        assertEquals(child2, child1.nextSibling());
        assertNull(child2.nextSibling());

        assertNull(child0.previousSibling());
        assertEquals(child0, child1.previousSibling());
        assertEquals(child1, child2.previousSibling());
    }

    @Test(timeout = 4000)
    public void testTraverse() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);

        final StringBuilder sb = new StringBuilder();
        parent.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                sb.append("H").append(node.nodeName());
            }
            @Override
            public void tail(Node node, int depth) {
                sb.append("T").append(node.nodeName());
            }
        });
        assertEquals("HparentHchildTchildTparent", sb.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        TestNode node = new TestNode("http://example.com", "div");
        assertEquals("<div></div>", node.outerHtml());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        TestNode node1 = new TestNode("http://example.com", "a");
        TestNode node2 = new TestNode("http://example.com", "a");
        // equals only returns true for same reference
        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(node2));
        // hashCode consistency: same object returns same hash
        assertEquals(node1.hashCode(), node1.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() {
        TestNode original = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        original.addChildren(child);

        Node cloned = original.clone();
        assertNotSame(original, cloned);
        assertNull(cloned.parent());
        assertEquals(1, cloned.childNodes().size());
        assertNotSame(child, cloned.childNode(0));
        // cloned child should have parent set to cloned
        assertEquals(cloned, cloned.childNode(0).parent());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        TestNode node = new TestNode("http://example.com", "div");
        node.attr(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHasAttrNullKey() {
        TestNode node = new TestNode("http://example.com", "div");
        node.hasAttr(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveAttrNullKey() {
        TestNode node = new TestNode("http://example.com", "div");
        node.removeAttr(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        TestNode node = new TestNode("http://example.com", "div");
        node.setBaseUri(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        TestNode node = new TestNode("http://example.com", "a");
        node.absUrl("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBeforeHtmlNull() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);
        child.before((String) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBeforeNodeNull() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);
        child.before((Node) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAfterHtmlNull() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);
        child.after((String) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAfterNodeNull() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);
        child.after((Node) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWrapEmptyHtml() {
        TestNode node = new TestNode("http://example.com", "div");
        node.wrap("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReplaceWithNull() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);
        child.replaceWith(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveOrphan() {
        TestNode orphan = new TestNode("http://example.com", "orphan");
        orphan.remove(); // parent is null -> Validate.notNull fails
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnwrapOrphan() {
        TestNode orphan = new TestNode("http://example.com", "orphan");
        orphan.unwrap();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddChildrenWithNull() {
        TestNode parent = new TestNode("http://example.com", "parent");
        parent.addChildren(0, (Node) null);
    }

    @Test(timeout = 4000)
    public void testChildNodeOutOfBounds() {
        TestNode parent = new TestNode("http://example.com", "parent");
        try {
            parent.childNode(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testOrphanNextSibling() {
        TestNode orphan = new TestNode("http://example.com", "orphan");
        assertNull(orphan.nextSibling());
    }

    @Test(timeout = 4000)
    public void testOrphanPreviousSibling() {
        TestNode orphan = new TestNode("http://example.com", "orphan");
        assertNull(orphan.previousSibling());
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    /**
     * Defect: siblingNodes() includes the node itself because it returns parent().childNodes()
     * which contains all children including this node.
     * Expected: siblingNodes() should return all siblings except this node.
     */
    @Test(timeout = 4000)
    public void testSiblingNodesDoesNotIncludeSelf() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child1 = new TestNode("http://example.com", "c1");
        TestNode child2 = new TestNode("http://example.com", "c2");
        TestNode child3 = new TestNode("http://example.com", "c3");
        parent.addChildren(child1, child2, child3);

        // siblingNodes() on child2 should return [child1, child3] (size 2)
        List<Node> siblings = child2.siblingNodes();
        assertEquals("siblingNodes should exclude self", 2, siblings.size());
        assertTrue(siblings.contains(child1));
        assertTrue(siblings.contains(child3));
        assertFalse(siblings.contains(child2));
    }

    /**
     * Defect: orphan node (no parent) causes NullPointerException when calling siblingNodes()
     * because parent() returns null and then .childNodes() is called on null.
     * Expected: return an empty list (as per javadoc: "If the node has no parent, returns an empty list.")
     */
    @Test(timeout = 4000)
    public void testOrphanNodeSiblingNodesReturnsEmptyList() {
        TestNode orphan = new TestNode("http://example.com", "orphan");
        List<Node> siblings = orphan.siblingNodes();
        assertNotNull("siblingNodes should not throw NPE", siblings);
        assertTrue("orphan node should have empty sibling list", siblings.isEmpty());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTraverseNullVisitor() {
        TestNode node = new TestNode("http://example.com", "div");
        node.traverse(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReplaceChildInvalidParent() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        // replaceChild called with out not being a child of this
        parent.replaceChild(child, new TestNode("http://example.com", "new"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveChildInvalidParent() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        // removeChild called with out not being a child of this
        parent.removeChild(child);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testCloneDeepCopyIndependence() {
        TestNode original = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        original.addChildren(child);
        child.attr("data", "value");

        Node cloned = original.clone();
        // modify original
        original.attr("newAttr", "new");
        child.attr("data", "changed");

        // cloned should be unaffected
        assertFalse(cloned.hasAttr("newAttr"));
        assertEquals("value", ((TestNode) cloned.childNode(0)).attr("data"));
    }

    @Test(timeout = 4000)
    public void testDoCloneWithParent() {
        TestNode parent = new TestNode("http://example.com", "parent");
        TestNode child = new TestNode("http://example.com", "child");
        parent.addChildren(child);

        Node clonedChild = child.doClone(parent);
        assertEquals(parent, clonedChild.parent());
        assertEquals(child.siblingIndex(), clonedChild.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        TestNode node = new TestNode(); // uses default constructor
        assertNull(node.attributes());
        assertTrue(node.childNodes().isEmpty());
        assertNull(node.baseUri());
    }
}