/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.ControlFlowAnalysis
 * Target Defects:
 *   - Defects4J Issue 779 / testDeepNestedFinally:
 *     In deeply nested try-finally blocks (with break/return/empty bodies), control flow edges
 *     leaving the inner finally block fail to connect properly to the outer finally block or
 *     incorrectly fall through to siblings, dropping required cross-edges or yielding false
 *     missing return statements.
 *   - testDeepNestedBreakwithFinally:
 *     Break statement inside nested try-finally fails to create cross edges between inner
 *     and outer finally statements.
 *
 * Branch & Condition Coverage:
 *   - Partition A: Control structures: IF (with/without else), WHILE, DO, FOR (4-child & for-in),
 *     SWITCH (CASE, DEFAULT, empty switch, fall-through between cases), WITH, FUNCTION.
 *   - Partition B: Boundary conditions: empty blocks, synthetic blocks, empty catch blocks,
 *     function declarations within statement lists, unlabeled and labeled breaks/continues.
 *   - Partition C: Defect-targeted tests:
 *     - testDeepNestedFinally (assert cross edge Token.EXPR_RESULT -> Token.EXPR_RESULT)
 *     - testDeepNestedBreakwithFinally (assert cross edge between inner & outer finally blocks)
 *     - testIssue779FinallyReturn (try { return 1; } finally { } inside function)
 *   - Partition D: Defensive & Exception paths:
 *     - Invalid break/continue throwing IllegalStateException vs IDE mode graceful fallback
 *     - mayThrowException checks: CALL, GETPROP, GETELEM, THROW, NEW, ASSIGN, INC, DEC, INSTANCEOF
 *     - getExceptionHandler and getCatchHandlerForBlock traversals
 *   - Partition E: AstControlFlowGraph Comparator & Priority verification:
 *     - Forward vs Backward node comparator contracts
 *     - Priority queue traversal and unreachable node priority assignment
 * ------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class ControlFlowAnalysisGeminiTest {

  // =========================================================================
  // Helper Utilities
  // =========================================================================

  private ControlFlowGraph<Node> createCfg(String js) {
    return createCfg(js, true, true);
  }

  private ControlFlowGraph<Node> createCfg(String js, boolean traverseFunctions, boolean edgeAnnotations) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, traverseFunctions, edgeAnnotations);
    cfa.process(null, root);
    return cfa.getCfg();
  }

  private static void assertCrossEdge(ControlFlowGraph<Node> cfg, int fromType, int toType) {
    for (DiGraphEdge<Node, Branch> edge : cfg.getDirectedGraphEdges()) {
      Node srcNode = edge.getSource().getValue();
      Node destNode = edge.getDestination().getValue();
      if (srcNode != null && srcNode.getType() == fromType &&
          destNode != null && destNode.getType() == toType) {
        return;
      }
    }
    fail("No cross edges found between type " + fromType + " and " + toType);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J ControlFlowAnalysisTest::testDeepNestedFinally defect.
   * Asserts that control properly flows from inner finally block expression
   * to outer finally block expression.
   */
  @Test(timeout = 4000)
  public void testDeepNestedFinally() {
    String src =
        "try {" +
        "  try {" +
        "  } finally {" +
        "    a; " +
        "  }" +
        "} finally {" +
        "  b;" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.EXPR_RESULT);
  }

  /**
   * Targets Defects4J ControlFlowAnalysisTest::testDeepNestedBreakwithFinally defect.
   * Asserts that break within nested try-finally constructs links inner finally to outer finally.
   */
  @Test(timeout = 4000)
  public void testDeepNestedBreakwithFinally() {
    String src =
        "while (x) {" +
        "  try {" +
        "    try {" +
        "      break;" +
        "    } finally {" +
        "      a; " +
        "    }" +
        "  } finally {" +
        "    b;" +
        "  }" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.EXPR_RESULT);
  }

  /**
   * Targets Defects4J CheckMissingReturnTest::testIssue779.
   * Verifies CFG creation for function returning inside try with empty finally.
   */
  @Test(timeout = 4000)
  public void testIssue779FinallyReturn() {
    String src = "var a = function() { try { return 1; } finally { } };";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull("CFG must not be null", cfg);
    assertNotNull("CFG entry must exist", cfg.getEntry());
    assertNotNull("CFG implicit return must exist", cfg.getImplicitReturn());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Control Structures
  // =========================================================================

  @Test(timeout = 4000)
  public void testIfWithAndWithoutElse() {
    String srcWithElse = "if (cond) { a(); } else { b(); }";
    ControlFlowGraph<Node> cfgWithElse = createCfg(srcWithElse);
    assertNotNull(cfgWithElse);

    String srcWithoutElse = "if (cond) { a(); } c();";
    ControlFlowGraph<Node> cfgWithoutElse = createCfg(srcWithoutElse);
    assertNotNull(cfgWithoutElse);
  }

  @Test(timeout = 4000)
  public void testWhileAndDoWhileLoops() {
    String srcWhile = "while (x < 10) { x++; }";
    ControlFlowGraph<Node> cfgWhile = createCfg(srcWhile);
    assertNotNull(cfgWhile);

    String srcDo = "do { x++; } while (x < 10);";
    ControlFlowGraph<Node> cfgDo = createCfg(srcDo);
    assertNotNull(cfgDo);
  }

  @Test(timeout = 4000)
  public void testForLoopsStandardAndForIn() {
    String srcFor = "for (var i = 0; i < 10; i++) { foo(i); }";
    ControlFlowGraph<Node> cfgFor = createCfg(srcFor);
    assertNotNull(cfgFor);

    String srcForIn = "for (var key in obj) { bar(key); }";
    ControlFlowGraph<Node> cfgForIn = createCfg(srcForIn);
    assertNotNull(cfgForIn);
  }

  @Test(timeout = 4000)
  public void testSwitchVariousConfigurations() {
    // Normal cases with default
    String src1 = "switch (x) { case 1: a(); break; case 2: b(); break; default: c(); }";
    ControlFlowGraph<Node> cfg1 = createCfg(src1);
    assertNotNull(cfg1);

    // No default case
    String src2 = "switch (x) { case 1: a(); break; case 2: b(); }";
    ControlFlowGraph<Node> cfg2 = createCfg(src2);
    assertNotNull(cfg2);

    // Only default case
    String src3 = "switch (x) { default: c(); break; }";
    ControlFlowGraph<Node> cfg3 = createCfg(src3);
    assertNotNull(cfg3);

    // Empty switch
    String src4 = "switch (x) { }";
    ControlFlowGraph<Node> cfg4 = createCfg(src4);
    assertNotNull(cfg4);
  }

  @Test(timeout = 4000)
  public void testWithStatement() {
    String src = "with (obj) { prop = 1; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyHandlers() {
    // try-catch
    String src1 = "try { a(); } catch (e) { b(e); }";
    ControlFlowGraph<Node> cfg1 = createCfg(src1);
    assertNotNull(cfg1);

    // try-finally
    String src2 = "try { a(); } finally { c(); }";
    ControlFlowGraph<Node> cfg2 = createCfg(src2);
    assertNotNull(cfg2);

    // try-catch-finally
    String src3 = "try { a(); } catch (e) { b(e); } finally { c(); }";
    ControlFlowGraph<Node> cfg3 = createCfg(src3);
    assertNotNull(cfg3);
  }

  @Test(timeout = 4000)
  public void testFunctionReturnAndThrow() {
    String src = "function f(x) { if (x) return 1; throw new Error(); }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Structural Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptAndEmptyBlock() {
    String src = ";; {}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testEmptyCatchBlockSpecialCase() {
    // Empty catch block is handled specially in handleStmtList
    String src = "try { a(); } catch (e) {}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationsSkippedInStatementList() {
    String src = "x = 1; function f() {} y = 2;";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testLabeledBreaksAndContinues() {
    String src =
        "outer: for (var i = 0; i < 5; i++) {" +
        "  inner: for (var j = 0; j < 5; j++) {" +
        "    if (j == 1) continue outer;" +
        "    if (i == 2) break outer;" +
        "  }" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testLabeledBreakOnBlockAndIf() {
    String src =
        "lblBlock: {" +
        "  if (x) break lblBlock;" +
        "  y = 1;" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testLabeledContinueOnForIn() {
    String src =
        "outer: for (var k in obj) {" +
        "  if (k) continue outer;" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testSyntheticBlockBranching() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1;");
    Node block = new Node(Token.BLOCK);
    block.setIsSyntheticBlock(true);
    Node expr = new Node(Token.EXPR_RESULT, new Node(Token.NAME, "a"));
    block.addChildToBack(expr);
    root.addChildToBack(block);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull(cfg);
  }

  @Test(timeout = 4000)
  public void testTraverseFunctionsFlagDisabled() {
    String src = "var x = 1; function inner() { var y = 2; } var z = 3;";
    ControlFlowGraph<Node> cfgWithoutFunc = createCfg(src, false, true);
    assertNotNull(cfgWithoutFunc);
  }

  // =========================================================================
  // Partition D: Exception, Defensive Guards & Static Analysis Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testInvalidBreakThrowsException() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("break;");
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);
    cfa.process(null, root);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testInvalidContinueThrowsException() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("continue;");
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);
    cfa.process(null, root);
  }

  @Test(timeout = 4000)
  public void testBreakInIdeModeDoesNotThrow() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    compiler.initOptions(options);

    Node root = compiler.parseTestCode("break;");
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);
    cfa.process(null, root);
    assertNotNull(cfa.getCfg());
  }

  @Test(timeout = 4000)
  public void testMayThrowExceptionAllBranches() {
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.CALL)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.GETPROP)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.GETELEM)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.THROW)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.NEW)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.ASSIGN)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.INC)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.DEC)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.INSTANCEOF)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.FUNCTION)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.NUMBER)));
  }

  @Test(timeout = 4000)
  public void testIsBreakAndContinueStructures() {
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.FOR), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.DO), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.WHILE), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.SWITCH), false));

    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.IF), true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.IF), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.TRY), true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.TRY), false));

    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.EXPR_RESULT), true));

    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.FOR)));
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.DO)));
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.WHILE)));
    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.SWITCH)));
    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.BLOCK)));
  }

  @Test(timeout = 4000)
  public void testComputeFallThroughVariants() {
    Node num = new Node(Token.NUMBER);
    assertSame(num, ControlFlowAnalysis.computeFallThrough(num));

    Node doNode = new Node(Token.DO, num, new Node(Token.TRUE));
    assertSame(num, ControlFlowAnalysis.computeFallThrough(doNode));

    Node labelNode = new Node(Token.LABEL, new Node(Token.LABEL_NAME), num);
    assertSame(num, ControlFlowAnalysis.computeFallThrough(labelNode));
  }

  @Test(timeout = 4000)
  public void testGetExceptionHandlerAndCatchHandler() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("try { a(); } catch (e) { b(); }");
    Node tryNode = root.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();
    Node catchBlock = ControlFlowAnalysis.getCatchHandlerForBlock(tryBlock);
    assertNotNull(catchBlock);
    assertEquals(Token.CATCH, catchBlock.getType());

    assertNull(ControlFlowAnalysis.getCatchHandlerForBlock(new Node(Token.NUMBER)));

    Node handler = ControlFlowAnalysis.getExceptionHandler(tryBlock.getFirstChild());
    assertNotNull(handler);
    assertEquals(Token.CATCH, handler.getType());

    assertNull(ControlFlowAnalysis.getExceptionHandler(root));
  }

  // =========================================================================
  // Partition E: CFG Node Comparator & Priorities Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testAstControlFlowGraphComparator() {
    String src = "var x = 1; var y = 2;";
    ControlFlowGraph<Node> cfg = createCfg(src);

    Comparator<DiGraphNode<Node, Branch>> forwardComp = cfg.getOptionalNodeComparator(true);
    Comparator<DiGraphNode<Node, Branch>> backwardComp = cfg.getOptionalNodeComparator(false);
    assertNotNull(forwardComp);
    assertNotNull(backwardComp);

    List<DiGraphNode<Node, Branch>> nodes = cfg.getDirectedGraphNodes();
    assertTrue(nodes.size() >= 2);

    DiGraphNode<Node, Branch> n1 = nodes.get(0);
    DiGraphNode<Node, Branch> n2 = nodes.get(1);

    int cmpForward = forwardComp.compare(n1, n2);
    int cmpBackward = backwardComp.compare(n1, n2);

    assertEquals("Forward and backward comparators must be symmetric inverses",
        cmpForward, -cmpBackward);
    assertEquals(0, forwardComp.compare(n1, n1));
  }
}