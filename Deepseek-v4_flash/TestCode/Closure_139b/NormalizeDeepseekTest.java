package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Collections;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: Normalize.java
 *
 * Key decision branches exercised:
 *  - process(): NormalizeStatements traversal, MakeDeclaredNamesUnique,
 *    removeDuplicateDeclarations, PropogateConstantAnnotations.
 *  - NormalizeStatements:
 *      . LABEL child classification (BLOCK/FOR/WHILE/DO vs rewrite-to-block);
 *      . FOR initializer extraction (VAR vs expression, labels, FOR-IN skip);
 *      . VAR declaration splitting (single child vs multiple children);
 *      . WHILE-to-FOR conversion;
 *      . moveNamedFunctions() (already-at-top vs deferred declarations);
 *  - DuplicateDeclarationHandler:
 *      . simple duplicate var removal;
 *      . initialized duplicate var -> assignment;
 *      . duplicate FOR-IN var -> bare name;
 *  - Constant propagation and verification:
 *      . IS_CONSTANT_NAME annotation from JSDoc;
 *      . assertOnChange paths.
 *
 * Defect targeted:
 *  Unhoisted named function declarations are not rewritten to
 *  "var f = function() {};".  This is known to break:
 *    - testNormalizeFunctionDeclarations
 *    - testRemoveDuplicateVarDeclarations3
 *    - testMoveFunctions2
 */
public class NormalizeDeepseekTest {

  private static Compiler compileJs(String js) {
    CompilerOptions options = new CompilerOptions();
    Compiler compiler = new Compiler();
    compiler.initOptions(options);
    compiler.compile(
        Collections.singletonList(SourceFile.fromCode("externs", "")),
        Collections.singletonList(SourceFile.fromCode("testcode", js)),
        options);
    return compiler;
  }

  private static String normalizeJs(String js) {
    Compiler compiler = compileJs(js);
    Node externs = compiler.getRoot().getFirstChild();
    Node root = compiler.getRoot().getLastChild();
    new Normalize(compiler, false).process(externs, root);
    return compiler.toSource();
  }

  private static String printJs(String js) {
    return compileJs(js).toSource();
  }

  private static void assertNormalized(String js, String expectedJs) {
    assertEquals(printJs(expectedJs), normalizeJs(js));
  }

  private static Node findName(Node n, String name) {
    if (n.getType() == Token.NAME && name.equals(n.getString())) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node result = findName(c, name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static boolean hasConstantName(Node n, String name) {
    if (n.getType() == Token.NAME
        && name.equals(n.getString())
        && n.getBooleanProp(Node.IS_CONSTANT_NAME)) {
      return true;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (hasConstantName(c, name)) {
        return true;
      }
    }
    return false;
  }

  @Test(timeout = 4000)
  public void testEmptyScript() {
    assertNormalized("", "");
  }

  @Test(timeout = 4000)
  public void testSplitVarDeclarations() {
    assertNormalized("var a, b;", "var a; var b;");
  }

  @Test(timeout = 4000)
  public void testSplitVarDeclarationsWithInitializers() {
    assertNormalized("var a = 1, b = 2;", "var a = 1; var b = 2;");
  }

  @Test(timeout = 4000)
  public void testExtractForInitializer() {
    assertNormalized(
        "for (var i = 0; i < 10; i++) {}",
        "var i = 0; for (; i < 10; i++) {}");
  }

  @Test(timeout = 4000)
  public void testExtractForExpressionInitializer() {
    assertNormalized(
        "for (init(); cond; ) {}",
        "init(); for (; cond; ) {}");
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerBeforeLabel() {
    assertNormalized(
        "label: for (var i = 0; i < 10; i++) {}",
        "var i = 0; label: for (; i < 10; i++) {}");
  }

  @Test(timeout = 4000)
  public void testForInInitializerNotExtracted() {
    assertNormalized("for (var k in obj) {}", "for (var k in obj) {}");
  }

  @Test(timeout = 4000)
  public void testConvertWhileToFor() {
    assertNormalized("while (x) { x = 0; }", "for (; x; ) { x = 0; }");
  }

  @Test(timeout = 4000)
  public void testNormalizeLabel() {
    assertNormalized("l: foo();", "l: { foo(); }");
  }

  @Test(timeout = 4000)
  public void testLabeledWhileConverted() {
    assertNormalized("l: while (x) {}", "l: for (; x; ) {}");
  }

  @Test(timeout = 4000)
  public void testMoveFunctions() {
    assertNormalized(
        "function f() { foo(); function g() {} }",
        "function f() { function g() {} foo(); }");
  }

  @Test(timeout = 4000)
  public void testMoveMultipleFunctions() {
    assertNormalized(
        "function f() { foo(); function g() {} bar(); function h() {} }",
        "function f() { function g() {} function h() {} foo(); bar(); }");
  }

  @Test(timeout = 4000)
  public void testMoveFunctions2() {
    // A function declaration nested in a block must be normalized to a
    // variable declaration: this is the core defect targeted by the suite.
    assertNormalized(
        "function f() { var a; if (true) { function g() {} } }",
        "function f() { var a; if (true) { var g = function() {}; } }");
  }

  @Test(timeout = 4000)
  public void testNormalizeFunctionDeclarations() {
    // Directly targets the missing rewrite of unhoisted function declarations.
    assertNormalized(
        "if (true) { function f() {} }",
        "if (true) { var f = function() {}; }");
  }

  @Test(timeout = 4000)
  public void testNormalizeNestedFunctionDeclaration() {
    assertNormalized(
        "function f() { if (x) { function g() {} } }",
        "function f() { if (x) { var g = function() {}; } }");
  }

  @Test(timeout = 4000)
  public void testRemoveDuplicateVarDeclarations() {
    assertNormalized("var a; var a;", "var a;");
  }

  @Test(timeout = 4000)
  public void testRemoveDuplicateVarWithInitializer() {
    assertNormalized("var a = 1; var a;", "var a = 1;");
  }

  @Test(timeout = 4000)
  public void testRemoveDuplicateVarDeclarations3() {
    // After block-function rewriting, this becomes a duplicate var where the
    // second declaration is initialized, and must collapse to an assignment.
    assertNormalized(
        "var f = 1; if (true) { function f() {} }",
        "var f = 1; if (true) { f = function() {}; }");
  }

  @Test(timeout = 4000)
  public void testRemoveDuplicateForInVar() {
    assertNormalized(
        "var k; for (var k in obj) {}",
        "var k; for (k in obj) {}");
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testAssertOnChangeRejectsVarSplit() {
    Compiler compiler = compileJs("var a, b;");
    new Normalize(compiler, true).process(
        compiler.getRoot().getFirstChild(),
        compiler.getRoot().getLastChild());
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testAssertOnChangeRejectsWhileConversion() {
    Compiler compiler = compileJs("while (x) {}");
    new Normalize(compiler, true).process(
        compiler.getRoot().getFirstChild(),
        compiler.getRoot().getLastChild());
  }

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotations() {
    Compiler compiler = compileJs("/** @const */ var foo = 1;");
    Node externs = compiler.getRoot().getFirstChild();
    Node root = compiler.getRoot().getLastChild();
    new Normalize.PropogateConstantAnnotations(compiler, false)
        .process(externs, root);
    assertTrue("Expected foo to be marked as a constant name",
        hasConstantName(root, "foo"));
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testPropagateConstantAnnotationsThrowsWhenForbidden() {
    Compiler compiler = compileJs("/** @const */ var foo = 1;");
    Node externs = compiler.getRoot().getFirstChild();
    Node root = compiler.getRoot().getLastChild();
    new Normalize.PropogateConstantAnnotations(compiler, true)
        .process(externs, root);
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsConsistent() {
    Compiler compiler = compileJs("var a = 1; a = 2;");
    Node externs = compiler.getRoot().getFirstChild();
    Node root = compiler.getRoot().getLastChild();
    new Normalize.VerifyConstants(compiler, false).process(externs, root);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testVerifyConstantsInconsistent() {
    Compiler compiler = compileJs("var a = 1; a = 2;");
    Node externs = compiler.getRoot().getFirstChild();
    Node root = compiler.getRoot().getLastChild();
    Node name = findName(root, "a");
    assertNotNull(name);
    name.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    new Normalize.VerifyConstants(compiler, false).process(externs, root);
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsWithUserDeclarations() {
    Compiler compiler = compileJs("/** @const */ var foo = 1;");
    Node externs = compiler.getRoot().getFirstChild();
    Node root = compiler.getRoot().getLastChild();
    new Normalize.PropogateConstantAnnotations(compiler, false)
        .process(externs, root);
    new Normalize.VerifyConstants(compiler, true).process(externs, root);
  }
}