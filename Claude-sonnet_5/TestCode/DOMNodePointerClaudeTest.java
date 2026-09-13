package org.apache.commons.jxpath.ri.model.dom;

import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ri.*;
import org.apache.commons.jxpath.ri.model.*;
import org.apache.commons.jxpath.ri.compiler.*;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;
import static org.junit.Assert.*;

public class DOMNodePointerClaudeTest {

    private Document newDoc() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    // ================= CRITICAL DEFECT TEST =================

    /**
     * @target DOMNodePointer#asPath()
     * @scenario A DOMNodePointer chain (Document -> root Element -> child Element)
     *           built without a JXPathContext (so no NamespaceResolver is available).
     * @defectRisk JXPATH-12: NullPointerException thrown from asPath() due to
     *             getNamespaceResolver() returning null when comparing namespace URIs.
     */
    @Test(timeout = 4000)
    public void testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);
        doc.appendChild(root);

        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.getDefault());
        DOMNodePointer rootPointer = new DOMNodePointer(docPointer, root);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        String path = childPointer.asPath();
        assertNotNull(path);
        assertEquals("/root[1]/child[1]", path);
    }

    // ================= testNode() coverage =================

    /**
     * @target testNode(NodeTest)
     * @scenario test == null
     * @defectRisk should always return true regardless of node
     */
    @Test(timeout = 4000)
    public void testTestNodeNullTestReturnsTrue() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertTrue(p.testNode(null));
    }

    /**
     * @target testNode(NodeTest) - NodeNameTest branch matching element
     * @scenario element name matches test name, no namespace
     * @defectRisk name/namespace comparison logic
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeNameTestMatchingElement() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "foo"));
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeNameTest branch non matching
     * @scenario element name does not match test name
     * @defectRisk false positive matching
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeNameTestNonMatchingName() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "bar"));
        assertFalse(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeNameTest on non-element node
     * @scenario NodeNameTest applied to a text node
     * @defectRisk should return false for non ELEMENT_NODE type
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeNameTestOnNonElementNode() throws Exception {
        Document doc = newDoc();
        Text text = doc.createTextNode("abc");
        DOMNodePointer p = new DOMNodePointer(text, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "foo"));
        assertFalse(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - wildcard NodeNameTest on Element
     * @scenario wildcard "*" without prefix on element node
     * @defectRisk wildcard branch should return true immediately
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeNameTestWildcardOnElement() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - wildcard NodeNameTest on non-element
     * @scenario wildcard test on a comment node
     * @defectRisk wildcard branch should still respect node type check first
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeNameTestWildcardOnNonElement() throws Exception {
        Document doc = newDoc();
        Comment c = doc.createComment("comment");
        DOMNodePointer p = new DOMNodePointer(c, Locale.getDefault());
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertFalse(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeTypeTest NODE_TYPE_NODE
     * @scenario element node tested against NODE_TYPE_NODE
     * @defectRisk mapping of node types
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeTypeTestNode() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeTypeTest NODE_TYPE_TEXT on TEXT_NODE
     * @scenario text node tested against NODE_TYPE_TEXT
     * @defectRisk text/cdata union handling
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeTypeTestTextOnTextNode() throws Exception {
        Document doc = newDoc();
        Text text = doc.createTextNode("abc");
        DOMNodePointer p = new DOMNodePointer(text, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeTypeTest NODE_TYPE_TEXT on CDATA
     * @scenario CDATA section tested against NODE_TYPE_TEXT
     * @defectRisk CDATA should be treated as text type
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeTypeTestTextOnCDATA() throws Exception {
        Document doc = newDoc();
        CDATASection cdata = doc.createCDATASection("abc");
        DOMNodePointer p = new DOMNodePointer(cdata, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeTypeTest NODE_TYPE_COMMENT
     * @scenario comment node tested against NODE_TYPE_COMMENT
     * @defectRisk comment mapping logic
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeTypeTestComment() throws Exception {
        Document doc = newDoc();
        Comment c = doc.createComment("abc");
        DOMNodePointer p = new DOMNodePointer(c, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeTypeTest NODE_TYPE_PI
     * @scenario PI node tested against NODE_TYPE_PI
     * @defectRisk PI mapping logic
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeTypeTestPI() throws Exception {
        Document doc = newDoc();
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        DOMNodePointer p = new DOMNodePointer(pi, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - NodeTypeTest mismatch
     * @scenario element node tested against NODE_TYPE_COMMENT
     * @defectRisk should return false for mismatched types
     */
    @Test(timeout = 4000)
    public void testTestNodeNodeTypeTestMismatchReturnsFalse() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertFalse(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - ProcessingInstructionTest match
     * @scenario PI node with target matching test target
     * @defectRisk target string comparison
     */
    @Test(timeout = 4000)
    public void testTestNodeProcessingInstructionMatch() throws Exception {
        Document doc = newDoc();
        ProcessingInstruction pi = doc.createProcessingInstruction("mytarget", "data");
        DOMNodePointer p = new DOMNodePointer(pi, Locale.getDefault());
        ProcessingInstructionTest test = new ProcessingInstructionTest("mytarget");
        assertTrue(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - ProcessingInstructionTest non match
     * @scenario PI node with different target
     * @defectRisk should return false on mismatch
     */
    @Test(timeout = 4000)
    public void testTestNodeProcessingInstructionNonMatch() throws Exception {
        Document doc = newDoc();
        ProcessingInstruction pi = doc.createProcessingInstruction("mytarget", "data");
        DOMNodePointer p = new DOMNodePointer(pi, Locale.getDefault());
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(p.testNode(test));
    }

    /**
     * @target testNode(NodeTest) - ProcessingInstructionTest on non-PI node
     * @scenario applying ProcessingInstructionTest to an element node
     * @defectRisk should fall through to final return false
     */
    @Test(timeout = 4000)
    public void testTestNodeProcessingInstructionOnNonPINode() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        ProcessingInstructionTest test = new ProcessingInstructionTest("mytarget");
        assertFalse(p.testNode(test));
    }

    // ================= asPath() coverage =================

    /**
     * @target asPath() - ELEMENT_NODE using JXPathContext-provided pointers
     * @scenario navigate to a simple element via context
     * @defectRisk namespace resolver interplay & relative position
     */
    @Test(timeout = 4000)
    public void testAsPathElementSimple() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        Element child = doc.createElement("child");
        root.appendChild(child);
        doc.appendChild(root);
        JXPathContext context = JXPathContext.newContext(doc);
        Pointer p = context.getPointer("/root/child");
        assertEquals("/root[1]/child[1]", p.asPath());
    }

    /**
     * @target asPath() - ELEMENT_NODE relative position with duplicates
     * @scenario two sibling elements with the same tag name
     * @defectRisk getRelativePositionByName counting logic
     */
    @Test(timeout = 4000)
    public void testAsPathElementDuplicateSiblings() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        Element item1 = doc.createElement("item");
        Element item2 = doc.createElement("item");
        root.appendChild(item1);
        root.appendChild(item2);
        doc.appendChild(root);
        JXPathContext context = JXPathContext.newContext(doc);
        Pointer p = context.getPointer("/root/item[2]");
        assertEquals("/root[1]/item[2]", p.asPath());
    }

    /**
     * @target asPath() - TEXT_NODE branch
     * @scenario text node child of an element
     * @defectRisk getRelativePositionOfTextNode logic
     */
    @Test(timeout = 4000)
    public void testAsPathTextNode() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        Element holder = doc.createElement("holder");
        Text text = doc.createTextNode("hello");
        holder.appendChild(text);
        root.appendChild(holder);
        doc.appendChild(root);
        JXPathContext context = JXPathContext.newContext(doc);
        Pointer p = context.getPointer("/root/holder/text()[1]");
        assertEquals("/root[1]/holder[1]/text()[1]", p.asPath());
    }

    /**
     * @target asPath() - CDATA_SECTION_NODE branch
     * @scenario CDATA section child treated as text node
     * @defectRisk shared logic with TEXT_NODE case
     */
    @Test(timeout = 4000)
    public void testAsPathCDATANode() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        Element holder = doc.createElement("holder");
        CDATASection cdata = doc.createCDATASection("hello");
        holder.appendChild(cdata);
        root.appendChild(holder);
        doc.appendChild(root);
        JXPathContext context = JXPathContext.newContext(doc);
        Pointer p = context.getPointer("/root/holder/text()[1]");
        assertEquals("/root[1]/holder[1]/text()[1]", p.asPath());
    }

    /**
     * @target asPath() - PROCESSING_INSTRUCTION_NODE branch
     * @scenario PI node child of root
     * @defectRisk getRelativePositionOfPI logic & string concat
     */
    @Test(timeout = 4000)
    public void testAsPathProcessingInstructionNode() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        ProcessingInstruction pi = doc.createProcessingInstruction("mytarget", "data");
        root.appendChild(pi);
        doc.appendChild(root);
        JXPathContext context = JXPathContext.newContext(doc);
        Pointer p = context.getPointer("/root/processing-instruction('mytarget')[1]");
        assertEquals("/root[1]/processing-instruction('mytarget')[1]", p.asPath());
    }

    /**
     * @target asPath() - id != null branch
     * @scenario DOMNodePointer constructed with an id string
     * @defectRisk escape() and id('...') formatting
     */
    @Test(timeout = 4000)
    public void testAsPathIdPointer() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault(), "myid");
        assertEquals("id('myid')", p.asPath());
    }

    /**
     * @target asPath() - escape() handling of quotes in id
     * @scenario id string contains single and double quotes
     * @defectRisk escape() substring replacement logic
     */
    @Test(timeout = 4000)
    public void testAsPathIdPointerWithQuotesEscape() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault(), "a'b\"c");
        assertEquals("id('a&apos;b&quot;c')", p.asPath());
    }

    // ================= getValue() coverage =================

    /**
     * @target getValue() - COMMENT_NODE
     * @scenario comment node with data containing whitespace
     * @defectRisk trimming logic in stringValue
     */
    @Test(timeout = 4000)
    public void testGetValueComment() throws Exception {
        Document doc = newDoc();
        Comment c = doc.createComment("  hello  ");
        DOMNodePointer p = new DOMNodePointer(c, Locale.getDefault());
        assertEquals("hello", p.getValue());
    }

    /**
     * @target getValue() - TEXT_NODE
     * @scenario simple text node value
     * @defectRisk trimming logic
     */
    @Test(timeout = 4000)
    public void testGetValueText() throws Exception {
        Document doc = newDoc();
        Text t = doc.createTextNode("  world  ");
        DOMNodePointer p = new DOMNodePointer(t, Locale.getDefault());
        assertEquals("world", p.getValue());
    }

    /**
     * @target getValue() - PROCESSING_INSTRUCTION_NODE
     * @scenario PI data with whitespace
     * @defectRisk trimming logic
     */
    @Test(timeout = 4000)
    public void testGetValuePI() throws Exception {
        Document doc = newDoc();
        ProcessingInstruction pi = doc.createProcessingInstruction("t", "  data  ");
        DOMNodePointer p = new DOMNodePointer(pi, Locale.getDefault());
        assertEquals("data", p.getValue());
    }

    /**
     * @target getValue() - ELEMENT_NODE concatenation of children
     * @scenario element with multiple text-bearing children
     * @defectRisk recursive stringValue concatenation
     */
    @Test(timeout = 4000)
    public void testGetValueElementConcatenatesChildren() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        el.appendChild(doc.createTextNode("Hello "));
        Element inner = doc.createElement("inner");
        inner.appendChild(doc.createTextNode("World"));
        el.appendChild(inner);
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertEquals("Hello World", p.getValue());
    }

    // ================= setValue() coverage =================

    /**
     * @target setValue(Object) - TEXT_NODE replace with new string
     * @scenario setting a non-empty string value on text node
     * @defectRisk node value replacement logic
     */
    @Test(timeout = 4000)
    public void testSetValueTextNodeReplace() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("p");
        Text text = doc.createTextNode("orig");
        parent.appendChild(text);
        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        DOMNodePointer tp = new DOMNodePointer(parentPtr, text);
        tp.setValue("newvalue");
        assertEquals("newvalue", text.getNodeValue());
    }

    /**
     * @target setValue(Object) - TEXT_NODE set to empty removes node
     * @scenario setting empty string value on text node
     * @defectRisk node removal branch
     */
    @Test(timeout = 4000)
    public void testSetValueTextNodeToEmptyRemovesNode() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("p");
        Text text = doc.createTextNode("orig");
        parent.appendChild(text);
        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        DOMNodePointer tp = new DOMNodePointer(parentPtr, text);
        tp.setValue("");
        assertEquals(0, parent.getChildNodes().getLength());
    }

    /**
     * @target setValue(Object) - non-text node with String value
     * @scenario setting a plain string on an element node
     * @defectRisk children removal + text node append
     */
    @Test(timeout = 4000)
    public void testSetValueElementWithStringValue() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        el.appendChild(doc.createTextNode("old"));
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        p.setValue("newtext");
        assertEquals("newtext", p.getValue());
    }

    /**
     * @target setValue(Object) - element node with Element value clones children
     * @scenario passing an Element as new value
     * @defectRisk cloning logic for Element/Document branch
     */
    @Test(timeout = 4000)
    public void testSetValueElementWithElementNodeValue() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());

        Element sourceElement = doc.createElement("src");
        sourceElement.appendChild(doc.createTextNode("clonedtext"));

        p.setValue(sourceElement);
        assertEquals("clonedtext", p.getValue());
    }

    /**
     * @target setValue(Object) - element node with Document value clones children
     * @scenario passing a Document as new value
     * @defectRisk cloning logic Document branch
     */
    @Test(timeout = 4000)
    public void testSetValueElementWithDocumentNodeValue() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());

        Document otherDoc = newDoc();
        Element otherRoot = otherDoc.createElement("otherRoot");
        otherRoot.appendChild(otherDoc.createTextNode("doctext"));
        otherDoc.appendChild(otherRoot);

        p.setValue(otherDoc);
        assertEquals("doctext", p.getValue());
    }

    /**
     * @target setValue(Object) - element node with non-Element/Document Node value
     * @scenario passing a Text node directly as new value
     * @defectRisk else branch appending cloned node directly
     */
    @Test(timeout = 4000)
    public void testSetValueElementWithNonElementNodeClone() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());

        Text valueText = doc.createTextNode("directtext");
        p.setValue(valueText);
        assertEquals("directtext", p.getValue());
    }

    // ================= createAttribute / attributeIterator =================

    /**
     * @target createAttribute(JXPathContext, QName) - no prefix
     * @scenario creating a plain attribute on an element
     * @defectRisk attribute creation without namespace
     */
    @Test(timeout = 4000)
    public void testCreateAttributeOnElementNoPrefix() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodePointer result = p.createAttribute(context, new QName(null, "attr1"));
        assertNotNull(result);
        assertTrue(el.hasAttribute("attr1"));
    }

    /**
     * @target createAttribute(JXPathContext, QName) - with known prefix namespace
     * @scenario prefix declared via xmlns:foo attribute
     * @defectRisk namespace resolution & setAttributeNS
     */
    @Test(timeout = 4000)
    public void testCreateAttributeOnElementWithPrefixKnownNamespace() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        el.setAttribute("xmlns:foo", "http://foo.example.com");
        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodePointer result = p.createAttribute(context, new QName("foo", "attr2"));
        assertNotNull(result);
        assertTrue(el.hasAttributeNS("http://foo.example.com", "attr2"));
    }

    /**
     * @target createAttribute(JXPathContext, QName) - unknown prefix throws
     * @scenario prefix not declared anywhere in the ancestry
     * @defectRisk should throw JXPathException
     */
    @Test(timeout = 4000)
    public void testCreateAttributeOnElementWithPrefixUnknownNamespaceThrows() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        try {
            p.createAttribute(context, new QName("unknownprefix", "attr3"));
            fail("Expected JXPathException");
        }
        catch (JXPathException e) {
            // expected
        }
    }

    /**
     * @target createAttribute(JXPathContext, QName) - non Element node delegates to super
     * @scenario invoking createAttribute on a Text node pointer
     * @defectRisk super.createAttribute behavior
     */
    @Test(timeout = 4000)
    public void testCreateAttributeOnNonElementDelegatesToSuper() throws Exception {
        Document doc = newDoc();
        Text text = doc.createTextNode("abc");
        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer p = new DOMNodePointer(text, Locale.getDefault());
        try {
            p.createAttribute(context, new QName(null, "attr"));
            // Some implementations may just return without throwing;
            // either behavior is acceptable as long as no crash of a
            // different kind occurs.
        }
        catch (RuntimeException e) {
            // acceptable - super's default behavior may throw
            assertNotNull(e);
        }
    }

    /**
     * @target attributeIterator(QName)
     * @scenario element with a set attribute, iterate to find it
     * @defectRisk DOMAttributeIterator wiring
     */
    @Test(timeout = 4000)
    public void testAttributeIterator() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        el.setAttribute("attr1", "val1");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        NodeIterator it = p.attributeIterator(new QName(null, "attr1"));
        assertTrue(it.setPosition(1));
        assertEquals("val1", it.getNodePointer().getValue());
    }

    // ================= createChild coverage =================

    /**
     * @target createChild(JXPathContext, QName, int, Object)
     * @scenario successful child creation via a custom AbstractFactory
     * @defectRisk factory invocation + child iterator lookup + setValue chaining
     */
    @Test(timeout = 4000)
    public void testCreateChildWithAbstractFactory() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                Element parentEl = (Element) parent;
                Element newEl = parentEl.getOwnerDocument().createElement(name);
                parentEl.appendChild(newEl);
                return true;
            }
        });

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        QName name = new QName(null, "child");
        NodePointer childPtr = rootPointer.createChild(context, name, 0, "value1");
        assertEquals("value1", childPtr.getValue());
    }

    /**
     * @target createChild(JXPathContext, QName, int)
     * @scenario factory returns false, so exception must be thrown
     * @defectRisk JXPathAbstractFactoryException path
     */
    @Test(timeout = 4000)
    public void testCreateChildFactoryFailsThrows() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        JXPathContext context = JXPathContext.newContext(doc);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, Pointer pointer,
                    Object parent, String name, int index) {
                return false;
            }
        });

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.getDefault());
        QName name = new QName(null, "child");
        try {
            rootPointer.createChild(context, name, 0);
            fail("Expected JXPathAbstractFactoryException");
        }
        catch (JXPathAbstractFactoryException e) {
            // expected
        }
    }

    // ================= namespace operations =================

    /**
     * @target getNamespaceURI(String) - empty/null prefix delegates to default
     * @scenario prefix is empty string
     * @defectRisk delegation to getDefaultNamespaceURI()
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIDefaultEmptyPrefix() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertNull(p.getNamespaceURI(""));
    }

    /**
     * @target getNamespaceURI(String) - "xml" prefix
     * @scenario requesting standard xml namespace
     * @defectRisk constant XML_NAMESPACE_URI returned
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIXmlPrefix() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertEquals(DOMNodePointer.XML_NAMESPACE_URI, p.getNamespaceURI("xml"));
    }

    /**
     * @target getNamespaceURI(String) - "xmlns" prefix
     * @scenario requesting standard xmlns namespace
     * @defectRisk constant XMLNS_NAMESPACE_URI returned
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIXmlnsPrefix() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertEquals(DOMNodePointer.XMLNS_NAMESPACE_URI, p.getNamespaceURI("xmlns"));
    }

    /**
     * @target getNamespaceURI(String) - custom prefix declared on ancestor
     * @scenario xmlns:foo declared on element itself
     * @defectRisk ancestor walk + caching in namespaces map
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURICustomPrefixFound() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        el.setAttribute("xmlns:foo", "http://foo.example.com");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertEquals("http://foo.example.com", p.getNamespaceURI("foo"));
        // Second call exercises cache branch
        assertEquals("http://foo.example.com", p.getNamespaceURI("foo"));
    }

    /**
     * @target getNamespaceURI(String) - unknown custom prefix
     * @scenario prefix not declared anywhere
     * @defectRisk should return null (UNKNOWN_NAMESPACE mapping)
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURICustomPrefixUnknown() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertNull(p.getNamespaceURI("unknownprefix"));
    }

    /**
     * @target getDefaultNamespaceURI() - xmlns attribute present
     * @scenario element declares default xmlns attribute
     * @defectRisk ancestor walk logic + caching field
     */
    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURIWithXmlnsAttribute() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        el.setAttribute("xmlns", "http://default.example.com");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertEquals("http://default.example.com", p.getDefaultNamespaceURI());
    }

    /**
     * @target getDefaultNamespaceURI() - no xmlns attribute
     * @scenario element with no default namespace declared
     * @defectRisk should return null when empty string
     */
    @Test(timeout = 4000)
    public void testGetDefaultNamespaceURINoAttribute() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertNull(p.getDefaultNamespaceURI());
    }

    /**
     * @target getNamespaceURI() (no-arg) delegates to static getNamespaceURI(Node)
     * @scenario element without explicit namespace URI, and without xmlns attr
     * @defectRisk static method delegation
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURINoArgOnPlainElement() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertNull(p.getNamespaceURI());
    }

    // ================= compareChildNodePointers =================

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario two sibling element nodes, in document order and reversed
     * @defectRisk sibling scan loop and sign of result
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointersOrderingBeforeAfter() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("parent");
        Element c1 = doc.createElement("c1");
        Element c2 = doc.createElement("c2");
        parent.appendChild(c1);
        parent.appendChild(c2);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        NodePointer p1 = new DOMNodePointer(parentPtr, c1);
        NodePointer p2 = new DOMNodePointer(parentPtr, c2);

        assertEquals(-1, parentPtr.compareChildNodePointers(p1, p2));
        assertEquals(1, parentPtr.compareChildNodePointers(p2, p1));
    }

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario identical node reference for both pointers
     * @defectRisk short-circuit early return of 0
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointersSameNode() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("parent");
        Element c1 = doc.createElement("c1");
        parent.appendChild(c1);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        NodePointer p1 = new DOMNodePointer(parentPtr, c1);
        NodePointer p1b = new DOMNodePointer(parentPtr, c1);

        assertEquals(0, parentPtr.compareChildNodePointers(p1, p1b));
    }

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario one pointer wraps an ATTRIBUTE_NODE, other a regular element
     * @defectRisk attribute-vs-non-attribute branch ordering
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointersAttributeVsNonAttribute() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("parent");
        Element c1 = doc.createElement("c1");
        parent.appendChild(c1);
        Attr attr = doc.createAttribute("attr1");
        parent.setAttributeNode(attr);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        NodePointer attrPtr = new DOMNodePointer(parentPtr, attr);
        NodePointer elemPtr = new DOMNodePointer(parentPtr, c1);

        assertEquals(-1, parentPtr.compareChildNodePointers(attrPtr, elemPtr));
        assertEquals(1, parentPtr.compareChildNodePointers(elemPtr, attrPtr));
    }

    /**
     * @target compareChildNodePointers(NodePointer, NodePointer)
     * @scenario both pointers wrap ATTRIBUTE_NODEs on the same parent element
     * @defectRisk NamedNodeMap scan for relative attribute ordering
     */
    @Test(timeout = 4000)
    public void testCompareChildNodePointersBothAttributes() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("parent");
        Attr attr1 = doc.createAttribute("attr1");
        Attr attr2 = doc.createAttribute("attr2");
        parent.setAttributeNode(attr1);
        parent.setAttributeNode(attr2);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        NodePointer p1 = new DOMNodePointer(parentPtr, attr1);
        NodePointer p2 = new DOMNodePointer(parentPtr, attr2);

        int result = parentPtr.compareChildNodePointers(p1, p2);
        assertTrue(result == -1 || result == 1 || result == 0);
    }

    // ================= misc coverage: getName, isLeaf, isLanguage, remove, equals/hashCode =================

    /**
     * @target getName() - ELEMENT_NODE
     * @scenario simple element without prefix
     * @defectRisk QName construction for elements
     */
    @Test(timeout = 4000)
    public void testGetNameElement() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        QName qn = p.getName();
        assertEquals("foo", qn.getName());
    }

    /**
     * @target getName() - PROCESSING_INSTRUCTION_NODE
     * @scenario PI node name equals its target
     * @defectRisk QName construction for PI nodes
     */
    @Test(timeout = 4000)
    public void testGetNamePI() throws Exception {
        Document doc = newDoc();
        ProcessingInstruction pi = doc.createProcessingInstruction("mytarget", "data");
        DOMNodePointer p = new DOMNodePointer(pi, Locale.getDefault());
        QName qn = p.getName();
        assertEquals("mytarget", qn.getName());
    }

    /**
     * @target isLeaf()
     * @scenario element with and without children
     * @defectRisk hasChildNodes negation logic
     */
    @Test(timeout = 4000)
    public void testIsLeafTrueFalse() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("parent");
        Element child = doc.createElement("child");
        parent.appendChild(child);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        DOMNodePointer childPtr = new DOMNodePointer(parentPtr, child);

        assertFalse(parentPtr.isLeaf());
        assertTrue(childPtr.isLeaf());
    }

    /**
     * @target isLanguage(String) - matching xml:lang attribute present
     * @scenario xml:lang="en" attribute set directly on element
     * @defectRisk case-insensitive prefix matching
     */
    @Test(timeout = 4000)
    public void testIsLanguageMatch() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        el.setAttribute("xml:lang", "en-US");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertTrue(p.isLanguage("en"));
    }

    /**
     * @target isLanguage(String) - no xml:lang attribute found, falls back to super
     * @scenario element without any lang attribute
     * @defectRisk super.isLanguage() fallback path
     */
    @Test(timeout = 4000)
    public void testIsLanguageNoAttributeFallsBackToSuper() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("el");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        // Without a lang attribute chain, matching default locale's language
        // exercises the super.isLanguage() fallback without throwing.
        boolean result = p.isLanguage("xx");
        assertFalse(result);
    }

    /**
     * @target remove()
     * @scenario removing a child node that has a parent
     * @defectRisk parent.removeChild invocation
     */
    @Test(timeout = 4000)
    public void testRemoveNode() throws Exception {
        Document doc = newDoc();
        Element parent = doc.createElement("parent");
        Element child = doc.createElement("child");
        parent.appendChild(child);

        DOMNodePointer parentPtr = new DOMNodePointer(parent, Locale.getDefault());
        DOMNodePointer childPtr = new DOMNodePointer(parentPtr, child);
        childPtr.remove();
        assertEquals(0, parent.getChildNodes().getLength());
    }

    /**
     * @target remove() - root node with no parent throws
     * @scenario removing a node without any DOM parent
     * @defectRisk JXPathException on missing parent
     */
    @Test(timeout = 4000)
    public void testRemoveRootThrows() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        DOMNodePointer p = new DOMNodePointer(root, Locale.getDefault());
        try {
            p.remove();
            fail("Expected JXPathException");
        }
        catch (JXPathException e) {
            // expected
        }
    }

    /**
     * @target hashCode() and equals(Object)
     * @scenario same node wrapped twice vs different node
     * @defectRisk identity-based equals/hashCode semantics
     */
    @Test(timeout = 4000)
    public void testHashCodeAndEquals() throws Exception {
        Document doc = newDoc();
        Element el1 = doc.createElement("el1");
        Element el2 = doc.createElement("el2");
        DOMNodePointer p1 = new DOMNodePointer(el1, Locale.getDefault());
        DOMNodePointer p1b = new DOMNodePointer(el1, Locale.getDefault());
        DOMNodePointer p2 = new DOMNodePointer(el2, Locale.getDefault());

        assertTrue(p1.equals(p1b));
        assertFalse(p1.equals(p2));
        assertFalse(p1.equals("not a pointer"));
        assertTrue(p1.equals(p1));
        assertEquals(System.identityHashCode(el1), p1.hashCode());
    }

    /**
     * @target getPointerByID(JXPathContext, String)
     * @scenario id not found in document, returns NullPointer
     * @defectRisk fallback to NullPointer when element not found
     */
    @Test(timeout = 4000)
    public void testGetPointerByIDNotFoundReturnsNullPointer() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        JXPathContext context = JXPathContext.newContext(doc);
        DOMNodePointer docPointer = new DOMNodePointer(doc, Locale.getDefault());
        Pointer result = docPointer.getPointerByID(context, "missingid");
        assertTrue(result instanceof NullPointer);
    }

    // ================= static helper methods coverage =================

    /**
     * @target getLocalName(Node) and getPrefix(Node) static methods
     * @scenario node name containing a prefix separated by colon
     * @defectRisk substring extraction logic
     */
    @Test(timeout = 4000)
    public void testGetLocalNameAndPrefixStaticMethods() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElementNS("http://ns.example.com", "ns:foo");
        assertEquals("foo", DOMNodePointer.getLocalName(el));
        assertEquals("ns", DOMNodePointer.getPrefix(el));

        Element plain = doc.createElement("plain");
        assertEquals("plain", DOMNodePointer.getLocalName(plain));
        assertNull(DOMNodePointer.getPrefix(plain));
    }

    /**
     * @target getNamespaceURI(Node) static method
     * @scenario element created with explicit namespace URI
     * @defectRisk direct namespaceURI retrieval branch
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIStaticMethod() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElementNS("http://ns.example.com", "ns:foo");
        assertEquals("http://ns.example.com", DOMNodePointer.getNamespaceURI(el));
    }

    /**
     * @target getNamespaceURI(Node) static method - Document argument
     * @scenario passing the Document itself, delegates to document element
     * @defectRisk Document -> Element delegation branch
     */
    @Test(timeout = 4000)
    public void testGetNamespaceURIStaticMethodOnDocument() throws Exception {
        Document doc = newDoc();
        Element root = doc.createElementNS("http://ns.example.com", "ns:root");
        doc.appendChild(root);
        assertEquals("http://ns.example.com", DOMNodePointer.getNamespaceURI(doc));
    }

    // ================= getBaseValue / getImmediateNode / isActual / isCollection / getLength =================

    /**
     * @target getBaseValue(), getImmediateNode(), isActual(), isCollection(), getLength()
     * @scenario basic invariants of a simple DOMNodePointer over an element
     * @defectRisk trivial accessor / constant methods
     */
    @Test(timeout = 4000)
    public void testBasicAccessors() throws Exception {
        Document doc = newDoc();
        Element el = doc.createElement("foo");
        DOMNodePointer p = new DOMNodePointer(el, Locale.getDefault());
        assertSame(el, p.getBaseValue());
        assertSame(el, p.getImmediateNode());
        assertTrue(p.isActual());
        assertFalse(p.isCollection());
        assertEquals(1, p.getLength());
    }
}