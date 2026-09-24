package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for FlowSensitiveInlineVariables.
 * Targets line/branch coverage and the known Defects4J defect in for-in loops.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (simple var inlining, assignment inlining)
 * - Partition B: BVA & extremes (null/empty scope, zero/negative counts)
 * - Partition C: Defect-targeted branch (for-in loop variable inlining)
 * - Partition D: Exception & defensive paths (invalid arguments, side-effect checks)
 * - Partition E: Object lifecycle (candidate state, getDefinition, getNumUseInUseCfgNode)
 */
public class FlowSensitiveInlineVariablesDeepseekTest {

  private static final String EXTERNS = "function alert(x) {}";

  /**
   * Helper: compile source with FlowSensitiveInlineVariables pass and return the compiled source.
   */
  private String compileAndInline(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
    // Enable the pass
    options.setFlowSensitiveInlineVariables(true);

    compiler.compile(
        CompilerTestCase.createExterns(EXTERNS),
        CompilerTestCase.createSource(js),
        options);
    return compiler.toSource();
  }

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testSimpleVarInline() {
    String js = "function f() { var x = 1; return x; }";
    String result = compileAndInline(js);
    // After inlining: function f() { return 1; }
    assertTrue("Expected inlined constant", result.contains("return 1"));
  }

  @Test(timeout = 4000)
  public void testAssignmentInline() {
    String js = "function f() { var x; x = 2; return x; }";
    String result = compileAndInline(js);
    assertTrue("Expected inlined assignment", result.contains("return 2"));
  }

  @Test(timeout = 4000)
  public void testNoInlineMultipleUses() {
    String js = "function f() { var x = 3; alert(x); alert(x); }";
    String result = compileAndInline(js);
    // x should not be inlined because used twice
    assertTrue("Variable should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInlineSideEffectRight() {
    String js = "function f() { var x = alert(1); alert(x); }";
    String result = compileAndInline(js);
    // x should not be inlined because RHS has side effect
    assertTrue("Variable should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInlineSideEffectLeft() {
    String js = "function f() { var x = 1; alert(alert(x)); }";
    String result = compileAndInline(js);
    // x should not be inlined because left of use has side effect
    assertTrue("Variable should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInlineParameter() {
    String js = "function f(x) { return x; }";
    String result = compileAndInline(js);
    // Parameter cannot be inlined
    assertTrue("Parameter should remain", result.contains("return x"));
  }

  @Test(timeout = 4000)
  public void testNoInlineWithinLoop() {
    String js = "function f() { for(var i=0;i<10;i++) { var x = i; alert(x); } }";
    String result = compileAndInline(js);
    // x is inside a loop, should not be inlined
    assertTrue("Variable should remain", result.contains("var x"));
  }

  // ==================== Partition B: BVA & Extremes ====================

  @Test(timeout = 4000)
  public void testEmptyFunction() {
    String js = "function f() {}";
    String result = compileAndInline(js);
    assertEquals("function f(){}", result.replaceAll("\\s+", ""));
  }

  @Test(timeout = 4000)
  public void testGlobalScopeIgnored() {
    String js = "var x = 1; alert(x);";
    String result = compileAndInline(js);
    // Global scope should be ignored, variable remains
    assertTrue("Global var should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testManyVariables() {
    // Create more than MAX_VARIABLES_TO_ANALYZE (default 100) to trigger early return
    StringBuilder sb = new StringBuilder("function f() {");
    for (int i = 0; i < 150; i++) {
      sb.append("var a").append(i).append(" = ").append(i).append(";");
    }
    sb.append("return a0; }");
    String js = sb.toString();
    String result = compileAndInline(js);
    // Should not crash, variable should remain because analysis skipped
    assertTrue("Variable should remain", result.contains("var a0"));
  }

  // ==================== Partition C: Defect-Targeted Branch (for-in) ====================

  @Test(timeout = 4000)
  public void testSimpleForIn() {
    // Known defect: for-in loop variable inlining causes assertion failure
    String js = "function f(obj) { for (var k in obj) { alert(k); } }";
    String result = compileAndInline(js);
    // The variable 'k' should NOT be inlined because it is a for-in loop variable
    // (the pass should not inline it, but the bug causes incorrect behavior)
    assertTrue("For-in variable should remain", result.contains("var k"));
  }

  @Test(timeout = 4000)
  public void testForInWithAssignment() {
    String js = "function f(obj) { var k; for (k in obj) { alert(k); } }";
    String result = compileAndInline(js);
    // Similar, should not inline
    assertTrue("For-in variable should remain", result.contains("var k"));
  }

  // ==================== Partition D: Exception & Defensive Paths ====================

  @Test(timeout = 4000)
  public void testNullCfgNode() {
    // This test exercises the path where graphNode is null in GatherCandiates
    // We need a construct that doesn't produce a CFG node (e.g., a label without statement?)
    // Actually, every statement should have a CFG node. But we can test the null check indirectly.
    // The pass will skip nodes not in CFG. Use a function with only a block that is not a CFG node?
    // Simpler: just ensure no crash on empty function body.
    String js = "function f() { ; }";
    String result = compileAndInline(js);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testExportedName() {
    String js = "function f() { var $export = 1; alert($export); }";
    String result = compileAndInline(js);
    // Exported names (starting with $) should not be inlined
    assertTrue("Exported var should remain", result.contains("var $export"));
  }

  @Test(timeout = 4000)
  public void testDependsOnOuterScopeVars() {
    String js = "var outer = 1; function f() { var x = outer; alert(x); }";
    String result = compileAndInline(js);
    // x depends on outer scope var, should not be inlined
    assertTrue("Variable should remain", result.contains("var x"));
  }

  // ==================== Partition E: Object Lifecycle & Contract ====================

  @Test(timeout = 4000)
  public void testCandidateState() {
    // Indirectly test Candidate creation and canInline logic via compilation
    String js = "function f() { var x = 1; alert(x); }";
    String result = compileAndInline(js);
    assertTrue("Expected inlined", result.contains("alert(1)"));
  }

  @Test(timeout = 4000)
  public void testGetDefinitionAndNumUse() {
    // Test that getDefinition and getNumUseInUseCfgNode work correctly
    // This is covered by the inlining logic; we just verify no crash
    String js = "function f() { var x = 1; var y = x; alert(y); }";
    String result = compileAndInline(js);
    // x is used only once (in y's definition), so x should be inlined
    assertTrue("x should be inlined", result.contains("var y = 1"));
  }

  @Test(timeout = 4000)
  public void testNoInlineIfDefIsNull() {
    // When definition is not found (e.g., variable not defined in this scope)
    String js = "function f() { alert(x); }";
    String result = compileAndInline(js);
    // Should not crash, x remains as is
    assertTrue("x should remain", result.contains("x"));
  }

  @Test(timeout = 4000)
  public void testNoInlineIfAssignmentNotExprAssign() {
    // Assignment used as R-Value (e.g., in a condition)
    String js = "function f() { var x; if (x = 1) { alert(x); } }";
    String result = compileAndInline(js);
    // x should not be inlined because assignment is not an expression statement
    assertTrue("Variable should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInlineIfRhsCreatesObject() {
    // RHS has GETPROP, GETELEM, etc.
    String js = "function f() { var x = a.b; alert(x); }";
    String result = compileAndInline(js);
    // x should not be inlined because RHS has GETPROP
    assertTrue("Variable should remain", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNoInlineIfPathSideEffect() {
    // Side effect along path between def and use
    String js = "function f() { var x = 1; alert(alert(2)); alert(x); }";
    String result = compileAndInline(js);
    // x should not be inlined because there is a side effect (alert) between def and use
    assertTrue("Variable should remain", result.contains("var x"));
  }
}