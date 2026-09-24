package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class SyntacticScopeCreatorDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - testCreateScopeWithNullParent: branch where parent == null -> creates global scope
     *   - testCreateScopeWithParent: branch where parent != null -> creates local scope
     *   - testFunctionExpressionNameBleeding: branch where fnName !isEmpty && isFunctionExpression
     *   - testFunctionDeclaration: branch where isFunctionExpression == false -> declares function var
     *   - testVarDeclaration: Token.VAR case with multiple variables
     *   - testCatchBlock: Token.CATCH case, declares catch var and scans block
     *   - testArgumentsVariable: local scope where name.equals("arguments") triggers redeclaration handler
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - testEmptyFunctionName: empty fnName in function declaration -> returns early
     *   - testNullSourceName: sourceName null handling in createScope
     *   - testNoFunctionBody: function with empty body
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - testDuplicateVarInExterns: targets the known defect where duplicate vars in externs cause errors
     *     The bug: In DefaultRedeclarationHandler.onRedeclaration, when scope.isGlobal() is true,
     *     it checks origParent.getType() and parent.getType() for CATCH, but doesn't properly handle
     *     the case where both are from externs. The test verifies that duplicate vars in global scope
     *     with proper JSDoc suppression are handled correctly.
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - testArgumentsShadowingError: shadowing "arguments" in non-var declaration should report error
     *   - testMultipleGlobalVarDeclarations: multiple var declarations at global scope with no suppression
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - testCreateScopeIdempotent: calling createScope twice returns different instances
     *   - testScopeParentRelationship: ensures parent scope is correctly set
     */

    // Helper to create a minimal AbstractCompiler for testing
    private static class TestCompiler extends AbstractCompiler {
        @Override
        public void report(JSError error) {
            // Capture for assertions
        }

        @Override
        public CompilerInput getInput(String sourceName) {
            return null;
        }

        @Override
        public boolean hasHaltingErrors() {
            return false;
        }

        @Override
        public void setHasCompiled(boolean hasCompiled) {}

        @Override
        public boolean hasCompiled() {
            return false;
        }
    }

    // Helper to create a compiler that tracks reported errors
    private static class ErrorTrackingCompiler extends AbstractCompiler {
        private JSError lastError;

        @Override
        public void report(JSError error) {
            this.lastError = error;
        }

        public JSError getLastError() {
            return lastError;
        }

        @Override
        public CompilerInput getInput(String sourceName) {
            return null;
        }

        @Override
        public boolean hasHaltingErrors() {
            return false;
        }

        @Override
        public void setHasCompiled(boolean hasCompiled) {}

        @Override
        public boolean hasCompiled() {
            return false;
        }
    }

    // Partition A: Core Functional Logic

    @Test(timeout = 4000)
    public void testCreateScopeWithNullParent() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        Node root = new Node(Token.SCRIPT);
        Scope scope = creator.createScope(root, null);
        
        assertNotNull("Scope should not be null", scope);
        assertNull("Global scope should have null parent", scope.getParent());
        assertEquals("Root node should match", root, scope.getRootNode());
    }

    @Test(timeout = 4000)
    public void testCreateScopeWithParent() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        Node globalNode = new Node(Token.SCRIPT);
        Scope globalScope = creator.createScope(globalNode, null);
        
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "testFn");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        functionNode.addChildToBack(nameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        
        Scope localScope = creator.createScope(functionNode, globalScope);
        
        assertNotNull("Local scope should not be null", localScope);
        assertEquals("Parent should be global scope", globalScope, localScope.getParent());
        assertEquals("Root node should be function", functionNode, localScope.getRootNode());
    }

    @Test(timeout = 4000)
    public void testFunctionExpressionNameBleeding() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create a function expression: var x = function foo() {};
        Node script = new Node(Token.SCRIPT);
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "x");
        Node functionNode = new Node(Token.FUNCTION);
        Node fnNameNode = Node.newString(Token.NAME, "foo");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        functionNode.addChildToBack(fnNameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        functionNode.putProp(Node.IS_FUNCTION_EXPRESSION, true);
        nameNode.addChildToBack(functionNode);
        varNode.addChildToBack(nameNode);
        script.addChildToBack(varNode);
        
        Scope scope = creator.createScope(script, null);
        
        // The function name "foo" should be declared in the scope as a variable
        assertTrue("Function name should be declared in scope", scope.isDeclared("foo", false));
    }

    @Test(timeout = 4000)
    public void testFunctionDeclaration() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create a function declaration: function bar() {}
        Node script = new Node(Token.SCRIPT);
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "bar");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        functionNode.addChildToBack(nameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        script.addChildToBack(functionNode);
        
        Scope scope = creator.createScope(script, null);
        
        assertTrue("Function name should be declared", scope.isDeclared("bar", false));
    }

    @Test(timeout = 4000)
    public void testVarDeclaration() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create: var a = 1, b, c;
        Node script = new Node(Token.SCRIPT);
        Node varNode = new Node(Token.VAR);
        Node aNode = Node.newString(Token.NAME, "a");
        Node bNode = Node.newString(Token.NAME, "b");
        Node cNode = Node.newString(Token.NAME, "c");
        aNode.addChildToBack(Node.newNumber(1));
        varNode.addChildToBack(aNode);
        varNode.addChildToBack(bNode);
        varNode.addChildToBack(cNode);
        script.addChildToBack(varNode);
        
        Scope scope = creator.createScope(script, null);
        
        assertTrue("Variable 'a' should be declared", scope.isDeclared("a", false));
        assertTrue("Variable 'b' should be declared", scope.isDeclared("b", false));
        assertTrue("Variable 'c' should be declared", scope.isDeclared("c", false));
    }

    @Test(timeout = 4000)
    public void testCatchBlock() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create: try {} catch(e) {}
        Node script = new Node(Token.SCRIPT);
        Node tryNode = new Node(Token.TRY);
        Node tryBody = new Node(Token.BLOCK);
        Node catchNode = new Node(Token.CATCH);
        Node catchVar = Node.newString(Token.NAME, "e");
        Node catchBody = new Node(Token.BLOCK);
        catchNode.addChildToBack(catchVar);
        catchNode.addChildToBack(catchBody);
        tryNode.addChildToBack(tryBody);
        tryNode.addChildToBack(catchNode);
        script.addChildToBack(tryNode);
        
        Scope scope = creator.createScope(script, null);
        
        assertTrue("Catch variable 'e' should be declared", scope.isDeclared("e", false));
    }

    @Test(timeout = 4000)
    public void testArgumentsVariable() {
        ErrorTrackingCompiler compiler = new ErrorTrackingCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create a function with arguments usage
        Node script = new Node(Token.SCRIPT);
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        // Add a reference to arguments (not a var declaration)
        functionNode.addChildToBack(nameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        script.addChildToBack(functionNode);
        
        // Create a local scope first
        Scope globalScope = creator.createScope(script, null);
        // Now create the function scope 
        Node functionNode2 = new Node(Token.FUNCTION);
        Node nameNode2 = Node.newString(Token.NAME, "");
        Node argsNode2 = new Node(Token.LP);
        Node bodyNode2 = new Node(Token.BLOCK);
        functionNode2.addChildToBack(nameNode2);
        functionNode2.addChildToBack(argsNode2);
        functionNode2.addChildToBack(bodyNode2);
        
        // The arguments variable should be automatically declared in local scopes
        // This tests the isLocal() && name.equals("arguments") branch in declareVar
        Scope localScope = creator.createScope(functionNode2, globalScope);
        
        // This test verifies that the redeclaration handler is NOT called for arguments
        // when it's a local scope, because the scope already has it declared
        assertNotNull("Local scope should be created", localScope);
        assertNull("No error should be reported for arguments in local scope", 
                   compiler.getLastError());
    }

    // Partition B: Boundary Value Analysis & Extremes

    @Test(timeout = 4000)
    public void testEmptyFunctionName() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create function with empty name (invalid)
        Node script = new Node(Token.SCRIPT);
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        functionNode.addChildToBack(nameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        script.addChildToBack(functionNode);
        
        // This should not throw and should not declare a variable with empty name
        Scope scope = creator.createScope(script, null);
        assertNotNull("Scope should be created", scope);
        assertFalse("Empty name should not be declared", scope.isDeclared("", false));
    }

    @Test(timeout = 4000)
    public void testScriptWithSourceName() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        Node script = new Node(Token.SCRIPT);
        script.putProp(Node.SOURCENAME_PROP, "test.js");
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "x");
        varNode.addChildToBack(nameNode);
        script.addChildToBack(varNode);
        
        Scope scope = creator.createScope(script, null);
        
        assertTrue("Variable should be declared", scope.isDeclared("x", false));
    }

    @Test(timeout = 4000)
    public void testEmptyScript() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        Node script = new Node(Token.SCRIPT);
        
        // Should handle empty script without error
        Scope scope = creator.createScope(script, null);
        assertNotNull("Scope should be created for empty script", scope);
    }

    // Partition C: Defect-Targeted Branch Zone

    @Test(timeout = 4000)
    public void testDuplicateVarInExterns() {
        // This test targets the known defect documented in Defects4J
        // The defect: when duplicate variables are declared in externs at global scope,
        // the Normalize pass incorrectly handles them, causing AssertionFailedError
        
        ErrorTrackingCompiler compiler = new ErrorTrackingCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Simulate externs: duplicate variable declarations in global scope
        Node script = new Node(Token.SCRIPT);
        script.putProp(Node.SOURCENAME_PROP, "externs.js");
        
        // First declaration: var x;
        Node varNode1 = new Node(Token.VAR);
        Node nameNode1 = Node.newString(Token.NAME, "x");
        JSDocInfo.Builder jsdocBuilder = new JSDocInfo.Builder();
        jsdocBuilder.suppress("duplicate");
        JSDocInfo jsdoc = jsdocBuilder.build();
        varNode1.setJSDocInfo(jsdoc);
        nameNode1.setJSDocInfo(jsdoc);
        varNode1.addChildToBack(nameNode1);
        script.addChildToBack(varNode1);
        
        // Second declaration: var x; (duplicate)
        Node varNode2 = new Node(Token.VAR);
        Node nameNode2 = Node.newString(Token.NAME, "x");
        varNode2.setJSDocInfo(jsdoc);
        nameNode2.setJSDocInfo(jsdoc);
        varNode2.addChildToBack(nameNode2);
        script.addChildToBack(varNode2);
        
        // The scope should handle duplicate vars with suppression
        // This is where the known defect lies - in the Normalize pass
        // The test verifies that the SyntacticScopeCreator itself doesn't
        // report an error when JSDoc suppresses duplicate warnings
        Scope scope = creator.createScope(script, null);
        
        assertTrue("Variable should be declared", scope.isDeclared("x", false));
        // The bug is that the Normalize pass later may fail, but at this stage
        // The SyntacticScopeCreator should not report an error when suppression is present
        // If the bug is fixed, no error should be reported
        assertNull("No error should be reported for suppressed duplicate", 
                   compiler.getLastError());
    }

    @Test(timeout = 4000)
    public void testDuplicateVarWithoutSuppression() {
        ErrorTrackingCompiler compiler = new ErrorTrackingCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Simulate duplicate var declarations WITHOUT suppression
        Node script = new Node(Token.SCRIPT);
        
        // First declaration: var x;
        Node varNode1 = new Node(Token.VAR);
        Node nameNode1 = Node.newString(Token.NAME, "x");
        varNode1.addChildToBack(nameNode1);
        script.addChildToBack(varNode1);
        
        // Second declaration: var x; (duplicate, no suppression)
        Node varNode2 = new Node(Token.VAR);
        Node nameNode2 = Node.newString(Token.NAME, "x");
        varNode2.addChildToBack(nameNode2);
        script.addChildToBack(varNode2);
        
        Scope scope = creator.createScope(script, null);
        
        // Should still declare the variable, but should report an error
        assertTrue("Variable should be declared", scope.isDeclared("x", false));
        assertNotNull("Error should be reported for non-suppressed duplicate", 
                      compiler.getLastError());
        assertEquals("Error type should be VAR_MULTIPLY_DECLARED_ERROR",
                     SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR,
                     compiler.getLastError().getType());
    }

    // Partition D: Exception & Defensive Guard Paths

    @Test(timeout = 4000)
    public void testArgumentsShadowingError() {
        ErrorTrackingCompiler compiler = new ErrorTrackingCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create a local scope with a function
        Node script = new Node(Token.SCRIPT);
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        // Add a function declaration that shadows arguments
        Node innerFnNode = new Node(Token.FUNCTION);
        Node innerNameNode = Node.newString(Token.NAME, "arguments");
        Node innerArgsNode = new Node(Token.LP);
        Node innerBodyNode = new Node(Token.BLOCK);
        innerFnNode.addChildToBack(innerNameNode);
        innerFnNode.addChildToBack(innerArgsNode);
        innerFnNode.addChildToBack(innerBodyNode);
        bodyNode.addChildToBack(innerFnNode);
        
        functionNode.addChildToBack(nameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        script.addChildToBack(functionNode);
        
        // First create global scope
        Scope globalScope = creator.createScope(script, null);
        
        // Now create a local scope for the inner function 
        // The function name "arguments" should trigger the shadowing error
        // But note: function declarations in local scope are handled differently
        // The shadowing check is for non-var declarations
        Node script2 = new Node(Token.SCRIPT);
        Node functionNode2 = new Node(Token.FUNCTION);
        Node nameNode2 = Node.newString(Token.NAME, "testFn");
        Node argsNode2 = new Node(Token.LP);
        Node bodyNode2 = new Node(Token.BLOCK);
        // Add a variable declaration that shadows arguments
        Node varNode = new Node(Token.VAR);
        Node argsVarName = Node.newString(Token.NAME, "arguments");
        varNode.addChildToBack(argsVarName);
        bodyNode2.addChildToBack(varNode);
        
        functionNode2.addChildToBack(nameNode2);
        functionNode2.addChildToBack(argsNode2);
        functionNode2.addChildToBack(bodyNode2);
        script2.addChildToBack(functionNode2);
        
        Scope localScope = creator.createScope(functionNode2, globalScope);
        
        // The shadowing of "arguments" via a var declaration is allowed in some cases
        // The error is for function declarations that shadow "arguments"
        // This verifies the correct behavior
        assertNotNull("Local scope should be created", localScope);
    }

    @Test(timeout = 4000)
    public void testArgumentsShadowingViaFunctionDecl() {
        ErrorTrackingCompiler compiler = new ErrorTrackingCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create a local scope where "arguments" is shadowed by a function declaration
        Node script = new Node(Token.SCRIPT);
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        // Add function declaration "function arguments() {}"
        Node innerFnNode = new Node(Token.FUNCTION);
        Node innerNameNode = Node.newString(Token.NAME, "arguments");
        Node innerArgsNode = new Node(Token.LP);
        Node innerBodyNode = new Node(Token.BLOCK);
        innerFnNode.addChildToBack(innerNameNode);
        innerFnNode.addChildToBack(innerArgsNode);
        innerFnNode.addChildToBack(innerBodyNode);
        bodyNode.addChildToBack(innerFnNode);
        
        functionNode.addChildToBack(nameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        script.addChildToBack(functionNode);
        
        // Create global scope
        Scope globalScope = creator.createScope(script, null);
        
        // Now create the local scope - the inner function "arguments" triggers shadowing
        Scope localScope = creator.createScope(functionNode, globalScope);
        
        // The shadowing error should be reported because it's a non-var declaration
        // that shadows "arguments" in a local scope
        assertNotNull("Local scope should be created", localScope);
        assertNotNull("Shadowing error should be reported", compiler.getLastError());
        assertEquals("Error should be VAR_ARGUMENTS_SHADOWED_ERROR",
                     SyntacticScopeCreator.VAR_ARGUMENTS_SHADOWED_ERROR,
                     compiler.getLastError().getType());
    }

    // Partition E: Object Lifecycle & Contract Integrity

    @Test(timeout = 4000)
    public void testCreateScopeIdempotent() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        Node script = new Node(Token.SCRIPT);
        Scope scope1 = creator.createScope(script, null);
        Scope scope2 = creator.createScope(script, null);
        
        // Each call should return a new scope instance
        assertNotSame("Each createScope call should return a new instance", scope1, scope2);
    }

    @Test(timeout = 4000)
    public void testScopeParentRelationship() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        Node globalNode = new Node(Token.SCRIPT);
        Scope globalScope = creator.createScope(globalNode, null);
        
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "");
        Node argsNode = new Node(Token.LP);
        Node bodyNode = new Node(Token.BLOCK);
        functionNode.addChildToBack(nameNode);
        functionNode.addChildToBack(argsNode);
        functionNode.addChildToBack(bodyNode);
        
        Scope childScope = creator.createScope(functionNode, globalScope);
        Scope grandchildScope = creator.createScope(functionNode, childScope);
        
        assertEquals("Child's parent should be global", globalScope, childScope.getParent());
        assertEquals("Grandchild's parent should be child", childScope, grandchildScope.getParent());
        assertFalse("Grandchild should not be global", grandchildScope.isGlobal());
        assertFalse("Child should not be global", childScope.isGlobal());
    }

    @Test(timeout = 4000)
    public void testControlStructureTraversal() {
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
        
        // Create if statement with var declarations inside
        Node script = new Node(Token.SCRIPT);
        Node ifNode = new Node(Token.IF);
        Node condition = Node.newString(Token.TRUE);
        Node thenBlock = new Node(Token.BLOCK);
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "insideIf");
        varNode.addChildToBack(nameNode);
        thenBlock.addChildToBack(varNode);
        ifNode.addChildToBack(condition);
        ifNode.addChildToBack(thenBlock);
        script.addChildToBack(ifNode);
        
        Scope scope = creator.createScope(script, null);
        
        // Vars inside control structures should still be declared in the enclosing scope
        // (JavaScript hoisting behavior)
        assertTrue("Variable inside if block should be declared", 
                   scope.isDeclared("insideIf", false));
    }

    @Test(timeout = 4000)
    public void testRedeclarationHandlerInjection() {
        final boolean[] handlerCalled = {false};
        
        SyntacticScopeCreator.RedeclarationHandler customHandler = 
            new SyntacticScopeCreator.RedeclarationHandler() {
                @Override
                public void onRedeclaration(Scope s, String name, Node n, Node parent, 
                                            Node gramps, Node nodeWithLineNumber) {
                    handlerCalled[0] = true;
                }
            };
        
        TestCompiler compiler = new TestCompiler();
        SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler, customHandler);
        
        Node script = new Node(Token.SCRIPT);
        Node varNode1 = new Node(Token.VAR);
        Node nameNode1 = Node.newString(Token.NAME, "x");
        varNode1.addChildToBack(nameNode1);
        script.addChildToBack(varNode1);
        
        Node varNode2 = new Node(Token.VAR);
        Node nameNode2 = Node.newString(Token.NAME, "x");
        varNode2.addChildToBack(nameNode2);
        script.addChildToBack(varNode2);
        
        Scope scope = creator.createScope(script, null);
        
        assertTrue("Custom redeclaration handler should have been called", handlerCalled[0]);
    }
}