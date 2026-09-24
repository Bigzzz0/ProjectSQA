package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: UnreachableCodeElimination.java
 * 
 * Key Decision Branches:
 * 1. process() - traverseChangedFunctions callback, CFG computation, GraphReachability
 * 2. EliminationPass.visit() - parent null check, function/script check, gNode null check
 * 3. EliminationPass.visit() - REACHABLE annotation check, removeNoOpStatements && !mayHaveSideEffects
 * 4. tryRemoveUnconditionalBranching() - n null check, gNode null check
 * 5. tryRemoveUnconditionalBranching() - Token.RETURN with children check
 * 6. tryRemoveUnconditionalBranching() - outEdges.size() == 1 && (n.getNext() == null || n.getNext().isFunction())
 * 7. computeFollowing() - while loop for Block nodes
 * 8. removeDeadExprStatementSafely() - isEmpty/empty Block check
 * 9. removeDeadExprStatementSafely() - NodeUtil.isForIn check
 * 10. removeDeadExprStatementSafely() - Token.DO return
 * 11. removeDeadExprStatementSafely() - Token.BLOCK in try/catch container
 * 12. removeDeadExprStatementSafely() - Token.CATCH handling
 * 13. removeDeadExprStatementSafely() - n.isVar() && !firstChild.hasChildren() check
 * 
 * Defect-Targeted Branches (Defects4J ground truth):
 * - Issue 4177428: Unconditional branching (return/break/continue) removal in try-finally blocks
 * - tryRemoveUnconditionalBranching() incorrectly removes break/continue/return when target is same as follow
 *   but doesn't account for finally blocks that must execute
 * - The defect is in the condition: outEdges.size() == 1 && (n.getNext() == null || n.getNext().isFunction())
 *   This doesn't properly handle cases where the jump target is the same as the follow node
 *   but there's a finally block that needs to execute
 * 
 * Boundary Conditions:
 * - Null parent, function, script nodes
 * - Nodes not in CFG (gNode == null)
 * - Unreachable nodes vs reachable nodes
 * - No-op statements with/without side effects
 * - Empty blocks, empty statements
 * - FOR-IN headers
 * - DO loops
 * - TRY-CATCH-FINALLY blocks
 * - Variable declarations without initializers
 */
public class UnreachableCodeEliminationDeepseekTest {
    
    private static final String EXTERN_URL = "externs.zip";
    
    private Compiler createCompiler() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setCodingConvention(new GoogleCodingConvention());
        compiler.initOptions(options);
        return compiler;
    }
    
    private Node parseAndRun(Compiler compiler, String code) {
        Node externs = compiler.parse(EXTERN_URL, "function alert(x) {}");
        Node root = compiler.parseSyntheticCode("test", code);
        assertEquals("Parsing errors: " + compiler.getErrors(), 0, compiler.getErrors().length);
        
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, true);
        uce.process(externs, root);
        return root;
    }
    
    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testBasicDeadCodeAfterReturn() {
        Compiler compiler = createCompiler();
        String code = "function f() { return 1; var x = 2; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // The var x = 2 should be removed
        assertFalse("Dead code after return should be removed", result.contains("x = 2"));
        assertTrue("Return should remain", result.contains("return 1"));
    }
    
    @Test(timeout = 4000)
    public void testNoOpStatementRemoval() {
        Compiler compiler = createCompiler();
        String code = "function f() { a.b.MyClass.prototype.propertyName; true; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // No-op statements should be removed
        assertFalse("No-op statement should be removed", result.contains("prototype.propertyName"));
        assertFalse("Literal true should be removed", result.contains("true"));
    }
    
    @Test(timeout = 4000)
    public void testSideEffectStatementPreserved() {
        Compiler compiler = createCompiler();
        String code = "function f() { alert('hello'); }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("Side-effect statement should be preserved", result.contains("alert"));
    }
    
    @Test(timeout = 4000)
    public void testDeadCodeAfterReturnInIf() {
        Compiler compiler = createCompiler();
        String code = "function f(x) { if (x) { return; alert('unreachable'); } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertFalse("Dead code after return in if should be removed", result.contains("unreachable"));
    }
    
    // ========== Partition B: Boundary Value Analysis & Extremes ==========
    
    @Test(timeout = 4000)
    public void testEmptyFunction() {
        Compiler compiler = createCompiler();
        String code = "function f() {}";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("Empty function should remain", result.contains("function f()"));
    }
    
    @Test(timeout = 4000)
    public void testFunctionWithOnlyReturn() {
        Compiler compiler = createCompiler();
        String code = "function f() { return; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("Return should remain", result.contains("return"));
    }
    
    @Test(timeout = 4000)
    public void testNullParentNode() {
        Compiler compiler = createCompiler();
        String code = "function f() { }";
        Node root = parseAndRun(compiler, code);
        // Should not crash - parent null check in visit()
        assertNotNull("Root should not be null", root);
    }
    
    @Test(timeout = 4000)
    public void testScriptNode() {
        Compiler compiler = createCompiler();
        String code = "var x = 1;";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("Script-level var should remain", result.contains("x = 1"));
    }
    
    @Test(timeout = 4000)
    public void testEmptyBlock() {
        Compiler compiler = createCompiler();
        String code = "function f() { {} }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // Empty blocks should be handled safely
        assertNotNull("Result should not be null", result);
    }
    
    @Test(timeout = 4000)
    public void testForInHeader() {
        Compiler compiler = createCompiler();
        String code = "function f(obj) { for (var x in obj) { alert(x); } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("FOR-IN should be preserved", result.contains("for"));
    }
    
    @Test(timeout = 4000)
    public void testDoLoop() {
        Compiler compiler = createCompiler();
        String code = "function f() { do { alert(1); } while (false); }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("DO loop should be preserved", result.contains("do"));
    }
    
    @Test(timeout = 4000)
    public void testVarWithoutInitializer() {
        Compiler compiler = createCompiler();
        String code = "function f() { var x; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // Var without initializer should be handled
        assertNotNull("Result should not be null", result);
    }
    
    // ========== Partition C: Defect-Targeted Branch Zone ==========
    // Targeting Defects4J issues: testIssue4177428_return, testIssue4177428_continue,
    // testIssue4177428a, testIssue4177428c, testDontRemoveBreakInTryFinally,
    // testDontRemoveBreakInTryFinallySwitch
    
    @Test(timeout = 4000)
    public void testIssue4177428_return() {
        Compiler compiler = createCompiler();
        // This test targets the defect where return in try-finally is incorrectly removed
        String code = "function f() { try { return 1; } finally { alert('finally'); } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // The return should NOT be removed because finally must execute
        assertTrue("Return in try-finally should be preserved", result.contains("return 1"));
        assertTrue("Finally block should be preserved", result.contains("finally"));
    }
    
    @Test(timeout = 4000)
    public void testIssue4177428_continue() {
        Compiler compiler = createCompiler();
        // This test targets the defect where continue in try-finally is incorrectly removed
        String code = "function f() { for(var i=0; i<10; i++) { try { continue; } finally { alert('finally'); } } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // The continue should NOT be removed because finally must execute
        assertTrue("Continue in try-finally should be preserved", result.contains("continue"));
        assertTrue("Finally block should be preserved", result.contains("finally"));
    }
    
    @Test(timeout = 4000)
    public void testDontRemoveBreakInTryFinally() {
        Compiler compiler = createCompiler();
        // This test targets the defect where break in try-finally is incorrectly removed
        String code = "function f() { for(var i=0; i<10; i++) { try { break; } finally { alert('finally'); } } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // The break should NOT be removed because finally must execute
        assertTrue("Break in try-finally should be preserved", result.contains("break"));
        assertTrue("Finally block should be preserved", result.contains("finally"));
    }
    
    @Test(timeout = 4000)
    public void testDontRemoveBreakInTryFinallySwitch() {
        Compiler compiler = createCompiler();
        // This test targets the defect where break in try-finally inside switch is incorrectly removed
        String code = "function f(x) { switch(x) { case 1: try { break; } finally { alert('finally'); } } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // The break should NOT be removed because finally must execute
        assertTrue("Break in try-finally inside switch should be preserved", result.contains("break"));
        assertTrue("Finally block should be preserved", result.contains("finally"));
    }
    
    @Test(timeout = 4000)
    public void testIssue4177428a() {
        Compiler compiler = createCompiler();
        // Variant of the issue with nested try-finally
        String code = "function f() { try { try { return 1; } finally { alert('inner'); } } finally { alert('outer'); } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // Both returns should be preserved
        assertTrue("Return in nested try-finally should be preserved", result.contains("return 1"));
        assertTrue("Inner finally should be preserved", result.contains("inner"));
        assertTrue("Outer finally should be preserved", result.contains("outer"));
    }
    
    @Test(timeout = 4000)
    public void testIssue4177428c() {
        Compiler compiler = createCompiler();
        // Variant with conditional return in try-finally
        String code = "function f(x) { try { if (x) { return 1; } } finally { alert('finally'); } return 2; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // The conditional return should be preserved
        assertTrue("Conditional return in try should be preserved", result.contains("return 1"));
        assertTrue("Finally block should be preserved", result.contains("finally"));
        assertTrue("Second return should be preserved", result.contains("return 2"));
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(timeout = 4000)
    public void testTryCatchBlock() {
        Compiler compiler = createCompiler();
        String code = "function f() { try { alert('try'); } catch(e) { alert('catch'); } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("Try block should be preserved", result.contains("try"));
        assertTrue("Catch block should be preserved", result.contains("catch"));
    }
    
    @Test(timeout = 4000)
    public void testTryCatchFinallyBlock() {
        Compiler compiler = createCompiler();
        String code = "function f() { try { alert('try'); } catch(e) { alert('catch'); } finally { alert('finally'); } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("Try block should be preserved", result.contains("try"));
        assertTrue("Catch block should be preserved", result.contains("catch"));
        assertTrue("Finally block should be preserved", result.contains("finally"));
    }
    
    @Test(timeout = 4000)
    public void testUnreachableCatchBlock() {
        Compiler compiler = createCompiler();
        // Catch block that is unreachable due to return before it
        String code = "function f() { try { return 1; } catch(e) { alert('catch'); } }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        // The catch block might be removed if unreachable, but the return should remain
        assertTrue("Return should be preserved", result.contains("return 1"));
    }
    
    @Test(timeout = 4000)
    public void testMultipleReturns() {
        Compiler compiler = createCompiler();
        String code = "function f(x) { if (x) { return 1; } return 2; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("First return should be preserved", result.contains("return 1"));
        assertTrue("Second return should be preserved", result.contains("return 2"));
    }
    
    @Test(timeout = 4000)
    public void testDeadCodeAfterThrow() {
        Compiler compiler = createCompiler();
        String code = "function f() { throw new Error(); var x = 1; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertFalse("Dead code after throw should be removed", result.contains("x = 1"));
    }
    
    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    
    @Test(timeout = 4000)
    public void testCompilerPassInterface() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, true);
        assertNotNull("CompilerPass should be created", uce);
        assertTrue("Should implement CompilerPass", uce instanceof CompilerPass);
    }
    
    @Test(timeout = 4000)
    public void testRemoveNoOpStatementsTrue() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, true);
        String code = "function f() { true; false; }";
        Node externs = compiler.parse(EXTERN_URL, "function alert(x) {}");
        Node root = compiler.parseSyntheticCode("test", code);
        uce.process(externs, root);
        String result = compiler.toSource(root);
        assertFalse("No-op true should be removed", result.contains("true"));
        assertFalse("No-op false should be removed", result.contains("false"));
    }
    
    @Test(timeout = 4000)
    public void testRemoveNoOpStatementsFalse() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, false);
        String code = "function f() { true; false; }";
        Node externs = compiler.parse(EXTERN_URL, "function alert(x) {}");
        Node root = compiler.parseSyntheticCode("test", code);
        uce.process(externs, root);
        String result = compiler.toSource(root);
        // When removeNoOpStatements is false, no-ops should NOT be removed
        assertTrue("No-op true should be preserved when removeNoOpStatements is false", result.contains("true"));
        assertTrue("No-op false should be preserved when removeNoOpStatements is false", result.contains("false"));
    }
    
    @Test(timeout = 4000)
    public void testCodeChangeDetection() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, true);
        String code = "function f() { return 1; var x = 2; }";
        Node externs = compiler.parse(EXTERN_URL, "function alert(x) {}");
        Node root = compiler.parseSyntheticCode("test", code);
        uce.process(externs, root);
        // Should have reported code change
        assertTrue("Code should have changed", compiler.hasChanged());
    }
    
    @Test(timeout = 4000)
    public void testNoCodeChangeForValidCode() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, true);
        String code = "function f() { alert(1); }";
        Node externs = compiler.parse(EXTERN_URL, "function alert(x) {}");
        Node root = compiler.parseSyntheticCode("test", code);
        uce.process(externs, root);
        // Should not have reported code change for valid code
        assertFalse("Code should not have changed", compiler.hasChanged());
    }
    
    @Test(timeout = 4000)
    public void testMultiplePasses() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, true);
        String code = "function f() { return 1; return 2; return 3; }";
        Node externs = compiler.parse(EXTERN_URL, "function alert(x) {}");
        Node root = compiler.parseSyntheticCode("test", code);
        uce.process(externs, root);
        String result = compiler.toSource(root);
        // Only the first return should remain
        assertTrue("First return should remain", result.contains("return 1"));
        assertFalse("Second return should be removed", result.contains("return 2"));
        assertFalse("Third return should be removed", result.contains("return 3"));
    }
    
    @Test(timeout = 4000)
    public void testNestedFunctions() {
        Compiler compiler = createCompiler();
        String code = "function outer() { function inner() { return 1; var x = 2; } return 3; }";
        Node root = parseAndRun(compiler, code);
        String result = compiler.toSource(root);
        assertTrue("Outer function should remain", result.contains("outer"));
        assertTrue("Inner function should remain", result.contains("inner"));
        assertFalse("Dead code in inner function should be removed", result.contains("x = 2"));
    }
}