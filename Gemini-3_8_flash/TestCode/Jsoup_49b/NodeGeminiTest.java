package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * TARGET CLASS: org.jsoup.nodes.Node
 * TARGET BENCHMARK: Defects4J - ElementTest::appendMustCorrectlyMoveChildrenInsideOneParentElement
 *
 * BRANCH & DEFECT COVERAGE MATRIX:
 * 1. DEFECT-TARGETED ZONE:
 *    - appendMustCorrectlyMoveChildrenInsideOneParentElement: Reparenting and inserting sibling nodes
 *      within the same parent node in reverse/forward order shifts indices and affects sibling removal.
 *    - Node.addChildren(int, Node...): Moving nodes that are already children of 'this' parent.
 *
 * 2. ATTRIBUTE SUBSYSTEM BRANCHES:
 *    - attr(key): attributes.hasKey(key) == true
 *    - attr(key): key.toLowerCase().startsWith("abs:") -> absUrl resolution
 *    - attr(key): key not found -> return ""
 *    - hasAttr(key): startsWith("abs:") && attributes.hasKey(key) && !absUrl(key).isEmpty()
 *    - hasAttr(key): startsWith("abs:") fallback to attributes.hasKey("abs:...")
 *    - hasAttr(key): standard attributes.hasKey(key)
 *    - removeAttr(key): key existence and removal chaining
 *
 * 3. URL RESOLUTION & BASE URI BRANCHES:
 *    - absUrl(key): !hasAttr(key) -> returns ""
 *    - absUrl(key): hasAttr(key) with absolute protocol (http://, https://) vs relative URL
 *    - setBaseUri(baseUri): depth-first traversal updating baseUri on this node and all descendants
 *
 * 4. DOM TREE & SIBLING NAVIGATION BRANCHES:
 *    - ownerDocument(): this instanceof Document -> return this
 *    - ownerDocument(): parentNode == null -> return null
 *    - ownerDocument(): parentNode != null -> recurse up to ownerDocument
 *    - nextSibling(): parentNode == null -> null; index within bounds -> sibling; index out of bounds -> null
 *    - previousSibling(): parentNode == null -> null; siblingIndex > 0 -> sibling; siblingIndex == 0 -> null
 *    - siblingNodes(): parentNode == null -> emptyList; parentNode != null -> list excluding self
 *
 * 5. MUTATION & MANIPULATION BRANCHES:
 *    - before(String) / after(String): parent() instanceof Element (context element) vs parent() is not Element
 *    - before(Node) / after(Node): orphan parentNode guard validation
 *    - wrap(html): wrapChildren is empty or wrapNode not Element -> returns null
 *    - wrap(html): deepest child traversal and remainder element adoption
 *    - unwrap(): childNodes.size() > 0 (returns first child) vs childNodes.size() == 0 (returns null)
 *    - replaceChild(out, in): in.parentNode != null (reparent from another parent)
 *    - removeChild(out): re-indexing siblings after removal
 *
 * 6. EQUALS, HASHCODE & CLONE CONTRACT INTEGRITY:
 *    - equals: this == o, o == null, class mismatch, null vs non-null childNodes and attributes
 *    - hashCode: null vs non-null childNodes and attributes calculation
 *    - clone(): deep BFS copy, parentNode reset to null, siblingIndex reset to 0, attributes cloned
 *
 * 7. DEFENSIVE GUARDS:
 *    - Validate.notNull and Validate.notEmpty preconditions on null/empty strings, nodes, and arrays.
 * ====================================================================================================
 */
public class NodeGeminiTest {

    private static class ConcreteNode extends Node {
        ConcreteNode() {
            super();
        }

        ConcreteNode(String baseUri) {
            super(baseUri);
        }

        ConcreteNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
        }

        @Override
        public String nodeName() {
            return "concrete";
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<concrete>");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</concrete>");
        }
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: ElementTest::appendMustCorrectlyMoveChildrenInsideOneParentElement
     * When moving multiple existing children within the same parent node, reparenting must
     * preserve all moving nodes and insert them in the designated position without losing nodes.
     */
    @Test(timeout = 4000)
    public void testAppendMustCorrectlyMoveChildrenInsideOneParentElement() {
        Document doc = new Document("");
        Element body = doc.appendElement("body");
        Element div1 = body.appendElement("div1");
        Element div2 = body.appendElement("div2");
        final Element div3 = body.appendElement("div3");
        div3.text("Check");
        final Element div4 = body.appendElement("div4");

        List<Element> toMove = new ArrayList<Element>();
        toMove.add(div4);
        toMove.add(div1);
        toMove.add(div2);

        body.insertChildren(-1, toMove);

        String result = doc.toString().replaceAll("\\s+", "");
        assertEquals("<body><div3>Check</div3><div4></div4><div1></div1><div2></div2></body>", result);
    }

    @Test(timeout = 4000)
    public void testAddChildrenMovingNodesWithinSameParent() {
        Element parent = new Element(Tag.valueOf("ul"), "");
        Element li1 = parent.appendElement("li").attr("id", "1");
        Element li2 = parent.appendElement("li").attr("id", "2");
        Element li3 = parent.appendElement("li").attr("id", "3");

        assertEquals(3, parent.childNodeSize());
        // Move li1 to the middle (index 1)
        parent.addChildren(1, li1);

        assertEquals(3, parent.childNodeSize());
        assertEquals("2", parent.childNode(0).attr("id"));
        assertEquals("1", parent.childNode(1).attr("id"));
        assertEquals("3", parent.childNode(2).attr("id"));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testAttributeGetSetRemoveAndHas() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "val1");
        ConcreteNode node = new ConcreteNode("http://example.com/", attrs);

        assertEquals("val1", node.attr("key1"));
        assertEquals("", node.attr("nonExistent"));
        assertTrue(node.hasAttr("key1"));
        assertFalse(node.hasAttr("nonExistent"));

        node.attr("key2", "val2");
        assertEquals("val2", node.attr("key2"));
        assertTrue(node.hasAttr("key2"));

        node.removeAttr("key1");
        assertFalse(node.hasAttr("key1"));
        assertEquals("", node.attr("key1"));

        assertSame(attrs, node.attributes());
    }

    @Test(timeout = 4000)
    public void testAbsUrlResolutionAndAbsPrefix() {
        ConcreteNode node = new ConcreteNode("http://example.com/path/index.html");
        node.attr("href", "sub/page.html");
        node.attr("absLink", "http://other.org/test");

        // absUrl method
        assertEquals("http://example.com/path/sub/page.html", node.absUrl("href"));
        assertEquals("http://other.org/test", node.absUrl("absLink"));
        assertEquals("", node.absUrl("missingKey"));

        // attr with "abs:" prefix (case-insensitive branch)
        assertEquals("http://example.com/path/sub/page.html", node.attr("abs:href"));
        assertEquals("http://example.com/path/sub/page.html", node.attr("ABS:href"));
        assertEquals("", node.attr("abs:missingKey"));

        // hasAttr with "abs:" prefix
        assertTrue(node.hasAttr("abs:href"));
        assertFalse(node.hasAttr("abs:missingKey"));

        // Node with empty baseUri: absUrl returns ""
        ConcreteNode noBaseNode = new ConcreteNode("");
        noBaseNode.attr("href", "sub/page.html");
        assertEquals("", noBaseNode.absUrl("href"));
        assertFalse(noBaseNode.hasAttr("abs:href"));

        // hasAttr literal "abs:" key present in attributes
        noBaseNode.attr("abs:custom", "literalValue");
        assertTrue(noBaseNode.hasAttr("abs:custom"));
    }

    @Test(timeout = 4000)
    public void testSetBaseUriRecursion() {
        Element root = new Element(Tag.valueOf("div"), "http://initial.com/");
        Element child = root.appendElement("span");
        Element grandchild = child.appendElement("a");

        assertEquals("http://initial.com/", root.baseUri());
        assertEquals("http://initial.com/", child.baseUri());
        assertEquals("http://initial.com/", grandchild.baseUri());

        root.setBaseUri("http://updated.com/");

        assertEquals("http://updated.com/", root.baseUri());
        assertEquals("http://updated.com/", child.baseUri());
        assertEquals("http://updated.com/", grandchild.baseUri());
    }

    @Test(timeout = 4000)
    public void testChildNodesAccessAndCopy() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("span");
        Element child2 = parent.appendElement("p");

        assertEquals(2, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));

        List<Node> unmodifiable = parent.childNodes();
        assertEquals(2, unmodifiable.size());
        try {
            unmodifiable.add(new Element(Tag.valueOf("b"), ""));
            fail("Expected UnsupportedOperationException on unmodifiable childNodes list");
        } catch (UnsupportedOperationException expected) {
            // Success
        }

        List<Node> copy = parent.childNodesCopy();
        assertEquals(2, copy.size());
        assertNotSame(unmodifiable, copy);
        assertNotSame(parent.childNode(0), copy.get(0));
        assertEquals(parent.childNode(0), copy.get(0));

        Node[] array = parent.childNodesAsArray();
        assertEquals(2, array.length);
        assertSame(child1, array[0]);
        assertSame(child2, array[1]);
    }

    @Test(timeout = 4000)
    public void testOwnerDocumentHierarchies() {
        Document doc = new Document("http://example.com/");
        assertSame(doc, doc.ownerDocument());

        Element html = doc.appendElement("html");
        Element body = html.appendElement("body");
        assertSame(doc, html.ownerDocument());
        assertSame(doc, body.ownerDocument());

        ConcreteNode orphan = new ConcreteNode("http://example.com/");
        assertNull(orphan.ownerDocument());

        ConcreteNode childOfOrphan = new ConcreteNode("http://example.com/");
        orphan.addChildren(childOfOrphan);
        assertNull(childOfOrphan.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testParentAndSiblingsNavigation() {
        ConcreteNode orphan = new ConcreteNode();
        assertNull(orphan.parent());
        assertNull(orphan.parentNode());
        assertNull(orphan.nextSibling());
        assertNull(orphan.previousSibling());
        assertEquals(0, orphan.siblingIndex());
        assertTrue(orphan.siblingNodes().isEmpty());

        Element parent = new Element(Tag.valueOf("div"), "");
        Element c0 = parent.appendElement("p");
        Element c1 = parent.appendElement("span");
        Element c2 = parent.appendElement("b");

        assertSame(parent, c0.parent());
        assertSame(parent, c0.parentNode());

        // First child (index 0)
        assertEquals(0, c0.siblingIndex());
        assertNull(c0.previousSibling());
        assertSame(c1, c0.nextSibling());
        List<Node> c0Siblings = c0.siblingNodes();
        assertEquals(2, c0Siblings.size());
        assertSame(c1, c0Siblings.get(0));
        assertSame(c2, c0Siblings.get(1));

        // Middle child (index 1)
        assertEquals(1, c1.siblingIndex());
        assertSame(c0, c1.previousSibling());
        assertSame(c2, c1.nextSibling());

        // Last child (index 2)
        assertEquals(2, c2.siblingIndex());
        assertSame(c1, c2.previousSibling());
        assertNull(c2.nextSibling());
    }

    @Test(timeout = 4000)
    public void testRemoveAndReplaceWith() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("span");
        Element child2 = parent.appendElement("b");

        child1.remove();
        assertNull(child1.parent());
        assertEquals(1, parent.childNodeSize());
        assertSame(child2, parent.childNode(0));
        assertEquals(0, child2.siblingIndex());

        Element replacement = new Element(Tag.valueOf("i"), "");
        child2.replaceWith(replacement);
        assertNull(child2.parent());
        assertSame(parent, replacement.parent());
        assertSame(replacement, parent.childNode(0));
        assertEquals(0, replacement.siblingIndex());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testBeforeAndAfterWithNodeAndHtml() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com/");
        Element mid = parent.appendElement("p");

        Element beforeNode = new Element(Tag.valueOf("span"), "");
        Element afterNode = new Element(Tag.valueOf("b"), "");

        mid.before(beforeNode);
        mid.after(afterNode);

        assertEquals(3, parent.childNodeSize());
        assertSame(beforeNode, parent.childNode(0));
        assertSame(mid, parent.childNode(1));
        assertSame(afterNode, parent.childNode(2));

        // Insert using HTML strings
        mid.before("<h1>Title</h1>");
        mid.after("<h2>Subtitle</h2>");

        assertEquals(5, parent.childNodeSize());
        assertEquals("span", parent.childNode(0).nodeName());
        assertEquals("h1", parent.childNode(1).nodeName());
        assertEquals("p", parent.childNode(2).nodeName());
        assertEquals("h2", parent.childNode(3).nodeName());
        assertEquals("b", parent.childNode(4).nodeName());
    }

    @Test(timeout = 4000)
    public void testAddSiblingHtmlWhenParentIsNotElement() {
        ConcreteNode nonElementParent = new ConcreteNode("http://example.com/");
        ConcreteNode child = new ConcreteNode("http://example.com/");
        nonElementParent.addChildren(child);

        // addSiblingHtml executes context = parent() instanceof Element ? (Element) parent() : null
        // Tests the false branch where context is null
        child.before("<p>InsertedBefore</p>");
        assertEquals(2, nonElementParent.childNodeSize());
        assertEquals("p", nonElementParent.childNode(0).nodeName());
        assertSame(child, nonElementParent.childNode(1));
    }

    @Test(timeout = 4000)
    public void testUnwrapWithChildren() {
        Element doc = new Element(Tag.valueOf("div"), "");
        Element p = doc.appendElement("p");
        Element span = p.appendElement("span");
        TextNode text = new TextNode("Two ", "");
        Element bold = new Element(Tag.valueOf("b"), "");
        bold.text("Three");
        span.appendChild(text);
        span.appendChild(bold);

        assertEquals(1, p.childNodeSize());
        Node firstChild = span.unwrap();

        assertSame(text, firstChild);
        assertEquals(2, p.childNodeSize());
        assertSame(text, p.childNode(0));
        assertSame(bold, p.childNode(1));
        assertNull(span.parent());
    }

    @Test(timeout = 4000)
    public void testUnwrapWithoutChildren() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element emptySpan = p.appendElement("span");

        Node firstChild = emptySpan.unwrap();
        assertNull(firstChild);
        assertEquals(0, p.childNodeSize());
        assertNull(emptySpan.parent());
    }

    @Test(timeout = 4000)
    public void testWrapInvalidNonElement() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child = root.appendElement("span");

        // Plain text fragment produces TextNodes, not an Element
        Node result = child.wrap("Just plain text");
        assertNull(result);
        assertSame(root, child.parent());
    }

    @Test(timeout = 4000)
    public void testWrapWithDeepElementHierarchy() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element target = root.appendElement("span");

        Node wrapped = target.wrap("<div id='outer'><div id='inner'></div></div>");
        assertSame(target, wrapped);
        assertEquals(1, root.childNodeSize());

        Element outer = (Element) root.childNode(0);
        assertEquals("outer", outer.id());
        Element inner = (Element) outer.childNode(0);
        assertEquals("inner", inner.id());
        assertSame(target, inner.childNode(0));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlAndToString() {
        ConcreteNode node = new ConcreteNode();
        assertEquals("<concrete></concrete>", node.outerHtml());
        assertEquals("<concrete></concrete>", node.toString());

        // TextNode tail bypass verification: nodeName "#text" avoids outerHtmlTail
        TextNode textNode = new TextNode("Hello & World", "http://example.com");
        assertEquals("Hello &amp; World", textNode.outerHtml());
    }

    @Test(timeout = 4000)
    public void testIndentMethod() {
        ConcreteNode node = new ConcreteNode();
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(2);

        node.indent(sb, 2, settings);
        assertEquals("\n    ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTraverseVisitor() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.appendElement("p").appendElement("span");

        final List<String> visited = new ArrayList<String>();
        root.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.add("head:" + node.nodeName() + ":" + depth);
            }

            public void tail(Node node, int depth) {
                visited.add("tail:" + node.nodeName() + ":" + depth);
            }
        });

        assertEquals(6, visited.size());
        assertEquals("head:div:0", visited.get(0));
        assertEquals("head:p:1", visited.get(1));
        assertEquals("head:span:2", visited.get(2));
        assertEquals("tail:span:2", visited.get(3));
        assertEquals("tail:p:1", visited.get(4));
        assertEquals("tail:div:0", visited.get(5));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullBaseUriThrows() {
        new ConcreteNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullAttributesThrows() {
        new ConcreteNode("http://example.com", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttrNullKeyThrows() {
        new ConcreteNode().attr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testHasAttrNullKeyThrows() {
        new ConcreteNode().hasAttr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveAttrNullKeyThrows() {
        new ConcreteNode().removeAttr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetBaseUriNullThrows() {
        new ConcreteNode().setBaseUri(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbsUrlNullKeyThrows() {
        new ConcreteNode().absUrl(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbsUrlEmptyKeyThrows() {
        new ConcreteNode().absUrl("");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testChildNodeOutOfBoundsThrows() {
        new ConcreteNode().childNode(0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveOrphanThrows() {
        new ConcreteNode().remove();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnwrapOrphanThrows() {
        new ConcreteNode().unwrap();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBeforeNodeNullThrows() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element child = p.appendElement("span");
        child.before((Node) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBeforeOrphanNodeThrows() {
        ConcreteNode orphan = new ConcreteNode();
        orphan.before(new ConcreteNode());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBeforeHtmlNullThrows() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element child = p.appendElement("span");
        child.before((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAfterNodeNullThrows() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element child = p.appendElement("span");
        child.after((Node) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAfterOrphanNodeThrows() {
        ConcreteNode orphan = new ConcreteNode();
        orphan.after(new ConcreteNode());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWrapEmptyHtmlThrows() {
        new ConcreteNode().wrap("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceWithNullThrows() {
        Element p = new Element(Tag.valueOf("p"), "");
        Element child = p.appendElement("span");
        child.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceWithOrphanThrows() {
        ConcreteNode orphan = new ConcreteNode();
        orphan.replaceWith(new ConcreteNode());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceChildForeignOutThrows() {
        ConcreteNode parent = new ConcreteNode();
        ConcreteNode foreign = new ConcreteNode();
        ConcreteNode in = new ConcreteNode();
        parent.replaceChild(foreign, in);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveChildForeignOutThrows() {
        ConcreteNode parent = new ConcreteNode();
        ConcreteNode foreign = new ConcreteNode();
        parent.removeChild(foreign);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTraverseNullVisitorThrows() {
        new ConcreteNode().traverse(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddChildrenWithNullElementThrows() {
        ConcreteNode parent = new ConcreteNode();
        parent.addChildren(0, new Node[]{null});
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY (Equals, HashCode, Clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeExhaustiveBranches() {
        ConcreteNode node1 = new ConcreteNode("http://a.com");
        ConcreteNode node2 = new ConcreteNode("http://a.com");

        // Identity and null checks
        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("A String"));

        // Content equals and hashCode
        assertTrue(node1.equals(node2));
        assertEquals(node1.hashCode(), node2.hashCode());

        // Attributes difference
        node1.attr("k", "v");
        assertFalse(node1.equals(node2));
        assertFalse(node2.equals(node1));
        node2.attr("k", "v");
        assertTrue(node1.equals(node2));
        assertEquals(node1.hashCode(), node2.hashCode());

        // Children difference
        Element c1 = new Element(Tag.valueOf("span"), "");
        node1.addChildren(c1);
        assertFalse(node1.equals(node2));

        Element c2 = new Element(Tag.valueOf("span"), "");
        node2.addChildren(c2);
        assertTrue(node1.equals(node2));
        assertEquals(node1.hashCode(), node2.hashCode());

        // Default constructor creates null attributes
        ConcreteNode nullAttrNode1 = new ConcreteNode();
        ConcreteNode nullAttrNode2 = new ConcreteNode();
        assertTrue(nullAttrNode1.equals(nullAttrNode2));
        assertEquals(nullAttrNode1.hashCode(), nullAttrNode2.hashCode());

        assertFalse(nullAttrNode1.equals(node1));
        assertFalse(node1.equals(nullAttrNode1));
    }

    @Test(timeout = 4000)
    public void testDeepCloneTreeIsolation() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com/");
        root.attr("rootAttr", "rootVal");
        Element child = root.appendElement("p").attr("childAttr", "childVal");
        child.appendElement("span").text("Original Text");

        Node cloneRoot = root.clone();

        assertNotSame(root, cloneRoot);
        assertEquals(root, cloneRoot);
        assertNull(cloneRoot.parent());
        assertEquals(0, cloneRoot.siblingIndex());

        // Ensure deep clone of children
        Node cloneChild = cloneRoot.childNode(0);
        assertNotSame(child, cloneChild);
        assertSame(cloneRoot, cloneChild.parent());
        assertEquals("childVal", cloneChild.attr("childAttr"));

        // Mutation on clone does not affect original
        cloneChild.attr("childAttr", "mutatedVal");
        assertEquals("childVal", child.attr("childAttr"));
        assertEquals("mutatedVal", cloneChild.attr("childAttr"));

        // Cloning node with null attributes
        ConcreteNode nullAttr = new ConcreteNode();
        Node nullAttrClone = nullAttr.clone();
        assertNotNull(nullAttrClone);
        assertNull(nullAttrClone.attributes());
    }

    @Test(timeout = 4000)
    public void testReparentChildWhenReplacingChild() {
        ConcreteNode parent1 = new ConcreteNode();
        ConcreteNode child = new ConcreteNode();
        parent1.addChildren(child);
        assertSame(parent1, child.parent());

        ConcreteNode parent2 = new ConcreteNode();
        ConcreteNode out = new ConcreteNode();
        parent2.addChildren(out);

        // Replacing 'out' in parent2 with 'child' from parent1
        parent2.replaceChild(out, child);
        assertSame(parent2, child.parent());
        assertNull(out.parent());
        assertEquals(0, parent1.childNodeSize());
        assertEquals(1, parent2.childNodeSize());
    }
}