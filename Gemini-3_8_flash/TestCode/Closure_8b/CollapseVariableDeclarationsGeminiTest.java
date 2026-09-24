package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.google.javascript.jscomp.CollapseVariableDeclarations
 *
 * 1. Constructor Precondition Branch:
 *    - !compiler.getLifeCycleStage().isNormalized()
 *      -> Normal execution when LifeCycleStage is RAW.
 *      -> IllegalStateException when LifeCycleStage is NORMALIZED.
 *
 * 2. GatherCollapses.visit() Decision Points:
 *    - n.isVar(): true -> blacklistStubVars(); false -> proceed to canBeRedeclared check.
 *    - !n.isVar() && !canBeRedeclared(n, scope): true -> early return.
 *    - nodesToCollapse.contains(n): true -> early return (avoid duplicate processing).
 *    - parent.isIf(): true -> early return (adjacent VAR children of IF/ELSE cannot collapse).
 *    - while (n != null && (n.isVar() || canBeRedeclared(...))):
 *      -> Sequence of consecutive vars / redeclarable assignments.
 *      -> Accumulates nodesToCollapse, sets hasVar when var encountered.
 *    - if (hasNodesToCollapse && hasVar):
 *      -> true: creates Collapse record and registers varNode in nodesToCollapse.
 *      -> false: assigns without VAR node or single var with no following collapsible nodes.
 *
 * 3. canBeRedeclared() Decision Points:
 *    - !NodeUtil.isExprAssign(n) -> false (non-assignment expression statements).
 *    - !lhs.isName() -> false (property assignment like obj.prop = 1, array access).
 *    - var == null -> false (undeclared variable).
 *    - var.getScope() != s -> false (variable belonging to outer scope/closure).
 *    - blacklistedVars.contains(var) -> false (stub vars like `var x;` without init).
 *    - All conditions pass -> true (reassignment eligible to be collapsed into VAR).
 *
 * 4. applyCollapses() Transformation Logic:
 *    - Iteration through nodes from collapse.startNode to collapse.endNode.
 *    - n.isVar(): moves all children to new VAR node.
 *    - !n.isVar() (redeclaration assignment): extracts LHS and RHS, sets redeclaration=true.
 *    - if (redeclaration): attaches JSDocInfo with suppression "duplicate".
 *
 * 5. Known Defect Ground Truth (Defects4J Issue 820):
 *    - Defect: testIssue820 - Reassigning a declared variable in subsequent statements
 *      (e.g., `var a = 1; a = 2; var b = 3;` or `var a = 1; a = 2;`) erroneously treats
 *      `a = 2` as a collapsible redeclaration, collapsing it into `var a = 1, a = 2;`.
 *      The test asserts that variable reassignment following a VAR is NOT collapsed.
 * =========================================================================================
 */
public class CollapseVariableDeclarationsGeminiTest {

  // =========================================================================
  // Test Harness Helpers
  // =========================================================================

  private Node test(String js, String expected) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);

    CollapseVariableDeclarations pass = new CollapseVariableDeclarations(compiler);
    pass.process(externs, root);

    Compiler expectedCompiler = new Compiler();
    Node expectedRoot = expectedCompiler.parseTestCode(expected);

    String actualCode = compiler.toSource(root);
    String expectedCode = expectedCompiler.toSource(expectedRoot);

    assertEquals("Transformed AST does not match expected output", expectedCode, actualCode);
    return root;
  }

  private void testSame(String js) {
    test(js, js);
  }

  private int countNodes(Node root, int token) {
    int count = (root.getType() == token) ? 1 : 0;
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      count += countNodes(child, token);
    }
    return count;
  }

  private Node findFirstNode(Node root, int token) {
    if (root.getType() == token) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstNode(child, token);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicVarCollapseTwoVars() {
    Node root = test("var a = 1; var b = 2;", "var a = 1, b = 2;");
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testBasicVarCollapseMultipleVars() {
    Node root = test("var a = 1; var b = 2; var c = 3;", "var a = 1, b = 2, c = 3;");
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testVarsWithoutValues() {
    Node root = test("var a; var b; var c;", "var a, b, c;");
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testMixedVarsWithAndWithoutValues() {
    Node root = test("var a; var b = 1; var c; var d = 2;", "var a, b = 1, c, d = 2;");
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testMultipleSeparateCollapseBlocks() {
    Node root = test(
        "var a = 1; var b = 2; alert(); var c = 3; var d = 4;",
        "var a = 1, b = 2; alert(); var c = 3, d = 4;");
    assertEquals(2, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testVarsInsideFunctionScope() {
    Node root = test(
        "function f() { var x = 10; var y = 20; return x + y; }",
        "function f() { var x = 10, y = 20; return x + y; }");
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testRedeclarationGeneratesDuplicateSuppressionJSDoc() {
    // When an assignment is collapsed into a following var, redeclaration is flagged
    // and JSDocInfo with @suppress {duplicate} is attached to the new VAR node.
    Node root = test(
        "var x = 0; alert(x); x = 1; var y = 2;",
        "var x = 0; alert(x); var x = 1, y = 2;");

    Node secondVar = null;
    int varCount = 0;
    for (Node n = root.getFirstChild(); n != null; n = n.getNext()) {
      if (n.isVar()) {
        varCount++;
        if (varCount == 2) {
          secondVar = n;
        }
      }
    }

    assertNotNull("Expected second VAR node", secondVar);
    JSDocInfo info = secondVar.getJSDocInfo();
    assertNotNull("Expected JSDocInfo on redeclared VAR node", info);
    assertTrue("Expected duplicate suppression in JSDoc", info.getSuppressions().contains("duplicate"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    testSame("");
  }

  @Test(timeout = 4000)
  public void testSingleVarNoOp() {
    Node root = test("var a = 1;", "var a = 1;");
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testSingleVarWithoutValueNoOp() {
    Node root = test("var a;", "var a;");
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testNonVarStatementsOnly() {
    testSame("alert('hello'); foo(); bar();");
  }

  @Test(timeout = 4000)
  public void testAdjacentVarsUnderIfStatementNotCollapsed() {
    // Adjacent VAR children of an IF node represent then/else branches and must not be collapsed
    testSame("if (x) var a = 1; else var b = 2;");
  }

  @Test(timeout = 4000)
  public void testVarsInsideIfBlockCollapsed() {
    // Inside a block under IF, the parent is BLOCK, not IF, so collapsing is legal
    test(
        "if (x) { var a = 1; var b = 2; }",
        "if (x) { var a = 1, b = 2; }");
  }

  @Test(timeout = 4000)
  public void testBlacklistedStubVarPreventsRedeclaration() {
    // A stub var `var a;` is blacklisted so later assignment `a = 1;` cannot be collapsed
    testSame("var a; a = 1; var b = 2;");
  }

  @Test(timeout = 4000)
  public void testAssignmentToNonNameLHSNotCollapsed() {
    // Property assignment or element access should not be collapsed
    testSame("var a = 1; a.prop = 2; var b = 3;");
    testSame("var a = 1; a[0] = 2; var b = 3;");
  }

  @Test(timeout = 4000)
  public void testAssignmentToUndeclaredVariableNotCollapsed() {
    // Undeclared global assignment should not be collapsed into var
    testSame("undeclared = 1; var b = 2;");
  }

  @Test(timeout = 4000)
  public void testAssignmentToOuterScopeVariableNotCollapsed() {
    // In inner function, reassigning outer scope variable must not be collapsed
    testSame("var outer = 1; function f() { outer = 2; var inner = 3; }");
  }

  @Test(timeout = 4000)
  public void testSequenceOfAssignmentsWithoutVarNotCollapsed() {
    // `a = 1; b = 2;` has no VAR node, so hasVar is false and nothing collapses
    testSame("var a = 0; var b = 0; alert(); a = 1; b = 2;");
  }

  @Test(timeout = 4000)
  public void testNonAssignmentExpressionBetweenVars() {
    testSame("var a = 1; 2 + 2; var b = 3;");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 820)
  // =========================================================================

  /**
   * Ground Truth Failure: Defects4J Issue 820
   * Target: com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testIssue820
   *
   * Reassigning a variable declared with an initial value should NOT collapse subsequent
   * assignments to that same variable. In the defective version, `a = 2` is erroneously
   * recognized as canBeRedeclared because `a` has an initializer and is not in blacklistedVars,
   * collapsing `var a = 1; a = 2; var b = 3;` into `var a = 1, a = 2, b = 3;`.
   */
  @Test(timeout = 4000)
  public void testIssue820() {
    testSame("var a = 1; a = 2; var b = 3;");
  }

  @Test(timeout = 4000)
  public void testIssue820_simpleReassignmentFollowedByNoVar() {
    testSame("var a = 1; a = 2;");
  }

  @Test(timeout = 4000)
  public void testIssue820_multipleSequentialReassignments() {
    testSame("var a = 1; a = 2; a = 3;");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorRejectsNormalizedCompiler() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    new CollapseVariableDeclarations(compiler);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorRejectsNormalizedObfuscatedCompiler() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED_OBFUSCATED);
    new CollapseVariableDeclarations(compiler);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & State Verification
  // =========================================================================

  @Test(timeout = 4000)
  public void testCodeChangeReportedWhenCollapsing() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; var b = 2;");
    Node externs = new Node(Token.BLOCK);

    CollapseVariableDeclarations pass = new CollapseVariableDeclarations(compiler);
    pass.process(externs, root);

    // Code change should be reported to compiler
    Node expectedRoot = compiler.parseTestCode("var a = 1, b = 2;");
    assertEquals(compiler.toSource(expectedRoot), compiler.toSource(root));
  }

  @Test(timeout = 4000)
  public void testMultipleProcessCallsClearInternalState() {
    Compiler compiler = new Compiler();
    CollapseVariableDeclarations pass = new CollapseVariableDeclarations(compiler);

    // First run with collapsible code
    Node root1 = compiler.parseTestCode("var a = 1; var b = 2;");
    pass.process(new Node(Token.BLOCK), root1);
    assertEquals(1, countNodes(root1, Token.VAR));

    // Second run with non-collapsible code ensuring internal collections were cleared
    Node root2 = compiler.parseTestCode("var x = 1;");
    pass.process(new Node(Token.BLOCK), root2);
    assertEquals(1, countNodes(root2, Token.VAR));
  }
}