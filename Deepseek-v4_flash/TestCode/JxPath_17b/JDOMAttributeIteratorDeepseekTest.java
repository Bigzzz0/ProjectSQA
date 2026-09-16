package org.apache.commons.jxpath.ri.model.jdom;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class JDOMAttributeIteratorDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: JDOMAttributeIterator
     * 
     * Branches:
     * 1. parent.getNode() instanceof Element - true/false
     * 2. prefix != null - true/false
     * 3. prefix.equals("xml") - true/false
     * 4. uri != null - true/false
     * 5. ns == null (after namespace resolution) - true/false
     * 6. lname.equals("*") - true/false
     * 7. ns != null (inside non-wildcard branch) - true/false
     * 8. attr != null - true/false
     * 9. attr.getNamespace().equals(ns) - true/false
     * 10. attributes == null - true/false
     * 11. position >= 1 && position <= attributes.size() - true/false
     * 
     * Boundary Values:
     * - position = 0, 1, size, size+1, negative
     * - null prefix, empty prefix, "xml" prefix
     * - wildcard "*" vs specific name
     * - empty attribute list
     * - namespace with no URI
     * 
     * Defect Target (from Defects4J):
     * The bug is in the wildcard attribute iteration with namespaces.
     * When iterating attributes with wildcard "*" and a namespace prefix,
     * the iterator incorrectly filters attributes. The expected behavior is
     * to return ALL attributes that match the namespace, but the current
     * implementation only returns attributes whose namespace equals the
     * resolved namespace. The defect is exposed when multiple attributes
     * share the same namespace but have different prefixes.
     * 
     * Test case: <vendor/product/price:amount/@*> should return [10%, 20%]
     * but returns only [20%] because the first attribute's namespace
     * doesn't match exactly due to prefix handling.
     */

    // Helper to create a JDOM element with namespaces
    private Element createElementWithNamespaces() {
        Element root = new Element("root");
        Namespace ns1 = Namespace.getNamespace("price", "http://price");
        Namespace ns2 = Namespace.getNamespace("discount", "http://discount");
        
        Element product = new Element("product");
        Element price = new Element("price:amount", ns1);
        price.setAttribute("value", "10%", ns1);
        price.setAttribute("value", "20%", ns2);
        
        product.addContent(price);
        root.addContent(product);
        return root;
    }

    @Test(timeout = 4000)
    public void testWildcardAttributeIterationWithNamespaces() {
        // This test targets the specific defect: wildcard iteration with namespaces
        Element root = createElementWithNamespaces();
        Element priceElement = root.getChild("product").getChild("price:amount", 
                Namespace.getNamespace("price", "http://price"));
        
        // Create a NodePointer for the price element
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                priceElement, null, null);
        
        // Use wildcard with namespace prefix
        QName name = new QName("http://price", "*", "price");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        // Should iterate through all attributes that match the namespace
        // The defect causes only the second attribute to be returned
        assertTrue("Should have at least one attribute", iterator.setPosition(1));
        assertEquals("First attribute value", "10%", 
                ((Attribute) ((JDOMAttributePointer) iterator.getNodePointer()).getNode()).getValue());
        
        assertTrue("Should have second attribute", iterator.setPosition(2));
        assertEquals("Second attribute value", "20%", 
                ((Attribute) ((JDOMAttributePointer) iterator.getNodePointer()).getNode()).getValue());
        
        assertFalse("Should not have third attribute", iterator.setPosition(3));
    }

    @Test(timeout = 4000)
    public void testWildcardAttributeIterationNoNamespace() {
        Element element = new Element("test");
        element.setAttribute("attr1", "value1");
        element.setAttribute("attr2", "value2");
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(null, "*", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertTrue(iterator.setPosition(1));
        assertEquals("value1", 
                ((Attribute) ((JDOMAttributePointer) iterator.getNodePointer()).getNode()).getValue());
        assertTrue(iterator.setPosition(2));
        assertEquals("value2", 
                ((Attribute) ((JDOMAttributePointer) iterator.getNodePointer()).getNode()).getValue());
        assertFalse(iterator.setPosition(3));
    }

    @Test(timeout = 4000)
    public void testSpecificAttributeWithNamespace() {
        Element element = new Element("test");
        Namespace ns = Namespace.getNamespace("prefix", "http://example.com");
        element.setAttribute("attr", "value", ns);
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName("http://example.com", "attr", "prefix");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertTrue(iterator.setPosition(1));
        Attribute attr = (Attribute) ((JDOMAttributePointer) iterator.getNodePointer()).getNode();
        assertEquals("value", attr.getValue());
        assertEquals("prefix", attr.getNamespacePrefix());
    }

    @Test(timeout = 4000)
    public void testSpecificAttributeNoNamespace() {
        Element element = new Element("test");
        element.setAttribute("attr", "value");
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(null, "attr", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertTrue(iterator.setPosition(1));
        Attribute attr = (Attribute) ((JDOMAttributePointer) iterator.getNodePointer()).getNode();
        assertEquals("value", attr.getValue());
        assertNull(attr.getNamespacePrefix());
    }

    @Test(timeout = 4000)
    public void testAttributeNotFound() {
        Element element = new Element("test");
        element.setAttribute("attr", "value");
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(null, "nonexistent", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertFalse("Should not find attribute", iterator.setPosition(1));
        assertNull("getNodePointer should return null", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testXmlNamespacePrefix() {
        Element element = new Element("test");
        element.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(Namespace.XML_NAMESPACE.getURI(), "lang", "xml");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertTrue(iterator.setPosition(1));
        Attribute attr = (Attribute) ((JDOMAttributePointer) iterator.getNodePointer()).getNode();
        assertEquals("en", attr.getValue());
        assertEquals("xml", attr.getNamespacePrefix());
    }

    @Test(timeout = 4000)
    public void testNullParentNode() {
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                null, null, null);
        
        QName name = new QName(null, "attr", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertFalse("Should not iterate with null parent node", iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testPositionBoundaries() {
        Element element = new Element("test");
        element.setAttribute("attr1", "value1");
        element.setAttribute("attr2", "value2");
        element.setAttribute("attr3", "value3");
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(null, "*", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        // Test position 0
        assertFalse("Position 0 should be invalid", iterator.setPosition(0));
        assertEquals("Position should be 0", 0, iterator.getPosition());
        
        // Test position 1
        assertTrue("Position 1 should be valid", iterator.setPosition(1));
        assertEquals("Position should be 1", 1, iterator.getPosition());
        
        // Test position 3 (size)
        assertTrue("Position 3 should be valid", iterator.setPosition(3));
        assertEquals("Position should be 3", 3, iterator.getPosition());
        
        // Test position 4 (size+1)
        assertFalse("Position 4 should be invalid", iterator.setPosition(4));
        assertEquals("Position should be 4", 4, iterator.getPosition());
        
        // Test negative position
        assertFalse("Negative position should be invalid", iterator.setPosition(-1));
        assertEquals("Position should be -1", -1, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWithPositionZero() {
        Element element = new Element("test");
        element.setAttribute("attr", "value");
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(null, "attr", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        // getNodePointer with position 0 should auto-advance to position 1
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull("Should return pointer", pointer);
        assertEquals("Position should be 1", 1, iterator.getPosition());
        
        // Now getNodePointer should return the same attribute
        NodePointer pointer2 = iterator.getNodePointer();
        assertSame("Should return same pointer", pointer, pointer2);
    }

    @Test(timeout = 4000)
    public void testEmptyAttributeList() {
        Element element = new Element("test");
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(null, "*", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertFalse("Should not iterate empty list", iterator.setPosition(1));
        assertNull("getNodePointer should return null", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testNamespaceWithNoURI() {
        Element element = new Element("test");
        Namespace ns = Namespace.getNamespace("prefix", "");
        element.setAttribute("attr", "value", ns);
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        // Use a prefix that resolves to null URI
        QName name = new QName("", "attr", "prefix");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        // Should not find attribute because namespace URI is empty
        assertFalse("Should not find attribute with empty namespace URI", iterator.setPosition(1));
    }

    @Test(timeout = 4000)
    public void testGetPositionInitialValue() {
        Element element = new Element("test");
        element.setAttribute("attr", "value");
        
        NodePointer parent = new org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer(
                element, null, null);
        
        QName name = new QName(null, "attr", null);
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);
        
        assertEquals("Initial position should be 0", 0, iterator.getPosition());
    }
}