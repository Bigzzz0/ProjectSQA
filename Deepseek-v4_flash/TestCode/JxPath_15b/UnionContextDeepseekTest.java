package org.apache.commons.jxpath.ri.axes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: UnionContext (extends NodeSetContext)
 * 
 * Key Branches & Conditions:
 * 1. getDocumentOrder(): return 1 if contexts.length > 1 else super.getDocumentOrder()
 * 2. setPosition(boolean):
 *    - if (!prepared) block
 *    - for loop over contexts array (non-null check implicitly trusted)
 *    - inner loops: while (ctx.nextSet()), while (ctx.nextNode())
 *    - condition: if (!pointers.contains(ptr))
 *    - calls to nodeSet.add(ptr) and pointers.add(ptr)
 *    - finally super.setPosition(position)
 * 
 * Boundary Conditions:
 * - contexts array length: 0, 1, 2 (multiple)
 * - duplicate pointers across contexts
 * - pointers returned in non-document order (defect scenario)
 * - setPosition with invalid positions (<=0)
 * - prepared flag: setPosition called multiple times
 * 
 * Defect (from Defects4J): Union of XPath nodes results in incorrect document order.
 * Root cause: setPosition adds pointers in iteration order of contexts, not sorted by document order.
 * 
 * Test Strategy:
 * - Partition A: Core functional tests (getDocumentOrder, setPosition with valid inputs)
 * - Partition B: Boundary tests (empty contexts, single context, duplicate pointers)
 * - Partition C: Defect-targeted test (two contexts with out-of-order pointers, verify sorted order)
 * - Partition D: Exception / defensive tests (invalid position, edge-case positions)
 * - Partition E: Lifecycle / state (prepared flag, multiple setPosition calls)
 */

public class UnionContextDeepseekTest {

    // ---------- Helper stubs ----------
    private static class TestNodePointer extends NodePointer {
        private final int order;
        private final String name;

        TestNodePointer(int order, String name) {
            super(null);
            this.order = order;
            this.name = name;
        }

        @Override
        public boolean isLeaf() { return true; }

        @Override
        public boolean isActual() { return true; }

        @Override
        public boolean isCollection() { return false; }

        @Override
        public int getLength() { return 1; }

        @Override
        public Object getNode() { return name; }

        @Override
        public Object getValue() { return name; }

        @Override
        public Object getImmediateNode() { return name; }

        @Override
        public void setValue(Object value) { }

        @Override
        public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }

        @Override
        public int compareTo(Object o) {
            if (o instanceof TestNodePointer) {
                return Integer.compare(this.order, ((TestNodePointer) o).order);
            }
            return 0;
        }

        @Override
        public String asPath() { return name; }

        @Override
        public int hashCode() { return order; }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TestNodePointer) {
                return this.order == ((TestNodePointer) obj).order && this.name.equals(((TestNodePointer) obj).name);
            }
            return false;
        }

        @Override
        public Object getBaseValue() { return null; }

        @Override
        public boolean isPointer() { return false; }

        @Override
        public NodePointer getValuePointer() { return this; }

        @Override
        public NodePointer getImplicitValuePointer() { return this; }
    }

    private static class TestEvalContext extends EvalContext {
        private final List<NodePointer> pointers;
        private int nodeIndex = -1;
        private boolean nextSetReturned = false;

        TestEvalContext(EvalContext parent, List<NodePointer> pointers) {
            super(parent);
            this.pointers = new ArrayList<>(pointers);
        }

        @Override
        public boolean nextSet() {
            if (!nextSetReturned) {
                nextSetReturned = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean nextNode() {
            nodeIndex++;
            return nodeIndex < pointers.size();
        }

        @Override
        public NodePointer getCurrentNodePointer() {
            if (nodeIndex >= 0 && nodeIndex < pointers.size()) {
                return pointers.get(nodeIndex);
            }
            return null;
        }

        @Override
        public boolean setPosition(int position) {
            return true;
        }

        @Override
        public int getDocumentOrder() {
            return 0; // default
        }

        @Override
        public Object getValue() { return null; }

        @Override
        public int getLength() { return pointers.size(); }

        @Override
        public NodePointer getSingleNodePointer() { return null; }

        @Override
        public boolean isSingleton() { return false; }

        @Override
        public boolean isContainer() { return false; }

        @Override
        public boolean isLocked() { return false; }

        @Override
        public void reset() {
            nodeIndex = -1;
            nextSetReturned = false;
        }

        @Override
        public boolean isReset() { return nodeIndex == -1; }

        @Override
        public EvalContext getContext() { return this; }

        @Override
        public Object getContextObject() { return getValue(); }

        @Override
        public NodePointer getContextNodePointer() { return getCurrentNodePointer(); }

        @Override
        public int getCurrentPosition() { return nodeIndex + 1; }

        @Override
        public void setCurrentPosition(int pos) { }

        @Override
        public boolean next() { return nextNode(); }

        @Override
        public boolean previous() { return false; }

        @Override
        public boolean gotoFirstChild() { return false; }

        @Override
        public boolean gotoParent() { return false; }

        @Override
        public boolean gotoNextSibling() { return false; }

        @Override
        public boolean gotoPreviousSibling() { return false; }

        @Override
        public int getChildrenCount() { return 0; }

        @Override
        public Object getThisContextObject() { return null; }

        @Override
        public JXPathContext getJXPathContext() { return null; }

        @Override
        public int getPosition() { return nodeIndex + 1; }

        @Override
        public boolean hasChildNodes() { return false; }
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testGetDocumentOrder_MultipleContexts() {
        EvalContext dummyParent = null;
        EvalContext[] contexts = new EvalContext[2];
        contexts[0] = new TestEvalContext(dummyParent, new ArrayList<>());
        contexts[1] = new TestEvalContext(dummyParent, new ArrayList<>());
        UnionContext union = new UnionContext(dummyParent, contexts);
        assertEquals("Document order should be 1 for multiple contexts", 1, union.getDocumentOrder());
    }

    @Test(timeout = 4000)
    public void testGetDocumentOrder_SingleContext() {
        EvalContext dummyParent = null;
        EvalContext[] contexts = new EvalContext[1];
        contexts[0] = new TestEvalContext(dummyParent, new ArrayList<>());
        UnionContext union = new UnionContext(dummyParent, contexts);
        // NodeSetContext.getDocumentOrder returns 0 by default (since parent is null, we rely on EvalContext default)
        assertEquals("Document order should be 0 for single context", 0, union.getDocumentOrder());
    }

    @Test(timeout = 4000)
    public void testGetDocumentOrder_ZeroContexts() {
        EvalContext dummyParent = null;
        EvalContext[] contexts = new EvalContext[0];
        UnionContext union = new UnionContext(dummyParent, contexts);
        assertEquals("Document order should be 0 for zero contexts", 0, union.getDocumentOrder());
    }

    @Test(timeout = 4000)
    public void testSetPosition_CollectsPointersFromMultipleContexts() {
        EvalContext dummyParent = null;
        List<NodePointer> list1 = new ArrayList<>();
        list1.add(new TestNodePointer(1, "a"));
        list1.add(new TestNodePointer(2, "b"));
        List<NodePointer> list2 = new ArrayList<>();
        list2.add(new TestNodePointer(3, "c"));
        EvalContext ctx1 = new TestEvalContext(dummyParent, list1);
        EvalContext ctx2 = new TestEvalContext(dummyParent, list2);
        EvalContext[] contexts = new EvalContext[]{ctx1, ctx2};
        UnionContext union = new UnionContext(dummyParent, contexts);
        assertTrue("setPosition(1) should succeed", union.setPosition(1));
        // The node set should contain all three pointers in iteration order (not sorted)
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        List pointers = nodeSet.getPointers();
        assertEquals("Should have 3 pointers", 3, pointers.size());
        assertEquals("First pointer should be from context1", "a", ((NodePointer) pointers.get(0)).getValue());
        assertEquals("Second pointer should be from context1", "b", ((NodePointer) pointers.get(1)).getValue());
        assertEquals("Third pointer should be from context2", "c", ((NodePointer) pointers.get(2)).getValue());
    }

    @Test(timeout = 4000)
    public void testSetPosition_DuplicatePointersRemoved() {
        EvalContext dummyParent = null;
        TestNodePointer dup = new TestNodePointer(5, "duplicate");
        List<NodePointer> list1 = new ArrayList<>();
        list1.add(dup);
        List<NodePointer> list2 = new ArrayList<>();
        list2.add(dup);
        EvalContext ctx1 = new TestEvalContext(dummyParent, list1);
        EvalContext ctx2 = new TestEvalContext(dummyParent, list2);
        EvalContext[] contexts = new EvalContext[]{ctx1, ctx2};
        UnionContext union = new UnionContext(dummyParent, contexts);
        assertTrue(union.setPosition(1));
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        List pointers = nodeSet.getPointers();
        assertEquals("Duplicate should be removed, only one pointer", 1, pointers.size());
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testSetPosition_EmptyContexts() {
        EvalContext dummyParent = null;
        EvalContext[] contexts = new EvalContext[0];
        UnionContext union = new UnionContext(dummyParent, contexts);
        // setPosition should not throw and result in empty node set
        assertTrue(union.setPosition(1));
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertTrue("Node set should be empty", nodeSet.getPointers().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetPosition_SingleContextWithNoNodes() {
        EvalContext dummyParent = null;
        List<NodePointer> emptyList = new ArrayList<>();
        EvalContext ctx = new TestEvalContext(dummyParent, emptyList);
        EvalContext[] contexts = new EvalContext[]{ctx};
        UnionContext union = new UnionContext(dummyParent, contexts);
        assertTrue(union.setPosition(1));
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertTrue("Node set should be empty", nodeSet.getPointers().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetPosition_PositionLessThanOne() {
        EvalContext dummyParent = null;
        List<NodePointer> list = new ArrayList<>();
        list.add(new TestNodePointer(1, "x"));
        EvalContext ctx = new TestEvalContext(dummyParent, list);
        EvalContext[] contexts = new EvalContext[]{ctx};
        UnionContext union = new UnionContext(dummyParent, contexts);
        // setPosition(0) should return false (super.setPosition for invalid position)
        assertFalse(union.setPosition(0));
        // Also test negative
        assertFalse(union.setPosition(-1));
    }

    // ========== Partition C: Defect-Targeted Test ==========

    @Test(timeout = 4000)
    public void testSetPosition_DocumentOrderDefect() {
        // Simulate the known Defects4J failure: union of /vendor[1]/contact[4] | /vendor[1]/contact[1]
        // Expected order: contact[1] first (lower document order), then contact[4] (higher document order)
        // Buggy implementation returns contact[4] first due to iteration order.
        EvalContext dummyParent = null;
        // Create pointers with simulated document order: contact[4] has higher order (4) than contact[1] (1)
        TestNodePointer contact1 = new TestNodePointer(1, "John");
        TestNodePointer contact4 = new TestNodePointer(4, "Jack Black");
        // Context for contact[4] (first context)
        List<NodePointer> list1 = new ArrayList<>();
        list1.add(contact4);
        EvalContext ctx1 = new TestEvalContext(dummyParent, list1);
        // Context for contact[1] (second context)
        List<NodePointer> list2 = new ArrayList<>();
        list2.add(contact1);
        EvalContext ctx2 = new TestEvalContext(dummyParent, list2);
        EvalContext[] contexts = new EvalContext[]{ctx1, ctx2};
        UnionContext union = new UnionContext(dummyParent, contexts);
        // Trigger collection by setPosition(1)
        assertTrue(union.setPosition(1));
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        List pointers = nodeSet.getPointers();
        assertEquals("Union should contain exactly 2 pointers", 2, pointers.size());
        // The correct document order should place contact[1] (John) first, then contact[4] (Jack Black)
        NodePointer first = (NodePointer) pointers.get(0);
        NodePointer second = (NodePointer) pointers.get(1);
        assertEquals("First pointer should be contact[1] (John) in document order", "John", first.getValue());
        assertEquals("Second pointer should be contact[4] (Jack Black) in document order", "Jack Black", second.getValue());
        // Defect reveals if order is reversed (Jack Black then John)
    }

    @Test(timeout = 4000)
    public void testSetPosition_DuplicateAndOrderMixed() {
        // Mix duplicates and out-of-order to further stress the defect
        EvalContext dummyParent = null;
        TestNodePointer a = new TestNodePointer(1, "A");
        TestNodePointer b = new TestNodePointer(2, "B");
        TestNodePointer c = new TestNodePointer(3, "C");
        List<NodePointer> list1 = new ArrayList<>();
        list1.add(c); // order 3
        list1.add(a); // order 1
        List<NodePointer> list2 = new ArrayList<>();
        list2.add(b); // order 2
        list2.add(c); // duplicate
        EvalContext ctx1 = new TestEvalContext(dummyParent, list1);
        EvalContext ctx2 = new TestEvalContext(dummyParent, list2);
        EvalContext[] contexts = new EvalContext[]{ctx1, ctx2};
        UnionContext union = new UnionContext(dummyParent, contexts);
        assertTrue(union.setPosition(1));
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        List pointers = nodeSet.getPointers();
        assertEquals("After duplicate removal, should have 3 distinct pointers", 3, pointers.size());
        // In buggy code: order is c, a, b (iteration order). Correct order should be a, b, c.
        NodePointer first = (NodePointer) pointers.get(0);
        NodePointer second = (NodePointer) pointers.get(1);
        NodePointer third = (NodePointer) pointers.get(2);
        assertEquals("First should be A (order 1)", "A", first.getValue());
        assertEquals("Second should be B (order 2)", "B", second.getValue());
        assertEquals("Third should be C (order 3)", "C", third.getValue());
    }

    // ========== Partition D: Exception/Defensive Paths ==========
    // No explicit exception paths in UnionContext, but we can test with invalid contexts array containing nulls
    // However, the class does not guard against null elements – that's a potential defect.
    // We'll test that passing null context element causes NPE during iteration (should be handled by class)
    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSetPosition_NullContextElement() {
        EvalContext dummyParent = null;
        EvalContext[] contexts = new EvalContext[]{null};
        UnionContext union = new UnionContext(dummyParent, contexts);
        union.setPosition(1); // Should throw NullPointerException when iterating
    }

    // ========== Partition E: Lifecycle & State ==========

    @Test(timeout = 4000)
    public void testSetPosition_PreparedFlagPreventsReCollection() {
        EvalContext dummyParent = null;
        List<NodePointer> list = new ArrayList<>();
        list.add(new TestNodePointer(1, "only"));
        EvalContext ctx = new TestEvalContext(dummyParent, list);
        EvalContext[] contexts = new EvalContext[]{ctx};
        UnionContext union = new UnionContext(dummyParent, contexts);
        // First call: collects and sets prepared
        assertTrue(union.setPosition(1));
        // Second call: should not re-collect, just forward to super.setPosition
        // If it re-collected, it would add duplicate pointers
        assertTrue(union.setPosition(1)); // position 1 exists
        BasicNodeSet nodeSet = (BasicNodeSet) union.getNodeSet();
        assertEquals("Pointers should still be only one (not duplicated)", 1, nodeSet.getPointers().size());
    }

    @Test(timeout = 4000)
    public void testSetPosition_PositionTwoAfterCollection() {
        // After collecting one pointer, setting position 2 should return false if only one pointer
        EvalContext dummyParent = null;
        List<NodePointer> list = new ArrayList<>();
        list.add(new TestNodePointer(1, "single"));
        EvalContext ctx = new TestEvalContext(dummyParent, list);
        EvalContext[] contexts = new EvalContext[]{ctx};
        UnionContext union = new UnionContext(dummyParent, contexts);
        assertTrue(union.setPosition(1)); // collects
        assertFalse(union.setPosition(2)); // no second pointer
    }
}