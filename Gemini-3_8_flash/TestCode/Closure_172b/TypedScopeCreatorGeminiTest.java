package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.TypedScopeCreator
 * Known Defect: TypeCheckTest::testIssue1024 (Methods overriding interface signatures
 *               triggering unexpected warnings in TypeCheck due to overridden function
 *               resolution failure on prototypes/implementing types).
 *
 * Branch & Feature Coverage Matrix:
 * -----------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic & Scoping Transitions
 *   - createInitialScope: verifies native type bindings (Object, Array, Date, undefined, ActiveXObject).
 *   - GlobalScope traversal: function declarations, variable declarations, hoisted functions.
 *   - LocalScope traversal: arguments declaration, bleeding functions, catch parameters.
 *   - IIFE parameter type inference from passing arguments.
 *   - Stub declarations and resolution to UNKNOWN type.
 *   - Window constructor special handling on GLOBAL_THIS.
 *
 * Partition B: Boundary Value Analysis & Type System Extremes
 *   - Literal types: NULL, VOID, STRING, NUMBER, BOOLEAN, REGEXP, OBJECTLIT.
 *   - Prototype re-assignment and object literal implicit prototype adjustments.
 *   - Empty and compound expressions (OR idiom: x = x || {}).
 *   - Object literal with @lends targeting prototype and instance objects.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Issue 1024)
 *   - testIssue1024_InterfaceMethodOverrideNoUnexpectedWarning: reproduces interface method
 *     declaration and subsequent overriding in implementing class without spurious warnings.
 *   - testInterfaceMethodOverrideTypeInheritance: asserts correct inheritance of parameter/return
 *     types from interface method to implementing class prototype method.
 *
 * Partition D: Defensive Guards & Diagnostic Error Paths
 *   - CTOR_INITIALIZER: uninitialized constructor declaration (var Foo; with @constructor).
 *   - IFACE_INITIALIZER: uninitialized interface declaration (var Bar; with @interface).
 *   - ENUM_INITIALIZER: non-object/non-qname enum initialization.
 *   - UNKNOWN_LENDS: @lends referring to undeclared identifier.
 *   - LENDS_ON_NON_OBJECT: @lends applied to primitive type.
 *   - MULTIPLE_VAR_DEF: multi-variable VAR with jsdoc comment.
 *   - ENUM_NOT_CONSTANT: invalid enum keys.
 *   - CONSTRUCTOR_EXPECTED: goog.reflect.object with non-constructor.
 *
 * Partition E: Lifecycle & Precondition Guards
 *   - patchGlobalScope with valid script modification.
 *   - patchGlobalScope precondition guards (non-script node, null scope, local scope).
 */

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class TypedScopeCreatorGeminiTest {

  private Scope buildScopes(Compiler compiler, String externs, String js) {
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    List<SourceFile> externsList = ImmutableList.of(
        SourceFile.fromCode("externs.js", externs));
    List<SourceFile> inputsList = ImmutableList.of(
        SourceFile.fromCode("testcode.js", js));
    compiler.init(externsList, inputsList, options);
    Node root = compiler.parseInputs();
    assertNotNull("AST Root must not be null", root);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  private Scope buildGlobalScope(Compiler compiler, String js) {
    return buildScopes(compiler, "", js);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateInitialScopeNativeBindings() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.init(
        ImmutableList.of(SourceFile.fromCode("externs.js", "")),
        ImmutableList.of(SourceFile.fromCode("testcode.js", "var a = 1;")),
        options);
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);

    Scope initialScope = creator.createInitialScope(root);
    assertTrue(initialScope.isGlobal());
    assertNotNull(initialScope.getVar("Object"));
    assertNotNull(initialScope.getVar("Array"));
    assertNotNull(initialScope.getVar("Date"));
    assertNotNull(initialScope.getVar("undefined"));
    assertNotNull(initialScope.getVar("ActiveXObject"));
    assertTrue(initialScope.getVar("Object").getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testFunctionAndVariableDeclarations() {
    Compiler compiler = new Compiler();
    String js =
        "/** @type {number} */ var count = 10;\n" +
        "function add(x, y) { return x + y; }\n" +
        "var multiply = function(a, b) { return a * b; };";
    Scope scope = buildGlobalScope(compiler, js);

    assertEquals(0, compiler.getWarningCount());
    assertTrue(scope.isDeclared("count", false));
    assertEquals("number", scope.getVar("count").getType().toString());
    assertFalse(scope.getVar("count").isTypeInferred());

    assertTrue(scope.isDeclared("add", false));
    assertTrue(scope.getVar("add").getType().isFunctionType());

    assertTrue(scope.isDeclared("multiply", false));
    assertTrue(scope.getVar("multiply").getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testLocalScopeCreationAndVariableResolution() {
    Compiler compiler = new Compiler();
    String js =
        "function processItems(p1, p2) {\n" +
        "  var localVar = 5;\n" +
        "  try {\n" +
        "    localVar++;\n" +
        "  } catch (err) {\n" +
        "    var inCatch = err;\n" +
        "  }\n" +
        "  return p1 + localVar;\n" +
        "}";
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.init(
        ImmutableList.of(SourceFile.fromCode("externs.js", "")),
        ImmutableList.of(SourceFile.fromCode("testcode.js", js)),
        options);
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var fnVar = globalScope.getVar("processItems");
    assertNotNull(fnVar);
    Node fnNode = fnVar.getNameNode().getParent();
    assertTrue(fnNode.isFunction());

    Scope localScope = creator.createScope(fnNode, globalScope);
    assertFalse(localScope.isGlobal());
    assertEquals(globalScope, localScope.getParent());
    assertTrue(localScope.isDeclared("p1", false));
    assertTrue(localScope.isDeclared("p2", false));
    assertTrue(localScope.isDeclared("localVar", false));
    assertTrue(localScope.isDeclared("err", false));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionNameInLocalScope() {
    Compiler compiler = new Compiler();
    String js = "var myFn = function bleed(val) { return bleed(val - 1); };";
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.init(
        ImmutableList.of(SourceFile.fromCode("externs.js", "")),
        ImmutableList.of(SourceFile.fromCode("testcode.js", js)),
        options);
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var myFnVar = globalScope.getVar("myFn");
    Node fnNode = myFnVar.getNameNode().getFirstChild();
    assertTrue(fnNode.isFunction());

    Scope localScope = creator.createScope(fnNode, globalScope);
    assertTrue(localScope.isDeclared("bleed", false));
    assertTrue(localScope.isDeclared("val", false));
    assertFalse(globalScope.isDeclared("bleed", false));
  }

  @Test(timeout = 4000)
  public void testIIFEArgumentInference() {
    Compiler compiler = new Compiler();
    String js =
        "/** @type {string} */ var outerStr = 'hello';\n" +
        "(function(arg) { return arg; })(outerStr);";
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.init(
        ImmutableList.of(SourceFile.fromCode("externs.js", "")),
        ImmutableList.of(SourceFile.fromCode("testcode.js", js)),
        options);
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node callNode = root.getLastChild().getLastChild().getFirstChild();
    assertTrue(callNode.isCall());
    Node fnNode = callNode.getFirstChild();
    assertTrue(fnNode.isFunction());

    Scope localScope = creator.createScope(fnNode, globalScope);
    Scope.Var argVar = localScope.getVar("arg");
    assertNotNull(argVar);
    assertEquals("string", argVar.getType().toString());
  }

  @Test(timeout = 4000)
  public void testStubPropertyResolvesToUnknown() {
    Compiler compiler = new Compiler();
    String js = "var ns = {}; ns.stubProp;";
    Scope scope = buildGlobalScope(compiler, js);

    assertEquals(0, compiler.getWarningCount());
    assertTrue(scope.isDeclared("ns.stubProp", false));
    Scope.Var stubVar = scope.getVar("ns.stubProp");
    assertNotNull(stubVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE), stubVar.getType());
  }

  @Test(timeout = 4000)
  public void testWindowConstructorUpdatesGlobalThis() {
    Compiler compiler = new Compiler();
    String js = "/** @constructor */ function Window() {}";
    Scope scope = buildGlobalScope(compiler, js);

    assertEquals(0, compiler.getWarningCount());
    Scope.Var windowVar = scope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());
    ObjectType globalThis = compiler.getTypeRegistry().getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertNotNull(globalThis.getConstructor());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testLiteralTypesAttachment() {
    Compiler compiler = new Compiler();
    String js =
        "var vNull = null;\n" +
        "var vVoid = void 0;\n" +
        "var vStr = 'closure';\n" +
        "var vNum = 42;\n" +
        "var vTrue = true;\n" +
        "var vFalse = false;\n" +
        "var vReg = /abc/;\n" +
        "var vObj = { a: 1, b: 'two' };";
    Scope scope = buildGlobalScope(compiler, js);

    assertEquals(0, compiler.getWarningCount());
    assertEquals("null", scope.getVar("vNull").getType().toString());
    assertEquals("undefined", scope.getVar("vVoid").getType().toString());
    assertEquals("string", scope.getVar("vStr").getType().toString());
    assertEquals("number", scope.getVar("vNum").getType().toString());
    assertEquals("boolean", scope.getVar("vTrue").getType().toString());
    assertEquals("boolean", scope.getVar("vFalse").getType().toString());
    assertTrue(scope.getVar("vReg").getType().isInstanceType());
    assertTrue(scope.getVar("vObj").getType().isRecordType() || scope.getVar("vObj").getType().isObjectType());
  }

  @Test(timeout = 4000)
  public void testOrIdiomPreservesType() {
    Compiler compiler = new Compiler();
    String js =
        "var ns = ns || {};\n" +
        "/** @type {number} */ ns.k = 100;";
    Scope scope = buildGlobalScope(compiler, js);

    assertEquals(0, compiler.getWarningCount());
    assertTrue(scope.isDeclared("ns", false));
    assertTrue(scope.isDeclared("ns.k", false));
    assertEquals("number", scope.getVar("ns.k").getType().toString());
  }

  @Test(timeout = 4000)
  public void testValidLendsOnObjectLiteral() {
    Compiler compiler = new Compiler();
    String js =
        "/** @constructor */ function Target() {}\n" +
        "Target.prototype = /** @lends {Target.prototype} */ ({\n" +
        "  sayHello: function() { return 'hello'; }\n" +
        "});";
    Scope scope = buildGlobalScope(compiler, js);

    assertEquals(0, compiler.getWarningCount());
    Scope.Var targetVar = scope.getVar("Target");
    assertNotNull(targetVar);
    ObjectType proto = targetVar.getType().toMaybeFunctionType().getPrototype();
    assertTrue(proto.hasProperty("sayHello"));
  }

  @Test(timeout = 4000)
  public void testSubclassRelationshipViaCodingConvention() {
    Compiler compiler = new Compiler();
    String js =
        "var goog = {}; goog.inherits = function(child, parent) {};\n" +
        "/** @constructor */ function Parent() {}\n" +
        "Parent.prototype.bar = function() {};\n" +
        "/** @constructor @extends {Parent} */ function Child() {}\n" +
        "goog.inherits(Child, Parent);";
    Scope scope = buildGlobalScope(compiler, js);

    assertEquals(0, compiler.getWarningCount());
    FunctionType childCtor = scope.getVar("Child").getType().toMaybeFunctionType();
    assertNotNull(childCtor);
    assertEquals("Parent", childCtor.getSuperClassConstructor().getReferenceName());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 1024)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue1024_InterfaceMethodOverrideNoUnexpectedWarning() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    SourceFile externs = SourceFile.fromCode("externs.js", "var window;");
    SourceFile input = SourceFile.fromCode("testcode.js",
        "/** @interface */\n" +
        "function B() {}\n" +
        "B.prototype.b = function() {};\n" +
        "/** @constructor @implements {B} */\n" +
        "function C() {}\n" +
        "/** @override */\n" +
        "C.prototype.b = function() {};");

    compiler.compile(ImmutableList.of(externs), ImmutableList.of(input), options);

    assertEquals("unexpected warnings(s): " + Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testInterfaceMethodOverrideTypeInheritance() {
    Compiler compiler = new Compiler();
    String js =
        "/** @interface */\n" +
        "function InterfaceX() {}\n" +
        "/** @param {string} text @return {number} */\n" +
        "InterfaceX.prototype.calc = function(text) {};\n" +
        "/** @constructor @implements {InterfaceX} */\n" +
        "function ImplX() {}\n" +
        "/** @override */\n" +
        "ImplX.prototype.calc = function(text) { return 42; };";

    Scope scope = buildGlobalScope(compiler, js);
    assertEquals(0, compiler.getWarningCount());

    Scope.Var ifaceProp = scope.getVar("InterfaceX.prototype.calc");
    assertNotNull(ifaceProp);
    FunctionType ifaceFn = ifaceProp.getType().toMaybeFunctionType();
    assertNotNull(ifaceFn);

    Scope.Var implProp = scope.getVar("ImplX.prototype.calc");
    assertNotNull(implProp);
    FunctionType implFn = implProp.getType().toMaybeFunctionType();
    assertNotNull(implFn);

    assertEquals(ifaceFn.getReturnType(), implFn.getReturnType());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testWarningConstructorMustBeInitialized() {
    Compiler compiler = new Compiler();
    String js = "/** @constructor */ var UninitializedCtor;";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarningInterfaceMustBeInitialized() {
    Compiler compiler = new Compiler();
    String js = "/** @interface */ var UninitializedIface;";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarningEnumInitializerNotObjectLit() {
    Compiler compiler = new Compiler();
    String js = "/** @enum {number} */ var BadEnum = 42;";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarningUnknownLendsTarget() {
    Compiler compiler = new Compiler();
    String js = "var x = /** @lends {nonExistentTarget} */ ({});";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarningLendsOnNonObject() {
    Compiler compiler = new Compiler();
    String js =
        "var numVar = 10;\n" +
        "var x = /** @lends {numVar} */ ({});";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarningMultipleVarDefWithJSDoc() {
    Compiler compiler = new Compiler();
    String js = "/** @type {number} */ var a = 1, b = 2;";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarningEnumKeyNotConstant() {
    Compiler compiler = new Compiler();
    String js = "/** @enum {number} */ var E = { 'invalid-key': 1 };";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_NOT_CONSTANT.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarningConstructorExpectedOnReflect() {
    Compiler compiler = new Compiler();
    String js =
        "var goog = {}; goog.reflect = {}; goog.reflect.object = function(a, b) {};\n" +
        "var notACtor = 123;\n" +
        "goog.reflect.object(notACtor, {});";
    buildGlobalScope(compiler, js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CONSTRUCTOR_EXPECTED.key,
        compiler.getWarnings()[0].getType().key);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Patching Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testPatchGlobalScopeLifecycle() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.init(
        ImmutableList.of(SourceFile.fromCode("externs.js", "")),
        ImmutableList.of(SourceFile.fromCode("testcode.js", "var alpha = 1; var beta = 2;")),
        options);
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("alpha", false));
    assertTrue(globalScope.isDeclared("beta", false));

    Node replacementScript = compiler.parseSyntheticCode("testcode.js", "var gamma = 3;");
    creator.patchGlobalScope(globalScope, replacementScript);

    assertFalse(globalScope.isDeclared("alpha", false));
    assertFalse(globalScope.isDeclared("beta", false));
    assertTrue(globalScope.isDeclared("gamma", false));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPatchGlobalScopeRejectsNonScriptNode() {
    Compiler compiler = new Compiler();
    Scope globalScope = buildGlobalScope(compiler, "var a = 1;");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);

    Node blockNode = new Node(Token.BLOCK);
    creator.patchGlobalScope(globalScope, blockNode);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testPatchGlobalScopeRejectsNullScope() {
    Compiler compiler = new Compiler();
    buildGlobalScope(compiler, "var a = 1;");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);

    Node scriptNode = compiler.parseSyntheticCode("testcode.js", "var b = 2;");
    creator.patchGlobalScope(null, scriptNode);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPatchGlobalScopeRejectsLocalScope() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.init(
        ImmutableList.of(SourceFile.fromCode("externs.js", "")),
        ImmutableList.of(SourceFile.fromCode("testcode.js", "function f() { var x = 1; }")),
        options);
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var fnVar = globalScope.getVar("f");
    Scope localScope = creator.createScope(fnVar.getNameNode().getParent(), globalScope);

    Node scriptNode = compiler.parseSyntheticCode("testcode.js", "var y = 2;");
    creator.patchGlobalScope(localScope, scriptNode);
  }
}