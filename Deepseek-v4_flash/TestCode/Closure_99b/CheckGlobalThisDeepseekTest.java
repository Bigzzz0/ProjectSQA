package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: CheckGlobalThis.
 *
 * Decision branches targeted:
 * - FUNCTION traversal suppression for @constructor, @this, @override, and
 *   the missing/defective @interface case.
 * - Function parent whitelist: BLOCK, SCRIPT, NAME, ASSIGN; specifically
 *   the missing STRING_KEY / object-literal property-method case.
 * - ASSIGN left-hand-side tracking via assignLhsChild.
 * - ASSIGN right-hand-side suppression for Foo.prototype and
 *   Foo.prototype.subproperty assignments.
 * - THIS report conditions: assignLhsChild != null, or parent is a GETPROP
 *   / GETELEM access.
 * - visit-side clearing of assignLhsChild.
 *
 * The defect-specific tests are:
 * - testPropertyOfMethod: object-literal property methods must report.
 * - testMethod4 and testInterface1: @interface-annotated functions must NOT
 *   report the global-this diagnostic.
 */
public class CheckGlobalThisDeepseekTest {

  private static List<JSError> runCheck(String js) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(js);
    assertNotNull("Failed to parse: " + js, root);

    NodeTraversal.traverse(
        compiler,
        root,
        new CheckGlobalThis(compiler, CheckLevel.WARNING));

    List<JSError> diagnostics = new ArrayList<JSError>();
    diagnostics.addAll(compiler.getErrors());
    diagnostics.addAll(compiler.getWarnings());
    return diagnostics;
  }

  private static void assertGlobalThisDiagnostics(String js, int expected) {
    List<JSError> diagnostics = runCheck(js);
    String message = "Unexpected diagnostics for [" + js + "]: " + diagnostics;
    assertEquals(message, expected, diagnostics.size());
    for (JSError error : diagnostics) {
      assertEquals(message, CheckGlobalThis.GLOBAL_THIS, error.getType());
    }
  }

  @Test(timeout = 4000)
  public void testGlobalThisAssignmentReports() {
    assertGlobalThisDiagnostics("this.x = 1;", 1);
  }

  @Test(timeout = 4000)
  public void testGlobalThisPropertyAccessReports() {
    assertGlobalThisDiagnostics("this.x;", 1);
  }

  @Test(timeout = 4000)
  public void testBareThisDoesNotReport() {
    assertGlobalThisDiagnostics("this;", 0);
  }

  @Test(timeout = 4000)
  public void testAssignmentToVariableDoesNotReport() {
    assertGlobalThisDiagnostics("var a; a = this;", 0);
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationThisAssignmentReports() {
    assertGlobalThisDiagnostics("function f() { this.x = 1; }", 1);
  }

  @Test(timeout = 4000)
  public void testNestedFunctionDeclarationThisAssignmentReports() {
    assertGlobalThisDiagnostics(
        "function f() { function g() { this.x = 1; } }", 1);
  }

  @Test(timeout = 4000)
  public void testVarFunctionThisAssignmentReports() {
    assertGlobalThisDiagnostics("var f = function() { this.x = 1; };", 1);
  }

  @Test(timeout = 4000)
  public void testConstructorFunctionNoReport() {
    assertGlobalThisDiagnostics(
        "/** @constructor */ function Foo() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testThisAnnotatedFunctionNoReport() {
    assertGlobalThisDiagnostics(
        "/** @this {Foo} */ function f() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testThisAnnotationOnAssignmentNoReport() {
    assertGlobalThisDiagnostics(
        "/** @this {Foo} */ a.b = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testVarConstructorAnnotationNoReport() {
    assertGlobalThisDiagnostics(
        "/** @constructor */ var Foo = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testOverrideAnnotatedFunctionNoReport() {
    assertGlobalThisDiagnostics(
        "/** @override */ function f() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testInterfaceFunctionNoReport() {
    assertGlobalThisDiagnostics(
        "/** @interface */ function I() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testMethod4() {
    assertGlobalThisDiagnostics(
        "/** @interface */ function method4() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testInterface1() {
    assertGlobalThisDiagnostics(
        "/** @interface */ function I() { this.x = 1; }", 0);
  }

  @Test(timeout = 4000)
  public void testPropertyOfMethod() {
    assertGlobalThisDiagnostics(
        "var a = { method: function() { this.x = 1; } };", 1);
  }

  @Test(timeout = 4000)
  public void testQuotedPropertyOfMethod() {
    assertGlobalThisDiagnostics(
        "var object = { 'method': function() { this.x = 1; } };", 1);
  }

  @Test(timeout = 4000)
  public void testPropertyMethodAssignmentReports() {
    assertGlobalThisDiagnostics(
        "var a = {}; a.b = function() { this.c = 1; };", 1);
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodNoReport() {
    assertGlobalThisDiagnostics(
        "Foo.prototype.bar = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testPrototypeSubPropertyMethodNoReport() {
    assertGlobalThisDiagnostics(
        "Foo.prototype.bar.baz = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testAssignmentToPrototypeNoReport() {
    assertGlobalThisDiagnostics(
        "Foo.prototype = function() { this.x = 1; };", 0);
  }

  @Test(timeout = 4000)
  public void testNestedAssignmentLhsThisReports() {
    assertGlobalThisDiagnostics("var a; (a = this).x = 1;", 1);
  }

  @Test(timeout = 4000)
  public void testCallArgumentFunctionDoesNotReport() {
    assertGlobalThisDiagnostics("foo(function() { this.x = 1; });", 0);
  }

  @Test(timeout = 4000)
  public void testReturnThisInsideFunctionDoesNotReport() {
    assertGlobalThisDiagnostics("function f() { return this; }", 0);
  }
}