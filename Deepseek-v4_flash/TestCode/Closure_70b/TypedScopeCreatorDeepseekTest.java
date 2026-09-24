/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TypedScopeCreator.createScope, defineVar, defineSlot, maybeDeclareQualifiedName, createFunctionTypeFromNodes, createEnumTypeFromNodes.
 * 
 * Decision branches targeted:
 * - Parent scope null (global) vs non-null (local)
 * - Variable with type info (@type, @constructor, @enum, @const)
 * - Variable without type info (inferred)
 * - Function declaration (hoisted vs non-hoisted)
 * - Duplicate variable declarations (should trigger warning via validator.expectUndeclaredVariable)
 * - Object literal with @lends
 * - Enum initializer validation (must be object literal or qualified name)
 * - Function parameters with and without JSDoc
 * - Stub declarations for properties (untyped GETPROP in EXPR_RESULT)
 * - Constructor/interface initializer checks (must be initialized)
 * - Global this property declarations
 * 
 * Defect-targeted branches:
 * - Duplicate local var declaration (defect in testDuplicateLocalVarDecl): ensure warning count is correct (expected 2, but was 1)
 * - Function arguments with missing JSDoc (defect in testFunctionArguments13): ensure warning for missing param type
 * - Scoping issue with var inside block (defect in testScoping12): ensure correct scope resolution
 */

package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;

import org.junit.Test;

/**
 * Test suite for TypedScopeCreator with focus on defect-revealing scenarios.
 */
public class TypedScopeCreatorDeepseekTest {

  private static final String EXTERNS = ""; // empty externs for simplicity

  /**
   * Helper to compile a JavaScript source string and return the global scope created by TypedScopeCreator.
   */
  private Scope compileAndGetGlobalScope(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    // Disable all optimizations to preserve structure
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    assertNotNull("Root should not be null", root);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    return globalScope;
  }

  /**
   * Helper to compile and get the local scope for a function body.
   */
  private Scope compileAndGetLocalScope(String js, String functionName) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    // Find the function node
    Node functionNode = findFunctionNode(root, functionName);
    assertNotNull("Function node not found: " + functionName, functionNode);
    return creator.createScope(functionNode, globalScope);
  }

  private Node findFunctionNode(Node node, String name) {
    if (node.isFunction() && node.getFirstChild().getString().equals(name)) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFunctionNode(child, name);
      if (result != null) return result;
    }
    return null;
  }

  // ==========================================
  // Partition A: Core Functional Logic & State Transitions
  // ==========================================

  @Test(timeout = 4000)
  public void testGlobalVarDeclarationWithType() {
    String js = "/** @type {number} */ var x = 5;";
    Scope scope = compileAndGetGlobalScope(js);
    Var x = scope.getVar("x");
    assertNotNull("x should be declared", x);
    assertFalse("x should not be inferred", x.isTypeInferred());
    assertTrue("x type should be number", x.getType().isNumber());
  }

  @Test(timeout = 4000)
  public void testGlobalVarDeclarationWithoutType() {
    String js = "var x = 'hello';";
    Scope scope = compileAndGetGlobalScope(js);
    Var x = scope.getVar("x");
    assertNotNull("x should be declared", x);
    assertTrue("x type should be inferred", x.isTypeInferred());
    // Inferred type should be string (unknown is also possible but we expect string)
    assertTrue("x type should be string or string?", x.getType().isStringValueType() || x.getType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclaration() {
    String js = "/** @return {number} */ function f() { return 1; }";
    Scope scope = compileAndGetGlobalScope(js);
    Var f = scope.getVar("f");
    assertNotNull("f should be declared", f);
    assertFalse("f type should be declared", f.isTypeInferred());
    assertTrue("f should be a function type", f.getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testEnumType() {
    String js = "/** @enum {string} */ var Color = {RED: 'red', GREEN: 'green'};";
    Scope scope = compileAndGetGlobalScope(js);
    Var color = scope.getVar("Color");
    assertNotNull("Color should be declared", color);
    assertTrue("Color type should be enum", color.getType() != null && color.getType().isEnumType());
  }

  @Test(timeout = 4000)
  public void testPropertyAssignmentOnPrototype() {
    String js = "/** @constructor */ function Foo() {}\n/** @type {number} */ Foo.prototype.bar = 5;";
    Scope scope = compileAndGetGlobalScope(js);
    Var bar = scope.getVar("Foo.prototype.bar");
    assertNotNull("Foo.prototype.bar should be declared", bar);
    assertTrue("bar type should be number", bar.getType().isNumber());
  }

  // ==========================================
  // Partition B: Boundary Value Analysis & Extremes
  // ==========================================

  @Test(timeout = 4000)
  public void testEmptyJSDocOnVar() {
    String js = "/** */ var x = 1;";
    Scope scope = compileAndGetGlobalScope(js);
    Var x = scope.getVar("x");
    assertNotNull("x should be declared", x);
    // No explicit type, so type should be inferred as number from literal
    assertTrue("x type should be inferred", x.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testVarWithNoInitializer() {
    String js = "var x;";
    Scope scope = compileAndGetGlobalScope(js);
    Var x = scope.getVar("x");
    assertNotNull("x should be declared", x);
    // Inferred type should be unknown (since no value)
    assertTrue("x type should be unknown", x.getType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testMultipleVarDeclarations() {
    String js = "var a = 1, b = 2;";
    Scope scope = compileAndGetGlobalScope(js);
    assertNotNull("a should be declared", scope.getVar("a"));
    assertNotNull("b should be declared", scope.getVar("b"));
  }

  @Test(timeout = 4000)
  public void testGlobalVariableWithNullJSType() {
    // The variable type is null when no type info
    String js = "var x = null;";
    Scope scope = compileAndGetGlobalScope(js);
    Var x = scope.getVar("x");
    assertNotNull("x should be declared", x);
    // null literal gives null type; should be inferred
    assertTrue("x type should be inferred", x.isTypeInferred());
    assertTrue("x type should be null", x.getType().isNullType());
  }

  // ==========================================
  // Partition C: Defect-Targeted Branch Zone
  // ==========================================

  /**
   * Defect: Duplicate local variable declaration should produce exactly two warnings.
   * The original test expected 2 warnings but got 1.
   */
  @Test(timeout = 4000)
  public void testDuplicateLocalVarDecl() {
    String js = "/** @param {number} x */ function f(x) { var x; }";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    // The function f should have a local scope
    Node functionNode = findFunctionNode(root, "f");
    assertNotNull("Function f should exist", functionNode);
    Scope localScope = creator.createScope(functionNode, globalScope);

    // There should be a duplicate variable declaration warning
    int warningCount = compiler.getErrorManager().getWarningCount();
    // According to defect, we expect 2 warnings; actual bug gives 1.
    // So we assert that we get 2 warnings to reveal the bug if we get 1.
    assertEquals("Duplicate variable declaration should produce 2 warnings", 2, warningCount);
    // Additionally verify that the variable is declared twice (or at least once)
    Var x = localScope.getVar("x");
    assertNotNull("x should be declared in local scope", x);
  }

  /**
   * Defect: Function arguments with missing JSDoc should produce a warning.
   * The original test expected a warning but got none.
   */
  @Test(timeout = 4000)
  public void testFunctionArguments13() {
    String js = "/** @param {number} a */ function f(a, b) {}";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    Node functionNode = findFunctionNode(root, "f");
    assertNotNull(functionNode);
    Scope localScope = creator.createScope(functionNode, globalScope);

    // Verify that 'b' is declared but with inferred type (since no JSDoc)
    Var b = localScope.getVar("b");
    assertNotNull("b should be declared as a parameter", b);
    // The defect expects a warning about missing parameter type
    // We check that at least one warning was reported
    int warningCount = compiler.getErrorManager().getWarningCount();
    assertTrue("Expected a warning for missing parameter type, but got " + warningCount, warningCount > 0);
  }

  /**
   * Defect: Scoping issue - var inside block should not leak to the wrong scope.
   * The original test expected a warning.
   */
  @Test(timeout = 4000)
  public void testScoping12() {
    String js = "function f() { if (true) { var x = 1; } var x = 2; }";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    Node functionNode = findFunctionNode(root, "f");
    assertNotNull(functionNode);
    Scope localScope = creator.createScope(functionNode, globalScope);

    // Expect a duplicate variable declaration warning
    int warningCount = compiler.getErrorManager().getWarningCount();
    assertTrue("Expected a warning for duplicate variable declaration", warningCount > 0);
  }

  // ==========================================
  // Partition D: Exception & Defensive Guard Paths
  // ==========================================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testCreateScopeWithNullRoot() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(null, null);
  }

  @Test(timeout = 4000)
  public void testEnumWithNonObjectLiteral() {
    String js = "/** @enum {number} */ var E = 5;";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    // Expect warning about enum initializer
    int warningCount = compiler.getErrorManager().getWarningCount();
    assertTrue("Expected warning for enum initializer not object literal", warningCount > 0);
  }

  @Test(timeout = 4000)
  public void testConstructorWithoutInitializer() {
    String js = "/** @constructor */ function Foo() {}";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    // No initializer, so expect CTOR_INITIALIZER warning
    int warningCount = compiler.getErrorManager().getWarningCount();
    assertTrue("Expected warning for constructor without initializer", warningCount > 0);
  }

  @Test(timeout = 4000)
  public void testLendsOnNonObject() {
    String js = "/** @lends {string} */ var x = {};";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    // Expect LENDS_ON_NON_OBJECT warning
    int warningCount = compiler.getErrorManager().getWarningCount();
    assertTrue("Expected warning for lends on non-object", warningCount > 0);
  }

  // ==========================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ==========================================

  @Test(timeout = 4000)
  public void testScopeWithParent() {
    String js = "function f() { var inner = 1; }";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    Node functionNode = findFunctionNode(root, "f");
    assertNotNull(functionNode);
    Scope localScope = creator.createScope(functionNode, globalScope);
    // local scope parent should be global scope
    assertSame("Local scope parent should be global", globalScope, localScope.getParent());
    // Var inner should not be visible in global scope
    assertNull("inner should not be in global scope", globalScope.getVar("inner"));
    // Var inner should be in local scope
    assertNotNull("inner should be in local scope", localScope.getVar("inner"));
  }

  @Test(timeout = 4000)
  public void testMultipleFunctionsDoNotShareScope() {
    String js = "function f() { var x = 1; }\nfunction g() { var x = 2; }";
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        SourceFile.fromCode("testcode", js));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    Node fNode = findFunctionNode(root, "f");
    Node gNode = findFunctionNode(root, "g");
    assertNotNull(fNode);
    assertNotNull(gNode);
    Scope fScope = creator.createScope(fNode, globalScope);
    Scope gScope = creator.createScope(gNode, globalScope);
    Var fX = fScope.getVar("x");
    Var gX = gScope.getVar("x");
    assertNotNull("x should be in f scope", fX);
    assertNotNull("x should be in g scope", gX);
    // They should be different Var objects
    assertNotSame("x in different functions should be different Var", fX, gX);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyPropertyDeclaration() {
    String js = "var obj = {/** @type {number} */ key: 5};";
    Scope scope = compileAndGetGlobalScope(js);
    Var obj = scope.getVar("obj");
    assertNotNull("obj should be declared", obj);
    // The object literal should have a property 'key' with type number
    assertTrue("obj should be an object type", obj.getType().isObjectType());
    ObjectType objType = (ObjectType) obj.getType();
    assertTrue("key property should be declared", objType.hasOwnProperty("key"));
    JSType keyType = objType.getPropertyType("key");
    assertTrue("key type should be number", keyType.isNumber());
  }

  @Test(timeout = 4000)
  public void testTypedefDeclaration() {
    String js = "/** @typedef {number|string} */ var MyType;";
    Scope scope = compileAndGetGlobalScope(js);
    Var myType = scope.getVar("MyType");
    assertNotNull("MyType should be declared", myType);
    // Typedef is registered in type registry, variable type should be NO_TYPE or similar
    assertTrue("MyType should not be undefined", myType.getType() != null);
  }

  @Test(timeout = 4000)
  public void testGoogRequireDoesNotCrash() {
    // Ensure that a goog.require call does not cause issues (though handler not implemented)
    String js = "goog.require('some.Module');";
    Scope scope = compileAndGetGlobalScope(js);
    // Just check no exception
    assertNotNull("Scope should be created", scope);
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionExpression() {
    String js = "var f = function() {};";
    Scope scope = compileAndGetGlobalScope(js);
    Var f = scope.getVar("f");
    assertNotNull("f should be declared", f);
    assertTrue("f type should be function", f.getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testExternOverride() {
    // Simulate externs with a variable declaration
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
    compiler.compile(
        SourceFile.fromCode("externs", "/** @type {string} */ var x;"),
        SourceFile.fromCode("src", "var x = 1;"));
    Node root = compiler.getRoot();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    Var x = globalScope.getVar("x");
    assertNotNull("x should be declared", x);
    // The externs declare x as string; the source assignment should not change type
    assertFalse("x type should not be inferred (extern)", x.isTypeInferred());
    assertTrue("x type should be string", x.getType().isStringValueType());
  }
}