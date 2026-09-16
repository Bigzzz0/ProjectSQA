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

/* [Branch & Defect Analysis Matrix]
 * ================================================================================================
 * Targeted Class: org.apache.commons.jxpath.ri.model.dom.DOMNodePointer
 *
 * 1. DEFECT-TARGETED ZONE (Defects4J: ExternalXMLNamespaceTest::testCreateAndSetAttributeDOM):
 *    - Method: createAttribute(JXPathContext, QName)
 *    - Fault: When creating an attribute with a prefix registered on JXPathContext/NamespaceResolver,
 *             createAttribute only delegates to getNamespaceURI(prefix), which queries DOM xmlns
 *             declarations. If the namespace is only bound to the context/resolver, getNamespaceURI()
 *             returns null and throws JXPathException: "Unknown namespace prefix: <prefix>".
 *    - Target Test: testCreateAttributeWithContextRegisteredNamespacePrefix()
 *
 * 2. BRANCH & DECISION COVERAGE:
 *    - testNode(Node, NodeTest):
 *      * test == null -> true
 *      * NodeNameTest on ELEMENT_NODE vs non-ELEMENT_NODE (returns false)
 *      * NodeNameTest wildcard with prefix == null -> true
 *      * NodeNameTest wildcard with prefix != null
 *      * NodeNameTest matching localName and matching namespace URI / null nodeNS with prefix match
 *      * NodeTypeTest: NODE_TYPE_NODE (Element, Document, vs Text)
 *      * NodeTypeTest: NODE_TYPE_TEXT (Text, CDATA, vs Comment)
 *      * NodeTypeTest: NODE_TYPE_COMMENT
 *      * NodeTypeTest: NODE_TYPE_PI
 *      * NodeTypeTest: Unknown node type
 *      * ProcessingInstructionTest: PI node with matching target vs mismatch target vs non-PI node
 *      * Unknown NodeTest implementation -> false
 *    - equalStrings(s1, s2):
 *      * s1 == s2 (same instance / null)
 *      * s1 != s2 (trimmed comparisons, null vs non-empty)
 *    - getName():
 *      * Element node with/without prefix
 *      * ProcessingInstruction node -> target
 *      * Other node types -> null local name & prefix
 *    - getNamespaceURI(prefix):
 *      * null / empty prefix -> getDefaultNamespaceURI()
 *      * "xml" -> XML_NAMESPACE_URI
 *      * "xmlns" -> XMLNS_NAMESPACE_URI
 *      * cached lookup in namespaces map
 *      * ancestor DOM traversal for xmlns:prefix
 *      * unresolvable prefix -> returns null (cached UNKNOWN_NAMESPACE)
 *    - getDefaultNamespaceURI():
 *      * cached defaultNamespace
 *      * searches ancestors for xmlns
 *      * null/empty result -> returns null
 *    - isLeaf():
 *      * hasChildNodes vs empty
 *    - isLanguage(lang):
 *      * xml:lang defined on current node or ancestor
 *      * xml:lang not defined -> delegates to super.isLanguage(lang)
 *    - setValue(value):
 *      * TEXT / CDATA node: non-empty string sets node value; empty string removes node from parent
 *      * Element node: removes all existing children; appends cloned Element/Document children or
 *        cloned non-element node or creates text node from String
 *    - createChild(context, name, index, [value]):
 *      * index == WHOLE_COLLECTION (-1) converts to 0
 *      * factory success vs factory failure (JXPathAbstractFactoryException)
 *      * null factory on context (JXPathException)
 *    - createAttribute(context, name):
 *      * non-Element node -> delegates to super.createAttribute
 *      * Element node with prefix vs without prefix
 *      * prefix not found -> JXPathException
 *    - remove():
 *      * with parent -> removed
 *      * parent == null -> JXPathException ("Cannot remove root DOM node")
 *    - asPath():
 *      * with ID -> id('...') escaping ' and "
 *      * parent is DOMNodePointer: Element with namespace prefix, with null namespace,
 *        with node() fallback when prefix cannot be resolved
 *      * Text / CDATA node -> /text()[pos]
 *      * ProcessingInstruction -> /processing-instruction('target')[pos]
 *      * Document node -> empty
 *    - hashCode() & equals():
 *      * identity, DOMNodePointer equality with same node, inequality with different node or non-DOMNodePointer
 *    - getPrefix(node), getLocalName(node), getNamespaceURI(node):
 *      * Document vs Element
 *      * localName != null vs colon split fallback
 *      * prefix != null vs colon split fallback
 *    - getValue() & stringValue(node):
 *      * Comment node -> comment text trimmed
 *      * xml:space="preserve" preserves whitespace in Text/CDATA/PI
 *      * default xml:space trims whitespace
 *    - getPointerByID(context, id):
 *      * found Element -> DOMNodePointer with id
 *      * not found -> NullPointer
 *    - compareChildNodePointers(p1, p2):
 *      * p1 == p2
 *      * attribute vs non-attribute, non-attribute vs attribute
 *      * attribute vs attribute (NamedNodeMap sequence)
 *      * child node sequence
 * ================================================================================================
 */
public class DOMNodePointerGeminiTest {

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where createAttribute fails when a prefix is registered
     * on the JXPathContext / NamespaceResolver rather than explicitly declared
     * via xmlns:prefix in the underlying DOM element hierarchy.
     */
    @Test(timeout = 4000)
    public void testCreateAttributeWithContextRegisteredNamespacePrefix() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        context.registerNamespace("A", "http://commons.apache.org/jxpath/testA");

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);

        // When the defect is present, rootPointer.createAttribute queries getNamespaceURI("A")
        // which only checks the DOM element tree (no xmlns:A exists yet), returns null,
        // and throws JXPathException: "Unknown namespace prefix: A".
        // The expected behavior is that it creates the attribute with the namespace registered on the context.
        NodePointer attrPointer = rootPointer.createAttribute(context, new QName("A", "myAttr"));
        assertNotNull("Created attribute pointer should not be null", attrPointer);
        assertTrue("Created attribute should exist on the element", root.hasAttributeNS("http://commons.apache.org/jxpath/testA", "myAttr"));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndState() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://example.com/ns", "ns:root");
        doc.appendChild(root);

        DOMNodePointer pointer = new DOMNodePointer(root, Locale.GERMANY, "elemId");
        assertEquals(Locale.GERMANY, pointer.getLocale());
        assertSame(root, pointer.getBaseValue());
        assertSame(root, pointer.getImmediateNode());
        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        Text text = doc.createTextNode("child");
        root.appendChild(text);
        assertFalse(pointer.isLeaf());

        QName name = pointer.getName();
        assertEquals("ns", name.getPrefix());
        assertEquals("root", name.getName());
    }

    @Test(timeout = 4000)
    public void testTestNodeEvaluations() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://example.com/ns", "pfx:element");
        Text text = doc.createTextNode("sample text");
        CDATASection cdata = doc.createCDATASection("sample cdata");
        Comment comment = doc.createComment("sample comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("targetPI", "dataPI");

        doc.appendChild(elem);
        elem.appendChild(text);
        elem.appendChild(cdata);
        elem.appendChild(comment);
        elem.appendChild(pi);

        // test == null -> true
        assertTrue(DOMNodePointer.testNode(elem, null));

        // NodeNameTest on Element
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("pfx", "element"), "http://example.com/ns")));
        assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("other"), "http://example.com/ns")));
        assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("pfx", "element"), "http://wrong.org")));
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName(null, "*"))));

        // NodeNameTest on non-Element
        assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("element"))));

        // NodeTypeTest
        assertTrue(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(doc, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(9999))); // Unknown node type

        // ProcessingInstructionTest
        assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("targetPI")));
        assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("otherPI")));
        assertFalse(DOMNodePointer.testNode(elem, new ProcessingInstructionTest("targetPI")));
    }

    @Test(timeout = 4000)
    public void testGetNameVariations() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("myTarget", "myData");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.US);
        assertEquals("myTarget", piPointer.getName().getName());
        assertNull(piPointer.getName().getPrefix());

        Text text = doc.createTextNode("content");
        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.US);
        assertNull(textPointer.getName().getName());
        assertNull(textPointer.getName().getPrefix());

        Element elemWithoutPrefix = doc.createElement("simple");
        DOMNodePointer elemPointer = new DOMNodePointer(elemWithoutPrefix, Locale.US);
        assertEquals("simple", elemPointer.getName().getName());
        assertNull(elemPointer.getName().getPrefix());
    }

    @Test(timeout = 4000)
    public void testNamespaceURILookup() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://example.com/root", "root");
        root.setAttribute("xmlns", "http://example.com/default");
        root.setAttribute("xmlns:custom", "http://example.com/custom");
        doc.appendChild(root);

        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);

        assertEquals("http://www.w3.org/XML/1998/namespace", childPointer.getNamespaceURI("xml"));
        assertEquals("http://www.w3.org/2000/xmlns/", childPointer.getNamespaceURI("xmlns"));
        assertEquals("http://example.com/default", childPointer.getNamespaceURI(""));
        assertEquals("http://example.com/default", childPointer.getNamespaceURI((String) null));
        assertEquals("http://example.com/custom", childPointer.getNamespaceURI("custom"));
        assertNull(childPointer.getNamespaceURI("nonexistent"));

        // Document level pointer namespace lookup
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        assertEquals("http://example.com/default", docPointer.getDefaultNamespaceURI());
        assertEquals("http://example.com/custom", docPointer.getNamespaceURI("custom"));
    }

    @Test(timeout = 4000)
    public void testLanguageHandling() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        doc.appendChild(parent);

        Element child = doc.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);
        assertTrue(childPointer.isLanguage("en"));
        assertTrue(childPointer.isLanguage("EN-US"));
        assertFalse(childPointer.isLanguage("fr"));

        Element noLang = doc.createElement("noLang");
        DOMNodePointer noLangPointer = new DOMNodePointer(noLang, Locale.US);
        // Falls back to super.isLanguage(lang) which matches locale
        assertTrue(noLangPointer.isLanguage("en"));
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextAndCData() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Text text = doc.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer textPointer = new DOMNodePointer(text, Locale.US);
        textPointer.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        // Empty value causes text node to be removed
        textPointer.setValue("");
        assertNull(text.getParentNode());
        assertEquals(0, root.getChildNodes().getLength());

        CDATASection cdata = doc.createCDATASection("initial CDATA");
        root.appendChild(cdata);
        DOMNodePointer cdataPointer = new DOMNodePointer(cdata, Locale.US);
        cdataPointer.setValue(null);
        assertNull(cdata.getParentNode());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElement() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        root.appendChild(doc.createTextNode("old text"));

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        // Set string value
        rootPointer.setValue("new text");
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("new text", root.getFirstChild().getNodeValue());

        // Set Element value: should append cloned children
        Element donor = doc.createElement("donor");
        donor.appendChild(doc.createElement("donorChild1"));
        donor.appendChild(doc.createElement("donorChild2"));
        rootPointer.setValue(donor);
        assertEquals(2, root.getChildNodes().getLength());
        assertEquals("donorChild1", root.getFirstChild().getNodeName());

        // Set Document value
        Document donorDoc = createDocument();
        donorDoc.appendChild(donorDoc.createElement("docChild"));
        rootPointer.setValue(donorDoc);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("docChild", root.getFirstChild().getNodeName());

        // Set other node type (e.g. Comment)
        Comment comment = doc.createComment("a comment");
        rootPointer.setValue(comment);
        assertEquals(1, root.getChildNodes().getLength());
        assertTrue(root.getFirstChild() instanceof Comment);
    }

    @Test(timeout = 4000)
    public void testGetValueAndStringValueFormatting() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Comment comment = doc.createComment("  a comment with spaces  ");
        root.appendChild(comment);
        DOMNodePointer commentPointer = new DOMNodePointer(comment, Locale.US);
        assertEquals("a comment with spaces", commentPointer.getValue());

        Element elem = doc.createElement("elem");
        elem.appendChild(doc.createTextNode("   trimmed text   "));
        DOMNodePointer elemPointer = new DOMNodePointer(elem, Locale.US);
        assertEquals("trimmed text", elemPointer.getValue());

        Element preserveElem = doc.createElement("preserveElem");
        preserveElem.setAttribute("xml:space", "preserve");
        preserveElem.appendChild(doc.createTextNode("   preserve text   "));
        DOMNodePointer preservePointer = new DOMNodePointer(preserveElem, Locale.US);
        assertEquals("   preserve text   ", preservePointer.getValue());

        ProcessingInstruction pi = doc.createProcessingInstruction("pi", "  data  ");
        DOMNodePointer piPointer = new DOMNodePointer(pi, Locale.US);
        assertEquals("data", piPointer.getValue());
    }

    @Test(timeout = 4000)
    public void testAsPathVariations() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element childA1 = doc.createElement("childA");
        Element childA2 = doc.createElement("childA");
        Element childB = doc.createElement("childB");
        root.appendChild(childA1);
        root.appendChild(childA2);
        root.appendChild(childB);

        Text text1 = doc.createTextNode("t1");
        Text text2 = doc.createTextNode("t2");
        childB.appendChild(text1);
        childB.appendChild(text2);

        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        childB.appendChild(pi);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer a2Pointer = new DOMNodePointer(rootPointer, childA2);
        assertEquals("/childA[2]", a2Pointer.asPath());

        DOMNodePointer bPointer = new DOMNodePointer(rootPointer, childB);
        DOMNodePointer t2Pointer = new DOMNodePointer(bPointer, text2);
        assertEquals("/childB[1]/text()[2]", t2Pointer.asPath());

        DOMNodePointer piPointer = new DOMNodePointer(bPointer, pi);
        assertEquals("/childB[1]/processing-instruction('target')[1]", piPointer.asPath());

        // ID based path
        DOMNodePointer idPointer = new DOMNodePointer(root, Locale.US, "my'\"id");
        assertEquals("id('my&apos;&quot;id')", idPointer.asPath());
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("id", "root1");
        root.setIdAttribute("id", true);
        doc.appendChild(root);

        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        Pointer found = docPointer.getPointerByID(JXPathContext.newContext(doc), "root1");
        assertTrue(found instanceof DOMNodePointer);
        assertSame(root, found.getNode());

        Pointer notFound = docPointer.getPointerByID(JXPathContext.newContext(doc), "nonexistent");
        assertTrue(notFound instanceof NullPointer);
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("attr1", "val1");
        root.setAttribute("attr2", "val2");
        doc.appendChild(root);

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

        // Attribute always precedes child element
        assertEquals(-1, rootPointer.compareChildNodePointers(pAttr1, pChild1));
        assertEquals(1, rootPointer.compareChildNodePointers(pChild1, pAttr1));
        // Attribute ordering according to attributes NamedNodeMap
        int cmpAttrs = rootPointer.compareChildNodePointers(pAttr1, pAttr2);
        assertTrue(cmpAttrs == -1 || cmpAttrs == 1);
    }

    @Test(timeout = 4000)
    public void testCreateAttributeWithoutPrefix() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);

        NodePointer attrPtr = rootPointer.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attrPtr);
        assertTrue(root.hasAttribute("simpleAttr"));

        // Creating it a second time should return existing
        NodePointer attrPtr2 = rootPointer.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attrPtr2);
    }

    @Test(timeout = 4000)
    public void testIterators() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("a", "1");
        doc.appendChild(root);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        NodeIterator childIt = rootPointer.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = rootPointer.attributeIterator(new QName("a"));
        assertNotNull(attrIt);

        NodeIterator nsIt = rootPointer.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = rootPointer.namespacePointer("xml");
        assertNotNull(nsPtr);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetPrefixAndLocalNameFallbackLogic() throws Exception {
        Document doc = createDocument();
        // Element created with non-namespace aware method or colon in name
        Element elementWithColon = doc.createElement("prefix:tag");
        assertEquals("prefix", DOMNodePointer.getPrefix(elementWithColon));
        assertEquals("tag", DOMNodePointer.getLocalName(elementWithColon));

        Element elementWithoutColon = doc.createElement("simpletag");
        assertNull(DOMNodePointer.getPrefix(elementWithoutColon));
        assertEquals("simpletag", DOMNodePointer.getLocalName(elementWithoutColon));
    }

    @Test(timeout = 4000)
    public void testEqualStringsBoundaryConditions() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("test");
        doc.appendChild(elem);

        // Wildcard test matching
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(DOMNodePointer.testNode(elem, wildcardTest));

        // Matching name with matching null namespaces
        NodeNameTest testWithNullNS = new NodeNameTest(new QName("test"), null);
        assertTrue(DOMNodePointer.testNode(elem, testWithNullNS));

        // Matching name with empty namespace matching null node namespace
        NodeNameTest testWithEmptyNS = new NodeNameTest(new QName("test"), "");
        assertTrue(DOMNodePointer.testNode(elem, testWithEmptyNS));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootThrowsException() throws Exception {
        Document doc = createDocument();
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.US);
        docPointer.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveChildNodeSuccess() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPointer = new DOMNodePointer(child, Locale.US);
        childPointer.remove();
        assertEquals(0, root.getChildNodes().getLength());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateChildWithoutFactoryThrowsException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(null);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        rootPointer.createChild(context, new QName("newChild"), 0);
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildWithFailingFactoryThrowsException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                return false;
            }
        });

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        rootPointer.createChild(context, new QName("uncreatable"), 0);
    }

    @Test(timeout = 4000)
    public void testCreateChildWithSuccessfulFactory() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                if ("child".equals(name) && node instanceof Element) {
                    Element newChild = ((Element) node).getOwnerDocument().createElement(name);
                    ((Element) node).appendChild(newChild);
                    return true;
                }
                return false;
            }
        });

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        NodePointer created = rootPointer.createChild(context, new QName("child"), NodePointer.WHOLE_COLLECTION, "initialValue");
        assertNotNull(created);
        assertEquals(1, root.getElementsByTagName("child").getLength());
        assertEquals("initialValue", created.getValue());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeWithUnknownPrefixThrowsException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        // Prefix "unknownPrefix" is neither in DOM nor in context
        rootPointer.createAttribute(context, new QName("unknownPrefix", "attr"));
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() throws Exception {
        Document doc = createDocument();
        Element elem1 = doc.createElement("elem1");
        Element elem2 = doc.createElement("elem2");

        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.US);
        DOMNodePointer ptr1SameNode = new DOMNodePointer(elem1, Locale.UK);
        DOMNodePointer ptr2 = new DOMNodePointer(elem2, Locale.US);

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr1SameNode));
        assertEquals(ptr1.hashCode(), ptr1SameNode.hashCode());

        assertFalse(ptr1.equals(ptr2));
        assertFalse(ptr1.equals(null));
        assertFalse(ptr1.equals("a string"));
    }
}