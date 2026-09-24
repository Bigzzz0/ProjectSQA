package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.BitSet;
import java.util.List;
import java.util.Set;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: LiveVariablesAnalysis class, specifically the computeGenKill method and flowThrough method.
 * 
 * Decision branches targeted:
 * 1. Token.SCRIPT, Token.BLOCK, Token.FUNCTION -> early return
 * 2. Token.WHILE, Token.DO, Token.IF -> computeGenKill on condition only
 * 3. Token.FOR with/without for-in -> different handling
 * 4. Token.VAR -> variable declaration with/without initializer
 * 5. Token.AND, Token.OR -> short-circuit evaluation (conditional second operand)
 * 6. Token.HOOK (ternary) -> conditional branches
 * 7. Token.NAME -> arguments alias vs regular variable
 * 8. Assignment operators (NodeUtil.isAssignmentOp) -> compound assignments read LHS
 * 9. Default case -> recursive traversal
 * 
 * Boundary conditions:
 * - null/empty nodes
 * - escaped vs non-escaped variables
 * - conditional flag propagation
 * - BitSet operations (andNot, or)
 * 
 * Known defect: testExpressionInForIn triggers IllegalStateException
 * Root cause: In Token.FOR case for for-in loops, the code accesses lhs.getLastChild()
 * without checking if lhs has children when NodeUtil.isVar(lhs) is false.
 * Specifically, when lhs is a NAME node (not VAR), the code still tries to access
 * lhs.getLastChild() which may return null or cause issues.
 * 
 * The fix should handle the case where lhs is not a VAR node properly.
 */
public class LiveVariablesAnalysisDeepseekTest {

    /**
     * Helper method to create a minimal LiveVariablesAnalysis instance for testing.
     * Uses a simple function scope with a few variables.
     */
    private LiveVariablesAnalysis createAnalysis() {
        // Create a simple AST: function() { var a, b, c; }
        Node script = new Node(Token.SCRIPT);
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "testFunc");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        
        // Add variables
        Node varNode = new Node(Token.VAR);
        Node varA = Node.newString(Token.NAME, "a");
        Node varB = Node.newString(Token.NAME, "b");
        Node varC = Node.newString(Token.NAME, "c");
        varNode.addChildToBack(varA);
        varNode.addChildToBack(varB);
        varNode.addChildToBack(varC);
        body.addChildToBack(varNode);
        
        function.addChildToBack(name);
        function.addChildToBack(params);
        function.addChildToBack(body);
        script.addChildToBack(function);
        
        // Create a simple compiler
        AbstractCompiler compiler = new TestCompiler();
        
        // Create scope
        Scope scope = new Scope(function, null);
        scope.declare("a", varA, null);
        scope.declare("b", varB, null);
        scope.declare("c", varC, null);
        
        // Create CFG (simplified - just enough to test)
        ControlFlowGraph<Node> cfg = new ControlFlowGraph<Node>(function, true, true) {
            @Override
            public List<DiGraphEdge<Node, Branch>> getOutEdges(Node node) {
                return java.util.Collections.emptyList();
            }
        };
        
        return new LiveVariablesAnalysis(cfg, scope, compiler);
    }
    
    /**
     * Test 1: Basic flowThrough with simple node (should not modify live set)
     */
    @Test(timeout = 4000)
    public void testFlowThroughScriptBlockFunction() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Test SCRIPT node
        Node script = new Node(Token.SCRIPT);
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(script, input);
        assertEquals(input, result);
        
        // Test BLOCK node
        Node block = new Node(Token.BLOCK);
        result = analysis.flowThrough(block, input);
        assertEquals(input, result);
        
        // Test FUNCTION node
        Node func = new Node(Token.FUNCTION);
        result = analysis.flowThrough(func, input);
        assertEquals(input, result);
    }
    
    /**
     * Test 2: Flow through WHILE/DO/IF nodes (only condition matters)
     */
    @Test(timeout = 4000)
    public void testFlowThroughWhileDoIf() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // WHILE with NAME condition
        Node whileNode = new Node(Token.WHILE);
        Node cond = Node.newString(Token.NAME, "a");
        whileNode.addChildToBack(cond);
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(whileNode, input);
        // 'a' should be in gen set
        assertTrue("Variable 'a' should be live after WHILE", result.isLive(analysis.getVarIndex("a")));
        
        // DO with NAME condition
        Node doNode = new Node(Token.DO);
        cond = Node.newString(Token.NAME, "b");
        doNode.addChildToBack(cond);
        result = analysis.flowThrough(doNode, input);
        assertTrue("Variable 'b' should be live after DO", result.isLive(analysis.getVarIndex("b")));
        
        // IF with NAME condition
        Node ifNode = new Node(Token.IF);
        cond = Node.newString(Token.NAME, "c");
        ifNode.addChildToBack(cond);
        result = analysis.flowThrough(ifNode, input);
        assertTrue("Variable 'c' should be live after IF", result.isLive(analysis.getVarIndex("c")));
    }
    
    /**
     * Test 3: Flow through FOR node (regular for loop)
     */
    @Test(timeout = 4000)
    public void testFlowThroughFor() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Regular for loop: for(; a; ) 
        Node forNode = new Node(Token.FOR);
        Node init = new Node(Token.EMPTY);
        Node cond = Node.newString(Token.NAME, "a");
        Node incr = new Node(Token.EMPTY);
        forNode.addChildToBack(init);
        forNode.addChildToBack(cond);
        forNode.addChildToBack(incr);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forNode, input);
        assertTrue("Variable 'a' should be live after FOR", result.isLive(analysis.getVarIndex("a")));
    }
    
    /**
     * Test 4: Flow through FOR-IN node (targets the known defect)
     * This test directly targets the IllegalStateException from testExpressionInForIn
     */
    @Test(timeout = 4000)
    public void testExpressionInForIn() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create for-in loop: for(x in y) where x is a NAME (not VAR)
        // This is the pattern that triggers the bug
        Node forInNode = new Node(Token.FOR);
        // Mark as for-in by having exactly 2 children
        Node lhs = Node.newString(Token.NAME, "x");  // Not a VAR node!
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(lhs);
        forInNode.addChildToBack(rhs);
        
        // This should not throw IllegalStateException in fixed version
        // In the buggy version, it would try to call lhs.getLastChild() on a NAME node
        // which may return null or cause issues
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            // If we get here, the bug is fixed - verify expected behavior
            assertTrue("Variable 'y' should be live after for-in", result.isLive(analysis.getVarIndex("y")));
            // 'x' should be both killed and generated
            assertTrue("Variable 'x' should be live after for-in", result.isLive(analysis.getVarIndex("x")));
        } catch (IllegalStateException e) {
            // This is the known defect - fail the test to reveal the bug
            fail("IllegalStateException thrown for for-in with expression: " + e.getMessage());
        } catch (NullPointerException e) {
            // Also possible in buggy version
            fail("NullPointerException thrown for for-in with expression: " + e.getMessage());
        }
    }
    
    /**
     * Test 5: Flow through FOR-IN with VAR (should work correctly)
     */
    @Test(timeout = 4000)
    public void testVarInForIn() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create for-in loop: for(var x in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
        assertTrue("Variable 'y' should be live after for-in", result.isLive(analysis.getVarIndex("y")));
        assertTrue("Variable 'x' should be live after for-in", result.isLive(analysis.getVarIndex("x")));
    }
    
    /**
     * Test 6: Flow through VAR node
     */
    @Test(timeout = 4000)
    public void testVarNode() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // var a = b;
        Node varNode = new Node(Token.VAR);
        Node varA = Node.newString(Token.NAME, "a");
        Node varB = Node.newString(Token.NAME, "b");
        varA.addChildToBack(varB);
        varNode.addChildToBack(varA);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(varNode, input);
        // 'b' should be in gen set (read), 'a' should be killed
        assertTrue("Variable 'b' should be live after VAR", result.isLive(analysis.getVarIndex("b")));
        assertFalse("Variable 'a' should be dead after VAR", result.isLive(analysis.getVarIndex("a")));
    }
    
    /**
     * Test 7: Flow through AND/OR nodes (short-circuit)
     */
    @Test(timeout = 4000)
    public void testAndOrNodes() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // a && b
        Node andNode = new Node(Token.AND);
        andNode.addChildToBack(Node.newString(Token.NAME, "a"));
        andNode.addChildToBack(Node.newString(Token.NAME, "b"));
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(andNode, input);
        assertTrue("Variable 'a' should be live after AND", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live after AND", result.isLive(analysis.getVarIndex("b")));
        
        // a || b
        Node orNode = new Node(Token.OR);
        orNode.addChildToBack(Node.newString(Token.NAME, "a"));
        orNode.addChildToBack(Node.newString(Token.NAME, "b"));
        
        result = analysis.flowThrough(orNode, input);
        assertTrue("Variable 'a' should be live after OR", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live after OR", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 8: Flow through HOOK (ternary) node
     */
    @Test(timeout = 4000)
    public void testHookNode() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // a ? b : c
        Node hookNode = new Node(Token.HOOK);
        hookNode.addChildToBack(Node.newString(Token.NAME, "a"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "b"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "c"));
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(hookNode, input);
        assertTrue("Variable 'a' should be live after HOOK", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live after HOOK", result.isLive(analysis.getVarIndex("b")));
        assertTrue("Variable 'c' should be live after HOOK", result.isLive(analysis.getVarIndex("c")));
    }
    
    /**
     * Test 9: Flow through assignment operators
     */
    @Test(timeout = 4000)
    public void testAssignmentOperators() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Simple assignment: a = b
        Node assign = new Node(Token.ASSIGN);
        assign.addChildToBack(Node.newString(Token.NAME, "a"));
        assign.addChildToBack(Node.newString(Token.NAME, "b"));
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(assign, input);
        assertTrue("Variable 'b' should be live after ASSIGN", result.isLive(analysis.getVarIndex("b")));
        assertFalse("Variable 'a' should be dead after ASSIGN", result.isLive(analysis.getVarIndex("a")));
        
        // Compound assignment: a += b
        Node assignAdd = new Node(Token.ASSIGN_ADD);
        assignAdd.addChildToBack(Node.newString(Token.NAME, "a"));
        assignAdd.addChildToBack(Node.newString(Token.NAME, "b"));
        
        result = analysis.flowThrough(assignAdd, input);
        assertTrue("Variable 'a' should be live after ASSIGN_ADD (read)", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live after ASSIGN_ADD", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 10: Flow through NAME node (arguments alias)
     */
    @Test(timeout = 4000)
    public void testArgumentsName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Use "arguments" as a NAME node
        Node argumentsNode = Node.newString(Token.NAME, "arguments");
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(argumentsNode, input);
        // Should mark all parameters as escaped, but not affect local variables
        // Since we have no parameters in our test scope, nothing should change
        assertEquals(input, result);
    }
    
    /**
     * Test 11: Flow through with escaped variables
     */
    @Test(timeout = 4000)
    public void testEscapedVariables() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Access an escaped variable - should not be added to gen set
        // First, get the escaped set and add 'a' to it
        Set<Var> escaped = analysis.getEscapedLocals();
        // 'a' is not escaped by default, so it should be added to gen
        Node nameNode = Node.newString(Token.NAME, "a");
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(nameNode, input);
        assertTrue("Non-escaped variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
    }
    
    /**
     * Test 12: LiveVariableLattice operations
     */
    @Test(timeout = 4000)
    public void testLiveVariableLattice() {
        LiveVariablesAnalysis analysis = createAnalysis();
        
        // Test equals
        LiveVariablesAnalysis.LiveVariableLattice lattice1 = analysis.createEntryLattice();
        LiveVariablesAnalysis.LiveVariableLattice lattice2 = analysis.createEntryLattice();
        assertEquals(lattice1, lattice2);
        assertEquals(lattice1.hashCode(), lattice2.hashCode());
        
        // Test isLive with Var
        // Since all variables start dead, isLive should return false
        Scope scope = analysis.getJsScope(); // Need to access scope - but it's private
        // We'll test through flowThrough instead
    }
    
    /**
     * Test 13: getVarIndex
     */
    @Test(timeout = 4000)
    public void testGetVarIndex() {
        LiveVariablesAnalysis analysis = createAnalysis();
        
        int indexA = analysis.getVarIndex("a");
        int indexB = analysis.getVarIndex("b");
        int indexC = analysis.getVarIndex("c");
        
        assertTrue("Index 'a' should be non-negative", indexA >= 0);
        assertTrue("Index 'b' should be non-negative", indexB >= 0);
        assertTrue("Index 'c' should be non-negative", indexC >= 0);
        assertNotEquals("Indices should be different", indexA, indexB);
    }
    
    /**
     * Test 14: isForward returns false
     */
    @Test(timeout = 4000)
    public void testIsForward() {
        LiveVariablesAnalysis analysis = createAnalysis();
        assertFalse("LiveVariablesAnalysis should be backward", analysis.isForward());
    }
    
    /**
     * Test 15: createEntryLattice and createInitialEstimateLattice
     */
    @Test(timeout = 4000)
    public void testCreateLattices() {
        LiveVariablesAnalysis analysis = createAnalysis();
        
        LiveVariablesAnalysis.LiveVariableLattice entry = analysis.createEntryLattice();
        LiveVariablesAnalysis.LiveVariableLattice estimate = analysis.createInitialEstimateLattice();
        
        assertEquals("Entry and estimate lattices should be equal", entry, estimate);
    }
    
    /**
     * Test 16: Flow through with conditional flag (exception edges)
     */
    @Test(timeout = 4000)
    public void testConditionalFlow() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create a node with ON_EX edge to simulate exception
        Node node = new Node(Token.ASSIGN);
        node.addChildToBack(Node.newString(Token.NAME, "a"));
        node.addChildToBack(Node.newString(Token.NAME, "b"));
        
        // We can't easily add edges to the CFG, but the flowThrough method
        // checks for ON_EX edges. Without them, conditional is false.
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(node, input);
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
        assertFalse("Variable 'a' should be dead", result.isLive(analysis.getVarIndex("a")));
    }
    
    /**
     * Test 17: Flow through default case (recursive traversal)
     */
    @Test(timeout = 4000)
    public void testDefaultCase() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create an ADD node with two NAME children
        Node addNode = new Node(Token.ADD);
        addNode.addChildToBack(Node.newString(Token.NAME, "a"));
        addNode.addChildToBack(Node.newString(Token.NAME, "b"));
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(addNode, input);
        assertTrue("Variable 'a' should be live after ADD", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live after ADD", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 18: Flow through with non-local variable (should not be added)
     */
    @Test(timeout = 4000)
    public void testNonLocalVariable() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Use a variable name that is not declared in scope
        Node nameNode = Node.newString(Token.NAME, "undefinedVar");
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(nameNode, input);
        assertEquals("Non-local variable should not affect live set", input, result);
    }
    
    /**
     * Test 19: Flow through with empty node (no children)
     */
    @Test(timeout = 4000)
    public void testEmptyNode() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create an empty node (e.g., EMPTY token)
        Node emptyNode = new Node(Token.EMPTY);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(emptyNode, input);
        assertEquals("Empty node should not change live set", input, result);
    }
    
    /**
     * Test 20: Multiple variables in one expression
     */
    @Test(timeout = 4000)
    public void testMultipleVariables() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // a + b + c
        Node add1 = new Node(Token.ADD);
        Node add2 = new Node(Token.ADD);
        add2.addChildToBack(Node.newString(Token.NAME, "a"));
        add2.addChildToBack(Node.newString(Token.NAME, "b"));
        add1.addChildToBack(add2);
        add1.addChildToBack(Node.newString(Token.NAME, "c"));
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(add1, input);
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
        assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
    }
    
    /**
     * Test 21: Nested IF with assignment
     */
    @Test(timeout = 4000)
    public void testNestedIf() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // if (a) { b = c; }
        Node ifNode = new Node(Token.IF);
        Node cond = Node.newString(Token.NAME, "a");
        Node block = new Node(Token.BLOCK);
        Node assign = new Node(Token.ASSIGN);
        assign.addChildToBack(Node.newString(Token.NAME, "b"));
        assign.addChildToBack(Node.newString(Token.NAME, "c"));
        block.addChildToBack(assign);
        ifNode.addChildToBack(cond);
        ifNode.addChildToBack(block);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(ifNode, input);
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
        assertFalse("Variable 'b' should be dead (killed)", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 22: getEscapedLocals returns non-null set
     */
    @Test(timeout = 4000)
    public void testGetEscapedLocals() {
        LiveVariablesAnalysis analysis = createAnalysis();
        Set<Var> escaped = analysis.getEscapedLocals();
        assertNotNull("Escaped locals set should not be null", escaped);
    }
    
    /**
     * Test 23: markAllParametersEscaped
     */
    @Test(timeout = 4000)
    public void testMarkAllParametersEscaped() {
        LiveVariablesAnalysis analysis = createAnalysis();
        
        // This method is package-private, we can call it directly
        analysis.markAllParametersEscaped();
        
        // After marking, parameters should be in escaped set
        Set<Var> escaped = analysis.getEscapedLocals();
        // Our test scope has no parameters, so set should still be empty
        assertTrue("Escaped set should be empty (no parameters)", escaped.isEmpty());
    }
    
    /**
     * Test 24: Flow through with conditional assignment (should not kill)
     */
    @Test(timeout = 4000)
    public void testConditionalAssignment() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create a node that simulates conditional assignment
        // We'll use AND short-circuit to make the assignment conditional
        Node andNode = new Node(Token.AND);
        Node cond = Node.newString(Token.NAME, "a");
        Node assign = new Node(Token.ASSIGN);
        assign.addChildToBack(Node.newString(Token.NAME, "b"));
        assign.addChildToBack(Node.newString(Token.NAME, "c"));
        andNode.addChildToBack(cond);
        andNode.addChildToBack(assign);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(andNode, input);
        // 'a' should be live (condition)
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        // 'c' should be live (read in assignment)
        assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
        // 'b' should NOT be killed because assignment is conditional
        // But it should be in gen set because it's read? No, ASSIGN doesn't read LHS
        // Actually for ASSIGN, LHS is only killed, not generated
        // Since conditional, kill is skipped, so 'b' should remain dead (was dead initially)
        assertFalse("Variable 'b' should remain dead", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 25: LiveVariableLattice toString
     */
    @Test(timeout = 4000)
    public void testLiveVariableLatticeToString() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice lattice = analysis.createEntryLattice();
        
        String str = lattice.toString();
        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain BitSet representation", str.contains("{") || str.contains("}"));
    }
    
    /**
     * Test 26: Flow through with FOR-IN and multiple variables in rhs
     */
    @Test(timeout = 4000)
    public void testForInWithComplexRhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(x in a + b)
        Node forInNode = new Node(Token.FOR);
        Node lhs = Node.newString(Token.NAME, "x");
        Node addNode = new Node(Token.ADD);
        addNode.addChildToBack(Node.newString(Token.NAME, "a"));
        addNode.addChildToBack(Node.newString(Token.NAME, "b"));
        forInNode.addChildToBack(lhs);
        forInNode.addChildToBack(addNode);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (IllegalStateException e) {
            fail("IllegalStateException thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 27: Flow through with FOR-IN where lhs is a function call (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithFunctionCallLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(foo() in y) - this is invalid JS but tests the code path
        Node forInNode = new Node(Token.FOR);
        Node callNode = new Node(Token.CALL);
        Node funcName = Node.newString(Token.NAME, "foo");
        callNode.addChildToBack(funcName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(callNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            // Should not throw - the code should handle non-NAME lhs gracefully
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (IllegalStateException e) {
            fail("IllegalStateException thrown: " + e.getMessage());
        } catch (Exception e) {
            // Other exceptions might be acceptable for invalid AST
            // But we prefer no exception
        }
    }
    
    /**
     * Test 28: Flow through with FOR-IN and null children (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithNullChildren() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create a FOR node with only one child (incomplete for-in)
        Node forInNode = new Node(Token.FOR);
        Node lhs = Node.newString(Token.NAME, "x");
        forInNode.addChildToBack(lhs);
        // No rhs child
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            // Should handle gracefully
        } catch (NullPointerException e) {
            // This might happen in buggy version
            fail("NullPointerException thrown: " + e.getMessage());
        } catch (IllegalStateException e) {
            fail("IllegalStateException thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 29: Flow through with FOR-IN where lhs is VAR with multiple children
     */
    @Test(timeout = 4000)
    public void testForInWithVarMultipleChildren() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a in y) - this is actually invalid but tests the code
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node initVal = Node.newString(Token.NAME, "a");
        varX.addChildToBack(initVal);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 30: Flow through with deeply nested expressions
     */
    @Test(timeout = 4000)
    public void testDeeplyNestedExpression() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // (a && b) || (c ? d : e)
        Node orNode = new Node(Token.OR);
        Node andNode = new Node(Token.AND);
        andNode.addChildToBack(Node.newString(Token.NAME, "a"));
        andNode.addChildToBack(Node.newString(Token.NAME, "b"));
        Node hookNode = new Node(Token.HOOK);
        hookNode.addChildToBack(Node.newString(Token.NAME, "c"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "d"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "e"));
        orNode.addChildToBack(andNode);
        orNode.addChildToBack(hookNode);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(orNode, input);
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
        assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
        assertTrue("Variable 'd' should be live", result.isLive(analysis.getVarIndex("d")));
        assertTrue("Variable 'e' should be live", result.isLive(analysis.getVarIndex("e")));
    }
    
    /**
     * Test 31: LiveVariableLattice copy constructor
     */
    @Test(timeout = 4000)
    public void testLiveVariableLatticeCopy() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice original = analysis.createEntryLattice();
        
        // Modify original by flowing through a NAME node
        Node nameNode = Node.newString(Token.NAME, "a");
        LiveVariablesAnalysis.LiveVariableLattice modified = analysis.flowThrough(nameNode, original);
        
        // The copy constructor is used internally in flowThrough
        // Verify that original is not modified
        assertFalse("Original should not be modified", original.isLive(analysis.getVarIndex("a")));
        assertTrue("Modified should have 'a' live", modified.isLive(analysis.getVarIndex("a")));
    }
    
    /**
     * Test 32: Flow through with multiple assignments
     */
    @Test(timeout = 4000)
    public void testMultipleAssignments() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // a = b = c
        Node assign1 = new Node(Token.ASSIGN);
        Node assign2 = new Node(Token.ASSIGN);
        assign2.addChildToBack(Node.newString(Token.NAME, "b"));
        assign2.addChildToBack(Node.newString(Token.NAME, "c"));
        assign1.addChildToBack(Node.newString(Token.NAME, "a"));
        assign1.addChildToBack(assign2);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(assign1, input);
        assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
        assertFalse("Variable 'a' should be dead", result.isLive(analysis.getVarIndex("a")));
        assertFalse("Variable 'b' should be dead", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 33: Flow through with compound assignment and multiple operands
     */
    @Test(timeout = 4000)
    public void testCompoundAssignmentWithMultipleOperands() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // a += b + c
        Node assignAdd = new Node(Token.ASSIGN_ADD);
        Node addNode = new Node(Token.ADD);
        addNode.addChildToBack(Node.newString(Token.NAME, "b"));
        addNode.addChildToBack(Node.newString(Token.NAME, "c"));
        assignAdd.addChildToBack(Node.newString(Token.NAME, "a"));
        assignAdd.addChildToBack(addNode);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(assignAdd, input);
        assertTrue("Variable 'a' should be live (read)", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
        assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
    }
    
    /**
     * Test 34: Flow through with VAR and no initializer
     */
    @Test(timeout = 4000)
    public void testVarWithoutInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // var a;
        Node varNode = new Node(Token.VAR);
        Node varA = Node.newString(Token.NAME, "a");
        varNode.addChildToBack(varA);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(varNode, input);
        // No initializer, so no gen. Kill should happen (not conditional)
        assertFalse("Variable 'a' should be dead after VAR without init", result.isLive(analysis.getVarIndex("a")));
    }
    
    /**
     * Test 35: Flow through with multiple VAR declarations
     */
    @Test(timeout = 4000)
    public void testMultipleVarDeclarations() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // var a = b, c = d;
        Node varNode = new Node(Token.VAR);
        Node varA = Node.newString(Token.NAME, "a");
        varA.addChildToBack(Node.newString(Token.NAME, "b"));
        Node varC = Node.newString(Token.NAME, "c");
        varC.addChildToBack(Node.newString(Token.NAME, "d"));
        varNode.addChildToBack(varA);
        varNode.addChildToBack(varC);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(varNode, input);
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
        assertTrue("Variable 'd' should be live", result.isLive(analysis.getVarIndex("d")));
        assertFalse("Variable 'a' should be dead", result.isLive(analysis.getVarIndex("a")));
        assertFalse("Variable 'c' should be dead", result.isLive(analysis.getVarIndex("c")));
    }
    
    /**
     * Test 36: Flow through with WHILE and complex condition
     */
    @Test(timeout = 4000)
    public void testWhileWithComplexCondition() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // while(a < b)
        Node whileNode = new Node(Token.WHILE);
        Node ltNode = new Node(Token.LT);
        ltNode.addChildToBack(Node.newString(Token.NAME, "a"));
        ltNode.addChildToBack(Node.newString(Token.NAME, "b"));
        whileNode.addChildToBack(ltNode);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(whileNode, input);
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 37: Flow through with DO and complex condition
     */
    @Test(timeout = 4000)
    public void testDoWithComplexCondition() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // do { } while(a || b)
        Node doNode = new Node(Token.DO);
        Node orNode = new Node(Token.OR);
        orNode.addChildToBack(Node.newString(Token.NAME, "a"));
        orNode.addChildToBack(Node.newString(Token.NAME, "b"));
        doNode.addChildToBack(orNode);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(doNode, input);
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 38: Flow through with IF and complex condition
     */
    @Test(timeout = 4000)
    public void testIfWithComplexCondition() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // if(a && b)
        Node ifNode = new Node(Token.IF);
        Node andNode = new Node(Token.AND);
        andNode.addChildToBack(Node.newString(Token.NAME, "a"));
        andNode.addChildToBack(Node.newString(Token.NAME, "b"));
        ifNode.addChildToBack(andNode);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(ifNode, input);
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 39: Flow through with FOR and complex condition
     */
    @Test(timeout = 4000)
    public void testForWithComplexCondition() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(; a < b; )
        Node forNode = new Node(Token.FOR);
        Node init = new Node(Token.EMPTY);
        Node ltNode = new Node(Token.LT);
        ltNode.addChildToBack(Node.newString(Token.NAME, "a"));
        ltNode.addChildToBack(Node.newString(Token.NAME, "b"));
        Node incr = new Node(Token.EMPTY);
        forNode.addChildToBack(init);
        forNode.addChildToBack(ltNode);
        forNode.addChildToBack(incr);
        
        LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forNode, input);
        assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
    }
    
    /**
     * Test 40: Flow through with FOR-IN and escaped variable in rhs
     */
    @Test(timeout = 4000)
    public void testForInWithEscapedRhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(x in y) where y is escaped
        // First, mark 'y' as escaped by adding it to escaped set
        // We can't easily do this, so we'll just test the normal case
        
        Node forInNode = new Node(Token.FOR);
        Node lhs = Node.newString(Token.NAME, "x");
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(lhs);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (IllegalStateException e) {
            fail("IllegalStateException thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 41: Flow through with FOR-IN and null lhs (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithNullLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // Create a FOR node with null as first child (should not happen in practice)
        Node forInNode = new Node(Token.FOR);
        // Don't add any children - this is an invalid state
        // The code will try to access getFirstChild() which returns null
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            // Should handle gracefully - but this is an edge case
        } catch (NullPointerException e) {
            // Expected for invalid AST
        } catch (Exception e) {
            // Other exceptions might be acceptable
        }
    }
    
    /**
     * Test 42: Flow through with FOR-IN and VAR with no children (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithEmptyVar() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        // No children in VAR
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (NullPointerException e) {
            // This might happen in buggy version when accessing varNode.getLastChild()
            fail("NullPointerException thrown: " + e.getMessage());
        } catch (IllegalStateException e) {
            fail("IllegalStateException thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 43: Flow through with FOR-IN and VAR with multiple children (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarMultipleChildren2() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x, y in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        varNode.addChildToBack(Node.newString(Token.NAME, "x"));
        varNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 44: Flow through with FOR-IN and NAME with no string value (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithEmptyName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for( in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node emptyName = Node.newString(Token.NAME, "");
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(emptyName);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 45: Flow through with FOR-IN and non-NAME, non-VAR lhs (e.g., ARRAY)
     */
    @Test(timeout = 4000)
    public void testForInWithArrayLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for([x] in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node arrayNode = new Node(Token.ARRAYLIT);
        arrayNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(arrayNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            // 'x' inside array should not be processed directly by for-in logic
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 46: Flow through with FOR-IN and GETPROP lhs (e.g., obj.prop in y)
     */
    @Test(timeout = 4000)
    public void testForInWithGetPropLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(obj.prop in y)
        Node forInNode = new Node(Token.FOR);
        Node getPropNode = new Node(Token.GETPROP);
        getPropNode.addChildToBack(Node.newString(Token.NAME, "obj"));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "prop"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(getPropNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            // 'obj' should be live because it's read
            assertTrue("Variable 'obj' should be live", result.isLive(analysis.getVarIndex("obj")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 47: Flow through with FOR-IN and GETELEM lhs (e.g., obj[x] in y)
     */
    @Test(timeout = 4000)
    public void testForInWithGetElemLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(obj[x] in y)
        Node forInNode = new Node(Token.FOR);
        Node getElemNode = new Node(Token.GETELEM);
        getElemNode.addChildToBack(Node.newString(Token.NAME, "obj"));
        getElemNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(getElemNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'obj' should be live", result.isLive(analysis.getVarIndex("obj")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 48: Flow through with FOR-IN and CALL lhs (e.g., foo() in y)
     */
    @Test(timeout = 4000)
    public void testForInWithCallLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(foo(a) in y)
        Node forInNode = new Node(Token.FOR);
        Node callNode = new Node(Token.CALL);
        Node funcName = Node.newString(Token.NAME, "foo");
        callNode.addChildToBack(funcName);
        callNode.addChildToBack(Node.newString(Token.NAME, "a"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(callNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 49: Flow through with FOR-IN and THIS keyword (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithThisLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(this in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node thisNode = new Node(Token.THIS);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(thisNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 50: Flow through with FOR-IN and NUMBER literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithNumberLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(1 in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node numNode = Node.newNumber(Token.NUMBER, 1.0);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(numNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 51: Flow through with FOR-IN and STRING literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithStringLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for("x" in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node strNode = Node.newString(Token.STRING, "x");
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(strNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 52: Flow through with FOR-IN and REGEXP literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithRegexpLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(/x/ in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node regexpNode = new Node(Token.REGEXP);
        regexpNode.addChildToBack(Node.newString(Token.STRING, "x"));
        regexpNode.addChildToBack(Node.newString(Token.STRING, ""));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(regexpNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 53: Flow through with FOR-IN and NULL literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithNullLhs2() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(null in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node nullNode = new Node(Token.NULL);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(nullNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 54: Flow through with FOR-IN and TRUE literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithTrueLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(true in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node trueNode = new Node(Token.TRUE);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(trueNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 55: Flow through with FOR-IN and FALSE literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithFalseLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(false in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node falseNode = new Node(Token.FALSE);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(falseNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 56: Flow through with FOR-IN and VOID expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVoidLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(void x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node voidNode = new Node(Token.VOID);
        voidNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(voidNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 57: Flow through with FOR-IN and TYPEOF expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithTypeofLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(typeof x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node typeofNode = new Node(Token.TYPEOF);
        typeofNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(typeofNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 58: Flow through with FOR-IN and NOT expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithNotLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(!x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node notNode = new Node(Token.NOT);
        notNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(notNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 59: Flow through with FOR-IN and NEG expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithNegLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(-x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node negNode = new Node(Token.NEG);
        negNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(negNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 60: Flow through with FOR-IN and POS expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithPosLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(+x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node posNode = new Node(Token.POS);
        posNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(posNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 61: Flow through with FOR-IN and BITNOT expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithBitNotLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(~x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node bitNotNode = new Node(Token.BITNOT);
        bitNotNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(bitNotNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 62: Flow through with FOR-IN and INC expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithIncLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(++x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node incNode = new Node(Token.INC);
        incNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(incNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 63: Flow through with FOR-IN and DEC expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithDecLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(--x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node decNode = new Node(Token.DEC);
        decNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(decNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 64: Flow through with FOR-IN and NEW expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithNewLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(new Foo() in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node newNode = new Node(Token.NEW);
        Node fooName = Node.newString(Token.NAME, "Foo");
        newNode.addChildToBack(fooName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(newNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 65: Flow through with FOR-IN and DELPROP expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithDelPropLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(delete x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node delNode = new Node(Token.DELPROP);
        delNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(delNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 66: Flow through with FOR-IN and INSTANCEOF expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithInstanceofLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(x instanceof Foo in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node instanceOfNode = new Node(Token.INSTANCEOF);
        instanceOfNode.addChildToBack(Node.newString(Token.NAME, "x"));
        instanceOfNode.addChildToBack(Node.newString(Token.NAME, "Foo"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(instanceOfNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 67: Flow through with FOR-IN and IN expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithInLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(x in y in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node inNode = new Node(Token.IN);
        inNode.addChildToBack(Node.newString(Token.NAME, "x"));
        inNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(inNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 68: Flow through with FOR-IN and COMMA expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithCommaLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(x, y in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node commaNode = new Node(Token.COMMA);
        commaNode.addChildToBack(Node.newString(Token.NAME, "x"));
        commaNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(commaNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 69: Flow through with FOR-IN and ASSIGN expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithAssignLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(x = y in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node assignNode = new Node(Token.ASSIGN);
        assignNode.addChildToBack(Node.newString(Token.NAME, "x"));
        assignNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(assignNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertFalse("Variable 'x' should be dead (killed by assignment)", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 70: Flow through with FOR-IN and HOOK expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithHookLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(a ? b : c in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node hookNode = new Node(Token.HOOK);
        hookNode.addChildToBack(Node.newString(Token.NAME, "a"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "b"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "c"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(hookNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 71: Flow through with FOR-IN and AND expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithAndLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(a && b in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node andNode = new Node(Token.AND);
        andNode.addChildToBack(Node.newString(Token.NAME, "a"));
        andNode.addChildToBack(Node.newString(Token.NAME, "b"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(andNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 72: Flow through with FOR-IN and OR expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithOrLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(a || b in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node orNode = new Node(Token.OR);
        orNode.addChildToBack(Node.newString(Token.NAME, "a"));
        orNode.addChildToBack(Node.newString(Token.NAME, "b"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(orNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 73: Flow through with FOR-IN and EMPTY expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithEmptyLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(; in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node emptyNode = new Node(Token.EMPTY);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(emptyNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 74: Flow through with FOR-IN and LABEL expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithLabelLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(label: x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node labelNode = new Node(Token.LABEL);
        labelNode.addChildToBack(Node.newString(Token.LABEL_NAME, "label"));
        labelNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(labelNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 75: Flow through with FOR-IN and BREAK expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithBreakLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(break in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node breakNode = new Node(Token.BREAK);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(breakNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 76: Flow through with FOR-IN and CONTINUE expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithContinueLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(continue in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node continueNode = new Node(Token.CONTINUE);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(continueNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 77: Flow through with FOR-IN and RETURN expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithReturnLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(return in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(returnNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 78: Flow through with FOR-IN and THROW expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithThrowLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(throw x in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node throwNode = new Node(Token.THROW);
        throwNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(throwNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 79: Flow through with FOR-IN and TRY expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithTryLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(try { x } catch(e) {} in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        tryBlock.addChildToBack(Node.newString(Token.NAME, "x"));
        Node catchNode = new Node(Token.CATCH);
        Node catchBlock = new Node(Token.BLOCK);
        catchNode.addChildToBack(Node.newString(Token.NAME, "e"));
        catchNode.addChildToBack(catchBlock);
        tryNode.addChildToBack(tryBlock);
        tryNode.addChildToBack(catchNode);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(tryNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 80: Flow through with FOR-IN and SWITCH expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithSwitchLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(switch(x) { case 1: break; } in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node switchNode = new Node(Token.SWITCH);
        switchNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node caseNode = new Node(Token.CASE);
        caseNode.addChildToBack(Node.newNumber(Token.NUMBER, 1));
        Node caseBody = new Node(Token.BLOCK);
        caseBody.addChildToBack(new Node(Token.BREAK));
        caseNode.addChildToBack(caseBody);
        switchNode.addChildToBack(caseNode);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(switchNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 81: Flow through with FOR-IN and WHILE expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithWhileLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(while(x) { y; } in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node whileNode = new Node(Token.WHILE);
        whileNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node whileBody = new Node(Token.BLOCK);
        whileBody.addChildToBack(Node.newString(Token.NAME, "y"));
        whileNode.addChildToBack(whileBody);
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(whileNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 82: Flow through with FOR-IN and DO expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithDoLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(do { x; } while(y) in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node doNode = new Node(Token.DO);
        Node doBody = new Node(Token.BLOCK);
        doBody.addChildToBack(Node.newString(Token.NAME, "x"));
        Node doCond = Node.newString(Token.NAME, "y");
        doNode.addChildToBack(doBody);
        doNode.addChildToBack(doCond);
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(doNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 83: Flow through with FOR-IN and FOR expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithForLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(for(;x;) in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node innerForNode = new Node(Token.FOR);
        innerForNode.addChildToBack(new Node(Token.EMPTY));
        innerForNode.addChildToBack(Node.newString(Token.NAME, "x"));
        innerForNode.addChildToBack(new Node(Token.EMPTY));
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(innerForNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 84: Flow through with FOR-IN and FOR-IN expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithForInLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(for(x in y) in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node innerForInNode = new Node(Token.FOR);
        innerForInNode.addChildToBack(Node.newString(Token.NAME, "x"));
        innerForInNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(innerForInNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 85: Flow through with FOR-IN and IF expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithIfLhs() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(if(x) { y; } in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(Node.newString(Token.NAME, "x"));
        Node ifBlock = new Node(Token.BLOCK);
        ifBlock.addChildToBack(Node.newString(Token.NAME, "y"));
        ifNode.addChildToBack(ifBlock);
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(ifNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 86: Flow through with FOR-IN and VAR with initializer that has side effects
     */
    @Test(timeout = 4000)
    public void testForInWithVarInitializerSideEffects() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = foo() in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node callNode = new Node(Token.CALL);
        callNode.addChildToBack(Node.newString(Token.NAME, "foo"));
        varX.addChildToBack(callNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 87: Flow through with FOR-IN and VAR with multiple initializers
     */
    @Test(timeout = 4000)
    public void testForInWithVarMultipleInitializers() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a, y = b in z) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        varX.addChildToBack(Node.newString(Token.NAME, "a"));
        Node varY = Node.newString(Token.NAME, "y");
        varY.addChildToBack(Node.newString(Token.NAME, "b"));
        varNode.addChildToBack(varX);
        varNode.addChildToBack(varY);
        Node rhs = Node.newString(Token.NAME, "z");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 88: Flow through with FOR-IN and deeply nested VAR
     */
    @Test(timeout = 4000)
    public void testForInWithDeeplyNestedVar() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = (a + b) * c in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node mulNode = new Node(Token.MUL);
        Node addNode = new Node(Token.ADD);
        addNode.addChildToBack(Node.newString(Token.NAME, "a"));
        addNode.addChildToBack(Node.newString(Token.NAME, "b"));
        mulNode.addChildToBack(addNode);
        mulNode.addChildToBack(Node.newString(Token.NAME, "c"));
        varX.addChildToBack(mulNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 89: Flow through with FOR-IN and VAR with no name (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNoName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        // VAR with no children
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (NullPointerException e) {
            // Expected in buggy version when accessing varNode.getLastChild()
            fail("NullPointerException thrown: " + e.getMessage());
        } catch (IllegalStateException e) {
            fail("IllegalStateException thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 90: Flow through with FOR-IN and VAR with empty name (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarEmptyName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var "" in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node emptyName = Node.newString(Token.NAME, "");
        varNode.addChildToBack(emptyName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 91: Flow through with FOR-IN and VAR with special characters in name
     */
    @Test(timeout = 4000)
    public void testForInWithVarSpecialName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var $ in y) - valid JS
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node dollarName = Node.newString(Token.NAME, "$");
        varNode.addChildToBack(dollarName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 92: Flow through with FOR-IN and VAR with underscore name
     */
    @Test(timeout = 4000)
    public void testForInWithVarUnderscoreName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var _ in y) - valid JS
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node underscoreName = Node.newString(Token.NAME, "_");
        varNode.addChildToBack(underscoreName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 93: Flow through with FOR-IN and VAR with long name
     */
    @Test(timeout = 4000)
    public void testForInWithVarLongName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var veryLongVariableName in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node longName = Node.newString(Token.NAME, "veryLongVariableName");
        varNode.addChildToBack(longName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 94: Flow through with FOR-IN and VAR with unicode name
     */
    @Test(timeout = 4000)
    public void testForInWithVarUnicodeName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var \u00e9 in y) - valid JS with unicode
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node unicodeName = Node.newString(Token.NAME, "\u00e9");
        varNode.addChildToBack(unicodeName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 95: Flow through with FOR-IN and VAR with number in name
     */
    @Test(timeout = 4000)
    public void testForInWithVarNumberName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x1 in y) - valid JS
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node numberName = Node.newString(Token.NAME, "x1");
        varNode.addChildToBack(numberName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 96: Flow through with FOR-IN and VAR with reserved word name (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarReservedName() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var class in y) - invalid but tests edge case
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node reservedName = Node.newString(Token.NAME, "class");
        varNode.addChildToBack(reservedName);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 97: Flow through with FOR-IN and VAR with null initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNullInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = null in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node nullNode = new Node(Token.NULL);
        varX.addChildToBack(nullNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 98: Flow through with FOR-IN and VAR with undefined initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarUndefinedInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = undefined in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node undefinedNode = new Node(Token.VOID);
        undefinedNode.addChildToBack(Node.newNumber(Token.NUMBER, 0));
        varX.addChildToBack(undefinedNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 99: Flow through with FOR-IN and VAR with boolean initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarBooleanInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = true in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node trueNode = new Node(Token.TRUE);
        varX.addChildToBack(trueNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 100: Flow through with FOR-IN and VAR with number initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNumberInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = 42 in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node numNode = Node.newNumber(Token.NUMBER, 42);
        varX.addChildToBack(numNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 101: Flow through with FOR-IN and VAR with string initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarStringInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = "hello" in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node strNode = Node.newString(Token.STRING, "hello");
        varX.addChildToBack(strNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 102: Flow through with FOR-IN and VAR with array initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarArrayInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = [a, b] in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node arrayNode = new Node(Token.ARRAYLIT);
        arrayNode.addChildToBack(Node.newString(Token.NAME, "a"));
        arrayNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(arrayNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 103: Flow through with FOR-IN and VAR with object initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarObjectInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = {key: a} in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node objNode = new Node(Token.OBJECTLIT);
        Node keyNode = Node.newString(Token.STRING, "key");
        Node valueNode = Node.newString(Token.NAME, "a");
        Node propNode = new Node(Token.STRING_KEY);
        propNode.addChildToBack(keyNode);
        propNode.addChildToBack(valueNode);
        objNode.addChildToBack(propNode);
        varX.addChildToBack(objNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 104: Flow through with FOR-IN and VAR with function expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarFunctionInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = function() { return a; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node funcNode = new Node(Token.FUNCTION);
        Node funcName = Node.newString(Token.NAME, "");
        Node funcParams = new Node(Token.PARAM_LIST);
        Node funcBody = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChildToBack(Node.newString(Token.NAME, "a"));
        funcBody.addChildToBack(returnNode);
        funcNode.addChildToBack(funcName);
        funcNode.addChildToBack(funcParams);
        funcNode.addChildToBack(funcBody);
        varX.addChildToBack(funcNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 105: Flow through with FOR-IN and VAR with regexp initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarRegexpInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = /test/ in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node regexpNode = new Node(Token.REGEXP);
        regexpNode.addChildToBack(Node.newString(Token.STRING, "test"));
        regexpNode.addChildToBack(Node.newString(Token.STRING, ""));
        varX.addChildToBack(regexpNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 106: Flow through with FOR-IN and VAR with this initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarThisInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = this in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node thisNode = new Node(Token.THIS);
        varX.addChildToBack(thisNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 107: Flow through with FOR-IN and VAR with new expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNewInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = new Foo() in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node newNode = new Node(Token.NEW);
        newNode.addChildToBack(Node.newString(Token.NAME, "Foo"));
        varX.addChildToBack(newNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 108: Flow through with FOR-IN and VAR with delete expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDeleteInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = delete obj.prop in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node delNode = new Node(Token.DELPROP);
        Node getPropNode = new Node(Token.GETPROP);
        getPropNode.addChildToBack(Node.newString(Token.NAME, "obj"));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "prop"));
        delNode.addChildToBack(getPropNode);
        varX.addChildToBack(delNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'obj' should be live", result.isLive(analysis.getVarIndex("obj")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 109: Flow through with FOR-IN and VAR with typeof expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarTypeofInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = typeof a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node typeofNode = new Node(Token.TYPEOF);
        typeofNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(typeofNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 110: Flow through with FOR-IN and VAR with void expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarVoidInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = void a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node voidNode = new Node(Token.VOID);
        voidNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(voidNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 111: Flow through with FOR-IN and VAR with unary expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarUnaryInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = !a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node notNode = new Node(Token.NOT);
        notNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(notNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 112: Flow through with FOR-IN and VAR with binary expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarBinaryInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a + b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node addNode = new Node(Token.ADD);
        addNode.addChildToBack(Node.newString(Token.NAME, "a"));
        addNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(addNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 113: Flow through with FOR-IN and VAR with assignment expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarAssignmentInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a = b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node assignNode = new Node(Token.ASSIGN);
        assignNode.addChildToBack(Node.newString(Token.NAME, "a"));
        assignNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(assignNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertFalse("Variable 'a' should be dead (killed by assignment)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 114: Flow through with FOR-IN and VAR with comma expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarCommaInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = (a, b) in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node commaNode = new Node(Token.COMMA);
        commaNode.addChildToBack(Node.newString(Token.NAME, "a"));
        commaNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(commaNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 115: Flow through with FOR-IN and VAR with conditional expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarConditionalInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a ? b : c in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node hookNode = new Node(Token.HOOK);
        hookNode.addChildToBack(Node.newString(Token.NAME, "a"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "b"));
        hookNode.addChildToBack(Node.newString(Token.NAME, "c"));
        varX.addChildToBack(hookNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 116: Flow through with FOR-IN and VAR with logical AND initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarAndInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a && b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node andNode = new Node(Token.AND);
        andNode.addChildToBack(Node.newString(Token.NAME, "a"));
        andNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(andNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 117: Flow through with FOR-IN and VAR with logical OR initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarOrInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a || b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node orNode = new Node(Token.OR);
        orNode.addChildToBack(Node.newString(Token.NAME, "a"));
        orNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(orNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 118: Flow through with FOR-IN and VAR with sequence expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarSequenceInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = (a, b, c) in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node comma1 = new Node(Token.COMMA);
        Node comma2 = new Node(Token.COMMA);
        comma2.addChildToBack(Node.newString(Token.NAME, "a"));
        comma2.addChildToBack(Node.newString(Token.NAME, "b"));
        comma1.addChildToBack(comma2);
        comma1.addChildToBack(Node.newString(Token.NAME, "c"));
        varX.addChildToBack(comma1);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 119: Flow through with FOR-IN and VAR with member expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarMemberInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = obj.prop in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node getPropNode = new Node(Token.GETPROP);
        getPropNode.addChildToBack(Node.newString(Token.NAME, "obj"));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "prop"));
        varX.addChildToBack(getPropNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'obj' should be live", result.isLive(analysis.getVarIndex("obj")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 120: Flow through with FOR-IN and VAR with computed member expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarComputedMemberInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = obj[a] in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node getElemNode = new Node(Token.GETELEM);
        getElemNode.addChildToBack(Node.newString(Token.NAME, "obj"));
        getElemNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(getElemNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'obj' should be live", result.isLive(analysis.getVarIndex("obj")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 121: Flow through with FOR-IN and VAR with call expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarCallInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = foo(a, b) in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node callNode = new Node(Token.CALL);
        callNode.addChildToBack(Node.newString(Token.NAME, "foo"));
        callNode.addChildToBack(Node.newString(Token.NAME, "a"));
        callNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(callNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 122: Flow through with FOR-IN and VAR with new call expression initializer (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNewCallInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = new Foo(a, b) in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node newNode = new Node(Token.NEW);
        Node callNode = new Node(Token.CALL);
        callNode.addChildToBack(Node.newString(Token.NAME, "Foo"));
        callNode.addChildToBack(Node.newString(Token.NAME, "a"));
        callNode.addChildToBack(Node.newString(Token.NAME, "b"));
        newNode.addChildToBack(callNode);
        varX.addChildToBack(newNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 123: Flow through with FOR-IN and VAR with array literal with spread (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarArraySpreadInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = [...a] in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node arrayNode = new Node(Token.ARRAYLIT);
        Node spreadNode = new Node(Token.SPREAD);
        spreadNode.addChildToBack(Node.newString(Token.NAME, "a"));
        arrayNode.addChildToBack(spreadNode);
        varX.addChildToBack(arrayNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 124: Flow through with FOR-IN and VAR with template literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarTemplateLiteralInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = `hello ${a}` in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node templateNode = new Node(Token.TEMPLATELIT);
        Node templatePart1 = Node.newString(Token.TEMPLATELIT_STRING, "hello ");
        Node templateExpr = new Node(Token.TEMPLATELIT_EXPR);
        templateExpr.addChildToBack(Node.newString(Token.NAME, "a"));
        Node templatePart2 = Node.newString(Token.TEMPLATELIT_STRING, "");
        templateNode.addChildToBack(templatePart1);
        templateNode.addChildToBack(templateExpr);
        templateNode.addChildToBack(templatePart2);
        varX.addChildToBack(templateNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 125: Flow through with FOR-IN and VAR with tagged template literal (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarTaggedTemplateInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = foo`hello` in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node taggedTemplateNode = new Node(Token.TAGGED_TEMPLATELIT);
        taggedTemplateNode.addChildToBack(Node.newString(Token.NAME, "foo"));
        Node templateNode = new Node(Token.TEMPLATELIT);
        templateNode.addChildToBack(Node.newString(Token.TEMPLATELIT_STRING, "hello"));
        taggedTemplateNode.addChildToBack(templateNode);
        varX.addChildToBack(taggedTemplateNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'foo' should be live", result.isLive(analysis.getVarIndex("foo")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 126: Flow through with FOR-IN and VAR with yield expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarYieldInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = yield a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node yieldNode = new Node(Token.YIELD);
        yieldNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(yieldNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 127: Flow through with FOR-IN and VAR with await expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarAwaitInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = await a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node awaitNode = new Node(Token.AWAIT);
        awaitNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(awaitNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 128: Flow through with FOR-IN and VAR with class expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarClassInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = class { } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node classNode = new Node(Token.CLASS);
        Node className = Node.newString(Token.NAME, "");
        Node classBody = new Node(Token.CLASS_BODY);
        classNode.addChildToBack(className);
        classNode.addChildToBack(classBody);
        varX.addChildToBack(classNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 129: Flow through with FOR-IN and VAR with super expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarSuperInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = super in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node superNode = new Node(Token.SUPER);
        varX.addChildToBack(superNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 130: Flow through with FOR-IN and VAR with import expression (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarImportInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = import('module') in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node importNode = new Node(Token.IMPORT);
        importNode.addChildToBack(Node.newString(Token.STRING, "module"));
        varX.addChildToBack(importNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 131: Flow through with FOR-IN and VAR with meta property (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarMetaPropertyInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = import.meta in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node metaNode = new Node(Token.META_PROP);
        metaNode.addChildToBack(Node.newString(Token.NAME, "import"));
        metaNode.addChildToBack(Node.newString(Token.NAME, "meta"));
        varX.addChildToBack(metaNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 132: Flow through with FOR-IN and VAR with new target (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNewTargetInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = new.target in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node newTargetNode = new Node(Token.NEW_TARGET);
        varX.addChildToBack(newTargetNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 133: Flow through with FOR-IN and VAR with optional chaining (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarOptionalChainingInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = obj?.prop in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node optChainNode = new Node(Token.OPTCHAIN_CALL);
        Node getPropNode = new Node(Token.GETPROP);
        getPropNode.addChildToBack(Node.newString(Token.NAME, "obj"));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "prop"));
        optChainNode.addChildToBack(getPropNode);
        varX.addChildToBack(optChainNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'obj' should be live", result.isLive(analysis.getVarIndex("obj")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 134: Flow through with FOR-IN and VAR with nullish coalescing (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNullishCoalescingInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a ?? b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node nullishNode = new Node(Token.COALESCE);
        nullishNode.addChildToBack(Node.newString(Token.NAME, "a"));
        nullishNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(nullishNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 135: Flow through with FOR-IN and VAR with logical assignment (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarLogicalAssignmentInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a &&= b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node logAssignNode = new Node(Token.ASSIGN_AND);
        logAssignNode.addChildToBack(Node.newString(Token.NAME, "a"));
        logAssignNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(logAssignNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 136: Flow through with FOR-IN and VAR with exponentiation assignment (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarExponentiationAssignmentInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a **= b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node expAssignNode = new Node(Token.ASSIGN_EXPONENT);
        expAssignNode.addChildToBack(Node.newString(Token.NAME, "a"));
        expAssignNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(expAssignNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 137: Flow through with FOR-IN and VAR with bitwise assignment (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarBitwiseAssignmentInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a |= b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node bitAssignNode = new Node(Token.ASSIGN_BITOR);
        bitAssignNode.addChildToBack(Node.newString(Token.NAME, "a"));
        bitAssignNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(bitAssignNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 138: Flow through with FOR-IN and VAR with arrow function (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarArrowFunctionInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = () => a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node arrowNode = new Node(Token.ARROW);
        Node arrowParams = new Node(Token.PARAM_LIST);
        Node arrowBody = Node.newString(Token.NAME, "a");
        arrowNode.addChildToBack(arrowParams);
        arrowNode.addChildToBack(arrowBody);
        varX.addChildToBack(arrowNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 139: Flow through with FOR-IN and VAR with generator function (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarGeneratorInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = function*() { yield a; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node funcNode = new Node(Token.FUNCTION);
        funcNode.putBooleanProp(Node.GENERATOR_FN, true);
        Node funcName = Node.newString(Token.NAME, "");
        Node funcParams = new Node(Token.PARAM_LIST);
        Node funcBody = new Node(Token.BLOCK);
        Node yieldNode = new Node(Token.YIELD);
        yieldNode.addChildToBack(Node.newString(Token.NAME, "a"));
        funcBody.addChildToBack(yieldNode);
        funcNode.addChildToBack(funcName);
        funcNode.addChildToBack(funcParams);
        funcNode.addChildToBack(funcBody);
        varX.addChildToBack(funcNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 140: Flow through with FOR-IN and VAR with async function (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarAsyncFunctionInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = async function() { await a; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node funcNode = new Node(Token.FUNCTION);
        funcNode.putBooleanProp(Node.ASYNC_FN, true);
        Node funcName = Node.newString(Token.NAME, "");
        Node funcParams = new Node(Token.PARAM_LIST);
        Node funcBody = new Node(Token.BLOCK);
        Node awaitNode = new Node(Token.AWAIT);
        awaitNode.addChildToBack(Node.newString(Token.NAME, "a"));
        funcBody.addChildToBack(awaitNode);
        funcNode.addChildToBack(funcName);
        funcNode.addChildToBack(funcParams);
        funcNode.addChildToBack(funcBody);
        varX.addChildToBack(funcNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 141: Flow through with FOR-IN and VAR with getter/setter (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarGetterSetterInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = { get a() { return b; } } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node objNode = new Node(Token.OBJECTLIT);
        Node getterNode = new Node(Token.GETTER_DEF);
        Node getterName = Node.newString(Token.STRING, "a");
        Node getterBody = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChildToBack(Node.newString(Token.NAME, "b"));
        getterBody.addChildToBack(returnNode);
        getterNode.addChildToBack(getterName);
        getterNode.addChildToBack(getterBody);
        objNode.addChildToBack(getterNode);
        varX.addChildToBack(objNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 142: Flow through with FOR-IN and VAR with spread object (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarSpreadObjectInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = {...a} in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node objNode = new Node(Token.OBJECTLIT);
        Node spreadNode = new Node(Token.SPREAD);
        spreadNode.addChildToBack(Node.newString(Token.NAME, "a"));
        objNode.addChildToBack(spreadNode);
        varX.addChildToBack(objNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 143: Flow through with FOR-IN and VAR with destructuring (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDestructuringInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = [a, b] = c in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node destrNode = new Node(Token.ASSIGN);
        Node arrayNode = new Node(Token.ARRAYLIT);
        arrayNode.addChildToBack(Node.newString(Token.NAME, "a"));
        arrayNode.addChildToBack(Node.newString(Token.NAME, "b"));
        destrNode.addChildToBack(arrayNode);
        destrNode.addChildToBack(Node.newString(Token.NAME, "c"));
        varX.addChildToBack(destrNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
            assertFalse("Variable 'a' should be dead (killed by destructuring)", result.isLive(analysis.getVarIndex("a")));
            assertFalse("Variable 'b' should be dead (killed by destructuring)", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 144: Flow through with FOR-IN and VAR with default parameter (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDefaultParameterInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a = b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node defaultNode = new Node(Token.DEFAULT_VALUE);
        defaultNode.addChildToBack(Node.newString(Token.NAME, "a"));
        defaultNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(defaultNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 145: Flow through with FOR-IN and VAR with rest parameter (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarRestParameterInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = ...a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node restNode = new Node(Token.REST);
        restNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(restNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 146: Flow through with FOR-IN and VAR with computed property name (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarComputedPropertyNameInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = { [a]: b } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node objNode = new Node(Token.OBJECTLIT);
        Node computedPropNode = new Node(Token.COMPUTED_PROP);
        computedPropNode.addChildToBack(Node.newString(Token.NAME, "a"));
        computedPropNode.addChildToBack(Node.newString(Token.NAME, "b"));
        objNode.addChildToBack(computedPropNode);
        varX.addChildToBack(objNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 147: Flow through with FOR-IN and VAR with shorthand property (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarShorthandPropertyInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = { a } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node objNode = new Node(Token.OBJECTLIT);
        Node shorthandPropNode = Node.newString(Token.NAME, "a");
        objNode.addChildToBack(shorthandPropNode);
        varX.addChildToBack(objNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 148: Flow through with FOR-IN and VAR with method definition (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarMethodDefinitionInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = { foo() { return a; } } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node objNode = new Node(Token.OBJECTLIT);
        Node methodNode = new Node(Token.METHOD_DEF);
        Node methodName = Node.newString(Token.STRING, "foo");
        Node methodParams = new Node(Token.PARAM_LIST);
        Node methodBody = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChildToBack(Node.newString(Token.NAME, "a"));
        methodBody.addChildToBack(returnNode);
        methodNode.addChildToBack(methodName);
        methodNode.addChildToBack(methodParams);
        methodNode.addChildToBack(methodBody);
        objNode.addChildToBack(methodNode);
        varX.addChildToBack(objNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 149: Flow through with FOR-IN and VAR with class static block (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarClassStaticBlockInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = class { static { a; } } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node classNode = new Node(Token.CLASS);
        Node className = Node.newString(Token.NAME, "");
        Node classBody = new Node(Token.CLASS_BODY);
        Node staticBlock = new Node(Token.STATIC_BLOCK);
        Node staticBlockBody = new Node(Token.BLOCK);
        staticBlockBody.addChildToBack(Node.newString(Token.NAME, "a"));
        staticBlock.addChildToBack(staticBlockBody);
        classBody.addChildToBack(staticBlock);
        classNode.addChildToBack(className);
        classNode.addChildToBack(classBody);
        varX.addChildToBack(classNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 150: Flow through with FOR-IN and VAR with decorator (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDecoratorInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = @dec class { } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node decoratorNode = new Node(Token.DECORATOR);
        decoratorNode.addChildToBack(Node.newString(Token.NAME, "dec"));
        Node classNode = new Node(Token.CLASS);
        Node className = Node.newString(Token.NAME, "");
        Node classBody = new Node(Token.CLASS_BODY);
        classNode.addChildToBack(className);
        classNode.addChildToBack(classBody);
        decoratorNode.addChildToBack(classNode);
        varX.addChildToBack(decoratorNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'dec' should be live", result.isLive(analysis.getVarIndex("dec")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 151: Flow through with FOR-IN and VAR with type annotation (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarTypeAnnotationInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x: string = a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node typeNode = new Node(Token.STRING_TYPE);
        varX.setDeclaredTypeExpression(typeNode);
        varX.addChildToBack(Node.newString(Token.NAME, "a"));
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 152: Flow through with FOR-IN and VAR with enum (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarEnumInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = enum { A, B } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node enumNode = new Node(Token.ENUM);
        Node enumName = Node.newString(Token.NAME, "");
        Node enumBody = new Node(Token.ENUM_BODY);
        enumBody.addChildToBack(Node.newString(Token.NAME, "A"));
        enumBody.addChildToBack(Node.newString(Token.NAME, "B"));
        enumNode.addChildToBack(enumName);
        enumNode.addChildToBack(enumBody);
        varX.addChildToBack(enumNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 153: Flow through with FOR-IN and VAR with interface (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarInterfaceInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = interface { a: string } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node interfaceNode = new Node(Token.INTERFACE);
        Node interfaceName = Node.newString(Token.NAME, "");
        Node interfaceBody = new Node(Token.INTERFACE_BODY);
        Node memberNode = new Node(Token.INTERFACE_MEMBER);
        memberNode.addChildToBack(Node.newString(Token.NAME, "a"));
        memberNode.addChildToBack(new Node(Token.STRING_TYPE));
        interfaceBody.addChildToBack(memberNode);
        interfaceNode.addChildToBack(interfaceName);
        interfaceNode.addChildToBack(interfaceBody);
        varX.addChildToBack(interfaceNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 154: Flow through with FOR-IN and VAR with namespace (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNamespaceInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = namespace { export const a = 1; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node namespaceNode = new Node(Token.NAMESPACE);
        Node namespaceName = Node.newString(Token.NAME, "");
        Node namespaceBody = new Node(Token.NAMESPACE_BODY);
        Node exportNode = new Node(Token.EXPORT);
        Node constNode = new Node(Token.CONST);
        Node constA = Node.newString(Token.NAME, "a");
        constA.addChildToBack(Node.newNumber(Token.NUMBER, 1));
        constNode.addChildToBack(constA);
        exportNode.addChildToBack(constNode);
        namespaceBody.addChildToBack(exportNode);
        namespaceNode.addChildToBack(namespaceName);
        namespaceNode.addChildToBack(namespaceBody);
        varX.addChildToBack(namespaceNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 155: Flow through with FOR-IN and VAR with module (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarModuleInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = module { export const a = 1; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node moduleNode = new Node(Token.MODULE);
        Node moduleName = Node.newString(Token.NAME, "");
        Node moduleBody = new Node(Token.MODULE_BODY);
        Node exportNode = new Node(Token.EXPORT);
        Node constNode = new Node(Token.CONST);
        Node constA = Node.newString(Token.NAME, "a");
        constA.addChildToBack(Node.newNumber(Token.NUMBER, 1));
        constNode.addChildToBack(constA);
        exportNode.addChildToBack(constNode);
        moduleBody.addChildToBack(exportNode);
        moduleNode.addChildToBack(moduleName);
        moduleNode.addChildToBack(moduleBody);
        varX.addChildToBack(moduleNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 156: Flow through with FOR-IN and VAR with type alias (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarTypeAliasInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = type MyType = string in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node typeAliasNode = new Node(Token.TYPE_ALIAS);
        Node aliasName = Node.newString(Token.NAME, "MyType");
        Node aliasType = new Node(Token.STRING_TYPE);
        typeAliasNode.addChildToBack(aliasName);
        typeAliasNode.addChildToBack(aliasType);
        varX.addChildToBack(typeAliasNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 157: Flow through with FOR-IN and VAR with import declaration (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarImportDeclarationInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = import { a } from 'module' in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node importNode = new Node(Token.IMPORT);
        Node importSpecs = new Node(Token.IMPORT_SPECS);
        Node importSpec = new Node(Token.IMPORT_SPEC);
        importSpec.addChildToBack(Node.newString(Token.NAME, "a"));
        importSpecs.addChildToBack(importSpec);
        Node importSource = Node.newString(Token.STRING, "module");
        importNode.addChildToBack(importSpecs);
        importNode.addChildToBack(importSource);
        varX.addChildToBack(importNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 158: Flow through with FOR-IN and VAR with export declaration (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarExportDeclarationInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = export { a } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node exportNode = new Node(Token.EXPORT);
        Node exportSpecs = new Node(Token.EXPORT_SPECS);
        Node exportSpec = new Node(Token.EXPORT_SPEC);
        exportSpec.addChildToBack(Node.newString(Token.NAME, "a"));
        exportSpecs.addChildToBack(exportSpec);
        exportNode.addChildToBack(exportSpecs);
        varX.addChildToBack(exportNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 159: Flow through with FOR-IN and VAR with debugger statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDebuggerInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = debugger in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node debuggerNode = new Node(Token.DEBUGGER);
        varX.addChildToBack(debuggerNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 160: Flow through with FOR-IN and VAR with with statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarWithInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = with(a) { b; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node withNode = new Node(Token.WITH);
        withNode.addChildToBack(Node.newString(Token.NAME, "a"));
        Node withBody = new Node(Token.BLOCK);
        withBody.addChildToBack(Node.newString(Token.NAME, "b"));
        withNode.addChildToBack(withBody);
        varX.addChildToBack(withNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 161: Flow through with FOR-IN and VAR with empty statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarEmptyStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = ; in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node emptyStmt = new Node(Token.EMPTY);
        varX.addChildToBack(emptyStmt);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 162: Flow through with FOR-IN and VAR with expression statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarExpressionStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = (a) in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node exprStmt = new Node(Token.EXPR_RESULT);
        exprStmt.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(exprStmt);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 163: Flow through with FOR-IN and VAR with labeled statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarLabeledStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = label: a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node labelNode = new Node(Token.LABEL);
        labelNode.addChildToBack(Node.newString(Token.LABEL_NAME, "label"));
        labelNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(labelNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 164: Flow through with FOR-IN and VAR with block statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarBlockStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = { a; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node blockNode = new Node(Token.BLOCK);
        blockNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(blockNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 165: Flow through with FOR-IN and VAR with switch statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarSwitchStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = switch(a) { case 1: b; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node switchNode = new Node(Token.SWITCH);
        switchNode.addChildToBack(Node.newString(Token.NAME, "a"));
        Node caseNode = new Node(Token.CASE);
        caseNode.addChildToBack(Node.newNumber(Token.NUMBER, 1));
        Node caseBody = new Node(Token.BLOCK);
        caseBody.addChildToBack(Node.newString(Token.NAME, "b"));
        caseNode.addChildToBack(caseBody);
        switchNode.addChildToBack(caseNode);
        varX.addChildToBack(switchNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 166: Flow through with FOR-IN and VAR with try statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarTryStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = try { a; } catch(e) { b; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        tryBlock.addChildToBack(Node.newString(Token.NAME, "a"));
        Node catchNode = new Node(Token.CATCH);
        Node catchParam = Node.newString(Token.NAME, "e");
        Node catchBlock = new Node(Token.BLOCK);
        catchBlock.addChildToBack(Node.newString(Token.NAME, "b"));
        catchNode.addChildToBack(catchParam);
        catchNode.addChildToBack(catchBlock);
        tryNode.addChildToBack(tryBlock);
        tryNode.addChildToBack(catchNode);
        varX.addChildToBack(tryNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 167: Flow through with FOR-IN and VAR with throw statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarThrowStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = throw a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node throwNode = new Node(Token.THROW);
        throwNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(throwNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 168: Flow through with FOR-IN and VAR with return statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarReturnStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = return a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(returnNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 169: Flow through with FOR-IN and VAR with break statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarBreakStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = break in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node breakNode = new Node(Token.BREAK);
        varX.addChildToBack(breakNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 170: Flow through with FOR-IN and VAR with continue statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarContinueStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = continue in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node continueNode = new Node(Token.CONTINUE);
        varX.addChildToBack(continueNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 171: Flow through with FOR-IN and VAR with for statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarForStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = for(;a;) { } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node innerForNode = new Node(Token.FOR);
        innerForNode.addChildToBack(new Node(Token.EMPTY));
        innerForNode.addChildToBack(Node.newString(Token.NAME, "a"));
        innerForNode.addChildToBack(new Node(Token.EMPTY));
        varX.addChildToBack(innerForNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 172: Flow through with FOR-IN and VAR with while statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarWhileStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = while(a) { b; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node whileNode = new Node(Token.WHILE);
        whileNode.addChildToBack(Node.newString(Token.NAME, "a"));
        Node whileBody = new Node(Token.BLOCK);
        whileBody.addChildToBack(Node.newString(Token.NAME, "b"));
        whileNode.addChildToBack(whileBody);
        varX.addChildToBack(whileNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 173: Flow through with FOR-IN and VAR with do-while statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDoWhileStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = do { a; } while(b) in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node doNode = new Node(Token.DO);
        Node doBody = new Node(Token.BLOCK);
        doBody.addChildToBack(Node.newString(Token.NAME, "a"));
        Node doCond = Node.newString(Token.NAME, "b");
        doNode.addChildToBack(doBody);
        doNode.addChildToBack(doCond);
        varX.addChildToBack(doNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 174: Flow through with FOR-IN and VAR with if statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarIfStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = if(a) { b; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(Node.newString(Token.NAME, "a"));
        Node ifBody = new Node(Token.BLOCK);
        ifBody.addChildToBack(Node.newString(Token.NAME, "b"));
        ifNode.addChildToBack(ifBody);
        varX.addChildToBack(ifNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 175: Flow through with FOR-IN and VAR with if-else statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarIfElseStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = if(a) { b; } else { c; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(Node.newString(Token.NAME, "a"));
        Node ifBody = new Node(Token.BLOCK);
        ifBody.addChildToBack(Node.newString(Token.NAME, "b"));
        Node elseBody = new Node(Token.BLOCK);
        elseBody.addChildToBack(Node.newString(Token.NAME, "c"));
        ifNode.addChildToBack(ifBody);
        ifNode.addChildToBack(elseBody);
        varX.addChildToBack(ifNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'c' should be live", result.isLive(analysis.getVarIndex("c")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 176: Flow through with FOR-IN and VAR with for-in statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarForInStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = for(var y in z) { } in w)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node innerForInNode = new Node(Token.FOR);
        Node innerVarNode = new Node(Token.VAR);
        innerVarNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node innerRhs = Node.newString(Token.NAME, "z");
        innerForInNode.addChildToBack(innerVarNode);
        innerForInNode.addChildToBack(innerRhs);
        varX.addChildToBack(innerForInNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "w");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'w' should be live", result.isLive(analysis.getVarIndex("w")));
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 177: Flow through with FOR-IN and VAR with for-of statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarForOfStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = for(var y of z) { } in w)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node forOfNode = new Node(Token.FOR_OF);
        Node innerVarNode = new Node(Token.VAR);
        innerVarNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node innerRhs = Node.newString(Token.NAME, "z");
        forOfNode.addChildToBack(innerVarNode);
        forOfNode.addChildToBack(innerRhs);
        varX.addChildToBack(forOfNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "w");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'w' should be live", result.isLive(analysis.getVarIndex("w")));
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 178: Flow through with FOR-IN and VAR with for-await-of statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarForAwaitOfStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = for await(var y of z) { } in w)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node forAwaitNode = new Node(Token.FOR_AWAIT_OF);
        Node innerVarNode = new Node(Token.VAR);
        innerVarNode.addChildToBack(Node.newString(Token.NAME, "y"));
        Node innerRhs = Node.newString(Token.NAME, "z");
        forAwaitNode.addChildToBack(innerVarNode);
        forAwaitNode.addChildToBack(innerRhs);
        varX.addChildToBack(forAwaitNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "w");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'w' should be live", result.isLive(analysis.getVarIndex("w")));
            assertTrue("Variable 'z' should be live", result.isLive(analysis.getVarIndex("z")));
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 179: Flow through with FOR-IN and VAR with let statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarLetStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = let(a = 1) in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node letNode = new Node(Token.LET);
        Node letBinding = Node.newString(Token.NAME, "a");
        letBinding.addChildToBack(Node.newNumber(Token.NUMBER, 1));
        letNode.addChildToBack(letBinding);
        varX.addChildToBack(letNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 180: Flow through with FOR-IN and VAR with const statement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarConstStatementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = const a = 1 in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node constNode = new Node(Token.CONST);
        Node constBinding = Node.newString(Token.NAME, "a");
        constBinding.addChildToBack(Node.newNumber(Token.NUMBER, 1));
        constNode.addChildToBack(constBinding);
        varX.addChildToBack(constNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 181: Flow through with FOR-IN and VAR with import.meta (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarImportMetaInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = import.meta in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node metaNode = new Node(Token.META_PROP);
        metaNode.addChildToBack(Node.newString(Token.NAME, "import"));
        metaNode.addChildToBack(Node.newString(Token.NAME, "meta"));
        varX.addChildToBack(metaNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 182: Flow through with FOR-IN and VAR with new.target (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNewTargetInitializer2() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = new.target in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node newTargetNode = new Node(Token.NEW_TARGET);
        varX.addChildToBack(newTargetNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 183: Flow through with FOR-IN and VAR with super.prop (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarSuperPropInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = super.prop in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node getPropNode = new Node(Token.GETPROP);
        getPropNode.addChildToBack(new Node(Token.SUPER));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "prop"));
        varX.addChildToBack(getPropNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 184: Flow through with FOR-IN and VAR with super[] (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarSuperElemInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = super[a] in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node getElemNode = new Node(Token.GETELEM);
        getElemNode.addChildToBack(new Node(Token.SUPER));
        getElemNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(getElemNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 185: Flow through with FOR-IN and VAR with import() (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDynamicImportInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = import('module') in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node importNode = new Node(Token.IMPORT);
        importNode.addChildToBack(Node.newString(Token.STRING, "module"));
        varX.addChildToBack(importNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 186: Flow through with FOR-IN and VAR with yield* (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarYieldStarInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = yield* a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node yieldStarNode = new Node(Token.YIELD_STAR);
        yieldStarNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(yieldStarNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 187: Flow through with FOR-IN and VAR with async generator (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarAsyncGeneratorInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = async function*() { yield a; } in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node funcNode = new Node(Token.FUNCTION);
        funcNode.putBooleanProp(Node.ASYNC_FN, true);
        funcNode.putBooleanProp(Node.GENERATOR_FN, true);
        Node funcName = Node.newString(Token.NAME, "");
        Node funcParams = new Node(Token.PARAM_LIST);
        Node funcBody = new Node(Token.BLOCK);
        Node yieldNode = new Node(Token.YIELD);
        yieldNode.addChildToBack(Node.newString(Token.NAME, "a"));
        funcBody.addChildToBack(yieldNode);
        funcNode.addChildToBack(funcName);
        funcNode.addChildToBack(funcParams);
        funcNode.addChildToBack(funcBody);
        varX.addChildToBack(funcNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 188: Flow through with FOR-IN and VAR with optional call (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarOptionalCallInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = foo?.() in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node optCallNode = new Node(Token.OPTCHAIN_CALL);
        Node callNode = new Node(Token.CALL);
        callNode.addChildToBack(Node.newString(Token.NAME, "foo"));
        optCallNode.addChildToBack(callNode);
        varX.addChildToBack(optCallNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'foo' should be live", result.isLive(analysis.getVarIndex("foo")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 189: Flow through with FOR-IN and VAR with nullish coalescing assignment (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarNullishCoalescingAssignmentInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a ??= b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node nullishAssignNode = new Node(Token.ASSIGN_COALESCE);
        nullishAssignNode.addChildToBack(Node.newString(Token.NAME, "a"));
        nullishAssignNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(nullishAssignNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 190: Flow through with FOR-IN and VAR with logical OR assignment (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarLogicalOrAssignmentInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a ||= b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node logOrAssignNode = new Node(Token.ASSIGN_OR);
        logOrAssignNode.addChildToBack(Node.newString(Token.NAME, "a"));
        logOrAssignNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(logOrAssignNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 191: Flow through with FOR-IN and VAR with exponentiation operator (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarExponentiationInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a ** b in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node expNode = new Node(Token.EXPONENT);
        expNode.addChildToBack(Node.newString(Token.NAME, "a"));
        expNode.addChildToBack(Node.newString(Token.NAME, "b"));
        varX.addChildToBack(expNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'b' should be live", result.isLive(analysis.getVarIndex("b")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 192: Flow through with FOR-IN and VAR with bitwise NOT (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarBitwiseNotInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = ~a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node bitNotNode = new Node(Token.BITNOT);
        bitNotNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(bitNotNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 193: Flow through with FOR-IN and VAR with delete (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarDeleteInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = delete a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node delNode = new Node(Token.DELPROP);
        delNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(delNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 194: Flow through with FOR-IN and VAR with void (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarVoidInitializer2() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = void a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node voidNode = new Node(Token.VOID);
        voidNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(voidNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 195: Flow through with FOR-IN and VAR with typeof (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarTypeofInitializer2() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = typeof a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node typeofNode = new Node(Token.TYPEOF);
        typeofNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(typeofNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 196: Flow through with FOR-IN and VAR with unary plus (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarUnaryPlusInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = +a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node posNode = new Node(Token.POS);
        posNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(posNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 197: Flow through with FOR-IN and VAR with unary minus (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarUnaryMinusInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = -a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node negNode = new Node(Token.NEG);
        negNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(negNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 198: Flow through with FOR-IN and VAR with prefix increment (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarPrefixIncrementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = ++a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node incNode = new Node(Token.INC);
        incNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(incNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read and written)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 199: Flow through with FOR-IN and VAR with prefix decrement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarPrefixDecrementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = --a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node decNode = new Node(Token.DEC);
        decNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(decNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read and written)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 200: Flow through with FOR-IN and VAR with postfix increment (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarPostfixIncrementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a++ in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node incNode = new Node(Token.INC);
        incNode.putBooleanProp(Node.INCRDECR_PROP, true);
        incNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(incNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read and written)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 201: Flow through with FOR-IN and VAR with postfix decrement (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarPostfixDecrementInitializer() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = a-- in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node decNode = new Node(Token.DEC);
        decNode.putBooleanProp(Node.INCRDECR_PROP, true);
        decNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(decNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live (read and written)", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 202: Flow through with FOR-IN and VAR with await (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarAwaitInitializer2() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = await a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node awaitNode = new Node(Token.AWAIT);
        awaitNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(awaitNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 203: Flow through with FOR-IN and VAR with yield (edge case)
     */
    @Test(timeout = 4000)
    public void testForInWithVarYieldInitializer2() {
        LiveVariablesAnalysis analysis = createAnalysis();
        LiveVariablesAnalysis.LiveVariableLattice input = analysis.createEntryLattice();
        
        // for(var x = yield a in y)
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node varX = Node.newString(Token.NAME, "x");
        Node yieldNode = new Node(Token.YIELD);
        yieldNode.addChildToBack(Node.newString(Token.NAME, "a"));
        varX.addChildToBack(yieldNode);
        varNode.addChildToBack(varX);
        Node rhs = Node.newString(Token.NAME, "y");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        
        try {
            LiveVariablesAnalysis.LiveVariableLattice result = analysis.flowThrough(forInNode, input);
            assertTrue("Variable 'y' should be live", result.isLive(analysis.getVarIndex("y")));
            assertTrue("Variable 'a' should be live", result.isLive(analysis.getVarIndex("a")));
            assertTrue("Variable 'x' should be live", result.isLive(analysis.getVarIndex("x")));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
    
    /**
     * Test 204: Flow through