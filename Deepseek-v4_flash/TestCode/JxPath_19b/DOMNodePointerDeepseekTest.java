package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.NamedNodeMap;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.jxpath.ri.NamespaceResolver;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.util.Locale;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DOMNodePointer
 * 
 * Known Defect: AliasedNamespaceIterationTest::testIterateDOM
 * - When iterating child nodes with an aliased namespace prefix, the iterator
 *   returns the same element twice instead of distinct sibling elements.
 *   Expected: [/a:doc[1]/a:elem[1], /a:doc[1]/a:elem[2]]
 *   Actual:   [/a:doc[1]/a:elem[1], /a:doc[1]/a:elem[1]]
 * 
 * Branch Coverage Targets:
 * 1. testNode(Node, NodeTest) - null test, NodeNameTest wildcard, namespace matching
 * 2. getNamespaceURI(String) - null/empty prefix, "xml", "xmlns", namespace resolution
 * 3. getDefaultNamespaceURI() - null default, empty default
 * 4. getValue() - COMMENT_NODE, TEXT_NODE, CDATA_SECTION_NODE, PROCESSING_INSTRUCTION_NODE
 * 5. isLanguage(String) - null lang, xml:lang attribute, prefix matching
 * 6. createChild() - WHOLE_COLLECTION, normal index, factory failure
 * 7. createAttribute() - non-Element node, existing attribute, new attribute
 * 8. remove() - root node removal, non-root removal
 * 9. asPath() - id-based, parent-based, text nodes, processing instructions
 * 10. compareChildNodePointers() - attribute vs element ordering, attribute ordering
 * 11. equals()/hashCode() - same node, different node, non-DOMNodePointer
 * 12. getPrefix()/getLocalName() - with/without colon, null node name
 * 13. getNamespaceURI(Node) - Document, Element, Attr, null URI
 * 14. findEnclosingAttribute() - element with/without attribute, non-element
 * 15. getPointerByID() - found element, not found
 * 
 * Boundary Conditions:
 * - null arguments for testNode, getNamespaceURI, isLanguage
 * - empty strings for namespace prefix, language
 * - index = WHOLE_COLLECTION (-1) for createChild
 * - index = 0 for createChild
 * - null value for setValue
 * - empty string value for setValue
 * - node types: ELEMENT, TEXT, CDATA, COMMENT, PI, ATTRIBUTE, DOCUMENT
 * - namespace URI: null, empty, "xml", "xmlns", custom
 */
public class DOMNodePointerDeepseekTest {

    private Document createDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /* ==================== Partition A: Core Functional Logic & State Transitions ==================== */

    @Test(timeout = 4000)
    public void testConstructorWithNodeAndLocale() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNotNull(pointer);
        assertEquals(doc.getDocumentElement(), pointer.getImmediateNode());
        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testConstructorWithParentAndNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer parent = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        DOMNodePointer child = new DOMNodePointer(parent, doc.getDocumentElement().getFirstChild());
        assertNotNull(child);
        assertEquals(doc.getDocumentElement().getFirstChild(), child.getImmediateNode());
        assertEquals(parent, child.getParent());
    }

    @Test(timeout = 4000)
    public void testGetNameForElement() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'><ns:child/></root>");
        Element child = (Element) doc.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(child, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("child", name.getName());
        assertEquals("ns", name.getPrefix());
    }

    @Test(timeout = 4000)
    public void testGetNameForProcessingInstruction() throws Exception {
        Document doc = createDocument("<?target data?><root/>");
        Node pi = doc.getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(pi, Locale.ENGLISH);
        QName name = pointer.getName();
        assertEquals("target", name.getName());
        assertNull(name.getPrefix());
    }

    @Test(timeout = 4000)
    public void testGetBaseValue() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(doc.getDocumentElement(), pointer.getBaseValue());
    }

    @Test(timeout = 4000)
    public void testIsLeafForElementWithChildren() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testIsLeafForTextNode() throws Exception {
        Document doc = createDocument("<root>text</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceResolver() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        NamespaceResolver resolver = pointer.getNamespaceResolver();
        assertNotNull(resolver);
        assertEquals("http://example.com", resolver.getNamespaceURI("ns"));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForNullPrefix() throws Exception {
        Document doc = createDocument("<root xmlns='http://default.com'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNull(pointer.getNamespaceURI(null));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForEmptyPrefix() throws Exception {
        Document doc = createDocument("<root xmlns='http://default.com'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNull(pointer.getNamespaceURI(""));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForXmlPrefix() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, pointer.getNamespaceURI("xml"));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForXmlnsPrefix() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, pointer.getNamespaceURI("xmlns"));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForCustomPrefix() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals("http://example.com", pointer.getNamespaceURI("ns"));
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURI() throws Exception {
        Document doc = createDocument("<root xmlns='http://default.com'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals("http://default.com", pointer.getDefaultNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURIWhenEmpty() throws Exception {
        Document doc = createDocument("<root xmlns=''/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNull(pointer.getDefaultNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURIWhenNoDefault() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNull(pointer.getDefaultNamespaceURI());
    }

    /* ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ==================== */

    @Test(timeout = 4000)
    public void testTestNodeWithNullTest() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertTrue(pointer.testNode(null));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithWildcardNameTest() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNameTestMatching() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName(null, "child"));
        assertTrue(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNameTestNotMatching() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName(null, "other"));
        assertFalse(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNameTestOnNonElement() throws Exception {
        Document doc = createDocument("<root>text</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        NodeNameTest test = new NodeNameTest(new QName(null, "child"));
        assertFalse(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestText() throws Exception {
        Document doc = createDocument("<root>text</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestComment() throws Exception {
        Document doc = createDocument("<root><!--comment--></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithNodeTypeTestPI() throws Exception {
        Document doc = createDocument("<?target data?><root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getFirstChild(), Locale.ENGLISH);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithProcessingInstructionTest() throws Exception {
        Document doc = createDocument("<?target data?><root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getFirstChild(), Locale.ENGLISH);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testTestNodeWithProcessingInstructionTestNotMatching() throws Exception {
        Document doc = createDocument("<?target data?><root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getFirstChild(), Locale.ENGLISH);
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testGetValueForCommentNode() throws Exception {
        Document doc = createDocument("<root><!--  comment  --></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        assertEquals("comment", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueForTextNode() throws Exception {
        Document doc = createDocument("<root>  text  </root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        assertEquals("text", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueForCDATANode() throws Exception {
        Document doc = createDocument("<root><![CDATA[  cdata  ]]></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        assertEquals("cdata", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueForProcessingInstruction() throws Exception {
        Document doc = createDocument("<?target  data  ?><root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getFirstChild(), Locale.ENGLISH);
        assertEquals("data", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueForElementWithText() throws Exception {
        Document doc = createDocument("<root>  text  </root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals("text", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueForElementWithMixedContent() throws Exception {
        Document doc = createDocument("<root>text<child/>more</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals("textmore", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testIsLanguageWithNullLang() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertFalse(pointer.isLanguage(null));
    }

    @Test(timeout = 4000)
    public void testIsLanguageWithXmlLangAttribute() throws Exception {
        Document doc = createDocument("<root xml:lang='en'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
    }

    @Test(timeout = 4000)
    public void testIsLanguageWithPrefixMatch() throws Exception {
        Document doc = createDocument("<root xml:lang='en-US'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
    }

    @Test(timeout = 4000)
    public void testIsLanguageWithNoMatch() throws Exception {
        Document doc = createDocument("<root xml:lang='fr'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertFalse(pointer.isLanguage("en"));
    }

    @Test(timeout = 4000)
    public void testIsLanguageWithInheritedAttribute() throws Exception {
        Document doc = createDocument("<root xml:lang='en'><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        assertTrue(pointer.isLanguage("en"));
    }

    /* ==================== Partition C: Defect-Targeted Branch Zone ==================== */

    /**
     * CRITICAL DEFECT TEST:
     * This test targets the known defect where iterating child nodes with an
     * aliased namespace prefix returns the same element twice instead of
     * distinct sibling elements.
     * 
     * Expected: [/a:doc[1]/a:elem[1], /a:doc[1]/a:elem[2]]
     * Actual (buggy): [/a:doc[1]/a:elem[1], /a:doc[1]/a:elem[1]]
     */
    @Test(timeout = 4000)
    public void testIterateWithAliasedNamespace() throws Exception {
        String xml = "<a:doc xmlns:a='http://example.com/a'>"
                + "<a:elem>1</a:elem>"
                + "<a:elem>2</a:elem>"
                + "</a:doc>";
        Document doc = createDocument(xml);
        
        JXPathContext context = JXPathContext.newContext(doc);
        context.registerNamespace("a", "http://example.com/a");
        
        // Use XPath to iterate over the elements
        Pointer pointer = context.getPointer("/a:doc/a:elem[1]");
        assertNotNull(pointer);
        
        // Get the parent pointer and iterate children
        DOMNodePointer docPointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        
        // Create a NodeNameTest for the aliased namespace
        QName qname = new QName("http://example.com/a", "elem");
        NodeNameTest test = new NodeNameTest(qname);
        
        // Iterate through children
        org.apache.commons.jxpath.ri.model.NodeIterator iterator = 
            docPointer.childIterator(test, false, null);
        
        assertNotNull(iterator);
        
        // First element
        assertTrue(iterator.setPosition(1));
        NodePointer first = iterator.getNodePointer();
        assertNotNull(first);
        String firstPath = first.asPath();
        
        // Second element - this is where the bug manifests
        assertTrue(iterator.setPosition(2));
        NodePointer second = iterator.getNodePointer();
        assertNotNull(second);
        String secondPath = second.asPath();
        
        // The two paths must be different - this is the core assertion
        assertNotEquals("Aliased namespace iteration should return distinct elements", 
            firstPath, secondPath);
        
        // Verify the actual paths
        assertEquals("/a:doc[1]/a:elem[1]", firstPath);
        assertEquals("/a:doc[1]/a:elem[2]", secondPath);
    }

    @Test(timeout = 4000)
    public void testIterateWithAliasedNamespaceUsingContext() throws Exception {
        String xml = "<a:doc xmlns:a='http://example.com/a'>"
                + "<a:elem>1</a:elem>"
                + "<a:elem>2</a:elem>"
                + "</a:doc>";
        Document doc = createDocument(xml);
        
        JXPathContext context = JXPathContext.newContext(doc);
        context.registerNamespace("a", "http://example.com/a");
        
        // Iterate using XPath
        java.util.Iterator<?> iterator = context.iteratePointers("/a:doc/a:elem");
        
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        Pointer first = (Pointer) iterator.next();
        assertTrue(iterator.hasNext());
        Pointer second = (Pointer) iterator.next();
        
        assertNotEquals("Aliased namespace iteration should return distinct elements",
            first.asPath(), second.asPath());
        
        assertEquals("/a:doc[1]/a:elem[1]", first.asPath());
        assertEquals("/a:doc[1]/a:elem[2]", second.asPath());
    }

    /* ==================== Partition D: Exception & Defensive Guard Paths ==================== */

    @Test(timeout = 4000)
    public void testCreateChildWithWholeCollection() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        
        NodePointer child = pointer.createChild(context, new QName(null, "child"), 
            NodePointer.WHOLE_COLLECTION);
        assertNotNull(child);
        assertEquals("child", child.getName().getName());
    }

    @Test(timeout = 4000)
    public void testCreateChildWithIndex() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        
        NodePointer child = pointer.createChild(context, new QName(null, "child"), 0);
        assertNotNull(child);
        assertEquals("child", child.getName().getName());
    }

    @Test(timeout = 4000)
    public void testCreateChildWithValue() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        
        NodePointer child = pointer.createChild(context, new QName(null, "child"), 0, "value");
        assertNotNull(child);
        assertEquals("value", child.getValue());
    }

    @Test(timeout = 4000)
    public void testCreateAttributeOnNonElement() throws Exception {
        Document doc = createDocument("<root>text</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        
        NodePointer attr = pointer.createAttribute(context, new QName(null, "attr"));
        assertNotNull(attr);
    }

    @Test(timeout = 4000)
    public void testCreateAttributeOnElement() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        
        NodePointer attr = pointer.createAttribute(context, new QName(null, "attr"));
        assertNotNull(attr);
        assertEquals("attr", attr.getName().getName());
    }

    @Test(timeout = 4000)
    public void testCreateAttributeWithNamespace() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        context.registerNamespace("ns", "http://example.com/ns");
        
        NodePointer attr = pointer.createAttribute(context, new QName("http://example.com/ns", "attr"));
        assertNotNull(attr);
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testRemoveRootNode() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        pointer.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveNonRootNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer parent = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        DOMNodePointer child = new DOMNodePointer(parent, doc.getDocumentElement().getFirstChild());
        child.remove();
        assertEquals(0, doc.getDocumentElement().getChildNodes().getLength());
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextNode() throws Exception {
        Document doc = createDocument("<root>old</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        pointer.setValue("new");
        assertEquals("new", doc.getDocumentElement().getFirstChild().getNodeValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithString() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        pointer.setValue("new");
        assertEquals("new", doc.getDocumentElement().getTextContent());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithNode() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        Element newChild = doc.createElement("newChild");
        pointer.setValue(newChild);
        assertEquals("newChild", doc.getDocumentElement().getFirstChild().getNodeName());
    }

    @Test(timeout = 4000)
    public void testSetValueWithNull() throws Exception {
        Document doc = createDocument("<root>text</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        pointer.setValue(null);
        assertNull(doc.getDocumentElement().getFirstChild().getNodeValue());
    }

    @Test(timeout = 4000)
    public void testSetValueWithEmptyString() throws Exception {
        Document doc = createDocument("<root>text</root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        pointer.setValue("");
        assertNull(doc.getDocumentElement().getFirstChild().getNodeValue());
    }

    /* ==================== Partition E: Object Lifecycle & Contract Integrity ==================== */

    @Test(timeout = 4000)
    public void testEqualsWithSameObject() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(pointer, pointer);
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameNode() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer1 = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        DOMNodePointer pointer2 = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(pointer1, pointer2);
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentNode() throws Exception {
        Document doc = createDocument("<root><child1/><child2/></root>");
        DOMNodePointer pointer1 = new DOMNodePointer(doc.getDocumentElement().getFirstChild(), Locale.ENGLISH);
        DOMNodePointer pointer2 = new DOMNodePointer(doc.getDocumentElement().getLastChild(), Locale.ENGLISH);
        assertNotEquals(pointer1, pointer2);
    }

    @Test(timeout = 4000)
    public void testEqualsWithNonDOMNodePointer() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNotEquals(pointer, "not a pointer");
    }

    @Test(timeout = 4000)
    public void testHashCode() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer1 = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        DOMNodePointer pointer2 = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(pointer1.hashCode(), pointer2.hashCode());
    }

    @Test(timeout = 4000)
    public void testGetPrefixWithColon() throws Exception {
        Document doc = createDocument("<ns:root xmlns:ns='http://example.com'/>");
        assertEquals("ns", DOMNodePointer.getPrefix(doc.getDocumentElement()));
    }

    @Test(timeout = 4000)
    public void testGetPrefixWithoutColon() throws Exception {
        Document doc = createDocument("<root/>");
        assertNull(DOMNodePointer.getPrefix(doc.getDocumentElement()));
    }

    @Test(timeout = 4000)
    public void testGetLocalNameWithColon() throws Exception {
        Document doc = createDocument("<ns:root xmlns:ns='http://example.com'/>");
        assertEquals("root", DOMNodePointer.getLocalName(doc.getDocumentElement()));
    }

    @Test(timeout = 4000)
    public void testGetLocalNameWithoutColon() throws Exception {
        Document doc = createDocument("<root/>");
        assertEquals("root", DOMNodePointer.getLocalName(doc.getDocumentElement()));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForElement() throws Exception {
        Document doc = createDocument("<ns:root xmlns:ns='http://example.com'/>");
        assertEquals("http://example.com", DOMNodePointer.getNamespaceURI(doc.getDocumentElement()));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForDocument() throws Exception {
        Document doc = createDocument("<root/>");
        assertNull(DOMNodePointer.getNamespaceURI(doc));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForAttr() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com' ns:attr='value'/>");
        Attr attr = doc.getDocumentElement().getAttributeNodeNS("http://example.com", "attr");
        assertEquals("http://example.com", DOMNodePointer.getNamespaceURI(attr));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIForNullNode() throws Exception {
        assertNull(DOMNodePointer.getNamespaceURI(null));
    }

    @Test(timeout = 4000)
    public void testFindEnclosingAttribute() throws Exception {
        Document doc = createDocument("<root xml:lang='en'><child/></root>");
        assertEquals("en", DOMNodePointer.findEnclosingAttribute(
            doc.getDocumentElement().getFirstChild(), "xml:lang"));
    }

    @Test(timeout = 4000)
    public void testFindEnclosingAttributeNotFound() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        assertNull(DOMNodePointer.findEnclosingAttribute(
            doc.getDocumentElement().getFirstChild(), "xml:lang"));
    }

    @Test(timeout = 4000)
    public void testFindEnclosingAttributeOnNonElement() throws Exception {
        Document doc = createDocument("<root>text</root>");
        assertNull(DOMNodePointer.findEnclosingAttribute(
            doc.getDocumentElement().getFirstChild(), "xml:lang"));
    }

    @Test(timeout = 4000)
    public void testGetPointerByIDFound() throws Exception {
        Document doc = createDocument("<root><child id='test'/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        
        Pointer result = pointer.getPointerByID(context, "test");
        assertNotNull(result);
        assertEquals("child", result.getName().getName());
    }

    @Test(timeout = 4000)
    public void testGetPointerByIDNotFound() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        
        Pointer result = pointer.getPointerByID(context, "nonexistent");
        assertNotNull(result);
        assertTrue(result instanceof NullPointer);
    }

    @Test(timeout = 4000)
    public void testAsPathForRootElement() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals("/root[1]", pointer.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathForChildElement() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer parent = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        DOMNodePointer child = new DOMNodePointer(parent, doc.getDocumentElement().getFirstChild());
        assertEquals("/root[1]/child[1]", child.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathForTextNode() throws Exception {
        Document doc = createDocument("<root>text</root>");
        DOMNodePointer parent = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        DOMNodePointer text = new DOMNodePointer(parent, doc.getDocumentElement().getFirstChild());
        assertEquals("/root[1]/text()[1]", text.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathForProcessingInstruction() throws Exception {
        Document doc = createDocument("<?target data?><root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getFirstChild(), Locale.ENGLISH);
        assertEquals("/processing-instruction('target')[1]", pointer.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathWithId() throws Exception {
        Document doc = createDocument("<root><child id='test'/></root>");
        DOMNodePointer pointer = new DOMNodePointer(
            doc.getDocumentElement().getFirstChild(), Locale.ENGLISH, "test");
        assertEquals("id('test')", pointer.asPath());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = createDocument("<root><child1/><child2/></root>");
        DOMNodePointer parent = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        DOMNodePointer child1 = new DOMNodePointer(parent, doc.getDocumentElement().getFirstChild());
        DOMNodePointer child2 = new DOMNodePointer(parent, doc.getDocumentElement().getLastChild());
        
        assertTrue(parent.compareChildNodePointers(child1, child2) < 0);
        assertTrue(parent.compareChildNodePointers(child2, child1) > 0);
        assertEquals(0, parent.compareChildNodePointers(child1, child1));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointersAttributeVsElement() throws Exception {
        Document doc = createDocument("<root attr='value'><child/></root>");
        DOMNodePointer parent = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        
        Attr attr = doc.getDocumentElement().getAttributeNode("attr");
        DOMNodePointer attrPointer = new DOMNodePointer(parent, attr);
        DOMNodePointer childPointer = new DOMNodePointer(parent, doc.getDocumentElement().getFirstChild());
        
        assertTrue(parent.compareChildNodePointers(attrPointer, childPointer) < 0);
        assertTrue(parent.compareChildNodePointers(childPointer, attrPointer) > 0);
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointersAttributes() throws Exception {
        Document doc = createDocument("<root attr2='2' attr1='1'/>");
        DOMNodePointer parent = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        
        Attr attr1 = doc.getDocumentElement().getAttributeNode("attr1");
        Attr attr2 = doc.getDocumentElement().getAttributeNode("attr2");
        DOMNodePointer attr1Pointer = new DOMNodePointer(parent, attr1);
        DOMNodePointer attr2Pointer = new DOMNodePointer(parent, attr2);
        
        assertTrue(parent.compareChildNodePointers(attr1Pointer, attr2Pointer) < 0);
        assertTrue(parent.compareChildNodePointers(attr2Pointer, attr1Pointer) > 0);
    }

    @Test(timeout = 4000)
    public void testNamespacePointer() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        NodePointer nsPointer = pointer.namespacePointer("ns");
        assertNotNull(nsPointer);
        assertEquals("ns", nsPointer.getName().getName());
    }

    @Test(timeout = 4000)
    public void testNamespaceIterator() throws Exception {
        Document doc = createDocument("<root xmlns:ns='http://example.com'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        org.apache.commons.jxpath.ri.model.NodeIterator iterator = pointer.namespaceIterator();
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testAttributeIterator() throws Exception {
        Document doc = createDocument("<root attr='value'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        org.apache.commons.jxpath.ri.model.NodeIterator iterator = pointer.attributeIterator(new QName(null, "attr"));
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testChildIterator() throws Exception {
        Document doc = createDocument("<root><child/></root>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        org.apache.commons.jxpath.ri.model.NodeIterator iterator = pointer.childIterator(null, false, null);
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNode() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(doc.getDocumentElement(), pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testIsActual() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertTrue(pointer.isActual());
    }

    @Test(timeout = 4000)
    public void testIsCollection() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testGetLength() throws Exception {
        Document doc = createDocument("<root/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals(1, pointer.getLength());
    }
}