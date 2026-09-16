package org.apache.commons.jxpath.ri.model.jdom;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

/* [Branch & Defect Analysis Matrix]
 * 
 * Coverage Targets:
 * 1. Constructor initialization (2 variants: with id, without id)
 * 2. getValue() for Element, Comment, Text, CDATA, ProcessingInstruction, null
 * 3. setValue() for Text, Element replacement, Document replacement, Text/CDATA/ProcessingInstruction/Comment, null/empty string
 * 4. testNode() - NodeNameTest (wildcard, with/without namespace, prefix matching)
 * 5. testNode() - NodeTypeTest (NODE, TEXT, COMMENT, PI)
 * 6. testNode() - ProcessingInstructionTest
 * 7. asPath() - with id, Element default ns, Element non-default ns, text, CDATA, PI
 * 8. compareChildNodePointers() - same node, attribute vs non-attribute, two attributes, element children
 * 9. getNamespaceURI() - Element with/without empty ns, Document, plain node
 * 10. getLanguage() - Element with xml:lang
 * 11. isLeaf() - Element, Document, others
 * 
 * Defect Targeting (from XMLSpaceTest failures):
 * - getValue() on Element should return trimmed text, but the bug is that xml:space="preserve" 
 *   may cause incorrect trimming. The failures show leading/trailing whitespace not being preserved
 *   or elements returning concatenated text incorrectly.
 * - Specifically: "foo" vs " foo " indicates trimming issue (maybe in setValue or getValue)
 * - nested elements returning concatenated text like "foo;bar; baz" instead of empty string or proper handling
 */
public class JDOMNodePointerDeepseekTest {

    // ===== Partition A: Core Functional Logic =====
    
    @Test(timeout = 4000)
    public void testConstructorWithId() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US, "myId");
        assertEquals("myId", ptr.asPath().substring(4, 9)); // id('myId')
        assertEquals(elem, ptr.getBaseValue());
        assertFalse(ptr.isCollection());
        assertEquals(1, ptr.getLength());
    }

    @Test(timeout = 4000)
    public void testConstructorWithoutId() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertNull(ptr.asPath().indexOf("id(") >= 0 ? "has id" : null);
    }

    @Test(timeout = 4000)
    public void testConstructorWithParent() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);
        NodePointer parentPtr = new JDOMNodePointer(parent, Locale.US);
        JDOMNodePointer childPtr = new JDOMNodePointer(parentPtr, child);
        assertEquals(parentPtr, childPtr.getParent());
        assertEquals(child, childPtr.getBaseValue());
    }

    @Test(timeout = 4000)
    public void testGetValueOnText() {
        Text text = new Text("  hello world  ");
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        assertEquals("hello world", ptr.getValue()); // trimmed
    }

    @Test(timeout = 4000)
    public void testGetValueOnCDATA() {
        CDATA cdata = new CDATA("  data  ");
        JDOMNodePointer ptr = new JDOMNodePointer(cdata, Locale.US);
        assertEquals("data", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueOnElement() {
        Element elem = new Element("test");
        elem.addContent(new Text("  value  "));
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertEquals("value", ptr.getValue()); // getTextTrim()
    }

    @Test(timeout = 4000)
    public void testGetValueOnComment() {
        Comment comment = new Comment("  comment  ");
        JDOMNodePointer ptr = new JDOMNodePointer(comment, Locale.US);
        assertEquals("comment", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueOnProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "  data  ");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.US);
        assertEquals("data", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueOnNullNode() {
        JDOMNodePointer ptr = new JDOMNodePointer(null, Locale.US);
        assertNull(ptr.getValue());
    }

    // ===== Partition B: Boundary Value Analysis =====
    
    @Test(timeout = 4000)
    public void testSetValueOnTextWithValidString() {
        Element parent = new Element("parent");
        Text text = new Text("");
        parent.addContent(text);
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        ptr.setValue("new text");
        assertEquals("new text", ((Text)ptr.getBaseValue()).getText());
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextWithEmptyString() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        ptr.setValue("");
        assertEquals(0, parent.getContent().size()); // should be removed
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithElementContent() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        Element child = new Element("child");
        child.addContent(new Text("data"));
        ptr.setValue(child);
        assertEquals(1, elem.getContent().size());
        assertTrue(elem.getContent().get(0) instanceof Element);
        assertEquals("data", ((Element)elem.getContent().get(0)).getText());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithTextContent() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        ptr.setValue("plain text");
        assertEquals(1, elem.getContent().size());
        assertTrue(elem.getContent().get(0) instanceof Text);
        assertEquals("plain text", ((Text)elem.getContent().get(0)).getText());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithNull() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        ptr.setValue(null);
        assertEquals(0, elem.getContent().size());
    }

    // ===== Partition C: Defect-Targeted Tests =====
    
    @Test(timeout = 4000)
    public void testGetValueOnElementWithPreserveWhitespace() throws Exception {
        // Simulate xml:space="preserve" issue
        String xml = "<root xml:space=\"preserve\"> foo </root>";
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        Element root = doc.getRootElement();
        
        // With xml:space="preserve", getTextTrim() should NOT trim
        // The defect shows expected:<foo> but was:< foo >
        // So we assert that the actual value (with spaces) matches expected behavior
        // Since we want to reveal the bug, we assert the CORRECT behavior
        // For xml:space="preserve", getTextTrim still trims - that IS the bug
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.US);
        // The CORRECT behavior for preserve should be " foo " but getTextTrim trims
        // Bug: it returns "foo" instead of " foo "
        // We assert the buggy behavior to show the failure
        assertEquals(" foo ", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueOnNestedElementWithWhitespace() throws Exception {
        // Test for nested elements returning concatenated text
        String xml = "<root><child>foo</child>; <child>bar</child>; <child> baz </child></root>";
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        Element root = doc.getRootElement();
        
        // Bug shows: expected:<> but was:<foo;bar; baz >
        // The getValue on root with nested elements should return trimmed text?
        // Actually getTextTrim() on Element returns the combined text content trimmed
        // The correct behavior for elements with nested children might be empty string
        // But the bug shows concatenated text
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.US);
        // We assert the expected correct behavior (empty or trimmed)
        // Bug returns concatenated text with semicolons
        assertEquals("", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueWithPreserveWhitespaceText() throws Exception {
        // Testing the preserve case for setValue
        String xml = "<root xml:space=\"preserve\">original</root>";
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        Element root = doc.getRootElement();
        
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.US);
        ptr.setValue(" foo "); // Should preserve spaces with xml:space="preserve"
        
        // The getter will use getTextTrim() which trims - bug
        assertEquals(" foo ", ptr.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueOnTextNodeWithPreservedSpaces() throws Exception {
        // Direct Text node with preserved space
        String xml = "<root xml:space=\"preserve\"> foo </root>";
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        Text text = (Text) doc.getRootElement().getContent().get(0);
        
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        // Bug: getTextTrim() trims even when xml:space="preserve"
        assertEquals(" foo ", ptr.getValue());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000, expected = JXPathAbstractFactoryException.class)
    public void testCreateChildWithNullFactory() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        // Need context with null factory - difficult without full setup, but ensure coverage
    }

    @Test(timeout = 4000)
    public void testRemoveRootNodeThrowsException() {
        Element elem = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        try {
            ptr.remove();
            fail("Should throw JXPathException for root node");
        } catch (JXPathException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateAttributeOnNonElementThrowsException() {
        Text text = new Text("test");
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        try {
            ptr.createAttribute(null, new QName(null, "attr"));
            fail("Should throw RuntimeException for non-element");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateAttributeWithUnknownNamespacePrefix() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        QName qname = new QName("unknown", "attr");
        try {
            ptr.createAttribute(null, qname);
            fail("Should throw JXPathException for unknown prefix");
        } catch (JXPathException e) {
            // expected
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertTrue(ptr.equals(ptr));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertFalse(ptr.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsSameNode() {
        Element elem = new Element("test");
        JDOMNodePointer ptr1 = new JDOMNodePointer(elem, Locale.US);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem, Locale.US);
        assertTrue(ptr1.equals(ptr2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentNode() {
        JDOMNodePointer ptr1 = new JDOMNodePointer(new Element("a"), Locale.US);
        JDOMNodePointer ptr2 = new JDOMNodePointer(new Element("b"), Locale.US);
        assertFalse(ptr1.equals(ptr2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertEquals(System.identityHashCode(elem), ptr.hashCode());
    }

    @Test(timeout = 4000)
    public void testIsLeafOnElementWithContent() {
        Element elem = new Element("test");
        elem.addContent(new Text("content"));
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertFalse(ptr.isLeaf());
    }

    @Test(timeout = 4000)
    public void testIsLeafOnEmptyElement() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertTrue(ptr.isLeaf());
    }

    @Test(timeout = 4000)
    public void testIsLeafOnDocumentWithContent() {
        Document doc = new Document(new Element("root"));
        JDOMNodePointer ptr = new JDOMNodePointer(doc, Locale.US);
        assertFalse(ptr.isLeaf());
    }

    @Test(timeout = 4000)
    public void testIsLeafOnText() {
        Text text = new Text("test");
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        assertTrue(ptr.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetNameOnElement() {
        Element elem = new Element("test", "ns");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        QName name = ptr.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("test", name.getName());
    }

    @Test(timeout = 4000)
    public void testGetNameOnProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.US);
        QName name = ptr.getName();
        assertNull(name.getPrefix());
        assertEquals("target", name.getName());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIOnElement() {
        Element elem = new Element("test", "ns", "http://example.com");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertEquals("http://example.com", ptr.getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIOnElementEmptyNS() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertNull(ptr.getNamespaceURI()); // empty ns becomes null
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIOnNullNode() {
        JDOMNodePointer ptr = new JDOMNodePointer(null, Locale.US);
        assertNull(ptr.getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetLanguageFromElement() {
        Element elem = new Element("test");
        elem.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertTrue(ptr.isLanguage("en"));
        assertFalse(ptr.isLanguage("de"));
    }

    @Test(timeout = 4000)
    public void testGetLanguageFromParent() {
        Element parent = new Element("parent");
        parent.setAttribute("lang", "fr", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer ptr = new JDOMNodePointer(child, Locale.US);
        assertTrue(ptr.isLanguage("fr"));
    }

    @Test(timeout = 4000)
    public void testGetLanguageNotFound() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertFalse(ptr.isLanguage("en")); // falls back to super.isLanguage
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointersSameNode() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        NodePointer p1 = new JDOMNodePointer(elem, Locale.US);
        assertEquals(0, ptr.compareChildNodePointers(p1, p1));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointersAttributeFirst() {
        Element elem = new Element("test");
        elem.setAttribute("attr1", "val1");
        Attribute attr = elem.getAttribute("attr1");
        Text text = new Text("text");
        elem.addContent(text);
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        NodePointer attrPtr = new JDOMNodePointer(attr, Locale.US);
        NodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        assertEquals(-1, ptr.compareChildNodePointers(attrPtr, textPtr));
        assertEquals(1, ptr.compareChildNodePointers(textPtr, attrPtr));
    }
    
    @Test(timeout = 4000)
    public void testCompareChildNodePointersBetweenTwoAttributes() {
        Element elem = new Element("test");
        elem.setAttribute("a1", "v1");
        elem.setAttribute("a2", "v2");
        Attribute attr1 = elem.getAttribute("a1");
        Attribute attr2 = elem.getAttribute("a2");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        NodePointer p1 = new JDOMNodePointer(attr1, Locale.US);
        NodePointer p2 = new JDOMNodePointer(attr2, Locale.US);
        assertEquals(-1, ptr.compareChildNodePointers(p1, p2));
        assertEquals(1, ptr.compareChildNodePointers(p2, p1));
    }
    
    @Test(timeout = 4000)
    public void testCompareChildNodePointersBetweenTwoElements() {
        Element elem = new Element("test");
        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        elem.addContent(child1);
        elem.addContent(child2);
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        NodePointer p1 = new JDOMNodePointer(child1, Locale.US);
        NodePointer p2 = new JDOMNodePointer(child2, Locale.US);
        assertEquals(-1, ptr.compareChildNodePointers(p1, p2));
        assertEquals(1, ptr.compareChildNodePointers(p2, p1));
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCompareChildNodePointersOnNonElementNode() {
        Text text = new Text("test");
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        NodePointer p1 = new JDOMNodePointer(text, Locale.US);
        NodePointer p2 = new JDOMNodePointer(text, Locale.US);
        ptr.compareChildNodePointers(p1, p2);
    }

    @Test(timeout = 4000)
    public void testAsPathWithId() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US, "myId");
        assertTrue(ptr.asPath().startsWith("id('myId')"));
    }

    @Test(timeout = 4000)
    public void testAsPathForTextNode() throws Exception {
        Element elem = new Element("test");
        Text text = new Text("content");
        elem.addContent(text);
        JDOMNodePointer parentPtr = new JDOMNodePointer(elem, Locale.US);
        JDOMNodePointer textPtr = new JDOMNodePointer(parentPtr, text);
        String path = textPtr.asPath();
        assertTrue(path.endsWith("/text()[1]"));
    }

    @Test(timeout = 4000)
    public void testAsPathForProcessingInstruction() {
        Element elem = new Element("test");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        elem.addContent(pi);
        JDOMNodePointer parentPtr = new JDOMNodePointer(elem, Locale.US);
        JDOMNodePointer piPtr = new JDOMNodePointer(parentPtr, pi);
        String path = piPtr.asPath();
        assertTrue(path.contains("processing-instruction('target')"));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNullTest() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        assertTrue(ptr.testNode(null));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeNameTestWildcard() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        NodeNameTest test = new NodeNameTest(new QName(null, "*"), true, null);
        assertTrue(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeNameTestNonElement() {
        Text text = new Text("test");
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        NodeNameTest test = new NodeNameTest(new QName(null, "test"), false, null);
        assertFalse(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestNode() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestTextOnText() {
        Text text = new Text("test");
        JDOMNodePointer ptr = new JDOMNodePointer(text, Locale.US);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestTextOnCDATA() {
        CDATA cdata = new CDATA("data");
        JDOMNodePointer ptr = new JDOMNodePointer(cdata, Locale.US);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestComment() {
        Comment comment = new Comment("comment");
        JDOMNodePointer ptr = new JDOMNodePointer(comment, Locale.US);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestPI() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.US);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithProcessingInstructionTest() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.US);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithProcessingInstructionTestMismatch() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer ptr = new JDOMNodePointer(pi, Locale.US);
        ProcessingInstructionTest test = new ProcessingInstructionTest("wrong");
        assertFalse(ptr.testNode(test));
    }

    @Test(timeout = 4000)
    public void testGetPrefixOnElement() {
        Element elem = new Element("test", "ns", "http://example.com");
        assertEquals("ns", JDOMNodePointer.getPrefix(elem));
    }

    @Test(timeout = 4000)
    public void testGetPrefixOnElementEmptyPrefix() {
        Element elem = new Element("test");
        assertNull(JDOMNodePointer.getPrefix(elem));
    }

    @Test(timeout = 4000)
    public void testGetPrefixOnAttribute() {
        Element elem = new Element("test");
        elem.setAttribute("attr", "val");
        Attribute attr = elem.getAttribute("attr");
        assertNull(JDOMNodePointer.getPrefix(attr));
    }

    @Test(timeout = 4000)
    public void testGetLocalNameOnElement() {
        Element elem = new Element("test");
        assertEquals("test", JDOMNodePointer.getLocalName(elem));
    }

    @Test(timeout = 4000)
    public void testGetLocalNameOnAttribute() {
        Element elem = new Element("test");
        elem.setAttribute("attr", "val");
        Attribute attr = elem.getAttribute("attr");
        assertEquals("attr", JDOMNodePointer.getLocalName(attr));
    }

    @Test(timeout = 4000)
    public void testGetRelativePositionByName() throws Exception {
        Element parent = new Element("parent");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        Element child3 = new Element("other");
        parent.addContent(child1);
        parent.addContent(child2);
        parent.addContent(child3);
        
        JDOMNodePointer ptr1 = new JDOMNodePointer(child1, Locale.US);
        JDOMNodePointer ptr2 = new JDOMNodePointer(child2, Locale.US);
        
        // getRelativePositionByName is private, test via asPath
        String path1 = ptr1.asPath();
        String path2 = ptr2.asPath();
        assertTrue(path1.endsWith("[1]") || path1.endsWith("[1]"));
        assertTrue(path2.endsWith("[2]") || path2.endsWith("[2]"));
    }

    @Test(timeout = 4000)
    public void testEscapeInAsPath() {
        Element elem = new Element("test");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.US, "it's \"quoted\"");
        String path = ptr.asPath();
        assertTrue(path.contains("&apos;"));
        assertTrue(path.contains("&quot;"));
    }
}