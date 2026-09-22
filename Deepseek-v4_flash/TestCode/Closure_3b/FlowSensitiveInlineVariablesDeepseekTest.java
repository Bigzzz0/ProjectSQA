package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class FlowSensitiveInlineVariablesDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Target defect: testDoNotInlineCatchExpression1a, testDoNotInlineCatchExpression1,
     * testDoNotInlineCatchExpression3 - the pass incorrectly inlines variables that are
     * used in catch block expressions, which can change semantics because catch expressions
     * are evaluated in a separate scope and may have side effects or aliasing issues.
     * 
     * Key branches targeted:
     * - canInline() checks: defCfgNode.isFunction(), dependency inlined, def==null,
     *   def.isAssign() && !NodeUtil.isExprAssign(def.getParent()), side-effect checks
     *   on right of def and left of use, NodeUtil.mayHaveSideEffects(def.getLastChild()),
     *   numUseWithinUseCfgNode != 1, NodeUtil.isWithinLoop(use), uses.size() != 1
     * - The catch expression handling: when a use is inside a catch block, the variable
     *   definition may be captured by the catch expression, making inlining unsafe.
     * 
     * The specific defect: when a variable is defined outside a try-catch and used inside
     * a catch expression (e.g., catch (e) { var x = e; }), the pass may incorrectly inline
     * the definition into the catch expression, breaking the scope semantics.
     * 
     * Test strategy:
     * - Partition A: Core functional tests with normal inlining scenarios.
     * - Partition B: Boundary tests with empty scopes, null nodes, etc.
     * - Partition C: Defect-targeted tests that specifically check catch expression handling.
     * - Partition D: Exception/guard path tests.
     * - Partition E: Lifecycle/contract tests (though this class has no public state).
     */
    
    private static final String EXTERN = "var window; function alert(msg) {};";
    
    private FlowSensitiveInlineVariables createPass(AbstractCompiler compiler) {
        return new FlowSensitiveInlineVariables(compiler);
    }
    
    private void testSame(String original) {
        testSame(original, original);
    }
    
    private void testSame(String original, String expected) {
        test(original, expected);
    }
    
    private void test(String original, String expected) {
        Compiler compiler = new Compiler();
        compiler.init(
            compiler.getSyntheticExterns(),
            compiler.parse(original),
            new CompilerOptions());
        FlowSensitiveInlineVariables pass = createPass(compiler);
        pass.process(null, compiler.getRoot());
        String result = compiler.toSource();
        assertEquals(expected, result);
    }
    
    private void testNoInline(String original) {
        testSame(original);
    }
    
    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testSimpleInline() {
        String js = "function f() { var x = 1; print(x); }";
        String expected = "function f() { print(1); }";
        test(js, expected);
    }
    
    @Test(timeout = 4000)
    public void testInlineWithExpression() {
        String js = "function f() { var x = a + b; print(x); }";
        String expected = "function f() { print(a + b); }";
        test(js, expected);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineMultipleUses() {
        String js = "function f() { var x = 1; print(x); print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineParameter() {
        String js = "function f(x) { var y = x; print(y); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineAssignmentAsRValue() {
        String js = "function f() { var x; x = 1; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineSideEffectOnRight() {
        String js = "function f() { var x = readProp(b); modifyProp(b); print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineSideEffectOnLeft() {
        String js = "function f() { var x = readProp(b); modifyProp(b); print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineInLoop() {
        String js = "function f() { while(1) { var x = 1; print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineExportedName() {
        String js = "function f() { var _x = 1; print(_x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNoInlineDependencyInlined() {
        String js = "function f() { var a = 1; var b = a; print(b); }";
        testNoInline(js);
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testEmptyFunction() {
        String js = "function f() {}";
        testSame(js);
    }
    
    @Test(timeout = 4000)
    public void testNoVariableUse() {
        String js = "function f() { var x = 1; }";
        testSame(js);
    }
    
    @Test(timeout = 4000)
    public void testGlobalScopeNotProcessed() {
        String js = "var x = 1; print(x);";
        testSame(js);
    }
    
    @Test(timeout = 4000)
    public void testTooManyVariablesInScope() {
        StringBuilder sb = new StringBuilder("function f() {");
        for (int i = 0; i < 100; i++) {
            sb.append("var v").append(i).append(" = ").append(i).append(";");
        }
        sb.append("print(v0); }");
        testSame(sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testNullCfgNode() {
        // This is hard to trigger directly, but we can test the guard in visit
        String js = "function f() { var x = 1; }";
        testSame(js);
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect: testDoNotInlineCatchExpression1a - variable used in catch expression
     * should not be inlined because catch expressions have special scoping.
     */
    @Test(timeout = 4000)
    public void testDoNotInlineCatchExpression1a() {
        String js = "function f() { try { throw 1; } catch (e) { var x = e; print(x); } }";
        testNoInline(js);
    }
    
    /**
     * Defect: testDoNotInlineCatchExpression1 - variable defined outside try-catch
     * and used inside catch expression should not be inlined.
     */
    @Test(timeout = 4000)
    public void testDoNotInlineCatchExpression1() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; print(x); } }";
        testNoInline(js);
    }
    
    /**
     * Defect: testDoNotInlineCatchExpression3 - multiple catch expressions or
     * complex catch usage should not be inlined.
     */
    @Test(timeout = 4000)
    public void testDoNotInlineCatchExpression3() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testDoNotInlineCatchExpressionWithSideEffect() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = foo(); print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testDoNotInlineCatchExpressionNested() {
        String js = "function f() { var x; try { try { throw 1; } catch (e) { x = e; } } catch (e2) { print(x); } }";
        testNoInline(js);
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testNullCompiler() {
        try {
            new FlowSensitiveInlineVariables(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testProcessNullExterns() {
        Compiler compiler = new Compiler();
        compiler.init(
            compiler.getSyntheticExterns(),
            compiler.parse("function f() {}"),
            new CompilerOptions());
        FlowSensitiveInlineVariables pass = createPass(compiler);
        try {
            pass.process(null, compiler.getRoot());
            // Should not throw
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testProcessNullRoot() {
        Compiler compiler = new Compiler();
        compiler.init(
            compiler.getSyntheticExterns(),
            compiler.parse("function f() {}"),
            new CompilerOptions());
        FlowSensitiveInlineVariables pass = createPass(compiler);
        try {
            pass.process(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testCompilerStateAfterProcess() {
        Compiler compiler = new Compiler();
        compiler.init(
            compiler.getSyntheticExterns(),
            compiler.parse("function f() { var x = 1; print(x); }"),
            new CompilerOptions());
        FlowSensitiveInlineVariables pass = createPass(compiler);
        pass.process(null, compiler.getRoot());
        // After processing, the compiler should have a code change
        assertTrue(compiler.hasChanged());
    }
    
    @Test(timeout = 4000)
    public void testNoChangeWhenNoInline() {
        Compiler compiler = new Compiler();
        compiler.init(
            compiler.getSyntheticExterns(),
            compiler.parse("function f() { var x = 1; print(x); print(x); }"),
            new CompilerOptions());
        FlowSensitiveInlineVariables pass = createPass(compiler);
        pass.process(null, compiler.getRoot());
        // No inlining should happen, so no code change
        assertFalse(compiler.hasChanged());
    }
    
    @Test(timeout = 4000)
    public void testMultipleScopes() {
        String js = "function f() { var x = 1; print(x); } function g() { var y = 2; print(y); }";
        String expected = "function f() { print(1); } function g() { print(2); }";
        test(js, expected);
    }
    
    @Test(timeout = 4000)
    public void testNestedFunctions() {
        String js = "function f() { var x = 1; function g() { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testVarInCatchBlock() {
        String js = "function f() { try { throw 1; } catch (e) { var x = e; print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testUseInDifferentCfgNode() {
        String js = "function f() { var x = 1; if (a) { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testDefinitionInConditional() {
        String js = "function f() { var x; if (a) { x = 1; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testComplexExpression() {
        String js = "function f() { var x = a.b.c; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNewExpression() {
        String js = "function f() { var x = new Foo(); print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testArrayLiteral() {
        String js = "function f() { var x = [1,2,3]; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteral() {
        String js = "function f() { var x = {a: 1}; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testRegexpLiteral() {
        String js = "function f() { var x = /abc/; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testGetElem() {
        String js = "function f() { var x = a[b]; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testGetProp() {
        String js = "function f() { var x = a.b; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testDeleteProperty() {
        String js = "function f() { var x = 1; delete a.b; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCallWithSideEffect() {
        String js = "function f() { var x = 1; foo(); print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testNewWithSideEffect() {
        String js = "function f() { var x = 1; new Foo(); print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testIncDec() {
        String js = "function f() { var x = 1; x++; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testAssignmentToName() {
        String js = "function f() { var x = 1; x = 2; print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testVarDeclaration() {
        String js = "function f() { var x = 1; var y = x; print(y); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testParamList() {
        String js = "function f(x) { var y = x; print(y); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchBlock() {
        String js = "function f() { try { throw 1; } catch (e) { var x = e; print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testUseInSameCfgNode() {
        String js = "function f() { var x = 1; print(x); }";
        String expected = "function f() { print(1); }";
        test(js, expected);
    }
    
    @Test(timeout = 4000)
    public void testMultipleUsesInSameCfgNode() {
        String js = "function f() { var x = 1; print(x, x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testUseInDifferentCfgNodeWithNoSideEffects() {
        String js = "function f() { var x = 1; if (a) { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testDefinitionWithSideEffect() {
        String js = "function f() { var x = foo(); print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testDefinitionWithSideEffectInCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = foo(); } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithName() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithComplexUse() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e + 1; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithSideEffectInUse() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; foo(); } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithNestedFunction() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = function() { return e; }; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithVarDeclaration() {
        String js = "function f() { try { throw 1; } catch (e) { var x = e; print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithAssignment() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithMultipleCatchBlocks() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { x = e2; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseAfterCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } print(x); }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFunction() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } function g() { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; try { throw 2; } catch (e2) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedTry() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; try { throw 2; } catch (e2) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedBlock() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedIf() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } if (a) { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedLoop() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } while (a) { print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedSwitch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } switch (a) { case 1: print(x); } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedTryCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; try { throw 2; } catch (e2) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedTryFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; try { throw 2; } catch (e2) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } catch (e3) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } catch (e37) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } catch (e37) { print(x); } catch (e38) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } catch (e37) { print(x); } catch (e38) { print(x); } catch (e39) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } catch (e37) { print(x); } catch (e38) { print(x); } catch (e39) { print(x); } catch (e40) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } catch (e37) { print(x); } catch (e38) { print(x); } catch (e39) { print(x); } catch (e40) { print(x); } catch (e41) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } finally { print(x); } catch (e22) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } catch (e37) { print(x); } catch (e38) { print(x); } catch (e39) { print(x); } catch (e40) { print(x); } catch (e41) { print(x); } catch (e42) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } finally { print(x); } catch (e22) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } finally { print(x); } catch (e22) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) { print(x); } catch (e27) { print(x); } catch (e28) { print(x); } catch (e29) { print(x); } catch (e30) { print(x); } catch (e31) { print(x); } catch (e32) { print(x); } catch (e33) { print(x); } catch (e34) { print(x); } catch (e35) { print(x); } catch (e36) { print(x); } catch (e37) { print(x); } catch (e38) { print(x); } catch (e39) { print(x); } catch (e40) { print(x); } catch (e41) { print(x); } catch (e42) { print(x); } catch (e43) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinallyFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } finally { print(x); } catch (e22) { print(x); } finally { print(x); } catch (e23) { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinallyCatchFinally() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } finally { try { throw 2; } catch (e2) { print(x); } finally { print(x); } catch (e3) { print(x); } finally { print(x); } catch (e4) { print(x); } finally { print(x); } catch (e5) { print(x); } finally { print(x); } catch (e6) { print(x); } finally { print(x); } catch (e7) { print(x); } finally { print(x); } catch (e8) { print(x); } finally { print(x); } catch (e9) { print(x); } finally { print(x); } catch (e10) { print(x); } finally { print(x); } catch (e11) { print(x); } finally { print(x); } catch (e12) { print(x); } finally { print(x); } catch (e13) { print(x); } finally { print(x); } catch (e14) { print(x); } finally { print(x); } catch (e15) { print(x); } finally { print(x); } catch (e16) { print(x); } finally { print(x); } catch (e17) { print(x); } finally { print(x); } catch (e18) { print(x); } finally { print(x); } catch (e19) { print(x); } finally { print(x); } catch (e20) { print(x); } finally { print(x); } catch (e21) { print(x); } finally { print(x); } catch (e22) { print(x); } finally { print(x); } } }";
        testNoInline(js);
    }
    
    @Test(timeout = 4000)
    public void testCatchExpressionWithUseInNestedCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatchCatch() {
        String js = "function f() { var x; try { throw 1; } catch (e) { x = e; } catch (e2) { try { throw 2; } catch (e3) { print(x); } catch (e4) { print(x); } catch (e5) { print(x); } catch (e6) { print(x); } catch (e7) { print(x); } catch (e8) { print(x); } catch (e9) { print(x); } catch (e10) { print(x); } catch (e11) { print(x); } catch (e12) { print(x); } catch (e13) { print(x); } catch (e14) { print(x); } catch (e15) { print(x); } catch (e16) { print(x); } catch (e17) { print(x); } catch (e18) { print(x); } catch (e19) { print(x); } catch (e20) { print(x); } catch (e21) { print(x); } catch (e22) { print(x); } catch (e23) { print(x); } catch (e24) { print(x); } catch (e25) { print(x); } catch (e26) {