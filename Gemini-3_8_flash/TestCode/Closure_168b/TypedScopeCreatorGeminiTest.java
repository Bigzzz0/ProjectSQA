/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects & Decision Branches:
 * 1. Defect Issue 726 (com.google.javascript.jscomp.TypeCheckTest::testIssue726):
 *    - Assigning a non-object primitive to a constructor prototype (Bad.prototype = 1).
 *    - In the buggy version, maybeDeclareQualifiedName undeclares the prototype property from the scope,
 *      erasing type definition and preventing TypeCheck from emitting an invalid assignment warning.
 *    - Targeted by: testIssue726_assignPrimitiveToPrototype_emitsWarning and testIssue726_prototypeVarIntegrity.
 *
 * 2. Scope Creation & Traversal:
 *    - Global scope vs. local scope construction (parent == null vs. parent != null).
 *    - createInitialScope: Native type bindings (Object, Array, Function, Date, undefined, ActiveXObject).
 *    - DiscoverEnumsAndTypedefs: Token.VAR, Token.EXPR_RESULT (with and without assign).
 *    - FirstOrderFunctionAnalyzer: Escaped vars, multiple assignment counting, non-empty returns.
 *
 * 3. Type Declarations & Inferences:
 *    - Literals: null, void, string, number, boolean, regexp, objectlit.
 *    - Annotations: @constructor, @interface, @type, @typedef, @enum, @lends, @const, @override.
 *    - Constructor / Interface initializers (CTOR_INITIALIZER, IFACE_INITIALIZER).
 *    - Enum definition, aliasing, and validation (ENUM_INITIALIZER, ENUM_NOT_CONSTANT).
 *    - @lends resolution: valid object lends, UNKNOWN_LENDS, LENDS_ON_NON_OBJECT.
 *    - Typedef processing: valid typedef, qualified typedef, MALFORMED_TYPEDEF.
 *    - Multiple var definition warning (MULTIPLE_VAR_DEF).
 *
 * 4. Advanced Coding Conventions & Class Definition:
 *    - Subclass relationships (inherits), singleton getters, delegate relationships.
 *    - Object literal cast (goog.reflect.object) with constructor vs non-constructor (CONSTRUCTOR_EXPECTED).
 *    - Window constructor prototype binding to GLOBAL_THIS.
 *    - Stub declarations and fallback to UNKNOWN_TYPE.
 *    - Global non-extern function property gathering (@this CollectProperties).
 *
 * 5. Scope Modification & Lifecycle:
 *    - patchGlobalScope: updating and re-traversing modified script roots.
 *    - Function bleeding and parameter declaration in LocalScopeBuilder.
 */

package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Test;

import java.util.List;

public class TypedScopeCreatorGeminiTest {

  private static class ScopeContext {
    final Compiler compiler;
    final TypedScopeCreator creator;
    final Node root;
    final Scope globalScope;

    ScopeContext(Compiler compiler, TypedScopeCreator creator, Node root, Scope globalScope) {
      this.compiler = compiler;
      this.creator = creator;
      this.root = root;
      this.globalScope = globalScope;
    }
  }

  private ScopeContext buildContext(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    options.checkSymbols = true;
    List<SourceFile> externs = ImmutableList.of(
        SourceFile.fromCode("externs.js",
            "var undefined;\n" +
            "function Object() {}\n" +
            "function Function() {}\n" +
            "var goog = {};\n" +
            "goog.inherits = function(child, parent) {};\n" +
            "goog.addSingletonGetter = function(ctor) {};\n" +
            "goog.reflect = {};\n" +
            "goog.reflect.object = function(type, obj) {};\n"));
    List<SourceFile> inputs = ImmutableList.of(
        SourceFile.fromCode("testcode.js", js));
    compiler.init(externs, inputs, options);
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope scope = creator.createScope(root, null);
    return new ScopeContext(compiler, creator, root, scope);
  }

  private Compiler compileAndCheck(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    options.checkSymbols = true;
    List<SourceFile> externs = ImmutableList.of(
        SourceFile.fromCode("externs.js",
            "var undefined;\n" +
            "function Object() {}\n" +
            "Object.prototype;\n" +
            "function Function() {}\n" +
            "Function.prototype;\n" +
            "function String() {}\n" +
            "String.prototype;\n" +
            "function Number() {}\n" +
            "Number.prototype;\n" +
            "function Boolean() {}\n" +
            "Boolean.prototype;\n" +
            "function RegExp() {}\n" +
            "RegExp.prototype;\n" +
            "function Date() {}\n" +
            "Date.prototype;\n" +
            "function Array() {}\n" +
            "Array.prototype;\n"));
    List<SourceFile> inputs = ImmutableList.of(
        SourceFile.fromCode("testcode.js", js));
    compiler.compile(externs, inputs, options);
    return compiler;
  }

  private Node findFunctionNode(Node n, String fnName) {
    if (n.isFunction()) {
      Node nameNode = n.getFirstChild();
      if (fnName.equals(nameNode.getString())) {
        return n;
      }
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFunctionNode(child, fnName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private boolean hasWarning(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == type) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateInitialScope_declaresNativeBindings() {
    ScopeContext context = buildContext("");
    Scope scope = context.globalScope;

    assertTrue(scope.isGlobal());
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  @Test(timeout = 4000)
  public void testLiteralTypeAttachment() {
    ScopeContext context = buildContext(
        "var n = null;\n" +
        "var v = void 0;\n" +
        "var s = 'closure';\n" +
        "var num = 42;\n" +
        "var bTrue = true;\n" +
        "var bFalse = false;\n" +
        "var r = /pattern/g;\n" +
        "var obj = { x: 1, y: 'str' };\n");
    Scope scope = context.globalScope;

    assertNotNull(scope.getVar("n"));
    assertNotNull(scope.getVar("s"));
    assertNotNull(scope.getVar("num"));
    assertNotNull(scope.getVar("bTrue"));
    assertNotNull(scope.getVar("bFalse"));
    assertNotNull(scope.getVar("r"));
    assertNotNull(scope.getVar("obj"));
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationsAndLocalScopeBuilding() {
    ScopeContext context = buildContext(
        "/** @param {number} a\n * @param {string} b\n * @return {boolean} */\n" +
        "function compute(a, b) {\n" +
        "  var localVar = 100;\n" +
        "  return a > localVar;\n" +
        "}\n");
    Scope globalScope = context.globalScope;
    Scope.Var fnVar = globalScope.getVar("compute");
    assertNotNull(fnVar);
    assertTrue(fnVar.getType().isFunctionType());

    Node fnNode = findFunctionNode(context.root, "compute");
    assertNotNull(fnNode);

    Scope localScope = context.creator.createScope(fnNode, globalScope);
    assertTrue(localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());
    assertNotNull(localScope.getVar("a"));
    assertNotNull(localScope.getVar("b"));
    assertNotNull(localScope.getVar("localVar"));
  }

  @Test(timeout = 4000)
  public void testConstructorAndPrototypeImplicitSlots() {
    ScopeContext context = buildContext(
        "/** @constructor */\n" +
        "function Person(name) {\n" +
        "  this.name = name;\n" +
        "}\n" +
        "Person.prototype.greet = function() { return this.name; };\n");
    Scope scope = context.globalScope;

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    assertTrue(personVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("Person.prototype");
    assertNotNull(protoVar);

    Scope.Var greetVar = scope.getVar("Person.prototype.greet");
    assertNotNull(greetVar);
  }

  @Test(timeout = 4000)
  public void testInterfaceDefinition() {
    ScopeContext context = buildContext(
        "/** @interface */\n" +
        "function Disposable() {}\n" +
        "Disposable.prototype.dispose = function() {};\n");
    Scope scope = context.globalScope;

    Scope.Var ifaceVar = scope.getVar("Disposable");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
    assertNotNull(scope.getVar("Disposable.prototype"));
  }

  @Test(timeout = 4000)
  public void testFirstOrderAnalysis_escapedVariables() {
    ScopeContext context = buildContext(
        "function outer() {\n" +
        "  var count = 0;\n" +
        "  function inner() {\n" +
        "    count = count + 1;\n" +
        "    return count;\n" +
        "  }\n" +
        "  return inner;\n" +
        "}\n");
    Node outerFn = findFunctionNode(context.root, "outer");
    assertNotNull(outerFn);

    Scope localScope = context.creator.createScope(outerFn, context.globalScope);
    Scope.Var countVar = localScope.getVar("count");
    assertNotNull(countVar);
    assertTrue("Variable count should be marked as escaped", countVar.isEscaped());
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpression() {
    ScopeContext context = buildContext(
        "var myFunc = function bleeding(x) {\n" +
        "  if (x <= 1) return 1;\n" +
        "  return bleeding(x - 1);\n" +
        "};\n");
    assertNull(context.globalScope.getVar("bleeding"));

    Node fnNode = findFunctionNode(context.root, "bleeding");
    assertNotNull(fnNode);

    Scope localScope = context.creator.createScope(fnNode, context.globalScope);
    assertNotNull(localScope.getVar("bleeding"));
    assertNotNull(localScope.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testCatchParameterDeclaration() {
    ScopeContext context = buildContext(
        "function run() {\n" +
        "  try {\n" +
        "    throw 'error';\n" +
        "  } catch (err) {\n" +
        "    var insideCatch = err;\n" +
        "  }\n" +
        "}\n");
    Node runFn = findFunctionNode(context.root, "run");
    Scope localScope = context.creator.createScope(runFn, context.globalScope);
    assertNotNull(localScope.getVar("err"));
    assertNotNull(localScope.getVar("insideCatch"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptRoot() {
    ScopeContext context = buildContext("");
    assertNotNull(context.globalScope);
    assertTrue(context.globalScope.isGlobal());
  }

  @Test(timeout = 4000)
  public void testMultipleVarDefWarning() {
    ScopeContext context = buildContext(
        "/** @type {number} */ var a = 1, b = 2;\n");
    assertTrue(hasWarning(context.compiler, TypeCheck.MULTIPLE_VAR_DEF));
  }

  @Test(timeout = 4000)
  public void testMalformedTypedefWarning() {
    ScopeContext context = buildContext(
        "/** @typedef */ var BadTypedef;\n");
    assertTrue(hasWarning(context.compiler, TypedScopeCreator.MALFORMED_TYPEDEF));
  }

  @Test(timeout = 4000)
  public void testValidTypedefResolution() {
    ScopeContext context = buildContext(
        "/** @typedef {(string|number)} */ var StringOrNumber;\n" +
        "/** @type {StringOrNumber} */ var val = 12;\n");
    assertFalse(hasWarning(context.compiler, TypedScopeCreator.MALFORMED_TYPEDEF));
    assertNotNull(context.globalScope.getVar("val"));
  }

  @Test(timeout = 4000)
  public void testLendsAnnotation_validAndInvalid() {
    // Unknown lends target
    ScopeContext context1 = buildContext(
        "var x = /** @lends {NoSuchSymbol} */ ({ a: 1 });\n");
    assertTrue(hasWarning(context1.compiler, TypedScopeCreator.UNKNOWN_LENDS));

    // Lends on non-object
    ScopeContext context2 = buildContext(
        "var numVar = 42;\n" +
        "var y = /** @lends {numVar} */ ({ a: 1 });\n");
    assertTrue(hasWarning(context2.compiler, TypedScopeCreator.LENDS_ON_NON_OBJECT));

    // Valid lends on object
    ScopeContext context3 = buildContext(
        "var targetObj = {};\n" +
        "var z = /** @lends {targetObj} */ ({ prop: 10 });\n");
    assertFalse(hasWarning(context3.compiler, TypedScopeCreator.UNKNOWN_LENDS));
    assertFalse(hasWarning(context3.compiler, TypedScopeCreator.LENDS_ON_NON_OBJECT));
  }

  @Test(timeout = 4000)
  public void testEnumInitializerValidation() {
    // Valid Enum
    ScopeContext validContext = buildContext(
        "/** @enum {number} */ var ValidEnum = { A: 1, B: 2 };\n");
    assertFalse(hasWarning(validContext.compiler, TypedScopeCreator.ENUM_INITIALIZER));
    Scope.Var enumVar = validContext.globalScope.getVar("ValidEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);

    // Invalid Enum initializer (primitive)
    ScopeContext invalidContext = buildContext(
        "/** @enum {number} */ var InvalidEnum = 123;\n");
    assertTrue(hasWarning(invalidContext.compiler, TypedScopeCreator.ENUM_INITIALIZER));

    // Enum with non-constant key
    ScopeContext invalidKeyContext = buildContext(
        "/** @enum {number} */ var KeyEnum = { 'invalid-key': 1 };\n");
    assertTrue(hasWarning(invalidKeyContext.compiler, TypeCheck.ENUM_NOT_CONSTANT));
  }

  @Test(timeout = 4000)
  public void testConstructorAndInterfaceInitializersRequired() {
    ScopeContext ctorContext = buildContext(
        "/** @constructor */ var UninitCtor;\n");
    assertTrue(hasWarning(ctorContext.compiler, TypedScopeCreator.CTOR_INITIALIZER));

    ScopeContext ifaceContext = buildContext(
        "/** @interface */ var UninitIface;\n");
    assertTrue(hasWarning(ifaceContext.compiler, TypedScopeCreator.IFACE_INITIALIZER));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Issue 726)
  // =========================================================================

  /**
   * Targets Defects4J issue where assigning a primitive value to a constructor prototype:
   *   function Bad() {}
   *   Bad.prototype = 1;
   * should trigger an invalid type warning rather than silently undeclaring the prototype slot.
   */
  @Test(timeout = 4000)
  public void testIssue726_assignPrimitiveToPrototype_emitsWarning() {
    Compiler compiler = compileAndCheck(
        "/** @constructor */ function Bad() {}\n" +
        "Bad.prototype = 1;\n");
    assertTrue("Expected at least 1 warning for assigning primitive to prototype, but found "
        + compiler.getWarningCount(), compiler.getWarningCount() > 0);

    boolean foundIssue726Warning = false;
    for (JSError warning : compiler.getWarnings()) {
      String desc = warning.getDescription();
      if (desc.contains("assignment to property prototype of Bad gives invalid type")
          || (desc.contains("Bad.prototype") && desc.contains("Object"))) {
        foundIssue726Warning = true;
        break;
      }
    }
    assertTrue("Warning description must target prototype assignment type mismatch", foundIssue726Warning);
  }

  @Test(timeout = 4000)
  public void testIssue726_prototypeVarIntegrity() {
    ScopeContext context = buildContext(
        "/** @constructor */ function Bad() {}\n" +
        "Bad.prototype = 1;\n");
    Scope.Var badVar = context.globalScope.getVar("Bad");
    assertNotNull("Constructor Bad must exist in global scope", badVar);
    assertTrue("Bad must be typed as constructor", badVar.getType().isConstructor());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testPatchGlobalScope_preconditionEnforcement() {
    Compiler compiler = new Compiler();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);

    try {
      creator.patchGlobalScope(null, new Node(Token.BLOCK));
      fail("Expected IllegalStateException for non-script node");
    } catch (IllegalStateException expected) {
      // Precondition: scriptRoot.isScript()
    }
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCast_constructorExpected() {
    ScopeContext context = buildContext(
        "var notAConstructor = 10;\n" +
        "goog.reflect.object(notAConstructor, { k: 1 });\n");
    assertTrue(hasWarning(context.compiler, TypedScopeCreator.CONSTRUCTOR_EXPECTED));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCast_validConstructor() {
    ScopeContext context = buildContext(
        "/** @constructor */ function MyType() {}\n" +
        "goog.reflect.object(MyType, { key: 'value' });\n");
    assertFalse(hasWarning(context.compiler, TypedScopeCreator.CONSTRUCTOR_EXPECTED));
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Inheritance & Advanced Features
  // =========================================================================

  @Test(timeout = 4000)
  public void testInheritanceAndMethodOverride() {
    ScopeContext context = buildContext(
        "/** @constructor */ function SuperClass() {}\n" +
        "SuperClass.prototype.render = function() {};\n" +
        "/** @constructor\n * @extends {SuperClass} */ function SubClass() {}\n" +
        "goog.inherits(SubClass, SuperClass);\n" +
        "/** @override */ SubClass.prototype.render = function() {};\n");
    Scope scope = context.globalScope;

    Scope.Var subClassVar = scope.getVar("SubClass");
    assertNotNull(subClassVar);
    FunctionType subCtor = subClassVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    assertNotNull(scope.getVar("SubClass.prototype.render"));
  }

  @Test(timeout = 4000)
  public void testInterfaceImplementation() {
    ScopeContext context = buildContext(
        "/** @interface */ function Action() {}\n" +
        "Action.prototype.execute = function() {};\n" +
        "/** @constructor\n * @implements {Action} */ function ConcreteAction() {}\n" +
        "ConcreteAction.prototype.execute = function() {};\n");
    Scope scope = context.globalScope;

    Scope.Var actionVar = scope.getVar("Action");
    assertNotNull(actionVar);
    assertTrue(actionVar.getType().isInterface());

    Scope.Var implVar = scope.getVar("ConcreteAction");
    assertNotNull(implVar);
    assertTrue(implVar.getType().isConstructor());
  }

  @Test(timeout = 4000)
  public void testSingletonGetterConvention() {
    ScopeContext context = buildContext(
        "/** @constructor */ function Service() {}\n" +
        "goog.addSingletonGetter(Service);\n");
    Scope scope = context.globalScope;

    Scope.Var serviceVar = scope.getVar("Service");
    assertNotNull(serviceVar);
    assertNotNull(scope.getVar("Service.getInstance"));
  }

  @Test(timeout = 4000)
  public void testCollectProperties_thisTypeResolution() {
    ScopeContext context = buildContext(
        "/** @constructor */ function Container() {}\n" +
        "/** @this {Container} */ function initContainer() {\n" +
        "  /** @type {number} */ this.capacity = 100;\n" +
        "}\n");
    Scope scope = context.globalScope;
    Scope.Var containerVar = scope.getVar("Container");
    assertNotNull(containerVar);
    ObjectType instanceType = containerVar.getType().toMaybeFunctionType().getInstanceType();
    assertTrue(instanceType.hasProperty("capacity"));
  }

  @Test(timeout = 4000)
  public void testWindowConstructorGlobalThisBinding() {
    ScopeContext context = buildContext(
        "/** @constructor */ function Window() {}\n");
    Scope scope = context.globalScope;
    Scope.Var windowVar = scope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());
  }

  @Test(timeout = 4000)
  public void testConstantVariableOrIdiom() {
    ScopeContext context = buildContext(
        "var ns = ns || {};\n" +
        "/** @const */ var MAX_LIMIT = MAX_LIMIT || 5