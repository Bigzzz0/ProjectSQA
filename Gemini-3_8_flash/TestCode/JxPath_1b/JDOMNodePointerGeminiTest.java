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
package org.apache.commons.jxpath.ri.model.jdom;

import java.util.*;
import org.jdom.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ri.*;
import org.apache.commons.jxpath.ri.model.*;
import org.apache.commons.jxpath.ri.compiler.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Targeted Defect (JXPath-1 / JXPATH-12):
 *    - In `asPath()`, child element pointer resolves namespace using `getNamespaceResolver().getDefaultNamespaceURI()`.
 *    - If the pointer hierarchy was instantiated without a pre-configured NamespaceResolver, `getNamespaceResolver()`
 *      returns `null`, triggering a latent `NullPointerException`.
 *    - Target Method: `testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12()`
 *
 * 2. Partition A: Initialization & Type Introspection
 *    - Constructors: (node, locale), (node, locale, id), (parent, node).
 *    - Core invariants: isLeaf() for Element/Document (empty vs non-empty content), Text, CDATA, Comment, PI.
 *    - isCollection() == false, getLength() == 1, getBaseValue() and getImmediateNode() equality.
 *    - getName() for Element (with/without namespace prefix), PI (target), Text/Comment (null).
 *    - hashCode() and equals() identity/type checks.
 *
 * 3. Partition B: Node Matching & Filtering (`testNode(NodeTest)`)
 *    - NodeNameTest: Non-element vs Element; wildcard without prefix, wildcard with prefix + NS match/mismatch;
 *      name match + NS equal/mismatch.
 *    - NodeTypeTest: Compiler.NODE_TYPE_NODE, NODE_TYPE_TEXT (Text & CDATA), NODE_TYPE_COMMENT, NODE_TYPE_PI,
 *      and unknown type fallthrough.
 *    - ProcessingInstructionTest: Matching vs mismatch target; non-PI node fallthrough.
 *    - null NodeTest boundary handling (always true).
 *
 * 4. Partition C: XPath Representation (`asPath()`)
 *    - id attribute present -> "id('...')" with single/double quote XML escaping.
 *    - Element child: resolver default NS match (`elem[idx]`), prefix match (`prefix:elem[idx]`),
 *      fallback unknown NS (`node()[idx]`).
 *    - Text / CDATA children: `/text()[idx]`.
 *    - PI child: `/processing-instruction('target')[idx]`.
 *
 * 5. Partition D: Content Mutation & Value Manipulation
 *    - getValue(): Element textTrim, Comment trimmed text, Text/CDATA trimmed text, PI trimmed data, Document null.
 *    - setValue(Object):
 *        * On Text: Non-empty string sets text, empty/null removes text node from parent.
 *        * On Element: Clears content and adds Element, Document, Text/CDATA, PI, Comment, String.
 *    - createAttribute(): Non-element delegation, element without prefix, element with prefix (known & unknown NS).
 *    - createChild(): Factory null vs factory success / failure.
 *    - remove(): Root node rejection (JXPathException), child node detachment.
 *
 * 6. Partition E: Namespace Resolution & Node Comparison
 *    - getNamespaceURI() and getNamespaceURI(prefix) on Element, Document, and unsupported nodes.
 *    - compareChildNodePointers(): identical pointers, Attribute vs non-Attribute, Attribute vs Attribute,
 *      content order comparison for Elements, non-Element comparison error check.
 *    - isLanguage(lang): xml:lang on node, xml:lang on parent/ancestor, fallback to super.isLanguage(lang).
 */
public class JDOMNodePointerGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (JxPath-1 / JXPATH-12)
    // =========================================================================

    /**
     * Targets JXPATH-12 / JXPath-1:
     * When constructing parent and child JDOMNodePointers directly, calling asPath()
     * on the child pointer must not crash with a NullPointerException when accessing
     * the NamespaceResolver.
     */
    @Test(timeout = 4000)
    public void testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);

        String path = childPointer.asPath();
        assertNotNull("Path should not be null", path);
        assertEquals("Calculated path for child element should be /child[1]", "/child[1]", path);
    }

    // =========================================================================
    // PARTITION A: NODE POINTER INITIALIZATION & TYPE INTROSPECTION
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndCoreProperties() {
        Element element = new Element("test");
        Locale locale = Locale.US;

        JDOMNodePointer pointer1 = new JDOMNodePointer(element, locale);
        assertSame(element, pointer1.getBaseValue());
        assertSame(element, pointer1.getImmediateNode());
        assertFalse(pointer1.isCollection());
        assertEquals(1, pointer1.getLength());

        JDOMNodePointer pointerWithId = new JDOMNodePointer(element, locale, "id123");
        assertEquals("id('id123')", pointerWithId.asPath());

        JDOMNodePointer childPointer = new JDOMNodePointer(pointer1, new Element("child"));
        assertSame(pointer1, childPointer.getParent());
    }

    @Test(timeout = 4000)
    public void testIsLeaf() {
        Element emptyElement = new Element("empty");
        JDOMNodePointer emptyPointer = new JDOMNodePointer(emptyElement, Locale.US);
        assertTrue("Empty element should be leaf", emptyPointer.isLeaf());

        Element parentElement = new Element("parent");
        parentElement.addContent(new Element("sub"));
        JDOMNodePointer parentPointer = new JDOMNodePointer(parentElement, Locale.US);
        assertFalse("Element with children should not be leaf", parentPointer.isLeaf());

        Document emptyDoc = new Document();
        JDOMNodePointer emptyDocPointer = new JDOMNodePointer(emptyDoc, Locale.US);
        assertTrue("Document without content should be leaf", emptyDocPointer.isLeaf());

        Document nonDoc = new Document(new Element("root"));
        JDOMNodePointer nonDocPointer = new JDOMNodePointer(nonDoc, Locale.US);
        assertFalse("Document with root element should not be leaf", nonDocPointer.isLeaf());

        Text text = new Text("content");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        assertTrue("Text node should always be leaf", textPointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetName() {
        Element elemNoPrefix = new Element("tag");
        JDOMNodePointer ptrNoPrefix = new JDOMNodePointer(elemNoPrefix, Locale.US);
        assertEquals(new QName(null, "tag"), ptrNoPrefix.getName());

        Namespace ns = Namespace.getNamespace("x", "http://example.com");
        Element elemWithPrefix = new Element("tag", ns);
        JDOMNodePointer ptrWithPrefix = new JDOMNodePointer(elemWithPrefix, Locale.US);
        assertEquals(new QName("x", "tag"), ptrWithPrefix.getName());

        ProcessingInstruction pi = new ProcessingInstruction("target-pi", "data-pi");
        JDOMNodePointer piPointer = new JDOMNodePointer(pi, Locale.US);
        assertEquals(new QName(null, "target-pi"), piPointer.getName());

        Text text = new Text("text");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        assertEquals(new QName(null, null), textPointer.getName());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element elem1 = new Element("item");
        Element elem2 = new Element("item");

        JDOMNodePointer ptr1 = new JDOMNodePointer(elem1, Locale.US);
        JDOMNodePointer ptr1Same = new JDOMNodePointer(elem1, Locale.US);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.US);

        assertTrue("Identity equality should hold", ptr1.equals(ptr1));
        assertTrue("Same underlying node should be equal", ptr1.equals(ptr1Same));
        assertFalse("Different underlying node should not be equal", ptr1.equals(ptr2));
        assertFalse("Comparison with non-pointer should be false", ptr1.equals("item"));
        assertFalse("Comparison with null should be false", ptr1.equals(null));

        assertEquals("Same node should produce identical hashcode", ptr1.hashCode(), ptr1Same.hashCode());
    }

    // =========================================================================
    // PARTITION B: NODE MATCHING & FILTERING (`testNode`)
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeWithNullTest() {
        Element element = new Element("item");
        assertTrue(JDOMNodePointer.testNode(null, element, null));
    }

    @Test(timeout = 4000)
    public void testNodeWithNameTest() {
        Element element = new Element("book", Namespace.getNamespace("lib", "http://library.org"));
        Text text = new Text("some text");

        // Non-element node should fail NodeNameTest
        assertFalse(JDOMNodePointer.testNode(null, text, new NodeNameTest(new QName("book"))));

        // Wildcard without prefix matches any element
        assertTrue(JDOMNodePointer.testNode(null, element, new NodeNameTest(new QName("*"))));

        // Wildcard with matching namespace
        NodeNameTest wildcardWithNs = new NodeNameTest(new QName("lib", "*"), "http://library.org");
        assertTrue(JDOMNodePointer.testNode(null, element, wildcardWithNs));

        // Wildcard with mismatching namespace
        NodeNameTest wildcardMismatchNs = new NodeNameTest(new QName("other", "*"), "http://other.org");
        assertFalse(JDOMNodePointer.testNode(null, element, wildcardMismatchNs));

        // Exact name test with matching namespace
        NodeNameTest exactMatch = new NodeNameTest(new QName("lib", "book"), "http://library.org");
        assertTrue(JDOMNodePointer.testNode(null, element, exactMatch));

        // Exact name test with mismatching local name
        NodeNameTest wrongName = new NodeNameTest(new QName("lib", "author"), "http://library.org");
        assertFalse(JDOMNodePointer.testNode(null, element, wrongName));

        // Exact name test with mismatching namespace URI
        NodeNameTest wrongNs = new NodeNameTest(new QName("lib", "book"), "http://other.org");
        assertFalse(JDOMNodePointer.testNode(null, element, wrongNs));
    }

    @Test(timeout = 4000)
    public void testNodeWithTypeTest() {
        Element element = new Element("item");
        Text text = new Text("hello");
        CDATA cdata = new CDATA("content");
        Comment comment = new Comment("a comment");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(JDOMNodePointer.testNode(null, element, nodeTest));
        assertFalse(JDOMNodePointer.testNode(null, text, nodeTest));

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(JDOMNodePointer.testNode(null, text, textTest));
        assertTrue(JDOMNodePointer.testNode(null, cdata, textTest));
        assertFalse(JDOMNodePointer.testNode(null, element, textTest));

        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(JDOMNodePointer.testNode(null, comment, commentTest));
        assertFalse(JDOMNodePointer.testNode(null, element, commentTest));

        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(JDOMNodePointer.testNode(null, pi, piTest));
        assertFalse(JDOMNodePointer.testNode(null, element, piTest));

        NodeTypeTest unknownTest = new NodeTypeTest(999);
        assertFalse(JDOMNodePointer.testNode(null, element, unknownTest));
    }

    @Test(timeout = 4000)
    public void testNodeWithProcessingInstructionTest() {
        ProcessingInstruction pi = new ProcessingInstruction("target-one", "data");
        Element element = new Element("item");

        ProcessingInstructionTest matchTest = new ProcessingInstructionTest("target-one");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("target-two");

        assertTrue(JDOMNodePointer.testNode(null, pi, matchTest));
        assertFalse(JDOMNodePointer.testNode(null, pi, mismatchTest));
        assertFalse(JDOMNodePointer.testNode(null, element, matchTest));
    }

    @Test(timeout = 4000)
    public void testNodeWithUnknownNodeTestSubclass() {
        NodeTest unknownNodeTest = new NodeTest() {};
        assertFalse(JDOMNodePointer.testNode(null, new Element("item"), unknownNodeTest));
    }

    // =========================================================================
    // PARTITION C (Continued): ADVANCED asPath() BRANCHES
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsPathWithEscapedId() {
        Element element = new Element("elem");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.US, "foo'bar\"baz");
        assertEquals("id('foo&apos;bar&quot;baz')", pointer.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathOnTextAndCDataAndPI() {
        Element root = new Element("root");
        Text text1 = new Text("hello");
        CDATA cdata1 = new CDATA("world");
        ProcessingInstruction pi1 = new ProcessingInstruction("target1", "content1");
        ProcessingInstruction pi2 = new ProcessingInstruction("target1", "content2");
        root.addContent(text1);
        root.addContent(cdata1);
        root.addContent(pi1);
        root.addContent(pi2);

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer textPointer = new JDOMNodePointer(rootPointer, text1);
        JDOMNodePointer cdataPointer = new JDOMNodePointer(rootPointer, cdata1);
        JDOMNodePointer pi1Pointer = new JDOMNodePointer(rootPointer, pi1);
        JDOMNodePointer pi2Pointer = new JDOMNodePointer(rootPointer, pi2);

        assertEquals("/text()[1]", textPointer.asPath());
        assertEquals("/text()[2]", cdataPointer.asPath());
        assertEquals("/processing-instruction('target1')[1]", pi1Pointer.asPath());
        assertEquals("/processing-instruction('target1')[2]", pi2Pointer.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathMultipleSiblingsRelativePosition() {
        Element root = new Element("root");
        Element a1 = new Element("a");
        Element a2 = new Element("a");
        Element b = new Element("b");
        root.addContent(a1);
        root.addContent(a2);
        root.addContent(b);

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer a1Pointer = new JDOMNodePointer(rootPointer, a1);
        JDOMNodePointer a2Pointer = new JDOMNodePointer(rootPointer, a2);
        JDOMNodePointer bPointer = new JDOMNodePointer(rootPointer, b);

        assertEquals("/a[1]", a1Pointer.asPath());
        assertEquals("/a[2]", a2Pointer.asPath());
        assertEquals("/b[1]", bPointer.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathWithNamespacesAndResolver() {
        Namespace ns = Namespace.getNamespace("myprefix", "http://test.org");
        Element root = new Element("root");
        Element child = new Element("child", ns);
        root.addContent(child);

        JXPathContext context = JXPathContext.newContext(root);
        context.registerNamespace("myprefix", "http://test.org");

        NodePointer rootPointer = (NodePointer) context.getPointer("/");
        NodeIterator it = rootPointer.childIterator(new NodeNameTest(new QName("myprefix", "child")), false, null);
        assertTrue(it.setPosition(1));
        NodePointer childPointer = it.getNodePointer();

        assertEquals("/myprefix:child[1]", childPointer.asPath());
    }

    // =========================================================================
    // PARTITION D: VALUE & CONTENT MUTATION
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetValueAcrossNodeTypes() {
        Element elem = new Element("item");
        elem.setText("   sample text   ");
        assertEquals("sample text", new JDOMNodePointer(elem, Locale.US).getValue());

        Comment comment = new Comment("  a comment  ");
        assertEquals("a comment", new JDOMNodePointer(comment, Locale.US).getValue());

        Text text = new Text("  text trimmed  ");
        assertEquals("text trimmed", new JDOMNodePointer(text, Locale.US).getValue());

        CDATA cdata = new CDATA("  cdata trimmed  ");
        assertEquals("cdata trimmed", new JDOMNodePointer(cdata, Locale.US).getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  pi data  ");
        assertEquals("pi data", new JDOMNodePointer(pi, Locale.US).getValue());

        Document doc = new Document(new Element("root"));
        assertNull(new JDOMNodePointer(doc, Locale.US).getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextNode() {
        Element parent = new Element("parent");
        Text text = new Text("initial");
        parent.addContent(text);

        JDOMNodePointer textPointer = new JDOMNodePointer(new JDOMNodePointer(parent, Locale.US), text);

        // Setting non-empty text updates value
        textPointer.setValue("updated");
        assertEquals("updated", text.getText());

        // Setting empty string removes text node from parent
        textPointer.setValue("");
        assertEquals(0, parent.getContent().size());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementNode() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, Locale.US);

        // 1. Set Element content (cloned)
        Element sourceElem = new Element("source");
        sourceElem.addContent(new Element("c1"));
        sourceElem.addContent(new Text("c2"));
        pointer.setValue(sourceElem);
        assertEquals(2, target.getContent().size());

        // 2. Set Document content (cloned)
        Document sourceDoc = new Document(new Element("docRoot"));
        pointer.setValue(sourceDoc);
        assertEquals(1, target.getContent().size());

        // 3. Set Text & CDATA
        pointer.setValue(new Text("freshText"));
        assertEquals("freshText", target.getText());
        pointer.setValue(new CDATA("freshCDATA"));
        assertEquals("freshCDATA", target.getText());

        // 4. Set ProcessingInstruction
        ProcessingInstruction pi = new ProcessingInstruction("targetPI", "data");
        pointer.setValue(pi);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof ProcessingInstruction);

        // 5. Set Comment
        Comment comment = new Comment("my comment");
        pointer.setValue(comment);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Comment);

        // 6. Set generic Object / String
        pointer.setValue("simple string");
        assertEquals("simple string", target.getText());

        // 7. Set empty string clears content
        pointer.setValue("");
        assertEquals(0, target.getContent().size());
    }

    @Test(timeout = 4000)
    public void testCreateAttribute() {
        Element elem = new Element("item");
        elem.addNamespaceDeclaration(Namespace.getNamespace("p", "http://p.org"));
        JDOMNodePointer pointer = new JDOMNodePointer(elem, Locale.US);
        JXPathContext context = JXPathContext.newContext(elem);

        // Create standard attribute
        NodePointer attrPtr1 = pointer.createAttribute(context, new QName("attr1"));
        assertNotNull(attrPtr1);
        assertEquals("attr1", elem.getAttributes().get(0).getName());

        // Create attribute with valid prefix
        NodePointer attrPtr2 = pointer.createAttribute(context, new QName("p", "attr2"));
        assertNotNull(attrPtr2);
        assertEquals("http://p.org", elem.getAttribute("attr2", Namespace.getNamespace("p", "http://p.org")).getNamespaceURI());

        // Setting attribute again should retrieve existing
        NodePointer attrPtr1Again = pointer.createAttribute(context, new QName("attr1"));
        assertNotNull(attrPtr1Again);
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testCreateAttributeWithUnknownPrefixThrowsException() {
        Element elem = new Element("item");
        JDOMNodePointer pointer = new JDOMNodePointer(elem, Locale.US);
        pointer.createAttribute(JXPathContext.newContext(elem), new QName("unknown", "attr"));
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testCreateAttributeOnNonElementThrowsException() {
        Text text = new Text("text");
        JDOMNodePointer pointer = new JDOMNodePointer(text, Locale.US);
        pointer.createAttribute(JXPathContext.newContext(text), new QName("attr"));
    }

    @Test(timeout = 4000)
    public void testCreateChildWithFactory() {
        Element root = new Element("root");
        JXPathContext context = JXPathContext.newContext(root);
        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object parentNode, String name, int index) {
                ((Element) parentNode).addContent(new Element(name));
                return true;
            }
        });

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.US);
        NodePointer childPtr = rootPointer.createChild(context, new QName("newChild"), 0, "childValue");
        assertNotNull(childPtr);
        assertEquals("childValue", childPtr.getValue());
        assertEquals("newChild", root.getChild("newChild").getName());
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testCreateChildWithoutFactoryThrowsException() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.US);
        rootPointer.createChild(JXPathContext.newContext(root), new QName("child"), 0);
    }

    @Test(timeout = 4000)
    public void testRemoveChild() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);

        JDOMNodePointer childPointer = new JDOMNodePointer(new JDOMNodePointer(parent, Locale.US), child);
        childPointer.remove();
        assertEquals(0, parent.getContent().size());
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testRemoveRootThrowsException() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.US);
        rootPointer.remove();
    }

    // =========================================================================
    // PARTITION E: NAMESPACE & POINTER COMPARISONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetNamespaceURI() {
        Namespace ns = Namespace.getNamespace("p", "http://ns.com");
        Element elemWithNs = new Element("tag", ns);
        Element elemNoNs = new Element("tag");

        assertEquals("http://ns.com", new JDOMNodePointer(elemWithNs, Locale.US).getNamespaceURI());
        assertNull(new JDOMNodePointer(elemNoNs, Locale.US).getNamespaceURI());
        assertNull(new JDOMNodePointer(new Text("txt"), Locale.US).getNamespaceURI());

        // By prefix on Element
        assertEquals("http://ns.com", new JDOMNodePointer(elemWithNs, Locale.US).getNamespaceURI("p"));
        assertNull(new JDOMNodePointer(elemWithNs, Locale.US).getNamespaceURI("unknown"));

        // By prefix on Document
        Document doc = new Document(elemWithNs);
        assertEquals("http://ns.com", new JDOMNodePointer(doc, Locale.US).getNamespaceURI("p"));

        // By prefix on other node
        assertNull(new JDOMNodePointer(new Text("txt"), Locale.US).getNamespaceURI("p"));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() {
        Element parent = new Element("parent");
        Attribute a1 = new Attribute("a1", "v1");
        Attribute a2 = new Attribute("a2", "v2");
        Element c1 = new Element("c1");
        Element c2 = new Element("c2");

        parent.setAttribute(a1);
        parent.setAttribute(a2);
        parent.addContent(c1);
        parent.addContent(c2);

        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.US);
        JDOMNodePointer pA1 = new JDOMNodePointer(parentPtr, a1);
        JDOMNodePointer pA2 = new JDOMNodePointer(parentPtr, a2);
        JDOMNodePointer pC1 = new JDOMNodePointer(parentPtr, c1);
        JDOMNodePointer pC2 = new JDOMNodePointer(parentPtr, c2);

        // Identical nodes
        assertEquals(0, parentPtr.compareChildNodePointers(pA1, pA1));

        // Attribute vs non-attribute
        assertEquals(-1, parentPtr.compareChildNodePointers(pA1, pC1));
        assertEquals(1, parentPtr.compareChildNodePointers(pC1, pA1));

        // Attribute order
        assertEquals(-1, parentPtr.compareChildNodePointers(pA1, pA2));
        assertEquals(1, parentPtr.compareChildNodePointers(pA2, pA1));

        // Element content order
        assertEquals(-1, parentPtr.compareChildNodePointers(pC1, pC2));
        assertEquals(1, parentPtr.compareChildNodePointers(pC2, pC1));
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCompareChildNodePointersOnNonElementThrowsException() {
        Text text = new Text("txt");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        textPtr.compareChildNodePointers(new JDOMNodePointer(textPtr, new Element("a")),
                                         new JDOMNodePointer(textPtr, new Element("b")));
    }

    @Test(timeout = 4000)
    public void testIsLanguage() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);

        assertTrue("Root should match exact language", rootPtr.isLanguage("en-US"));
        assertTrue("Root should match prefix language", rootPtr.isLanguage("en"));
        assertFalse("Root should not match different language", rootPtr.isLanguage("fr"));

        assertTrue("Child inherits parent language", childPtr.isLanguage("en"));

        // Fallback when no xml:lang attribute exists
        Element plain = new Element("plain");
        JDOMNodePointer plainPtr = new JDOMNodePointer(plain, Locale.GERMAN);
        assertTrue("Fallback to locale language", plainPtr.isLanguage("de"));
    }

    @Test(timeout = 4000)
    public void testIteratorsAndStaticHelpers() {
        Element element = new Element("test", Namespace.getNamespace("pre", "http://test"));
        Attribute attr = new Attribute("attr", "val", Namespace.getNamespace("pre", "http://test"));
        element.setAttribute(attr);

        assertEquals("pre", JDOMNodePointer.getPrefix(element));
        assertEquals("test", JDOMNodePointer.getLocalName(element));
        assertEquals("pre", JDOMNodePointer.getPrefix(attr));
        assertEquals("attr", JDOMNodePointer.getLocalName(attr));
        assertNull(JDOMNodePointer.getPrefix(new Text("txt")));
        assertNull(JDOMNodePointer.getLocalName(new Text("txt")));

        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.US);
        assertNotNull(pointer.childIterator(new NodeNameTest(new QName("test")), false, null));
        assertNotNull(pointer.attributeIterator(new QName("attr")));
        assertNotNull(pointer.namespaceIterator());
        assertNotNull(pointer.namespacePointer("pre"));
    }
}