package org.jsoup.nodes;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.jsoup.nodes.LeafNode
 *
 * Branch & State Analysis:
 * 1. hasAttributes():
 *    - Branch: value instanceof Attributes -> true
 *    - Branch: value not instanceof Attributes (String or null) -> false
 * 2. attributes() & ensureAttributes():
 *    - Branch: !hasAttributes() with coreValue != null -> puts (nodeName, coreValue) into new Attributes
 *    - Branch: !hasAttributes() with coreValue == null -> empty Attributes created, no put
 *    - Branch: hasAttributes() == true -> no-op in ensureAttributes(), casts value to Attributes
 * 3. coreValue() & coreValue(String):
 *    - coreValue() delegates to attr(nodeName())
 *    - coreValue(val) delegates to attr(nodeName(), val)
 * 4. attr(String key):
 *    - Guard: key == null -> throws IllegalArgumentException via Validate.notNull(key)
 *    - Branch: !hasAttributes() && key.equals(nodeName()) -> returns (String) value
 *    - Branch: !hasAttributes() && !key.equals(nodeName()) -> returns EmptyString ("")
 *    - Branch: hasAttributes() -> delegates to super.attr(key)
 * 5. attr(String key, String value):
 *    - Branch: !hasAttributes() && key.equals(nodeName()) -> updates this.value directly (scalar string)
 *    - Branch: !hasAttributes() && !key.equals(nodeName()) -> promotes to Attributes, super.attr(...)
 *    - Branch: hasAttributes() -> ensureAttributes(), super.attr(...)
 * 6. hasAttr(String key):
 *    - Promotes via ensureAttributes(), delegates to super.hasAttr(key)
 * 7. removeAttr(String key):
 *    - Promotes via ensureAttributes(), delegates to super.removeAttr(key)
 * 8. absUrl(String key):
 *    - Promotes via ensureAttributes(), delegates to super.absUrl(key)
 * 9. baseUri() & doSetBaseUri(String):
 *    - Branch: hasParent() == true -> parent().baseUri()
 *    - Branch: hasParent() == false -> ""
 *    - doSetBaseUri is a no-op
 * 10. childNodeSize():
 *    - Unconditionally returns 0
 * 11. Defects4J Known Defect (TextNodeTest::testLeadNodesHaveNoChildren):
 *    - Leaf nodes throwing UnsupportedOperationException when childNodes() is queried.
 *      Node.childNodes() delegates to ensureChildNodes(), which should safely represent empty children.
 * ====================================================================================================
 */
public class LeafNodeGeminiTest {

    private static class ConcreteLeafNode extends LeafNode {
        private final String name;

        ConcreteLeafNode(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) {
        }

        @Override
        void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {
        }
    }

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testCoreValueAndInitialState() {
        ConcreteLeafNode node = new ConcreteLeafNode("textNode", "sample text");
        assertFalse("Initial state should not hold Attributes object", node.hasAttributes());
        assertEquals("sample text", node.coreValue());
        assertEquals("sample text", node.attr("textNode"));

        node.coreValue("updated text");
        assertFalse("Setting core value matching nodeName should not inflate Attributes", node.hasAttributes());
        assertEquals("updated text", node.coreValue());
        assertEquals("updated text", node.attr("textNode"));
    }

    @Test(timeout = 4000)
    public void testAttributeExpansionOnAdditionalKey() {
        ConcreteLeafNode node = new ConcreteLeafNode("customLeaf", "coreVal");
        assertFalse(node.hasAttributes());

        // Setting a different attribute key triggers inflation to Attributes object
        node.attr("extraKey", "extraVal");
        assertTrue("Attributes should now be inflated", node.hasAttributes());
        assertEquals("coreVal", node.attr("customLeaf"));
        assertEquals("extraVal", node.attr("extraKey"));
        assertEquals("coreVal", node.coreValue());
    }

    @Test(timeout = 4000)
    public void testEnsureAttributesDirectAccess() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "init");
        Attributes attrs = node.attributes();

        assertNotNull("attributes() should return non-null Attributes instance", attrs);
        assertTrue("Node should now have attributes inflated", node.hasAttributes());
        assertEquals("init", attrs.get("leaf"));
        assertEquals(1, attrs.size());

        // Subsequent call returns the exact same Attributes container
        assertSame("Subsequent attributes() call must return existing instance", attrs, node.attributes());
    }

    @Test(timeout = 4000)
    public void testAttrQueryWithInflatedAttributes() {
        ConcreteLeafNode node = new ConcreteLeafNode("node", "val");
        node.attributes(); // force inflation

        assertEquals("val", node.attr("node"));
        assertEquals("", node.attr("nonExistent"));

        node.attr("node", "val2");
        assertEquals("val2", node.attr("node"));
    }

    @Test(timeout = 4000)
    public void testHasAttrPromotesAndChecks() {
        ConcreteLeafNode node = new ConcreteLeafNode("token", "val");
        assertFalse(node.hasAttributes());

        assertTrue("Should have attribute for its nodeName", node.hasAttr("token"));
        assertTrue("hasAttr must inflate Attributes", node.hasAttributes());
        assertFalse("Should not have attribute for unmatched key", node.hasAttr("other"));
    }

    @Test(timeout = 4000)
    public void testRemoveAttrPromotesAndRemoves() {
        ConcreteLeafNode node = new ConcreteLeafNode("removable", "active");
        assertFalse(node.hasAttributes());

        node.removeAttr("nonExistent");
        assertTrue("removeAttr must inflate Attributes", node.hasAttributes());
        assertEquals("active", node.coreValue());

        node.removeAttr("removable");
        assertFalse("Core attribute should be removed", node.hasAttr("removable"));
        assertEquals("", node.coreValue());
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testNullInitialCoreValueExpansion() {
        ConcreteLeafNode node = new ConcreteLeafNode("emptyNode", null);
        assertFalse(node.hasAttributes());
        assertNull("Core value initialized to null should be returned as null", node.attr("emptyNode"));

        Attributes attrs = node.attributes();
        assertNotNull(attrs);
        assertEquals("Attributes inflated from null value should not contain the nodeName key", 0, attrs.size());
        assertFalse(attrs.hasKey("emptyNode"));
    }

    @Test(timeout = 4000)
    public void testAttrUnmatchedKeyBeforeExpansionReturnsEmptyString() {
        ConcreteLeafNode node = new ConcreteLeafNode("myNode", "myVal");
        assertEquals("", node.attr("unmatched"));
        assertEquals("", node.attr(""));
    }

    @Test(timeout = 4000)
    public void testChildNodeSizeIsZero() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "val");
        assertEquals("childNodeSize must strictly return 0 for LeafNode", 0, node.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testBaseUriWithAndWithoutParent() {
        ConcreteLeafNode orphan = new ConcreteLeafNode("orphan", "val");
        assertEquals("Base URI of orphan node must be empty string", "", orphan.baseUri());

        // doSetBaseUri is a no-op on LeafNode
        orphan.doSetBaseUri("http://example.com/test");
        assertEquals("doSetBaseUri must be a no-op for LeafNode", "", orphan.baseUri());

        Element parent = new Element("div");
        parent.setBaseUri("http://example.com/parent");
        parent.appendChild(orphan);

        assertEquals("Base URI should inherit from parent", "http://example.com/parent", orphan.baseUri());
    }

    @Test(timeout = 4000)
    public void testAbsUrlResolution() {
        ConcreteLeafNode node = new ConcreteLeafNode("link", "val");
        Element parent = new Element("div");
        parent.setBaseUri("http://example.com/path/");
        parent.appendChild(node);

        node.attr("href", "sub/page.html");
        assertEquals("http://example.com/path/sub/page.html", node.absUrl("href"));
        assertEquals("", node.absUrl("nonExistent"));
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ================================================================================================

    /**
     * Targets known defect: org.jsoup.nodes.TextNodeTest::testLeadNodesHaveNoChildren
     * Leaf nodes must not throw UnsupportedOperationException when their child nodes are queried.
     */
    @Test(timeout = 4000)
    public void testLeadNodesHaveNoChildren() {
        TextNode text = new TextNode("Hello world");
        assertEquals(0, text.childNodeSize());
        List<Node> children = text.childNodes();
        assertNotNull("childNodes() on a LeafNode should never return null", children);
        assertTrue("childNodes() on a LeafNode must be empty", children.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConcreteLeafNodeChildNodesContract() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "content");
        assertEquals(0, node.childNodeSize());
        List<Node> children = node.childNodes();
        assertNotNull("childNodes() must return non-null list", children);
        assertEquals(0, children.size());
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttrNullKeyThrowsException() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "val");
        node.attr(null);
    }

    @Test(timeout = 4000)
    public void testAttrNullKeyAfterInflationThrowsException() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "val");
        node.attributes(); // inflate attributes
        try {
            node.attr(null);
            fail("Expected IllegalArgumentException on null key when attributes are inflated");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testHasAttrNullKeyThrowsException() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "val");
        node.hasAttr(null);
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Clone / Equivalence Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testCloningMaintainsValueState() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "data");
        ConcreteLeafNode cloned = (ConcreteLeafNode) node.clone();

        assertNotSame("Cloned instance must be distinct", node, cloned);
        assertEquals(node.nodeName(), cloned.nodeName());
        assertEquals(node.coreValue(), cloned.coreValue());
        assertFalse(cloned.hasAttributes());

        // Mutating clone should not mutate original
        cloned.attr("extra", "extraVal");
        assertTrue(cloned.hasAttributes());
        assertFalse(node.hasAttributes());
    }

    @Test(timeout = 4000)
    public void testCloningWithInflatedAttributes() {
        ConcreteLeafNode node = new ConcreteLeafNode("leaf", "data");
        node.attr("key1", "val1");
        assertTrue(node.hasAttributes());

        ConcreteLeafNode cloned = (ConcreteLeafNode) node.clone();
        assertTrue(cloned.hasAttributes());
        assertNotSame(node.attributes(), cloned.attributes());
        assertEquals("val1", cloned.attr("key1"));
        assertEquals("data", cloned.attr("leaf"));
    }
}