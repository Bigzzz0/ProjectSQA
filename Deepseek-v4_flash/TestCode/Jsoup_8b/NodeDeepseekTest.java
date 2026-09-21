package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class NodeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: Node (abstract)
     * 
     * Branches & Conditions Analyzed:
     * 1. attr(String): hasAttr true/false, abs: prefix, empty string return
     * 2. absUrl(String): hasAttr false, baseUri valid/invalid, relUrl absolute/relative, MalformedURLException
     * 3. childNode(int): valid index, out-of-bounds (implicit)
     * 4. childNodes(): unmodifiable list, empty list
     * 5. parent(): null parent, non-null parent
     * 6. ownerDocument(): Document instance, null parent, recursive call
     * 7. remove(): null parent (NPE), valid parent
     * 8. replaceWith(): null in, null parent, valid replacement
     * 9. setParentNode(): null parent, existing parent (removeChild)
     * 10. replaceChild(): out.parentNode != this, in.parentNode != null, index handling
     * 11. removeChild(): out.parentNode != this, index removal, reindex
     * 12. addChildren(Node...): reparent, sibling index update
     * 13. addChildren(int, Node...): null elements, reverse order, reindex
     * 14. siblingNodes(): parent null (NPE), valid parent
     * 15. nextSibling(): parent null, last sibling, valid next
     * 16. previousSibling(): parent null (NPE), first sibling, valid previous
     * 17. siblingIndex()/setSiblingIndex(): get/set
     * 18. outerHtml(): StringBuilder accumulation
     * 19. equals(): identity check, always false for non-identical
     * 20. hashCode(): parentNode null/non-null, attributes null/non-null
     * 
     * Known Defect: parentlessToString -> NullPointerException
     * Root Cause: outerHtml() calls ownerDocument().outputSettings() which
     * returns null when node has no parent/document. The NPE occurs in
     * outerHtml(StringBuilder) when accessing outputSettings() on null.
     * 
     * Test Strategy: Create a node with no parent (using default constructor
     * or setting parent to null) and call outerHtml() to trigger the NPE.
     * The test asserts that outerHtml() returns valid HTML without throwing.
     */

    // Test helper to create a concrete Node subclass for testing
    private static class TestNode extends Node {
        TestNode() { super(); }
        TestNode(String baseUri) { super(baseUri); }
        TestNode(String baseUri, Attributes attributes) { super(baseUri, attributes); }
        
        @Override
        public String nodeName() { return "#test"; }
        
        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<test>");
        }
        
        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</test>");
        }
    }

    // ========== PARTITION A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testAttrGetExisting() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("value", node.attr("key"));
    }

    @Test(timeout = 4000)
    public void testAttrGetMissing() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.attr("missing"));
    }

    @Test(timeout = 4000)
    public void testAttrGetAbsPrefix() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/path");
        TestNode node = new TestNode("http://example.com/base", attrs);
        assertEquals("http://example.com/path", node.attr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testAttrSet() {
        TestNode node = new TestNode("http://example.com");
        Node result = node.attr("key", "value");
        assertSame(node, result);
        assertEquals("value", node.attr("key"));
    }

    @Test(timeout = 4000)
    public void testHasAttr() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode node = new TestNode("http://example.com", attrs);
        assertTrue(node.hasAttr("key"));
        assertFalse(node.hasAttr("missing"));
    }

    @Test(timeout = 4000)
    public void testRemoveAttr() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode node = new TestNode("http://example.com", attrs);
        Node result = node.removeAttr("key");
        assertSame(node, result);
        assertFalse(node.hasAttr("key"));
    }

    @Test(timeout = 4000)
    public void testBaseUri() {
        TestNode node = new TestNode("  http://example.com  ");
        assertEquals("http://example.com", node.baseUri());
    }

    @Test(timeout = 4000)
    public void testSetBaseUri() {
        TestNode node = new TestNode("http://old.com");
        node.setBaseUri("http://new.com");
        assertEquals("http://new.com", node.baseUri());
    }

    @Test(timeout = 4000)
    public void testChildNodeValidIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
    }

    @Test(timeout = 4000)
    public void testChildNodesUnmodifiable() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        List<Node> children = parent.childNodes();
        assertEquals(1, children.size());
        try {
            children.add(new TestNode("http://example.com"));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertSame(parent, child.parent());
        assertNull(parent.parent());
    }

    @Test(timeout = 4000)
    public void testOwnerDocumentNull() {
        TestNode node = new TestNode("http://example.com");
        assertNull(node.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testOwnerDocumentWithParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertNull(child.ownerDocument()); // no Document in hierarchy
    }

    @Test(timeout = 4000)
    public void testRemoveWithParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        child.remove();
        assertEquals(0, parent.childNodes().size());
        assertNull(child.parent());
    }

    @Test(timeout = 4000)
    public void testReplaceWith() {
        TestNode parent = new TestNode("http://example.com");
        TestNode oldChild = new TestNode("http://example.com");
        TestNode newChild = new TestNode("http://example.com");
        parent.addChildren(oldChild);
        oldChild.replaceWith(newChild);
        assertSame(newChild, parent.childNode(0));
        assertNull(oldChild.parent());
        assertSame(parent, newChild.parent());
    }

    @Test(timeout = 4000)
    public void testSetParentNode() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent1.addChildren(child);
        child.setParentNode(parent2);
        assertSame(parent2, child.parent());
        assertEquals(0, parent1.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testReplaceChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode oldChild = new TestNode("http://example.com");
        TestNode newChild = new TestNode("http://example.com");
        parent.addChildren(oldChild);
        parent.replaceChild(oldChild, newChild);
        assertSame(newChild, parent.childNode(0));
        assertNull(oldChild.parent());
        assertSame(parent, newChild.parent());
    }

    @Test(timeout = 4000)
    public void testRemoveChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        parent.removeChild(child);
        assertEquals(0, parent.childNodes().size());
        assertNull(child.parent());
    }

    @Test(timeout = 4000)
    public void testAddChildren() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertEquals(2, parent.childNodes().size());
        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child2.siblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testAddChildrenAtIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode child3 = new TestNode("http://example.com");
        parent.addChildren(child1, child3);
        parent.addChildren(1, child2);
        assertEquals(3, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertSame(child3, parent.childNode(2));
    }

    @Test(timeout = 4000)
    public void testSiblingNodes() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        List<Node> siblings = child1.siblingNodes();
        assertEquals(2, siblings.size());
        assertSame(child1, siblings.get(0));
        assertSame(child2, siblings.get(1));
    }

    @Test(timeout = 4000)
    public void testNextSibling() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertSame(child2, child1.nextSibling());
        assertNull(child2.nextSibling());
    }

    @Test(timeout = 4000)
    public void testPreviousSibling() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertNull(child1.previousSibling());
        assertSame(child1, child2.previousSibling());
    }

    @Test(timeout = 4000)
    public void testSiblingIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child2.siblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testSetSiblingIndex() {
        TestNode node = new TestNode("http://example.com");
        node.setSiblingIndex(5);
        assertEquals(5, node.siblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("<test></test>", node.outerHtml());
    }

    @Test(timeout = 4000)
    public void testToString() {
        TestNode node = new TestNode("http://example.com");
        assertEquals(node.outerHtml(), node.toString());
    }

    // ========== PARTITION B: Boundary Value Analysis (BVA) & Extremes ==========

    @Test(timeout = 4000)
    public void testAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.attr((String) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testHasAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.hasAttr(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.removeAttr(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetBaseUriNull() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.setBaseUri(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAbsUrlEmptyKey() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.absUrl("");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAbsUrlMissingAttribute() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.absUrl("missing"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlRelative() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/path");
        TestNode node = new TestNode("http://example.com/base", attrs);
        assertEquals("http://example.com/path", node.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlAbsolute() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://other.com/path");
        TestNode node = new TestNode("http://example.com/base", attrs);
        assertEquals("http://other.com/path", node.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlInvalidBase() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://other.com/path");
        TestNode node = new TestNode("invalid base", attrs);
        assertEquals("http://other.com/path", node.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlInvalidBoth() {
        Attributes attrs = new Attributes();
        attrs.put("href", "invalid");
        TestNode node = new TestNode("invalid base", attrs);
        assertEquals("", node.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testChildNodeOutOfBounds() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.childNode(0);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testChildNodesEmpty() {
        TestNode node = new TestNode("http://example.com");
        assertTrue(node.childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testNextSiblingNoParent() {
        TestNode node = new TestNode("http://example.com");
        assertNull(node.nextSibling());
    }

    @Test(timeout = 4000)
    public void testPreviousSiblingNoParent() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.previousSibling();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected - parentNode is null
        }
    }

    @Test(timeout = 4000)
    public void testSiblingNodesNoParent() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.siblingNodes();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected - parentNode is null
        }
    }

    @Test(timeout = 4000)
    public void testRemoveNoParent() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.remove();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReplaceWithNull() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.replaceWith(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReplaceWithNoParent() {
        TestNode node = new TestNode("http://example.com");
        TestNode replacement = new TestNode("http://example.com");
        try {
            node.replaceWith(replacement);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddChildrenNullElements() {
        TestNode parent = new TestNode("http://example.com");
        try {
            parent.addChildren(0, (Node) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddChildrenEmpty() {
        TestNode parent = new TestNode("http://example.com");
        parent.addChildren();
        assertTrue(parent.childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddChildrenAtIndexEmpty() {
        TestNode parent = new TestNode("http://example.com");
        parent.addChildren(0);
        assertTrue(parent.childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        TestNode node = new TestNode("http://example.com");
        assertTrue(node.equals(node));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentInstance() {
        TestNode node1 = new TestNode("http://example.com");
        TestNode node2 = new TestNode("http://example.com");
        assertFalse(node1.equals(node2));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        TestNode node = new TestNode("http://example.com");
        assertFalse(node.equals(null));
    }

    @Test(timeout = 4000)
    public void testHashCodeNoParentNoAttrs() {
        TestNode node = new TestNode();
        assertEquals(0, node.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithAttrs() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode node = new TestNode("http://example.com", attrs);
        int expected = 31 * 0 + attrs.hashCode();
        assertEquals(expected, node.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        int expected = 31 * parent.hashCode() + 0;
        assertEquals(expected, child.hashCode());
    }

    // ========== PARTITION C: Defect-Targeted Branch Zone ==========

    /**
     * Defect: parentlessToString -> NullPointerException
     * 
     * This test targets the known defect where calling outerHtml() on a
     * node without a parent or document causes a NullPointerException.
     * The root cause is that outerHtml(StringBuilder) calls
     * ownerDocument().outputSettings() which returns null when the node
     * has no parent or document.
     * 
     * The test creates a node with no parent (using the default constructor)
     * and calls outerHtml(). The expected behavior is that it returns
     * valid HTML without throwing an exception.
     */
    @Test(timeout = 4000)
    public void testParentlessToString() {
        TestNode node = new TestNode(); // no parent, no document
        String html = node.outerHtml();
        assertNotNull(html);
        assertEquals("<test></test>", html);
    }

    /**
     * Additional test for the same defect - using a node with baseUri
     * but no parent.
     */
    @Test(timeout = 4000)
    public void testParentlessToStringWithBaseUri() {
        TestNode node = new TestNode("http://example.com");
        String html = node.outerHtml();
        assertNotNull(html);
        assertEquals("<test></test>", html);
    }

    /**
     * Test that toString() also works on parentless nodes.
     */
    @Test(timeout = 4000)
    public void testParentlessToStringMethod() {
        TestNode node = new TestNode();
        String str = node.toString();
        assertNotNull(str);
        assertEquals("<test></test>", str);
    }

    // ========== PARTITION D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testReplaceChildWrongParent() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        TestNode replacement = new TestNode("http://example.com");
        parent1.addChildren(child);
        try {
            parent2.replaceChild(child, replacement);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReplaceChildNullIn() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        try {
            parent.replaceChild(child, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveChildWrongParent() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent1.addChildren(child);
        try {
            parent2.removeChild(child);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetParentNodeWithExistingParent() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent1.addChildren(child);
        child.setParentNode(parent2);
        assertSame(parent2, child.parent());
        assertEquals(0, parent1.childNodes().size());
        assertEquals(1, parent2.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testAddChildrenReparent() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent1.addChildren(child);
        parent2.addChildren(child);
        assertEquals(0, parent1.childNodes().size());
        assertEquals(1, parent2.childNodes().size());
        assertSame(parent2, child.parent());
    }

    @Test(timeout = 4000)
    public void testAddChildrenAtIndexReparent() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent1.addChildren(child);
        parent2.addChildren(0, child);
        assertEquals(0, parent1.childNodes().size());
        assertEquals(1, parent2.childNodes().size());
        assertSame(parent2, child.parent());
    }

    @Test(timeout = 4000)
    public void testReplaceChildWithParentedNode() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode oldChild = new TestNode("http://example.com");
        TestNode newChild = new TestNode("http://example.com");
        parent1.addChildren(oldChild);
        parent2.addChildren(newChild);
        parent1.replaceChild(oldChild, newChild);
        assertEquals(0, parent2.childNodes().size());
        assertSame(parent1, newChild.parent());
        assertNull(oldChild.parent());
    }

    // ========== PARTITION E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        TestNode node = new TestNode();
        assertNull(node.attributes());
        assertNull(node.baseUri());
        assertTrue(node.childNodes().isEmpty());
        assertNull(node.parent());
        assertEquals(0, node.siblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBaseUri() {
        TestNode node = new TestNode("http://example.com");
        assertNotNull(node.attributes());
        assertEquals("http://example.com", node.baseUri());
        assertTrue(node.childNodes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBaseUriAndAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode node = new TestNode("http://example.com", attrs);
        assertSame(attrs, node.attributes());
        assertEquals("http://example.com", node.baseUri());
    }

    @Test(timeout = 4000)
    public void testConstructorNullBaseUri() {
        try {
            new TestNode(null, new Attributes());
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorNullAttributes() {
        try {
            new TestNode("http://example.com", null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testChildNodesAsArray() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);
        Node[] array = parent.childNodesAsArray();
        assertEquals(2, array.length);
        assertSame(child1, array[0]);
        assertSame(child2, array[1]);
    }

    @Test(timeout = 4000)
    public void testReindexChildren() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode child3 = new TestNode("http://example.com");
        parent.addChildren(child1, child2, child3);
        // Remove middle child and verify reindexing
        parent.removeChild(child2);
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child3.siblingIndex().intValue());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithIndent() {
        TestNode node = new TestNode("http://example.com");
        // Test indent method indirectly through outerHtml
        String html = node.outerHtml();
        assertNotNull(html);
    }

    @Test(timeout = 4000)
    public void testOwnerDocumentWithDocument() {
        // Create a Document and add a node to it
        Document doc = new Document("http://example.com");
        TestNode node = new TestNode("http://example.com");
        doc.appendChild(node);
        assertSame(doc, node.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testOwnerDocumentWithNestedParent() {
        Document doc = new Document("http://example.com");
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        doc.appendChild(parent);
        parent.addChildren(child);
        assertSame(doc, child.ownerDocument());
    }
}