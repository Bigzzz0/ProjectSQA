package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.CollapseProperties
 * Known Defect (Defects4J): CollapsePropertiesTest::testIssue931 (AssertionFailedError)
 *
 * Core Decision Logic & Branches Targeted:
 * 1. process(Node, Node):
 *    - collapsePropertiesOnExternTypes (true / false)
 *    - inlineAliases (true / false)
 * 2. inlineAliases(GlobalNamespace):
 *    - Getter/setter properties skipped (Name.Type.GET / SET)
 *    - Inlining local aliases of global namespaces meeting conditions (a), (b), (c)
 *    - Re-scanning namespace after alias replacement
 * 3. checkNamespaces():
 *    - Warning JSC_UNSAFE_NAMESPACE on aliasing of namespace
 *    - Warning JSC_NAMESPACE_REDEFINED on redefinition or deletion (DELETE_PROP) of namespace
 * 4. flattenReferencesToCollapsibleDescendantNames(Name, String):
 *    - canCollapse() vs. isSimpleStubDeclaration()
 *    - Recursing on collapsible descendant subnames
 *    - [DEFECT ZONE Issue 931]: Simple stub declarations (e.g. `a.b;`) child prefixes
 *      must not leave child properties uncollapsed or desynchronized from the stub.
 * 5. flattenReferencesTo(Name, String) & flattenPrefixes():
 *    - Object literal keys skipped
 *    - Twin references handled (complex assignments e.g. `(x.y = 3)`)
 *    - GETPROP chains flattening at given depth
 *    - Free call marking (Node.FREE_CALL) on call targets
 * 6. collapseDeclarationOfNameAndDescendants(Name, String):
 *    - Object literal / function declarations updated
 *    - Simple declarations (ASSIGN) updated to VAR
 *    - Unrolled nested object literals
 * 7. updateObjLitOrFunctionDeclaration:
 *    - Assignment at ASSIGN node vs. VAR node vs. FUNCTION node
 *    - Elimination of empty/pure object literals
 *    - Warning JSC_UNSAFE_THIS on non-constructor/undocumented functions referencing 'this'
 * 8. declareVarsForObjLitValues:
 *    - Getter/Setter definitions skipped
 *    - Non-identifier property names assigned arbitrary names
 *    - Property names with '$' encoded as '$0'
 * 9. addStubsForUndeclaredProperties:
 *    - Adding uninitialized global variable stubs for properties assigned in local scopes
 * -----------------------------------------------------------------------------------------
 */
public class CollapsePropertiesGeminiTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window; function alert(s) {} function parseInt(s) {}"
      + "/** @constructor */ function String() {}; String.fromCharCode = function(x) {};"
      + "/** @constructor */ function Object() {};"
      + "var arguments;";

  private boolean enableCheckAliases = false;
  private boolean collapsePropertiesOnExternTypes = false;

  public CollapsePropertiesGeminiTest() {
    super(EXTERNS);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableCheckAliases = false;
    collapsePropertiesOnExternTypes = false;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseProperties(
        compiler, collapsePropertiesOnExternTypes, enableCheckAliases);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 931 Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J ground truth defect in CollapsePropertiesTest::testIssue931.
   * Tests that a simple stub declaration `a.b;` correctly allows properties
   * of the stub (e.g. `a.b.c = function() {};`) to be prefixed with the stub's
   * flattened alias rather than leaving mismatched references or breaking ancestry.
   */
  @Test(timeout = 4000)
  public void testIssue931() {
    test(
        "var a = {};\n"
        + "a.b;\n"
        + "a.b.c = function() {};",
        "var a$b;\n"
        + "a$b.c = function() {};");
  }

  @Test(timeout = 4000)
  public void testIssue931_primitiveProperty() {
    test(
        "var a = {};\n"
        + "a.b;\n"
        + "a.b.c = 1;",
        "var a$b;\n"
        + "a$b.c = 1;");
  }

  @Test(timeout = 4000)
  public void testIssue931_stubWithCall() {
    test(
        "var a = {};\n"
        + "a.b;\n"
        + "a.b.c = function() {};\n"
        + "a.b.c();",
        "var a$b;\n"
        + "a$b.c = function() {};\n"
        + "a$b.c();");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicObjectLiteralCollapse() {
    test(
        "var a = {}; a.b = 1; a.c = 2;",
        "var a$b = 1; var a$c = 2;");
  }

  @Test(timeout = 4000)
  public void testNestedObjectLiteralCollapse() {
    test(
        "var a = { b: { c: 1 } };",
        "var a$b$c = 1;");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyDiscard() {
    test(
        "var a = { b: 1, c: 2 };",
        "var a$b = 1; var a$c = 2;");
  }

  @Test(timeout = 4000)
  public void testFunctionPropertyCollapse() {
    test(
        "var a = {}; a.b = function() {}; a.b.c = 1;",
        "var a$b = function() {}; var a$b$c = 1;");
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionDeclarationWithProperty() {
    test(
        "function a() {} a.b = 99;",
        "function a() {} var a$b = 99;");
  }

  @Test(timeout = 4000)
  public void testFunctionCallTargetFreeCall() {
    test(
        "var a = {}; a.b = function() {}; a.b();",
        "var a$b = function() {}; a$b();");
  }

  @Test(timeout = 4000)
  public void testDollarSignEncodingInProperties() {
    test(
        "var a = {}; a.b$c = 1; var d = a.b$c;",
        "var a$b$0c = 1; var d = a$b$0c;");
  }

  @Test(timeout = 4000)
  public void testComplexAssignmentTwinReferences() {
    test(
        "var a = {}; var b; b = a.c = 5;",
        "var a$c; var b; b = a$c = 5;");
  }

  @Test(timeout = 4000)
  public void testUndeclaredPropertyStubAdded() {
    test(
        "var a = {}; function f() { a.b = 1; }",
        "var a$b; function f() { a$b = 1; }");
  }

  @Test(timeout = 4000)
  public void testConstructorDocPreventsUnsafeThisWarning() {
    test(
        "var a = {}; /** @constructor */ a.b = function() { this.x = 1; };",
        "/** @constructor */ var a$b = function() { this.x = 1; };");
  }

  @Test(timeout = 4000)
  public void testThisTypeDocPreventsUnsafeThisWarning() {
    test(
        "var a = {}; /** @this {Object} */ a.b = function() { this.x = 1; };",
        "var a$b = function() { this.x = 1; };");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyObjectLiteralElimination() {
    test(
        "var a = {};",
        "");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGettersSettersNotCollapsed() {
    testSame("var a = { get b() { return 1; }, set b(val) {} };");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNonIdentifierKey() {
    test(
        "var a = { 'invalid-id': 10 };",
        "var a$1 = 10;");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNumericKey() {
    test(
        "var a = { 0: 42 };",
        "var a$1 = 42;");
  }

  @Test(timeout = 4000)
  public void testChainedPropertyAccess() {
    test(
        "var a = { b: { c: { d: 5 } } }; var x = a.b.c.d;",
        "var a$b$c$d = 5; var x = a$b$c$d;");
  }

  @Test(timeout = 4000)
  public void testGlobalAssignWithoutVar() {
    test(
        "a = {}; a.b = 1;",
        "var a$b = 1;");
  }

  @Test(timeout = 4000)
  public void testBracketAccessPreventsCollapse() {
    testSame("var a = {}; a['b'] = 1;");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths (Warnings & Unsafe Patterns)
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnsafeThisWarningReported() {
    test(
        "var a = {}; a.b = function() { return this; };",
        "var a$b = function() { return this; };",
        CollapseProperties.UNSAFE_THIS);
  }

  @Test(timeout = 4000)
  public void testUnsafeNamespaceAliasingWarning() {
    test(
        "/** @constructor */ function Foo() {} var Bar = Foo;",
        "/** @constructor */ function Foo() {} var Bar = Foo;",
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  @Test(timeout = 4000)
  public void testNamespaceRedefinedWarning() {
    test(
        "/** @constructor */ function Foo() {} Foo = function() {};",
        "/** @constructor */ function Foo() {} Foo = function() {};",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  @Test(timeout = 4000)
  public void testNamespaceDeletedWarning() {
    test(
        "/** @constructor */ function Foo() {} delete Foo;",
        "/** @constructor */ function Foo() {} delete Foo;",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  @Test(timeout = 4000)
  public void testNamespaceRedefinedInLocalScopeWarning() {
    test(
        "/** @constructor */ function Foo() {} function g() { Foo = function() {}; }",
        "/** @constructor */ function Foo() {} function g() { Foo = function() {}; }",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  // =========================================================================
  // Partition E: Local Alias Inlining and Externs Processing
  // =========================================================================

  @Test(timeout = 4000)
  public void testInlineLocalAliasOfGlobalObject() {
    enableCheckAliases = true;
    test(
        "var a = { b: 1 }; function f() { var x = a; return x.b; }",
        "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineReassignedLocalAlias() {
    enableCheckAliases = true;
    testSame(
        "var a = { b: 1 }; function f() { var x = a; x = {}; return x.b; }");
  }

  @Test(timeout = 4000)
  public void testCollapsePropertiesOnExternTypesEnabled() {
    collapsePropertiesOnExternTypes = true;
    test(
        "String.myProp = 10; var x = String.myProp;",
        "var String$myProp = 10; var x = String$myProp;");
  }

  @Test(timeout = 4000)
  public void testCollapsePropertiesOnExternTypesDisabled() {
    collapsePropertiesOnExternTypes = false;
    testSame("String.myProp = 10; var x = String.myProp;");
  }

  @Test(timeout = 4000)
  public void testConstantPropertyPreservedOnCollapse() {
    test(
        "var a = {}; /** @const */ a.B = 10;",
        "/** @const */ var a$B = 10;");
  }
}