package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Branch / Condition                                     | Targeted Test Method
 * --------------------------------------------------------------------------------------------------
 * Token.WHILE -> Token.FOR normalization                | testWhileToForConversion
 * Token.LABEL non-block wrapping                         | testNormalizeLabelsWithExpression
 * Token.LABEL block/loop (no double-wrapping)            | testNormalizeLabelsWithLoopDoNotWrap, testNormalizeLabelsWithDoLoop, testNormalizeLabelsWithBlock
 * extractForInitializer with VAR init                    | testExtractForInitializerVar
 * extractForInitializer with EXPR init                   | testExtractForInitializerExpr
 * extractForInitializer empty init (no extraction)       | testExtractForInitializerEmpty
 * extractForInitializer inside single & nested LABEL     | testExtractForInitializerNestedInLabels
 * extractForInitializer for-in (no extraction)           | testExtractForInitializerForIn
 * splitVarDeclarations with multiple declarations        | testSplitMultipleVarDeclarations
 * splitVarDeclarations empty var node + assertOnChange   | testSplitVarDeclarationsEmptyVarAssertOnChange
 * moveNamedFunctions reordering to top                   | testMoveNamedFunctions, testMoveMultipleNamedFunctions
 * moveNamedFunctions already at top (no change)          | testMoveNamedFunctionsAlreadyAtTop
 * DuplicateDeclarationHandler init duplicate             | testDuplicateVarDeclarations
 * DuplicateDeclarationHandler empty duplicate removal    | testDuplicateEmptyVarDeclarations
 * DuplicateDeclarationHandler for-in duplicate           | testDuplicateVarInForIn
 * Defect Issue 115 (duplicate arguments in function)     | testIssue115, testIssue115EmptyArgumentsDeclaration
 * PropogateConstantAnnotations JSdoc @const propagation  | testPropagateConstantAnnotationsWithJSDoc
 * PropogateConstantAnnotations with assertOnChange       | testPropagateConstantAnnotationsAssertOnChange
 * PropogateConstantAnnotations empty name handling       | testPropagateConstantAnnotationsEmptyName
 * VerifyConstants preconditions (null parent, externs)   | testVerifyConstantsWithoutParent, testVerifyConstantsParentWithoutExterns
 * VerifyConstants user declarations (expected vs const)  | testVerifyConstantsExpectedConstantNotAnnotated, testVerifyConstantsUnexpectedConstantAnnotated
 * VerifyConstants consistency across AST                 | testVerifyConstantsInconsistentAnnotation, testVerifyConstantsConsistent
 * assertOnChange violations reporting                    | testAssertOnChangeWithWhile, testAssertOnChangeWithMultipleVarDeclarations,
 *                                                        | testAssertOnChangeWithForInitializer, testAssertOnChangeWithLabelNormalization,
 *                                                        | testAssertOnChangeWithDuplicateVar
 * --------------------------------------------------------------------------------------------------
 */
public class NormalizeGeminiTest {

  private Node parseAndNormalize(Compiler compiler, String js, boolean assertOnChange) {
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode(js);
    new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, assertOnChange);
    normalize.process(externs, root);
    return root;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testWhileToForConversion() {
    Compiler compiler = new Compiler();
    String js = "while (x < 10) { x++; }";
    Node root = parseAndNormalize(compiler, js, false);

    assertNotNull(root);
    Node forNode = root.getFirstChild();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
    assertEquals(Token.LT, forNode.getFirstChild().getNext().getType());
  }

  @Test(timeout = 4000)
  public void testSplitMultipleVarDeclarations() {
    Compiler compiler = new Compiler();
    String js = "var a = 1, b = 2, c = 3;";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(3, root.getChildCount());
    Node firstVar = root.getFirstChild();
    assertEquals(Token.VAR, firstVar.getType());
    assertEquals("a", firstVar.getFirstChild().getString());

    Node secondVar = firstVar.getNext();
    assertEquals(Token.VAR, secondVar.getType());
    assertEquals("b", secondVar.getFirstChild().getString());

    Node thirdVar = secondVar.getNext();
    assertEquals(Token.VAR, thirdVar.getType());
    assertEquals("c", thirdVar.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerVar() {
    Compiler compiler = new Compiler();
    String js = "for (var i = 0; i < 5; i++) { foo(); }";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(2, root.getChildCount());
    Node extractedVar = root.getFirstChild();
    assertEquals(Token.VAR, extractedVar.getType());
    assertEquals("i", extractedVar.getFirstChild().getString());

    Node forNode = extractedVar.getNext();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerExpr() {
    Compiler compiler = new Compiler();
    String js = "for (i = 0; i < 5; i++) { foo(); }";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(2, root.getChildCount());
    Node extractedExpr = root.getFirstChild();
    assertEquals(Token.EXPR_RESULT, extractedExpr.getType());
    assertEquals(Token.ASSIGN, extractedExpr.getFirstChild().getType());

    Node forNode = extractedExpr.getNext();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerNestedInLabels() {
    Compiler compiler = new Compiler();
    String js = "l1: l2: for (var i = 0; i < 5; i++) {}";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(2, root.getChildCount());
    Node extractedVar = root.getFirstChild();
    assertEquals(Token.VAR, extractedVar.getType());
    assertEquals("i", extractedVar.getFirstChild().getString());

    Node l1 = extractedVar.getNext();
    assertEquals(Token.LABEL, l1.getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWithExpression() {
    Compiler compiler = new Compiler();
    String js = "lbl: x = 1;";
    Node root = parseAndNormalize(compiler, js, false);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node blockNode = labelNode.getLastChild();
    assertEquals(Token.BLOCK, blockNode.getType());
    assertEquals(Token.EXPR_RESULT, blockNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWithLoopDoNotWrap() {
    Compiler compiler = new Compiler();
    String js = "lbl: while (true) {}";
    Node root = parseAndNormalize(compiler, js, false);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node loopChild = labelNode.getLastChild();
    assertEquals(Token.FOR, loopChild.getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWithDoLoop() {
    Compiler compiler = new Compiler();
    String js = "lbl: do { x++; } while (x < 5);";
    Node root = parseAndNormalize(compiler, js, false);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    assertEquals(Token.DO, labelNode.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWithBlock() {
    Compiler compiler = new Compiler();
    String js = "lbl: { x = 1; }";
    Node root = parseAndNormalize(compiler, js, false);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    assertEquals(Token.BLOCK, labelNode.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testMoveNamedFunctions() {
    Compiler compiler = new Compiler();
    String js = "function f() { var x = 1; function g() { return 2; } return x + g(); }";
    Node root = parseAndNormalize(compiler, js, false);

    Node fNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fNode.getType());
    Node fBody = fNode.getLastChild();

    Node firstInBody = fBody.getFirstChild();
    assertEquals(Token.FUNCTION, firstInBody.getType());
    assertEquals("g", firstInBody.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testMoveMultipleNamedFunctions() {
    Compiler compiler = new Compiler();
    String js = "function f() { var a = 1; function g() {} var b = 2; function h() {} }";
    Node root = parseAndNormalize(compiler, js, false);

    Node fNode = root.getFirstChild();
    Node fBody = fNode.getLastChild();

    Node first = fBody.getFirstChild();
    assertEquals(Token.FUNCTION, first.getType());
    assertEquals("g", first.getFirstChild().getString());

    Node second = first.getNext();
    assertEquals(Token.FUNCTION, second.getType());
    assertEquals("h", second.getFirstChild().getString());

    Node third = second.getNext();
    assertEquals(Token.VAR, third.getType());
  }

  @Test(timeout = 4000)
  public void testMoveNamedFunctionsAlreadyAtTop() {
    Compiler compiler = new Compiler();
    String js = "function f() { function g() {} function h() {} var a = 1; }";
    Node root = parseAndNormalize(compiler, js, false);

    Node fBody = root.getFirstChild().getLastChild();
    assertEquals("g", fBody.getFirstChild().getFirstChild().getString());
    assertEquals("h", fBody.getFirstChild().getNext().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarDeclarations() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; var a = 2;";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(2, root.getChildCount());
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());

    Node second = first.getNext();
    assertEquals(Token.EXPR_RESULT, second.getType());
    assertEquals(Token.ASSIGN, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateEmptyVarDeclarations() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; var a;";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(1, root.getChildCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarInForIn() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; for (var a in obj) {}";
    Node root = parseAndNormalize(compiler, js, false);

    Node forNode = root.getFirstChild().getNext();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.NAME, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotationsWithJSDoc() {
    Compiler compiler = new Compiler();
    String js = "/** @const */ var CONST_VAL = 42; CONST_VAL;";
    Node root = parseAndNormalize(compiler, js, false);

    Node exprNode = root.getLastChild();
    assertEquals(Token.EXPR_RESULT, exprNode.getType());
    Node nameNode = exprNode.getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    Compiler compiler = new Compiler();
    Node root = parseAndNormalize(compiler, "", false);
    assertEquals(0, root.getChildCount());
  }

  @Test(timeout = 4000)
  public void testEmptyFunctionBody() {
    Compiler compiler = new Compiler();
    Node root = parseAndNormalize(compiler, "function f() {}", false);
    Node fBody = root.getFirstChild().getLastChild();
    assertEquals(0, fBody.getChildCount());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerEmpty() {
    Compiler compiler = new Compiler();
    String js = "for (; i < 5; i++) {}";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(1, root.getChildCount());
    assertEquals(Token.FOR, root.getFirstChild().getType());
    assertEquals(Token.EMPTY, root.getFirstChild().getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerForIn() {
    Compiler compiler = new Compiler();
    String js = "for (var k in obj) {}";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(1, root.getChildCount());
    assertEquals(Token.FOR, root.getFirstChild().getType());
    assertEquals(Token.VAR, root.getFirstChild().getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testSingleVarDeclarationUnmodified() {
    Compiler compiler = new Compiler();
    String js = "var single = 100;";
    Node root = parseAndNormalize(compiler, js, false);

    assertEquals(1, root.getChildCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Targeting CompilerRunnerTest::testIssue115)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue115() {
    Compiler compiler = new Compiler();
    String js = "function f() { " +
        "  var arguments = Array.prototype.slice.call(arguments, 0); " +
        "  return arguments[0]; " +
        "}";
    Node root = parseAndNormalize(compiler, js, false);

    assertNotNull(root);
    Node functionNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, functionNode.getType());
    Node functionBody = functionNode.getLastChild();
    Node firstStmt = functionBody.getFirstChild();
    assertEquals(Token.EXPR_RESULT, firstStmt.getType());
    assertEquals(Token.ASSIGN, firstStmt.getFirstChild().getType());
    assertEquals("arguments", firstStmt.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testIssue115EmptyArgumentsDeclaration() {
    Compiler compiler = new Compiler();
    String js = "function f() { " +
        "  var arguments; " +
        "  return arguments; " +
        "}";
    Node root = parseAndNormalize(compiler, js, false);

    assertNotNull(root);
    Node functionBody = root.getFirstChild().getLastChild();
    assertEquals(1, functionBody.getChildCount());
    assertEquals(Token.RETURN, functionBody.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testIssue115DuplicateVarInInnerScope() {
    Compiler compiler = new Compiler();
    String js = "function f() { " +
        "  var default__1 = 1; " +
        "  function x() { " +
        "    var default__1 = 2; " +
        "  } " +
        "}";
    Node root = parseAndNormalize(compiler, js, false);
    assertNotNull(root);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeWithWhile() {
    Compiler compiler = new Compiler();
    parseAndNormalize(compiler, "while (true) {}", true);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeWithMultipleVarDeclarations() {
    Compiler compiler = new Compiler();
    parseAndNormalize(compiler, "var a = 1, b = 2;", true);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeWithForInitializer() {
    Compiler compiler = new Compiler();
    parseAndNormalize(compiler, "for (var i = 0; i < 10; i++) {}", true);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeWithLabelNormalization() {
    Compiler compiler = new Compiler();
    parseAndNormalize(compiler, "myLabel: x = 1;", true);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeWithDuplicateVar() {
    Compiler compiler = new Compiler();
    parseAndNormalize(compiler, "var a = 1; var a = 2;", true);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSplitVarDeclarationsEmptyVarAssertOnChange() {
    Compiler compiler = new Compiler();
    Node script = new Node(Token.SCRIPT);
    Node emptyVar = new Node(Token.VAR);
    script.addChildToBack(emptyVar);
    Node externs = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, script);

    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPropagateConstantAnnotationsAssertOnChange() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("/** @const */ var X = 1; X;");
    new Node(Token.BLOCK, externs, root);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    pass.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsWithoutParent() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsParentWithoutExterns() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    new Node(Token.BLOCK, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsExpectedConstantNotAnnotated() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var CONST_VAL = 10;");
    new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsUnexpectedConstantAnnotated() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var myVar = 10;");
    new Node(Token.BLOCK, externs, root);

    Node nameNode = root.getFirstChild().getFirstChild();
    nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsInconsistentAnnotation() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var x = 1; x = 2;");
    new Node(Token.BLOCK, externs, root);

    Node firstX = root.getFirstChild().getFirstChild();
    firstX.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotationsEmptyName() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.SCRIPT);
    root.addChildToBack(Node.newString(Token.NAME, ""));
    new Node(Token.BLOCK, externs, root);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    pass.process(externs, root);
    assertEquals(1, root.getChildCount());
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsConsistent() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = compiler.parseTestCode("var x = 1; x = 2;");
    new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsEmptyName() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.SCRIPT);
    root.addChildToBack(Node.newString(Token.NAME, ""));
    new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
    assertEquals(1, root.getChildCount());
  }

  @Test(timeout = 4000)
  public void testDirectCallbackInvocations() {
    Compiler compiler = new Compiler();
    Normalize normalize = new Normalize(compiler, false);
    Node node = new Node(Token.BLOCK);
    NodeTraversal t = new NodeTraversal(compiler, normalize);

    boolean shouldTraverse = normalize.shouldTraverse(t, node, null);
    assertTrue(shouldTraverse);

    normalize.visit(t, node, null);
    assertEquals(Token.BLOCK, node.getType());
  }
}