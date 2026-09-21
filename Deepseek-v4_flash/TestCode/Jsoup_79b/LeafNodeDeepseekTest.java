package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: LeafNode (abstract) – tested via TextNode concrete subclass.
 *
 * Decision branches / boundary conditions covered:
 * 1. hasAttributes() – value instanceof Attributes (true/false)
 * 2. ensureAttributes() – if not hasAttributes, creates Attributes and puts core value
 * 3. attr(String key) – if !hasAttributes && key.equals(nodeName()) return (String) value; else if !hasAttributes return EmptyString; else super.attr(key)
 * 4. attr(String key, String value) – if !hasAttributes && key.equals(nodeName()) set this.value; else ensureAttributes + super.attr
 * 5. hasAttr / removeAttr / absUrl – always call ensureAttributes then super
 * 6. coreValue() / coreValue(String) – delegate to attr(nodeName())
 * 7. childNodeSize() – always returns 0
 * 8. ensureChildNodes() – throws UnsupportedOperationException (DEFECT: should return empty list)
 * 9. baseUri() – delegates to parent if hasParent else ""
 * 10. doSetBaseUri – noop
 * 11. Boundary: null key, empty key, nodeName() key, non-nodeName key
 * 12. Defect-targeted: leaf nodes must have no children (childNodeSize=0, childNodes() returns empty list, ensureChildNodes does not throw)
 */
public class LeafNodeDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testCoreValueGetter() {
        TextNode tn = new TextNode("hello");
        assertEquals("hello", tn.coreValue());
    }

    @Test(timeout = 4000)
    public void testCoreValueSetter() {
        TextNode tn = new TextNode("old");
        tn.coreValue("new");
        assertEquals("new", tn.coreValue());
    }

    @Test(timeout = 4000)
    public void testAttrGetWithNodeNameKey() {
        TextNode tn = new TextNode("text");
        assertEquals("text", tn.attr("#text"));
    }

    @Test(timeout = 4000)
    public void testAttrGetWithNonNodeNameKey() {
        TextNode tn = new TextNode("text");
        assertEquals("", tn.attr("class"));
    }

    @Test(timeout = 4000)
    public void testAttrSetWithNodeNameKey() {
        TextNode tn = new TextNode("old");
        tn.attr("#text", "new");
        assertEquals("new", tn.coreValue());
    }

    @Test(timeout = 4000)
    public void testAttrSetWithNonNodeNameKey() {
        TextNode tn = new TextNode("text");
        tn.attr("class", "foo");
        assertTrue(tn.hasAttributes());
        assertEquals("foo", tn.attr("class"));
    }

    @Test(timeout = 4000)
    public void testHasAttributesInitiallyFalse() {
        TextNode tn = new TextNode("text");
        assertFalse(tn.hasAttributes());
    }

    @Test(timeout = 4000)
    public void testAttributesCreatesAttributes() {
        TextNode tn = new TextNode("text");
        Attributes attrs = tn.attributes();
        assertNotNull(attrs);
        assertTrue(tn.hasAttributes());
        assertEquals("text", attrs.get("#text"));
    }

    @Test(timeout = 4000)
    public void testChildNodeSize() {
        TextNode tn = new TextNode("text");
        assertEquals(0, tn.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testBaseUriNoParent() {
        TextNode tn = new TextNode("text");
        assertEquals("", tn.baseUri());
    }

    @Test(timeout = 4000)
    public void testBaseUriWithParent() {
        TextNode tn = new TextNode("text");
        Element parent = new Element("p");
        tn.setParentNode(parent);
        parent.baseUri("http://example.com");
        assertEquals("http://example.com", tn.baseUri());
    }

    // ---------- Partition B: Boundary Value Analysis & Extremes ----------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAttrGetNullKey() {
        TextNode tn = new TextNode("text");
        tn.attr(null);
    }

    @Test(timeout = 4000)
    public void testAttrGetEmptyKey() {
        TextNode tn = new TextNode("text");
        assertEquals("", tn.attr(""));
    }

    @Test(timeout = 4000)
    public void testAttrSetNullKey() {
        TextNode tn = new TextNode("text");
        try {
            tn.attr(null, "val");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAttrSetEmptyKey() {
        TextNode tn = new TextNode("text");
        tn.attr("", "val");
        assertTrue(tn.hasAttributes());
        assertEquals("val", tn.attr(""));
    }

    @Test(timeout = 4000)
    public void testCoreValueNull() {
        TextNode tn = new TextNode(null);
        assertNull(tn.coreValue());
    }

    @Test(timeout = 4000)
    public void testCoreValueEmptyString() {
        TextNode tn = new TextNode("");
        assertEquals("", tn.coreValue());
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------

    /**
     * Defect: LeafNode.ensureChildNodes() throws UnsupportedOperationException.
     * Correct behavior: leaf nodes have no children, so childNodes() should return an empty list.
     */
    @Test(timeout = 4000)
    public void testLeafNodesHaveNoChildren() {
        TextNode tn = new TextNode("leaf");
        // childNodes() calls ensureChildNodes() – should not throw and return empty list
        assertEquals(0, tn.childNodes().size());
        // Also verify childNodeSize returns 0
        assertEquals(0, tn.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testHasAttrAfterEnsureAttributes() {
        TextNode tn = new TextNode("text");
        tn.attr("class", "foo");
        assertTrue(tn.hasAttr("class"));
        assertFalse(tn.hasAttr("id"));
    }

    @Test(timeout = 4000)
    public void testRemoveAttr() {
        TextNode tn = new TextNode("text");
        tn.attr("class", "foo");
        tn.removeAttr("class");
        assertFalse(tn.hasAttr("class"));
    }

    @Test(timeout = 4000)
    public void testAbsUrl() {
        TextNode tn = new TextNode("text");
        tn.attr("href", "/path");
        // no base URI set, so absUrl returns empty string
        assertEquals("", tn.absUrl("href"));
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testEnsureChildNodesThrows() {
        // This test verifies the current (defective) behavior.
        // Once fixed, this test should be removed or changed to expect no exception.
        TextNode tn = new TextNode("text");
        tn.ensureChildNodes(); // protected method, but we can call via reflection? Actually it's protected.
        // We can't call directly from test because it's protected. Instead we rely on childNodes() which calls it.
        // So we already test that childNodes() should not throw. This test is for documentation.
        // We'll skip this test because ensureChildNodes is protected.
    }

    // Instead, we test that childNodes() does not throw (defect-revealing test above).

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testDoSetBaseUriNoop() {
        TextNode tn = new TextNode("text");
        tn.setBaseUri("http://example.com");
        assertEquals("", tn.baseUri()); // baseUri not stored in leaf node
    }

    @Test(timeout = 4000)
    public void testAttributesAfterMultipleCalls() {
        TextNode tn = new TextNode("text");
        Attributes attrs1 = tn.attributes();
        Attributes attrs2 = tn.attributes();
        assertSame(attrs1, attrs2); // same instance
    }

    @Test(timeout = 4000)
    public void testAttrAfterAttributesCreated() {
        TextNode tn = new TextNode("text");
        tn.attributes(); // forces creation of Attributes
        // now attr with nodeName key should still work
        assertEquals("text", tn.attr("#text"));
        // attr with non-nodeName key should return empty string (since not set)
        assertEquals("", tn.attr("class"));
    }
}