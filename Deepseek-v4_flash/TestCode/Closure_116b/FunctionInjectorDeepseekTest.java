package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.*;

public class FunctionInjectorDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: FunctionInjector.java - Known defect in canInlineReferenceToFunction
     * and related methods causing false YES for NO cases.
     * 
     * Key branches targeted:
     * 1. doesFunctionMeetMinimumRequirements - recursion name check, arguments/eval references
     * 2. isSupportedCallType - .call/.apply handling, assumeStrictThis flag
     * 3. canInlineReferenceDirectly - side effect analysis, parameter reference counts
     * 4. canInlineReferenceAsStatementBlock - call site classification, decomposition
     * 5. callMeetsBlockInliningRequirements - var declarations, forbidTemps logic
     * 6. inliningLowersCost - module graph, cost estimation
     * 7. inlineReturnValue - empty function, return node handling
     * 8. classifyCallSite - all call site types including edge cases
     * 9. isDirectCallNodeReplacementPossible - empty block, single return
     * 
     * Defect-specific: Issue 1101 - functions with inner functions incorrectly
     * allowed for inlining when they should be NO. Also testBug4944818 and
     * double inlining issues.
     */

    // ==================== Test Infrastructure ====================
    
    private static class TestCompiler extends AbstractCompiler {
        private final CodingConvention convention = new DefaultCodingConvention();
        private LifeCycleStage stage = LifeCycleStage.NORMALIZED;
        
        @Override
        public CodingConvention getCodingConvention() {
            return convention;
        }
        
        @Override
        public LifeCycleStage getLifeCycleStage() {
            return stage;
        }
        
        @Override
        public JSModuleGraph getModuleGraph() {
            return null;
        }
    }
    
    private static class TestSupplier implements Supplier<String> {
        private int counter = 0;
        @Override
        public String get() {
            return "tmp" + (counter++);
        }
    }
    
    private FunctionInjector createInjector(boolean allowDecomposition, 
            boolean assumeStrictThis, boolean assumeMinimumCapture) {
        return new FunctionInjector(
            new TestCompiler(),
            new TestSupplier(),
            allowDecomposition,
            assumeStrictThis,
            assumeMinimumCapture
        );
    }
    
    private Node parseFunction(String functionSource) {
        // Simplified: create a FUNCTION node with proper structure
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        return fnNode;
    }
    
    private Node createCallNode(String fnName, Node... args) {
        Node callNode = new Node(Token.CALL);
        callNode.addChildToFront(Node.newString(Token.NAME, fnName));
        for (Node arg : args) {
            callNode.addChildToBack(arg);
        }
        return callNode;
    }

    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testConstructorAndBasicState() {
        FunctionInjector injector = createInjector(true, true, true);
        assertNotNull("Injector should be created", injector);
        
        // Test with different flag combinations
        FunctionInjector injector2 = createInjector(false, false, false);
        assertNotNull(injector2);
    }
    
    @Test(timeout = 4000)
    public void testDoesFunctionMeetMinimumRequirements_SimpleFunction() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Create a simple function: function f() { return 1; }
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        assertTrue("Simple function should meet minimum requirements", 
            injector.doesFunctionMeetMinimumRequirements("f", fnNode));
    }
    
    @Test(timeout = 4000)
    public void testDoesFunctionMeetMinimumRequirements_ReferencesArguments() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Function that references arguments: function f() { return arguments[0]; }
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node getProp = new Node(Token.GETELEM, 
            Node.newString(Token.NAME, "arguments"), 
            Node.newNumber(0));
        returnNode.addChildToBack(getProp);
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        assertFalse("Function referencing arguments should not be inlinable", 
            injector.doesFunctionMeetMinimumRequirements("f", fnNode));
    }
    
    @Test(timeout = 4000)
    public void testDoesFunctionMeetMinimumRequirements_ReferencesEval() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Function that references eval
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node exprResult = new Node(Token.EXPR_RESULT);
        Node call = new Node(Token.CALL, Node.newString(Token.NAME, "eval"));
        exprResult.addChildToBack(call);
        body.addChildToBack(exprResult);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        assertFalse("Function referencing eval should not be inlinable", 
            injector.doesFunctionMeetMinimumRequirements("f", fnNode));
    }
    
    @Test(timeout = 4000)
    public void testDoesFunctionMeetMinimumRequirements_ReferencesSelf() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Recursive function: function f() { return f(); }
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node call = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
        returnNode.addChildToBack(call);
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        assertFalse("Recursive function should not be inlinable", 
            injector.doesFunctionMeetMinimumRequirements("f", fnNode));
    }

    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testIsDirectCallNodeReplacementPossible_EmptyFunction() {
        FunctionInjector injector = createInjector(true, true, true);
        
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        assertTrue("Empty function should be direct replacement possible", 
            injector.isDirectCallNodeReplacementPossible(fnNode));
    }
    
    @Test(timeout = 4000)
    public void testIsDirectCallNodeReplacementPossible_SingleReturn() {
        FunctionInjector injector = createInjector(true, true, true);
        
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(42));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        assertTrue("Function with single return should be direct replacement possible", 
            injector.isDirectCallNodeReplacementPossible(fnNode));
    }
    
    @Test(timeout = 4000)
    public void testIsDirectCallNodeReplacementPossible_MultipleStatements() {
        FunctionInjector injector = createInjector(true, true, true);
        
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node varDecl = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(varDecl);
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        assertFalse("Function with multiple statements should not be direct replacement", 
            injector.isDirectCallNodeReplacementPossible(fnNode));
    }
    
    @Test(timeout = 4000)
    public void testSetKnownConstants_EmptySet() {
        FunctionInjector injector = createInjector(true, true, true);
        Set<String> constants = new HashSet<>();
        injector.setKnownConstants(constants);
        // Should not throw
    }
    
    @Test(timeout = 4000)
    public void testSetKnownConstants_NonEmptySet() {
        FunctionInjector injector = createInjector(true, true, true);
        Set<String> constants = new HashSet<>(Arrays.asList("CONST1", "CONST2"));
        injector.setKnownConstants(constants);
        // Should not throw
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testIssue1101_InnerFunctionInNonGlobalScope() {
        // This test targets the known defect where functions with inner functions
        // are incorrectly allowed for inlining when assumeMinimumCapture is false
        // and not in global scope.
        
        FunctionInjector injector = createInjector(true, true, false); // assumeMinimumCapture = false
        
        // Create a function with an inner function
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "outer");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        
        // Inner function
        Node innerFn = new Node(Token.FUNCTION);
        Node innerName = Node.newString(Token.NAME, "inner");
        Node innerParams = new Node(Token.PARAM_LIST);
        Node innerBody = new Node(Token.BLOCK);
        innerFn.addChildToBack(innerName);
        innerFn.addChildToBack(innerParams);
        innerFn.addChildToBack(innerBody);
        
        Node varDecl = new Node(Token.VAR, Node.newString(Token.NAME, "inner"));
        varDecl.getFirstChild().addChildToBack(innerFn);
        body.addChildToBack(varDecl);
        
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        // Create a call node
        Node callNode = createCallNode("outer");
        
        // Create a mock traversal that is NOT in global scope
        NodeTraversal t = new NodeTraversal(new TestCompiler(), null) {
            @Override
            public boolean inGlobalScope() {
                return false;
            }
            
            @Override
            public Node getScopeRoot() {
                Node fn = new Node(Token.FUNCTION);
                Node fnName = Node.newString(Token.NAME, "caller");
                Node fnParams = new Node(Token.PARAM_LIST);
                Node fnBody = new Node(Token.BLOCK);
                fn.addChildToBack(fnName);
                fn.addChildToBack(fnParams);
                fn.addChildToBack(fnBody);
                return fn;
            }
        };
        
        Set<String> needAliases = new HashSet<>();
        
        // This should return NO because containsFunctions is true and not in global scope
        FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
            t, callNode, fnNode, needAliases, 
            FunctionInjector.InliningMode.DIRECT, false, true);
        
        assertEquals("Function with inner function in non-global scope should not be inlinable", 
            FunctionInjector.CanInlineResult.NO, result);
    }
    
    @Test(timeout = 4000)
    public void testIssue1101_InnerFunctionInLoop() {
        // Test that functions with inner functions in a loop are not inlined
        FunctionInjector injector = createInjector(true, true, true); // assumeMinimumCapture = true
        
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        
        Node innerFn = new Node(Token.FUNCTION);
        Node innerName = Node.newString(Token.NAME, "inner");
        Node innerParams = new Node(Token.PARAM_LIST);
        Node innerBody = new Node(Token.BLOCK);
        innerFn.addChildToBack(innerName);
        innerFn.addChildToBack(innerParams);
        innerFn.addChildToBack(innerBody);
        
        Node varDecl = new Node(Token.VAR, Node.newString(Token.NAME, "inner"));
        varDecl.getFirstChild().addChildToBack(innerFn);
        body.addChildToBack(varDecl);
        
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        Node callNode = createCallNode("f");
        
        // Place call node inside a loop structure
        Node forNode = new Node(Token.FOR);
        forNode.addChildToBack(callNode);
        
        NodeTraversal t = new NodeTraversal(new TestCompiler(), null) {
            @Override
            public boolean inGlobalScope() {
                return true;
            }
        };
        
        Set<String> needAliases = new HashSet<>();
        
        // With assumeMinimumCapture=true and in global scope, but call is in loop
        // This should still return NO because containsFunctions and isWithinLoop
        FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
            t, callNode, fnNode, needAliases, 
            FunctionInjector.InliningMode.DIRECT, false, true);
        
        assertEquals("Function with inner function in loop should not be inlinable", 
            FunctionInjector.CanInlineResult.NO, result);
    }
    
    @Test(timeout = 4000)
    public void testBug4944818_ThisReferenceInNonCallCall() {
        // Test that functions referencing 'this' without .call() are not inlined
        FunctionInjector injector = createInjector(true, false, true); // assumeStrictThis = false
        
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, new Node(Token.THIS));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        // Direct call (not .call())
        Node callNode = createCallNode("f");
        
        NodeTraversal t = new NodeTraversal(new TestCompiler(), null) {
            @Override
            public boolean inGlobalScope() {
                return true;
            }
        };
        
        Set<String> needAliases = new HashSet<>();
        
        FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
            t, callNode, fnNode, needAliases, 
            FunctionInjector.InliningMode.DIRECT, true, false);
        
        assertEquals("Function with this reference in non-.call() should not be inlinable", 
            FunctionInjector.CanInlineResult.NO, result);
    }
    
    @Test(timeout = 4000)
    public void testDoubleInlining2_SideEffectParameter() {
        // Test that functions with side-effect parameters used multiple times are not inlined
        FunctionInjector injector = createInjector(true, true, true);
        
        // function f(a) { return a + a; }
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node paramA = Node.newString(Token.NAME, "a");
        params.addChildToBack(paramA);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node add = new Node(Token.ADD, 
            Node.newString(Token.NAME, "a"), 
            Node.newString(Token.NAME, "a"));
        returnNode.addChildToBack(add);
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        // Call with side-effect argument: f(i++)
        Node incNode = new Node(Token.INC, Node.newString(Token.NAME, "i"));
        Node callNode = createCallNode("f", incNode);
        
        FunctionInjector.CanInlineResult result = injector.canInlineReferenceDirectly(
            callNode, fnNode);
        
        assertEquals("Function with side-effect parameter used twice should not be directly inlinable", 
            FunctionInjector.CanInlineResult.NO, result);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullCompiler() {
        new FunctionInjector(null, new TestSupplier(), true, true, true);
    }
    
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullSupplier() {
        new FunctionInjector(new TestCompiler(), null, true, true, true);
    }
    
    @Test(timeout = 4000)
    public void testInline_NullCheck() {
        FunctionInjector injector = createInjector(true, true, true);
        
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        Node callNode = createCallNode("f");
        Node exprResult = new Node(Token.EXPR_RESULT);
        exprResult.addChildToBack(callNode);
        
        try {
            injector.inline(callNode, "f", fnNode, FunctionInjector.InliningMode.DIRECT);
            fail("Should throw because life cycle stage is not normalized");
        } catch (IllegalStateException e) {
            // Expected - life cycle stage not normalized
        }
    }
    
    @Test(timeout = 4000)
    public void testClassifyCallSite_Unsupported() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Create a call node that is not in a supported context
        Node callNode = createCallNode("f");
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(callNode);
        
        // This should not throw but may return UNSUPPORTED
        // We just verify it doesn't crash
        injector.maybePrepareCall(callNode);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testReferenceCreation() {
        Node callNode = createCallNode("f");
        FunctionInjector.Reference ref = new FunctionInjector.Reference(
            callNode, null, FunctionInjector.InliningMode.DIRECT);
        
        assertNotNull("Reference should be created", ref);
        assertSame("Call node should match", callNode, ref.callNode);
        assertNull("Module should be null", ref.module);
        assertEquals("Mode should be DIRECT", 
            FunctionInjector.InliningMode.DIRECT, ref.mode);
    }
    
    @Test(timeout = 4000)
    public void testReferenceWithModule() {
        Node callNode = createCallNode("f");
        JSModule module = new JSModule("test");
        FunctionInjector.Reference ref = new FunctionInjector.Reference(
            callNode, module, FunctionInjector.InliningMode.BLOCK);
        
        assertSame("Module should match", module, ref.module);
        assertEquals("Mode should be BLOCK", 
            FunctionInjector.InliningMode.BLOCK, ref.mode);
    }
    
    @Test(timeout = 4000)
    public void testInliningModes() {
        assertEquals("DIRECT ordinal should be 0", 0, FunctionInjector.InliningMode.DIRECT.ordinal());
        assertEquals("BLOCK ordinal should be 1", 1, FunctionInjector.InliningMode.BLOCK.ordinal());
    }
    
    @Test(timeout = 4000)
    public void testCanInlineResults() {
        assertEquals("YES ordinal should be 0", 0, FunctionInjector.CanInlineResult.YES.ordinal());
        assertEquals("AFTER_PREPARATION ordinal should be 1", 1, FunctionInjector.CanInlineResult.AFTER_PREPARATION.ordinal());
        assertEquals("NO ordinal should be 2", 2, FunctionInjector.CanInlineResult.NO.ordinal());
    }
    
    @Test(timeout = 4000)
    public void testInlineFunction_SimpleCall() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Create a simple function: function f() { return 1; }
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        // Create call site: EXPR_RESULT -> CALL -> NAME
        Node callNode = createCallNode("f");
        Node exprResult = new Node(Token.EXPR_RESULT);
        exprResult.addChildToBack(callNode);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprResult);
        
        // This should throw because life cycle stage is not normalized
        try {
            injector.inline(callNode, "f", fnNode, FunctionInjector.InliningMode.DIRECT);
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testInlineReturnValue_EmptyFunction() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Empty function: function f() {}
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        Node callNode = createCallNode("f");
        Node exprResult = new Node(Token.EXPR_RESULT);
        exprResult.addChildToBack(callNode);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprResult);
        
        try {
            injector.inline(callNode, "f", fnNode, FunctionInjector.InliningMode.DIRECT);
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected - life cycle stage not normalized
        }
    }
    
    @Test(timeout = 4000)
    public void testInlineCostDelta_DirectMode() {
        // Test the inlineCostDelta calculation for DIRECT mode
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        Set<String> namesToAlias = new HashSet<>();
        
        // This is a private method, but we can test it indirectly through inliningLowersCost
        // Just verify the method exists and works
        FunctionInjector injector = createInjector(true, true, true);
        
        // Create a reference
        Node callNode = createCallNode("f");
        FunctionInjector.Reference ref = new FunctionInjector.Reference(
            callNode, null, FunctionInjector.InliningMode.DIRECT);
        
        Collection<FunctionInjector.Reference> refs = Collections.singletonList(ref);
        
        // Test inliningLowersCost with single reference
        boolean result = injector.inliningLowersCost(
            null, fnNode, refs, namesToAlias, true, false);
        
        assertTrue("Single reference should lower cost", result);
    }
    
    @Test(timeout = 4000)
    public void testEstimateCallCost_NoArgs() {
        // Test estimateCallCost indirectly through inliningLowersCost
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        FunctionInjector injector = createInjector(true, true, true);
        
        Node callNode = createCallNode("f");
        FunctionInjector.Reference ref = new FunctionInjector.Reference(
            callNode, null, FunctionInjector.InliningMode.DIRECT);
        
        Collection<FunctionInjector.Reference> refs = Collections.singletonList(ref);
        Set<String> namesToAlias = new HashSet<>();
        
        boolean result = injector.inliningLowersCost(
            null, fnNode, refs, namesToAlias, true, false);
        
        assertTrue("Should lower cost for simple function", result);
    }
    
    @Test(timeout = 4000)
    public void testInlineFunction_BlockModeWithVarDecl() {
        // Test block inlining with var declaration call site
        FunctionInjector injector = createInjector(true, true, true);
        
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        // Create var declaration: var x = f();
        Node varNode = new Node(Token.VAR);
        Node varName = Node.newString(Token.NAME, "x");
        Node callNode = createCallNode("f");
        varName.addChildToBack(callNode);
        varNode.addChildToBack(varName);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(varNode);
        
        try {
            injector.inline(callNode, "f", fnNode, FunctionInjector.InliningMode.BLOCK);
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected - life cycle stage not normalized
        }
    }
    
    @Test(timeout = 4000)
    public void testIsSupportedCallType_NameCall() {
        FunctionInjector injector = createInjector(true, true, true);
        
        // Direct name call: f()
        Node callNode = createCallNode("f");
        
        // This is a private method, test indirectly through canInlineReferenceToFunction
        Node fnNode = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        body.addChildToBack(returnNode);
        fnNode.addChildToBack(name);
        fnNode.addChildToBack(params);
        fnNode.addChildToBack(body);
        
        NodeTraversal t = new NodeTraversal(new TestCompiler(), null) {
            @Override
            public boolean inGlobalScope() {
                return true;
            }
        };
        
        Set<String> needAliases = new HashSet<>();
        
        FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
            t, callNode, fnNode, needAliases, 
            FunctionInjector.InliningMode.DIRECT, false, false);
        
        assertNotNull("Result should not be null", result);
    }
}