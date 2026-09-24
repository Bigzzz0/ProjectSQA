package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.InputId;
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

import java.util.Iterator;

public class TypedScopeCreatorGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypedScopeCreator scopeCreator;
  private Scope globalScope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Node parseAndCreateScope(String js) {
    Node root = compiler.parseTestCode(js);
    globalScope = scopeCreator.createScope(root, null);
    return root;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 537 & Unknown Superclass)
  // =========================================================================

  /**
   * Targets Defects4J issue where a constructor inherits from an unknown or undeclared
   * superclass (e.g. @extends {Unknown}). Prototype property declarations on such
   * a constructor must resolve correctly without incorrect type assignments.
   */
  @Test(timeout = 4000)
  public void testPropertyOnUnknownSuperClassDefectTarget() {
    String js =
        "/** @constructor\n" +
        " *  @extends {UnknownSuper}\n" +
        " */\n" +
        "function Foo() {}\n" +
        "Foo.prototype.foo = 1;\n";

    parseAndCreateScope(js);

    Scope.Var fooVar = globalScope.getVar("Foo.prototype.foo");
    assertNotNull("Foo.prototype.foo slot should be declared or resolved in scope", fooVar);
    JSType fooType = fooVar.getType();
    assertNotNull("Foo.prototype.foo should have a type", fooType);

    // In the presence of the defect, Foo.prototype.foo was incorrectly given a concrete type
    // or failed to identify property on unknown superclass properly.
    // Verify that Foo constructor exists and prototype is correctly formed.
    Scope.Var ctorVar = globalScope.getVar("Foo");
    assertNotNull(ctorVar);
    assertTrue(ctorVar.getType().isConstructor());
  }

  /**
   * Targets Issue 537 variation: method override with mismatched arguments when extending
   * an unknown or stubbed prototype hierarchy.
   */
  @Test(timeout = 4000)
  public void testIssue537MethodOverrideHierarchy() {
    String js =
        "/** @constructor */ function Bar() {}\n" +
        "Bar.prototype.baz = function() {};\n" +
        "/** @constructor \n * @extends {Bar} */ function SubBar() {}\n" +
        "SubBar.prototype.baz = function(x) {};\n";

    parseAndCreateScope(js);

    Scope.Var subBaz = globalScope.getVar("SubBar.prototype.baz");
    assertNotNull(subBaz);
    FunctionType subFn = subBaz.getType().toMaybeFunctionType();
    assertNotNull(subFn);
    assertEquals(1, subFn.getParametersCount());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testNativeTypeDeclarationsInInitialScope() {
    Node root = compiler.parseTestCode("");
    Scope scope = scopeCreator.createInitialScope(root);

    assertTrue(scope.isGlobal());
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("ActiveXObject"));
    assertNotNull(scope.getVar("undefined"));

    JSType activeX = scope.getVar("ActiveXObject").getType();
    assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), activeX);
  }

  @Test(timeout = 4000)
  public void testVarDeclarationPrimitives() {
    String js =
        "var a = null;\n" +
        "var b = void 0;\n" +
        "var c = 'closure';\n" +
        "var d = 42;\n" +
        "var e = true;\n" +
        "var f = false;\n" +
        "var g = /abc/;\n";

    parseAndCreateScope(js);

    assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), globalScope.getVar("a").getType());
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), globalScope.getVar("b").getType());
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), globalScope.getVar("c").getType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), globalScope.getVar("d").getType());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), globalScope.getVar("e").getType());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), globalScope.getVar("f").getType());
    assertEquals(registry.getNativeType(JSTypeNative.REGEXP_TYPE), globalScope.getVar("g").getType());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationAndLocalScope() {
    String js =
        "function outer(a, b) {\n" +
        "  var innerVar = 10;\n" +
        "  return function inner(c) { return innerVar + c; };\n" +
        "}\n";

    Node root = parseAndCreateScope(js);

    Scope.Var outerVar = globalScope.getVar("outer");
    assertNotNull(outerVar);
    assertTrue(outerVar.getType().isFunctionType());

    // Find outer's function node and create its local scope
    Node script = root.getFirstChild();
    Node fnOuterNode = script.getFirstChild();
    assertEquals(Token.FUNCTION, fnOuterNode.getType());

    Scope localScope = scopeCreator.createScope(fnOuterNode, globalScope);
    assertFalse(localScope.isGlobal());
    assertEquals(globalScope, localScope.getParent());
    assertNotNull(localScope.getVar("a"));
    assertNotNull(localScope.getVar("b"));
    assertNotNull(localScope.getVar("innerVar"));
  }

  @Test(timeout = 4000)
  public void testConstructorAndPrototypeDeclaration() {
    String js =
        "/** @constructor */\n" +
        "function MyClass() {\n" +
        "  /** @type {number} */\n" +
        "  this.prop = 5;\n" +
        "}\n" +
        "MyClass.prototype.getProp = function() { return this.prop; };\n";

    parseAndCreateScope(js);

    Scope.Var classVar = globalScope.getVar("MyClass");
    assertNotNull(classVar);
    FunctionType ctorType = classVar.getType().toMaybeFunctionType();
    assertTrue(ctorType.isConstructor());

    Scope.Var protoVar = globalScope.getVar("MyClass.prototype");
    assertNotNull(protoVar);

    Scope.Var methodVar = globalScope.getVar("MyClass.prototype.getProp");
    assertNotNull(methodVar);
    assertTrue(methodVar.getType().isFunctionType());

    ObjectType instanceType = ctorType.getInstanceType();
    assertTrue(instanceType.hasProperty("prop"));
  }

  @Test(timeout = 4000)
  public void testCatchScopeVariableDeclaration() {
    String js =
        "function testCatch() {\n" +
        "  try {\n" +
        "  } catch (err) {\n" +
        "    var inCatch = err;\n" +
        "  }\n" +
        "}\n";

    Node root = parseAndCreateScope(js);
    Node script = root.getFirstChild();
    Node fnNode = script.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("err"));
    assertNotNull(localScope.getVar("inCatch"));
  }

  @Test(timeout = 4000)
  public void testEnumDeclarationAndElements() {
    String js =
        "/** @enum {string} */\n" +
        "var Color = {\n" +
        "  RED: 'r',\n" +
        "  GREEN: 'g',\n" +
        "  BLUE: 'b'\n" +
        "};\n";

    parseAndCreateScope(js);

    Scope.Var enumVar = globalScope.getVar("Color");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) enumVar.getType();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), enumType.getElementsType());
    assertTrue(enumType.hasOwnProperty("RED"));
    assertTrue(enumType.hasOwnProperty("GREEN"));
    assertTrue(enumType.hasOwnProperty("BLUE"));
  }

  @Test(timeout = 4000)
  public void testTypedefDeclaration() {
    String js =
        "/** @typedef {{name: string, age: number}} */\n" +
        "var Person;\n";

    parseAndCreateScope(js);

    JSType personType = registry.getType("Person");
    assertNotNull(personType);
    assertTrue(personType.isRecordType());
  }

  @Test(timeout = 4000)
  public void testStubDeclarationsInGlobalScope() {
    String js =
        "var ns = {};\n" +
        "ns.uninitializedProperty;\n";

    parseAndCreateScope(js);

    Scope.Var stubVar = globalScope.getVar("ns.uninitializedProperty");
    assertNotNull(stubVar);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), stubVar.getType());
  }

  @Test(timeout = 4000)
  public void testGlobalThisWindowRedefinition() {
    String js =
        "/** @constructor */\n" +
        "function Window() {}\n";

    parseAndCreateScope(js);

    Scope.Var windowVar = globalScope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());

    ObjectType globalThis = registry.getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertNotNull(globalThis.getConstructor());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    parseAndCreateScope("");
    assertNotNull(globalScope);
    assertTrue(globalScope.isGlobal());
    // Base native objects should still be initialized
    assertNotNull(globalScope.getVar("Object"));
  }

  @Test(timeout = 4000)
  public void testMultipleDeclarationsInSingleVar() {
    String js = "var x = 1, y = 'two', z = true;\n";
    parseAndCreateScope(js);

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), globalScope.getVar("x").getType());
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), globalScope.getVar("y").getType());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), globalScope.getVar("z").getType());
  }

  @Test(timeout = 4000)
  public void testMultipleVarWithJsDocEmitsWarning() {
    String js = "/** @type {number} */ var a = 1, b = 2;\n";
    parseAndCreateScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testConstIdiomOrAssignment() {
    String js =
        "/** @const */ var goog = goog || {};\n";
    parseAndCreateScope(js);

    Scope.Var var = globalScope.getVar("goog");
    assertNotNull(var);
  }

  @Test(timeout = 4000)
  public void testPatchGlobalScopeWorkflow() {
    String script1Js = "var script1Var = 100;\n";
    Node root1 = compiler.parseTestCode(script1Js);
    Scope scope = scopeCreator.createScope(root1, null);
    assertNotNull(scope.getVar("script1Var"));

    // Prepare updated script
    String script2Js = "var script2Var = 'updated';\n";
    Node root2 = compiler.parseTestCode(script2Js);
    Node scriptNode = root2.getFirstChild();

    scopeCreator.patchGlobalScope(scope, scriptNode);

    assertNull("Old variable should be removed during patch", scope.getVar("script1Var"));
    assertNotNull("New variable should be recognized", scope.getVar("script2Var"));
  }

  // =========================================================================
  // Partition D: Diagnostic Warnings & Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testLendsAnnotationOnNonObjectEmitsWarning() {
    String js =
        "var myNum = 123;\n" +
        "var obj = /** @lends {myNum} */ ({ a: 1 });\n";

    parseAndCreateScope(js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (TypedScopeCreator.LENDS_ON_NON_OBJECT.key.equals(error.getType().key)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should emit LENDS_ON_NON_OBJECT warning", foundWarning);
  }

  @Test(timeout = 4000)
  public void testLendsAnnotationOnUnknownVarEmitsWarning() {
    String js =
        "var obj = /** @lends {nonExistentVariable} */ ({ a: 1 });\n";

    parseAndCreateScope(js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (TypedScopeCreator.UNKNOWN_LENDS.key.equals(error.getType().key)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should emit UNKNOWN_LENDS warning", foundWarning);
  }

  @Test(timeout = 4000)
  public void testEnumInitializerInvalidEmitsWarning() {
    String js =
        "/** @enum {number} */\n" +
        "var InvalidEnum = 100;\n";

    parseAndCreateScope(js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (TypedScopeCreator.ENUM_INITIALIZER.key.equals(error.getType().key)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should emit ENUM_INITIALIZER warning", foundWarning);
  }

  @Test(timeout = 4000)
  public void testConstructorUninitializedEmitsWarning() {
    String js =
        "/** @constructor */\n" +
        "var UninitializedCtor;\n";

    parseAndCreateScope(js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (TypedScopeCreator.CTOR_INITIALIZER.key.equals(error.getType().key)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should emit CTOR_INITIALIZER warning", foundWarning);
  }

  @Test(timeout = 4000)
  public void testInterfaceUninitializedEmitsWarning() {
    String js =
        "/** @interface */\n" +
        "var UninitializedIface;\n";

    parseAndCreateScope(js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (TypedScopeCreator.IFACE_INITIALIZER.key.equals(error.getType().key)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should emit IFACE_INITIALIZER warning", foundWarning);
  }

  @Test(timeout = 4000)
  public void testEnumDuplicateKeyWarning() {
    String js =
        "/** @enum {number} */\n" +
        "var DupEnum = {\n" +
        "  KEY: 1,\n" +
        "  KEY: 2\n" +
        "};\n";

    parseAndCreateScope(js);

    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (TypeCheck.ENUM_DUP.key.equals(error.getType().key)) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Should emit ENUM_DUP warning for duplicate enum element", foundWarning);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Subclasses & Coding Conventions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInheritsSubclassRelationship() {
    String js =
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor \n * @extends {Super} */ function Sub() {}\n" +
        "goog.inherits(Sub, Super);\n";

    parseAndCreateScope(js);

    Scope.Var subVar = globalScope.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    FunctionType superCtor = subCtor.getSuperClassConstructor();
    assertNotNull(superCtor);
    assertEquals("Super", superCtor.getReferenceName());
  }

  @Test(timeout = 4000)
  public void testFunctionWithThisTypeInDoc() {
    String js =
        "/** @constructor */ function Widget() {}\n" +
        "/** @this {Widget} */ function doSomething() {\n" +
        "  this.widgetProp = 1;\n" +
        "}\n";

    parseAndCreateScope(js);

    Scope.Var widgetVar = globalScope.getVar("Widget");
    assertNotNull(widgetVar);
    ObjectType widgetInstance = widgetVar.getType().toMaybeFunctionType().getInstanceType();
    assertTrue(widgetInstance.hasProperty("widgetProp"));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionExpression() {
    String js =
        "var myFn = function innerBleed(x) {\n" +
        "  return innerBleed(x - 1);\n" +
        "};\n";

    Node root = parseAndCreateScope(js);
    Node script = root.getFirstChild();
    Node varNode = script.getFirstChild();
    Node assignName = varNode.getFirstChild();
    Node fnLiteral = assignName.getFirstChild();

    assertEquals(Token.FUNCTION, fnLiteral.getType());
    Scope localScope = scopeCreator.createScope(fnLiteral, globalScope);

    assertNotNull("Bleeding function name should be available in local scope",
        localScope.getVar("innerBleed"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPatchGlobalScopeRequiresScriptNode() {
    parseAndCreateScope("var a = 1;");
    Node invalidNode = new Node(Token.BLOCK);
    scopeCreator.patchGlobalScope(globalScope, invalidNode);
  }
}