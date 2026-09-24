package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Node
 *
 * Defect Identification & Ground Truth:
 * 1. siblingNodes() includes 'this' node:
 *    - Javadoc specification: "Retrieves this node's sibling nodes... but does not include this node
 *      (a node is not a sibling of itself)."
 *    - Faulty implementation returns parent().childNodes(), which contains this node itself (size N instead of N-1).
 * 2. siblingNodes() on orphan nodes:
 *    - Javadoc specification: "If the node has no parent, returns an empty list."
 *    - Faulty implementation calls parent().childNodes(), throwing NullPointerException when parentNode == null.
 *
 * Branch & Coverage Matrix:
 * - Constructors: Node(String, Attributes), Node(String), Node()
 * - Attribute handling:
 *     attr(key): existing key, abs: prefix with valid/invalid URLs, non-existent key.
 *     hasAttr(key): exact match, abs: prefix condition (present vs missing, empty vs non-empty URL).
 *     removeAttr(key): removal, null guard.
 *     absUrl(key): baseUri valid/invalid, relative query '?', relative path, empty baseUri.
 * - Sibling Navigation & Mutation:
 *     siblingIndex(), setSiblingIndex(), nextSibling() (last, middle, orphan),
 *     previousSibling() (first, middle, orphan), siblingNodes().
 *     before(Node/String), after(Node/String), wrap(String), unwrap(), replaceWith(Node).
 * - DOM Hierarchy:
 *     parent(), setParentNode(), ownerDocument() (Document, descendant, orphan).
 *     addChildren(Node...), addChildren(int, Node...), reparentChild, reindexChildren.
 *     removeChild, replaceChild, childNode(int), childNodes(), childNodesAsArray().
 * - Traversal & Serialization:
 *     traverse(NodeVisitor), outerHtml(), toString(), indent().
 * - Object Contracts:
 *     equals(Object), hashCode(), clone() (deep copy, orphan status).
 */
public class NodeGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defect: siblingNodes() must not include the node itself.
     * With 3 sibling children, siblingNodes() should contain 2 nodes.
     * Defective version returns 3 nodes (all child nodes of the parent).
     */
    @Test(timeout = 4000)
    public void testNodeIsNotASiblingOfItself() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("p");
        Element child3 = parent.appendElement("p");

        List<Node> siblings = child1.siblingNodes();
        assertEquals("Node should not consider itself as a sibling", 2, siblings.size());
        assertFalse("Sibling list must not contain the target node", siblings.contains(child1));
        assertTrue(siblings.contains(child2));
        assertTrue(siblings.contains(child3));

        List<Node> midSiblings = child2.siblingNodes();
        assertEquals(2, midSiblings.size());
        assertFalse(midSiblings.contains(child2));
    }

    /**
     * Targets Defect: orphan node calling siblingNodes() should return an empty list,
     * not throw NullPointerException.
     */
    @Test(timeout = 4000)
    public void testOrphanNodeReturnsEmptyListForSiblingNodes() {
        Element orphan = new Element(Tag.valueOf("div"), "http://example.com");
        assertNull("Orphan should have null parent", orphan.parent());

        List<Node> siblings = orphan.siblingNodes();
        assertNotNull("Sibling list of orphan node must not be null", siblings);
        assertTrue("Sibling list of orphan node must be empty", siblings.isEmpty());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBaseUriAndSetBaseUriRecursive() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com/dir/");
        Element child = root.appendElement("a");
        Element grandChild = child.appendElement("span");

        assertEquals("http://example.com/dir/", root.baseUri());
        assertEquals("http://example.com/dir/", child.baseUri());
        assertEquals("http://example.com/dir/", grandChild.baseUri());

        root.setBaseUri("http://other.com/sub/");
        assertEquals("http://other.com/sub/", root.baseUri());
        assertEquals("http://other.com/sub/", child.baseUri());
        assertEquals("http://other.com/sub/", grandChild.baseUri());
    }

    @Test(timeout = 4000)
    public void testAttributesCrud() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "/index.html");
        el.attr("title", "Home");

        assertTrue(el.hasAttr("href"));
        assertTrue(el.hasAttr("title"));
        assertFalse(el.hasAttr("target"));

        assertEquals("/index.html", el.attr("href"));
        assertEquals("Home", el.attr("title"));
        assertEquals("", el.attr("nonexistent"));

        el.removeAttr("title");
        assertFalse(el.hasAttr("title"));
        assertEquals("", el.attr("title"));

        Attributes attrs = el.attributes();
        assertNotNull(attrs);
        assertTrue(attrs.hasKey("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlResolution() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com/path/index.html");
        el.attr("href", "sub/page.html");

        assertEquals("http://example.com/path/sub/page.html", el.absUrl("href"));
        assertEquals("http://example.com/path/sub/page.html", el.attr("abs:href"));
        assertTrue(el.hasAttr("abs:href"));

        // Query-relative URL resolution workaround check
        el.attr("href", "?query=1");
        assertEquals("http://example.com/path/index.html?query=1", el.absUrl("href"));
        assertEquals("http://example.com/path/index.html?query=1", el.attr("abs:href"));

        // Already absolute URL
        el.attr("href", "https://jsoup.org");
        assertEquals("https://jsoup.org", el.absUrl("href"));

        // Non-existent attribute
        assertEquals("", el.absUrl("nonexistent"));
        assertFalse(el.hasAttr("abs:nonexistent"));
        assertEquals("", el.attr("abs:nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlWith