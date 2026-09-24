package com.google.javascript.jscomp;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/* [Branch & Defect Analysis Matrix]
 * Target Class: DeadAssignmentsElimination (Closure Compiler / Defects4J Closure-88)
 *
 * Decision / Condition Coverage Targets:
 * 1. Scope checks:
 *    - scope.isGlobal() -> early return
 *    - NodeUtil.containsFunction(fnBlock) -> inner functions cause locals to escape -> early return
 *    - !NodeUtil.has(fnBlock, matchRemovableAssigns) -> no removable assignments -> early return
 * 2. matchRemovableAssigns predicate:
 *    - NodeUtil.isAssignmentOp(n) && LHS == Token.NAME
 *    - Token.INC / Token.DEC
 * 3. CFG Node processing branches (switch n.getType()):
 *    - Token.IF, Token.WHILE, Token.DO -> condition expression checked
 *    - Token.FOR -> !isForIn -> condition expression checked
 *    - Token.SWITCH, Token.CASE, Token.RETURN -> first child checked
 *    - Default statement node
 * 4. Local vs Non-local assignments:
 *    - LHS not a name -> skip
 *    - LHS name not declared in local scope -> skip
 *    - Escaped locals -> skip
 * 5. Identity assignment (a = a):
 *    - RHS is name && RHS name equals LHS name && isAssign(n) -> replace with RHS
 * 6. Liveness evaluation & Expression sub-tree traversal:
 *    - state.getOut().isLive(var) -> not dead, keep
 *    - isVariableStillLiveWithinExpression: checks read before kill within RHS / sibling nodes
 * 7. Replacement transformations:
 *    - NodeUtil.isAssign -> replace parent with RHS
 *    - NodeUtil.isAssignmentOp (+=, -=, etc.) -> replace with binary op
 *    - Token.INC / Token.DEC in ExpressionNode -> replace with (void 0)
 *    - Token.INC / Token.DEC in comma or for update -> replace with Token.EMPTY
 * 8. Defect Targets (Closure-88 / Issue 384):
 *    - Short-circuit boolean AND/OR and HOOK (?) expressions where assignments occur in
 *      conditional branches: isVariableStillLiveWithinExpression incorrectly treats conditional
 *      evaluations / kills as unconditional when evaluating whether an assignment is live.
 *      Specifically tests:
 *      - testInExpression2: (a = 1) || (a = 2); return a
 *      - testIssue384b: (a = 1) && (b = a); return b
 *      - testIssue384c: (a = 1) || (b = a); return b
 *      - testIssue384d: (a = 1) ? (a = 2) : (a = 3); return a
 */
public class DeadAssignmentsEliminationGeminiTest extends CompilerTestCase {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @After
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  private void inFunction(String js) {
    testSame("function f() {" + js + "}");
  }

  private void inFunction(String js, String expected) {
    test("function f() {" + js + "}", "function f() {" + expected + "}");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleDeadAssignmentElimination() {
    inFunction("var x; x = 1;", "var x; 1;");
  }

  @Test(timeout = 4000)
  public void testMultipleDeadAssignmentsToSameVar() {
    inFunction("var a; a = 1; a = 2;", "var a; 1; 2;");
  }

  @Test(timeout = 4000)
  public void testLiveVariableAssignmentRetained() {
    inFunction("var x; x = 1; return x;");
  }

  @Test(timeout = 4000)
  public void testReadBetweenAssignmentsRetained() {
    inFunction("var x; x = 1; alert(x); x = 2; return x;");
  }

  @Test(timeout = 4000)
  public void testCompoundAssignmentOpsElimination() {
    inFunction("var x; x += 1;", "var x; x + 1;");
    inFunction("var x; x -= 2;", "var x; x - 2;");
    inFunction("var x; x *= 3;", "var x; x * 3;");
    inFunction("var x; x /= 4;", "var x; x / 4;");
  }

  @Test(timeout = 4000)
  public void testIncDecOpsElimination() {
    inFunction("var x; x++;", "var x; void 0;");
    inFunction("var x; x--;", "var x; void 0;");
    inFunction("var x; ++x;", "var x; void 0;");
    inFunction("var x; --x;", "var x; void 0;");
  }

  @Test(timeout = 4000)
  public void testIdentityAssignmentRemoval() {
    inFunction("var a; a = a;", "var a; a;");
    inFunction("var a = 1; a = a; return a;", "var a = 1; a; return a;");
  }

  @Test(timeout = 4000)
  public void testChainedDeadAssignments() {
    inFunction("var x, y; x = y = 1;", "var x, y; 1;");
    inFunction("var x, y; x = y = 1; return y;", "var x, y; y = 1; return y;");
  }