package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.parser.Tag;

import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Node
 *
 * 1. DEFECT TARGETING (Ground Truth: handlesAbsPrefix, handlesAbsPrefixOnHasAttr):
 *    - Node#hasAttr(String attributeKey): Fails to check 'abs:' prefix logic. If attributeKey starts with 'abs:',
 *      it must verify whether the relative attribute exists and produces a non-empty absUrl().
 *    - Node#attr(String attributeKey): Verifies resolution of 'abs:' attributes when absolute URL can or cannot be resolved.
 *
 * 2. BRANCH & CONDITION COVERAGE:
 *    - absUrl(String):
 *        * attributeKey empty / null -> exception
 *        * hasAttr(key) == false -> return ""
 *        * baseUri invalid URL, relUrl is absolute -> returns relUrl external form
 *        * baseUri invalid URL, relUrl is relative -> returns ""
 *        * relUrl starts with "?" -> checks query workaround (base.getPath() + relUrl)
 *        * relUrl is relative with valid base -> resolves absolute URL
 *        * relUrl is malformed relative -> catch MalformedURLException -> return ""
 *    - attr(String) / hasAttr(String) / removeAttr(String):
 *        * attributes.hasKey(key) == true
 *        * key starts with "abs:" (case-insensitive test: "abs:", "ABS:")
 *        * key absent -> returns "" / false
 *    - ownerDocument():
 *        * this instanceof Document -> return this
 *        * parentNode == null -> return null
 *        * parentNode != null -> return parentNode.ownerDocument()
 *    - sibling navigation (nextSibling, previousSibling, siblingIndex):
 *        * parentNode == null -> return null
 *        * siblingIndex == 0 -> previousSibling() == null
 *        * siblingIndex == size - 1 -> nextSibling() == null
 *        * intermediate sibling -> correctly retrieves next and previous
 *    - wrap(String):
 *        * empty html -> exception
 *        * wrapNode is not Element -> return null
 *        * wrap with single container, nested elements (getDeepChild loop)
 *        * wrap with multiple sibling elements (remainder loop)
 *    - replaceWith(Node), replaceChild(Node, Node), removeChild(Node):
 *        * out.parentNode != this -> exception
 *        * in.parentNode != null -> reparenting logic
 *    - clone() and doClone(Node):
 *        * orphan split: parentNode is null, siblingIndex is 0
 *        * deep copy: attributes cloned, childNodes recursively cloned with new parent
 *    - outerHtmlHead / outerHtmlTail / indent:
 *        * outerHtml traversal with Document owner vs fallback default OutputSettings
 *        * OuterHtmlVisitor#tail skips "#text" nodes
 *    - equals(Object) / hashCode():
 *        * this == o -> true; this != o -> false
 *        * parentNode null vs non-null, attributes null vs non-null
 */
public class NodeGeminiTest {

    // Helper concrete subclass of Node to test abstract and protected members directly
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
            return "concreteNode";
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            indent(accum, depth, out);
            accum.append("<concreteNode>");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</concreteNode>");
        }
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Ground Truth Defects)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlesAbsPrefixOnHasAttr() {
        Document doc = new Document("http://example.com/");
        Element a = doc.appendElement("a");
        a.attr("href", "/relative/path");

        // Ground truth defect: hasAttr("abs:href") failed on relative attributes when checking abs: prefix
        assertTrue("Element should recognize abs:href via hasAttr", a.hasAttr("abs:href"));
        assertFalse("Element should not recognize non-existent abs:nonexistent", a.hasAttr("abs:nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHandlesAbsPrefixOnAttr() {
        Document doc = new Document("http://example.com/sub/");
        Element a = doc.appendElement("a");
        a.attr("href", "path/index.html");

        assertEquals("http://example.com/sub/path/index.html", a.attr("abs:href"));
        assertEquals("http://example.com/sub/path/index.html", a.attr("ABS:HREF"));
        assertEquals("", a.attr("abs:nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHandlesAbsPrefixWhenBaseUriEmpty() {
        Document doc = new Document("");
        Element a = doc.appendElement("a");
        a.attr("href", "/relative");

        // Relative path cannot be made absolute without a base URI
        assertFalse("Relative url without valid baseUri should not satisfy hasAttr(abs:)", a.hasAttr("abs:href"));
        assertEquals("", a.attr("abs:href"));

        // But absolute URL on its own should succeed even with empty base URI
        a.attr("href", "http://jsoup.org/sample");
        assertTrue("Already absolute URL should satisfy hasAttr(abs:)", a.hasAttr("abs:href"));
        assertEquals("http://jsoup.org/sample", a.attr("abs:href"));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndInitialState() {
        ConcreteNode nodeWithBase = new ConcreteNode("  http://example.com  ");
        assertEquals("http://example.com", nodeWithBase.baseUri());
        assertNotNull(nodeWithBase.attributes());
        assertEquals(0, nodeWithBase.childNodes().size());

        Attributes attrs = new Attributes();
        attrs.put("key1", "val1");
        ConcreteNode nodeWithAttrs = new ConcreteNode("http://example.com", attrs);
        assertEquals("val1", nodeWithAttrs.attr("key1"));
        assertSame(attrs, nodeWithAttrs.attributes());

        ConcreteNode defaultNode = new ConcreteNode();
        assertNull(defaultNode.baseUri());
        assertNull(defaultNode.attributes());
        assertEquals(0, defaultNode.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testAttributeManipulation() {
        ConcreteNode node = new ConcreteNode("http://example.com", new Attributes());

        node.attr("data-test", "sampleValue");
        assertTrue(node.hasAttr("data-test"));
        assertEquals("sampleValue", node.attr("data-test"));

        // Replacing attribute value
        node.attr("data-test", "updatedValue");
        assertEquals("updatedValue", node.attr("data-test"));

        // Removing attribute
        Node returned = node.removeAttr("data-test");
        assertSame(node, returned);
        assertFalse(node.hasAttr("data-test"));
        assertEquals("", node.attr("data-test"));
    }

    @Test(timeout = 4000)
    public void testBaseUriUpdates() {
        ConcreteNode node = new ConcreteNode("http://example.com");
        assertEquals("http://example.com", node.baseUri());

        node.setBaseUri("http://example.org/dir");
        assertEquals("http://example.org/dir", node.baseUri());
    }

    @Test(timeout = 4000)
    public void testAbsUrlResolutionBranches() {
        Document doc = new Document("http://example.com/dir/file.html");
        Element link = doc.appendElement("a");

        // RelUrl query workaround branch (relUrl.startsWith("?"))
        link.attr("href", "?query=1#hash");
        assertEquals("http://example.com/dir/file.html?query=1#hash", link.absUrl("href"));

        // RelUrl standard relative path
        link.attr("href", "other.html");
        assertEquals("http://example.com/dir/other.html", link.absUrl("href"));

        // RelUrl starting with root slash
        link.attr("href", "/root.html");
        assertEquals("http://example.com/root.html", link.absUrl("href"));

        // Malformed base URI but absolute attribute URL branch
        link.setBaseUri("not-a-valid-uri");
        link.attr("href", "http://absolute.com/index.html");
        assertEquals("http://absolute.com/index.html", link.absUrl("href"));

        // Malformed base URI and relative attribute URL branch
        link.attr("href", "relative.html");
        assertEquals("", link.absUrl("href"));

        // Malformed attribute value that fails URL parsing
        link.setBaseUri("http://example.com");
        link.attr("href", "http://");
        assertEquals("", link.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testChildNodesAccessors() {
        Document doc = new Document("http://example.com");
        Element p1 = doc.appendElement("p");
        Element p2 = doc.appendElement("p");

        assertEquals(2, doc.childNodes().size());
        assertSame(p1, doc.childNode(0));
        assertSame(p2, doc.childNode(1));

        Node[] array = doc.childNodesAsArray();
        assertEquals(2, array.length);
        assertSame(p1, array[0]);
        assertSame(p2, array[1]);
    }

    @Test(timeout = 4000)
    public void testParentAndOwnerDocument() {
        Document doc = new Document("http://example.com");
        Element child = doc.appendElement("div");
        Element grandchild = child.appendElement("span");

        assertSame(doc, doc.ownerDocument());
        assertSame(doc, child.ownerDocument());
        assertSame(doc, grandchild.ownerDocument());

        assertSame(doc, child.parent());
        assertSame(child, grandchild.parent());

        ConcreteNode orphan = new ConcreteNode("http://example.com");
        assertNull(orphan.parent());
        assertNull(orphan.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testSiblingNavigation() {
        Document doc = new Document("http://example.com");
        Element e0 = doc.appendElement("div");
        Element e1 = doc.appendElement("span");
        Element e2 = doc.appendElement("p");

        assertEquals(0, e0.siblingIndex());
        assertEquals(1, e1.siblingIndex());
        assertEquals(2, e2.siblingIndex());

        assertNull(e0.previousSibling());
        assertSame(e1, e0.nextSibling());

        assertSame(e0, e1.previousSibling());
        assertSame(e2, e1.nextSibling());

        assertSame(e1, e2.previousSibling());
        assertNull(e2.nextSibling());

        List<Node> siblings = e1.siblingNodes();
        assertEquals(3, siblings.size());
        assertSame(e0, siblings.get(0));
        assertSame(e1, siblings.get(1));
        assertSame(e2, siblings.get(2));

        ConcreteNode orphan = new ConcreteNode();
        assertNull(orphan.nextSibling());
    }

    @Test(timeout = 4000)
    public void testDomTreeMutationsBeforeAfter() {
        Document doc = new Document("http://example.com");
        Element middle = doc.appendElement("div");

        Element beforeNode = new Element(Tag.valueOf("span"), "");
        middle.before(beforeNode);
        assertSame(beforeNode, doc.childNode(0));
        assertSame(middle, doc.childNode(1));

        Element afterNode = new Element(Tag.valueOf("p"), "");
        middle.after(afterNode);
        assertSame(afterNode, doc.childNode(2));

        // Test HTML insertion siblings
        middle.before("<b>bold</b>");
        assertEquals("span", doc.childNode(0).nodeName());
        assertEquals("b", doc.childNode(1).nodeName());
        assertSame(middle, doc.childNode(2));

        middle.after("<i>italic</i>");
        assertEquals("i", doc.childNode(3).nodeName());
        assertSame(afterNode, doc.childNode(4));
    }

    @Test(timeout = 4000)
    public void testWrapAndDeepWrap() {
        Document doc = new Document("http://example.com");
        Element target = doc.appendElement("em");
        target.attr("id", "target");

        // Deep wrapping with remainder siblings
        target.wrap("<div class='outer'><div class='inner'></div></div><span class='remainder'></span>");

        Element outer = (Element) doc.childNode(0);
        assertEquals("div", outer.nodeName());
        assertEquals("outer", outer.attr("class"));

        Element inner = outer.select("div.inner").first();
        assertNotNull(inner);
        assertSame(target, inner.childNode(0));

        // Verify remainder element was appended to the wrap container
        Element remainder = outer.select("span.remainder").first();
        assertNotNull(remainder);
        assertSame(outer, remainder.parent());
    }

    @Test(timeout = 4000)
    public void testWrapReturnsNullOnNonElement() {
        Document doc = new Document("http://example.com");
        Element target = doc.appendElement("span");
        Node wrapped = target.wrap("<!-- comment only -->");
        assertNull(wrapped);
    }

    @Test(timeout = 4000)
    public void testReplaceWithAndRemove() {
        Document doc = new Document("http://example.com");
        Element first = doc.appendElement("div");
        Element second = doc.appendElement("span");

        Element replacement = new Element(Tag.valueOf("p"), "");
        first.replaceWith(replacement);

        assertSame(replacement, doc.childNode(0));
        assertNull(first.parent());
        assertEquals(0, replacement.siblingIndex());
        assertEquals(1, second.siblingIndex());

        // Remove node
        second.remove();
        assertNull(second.parent());
        assertEquals(1, doc.childNodes().size());
    }

    @Test(timeout = 4000)
    public void testReparentingChildren() {
        Document doc1 = new Document("http://example.com");
        Element e1 = doc1.appendElement("div");
        Element child = e1.appendElement("span");

        Document doc2 = new Document("http://example.com");
        Element e2 = doc2.appendElement("section");

        // Reparent child to another element using addChildren
        e2.addChildren(child);
        assertSame(e2, child.parent());
        assertEquals(0, e1.childNodes().size());
        assertEquals(1, e2.childNodes().size());
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndBoundaryAbsUrl() {
        Document doc = new Document("http://example.com");
        Element link = doc.appendElement("a");
        // Non-existent attribute key returns empty string
        assertEquals("", link.absUrl("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlAndToStringFormatting() {
        Document doc = new Document("http://example.com");
        Element div = doc.appendElement("div");
        div.appendElement("span").text("Content");

        String outer = div.outerHtml();
        assertTrue(outer.contains("<div>"));
        assertTrue(outer.contains("<span>Content</span>"));
        assertTrue(outer.contains("</div>"));
        assertEquals(outer, div.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithNoDocumentOutputSettingsFallback() {
        ConcreteNode node = new ConcreteNode("http://example.com", new Attributes());
        // Orphan node has no ownerDocument, triggering default OutputSettings fallback
        String html = node.outerHtml();
        assertEquals("<concreteNode></concreteNode>", html.trim());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlVisitorSkipsTextNodeTail() {
        Document doc = new Document("");
        Element p = doc.appendElement("p");
        p.text("Some text"); // Adds a TextNode ("#text") child
        String html = p.outerHtml();
        assertTrue(html.contains("Some text"));
    }

    @Test(timeout = 4000)
    public void testUnmodifiableChildNodesList() {
        Document doc = new Document("http://example.com");
        doc.appendElement("p");
        List<Node> children = doc.childNodes();
        try {
            children.add(new ConcreteNode());
            fail("Expected UnsupportedOperationException when mutating childNodes unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullBaseUri() {
        new ConcreteNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullAttributes() {
        new ConcreteNode("http://example.com", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttrNullKey() {
        ConcreteNode node = new ConcreteNode("http://example.com", new Attributes());
        node.attr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testHasAttrNullKey() {
        ConcreteNode node = new ConcreteNode("http://example.com", new Attributes());
        node.hasAttr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveAttrNullKey() {
        ConcreteNode node = new ConcreteNode("http://example.com", new Attributes());
        node.removeAttr(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetBaseUriNull() {
        ConcreteNode node = new ConcreteNode("http://example.com", new Attributes());
        node.setBaseUri(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbsUrlNullOrEmptyKey() {
        ConcreteNode node = new ConcreteNode("http://example.com", new Attributes());
        node.absUrl("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveOrphanFails() {
        ConcreteNode node = new ConcreteNode("http://example.com");
        node.remove();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBeforeNullNodeFails() {
        Document doc = new Document("");
        Element el = doc.appendElement("p");
        el.before((Node) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBeforeOrphanFails() {
        ConcreteNode orphan = new ConcreteNode();
        orphan.before("<p></p>");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAfterNullNodeFails() {
        Document doc = new Document("");
        Element el = doc.appendElement("p");
        el.after((Node) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAfterOrphanFails() {
        ConcreteNode orphan = new ConcreteNode();
        orphan.after(new ConcreteNode());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWrapEmptyHtmlFails() {
        Document doc = new Document("");
        Element el = doc.appendElement("p");
        el.wrap("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceWithNullFails() {
        Document doc = new Document("");
        Element el = doc.appendElement("p");
        el.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceWithOrphanFails() {
        ConcreteNode orphan = new ConcreteNode();
        orphan.replaceWith(new ConcreteNode());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceChildForeignNodeFails() {
        Document doc = new Document("");
        Element p = doc.appendElement("p");
        Element foreign = new Element(Tag.valueOf("span"), "");
        p.replaceChild(foreign, new Element(Tag.valueOf("em"), ""));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveChildForeignNodeFails() {
        Document doc = new Document("");
        Element p = doc.appendElement("p");
        Element foreign = new Element(Tag.valueOf("span"), "");
        p.removeChild(foreign);
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY (equals, hashCode, clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        ConcreteNode n1 = new ConcreteNode("http://example.com");
        ConcreteNode n2 = new ConcreteNode("http://example.com");

        assertTrue("Reflexivity: node equals itself", n1.equals(n1));
        assertFalse("Non-nullity: node never equals null", n1.equals(null));
        assertFalse("Type safety: node never equals other object type", n1.equals("Some String"));
        assertFalse("Distinct nodes are not equal by reference", n1.equals(n2));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        Document doc = new Document("http://example.com");
        Element el = doc.appendElement("p");
        el.attr("key", "val");

        int hash1 = el.hashCode();
        int hash2 = el.hashCode();
        assertEquals("Deterministic hashCode", hash1, hash2);

        ConcreteNode defaultNode = new ConcreteNode();
        assertEquals(0, defaultNode.hashCode());
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        Document doc = new Document("http://example.com");
        Element parent = doc.appendElement("div");
        parent.attr("data-role", "container");
        Element child = parent.appendElement("span");
        child.attr("data-name", "item");

        Element clonedParent = (Element) parent.clone();

        assertNotSame(parent, clonedParent);
        assertNull("Cloned root must be an orphan", clonedParent.parent());
        assertEquals("Orphan clone siblingIndex must be 0", 0, clonedParent.siblingIndex());
        assertEquals("container", clonedParent.attr("data-role"));

        // Verify deep cloning of child nodes
        assertEquals(1, clonedParent.childNodes().size());
        Node clonedChild = clonedParent.childNode(0);
        assertNotSame(child, clonedChild);
        assertSame("Cloned child parent must be the cloned container", clonedParent, clonedChild.parent());
        assertEquals("item", clonedChild.attr("data-name"));

        // Verify modifications to clone do not mutate original
        clonedChild.attr("data-name", "modified");
        assertEquals("modified", clonedChild.attr("data-name"));
        assertEquals("item", child.attr("data-name"));
    }

    @Test(timeout = 4000)
    public void testIndentHelper() {
        ConcreteNode node = new ConcreteNode();
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(4);

        node.indent(sb, 2, settings);
        assertEquals("\n        ", sb.toString());
    }
}