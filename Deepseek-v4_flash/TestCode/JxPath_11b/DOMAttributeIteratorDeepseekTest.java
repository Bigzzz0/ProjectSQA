package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/* [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor with ELEMENT_NODE node and specific name (non-wildcard)
 *   - Constructor with ELEMENT_NODE node and wildcard "*" name
 *   - Constructor with non-ELEMENT node (e.g., TEXT_NODE, DOCUMENT_NODE)
 *   - getNodePointer() with position=0 (default) and position=1
 *   - getPosition() returns current position
 *   - setPosition() boundary conditions (0, 1, size, size+1)
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - null parent (should not happen in practice but test defensive behavior)
 *   - null QName (test defensive path)
 *   - Empty attributes map
 *   - Attributes with no namespace prefix
 *   - Attributes with "xmlns" prefix/special namespace handling
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - KNOWN DEFECT: Namespace handling for prefixed attributes
 *     When testPrefix != null and testNS resolves successfully, 
 *     getAttributeNodeNS() can fail to find the attribute due to parser limitations,
 *     triggering fallback to manual iteration through NamedNodeMap.
 *     However, the testAttr() method in the fallback path has a bug: it uses 'name' 
 *     (the field) instead of 'testName' (parameter) for testLocalName resolution,
 *     causing incorrect namespace matching. This leads to returning the wrong attribute
 *     (e.g., 10% vs 20%) or missing it entirely.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - null QName.getName() (defensive)
 *   - null prefix handling in equalStrings
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple calls to getNodePointer() with same position
 *   - setPosition() with invalid negative position
 *   - Iteration pattern: setPosition(1)..setPosition(n)
 */

public class DOMAttributeIteratorDeepseekTest {

    // Helper to create a simple XML document with namespaced attributes
    private Document createTestDocument(String namespaceURI, String prefix, String localName, String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        Element root = doc.createElementNS(namespaceURI, prefix + ":root");
        root.setAttributeNS(namespaceURI, prefix + ":" + localName, value);
        doc.appendChild(root);
        return doc;
    }

    // Helper to create a NodePointer for test element
    private NodePointer createElementPointer(Document doc) {
        return new DOMNodePointer(doc.getDocumentElement(), null);
    }

    // Helper to create a specific attribute pointer for parent
    private NodePointer createAttributeParentPointer(Document doc) {
        Element root = doc.getDocumentElement();
        return new DOMNodePointer(root, null);
    }

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorWithSpecificAttrName() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName("http://example.com", "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        assertEquals("Position should be 0 initially", 0, iter.getPosition());
        assertTrue("setPosition(1) should succeed", iter.setPosition(1));
        assertEquals("Position should be 1 after set", 1, iter.getPosition());
        
        NodePointer np = iter.getNodePointer();
        assertNotNull("NodePointer should not be null", np);
        assertEquals("Attribute value should be 10%", "10%", np.getValue());
    }

    @Test(timeout = 4000)
    public void testConstructorWithWildcardName() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        assertTrue("setPosition(1) should succeed", iter.setPosition(1));
        NodePointer np = iter.getNodePointer();
        assertNotNull("NodePointer should not be null", np);
        assertEquals("Attribute value should be 10%", "10%", np.getValue());
        assertFalse("setPosition(2) should fail (only one attribute)", iter.setPosition(2));
    }

    @Test(timeout = 4000)
    public void testConstructorWithNonElementNode() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Text textNode = doc.createTextNode("test");
        NodePointer parent = new DOMNodePointer(textNode, null);
        QName qname = new QName("test");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        assertFalse("setPosition(1) should fail for non-element node", iter.setPosition(1));
        assertNull("getNodePointer() should return null", iter.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWithDefaultPosition() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName("http://example.com", "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        // getNodePointer() with position=0 should trigger setPosition(1)
        NodePointer np = iter.getNodePointer();
        assertNotNull("getNodePointer() should return first item", np);
        assertEquals("value should be 10%", "10%", np.getValue());
        assertEquals("Position should be 0 after getNodePointer() call", 0, iter.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetPositionAfterSetPosition() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName("http://example.com", "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        assertEquals(0, iter.getPosition());
        iter.setPosition(1);
        assertEquals(1, iter.getPosition());
        iter.setPosition(0);
        assertEquals(0, iter.getPosition());
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testSetPositionBoundaryValues() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName("http://example.com", "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        // Boundary: position = 1 (valid)
        assertTrue(iter.setPosition(1));
        // Boundary: position = 0 (invalid, returns false but sets position)
        assertFalse(iter.setPosition(0));
        assertEquals(0, iter.getPosition());
        // Boundary: position > size (invalid)
        assertFalse(iter.setPosition(2));
        assertEquals(2, iter.getPosition());
        // Boundary: position = size (valid)
        assertTrue(iter.setPosition(1));
        // Negative position
        assertFalse(iter.setPosition(-1));
        assertEquals(-1, iter.getPosition());
    }

    @Test(timeout = 4000)
    public void testMultipleAttributesWithNoNamespace() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("root");
        root.setAttribute("id", "1");
        root.setAttribute("type", "test");
        doc.appendChild(root);
        
        NodePointer parent = new DOMNodePointer(root, null);
        QName qname = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        assertTrue(iter.setPosition(1));
        assertEquals("1", ((DOMAttributePointer)iter.getNodePointer()).getValue());
        assertTrue(iter.setPosition(2));
        assertEquals("test", ((DOMAttributePointer)iter.getNodePointer()).getValue());
        assertFalse(iter.setPosition(3));
    }

    @Test(timeout = 4000)
    public void testXmlnsAttributeExclusion() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElementNS("http://example.com", "root");
        root.setAttribute("xmlns:special", "http://special.com");
        root.setAttribute("test", "value");
        doc.appendChild(root);
        
        NodePointer parent = new DOMNodePointer(root, null);
        QName qname = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        // The xmlns:special attribute should be excluded, only 'test' remains
        assertTrue(iter.setPosition(1));
        assertEquals("value", ((DOMAttributePointer)iter.getNodePointer()).getValue());
        assertFalse("Only one attribute should be collected", iter.setPosition(2));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // Directly targets the known defect: namespace attribute resolution failure
    @Test(timeout = 4000)
    public void testNamespaceMappingWithPrefixedAttribute() throws Exception {
        // Create document that simulates the test scenario:
        // vendor[1]/product[1]/rate:amount[1]/@rate:discount
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        Element vendor = doc.createElement("vendor");
        Element product = doc.createElement("product");
        Element amount = doc.createElementNS("http://rate.example.com", "rate:amount");
        amount.setAttributeNS("http://rate.example.com", "rate:discount", "10%");
        product.appendChild(amount);
        vendor.appendChild(product);
        doc.appendChild(vendor);
        
        // Navigate to the amount element
        Element amountElement = (Element) doc.getDocumentElement()
            .getChildNodes().item(0)  // vendor
            .getChildNodes().item(0)  // product
            .getChildNodes().item(0); // rate:amount
        
        NodePointer parent = new DOMNodePointer(amountElement, null);
        
        // This QName uses prefix "rate" which maps to namespace "http://rate.example.com"
        // The test fails if bug is present because testAttr() uses name (field) instead of testName (parameter)
        QName qname = new QName("http://rate.example.com", "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        assertTrue("Should find the attribute with namespace", iter.setPosition(1));
        NodePointer np = iter.getNodePointer();
        assertNotNull("NodePointer should not be null", np);
        assertEquals("Expected discount value to be 10%", "10%", np.getValue());
    }

    @Test(timeout = 4000)
    public void testNamespaceMappingFallbackPath() throws Exception {
        // This test triggers the fallback path in getAttribute() when
        // getAttributeNodeNS() fails (simulating parser limitation)
        // The bug is in testAttr() called from fallback loop using 'name' field instead of 'testName' parameter
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Create element with multiple namespace attributes to force fallback iteration
        Element root = doc.createElementNS("http://ns1.com", "ns1:root");
        root.setAttributeNS("http://ns1.com", "ns1:attr1", "value1");
        root.setAttributeNS("http://ns2.com", "ns2:attr2", "value2");
        doc.appendChild(root);
        
        NodePointer parent = new DOMNodePointer(root, null);
        
        // First, find attr1 from ns1
        QName qname1 = new QName("http://ns1.com", "attr1");
        DOMAttributeIterator iter1 = new DOMAttributeIterator(parent, qname1);
        assertTrue("Should find attr1", iter1.setPosition(1));
        assertEquals("value1", iter1.getNodePointer().getValue());
        
        // Second, find attr2 from ns2 (different namespace)
        QName qname2 = new QName("http://ns2.com", "attr2");
        DOMAttributeIterator iter2 = new DOMAttributeIterator(parent, qname2);
        assertTrue("Should find attr2", iter2.setPosition(1));
        assertEquals("value2", iter2.getNodePointer().getValue());
    }

    @Test(timeout = 4000)
    public void testNamespaceMappingWithPremixedBugTrigger() throws Exception {
        // More complex scenario where the bug manifests:
        // testAttr() uses 'name' field instead of 'testName' parameter when
        // checking testLocalName equals nodeLocalName
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        Element root = doc.createElementNS("http://test.com", "test:root");
        // Create two attributes with same local name but different namespaces
        root.setAttributeNS("http://nsA.com", "nsA:attr", "expectedValue");
        root.setAttributeNS("http://nsB.com", "nsB:attr", "wrongValue");
        doc.appendChild(root);
        
        NodePointer parent = new DOMNodePointer(root, null);
        
        // Looking for attr in nsA namespace
        QName qname = new QName("http://nsA.com", "attr");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        assertTrue("Should find the attribute in nsA namespace", iter.setPosition(1));
        NodePointer np = iter.getNodePointer();
        assertNotNull("NodePointer should not be null", np);
        // The bug can cause this to return 'wrongValue' instead of 'expectedValue'
        assertEquals("Expected attribute value from correct namespace", "expectedValue", np.getValue());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testNullQNameHandling() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName((String) null, "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        assertTrue("Should find attribute with null prefix", iter.setPosition(1));
        assertNotNull("NodePointer should not be null", iter.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testNullParentNode() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        NodePointer parent = new DOMNodePointer(doc, null) {
            @Override
            public Object getNode() {
                return null; // Simulate null node
            }
        };
        QName qname = new QName("test");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        assertFalse("setPosition(1) should fail with null node", iter.setPosition(1));
        assertNull("getNodePointer() should return null", iter.getNodePointer());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testMultipleGetNodePointerCalls() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName("http://example.com", "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        NodePointer np1 = iter.getNodePointer();
        iter.setPosition(1);
        NodePointer np2 = iter.getNodePointer();
        
        assertNotNull(np1);
        assertNotNull(np2);
        assertEquals("Both NodePointers should have same value", np1.getValue(), np2.getValue());
    }

    @Test(timeout = 4000)
    public void testFullIterationCycle() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("root");
        root.setAttribute("a", "1");
        root.setAttribute("b", "2");
        root.setAttribute("c", "3");
        doc.appendChild(root);
        
        NodePointer parent = new DOMNodePointer(root, null);
        QName qname = new QName("*");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        int count = 0;
        while (iter.setPosition(count + 1)) {
            NodePointer np = iter.getNodePointer();
            assertNotNull("NodePointer at position " + (count + 1), np);
            count++;
        }
        assertEquals("Should have 3 attributes", 3, count);
    }

    @Test(timeout = 4000)
    public void testGetNodePointerReturnsCorrectType() throws Exception {
        Document doc = createTestDocument("http://example.com", "rate", "discount", "10%");
        NodePointer parent = createAttributeParentPointer(doc);
        QName qname = new QName("http://example.com", "discount");
        DOMAttributeIterator iter = new DOMAttributeIterator(parent, qname);
        
        iter.setPosition(1);
        NodePointer np = iter.getNodePointer();
        assertTrue("NodePointer should be DOMAttributePointer", np instanceof DOMAttributePointer);
    }
}