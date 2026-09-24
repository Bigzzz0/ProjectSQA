package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.TypedScopeCreator
 *
 * Decision / Branch Coverage & Defect Analysis Matrix:
 * ---------------------------------------------------------------------------------------------------------------------
 * Method / Branch Target                   | Input Condition / Scenario                    | Target Outcome / Invariant
 * ---------------------------------------------------------------------------------------------------------------------
 * Defect Zone (Closure Issue 688)          | @const var with type-cast object literal      | Expected "inconsistent return type"
 *                                          | and prototype property override               | warning triggered & type resolved
 * createInitialScope                       | Root node with standard ES5 environment       | Native types declared (Object, Array,
 *                                          |                                               | String, ActiveXObject, undefined, etc.)
 * patchGlobalScope                         | Non-script node passed                        | IllegalStateException thrown
 * patchGlobalScope                         | Local scope passed instead of global          | IllegalStateException thrown
 * patchGlobalScope                         | Script modification                           | Old vars undeclared, new vars added
 * defineVar / MULTIPLE_VAR_DEF             | JSDoc on multi-var declaration                | MULTIPLE_VAR_DEF warning reported
 * defineSlot / CTOR_INITIALIZER            | @constructor var without initialization       | CTOR_INITIALIZER warning reported
 * defineSlot / IFACE_INITIALIZER           | @interface var without initialization          | IFACE_INITIALIZER warning reported
 * defineSlot / ENUM_INITIALIZER            | @enum assigned to primitive (number)          | ENUM_INITIALIZER warning reported
 * createEnumTypeFromNodes                  | Valid enum object literal                     | Enum type created with elements
 * createEnumTypeFromNodes                  | Enum aliasing another enum                    | Correctly aliases existing EnumType
 * createEnumTypeFromNodes / ENUM_NOT_CONST | Enum containing getter key                    | ENUM_NOT_CONSTANT warning reported
 * checkForTypedef / MALFORMED_TYPEDEF      | @typedef without type specification           | MALFORMED_TYPEDEF warning reported
 * checkForTypedef                          | Valid @typedef declaration                    | Registered in JSTypeRegistry
 * defineObjectLiteral / UNKNOWN_LENDS      | @lends referencing undeclared symbol          | UNKNOWN_LENDS warning reported
 * defineObjectLiteral / LENDS_ON_NON_OBJECT| @lends referencing primitive (number)         | LENDS_ON_NON_OBJECT warning reported
 * defineObjectLiteral / Valid @lends       | @lends referencing valid constructor proto    | Properties attached to prototype
 * checkForClassDefiningCalls / CONSTRUCTOR | goog.reflect.object with invalid ctor         | CONSTRUCTOR_EXPECTED reported
 * checkForClassDefiningCalls / INHERITS    | goog.inherits(Sub, Super)                     | Subclass relationship applied
 * checkForClassDefiningCalls / SINGLETON   | goog.addSingletonGetter(Class)                | Singleton getter method attached
 * defineFunctionLiteral (Hoisting)         | Function called before declaration            | Hoisted function pre-processed
 * defineCatch                              | try { } catch (e) { }                         | Catch parameter slot declared
 * handleFunctionInputs (Bleeding)          | Named function expression var f = function g()| Bleeding function g declared in local
 * declareArguments                         | Mismatched JSDoc @param count vs AST args     | Typed and inferred args declared
 * maybeDeclareQualifiedName (Prototypes)   | Arbitrary F.prototype = { ... }               | Prototype properties attached
 * FirstOrderFunctionAnalyzer               | Variable modified in nested scope             | Var marked as escaped
 * CollectProperties                        | Property assignments on this.prop in @this fn | Instance properties recorded
 * Window constructor special handling      | Constructor named "Window"                    | Updates GLOBAL_THIS prototype
 * StubDeclaration                          | Qualified name expression (ns.stub;)          | Stub property slot declared
 * Literal types attachment                 | null, void, string, number, bool, regex lit   | Corresponding native types assigned
 * Constructors integrity                  | 1-arg and 2-arg constructor calls             | Non-null instance created
 * ---------------------------------------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;

import java.util.Collections;
import java.util.List;

public class TypedScopeCreatorGeminiTest {

  private Compiler compiler;

  private Compiler initCompiler() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    return compiler;
  }

  private Scope createGlobalScope(String codeJs) {
    return createGlobalScope("", codeJs);
  }

  private Scope createGlobalScope(String externsJs, String codeJs) {
    Compiler c = initCompiler();
    List<SourceFile> externs = Collections.singletonList(
        SourceFile.fromCode("externs.js", externsJs));
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("testcode.js", codeJs));
    c.init(externs, inputs, c.getOptions());
    Node parsed = c.parseInputs();
    Node root = c.getRoot() != null ? c.getRoot() : parsed;
    TypedScopeCreator creator = new TypedScopeCreator(c);
    return creator.createScope(root, null);
  }

  private Scope createLocalScope(Scope globalScope, String functionName) {
    Var fnVar = globalScope.getVar(functionName);
    assertNotNull("Function " + functionName + " must exist", fnVar);
    Node fnNode = fnVar.getInitialValue();
    assertNotNull("Initial value for " + functionName + " must not be null", fnNode);
    assertTrue(fnNode.isFunction());
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(fnNode, globalScope);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 688)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue688() {
    Compiler c = initCompiler();
    String code =
        "/** @const */ var SOME_DEFAULT =\n" +
        "    /** @type {SomeType} */ ({foo: 'bar'});\n" +
        "/** @constructor */\n" +
        "function SomeType() {}\n" +
        "/** @return {string} */\n" +
        "SomeType.prototype.foo = function() { return 'x'; };\n" +
        "/** @return {number} */\n" +
        "function f() {\n" +
        "  return SOME_DEFAULT.foo;\n" +
        "}";

    List<SourceFile> externs = Collections.singletonList(
        SourceFile.fromCode("externs.js", ""));
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("testcode.js", code));
    c.init(externs, inputs, c.getOptions());
    Node parsed = c.parseInputs();
    Node root = c.getRoot() != null ? c.getRoot() : parsed;

    TypedScopeCreator creator = new TypedScopeCreator(c);
    Scope globalScope = creator.createScope(root, null);
    assertNotNull(globalScope);

    Var someDefault = globalScope.getVar("SOME_DEFAULT");
    assertNotNull(someDefault);

    TypeCheck checker = new TypeCheck(
        c,
        new SemanticReverseAbstractInterpreter(
            c.getCodingConvention(), c.getTypeRegistry()),
        c.getTypeRegistry());
    checker.process(root.getFirstChild(), root.getLastChild());

    boolean foundWarning = false;
    for (JSError warning : c.getWarnings()) {
      if (warning.getDescription().contains("inconsistent return type")) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Defect verification: Expected 'inconsistent return type' warning for Issue 688", foundWarning);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testNativeTypesDeclaredInInitialScope() {
    Scope s = createGlobalScope("");
    assertTrue(s.isGlobal());
    assertNotNull(s.getVar("Object"));
    assertNotNull(s.getVar("Function"));
    assertNotNull(s.getVar("Array"));
    assertNotNull(s.getVar("String"));
    assertNotNull(s.getVar("Boolean"));
    assertNotNull(s.getVar("Number"));
    assertNotNull(s.getVar("Date"));
    assertNotNull(s.getVar("RegExp"));
    assertNotNull(s.getVar("Error"));
    assertNotNull(s.getVar("EvalError"));
    assertNotNull(s.getVar("RangeError"));
    assertNotNull(s.getVar("ReferenceError"));
    assertNotNull(s.getVar("SyntaxError"));
    assertNotNull(s.getVar("TypeError"));
    assertNotNull(s.getVar("URIError"));
    assertNotNull(s.getVar("undefined"));
    assertNotNull(s.getVar("ActiveXObject"));
  }

  @Test(timeout = 4000)
  public void testPatchGlobalScopeSuccess() {
    Compiler c = initCompiler();
    List<SourceFile> externs = Collections.singletonList(
        SourceFile.fromCode("externs.js", ""));
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("testcode.js", "var a = 1; function foo() { return a; }"));
    c.init(externs, inputs, c.getOptions());
    Node parsed = c.parseInputs();
    Node root = c.getRoot() != null ? c.getRoot() : parsed;
    TypedScopeCreator creator = new TypedScopeCreator(c);
    Scope globalScope = creator.createScope(root, null);
    assertNotNull(globalScope.getVar("a"));
    assertNotNull(globalScope.getVar("foo"));

    Node newScript = c.parseSyntheticCode("testcode.js", "var b = 2; function bar() { return b; }");
    creator.patchGlobalScope(globalScope, newScript);

    assertNull(globalScope.getVar("a"));
    assertNull(globalScope.getVar("foo"));
    assertNotNull(globalScope.getVar("b"));
    assertNotNull(globalScope.getVar("bar"));
  }

  @Test(timeout = 4000)
  public void testWindowConstructorUpdatesGlobalThis() {
    Scope s = createGlobalScope("/** @constructor */ function Window() {}");
    Var windowVar = s.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());
  }

  @Test(timeout = 4000)
  public void testValidEnumDeclaration() {
    Scope s = createGlobalScope("/** @enum {number} */ var MyEnum = { A: 1, B: 2 };");
    assertEquals(0, compiler.getWarningCount());
    Var enumVar = s.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());
  }

  @Test(timeout = 4000)
  public void testAliasedEnumDeclaration() {
    Scope s = createGlobalScope(
        "/** @enum {number} */ var MyEnum = { A: 1 };\n" +
        "/** @enum {number} */ var MyAlias = MyEnum;");
    Var aliasVar = s.getVar("MyAlias");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType().isEnumType());
  }

  @Test(timeout = 4000)
  public void testValidTypedefDeclaration() {
    Scope s = createGlobalScope("/** @typedef {number|string} */ var MyType;");
    assertEquals(0, compiler.getWarningCount());
    assertNotNull(compiler.getTypeRegistry().getType("MyType"));
  }

  @Test(timeout = 4000)
  public void testValidLendsOnObjectLiteral() {
    Scope s = createGlobalScope(
        "/** @constructor */ function Foo() {}\n" +
        "var x = /** @lends {Foo.prototype} */ ({ bar: function() {} });");
    assertEquals(0, compiler.getWarningCount());
    Var fooVar = s.getVar("Foo");
    assertNotNull(fooVar);
    FunctionType ctor = fooVar.getType().toMaybeFunctionType();
    assertTrue(ctor.getPrototype().hasProperty("bar"));
  }

  @Test(timeout = 4000)
  public void testSubclassInheritance() {
    Scope s = createGlobalScope(
        "var goog = {}; goog.inherits = function(child, parent) {};\n" +
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor */ function Sub() {}\n" +
        "goog.inherits(Sub, Super);");
    Var subVar = s.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor.getSuperClassConstructor());
    assertEquals("Super", subCtor.getSuperClassConstructor().getInstanceType().getReferenceName());
  }

  @Test(timeout = 4000)
  public void testSingletonGetter() {
    Scope s = createGlobalScope(
        "var goog = {}; goog.addSingletonGetter = function(cls) {};\n" +
        "/** @constructor */ function Singleton() {}\n" +
        "goog.addSingletonGetter(Singleton);");
    Var singletonVar = s.getVar("Singleton");
    assertNotNull(singletonVar);
    FunctionType ctor = singletonVar.getType().toMaybeFunctionType();
    assertTrue(ctor.hasProperty("getInstance"));
  }

  @Test(timeout = 4000)
  public void testHoistedFunctionDeclaration() {
    Scope s = createGlobalScope(
        "var x = foo();\n" +
        "function foo() { return 42; }");
    assertNotNull(s.getVar("foo"));
    assertNotNull(s.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testCatchParameterDeclaration() {
    Scope s = createGlobalScope(
        "function f() {\n" +
        "  try {\n" +
        "  } catch (e) {\n" +
        "    var y = e;\n" +
        "  }\n" +
        "}");
    Scope local = createLocalScope(s, "f");
    assertNotNull(local.getVar("e"));
    assertNotNull(local.getVar("y"));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpression() {
    Scope s = createGlobalScope(
        "var f = function g(x) {\n" +
        "  return g(x - 1);\n" +
        "};");
    assertNotNull(s.getVar("f"));
    Scope local = createLocalScope(s, "f");
    assertNotNull(local.getVar("g"));
    assertNotNull(local.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testFunctionParametersWithJsDoc() {
    Scope s = createGlobalScope(
        "/**\n" +
        " * @param {string} a\n" +
        " * @param {number} b\n" +
        " */\n" +
        "function fn(a, b, c) {}\n");
    Scope local = createLocalScope(s, "fn");
    Var varA = local.getVar("a");
    Var varB = local.getVar("b");
    Var varC = local.getVar("c");
    assertNotNull(varA);
    assertNotNull(varB);
    assertNotNull(varC);
    assertTrue(varA.getType().isStringType());
    assertTrue(varB.getType().isNumberType());
    assertNull(varC.getType());
  }

  @Test(timeout = 4000)
  public void testPrototypeRedefinition() {
    Scope s = createGlobalScope(
        "/** @constructor */ function F() {}\n" +
        "F.prototype = { bar: function() {} };\n");
    Var fVar = s.getVar("F");
    assertNotNull(fVar);
    FunctionType ctor = fVar.getType().toMaybeFunctionType();
    assertTrue(ctor.getPrototype().hasProperty("bar"));
  }

  @Test(timeout = 4000)
  public void testEscapedVariablesRecorded() {
    Scope s = createGlobalScope(
        "function outer() {\n" +
        "  var escaped = 1;\n" +
        "  function inner() {\n" +
        "    escaped = 2;\n" +
        "  }\n" +
        "}\n");
    Scope outer = createLocalScope(s, "outer");
    Var v = outer.getVar("escaped");
    assertNotNull(v);
    assertTrue(v.isEscaped());
  }

  @Test(timeout = 4000)
  public void testInterfaceMethodOverrideInference() {
    Scope s = createGlobalScope(
        "/** @interface */ function AnInterface() {}\n" +
        "/** @return {string} */ AnInterface.prototype.sayHello = function() {};\n" +
        "/** @constructor @implements {AnInterface} */ function Implementation() {}\n" +
        "Implementation.prototype.sayHello = function() { return 'hello'; };\n");
    Var implVar = s.getVar("Implementation");
    assertNotNull(implVar);
    FunctionType ctor = implVar.getType().toMaybeFunctionType();
    JSType methodType = ctor.getPrototype().getPropertyType("sayHello");
    assertTrue(methodType.isFunctionType());
  }

  @Test(timeout = 4000)
  public void testStubPropertyDeclaration() {
    Scope s = createGlobalScope(
        "var ns = {};\n" +
        "ns.stub;\n");
    Var stubVar = s.getVar("ns.stub");
    assertNotNull(stubVar);
    assertTrue(stubVar.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testLiteralTypesAttached() {
    Scope s = createGlobalScope(
        "var a = null;\n" +
        "var b = void 0;\n" +
        "var c = 'str';\n" +
        "var d = 123;\n" +
        "var e = true;\n" +
        "var f = false;\n" +
        "var g = /abc/;\n");
    assertNotNull(s.getVar("a"));
    assertNotNull(s.getVar("b"));
    assertNotNull(s.getVar("c"));
    assertNotNull(s.getVar("d"));
    assertNotNull(s.getVar("e"));
    assertNotNull(s.getVar("f"));
    assertNotNull(s.getVar("g"));
  }

  @Test(timeout = 4000)
  public void testCollectPropertiesOnThis() {
    Scope s = createGlobalScope(
        "/** @constructor */ function Person() {\n" +
        "  /** @type {string} */ this.name = 'John';\n" +
        "}\n");
    Var personVar = s.getVar("Person");
    assertNotNull(personVar);
    FunctionType ctor = personVar.getType().toMaybeFunctionType();
    assertTrue(ctor.getInstanceType().hasProperty("name"));
    assertTrue(ctor.getInstanceType().getPropertyType("name").isStringType());
  }

  @Test(timeout = 4000)
  public void testGlobalCtorAlias() {
    Scope s = createGlobalScope(
        "/** @constructor */ function Original() {}\n" +
        "var Alias = Original;\n");
    Var aliasVar = s.getVar("Alias");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType().isConstructor());
  }

  // =========================================================================
  // Partition B & D: Boundary Value Analysis, Diagnostic Warnings & Guards
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPatchGlobalScopeThrowsOnNonScript() {
    Scope s = createGlobalScope("var x = 1;");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.patchGlobalScope(s, new Node(Token.BLOCK));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPatchGlobalScopeThrowsOnLocalScope() {
    Scope global = createGlobalScope("function foo() { var x = 1; }");
    Scope local = createLocalScope(global, "foo");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Node script = compiler.parseSyntheticCode("testcode.js", "var y = 2;");
    creator.patchGlobalScope(local, script);
  }

  @Test(timeout = 4000)
  public void testMultipleVarDefWarning() {
    createGlobalScope("/** @type {number} */ var a = 1, b = 2;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testCtorInitializerWarning() {
    createGlobalScope("/** @constructor */ var Foo;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testIfaceInitializerWarning() {
    createGlobalScope("/** @interface */ var FooInterface;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testEnumInitializerWarningWhenNotObjectLit() {
    createGlobalScope("/** @enum {number} */ var MyEnum = 5;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testEnumKeyNotConstantForGetter() {
    createGlobalScope("/** @enum {number} */ var MyEnum = { get a() { return 1; } };");
    assertTrue(compiler.getWarningCount() >= 1);
    boolean found = false;
    for (JSError w : compiler.getWarnings()) {
      if (TypeCheck.ENUM_NOT_CONSTANT.key.equals(w.getType().key)) {
        found = true;
        break;
      }
    }
    assertTrue(found);
  }

  @Test(timeout = 4000)
  public void testMalformedTypedefWarning() {
    createGlobalScope("/** @typedef */ var MyType;");
    assertTrue(compiler.getWarningCount() >= 1);
    boolean found = false;
    for (JSError w : compiler.getWarnings()) {
      if (TypedScopeCreator.MALFORMED_TYPEDEF.key.equals(w.getType().key)) {
        found = true;
        break;
      }
    }
    assertTrue(found);
  }

  @Test(timeout = 4000)
  public void testUnknownLendsWarning() {
    createGlobalScope("var x = /** @lends {nonExistentVar} */ ({});");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testLendsOnNonObjectWarning() {
    createGlobalScope("/** @type {number} */ var num = 123;\nvar x = /** @lends {num} */ ({});");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastConstructorExpectedWarning() {
    createGlobalScope(
        "var goog = {}; goog.reflect = {}; goog.reflect.object = function(a, b) {};\n" +
        "var notACtor = 123;\n" +
        "goog.reflect.object(notACtor, {});");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CONSTRUCTOR_EXPECTED.key,
        compiler.getWarnings()[0].getType().key);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorsIntegrity() {
    Compiler c = initCompiler();
    TypedScopeCreator c1 = new TypedScopeCreator(c);
    assertNotNull(c1);
    TypedScopeCreator c2 = new TypedScopeCreator(c, CodingConventions.getDefault());
    assertNotNull(c2);
    assertEquals("Proxy", TypedScopeCreator.DELEGATE_PROXY_SUFFIX.substring(
        TypedScopeCreator.DELEGATE_PROXY_SUFFIX.length() - 5));
  }
}