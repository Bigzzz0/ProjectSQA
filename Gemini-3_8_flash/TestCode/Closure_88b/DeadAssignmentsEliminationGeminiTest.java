package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Target Class: DeadAssignmentsElimination
 *
 * Core Logic & Decision Branches Covered:
 * 1. Scope Filtering:
 *    - Global scope vs Function scope (global assignments ignored).
 *    - Inner functions / closures present (all local variable dead assignment elimination aborted).
 *    - Presence vs absence of removable assigns (matchRemovableAssigns predicate).
 * 2. Removable Assignment Types:
 *    - Normal assignment (`=`): RHS replaced directly into AST.
 *    - Compound assignment (`+=`, `-=`, etc.): transformed to binary operator (e.g. `x + 1`).
 *    - Increment/Decrement (`++`, `--`):
 *      a) Inside expression node (`void 0;`).
 *      b) Inside FOR loop condition/update (`Token.EMPTY`).
 *      c) In value expression context (cannot replace, e.g. `y = x++`).
 *    - Self/Identity assignment (`x = x`): unconditionally replaced with RHS name.
 *    - Chained dead assignments (`x = y = 1`): recursive bottom-up cleanup.
 * 3. CFG Node Conditions & Branches:
 *    - IF, WHILE, DO conditions.
 *    - FOR condition (standard for-loops vs for-in loops).
 *    - SWITCH expression, CASE labels.
 *    - RETURN statements with and without return value.
 * 4. Variable Liveness within Expressions:
 *    - Local vs global / undeclared names.
 *    - Variable read before kill vs killed before read.
 *    - Sibling traversal and parent AST climbing.
 *
 * Ground Truth Defect Analysis (Closure Defect / Issue 297):
 * - Methods: testIssue297a through testIssue297f and related conditional read tests.
 * - Flaw: In tryRemoveAssignment, `state.getIn().isLive(var)` was required to be true before
 *   calling `isVariableStillLiveWithinExpression`. For variables declared without an initial value
 *   (`var x;`), `state.getIn().isLive(var)` evaluates to false at the CFG node entry.
 *   Consequently, assignments in short-circuiting logical expressions (`&&`, `||`, `?:`) were
 *   falsely identified as dead and eliminated, breaking the execution logic whenever the variable
 *   was read later or conditionally evaluated.
 * - The test suite asserts the preservation of these expressions (via testSame).
 */
public class DeadAssignmentsEliminationGeminiTest {

  private void test(String js, String expected) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    DeadAssignmentsElimination pass = new DeadAssignmentsElimination(compiler);
    pass.process(externs, root);

    Compiler expectedCompiler = new Compiler();
    Node expectedRoot = expectedCompiler.parseTestCode(expected);

    assertEquals(expectedCompiler.toSource(expectedRoot), compiler.toSource(root));
  }

  private void testSame(String js) {
    test(js, js);
  }

  // ===============================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ===============================================================================================

  @Test(timeout = 4000)
  public void testSimpleDeadAssignmentElimination() {
    test("function f() { var x; x = 1; }",
         "function f() { var x; 1; }");
  }

  @Test(timeout = 4000)
  public void testLiveAssignmentPreserved() {
    testSame("function f() { var x; x = 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testCompoundAssignmentOpElimination() {
    test("function f() { var x; x += 1; }",
         "function f() { var x; x + 1; }");
  }

  @Test(timeout = 4000)
  public void testCompoundAssignmentSubtract() {
    test("function f() { var x; x -= 2; }",
         "function f() { var x; x - 2; }");
  }

  @Test(timeout = 4000)
  public void testExpressionNodeIncrementElimination() {
    test("function f() { var x; x++; }",
         "function f() { var x; void 0; }");
  }

  @Test(timeout = 4000)
  public void testExpressionNodeDecrementElimination() {
    test("function f() { var x; x--; }",
         "function f() { var x; void 0; }");
  }

  @Test(timeout = 4000)
  public void testPrefixIncrementElimination() {
    test("function f() { var x; ++x; }",
         "function f() { var x; void 0; }");
  }

  @Test(timeout = 4000)
  public void testPrefixDecrementElimination() {
    test("function f() { var x; --x; }",
         "function f() { var x; void 0; }");
  }

  @Test(timeout = 4000)
  public void testForLoopUpdateIncrementElimination() {
    test("function f() { var x; for (; ; x++) {} }",
         "function f() { var x; for (; ; ) {} }");
  }

  @Test(timeout = 4000)
  public void testIncrementInValueContextNotEliminated() {
    testSame("function f() { var x, y; y = x++; return y; }");
  }

  @Test(timeout = 4000)
  public void testIdentityAssignmentElimination() {
    test("function f() { var x; x = x; }",
         "function f() { var x; x; }");
  }

  @Test(timeout = 4000)
  public void testChainedDeadAssignmentsElimination() {
    test("function f() { var x, y; x = y = 1; }",
         "function f() { var x, y; 1; }");
  }

  @Test(timeout = 4000)
  public void testChainedAssignmentOneLiveOneDead() {
    test("function f() { var x, y; x = y = 1; return x; }",
         "function f() { var x, y; x = 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testReassignmentOverwritesDeadValue() {
    test("function f() { var x; x = 1; x = 2; return x; }",
         "function f() { var x; 1; x = 2; return x; }");
  }

  // ===============================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Control Flow Graph Extremes
  // ===============================================================================================

  @Test(timeout = 4000)
  public void testGlobalScopeAssignmentsNeverEliminated() {
    testSame("var x; x = 1; x = 2;");
  }

  @Test(timeout = 4000)
  public void testInnerFunctionPreservesAllVariables() {
    testSame("function f() { var x; x = 1; function inner() { return x; } x = 2; }");
  }

  @Test(timeout = 4000)
  public void testNoRemovableAssignsFastReturn() {
    testSame("function f() { var x = 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testUndeclaredGlobalAssignmentInFunctionPreserved() {
    testSame("function f() { globalVar = 1; }");
  }

  @Test(timeout = 4000)
  public void testPropertyAssignmentNotEliminated() {
    testSame("function f() { var obj = {}; obj.prop = 1; }");
  }

  @Test(timeout = 4000)
  public void testArrayElementAssignmentNotEliminated() {
    testSame("function f() { var arr = []; arr[0] = 1; }");
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentInIfCondition() {
    test("function f() { var x; if (x = 1) {} }",
         "function f() { var x; if (1) {} }");
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentInWhileCondition() {
    test("function f() { var x; while (x = 1) {} }",
         "function f() { var x; while (1) {} }");
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentInDoWhileCondition() {
    test("function f() { var x; do {} while (x = 1); }",
         "function f() { var x; do {} while (1); }");
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentInForCondition() {
    test("function f() { var x; for (; x = 1; ) {} }",
         "function f() { var x; for (; 1; ) {} }");
  }

  @Test(timeout = 4000)
  public void testForInAssignmentPreserved() {
    testSame("function f() { var obj = {}, x; for (x in obj) {} }");
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentInSwitchExpression() {
    test("function f() { var x; switch (x = 1) {} }",
         "function f() { var x; switch (1) {} }");
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentInCaseExpression() {
    test("function f(y) { var x; switch (y) { case (x = 1): break; } }",
         "function f(y) { var x; switch (y) { case 1: break; } }");
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentInReturnExpression() {
    test("function f() { var x; return x = 1; }",
         "function f() { var x; return 1; }");
  }

  @Test(timeout = 4000)
  public void testEmptyReturnStatement() {
    testSame("function f() { var x; x = 1; return; }");
  }

  // ===============================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 297 Regression Verification)
  // ===============================================================================================

  @Test(timeout = 4000)
  public void testIssue297a() {
    testSame("function f() { var x; return !(x = 1) || (x = 2); }");
  }

  @Test(timeout = 4000)
  public void testIssue297b() {
    testSame("function f() { var x; return (x = 1) && (x = 2); }");
  }

  @Test(timeout = 4000)
  public void testIssue297c() {
    testSame("function f() { var x; return (x = 1) ? (x = 2) : 0; }");
  }

  @Test(timeout = 4000)
  public void testIssue297d() {
    testSame("function f() { var x; return (x = 1) || (x = 2); }");
  }

  @Test(timeout = 4000)
  public void testIssue297e() {
    testSame("function f() { var x; return (x = 1) ? 0 : (x = 2); }");
  }

  @Test(timeout = 4000)
  public void testIssue297f() {
    testSame("function f() { var x; return !(x = 1) ? (x = 2) : 0; }");
  }

  @Test(timeout = 4000)
  public void testIssue297_conditionalAndRead() {
    testSame("function f() { var x; return (x = 1) && x; }");
  }

  @Test(timeout = 4000)
  public void testIssue297_conditionalOrRead() {
    testSame("function f() { var x; return (x = 1) || x; }");
  }

  @Test(timeout = 4000)
  public void testIssue297_hookReadInThen() {
    testSame("function f() { var x; return (x = 1) ? x : 0; }");
  }

  @Test(timeout = 4000)
  public void testIssue297_hookReadInElse() {
    testSame("function f() { var x; return !(x = 1) ? 0 : x; }");
  }

  @Test(timeout = 4000)
  public void testIssue297_binaryOpReadWithinExpression() {
    testSame("function f() { var x; return (x = 1) + x; }");
  }

  @Test(timeout = 4000)
  public void testIssue297_assignmentInIfConditionReadInside() {
    testSame("function f() { var x; if ((x = 1) && x) { return 2; } }");
  }

  // ===============================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ===============================================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testProcessNullExternsThrowsException() {
    Compiler compiler = new Compiler();
    DeadAssignmentsElimination pass = new DeadAssignmentsElimination(compiler);
    pass.process(null, new Node(Token.BLOCK));
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testProcessNullRootThrowsException() {
    Compiler compiler = new Compiler();
    DeadAssignmentsElimination pass = new DeadAssignmentsElimination(compiler);
    pass.process(new Node(Token.BLOCK), null);
  }

  @Test(timeout = 4000)
  public void testExitScopeAndVisitDoNotThrow() {
    Compiler compiler = new Compiler();
    DeadAssignmentsElimination pass = new DeadAssignmentsElimination(compiler);
    // Explicitly exercise no-op interface methods to verify safe execution
    pass.exitScope(null);
    pass.visit(null, null, null);
  }

  // ===============================================================================================
  // Partition E: Object Lifecycle & Multiple Passes Consistency
  // ===============================================================================================

  @Test(timeout = 4000)
  public void testMultipleSequentialPassesAreIdempotent() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function f() { var x; x = 1; x = 2; return x; }");
    Node externs = new Node(Token.BLOCK);

    DeadAssignmentsElimination pass1 = new DeadAssignmentsElimination(compiler);
    pass1.process(externs, root);
    String afterPass1 = compiler.toSource(root);

    DeadAssignmentsElimination pass2 = new DeadAssignmentsElimination(compiler);
    pass2.process(externs, root);
    String afterPass2 = compiler.toSource(root);

    assertEquals(afterPass1, afterPass2);
  }

  @Test(timeout = 4000)
  public void testMultipleFunctionsInSameScript() {
    test("function f1() { var x; x = 1; }\nfunction f2() { var y; y = 2; return y; }",
         "function f1() { var x; 1; }\nfunction f2() { var y; y = 2; return y; }");
  }
}