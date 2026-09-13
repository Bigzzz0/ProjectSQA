package org.apache.commons.jxpath.ri.model.jdom;

import java.util.*;

import org.jdom.*;

import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ri.*;
import org.apache.commons.jxpath.ri.model.*;
import org.apache.commons.jxpath.ri.compiler.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class JDOMNodePointerClaudeTest {

    private final Locale locale = Locale.getDefault();

    // ---------------------------------------------------------------
    // CRITICAL DEFECT-TARGETED TEST (JXPATH-12 style NPE)
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#asPath()
     * @scenario Build a parent/child JDOMNodePointer chain (both raw, no
     * JXPathContext, hence no NamespaceResolver explicitly wired) and call
     * asPath() on the child pointer. The buggy version dereferences a null
     * NamespaceResolver via getNamespaceResolver().getDefaultNamespaceURI()
     * causing a NullPointerException.
     * @defectRisk NullPointerException in asPath() when NamespaceResolver
     * has not been lazily initialized (JXPATH-12 defect).
     */
    @Test(timeout = 4000)
    public void testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);

        String path;
        try {
            path = childPointer.asPath();
        }
        catch (NullPointerException npe) {
            fail("asPath() threw NullPointerException - JXPATH-12 defect present: " + npe);
            return;
        }
        assertNotNull(path);
    }

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer(Object,Locale)
     * @scenario Basic construction and getBaseValue()/getImmediateNode()
     * @defectRisk Constructor not storing node reference correctly
     */
    @Test(timeout = 4000)
    public void testConstructorNodeLocale() {
        Element e = new Element("foo");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertSame(e, p.getBaseValue());
        assertSame(e, p.getImmediateNode());
    }

    /**
     * @target JDOMNodePointer(Object,Locale,String)
     * @scenario Construction with id, used by asPath() id(...) branch
     * @defectRisk id field ignored or escaped incorrectly
     */
    @Test(timeout = 4000)
    public void testConstructorNodeLocaleId() {
        Element e = new Element("foo");
        JDOMNodePointer p = new JDOMNodePointer(e, locale, "abc");
        assertEquals("id('abc')", p.asPath());
    }

    /**
     * @target JDOMNodePointer(NodePointer,Object)
     * @scenario Construction with parent pointer
     * @defectRisk parent field not set, node field not set
     */
    @Test(timeout = 4000)
    public void testConstructorParentNode() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        assertSame(child, childPointer.getBaseValue());
    }

    // ---------------------------------------------------------------
    // Iterators
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#childIterator(NodeTest,boolean,NodePointer)
     * @scenario Element with child content
     * @defectRisk Returned iterator null or wrong type
     */
    @Test(timeout = 4000)
    public void testChildIterator() {
        Element root = new Element("root");
        root.addContent(new Element("child"));
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        NodeIterator it = p.childIterator(null, false, null);
        assertNotNull(it);
    }

    /**
     * @target JDOMNodePointer#attributeIterator(QName)
     * @scenario Element with attribute
     * @defectRisk Returned iterator null
     */
    @Test(timeout = 4000)
    public void testAttributeIterator() {
        Element root = new Element("root");
        root.setAttribute("attr", "val");
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        NodeIterator it = p.attributeIterator(new QName(null, "attr"));
        assertNotNull(it);
    }

    /**
     * @target JDOMNodePointer#namespaceIterator()
     * @scenario Basic element
     * @defectRisk Returned iterator null
     */
    @Test(timeout = 4000)
    public void testNamespaceIterator() {
        Element root = new Element("root");
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        NodeIterator it = p.namespaceIterator();
        assertNotNull(it);
    }

    /**
     * @target JDOMNodePointer#namespacePointer(String)
     * @scenario Basic element with prefix argument
     * @defectRisk Null return or wrong pointer type
     */
    @Test(timeout = 4000)
    public void testNamespacePointer() {
        Element root = new Element("root");
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        NodePointer np = p.namespacePointer("pre");
        assertNotNull(np);
    }

    // ---------------------------------------------------------------
    // getNamespaceURI()
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#getNamespaceURI()
     * @scenario Element with explicit namespace
     * @defectRisk Namespace URI not returned correctly
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURI_WithNamespace() {
        Namespace ns = Namespace.getNamespace("ns", "http://test.com");
        Element e = new Element("foo", ns);
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertEquals("http://test.com", p.getNamespaceURI());
    }

    /**
     * @target JDOMNodePointer#getNamespaceURI()
     * @scenario Element without namespace (empty string URI normalized to null)
     * @defectRisk Empty string not converted to null
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURI_NoNamespace() {
        Element e = new Element("foo");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertNull(p.getNamespaceURI());
    }

    /**
     * @target JDOMNodePointer#getNamespaceURI()
     * @scenario Non element node returns null
     * @defectRisk Wrong handling of non-Element node types
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURI_NonElement() {
        Text t = new Text("hello");
        JDOMNodePointer p = new JDOMNodePointer(t, locale);
        assertNull(p.getNamespaceURI());
    }

    // ---------------------------------------------------------------
    // getNamespaceURI(String prefix)
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#getNamespaceURI(String)
     * @scenario Document node, prefix resolved through root element
     * @defectRisk Document branch not handled correctly
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIPrefix_Document() {
        Element root = new Element("root");
        root.addNamespaceDeclaration(Namespace.getNamespace("ns1", "http://test1.com"));
        Document doc = new Document(root);
        JDOMNodePointer p = new JDOMNodePointer(doc, locale);
        assertEquals("http://test1.com", p.getNamespaceURI("ns1"));
    }

    /**
     * @target JDOMNodePointer#getNamespaceURI(String)
     * @scenario Element node, prefix resolved directly
     * @defectRisk Element branch not handled correctly
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIPrefix_Element() {
        Element root = new Element("root");
        root.addNamespaceDeclaration(Namespace.getNamespace("ns2", "http://test2.com"));
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        assertEquals("http://test2.com", p.getNamespaceURI("ns2"));
    }

    /**
     * @target JDOMNodePointer#getNamespaceURI(String)
     * @scenario Unknown prefix -> null result
     * @defectRisk Loop not exiting properly on missing namespace
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIPrefix_Unknown() {
        Element root = new Element("root");
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        assertNull(p.getNamespaceURI("doesnotexist"));
    }

    /**
     * @target JDOMNodePointer#getNamespaceURI(String)
     * @scenario Non document/element node -> null
     * @defectRisk Improper fallthrough handling
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIPrefix_NonElementNonDocument() {
        Text t = new Text("hello");
        JDOMNodePointer p = new JDOMNodePointer(t, locale);
        assertNull(p.getNamespaceURI("any"));
    }

    // ---------------------------------------------------------------
    // compareChildNodePointers
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#compareChildNodePointers
     * @scenario Same node reference -> 0
     * @defectRisk Reference equality check broken
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_SameNode() {
        Element parent = new Element("parent");
        Element c1 = new Element("c1");
        parent.addContent(c1);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, locale);
        NodePointer p1 = new JDOMNodePointer(parentPointer, c1);
        NodePointer p2 = new JDOMNodePointer(parentPointer, c1);
        assertEquals(0, parentPointer.compareChildNodePointers(p1, p2));
    }

    /**
     * @target JDOMNodePointer#compareChildNodePointers
     * @scenario Both nodes are attributes on the same element
     * @defectRisk Attribute ordering logic incorrect
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_Attributes() {
        Element el = new Element("el");
        el.setAttribute("a1", "v1");
        el.setAttribute("a2", "v2");
        Attribute attr1 = el.getAttribute("a1");
        Attribute attr2 = el.getAttribute("a2");

        JDOMNodePointer elPointer = new JDOMNodePointer(el, locale);
        NodePointer p1 = new JDOMNodePointer(elPointer, attr1);
        NodePointer p2 = new JDOMNodePointer(elPointer, attr2);

        assertEquals(-1, elPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, elPointer.compareChildNodePointers(p2, p1));
    }

    /**
     * @target JDOMNodePointer#compareChildNodePointers
     * @scenario One node is attribute, the other is not
     * @defectRisk Mixed attribute/non-attribute ordering incorrect
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_MixedAttrNonAttr() {
        Element el = new Element("el");
        el.setAttribute("a1", "v1");
        Element childEl = new Element("child");
        el.addContent(childEl);
        Attribute attr1 = el.getAttribute("a1");

        JDOMNodePointer elPointer = new JDOMNodePointer(el, locale);
        NodePointer attrPtr = new JDOMNodePointer(elPointer, attr1);
        NodePointer elemPtr = new JDOMNodePointer(elPointer, childEl);

        assertEquals(-1, elPointer.compareChildNodePointers(attrPtr, elemPtr));
        assertEquals(1, elPointer.compareChildNodePointers(elemPtr, attrPtr));
    }

    /**
     * @target JDOMNodePointer#compareChildNodePointers
     * @scenario Both are element children, order determined by content list
     * @defectRisk Element ordering logic incorrect
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_Elements() {
        Element parent = new Element("parent");
        Element c1 = new Element("c1");
        Element c2 = new Element("c2");
        parent.addContent(c1);
        parent.addContent(c2);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, locale);
        NodePointer p1 = new JDOMNodePointer(parentPointer, c1);
        NodePointer p2 = new JDOMNodePointer(parentPointer, c2);
        assertEquals(-1, parentPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, parentPointer.compareChildNodePointers(p2, p1));
    }

    /**
     * @target JDOMNodePointer#compareChildNodePointers
     * @scenario this.node is not an Element and neither child is Attribute
     * @defectRisk Missing RuntimeException for internal error case
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointers_NonElementThrows() {
        Text textNode = new Text("txt");
        JDOMNodePointer thisPointer = new JDOMNodePointer(textNode, locale);
        NodePointer p1 = new JDOMNodePointer(thisPointer, new Element("x1"));
        NodePointer p2 = new JDOMNodePointer(thisPointer, new Element("x2"));
        try {
            thisPointer.compareChildNodePointers(p1, p2);
            fail("Expected RuntimeException");
        }
        catch (RuntimeException expected) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // Basic accessors
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#isCollection()
     * @scenario Always false
     * @defectRisk Method incorrectly returns true
     */
    @Test(timeout = 4000)
    public void testIsCollection() {
        JDOMNodePointer p = new JDOMNodePointer(new Element("e"), locale);
        assertFalse(p.isCollection());
    }

    /**
     * @target JDOMNodePointer#getLength()
     * @scenario Always 1
     * @defectRisk Wrong constant returned
     */
    @Test(timeout = 4000)
    public void testGetLength() {
        JDOMNodePointer p = new JDOMNodePointer(new Element("e"), locale);
        assertEquals(1, p.getLength());
    }

    /**
     * @target JDOMNodePointer#isLeaf()
     * @scenario Element with no content -> leaf true
     * @defectRisk Content size check inverted
     */
    @Test(timeout = 4000)
    public void testIsLeaf_EmptyElement() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertTrue(p.isLeaf());
    }

    /**
     * @target JDOMNodePointer#isLeaf()
     * @scenario Element with content -> leaf false
     * @defectRisk Content size check inverted
     */
    @Test(timeout = 4000)
    public void testIsLeaf_NonEmptyElement() {
        Element e = new Element("e");
        e.addContent(new Element("child"));
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertFalse(p.isLeaf());
    }

    /**
     * @target JDOMNodePointer#isLeaf()
     * @scenario Document with content -> leaf false
     * @defectRisk Document branch not handled
     */
    @Test(timeout = 4000)
    public void testIsLeaf_DocumentNonEmpty() {
        Document doc = new Document(new Element("root"));
        JDOMNodePointer p = new JDOMNodePointer(doc, locale);
        assertFalse(p.isLeaf());
    }

    /**
     * @target JDOMNodePointer#isLeaf()
     * @scenario Other node type (Text) -> leaf true default
     * @defectRisk Default true branch missing
     */
    @Test(timeout = 4000)
    public void testIsLeaf_Text() {
        Text t = new Text("hi");
        JDOMNodePointer p = new JDOMNodePointer(t, locale);
        assertTrue(p.isLeaf());
    }

    // ---------------------------------------------------------------
    // getName()
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#getName()
     * @scenario Element with namespace prefix
     * @defectRisk Prefix/local name extraction incorrect
     */
    @Test(timeout = 4000)
    public void testGetName_ElementWithPrefix() {
        Namespace ns = Namespace.getNamespace("pre", "http://uri.com");
        Element e = new Element("foo", ns);
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        QName name = p.getName();
        assertEquals("pre", name.getPrefix());
        assertEquals("foo", name.getName());
    }

    /**
     * @target JDOMNodePointer#getName()
     * @scenario Element without namespace prefix -> null prefix
     * @defectRisk Empty string prefix not converted to null
     */
    @Test(timeout = 4000)
    public void testGetName_ElementNoPrefix() {
        Element e = new Element("foo");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        QName name = p.getName();
        assertNull(name.getPrefix());
        assertEquals("foo", name.getName());
    }

    /**
     * @target JDOMNodePointer#getName()
     * @scenario ProcessingInstruction node -> target used as local name
     * @defectRisk PI branch not correctly extracting target
     */
    @Test(timeout = 4000)
    public void testGetName_ProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer p = new JDOMNodePointer(pi, locale);
        QName name = p.getName();
        assertEquals("target", name.getName());
    }

    // ---------------------------------------------------------------
    // getValue()
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#getValue()
     * @scenario Element getTextTrim
     * @defectRisk Wrong text extraction
     */
    @Test(timeout = 4000)
    public void testGetValue_Element() {
        Element e = new Element("e");
        e.addContent(new Text("  hello  "));
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertEquals("hello", p.getValue());
    }

    /**
     * @target JDOMNodePointer#getValue()
     * @scenario Comment node -> trimmed text
     * @defectRisk Trim not applied or NPE on null text
     */
    @Test(timeout = 4000)
    public void testGetValue_Comment() {
        Comment c = new Comment("  comment text  ");
        JDOMNodePointer p = new JDOMNodePointer(c, locale);
        assertEquals("comment text", p.getValue());
    }

    /**
     * @target JDOMNodePointer#getValue()
     * @scenario Text node -> getTextTrim
     * @defectRisk Wrong extraction method used
     */
    @Test(timeout = 4000)
    public void testGetValue_Text() {
        Text t = new Text("  text value  ");
        JDOMNodePointer p = new JDOMNodePointer(t, locale);
        assertEquals("text value", p.getValue());
    }

    /**
     * @target JDOMNodePointer#getValue()
     * @scenario CDATA node -> getTextTrim
     * @defectRisk CDATA branch missing / wrong cast
     */
    @Test(timeout = 4000)
    public void testGetValue_CDATA() {
        CDATA cdata = new CDATA("  cdata value  ");
        JDOMNodePointer p = new JDOMNodePointer(cdata, locale);
        assertEquals("cdata value", p.getValue());
    }

    /**
     * @target JDOMNodePointer#getValue()
     * @scenario ProcessingInstruction -> trimmed data
     * @defectRisk Trim missing / wrong data extraction
     */
    @Test(timeout = 4000)
    public void testGetValue_PI() {
        ProcessingInstruction pi = new ProcessingInstruction("t", "  data value  ");
        JDOMNodePointer p = new JDOMNodePointer(pi, locale);
        assertEquals("data value", p.getValue());
    }

    /**
     * @target JDOMNodePointer#getValue()
     * @scenario Document node -> falls through to null
     * @defectRisk Missing default null return
     */
    @Test(timeout = 4000)
    public void testGetValue_Other() {
        Document doc = new Document(new Element("root"));
        JDOMNodePointer p = new JDOMNodePointer(doc, locale);
        assertNull(p.getValue());
    }

    // ---------------------------------------------------------------
    // setValue()
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Text node set to non-empty string
     * @defectRisk setText not invoked correctly
     */
    @Test(timeout = 4000)
    public void testSetValue_TextNonEmpty() {
        Text t = new Text("old");
        JDOMNodePointer p = new JDOMNodePointer(t, locale);
        p.setValue("new value");
        assertEquals("new value", t.getText());
    }

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Text node set to empty string removes it from real JDOM parent
     * @defectRisk removeContent not called on real parent
     */
    @Test(timeout = 4000)
    public void testSetValue_TextEmpty_RemovesFromParent() {
        Element parent = new Element("parent");
        Text t = new Text("something");
        parent.addContent(t);
        JDOMNodePointer p = new JDOMNodePointer(t, locale);
        p.setValue("");
        assertEquals(0, parent.getContent().size());
    }

    /**
     * @target JDOMNodePointer#setValue(Object) / addContent(List)
     * @scenario Element node set with Element value clones its content
     * @defectRisk Content not cleared or clone not added
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithElementValue() {
        Element target = new Element("target");
        target.addContent(new Text("old content"));

        Element valueElement = new Element("valueHolder");
        valueElement.addContent(new Element("cloneChild"));

        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue(valueElement);

        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Element);
    }

    /**
     * @target JDOMNodePointer#setValue(Object) / addContent(List)
     * @scenario Element node set with Document value clones root document content
     * @defectRisk Document branch not handled
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithDocumentValue() {
        Element target = new Element("target");
        Element docRoot = new Element("docRoot");
        Document valueDoc = new Document(docRoot);

        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue(valueDoc);

        assertEquals(1, target.getContent().size());
    }

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Element node set with a Text value object
     * @defectRisk Text/CDATA branch mishandled
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithTextValue() {
        Element target = new Element("target");
        Text valueText = new Text("some text");

        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue(valueText);

        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Text);
        assertEquals("some text", ((Text) target.getContent().get(0)).getText());
    }

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Element node set with a CDATA value object
     * @defectRisk CDATA cast to Text failing
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithCDATAValue() {
        Element target = new Element("target");
        CDATA valueCdata = new CDATA("cdata text");

        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue(valueCdata);

        assertEquals(1, target.getContent().size());
    }

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Element node set with ProcessingInstruction value (cloned)
     * @defectRisk PI clone not added correctly
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithPIValue() {
        Element target = new Element("target");
        ProcessingInstruction pi = new ProcessingInstruction("tgt", "dat");

        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue(pi);

        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof ProcessingInstruction);
    }

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Element node set with Comment value (cloned)
     * @defectRisk Comment clone not added correctly
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithCommentValue() {
        Element target = new Element("target");
        Comment comment = new Comment("a comment");

        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue(comment);

        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Comment);
    }

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Element node set with plain String value
     * @defectRisk String-to-Text conversion path broken
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithStringValue() {
        Element target = new Element("target");
        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue("plain string");
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Text);
    }

    /**
     * @target JDOMNodePointer#setValue(Object)
     * @scenario Element node set with empty String value -> no content added
     * @defectRisk Empty string check missing
     */
    @Test(timeout = 4000)
    public void testSetValue_ElementWithEmptyStringValue() {
        Element target = new Element("target");
        target.addContent(new Text("existing"));
        JDOMNodePointer p = new JDOMNodePointer(target, locale);
        p.setValue("");
        assertEquals(0, target.getContent().size());
    }

    // ---------------------------------------------------------------
    // testNode()
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#testNode(NodeTest)
     * @scenario null test -> always true
     * @defectRisk Null check missing
     */
    @Test(timeout = 4000)
    public void testTestNode_NullTest() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertTrue(p.testNode(null));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeNameTest branch
     * @scenario Non element node -> false
     * @defectRisk instanceof Element check missing
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeNameTest_NonElement() {
        Text t = new Text("txt");
        JDOMNodePointer p = new JDOMNodePointer(t, locale);
        NodeNameTest test = new NodeNameTest(new QName(null, "foo"));
        assertFalse(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeNameTest branch
     * @scenario Matching element local name, no namespace
     * @defectRisk Name/namespace comparison incorrect
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeNameTest_Match() {
        Element e = new Element("foo");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        NodeNameTest test = new NodeNameTest(new QName(null, "foo"));
        assertTrue(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeNameTest branch
     * @scenario Non-matching element local name
     * @defectRisk False positive on mismatched names
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeNameTest_NoMatch() {
        Element e = new Element("bar");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        NodeNameTest test = new NodeNameTest(new QName(null, "foo"));
        assertFalse(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeNameTest wildcard branch
     * @scenario Wildcard test with null prefix -> immediate true
     * @defectRisk Wildcard early-return branch missing
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeNameTest_Wildcard() {
        Element e = new Element("anything");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeTypeTest branch
     * @scenario NODE_TYPE_NODE matches Element
     * @defectRisk Node type constant mismatched
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_Node() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeTypeTest branch
     * @scenario NODE_TYPE_TEXT matches Text and CDATA
     * @defectRisk CDATA not recognized as text type
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_Text() {
        Text t = new Text("t");
        CDATA c = new CDATA("c");
        JDOMNodePointer pt = new JDOMNodePointer(t, locale);
        JDOMNodePointer pc = new JDOMNodePointer(c, locale);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(pt.testNode(test));
        assertTrue(pc.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeTypeTest branch
     * @scenario NODE_TYPE_COMMENT matches Comment
     * @defectRisk Comment type check missing
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_Comment() {
        Comment c = new Comment("c");
        JDOMNodePointer p = new JDOMNodePointer(c, locale);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) NodeTypeTest branch
     * @scenario NODE_TYPE_PI matches ProcessingInstruction
     * @defectRisk PI type check missing
     */
    @Test(timeout = 4000)
    public void testTestNode_NodeTypeTest_PI() {
        ProcessingInstruction pi = new ProcessingInstruction("t", "d");
        JDOMNodePointer p = new JDOMNodePointer(pi, locale);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) ProcessingInstructionTest branch
     * @scenario Matching target
     * @defectRisk Target comparison incorrect
     */
    @Test(timeout = 4000)
    public void testTestNode_PITest_Match() {
        ProcessingInstruction pi = new ProcessingInstruction("target1", "d");
        JDOMNodePointer p = new JDOMNodePointer(pi, locale);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target1");
        assertTrue(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) ProcessingInstructionTest branch
     * @scenario Non-matching target
     * @defectRisk False positive on mismatched PI target
     */
    @Test(timeout = 4000)
    public void testTestNode_PITest_NoMatch() {
        ProcessingInstruction pi = new ProcessingInstruction("target1", "d");
        JDOMNodePointer p = new JDOMNodePointer(pi, locale);
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(p.testNode(test));
    }

    /**
     * @target JDOMNodePointer#testNode(NodeTest) ProcessingInstructionTest branch
     * @scenario Node is not a PI -> false
     * @defectRisk instanceof check missing
     */
    @Test(timeout = 4000)
    public void testTestNode_PITest_NonPINode() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        ProcessingInstructionTest test = new ProcessingInstructionTest("target1");
        assertFalse(p.testNode(test));
    }

    // ---------------------------------------------------------------
    // getPrefix / getLocalName static methods
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#getPrefix(Object)
     * @scenario Element with prefix
     * @defectRisk Prefix extraction incorrect
     */
    @Test(timeout = 4000)
    public void testGetPrefix_Element() {
        Namespace ns = Namespace.getNamespace("pre", "http://uri.com");
        Element e = new Element("foo", ns);
        assertEquals("pre", JDOMNodePointer.getPrefix(e));
    }

    /**
     * @target JDOMNodePointer#getPrefix(Object)
     * @scenario Attribute with prefix
     * @defectRisk Attribute branch missing
     */
    @Test(timeout = 4000)
    public void testGetPrefix_Attribute() {
        Namespace ns = Namespace.getNamespace("apre", "http://attr.com");
        Attribute attr = new Attribute("name", "value", ns);
        assertEquals("apre", JDOMNodePointer.getPrefix(attr));
    }

    /**
     * @target JDOMNodePointer#getPrefix(Object)
     * @scenario Other object type -> null
     * @defectRisk Default null return missing
     */
    @Test(timeout = 4000)
    public void testGetPrefix_Other() {
        assertNull(JDOMNodePointer.getPrefix(new Text("t")));
    }

    /**
     * @target JDOMNodePointer#getLocalName(Object)
     * @scenario Element -> name
     * @defectRisk Wrong extraction
     */
    @Test(timeout = 4000)
    public void testGetLocalName_Element() {
        Element e = new Element("foo");
        assertEquals("foo", JDOMNodePointer.getLocalName(e));
    }

    /**
     * @target JDOMNodePointer#getLocalName(Object)
     * @scenario Attribute -> name
     * @defectRisk Attribute branch missing
     */
    @Test(timeout = 4000)
    public void testGetLocalName_Attribute() {
        Attribute attr = new Attribute("attrName", "value");
        assertEquals("attrName", JDOMNodePointer.getLocalName(attr));
    }

    /**
     * @target JDOMNodePointer#getLocalName(Object)
     * @scenario Other object -> null
     * @defectRisk Default null return missing
     */
    @Test(timeout = 4000)
    public void testGetLocalName_Other() {
        assertNull(JDOMNodePointer.getLocalName(new Text("t")));
    }

    // ---------------------------------------------------------------
    // isLanguage / getLanguage
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#isLanguage(String) / getLanguage()
     * @scenario xml:lang attribute present and matches
     * @defectRisk Attribute lookup or comparison incorrect
     */
    @Test(timeout = 4000)
    public void testIsLanguage_AttributeMatch() {
        Element e = new Element("e");
        e.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        JDOMNodePointer p = new JDOMNodePointer(e, Locale.US);
        assertTrue(p.isLanguage("en"));
    }

    /**
     * @target JDOMNodePointer#isLanguage(String) / getLanguage()
     * @scenario No xml:lang attribute anywhere -> falls back to super
     * @defectRisk Fallback to super.isLanguage not triggered
     */
    @Test(timeout = 4000)
    public void testIsLanguage_NoAttributeFallsBackToSuper() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, Locale.US);
        assertFalse(p.isLanguage("xx"));
    }

    // ---------------------------------------------------------------
    // createAttribute
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#createAttribute(JXPathContext,QName)
     * @scenario No prefix, attribute does not exist yet -> created
     * @defectRisk Attribute not created or wrong value
     */
    @Test(timeout = 4000)
    public void testCreateAttribute_NoPrefix() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        NodePointer result = p.createAttribute(null, new QName(null, "newattr"));
        assertNotNull(result);
        assertNotNull(e.getAttribute("newattr"));
    }

    /**
     * @target JDOMNodePointer#createAttribute(JXPathContext,QName)
     * @scenario With known namespace prefix -> attribute created in namespace
     * @defectRisk Namespace prefix resolution incorrect
     */
    @Test(timeout = 4000)
    public void testCreateAttribute_WithPrefix() {
        Element e = new Element("e");
        Namespace ns = Namespace.getNamespace("pfx", "http://ns.com");
        e.addNamespaceDeclaration(ns);
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        NodePointer result = p.createAttribute(null, new QName("pfx", "attrX"));
        assertNotNull(result);
        assertNotNull(e.getAttribute("attrX", ns));
    }

    /**
     * @target JDOMNodePointer#createAttribute(JXPathContext,QName)
     * @scenario Unknown namespace prefix -> JXPathException thrown
     * @defectRisk Missing exception for unresolved prefix
     */
    @Test(timeout = 4000)
    public void testCreateAttribute_UnknownPrefixThrows() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        try {
            p.createAttribute(null, new QName("unknownpfx", "attrY"));
            fail("Expected JXPathException");
        }
        catch (JXPathException expected) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // remove()
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#remove()
     * @scenario Child element removed from its real parent
     * @defectRisk removeContent not invoked on correct parent
     */
    @Test(timeout = 4000)
    public void testRemove_Child() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        childPointer.remove();
        assertEquals(0, root.getContent().size());
    }

    /**
     * @target JDOMNodePointer#remove()
     * @scenario Root node without JDOM parent -> exception
     * @defectRisk Missing exception for root removal attempt
     */
    @Test(timeout = 4000)
    public void testRemove_RootThrows() {
        Element root = new Element("root");
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        try {
            p.remove();
            fail("Expected JXPathException");
        }
        catch (JXPathException expected) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // asPath()
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#asPath()
     * @scenario id set -> id('...') path with escaping
     * @defectRisk Escaping of quotes broken
     */
    @Test(timeout = 4000)
    public void testAsPath_WithIdEscaping() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale, "my'id");
        assertEquals("id('my&apos;id')", p.asPath());
    }

    /**
     * @target JDOMNodePointer#asPath()
     * @scenario Root element with no parent pointer -> empty path segment
     * @defectRisk Parent-null handling incorrect
     */
    @Test(timeout = 4000)
    public void testAsPath_RootNoParent() {
        Element root = new Element("root");
        JDOMNodePointer p = new JDOMNodePointer(root, locale);
        assertEquals("", p.asPath());
    }

    /**
     * @target JDOMNodePointer#asPath()
     * @scenario Text node under JDOMNodePointer parent -> /text()[n]
     * @defectRisk Text position calculation incorrect
     */
    @Test(timeout = 4000)
    public void testAsPath_TextNode() {
        Element root = new Element("root");
        Text text = new Text("hello");
        root.addContent(text);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        JDOMNodePointer textPointer = new JDOMNodePointer(rootPointer, text);
        String path = textPointer.asPath();
        assertTrue(path.endsWith("/text()[1]"));
    }

    /**
     * @target JDOMNodePointer#asPath()
     * @scenario ProcessingInstruction node -> /processing-instruction('target')[n]
     * @defectRisk PI position calculation incorrect
     */
    @Test(timeout = 4000)
    public void testAsPath_ProcessingInstructionNode() {
        Element root = new Element("root");
        ProcessingInstruction pi = new ProcessingInstruction("tgt", "data");
        root.addContent(pi);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        JDOMNodePointer piPointer = new JDOMNodePointer(rootPointer, pi);
        String path = piPointer.asPath();
        assertTrue(path.contains("processing-instruction('tgt')[1]"));
    }

    /**
     * @target JDOMNodePointer#asPath()
     * @scenario Full traversal through JXPathContext to exercise namespace
     * resolver-based default-namespace matching branch producing name[pos]
     * @defectRisk Position/name computation under proper context incorrect
     */
    @Test(timeout = 4000)
    public void testAsPath_FullContextTraversal() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);
        Document doc = new Document(root);

        JXPathContext context = JXPathContext.newContext(doc);
        Pointer p = context.getPointer("/root/child");
        String path = p.asPath();
        assertNotNull(path);
        assertTrue(path.contains("child"));
    }

    // ---------------------------------------------------------------
    // createChild
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#createChild(JXPathContext,QName,int)
     * @scenario AbstractFactory successfully creates child element
     * @defectRisk Factory invocation or child lookup logic incorrect
     */
    @Test(timeout = 4000)
    public void testCreateChild_Success() {
        Element root = new Element("root");
        Document doc = new Document(root);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                if (parent instanceof Element) {
                    ((Element) parent).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        QName name = new QName(null, "newchild");
        NodePointer childPointer = rootPointer.createChild(context, name, 0);
        assertNotNull(childPointer);
        assertEquals(1, root.getContent().size());
    }

    /**
     * @target JDOMNodePointer#createChild(JXPathContext,QName,int,Object)
     * @scenario Factory creates child then value is applied
     * @defectRisk setValue not invoked on newly created pointer
     */
    @Test(timeout = 4000)
    public void testCreateChild_WithValue() {
        Element root = new Element("root");
        Document doc = new Document(root);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                if (parent instanceof Element) {
                    ((Element) parent).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        QName name = new QName(null, "vchild");
        NodePointer childPointer =
                rootPointer.createChild(context, name, 0, "childValue");
        assertNotNull(childPointer);
        Element createdChild = (Element) root.getContent().get(0);
        assertEquals("childValue", createdChild.getTextTrim());
    }

    /**
     * @target JDOMNodePointer#createChild(JXPathContext,QName,int)
     * @scenario Factory fails to create object -> exception thrown
     * @defectRisk Missing exception on factory failure
     */
    @Test(timeout = 4000)
    public void testCreateChild_FactoryFailureThrows() {
        Element root = new Element("root");
        Document doc = new Document(root);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                return false;
            }
        });

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        QName name = new QName(null, "failchild");
        try {
            rootPointer.createChild(context, name, 0);
            fail("Expected JXPathAbstractFactoryException");
        }
        catch (JXPathAbstractFactoryException expected) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // hashCode / equals
    // ---------------------------------------------------------------

    /**
     * @target JDOMNodePointer#hashCode()
     * @scenario Based on identity hash of wrapped node
     * @defectRisk hashCode not based on identity
     */
    @Test(timeout = 4000)
    public void testHashCode() {
        Element e = new Element("e");
        JDOMNodePointer p = new JDOMNodePointer(e, locale);
        assertEquals(System.identityHashCode(e), p.hashCode());
    }

    /**
     * @target JDOMNodePointer#equals(Object)
     * @scenario Same wrapped node -> equal; different node -> not equal;
     * non-JDOMNodePointer -> not equal; same instance -> equal
     * @defectRisk equals logic broken for various branches
     */
    @Test(timeout = 4000)
    public void testEquals() {
        Element e1 = new Element("e1");
        Element e2 = new Element("e2");
        JDOMNodePointer p1 = new JDOMNodePointer(e1, locale);
        JDOMNodePointer p1b = new JDOMNodePointer(e1, locale);
        JDOMNodePointer p2 = new JDOMNodePointer(e2, locale);

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p1b));
        assertFalse(p1.equals(p2));
        assertFalse(p1.equals("not a pointer"));
    }
}