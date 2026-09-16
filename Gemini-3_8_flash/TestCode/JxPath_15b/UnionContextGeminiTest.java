package org.apache.commons.jxpath.ri.axes;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.jxpath.ri.axes.UnionContext
 *
 * Decision / Branch Points Analyzed:
 * 1. getDocumentOrder():
 *    - Branch A: contexts.length > 1  --> returns 1 in defective version, falsely asserting document order.
 *    - Branch B: contexts.length <= 1 --> delegates to super.getDocumentOrder().
 *    - Boundary: contexts == null     --> throws NullPointerException.
 *    - Boundary: contexts.length == 0 --> delegates to super.getDocumentOrder().
 *
 * 2. setPosition(int position):
 *    - Branch A: !prepared (first call) --> sets prepared=true, initializes BasicNodeSet, iterates over contexts array.
 *    - Branch B: prepared (subsequent)  --> skips node collection, delegates directly to super.setPosition(position).
 *    - Loop 1: for (int i = 0; i < contexts.length; i++)
 *      - contexts.length == 0
 *      - contexts.length == 1
 *      - contexts.length > 1
 *    - Loop 2: while (ctx.nextSet())
 *      - false immediately (empty context set)
 *      - true once
 *      - true multiple times (multi-set context)
 *    - Loop 3: while (ctx.nextNode())
 *      - false immediately (empty node context)
 *      - true one or more times
 *    - Branch C: !pointers.contains(ptr)
 *      - true  --> nodeSet.add(ptr), pointers.add(ptr) (new unique node)
 *      - false --> skipped (duplicate node suppression across or within contexts)
 *    - Return: super.setPosition(position)
 *      - position < 1 (e.g. 0, -1) --> returns false
 *      - 1 <= position <= size     --> returns true
 *      - position > size           --> returns false
 *
 * Defects4J Ground Truth Target:
 * - DOMModelTest::testUnion & JDOMModelTest::testUnion:
 *   Evaluating </vendor[1]/contact[4] | /vendor[1]/contact[1]> expected:<John> but was:<Jack Black>.
 *   In XPath, the union operator '|' requires nodes to be returned in document order without duplicates.
 *   The defective implementation appends nodes in expression order and incorrectly claims documentOrder == 1
 *   (or fails to sort the merged set), resulting in contact[4] appearing before contact[1].
 * ---------------------------------------------------------------------------------------------------------------------
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

public class UnionContextGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnionWithDistinctPointersAcrossContexts() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        NodePointer ptr1 = NodePointer.newNodePointer(new QName("nodeA"), "valA", Locale.getDefault());
        NodePointer ptr2 = NodePointer.newNodePointer(new QName("nodeB"), "valB", Locale.getDefault());

        EvalContext ctx1 = new MockEvalContext(rootContext, new NodePointer[]{ ptr1 });
        EvalContext ctx2 = new MockEvalContext(rootContext, new NodePointer[]{ ptr2 });

        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[]{ ctx1, ctx2 });

        // Verify initial state
        assertEquals(0, unionContext.getPosition());

        // Step to position 1
        assertTrue("Position 1 should exist", unionContext.setPosition(1));
        assertEquals(1, unionContext.getPosition());
        assertEquals(ptr1, unionContext.getCurrentNodePointer());

        // Step to position 2
        assertTrue("Position 2 should exist", unionContext.setPosition(2));
        assertEquals(2, unionContext.getPosition());
        assertEquals(ptr2, unionContext.getCurrentNodePointer());

        // Step out of bounds
        assertFalse("Position 3 should be out of bounds", unionContext.setPosition(3));

        // Verify NodeSet contents
        BasicNodeSet nodeSet = (BasicNodeSet) unionContext.getNodeSet();
        assertEquals(2, nodeSet.getPointers().size());
        assertTrue(nodeSet.getPointers().contains(ptr1));
        assertTrue(nodeSet.getPointers().contains(ptr2));
    }

    @Test(timeout = 4000)
    public void testDeduplicationAcrossAndWithinContexts() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        NodePointer ptrShared = NodePointer.newNodePointer(new QName("shared"), "valShared", Locale.getDefault());
        NodePointer ptrUnique = NodePointer.newNodePointer(new QName("unique"), "valUnique", Locale.getDefault());

        // ctx1 has ptrShared twice; ctx2 has ptrShared and ptrUnique
        EvalContext ctx1 = new MockEvalContext(rootContext, new NodePointer[]{ ptrShared, ptrShared });
        EvalContext ctx2 = new MockEvalContext(rootContext, new NodePointer[]{ ptrShared, ptrUnique });

        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[]{ ctx1, ctx2 });

        assertTrue("Position 1 should exist", unionContext.setPosition(1));
        assertEquals(ptrShared, unionContext.getCurrentNodePointer());

        assertTrue("Position 2 should exist", unionContext.setPosition(2));
        assertEquals(ptrUnique, unionContext.getCurrentNodePointer());

        // Total unique nodes should be exactly 2
        assertFalse("Position 3 should be out of bounds due to deduplication", unionContext.setPosition(3));
        BasicNodeSet nodeSet = (BasicNodeSet) unionContext.getNodeSet();
        assertEquals(2, nodeSet.getPointers().size());
    }

    @Test(timeout = 4000)
    public void testPreparedFlagPreventsRecomputation() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        NodePointer ptr = NodePointer.newNodePointer(new QName("item"), "val", Locale.getDefault());
        MockEvalContext ctx = new MockEvalContext(rootContext, new NodePointer[]{ ptr });

        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[]{ ctx });

        assertTrue(unionContext.setPosition(1));
        assertEquals(1, ctx.nextSetCallCount);

        // Subsequent setPosition calls should NOT re-iterate child contexts
        assertTrue(unionContext.setPosition(1));
        assertFalse(unionContext.setPosition(2));
        assertEquals("Subsequent calls to setPosition must not re-examine contexts when prepared=true",
                1, ctx.nextSetCallCount);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyContextsArray() {
        UnionContext unionContext = new UnionContext(null, new EvalContext[0]);

        assertEquals("Document order for empty contexts array should match super.getDocumentOrder()",
                0, unionContext.getDocumentOrder());
        assertFalse("setPosition(1) on empty union must return false", unionContext.setPosition(1));
        assertEquals(0, unionContext.getPosition());
        assertEquals(0, unionContext.getNodeSet().getPointers().size());
    }

    @Test(timeout = 4000)
    public void testSingleContextDocumentOrderBranch() {
        EvalContext[] contexts = new EvalContext[]{ null };
        UnionContext unionContext = new UnionContext(null, contexts);

        // contexts.length == 1: branch 'contexts.length > 1' is FALSE -> delegates to super
        assertEquals("Single context array should delegate to super.getDocumentOrder()",
                0, unionContext.getDocumentOrder());
    }

    @Test(timeout = 4000)
    public void testMultipleContextsDocumentOrderBranch() {
        EvalContext[] contexts = new EvalContext[]{ null, null };
        UnionContext unionContext = new UnionContext(null, contexts);

        // contexts.length == 2: branch 'contexts.length > 1' is TRUE -> returns 1
        assertEquals("Multiple contexts array returns 1", 1, unionContext.getDocumentOrder());
    }

    @Test(timeout = 4000)
    public void testPositionBoundariesZeroNegativeAndLarge() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        NodePointer ptr = NodePointer.newNodePointer(new QName("item"), "val", Locale.getDefault());
        EvalContext ctx = new MockEvalContext(rootContext, new NodePointer[]{ ptr });
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[]{ ctx });

        // Position zero boundary
        assertFalse("setPosition(0) must return false", unionContext.setPosition(0));
        assertEquals(0, unionContext.getPosition());

        // Negative position boundary
        assertFalse("setPosition(-1) must return false", unionContext.setPosition(-1));
        assertEquals(0, unionContext.getPosition());

        // Integer max boundary
        assertFalse("setPosition(Integer.MAX_VALUE) must return false", unionContext.setPosition(Integer.MAX_VALUE));
        assertEquals(0, unionContext.getPosition());

        // Valid boundary
        assertTrue("setPosition(1) must return true", unionContext.setPosition(1));
        assertEquals(1, unionContext.getPosition());

        // Reset to 0 after valid position
        assertFalse("setPosition(0) resets position", unionContext.setPosition(0));
        assertEquals(0, unionContext.getPosition());
    }

    @Test(timeout = 4000)
    public void testContextsYieldingNoSetsOrNoNodes() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        // Context that returns false on first nextSet()
        EvalContext emptySetCtx = new EvalContext(rootContext) {
            public boolean nextNode() { return false; }
            public boolean nextSet() { return false; }
            public NodePointer getCurrentNodePointer() { return null; }
            public int getDocumentOrder() { return 0; }
            public boolean setPosition(int position) { return false; }
        };

        // Context that returns true on nextSet(), but false on first nextNode()
        EvalContext emptyNodeCtx = new EvalContext(rootContext) {
            private boolean setReturned = false;
            public boolean nextSet() {
                if (!setReturned) {
                    setReturned = true;
                    return true;
                }
                return false;
            }
            public boolean nextNode() { return false; }
            public NodePointer getCurrentNodePointer() { return null; }
            public int getDocumentOrder() { return 0; }
            public boolean setPosition(int position) { return false; }
        };

        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[]{ emptySetCtx, emptyNodeCtx });
        assertFalse("Empty contexts should result in empty UnionContext", unionContext.setPosition(1));
        assertEquals(0, unionContext.getNodeSet().getPointers().size());
    }

    @Test(timeout = 4000)
    public void testMultiSetIterationInChildContext() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        final NodePointer ptrSet1 = NodePointer.newNodePointer(new QName("s1"), "v1", Locale.getDefault());
        final NodePointer ptrSet2 = NodePointer.newNodePointer(new QName("s2"), "v2", Locale.getDefault());

        // Context that yields 2 sets, each with 1 node
        EvalContext multiSetCtx = new EvalContext(rootContext) {
            private int setIdx = 0;
            private boolean nodeReturned = false;

            public boolean nextSet() {
                if (setIdx < 2) {
                    setIdx++;
                    nodeReturned = false;
                    return true;
                }
                return false;
            }

            public boolean nextNode() {
                if (!nodeReturned) {
                    nodeReturned = true;
                    return true;
                }
                return false;
            }

            public NodePointer getCurrentNodePointer() {
                return (setIdx == 1) ? ptrSet1 : ptrSet2;
            }

            public int getDocumentOrder() { return 0; }
            public boolean setPosition(int position) { return false; }
        };

        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[]{ multiSetCtx });

        assertTrue(unionContext.setPosition(1));
        assertEquals(ptrSet1, unionContext.getCurrentNodePointer());

        assertTrue(unionContext.setPosition(2));
        assertEquals(ptrSet2, unionContext.getCurrentNodePointer());

        assertFalse(unionContext.setPosition(3));
        assertEquals(2, unionContext.getNodeSet().getPointers().size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * CRITICAL DEFECT TEST:
     * Targets Defects4J issue in DOMModelTest::testUnion / JDOMModelTest::testUnion
     * Error: Evaluating </vendor[1]/contact[4] | /vendor[1]/contact[1]> expected:<John> but was:<Jack Black>
     *
     * XPath specification mandates that the union operator '|' returns nodes in document order.
     * In the defective implementation, UnionContext preserves context evaluation order
     * instead of document order, causing contact[4] ("Jack Black") to be returned prior to
     * contact[1] ("John").
     */
    @Test(timeout = 4000)
    public void testDefectUnionDocumentOrderEvaluationDOM() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element vendor = doc.createElement("vendor");
        doc.appendChild(vendor);

        Element contact1 = doc.createElement("contact");
        contact1.appendChild(doc.createTextNode("John"));
        vendor.appendChild(contact1);

        Element contact2 = doc.createElement("contact");
        contact2.appendChild(doc.createTextNode("Jack"));
        vendor.appendChild(contact2);

        Element contact3 = doc.createElement("contact");
        contact3.appendChild(doc.createTextNode("Jim"));
        vendor.appendChild(contact3);

        Element contact4 = doc.createElement("contact");
        contact4.appendChild(doc.createTextNode("Jack Black"));
        vendor.appendChild(contact4);

        JXPathContext context = JXPathContext.newContext(doc);

        // Reverse union operand order: contact[4] | contact[1]
        Object value = context.getValue("/vendor[1]/contact[4] | /vendor[1]/contact[1]");

        // In document order, contact[1] ("John") MUST precede contact[4] ("Jack Black")
        assertEquals("Union of /contact[4] | /contact[1] must return 'John' following document order",
                "John", value);
    }

    /**
     * CRITICAL DEFECT TEST (Iterator Variant):
     * Validates that iterating over the union node-set traverses elements in strict document order.
     */
    @Test(timeout = 4000)
    public void testDefectUnionIteratorDocumentOrderDOM() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element vendor = doc.createElement("vendor");
        doc.appendChild(vendor);

        Element contact1 = doc.createElement("contact");
        contact1.appendChild(doc.createTextNode("John"));
        vendor.appendChild(contact1);

        Element contact4 = doc.createElement("contact");
        contact4.appendChild(doc.createTextNode("Jack Black"));
        vendor.appendChild(contact4);

        JXPathContext context = JXPathContext.newContext(doc);

        Iterator it = context.iterate("/vendor[1]/contact[4] | /vendor[1]/contact[1]");
        assertTrue("Iterator should have first element", it.hasNext());
        assertEquals("First iterated element in document order must be 'John'", "John", it.next());

        assertTrue("Iterator should have second element", it.hasNext());
        assertEquals("Second iterated element in document order must be 'Jack Black'", "Jack Black", it.next());

        assertFalse("Iterator should have no further elements", it.hasNext());
    }

    @Test(timeout = 4000)
    public void testUnionSelfDeduplicationDOM() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element vendor = doc.createElement("vendor");
        doc.appendChild(vendor);

        Element contact4 = doc.createElement("contact");
        contact4.appendChild(doc.createTextNode("Jack Black"));
        vendor.appendChild(contact4);

        JXPathContext context = JXPathContext.newContext(doc);

        Object value = context.getValue("/vendor[1]/contact[4] | /vendor[1]/contact[4]");
        assertEquals("Jack Black", value);

        Iterator it = context.iterate("/vendor[1]/contact[4] | /vendor[1]/contact[4]");
        assertTrue(it.hasNext());
        assertEquals("Jack Black", it.next());
        assertFalse("Duplicate operand union must result in a single element", it.hasNext());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetDocumentOrderWithNullContextsThrowsNPE() {
        UnionContext unionContext = new UnionContext(null, null);
        unionContext.getDocumentOrder();
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSetPositionWithNullContextsThrowsNPE() {
        UnionContext unionContext = new UnionContext(null, null);
        unionContext.setPosition(1);
    }

    @Test(timeout = 4000)
    public void testSetPositionWithNullContextElementThrowsNPE() {
        UnionContext unionContext = new UnionContext(null, new EvalContext[]{ null });
        try {
            unionContext.setPosition(1);
            fail("Expected NullPointerException when iterating over a null context element");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialNodeSetInstantiation() {
        UnionContext unionContext = new UnionContext(null, new EvalContext[0]);
        assertNotNull("Constructor must initialize an internal NodeSet", unionContext.getNodeSet());
        assertTrue("Initial NodeSet should be an instance of BasicNodeSet",
                unionContext.getNodeSet() instanceof BasicNodeSet);
        assertEquals("Initial NodeSet must be empty", 0, unionContext.getNodeSet().getPointers().size());
    }

    @Test(timeout = 4000)
    public void testSuccessiveNextNodeCalls() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        NodePointer ptr1 = NodePointer.newNodePointer(new QName("p1"), "v1", Locale.getDefault());
        NodePointer ptr2 = NodePointer.newNodePointer(new QName("p2"), "v2", Locale.getDefault());

        EvalContext ctx = new MockEvalContext(rootContext, new NodePointer[]{ ptr1, ptr2 });
        UnionContext unionContext = new UnionContext(rootContext, new EvalContext[]{ ctx });

        assertTrue("First nextNode() must return true", unionContext.nextNode());
        assertEquals(1, unionContext.getPosition());
        assertEquals(ptr1, unionContext.getCurrentNodePointer());

        assertTrue("Second nextNode() must return true", unionContext.nextNode());
        assertEquals(2, unionContext.getPosition());
        assertEquals(ptr2, unionContext.getCurrentNodePointer());

        assertFalse("Third nextNode() must return false", unionContext.nextNode());
    }

    // =========================================================================
    // Helper Classes for Deterministic White-Box Testing
    // =========================================================================

    private static class MockEvalContext extends EvalContext {
        private final NodePointer[] pointers;
        private int index = -1;
        private boolean setConsumed = false;
        int nextSetCallCount = 0;

        MockEvalContext(EvalContext parent, NodePointer[] pointers) {
            super(parent);
            this.pointers = pointers != null ? pointers : new NodePointer[0];
        }

        public boolean nextSet() {
            nextSetCallCount++;
            if (!setConsumed) {
                setConsumed = true;
                index = -1;
                return true;
            }
            return false;
        }

        public boolean nextNode() {
            if (index + 1 < pointers.length) {
                index++;
                return true;
            }
            return false;
        }

        public NodePointer getCurrentNodePointer() {
            if (index >= 0 && index < pointers.length) {
                return pointers[index];
            }
            return null;
        }

        public int getDocumentOrder() {
            return 0;
        }

        public boolean setPosition(int position) {
            if (position >= 1 && position <= pointers.length) {
                this.index = position - 1;
                return true;
            }
            return false;
        }
    }
}