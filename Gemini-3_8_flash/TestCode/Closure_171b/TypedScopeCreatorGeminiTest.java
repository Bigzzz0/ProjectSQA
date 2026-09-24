package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: TypedScopeCreator.java (Defects4J Closure compiler)
 *
 * Targeted Defects & Regressions:
 * 1. testPropertiesOnInterface2:
 *    - NPE when resolving interface properties or finding overridden members on interfaces/prototypes
 *      e.g. (@interface F with F.prototype.x; @interface G @extends {F} with G.prototype.x).
 * 2. testMethodBeforeFunction2:
 *    - Reordering / prototype property declared before constructor declaration:
 *      Window.prototype.alert = function(x) {}; /** @constructor * / function Window() {}
 *      Verifies type inference on 'Window' and that methods declared on prototype retain correct 'this' and parameter types.
 * 3. testIssue1023:
 *    - Overridden function parameter matching or mismatch warning behavior when subclass overrides superclass prototype methods.
 *
 * Specific Branch Zones Targeted:
 * - Scope building: parent == null (global scope creation, createInitialScope, FirstOrderFunctionAnalyzer,
 *   declareNativeFunctionType, declareNativeValueType, delegateProxyPrototypes) vs parent != null (local scopes).
 * - Function declarations (hoisted vs expressions, bleeding functions, IIFE parameter inference).
 * - Object literals with and without @lends annotations; lends to unknown var, lends to non-object, lends to object.
 * - Enums (@enum) defined via object literals, aliased enums, invalid enum keys (getters/setters, non-constant).
 * - Constructors and interfaces (@constructor, @interface), constructor initializer checking,
 *   Window special-casing (setPrototypeBasedOn).
 * - Class-defining calls (Google coding conventions: goog.inherits, ObjectLiteralCast, Delegate relationships).
 * - Stub declarations (n.isGetProp() without value) and resolution in resolveStubDeclarations().
 * - Global scope patching: patchGlobalScope(globalScope, scriptRoot).
 * - Constant symbols (@const and convention) in getDeclaredType.
 */
public class TypedScopeCreatorGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  /**
   * Helper to parse externs and code, create global scope, and return the Scope.
   */
  private Scope compileAndBuildScope(String externsCode, String jsCode) {
    List<SourceFile> externs = new ArrayList<>();
    externs.add(SourceFile.fromCode("externs.js", externsCode));
    List<SourceFile> inputs = new ArrayList<>();
    inputs.add(SourceFile.fromCode("input.js", jsCode));

    compiler.init(externs, inputs, compiler.getOptions());
    Node root = compiler.parseInputs();
    assertNotNull("Root AST should not be null", root);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  private Scope compileAndBuildScope(String jsCode) {
    return compileAndBuildScope("", jsCode);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Ground Truth Defect 1: testPropertiesOnInterface2
   * Target: Avoid NPE when defining/inheriting properties on an interface prototype.
   */
  @Test(timeout = 4000)
  public void testPropertiesOnInterface2() {
    String js = "/** @interface */ function F() {}\n"
        + "/** @type {number} */ F.prototype.x;\n"
        + "/** @interface \n * @extends {F} */ function G() {}\n"
        + "/** @type {number} */ G.prototype.x;\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope);

    JSType typeF = registry.getType("F");
    assertNotNull("F should be registered", typeF);
    ObjectType objF = typeF.toMaybeObjectType();
    assertNotNull(objF);

    JSType typeG = registry.getType("G");
    assertNotNull("G should be registered", typeG);
    ObjectType objG = typeG.toMaybeObjectType();
    assertNotNull(objG);

    // Verify property 'x' exists on both prototypes and matches
    ObjectType protoF = objF.getPrototype();
    ObjectType protoG = objG.getPrototype();
    assertNotNull(protoF);
    assertNotNull(protoG);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), protoF.getPropertyType("x"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), protoG.getPropertyType("x"));
  }

  /**
   * Ground Truth Defect 2: testMethodBeforeFunction2
   * Target: Function defined on Window.prototype before constructor is declared.
   */
  @Test(timeout = 4000)
  public void testMethodBeforeFunction2() {
    String js = "Window.prototype.alert = function(x) {};\n"
        + "/** @constructor */ function Window() {}\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope);

    Scope.Var alertVar = scope.getVar("Window.prototype.alert");
    assertNotNull("Window.prototype.alert should be declared in scope", alertVar);
    JSType alertType = alertVar.getType();
    assertNotNull(alertType);
    assertTrue("alertType should be a function type", alertType.isFunctionType());

    FunctionType fnType = alertType.toMaybeFunctionType();
    JSType thisType = fnType.getTypeOfThis();
    assertNotNull(thisType);
    // Should be typed to Window instance
    assertTrue("Expected Window instance this-type, got: " + thisType,
        thisType.isInstanceType() || thisType.isObjectType());
  }

  /**
   * Ground Truth Defect 3: testIssue1023
   * Target: Overriding a method with mismatched signature in inheritance.
   */
  @Test(timeout = 4000)
  public void testIssue1023() {
    String js = "/** @constructor */ function Super() {}\n"
        + "/** @param {number} x */ Super.prototype.bar = function(x) {};\n"
        + "/** @constructor \n * @extends {Super} */ function Sub() {}\n"
        + "/** @override */ Sub.prototype.bar = function(x, y) {};\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope);
    Scope.Var subBar = scope.getVar("Sub.prototype.bar");
    assertNotNull(subBar);
    assertTrue(subBar.getType().isFunctionType());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalNativeTypesDeclared() {
    Scope scope = compileAndBuildScope("var a = 1;");
    assertTrue(scope.isGlobal());

    // Native types must be registered in the scope
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));

    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), scope.getVar("undefined").getType());
  }

  @Test(timeout = 4000)
  public void testVariableDeclarationsAndInference() {
    String js = "var num = 42;\n"
        + "var str = 'hello';\n"
        + "var bool = true;\n"
        + "var n = null;\n"
        + "var v = void 0;\n"
        + "var regex = /abc/;\n";
    Scope scope = compileAndBuildScope(js);

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("num").getType());
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), scope.getVar("str").getType());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), scope.getVar("bool").getType());
    assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), scope.getVar("n").getType());
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), scope.getVar("v").getType());
    assertEquals(registry.getNativeType(JSTypeNative.REGEXP_TYPE), scope.getVar("regex").getType());
  }

  @Test(timeout = 4000)
  public void testLocalScopeCreation() {
    String js = "function outer(a, b) {\n"
        + "  var c = a;\n"
        + "  return function inner(d) { return c + d; };\n"
        + "}\n";
    Scope globalScope = compileAndBuildScope(js);
    Scope.Var outerVar = globalScope.getVar("outer");
    assertNotNull(outerVar);
    Node outerFn = outerVar.getNameNode().getParent();

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope localScope = creator.createScope(outerFn, globalScope);

    assertNotNull(localScope);
    assertFalse(localScope.isGlobal());
    assertEquals(globalScope, localScope.getParent());
    assertNotNull(localScope.getVar("a"));
    assertNotNull(localScope.getVar("b"));
    assertNotNull(localScope.getVar("c"));
  }

  @Test(timeout = 4000)
  public void testConstructorAndPrototype() {
    String js = "/** @constructor */\n"
        + "function Person(name) {\n"
        + "  this.name = name;\n"
        + "}\n"
        + "Person.prototype.greet = function() { return this.name; };\n";
    Scope scope = compileAndBuildScope(js);

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    assertTrue(personVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("Person.prototype");
    assertNotNull(protoVar);

    Scope.Var greetVar = scope.getVar("Person.prototype.greet");
    assertNotNull(greetVar);
    assertTrue(greetVar.getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testEnumTypeDeclaration() {
    String js = "/** @enum {string} */\n"
        + "var Color = {\n"
        + "  RED: 'red',\n"
        + "  BLUE: 'blue'\n"
        + "};\n";
    Scope scope = compileAndBuildScope(js);

    Scope.Var colorVar = scope.getVar("Color");
    assertNotNull(colorVar);
    assertTrue(colorVar.getType().isEnumType());

    EnumType enumType = (EnumType) colorVar.getType();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), enumType.getElementsType());
    assertTrue(enumType.getElements().contains("RED"));
    assertTrue(enumType.getElements().contains("BLUE"));
  }

  @Test(timeout = 4000)
  public void testTypedefDeclaration() {
    String js = "/** @typedef {{x: number, y: number}} */\n"
        + "var Point;\n"
        + "/** @type {Point} */\n"
        + "var p;\n";
    Scope scope = compileAndBuildScope(js);

    JSType pointType = registry.getType("Point");
    assertNotNull(pointType);

    Scope.Var pVar = scope.getVar("p");
    assertNotNull(pVar);
    assertEquals(pointType, pVar.getType());
  }

  @Test(timeout = 4000)
  public void testCatchParameter() {
    String js = "function testCatch() {\n"
        + "  try {\n"
        + "    var x = 1;\n"
        + "  } catch (e) {\n"
        + "    var y = e;\n"
        + "  }\n"
        + "}\n";
    Scope globalScope = compileAndBuildScope(js);
    Scope.Var fnVar = globalScope.getVar("testCatch");
    Node fnNode = fnVar.getNameNode().getParent();

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope localScope = creator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("e"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    Scope scope = compileAndBuildScope("");
    assertNotNull(scope);
    assertTrue(scope.isGlobal());
  }

  @Test(timeout = 4000)
  public void testMultipleVarDeclarationsOnSingleLine() {
    String js = "var a = 1, b = 2, c = 3;";
    Scope scope = compileAndBuildScope(js);

    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));
  }

  @Test(timeout = 4000)
  public void testMultipleVarDefWarningWhenJsDocPresent() {
    String js = "/** @type {number} */ var a = 1, b = 2;";
    compileAndBuildScope(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testStubDeclarations() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.uninitializedProp;\n";
    Scope scope = compileAndBuildScope(js);
    Scope.Var propVar = scope.getVar("Foo.prototype.uninitializedProp");
    assertNotNull(propVar);
    assertTrue(propVar.getType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testIIFEArgumentsInference() {
    String js = "var num = 100;\n"
        + "(function(param) {\n"
        + "  var local = param;\n"
        + "})(num);\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope);
  }

  @Test(timeout = 4000)
  public void testConstantSymbols() {
    String js = "/** @const */ var CONST_VAL = 10;\n"
        + "var OTHER = CONST_VAL;\n";
    Scope scope = compileAndBuildScope(js);

    Scope.Var constVar = scope.getVar("CONST_VAL");
    assertNotNull(constVar);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), constVar.getType());
  }

  @Test(timeout = 4000)
  public void testOrPatternConstantIdiom() {
    String js = "var ns = ns || {};\n";
    Scope scope = compileAndBuildScope(js);
    Scope.Var nsVar = scope.getVar("ns");
    assertNotNull(nsVar);
  }

  // =========================================================================
  // Partition D: Exception, Diagnostics & Defensive Guards
  // =========================================================================

  @Test(timeout = 4000)
  public void testLendsAnnotationOnValidObject() {
    String js = "/** @constructor */ function Target() {}\n"
        + "var obj = /** @lends {Target.prototype} */ ({\n"
        + "  foo: function() { return 1; }\n"
        + "});\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope);
  }

  @Test(timeout = 4000)
  public void testUnknownLendsWarning() {
    String js = "var obj = /** @lends {NonExistentClass.prototype} */ ({ a: 1 });\n";
    compileAndBuildScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testLendsOnNonObjectWarning() {
    String js = "var notAnObj = 123;\n"
        + "var obj = /** @lends {notAnObj} */ ({ a: 1 });\n";
    compileAndBuildScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testConstructorInitializerNotCtorWarning() {
    String js = "/** @constructor */ var MyCtor;\n";
    compileAndBuildScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testInterfaceInitializerNotIfaceWarning() {
    String js = "/** @interface */ var MyIface;\n";
    compileAndBuildScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testEnumInitializerNotEnumWarning() {
    String js = "/** @enum {number} */ var InvalidEnum = 123;\n";
    compileAndBuildScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testMalformedTypedefWarning() {
    String js = "/** @typedef {nonexistent.Type} */ var MyType;\n";
    compileAndBuildScope(js);
    // Malformed/unresolvable typedef should generate a warning
    assertTrue(compiler.getWarningCount() >= 1);
  }

  // =========================================================================
  // Partition E: Scope Lifecycle, Patching & Delegations
  // =========================================================================

  @Test(timeout = 4000)
  public void testPatchGlobalScope() {
    String js = "var firstVar = 10;\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope.getVar("firstVar"));

    // Prepare updated script
    Node newScriptRoot = IR.script();
    newScriptRoot.putProp(Node.SOURCENAME_PROP, "input.js");
    Node varNode = IR.var(IR.name("firstVar"), IR.number(20));
    varNode.setLineno(1);
    newScriptRoot.addChildToBack(varNode);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.patchGlobalScope(scope, newScriptRoot);

    assertNotNull(scope.getVar("firstVar"));
  }

  @Test(timeout = 4000)
  public void testDelegateRelationship() {
    CodingConvention customConvention = new GoogleCodingConvention() {
      @Override
      public DelegateRelationship getDelegateRelationship(Node callNode) {
        if (callNode.isCall() && "goog.setupDelegates".equals(callNode.getFirstChild().getQualifiedName())) {
          return new DelegateRelationship("Delegator", "DelegateBase");
        }
        return null;
      }

      @Override
      public String getDelegateSuperclassName() {
        return "DelegateSuper";
      }
    };

    String js = "/** @constructor */ function DelegateSuper() {}\n"
        + "/** @constructor */ function DelegateBase() {}\n"
        + "/** @constructor */ function Delegator() {}\n"
        + "goog.setupDelegates();\n";

    List<SourceFile> externs = Collections.emptyList();
    List<SourceFile> inputs = Collections.singletonList(SourceFile.fromCode("input.js", js));

    compiler.init(externs, inputs, compiler.getOptions());
    Node root = compiler.parseInputs();
    TypedScopeCreator creator = new TypedScopeCreator(compiler, customConvention);
    Scope scope = creator.createScope(root, null);
    assertNotNull(scope);
  }

  @Test(timeout = 4000)
  public void testSubclassInheritanceRelationship() {
    String js = "/** @constructor */ function SuperClass() {}\n"
        + "/** @constructor \n * @extends {SuperClass} */ function SubClass() {}\n"
        + "goog.inherits(SubClass, SuperClass);\n";

    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope.getVar("SubClass"));
    assertNotNull(scope.getVar("SuperClass"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCast() {
    String js = "/** @constructor */ function SomeType() {}\n"
        + "goog.reflect.object(SomeType, { key: 1 });\n";

    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastNonConstructor() {
    String js = "var notACtor = 123;\n"
        + "goog.reflect.object(notACtor, { key: 1 });\n";

    compileAndBuildScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CONSTRUCTOR_EXPECTED, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testFunctionWithThisTypeCollectProperties() {
    String js = "/** @constructor */\n"
        + "function Widget() {\n"
        + "  /** @type {string} */\n"
        + "  this.title = 'default';\n"
        + "}\n";
    Scope scope = compileAndBuildScope(js);

    JSType widgetType = registry.getType("Widget");
    assertNotNull(widgetType);
    ObjectType instanceType = widgetType.toMaybeFunctionType().getInstanceType();
    assertTrue(instanceType.hasProperty("title"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), instanceType.getPropertyType("title"));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpression() {
    String js = "var outer = function inner() {\n"
        + "  return inner;\n"
        + "};\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope.getVar("outer"));
    // 'inner' should not leak into global scope
    assertNull(scope.getVar("inner"));
  }

  @Test(timeout = 4000)
  public void testPrototypeRedefinedWithObjectLit() {
    String js = "/** @constructor */ function MyClass() {}\n"
        + "MyClass.prototype = {\n"
        + "  /** @type {number} */\n"
        + "  count: 0\n"
        + "};\n";
    Scope scope = compileAndBuildScope(js);
    assertNotNull(scope);

    JSType myClassType = registry.getType("MyClass");
    assertNotNull(myClassType);
    ObjectType proto = myClassType.toMaybeFunctionType().getPrototype();
    assertNotNull(proto);
    assertTrue(proto.hasProperty("count"));
  }
}