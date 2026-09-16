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
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;
import static org.junit.Assert.*;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.jxpath.ri.model.dom.DOMNodePointer
 * Known Defect (JXPath-154):
 *   - Symptom: JXPath154Test::testInnerEmptyNamespaceDOM -> ComparisonFailure:
 *              expected:</b:foo[1]/[test[1]]> but was:</b:foo[1]/[node()[2]]>
 *   - Cause: When an element defines an empty inner default namespace (e.g. xmlns=""), getNamespaceURI(node)
 *            returns "" instead of null. In asPath(), nsURI is non-null, resolving prefix for "" fails,
 *            causing it to fall through to buffer.append("node()[" + pos + "]") rather than localName.
 * Decision Branches & Partitions Tested:
 *   - Partition A: Core Functional Logic & State Transitions
 *       * getBaseValue, getImmediateNode, isActual, isCollection, getLength, isLeaf
 *       * getName: ELEMENT (prefixed and unprefixed), PROCESSING_INSTRUCTION, others
 *       * getNamespaceURI(prefix): null, empty, "xml", "xmlns", hierarchy traversal, cached UNKNOWN_NAMESPACE
 *       * getDefaultNamespaceURI: direct xmlns, inherited, document element, null fallback
 *       * getValue / stringValue: Comment (trimmed), Text/CDATA with preserve vs default trim, PI, Element tree
 *       * isLanguage: direct xml:lang, enclosing xml:lang, case-insensitivity, locale fallback
 *       * namespace & attribute iterators and pointers
 *   - Partition B: Boundary Value Analysis (BVA) & Extremes
 *       * testNode: null test, NodeNameTest (wildcard without prefix, wildcard with prefix, localName match/mismatch,
 *         non-element rejection, namespaceURI match vs null with prefix match)
 *       * NodeTypeTest: NODE_TYPE_NODE, TEXT, COMMENT, PI, unknown type
 *       * ProcessingInstructionTest: matching target, mismatched target, non-PI node
 *       * getPrefix & getLocalName: DOM Level 1 names ("prefix:local") vs DOM Level 2 (null prefix/localName)
 *   - Partition C: Defect-Targeted Branch Zone (JXPath-154)
 *       * testInnerEmptyNamespaceDOM: Element with xmlns="" nested in prefixed parent element with siblings.
 *   - Partition D: Exception & Defensive Guard Paths
 *       * remove(): root node without parent throws JXPathException; non-root succeeds
 *       * createAttribute(): non-element delegation, unknown namespace prefix throws JXPathException
 *       * createChild(): AbstractFactory failure throws JXPathAbstractFactoryException; success path verified
 *   - Partition E: Object Lifecycle & Contract Integrity
 *       * equals & hashCode: self, equal DOM nodes, different DOM nodes, null, incompatible type
 *       * compareChildNodePointers: same node, attribute vs element, element vs attribute,
 *         attribute vs attribute ordering, sibling child ordering
 *       * getPointerByID: valid id returns DOMNodePointer; unknown id returns NullPointer
 *       * setValue(): Text node (replacement vs removal on empty), Element node (Element, Document, String, Node)
 * ---------------------------------------------------------------------------------------------------------
 */
public class DOMNodePointerGeminiTest {

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicProperties() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        assertSame(root, rootPtr.getBaseValue());
        assertSame(root, rootPtr.getImmediateNode());
        assertTrue(rootPtr.isActual());
        assertFalse(rootPtr.isCollection());
        assertEquals(1, rootPtr.getLength());
        assertTrue(rootPtr.isLeaf());

        root.appendChild(doc.createTextNode("child"));
        assertFalse(rootPtr.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetName() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://example.com/ns", "ex:element");
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.ENGLISH);
        QName elemQName = elemPtr.getName();
        assertEquals("ex", elemQName.getPrefix());
        assertEquals("element", elemQName.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("targetPI", "data");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        QName piQName = piPtr.getName();
        assertNull(piQName.getPrefix());
        assertEquals("targetPI", piQName.getName());

        Text text = doc.createTextNode("some text");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        QName textQName = textPtr.getName();
        assertNull(textQName.getPrefix());
        assertNull(textQName.getName());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIPrefixes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://example.com/root", "root");
        root.setAttribute("xmlns:ns1", "http://example.com/ns1");
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);

        assertEquals("http://www.w3.org/XML/1998/namespace", childPtr.getNamespaceURI("xml"));
        assertEquals("http://www.w3.org/2000/xmlns/", childPtr.getNamespaceURI("xmlns"));
        assertEquals("http://example.com/ns1", childPtr.getNamespaceURI("ns1"));
        // Second lookup retrieves cached entry
        assertEquals("http://example.com/ns1", childPtr.getNamespaceURI("ns1"));

        assertNull(childPtr.getNamespaceURI("unknownPrefix"));
        // Second lookup retrieves cached unknown
        assertNull(childPtr.getNamespaceURI("unknownPrefix"));
    }

    @Test(timeout = 4000)
    public void testDefaultNamespaceURI() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("xmlns", "http://example.com/default");
        doc.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://example.com/default", docPtr.getDefaultNamespaceURI());

        Element child = doc.createElement("child");
        root.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);
        assertEquals("http://example.com/default", childPtr.getDefaultNamespaceURI());
        assertEquals("http://example.com/default", childPtr.getNamespaceURI(""));
        assertEquals("http://example.com/default", childPtr.getNamespaceURI((String) null));

        Document docWithoutDefault = createDocument();
        Element plainRoot = docWithoutDefault.createElement("plain");
        docWithoutDefault.appendChild(plainRoot);
        DOMNodePointer plainPtr = new DOMNodePointer(plainRoot, Locale.ENGLISH);
        assertNull(plainPtr.getDefaultNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetValueAndStringValue() throws Exception {
        Document doc = createDocument();
        Comment comment = doc.createComment("  a comment with spaces  ");
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("a comment with spaces", commentPtr.getValue());

        Element preserveElem = doc.createElement("elem");
        preserveElem.setAttribute("xml:space", "preserve");
        Text preserveText = doc.createTextNode("  untouched text  ");
        preserveElem.appendChild(preserveText);
        DOMNodePointer preservePtr = new DOMNodePointer(preserveElem, Locale.ENGLISH);
        assertEquals("  untouched text  ", preservePtr.getValue());

        Element trimElem = doc.createElement("elem");
        Text trimText = doc.createTextNode("  trimmed text  ");
        trimElem.appendChild(trimText);
        DOMNodePointer trimPtr = new DOMNodePointer(trimElem, Locale.ENGLISH);
        assertEquals("trimmed text", trimPtr.getValue());

        ProcessingInstruction pi = doc.createProcessingInstruction("piTarget", "  some pi data  ");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("some pi data", piPtr.getValue());

        trimElem.appendChild(doc.createComment("ignored comment in tree"));
        trimElem.appendChild(doc.createTextNode(" and more"));
        assertEquals("trimmed text and more", trimPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testIsLanguage() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element child = doc.createElement("child");
        parent.appendChild(child);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.GERMAN);
        assertTrue(childPtr.isLanguage("en"));
        assertTrue(childPtr.isLanguage("EN-US"));
        assertFalse(childPtr.isLanguage("fr"));

        Element noLang = doc.createElement("noLang");
        DOMNodePointer noLangPtr = new DOMNodePointer(noLang, Locale.FRENCH);
        assertTrue(noLangPtr.isLanguage("fr"));
        assertFalse(noLangPtr.isLanguage("de"));
    }

    @Test(timeout = 4000)
    public void testIterators() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://ns", "root");
        root.setAttribute("attr", "val");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p", "http://ns");
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        NodeIterator childIt = rootPtr.childIterator(new NodeNameTest(new QName("child")), false, null);
        assertNotNull(childIt);
        assertTrue(childIt.setPosition(1));
        assertNotNull(childIt.getNodePointer());

        NodeIterator attrIt = rootPtr.attributeIterator(new QName("attr"));
        assertNotNull(attrIt);
        assertTrue(attrIt.setPosition(1));
        assertNotNull(attrIt.getNodePointer());

        NodeIterator nsIt = rootPtr.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = rootPtr.namespacePointer("p");
        assertNotNull(nsPtr);
        assertEquals("p", nsPtr.getName().getName());

        NamespaceResolver nsr = rootPtr.getNamespaceResolver();
        assertNotNull(nsr);
        assertSame(nsr, rootPtr.getNamespaceResolver());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeTestNull() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        assertTrue(DOMNodePointer.testNode(elem, null));
    }

    @Test(timeout = 4000)
    public void testNodeNameTestEvaluation() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://example.com/ns", "p:tag");
        Text text = doc.createTextNode("content");

        assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("tag"))));

        NodeNameTest wildcardTest = new NodeNameTest(new QName("*"));
        assertTrue(DOMNodePointer.testNode(elem, wildcardTest));

        NodeNameTest wildcardWithPrefixMatch = new NodeNameTest(new QName("p", "*"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(elem, wildcardWithPrefixMatch));

        NodeNameTest wildcardWithPrefixMismatch = new NodeNameTest(new QName("other", "*"), "http://other.com");
        assertFalse(DOMNodePointer.testNode(elem, wildcardWithPrefixMismatch));

        NodeNameTest exactMatch = new NodeNameTest(new QName("p", "tag"), "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(elem, exactMatch));

        NodeNameTest wrongName = new NodeNameTest(new QName("p", "wrong"), "http://example.com/ns");
        assertFalse(DOMNodePointer.testNode(elem, wrongName));

        Element unnamespaced = doc.createElement("simple");
        NodeNameTest unnamespacedMatch = new NodeNameTest(new QName("simple"));
        assertTrue(DOMNodePointer.testNode(unnamespaced, unnamespacedMatch));
    }

    @Test(timeout = 4000)
    public void testNodeTypeTestEvaluation() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("elem");
        Text text = doc.createTextNode("text");
        CDATASection cdata = doc.createCDATASection("cdata");
        Comment comment = doc.createComment("comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");

        assertTrue(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(Compiler.NODE_TYPE_PI)));

        assertFalse(DOMNodePointer.testNode(elem, new NodeTypeTest(9999)));
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionTestEvaluation() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("targetName", "data");
        Element elem = doc.createElement("elem");

        assertTrue(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("targetName")));
        assertFalse(DOMNodePointer.testNode(pi, new ProcessingInstructionTest("otherTarget")));
        assertFalse(DOMNodePointer.testNode(elem, new ProcessingInstructionTest("targetName")));

        NodeTest dummyTest = new NodeTest() {};
        assertFalse(DOMNodePointer.testNode(pi, dummyTest));
    }

    @Test(timeout = 4000)
    public void testGetPrefixAndLocalNameFallbacks() throws Exception {
        Document doc = createDocument();
        // DOM Level 1 element without namespace support
        Element dom1Elem = doc.createElement("pref:elemName");
        assertEquals("pref", DOMNodePointer.getPrefix(dom1Elem));
        assertEquals("elemName", DOMNodePointer.getLocalName(dom1Elem));

        Element simpleElem = doc.createElement("plainName");
        assertNull(DOMNodePointer.getPrefix(simpleElem));
        assertEquals("plainName", DOMNodePointer.getLocalName(simpleElem));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JXPath-154)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * org.apache.commons.jxpath.ri.model.JXPath154Test::testInnerEmptyNamespaceDOM
     * ComparisonFailure: expected:</b:foo[1]/[test[1]]> but was:</b:foo[1]/[node()[2]]>
     */
    @Test(timeout = 4000)
    public void testInnerEmptyNamespaceDOM() throws Exception {
        Document doc = createDocument();
        Element foo = doc.createElementNS("http://foo", "b:foo");
        doc.appendChild(foo);

        Element bar = doc.createElementNS("http://foo", "b:bar");
        foo.appendChild(bar);

        Element test = doc.createElementNS("", "test");
        test.setAttribute("xmlns", "");
        foo.appendChild(test);

        DOMNodePointer rootPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        DOMNodePointer fooPtr = new DOMNodePointer(rootPtr, foo);
        DOMNodePointer testPtr = new DOMNodePointer(fooPtr, test);

        assertEquals("/b:foo[1]/test[1]", testPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathVariousNodeTypes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH, "root-id");
        assertEquals("id('root-id')", rootPtr.asPath());

        DOMNodePointer plainRootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        Element child = doc.createElement("child");
        root.appendChild(child);
        DOMNodePointer childPtr = new DOMNodePointer(plainRootPtr, child);
        assertEquals("/child[1]", childPtr.asPath());

        Text text = doc.createTextNode("txt");
        child.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(childPtr, text);
        assertEquals("/child[1]/text()[1]", textPtr.asPath());

        CDATASection cdata = doc.createCDATASection("data");
        child.appendChild(cdata);
        DOMNodePointer cdataPtr = new DOMNodePointer(childPtr, cdata);
        assertEquals("/child[1]/text()[2]", cdataPtr.asPath());

        ProcessingInstruction pi = doc.createProcessingInstruction("myPI", "data");
        child.appendChild(pi);
        DOMNodePointer piPtr = new DOMNodePointer(childPtr, pi);
        assertEquals("/child[1]/processing-instruction('myPI')[1]", piPtr.asPath());

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("", docPtr.asPath());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootThrowsException() throws Exception {
        Document doc = createDocument();
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        docPtr.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveChildSuccess() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Element child = doc.createElement("child");
        root.appendChild(child);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, child);

        childPtr.remove();
        assertNull(child.getParentNode());
        assertFalse(root.hasChildNodes());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeUnknownNamespacePrefixThrowsException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(root);
        rootPtr.createAttribute(context, new QName("unknownPrefix", "newAttr"));
    }

    @Test(timeout = 4000)
    public void testCreateAttributeSuccess() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(root);

        NodePointer attrPtr = rootPtr.createAttribute(context, new QName("myAttr"));
        assertNotNull(attrPtr);
        assertEquals("myAttr", attrPtr.getName().getName());
        assertTrue(root.hasAttribute("myAttr"));

        // If attribute already exists, returns pointer without error
        NodePointer existingAttrPtr = rootPtr.createAttribute(context, new QName("myAttr"));
        assertNotNull(existingAttrPtr);
    }

    @Test(timeout = 4000)
    public void testCreateAttributeOnNonElementDelegates() throws Exception {
        Document doc = createDocument();
        Text text = doc.createTextNode("content");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        try {
            textPtr.createAttribute(context, new QName("attr"));
            fail("Expected JXPathException when creating attribute on non-element");
        } catch (JXPathException expected) {
            assertTrue(expected.getMessage().contains("Cannot create an attribute"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateChildWithAbstractFactory() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object parentNode, String name, int index) {
                Element child = ((Node) parentNode).getOwnerDocument().createElement(name);
                ((Node) parentNode).appendChild(child);
                return true;
            }
        });

        NodePointer childPtr = rootPtr.createChild(context, new QName("subElem"), 0);
        assertNotNull(childPtr);
        assertEquals("subElem", childPtr.getName().getName());

        NodePointer childWithValuePtr = rootPtr.createChild(context, new QName("valElem"), 1, "testVal");
        assertNotNull(childWithValuePtr);
        assertEquals("testVal", childWithValuePtr.getValue());
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildFailsThrowsException() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object parentNode, String name, int index) {
                return false;
            }
        });

        rootPtr.createChild(context, new QName("uncreatable"), 0);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() throws Exception {
        Document doc = createDocument();
        Element el1 = doc.createElement("item");
        Element el2 = doc.createElement("item");

        DOMNodePointer ptr1a = new DOMNodePointer(el1, Locale.ENGLISH);
        DOMNodePointer ptr1b = new DOMNodePointer(el1, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(el2, Locale.ENGLISH);

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(null));
        assertFalse(ptr1a.equals("not a pointer"));

        assertEquals(el1.hashCode(), ptr1a.hashCode());
        assertEquals(ptr1a.hashCode(), ptr1b.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Attr a1 = doc.createAttribute("attr1");
        Attr a2 = doc.createAttribute("attr2");
        root.setAttributeNode(a1);
        root.setAttributeNode(a2);

        Element c1 = doc.createElement("c1");
        Element c2 = doc.createElement("c2");
        root.appendChild(c1);
        root.appendChild(c2);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer ptrA1 = new DOMNodePointer(rootPtr, a1);
        DOMNodePointer ptrA2 = new DOMNodePointer(rootPtr, a2);
        DOMNodePointer ptrC1 = new DOMNodePointer(rootPtr, c1);
        DOMNodePointer ptrC2 = new DOMNodePointer(rootPtr, c2);

        assertEquals(0, rootPtr.compareChildNodePointers(ptrC1, ptrC1));
        assertEquals(-1, rootPtr.compareChildNodePointers(ptrA1, ptrC1));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrC1, ptrA1));

        int attrCmp = rootPtr.compareChildNodePointers(ptrA1, ptrA2);
        int attrCmpRev = rootPtr.compareChildNodePointers(ptrA2, ptrA1);
        assertTrue((attrCmp < 0 && attrCmpRev > 0) || (attrCmp > 0 && attrCmpRev < 0));

        assertEquals(-1, rootPtr.compareChildNodePointers(ptrC1, ptrC2));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrC2, ptrC1));
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("id", "rootId");
        root.setIdAttribute("id", true);
        doc.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);

        Pointer foundPtr = docPtr.getPointerByID(context, "rootId");
        assertTrue(foundPtr instanceof DOMNodePointer);
        assertSame(root, foundPtr.getNode());

        Pointer notFoundPtr = docPtr.getPointerByID(context, "unknownId");
        assertTrue(notFoundPtr instanceof NullPointer);
    }

    @Test(timeout = 4000)
    public void testSetValue() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        doc.appendChild(parent);

        Text text = doc.createTextNode("original");
        parent.appendChild(text);
        DOMNodePointer textPtr = new DOMNodePointer(parent, text);

        textPtr.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        // Setting empty string on text node removes it from parent
        textPtr.setValue("");
        assertNull(text.getParentNode());

        // Setting string on element replaces children
        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.ENGLISH);
        parentPtr.setValue("new text content");
        assertEquals("new text content", parent.getTextContent());

        // Setting Element on element clones and appends children of the passed element
        Element sourceElem = doc.createElement("source");
        sourceElem.appendChild(doc.createElement("nested1"));
        sourceElem.appendChild(doc.createElement("nested2"));
        parentPtr.setValue(sourceElem);
        assertEquals(2, parent.getChildNodes().getLength());
        assertEquals("nested1", parent.getFirstChild().getNodeName());

        // Setting single non-Element/non-Document Node appends cloned node
        Comment sourceComment = doc.createComment("a cloned comment");
        parentPtr.setValue(sourceComment);
        assertEquals(1, parent.getChildNodes().getLength());
        assertTrue(parent.getFirstChild() instanceof Comment);
    }
}