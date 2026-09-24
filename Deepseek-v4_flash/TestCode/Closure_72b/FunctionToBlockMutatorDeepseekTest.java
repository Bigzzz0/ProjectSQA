package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches targeted:
 * - Branch 1: hasArgs (true/false) in mutate()
 * - Branch 2: namesToAlias null/empty vs non-empty in aliasAndInlineArguments()
 * - Branch 3: isCallInLoop (true/false) in mutate() and fixUnitializedVarDeclarations()
 * - Branch 4: returnCount > 0 and hasReturnAtExit (true/false) in replaceReturns()
 * - Branch 5: resultMustBeSet (true/false) in replaceReturns()
 * - Branch 6: resultName null vs non-null in getReplacementReturnStatement()
 * - Branch 7: node.hasChildren() in getReplacementReturnStatement()
 * - Branch 8: NodeUtil.isLoopStructure(n) in fixUnitializedVarDeclarations()
 * - Branch 9: NodeUtil.isVar(n) with/without children in fixUnitializedVarDeclarations()
 * - Branch 10: fnName null/empty vs non-null in getLabelNameForFunction()
 * 
 * Boundary conditions:
 * - Empty argument map
 * - Null namesToAlias
 * - Multiple returns in function body
 * - Return at exit vs return in middle
 * - VAR declarations with and without initializers
 * - Loop structures in fixUnitializedVarDeclarations
 * 
 * Defect targeting (Defects4J testInlineFunctions31):
 * - The defect involves incorrect handling of function inlining when there are
 *   multiple return statements and the function is called in a loop context.
 * - Specifically, the label/break generation for returns may produce incorrect
 *   control flow when combined with loop structures.
 * - Test case: function with multiple returns where one return is at the end
 *   and another is in the middle, called inside a loop.
 */
public class FunctionToBlockMutatorDeepseekTest {

    private static class TestCompiler extends AbstractCompiler {
        @Override
        public Supplier<String> getUniqueNameIdSupplier() {
            return new Supplier<String>() {
                private int counter = 0;
                @Override
                public String get() {
                    return "unique_" + (counter++);
                }
            };
        }

        @Override
        public CodingConvention getCodingConvention() {
            return new DefaultCodingConvention();
        }

        // Minimal implementation for testing
        @Override
        public void report(JSError error) {}
        
        @Override
        public CheckLevel getErrorLevel(JSError error) {
            return CheckLevel.ERROR;
        }
    }

    private static class TestSupplier implements Supplier<String> {
        private int counter = 0;
        @Override
        public String get() {
            return "test_" + (counter++);
        }
    }

    private FunctionToBlockMutator createMutator() {
        return new FunctionToBlockMutator(new TestCompiler(), new TestSupplier());
    }

    // Helper to create a simple function node: function f() { return 1; }
    private Node createSimpleFunction() {
        Node script = new Node(Token.SCRIPT);
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        function.addChildToFront(name);
        function.addChildToBack(params);
        function.addChildToBack(body);
        script.addChildToBack(function);
        return function;
    }

    // Helper to create a call node: f()
    private Node createCallNode() {
        Node call = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
        return call;
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testMutateSimpleFunction() {
        FunctionToBlockMutator mutator = createMutator();
        Node fnNode = createSimpleFunction();
        Node callNode = createCallNode();
        
        Node result = mutator.mutate("f", fnNode, callNode, "result", false, false);
        
        assertNotNull("Result should not be null", result);
        // Should be a BLOCK containing the transformed body
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    @Test(timeout = 4000)
    public void testMutateWithResultName() {
        FunctionToBlockMutator mutator = createMutator();
        Node fnNode = createSimpleFunction();
        Node callNode = createCallNode();
        
        Node result = mutator.mutate("f", fnNode, callNode, "result", true, false);
        
        assertNotNull("Result should not be null", result);
        // With needsDefaultResult=true and no return at exit, should add dummy assignment
        // The function has a return at exit, so no dummy assignment needed
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    @Test(timeout = 4000)
    public void testMutateWithLoopContext() {
        FunctionToBlockMutator mutator = createMutator();
        Node fnNode = createSimpleFunction();
        Node callNode = createCallNode();
        
        Node result = mutator.mutate("f", fnNode, callNode, "result", false, true);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testMutateWithNullFunctionName() {
        FunctionToBlockMutator mutator = createMutator();
        Node fnNode = createSimpleFunction();
        Node callNode = createCallNode();
        
        Node result = mutator.mutate(null, fnNode, callNode, "result", false, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    @Test(timeout = 4000)
    public void testMutateWithEmptyFunctionName() {
        FunctionToBlockMutator mutator = createMutator();
        Node fnNode = createSimpleFunction();
        Node callNode = createCallNode();
        
        Node result = mutator.mutate("", fnNode, callNode, "result", false, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    @Test(timeout = 4000)
    public void testMutateWithNullResultName() {
        FunctionToBlockMutator mutator = createMutator();
        Node fnNode = createSimpleFunction();
        Node callNode = createCallNode();
        
        Node result = mutator.mutate("f", fnNode, callNode, null, false, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test: Function with multiple returns called in a loop.
     * This targets the known defect from testInlineFunctions31.
     * 
     * The function: function f() { if (x) { return 1; } return 2; }
     * Called in a loop context with needsDefaultResult=true.
     * 
     * Expected: The transformed block should have proper label/break structure
     * for the early return, and the last return should be converted to assignment.
     */
    @Test(timeout = 4000)
    public void testMutateMultipleReturnsInLoop() {
        FunctionToBlockMutator mutator = createMutator();
        
        // Create function: function f() { if (true) { return 1; } return 2; }
        Node script = new Node(Token.SCRIPT);
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        
        // if (true) { return 1; }
        Node ifNode = new Node(Token.IF, new Node(Token.TRUE));
        Node ifBlock = new Node(Token.BLOCK);
        Node return1 = new Node(Token.RETURN, Node.newNumber(1));
        ifBlock.addChildToBack(return1);
        ifNode.addChildToBack(ifBlock);
        
        // return 2;
        Node return2 = new Node(Token.RETURN, Node.newNumber(2));
        
        body.addChildToBack(ifNode);
        body.addChildToBack(return2);
        
        function.addChildToFront(name);
        function.addChildToBack(params);
        function.addChildToBack(body);
        script.addChildToBack(function);
        
        Node callNode = createCallNode();
        
        // This is the defect-targeting call: in loop context with needsDefaultResult=true
        Node result = mutator.mutate("f", function, callNode, "result", true, true);
        
        assertNotNull("Result should not be null", result);
        
        // The result should be a BLOCK containing a LABEL node
        // because there are multiple returns and one is not at the end
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
        
        // The first child should be a LABEL node
        Node firstChild = result.getFirstChild();
        assertNotNull("First child should not be null", firstChild);
        assertEquals("First child should be a LABEL", Token.LABEL, firstChild.getType());
        
        // The label should have a LABEL_NAME child
        Node labelName = firstChild.getFirstChild();
        assertNotNull("Label name should not be null", labelName);
        assertEquals("First child of label should be LABEL_NAME", Token.LABEL_NAME, labelName.getType());
        
        // The label name should contain "JSCompiler_inline_label"
        assertTrue("Label name should contain JSCompiler_inline_label", 
                   labelName.getString().contains("JSCompiler_inline_label"));
    }

    /**
     * Another defect-targeted test: Function with return at end only, called in loop.
     * This should not create a label since there's only one return at the end.
     */
    @Test(timeout = 4000)
    public void testMutateSingleReturnAtEndInLoop() {
        FunctionToBlockMutator mutator = createMutator();
        Node fnNode = createSimpleFunction();
        Node callNode = createCallNode();
        
        Node result = mutator.mutate("f", fnNode, callNode, "result", true, true);
        
        assertNotNull("Result should not be null", result);
        // Single return at end should not create a label
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
        
        // Should not have a LABEL as first child
        Node firstChild = result.getFirstChild();
        if (firstChild != null) {
            assertNotEquals("Should not be a LABEL for single return at end", 
                           Token.LABEL, firstChild.getType());
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testMutateWithNullFnNode() {
        FunctionToBlockMutator mutator = createMutator();
        try {
            mutator.mutate("f", null, createCallNode(), "result", false, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testMutateWithNullCallNode() {
        FunctionToBlockMutator mutator = createMutator();
        try {
            mutator.mutate("f", createSimpleFunction(), null, "result", false, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMutatorCreation() {
        FunctionToBlockMutator mutator = createMutator();
        assertNotNull("Mutator should be created", mutator);
    }

    @Test(timeout = 4000)
    public void testLabelNameSupplier() {
        Supplier<String> idSupplier = new TestSupplier();
        FunctionToBlockMutator.LabelNameSupplier labelSupplier = 
            new FunctionToBlockMutator.LabelNameSupplier(idSupplier);
        
        String label1 = labelSupplier.get();
        String label2 = labelSupplier.get();
        
        assertNotNull("Label should not be null", label1);
        assertNotNull("Label should not be null", label2);
        assertNotEquals("Labels should be unique", label1, label2);
        assertTrue("Label should contain JSCompiler_inline_label", 
                   label1.contains("JSCompiler_inline_label"));
    }

    @Test(timeout = 4000)
    public void testMutateWithVarDeclarationsInLoop() {
        FunctionToBlockMutator mutator = createMutator();
        
        // Create function with uninitialized var: function f() { var x; return x; }
        Node script = new Node(Token.SCRIPT);
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        
        Node varNode = new Node(Token.VAR);
        Node varName = Node.newString(Token.NAME, "x");
        varNode.addChildToBack(varName);
        
        Node returnNode = new Node(Token.RETURN, Node.newString(Token.NAME, "x"));
        
        body.addChildToBack(varNode);
        body.addChildToBack(returnNode);
        
        function.addChildToFront(name);
        function.addChildToBack(params);
        function.addChildToBack(body);
        script.addChildToBack(function);
        
        Node callNode = createCallNode();
        
        // In loop context, uninitialized vars should get undefined initialization
        Node result = mutator.mutate("f", function, callNode, "result", false, true);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    @Test(timeout = 4000)
    public void testMutateWithNoArguments() {
        FunctionToBlockMutator mutator = createMutator();
        
        // Create function with no parameters: function f() { return 1; }
        Node fnNode = createSimpleFunction();
        Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
        
        Node result = mutator.mutate("f", fnNode, callNode, "result", false, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
    }

    @Test(timeout = 4000)
    public void testMutateWithNeedsDefaultResultNoReturn() {
        FunctionToBlockMutator mutator = createMutator();
        
        // Create function with no return: function f() { var x = 1; }
        Node script = new Node(Token.SCRIPT);
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        
        Node varNode = new Node(Token.VAR);
        Node varName = Node.newString(Token.NAME, "x");
        varName.addChildToBack(Node.newNumber(1));
        varNode.addChildToBack(varName);
        body.addChildToBack(varNode);
        
        function.addChildToFront(name);
        function.addChildToBack(params);
        function.addChildToBack(body);
        script.addChildToBack(function);
        
        Node callNode = createCallNode();
        
        // needsDefaultResult=true with no return should add dummy assignment
        Node result = mutator.mutate("f", function, callNode, "result", true, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Result should be a BLOCK", Token.BLOCK, result.getType());
        
        // Should have at least 2 children: the var statement and the dummy assignment
        assertTrue("Block should have children", result.hasChildren());
    }
}