package org.apache.commons.jxpath.ri.model.jdom;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.model.jdom.JDOMAttributeIterator
 *
 * 1. Constructor Branches:
 *    - parent.getNode() instanceof Element (true vs false e.g. Document, Text, Object)
 *    - name.getPrefix() != null vs null
 *    - prefix.equals("xml") (XML_NAMESPACE) vs custom prefix vs unknown prefix
 *    - uri != null vs null from parent.getNamespaceResolver().getNamespaceURI(prefix)
 *    - ns == null early return with Collections.EMPTY_LIST
 *    - !lname.equals("*") vs lname.equals("*")
 *    - element.getAttribute(lname, ns) returns null vs non-null
 *    - wildcard loop: element.getAttributes() filtering by attr.getNamespace().equals(ns)
 *
 * 2. Method Branches:
 *    - getNodePointer():
 *        * position == 0 -> setPosition(1) returns false -> returns null
 *        * position == 0 -> setPosition(1) returns true -> resets position to 0, index becomes 0
 *        * position != 0 -> index = position - 1; (index < 0 branch via negative position)
 *        * returns new JDOMAttributePointer(parent, attribute)
 *    - getPosition(): returns current position state
 *    - setPosition(int position):
 *        * attributes == null guard -> returns false
 *        * updates position
 *        * boundary check: position >= 1 && position <= attributes.size()
 *
 * 3. Defect-Targeted Zone (Defects4J ground truth):
 *    - Bug: In JDOMAttributeIterator with wildcard query (@*), when prefix is null,
 *      ns is set to Namespace.NO_NAMESPACE. As a result, all attributes belonging to
 *      declared XML namespaces are excluded from wildcard results instead of returning
 *      all attributes on the element regardless of namespace.
 */

import java.util.Locale;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;

import static org.junit.Assert.*;

public class JDOMAttributeIteratorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardAttributeLookupByName() {
        Element element = new Element("item");
        element.setAttribute("id", "item-123");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        QName qname = new QName("id");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertEquals("Initial position must be 0", 0, iterator.getPosition());
        assertTrue("setPosition(1) should succeed for matching attribute", iterator.setPosition(1));
        assertEquals("Position must reflect 1", 1, iterator.getPosition());

        NodePointer pointer = iterator.getNodePointer();
        assertNotNull("NodePointer must not be null", pointer);
        assertTrue("Pointer must be an instance of JDOMAttributePointer", pointer instanceof JDOMAttributePointer);
        assertEquals("Pointer node must match attribute", "item-123", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testXmlPrefixNamespaceLookup() {
        Element element = new Element("text");
        Attribute langAttr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(langAttr);

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName qname = new QName("xml", "lang");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertTrue("Should locate attribute in standard XML namespace", iterator.setPosition(1));
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull("Attribute pointer must exist", pointer);
        assertEquals("en", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testCustomPrefixNamespaceLookup() {
        Namespace customNs = Namespace.getNamespace("custom", "http://example.com/ns");
        Element element = new Element("item");
        element.addNamespaceDeclaration(customNs);
        element.setAttribute(new Attribute("flag", "true", customNs));

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName qname = new QName("custom", "flag");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertTrue("Should locate attribute with custom namespace prefix", iterator.setPosition(1));
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull(pointer);
        assertEquals("true", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testPrefixWithWildcardMatchingCustomNamespace() {
        Namespace customNs = Namespace.getNamespace("data", "http://data.org");
        Element element = new Element("record");
        element.addNamespaceDeclaration(customNs);
        element.setAttribute(new Attribute("attr1", "val1", customNs));
        element.setAttribute(new Attribute("attr2", "val2", customNs));
        element.setAttribute(new Attribute("other", "val3")); // Default namespace

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName qname = new QName("data", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertTrue("Should find first attribute with matching prefix", iterator.setPosition(1));
        assertEquals("val1", iterator.getNodePointer().getValue());

        assertTrue("Should find second attribute with matching prefix", iterator.setPosition(2));
        assertEquals("val2", iterator.getNodePointer().getValue());

        assertFalse("Should not include attribute with different namespace", iterator.setPosition(3));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParentNodeIsNotElement() {
        // Document node is not an Element
        Document doc = new Document();
        NodePointer parent = new JDOMNodePointer(doc, Locale.getDefault());
        QName qname = new QName("any");

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);
        assertEquals(0, iterator.getPosition());
        assertFalse("setPosition should fail when parent is not Element", iterator.setPosition(1));
        assertNull("getNodePointer should return null when parent is not Element", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testParentNodeIsComment() {
        Comment comment = new Comment("just a comment");
        NodePointer parent = new JDOMNodePointer(comment, Locale.getDefault());
        QName qname = new QName("any");

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);
        assertFalse("setPosition should return false for comment node", iterator.setPosition(1));
        assertNull("getNodePointer should return null for comment node", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testUndefinedPrefixNamespaceReturnsEmpty() {
        Element element = new Element("item");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        // Namespace prefix 'undef' is not mapped
        QName qname = new QName("undef", "field");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertFalse("setPosition(1) should return false for undefined namespace", iterator.setPosition(1));
        assertNull("getNodePointer() must be null when namespace is unresolved", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testNonExistentAttributeName() {
        Element element = new Element("item");
        element.setAttribute("exists", "true");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        QName qname = new QName("doesNotExist");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertFalse("setPosition(1) should return false for non-existent attribute", iterator.setPosition(1));
        assertNull("getNodePointer() should return null for non-existent attribute", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testEmptyElementWildcardAttributes() {
        Element element = new Element("empty");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        QName qname = new QName("*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertFalse("setPosition(1) on element with no attributes should return false", iterator.setPosition(1));
        assertNull("getNodePointer() on empty element should return null", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testSetPositionBoundaries() {
        Element element = new Element("item");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        QName qname = new QName("*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertFalse("setPosition(0) should return false", iterator.setPosition(0));
        assertTrue("setPosition(1) lower valid boundary", iterator.setPosition(1));
        assertTrue("setPosition(2) upper valid boundary", iterator.setPosition(2));
        assertFalse("setPosition(3) out of bounds upper boundary", iterator.setPosition(3));
        assertFalse("setPosition(-1) negative boundary should return false", iterator.setPosition(-1));
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWhenPositionZeroAutoAdvances() {
        Element element = new Element("item");
        element.setAttribute("attr", "val");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        QName qname = new QName("attr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertEquals("Initial position must be 0", 0, iterator.getPosition());
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull("getNodePointer at position 0 must auto-advance and return first attribute", pointer);
        assertEquals("val", pointer.getValue());
        assertEquals("Position should be reset to 0 after getNodePointer auto-advance", 0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWhenNegativePositionHandledSafely() {
        Element element = new Element("item");
        element.setAttribute("attr", "val");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        QName qname = new QName("attr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertFalse(iterator.setPosition(-5));
        assertEquals(-5, iterator.getPosition());
        // index = -5 - 1 = -6, branch 'if (index < 0) index = 0;' ensures index 0 is accessed
        NodePointer pointer = iterator.getNodePointer();
        assertNotNull("getNodePointer with negative position must fallback to index 0", pointer);
        assertEquals("val", pointer.getValue());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defects4J Defect:
     * When evaluating value iterator for wildcard '@*' (prefix is null, lname is '*'),
     * all attributes on the element should be matched regardless of their namespace.
     * In the defective version, prefix == null forces ns = Namespace.NO_NAMESPACE,
     * which discards any attributes that belong to an explicit namespace.
     */
    @Test(timeout = 4000)
    public void testDefectWildcardMustMatchAttributesAcrossAllNamespaces() {
        Element element = new Element("amount");
        Namespace priceNs = Namespace.getNamespace("price", "http://price.com");
        element.addNamespaceDeclaration(priceNs);

        Attribute namespacedAttr = new Attribute("discount", "10%", priceNs);
        Attribute standardAttr = new Attribute("regular", "20%"); // NO_NAMESPACE
        element.setAttribute(namespacedAttr);
        element.setAttribute(standardAttr);

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName wildcardQName = new QName("*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, wildcardQName);

        // Position 1 should match the first attribute
        assertTrue("Iterator should position at index 1", iterator.setPosition(1));
        assertNotNull("NodePointer at index 1 must not be null", iterator.getNodePointer());

        // Position 2 must match the second attribute.
        // Under defective code, namespacedAttr is omitted because its namespace != NO_NAMESPACE,
        // causing attributes list size to be 1 instead of 2.
        assertTrue("Iterator must match second attribute under wildcard '@*' regardless of namespace",
                iterator.setPosition(2));
        assertNotNull("NodePointer at index 2 must not be null", iterator.getNodePointer());

        assertFalse("Iterator must not exceed total attribute count of 2", iterator.setPosition(3));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetNodePointerOutOfBoundsPositiveThrowsException() {
        Element element = new Element("item");
        element.setAttribute("k", "v");
        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());

        QName qname = new QName("k");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        // Force position beyond size: position = 10 -> index = 9, but attributes.size() == 1
        iterator.setPosition(10);
        try {
            iterator.getNodePointer();
            fail("Expected IndexOutOfBoundsException when accessing node pointer at out-of-bounds position");
        } catch (IndexOutOfBoundsException expected) {
            // Success: expected defensive failure when internal pointer is out of range
        }
    }

    @Test(timeout = 4000)
    public void testSetPositionOnUnresolvedParentRemainsFalse() {
        NodePointer parent = new JDOMNodePointer("non-jdom-object", Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("test"));

        assertFalse(iterator.setPosition(0));
        assertFalse(iterator.setPosition(1));
        assertFalse(iterator.setPosition(-1));
        assertEquals(0, iterator.getPosition());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testMultipleSequentialIterations() {
        Element element = new Element("book");
        element.setAttribute("title", "Java In Action");
        element.setAttribute("edition", "2");
        element.setAttribute("inStock", "true");

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName qname = new QName("*");
        NodeIterator iterator = new JDOMAttributeIterator(parent, qname);

        // Sequential walk-through
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertEquals("Java In Action", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        assertEquals("2", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(3));
        assertEquals(3, iterator.getPosition());
        assertEquals("true", iterator.getNodePointer().getValue());

        assertFalse(iterator.setPosition(4));

        // Rewind and re-read
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertEquals("Java In Action", iterator.getNodePointer().getValue());
    }

    @Test(timeout = 4000)
    public void testAttributePointerProperties() {
        Element element = new Element("record");
        element.setAttribute("code", "ALPHA");

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName qname = new QName("code");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertSame("Parent pointer must be preserved", parent, attrPointer.getParent());
        assertEquals("ALPHA", attrPointer.getValue());
        assertEquals("code", attrPointer.getName().getName());
    }
}