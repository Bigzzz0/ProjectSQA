package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/*
 * [Branch & Defect Analysis Matrix]
 * ============================================================================================
 * Target Class: CheckGlobalThis (Defects4J Closure Compiler)
 *
 * Decision / Condition Coverage Targets:
 * 1. shouldTraverse() - Token.FUNCTION branch:
 *    - jsDoc != null && (isConstructor || isInterface || hasThisType || isOverride) -> false
 *    - jsDoc != null && !isConstructor && !isInterface && !hasThisType && !isOverride -> continue
 *    - jsDoc == null -> continue
 *    - Parent type checks: Token.BLOCK, Token.SCRIPT, Token.NAME, Token.ASSIGN -> true
 *    - Parent type other (e.g., Token.CALL, Token.ARRAYLIT) -> false
 *    - Known Defect (Issue 182): Token.STRING / ObjectLiteral key parent -> should traverse!
 *
 * 2. shouldTraverse() - Token.ASSIGN branch:
 *    - parent == null / parent != Token.ASSIGN -> continue
 *    - n == lhs: assignLhsChild == null (set) vs assignLhsChild != null (nested, keep outer)
 *    - n != lhs (rhs): NodeUtil.isGet(lhs)
 *      - lhs.getType() == Token.GETPROP && lhs.getLastChild() == "prototype" -> false
 *      - llhs.getType() == Token.GETPROP && llhs.getLastChild() == "prototype" -> false
 *      - Otherwise (non-prototype GETPROP or non-GET) -> true
 *
 * 3. visit() & shouldReportThis():
 *    - n.getType() == Token.THIS
 *      - assignLhsChild != null (LHS of assignment) -> report
 *      - parent != null && NodeUtil.isGet(parent) (e.g. this.prop, this[0]) -> report
 *      - standalone/safe this (e.g. return this, foo(this), var a = this) -> do not report
 *    - n == assignLhsChild: reset assignLhsChild to null
 *
 * 4. getFunctionJsDocInfo():
 *    - n.getJSDocInfo() != null
 *    - n.getJSDocInfo() == null:
 *      - parent.getType() == Token.ASSIGN -> parent.getJSDocInfo()
 *      - parent.getType() == Token.NAME -> parent.getJSDocInfo()
 *        - if null and gramps.getType() == Token.VAR -> gramps.getJSDocInfo()
 *
 * 5. Ground Truth Defects (Closure Issue 182 / RuntimeTypeCheckTest):
 *    - Object literal property functions: var a = {x: function() { this.foo = 3; }};
 *    - Expected: 1 warning/error. Defective version returns false on shouldTraverse -> 0 errors.
 * ============================================================================================
 */
public class CheckGlobalThisGeminiTest {

  private Compiler testCompile(String js, CheckLevel level) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis pass = new CheckGlobalThis(compiler, level);
    NodeTraversal.traverse(compiler, root, pass);
    return compiler;
  }

  // ==========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==========================================================================

  @Test(timeout = 4000)
  public void testGlobalThisInFunctionDeclarationReportsError() {
    String js = "function dangerous() { this.foo = 1; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testGlobalThisInVarFunctionExpressionReportsError() {
    String js = "var f = function() { this.a = 2; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testGlobalThisInAssignmentExpressionReportsError() {
    String js = "f = function() { this.a = 2; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalThisInBlockScopedFunctionReportsError() {
    String js = "{ function inner() { this.val = 42; } }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalThisOnLhsOfAssignmentReportsError() {
    String js = "this.foo = 5;";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalThisPropertyReadReportsError() {
    String js = "var a = this.foo;";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalThisElementAccessReportsError() {
    String js = "var a = this['bar'];";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNestedAssignmentOnLhsMaintainsOuterAssignChild() {
    // (a = this).property = 123;
    // Outer assign LHS is GETPROP ((a = this).property).
    // Inner assign is (a = this).
    String js = "(a = this).property = 123;";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testSafeThisAssignmentToVariableDoesNotReport() {
    String js = "var a = this;";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testSafeThisPassedToFunctionDoesNotReport() {
    String js = "foo(this);";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testSafeThisInReturnStatementDoesNotReport() {
    String js = "function getThis() { return this; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testSafeThisInIfConditionDoesNotReport() {
    String js = "if (this) { var x = 1; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testWarningCheckLevelReportsWarningInsteadOfError() {
    String js = "this.foo = 1;";
    Compiler compiler = testCompile(js, CheckLevel.WARNING);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(1, compiler.getWarningCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testOffCheckLevelDoesNotReport() {
    String js = "this.foo = 1;";
    Compiler compiler = testCompile(js, CheckLevel.OFF);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // ==========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Prototype Assign Traversal
  // ==========================================================================

  @Test(timeout = 4000)
  public void testPrototypeAssignmentRhsIsNotTraversed() {
    // Foo.prototype = { method: function() { this.x = 1; } };
    String js = "Foo.prototype = { method: function() { this.x = 1; } };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testPrototypePropertyAssignmentRhsIsNotTraversed() {
    // Foo.prototype.bar = function() { this.x = 1; };
    String js = "Foo.prototype.bar = function() { this.x = 1; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testPrototypeGetElemAssignmentRhsIsNotTraversed() {
    // Foo.prototype['bar'] = function() { this.x = 1; };
    String js = "Foo.prototype['bar'] = function() { this.x = 1; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testSubpropertyOfPrototypeMethodIsTraversedAndReported() {
    // Foo.prototype.bar.baz = function() { this.x = 1; };
    // Here llhs is Foo.prototype.bar, llhs.getLastChild() is "bar" != "prototype"
    String js = "Foo.prototype.bar.baz = function() { this.x = 1; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNonPrototypeStaticMethodReportsError() {
    // Foo.bar = function() { this.x = 1; };
    String js = "Foo.bar = function() { this.x = 1; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInsideCallArgumentNotTraversed() {
    // Function passed directly to a function call (parent is CALL, not BLOCK/SCRIPT/NAME/ASSIGN)
    String js = "setTimeout(function() { this.x = 1; }, 100);";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInsideArrayLiteralNotTraversed() {
    // Function inside array literal (parent is ARRAYLIT)
    String js = "var arr = [function() { this.x = 1; }];";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testImmediatelyInvokedFunctionExpressionNotTraversed() {
    // IIFE (parent is CALL)
    String js = "(function() { this.x = 1; })();";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  // ==========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 182 Ground Truth)
  // ==========================================================================

  /**
   * Targets Defects4J ground truth: CheckGlobalThisTest::testIssue182a
   * Functions defined in object literals with unquoted keys must be inspected.
   */
  @Test(timeout = 4000)
  public void testIssue182a() {
    String js = "var a = {x: function() { this.foo = 3; }};";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals("Expected 1 error for global this in object literal property function",
        1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  /**
   * Targets Defects4J ground truth: CheckGlobalThisTest::testIssue182b
   * Functions defined in object literals with string keys must be inspected.
   */
  @Test(timeout = 4000)
  public void testIssue182b() {
    String js = "var a = {'x': function() { this.foo = 3; }};";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals("Expected 1 error for global this in object literal with quoted key",
        1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  /**
   * Additional defect variant: Object literal assigned directly in expression.
   */
  @Test(timeout = 4000)
  public void testIssue182cObjectLiteralInAssignment() {
    String js = "a = {x: function() { this.foo = 3; }};";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals("Expected 1 error for global this in object literal assigned to variable",
        1, compiler.getErrorCount());
  }

  /**
   * Additional defect variant: Object literal with numeric property key.
   */
  @Test(timeout = 4000)
  public void testIssue182NumericKeyInObjectLiteral() {
    String js = "var a = {1: function() { this.foo = 3; }};";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals("Expected 1 error for global this in object literal with numeric key",
        1, compiler.getErrorCount());
  }

  /**
   * Targets Defects4J ground truth: RuntimeTypeCheckTest::testValueWithInnerFn
   * Function returning an object literal containing a method using global this.
   */
  @Test(timeout = 4000)
  public void testValueWithInnerFn() {
    String js = "function f() { return { foo: function() { this.bar = 1; } }; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  // ==========================================================================
  // Partition D: JSDoc Annotations & Defensive Paths
  // ==========================================================================

  @Test(timeout = 4000)
  public void testConstructorFunctionDeclarationSuppressesWarning() {
    String js = "/** @constructor */ function Foo() { this.x = 1; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testInterfaceFunctionDeclarationSuppressesWarning() {
    String js = "/** @interface */ function IFoo() { this.x = 1; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testThisTypeAnnotationSuppressesWarning() {
    String js = "/** @this {MyType} */ function helper() { this.x = 1; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testOverrideAnnotationSuppressesWarning() {
    String js = "/** @override */ function overridden() { this.x = 1; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testConstructorAnnotationOnVarDeclarationSuppressesWarning() {
    String js = "/** @constructor */ var Foo = function() { this.x = 1; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testConstructorAnnotationOnAssignmentSuppressesWarning() {
    String js = "/** @constructor */ Foo = function() { this.x = 1; };";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNonSuppressionJsDocStillReportsError() {
    String js = "/** @param {number} x */ function f(x) { this.x = x; }";
    Compiler compiler = testCompile(js, CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  // ==========================================================================
  // Partition E: Synthetic Node Scenarios & Diagnostic Integrity
  // ==========================================================================

  @Test(timeout = 4000)
  public void testDiagnosticTypeMetadata() {
    assertEquals("JSC_USED_GLOBAL_THIS", CheckGlobalThis.GLOBAL_THIS.key);
    assertNotNull(CheckGlobalThis.GLOBAL_THIS.format);
  }

  @Test(timeout = 4000)
  public void testDirectTraversalCallbackWithNonTargetNodes() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.ERROR);

    Node emptyNode = new Node(Token.EMPTY);
    Node exprResult = new Node(Token.EXPR_RESULT);

    // Non-function, non-assign parent should traverse
    assertTrue(callback.shouldTraverse(null, emptyNode, null));
    assertTrue(callback.shouldTraverse(null, emptyNode, exprResult));

    // Visiting non-THIS node does not throw or report
    callback.visit(null, emptyNode, exprResult);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDirectVisitOnThisWithoutParentDoesNotReport() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.ERROR);

    Node thisNode = new Node(Token.THIS);
    callback.visit(null, thisNode, null);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAssignLhsResetWhenVisited() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.ERROR);

    Node assign = new Node(Token.ASSIGN);
    Node thisNode = new Node(Token.THIS);
    Node numberNode = new Node(Token.NUMBER);
    assign.addChildToBack(thisNode);
    assign.addChildToBack(numberNode);

    // Traverse LHS
    assertTrue(callback.shouldTraverse(null, thisNode, assign));

    // Visit THIS on LHS (should report)
    NodeTraversal t = new NodeTraversal(compiler, callback);
    callback.visit(t, thisNode, assign);
    assertEquals(1, compiler.getErrorCount());

    // After LHS visited, assignLhsChild is reset to null
    // Subsequent standalone THIS without parent should not report
    Node anotherThis = new Node(Token.THIS);
    callback.visit(t, anotherThis, null);
    assertEquals(1, compiler.getErrorCount());
  }
}