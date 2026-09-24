/*
 * Copyright 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.TypedScopeCreator
 *
 * Defect Under Analysis (Defects4J Ground Truth):
 * - TypeCheckTest::testIssue86 (AssertionFailedError: expected a warning)
 * - AmbiguatePropertiesTest::testImplementsAndExtends
 *
 * Root Cause Analysis:
 * In TypedScopeCreator.AbstractScopeBuilder#findOverriddenFunction(ObjectType ownerType, String propName),
 * the method inspected superclasses on `ownerType` for an overridden property function, but failed to
 * traverse implemented/extended interfaces of `ownerType`'s constructor. Consequently, methods implementing
 * an interface method did not inherit the interface's function signature, parameter types, or return type.
 *
 * Branch & Coverage Analysis Matrix:
 * - Branch 1: createScope(root, null) vs createScope(fnNode, globalScope)
 * - Branch 2: createInitialScope declaring native functions (Object, Array, Function, Date, Error, etc.)
 *             and value types ("undefined", "goog.typedef", "ActiveXObject")
 * - Branch 3: AbstractScopeBuilder.define: Token.VAR, Token.FUNCTION, Token.ASSIGN, Token.CATCH
 * - Branch 4: Token.VAR single vs multiple variable declarations (reporting MULTIPLE_VAR_DEF when annotated)
 * - Branch 5: Function aliasing (var Alias = OriginalCtor) -> declares constructor type in registry
 * - Branch 6: Prototype assignment (F.prototype = {...}) -> undeclares inferred prototype property
 * - Branch 7: Enum handling (Token.OBJECTLIT, enum alias, duplicate keys ENUM_DUP, non-constant keys
 *             ENUM_NOT_CONSTANT, non-object initializer ENUM_INITIALIZER)
 * - Branch 8: Typedef handling (standard @typedef, GETPROP typedef, malformed typedef without type)
 * - Branch 9: GlobalScopeBuilder.visit Token.CALL constructs:
 *             - goog.inherits (subclass relationship)
 *             - goog.addSingletonGetter
 *             - goog.reflect.object (ObjectLiteralCast, reporting CONSTRUCTOR_EXPECTED on invalid target)
 * - Branch 10: CollectProperties shallow traversal on constructor body (`this.prop = ...`)
 * - Branch 11: LocalScopeBuilder: bleeding function detection, function parameters matching JSDoc
 *              parameters, and local catch parameters
 * - Branch 12: StubDeclaration resolution for property stubs on externs and prototype targets
 */
public class TypedScopeCreatorGeminiTest {

  private Compiler lastCompiler;
  private TypedScopeCreator lastCreator;
  private Node lastRoot;

  /**
   * Helper to initialize compiler, parse inputs and externs, and create a typed global scope.
   */
  private Scope buildGlobalScope(String externsJs, String js) {
    lastCompiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    lastCompiler.init(
        Lists.newArrayList(JSSourceFile.fromCode("externs.js", externsJs)),
        Lists.newArrayList(JSSourceFile.fromCode("testcode.js", js)),
        options);
    lastRoot = lastCompiler.parseInputs();
    assertNotNull("Root AST node must not be null after parseInputs()", lastRoot);

    lastCreator = new TypedScopeCreator(lastCompiler);
    return lastCreator.createScope(lastRoot, null);
  }

  private Scope buildGlobalScope(String js) {
    return buildGlobalScope("", js);
  }

  /**
   * Traverses AST recursively to find a function declaration or expression by name.
   */
  private Node findFunctionNode(Node node, String fnName) {
    if (node.getType() == Token.FUNCTION) {
      Node nameNode = node.getFirstChild();
      if (nameNode != null && fnName.equals(nameNode.getString())) {
        return node;
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node match = findFunctionNode(child, fnName);
      if (match != null) {
        return match;
      }
    }
    return null;
  }

  private boolean hasWarning(DiagnosticType type) {
    for (JSError warning : lastCompiler.getWarnings()) {
      if (warning.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInitialScopeDeclaresNativeTypes() {
    Scope scope = buildGlobalScope("");
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
    assertNotNull(scope.getVar("goog.typedef"));
    assertNotNull(scope.getVar("ActiveXObject"));

    assertEquals(
        lastCompiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE),
        scope.getVar("undefined").getType());
    assertEquals(
        lastCompiler.getTypeRegistry().getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        scope.getVar("ActiveXObject").getType());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationRegistersConstructorAndPrototype() {
    String js = "/** @constructor */ function Animal() {}";
    Scope scope = buildGlobalScope(js);

    Scope.Var animalVar = scope.getVar("Animal");
    assertNotNull("Constructor Animal should be declared", animalVar);
    assertTrue(animalVar.getType() instanceof FunctionType);

    FunctionType fnType = (FunctionType) animalVar.getType();
    assertTrue("Animal should be recognized as a constructor", fnType.isConstructor());

    Scope.Var protoVar = scope.getVar("Animal.prototype");
    assertNotNull("Animal.prototype should be declared in the scope", protoVar);
    assertEquals(fnType.getPrototype(), protoVar.getType());
  }

  @Test(timeout = 4000)
  public void testCollectPropertiesWithinConstructorThis() {
    String js =
        "/** @constructor */\n" +
        "function Person() {\n" +
        "  /** @type {string} */\n" +
        "  this.name = 'test';\n" +
        "  /** @type {number} */\n" +
        "  this.age = 42;\n" +
        "}";
    Scope scope = buildGlobalScope(js);
    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    FunctionType personCtor = (FunctionType) personVar.getType();
    ObjectType instanceType = personCtor.getInstanceType();

    assertTrue("Person instance must possess property 'name'", instanceType.hasProperty("name"));
    assertEquals("string", instanceType.getPropertyType("name").toString());

    assertTrue("Person instance must possess property 'age'", instanceType.hasProperty("age"));
    assertEquals("number", instanceType.getPropertyType("age").toString());
  }

  @Test(timeout = 4000)
  public void testLocalScopeCreationAndVariableHoisting() {
    String js =
        "function compute(x, y) {\n" +
        "  var result = x + y;\n" +
        "  function helper() {}\n" +
        "  try { throw 'err'; } catch (e) { var inCatch = e; }\n" +
        "  return result;\n" +
        "}";
    Scope globalScope = buildGlobalScope(js);
    Node computeFn = findFunctionNode(lastRoot, "compute");
    assertNotNull("Function node compute must exist", computeFn);

    Scope localScope = lastCreator.createScope(computeFn, globalScope);
    assertTrue("Newly created scope must be local", localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());

    assertNotNull("Parameter x must be in local scope", localScope.getVar("x"));
    assertNotNull("Parameter y must be in local scope", localScope.getVar("y"));
    assertNotNull("Local var result must be in local scope", localScope.getVar("result"));
    assertNotNull("Nested function helper must be in local scope", localScope.getVar("helper"));
    assertNotNull("Catch parameter e must be in local scope", localScope.getVar("e"));
    assertNotNull("Variable inCatch must be in local scope", localScope.getVar("inCatch"));
  }

  @Test(timeout = 4000)
  public void testBleedingNamedFunctionExpression() {
    String js = "var outer = function bleeding(a) { return a; };";
    Scope globalScope = buildGlobalScope(js);
    assertNull("Bleeding function name should not exist in global scope", globalScope.getVar("bleeding"));

    Node bleedingFn = findFunctionNode(lastRoot, "bleeding");
    assertNotNull(bleedingFn);

    Scope localScope = lastCreator.createScope(bleedingFn, globalScope);
    assertNotNull("Bleeding function name must be declared in its own local scope",
        localScope.getVar("bleeding"));
    assertEquals(bleedingFn.getJSType(), localScope.getVar("bleeding").getType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptySourceTraversal() {
    Scope scope = buildGlobalScope("");
    assertNotNull("Empty source should build a valid global scope", scope);
    assertTrue(scope.isGlobal());
  }

  @Test(timeout = 4000)
  public void testMultipleUnannotatedVarDeclarations() {
    String js = "var a = 1, b = 'str', c = true;";
    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));
    assertFalse("Multiple unannotated vars should not trigger MULTIPLE_VAR_DEF",
        hasWarning(TypeCheck.MULTIPLE_VAR_DEF));
  }

  @Test(timeout = 4000)
  public void testFunctionWithMismatchedParamCount() {
    String js =
        "/**\n" +
        " * @param {number} x\n" +
        " */\n" +
        "function varargs(x, y, z) {}\n";
    Scope globalScope = buildGlobalScope(js);
    Node fnNode = findFunctionNode(lastRoot, "varargs");
    assertNotNull(fnNode);

    Scope localScope = lastCreator.createScope(fnNode, globalScope);
    Scope.Var varX = localScope.getVar("x");
    Scope.Var varY = localScope.getVar("y");
    Scope.Var varZ = localScope.getVar("z");

    assertNotNull(varX);
    assertNotNull(varY);
    assertNotNull(varZ);

    assertEquals("number", varX.getType().toString());
    assertNull("Extra AST parameter y should have inferred null initial type", varY.getType());
    assertNull("Extra AST parameter z should have inferred null initial type", varZ.getType());
  }

  @Test(timeout = 4000)
  public void testFunctionWithMoreJsDocParamsThanAstParams() {
    String js =
        "/**\n" +
        " * @param {number} a\n" +
        " * @param {string} b\n" +
        " */\n" +
        "function singleArg(a) {}\n";
    Scope globalScope = buildGlobalScope(js);
    Node fnNode = findFunctionNode(lastRoot, "singleArg");
    assertNotNull(fnNode);

    Scope localScope = lastCreator.createScope(fnNode, globalScope);
    assertNotNull("Parameter a must exist", localScope.getVar("a"));
    assertNull("Parameter b should not be declared in local AST scope", localScope.getVar("b"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (TypeCheckTest::testIssue86 & AmbiguatePropertiesTest)
  // =========================================================================

  /**
   * Targets the defect where findOverriddenFunction did not inspect interfaces implemented
   * by the class when defining a method on the prototype.
   */
  @Test(timeout = 4000)
  public void testDefectIssue86InterfaceMethodOverriddenReturnTypeInferred() {
    String js =
        "/** @interface */\n" +
        "function Foo() {}\n" +
        "/** @return {number} */\n" +
        "Foo.prototype.bar = function() {};\n" +
        "/** @constructor\n * @implements {Foo} */\n" +
        "function Bar() {}\n" +
        "Bar.prototype.bar = function() { return 1; };\n";
    Scope scope = buildGlobalScope(js);
    Scope.Var barMethod = scope.getVar("Bar.prototype.bar");
    assertNotNull("Bar.prototype.bar should be declared in scope", barMethod);

    JSType type = barMethod.getType();
    assertTrue("Bar.prototype.bar should be a FunctionType", type instanceof FunctionType);

    FunctionType fnType = (FunctionType) type;
    assertEquals(
        "Bar.prototype.bar should inherit return type 'number' from Foo interface definition",
        "number",
        fnType.getReturnType().toString());
  }

  /**
   * Targets the defect when an interface is implemented across a class inheritance hierarchy.
   */
  @Test(timeout = 4000)
  public void testDefectImplementsAndExtendsInterfaceMethod() {
    String js =
        "/** @interface */\n" +
        "function AnInterface() {}\n" +
        "/** @param {string} x\n * @return {boolean} */\n" +
        "AnInterface.prototype.action = function(x) {};\n" +
        "/** @constructor */\n" +
        "function SuperClass() {}\n" +
        "/** @constructor\n * @extends {SuperClass}\n * @implements {AnInterface} */\n" +
        "function SubClass() {}\n" +
        "SubClass.prototype.action = function(x) { return true; };\n";
    Scope scope = buildGlobalScope(js);
    Scope.Var actionVar = scope.getVar("SubClass.prototype.action");
    assertNotNull("SubClass.prototype.action must exist in scope", actionVar);

    assertTrue(actionVar.getType() instanceof FunctionType);
    FunctionType actionFn = (FunctionType) actionVar.getType();
    assertEquals("SubClass.prototype.action must inherit return type boolean",
        "boolean", actionFn.getReturnType().toString());
    assertEquals("SubClass.prototype.action must inherit parameter type string",
        "string", actionFn.getParameterType(0).toString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleVarDefWarningWhenAnnotated() {
    String js = "/** @type {number} */ var x = 1, y = 2;";
    buildGlobalScope(js);
    assertTrue("Annotating multiple vars in single statement must report MULTIPLE_VAR_DEF",
        hasWarning(TypeCheck.MULTIPLE_VAR_DEF));
  }

  @Test(timeout = 4000)
  public void testEnumDuplicateKeyWarning() {
    String js = "/** @enum {number} */ var DuplicateEnum = { KEY: 1, KEY: 2 };";
    buildGlobalScope(js);
    assertTrue("Duplicate enum key must report ENUM_DUP warning",
        hasWarning(TypeCheck.ENUM_DUP));
  }

  @Test(timeout = 4000)
  public void testEnumNonConstantKeyWarning() {
    String js = "/** @enum {number} */ var InvalidEnum = { 'lowerCaseKey': 1 };";
    buildGlobalScope(js);
    assertTrue("Non-constant enum key must report ENUM_NOT_CONSTANT warning",
        hasWarning(TypeCheck.ENUM_NOT_CONSTANT));
  }

  @Test(timeout = 4000)
  public void testEnumNonObjectLiteralInitializerWarning() {
    String js = "/** @enum {number} */ var ScalarEnum = 123;";
    buildGlobalScope(js);
    assertTrue("Non-object-literal enum initializer must report ENUM_INITIALIZER warning",
        hasWarning(TypedScopeCreator.ENUM_INITIALIZER));
  }

  @Test(timeout = 4000)
  public void testMalformedTypedefReportsWarning() {
    String js = "/** @typedef */ var EmptyTypedef;";
    buildGlobalScope(js);
    assertTrue("Typedef without type definition must report MALFORMED_TYPEDEF warning",
        hasWarning(TypedScopeCreator.MALFORMED_TYPEDEF));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastReportsConstructorExpected() {
    String js =
        "var goog = { reflect: {} };\n" +
        "goog.reflect.object = function(c, o) {};\n" +
        "var notAConstructor = 123;\n" +
        "goog.reflect.object(notAConstructor, {});\n";
    buildGlobalScope(js);
    assertTrue("Object literal cast with non-constructor must report CONSTRUCTOR_EXPECTED",
        hasWarning(TypedScopeCreator.CONSTRUCTOR_EXPECTED));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testValidEnumAndEnumAliasing() {
    String js =
        "/** @enum {string} */\n" +
        "var Status = { OK: 'ok', ERROR: 'error' };\n" +
        "/** @enum {string} */\n" +
        "var StatusAlias = Status;\n";
    Scope scope = buildGlobalScope(js);

    Scope.Var statusVar = scope.getVar("Status");
    assertNotNull(statusVar);
    assertTrue(statusVar.getType() instanceof EnumType);
    EnumType statusEnum = (EnumType) statusVar.getType();
    assertTrue(statusEnum.hasOwnProperty("OK"));
    assertTrue(statusEnum.hasOwnProperty("ERROR"));

    Scope.Var aliasVar = scope.getVar("StatusAlias");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType() instanceof EnumType);
    assertEquals(statusEnum, aliasVar.getType());
  }

  @Test(timeout = 4000)
  public void testTypedefRegistrationInRegistry() {
    String js = "/** @typedef {(string|number)} */ var CustomType;";
    buildGlobalScope(js);
    JSType registryType = lastCompiler.getTypeRegistry().getType("CustomType");
    assertNotNull("Typedef should be registered in TypeRegistry", registryType);
    assertTrue("CustomType should be union type", registryType.isUnionType());
  }

  @Test(timeout = 4000)
  public void testFunctionAliasingDeclaresConstructorInRegistry() {
    String js =
        "/** @constructor */ function Original() {}\n" +
        "var Alias = Original;\n";
    Scope scope = buildGlobalScope(js);
    Scope.Var aliasVar = scope.getVar("Alias");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType() instanceof FunctionType);
    FunctionType aliasFn = (FunctionType) aliasVar.getType();
    assertTrue(aliasFn.isConstructor());

    JSType declaredType = lastCompiler.getTypeRegistry().getType("Alias");
    assertNotNull("Constructor alias should be registered in TypeRegistry", declaredType);
  }

  @Test(timeout = 4000)
  public void testPrototypeUndeclareOnObjectLiteralAssignment() {
    String js =
        "function Widget() {}\n" +
        "Widget.prototype = { render: function() {} };\n";
    Scope scope = buildGlobalScope(js);
    Scope.Var renderVar = scope.getVar("Widget.prototype.render");
    assertNotNull("Widget.prototype.render should be recognized in global scope", renderVar);
  }

  @Test(timeout = 4000)
  public void testStubDeclarationsInGlobalScope() {
    String js =
        "var myNamespace = {};\n" +
        "myNamespace.stubProperty;\n";
    Scope scope = buildGlobalScope(js);
    Scope.Var stubVar = scope.getVar("myNamespace.stubProperty");
    assertNotNull("Stub property must be declared in scope", stubVar);
    assertTrue("Stub property type must be inferred", stubVar.isTypeInferred());
    assertEquals(
        lastCompiler.getTypeRegistry().getNativeObjectType(JSTypeNative.UNKNOWN_TYPE),
        stubVar.getType());
  }

  @Test(timeout = 4000)
  public void testGoogInheritsSubclassRelationship() {
    String js =
        "var goog = {};\n" +
        "goog.inherits = function(child, parent) {};\n" +
        "/** @constructor */ function Parent() {}\n" +
        "/** @constructor */ function Child() {}\n" +
        "goog.inherits(Child, Parent);\n";
    Scope scope = buildGlobalScope(js);

    FunctionType childCtor = (FunctionType) scope.getVar("Child").getType();
    FunctionType parentCtor = (FunctionType) scope.getVar("Parent").getType();

    assertNotNull(childCtor.getSuperClassConstructor());
    assertEquals(parentCtor, childCtor.getSuperClassConstructor());
    assertTrue(childCtor.getPrototype().isSubtype(parentCtor.getPrototype()));
  }

  @Test(timeout = 4000)
  public void testGoogAddSingletonGetter() {
    String js =
        "var goog = {};\n" +
        "goog.addSingletonGetter = function(ctor) {};\n" +
        "/** @constructor */ function Service() {}\n" +
        "goog.addSingletonGetter(Service);\n";
    Scope scope = buildGlobalScope(js);

    FunctionType serviceCtor = (FunctionType) scope.getVar("Service").getType();
    assertTrue("getInstance property must be added by singleton getter",
        serviceCtor.hasProperty("getInstance"));
    assertTrue(serviceCtor.getPropertyType("getInstance") instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testCustomCodingConventionConstructor() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Lists.newArrayList(JSSourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(JSSourceFile.fromCode("testcode.js", "var a = 1;")),
        options);
    Node root = compiler.parseInputs();

    TypedScopeCreator creator = new TypedScopeCreator(compiler, CodingConventions.getDefault());
    Scope scope = creator.createScope(root, null);
    assertNotNull(scope);
    assertNotNull(scope.getVar("a"));
  }
}