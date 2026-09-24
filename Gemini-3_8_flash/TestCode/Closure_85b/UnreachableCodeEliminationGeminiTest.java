/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.UnreachableCodeElimination
 * Tested Logic & Branches Covered:
 *   1. visit(NodeTraversal, Node, Node):
 *      - parent == null branch -> early return.
 *      - n.getType() == Token.FUNCTION || Token.SCRIPT -> early return.
 *      - curCfg.getDirectedGraphNode(n) == null -> not in CFG, early return.
 *      - gNode annotation != REACHABLE -> trigger removeDeadExprStatementSafely.
 *      - removeNoOpStatements == true vs false && !NodeUtil.mayHaveSideEffects(n).
 *      - fall-through to tryRemoveUnconditionalBranching.
 *   2. tryRemoveUnconditionalBranching(Node):
 *      - n == null branch.
 *      - gNode == null branch.
 *      - n.getParent() == null branch with outEdges.size() == 1.
 *      - case Token.BLOCK: hasChildren() (recurse first) vs !hasChildren() (recurse follow node).
 *      - case Token.RETURN: hasChildren() (break/retain) vs !hasChildren() (removable jump).
 *      - case Token.BREAK / CONTINUE:
 *        - outEdges.size() == 1 and (next == null or next is FUNCTION).
 *        - nextCfgNode == fallThrough -> removeDeadExprStatementSafely & return fallThrough.
 *   3. removeDeadExprStatementSafely(Node):
 *      - Token.EMPTY or (Token.BLOCK && !hasChildren()) -> return immediately.
 *      - Token.DO -> retained without removal.
 *      - Token.BLOCK inside TRY catch container -> retained.
 *      - Token.CATCH -> maybeAddFinally logic.
 *      - NodeUtil.redeclareVarsInsideBranch(n) -> variable hoisting on unreachable dead branches.
 *      - Logger fine level branch execution.
 *   4. enterScope / exitScope:
 *      - cfgStack push / pop lifecycle and nested function CFG maintenance.
 *   5. Known Defects Covered:
 *      - testIssue311: Switch statement with conditional return followed by unconditional break
 *        triggering internal compiler error (unhandled CFG edge / follow branch).
 *      - testCascadedRemovalOfUnlessUnconditonalJumps: Cascaded redundant break statements in switch cases.
 * ---------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UnreachableCodeEliminationGeminiTest {

  /**
   * Helper method to parse input JS, apply UnreachableCodeElimination pass,
   * and compare the stringified source against the expected JS representation.
   */
  private void test(String js, String expected, boolean removeNoOpStatements) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, removeNoOpStatements);
    pass.process(externs, root);

    Compiler expectedCompiler = new Compiler();
    Node expectedRoot = expectedCompiler.parseTestCode(expected);

    String actualSource = compiler.toSource(root);
    String expectedSource = expectedCompiler.toSource(expectedRoot);
    assertEquals(expectedSource, actualSource);
  }

  private void test(String js, String expected) {
    test(js, expected, true);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnreachableCodeAfterReturn() {
    test("function f() { return; alert('unreachable'); }",
         "function f() {}");
  }

  @Test(timeout = 4000)
  public void testUnreachableCodeAfterThrow() {
    test("function f() { throw 'error'; alert('unreachable'); }",
         "function f() { throw 'error'; }");
  }

  @Test(timeout = 4000)
  public void testUselessReturnEliminationAtEndOfFunction() {
    test("function f() { alert('hello'); return; }",
         "function f() { alert('hello'); }");
  }

  @Test(timeout = 4000)
  public void testReturnWithValueNotEliminated() {
    test("function f() { return 42; }",
         "function f() { return 42; }");
  }

  @Test(timeout = 4000)
  public void testUselessContinueInWhileLoop() {
    test("while (x) { continue; }",
         "while (x) {}");
  }

  @Test(timeout = 4000)
  public void testUselessContinueInForLoop() {
    test("for (var i = 0; i < 10; i++) { continue; }",
         "for (var i = 0; i < 10; i++) {}");
  }

  @Test(timeout = 4000)
  public void testUnconditionalReturnFollowedByFunctionDeclaration() {
    // Functions are hoisted; next sibling is FUNCTION node, so jump is removable
    test("function f() { return; function bar() {} }",
         "function f() { function bar() {} }");
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInUnreachableBranch() {
    // Variables inside dead branches must be redeclared to preserve hoisting
    test("function f() { if (false) { var x = 1; } }",
         "function f() { var x; }");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testDirectVisitOnRootNode() {
    Compiler compiler = new Compiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);
    Node root = new Node(Token.SCRIPT);
    // Root has parent == null, must return safely
    pass.visit(null, root, null);
    assertNull(root.getParent());
  }

  @Test(timeout = 4000)
  public void testDirectVisitOnFunctionAndScriptTokens() {
    Compiler compiler = new Compiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);
    Node parent = new Node(Token.BLOCK);
    Node funcNode = new Node(Token.FUNCTION);
    Node scriptNode = new Node(Token.SCRIPT);

    pass.visit(null, funcNode, parent);
    pass.visit(null, scriptNode, parent);
    assertNull(funcNode.getNext());
    assertNull(scriptNode.getNext());
  }

  @Test(timeout = 4000)
  public void testDirectVisitNodeNotInCurCfg() {
    Compiler compiler = new Compiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);
    Node root = compiler.parseTestCode("var a = 1;");
    NodeTraversal t = new NodeTraversal(compiler, pass);
    pass.enterScope(t);

    Node orphanNode = new Node(Token.EXPR_RESULT);
    // orphanNode is not in curCfg, visit should exit cleanly
    pass.visit(t, orphanNode, root);
    pass.exitScope(t);
  }

  @Test(timeout = 4000)
  public void testEmptyStatementHandling() {
    // Semicolon alone represents Token.EMPTY; must not throw or remove destructively
    test("function f() { return; ; }",
         "function f() { ; }");
  }

  @Test(timeout = 4000)
  public void testEmptyBlockHandling() {
    test("function f() { return; {} }",
         "function f() { {} }");
  }

  @Test(timeout = 4000)
  public void testUnreachableDoLoopPreserved() {
    // Unreachable DO statements are messy and should not be removed
    test("function f() { return; do { var x = 1; } while (false); }",
         "function f() { return; do { var x = 1; } while (false); }");
  }

  @Test(timeout = 4000)
  public void testTryCatchBlockContainerPreserved() {
    test("try { alert(1); } catch (e) { alert(e); }",
         "try { alert(1); } catch (e) { alert(e); }");
  }

  @Test(timeout = 4000)
  public void testNoOpStatementsDisabled() {
    // With removeNoOpStatements = false, side-effect-free statements must be kept
    test("var x; x; 'literal'; 1 + 2;",
         "var x; x; 'literal'; 1 + 2;",
         false);
  }

  @Test(timeout = 4000)
  public void testNoOpStatementsEnabled() {
    // With removeNoOpStatements = true, side-effect-free expressions are removed
    test("var x; x; 'literal'; 1 + 2;",
         "var x;",
         true);
  }

  @Test(timeout = 4000)
  public void testSideEffectsRetainedEvenWhenNoOpEnabled() {
    test("var x = 0; x++; foo();",
         "var x = 0; x++; foo();",
         true);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 311 & Cascaded Jumps)
  // =========================================================================

  /**
   * Targets ground-truth defect: Closure Issue 311
   * Switch statement containing conditional return followed by unconditional break
   * previously caused an INTERNAL COMPILER ERROR.
   */
  @Test(timeout = 4000)
  public void testIssue311() {
    test("function a(b) {\n" +
         "  switch (b.v) {\n" +
         "    case 'SWITCH':\n" +
         "      if (b.i >= 0) {\n" +
         "        return (a.y = -1);\n" +
         "      }\n" +
         "      break;\n" +
         "  }\n" +
         "}",
         "function a(b) {\n" +
         "  switch (b.v) {\n" +
         "    case 'SWITCH':\n" +
         "      if (b.i >= 0) {\n" +
         "        return (a.y = -1);\n" +
         "      }\n" +
         "  }\n" +
         "}");
  }

  /**
   * Targets ground-truth defect: testCascadedRemovalOfUnlessUnconditonalJumps
   * Useless break jump in the final switch case should be eliminated, cascading
   * if multiple cases lead directly to exit.
   */
  @Test(timeout = 4000)
  public void testCascadedRemovalOfUnlessUnconditonalJumps() {
    test("switch (a) { case 'a': break; case 'b': break; }",
         "switch (a) { case 'a': break; case 'b': }");
  }

  @Test(timeout = 4000)
  public void testSingleCaseUselessBreakInSwitch() {
    test("switch (x) { case 1: break; }",
         "switch (x) { case 1: }");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testFineLoggerLoggingPath() {
    Logger logger = Logger.getLogger(UnreachableCodeElimination.class.getName());
    Level originalLevel = logger.getLevel();
    try {
      logger.setLevel(Level.FINE);
      // Triggers removeDeadExprStatementSafely logger.fine path
      test("function f() { return; alert(1); }",
           "function f() {}");
    } finally {
      logger.setLevel(originalLevel);
    }
  }

  @Test(timeout = 4000)
  public void testTryFinallyStructureIntegrity() {
    test("function f() { try { return 1; } finally { return 2; } }",
         "function f() { try { return 1; } finally { return 2; } }");
  }

  @Test(timeout = 4000)
  public void testBranchingNodeWithFollowingStatementsNotRemoved() {
    // Incomplete unconditional jump check: break followed by another active statement
    test("function f(x) { switch (x) { case 1: if (x) { break; } alert(2); } }",
         "function f(x) { switch (x) { case 1: if (x) { break; } alert(2); } }");
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Stack Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testNestedFunctionScopeStackTransitions() {
    // Tests enterScope and exitScope across multiple nested function levels
    test("function outer() {\n" +
         "  function middle() {\n" +
         "    function inner() {\n" +
         "      return;\n" +
         "    }\n" +
         "    return;\n" +
         "  }\n" +
         "  return;\n" +
         "}",
         "function outer() {\n" +
         "  function middle() {\n" +
         "    function inner() {}\n" +
         "  }\n" +
         "}");
  }

  @Test(timeout = 4000)
  public void testScopeStackPushPopIntegrity() {
    Compiler compiler = new Compiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);
    assertNull(pass.curCfg);
    assertTrue(pass.cfgStack.isEmpty());

    Node root = compiler.parseTestCode("function a() { function b() {} }");
    NodeTraversal t = new NodeTraversal(compiler, pass);

    pass.enterScope(t);
    assertNotNull(pass.curCfg);
    assertEquals(1, pass.cfgStack.size());

    pass.enterScope(t);
    assertEquals(2, pass.cfgStack.size());

    pass.exitScope(t);
    assertEquals(1, pass.cfgStack.size());

    pass.exitScope(t);
    assertNull(pass.curCfg);
    assertTrue(pass.cfgStack.isEmpty());
  }
}