package org.apache.commons.jxpath.ri.axes;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: AttributeContext (org.apache.commons.jxpath.ri.axes)
 * 
 * Decision branches identified:
 * 1. setPosition(int position): 
 *    - position < getCurrentPosition() -> reset() path
 *    - position >= getCurrentPosition() -> while loop path
 *    - while loop condition: getCurrentPosition() < position
 *    - nextNode() returns false -> return false
 *    - nextNode() returns true -> continue loop
 *    - loop completes -> return true
 * 
 * 2. nextNode():
 *    - setStarted == false -> initialize iterator
 *    - setStarted == true -> skip initialization
 *    - nodeTest instanceof NodeNameTest -> proceed
 *    - nodeTest NOT instanceof NodeNameTest -> return false
 *    - iterator == null -> return false
 *    - iterator.setPosition(iterator.getPosition() + 1) fails -> return false
 *    - iterator.setPosition succeeds -> set currentNodePointer, return true
 * 
 * 3. reset():
 *    - setStarted = false
 *    - iterator = null
 *    - super.reset() called
 * 
 * Boundary conditions:
 * - position = 0 (initial state)
 * - position = 1 (first valid position)
 * - position = negative (invalid)
 * - position = Integer.MAX_VALUE (extreme)
 * - nodeTest = null (invalid)
 * - nodeTest = NodeNameTest with null name
 * - nodeTest = non-NodeNameTest implementation
 * - iterator returns null pointer
 * - iterator position boundaries
 * 
 * Defect targeting (from Defects4J):
 * - testAxisAttribute failure: attribute::node() returns empty list
 *   Expected: [10%, 20%] but was: []
 *   Root cause: When nodeTest is a NodeNameTest with wildcard name,
 *   the iterator may not be properly initialized or the attribute
 *   iteration fails to find attributes.
 * 
 * Test strategy:
 * - Partition A: Core functional logic (nextNode, setPosition, reset)
 * - Partition B: Boundary values (null, empty, extremes)
 * - Partition C: Defect-specific test for attribute::node() wildcard
 * - Partition D: Exception paths (invalid nodeTest)
 * - Partition E: Lifecycle (reset after partial iteration)
 */

public class AttributeContextDeepseekTest {

    // Helper to create a minimal EvalContext for testing
    private static class TestEvalContext extends EvalContext {
        private NodePointer pointer;
        
        TestEvalContext(NodePointer pointer) {
            super(null);
            this.pointer = pointer;
        }
        
        @Override
        public NodePointer getCurrentNodePointer() {
            return pointer;
        }
        
        @Override
        public boolean nextNode() {
            return false;
        }
        
        @Override
        public boolean setPosition(int position) {
            return position == 0;
        }
        
        @Override
        public int getCurrentPosition() {
            return 0;
        }
    }
    
    // Minimal NodePointer implementation for testing
    private static class TestNodePointer extends NodePointer {
        private NodeIterator attrIterator;
        
        TestNodePointer(NodeIterator attrIterator) {
            super(null);
            this.attrIterator = attrIterator;
        }
        
        @Override
        public NodeIterator attributeIterator(QName name) {
            return attrIterator;
        }
        
        @Override
        public Object getValue() {
            return null;
        }
        
        @Override
        public Object getBaseValue() {
            return null;
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
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }
        
        @Override
        public Object getImmediateNode() {
            return null;
        }
        
        @Override
        public boolean isCollection() {
            return false;
        }
        
        @Override
        public int getLength() {
            return 0;
        }
        
        @Override
        public void setValue(Object value) {
        }
    }
    
    // Simple NodeIterator implementation
    private static class TestNodeIterator implements NodeIterator {
        private int position = 0;
        private int maxPosition;
        private NodePointer pointer;
        
        TestNodeIterator(int maxPosition, NodePointer pointer) {
            this.maxPosition = maxPosition;
            this.pointer = pointer;
        }
        
        @Override
        public NodePointer getNodePointer() {
            return pointer;
        }
        
        @Override
        public boolean setPosition(int position) {
            if (position <= 0 || position > maxPosition) {
                return false;
            }
            this.position = position;
            return true;
        }
        
        @Override
        public int getPosition() {
            return position;
        }
    }
    
    // Simple NodeNameTest implementation
    private static class TestNodeNameTest extends NodeNameTest {
        private QName name;
        
        TestNodeNameTest(QName name) {
            super(name);
            this.name = name;
        }
        
        @Override
        public QName getNodeName() {
            return name;
        }
        
        @Override
        public boolean isWildcard() {
            return name == null || name.getName().equals("*");
        }
    }
    
    // Non-NodeNameTest implementation
    private static class NonNodeNameTest implements NodeTest {
    }
    
    // ========== Partition A: Core Functional Logic ==========
    
    @Test(timeout = 4000)
    public void testNextNodeWithValidNodeNameTest() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(2, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("First nextNode should succeed", context.nextNode());
        assertNotNull("Current node pointer should be set", context.getCurrentNodePointer());
        assertEquals("Position should be 1", 1, context.getCurrentPosition());
        
        assertTrue("Second nextNode should succeed", context.nextNode());
        assertEquals("Position should be 2", 2, context.getCurrentPosition());
        
        assertFalse("Third nextNode should fail", context.nextNode());
    }
    
    @Test(timeout = 4000)
    public void testNextNodeWithWildcardName() {
        QName name = new QName(null, "*");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(3, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("Wildcard should match all attributes", context.nextNode());
        assertTrue("Second attribute should be found", context.nextNode());
        assertTrue("Third attribute should be found", context.nextNode());
        assertFalse("No more attributes", context.nextNode());
    }
    
    @Test(timeout = 4000)
    public void testSetPositionForward() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(5, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("Set position to 3", context.setPosition(3));
        assertEquals("Position should be 3", 3, context.getCurrentPosition());
        assertNotNull("Current node pointer should be set", context.getCurrentNodePointer());
    }
    
    @Test(timeout = 4000)
    public void testSetPositionBackward() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(5, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("Move to position 4", context.setPosition(4));
        assertTrue("Move back to position 2", context.setPosition(2));
        assertEquals("Position should be 2", 2, context.getCurrentPosition());
    }
    
    @Test(timeout = 4000)
    public void testReset() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(3, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("Move to position 2", context.setPosition(2));
        context.reset();
        assertEquals("Position should be 0 after reset", 0, context.getCurrentPosition());
        assertTrue("Should be able to iterate again", context.nextNode());
    }
    
    // ========== Partition B: Boundary Value Analysis ==========
    
    @Test(timeout = 4000)
    public void testSetPositionToZero() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(1, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("Set position to 0", context.setPosition(0));
        assertEquals("Position should be 0", 0, context.getCurrentPosition());
    }
    
    @Test(timeout = 4000)
    public void testSetPositionBeyondLimit() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(2, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertFalse("Set position beyond limit should fail", context.setPosition(10));
    }
    
    @Test(timeout = 4000)
    public void testSetPositionNegative() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(2, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertFalse("Negative position should fail", context.setPosition(-1));
    }
    
    @Test(timeout = 4000)
    public void testSetPositionMaxValue() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(2, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertFalse("MAX_VALUE position should fail", context.setPosition(Integer.MAX_VALUE));
    }
    
    @Test(timeout = 4000)
    public void testNextNodeWithNullNodeTest() {
        TestNodeIterator iterator = new TestNodeIterator(1, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, null);
        
        assertFalse("Null nodeTest should return false", context.nextNode());
    }
    
    @Test(timeout = 4000)
    public void testNextNodeWithNullIterator() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodePointer pointer = new TestNodePointer(null);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertFalse("Null iterator should return false", context.nextNode());
    }
    
    // ========== Partition C: Defect-Targeted Tests ==========
    
    @Test(timeout = 4000)
    public void testAttributeNodeWildcardDefect() {
        // This test targets the specific defect where attribute::node()
        // returns empty list instead of [10%, 20%]
        QName wildcardName = new QName(null, "*");
        TestNodeNameTest nodeTest = new TestNodeNameTest(wildcardName);
        
        // Simulate an iterator that returns two attribute nodes
        TestNodeIterator iterator = new TestNodeIterator(2, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        // The defect causes this to return false immediately
        // Correct behavior: should iterate through both attributes
        assertTrue("First attribute should be found with wildcard", context.nextNode());
        assertNotNull("First attribute pointer should not be null", context.getCurrentNodePointer());
        
        assertTrue("Second attribute should be found with wildcard", context.nextNode());
        assertNotNull("Second attribute pointer should not be null", context.getCurrentNodePointer());
        
        assertFalse("No more attributes after two", context.nextNode());
    }
    
    @Test(timeout = 4000)
    public void testAttributeNodeWildcardWithMultipleIterations() {
        // More comprehensive defect test
        QName wildcardName = new QName(null, "*");
        TestNodeNameTest nodeTest = new TestNodeNameTest(wildcardName);
        
        TestNodeIterator iterator = new TestNodeIterator(3, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        int count = 0;
        while (context.nextNode()) {
            count++;
            assertNotNull("Pointer should be set", context.getCurrentNodePointer());
        }
        
        assertEquals("Should find exactly 3 attributes", 3, count);
    }
    
    @Test(timeout = 4000)
    public void testAttributeNodeWildcardAfterReset() {
        QName wildcardName = new QName(null, "*");
        TestNodeNameTest nodeTest = new TestNodeNameTest(wildcardName);
        
        TestNodeIterator iterator = new TestNodeIterator(2, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("First iteration", context.nextNode());
        context.reset();
        
        assertTrue("Should work after reset", context.nextNode());
        assertNotNull("Pointer should be set after reset", context.getCurrentNodePointer());
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(timeout = 4000)
    public void testNonNodeNameTest() {
        NonNodeNameTest nodeTest = new NonNodeNameTest();
        TestNodeIterator iterator = new TestNodeIterator(1, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertFalse("Non-NodeNameTest should return false", context.nextNode());
    }
    
    @Test(timeout = 4000)
    public void testNodeNameTestWithNullName() {
        TestNodeNameTest nodeTest = new TestNodeNameTest(null);
        TestNodeIterator iterator = new TestNodeIterator(1, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("Null name should still work", context.nextNode());
    }
    
    @Test(timeout = 4000)
    public void testIteratorFailsOnFirstPosition() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(0, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertFalse("Iterator with zero positions should fail", context.nextNode());
    }
    
    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    
    @Test(timeout = 4000)
    public void testMultipleResets() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(2, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("First iteration", context.nextNode());
        context.reset();
        assertTrue("Second iteration after reset", context.nextNode());
        context.reset();
        assertTrue("Third iteration after second reset", context.nextNode());
    }
    
    @Test(timeout = 4000)
    public void testSetPositionAfterReset() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(3, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertTrue("Move to position 2", context.setPosition(2));
        context.reset();
        assertTrue("Set position after reset", context.setPosition(1));
        assertEquals("Position should be 1", 1, context.getCurrentPosition());
    }
    
    @Test(timeout = 4000)
    public void testGetCurrentNodePointerInitialState() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        TestNodeIterator iterator = new TestNodeIterator(1, new TestNodePointer(null));
        TestNodePointer pointer = new TestNodePointer(iterator);
        TestEvalContext parent = new TestEvalContext(pointer);
        
        AttributeContext context = new AttributeContext(parent, nodeTest);
        
        assertNull("Initial pointer should be null", context.getCurrentNodePointer());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithNullParent() {
        QName name = new QName("test", "attr");
        TestNodeNameTest nodeTest = new TestNodeNameTest(name);
        
        AttributeContext context = new AttributeContext(null, nodeTest);
        
        assertFalse("Null parent should cause failure", context.nextNode());
    }
}