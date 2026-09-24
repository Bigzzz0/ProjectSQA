package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.javascript.rhino.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: CheckGlobalThis
 *
 * Decision branches exercised:
 *  - shouldTraverse(FUNCTION): constructor/@this JSDoc short-circuit; function
 *    RHS of a prototype-assignment skip; ordinary function traversal.
 *  - shouldTraverse(ASSIGN LHS/RHS): assignLhsChild state maintenance and the
 *    GETPROP prototype / ".prototype." subproperty guards.
 *  - visit(THIS): reporting when assignLhsChild is active; shouldReportThis
 *    currently fails to report bare property access on `this`.
 *  - getFunctionJsDocInfo: JSDoc discovered on FUNCTION, NAME, VAR and ASSIGN.
 *
 * Defect-targeted cases:
 *  - Static methods are incorrectly reported when their own `this` is used on
 *    the LHS of an assignment (testStaticFunction6/7, testStaticMethod2/3).
 *  - Property accesses such as `this.foo` are not reported (testGlobalThis7).
 *  - Safe outer contexts (constructors, @this functions, prototype methods)
 *    suppress traversal of inner functions, missing inner-function `this`
 *    reports (testInnerFunction1/2/3, testStaticFunction8).
 */
public class CheckGlobalThisDeepseekTest {

  @Test(timeout = 4000)
  public void testGlobalThisAssignmentReports() {
    assertWarningCount("this.x = 1;", 1);
  }

  @Test(timeout = 4000)
  public void testGlobalThisPropertyAccessReports() {
    assertWarningCount("this.foo;", 1);
  }

  @Test(timeout = 4000)
  public void testBareThisIsNotReported() {
    assertWarningCount("var x = this;", 0);
  }

  @Test(timeout = 4000)
  public void testOrdinaryFunctionThisAssignmentReports() {
    assertWarningCount("function f() { this.x = 1; }", 1);
  }

  @Test(timeout = 4000)
  public void testVarAssignedFunctionThisAssignmentReports() {
    assertWarningCount("var f = function() { this.x = 1; };", 1);
  }

  @Test(timeout = 4000)
  public void testConstructorFunctionThisAssignmentSafe() {
    assertWarningCount("/** @constructor */ function Foo() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testConstructorVarFunctionThisAssignmentSafe() {
    assertWarningCount("/** @constructor */ var Foo = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testThisAnnotatedFunctionThisAssignmentSafe() {
    assertWarningCount("/** @this {Foo} */ function f() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testThisAnnotatedAssignedFunctionSafe() {
    assertWarningCount("/** @this {Foo} */ Foo.bar = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodDirectThisAssignmentSafe() {
    assertWarningCount(
        "function Foo() {} Foo.prototype.bar = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodDirectPropertyAccessSafe() {
    assertWarningCount(
        "function Foo() {} Foo.prototype.bar = function() { return this.x; };", 0);
  }

  @Test(timeout = 4000)
  public void testPrototypeSubpropertyMethodDirectThisAssignmentSafe() {
    assertWarningCount(
        "function Foo() {} Foo.prototype.ns.bar = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testStaticMethodDirectThisAssignmentSafe() {
    assertWarningCount(
        "var Foo = {}; Foo.bar = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testStaticMethodDirectPropertyAccessSafe() {
    assertWarningCount(
        "var Foo = {}; Foo.bar = function() { return this.x; };", 0);
  }

  @Test(timeout = 4000)
  public void testStaticMethodDeepPropertyAssignmentSafe() {
    assertWarningCount(
        "var Foo = {}; Foo.ns.bar = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testStaticMethodInnerFunctionAssignmentReports() {
    assertWarningCount(
        "var Foo = {}; Foo.bar = function() { var inner = function() { this.x = 1; }; };",
        1);
  }

  @Test(timeout = 4000)
  public void testStaticMethodInnerFunctionPropertyAccessReports() {
    assertWarningCount(
        "var Foo = {}; Foo.bar = function() { var inner = function() { return this.x; }; };",
        1);
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodInnerFunctionAssignmentReports() {
    assertWarningCount(
        "function Foo() {} Foo.prototype.bar = function() { var inner = function() { this.x = 1; }; };",
        1);
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodInnerFunctionPropertyAccessReports() {
    assertWarningCount(
        "function Foo() {} Foo.prototype.bar = function() { var inner = function() { return this.x; }; };",
        1);
  }

  @Test(timeout = 4000)
  public void testConstructorInnerFunctionAssignmentReports() {
    assertWarningCount(
        "/** @constructor */ function Foo() { var inner = function() { this.x = 1; }; }",
        1);
  }

  @Test(timeout = 4000)
  public void testThisAnnotatedInnerFunctionPropertyAccessReports() {
    assertWarningCount(
        "/** @this {Foo} */ function f() { var inner = function() { return this.x; }; }",
        1);
  }

  @Test(timeout = 4000)
  public void testEmptyFunctionHasNoWarning() {
    assertWarningCount("function f() {}", 0);
  }

  private static void assertWarningCount(String js, int expected) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    assertNotNull("Compiler could not parse: " + js, root);

    CheckGlobalThis check = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal.traverse(compiler, root, check);

    List<JSError> globalThisErrors = new ArrayList<>();
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == CheckGlobalThis.GLOBAL_THIS) {
        globalThisErrors.add(error);
      }
    }

    assertEquals(
        "Unexpected GLOBAL_THIS warning count for: " + js,
        expected,
        globalThisErrors.size());
  }
}