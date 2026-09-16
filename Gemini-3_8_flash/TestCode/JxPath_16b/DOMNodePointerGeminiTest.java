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

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
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
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.jxpath.ri.model.dom.DOMNodePointer
 *
 * Decision / Branch Coverage Targets:
 * - testNode(Node, NodeTest):
 *   * test == null (true)
 *   * NodeNameTest: ELEMENT_NODE vs non-ELEMENT_NODE (false)
 *   * NodeNameTest: wildcard with null prefix (true)
 *   * NodeNameTest: wildcard vs matching localName
 *   * NodeNameTest: namespaceURI matching (equalStrings true/false) vs null namespaceURI + prefix matching
 *   * NodeTypeTest: NODE_TYPE_NODE (ELEMENT/DOCUMENT vs others)
 *   * NodeTypeTest: NODE_TYPE_TEXT (TEXT/CDATA vs others)
 *   * NodeTypeTest: NODE_TYPE_COMMENT (COMMENT vs others)
 *   * NodeTypeTest: NODE_TYPE_PI (PI vs others)
 *   * NodeTypeTest: unknown node type (false)
 *   * ProcessingInstructionTest: PI node vs non-PI node; matching target vs non-matching
 *   * Other NodeTest implementation (false)
 * - equalStrings(String, String): s1 == s2, s1 null, s2 null, trimmed comparison
 * - getName(): ELEMENT_NODE (with prefix, without prefix), PI node, other node types
 * - getNamespaceURI(String): null/empty (default), "xml", "xmlns", cached map, DOM traversal, unknown
 * - getDefaultNamespaceURI(): Document vs Element, xmlns attribute found vs not found
 * - setValue(Object):
 *   * TEXT_NODE/CDATA_SECTION_NODE: non-empty string vs empty/null (removes child)
 *   * Element/Document: Element/Document value (clones children), Node value (clones node), String value (non-empty vs empty)
 * - createChild(...): factory missing exception, factory returns false (JXPathAbstractFactoryException), factory returns true
 * - createAttribute(...): non-Element node delegate, prefixed attribute with unknown prefix (exception) vs known prefix, unprefixed
 * - remove(): root node (parent == null -> exception) vs child node
 * - asPath():
 *   * id != null (escape quotes and apostrophes)
 *   * ELEMENT_NODE: with parent DOMNodePointer (namespace prefix resolved vs node() fallback vs no namespace)
 *   * TEXT_NODE / CDATA_SECTION_NODE: /text()[pos]
 *   * PROCESSING_INSTRUCTION_NODE: /processing-instruction('...')[pos]
 *   * DOCUMENT_NODE: empty
 * - getValue() & stringValue():
 *   * Comment (trimmed text)
 *   * xml:space preserve vs non-preserve (trimming behavior)
 *   * Child traversal concatenation
 * - compareChildNodePointers: pointer1 == pointer2, attr vs non-attr, non-attr vs attr, attr vs attr, sibling traversal order
 * - Known Defect Reproduction: Following / Preceding axis ordering with child text/element hierarchies.
 */
public class DOMNodePointerGeminiTest {

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    private Document createNonNamespaceDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US, "rootId");

        assertSame(root, rootPointer.getBaseValue());
        assertSame(root, rootPointer.getImmediateNode());
        assertTrue(rootPointer.isActual());
        assertFalse(rootPointer.isCollection());
        assertEquals(1, rootPointer.getLength());
        assertTrue(rootPointer.isLeaf());

        root.appendChild(doc.createElement("child"));
        assertFalse(rootPointer.isLeaf());

        assertEquals(System.identityHashCode(root), rootPointer.hashCode());
        assertTrue(rootPointer.equals(rootPointer));
        assertTrue(rootPointer.equals(new DOMNodePointer(root, Locale.US)));
        assertFalse(rootPointer.equals(null));
        assertFalse(rootPointer.equals("string"));
    }

    @Test(timeout = 4000)
    public void testGetNameForVariousNodeTypes() throws Exception {
        Document doc = createDocument();
        Element element = doc.createElementNS("http://example.com/ns", "ex:item");
        DOMNodePointer elemPointer = new DOMNodePointer(element, Locale.ENGLISH);
        QName elemName = elemPointer.getName();
        assertEquals("ex", elemName.getPrefix());
        assertEquals("item", elemName.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("myTarget", "my-data");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        QName piName = piPointer.getName();
        assertNull(piName.getPrefix());
        assertEquals("myTarget", piName.getName());

        Text text = doc.createTextNode("hello");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        QName textName = textPointer.getName();
        assertNull(textName.getPrefix());
        assertNull(textName.getName());
    }

    @Test(timeout = 4000)
    public void testNamespaceResolution() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://default.org", "root");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://default.org");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", "http://foo.org");
        doc.appendChild(root);

        Element child = doc.createElementNS("http://foo.org", "foo:child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(root, Locale.ENGLISH);

        assertEquals("http://www.w3.org/XML/1998/namespace", childPointer.getNamespaceURI("xml"));
        assertEquals("http://www.w3.org/2000/xmlns/", childPointer.getNamespaceURI("xmlns"));
        assertEquals("http://default.org", childPointer.getNamespaceURI(null));
        assertEquals("http://default.org", childPointer.getNamespaceURI(""));
        assertEquals("http://foo.org", childPointer.getNamespaceURI("foo"));
        assertNull(childPointer.getNamespaceURI("unknownPrefix"));

        // Verify caching branch
        assertEquals("http://foo.org", childPointer.getNamespaceURI("foo"));

        assertNotNull(childPointer.namespacePointer("foo"));
        assertNotNull(childPointer.namespaceIterator());
        assertNotNull(childPointer.childIterator(null, false, null));
        assertNotNull(childPointer.attributeIterator(new QName("attr")));
        assertNotNull(childPointer.getNamespaceResolver());
    }

    @Test(timeout = 4000)
    public void testIsLanguage() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = doc.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("en-US"));
        assertFalse(childPointer.isLanguage("fr"));

        Element noLang = doc.createElement("noLang");
        DOMNodePointer noLangPointer = new DOMNodePointer(noLang, Locale.GERMAN);
        assertTrue(noLangPointer.isLanguage("de"));
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextAndCDataNodes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        Text text = doc.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.US);
        textPointer.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        // Empty value causes removal from parent
        textPointer.setValue("");
        assertNull(text.getParentNode());

        CDATASection cdata = doc.createCDATASection("initial-cdata");
        root.appendChild(cdata);
        DOMNodePointer cdataPointer = new DOMNodePointer(cdata, Locale.US);
        cdataPointer.setValue(null);
        assertNull(cdata.getParentNode());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementNodes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.appendChild(doc.createElement("oldChild"));

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);

        // Set string value
        rootPointer.setValue("textValue");
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("textValue", root.getFirstChild().getNodeValue());

        // Set empty string value
        rootPointer.setValue("");
        assertEquals(0, root.getChildNodes().getLength());

        // Set Element value (children cloned)
        Element sourceElem = doc.createElement("source");
        sourceElem.appendChild(doc.createElement("sub1"));
        sourceElem.appendChild(doc.createElement("sub2"));
        rootPointer.setValue(sourceElem);
        assertEquals(2, root.getChildNodes().getLength());
        assertEquals("sub1", root.getFirstChild().getNodeName());

        // Set other Node value (e.g., text node directly)
        Text singleText = doc.createTextNode("clonedText");
        rootPointer.setValue(singleText);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("clonedText", root.getFirstChild().getNodeValue());
    }

    @Test(timeout = 4000)
    public void testGetValueAndStringValueFormatting() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");

        Comment comment = doc.createComment("  test comment  ");
        root.appendChild(comment);
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.US);
        assertEquals("test comment", commentPointer.getValue());

        Element textContainer = doc.createElement("container");
        textContainer.appendChild(doc.createTextNode("  trimmed  "));
        DOMNodePointer textContainerPointer = new DOMNodePointer(textContainer, Locale.US);
        assertEquals("trimmed", textContainerPointer.getValue());

        // Preserve whitespace
        Element preserveElem = doc.createElement("preserveElem");
        preserveElem.setAttribute("xml:space", "preserve");
        preserveElem.appendChild(doc.createTextNode("  not trimmed  "));
        DOMNodePointer preservePointer = new DOMNodePointer(preserveElem, Locale.US);
        assertEquals("  not trimmed  ", preservePointer.getValue());

        // Processing Instruction trimming
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "  piData  ");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.US);
        assertEquals("piData", piPointer.getValue());
    }

    @Test(timeout = 4000)
    public void testAsPathVariations() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element e1 = doc.createElement("item");
        Element e2 = doc.createElement("item");
        root.appendChild(e1);
        root.appendChild(e2);

        Text t1 = doc.createTextNode("first");
        Text t2 = doc.createTextNode("second");
        root.appendChild(t1);
        root.appendChild(t2);

        ProcessingInstruction pi1 = doc.createProcessingInstruction("pi", "one");
        ProcessingInstruction pi2 = doc.createProcessingInstruction("pi", "two");
        root.appendChild(pi1);
        root.appendChild(pi2);

        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        assertEquals("", docPointer.asPath());

        DOMNodePointer rootPointer = new DOMNodePointer(docPointer, root);
        DOMNodePointer e2Pointer = new DOMNodePointer(rootPointer, e2);
        assertEquals("/item[2]", e2Pointer.asPath());

        DOMNodePointer t2Pointer = new DOMNodePointer(rootPointer, t2);
        assertEquals("/text()[2]", t2Pointer.asPath());

        DOMNodePointer pi2Pointer = new DOMNodePointer(rootPointer, pi2);
        assertEquals("/processing-instruction('pi')[2]", pi2Pointer.asPath());

        // ID path with escaping
        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.US, "a'b\"c");
        assertEquals("id('a&apos;b&quot;c')", idPointer.asPath());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Node Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testTestNodeEvaluations() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://example.com", "ns:elem");
        Text text = doc.createTextNode("text");
        Comment comment = doc.createComment("comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");

        // Null test
        assertTrue(DOMNodePointer.testNode(elem, null));

        // NodeNameTest on non-element
        assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("elem"))));

        // NodeNameTest wildcards
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName(null, "*"))));
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "*"), "http://example.com")));
        assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("other", "*"), "http://other.com")));

        // NodeNameTest exact matches
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "elem"), "http://example.com")));
        assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "wrong"), "http://example.com")));

        // NodeTypeTest
        assertTrue(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(doc, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        CDATASection cdata = doc.createCDATASection("cdata");
        assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(9999)));

        // ProcessingInstructionTest
        assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("target")));
        assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("other")));
        assertFalse(DOMNodePointer.testNode(elem, new ProcessingInstructionTest("target")));

        // Unknown NodeTest subclass
        NodeTest unknownTest = new NodeTest() {};
        assertFalse(DOMNodePointer.testNode(elem, unknownTest));
    }

    @Test(timeout = 4000)
    public void testPrefixAndLocalNameNonNamespaceAware() throws Exception {
        Document doc = createNonNamespaceDocument();
        Element elem = doc.createElement("foo:bar");
        assertEquals("foo", DOMNodePointer.getPrefix(elem));
        assertEquals("bar", DOMNodePointer.getLocalName(elem));

        Element simpleElem = doc.createElement("simple");
        assertNull(DOMNodePointer.getPrefix(simpleElem));
        assertEquals("simple", DOMNodePointer.getLocalName(simpleElem));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointersOrder() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("attr1", "v1");
        root.setAttribute("attr2", "v2");

        Element child1 = doc.createElement("child1");
        Element child2 = doc.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer pChild1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer pChild2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(0, rootPointer.compareChildNodePointers(pChild1, pChild1));
        assertEquals(-1, rootPointer.compareChildNodePointers(pChild1, pChild2));
        assertEquals(1, rootPointer.compareChildNodePointers(pChild2, pChild1));

        Attr attr1 = root.getAttributeNode("attr1");
        Attr attr2 = root.getAttributeNode("attr2");
        DOMNodePointer pAttr1 = new DOMNodePointer(rootPointer, attr1);
        DOMNodePointer pAttr2 = new DOMNodePointer(rootPointer, attr2);

        // Attribute vs Child
        assertEquals(-1, rootPointer.compareChildNodePointers(pAttr1, pChild1));
        assertEquals(1, rootPointer.compareChildNodePointers(pChild1, pAttr1));

        // Attribute vs Attribute
        int attrComp = rootPointer.compareChildNodePointers(pAttr1, pAttr2);
        assertTrue(attrComp == -1 || attrComp == 1);
        assertEquals(-attrComp, rootPointer.compareChildNodePointers(pAttr2, pAttr1));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets following:: / preceding:: document order navigation across hierarchical DOM nodes
     * corresponding to DOMModelTest::testAxisFollowing and DOMModelTest::testAxisPreceding defects.
     */
    @Test(timeout = 4000)
    public void testDefectAxisFollowingAndPrecedingDocumentOrder() throws Exception {
        Document doc = createDocument();
        // Construct hierarchy equivalent to vendor/location and product
        // <vendor>
        //   <location id="loc1"><address>123 St</address><employeeCount>10</employeeCount></location>
        //   <location id="loc2"><address>456 Ave</address></location>
        //   <product><name>Gadget</name></product>
        // </vendor>
        Element vendor = doc.createElement("vendor");
        doc.appendChild(vendor);

        Element loc1 = doc.createElement("location");
        Element addr1 = doc.createElement("address");
        addr1.appendChild(doc.createTextNode("123 St"));
        Element empCount = doc.createElement("employeeCount");
        empCount.appendChild(doc.createTextNode("10"));
        loc1.appendChild(addr1);
        loc1.appendChild(empCount);
        vendor.appendChild(loc1);

        Element loc2 = doc.createElement("location");
        Element addr2 = doc.createElement("address");
        addr2.appendChild(doc.createTextNode("456 Ave"));
        loc2.appendChild(addr2);
        vendor.appendChild(loc2);

        Element product = doc.createElement("product");
        Element prodName = doc.createElement("name");
        prodName.appendChild(doc.createTextNode("Gadget"));
        product.appendChild(prodName);
        vendor.appendChild(product);

        JXPathContext context = JXPathContext.newContext(doc);

        // Verify following::node()[2] from //location[2] resolves to product[1], not product/name
        Pointer followingPtr = context.getPointer("//location[2]/following::node()[2]");
        assertNotNull(followingPtr);
        assertTrue("Expected following node to point to product element",
                followingPtr.asPath().contains("product[1]"));

        // Verify preceding::node()[3] from //location[2] resolves to loc1/employeeCount text node
        Pointer precedingPtr = context.getPointer("//location[2]/preceding::node()[3]");
        assertNotNull(precedingPtr);
        assertTrue("Expected preceding node to point to employeeCount or its text()",
                precedingPtr.asPath().contains("location[1]") &&
                (precedingPtr.asPath().contains("employeeCount") || precedingPtr.asPath().contains("text()")));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootThrowsException() throws Exception {
        Document doc = createDocument();
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        docPointer.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveNonRootNodeSuccess() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);
        doc.appendChild(root);

        DOMNodePointer childPointer = new DOMNodePointer(new DOMNodePointer(root, Locale.US), child);
        childPointer.remove();
        assertNull(child.getParentNode());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateChildWithoutFactoryThrowsException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);
        pointer.createChild(context, new QName("newChild"), 0);
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildWithFailingFactoryThrowsAbstractFactoryException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                return false;
            }
        });
        pointer.createChild(context, new QName("newChild"), 0);
    }

    @Test(timeout = 4000)
    public void testCreateChildWithSuccessfulFactory() throws Exception {
        Document doc = createDocument();
        final Element root = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                Element newElem = ((Element) node).getOwnerDocument().createElement(name);
                ((Element) node).appendChild(newElem);
                return true;
            }
        });
        NodePointer created = pointer.createChild(context, new QName("newChild"), 0, "childValue");
        assertNotNull(created);
        assertEquals("childValue", created.getValue());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeWithUnknownPrefixThrowsException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);
        pointer.createAttribute(context, new QName("unknown", "attr"));
    }

    @Test(timeout = 4000)
    public void testCreateAttributeSuccess() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);

        NodePointer attrPtr = pointer.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attrPtr);
        assertTrue(root.hasAttribute("simpleAttr"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetPointerById() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        Element elemWithId = doc.createElement("elem");
        elemWithId.setAttribute("id", "targetId");
        elemWithId.setIdAttribute("id", true);
        root.appendChild(elemWithId);
        doc.appendChild(root);

        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        JXPathContext context = JXPathContext.newContext(doc);

        Pointer found = docPointer.getPointerByID(context, "targetId");
        assertTrue(found instanceof DOMNodePointer);
        assertSame(elemWithId, found.getBaseValue());

        Pointer notFound = docPointer.getPointerByID(context, "nonExistent");
        assertTrue(notFound instanceof NullPointer);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        Pointer foundFromRoot = rootPointer.getPointerByID(context, "targetId");
        assertTrue(foundFromRoot instanceof DOMNodePointer);
    }

    @Test(timeout = 4000)
    public void testNamespacePrefixFallbackInAsPath() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://custom.uri", "root");
        doc.appendChild(root);

        Element child = doc.createElementNS("http://unmapped.uri", "unmapped:item");
        root.appendChild(child);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        // When namespace resolver cannot find prefix for unmapped URI, falls back to node()[pos]
        String path = childPointer.asPath();
        assertTrue(path.endsWith("/node()[1]"));
    }
}