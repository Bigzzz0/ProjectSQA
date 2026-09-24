package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeCheckDeepseekTest {

  /*
   * [Branch & Defect Analysis Matrix]
   *
   * Targeted branches:
   * - TypeCheck constructors (3-arg and public delegating constructor).
   * - reportMissingProperties chaining and getTypedPercent initial state.
   * - process() null precondition failure path.
   * - visitName, visitGetProp, visitGetElem, visitVar, visitNew, visitCall,
   *   visitReturn, visitAssign, visitBinaryOperator, visitFunction.
   * - Wrong-argument-count reporting for too few, too many, and constructor calls.
   * - @notypecheck suppression path in checkNoTypeCheckSection.
   * - BAD_DELETE, NOT_CALLABLE, NOT_A_CONSTRUCTOR, CONSTRUCTOR_NOT_CALLABLE.
   * - Enum initializer validation, in/instanceof object checks.
   *
   * Defect-targeted regression:
   * - TypeCheckTest.testFunctionArguments16 expects a warning when a typed
   *   function is invoked through Function.prototype.call with too many
   *   arguments. The defective TypeCheck does not unwrap .call and therefore
   *   misses WRONG_ARGUMENT_COUNT.
   */

  private Result compile(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    List<SourceFile> externs = new ArrayList<SourceFile>();
    List<SourceFile> inputs = new ArrayList<SourceFile>();
    inputs.add(SourceFile.fromCode("test.js", js));
    return compiler.compile(externs, inputs, options);
  }

  private void assertWarning(String js, DiagnosticType expected) {
    Result result = compile(js);
    assertTrue("expected a warning but got errors: " + Arrays.toString(result.errors),
        result.warnings.length > 0);
    assertEquals("wrong diagnostic type; warnings=" + Arrays.toString(result.warnings),
        expected, result.warnings[0].getType());
  }

  private void assertWarningPresent(String js) {
    Result result = compile(js);
    assertTrue("expected a warning but got errors: " + Arrays.toString(result.errors),
        result.warnings.length > 0);
  }

  private void assertNoWarning(String js) {
    Result result = compile(js);
    assertTrue("unexpected errors: " + Arrays.toString(result.errors),
        result.errors.length == 0);
    assertEquals("unexpected warnings: " + Arrays.toString(result.warnings),
        0, result.warnings.length);
  }

  private TypeCheck createTypeCheck() {
    Compiler compiler = new Compiler();
    return new TypeCheck(compiler,
        new DefaultReverseAbstractInterpreter(compiler.getTypeRegistry()),
        compiler.getTypeRegistry());
  }

  @Test(timeout = 4000)
  public void testCoreStatementsAndExpressions() {
    assertNoWarning(
        "var n = 1;\n" +
        "var b = true;\n" +
        "var s = 'str';\n" +
        "var r = /x/;\n" +
        "var a = [1, 2];\n" +
        "var o = {a: 1};\n" +
        "var p = o.a;\n" +
        "function f(x, y) { return x + y; }\n" +
        "var z = f(1, 2);");
  }

  @Test(timeout = 4000)
  public void testBinaryAndComparisonOperators() {
    assertNoWarning(
        "var a = 1;\n" +
        "var b = 2;\n" +
        "var c = a + b;\n" +
        "var d = a - b;\n" +
        "var e = a * b;\n" +
        "var f = a / b;\n" +
        "var g = a % b;\n" +
        "var h = a << b;\n" +
        "var i = a & b;\n" +
        "var j = a | b;\n" +
        "var k = a ^ b;\n" +
        "var l = a < b;\n" +
        "var m = a <= b;\n" +
        "var n = a > b;\n" +
        "var o = a >= b;");
  }

  @Test(timeout = 4000)
  public void testUnaryOperatorsAndIncrement() {
    assertNoWarning(
        "var a = 1;\n" +
        "var b = !a;\n" +
        "var c = ~a;\n" +
        "var d = -a;\n" +
        "var e = +a;\n" +
        "var f = typeof a;\n" +
        "var g = void a;\n" +
        "a++;\n" +
        "--a;");
  }

  @Test(timeout = 4000)
  public void testGetPropGetElemAndAssignment() {
    assertNoWarning(
        "var o = {a: 1, b: 2};\n" +
        "var x = o.a;\n" +
        "var y = o['b'];\n" +
        "o.a = 3;\n" +
        "var z = x + y;");
  }

  @Test(timeout = 4000)
  public void testControlFlowStatements() {
    assertNoWarning(
        "function f() { return 1; }\n" +
        "if (f()) { var x = 1; } else { x = 2; }\n" +
        "while (x < 3) { x++; }\n" +
        "for (var i = 0; i < 10; i++) { x += i; }\n" +
        "switch (x) { case 1: break; default: break; }");
  }

  @Test(timeout = 4000)
  public void testNoWarningValidFunctionCalls() {
    assertNoWarning(
        "function f(a, b) { return a; }\n" +
        "f(1, 2);");
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountTooFew() {
    assertWarning(
        "/** @param {number} x */\n" +
        "function f(x) {}\n" +
        "f();",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountTooMany() {
    assertWarning(
        "/** @param {number} x */\n" +
        "function f(x) {}\n" +
        "f(1, 2);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountOnNew() {
    assertWarning(
        "/** @constructor\n" +
        " * @param {number} x\n" +
        " */\n" +
        "function Foo(x) {}\n" +
        "new Foo(1, 2);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testOptionalArgumentsDoNotWarn() {
    assertNoWarning(
        "/** @param {number=} x */\n" +
        "function f(x) {}\n" +
        "f();\n" +
        "f(1);");
  }

  @Test(timeout = 4000)
  public void testVarArgsDoNotWarn() {
    assertNoWarning(
        "/** @param {...number} x */\n" +
        "function f(x) {}\n" +
        "f(1, 2, 3);");
  }

  @Test(timeout = 4000)
  public void testDeleteReferenceWarning() {
    assertWarning("delete 1;", TypeCheck.BAD_DELETE);
  }

  @Test(timeout = 4000)
  public void testNotCallableWarning() {
    assertWarning("var x = 1; x();", TypeCheck.NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testNotAConstructorWarning() {
    assertWarning("var x = 1; new x();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

  @Test(timeout = 4000)
  public void testConstructorCalledWithoutNewWarning() {
    assertWarning(
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "Foo();",
        TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testFunctionArguments16() {
    // Defect-targeted: .call unwrapping must validate the target function's
    // argument list. The buggy TypeCheck misses WRONG_ARGUMENT_COUNT here.
    Result result = compile(
        "/** @param {number} x */\n" +
        "function f(x) { return x; }\n" +
        "f.call(null, 1, 2);");
    assertTrue("expected a warning", result.warnings.length > 0);
    assertEquals("expected wrong-argument-count warning",
        TypeCheck.WRONG_ARGUMENT_COUNT, result.warnings[0].getType());
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckSuppressesWarnings() {
    assertNoWarning(
        "/** @notypecheck */\n" +
        "function f() {\n" +
        "  var x = 1;\n" +
        "  x();\n" +
        "}");
  }

  @Test(timeout = 4000)
  public void testEnumInitializerBadElement() {
    assertWarningPresent(
        "/** @enum {number} */\n" +
        "var E = {A: 1, B: 'x'};");
  }

  @Test(timeout = 4000)
  public void testInOperatorRequiresObject() {
    assertWarningPresent("var x = 'a' in 1;");
  }

  @Test(timeout = 4000)
  public void testInstanceofRequiresObject() {
    assertWarningPresent("var x = {} instanceof 1;");
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesChaining() {
    TypeCheck typeCheck = createTypeCheck();
    assertSame(typeCheck, typeCheck.reportMissingProperties(true));
    assertSame(typeCheck, typeCheck.reportMissingProperties(false));
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentInitiallyZero() {
    TypeCheck typeCheck = createTypeCheck();
    assertEquals(0.0, typeCheck.getTypedPercent(), 0.0);
  }

  @Test(timeout = 4000)
  public void testProcessNullArgumentsFails() {
    TypeCheck typeCheck = createTypeCheck();
    try {
      typeCheck.process(null, null);
      fail("Expected NullPointerException from process(null, null)");
    } catch (NullPointerException expected) {
      // Expected defensive precondition failure.
    }
  }
}