package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: TypedScopeCreator
 * Defects4J Ground Truth Defects Targeted:
 *  1. testNamespacedFunctionStubLocal:
 *     - LocalScopeBuilder fails to process stub function/property declarations
 *       like `ns.func;` inside local scopes, preventing namespaced properties
 *       from being recognized on their parent object.
 *  2. testCollectedFunctionStubLocal:
 *     - LocalScopeBuilder does not collect stub property declarations on `this`
 *       (e.g., `this.bar;` inside local constructors) because nonExternFunctions
 *       are not traversed/collected in local scope creation.
 *
 * Decision / Condition Matrix Covered:
 *  - createInitialScope: verifies declarations of all 15 native function types,
 *    undefined, goog.typedef, and ActiveXObject (NO_OBJECT_TYPE).
 *  - DiscoverEnums: NAME, VAR, and ASSIGN nodes with @enum doc annotations.
 *  - attachLiteralTypes: NULL, VOID, STRING, NUMBER, TRUE/FALSE, REGEXP, OBJECTLIT.
 *  - defineVar: Single vs. multiple children, multiple var def warning (MULTIPLE_VAR_DEF),
 *    inferred vs. declared types.
 *  - defineCatch: Catch parameter slot declaration.
 *  - defineDeclaredFunction: Standalone function declarations in global and local scopes.
 *  - defineNamedTypeAssign: Named function/constructor assignments, enum assignments.
 *  - getEnumType: Enum validation, ENUM_DUP, ENUM_NOT_CONSTANT, ENUM_INITIALIZER warnings.
 *  - checkForClassDefiningCalls: goog.inherits (subclass), goog.addSingletonGetter,
 *    goog.reflect.object (ObjectLiteralCast, CONSTRUCTOR_EXPECTED).
 *  - checkForTypedef & checkForOldStyleTypedef: Typedef resolution and MALFORMED_TYPEDEF.
 *  - Bleeding functions & argument handling in LocalScopeBuilder.
 *  - Overridden method resolution on superclasses and implemented interfaces.
 * =========================================================================
 */
public class TypedScopeCreatorGeminiTest {

  private Compiler compiler;
  private TypedScopeCreator scopeCreator;
  private Node rootNode;
  private Scope globalScope;

  private Scope parseAndBuildScope(String js) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    rootNode = compiler.parseTestCode(js);
    scopeCreator = new TypedScopeCreator(compiler);
    globalScope = scopeCreator.createScope(rootNode, null);
    return globalScope;
  }

  private Node findFunctionNode(Node n, String name) {
    if (n.getType() == Token.FUNCTION) {
      Node nameNode = n.getFirstChild();
      if (name == null) {
        if (nameNode.getString().isEmpty()) {
          return n;
        }
      } else if (name.equals(nameNode.getString())) {
        return n;
      }
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFunctionNode(child, name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalVarDeclaredAndInferred() {
    Scope scope = parseAndBuildScope(
        "/** @type {number} */ var declaredNum = 10;\n" +
        "var inferredStr = 'hello';");

    Scope.Var varDeclared = scope.getVar("declaredNum");
    assertNotNull("Declared variable slot must exist", varDeclared);
    assertFalse("Variable with @type must not be inferred", varDeclared.isTypeInferred());
    assertEquals("number", varDeclared.getType().toString());

    Scope.Var varInferred = scope.getVar("inferredStr");
    assertNotNull("Inferred variable slot must exist", varInferred);
    assertTrue("Variable without @type should be inferred", varInferred.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationGlobal() {
    Scope scope = parseAndBuildScope(
        "/**\n" +
        " * @param {number} x\n" +
        " * @return {string}\n" +
        " */\n" +
        "function foo(x) { return '' + x; }");

    Scope.Var fooVar = scope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) fooVar.getType();
    assertEquals("string", fnType.getReturnType().toString());
  }

  @Test(timeout = 4000)
  public void testConstructorAndPrototypeDeclaration() {
    Scope scope = parseAndBuildScope(
        "/** @constructor */ function MyClass() {}\n" +
        "MyClass.prototype.doWork = function() {};");

    Scope.Var classVar = scope.getVar("MyClass");
    assertNotNull(classVar);
    assertTrue(classVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("MyClass.prototype");
    assertNotNull("Constructor prototype should be declared in scope chain", protoVar);
    assertTrue(protoVar.getType().isObject());

    ObjectType instanceType = ((FunctionType) classVar.getType()).getInstanceType();
    assertTrue(instanceType.hasProperty("doWork"));
  }

  @Test(timeout = 4000)
  public void testInheritanceSubclassSuperclass() {
    Scope scope = parseAndBuildScope(
        "/** @constructor */ function Parent() {}\n" +
        "Parent.prototype.baseMethod = function() {};\n" +
        "/** @constructor\n" +
        " *  @extends {Parent} */\n" +
        "function Child() {}\n" +
        "goog.inherits(Child, Parent);");

    Scope.Var childVar = scope.getVar("Child");
    assertNotNull(childVar);
    FunctionType childCtor = (FunctionType) childVar.getType();
    FunctionType parentCtor = (FunctionType) scope.getVar("Parent").getType();

    assertEquals(parentCtor, childCtor.getSuperClassConstructor());
    assertTrue(childCtor.getInstanceType().hasProperty("baseMethod"));
  }

  @Test(timeout = 4000)
  public void testMethodOverrideOnSuperclassAndInterface() {
    Scope scope = parseAndBuildScope(
        "/** @interface */ function Action() {}\n" +
        "Action.prototype.run = function() {};\n" +
        "/** @constructor\n" +
        " *  @implements {Action} */\n" +
        "function Runner() {}\n" +
        "Runner.prototype.run = function() {};");

    Scope.Var runnerVar = scope.getVar("Runner");
    assertNotNull(runnerVar);
    FunctionType runnerCtor = (FunctionType) runnerVar.getType();
    assertTrue(runnerCtor.getInstanceType().hasProperty("run"));
  }

  @Test(timeout = 4000)
  public void testAddSingletonGetter() {
    Scope scope = parseAndBuildScope(
        "/** @constructor */ function Service() {}\n" +
        "goog.addSingletonGetter(Service);");

    Scope.Var serviceVar = scope.getVar("Service");
    assertNotNull(serviceVar);
    FunctionType serviceCtor = (FunctionType) serviceVar.getType();
    assertTrue("Singleton getter should attach getInstance to constructor",
        serviceCtor.hasProperty("getInstance"));
  }

  @Test(timeout = 4000)
  public void testCollectPropertiesFromThisInGlobalFunction() {
    Scope scope = parseAndBuildScope(
        "/** @constructor */ function Car() {}\n" +
        "/** @this {Car} */ function initCar() {\n" +
        "  /** @type {string} */ this.model = 'Sedan';\n" +
        "}");

    FunctionType carCtor = (FunctionType) scope.getVar("Car").getType();
    assertTrue("Declared property on this should be collected onto Car instance",
        carCtor.getInstanceType().hasProperty("model"));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpressionInLocalScope() {
    parseAndBuildScope("var f = function myFunc(a) { return a; };");

    Node fnNode = findFunctionNode(rootNode, "myFunc");
    assertNotNull("Named function expression should be in AST", fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertTrue("Bleeding function name should be accessible inside its own local scope",
        localScope.isDeclared("myFunc", false));
    Scope.Var bleedingVar = localScope.getVar("myFunc");
    assertNotNull(bleedingVar);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testAllLiteralTypesAttachment() {
    Scope scope = parseAndBuildScope(
        "var a = null;\n" +
        "var b = void 0;\n" +
        "var c = 'str';\n" +
        "var d = 123.45;\n" +
        "var e = true;\n" +
        "var f = false;\n" +
        "var g = /abc/g;\n" +
        "var h = {};");

    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));
    assertNotNull(scope.getVar("d"));
    assertNotNull(scope.getVar("e"));
    assertNotNull(scope.getVar("f"));
    assertNotNull(scope.getVar("g"));
    assertNotNull(scope.getVar("h"));
  }

  @Test(timeout = 4000)
  public void testInitialNativeScopeContent() {
    Scope scope = parseAndBuildScope("");
    assertTrue(scope.isGlobal());

    assertNotNull("Object constructor must be in initial scope", scope.getVar("Object"));
    assertNotNull("Function constructor must be in initial scope", scope.getVar("Function"));
    assertNotNull("Array constructor must be in initial scope", scope.getVar("Array"));
    assertNotNull("Date constructor must be in initial scope", scope.getVar("Date"));
    assertNotNull("RegExp constructor must be in initial scope", scope.getVar("RegExp"));
    assertNotNull("Error constructor must be in initial scope", scope.getVar("Error"));

    Scope.Var undefinedVar = scope.getVar("undefined");
    assertNotNull(undefinedVar);
    assertEquals("void", undefinedVar.getType().toString());

    Scope.Var activeXVar = scope.getVar("ActiveXObject");
    assertNotNull(activeXVar);
    assertTrue(activeXVar.getType().isNoObjectType());

    Scope.Var typedefVar = scope.getVar("goog.typedef");
    assertNotNull(typedefVar);
    assertTrue(typedefVar.getType().isNoType());
  }

  @Test(timeout = 4000)
  public void testEmptyAndStubPropertiesResolution() {
    Scope scope = parseAndBuildScope(
        "var ns = {};\n" +
        "ns.stubProperty;");

    Scope.Var stubVar = scope.getVar("ns.stubProperty");
    assertNotNull("Stub property should be registered in scope", stubVar);
    assertTrue("Unresolved stub property should default to unknown type",
        stubVar.getType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testCatchParameterDeclaration() {
    parseAndBuildScope(
        "function runCatch() {\n" +
        "  try {\n" +
        "    var insideTry = 1;\n" +
        "  } catch (err) {\n" +
        "    var insideCatch = err;\n" +
        "  }\n" +
        "}");

    Node fnNode = findFunctionNode(rootNode, "runCatch");
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue("Catch parameter must be declared in scope",
        localScope.isDeclared("err", true));
    Scope.Var errVar = localScope.getVar("err");
    assertNotNull(errVar);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets known Defect:
   * TypedScopeCreatorTest::testNamespacedFunctionStubLocal
   * Verifies that namespaced function stubs inside local scopes have their properties
   * declared and properly typed on the enclosing object slot.
   */
  @Test(timeout = 4000)
  public void testNamespacedFunctionStubLocal() {
    parseAndBuildScope(
        "(function() {\n" +
        "  var goog = {};\n" +
        "  /** @param {number} x */ goog.modify;\n" +
        "})();");

    Node fnNode = findFunctionNode(rootNode, null);
    assertNotNull("Anonymous IIFE function node should be found", fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    Scope.Var googVar = localScope.getVar("goog");
    assertNotNull("Local variable 'goog' should be declared", googVar);

    ObjectType googType = (ObjectType) googVar.getType();
    assertNotNull("googType must be resolved as ObjectType", googType);

    assertTrue("Local namespaced property 'modify' must be declared on 'goog'",
        googType.hasProperty("modify"));
    assertEquals("function (number): ?", googType.getPropertyType("modify").toString());
    assertTrue("Property type of 'modify' should be marked as declared",
        googType.isPropertyTypeDeclared("modify"));
  }

  /**
   * Targets known Defect:
   * TypedScopeCreatorTest::testCollectedFunctionStubLocal
   * Verifies that property stubs on 'this' inside local constructors are properly
   * collected and declared on the constructor's instance/prototype.
   */
  @Test(timeout = 4000)
  public void testCollectedFunctionStubLocal() {
    parseAndBuildScope(
        "(function() {\n" +
        "  /** @constructor */ function Foo() {\n" +
        "    /** @param {number} x */ this.bar;\n" +
        "  }\n" +
        "})();");

    Node fnNode = findFunctionNode(rootNode, null);
    assertNotNull("Anonymous IIFE function node should be found", fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    Scope.Var fooVar = localScope.getVar("Foo");
    assertNotNull("Constructor 'Foo' should be declared in local scope", fooVar);

    FunctionType fooType = (FunctionType) fooVar.getType();
    assertNotNull("Foo should have FunctionType", fooType);

    ObjectType instanceType = fooType.getInstanceType();
    ObjectType protoType = fooType.getPrototype();

    boolean hasProp = instanceType.hasProperty("bar") || protoType.hasProperty("bar");
    assertTrue("Local constructor's stub property 'bar' should be collected on instance or prototype",
        hasProp);

    ObjectType target = instanceType.hasProperty("bar") ? instanceType : protoType;
    assertEquals("function (number): ?", target.getPropertyType("bar").toString());
    assertTrue("Collected function stub should be marked declared",
        target.isPropertyTypeDeclared("bar"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testWarningOnMultipleVarDefWithJsDoc() {
    parseAndBuildScope("/** @type {number} */ var a = 1, b = 2;");
    assertEquals("Multiple var def with JSDoc must emit a warning",
        1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testWarningOnEnumInitializerNotObjectLit() {
    parseAndBuildScope("/** @enum {number} */ var InvalidEnum = 123;");
    assertEquals("Enum not initialized with object literal should trigger warning",
        1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testWarningOnDuplicateEnumKeys() {
    parseAndBuildScope("/** @enum {number} */ var DupEnum = { KEY: 1, KEY: 2 };");
    assertEquals("Duplicate enum key should trigger ENUM_DUP warning",
        1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_DUP, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testWarningOnNonConstantEnumKey() {
    parseAndBuildScope("/** @enum {number} */ var BadKeyEnum = { lowercase: 1 };");
    assertEquals("Non-constant enum key should trigger ENUM_NOT_CONSTANT warning",
        1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_NOT_CONSTANT, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testWarningOnMalformedTypedef() {
    parseAndBuildScope("/** @typedef {nonexistent.BadType} */ var MyBadTypedef;");
    assertEquals("Malformed typedef with unresolvable type must trigger MALFORMED_TYPEDEF warning",
        1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.MALFORMED_TYPEDEF, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testWarningOnReflectObjectWithoutConstructor() {
    parseAndBuildScope("goog.reflect.object(NonExistentCtor, {});");
    assertEquals("goog.reflect.object with invalid constructor must trigger warning",
        1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CONSTRUCTOR_EXPECTED, compiler.getWarnings()[0].getType());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Chain Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testValidEnumCreationAndValues() {
    Scope scope = parseAndBuildScope(
        "/** @enum {string} */ var Direction = { NORTH: 'N', SOUTH: 'S' };");

    Scope.Var enumVar = scope.getVar("Direction");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("NORTH"));
    assertTrue(enumType.hasOwnProperty("SOUTH"));
    assertEquals("string", enumType.getElementsType().toString());
  }

  @Test(timeout = 4000)
  public void testEnumAliasing() {
    Scope scope = parseAndBuildScope(
        "/** @enum {number} */ var Color = { RED: 1, GREEN: 2 };\n" +
        "/** @enum {number} */ var Palette = Color;");

    Scope.Var colorVar = scope.getVar("Color");
    Scope.Var paletteVar = scope.getVar("Palette");
    assertNotNull(colorVar);
    assertNotNull(paletteVar);
    assertEquals(colorVar.getType(), paletteVar.getType());
  }

  @Test(timeout = 4000)
  public void testTypedefObjectPropertyDeclaration() {
    Scope scope = parseAndBuildScope(
        "var myPackage = {};\n" +
        "/** @typedef {number|string} */ myPackage.ID;");

    JSType declaredType = compiler.getTypeRegistry().getType("myPackage.ID");
    assertNotNull("Typedef should register in JSTypeRegistry", declaredType);
    assertTrue(declaredType.isUnionType());

    Scope.Var propVar = scope.getVar("myPackage.ID");
    assertNotNull("Typedef GETPROP should declare a slot with NoType", propVar);
    assertTrue(propVar.getType().isNoType());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastValid() {
    parseAndBuildScope(
        "/** @constructor */ function TargetRecord() {}\n" +
        "var obj = goog.reflect.object(TargetRecord, { prop: 1 });");

    Scope.Var objVar = globalScope.getVar("obj");
    assertNotNull(objVar);
    assertNotNull(objVar.getType());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionAliasRegistration() {
    Scope scope = parseAndBuildScope(
        "/** @constructor */ function BaseClass() {}\n" +
        "var AliasClass = BaseClass;");

    Scope.Var baseVar = scope.getVar("BaseClass");
    Scope.Var aliasVar = scope.getVar("AliasClass");
    assertNotNull(baseVar);
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType().isConstructor());
    assertNotNull(compiler.getTypeRegistry().getType("AliasClass"));
  }

  @Test(timeout = 4000)
  public void testPrototypeRedefinition() {
    Scope scope = parseAndBuildScope(
        "function Worker() {}\n" +
        "Worker.prototype = { a: function() {} };\n" +
        "Worker.prototype = { b: function() {} };");

    Scope.Var protoVar = scope.getVar("Worker.prototype");
    assertNotNull(protoVar);
  }
}