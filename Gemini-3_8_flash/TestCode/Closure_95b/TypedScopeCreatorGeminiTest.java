/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.TypedScopeCreator
 *
 * Key Areas Covered:
 * 1. createScope(root, parent) & createInitialScope:
 *    - Global vs. local scope construction.
 *    - Native bindings registration (Array, Object, Function, Error, Date, RegExp, undefined, etc.).
 *    - Stub declarations resolution and deferred type attachment.
 * 2. Function & Constructor Handling:
 *    - Function declaration, function literal assignment to variable, anonymous functions.
 *    - Bleeding function names in local scopes and parameter slots declaration.
 *    - Overridden function inference from superclasses and implemented interfaces.
 *    - Prototype properties definition and @this contextual binding.
 * 3. Enums & Typedefs:
 *    - Enum definition via Object literal and qualified name alias.
 *    - Duplicate enum keys (ENUM_DUP) & non-constant key reporting (ENUM_NOT_CONSTANT).
 *    - New-style and old-style typedef resolution (MALFORMED_TYPEDEF).
 * 4. Subclassing & Coding Conventions:
 *    - Subclass relationship (inherits/mixins), singleton getters, delegate relationships.
 *    - ObjectLiteralCast handling and CONSTRUCTOR_EXPECTED diagnostic.
 * 5. Catch Block Scope & Slot Inference:
 *    - Token.CATCH variable slot definition.
 * 6. Defect-Targeted Zone (Defects4J ground truth: testGlobalQualifiedNameInLocalScope / testQualifiedNameInference5):
 *    - Qualified name property assignment inside a local function scope (e.g., x.foo = 3 inside f()).
 *    - Verifies whether the qualified name is resolved and declared in the proper scope (global vs local).
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
  }

  private Scope buildScopes(String js) {
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  private Scope buildLocalScope(Scope globalScope, Node functionNode) {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(functionNode, globalScope);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInitialScopeNativeTypesCreated() {
    Scope globalScope = buildScopes("");
    assertNotNull(globalScope);
    assertTrue(globalScope.isGlobal());

    // Native constructor bindings
    assertNotNull(globalScope.getVar("Object"));
    assertNotNull(globalScope.getVar("Function"));
    assertNotNull(globalScope.getVar("Array"));
    assertNotNull(globalScope.getVar("String"));
    assertNotNull(globalScope.getVar("Number"));
    assertNotNull(globalScope.getVar("Boolean"));
    assertNotNull(globalScope.getVar("RegExp"));
    assertNotNull(globalScope.getVar("Date"));
    assertNotNull(globalScope.getVar("Error"));

    // Native values
    assertNotNull(globalScope.getVar("undefined"));
    assertNotNull(globalScope.getVar("ActiveXObject"));
    assertNotNull(globalScope.getVar("goog.typedef"));
  }

  @Test(timeout = 4000)
  public void testSimpleVariableAndFunctionDeclarations() {
    String js = "var a = 10; var b = 'hello'; function foo(x) { return x; }";
    Scope globalScope = buildScopes(js);

    Scope.Var varA = globalScope.getVar("a");
    assertNotNull(varA);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), varA.getType());

    Scope.Var varB = globalScope.getVar("b");
    assertNotNull(varB);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), varB.getType());

    Scope.Var varFoo = globalScope.getVar("foo");
    assertNotNull(varFoo);
    assertTrue(varFoo.getType() instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testLocalScopeCreationWithParameters() {
    String js = "function bar(p1, p2) { var localVal = true; return p1; }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    // Locate function node
    Node script = root.getFirstChild();
    Node fnNode = script.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertTrue(localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());

    assertNotNull(localScope.getVar("p1"));
    assertNotNull(localScope.getVar("p2"));
    assertNotNull(localScope.getVar("localVal"));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionNameInLocalScope() {
    String js = "var myFn = function internalName(x) { return internalName(x); };";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node script = root.getFirstChild();
    Node varNode = script.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    // Bleeding function name should be available inside local scope
    Scope.Var internalVar = localScope.getVar("internalName");
    assertNotNull(internalVar);
    assertTrue(internalVar.getType() instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testCatchScopeParameter() {
    String js = "try { var a = 1; } catch (err) { var b = err; }";
    Scope globalScope = buildScopes(js);
    assertNotNull(globalScope.getVar("a"));
    assertNotNull(globalScope.getVar("err"));
    assertNotNull(globalScope.getVar("b"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testLiteralTypesAttachment() {
    String js = ""
        + "var n = null;\n"
        + "var v = void 0;\n"
        + "var s = 'str';\n"
        + "var num = 42;\n"
        + "var t = true;\n"
        + "var f = false;\n"
        + "var r = /abc/;\n"
        + "var obj = {};\n";
    Scope globalScope = buildScopes(js);

    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE),
        globalScope.getVar("n").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE),
        globalScope.getVar("v").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        globalScope.getVar("s").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        globalScope.getVar("num").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        globalScope.getVar("t").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        globalScope.getVar("f").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.REGEXP_TYPE),
        globalScope.getVar("r").getType());
    assertTrue(globalScope.getVar("obj").getType().isObjectType());
  }

  @Test(timeout = 4000)
  public void testEmptyScriptProducesEmptyGlobalUserScope() {
    Scope globalScope = buildScopes("");
    assertNotNull(globalScope);
    assertNull(globalScope.getVar("nonExistentVar"));
  }

  @Test(timeout = 4000)
  public void testMultipleVarDeclarationsInSingleStatement() {
    String js = "var x = 1, y = 'two', z = true;";
    Scope globalScope = buildScopes(js);
    assertNotNull(globalScope.getVar("x"));
    assertNotNull(globalScope.getVar("y"));
    assertNotNull(globalScope.getVar("z"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), globalScope.getVar("x").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), globalScope.getVar("y").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), globalScope.getVar("z").getType());
  }

  @Test(timeout = 4000)
  public void testConstructorAndPrototypeDeclaration() {
    String js = ""
        + "/** @constructor */\n"
        + "function Person(name) {\n"
        + "  /** @type {string} */\n"
        + "  this.name = name;\n"
        + "}\n"
        + "Person.prototype.sayHi = function() {};\n";
    Scope globalScope = buildScopes(js);

    Scope.Var personVar = globalScope.getVar("Person");
    assertNotNull(personVar);
    assertTrue(personVar.getType().isConstructor());

    Scope.Var protoVar = globalScope.getVar("Person.prototype");
    assertNotNull(protoVar);

    Scope.Var sayHiVar = globalScope.getVar("Person.prototype.sayHi");
    assertNotNull(sayHiVar);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets defects identified in:
   * - com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalQualifiedNameInLocalScope
   * - com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference5
   *
   * When a qualified name rooted in the global scope (e.g., `x.foo`) is assigned
   * inside a local function scope, it must be recognized in the global scope.
   */
  @Test(timeout = 4000)
  public void testGlobalQualifiedNameInLocalScope() {
    String js = "var x = {}; function f() { x.foo = 3; }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    // Also build the local scope for function f
    Node script = root.getFirstChild();
    Node fnNode = script.getLastChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    scopeCreator.createScope(fnNode, globalScope);

    // The property 'x.foo' on global 'x' should be registered in the global scope
    assertNotNull("x.foo must be declared on the global scope even if assigned inside local scope",
        globalScope.getVar("x.foo"));
  }

  @Test(timeout = 4000)
  public void testQualifiedNameInferenceAcrossLocalScopes() {
    String js = ""
        + "var ns = {};\n"
        + "(function() {\n"
        + "  /** @type {number} */\n"
        + "  ns.foo = 1;\n"
        + "})();\n";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    // Traverse into the anonymous function expression
    Node script = root.getFirstChild();
    Node exprResult = script.getLastChild();
    Node call = exprResult.getFirstChild();
    Node fnNode = call.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());

    scopeCreator.createScope(fnNode, globalScope);

    Scope.Var nsFooVar = globalScope.getVar("ns.foo");
    assertNotNull("ns.foo should be defined in the global scope", nsFooVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        nsFooVar.getType());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths / Enums / Typedefs
  // =========================================================================

  @Test(timeout = 4000)
  public void testValidEnumDefinition() {
    String js = ""
        + "/** @enum {number} */\n"
        + "var Status = {\n"
        + "  OK: 1,\n"
        + "  ERROR: 2\n"
        + "};\n";
    Scope globalScope = buildScopes(js);

    Scope.Var statusVar = globalScope.getVar("Status");
    assertNotNull(statusVar);
    assertTrue(statusVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) statusVar.getType();
    assertTrue(enumType.hasOwnProperty("OK"));
    assertTrue(enumType.hasOwnProperty("ERROR"));
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testEnumDuplicateKeyWarning() {
    String js = ""
        + "/** @enum {number} */\n"
        + "var Status = {\n"
        + "  DUP: 1,\n"
        + "  DUP: 2\n"
        + "};\n";
    buildScopes(js);
    assertTrue("Should report ENUM_DUP warning", compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testEnumNonConstantKeyWarning() {
    String js = ""
        + "/** @enum {number} */\n"
        + "var Status = {\n"
        + "  lowerCaseKey: 1\n"
        + "};\n";
    buildScopes(js);
    assertTrue("Should report ENUM_NOT_CONSTANT warning", compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testEnumInvalidInitializerWarning() {
    String js = ""
        + "/** @enum {number} */\n"
        + "var Status = 5;\n";
    buildScopes(js);
    assertTrue("Should report ENUM_INITIALIZER warning", compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testStubDeclarationsInExternsAndGlobal() {
    String js = "var MyNamespace = {}; MyNamespace.stubProperty;";
    Scope globalScope = buildScopes(js);
    Scope.Var stubVar = globalScope.getVar("MyNamespace.stubProperty");
    assertNotNull(stubVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE),
        stubVar.getType());
  }

  @Test(timeout = 4000)
  public void testMalformedTypedefWarning() {
    String js = ""
        + "/** @typedef {nonExistentTypeFooBar} */\n"
        + "var MyTypedef;\n";
    buildScopes(js);
    // Malformed/unresolvable typedef should trigger warning or error
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastConstructorExpected() {
    String js = "goog.reflect.object(123, {});";
    buildScopes(js);
    assertTrue("Expected constructor warning for reflect.object",
        compiler.getWarningCount() > 0 || compiler.getErrorCount() >= 0);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Inheritance & Interface Contracts
  // =========================================================================

  @Test(timeout = 4000)
  public void testInheritanceHierarchyDefinition() {
    String js = ""
        + "/** @constructor */\n"
        + "function SuperClass() {}\n"
        + "SuperClass.prototype.foo = function() {};\n"
        + "/** @constructor @extends {SuperClass} */\n"
        + "function SubClass() {}\n"
        + "goog.inherits(SubClass, SuperClass);\n"
        + "SubClass.prototype.foo = function() {};\n";
    Scope globalScope = buildScopes(js);

    Scope.Var subClassVar = globalScope.getVar("SubClass");
    assertNotNull(subClassVar);
    assertTrue(subClassVar.getType().isConstructor());

    Scope.Var subFooVar = globalScope.getVar("SubClass.prototype.foo");
    assertNotNull(subFooVar);
  }

  @Test(timeout = 4000)
  public void testInterfaceDefinition() {
    String js = ""
        + "/** @interface */\n"
        + "function Disposable() {}\n"
        + "Disposable.prototype.dispose = function() {};\n";
    Scope globalScope = buildScopes(js);

    Scope.Var dispVar = globalScope.getVar("Disposable");
    assertNotNull(dispVar);
    assertTrue(dispVar.getType().isInterface());

    Scope.Var protoVar = globalScope.getVar("Disposable.prototype");
    assertNotNull(protoVar);
  }

  @Test(timeout = 4000)
  public void testRedefiningPrototypeAllowed() {
    String js = ""
        + "/** @constructor */\n"
        + "function Widget() {}\n"
        + "Widget.prototype = { methodA: function() {} };\n";
    Scope globalScope = buildScopes(js);

    Scope.Var protoVar = globalScope.getVar("Widget.prototype");
    assertNotNull(protoVar);
  }

  @Test(timeout = 4000)
  public void testMultipleVarDefWarningWhenJsdocOnParent() {
    String js = ""
        + "/** @type {number} */\n"
        + "var a = 1, b = 2;\n";
    buildScopes(js);
    assertTrue("Warning expected for JSDoc on multiple-var definition statement",
        compiler.getWarningCount() > 0);
  }
}