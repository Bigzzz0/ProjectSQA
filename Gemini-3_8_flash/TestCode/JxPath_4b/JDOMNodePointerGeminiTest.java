package org.apache.commons.jxpath.ri.model.jdom;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer
 *
 * Targeted Decision Branches & Conditions:
 * - Constructors: (node, locale), (node, locale, id), (parent, node)
 * - Iterators: childIterator, attributeIterator, namespaceIterator, namespacePointer
 * - Namespace URI Resolution:
 *   * getNamespaceURI(): Element (with/without namespace, empty string fallback) vs non-Element
 *   * getNamespaceURI(prefix): Document (has root / prefix found / not found) vs Element vs other nodes
 * - Child Node Pointer Comparison (compareChildNodePointers):
 *   * node1 == node2 (identity)
 *   * Attribute vs non-Attribute (-1), non-Attribute vs Attribute (+1)
 *   * Attribute vs Attribute (index search in Element attributes, found/not-found)
 *   * Non-Element parent runtime exception guard
 *   * Element children order comparison (found n1 first, found n2 first, neither)
 * - Node Metadata & Leaf Detection:
 *   * isLeaf(): Element (0 children vs >0 children), Document (0 vs >0), other node types
 *   * isCollection(): always false; getLength(): always 1; getBaseValue(), getImmediateNode()
 *   * getName(): Element (with prefix, empty prefix normalized to null, no prefix), ProcessingInstruction, other
 * - Value Getters and Setters (getValue, setValue, addContent):
 *   * getValue(): Element (getTextTrim), Comment (trim / null), Text, CDATA, ProcessingInstruction, other
 *   * setValue():
 *     - Text node: non-empty string setText vs null/empty string removeContent from parent
 *     - Element node: clear content; accept Element, Document, Text/CDATA, PI, Comment, String/Object
 *     - addContent(): handle list containing Element, Text, CDATA, PI, Comment
 * - Node Testing (testNode):
 *   * null test (true)
 *   * NodeNameTest: non-Element (false), wildcard without prefix (true), wildcard with prefix,
 *                   matching localName with matching/mismatched namespaceURI
 *   * NodeTypeTest: NODE_TYPE_NODE, NODE_TYPE_TEXT, NODE_TYPE_COMMENT, NODE_TYPE_PI, unknown
 *   * ProcessingInstructionTest: PI target matches vs mismatches, non-PI node
 * - Language Resolution (isLanguage, getLanguage):
 *   * Current node has xml:lang, parent has xml:lang, traversal through Text/CDATA/PI/Comment, fallback to super
 * - Child & Attribute Creation (createChild, createAttribute):
 *   * Missing factory exception, factory failure exception, success paths
 *   * createAttribute on non-Element (super), on Element with/without prefix, unknown prefix exception
 * - Removal (remove):
 *   * Root node (null parent) throws JXPathException, non-root removes from parent
 * - Path Generation (asPath):
 *   * With ID (escape single quotes and double quotes)
 *   * Relative to parent JDOMNodePointer: default namespace match vs prefixed vs unnamed node()
 *   * Relative position indexing: getRelativePositionByName, getRelativePositionOfElement,
 *                                  getRelativePositionOfTextNode, getRelativePositionOfPI
 * - Equality & Hashing:
 *   * equals: identity, non-pointer, same node, different node; hashCode: identityHashCode
 *
 * Ground Truth Defect Targeted:
 * - XMLSpaceTest::testPreserveJDOM / testNestedJDOM: JDOMNodePointer.getValue() trims whitespace
 *   unconditionally and fails to preserve xml:space="preserve" or accurately represent nested text.
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
import org.apache.commons.jxpath.ri.compiler.NodeTest;
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
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() {
        Element element = new Element("item");
        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);

        assertSame("getBaseValue should return underlying node", element, pointer.getBaseValue());
        assertSame("getImmediateNode should return underlying node", element, pointer.getImmediateNode());
        assertFalse("isCollection should always return false", pointer.isCollection());
        assertEquals("getLength should always return 1", 1, pointer.getLength());
        assertEquals("Root element path should be /", "/", pointer.asPath());
    }

    @Test(timeout = 4000)
    public void testLeafDetection() {
        Element emptyElem = new Element("empty");
        JDOMNodePointer elemPtr = new JDOMNodePointer(emptyElem, Locale.US);
        assertTrue("Element with no children should be leaf", elemPtr.isLeaf());

        Element parentElem = new Element("parent");
        parentElem.addContent(new Element("child"));
        JDOMNodePointer parentPtr = new JDOMNodePointer(parentElem, Locale.US);
        assertFalse("Element with children should not be leaf", parentPtr.isLeaf());

        Document emptyDoc = new Document();
        JDOMNodePointer docPtr = new JDOMNodePointer(emptyDoc, Locale.US);
        assertTrue("Document without content should be leaf", docPtr.isLeaf());

        Document docWithRoot = new Document(new Element("root"));
        JDOMNodePointer docWithRootPtr = new JDOMNodePointer(docWithRoot, Locale.US);
        assertFalse("Document with root element should not be leaf", docWithRootPtr.isLeaf());

        Text textNode = new Text("hello");
        JDOMNodePointer textPtr = new JDOMNodePointer(textNode, Locale.US);
        assertTrue("Text node should always be leaf", textPtr.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetNameVariations() {
        Namespace ns = Namespace.getNamespace("p", "http://example.com/ns");
        Element elemWithPrefix = new Element("test", ns);
        JDOMNodePointer ptr1 = new JDOMNodePointer(elemWithPrefix, Locale.ENGLISH);
        QName qName1 = ptr1.getName();
        assertEquals("Prefix should match", "p", qName1.getPrefix());
        assertEquals("Local name should match", "test", qName1.getName());

        Element elemNoPrefix = new Element("simple");
        JDOMNodePointer ptr2 = new JDOMNodePointer(elemNoPrefix, Locale.ENGLISH);
        QName qName2 = ptr2.getName();
        assertNull("Prefix should be null when empty", qName2.getPrefix());
        assertEquals("Local name should match", "simple", qName2.getName());

        ProcessingInstruction pi = new ProcessingInstruction("targetApp", "dataVal");
        JDOMNodePointer ptr3 = new JDOMNodePointer(pi, Locale.ENGLISH);
        QName qName3 = ptr3.getName();
        assertNull("PI prefix should be null", qName3.getPrefix());
        assertEquals("PI name should match target", "targetApp", qName3.getName());

        Text text = new Text("data");
        JDOMNodePointer ptr4 = new JDOMNodePointer(text, Locale.ENGLISH);
        QName qName4 = ptr4.getName();
        assertNull("Text prefix should be null", qName4.getPrefix());
        assertNull("Text local name should be null", qName4.getName());
    }

    @Test(timeout = 4000)
    public void testGetValuesAcrossNodeTypes() {
        Element elem = new Element("e");
        elem.setText("   element text   ");
        JDOMNodePointer pElem = new JDOMNodePointer(elem, Locale.ENGLISH);
        assertEquals("Element value should be trimmed text", "element text", pElem.getValue());

        Comment comment = new Comment("  comment info  ");
        JDOMNodePointer pComment = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("Comment value should be trimmed text", "comment info", pComment.getValue());

        Text text = new Text("  text data  ");
        JDOMNodePointer pText = new JDOMNodePointer(text, Locale.ENGLISH);
        assertEquals("Text value should be trimmed", "text data", pText.getValue());

        CDATA cdata = new CDATA("  cdata data  ");
        JDOMNodePointer pCdata = new JDOMNodePointer(cdata, Locale.ENGLISH);
        assertEquals("CDATA value should be trimmed", "cdata data", pCdata.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("app", "  pi data  ");
        JDOMNodePointer pPi = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("PI value should be trimmed", "pi data", pPi.getValue());

        Document doc = new Document(new Element("root"));
        JDOMNodePointer pDoc = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertNull("Document value should be null", pDoc.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElementWithVariousPayloads() {
        Element root = new Element("root");
        JDOMNodePointer ptr = new JDOMNodePointer(root, Locale.ENGLISH);

        // Set Element payload
        Element childElem = new Element("source");
        childElem.addContent(new Element("sub"));
        ptr.setValue(childElem);
        assertEquals("Element content should be copied", 1, root.getContent().size());
        assertTrue("Copied child should be an Element", root.getContent().get(0) instanceof Element);

        // Set Document payload
        Document docPayload = new Document(new Element("docRoot"));
        ptr.setValue(docPayload);
        assertEquals("Document content should be copied", 1, root.getContent().size());

        // Set Text payload
        ptr.setValue(new Text("raw text"));
        assertEquals("1 text node expected", 1, root.getContent().size());
        assertEquals("Text value should match", "raw text", ((Text) root.getContent().get(0)).getText());

        // Set ProcessingInstruction payload
        ptr.setValue(new ProcessingInstruction("piTarget", "piValue"));
        assertEquals(1, root.getContent().size());
        assertTrue("Content should be PI", root.getContent().get(0) instanceof ProcessingInstruction);

        // Set Comment payload
        ptr.setValue(new Comment("comment payload"));
        assertEquals(1, root.getContent().size());
        assertTrue("Content should be Comment", root.getContent().get(0) instanceof Comment);

        // Set String payload
        ptr.setValue("simple string");
        assertEquals("simple string", ((Text) root.getContent().get(0)).getText());

        // Set empty string converts to nothing added
        ptr.setValue("");
        assertEquals(0, root.getContent().size());
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextNode() {
        Element parent = new Element("parent");
        Text text = new Text("initial");
        parent.addContent(text);
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);

        textPtr.setValue("updated");
        assertEquals("Text should be updated", "updated", text.getText());

        textPtr.setValue("");
        assertEquals("Text node should be removed from parent when set to empty", 0, parent.getContent().size());
    }

    @Test(timeout = 4000)
    public void testIteratorsAndPointers() {
        Element parent = new Element("parent");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        parent.addContent(child1);
        parent.addContent(child2);
        parent.setAttribute("attr1", "val1");

        JDOMNodePointer pointer = new JDOMNodePointer(parent, Locale.ENGLISH);

        NodeIterator childIter = pointer.childIterator(null, false, null);
        assertNotNull("childIterator should not be null", childIter);
        assertTrue("Child iterator should position to 1", childIter.setPosition(1));
        assertSame(child1, childIter.getNodePointer().getBaseValue());
        assertTrue("Child iterator should position to 2", childIter.setPosition(2));
        assertSame(child2, childIter.getNodePointer().getBaseValue());

        NodeIterator attrIter = pointer.attributeIterator(new QName("attr1"));
        assertNotNull("attributeIterator should not be null", attrIter);
        assertTrue(attrIter.setPosition(1));
        assertEquals("val1", attrIter.getNodePointer().getValue());

        NodeIterator nsIter = pointer.namespaceIterator();
        assertNotNull("namespaceIterator should not be null", nsIter);

        NodePointer nsPtr = pointer.namespacePointer("xml");
        assertNotNull("namespacePointer should not be null", nsPtr);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNamespaceURIExtremes() {
        // Element with empty namespace URI vs non-empty
        Element elemEmptyNs = new Element("emptyNs", "", "");
        JDOMNodePointer ptrEmpty = new JDOMNodePointer(elemEmptyNs, Locale.ENGLISH);
        assertNull("Empty string NS should be normalized to null", ptrEmpty.getNamespaceURI());

        Element elemValidNs = new Element("validNs", "p", "http://test.org");
        JDOMNodePointer ptrValid = new JDOMNodePointer(elemValidNs, Locale.ENGLISH);
        assertEquals("http://test.org", ptrValid.getNamespaceURI());
        assertEquals("http://test.org", ptrValid.getNamespaceURI("p"));
        assertNull("Unknown prefix should return null", ptrValid.getNamespaceURI("unknown"));

        // Document namespace prefix lookup
        Document doc = new Document(elemValidNs);
        JDOMNodePointer ptrDoc = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://test.org", ptrDoc.getNamespaceURI("p"));
        assertNull(ptrDoc.getNamespaceURI("unknown"));

        // Non-Element, Non-Document node returns null
        Text text = new Text("data");
        JDOMNodePointer ptrText = new JDOMNodePointer(text, Locale.ENGLISH);
        assertNull(ptrText.getNamespaceURI());
        assertNull(ptrText.getNamespaceURI("p"));
    }

    @Test(timeout = 4000)
    public void testPrefixAndLocalNameExtraction() {
        Element elemNoPrefix = new Element("noPrefix");
        assertEquals("noPrefix", JDOMNodePointer.getLocalName(elemNoPrefix));
        assertNull(JDOMNodePointer.getPrefix(elemNoPrefix));

        Element elemPrefix = new Element("withPrefix", "pr", "http://uri");
        assertEquals("withPrefix", JDOMNodePointer.getLocalName(elemPrefix));
        assertEquals("pr", JDOMNodePointer.getPrefix(elemPrefix));

        Attribute attrNoPrefix = new Attribute("attr", "val");
        assertEquals("attr", JDOMNodePointer.getLocalName(attrNoPrefix));
        assertNull(JDOMNodePointer.getPrefix(attrNoPrefix));

        Attribute attrPrefix = new Attribute("attrP", "val", Namespace.getNamespace("ap", "http://attr.org"));
        assertEquals("attrP", JDOMNodePointer.getLocalName(attrPrefix));
        assertEquals("ap", JDOMNodePointer.getPrefix(attrPrefix));

        Text text = new Text("txt");
        assertNull(JDOMNodePointer.getLocalName(text));
        assertNull(JDOMNodePointer.getPrefix(text));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() {
        Element root = new Element("root");
        Attribute attr1 = new Attribute("a1", "v1");
        Attribute attr2 = new Attribute("a2", "v2");
        root.setAttribute(attr1);
        root.setAttribute(attr2);

        Element child1 = new Element("c1");
        Element child2 = new Element("c2");
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        NodePointer ptrA1 = new JDOMNodePointer(rootPtr, attr1);
        NodePointer ptrA2 = new JDOMNodePointer(rootPtr, attr2);
        NodePointer ptrC1 = new JDOMNodePointer(rootPtr, child1);
        NodePointer ptrC2 = new JDOMNodePointer(rootPtr, child2);

        // Same node
        assertEquals(0, rootPtr.compareChildNodePointers(ptrA1, ptrA1));
        assertEquals(0, rootPtr.compareChildNodePointers(ptrC1, ptrC1));

        // Attribute vs non-Attribute
        assertEquals(-1, rootPtr.compareChildNodePointers(ptrA1, ptrC1));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrC1, ptrA1));

        // Attribute vs Attribute
        assertEquals(-1, rootPtr.compareChildNodePointers(ptrA1, ptrA2));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrA2, ptrA1));

        // Child vs Child
        assertEquals(-1, rootPtr.compareChildNodePointers(ptrC1, ptrC2));
        assertEquals(1, rootPtr.compareChildNodePointers(ptrC2, ptrC1));
    }

    @Test(timeout = 4000)
    public void testLanguageResolutionHierarchy() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);

        Element child = new Element("child");
        root.addContent(child);

        Text text = new Text("message");
        child.addContent(text);

        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertTrue("Text should inherit xml:lang en", textPtr.isLanguage("en"));
        assertTrue("Text should inherit xml:lang en-us case-insensitive", textPtr.isLanguage("EN-US"));
        assertFalse("Text should not match fr", textPtr.isLanguage("fr"));

        Element noLangElem = new Element("plain");
        JDOMNodePointer noLangPtr = new JDOMNodePointer(noLangElem, Locale.ENGLISH);
        // Falls back to pointer's Locale (Locale.ENGLISH)
        assertTrue(noLangPtr.isLanguage("en"));
    }

    @Test(timeout = 4000)
    public void testAsPathWithIdAndEscapes() {
        Element elem = new Element("test");
        JDOMNodePointer idPtr = new JDOMNodePointer(elem, Locale.ENGLISH, "a'b\"c");
        assertEquals("id('a&apos;b&quot;c')", idPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathStructuralFormatting() {
        Element root = new Element("root");
        Element child1 = new Element("item");
        Element child2 = new Element("item");
        Text textNode = new Text("hello");
        ProcessingInstruction pi = new ProcessingInstruction("action", "run");

        root.addContent(child1);
        root.addContent(child2);
        root.addContent(textNode);
        root.addContent(pi);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer child1Ptr = new JDOMNodePointer(rootPtr, child1);
        JDOMNodePointer child2Ptr = new JDOMNodePointer(rootPtr, child2);
        JDOMNodePointer textPtr = new JDOMNodePointer(rootPtr, textNode);
        JDOMNodePointer piPtr = new JDOMNodePointer(rootPtr, pi);

        assertEquals("/item[1]", child1Ptr.asPath());
        assertEquals("/item[2]", child2Ptr.asPath());
        assertEquals("/text()[1]", textPtr.asPath());
        assertEquals("/processing-instruction('action')[1]", piPtr.asPath());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETED DEFECT TEST:
     * Ground Truth: XMLSpaceTest::testPreserveJDOM
     * When xml:space="preserve" is specified on an element, its whitespace should
     * be preserved intact by getValue(). Defective implementation unconditionally
     * invokes ((Element) node).getTextTrim(), discarding whitespace.
     */
    @Test(timeout = 4000)
    public void testPreserveJDOMDefectTarget() {
        Element element = new Element("unformatted");
        element.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
        element.setText(" foo ");

        JDOMNodePointer pointer = new JDOMNodePointer(element, Locale.ENGLISH);
        // On fixed/expected implementation: " foo " whitespace is preserved.
        // On defective version: returns "foo", exposing junit.framework.AssertionFailedError
        assertEquals("Expected whitespace to be preserved when xml:space='preserve'", " foo ", pointer.getValue());
    }

    /**
     * TARGETED DEFECT TEST:
     * Ground Truth: XMLSpaceTest::testNestedJDOM
     * When nested elements are present, standard XPath string-value is the concatenation
     * of all descendant text nodes. JDOMNodePointer.getValue() delegates directly to
     * Element.getTextTrim(), losing interleaved or nested text structure.
     */
    @Test(timeout = 4000)
    public void testNestedJDOMDefectTarget() {
        Element parent = new Element("unformatted");
        parent.addContent(new Text("foo;"));
        Element nested = new Element("nested");
        parent.addContent(nested);
        parent.addContent(new Text("bar; baz "));

        JDOMNodePointer pointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        assertEquals("foo;bar; baz ", pointer.getValue());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testCompareChildNodePointersOnNonElementThrowsException() {
        Text parent = new Text("data");
        JDOMNodePointer parentPtr = new JDOMNodePointer(parent, Locale.ENGLISH);
        NodePointer p1 = new JDOMNodePointer(parentPtr, new Text("c1"));
        NodePointer p2 = new JDOMNodePointer(parentPtr, new Text("c2"));
        parentPtr.compareChildNodePointers(p1, p2);
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootNodeThrowsException() {
        Element root = new Element("root");
        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        rootPtr.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveChildNodeSuccess() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPtr = new JDOMNodePointer(root, Locale.ENGLISH);
        JDOMNodePointer childPtr = new JDOMNodePointer(rootPtr, child);

        assertEquals("Root should have 1 child before removal", 1, root.getContent().size());
        childPtr.remove();
        assertEquals("Child should be removed from parent", 0, root.getContent().size());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeWithUnknownPrefixThrowsException() {
        Element elem = new Element("item");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);
        ptr.createAttribute(context, new QName("unknownPrefix", "attrName"));
    }

    @Test(timeout = 4000)
    public void testCreateAttributeSuccess() {
        Element elem = new Element("item");
        Namespace ns = Namespace.getNamespace("p", "http://ns.org");
        elem.addNamespaceDeclaration(ns);

        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer attr1 = ptr.createAttribute(context, new QName("simpleAttr"));
        assertNotNull(attr1);
        assertEquals("", attr1.getValue());

        NodePointer attr2 = ptr.createAttribute(context, new QName("p", "prefixedAttr"));
        assertNotNull(attr2);
        assertEquals("", attr2.getValue());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateChildWithoutFactoryThrowsException() {
        Element elem = new Element("item");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);
        ptr.createChild(context, new QName("subItem"), 0);
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildWhenFactoryReturnsFalseThrowsException() {
        Element elem = new Element("item");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                return false;
            }
        });
        ptr.createChild(context, new QName("subItem"), 0);
    }

    @Test(timeout = 4000)
    public void testCreateChildSuccess() {
        Element elem = new Element("item");
        JDOMNodePointer ptr = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer parent, Object node, String name, int index) {
                if ("subItem".equals(name) && node instanceof Element) {
                    ((Element) node).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });
        NodePointer newChild = ptr.createChild(context, new QName("subItem"), 0, "childValue");
        assertNotNull(newChild);
        assertEquals("childValue", newChild.getValue());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Element elem1 = new Element("test");
        Element elem2 = new Element("test");

        JDOMNodePointer p1 = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer p1Dup = new JDOMNodePointer(elem1, Locale.FRENCH);
        JDOMNodePointer p2 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        assertTrue("Reflexive", p1.equals(p1));
        assertTrue("Symmetric with same underlying node", p1.equals(p1Dup));
        assertTrue("Symmetric reverse", p1Dup.equals(p1));
        assertEquals("HashCode must be identical for same node", p1.hashCode(), p1Dup.hashCode());

        assertFalse("Different underlying nodes must not be equal", p1.equals(p2));
        assertFalse("Comparison with null must return false", p1.equals(null));
        assertFalse("Comparison with foreign object must return false", p1.equals("string"));
    }

    @Test(timeout = 4000)
    public void testNodeTestsComprehensive() {
        Element element = new Element("tag", "p", "http://test.org");
        JDOMNodePointer elemPtr = new JDOMNodePointer(element, Locale.ENGLISH);

        // Null test matches everything
        assertTrue(elemPtr.testNode(null));

        // NodeNameTest wildcard
        assertTrue(elemPtr.testNode(new NodeNameTest(new QName(null, "*"))));
        // NodeNameTest match
        assertTrue(elemPtr.testNode(new NodeNameTest(new QName("p", "tag"), "http://test.org")));
        // NodeNameTest mismatch
        assertFalse(elemPtr.testNode(new NodeNameTest(new QName("p", "other"), "http://test.org")));
        assertFalse(elemPtr.testNode(new NodeNameTest(new QName("p", "tag"), "http://different.org")));

        // NodeTypeTest
        assertTrue(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(elemPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Text text = new Text("content");
        JDOMNodePointer textPtr = new JDOMNodePointer(text, Locale.ENGLISH);
        assertTrue(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(textPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));

        Comment comment = new Comment("info");
        JDOMNodePointer commentPtr = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertTrue(commentPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(commentPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        ProcessingInstruction pi = new ProcessingInstruction("app", "data");
        JDOMNodePointer piPtr = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertTrue(piPtr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertTrue(piPtr.testNode(new ProcessingInstructionTest("app")));
        assertFalse(piPtr.testNode(new ProcessingInstructionTest("other")));
    }
}