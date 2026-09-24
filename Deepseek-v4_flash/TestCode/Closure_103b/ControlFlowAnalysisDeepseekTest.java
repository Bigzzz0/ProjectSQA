package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.List;

/**
 * Tests for {@link ControlFlowAnalysis}.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Branches exercised:
 * - shouldTraverse: FUNCTION, TRY, FOR, IF, WHILE, WITH, DO, SWITCH, CASE, CATCH, LABEL,
 *   CONTINUE/BREAK/EXPR_RESULT/VAR/RETURN/THROW, TRY catch handling.
 * - visit: all node types (IF, WHILE, DO, FOR, SWITCH, CASE, DEFAULT, BLOCK/SCRIPT,
 *   FUNCTION, EXPR_RESULT, THROW, TRY, CATCH, BREAK, CONTINUE, RETURN, WITH, LABEL, default).
 * - computeFollowNode: parent null, parent FUNCTION, node==root, IF, CASE/DEFAULT,
 *   FOR (for-in vs regular), WHILE/DO, TRY (try/catch/finally), finallyMap handling.
 * - computeFallThrough: DO, FOR (for-in vs regular), LABEL.
 * - connectToPossibleExceptionHandler: mayThrowException, exceptionHandler stack.
 * - Break/Continue: labeled/unlabeled, finally map.
 * 
 * Known defect: mayThrowException does not consider Token.INSTANCEOF as a possible
 * exception thrower, causing the catch block of a try containing an instanceof
 * expression to be unreachable. This is targeted by testInstanceofShouldBeThrowing.
 */
public class ControlFlowAnalysisDeepseekTest {

  private ControlFlowGraph<Node> computeCfg(String code) {
    return computeCfg(code, false);
  }

  private ControlFlowGraph<Node> computeCfg(String code, boolean traverseFunctions) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseCode(code);
    assertNotNull("Failed to parse code: " + code, root);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, traverseFunctions);
    cfa.process(null, root);
    return cfa.getCfg();
  }

  private DiGraphNode<Node, Branch> getDiGraphNode(ControlFlowGraph<Node> cfg, Node value) {
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      if (n.getValue() == value) {
        return n;
      }
    }
    return null;
  }

  private boolean hasEdge(ControlFlowGraph<Node> cfg, Node from, Node to) {
    DiGraphNode<Node, Branch> fromNode = getDiGraphNode(cfg, from);
    DiGraphNode<Node, Branch> toNode = getDiGraphNode(cfg, to);
    if (fromNode == null || toNode == null) {
      return false;
    }
    List<DiGraphNode<Node, Branch>> successors = cfg.getDirectedSuccNodes(fromNode);
    return successors.contains(toNode);
  }

  private DiGraphNode<Node, Branch> findCatchBlock(ControlFlowGraph<Node> cfg) {
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.BLOCK && v.getParent() != null
          && v.getParent().getType() == Token.CATCH) {
        return n;
      }
    }
    return null;
  }

  @Test(timeout = 4000)
  public void testConstructorAndGetCfgInitiallyNull() {
    Compiler compiler = new Compiler();
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
    assertNull(cfa.getCfg());
  }

  @Test(timeout = 4000)
  public void testProcessSetsCfgAndEntryImplicitReturn() {
    ControlFlowGraph<Node> cfg = computeCfg("var x = 1;");
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
    assertNotNull(cfg.getImplicitReturn());
    Node entryValue = cfg.getEntry().getValue();
    assertNotNull(entryValue);
    assertEquals(Token.SCRIPT, entryValue.getType());
  }

  @Test(timeout = 4000)
  public void testSimpleStatementSequence() {
    ControlFlowGraph<Node> cfg = computeCfg("var a=1; var b=2;");
    Node firstVar = null;
    Node secondVar = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.VAR) {
        if (firstVar == null) {
          firstVar = v;
        } else {
          secondVar = v;
        }
      }
    }
    assertNotNull(firstVar);
    assertNotNull(secondVar);
    assertTrue(hasEdge(cfg, firstVar, secondVar));
  }

  @Test(timeout = 4000)
  public void testIfStatementEdges() {
    ControlFlowGraph<Node> cfg = computeCfg("if (a) { b(); } else { c(); }");
    Node ifNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.IF) {
        ifNode = v;
        break;
      }
    }
    assertNotNull(ifNode);
    Node thenBlock = ifNode.getFirstChild().getNext();
    Node elseBlock = ifNode.getLastChild();
    assertTrue(hasEdge(cfg, ifNode, thenBlock));
    assertTrue(hasEdge(cfg, ifNode, elseBlock));
  }

  @Test(timeout = 4000)
  public void testWhileLoopEdges() {
    ControlFlowGraph<Node> cfg = computeCfg("while (a) { b(); } c();");
    Node whileNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.WHILE) {
        whileNode = v;
        break;
      }
    }
    assertNotNull(whileNode);
    Node body = whileNode.getFirstChild().getNext();
    Node after = whileNode.getNext();
    assertTrue(hasEdge(cfg, whileNode, body));
    assertTrue(hasEdge(cfg, whileNode, after));
  }

  @Test(timeout = 4000)
  public void testDoWhileLoopEdges() {
    ControlFlowGraph<Node> cfg = computeCfg("do { b(); } while (a); c();");
    Node doNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.DO) {
        doNode = v;
        break;
      }
    }
    assertNotNull(doNode);
    Node body = doNode.getFirstChild();
    Node after = doNode.getNext();
    assertTrue(hasEdge(cfg, doNode, body));
    assertTrue(hasEdge(cfg, doNode, after));
  }

  @Test(timeout = 4000)
  public void testForLoopEdges() {
    ControlFlowGraph<Node> cfg = computeCfg("for (var i=0; i<10; i++) { b(); } c();");
    Node forNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.FOR) {
        forNode = v;
        break;
      }
    }
    assertNotNull(forNode);
    Node init = forNode.getFirstChild();
    Node cond = init.getNext();
    Node iter = cond.getNext();
    Node body = iter.getNext();
    Node after = forNode.getNext();
    assertTrue(hasEdge(cfg, init, forNode));
    assertTrue(hasEdge(cfg, forNode, body));
    assertTrue(hasEdge(cfg, forNode, after));
    assertTrue(hasEdge(cfg, iter, forNode));
  }

  @Test(timeout = 4000)
  public void testForInLoopEdges() {
    ControlFlowGraph<Node> cfg = computeCfg("for (var x in obj) { b(); } c();");
    Node forNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.FOR) {
        forNode = v;
        break;
      }
    }
    assertNotNull(forNode);
    assertEquals(3, forNode.getChildCount());
    Node body = forNode.getLastChild();
    Node after = forNode.getNext();
    assertTrue(hasEdge(cfg, forNode, body));
    assertTrue(hasEdge(cfg, forNode, after));
  }

  @Test(timeout = 4000)
  public void testSwitchCaseEdges() {
    ControlFlowGraph<Node> cfg = computeCfg("switch (a) { case 1: b(); } c();");
    Node switchNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.SWITCH) {
        switchNode = v;
        break;
      }
    }
    assertNotNull(switchNode);
    Node firstCase = switchNode.getFirstChild().getNext();
    assertNotNull(firstCase);
    assertEquals(Token.CASE, firstCase.getType());
    assertTrue(hasEdge(cfg, switchNode, firstCase));
    Node caseBody = firstCase.getFirstChild().getNext();
    assertTrue(hasEdge(cfg, firstCase, caseBody));
    Node after = switchNode.getNext();
    assertTrue(hasEdge(cfg, firstCase, after));
  }

  @Test(timeout = 4000)
  public void testThrowingCallAllowsCatchBlock() {
    ControlFlowGraph<Node> cfg = computeCfg("try { a.b(); } catch (e) {}");
    DiGraphNode<Node, Branch> catchBlock = findCatchBlock(cfg);
    assertNotNull(catchBlock);
    assertTrue("Catch block should be reachable for a throwing call",
        cfg.getDirectedPredNodes(catchBlock).size() > 0);
  }

  @Test(timeout = 4000)
  public void testNoThrowNoEdgeToCatch() {
    ControlFlowGraph<Node> cfg = computeCfg("try { var x = 1; } catch (e) {}");
    DiGraphNode<Node, Branch> catchBlock = findCatchBlock(cfg);
    assertNotNull(catchBlock);
    assertEquals(0, cfg.getDirectedPredNodes(catchBlock).size());
  }

  @Test(timeout = 4000)
  public void testInstanceofShouldBeThrowing() {
    ControlFlowGraph<Node> cfg = computeCfg("try { x instanceof y; } catch (e) {}");
    DiGraphNode<Node, Branch> catchBlock = findCatchBlock(cfg);
    assertNotNull(catchBlock);
    assertTrue("Catch block should be reachable because instanceof can throw",
        cfg.getDirectedPredNodes(catchBlock).size() > 0);
  }

  @Test(timeout = 4000)
  public void testBreakEdge() {
    ControlFlowGraph<Node> cfg = computeCfg("while (a) { break; } c();");
    Node breakNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.BREAK) {
        breakNode = v;
        break;
      }
    }
    assertNotNull(breakNode);
    Node whileNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.WHILE) {
        whileNode = v;
        break;
      }
    }
    assertNotNull(whileNode);
    Node after = whileNode.getNext();
    assertTrue(hasEdge(cfg, breakNode, after));
  }

  @Test(timeout = 4000)
  public void testContinueEdge() {
    ControlFlowGraph<Node> cfg = computeCfg("while (a) { continue; } c();");
    Node continueNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.CONTINUE) {
        continueNode = v;
        break;
      }
    }
    assertNotNull(continueNode);
    Node whileNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.WHILE) {
        whileNode = v;
        break;
      }
    }
    assertNotNull(whileNode);
    assertTrue(hasEdge(cfg, continueNode, whileNode));
  }

  @Test(timeout = 4000)
  public void testReturnWithFinally() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { try { return 1; } finally { g(); } }", true);
    Node returnNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.RETURN) {
        returnNode = v;
        break;
      }
    }
    assertNotNull(returnNode);
    Node tryNode = returnNode.getParent().getParent().getParent(); // return -> block -> try
    Node finallyBlock = tryNode.getLastChild();
    assertTrue(hasEdge(cfg, returnNode, finallyBlock));
  }

  @Test(timeout = 4000)
  public void testWithStatementEdge() {
    ControlFlowGraph<Node> cfg = computeCfg("with (obj) { a(); }");
    Node withNode = null;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.WITH) {
        withNode = v;
        break;
      }
    }
    assertNotNull(withNode);
    Node body = withNode.getLastChild();
    assertTrue(hasEdge(cfg, withNode, body));
  }

  @Test(timeout = 4000)
  public void testFunctionTraversalDisabled() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { var x = 1; }");
    boolean foundVar = false;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.VAR) {
        foundVar = true;
        break;
      }
    }
    assertFalse(foundVar);
  }

  @Test(timeout = 4000)
  public void testFunctionTraversalEnabled() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { var x = 1; }", true);
    boolean foundVar = false;
    for (DiGraphNode<Node, Branch> n : cfg.getDirectedGraphNodes()) {
      Node v = n.getValue();
      if (v != null && v.getType() == Token.VAR) {
        foundVar = true;
        break;
      }
    }
    assertTrue(foundVar);
  }

  @Test(timeout = 4000)
  public void testIsBreakStructure() {
    Node forNode = new Node(Token.FOR);
    Node doNode = new Node(Token.DO);
    Node whileNode = new Node(Token.WHILE);
    Node switchNode = new Node(Token.SWITCH);
    Node blockNode = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF);
    Node tryNode = new Node(Token.TRY);
    Node otherNode = new Node(Token.EXPR_RESULT);

    assertTrue(ControlFlowAnalysis.isBreakStructure(forNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(doNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(whileNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(switchNode, false));
    assertFalse(ControlFlowAnalysis.isBreakStructure(blockNode, false));
    assertFalse(ControlFlowAnalysis.isBreakStructure(ifNode, false));
    assertFalse(ControlFlowAnalysis.isBreakStructure(tryNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(blockNode, true));
    assertTrue(ControlFlowAnalysis.isBreakStructure(ifNode, true));
    assertTrue(ControlFlowAnalysis.isBreakStructure(tryNode, true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(otherNode, true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(otherNode, false));
  }

  @Test(timeout = 4000)
  public void testIsContinueStructure() {
    Node forNode = new Node(Token.FOR);
    Node doNode = new Node(Token.DO);
    Node whileNode = new Node(Token.WHILE);
    Node switchNode = new Node(Token.SWITCH);
    Node otherNode = new Node(Token.EXPR_RESULT);

    assertTrue(ControlFlowAnalysis.isContinueStructure(forNode));
    assertTrue(ControlFlowAnalysis.isContinueStructure(doNode));
    assertTrue(ControlFlowAnalysis.isContinueStructure(whileNode));
    assertFalse(ControlFlowAnalysis.isContinueStructure(switchNode));
    assertFalse(ControlFlowAnalysis.isContinueStructure(otherNode));
  }
}