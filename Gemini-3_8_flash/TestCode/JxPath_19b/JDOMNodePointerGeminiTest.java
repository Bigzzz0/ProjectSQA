/* [Branch & Defect Analysis Matrix]
 *
 * Decision / Condition Branch Analysis:
 * 1. getNamespaceURI():
 *    - node instanceof Element (empty namespace "" vs valid namespace vs null)
 *    - node not Element (Document, Text, Comment, PI) -> returns null
 * 2. getNamespaceURI(String prefix):
 *    - prefix == "xml" -> XML_NAMESPACE.getURI()
 *    - node instanceof Document -> element = getRootElement()
 *    - node instanceof Element -> element = (Element) node
 *    - element == null -> returns null
 *    - element.getNamespace(prefix) == null -> null vs URI
 * 3. compareChildNodePointers(p1, p2):
 *    - pointer1.getBaseValue() == pointer2.getBaseValue() -> 0
 *    - p1 Attribute, p2 not Attribute -> -1
 *    - p1 not Attribute, p2 Attribute -> 1
 *    - both Attribute -> traverse Element.getAttributes(), match p1 (-1), match p2 (1), fallthrough (0)
 *    - node not Element -> throws RuntimeException
 *    - both Content -> traverse Element.getContent(), match p1 (-1), match p2 (1), fallthrough (0)
 * 4. isLeaf():
 *    - Element (size == 0 -> true, size > 0 -> false)
 *    - Document (size == 0 -> true, size > 0 -> false)
 *    - other (Text, Comment, PI) -> true
 * 5. getName():
 *    - Element with prefix ("" converted to null vs non-empty prefix)
 *    - ProcessingInstruction -> QName(null, target)
 *    - Other -> QName(null, null)
 * 6. getValue():
 *    - Element: concatenates Element and Text child values
 *    - Comment: returns trimmed text
 *    - Text: xml:space preserve vs normal (trimmed)
 *    - ProcessingInstruction: xml:space preserve vs normal (trimmed)
 * 7. setValue(value):
 *    - node instanceof Text (empty string removes from parent, non-empty sets text)
 *    - node instanceof Element: clears content, then handles:
 *      Element, Document, Text/CDATA, ProcessingInstruction, Comment, other Object
 * 8. testNode(pointer, node, test):
 *    - test == null -> true
 *    - NodeNameTest: node not Element -> false; wildcard with/without prefix; localName and NS matches
 *    - NodeTypeTest: NODE_TYPE_NODE, TEXT (Text or CDATA), COMMENT, PI, default
 *    - ProcessingInstructionTest: match vs mismatch target
 * 9. isLanguage(lang):
 *    - nearest xml:lang attribute present vs absent (delegates to super)
 * 10. createAttribute(context, name):
 *     - node not Element -> delegates to super
 *     - node Element: with prefix (resolvable vs unresolvable), without prefix, attribute existing vs new
 * 11. createChild(context, name, index, [value]):
 *     - AbstractFactory success vs failure (JXPathAbstractFactoryException)
 * 12. remove():
 *     - root node (no parent) -> throws JXPathException
 *     - non-root node -> removed from parent Element
 * 13. asPath():
 *     - id != null -> id('...')
 *     - parent != null -> recursive path
 *     - Element under JDOMNodePointer: with NS, without NS, prefix in resolver vs node()
 *     - Text / CDATA -> /text()[n]
 *     - PI -> /processing-instruction('...')[n]
 *
 * Known Defect (Defects4J):
 * AliasedNamespaceIterationTest::testIterateJDOM:
 * When querying elements belonging to an aliased namespace, getRelativePositionByQName() compares
 * child.getQualifiedName().equals(name), causing sibling elements with aliased/differing prefixes
 * in the same namespace to miscount their relative index (producing /a:doc[1]/a:elem[1] instead of
 * /a:doc[1]/a:elem[2]).
 */
package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Iterator;
import java.util.Locale;

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
    public void testConstructorsAndBasicGetters() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);

        assertSame(root, rootPointer.getNode());
        assertSame(root, rootPointer.getBaseValue());
        assertSame(root, rootPointer.getImmediateNode());
        assertEquals(1, rootPointer.getLength());
        assertFalse(rootPointer.isCollection());
        assertEquals(new QName(null, "root"), rootPointer.getName());

        Element child = new Element("child");
        root.addContent(child);
        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
        assertSame(rootPointer, childPointer.getParent());
        assertSame(child, childPointer.getNode());

        JDOMNodePointer idPointer = new JDOMNodePointer(root, Locale.FRENCH, "myId");
        assertEquals("id('myId')", idPointer.asPath());
    }

    @Test(timeout = 4000)
    public void testIsLeaf() {
        Element emptyElem = new Element("empty");
        JDOMNodePointer emptyPointer = new JDOMNodePointer(emptyElem, Locale.ENGLISH);
        assertTrue("Empty element should be leaf", emptyPointer.isLeaf());

        Element parentElem = new Element("parent");
        parentElem.addContent(new Element("sub"));
        JDOMNodePointer parentPointer = new JDOMNodePointer(parentElem, Locale.ENGLISH);
        assertFalse("Element with child should not be leaf", parentPointer.isLeaf());

        Document emptyDoc = new Document();
        JDOMNodePointer emptyDocPointer = new JDOMNodePointer(emptyDoc, Locale.ENGLISH);
        assertTrue("Empty document should be leaf", emptyDocPointer.isLeaf());

        Document docWithRoot = new Document(new Element("rootDoc"));
        JDOMNodePointer docPointer = new JDOMNodePointer(docWithRoot, Locale.ENGLISH);
        assertFalse("Document with root element should not be leaf", docPointer.isLeaf());

        Text text = new Text("sample");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        assertTrue("Text node should be leaf", textPointer.isLeaf());

        Comment comment = new Comment("sample comment");
        JDOMNodePointer commentPointer = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertTrue("Comment node should be leaf", commentPointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIFromVariousNodes() {
        Element elementWithDefaultNS = new Element("elem", Namespace.getNamespace("", "http://example.com/default"));
        JDOMNodePointer ptr1 = new JDOMNodePointer(elementWithDefaultNS, Locale.ENGLISH);
        assertEquals("http://example.com/default", ptr1.getNamespaceURI());

        Element elementWithoutNS = new Element("elem");
        JDOMNodePointer ptr2 = new JDOMNodePointer(elementWithoutNS, Locale.ENGLISH);
        assertNull(ptr2.getNamespaceURI());

        Text text = new Text("text");
        JDOMNodePointer ptr3 = new JDOMNodePointer(text, Locale.ENGLISH);
        assertNull(ptr3.getNamespaceURI());

        Element elementWithPrefix = new Element("elem", "ns", "http://example.com/ns");
        JDOMNodePointer ptr4 = new JDOMNodePointer(elementWithPrefix, Locale.ENGLISH);
        assertEquals("http://example.com/ns", ptr4.getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIByPrefix() {
        Element root = new Element("root", "pref", "http://example.com/pref");
        Namespace extraNS = Namespace.getNamespace("extra", "http://example.com/extra");
        root.addNamespaceDeclaration(extraNS);
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.ENGLISH);

        assertEquals(Namespace.XML_NAMESPACE.getURI(), pointer.getNamespaceURI("xml"));
        assertEquals("http://example.com/pref", pointer.getNamespaceURI("pref"));
        assertEquals("http://example.com/extra", pointer.getNamespaceURI("extra"));
        assertNull(pointer.getNamespaceURI("unmapped"));

        Document doc = new Document(new Element("docRoot", "dpre", "http://example.com/doc"));
        JDOMNodePointer docPointer = new JDOMNodePointer(doc, Locale.ENGLISH);
        assertEquals("http://example.com/doc", docPointer.getNamespaceURI("dpre"));

        Comment comment = new Comment("note");
        JDOMNodePointer commentPointer = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertNull(commentPointer.getNamespaceURI("pref"));
    }

    @Test(timeout = 4000)
    public void testGetValueOnDifferentNodeTypes() {
        Element parent = new Element("parent");
        Element child1 = new Element("child1");
        child1.setText("Hello ");
        Element child2 = new Element("child2");
        child2.setText("World");
        parent.addContent(child1);
        parent.addContent(child2);

        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        assertEquals("Hello World", parentPointer.getValue());

        Comment comment = new Comment("  A comment with spaces  ");
        JDOMNodePointer commentPointer = new JDOMNodePointer(comment, Locale.ENGLISH);
        assertEquals("A comment with spaces", commentPointer.getValue());

        ProcessingInstruction pi = new ProcessingInstruction("target", "  piData  ");
        JDOMNodePointer piPointer = new JDOMNodePointer(pi, Locale.ENGLISH);
        assertEquals("piData", piPointer.getValue());

        Text text = new Text("  text with spaces  ");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        assertEquals("text with spaces", textPointer.getValue());
    }

    @Test(timeout = 4000)
    public void testGetValueWithXmlSpacePreserve() {
        Element parent = new Element("parent");
        parent.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
        Text text = new Text("  preserved spaces  ");
        parent.addContent(text);

        JDOMNodePointer textPointer = new JDOMNodePointer(parent, text);
        assertEquals("  preserved spaces  ", textPointer.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnText() {
        Element parent = new Element("parent");
        Text text = new Text("initial");
        parent.addContent(text);

        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        textPointer.setValue("updated");
        assertEquals("updated", text.getText());

        textPointer.setValue("");
        assertEquals(0, parent.getContent().size());
    }

    @Test(timeout = 4000)
    public void testSetValueOnElement() {
        Element target = new Element("target");
        JDOMNodePointer targetPointer = new JDOMNodePointer(target, Locale.ENGLISH);

        targetPointer.setValue(new Text("textChild"));
        assertEquals(1, target.getContent().size());
        assertEquals("textChild", ((Text) target.getContent().get(0)).getText());

        targetPointer.setValue(new CDATA("cdataChild"));
        assertEquals(1, target.getContent().size());
        assertEquals("cdataChild", ((Text) target.getContent().get(0)).getText());

        targetPointer.setValue(new ProcessingInstruction("piTarget", "piData"));
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof ProcessingInstruction);

        targetPointer.setValue(new Comment("commentData"));
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Comment);

        Element sourceElem = new Element("source");
        sourceElem.addContent(new Element("c1"));
        sourceElem.addContent(new Text("c2"));
        targetPointer.setValue(sourceElem);
        assertEquals(2, target.getContent().size());

        Document sourceDoc = new Document();
        Element docRoot = new Element("docRoot");
        docRoot.addContent(new Element("subDocElem"));
        sourceDoc.setRootElement(docRoot);
        targetPointer.setValue(sourceDoc);
        assertEquals(1, target.getContent().size());

        targetPointer.setValue(Integer.valueOf(42));
        assertEquals("42", target.getText());
    }

    @Test(timeout = 4000)
    public void testIterators() {
        Element root = new Element("root");
        root.setAttribute("attr1", "val1");
        root.addContent(new Element("child"));

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);

        NodeIterator childIt = rootPointer.childIterator(null, false, null);
        assertNotNull(childIt);
        assertTrue(childIt.setPosition(1));
        assertNotNull(childIt.getNodePointer());

        NodeIterator attrIt = rootPointer.attributeIterator(new QName("attr1"));
        assertNotNull(attrIt);
        assertTrue(attrIt.setPosition(1));
        assertNotNull(attrIt.getNodePointer());

        NodeIterator nsIt = rootPointer.namespaceIterator();
        assertNotNull(nsIt);

        NodePointer nsPointer = rootPointer.namespacePointer("xml");
        assertNotNull(nsPointer);
    }

    @Test(timeout = 4000)
    public void testLanguageHandling() {
        Element root = new Element("root");
        root.setAttribute("lang", "en-US", Namespace.XML_NAMESPACE);
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.US);
        assertTrue(rootPointer.isLanguage("en"));
        assertTrue(rootPointer.isLanguage("en-US"));
        assertFalse(rootPointer.isLanguage("fr"));

        JDOMNodePointer childPointer = new JDOMNodePointer(child, Locale.US);
        assertTrue("Enclosing xml:lang should be inherited", childPointer.isLanguage("en"));

        Element bareElem = new Element("bare");
        JDOMNodePointer barePointer = new JDOMNodePointer(bareElem, Locale.GERMAN);
        assertTrue("Should fallback to super.isLanguage() using locale", barePointer.isLanguage("de"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() {
        Element parent = new Element("parent");
        Attribute a1 = new Attribute("a1", "v1");
        Attribute a2 = new Attribute("a2", "v2");
        parent.setAttribute(a1);
        parent.setAttribute(a2);

        Element c1 = new Element("c1");
        Element c2 = new Element("c2");
        parent.addContent(c1);
        parent.addContent(c2);

        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, Locale.ENGLISH);

        NodePointer ptrA1 = new JDOMNodePointer(parentPointer, a1);
        NodePointer ptrA2 = new JDOMNodePointer(parentPointer, a2);
        NodePointer ptrC1 = new JDOMNodePointer(parentPointer, c1);
        NodePointer ptrC2 = new JDOMNodePointer(parentPointer, c2);

        assertEquals(0, parentPointer.compareChildNodePointers(ptrA1, ptrA1));
        assertEquals(-1, parentPointer.compareChildNodePointers(ptrA1, ptrC1));
        assertEquals(1, parentPointer.compareChildNodePointers(c1 != null ? ptrC1 : null, ptrA1));

        assertEquals(-1, parentPointer.compareChildNodePointers(ptrA1, ptrA2));
        assertEquals(1, parentPointer.compareChildNodePointers(ptrA2, ptrA1));

        assertEquals(-1, parentPointer.compareChildNodePointers(ptrC1, ptrC2));
        assertEquals(1, parentPointer.compareChildNodePointers(ptrC2, ptrC1));

        Element foreignElem = new Element("foreign");
        NodePointer ptrForeign = new JDOMNodePointer(parentPointer, foreignElem);
        assertEquals(0, parentPointer.compareChildNodePointers(ptrForeign, ptrForeign));
    }

    @Test(timeout = 4000)
    public void testTestNodeCombinations() {
        Element elem = new Element("myElem", "p", "http://example.com/ns");
        JDOMNodePointer elemPointer = new JDOMNodePointer(elem, Locale.ENGLISH);

        assertTrue(JDOMNodePointer.testNode(elemPointer, elem, null));

        assertTrue(JDOMNodePointer.testNode(elemPointer, elem, new NodeNameTest(new QName(null, "*"))));
        assertTrue(JDOMNodePointer.testNode(elemPointer, elem, new NodeNameTest(new QName("p", "*"), "http://example.com/ns")));
        assertTrue(JDOMNodePointer.testNode(elemPointer, elem, new NodeNameTest(new QName("p", "myElem"), "http://example.com/ns")));
        assertFalse(JDOMNodePointer.testNode(elemPointer, elem, new NodeNameTest(new QName("p", "otherElem"), "http://example.com/ns")));

        Text text = new Text("hello");
        assertFalse(JDOMNodePointer.testNode(elemPointer, text, new NodeNameTest(new QName(null, "*"))));

        assertTrue(JDOMNodePointer.testNode(elemPointer, elem, new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertTrue(JDOMNodePointer.testNode(elemPointer, text, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertTrue(JDOMNodePointer.testNode(elemPointer, new CDATA("raw"), new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(JDOMNodePointer.testNode(elemPointer, elem, new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        Comment comment = new Comment("comm");
        assertTrue(JDOMNodePointer.testNode(elemPointer, comment, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(JDOMNodePointer.testNode(elemPointer, elem, new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));

        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        assertTrue(JDOMNodePointer.testNode(elemPointer, pi, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(JDOMNodePointer.testNode(elemPointer, elem, new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(JDOMNodePointer.testNode(elemPointer, elem, new NodeTypeTest(9999)));

        assertTrue(JDOMNodePointer.testNode(elemPointer, pi, new ProcessingInstructionTest("target")));
        assertFalse(JDOMNodePointer.testNode(elemPointer, pi, new ProcessingInstructionTest("mismatched")));
        assertFalse(JDOMNodePointer.testNode(elemPointer, elem, new ProcessingInstructionTest("target")));

        assertFalse(JDOMNodePointer.testNode(elemPointer, elem, new NodeTest() {}));
    }

    @Test(timeout = 4000)
    public void testAsPathVariations() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        assertEquals("", rootPointer.asPath());

        Element child1 = new Element("child");
        Element child2 = new Element("child");
        root.addContent(child1);
        root.addContent(child2);

        JDOMNodePointer child1Pointer = new JDOMNodePointer(rootPointer, child1);
        JDOMNodePointer child2Pointer = new JDOMNodePointer(rootPointer, child2);
        assertEquals("/child[1]", child1Pointer.asPath());
        assertEquals("/child[2]", child2Pointer.asPath());

        Text text1 = new Text("t1");
        CDATA cdata1 = new CDATA("c1");
        root.addContent(text1);
        root.addContent(cdata1);

        JDOMNodePointer textPointer = new JDOMNodePointer(rootPointer, text1);
        JDOMNodePointer cdataPointer = new JDOMNodePointer(rootPointer, cdata1);
        assertEquals("/text()[1]", textPointer.asPath());
        assertEquals("/text()[2]", cdataPointer.asPath());

        ProcessingInstruction pi1 = new ProcessingInstruction("target", "1");
        ProcessingInstruction pi2 = new ProcessingInstruction("target", "2");
        root.addContent(pi1);
        root.addContent(pi2);

        JDOMNodePointer pi1Pointer = new JDOMNodePointer(rootPointer, pi1);
        JDOMNodePointer pi2Pointer = new JDOMNodePointer(rootPointer, pi2);
        assertEquals("/processing-instruction('target')[1]", pi1Pointer.asPath());
        assertEquals("/processing-instruction('target')[2]", pi2Pointer.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathWithNamespaces() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);

        Element childPrefixed = new Element("elem", "p", "http://example.com/p");
        root.addContent(childPrefixed);
        rootPointer.getNamespaceResolver().registerNamespace("p", "http://example.com/p");

        JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, childPrefixed);
        assertEquals("/p:elem[1]", childPointer.asPath());

        Element childUnresolvedNS = new Element("item", "", "http://example.com/unresolved");
        root.addContent(childUnresolvedNS);
        JDOMNodePointer childUnresolvedPointer = new JDOMNodePointer(rootPointer, childUnresolvedNS);
        assertEquals("/node()[2]", childUnresolvedPointer.asPath());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug reported in AliasedNamespaceIterationTest::testIterateJDOM.
     * When elements in the same namespace have different prefix declarations in JDOM
     * (e.g. one without prefix and one with prefix "b"), getRelativePositionByQName()
     * incorrectly uses child.getQualifiedName().equals(name), causing sibling index
     * miscalculation and producing duplicate paths (e.g. /a:doc[1]/a:elem[1] twice).
     */
    @Test(timeout = 4000)
    public void testDefectAliasedNamespaceIterationJDOM() {
        Document doc = new Document();
        Element root = new Element("doc", "b", "http://foo");
        doc.setRootElement(root);
        Element elem1 = new Element("elem", "http://foo");
        Element elem2 = new Element("elem", "b", "http://foo");
        root.addContent(elem1);
        root.addContent(elem2);

        JXPathContext context = JXPathContext.newContext(doc);
        context.registerNamespace("a", "http://foo");

        Iterator iterator = context.iteratePointers("/a:doc/a:elem");
        assertTrue("Iterator should yield first child pointer", iterator.hasNext());
        NodePointer ptr1 = (NodePointer) iterator.next();
        assertEquals("/a:doc[1]/a:elem[1]", ptr1.asPath());

        assertTrue("Iterator should yield second child pointer", iterator.hasNext());
        NodePointer ptr2 = (NodePointer) iterator.next();
        assertEquals("Evaluating pointer iterator path for aliased namespace second element",
                "/a:doc[1]/a:elem[2]", ptr2.asPath());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCompareChildNodePointersNonElementThrows() {
        Text text = new Text("text");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        NodePointer p1 = new JDOMNodePointer(text, Locale.ENGLISH);
        NodePointer p2 = new JDOMNodePointer(text, Locale.ENGLISH);
        textPointer.compareChildNodePointers(p1, p2);
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testRemoveRootNodeThrows() {
        Element root = new Element("root");
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.ENGLISH);
        rootPointer.remove();
    }

    @Test(timeout = 4000)
    public void testRemoveNonRootNodeSuccess() {
        Element root = new Element("root");
        Element child = new Element("child");
        root.addContent(child);

        JDOMNodePointer childPointer = new JDOMNodePointer(root, child);
        childPointer.remove();
        assertEquals(0, root.getContent().size());
    }

    @Test(timeout = 4000)
    public void testCreateAttributeSuccessAndErrors() {
        Element elem = new Element("elem");
        JDOMNodePointer pointer = new JDOMNodePointer(elem, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(elem);

        NodePointer attr1 = pointer.createAttribute(context, new QName("attr1"));
        assertNotNull(attr1);
        assertEquals("attr1", ((Attribute) attr1.getBaseValue()).getName());

        NodePointer attr1Again = pointer.createAttribute(context, new QName("attr1"));
        assertSame(attr1.getBaseValue(), attr1Again.getBaseValue());

        try {
            pointer.createAttribute(context, new QName("unknownPrefix", "attr2"));
            fail("Expected JXPathException for unknown namespace prefix");
        } catch (JXPathException expected) {
            assertTrue(expected.getMessage().contains("Unknown namespace prefix"));
        }

        pointer.getNamespaceResolver().registerNamespace("knownPrefix", "http://example.com/ns");
        NodePointer attrWithPrefix = pointer.createAttribute(context, new QName("knownPrefix", "attr2"));
        assertNotNull(attrWithPrefix);
        assertEquals("http://example.com/ns", ((Attribute) attrWithPrefix.getBaseValue()).getNamespaceURI());

        Text text = new Text("text");
        JDOMNodePointer textPointer = new JDOMNodePointer(text, Locale.ENGLISH);
        try {
            textPointer.createAttribute(context, new QName("attr3"));
            fail("Cannot create attribute on non-element");
        } catch (JXPathException expected) {
            // Expected from super.createAttribute()
        }
    }

    @Test(timeout = 4000)
    public void testCreateChildWithFactory() {
        Element parent = new Element("parent");
        JDOMNodePointer pointer = new JDOMNodePointer(parent, Locale.ENGLISH);
        JXPathContext context = JXPathContext.newContext(parent);

        try {
            pointer.createChild(context, new QName("child"), 0);
            fail("Expected JXPathAbstractFactoryException when factory is missing");
        } catch (JXPathAbstractFactoryException expected) {
            // Success
        }

        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, Pointer ptr, Object pNode, String name, int index) {
                if (pNode instanceof Element) {
                    ((Element) pNode).addContent(new Element(name));
                    return true;
                }
                return false;
            }
        });

        NodePointer created = pointer.createChild(context, new QName("child"), 0);
        assertNotNull(created);
        assertEquals("child", ((Element) created.getBaseValue()).getName());

        NodePointer createdWithValue = pointer.createChild(context, new QName("childWithValue"), 1, "testValue");
        assertNotNull(createdWithValue);
        assertEquals("testValue", createdWithValue.getValue());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element elem1 = new Element("elem");
        Element elem2 = new Element("elem");

        JDOMNodePointer ptr1A = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr1B = new JDOMNodePointer(elem1, Locale.ENGLISH);
        JDOMNodePointer ptr2 = new JDOMNodePointer(elem2, Locale.ENGLISH);

        assertEquals(ptr1A, ptr1A);
        assertEquals(ptr1A, ptr1B);
        assertFalse(ptr1A.equals(ptr2));
        assertFalse(ptr1A.equals(null));
        assertFalse(ptr1A.equals("notAPointer"));

        assertEquals(elem1.hashCode(), ptr1A.hashCode());
        assertEquals(ptr1A.hashCode(), ptr1B.hashCode());
    }

    @Test(timeout = 4000)
    public void testStaticHelperMethods() {
        assertEquals("http://www.w3.org/XML/1998/namespace", JDOMNodePointer.XML_NAMESPACE_URI);
        assertEquals("http://www.w3.org/2000/xmlns/", JDOMNodePointer.XMLNS_NAMESPACE_URI);

        Element elem = new Element("test", "p", "http://example.com");
        Attribute attr = new Attribute("name", "val", Namespace.getNamespace("ap", "http://example.com/attr"));
        Text text = new Text("hello");

        assertEquals("p", JDOMNodePointer.getPrefix(elem));
        assertEquals("ap", JDOMNodePointer.getPrefix(attr));
        assertNull(JDOMNodePointer.getPrefix(text));

        assertEquals("test", JDOMNodePointer.getLocalName(elem));
        assertEquals("name", JDOMNodePointer.getLocalName(attr));
        assertNull(JDOMNodePointer.getLocalName(text));
    }

    @Test(timeout = 4000)
    public void testNamespaceResolverContextIntegrity() {
        Element root = new Element("root");
        JDOMNodePointer pointer = new JDOMNodePointer(root, Locale.ENGLISH);

        NamespaceResolver resolver1 = pointer.getNamespaceResolver();
        assertNotNull(resolver1);
        NamespaceResolver resolver2 = pointer.getNamespaceResolver();
        assertSame("NamespaceResolver should be lazily instantiated and cached", resolver1, resolver2);
    }
}