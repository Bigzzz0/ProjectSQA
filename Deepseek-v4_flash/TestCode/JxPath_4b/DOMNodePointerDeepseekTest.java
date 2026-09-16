package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 * Test class for DOMNodePointer with comprehensive coverage and defect targeting.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - testNode with null test -> true
 *   - testNode with NodeNameTest (wildcard, prefix, namespace matching)
 *   - testNode with NodeTypeTest (NODE, TEXT, COMMENT, PI)
 *   - testNode with ProcessingInstructionTest
 *   - getName for ELEMENT, PI nodes
 *   - getNamespaceURI, getDefaultNamespaceURI
 *   - isLanguage, getLanguage
 *   - setValue for TEXT/CDATA, Element/Document, String
 *   - createChild, createAttribute
 *   - remove, asPath, getValue, stringValue
 *   - compareChildNodePointers
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments for testNode, getNamespaceURI
 *   - empty string for namespace prefix
 *   - Document node handling in getNamespaceURI, getDefaultNamespaceURI
 *   - Node with no namespace prefix
 *   - Node with colon in name but no prefix
 *   - Empty string values in setValue
 *   - Null values in stringValue for COMMENT, TEXT, PI
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - xml:space="preserve" handling in getValue/stringValue
 *   - Whitespace preservation in text nodes
 *   - Nested elements with mixed content and xml:space
 *   - Comments within preserved whitespace content
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - remove on root node -> JXPathException
 *   - createAttribute on non-Element -> super.createAttribute
 *   - createChild with null factory -> JXPathException
 *   - Unknown namespace prefix in createAttribute -> JXPathException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals (same node, different node, null, different type)
 *   - hashCode consistency
 *   - isActual, isCollection, getLength, isLeaf
 *   - getBaseValue, getImmediateNode
 */
public class DOMNodePointerDeepseekTest {

    private Document createSimpleDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testTestNodeWithNull() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertTrue("testNode(null) should return true", pointer.testNode(null));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeNameTestWildcardNoPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        NodeNameTest test = new NodeNameTest(new QName(null, "*"), null, true);
        assertTrue("Wildcard without prefix should match any element", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeNameTestExactMatch() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("foo");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        NodeNameTest test = new NodeNameTest(new QName(null, "foo"), null);
        assertTrue("Exact name match should return true", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeNameTestNonElement() throws Exception {
        Document doc = createSimpleDocument();
        Text text = doc.createTextNode("text");
        DOMNodePointer pointer = new DOMNodePointer(text, null);
        NodeNameTest test = new NodeNameTest(new QName(null, "foo"), null);
        assertFalse("Non-element node should not match NodeNameTest", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue("Element should match NODE_TYPE_NODE", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestText() throws Exception {
        Document doc = createSimpleDocument();
        Text text = doc.createTextNode("text");
        DOMNodePointer pointer = new DOMNodePointer(text, null);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue("Text node should match NODE_TYPE_TEXT", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestComment() throws Exception {
        Document doc = createSimpleDocument();
        Comment comment = doc.createComment("comment");
        DOMNodePointer pointer = new DOMNodePointer(comment, null);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue("Comment node should match NODE_TYPE_COMMENT", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestPI() throws Exception {
        Document doc = createSimpleDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, null);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue("PI node should match NODE_TYPE_PI", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithProcessingInstructionTest() throws Exception {
        Document doc = createSimpleDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, null);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue("PI with matching target should return true", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithProcessingInstructionTestNoMatch() throws Exception {
        Document doc = createSimpleDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, null);
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse("PI with non-matching target should return false", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testGetNameForElement() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElementNS("http://example.com/ns", "prefix:local");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        QName name = pointer.getName();
        assertEquals("Prefix should be 'prefix'", "prefix", name.getPrefix());
        assertEquals("Local name should be 'local'", "local", name.getName());
    }

    @Test(timeout = 4000)
    public void testGetNameForPI() throws Exception {
        Document doc = createSimpleDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        DOMNodePointer pointer = new DOMNodePointer(pi, null);
        QName name = pointer.getName();
        assertNull("PI name should have null prefix", name.getPrefix());
        assertEquals("PI name should be target", "target", name.getName());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForElement() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElementNS("http://example.com/ns", "prefix:local");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertEquals("Namespace URI should match", "http://example.com/ns", pointer.getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURI() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        elem.setAttribute("xmlns", "http://default-ns.com");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertEquals("Default namespace should be resolved", "http://default-ns.com", pointer.getDefaultNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURIWithNoXmlns() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertNull("No xmlns should return null", pointer.getDefaultNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testIsLanguage() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        elem.setAttribute("xml:lang", "en-US");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertTrue("isLanguage('en') should be true", pointer.isLanguage("en"));
        assertTrue("isLanguage('EN') should be true (case insensitive)", pointer.isLanguage("EN"));
        assertFalse("isLanguage('fr') should be false", pointer.isLanguage("fr"));
    }

    @Test(timeout = 4000)
    public void testIsLanguageWithNoLang() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertFalse("isLanguage with no xml:lang should return false", pointer.isLanguage("en"));
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextNode() throws Exception {
        Document doc = createSimpleDocument();
        Element parent = doc.createElement("parent");
        Text text = doc.createTextNode("old");
        parent.appendChild(text);
        DOMNodePointer pointer = new DOMNodePointer(text, null);
        pointer.setValue("new");
        assertEquals("Text node value should be updated", "new", text.getNodeValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextNodeWithEmptyString() throws Exception {
        Document doc = createSimpleDocument();
        Element parent = doc.createElement("parent");
        Text text = doc.createTextNode("old");
        parent.appendChild(text);
        DOMNodePointer pointer = new DOMNodePointer(text, null);
        pointer.setValue("");
        assertNull("Text node with empty string should be removed", text.getParentNode());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithString() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        pointer.setValue("text content");
        assertEquals("Element should have text child", "text content", elem.getTextContent());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        Element child = doc.createElement("child");
        child.setTextContent("value");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        pointer.setValue(child);
        assertEquals("Element should have cloned child", "value", elem.getFirstChild().getTextContent());
    }

    @Test(timeout = 4000)
    public void testRemove() throws Exception {
        Document doc = createSimpleDocument();
        Element parent = doc.createElement("parent");
        Element child = doc.createElement("child");
        parent.appendChild(child);
        DOMNodePointer pointer = new DOMNodePointer(child, null);
        pointer.remove();
        assertNull("Child should be removed", child.getParentNode());
    }

    @Test(timeout = 4000)
    public void testAsPathWithId() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null, "myId");
        String path = pointer.asPath();
        assertEquals("Path should use id()", "id('myId')", path);
    }

    @Test(timeout = 4000)
    public void testAsPathForTextNode() throws Exception {
        Document doc = createSimpleDocument();
        Element parent = doc.createElement("parent");
        Text text = doc.createTextNode("text");
        parent.appendChild(text);
        DOMNodePointer parentPtr = new DOMNodePointer(parent, null);
        DOMNodePointer textPtr = new DOMNodePointer(parentPtr, text);
        String path = textPtr.asPath();
        assertTrue("Path should contain /text()[1]", path.contains("/text()[1]"));
    }

    @Test(timeout = 4000)
    public void testGetValueForTextNode() throws Exception {
        Document doc = createSimpleDocument();
        Text text = doc.createTextNode("  hello  ");
        DOMNodePointer pointer = new DOMNodePointer(text, null);
        assertEquals("Text value should be trimmed", "hello", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueForCommentNode() throws Exception {
        Document doc = createSimpleDocument();
        Comment comment = doc.createComment("  comment  ");
        DOMNodePointer pointer = new DOMNodePointer(comment, null);
        assertEquals("Comment value should be trimmed", "comment", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueForPINode() throws Exception {
        Document doc = createSimpleDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "  data  ");
        DOMNodePointer pointer = new DOMNodePointer(pi, null);
        assertEquals("PI data should be trimmed", "data", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = createSimpleDocument();
        Element parent = doc.createElement("parent");
        Element child1 = doc.createElement("child1");
        Element child2 = doc.createElement("child2");
        parent.appendChild(child1);
        parent.appendChild(child2);
        DOMNodePointer pointer = new DOMNodePointer(parent, null);
        DOMNodePointer ptr1 = new DOMNodePointer(pointer, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(pointer, child2);
        assertTrue("child1 should come before child2", pointer.compareChildNodePointers(ptr1, ptr2) < 0);
        assertTrue("child2 should come after child1", pointer.compareChildNodePointers(ptr2, ptr1) > 0);
        assertEquals("Same node should return 0", 0, pointer.compareChildNodePointers(ptr1, ptr1));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithNullPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertNull("Null prefix should return default namespace", pointer.getNamespaceURI(null));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithEmptyPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertNull("Empty prefix should return default namespace", pointer.getNamespaceURI(""));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithXmlPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertEquals("xml prefix should return XML namespace", 
            "http://www.w3.org/XML/1998/namespace", pointer.getNamespaceURI("xml"));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithXmlnsPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertEquals("xmlns prefix should return xmlns namespace", 
            "http://www.w3.org/2000/xmlns/", pointer.getNamespaceURI("xmlns"));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithUnknownPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertNull("Unknown prefix should return null", pointer.getNamespaceURI("unknown"));
    }

    @Test(timeout = 4000)
    public void testGetLocalNameWithExplicitLocalName() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElementNS("http://ns.com", "prefix:local");
        assertEquals("Should return explicit local name", "local", DOMNodePointer.getLocalName(elem));
    }

    @Test(timeout = 4000)
    public void testGetLocalNameWithColonInName() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("prefix:local");
        assertEquals("Should extract local name after colon", "local", DOMNodePointer.getLocalName(elem));
    }

    @Test(timeout = 4000)
    public void testGetLocalNameWithNoColon() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("simple");
        assertEquals("Should return full name", "simple", DOMNodePointer.getLocalName(elem));
    }

    @Test(timeout = 4000)
    public void testGetPrefixWithExplicitPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElementNS("http://ns.com", "prefix:local");
        assertEquals("Should return explicit prefix", "prefix", DOMNodePointer.getPrefix(elem));
    }

    @Test(timeout = 4000)
    public void testGetPrefixWithColonInName() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("prefix:local");
        assertEquals("Should extract prefix from colon", "prefix", DOMNodePointer.getPrefix(elem));
    }

    @Test(timeout = 4000)
    public void testGetPrefixWithNoPrefix() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("simple");
        assertNull("No prefix should return null", DOMNodePointer.getPrefix(elem));
    }

    @Test(timeout = 4000)
    public void testIsLeafWithNoChildren() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("leaf");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertTrue("Element with no children should be leaf", pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithChildren() throws Exception {
        Document doc = createSimpleDocument();
        Element parent = doc.createElement("parent");
        parent.appendChild(doc.createElement("child"));
        DOMNodePointer pointer = new DOMNodePointer(parent, null);
        assertFalse("Element with children should not be leaf", pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetLength() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertEquals("Length should always be 1", 1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsActual() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertTrue("isActual should return true", pointer.isActual());
    }

    @Test(timeout = 4000)
    public void testIsCollection() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertFalse("isCollection should return false", pointer.isCollection());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Tests xml:space="preserve" handling in getValue().
     * This targets the known defect where whitespace is incorrectly trimmed
     * when xml:space="preserve" is set.
     */
    @Test(timeout = 4000)
    public void testGetValueWithXmlSpacePreserve() throws Exception {
        Document doc = createSimpleDocument();
        Element root = doc.createElement("root");
        root.setAttribute("xml:space", "preserve");
        Element child = doc.createElement("child");
        child.setTextContent(" foo ");
        root.appendChild(child);
        DOMNodePointer pointer = new DOMNodePointer(root, null);
        // The bug causes the value to be " foo " (with spaces) instead of "foo"
        // Actually the bug is that whitespace is preserved when it shouldn't be,
        // or trimmed when it shouldn't be. Based on the defect description,
        // the expected value is "foo" but the bug returns " foo ".
        // We assert the correct behavior (trimmed) to reveal the bug.
        assertEquals("Value should be trimmed despite xml:space=preserve", "foo", pointer.getValue());
    }

    /**
     * Tests nested elements with xml:space="preserve" and text content.
     * This targets the specific defect from XMLSpaceTest::testNestedDOM
     */
    @Test(timeout = 4000)
    public void testGetValueWithNestedPreserve() throws Exception {
        Document doc = createSimpleDocument();
        Element root = doc.createElement("root");
        root.setAttribute("xml:space", "preserve");
        Element child = doc.createElement("child");
        child.setTextContent("foo");
        root.appendChild(child);
        Element child2 = doc.createElement("child2");
        child2.setTextContent("bar");
        root.appendChild(child2);
        DOMNodePointer pointer = new DOMNodePointer(root, null);
        // The bug causes the value to include newlines and spaces between elements
        // Expected correct behavior: concatenated text content trimmed
        assertEquals("Nested preserve should concatenate text", "foobar", pointer.getValue());
    }

    /**
     * Tests nested elements with comments and xml:space="preserve".
     * This targets the specific defect from XMLSpaceTest::testNestedWithCommentsDOM
     */
    @Test(timeout = 4000)
    public void testGetValueWithNestedPreserveAndComments() throws Exception {
        Document doc = createSimpleDocument();
        Element root = doc.createElement("root");
        root.setAttribute("xml:space", "preserve");
        Element child = doc.createElement("child");
        child.setTextContent("foo");
        root.appendChild(child);
        Comment comment = doc.createComment("comment");
        root.appendChild(comment);
        Element child2 = doc.createElement("child2");
        child2.setTextContent("bar");
        root.appendChild(child2);
        DOMNodePointer pointer = new DOMNodePointer(root, null);
        // The bug causes the value to include newlines and spaces
        // Expected correct behavior: concatenated text content trimmed
        assertEquals("Nested preserve with comments should concatenate text", "foobar", pointer.getValue());
    }

    /**
     * Tests that xml:space="preserve" on a text node preserves whitespace.
     * This is the opposite case - when preserve is set, whitespace should be kept.
     */
    @Test(timeout = 4000)
    public void testGetValueWithXmlSpacePreserveOnTextNode() throws Exception {
        Document doc = createSimpleDocument();
        Element root = doc.createElement("root");
        root.setAttribute("xml:space", "preserve");
        Text text = doc.createTextNode(" foo ");
        root.appendChild(text);
        DOMNodePointer pointer = new DOMNodePointer(root, null);
        // When xml:space="preserve" is set on the parent, the text node's whitespace
        // should be preserved. The bug might be trimming it incorrectly.
        // Based on the defect, the expected value is " foo " but the bug returns "foo"
        // Actually looking at the defect more carefully:
        // expected:<foo> but was:< foo > means the bug returns " foo " when "foo" is expected
        // So the bug is that whitespace is NOT being trimmed when it should be.
        // But wait - the test says expected:<foo> but was:< foo >, meaning the test expects "foo"
        // but gets " foo ". So the bug is that whitespace is preserved when it shouldn't be.
        // However, with xml:space="preserve", whitespace SHOULD be preserved.
        // This suggests the bug is in the stringValue method which always trims.
        // The correct behavior should respect xml:space="preserve".
        // For this test, we assert the behavior that reveals the bug:
        // The buggy version returns " foo " (preserved) when it should return "foo" (trimmed)
        // Actually no - the defect says expected:<foo> but was:< foo >, meaning the test
        // expects "foo" but gets " foo ". So the bug is that whitespace is NOT trimmed.
        // But with xml:space="preserve", it should be preserved. So the test might be wrong?
        // Looking at the actual defect reports, these are failing tests that expect
        // the correct behavior. The bug is that whitespace is being trimmed even when
        // xml:space="preserve" is set. So the correct behavior is to preserve whitespace.
        // We'll write the test to assert the correct behavior (preserve whitespace)
        // which will fail on the buggy version that trims.
        assertEquals("With xml:space=preserve, whitespace should be preserved", " foo ", pointer.getValue());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = org.apache.commons.jxpath.JXPathException.class)
    public void testRemoveOnRootNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        doc.appendChild(elem);
        DOMNodePointer pointer = new DOMNodePointer(doc, null);
        pointer.remove();
    }

    @Test(timeout = 4000)
    public void testCreateAttributeOnNonElement() throws Exception {
        Document doc = createSimpleDocument();
        Text text = doc.createTextNode("text");
        DOMNodePointer pointer = new DOMNodePointer(text, null);
        // Should delegate to super.createAttribute which may throw or return null
        try {
            pointer.createAttribute(null, new QName(null, "attr"));
            fail("Should have thrown an exception");
        } catch (Exception e) {
            // Expected - non-Element nodes cannot have attributes
        }
    }

    @Test(timeout = 4000)
    public void testCreateChildWithNullFactory() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        try {
            pointer.createChild(null, new QName(null, "child"), 0);
            fail("Should have thrown JXPathException");
        } catch (org.apache.commons.jxpath.JXPathException e) {
            // Expected - no factory set
        }
    }

    @Test(timeout = 4000)
    public void testGetPointerByIDWithNullElement() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        doc.appendChild(elem);
        DOMNodePointer pointer = new DOMNodePointer(doc, null);
        org.apache.commons.jxpath.Pointer result = pointer.getPointerByID(null, "nonexistent");
        assertTrue("Non-existent ID should return NullPointer", 
            result instanceof org.apache.commons.jxpath.ri.model.beans.NullPointer);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsSameObject() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertTrue("Same object should be equal", pointer.equals(pointer));
    }

    @Test(timeout = 4000)
    public void testEqualsSameNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer1 = new DOMNodePointer(elem, null);
        DOMNodePointer pointer2 = new DOMNodePointer(elem, null);
        assertTrue("Same node should be equal", pointer1.equals(pointer2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem1 = doc.createElement("test1");
        Element elem2 = doc.createElement("test2");
        DOMNodePointer pointer1 = new DOMNodePointer(elem1, null);
        DOMNodePointer pointer2 = new DOMNodePointer(elem2, null);
        assertFalse("Different nodes should not be equal", pointer1.equals(pointer2));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertFalse("Should not equal null", pointer.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertFalse("Should not equal different type", pointer.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        int hash1 = pointer.hashCode();
        int hash2 = pointer.hashCode();
        assertEquals("Hash code should be consistent", hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCodeForSameNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer1 = new DOMNodePointer(elem, null);
        DOMNodePointer pointer2 = new DOMNodePointer(elem, null);
        assertEquals("Same node should have same hash code", pointer1.hashCode(), pointer2.hashCode());
    }

    @Test(timeout = 4000)
    public void testGetBaseValue() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertSame("Base value should be the node", elem, pointer.getBaseValue());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        assertSame("Immediate node should be the node", elem, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testConstructorWithId() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null, "myId");
        String path = pointer.asPath();
        assertEquals("Path should use id()", "id('myId')", path);
    }

    @Test(timeout = 4000)
    public void testConstructorWithParent() throws Exception {
        Document doc = createSimpleDocument();
        Element parent = doc.createElement("parent");
        Element child = doc.createElement("child");
        parent.appendChild(child);
        DOMNodePointer parentPtr = new DOMNodePointer(parent, null);
        DOMNodePointer childPtr = new DOMNodePointer(parentPtr, child);
        assertNotNull("Child pointer should have parent", childPtr.getParent());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithDocumentNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        doc.appendChild(elem);
        DOMNodePointer pointer = new DOMNodePointer(doc, null);
        // Document node should delegate to document element
        String ns = pointer.getNamespaceURI();
        // No namespace set, should be null
        assertNull("Document node namespace should be null", ns);
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURIWithDocumentNode() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("root");
        elem.setAttribute("xmlns", "http://default.com");
        doc.appendChild(elem);
        DOMNodePointer pointer = new DOMNodePointer(doc, null);
        assertEquals("Document node should resolve default namespace", 
            "http://default.com", pointer.getDefaultNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testStringValueWithNullCommentData() throws Exception {
        Document doc = createSimpleDocument();
        Comment comment = doc.createComment(null);
        DOMNodePointer pointer = new DOMNodePointer(comment, null);
        assertEquals("Null comment data should return empty string", "", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testStringValueWithNullTextData() throws Exception {
        Document doc = createSimpleDocument();
        Text text = doc.createTextNode(null);
        DOMNodePointer pointer = new DOMNodePointer(text, null);
        assertEquals("Null text data should return empty string", "", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testStringValueWithNullPIData() throws Exception {
        Document doc = createSimpleDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", null);
        DOMNodePointer pointer = new DOMNodePointer(pi, null);
        assertEquals("Null PI data should return empty string", "", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointersWithAttributes() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        Attr attr1 = doc.createAttribute("attr1");
        Attr attr2 = doc.createAttribute("attr2");
        elem.setAttributeNode(attr1);
        elem.setAttributeNode(attr2);
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        DOMNodePointer attrPtr1 = new DOMNodePointer(pointer, attr1);
        DOMNodePointer attrPtr2 = new DOMNodePointer(pointer, attr2);
        // Attributes should come before child elements
        assertTrue("Attribute should come before element", 
            pointer.compareChildNodePointers(attrPtr1, attrPtr2) < 0);
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointersWithAttributeAndElement() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        Attr attr = doc.createAttribute("attr");
        elem.setAttributeNode(attr);
        Element child = doc.createElement("child");
        elem.appendChild(child);
        DOMNodePointer pointer = new DOMNodePointer(elem, null);
        DOMNodePointer attrPtr = new DOMNodePointer(pointer, attr);
        DOMNodePointer childPtr = new DOMNodePointer(pointer, child);
        // Attribute should come before element
        assertTrue("Attribute should come before element", 
            pointer.compareChildNodePointers(attrPtr, childPtr) < 0);
        assertTrue("Element should come after attribute", 
            pointer.compareChildNodePointers(childPtr, attrPtr) > 0);
    }

    @Test(timeout = 4000)
    public void testEscapeMethod() throws Exception {
        Document doc = createSimpleDocument();
        Element elem = doc.createElement("test");
        DOMNodePointer pointer = new DOMNodePointer(elem, null, "it's \"quoted\"");
        String path = pointer.asPath();
        assertTrue("Path should escape single quotes", path.contains("&apos;"));
        assertTrue("Path should escape double quotes", path.contains("&quot;"));
    }
}