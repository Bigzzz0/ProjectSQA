/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.CollapseProperties
 * Defects4J Defect Focus: Function aliasing and uncollapsible child properties
 * (Closure-89 / CollapsePropertiesTest failure set):
 *   - testAddPropertyToChildOfUncollapsibleFunctionInLocalScope
 *   - testAliasCreatedForFunctionDepth1_1
 *   - testAliasCreatedForFunctionDepth1_2
 *   - testAliasCreatedForFunctionDepth1_3
 *   - testAddPropertyToUncollapsibleNamedCtorInLocalScopeDepth1
 *   - testAddPropertyToUncollapsibleFunctionInLocalScopeDepth1
 *   - testAddPropertyToUncollapsibleFunctionInLocalScopeDepth2
 *   - testAliasCreatedForFunctionDepth2
 *
 * Coverage Target Branches:
 *   1. inlineAliases:
 *      - Global sets == 1, local sets == 0, aliasing gets > 0 (satisfied vs unsatisfied)
 *      - Local alias inlining (well-formed vs non-inlinable VAR assignment)
 *   2. checkNamespaces:
 *      - name.isNamespace() with aliasingGets > 0 -> UNSAFE_NAMESPACE_WARNING
 *      - name.isNamespace() redefined -> NAMESPACE_REDEFINED_WARNING
 *   3. checkForHosedThisReferences:
 *      - Non-constructor, no @this static method using 'this' -> UNSAFE_THIS warning
 *      - With @constructor or @this -> no warning
 *   4. flattenReferencesTo:
 *      - Twin references handling (complex assignment vs simple set)
 *      - Object literal key exclusion
 *   5. flattenNameRefAtDepth & flattenPrefixes:
 *      - Depth == 1 vs depth > 1 property chain resolution
 *      - GETPROP chain navigation
 *   6. appendPropForAlias:
 *      - Property containing '$' -> escape to '$0'
 *      - Normal property concatenation
 *   7. declareVarsForObjLitValues:
 *      - Number / Non-JS identifier keys -> arbitrary name generation
 *      - shouldKeepKeys() true vs false
 *      - IS_CONSTANT_NAME propagation
 *   8. addStubsForUndeclaredProperties:
 *      - Late local initialization -> stub insertion
 *   9. collapseDeclarationOfNameAndDescendants:
 *      - canCollapse() true vs false
 *      - canCollapseUnannotatedChildNames() true vs false
 *  10. collapsePropertiesOnExternTypes:
 *      - true (collapsing extern static properties) vs false (preserving externs)
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

public class CollapsePropertiesGeminiTest {

  // =========================================================================
  // Test Harness Helpers
  // =========================================================================

  private static String toNormalizedSource(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    return compiler.toSource(root);
  }

  private void test(String js, String expected) {
    test(js, expected, false, true, null);
  }

  private void test(String js, String expected, DiagnosticType warning) {
    test(js, expected, false, true, warning);
  }

  private void test(String js, String expected, boolean collapseExterns,
                    boolean inlineAliases, DiagnosticType warning) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    assertNotNull("Failed to parse test js: " + js, root);
    Node externsRoot = compiler.parseTestCode("");
    assertNotNull("Failed to parse dummy externs", externsRoot);

    CollapseProperties pass = new CollapseProperties(compiler, collapseExterns, inlineAliases);
    pass.process(externsRoot, root);

    if (warning != null) {
      assertEquals("Expected 1 warning", 1, compiler.getErrorManager().getWarningCount());
      assertEquals(warning, compiler.getErrorManager().getWarnings()[0].getType());
    } else {
      assertEquals("Expected 0 warnings", 0, compiler.getErrorManager().getWarningCount());
    }
    assertEquals("Expected 0 errors", 0, compiler.getErrorManager().getErrorCount());

    String actualSource = compiler.toSource(root);
    String expectedSource = toNormalizedSource(expected);
    assertEquals(expectedSource, actualSource);
  }

  private void testWithExterns(String externs, String js, String expected, boolean collapseExterns) {
    Compiler compiler = new Compiler();
    Node externsRoot = compiler.parseTestCode(externs);
    Node root = compiler.parseTestCode(js);

    CollapseProperties pass = new CollapseProperties(compiler, collapseExterns, true);
    pass.process(externsRoot, root);

    assertEquals(0, compiler.getErrorManager().getErrorCount());
    String actualSource = compiler.toSource(root);
    String expectedSource = toNormalizedSource(expected);
    assertEquals(expectedSource, actualSource);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleObjectPropertyCollapse() {
    test("var a = {}; a.b = 1; var c = a.b;",
         "var a$b = 1; var c = a$b;");
  }

  @Test(timeout = 4000)
  public void testNestedObjectCollapse() {
    test("var a = { b: { c: 1 } }; var d = a.b.c;",
         "var a$b$c = 1; var d = a$b$c;");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyDiscardingWhenSafe() {
    test("var a = { b: 1, c: 2 };",
         "var a$b = 1; var a$c = 2;");
  }

  @Test(timeout = 4000)
  public void testPropertyContainingDollarSignEscaped() {
    // Tests appendPropForAlias: '$' must be escaped to '$0'
    test("var a = {}; a.$b = 1; var c = a.$b;",
         "var a$$0b = 1; var c = a$$0b;");
  }

  @Test(timeout = 4000)
  public void testPropertyWithMultipleDollarSignsEscaped() {
    test("var a = {}; a.$b$c = 2; var d = a.$b$c;",
         "var a$$0b$0c = 2; var d = a$$0b$0c;");
  }

  @Test(timeout = 4000)
  public void testLocalAliasInlining() {
    test("var a = {}; a.b = { c: 1 }; function f() { var x = a.b; return x.c; }",
         "var a$b$c = 1; function f() { var x = null; return a$b$c; }");
  }

  @Test(timeout = 4000)
  public void testGlobalStubCreatedForLateLocalPropertyAddition() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a$b; function f() { a$b = 1; }");
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationPropertiesStubbed() {
    test("function a() {} function f() { a.b = 1; }",
         "function a() {} var a$b; function f() { a$b = 1; }");
  }

  @Test(timeout = 4000)
  public void testComplexAssignmentWithTwinReference() {
    test("var a = {}; var b; b = (a.c = 1);",
         "var a$c; var b; b = (a$c = 1);");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    test("", "");
  }

  @Test(timeout = 4000)
  public void testPrimitiveVariablesOnly() {
    test("var a = 1; var b = 'text';",
         "var a = 1; var b = 'text';");
  }

  @Test(timeout = 4000)
  public void testEmptyObjectLiteralElimination() {
    test("var a = {};", "");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralNumericKeys() {
    // Non-JS identifier keys trigger arbitrary name generation
    test("var a = {1: 'num', 2: 'two'};",
         "var a$1 = 'num'; var a$2 = 'two';");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralNonIdentifierStringKeys() {
    test("var a = {'hello world': 42};",
         "var a$1 = 42;");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralDuplicateKeys() {
    test("var a = { b: 1, b: 2 };",
         "var a$b = 1; var a$b = 2;");
  }

  @Test(timeout = 4000)
  public void testDeepPropertyHierarchyDepth4() {
    test("var a = {b: {c: {d: 10}}}; var e = a.b.c.d;",
         "var a$b$c$d = 10; var e = a$b$c$d;");
  }

  @Test(timeout = 4000)
  public void testExternPropertiesCollapsingDisabled() {
    testWithExterns("function String() {}",
                    "String.foo = 1; var x = String.foo;",
                    "String.foo = 1; var x = String.foo;",
                    false);
  }

  @Test(timeout = 4000)
  public void testExternPropertiesCollapsingEnabled() {
    testWithExterns("function String() {}",
                    "String.foo = 1; var x = String.foo;",
                    "var String$foo = 1; var x = String$foo;",
                    true);
  }

  @Test(timeout = 4000)
  public void testBracketAccessPreventsCollapsing() {
    test("var a = {}; a['b'] = 1; var c = a['b'];",
         "var a = {}; a['b'] = 1; var c = a['b'];");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // These directly test uncollapsible function child properties and aliasing
  // =========================================================================

  @Test(timeout = 4000)
  public void testAliasCreatedForFunctionDepth1_1() {
    // Ground Truth: Aliased function must prevent collapsing properties of the function
    test("var a = function(){}; var b = a; a.c = 1;",
         "var a = function(){}; var b = a; a.c = 1;");
  }

  @Test(timeout = 4000)
  public void testAliasCreatedForFunctionDepth1_2() {
    test("var a = function(){}; var b = a; a.c = 1; a.c.d = 2;",
         "var a = function(){}; var b = a; a.c = 1; a.c.d = 2;");
  }

  @Test(timeout = 4000)
  public void testAliasCreatedForFunctionDepth1_3() {
    test("var a = function(){}; var b = a; a.c = function(){}; a.c.d = 2;",
         "var a = function(){}; var b = a; a.c = function(){}; a.c.d = 2;");
  }

  @Test(timeout = 4000)
  public void testAliasCreatedForFunctionDepth2() {
    test("var a = {}; a.b = function(){}; var c = a.b; a.b.d = 1;",
         "var a$b = function(){}; var c = a$b; a$b.d = 1;");
  }

  @Test(timeout = 4000)
  public void testAddPropertyToUncollapsibleFunctionInLocalScopeDepth1() {
    test("function a() {} var d = a; (function() { a.b = 0; })();",
         "function a() {} var d = a; (function() { a.b = 0; })();");
  }

  @Test(timeout = 4000)
  public void testAddPropertyToUncollapsibleFunctionInLocalScopeDepth2() {
    test("function a() {} a.b = function() {}; var d = a.b; (function() { a.b.c = 0; })();",
         "function a() {} var a$b = function() {}; var d = a$b; (function() { a$b.c = 0; })();");
  }

  @Test(timeout = 4000)
  public void testAddPropertyToUncollapsibleNamedCtorInLocalScopeDepth1() {
    test("function a() {} /** @constructor */ a.b = function() {}; var d = a; (function() { a.b.c = 0; })();",
         "function a() {} /** @constructor */ a.b = function() {}; var d = a; (function() { a.b.c = 0; })();");
  }

  @Test(timeout = 4000)
  public void testAddPropertyToChildOfUncollapsibleFunctionInLocalScope() {
    test("function a() {} a.b = {c: 1}; var d = a; (function() { a.b.d = 2; })();",
         "function a() {} var a$b$c = 1; var a$b = {c: a$b$c}; var d = a; (function() { a$b.d = 2; })();");
  }

  // =========================================================================
  // Partition D: Diagnostics & Warnings (Unsafe Namespaces, This, Redefinition)
  // =========================================================================

  @Test(timeout = 4000)
  public void testWarningUnsafeThisInStaticMethod() {
    test("var a = {}; a.b = function() { return this.x; };",
         "var a$b = function() { return this.x; };",
         CollapseProperties.UNSAFE_THIS);
  }

  @Test(timeout = 4000)
  public void testNoWarningForThisWhenConstructorAnnotated() {
    test("var a = {}; /** @constructor */ a.b = function() { this.x = 1; };",
         "var a$b = function() { this.x = 1; };",
         null);
  }

  @Test(timeout = 4000)
  public void testNoWarningForThisWhenThisAnnotated() {
    test("var a = {}; /** @this {Object} */ a.b = function() { this.x = 1; };",
         "var a$b = function() { this.x = 1; };",
         null);
  }

  @Test(timeout = 4000)
  public void testWarningUnsafeNamespaceAliasing() {
    test("var a = {}; /** @constructor */ a.b = function() {}; var c = a; var d = new a.b();",
         "var a = {}; var a$b = function() {}; var c = a; var d = new a$b();",
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  @Test(timeout = 4000)
  public void testWarningNamespaceRedefined() {
    test("var a = {}; /** @constructor */ a.b = function() {}; a = {};",
         "var a = {}; var a$b = function() {}; a = {};",
         CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // =========================================================================
  // Partition E: Constant Name Propagation & Object Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstantPropertyPreservedOnObjectLit() {
    test("var a = { /** @const */ B: 1 }; var c = a.B;",
         "var a$B = 1; var c = a$B;");
  }

  @Test(timeout = 4000)
  public void testObjectReassignedMultipleTimesNotCollapsed() {
    test("var a = {}; a = {}; a.b = 1;",
         "var a = {}; a = {}; a.b = 1;");
  }

  @Test(timeout = 4000)
  public void testConstructorInstancePassInstantiation() {
    Compiler compiler = new Compiler();
    CollapseProperties pass1 = new CollapseProperties(compiler, true, true);
    assertNotNull("CollapseProperties should instantiate successfully", pass1);

    CollapseProperties pass2 = new CollapseProperties(compiler, false, false);
    assertNotNull("CollapseProperties should instantiate with false flags", pass2);
  }
}