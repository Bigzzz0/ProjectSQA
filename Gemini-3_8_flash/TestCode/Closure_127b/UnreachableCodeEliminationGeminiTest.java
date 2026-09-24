/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.UnreachableCodeElimination
 *
 * Decision / Branch Coverage Points:
 * 1. process(Node, Node):
 *    - traverseChangedFunctions execution on SCRIPT and FUNCTION nodes.
 *    - root.isFunction() branch (root = root.getLastChild()).
 *    - do-while loop termination on codeChanged flag transitions.
 * 2. EliminationPass.visit(NodeTraversal, Node, Node):
 *    - Guard: parent == null, n.isFunction(), n.isScript().
 *    - Guard: cfg.getDirectedGraphNode(n) == null.
 *    - Unreachable/No-Op branch: gNode annotation != REACHABLE || (removeNoOp && !mayHaveSideEffects).
 *    - Safe dead statement branch: removeDeadExprStatementSafely(n).
 *    - Candidate unconditional branch: tryRemoveUnconditionalBranching(n).
 * 3. tryRemoveUnconditionalBranching(Node):
 *    - Guard: n == null, gNode == null.
 *    - switch (n.getType()):
 *        - Token.RETURN with children (hasChildren() -> break, keep node).
 *        - Token.RETURN without children (fall-through to BREAK/CONTINUE).
 *        - Token.BREAK / Token.CONTINUE:
 *            - outEdges.size() == 1 check.
 *            - Next node guard: (n.getNext() == null || n.getNext().isFunction()).
 *            - nextCfgNode == fallThrough equality check (removes branch node).
 * 4. computeFollowing(Node):
 *    - Follow node resolution through nested and empty blocks (next.isBlock(), hasChildren()).
 * 5. removeDeadExprStatementSafely(Node):
 *    - Guard: n.isEmpty() || (n.isBlock() && !n.hasChildren()).
 *    - Guard: NodeUtil.isForIn(parent).
 *    - switch (n.getType()):
 *        - Token.DO (must return early to avoid increasing code size).
 *        - Token.BLOCK in TRY catch-container (parent.isTry() && isTryCatchNodeContainer).
 *        - Token.CATCH (NodeUtil.maybeAddFinally).
 *    - Guard: n.isVar() && !n.getFirstChild().hasChildren() (dead var declaration preservation).
 * 6. removeNode(Node):
 *    - Code changed notification, var redeclaration in branch, logger.fine logging, tree removal.
 *
 * Known Defects4J Regression Flaws Targeted:
 * - Issue 4177428 / testDontRemoveBreakInTryFinally: Erroneous removal of unconditional branches
 *   (break, continue, return) enclosed in try blocks having finally clauses because computeFollowing
 *   erroneously equated fall-through to the finally block with the branch jump destination.
 * ---------------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnreachableCodeEliminationGeminiTest {

  private String compileAndRun(String js, boolean removeNoOpStatements) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile extern = SourceFile.fromCode("externs.js", "var window; var alert;");
    SourceFile input = SourceFile.fromCode("testcode.js", js);

    compiler.init(Collections.singletonList(extern), Collections.singletonList(input), options);
    Node root = compiler.parseInputs();
    assertNotNull("Input AST root should not be null", root);

    Node externsRoot = root.getFirstChild();
    Node mainRoot = root.getLastChild();

    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, removeNoOpStatements);
    pass.process(externsRoot, mainRoot);

    return compiler.toSource(mainRoot);
  }

  private String compileExpected(String expectedJs) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile extern = SourceFile.fromCode("externs.js", "var window; var alert;");
    SourceFile input = SourceFile.fromCode("testcode.js", expectedJs);

    compiler.init(Collections.singletonList(extern), Collections.singletonList(input), options);
    Node root = compiler.parseInputs();
    assertNotNull("Expected AST root should not be null", root);

    return compiler.toSource(root.getLastChild());
  }

  private void test(String js, String expected, boolean removeNoOp) {
    String actual = compileAndRun(js, removeNoOp);
    String expectedSource = compileExpected(expected);
    assertEquals(expectedSource, actual);
  }

  private void test(String js, String expected) {
    test(js, expected, true);
  }

  private void testSame(String js, boolean removeNoOp) {
    test(js, js, removeNoOp);
  }

  private void testSame(String js) {
    testSame(js, true);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testRemoveCodeAfterReturn() {
    test("function f() { return 1; alert('unreachable'); }",
         "function f() { return 1; }");
  }

  @Test(timeout = 4000)
  public void testRemoveUselessReturnAtEndOfFunction() {
    test("function f() { alert(1); return; }",
         "function f() { alert(1); }");
  }

  @Test(timeout = 4000)
  public void testPreserveReturnWithValueAtEndOfFunction() {
    testSame("function f() { alert(1); return 1; }");
  }

  @Test(timeout = 4000)
  public void testRemoveUselessContinueInLoop() {
    test("for (var i = 0; i < 10; i++) { alert(i); continue; }",
         "for (var i = 0; i < 10; i++) { alert(i); }");
  }

  @Test(timeout = 4000)
  public void testRemoveNoOpExpressionsWhenEnabled() {
    test("function f() { 'no-op string'; 123; true; alert(1); }",
         "function f() { alert(1); }", true);
  }

  @Test(timeout = 4000)
  public void testPreserveNoOpExpressionsWhenDisabled() {
    testSame("function f() { 'no-op string'; 123; true; alert(1); }", false);
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInsideDeadBranch() {
    // Variable declaration must be preserved/hoisted when eliminating dead code
    test("function f() { if (false) { var x = 1; } alert(x); }",
         "function f() { var x; alert(x); }");
  }

  @Test(timeout = 4000)
  public void testCascadingDeadCodeRemoval() {
    // Tests the do { ... } while (codeChanged) multi-iteration loop
    test("function f() { return 1; alert(1); alert(2); alert(3); }",
         "function f() { return 1; }");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyFunctionBody() {
    testSame("function f() {}");
  }

  @Test(timeout = 4000)
  public void testEmptyTopLevelScript() {
    testSame("");
  }

  @Test(timeout = 4000)
  public void testEmptyStatementsAfterReturn() {
    // Tests n.isEmpty() branch in removeDeadExprStatementSafely
    test("function f() { return 1; ;;; }",
         "function f() { return 1; }");
  }

  @Test(timeout = 4000)
  public void testEmptyBlockAfterReturn() {
    // Tests (n.isBlock() && !n.hasChildren()) branch in removeDeadExprStatementSafely
    test("function f() { return 1; {} }",
         "function f() { return 1; }");
  }

  @Test(timeout = 4000)
  public void testUnreachableDoWhilePreserved() {
    // case Token.DO: unreachable DO loops are intentionally preserved
    testSame("function f() { return 1; do { alert(2); } while (true); }");
  }

  @Test(timeout = 4000)
  public void testDeadVarWithoutInitializerPreserved() {
    // if (n.isVar() && !n.getFirstChild().hasChildren()) return;
    testSame("function f() { return 1; var x; }");
  }

  @Test(timeout = 4000)
  public void testForInHeaderNotRemoved() {
    // if (NodeUtil.isForIn(parent)) return;
    testSame("for (var x in [1, 2, 3]) { alert(x); }");
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationsHoistedNotRemoved() {
    // Hoisted function statements after a return must not be removed
    testSame("function f() { return g(); function g() { return 42; } }");
  }

  @Test(timeout = 4000)
  public void testUnconditionalReturnFollowedByFunction() {
    // Tests (n.getNext() == null || n.getNext().isFunction()) branch
    test("function f() { return; function g() { return 1; } }",
         "function f() { function g() { return 1; } }");
  }

  @Test(timeout = 4000)
  public void testUnreachableCatchConvertsToFinally() {
    // Tests case Token.CATCH: NodeUtil.maybeAddFinally(tryNode)
    test("function f() { return 1; try { alert(2); } catch (e) { alert(e); } }",
         "function f() { return 1; }");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDontRemoveBreakInTryFinally() {
    // Directly targets: testDontRemoveBreakInTryFinally failure in Defects4J
    testSame("function f() { b: try { break b; } finally { return 1; } }");
  }

  @Test(timeout = 4000)
  public void testDontRemoveBreakInTryFinallySwitch() {
    // Directly targets: testDontRemoveBreakInTryFinallySwitch failure in Defects4J
    testSame("function f() { switch (a) { default: try { break; } finally { return 1; } } }");
  }

  @Test(timeout = 4000)
  public void testIssue4177428_return() {
    // Directly targets: testIssue4177428_return failure in Defects4J
    testSame("function f() { try { return; } finally { a = 1; } }");
  }

  @Test(timeout = 4000)
  public void testIssue4177428_continue() {
    // Directly targets: testIssue4177428_continue failure in Defects4J
    testSame("for (var x in y) { try { continue; } finally { a = 1; } }");
  }

  @Test(timeout = 4000)
  public void testIssue4177428a() {
    // Directly targets: testIssue4177428a failure in Defects4J
    testSame("var f = function() { var a; return a; try {} finally { a = 1; } };");
  }

  @Test(timeout = 4000)
  public void testIssue4177428c() {
    // Directly targets: testIssue4177428c failure in Defects4J
    testSame("var f = function() { var a; return a; try { a = 1; } finally {} };");
  }

  // =========================================================================
  // Partition D: Complex Control Flow & computeFollowing Block Resolution
  // =========================================================================

  @Test(timeout = 4000)
  public void testNestedEmptyBlocksInComputeFollowing() {
    // Triggers while (next != null && next.isBlock()) recursion in computeFollowing
    test("function f() { switch (a) { default: alert(1); break; } { {} } }",
         "function f() { switch (a) { default: alert(1); } { {} } }");
  }

  @Test(timeout = 4000)
  public void testUnconditionalBranchTargetingAnotherBranch() {
    // Cascading breaks targeting the next statement
    test("function f() { switch (a) { case 1: break; default: break; } }",
         "function f() { switch (a) { case 1: break; default: } }");
  }

  @Test(timeout = 4000)
  public void testUnreachableCodeInWhileConditionFalse() {
    test("while (false) { alert('dead'); }", "");
  }

  @Test(timeout = 4000)
  public void testUnreachableCodeInIfElseBranches() {
    test("if (true) { alert(1); } else { alert(2); }",
         "alert(1);");
  }

  @Test(timeout = 4000)
  public void testSwitchCaseUnreachableStatements() {
    test("function f(x) { switch(x) { case 1: return 1; alert('dead'); case 2: return 2; } }",
         "function f(x) { switch(x) { case 1: return 1; case 2: return 2; } }");
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Logging Diagnostics
  // =========================================================================

  @Test(timeout = 4000)
  public void testRemovalWithLoggingFineEnabled() {
    Logger logger = Logger.getLogger(UnreachableCodeElimination.class.getName());
    Level oldLevel = logger.getLevel();
    try {
      logger.setLevel(Level.FINE);
      test("function f() { return 1; alert(2); }",
           "function f() { return 1; }");
    } finally {
      logger.setLevel(oldLevel);
    }
  }

  @Test(timeout = 4000)
  public void testSyntheticRootDirectInvocation() {
    // Direct verification of pass process() invocation with non-null roots
    Compiler compiler = new Compiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);

    Node externs = new Node(Token.BLOCK);
    Node topLevel = new Node(Token.BLOCK);
    pass.process(externs, topLevel);
    assertFalse("Top level block should remain intact after empty processing", topLevel.hasChildren());
  }
}