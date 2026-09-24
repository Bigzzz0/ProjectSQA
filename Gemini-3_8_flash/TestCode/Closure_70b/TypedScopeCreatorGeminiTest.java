/*
 * Copyright 2023 The Closure Compiler Authors.
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
 * ====================================================================================================
 * Defect ID: Closure / TypedScopeCreator / testDuplicateLocalVarDecl & testFunctionArguments13
 * Core Root Cause:
 *   In TypedScopeCreator.LocalScopeBuilder#declareArguments(Node), parameters with explicit JSDoc
 *   parameter types (@param) are declared via:
 *       defineSlot(astParameter, functionNode, jsDocParameter.getJSType(), true);
 *   The 4th argument 'inferred' is hardcoded to true instead of checking whether the parameter type
 *   was explicitly declared (e.g. false when jsDocParameter.getJSType() != null).
 *   Because 'inferred' is true, Var#isTypeInferred() returns true, suppressing duplicate local
 *   variable type contradiction warnings (e.g., in TypeCheck / TypeValidator).
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. LocalScopeBuilder#declareArguments:
 *    - Parameter WITH explicit @param type -> Var#isTypeInferred() MUST be false. (DEFECT REVEALING)
 *    - Parameter WITHOUT JSDoc type -> Var#isTypeInferred() MUST be true.
 * 2. TypedScopeCreator#createInitialScope:
 *    - Native bindings registration (Object, Array, Function, Date, RegExp, undefined, ActiveXObject).
 *    - DiscoverEnumsAndTypedefs shallow pass.
 * 3. AbstractScopeBuilder#attachLiteralTypes:
 *    - NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT.
 * 4. AbstractScopeBuilder#defineObjectLiteral & processObjectLitProperties:
 *    - @lends resolution: valid object target, unknown lends target, non-object lends target.
 *    - Property types on anonymous and named object literals.
 * 5. AbstractScopeBuilder#defineSlot & Special Names:
 *    - Global variable named "Window" triggering GLOBAL_THIS prototype reassignment.
 *    - Constructor / Interface initialization requirement (CTOR_INITIALIZER, IFACE_INITIALIZER).
 *    - Enum definition & validation (ENUM_INITIALIZER, ENUM_DUP, ENUM_NOT_CONSTANT).
 * 6. AbstractScopeBuilder#checkForClassDefiningCalls:
 *    - Subclass relationship (goog.inherits).
 *    - Singleton getter (goog.addSingletonGetter).
 *    - ObjectLiteralCast with non-constructor (CONSTRUCTOR_EXPECTED).
 * 7. GlobalScopeBuilder & Typedefs:
 *    - New-style @typedef: valid definition and MALFORMED_TYPEDEF.
 *    - Old-style typedef (goog.typedef).
 * 8. Function & Variable Scopes:
 *    - Bleeding function expression names in local scope.
 *    - Catch block parameter slot creation.
 *    - Multiple VAR declaration with JSDoc (MULTIPLE_VAR_DEF).
 *    - Prototype assignment and undeclaration / redefinition.
 *    - Stub declarations in resolveStubDeclarations.
 * ====================================================================================================
 */
public class TypedScopeCreatorGeminiTest {

  private Compiler createCompiler() {
    return createCompiler(new ClosureCodingConvention());
  }

  private Compiler createCompiler(CodingConvention convention) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initOptions(options);
    return compiler;
  }

  private Node parse(Compiler compiler, String js) {
    return compiler.parseTestCode(js);
  }

  private boolean hasWarning(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == type) {
        return true;
      }
    }
    return false;
  }

  // ==================================================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testGlobalScopeCreationWithPrimitives() {
    Compiler compiler = createCompiler();
    String js = "var n = 123;\n" +
                "var s = 'hello';\n" +
                "var b = true;\n" +
                "var nl = null;\n" +
                "var vd = void 0;\n" +
                "var r = /abc/;\n";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertNotNull(globalScope);
    assertTrue(globalScope.isGlobal());

    Scope.Var varN = globalScope.getVar("n");
    assertNotNull(varN);
    assertEquals("number", varN.getType().toString());

    Scope.Var varS = globalScope.getVar("s");
    assertNotNull(varS);
    assertEquals("string", varS.getType().toString());

    Scope.Var varB = globalScope.getVar("b");
    assertNotNull(varB);
    assertEquals("boolean", varB.getType().toString());

    Scope.Var varNl = globalScope.getVar("nl");
    assertNotNull(varNl);
    assertEquals("null", varNl.getType().toString());

    Scope.Var varVd = globalScope.getVar("vd");
    assertNotNull(varVd);
    assertEquals("undefined", varVd.getType().toString());

    Scope.Var varR = globalScope.getVar("r");
    assertNotNull(varR);
    assertEquals("RegExp", varR.getType().toString());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationInGlobalScope() {
    Compiler compiler = createCompiler();
    String js = "/**\n" +
                " * @param {number} x\n" +
                " * @return {string}\n" +
                " */\n" +
                "function foo(x) { return '' + x; }";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var fooVar = globalScope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType() instanceof FunctionType);

    FunctionType fnType = (FunctionType) fooVar.getType();
    assertEquals("string", fnType.getReturnType().toString());
    assertEquals(1, fnType.getParametersCount());
  }

  @Test(timeout = 4000)
  public void testConstructorDeclarationAndPrototype() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ function Animal() {}\n" +
                "Animal.prototype.speak = function() {};";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var animalVar = globalScope.getVar("Animal");
    assertNotNull(animalVar);
    assertTrue(animalVar.getType().isConstructor());

    Scope.Var protoVar = globalScope.getVar("Animal.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());

    Scope.Var speakVar = globalScope.getVar("Animal.prototype.speak");
    assertNotNull(speakVar);
    assertTrue(speakVar.getType() instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testInterfaceDeclaration() {
    Compiler compiler = createCompiler();
    String js = "/** @interface */ function Disposable() {}\n" +
                "Disposable.prototype.dispose = function() {};";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var dispVar = globalScope.getVar("Disposable");
    assertNotNull(dispVar);
    assertTrue(dispVar.getType().isInterface());
  }

  @Test(timeout = 4000)
  public void testFunctionWithThisTypeCollectProperties() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ function Car() { /** @type {number} */ this.speed = 0; }";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var carVar = globalScope.getVar("Car");
    assertNotNull(carVar);
    FunctionType carCtor = (FunctionType) carVar.getType();
    ObjectType instanceType = carCtor.getInstanceType();
    assertTrue(instanceType.hasProperty("speed"));
    assertEquals("number", instanceType.getPropertyType("speed").toString());
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpressionInLocalScope() {
    Compiler compiler = createCompiler();
    String js = "var outer = function inner() { return inner; };";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertNull("Bleeding name should not leak to global scope", globalScope.getVar("inner"));
    Node fnNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());

    Scope localScope = creator.createScope(fnNode, globalScope);
    Scope.Var innerVar = localScope.getVar("inner");
    assertNotNull("Bleeding function name must exist in its local scope", innerVar);
    assertTrue(innerVar.getType() instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testCatchParameterScope() {
    Compiler compiler = createCompiler();
    String js = "function runner() { try { var x = 1; } catch (err) { var y = err; } }";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);
    Scope.Var errVar = localScope.getVar("err");
    assertNotNull("Catch parameter must be declared in the function scope", errVar);
  }

  // ==================================================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptInitialScopeBindings() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertNotNull(globalScope.getVar("Object"));
    assertNotNull(globalScope.getVar("Array"));
    assertNotNull(globalScope.getVar("Function"));
    assertNotNull(globalScope.getVar("Date"));
    assertNotNull(globalScope.getVar("RegExp"));
    assertNotNull(globalScope.getVar("String"));
    assertNotNull(globalScope.getVar("Number"));
    assertNotNull(globalScope.getVar("Boolean"));
    assertNotNull(globalScope.getVar("Error"));
    assertNotNull(globalScope.getVar("undefined"));
    assertNotNull(globalScope.getVar("ActiveXObject"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        globalScope.getVar("ActiveXObject").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE),
        globalScope.getVar("undefined").getType());
  }

  @Test(timeout = 4000)
  public void testSpecialGlobalWindowConstructor() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ function Window() {}";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var winVar = globalScope.getVar("Window");
    assertNotNull(winVar);
    assertTrue(winVar.getType().isConstructor());

    ObjectType globalThis = compiler.getTypeRegistry().getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertNotNull(globalThis);
    assertEquals(((FunctionType) winVar.getType()).getInstanceType(), globalThis.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testLendsAnnotationValid() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ function Person() {}\n" +
                "Person.prototype = /** @lends {Person.prototype} */ ({ sayHi: function() {} });";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertFalse(hasWarning(compiler, TypedScopeCreator.UNKNOWN_LENDS));
    assertFalse(hasWarning(compiler, TypedScopeCreator.LENDS_ON_NON_OBJECT));
    Scope.Var protoVar = globalScope.getVar("Person.prototype");
    assertNotNull(protoVar);
  }

  @Test(timeout = 4000)
  public void testLendsAnnotationOnNonObject() {
    Compiler compiler = createCompiler();
    String js = "var notAnObj = 123;\n" +
                "var literal = /** @lends {notAnObj} */ ({ prop: 'val' });";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report LENDS_ON_NON_OBJECT",
        hasWarning(compiler, TypedScopeCreator.LENDS_ON_NON_OBJECT));
  }

  @Test(timeout = 4000)
  public void testLendsAnnotationOnUnknownTarget() {
    Compiler compiler = createCompiler();
    String js = "var literal = /** @lends {NonExistentClass.prototype} */ ({ prop: 'val' });";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report UNKNOWN_LENDS",
        hasWarning(compiler, TypedScopeCreator.UNKNOWN_LENDS));
  }

  // ==================================================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // ==================================================================================================

  /**
   * Targets the defect where TypedScopeCreator.LocalScopeBuilder#declareArguments marks parameters
   * with explicit @param annotations as inferred (inferred = true), preventing downstream type
   * checking from warning about type discrepancies or duplicate local variable declarations.
   */
  @Test(timeout = 4000)
  public void testParamWithJsDocTypeNotMarkedAsInferred_TargetDefect() {
    Compiler compiler = createCompiler();
    String js = "/** @param {number} x */ function f(x) { return x; }";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Scope localScope = creator.createScope(fnNode, globalScope);

    Scope.Var xVar = localScope.getVar("x");
    assertNotNull("Parameter x should be registered in local scope", xVar);
    assertNotNull("Parameter x should possess a declared type", xVar.getType());
    assertTrue("Parameter x should be recognized as number", xVar.getType().isNumber());

    // CRITICAL ASSERTION:
    // In the defective version, declareArguments calls:
    //   defineSlot(astParameter, functionNode, jsDocParameter.getJSType(), true);
    // Hardcoding 'true' forces Var#isTypeInferred() to true, causing the defect.
    // In the corrected version, it must NOT be inferred because it has an explicit @param JSDoc type.
    assertFalse("Parameter declared with @param {number} MUST NOT be inferred", xVar.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testParamWithoutJsDocTypeMarkedAsInferred() {
    Compiler compiler = createCompiler();
    String js = "function f(untypedArg) { return untypedArg; }";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);

    Scope.Var untypedVar = localScope.getVar("untypedArg");
    assertNotNull(untypedVar);
    assertTrue("Parameter without @param annotation SHOULD be inferred", untypedVar.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testMultipleParamsWithMixedAnnotations_TargetDefect() {
    Compiler compiler = createCompiler();
    String js = "/**\n" +
                " * @param {string} a\n" +
                " * @param {boolean} b\n" +
                " */\n" +
                "function mix(a, b, c) {}";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);

    Scope.Var varA = localScope.getVar("a");
    Scope.Var varB = localScope.getVar("b");
    Scope.Var varC = localScope.getVar("c");

    assertNotNull(varA);
    assertNotNull(varB);
    assertNotNull(varC);

    assertFalse("Param 'a' with explicit @param MUST NOT be inferred", varA.isTypeInferred());
    assertFalse("Param 'b' with explicit @param MUST NOT be inferred", varB.isTypeInferred());
    assertTrue("Param 'c' lacking @param MUST be inferred", varC.isTypeInferred());
  }

  // ==================================================================================================
  // PARTITION D: Exception & Diagnostic Guard Paths
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testMultipleVarDefWarning() {
    Compiler compiler = createCompiler();
    String js = "/** @type {number} */ var a = 1, b = 2;";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report MULTIPLE_VAR_DEF warning",
        hasWarning(compiler, TypeCheck.MULTIPLE_VAR_DEF));
  }

  @Test(timeout = 4000)
  public void testConstructorInitializerWarning() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ var UninitializedCtor;";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report CTOR_INITIALIZER warning",
        hasWarning(compiler, TypedScopeCreator.CTOR_INITIALIZER));
  }

  @Test(timeout = 4000)
  public void testInterfaceInitializerWarning() {
    Compiler compiler = createCompiler();
    String js = "/** @interface */ var UninitializedIface;";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report IFACE_INITIALIZER warning",
        hasWarning(compiler, TypedScopeCreator.IFACE_INITIALIZER));
  }

  @Test(timeout = 4000)
  public void testEnumInitializerWarning() {
    Compiler compiler = createCompiler();
    String js = "/** @enum {number} */ var BadEnum = 42;";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report ENUM_INITIALIZER warning",
        hasWarning(compiler, TypedScopeCreator.ENUM_INITIALIZER));
  }

  @Test(timeout = 4000)
  public void testEnumDuplicateKeyWarning() {
    Compiler compiler = createCompiler();
    String js = "/** @enum {number} */ var DuplicateKeyEnum = { VAL: 1, VAL: 2 };";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report ENUM_DUP warning",
        hasWarning(compiler, TypeCheck.ENUM_DUP));
  }

  @Test(timeout = 4000)
  public void testMalformedTypedefWarning() {
    Compiler compiler = createCompiler();
    String js = "/** @typedef */ var EmptyTypedef;";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report MALFORMED_TYPEDEF warning",
        hasWarning(compiler, TypedScopeCreator.MALFORMED_TYPEDEF));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralCastConstructorExpectedWarning() {
    Compiler compiler = createCompiler();
    String js = "var notACtor = 123;\n" +
                "goog.reflect.object(notACtor, { x: 1 });";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertTrue("Should report CONSTRUCTOR_EXPECTED warning",
        hasWarning(compiler, TypedScopeCreator.CONSTRUCTOR_EXPECTED));
  }

  // ==================================================================================================
  // PARTITION E: Advanced Structures & Coding Conventions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testValidEnumElementsAndAliasing() {
    Compiler compiler = createCompiler();
    String js = "/** @enum {string} */ var Direction = { NORTH: 'N', SOUTH: 'S' };\n" +
                "/** @type {Direction} */ var alias = Direction;";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var dirVar = globalScope.getVar("Direction");
    assertNotNull(dirVar);
    assertTrue(dirVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) dirVar.getType();
    assertTrue(enumType.hasOwnProperty("NORTH"));
    assertTrue(enumType.hasOwnProperty("SOUTH"));
    assertEquals("string", enumType.getElementsType().toString());
  }

  @Test(timeout = 4000)
  public void testInheritanceViaGoogInherits() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ function SuperClass() {}\n" +
                "/** @constructor */ function SubClass() {}\n" +
                "goog.inherits(SubClass, SuperClass);";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var subVar = globalScope.getVar("SubClass");
    Scope.Var superVar = globalScope.getVar("SuperClass");
    assertNotNull(subVar);
    assertNotNull(superVar);

    FunctionType subCtor = (FunctionType) subVar.getType();
    FunctionType superCtor = (FunctionType) superVar.getType();
    assertEquals(superCtor.getInstanceType(), subCtor.getSuperClassConstructor().getInstanceType());
  }

  @Test(timeout = 4000)
  public void testAddSingletonGetter() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ function Manager() {}\n" +
                "goog.addSingletonGetter(Manager);";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var mgrVar = globalScope.getVar("Manager");
    assertNotNull(mgrVar);
    FunctionType mgrCtor = (FunctionType) mgrVar.getType();
    assertTrue(mgrCtor.hasProperty("getInstance"));
    assertTrue(mgrCtor.getPropertyType("getInstance") instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testPrototypeRedefinitionUndeclaration() {
    Compiler compiler = createCompiler();
    String js = "function Base() {}\n" +
                "Base.prototype = { methodA: function() {} };";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var protoVar = globalScope.getVar("Base.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());
  }

  @Test(timeout = 4000)
  public void testStubDeclarationsResolution() {
    Compiler compiler = createCompiler();
    String js = "/** @constructor */ function Widget() {}\n" +
                "Widget.prototype.render;\n";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var stubVar = globalScope.getVar("Widget.prototype.render");
    assertNotNull(stubVar);
    assertTrue(stubVar.getType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testOrIdiomNamespaceDeclaration() {
    Compiler compiler = createCompiler();
    String js = "var my = my || {};\n" +
                "/** @const */ my.NAME = 'Closure';";
    Node root = parse(compiler, js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var myVar = globalScope.getVar("my");
    assertNotNull(myVar);
    Scope.Var nameVar = globalScope.getVar("my.NAME");
    assertNotNull(nameVar);
    assertEquals("string", nameVar.getType().toString());
  }

  @Test(timeout = 4000)
  public void testDelegateProxySuffixConstant() {
    assertNotNull(TypedScopeCreator.DELEGATE_PROXY_SUFFIX);
    assertTrue(TypedScopeCreator.DELEGATE_PROXY_SUFFIX.contains("Proxy"));
  }

  @Test(timeout = 4000)
  public void testCustomCodingConventionConstructor() {
    GoogleCodingConvention customConvention = new GoogleCodingConvention();
    Compiler compiler = createCompiler(customConvention);
    TypedScopeCreator creator = new TypedScopeCreator(compiler, customConvention);
    Node root = parse(compiler, "var x = 1;");
    Scope scope = creator.createScope(root, null);
    assertNotNull(scope);
    assertNotNull(scope.getVar("x"));
  }
}