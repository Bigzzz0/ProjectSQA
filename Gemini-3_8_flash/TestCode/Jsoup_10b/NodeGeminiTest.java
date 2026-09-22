/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Node
 *
 * 1. Targeted Defects (Defects4J Ground Truth):
 *    - absUrl relative query resolution:
 *      When baseUri is "http://jsoup.org/path/file" and relative URL is "?foo",
 *      the resolution must yield "http://jsoup.org/path/file?foo", not "http://jsoup.org/path/?foo".
 *      Targeted by: absHandlesRelativeQuery()
 *
 * 2. Decision Branches & Conditions Analyzed:
 *    - Node(baseUri, attributes): baseUri null check, attributes null check, baseUri.trim().
 *    - Node(): default empty state, null attributes, empty child list.
 *    - attr(attributeKey):
 *        * key null validation
 *        * hasAttr is true -> attributes.get(attributeKey)
 *        * hasAttr is false, starts with case-insensitive "abs:" -> absUrl(...)
 *        * hasAttr is false, does not start with "abs:" -> ""
 *    - hasAttr(attributeKey): key null validation, attributes.hasKey(key)
 *    - removeAttr(attributeKey): key null validation, attributes.remove(key)
 *    - setBaseUri(baseUri): null validation, updates baseUri
 *    - absUrl(attributeKey):
 *        * key empty/null validation
 *        * hasAttr is false -> returns ""
 *        * hasAttr is true:
 *            - baseUri invalid, relUrl valid absolute URL -> returns relUrl.toExternalForm()
 *            - baseUri invalid, relUrl invalid -> MalformedURLException handled -> returns ""
 *            - baseUri valid, relUrl valid -> returns combined URL
 *            - baseUri valid with path file, relUrl is query string "?..." -> verifies proper query resolution
 *    - childNode(index), childNodes(), childNodesAsArray(): unmodifiable list checks, array conversion.
 *    - parent(): returns parentNode, null if detached.
 *    - ownerDocument():
 *        * this instanceof Document -> returns this
 *        * parentNode == null -> returns null
 *        * parentNode != null -> recurses to root
 *    - remove(): parentNode == null throws, parentNode != null removes from parent.
 *    - replaceWith(Node in):
 *        * in null validation
 *        * parentNode null validation
 *        * replaces child and preserves/updates sibling indices
 *    - setParentNode(Node parentNode): if existing parent, removes from it first.
 *    - replaceChild(out, in):
 *        * out.parentNode != this throws
 *        * in null throws
 *        * in has prior parent -> reparents
 *        * swaps out with in, resets out.parentNode to null
 *    - removeChild(out): out.parentNode != this throws, removes, reindexes children.
 *    - addChildren(children...): reparents, sets sibling indices.
 *    - addChildren(index, children...): noNullElements check, reverse insertion at index, reindexing.
 *    - reparentChild(child): removes from old parent, sets this as parent.
 *    - siblingNodes(): fetches parent.childNodes().
 *    - nextSibling(): parent == null -> null; siblingIndex+1 < size -> sibling; else null.
 *    - previousSibling(): siblingIndex > 0 -> previous; siblingIndex == 0 -> null.
 *    - siblingIndex(): returns index; setSiblingIndex() updates it.
 *    - outerHtml(), toString(), indent():
 *        * ownerDocument present vs absent (fallback default Document)
 *        * visitor traverses head and tail
 *        * nodeName() == "#text" skips outerHtmlTail
 *    - equals(Object): reference equality true, different instance false.
 *    - hashCode(): parentNode null vs non-null, attributes null vs non-null.
 *    - clone(): deep copy with orphan status (parent null, siblingIndex 0), child recursion.
 */

package org.jsoup.nodes;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeGeminiTest {

    // Concrete test implementation of abstract Node for white-box testing
    private static class TestNode extends Node {
        private String name;

        TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
            this.name = "test";
        }

        TestNode(String baseUri) {
            super(baseUri);
            this.name = "test";
        }

        TestNode() {
            super();
            this.name = "test";
        }

        TestNode(String name, String baseUri) {
            super(baseUri);
            this.name = name;
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

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect in absUrl where resolving a relative query URL "?foo"
     * against a base URI containing a path segment (e.g. "http://jsoup.org/path/file")
     * incorrectly drops the file component.
     */
    @Test(timeout = 4000)
    public void absHandlesRelativeQuery() {
        TestNode node = new TestNode("http://jsoup.org/path/file");
        node.attr("href", "?foo");

        String absUrl = node.absUrl("href");
        assertEquals("http://jsoup.org/path/file?foo", absUrl);

        // Also test via the attr("abs:href") shortcut
        String absViaAttr = node.attr("abs:href");
        assertEquals("http://jsoup.org/path/file?foo", absViaAttr);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndAttributeBasics() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "val1");
        TestNode node = new TestNode("  http://example.com/dir/  ", attrs);

        assertEquals("http://example.com/dir/", node.baseUri());
        assertSame(attrs, node.attributes());
        assertTrue(node.hasAttr("key1"));
        assertFalse(node.hasAttr("key2"));
        assertEquals("val1", node.attr("key1"));

        node.attr("key2", "val2");
        assertTrue(node.hasAttr("key2"));
        assertEquals("val2", node.attr("key2"));

        node.removeAttr("key1");
        assertFalse(node.hasAttr("key1"));
        assertEquals("", node.attr("key1"));

        node.setBaseUri("http://new.example.com");
        assertEquals("http://new.example.com", node.baseUri());
    }

    @Test(timeout = 4000)
    public void testAbsPrefixInAttr() {
        TestNode node = new TestNode("http://example.com/page.html");
        node.attr("href", "sub/link.html");

        // Lowercase abs:
        assertEquals("http://example.com/sub/link.html", node.attr("abs:href"));
        // Uppercase ABS:
        assertEquals("http://example.com/sub/link.html", node.attr("ABS:href"));
        // Non-existent key with abs:
        assertEquals("", node.attr("abs:nonexistent"));
        // Non-existent key without abs:
        assertEquals("", node.attr("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlEdgeCases() {
        TestNode node = new TestNode("http://example.com/index.html");

        // 1. Missing attribute
        assertEquals("", node.absUrl("nonexistent"));

        // 2. Already absolute URL
        node.attr("href", "http://other.com/path");
        assertEquals("http://other.com/path", node.absUrl("href"));

        // 3. Base URL is malformed, but relative URL is actually absolute
        TestNode invalidBaseNode = new TestNode("malformed-url");
        invalidBaseNode.attr("href", "http://valid.com/resource");
        assertEquals("http://valid.com/resource", invalidBaseNode.absUrl("href"));

        // 4. Base URL is malformed, and relative URL is also relative -> should catch exception and return ""
        invalidBaseNode.attr("href", "relative/path");
        assertEquals("", invalidBaseNode.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testChildAndSiblingManipulation() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child0 = new TestNode("c0", "http://example.com");
        TestNode child1 = new TestNode("c1", "http://example.com");
        TestNode child2 = new TestNode("c2", "http://example.com");

        parent.addChildren(child0, child1, child2);

        assertEquals(3, parent.childNodes().size());
        assertSame(child0, parent.childNode(0));
        assertSame(child1, parent.childNode(1));
        assertSame(child2, parent.childNode(2));

        assertEquals(Integer.valueOf(0), child0.siblingIndex());
        assertEquals(Integer.valueOf(1), child1.siblingIndex());
        assertEquals(Integer.valueOf(2), child2.siblingIndex());

        assertSame(parent, child0.parent());
        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());

        // Previous and Next Siblings
        assertNull(child0.previousSibling());
        assertSame(child1, child0.nextSibling());
        assertSame(child0, child1.previousSibling());
        assertSame(child2, child1.nextSibling());
        assertSame(child1, child2.previousSibling());
        assertNull(child2.nextSibling());

        // siblingNodes
        List<Node> siblings = child1.siblingNodes();
        assertEquals(3, siblings.size());
        assertSame(child0, siblings.get(0));
        assertSame(child1, siblings.get(1));
        assertSame(child2, siblings.get(2));

        // childNodesAsArray
        Node[] array = parent.childNodesAsArray();
        assertEquals(3, array.length);
        assertSame(child0, array[0]);
        assertSame(child1, array[1]);
        assertSame(child2, array[2]);
    }

    @Test(timeout = 4000)
    public void testAddChildrenAtIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child0 = new TestNode("c0", "http://example.com");
        TestNode child1 = new TestNode("c1", "http://example.com");
        parent.addChildren(child0, child1);

        TestNode insertA = new TestNode("insA", "http://example.com");
        TestNode insertB = new TestNode("insB", "http://example.com");

        // Insert at index 1: between child0 and child1
        parent.addChildren(1, insertA, insertB);

        assertEquals(4, parent.childNodes().size());
        assertSame(child0, parent.childNode(0));
        assertSame(insertA, parent.childNode(1));
        assertSame(insertB, parent.childNode(2));
        assertSame(child1, parent.childNode(3));

        assertEquals(Integer.valueOf(0), child0.siblingIndex());
        assertEquals(Integer.valueOf(1), insertA.siblingIndex());
        assertEquals(Integer.valueOf(2), insertB.siblingIndex());
        assertEquals(Integer.valueOf(3), child1.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testReparentChildWhenAdding() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("child", "http://example.com");

        parent1.addChildren(child);
        assertSame(parent1, child.parent());
        assertEquals(1, parent1.childNodes().size());

        // Adding child to parent2 should reparent and remove from parent1
        parent2.addChildren(child);
        assertSame(parent2, child.parent());
        assertEquals(0, parent1.childNodes().size());
        assertEquals(1, parent2.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testRemoveChildAndReindexing() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child0 = new TestNode("c0", "http://example.com");
        TestNode child1 = new TestNode("c1", "http://example.com");
        TestNode child2 = new TestNode("c2", "http://example.com");
        parent.addChildren(child0, child1, child2);

        // Remove middle child via child.remove()
        child1.remove();

        assertNull(child1.parent());
        assertEquals(2, parent.childNodes().size());
        assertSame(child0, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertEquals(Integer.valueOf(0), child0.siblingIndex());
        assertEquals(Integer.valueOf(1), child2.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testReplaceChildAndReplaceWith() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child0 = new TestNode("c0", "http://example.com");
        TestNode child1 = new TestNode("c1", "http://example.com");
        parent.addChildren(child0, child1);

        TestNode replacement1 = new TestNode("rep1", "http://example.com");
        // replaceWith
        child0.replaceWith(replacement1);

        assertNull(child0.parent());
        assertSame(parent, replacement1.parent());
        assertEquals(Integer.valueOf(0), replacement1.siblingIndex());
        assertSame(replacement1, parent.childNode(0));

        // replaceChild where replacement already had a parent
        TestNode otherParent = new TestNode("http://example.com");
        TestNode replacement2 = new TestNode("rep2", "http://example.com");
        otherParent.addChildren(replacement2);
        assertSame(otherParent, replacement2.parent());

        parent.replaceChild(child1, replacement2);
        assertNull(child1.parent());
        assertSame(parent, replacement2.parent());
        assertEquals(0, otherParent.childNodes().size());
        assertEquals(Integer.valueOf(1), replacement2.siblingIndex());
        assertSame(replacement2, parent.childNode(1));
    }

    @Test(timeout = 4000)
    public void testSetParentNodeRemovesFromOldParent() {
        TestNode p