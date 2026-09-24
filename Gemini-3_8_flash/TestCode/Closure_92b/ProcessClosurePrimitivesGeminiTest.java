package com.google.javascript.jscomp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------
 * Targeted Class: com.google.javascript.jscomp.ProcessClosurePrimitives
 * Key Branches & Decision Conditions Tested:
 * 1. Base Class Super Calls (processBaseClassCall, getEnclosingDeclNameNode, reportBadBaseClassUse):
 *    - Token.GETPROP on 'goog.base' not in CALL or ASSIGN -> BASE_CLASS_ERROR
 *    - Argument 0 != 'this' (null or non-this) -> BASE_CLASS_ERROR
 *    - Missing enclosing function / anonymous scope -> BASE_CLASS_ERROR
 *    - Constructor call without goog.inherits or invalid inherits -> BASE_CLASS_ERROR
 *    - Constructor call valid rewrite -> <BaseClass>.call(this, ...)
 *    - Prototype method call without methodName string / wrong methodName -> BASE_CLASS_ERROR
 *    - Prototype method call valid rewrite -> <Class>.superClass_.<method>.call(this, ...)
 * 2. Closure Provide Processing (processProvideCall, verifyProvide, registerAnyProvidedPrefixes, ProvidedName.replace):
 *    - Single and multi-level namespaces (e.g., 'a', 'a.b', 'a.b.c')
 *    - Duplicate goog.provide() -> DUPLICATE_NAMESPACE_ERROR
 *    - Invalid identifier / property syntax in namespace -> INVALID_PROVIDE_ERROR
 *    - Function declaration collisions in global scope -> FUNCTION_NAMESPACE_ERROR
 *    - Candidate provide definition handling (VAR, ASSIGN, duplicate definitions)
 * 3. Closure Require Processing (processRequireCall, verifyArgument):
 *    - Missing provide when required -> MISSING_PROVIDE_ERROR
 *    - Out-of-order require before provide -> LATE_PROVIDE_ERROR
 *    - Missing / non-string / too many arguments -> NULL_ARGUMENT_ERROR, INVALID_ARGUMENT_ERROR, TOO_MANY_ARGUMENTS_ERROR
 *    - CheckLevel.OFF toggling -> unremoved / suppressed requires
 *    - Cross-module require with and without module dependency -> XMODULE_REQUIRE_ERROR
 * 4. CSS Name Mapping (processSetCssNameMapping):
 *    - Valid object literal map -> translates to CssRenamingMap and detaches node
 *    - Non-object literal argument -> INVALID_ARGUMENT_ERROR
 *    - Non-string keys or non-string values -> NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR
 * 5. Simplification of new Date(goog.now()) (trySimplifyNewDate):
 *    - Valid new Date(goog.now()) rewrite when rewriteNewDateGoogNow is enabled
 *    - Bypass when rewriteNewDateGoogNow is disabled or parameters do not strictly match
 * 6. Symbol Export & Dependency Registration (exportSymbol, addDependency):
 *    - Export variable recording (getExportedVariableNames) for simple and dotted names
 *    - goog.addDependency call replacement with numeric 0 placeholder
 * 7. DEFECT TARGETING:
 *    - testProvideInIndependentModules4: Multi-module bush dependency where nested implicit namespaces
 *      are moved to a common root module (Defects4J ground truth failure).
 */
public class ProcessClosurePrimitivesGeminiTest extends CompilerTestCase {

  private CheckLevel checkLevel;
  private boolean rewriteNewDateGoogNow;
  private ProcessClosurePrimitives lastProcessor;

  public ProcessClosurePrimitivesGeminiTest() {
    this.checkLevel = CheckLevel.ERROR;
    this.rewriteNewDateGoogNow = true;
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.checkLevel = CheckLevel.ERROR;
    this.rewriteNewDateGoogNow = true;
  }

  @After
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    lastProcessor = new ProcessClosurePrimitives(compiler, checkLevel, rewriteNewDateGoogNow);
    return lastProcessor;
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleProvideAndRequire() {
    test("goog.provide('foo'); goog.require('foo');",
         "var foo = {};");
  }

  @Test(timeout = 4000)
  public void testDottedProvideHierarchy() {
    test("goog.provide('a.b.c');",
         "var a = {}; a.b = {}; a.b.c = {};");
  }

  @Test(timeout = 4000)
  public void testMultipleProvidesSharingPrefix() {
    test("goog.provide('a.b'); goog.provide('a.c');",
         "var a = {}; a.b = {}; a.c = {};");
  }

  @Test(timeout = 4000)
  public void testProvideWithVariableDefinition() {
    test("goog.provide('foo'); var foo = 123;",
         "var foo = 123;");
  }

  @Test(timeout = 4000)
  public void testProvideWithAssignmentDefinition() {
    test("goog.provide('foo'); foo = 123;",
         "var foo = 123;");
  }

  @Test(timeout = 4000)
  public void testDottedProvideWithAssignmentDefinition() {
    test("goog.provide('foo.bar'); foo.bar = function() {};",
         "var foo = {}; foo.bar = function() {};");
  }

  @Test(timeout = 4000)
  public void testProvideWithConstantNaming() {
    test("goog.provide('FOO_CONSTANT');",
         "var FOO_CONSTANT = {};");
  }

  @Test(timeout = 4000)
  public void testBaseClassConstructorCall() {
    test("function Base() {}" +
         "function Sub() { goog.base(this); }" +
         "goog.inherits(Sub, Base);",
         "function Base() {}" +
         "function Sub() { Base.call(this); }" +
         "goog.inherits(Sub, Base);");
  }

  @Test(timeout = 4000)
  public void testBaseClassConstructorCallWithArguments() {
    test("function Base(x, y) {}" +
         "function Sub(x, y) { goog.base(this, x, y); }" +
         "goog.inherits(Sub, Base);",
         "function Base(x, y) {}" +
         "function Sub(x, y) { Base.call(this, x, y); }" +
         "goog.inherits(Sub, Base);");
  }

  @Test(timeout = 4000)
  public void testBaseClassMethodCall() {
    test("function Base() {}" +
         "function Sub() {}" +
         "goog.inherits(Sub, Base);" +
         "Sub.prototype.foo = function(a) { goog.base(this, 'foo', a); };",
         "function Base() {}" +
         "function Sub() {}" +
         "goog.inherits(Sub, Base);" +
         "Sub.prototype.foo = function(a) { Sub.superClass_.foo.call(this, a); };");
  }

  @Test(timeout = 4000)
  public void testAddDependencyCall() {
    test("goog.addDependency('path/file.js', ['ns1'], ['ns2']);",
         "0;");
  }

  @Test(timeout = 4000)
  public void testExportSymbolRecording() {
    test("goog.exportSymbol('myExport', 1); goog.exportSymbol('ns.sub.Export', 2);",
         "goog.exportSymbol('myExport', 1); goog.exportSymbol('ns.sub.Export', 2);");
    assertNotNull(lastProcessor);
    Set<String> exported = lastProcessor.getExportedVariableNames();
    assertTrue(exported.contains("myExport"));
    assertTrue(exported.contains("ns"));
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingValid() {
    test("goog.setCssNameMapping({'active': 'act', 'hidden': 'hid'});",
         "");
    assertNotNull(getLastCompiler().getCssRenamingMap());
    assertEquals("act", getLastCompiler().getCssRenamingMap().get("active"));
    assertEquals("hid", getLastCompiler().getCssRenamingMap().get("hidden"));
    assertEquals("fallback", getLastCompiler().getCssRenamingMap().get("fallback"));
  }

  @Test(timeout = 4000)
  public void testSimplifyNewDateGoogNow() {
    test("var d = new Date(goog.now());",
         "var d = new Date();");
  }

  @Test(timeout = 4000)
  public void testSimplifyNewDateDisabled() {
    this.rewriteNewDateGoogNow = false;
    test("var d = new Date(goog.now());",
         "var d = new Date(goog.now());");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testProvideNullArgument() {
    testError("goog.provide();",
        ProcessClosurePrimitives.NULL_ARGUMENT_ERROR);
  }

  @Test(timeout = 4000)
  public void testProvideNonStringArgument() {
    testError("goog.provide(42);",
        ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }

  @Test(timeout = 4000)
  public void testProvideTooManyArguments() {
    testError("goog.provide('a', 'b');",
        ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR);
  }

  @Test(timeout = 4000)
  public void testRequireNullArgument() {
    testError("goog.require();",
        ProcessClosurePrimitives.NULL_ARGUMENT_ERROR);
  }

  @Test(timeout = 4000)
  public void testRequireNonStringArgument() {
    testError("goog.require(false);",
        ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }

  @Test(timeout = 4000)
  public void testRequireTooManyArguments() {
    testError("goog.require('a', 'b');",
        ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR);
  }

  @Test(timeout = 4000)
  public void testProvideEmptyStringNamespace() {
    testError("goog.provide('');",
        ProcessClosurePrimitives.INVALID_PROVIDE_ERROR);
  }

  @Test(timeout = 4000)
  public void testProvideInvalidIdentifierTokens() {
    testError("goog.provide('bad-name');",
        ProcessClosurePrimitives.INVALID_PROVIDE_ERROR);
    testError("goog.provide('bad..name');",
        ProcessClosurePrimitives.INVALID_PROVIDE_ERROR);
    testError("goog.provide('123number');",
        ProcessClosurePrimitives.INVALID_PROVIDE_ERROR);
  }

  @Test(timeout = 4000)
  public void testSimplifyNewDateNonMatchingArguments() {
    testSame("var d = new Date(12345);");
    testSame("var d = new Date(goog.now(), 1);");
    testSame("var d = new Other(goog.now());");
    testSame("var d = new Date(other.now());");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testProvideInIndependentModules1() {
    test(createModuleStar(
             "",
             "goog.provide('apps.A');",
             "goog.provide('apps.B');"),
         new String[] {
             "var apps = {};",
             "apps.A = {};",
             "apps.B = {};"
         });
  }

  @Test(timeout = 4000)
  public void testProvideInIndependentModules2() {
    test(createModuleStar(
             "goog.provide('apps');",
             "goog.provide('apps.A');",
             "goog.provide('apps.B');"),
         new String[] {
             "var apps = {};",
             "apps.A = {};",
             "apps.B = {};"
         });
  }

  @Test(timeout = 4000)
  public void testProvideInIndependentModules3() {
    test(createModuleStar(
             "goog.provide('a.b');",
             "goog.provide('a.b.c');",
             "goog.provide('a.b.d');"),
         new String[] {
             "var a = {}; a.b = {};",
             "a.b.c = {};",
             "a.b.d = {};"
         });
  }

  /**
   * Ground Truth Defect Test from Defects4J:
   * targets com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules4
   * Implicit namespaces with multiple dot-separated prefixes across a module bush graph
   * must be hoisted to common dependency root module correctly.
   */
  @Test(timeout = 4000)
  public void testProvideInIndependentModules4() {
    JSModule[] modules = createModuleBush(
        "",
        "goog.provide('apps.submodule.B');",
        "goog.provide('apps.submodule.C');",
        "goog.provide('apps.submodule.A');");
    test(modules,
         new String[] {
            "var apps = {}; apps.submodule = {};",
            "apps.submodule.B = {};",
            "apps.submodule.C = {};",
            "apps.submodule.A = {};"
         });
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testDuplicateProvideError() {
    testError("goog.provide('foo'); goog.provide('foo');",
        ProcessClosurePrimitives.DUPLICATE_NAMESPACE_ERROR);
  }

  @Test(timeout = 4000)
  public void testFunctionNamespaceCollision() {
    testError("goog.provide('foo'); function foo() {}",
        ProcessClosurePrimitives.FUNCTION_NAMESPACE_ERROR);
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionNoCollision() {
    test("goog.provide('foo'); var f = function foo() {};",
         "var foo = {}; var f = function foo() {};");
  }

  @Test(timeout = 4000)
  public void testInnerFunctionNoCollision() {
    test("goog.provide('foo'); function outer() { function foo() {} }",
         "var foo = {}; function outer() { function foo() {} }");
  }

  @Test(timeout = 4000)
  public void testMissingProvideError() {
    testError("goog.require('missing.namespace');",
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }

  @Test(timeout = 4000)
  public void testLateProvideError() {
    testError("goog.require('foo'); goog.provide('foo');",
        ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

  @Test(timeout = 4000)
  public void testRequireTurnedOffSuppressesMissingError() {
    this.checkLevel = CheckLevel.OFF;
    testSame("goog.require('unprovided.name');");
  }

  @Test(timeout = 4000)
  public void testRequireTurnedOffStillRemovesProvided() {
    this.checkLevel = CheckLevel.OFF;
    test("goog.provide('foo'); goog.require('foo');",
         "var foo = {};");
  }

  @Test(timeout = 4000)
  public void testCrossModuleRequireError() {
    JSModule[] modules = createModules(
        "goog.provide('mod0');",
        "goog.require('mod0');");
    test(modules, new String[] { "var mod0 = {};", "" },
        ProcessClosurePrimitives.XMODULE_REQUIRE_ERROR);
  }

  @Test(timeout = 4000)
  public void testCrossModuleRequireSuccessWithDependency() {
    JSModule[] modules = createModules(
        "goog.provide('mod0');",
        "goog.require('mod0');");
    modules[1].addDependency(modules[0]);
    test(modules, new String[] { "var mod0 = {};", "" });
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingNonObjectLit() {
    testError("goog.setCssNameMapping('notAnObjectLiteral');",
        ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingNonStringValue() {
    testError("goog.setCssNameMapping({'a': 123});",
        ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingNonStringKey() {
    testError("goog.setCssNameMapping({123: 'val'});",
        ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

  @Test(timeout = 4000)
  public void testBaseClassUseNotDirectCall() {
    testError("var x = goog.base;",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
    testError("goog.base;",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test(timeout = 4000)
  public void testBaseClassUseFirstArgNotThis() {
    testError("function Foo() { goog.base(); }",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
    testError("function Foo() { goog.base('notThis'); }",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test(timeout = 4000)
  public void testBaseClassUseNoEnclosingMethod() {
    testError("goog.base(this);",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
    testError("(function() { goog.base(this); })();",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test(timeout = 4000)
  public void testBaseClassConstructorMissingInherits() {
    testError("function Foo() { goog.base(this); }",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
    testError("function Foo() { goog.base(this); } goog.inherits(Foo, getBase());",
        ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test(timeout = 4000)
  public void testBaseClassMethodMissingMethodName() {
    testError("function Base() {} function Sub() {} goog.inherits(Sub, Base);" +
              "Sub.prototype.foo = function() { goog.base(this); };",
              ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test(timeout = 4000)
  public void testBaseClassMethodNonStringMethodName() {
    testError("function Base() {} function Sub() {} goog.inherits(Sub, Base);" +
              "Sub.prototype.foo = function() { goog.base(this, 123); };",
              ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test(timeout = 4000)
  public void testBaseClassMethodMismatchingMethodName() {
    testError("function Base() {} function Sub() {} goog.inherits(Sub, Base);" +
              "Sub.prototype.foo = function() { goog.base(this, 'bar'); };",
              ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Structural Integrities
  // =========================================================================

  @Test(timeout = 4000)
  public void testNonExprCallIgnored() {
    testSame("var x = goog.provide('foo');");
    testSame("var x = goog.require('foo');");
  }

  @Test(timeout = 4000)
  public void testBaseClassConstructorVariations() {
    test("var Foo = function() { goog.base(this); };" +
         "goog.inherits(Foo, Base);",
         "var Foo = function() { Base.call(this); };" +
         "goog.inherits(Foo, Base);");

    test("my.pack.Foo = function() { goog.base(this); };" +
         "goog.inherits(my.pack.Foo, Base);",
         "my.pack.Foo = function() { Base.call(this); };" +
         "goog.inherits(my.pack.Foo, Base);");
  }
}