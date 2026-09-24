package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: TypedScopeCreator (Defects4J Closure Compiler)
 *
 * DEFECT SPECIFICATION:
 * - TypeCheckTest::testLends10 / testLends11:
 *   When a property is defined via an ObjectLiteral annotated with @lends (or when assigning directly
 *   to a prototype with an object literal), overriding a previously declared prototype property with
 *   an incompatible return type must produce an "inconsistent return type" warning. In the defective
 *   version, prototype reassignment and lends property analysis failed to properly preserve or check
 *   the overridden slot types, allowing inconsistent return types to silently pass through.
 *
 * COVERAGE ZONE MATRIX:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - Literal type attachment (null, void, string, number, boolean, regexp, object literal).
 *    - Function declaration and hoisting in statement parent blocks.
 *    - Constructor and interface declaration, implicit prototype declaration in scope chain.
 *    - Object literal property declaration and slot inference.
 *    - Constructor aliasing in global scope.
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Empty input scripts.
 *    - Multiple VAR declarations with JSDoc (MULTIPLE_VAR_DEF warning).
 *    - Invalid enum initializer (non-object-literal, non-QName -> ENUM_INITIALIZER).
 *    - Invalid enum keys (non-constant identifier -> ENUM_NOT_CONSTANT).
 *    - Missing initializers for constructors and interfaces (CTOR_INITIALIZER, IFACE_INITIALIZER).
 *    - Malformed typedefs (MALFORMED_TYPEDEF) and valid typedefs.
 *    - Unknown @lends target (UNKNOWN_LENDS) and @lends on non-object (LENDS_ON_NON_OBJECT).
 *
 * 3. Partition C: Defect-Targeted Branch Zone
 *    - testLendsInconsistentReturnTypeDefect: Reproduction of testLends10.
 *    - testPrototypeReassignInconsistentReturnTypeDefect: Reproduction of testLends11.
 *    - Prototype reassignment with object literal resetting implicit prototype.
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - Catch parameter scoping in catch blocks.
 *    - Bleeding function names in function expressions.
 *    - Escaped variables tracking across nested local scopes.
 *    - Subclass relationship validation (expectSuperType via inherits).
 *    - "Window" global constructor special casing and GlobalThis prototype rebasing.
 *
 * 5. Partition E: Object Lifecycle & Scope Maintenance
 *    - patchGlobalScope validation: variable removal and re-traversal.
 *    - patchGlobalScope defensive precondition checks (non-script node, non-global scope).
 *    - createInitialScope: native JS bindings (Object, Array, Date, ActiveXObject, undefined).
 * ----------------------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.JSError;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;

public class TypedScopeCreatorGeminiTest {

  private Compiler compiler;
  private TypedScopeCreator scopeCreator;

  private Scope buildGlobalScope(String js) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node script = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externs, script);
    scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  private boolean hasWarning(DiagnosticType type) {
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
  public void testLiteralTypesAttachment() {
    String js =
        "var n = null;\n" +
        "var v = void 0;\n" +
        "var s = 'closure';\n" +
        "var num = 42;\n" +
        "var b1 = true;\n" +
        "var b2 = false;\n" +
        "var r = /abc/;\n" +
        "var o = {};";

    Scope scope = buildGlobalScope(js);

    assertTrue(scope.isDeclared("n", false));
    assertTrue(scope.isDeclared("v", false));
    assertTrue(scope.isDeclared("s", false));
    assertTrue(scope.isDeclared("num", false));
    assertTrue(scope.isDeclared("b1", false));
    assertTrue(scope.isDeclared("b2", false));
    assertTrue(scope.isDeclared("r", false));
    assertTrue(scope.isDeclared("o", false));

    Scope.Var oVar = scope.getVar("o");
    assertNotNull(oVar);
    assertNotNull(oVar.getType());
    assertTrue(oVar.getType().isObjectType());
  }

  @Test(timeout = 4000)
  public void testConstructorAndPrototypeDeclaration() {
    String js =
        "/** @constructor */\n" +
        "function Person(name) {\n" +
        "  /** @type {string} */ this.name = name;\n" +
        "}\n" +
        "Person.prototype.greet = function() {};";

    Scope scope = buildGlobalScope(js);

    assertTrue(scope.isDeclared("Person", false));
    assertTrue(scope.isDeclared("Person.prototype", false));
    assertTrue(scope.isDeclared("Person.prototype.greet", false));

    Scope.Var personVar = scope.getVar("Person");
    assertTrue(personVar.getType().isConstructor());

    FunctionType personCtor = personVar.getType().toMaybeFunctionType();
    assertNotNull(personCtor);
    ObjectType instanceType = personCtor.getInstanceType();
    assertTrue(instanceType.hasProperty("name"));
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        instanceType.getPropertyType("name"));
  }

  @Test(timeout = 4000)
  public void testHoistedFunctionDeclaration() {
    String js =
        "function test() {\n" +
        "  return inner();\n" +
        "  function inner() { return 1; }\n" +
        "}";

    Scope globalScope = buildGlobalScope(js);
    Scope.Var testVar = globalScope.getVar("test");
    assertNotNull(testVar);

    Node testFnNode = testVar.getInitialValue();
    Scope localScope = scopeCreator.createScope(testFnNode, globalScope);

    assertTrue(localScope.isDeclared("inner", false));
    Scope.Var innerVar = localScope.getVar("inner");
    assertNotNull(innerVar);
    assertTrue(innerVar.getType().isFunctionType());
  }

  @Test(timeout = 4000)
  public void testConstructorAliasInGlobalScope() {
    String js =
        "/** @constructor */ function Original() {}\n" +
        "var Alias = Original;";

    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("Alias", false));
    Scope.Var aliasVar = scope.getVar("Alias");
    assertNotNull(aliasVar.getType());
    assertTrue(aliasVar.getType().isConstructor());
    assertNotNull(compiler.getTypeRegistry().getType("Alias"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptExecution() {
    Scope scope = buildGlobalScope("");
    assertNotNull(scope);
    assertTrue(scope.isGlobal());
  }

  @Test(timeout = 4000)
  public void testMultipleVarDefWarning() {
    String js = "/** @type {number} */ var a = 1, b = 2;";
    buildGlobalScope(js);
    assertTrue("Should report MULTIPLE_VAR_DEF warning",
        hasWarning(TypeCheck.MULTIPLE_VAR_DEF));
  }

  @Test(timeout = 4000)
  public void testInvalidEnumInitializer() {
    String js = "/** @enum {number} */ var MyEnum = 123;";
    buildGlobalScope(js);
    assertTrue("Should report ENUM_INITIALIZER warning",
        hasWarning(TypedScopeCreator.ENUM_INITIALIZER));
  }

  @Test(timeout = 4000)
  public void testInvalidEnumKey() {
    String js = "/** @enum {number} */ var MyEnum = { 'invalid-key': 1 };";
    buildGlobalScope(js);
    assertTrue("Should report ENUM_NOT_CONSTANT warning",
        hasWarning(TypeCheck.ENUM_NOT_CONSTANT));
  }

  @Test(timeout = 4000)
  public void testConstructorMissingInitializer() {
    String js = "/** @constructor */ var EmptyCtor;";
    buildGlobalScope(js);
    assertTrue("Should report CTOR_INITIALIZER warning",
        hasWarning(TypedScopeCreator.CTOR_INITIALIZER));
  }

  @Test(timeout = 4000)
  public void testInterfaceMissingInitializer() {
    String js = "/** @interface */ var EmptyIface;";
    buildGlobalScope(js);
    assertTrue("Should report IFACE_INITIALIZER warning",
        hasWarning(TypedScopeCreator.IFACE_INITIALIZER));
  }

  @Test(timeout = 4000)
  public void testUnknownLendsWarning() {
    String js = "var obj = /** @lends {NonExistentTarget} */ ({ prop: 1 });";
    buildGlobalScope(js);
    assertTrue("Should report UNKNOWN_LENDS warning",
        hasWarning(TypedScopeCreator.UNKNOWN_LENDS));
  }

  @Test(timeout = 4000)
  public void testLendsOnNonObjectWarning() {
    String js =
        "var myNumber = 42;\n" +
        "var obj = /** @lends {myNumber} */ ({ prop: 1 });";
    buildGlobalScope(js);
    assertTrue("Should report LENDS_ON_NON_OBJECT warning",
        hasWarning(TypedScopeCreator.LENDS_ON_NON_OBJECT));
  }

  @Test(timeout = 4000)
  public void testTypedefValidAndMalformed() {
    String validJs = "/** @typedef {string} */ var StringAlias;";
    Scope scope = buildGlobalScope(validJs);
    assertNotNull(compiler.getTypeRegistry().getType("StringAlias"));

    String malformedJs = "/** @typedef {NonExistentTypeUnknownFoo} */ var BadAlias;";
    buildGlobalScope(malformedJs);
    assertTrue("Should report MALFORMED_TYPEDEF warning",
        hasWarning(TypedScopeCreator.MALFORMED_TYPEDEF));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J testLends10 / testLends11)
  // =========================================================================

  /**
   * Targets Defects4J defect: TypeCheckTest::testLends10.
   * When an object literal with @lends redefines an existing prototype method
   * with an incompatible return type, an inconsistent return type warning must be emitted.
   */
  @Test(timeout = 4000)
  public void testLendsInconsistentReturnTypeDefect() {
    String js =
        "function extend(x, y) {}\n" +
        "/** @constructor */ function Foo() {}\n" +
        "/** @return {number} */ Foo.prototype.foo = function() { return 3; };\n" +
        "Foo.prototype = extend(Foo.prototype, /** @lends {Foo.prototype} */ ({\n" +
        "  /** @return {string} */ foo: function() { return 'a'; }\n" +
        "}));";

    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externs, script);
    TypedScopeCreator tsc = new TypedScopeCreator(compiler);
    Scope scope = tsc.createScope(root, null);

    TypeCheck check = new TypeCheck(
        compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), compiler.getTypeRegistry()),
        compiler.getTypeRegistry(),
        scope, null, CheckLevel.WARNING, CheckLevel.WARNING);
    check.process(externs, script);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.description != null &&
          error.description.contains("inconsistent return type")) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Defect verification: Expected 'inconsistent return type' warning for @lends override",
        foundWarning);
  }

  /**
   * Targets Defects4J defect: TypeCheckTest::testLends11.
   * Reassigning a prototype directly to an object literal with an incompatible
   * property return type must trigger an inconsistent return type warning.
   */
  @Test(timeout = 4000)
  public void testPrototypeReassignInconsistentReturnTypeDefect() {
    String js =
        "/** @constructor */ function Foo() {}\n" +
        "/** @return {number} */ Foo.prototype.foo = function() { return 3; };\n" +
        "Foo.prototype = {\n" +
        "  /** @return {string} */ foo: function() { return 'a'; }\n" +
        "};";

    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externs, script);
    TypedScopeCreator tsc = new TypedScopeCreator(compiler);
    Scope scope = tsc.createScope(root, null);

    TypeCheck check = new TypeCheck(
        compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), compiler.getTypeRegistry()),
        compiler.getTypeRegistry(),
        scope, null, CheckLevel.WARNING, CheckLevel.WARNING);
    check.process(externs, script);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.description != null &&
          error.description.contains("inconsistent return type")) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Defect verification: Expected 'inconsistent return type' warning for prototype literal reassignment",
        foundWarning);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testCatchParameterScope() {
    String js =
        "function f() {\n" +
        "  try {\n" +
        "  } catch (err) {\n" +
        "    var inCatch = err;\n" +
        "  }\n" +
        "}";

    Scope globalScope = buildGlobalScope(js);
    Scope.Var fVar = globalScope.getVar("f");
    Node fNode = fVar.getInitialValue();
    Scope localScope = scopeCreator.createScope(fNode, globalScope);

    assertTrue(localScope.isDeclared("inCatch", false));
    assertTrue(localScope.isDeclared("err", false));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpressionName() {
    String js = "var outerFn = function bleedingName() { return bleedingName; };";
    Scope globalScope = buildGlobalScope(js);

    assertTrue(globalScope.isDeclared("outerFn", false));
    assertFalse(globalScope.isDeclared("bleedingName", false));

    Scope.Var outerVar = globalScope.getVar("outerFn");
    Node fnNode = outerVar.getInitialValue();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("bleedingName", false));
  }

  @Test(timeout = 4000)
  public void testEscapedVariableTracking() {
    String js =
        "function factory() {\n" +
        "  var counter = 0;\n" +
        "  return function() {\n" +
        "    counter++;\n" +
        "    return counter;\n" +
        "  };\n" +
        "}";

    Scope globalScope = buildGlobalScope(js);
    Scope.Var factoryVar = globalScope.getVar("factory");
    Node factoryNode = factoryVar.getInitialValue();
    Scope factoryScope = scopeCreator.createScope(factoryNode, globalScope);

    Scope.Var counterVar = factoryScope.getVar("counter");
    assertNotNull(counterVar);
    assertTrue("Captured closure variable counter must be marked escaped",
        counterVar.isEscaped());
  }

  @Test(timeout = 4000)
  public void testWindowConstructorGlobalThisConfiguration() {
    String js = "/** @constructor */ function Window() {}";
    Scope scope = buildGlobalScope(js);

    assertTrue(scope.isDeclared("Window", false));
    ObjectType globalThis = compiler.getTypeRegistry().getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertNotNull(globalThis);
    assertNotNull(globalThis.getConstructor());
  }

  @Test(timeout = 4000)
  public void testSubclassRelationshipAndSuperTypeInference() {
    String js =
        "function googInherits(child, parent) {}\n" +
        "/** @constructor */ function Parent() {}\n" +
        "/** @return {number} */ Parent.prototype.getVal = function() { return 1; };\n" +
        "/** @constructor\n" +
        " *  @extends {Parent} */\n" +
        "function Child() {}\n" +
        "googInherits(Child, Parent);\n" +
        "Child.prototype.getVal = function() { return 2; };";

    Scope scope = buildGlobalScope(js);
    Scope.Var childVar = scope.getVar("Child");
    assertNotNull(childVar);
    FunctionType childCtor = childVar.getType().toMaybeFunctionType();
    assertNotNull(childCtor);
    assertEquals("Parent", childCtor.getSuperClassConstructor().getReferenceName());
  }

  @Test(timeout = 4000)
  public void testStubDeclarationsResolution() {
    String js =
        "var ns = {};\n" +
        "ns.stubProp;\n";

    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("ns.stubProp", false));
    Scope.Var stubVar = scope.getVar("ns.stubProp");
    assertNotNull(stubVar);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE),
        stubVar.getType());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Maintenance
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateInitialScopeNativeTypes() {
    Compiler comp = new Compiler();
    comp.initOptions(new CompilerOptions());
    Node emptyRoot = new Node(Token.BLOCK);
    TypedScopeCreator creator = new TypedScopeCreator(comp);
    Scope initialScope = creator.createInitialScope(emptyRoot);

    assertNotNull(initialScope.getVar("Object"));
    assertNotNull(initialScope.getVar("Function"));
    assertNotNull(initialScope.getVar("Array"));
    assertNotNull(initialScope.getVar("String"));
    assertNotNull(initialScope.getVar("Boolean"));
    assertNotNull(initialScope.getVar("Number"));
    assertNotNull(initialScope.getVar("Date"));
    assertNotNull(initialScope.getVar("RegExp"));
    assertNotNull(initialScope.getVar("Error"));
    assertNotNull(initialScope.getVar("undefined"));
    assertNotNull(initialScope.getVar("ActiveXObject"));
  }

  @Test(timeout = 4000)
  public void testPatchGlobalScopeLifecycle() {
    String originalJs = "var oldVar = 100;";
    Scope globalScope = buildGlobalScope(originalJs);
    assertTrue(globalScope.isDeclared("oldVar", false));

    Node newScript = compiler.parseTestCode("var newVar = 200;");
    scopeCreator.patchGlobalScope(globalScope, newScript);

    assertFalse("oldVar should have been undeclared during patch",
        globalScope.isDeclared("oldVar", false));
    assertTrue("newVar should have been declared after patch",
        globalScope.isDeclared("newVar", false));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPatchGlobalScopeGuardsNonScript() {
    Scope globalScope = buildGlobalScope("var x = 1;");
    Node nonScript = new Node(Token.BLOCK);
    scopeCreator.patchGlobalScope(globalScope, nonScript);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPatchGlobalScopeGuardsNonGlobalScope() {
    String js = "function local() { var a = 1; }";
    Scope globalScope = buildGlobalScope(js);
    Scope.Var fnVar = globalScope.getVar("local");
    Scope localScope = scopeCreator.createScope(fnVar.getInitialValue(), globalScope);

    Node newScript = compiler.parseTestCode("var b = 2;");
    scopeCreator.patchGlobalScope(localScope, newScript);
  }
}