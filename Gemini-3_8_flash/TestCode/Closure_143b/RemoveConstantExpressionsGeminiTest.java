package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.RemoveConstantExpressions
 * Target Branches & Decisions:
 *  1. trySimplify(Node parent, Node node):
 *     - node.getType() != Token.EXPR_RESULT (Early return for non-expression statements)
 *     - !NodeUtil.nodeTypeMayHaveSideEffects(exprBody) [TRUE: constant/pure expr; FALSE: side-effects]
 *  2. getSideEffectNodes(Node node):
 *     - Traversal using GatherSideEffectSubexpressionsCallback + CopySideEffectSubexpressions
 *     - 0 replacements (pure constant eliminated)
 *     - 1 replacement (simplified single side-effect)
 *     - >1 replacements (unrolled binary/comma operations)
 *  3. ReportCodeHasChangedListener & Result notifications:
 *     - nodeRemoved updates result.changed to true
 *     - notifyCompiler reports changes to compiler instance
 *
 * Known Defects4J Ground Truth Triggered:
 *  - RemoveConstantExpressionsTest::testCall1 -> Math.sin(0); is a call without side-effects
 *    that should be eliminated by this pass.
 *  - RemoveConstantExpressionsTest::testNew1 -> new Date(); is an instantiation without side-effects
 *    that should be eliminated by this pass.
 * -----------------------------------------------------------------------------------------------
 */
public class RemoveConstantExpressionsGeminiTest {

  /**
   * Helper method to parse input JS, run RemoveConstantExpressions,
   * parse expected JS, and verify the resulting AST source matches.
   */
  private void test(String js, String expected) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);

    RemoveConstantExpressions pass = new RemoveConstantExpressions(compiler);
    pass.process(externs, root);

    Compiler expectedCompiler = new Compiler();
    Node expectedRoot = expectedCompiler.parseTestCode(expected);

    String actualSource = compiler.toSource(root);
    String expectedSource = expectedCompiler.toSource(expectedRoot);

    assertEquals("Transformed code does not match expected output",
        expectedSource, actualSource);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testRemoveLiteralNumber() {
    test("1;", "");
  }

  @Test(timeout = 4000)
  public void testRemoveLiteralString() {
    test("'hello world';", "");
  }

  @Test(timeout = 4000)
  public void testRemoveLiteralBooleanAndNull() {
    test("true; false; null; undefined;", "");
  }

  @Test(timeout = 4000)
  public void testRemoveConstantArithmetic() {
    test("1 + 2 * 3 / 4;", "");
  }

  @Test(timeout = 4000)
  public void testRetainStandardCall() {
    test("foo();", "foo();");
  }

  @Test(timeout = 4000)
  public void testRetainStandardNew() {
    test("new CustomClass();", "new CustomClass();");
  }

  @Test(timeout = 4000)
  public void testRetainAssignmentsAndMutations() {
    test("x = 1; x += 2; x++; ++x; delete obj.prop;",
         "x = 1; x += 2; x++; ++x; delete obj.prop;");
  }

  @Test(timeout = 4000)
  public void testExtractSideEffectsFromAdditiveExpression() {
    test("1 + foo() + 2 + bar();", "foo(); bar();");
  }

  @Test(timeout = 4000)
  public void testExtractSideEffectsFromPurePrefix() {
    test("1 + foo();", "foo();");
  }

  @Test(timeout = 4000)
  public void testExtractSideEffectsFromPureSuffix() {
    test("foo() + 1;", "foo();");
  }

  @Test(timeout = 4000)
  public void testExtractSideEffectsFromCommaOperator() {
    test("1, foo(), 2, bar();", "foo(); bar();");
  }

  @Test(timeout = 4000)
  public void testExtractSideEffectsFromArrayLiteral() {
    test("[1, foo(), 2, bar()];", "foo(); bar();");
  }

  @Test(timeout = 4000)
  public void testExtractSideEffectsFromObjectLiteral() {
    test("({a: foo(), b: 1, c: bar()});", "foo(); bar();");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    test("", "");
  }

  @Test(timeout = 4000)
  public void testWhitespaceAndCommentsOnly() {
    test("  /* comment */  \n  // line comment\n  ", "");
  }

  @Test(timeout = 4000)
  public void testDeeplyNestedConstantExpressions() {
    test("((((1 + 2) * (3 - 4)) / (5 + 6)));", "");
  }

  @Test(timeout = 4000)
  public void testDeeplyNestedSideEffects() {
    test("((((1 + foo()) * 3)));", "foo();");
  }

  @Test(timeout = 4000)
  public void testMultipleSequentialStatementsMixed() {
    test("1; foo(); 2; bar(); 3;", "foo(); bar();");
  }

  @Test(timeout = 4000)
  public void testPreserveNonExprResultNodes() {
    test("var x = 1 + 2; function f() { return 3 + 4; }",
         "var x = 1 + 2; function f() { return 3 + 4; }");
  }

  @Test(timeout = 4000)
  public void testInsideFunctionBodySimplification() {
    test("function f() { 1; foo(); 2; }",
         "function f() { foo(); }");
  }

  @Test(timeout = 4000)
  public void testInsideBlockSimplification() {
    test("if (true) { 1; foo(); 2; }",
         "if (true) { foo(); }");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Faults)
  // =========================================================================

  /**
   * Targets ground truth failure:
   * com.google.javascript.jscomp.RemoveConstantExpressionsTest::testCall1
   * Math.sin(0); has no side effects and must be eliminated.
   */
  @Test(timeout = 4000)
  public void testCall1() {
    test("Math.sin(0);", "");
  }

  /**
   * Targets ground truth companion:
   * Call with side effects must be preserved.
   */
  @Test(timeout = 4000)
  public void testCall2() {
    test("foo();", "foo();");
  }

  /**
   * Targets ground truth failure:
   * com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNew1
   * new Date(); has no side effects and must be eliminated.
   */
  @Test(timeout = 4000)
  public void testNew1() {
    test("new Date();", "");
  }

  /**
   * Targets ground truth companion:
   * New instantiation with potential side effects must be preserved.
   */
  @Test(timeout = 4000)
  public void testNew2() {
    test("new A();", "new A();");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessWithNullExternsNode() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("1 + 2;");
    RemoveConstantExpressions pass = new RemoveConstantExpressions(compiler);
    // externs node is not directly used by the pass, null should be safely handled
    pass.process(null, root);

    Compiler expectedCompiler = new Compiler();
    Node expectedRoot = expectedCompiler.parseTestCode("");
    assertEquals(expectedCompiler.toSource(expectedRoot), compiler.toSource(root));
  }

  @Test(timeout = 4000)
  public void testCallbackOnNonExprResultNodeDirectly() {
    RemoveConstantExpressions.RemoveConstantRValuesCallback cb =
        new RemoveConstantExpressions.RemoveConstantRValuesCallback();
    Node nameNode = Node.newString(Token.NAME, "x");
    Node parent = new Node(Token.VAR, nameNode);

    // Visiting a non-EXPR_RESULT node must early-exit without AST modification
    cb.visit(null, nameNode, parent);
    assertFalse("Result should not report changes for non-EXPR_RESULT nodes",
        cb.getResult().hasChanged());
    assertEquals(1, parent.getChildCount());
  }

  @Test(timeout = 4000)
  public void testCallbackSimplifiesSyntheticExprResult() {
    RemoveConstantExpressions.RemoveConstantRValuesCallback cb =
        new RemoveConstantExpressions.RemoveConstantRValuesCallback();
    Node parent = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT, Node.newNumber(42));
    parent.addChildToBack(exprResult);

    cb.visit(null, exprResult, parent);
    assertTrue("Result should report change when simplifying constant expression",
        cb.getResult().hasChanged());
    assertEquals("Constant expression should be removed from parent BLOCK",
        0, parent.getChildCount());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testCallbackLifecycleAndCompilerNotification() {
    Compiler compiler = new Compiler();
    RemoveConstantExpressions.RemoveConstantRValuesCallback cb =
        new RemoveConstantExpressions.RemoveConstantRValuesCallback();

    assertNotNull("Callback result should be initialized", cb.getResult());
    assertFalse("Initial result state should be unchanged", cb.getResult().hasChanged());

    // Notify compiler when no changes occurred
    cb.getResult().notifyCompiler(compiler);

    // Trigger a change
    Node parent = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT, Node.newString("removable"));
    parent.addChildToBack(exprResult);
    cb.visit(null, exprResult, parent);

    assertTrue("State should transition to changed", cb.getResult().hasChanged());
    cb.getResult().notifyCompiler(compiler);
  }

  @Test(timeout = 4000)
  public void testPassInstantiationContract() {
    Compiler compiler = new Compiler();
    RemoveConstantExpressions pass = new RemoveConstantExpressions(compiler);
    assertNotNull("CompilerPass instance must be non-null", pass);
  }
}