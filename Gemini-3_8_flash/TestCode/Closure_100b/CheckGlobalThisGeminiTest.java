/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.CheckGlobalThis
 * 
 * Branches & Conditions Targeted:
 * 1. shouldTraverse(NodeTraversal t, Node n, Node parent):
 *    - n.getType() == Token.FUNCTION:
 *      * jsDoc != null && jsDoc.isConstructor() -> returns false (skips constructor functions).
 *      * jsDoc != null && jsDoc.hasThisType() -> returns false (skips functions annotated with @this).
 *      * jsDoc != null && (!isConstructor() && !hasThisType()) -> returns true (e.g. @param / @type).
 *      * jsDoc == null -> falls through to check parent/gramps jsDoc sources.
 *      * JSDoc resolution fallback paths in getFunctionJsDocInfo:
 *        - parent.getType() == Token.NAME && parent.getJSDocInfo() != null
 *        - parent.getType() == Token.NAME && parent.getJSDocInfo() == null && gramps.getType() == Token.VAR
 *        - parent.getType() == Token.NAME && parent.getJSDocInfo() == null && gramps.getType() != Token.VAR
 *        - parent.getType() == Token.ASSIGN && parent.getJSDocInfo() != null
 *        - parent is neither NAME nor ASSIGN (e.g., EXPR_RESULT, CALL, ARRAYLIT).
 *    - parent != null && parent.getType() == Token.ASSIGN:
 *      * n == lhs && assignLhsChild == null -> assignLhsChild set to lhs.
 *      * n == lhs && assignLhsChild != null -> nested assignments preserve first assignLhsChild.
 *      * n != lhs (RHS of assignment):
 *        - lhs.getType() == Token.GETPROP && lhs.getLastChild().getString().equals("prototype") -> returns false.
 *        - lhs.getType() == Token.GETPROP && lhs.getQualifiedName() contains ".prototype." -> returns false.
 *        - lhs.getType() == Token.GETPROP && !contains(".prototype.") && last != "prototype" -> returns true.
 *        - lhs.getType() == Token.GETPROP && lhs.getQualifiedName() == null (dynamic LHS) -> returns true.
 *        - lhs.getType() != Token.GETPROP (e.g. Token.NAME) -> returns true.
 *    - parent == null (root AST node) -> returns true.
 *    - parent.getType() != Token.ASSIGN -> returns true.
 *
 * 2. visit(NodeTraversal t, Node n, Node parent):
 *    - n.getType() == Token.THIS && shouldReportThis(n, parent) -> compiler.report(...) invoked.
 *    - n.getType() == Token.THIS && !shouldReportThis(n, parent) -> no report.
 *    - n.getType() != Token.THIS -> no report.
 *    - n == assignLhsChild -> resets assignLhsChild to null.
 *    - n != assignLhsChild -> assignLhsChild unaffected.
 *
 * 3. shouldReportThis(Node n, Node parent):
 *    - assignLhsChild != null -> returns true.
 *    - assignLhsChild == null -> returns false (DEFECT ZONE: Property reads like 'var a = this.foo'
 *      or reads of global this incorrectly return false).
 *
 * 4. Defects4J Defect Ground Truth Trigger:
 *    - testGlobalThis7: Reads of global 'this' properties outside assignments: `var a = this.foo;`
 *      Ground truth failure: expected:<1> but was:<0>.
 *    - testStaticMethod2: Methods assigned to object properties: `a.b = function() { this.a = 1; };`
 *      Ground truth failure: expected:<0> but was:<1>.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckGlobalThisGeminiTest {

  private Compiler compileCheck(String js, CheckLevel level) {
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
  public void testGlobalThisLhsAssignmentReported() {
    Compiler compiler = compileCheck("this.foo = 5;", CheckLevel.ERROR);
    assertEquals("Global this property assignment must trigger an error", 1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS.key, compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testGlobalThisElementAssignmentReported() {
    Compiler compiler = compileCheck("this['foo'] = 5;", CheckLevel.ERROR);
    assertEquals("Global this element assignment must trigger an error", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testConstructorFunctionDoesNotTraverse() {
    String js = "/** @constructor */ function F() { this.foo = 1; }";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Constructors should not flag this keyword", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionWithThisAnnotationDoesNotTraverse() {
    String js = "/** @this {Object} */ function f() { this.foo = 1; }";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Functions with @this annotation should not flag this", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testVarWithConstructorJSDocDoesNotTraverse() {
    String js = "/** @constructor */ var F = function() { this.foo = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Var assigned function with @constructor should not flag this", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testVarWithThisJSDocDoesNotTraverse() {
    String js = "/** @this {Object} */ var f = function() { this.foo = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Var assigned function with @this should not flag this", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAssignWithConstructorJSDocDoesNotTraverse() {
    String js = "/** @constructor */ F = function() { this.foo = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("ASSIGN expression with @constructor should not flag this", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAssignWithThisJSDocDoesNotTraverse() {
    String js = "/** @this {Object} */ F = function() { this.foo = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("ASSIGN expression with @this should not flag this", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testPrototypePropertyAssignmentDoesNotTraverseRhs() {
    String js = "MyClass.prototype.method = function() { this.foo = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Assignments to .prototype.* should not traverse RHS", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDirectPrototypeObjectAssignmentDoesNotTraverseRhs() {
    String js = "MyClass.prototype = { method: function() { this.foo = 1; } };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Assignments directly to .prototype should not traverse RHS", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAssignToNameTraversesRhsFunction() {
    String js = "a = function() { this.x = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Assignment to a simple name should traverse RHS", 1, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Structural Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptProducesNoErrors() {
    Compiler compiler = compileCheck("", CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testScriptWithoutThisKeyword() {
    Compiler compiler = compileCheck("var x = 1; var y = x + 2;", CheckLevel.ERROR);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNestedAssignPreservesAssignLhsChild() {
    // ((a = this).property) = c; tests nested assignment where assignLhsChild is already set
    String js = "(a = this).property = 1;";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Inner this on nested assignment LHS should be flagged", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionWithNonConstructorNonThisJSDocTraverses() {
    String js = "/** @param {number} x */ function f(x) { this.x = x; }";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Function with ordinary JSDoc (@param) must be traversed", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testVarWithNonConstructorNonThisJSDocTraverses() {
    String js = "/** @type {Function} */ var f = function() { this.x = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Var with @type JSDoc should not suppress global this check", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDynamicLhsPropertyAccessQualifiedNameNull() {
    // In (getObj()).prop = function() { this.x = 1; }, lhs.getQualifiedName() is null
    String js = "(function(){ return {}; })().prop = function() { this.x = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Assignment to computed property should traverse RHS", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNonPrototypeSubpropertyWithPrototypeSubstringInName() {
    // Qualified name contains 'prototype' but NOT as '.prototype.'
    String js = "obj.my_prototype_prop.x = function() { this.val = 1; };";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Property containing prototype substring must still traverse RHS", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCheckLevelWarningReportsWarningsInsteadOfErrors() {
    Compiler compiler = compileCheck("this.foo = 5;", CheckLevel.WARNING);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(1, compiler.getWarningCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS.key, compiler.getWarnings()[0].getType().key);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J Failure: CheckGlobalThisTest::testGlobalThis7
   * Defect: shouldReportThis returns false for property reads:
   *   // Also report a THIS with a property access.
   *   return false;
   * Expected: 1 error. Defective behavior: 0 errors.
   */
  @Test(timeout = 4000)
  public void testDefectGlobalThis7() {
    Compiler compiler = compileCheck("var a = this.foo;", CheckLevel.ERROR);
    assertEquals("Reading global this property ('var a = this.foo;') must be flagged",
        1, compiler.getErrorCount());
  }

  /**
   * Targets Defects4J Failure: CheckGlobalThisTest::testStaticMethod2
   * Defect: Assigning a function to an object property is a static method and should not flag this.
   * Expected: 0 errors. Defective behavior: 1 error.
   */
  @Test(timeout = 4000)
  public void testDefectStaticMethod2() {
    Compiler compiler = compileCheck("a.b = function() { this.a = 1; };", CheckLevel.ERROR);
    assertEquals("Static method assignment should not flag dangerous global this",
        0, compiler.getErrorCount());
  }

  /**
   * Targets Defects4J Failure: CheckGlobalThisTest::testStaticFunction6
   * Defect: Nested function inside a method.
   * Expected: 0 errors. Defective behavior: 1 error.
   */
  @Test(timeout = 4000)
  public void testDefectStaticFunction6() {
    String js = "function a() { return function() { this.x = 1; }; }";
    Compiler compiler = compileCheck(js, CheckLevel.ERROR);
    assertEquals("Static function return should not trigger global this error",
        0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition D: White-Box Node & Callback Invocations
  // =========================================================================

  @Test(timeout = 4000)
  public void testDirectShouldTraverseRootAndNonAssignParent() {
    Compiler compiler = new Compiler();
    CheckGlobalThis checker = new CheckGlobalThis(compiler, CheckLevel.ERROR