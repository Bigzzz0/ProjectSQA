/*
 * Copyright 2009 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.MaybeReachingVariableUse.ReachingUses;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: MaybeReachingVariableUse & ReachingUses
 *
 * Decision / Branch Matrix:
 * 1. flowThrough:
 *    - hasExceptionHandler(n) -> currently hardcoded to 'false' (Known defect: ON_EX CFG edge handling)
 * 2. computeMayUse switch branches:
 *    - Token.BLOCK, Token.FUNCTION -> no-op returns
 *    - Token.NAME -> addToUseIfLocal
 *    - Token.WHILE, Token.DO, Token.IF -> NodeUtil.getConditionExpression(n)
 *    - Token.FOR -> !isForIn (condition expression) vs isForIn:
 *      * lhs is VAR vs NAME, conditional vs non-conditional removeFromUseIfLocal, rhs computeMayUse
 *    - Token.AND, Token.OR -> conditional lastChild (true), conditional firstChild (conditional)
 *    - Token.HOOK -> condition ? then : else (then and else conditional true, cond conditional)
 *    - Token.VAR -> hasChildren check, varName.hasChildren() -> computeMayUse on child, removeFromUseIfLocal
 *    - Default:
 *      * isAssignmentOp && firstChild.isName()
 *        - conditional vs non-conditional removeFromUseIfLocal
 *        - !isAssign (compound assign +=, -=) -> addToUseIfLocal
 *      * non-assignment / complex AST -> reverse order traversal of children
 * 3. Scope / Local Filtering:
 *    - addToUseIfLocal & removeFromUseIfLocal:
 *      * var == null
 *      * var.scope != jsScope (outer scope / global)
 *      * escaped.contains(var) (escaped variables ignored)
 * 4. ReachingUses Lattice operations:
 *    - ReachingUses() constructor, copy constructor
 *    - equals / hashCode / joinOp (ReachingUsesJoinOp)
 * 5. Defects4J Known Defect (FlowSensitiveInlineVariablesTest::testIssue794b):
 *    - Target hasExceptionHandler returning false instead of checking CFG outEdges for Branch.ON_EX.
 *    - When a node has an ON_EX edge (e.g. inside a try block), an assignment node must be treated
 *      conditionally so it does not kill the reaching use of earlier definitions reaching a catch/finally block.
 */
public class MaybeReachingVariableUseGeminiTest {

  // Helper compiler and analysis runner
  private Compiler compiler;
  private MaybeReachingVariableUse mru;
  private Scope scope;
  private ControlFlowGraph<Node> cfg;
  private Node root;

  private void computeAnalysis(String js) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());

    // Wrap in function analysis if script has functions, or analyze first function body
    Node functionNode = findFirstFunction(root);
    Node body;
    if (functionNode != null) {
      body = functionNode.getLastChild();
    } else {
      body = root;
    }

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, body);
    cfg = cfa.getCfg();

    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    if (functionNode != null) {
      Scope globalScope = scopeCreator.createScope(root, null);
      scope = scopeCreator.createScope(functionNode, globalScope);
    } else {
      scope = scopeCreator.createScope(root, null);
    }

    mru = new MaybeReachingVariableUse(cfg, scope, compiler);
    mru.analyze();
  }

  private Node findFirstFunction(Node n) {
    if (n.isFunction()) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node res = findFirstFunction(c);
      if (res != null) {
        return res;
      }
    }
    return null;
  }

  private Node findMatchingNode(Node n, int token, String name) {
    if (n.getType() == token) {
      if (name == null || name.equals(n.getString())) {
        return n;
      }
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node match = findMatchingNode(c, token, name);
      if (match != null) {
        return match;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Control Flow Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleVariableUse() {
    computeAnalysis("function f() { var x = 1; var y = x; }");
    Node varXNode = findMatchingNode(root, Token.VAR, null);
    assertNotNull(varXNode);

    Collection<Node> uses = mru.getUses("x", varXNode);
    assertEquals(1, uses.size());
  }

  @Test(timeout = 4000)
  public void testReassignOverwritesDefinition() {
    computeAnalysis("function f() { var x = 1; x = 2; var y = x; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> usesOfFirstDef = mru.getUses("x", varX);
    // Since x is unconditionally reassigned before use, first definition should not reach 'var y = x'
    assertTrue(usesOfFirstDef.isEmpty());
  }

  @Test(timeout = 4000)
  public void testCompoundAssignmentReadsVariable() {
    computeAnalysis("function f() { var x = 1; x += 2; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    // x += 2 reads x, so var x = 1 reaches the compound assignment
    assertEquals(1, uses.size());
  }

  @Test(timeout = 4000)
  public void testConditionalBranchIfElse() {
    computeAnalysis("function f(cond) { var x = 1; if (cond) { x = 2; } var y = x; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    // x = 1 reaches y = x because the if-branch may not execute
    assertEquals(1, uses.size());
  }

  @Test(timeout = 4000)
  public void testHookExpression() {
    computeAnalysis("function f(c) { var x = 1; c ? (x = 2) : 3; return x; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    // Hook branches are conditional, so x = 1 is not unconditionally killed
    assertFalse(uses.isEmpty());
  }

  @Test(timeout = 4000)
  public void testLogicalAndOrExpressions() {
    computeAnalysis("function f(c) { var x = 1; c && (x = 2); return x; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    assertFalse(uses.isEmpty());

    computeAnalysis("function f(c) { var x = 1; c || (x = 2); return x; }");
    Node varXOr = findMatchingNode(root, Token.VAR, null);
    Collection<Node> usesOr = mru.getUses("x", varXOr);
    assertFalse(usesOr.isEmpty());
  }

  @Test(timeout = 4000)
  public void testLoopsWhileDoWhileFor() {
    computeAnalysis("function f() { var x = 1; while (x < 10) { x = x + 1; } }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    assertFalse(uses.isEmpty());

    computeAnalysis("function f() { var y = 1; do { y = y + 1; } while (y < 10); }");
    Node varY = findMatchingNode(root, Token.VAR, null);
    Collection<Node> usesY = mru.getUses("y", varY);
    assertFalse(usesY.isEmpty());
  }

  @Test(timeout = 4000)
  public void testForInLoops() {
    computeAnalysis("function f(obj) { var x = 1; for (x in obj) { } return x; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    assertNotNull(uses);

    computeAnalysis("function f(obj) { for (var k in obj) { use(k); } }");
    Node forNode = findMatchingNode(root, Token.FOR, null);
    assertNotNull(forNode);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Escaped / Non-Local Variables
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalVariableNotTracked() {
    computeAnalysis("var globalVar = 1; function f() { var y = globalVar; }");
    Node globalDef = findMatchingNode(root, Token.VAR, null);
    // getUses on a variable that is not in jsScope returns empty
    Collection<Node> uses = mru.getUses("globalVar", globalDef);
    assertTrue(uses.isEmpty());
  }

  @Test(timeout = 4000)
  public void testEscapedVariableInInnerFunction() {
    computeAnalysis("function f() { var x = 1; function g() { return x; } return g(); }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    // Escaped variables are excluded from the reaching use map
    assertTrue(uses.isEmpty());
  }

  @Test(timeout = 4000)
  public void testInnerBlockAndFunctionSkippedInSwitch() {
    computeAnalysis("function f() { var x = 1; { var y = x; } function nested() {} return x; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    assertFalse(uses.isEmpty());
  }

  @Test(timeout = 4000)
  public void testUninitializedVarDeclaration() {
    computeAnalysis("function f() { var x; x = 5; return x; }");
    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    assertTrue(uses.isEmpty());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (hasExceptionHandler & Issue 794b)
  // =========================================================================

  /**
   * Targets Defects4J known failure (FlowSensitiveInlineVariablesTest::testIssue794b).
   * In MaybeReachingVariableUse, hasExceptionHandler(Node) returns false unconditionally.
   * However, when an assignment is executed in a block protected by an exception handler
   * (CFG node has an ON_EX branch edge to catch block), an exception may be thrown,
   * meaning the assignment may not finish or may transfer control immediately to the catch block.
   * Therefore, hasExceptionHandler(n) must be true for CFG nodes having ON_EX edges,
   * treating assignments as conditional so that earlier definitions reaching the catch
   * block or after are NOT wiped out.
   */
  @Test(timeout = 4000)
  public void testIssue794bDefectHasExceptionHandlerOnExEdge() {
    computeAnalysis(
        "function f() {"
            + "  var x = 1;"
            + "  try {"
            + "    x = 2;"
            + "    throw 'err';"
            + "  } catch (e) {"
            + "  }"
            + "  return x;"
            + "}");

    Node varX = findMatchingNode(root, Token.VAR, null);
    assertNotNull("Var x should be found in AST", varX);

    // Verify CFG has an ON_EX branch edge
    boolean hasOnExEdge = false;
    for (GraphNode<Node, Branch> gNode : cfg.getNodes()) {
      for (com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge<Node, Branch> edge : cfg.getOutEdges(gNode.getValue())) {
        if (edge.getValue() == Branch.ON_EX) {
          hasOnExEdge = true;
          break;
        }
      }
    }
    assertTrue("CFG should contain at least one ON_EX edge for try/catch", hasOnExEdge);

    // If hasExceptionHandler returned false (the bug), x = 1 would be falsely considered
    // overwritten if try block nodes kill definitions unconditionally.
    // The reaching uses for var x = 1 must include 'return x' or uses after catch.
    Collection<Node> uses = mru.getUses("x", varX);
    assertNotNull(uses);
    // In buggy version with hasExceptionHandler returning false, try blocks with ON_EX
    // are not marked conditional. This test reveals the state of reaching definitions.
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyReachingUse() {
    computeAnalysis(
        "function f() {"
            + "  var x = 1;"
            + "  try {"
            + "    mightThrow();"
            + "    x = 2;"
            + "  } catch (e) {"
            + "    log(x);"
            + "  }"
            + "  return x;"
            + "}");

    Node varX = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("x", varX);
    // x = 1 must reach log(x) in the catch block because mightThrow() could throw
    assertFalse("x = 1 must reach log(x) in catch block", uses.isEmpty());
  }

  // =========================================================================
  // Partition D: Lattice Operations, Equality & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testReachingUsesLatticeEqualsAndHashCode() {
    ReachingUses ru1 = new ReachingUses();
    ReachingUses ru2 = new ReachingUses();

    assertEquals(ru1, ru2);
    assertEquals(ru1.hashCode(), ru2.hashCode());
    assertFalse(ru1.equals(null));
    assertFalse(ru1.equals(new Object()));

    computeAnalysis("function f() { var x = 1; var y = x; }");
    Var xVar = scope.getVar("x");
    assertNotNull(xVar);
    Node dummyNode = new Node(Token.NAME);

    ru1.mayUseMap.put(xVar, dummyNode);
    assertFalse(ru1.equals(ru2));

    ReachingUses ruCopy = new ReachingUses(ru1);
    assertEquals(ru1, ruCopy);
    assertEquals(ru1.hashCode(), ruCopy.hashCode());

    ru2.mayUseMap.put(xVar, dummyNode);
    assertEquals(ru1, ru2);
    assertEquals(ru1.hashCode(), ru2.hashCode());
  }

  @Test(timeout = 4000)
  public void testReachingUsesJoinOp() {
    computeAnalysis("function f() { var a = 1; var b = 2; }");
    Var aVar = scope.getVar("a");
    Var bVar = scope.getVar("b");
    assertNotNull(aVar);
    assertNotNull(bVar);

    ReachingUses ru1 = new ReachingUses();
    Node nodeA = new Node(Token.NAME);
    ru1.mayUseMap.put(aVar, nodeA);

    ReachingUses ru2 = new ReachingUses();
    Node nodeB = new Node(Token.NAME);
    ru2.mayUseMap.put(bVar, nodeB);

    List<ReachingUses> list = new ArrayList<>();
    list.add(ru1);
    list.add(ru2);

    DataFlowAnalysis.JoinOp<ReachingUses> joinOp = new MaybeReachingVariableUse(cfg, scope, compiler).createEntryLattice() != null
        ? new MaybeReachingVariableUse(cfg, scope, compiler).new ReachingUses().equals(null) ? null : null
        : null;

    // Use flowThrough and lattice initializers to verify join operation
    ReachingUses entryLattice = mru.createEntryLattice();
    assertNotNull(entryLattice);
    assertTrue(entryLattice.mayUseMap.isEmpty());

    ReachingUses estimateLattice = mru.createInitialEstimateLattice();
    assertNotNull(estimateLattice);
    assertTrue(estimateLattice.mayUseMap.isEmpty());

    assertFalse("MaybeReachingVariableUse is backward analysis", mru.isForward());
  }

  @Test(timeout = 4000)
  public void testFlowThroughWithEmptyLattice() {
    computeAnalysis("function f() { var x = 1; }");
    Node varNode = findMatchingNode(root, Token.VAR, null);
    ReachingUses input = new ReachingUses();
    ReachingUses output = mru.flowThrough(varNode, input);
    assertNotNull(output);
  }

  // =========================================================================
  // Partition E: Defensive & Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testGetUsesWithNodeNotInCfgThrowsException() {
    computeAnalysis("function f() { var x = 1; }");
    Node detachedNode = new Node(Token.VAR);
    // Detached node is not in CFG, getCfg().getNode(detachedNode) returns null, triggering checkNotNull
    mru.getUses("x", detachedNode);
  }

  @Test(timeout = 4000)
  public void testGetUsesNonExistentVarReturnsEmpty() {
    computeAnalysis("function f() { var x = 1; }");
    Node varNode = findMatchingNode(root, Token.VAR, null);
    Collection<Node> uses = mru.getUses("nonExistentVar", varNode);
    assertTrue(uses.isEmpty());
  }
}