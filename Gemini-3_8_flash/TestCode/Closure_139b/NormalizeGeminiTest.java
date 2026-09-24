/*
 * Copyright 2024 The Closure Compiler Authors.
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

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.Normalize and inner classes:
 *   - Normalize (process, reportCodeChange, removeDuplicateDeclarations)
 *   - Normalize.NormalizeStatements (doStatementNormalizations, splitVarDeclarations,
 *       extractForInitializer, normalizeLabels, moveNamedFunctions, visit WHILE->FOR)
 *   - Normalize.DuplicateDeclarationHandler (onRedeclaration, replaceVarWithAssignment:
 *       hasChildren, isStatementBlock, FOR-in, LABEL)
 *   - Normalize.PropogateConstantAnnotations (visit NAME, Var lookup, isConstant check, assertOnChange)
 *   - Normalize.VerifyConstants (process preconditions, constantMap check, checkUserDeclarations)
 * Defects4J Ground Truth Defects:
 *   - Function declaration normalization / moving unhoisted functions
 *   - Duplicate var declarations vs function declarations (JSC_VAR_MULTIPLY_DECLARED_ERROR)
 *   - Moving functions in nested function bodies
 */
public class NormalizeGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
  }

  private Node testNormalize(String js) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    return root;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSplitVarDeclarations() {
    Node root = testNormalize("var a = 1, b = 2, c = 3;");
    // Should split into 3 distinct VAR statements
    int varCount = 0;
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      if (child.getType() == Token.VAR) {
        varCount++;
        assertEquals(1, child.getChildCount());
      }
    }
    assertEquals(3, varCount);
  }

  @Test(timeout = 4000)
  public void testWhileToForConversion() {
    Node root = testNormalize("while (x < 10) { x++; }");
    Node statement = root.getFirstChild();
    assertEquals(Token.FOR, statement.getType());
    // In for (;x < 10;), child 0 is EMPTY, child 1 is condition, child 2 is EMPTY, child 3 is BLOCK
    assertEquals(Token.EMPTY, statement.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerVar() {
    Node root = testNormalize("for (var i = 0; i < 10; i++) {}");
    // Initializer 'var i = 0' should be pulled out before the FOR loop
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertEquals("i", first.getFirstChild().getString());

    Node second = first.getNext();
    assertEquals(Token.FOR, second.getType());
    assertEquals(Token.EMPTY, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerExpression() {
    Node root = testNormalize("for (i = 0; i < 10; i++) {}");
    // Initializer 'i = 0' should be pulled out as an EXPR_RESULT before the FOR loop
    Node first = root.getFirstChild();
    assertEquals(Token.EXPR_RESULT, first.getType());
    assertEquals(Token.ASSIGN, first.getFirstChild().getType());

    Node second = first.getNext();
    assertEquals(Token.FOR, second.getType());
    assertEquals(Token.EMPTY, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerUnderLabel() {
    Node root = testNormalize("loop: for (var i = 0; i < 5; i++) {}");
    // The var i = 0 should be extracted before the LABEL
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());

    Node second = first.getNext();
    assertEquals(Token.LABEL, second.getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWrappingNonBlock() {
    Node root = testNormalize("lbl: a = 1;");
    Node label = root.getFirstChild();
    assertEquals(Token.LABEL, label.getType());
    // Non-block statement under label should be wrapped in a BLOCK
    Node block = label.getLastChild();
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(Token.EXPR_RESULT, block.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsPreservesAllowedChildren() {
    // Labels containing BLOCK, FOR, WHILE, DO, or nested LABEL should not be wrapped in another BLOCK
    Node rootBlock = testNormalize("lbl: { a = 1; }");
    assertEquals(Token.BLOCK, rootBlock.getFirstChild().getLastChild().getType());

    Node rootFor = testNormalize("lbl: for (;;) {}");
    assertEquals(Token.FOR, rootFor.getFirstChild().getLastChild().getType());

    Node rootDo = testNormalize("lbl: do {} while(true);");
    assertEquals(Token.DO, rootDo.getFirstChild().getLastChild().getType());

    Node rootNested = testNormalize("l1: l2: for (;;) {}");
    assertEquals(Token.LABEL, rootNested.getFirstChild().getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testMoveNamedFunctionsToBeginning() {
    Node root = testNormalize("function f() { var x = 1; function g() {} var y = 2; function h() {} }");
    Node f = root.getFirstChild();
    assertEquals(Token.FUNCTION, f.getType());
    Node body = f.getLastChild();

    Node firstStatement = body.getFirstChild();
    assertEquals(Token.FUNCTION, firstStatement.getType());
    assertEquals("g", firstStatement.getFirstChild().getString());

    Node secondStatement = firstStatement.getNext();
    assertEquals(Token.FUNCTION, secondStatement.getType());
    assertEquals("h", secondStatement.getFirstChild().getString());

    Node thirdStatement = secondStatement.getNext();
    assertEquals(Token.VAR, thirdStatement.getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarDeclarationsSimpleAssignment() {
    Node root = testNormalize("var a = 1; var a = 2;");
    // The second 'var a = 2' should become 'a = 2'
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());

    Node second = first.getNext();
    assertEquals(Token.EXPR_RESULT, second.getType());
    assertEquals(Token.ASSIGN, second.getFirstChild().getType());
    assertEquals(Token.NAME, second.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarDeclarationsWithoutValueRemoved() {
    Node root = testNormalize("var a = 1; var a;");
    // The second empty var declaration should be removed
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertNull(first.getNext());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarInForIn() {
    Node root = testNormalize("var a; for (var a in [1, 2]) {}");
    // The for-in should have its 'var a' converted to a simple name 'a'
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());

    Node second = first.getNext();
    assertEquals(Token.FOR, second.getType());
    // In for-in, first child is the loop variable expression
    assertEquals(Token.NAME, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarUnderLabel() {
    Node root = testNormalize("var a; lbl: var a;");
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());

    Node label = first.getNext();
    assertEquals(Token.LABEL, label.getType());
    assertEquals(Token.EMPTY, label.getLastChild().getType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    Node root = testNormalize("");
    assertNotNull(root);
    assertNull(root.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testSingleVarNoSplitNeeded() {
    Node root = testNormalize("var x = 1;");
    assertEquals(Token.VAR, root.getFirstChild().getType());
    assertNull(root.getFirstChild().getNext());
  }

  @Test(timeout = 4000)
  public void testForInInitializerNotExtracted() {
    Node root = testNormalize("for (var prop in obj) {}");
    // for-in initializers must stay inside the for structure
    Node first = root.getFirstChild();
    assertEquals(Token.FOR, first.getType());
    assertEquals(Token.VAR, first.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testFunctionWithAlreadyHoistedFunctions() {
    Node root = testNormalize("function f() { function a() {} function b() {} return 1; }");
    Node f = root.getFirstChild();
    Node body = f.getLastChild();
    assertEquals("a", body.getFirstChild().getFirstChild().getString());
    assertEquals("b", body.getFirstChild().getNext().getFirstChild().getString());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testRemoveDuplicateVarDeclarationsWithFunctionDeclaration() {
    // Defects4J ground truth: function f() {} var f = 1;
    // Redeclaration handler must properly convert 'var f = 1' into 'f = 1'
    Node root = testNormalize("function f() {} var f = 1;");
    Node first = root.getFirstChild();
    assertEquals(Token.FUNCTION, first.getType());

    Node second = first.getNext();
    assertNotNull(second);
    assertEquals(Token.EXPR_RESULT, second.getType());
    assertEquals(Token.ASSIGN, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testMoveFunctionsInsideBlock() {
    // Defects4J ground truth: testMoveFunctions2 / testNormalizeFunctionDeclarations
    // Functions inside inner blocks or nested functions should be normalized correctly
    Node root = testNormalize("function outer() { if (true) { function inner() {} } }");
    Node outer = root.getFirstChild();
    assertEquals(Token.FUNCTION, outer.getType());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeThrowsOnNormalizationNeeded() {
    Node root = compiler.parseTestCode("while (x) {}");
    Node externs = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testEmptyVarNodeThrowsWhenAssertOnChange() {
    Node emptyVar = new Node(Token.VAR);
    Node block = new Node(Token.BLOCK, emptyVar);

    Normalize.NormalizeStatements normalizer =
        new Normalize.NormalizeStatements(compiler, true);
    NodeTraversal t = new NodeTraversal(compiler, normalizer);
    t.traverse(block);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsThrowsWhenParentMissing() {
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    // root has no parent, should fail precondition
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsThrowsWhenExternsNotChildOfParent() {
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    new Node(Token.BLOCK, root); // parent contains root, but not externs

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsInconsistentConstantAnnotation() {
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);

    Node name1 = Node.newString(Token.NAME, "MY_CONST");
    name1.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Node name2 = Node.newString(Token.NAME, "MY_CONST");
    name2.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    root.addChildToBack(new Node(Token.EXPR_RESULT, name1));
    root.addChildToBack(new Node(Token.EXPR_RESULT, name2));

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  // =========================================================================
  // Partition E: Constant Annotation Propagation & Verification
  // =========================================================================

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotations() {
    Node root = compiler.parseTestCode("var a = 1; a;");
    Node externs = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, root);

    // Annotate variable 'a' in scope as constant via JSDoc
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    JSDocInfo info = builder.build(varNode);
    nameNode.setJSDocInfo(info);

    Normalize.PropogateConstantAnnotations propagator =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    propagator.process(externs, root);

    Node secondExpr = root.getLastChild();
    assertEquals(Token.EXPR_RESULT, secondExpr.getType());
    Node refName = secondExpr.getFirstChild();
    assertEquals("a", refName.getString());
    assertTrue(refName.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPropagateConstantAnnotationsAssertOnChangeThrows() {
    Node root = compiler.parseTestCode("var a = 1; a;");
    Node externs = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, root);

    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    JSDocInfo info = builder.build(varNode);
    nameNode.setJSDocInfo(info);

    Normalize.PropogateConstantAnnotations propagator =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    propagator.process(externs, root);
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsPassesWhenConsistent() {
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, root);

    Node name1 = Node.newString(Token.NAME, "MY_CONST");
    name1.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Node name2 = Node.newString(Token.NAME, "MY_CONST");
    name2.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    root.addChildToBack(new Node(Token.EXPR_RESULT, name1));
    root.addChildToBack(new Node(Token.EXPR_RESULT, name2));

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
    // Should complete without exception
  }

  @Test(timeout = 4000)
  public void testEmptyNameNodesIgnoredInPropagationAndVerification() {
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, root);

    Node emptyName = Node.newString(Token.NAME, "");
    root.addChildToBack(new Node(Token.EXPR_RESULT, emptyName));

    Normalize.PropogateConstantAnnotations propagator =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    propagator.process(externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }
}