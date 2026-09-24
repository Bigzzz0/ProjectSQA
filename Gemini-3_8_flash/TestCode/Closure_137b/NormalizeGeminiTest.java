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

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: Normalize & its nested classes (NormalizeStatements, PropogateConstantAnnotations, VerifyConstants)
 *
 * 1. Defect-Targeted Branches (Known Defects4J issue):
 *    - Process execution order: removeDuplicateDeclarations vs MakeDeclaredNamesUnique.
 *    - Catch block parameter scope collision: Catch block exception variable shadowing
 *      function-level var declaration ("catch(e) ... var e = 1;"). When removeDuplicateDeclarations
 *      runs before MakeDeclaredNamesUnique, it strips the "var" keyword and turns it into an assignment "e = 1".
 *
 * 2. Partition A: Core Functional Logic & Statement Normalizations
 *    - WHILE to FOR conversion (Token.WHILE -> Token.FOR with EMPTY children).
 *    - Extract FOR initializers (both VAR statements and EXPR_RESULT expressions).
 *    - Extract FOR initializers nested under LABEL.
 *    - Normalize non-block/non-loop labels (LABEL with EXPR_RESULT -> LABEL with BLOCK).
 *    - Function hoisting/reordering: move inner function declarations to the top of function body.
 *    - Split multiple VAR declarations: "var a = 1, b = 2;" -> "var a = 1; var b = 2;".
 *
 * 3. Partition B: Boundary Conditions & Edge Cases
 *    - Empty names (Token.NAME with empty string).
 *    - Empty VAR statements / assertOnChange flag checks.
 *    - For-in loops (initializers should NOT be extracted).
 *    - Duplicate VAR declarations without initializers inside blocks, loops, and labels.
 *
 * 4. Partition C: Constant Propagation & Verification
 *    - PropogateConstantAnnotations: JSDoc @const marking IS_CONSTANT_NAME property.
 *    - PropogateConstantAnnotations with assertOnChange=true throwing IllegalStateException.
 *    - VerifyConstants: consistent vs inconsistent constant annotations.
 *    - VerifyConstants: missing externsAndJs parent precondition checks.
 */
public class NormalizeGeminiTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private Node parse(Compiler compiler, String js) {
    Node n = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return n;
  }

  // =========================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Defects4J Failure Reproduction)
  // =========================================================================

  /**
   * Targets the defect where removeDuplicateDeclarations runs BEFORE
   * MakeDeclaredNamesUnique, causing a catch parameter name collision to strip
   * 'var e = 1' into 'e = 1'.
   */
  @Test(timeout = 4000)
  public void testCatchParameterShadowingVarDeclarationNotStripped() {
    Compiler compiler = createCompiler();
    String js = "function f() { try { throw 0; } catch (e) { e; } var e = 1; }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    // Locate the function body
    Node func = root.getFirstChild();
    assertEquals(Token.FUNCTION, func.getType());
    Node funcBody = func.getLastChild();

    // Verify that the var declaration for 'e' remains a Token.VAR and was not converted to an EXPR_RESULT assignment
    boolean foundVar = false;
    for (Node child = funcBody.getFirstChild(); child != null; child = child.getNext()) {
      if (child.getType() == Token.VAR) {
        foundVar = true;
        assertEquals(1, child.getChildCount());
        Node nameNode = child.getFirstChild();
        assertTrue(nameNode.getString().startsWith("e"));
      }
    }
    assertTrue("var declaration should be preserved after normalization", foundVar);
  }

  // =========================================================================
  // PARTITION A: Core Functional Logic & Statement Normalization
  // =========================================================================

  @Test(timeout = 4000)
  public void testConvertWhileToFor() {
    Compiler compiler = createCompiler();
    String js = "while (x < 10) { x++; }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node statement = root.getFirstChild();
    assertEquals(Token.FOR, statement.getType());
    assertEquals(4, statement.getChildCount());
    assertEquals(Token.EMPTY, statement.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerVar() {
    Compiler compiler = createCompiler();
    String js = "for (var i = 0; i < 10; i++) { foo(); }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertEquals("i", first.getFirstChild().getString());

    Node second = first.getNext();
    assertEquals(Token.FOR, second.getType());
    assertEquals(Token.EMPTY, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerExpr() {
    Compiler compiler = createCompiler();
    String js = "var i; for (i = 0; i < 10; i++) { foo(); }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());

    Node second = first.getNext();
    assertEquals(Token.EXPR_RESULT, second.getType());
    assertEquals(Token.ASSIGN, second.getFirstChild().getType());

    Node third = second.getNext();
    assertEquals(Token.FOR, third.getType());
    assertEquals(Token.EMPTY, third.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerUnderLabel() {
    Compiler compiler = createCompiler();
    String js = "myLoop: for (var k = 0; k < 5; k++) { break myLoop; }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertEquals("k", first.getFirstChild().getString());

    Node label = first.getNext();
    assertEquals(Token.LABEL, label.getType());
    Node forNode = label.getLastChild();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWithoutBlockOrLoop() {
    Compiler compiler = createCompiler();
    String js = "lbl: foo();";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node label = root.getFirstChild();
    assertEquals(Token.LABEL, label.getType());
    Node block = label.getLastChild();
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(Token.EXPR_RESULT, block.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testSplitVarDeclarations() {
    Compiler compiler = createCompiler();
    String js = "var a = 1, b = 2, c = 3;";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(3, root.getChildCount());
    Node child = root.getFirstChild();
    for (int i = 0; i < 3; i++) {
      assertEquals(Token.VAR, child.getType());
      assertEquals(1, child.getChildCount());
      child = child.getNext();
    }
  }

  @Test(timeout = 4000)
  public void testMoveNamedFunctionsHoisting() {
    Compiler compiler = createCompiler();
    String js = "function outer() { var x = 1; function inner1() {} var y = 2; function inner2() {} }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node outerFunc = root.getFirstChild();
    Node funcBody = outerFunc.getLastChild();

    Node first = funcBody.getFirstChild();
    assertEquals(Token.FUNCTION, first.getType());
    assertEquals("inner1", first.getFirstChild().getString());

    Node second = first.getNext();
    assertEquals(Token.FUNCTION, second.getType());
    assertEquals("inner2", second.getFirstChild().getString());

    Node third = second.getNext();
    assertEquals(Token.VAR, third.getType());
  }

  // =========================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testForInInitializerNotExtracted() {
    Compiler compiler = createCompiler();
    String js = "for (var prop in obj) { }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    assertEquals(1, root.getChildCount());
    Node forNode = root.getFirstChild();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.VAR, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarDeclarationsSimple() {
    Compiler compiler = createCompiler();
    String js = "var x = 1; var x = 2;";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    assertEquals(Token.EXPR_RESULT, second.getType());
    assertEquals(Token.ASSIGN, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarDeclarationsEmptyRemoved() {
    Compiler compiler = createCompiler();
    String js = "var x = 1; var x;";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    // The duplicate empty var declaration must be completely removed
    assertEquals(1, root.getChildCount());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarDeclarationsInForIn() {
    Compiler compiler = createCompiler();
    String js = "var key; for (var key in obj) { }";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node forNode = root.getLastChild();
    assertEquals(Token.FOR, forNode.getType());
    // "for (var key in obj)" should be changed to "for (key in obj)"
    assertEquals(Token.NAME, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarUnderLabel() {
    Compiler compiler = createCompiler();
    String js = "var z; L: var z;";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node label = root.getLastChild();
    assertEquals(Token.LABEL, label.getType());
    assertEquals(Token.EMPTY, label.getLastChild().getType());
  }

  // =========================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNormalizeAssertOnChangeThrowsOnReportCodeChange() {
    Compiler compiler = createCompiler();
    String js = "while (true) {}";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    // assertOnChange = true triggers IllegalStateException whenever an AST transformation occurs
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSplitVarDeclarationsEmptyVarAssertOnChangeThrows() {
    Compiler compiler = createCompiler();
    Node script = new Node(Token.SCRIPT);
    Node emptyVar = new Node(Token.VAR); // No children
    script.addChildToBack(emptyVar);

    Normalize.NormalizeStatements normalizer =
        new Normalize.NormalizeStatements(compiler, true);
    NodeTraversal.traverse(compiler, script, normalizer);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsFailsWhenNoParent() {
    Compiler compiler = createCompiler();
    Node root = new Node(Token.SCRIPT);
    Node externs = new Node(Token.SCRIPT);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    // root has no parent, which violates precondition
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsFailsWhenInconsistent() {
    Compiler compiler = createCompiler();
    Node root = new Node(Token.SCRIPT);
    Node name1 = Node.newString(Token.NAME, "MY_CONST");
    name1.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Node name2 = Node.newString(Token.NAME, "MY_CONST");
    name2.putBooleanProp(Node.IS_CONSTANT_NAME, false);
    root.addChildToBack(name1);
    root.addChildToBack(name2);

    Node externs = new Node(Token.SCRIPT);
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsCheckUserDeclarationsMismatchThrows() {
    Compiler compiler = createCompiler();
    Node root = new Node(Token.SCRIPT);
    // In default coding convention, all uppercase is treated as constant
    Node name = Node.newString(Token.NAME, "CONSTANT_VAR");
    name.putBooleanProp(Node.IS_CONSTANT_NAME, false);
    root.addChildToBack(name);

    Node externs = new Node(Token.SCRIPT);
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  // =========================================================================
  // PARTITION E: Constant Annotations Propagation
  // =========================================================================

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotationsSuccess() {
    Compiler compiler = createCompiler();
    String js = "/** @const */ var FOO = 1; FOO;";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    pass.process(externs, root);

    Node exprResult = root.getLastChild();
    assertEquals(Token.EXPR_RESULT, exprResult.getType());
    Node nameNode = exprResult.getFirstChild();
    assertEquals("FOO", nameNode.getString());
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPropagateConstantAnnotationsAssertOnChangeThrows() {
    Compiler compiler = createCompiler();
    String js = "/** @const */ var FOO = 1; FOO;";
    Node root = parse(compiler, js);
    Node externs = new Node(Token.BLOCK);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    pass.process(externs, root);
  }

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotationsEmptyNameNodeIgnored() {
    Compiler compiler = createCompiler();
    Node root = new Node(Token.SCRIPT);
    Node emptyName = Node.newString(Token.NAME, "");
    root.addChildToBack(emptyName);
    Node externs = new Node(Token.BLOCK);

    Normalize.PropogateConstantAnnotations pass =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    // Should not throw because empty name returns early
    pass.process(externs, root);
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsPassesForConsistentCode() {
    Compiler compiler = createCompiler();
    Node root = new Node(Token.SCRIPT);
    Node name1 = Node.newString(Token.NAME, "MY_CONST");
    name1.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Node name2 = Node.newString(Token.NAME, "MY_CONST");
    name2.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    root.addChildToBack(name1);
    root.addChildToBack(name2);

    Node externs = new Node(Token.SCRIPT);
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
    // Verify terminates with no exception
    assertTrue(name1.getBooleanProp(Node.IS_CONSTANT_NAME));
  }
}