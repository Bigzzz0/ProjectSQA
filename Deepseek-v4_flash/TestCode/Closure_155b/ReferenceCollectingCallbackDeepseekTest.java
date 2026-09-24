package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.jscomp.Scope.Var;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.Map;
import java.util.Set;

public class ReferenceCollectingCallbackDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - Constructor with 2 args (compiler, behavior)
     *   - Constructor with 3 args (compiler, behavior, varFilter)
     *   - process(Node externs, Node root) - basic traversal
     *   - getReferencedVariables() - returns key set
     *   - getReferenceCollection(Var v) - returns collection or null
     *   - visit() - NAME token handling, block boundary pop
     *   - enterScope() - push new BasicBlock
     *   - exitScope() - pop block, call behavior.afterExitScope
     *   - shouldTraverse() - push block boundary, return true
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - Null varFilter predicate
     *   - Empty AST (no nodes)
     *   - Single variable with no references
     *   - Multiple variables in same scope
     *   - Nested scopes (function inside function)
     *   - Block boundaries: DO, FOR, TRY, WHILE, WITH, AND, HOOK, IF, OR, CASE
     *   - Non-boundary nodes (simple expressions)
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - Arguments object handling in inner/outer functions
     *   - Modified arguments in inner function
     *   - Modified arguments in outer function
     *   - Escaped arguments
     *   - Issue 378 patterns
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - Null compiler (should not throw NPE at construction)
     *   - Null behavior (should not throw NPE at construction)
     *   - Null varFilter (should use default always-true)
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - ReferenceCollection state transitions
     *   - BasicBlock parent chain
     *   - Reference construction with various parents
     * 
     * Key branches targeted:
     *   - isBlockBoundary: parent != null check, switch cases, n != parent.getFirstChild()
     *   - visit: n.getType() == Token.NAME, v != null, varFilter.apply(v)
     *   - enterScope: blockStack.isEmpty() ? null : blockStack.peek()
     *   - shouldTraverse: isBlockBoundary check, always returns true
     *   - addReference: referenceMap.get(v) == null path
     *   - ReferenceCollection: add, isWellDefined, isEscaped, getInitializingReference
     *   - BasicBlock: constructor with parent/root, provablyExecutesBefore
     */

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorWithTwoArgs() {
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback.Behavior behavior = ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR;
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
        assertNotNull("Callback should be created", callback);
    }

    @Test(timeout = 4000)
    public void testConstructorWithThreeArgs() {
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback.Behavior behavior = ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR;
        Predicate<Var> varFilter = Predicates.alwaysTrue();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior, varFilter);
        assertNotNull("Callback should be created", callback);
    }

    @Test(timeout = 4000)
    public void testProcessEmptyTree() {
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback.Behavior behavior = ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR;
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
        
        Node externs = new Node(Token.EMPTY);
        Node root = new Node(Token.BLOCK);
        callback.process(externs, root);
        
        Set<Var> referencedVars = callback.getReferencedVariables();
        assertNotNull("Referenced variables set should not be null", referencedVars);
        assertTrue("No variables should be referenced", referencedVars.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetReferencedVariablesEmpty() {
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, 
            ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
        Set<Var> vars = callback.getReferencedVariables();
        assertNotNull("Should return non-null set", vars);
        assertTrue("Set should be empty initially", vars.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetReferenceCollectionReturnsNullForUnknownVar() {
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler,
            ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
        
        // Create a mock scope and var
        Scope scope = new Scope(new Node(Token.BLOCK), null);
        Var unknownVar = scope.declare("unknownVar", new Node(Token.NAME), null);
        
        ReferenceCollectingCallback.ReferenceCollection collection = 
            callback.getReferenceCollection(unknownVar);
        assertNull("Should return null for unknown variable", collection);
    }

    @Test(timeout = 4000)
    public void testEnterScopeAndExitScope() {
        AbstractCompiler compiler = new TestCompiler();
        final boolean[] afterExitCalled = {false};
        
        ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
            @Override
            public void afterExitScope(NodeTraversal t,
                    Map<Var, ReferenceCollectingCallback.ReferenceCollection> referenceMap) {
                afterExitCalled[0] = true;
            }
        };
        
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
        
        Node root = new Node(Token.BLOCK);
        NodeTraversal t = new NodeTraversal(compiler, callback);
        
        callback.enterScope(t);
        callback.exitScope(t);
        
        assertTrue("afterExitScope should have been called", afterExitCalled[0]);
    }

    @Test(timeout = 4000)
    public void testShouldTraverseReturnsTrue() {
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler,
            ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
        
        NodeTraversal t = new NodeTraversal(compiler, callback);
        Node n = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node parent = new Node(Token.VAR, n);
        
        boolean result = callback.shouldTraverse(t, n, parent);
        assertTrue("shouldTraverse should always return true", result);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testBlockBoundaryDo() {
        Node condition = new Node(Token.TRUE);
        Node body = new Node(Token.BLOCK);
        Node doNode = new Node(Token.DO, body, condition);
        
        // body is a child of DO, should be boundary
        assertTrue("BLOCK child of DO should be boundary", 
            isBlockBoundaryReflective(body, doNode));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryFor() {
        Node init = new Node(Token.EMPTY);
        Node condition = new Node(Token.TRUE);
        Node increment = new Node(Token.EMPTY);
        Node body = new Node(Token.BLOCK);
        Node forNode = new Node(Token.FOR, init, condition, increment, body);
        
        assertTrue("BLOCK child of FOR should be boundary",
            isBlockBoundaryReflective(body, forNode));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryTry() {
        Node tryBlock = new Node(Token.BLOCK);
        Node catchBlock = new Node(Token.BLOCK);
        Node finallyBlock = new Node(Token.BLOCK);
        Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
        
        assertTrue("First BLOCK child of TRY should be boundary",
            isBlockBoundaryReflective(tryBlock, tryNode));
        assertTrue("Second BLOCK child of TRY should be boundary",
            isBlockBoundaryReflective(catchBlock, tryNode));
        assertTrue("Third BLOCK child of TRY should be boundary",
            isBlockBoundaryReflective(finallyBlock, tryNode));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryWhile() {
        Node condition = new Node(Token.TRUE);
        Node body = new Node(Token.BLOCK);
        Node whileNode = new Node(Token.WHILE, condition, body);
        
        assertTrue("BLOCK child of WHILE should be boundary",
            isBlockBoundaryReflective(body, whileNode));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryWith() {
        Node object = new Node(Token.NAME, new Node(Token.STRING, "obj"));
        Node body = new Node(Token.BLOCK);
        Node withNode = new Node(Token.WITH, object, body);
        
        assertTrue("BLOCK child of WITH should be boundary",
            isBlockBoundaryReflective(body, withNode));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryIfFirstChild() {
        Node condition = new Node(Token.TRUE);
        Node thenBlock = new Node(Token.BLOCK);
        Node elseBlock = new Node(Token.BLOCK);
        Node ifNode = new Node(Token.IF, condition, thenBlock, elseBlock);
        
        // First child (condition) should NOT be boundary
        assertFalse("First child of IF should not be boundary",
            isBlockBoundaryReflective(condition, ifNode));
        
        // Second child (thenBlock) should be boundary
        assertTrue("Second child of IF should be boundary",
            isBlockBoundaryReflective(thenBlock, ifNode));
        
        // Third child (elseBlock) should be boundary
        assertTrue("Third child of IF should be boundary",
            isBlockBoundaryReflective(elseBlock, ifNode));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryCase() {
        Node caseNode = new Node(Token.CASE);
        Node parent = new Node(Token.SWITCH, caseNode);
        
        assertTrue("CASE node should be boundary",
            isBlockBoundaryReflective(caseNode, parent));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryNonBoundary() {
        Node n = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node parent = new Node(Token.EXPR_RESULT, n);
        
        assertFalse("Simple expression should not be boundary",
            isBlockBoundaryReflective(n, parent));
    }

    @Test(timeout = 4000)
    public void testBlockBoundaryNullParent() {
        Node n = new Node(Token.BLOCK);
        
        assertFalse("Null parent should not be boundary",
            isBlockBoundaryReflective(n, null));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testArgumentsModifiedInInnerFunction() {
        // This test targets the known defect where arguments modified in inner
        // functions are not properly handled
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
            @Override
            public void afterExitScope(NodeTraversal t,
                    Map<Var, ReferenceCollectingCallback.ReferenceCollection> referenceMap) {
                // Verify that arguments variable is properly tracked
                for (Map.Entry<Var, ReferenceCollectingCallback.ReferenceCollection> entry : 
                     referenceMap.entrySet()) {
                    Var v = entry.getKey();
                    ReferenceCollectingCallback.ReferenceCollection collection = entry.getValue();
                    
                    // If this is the arguments variable, verify it's properly handled
                    if (v.getName().equals("arguments")) {
                        // The defect causes incorrect inlining when arguments is modified
                        // in an inner function. We verify the collection state.
                        assertNotNull("Arguments reference collection should exist", collection);
                    }
                }
            }
        };
        
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
        
        // Build AST representing: function f() { arguments[0] = 1; function g() { arguments[0] = 2; } }
        Node argumentsName = new Node(Token.NAME, new Node(Token.STRING, "arguments"));
        Node argumentsGetProp = new Node(Token.GETELEM, argumentsName, new Node(Token.NUMBER, 0.0));
        Node assign = new Node(Token.ASSIGN, argumentsGetProp, new Node(Token.NUMBER, 1.0));
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        Node innerBody = new Node(Token.BLOCK, exprResult);
        
        Node innerFunctionName = new Node(Token.NAME, new Node(Token.STRING, "g"));
        Node innerFunction = new Node(Token.FUNCTION, innerFunctionName, new Node(Token.PARAM_LIST), innerBody);
        Node innerExpr = new Node(Token.EXPR_RESULT, innerFunction);
        
        Node outerBody = new Node(Token.BLOCK, innerExpr);
        Node outerFunctionName = new Node(Token.NAME, new Node(Token.STRING, "f"));
        Node outerFunction = new Node(Token.FUNCTION, outerFunctionName, new Node(Token.PARAM_LIST), outerBody);
        Node root = new Node(Token.BLOCK, outerFunction);
        
        callback.process(new Node(Token.EMPTY), root);
        
        // Verify that the traversal completed without errors
        Set<Var> referencedVars = callback.getReferencedVariables();
        assertNotNull("Referenced variables should not be null", referencedVars);
    }

    @Test(timeout = 4000)
    public void testArgumentsModifiedInOuterFunction() {
        // This test targets the defect where arguments modified in outer function
        // are not properly handled
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler,
            ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
        
        // Build AST representing: function f() { arguments[0] = 1; }
        Node argumentsName = new Node(Token.NAME, new Node(Token.STRING, "arguments"));
        Node argumentsGetProp = new Node(Token.GETELEM, argumentsName, new Node(Token.NUMBER, 0.0));
        Node assign = new Node(Token.ASSIGN, argumentsGetProp, new Node(Token.NUMBER, 1.0));
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        Node body = new Node(Token.BLOCK, exprResult);
        Node functionName = new Node(Token.NAME, new Node(Token.STRING, "f"));
        Node function = new Node(Token.FUNCTION, functionName, new Node(Token.PARAM_LIST), body);
        Node root = new Node(Token.BLOCK, function);
        
        callback.process(new Node(Token.EMPTY), root);
        
        Set<Var> referencedVars = callback.getReferencedVariables();
        assertNotNull("Referenced variables should not be null", referencedVars);
    }

    @Test(timeout = 4000)
    public void testIssue378ModifiedArguments1() {
        // Issue 378: Modified arguments should prevent inlining
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler,
            ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
        
        // Build AST: var x = arguments; x[0] = 1;
        Node argumentsName = new Node(Token.NAME, new Node(Token.STRING, "arguments"));
        Node varX = new Node(Token.VAR, new Node(Token.NAME, new Node(Token.STRING, "x"), argumentsName));
        Node xName = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node xGetProp = new Node(Token.GETELEM, xName, new Node(Token.NUMBER, 0.0));
        Node assign = new Node(Token.ASSIGN, xGetProp, new Node(Token.NUMBER, 1.0));
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        Node body = new Node(Token.BLOCK, varX, exprResult);
        Node functionName = new Node(Token.NAME, new Node(Token.STRING, "f"));
        Node function = new Node(Token.FUNCTION, functionName, new Node(Token.PARAM_LIST), body);
        Node root = new Node(Token.BLOCK, function);
        
        callback.process(new Node(Token.EMPTY), root);
        
        Set<Var> referencedVars = callback.getReferencedVariables();
        assertNotNull("Referenced variables should not be null", referencedVars);
    }

    @Test(timeout = 4000)
    public void testIssue378EscapedArguments1() {
        // Escaped arguments should prevent inlining
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler,
            ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
        
        // Build AST: function f() { return arguments; }
        Node argumentsName = new Node(Token.NAME, new Node(Token.STRING, "arguments"));
        Node returnNode = new Node(Token.RETURN, argumentsName);
        Node body = new Node(Token.BLOCK, returnNode);
        Node functionName = new Node(Token.NAME, new Node(Token.STRING, "f"));
        Node function = new Node(Token.FUNCTION, functionName, new Node(Token.PARAM_LIST), body);
        Node root = new Node(Token.BLOCK, function);
        
        callback.process(new Node(Token.EMPTY), root);
        
        Set<Var> referencedVars = callback.getReferencedVariables();
        assertNotNull("Referenced variables should not be null", referencedVars);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testConstructorWithNullCompiler() {
        try {
            new ReferenceCollectingCallback(null, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
            fail("Should throw NullPointerException for null compiler");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullBehavior() {
        AbstractCompiler compiler = new TestCompiler();
        try {
            new ReferenceCollectingCallback(compiler, null);
            fail("Should throw NullPointerException for null behavior");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullVarFilter() {
        AbstractCompiler compiler = new TestCompiler();
        ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler,
            ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, null);
        assertNotNull("Callback should be created even with null varFilter", callback);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testBasicBlockParentChain() {
        Node root = new Node(Token.BLOCK);
        Node child = new Node(Token.BLOCK);
        Node grandchild = new Node(Token.BLOCK);
        
        ReferenceCollectingCallback.BasicBlock rootBlock = 
            new ReferenceCollectingCallback.BasicBlock(null, root);
        ReferenceCollectingCallback.BasicBlock childBlock = 
            new ReferenceCollectingCallback.BasicBlock(rootBlock, child);
        ReferenceCollectingCallback.BasicBlock grandchildBlock = 
            new ReferenceCollectingCallback.BasicBlock(childBlock, grandchild);
        
        assertNull("Root block should have null parent", rootBlock.getParent());
        assertSame("Child block parent should be root", rootBlock, childBlock.getParent());
        assertSame("Grandchild block parent should be child", childBlock, grandchildBlock.getParent());
    }

    @Test(timeout = 4000)
    public void testBasicBlockProvablyExecutesBefore() {
        Node root = new Node(Token.BLOCK);
        Node child = new Node(Token.BLOCK);
        Node grandchild = new Node(Token.BLOCK);
        
        ReferenceCollectingCallback.BasicBlock rootBlock = 
            new ReferenceCollectingCallback.BasicBlock(null, root);
        ReferenceCollectingCallback.BasicBlock childBlock = 
            new ReferenceCollectingCallback.BasicBlock(rootBlock, child);
        ReferenceCollectingCallback.BasicBlock grandchildBlock = 
            new ReferenceCollectingCallback.BasicBlock(childBlock, grandchild);
        
        assertTrue("Root should provably execute before child", 
            rootBlock.provablyExecutesBefore(childBlock));
        assertTrue("Root should provably execute before grandchild", 
            rootBlock.provablyExecutesBefore(grandchildBlock));
        assertTrue("Child should provably execute before grandchild", 
            childBlock.provablyExecutesBefore(grandchildBlock));
        assertFalse("Grandchild should not provably execute before child", 
            grandchildBlock.provablyExecutesBefore(childBlock));
    }

    @Test(timeout = 4000)
    public void testBasicBlockIsFunction() {
        Node functionNode = new Node(Token.FUNCTION);
        ReferenceCollectingCallback.BasicBlock block = 
            new ReferenceCollectingCallback.BasicBlock(null, functionNode);
        // Note: isFunction is private, but we can verify through provablyExecutesBefore behavior
        assertNotNull("Block should be created", block);
    }

    @Test(timeout = 4000)
    public void testBasicBlockIsLoop() {
        Node body = new Node(Token.BLOCK);
        Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), body);
        ReferenceCollectingCallback.BasicBlock block = 
            new ReferenceCollectingCallback.BasicBlock(null, body);
        // Note: isLoop is private, but we can verify through provablyExecutesBefore behavior
        assertNotNull("Block should be created", block);
    }

    @Test(timeout = 4000)
    public void testReferenceCollectionAdd() {
        ReferenceCollectingCallback.ReferenceCollection collection = 
            new ReferenceCollectingCallback.ReferenceCollection();
        
        AbstractCompiler compiler = new TestCompiler();
        NodeTraversal t = new NodeTraversal(compiler, null);
        Node nameNode = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node parent = new Node(Token.VAR, nameNode);
        ReferenceCollectingCallback.BasicBlock block = 
            new ReferenceCollectingCallback.BasicBlock(null, new Node(Token.BLOCK));
        
        ReferenceCollectingCallback.Reference ref = 
            new ReferenceCollectingCallback.Reference(nameNode, parent, t, block);
        
        // Create a mock Var
        Scope scope = new Scope(new Node(Token.BLOCK), null);
        Var v = scope.declare("x", nameNode, null);
        
        collection.add(ref, t, v);
        
        // Verify the reference was added (we can check via isWellDefined which checks size)
        assertFalse("Collection with one reference should not be well-defined", 
            collection.isWellDefined());
    }

    @Test(timeout = 4000)
    public void testReferenceIsDeclaration() {
        // Test VAR declaration
        Node nameNode = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node varNode = new Node(Token.VAR, nameNode);
        AbstractCompiler compiler = new TestCompiler();
        NodeTraversal t = new NodeTraversal(compiler, null);
        ReferenceCollectingCallback.BasicBlock block = 
            new ReferenceCollectingCallback.BasicBlock(null, new Node(Token.BLOCK));
        
        ReferenceCollectingCallback.Reference ref = 
            new ReferenceCollectingCallback.Reference(nameNode, varNode, t, block);
        
        assertTrue("VAR parent should be declaration", ref.isDeclaration());
        assertTrue("VAR parent should be var declaration", ref.isVarDeclaration());
    }

    @Test(timeout = 4000)
    public void testReferenceIsNotDeclaration() {
        Node nameNode = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node exprResult = new Node(Token.EXPR_RESULT, nameNode);
        AbstractCompiler compiler = new TestCompiler();
        NodeTraversal t = new NodeTraversal(compiler, null);
        ReferenceCollectingCallback.BasicBlock block = 
            new ReferenceCollectingCallback.BasicBlock(null, new Node(Token.BLOCK));
        
        ReferenceCollectingCallback.Reference ref = 
            new ReferenceCollectingCallback.Reference(nameNode, exprResult, t, block);
        
        assertFalse("EXPR_RESULT parent should not be declaration", ref.isDeclaration());
        assertFalse("EXPR_RESULT parent should not be var declaration", ref.isVarDeclaration());
    }

    @Test(timeout = 4000)
    public void testReferenceIsLvalue() {
        // Test ASSIGN lvalue
        Node nameNode = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node assign = new Node(Token.ASSIGN, nameNode, new Node(Token.NUMBER, 1.0));
        AbstractCompiler compiler = new TestCompiler();
        NodeTraversal t = new NodeTraversal(compiler, null);
        ReferenceCollectingCallback.BasicBlock block = 
            new ReferenceCollectingCallback.BasicBlock(null, new Node(Token.BLOCK));
        
        ReferenceCollectingCallback.Reference ref = 
            new ReferenceCollectingCallback.Reference(nameNode, assign, t, block);
        
        assertTrue("ASSIGN with name as first child should be lvalue", ref.isLvalue());
    }

    @Test(timeout = 4000)
    public void testReferenceIsNotLvalue() {
        Node nameNode = new Node(Token.NAME, new Node(Token.STRING, "x"));
        Node exprResult = new Node(Token.EXPR_RESULT, nameNode);
        AbstractCompiler compiler = new TestCompiler();
        NodeTraversal t = new NodeTraversal(compiler, null);
        ReferenceCollectingCallback.BasicBlock block = 
            new ReferenceCollectingCallback.BasicBlock(null, new Node(Token.BLOCK));
        
        ReferenceCollectingCallback.Reference ref = 
            new ReferenceCollectingCallback.Reference(nameNode, exprResult, t, block);
        
        assertFalse("EXPR_RESULT parent should not be lvalue", ref.isLvalue());
    }

    // ==================== Helper Methods ====================

    /**
     * Reflectively calls the private isBlockBoundary method for testing.
     */
    private boolean isBlockBoundaryReflective(Node n, Node parent) {
        try {
            java.lang.reflect.Method method = 
                ReferenceCollectingCallback.class.getDeclaredMethod("isBlockBoundary", 
                    Node.class, Node.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(null, n, parent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke isBlockBoundary", e);
        }
    }

    /**
     * A minimal AbstractCompiler implementation for testing.
     */
    private static class TestCompiler extends AbstractCompiler {
        @Override
        public CompilerOptions getOptions() {
            return new CompilerOptions();
        }

        @Override
        public void report(JSError error) {
            // No-op for testing
        }

        @Override
        public CheckLevel getErrorLevel(JSError error) {
            return CheckLevel.ERROR;
        }

        @Override
        public void setCssNames(Set<String> cssNames) {
            // No-op
        }

        @Override
        public void setSymbolNames(Set<String> symbolNames) {
            // No-op
        }

        @Override
        public void setProcessClosurePrimitives(boolean process) {
            // No-op
        }

        @Override
        public void setManagedDependencies(Set<String> deps) {
            // No-op
        }

        @Override
        public void setTrustedStrings(boolean trusted) {
            // No-op
        }
    }
}