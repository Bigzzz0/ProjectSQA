package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: TypedScopeCreator (Closure Compiler).
 *
 * Primary defect area:
 * - LocalScopeBuilder did not register nested/non-extern functions in
 *   nonExternFunctions, so CollectProperties never ran for functions declared
 *   or assigned inside a local scope. This breaks:
 *     . namespaced function stubs in local scopes
 *     . collected function stubs in local scopes
 *
 * Other decision/boundary zones covered:
 * - Global vs local scope construction.
 * - Native scope initialization (Object, Function, undefined, prototypes).
 * - VAR / CATCH / FUNCTION / arguments declarations.
 * - Namespaced GETPROP declarations and stub declarations.
 * - Enum validation, duplicate enum keys, non-object-literal enums.
 * - Typedef registration.
 * - Literal type attachment.
 * - Global this property registration.
 * - Stub resolution to UNKNOWN.
 */
public class TypedScopeCreatorDeepseekTest {

  private Compiler compiler;
  private TypedScopeCreator creator;
  private Scope globalScope;
  private Node root;

  private Scope createGlobalScope(String js) {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    root = compiler.parseTestCode(js);
    assertNotNull(root);
    creator = new TypedScopeCreator(compiler);
    globalScope = creator.createScope(root, null);
    assertNotNull(globalScope);
    return globalScope;
  }

  private Scope createLocalScope(String js, String functionName) {
    createGlobalScope(js);
    Node functionNode = findFunction(root, functionName);
    assertNotNull("Function not found: " + functionName, functionNode);
    Scope localScope = creator.createScope(functionNode, globalScope);
    assertNotNull(localScope);
    return localScope;
  }

  private static Node findFunction(Node node, String name) {
    if (node == null) {
      return null;
    }
    if (node.getType() == Token.FUNCTION) {
      Node nameNode = node.getFirstChild();
      if (nameNode != null && nameNode.getType() == Token.NAME
          && name.equals(nameNode.getString())) {
        return node;
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFunction(child, name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static Node findNodeByType(Node node, int type) {
    if (node == null) {
      return null;
    }
    if (node.getType() == type) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findNodeByType(child, type);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static void assertType(Node node, String expected) {
    assertNotNull(node);
    assertNotNull("JSType was not attached", node.getJSType());
    assertEquals(expected, node.getJSType().toString());
  }

  @Test(timeout = 4000)
  public void testCreateInitialScopeDeclaresNativeTypes() {
    createGlobalScope("var x;");
    assertNotNull(globalScope.getVar("Object"));
    assertNotNull(globalScope.getVar("Function"));
    assertNotNull(globalScope.getVar("Array"));
    assertNotNull(globalScope.getVar("undefined"));
    assertNotNull(globalScope.getVar("Object.prototype"));
  }

  @Test(timeout = 4000)
  public void testLocalScopeDeclaresLocalVariables() {
    createGlobalScope("function f() { var x; }");
    Scope local = creator.createScope(findFunction(root, "f"), globalScope);
    assertNotNull(local.getVar("x"));
    assertNull(globalScope.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testCatchParameterInLocalScope() {
    Scope local = createLocalScope(
        "function f() { try {} catch (e) { var x; } }", "f");
    assertNotNull(local.getVar("e"));
    assertNotNull(local.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testFunctionArgumentsInLocalScope() {
    Scope local = createLocalScope(
        "function f(a, b) { var x; }", "f");
    assertNotNull(local.getVar("a"));
    assertNotNull(local.getVar("b"));
    assertNotNull(local.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testLocalFunctionDeclarationIsTyped() {
    Scope local = createLocalScope(
        "function f() { function g() {} }", "f");
    assertNotNull(local.getVar("g"));
    assertNotNull(local.getVar("g").getType());
    assertTrue(local.getVar("g").getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testNamespacedFunctionStubGlobal() {
    createGlobalScope("var ns = {}; ns.foo = function() {};");
    assertNotNull(globalScope.getVar("ns.foo"));
    assertNotNull(globalScope.getVar("ns.foo").getType());
    assertTrue(globalScope.getVar("ns.foo").getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testGlobalStubDeclarationResolvedToUnknown() {
    createGlobalScope("var ns = {}; ns.foo;");
    assertNotNull(globalScope.getVar("ns.foo"));
    assertNotNull(globalScope.getVar("ns.foo").getType());
    assertTrue(globalScope.getVar("ns.foo").getType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testGlobalGetPropTypeAnnotation() {
    createGlobalScope("var ns = {}; /** @type {number} */ ns.foo;");
    assertNotNull(globalScope.getVar("ns.foo"));
    assertNotNull(globalScope.getVar("ns.foo").getType());
    assertEquals("number", globalScope.getVar("ns.foo").getType().toString());
  }

  @Test(timeout = 4000)
  public void testGlobalConstructorPrototypeDeclared() {
    createGlobalScope("/** @constructor */ function Foo() {}");
    assertNotNull(globalScope.getVar("Foo"));
    assertTrue(globalScope.getVar("Foo").getType().isFunctionType());
    assertNotNull(globalScope.getVar("Foo.prototype"));
  }

  @Test(timeout = 4000)
  public void testGlobalPrototypeMethodTyped() {
    createGlobalScope(
        "/** @constructor */ function Foo() {}"
        + "Foo.prototype.bar = function() {};");
    assertNotNull(globalScope.getVar("Foo.prototype.bar"));
    assertTrue(globalScope.getVar("Foo.prototype.bar").getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testNamespacedFunctionStubLocal() {
    createLocalScope(
        "/** @constructor */ function Foo() {}"
        + "function outer() {"
        + "  var ns = {};"
        + "  /** @this {Foo} */"
        + "  ns.foo = function() { /** @type {number} */ this.x; };"
        + "}",
        "outer");

    FunctionType foo = (FunctionType) globalScope.getVar("Foo").getType();
    assertTrue(
        "Namespaced function stub in a local scope should have its body collected",
        foo.getInstanceType().hasOwnProperty("x"));
  }

  @Test(timeout = 4000)
  public void testCollectedFunctionStubLocal() {
    createLocalScope(
        "/** @constructor */ function Foo() {}"
        + "function outer() {"
        + "  /** @this {Foo} */"
        + "  function baz() { /** @type {number} */ this.y; }"
        + "}",
        "outer");

    FunctionType foo = (FunctionType) globalScope.getVar("Foo").getType();
    assertTrue(
        "Collected function stub in a local scope should be registered",
        foo.getInstanceType().hasOwnProperty("y"));
  }

  @Test(timeout = 4000)
  public void testCollectedFunctionStubGlobal() {
    createGlobalScope(
        "/** @constructor */ function Foo() {}"
        + "/** @this {Foo} */"
        + "function baz() { /** @type {number} */ this.y; }");

    FunctionType foo = (FunctionType) globalScope.getVar("Foo").getType();
    assertTrue(foo.getInstanceType().hasOwnProperty("y"));
  }

  @Test(timeout = 4000)
  public void testEnumTypeDeclared() {
    createGlobalScope("/** @enum {number} */ var E = {A: 1, B: 2};");
    assertNotNull(globalScope.getVar("E"));
    assertNotNull(globalScope.getVar("E").getType());
  }

  @Test(timeout = 4000)
  public void testEnumDuplicateKeyReportsWarning() {
    createGlobalScope("/** @enum {number} */ var E = {A: 1, A: 2};");
    assertTrue(
        "Expected duplicate enum key diagnostic",
        compiler.getErrors().length > 0 || compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testEnumInitializerNotObjectLiteralReportsWarning() {
    createGlobalScope("/** @enum {number} */ var E = 3;");
    assertTrue(
        "Expected enum initializer diagnostic",
        compiler.getErrors().length > 0 || compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testTypedefRegistered() {
    createGlobalScope("/** @typedef {number|string} */ var MyType;");
    JSType type = compiler.getTypeRegistry().getType("MyType");
    assertNotNull("Typedef should be registered in the type registry", type);
  }

  @Test(timeout = 4000)
  public void testLiteralTypesAttached() {
    createGlobalScope(
        "var n = 1;"
        + "var s = 'x';"
        + "var t = true;"
        + "var f = false;"
        + "var r = /a/;"
        + "var o = {};"
        + "var z = null;"
        + "var u = void 0;");

    assertType(findNodeByType(root, Token.NUMBER), "number");
    assertType(findNodeByType(root, Token.STRING), "string");
    assertType(findNodeByType(root, Token.TRUE), "boolean");
    assertType(findNodeByType(root, Token.FALSE), "boolean");
    assertType(findNodeByType(root, Token.REGEXP), "RegExp");
    assertType(findNodeByType(root, Token.NULL), "null");
    assertType(findNodeByType(root, Token.VOID), "undefined");

    Node objectLit = findNodeByType(root, Token.OBJECTLIT);
    assertNotNull(objectLit);
    assertNotNull(objectLit.getJSType());
    assertTrue(objectLit.getJSType().isObjectType());
  }

  @Test(timeout = 4000)
  public void testGlobalThisPropertyDeclared() {
    createGlobalScope("var x;");
    ObjectType globalThis =
        compiler.getTypeRegistry().getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertTrue(globalThis.hasOwnProperty("x"));
  }
}