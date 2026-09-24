package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: CheckSideEffects.java
 * 
 * Decision branches targeted:
 * 1. n.isEmpty() || n.isComma() -> early return
 * 2. parent == null -> early return
 * 3. n.isExprResult() -> early return
 * 4. n.isQualifiedName() && n.getJSDocInfo() != null -> early return
 * 5. parent.getType() == Token.COMMA:
 *    a. isResultUsed -> return
 *    b. n == parent.getLastChild() -> traverse ancestors
 *       i. ancestorType == COMMA -> continue
 *       ii. ancestorType != EXPR_RESULT && ancestorType != BLOCK -> return
 *       iii. else -> break (fall through to error check)
 *    c. else -> fall through to error check
 * 6. parent.getType() != EXPR_RESULT && parent.getType() != BLOCK:
 *    a. NOT (FOR with 4 children and n is first or third) -> return
 *    b. else -> fall through to error check
 * 7. (isSimpleOp || !NodeUtil.mayHaveSideEffects(n, t.getCompiler())) -> report error
 *    a. n.isString() -> specific message
 *    b. isSimpleOp -> specific message
 *    c. else -> default message
 * 8. If error reported and !NodeUtil.isStatement(n) -> add to problemNodes
 * 
 * Known defect: testUselessCode expects 1 warning but gets 0.
 * Likely root cause: issue with how EXPR_RESULT nodes are handled or
 * NodeUtil.mayHaveSideEffects returning incorrect result for certain nodes.
 * 
 * Boundary values tested:
 * - null parent
 * - empty nodes (semicolons)
 * - EXPR_RESULT nodes
 * - Qualified names with/without JSDoc
 * - COMMA expression trees (nested/not last child)
 * - FOR loop initializer/updater positions
 * - Various operator types (simple vs complex)
 * - String literals
 * - Side-effect free statements
 */
public class CheckSideEffectsDeepseekTest {

    // ===================== PARTITION A: Core Functional Logic =====================
    
    @Test(timeout = 4000)
    public void testProcessWithNoSideEffects() {
        // Test that a simple string literal in expression context generates warning
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "var x = 'hello'; 'world';";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have exactly 1 warning", 1, result.warnings.size());
        assertEquals("Useless code warning expected",
            CheckSideEffects.USELESS_CODE_ERROR,
            result.warnings.get(0).getType());
    }

    @Test(timeout = 4000)
    public void testProcessWithSideEffect() {
        // Test that side-effect code does not generate warning
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "var x = []; x.push(1);";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have 0 warnings", 0, result.warnings.size());
    }

    @Test(timeout = 4000)
    public void testExternProcessAndHotSwap() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        Node externs = IR.empty();
        Node root = IR.script();
        root.addChildToBack(IR.exprResult(IR.string("test")));
        checker.process(externs, root);
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testVisitEmptyNode() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        NodeTraversal t = new NodeTraversal(compiler, checker);
        Node empty = IR.empty();
        checker.visit(t, empty, IR.block());
        // Should not add to problemNodes or report error
    }

    @Test(timeout = 4000)
    public void testVisitNullParent() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        NodeTraversal t = new NodeTraversal(compiler, checker);
        checker.visit(t, IR.name("x"), null);
        // Should not throw NPE
    }

    @Test(timeout = 4000)
    public void testVisitExprResultNode() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        NodeTraversal t = new NodeTraversal(compiler, checker);
        Node exprResult = IR.exprResult(IR.string("test"));
        checker.visit(t, exprResult, IR.block());
        // Should skip - EXPR_RESULT is parent, not child
    }

    @Test(timeout = 4000)
    public void testQualifiedNameWithJSDoc() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        NodeTraversal t = new NodeTraversal(compiler, checker);
        Node name = IR.name("someVar");
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        builder.recordNoAlias();
        name.setJSDocInfo(builder.build(name));
        Node block = IR.block();
        block.addChildToBack(name);
        checker.visit(t, name, block);
        // Should not warn because it has JSDoc
    }

    // ===================== PARTITION B: Boundary Value Analysis =====================

    @Test(timeout = 4000)
    public void testCommaExpressionResultUsed() {
        // When comma expression result is used, should not warn
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "var x = (1, 2);";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have 0 warnings for used comma result", 0, result.warnings.size());
    }

    @Test(timeout = 4000)
    public void testCommaExpressionResultUnused() {
        // When comma expression result is unused, should warn
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "1, 2;";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have 1 warning for unused comma result", 1, result.warnings.size());
    }

    @Test(timeout = 4000)
    public void testSimpleOperatorUnused() {
        // Test that '+' operator with unused result triggers message about missing '+'
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "'hello' 'world';";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have 1 warning", 1, result.warnings.size());
        assertTrue("Should mention missing '+'", 
            result.warnings.get(0).description.contains("missing '+'"));
    }

    @Test(timeout = 4000)
    public void testForLoopInitializerNoSideEffect() {
        // Test that expression in for loop initializer without side effect warns
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "for (var x = 0; x < 10; x++) {}";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have 0 warnings for standard for loop", 0, result.warnings.size());
    }

    @Test(timeout = 4000)
    public void testSideEffectFreeProtection() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, true);
        Node externs = IR.empty();
        Node root = IR.script();
        root.addChildToBack(IR.exprResult(IR.string("test")));
        checker.process(externs, root);
        // After protection, should have added extern function and wrapped nodes
    }

    // ===================== PARTITION C: Defect-Targeted Branch Zone =====================

    @Test(timeout = 4000)
    public void testUselessCode() {
        // DIRECT TARGET OF KNOWN DEFECT: This should produce 1 warning
        // The defect says expected:1 but was:0 - testing the exact scenario
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        // Simple expression that has no side effects - should generate warning
        String code = "1 + 2;";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("There should be one warning", 1, result.warnings.size());
    }

    @Test(timeout = 4000)
    public void testMultipleUselessStatements() {
        // Test multiple useless statements produce multiple warnings
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "1; 'hello'; true;";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have 3 warnings", 3, result.warnings.size());
    }

    @Test(timeout = 4000)
    public void testUselessCodeWithProtectSideEffects() {
        // Test that protectSideEffects flag changes behavior with useless code
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        options.setProtectSideEffects(true);
        
        String code = "1 + 2;";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        // With protection, still a warning but code gets wrapped
        assertEquals("Should have 1 warning", 1, result.warnings.size());
    }

    @Test(timeout = 4000)
    public void testNestedCommaExpression() {
        // Test deeply nested comma expressions
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "(1, (2, 3));";
        Result result = compiler.compile(
            Collections.singletonList(SourceFile.fromCode("externs.js", "")),
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        assertEquals("Should have 1 warning", 1, result.warnings.size());
    }

    // ===================== PARTITION D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000)
    public void testWithNullExterns() {
        // Test process with null externs
        AbstractCompiler compiler = Compiler.createCompiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_SIDE_EFFECTS, CheckLevel.WARNING);
        
        String code = "var x = 1;";
        Result result = compiler.compile(
            null,
            Collections.singletonList(SourceFile.fromCode("test.js", code)),
            options);
        
        // Should not crash, but may produce different results
    }

    @Test(timeout = 4000)
    public void testHotSwapWithNullOriginalRoot() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        Node scriptRoot = IR.script();
        checker.hotSwapScript(scriptRoot, null);
        // Should not throw NPE
    }

    @Test(timeout = 4000)
    public void testVisitWithCommaParentAndLastChild() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        NodeTraversal t = new NodeTraversal(compiler, checker);
        
        // Create: EXPR_RESULT -> COMMA -> [string1, string2]
        Node comma = IR.comma(IR.string("a"), IR.string("b"));
        Node exprResult = IR.exprResult(comma);
        Node block = IR.block();
        block.addChildToBack(exprResult);
        
        // Visit the last child of comma (string "b")
        checker.visit(t, comma.getLastChild(), comma);
        // Should traverse to EXPR_RESULT and generate warning
    }

    @Test(timeout = 4000)
    public void testStripProtection() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects.StripProtection stripper = new CheckSideEffects.StripProtection(compiler);
        
        Node externs = IR.empty();
        Node root = IR.script();
        
        // Simulate protected code: JSCOMPILER_PRESERVE("test")
        Node call = IR.call(IR.name("JSCOMPILER_PRESERVE"));
        call.addChildToBack(IR.string("test"));
        root.addChildToBack(IR.exprResult(call));
        
        stripper.process(externs, root);
        // After stripping, the call should be replaced with just the string
        assertTrue("Call should be stripped", 
            root.getFirstChild().getFirstChild().isString());
    }

    // ===================== PARTITION E: Object Lifecycle & Contract =====================

    @Test(timeout = 4000)
    public void testConstructorAndState() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
        assertNotNull("Checker should be created", checker);
    }

    @Test(timeout = 4000)
    public void testProtectSideEffectsWithEmptyProblemNodes() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, true);
        Node externs = IR.empty();
        Node root = IR.script();
        checker.process(externs, root);
        // With no problem nodes, protectSideEffects should do nothing silently
    }

    @Test(timeout = 4000)
    public void testAddExternAndCompilerCodeChange() {
        AbstractCompiler compiler = Compiler.createCompiler();
        CheckSideEffects checker = new CheckSideEffects(compiler, CheckLevel.WARNING, true);
        
        // Trigger problem detection
        NodeTraversal t = new NodeTraversal(compiler, checker);
        Node name = IR.name("x");
        Node assign = IR.assign(name, IR.number(1));
        Node exprResult = IR.exprResult(assign);
        Node block = IR.block();
        block.addChildToBack(exprResult);
        
        // Visit the assignment node inside expression result
        checker.visit(t, assign, exprResult);
        // The assigment has side effects, so no warning, but ensures method works
    }
}