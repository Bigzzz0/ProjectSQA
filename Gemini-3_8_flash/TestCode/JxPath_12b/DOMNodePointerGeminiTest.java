/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.model.dom.DOMNodePointer
 *
 * Core Decision Branches & Conditions Targeted:
 * 1. testNode(Node, NodeTest):
 *    - test == null (true)
 *    - NodeNameTest: node type ELEMENT vs non-ELEMENT; wildcard with null vs non-null prefix;
 *      name match + namespace URI match/mismatch via equalStrings; wildcard true/false.
 *    - NodeTypeTest: NODE_TYPE_NODE (Element/Doc vs other), NODE_TYPE_TEXT (Text/CDATA vs other),
 *      NODE_TYPE_COMMENT (Comment vs other), NODE_TYPE_PI (PI vs other), unrecognized type.
 *    - ProcessingInstructionTest: PI node with matching target, PI node with non-matching target, non-PI node.
 * 2. equalStrings(String, String):
 *    - s1 == s2 (null == null, same ref); s1 null vs s2 not null; trim equivalence.
 * 3. getName(): ELEMENT (prefix + localName), PI (target), other node types.
 * 4. getNamespaceURI(String):
 *    - prefix null or empty -> getDefaultNamespaceURI()
 *    - prefix "xml" -> XML_NAMESPACE_URI; "xmlns" -> XMLNS_NAMESPACE_URI
 *    - cached namespace lookup vs iterative DOM tree resolution; UNKNOWN_NAMESPACE caching.
 * 5. getDefaultNamespaceURI():
 *    - Document vs Element traversal; 'xmlns' attribute found vs not found; empty string normalized to null.
 * 6. setValue(Object):
 *    - Node is Text/CDATA: non-empty string sets value; null/empty string removes node from parent.
 *    - Node is Element: removes existing children; value is Element/Document (appends cloned children);
 *      value is other Node (clones and appends); value is String (appends text node or empty).
 * 7. createChild(JXPathContext, QName, int) & createAttribute(JXPathContext, QName):
 *    - Success vs failure (JXPathAbstractFactoryException); WHOLE_COLLECTION index normalization;
 *      factory missing (JXPathException); unknown prefix in createAttribute (JXPathException); non-element node.
 * 8. remove(): Root node with null parent throws JXPathException; child node removed cleanly.
 * 9. asPath():
 *    - id != null (with escape for single and double quotes);
 *    - Element with DOMNodePointer parent: nsURI null, nsURI with prefix in resolver, nsURI without prefix ("node()");
 *    - Relative positioning: by name, of element, of text/CDATA, of PI; Document node (empty).
 * 10. compareChildNodePointers():
 *    - same pointer; attribute vs non-attribute; attribute vs attribute; sibling order traversal.
 * 11. Defects4J Known Defect (ExternalXMLNamespaceTest::testElementDOM):
 *    - XPath resolution of externally registered namespace (/ElementA/B:ElementB) when DOM does not declare
 *      xmlns:B inline, exposing DOMNodePointer's testNode and getNamespaceURI handling.
 */
package org.apache.commons.jxpath.ri.model.dom;

import java.io.StringReader;
import java.util.Locale;
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
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;

public class DOMNodePointerGeminiTest {

    private Document parseXml(String xml, boolean namespaceAware) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private Document newDocument() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() throws Exception {
        Document doc = parseXml("<root attr=\"val\">text</root>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.ENGLISH);

        assertSame(root, pointer.getBaseValue());
        assertSame(root, pointer.getImmediateNode());
        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertFalse(pointer.isLeaf());

        Node textChild = root.getFirstChild();
        DOMNodePointer textPointer = new DOMNodePointer(pointer, textChild);
        assertTrue(textPointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetNameForVariousNodeTypes() throws Exception {
        Document doc = parseXml("<ns:root xmlns:ns=\"http://foo\">hello<!--comment--></ns:root>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        QName rootName = rootPtr.getName();
        assertEquals("ns", rootName.getPrefix());
        assertEquals("root", rootName.getName());

        ProcessingInstruction pi = doc.createProcessingInstruction("targetPI", "data");
        DOMNodePointer piPtr = new DOMNodePointer(pi, Locale.ENGLISH);
        QName piName = piPtr.getName();
        assertNull(piName.getPrefix());
        assertEquals("targetPI", piName.getName());

        Comment comment = (Comment) root.getChildNodes().item(1);
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        QName commentName = commentPtr.getName();
        assertNull(commentName.getPrefix());
        assertNull(commentName.getName());
    }

    @Test(timeout = 4000)
    public void testGetValueAndXmlSpacePreserve() throws Exception {
        Document doc = parseXml(
                "<root>"
                + "  <normal>   padded   </normal>"
                + "  <preserved xml:space=\"preserve\">   padded   </preserved>"
                + "  <!--  comment text  -->"
                + "</root>", false);
        Element root = doc.getDocumentElement();

        Element normal = (Element) root.getElementsByTagName("normal").item(0);
        DOMNodePointer normalPtr = new DOMNodePointer(normal, Locale.ENGLISH);
        assertEquals("padded", normalPtr.getValue());

        Element preserved = (Element) root.getElementsByTagName("preserved").item(0);
        DOMNodePointer preservedPtr = new DOMNodePointer(preserved, Locale.ENGLISH);
        assertEquals("   padded   ", preservedPtr.getValue());

        Node comment = root.getChildNodes().item(5);
        DOMNodePointer commentPtr = new DOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("comment text", commentPtr.getValue());
    }

    @Test(timeout = 4000)
    public void testIteratorsCreation() throws Exception {
        Document doc = parseXml("<root a=\"1\" b=\"2\"><child/></root>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        NodeIterator childIt = rootPtr.childIterator(null, false, null);
        assertNotNull(childIt);

        NodeIterator attrIt = rootPtr.attributeIterator(new QName("a"));
        assertNotNull(attrIt);

        NodeIterator nsIt = rootPtr.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPtr = rootPtr.namespacePointer("xml");
        assertNotNull(nsPtr);
        assertEquals("xml", nsPtr.getName().getName());
    }

    @Test(timeout = 4000)
    public void testLanguageResolution() throws Exception {
        Document doc = parseXml(
                "<root xml:lang=\"en-US\">"
                + "  <child/>"
                + "</root>", false);
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getElementsByTagName("child").item(0);

        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.US);
        assertTrue(childPtr.isLanguage("en"));
        assertTrue(childPtr.isLanguage("en-US"));
        assertFalse(childPtr.isLanguage("fr"));

        Document docNoLang = parseXml("<root><child/></root>", false);
        DOMNodePointer noLangPtr = new DOMNodePointer(docNoLang.getDocumentElement(), Locale.GERMAN);
        assertTrue(noLangPtr.isLanguage("de"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Node Testing
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeWithNullTest() throws Exception {
        Document doc = newDocument();
        Element el = doc.createElement("test");
        assertTrue(DOMNodePointer.testNode(el, null));
        DOMNodePointer ptr = new DOMNodePointer(el, Locale.ENGLISH);
        assertTrue(ptr.testNode(null));
    }

    @Test(timeout = 4000)
    public void testNodeNameTestEvaluation() throws Exception {
        Document doc = parseXml("<ns:elem xmlns:ns=\"http://uri\">content</ns:elem>", false);
        Element elem = doc.getDocumentElement();

        // 1. Non-element node returns false
        Text text = (Text) elem.getFirstChild();
        assertFalse(DOMNodePointer.testNode(text, new NodeNameTest(new QName("elem"))));

        // 2. Wildcard with null prefix returns true
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName(null, "*"))));

        // 3. Exact match name and namespace
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "elem"), "http://uri")));

        // 4. Wildcard with namespace match
        assertTrue(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "*"), "http://uri")));

        // 5. Namespace mismatch
        assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "elem"), "http://other")));

        // 6. Name mismatch
        assertFalse(DOMNodePointer.testNode(elem, new NodeNameTest(new QName("ns", "other"), "http://uri")));
    }

    @Test(timeout = 4000)
    public void testNodeTypeTestEvaluation() throws Exception {
        Document doc = newDocument();
        Element elem = doc.createElement("e");
        Text text = doc.createTextNode("txt");
        CDATASection cdata = doc.createCDATASection("cdata");
        Comment comment = doc.createComment("comm");
        ProcessingInstruction pi = doc.createProcessingInstruction("pi", "data");

        NodeTypeTest testNode = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(elem, testNode));
        assertTrue(DOMNodePointer.testNode(doc, testNode));
        assertFalse(DOMNodePointer.testNode(text, testNode));

        NodeTypeTest testText = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(text, testText));
        assertTrue(DOMNodePointer.testNode(cdata, testText));
        assertFalse(DOMNodePointer.testNode(elem, testText));

        NodeTypeTest testComment = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(comment, testComment));
        assertFalse(DOMNodePointer.testNode(elem, testComment));

        NodeTypeTest testPI = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(pi, testPI));
        assertFalse(DOMNodePointer.testNode(elem, testPI));

        NodeTypeTest unknownType = new NodeTypeTest(999);
        assertFalse(DOMNodePointer.testNode(elem, unknownType));
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionTestEvaluation() throws Exception {
        Document doc = newDocument();
        ProcessingInstruction pi = doc.createProcessingInstruction("targetA", "dataA");
        Element elem = doc.createElement("elem");

        ProcessingInstructionTest matchTest = new ProcessingInstructionTest("targetA");
        ProcessingInstructionTest mismatchTest = new ProcessingInstructionTest("targetB");

        assertTrue(DOMNodePointer.testNode(pi, matchTest));
        assertFalse(DOMNodePointer.testNode(pi, mismatchTest));
        assertFalse(DOMNodePointer.testNode(elem, matchTest));
    }

    @Test(timeout = 4000)
    public void testCustomNodeTestReturnsFalse() throws Exception {
        Document doc = newDocument();
        Element elem = doc.createElement("e");
        NodeTest customTest = new NodeTest() {};
        assertFalse(DOMNodePointer.testNode(elem, customTest));
    }

    @Test(timeout = 4000)
    public void testNamespaceURILookupAndCaching() throws Exception {
        Document doc = parseXml(
                "<root xmlns=\"http://default\" xmlns:pre=\"http://prefix\">"
                + "  <child xmlns:childPre=\"http://child\"/>"
                + "</root>", false);
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getElementsByTagName("child").item(0);
        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);

        assertEquals("http://default", childPtr.getNamespaceURI(null));
        assertEquals("http://default", childPtr.getNamespaceURI(""));
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, childPtr.getNamespaceURI("xml"));
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, childPtr.getNamespaceURI("xmlns"));
        assertEquals("http://child", childPtr.getNamespaceURI("childPre"));
        assertEquals("http://prefix", childPtr.getNamespaceURI("pre"));

        // Repeated lookup to exercise internal namespace cache
        assertEquals("http://prefix", childPtr.getNamespaceURI("pre"));

        // Unknown namespace
        assertNull(childPtr.getNamespaceURI("unknown"));
        assertNull(childPtr.getNamespaceURI("unknown")); // Cached UNKNOWN_NAMESPACE
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIFromDocumentNode() throws Exception {
        Document doc = parseXml("<root xmlns=\"http://default\" xmlns:a=\"http://a\"/>", false);
        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);

        assertEquals("http://default", docPtr.getDefaultNamespaceURI());
        assertEquals("http://a", docPtr.getNamespaceURI("a"));
    }

    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURIWhenNoneDeclared() throws Exception {
        Document doc = parseXml("<root><child/></root>", false);
        DOMNodePointer ptr = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        assertNull(ptr.getDefaultNamespaceURI());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * Target Defect Test:
     * Ground Truth: ExternalXMLNamespaceTest::testElementDOM
     * Failure symptom: org.apache.commons.jxpath.JXPathNotFoundException: No value for xpath: /ElementA/B:ElementB
     *
     * Validates that an externally registered namespace prefix on JXPathContext
     * matches the element when evaluated via xpath /ElementA/B:ElementB.
     */
    @Test(timeout = 4000)
    public void testExternalXMLNamespaceDefectDirect() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        Document doc = dbf.newDocumentBuilder().parse(
                new InputSource(new StringReader("<ElementA><B:ElementB>defectRevealed</B:ElementB></ElementA>")));

        JXPathContext context = JXPathContext.newContext(doc);
        context.registerNamespace("B", "http://external-uri");

        Object val = context.getValue("/ElementA/B:ElementB");
        assertEquals("defectRevealed", val);
    }

    @Test(timeout = 4000)
    public void testExternalNamespaceNodeNameTestDefect() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        Document doc = dbf.newDocumentBuilder().parse(
                new InputSource(new StringReader("<ElementA><B:ElementB>value</B:ElementB></ElementA>")));
        Element elementB = (Element) doc.getDocumentElement().getFirstChild();

        NodeNameTest test = new NodeNameTest(new QName("B", "ElementB"), "http://external-uri");
        boolean matched = DOMNodePointer.testNode(elementB, test);
        assertTrue("testNode should resolve external namespace correctly for B:ElementB", matched);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testRemoveRootNodeThrowsException() throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.ENGLISH);
        ptr.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveChildNodeSuccess() throws Exception {
        Document doc = parseXml("<root><child/></root>", false);
        Element root = doc.getDocumentElement();
        Element child = (Element) root.getFirstChild();
        DOMNodePointer childPtr = new DOMNodePointer(child, Locale.ENGLISH);

        childPtr.remove();
        assertEquals(0, root.getChildNodes().getLength());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateChildWithoutFactoryThrowsException() throws Exception {
        Document doc = parseXml("<root/>", false);
        DOMNodePointer ptr = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ptr.createChild(ctx, new QName("newChild"), 0);
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreateChildFactoryReturnsFalseThrowsException() throws Exception {
        Document doc = parseXml("<root/>", false);
        DOMNodePointer ptr = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ctx.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                return false;
            }
        });
        ptr.createChild(ctx, new QName("failingChild"), 0);
    }

    @Test(timeout = 4000)
    public void testCreateChildFactorySuccess() throws Exception {
        Document doc = parseXml("<root/>", false);
        DOMNodePointer ptr = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ctx.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
                Element parentElem = (Element) parent;
                Element child = parentElem.getOwnerDocument().createElement(name);
                parentElem.appendChild(child);
                return true;
            }
        });

        NodePointer newPtr = ptr.createChild(ctx, new QName("newChild"), NodePointer.WHOLE_COLLECTION);
        assertNotNull(newPtr);
        assertEquals("newChild", newPtr.getName().getName());

        NodePointer withValuePtr = ptr.createChild(ctx, new QName("valChild"), 0, "childValue");
        assertNotNull(withValuePtr);
        assertEquals("childValue", withValuePtr.getValue());
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreateAttributeWithUnknownPrefixThrowsException() throws Exception {
        Document doc = parseXml("<root/>", false);
        DOMNodePointer ptr = new DOMNodePointer(doc.getDocumentElement(), Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(doc);
        ptr.createAttribute(ctx, new QName("unknown", "attr"));
    }

    @Test(timeout = 4000)
    public void testCreateAttributeSuccess() throws Exception {
        Document doc = parseXml("<root xmlns:ns=\"http://declared\"/>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer ptr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(doc);

        NodePointer attr1 = ptr.createAttribute(ctx, new QName("simpleAttr"));
        assertNotNull(attr1);
        assertTrue(root.hasAttribute("simpleAttr"));

        NodePointer attr2 = ptr.createAttribute(ctx, new QName("ns", "prefixedAttr"));
        assertNotNull(attr2);
        assertTrue(root.hasAttribute("ns:prefixedAttr") || root.hasAttributeNS("http://declared", "prefixedAttr"));
    }

    @Test(timeout = 4000)
    public void testSetValueOnTextAndCDATA() throws Exception {
        Document doc = parseXml("<root>initialText<![CDATA[initialCDATA]]></root>", false);
        Element root = doc.getDocumentElement();
        Text textNode = (Text) root.getFirstChild();
        CDATASection cdataNode = (CDATASection) root.getLastChild();

        DOMNodePointer textPtr = new DOMNodePointer(textNode, Locale.ENGLISH);
        textPtr.setValue("updatedText");
        assertEquals("updatedText", textNode.getNodeValue());

        DOMNodePointer cdataPtr = new DOMNodePointer(cdataNode, Locale.ENGLISH);
        cdataPtr.setValue("updatedCDATA");
        assertEquals("updatedCDATA", cdataNode.getNodeValue());

        // Setting empty string removes the text node
        textPtr.setValue("");
        assertNull(textNode.getParentNode());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElement() throws Exception {
        Document doc = parseXml("<root><oldChild/></root>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        // 1. Set string value
        rootPtr.setValue("newTextContent");
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("newTextContent", root.getFirstChild().getNodeValue());

        // 2. Set Element value (children copied)
        Document donorDoc = parseXml("<donor><childA/><childB/></donor>", false);
        rootPtr.setValue(donorDoc.getDocumentElement());
        assertEquals(2, root.getChildNodes().getLength());
        assertEquals("childA", root.getChildNodes().item(0).getNodeName());
        assertEquals("childB", root.getChildNodes().item(1).getNodeName());

        // 3. Set Document value (document children copied)
        Document donorDoc2 = parseXml("<donorDoc><docChild/></donorDoc>", false);
        rootPtr.setValue(donorDoc2);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("donorDoc", root.getChildNodes().item(0).getNodeName());

        // 4. Set Comment value (single non-element/document node appended)
        Comment donorComment = doc.createComment("donorComment");
        rootPtr.setValue(donorComment);
        assertEquals(1, root.getChildNodes().getLength());
        assertTrue(root.getFirstChild() instanceof Comment);
        assertEquals("donorComment", ((Comment) root.getFirstChild()).getData());
    }

    // =========================================================================
    // Partition E: Path Construction & Structural Comparison
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsPathWithIdAndEscaping() throws Exception {
        Document doc = newDocument();
        Element elem = doc.createElement("e");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.ENGLISH, "item'with\"quotes");
        assertEquals("id('item&apos;with&quot;quotes')", ptr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathElementsAndPositions() throws Exception {
        Document doc = parseXml(
                "<root>"
                + "  <item/>"
                + "  <item/>"
                + "  <!--comment-->"
                + "  <?pi target?>"
                + "  some text"
                + "  <![CDATA[cdata]]>"
                + "</root>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        Element firstItem = (Element) root.getElementsByTagName("item").item(0);
        Element secondItem = (Element) root.getElementsByTagName("item").item(1);

        DOMNodePointer item1Ptr = new DOMNodePointer(rootPtr, firstItem);
        DOMNodePointer item2Ptr = new DOMNodePointer(rootPtr, secondItem);

        assertEquals("/item[1]", item1Ptr.asPath());
        assertEquals("/item[2]", item2Ptr.asPath());

        ProcessingInstruction pi = (ProcessingInstruction) root.getChildNodes().item(3);
        DOMNodePointer piPtr = new DOMNodePointer(rootPtr, pi);
        assertEquals("/processing-instruction('pi')[1]", piPtr.asPath());

        Node textNode = root.getChildNodes().item(4);
        DOMNodePointer textPtr = new DOMNodePointer(rootPtr, textNode);
        assertTrue(textPtr.asPath().startsWith("/text()["));

        DOMNodePointer docPtr = new DOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("", docPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathWithNamespaces() throws Exception {
        Document doc = parseXml(
                "<root xmlns:pre=\"http://foo\" xmlns:other=\"http://bar\">"
                + "  <pre:item/>"
                + "  <other:item/>"
                + "</root>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        Element preItem = (Element) root.getElementsByTagName("pre:item").item(0);
        DOMNodePointer itemPtr = new DOMNodePointer(rootPtr, preItem);

        // When resolver knows prefix
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("localPre", "http://foo");
        rootPtr.setNamespaceResolver(resolver);

        assertEquals("/localPre:item[1]", itemPtr.asPath());

        // When resolver does NOT know prefix -> defaults to node()[idx]
        NamespaceResolver emptyResolver = new NamespaceResolver();
        rootPtr.setNamespaceResolver(emptyResolver);
        assertEquals("/node()[1]", itemPtr.asPath());
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() throws Exception {
        Document doc = parseXml("<root id=\"rootId\"><child id=\"childId\"/></root>", false);
        Element root = doc.getDocumentElement();
        root.setIdAttribute("id", true);

        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);
        JXPathContext ctx = JXPathContext.newContext(doc);

        Pointer found = rootPtr.getPointerByID(ctx, "rootId");
        assertNotNull(found);
        assertTrue(found instanceof DOMNodePointer);
        assertEquals(root, found.getNode());

        Pointer notFound = rootPtr.getPointerByID(ctx, "nonExistent");
        assertNotNull(notFound);
        assertTrue(notFound instanceof NullPointer);
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() throws Exception {
        Document doc = parseXml("<root a=\"1\" b=\"2\"><child1/><child2/></root>", false);
        Element root = doc.getDocumentElement();
        DOMNodePointer rootPtr = new DOMNodePointer(root, Locale.ENGLISH);

        Node child1 = root.getElementsByTagName("child1").item(0);
        Node child2 = root.getElementsByTagName("child2").item(0);
        DOMNodePointer ptr1 = new DOMNodePointer(rootPtr, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(rootPtr, child2);

        // Identity comparison
        assertEquals(0, rootPtr.compareChildNodePointers(ptr1, ptr1));

        // Sibling order: child1 before child2
        assertEquals(-1, rootPtr.compareChildNodePointers(ptr1, ptr2));
        assertEquals(1, rootPtr.compareChildNodePointers(ptr2, ptr1));

        // Attribute vs Attribute
        Attr attrA = root.getAttributeNode("a");
        Attr attrB = root.getAttributeNode("b");
        DOMNodePointer attrPtrA = new DOMNodePointer(rootPtr, attrA);
        DOMNodePointer attrPtrB = new DOMNodePointer(rootPtr, attrB);
        int attrComp = rootPtr.compareChildNodePointers(attrPtrA, attrPtrB);
        assertTrue(attrComp == -1 || attrComp == 1);

        // Attribute vs Element (Attribute always comes before Element)
        assertEquals(-1, rootPtr.compareChildNodePointers(attrPtrA, ptr1));
        assertEquals(1, rootPtr.compareChildNodePointers(ptr1, attrPtrA));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() throws Exception {
        Document doc = newDocument();
        Element e1 = doc.createElement("e1");
        Element e2 = doc.createElement("e2");

        DOMNodePointer p1 = new DOMNodePointer(e1, Locale.ENGLISH);
        DOMNodePointer p1Same = new DOMNodePointer(e1, Locale.ENGLISH);
        DOMNodePointer p2 = new DOMNodePointer(e2, Locale.ENGLISH);

        assertEquals(p1, p1);
        assertEquals(p1, p1Same);
        assertNotEquals(p1, p2);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "someString");

        assertEquals(p1.hashCode(), p1Same.hashCode());
        assertEquals(System.identityHashCode(e1), p1.hashCode());
    }

    @Test(timeout = 4000)
    public void testStaticPrefixAndLocalNameUtility() throws Exception {
        Document doc = newDocument();

        // 1. Prefix and name from qualified DOM Level 1 string
        Element prefixed = doc.createElement("foo:bar");
        assertEquals("foo", DOMNodePointer.getPrefix(prefixed));
        assertEquals("bar", DOMNodePointer.getLocalName(prefixed));

        // 2. Unprefixed element
        Element plain = doc.createElement("plain");
        assertNull(DOMNodePointer.getPrefix(plain));
        assertEquals("plain", DOMNodePointer.getLocalName(plain));
    }
}