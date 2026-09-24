package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: TypedScopeCreator.java (Defects4J Closure Benchmark)
 *
 * Key Branches & Decision Points Targeted:
 * 1. Global vs. Local Scope Generation:
 *    - parent == null: FirstOrderFunctionAnalyzer, createInitialScope, GlobalScopeBuilder.
 *    - parent != null: LocalScopeBuilder, parameter declaration, bleeding functions, escaped var analysis.
 * 2. Scope Patching:
 *    - patchGlobalScope() on modified SCRIPT node, clearing old variables, re-traversing.
 * 3. Functional Inference & Context Determination:
 *    - defineFunctionLiteral(), hoisted function handling, anonymous vs named function expressions.
 *    - Overridden function detection (findOverriddenFunction) on superclasses and interfaces.
 *    - Function prototype binding and context inference (thisType).
 * 4. Object Literals & Lends Annotations:
 *    - defineObjectLiteral() with @lends: valid object lend, missing variable (UNKNOWN_LENDS),
 *      non-object target (LENDS_ON_NON_OBJECT).
 *    - Object literal property definitions and key type resolution.
 * 5. Enum & Typedef Declarations:
 *    - createEnumTypeFromNodes(), aliased enums, invalid enum key errors (ENUM_NOT_CONSTANT),
 *      non-object/non-qname enum initializer (ENUM_INITIALIZER).
 *    - Typedef creation, Malformed typedef (MALFORMED_TYPEDEF), recursive typedef guard.
 * 6. Constructors, Interfaces & Declarations:
 *    - Uninitialized constructors (CTOR_INITIALIZER) and interfaces (IFACE_INITIALIZER).
 *    - Prototype assignments: F.prototype = { ... } (implicit prototype resetting vs undeclaring).
 *    - Multiple variable declarations in single VAR statement (MULTIPLE_VAR_DEF).
 * 7. Coding Conventions & Class-Defining Calls:
 *    - Subclass relationship (goog.inherits / inherits), singleton getters, object literal casts.
 *    - Missing constructor in cast (CONSTRUCTOR_EXPECTED).
 *    - Special global object Window prototype reassignment.
 * 8. Ground Truth Defect Target (Issue 586 / TypeCheckTest::testIssue586):
 *    - Object literal returned inside a method returning a class type must not clobber/overwrite
 *      existing declared class prototype methods and their parameter signatures.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.jscomp.Scope.Var;

import java.util.Iterator;

public class TypedScopeCreatorGeminiTest {

  // Standard extern definitions for native objects
  private static final String DEFAULT_EXTERNS =
      "/** @constructor */ function Object() {}\n" +
      "/** @constructor */ function Function() {}\n" +
      "/** @constructor */ function String() {}\n" +
      "/** @constructor */ function Boolean() {}\n" +
      "/** @constructor */ function Number() {}\n" +
      "/** @constructor */ function Array() {}\n" +
      "/** @constructor */ function RegExp() {}\n" +
      "/** @constructor */ function Date() {}\n" +
      "/** @constructor */ function Error() {}\n" +
      "var undefined;\n";

  /**
   * Helper to create a global Scope using TypedScopeCreator.
   */
  private Scope createGlobalScope(Compiler compiler, String js) {
    return createGlobalScope(compiler, DEFAULT_EXTERNS, js);
  }

  private Scope createGlobalScope(Compiler compiler, String externs, String js) {
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initOptions(options);

    Node externsNode = compiler.parseSyntheticCode("externs.js", externs);
    Node mainNode = compiler.parseSyntheticCode("testcode.js", js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  /**
   * Helper to compile and run full type checking.
   */
  private Compiler compileWithCheckTypes(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initOptions(options);

    SourceFile externFile = SourceFile.fromCode("externs.js", DEFAULT_EXTERNS);
    SourceFile mainFile = SourceFile.fromCode("testcode.js", js);
    compiler.compile(externFile, mainFile, options);
    return compiler;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleVariableDeclarationAndInference() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; /** @type {string} */ var b = 'hello';";
    Scope scope = createGlobalScope(compiler, js);

    Var varA = scope.getVar("a");
    assertNotNull("Var 'a' should be declared", varA);
    assertTrue("Type of 'a' should be inferred", varA.isTypeInferred());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), varA.getType());

    Var varB = scope.getVar("b");
    assertNotNull("Var 'b' should be declared", varB);
    assertFalse("Type of 'b' should be declared explicitly", varB.isTypeInferred());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), varB.getType());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationAndLocalScopeCreation() {
    Compiler compiler = new Compiler();
    String js = "function outer(x) { var y = x; return y; }";
    Scope globalScope = createGlobalScope(compiler, js);

    Var outerVar = globalScope.getVar("outer");
    assertNotNull(outerVar);
    assertTrue(outerVar.getType().isFunctionType());

    Node fnNode = outerVar.getNameNode().getParent();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope);
    assertFalse(localScope.isGlobal());
    assertEquals(globalScope, localScope.getParent());

    Var paramX = localScope.getVar("x");
    assertNotNull("Parameter 'x' should exist in local scope", paramX);

    Var varY = localScope.getVar("y");
    assertNotNull("Local variable 'y' should exist in local scope", varY);
  }

  @Test(timeout = 4000)
  public void testHoistedFunctionPrecedence() {
    Compiler compiler = new Compiler();
    String js = "var result = hoisted(); function hoisted() { return 42; }";
    Scope scope = createGlobalScope(compiler, js);

    Var hoistedVar = scope.getVar("hoisted");
    assertNotNull(hoistedVar);
    assertTrue(hoistedVar.getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testCatchParameterDefinition() {
    Compiler compiler = new Compiler();
    String js = "function testCatch() { try { } catch (err) { var caught = err; } }";
    Scope globalScope = createGlobalScope(compiler, js);

    Var testCatchVar = globalScope.getVar("testCatch");
    Node fnNode = testCatchVar.getNameNode().getParent();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope localScope = creator.createScope(fnNode, globalScope);

    Var errVar = localScope.getVar("err");
    assertNotNull("Catch parameter 'err' must be declared in scope", errVar);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralPropertyInference() {
    Compiler compiler = new Compiler();
    String js = "var myObj = { num: 100, str: 'value', flag: true };";
    Scope scope = createGlobalScope(compiler, js);

    Var objVar = scope.getVar("myObj");
    assertNotNull(objVar);
    ObjectType objType = ObjectType.cast(objVar.getType());
    assertNotNull(objType);
    assertTrue(objType.hasProperty("num"));
    assertTrue(objType.hasProperty("str"));
    assertTrue(objType.hasProperty("flag"));
  }

  @Test(timeout = 4000)
  public void testStubPropertyDeclarationOnExprResult() {
    Compiler compiler = new Compiler();
    String js = "var ns = {}; ns.prop;";
    Scope scope = createGlobalScope(compiler, js);

    Var nsVar = scope.getVar("ns");
    assertNotNull(nsVar);
    ObjectType nsType = ObjectType.cast(nsVar.getType());
    assertNotNull(nsType);
    assertTrue("Stub property 'prop' should be registered on owner type", nsType.hasProperty("prop"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptHandling() {
    Compiler compiler = new Compiler();
    Scope scope = createGlobalScope(compiler, "");
    assertNotNull(scope);
    assertTrue(scope.isGlobal());
  }

  @Test(timeout = 4000)
  public void testMultipleVarDeclarationWithJSDocReportsWarning() {
    Compiler compiler = new Compiler();
    String js = "/** @type {number} */ var a = 1, b = 2;";
    createGlobalScope(compiler, js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(TypeCheck.MULTIPLE_VAR_DEF)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should report MULTIPLE_VAR_DEF diagnostic", foundWarning);
  }

  @Test(timeout = 4000)
  public void testWindowConstructorUpdatesGlobalThisPrototype() {
    Compiler compiler = new Compiler();
    String js = "/** @constructor */ function Window() {}";
    Scope scope = createGlobalScope(compiler, js);

    Var windowVar = scope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());

    ObjectType globalThis = compiler.getTypeRegistry().getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    ObjectType windowInstance = windowVar.getType().toMaybeFunctionType().getInstanceType();
    assertTrue(globalThis.getImplicitPrototype().isEquivalentTo(windowInstance));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionNameInLocalScope() {
    Compiler compiler = new Compiler();
    String js = "var outer = function inner() { return inner; };";
    Scope globalScope = createGlobalScope(compiler, js);

    Var outerVar = globalScope.getVar("outer");
    assertNotNull(outerVar);
    assertNull("Bleeding function name 'inner' must NOT leak into global scope", globalScope.getVar("inner"));

    Node fnNode = outerVar.getNameNode().getFirstChild();
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull("Bleeding function name 'inner' must be defined inside its local scope", localScope.getVar("inner"));
  }

  @Test(timeout = 4000)
  public void testEscapedVariableMarking() {
    Compiler compiler = new Compiler();
    String js = "function parentFn() {\n" +
                "  var escapedVar = 1;\n" +
                "  function innerFn() {\n" +
                "    escapedVar = 2;\n" +
                "  }\n" +
                "  return innerFn;\n" +
                "}";
    Scope globalScope = createGlobalScope(compiler, js);
    Var parentVar = globalScope.getVar("parentFn");
    Node parentFnNode = parentVar.getNameNode().getParent();

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope parentLocalScope = creator.createScope(parentFnNode, globalScope);

    Var escaped = parentLocalScope.getVar("escapedVar");
    assertNotNull(escaped);
    assertTrue("Variable modified in nested closure must be marked escaped", escaped.isEscaped());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 586 / TypeCheckTest::testIssue586)
  // =========================================================================

  /**
   * CRITICAL DEFECT TEST: Issue 586 (TypeCheckTest::testIssue586).
   * An object literal cast or returned inside a method must NOT override or clobber
   * the declared method signature of MyClass.prototype.fn with the empty parameter list
   * of the object literal's property.
   */
  @Test(timeout = 4000)
  public void testIssue586_prototypeMethodNotOverwrittenByObjectLiteral() {
    Compiler compiler = new Compiler();
    String js =
        "/** @constructor */\n" +
        "var MyClass = function() {};\n" +
        "/** @param {boolean} success */\n" +
        "MyClass.prototype.fn = function(success) {};\n" +
        "MyClass.prototype.filter = function() {\n" +
        "  return /** @type {MyClass} */ ({\n" +
        "    fn: function() {}\n" +
        "  });\n" +
        "};\n";

    Scope scope = createGlobalScope(compiler, js);

    Var fnVar = scope.getVar("MyClass.prototype.fn");
    assertNotNull("MyClass.prototype.fn must exist in global scope slots", fnVar);

    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertNotNull("MyClass.prototype.fn must be a FunctionType", fnType);

    // The method fn must retain its original 1 parameter ('success'), not be overwritten with 0
    assertEquals("MyClass.prototype.fn must not have its parameter count clobbered to 0 by object literal",
        1, fnType.getParametersCount());
  }

  @Test(timeout = 4000)
  public void testIssue586_typeCheckWarningEmittedOnInvalidCall() {
    String js =
        "/** @constructor */\n" +
        "var MyClass = function() {};\n" +
        "/** @param {boolean} success */\n" +
        "MyClass.prototype.fn = function(success) {};\n" +
        "MyClass.prototype.filter = function() {\n" +
        "  return /** @type {MyClass} */ ({\n" +
        "    fn: function() {}\n" +
        "  });\n" +
        "};\n" +
        "var instance = new MyClass();\n" +
        "instance.fn();\n"; // Missing required boolean argument

    Compiler compiler = compileWithCheckTypes(js);
    assertTrue("Should emit at least one warning for calling fn() with 0 arguments",
        compiler.getWarningCount() > 0);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testMalformedTypedefReportsWarning() {
    Compiler compiler = new Compiler();
    String js = "/** @typedef {NonExistentType} */ var MyType;";
    createGlobalScope(compiler, js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(TypedScopeCreator.MALFORMED_TYPEDEF)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should report MALFORMED_TYPEDEF", foundWarning);
  }

  @Test(timeout = 4000)
  public void testEnumInitializerNotObjectLitReportsWarning() {
    Compiler compiler = new Compiler();
    String js = "/** @enum {number} */ var MyEnum = 123;";
    createGlobalScope(compiler, js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(TypedScopeCreator.ENUM_INITIALIZER)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should report ENUM_INITIALIZER when enum value is not an object literal", foundWarning);
  }

  @Test(timeout = 4000)
  public void testUninitializedConstructorReportsWarning() {
    Compiler compiler = new Compiler();
    String js = "/** @constructor */ var UninitializedCtor;";
    createGlobalScope(compiler, js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(TypedScopeCreator.CTOR_INITIALIZER)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should report CTOR_INITIALIZER when constructor is not initialized", foundWarning);
  }

  @Test(timeout = 4000)
  public void testUninitializedInterfaceReportsWarning() {
    Compiler compiler = new Compiler();
    String js = "/** @interface */ var UninitializedIface;";
    createGlobalScope(compiler, js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(TypedScopeCreator.IFACE_INITIALIZER)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should report IFACE_INITIALIZER when interface is not initialized", foundWarning);
  }

  @Test(timeout = 4000)
  public void testUnknownLendsReportsWarning() {
    Compiler compiler = new Compiler();
    String js = "var obj = /** @lends {nonExistentVar} */ ({});";
    createGlobalScope(compiler, js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(TypedScopeCreator.UNKNOWN_LENDS)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should report UNKNOWN_LENDS for undeclared variable target", foundWarning);
  }

  @Test(timeout = 4000)
  public void testLendsOnNonObjectReportsWarning() {
    Compiler compiler = new Compiler();
    String js = "var myNum = 42; var obj = /** @lends {myNum} */ ({});";
    createGlobalScope(compiler, js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(TypedScopeCreator.LENDS_ON_NON_OBJECT)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should report LENDS_ON_NON_OBJECT when lending to a number", foundWarning);
  }

  @Test(timeout = 4000)
  public void testPatchGlobalScopeNonScriptThrowsException() {
    Compiler compiler = new Compiler();
    Scope globalScope = createGlobalScope(compiler, "var a = 1;");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);

    Node nonScriptNode = new Node(Token.BLOCK);
    try {
      creator.patchGlobalScope(globalScope, nonScriptNode);
      fail("Expected IllegalStateException when patching non-SCRIPT root");
    } catch (IllegalStateException expected) {
      // expected guard
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Inheritance & Advanced Scopes
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateInitialScopeNativeBindings() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);

    Node root = compiler.parseSyntheticCode("test.js", "");
    Scope initialScope = creator.createInitialScope(root);

    assertNotNull("Object must be bound", initialScope.getVar("Object"));
    assertNotNull("Function must be bound", initialScope.getVar("Function"));
    assertNotNull("Array must be bound", initialScope.getVar("Array"));
    assertNotNull("Date must be bound", initialScope.getVar("Date"));
    assertNotNull("RegExp must be bound", initialScope.getVar("RegExp"));
    assertNotNull("Error must be bound", initialScope.getVar("Error"));
    assertNotNull("undefined must be bound", initialScope.getVar("undefined"));
    assertNotNull("ActiveXObject must be bound", initialScope.getVar("ActiveXObject"));
  }

  @Test(timeout = 4000)
  public void testPatchGlobalScopeVariableReplacement() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node script1 = compiler.parseSyntheticCode("script1.js", "var x = 10;");
    Node root = new Node(Token.BLOCK, script1);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    assertNotNull(globalScope.getVar("x"));

    Node updatedScript1 = compiler.parseSyntheticCode("script1.js", "var y = 20;");
    creator.patchGlobalScope(globalScope, updatedScript1);

    assertNull("Old variable 'x' should be undeclared after patch", globalScope.getVar("x"));
    assertNotNull("New variable 'y' should be declared after patch", globalScope.getVar("y"));
  }

  @Test(timeout = 4000)
  public void testPrototypeRedefinitionWithObjectLiteral() {
    Compiler compiler = new Compiler();
    String js =
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = { bar: function() { return 1; } };";

    Scope scope = createGlobalScope(compiler, js);
    Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);

    FunctionType fooCtor = fooVar.getType().toMaybeFunctionType();
    assertNotNull(fooCtor);
    assertTrue("Foo.prototype must have property 'bar'",
        fooCtor.getPrototype().hasProperty("bar"));
  }

  @Test(timeout = 4000)
  public void testSubclassInheritanceRelationship() {
    Compiler compiler = new Compiler();
    String js =
        "var goog = {};\n" +
        "goog.inherits = function(child, parent) {};\n" +
        "/** @constructor */ function SuperClass() {}\n" +
        "/** @constructor \n * @extends {SuperClass} */ function SubClass() {}\n" +
        "goog.inherits(SubClass, SuperClass);";

    Scope scope = createGlobalScope(compiler, js);
    Var subClassVar = scope.getVar("SubClass");
    assertNotNull(subClassVar);

    FunctionType subCtor = subClassVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    FunctionType superCtor = scope.getVar("SuperClass").getType().toMaybeFunctionType();
    assertEquals(superCtor.getInstanceType(), subCtor.getSuperClassConstructor().getInstanceType());
  }

  @Test(timeout = 4000)
  public void testEnumDeclarationWithValidKeys() {
    Compiler compiler = new Compiler();
    String js = "/** @enum {string} */ var Direction = { NORTH: 'N', SOUTH: 'S' };";
    Scope scope = createGlobalScope(compiler, js);

    Var directionVar = scope.getVar("Direction");
    assertNotNull(directionVar);
    JSType type = directionVar.getType();
    assertTrue("Type must be an EnumType", type instanceof EnumType);

    EnumType enumType = (EnumType) type;
    assertTrue(enumType.getElements().contains("NORTH"));
    assertTrue(enumType.getElements().contains("SOUTH"));
  }
}