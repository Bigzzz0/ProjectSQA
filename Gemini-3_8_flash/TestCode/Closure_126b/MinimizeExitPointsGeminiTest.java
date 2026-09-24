package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.MinimizeExitPoints
 *
 * Decision / Condition Coverage Targets:
 * 1. visit(NodeTraversal, Node, Node):
 *    - Token.LABEL: minimize exits with Token.BREAK and labelName.
 *    - Token.FOR / Token.WHILE: minimize exits with Token.CONTINUE, labelName=null.
 *    - Token.DO: minimize exits with Token.CONTINUE, and if condition evaluates to TernaryValue.FALSE,
 *                minimize Token.BREAK on n.getFirstChild().
 *    - Token.FUNCTION: minimize exits with Token.RETURN on n.getLastChild().
 *    - Default/other tokens: ignored.
 *
 * 2. tryMinimizeExits(Node n, int exitType, String labelName):
 *    - matchingExitNode(n, exitType, labelName) -> removes node from parent, reports code change.
 *    - n.isIf() -> traverses trueBlock and elseBlock (if present).
 *    - n.isTry() -> traverses tryBlock, catchCodeBlock (if present), and finallyBlock (DEFECT ZONE).
 *    - n.isLabel() -> traverses labelBlock.
 *    - Non-block or empty block check (!n.isBlock() || n.getLastChild() == null) -> returns immediately.
 *    - Loop over children for c.isIf() -> calls tryMinimizeIfBlockExits for trueBlock and falseBlock.
 *    - Trailing exit node loop: examines n.getLastChild() repeatedly until no change.
 *
 * 3. tryMinimizeIfBlockExits(Node srcBlock, Node destBlock, Node ifNode, int exitType, String labelName):
 *    - srcBlock is block vs single statement.
 *    - srcBlock empty block bail.
 *    - matchingExitNode verification bail.
 *    - ifNode.getNext() != null: sibling movement into destBlock.
 *      * destBlock == null (false block creation).
 *      * destBlock.isEmpty() (replace with new block).
 *      * destBlock.isBlock() (reuse block).
 *      * destBlock is single statement (wrap in new block).
 *
 * 4. matchingExitNode(Node n, int type, String labelName):
 *    - type == Token.RETURN: !n.hasChildren() (only valueless returns match).
 *    - other types:
 *      * labelName == null: !n.hasChildren().
 *      * labelName != null: n.hasChildren() && labelName.equals(n.getFirstChild().getString()).
 *
 * 5. moveAllFollowing(Node start, Node srcParent, Node destParent):
 *    - moves sibling nodes: function declarations to front, other statements to back.
 *
 * [Defects4J Ground Truth Targets]:
 * - Closure Defect: MinimizeExitPoints erroneously optimizes/removes exit points inside `finally` blocks
 *   (e.g., in `try ... finally { return; }` or labeled `try ... finally { break label; }`), violating
 *   ECMA-262 completion specification.
 * - Targeted tests:
 *   * testDontRemoveBreakInTryFinally: ensures `break` inside finally block of a labeled try is preserved.
 *   * testFunctionReturnOptimization: ensures `return` inside finally block of a function is preserved.
 */
public class MinimizeExitPointsGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node parseAndProcess(String js) {
    Node root = compiler.parseTestCode(js);
    MinimizeExitPoints pass = new MinimizeExitPoints(compiler);
    pass.process(null, root);
    return root;
  }

  private int countNodesOfToken(Node root, int tokenType) {
    int count = (root.getType() == tokenType) ? 1 : 0;
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      count += countNodesOfToken(child, tokenType);
    }
    return count;
  }

  private Node findFirstNode(Node root, int tokenType) {
    if (root.getType() == tokenType) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstNode(child, tokenType);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J known failure: testDontRemoveBreakInTryFinally.
   * A break statement within a finally block changes the completion type of the try/finally
   * construct and must NOT be removed by tryMinimizeExits.
   */
  @Test(timeout = 4000)
  public void testDontRemoveBreakInTryFinally() {
    String js = "g: try { throw 1; } finally { break g; }";
    Node root = parseAndProcess(js);

    Node tryNode = findFirstNode(root, Token.TRY);
    assertNotNull("TRY block must be present", tryNode);
    assertTrue("TRY node must have finally block", NodeUtil.hasFinally(tryNode));

    Node finallyBlock = tryNode.getLastChild();
    assertEquals("Finally block must retain the BREAK statement", 1, countNodesOfToken(finallyBlock, Token.BREAK));
  }

  /**
   * Targets Defects4J known failure: testFunctionReturnOptimization.
   * A return statement in a finally block overrides any exception or return in the try block
   * and must NOT be minimized/removed.
   */
  @Test(timeout = 4000)
  public void testFunctionReturnOptimization() {
    String js = "function f() { try { throw 1; } finally { return; } }";
    Node root = parseAndProcess(js);

    Node tryNode = findFirstNode(root, Token.TRY);
    assertNotNull("TRY block must be present", tryNode);
    assertTrue("TRY node must have finally block", NodeUtil.hasFinally(tryNode));

    Node finallyBlock = tryNode.getLastChild();
    assertEquals("Finally block must retain the RETURN statement", 1, countNodesOfToken(finallyBlock, Token.RETURN));
  }

  /**
   * Ensures that return in finally is preserved even when try block also has a return.
   */
  @Test(timeout = 4000)
  public void testPreserveReturnInFinallyWithTryReturn() {
    String js = "function f() { try { return; } finally { return; } }";
    Node root = parseAndProcess(js);

    Node tryNode = findFirstNode(root, Token.TRY);
    assertNotNull(tryNode);
    Node finallyBlock = tryNode.getLastChild();
    assertEquals("Finally block must NOT have its return removed", 1, countNodesOfToken(finallyBlock, Token.RETURN));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testFunctionSimpleReturnRemoval() {
    String js = "function f() { a(); return; }";
    Node root = parseAndProcess(js);

    assertEquals("Redundant trailing return must be removed", 0, countNodesOfToken(root, Token.RETURN));
  }

  @Test(timeout = 4000)
  public void testFunctionReturnWithValuePreserved() {
    String js = "function f() { return 1; }";
    Node root = parseAndProcess(js);

    assertEquals("Return with value must not be removed", 1, countNodesOfToken(root, Token.RETURN));
  }

  @Test