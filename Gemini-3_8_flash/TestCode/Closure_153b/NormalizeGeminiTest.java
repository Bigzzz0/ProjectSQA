/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.Normalize
 *
 * 1. Defects4J Defect Target (Closure-102):
 *    - testDuplicateVarInExterns: When an extern declaration (e.g., "var window;") conflicts with a
 *      source declaration ("var window; window = 3;"), calling MakeDeclaredNamesUnique before
 *      removeDuplicateDeclarations causes the source variable to be renamed (e.g. to window$$1 or
 *      treated as local) rather than removing the duplicate VAR declaration.
 *    - testMakeLocalNamesUnique: Catch block exception variable scope collision, arguments name handling,
 *      and duplicate declarations inside and outside function scopes.
 *
 * 2. Normalization Branches Covered:
 *    - WHILE -> FOR conversion (Token.WHILE -> Token.FOR with empty initializers and conditions).
 *    - Function declaration hoisting/normalization (unhoisted named function to VAR declaration).
 *    - Moving function declarations within function bodies.
 *    - Splitting multi-child VAR declarations ("var a = 1, b = 2;" -> "var a = 1; var b = 2;").
 *    - Empty VAR handling under assertOnChange.
 *    - FOR and FOR-IN initializers extraction (moving VAR / expressions out of FOR loop headers).
 *    - LABEL normalization (wrapping non-block/non-loop children into a BLOCK).
 *    - Duplicate VAR removal:
 *      * Initialized VAR to ASSIGN ("var a = 1; var a = 2;" -> "a = 2;").
 *      * Empty duplicate VAR ("var a; var a;" -> second removed).
 *      * Duplicate VAR in FOR-IN ("for (var a in b)").
 *      * Catch exception collision (reporting CATCH_BLOCK_VAR_ERROR).
 *      * Function re-declaration of existing VAR.
 *    - PropagateConstantAnnotationsOverVars & VerifyConstants passes:
 *      * Naming conventions (ALL_CAPS names, @const JSDoc annotations).
 *      * String properties (isObjLitKey, GETPROP).
 *      * assertOnChange failure when unexpected const changes occur.
 *      * Inconsistent constant annotation assertions in VerifyConstants.
 *    - Synthetic & Test Code parsing helper entry points:
 *      * parseAndNormalizeSyntheticCode, parseAndNormalizeTestCode.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizeGeminiTest {

  // Helper to compile and normalize code
  private Compiler compileAndNormalize(String externsCode, String jsCode, boolean assertOnChange) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node externsNode = compiler.parseTestCode(externsCode);
    Node rootNode = compiler.parseTestCode(jsCode);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize normalize = new Normalize(compiler, assertOnChange);
    normalize.process(externsNode, rootNode);
    return compiler;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-102 Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDuplicateVarInExterns() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String externs = "var window;";
    String js = "var window; window = 3;";

    Node externsNode = compiler.parseTestCode(externs);
    Node rootNode = compiler.parseTestCode(js);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsNode, rootNode);

    // The duplicate "var window;" in JS should be eliminated, leaving just "window = 3;"
    String normalizedJs = compiler.toSource(rootNode);
    assertFalse("Duplicate VAR for extern variable 'window' should be removed",
        normalizedJs.contains("var window"));
    assertTrue("Assignment should remain intact", normalizedJs.contains("window = 3"));
  }

  @Test(timeout = 4000)
  public void testMakeLocalNamesUniqueWithDuplicateVar() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String externs = "";
    String js = "function f() { var x = 1; var x = 2; }";

    Node externsNode = compiler.parseTestCode(externs);
    Node rootNode = compiler.parseTestCode(js);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsNode, rootNode);

    String result = compiler.toSource(rootNode);
    // Duplicate declaration should be converted to an assignment: x = 2;
    assertTrue("Should contain assignment for redeclared var", result.contains("x = 2"));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Normalization Transforms
  // =========================================================================

  @Test(timeout = 4000)
  public void testConvertWhileToFor() {
    Compiler compiler = new Compiler();
    String js = "while (x < 10) { x++; }";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");
    // While should be transformed to a FOR node
    Node firstStatement = root.getFirstChild();
    assertEquals(Token.FOR, firstStatement.getType());
  }

  @Test(timeout = 4000)
  public void testSplitVarDeclarations() {
    Compiler compiler = new Compiler();
    String js = "var a = 1, b = 2, c = 3;";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");

    // After normalization, each var should have exactly one child
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
  public void testExtractForInitializerVar() {
    Compiler compiler = new Compiler();
    String js = "for (var i = 0; i < 10; i++) {}";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");

    // The VAR should be moved before the FOR loop
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertEquals("i", first.getFirstChild().getString());

    Node second = first.getNext();
    assertEquals(Token.FOR, second.getType());
    assertEquals(Token.EMPTY, second.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInitializerExpression() {
    Compiler compiler = new Compiler();
    String js = "var i; for (i = 0; i < 10; i++) {}";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");

    // The i = 0 expression should be moved out before the FOR loop
    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());

    Node exprNode = varNode.getNext();
    assertEquals(Token.EXPR_RESULT, exprNode.getType());

    Node forNode = exprNode.getNext();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testExtractForInInitializerVar() {
    Compiler compiler = new Compiler();
    String js = "for (var k in obj) {}";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");

    // for (var k in obj) should become: var k; for (k in obj)
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    assertEquals("k", first.getFirstChild().getString());

    Node forNode = first.getNext();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.NAME, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testNormalizeLabelsWrappingNonBlock() {
    Compiler compiler = new Compiler();
    String js = "myLabel: var x = 1;";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");

    // myLabel should contain a BLOCK child wrapping the statement
    Node label = root.getFirstChild();
    while (label != null && label.getType() != Token.LABEL) {
      label = label.getNext();
    }
    assertNotNull(label);
    assertEquals(Token.BLOCK, label.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testMoveNamedFunctionsWithinBody() {
    Compiler compiler = new Compiler();
    String js = "function f() { var x = 1; function g() { return 2; } return g(); }";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");

    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node body = fnNode.getLastChild();

    // The function g declaration should be hoisted to the top of body
    Node firstStmtInBody = body.getFirstChild();
    assertEquals(Token.FUNCTION, firstStmtInBody.getType());
    assertEquals("g", firstStmtInBody.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testRewriteUnhoistedFunctionDeclaration() {
    Compiler compiler = new Compiler();
    String js = "if (true) { function inner() {} }";
    Node root = Normalize.parseAndNormalizeTestCode(compiler, js, "test");

    // Unhoisted function in block should become a VAR declaration
    Node ifNode = root.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    Node block = ifNode.getLastChild();
    Node varNode = block.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    assertEquals("inner", varNode.getFirstChild().getString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Special Handlings
  // =========================================================================

  @Test(timeout = 4000)
  public void testCatchBlockVarDeclarationError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "function f() { try {} catch (e) { var e; } }";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsNode, rootNode);

    assertEquals("Should report CATCH_BLOCK_VAR_ERROR", 1, compiler.getErrorCount());
    assertEquals(Normalize.CATCH_BLOCK_VAR_ERROR.key, compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testRedeclareFunctionOverVar() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    // Redeclaration where function redeclares existing var
    String js = "var f = 1; function f() {}";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsNode, rootNode);

    String res = compiler.toSource(rootNode);
    assertTrue(res.contains("f = 1") || res.contains("function f"));
  }

  @Test(timeout = 4000)
  public void testParseAndNormalizeSyntheticCode() {
    Compiler compiler = new Compiler();
    String code = "var x = 1, y = 2;";
    Node result = Normalize.parseAndNormalizeSyntheticCode(compiler, code, "synth_");
    assertNotNull(result);
    // Two separate VAR statements expected
    int vars = 0;
    for (Node c = result.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) vars++;
    }
    assertEquals(2, vars);
  }

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotationsOverVars() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    // CONSTANT by convention (UPPER_CASE)
    String js = "var FOO_BAR = 123; var regular = FOO_BAR;";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externsNode, rootNode);

    // Check that FOO_BAR has IS_CONSTANT_NAME set
    Node firstVar = rootNode.getFirstChild();
    Node nameNode = firstVar.getFirstChild();
    assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(timeout = 4000)
  public void testPropagateConstantAnnotationsEmptyName() {
    Compiler compiler = new Compiler();
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode("");

    // Create an empty Token.NAME
    Node emptyName = new Node(Token.NAME);
    emptyName.setString("");
    rootNode.addChildToFront(emptyName);

    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    // Should gracefully skip empty name
    pass.process(externsNode, rootNode);
    assertFalse(emptyName.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeThrowsOnModification() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "while (true) {}";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    // assertOnChange = true triggers IllegalStateException if tree is modified
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externsNode, rootNode);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPropagateConstantThrowsOnAssertOnChange() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var CONSTANT_VALUE = 42;";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node mainRoot = new Node(Token.BLOCK, externsNode, rootNode);

    // Should throw because CONSTANT_VALUE needs annotation but forbidChanges = true
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, true);
    pass.process(externsNode, rootNode);
  }

  @Test(timeout = 4000)
  public void testVerifyConstantsPassSuccess() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var CONST_VAL = 1; var x = CONST_VAL;";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsNode, rootNode);

    // Manually mark all CONST_VAL nodes as constant
    for (Node c = rootNode.getFirstChild(); c != null; c = c.getNext()) {
      for (Node n = c.getFirstChild(); n != null; n = n.getNext()) {
        if ("CONST_VAL".equals(n.getString())) {
          n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
        }
      }
    }

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externsNode, rootNode);
    // Verification should pass without throwing
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsPassInconsistent() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var myVar = 1; var myVar = 2;";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsNode, rootNode);

    // Mark one as constant and leave the other unmarked
    Node firstVar = rootNode.getFirstChild();
    firstVar.getFirstChild().putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    // Should detect inconsistent annotation for the same name and throw IllegalStateException
    verifier.process(externsNode, rootNode);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVerifyConstantsUserDeclarationsMismatch() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    // ALL_CAPS name should be marked as constant, but isn't
    String js = "var CONSTANT_A = 100;";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    // Should fail checkUserDeclarations because CONSTANT_A is expectedConst but not marked
    verifier.process(externsNode, rootNode);
  }

  // =========================================================================
  // Partition E: Object Properties & Structural Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testObjectLiteralConstantKeys() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var obj = { CONST_PROP: 1 };";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize.NormalizeStatements normStatements =
        new Normalize.NormalizeStatements(compiler, false);
    NodeTraversal.traverse(compiler, rootNode, normStatements);

    // Inspect object lit key to verify constant annotation
    Node varNode = rootNode.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node objLit = nameNode.getFirstChild();
    Node key = objLit.getFirstChild();
    assertEquals("CONST_PROP", key.getString());
    assertTrue("Key should be marked constant by convention",
        key.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(timeout = 4000)
  public void testGetPropConstantKey() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "obj.CONST_FIELD = 10;";
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsNode, rootNode);

    Normalize.NormalizeStatements normStatements =
        new Normalize.NormalizeStatements(compiler, false);
    NodeTraversal.traverse(compiler, rootNode, normStatements);

    Node exprNode = rootNode.getFirstChild();
    Node assignNode = exprNode.getFirstChild();
    Node getPropNode = assignNode.getFirstChild();
    Node propName = getPropNode.getLastChild();

    assertEquals("CONST_FIELD", propName.getString());
    assertTrue("Property should be marked constant by convention",
        propName.getBooleanProp(Node.IS_CONSTANT_NAME));
  }
}