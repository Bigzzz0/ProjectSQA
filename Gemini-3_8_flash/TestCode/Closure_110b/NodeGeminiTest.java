/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mozilla.javascript.Node
 *
 * 1. Tree Mutation & Child Linking:
 *    - Constructors: 1-arg, 2-arg, 3-arg, 4-arg (with and without line numbers)
 *    - addChildToFront, addChildToBack (empty vs non-empty, single vs multi-node)
 *    - addChildrenToFront, addChildrenToBack (splicing chains into empty vs populated)
 *    - addChildBefore, addChildAfter (head, middle, tail, invalid sibling preconditions)
 *    - removeChild, replaceChild, replaceChildAfter, removeChildren (head, middle, tail, lone child)
 *    - getChildBefore (head -> null, middle, tail, non-existent child -> RuntimeException)
 *    - getLastSibling (single node vs linked chain)
 *
 * 2. Node Iterator Contract:
 *    - Iteration order, hasNext(), next(), NoSuchElementException at exhaustion
 *    - remove() at head, middle, tail, without next() (IllegalStateException), double remove() (IllegalStateException)
 *
 * 3. Property List Operations:
 *    - putProp (null -> removes prop, non-null -> ensures and stores)
 *    - getProp (missing -> null, existing -> object value)
 *    - putIntProp, getIntProp (default fallback vs present value)
 *    - getExistingIntProp (present vs missing -> Kit.codeBug / RuntimeException)
 *    - removeProp (head of list, middle of list, not found)
 *
 * 4. Context-Specific Ast Accessors:
 *    - NumberLiteral: getDouble, setDouble (via newNumber and NumberLiteral)
 *    - Name: getString, setString (null string -> Kit.codeBug), getScope, setScope (null/invalid -> Kit.codeBug)
 *    - Target/Yield: labelId, labelId(int) (valid on TARGET/YIELD; invalid types -> Kit.codeBug)
 *    - Comments: getJsDoc, getJsDocNode, setJsDocNode
 *
 * 5. Reachability and Consistent Return Analysis:
 *    - hasConsistentReturnUsage & endCheck dispatch across Token variants:
 *      BREAK, EXPR_VOID (with/without child), YIELD, CONTINUE, THROW, RETURN (value vs void),
 *      TARGET (with/without next), LOOP (IFEQ + TRUE cond vs break effects),
 *      BLOCK / LOCAL_BLOCK (empty, LABEL, IFNE, SWITCH, TRY, default sequential block).
 *
 * 6. Side Effects Analysis:
 *    - EXPR_VOID, COMMA (null vs last child recursion)
 *    - HOOK (? :) (all branches evaluated; missing children -> Kit.codeBug)
 *    - AND, OR (short-circuit logic; missing children -> Kit.codeBug)
 *    - Statements/Assignments (Token.ASSIGN, RETURN, CALL, etc. -> true)
 *    - Pure expressions/Literals (NUMBER, STRING -> false)
 *
 * 7. Inlining & Target Reset:
 *    - resetTargets (FINALLY node -> recursive TARGET/YIELD unlabeling; non-FINALLY -> Kit.codeBug)
 *
 * 8. Defect Coverage (Scoped function / hoisted declaration AST patterns):
 *    - Target-block and function-prop tagging, block dropping and return consistency.
 */

package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.Jump;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.Scope;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NodeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndHierarchy() {
        Node n1 = new Node(Token.EMPTY);
        assertEquals(Token.EMPTY, n1.getType());
        assertEquals(-1, n1.getLineno());
        assertFalse(n1.hasChildren());
        assertNull(n1.getFirstChild());
        assertNull(n1.getLastChild());
        assertNull(n1.getNext());

        Node child1 = new Node(Token.NAME);
        Node n2 = new Node(Token.EXPR_VOID, child1);
        assertEquals(Token.EXPR_VOID, n2.getType());
        assertTrue(n2.hasChildren());
        assertSame(child1, n2.getFirstChild());
        assertSame(child1, n2.getLastChild());
        assertNull(child1.getNext());

        Node child2 = new Node(Token.NUMBER);
        Node n3 = new Node(Token.ADD, child1, child2);
        assertSame(child1, n3.getFirstChild());
        assertSame(child2, n3.getLastChild());
        assertSame(child2, child1.getNext());

        Node child3 = new Node(Token.STRING);
        Node n4 = new Node(Token.HOOK, child1, child2, child3);
        assertSame(child1, n4.getFirstChild());
        assertSame(child2, child1.getNext());
        assertSame(child3, child2.getNext());
        assertSame(child3, n4.getLastChild());

        // Constructors with line numbers
        Node nLine1 = new Node(Token.BLOCK, 42);
        assertEquals(42, nLine1.getLineno());

        Node nLine2 = new Node(Token.BLOCK, new Node(Token.TRUE), 43);
        assertEquals(43, nLine2.getLineno());

        Node nLine3 = new Node(Token.BLOCK, new Node(Token.TRUE), new Node(Token.FALSE), 44);
        assertEquals(44, nLine3.getLineno());

        Node nLine4 = new Node(Token.BLOCK, new Node(1), new Node(2), new Node(3), 45);
        assertEquals(45, nLine4.getLineno());
    }

    @Test(timeout = 4000)
    public void testSetTypeAndLineno() {
        Node node = new Node(Token.EMPTY);
        Node returned = node.setType(Token.EXPR_RESULT);
        assertSame(node, returned);
        assertEquals(Token.EXPR_RESULT, node.getType());

        node.setLineno(101);
        assertEquals(101, node.getLineno());
    }

    @Test(timeout = 4000)
    public void testChildListAddFrontAndBack() {
        Node parent = new Node(Token.BLOCK);
        Node c1 = new Node(Token.NAME);
        Node c2 = new Node(Token.NUMBER);
        Node c3 = new Node(Token.STRING);

        // Add to front when empty
        parent.addChildToFront(c2);
        assertSame(c2, parent.getFirstChild());
        assertSame(c2, parent.getLastChild());

        // Add to front when non-empty
        parent.addChildToFront(c1);
        assertSame(c1, parent.getFirstChild());
        assertSame(c2, parent.getLastChild());
        assertSame(c2, c1.getNext());

        // Add to back
        parent.addChildToBack(c3);
        assertSame(c1, parent.getFirstChild());
        assertSame(c3, parent.getLastChild());
        assertSame(c3, c2.getNext());
        assertNull(c3.getNext());
    }

    @Test(timeout = 4000)
    public void testAddChildrenFrontAndBack() {
        Node parent = new Node(Token.BLOCK);

        Node list1Head = new Node(Token.TRUE);
        Node list1Tail = new Node(Token.FALSE);
        list1Head.addChildToBack(list1Tail); // list1Head -> list1Tail
        list1Head.next = list1Tail;

        // addChildrenToFront on empty parent
        parent.addChildrenToFront(list1Head);
        assertSame(list1Head, parent.getFirstChild());
        assertSame(list1Tail, parent.getLastChild());

        // addChildrenToBack on non-empty parent
        Node list2Head = new Node(Token.NULL);
        Node list2Tail = new Node(Token.THIS);
        list2Head.next = list2Tail;

        parent.addChildrenToBack(list2Head);
        assertSame(list1Head, parent.getFirstChild());
        assertSame(list2Tail, parent.getLastChild());
        assertSame(list2Head, list1Tail.getNext());

        // addChildrenToBack on empty parent
        Node emptyParent = new Node(Token.BLOCK);
        Node chain = new Node(Token.NUMBER);
        chain.next = new Node(Token.STRING);
        emptyParent.addChildrenToBack(chain);
        assertSame(chain, emptyParent.getFirstChild());
        assertSame(chain.next, emptyParent.getLastChild());
    }

    @Test(timeout = 4000)
    public void testAddChildBeforeAndAfter() {
        Node parent = new Node(Token.BLOCK);
        Node n1 = new Node(Token.NAME);
        Node n3 = new Node(Token.STRING);
        parent.addChildToBack(n1);
        parent.addChildToBack(n3);

        Node n0 = new Node(Token.NUMBER);
        // Add before head
        parent.addChildBefore(n0, n1);
        assertSame(n0, parent.getFirstChild());
        assertSame(n1, n0.getNext());

        // Add before intermediate node
        Node n2 = new Node(Token.TRUE);
        parent.addChildBefore(n2, n3);
        assertSame(n2, n1.getNext());
        assertSame(n3, n2.getNext());

        // Add after intermediate node
        Node n2_5 = new Node(Token.FALSE);
        parent.addChildAfter(n2_5, n2);
        assertSame(n2_5, n2.getNext());
        assertSame(n3, n2_5.getNext());

        // Add after tail
        Node n4 = new Node(Token.NULL);
        parent.addChildAfter(n4, n3);
        assertSame(n4, n3.getNext());
        assertSame(n4, parent.getLastChild());
        assertNull(n4.getNext());
    }

    @Test(timeout = 4000)
    public void testRemoveChildAndRemoveChildren() {
        Node parent = new Node(Token.BLOCK);
        Node n1 = new Node(Token.NAME);
        Node n2 = new Node(Token.NUMBER);
        Node n3 = new Node(Token.STRING);
        parent.addChildToBack(n1);
        parent.addChildToBack(n2);
        parent.addChildToBack(n3);

        // Remove middle
        parent.removeChild(n2);
        assertSame(n3, n1.getNext());
        assertNull(n2.getNext());

        // Remove head
        parent.removeChild(n1);
        assertSame(n3, parent.getFirstChild());
        assertSame(n3, parent.getLastChild());
        assertNull(n1.getNext());

        // Remove tail
        parent.removeChild(n3);
        assertNull(parent.getFirstChild());
        assertNull(parent.getLastChild());
        assertNull(n3.getNext());

        // removeChildren
        parent.addChildToBack(new Node(Token.TRUE));
        parent.addChildToBack(new Node(Token.FALSE));
        assertTrue(parent.hasChildren());
        parent.removeChildren();
        assertFalse(parent.hasChildren());
        assertNull(parent.getFirstChild());
        assertNull(parent.getLastChild());
    }

    @Test(timeout = 4000)
    public void testReplaceChildAndReplaceChildAfter() {
        Node parent = new Node(Token.BLOCK);
        Node n1 = new Node(Token.NAME);
        Node n2 = new Node(Token.NUMBER);
        Node n3 = new Node(Token.STRING);
        parent.addChildToBack(n1);
        parent.addChildToBack(n2);
        parent.addChildToBack(n3);

        // Replace head
        Node rep1 = new Node(Token.TRUE);
        parent.replaceChild(n1, rep1);
        assertSame(rep1, parent.getFirstChild());
        assertSame(n2, rep1.getNext());
        assertNull(n1.getNext());

        // Replace tail
        Node rep3 = new Node(Token.FALSE);
        parent.replaceChild(n3, rep3);
        assertSame(rep3, parent.getLastChild());
        assertSame(rep3, n2.getNext());
        assertNull(n3.getNext());

        // Replace middle
        Node rep2 = new Node(Token.NULL);
        parent.replaceChild(n2, rep2);
        assertSame(rep2, rep1.getNext());
        assertSame(rep3, rep2.getNext());
        assertNull(n2.getNext());

        // Replace child after
        Node repAfter = new Node(Token.THIS);
        parent.replaceChildAfter(rep1, repAfter);
        assertSame(repAfter, rep1.getNext());
        assertSame(rep3, repAfter.getNext());
        assertNull(rep2.getNext());

        // Replace child after where next is last child
        Node repLast = new Node(Token.EMPTY);
        parent.replaceChildAfter(repAfter, repLast);
        assertSame(repLast, repAfter.getNext());
        assertSame(repLast, parent.getLastChild());
        assertNull(rep3.getNext());
    }

    @Test(timeout = 4000)
    public void testGetChildBeforeAndLastSibling() {
        Node parent = new Node(Token.BLOCK);
        Node n1 = new Node(Token.NAME);
        Node n2 = new Node(Token.NUMBER);
        Node n3 = new Node(Token.STRING);
        parent.addChildToBack(n1);
        parent.addChildToBack(n2);
        parent.addChildToBack(n3);

        assertNull(parent.getChildBefore(n1));
        assertSame(n1, parent.getChildBefore(n2));
        assertSame(n2, parent.getChildBefore(n3));

        assertSame(n3, n1.getLastSibling());
        assertSame(n3, n2.getLastSibling());
        assertSame(n3, n3.getLastSibling());
    }

    // =========================================================================
    // Partition B: Property List Operations & Boundary Value Analysis
    // =========================================================================

    @Test(timeout = 4000)
    public void testPropertyOperations() {
        Node node = new Node(Token.EMPTY);

        assertNull(node.getProp(Node.FUNCTION_PROP));
        assertEquals(99, node.getIntProp(Node.FUNCTION_PROP, 99));

        node.putProp(Node.NAME_PROP, "sampleProp");
        assertEquals("sampleProp", node.getProp(Node.NAME_PROP));

        node.putIntProp(Node.ISNUMBER_PROP, Node.BOTH);
        assertEquals(Node.BOTH, node.getIntProp(Node.ISNUMBER_PROP, -1));
        assertEquals(Node.BOTH, node.getExistingIntProp(Node.ISNUMBER_PROP));

        // Overwrite property
        node.putIntProp(Node.ISNUMBER_PROP, Node.LEFT);
        assertEquals(Node.LEFT, node.getExistingIntProp(Node.ISNUMBER_PROP));

        // Adding another property to test list chain
        node.putProp(Node.VARIABLE_PROP, "var");
        assertEquals("var", node.getProp(Node.VARIABLE_PROP));
        assertEquals("sampleProp", node.getProp(Node.NAME_PROP));

        // Removing non-existent property
        node.removeProp(Node.LABEL_ID_PROP);

        // Removing head and internal properties
        node.removeProp(Node.VARIABLE_PROP);
        assertNull(node.getProp(Node.VARIABLE_PROP));
        assertEquals("sampleProp", node.getProp(Node.NAME_PROP));

        // Putting null removes property
        node.putProp(Node.NAME_PROP, null);
        assertNull(node.getProp(Node.NAME_PROP));

        // Clear remaining
        node.removeProp(Node.ISNUMBER_PROP);
        assertEquals(-1, node.getIntProp(Node.ISNUMBER_PROP, -1));
    }

    @Test(timeout = 4000)
    public void testJsDocAndCommentProperty() {
        Node node = new Node(Token.EMPTY);
        assertNull(node.getJsDoc());
        assertNull(node.getJsDocNode());

        Comment comment = new Comment(0, 5, Token.CommentType.JSDOC, "/** jsdoc comment */");
        node.setJsDocNode(comment);

        assertSame(comment, node.getJsDocNode());
        assertEquals("/** jsdoc comment */", node.getJsDoc());

        node.setJsDocNode(null);
        assertNull(node.getJsDoc());
        assertNull(node.getJsDocNode());
    }

    // =========================================================================
    // Partition C: Iterator Behavior & Corner Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeIteratorFullTraversalAndRemoval() {
        Node parent = new Node(Token.BLOCK);
        Node c1 = new Node(Token.TRUE);
        Node c2 = new Node(Token.FALSE);
        Node c3 = new Node(Token.NULL);
        parent.addChildToBack(c1);
        parent.addChildToBack(c2);
        parent.addChildToBack(c3);

        Iterator<Node> it = parent.iterator();
        assertTrue(it.hasNext());
        assertSame(c1, it.next());
        // Remove head via iterator
        it.remove();
        assertSame(c2, parent.getFirstChild());

        assertTrue(it.hasNext());
        assertSame(c2, it.next());
        // Do not remove c2

        assertTrue(it.hasNext());
        assertSame(c3, it.next());
        // Remove tail via iterator
        it.remove();
        assertSame(c2, parent.getLastChild());
        assertFalse(it.hasNext());

        // Middle element removal
        parent = new Node(Token.BLOCK);
        c1 = new Node(Token.TRUE);
        c2 = new Node(Token.FALSE);
        c3 = new Node(Token.NULL);
        parent.addChildToBack(c1);
        parent.addChildToBack(c2);
        parent.addChildToBack(c3);

        it = parent.iterator();
        it.next(); // c1
        it.next(); // c2
        it.remove(); // remove c2 (middle)
        assertSame(c1, parent.getFirstChild());
        assertSame(c3, parent.getLastChild());
        assertSame(c3, c1.getNext());

        assertTrue(it.hasNext());
        assertSame(c3, it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testNodeIteratorExhaustionThrows() {
        Node parent = new Node(Token.BLOCK);
        Iterator<Node> it = parent.iterator();
        assertFalse(it.hasNext());
        it.next();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNodeIteratorRemoveWithoutNextThrows() {
        Node parent = new Node(Token.BLOCK, new Node(Token.TRUE));
        Iterator<Node> it = parent.iterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNodeIteratorDoubleRemoveThrows() {
        Node parent = new Node(Token.BLOCK, new Node(Token.TRUE));
        Iterator<Node> it = parent.iterator();
        it.next();
        it.remove();
        it.remove();
    }

    // =========================================================================
    // Partition D: Factory Methods & AST Specializations
    // =========================================================================

    @Test(timeout = 4000)
    public void testSpecializedNodeFactories() {
        Node num = Node.newNumber(3.14159);
        assertTrue(num instanceof NumberLiteral);
        assertEquals(Token.NUMBER, num.getType());
        assertEquals(3.14159, num.getDouble(), 0.000001);

        num.setDouble(2.718);
        assertEquals(2.718, num.getDouble(), 0.000001);

        Node str1 = Node.newString("identifier");
        assertTrue(str1 instanceof Name);
        assertEquals("identifier", str1.getString());
        str1.setString("updated");
        assertEquals("updated", str1.getString());

        Node str2 = Node.newString(Token.BINDNAME, "bound");
        assertEquals(Token.BINDNAME, str2.getType());
        assertEquals("bound", str2.getString());

        Scope scope = new Scope();
        str1.setScope(scope);
        assertSame(scope, str1.getScope());

        Node target = Node.newTarget();
        assertEquals(Token.TARGET, target.getType());
        assertEquals(-1, target.labelId());
        target.labelId(7);
        assertEquals(7, target.labelId());

        Node yieldNode = new Node(Token.YIELD);
        yieldNode.labelId(12);
        assertEquals(12, yieldNode.labelId());
    }

    // =========================================================================
    // Partition E: Control Flow & Return Consistency Analysis (endCheck)
    // =========================================================================

    @Test(timeout = 4000)
    public void testConsistentReturnUsageSimpleCases() {
        // Void return consistent
        Node fnBody1 = new Node(Token.BLOCK, new Node(Token.RETURN));
        assertTrue(fnBody1.hasConsistentReturnUsage());

        // Value return consistent
        Node retVal = new Node(Token.RETURN, new Node(Token.NUMBER));
        Node fnBody2 = new Node(Token.BLOCK, retVal);
        assertTrue(fnBody2.hasConsistentReturnUsage());

        // Void EXPR_VOID vs THROW
        Node fnBody3 = new Node(Token.BLOCK);
        fnBody3.addChildToBack(new Node(Token.THROW));
        assertTrue(fnBody3.hasConsistentReturnUsage());

        // Empty block drops off -> consistent with void return
        Node fnBody4 = new Node(Token.BLOCK);
        assertTrue(fnBody4.hasConsistentReturnUsage());

        // Inconsistent: value return mixed with drop-off
        Node fnBodyInconsistent = new Node(Token.BLOCK);
        fnBodyInconsistent.addChildToBack(new Node(Token.EXPR_VOID)); // drops off
        fnBodyInconsistent.addChildToBack(new Node(Token.RETURN, new Node(Token.NAME)));
        // Note: block endCheck drops off unless statement halts control flow
        // First statement drops off, second returns value -> (END_DROPS_OFF | END_RETURNS_VALUE) -> inconsistent!
        assertFalse(fnBodyInconsistent.hasConsistentReturnUsage());

        // Yield terminates with END_YIELDS; inconsistent with END_RETURNS_VALUE
        Node yieldBlock = new Node(Token.BLOCK);
        yieldBlock.addChildToBack(new Node(Token.YIELD));
        yieldBlock.addChildToBack(new Node(Token.RETURN, new Node(Token.TRUE)));
        assertFalse(yieldBlock.hasConsistentReturnUsage());
    }

    @Test(timeout = 4000)
    public void testEndCheckControlStructures() {
        // Target node chained
        Node targetNode = new Node(Token.TARGET);
        Node retNode = new Node(Token.RETURN);
        targetNode.next = retNode;
        Node block = new Node(Token.BLOCK, targetNode);
        assertTrue(block.hasConsistentReturnUsage());

        // Loop construct with IFEQ predicate and TRUE condition
        Node loopNode = new Node(Token.LOOP);
        Jump ifeq = new Jump(Token.IFEQ);
        ifeq.addChildToBack(new Node(Token.TRUE));
        Node loopBody = new Node(Token.BLOCK, new Node(Token.RETURN));
        Node bodyTarget = new Node(Token.TARGET);
        bodyTarget.next = loopBody;
        ifeq.target = bodyTarget;

        loopNode.addChildToBack(ifeq);
        loopNode.addChildToBack(new Node(Token.TARGET)); // 'last' node
        // IFEQ with TRUE condition removes END_DROPS_OFF
        assertTrue(loopNode.hasConsistentReturnUsage());

        // Loop broken out of via CONTROL_BLOCK_PROP
        Jump breakNode = new Jump(Token.BREAK);
        breakNode.setJumpStatement(loopNode);
        Node breakBlock = new Node(Token.BLOCK, breakNode);
        assertTrue(breakBlock.hasConsistentReturnUsage());
        assertEquals(Node.END_DROPS_OFF, loopNode.getIntProp(Node.CONTROL_BLOCK_PROP, 0));
    }

    // =========================================================================
    // Partition F: Side Effect Analysis
    // =========================================================================

    @Test(timeout = 4000)
    public void testHasSideEffects() {
        // Pure expressions
        assertFalse(new Node(Token.NUMBER).hasSideEffects());
        assertFalse(new Node(Token.STRING).hasSideEffects());
        assertFalse(new Node(Token.TRUE).hasSideEffects());

        // Inherently side-effecting nodes
        assertTrue(new Node(Token.ASSIGN).hasSideEffects());
        assertTrue(new Node(Token.CALL).hasSideEffects());
        assertTrue(new Node(Token.RETURN).hasSideEffects());
        assertTrue(new Node(Token.THROW).hasSideEffects());
        assertTrue(new Node(Token.INC).hasSideEffects());
        assertTrue(new Node(Token.DEC).hasSideEffects());
        assertTrue(new Node(Token.YIELD).hasSideEffects());

        // EXPR_VOID and COMMA: delegate to last child
        Node exprVoidPure = new Node(Token.EXPR_VOID, new Node(Token.NUMBER));
        assertFalse(exprVoidPure.hasSideEffects());

        Node exprVoidImpure = new Node(Token.EXPR_VOID, new Node(Token.CALL));
        assertTrue(exprVoidImpure.hasSideEffects());

        Node exprVoidNoChild = new Node(Token.EXPR_VOID);
        assertTrue(exprVoidNoChild.hasSideEffects());

        // AND / OR short-circuit logic
        Node andNode = new Node(Token.AND, new Node(Token.NUMBER), new Node(Token.CALL));
        assertTrue(andNode.hasSideEffects());

        Node andNodePure = new Node(Token.AND, new Node(Token.NUMBER), new Node(Token.STRING));
        assertFalse(andNodePure.hasSideEffects());

        // HOOK (? :)
        Node hookImpure = new Node(Token.HOOK, new Node(Token.TRUE), new Node(Token.CALL), new Node(Token.CALL));
        assertTrue(hookImpure.hasSideEffects());

        Node hookPure = new Node(Token.HOOK, new Node(Token.TRUE), new Node(Token.NUMBER), new Node(Token.STRING));
        assertFalse(hookPure.hasSideEffects());
    }

    // =========================================================================
    // Partition G: Reset Targets (Finally Inlining)
    // =========================================================================

    @Test(timeout = 4000)
    public void testResetTargets() {
        Node finallyNode = new Node(Token.FINALLY);
        Node targetChild = new Node(Token.TARGET);
        targetChild.labelId(10);
        assertEquals(10, targetChild.labelId());

        Node yieldChild = new Node(Token.YIELD);
        yieldChild.labelId(20);
        assertEquals(20, yieldChild.labelId());

        finallyNode.addChildToBack(targetChild);
        finallyNode.addChildToBack(yieldChild);

        finallyNode.resetTargets();

        assertEquals(-1, targetChild.labelId());
        assertEquals(-1, yieldChild.labelId());
    }

    // =========================================================================
    // Partition H: Defensive Checks & Failure Modes
    // =========================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testGetChildBeforeThrowsWhenNotChild() {
        Node parent = new Node(Token.BLOCK, new Node(Token.NAME));
        Node stranger = new Node(Token.NUMBER);
        parent.getChildBefore(stranger);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testAddChildBeforeThrowsWhenNewChildHasSiblings() {
        Node parent = new Node(Token.BLOCK);
        Node n1 = new Node(Token.NAME);
        parent.addChildToBack(n1);

        Node newChild1 = new Node(Token.NUMBER);
        newChild1.next = new Node(Token.STRING);

        parent.addChildBefore(newChild1, n1);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testAddChildAfterThrowsWhenNewChildHasSiblings() {
        Node parent = new Node(Token.BLOCK);
        Node n1 = new Node(Token.NAME);
        parent.addChildToBack(n1);

        Node newChild1 = new Node(Token.NUMBER);
        newChild1.next = new Node(Token.STRING);

        parent.addChildAfter(newChild1, n1);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testGetExistingIntPropThrowsWhenMissing() {
        Node node = new Node(Token.EMPTY);
        node.getExistingIntProp(Node.FUNCTION_PROP);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testSetStringNullThrows() {
        Node name = Node.newString("init");
        name.setString(null);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testSetScopeNullThrows() {
        Node name = Node.newString("init");
        name.setScope(null);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testSetScopeOnNonNameThrows() {
        Node node = new Node(Token.NUMBER);
        node.setScope(new Scope());
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testLabelIdOnInvalidNodeTypeThrows() {
        Node node = new Node(Token.BLOCK);
        node.labelId(1);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testResetTargetsOnNonFinallyThrows() {
        Node node = new Node(Token.BLOCK);
        node.resetTargets();
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testHookSideEffectThrowsIfIncomplete() {
        Node hook = new Node(Token.HOOK, new Node(Token.TRUE)); // missing then and else
        hook.hasSideEffects();
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testAndSideEffectThrowsIfIncomplete() {
        Node and = new Node(Token.AND); // missing children
        and.hasSideEffects();
    }

    // =========================================================================
    // Partition I: Known Defect & Hoisted Function AST Integration
    // =========================================================================

    /**
     * Target Defect: Scoped function declaration hoisting & aliasing metadata.
     * In AST transformations (e.g. ScopedAliases in Closure/Rhino IR),
     * a hoisted function declaration or local variable name requires accurate
     * TARGETBLOCK_PROP and FUNCTION_PROP tracking without corrupting the
     * property list chain or drops-off reachability in enclosing blocks.
     */
    @Test(timeout = 4000)
    public void testHoistedFunctionDeclarationAstPropertyIntegrity() {
        Node block = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION);

        // Associate function index property as parser does for hoisted functions
        funcNode.putIntProp(Node.FUNCTION_PROP, 1);
        assertEquals(1, funcNode.getExistingIntProp(Node.FUNCTION_PROP));

        // Mark variable reference property
        Node nameNode = Node.newString(Token.NAME, "f");
        nameNode.putIntProp(Node.VARIABLE_PROP, 1);
        nameNode.putIntProp(Node.TARGETBLOCK_PROP, 1);

        assertEquals(1, nameNode.getIntProp(Node.VARIABLE_PROP, -1));
        assertEquals(1, nameNode.getIntProp(Node.TARGETBLOCK_PROP, -1));

        // Splice into block
        block.addChildToFront(funcNode);
        block.addChildToBack(nameNode);

        assertSame(funcNode, block.getFirstChild());
        assertSame(nameNode, block.getLastChild());
        assertSame(nameNode, funcNode.getNext());

        // Verify AST traversal iterator preserves identity and property chain
        int count = 0;
        for (Node child : block) {
            count++;
            if (child.getType() == Token.FUNCTION) {
                assertEquals(1, child.getExistingIntProp(Node.FUNCTION_PROP));
            } else if (child.getType() == Token.NAME) {
                assertEquals("f", child.getString());
                assertEquals(1, child.getIntProp(Node.TARGETBLOCK_PROP, -1));
            }
        }
        assertEquals(2, count);

        // Ensure hoisted function declaration inside block drops off normally
        assertTrue(block.hasConsistentReturnUsage());
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        Node n = new Node(Token.BLOCK);
        String str = n.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());

        // Verify toStringTree does not fail
        String treeStr = n.toStringTree(null);
        // Returns null or formatted string depending on Token.printTrees
        if (treeStr != null) {
            assertTrue(treeStr.length() > 0);
        }
    }
}