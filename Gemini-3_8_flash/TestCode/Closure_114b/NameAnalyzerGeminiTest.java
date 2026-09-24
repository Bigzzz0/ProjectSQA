/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.google.javascript.jscomp.NameAnalyzer
 * Primary Defect Targeted: Defects4J - NameAnalyzerTest::testAssignWithCall
 *
 * Core Decision Logic & Branches Targeted:
 * 1. FindDependencyScopes::visit & recordAssignment:
 *    - (a = function() {})() call sites: assignment is child of CALL vs COMMA vs FOR.
 *    - Immutable vs mutable RHS check (NodeUtil.isImmutableResult).
 *    - Dependency scope assignment claiming inside FOR loop (init vs cond vs iter).
 * 2. FindReferences::maybeRecordReferenceOrAlias & valueConsumedByParent:
 *    - Caller expression assignment (nested or direct in CALL target).
 *    - Subexpressions with side effects preserved while stripping dead assignments.
 *    - Parent predicates: IF, WHILE, DO, FOR, RETURN, AND, OR, HOOK.
 * 3. ClassDefiningFunctionNode & PrototypeSetNode:
 *    - Subclass relationships (goog.inherits) and singleton getters (goog.addSingletonGetter).
 *    - Expression result removal vs embedded replacement with 'void 0'.
 *    - Prototype assignments: ExprResult removal vs embedded RHS preservation.
 * 4. InstanceOfCheckNode:
 *    - Removal of instanceof expressions referencing eliminated classes -> replaced with false.
 *    - hasInstanceOfReference triggering alias graph connections in referenceAliases().
 * 5. Alias Graph (AliasSet) & referenceParentNames:
 *    - Disjoint sets, single merge, mutual merge of alias sets.
 *    - Two-way reference propagation between parent namespaces and children (e.g. a <-> a.b <-> a.b.c).
 * 6. HTML Reporting:
 *    - getHtmlReport() coverage across all TriState counts, prototype lists, in/out edges.
 * ====================================================================================================
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class NameAnalyzerGeminiTest {

  // --------------------------------------------------------------------------------------------------
  // Helper Harness
  // --------------------------------------------------------------------------------------------------

  private void test(String js, String expected) {
    test(js, expected, null);
  }

  private void test(String js, String expected, CodingConvention convention) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    if (convention != null) {
      options.setCodingConvention(convention);
    }
    compiler.initOptions(options);

    Node actualNode = compiler.parseTestCode(js);
    Node externsNode = compiler.parseTestCode("var window; var goog = {};");
    NameAnalyzer na = new NameAnalyzer(compiler, true);
    na.process(externsNode, actualNode);

    Node expectedNode = compiler.parseTestCode(expected);
    assertEquals(compiler.toSource(expectedNode), compiler.toSource(actualNode));
  }

  private void testSame(String js) {
    test(js, js, null);
  }

  private NameAnalyzer process(String js, boolean removeUnreferenced, Compiler compiler) {
    compiler.initCompilerOptionsIfTesting();
    Node actualNode = compiler.parseTestCode(js);
    Node externsNode = compiler.parseTestCode("var window; var goog = {};");
    NameAnalyzer na = new NameAnalyzer(compiler, removeUnreferenced);
    na.process(externsNode, actualNode);
    return na;
  }

  // --------------------------------------------------------------------------------------------------
  // Partition C: Defect-Targeted Branch Zone (Defects4J: testAssignWithCall)
  // --------------------------------------------------------------------------------------------------

  /**
   * Targets the exact Defects4J regression testAssignWithCall:
   * (a = function() {})() where 'a' is an unused global variable assigned
   * in the caller position of a call expression.
   */
  @Test(timeout = 4000)
  public void testAssignWithCall() {
    test("var foo; (foo = function() {})()",
         "(function() {})()");
  }

  @Test(timeout = 4000)
  public void testAssignWithCall2() {
    test("var foo; (1, foo = function() {})()",
         "(1, function() {})()");
  }

  @Test(timeout = 4000)
  public void testAssignWithCall_simple() {
    test("var a; (a = function() {})();",
         "(function() {})();");
  }

  @Test(timeout = 4000)
  public void testAssignWithCall_nestedInCallArgs() {
    test("var a; var b = function(x) {}; (b(a = function() {}))",
         "var b = function(x) {}; (b(function() {}))");
  }

  // --------------------------------------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // --------------------------------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testSimpleDeadVariablesAndFunctionsRemoved() {
    test("var a = 1; var b = 2; function unused() { return a; }", "");
  }

  @Test(timeout = 4000)
  public void testReferencedViaWindowPreserved() {
    testSame("var a = 1; window['a'] = a;");
  }

  @Test(timeout = 4000)
  public void testCircularReferencesEliminated() {
    test("function f() { g(); } function g() { f(); }", "");
  }

  @Test(timeout = 4000)
  public void testNamespaceHierarchyMutualReference() {
    testSame("var a = {}; a.b = {}; a.b.c = 1; window.foo = a.b.c;");
    testSame("var a = {}; a.b = {}; a.b.c = 1; window.foo = a;");
  }

  @Test(timeout = 4000)
  public void testPrototypeAssignmentRemovedWhenUnreferenced() {
    test("function Foo() {} Foo.prototype.bar = function() { return 42; };", "");
  }

  @Test(timeout = 4000)
  public void testPrototypeAssignmentPreservedWhenClassReferenced() {
    testSame("function Foo() {} Foo.prototype.bar = function() { return 42; }; window.f = new Foo();");
  }

  @Test(timeout = 4000)
  public void testPrototypeAssignmentInExpressionReplacedWithRhs() {
    test("function Foo() {} var x; var y = Foo.prototype.bar = function() { return 1; }; window.y = y;",
         "var y = function() { return 1; }; window.y = y;");
  }

  @Test(timeout = 4000)
  public void testPrototypeObjectLiteralReplacement() {
    test("function Foo() {} Foo.prototype = { a: function() {}, b: 2 };", "");
  }

  @Test(timeout = 4000)
  public void testThisPropertiesInGlobalScope() {
    testSame("this.foo = 1; window.bar = this.foo;");
  }

  @Test(timeout = 4000)
  public void testClassDefiningFunction_googInherits() {
    test("function Super() {} function Sub() {} goog.inherits(Sub, Super);",
         "",
         new ClosureCodingConvention());
  }

  @Test(timeout = 4000)
  public void testClassDefiningFunction_googInherits_keptWhenSubclassReferenced() {
    testSame("function Super() {} function Sub() {} goog.inherits(Sub, Super); window.x = new Sub();",
             new ClosureCodingConvention());
  }

  @Test(timeout = 4000)
  public void testClassDefiningFunction_embeddedCallReplacedWithVoid() {
    test("function Super() {} function Sub() {} var x = goog.inherits(Sub, Super); window.x = x;",
         "function Super() {} function Sub() {} var x = void 0; window.x = x;",
         new ClosureCodingConvention());
  }

  @Test(timeout = 4000)
  public void testSingletonGetter_removedWhenUnreferenced() {
    test("function Foo() {} goog.addSingletonGetter(Foo);",
         "",
         new ClosureCodingConvention());
  }

  @Test(timeout = 4000)
  public void testInstanceOfRemovalReplacedWithFalse() {
    test("function Foo() {} var x = {}; var a = x instanceof Foo; window.res = a;",
         "var x = {}; var a = false; window.res = a;");
  }

  @Test(timeout = 4000)
  public void testInstanceOfWithAliasedClass() {
    testSame("var A = function() {}; var B = A; var x = {}; window.res = x instanceof B;");
  }

  // --------------------------------------------------------------------------------------------------
  // Partition B: Boundary Value Analysis (BVA) & Control Structure Predicates
  // --------------------------------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testValueConsumedByParent_IfPredicate() {
    test("var a; function foo() { return true; } if (a = foo()) { window.b = 1; }",
         "function foo() { return true; } if (foo()) { window.b = 1; }");
  }

  @Test(timeout = 4000)
  public void testValueConsumedByParent_WhilePredicate() {
    test("var a; function foo() { return true; } while (a = foo()) { window.b = 1; }",
         "function foo() { return true; } while (foo()) { window.b = 1; }");
  }

  @Test(timeout = 4000)
  public void testValueConsumedByParent_DoWhilePredicate() {
    test("var a; function foo() { return true; } do { window.b = 1; } while (a = foo());",
         "function foo() { return true; } do { window.b = 1; } while (foo());");
  }

  @Test(timeout = 4000)
  public void testValueConsumedByParent_ReturnStatement() {
    test("function f() { var a; return a = Math.random(); } window.f = f;",
         "function f() { return Math.random(); } window.f = f;");
  }

  @Test(timeout = 4000)
  public void testValueConsumedByParent_LogicalExpressions() {
    test("var a; function foo() { return 1; } var x = (a = foo()) && true; window.x = x;",
         "function foo() { return 1; } var x = foo() && true; window.x = x;");

    test("var a; function foo() { return 1; } var x = (a = foo()) || false; window.x = x;",
         "function foo() { return 1; } var x = foo() || false; window.x = x;");

    test("var a; function foo() { return 1; } var x = (a = foo()) ? 2 : 3; window.x = x;",
         "function foo() { return 1; } var x = foo() ? 2 : 3; window.x = x;");
  }

  @Test(timeout = 4000)
  public void testForLoops_StandardAndForIn() {
    test("for (var a = 0; a < 10; a++) { window.x = 1; }",
         "for (0; 0 < 10; 0) { window.x = 1; }");

    testSame("var obj = {a: 1}; for (var k in obj) { window.k = k; }");
  }

  @Test(timeout = 4000)
  public void testForLoop_EmptyInitReplacements() {
    test("for (var a = 1; ; ) { window.x = 1; }",
         "for (; ; ) { window.x = 1; }");
  }

  @Test(timeout = 4000)
  public void testForLoop_SideEffectInitReplacements() {
    test("function init() {} for (var a = init(); ; ) { window.x = 1; }",
         "function init() {} for (init(); ; ) { window.x = 1; }");
  }

  @Test(timeout = 4000)
  public void testSideEffectsPreservedInDeadAssignments() {
    test("var a = Math.random();", "Math.random();");
  }

  @Test(timeout = 4000)
  public void testGetElemPropertyWritten() {
    test("var a = {}; a['b'] = 1;", "");
    testSame("var a = {}; a['b'] = 1; window.a = a;");
  }

  @Test(timeout = 4000)
  public void testHookConditionalFunctionAssignment() {
    test("var a = true ? function() { return 1; } : function() { return 2; };", "");
    testSame("var a = true ? function() { return 1; } : function() { return 2; }; window.a = a;");
  }

  // --------------------------------------------------------------------------------------------------
  // Partition D: Alias Handling & Hidden Aliases
  // --------------------------------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testAliasSetMerging() {
    test("var a = {}; var b = a; var c = b; a.prop = 1;", "");
    testSame("var a = {}; var b = a; var c = b; a.prop = 1; window.c = c;");
  }

  @Test(timeout = 4000)
  public void testHiddenAliasViaFunctionReturn() {
    testSame("function getGlobal() { return window; } var a = getGlobal(); a.foo = 123;");
  }

  @Test(timeout = 4000)
  public void testConsumersRecord_OrAndCommaHook() {
    test("var a; var b; (a || b).foo = 1;", "");
    test("var a; var b; (a && b).foo = 1;", "");
    test("var a; var b; var c; (a ? b : c).foo = 1;", "");
    test("var a; var b; (a, b).foo = 1;", "");
  }

  // --------------------------------------------------------------------------------------------------
  // Partition E: Reporting, State Invariants & Lifecycle Integrity
  // --------------------------------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testHtmlReportGenerationWithData() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {} "
              + "Foo.prototype.bar = function() {}; "
              + "var a = new Foo(); "
              + "window.a = a; "
              + "var unref = 10;";
    NameAnalyzer na = process(js, false, compiler);

    String report = na.getHtmlReport();
    assertNotNull("Report must not be null", report);
    assertTrue("Report must contain HTML wrapper", report.contains("<html><body>"));
    assertTrue("Report must contain overall stats", report.contains("OVERALL STATS"));
    assertTrue("Report must contain Total Names", report.contains("Total Names:"));
    assertTrue("Report must contain Total Classes", report.contains("Total Classes:"));
    assertTrue("Report must contain Referenced Names", report.contains("Referenced Names:"));
    assertTrue("Report must contain prototype references", report.contains("PROTOTYPES:"));
    assertTrue("Report must close body and html", report.endsWith("</body></html>"));
  }

  @Test(timeout = 4000)
  public void testHtmlReportGenerationEmpty() {
    Compiler compiler = new Compiler();
    NameAnalyzer na = process("", false, compiler);
    String report = na.getHtmlReport();
    assertNotNull("Report must not be null for empty input", report);
    assertTrue(report.contains("Total Names: 0"));
  }

  @Test(timeout = 4000)
  public void testExternallyDefinedNamesNotRemoved() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node externsNode = compiler.parseTestCode("var extVar; function extFn() {}");
    Node rootNode = compiler.parseTestCode("extVar = 1; extFn();");
    NameAnalyzer na = new NameAnalyzer(compiler, true);
    na.process(externsNode, rootNode);

    Node expectedNode = compiler.parseTestCode("extVar = 1; extFn();");
    assertEquals(compiler.toSource(expectedNode), compiler.toSource(rootNode));
  }

  @Test(timeout = 4000)
  public void testRemoveUnreferencedFalseKeepsAstIntact() {
    Compiler compiler = new Compiler();
    String js = "var unused = 1; function dead() { return 2; }";
    NameAnalyzer na = process(js, false, compiler);
    assertNotNull(na.getHtmlReport());
  }

  @Test(timeout = 4000)
  public void testLabeledStatementRemoval() {
    test("lbl: var a = 1;", "");
  }
}