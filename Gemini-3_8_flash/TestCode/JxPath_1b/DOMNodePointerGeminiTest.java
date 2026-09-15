/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.model.dom.DOMNodePointer
 *
 * 1. Defect Coverage (JXPATH-12 / JXPath-1):
 *    - Method: asPath()
 *    - Branch: Element node when parent is DOMNodePointer, resolving namespaceURI via getNamespaceResolver().
 *    - Defect: When a DOMNodePointer hierarchy is instantiated directly without an attached JXPathContext,
 *              calling getNamespaceResolver() can lead to NullPointerException when resolving default namespace or prefix.
 *    - Test: testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12()
 *
 * 2. Partition A: Initialization, Introspection & Equality
 *    - Constructors: (Node, Locale), (Node, Locale, Id), (NodePointer, Node).
 *    - Introspection: isLeaf() [with/without children], isCollection() [false], getLength() [1], isActual() [true].
 *    - Identity: equals() [same instance, equal node, different node, non-DOMNodePointer], hashCode() [System.identityHashCode].
 *    - Language: isLanguage() traversing hierarchy for xml:lang attribute; fallback to super.isLanguage().
 *
 * 3. Partition B: Node Testing & Filtering (testNode / equalStrings)
 *    - null test handling -> returns true.
 *    - NodeNameTest:
 *      * Non-element node rejection.
 *      * Wildcard with null prefix -> returns true.
 *      * Wildcard with prefix vs matching local name with namespace check.
 *      * Mismatched local name / namespace.
 *    - NodeTypeTest:
 *      * NODE_TYPE_NODE -> Element.
 *      * NODE_TYPE_TEXT -> CDATA or Text.
 *      * NODE_TYPE_COMMENT -> Comment.
 *      * NODE_TYPE_PI -> ProcessingInstruction.
 *      * Other unknown types -> false.
 *    - ProcessingInstructionTest: Target matching and non-matching.
 *    - equalStrings(): null vs empty, null vs non-empty, matching strings, mismatched strings.
 *
 * 4. Partition C: Navigation & Structural Path (asPath, escape, positions)
 *    - asPath() with id (escaping ' and " characters).
 *    - asPath() on Element, Text, CDATA, ProcessingInstruction, Document nodes.
 *    - Relative position calculation across siblings for name, element, text, and PI.
 *
 * 5. Partition D: Value Operations & Mutation (getValue, setValue, remove, createAttribute)
 *    - getValue() for Comment, Text, CDATA, PI, Element (recursive text concat).
 *    - setValue() on Text/CDATA (update string or remove node if empty).
 *    - setValue() on Element with String, Node (Element/Document children cloned, other nodes appended).
 *    - remove() on root node (throws JXPathException) vs child node.
 *    - createAttribute() with prefix (resolvable vs unknown prefix throwing exception), and without prefix.
 *
 * 6. Partition E: Namespace Handling & Pointer Comparison
 *    - getNamespaceURI(), getDefaultNamespaceURI(), getNamespaceURI(prefix):
 *      * "xml", "xmlns", custom prefixes, cached prefixes, hierarchy resolution, UNKNOWN_NAMESPACE.
 *    - compareChildNodePointers():
 *      * Same node (0).
 *      * Attribute vs non-attribute (-1 and 1).
 *      * Attribute vs attribute (relative order in NamedNodeMap).
 *      * Child vs child (sibling traversal order).
 *    - getPointerByID(): Document node vs element's owner document; found vs NullPointer.
 */

package org.apache.commons.jxpath.ri.model.dom;

import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ri.*;
import org.apache.commons.jxpath.ri.model.*;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.jxpath.ri.compiler.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class DOMNodePointerGeminiTest {

    private Document createDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().newDocument();
    }

    // =========================================================================
    // CRITICAL DEFECT TEST: JXPATH-12 / JXPath-1
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);
        doc.appendChild(root);

        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.getDefault());
        DOMNodePointer rootPointer = new DOMNodePointer(docPointer, root);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        String path = childPointer.asPath();
        assertNotNull("Path should not be null", path);
        assertEquals("/root[1]/child[1]", path);
    }

    // =========================================================================
    // PARTITION A: Constructors, Identity, Introspection & Language
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndBasics() throws Exception {
        Document doc = createDocument();
        Element element = doc.createElement("testElem");
        doc.appendChild(element);

        DOMNodePointer ptr = new DOMNodePointer(element, Locale.ENGLISH);
        assertSame(element, ptr.getBaseValue());
        assertSame(element, ptr.getImmediateNode());
        assertSame(element, ptr.getNode());
        assertEquals(1, ptr.getLength());
        assertTrue(ptr.isActual());
        assertFalse(ptr.isCollection());
        assertTrue(ptr.isLeaf());

        element.appendChild(doc.createTextNode("text"));
        assertFalse(ptr.isLeaf());

        DOMNodePointer ptrWithId = new DOMNodePointer(element, Locale.ENGLISH, "id123");
        assertEquals("id('id123')", ptrWithId.asPath());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() throws Exception {
        Document doc = createDocument();
        Element el1 = doc.createElement("elem1");
        Element el2 = doc.createElement("elem2");

        DOMNodePointer ptr1a = new DOMNodePointer(el1, Locale.ENGLISH);
        DOMNodePointer ptr1b = new DOMNodePointer(el1, Locale.ENGLISH);
        DOMNodePointer ptr2 = new DOMNodePointer(el2, Locale.ENGLISH);

        assertTrue(ptr1a.equals(ptr1a));
        assertTrue(ptr1a.equals(ptr1b));
        assertFalse(ptr1a.equals(ptr2));
        assertFalse(ptr1a.equals(null));
        assertFalse(ptr1a.equals("stringObject"));

        assertEquals(System.identityHashCode(el1), ptr1a.hashCode());
    }

    @Test(timeout = 4000)
    public void testLanguageHandling() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("xml:lang", "en-US");
        Element childNoLang = doc.createElement("child");
        Element childWithLang = doc.createElement("childWithLang");
        childWithLang.setAttribute("xml:lang", "fr-FR");

        parent.appendChild(childNoLang);
        parent.appendChild(childWithLang);
        doc.appendChild(parent);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.ENGLISH);
        DOMNodePointer childNoLangPtr = new DOMNodePointer(parentPtr, childNoLang);
        DOMNodePointer childWithLangPtr = new DOMNodePointer(parentPtr, childWithLang);

        assertTrue(parentPtr.isLanguage("en"));
        assertTrue(parentPtr.isLanguage("EN-US"));
        assertFalse(parentPtr.isLanguage("fr"));

        // Inherits from parent
        assertTrue(childNoLangPtr.isLanguage("en"));
        assertFalse(childNoLangPtr.isLanguage("fr"));

        // Overrides parent
        assertTrue(childWithLangPtr.isLanguage("fr"));
        assertFalse(childWithLangPtr.isLanguage("en"));
    }

    // =========================================================================
    // PARTITION B: Node Matching & Filtering (testNode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeWithNullTest() throws Exception {
        Document doc = createDocument();
        Element element = doc.createElement("sample");
        DOMNodePointer ptr = new DOMNodePointer(element, Locale.ENGLISH);

        assertTrue(ptr.testNode(null));
        assertTrue(DOMNodePointer.testNode(element, null));
    }

    @Test(timeout = 4000)
    public void testNodeWithNameTest() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://gemini.org/ns", "gem:testNode");
        Text text = doc.createTextNode("sample");

        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.ENGLISH);
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);

        // Non-element node against NodeNameTest returns false
        NodeNameTest nameTest = new NodeNameTest(new QName("testNode"));
        assertFalse(textPtr.testNode(nameTest));

        // Wildcard with null prefix returns true
        NodeNameTest wildcardNullPrefix = new NodeNameTest(new QName(null, "*"));
        assertTrue(elemPtr.testNode(wildcardNullPrefix));

        // Wildcard with prefix matching namespace
        NodeNameTest wildcardWithPrefix = new NodeNameTest(new QName("gem", "*"), "http://gemini.org/ns");
        assertTrue(elemPtr.testNode(wildcardWithPrefix));

        // Exact match
        NodeNameTest exactTest = new NodeNameTest(new QName("gem", "testNode"), "http://gemini.org/ns");
        assertTrue(elemPtr.testNode(exactTest));

        // Mismatched local name
        NodeNameTest wrongNameTest = new NodeNameTest(new QName("gem", "otherNode"), "http://gemini.org/ns");
        assertFalse(elemPtr.testNode(wrongNameTest));

        // Mismatched namespace
        NodeNameTest wrongNsTest = new NodeNameTest(new QName("gem", "testNode"), "http://wrong.org/ns");
        assertFalse(elemPtr.testNode(wrongNsTest));
    }

    @Test(timeout = 4000)
    public void testNodeWithTypeTest() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("e");
        Text text = doc.createTextNode("txt");
        CDATASection cdata = doc.createCDATASection("cdata");
        Comment comment = doc.createComment("comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        NodeTypeTest unknownTest = new NodeTypeTest(999);

        assertTrue(DOMNodePointer.testNode(elem, nodeTest));
        assertFalse(DOMNodePointer.testNode(text, nodeTest));

        assertTrue(DOMNodePointer.testNode(text, textTest));
        assertTrue(DOMNodePointer.testNode(cdata, textTest));
        assertFalse(DOMNodePointer.testNode(elem, textTest));

        assertTrue(DOMNodePointer.testNode(comment, commentTest));
        assertFalse(DOMNodePointer.testNode(elem, commentTest));

        assertTrue(DOMNodePointer.testNode(pi, piTest));
        assertFalse(DOMNodePointer.testNode(elem, piTest));

        assertFalse(DOMNodePointer.testNode(elem, unknownTest));
    }

    @Test(timeout = 4000)
    public void testNodeWithProcessingInstructionTest() throws Exception {
        Document doc = createDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("targetAlpha", "data");
        Element elem = doc.createElement("targetAlpha");

        ProcessingInstructionTest piTestMatch = new ProcessingInstructionTest("targetAlpha");
        ProcessingInstructionTest piTestMismatch = new ProcessingInstructionTest("targetBeta");

        assertTrue(DOMNodePointer.testNode(pi, piTestMatch));
        assertFalse(DOMNodePointer.testNode(pi, piTestMismatch));
        assertFalse(DOMNodePointer.testNode(elem, piTestMatch));
    }

    // =========================================================================
    // PARTITION C: Path Generation, Escaping & Positional Indexing (asPath)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsPathWithIdAndEscaping() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("el");

        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH, "a'b\"c");
        assertEquals("id('a&apos;b&quot;c')", ptr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathDocumentNode() throws Exception {
        Document doc = createDocument();
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("", docPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathNonElementNodes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Text text1 = doc.createTextNode("one");
        Text text2 = doc.createTextNode("two");
        CDATASection cdata = doc.createCDATASection("three");
        ProcessingInstruction pi1 = doc.createProcessingInstruction("agent", "first");
        ProcessingInstruction pi2 = doc.createProcessingInstruction("agent", "second");
        ProcessingInstruction piOther = doc.createProcessingInstruction("other", "third");

        root.appendChild(text1);
        root.appendChild(text2);
        root.appendChild(cdata);
        root.appendChild(pi1);
        root.appendChild(pi2);
        root.appendChild(piOther);

        DOMNodePointer rootPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        DOMNodePointer elemPtr = new DOMNodePointer(rootPtr, root);

        DOMNodePointer t1Ptr = new DOMNodePointer(elemPtr, text1);
        DOMNodePointer t2Ptr = new DOMNodePointer(elemPtr, text2);
        DOMNodePointer cdPtr = new DOMNodePointer(elemPtr, cdata);
        DOMNodePointer pi1Ptr = new DOMNodePointer(elemPtr, pi1);
        DOMNodePointer pi2Ptr = new DOMNodePointer(elemPtr, pi2);
        DOMNodePointer piOtherPtr = new DOMNodePointer(elemPtr, piOther);

        assertEquals("/root[1]/text()[1]", t1Ptr.asPath());
        assertEquals("/root[1]/text()[2]", t2Ptr.asPath());
        assertEquals("/root[1]/text()[3]", cdPtr.asPath());
        assertEquals("/root[1]/processing-instruction('agent')[1]", pi1Ptr.asPath());
        assertEquals("/root[1]/processing-instruction('agent')[2]", pi2Ptr.asPath());
        assertEquals("/root[1]/processing-instruction('other')[1]", piOtherPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathWithParentNotDOMNodePointer() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElement("sample");

        VariablePointer varPointer = new VariablePointer(new QName("var"));
        DOMNodePointer ptr = new DOMNodePointer(varPointer, elem);

        assertEquals("$var", ptr.asPath());
    }

    // =========================================================================
    // PARTITION D: Content & Value Operations (getValue, setValue, remove)
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetValueAcrossNodeTypes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Comment comment = doc.createComment(" a comment ");
        Text text = doc.createTextNode(" text value ");
        CDATASection cdata = doc.createCDATASection(" cdata value ");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", " pi value ");

        assertEquals("a comment", new DOMNodePointer(comment, Locale.ENGLISH).getValue());
        assertEquals("text value", new DOMNodePointer(text, Locale.ENGLISH).getValue());
        assertEquals("cdata value", new DOMNodePointer(cdata, Locale.ENGLISH).getValue());
        assertEquals("pi value", new DOMNodePointer(pi, Locale.ENGLISH).getValue());

        Element child1 = doc.createElement("c1");
        child1.appendChild(doc.createTextNode("Hello "));
        Element child2 = doc.createElement("c2");
        child2.appendChild(doc.createTextNode("World"));
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        assertEquals("Hello World", rootPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextAndCDATA() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        Text text = doc.createTextNode("initial");
        root.appendChild(text);

        DOMNodePointer textPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPtr = new DOMNodePointer(textPtr, text);

        childPtr.setValue("updated");
        assertEquals("updated", text.getNodeValue());

        // Setting empty string removes node from parent
        childPtr.setValue("");
        assertNull(text.getParentNode());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithString() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.appendChild(doc.createElement("oldChild"));

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        rootPtr.setValue("New Content");

        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("New Content", root.getFirstChild().getNodeValue());

        // Set empty value removes all children
        rootPtr.setValue("");
        assertEquals(0, root.getChildNodes().getLength());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithNodes() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.appendChild(doc.createElement("oldChild"));

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        Element newElem = doc.createElement("container");
        newElem.appendChild(doc.createElement("child1"));
        newElem.appendChild(doc.createElement("child2"));

        rootPtr.setValue(newElem);
        assertEquals(2, root.getChildNodes().getLength());
        assertEquals("child1", root.getChildNodes().item(0).getNodeName());
        assertEquals("child2", root.getChildNodes().item(1).getNodeName());

        // Replace with single text node
        Text standaloneText = doc.createTextNode("standalone");
        rootPtr.setValue(standaloneText);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("standalone", root.getFirstChild().getNodeValue());
    }

    @Test(timeout = 4000)
    public void testRemove() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        DOMNodePointer childPtr = new DOMNodePointer(rootPtr, child);

        childPtr.remove();
        assertNull(child.getParentNode());

        try {
            rootPtr.remove();
            fail("Expected JXPathException when removing root node without DOM parent");
        } catch (JXPathException expected) {
            assertTrue(expected.getMessage().contains("Cannot remove root DOM node"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateAttribute() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://example.org/ns", "ex:root");
        root.setAttribute("xmlns:ex", "http://example.org/ns");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(doc);

        // Simple attribute
        NodePointer attrPtr = rootPtr.createAttribute(context, new QName("attr1"));
        assertNotNull(attrPtr);
        assertTrue(root.hasAttribute("attr1"));

        // Namespaced attribute
        NodePointer nsAttrPtr = rootPtr.createAttribute(context, new QName("ex", "attr2"));
        assertNotNull(nsAttrPtr);
        assertTrue(root.hasAttributeNS("http://example.org/ns", "attr2"));

        // Unknown namespace prefix
        try {
            rootPtr.createAttribute(context, new QName("unknown", "attr3"));
            fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException expected) {
            assertTrue(expected.getMessage().contains("Unknown namespace prefix: unknown"));
        }

        // On non-element node
        Text text = doc.createTextNode("sample");
        DOMNodePointer textPtr = new DOMNodePointer(text, Locale.ENGLISH);
        try {
            textPtr.createAttribute(context, new QName("any"));
            fail("Expected JXPathException when creating attribute on non-element");
        } catch (JXPathException expected) {
            // Expected from super.createAttribute
        }
    }

    // =========================================================================
    // PARTITION E: Namespaces, Identifiers & Pointer Comparison
    // =========================================================================

    @Test(timeout = 4000)
    public void testNamespaceResolutions() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElementNS("http://default.org", "root");
        root.setAttribute("xmlns", "http://default.org");
        root.setAttribute("xmlns:custom", "http://custom.org");
        Element child = doc.createElementNS("http://custom.org", "custom:child");
        root.appendChild(child);
        doc.appendChild(root);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);

        assertEquals("http://www.w3.org/XML/1998/namespace", childPtr.getNamespaceURI("xml"));
        assertEquals("http://www.w3.org/2000/xmlns/", childPtr.getNamespaceURI("xmlns"));
        assertEquals("http://default.org", childPtr.getNamespaceURI(""));
        assertEquals("http://default.org", childPtr.getNamespaceURI((String) null));
        assertEquals("http://custom.org", childPtr.getNamespaceURI("custom"));
        assertNull(childPtr.getNamespaceURI("unknownPrefix"));

        // Document level fallback
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://default.org", docPtr.getDefaultNamespaceURI());
        assertEquals("http://custom.org", docPtr.getNamespaceURI("custom"));

        // Static helpers
        assertEquals("custom", DOMNodePointer.getPrefix(child));
        assertEquals("child", DOMNodePointer.getLocalName(child));
        assertEquals("http://custom.org", DOMNodePointer.getNamespaceURI(child));

        // Unprefixed node
        assertEquals(null, DOMNodePointer.getPrefix(root));
        assertEquals("root", DOMNodePointer.getLocalName(root));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = createDocument();
        Element parent = doc.createElement("parent");
        parent.setAttribute("a1", "v1");
        parent.setAttribute("a2", "v2");

        Element child1 = doc.createElement("c1");
        Element child2 = doc.createElement("c2");
        parent.appendChild(child1);
        parent.appendChild(child2);
        doc.appendChild(parent);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.ENGLISH);
        DOMNodePointer pChild1 = new DOMNodePointer(parentPtr, child1);
        DOMNodePointer pChild2 = new DOMNodePointer(parentPtr, child2);

        Attr a1 = parent.getAttributeNode("a1");
        Attr a2 = parent.getAttributeNode("a2");
        DOMNodePointer pAttr1 = new DOMNodePointer(parentPtr, a1);
        DOMNodePointer pAttr2 = new DOMNodePointer(parentPtr, a2);

        // Same node
        assertEquals(0, parentPtr.compareChildNodePointers(pChild1, pChild1));

        // Attribute vs Element
        assertEquals(-1, parentPtr.compareChildNodePointers(pAttr1, pChild1));
        assertEquals(1, parentPtr.compareChildNodePointers(pChild1, pAttr1));

        // Attribute vs Attribute
        assertEquals(-1, parentPtr.compareChildNodePointers(pAttr1, pAttr2));
        assertEquals(1, parentPtr.compareChildNodePointers(pAttr2, pAttr1));

        // Sibling vs Sibling
        assertEquals(-1, parentPtr.compareChildNodePointers(pChild1, pChild2));
        assertEquals(1, parentPtr.compareChildNodePointers(pChild2, pChild1));
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        root.setAttribute("id", "targetId");
        root.setIdAttribute("id", true);
        doc.appendChild(root);

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(doc);

        Pointer foundPtr = docPtr.getPointerByID(ctx, "targetId");
        assertTrue(foundPtr instanceof DOMNodePointer);
        assertSame(root, foundPtr.getNode());

        Pointer notFoundPtr = docPtr.getPointerByID(ctx, "nonExistent");
        assertTrue(notFoundPtr instanceof NullPointer);

        // From element pointer invoking getPointerByID
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        Pointer foundFromElem = rootPtr.getPointerByID(ctx, "targetId");
        assertTrue(foundFromElem instanceof DOMNodePointer);
        assertSame(root, foundFromElem.getNode());
    }

    @Test(timeout = 4000)
    public void testIteratorsInstantiation() throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        assertNotNull(rootPtr.childIterator(null, false, null));
        assertNotNull(rootPtr.attributeIterator(new QName("test")));
        assertNotNull(rootPtr.namespaceIterator());
        assertNotNull(rootPtr.namespacePointer("xml"));
    }

    @Test(timeout = 4000)
    public void testGetName() throws Exception {
        Document doc = createDocument();
        Element elem = doc.createElementNS("http://ns.org", "pfx:sample");
        ProcessingInstruction pi = doc.createProcessingInstruction("piTarget", "piData");
        Text text = doc.createTextNode("txt");

        assertEquals(new QName("pfx", "sample"), new DOMNodePointer(elem, Locale.ENGLISH).getName());
        assertEquals(new QName(null, "piTarget"), new DOMNodePointer(pi, Locale.ENGLISH).getName());
        assertEquals(new QName(null, null), new DOMNodePointer(text, Locale.ENGLISH).getName());
    }
}