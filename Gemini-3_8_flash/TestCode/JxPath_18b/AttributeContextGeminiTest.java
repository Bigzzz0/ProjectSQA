package org.apache.commons.jxpath.ri.axes;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Iterator;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.jxpath.ri.axes.AttributeContext
 *
 * Decision / Branch Points:
 * 1. reset():
 *    - Clears internal state (setStarted = false, iterator = null) and delegates to super.reset().
 * 2. setPosition(int position):
 *    - Branch: (position < getCurrentPosition()) -> calls reset() [backward positioning].
 *    - Loop: (getCurrentPosition() < position) -> calls nextNode() until position reached.
 *    - Branch: (!nextNode()) inside loop -> returns false if out-of-bounds.
 *    - Target reached -> returns true.
 * 3. nextNode():
 *    - super.setPosition(getCurrentPosition() + 1) invoked unconditionally.
 *    - Branch: (!setStarted)
 *        * setStarted = true.
 *        * Branch: (!(nodeTest instanceof NodeNameTest)) -> returns false [DEFECT POINT: ignores NodeTypeTest(node())].
 *        * nodeTest is NodeNameTest -> resolves QName and obtains attributeIterator from parent pointer.
 *    - Branch: (iterator == null) -> returns false.
 *    - Branch: (!iterator.setPosition(iterator.getPosition() + 1)) -> returns false (exhausted).
 *    - Success: sets currentNodePointer = iterator.getNodePointer() -> returns true.
 *
 * Defects4J Ground Truth Target:
 * - Bug in DOMModelTest/JDOMModelTest: Evaluating <.../attribute::node()> fails with empty result <[]>
 *   because AttributeContext.nextNode() prematurely exits with false on non-NodeNameTest instances
 *   rather than allowing wildcard matching for node() / NodeTypeTest(NODE_TYPE_NODE).
 * ====================================================================================================
 */
public class AttributeContextGeminiTest {

    // -------------------------------------------------------------------------
    // Test Harness & Fixture Helpers
    // -------------------------------------------------------------------------

    private Document createDocument(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new RuntimeException("XML parsing error in test fixture setup", e);
        }
    }

    private EvalContext createParentContext(Document doc, String xpath) {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext(doc);
        NodePointer pointer = (NodePointer) jxContext.getPointer(xpath);
        RootContext rootContext = new RootContext(jxContext, pointer);
        return new InitialContext(rootContext);
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIterateSpecificAttributeByName() {
        Document doc = createDocument("<item id=\"101\" name=\"gadget\"/>");
        EvalContext parent = createParentContext(doc, "/item");
        NodeNameTest nodeTest = new NodeNameTest(new QName(null, "id"));
        AttributeContext context = new AttributeContext(parent, nodeTest);

        assertEquals(0, context.getCurrentPosition());
        assertNull(context.getCurrentNodePointer());

        assertTrue("First step should locate 'id' attribute", context.nextNode());
        assertEquals(1, context.getCurrentPosition());
        assertNotNull(context.getCurrentNodePointer());
        assertEquals("id", context.getCurrentNodePointer().getName().getName());

        assertFalse("Second step should terminate because only one 'id' exists", context.nextNode());
    }

    @Test(timeout = 4000)
    public void testIterateWildcardAttributes() {
        Document doc = createDocument("<product attr1=\"v1\" attr2=\"v2\" attr3=\"v3\"/>");
        EvalContext parent = createParentContext(doc, "/product");
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        AttributeContext context = new AttributeContext(parent, wildcardTest);

        int count = 0;
        while (context.nextNode()) {
            count++;
            assertEquals(count, context.getCurrentPosition());
            assertNotNull(context.getCurrentNodePointer());
        }
        assertEquals("Wildcard match should traverse all 3 attributes", 3, count);
        assertFalse("Exhausted iterator should return false", context.nextNode());
    }

    @Test(timeout = 4000)
    public void testGetCurrentNodePointerBeforeAndAfterNavigation() {
        Document doc = createDocument("<item code=\"A1\"/>");
        EvalContext parent = createParentContext(doc, "/item");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "code")));

        assertNull("Pointer must be null before nextNode() is invoked", context.getCurrentNodePointer());
        boolean advanced = context.nextNode();
        assertTrue(advanced);
        assertNotNull("Pointer must not be null after successful step", context.getCurrentNodePointer());
        assertEquals("A1", context.getCurrentNodePointer().getValue());
    }

    @Test(timeout = 4000)
    public void testResetRestartsIteration() {
        Document doc = createDocument("<entry k1=\"v1\" k2=\"v2\"/>");
        EvalContext parent = createParentContext(doc, "/entry");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "*")));

        assertTrue(context.nextNode());
        assertEquals(1, context.getCurrentPosition());
        assertTrue(context.nextNode());
        assertEquals(2, context.getCurrentPosition());

        context.reset();
        assertEquals(0, context.getCurrentPosition());

        assertTrue("Should re-traverse first attribute after reset", context.nextNode());
        assertEquals(1, context.getCurrentPosition());
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNoAttributesOnElement() {
        Document doc = createDocument("<emptyElement/>");
        EvalContext parent = createParentContext(doc, "/emptyElement");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "*")));

        assertFalse("Element without attributes must return false on first step", context.nextNode());
        assertNull("Current node pointer must remain null", context.getCurrentNodePointer());
    }

    @Test(timeout = 4000)
    public void testNonExistentAttributeName() {
        Document doc = createDocument("<data present=\"true\"/>");
        EvalContext parent = createParentContext(doc, "/data");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "missing")));

        assertFalse("Querying an absent attribute must return false", context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    @Test(timeout = 4000)
    public void testSetPositionBoundariesZeroAndNegative() {
        Document doc = createDocument("<box size=\"L\" color=\"red\"/>");
        EvalContext parent = createParentContext(doc, "/box");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "*")));

        assertTrue("setPosition(0) should succeed and remain at 0", context.setPosition(0));
        assertEquals(0, context.getCurrentPosition());

        assertTrue("setPosition(-1) should succeed and remain at 0", context.setPosition(-1));
        assertEquals(0, context.getCurrentPosition());

        assertFalse("setPosition exceeding count should return false", context.setPosition(999));
    }

    @Test(timeout = 4000)
    public void testSetPositionBackAndForth() {
        Document doc = createDocument("<elem a=\"1\" b=\"2\" c=\"3\"/>");
        EvalContext parent = createParentContext(doc, "/elem");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "*")));

        assertTrue("Navigate forward to position 2", context.setPosition(2));
        assertEquals(2, context.getCurrentPosition());
        NodePointer ptrAt2 = context.getCurrentNodePointer();
        assertNotNull(ptrAt2);

        assertTrue("Navigate backward to position 1 triggers reset and replay", context.setPosition(1));
        assertEquals(1, context.getCurrentPosition());
        NodePointer ptrAt1 = context.getCurrentNodePointer();
        assertNotNull(ptrAt1);
        assertNotEquals("Position 1 pointer must differ from position 2", ptrAt1, ptrAt2);

        assertTrue("Navigate forward to position 3", context.setPosition(3));
        assertEquals(3, context.getCurrentPosition());

        assertFalse("Navigate beyond available attributes should return false", context.setPosition(4));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefectAxisAttributeWithNodeTypeNode() {
        /*
         * Defect Specification:
         * In XPath expressions like <vendor/product/price:amount/attribute::node()>,
         * the step specifies the attribute axis with a node() test (NodeTypeTest).
         * In the defective implementation of AttributeContext.nextNode():
         *    if (!(nodeTest instanceof NodeNameTest)) {
         *        return false;
         *    }
         * This unconditionally fails when nodeTest is a NodeTypeTest, returning empty results <[]>.
         */
        Document doc = createDocument("<amount currency=\"USD\" discount=\"10%\">100</amount>");
        JXPathContext context = JXPathContext.newContext(doc);
        Iterator<?> iterator = context.iterate("/amount/attribute::node()");

        assertTrue("Defect Check: attribute::node() must locate attributes on element", iterator.hasNext());
        Object first = iterator.next();
        assertNotNull("Attribute value should not be null", first);
    }

    @Test(timeout = 4000)
    public void testDefectAttributeContextWithNodeTypeTestDirect() {
        /*
         * Direct unit-level defect reproduction on AttributeContext:
         * Instantiating AttributeContext directly with NodeTypeTest(Compiler.NODE_TYPE_NODE)
         * reveals the rigid type-check defect in nextNode().
         */
        Document doc = createDocument("<root attr1=\"val1\" attr2=\"val2\"/>");
        EvalContext parent = createParentContext(doc, "/root");
        NodeTypeTest nodeTypeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        AttributeContext attributeContext = new AttributeContext(parent, nodeTypeTest);

        boolean found = attributeContext.nextNode();
        assertTrue("Defect Check: nextNode() should return true for NodeTypeTest(NODE_TYPE_NODE) on element with attributes", found);
        assertNotNull(attributeContext.getCurrentNodePointer());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullNodeTestHandledGracefully() {
        Document doc = createDocument("<item id=\"1\"/>");
        EvalContext parent = createParentContext(doc, "/item");
        AttributeContext context = new AttributeContext(parent, null);

        assertFalse("null nodeTest must return false gracefully from nextNode()", context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    @Test(timeout = 4000)
    public void testNonNodeNameTestProcessingInstruction() {
        Document doc = createDocument("<item id=\"1\"/>");
        EvalContext parent = createParentContext(doc, "/item");
        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        AttributeContext context = new AttributeContext(parent, piTest);

        assertFalse("ProcessingInstructionTest on attribute axis should return false", context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    @Test(timeout = 4000)
    public void testNullIteratorReturnedFromNodePointer() {
        // Construct a synthetic NodePointer that yields null for attributeIterator
        final NodePointer nullAttrPointer = new NodePointer(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public QName getName() {
                return new QName("mock");
            }

            @Override
            public Object getBaseValue() {
                return null;
            }

            @Override
            public Object getImmediateNode() {
                return null;
            }

            @Override
            public int getLength() {
                return 1;
            }

            @Override
            public boolean isCollection() {
                return false;
            }

            @Override
            public boolean isLeaf() {
                return true;
            }

            @Override
            public boolean isActual() {
                return true;
            }

            @Override
            public int compareChildNodePointers(NodePointer p1, NodePointer p2) {
                return 0;
            }

            @Override
            public NodeIterator attributeIterator(QName name) {
                return null; // Forces iterator == null branch in nextNode()
            }
        };

        EvalContext parentContext = new EvalContext(null) {
            @Override
            public NodePointer getCurrentNodePointer() {
                return nullAttrPointer;
            }

            @Override
            public boolean nextNode() {
                return true;
            }
        };

        AttributeContext context = new AttributeContext(parentContext, new NodeNameTest(new QName("any")));
        assertFalse("nextNode() must return false when attributeIterator() returns null", context.nextNode());
        assertNull(context.getCurrentNodePointer());
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMultipleResetsIdempotent() {
        Document doc = createDocument("<item a=\"1\"/>");
        EvalContext parent = createParentContext(doc, "/item");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "*")));

        context.reset();
        context.reset();
        assertEquals(0, context.getCurrentPosition());

        assertTrue(context.nextNode());
        assertEquals(1, context.getCurrentPosition());

        context.reset();
        context.reset();
        assertEquals(0, context.getCurrentPosition());
        assertTrue("Re-iteration after idempotent resets must function normally", context.nextNode());
    }

    @Test(timeout = 4000)
    public void testNextNodeAfterExhaustionReturnsFalseRepeatedly() {
        Document doc = createDocument("<single x=\"1\"/>");
        EvalContext parent = createParentContext(doc, "/single");
        AttributeContext context = new AttributeContext(parent, new NodeNameTest(new QName(null, "*")));

        assertTrue(context.nextNode());
        assertFalse(context.nextNode());
        assertFalse("Repeated nextNode() calls after exhaustion must continue returning false", context.nextNode());
        assertFalse(context.nextNode());
    }
}