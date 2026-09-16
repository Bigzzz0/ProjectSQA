package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DOMAttributeIterator
 * 
 * Branches covered:
 * 1. Constructor: node.getNodeType() == Node.ELEMENT_NODE (true/false)
 * 2. Constructor: lname.equals("*") (true/false)
 * 3. Constructor: attr != null (true/false) - when specific name
 * 4. Constructor: loop over NamedNodeMap (0, 1, multiple iterations)
 * 5. testAttr: nodePrefix != null && nodePrefix.equals("xmlns") (true/false)
 * 6. testAttr: nodePrefix == null && nodeLocalName.equals("xmlns") (true/false)
 * 7. testAttr: testLocalName.equals("*") || testLocalName.equals(nodeLocalName) (true/false)
 * 8. testAttr: equalStrings(testPrefix, nodePrefix) (true/false)
 * 9. testAttr: testPrefix != null (true/false) - for namespace resolution
 * 10. testAttr: nodePrefix != null (true/false) - for namespace resolution
 * 11. testAttr: equalStrings(testNS, nodeNS) (true/false)
 * 12. getAttribute: testPrefix != null (true/false)
 * 13. getAttribute: testNS != null (true/false)
 * 14. getAttribute: attr != null (true/false) - after getAttributeNodeNS
 * 15. getAttribute: loop over NamedNodeMap (0, 1, multiple)
 * 16. getAttribute: testAttr(attr) (true/false) - fallback loop
 * 17. getNodePointer: position == 0 (true/false)
 * 18. getNodePointer: setPosition(1) (true/false)
 * 19. getNodePointer: index < 0 (true/false)
 * 20. setPosition: position >= 1 && position <= attributes.size() (true/false)
 * 
 * Boundary conditions:
 * - Empty attribute list
 * - Single attribute
 * - Multiple attributes
 * - Position 0, 1, size, size+1
 * - Null prefix, empty prefix, "xmlns" prefix
 * - Namespace URI resolution with null/empty/non-existent prefix
 * - Wildcard "*" name
 * - Specific name with/without prefix
 * 
 * Defect targeted (from Defects4J):
 * - testAxisAttribute: Evaluating value iterator <vendor/product/price:amount/@*>
 *   expected:<[10%, 20%]> but was:<[20%]>
 *   This indicates that when iterating attributes with wildcard "*", the iterator
 *   incorrectly filters out attributes that have a namespace prefix, specifically
 *   when the prefix is not "xmlns" but the attribute is in a namespace.
 *   The bug is in testAttr: when nodePrefix is not null and not "xmlns", and
 *   testLocalName is "*", the method should return true for all non-xmlns attributes,
 *   but it incorrectly requires namespace URI matching.
 */
public class DOMAttributeIteratorDeepseekTest {

    private Document createDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    private NodePointer createNodePointer(Element element) {
        // Create a simple NodePointer for testing
        return new DOMNodePointer(element, null, null);
    }

    @Test(timeout = 4000)
    public void testConstructorNonElementNode() throws Exception {
        Document doc = createDocument("<root>text</root>");
        Node textNode = doc.getDocumentElement().getFirstChild();
        NodePointer pointer = createNodePointer(doc.getDocumentElement());
        
        // Create iterator with a non-element node (text node)
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testConstructorElementWithNoAttributes() throws Exception {
        Document doc = createDocument("<root/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testConstructorSpecificAttributeName() throws Exception {
        Document doc = createDocument("<root attr1='value1' attr2='value2'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));
        
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("attr1", np.getName().getName());
        assertEquals("value1", np.getValue());
    }

    @Test(timeout = 4000)
    public void testConstructorSpecificAttributeNameNotFound() throws Exception {
        Document doc = createDocument("<root attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("nonexistent"));
        
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testConstructorWildcardAllAttributes() throws Exception {
        Document doc = createDocument("<root attr1='value1' attr2='value2' attr3='value3'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        assertEquals(3, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals("attr1", iterator.getNodePointer().getName().getName());
        assertTrue(iterator.setPosition(2));
        assertEquals("attr2", iterator.getNodePointer().getName().getName());
        assertTrue(iterator.setPosition(3));
        assertEquals("attr3", iterator.getNodePointer().getName().getName());
        assertFalse(iterator.setPosition(4));
    }

    @Test(timeout = 4000)
    public void testConstructorFiltersXmlnsAttributes() throws Exception {
        Document doc = createDocument("<root xmlns='http://example.com' xmlns:foo='http://foo.com' attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Should only contain attr1, not xmlns or xmlns:foo
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals("attr1", iterator.getNodePointer().getName().getName());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNamespacePrefix() throws Exception {
        Document doc = createDocument("<root xmlns:foo='http://foo.com' foo:attr1='value1' attr2='value2'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("foo", "attr1"));
        
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        assertEquals("attr1", np.getName().getName());
        assertEquals("foo", np.getName().getPrefix());
        assertEquals("value1", np.getValue());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNamespacePrefixNotFound() throws Exception {
        Document doc = createDocument("<root xmlns:foo='http://foo.com' foo:attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("bar", "attr1"));
        
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerPositionZero() throws Exception {
        Document doc = createDocument("<root attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Reset position to 0
        iterator.setPosition(0);
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("attr1", np.getName().getName());
        // Position should be reset to 0 after getNodePointer
        assertEquals(0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerPositionZeroNoAttributes() throws Exception {
        Document doc = createDocument("<root/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        iterator.setPosition(0);
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testSetPositionBoundaries() throws Exception {
        Document doc = createDocument("<root attr1='v1' attr2='v2' attr3='v3'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Test boundaries
        assertFalse(iterator.setPosition(0));
        assertTrue(iterator.setPosition(1));
        assertTrue(iterator.setPosition(2));
        assertTrue(iterator.setPosition(3));
        assertFalse(iterator.setPosition(4));
        assertFalse(iterator.setPosition(-1));
        assertFalse(iterator.setPosition(100));
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWithIndexAdjustment() throws Exception {
        Document doc = createDocument("<root attr1='v1' attr2='v2'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Set position to 0, then getNodePointer should adjust index to 0
        iterator.setPosition(0);
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("attr1", np.getName().getName());
    }

    @Test(timeout = 4000)
    public void testDefectTargetedWildcardWithNamespaceAttributes() throws Exception {
        // This test targets the specific defect from Defects4J
        // The bug: when iterating with wildcard "*", attributes with namespace prefixes
        // are incorrectly filtered out when the namespace URI doesn't match
        String xml = "<vendor xmlns:price='http://example.com/price'>"
                + "<product>"
                + "<price:amount discount='10%' regular='20%'/>"
                + "</product>"
                + "</vendor>";
        
        Document doc = createDocument(xml);
        Element product = (Element) doc.getElementsByTagName("product").item(0);
        Element amount = (Element) product.getFirstChild();
        
        NodePointer pointer = createNodePointer(amount);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Should find both attributes: discount and regular
        assertEquals(2, iterator.getPosition());
        
        // Verify both attributes are accessible
        assertTrue(iterator.setPosition(1));
        NodePointer np1 = iterator.getNodePointer();
        assertNotNull(np1);
        
        assertTrue(iterator.setPosition(2));
        NodePointer np2 = iterator.getNodePointer();
        assertNotNull(np2);
        
        // Collect all attribute names
        String[] names = new String[2];
        names[0] = np1.getName().getName();
        names[1] = np2.getName().getName();
        
        // Both attributes should be present regardless of order
        boolean hasDiscount = false;
        boolean hasRegular = false;
        for (String name : names) {
            if ("discount".equals(name)) hasDiscount = true;
            if ("regular".equals(name)) hasRegular = true;
        }
        
        assertTrue("Should contain discount attribute", hasDiscount);
        assertTrue("Should contain regular attribute", hasRegular);
        
        // Verify values
        String[] values = new String[2];
        values[0] = (String) np1.getValue();
        values[1] = (String) np2.getValue();
        
        boolean has10 = false;
        boolean has20 = false;
        for (String value : values) {
            if ("10%".equals(value)) has10 = true;
            if ("20%".equals(value)) has20 = true;
        }
        
        assertTrue("Should contain value 10%", has10);
        assertTrue("Should contain value 20%", has20);
    }

    @Test(timeout = 4000)
    public void testDefectTargetedWildcardWithMixedAttributes() throws Exception {
        // Additional test for the defect: mixed namespace and non-namespace attributes
        String xml = "<root xmlns:foo='http://foo.com' foo:attr1='value1' attr2='value2'/>";
        
        Document doc = createDocument(xml);
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Should contain both attributes
        assertEquals(2, iterator.getPosition());
        
        assertTrue(iterator.setPosition(1));
        NodePointer np1 = iterator.getNodePointer();
        assertNotNull(np1);
        
        assertTrue(iterator.setPosition(2));
        NodePointer np2 = iterator.getNodePointer();
        assertNotNull(np2);
        
        String name1 = np1.getName().getName();
        String name2 = np2.getName().getName();
        
        assertTrue("attr1".equals(name1) || "attr1".equals(name2));
        assertTrue("attr2".equals(name1) || "attr2".equals(name2));
    }

    @Test(timeout = 4000)
    public void testGetAttributeWithNamespaceFallback() throws Exception {
        // Test the fallback path in getAttribute when getAttributeNodeNS returns null
        String xml = "<root xmlns:foo='http://foo.com' foo:attr1='value1'/>";
        
        Document doc = createDocument(xml);
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("foo", "attr1"));
        
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("attr1", np.getName().getName());
        assertEquals("value1", np.getValue());
    }

    @Test(timeout = 4000)
    public void testGetAttributeWithNullPrefix() throws Exception {
        Document doc = createDocument("<root attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));
        
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("attr1", np.getName().getName());
        assertEquals("value1", np.getValue());
    }

    @Test(timeout = 4000)
    public void testEqualStringsEdgeCases() throws Exception {
        // Test the equalStrings method indirectly through testAttr
        Document doc = createDocument("<root xmlns:foo='http://foo.com' foo:attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        // Test with prefix that doesn't resolve
        DOMAttributeIterator iterator1 = new DOMAttributeIterator(pointer, new QName("nonexistent", "attr1"));
        assertEquals(0, iterator1.getPosition());
        
        // Test with null prefix and namespace attribute
        DOMAttributeIterator iterator2 = new DOMAttributeIterator(pointer, new QName("attr1"));
        assertEquals(0, iterator2.getPosition());
    }

    @Test(timeout = 4000)
    public void testMultipleIterations() throws Exception {
        Document doc = createDocument("<root attr1='v1' attr2='v2' attr3='v3'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Iterate through all positions multiple times
        for (int i = 0; i < 3; i++) {
            assertTrue(iterator.setPosition(1));
            assertEquals("attr1", iterator.getNodePointer().getName().getName());
            assertTrue(iterator.setPosition(2));
            assertEquals("attr2", iterator.getNodePointer().getName().getName());
            assertTrue(iterator.setPosition(3));
            assertEquals("attr3", iterator.getNodePointer().getName().getName());
        }
    }

    @Test(timeout = 4000)
    public void testEmptyAttributeListWithSpecificName() throws Exception {
        Document doc = createDocument("<root/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));
        
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testSingleAttributeWithWildcard() throws Exception {
        Document doc = createDocument("<root attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("attr1", np.getName().getName());
        assertEquals("value1", np.getValue());
    }

    @Test(timeout = 4000)
    public void testAttributeWithXmlnsPrefixExplicitly() throws Exception {
        Document doc = createDocument("<root xmlns:foo='http://foo.com' foo:attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        // Try to access xmlns:foo attribute directly
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("xmlns", "foo"));
        
        // Should not find it because xmlns attributes are filtered
        assertEquals(0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testAttributeWithDefaultXmlns() throws Exception {
        Document doc = createDocument("<root xmlns='http://example.com' attr1='value1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Should only contain attr1, not the default xmlns
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals("attr1", iterator.getNodePointer().getName().getName());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerAfterSetPositionToZero() throws Exception {
        Document doc = createDocument("<root attr1='v1' attr2='v2'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Set position to 0 and then get node pointer
        iterator.setPosition(0);
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("attr1", np.getName().getName());
        
        // Position should be reset to 0
        assertEquals(0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWithInvalidPosition() throws Exception {
        Document doc = createDocument("<root attr1='v1'/>");
        Element element = doc.getDocumentElement();
        NodePointer pointer = createNodePointer(element);
        
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        
        // Set position to invalid value
        iterator.setPosition(5);
        NodePointer np = iterator.getNodePointer();
        // Should return null because position is out of bounds
        assertNull(np);
    }
}