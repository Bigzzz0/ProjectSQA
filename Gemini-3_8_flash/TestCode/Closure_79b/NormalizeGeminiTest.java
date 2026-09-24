/*
 * Copyright 2008 The Closure Compiler Authors.
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

import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.Normalize
 *
 * Defects4J Known Failure Conditions:
 * 1. NormalizeTest::testIssue -> java.lang.RuntimeException: INTERNAL COMPILER ERROR.
 *    Root Cause: Labeled FOR/FOR-IN loops containing variable declarations ("a: for (var x in y) {}").
 *    When extractForInitializer traverses LABEL nodes without a statement block context, it inappropriately
 *    inserts newly extracted statements as children of the LABEL rather than outside of it.
 *    When redeclarations occur inside the label, replaceVarWithAssignment encounters a LABEL parent where a
 *    statement block is expected, throwing IllegalStateException("Unexpected LABEL").
 *
 * Decision / Branch Matrix:
 * - NormalizeStatements.visit:
 *   * WHILE: converts to FOR with two EMPTY nodes.
 *   * FUNCTION: rewrites unhoisted named function declarations to VAR declarations.
 *   * NAME / STRING / GET / SET: annotations based on coding convention (unless normalized-obfuscated).
 * - NormalizeStatements.normalizeLabels:
 *   * LABEL, BLOCK, FOR, WHILE, DO children: return early.
 *   * Other statements: wrap in synthetic BLOCK.
 * - NormalizeStatements.extractForInitializer:
 *   * FOR-IN with VAR: extract VAR declaration before loop.
 *   * Standard FOR with non-EMPTY init: extract VAR or EXPR before loop.
 *   * Nested LABEL: recurse with appropriate before and beforeParent targets.
 * - NormalizeStatements.splitVarDeclarations:
 *   * Multi-child VARs (var a = 1, b = 2): split into distinct VAR statements.
 *   * Empty VAR with assertOnChange: throws IllegalStateException.
 * - NormalizeStatements.moveNamedFunctions:
 *   * Functions defined after statements in function bodies moved to top of function scope.
 * - DuplicateDeclarationHandler.onRedeclaration:
 *   * Global extern vs non-extern duplicate: tolerated and tracked.
 *   * Catch block variable collision: emits CATCH_BLOCK_VAR_ERROR.
 *   * Function name redeclaring prior VAR: undeclare VAR, declare function, convert VAR to assignment.
 *   * Duplicate VAR with initializer: converts "var a = value" to "a = value".
 *   * Duplicate VAR without initializer: removed from statement block or replaced in FOR-IN.
 * - PropagateConstantAnnotationsOverVars:
 *   * Propagates @const JSDoc and convention-based constant property to IS_CONSTANT_NAME.
 *   * assertOnChange violation check.
 * - VerifyConstants:
 *   * Verifies consistency of constant annotations and user-declared constants across AST.
 */
public class NormalizeGeminiTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private Node[] createTree(Compiler compiler, String externsCode, String jsCode) {
    Node externs = compiler.parseTestCode(externsCode);
    Node root = compiler.parseTestCode(jsCode);
    Node block = new Node(Token.BLOCK, externs, root);
    return new Node[] { externs, root, block };
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSplitVarDeclarations() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var a = 1, b = 2, c = 3;");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    // Root should now contain 3 separate VAR nodes instead of 1 multi-child VAR
    int varCount = 0;
    for (Node c = tree[1].getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) {
        varCount++;
        assertEquals(1, c.getChildCount());
      }
    }
    assertEquals(3, varCount);
  }

  @Test(timeout = 4000)
  public void testConvertWhileToFor() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "while (x < 10) { x++; }");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node firstStatement = tree[1].getFirstChild();
    assertNotNull(firstStatement);
    assertEquals(Token.FOR, firstStatement.getType());
    assertEquals(Token.EMPTY, firstStatement.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializers() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "for (var i = 0; i < 10; i++) {}");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node firstStatement = tree[1].getFirstChild();
    assertNotNull(firstStatement);
    assertEquals(Token.VAR, firstStatement.getType());
    assertEquals("i", firstStatement.getFirstChild().getString());

    Node secondStatement = firstStatement.getNext();
    assertNotNull(secondStatement);
    assertEquals(Token.FOR, secondStatement.getType());
    assertEquals(Token.EMPTY, secondStatement.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInInitializers() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "for (var p in obj) {}");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node firstStatement = tree[1].getFirstChild();
    assertNotNull(firstStatement);
    assertEquals(Token.VAR, firstStatement.getType());

    Node secondStatement = firstStatement.getNext();
    assertNotNull(secondStatement);
    assertEquals(Token.FOR, secondStatement.getType());
    assertEquals(Token.NAME, secondStatement.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testMoveNamedFunctionsWithinBody() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "function outer() { var x = 1; function inner() {} }");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node fnNode = tree[1].getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node fnBody = fnNode.getLastChild();

    Node firstInBody = fnBody.getFirstChild();
    assertNotNull(firstInBody);
    // Named inner function should have been hoisted to the top of the function body
    assertEquals(Token.FUNCTION, firstInBody.getType());
    assertEquals("inner", firstInBody.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testRewriteUnhoistedNamedFunction() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "if (true) { function nonHoisted() {} }");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node ifNode = tree[1].getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    Node block = ifNode.getLastChild();
    assertEquals(Token.BLOCK, block.getType());

    Node varNode = block.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();
    assertEquals("nonHoisted", nameNode.getString());
    Node assignedFn = nameNode.getFirstChild();
    assertEquals(Token.FUNCTION, assignedFn.getType());
    assertEquals("", assignedFn.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarInitializationConvertedToAssignment() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var x = 1; var x = 2;");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node first = tree[1].getFirstChild();
    assertEquals(Token.VAR, first.getType());

    Node second = first.getNext();
    assertEquals(Token.EXPR_RESULT, second.getType());
    assertEquals(Token.ASSIGN, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testLifeCycleStageTransition() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var a = 1;");
    assertFalse(compiler.getLifeCycleStage().isNormalized());

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    assertTrue(compiler.getLifeCycleStage().isNormalized());
    assertEquals(LifeCycleStage.NORMALIZED, compiler.getLifeCycleStage());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyCodeProcessing() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    assertFalse(tree[1].hasChildren());
    assertTrue(compiler.getLifeCycleStage().isNormalized());
  }

  @Test(timeout = 4000)
  public void testEmptyForLoopConditionAndIncrement() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "for (;;) { break; }");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node forNode = tree[1].getFirstChild();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWithVariousStatements() {
    Compiler compiler = createCompiler();
    // LABEL on loops and blocks should not be wrapped; expression statement should be wrapped in BLOCK
    Node[] tree = createTree(compiler, "", "lbl1: x = 1; lbl2: while (true) {} lbl3: { var z; }");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node lbl1 = tree[1].getFirstChild();
    assertEquals(Token.LABEL, lbl1.getType());
    // Expression statement is wrapped in a BLOCK
    assertEquals(Token.BLOCK, lbl1.getLastChild().getType());

    Node lbl2 = lbl1.getNext();
    assertEquals(Token.LABEL, lbl2.getType());
    // Loop label stays loop (converted to FOR)
    assertEquals(Token.FOR, lbl2.getLastChild().getType());

    Node lbl3 = lbl2.getNext();
    assertEquals(Token.LABEL, lbl3.getType());
    // Block remains BLOCK
    assertEquals(Token.BLOCK, lbl3.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testFunctionWithNoBodyStatements() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "function f() {}");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node fn = tree[1].getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node body = fn.getLastChild();
    assertFalse(body.hasChildren());
  }

  @Test(timeout = 4000)
  public void testEmptyNameNodeInPropagateConstants() {
    Compiler compiler = createCompiler();
    Node nameNode = new Node(Token.NAME, "");
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);

    NodeTraversal t = new NodeTraversal(compiler, pass);
    pass.visit(t, nameNode, null);
    assertFalse(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Normalized Issue Matrix)
  // =========================================================================

  @Test(timeout = 4000)
  public void testExtractForInInitializerUnderLabelBug() {
    // Directly targets the defect reported in NormalizeTest::testIssue / VarCheckTest
    // where labeled loops with var declarations caused unexpected LABEL node handling in replaceVarWithAssignment.
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var x; a: for (var x in y) {}");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    // Validation: Normalization must complete without throwing IllegalStateException: Unexpected LABEL
    assertTrue(compiler.getLifeCycleStage().isNormalized());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testExtractStandardForInitializerUnderLabelBug() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var i; a: for (var i = 0; i < 10; i++) {}");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    assertTrue(compiler.getLifeCycleStage().isNormalized());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNestedLabeledForInLoop() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "a: b: for (var k in obj) {}");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    assertTrue(compiler.getLifeCycleStage().isNormalized());
  }

  @Test(timeout = 4000)
  public void testCatchBlockVarDeclarationError() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "try { throw 1; } catch (e) { var e = 2; }");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    // Catch block exception variable collision must trigger CATCH_BLOCK_VAR_ERROR
    assertTrue(compiler.getErrorCount() > 0);
    assertEquals(Normalize.CATCH_BLOCK_VAR_ERROR, compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testGlobalExternAndSourceDuplicatePermitted() {
    Compiler compiler = createCompiler();
    // Variable declared both in externs and source should be accepted
    Node externs = compiler.parseTestCode("var window;");
    Node root = compiler.parseTestCode("var window = 1;");
    Node block = new Node(Token.BLOCK, externs, root);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testRedeclarationFunctionOverVar() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var f = 1; function f() {}");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    assertEquals(0, compiler.getErrorCount());
    assertTrue(compiler.getLifeCycleStage().isNormalized());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarWithoutInitializerRemoved() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var a = 1; var a;");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    // Second var a; should be removed entirely
    int count = 0;
    for (Node c = tree[1].getFirstChild(); c != null; c = c.getNext()) {
      count++;
    }
    assertEquals(1, count);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testAssertOnChangeThrowsOnUnnormalizedCode() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "while (true) {}");
    // When assertOnChange is true, converting while-to-for triggers an IllegalStateException
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(tree[0], tree[1]);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testAssertOnChangeThrowsOnEmptyVarNode() {
    Compiler compiler = createCompiler();
    Node varNode = new Node(Token.VAR);
    Node script = new Node(Token.SCRIPT, varNode);

    Normalize.NormalizeStatements statements = new Normalize.NormalizeStatements(compiler, true);
    statements.shouldTraverse(null, script, null);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNormalizeLabelsRejectsNonLabelNode() {
    Compiler compiler = createCompiler();
    Normalize.NormalizeStatements statements = new Normalize.NormalizeStatements(compiler, false);
    Node block = new Node(Token.BLOCK);
    // Passing a non-LABEL node directly to normalizeLabels violates preconditions
    Node label = new Node(Token.EXPR_RESULT);
    Node traversalRoot = new Node(Token.SCRIPT, label);
    statements.shouldTraverse(null, label, traversalRoot);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testVerifyConstantsFailsWhenNoParentNode() {
    Compiler compiler = createCompiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("");
    // root has no parent, violating VerifyConstants.process preconditions
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsPassesConsistentConstants() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var CONST_A = 1; var b = CONST_A;");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(tree[0], tree[1]);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testVerifyConstantsDetectsInconsistentAnnotation() {
    Compiler compiler = createCompiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1;");
    Node block = new Node(Token.BLOCK, externs, root);

    Node nameNode1 = root.getFirstChild().getFirstChild();
    nameNode1.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    // Create a second reference to 'a' without constant annotation
    Node nameNode2 = new Node(Token.NAME, "a");
    root.addChildToBack(new Node(Token.EXPR_RESULT, nameNode2));

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  // =========================================================================
  // Partition E: Constant Annotations & Static Helper Integration
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstantAnnotationByConvention() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var CONST_FOO = 100;");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node nameNode = tree[1].getFirstChild().getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(timeout = 4000)
  public void testConstantAnnotationByJSDoc() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var myConst = 42;");

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    JSDocInfo info = builder.build(tree[1]);

    Node nameNode = tree[1].getFirstChild().getFirstChild();
    nameNode.setJSDocInfo(info);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(timeout = 4000)
  public void testConstantObjectLitKeyAndGetProp() {
    Compiler compiler = createCompiler();
    Node[] tree = createTree(compiler, "", "var obj = { CONST_KEY: 1 }; obj.CONST_PROP = 2;");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(tree[0], tree[1]);

    Node varNode = tree[1].getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    Node keyNode = objLit.getFirstChild();
    assertTrue(keyNode.getBooleanProp(Node.IS_CONSTANT_NAME));

    Node exprNode = varNode.getNext();
    Node getPropNode = exprNode.getFirstChild().getFirstChild();
    Node propNameNode = getPropNode.getLastChild();
    assertTrue(propNameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(timeout = 4000)
  public void testParseAndNormalizeSyntheticCode() {
    Compiler compiler = createCompiler();
    Node js = Normalize.parseAndNormalizeSyntheticCode(compiler, "while (x) { x--; }", "prefix_");
    assertNotNull(js);
    assertEquals(Token.SCRIPT, js.getType());
    assertEquals(Token.FOR, js.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testParseAndNormalizeTestCode() {
    Compiler compiler = createCompiler();
    Node js = Normalize.parseAndNormalizeTestCode(compiler, "var a, b = 2;", "prefix_");
    assertNotNull(js);
    assertEquals(Token.SCRIPT, js.getType());
    // Should be split into two var statements
    int varCount = 0;
    for (Node c = js.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) {
        varCount++;
      }
    }
    assertEquals(2, varCount);
  }
}