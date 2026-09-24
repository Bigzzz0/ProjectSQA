/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target: com.google.javascript.jscomp.CollapseProperties
 * Defect Focus: Defects4J Ground Truth
 *   - testAliasedTopLevelEnum: Enum objects aliased locally fail to collapse properties if
 *     the type check does not recognize ENUM or subproperties appropriately.
 *   - testIssue389: Local aliasing of 'arguments' in a parent function must not be inlined
 *     into nested inner function scopes, where 'arguments' resolves to the inner function.
 *
 * Branch & Condition Coverage Matrix:
 * -----------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic & State Transitions
 *   - Flatten single-level & multi-level object literals (var a = {b: 1}, a.b.c = 2)
 *   - Replace '.' with '$' in property access chains
 *   - Function declarations with static properties (function a() {}; a.b = 1)
 *   - Elimination of fully-collapsed object literals from AST
 *   - Inline namespace local aliases (inlineAliases = true)
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Empty program / no-op passes
 *   - Properties with '$' in their names (verifying '$' -> '$0' escaping via appendPropForAlias)
 *   - Object literal keys that are numeric or non-JS identifiers (arbitrary name generation)
 *   - Twin reference handling during complex assignments (var b = a.c = 1)
 *   - Property accesses using bracket notation (a['b']) which must remain uncollapsed
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *   - testAliasedTopLevelEnum: aliased enum object literal retains collapsible descendant properties
 *   - testIssue389: alias of 'arguments' accessed inside nested function must not be inlined
 *
 * Partition D: Exception & Defensive Guard Paths / Warnings
 *   - Warning JSC_UNSAFE_THIS: 'this' used inside static method being collapsed
 *   - Constructor functions with 'this': safe, should NOT trigger JSC_UNSAFE_THIS
 *   - Warning JSC_NAMESPACE_REDEFINED: reassignment of an initialized namespace
 *   - Warning JSC_UNSAFE_NAMESPACE: global unsafe aliasing of a namespace object
 *   - Object literal getters/setters: ignored and preserved, not converted to vars
 *   - Extern type static properties collapsing on/off (collapsePropertiesOnExternTypes)
 *
 * Partition E: Object Lifecycle & Pass Options
 *   - Pass execution with inlineAliases = false
 *   - Pass execution with collapsePropertiesOnExternTypes = true vs false
 *   - Constructor and lifecycle stability
 * =========================================================================================
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

public class CollapsePropertiesGeminiTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window; function alert(s) {} function parseInt(s) {}\n"
      + "/** @constructor */ function String() {}; String.fromCharCode = function () {};\n"
      + "/** @constructor */ function Object() {}\n"
      + "var arguments;\n";

  private boolean collapsePropertiesOnExternTypes = false;
  private boolean inlineAliases = true;

  public CollapsePropertiesGeminiTest() {
    super(EXTERNS);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseProperties(
        compiler, collapsePropertiesOnExternTypes, inlineAliases);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableNormalize();
    collapsePropertiesOnExternTypes = false;
    inlineAliases = true;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defect 1: testAliasedTopLevelEnum
   * Aliasing a top-level enum in a local scope should inline the alias and
   * correctly collapse the enum properties (e.g. Foo$A, Foo$B).
   */
  @Test(timeout = 4000)
  public void testAliasedTopLevelEnum() {
    test(
        "/** @enum {number} */ var Foo = {A: 1, B: 2};\n"
        + "function f() {\n"
        + "  var alias = Foo;\n"
        + "  return alias.A;\n"
        + "}",
        "var Foo$A = 1;\n"
        + "var Foo$B = 2;\n"
        + "function f() {\n"
        + "  var alias = null;\n"
        + "  return Foo$A;\n"
        + "}");
  }

  /**
   * Targets Defect 2: testIssue389
   * 'arguments' alias should NOT be inlined into an inner function where
   * 'arguments' re-binds to the inner scope.
   */
  @Test(timeout = 4000)
  public void testIssue389() {
    testSame(
        "function f() {\n"
        + "  var args = arguments;\n"
        + "  function g() {\n"
        + "    return args[0];\n"
        + "  }\n"
        + "}");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCollapseSimpleObjectLiteral() {
    test(
        "var a = {b: 1};",
        "var a$b = 1;");
  }

  @Test(timeout = 4000)
  public void testCollapseNestedObjectLiteral() {
    test(
        "var a = {b: {c: 1}};",
        "var a$b$c = 1;");
  }

  @Test(timeout = 4000)
  public void testCollapsePropertyAssignments() {
    test(
        "var a = {}; a.b = 1; a.c = 2;",
        "var a$b = 1; var a$c = 2;");
  }

  @Test(timeout = 4000)
  public void testCollapseFunctionWithStaticProperties() {
    test(
        "function a() {} a.b = 1;",
        "function a() {} var a$b = 1;");
  }

  @Test(timeout = 4000)
  public void testLocalAliasInliningGlobalObject() {
    test(
        "var a = {b: 1};\n"
        + "function f() {\n"
        + "  var x = a;\n"
        + "  return x.b;\n"
        + "}",
        "var a$b = 1;\n"
        + "function f() {\n"
        + "  var x = null;\n"
        + "  return a$b;\n"
        + "}");
  }

  @Test(timeout = 4000)
  public void testLateAddedPropertyStubsInLocalScope() {
    test(
        "var a = {};\n"
        + "function f() {\n"
        + "  a.b = 1;\n"
        + "}",
        "var a = {};\n"
        + "var a$b;\n"
        + "function f() {\n"
        + "  a$b = 1;\n"
        + "}");
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationWithLatePropertyStubs() {
    test(
        "function a() {}\n"
        + "function f() {\n"
        + "  a.b = 1;\n"
        + "}",
        "function a() {}\n"
        + "var a$b;\n"
        + "function f() {\n"
        + "  a$b = 1;\n"
        + "}");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    testSame("");
  }

  @Test(timeout = 4000)
  public void testDollarSignEscapingInPropertyName() {
    test(
        "var a = {'b$c': 1};",
        "var a$b$0c = 1;");
  }

  @Test(timeout = 4000)
  public void testNonIdentifierKeyInObjectLiteral() {
    test(
        "var a = {'not an ident': 1};",
        "var a$1 = 1;");
  }

  @Test(timeout = 4000)
  public void testNumericKeyInObjectLiteral() {
    test(
        "var a = {1: 'numericKey'};",
        "var a$1 = 'numericKey';");
  }

  @Test(timeout = 4000)
  public void testBracketPropertyAccessIgnored() {
    testSame("var a = {}; a['b'] = 1;");
  }

  @Test(timeout = 4000)
  public void testComplexAssignmentTwinReference() {
    test(
        "var a = {}; var b = a.c = 1;",
        "var a$c; var b = a$c = 1;");
  }

  @Test(timeout = 4000)
  public void testLocalAliasAssignedMultipleTimesNotCollapsed() {
    testSame(
        "var a = {b: 1};\n"
        + "function f() {\n"
        + "  var x = a;\n"
        + "  x = 2;\n"
        + "  return x.b;\n"
        + "}");
  }

  @Test(timeout = 4000)
  public void testGetterSetterNotCollapsed() {
    testSame("var a = { get b() { return 1; }, set b(val) {} };");
  }

  // =========================================================================
  // Partition D: Defensive Guards, Warnings & JSError Diagnostics
  // =========================================================================

  @Test(timeout = 4000)
  public void testWarningUnsafeThisInStaticMethod() {
    test(
        "var a = {}; a.b = function() { return this.x; };",
        "var a$b = function() { return this.x; };",
        CollapseProperties.UNSAFE_THIS);
  }

  @Test(timeout = 4000)
  public void testNoWarningForThisInConstructor() {
    test(
        "var a = {};\n"
        + "/** @constructor */ a.b = function() { this.x = 1; };",
        "/** @constructor */ var a$b = function() { this.x = 1; };");
  }

  @Test(timeout = 4000)
  public void testWarningNamespaceRedefined() {
    testSame(
        "var a = {}; a.b = 1; a = {};",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  @Test(timeout = 4000)
  public void testWarningUnsafeNamespaceAliasing() {
    testSame(
        "var a = {}; a.b = 1; var c = a;",
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  // =========================================================================
  // Partition E: Options & Pass Lifecycle
  // =========================================================================

  @Test(timeout = 4000)
  public void testCollapsePropertiesOnExternTypesEnabled() {
    collapsePropertiesOnExternTypes = true;
    test(
        "String.foo = 1; var x = String.foo;",
        "var String$foo = 1; var x = String$foo;");
  }

  @Test(timeout = 4000)
  public void testCollapsePropertiesOnExternTypesDisabled() {
    collapsePropertiesOnExternTypes = false;
    testSame("String.foo = 1; var x = String.foo;");
  }

  @Test(timeout = 4000)
  public void testInlineAliasesDisabledOption() {
    inlineAliases = false;
    testSame(
        "var a = {b: 1};\n"
        + "function f() {\n"
        + "  var x = a;\n"
        + "  return x.b;\n"
        + "}");
  }

  @Test(timeout = 4000)
  public void testCollapsePropertiesInstantiable() {
    Compiler compiler = new Compiler();
    CollapseProperties pass = new CollapseProperties(compiler, false, true);
    assertNotNull(pass);
  }
}