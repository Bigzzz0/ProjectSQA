package com.google.javascript.jscomp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.google.javascript.jscomp.InlineVariables
 * Target Defects: Defects4J Issue 378 / Arguments modification & escaping regressions
 * =========================================================================================
 * 1. Branch Coverage Targets:
 *    - Mode Filter Branches:
 *      * Mode.ALL -> Predicates.alwaysTrue()
 *      * Mode.LOCALS_ONLY -> IdentifyLocals (var.scope.isLocal())
 *      * Mode.CONSTANTS_ONLY -> IdentifyConstants (var.isConst())
 *    - Variable Forbid Checks (isVarInlineForbidden):
 *      * CodingConvention.isExported(var.name) (e.g., names starting with '_')
 *      * RenameProperties.RENAME_PROPERTY_FUNCTION_NAME
 *      * Stale vars set (already mutated/inlined vars)
 *    - Constant Inlining (isInlineableDeclaredConstant):
 *      * Non-immutable value rejection
 *      * Uninitialized / external @const rejection
 *      * String threshold check (inlineAllStrings == false vs true)
 *    - Non-Constant Inlining Heuristics (inlineNonConstants):
 *      * Immutable & well-defined (refCount > 1) -> inlineWellDefinedVariable
 *      * Uninitialized var (init == null) -> NodeUtil.newUndefinedNode (void 0)
 *      * Single read (refCount == firstRefAfterInit) -> canInline heuristics
 *      * Declaration != initialization && refCount == 2
 *      * Alias candidate chain collection & candidate inlining
 *    - Safety Guards (canInline & canMoveModerately):
 *      * Basic block boundary checks (same block requirement)
 *      * Context protection: GETPROP in CALL node (e.g., var a = b.c; a();)
 *      * Subclass relationship calls (e.g., goog.inherits)
 *      * Iterating across intervening side-effects
 *      * Arguments object modifications or escapes
 *    - "this" Aliases:
 *      * Inlinable "this" alias vs escaped "this" alias
 * 2. Defect Zone Targeting:
 *    - testArgumentsModifiedInOuterFunction: Modifying arguments[i] inside function body
 *    - testArgumentsModifiedInInnerFunction: Modifying arguments in nested IIFE
 *    - testIssue378ModifiedArguments1 & 2: Arguments reassignment before callback invocation
 *    - testIssue378EscapedArguments1, 2, 4: Arguments escaping via call arguments/closures
 * =========================================================================================
 */
public class InlineVariablesGeminiTest extends CompilerTestCase {

  private InlineVariables.Mode mode = InlineVariables.Mode.ALL;
  private boolean inlineAllStrings = true;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new InlineVariables(compiler, mode, inlineAllStrings);
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    mode = InlineVariables.Mode.ALL;
    inlineAllStrings = true;
  }

  @After
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth / Issue 378)
  // =========================================================================

  @Test(timeout = 4000)
  public void testArgumentsModifiedInOuterFunction() {
    testSame("function f(a) { var b = a; arguments[0] = 2; return b; }");
  }

  @Test(timeout = 4000)
  public void testArgumentsModifiedInInnerFunction() {
    testSame("function f(a) { var b = a; (function() { arguments[0] = 2; })(); return b; }");
  }

  @Test(timeout = 4000)
  public void testIssue378ModifiedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  arguments[0] = this;\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

  @Test(timeout = 4000)
  public void testIssue378ModifiedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  arguments[0] = 1;\n" +
        "  (function() { arguments[0] = 2; })();\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

  @Test(timeout = 4000)
  public void testIssue378EscapedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments, this);\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

  @Test(timeout = 4000)
  public void testIssue378EscapedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  var f2 = function() { h(arguments, this); };\n" +
        "  f2();\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

  @Test(timeout = 4000)
  public void testIssue378EscapedArguments4() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  var h = function() { arguments[0] = this; };\n" +
        "  h();\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInlineSingleUsePrimitiveVar() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  @Test(timeout = 4000)
  public void testInlineMultipleReadsOfImmutableVar() {
    test("function f() { var x = 1; return x + x; }",
         "function f() { return 1 + 1; }");
  }

  @Test(timeout = 4000)
  public void testInlineFunctionExpression() {
    test("var f = function() {}; f();",
         "(function() {})();");
  }

  @Test(timeout = 4000)
  public void testInlineFunctionDeclaration() {
    test("function f() {} f();",
         "(function() {})();");
  }

  @Test(timeout = 4000)
  public void testInlineUninitializedVarToUndefined() {
    test("function f() { var x; return x; }",
         "function f() { return void 0; }");
  }

  @Test(timeout = 4000)
  public void testInlineVarAssignedLaterInBlock() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { return 1; }");
  }

  @Test(timeout = 4000)
  public void testInlineMultiVarDeclarationFull() {
    test("function f() { var x = 1, y = 2; return x + y; }",
         "function f() { return 1 + 2; }");
  }

  @Test(timeout = 4000)
  public void testInlineMultiVarDeclarationPartial() {
    test("function f() { var x = 1, y = 2; y++; return x + y; }",
         "function f() { var y = 2; y++; return 1 + y; }");
  }

  @Test(timeout = 4000)
  public void testInlineAliasCandidates() {
    test("function f(a) { var b = a; return b; }",
         "function f(a) { return a; }");
  }

  @Test(timeout = 4000)
  public void testInlineChainedAliases() {
    test("var a = 1; var b = a; var c = b; return c;",
         "return 1;");
  }

  @Test(timeout = 4000)
  public void testInlineThisAlias() {
    test("function f() { var self = this; return self.foo(); }",
         "function f() { return this.foo(); }");
  }

  @Test(timeout = 4000)
  public void testLocalsOnlyModePreservesGlobals() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    test("var x = 1; function f() { var y = 2; return y; } return x;",
         "var x = 1; function f() { return 2; } return x;");
  }

  @Test(timeout = 4000)
  public void testConstantsOnlyModeFiltersNonConstants() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    test("var x = 1; var CONST_A = 2; return x + CONST_A;",
         "var x = 1; return x + 2;");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Heuristic Safeguards
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoNotInlineAcrossControlStructures() {
    testSame("var a = foo(); if (true) { alert(a); }");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineIntoInsideIfBranchNonLiteral() {
    testSame("var a = \"a\"; if (1) { alert(a); }");
  }

  @Test(timeout = 4000)
  public void testInlineInsideIfConditionSameBlock() {
    test("var a = \"a\"; if (a) { alert(a); }",
         "if (\"a\") { alert(\"a\"); }");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineMethodCallContextChange() {
    testSame("var a = b.c; a();");
  }

  @Test(timeout = 4000)
  public void testInlineMethodCallWhenPassedAsArgument() {
    test("var a = b.c; f(a);",
         "f(b.c);");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineVarDeclaredInForLoopHeader() {
    testSame("for (var x = 0; x < 10; x++) { alert(x); }");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineReassignedVariable() {
    testSame("var x = 1; x = 2; return x;");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineReadBeforeDeclaration() {
    testSame("function f() { alert(x); var x = 1; }");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineRecursiveFunctionReference() {
    testSame("var f = function() { return f(); }; return f();");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineEscapedThisAlias() {
    testSame("function f() { var self = this; function g() { return self.foo(); } return g; }");
  }

  @Test(timeout = 4000)
  public void testStringCostCalculationWithInlineAllStringsFalse() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    inlineAllStrings = false;
    testSame("var CONST_STR = 'this is a very long string that uses many bytes'; " +
             "return CONST_STR + CONST_STR + CONST_STR + CONST_STR;");
  }

  @Test(timeout = 4000)
  public void testStringCostCalculationWithInlineAllStringsTrue() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    inlineAllStrings = true;
    test("var CONST_STR = 'this is a very long string'; return CONST_STR + CONST_STR;",
         "return 'this is a very long string' + 'this is a very long string';");
  }

  @Test(timeout = 4000)
  public void testNoInlineSubclassesConfusingInheritanceCall() {
    testSame("var C = function() {}; goog.inherits(C, Base);");
  }

  // =========================================================================
  // Partition D: Defensive Guards & Forbidden Names
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoNotInlineExportedVariables() {
    testSame("var _x = 1; return _x;");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineSpecialRenamePropertyFunction() {
    testSame("var JSCompiler_renameProperty = function(p) { return p; }; " +
             "alert(JSCompiler_renameProperty('foo'));");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineUninitializedConst() {
    testSame("/** @const */ var CONST_UNINITIALIZED;");
  }

  @Test(timeout = 4000)
  public void testDoNotInlineMutableConst() {
    testSame("/** @const */ var CONST_MUTABLE = { a: 1 }; return CONST_MUTABLE;");
  }

  // =========================================================================
  // Partition E: Lifecycle, Enums & Standalone Pipeline Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testModeEnumCoverage() {
    assertEquals(3, InlineVariables.Mode.values().length);
    assertEquals(InlineVariables.Mode.ALL, InlineVariables.Mode.valueOf("ALL"));
    assertEquals(InlineVariables.Mode.LOCALS_ONLY, InlineVariables.Mode.valueOf("LOCALS_ONLY"));
    assertEquals(InlineVariables.Mode.CONSTANTS_ONLY, InlineVariables.Mode.valueOf("CONSTANTS_ONLY"));
  }

  @Test(timeout = 4000)
  public void testProcessDirectlyOnEmptyAstNodes() {
    Compiler compiler = new Compiler();
    com.google.javascript.rhino.Node externs = new com.google.javascript.rhino.Node(com.google.javascript.rhino.Token.BLOCK);
    com.google.javascript.rhino.Node root = new com.google.javascript.rhino.Node(com.google.javascript.rhino.Token.BLOCK);
    InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.ALL, true);
    pass.process(externs, root);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testScopeExitWithMultipleDistinctFunctions() {
    test("function f() { var x = 1; return x; } function g() { var x = 2; return x; }",
         "function f() { return 1; } function g() { return 2; }");
  }
}