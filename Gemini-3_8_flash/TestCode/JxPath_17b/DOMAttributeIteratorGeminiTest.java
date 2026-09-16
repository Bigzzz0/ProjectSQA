package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.jxpath.ri.model.dom.DOMAttributeIterator
 * Known Defect (Defects4J):
 *   - DOMModelTest::testAxisAttribute / JDOMModelTest::testAxisAttribute
 *   - Evaluating <vendor/product/price:amount/@*> expected:<[10%, 20%]> but was:<[20%]>
 *   - Cause: When querying wildcard attributes (@*), namespaced attributes (e.g. price:discount)
 *     are erroneously discarded because testAttr compares testPrefix (null) against nodePrefix ("price")
 *     and equalStrings(null, nodeNS) yields false.
 *
 * Decision / Branch Coverage Targets:
 *   1. Constructor:
 *      - node.getNodeType() == Node.ELEMENT_NODE (True vs False for Text/Comment nodes)
 *      - !lname.equals("*") (True: single getAttribute vs False: wildcard attribute iteration)
 *      - attr != null check when adding to attributes list
 *   2. testAttr:
 *      - nodePrefix != null && nodePrefix.equals("xmlns") -> false (xmlns:foo definition)
 *      - nodePrefix == null && nodeLocalName.equals("xmlns") -> false (default xmlns definition)
 *      - testLocalName.equals("*") vs testLocalName.equals(nodeLocalName) vs no match
 *      - equalStrings(testPrefix, nodePrefix) (True branch vs False branch)
 *      - testPrefix != null (lookup testNS from parent) vs null
 *      - nodePrefix != null (lookup nodeNS from parent) vs null
 *      - equalStrings(testNS, nodeNS)
 *   3. getAttribute:
 *      - testPrefix != null:
 *          - testNS != null -> element.getAttributeNodeNS != null (Found)
 *          - testNS != null -> element.getAttributeNodeNS == null (Fallback loop over NamedNodeMap)
 *          - Fallback loop item matches testAttr vs no match found (returns null)
 *          - testNS == null -> fallback to element.getAttributeNode
 *      - testPrefix == null -> element.getAttributeNode
 *   4. getNodePointer:
 *      - position == 0:
 *          - !setPosition(1) -> returns null (empty attributes)
 *          - setPosition(1) -> resets position = 0, calculates index = 0, returns pointer
 *      - index < 0 (defensive fallback when position <= 0)
 *      - normal position >= 1
 *   5. setPosition:
 *      - position >= 1 && position <= attributes.size() (Lower & upper boundary checks)
 *      - negative, zero, middle, exact size, and size + 1
 */
public class DOMAttributeIteratorGeminiTest {

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().newDocument();
    }

    private Document createNonNamespaceAwareDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().newDocument();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleUnprefixedAttributeFound() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("item");
        elem.setAttribute("status", "active");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("status"));

        assertEquals("Initial position must be 0", 0, iterator.getPosition());
        assertTrue("setPosition(1) must succeed", iterator.setPosition(1));
        assertEquals("Position must be updated to 1", 1, iterator.getPosition());

        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull("NodePointer must not be null", attrPointer);
        assertTrue("NodePointer must be DOMAttributePointer", attrPointer instanceof DOMAttributePointer);
        assertEquals("status", attrPointer.getName().getName());
        assertEquals("active", attrPointer.getValue());

        assertFalse("setPosition(2) beyond count must return false", iterator.setPosition(2));
    }

    @Test(timeout = 4000)
    public void testSingleUnprefixedAttributeNotFound() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("item");
        elem.setAttribute("status", "active");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("missing"));

        assertEquals("Position should be 0", 0, iterator.getPosition());
        assertFalse("setPosition(1) should fail when attribute missing", iterator.setPosition(1));
        assertNull("getNodePointer() must return null when no attribute found", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWhenPositionIsZeroAutomaticallyProbesFirst() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("item");
        elem.setAttribute("code", "ABC");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("code"));

        // When position == 0, getNodePointer() checks setPosition(1), resets position to 0,
        // and returns the element at index 0
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull("getNodePointer() should retrieve first element even when position is 0", attrPointer);
        assertEquals("ABC", attrPointer.getValue());
        assertEquals("Position must remain 0 after getNodePointer() probe", 0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testWildcardUnprefixedAttributesIteration() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("book");
        elem.setAttribute("title", "JXPath In Depth");
        elem.setAttribute("edition", "2");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertTrue("setPosition(1) should succeed", iterator.setPosition(1));
        assertNotNull("Pointer at 1 should not be null", iterator.getNodePointer());

        assertTrue("setPosition(2) should succeed", iterator.setPosition(2));
        assertNotNull("Pointer at 2 should not be null", iterator.getNodePointer());

        assertFalse("setPosition(3) should fail", iterator.setPosition(3));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNonElementNodeParentHasNoAttributes() throws Exception {
        Document doc = createDocument();
        Text textNode = doc.createTextNode("Some sample text");
        doc.appendChild(textNode);

        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertEquals("Initial position is 0", 0, iterator.getPosition());
        assertFalse("Text node has no attributes, setPosition(1) must be false", iterator.setPosition(1));
        assertNull("getNodePointer() on non-element must return null", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testElementWithZeroAttributes() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("empty");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iteratorWildcard = new DOMAttributeIterator(pointer, new QName("*"));
        assertFalse("setPosition(1) on empty element must be false", iteratorWildcard.setPosition(1));
        assertNull("getNodePointer() on empty attributes must return null", iteratorWildcard.getNodePointer());

        DOMAttributeIterator iteratorSpecific = new DOMAttributeIterator(pointer, new QName("attr"));
        assertFalse("setPosition(1) on empty element must be false", iteratorSpecific.setPosition(1));
        assertNull("getNodePointer() on empty attributes must return null", iteratorSpecific.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testSetPositionBoundariesAndNegativeIndices() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("item");
        elem.setAttribute("k1", "v1");
        elem.setAttribute("k2", "v2");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertFalse("setPosition(0) is invalid", iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertFalse("setPosition(-1) is invalid", iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        // When position is negative, getNodePointer() evaluates index < 0 and resets index to 0
        NodePointer fallbackPointer = iterator.getNodePointer();
        assertNotNull("Fallback pointer for negative position must return element 0", fallbackPointer);

        assertFalse("setPosition(Integer.MIN_VALUE) is invalid", iterator.setPosition(Integer.MIN_VALUE));
        assertFalse("setPosition(3) is invalid for 2 attributes", iterator.setPosition(3));
        assertFalse("setPosition(Integer.MAX_VALUE) is invalid", iterator.setPosition(Integer.MAX_VALUE));

        assertTrue("setPosition(1) is valid", iterator.setPosition(1));
        assertTrue("setPosition(2) is valid", iterator.setPosition(2));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETS DEFECT: DOMModelTest::testAxisAttribute
     * Failure: Evaluating <vendor/product/price:amount/@*> expected:<[10%, 20%]> but was:<[20%]>
     * Root Cause in DOMAttributeIterator:
     * When evaluating wildcard '@*', testLocalName is "*", but testPrefix is null.
     * When the element contains both an unprefixed attribute and a prefixed/namespaced attribute,
     * testAttr tests `equalStrings(testPrefix, nodePrefix)` which is false, and then tests
     * `equalStrings(testNS, nodeNS)`. Since testNS is null and nodeNS is non-null, it returns false!
     * The namespaced attribute is skipped, returning only 1 attribute instead of 2.
     */
    @Test(timeout = 4000)
    public void testDefectAxisAttributeWildcardWithNamespacedAndUnnamespacedAttributes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://vendor", "vendor");
        doc.appendChild(root);

        Element amount = doc.createElementNS("http://price", "price:amount");
        amount.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:price", "http://price");
        amount.setAttribute("discount", "10%");
        amount.setAttributeNS("http://price", "price:discount", "20%");
        root.appendChild(amount);

        DOMNodePointer pointer = new DOMNodePointer(amount, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertTrue("First attribute must be present at position 1", iterator.setPosition(1));
        assertNotNull("First attribute pointer must exist", iterator.getNodePointer());

        // FAULT ASSERTION: On defective JXPath, position 2 returns false because price:discount is dropped
        assertTrue("Second attribute must be present at position 2 (wildcard @* must match namespaced attributes too)",
                iterator.setPosition(2));
        assertNotNull("Second attribute pointer must exist", iterator.getNodePointer());

        assertFalse("There are only two attributes, position 3 must be false", iterator.setPosition(3));
    }

    // =========================================================================
    // Partition D: Namespace Branches & Fallback Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testXmlnsAttributesAreExcludedFromWildcardIteration() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://default", "root");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://default");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:custom", "http://custom");
        elem.setAttribute("regular", "value");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        // Only "regular" should be returned; xmlns and xmlns:custom must be skipped by testAttr
        assertTrue("First regular attribute should be found", iterator.setPosition(1));
        assertEquals("regular", iterator.getNodePointer().getName().getName());
        assertFalse("xmlns attributes must not be included in wildcard iteration", iterator.setPosition(2));
    }

    @Test(timeout = 4000)
    public void testExactNamespacedAttributeLookup() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://example.com/ns", "ns:test");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com/ns");
        elem.setAttributeNS("http://example.com/ns", "ns:target", "hit");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("ns", "target"));

        assertTrue("Exact namespaced attribute should be located", iterator.setPosition(1));
        NodePointer attrPtr = iterator.getNodePointer();
        assertNotNull(attrPtr);
        assertEquals("hit", attrPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testNamespacedAttributeFallbackWhenAttributeNodeNSIsNull() throws Exception {
        // Construct non-namespace-aware document to trigger Crimson/JDK 1.4 parser fallback
        // where getAttributeNodeNS returns null but element attributes contains prefixed node
        Document doc = createNonNamespaceAwareDocument();
        Element elem = doc.createElement("root");
        elem.setAttribute("xmlns:p", "http://simulated.uri");
        elem.setAttribute("p:legacy", "found");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        // Explicitly register prefix on the namespace resolver
        pointer.getNamespaceResolver().registerNamespace("p", "http://simulated.uri");

        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("p", "legacy"));

        assertTrue("Fallback loop should find the attribute by matching testAttr", iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("found", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testNamespacedAttributeFallbackReturnsNullWhenNotFoundInMap() throws Exception {
        Document doc = createNonNamespaceAwareDocument();
        Element elem = doc.createElement("root");
        elem.setAttribute("p:other", "val");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        pointer.getNamespaceResolver().registerNamespace("p", "http://simulated.uri");

        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("p", "nonexistent"));

        assertFalse("Fallback loop should return null when attribute is missing", iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testPrefixSpecifiedButNamespaceUnresolvableFallsBackToLocalName() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("root");
        elem.setAttribute("orphan", "val");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        // "unknown" prefix is not bound in resolver -> testNS is null -> fallback to getAttributeNode("orphan")
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("unknown", "orphan"));

        assertTrue("Unresolvable prefix falls back to getAttributeNode matching local name", iterator.setPosition(1));
        assertEquals("val", iterator.getNodePointer().getValue());
    }

    @Test(timeout = 4000)
    public void testNamespacedAttributesWithDifferentPrefixesMatchingSameNamespaceURI() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://common.ns", "item");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p1", "http://common.ns");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p2", "http://common.ns");
        elem.setAttributeNS("http://common.ns", "p1:attr", "sameURI");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        // Query using prefix p2, while DOM attribute uses prefix p1, but both map to "http://common.ns"
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("p2", "attr"));

        assertTrue("Attributes matching the same namespace URI should match across different prefixes",
                iterator.setPosition(1));
        assertEquals("sameURI", iterator.getNodePointer().getValue());
    }

    @Test(timeout = 4000)
    public void testAttributeWithMismatchedNamespaceURIReturnsEmpty() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://ns1.com", "item");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p1", "http://ns1.com");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p2", "http://ns2.com");
        elem.setAttributeNS("http://ns1.com", "p1:attr", "val");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("p2", "attr"));

        assertFalse("Attribute with different namespace URI must not match", iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testIteratorContractAndNodePointerIntegrity() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("data");
        elem.setAttribute("id", "1001");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        NodeIterator iterator = new DOMAttributeIterator(pointer, new QName("id"));

        assertEquals(0, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        NodePointer nodePointer = iterator.getNodePointer();
        assertNotNull(nodePointer);
        assertTrue(nodePointer instanceof DOMAttributePointer);
        Attr attrNode = (Attr) nodePointer.getBaseValue();
        assertEquals("id", attrNode.getName());
        assertEquals("1001", attrNode.getValue());
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetNodePointerThrowsExceptionWhenPositionOutOfBounds() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("data");
        elem.setAttribute("id", "1001");
        doc.appendChild(elem);

        DOMNodePointer pointer = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("id"));

        // Force position out of bounds (allowed by setPosition implementation)
        assertFalse(iterator.setPosition(10));
        assertEquals(10, iterator.getPosition());

        // Attempting to get node pointer at position 10 must throw IndexOutOfBoundsException
        iterator.getNodePointer();
    }
}