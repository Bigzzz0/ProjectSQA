package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Test;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;

import java.util.ArrayList;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target: com.google.javascript.jscomp.TypedScopeCreator
 * Defects4J Ground Truth Defects Targeted:
 *  - TypedScopeCreatorTest::testReturnTypeInference1
 *    (expected: "function (): undefined", actual: "function (): ?")
 *  - TypedScopeCreatorTest::testConstructorNode
 *    (expected: "function (this:goog.Foo): undefined", actual: "function (this:goog.Foo): ?")
 *  - TypedScopeCreatorTest::testConstructorProperty
 *    (expected: "function (this:foo.Bar): undefined", actual: "function (this:foo.Bar): ?")
 *  - TypedScopeCreatorTest::testPropertiesOnInterface
 *    (expected: "function (this:I): undefined", actual: "function (this:I): ?")
 *
 * Core Decision Logic & Branches Targeted:
 *  1. Root Scope Initialization (createInitialScope):
 *     - Verification of built-in JavaScript constructors and primitives
 *       (Object, Array, Boolean, Date, Error, EvalError, Function, Number, RangeError,
 *        ReferenceError, RegExp, String, SyntaxError, TypeError, URIError, undefined,
 *        goog.typedef, ActiveXObject).
 *  2. GlobalScopeBuilder:
 *     - CALL branches: Subclass inherits (goog.inherits), Singleton getter (goog.addSingletonGetter),
 *       Delegate relationship, ObjectLiteralCast (goog.reflect.object).
 *     - FUNCTION / VAR / ASSIGN / CATCH / GETPROP dispatching.
 *     - JSDoc @typedef parsing (standard and old-style typedefs) and error reporting on malformed.
 *     - Qualified name resolution (owner prototype properties, member declarations, stubs).
 *     - CollectProperties traversal in non-extern constructors with explicit @this types.
 *  3. LocalScopeBuilder:
 *     - Local variable definition, function argument declarations, bleeding function expressions,
 *       catch-block parameter definition, deferred type attachment and resolution.
 *  4. Enum Processing (getEnumType & DiscoverEnums):
 *     - Valid enum declarations, enum aliasing, duplicate keys (ENUM_DUP),
 *       non-constant keys (ENUM_NOT_CONSTANT), invalid initializers (ENUM_INITIALIZER).
 *  5. Method Overriding (findOverriddenFunction):
 *     - Superclass method override inference vs interface method implementation inference.
 *  6. Prototype redefinition and assignment to declared prototypes.
 * =========================================================================================
 */
public class TypedScopeCreatorGeminiTest {

  private Compiler compiler;
  private TypedScopeCreator scopeCreator;

  private Scope createGlobalScope(String js) {
    return createGlobalScope(new String[0], js, null);
  }

  private Scope createGlobalScope(String js, CodingConvention convention) {
    return createGlobalScope(new String[0], js, convention);
  }

  private Scope createGlobalScope(String[] externs, String js, CodingConvention convention) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    List<JSSourceFile> externList = new ArrayList<>();
    if (externs != null) {
      for (int i = 0; i < externs.length; i++) {
        externList.add(JSSourceFile.fromCode("extern_" + i + ".js", externs[i]));
      }
    }
    if (externList.isEmpty()) {
      externList.add(JSSourceFile.fromCode("externs.js", ""));
    }

    List<JSSourceFile> inputList = new ArrayList<>();
    inputList.add(JSSourceFile.fromCode("testcode.js", js));

    compiler.init(externList, inputList, options);
    Node root = compiler.parseInputs();
    assertNotNull("Root AST node should not be null", root);

    if (convention != null) {
      scopeCreator = new TypedScopeCreator(compiler, convention);
    } else {
      scopeCreator = new TypedScopeCreator(compiler);
    }
    return scopeCreator.createScope(root, null);
  }

  private Scope createLocalScope(Scope globalScope, String functionName) {
    Node fnNode = findFunctionNode(globalScope.getRootNode(), functionName);
    assertNotNull("Function node '" + functionName + "' not found in AST", fnNode);
    return scopeCreator.createScope(fnNode, globalScope);
  }

  private Node findFunctionNode(Node n, String name) {
    if (n == null) {
      return null;
    }
    if (n.getType() == Token.FUNCTION) {
      Node nameChild = n.getFirstChild();
      if (nameChild != null && name.equals(nameChild.getString())) {
        return n;
      }
    }
    if (n.getType() == Token.VAR) {
      Node varName = n.getFirstChild();
      if (varName != null && name.equals(varName.getString())) {
        Node val = varName.getFirstChild();
        if (val != null && val.getType() == Token.FUNCTION) {
          return val;
        }
      }
    }
    if (n.getType() == Token.ASSIGN) {
      Node lhs = n.getFirstChild();
      if (lhs != null && name.equals(lhs.getQualifiedName())) {
        Node rhs = lhs.getNext();
        if (rhs != null && rhs.getType() == Token.FUNCTION) {
          return rhs;
        }
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
  // PARTITION A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testNativeTypesDeclaredInInitialScope() {
    Scope scope = createGlobalScope("");
    assertTrue("Global scope must be marked global", scope.isGlobal());
    assertFalse("Global scope must not be local", scope.isLocal());

    String[] nativeTypes = {
      "Object", "Array", "Boolean", "Date", "Error", "EvalError",
      "Function", "Number", "RangeError", "ReferenceError", "RegExp",
      "String", "SyntaxError", "TypeError", "URIError",
      "undefined", "goog.typedef", "ActiveXObject"
    };

    for (String typeName : nativeTypes) {
      Scope.Var var = scope.getVar(typeName);
      assertNotNull("Native binding '" + typeName + "' must exist in initial scope", var);
      assertNotNull("Type of native binding '" + typeName + "' must not be null", var.getType());
    }
  }

  @Test(timeout = 4000)
  public void testGlobalVariableDeclarationAndInference() {
    Scope scope = createGlobalScope("var a = 10; var b; /** @type {string} */ var c = 'hello';");
    Scope.Var varA = scope.getVar("a");
    assertNotNull(varA);
    assertTrue("var a should be inferred", varA.isTypeInferred());

    Scope.Var varB = scope.getVar("b");
    assertNotNull(varB);
    assertTrue("var b should be inferred", varB.isTypeInferred());

    Scope.Var varC = scope.getVar("c");
    assertNotNull(varC);
    assertFalse("var c with explicit JSDoc should not be inferred", varC.isTypeInferred());
    assertEquals("string", varC.getType().toString());
  }

  @Test(timeout = 4000)
  public void testLocalScopeCreationAndVariableResolution() {
    Scope globalScope = createGlobalScope(
        "function compute(x, y) {\n" +
        "  var localVar = x + y;\n" +
        "  return localVar;\n" +
        "}\n");

    Scope localScope = createLocalScope(globalScope, "compute");
    assertTrue("Scope must be local", localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());

    Scope.Var varLocal = localScope.getVar("localVar");
    assertNotNull("Local variable 'localVar' must exist in local scope", varLocal);
    assertEquals(localScope, varLocal.getScope());

    Scope.Var paramX = localScope.getVar("x");
    assertNotNull("Parameter 'x' must exist in local scope", paramX);

    Scope.Var paramY = localScope.getVar("y");
    assertNotNull("Parameter 'y' must exist in local scope", paramY);
  }

  @Test(timeout = 4000)
  public void testFunctionParametersWithJsDocInLocalScope() {
    Scope globalScope = createGlobalScope(
        "/**\n" +
        " * @param {number} n\n" +
        " * @param {string} s\n" +
        " */\n" +
        "function typedFn(n, s) {\n" +
        "  var inner = 1;\n" +
        "}\n");

    Scope localScope = createLocalScope(globalScope, "typedFn");
    Scope.Var paramN = localScope.getVar("n");
    assertNotNull(paramN);
    assertNotNull(paramN.getType());
    assertEquals("number", paramN.getType().toString());

    Scope.Var paramS = localScope.getVar("s");
    assertNotNull(paramS);
    assertNotNull(paramS.getType());
    assertEquals("string", paramS.getType().toString());
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpression() {
    Scope globalScope = createGlobalScope("var f = function bleeding(arg) { return bleeding; };");
    Scope localScope = createLocalScope(globalScope, "f");

    Scope.Var bleedVar = localScope.getVar("bleeding");
    assertNotNull("Bleeding function name must be declared in its own scope", bleedVar);
    assertNull("Bleeding function name must not leak into global scope", globalScope.getVar("bleeding"));
  }

  @Test(timeout = 4000)
  public void testCollectPropertiesInConstructorBody() {
    Scope scope = createGlobalScope(
        "/** @constructor */\n" +
        "function Widget() {\n" +
        "  /** @type {number} */\n" +
        "  this.width = 100;\n" +
        "  /** @type {string} */\n" +
        "  this.title;\n" +
        "}\n");

    Scope.Var widgetVar = scope.getVar("Widget");
    assertNotNull(widgetVar);
    assertTrue(widgetVar.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) widgetVar.getType();
    ObjectType instanceType = fnType.getInstanceType();

    assertTrue("Instance must contain property width", instanceType.hasProperty("width"));
    assertTrue("Instance must contain property title", instanceType.hasProperty("title"));
    assertEquals("number", instanceType.getPropertyType("width").toString());
    assertEquals("string", instanceType.getPropertyType("title").toString());
  }

  @Test(timeout = 4000)
  public void testEnumDeclarationAndElements() {
    Scope scope = createGlobalScope(
        "/** @enum {number} */\n" +
        "var Severity = {\n" +
        "  LOW: 1,\n" +
        "  HIGH: 2\n" +
        "};\n");

    Scope.Var enumVar = scope.getVar("Severity");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue("Enum must contain LOW", enumType.hasOwnProperty("LOW"));
    assertTrue("Enum must contain HIGH", enumType.hasOwnProperty("HIGH"));
    assertEquals("number", enumType.getElementsType().toString());

    // Test enum aliasing
    Scope aliasScope = createGlobalScope(
        "/** @enum {number} */ var E = { A: 1 };\n" +
        "/** @enum {number} */ var AliasE = E;\n");
    Scope.Var aliasVar = aliasScope.getVar("AliasE");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType() instanceof EnumType);
  }

  @Test(timeout = 4000)
  public void testSubclassInheritanceWithCodingConvention() {
    Scope scope = createGlobalScope(
        "var goog = {};\n" +
        "goog.inherits = function(child, parent) {};\n" +
        "/** @constructor */ function SuperClass() {}\n" +
        "SuperClass.prototype.superMethod = function() {};\n" +
        "/** @constructor */ function SubClass() {}\n" +
        "goog.inherits(SubClass, SuperClass);\n",
        new ClosureCodingConvention());

    Scope.Var subClassVar = scope.getVar("SubClass");
    assertNotNull(subClassVar);
    assertTrue(subClassVar.getType() instanceof FunctionType);
    FunctionType subCtor = (FunctionType) subClassVar.getType();
    FunctionType superCtor = subCtor.getSuperClassConstructor();
    assertNotNull("SuperClass constructor should be registered on subClass", superCtor);
    assertEquals("SuperClass", superCtor.getInstanceType().getReferenceName());
  }

  @Test(timeout = 4000)
  public void testSingletonGetter() {
    Scope scope = createGlobalScope(
        "var goog = {};\n" +
        "goog.addSingletonGetter = function(ctor) {};\n" +
        "/** @constructor */ function MySingleton() {}\n" +
        "goog.addSingletonGetter(MySingleton);\n",
        new ClosureCodingConvention());

    Scope.Var singletonVar = scope.getVar("MySingleton");
    assertNotNull(singletonVar);
    ObjectType ctorObj = (ObjectType) singletonVar.getType();
    assertTrue("getInstance property must be defined on constructor", ctorObj.hasProperty("getInstance"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastValid() {
    Scope scope = createGlobalScope(
        "var goog = {};\n" +
        "goog.reflect = {};\n" +
        "goog.reflect.object = function(type, obj) {};\n" +
        "/** @constructor */ function ExpectedType() {}\n" +
        "goog.reflect.object(ExpectedType, { foo: 'bar' });\n",
        new ClosureCodingConvention());

    assertEquals("No warnings expected for valid objectLiteralCast", 0, compiler.getWarningCount());
    assertEquals("No errors expected for valid objectLiteralCast", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testMethodOverrideFromSuperclass() {
    Scope scope = createGlobalScope(
        "var goog = {};\n" +
        "goog.inherits = function(child, parent) {};\n" +
        "/** @constructor */ function Animal() {}\n" +
        "/** @param {string} msg */ Animal.prototype.speak = function(msg) {};\n" +
        "/** @constructor */ function Dog() {}\n" +
        "goog.inherits(Dog, Animal);\n" +
        "Dog.prototype.speak = function(msg) {};\n",
        new ClosureCodingConvention());

    Scope.Var dogSpeak = scope.getVar("Dog.prototype.speak");
    assertNotNull(dogSpeak);
    assertTrue(dogSpeak.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) dogSpeak.getType();
    assertEquals("Inferred parameter count should match overridden function", 1, fnType.getParametersCount());
  }

  @Test(timeout = 4000)
  public void testMethodOverrideFromInterface() {
    Scope scope = createGlobalScope(
        "/** @interface */ function Playable() {}\n" +
        "/** @param {number} vol */ Playable.prototype.play = function(vol) {};\n" +
        "/** @constructor @implements {Playable} */ function AudioTrack() {}\n" +
        "AudioTrack.prototype.play = function(vol) {};\n");

    Scope.Var audioPlay = scope.getVar("AudioTrack.prototype.play");
    assertNotNull(audioPlay);
    assertTrue(audioPlay.getType() instanceof FunctionType);
  }

  // =========================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleVarDeclarationWarning() {
    createGlobalScope("/** @type {number} */ var x = 1, y = 2;");
    assertTrue("MULTIPLE_VAR_DEF diagnostic expected", compiler.getWarningCount() > 0);
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateEnumKeysWarning() {
    createGlobalScope("/** @enum {number} */ var DuplicateEnum = { KEY1: 1, KEY1: 2 };");
    assertTrue("ENUM_DUP diagnostic expected", compiler.getWarningCount() > 0);
    assertEquals(TypeCheck.ENUM_DUP.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testInvalidEnumKeyWarning() {
    createGlobalScope(
        "/** @enum {number} */ var InvalidKeys = { 'non_constant_name': 1 };",
        new ClosureCodingConvention());
    assertTrue("ENUM_NOT_CONSTANT diagnostic expected", compiler.getWarningCount() > 0);
    assertEquals(TypeCheck.ENUM_NOT_CONSTANT.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testInvalidEnumInitializerWarning() {
    createGlobalScope("/** @enum {number} */ var BrokenEnum = 12345;");
    assertTrue("ENUM_INITIALIZER diagnostic expected", compiler.getWarningCount() > 0);
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testMalformedTypedefWarning() {
    createGlobalScope("/** @typedef */ var BadTypedef;");
    assertTrue("MALFORMED_TYPEDEF diagnostic expected", compiler.getWarningCount() > 0);
    assertEquals(TypedScopeCreator.MALFORMED_TYPEDEF.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastInvalidConstructor() {
    createGlobalScope(
        "var goog = {};\n" +
        "goog.reflect = {};\n" +
        "goog.reflect.object = function(type, obj) {};\n" +
        "goog.reflect.object('NotAConstructor', { a: 1 });\n",
        new ClosureCodingConvention());
    assertTrue("CONSTRUCTOR_EXPECTED diagnostic expected", compiler.getWarningCount() > 0);
    assertEquals(TypedScopeCreator.CONSTRUCTOR_EXPECTED.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testStubDeclarationResolvedToUnknown() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "ns.untypedStub;\n");

    Scope.Var stubVar = scope.getVar("ns.untypedStub");
    assertNotNull("Stub property should be registered in scope", stubVar);
    assertTrue("Stub property type must be unknown", stubVar.getType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testPrototypeAssignmentAndRedefinition() {
    Scope scope = createGlobalScope(
        "function Simple() {}\n" +
        "Simple.prototype = { action: function() {} };\n");

    Scope.Var protoVar = scope.getVar("Simple.prototype");
    assertNotNull(protoVar);

    // Redeclaration on an explicitly declared prototype
    Scope subScope = createGlobalScope(
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor @extends {Super} */ function Sub() {}\n" +
        "Sub.prototype = {};\n");
    assertNotNull(subScope.getVar("Sub.prototype"));
  }

  @Test(timeout = 4000)
  public void testCatchClauseInLocalScope() {
    Scope globalScope = createGlobalScope(
        "function runner() {\n" +
        "  try {\n" +
        "    var a = 1;\n" +
        "  } catch (err) {\n" +
        "    var b = err;\n" +
        "  }\n" +
        "}\n");

    Scope localScope = createLocalScope(globalScope, "runner");
    Scope.Var catchVar = localScope.getVar("err");
    assertNotNull("Catch parameter must be registered in local scope", catchVar);
  }

  // =========================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testReturnTypeInferenceOnFunctionDeclaration() {
    Scope scope = createGlobalScope("function f() {}");
    Scope.Var varF = scope.getVar("f");
    assertNotNull("Function 'f' must be declared in scope", varF);
    assertNotNull("Function 'f' must have a type", varF.getType());
    assertEquals("Return type of empty function declaration must be inferred as undefined",
        "function (): undefined", varF.getType().toString());
  }

  @Test(timeout = 4000)
  public void testConstructorReturnTypeInference() {
    Scope scope = createGlobalScope("/** @constructor */ function Foo() {}");
    Scope.Var varFoo = scope.getVar("Foo");
    assertNotNull("Constructor 'Foo' must be declared in scope", varFoo);
    assertNotNull("Constructor 'Foo' must have a type", varFoo.getType());
    assertEquals("Constructor function return type must be undefined",
        "function (this:Foo): undefined", varFoo.getType().toString());
  }

  @Test(timeout = 4000)
  public void testConstructorPropertyReturnType() {
    Scope scope = createGlobalScope("var foo = {}; /** @constructor */ foo.Bar = function() {};");
    Scope.Var varBar = scope.getVar("foo.Bar");
    assertNotNull("Constructor property 'foo.Bar' must be declared in scope", varBar);
    assertNotNull("Constructor property 'foo.Bar' must have a type", varBar.getType());
    assertEquals("Constructor property return type must be undefined",
        "function (this:foo.Bar): undefined", varBar.getType().toString());
  }

  @Test(timeout = 4000)
  public void testPropertiesOnInterfaceReturnType() {
    Scope scope = createGlobalScope(
        "/** @interface */ function I() {}\n" +
        "/** @type {function()} */ I.prototype.foo;");
    Scope.Var varFoo = scope.getVar("I.prototype.foo");
    assertNotNull("Interface method 'I.prototype.foo' must be declared in scope", varFoo);
    assertNotNull("Interface method 'I.prototype.foo' must have a type", varFoo.getType());
    assertEquals("Interface method without return annotation must have undefined return type",
        "function (this:I): undefined", varFoo.getType().toString());
  }

  @Test(timeout = 4000)
  public void testMethodBeforeFunction() {
    Scope scope = createGlobalScope(
        "var Window = function() {};\n" +
        "Window.prototype.alert = function(msg) {};\n");
    Scope.Var varAlert = scope.getVar("Window.prototype.alert");
    assertNotNull(varAlert);
    assertNotNull(varAlert.getType());
    assertEquals("function (this:Window, ?): undefined", varAlert.getType().toString());
  }

  // =========================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testDuplicateVarDeclarationInSameScope() {
    createGlobalScope("var dup = 1; var dup = 2;");
    assertTrue("Redeclaration of variable should generate a type warning", compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testFunctionAliasResolution() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Original() {}\n" +
        "var Alias = Original;\n");

    Scope.Var aliasVar = scope.getVar("Alias");
    assertNotNull(aliasVar);
    assertTrue("Alias must have a function type", aliasVar.getType() instanceof FunctionType);
    FunctionType aliasFn = (FunctionType) aliasVar.getType();
    assertTrue("Alias must retain constructor status", aliasFn.isConstructor());
  }

  @Test(timeout = 4000)
  public void testTypedefEvaluation() {
    Scope scope = createGlobalScope(
        "/** @typedef {number|string} */ var StringOrNum;\n" +
        "/** @type {StringOrNum} */ var val = 1;\n");

    Scope.Var valVar = scope.getVar("val");
    assertNotNull(valVar);
    assertFalse(valVar.isTypeInferred());
    assertEquals("(number|string)", valVar.getType().toString());
  }

  // =========================================================================
  // PARTITION E: Object Lifecycle & Delegate Relationship Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDelegateRelationshipLifecycle() {
    ClosureCodingConvention customConvention = new ClosureCodingConvention() {
      @Override
      public String getDelegateSuperclassName() {
        return "DelegateSuper";
      }

      @Override
      public DelegateRelationship getDelegateRelationship(Node callNode) {
        if (callNode.isCall()) {
          Node callee = callNode.getFirstChild();
          if (callee != null && "setupDelegate".equals(callee.getString())) {
            return new DelegateRelationship("Delegator", "DelegateBase");
          }
        }
        return null;
      }
    };

    Scope scope = createGlobalScope(
        "function setupDelegate() {}\n" +
        "/** @constructor */ function DelegateSuper() {}\n" +
        "/** @constructor */ function DelegateBase() {}\n" +
        "/** @constructor */ function Delegator() {}\n" +
        "setupDelegate();\n",
        customConvention);

    assertNotNull(scope.getVar("Delegator"));
    assertNotNull(scope.getVar("DelegateBase"));
    assertNotNull(scope.getVar("DelegateSuper"));
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testEmptySourceAndCommentsOnly() {
    Scope scope1 = createGlobalScope("");
    assertNotNull(scope1);
    assertNull(scope1.getParent());

    Scope scope2 = createGlobalScope("// Just a line comment\n/* Multi-line comment */\n");
    assertNotNull(scope2);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }
}