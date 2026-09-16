package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructors, basic getters (getBaseValue, getImmediateNode, isActual,
 *     isCollection, getLength, isLeaf, getName, equals, hashCode)
 *   - Namespace resolution, default namespace, namespace resolver, child and
 *     attribute iterators, setValue, createAttribute, remove, getValue,
 *     getPointerByID, compareChildNodePointers.
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - Null NodeTest, null/empty namespace prefixes, no namespace, empty xmlns
 *     declarations, text/CDATA/PI/comment values, multiple sibling positions,
 *     duplicate names, unknown prefixes, root removal.
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - JXPath-154 regression: an element using xmlns="" inside a namespaced tree
 *     must be treated as having no namespace.  The defective implementation
 *     returns an empty namespace URI from getNamespaceURI(node) and therefore
 *     renders asPath() as node()[n] instead of the qualified local-name path.
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - createAttribute with unknown prefix throws JXPathException.
 *   - remove() on the root DOM node throws JXPathException.
 *   - Invalid NodeTypeTest falls through to false.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals/hashCode use the underlying DOM node identity; pointers with
 *     different locales but same node are equal.
 */
public class DOMNodePointerDeepseekTest {

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().newDocument();
    }

    @Test(timeout = 4000)
    public void testBasicGettersAndConstructors() throws Exception {
        Document doc = parse("<root/>");
        Element element = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        assertSame(element, pointer.getBaseValue());
        assertSame(element, pointer.getImmediateNode());
        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        DOMNodePointer idPointer = new DOMNodePointer(element, Locale.ENGLISH, "abc");
        assertEquals("id('abc')", idPointer.asPath());

        Element child = doc.createElement("child");
        element.appendChild(child);
        DOMNodePointer parentPointer = new DOMNodePointer(element, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(parentPointer, child);
        assertFalse(parentPointer.isLeaf());
        assertTrue(childPointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testTestNodeNull() throws Exception {
        Element root = parse("<root/>").getDocumentElement();
        assertTrue(DOMNodePointer.testNode(root, null));
        assertTrue(new DOMNodePointer(root, Locale.ENGLISH).testNode(null));
    }

    @Test(timeout = 4000)
    public void testTestNodeNameTest() throws Exception {
        Element root = parse("<root/>").getDocumentElement();
        assertTrue(DOMNodePointer.testNode(root, new NodeNameTest(new QName("root"), null)));
        assertFalse(DOMNodePointer.testNode(root, new NodeNameTest(new QName("other"), null)));
        assertTrue(DOMNodePointer.testNode(root, new NodeNameTest(new QName("*"), null)));

        Text text = parse("<root/>").createTextNode("text");
        assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("root"), null)));

        Document nsDoc = parse("<b:foo xmlns:b='urn:b'/>");
        Element foo = nsDoc.getDocumentElement();
        assertTrue(DOMNodePointer.testNode(foo, new NodeNameTest(new QName("b", "foo"), "urn:b")));
        assertFalse(DOMNodePointer.testNode(foo, new NodeNameTest(new QName("b", "foo"), "urn:other")));
        assertTrue(DOMNodePointer.testNode(foo, new NodeNameTest(new QName("b", "*"), "urn:b")));
        assertFalse(DOMNodePointer.testNode(foo, new NodeNameTest(new QName("b", "*"), "urn:other")));
    }

    @Test(timeout = 4000)
    public void testTestNodeTypeTest() throws Exception {
        Document doc = createDocument();
        Element element = doc.createElement("root");
        Text text = doc.createTextNode("text");
        CDATASection cdata = doc.createCDATASection("cdata");
        Comment comment = doc.createComment("comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");

        assertTrue(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(pi, new NodeTypeTest(999)));
    }

    @Test(timeout = 4000)
    public void testTestNodeProcessingInstruction() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("target")));
        assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("other")));
        assertFalse(DOMNodePointer.testNode(doc.createElement("root"), new ProcessingInstructionTest("target")));
    }

    @Test(timeout = 4000)
    public void testGetName() throws Exception {
        Document doc = parse("<root><b:child xmlns:b='urn:b'/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();

        assertEquals(new QName(null, "root"), new DOMNodePointer(root, Locale.ENGLISH).getName());
        assertEquals(new QName("b", "child"), new DOMNodePointer(child, Locale.ENGLISH).getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        assertEquals(new QName(null, "target"), new DOMNodePointer(pi, Locale.ENGLISH).getName());

        Text text = doc.createTextNode("text");
        assertEquals(new QName(null, null), new DOMNodePointer(text, Locale.ENGLISH).getName());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURI() throws Exception {
        Document doc = parse("<root xmlns='urn:default' xmlns:b='urn:b'><child/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.ENGLISH);

        assertEquals("urn:default", rootPointer.getNamespaceURI());
        assertEquals("urn:default", childPointer.getDefaultNamespaceURI());
        assertEquals("urn:default", childPointer.getNamespaceURI((String) null));
        assertEquals("urn:default", childPointer.getNamespaceURI(""));
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, childPointer.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, childPointer.getNamespaceURI("xmlns"));
        assertEquals("urn:b", childPointer.getNamespaceURI("b"));
        assertNull(childPointer.getNamespaceURI("unknown"));

        Document noNsDoc = parse("<root/>");
        DOMNodePointer noNsPointer = new DOMNodePointer(noNsDoc.getDocumentElement(), Locale.ENGLISH);
        assertNull(noNsPointer.getNamespaceURI());
        assertNull(noNsPointer.getDefaultNamespaceURI());

        Document prefixedDoc = parse("<b:root xmlns:b='urn:b'/>");
        DOMNodePointer docPointer = new DOMNodePointer(prefixedDoc, Locale.ENGLISH);
        assertEquals("urn:b", docPointer.getNamespaceURI());

        assertEquals("urn:b", DOMNodePointer.getNamespaceURI(prefixedDoc.getDocumentElement()));
        assertEquals("b", DOMNodePointer.getPrefix(prefixedDoc.getDocumentElement()));
        assertEquals("root", DOMNodePointer.getLocalName(prefixedDoc.getDocumentElement()));
        assertNull(DOMNodePointer.getPrefix(noNsDoc.getDocumentElement()));
    }

    @Test(timeout = 4000)
    public void testNamespaceResolver() throws Exception {
        Document doc = parse("<root xmlns:b='urn:b'/>");
        DOMNodePointer pointer = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNotNull(pointer.getNamespaceResolver());
        assertSame(pointer.getNamespaceResolver(), pointer.getNamespaceResolver());
    }

    @Test(timeout = 4000)
    public void testChildAndAttributeIterators() throws Exception {
        Document doc = parse("<root><a/><b/></root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);

        NodeIterator childIt = pointer.childIterator(null, false, null);
        int childCount = 0;
        while (childIt.setPosition(childCount + 1)) {
            childCount++;
        }
        assertEquals(2, childCount);

        Element attrElement = doc.createElement("root");
        attrElement.setAttribute("id", "x");
        DOMNodePointer attrPointer = new DOMNodePointer(attrElement, Locale.ENGLISH);
        NodeIterator attrIt = attrPointer.attributeIterator(new QName("id"));
        assertTrue(attrIt.setPosition(1));
        Node attrNode = (Node) attrIt.getNodePointer().getBaseValue();
        assertEquals("id", attrNode.getNodeName());

        assertNotNull(attrPointer.namespaceIterator());
        assertNotNull(attrPointer.namespacePointer("xml"));
    }

    @Test(timeout = 4000)
    public void testIsLeafAndIsLanguage() throws Exception {
        Document doc = parse("<root><child/></root>");
        DOMNodePointer nonLeaf = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertFalse(nonLeaf.isLeaf());

        Document emptyDoc = createDocument();
        Element alone = emptyDoc.createElement("alone");
        assertTrue(new DOMNodePointer(alone, Locale.ENGLISH).isLeaf());

        Document langDoc = parse("<root xml:lang='en'><child/></root>");
        Element child = (Element) langDoc.getDocumentElement().getFirstChild();
        DOMNodePointer langPointer = new DOMNodePointer(child, Locale.ENGLISH);
        assertTrue(langPointer.isLanguage("e"));
        assertTrue(langPointer.isLanguage("EN"));
        assertFalse(langPointer.isLanguage("fr"));
    }

    @Test(timeout = 4000)
    public void testSetValueTextAndElement() throws Exception {
        Document doc = parse("<root>old</root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        NodeIterator it = pointer.childIterator(null, false, null);
        assertTrue(it.setPosition(1));
        NodePointer textPointer = it.getNodePointer();
        textPointer.setValue("new");
        assertEquals("new", root.getFirstChild().getNodeValue());

        Document removeDoc = parse("<root>remove</root>");
        Element removeRoot = removeDoc.getDocumentElement();
        DOMNodePointer removePointer = new DOMNodePointer(removeRoot, Locale.ENGLISH);
        NodeIterator removeIt = removePointer.childIterator(null, false, null);
        assertTrue(removeIt.setPosition(1));
        removeIt.getNodePointer().setValue("");
        assertFalse(removeRoot.hasChildNodes());

        Document setDoc = createDocument();
        Element target = setDoc.createElement("target");
        setDoc.appendChild(target);
        DOMNodePointer targetPointer = new DOMNodePointer(target, Locale.ENGLISH);
        targetPointer.setValue("abc");
        assertEquals(1, target.getChildNodes().getLength());
        assertEquals("abc", target.getFirstChild().getNodeValue());

        Element source = setDoc.createElement("source");
        source.appendChild(setDoc.createTextNode("old"));
        Element target2 = setDoc.createElement("target2");
        setDoc.appendChild(target2);
        new DOMNodePointer(target2, Locale.ENGLISH).setValue(source);
        assertEquals("old", target2.getTextContent());

        Document subDoc = createDocument();
        Element subRoot = subDoc.createElement("subroot");
        subRoot.appendChild(subDoc.createTextNode("sub"));
        subDoc.appendChild(subRoot);
        Element target3 = setDoc.createElement("target3");
        setDoc.appendChild(target3);
        new DOMNodePointer(target3, Locale.ENGLISH).setValue(subDoc);
        assertEquals(1, target3.getChildNodes().getLength());
        assertEquals("subroot", target3.getFirstChild().getNodeName());
    }

    @Test(timeout = 4000)
    public void testCreateAttributeAndRemove() throws Exception {
        Document doc = createDocument();
        Element element = doc.createElement("root");
        doc.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.ENGLISH);

        NodePointer attrPointer = pointer.createAttribute(null, new QName("id"));
        assertNotNull(attrPointer);
        assertTrue(element.hasAttribute("id"));
        assertEquals("", element.getAttribute("id"));

        Document nsDoc = parse("<root xmlns:b='urn:b'/>");
        Element nsRoot = nsDoc.getDocumentElement();
        DOMNodePointer nsPointer = new DOMNodePointer(nsRoot, Locale.ENGLISH);
        NodePointer nsAttr = nsPointer.createAttribute(null, new QName("b", "attr"));
        assertNotNull(nsAttr);
        assertEquals("urn:b", nsRoot.getAttributeNS("urn:b", "attr"));

        Element unknownElement = doc.createElement("unknown");
        DOMNodePointer unknownPointer = new DOMNodePointer(unknownElement, Locale.ENGLISH);
        try {
            unknownPointer.createAttribute(null, new QName("unknown", "attr"));
            fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException expected) {
            assertNotNull(expected);
        }

        Element detachedRoot = doc.createElement("detached");
        DOMNodePointer detachedPointer = new DOMNodePointer(detachedRoot, Locale.ENGLISH);
        try {
            detachedPointer.remove();
            fail("Expected JXPathException when removing root DOM node");
        } catch (JXPathException expected) {
            assertNotNull(expected);
        }

        Element parent = doc.createElement("parent");
        Element child = doc.createElement("child");
        parent.appendChild(child);
        doc.appendChild(parent);
        DOMNodePointer parentPointer = new DOMNodePointer(parent, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(parentPointer, child);
        childPointer.remove();
        assertFalse(parent.hasChildNodes());
    }

    @Test(timeout = 4000)
    public void testAsPathBasic() throws Exception {
        Document doc = parse("<root/>");
        Element root = doc.getDocumentElement();
        assertEquals("/root[1]", new DOMNodePointer(root, Locale.ENGLISH).asPath());

        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.ENGLISH, "x");
        assertEquals("id('x')", idPointer.asPath());

        Document childDoc = parse("<root><child/></root>");
        Element childRoot = childDoc.getDocumentElement();
        DOMNodePointer childRootPointer = new DOMNodePointer(childRoot, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(childRootPointer, (Element) childRoot.getFirstChild());
        assertEquals("/root[1]/child[1]", childPointer.asPath());

        Document nsDoc = parse("<b:root xmlns:b='urn:b'/>");
        DOMNodePointer nsRootPointer = new DOMNodePointer(nsDoc.getDocumentElement(), Locale.ENGLISH);
        assertEquals("/b:root[1]", nsRootPointer.asPath());

        Document childNsDoc = parse("<b:root xmlns:b='urn:b'><b:child/></b:root>");
        Element nsChildRoot = childNsDoc.getDocumentElement();
        Element nsChild = (Element) nsChildRoot.getFirstChild();
        DOMNodePointer nsChildRootPointer = new DOMNodePointer(nsChildRoot, Locale.ENGLISH);
        DOMNodePointer nsChildPointer = new DOMNodePointer(nsChildRootPointer, nsChild);
        assertEquals("/b:root[1]/b:child[1]", nsChildPointer.asPath());

        Document docNode = parse("<root/>");
        assertEquals("", new DOMNodePointer(docNode, Locale.ENGLISH).asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathPositions() throws Exception {
        Document doc = parse("<root>t1<b/>t2</root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        NodeIterator it = rootPointer.childIterator(null, false, null);
        assertTrue(it.setPosition(1));
        assertTrue(it.setPosition(2));
        assertTrue(it.setPosition(3));
        assertEquals("/root[1]/text()[2]", it.getNodePointer().asPath());

        Document dupDoc = parse("<root><a/><b/><a/></root>");
        Element dupRoot = dupDoc.getDocumentElement();
        NodeList aNodes = dupRoot.getElementsByTagName("a");
        Element secondA = (Element) aNodes.item(1);
        DOMNodePointer dupRootPointer = new DOMNodePointer(dupRoot, Locale.ENGLISH);
        assertEquals("/root[1]/a[2]", new DOMNodePointer(dupRootPointer, secondA).asPath());

        Document piDoc = createDocument();
        Element piRoot = piDoc.createElement("root");
        piRoot.appendChild(piDoc.createProcessingInstruction("t", "1"));
        piRoot.appendChild(piDoc.createProcessingInstruction("x", "2"));
        piRoot.appendChild(piDoc.createProcessingInstruction("t", "3"));
        piDoc.appendChild(piRoot);
        DOMNodePointer piRootPointer = new DOMNodePointer(piRoot, Locale.ENGLISH);
        NodeIterator piIt = piRootPointer.childIterator(null, false, null);
        assertTrue(piIt.setPosition(3));
        assertEquals("/root[1]/processing-instruction('t')[2]", piIt.getNodePointer().asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathNamespaceWithoutPrefixUsesNode() throws Exception {
        Document doc = parse("<b:root xmlns:b='urn:b'><x xmlns='urn:x'/><y xmlns='urn:x'/></b:root>");
        Element root = doc.getDocumentElement();
        Element y = (Element) root.getChildNodes().item(1);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer yPointer = new DOMNodePointer(rootPointer, y);
        assertEquals("/b:root[1]/node()[2]", yPointer.asPath());
    }

    @Test(timeout = 4000)
    public void testInnerEmptyNamespaceAsPathDefect() throws Exception {
        Document doc = parse("<b:foo xmlns:b='urn:b'><test xmlns=''/></b:foo>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();

        assertNull(DOMNodePointer.getNamespaceURI(child));

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        assertEquals("/b:foo[1]/test[1]", childPointer.asPath());
    }

    @Test(timeout = 4000)
    public void testGetValue() throws Exception {
        Document doc = parse("<root>  hi  </root>");
        assertEquals("hi", new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH).getValue());

        Document preserveDoc = parse("<root xml:space='preserve'>  hi  </root>");
        assertEquals("  hi  ", new DOMNodePointer(preserveDoc.getDocumentElement(), Locale.ENGLISH).getValue());

        Document commentDoc = createDocument();
        Comment comment = commentDoc.createComment("  comment  ");
        assertEquals("comment", new DOMNodePointer(comment, Locale.ENGLISH).getValue());

        Document piDoc = createDocument();
        ProcessingInstruction pi = piDoc.createProcessingInstruction("target", "  data  ");
        assertEquals("data", new DOMNodePointer(pi, Locale.ENGLISH).getValue());
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("id", "x");
        root.setIdAttribute("id", true);
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(doc, Locale.ENGLISH);
        Pointer found = pointer.getPointerByID(null, "x");
        assertNotNull(found);
        assertTrue(found instanceof DOMNodePointer);
        assertEquals("id('x')", found.asPath());

        Pointer missing = pointer.getPointerByID(null, "missing");
        assertNotNull(missing);
        assertTrue(missing instanceof NullPointer);
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        Element child1 = doc.createElement("c1");
        Element child2 = doc.createElement("c2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        Attr attr1 = doc.createAttribute("a");
        Attr attr2 = doc.createAttribute("b");
        parent.setAttributeNode(attr1);
        parent.setAttributeNode(attr2);

        DOMNodePointer parentPointer = new DOMNodePointer(parent, Locale.ENGLISH);
        DOMNodePointer p1 = new DOMNodePointer(parentPointer, child1);
        DOMNodePointer p2 = new DOMNodePointer(parentPointer, child2);
        DOMNodePointer pa1 = new DOMNodePointer(parentPointer, attr1);
        DOMNodePointer pa2 = new DOMNodePointer(parentPointer, attr2);

        assertEquals(0, parentPointer.compareChildNodePointers(p1, p1));
        assertEquals(-1, parentPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, parentPointer.compareChildNodePointers(p2, p1));
        assertEquals(-1, parentPointer.compareChildNodePointers(pa1, p1));
        assertEquals(1, parentPointer.compareChildNodePointers(p1, pa1));

        int attrComparison = parentPointer.compareChildNodePointers(pa1, pa2);
        assertTrue(attrComparison == -1 || attrComparison == 1);
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() throws Exception {
        Document doc = parse("<root/>");
        Element element = doc.getDocumentElement();
        DOMNodePointer pointer1 = new DOMNodePointer(element, Locale.ENGLISH);
        DOMNodePointer pointer2 = new DOMNodePointer(element, Locale.FRENCH);
        DOMNodePointer pointer3 = new DOMNodePointer(doc.createElement("other"), Locale.ENGLISH);

        assertEquals(pointer1, pointer1);
        assertEquals(pointer1, pointer2);
        assertFalse(pointer1.equals(pointer3));
        assertFalse(pointer1.equals(null));
        assertFalse(pointer1.equals("x"));
        assertEquals(pointer1.hashCode(), pointer2.hashCode());
    }
}