package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: CoalesceVariableNames
 *
 * Key Decision Branches Analyzed:
 * 1. Scope checks:
 *    - scope.isGlobal() -> early exit in enterScope/exitScope
 * 2. Interference Graph Construction:
 *    - Non-escaped vs. escaped locals (Set<Var> escaped)
 *    - IE 2-parameter callback workaround: functions with exactly 2 parameters
 *      have their parameters marked as escaped by LiveVariablesAnalysis.
 *    - Function declaration filtering: NodeUtil.isFunction(v.getParentNode()) excluded from graph
 *    - DiGraph implicit return nodes -> skipped
 *    - Self-comparison (v1 == v2) -> skipped
 *    - Nodes not in interferenceGraph -> skipped
 *    - Both v1, v2 are LP (formal params) -> connectIfNotFound
 *    - Both live at IN or OUT -> connectIfNotFound
 *    - Disjoint/Sequential -> LiveRangeChecker with crossed checks
 * 3. Visit & Renaming Logic:
 *    - colorings.isEmpty() / !NodeUtil.isName(n) / NodeUtil.isFunction(parent) -> early exit
 *    - vNode == null (non-local or escaped local) -> early exit
 *    - !usePseudoNames:
 *      * vNode.getValue().equals(coalescedVar) -> early exit
 *      * Coalesced rename + removeVarDeclaration
 *    - usePseudoNames:
 *      * allMergedNames.size() == 1 -> no renaming
 *      * while (t.getScope().isDeclared(pseudoName, true)) pseudoName += "$"
 *      * !vNode.getValue().equals(coalescedVar) && isVar(parent) -> removeVarDeclaration
 * 4. removeVarDeclaration Variations:
 *    - NodeUtil.isForIn(parent) -> replaceChild(var, name)
 *    - var.getChildCount() == 1:
 *      * name.hasChildren() & parent != Token.FOR -> wrapped in EXPR_RESULT
 *      * name.hasChildren() & parent == Token.FOR -> ASSIGN not wrapped in EXPR
 *      * !name.hasChildren() -> NodeUtil.removeChild(parent, var)
 *    - var.getChildCount() > 1:
 *      * !name.hasChildren() -> var.removeChild(name)
 *      * name.hasChildren() -> left as duplicate declaration
 *
 * Ground Truth Defect:
 * - Closure Defect testParameter4:
 *   function f(x, y) { if (x) { return x; } var z = 1; return z; }
 *   In functions with exactly 2 parameters, parameters are escaped to avoid
 *   the IE sort() callback mutation bug. Consequently, local variable 'z'
 *   must NOT coalesce with parameter 'x' or 'y'.
 */
public class CoalesceVariableNamesGeminiTest {

  private void test(String js, String expected) {
    test(js, expected, false);
  }

  private void test(String js, String expected, boolean usePseudoNames) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node root = compiler.parseTestCode(js);
    assertNotNull("Failed to parse js: " + js, root);

    Node externs = new Node(Token.BLOCK);
    CoalesceVariableNames pass = new CoalesceVariableNames(compiler, usePseudoNames);
    pass.process(externs, root);

    Node expectedRoot = compiler.parseTestCode(expected);
    assertNotNull("Failed to parse expected: " + expected, expectedRoot);

    String actualCode = compiler.toSource(root);
    String expectedCode = compiler.toSource(expectedRoot);
    assertEquals(expectedCode, actualCode);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testParameter1() {
    test("function f(x) { var y = 1; return y; }",
         "function f(x) { x = 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testParameter2() {
    test("function f(x) { if (x) { var y = 1; return y; } }",
         "function f(x) { if (x) { x = 1; return x; } }");
  }

  @Test(timeout = 4000)
  public void testParameter3() {
    test("function f(x) { if (x) { return x; } var y = 1; return y; }",
         "function f(x) { if (x) { return x; } x = 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testThreeParametersNoIEWorkaround() {
    // With 3 parameters, the IE 2-param workaround does not trigger; x can coalesce with w
    test("function f(x, y, z) { if (x) { return x; } var w = 1; return w; }",
         "function f(x, y, z) { if (x) { return x; } x = 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testSequentialVarsCoalesced() {
    test("function f() { var a = 1; alert(a); var b = 2; alert(b); }",
         "function f() { var a = 1; alert(a); a = 2; alert(a); }");
  }

  @Test(timeout = 4000)
  public void testInterferingVarsNotCoalesced() {
    test("function f() { var a = 1; var b = 2; return a + b; }",
         "function f() { var a = 1; var b = 2; return a + b; }");
  }

  @Test(timeout = 4000)
  public void testAssignmentOpCoalescing() {
    test("function f() { var x = 1; x += 2; alert(x); var y = 3; return y; }",
         "function f() { var x = 1; x += 2; alert(x); x = 3; return x; }");
  }

  @Test(timeout = 4000)
  public void testNestedFunctionsCoalescing() {
    test("function outer() { " +
         "  var a = 1; alert(a); var b = 2; alert(b); " +
         "  function inner() { var c = 3; alert(c); var d = 4; alert(d); } " +
         "  inner(); " +
         "}",
         "function outer() { " +
         "  var a = 1; alert(a); a = 2; alert(a); " +
         "  function inner() { var c = 3; alert(c); c = 4; alert(c); } " +
         "  inner(); " +
         "}");
  }

  @Test(timeout = 4000)
  public void testWhileLoopCoalesce() {
    test("function f() { var x = 1; while (x < 10) { x++; } var y = 2; alert(y); }",
         "function f() { var x = 1; while (x < 10) { x++; } x = 2; alert(x); }");
  }

  @Test(timeout = 4000)
  public void testSwitchStatementCoalesce() {
    test("function f(a) { var x = 1; switch (a) { case 1: alert(x); break; } var y = 2; alert(y); }",
         "function f(a) { var x = 1; switch (a) { case 1: alert(x); break; } x = 2; alert(x); }");
  }

  @Test(timeout = 4000)
  public void testTryCatchCoalesce() {
    test("function f() { var x = 1; try { alert(x); } catch (e) { alert(e); } var y = 2; alert(y); }",
         "function f() { var x = 1; try { alert(x); } catch (e) { alert(e); } x = 2; alert(x); }");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Control Structure Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyRoot() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("");
    CoalesceVariableNames pass = new CoalesceVariableNames(compiler, false);
    pass.process(new Node(Token.BLOCK), root);
    assertEquals("", compiler.toSource(root));
  }

  @Test(timeout = 4000)
  public void testEmptyFunction() {
    test("function f() {}", "function f() {}");
  }

  @Test(timeout = 4000)
  public void testSingleVariableNoCoalescing() {
    test("function f() { var x = 1; return x; }",
         "function f() { var x = 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testGlobalScopeOnlySkipped() {
    test("var x = 1; var y = 2; alert(x + y);",
         "var x = 1; var y = 2; alert(x + y);");
  }

  @Test(timeout = 4000)
  public void testForInLoopVariableRemoval() {
    // Tests NodeUtil.isForIn(parent) removal path
    test("function f(obj) { var x = 1; alert(x); for (var y in obj) { alert(y); } }",
         "function f(obj) { var x = 1; alert(x); for (x in obj) { alert(x); } }");
  }

  @Test(timeout = 4000)
  public void testForLoopHeaderWithInitializer() {
    // Tests parent.getType() == Token.FOR with initializer
    test("function f() { var x = 1; alert(x); for (var y = 2; y < 10; y++) { alert(y); } }",
         "function f() { var x = 1; alert(x); for (x = 2; x < 10; x++) { alert(x); } }");
  }

  @Test(timeout = 4000)
  public void testForLoopHeaderWithoutInitializer() {
    // Tests parent.getType() == Token.FOR without initializer replacing with EMPTY
    test("function f() { var x = 1; alert(x); for (var y; ; ) { y = 2; alert(y); } }",
         "function f() { var x = 1; alert(x); for ( ; ; ) { x = 2; alert(x); } }");
  }

  @Test(timeout = 4000)
  public void testVarDeclarationWithoutInitializerInBlock() {
    // Tests var.getChildCount() == 1 and !name.hasChildren() in normal statement
    test("function f() { var x = 1; alert(x); var y; y = 2; alert(y); }",
         "function f() { var x = 1; alert(x); x = 2; alert(x); }");
  }

  @Test(timeout = 4000)
  public void testMultipleVarsDeclarationUninitializedRemoved() {
    // Tests var.getChildCount() > 1 and !name.hasChildren()
    test("function f() { var x = 1; alert(x); var y, z = 2; alert(y); alert(z); }",
         "function f() { var x = 1; alert(x); var z = 2; alert(x); alert(z); }");
  }

  @Test(timeout = 4000)
  public void testMultipleVarsDeclarationInitializedRetained() {
    // Tests var.getChildCount() > 1 and name.hasChildren() (duplicated declaration branch)
    test("function f() { var x = 1; alert(x); var y = 2, z = 3; alert(y); alert(z); }",
         "function f() { var x = 1; alert(x); var x = 2, z = 3; alert(x); alert(z); }");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testParameter4() {
    // Exact test from Defects4J ground truth:
    // With exactly 2 parameters, x and y are escaped to work around IE's sort() callback bug.
    // Local variable z MUST NOT coalesce into x or y.
    test("function f(x, y) { if (x) { return x; } var z = 1; return z; }",
         "function f(x, y) { if (x) { return x; } var z = 1; return z; }");
  }

  @Test(timeout = 4000)
  public void testTwoParametersLocalDoesNotCoalesce() {
    // Confirms that when function has exactly 2 params, both are escaped and neither merges with local
    test("function f(x, y) { var z = 1; return z; }",
         "function f(x, y) { var z = 1; return z; }");
  }

  @Test(timeout = 4000)
  public void testOneParameterAllowsCoalesce() {
    // Contrast check: 1 parameter function does NOT trigger IE workaround, local z merges with x
    test("function f(x) { var z = 1; return z; }",
         "function f(x) { x = 1; return x; }");
  }

  // =========================================================================
  // Partition D: Pseudo-Names Debug Mode & Name Collision Guard
  // =========================================================================

  @Test(timeout = 4000)
  public void testPseudoNamesBasic() {
    test("function f() { var x = 1; alert(x); var y = 2; alert(y); }",
         "function f() { var x_y = 1; alert(x_y); x_y = 2; alert(x_y); }",
         true);
  }

  @Test(timeout = 4000)
  public void testPseudoNamesSingleVarNoChange() {
    test("function f() { var x = 1; return x; }",
         "function f() { var x = 1; return x; }",
         true);
  }

  @Test(timeout = 4000)
  public void testPseudoNamesCollisionResolutionSingleDollar() {
    // x_y is a live parameter across the scope. Coalescing x and y produces "x_y".
    // Since "x_y" is already declared, it must resolve collision to "x_y$".
    test("function f(x_y) { var x = 1; alert(x); var y = 2; alert(y); alert(x_y); }",
         "function f(x_y) { var x_y$ = 1; alert(x_y$); x_y$ = 2; alert(x_y$); alert(x_y); }",
         true);
  }

  @Test(timeout = 4000)
  public void testPseudoNamesCollisionResolutionMultipleDollars() {
    // Both x_y and x_y$ are already declared in scope. Coalescing x and y must append
    // multiple '$' signs in the while loop: "x_y" -> "x_y$" -> "x_y$$".
    test("function f(x_y, x_y$) { var x = 1; alert(x); var y = 2; alert(y); alert(x_y); alert(x_y$); }",
         "function f(x_y, x_y$) { var x_y$$ = 1; alert(x_y$$); x_y$$ = 2; alert(x_y$$); alert(x_y); alert(x_y$); }",
         true);
  }

  @Test(timeout = 4000)
  public void testPseudoNamesThreeVariablesMerged() {
    test("function f() { var a = 1; alert(a); var b = 2; alert(b); var c = 3; alert(c); }",
         "function f() { var a_b_c = 1; alert(a_b_c); a_b_c = 2; alert(a_b_c); a_b_c = 3; alert(a_b_c); }",
         true);
  }

  // =========================================================================
  // Partition E: Non-Locals, Named Functions & Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testFunctionDeclarationsNotCoalesced() {
    // Function declarations inside a function should not have their names coalesced
    test("function f() { function g() {} return g(); }",
         "function f() { function g() {} return g(); }");
  }

  @Test(timeout = 4000)
  public void testGlobalVariableAccessInsideFunction() {
    // Accessing global variables (vNode == null) must not crash or trigger renaming
    test("var g = 10; function f() { var x = 1; alert(x); var y = g; return y; }",
         "var g = 10; function f() { var x = 1; alert(x); x = g; return x; }");
  }

  @Test(timeout = 4000)
  public void testCoalescedVarSelfEquals() {
    // The super node variable's vNode.getValue().equals(coalescedVar) path
    test("function f() { var a = 1; alert(a); var b = 2; alert(b); }",
         "function f() { var a = 1; alert(a); a = 2; alert(a); }");
  }
}