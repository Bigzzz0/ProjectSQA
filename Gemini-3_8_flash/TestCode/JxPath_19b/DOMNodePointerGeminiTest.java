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

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;

import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;
import java.util.Locale;

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
 * 1. DEFECT-TARGETED ZONE (AliasedNamespaceIterationTest regression):
 *    - Branch: asPath() -> getRelativePositionByQName()
 *    - Defect: When elements belong to the same namespace URI and local name, but have differing prefixes
 *      (e.g., default namespace "" vs aliased prefix "a:"), getRelativePositionByQName() compares raw
 *      getNodeName() strings ("elem" vs "a:elem"), failing to increment position count and generating duplicate
 *      indexed paths (/a:doc[1]/a:elem[1] instead of [1] and [2]).
 *
 * 2. TESTNODE & NODE FILTERING BRANCHES:
 *    - NodeNameTest: wildcard with null prefix; wildcard with prefix; matching localName + matching/mismatched NS;
 *      non-element nodes (must return false).
 *    - NodeTypeTest: NODE_TYPE_NODE, NODE_TYPE_TEXT (TEXT & CDATA), NODE_TYPE_COMMENT, NODE_TYPE_PI, unknown type.
 *    - ProcessingInstructionTest: matching target vs mismatched target; non-PI node.
 *    - null test handling (returns true).
 *
 * 3. NAMESPACE & URI RESOLUTION:
 *    - XML / XMLNS reserved prefixes.
 *    - Default namespace resolution from xmlns attributes up the DOM tree and Document wrapper.
 *    - Prefix caching and UNKNOWN_NAMESPACE mapping.
 *
 * 4. MUTATION, FACTORY & LIFECYCLE:
 *    - setValue: Text/CDATA replacement & removal on empty string; Node replacement/appending; non-node string conversion.
 *    - createAttribute: Element vs non-element fallback; prefix resolution & unknown namespace failure; duplicate attribute.
 *    - createChild: AbstractFactory invocation, index traversal, failure propagation.
 *    - remove: Root removal exception vs parent-child detachment.
 *
 * 5. PATHS, STRINGS & COMPARISONS:
 *    - asPath: Element with prefix/no prefix, Text, CDATA, PI, Document, id('...') locator.
 *    - compareChildNodePointers: Attribute vs Element, Attribute vs Attribute, Element order in sibling chain.
 *    - equals & hashCode contract.
 */
public class DOMNodePointerGeminiTest {

    private Document document;

    @Before
    public void setUp() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        document = factory.newDocumentBuilder().newDocument();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Aliased Namespaces & asPath())
    // =========================================================================

    /**
     * Targets AliasedNamespaceIterationTest defect:
     * When sibling elements have identical local names and namespace URIs, but differing prefixes
     * (e.g. one without prefix using default xmlns, and the other with prefix "a:"),
     * DOMNodePointer's asPath() or relative position calculation must distinguish them and yield
     * unique positional indexes (/a:doc[1]/a:elem[1] and /a:doc[1]/a:elem[2]).
     */
    @Test(timeout = 4000)
    public void testAliasedNamespaceSiblingPathsInAsPath() {
        String nsUri = "http://commons.apache.org/test";
        Element root = document.createElementNS(nsUri, "a:doc");
        document.appendChild(root);

        // Child 1: created without prefix under the same namespace
        Element child1 = document.createElementNS(nsUri, "elem");
        root.appendChild(child1);

        // Child 2: created with prefix 'a:' under the same namespace
        Element child2 = document.createElementNS(nsUri, "a:elem");
        root.appendChild(child2);

        JXPathContext context = JXPathContext.newContext(document);
        context.registerNamespace("a", nsUri);

        Iterator iterator = context.iteratePointers("/a:doc/a:elem");
        assertTrue("Iterator must have at least one pointer", iterator.hasNext());
        Pointer p1 = (Pointer) iterator.next();
        assertTrue("Iterator must have a second pointer", iterator.hasNext());
        Pointer p2 = (Pointer) iterator.next();
        assertFalse("Iterator must only contain two elements", iterator.hasNext());

        assertEquals("First element path mismatch", "/a:doc[1]/a:elem[1]", p1.asPath());
        assertEquals("Second element path mismatch (defect in getRelativePositionByQName)",
                "/a:doc[1]/a:elem[2]", p2.asPath());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTestNodeCombinations() {
        Element elem = document.createElementNS("http://test.com", "ns:item");
        Text text = document.createTextNode("sample");
        CDATASection cdata = document.createCDATASection("data");
        Comment comment = document.createComment("a comment");
        ProcessingInstruction pi = document.createProcessingInstruction("targetPI", "dataPI");

        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.US);
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.US);
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);

        // Null test
        assertTrue(elemPtr.testNode(null));

        // NodeNameTest matching
        assertTrue(elemPtr.testNode(new NodeNameTest(new QName("ns", "item"), "http://test.com")));
        assertFalse(elemPtr.testNode(new NodeNameTest(new QName("ns", "other"), "http://test.com")));
        assertFalse(elemPtr.testNode(new NodeNameTest(new QName("ns", "item"), "http://different.com")));

        // NodeNameTest on non-element
        assertFalse(textPtr.testNode(new NodeNameTest(new QName(null, "item"))));

        // Wildcard tests
        assertTrue(elemPtr.testNode(new NodeNameTest(new QName(null, "*"))));
        assertTrue(elemPtr.testNode(new NodeNameTest(new QName("ns", "*"), "http://test.com")));

        // NodeTypeTest evaluations
        assertTrue(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(new DOMNodePointer(cdata, Locale.US).testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        assertTrue(commentPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(piPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(elemPtr.testNode(new NodeTypeTest(999))); // default branch

        // ProcessingInstructionTest
        assertTrue(piPtr.testNode(new ProcessingInstructionTest("targetPI")));
        assertFalse(piPtr.testNode(new ProcessingInstructionTest("otherPI")));
        assertFalse(elemPtr.testNode(new ProcessingInstructionTest("targetPI")));
    }

    @Test(timeout = 4000)
    public void testGetNameAndNamespaceURIs() {
        Element elem = document.createElementNS("http://ns1.com", "pfx:root");
        elem.setAttribute("xmlns:pfx", "http://ns1.com");
        elem.setAttribute("xmlns", "http://default.com");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH);

        QName qName = ptr.getName();
        assertEquals("pfx", qName.getPrefix());
        assertEquals("root", qName.getName());

        assertEquals("http://default.com", ptr.getNamespaceURI(null));
        assertEquals("http://default.com", ptr.getNamespaceURI(""));
        assertEquals("http://ns1.com", ptr.getNamespaceURI("pfx"));
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, ptr.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, ptr.getNamespaceURI("xmlns"));
        assertNull(ptr.getNamespaceURI("unknownPrefix"));

        // Processing Instruction QName
        ProcessingInstruction pi = document.createProcessingInstruction("myTarget", "myData");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals(new QName(null, "myTarget"), piPtr.getName());
    }

    @Test(timeout = 4000)
    public void testLanguageHandling() {
        Element parent = document.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = document.createElement("child");
        parent.appendChild(child);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.US);
        DOMNodePointer childPtr = new DOMNodePointer(parentPtr, child);

        assertTrue(childPtr.isLanguage("en"));
        assertTrue(childPtr.isLanguage("en-US"));
        assertFalse(childPtr.isLanguage("fr"));

        // When xml:lang is absent, super.isLanguage checks Locale
        Element noLang = document.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLang, Locale.GERMAN);
        assertTrue(noLangPtr.isLanguage("de"));
        assertFalse(noLangPtr.isLanguage("fr"));
    }

    @Test(timeout = 4000)
    public void testGetValueAndStringValue() {
        Element elem = document.createElement("container");
        elem.setAttribute("xml:space", "preserve");
        Text text1 = document.createTextNode("  Hello  ");
        elem.appendChild(text1);

        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.US);
        assertEquals("  Hello  ", ptr.getValue());

        Element defaultSpace = document.createElement("spaceTrim");
        Text text2 = document.createTextNode("  Hello Trim  ");
        defaultSpace.appendChild(text2);
        DOMNodePointer trimPtr = new DOMNodePointer(defaultSpace, Locale.US);
        assertEquals("Hello Trim", trimPtr.getValue());

        Comment comment = document.createComment(" my comment ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.US);
        assertEquals("my comment", commentPtr.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", " pi text ");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.US);
        assertEquals("pi text", piPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueScenarios() {
        Element elem = document.createElement("target");
        document.appendChild(elem);
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.US);

        // Set string value on Element
        elemPtr.setValue("New Text Content");
        assertEquals(1, elem.getChildNodes().getLength());
        assertEquals("New Text Content", elem.getFirstChild().getNodeValue());

        // Set Element value on Element (cloning children)
        Element donor = document.createElement("donor");
        donor.appendChild(document.createElement("sub1"));
        donor.appendChild(document.createElement("sub2"));
        elemPtr.setValue(donor);
        assertEquals(2, elem.getChildNodes().getLength());
        assertEquals("sub1", elem.getFirstChild().getNodeName());

        // Set single Node (e.g. comment) on Element
        Comment singleChild = document.createComment("inline");
        elemPtr.setValue(singleChild);
        assertEquals(1, elem.getChildNodes().getLength());
        assertTrue(elem.getFirstChild() instanceof Comment);

        // Set value on Text node
        Text textNode = document.createTextNode("Original");
        elem.appendChild(textNode);
        DOMNodePointer textPtr = new DOMNodePointer(elemPtr, textNode);
        textPtr.setValue("Replaced");
        assertEquals("Replaced", textNode.getNodeValue());

        // Setting empty string on Text node removes it
        textPtr.setValue("");
        assertNull(textNode.getParentNode());
    }

    @Test(timeout = 4000)
    public void testAsPathVariations() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Element e1 = document.createElement("item");
        Element e2 = document.createElement("item");
        root.appendChild(e1);
        root.appendChild(e2);

        DOMNodePointer e1Ptr = new DOMNodePointer(rootPtr, e1);
        DOMNodePointer e2Ptr = new DOMNodePointer(rootPtr, e2);

        assertEquals("/root[1]/item[1]", e1Ptr.asPath());
        assertEquals("/root[1]/item[2]", e2Ptr.asPath());

        // Sibling Text nodes
        Text t1 = document.createTextNode("one");
        Text t2 = document.createTextNode("two");
        root.appendChild(t1);
        root.appendChild(t2);

        DOMNodePointer t1Ptr = new DOMNodePointer(rootPtr, t1);
        DOMNodePointer t2Ptr = new DOMNodePointer(rootPtr, t2);
        assertEquals("/root[1]/text()[1]", t1Ptr.asPath());
        assertEquals("/root[1]/text()[2]", t2Ptr.asPath());

        // CDATA relative positioning mixed with Text
        CDATASection c1 = document.createCDATASection("three");
        root.appendChild(c1);
        DOMNodePointer c1Ptr = new DOMNodePointer(rootPtr, c1);
        assertEquals("/root[1]/text()[3]", c1Ptr.asPath());

        // ProcessingInstruction path
        ProcessingInstruction pi1 = document.createProcessingInstruction("testPI", "a");
        ProcessingInstruction pi2 = document.createProcessingInstruction("testPI", "b");
        root.appendChild(pi1);
        root.appendChild(pi2);
        DOMNodePointer pi1Ptr = new DOMNodePointer(rootPtr, pi1);
        DOMNodePointer pi2Ptr = new DOMNodePointer(rootPtr, pi2);
        assertEquals("/root[1]/processing-instruction('testPI')[1]", pi1Ptr.asPath());
        assertEquals("/root[1]/processing-instruction('testPI')[2]", pi2Ptr.asPath());

        // Pointer with ID
        DOMNodePointer idPtr = new DOMNodePointer(root, Locale.US, "elementId");
        assertEquals("id('elementId')", idPtr.asPath());

        // Document node path
        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        assertEquals("", docPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() {
        Element root = document.createElement("root");
        root.setAttribute("attr1", "val1");
        root.setAttribute("attr2", "val2");

        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.US);

        Attr attr1 = root.getAttributeNode("attr1");
        Attr attr2 = root.getAttributeNode("attr2");
        DOMNodePointer attr1Ptr = new DOMNodePointer(rootPtr, attr1);
        DOMNodePointer attr2Ptr = new DOMNodePointer(rootPtr, attr2);

        DOMNodePointer child1Ptr = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer child2Ptr = new DOMNodePointer(rootPtr, child2);

        // Same node
        assertEquals(0, rootPtr.compareChildNodePointers(child1Ptr, child1Ptr));

        // Attribute vs Element
        assertEquals(-1, rootPtr.compareChildNodePointers(attr1Ptr, child1Ptr));
        assertEquals(1, rootPtr.compareChildNodePointers(child1Ptr, attr1Ptr));

        // Attribute vs Attribute
        int attrComp = rootPtr.compareChildNodePointers(attr1Ptr, attr2Ptr);
        assertTrue(attrComp == -1 || attrComp == 1);

        // Child elements order
        assertEquals(-1, rootPtr.compareChildNodePointers(child1Ptr, child2Ptr));
        assertEquals(1, rootPtr.compareChildNodePointers(child2Ptr, child1Ptr));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBVALocalNameAndPrefixDerivation() {
        // Unprefixed node
        Element elemNoPrefix = document.createElement("simplename");
        assertEquals("simplename", DOMNodePointer.getLocalName(elemNoPrefix));
        assertNull(DOMNodePointer.getPrefix(elemNoPrefix));

        // Prefixed node
        Element elemWithPrefix = document.createElement("myprefix:complexname");
        assertEquals("complexname", DOMNodePointer.getLocalName(elemWithPrefix));
        assertEquals("myprefix", DOMNodePointer.getPrefix(elemWithPrefix));

        // Node with colon at boundaries
        Element trailingColon = document.createElement("prefixonly:");
        assertEquals("", DOMNodePointer.getLocalName(trailingColon));
        assertEquals("prefixonly", DOMNodePointer.getPrefix(trailingColon));
    }

    @Test(timeout = 4000)
    public void testLeafAndCollectionCharacteristics() {
        Element emptyElem = document.createElement("empty");
        DOMNodePointer ptr = new DOMNodePointer(emptyElem, Locale.US);

        assertTrue(ptr.isLeaf());
        assertFalse(ptr.isCollection());
        assertEquals(1, ptr.getLength());
        assertTrue(ptr.isActual());
        assertSame(emptyElem, ptr.getBaseValue());
        assertSame(emptyElem, ptr.getImmediateNode());

        emptyElem.appendChild(document.createElement("child"));
        assertFalse(ptr.isLeaf());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootNodeThrowsException() {
        DOMNodePointer ptr = new DOMNodePointer(document, Locale.US);
        ptr.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveChildNodeSuccess() {
        Element root = document.createElement("root");
        Element child = document.createElement("child");
        root.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(root, child);

        childPtr.remove();
        assertNull(child.getParentNode());
        assertEquals(0, root.getChildNodes().getLength());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeUnknownNamespaceThrowsException() {
        Element root = document.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);

        ptr.createAttribute(context, new QName("unknownPfx", "attrName"));
    }

    @Test(timeout = 4000)
    public void testCreateAttributeSuccess() {
        Element root = document.createElementNS("http://ns.com", "ns:root");
        root.setAttribute("xmlns:ns", "http://ns.com");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);

        NodePointer attrPtr = ptr.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attrPtr);
        assertTrue(root.hasAttribute("simpleAttr"));

        NodePointer nsAttrPtr = ptr.createAttribute(context, new QName("ns", "qualifiedAttr"));
        assertNotNull(nsAttrPtr);
        assertTrue(root.hasAttributeNS("http://ns.com", "qualifiedAttr"));
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildWithoutFactoryThrowsException() {
        Element root = document.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(root);

        ptr.createChild(context, new QName("child"), 0);
    }

    @Test(timeout = 4000)
    public void testCreateChildWithFactorySuccess() {
        Element root = document.createElement("root");
        document.appendChild(root);
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, Pointer parent, Object parentNode, String name, int index) {
                if ("child".equals(name) && parentNode instanceof Element) {
                    Element child = ((Element) parentNode).getOwnerDocument().createElement("child");
                    ((Element) parentNode).appendChild(child);
                    return true;
                }
                return false;
            }
        });

        NodePointer childPtr = ptr.createChild(context, new QName("child"), 0, "InitialValue");
        assertNotNull(childPtr);
        assertEquals(1, root.getElementsByTagName("child").getLength());
        assertEquals("InitialValue", childPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() {
        Element root = document.createElement("root");
        Element target = document.createElement("target");
        target.setAttribute("id", "targetId");
        target.setIdAttribute("id", true);
        root.appendChild(target);
        document.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.US);
        JXPathContext context = JXPathContext.newContext(document);

        Pointer found = docPtr.getPointerByID(context, "targetId");
        assertTrue(found instanceof DOMNodePointer);
        assertEquals(target, found.getNode());

        Pointer missing = docPtr.getPointerByID(context, "missingId");
        assertTrue(missing instanceof NullPointer);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Element elem1 = document.createElement("node");
        Element elem2 = document.createElement("node");

        DOMNodePointer ptr1A = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer ptr1B = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.US);

        assertEquals(ptr1A, ptr1A);
        assertEquals(ptr1A, ptr1B);
        assertEquals(ptr1B, ptr1A);
        assertEquals(ptr1A.hashCode(), ptr1B.hashCode());

        assertNotEquals(ptr1A, ptr2);
        assertNotEquals(ptr1A, null);
        assertNotEquals(ptr1A, "StringObject");
    }

    @Test(timeout = 4000)
    public void testIteratorsAndResolvers() {
        Element root = document.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.US);

        NodeIterator childIter = ptr.childIterator(new NodeNameTest(new QName("item")), false, null);
        assertNotNull(childIter);

        NodeIterator attrIter = ptr.attributeIterator(new QName("attr"));
        assertNotNull(attrIter);

        NodeIterator nsIter = ptr.namespaceIterator();
        assertNotNull(nsIter);

        NodePointer nsPtr = ptr.namespacePointer("xml");
        assertNotNull(nsPtr);

        NamespaceResolver resolver = ptr.getNamespaceResolver();
        assertNotNull(resolver);
        assertSame(resolver, ptr.getNamespaceResolver());
    }
}