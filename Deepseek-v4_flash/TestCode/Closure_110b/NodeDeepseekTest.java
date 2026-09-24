package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

public class NodeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partitions targeted:
     * A: Constructors (7 signatures), getType/setType, hasChildren, getFirstChild, getLastChild, getNext
     * B: Tree manipulation: addChildToFront, addChildToBack, addChildrenToFront, addChildrenToBack, addChildBefore, addChildAfter, removeChild, replaceChild, replaceChildAfter, removeChildren, getChildBefore, getLastSibling
     * C: NodeIterator: hasNext, next, remove (including error states: NoSuchElementException, IllegalStateException)
     * D: Property management: getProp, putProp, removeProp, getIntProp, putIntProp, getExistingIntProp, lookupProperty (via getProp), ensureProperty (via putProp), removeProp on non-existent, properties on multiple nodes
     * E: JsDoc: getJsDoc, getJsDocNode, setJsDocNode, null handling
     * F: Position: getLineno, setLineno, default -1
     * G: Number/Name helpers: newNumber, newString, newString(int,String), newTarget, getDouble, setDouble, getString, setString, getScope, setScope
     * H: Label: labelId()/labelId(int) on TARGET and YIELD, code bug on wrong type
     * I: hasConsistentReturnUsage: scenarios including simple return, if-else both return, if without else (drops off), loop with break, nested blocks
     * J: hasSideEffects: various node types (RETURN, CALL, etc. vs simple literals)
     * K: resetTargets (on FINALLY only, code bug otherwise)
     * L: toString / toStringTree (calls but not deep verification, depends on Token.printTrees)
     * M: EndCheck sub-methods indirectly tested via hasConsistentReturnUsage
     * 
     * Defect target (based on ScopedAliases issue): potential mis-handling of scope aliases,
     * simulated by testing getScope/setScope on Name nodes, and hasConsistentReturnUsage in 
     * presence of function declarations (if possible). The defect may manifest as incorrect
     * return consistency analysis for hoisted functions, so we test a complex tree with
     * LABEL/BREAK and a FUNCTION node inside a block.
     */

    // ---- Partition A: Constructors and basic getters ----

    @Test(timeout = 4000)
    public void testConstructorsAndType() {
        Node n1 = new Node(Token.ERROR);
        assertEquals(Token.ERROR, n1.getType());
        assertFalse(n1.hasChildren());
        assertNull(n1.getFirstChild());
        assertNull(n1.getLastChild());
        assertNull(n1.getNext());

        Node child = new Node(Token.NUMBER);
        Node n2 = new Node(Token.ADD, child);
        assertEquals(Token.ADD, n2.getType());
        assertTrue(n2.hasChildren());
        assertSame(child, n2.getFirstChild());
        assertSame(child, n2.getLastChild());
        assertNull(child.getNext());

        Node left = new Node(Token.NAME);
        Node right = new Node(Token.NUMBER);
        Node n3 = new Node(Token.ASSIGN, left, right);
        assertEquals(Token.ASSIGN, n3.getType());
        assertSame(left, n3.getFirstChild());
        assertSame(right, n3.getLastChild());

        Node mid = new Node(Token.NULL);
        Node n4 = new Node(Token.HOOK, left, mid, right);
        assertEquals(Token.HOOK, n4.getType());
        assertSame(left, n4.getFirstChild());
        assertSame(right, n4.getLastChild());
        assertSame(left.next, mid);
        assertSame(mid.next, right);

        Node n5 = new Node(Token.RETURN, 42);
        assertEquals(Token.RETURN, n5.getType());
        assertEquals(42, n5.getLineno());

        Node n6 = new Node(Token.THROW, child, 10);
        assertEquals(Token.THROW, n6.getType());
        assertEquals(10, n6.getLineno());

        Node n7 = new Node(Token.IFNE, left, right, 5);
        assertEquals(Token.IFNE, n7.getType());
        assertEquals(5, n7.getLineno());

        Node n8 = new Node(Token.BLOCK, left, mid, right, 3);
        assertEquals(Token.BLOCK, n8.getType());
        assertEquals(3, n8.getLineno());
    }

    @Test(timeout = 4000)
    public void testSetType() {
        Node n = new Node(Token.ERROR);
        assertSame(n, n.setType(Token.NUMBER));
        assertEquals(Token.NUMBER, n.getType());
    }

    // ---- Partition B: Tree manipulation ----

    @Test(timeout = 4000)
    public void testAddChildToFront() {
        Node parent = new Node(Token.BLOCK);
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        parent.addChildToFront(second);
        parent.addChildToFront(first);
        assertSame(first, parent.getFirstChild());
        assertSame(second, parent.getLastChild());
        assertSame(first.next, second);
    }

    @Test(timeout = 4000)
    public void testAddChildToBack() {
        Node parent = new Node(Token.BLOCK);
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        parent.addChildToBack(first);
        parent.addChildToBack(second);
        assertSame(first, parent.getFirstChild());
        assertSame(second, parent.getLastChild());
        assertSame(first.next, second);
    }

    @Test(timeout = 4000)
    public void testAddChildrenToFront() {
        Node parent = new Node(Token.BLOCK);
        Node child = new Node(Token.NUMBER);
        Node sibling = new Node(Token.STRING);
        child.addChildToBack(sibling);  // sibling
        parent.addChildrenToFront(child);
        assertSame(child, parent.getFirstChild());
        assertSame(sibling, parent.getLastChild());
        assertNull(sibling.next);
    }

    @Test(timeout = 4000)
    public void testAddChildrenToBack() {
        Node parent = new Node(Token.BLOCK);
        Node existing = new Node(Token.NULL);
        parent.addChildToBack(existing);
        Node child = new Node(Token.NUMBER);
        Node sibling = new Node(Token.STRING);
        child.addChildToBack(sibling);
        parent.addChildrenToBack(child);
        assertSame(existing, parent.getFirstChild());
        assertSame(sibling, parent.getLastChild());
        assertSame(existing.next, child);
        assertNull(sibling.next);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testAddChildBeforeWithSiblings() {
        Node parent = new Node(Token.BLOCK);
        Node child = new Node(Token.NUMBER);
        Node sibling = new Node(Token.STRING);
        child.addChildToBack(sibling); // child has sibling
        Node node = new Node(Token.NULL);
        parent.addChildToBack(node);
        parent.addChildBefore(child, node); // should throw
    }

    @Test(timeout = 4000)
    public void testAddChildBefore() {
        Node parent = new Node(Token.BLOCK);
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        parent.addChildToBack(first);
        parent.addChildToBack(second);
        Node newChild = new Node(Token.NULL);
        parent.addChildBefore(newChild, second);
        assertSame(first, parent.getFirstChild());
        assertSame(second, parent.getLastChild());
        assertSame(first.next, newChild);
        assertSame(newChild.next, second);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testAddChildAfterWithSiblings() {
        Node parent = new Node(Token.BLOCK);
        Node child = new Node(Token.NUMBER);
        Node sibling = new Node(Token.STRING);
        child.addChildToBack(sibling);
        Node node = new Node(Token.NULL);
        parent.addChildToBack(node);
        parent.addChildAfter(child, node); // should throw
    }

    @Test(timeout = 4000)
    public void testAddChildAfter() {
        Node parent = new Node(Token.BLOCK);
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        parent.addChildToBack(first);
        Node newChild = new Node(Token.NULL);
        parent.addChildAfter(newChild, first);
        assertSame(first, parent.getFirstChild());
        assertSame(newChild, parent.getLastChild());
        assertNull(newChild.next);
    }

    @Test(timeout = 4000)
    public void testRemoveChild() {
        Node parent = new Node(Token.BLOCK);
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        parent.addChildToBack(first);
        parent.addChildToBack(second);
        parent.removeChild(first);
        assertSame(second, parent.getFirstChild());
        assertSame(second, parent.getLastChild());
        assertNull(second.next);
        assertNull(first.next);
    }

    @Test(timeout = 4000)
    public void testReplaceChild() {
        Node parent = new Node(Token.BLOCK);
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        parent.addChildToBack(first);
        parent.addChildToBack(second);
        Node replacement = new Node(Token.NULL);
        parent.replaceChild(second, replacement);
        assertSame(first, parent.getFirstChild());
        assertSame(replacement, parent.getLastChild());
        assertNull(replacement.next);
    }

    @Test(timeout = 4000)
    public void testReplaceChildAfter() {
        Node parent = new Node(Token.BLOCK);
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        parent.addChildToBack(first);
        parent.addChildToBack(second);
        Node replacement = new Node(Token.NULL);
        parent.replaceChildAfter(first, replacement);
        assertSame(first, parent.getFirstChild());
        assertSame(replacement, parent.getLastChild());
        assertNull(replacement.next);
    }

    @Test(timeout = 4000)
    public void testRemoveChildren() {
        Node parent = new Node(Token.BLOCK);
        parent.addChildToBack(new Node(Token.NUMBER));
        parent.addChildToBack(new Node(Token.STRING));
        parent.removeChildren();
        assertFalse(parent.hasChildren());
        assertNull(parent.getFirstChild());
        assertNull(parent.getLastChild());
    }

    @Test(timeout = 4000)
    public void testGetChildBeforeFirst() {
        Node parent = new Node(Token.BLOCK);
        Node child = new Node(Token.NUMBER);
        parent.addChildToBack(child);
        assertNull(parent.getChildBefore(child));
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testGetChildBeforeNonChild() {
        Node parent = new Node(Token.BLOCK);
        Node notChild = new Node(Token.NUMBER);
        parent.getChildBefore(notChild);
    }

    @Test(timeout = 4000)
    public void testGetLastSibling() {
        Node first = new Node(Token.NUMBER);
        Node second = new Node(Token.STRING);
        Node third = new Node(Token.NULL);
        first.addChildToBack(second);
        first.addChildToBack(third);
        assertSame(third, first.getLastSibling());
        assertSame(third, second.getLastSibling());
    }

    // ---- Partition C: NodeIterator ----

    @Test(timeout = 4000)
    public void testIteratorHappyPath() {
        Node parent = new Node(Token.BLOCK);
        Node a = new Node(Token.NUMBER);
        Node b = new Node(Token.STRING);
        parent.addChildToBack(a);
        parent.addChildToBack(b);
        java.util.Iterator<Node> it = parent.iterator();
        assertTrue(it.hasNext());
        assertSame(a, it.next());
        assertTrue(it.hasNext());
        assertSame(b, it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000, expected = java.util.NoSuchElementException.class)
    public void testIteratorNextOnEmpty() {
        Node parent = new Node(Token.BLOCK);
        java.util.Iterator<Node> it = parent.iterator();
        it.next();
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveFirst() {
        Node parent = new Node(Token.BLOCK);
        Node a = new Node(Token.NUMBER);
        Node b = new Node(Token.STRING);
        parent.addChildToBack(a);
        parent.addChildToBack(b);
        java.util.Iterator<Node> it = parent.iterator();
        it.next(); // a
        it.remove();
        assertSame(b, parent.getFirstChild());
        assertSame(b, parent.getLastChild());
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveLast() {
        Node parent = new Node(Token.BLOCK);
        Node a = new Node(Token.NUMBER);
        Node b = new Node(Token.STRING);
        parent.addChildToBack(a);
        parent.addChildToBack(b);
        java.util.Iterator<Node> it = parent.iterator();
        it.next(); // a
        it.next(); // b
        it.remove();
        assertSame(a, parent.getFirstChild());
        assertSame(a, parent.getLastChild());
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testIteratorRemoveBeforeNext() {
        Node parent = new Node(Token.BLOCK);
        parent.addChildToBack(new Node(Token.NUMBER));
        java.util.Iterator<Node> it = parent.iterator();
        it.remove();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testIteratorRemoveTwice() {
        Node parent = new Node(Token.BLOCK);
        parent.addChildToBack(new Node(Token.NUMBER));
        java.util.Iterator<Node> it = parent.iterator();
        it.next();
        it.remove();
        it.remove();
    }

    // ---- Partition D: Property management ----

    @Test(timeout = 4000)
    public void testPropMethods() {
        Node n = new Node(Token.ERROR);
        assertNull(n.getProp(Node.FUNCTION_PROP));
        assertEquals(42, n.getIntProp(Node.FUNCTION_PROP, 42));
        n.putProp(Node.FUNCTION_PROP, "hello");
        assertEquals("hello", n.getProp(Node.FUNCTION_PROP));
        n.putIntProp(Node.FUNCTION_PROP, 100);
        assertEquals(100, n.getIntProp(Node.FUNCTION_PROP, -1));
        assertEquals(100, n.getExistingIntProp(Node.FUNCTION_PROP));
        n.removeProp(Node.FUNCTION_PROP);
        assertNull(n.getProp(Node.FUNCTION_PROP));
        // remove non-existent
        n.removeProp(Node.LOCAL_PROP); // should not throw
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testGetExistingIntPropMissing() {
        Node n = new Node(Token.ERROR);
        n.getExistingIntProp(Node.FUNCTION_PROP);
    }

    @Test(timeout = 4000)
    public void testPutPropNullRemoves() {
        Node n = new Node(Token.ERROR);
        n.putProp(Node.CASEARRAY_PROP, "value");
        assertNotNull(n.getProp(Node.CASEARRAY_PROP));
        n.putProp(Node.CASEARRAY_PROP, null);
        assertNull(n.getProp(Node.CASEARRAY_PROP));
    }

    // ---- Partition E: JsDoc ----

    @Test(timeout = 4000)
    public void testJsDoc() {
        Node n = new Node(Token.ERROR);
        assertNull(n.getJsDoc());
        assertNull(n.getJsDocNode());
        Comment c = new Comment(1, 1, "/** test */");
        n.setJsDocNode(c);
        assertSame(c, n.getJsDocNode());
        assertEquals("/** test */", n.getJsDoc());
        n.setJsDocNode(null);
        assertNull(n.getJsDoc());
    }

    // ---- Partition F: Line number ----

    @Test(timeout = 4000)
    public void testLineno() {
        Node n = new Node(Token.ERROR);
        assertEquals(-1, n.getLineno());
        n.setLineno(10);
        assertEquals(10, n.getLineno());
    }

    // ---- Partition G: Number/Name helpers ----

    @Test(timeout = 4000)
    public void testNewNumberAndDouble() {
        Node n = Node.newNumber(3.14);
        assertEquals(Token.NUMBER, n.getType());
        assertTrue(n instanceof NumberLiteral);
        assertEquals(3.14, n.getDouble(), 1e-15);
        n.setDouble(2.71);
        assertEquals(2.71, n.getDouble(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNewStringAndStringAccess() {
        Node n = Node.newString("hello");
        assertEquals(Token.STRING, n.getType());
        assertTrue(n instanceof Name);
        assertEquals("hello", n.getString());
        n.setString("world");
        assertEquals("world", n.getString());
    }

    @Test(timeout = 4000)
    public void testNewStringWithType() {
        Node n = Node.newString(Token.NAME, "foo");
        assertEquals(Token.NAME, n.getType());
        assertEquals("foo", n.getString());
    }

    @Test(timeout = 4000)
    public void testNewTarget() {
        Node t = Node.newTarget();
        assertEquals(Token.TARGET, t.getType());
    }

    @Test(timeout = 4000)
    public void testScopeOnName() {
        Node n = Node.newString("x");
        Scope scope = new Scope();
        // setScope expects Name instance, works because newString returns Name
        n.setScope(scope);
        assertSame(scope, n.getScope());
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testSetScopeOnNonNameThrows() {
        Node n = new Node(Token.NUMBER);
        n.setScope(new Scope()); // should throw via Kit.codeBug()
    }

    // ---- Partition H: Label ----

    @Test(timeout = 4000)
    public void testLabelOnTarget() {
        Node t = Node.newTarget();
        assertEquals(-1, t.labelId());
        t.labelId(5);
        assertEquals(5, t.labelId());
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testLabelOnWrongType() {
        Node n = new Node(Token.NUMBER);
        n.labelId();
    }

    // ---- Partition I: hasConsistentReturnUsage ----

    @Test(timeout = 4000)
    public void testHasConsistentReturnSimpleReturnValue() {
        // function body: { return 1; }
        Node body = new Node(Token.BLOCK);
        Node ret = new Node(Token.RETURN);
        Node value = Node.newNumber(1);
        ret.addChildToBack(value);
        body.addChildToBack(ret);
        assertTrue(body.hasConsistentReturnUsage());
    }

    @Test(timeout = 4000)
    public void testHasConsistentReturnSimpleReturnNoValue() {
        Node body = new Node(Token.BLOCK);
        Node ret = new Node(Token.RETURN);
        body.addChildToBack(ret);
        assertTrue(body.hasConsistentReturnUsage());
    }

    @Test(timeout = 4000)
    public void testHasConsistentReturnIfElseBothReturn() {
        // if (cond) { return 1; } else { return 2; }
        Node body = new Node(Token.BLOCK);
        Node ifNode = new Node(Token.IFNE);
        Node cond = new Node(Token.TRUE);
        Node thenBlock = new Node(Token.BLOCK);
        thenBlock.addChildToBack(new Node(Token.RETURN, Node.newNumber(1)));
        Node elseBlock = new Node(Token.BLOCK);
        elseBlock.addChildToBack(new Node(Token.RETURN, Node.newNumber(2)));
        ifNode.addChildToBack(cond);
        ifNode.addChildToBack(thenBlock);
        // The Jump class is needed for target property; we simulate with simple structure
        // For endCheckIf, it expects the 'then' node as next and 'else' as target.
        // We create a minimal Jump-compatible node using Jump directly.
        Jump jumpIf = new Jump(Token.IFNE);
        jumpIf.addChildToBack(cond);
        jumpIf.addChildToBack(thenBlock);
        jumpIf.target = elseBlock;
        body.addChildToBack(jumpIf);
        assertTrue(body.hasConsistentReturnUsage());
    }

    @Test(timeout = 4000)
    public void testHasConsistentReturnIfNoElseDropsOff() {
        // function body: if (cond) { return 1; } // no else -> can drop off
        Node body = new Node(Token.BLOCK);
        Jump jumpIf = new Jump(Token.IFNE);
        Node cond = new Node(Token.TRUE);
        Node thenBlock = new Node(Token.BLOCK);
        thenBlock.addChildToBack(new Node(Token.RETURN, Node.newNumber(1)));
        jumpIf.addChildToBack(cond);
        jumpIf.addChildToBack(thenBlock);
        // no else target (null)
        jumpIf.target = null;
        body.addChildToBack(jumpIf);
        assertFalse(body.hasConsistentReturnUsage());
    }

    @Test(timeout = 4000)
    public void testHasConsistentReturnLoopWithBreak() {
        // function body: while(true) { if (cond) break; return 1; }
        // Should be consistent because the return is after the loop?
        // Actually we need a loop that always returns. Build a simple loop body that returns.
        // For brevity, test a loop with a break that leads to a return.
        // We'll create a LOOP node with a predicate of TRUE and a body that returns.
        Node loop = new Node(Token.LOOP);
        Node predicate = new Node(Token.IFEQ);
        Node cond = new Node(Token.TRUE); // constant true -> should not drop off
        predicate.addChildToBack(cond);
        // target is the loop body
        Node bodyBlock = new Node(Token.BLOCK);
        bodyBlock.addChildToBack(new Node(Token.RETURN, Node.newNumber(1)));
        // The loop structure: first child of loop is predicate? In endCheckLoop, it expects
        // the second-to-last child of loop to be the predicate (IFEQ). We'll follow that.
        // Actually in Node constructor: (type, children) we can add manually.
        // For simplicity, we construct the tree manually.
        loop.addChildToBack(new Node(Token.NOP)); // dummy, will be overwritten
        loop.addChildToBack(predicate);
        loop.addChildToBack(bodyBlock);
        // The predicate's target should be the body (next after predicate?)
        // In endCheckLoop, it does: n = first... until n.next != last, then n is predicate
        // so predicate should be second-to-last child.
        // Set predicate's target as the body (Jump cast). We'll use Jump.
        Jump jumpPred = (Jump) predicate;
        jumpPred.target = bodyBlock;
        // Also set control block property for break: not needed.
        assertTrue(loop.hasConsistentReturnUsage());
    }

    @Test(timeout = 4000)
    public void testHasConsistentReturnFunctionHoisted() {
        // Simulate a function body with a hoisted function declaration inside a block.
        // According to the defect, this may cause incorrect analysis.
        // We create a BLOCK with a LABEL (representing hoisted function) and a return.
        // The LABEL has a TARGET and a BREAK maybe.
        Node body = new Node(Token.BLOCK);
        // Create a LABEL node (part of Jump)
        Jump label = new Jump(Token.LABEL);
        Node target = Node.newTarget();
        Node stmt = new Node(Token.EXPR_VOID, Node.newNumber(42)); // some statement
        label.addChildToBack(stmt);
        // In endCheckLabel, it checks 'next' (child of block?) Actually endCheckLabel expects
        // the node itself is the label, and next is the statement.
        // For simplicity, we'll just test that it doesn't throw and returns a proper value.
        label.target = target; // break target (unused)
        // Add a return value after the label
        Node ret = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(label);
        body.addChildToBack(ret);
        // This should be valid: the label's statement drops off, then return.
        assertTrue(body.hasConsistentReturnUsage());
    }

    // ---- Partition J: hasSideEffects ----

    @Test(timeout = 4000)
    public void testHasSideEffectsVarious() {
        assertTrue(new Node(Token.RETURN).hasSideEffects());
        assertTrue(new Node(Token.CALL).hasSideEffects());
        assertTrue(new Node(Token.ASSIGN).hasSideEffects());
        assertTrue(new Node(Token.THROW).hasSideEffects());
        assertTrue(new Node(Token.YIELD).hasSideEffects());
        // HOOK with both branches having side effects
        Node hook = new Node(Token.HOOK);
        Node cond = new Node(Token.TRUE);
        Node left = new Node(Token.CALL);
        Node right = new Node(Token.CALL);
        hook.addChildToBack(cond);
        hook.addChildToBack(left);
        hook.addChildToBack(right);
        assertTrue(hook.hasSideEffects());
        // AND/OR with side effects in left only
        Node and = new Node(Token.AND);
        and.addChildToBack(new Node(Token.CALL));
        and.addChildToBack(new Node(Token.NUMBER));
        assertTrue(and.hasSideEffects());
        // No side effects node (e.g., NUMBER)
        assertFalse(new Node(Token.NUMBER).hasSideEffects());
        assertFalse(Node.newNumber(1).hasSideEffects());
    }

    // ---- Partition K: resetTargets ----

    @Test(timeout = 4000)
    public void testResetTargetsOnFinally() {
        Node finallyNode = new Node(Token.FINALLY);
        Node target = Node.newTarget();
        target.labelId(10);
        finallyNode.addChildToBack(target);
        finallyNode.resetTargets();
        assertEquals(-1, target.labelId());
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testResetTargetsOnNonFinally() {
        new Node(Token.BLOCK).resetTargets();
    }

    // ---- Partition L: toString (smoke tests) ----

    @Test(timeout = 4000)
    public void testToString() {
        Node n = new Node(Token.ERROR);
        assertNotNull(n.toString());
        Node n2 = Node.newNumber(3.14);
        assertNotNull(n2.toString());
    }

    // ---- Partition M: Edge cases and defect target ----

    @Test(timeout = 4000)
    public void testGetChildBeforeWhenFirst() {
        Node parent = new Node(Token.BLOCK);
        Node child = new Node(Token.NUMBER);
        parent.addChildToBack(child);
        assertNull(parent.getChildBefore(child));
    }

    @Test(timeout = 4000)
    public void testStringNullHandling() {
        // setString with null should call Kit.codeBug -> Exception
        Node n = Node.newString("test");
        try {
            n.setString(null);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testScopeNullHandling() {
        Node n = Node.newString("x");
        try {
            n.setScope(null);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    // Defect-targeted test: simulate a function body that contains a function declaration
    // and a return; the hasConsistentReturnUsage should not incorrectly return false.
    // This aims to capture the scenario where a hoisted function interferes.
    @Test(timeout = 4000)
    public void testHasConsistentReturnWithFunctionDeclarationInBlock() {
        // function body: { function f() { return 1; } return 2; }
        Node body = new Node(Token.BLOCK);
        Node funcDecl = new Node(Token.FUNCTION);
        funcDecl.addChildToBack(new Node(Token.NAME, "f"));
        Node funcBody = new Node(Token.BLOCK);
        funcBody.addChildToBack(new Node(Token.RETURN, Node.newNumber(1)));
        funcDecl.addChildToBack(funcBody);
        body.addChildToBack(funcDecl);
        Node ret = new Node(Token.RETURN, Node.newNumber(2));
        body.addChildToBack(ret);
        // endCheckBlock will check each statement; FUNCTION type returns default END_DROPS_OFF,
        // then the return also returns END_RETURNS_VALUE, so overall should be consistent: (END_DROPS_OFF | END_RETURNS_VALUE).
        // hasConsistentReturnUsage checks if (END_RETURNS_VALUE and not (END_DROPS_OFF|END_RETURNS|END_YIELDS)) -> true.
        // So this should be true.
        assertTrue("Function with declaration and consistent return should be true", body.hasConsistentReturnUsage());
    }

    // Additional test to cover the case where a break causes inconsistency
    @Test(timeout = 4000)
    public void testHasConsistentReturnInconsistentBreak() {
        // function body: while (false) { break; } return 1; => consistent? Actually while(false) never enters,
        // but analysis may not see it. We'll create a loop with break that drops off.
        Node body = new Node(Token.BLOCK);
        Node loop = new Node(Token.LOOP);
        Node predicate = new Node(Token.IFEQ);
        Node cond = new Node(Token.FALSE); // constant false -> should not drop off? Actually constant false means loop never enters.
        predicate.addChildToBack(cond);
        Node loopBody = new Node(Token.BLOCK);
        Node breakNode = new Node(Token.BREAK);
        // break needs a JumpStatement, but for simplicity we skip target.
        loopBody.addChildToBack(breakNode);
        // Set predicate target to loopBody
        ((Jump)predicate).target = loopBody;
        loop.addChildToBack(new Node(Token.NOP));
        loop.addChildToBack(predicate);
        loop.addChildToBack(loopBody);
        // Add return after loop
        body.addChildToBack(loop);
        body.addChildToBack(new Node(Token.RETURN, Node.newNumber(1)));
        // The loop body has a break, which sets CONTROL_BLOCK_PROP on loop to END_DROPS_OFF,
        // so loop returns END_DROPS_OFF, then the return makes it consistent.
        assertTrue(body.hasConsistentReturnUsage());
    }
}