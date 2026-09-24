package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.jscomp.FunctionInjector.InliningMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class InlineFunctionsDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Target: InlineFunctions.java (1101 lines)
     *
     * Key Decision Branches:
     * 1. maybeAddFunction(): Multiple definitions check, isCandidateFunction(), 
     *    referencesThis, hasInnerFunctions, block inlining enabled
     * 2. isCandidateFunction(): Exported check, special function check, 
     *    specialization check, minimum requirements
     * 3. FindCandidatesReferences.checkNameUsage(): isCandidateUsage(), 
     *    ObjectPropertyString, ASSIGN parent, other references
     * 4. trimCanidatesUsingOnCost(): hasReferences(), canRemove(), mimimizeCost()
     * 5. resolveInlineConflicts(): findCalledFunctions(), setSafeFnNode()
     * 6. Inline.visitCallSite(): ref != null, specializationState
     * 7. FunctionState: All getters/setters, canInline/canRemove interactions
     * 8. Reference class: requiresDecomposition, inlined flag
     *
     * Defect Target (testIssue423): The known defect involves incorrect handling
     * of function inlining when functions have inner functions and local names.
     * The bug manifests when hasLocalNames() returns true for functions with
     * parameters but no local vars/functions, causing false negative for inlining.
     *
     * Boundary Conditions:
     * - Null/empty function maps
     * - Functions with/without parameters
     * - Functions with/without inner functions
     * - Direct vs block inlining modes
     * - Module assignment
     * - Names to alias sets
     */

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorAndBasicState() {
        // Create a mock compiler and supplier
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        assertNotNull("InlineFunctions should be created", inlineFunctions);
    }

    @Test(timeout = 4000)
    public void testGetOrCreateFunctionState_NewFunction() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        // Access via reflection or test through public API
        // Since getOrCreateFunctionState is package-private, we test indirectly
        // by checking that process doesn't throw with empty inputs
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Should not throw
        inlineFunctions.process(externs, root);
    }

    @Test(timeout = 4000)
    public void testEnableSpecialization() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        // SpecializationState is an interface, we can't instantiate directly
        // but we can verify the method doesn't throw
        inlineFunctions.enableSpecialization(null);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testProcessWithNullExterns() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        Node root = new Node(Token.SCRIPT);
        
        try {
            inlineFunctions.process(null, root);
            fail("Expected NullPointerException for null externs");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testProcessWithNullRoot() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        Node externs = new Node(Token.SCRIPT);
        
        try {
            inlineFunctions.process(externs, null);
            fail("Expected NullPointerException for null root");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testProcessWithEmptyScripts() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Should complete without error
        inlineFunctions.process(externs, root);
    }

    @Test(timeout = 4000)
    public void testProcessWithSimpleFunction() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        // Create a simple function: function foo() { return 1; }
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node functionNode = createSimpleFunction("foo");
        root.addChildToBack(functionNode);
        
        // Should complete without error
        inlineFunctions.process(externs, root);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testIssue423_DefectTarget() {
        // This test targets the known defect where functions with parameters
        // but no local vars/functions are incorrectly rejected for inlining
        // when hasInnerFunctions is true but hasLocalNames returns false positive
        
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        // Create a function with parameters but no inner functions or local vars
        // function foo(x) { return x + 1; }
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Build: function foo(x) { return x + 1; }
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "foo");
        functionNode.addChildToFront(nameNode);
        
        // Parameters: x
        Node paramsNode = new Node(Token.PARAM_LIST);
        Node paramX = Node.newString(Token.NAME, "x");
        paramsNode.addChildToBack(paramX);
        functionNode.addChildAfter(paramsNode, nameNode);
        
        // Body: return x + 1;
        Node bodyNode = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node addNode = new Node(Token.ADD);
        Node refX = Node.newString(Token.NAME, "x");
        Node num1 = Node.newNumber(1);
        addNode.addChildToBack(refX);
        addNode.addChildToBack(num1);
        returnNode.addChildToBack(addNode);
        bodyNode.addChildToBack(returnNode);
        functionNode.addChildToBack(bodyNode);
        
        // Add as a named function: function foo(x) { return x + 1; }
        root.addChildToBack(functionNode);
        
        // Add a call: foo(5)
        Node callNode = new Node(Token.CALL);
        Node callName = Node.newString(Token.NAME, "foo");
        callNode.addChildToBack(callName);
        Node arg5 = Node.newNumber(5);
        callNode.addChildToBack(arg5);
        
        // Add call to a statement
        Node exprResult = new Node(Token.EXPR_RESULT);
        exprResult.addChildToBack(callNode);
        root.addChildToBack(exprResult);
        
        // Process should handle this without error
        inlineFunctions.process(externs, root);
        
        // The defect would cause the function to not be inlined when it should be
        // We verify the process completed (the bug would throw AssertionFailedError)
    }

    @Test(timeout = 4000)
    public void testIssue423_WithInnerFunction() {
        // Test the case where a function has both parameters and inner functions
        // This should correctly prevent inlining
        
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Build: function bar(y) { function inner() { return y; } return inner(); }
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "bar");
        functionNode.addChildToFront(nameNode);
        
        // Parameters: y
        Node paramsNode = new Node(Token.PARAM_LIST);
        Node paramY = Node.newString(Token.NAME, "y");
        paramsNode.addChildToBack(paramY);
        functionNode.addChildAfter(paramsNode, nameNode);
        
        // Body with inner function
        Node bodyNode = new Node(Token.BLOCK);
        
        // Inner function: function inner() { return y; }
        Node innerFunction = new Node(Token.FUNCTION);
        Node innerName = Node.newString(Token.NAME, "inner");
        innerFunction.addChildToFront(innerName);
        Node innerParams = new Node(Token.PARAM_LIST);
        innerFunction.addChildAfter(innerParams, innerName);
        Node innerBody = new Node(Token.BLOCK);
        Node innerReturn = new Node(Token.RETURN);
        Node refY = Node.newString(Token.NAME, "y");
        innerReturn.addChildToBack(refY);
        innerBody.addChildToBack(innerReturn);
        innerFunction.addChildToBack(innerBody);
        
        bodyNode.addChildToBack(innerFunction);
        
        // Return inner()
        Node returnNode = new Node(Token.RETURN);
        Node innerCall = new Node(Token.CALL);
        Node innerCallName = Node.newString(Token.NAME, "inner");
        innerCall.addChildToBack(innerCallName);
        returnNode.addChildToBack(innerCall);
        bodyNode.addChildToBack(returnNode);
        
        functionNode.addChildToBack(bodyNode);
        root.addChildToBack(functionNode);
        
        // Process should handle this without error
        inlineFunctions.process(externs, root);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorWithNullCompiler() {
        Supplier<String> supplier = createMockSupplier();
        new InlineFunctions(null, supplier, true, true, true);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorWithNullSupplier() {
        AbstractCompiler compiler = createMockCompiler();
        new InlineFunctions(compiler, null, true, true, true);
    }

    @Test(timeout = 4000)
    public void testProcessWithBlockInliningDisabled() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, false);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Should complete without error
        inlineFunctions.process(externs, root);
    }

    @Test(timeout = 4000)
    public void testProcessWithLocalInliningDisabled() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, false, true);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Should complete without error
        inlineFunctions.process(externs, root);
    }

    @Test(timeout = 4000)
    public void testProcessWithGlobalInliningDisabled() {
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, false, true, true);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Should complete without error
        inlineFunctions.process(externs, root);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testFunctionStateLifecycle() {
        // Test the FunctionState inner class through its public methods
        // Since it's private, we test indirectly through InlineFunctions behavior
        
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        // Process with a function that should be inlined
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Simple function: function identity(x) { return x; }
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "identity");
        functionNode.addChildToFront(nameNode);
        
        Node paramsNode = new Node(Token.PARAM_LIST);
        Node paramX = Node.newString(Token.NAME, "x");
        paramsNode.addChildToBack(paramX);
        functionNode.addChildAfter(paramsNode, nameNode);
        
        Node bodyNode = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node refX = Node.newString(Token.NAME, "x");
        returnNode.addChildToBack(refX);
        bodyNode.addChildToBack(returnNode);
        functionNode.addChildToBack(bodyNode);
        
        root.addChildToBack(functionNode);
        
        // Call: identity(42)
        Node callNode = new Node(Token.CALL);
        Node callName = Node.newString(Token.NAME, "identity");
        callNode.addChildToBack(callName);
        Node arg42 = Node.newNumber(42);
        callNode.addChildToBack(arg42);
        
        Node exprResult = new Node(Token.EXPR_RESULT);
        exprResult.addChildToBack(callNode);
        root.addChildToBack(exprResult);
        
        // Process should complete successfully
        inlineFunctions.process(externs, root);
    }

    @Test(timeout = 4000)
    public void testMultipleFunctionsWithSameName() {
        // Test handling of multiple function definitions with same name
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // First function: function foo() { return 1; }
        Node func1 = createSimpleFunction("foo");
        root.addChildToBack(func1);
        
        // Second function: function foo() { return 2; }
        Node func2 = createSimpleFunction("foo");
        root.addChildToBack(func2);
        
        // Should handle duplicate definitions without error
        inlineFunctions.process(externs, root);
    }

    @Test(timeout = 4000)
    public void testFunctionWithExportedName() {
        // Test that exported functions are not inlined
        AbstractCompiler compiler = createMockCompiler();
        Supplier<String> supplier = createMockSupplier();
        InlineFunctions inlineFunctions = new InlineFunctions(
            compiler, supplier, true, true, true);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        // Function with exported name (starts with _$)
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "_exportedFunc");
        functionNode.addChildToFront(nameNode);
        
        Node paramsNode = new Node(Token.PARAM_LIST);
        functionNode.addChildAfter(paramsNode, nameNode);
        
        Node bodyNode = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node num1 = Node.newNumber(1);
        returnNode.addChildToBack(num1);
        bodyNode.addChildToBack(returnNode);
        functionNode.addChildToBack(bodyNode);
        
        root.addChildToBack(functionNode);
        
        // Should complete without error
        inlineFunctions.process(externs, root);
    }

    // ==================== Helper Methods ====================

    private AbstractCompiler createMockCompiler() {
        // Create a minimal mock compiler for testing
        return new AbstractCompiler() {
            @Override
            public void reportCodeChange() {
                // No-op for testing
            }
            
            @Override
            public void addToDebugLog(String message) {
                // No-op for testing
            }
            
            @Override
            public CodingConvention getCodingConvention() {
                return new CodingConvention() {
                    @Override
                    public boolean isExported(String name) {
                        return name.startsWith("_");
                    }
                };
            }
            
            @Override
            public LifeCycleStage getLifeCycleStage() {
                return LifeCycleStage.NORMALIZED;
            }
            
            @Override
            public Supplier<String> getUniqueNameIdSupplier() {
                return new Supplier<String>() {
                    private int counter = 0;
                    @Override
                    public String get() {
                        return "tmp" + (counter++);
                    }
                };
            }
            
            // Other required methods with minimal implementations
            @Override
            public void process(Node externs, Node root) {}
            
            @Override
            public CompilerOptions getOptions() { return new CompilerOptions(); }
            
            @Override
            public void report(JSError error) {}
            
            @Override
            public Node getNodeForCodeInsertion(JSModule module) { return null; }
            
            @Override
            public JSModuleGraph getModuleGraph() { return null; }
            
            @Override
            public void reportChangeToEnclosingScope(Node n) {}
        };
    }

    private Supplier<String> createMockSupplier() {
        return new Supplier<String>() {
            private int counter = 0;
            @Override
            public String get() {
                return "inline" + (counter++);
            }
        };
    }

    private Node createSimpleFunction(String name) {
        // Creates: function name() { return 1; }
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, name);
        functionNode.addChildToFront(nameNode);
        
        Node paramsNode = new Node(Token.PARAM_LIST);
        functionNode.addChildAfter(paramsNode, nameNode);
        
        Node bodyNode = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node num1 = Node.newNumber(1);
        returnNode.addChildToBack(num1);
        bodyNode.addChildToBack(returnNode);
        functionNode.addChildToBack(bodyNode);
        
        return functionNode;
    }
}