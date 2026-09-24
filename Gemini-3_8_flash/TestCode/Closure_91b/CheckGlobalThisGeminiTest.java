package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.CheckGlobalThis
 * 
 * Decision Branches & Boundary Conditions Covered:
 * 1. shouldTraverse(NodeTraversal, Node, Node):
 *    - n.getType() == Token.FUNCTION:
 *      * getFunctionJsDocInfo returns JSDocInfo with:
 *        - isConstructor() == true -> returns false
 *        - isInterface() == true -> returns false
 *        - hasThisType() == true -> returns false
 *        - isOverride() == true -> returns false
 *        - JSDoc present but none of the above -> continues to parent check
 *      * getFunctionJsDocInfo lookup order:
 *        - direct n.getJSDocInfo()
 *        - parent.getType() == Token.NAME -> parent.getJSDocInfo()
 *        - parent.getType() == Token.NAME and gramps.getType() == Token.VAR -> gramps.getJSDocInfo()
 *        - parent.getType() == Token.ASSIGN -> parent.getJSDocInfo()
 *      * Function parent type validation:
 *        - pType in {BLOCK, SCRIPT, NAME, ASSIGN, STRING, NUMBER} -> returns true
 *        - pType not in allowed set (e.g., CALL, ARRAYLIT, EXPR_RESULT, RETURN) -> returns false
 *    - parent != null && parent.getType() == Token.ASSIGN:
 *      * n == lhs:
 *        - assignLhsChild == null -> assignLhsChild set to lhs
 *        - assignLhsChild != null -> assignLhsChild preserved (nested assignment guard)
 *      * n != lhs (RHS traversal):
 *        - NodeUtil.isGet(lhs) == true:
 *          + lhs.getType() == GETPROP && lhs.getLastChild().getString().equals("prototype") -> returns false
 *          + llhs.getType() == GETPROP && llhs.getLastChild().getString().equals("prototype") -> returns false
 *          + other GETPROP (e.g. A.prototype.bar.baz or A.bar) -> returns true
 *          + GETELEM on prototype (e.g. A.prototype[x]) -> returns false
 *          + GETELEM not on prototype -> returns true
 *        - NodeUtil.isGet(lhs) == false (e.g. simple name assign) -> returns true
 *    - parent == null -> returns true
 *
 * 2. visit(NodeTraversal, Node, Node):
 *    - n.getType() == Token.THIS:
 *      * shouldReportThis:
 *        - assignLhsChild != null (LHS of assignment) -> reports GLOBAL_THIS
 *        - parent != null && NodeUtil.isGet(parent) (GETPROP / GETELEM read) -> reports GLOBAL_THIS
 *        - standalone / return / argument -> does NOT report
 *    - n == assignLhsChild -> resets assignLhsChild to null
 *
 * 3. Defects4J Ground Truth Target:
 *    - Known Defect: CheckGlobalThisTest::testLendsAnnotation3
 *    - Target: Functions lent to a prototype via @lends {F.prototype} in an object literal
 *      must not be flagged as dangerous uses of the global 'this' object.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class CheckGlobalThisGeminiTest {

  private Compiler compileAndTraverse(String js, CheckLevel level) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis callback = new CheckGlobalThis(compiler, level);
    NodeTraversal.traverse(compiler, root, callback);
    return compiler;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalThisLhsAssignmentReportsWarning() {
    String js = "this.foo = 1;";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testGlobalThisPropertyGetReportsWarning() {
    String js = "var x = this.foo;";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testGlobalThisElementAccessReportsWarning() {
    String js = "this['bar'] = 2;";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodAssignmentNotFlagged() {
    String js = "function Foo() {} Foo.prototype.bar = function() { this.x = 1; };";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testPrototypeObjectLiteralAssignmentNotFlagged() {
    String js = "function Foo() {} Foo.prototype = { bar: function() { this.x = 1; } };";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testPrototypeElementAssignmentNotFlagged() {
    String js = "function Foo() {} Foo.prototype['bar'] = function() { this.x = 1; };";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCheckLevelErrorReportsErrorInsteadOfWarning() {
    String js = "this.foo = 1;";
    Compiler compiler = compileAndTraverse(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS.key, compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testCheckLevelOffReportsNothing() {
    String js = "this.foo = 1;";
    Compiler compiler = compileAndTraverse(js, CheckLevel.OFF);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testShouldTraverseNullParentReturnsTrue() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    Node script = new Node(Token.SCRIPT);
    assertTrue(callback.shouldTraverse(null, script, null));
  }

  @Test(timeout = 4000)
  public void testConstructorJsDocOnFunctionNodeSuppressesWarning() {
    String js = "/** @constructor */ function F() { this.x = 1; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testInterfaceJsDocOnFunctionNodeSuppressesWarning() {
    String js = "/** @interface */ function I() { this.x = 1; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testThisTypeJsDocOnFunctionNodeSuppressesWarning() {
    String js = "/** @this {Object} */ function f() { this.x = 1; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testOverrideJsDocOnFunctionNodeSuppressesWarning() {
    String js = "/** @override */ function f() { this.x = 1; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testConstructorJsDocOnVarDeclarationSuppressesWarning() {
    String js = "/** @constructor */ var F = function() { this.x = 1; };";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testConstructorJsDocOnAssignSuppressesWarning() {
    String js = "var F; /** @constructor */ F = function() { this.x = 1; };";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionWithNonConstructorJsDocStillTraversedAndWarned() {
    String js = "/** @param {number} a */ function f(a) { this.x = a; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInDisallowedCallParentNotTraversed() {
    String js = "foo(function() { this.x = 1; });";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInDisallowedArrayLitParentNotTraversed() {
    String js = "var arr = [function() { this.x = 1; }];";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInDisallowedReturnParentNotTraversed() {
    String js = "function outer() { return function() { this.x = 1; }; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInDisallowedIifeCallNotTraversed() {
    String js = "(function() { this.x = 1; })();";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInAllowedBlockParentWarns() {
    String js = "if (true) { function f() { this.x = 1; } }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInAllowedStringKeyObjectLiteralWarns() {
    String js = "var obj = {'key': function() { this.x = 1; }};";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInAllowedNumberKeyObjectLiteralWarns() {
    String js = "var obj = {1: function() { this.x = 1; }};";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testNestedAssignmentPreservesAssignLhsChild() {
    String js = "(a = this).property = 1;";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertTrue(compiler.getWarningCount() >= 1);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J Failure: CheckGlobalThisTest::testLendsAnnotation3
   * Function lent to prototype via @lends must not trigger JSC_USED_GLOBAL_THIS.
   */
  @Test(timeout = 4000)
  public void testLendsAnnotation3() {
    String js = "function extend() {} " +
        "/** @constructor */ function F() {} " +
        "extend(F.prototype, /** @lends {F.prototype} */ ({ " +
        "  /** @param {string} x */" +
        "  a: function(x) { this.x = x; }" +
        "}));";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals("Unexpected warning when function is lent to prototype via @lends",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testLendsAnnotation1() {
    String js = "/** @constructor */ function F() {} " +
        "/** @param {Function} x */ function f(x) {} " +
        "f(/** @lends {F.prototype} */ ({ " +
        "  a: function() { this.x = 1; } " +
        "}));";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals("Unexpected warning when function is lent to prototype via @lends",
        0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testEvilPatternAssignThisToLocalVariableNotFlagged() {
    String js = "function evil() { var a = this; a.useful = undefined; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testReturnThisNotFlagged() {
    String js = "function f() { return this; }";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testPassThisAsArgumentNotFlagged() {
    String js = "foo(this);";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testStandaloneThisNotFlagged() {
    String js = "this;";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testSubpropertyOfPrototypeMethodWarns() {
    String js = "function Foo() {} Foo.prototype.bar.baz = function() { this.x = 1; };";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testStaticMethodAssignmentWarns() {
    String js = "function Foo() {} Foo.bar = function() { this.x = 1; };";
    Compiler compiler = compileAndTraverse(js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDiagnosticTypeIntegrity() {
    assertNotNull(CheckGlobalThis.GLOBAL_THIS);
    assertEquals("JSC_USED_GLOBAL_THIS", CheckGlobalThis.GLOBAL_THIS.key);
    assertEquals(CheckLevel.WARNING, CheckGlobalThis.GLOBAL_THIS.defaultLevel);
  }

  @Test(timeout = 4000)
  public void testSyntheticAstTransitionsAndAssignLhsChildReset() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);

    Node lhs = Node.newString(Token.NAME, "target");
    Node rhs = new Node(Token.NUMBER);
    Node assign = new Node(Token.ASSIGN, lhs, rhs);

    // Initial shouldTraverse on LHS sets assignLhsChild
    assertTrue(callback.shouldTraverse(null, lhs, assign));

    // Visiting LHS node resets assignLhsChild
    callback.visit(null, lhs, assign);

    // After reset, visiting a return node containing THIS should not trigger a report
    Node thisNode = new Node(Token.THIS);
    Node returnNode = new Node(Token.RETURN, thisNode);
    callback.visit(null, thisNode, returnNode);

    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testSyntheticNonPrototypeGetRhsTraversed() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);

    Node obj = Node.newString(Token.NAME, "Foo");
    Node prop = Node.newString(Token.STRING, "bar");
    Node getprop = new Node(Token.GETPROP, obj, prop);
    Node rhs = new Node(Token.FUNCTION);
    Node assign = new Node(Token.ASSIGN, getprop, rhs);

    // RHS of non-prototype assignment must be traversed
    assertTrue(callback.shouldTraverse(null, rhs, assign));
  }

  @Test(timeout = 4000)
  public void testSyntheticPrototypeGetRhsNotTraversed() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);

    Node obj = Node.newString(Token.NAME, "Foo");
    Node prop = Node.newString(Token.STRING, "prototype");
    Node getprop = new Node(Token.GETPROP, obj, prop);
    Node rhs = new Node(Token.OBJECTLIT);
    Node assign = new Node(Token.ASSIGN, getprop, rhs);

    // RHS of prototype assignment must not be traversed
    assertFalse(callback.shouldTraverse(null, rhs, assign));
  }
}