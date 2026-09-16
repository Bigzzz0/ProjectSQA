package org.apache.commons.jxpath.ri.model.jdom;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer
 *
 * 1. Defect-Targeted Branch Zone (Defects4J ground truth: following/preceding axes navigation)
 *    - Following-axis traversal: child nodes vs. sibling nodes positioning and path resolution.
 *    - Preceding-axis traversal: ancestor-descendant boundary ordering and text node positioning.
 *    - Sibling/child comparison order in compareChildNodePointers (Attribute vs Content, Element vs Element).
 *
 * 2. Core Functional Logic & State Transitions:
 *    - getValue(): recursive text concatenation on Element, trimmed Comment, Text/PI with/without xml:space="preserve".
 *    - setValue(): Text replacement, Text removal on empty value, Element clearing and replacement with
 *      Element, Document, Text/CDATA, ProcessingInstruction, Comment, and primitive String.
 *    - testNode(): NodeNameTest (wildcard, prefix, namespace URI match/mismatch),
 *      NodeTypeTest (NODE_TYPE_NODE, TEXT, COMMENT, PI), ProcessingInstructionTest.
 *    - asPath(): id escaping (&apos;, &quot;), Element indexing by qualified name, namespace prefix,
 *      unresolved prefix fallback (node()[index]), Text/CDATA relative indexing (/text()[n]),
 *      PI relative indexing (/processing-instruction('target')[n]).
 *    - createChild(), createAttribute(), and remove(): factory success/failure, namespace validation, root node guard.
 *
 * 3. Boundary Values & Defensive Paths:
 *    - Null and blank namespaces, prefixes, attributes, and text values.
 *    - CompareChildNodePointers on non-Element node -> RuntimeException.
 *    - Remove node with null parent -> JXPathException.
 *    - equals() / hashCode() contract: identity, null, different types, different node instances.
 */

import java.util.Locale;

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
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.junit.Test;

import static org.junit.Assert.*;

public class JDOMNodePointerGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() {
        Element root = new Element("root");
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.US);

        assertSame(root, pointer.getBaseValue());
        assertSame(root, pointer.getImmediateNode());
        assertSame(root, pointer.getNode());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertTrue(pointer.isLeaf());

        root.addContent(new Element("child"));
        assertFalse(pointer.isLeaf());

        Document doc = new Document();
        JDOMNodePointer docPointer = new JDOMNodePointer(doc, Locale.US);
        assertTrue(docPointer.isLeaf());
        doc.setRootElement(root);
        assertFalse(docPointer.isLeaf());

        Text text = new Text("hello");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.US);
        assertTrue(textPointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetNameForVariousNodeTypes() {
        Element elemNoNs = new Element("simple");
        JDOMNodePointer ptr1 = new JDOMNodePointer(elemNoNs, Locale.US);
        QName name1 = ptr1.getName();
        assertNull(name1.getPrefix());
        assertEquals("simple", name1.getName());

        Namespace ns = Namespace.getNamespace("x", "http://example.com/x");
        Element elemWithNs = new Element("complex", ns);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elemWithNs, Locale.US);
        QName name2 = ptr2.getName();
        assertEquals("x", name2.getPrefix());
        assertEquals("complex", name2.getName());

        ProcessingInstruction pi = new ProcessingInstruction("targetApp", "dataValue");
        JDOMNodePointer ptr3 = new JDOMNodePointer(pi, Locale.US);
        QName name3 = ptr3.getName();
        assertNull(name3.getPrefix());
        assertEquals("targetApp", name3.getName());

        Text text = new Text("sample");
        JDOMNodePointer ptr4 = new JDOMNodePointer(text, Locale.US);
        QName name4 = ptr4.getName();
        assertNull(name4.getPrefix());
        assertNull(name4.getName());
    }

    @Test(timeout = 4000)
    public void testGetValueAcrossNodeTypes() {
        // Element with nested text and elements
        Element root = new Element("root");
        root.addContent(new Text("Hello "));
        Element child = new Element("child");
        child.addContent(new Text("World"));
        root.addContent(child);
        root.addContent(new Comment("comment to ignore"));

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        assertEquals("Hello World", rootPtr.getValue());

        // Comment trimming
        Comment comment = new Comment("  spaced comment  ");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.US);
        assertEquals("spaced comment", commentPtr.getValue());

        // ProcessingInstruction trimming
        ProcessingInstruction pi = new ProcessingInstruction("pi", "  pi data  ");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.US);
        assertEquals("pi data", piPtr.getValue());

        // Text with and without xml:space="preserve"
        Element textParent = new Element("container");
        Text textNode = new Text("  content  ");
        textParent.addContent(textNode);
        JDOMNodePointer textPtr = new JDOMNodePointer(textParent, textNode);
        assertEquals("content", textPtr.getValue());

        textParent.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
        assertEquals("  content  ", textPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextNode() {
        Element parent = new Element("parent");
        Text text = new Text("initial");
        parent.addContent(text);
        JDOMNodePointer textPtr = new JDOMNodePointer(parent, text);

        textPtr.setValue("updated");
        assertEquals("updated", text.getText());

        // Empty value removes text node from parent
        textPtr.setValue("");
        assertEquals(0, parent.getContent().size());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithVariousPayloads() {
        Element target = new Element("target");
        JDOMNodePointer targetPtr = new JDOMNodePointer(target, Locale.US);

        // Set value from another Element
        Element sourceElem = new Element("src");
        sourceElem.addContent(new Element("sub1"));
        sourceElem.addContent(new Text("txt"));
        targetPtr.setValue(sourceElem);
        assertEquals(2, target.getContent().size());

        // Set value from Document
        Document sourceDoc = new Document();
        Element docRoot = new Element("docRoot");
        docRoot.addContent(new Element("docChild"));
        sourceDoc.setRootElement(docRoot);
        targetPtr.setValue(sourceDoc);
        assertEquals(1, target.getContent().size());
        assertEquals("docChild", ((Element) target.getContent().get(0)).getName());

        // Set value from ProcessingInstruction
        ProcessingInstruction pi = new ProcessingInstruction("testPI", "payload");
        targetPtr.setValue(pi);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof ProcessingInstruction);

        // Set value from Comment
        Comment comment = new Comment("a comment");
        targetPtr.setValue(comment);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Comment);

        // Set value from Text
        Text text = new Text("plain text");
        targetPtr.setValue(text);
        assertEquals(1, target.getContent().size());
        assertEquals("plain text", ((Text) target.getContent().get(0)).getText());

        // Set value from primitive / String
        targetPtr.setValue("direct string");
        assertEquals(1, target.getContent().size());
        assertEquals("direct string", ((Text) target.getContent().get(0)).getText());

        // Set value empty string clears content
        targetPtr.setValue("");
        assertEquals(0, target.getContent().size());
    }

    @Test(timeout = 4000)
    public void testNamespaceResolutionAndIterators() {
        Namespace nsX = Namespace.getNamespace("x", "http://example.com/nsx");
        Element root = new Element("root", nsX);
        root.addNamespaceDeclaration(Namespace.getNamespace("y", "http://example.com/nsy"));
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);

        assertEquals("http://example.com/nsx", rootPtr.getNamespaceURI());
        assertEquals(JDOMNodePointer.XML_NAMESPACE_URI, rootPtr.getNamespaceURI("xml"));
        assertEquals("http://example.com/nsx", rootPtr.getNamespaceURI("x"));
        assertEquals("http://example.com/nsy", rootPtr.getNamespaceURI("y"));
        assertNull(rootPtr.getNamespaceURI("unknownPrefix"));

        Document doc = new Document(root);
        JDOMNodePointer docPtr = new JDOMNodePointer(doc, Locale.US);
        assertEquals("http://example.com/nsx", docPtr.getNamespaceURI("x"));

        assertNotNull(rootPtr.getNamespaceResolver());
        assertNotNull(rootPtr.namespaceIterator());
        assertNotNull(rootPtr.namespacePointer("x"));
        assertNotNull(rootPtr.attributeIterator(new QName("test")));
        assertNotNull(rootPtr.childIterator(null, false, null));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Node Testing
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeNameTestEvaluation() {
        Namespace ns = Namespace.getNamespace("ns", "http://test.org");
        Element elem = new Element("item", ns);
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.US);

        // Null test matches everything
        assertTrue(elemPtr.testNode(null));

        // Wildcard tests
        NodeNameTest wildcardNoPrefix = new NodeNameTest(new QName(null, "*"));
        assertTrue(elemPtr.testNode(wildcardNoPrefix));

        NodeNameTest wildcardWithPrefix = new NodeNameTest(new QName("ns", "*"), "http://test.org");
        assertTrue(elemPtr.testNode(wildcardWithPrefix));

        // Exact match
        NodeNameTest exactMatch = new NodeNameTest(new QName("ns", "item"), "http://test.org");
        assertTrue(elemPtr.testNode(exactMatch));

        // Mismatched name
        NodeNameTest wrongName = new NodeNameTest(new QName("ns", "other"), "http://test.org");
        assertFalse(elemPtr.testNode(wrongName));

        // Non-element node should return false for NodeNameTest
        Text text = new Text("data");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        assertFalse(textPtr.testNode(exactMatch));
    }

    @Test(timeout = 4000)
    public void testNodeTypeTestEvaluation() {
        Element elem = new Element("e");
        Document doc = new Document(new Element("r"));
        Text text = new Text("txt");
        CDATA cdata = new CDATA("cdata");
        Comment comment = new Comment("cmt");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(JDOMNodePointer.testNode(null, elem, nodeTest));
        assertTrue(JDOMNodePointer.testNode(null, doc, nodeTest));
        assertFalse(JDOMNodePointer.testNode(null, text, nodeTest));

        NodeTypeTest textTest = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(JDOMNodePointer.testNode(null, text, textTest));
        assertTrue(JDOMNodePointer.testNode(null, cdata, textTest));
        assertFalse(JDOMNodePointer.testNode(null, elem, textTest));

        NodeTypeTest commentTest = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(JDOMNodePointer.testNode(null, comment, commentTest));
        assertFalse(JDOMNodePointer.testNode(null, elem, commentTest));

        NodeTypeTest piTest = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(JDOMNodePointer.testNode(null, pi, piTest));
        assertFalse(JDOMNodePointer.testNode(null, elem, piTest));

        NodeTypeTest invalidType = new NodeTypeTest(999);
        assertFalse(JDOMNodePointer.testNode(null, elem, invalidType));

        ProcessingInstructionTest piTargetMatch = new ProcessingInstructionTest("target");
        assertTrue(JDOMNodePointer.testNode(null, pi, piTargetMatch));
        ProcessingInstructionTest piTargetMismatch = new ProcessingInstructionTest("other");
        assertFalse(JDOMNodePointer.testNode(null, pi, piTargetMismatch));
        assertFalse(JDOMNodePointer.testNode(null, elem, piTargetMatch));
    }

    @Test(timeout = 4000)
    public void testStaticGetPrefixAndLocalName() {
        Namespace ns = Namespace.getNamespace("pre", "http://test.org");
        Element elem = new Element("name", ns);
        Attribute attr = new Attribute("attrName", "val", ns);

        assertEquals("pre", JDOMNodePointer.getPrefix(elem));
        assertEquals("name", JDOMNodePointer.getLocalName(elem));
        assertEquals("pre", JDOMNodePointer.getPrefix(attr));
        assertEquals("attrName", JDOMNodePointer.getLocalName(attr));

        Element simpleElem = new Element("plain");
        Attribute simpleAttr = new Attribute("plainAttr", "val");
        assertNull(JDOMNodePointer.getPrefix(simpleElem));
        assertEquals("plain", JDOMNodePointer.getLocalName(simpleElem));
        assertNull(JDOMNodePointer.getPrefix(simpleAttr));
        assertEquals("plainAttr", JDOMNodePointer.getLocalName(simpleAttr));

        assertNull(JDOMNodePointer.getPrefix(new Text("txt")));
        assertNull(JDOMNodePointer.getLocalName(new Text("txt")));
    }

    @Test(timeout = 4000)
    public void testLanguageHandling() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);

        assertTrue(rootPtr.isLanguage("en"));
        assertTrue(rootPtr.isLanguage("en-US"));
        assertFalse(rootPtr.isLanguage("fr"));

        // Inherited language from parent
        assertTrue(childPtr.isLanguage("en"));

        // When no language attribute is present
        Element unkElem = new Element("unknown");
        JDOMNodePointer unkPtr = new JDOMNodePointer(unkElem, Locale.GERMANY);
        assertTrue(unkPtr.isLanguage("de"));
    }

    @Test(timeout = 4000)
    public void testAsPathScenariosAndEscaping() {
        // ID escaping with single and double quotes
        Element elem = new Element("elem");
        JDOMNodePointer idPtr = new JDOMNodePointer(elem, Locale.US, "foo'bar\"baz");
        assertEquals("id('foo&apos;bar&quot;baz')", idPtr.asPath());

        // Hierarchy path generation
        Element root = new Element("root");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        Text text1 = new Text("textVal");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        root.addContent(child1);
        root.addContent(child2);
        child2.addContent(text1);
        child2.addContent(pi);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer child2Ptr = new JDOMNodePointer(rootPtr, child2);
        JDOMNodePointer textPtr = new JDOMNodePointer(child2Ptr, text1);
        JDOMNodePointer piPtr = new JDOMNodePointer(child2Ptr, pi);

        assertEquals("/child[2]", child2Ptr.asPath());
        assertEquals("/child[2]/text()[1]", textPtr.asPath());
        assertEquals("/child[2]/processing-instruction('target')[1]", piPtr.asPath());

        // Namespace prefix in asPath
        Namespace ns = Namespace.getNamespace("my", "http://ns.example.com");
        Element nsElem = new Element("item", ns);
        root.addContent(nsElem);
        JDOMNodePointer nsElemPtr = new JDOMNodePointer(rootPtr, nsElem);
        rootPtr.getNamespaceResolver().registerNamespace("my", "http://ns.example.com");
        assertEquals("/my:item[1]", nsElemPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() {
        Element root = new Element("root");
        Attribute a1 = new Attribute("a1", "v1");
        Attribute a2 = new Attribute("a2", "v2");
        root.setAttribute(a1);
        root.setAttribute(a2);

        Element c1 = new Element("c1");
        Element c2 = new Element("c2");
        root.addContent(c1);
        root.addContent(c2);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        NodePointer pA1 = new JDOMAttributeIterator(rootPtr, new QName("a1")).getNodePointer();
        rootPtr.attributeIterator(new QName("a1")).setPosition(1);
        NodeIterator it = rootPtr.attributeIterator(new QName("a1"));
        it.setPosition(1);
        pA1 = it.getNodePointer();

        NodeIterator it2 = rootPtr.attributeIterator(new QName("a2"));
        it2.setPosition(1);
        NodePointer pA2 = it2.getNodePointer();

        JDOMNodePointer pC1 = new JDOMNodePointer(rootPtr, c1);
        JDOMNodePointer pC2 = new JDOMNodePointer(rootPtr, c2);

        // Same pointer comparison
        assertEquals(0, rootPtr.compareChildNodePointers(pC1, pC1));

        // Attribute vs Attribute
        assertEquals(-1, rootPtr.compareChildNodePointers(pA1, pA2));
        assertEquals(1, rootPtr.compareChildNodePointers(pA2, pA1));

        // Attribute vs Content child
        assertEquals(-1, rootPtr.compareChildNodePointers(pA1, pC1));
        assertEquals(1, rootPtr.compareChildNodePointers(c1 != null ? pC1 : null, pA1));

        // Content vs Content
        assertEquals(-1, rootPtr.compareChildNodePointers(pC1, pC2));
        assertEquals(1, rootPtr.compareChildNodePointers(pC2, pC1));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets following:: axis evaluation behavior documented in Defects4J:
     * JDOMModelTest::testAxisFollowing
     * "//location[2]/following::node()[2]" should resolve to the next following node (product[1])
     * rather than jumping/descending into product[1]/name[1].
     */
    @Test(timeout = 4000)
    public void testAxisFollowingDefectResolution() {
        Element vendor = new Element("vendor");
        Document doc = new Document(vendor);

        Element loc1 = new Element("location");
        loc1.addContent(new Element("address"));
        Element empCount = new Element("employeeCount");
        empCount.addContent(new Text("10"));
        loc1.addContent(empCount);
        vendor.addContent(loc1);

        Element loc2 = new Element("location");
        loc2.addContent(new Element("address"));
        vendor.addContent(loc2);

        Element prod = new Element("product");
        Element prodName = new Element("name");
        prod.addContent(prodName);
        vendor.addContent(prod);

        JXPathContext context = JXPathContext.newContext(doc);

        Pointer pFollowing = context.getPointer("//location[2]/following::node()[2]");
        assertNotNull("Following pointer must not be null", pFollowing);
        String asPath = pFollowing.asPath();
        assertTrue("Expected following node path to end at product[1], but got: " + asPath,
                asPath.endsWith("/product[1]") || asPath.endsWith("/product[1]/"));
    }

    /**
     * Targets preceding:: axis evaluation behavior documented in Defects4J:
     * JDOMModelTest::testAxisPreceding
     * "//location[2]/preceding::node()[3]" ordering and proper leaf node selection.
     */
    @Test(timeout = 4000)
    public void testAxisPrecedingDefectResolution() {
        Element vendor = new Element("vendor");
        Document doc = new Document(vendor);

        Element loc1 = new Element("location");
        Element address = new Element("address");
        loc1.addContent(address);
        Element empCount = new Element("employeeCount");
        empCount.addContent(new Text("10"));
        loc1.addContent(empCount);
        vendor.addContent(loc1);

        Element loc2 = new Element("location");
        loc2.addContent(new Element("address"));
        vendor.addContent(loc2);

        JXPathContext context = JXPathContext.newContext(doc);

        Pointer pPreceding = context.getPointer("//location[2]/preceding::node()[3]");
        assertNotNull("Preceding pointer must not be null", pPreceding);
        String asPath = pPreceding.asPath();
        assertTrue("Expected preceding node to point to employeeCount text node, but got: " + asPath,
                asPath.contains("employeeCount[1]/text()[1]"));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testCompareChildNodePointersOnNonElementThrowsException() {
        Text text = new Text("leaf");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.US);
        JDOMNodePointer dummyChild1 = new JDOMNodePointer(new Text("c1"), Locale.US);
        JDOMNodePointer dummyChild2 = new JDOMNodePointer(new Text("c2"), Locale.US);

        textPtr.compareChildNodePointers(dummyChild1, dummyChild2);
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootNodeThrowsException() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        rootPtr.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveChildNodeSuccess() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.US);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);

        assertEquals(1, root.getContent().size());
        childPtr.remove();
        assertEquals(0, root.getContent().size());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeWithUnknownPrefixThrowsException() {
        Element elem = new Element("elem");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.US);
        JXPathContext context = JXPathContext.newContext(elem);

        elemPtr.createAttribute(context, new QName("unknownPrefix", "attr"));
    }

    @Test(timeout = 4000)
    public void testCreateAttributeSuccess() {
        Element elem = new Element("elem");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.US);
        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer attrPtr = elemPtr.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attrPtr);
        assertEquals("simpleAttr", ((Attribute) attrPtr.getBaseValue()).getName());

        elemPtr.getNamespaceResolver().registerNamespace("foo", "http://foo.org");
        NodePointer nsAttrPtr = elemPtr.createAttribute(context, new QName("foo", "nsAttr"));
        assertNotNull(nsAttrPtr);
        assertEquals("nsAttr", ((Attribute) nsAttrPtr.getBaseValue()).getName());
        assertEquals("http://foo.org", ((Attribute) nsAttrPtr.getBaseValue()).getNamespaceURI());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateChildWithoutFactoryThrowsException() {
        Element elem = new Element("elem");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.US);
        JXPathContext context = JXPathContext.newContext(elem);

        elemPtr.createChild(context, new QName("child"), 0);
    }

    @Test(timeout = 4000)
    public void testCreateChildWithFactory() {
        Element elem = new Element("elem");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.US);
        JXPathContext context = JXPathContext.newContext(elem);

        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object parentNode, String name, int index) {
                if (parentNode instanceof Element) {
                    ((Element) parentNode).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });

        NodePointer created = elemPtr.createChild(context, new QName("newChild"), 0, "childValue");
        assertNotNull(created);
        assertEquals(1, elem.getContent().size());
        assertEquals("newChild", ((Element) elem.getContent().get(0)).getName());
        assertEquals("childValue", created.getValue());
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildFactoryFailsToCreate() {
        Element elem = new Element("elem");
        JDOMNodePointer elemPtr = new JDOMNodePointer(elem, Locale.US);
        JXPathContext context = JXPathContext.newContext(elem);

        context.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext ctx, Pointer parent, Object parentNode, String name, int index) {
                return false; // Intentionally fail
            }
        });

        elemPtr.createChild(context, new QName("failingChild"), 0);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Element elem1 = new Element("test");
        Element elem2 = new Element("test");

        JDOMNodePointer ptr1A = new JDOMNodePointer(elem1, Locale.US);
        JDOMNodePointer ptr1B = new JDOMNodePointer(elem1, Locale.US);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.US);

        // Reflexive
        assertTrue(ptr1A.equals(ptr1A));

        // Symmetric
        assertTrue(ptr1A.equals(ptr1B));
        assertTrue(ptr1B.equals(ptr1A));
        assertEquals(ptr1A.hashCode(), ptr1B.hashCode());

        // Distinct node identity
        assertFalse(ptr1A.equals(ptr2));
        assertFalse(ptr1A.equals(null));
        assertFalse(ptr1A.equals("NotANodePointer"));

        // Hash code consistency with System.identityHashCode
        assertEquals(System.identityHashCode(elem1), ptr1A.hashCode());
    }
}