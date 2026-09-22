package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.nodes.Node
 * Known Defect (Defects4J): ElementTest::parentlessToString -> NullPointerException
 * Root Cause in Defective Code:
 *   Node.outerHtml(StringBuilder) calls `ownerDocument().outputSettings()`.
 *   When a Node is not a Document and has no parent attached to a Document, ownerDocument() returns null,
 *   causing a NullPointerException during outerHtml() / toString().
 *
 * Decision / Condition Coverage Targets:
 * 1. Node(baseUri, attributes): baseUri null check, attributes null check, baseUri.trim().
 * 2. Node(baseUri): delegates with empty Attributes.
 * 3. Node() default constructor: empty childNodes, null attributes.
 * 4. attr(attributeKey):
 *    - null attributeKey (defensive check).
 *    - hasAttr(attributeKey) == true -> returns attributes.get(attributeKey).
 *    - starts with "abs:" (case-insensitive e.g. "abs:", "ABS:") -> delegates to absUrl().
 *    - fallback -> returns empty string "".
 * 5. absUrl(attributeKey):
 *    - attributeKey empty or null -> Validate.notEmpty throws IllegalArgumentException.
 *    - !hasAttr(attributeKey) -> returns "".
 *    - base URI valid URL + relative attribute -> absolute URL.
 *    - base URI valid URL + absolute attribute -> external form.
 *    - base URI invalid URL + absolute attribute -> fallback to abs URL.
 *    - base URI invalid URL + relative attribute -> MalformedURLException handled, returns "".
 *    - base URI valid URL + malformed relative URL -> MalformedURLException handled, returns "".
 * 6. childNode(int) & childNodes():
 *    - returns child at index, IndexOutOfBoundsException on illegal index.
 *    - childNodes() is unmodifiable collection.
 * 7. childNodesAsArray(): returns array copy of child nodes.
 * 8. ownerDocument():
 *    - this instanceof Document -> returns this.
 *    - parentNode == null -> returns null.
 *    - recursive parent traversal until Document or null.
 * 9. remove():
 *    - parentNode == null -> IllegalArgumentException.
 *    - parentNode != null -> calls parentNode.removeChild(this).
 * 10. replaceWith(Node):
 *    - in == null -> IllegalArgumentException.
 *    - parentNode == null -> IllegalArgumentException.
 *    - in with existing parent -> in reparented correctly.
 * 11. setParentNode(Node):
 *    - removes from previous parent if already set, sets new parentNode.
 * 12. replaceChild(Node out, Node in):
 *    - out.parentNode != this -> IllegalArgumentException.
 *    - in == null -> IllegalArgumentException.
 *    - in with existing parent -> removes in from old parent.
 *    - replaces at out.siblingIndex(), clears out.parentNode.
 * 13. removeChild(Node out):
 *    - out.parentNode != this -> IllegalArgumentException.
 *    - removes child, triggers reindexChildren(), clears out.parentNode.
 * 14. addChildren(Node...): reparents children, appends to end, sets sibling indices.
 * 15. addChildren(int index, Node...):
 *    - null elements -> IllegalArgumentException.
 *    - inserts at specified index preserving relative order, reindexes all children.
 * 16. siblingNodes():
 *    - parentNode == null -> NullPointerException.
 *    - returns parent.childNodes().
 * 17. nextSibling() & previousSibling():
 *    - parentNode == null -> nextSibling returns null, previousSibling throws NPE.
 *    - boundary: first child (previousSibling == null), last child (nextSibling == null).
 *    - middle child -> returns correct adjacent sibling.
 * 18. equals(Object) & hashCode():
 *    - this == o -> true; other instances -> false.
 *    - hashCode with parentNode != null vs null, attributes != null vs null.
 * 19. OuterHtmlVisitor:
 *    - calls outerHtmlHead and outerHtmlTail.
 *    - branch: if nodeName is "#text", outerHtmlTail is skipped.
 * ====================================================================================================
 */
public class NodeGeminiTest {

    private static class TestNode extends Node {
        private String name;

        public TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
            this.name = "test";
        }

        public TestNode(String baseUri) {
            super(baseUri);
            this.name = "test";
        }

        public TestNode(String baseUri, String name) {
            super(baseUri);
            this.name = name;
        }

        public TestNode() {
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

    private static class TextTestNode extends Node {
        private boolean tailCalled = false;

        public TextTestNode(String baseUri) {
            super(baseUri);
        }

        @Override
        public String nodeName() {
            return "#text";
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("textcontent");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            tailCalled = true;
            accum.append("tailShouldNotBeCalled");
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeStateAndAttributeManipulation() {
        TestNode node = new TestNode("http://example.com/");
        assertEquals("http://example.com/", node.baseUri());
        assertEquals("test", node.nodeName());

        node.attr("key1", "val1");
        assertTrue(node.hasAttr("key1"));
        assertEquals("val1", node.attr("key1"));
        assertNotNull(node.attributes());
        assertEquals(1, node.attributes().size());

        node.setBaseUri("http://example.org/");
        assertEquals("http://example.org/", node.baseUri());

        Node returned = node.removeAttr("key1");
        assertSame(node, returned);
        assertFalse(node.hasAttr("key1"));
        assertEquals("", node.attr("key1"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlResolutionSuccess() {
        TestNode node = new TestNode("http://example.com/sub/index.html");
        node.attr("href", "page.html");
        node.attr("absHref", "http://other.com/other.html");

        assertEquals("http://example.com/sub/page.html", node.absUrl("href"));
        assertEquals("http://example.com/sub/page.html", node.attr("abs:href"));
        assertEquals("http://example.com/sub/page.html", node.attr("ABS:href"));
        assertEquals("http://other.com/other.html", node.absUrl("absHref"));
    }

    @Test(timeout = 4000)
    public void testAddAndTraverseChildren() {
        TestNode parent = new TestNode("http://example.com/");
        TestNode child1 = new TestNode("http://example.com/", "c1");
        TestNode child2 = new TestNode("http://example.com/", "c2");

        parent.addChildren(child1, child2);

        assertEquals(2, parent.childNodes().size());
        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertEquals(Integer.valueOf(0), child1.siblingIndex());
        assertEquals(Integer.valueOf(1), child2.siblingIndex());

        Node[] childArray = parent.childNodesAsArray();
        assertEquals(2, childArray.length);
        assertSame(child1, childArray[0]);
        assertSame(child2, childArray[1]);
    }

    @Test(timeout = 4000)
    public void testInsertChildrenAtIndexPreservesOrder() {
        TestNode parent = new TestNode("http://example.com/");
        TestNode c1 = new TestNode("http://example.com/", "c1");
        TestNode c2 = new TestNode("http://example.com/", "c2");
        TestNode c3 = new TestNode("http://example.com/", "c3");

        parent.addChildren(c3);
        parent.addChildren(0, c1, c2);

        assertEquals(3, parent.childNodes().size());
        assertSame(c1, parent.childNode(0));
        assertSame(c2, parent.childNode(1));
        assertSame(c3, parent.childNode(2));
        assertEquals(Integer.valueOf(0), c1.siblingIndex());
        assertEquals(Integer.valueOf(1), c2.siblingIndex());
        assertEquals(Integer.valueOf(2), c3.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testNextAndPreviousSiblings() {
        TestNode parent = new TestNode("http://example.com/");
        TestNode c1 = new TestNode("http://example.com/", "c1");
        TestNode c2 = new TestNode("http://example.com/", "c2");
        TestNode c3 = new TestNode("http://example.com/", "c3");
        parent.addChildren(c1, c2, c3);

        assertNull(c1.previousSibling());
        assertSame(c2, c1.nextSibling());

        assertSame(c1, c2.previousSibling());
        assertSame(c3, c2.nextSibling());

        assertSame(c2, c3.previousSibling());
        assertNull(c3.nextSibling());

        List<Node> siblings = c1.siblingNodes();
        assertEquals(3, siblings.size());
        assertSame(c1, siblings.get(0));
    }

    @Test(timeout = 4000)
    public void testReplaceChildAndReplaceWith() {
        TestNode parent = new TestNode("http://example.com/");
        TestNode c1 = new TestNode("http://example.com/", "c1");
        TestNode c2 = new TestNode("http://example.com/", "c2");
        parent.addChildren(c1);

        parent.replaceChild(c1, c2);
        assertNull(c1.parent());
        assertSame(parent, c2.parent());
        assertEquals(Integer.valueOf(0), c2.siblingIndex());
        assertSame(c2, parent.childNode(0));

        TestNode c3 = new TestNode("http://example.com/", "c3");
        c2.replaceWith(c3);
        assertNull(c2.parent());
        assertSame(parent, c3.parent());
        assertEquals(Integer.valueOf(0), c3.siblingIndex());
        assertSame(c3, parent.childNode(0));
    }

    @Test(timeout = 4000)
    public void testRemoveChildAndRemove() {
        TestNode parent = new TestNode("http://example.com/");
        TestNode c1 = new TestNode("http://example.com/", "c1");
        TestNode c2 = new TestNode("http://example.com/", "c2");
        parent.addChildren(c1, c2);

        parent.removeChild(c1);
        assertNull(c1.parent());
        assertEquals(1, parent.childNodes().size());
        assertSame(c2, parent.childNode(0));
        assertEquals(Integer.valueOf(0), c2.siblingIndex());

        c2.remove();
        assertNull(c2.parent());
        assertEquals(0, parent.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testReparentChildWhenAddedToNewParent() {
        TestNode p1 = new TestNode("http://example.com/");
        TestNode p2 = new TestNode("http://example.com/");
        TestNode c = new TestNode("http://example.com/");

        p1.addChildren(c);
        assertEquals(1, p1.childNodes().size());
        assertSame(p1, c.parent());

        p2.addChildren(c);
        assertEquals(0, p1.childNodes().size());
        assertEquals(1, p2.childNodes().size());
        assertSame(p2, c.parent());
    }

    @Test(timeout = 4000)
    public void testSetParentNodeReplacesOldParent() {
        TestNode p1 = new TestNode("http://example.com/");
        TestNode p2 = new TestNode("http://example.com/");
        TestNode c = new TestNode("http://example.com/");

        p1.addChildren(c);
        c.setParentNode(p2);

        assertEquals(0, p1.childNodes().size());
        assertSame(p2, c.parent());
    }

    @Test(timeout = 4000)
    public void testOwnerDocumentHierarchy() {
        Document doc = new Document("http://example.com/");
        assertSame(doc, doc.ownerDocument());

        TestNode child = new TestNode("http://example.com/");
        TestNode grandChild = new TestNode("http://example.com/");
        child.addChildren(grandChild);

        assertNull(child.ownerDocument());
        assertNull(grandChild.ownerDocument());

        doc.addChildren(child);
        assertSame(doc, child.ownerDocument());
        assertSame(doc, grandChild.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithDocumentOutputSettings() {
        Document doc = new Document("http://example.com/");
        TestNode node = new TestNode("http://example.com/");
        doc.addChildren(node);

        String html = node.outerHtml();
        assertEquals("<test></test>", html);

        StringBuilder sb = new StringBuilder();
        node.indent(sb, 2, doc.outputSettings());
        assertTrue(sb.toString().startsWith("\n"));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlVisitorTextNodeSkipsTail() {
        Document doc = new Document("http://example.com/");
        TextTestNode textNode = new TextTestNode("http://example.com/");
        doc.addChildren(textNode);

        String html = textNode.outerHtml();
        assertEquals("textcontent", html);
        assertFalse(textNode.tailCalled);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBaseUriTrimming() {
        TestNode node = new TestNode("   http://example.com/trimmed   ");
        assertEquals("http://example.com/trimmed", node.baseUri());
    }

    @Test(timeout = 4000)
    public void testAbsUrlWithInvalidBaseUriFallsBackToAttribute() {
        TestNode node = new TestNode("invalid-uri");
        node.attr("href", "http://valid.com/page");
        assertEquals("http://valid.com/page", node.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlWithInvalidBaseAndRelativeUrlReturnsEmpty() {
        TestNode node = new TestNode("invalid-uri");
        node.attr("href", "relative/page.html");
        assertEquals("", node.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlMissingAttributeReturnsEmpty() {
        TestNode node = new TestNode("http://example.com/");
        assertEquals("", node.absUrl("nonexistent"));
        assertEquals("", node.attr("abs:nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlMalformedTargetReturnsEmpty() {
        TestNode node = new TestNode("http://example.com/");
        node.attr("bad", "http://::malformed::");
        assertEquals("", node.absUrl("bad"));
    }

    @Test(timeout = 4000)
    public void testNextSiblingWhenNoParentReturnsNull() {
        TestNode orphan = new TestNode("http://example.com/");
        assertNull(orphan.nextSibling());
    }

    @Test(timeout = 4000)
    public void testDefaultConstructorState() {
        TestNode node = new TestNode();
        assertNull(node.baseUri());
        assertNull(node.attributes());
        assertNotNull(node.childNodes());
        assertEquals(0, node.childNodes().size());
        assertEquals(0, node.hashCode());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testParentlessToStringDefect() {
        // Targets known defect: Node.outerHtml() / toString() throws NullPointerException when ownerDocument() is null
        TestNode node = new TestNode("http://example.com/");
        String html = node.outerHtml();
        assertEquals("<test></test>", html);
    }

    @Test(timeout = 4000)
    public void testParentlessToStringMethodDefect() {
        // Targets known defect via direct toString invocation
        TestNode node = new TestNode("http://example.com/");
        String stringVal = node.toString();
        assertEquals("<test></test>", stringVal);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullBaseUriThrows() {
        new TestNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullAttributesThrows() {
        new TestNode("http://example.com/", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetBaseUriNullThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.setBaseUri(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttrNullKeyThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.attr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testHasAttrNullKeyThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.hasAttr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveAttrNullKeyThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.removeAttr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbsUrlNullKeyThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.absUrl(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbsUrlEmptyKeyThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.absUrl("");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testChildNodeInvalidIndexThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.childNode(0);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testChildNodesListIsUnmodifiable() {
        TestNode node = new TestNode("http://example.com/");
        node.childNodes().add(new TestNode("http://example.com/"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveWithoutParentThrows() {
        TestNode node = new TestNode("http://example.com/");
        node.remove();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceWithWithoutParentThrows() {
        TestNode node1 = new TestNode("http://example.com/");
        TestNode node2 = new TestNode("http://example.com/");
        node1.replaceWith(node2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceWithNullInThrows() {
        TestNode parent = new TestNode("http://example.com/");
        TestNode child = new TestNode("http://example.com/");
        parent.addChildren(child);
        child.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceChildWrongParentThrows() {
        TestNode parent1 = new TestNode("http://example.com/");
        TestNode parent2 = new TestNode("http://example.com/");
        TestNode c1 = new TestNode("http://example.com/");
        TestNode c2 = new TestNode("http://example.com/");
        parent1.addChildren(c1);

        parent2.replaceChild(c1, c2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceChildNullInThrows() {
        TestNode parent = new TestNode("http://example.com/");
        TestNode child = new TestNode("http://example.com/");
        parent.addChildren(child);
        parent.replaceChild(child, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveChildWrongParentThrows() {
        TestNode parent1 = new TestNode("http://example.com/");
        TestNode parent2 = new TestNode("http://example.com/");
        TestNode c1 = new TestNode("http://example.com/");
        parent1.addChildren(c1);

        parent2.removeChild(c1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddChildrenWithNullElementsThrows() {
        TestNode parent = new TestNode("http://example.com/");
        parent.addChildren(0, new Node[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSiblingNodesNullParentThrowsNPE() {
        TestNode orphan = new TestNode("http://example.com/");
        orphan.siblingNodes();
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPreviousSiblingNullParentThrowsNPE() {
        TestNode orphan = new TestNode("http://example.com/");
        orphan.previousSibling();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        TestNode node1 = new TestNode("http://example.com/");
        TestNode node2 = new TestNode("http://example.com/");

        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(node2));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("NotANode"));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        TestNode node1 = new TestNode("http://example.com/");
        int hash1 = node1.hashCode();
        assertEquals(hash1, node1.hashCode());

        TestNode parent = new TestNode("http://example.com/");
        parent.addChildren(node1);
        int hash2 = node1.hashCode();
        assertNotEquals(hash1, hash2);
    }
}