package org.apache.commons.jxpath.ri.model.jdom;

/*
 * [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Class Under Test: org.apache.commons.jxpath.ri.model.jdom.JDOMAttributeIterator
 * Target Environment: Java 8 / JUnit 4 / Apache Commons JXPath (Defects4J)
 *
 * Decision / Condition Branch Analysis:
 * 1. Constructor: parent.getNode() instanceof Element
 *    - TRUE: Normal element traversal path.
 *    - FALSE: Non-element parent (Document, Text, Comment, etc.) -> attributes remains null.
 * 2. Constructor: prefix != null
 *    - TRUE: Check prefix ("xml" vs custom/unknown prefix).
 *    - FALSE: Default to Namespace.NO_NAMESPACE.
 * 3. Constructor: prefix.equals("xml")
 *    - TRUE: Namespace.XML_NAMESPACE.
 *    - FALSE: Lookup namespace on element -> if (ns == null) -> Collections.EMPTY_LIST & early return.
 * 4. Constructor: !lname.equals("*")
 *    - TRUE (Exact match): element.getAttribute(lname, ns) -> if attr != null add to list.
 *    - FALSE (Wildcard "*"): Iterate allAttributes, filter by attr.getNamespace().equals(ns).
 * 5. Method getNodePointer():
 *    - position == 0: attempts setPosition(1). If false -> returns null; if true -> position reset to 0.
 *    - index < 0: boundary fallback (index reset to 0).
 *    - normal position: returns JDOMAttributePointer with attributes.get(index).
 * 6. Method setPosition(int):
 *    - attributes == null: returns false.
 *    - position >= 1 && position <= attributes.size(): true for valid range, false for out-of-bounds.
 *
 * Defect-Targeted Ground Truth:
 * - Defects4J JXPath (DOMModelTest/JDOMModelTest testNamespaceMapping):
 *   Querying attributes with an XPath prefix (e.g. rate:discount) resolved via JXPath NamespaceResolver
 *   fails with JXPathNotFoundException when the prefix is not declared directly on the element via
 *   element.getNamespace(prefix), or when the document prefix differs from the registered XPath prefix.
 * =====================================================================================================
 */

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.junit.Test;

import static org.junit.Assert.*;

public class JDOMAttributeIteratorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testExactAttributeNoNamespace() {
        Element element = new Element("item");
        Attribute attr = new Attribute("id", "123");
        element.setAttribute(attr);

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("id");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        assertEquals("Initial position must be 0", 0, iterator.getPosition());
        assertTrue("setPosition(1) should succeed", iterator.setPosition(1));
        assertEquals("Position should now be 1", 1, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        assertNotNull("NodePointer should not be null", ptr);
        assertTrue("NodePointer must be instance of JDOMAttributePointer", ptr instanceof JDOMAttributePointer);
        assertEquals("Attribute value should match", "123", ptr.getValue());
        assertEquals("Immediate node should be the JDOM Attribute", attr, ptr.getImmediateNode());

        assertFalse("setPosition(2) beyond count must return false", iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testExactAttributeWithStandardXmlNamespace() {
        Element element = new Element("item");
        Attribute xmlAttr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(xmlAttr);

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("xml", "lang");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        assertTrue("Should locate attribute with xml prefix", iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull("NodePointer should not be null", ptr);
        assertEquals("en", ptr.getValue());
        assertEquals("xml:lang", ((Attribute) ptr.getImmediateNode()).getQualifiedName());
    }

    @Test(timeout = 4000)
    public void testWildcardNoNamespace() {
        Element element = new Element("item");
        Attribute attr1 = new Attribute("a", "1");
        Attribute attr2 = new Attribute("b", "2");
        Attribute attrXml = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(attr1);
        element.setAttribute(attr2);
        element.setAttribute(attrXml);

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName wildcard = new QName("*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, wildcard);

        assertTrue("First wildcard attribute should be accessible", iterator.setPosition(1));
        assertEquals("1", iterator.getNodePointer().getValue());

        assertTrue("Second wildcard attribute should be accessible", iterator.setPosition(2));
        assertEquals("2", iterator.getNodePointer().getValue());

        assertFalse("Third attribute has XML_NAMESPACE, should not match NO_NAMESPACE wildcard", iterator.setPosition(3));
    }

    @Test(timeout = 4000)
    public void testWildcardWithDeclaredNamespace() {
        Namespace customNs = Namespace.getNamespace("custom", "http://commons.apache.org/test");
        Element element = new Element("item", customNs);
        Attribute attr1 = new Attribute("x", "10", customNs);
        Attribute attr2 = new Attribute("y", "20", customNs);
        Attribute attrNoNs = new Attribute("z", "30");
        element.setAttribute(attr1);
        element.setAttribute(attr2);
        element.setAttribute(attrNoNs);

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName wildcardCustom = new QName("custom", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, wildcardCustom);

        assertTrue("First custom namespace attribute found", iterator.setPosition(1));
        assertEquals("10", iterator.getNodePointer().getValue());

        assertTrue("Second custom namespace attribute found", iterator.setPosition(2));
        assertEquals("20", iterator.getNodePointer().getValue());

        assertFalse("Should not match attributes outside custom namespace", iterator.setPosition(3));
    }

    @Test(timeout = 4000)
    public void testWildcardXmlNamespace() {
        Element element = new Element("item");
        element.setAttribute(new Attribute("lang", "en", Namespace.XML_NAMESPACE));
        element.setAttribute(new Attribute("space", "preserve", Namespace.XML_NAMESPACE));
        element.setAttribute(new Attribute("id", "regular"));

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName wildcardXml = new QName("xml", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, wildcardXml);

        assertTrue("First xml attribute found", iterator.setPosition(1));
        assertEquals("en", iterator.getNodePointer().getValue());

        assertTrue("Second xml attribute found", iterator.setPosition(2));
        assertEquals("preserve", iterator.getNodePointer().getValue());

        assertFalse("Regular attribute should not match xml:*", iterator.setPosition(3));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetNodePointerWhenPositionZeroAutoAdvances() {
        Element element = new Element("item");
        element.setAttribute(new Attribute("title", "book"));

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("title"));

        assertEquals("Initially position is 0", 0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull("getNodePointer at position 0 should auto-advance to 1 and reset to 0", ptr);
        assertEquals("book", ptr.getValue());
        assertEquals("Position should remain 0 after getNodePointer() at pos 0", 0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWhenEmptyReturnsNull() {
        Element element = new Element("item");
        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("missing"));

        assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        assertNull("getNodePointer on empty attribute list must return null", ptr);
        assertEquals(0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testSetPositionBoundaries() {
        Element element = new Element("item");
        element.setAttribute(new Attribute("a", "1"));
        element.setAttribute(new Attribute("b", "2"));

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));

        assertFalse("position 0 is invalid (1-based index)", iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertFalse("negative position is invalid", iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());

        assertTrue("position 1 is lower valid boundary", iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());

        assertTrue("position 2 is upper valid boundary", iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());

        assertFalse("position 3 is beyond upper boundary", iterator.setPosition(3));
        assertEquals(3, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testGetNodePointerWithNegativePositionFallback() {
        Element element = new Element("item");
        element.setAttribute(new Attribute("prop", "val"));

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("prop"));

        // Force position to negative value
        iterator.setPosition(-5);
        assertEquals(-5, iterator.getPosition());

        // index = position - 1 = -6 < 0 -> resets index to 0
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull("Fallback index < 0 should resolve to index 0", ptr);
        assertEquals("val", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testNonElementParentLeavesAttributesNull() {
        Text textNode = new Text("sample text");
        JDOMNodePointer parent = new JDOMNodePointer(textNode, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("attr"));

        assertFalse("setPosition on non-element parent must return false", iterator.setPosition(1));
        assertNull("getNodePointer on non-element parent must return null", iterator.getNodePointer());
        assertEquals("Position should remain 0", 0, iterator.getPosition());
    }

    @Test(timeout = 4000)
    public void testDocumentParentLeavesAttributesNull() {
        Document doc = new Document(new Element("root"));
        JDOMNodePointer parent = new JDOMNodePointer(doc, Locale.getDefault());

        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("attr"));

        assertFalse("setPosition on Document parent must return false", iterator.setPosition(1));
        assertNull("getNodePointer on Document parent must return null", iterator.getNodePointer());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * Targets the defect revealed in JDOMModelTest::testNamespaceMapping:
     * JXPathNotFoundException: No value for xpath: vendor[1]/product[1]/rate:amount[1]/@rate:discount
     *
     * JDOMAttributeIterator incorrectly relies on element.getNamespace(prefix) which
     * returns null when the namespace prefix is mapped in JXPath's NamespaceResolver
     * or on an attribute/ancestor rather than declared as an in-scope prefix on the element.
     */
    @Test(timeout = 4000)
    public void testDefects4JNamespaceMappingExactAttribute() {
        Element element = new Element("amount");
        Namespace rateNs = Namespace.getNamespace("rate", "http://www.formals.com/schema/rate");
        Attribute discountAttr = new Attribute("discount", "10%", rateNs);
        element.setAttribute(discountAttr);

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        parent.getNamespaceResolver().registerNamespace("rate", "http://www.formals.com/schema/rate");

        QName qname = new QName("rate", "discount");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertTrue("Evaluating mapped namespace attribute @rate:discount should succeed",
                   iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull("Attribute NodePointer must not be null", ptr);
        assertEquals("Attribute value must be 10%", "10%", ptr.getValue());
    }

    /**
     * Targets namespace prefix aliasing where XPath prefix differs from XML document prefix.
     */
    @Test(timeout = 4000)
    public void testDefects4JNamespaceMappingDifferentPrefix() {
        Element element = new Element("amount");
        Namespace docNs = Namespace.getNamespace("r", "http://www.formals.com/schema/rate");
        Attribute discountAttr = new Attribute("discount", "10%", docNs);
        element.setAttribute(discountAttr);

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        parent.getNamespaceResolver().registerNamespace("rate", "http://www.formals.com/schema/rate");

        QName qname = new QName("rate", "discount");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, qname);

        assertTrue("XPath prefix 'rate' should resolve to document prefix 'r' via URI match",
                   iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull("Attribute pointer must exist", ptr);
        assertEquals("10%", ptr.getValue());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnknownPrefixReturnsEmptyList() {
        Element element = new Element("item");
        element.setAttribute(new Attribute("name", "test"));

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName unknownPrefixQName = new QName("unregistered", "attr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, unknownPrefixQName);

        assertFalse("Unknown prefix must fail setPosition", iterator.setPosition(1));
        assertNull("Unknown prefix must return null pointer", iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testAttributeNotFoundReturnsEmptyList() {
        Element element = new Element("item");
        element.setAttribute(new Attribute("existing", "true"));

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("missing");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        assertFalse("Non-existent attribute setPosition(1) must be false", iterator.setPosition(1));
        assertNull("Non-existent attribute getNodePointer must be null", iterator.getNodePointer());
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullQNameThrowsException() {
        Element element = new Element("item");
        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        new JDOMAttributeIterator(parent, null);
    }

    // =========================================================================
    // Partition E: Object State Consistency & Sequential Iteration
    // =========================================================================

    @Test(timeout = 4000)
    public void testSequentialIterationForwardAndBackward() {
        Element element = new Element("item");
        element.setAttribute(new Attribute("first", "1"));
        element.setAttribute(new Attribute("second", "2"));
        element.setAttribute(new Attribute("third", "3"));

        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));

        // Iterate forward
        assertTrue(iterator.setPosition(1));
        assertEquals("1", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(2));
        assertEquals("2", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(3));
        assertEquals("3", iterator.getNodePointer().getValue());

        assertFalse(iterator.setPosition(4));

        // Iterate backward
        assertTrue(iterator.setPosition(2));
        assertEquals("2", iterator.getNodePointer().getValue());

        assertTrue(iterator.setPosition(1));
        assertEquals("1", iterator.getNodePointer().getValue());
    }

    @Test(timeout = 4000)
    public void testNoAttributesOnElementWildcardEmpty() {
        Element element = new Element("item");
        JDOMNodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));

        assertFalse("Wildcard on empty attributes must return false for setPosition(1)",
                    iterator.setPosition(1));
        assertNull("getNodePointer must return null for empty attributes",
                   iterator.getNodePointer());
    }
}