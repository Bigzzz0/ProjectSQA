package org.apache.commons.jxpath.ri.model.dom;

/* [Branch & Defect Analysis Matrix]
 * The primary defect is in DOMNodePointer.testNode(Node, NodeTest):
 * the Compiler.NODE_TYPE_NODE branch only accepts ELEMENT_NODE and
 * DOCUMENT_NODE, but XPath's node() test must accept every node type.
 * This causes the following/preceding axis tests to skip text, CDATA,
 * comment, PI and attribute nodes, shifting positional predicates and
 * producing the reported asPath mismatches (product[1] vs.
 * product[1]/product:name[1], employeeCount text vs. address element).
 *
 * This suite targets:
 *  - node() acceptance for all W3C node types (defect-revealing)
 *  - text(), comment(), processing-instruction() node tests
 *  - name tests, wildcard tests, namespace-aware tests
 *  - getNamespaceURI / getDefaultNamespaceURI / namespace resolver cache
 *  - asPath for elements, text, CDATA, PI, id escapes and namespace prefixes
 *  - setValue for text, CDATA, element-with-string, element-with-Node
 *  - getValue, xml:space trimming behavior
 *  - remove, equals/hashCode, compareChildNodePointers
 *  - createAttribute and factory-less createChild error paths
 *  - getPointerByID with DTD-declared ID attributes
 */

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
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

public class DOMNodePointerDeepseekTest {

    private DocumentBuilder createBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder();
    }

    private Document parse(String xml) throws Exception {
        return createBuilder().parse(
            new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    private Document newDocument() throws Exception {
        return createBuilder().newDocument();
    }

    @Test(timeout = 4000)
    public void testNullNodeTestReturnsTrue() throws Exception {
        Document doc = parse("<root/>");
        assertTrue(DOMNodePointer.testNode(doc.getDocumentElement(), (NodeTest) null));
    }

    @Test(timeout = 4000)
    public void testNodeTypeNodeMatchesAllXPathNodeTypes() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Attr attr = doc.createAttribute("id");
        attr.setValue("x");
        root.setAttributeNode(attr);

        Text text = doc.createTextNode("text");
        root.appendChild(text);

        CDATASection cdata = doc.createCDATASection("cdata");
        root.appendChild(cdata);

        Comment comment = doc.createComment("comment");
        root.appendChild(comment);

        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        root.appendChild(pi);

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);

        assertTrue("node() must match element nodes",
                DOMNodePointer.testNode(root, nodeTest));
        assertTrue("node() must match document nodes",
                DOMNodePointer.testNode(doc, nodeTest));
        assertTrue("node() must match text nodes",
                DOMNodePointer.testNode(text, nodeTest));
        assertTrue("node() must match CDATA sections",
                DOMNodePointer.testNode(cdata, nodeTest));
        assertTrue("node() must match comments",
                DOMNodePointer.testNode(comment, nodeTest));
        assertTrue("node() must match processing instructions",
                DOMNodePointer.testNode(pi, nodeTest));
        assertTrue("node() must match attribute nodes",
                DOMNodePointer.testNode(attr, nodeTest));
    }

    @Test(timeout = 4000)
    public void testNodeTypeTextMatchesOnlyTextAndCdata() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Text text = doc.createTextNode("x");
        CDATASection cdata = doc.createCDATASection("y");
        Comment comment = doc.createComment("z");
        root.appendChild(text);
        root.appendChild(cdata);
        root.appendChild(comment);

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(text, textTest));
        assertTrue(DOMNodePointer.testNode(cdata, textTest));
        assertFalse(DOMNodePointer.testNode(comment, textTest));
        assertFalse(DOMNodePointer.testNode(root, textTest));
    }

    @Test(timeout = 4000)
    public void testNodeTypeCommentMatchesOnlyComment() throws Exception {
        Document doc = newDocument();
        Comment comment = doc.createComment("c");
        Text text = doc.createTextNode("t");

        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(comment, commentTest));
        assertFalse(DOMNodePointer.testNode(text, commentTest));
    }

    @Test(timeout = 4000)
    public void testNodeTypePIMatchesOnlyPI() throws Exception {
        Document doc = newDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        Element element = doc.createElement("root");

        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(pi, piTest));
        assertFalse(DOMNodePointer.testNode(element, piTest));
    }

    @Test(timeout = 4000)
    public void testNodeTypeUnknownReturnsFalse() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        NodeTypeTest unknown = new NodeTypeTest(123456);
        assertFalse(DOMNodePointer.testNode(element, unknown));
    }

    @Test(timeout = 4000)
    public void testNodeNameTestNonElementFalse() throws Exception {
        Document doc = newDocument();
        Text text = doc.createTextNode("x");
        NodeNameTest nameTest = new NodeNameTest(new QName("root"));
        assertFalse(DOMNodePointer.testNode(text, nameTest));
    }

    @Test(timeout = 4000)
    public void testNodeNameTestExactAndMismatch() throws Exception {
        Document doc = parse("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();

        NodeNameTest rootTest = new NodeNameTest(new QName("root"));
        assertTrue(DOMNodePointer.testNode(root, rootTest));
        assertFalse(DOMNodePointer.testNode(child, rootTest));

        NodeNameTest childTest = new NodeNameTest(new QName("child"));
        assertTrue(DOMNodePointer.testNode(child, childTest));
        assertFalse(DOMNodePointer.testNode(root, childTest));
    }

    @Test(timeout = 4000)
    public void testNodeNameTestWildcard() throws Exception {
        Document doc = parse("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();

        NodeNameTest wildcard = new NodeNameTest(new QName("*"));
        assertTrue(DOMNodePointer.testNode(root, wildcard));
        assertTrue(DOMNodePointer.testNode(child, wildcard));
    }

    @Test(timeout = 4000)
    public void testNodeNameTestNamespaceMatching() throws Exception {
        Document doc = parse("<root xmlns:p='urn:p'><p:item/></root>");
        Element pItem = (Element) doc.getElementsByTagNameNS("urn:p", "item").item(0);

        NodeNameTest nsTest = new NodeNameTest(new QName("p", "item"), "urn:p");
        assertTrue(DOMNodePointer.testNode(pItem, nsTest));

        NodeNameTest wrongNs = new NodeNameTest(new QName("p", "item"), "urn:other");
        assertFalse(DOMNodePointer.testNode(pItem, wrongNs));

        NodeNameTest prefixedWildcard = new NodeNameTest(new QName("p", "*"), "urn:p");
        assertTrue(DOMNodePointer.testNode(pItem, prefixedWildcard));

        NodeNameTest wrongName = new NodeNameTest(new QName("wrong"));
        assertFalse(DOMNodePointer.testNode(pItem, wrongName));
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionTest() throws Exception {
        Document doc = newDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        Element element = doc.createElement("root");

        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(pi, piTest));

        ProcessingInstructionTest otherTest = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(pi, otherTest));
        assertFalse(DOMNodePointer.testNode(element, piTest));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceUriForPrefixes() throws Exception {
        Document doc = parse("<root xmlns:p='urn:p' xmlns='urn:d'><p:item/></root>");
        Element pItem = (Element) doc.getElementsByTagNameNS("urn:p", "item").item(0);

        DOMNodePointer p = new DOMNodePointer(pItem, Locale.ENGLISH);
        assertEquals("urn:p", p.getNamespaceURI("p"));
        assertEquals("urn:p", p.getNamespaceURI("p"));
        assertEquals("urn:d", p.getNamespaceURI(null));
        assertEquals("urn:d", p.getNamespaceURI(""));
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, p.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, p.getNamespaceURI("xmlns"));
        assertNull(p.getNamespaceURI("unknown"));
        assertNull(p.getNamespaceURI("unknown"));

        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("urn:p", docPointer.getNamespaceURI("p"));
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceUri() throws Exception {
        Document doc = parse("<root xmlns='urn:d'/>");
        DOMNodePointer p = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertEquals("urn:d", p.getDefaultNamespaceURI());

        Document noNs = parse("<root/>");
        DOMNodePointer pNoNs = new DOMNodePointer(noNs.getDocumentElement(), Locale.ENGLISH);
        assertNull(pNoNs.getDefaultNamespaceURI());
        assertNull(pNoNs.getDefaultNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testStaticGetNamespaceUri() throws Exception {
        Document doc = parse("<root xmlns:p='urn:p' xmlns='urn:d'><p:item/><item/></root>");
        Element pItem = (Element) doc.getElementsByTagNameNS("urn:p", "item").item(0);
        Element defaultItem = (Element) doc.getElementsByTagNameNS("urn:d", "item").item(0);

        assertEquals("urn:p", DOMNodePointer.getNamespaceURI(pItem));
        assertEquals("urn:d", DOMNodePointer.getNamespaceURI(defaultItem));
        assertEquals("urn:d", DOMNodePointer.getNamespaceURI(doc));

        Document noNs = parse("<root/>");
        assertNull(DOMNodePointer.getNamespaceURI(noNs.getDocumentElement()));
    }

    @Test(timeout = 4000)
    public void testGetNameForElementAndPi() throws Exception {
        Document doc = parse("<root xmlns:p='urn:p'><p:item/></root>");
        Element pItem = (Element) doc.getElementsByTagNameNS("urn:p", "item").item(0);
        DOMNodePointer pItemPtr = new DOMNodePointer(pItem, Locale.ENGLISH);
        QName itemName = pItemPtr.getName();
        assertEquals("item", itemName.getName());
        assertEquals("p", itemName.getPrefix());

        DOMNodePointer rootPtr = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        QName rootName = rootPtr.getName();
        assertEquals("root", rootName.getName());
        assertNull(rootName.getPrefix());

        Document piDoc = parse("<?target data?><root/>");
        ProcessingInstruction pi = (ProcessingInstruction) piDoc.getFirstChild();
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("target", piPtr.getName().getName());
    }

    @Test(timeout = 4000)
    public void testBasicAccessorsAndLeaf() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        DOMNodePointer p = new DOMNodePointer(root, Locale.ENGLISH);

        assertSame(root, p.getBaseValue());
        assertSame(root, p.getImmediateNode());
        assertTrue(p.isActual());
        assertFalse(p.isCollection());
        assertEquals(1, p.getLength());
        assertTrue(p.isLeaf());

        root.appendChild(doc.createTextNode("x"));
        assertFalse(p.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetValueCommentTextElementPiAndXmlSpace() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Comment comment = doc.createComment("  comment  ");
        root.appendChild(comment);
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("comment", commentPtr.getValue());

        ProcessingInstruction pi = doc.createProcessingInstruction("t", "  pi-data  ");
        root.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("pi-data", piPtr.getValue());

        Element child = doc.createElement("child");
        Text text = doc.createTextNode("  hello  ");
        child.appendChild(text);
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("hello", childPtr.getValue());

        child.setAttribute("xml:space", "preserve");
        assertEquals("  hello  ", childPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueTextAndCdata() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Text text = doc.createTextNode("old");
        root.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        textPtr.setValue("new");
        assertEquals("new", text.getNodeValue());

        Text emptyText = doc.createTextNode("old");
        root.appendChild(emptyText);
        DOMNodePointer emptyPtr = new DOMNodePointer(emptyText, Locale.ENGLISH);
        emptyPtr.setValue("");
        assertNull(emptyText.getParentNode());

        Text nullText = doc.createTextNode("old");
        root.appendChild(nullText);
        DOMNodePointer nullPtr = new DOMNodePointer(nullText, Locale.ENGLISH);
        nullPtr.setValue(null);
        assertNull(nullText.getParentNode());

        CDATASection cdata = doc.createCDATASection("old");
        root.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.ENGLISH);
        cdataPtr.setValue("new-cdata");
        assertEquals("new-cdata", cdata.getNodeValue());
    }

    @Test(timeout = 4000)
    public void testSetValueElementWithString() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element elem = doc.createElement("elem");
        elem.appendChild(doc.createTextNode("old"));
        root.appendChild(elem);

        DOMNodePointer p = new DOMNodePointer(elem, Locale.ENGLISH);
        p.setValue("new");
        assertEquals("new", elem.getTextContent());
        assertEquals(1, elem.getChildNodes().getLength());

        Element emptyElem = doc.createElement("empty");
        emptyElem.appendChild(doc.createTextNode("old"));
        root.appendChild(emptyElem);
        DOMNodePointer emptyPtr = new DOMNodePointer(emptyElem, Locale.ENGLISH);
        emptyPtr.setValue("");
        assertEquals(0, emptyElem.getChildNodes().getLength());
    }

    @Test(timeout = 4000)
    public void testSetValueElementWithNode() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element container = doc.createElement("container");
        container.appendChild(doc.createElement("old"));
        root.appendChild(container);

        Element source = doc.createElement("source");
        Element sourceChild = doc.createElement("child");
        source.appendChild(sourceChild);

        DOMNodePointer containerPtr = new DOMNodePointer(container, Locale.ENGLISH);
        containerPtr.setValue(source);
        assertEquals(1, container.getChildNodes().getLength());
        assertEquals("child", container.getFirstChild().getNodeName());

        Element textContainer = doc.createElement("textContainer");
        textContainer.appendChild(doc.createElement("old"));
        root.appendChild(textContainer);

        DOMNodePointer textContainerPtr = new DOMNodePointer(textContainer, Locale.ENGLISH);
        textContainerPtr.setValue(doc.createTextNode("abc"));
        assertEquals(1, textContainer.getChildNodes().getLength());
        assertEquals(Node.TEXT_NODE, textContainer.getFirstChild().getNodeType());
        assertEquals("abc", textContainer.getFirstChild().getNodeValue());

        Element docContainer = doc.createElement("docContainer");
        docContainer.appendChild(doc.createElement("old"));
        root.appendChild(docContainer);

        Document valueDoc = newDocument();
        Element valueRoot = valueDoc.createElement("valueRoot");
        valueDoc.appendChild(valueRoot);

        DOMNodePointer docContainerPtr = new DOMNodePointer(docContainer, Locale.ENGLISH);
        docContainerPtr.setValue(valueDoc);
        assertEquals(1, docContainer.getChildNodes().getLength());
        assertEquals("valueRoot", docContainer.getFirstChild().getNodeName());
    }

    @Test(timeout = 4000)
    public void testIsLanguageAndFindEnclosingAttribute() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        root.setAttribute("xml:lang", "en");
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        assertTrue(rootPtr.isLanguage("EN"));
        assertFalse(rootPtr.isLanguage("fr"));

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);
        assertTrue(childPtr.isLanguage("en"));
        assertEquals("en", DOMNodePointer.findEnclosingAttribute(child, "xml:lang"));
        assertNull(DOMNodePointer.findEnclosingAttribute(child, "missing"));
    }

    @Test(timeout = 4000)
    public void testAsPathForElementAndText() throws Exception {
        Document doc = parse("<root><a/><a/><b/></root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        DOMNodePointer rootPtr = new DOMNodePointer(docPtr, root);

        assertEquals("", docPtr.asPath());
        assertEquals("/root[1]", rootPtr.asPath());

        Element a1 = (Element) root.getFirstChild();
        Element a2 = (Element) root.getChildNodes().item(1);
        Element b = (Element) root.getChildNodes().item(2);

        DOMNodePointer a1Ptr = new DOMNodePointer(rootPtr, a1);
        DOMNodePointer a2Ptr = new DOMNodePointer(rootPtr, a2);
        DOMNodePointer bPtr = new DOMNodePointer(rootPtr, b);

        assertEquals("/root[1]/a[1]", a1Ptr.asPath());
        assertEquals("/root[1]/a[2]", a2Ptr.asPath());
        assertEquals("/root[1]/b[1]", bPtr.asPath());

        Text text = doc.createTextNode("x");
        a1.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(a1Ptr, text);
        assertEquals("/root[1]/a[1]/text()[1]", textPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathForCdataAndPi() throws Exception {
        Document doc = parse("<root><a/></root>");
        Element root = doc.getDocumentElement();
        Element a = (Element) root.getFirstChild();
        DOMNodePointer rootPtr = new DOMNodePointer(new DOMNodePointer(doc, Locale.ENGLISH), root);
        DOMNodePointer aPtr = new DOMNodePointer(rootPtr, a);

        Text text = doc.createTextNode("x");
        a.appendChild(text);
        CDATASection cdata = doc.createCDATASection("y");
        a.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(aPtr, cdata);
        assertEquals("/root[1]/a[1]/text()[2]", cdataPtr.asPath());

        Document piDoc = parse("<root><?target data?></root>");
        Element piRoot = piDoc.getDocumentElement();
        DOMNodePointer piRootPtr = new DOMNodePointer(new DOMNodePointer(piDoc, Locale.ENGLISH), piRoot);
        ProcessingInstruction pi = (ProcessingInstruction) piRoot.getFirstChild();
        DOMNodePointer piPtr = new DOMNodePointer(piRootPtr, pi);
        assertEquals("/root[1]/processing-instruction('target')[1]", piPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathForNamespacedElement() throws Exception {
        Document doc = parse("<root xmlns:p='urn:p'><p:item/><p:item/></root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(new DOMNodePointer(doc, Locale.ENGLISH), root);

        NodeList items = doc.getElementsByTagNameNS("urn:p", "item");
        Element item1 = (Element) items.item(0);
        Element item2 = (Element) items.item(1);

        DOMNodePointer item1Ptr = new DOMNodePointer(rootPtr, item1);
        DOMNodePointer item2Ptr = new DOMNodePointer(rootPtr, item2);

        assertEquals("/root[1]/p:item[1]", item1Ptr.asPath());
        assertEquals("/root[1]/p:item[2]", item2Ptr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathForIdEscaping() throws Exception {
        Document doc = parse("<root/>");
        Element root = doc.getDocumentElement();

        DOMNodePointer simpleId = new DOMNodePointer(root, Locale.ENGLISH, "x");
        assertEquals("id('x')", simpleId.asPath());

        DOMNodePointer escapedId = new DOMNodePointer(root, Locale.ENGLISH, "a'b\"c");
        assertEquals("id('a&apos;b&quot;c')", escapedId.asPath());
    }

    @Test(timeout = 4000)
    public void testRemove() throws Exception {
        Document doc = parse("<root><child/></root>");
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);
        childPtr.remove();
        assertEquals(0, root.getChildNodes().getLength());

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        try {
            rootPtr.remove();
            fail("Expected JXPathException for root removal");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() throws Exception {
        Document doc = parse("<root/><other/>");
        Element root = doc.getDocumentElement();
        Element other = (Element) doc.getElementsByTagName("other").item(0);

        DOMNodePointer p1 = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer p2 = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer p3 = new DOMNodePointer(other, Locale.ENGLISH);

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
        assertFalse(p1.equals(null));
        assertFalse(p1.equals(new Object()));
        assertFalse(p1.equals(p3));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = parse("<root a='1'><a/><b/></root>");
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        Element a = (Element) root.getChildNodes().item(0);
        Element b = (Element) root.getChildNodes().item(1);
        DOMNodePointer aPtr = new DOMNodePointer(rootPtr, a);
        DOMNodePointer bPtr = new DOMNodePointer(rootPtr, b);

        assertEquals(0, rootPtr.compareChildNodePointers(aPtr, aPtr));
        assertTrue(rootPtr.compareChildNodePointers(aPtr, bPtr) < 0);
        assertTrue(rootPtr.compareChildNodePointers(bPtr, aPtr) > 0);

        Attr attrA = root.getAttributeNode("a");
        DOMNodePointer attrAPtr = new DOMNodePointer(rootPtr, attrA);

        assertTrue(rootPtr.compareChildNodePointers(attrAPtr, aPtr) < 0);
        assertTrue(rootPtr.compareChildNodePointers(aPtr, attrAPtr) > 0);

        Attr attrA2 = root.getAttributeNode("a");
        DOMNodePointer attrA2Ptr = new DOMNodePointer(rootPtr, attrA2);
        assertEquals(0, rootPtr.compareChildNodePointers(attrAPtr, attrA2Ptr));
    }

    @Test(timeout = 4000)
    public void testCreateAttribute() throws Exception {
        Document doc = parse("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer p = new DOMNodePointer(root, Locale.ENGLISH);

        NodePointer ptr = p.createAttribute(null, new QName("attr"));
        assertNotNull(ptr);
        assertTrue(root.hasAttribute("attr"));
        assertEquals(1, root.getAttributes().getLength());

        p.createAttribute(null, new QName("attr"));
        assertEquals(1, root.getAttributes().getLength());
    }

    @Test(timeout = 4000)
    public void testCreateAttributeWithKnownPrefix() throws Exception {
        Document doc = parse("<root xmlns:p='urn:p'/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer p = new DOMNodePointer(root, Locale.ENGLISH);

        p.createAttribute(null, new QName("p", "attr"));
        assertEquals("urn:p", root.getAttributeNS("urn:p", "attr"));
    }

    @Test(timeout = 4000)
    public void testCreateAttributeUnknownPrefixThrows() throws Exception {
        Document doc = parse("<root/>");
        Element root = doc.getDocumentElement();
        DOMNodePointer p = new DOMNodePointer(root, Locale.ENGLISH);

        try {
            p.createAttribute(null, new QName("p", "attr"));
            fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateChildWithoutFactoryThrows() throws Exception {
        JXPathContext context = JXPathContext.newContext(new Object());
        Document doc = parse("<root/>");
        DOMNodePointer p = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);

        try {
            p.createChild(context, new QName("child"), 0);
            fail("Expected JXPathException when factory is not set");
        } catch (JXPathException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() throws Exception {
        String xml = "<?xml version='1.0'?>\n"
            + "<!DOCTYPE root [\n"
            + "  <!ELEMENT root ANY>\n"
            + "  <!ATTLIST root id ID #IMPLIED>\n"
            + "]>\n"
            + "<root id='foo'/>";

        Document doc = parse(xml);
        DOMNodePointer p = new DOMNodePointer(doc, Locale.ENGLISH);

        Pointer found = p.getPointerByID(null, "foo");
        assertTrue(found instanceof DOMNodePointer);
        assertEquals("id('foo')", found.asPath());

        Pointer missing = p.getPointerByID(null, "nope");
        assertTrue(missing instanceof NullPointer);
    }

    @Test(timeout = 4000)
    public void testGetNamespaceResolverNotNull() throws Exception {
        Document doc = parse("<root xmlns:p='urn:p'/>");
        DOMNodePointer p = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNotNull(p.getNamespaceResolver());
        assertNotNull(p.getNamespaceResolver());
    }
}