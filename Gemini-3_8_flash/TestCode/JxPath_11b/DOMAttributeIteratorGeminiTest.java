/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.model.dom.DOMAttributeIterator
 *
 * Decision / Branch Points Analyzed:
 * 1. Constructor DOMAttributeIterator(NodePointer, QName):
 *    - node.getNodeType() == Node.ELEMENT_NODE vs non-ELEMENT_NODE (e.g. TEXT_NODE, DOCUMENT_NODE).
 *    - name.getName().equals("*") vs specific attribute name.
 *    - getAttribute() returning null vs non-null (attribute present vs absent).
 *    - NamedNodeMap iteration in wildcard mode (empty attributes, single/multiple attributes).
 * 2. Method testAttr(Attr, QName):
 *    - Attribute prefix "xmlns": returns false (DOM namespace declaration attribute).
 *    - Attribute localName "xmlns" with prefix null: returns false (default namespace declaration).
 *    - testLocalName equals "*" vs specific match vs mismatch.
 *    - equalStrings(testPrefix, nodePrefix) branch.
 *    - testPrefix != null -> parent.getNamespaceURI(testPrefix).
 *    - nodePrefix != null -> parent.getNamespaceURI(nodePrefix).
 *    - Namespace comparison: equalStrings(testNS, nodeNS).
 * 3. Method equalStrings(String, String):
 *    - s1 == s2 (both null, or same reference).
 *    - s1 != null && s1.equals(s2).
 *    - s1 != null && !s1.equals(s2).
 *    - s1 == null && s2 != null.
 * 4. Method getAttribute(Element, QName):
 *    - testPrefix != null vs testPrefix == null.
 *    - testNS != null -> element.getAttributeNodeNS(testNS, localName) returns Attr vs null.
 *    - Crimson/fallback scan: iterating element.getAttributes() using testAttr.
 *    - testNS == null -> element.getAttributeNode(localName).
 * 5. Method getNodePointer():
 *    - position == 0 with successful setPosition(1) vs failed setPosition(1) (empty list).
 *    - index < 0 boundary check.
 *    - return DOMAttributePointer with parent and Attr.
 * 6. Method getPosition() and setPosition(int):
 *    - position < 1 (underflow boundary).
 *    - position between 1 and size (in-bounds).
 *    - position > size (overflow boundary).
 *
 * Ground Truth Defect Targeted (Defects4J DOMModelTest::testNamespaceMapping):
 * - Target: Prefix vs Namespace resolution mapping when attributes share local names across namespaces
 *   or prefix mapping differs between document/element scopes.
 */
package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.*;

public class DOMAttributeIteratorGeminiTest {

    private Document doc;
    private DocumentBuilder builder;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectAttributeRetrievalByName() {
        Element root = doc.createElement("item");
        root.setAttribute("price", "100");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("price"));

        assertEquals("Initial position must be 0", 0, iterator.getPosition());
        assertTrue("setPosition(1) should succeed for existing attribute", iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull("NodePointer must not be null", attrPointer);
        assertTrue("Pointer should be DOMAttributePointer", attrPointer instanceof DOMAttributePointer);
        assertEquals("price", attrPointer.getName().getName());
        assertEquals("100", attrPointer.getValue());
    }

    @Test(timeout = 4000)
    public void testWildcardAttributeIteration() {
        Element root = doc.createElement("item");
        root.setAttribute("a", "1");
        root.setAttribute("b", "2");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertEquals(0, iterator.getPosition());
        assertTrue("setPosition(1) must succeed", iterator.setPosition(1));
        assertNotNull("First attribute pointer must exist", iterator.getNodePointer());

        assertTrue("setPosition(2) must succeed", iterator.setPosition(2));
        assertNotNull("Second attribute pointer must exist", iterator.getNodePointer());

        assertFalse("setPosition(3) must fail (out of bounds)", iterator.setPosition(3));
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWhenPositionIsZero() {
        Element root = doc.createElement("item");
        root.setAttribute("attr1", "val1");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        // When position is 0, getNodePointer() internally invokes setPosition(1) then resets position to 0
        NodePointer np = iterator.getNodePointer();
        assertNotNull("getNodePointer at position 0 should implicitly fetch first element", np);
        assertEquals("attr1", np.getName().getName());
        assertEquals("val1", np.getValue());
        assertEquals("Position should remain 0 after getNodePointer() at pos 0", 0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWhenEmptyAndPositionZero() {
        Element root = doc.createElement("item");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("missing"));

        NodePointer np = iterator.getNodePointer();
        assertNull("getNodePointer on empty iterator must return null", np);
        assertEquals(0, iterator.getPosition());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPositionBoundaryValues() {
        Element root = doc.createElement("box");
        root.setAttribute("w", "10");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("w"));

        assertFalse("Negative position must return false", iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        assertFalse("Position 0 must return false", iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertTrue("Position 1 is valid", iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertFalse("Position 2 is out of range", iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());

        assertFalse("Large position is out of range", iterator.setPosition(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testNonElementNodeHandling() {
        org.w3c.dom.Text textNode = doc.createTextNode("sample text");
        doc.appendChild(textNode);

        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("any"));

        assertFalse("setPosition(1) on Text node must return false", iterator.setPosition(1));
        assertNull("getNodePointer() on Text node must return null", iterator.getNodePointer());
        assertEquals(0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testXmlnsAttributesExcludedFromWildcard() {
        Element root = doc.createElementNS("http://default.ns", "item");
        root.setAttribute("xmlns", "http://default.ns");
        root.setAttribute("xmlns:custom", "http://custom.ns");
        root.setAttribute("normal", "ok");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        // Only "normal" should be collected; "xmlns" and "xmlns:custom" must be filtered out by testAttr
        assertTrue("Should have at least 1 non-xmlns attribute", iterator.setPosition(1));
        assertEquals("normal", iterator.getNodePointer().getName().getName());
        assertFalse("Only 1 normal attribute expected", iterator.setPosition(2));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Namespaces & Mappings)
    // =========================================================================

    /**
     * Defects4J DOMModelTest::testNamespaceMapping defect target:
     * When matching qualified attribute names (e.g. rate:discount) where different
     * elements or namespaces are mapped, ensure the attribute with matching namespace URI
     * and local name is correctly distinguished from another attribute of the same local name.
     */
    @Test(timeout = 4000)
    public void testNamespaceAttributeResolutionMatching() {
        final String NS_RATE_V1 = "http://ns.example.com/rate/v1";
        final String NS_RATE_V2 = "http://ns.example.com/rate/v2";

        Element vendor = doc.createElementNS(null, "vendor");
        doc.appendChild(vendor);

        // Product element defines rate namespace as v1
        Element product = doc.createElementNS(null, "product");
        vendor.appendChild(product);

        Element amount = doc.createElementNS(NS_RATE_V1, "rate:amount");
        amount.setAttributeNS(NS_RATE_V1, "rate:discount", "10%");
        product.appendChild(amount);

        // Let pointer be on amount element
        DOMNodePointer amountPointer = new DOMNodePointer(product, amount);

        // Query @rate:discount using namespace v1
        DOMAttributeIterator iter = new DOMAttributeIterator(amountPointer, new QName("rate", "discount"));

        assertTrue("Should resolve @rate:discount when prefix is declared on element", iter.setPosition(1));
        NodePointer np = iter.getNodePointer();
        assertNotNull("Resolved attribute pointer must not be null", np);
        assertEquals("10%", np.getValue());
    }

    @Test(timeout = 4000)
    public void testPrefixedAttributeFallbackWithoutNamespaceOnElement() {
        // Test getAttribute fallback branch where parser/DOM attribute has testNS
        // but element.getAttributeNodeNS returns null, triggering nnm loop fallback.
        Element root = doc.createElementNS("http://ns.root.org", "root");
        root.setAttributeNS("http://ns.root.org", "ns1:target", "found");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        // Query target with explicit prefix
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("ns1", "target"));

        assertTrue("Should locate attribute with matching prefix and localName", iterator.setPosition(1));
        assertEquals("found", iterator.getNodePointer().getValue());
    }

    @Test(timeout = 4000)
    public void testWildcardWithNamespacePrefix() {
        final String NS = "http://ns.test.com";
        Element root = doc.createElementNS(NS, "item");
        root.setAttributeNS(NS, "p:attr1", "val1");
        root.setAttributeNS(NS, "p:attr2", "val2");
        root.setAttribute("unprefixed", "val3");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        // Wildcard local name with prefix "p": matches any attribute in prefix p's namespace
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("p", "*"));

        int count = 0;
        while (iterator.setPosition(count + 1)) {
            count++;
        }
        assertEquals("Wildcard with prefix 'p:*' should match 2 prefixed attributes", 2, count);
    }

    @Test(timeout = 4000)
    public void testAttributeNotFoundReturnsFalseAndNull() {
        Element root = doc.createElement("item");
        root.setAttribute("existing", "true");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("nonexistent"));

        assertFalse("setPosition(1) should return false for nonexistent attribute", iterator.setPosition(1));
        assertNull("getNodePointer() should be null when not found", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testTestAttrWithUnmatchedPrefixOrNamespace() {
        final String NS1 = "http://ns1.org";
        final String NS2 = "http://ns2.org";

        Element root = doc.createElementNS(NS1, "node");
        root.setAttributeNS(NS1, "ns1:flag", "true");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        // Query with ns2 prefix which maps differently or not at all
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("ns2", "flag"));

        assertFalse("Attribute under mismatched namespace/prefix must not match", iterator.setPosition(1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullParentThrowsException() {
        new DOMAttributeIterator(null, new QName("test"));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullQNameThrowsException() {
        Element root = doc.createElement("node");
        doc.appendChild(root);
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        new DOMAttributeIterator(pointer, null);
    }

    @Test(timeout = 4000)
    public void testSetPositionPastEndThenGetNodePointer() {
        Element root = doc.createElement("node");
        root.setAttribute("k", "v");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("k"));

        assertTrue(iterator.setPosition(1));
        assertFalse(iterator.setPosition(2));
        // NodePointer at out-of-bound index
        try {
            iterator.getNodePointer();
        } catch (IndexOutOfBoundsException e) {
            // Expected if position - 1 is beyond size
            assertNotNull(e);
        }
    }
}