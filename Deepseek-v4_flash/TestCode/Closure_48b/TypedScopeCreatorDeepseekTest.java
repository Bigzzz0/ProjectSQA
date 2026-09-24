package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TypedScopeCreatorDeepSearchTest {

  private JSError[] check(String source) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(source);

    JSTypeRegistry registry = new JSTypeRegistry(compiler.getErrorReporter());
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    TypeCheck typeCheck = new TypeCheck(compiler, registry, scopeCreator);
    typeCheck.processForTesting(null, root);

    return compiler.getErrors();
  }

  private void assertNoErrors(String source) {
    JSError[] errors = check(source);
    assertEquals("Expected no errors, got: " + Arrays.toString(errors),
        0, errors.length);
  }

  private void assertError(String source, String messagePart) {
    JSError[] errors = check(source);
    for (JSError error : errors) {
      if (error.getDescription().contains(messagePart)) {
        return;
      }
    }
    fail("Expected error containing '" + messagePart + "', got: "
        + Arrays.toString(errors));
  }

  @Test
  public void testIssue586PrototypeObjectLiteralPropertyType() {
    JSError[] errors = check(
        "/** @constructor */ function Foo() {}"
        + "Foo.prototype = { /** @type {number} */ bar: 1 };"
        + "var f = new Foo();"
        + "f.bar = 'a';");

    for (JSError error : errors) {
      if (error.getDescription().contains("assignment")) {
        return;
      }
    }
    fail("Expected assignment warning for f.bar, got: "
        + Arrays.toString(errors));
  }

  @Test
  public void testScopeCreatorDirect() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode("/** @constructor */ function Foo() {}");

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope scope = creator.createScope(root, null);

    Var foo = scope.getVar("Foo");
    assertNotNull(foo);
    assertTrue(foo.getType().isConstructor());
  }

  @Test
  public void testConstructor() {
    assertNoErrors("/** @constructor */ function Foo() {} var f = new Foo();");
  }

  @Test
  public void testInterface() {
    assertNoErrors("/** @interface */ function Foo() {}");
  }

  @Test
  public void testEnum() {
    assertNoErrors("/** @enum {number} */ var E = {A: 1}; var x = E.A;");
  }

  @Test
  public void testTypedef() {
    assertNoErrors("/** @typedef {number} */ var MyNumber; var x = 1;");
  }

  @Test
  public void testFunctionParam() {
    assertNoErrors("/** @param {number} a */ function f(a) {} f(1);");
  }

  @Test
  public void testFunctionReturn() {
    assertNoErrors(
        "/** @return {number} */ function f() { return 1; } var x = f();");
  }

  @Test
  public void testPropertyDeclaration() {
    assertNoErrors(
        "/** @constructor */ function Foo() {}"
        + "/** @type {number} */ Foo.prototype.bar = 1;"
        + "var f = new Foo(); f.bar = 2;");
  }

  @Test
  public void testObjectLiteralPrototype() {
    assertNoErrors(
        "/** @constructor */ function Foo() {}"
        + "Foo.prototype = { /** @type {number} */ bar: 1 };"
        + "var f = new Foo(); f.bar = 2;");
  }

  @Test
  public void testCatchParam() {
    assertNoErrors("try { throw 1; } catch (e) { var x = e; }");
  }

  @Test
  public void testLends() {
    assertNoErrors(
        "/** @constructor */ function Foo() {}"
        + "var x = /** @lends {Foo.prototype} */ ({ method: function() {} });");
  }

  @Test
  public void testQualifiedName() {
    assertNoErrors("var a = {b: 1}; var c = a.b;");
  }

  @Test
  public void testFunctionExpression() {
    assertNoErrors("var f = function() {}; f();");
  }

  @Test
  public void testObjectLiteralCast() {
    assertNoErrors(
        "/** @constructor */ function Foo() {}"
        + "var f = /** @type {Foo} */ ({});");
  }

  @Test
  public void testThisType() {
    assertNoErrors(
        "/** @constructor */ function Foo() {"
        + "  /** @type {number} */ this.x = 1;"
        + "}"
        + "var f = new Foo(); f.x = 2;");
  }

  @Test
  public void testEnumTypeReference() {
    assertNoErrors(
        "/** @enum {number} */ var E = {A: 1};"
        + "/** @param {E} e */ function f(e) {} f(E.A);");
  }

  @Test
  public void testTypedefTypeReference() {
    assertNoErrors(
        "/** @typedef {number} */ var MyNumber;"
        + "/** @param {MyNumber} x */ function f(x) {} f(1);");
  }

  @Test
  public void testFunctionType() {
    assertNoErrors(
        "/** @type {function(number): number} */"
        + "var f = function(x) { return x; }; f(1);");
  }

  @Test
  public void testRecordType() {
    assertNoErrors(
        "/** @param {{x: number}} obj */ function f(obj) {} f({x: 1});");
  }

  @Test
  public void testEnumInitializerWarning() {
    assertError("/** @enum {number} */ var E = 3;", "enum initializer");
  }

  @Test
  public void testConstructorNotInitializedWarning() {
    assertError("/** @constructor */ var Foo;", "must be initialized");
  }

  @Test
  public void testInterfaceNotInitializedWarning() {
    assertError("/** @interface */ var Foo;", "must be initialized");
  }
}