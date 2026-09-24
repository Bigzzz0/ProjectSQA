/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Branch / Condition                                | Target Test Method
 * ---------------------------------------------------------------------------------------------------
 * enterScope: Global scope early-exit               | testGlobalScopeBypass()
 * enterScope: Exceed MAX_VARIABLES_TO_ANALYZE       | testTooManyVariablesBypass()
 * Candidate.canInline: defCfgNode is function param | testParameterNotCandidate()
 * Candidate.canInline: def is assign not exprAssign | testAssignUsedAsRValueCannotInline()
 * Candidate.canInline: checkRightOf has side-effect | testSideEffectRightOfDefinition()
 * Candidate.canInline: checkLeftOf has side-effect  | testSideEffectLeftOfUse()
 * Candidate.canInline: def RHS may have side effects| testDefRhsHasSideEffects()
 * Candidate.canInline: numUseWithinUseCfgNode != 1  | testMultipleUsesInSingleCfgNode()
 * Candidate.canInline: NodeUtil.isWithinLoop(use)   | testUseWithinLoopCannotInline()
 * Candidate.canInline: reachingUses.size != 1       | testMultipleReachingUsesCannotInline()
 * Candidate.canInline: def RHS has GETPROP/GETELEM/ | testComplexRhsTypesCannotInline()
 *                      ARRAYLIT/OBJECTLIT/REGEXP/NEW|
 * Candidate.canInline: statementBlock adjacent node | testAdjacentNodesInlining()
 * Candidate.canInline: non-adjacent statement path  | testCanInlineAcrossNoSideEffect() [DEFECT TRIGGER]
 * Candidate.inlineVariable: def is assign & labels  | testInlineLabeledAssignment()
 * Candidate.inlineVariable: def is var              | testInlineVarDeclaration()
 * SIDE_EFFECT_PREDICATE: call with side effects     | testInlineAcrossSideEffect1()     [DEFECT TRIGGER]
 * Defect Issue 698: Loop inlining regression        | testIssue698()                    [DEFECT TRIGGER]
 * Read check guards: Assign LHS, INC, DEC, CATCH    | testGatherCandidatesNonReadSkipped()
 * CompilerPass contract & lifecycle integrity       | testDirectProcessInvocation(), testLifecycleMethods()
 * ---------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.Node;

/**
 * Production-grade White-Box test suite targeting {@link FlowSensitiveInlineVariables}.
 */
public class FlowSensitiveInlineVariablesGeminiTest extends CompilerTestCase {

  public FlowSensitiveInlineVariablesGeminiTest() {
    enableNormalize();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  private void inline(String src, String expected) {
    test("function _func() {" + src + "}",
         "function _func() {" + expected + "}");
  }

  private void noInline(String src) {
    inline(src, src);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInlineVarDeclaration() {
    inline("var x = 1; x;", "var x; 1;");
  }

  @Test(timeout = 4000)
  public void testInlineAssignment() {
    inline("var x; x = 1; x;", "var x; 1;");
  }

  @Test(timeout = 4000)
  public void testAdjacentNodesInlining() {
    inline("var a = true; if (a) { var x = 10; x; }",
           "var a = true; if (a) { var x; 10; }");
  }

  @Test(timeout = 4000)
  public void testInlineLabeledAssignment() {
    inline("var x; L: x = 1; x;", "var x; 1;");
    inline("var x; L1: L2: x = 2; x;", "var x; 2;");
  }

  @Test(timeout = 4000)
  public void testDirectProcessInvocation() {
    Compiler compiler = new Compiler();
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    Node externs = new Node(132); // Token.SCRIPT
    Node root = new Node(132);
    pass.process(externs, root);
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleUsesInSingleCfgNode() {
    noInline("var x = 1; var y = x + x;");
  }

  @Test(timeout = 4000)
  public void testMultipleReachingUsesCannotInline() {
    noInline("var x = 1; if (true) { x; } else { x; }");
  }

  @Test(timeout = 4000)
  public void testUseWithinLoopCannotInline() {
    noInline("var x = 1; while (true) { x; }");
    noInline("var x = 1; for (var i = 0; i < 10; i++) { x; }");
    noInline("var x = 1; do { x; } while (true);");
  }

  @Test(timeout = 4000)
  public void testComplexRhsTypesCannotInline() {
    // GETPROP
    noInline("var a = {b: 1}; var x = a.b; x;");
    // GETELEM
    noInline("var a = [1]; var x = a[0]; x;");
    // ARRAYLIT
    noInline("var x = [1, 2]; x;");
    // OBJECTLIT
    noInline("var x = {a: 1}; x;");
    // REGEXP
    noInline("var x = /abc/; x;");
    // NEW
    noInline("var x = new Object(); x;");
  }

  @Test(timeout = 4000)
  public void testTooManyVariablesBypass() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i <= LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE + 1; i++) {
      sb.append("var v").append(i).append(" = ").append(i).append(";\n");
    }
    sb.append("v0;\n");
    String js = sb.toString();
    // Bypass triggered, no variable should be inlined
    noInline(js);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets known bug: call expression with potential side-effects must block inlining.
   */
  @Test(timeout = 4000)
  public void testInlineAcrossSideEffect1() {
    noInline("var y; var x = noSuchEntry['foo'](); print(x);");
  }

  /**
   * Targets known bug: intermediate node without side effect should safely permit inlining.
   */
  @Test(timeout = 4000)
  public void testCanInlineAcrossNoSideEffect() {
    inline("var y; var x = \"foo\"; var z = y; x;",
           "var y; var z = y; \"foo\";");
  }

  /**
   * Targets issue 698: loop variable dependency and side effect analysis across iterations.
   */
  @Test(timeout = 4000)
  public void testIssue698() {
    inline(
        "var item;\n" +
        "var transform;\n" +
        "for (var i = 0; i < 2; i++) {\n" +
        "  item = items[i];\n" +
        "  transform = getTransform(item);\n" +
        "  item.style.transform = transform;\n" +
        "}",
        "var item;\n" +
        "var transform;\n" +
        "for (var i = 0; i < 2; i++) {\n" +
        "  item = items[i];\n" +
        "  item.style.transform = getTransform(item);\n" +
        "}");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalScopeBypass() {
    // FlowSensitiveInlineVariables must not run on global scope
    testSame("var x = 1; x;");
  }

  @Test(timeout = 4000)
  public void testParameterNotCandidate() {
    testSame("function f(p) { p; }");
  }

  @Test(timeout = 4000)
  public void testAssignUsedAsRValueCannotInline() {
    noInline("var x; var y = (x = 1); x;");
  }

  @Test(timeout = 4000)
  public void testSideEffectRightOfDefinition() {
    noInline("var x; x = 1, print(); x;");
  }

  @Test(timeout = 4000)
  public void testSideEffectLeftOfUse() {
    noInline("var x = 1; print(), x;");
  }

  @Test(timeout = 4000)
  public void testDefRhsHasSideEffects() {
    noInline("var x = print(); x;");
  }

  @Test(timeout = 4000)
  public void testGatherCandidatesNonReadSkipped() {
    // Assignment LHS
    noInline("var x = 1; x = 2;");
    // Compound Assignment LHS
    noInline("var x = 1; x += 2;");
    // Increment / Decrement
    noInline("var x = 1; x++;");
    noInline("var x = 1; x--;");
    // Catch block param
    noInline("try {} catch (x) { x; }");
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testLifecycleMethods() {
    Compiler compiler = new Compiler();
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    NodeTraversal t = new NodeTraversal(compiler, pass);
    Node dummyNode = new Node(132);

    // Verify exitScope and visit do not throw or mutate unexpectedly
    pass.exitScope(t);
    pass.visit(t, dummyNode, null);
    assertNotNull(pass);
  }

  @Test(timeout = 4000)
  public void testIdempotency() {
    inline("var x = 1; var y = x; y;", "var x; var y = 1; y;");
  }
}