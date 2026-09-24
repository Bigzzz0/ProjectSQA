/*
 * Copyright 2008 Google Inc.
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

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT UNDER TEST (Defects4J Closure):
 *    - Missing Token.INSTANCEOF handling in ControlFlowAnalysis.mayThrowException(Node).
 *    - In JavaScript, `x instanceof y` can throw a TypeError if `y` is not callable/object.
 *      Without Token.INSTANCEOF in mayThrowException, no ON_EX edge is generated to
 *      enclosing TRY-CATCH/FINALLY handlers, causing incorrect dead code elimination
 *      and missing CFG exception transitions.
 *
 * 2. BRANCH & LOGIC TARGETS:
 *    - Control structures: IF-THEN, IF-THEN-ELSE, WHILE, DO-WHILE, FOR (4-clause), FOR-IN.
 *    - SWITCH, CASE, DEFAULT with multiple fall-throughs and trailing empty cases.
 *    - Synthetic blocks (Token.BLOCK marked as isSyntheticBlock) & SYN_BLOCK branch.
 *    - Exception handling: TRY-CATCH, TRY-FINALLY, TRY-CATCH-FINALLY, nested TRY structures.
 *    - Loop jumps: BREAK with and without labels; CONTINUE with and without labels.
 *    - Return statements escaping try blocks with finally handlers (filling finallyMap).
 *    - Function traversal flag: shouldTraverseFunctions = true vs false.
 *    - AST Node priority comparator: forward and reverse order.
 *    - Static structural predicates: isBreakStructure, isContinueStructure.
 */
public class ControlFlowAnalysisGeminiTest {

  private ControlFlowGraph<Node> buildCfg(String js, boolean traverseFunctions) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseSyntheticCode("testcode", js);
    assertNotNull("Synthetic parsing failed for: " + js, root);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, traverseFunctions);
    cfa.process(null, root);
    return cfa.getCfg();
  }

  private boolean hasEdge(ControlFlowGraph<Node> cfg, int fromType, int toType, Branch branch) {
    for (DiGraphEdge<Node, Branch> edge : cfg.getEdges()) {
      Node source = edge.getSource().getValue();
      Node dest = edge.getDestination().getValue();
      if (source != null && source.getType() == fromType &&
          dest != null && dest.getType() == toType &&
          edge.getValue() == branch) {
        return true;
      }
    }
    return false;
  }

  private boolean hasExceptionEdgeFrom(ControlFlowGraph<Node> cfg, int fromType) {
    for (DiGraphEdge<Node, Branch> edge : cfg.getEdges()) {
      Node source = edge.getSource().getValue();
      if (source != null && source.getType() == fromType && edge.getValue() == Branch.ON_EX) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Token.INSTANCEOF in mayThrowException)
  // =========================================================================

  @Test(timeout = 4000)
  public void testInstanceOfThrowsExceptionDefect() {
    String js = "try { x instanceof y; } catch (e) { handle(e); }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    // If mayThrowException properly recognizes INSTANCEOF, an ON_EX branch MUST exist
    // from EXPR_RESULT to the catch block / handler.
    boolean hasExBranch = hasExceptionEdgeFrom(cfg, Token.EXPR_RESULT);
    assertTrue("Defect failure: 'instanceof' expression must produce an ON_EX branch because it can throw TypeError",
        hasExBranch);
  }

  @Test(timeout = 4000)
  public void testInstanceOfInFinallyBlockDefect() {
    String js = "try { a instanceof B; } finally { cleanup(); }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);
    boolean hasExBranch = hasExceptionEdgeFrom(cfg, Token.EXPR_RESULT);
    assertTrue("'instanceof' within try-finally must create an ON_EX edge into the finally block",
        hasExBranch);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Control Flow Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testIfElseBranches() {
    String js = "if (a) { b(); } else { c(); }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("IF node should have ON_TRUE branch", hasEdge(cfg, Token.IF, Token.EXPR_RESULT, Branch.ON_TRUE));
    assertTrue("IF node should have ON_FALSE branch", hasEdge(cfg, Token.IF, Token.EXPR_RESULT, Branch.ON_FALSE));
  }

  @Test(timeout = 4000)
  public void testIfWithoutElseBranch() {
    String js = "if (a) { b(); } next();";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("IF without else should branch ON_FALSE to next statement",
        hasEdge(cfg, Token.IF, Token.EXPR_RESULT, Branch.ON_FALSE));
  }

  @Test(timeout = 4000)
  public void testWhileLoop() {
    String js = "while (x > 0) { x--; }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("WHILE condition should branch ON_TRUE to body",
        hasEdge(cfg, Token.WHILE, Token.EXPR_RESULT, Branch.ON_TRUE));
    // The follow of while loop when false is implicit return (null node value)
    boolean hasFalseBranch = false;
    for (DiGraphEdge<Node, Branch> edge : cfg.getEdges()) {
      if (edge.getSource().getValue() != null &&
          edge.getSource().getValue().getType() == Token.WHILE &&
          edge.getValue() == Branch.ON_FALSE) {
        hasFalseBranch = true;
        break;
      }
    }
    assertTrue("WHILE must have ON_FALSE exit edge", hasFalseBranch);
  }

  @Test(timeout = 4000)
  public void testDoWhileLoop() {
    String js = "do { foo(); } while (cond);";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("DO should branch ON_TRUE back to statement",
        hasEdge(cfg, Token.DO, Token.EXPR_RESULT, Branch.ON_TRUE));
  }

  @Test(timeout = 4000)
  public void testForLoopStandard() {
    String js = "for (var i = 0; i < 10; i++) { doWork(i); }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("VAR init branches UNCOND to FOR condition check",
        hasEdge(cfg, Token.VAR, Token.FOR, Branch.UNCOND));
    assertTrue("FOR node branches ON_TRUE to loop body",
        hasEdge(cfg, Token.FOR, Token.EXPR_RESULT, Branch.ON_TRUE));
  }

  @Test(timeout = 4000)
  public void testForInLoop() {
    String js = "for (var k in obj) { process(k); }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("FOR-IN node branches ON_TRUE to body",
        hasEdge(cfg, Token.FOR, Token.EXPR_RESULT, Branch.ON_TRUE));
  }

  @Test(timeout = 4000)
  public void testSwitchWithCasesAndDefault() {
    String js = "switch (val) { case 1: one(); break; case 2: two(); break; default: def(); }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("SWITCH unconditionally branches to first case",
        hasEdge(cfg, Token.SWITCH, Token.CASE, Branch.UNCOND));
    assertTrue("CASE 1 condition branches ON_TRUE to statement",
        hasEdge(cfg, Token.CASE, Token.EXPR_RESULT, Branch.ON_TRUE));
    assertTrue("CASE 1 branches ON_FALSE to CASE 2",
        hasEdge(cfg, Token.CASE, Token.CASE, Branch.ON_FALSE));
    assertTrue("CASE 2 branches ON_FALSE to DEFAULT",
        hasEdge(cfg, Token.CASE, Token.DEFAULT, Branch.ON_FALSE));
    assertTrue("DEFAULT unconditionally branches to its statement",
        hasEdge(cfg, Token.DEFAULT, Token.EXPR_RESULT, Branch.UNCOND));
  }

  @Test(timeout = 4000)
  public void testSwitchEmpty() {
    String js = "switch (val) {} end();";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("Empty SWITCH unconditionally branches to follow statement",
        hasEdge(cfg, Token.SWITCH, Token.EXPR_RESULT, Branch.UNCOND));
  }

  @Test(timeout = 4000)
  public void testWithStatement() {
    String js = "with (o) { bar(); }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("WITH unconditionally branches to body",
        hasEdge(cfg, Token.WITH, Token.EXPR_RESULT, Branch.UNCOND));
  }

  // =========================================================================
  // Partition B: Break, Continue, Labeled Jumps & Finally Interactions
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnlabeledBreakAndContinueInWhile() {
    String js = "while (c) { if (b) { break; } else { continue; } }";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("CONTINUE unconditionally loops back to WHILE node",
        hasEdge(cfg, Token.CONTINUE, Token.WHILE, Branch.UNCOND));
  }

  @Test(timeout = 4000)
  public void testLabeledBreakAndContinueInFor() {
    String js = "outer: for (var i = 0; i < 5; i++) {" +
                "  inner: for (var j = 0; j < 5; j++) {" +
                "    if (i == j) continue outer;" +
                "    if (i > j) break outer;" +
                "  }" +
                "}";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    // Labeled continue jumps to the increment (iter) node of the outer loop
    assertTrue("Labeled continue routes to outer loop increment",
        hasEdge(cfg, Token.CONTINUE, Token.INC, Branch.UNCOND));
  }

  @Test(timeout = 4000)
  public void testBreakInsideTryWithFinally() {
    String js = "while (true) {" +
                "  try {" +
                "    break;" +
                "  } finally {" +
                "    cleanup();" +
                "  }" +
                "}";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("BREAK inside try branches unconditionally to finally statement",
        hasEdge(cfg, Token.BREAK, Token.EXPR_RESULT, Branch.UNCOND));
  }

  @Test(timeout = 4000)
  public void testContinueInsideTryWithFinally() {
    String js = "while (true) {" +
                "  try {" +
                "    continue;" +
                "  } finally {" +
                "    step();" +
                "  }" +
                "}";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("CONTINUE inside try branches unconditionally to finally block",
        hasEdge(cfg, Token.CONTINUE, Token.BLOCK, Branch.UNCOND));
  }

  @Test(timeout = 4000)
  public void testReturnEscapingTryWithFinally() {
    String js = "function test() {" +
                "  try {" +
                "    return 1;" +
                "  } finally {" +
                "    clean();" +
                "  }" +
                "}";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("RETURN inside try branches unconditionally to finally block",
        hasEdge(cfg, Token.RETURN, Token.BLOCK, Branch.UNCOND));
  }

  @Test(timeout = 4000)
  public void testNestedTryCatchFinally() {
    String js = "try {" +
                "  try {" +
                "    throw 'err';" +
                "  } catch (e1) {" +
                "    foo();" +
                "  } finally {" +
                "    bar();" +
                "  }" +
                "} catch (e2) {" +
                "  baz();" +
                "}";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertTrue("THROW connects to inner CATCH",
        hasEdge(cfg, Token.THROW, Token.BLOCK, Branch.ON_EX));
  }

  // =========================================================================
  // Partition D: Function Traversal & Priority Sorting
  // =========================================================================

  @Test(timeout = 4000)
  public void testTraverseFunctionsEnabled() {
    String js = "function f() { var x = 10; return x; } f();";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    assertNotNull("CFG must be created", cfg);
    boolean hasVarInsideFunc = false;
    for (DiGraphNode<Node, Branch> node : cfg.getDirectedGraphNodes()) {
      if (node.getValue() != null && node.getValue().getType() == Token.VAR) {
        hasVarInsideFunc = true;
        break;
      }
    }
    assertTrue("Inner function nodes should be traversed when shouldTraverseFunctions=true",
        hasVarInsideFunc);
  }

  @Test(timeout = 4000)
  public void testTraverseFunctionsDisabled() {
    String js = "function f() { var x = 10; } next();";
    ControlFlowGraph<Node> cfg = buildCfg(js, false);

    assertNotNull("CFG must be created", cfg);
    boolean hasVarInsideFunc = false;
    for (DiGraphNode<Node, Branch> node : cfg.getDirectedGraphNodes()) {
      if (node.getValue() != null && node.getValue().getType() == Token.VAR) {
        hasVarInsideFunc = true;
        break;
      }
    }
    assertFalse("Inner function bodies should not be traversed when shouldTraverseFunctions=false",
        hasVarInsideFunc);
  }

  @Test(timeout = 4000)
  public void testCfgOptionalNodeComparator() {
    String js = "var a = 1; var b = 2; var c = 3;";
    ControlFlowGraph<Node> cfg = buildCfg(js, true);

    Comparator<DiGraphNode<Node, Branch>> forwardComp = cfg.getOptionalNodeComparator(true);
    Comparator<DiGraphNode<Node, Branch>> reverseComp = cfg.getOptionalNodeComparator(false);
    assertNotNull("Forward comparator should not be null", forwardComp);
    assertNotNull("Reverse comparator should not be null", reverseComp);

    List<DiGraphNode<Node, Branch>> nodes = cfg.getDirectedGraphNodes();
    assertTrue("Should have multiple nodes", nodes.size() >= 2);

    DiGraphNode<Node, Branch> n1 = nodes.get(0);
    DiGraphNode<Node, Branch> n2 = nodes.get(1);

    int fwdDiff = forwardComp.compare(n1, n2);
    int revDiff = reverseComp.compare(n1, n2);
    assertEquals("Reverse comparator must yield opposite sign of forward",
        Integer.signum(fwdDiff), -Integer.signum(revDiff));
  }

  // =========================================================================
  // Partition E: Synthetic Blocks, Exceptions & Static Predicates
  // =========================================================================

  @Test(timeout = 4000)
  public void testSyntheticBlockHandling() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseSyntheticCode("syntheticTest", "var x = 1; var y = 2;");
    // Manually mark a block as synthetic to cover Branch.SYN_BLOCK
    Node synBlock = new Node(Token.BLOCK);
    synBlock.setIsSyntheticBlock(true);
    synBlock.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString("syn")));
    root.addChildToBack(synBlock);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    boolean hasSynEdge = false;
    for (DiGraphEdge<Node, Branch> edge : cfg.getEdges()) {
      if (edge.getValue() == Branch.SYN_BLOCK) {
        hasSynEdge = true;
        break;
      }
    }
    assertTrue("Synthetic block should register a SYN_BLOCK branch edge", hasSynEdge);
  }

  @Test(timeout = 4000)
  public void testStaticIsBreakStructure() {
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.FOR), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.DO), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.WHILE), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.SWITCH), false));

    assertFalse("Unlabeled BLOCK cannot be break target",
        ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), false));
    assertTrue("Labeled BLOCK can be break target",
        ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), true));
    assertTrue("Labeled IF can be break target",
        ControlFlowAnalysis.isBreakStructure(new Node(Token.IF), true));
    assertTrue("Labeled TRY can be break target",
        ControlFlowAnalysis.isBreakStructure(new Node(Token.TRY), true));

    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.EXPR_RESULT), false));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.EXPR_RESULT), true));
  }

  @Test(timeout = 4000)
  public void testStaticIsContinueStructure() {
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.FOR)));
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.DO)));
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.WHILE)));

    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.SWITCH)));
    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.BLOCK)));
    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.IF)));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBreakWithInvalidTargetThrowsException() {
    // Parsing broken tree or break without loop/label target
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);
    Node breakNode = new Node(Token.BREAK);
    script.addChildToBack(breakNode);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
    cfa.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testContinueWithInvalidTargetThrowsException() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);
    Node continueNode = new Node(Token.CONTINUE);
    script.addChildToBack(continueNode);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
    cfa.process(null, script);
  }
}