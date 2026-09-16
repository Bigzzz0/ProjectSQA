/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: DOMNodePointer
 *
 * Branch & Condition Coverage:
 * 1. testNode:
 *    - test == null (true)
 *    - NodeNameTest: node is not ELEMENT_NODE (false), wildcard with null prefix (true),
 *      wildcard with prefix, matching vs non-matching name & namespaceURI
 *    - NodeTypeTest: NODE_TYPE_NODE (Element, Document), NODE_TYPE_TEXT (Text, CDATA),
 *      NODE_TYPE_COMMENT, NODE_TYPE_PI, default (false)
 *    - ProcessingInstructionTest: PI node matching target vs non-matching target, non-PI node
 *    - Custom NodeTest (false)
 * 2. getName:
 *    - ELEMENT_NODE (with/without prefix), PROCESSING_INSTRUCTION_NODE, DOCUMENT_NODE / other
 * 3. getNamespaceURI(prefix):
 *    - null / "" (default NS), "xml" (XML_NAMESPACE_URI), "xmlns" (XMLNS_NAMESPACE_URI)
 *    - cached in namespaces map vs uncached (walks up DOM hierarchy)
 *    - found in Element vs not found (UNKNOWN_NAMESPACE -> returns null)
 *    - Document node handling (delegates to DocumentElement)
 * 4. getDefaultNamespaceURI:
 *    - found in Element vs parent vs not found (returns null)
 * 5. setValue:
 *    - TEXT / CDATA: non-empty string (setNodeValue) vs empty/null (removes child from parent)
 *    - ELEMENT: clears existing children; value is Element/Document (appends cloned children),
 *      value is other Node (appends clone), value is String/other (appends TextNode)
 * 6. createChild:
 *    - WHOLE_COLLECTION vs specific index
 *    - Factory creates child successfully vs factory returns false (JXPathAbstractFactoryException)
 *    - Factory missing (JXPathException)
 * 7. createAttribute:
 *    - on non-Element (delegates to super)
 *    - prefix present: resolves namespace vs unknown prefix (throws JXPathException)
 *    - prefix absent: hasAttribute vs sets attribute
 * 8. remove:
 *    - parent != null (removes child) vs parent == null (throws JXPathException)
 * 9. asPath:
 *    - id != null: id('...') with escaping single and double quotes
 *    - parent is DOMNodePointer: matching default NS vs prefix NS vs node() positional
 *    - TEXT / CDATA (/text()[n]), PI (/processing-instruction('...')[n]), DOCUMENT_NODE
 * 10. compareChildNodePointers:
 *    - pointer1 == pointer2 (0)
 *    - pointer1 Attr vs pointer2 Element (-1)
 *    - pointer1 Element vs pointer2 Attr (1)
 *    - both Attrs: order in NamedNodeMap
 *    - both Elements/Nodes: sibling order
 * 11. Known Defect Ground Truth:
 *    - XMLSpaceTest defect: stringValue(node) on element nodes incorrectly traverses into
 *      COMMENT_NODE and PROCESSING_INSTRUCTION_NODE and appends their content into the element's string value.
 *      According to XPath 1.0, string-value of an element is the concatenation of text-node descendants only.
 */
public class DOMNodePointerGeminiTest {

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().newDocument();
    }

    private Document createNonNamespaceAwareDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().newDocument();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (XPath String Value & Comments)
    // =========================================================================

    /**
     * Target Defect Ground Truth:
     * In DOMNodePointer.stringValue(Node node), comments and processing instructions
     * should NOT contribute to an Element's string-value under standard XPath rules.
     * The defective implementation recurses into non-TEXT children and appends comment/PI data.
     */
    @Test(timeout = 4000)
    public void testDefectElementValueMustExcludeComments() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("test");
        doc.appendChild(root);

        root.appendChild(doc.createTextNode("foo"));
        Comment comment = doc.createComment("comment");
        root.appendChild(comment);
        root.appendChild(doc.createTextNode("bar"));

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        // Under XPath 1.0 specifications, the string-value of <test> is "foobar", not "foocommentbar"
        assertEquals("foobar", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testDefectElementValueMustExcludeProcessingInstructions() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("test");
        doc.appendChild(root);

        root.appendChild(doc.createTextNode("foo"));
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        root.appendChild(pi);
        root.appendChild(doc.createTextNode("bar"));

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("foobar", pointer.getValue());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("root");
        doc.appendChild(elem);

        DOMNodePointer rootPointer = new DOMNodePointer(doc, Locale.US, "rootId");
        assertEquals("rootId", rootPointer.asPath());
        assertSame(doc, rootPointer.getBaseValue());
        assertSame(doc, rootPointer.getImmediateNode());
        assertTrue(rootPointer.isActual());
        assertFalse(rootPointer.isCollection());
        assertEquals(1, rootPointer.getLength());
        assertFalse(rootPointer.isLeaf());

        DOMNodePointer elemPointer = new DOMNodePointer(rootPointer, elem);
        assertSame(elem, elemPointer.getBaseValue());
        assertTrue(elemPointer.isLeaf()); // no children yet
    }

    @Test(timeout = 4000)
    public void testGetName() throws Exception {
        Document doc = createDocument();
        Element elemWithNs = doc.createElementNS("http://example.com/ns", "p:item");
        DOMNodePointer ptr1 = new DOMNodePointer(elemWithNs, Locale.US);
        assertEquals(new QName("p", "item"), ptr1.getName());

        Element elemNoPrefix = doc.createElement("simple");
        DOMNodePointer ptr2 = new DOMNodePointer(elemNoPrefix, Locale.US);
        assertEquals(new QName(null, "simple"), ptr2.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("my-target", "some data");
        DOMNodePointer ptr3 = new DOMNodePointer(pi, Locale.US);
        assertEquals(new QName(null, "my-target"), ptr3.getName());

        DOMNodePointer ptrDoc = new DOMNodePointer(doc, Locale.US);
        assertEquals(new QName(null, null), ptrDoc.getName());
    }

    @Test(timeout = 4000)
    public void testGetPrefixAndLocalNameNonNamespaceAware() throws Exception {
        Document doc = createNonNamespaceAwareDocument();
        Element elem = doc.createElement("ns:customTag");
        assertEquals("ns", DOMNodePointer.getPrefix(elem));
        assertEquals("customTag", DOMNodePointer.getLocalName(elem));

        Element simpleElem = doc.createElement("plainTag");
        assertNull(DOMNodePointer.getPrefix(simpleElem));
        assertEquals("plainTag", DOMNodePointer.getLocalName(simpleElem));
    }

    @Test(timeout = 4000)
    public void testNodeNameTest() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://example.com/ns", "ex:node");
        doc.appendChild(elem);
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);

        assertTrue(ptr.testNode(null));

        // Non-element node should return false for NodeNameTest
        Text textNode = doc.createTextNode("hello");
        elem.appendChild(textNode);
        assertFalse(DOMNodePointer.testNode(textNode, new NodeNameTest(new QName("ex", "node"))));

        // Wildcard with null prefix matches any element
        assertTrue(ptr.testNode(new NodeNameTest(new QName(null, "*"))));

        // Matching name and URI
        assertTrue(ptr.testNode(new NodeNameTest(new QName("ex", "node"), "http://example.com/ns")));

        // Mismatched URI
        assertFalse(ptr.testNode(new NodeNameTest(new QName("ex", "node"), "http://wrong.com")));

        // Wildcard with specific URI
        assertTrue(ptr.testNode(new NodeNameTest(new QName("ex", "*"), "http://example.com/ns")));

        // Mismatched local name
        assertFalse(ptr.testNode(new NodeNameTest(new QName("other"), "http://example.com/ns")));
    }

    @Test(timeout = 4000)
    public void testNodeTypeTest() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("element");
        Text text = doc.createTextNode("txt");
        CDATASection cdata = doc.createCDATASection("cdata");
        Comment comment = doc.createComment("comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("pi", "data");

        // NODE_TYPE_NODE (Element or Document)
        assertTrue(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(doc, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        // NODE_TYPE_TEXT (Text or CDATA)
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        // NODE_TYPE_COMMENT
        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        // NODE_TYPE_PI
        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        // Unknown node type
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(9999)));
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionTest() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("appTarget", "someData");
        Element elem = doc.createElement("element");

        assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("appTarget")));
        assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("otherTarget")));
        assertFalse(DOMNodePointer.testNode(elem, new ProcessingInstructionTest("appTarget")));

        // Custom unsupported NodeTest returns false
        NodeTest customTest = new NodeTest() {};
        assertFalse(DOMNodePointer.testNode(pi, customTest));
    }

    @Test(timeout = 4000)
    public void testNamespaceURILookup() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("xmlns", "http://default.ns");
        parent.setAttribute("xmlns:p1", "http://p1.ns");
        doc.appendChild(parent);

        Element child = doc.createElement("child");
        child.setAttribute("xmlns:p2", "http://p2.ns");
        parent.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);

        assertEquals("http://default.ns", childPtr.getNamespaceURI(""));
        assertEquals("http://default.ns", childPtr.getNamespaceURI(null));
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, childPtr.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, childPtr.getNamespaceURI("xmlns"));
        assertEquals("http://p2.ns", childPtr.getNamespaceURI("p2"));
        assertEquals("http://p1.ns", childPtr.getNamespaceURI("p1"));
        assertNull(childPtr.getNamespaceURI("nonExistent"));

        // Document node delegation
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        assertEquals("http://default.ns", docPtr.getDefaultNamespaceURI());
        assertEquals("http://p1.ns", docPtr.getNamespaceURI("p1"));
    }

    @Test(timeout = 4000)
    public void testIsLanguageHierarchy() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        doc.appendChild(parent);

        Element child = doc.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        assertTrue(childPtr.isLanguage("en"));
        assertTrue(childPtr.isLanguage("en-US"));
        assertFalse(childPtr.isLanguage("fr"));

        Element noLangElem = doc.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLangElem, Locale.FRANCE);
        assertTrue(noLangPtr.isLanguage("fr"));
        assertFalse(noLangPtr.isLanguage("en"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & State Mutations
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetValueOnTextAndCDATA() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Text text = doc.createTextNode("initial");
        root.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);

        textPtr.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        // Empty string removes text node from its parent
        textPtr.setValue("");
        assertNull(text.getParentNode());

        CDATASection cdata = doc.createCDATASection("cdataInit");
        root.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(cdata, Locale.US);

        cdataPtr.setValue("cdataUpdated");
        assertEquals("cdataUpdated", cdata.getNodeValue());

        cdataPtr.setValue(null);
        assertNull(cdata.getParentNode());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithStringAndNodes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        root.appendChild(doc.createTextNode("oldContent"));

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        // String value replaces children with new Text node
        rootPtr.setValue("newContent");
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("newContent", root.getFirstChild().getNodeValue());

        // Node value (Element) copies children of passed element
        Element donor = doc.createElement("donor");
        donor.appendChild(doc.createElement("c1"));
        donor.appendChild(doc.createElement("c2"));
        rootPtr.setValue(donor);
        assertEquals(2, root.getChildNodes().getLength());
        assertEquals("c1", root.getFirstChild().getNodeName());
        assertEquals("c2", root.getLastChild().getNodeName());

        // Node value (Single Text Node)
        Text singleText = doc.createTextNode("single");
        rootPtr.setValue(singleText);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("single", root.getFirstChild().getNodeValue());

        // Node value (Document) copies children of document
        Document donorDoc = createDocument();
        donorDoc.appendChild(donorDoc.createElement("docChild"));
        rootPtr.setValue(donorDoc);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("docChild", root.getFirstChild().getNodeName());
    }

    @Test(timeout = 4000)
    public void testGetValuesForDifferentNodeTypes() throws Exception {
        Document doc = createDocument();
        Comment comment = doc.createComment(" comment text ");
        assertEquals("comment text", new DOMNodePointer(comment, Locale.US).getValue());

        ProcessingInstruction pi = doc.createProcessingInstruction("target", " pi data ");
        assertEquals("pi data", new DOMNodePointer(pi, Locale.US).getValue());

        CDATASection cdata = doc.createCDATASection(" cdata text ");
        assertEquals("cdata text", new DOMNodePointer(cdata, Locale.US).getValue());

        Text text = doc.createTextNode(" text value ");
        assertEquals("text value", new DOMNodePointer(text, Locale.US).getValue());
    }

    @Test(timeout = 4000)
    public void testAsPathGeneration() throws Exception {
        Document doc = createDocument();
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        assertEquals("", docPtr.asPath());

        Element root = doc.createElement("root");
        doc.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(docPtr, root);
        assertEquals("/root[1]", rootPtr.asPath());

        Element child1 = doc.createElement("item");
        root.appendChild(child1);
        DOMNodePointer child1Ptr = new DOMNodePointer(rootPtr, child1);
        assertEquals("/root[1]/item[1]", child1Ptr.asPath());

        Element child2 = doc.createElement("item");
        root.appendChild(child2);
        DOMNodePointer child2Ptr = new DOMNodePointer(rootPtr, child2);
        assertEquals("/root[1]/item[2]", child2Ptr.asPath());

        Text textNode = doc.createTextNode("content");
        child2.appendChild(textNode);
        DOMNodePointer textPtr = new DOMNodePointer(child2Ptr, textNode);
        assertEquals("/root[1]/item[2]/text()[1]", textPtr.asPath());

        ProcessingInstruction pi = doc.createProcessingInstruction("app", "conf");
        child2.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(child2Ptr, pi);
        assertEquals("/root[1]/item[2]/processing-instruction('app')[1]", piPtr.asPath());

        DOMNodePointer idPtr = new DOMNodePointer(root, Locale.US, "foo'\"bar");
        assertEquals("id('foo&apos;&quot;bar')", idPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("attr1", "v1");
        parent.setAttribute("attr2", "v2");
        doc.appendChild(parent);

        Element child1 = doc.createElement("c1");
        Element child2 = doc.createElement("c2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.US);
        DOMNodePointer pChild1 = new DOMNodePointer(parentPtr, child1);
        DOMNodePointer pChild2 = new DOMNodePointer(parentPtr, child2);

        // Same pointer
        assertEquals(0, parentPtr.compareChildNodePointers(pChild1, pChild1));
        // Sibling order: child1 comes before child2
        assertEquals(-1, parentPtr.compareChildNodePointers(pChild1, pChild2));
        assertEquals(1, parentPtr.compareChildNodePointers(pChild2, pChild1));

        Attr attr1 = parent.getAttributeNode("attr1");
        Attr attr2 = parent.getAttributeNode("attr2");
        DOMNodePointer pAttr1 = new DOMNodePointer(parentPtr, attr1);
        DOMNodePointer pAttr2 = new DOMNodePointer(parentPtr, attr2);

        // Attribute vs Child
        assertEquals(-1, parentPtr.compareChildNodePointers(pAttr1, pChild1));
        assertEquals(1, parentPtr.compareChildNodePointers(pChild1, pAttr1));

        // Attribute vs Attribute
        assertEquals(-1, parentPtr.compareChildNodePointers(pAttr1, pAttr2));
        assertEquals(1, parentPtr.compareChildNodePointers(pAttr2, pAttr1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootThrowsException() throws Exception {
        Document doc = createDocument();
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        docPtr.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveNonRootNodeSuccess() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);
        doc.appendChild(root);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        childPtr.remove();
        assertNull(child.getParentNode());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeWithUnknownPrefixThrowsException() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ptr.createAttribute(ctx, new QName("unknownPrefix", "myAttr"));
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateChildWithoutFactoryThrowsException() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ptr.createChild(ctx, new QName("child"), 0);
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildFactoryReturnsFalseThrowsException() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ctx.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                return false;
            }
        });
        ptr.createChild(ctx, new QName("child"), 0);
    }

    @Test(timeout = 4000)
    public void testCreateChildAndAttributeSuccess() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        elem.setAttribute("xmlns:p", "http://example.com/ns");
        doc.appendChild(elem);

        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ctx.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                Element p = (Element) parent;
                Element child = p.getOwnerDocument().createElement(name);
                p.appendChild(child);
                return true;
            }
        });

        NodePointer createdChild = ptr.createChild(ctx, new QName("myChild"), 0, "childValue");
        assertNotNull(createdChild);
        assertEquals("childValue", createdChild.getValue());

        NodePointer createdAttr = ptr.createAttribute(ctx, new QName("p", "newAttr"));
        assertNotNull(createdAttr);
        assertTrue(elem.hasAttributeNS("http://example.com/ns", "newAttr"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() throws Exception {
        Document doc = createDocument();
        Element elem1 = doc.createElement("e1");
        Element elem2 = doc.createElement("e2");

        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer ptr1Same = new DOMNodePointer(elem1, Locale.CANADA);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.US);

        assertEquals(ptr1, ptr1);
        assertEquals(ptr1, ptr1Same);
        assertNotEquals(ptr1, ptr2);
        assertNotEquals(ptr1, null);
        assertNotEquals(ptr1, "notAPointer");

        assertEquals(System.identityHashCode(elem1), ptr1.hashCode());
    }

    @Test(timeout = 4000)
    public void testIteratorsAndNamespacePointers() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        elem.setAttribute("xmlns:test", "http://test.com");
        doc.appendChild(elem);

        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        NodeIterator childIt = ptr.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = ptr.attributeIterator(new QName("test"));
        assertNotNull(attrIt);

        NodeIterator nsIt = ptr.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = ptr.namespacePointer("test");
        assertNotNull(nsPtr);
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        elem.setAttribute("id", "targetId");
        elem.setIdAttribute("id", true);
        doc.appendChild(elem);

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.US);
        JXPathContext ctx = JXPathContext.newContext(doc);

        Pointer found = docPtr.getPointerByID(ctx, "targetId");
        assertNotNull(found);
        assertSame(elem, found.getNode());

        Pointer notFound = docPtr.getPointerByID(ctx, "missingId");
        assertTrue(notFound instanceof NullPointer);
    }
}