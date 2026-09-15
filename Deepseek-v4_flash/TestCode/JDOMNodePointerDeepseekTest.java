package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;

import org.jdom.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ri.*;
import org.apache.commons.jxpath.ri.model.*;
import org.apache.commons.jxpath.ri.compiler.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for JDOMNodePointer.
 * Targets maximum line/branch coverage and the known NullPointerException defect in asPath().
 */
public class JDOMNodePointerDeepseekTest {

    // ===================== Helper Methods =====================

    private JXPathContext createContext(Object node) {
        JXPathContext context = JXPathContext.newContext(node);
        return context;
    }

    // ===================== testNode() Tests =====================

    /**
     * @target testNode(NodeTest)
     * @scenario null test
     * @defectRisk NullPointerException if test is null and node is not handled
     */
    @Test(timeout = 4000)
    public void testTestNode_NullTest() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertTrue("Null test should return true", pointer.testNode(null));
    }

    /**
     * @target testNode(NodeTest) with NodeNameTest
     * @scenario Element matching name and namespace
     * @defectRisk Incorrect namespace comparison
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeNameTest_ExactMatch() {
        Element element = new Element("child", "ns", "http://example.com");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("ns", "child"), "http://example.com");
        assertTrue("Element should match exact name test", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with NodeNameTest
     * @scenario Wildcard prefix, no namespace
     * @defectRisk Wildcard matching logic
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeNameTest_WildcardNoPrefix() {
        Element element = new Element("any");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "*"), null);
        test.setWildcard(true);
        assertTrue("Wildcard without prefix should match any element", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with NodeNameTest
     * @scenario Non-element node (Text)
     * @defectRisk Should return false for non-element
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeNameTest_NonElement() {
        Text text = new Text("hello");
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName("test"));
        assertFalse("Text node should not match element name test", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with NodeTypeTest NODE_TYPE_NODE
     * @scenario Element node
     * @defectRisk Incorrect type matching
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_Node_Element() {
        Element element = new Element("root");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue("Element should match NODE type", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with NodeTypeTest NODE_TYPE_TEXT
     * @scenario Text node
     * @defectRisk Text vs CDATA distinction
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_Text_TextNode() {
        Text text = new Text("data");
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue("Text node should match TEXT type", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with NodeTypeTest NODE_TYPE_TEXT
     * @scenario CDATA node
     * @defectRisk CDATA should also match TEXT type
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_Text_CDATANode() {
        CDATA cdata = new CDATA("cdata");
        JDOMNodePointer pointer = new JDOMNodePointer(cdata, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue("CDATA node should match TEXT type", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with NodeTypeTest NODE_TYPE_COMMENT
     * @scenario Comment node
     * @defectRisk Comment type matching
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_Comment() {
        Comment comment = new Comment("note");
        JDOMNodePointer pointer = new JDOMNodePointer(comment, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue("Comment node should match COMMENT type", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with NodeTypeTest NODE_TYPE_PI
     * @scenario ProcessingInstruction node
     * @defectRisk PI type matching
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_PI() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue("PI node should match PI type", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with ProcessingInstructionTest
     * @scenario Matching target
     * @defectRisk PI target comparison
     */
    @Test(timeout = 4000)
    public void testTestNode_ProcessingInstructionTest_Match() {
        ProcessingInstruction pi = new ProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.getDefault());
        ProcessingInstructionTest test = new ProcessingInstructionTest("xml-stylesheet");
        assertTrue("PI should match target", pointer.testNode(test));
    }

    /**
     * @target testNode(NodeTest) with ProcessingInstructionTest
     * @scenario Non-matching target
     * @defectRisk PI target mismatch
     */
    @Test(timeout = 4000)
    public void testTestNode_ProcessingInstructionTest_NoMatch() {
        ProcessingInstruction pi = new ProcessingInstruction("target1", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.getDefault());
        ProcessingInstructionTest test = new ProcessingInstructionTest("target2");
        assertFalse("PI should not match different target", pointer.testNode(test));
    }

    // ===================== asPath() Tests =====================

    /**
     * @target asPath()
     * @scenario Element with default namespace
     * @defectRisk NullPointerException when namespace resolver is missing (JXPATH-12)
     */
    @Test(timeout = 4000)
    public void testAsPath_ElementDefaultNamespace() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        String path = childPointer.asPath();
        assertNotNull("Path should not be null", path);
        assertTrue("Path should contain child", path.contains("child"));
    }

    /**
     * @target asPath()
     * @scenario Element with explicit namespace prefix
     * @defectRisk Incorrect prefix resolution
     */
    @Test(timeout = 4000)
    public void testAsPath_ElementWithNamespace() {
        Namespace ns = Namespace.getNamespace("myns", "http://example.com");
        Element root = new Element("root", ns);
        Element child = new Element("child", ns);
        root.addContent(child);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        String path = childPointer.asPath();
        assertNotNull("Path should not be null", path);
    }

    /**
     * @target asPath()
     * @scenario Text node
     * @defectRisk Incorrect text position calculation
     */
    @Test(timeout = 4000)
    public void testAsPath_TextNode() {
        Element root = new Element("root");
        Text text = new Text("hello");
        root.addContent(text);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer textPointer = new JDOMNodePointer(rootPointer, text);
        String path = textPointer.asPath();
        assertTrue("Path should contain text()", path.contains("text()"));
    }

    /**
     * @target asPath()
     * @scenario CDATA node
     * @defectRisk CDATA treated as text
     */
    @Test(timeout = 4000)
    public void testAsPath_CDATANode() {
        Element root = new Element("root");
        CDATA cdata = new CDATA("cdata content");
        root.addContent(cdata);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer cdataPointer = new JDOMNodePointer(rootPointer, cdata);
        String path = cdataPointer.asPath();
        assertTrue("Path should contain text() for CDATA", path.contains("text()"));
    }

    /**
     * @target asPath()
     * @scenario ProcessingInstruction node
     * @defectRisk PI path construction
     */
    @Test(timeout = 4000)
    public void testAsPath_ProcessingInstruction() {
        Element root = new Element("root");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        root.addContent(pi);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer piPointer = new JDOMNodePointer(rootPointer, pi);
        String path = piPointer.asPath();
        assertTrue("Path should contain processing-instruction", path.contains("processing-instruction"));
    }

    /**
     * @target asPath()
     * @scenario Pointer with ID
     * @defectRisk ID-based path
     */
    @Test(timeout = 4000)
    public void testAsPath_WithId() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault(), "myid");
        String path = pointer.asPath();
        assertTrue("Path should use id()", path.startsWith("id('"));
    }

    // ===================== getValue() Tests =====================

    /**
     * @target getValue()
     * @scenario Element with text
     * @defectRisk getTextTrim() behavior
     */
    @Test(timeout = 4000)
    public void testGetValue_Element() {
        Element element = new Element("test");
        element.setText("  hello  ");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertEquals("Element value should be trimmed text", "hello", pointer.getValue());
    }

    /**
     * @target getValue()
     * @scenario Comment node
     * @defectRisk Comment text trimming
     */
    @Test(timeout = 4000)
    public void testGetValue_Comment() {
        Comment comment = new Comment("  comment  ");
        JDOMNodePointer pointer = new JDOMNodePointer(comment, Locale.getDefault());
        assertEquals("Comment value should be trimmed", "comment", pointer.getValue());
    }

    /**
     * @target getValue()
     * @scenario Text node
     * @defectRisk Text value extraction
     */
    @Test(timeout = 4000)
    public void testGetValue_Text() {
        Text text = new Text("  text  ");
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.getDefault());
        assertEquals("Text value should be trimmed", "text", pointer.getValue());
    }

    /**
     * @target getValue()
     * @scenario CDATA node
     * @defectRisk CDATA value extraction
     */
    @Test(timeout = 4000)
    public void testGetValue_CDATA() {
        CDATA cdata = new CDATA("  cdata  ");
        JDOMNodePointer pointer = new JDOMNodePointer(cdata, Locale.getDefault());
        assertEquals("CDATA value should be trimmed", "cdata", pointer.getValue());
    }

    /**
     * @target getValue()
     * @scenario ProcessingInstruction node
     * @defectRisk PI data extraction
     */
    @Test(timeout = 4000)
    public void testGetValue_ProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "  data  ");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.getDefault());
        assertEquals("PI value should be trimmed data", "data", pointer.getValue());
    }

    // ===================== setValue() Tests =====================

    /**
     * @target setValue(Object)
     * @scenario Set string value on Text node
     * @defectRisk Text replacement
     */
    @Test(timeout = 4000)
    public void testSetValue_Text_String() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.getDefault());
        pointer.setValue("new");
        assertEquals("Text should be updated", "new", text.getText());
    }

    /**
     * @target setValue(Object)
     * @scenario Set empty string on Text node (should remove)
     * @defectRisk Empty string removal
     */
    @Test(timeout = 4000)
    public void testSetValue_Text_EmptyString() {
        Element parent = new Element("parent");
        Text text = new Text("old");
        parent.addContent(text);
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.getDefault());
        pointer.setValue("");
        assertFalse("Text should be removed for empty string", parent.getContent().contains(text));
    }

    /**
     * @target setValue(Object)
     * @scenario Set Element value on Element node
     * @defectRisk Element cloning and content replacement
     */
    @Test(timeout = 4000)
    public void testSetValue_Element_Element() {
        Element target = new Element("target");
        Element source = new Element("source");
        source.setText("value");
        JDOMNodePointer pointer = new JDOMNodePointer(target, Locale.getDefault());
        pointer.setValue(source);
        assertEquals("Target should contain source's content", "value", target.getTextTrim());
    }

    /**
     * @target setValue(Object)
     * @scenario Set Text value on Element node
     * @defectRisk Text content addition
     */
    @Test(timeout = 4000)
    public void testSetValue_Element_Text() {
        Element target = new Element("target");
        Text source = new Text("text value");
        JDOMNodePointer pointer = new JDOMNodePointer(target, Locale.getDefault());
        pointer.setValue(source);
        assertEquals("Target should have text content", "text value", target.getTextTrim());
    }

    /**
     * @target setValue(Object)
     * @scenario Set ProcessingInstruction value on Element node
     * @defectRisk PI cloning
     */
    @Test(timeout = 4000)
    public void testSetValue_Element_PI() {
        Element target = new Element("target");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(target, Locale.getDefault());
        pointer.setValue(pi);
        assertEquals("Target should contain PI", 1, target.getContent().size());
        assertTrue("Content should be PI", target.getContent().get(0) instanceof ProcessingInstruction);
    }

    /**
     * @target setValue(Object)
     * @scenario Set Comment value on Element node
     * @defectRisk Comment cloning
     */
    @Test(timeout = 4000)
    public void testSetValue_Element_Comment() {
        Element target = new Element("target");
        Comment comment = new Comment("note");
        JDOMNodePointer pointer = new JDOMNodePointer(target, Locale.getDefault());
        pointer.setValue(comment);
        assertEquals("Target should contain comment", 1, target.getContent().size());
        assertTrue("Content should be Comment", target.getContent().get(0) instanceof Comment);
    }

    /**
     * @target setValue(Object)
     * @scenario Set string value on Element node
     * @defectRisk String conversion and addition
     */
    @Test(timeout = 4000)
    public void testSetValue_Element_String() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, Locale.getDefault());
        pointer.setValue("string value");
        assertEquals("Target should have text content", "string value", target.getTextTrim());
    }

    // ===================== createAttribute() Tests =====================

    /**
     * @target createAttribute(JXPathContext, QName)
     * @scenario Create attribute with prefix
     * @defectRisk Namespace resolution
     */
    @Test(timeout = 4000)
    public void testCreateAttribute_WithPrefix() {
        Element element = new Element("test", "ns", "http://example.com");
        JXPathContext context = JXPathContext.newContext(element);
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("ns", "attr");
        NodePointer attrPointer = pointer.createAttribute(context, name);
        assertNotNull("Attribute pointer should not be null", attrPointer);
        assertNotNull("Attribute should exist", element.getAttribute("attr", Namespace.getNamespace("ns", "http://example.com")));
    }

    /**
     * @target createAttribute(JXPathContext, QName)
     * @scenario Create attribute without prefix
     * @defectRisk No-namespace attribute creation
     */
    @Test(timeout = 4000)
    public void testCreateAttribute_WithoutPrefix() {
        Element element = new Element("test");
        JXPathContext context = JXPathContext.newContext(element);
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("attr");
        NodePointer attrPointer = pointer.createAttribute(context, name);
        assertNotNull("Attribute pointer should not be null", attrPointer);
        assertNotNull("Attribute should exist", element.getAttribute("attr"));
    }

    /**
     * @target createAttribute(JXPathContext, QName)
     * @scenario Non-element node (should delegate to super)
     * @defectRisk Fallback behavior
     */
    @Test(timeout = 4000)
    public void testCreateAttribute_NonElement() {
        Text text = new Text("test");
        JXPathContext context = JXPathContext.newContext(text);
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.getDefault());
        QName name = new QName("attr");
        try {
            pointer.createAttribute(context, name);
            fail("Should throw exception for non-element");
        } catch (Exception e) {
            // Expected
        }
    }

    // ===================== createChild() Tests =====================

    /**
     * @target createChild(JXPathContext, QName, int)
     * @scenario Create child element
     * @defectRisk Child creation with factory
     */
    @Test(timeout = 4000)
    public void testCreateChild_Element() {
        Element root = new Element("root");
        JXPathContext context = JXPathContext.newContext(root);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                if (parent instanceof Element) {
                    ((Element) parent).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.getDefault());
        QName name = new QName("child");
        NodePointer childPointer = pointer.createChild(context, name, 0);
        assertNotNull("Child pointer should not be null", childPointer);
        assertEquals("Child element should exist", 1, root.getChildren().size());
    }

    /**
     * @target createChild(JXPathContext, QName, int, Object)
     * @scenario Create child with value
     * @defectRisk Child creation with value setting
     */
    @Test(timeout = 4000)
    public void testCreateChild_WithValue() {
        Element root = new Element("root");
        JXPathContext context = JXPathContext.newContext(root);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                if (parent instanceof Element) {
                    ((Element) parent).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.getDefault());
        QName name = new QName("child");
        NodePointer childPointer = pointer.createChild(context, name, 0, "value");
        assertNotNull("Child pointer should not be null", childPointer);
        assertEquals("Child should have value", "value", ((Element) root.getChildren().get(0)).getTextTrim());
    }

    // ===================== getNamespaceURI() Tests =====================

    /**
     * @target getNamespaceURI()
     * @scenario Element with namespace
     * @defectRisk Namespace URI extraction
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURI_ElementWithNamespace() {
        Element element = new Element("test", "ns", "http://example.com");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertEquals("Namespace URI should be returned", "http://example.com", pointer.getNamespaceURI());
    }

    /**
     * @target getNamespaceURI()
     * @scenario Element without namespace
     * @defectRisk Empty namespace handling
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURI_ElementNoNamespace() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertNull("Element without namespace should return null", pointer.getNamespaceURI());
    }

    /**
     * @target getNamespaceURI(String)
     * @scenario Get namespace URI by prefix from Element
     * @defectRisk Prefix resolution
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURI_ByPrefix_Element() {
        Namespace ns = Namespace.getNamespace("myns", "http://myns.com");
        Element element = new Element("test", ns);
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertEquals("Should resolve prefix", "http://myns.com", pointer.getNamespaceURI("myns"));
    }

    /**
     * @target getNamespaceURI(String)
     * @scenario Get namespace URI from Document
     * @defectRisk Document-level namespace resolution
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURI_ByPrefix_Document() {
        Namespace ns = Namespace.getNamespace("docns", "http://docns.com");
        Element root = new Element("root", ns);
        Document doc = new Document(root);
        JDOMNodePointer pointer = new JDOMNodePointer(doc, Locale.getDefault());
        assertEquals("Should resolve prefix from root element", "http://docns.com", pointer.getNamespaceURI("docns"));
    }

    // ===================== compareChildNodePointers() Tests =====================

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario Same node
     * @defectRisk Identity comparison
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_SameNode() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.getDefault());
        JDOMNodePointer childPointer1 = new JDOMNodePointer(parentPointer, child);
        JDOMNodePointer childPointer2 = new JDOMNodePointer(parentPointer, child);
        assertEquals("Same node should return 0", 0, parentPointer.compareChildNodePointers(childPointer1, childPointer2));
    }

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario Attribute vs Element
     * @defectRisk Attribute ordering
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_AttributeBeforeElement() {
        Element parent = new Element("parent");
        parent.setAttribute("attr", "val");
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.getDefault());
        Attribute attr = parent.getAttribute("attr");
        JDOMNodePointer attrPointer = new JDOMNodePointer(parentPointer, attr);
        JDOMNodePointer childPointer = new JDOMNodePointer(parentPointer, child);
        assertTrue("Attribute should come before element", parentPointer.compareChildNodePointers(attrPointer, childPointer) < 0);
    }

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario Two attributes
     * @defectRisk Attribute ordering among themselves
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_TwoAttributes() {
        Element parent = new Element("parent");
        parent.setAttribute("a1", "v1");
        parent.setAttribute("a2", "v2");
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.getDefault());
        Attribute attr1 = parent.getAttribute("a1");
        Attribute attr2 = parent.getAttribute("a2");
        JDOMNodePointer ptr1 = new JDOMNodePointer(parentPointer, attr1);
        JDOMNodePointer ptr2 = new JDOMNodePointer(parentPointer, attr2);
        assertTrue("First attribute should come before second", parentPointer.compareChildNodePointers(ptr1, ptr2) < 0);
    }

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario Two child elements
     * @defectRisk Element ordering
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_TwoElements() {
        Element parent = new Element("parent");
        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        parent.addContent(child1);
        parent.addContent(child2);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.getDefault());
        JDOMNodePointer ptr1 = new JDOMNodePointer(parentPointer, child1);
        JDOMNodePointer ptr2 = new JDOMNodePointer(parentPointer, child2);
        assertTrue("First child should come before second", parentPointer.compareChildNodePointers(ptr1, ptr2) < 0);
    }

    // ===================== isLeaf() Tests =====================

    /**
     * @target isLeaf()
     * @scenario Element with children
     * @defectRisk Leaf detection
     */
    @Test(timeout = 4000)
    public void testIsLeaf_ElementWithChildren() {
        Element parent = new Element("parent");
        parent.addContent(new Element("child"));
        JDOMNodePointer pointer = new JDOMNodePointer(parent, Locale.getDefault());
        assertFalse("Element with children should not be leaf", pointer.isLeaf());
    }

    /**
     * @target isLeaf()
     * @scenario Element without children
     * @defectRisk Leaf detection
     */
    @Test(timeout = 4000)
    public void testIsLeaf_ElementWithoutChildren() {
        Element element = new Element("empty");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertTrue("Element without children should be leaf", pointer.isLeaf());
    }

    /**
     * @target isLeaf()
     * @scenario Text node
     * @defectRisk Leaf detection for text
     */
    @Test(timeout = 4000)
    public void testIsLeaf_Text() {
        Text text = new Text("data");
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.getDefault());
        assertTrue("Text node should be leaf", pointer.isLeaf());
    }

    // ===================== isLanguage() Tests =====================

    /**
     * @target isLanguage(String)
     * @scenario Element with xml:lang attribute
     * @defectRisk Language detection
     */
    @Test(timeout = 4000)
    public void testIsLanguage_Match() {
        Element element = new Element("test");
        element.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertTrue("Should match language", pointer.isLanguage("en"));
    }

    /**
     * @target isLanguage(String)
     * @scenario No xml:lang attribute
     * @defectRisk Fallback to super
     */
    @Test(timeout = 4000)
    public void testIsLanguage_NoAttribute() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertFalse("Should not match without attribute", pointer.isLanguage("en"));
    }

    // ===================== remove() Tests =====================

    /**
     * @target remove()
     * @scenario Remove child element
     * @defectRisk Removal from parent
     */
    @Test(timeout = 4000)
    public void testRemove_ChildElement() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);
        JDOMNodePointer pointer = new JDOMNodePointer(child, Locale.getDefault());
        pointer.remove();
        assertEquals("Parent should have no children", 0, parent.getContent().size());
    }

    /**
     * @target remove()
     * @scenario Remove root element (should throw)
     * @defectRisk Root removal protection
     */
    @Test(timeout = 4000)
    public void testRemove_RootElement() {
        Element root = new Element("root");
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.getDefault());
        try {
            pointer.remove();
            fail("Should throw JXPathException for root node");
        } catch (JXPathException e) {
            // Expected
        }
    }

    // ===================== equals() and hashCode() Tests =====================

    /**
     * @target equals(Object)
     * @scenario Same object
     * @defectRisk Identity check
     */
    @Test(timeout = 4000)
    public void testEquals_SameObject() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertTrue("Should equal itself", pointer.equals(pointer));
    }

    /**
     * @target equals(Object)
     * @scenario Different object with same node
     * @defectRisk Node identity comparison
     */
    @Test(timeout = 4000)
    public void testEquals_DifferentObjectSameNode() {
        Element element = new Element("test");
        JDOMNodePointer pointer1 = new JDOMNodePointer(element, Locale.getDefault());
        JDOMNodePointer pointer2 = new JDOMNodePointer(element, Locale.getDefault());
        assertTrue("Pointers to same node should be equal", pointer1.equals(pointer2));
    }

    /**
     * @target equals(Object)
     * @scenario Different nodes
     * @defectRisk Non-equality
     */
    @Test(timeout = 4000)
    public void testEquals_DifferentNodes() {
        Element element1 = new Element("test1");
        Element element2 = new Element("test2");
        JDOMNodePointer pointer1 = new JDOMNodePointer(element1, Locale.getDefault());
        JDOMNodePointer pointer2 = new JDOMNodePointer(element2, Locale.getDefault());
        assertFalse("Pointers to different nodes should not be equal", pointer1.equals(pointer2));
    }

    /**
     * @target hashCode()
     * @scenario Identity hash code
     * @defectRisk Consistent with equals
     */
    @Test(timeout = 4000)
    public void testHashCode_ConsistentWithEquals() {
        Element element = new Element("test");
        JDOMNodePointer pointer1 = new JDOMNodePointer(element, Locale.getDefault());
        JDOMNodePointer pointer2 = new JDOMNodePointer(element, Locale.getDefault());
        assertEquals("Equal objects should have same hash code", pointer1.hashCode(), pointer2.hashCode());
    }

    // ===================== getName() Tests =====================

    /**
     * @target getName()
     * @scenario Element with namespace prefix
     * @defectRisk Name extraction
     */
    @Test(timeout = 4000)
    public void testGetName_ElementWithPrefix() {
        Element element = new Element("child", "ns", "http://example.com");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        QName name = pointer.getName();
        assertEquals("Prefix should be 'ns'", "ns", name.getPrefix());
        assertEquals("Local name should be 'child'", "child", name.getName());
    }

    /**
     * @target getName()
     * @scenario ProcessingInstruction
     * @defectRisk PI name extraction
     */
    @Test(timeout = 4000)
    public void testGetName_ProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, Locale.getDefault());
        QName name = pointer.getName();
        assertEquals("PI name should be target", "target", name.getName());
    }

    // ===================== getBaseValue() and getImmediateNode() Tests =====================

    /**
     * @target getBaseValue()
     * @scenario Returns underlying node
     * @defectRisk Correct node reference
     */
    @Test(timeout = 4000)
    public void testGetBaseValue() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertSame("Should return the same node", element, pointer.getBaseValue());
    }

    /**
     * @target getImmediateNode()
     * @scenario Returns underlying node
     * @defectRisk Correct node reference
     */
    @Test(timeout = 4000)
    public void testGetImmediateNode() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertSame("Should return the same node", element, pointer.getImmediateNode());
    }

    // ===================== isCollection() and getLength() Tests =====================

    /**
     * @target isCollection()
     * @scenario Always false
     * @defectRisk Collection detection
     */
    @Test(timeout = 4000)
    public void testIsCollection() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertFalse("Should not be a collection", pointer.isCollection());
    }

    /**
     * @target getLength()
     * @scenario Always 1
     * @defectRisk Length calculation
     */
    @Test(timeout = 4000)
    public void testGetLength() {
        Element element = new Element("test");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.getDefault());
        assertEquals("Length should be 1", 1, pointer.getLength());
    }

    // ===================== DEDICATED DEFECT TEST (JXPATH-12) =====================

    /**
     * @target asPath()
     * @scenario Child element pointer without namespace resolver (JXPATH-12)
     * @defectRisk NullPointerException when getNamespaceResolver().getDefaultNamespaceURI() is called
     *            on a child pointer whose parent is a JDOMNodePointer but the resolver is null.
     *            This test MUST fail on the defective version and pass on the fixed version.
     */
    @Test(timeout = 4000)
    public void testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12() {
        // Create JDOM Elements with parent-child structure
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        // Wrap them in parent and child JDOMNodePointers
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);

        // Invoke asPath() - this should NOT throw NullPointerException
        String path = childPointer.asPath();

        // Assert that asPath() completes successfully
        assertNotNull("asPath() should return a non-null string", path);
        assertTrue("Path should contain 'child'", path.contains("child"));
    }
}