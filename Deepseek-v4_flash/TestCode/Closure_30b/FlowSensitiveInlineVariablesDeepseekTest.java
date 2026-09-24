package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: FlowSensitiveInlineVariables (package-private class)
 * Known Defects (from Defects4J):
 *   - testInlineAcrossSideEffect1: Inlining across a side-effect boundary is incorrectly allowed.
 *   - testCanInlineAcrossNoSideEffect: Inlining across a no-side-effect path is incorrectly rejected.
 *   - testIssue698: A specific inlining scenario (likely involving side-effect checks) fails.
 *
 * Branch/Decision Coverage Targets:
 *   - enterScope: global scope early return; variable count threshold; function scope processing.
 *   - GatherCandidates.visit: CFG node existence; name node read-only check; exported name check;
 *     reaching def existence; outer scope dependency check.
 *   - Candidate.canInline: parameter check; def null check; assignment R-value check;
 *     side-effect right/left checks; RHS side-effect check; use count check; loop check;
 *     reaching uses size check; GETPROP/GETELEM/etc. check; path side-effect check.
 *   - checkRightOf / checkLeftOf: loop termination and predicate application.
 *   - inlineVariable: assign vs var vs other definition handling.
 *
 * Boundary/Partition Tests:
 *   - Partition A: Core functional logic (normal inlining, no inlining cases).
 *   - Partition B: Boundary values (null CFG node, empty scope, zero uses).
 *   - Partition C: Defect-targeted (side-effect across paths, no-side-effect paths, issue698).
 *   - Partition D: Defensive guards (global scope, too many variables).
 *   - Partition E: Lifecycle/contract (candidate state transitions).
 *
 * Note: The class is package-private; tests are in the same package.
 * We use the compiler infrastructure to build ASTs and run the pass.
 * For deterministic behavior, we use the Rhino parser via Compiler.
 */

public class FlowSensitiveInlineVariablesDeepseekTest {

  /** Helper to create a Compiler instance and parse JS source. */
  private Compiler createCompiler(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    // Parse the source into an AST.
    Node root = compiler.parseSyntheticCode("test.js", js);
    assertNotNull("Parsing failed", root);
    return compiler;
  }

  /** Helper to run the FlowSensitiveInlineVariables pass on a function body. */
  private void runPass(Compiler compiler, String functionBody) {
    String js = "function f() { " + functionBody + " }";
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseSyntheticCode("test.js", js);
    assertNotNull("Parsing failed", root);
    // Find the function node and run the pass.
    NodeTraversal traversal = new NodeTraversal(compiler, new FlowSensitiveInlineVariables(compiler));
    traversal.traverse(root);
  }

  /** Helper to get the compiled output source. */
  private String getResultSource(Compiler compiler) {
    return compiler.toSource();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleInlining() {
    Compiler compiler = createCompiler("function f() { var x = 1; print(x); }");
    runPass(compiler, "var x = 1; print(x);");
    String result = getResultSource(compiler);
    // After inlining, x should be replaced by 1.
    assertTrue("Expected inlining to happen", result.contains("print(1)"));
    assertFalse("Variable x should be removed", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenMultipleUses() {
    Compiler compiler = createCompiler("function f() { var x = 1; print(x); print(x); }");
    runPass(compiler, "var x = 1; print(x); print(x);");
    String result = getResultSource(compiler);
    // Should not inline because there are two uses.
    assertTrue("Variable x should remain", result.contains("var x"));
    assertTrue("First use remains", result.contains("print(x)"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenParameter() {
    Compiler compiler = createCompiler("function f(x) { print(x); }");
    runPass(compiler, "print(x);");
    String result = getResultSource(compiler);
    // Parameters cannot be inlined.
    assertTrue("Parameter x should remain", result.contains("print(x)"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenAssignmentUsedAsRValue() {
    Compiler compiler = createCompiler("function f() { var y; var x = (y = 1); print(x); }");
    runPass(compiler, "var y; var x = (y = 1); print(x);");
    String result = getResultSource(compiler);
    // Assignment used as R-value should not be inlined.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenRHSHasSideEffects() {
    Compiler compiler = createCompiler("function f() { var x = foo(); print(x); }");
    runPass(compiler, "var x = foo(); print(x);");
    String result = getResultSource(compiler);
    // RHS has a call with potential side effects, so no inlining.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenUseInsideLoop() {
    Compiler compiler = createCompiler("function f() { var x = 1; while(true) { print(x); } }");
    runPass(compiler, "var x = 1; while(true) { print(x); }");
    String result = getResultSource(compiler);
    // Use inside loop should not be inlined.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenRHSHasGetProp() {
    Compiler compiler = createCompiler("function f() { var x = a.b; print(x); }");
    runPass(compiler, "var x = a.b; print(x);");
    String result = getResultSource(compiler);
    // GETPROP in RHS should not be inlined.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyFunctionBody() {
    Compiler compiler = createCompiler("function f() { }");
    runPass(compiler, "");
    String result = getResultSource(compiler);
    // Nothing to inline, should not crash.
    assertNotNull("Result should not be null", result);
  }

  @Test(timeout = 4000)
  public void testNullCFGNodeHandling() {
    // A node that is not in the CFG (e.g., a function declaration) should be skipped.
    Compiler compiler = createCompiler("function f() { var x = 1; function g() {} print(x); }");
    runPass(compiler, "var x = 1; function g() {} print(x);");
    String result = getResultSource(compiler);
    // Should not crash; x may or may not be inlined depending on CFG structure.
    assertNotNull("Result should not be null", result);
  }

  @Test(timeout = 4000)
  public void testZeroUses() {
    Compiler compiler = createCompiler("function f() { var x = 1; }");
    runPass(compiler, "var x = 1;");
    String result = getResultSource(compiler);
    // No uses, so no inlining; variable may be removed by other passes but not here.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testGlobalScopeSkipped() {
    Compiler compiler = createCompiler("var x = 1; print(x);");
    // Run the pass on the global scope (should be skipped).
    Node root = compiler.parseSyntheticCode("test.js", "var x = 1; print(x);");
    NodeTraversal traversal = new NodeTraversal(compiler, new FlowSensitiveInlineVariables(compiler));
    traversal.traverse(root);
    String result = getResultSource(compiler);
    // Global scope should not be processed.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testTooManyVariablesSkipped() {
    // Build a function with many variables to exceed MAX_VARIABLES_TO_ANALYZE.
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE + 1; i++) {
      sb.append("var v").append(i).append(" = ").append(i).append("; ");
    }
    sb.append("print(v0);");
    Compiler compiler = createCompiler("function f() { " + sb.toString() + " }");
    runPass(compiler, sb.toString());
    String result = getResultSource(compiler);
    // Should skip analysis due to too many variables.
    assertTrue("Variable v0 should remain", result.contains("var v0"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // =========================================================================

  /**
   * Defect: testInlineAcrossSideEffect1
   * Inlining should NOT happen when there is a side-effect between definition and use.
   * Example: var x = foo(); bar(); print(x); -- bar() may affect x's value.
   */
  @Test(timeout = 4000)
  public void testInlineAcrossSideEffect1() {
    Compiler compiler = createCompiler("function f() { var x = foo(); bar(); print(x); }");
    runPass(compiler, "var x = foo(); bar(); print(x);");
    String result = getResultSource(compiler);
    // The side-effect call bar() between def and use should prevent inlining.
    assertTrue("Variable x should remain due to side effect", result.contains("var x"));
    assertTrue("Use should remain", result.contains("print(x)"));
  }

  /**
   * Defect: testCanInlineAcrossNoSideEffect
   * Inlining SHOULD happen when there is no side-effect between definition and use.
   * Example: var x = 1; var y = 2; print(x); -- no side effects.
   */
  @Test(timeout = 4000)
  public void testCanInlineAcrossNoSideEffect() {
    Compiler compiler = createCompiler("function f() { var x = 1; var y = 2; print(x); }");
    runPass(compiler, "var x = 1; var y = 2; print(x);");
    String result = getResultSource(compiler);
    // No side effects between def and use, so inlining should happen.
    assertTrue("Expected inlining to happen", result.contains("print(1)"));
    assertFalse("Variable x should be removed", result.contains("var x"));
  }

  /**
   * Defect: testIssue698
   * A specific scenario where inlining should be correctly handled.
   * Based on the issue, this likely involves a side-effect check on the left of the use.
   * Example: var x = 1; foo(); print(x); -- foo() is a side effect on the left of use.
   */
  @Test(timeout = 4000)
  public void testIssue698() {
    Compiler compiler = createCompiler("function f() { var x = 1; foo(); print(x); }");
    runPass(compiler, "var x = 1; foo(); print(x);");
    String result = getResultSource(compiler);
    // Side-effect foo() on the left of use should prevent inlining.
    assertTrue("Variable x should remain due to side effect", result.contains("var x"));
    assertTrue("Use should remain", result.contains("print(x)"));
  }

  /**
   * Additional defect-targeted test: side effect on the right of definition.
   * var x = foo(); print(x); -- foo() is a side effect on the right.
   */
  @Test(timeout = 4000)
  public void testSideEffectOnRightOfDefinition() {
    Compiler compiler = createCompiler("function f() { var x = foo(); print(x); }");
    runPass(compiler, "var x = foo(); print(x);");
    String result = getResultSource(compiler);
    // Side effect on RHS should prevent inlining.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  /**
   * Additional defect-targeted test: side effect on the left of use.
   * var x = 1; foo(); print(x); -- foo() is a side effect on the left of use.
   */
  @Test(timeout = 4000)
  public void testSideEffectOnLeftOfUse() {
    Compiler compiler = createCompiler("function f() { var x = 1; foo(); print(x); }");
    runPass(compiler, "var x = 1; foo(); print(x);");
    String result = getResultSource(compiler);
    // Side effect on left of use should prevent inlining.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testNullCompiler() {
    try {
      new FlowSensitiveInlineVariables(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testProcessWithNullExterns() {
    Compiler compiler = createCompiler("function f() { var x = 1; print(x); }");
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    Node root = compiler.parseSyntheticCode("test.js", "function f() { var x = 1; print(x); }");
    // process with null externs should not crash.
    pass.process(null, root);
    String result = getResultSource(compiler);
    assertNotNull("Result should not be null", result);
  }

  @Test(timeout = 4000)
  public void testVisitDoesNothing() {
    Compiler compiler = createCompiler("function f() { var x = 1; print(x); }");
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    Node root = compiler.parseSyntheticCode("test.js", "function f() { var x = 1; print(x); }");
    NodeTraversal t = new NodeTraversal(compiler, pass);
    // visit should not throw.
    pass.visit(t, root, null);
  }

  @Test(timeout = 4000)
  public void testExitScopeDoesNothing() {
    Compiler compiler = createCompiler("function f() { var x = 1; print(x); }");
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    Node root = compiler.parseSyntheticCode("test.js", "function f() { var x = 1; print(x); }");
    NodeTraversal t = new NodeTraversal(compiler, pass);
    // exitScope should not throw.
    pass.exitScope(t);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testCandidateStateTransitions() throws Exception {
    // Use reflection to test the private Candidate class indirectly.
    // We'll test the canInline logic through the pass behavior.
    Compiler compiler = createCompiler("function f() { var x = 1; print(x); }");
    runPass(compiler, "var x = 1; print(x);");
    String result = getResultSource(compiler);
    // Successful inlining means the candidate transitioned from gathered to inlined.
    assertTrue("Expected inlining", result.contains("print(1)"));
  }

  @Test(timeout = 4000)
  public void testMultipleCandidates() {
    Compiler compiler = createCompiler("function f() { var x = 1; var y = 2; print(x); print(y); }");
    runPass(compiler, "var x = 1; var y = 2; print(x); print(y);");
    String result = getResultSource(compiler);
    // Both should be inlined.
    assertTrue("Expected x inlined", result.contains("print(1)"));
    assertTrue("Expected y inlined", result.contains("print(2)"));
    assertFalse("Variable x should be removed", result.contains("var x"));
    assertFalse("Variable y should be removed", result.contains("var y"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenExportedName() {
    Compiler compiler = createCompiler("function f() { var x_ = 1; print(x_); }");
    runPass(compiler, "var x_ = 1; print(x_);");
    String result = getResultSource(compiler);
    // Exported names (with underscore) should not be inlined.
    assertTrue("Variable x_ should remain", result.contains("var x_"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenDependsOnOuterScope() {
    Compiler compiler = createCompiler("var a = 1; function f() { var x = a; print(x); }");
    runPass(compiler, "var x = a; print(x);");
    String result = getResultSource(compiler);
    // Depends on outer scope variable, so no inlining.
    assertTrue("Variable x should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testInliningWithLabel() {
    Compiler compiler = createCompiler("function f() { var x = 1; label: print(x); }");
    runPass(compiler, "var x = 1; label: print(x);");
    String result = getResultSource(compiler);
    // Inlining should work even with labels.
    assertTrue("Expected inlining", result.contains("print(1)"));
  }

  @Test(timeout = 4000)
  public void testInliningWithVarDefinition() {
    Compiler compiler = createCompiler("function f() { var x = 1; print(x); }");
    runPass(compiler, "var x = 1; print(x);");
    String result = getResultSource(compiler);
    // Var definition should be inlined.
    assertTrue("Expected inlining", result.contains("print(1)"));
  }

  @Test(timeout = 4000)
  public void testInliningWithAssignDefinition() {
    Compiler compiler = createCompiler("function f() { var x; x = 1; print(x); }");
    runPass(compiler, "var x; x = 1; print(x);");
    String result = getResultSource(compiler);
    // Assign definition should be inlined.
    assertTrue("Expected inlining", result.contains("print(1)"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenDefinitionNotFound() {
    // A use without a reaching definition should not be inlined.
    Compiler compiler = createCompiler("function f() { print(x); }");
    runPass(compiler, "print(x);");
    String result = getResultSource(compiler);
    // x is undefined, no inlining.
    assertTrue("Use should remain", result.contains("print(x)"));
  }

  @Test(timeout = 4000)
  public void testNoInliningWhenMultipleReachingDefs() {
    Compiler compiler = createCompiler("function f() { var x = 1; if (a) { x = 2; } print(x); }");
    runPass(compiler, "var x = 1; if (a) { x = 2; } print(x);");
    String result = getResultSource(compiler);
    // Multiple reaching definitions, no inlining.
    assertTrue("Variable x should remain", result.contains("var x"));
  }
}